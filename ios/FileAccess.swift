import CommonCrypto

@objc(FileAccess)
class FileAccess: NSObject {
    @objc static func requiresMainQueueSetup() -> Bool {
        return false
    }

    @objc func constantsToExport() -> NSObject {
        return [
            "CacheDir": NSSearchPathForDirectoriesInDomains(.cachesDirectory, .userDomainMask, true).first!,
            "DocumentDir": NSSearchPathForDirectoriesInDomains(.documentDirectory, .userDomainMask, true).first!,
            "LibraryDir": NSSearchPathForDirectoriesInDomains(.libraryDirectory, .userDomainMask, true).first!,
            "MainBundleDir": Bundle.main.bundlePath
        ] as NSObject
    }

    @objc(appendFile:withData:withResolver:withRejecter:)
    func appendFile(path: String, data: String, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) -> Void {
        guard let encodedData = data.data(using: .utf8), let handle = FileHandle(forWritingAtPath: path) else {
            reject("ERR", "Failed to append to '\(path)'.", nil)
            return
        }

        handle.seekToEndOfFile()
        handle.write(encodedData)
        handle.closeFile()
        resolve(nil)
    }

    @objc(concatFiles:withTarget:withResolver:withRejecter:)
    func concatFiles(source: String, target: String, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) -> Void {
        guard let input = InputStream(fileAtPath: source), let output = OutputStream(toFileAtPath: target, append: true) else {
            reject("ERR", "Failed to concat '\(source)' to '\(target)'.", nil)
            return
        }

        input.open()
        output.open()
        var bytesCopied = 0
        let bufferSize = 8 * 1024
        var buffer = [UInt8](repeating: 0, count: bufferSize);
        var bytes = input.read(&buffer, maxLength: bufferSize)
        while bytes > 0 {
            output.write(buffer, maxLength: bytes)
            bytesCopied += bytes
            bytes = input.read(&buffer, maxLength: bufferSize)
        }
        output.close()
        input.close()

        resolve(bytesCopied)
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

    @objc(cpAsset:withTarget:withResolver:withRejecter:)
    func cpAsset(asset: String, target: String, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) -> Void {
        guard let assetPath = Bundle.main.path(forResource: asset, ofType: nil) else {
            reject("ENOENT", "Asset \(asset) not found", nil)
            return
        }

        do {
            try FileManager.default.copyItem(atPath: assetPath, toPath: target)
            resolve(nil)
        } catch {
            reject("ERR", "Failed to copy '\(asset)' to '\(target)'.", error)
        }
    }

    @objc(cpExternal:withTargetName:withDir:withResolver:withRejecter:)
    func cpExternal(source: String, targetName: String, dir: String, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) -> Void {
        let targetFolder: String
        switch dir {
        case "audio":
            targetFolder = NSSearchPathForDirectoriesInDomains(.musicDirectory, .userDomainMask, true).first!
        case "downloads":
            targetFolder = NSSearchPathForDirectoriesInDomains(.downloadsDirectory, .userDomainMask, true).first!
        case "images":
            targetFolder = NSSearchPathForDirectoriesInDomains(.picturesDirectory, .userDomainMask, true).first!
        case "video":
            targetFolder = NSSearchPathForDirectoriesInDomains(.moviesDirectory, .userDomainMask, true).first!
        default:
            reject("ERR", "Unknown destination '\(dir)'.", nil)
            return
        }

        try? FileManager.default.createDirectory(atPath: targetFolder, withIntermediateDirectories: true, attributes: nil)

        let targetUrl = URL(fileURLWithPath: targetFolder, isDirectory: true)
            .appendingPathComponent(targetName, isDirectory: false)
        cp(source: source, target: targetUrl.path, resolve: resolve, reject: reject)
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
                try? FileManager.default.removeItem(at: pathUrl)
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

    @objc(hash:withAlgorithm:withResolver:withRejecter:)
    func hash(path: String, algorithm: String, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) -> Void {
        guard let data = NSData(contentsOfFile: path) else {
            reject("ERR", "Failed to read '\(path)'.", nil)
            return
        }

        let hashAlgo: (UnsafeRawPointer?, CC_LONG, UnsafeMutablePointer<UInt8>?) -> UnsafeMutablePointer<UInt8>?
        let digestLength: Int32
        switch algorithm {
        case "MD5":
            hashAlgo = CC_MD5
            digestLength = CC_MD5_DIGEST_LENGTH
        case "SHA-1":
            hashAlgo = CC_SHA1
            digestLength = CC_SHA1_DIGEST_LENGTH
        case "SHA-224":
            hashAlgo = CC_SHA224
            digestLength = CC_SHA224_DIGEST_LENGTH
        case "SHA-256":
            hashAlgo = CC_SHA256
            digestLength = CC_SHA256_DIGEST_LENGTH
        case "SHA-384":
            hashAlgo = CC_SHA384
            digestLength = CC_SHA384_DIGEST_LENGTH
        case "SHA-512":
            hashAlgo = CC_SHA512
            digestLength = CC_SHA512_DIGEST_LENGTH
        default:
            reject("ERR", "Unknown algorithm '\(algorithm)'.", nil)
            return
        }

        var digest = [UInt8](repeating: 0, count: Int(digestLength));
        _ = hashAlgo(data.bytes, CC_LONG(data.length), &digest)
        resolve(digest.map { String(format: "%02x", $0) }.joined())
    }

    @objc(isDir:withResolver:withRejecter:)
    func isDir(path: String, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) -> Void {
        let status = checkIfIsDirectory(path: path)
        resolve(status.exists && status.isDirectory)
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
            try? FileManager.default.removeItem(atPath: target)
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

    @objc(stat:withResolver:withRejecter:)
    func stat(path: String, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) -> Void {
        do {
            let pathUrl = URL(fileURLWithPath: path)
            let attrs = try FileManager.default.attributesOfItem(atPath: path)
            resolve([
                "filename": pathUrl.lastPathComponent,
                "lastModified": 1000 * (attrs[.modificationDate] as! NSDate).timeIntervalSince1970,
                "path": pathUrl.path,
                "size": attrs[.size],
                "type": checkIfIsDirectory(path: path).isDirectory ? "directory" : "file"
            ])
        } catch {
            reject("ERR", "Failed to stat '\(path)'.", error)
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

    private func checkIfIsDirectory(path: String) -> (exists: Bool, isDirectory: Bool) {
        var isDir: ObjCBool = false
        let exists = FileManager.default.fileExists(atPath: path, isDirectory: &isDir)
        let isDirectory = isDir.boolValue
        return (exists, isDirectory)
    }
}
