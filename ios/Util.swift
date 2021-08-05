extension String {
    /**
     * Get a null terminated string as mutable data.
     */
    func mutableCString(using encoding: String.Encoding) -> NSMutableData? {
        guard let encoded = self.data(using: encoding) else {
            return nil
        }
        let mutable = NSMutableData(data: encoded)
        mutable.increaseLength(by: 1)
        return mutable
    }

    /**
     * Normalize standard file system paths and file URIs.
     */
    func path() -> String {
        if self.contains("://"), let pathUri = URL(string: self) {
            return pathUri.path
        }
        return self
    }
}
