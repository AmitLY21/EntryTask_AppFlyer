package com.aldev.helloworld;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.appsflyer.AFInAppEventParameterName;
import com.appsflyer.AFInAppEventType;
import com.appsflyer.AppsFlyerLib;
import com.appsflyer.attribution.AppsFlyerRequestListener;
import com.appsflyer.AppsFlyerConversionListener;
import com.appsflyer.deeplink.DeepLink;
import com.appsflyer.deeplink.DeepLinkListener;
import com.appsflyer.deeplink.DeepLinkResult;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private final String DEV_KEY = "2VWAg9aTu8bLeA78E9GfHZ";
    private final String LOG_TAG = "appFlyerListener";
    private Map<String, Object> conversionData = null;
    private AppsFlyerLib appsflyer = AppsFlyerLib.getInstance();
    private ImageButton btnContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initAppsFlyerInstances();
        inAppPurchaseEvent();

        btnContact = findViewById(R.id.contact_BTN);
        btnContact.setOnClickListener(view -> {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:0545455692"));
            startActivity(callIntent);
        });

    }

    private void myDeepLink() {
        appsflyer.subscribeForDeepLink(new DeepLinkListener(){
            @Override
            public void onDeepLinking(@NonNull DeepLinkResult deepLinkResult) {
                DeepLinkResult.Status dlStatus = deepLinkResult.getStatus();
                if (dlStatus == DeepLinkResult.Status.FOUND) {
                    Log.d(LOG_TAG, "Deep link found");
                } else if (dlStatus == DeepLinkResult.Status.NOT_FOUND) {
                    Log.d(LOG_TAG, "Deep link not found");
                    return;
                } else {
                    // dlStatus == DeepLinkResult.Status.ERROR
                    DeepLinkResult.Error dlError = deepLinkResult.getError();
                    Log.d(LOG_TAG, "There was an error getting Deep Link data: " + dlError.toString());
                    return;
                }
                DeepLink deepLinkObj = deepLinkResult.getDeepLink();
                try {
                    Log.d(LOG_TAG, "The DeepLink data is: " + deepLinkObj.toString());
                } catch (Exception e) {
                    Log.d(LOG_TAG, "DeepLink data came back null");
                    return;
                }
                // An example for using is_deferred
                if (deepLinkObj.isDeferred()) {
                    Log.d(LOG_TAG, "This is a deferred deep link");
                } else {
                    Log.d(LOG_TAG, "This is a direct deep link");
                }
            }
        });
    }

    private AppsFlyerConversionListener myConversion() {
        AppsFlyerConversionListener conversionListener =  new AppsFlyerConversionListener() {
            @Override
            public void onConversionDataSuccess(Map<String, Object> conversionDataMap) {
                for (String attrName : conversionDataMap.keySet())
                    Log.d(LOG_TAG, "Conversion attribute: " + attrName + " = " + conversionDataMap.get(attrName));
                String status = Objects.requireNonNull(conversionDataMap.get("af_status")).toString();
                if(status.equals("Non-organic")){
                    if( Objects.requireNonNull(conversionDataMap.get("is_first_launch")).toString().equals("true")){
                        Log.d(LOG_TAG,"Conversion: First Launch");
                    } else {
                        Log.d(LOG_TAG,"Conversion: Not First Launch");
                    }
                } else {
                    Log.d(LOG_TAG, "Conversion: This is an organic install.");
                }
                conversionData = conversionDataMap;
            }

            @Override
            public void onConversionDataFail(String errorMessage) {
                Log.d(LOG_TAG, "error getting conversion data: " + errorMessage);
            }

            @Override
            public void onAppOpenAttribution(Map<String, String> attributionData) {
                Log.d(LOG_TAG, "onAppOpenAttribution: This is fake call.");
            }

            @Override
            public void onAttributionFailure(String errorMessage) {
                Log.d(LOG_TAG, "error onAttributionFailure : " + errorMessage);
            }
        };
        return conversionListener;
    }

    private void inAppPurchaseEvent() {
        //Default currency is USD so there is no need to put a currency attribute in eventValues
        Map<String, Object> eventValues = new HashMap<String, Object>();
        eventValues.put(AFInAppEventParameterName.CONTENT_ID,1);
        eventValues.put(AFInAppEventParameterName.CONTENT_TYPE, "Ad Promotion");
        eventValues.put(AFInAppEventParameterName.REVENUE, 200);

        AppsFlyerLib.getInstance().logEvent(getApplicationContext(),
                AFInAppEventType.PURCHASE, eventValues,
                new AppsFlyerRequestListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(LOG_TAG, "Event sent successfully");
                    }
                    @Override
                    public void onError(int i, @NonNull String s) {
                        Log.d(LOG_TAG, "Event failed to be sent:\n" +
                                "Error code: " + i + "\n"
                                + "Error description: " + s);
                    }
                });


    }

    private void initAppsFlyerInstances() {
        myDeepLink();
        AppsFlyerLib.getInstance().setDebugLog(true);
        AppsFlyerLib.getInstance().init(DEV_KEY, myConversion(), this);
        AppsFlyerLib.getInstance().start(getApplicationContext(), DEV_KEY, new AppsFlyerRequestListener() {
            /**
             * if we get a response from 200-299 we go to onSuccess
             * else we goto onError
             */
            @Override
            public void onSuccess() {
                Log.d(LOG_TAG, "Launch sent successfully, got 200 response code from server");
            }

            @Override
            public void onError(int i, @NonNull String s) {
                Log.d(LOG_TAG, "Launch failed to be sent:\n" +
                        "Error code: " + i + "\n"
                        + "Error description: " + s);
            }
        });
    }
    /**
     * Log event signature ,event name -> AFInAppEventType.PURCHASE
     */
    /*
    void logEvent(Context context,
                          String eventName,
                          Map<String, Object> eventValues,
                          AppsFlyerRequestListener listener) {

   */
}