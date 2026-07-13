# g01-ios-game

G01 game integration — consumes **VLPlaySDK as a prebuilt xcframework** (production / binary mode),
exactly like a partner game. iOS analog of [`g01-android-game`](../g01-android-game) (which consumes
`VLPlaySDK-G01.aar`).

App sources are a copy of the canonical SwiftUI demo (`vlgame-sdk-ios/demo2`); the only difference is
the **dependency**: it links the released binary instead of compiling the SDK from source.

**SDK pinned: v1.1.7** (`G01/v1.1.7`, released 2026-07-13).

## Layout

```text
g01-ios-game/
├── Podfile                          # pod 'VLPlaySDK', :path => 'LocalPods/VLPlaySDK' + Google-Mobile-Ads-SDK
├── LocalPods/VLPlaySDK/
│   ├── VLPlaySDK.podspec             # binary podspec: vendors the xcframework + the 4 external deps
│   └── VLPlaySDK.xcframework/        # the release artifact (renamed — see "xcframework naming")
├── VLPlayDemo2/                      # SwiftUI app sources
├── VLPlayDemo2.xcodeproj
└── VLPlayDemo2.xcworkspace           # open THIS (not .xcodeproj)
```

## Build

```sh
pod install
open VLPlayDemo2.xcworkspace        # then ⌘B in Xcode
# or CLI:
xcodebuild build -workspace VLPlayDemo2.xcworkspace -scheme VLPlayDemo2 \
  -sdk iphonesimulator -destination 'generic/platform=iOS Simulator' CODE_SIGNING_ALLOWED=NO
```

Runtime creds (`VLPlayDemo2/VLPlaySDK-Info.plist` + `GoogleService-Info.plist`) are **gitignored** —
copy `VLPlaySDK-Info.plist.example` and fill, or take them from `vlgame-sdk-ios/demo2/VLPlayDemo2/`.
Same `com.vlplay.demo` game (clientId `69ec2ed7…`).

## Bumping the SDK

Take the **release artifact**, not a local build — that is what partners actually get:

```sh
gh release download G01/v1.1.7 --repo VL-Game/vlgame-sdk-main-ios
unzip VLPlaySDK-G01.xcframework.zip
rm -rf LocalPods/VLPlaySDK/VLPlaySDK.xcframework
mv VLPlaySDK-G01.xcframework LocalPods/VLPlaySDK/VLPlaySDK.xcframework   # rename is mandatory, see below
# bump s.version in LocalPods/VLPlaySDK/VLPlaySDK.podspec, then:
pod install
```

Verify the artifact before trusting it:

```sh
FW=LocalPods/VLPlaySDK/VLPlaySDK.xcframework/ios-arm64/VLPlaySDK.framework
grep kVLPlaySDKVersionString $FW/Headers/Constants.h   # must match the tag (source lied 1.0.0 until v1.1.7)
nm -a $FW/VLPlaySDK | grep -c GULAppDelegateSwizzler   # must be 0 — see "Firebase / GUL" below
ls $FW/VLPlaySDKResource.bundle                        # resources must be nested in the framework
```

## xcframework naming

`build-xcframework.sh` (and the release asset) is named `VLPlaySDK-G01.xcframework`, but the framework
**inside** it is `VLPlaySDK.framework`. CocoaPods derives the link flag from the *xcframework filename*,
so consuming it as-is yields `-framework "VLPlaySDK-G01"` → `ld: framework 'VLPlaySDK-G01' not found`.
Rename to `VLPlaySDK.xcframework` (basename == inner name) before vendoring.

## Dependency model (why the Podfile looks the way it does)

| Dependency | In the binary? | Consumer action |
| --- | --- | --- |
| AppsFlyer, FirebaseCore, GoogleSignIn, ZMJTipView, SVProgressHUD | **statically linked in** | do NOT re-declare → dup-symbol |
| FBSDK (Core/Login/Share/AEMKit/Basics) | external `@rpath` | `pod 'FBSDKCoreKit/LoginKit/ShareKit' ~> 18.0` |
| OneSignal (10 frameworks) | external `@rpath` | `pod 'OneSignalXCFramework' '5.5.1'` |
| Google-Mobile-Ads-SDK | **not linked** (soft-link via `NSClassFromString`) | opt-in by the host; needs `GADApplicationIdentifier` in Info.plist |

## 🔥 Firebase / GUL — do NOT add Firebase pods

`Firebase/Core`, `Firebase/Auth`, `Firebase/Analytics` pull `GoogleUtilities/AppDelegateSwizzler`,
which isa-swizzles the host app delegate into `GUL_AppController`. UIKit then reads `delegate.window`,
and engine templates (cocos2d-x / Unity `AppController`) keep `window` as a bare ivar with **no
accessor** → `-[GUL_AppController window]: unrecognized selector` right after
`handleApplication:didFinishLaunchingWithOptions:`. `FirebaseAppDelegateProxyEnabled=NO` does NOT fix
it (the app then dies later on `com.firebase.installations`) — that key must be **absent**.

v1.1.7 fixed this at the root: Analytics + Auth dropped, only the direct `FirebaseCore` pod kept, plus
a defensive `-window`/`setWindow:` shim synthesised on the host delegate. This project is the
regression check: `Podfile.lock` must contain **zero** `AppDelegateSwizzler`, and `nm` on the shipped
binary must show zero `GULAppDelegateSwizzler` symbols.

> ⚠️ A SwiftUI host like this one would **not crash even on a broken SDK** (its delegate does have a
> `window` property). The crash only reproduces on an engine host — use `../vlplay-unity-demo` (or a
> cocos2d-x game) for the real device-level proof.

## ~~Resource gap~~ (CLOSED since v1.1.6)

The xcframework now **nests `VLPlaySDKResource.bundle`** (nibs, pngs, Roboto, `ErrorCode.plist`,
`LocalizableString.plist`, `SVProgressHUD.bundle`) inside `VLPlaySDK.framework`, and the SDK resolves
it via `bundleForClass` (`NSBundle+Additions.m`). Older demos copied an `sdk-resources/` tree next to
the podspec and rebuilt the bundles via `resource_bundles` — that workaround is gone here.
