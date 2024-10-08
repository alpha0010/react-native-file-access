require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))
folly_compiler_flags = '-DFOLLY_NO_CONFIG -DFOLLY_MOBILE=1 -DFOLLY_USE_LIBCPP=1 -Wno-comma -Wno-shorten-64-to-32'

Pod::Spec.new do |s|
  s.name         = "ReactNativeFileAccess"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.homepage     = package["homepage"]
  s.license      = package["license"]
  s.authors      = package["author"]

  s.platforms    = { :ios => "12.0", :osx => "10.11" }
  s.source       = { :git => "https://github.com/alpha0010/react-native-file-access.git", :tag => "#{s.version}" }

  s.source_files = "ios/**/*.{h,m,mm,swift}"

  s.dependency "React-Core"
  s.dependency "ZIPFoundation"

  if defined?($RNFANoPrivacyAPI)
    Pod::UI.puts "#{s.name}: Removing privacy sensitive API calls"
    s.pod_target_xcconfig = {
      "OTHER_SWIFT_FLAGS" => "-DNO_PRIVACY_API"
    }
  end

  # Don't install the dependencies when we run `pod install` in the old architecture.
  if ENV['RCT_NEW_ARCH_ENABLED'] == '1' then
    install_modules_dependencies(s)
  end
end
