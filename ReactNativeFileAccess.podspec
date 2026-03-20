require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = "ReactNativeFileAccess"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.homepage     = package["homepage"]
  s.license      = package["license"]
  s.authors      = package["author"]

  s.platforms    = { :ios => min_ios_version_supported, :osx => "10.11" }
  s.source       = { :git => "https://github.com/alpha0010/react-native-file-access.git", :tag => "#{s.version}" }

  s.source_files = "ios/**/*.{h,m,mm,swift,cpp}"
  s.public_header_files = "ios/ReactNativeFileAccess.h"

  s.dependency "ZIPFoundation"

  if defined?($RNFANoPrivacyAPI)
    Pod::UI.puts "#{s.name}: Removing privacy sensitive API calls"
    s.pod_target_xcconfig = {
      "OTHER_SWIFT_FLAGS" => "-DNO_PRIVACY_API"
    }
  end

  install_modules_dependencies(s)
end
