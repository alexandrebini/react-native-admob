package com.sbugert.rnadmob;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.facebook.react.views.view.ReactViewGroup;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.formats.NativeAppInstallAd;
import com.google.android.gms.ads.formats.NativeContentAd;
import com.google.android.gms.ads.formats.NativeAdOptions;
import java.util.Map;

public class RNAdMobNativeAdvancedViewManager extends SimpleViewManager<ReactViewGroup> {

    public static final String REACT_CLASS = "RNAdMobNativeAdvanced";

    public static final String PROP_AD_UNIT_ID = "adUnitID";
    public static final String PROP_TEST_DEVICE_ID = "testDeviceID";

    private String testDeviceID = null;
    private String adUnitID;


    public enum Events {
        EVENT_RECEIVE_AD("onAdViewDidReceiveAd"),
        EVENT_ERROR("onDidFailToReceiveAdWithError"),
        EVENT_WILL_PRESENT("onAdViewWillPresentScreen"),
        EVENT_WILL_DISMISS("onAdViewWillDismissScreen"),
        EVENT_DID_DISMISS("onAdViewDidDismissScreen"),
        EVENT_WILL_LEAVE_APP("onAdViewWillLeaveApplication"),
        EVENT_APP_INSTALL_LOADED("onAppInstallAdLoaded");

        private final String mName;

        Events(final String name) {
          mName = name;
        }

        @Override
        public String toString() {
          return mName;
        }
    }

    private ThemedReactContext mThemedReactContext;
    private RCTEventEmitter mEventEmitter;
    private ReactViewGroup mView;

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    protected ReactViewGroup createViewInstance(ThemedReactContext themedReactContext) {
        mThemedReactContext = themedReactContext;
        mEventEmitter = themedReactContext.getJSModule(RCTEventEmitter.class);
        mView = new ReactViewGroup(themedReactContext);
//        attachNewAdView(mView);
        return mView;
    }

//    protected void attachNewAdView(final ReactViewGroup view) {
//        final NativeExpressAdView adView = new NativeExpressAdView(mThemedReactContext);
//
//        // destroy old AdView if present
//        NativeExpressAdView oldAdView = (NativeExpressAdView) mView.getChildAt(0);
//        mView.removeAllViews();
//        if (oldAdView != null) oldAdView.destroy();
//        view.addView(adView);
//    }


    @Override
    @Nullable
    public Map<String, Object> getExportedCustomDirectEventTypeConstants() {
        MapBuilder.Builder<String, Object> builder = MapBuilder.builder();
        for (Events event : Events.values()) {
            builder.put(event.toString(), MapBuilder.of("registrationName", event.toString()));
        }
        return builder.build();
    }

    @ReactProp(name = PROP_AD_UNIT_ID)
    public void setAdUnitID(final ReactViewGroup view, final String adUnitID) {
        this.adUnitID = adUnitID;
        loadAd();
    }

    @ReactProp(name = PROP_TEST_DEVICE_ID)
    public void setPropTestDeviceID(final ReactViewGroup view, final String testDeviceID) {
        this.testDeviceID = testDeviceID;
    }

    private AdRequest buildAdRequest() {
        AdRequest.Builder adRequestBuilder = new AdRequest.Builder();
        if (testDeviceID != null){
            if (testDeviceID.equals("EMULATOR")) {
                adRequestBuilder = adRequestBuilder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
            } else {
                adRequestBuilder = adRequestBuilder.addTestDevice(testDeviceID);
            }
        }
        return adRequestBuilder.build();
    }

    private NativeAppInstallAd.OnAppInstallAdLoadedListener onAppInstallAdLoadedListener() {
        return new NativeAppInstallAd.OnAppInstallAdLoadedListener() {
            @Override
            public void onAppInstallAdLoaded(NativeAppInstallAd appInstallAd) {
                // Show the app install ad.
                mEventEmitter.receiveEvent(mView.getId(), Events.EVENT_APP_INSTALL_LOADED.toString(), null);
            }
        };
    }

    private NativeContentAd.OnContentAdLoadedListener onContentAdLoadedListener() {
        return new NativeContentAd.OnContentAdLoadedListener() {
            @Override
            public void onContentAdLoaded(NativeContentAd contentAd) {
                // Show the content ad.
            }
        };
    }

    private NativeAdOptions buildNativeOptions() {
        return new NativeAdOptions.Builder()
            .setReturnUrlsForImageAssets(true)
            .build();
    }

    private AdLoader buildAdLoader() {
        return new AdLoader.Builder(mThemedReactContext, this.adUnitID)
            .forAppInstallAd(this.onAppInstallAdLoadedListener())
            .forContentAd(this.onContentAdLoadedListener())
            .withAdListener(this.buildAdListener())
            .withNativeAdOptions(this.buildNativeOptions())
            .build();
    }

    private AdListener buildAdListener() {
        return new AdListener() {
            @Override
            public void onAdLoaded() {
                mEventEmitter.receiveEvent(mView.getId(), Events.EVENT_RECEIVE_AD.toString(), null);
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                WritableMap event = Arguments.createMap();
                switch (errorCode) {
                    case AdRequest.ERROR_CODE_INTERNAL_ERROR:
                        event.putString("error", "ERROR_CODE_INTERNAL_ERROR");
                        break;
                    case AdRequest.ERROR_CODE_INVALID_REQUEST:
                        event.putString("error", "ERROR_CODE_INVALID_REQUEST");
                        break;
                    case AdRequest.ERROR_CODE_NETWORK_ERROR:
                        event.putString("error", "ERROR_CODE_NETWORK_ERROR");
                        break;
                    case AdRequest.ERROR_CODE_NO_FILL:
                        event.putString("error", "ERROR_CODE_NO_FILL");
                        break;
                }
                mEventEmitter.receiveEvent(mView.getId(), Events.EVENT_ERROR.toString(), event);
            }

            @Override
            public void onAdOpened() {
                mEventEmitter.receiveEvent(mView.getId(), Events.EVENT_WILL_PRESENT.toString(), null);
            }

            @Override
            public void onAdClosed() {
                mEventEmitter.receiveEvent(mView.getId(), Events.EVENT_WILL_DISMISS.toString(), null);
            }

            @Override
            public void onAdLeftApplication() {
                mEventEmitter.receiveEvent(mView.getId(), Events.EVENT_WILL_LEAVE_APP.toString(), null);
            }
        };
    }


    private void loadAd() {
        if (this.adUnitID != null) {
            AdRequest adRequest = this.buildAdRequest();
            AdLoader adLoader = this.buildAdLoader();
            adLoader.loadAd(adRequest);
        }
    }
}
