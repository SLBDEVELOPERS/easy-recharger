package com.slbdeveloper.easyrecharger;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback{

    private SurfaceView mCameraView;
    private TextView mTextView;
    private CameraSource mCameraSource;
    private Camera camera = null;
    private SurfaceHolder surfaceHolder;
    private Camera.AutoFocusCallback  autoFocusCallback;
    private static final String TAG = "MainActivity";
    private static final int requestPermissionID = 101;
    private Button  flashButton;
    boolean isFlashOn = false;
    private Camera.Parameters param;
    private Pattern airtel;
    private Matcher airtelMatcher;
    private Pattern dialog;
    private Matcher dialogMatcher;
    private Pattern etisalat;
    private Matcher etisalatMatcher;
    private Pattern mobitel;
    private Matcher mobitelMatcher;
    private Pattern mobitelTwo;
    private Matcher mobitelTwoMatcher;
    private Pattern hutch;
    private Matcher hutchMatcher;
    private Handler mAutoFocusHandler;
    public boolean isHutch;
    private static final int FOCUS_AREA_SIZE = 300;

    private AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(this, getResources().getString(R.string.admob_app_id));

        mCameraView = findViewById(R.id.surfaceView);
        flashButton = (Button) findViewById(R.id.flash_button);
        surfaceHolder = mCameraView.getHolder();
        surfaceHolder.addCallback(this);
        mTextView = findViewById(R.id.mTextView);

        mAutoFocusHandler = new Handler();
        autoFocusCallback = new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean b, Camera camera) {
                mAutoFocusHandler.postDelayed((Runnable) autoFocusCallback, 1000);
            }
        };

        flashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                flashOnButton();

            }
        });

        mCameraView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                cameraFocus(event, mCameraSource, Camera.Parameters.FOCUS_MODE_AUTO);
                return false;
            }
        });

        AdRequest adRequestBanner = new AdRequest.Builder()
                .build();
        adView = (AdView)this.findViewById(R.id.adbannerView);
        adView.loadAd(adRequestBanner);

        startCameraSource();

    }

    private void flashOnButton() {
        camera=getCamera(mCameraSource);
        if (camera != null) {
            try {
                param = camera.getParameters();
                param.setFlashMode(!isFlashOn?Camera.Parameters.FLASH_MODE_TORCH :Camera.Parameters.FLASH_MODE_OFF);
                camera.setParameters(param);
                isFlashOn = !isFlashOn;
                if(isFlashOn){
                    //showToast("Flash Switched ON");
                }
                else {
                    //showToast("Flash Switched Off");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private static Camera getCamera(@NonNull CameraSource cameraSource) {
        Field[] declaredFields = CameraSource.class.getDeclaredFields();

        for (Field field : declaredFields) {
            if (field.getType() == Camera.class) {
                field.setAccessible(true);
                try {
                    Camera camera = (Camera) field.get(cameraSource);
                    return camera;
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
        return null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != requestPermissionID) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mCameraSource.start(mCameraView.getHolder());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void startCameraSource() {

        //Create the TextRecognizer
        final TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();

        if (!textRecognizer.isOperational()) {
            Log.w(TAG, "Detector dependencies not loaded yet");
        } else {

            //Initialize camerasource to use high resolution and set Autofocus on.
            mCameraSource = new CameraSource.Builder(getApplicationContext(), textRecognizer)
                    .setRequestedPreviewSize(1280, 1024)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedFps(30.0f)
                    .setAutoFocusEnabled(true)
                    .build();


            //Set the TextRecognizer's Processor.
            textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
                @Override
                public void release() {
                }

                /**
                 * Detect all the text from camera using TextBlock and the values into a stringBuilder
                 * which will then be set to the textView.
                 * */
                @Override
                public void receiveDetections(Detector.Detections<TextBlock> detections) {
                    final SparseArray<TextBlock> items = detections.getDetectedItems();
                    if (items.size() != 0 ){
                        mTextView.post(new Runnable() {
                            @SuppressLint("WrongConstant")
                            @Override
                            public void run() {
                                for(int i=0;i<items.size();i++){
                                    TextBlock item = items.valueAt(i);

                                    if (!(item == null || item.getValue() == null)) {
                                        if (item.getValue().toString().contains("*355*") || item.getValue().toString().contains("Lan") || item.getValue().toString().contains("Pv") || item.getValue().toString().contains("Lt") || item.getValue().toString().contains("Hut") || item.getValue().toString().contains("Tel") || item.getValue().toString().contains("son")) {
                                            System.out.println("HELOOOOO HUTCH");
                                            isHutch = true;
                                        }

                                        dialog = Pattern.compile(".*([0-9]{4} [0-9]{4} [0-9]{4}).*", 32);
                                        hutch = Pattern.compile(".*([0-9]{5} [0-9]{5} [0-9]{5}).*", 32);
                                        mobitel = Pattern.compile(".*([0-9]{4} [0-9]{5} [0-9]{5}).*", 32);
                                        airtel = Pattern.compile(".*([0-9]{4} [0-9]{4} [0-9]{4} [0-9]{4}).*", 32);
                                        mobitelTwo = Pattern.compile(".*([0-9]{14}).*", 32);
                                        etisalat = Pattern.compile(".*([0-9]{3} [0-9]{7} [0-9]{4}).*", 32);
                                        dialogMatcher = dialog.matcher(item.getValue().replaceAll("!", ""));
                                        hutchMatcher = hutch.matcher(item.getValue().replaceAll("!", ""));
                                        mobitelMatcher = mobitel.matcher(item.getValue().replaceAll("!", ""));
                                        airtelMatcher = airtel.matcher(item.getValue().replaceAll("!", ""));
                                        etisalatMatcher = etisalat.matcher(item.getValue().replaceAll("!", ""));
                                        mobitelTwoMatcher = mobitelTwo.matcher(item.getValue().replaceAll("!", ""));
                                        String replaceAll = item.getValue().replaceAll("!", "");

                                        if (dialogMatcher.matches()) {
                                            textRecognizer.release();
                                            // mTextView.setText(dialogMatcher.group(1));
                                            getCardNumber(dialogMatcher.group(1), "dialog");
                                        } else if (hutchMatcher.matches()) {
                                            getCardNumber(hutchMatcher.group(1), "hutch");
                                        } else if (mobitelMatcher.matches()) {
                                            getCardNumber(mobitelMatcher.group(1), "mobitel");
                                        } else if (airtelMatcher.matches()) {
                                            getCardNumber(airtelMatcher.group(1), "airtel");
                                        } else if (etisalatMatcher.matches()) {
                                            getCardNumber(etisalatMatcher.group(1), "etisalat");
                                        } else if (mobitelTwoMatcher.matches()) {
                                            getCardNumber(mobitelTwoMatcher.group(1), "mobitel");
                                        }
                                    }

                                }
                            }
                        });
                    }
                }
            });
        }
    }

    private void getCardNumber(String number , String carier){

        String pin = number;
        String carierName = carier;
        Intent intent = new Intent(MainActivity.this,RechargeActivity.class);
        intent.putExtra("pin",pin);
        intent.putExtra("carier",carierName);
        startActivity(intent);
        finish();

    }


    private boolean cameraFocus(MotionEvent event, @NonNull CameraSource cameraSource, @NonNull String focusMode) {
        Field[] declaredFields = CameraSource.class.getDeclaredFields();

        Rect rect = calculateFocusArea(event.getX(), event.getY());

        ArrayList<Camera.Area> focusAreas = new ArrayList<Camera.Area>();
        focusAreas.add(new Camera.Area(rect, 800));

        for (Field field : declaredFields) {
            if (field.getType() == Camera.class) {
                field.setAccessible(true);
                try {
                    camera = (Camera) field.get(cameraSource);
                    if (camera != null) {
                        param = camera.getParameters();
                        param.setFocusMode(focusMode);
                        param.setFocusAreas(focusAreas);
                        camera.setParameters(param);
                        param.setFocusMode("auto");
                        // Start the autofocus operation

                        camera.autoFocus(new Camera.AutoFocusCallback() {
                            @Override
                            public void onAutoFocus(boolean b, Camera camera) {
                                // currently set to auto-focus on single touch
                            }
                        });
                        return true;
                    }

                    return false;
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

                break;
            }
        }
        return false;
    }


    public Rect calculateFocusArea(float x, float y) {
        int left = clamp(Float.valueOf(((x / ((float) this.mCameraView.getWidth())) * 2000.0f) - 1000.0f).intValue(), FOCUS_AREA_SIZE);
        int top = clamp(Float.valueOf(((y / ((float) this.mCameraView.getHeight())) * 2000.0f) - 1000.0f).intValue(), FOCUS_AREA_SIZE);
        return new Rect(left, top, left + FOCUS_AREA_SIZE, top + FOCUS_AREA_SIZE);
    }

    private int clamp(int touchCoordinateInCameraReper, int focusAreaSize) {
        if (Math.abs(touchCoordinateInCameraReper) + (focusAreaSize / 2) <= 1000) {
            return touchCoordinateInCameraReper - (focusAreaSize / 2);
        }
        if (touchCoordinateInCameraReper > 0) {
            return 1000 - (focusAreaSize / 2);
        }
        return (focusAreaSize / 2) + NotificationManagerCompat.IMPORTANCE_UNSPECIFIED;
    }



    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.CAMERA},
                    requestPermissionID);
            return;
        }

        try {
            mCameraSource.start(mCameraView.getHolder());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mCameraSource.stop();
    }

}
