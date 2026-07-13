//
//  VLPlayBridge.h
//  VLPlaySDK
//
//  WKWebView <-> Native JS Bridge via WKScriptMessageHandler.
//  Used by the payment WebView (WebPay/AppotaPay) host. Auth is fully native —
//  see NATIVE-AUTH-MIGRATION.
//
//  Handler name: "VLGameSDK" (literal — Web SDK substring-matches this to detect iOS platform).
//
//  Web SDK -> Native calls:
//    window.webkit.messageHandlers.VLGameSDK.postMessage({ action: "getUserToken" })
//    window.webkit.messageHandlers.VLGameSDK.postMessage({ action: "getUserInfo" })
//    window.webkit.messageHandlers.VLGameSDK.postMessage({ action: "closeWebView" })
//    window.webkit.messageHandlers.VLGameSDK.postMessage({ action: "openExternalApp", deeplink: "momo://..." })
//    window.webkit.messageHandlers.VLGameSDK.postMessage({ action: "onPaymentSuccess", data: "{...json...}" })
//    window.webkit.messageHandlers.VLGameSDK.postMessage({ action: "sendEvent", eventName: "...", params: "{...}" })
//    window.webkit.messageHandlers.VLGameSDK.postMessage({ action: "requestNativePayment", productId: "...", productName: "...", amount: 0, currency: "..." })
//
//  Native -> Web callbacks (evaluated on the WebView).
//  Objects are passed as JSON strings (JSON.stringify({...})) per bridge-interface.md contract:
//    window.VLGameSDK.onTokenReceived("access-token")
//    window.VLGameSDK.onUserInfo(JSON.stringify({ uid, username, email, accessToken }))
//    window.VLGameSDK.onPaymentReturn(JSON.stringify({ transactionId, productId, status, error? }))
//

#import <Foundation/Foundation.h>
#import <WebKit/WebKit.h>

NS_ASSUME_NONNULL_BEGIN

/// Notification posted when a native IAP transaction finishes (success or failure).
/// userInfo keys: transactionId, productId, status ("success"|"failed"), error (optional).
extern NSString *const VLPlaySDKPaymentDidFinishNotification;

@interface VLPlayBridge : NSObject <WKScriptMessageHandler>

/// Register the bridge on a WKWebView's user content controller.
/// Call this before loading any URL in the WebView.
+ (void)registerBridgeOnWebView:(WKWebView *)webView;

/// Send token to WebView after it requests getUserToken.
+ (void)sendTokenToWebView:(WKWebView *)webView token:(NSString *)token;

/// Notify WebView of payment return status.
+ (void)notifyPaymentReturn:(WKWebView *)webView status:(NSString *)status;

@end

NS_ASSUME_NONNULL_END
