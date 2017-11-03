package com.founder.niuzhendong.cesiumgps;


import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.MapView;
import com.baidu.recorder.api.LiveConfig;
import com.baidu.recorder.api.LiveSession;
import com.baidu.recorder.api.LiveSessionHW;
import com.baidu.recorder.api.LiveSessionSW;
import com.founder.niuzhendong.cesiumgps.gps.GpsClient;
import com.founder.niuzhendong.cesiumgps.rabbit.RabbitClient;

import static android.R.attr.orientation;
import static android.content.ContentValues.TAG;

public class SettingsActivity extends Activity {
    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */

    public GpsClient gpsClient = null;
    public Button bt_location = null;
    public Button bt_rtmp = null;
    public MapView mMapView = null;
    private LiveSession mLiveSession = null;
    private String url = "rtmp://120.24.225.21/live/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.layout);
        bt_location=(Button) findViewById(R.id.bt_location);
        bt_rtmp=(Button) findViewById(R.id.bt_rtmp);
        mMapView = (MapView) findViewById(R.id.bmapView);
        TelephonyManager tm = (TelephonyManager) getSystemService(getApplicationContext().TELEPHONY_SERVICE);
        url = url+tm.getDeviceId().toString();
        LiveConfig liveConfig = new LiveConfig.Builder()
                .setCameraId(LiveConfig.CAMERA_FACING_FRONT) // 选择摄像头为前置摄像头
                .setCameraOrientation(LiveConfig.CAMERA_FACING_FRONT) // 设置摄像头为竖向
                .setVideoWidth(240) // 设置推流视频宽度, 需传入长的一边
                .setVideoHeight(320) // 设置推流视频高度，需传入短的一边
                .setVideoFPS(8) // 设置视频帧率
                .setInitVideoBitrate(128) // 设置视频码率，单位为bit per seconds
                .setAudioBitrate(128) // 设置音频码率，单位为bit per seconds
                .setAudioSampleRate(LiveConfig.AUDIO_SAMPLE_RATE_44100) // 设置音频采样率
                .setGopLengthInSeconds(20) // 设置I帧间隔，单位为秒
                .setQosEnabled(true) // 开启码率自适应，默认为true，即默认开启
                .setMinVideoBitrate(128) // 码率自适应，最低码率
                .setMaxVideoBitrate(256) // 码率自适应，最高码率
                .setQosSensitivity(5) // 码率自适应，调整的灵敏度，单位为秒，可接受[5, 10]之间的整数值
                .build();
        Log.d(TAG, "Calling initRTMPSession..." + liveConfig.toString());
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
        //    mLiveSession = new LiveSessionHW(this, liveConfig);
        //} else {
            mLiveSession = new LiveSessionSW(this, liveConfig);
        //}
        mLiveSession.prepareSessionAsync();
        gpsClient = new GpsClient(getApplicationContext(),mMapView,tm);
        //Button bt_location=(Button) findViewById(R.id.bt_location);
        SurfaceView cameraView = (SurfaceView) findViewById(R.id.cameraView);
        mLiveSession.bindPreviewDisplay(cameraView.getHolder());

        bt_location.setOnClickListener(new OnClickListener(){
            // 点击事件触发
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                gpsClient.onStart();
                //rabbitClient.doPost("ceshi");
            }
        });
        bt_rtmp.setOnClickListener(new OnClickListener(){
            // 点击事件触发
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                mLiveSession.startRtmpSession(url);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gpsClient.onDestroy();
        mMapView.onDestroy();
    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }
}
