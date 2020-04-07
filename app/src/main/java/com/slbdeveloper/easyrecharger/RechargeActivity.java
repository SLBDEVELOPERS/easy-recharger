package com.slbdeveloper.easyrecharger;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

public class RechargeActivity extends AppCompatActivity {

    TextView number_text_view , network_text_view;

    LinearLayout retry_layout , dial_now_layout;

    String pin;
    String carierName;

    private AdView adView;
    private InterstitialAd interstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alert_dialog);

        MobileAds.initialize(this, getResources().getString(R.string.admob_app_id));
        loadBannerAd();
        loadInterstitialAd();
        number_text_view = findViewById(R.id.number_text_view);
        network_text_view = findViewById(R.id.network_text_view);
        retry_layout = findViewById(R.id.retry_layout);
        dial_now_layout = findViewById(R.id.dial_now_layout);

        pin = getIntent().getExtras().getString("pin");
        carierName = getIntent().getExtras().getString("carier");

        number_text_view.setText(pin);
        network_text_view.setText(carierName);

        retry_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(RechargeActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });

        dial_now_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               String topupnumber =  getRechargeNumber(pin,carierName);
                showInterstitialAd();
                Intent intent = new Intent("android.intent.action.DIAL");
                intent.setData(Uri.parse("tel:" + topupnumber));
                startActivity(intent);
            }
        });

    }

    public void loadBannerAd() {

        adView = findViewById(R.id.adbannerView);

        AdRequest request = new AdRequest.Builder()
                .build();
        adView.loadAd(request);
        adView.setAdListener(new AdListener() {

            @Override
            public void onAdClosed() {
            }

            @Override
            public void onAdFailedToLoad(int error) {
                adView.setVisibility(View.GONE);
            }

            @Override
            public void onAdLeftApplication() {
            }

            @Override
            public void onAdOpened() {
            }

            @Override
            public void onAdLoaded() {
                adView.setVisibility(View.VISIBLE);
            }
        });

    }


    private void loadInterstitialAd() {

        interstitialAd = new InterstitialAd(getApplicationContext());
        interstitialAd.setAdUnitId(getResources().getString(R.string.admob_interstitial_unit_id));
        AdRequest request = new AdRequest.Builder()
                .addTestDevice("C2F3BAD0F1018CFCBFFE85DBC8DD7C43")
                .build();
        interstitialAd.loadAd(request);

        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                interstitialAd.loadAd(new AdRequest.Builder().build());
            }
        });

    }

    private void showInterstitialAd() {

        if (interstitialAd != null && interstitialAd.isLoaded()) {
            interstitialAd.show();
        }

    }

    public static String getRechargeNumber(String cardNo, String operator) {
        String dialNo = "";
        try {
            String encodedHash = Uri.encode("#");
            if (operator.equals("dialog")) {
                dialNo = encodedHash + "123" + encodedHash + cardNo + encodedHash;

            }
            if (operator.equals("mobitel")) {
                dialNo = "*102*" + cardNo + Uri.encode("#");
            }
            if (operator.equals("airtel")) {
                dialNo = "*567" + encodedHash + cardNo + encodedHash;
            }
            if (operator.equals("hutch")) {
                dialNo =  "*355*" + cardNo + encodedHash;
            }
            if (operator.equals("etisalat")) {
                dialNo = "*336" + encodedHash + cardNo + encodedHash;
            }
            return dialNo;
        } catch (Exception ex) {
            ex.printStackTrace();
            return dialNo;
        }
    }

}
