package com.soulstring94.onlinewalkingclub;

import android.app.Application;

import com.kakao.sdk.common.KakaoSdk;

public class GlobalApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        KakaoSdk.init(this, "23ecb1735e5072ae849e65d7431c38b5");
    }
}
