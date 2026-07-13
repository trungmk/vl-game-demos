package sdk.vlplay.vn.sample;

import androidx.multidex.MultiDexApplication;

import sdk.vlplay.vn.tracking.VLPlaySDKManager;
public class MainApplication extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        VLPlaySDKManager.initApplication(this, VLPlaySDKManager.ENVIRONMENT_PRODUCT);
        VLPlaySDKManager.prepareSignIn(this);
    }
}
