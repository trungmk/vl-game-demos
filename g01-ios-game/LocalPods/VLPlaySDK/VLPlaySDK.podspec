Pod::Spec.new do |s|
  s.name         = 'VLPlaySDK'
  s.version      = '1.3.2'
  s.summary      = 'VLPlaySDK G01 — prebuilt xcframework consumed as a binary (g01-ios-game demo).'
  s.description  = <<-DESC
    Binary-distribution podspec for the G01 variant of VLPlaySDK. Vendors the
    prebuilt xcframework instead of compiling from source — the iOS analog of how
    g01-android-game consumes VLPlaySDK-G01.aar.

    The binary here is the RELEASE ARTIFACT, byte-for-byte what a partner gets:
      gh release download G01/v1.1.7 --repo VL-Game/vlgame-sdk-main-ios
      unzip VLPlaySDK-G01.xcframework.zip
      # rename: basename MUST match the inner framework name, else CocoaPods
      # emits `-framework VLPlaySDK-G01` -> "framework not found".
      mv VLPlaySDK-G01.xcframework LocalPods/VLPlaySDK/VLPlaySDK.xcframework
  DESC
  s.homepage     = 'https://sdk.vlplay.vn'
  s.license      = { :type => 'Proprietary', :text => 'Copyright (c) 2016-2026 VL Play. All rights reserved.' }
  s.author       = { 'VL Play' => 'dev@vlplay.vn' }
  # Ignored for :path pods; kept for documentation / spec validation.
  s.source       = { :git => 'https://github.com/VL-Game/vlgame-sdk-main-ios.git', :tag => "G01/v#{s.version}" }

  s.ios.deployment_target = '15.0'
  s.swift_versions        = ['5.0']

  # AppsFlyer / FirebaseCore / GoogleSignIn / ZMJTipView / SVProgressHUD are
  # ALREADY statically linked into this binary. FBSDK + OneSignal stay external
  # dynamic frameworks (@rpath) — see s.dependency below.
  #
  # Since v1.1.6 the xcframework NESTS VLPlaySDKResource.bundle (nibs, pngs,
  # Roboto, ErrorCode/LocalizableString, SVProgressHUD.bundle) inside
  # VLPlaySDK.framework, and the SDK resolves it via `bundleForClass`
  # (NSBundle+Additions.m). No side-car `resource_bundles` needed anymore —
  # older demos copied an `sdk-resources/` tree in to work around that gap.
  s.vendored_frameworks = 'VLPlaySDK.xcframework'

  s.frameworks = [
    'UIKit', 'Foundation', 'StoreKit', 'WebKit',
    'UserNotifications', 'SafariServices', 'SystemConfiguration',
    'CoreData', 'AddressBook', 'Security', 'AdSupport',
    'AuthenticationServices', 'CoreTelephony',
  ]

  # ONLY the transitive deps NOT baked into the binary. Re-declaring the
  # statically-embedded ones would produce duplicate symbols at link.
  #
  # Do NOT add Firebase/Core|Auth|Analytics here or in the app Podfile: they
  # re-pull GoogleUtilities/AppDelegateSwizzler, whose isa-swizzled
  # GUL_AppController kills engine hosts (cocos2d-x / Unity) on launch with
  # `-[GUL_AppController window]: unrecognized selector`. v1.1.7 dropped them.
  s.dependency 'FBSDKCoreKit',         '~> 18.0'
  s.dependency 'FBSDKLoginKit',        '~> 18.0'
  s.dependency 'FBSDKShareKit',        '~> 18.0'
  s.dependency 'OneSignalXCFramework', '5.5.1'
end
