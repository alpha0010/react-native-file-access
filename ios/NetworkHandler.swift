class NetworkHandler: NSObject, URLSessionDownloadDelegate {
    public static let FETCH_EVENT = "FetchEvent"
    private static let MIN_EVENT_INTERVAL: TimeInterval = 0.15

    private var currentUrl = ""
    private var destination: String?
    private let emitter: RCTEventEmitter
    private var lastEventTime = Date.distantPast
    private let onComplete: () -> Void
    private let requestId: Int

    init(requestId: NSNumber, emitter: RCTEventEmitter, onComplete: @escaping () -> Void) {
        self.emitter = emitter
        self.requestId = requestId.intValue
        self.onComplete = onComplete
    }

    func fetch(resource: String, config: NSDictionary) -> URLSessionDownloadTask? {
        guard let url = URL(string: resource) else {
            onComplete()
            onFetchError("'\(resource)' is not a url.")
            return nil
        }

        currentUrl = resource
        destination = (config["path"] as? String)?.path()

        var request = URLRequest(url: url)
        if let method = config["method"] as? String {
            request.httpMethod = method
        }
        if let body = config["body"] as? String {
            request.httpBody = body.data(using: .utf8)
        }
        if let headers = config["headers"] as? NSDictionary {
            for (key, value) in headers {
                if let headerName = key as? String, let headerValue = value as? String {
                    request.addValue(headerValue, forHTTPHeaderField: headerName)
                }
            }
        }

        let session = URLSession(
            configuration: URLSessionConfiguration.default,
            delegate: self,
            delegateQueue: OperationQueue.current
        )
        let downloadTask = session.downloadTask(with: request)
        downloadTask.resume()
        return downloadTask
    }

    func urlSession(_ session: URLSession, downloadTask: URLSessionDownloadTask, didFinishDownloadingTo location: URL) {
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

        self.emitter.sendEvent(withName: NetworkHandler.FETCH_EVENT, body: [
            "requestId": self.requestId,
            "state": "complete",
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

        self.emitter.sendEvent(withName: NetworkHandler.FETCH_EVENT, body: [
            "requestId": requestId,
            "state": "progress",
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
        self.emitter.sendEvent(withName: NetworkHandler.FETCH_EVENT, body: [
            "requestId": self.requestId,
            "state": "error",
            "message": message
        ])
    }
}
