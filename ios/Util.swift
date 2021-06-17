extension String {
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
