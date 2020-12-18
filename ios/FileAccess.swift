@objc(FileAccess)
class FileAccess: NSObject {
    @objc func constantsToExport() -> NSObject {
        return [
            "CacheDir": NSSearchPathForDirectoriesInDomains(.cachesDirectory, .userDomainMask, true).first!,
            "DocumentDir": NSSearchPathForDirectoriesInDomains(.documentDirectory, .userDomainMask, true).first!,
            "LibraryDir": NSSearchPathForDirectoriesInDomains(.libraryDirectory, .userDomainMask, true).first!,
            "MainBundleDir": Bundle.main.bundlePath
        ] as NSObject
    }

    @objc(cp:withTarget:withResolver:withRejecter:)
    func cp(source: String, target: String, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) -> Void {
        do {
            try FileManager.default.copyItem(atPath: source, toPath: target)
            resolve(nil)
        } catch {
            reject("ERR", "Failed to copy '\(source)' to '\(target)'.", error)
        }
    }

    @objc(df:withRejecter:)
    func df(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) -> Void {
        do {
            let stat = try FileManager.default.attributesOfFileSystem(
                forPath: NSSearchPathForDirectoriesInDomains(.documentDirectory, .userDomainMask, true).first!
            )

            resolve([
                "internal_free": stat[.systemFreeSize],
                "internal_total": stat[.systemSize]
            ])
        } catch {
            reject("ERR", "Failed to stat filesystem.", error)
        }
    }

    @objc(exists:withResolver:withRejecter:)
    func exists(path: String, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) -> Void {
        resolve(FileManager.default.fileExists(atPath: path))
    }

    @objc(fetch:withConfig:withResolver:withRejecter:)
    func fetch(resource: String, config: NSDictionary, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
        guard let url = URL(string: resource) else {
            reject("ERR", "'\(resource)' is not a url.", nil)
            return
        }
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

        let downloadTask = URLSession.shared.downloadTask(with: request) {
            locationOrNil, responseOrNil, errorOrNil in

            if let requestError = errorOrNil {
                reject("ERR", "Failed to fetch '\(resource)'.", requestError)
                return
            }
            guard let location = locationOrNil, let response = responseOrNil as? HTTPURLResponse else {
                reject("ERR", "Failed to fetch '\(resource)'.", nil)
                return
            }

            if let path = config["path"] as? String {
                let pathUrl = URL(fileURLWithPath: path)
                do {
                    try FileManager.default.removeItem(at: pathUrl)
                } catch {
                    // Ignored.
                }
                do {
                    try FileManager.default.moveItem(at: location, to: pathUrl)
                } catch {
                    reject("ERR", "Failed to save '\(resource)' to '\(path)'.", error)
                    return
                }
            }

            resolve([
                "headers": response.allHeaderFields,
                "ok": response.statusCode >= 200 && response.statusCode < 300,
                "redirected": false, // TODO: Determine actual value.
                "status": response.statusCode,
                "statusText": HTTPURLResponse.localizedString(forStatusCode: response.statusCode),
                "url": response.url?.absoluteString ?? ""
            ])
        }
        downloadTask.resume()
    }

    @objc(isDir:withResolver:withRejecter:)
    func isDir(path: String, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) -> Void {
        var isDir: ObjCBool = false
        resolve(
            FileManager.default.fileExists(atPath: path, isDirectory: &isDir)
                && isDir.boolValue
        )
    }

    @objc(ls:withResolver:withRejecter:)
    func ls(path: String, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) -> Void {
        do {
            try resolve(FileManager.default.contentsOfDirectory(atPath: path))
        } catch {
            reject("ERR", "Failed to list '\(path)'.", error)
        }
    }

    @objc(mkdir:withResolver:withRejecter:)
    func mkdir(path: String, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) -> Void {
        do {
            try FileManager.default.createDirectory(atPath: path, withIntermediateDirectories: true, attributes: nil)
        } catch {
            reject("ERR", "Failed to create directory '\(path)'.", error)
        }
    }

    @objc(mv:withTarget:withResolver:withRejecter:)
    func mv(source: String, target: String, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) -> Void {
        do {
            try FileManager.default.moveItem(atPath: source, toPath: target)
            resolve(nil)
        } catch {
            reject("ERR", "Failed to rename '\(source)' to '\(target)'.", error)
        }
    }

    @objc(readFile:withResolver:withRejecter:)
    func readFile(path: String, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) -> Void {
        do {
            try resolve(String(contentsOfFile: path))
        } catch {
            reject("ERR", "Failed to read '\(path)'.", error)
        }
    }

    @objc(unlink:withResolver:withRejecter:)
    func unlink(path: String, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) -> Void {
        do {
            try FileManager.default.removeItem(atPath: path)
            resolve(nil)
        } catch {
            reject("ERR", "Failed to unlink '\(path)'.", error)
        }
    }

    @objc(writeFile:withData:withResolver:withRejecter:)
    func writeFile(path: String, data: String, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) -> Void {
        do {
            try data.write(toFile: path, atomically: false, encoding: .utf8)
            resolve(nil)
        } catch {
            reject("ERR", "Failed to write to '\(path)'.", error)
        }
    }
}
