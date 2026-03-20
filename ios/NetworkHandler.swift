public typealias EventEmitter = (NSDictionary) -> Void

class NetworkHandler: NSObject, URLSessionDownloadDelegate {
    private static let MIN_EVENT_INTERVAL: TimeInterval = 0.15

    private var currentUrl = ""
    private var destination: String?
    private let emitOnProgress: EventEmitter
    private let emitOnError: EventEmitter
    private let emitOnComplete: EventEmitter
    private var lastEventTime = Date.distantPast
    private let onComplete: () -> Void
    private let requestId: Int

    init(requestId: Int, emitOnProgress: @escaping EventEmitter, emitOnError: @escaping EventEmitter, emitOnComplete: @escaping EventEmitter, onComplete: @escaping () -> Void) {
        self.emitOnProgress = emitOnProgress
        self.emitOnError = emitOnError
        self.emitOnComplete = emitOnComplete
        self.requestId = requestId
        self.onComplete = onComplete
    }

    func fetch(resource: String, body: String?, headers: NSDictionary?, method: String?, network: String?, path: String?) -> URLSessionDownloadTask? {
        guard let url = URL(string: resource) else {
            onComplete()
            onFetchError("'\(resource)' is not a url.")
            return nil
        }

        currentUrl = resource
        destination = path?.path()

        var request = URLRequest(url: url)
        if let method = method {
            request.httpMethod = method
        }
        if let body = body {
            request.httpBody = body.data(using: .utf8)
        }
        if let headers = headers {
            for (key, value) in headers {
                if let headerName = key as? String, let headerValue = value as? String {
                    request.addValue(headerValue, forHTTPHeaderField: headerName)
                }
            }
        }

        let sessionConf = URLSessionConfiguration.default
        if let network = network,
            network == "unmetered" {
            // Use an unmetered network (most likely WiFi).
            if #available(iOS 13.0, *) {
                sessionConf.allowsConstrainedNetworkAccess = false
                sessionConf.allowsExpensiveNetworkAccess = false
            } else {
                sessionConf.allowsCellularAccess = false
            }
        }
        let session = URLSession(
            configuration: sessionConf,
            delegate: self,
            delegateQueue: OperationQueue.current
        )
        let downloadTask = session.downloadTask(with: request)
        downloadTask.resume()
        return downloadTask
    }

    func urlSession(_ session: URLSession, downloadTask: URLSessionDownloadTask, didFinishDownloadingTo location: URL) {
        session.finishTasksAndInvalidate()
        onComplete()
        lastEventTime = Date.distantFuture
        guard let response = downloadTask.response as? HTTPURLResponse else {
            onFetchError("Failed to fetch '\(currentUrl)'.")
            return
        }

        if let path = destination {
            let pathUrl = URL(fileURLWithPath: path)
            try? FileManager.default.removeItem(at: pathUrl)
            do {
                try FileManager.default.moveItem(at: location, to: pathUrl)
            } catch {
                onFetchError("Failed to save '\(currentUrl)' to '\(path)'.", error)
                return
            }
        }

        self.emitOnComplete([
            "requestId": self.requestId,
            "headers": response.allHeaderFields,
            "ok": response.statusCode >= 200 && response.statusCode < 300,
            "redirected": false, // TODO: Determine actual value.
            "status": response.statusCode,
            "statusText": HTTPURLResponse.localizedString(forStatusCode: response.statusCode),
            "url": response.url?.absoluteString ?? ""
        ])
    }

    func urlSession(_ session: URLSession, task: URLSessionTask, didCompleteWithError error: Error?) {
        if let error = error {
            session.finishTasksAndInvalidate()
            onComplete()
            onFetchError("Failed to fetch '\(currentUrl)'.", error)
        }
    }

    func urlSession(_ session: URLSession, downloadTask: URLSessionDownloadTask, didWriteData bytesWritten: Int64, totalBytesWritten: Int64, totalBytesExpectedToWrite: Int64) {
        let currentTime = Date()
        guard currentTime.timeIntervalSince(lastEventTime) > NetworkHandler.MIN_EVENT_INTERVAL else {
            return
        }
        lastEventTime = currentTime

        self.emitOnProgress([
            "requestId": requestId,
            "bytesRead": totalBytesWritten,
            "contentLength": totalBytesExpectedToWrite,
            "done": false
        ])
    }

    private func onFetchError(_ msg: String, _ err: Error? = nil) {
        var message = msg
        if let errMsg = err?.localizedDescription {
            message += " " + errMsg
        }
        self.emitOnError([
            "requestId": self.requestId,
            "message": message
        ])
    }
}
