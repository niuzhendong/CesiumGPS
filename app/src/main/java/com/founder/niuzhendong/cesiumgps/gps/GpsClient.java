package com.founder.niuzhendong.cesiumgps.gps;

/**
 * Created by niuzh on 2017-10-16.
 */

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.location.BDNotifyListener;//假如用到位置提醒功能，需要import该类
import com.baidu.location.Poi;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.founder.niuzhendong.cesiumgps.rabbit.RabbitClient;

import net.sf.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class GpsClient {
    public LocationClient mLocationClient = null;
    public Context _context = null;
    public boolean nfalg = false;
    public MapView mMapView = null;
    TelephonyManager _tm = null;

    public GpsClient(Context context,MapView mapview,TelephonyManager tm){
        _context = context;
        mMapView = mapview;
        _tm = tm;
        mLocationClient = new LocationClient(context);     //声明LocationClient类
        mLocationClient.registerLocationListener(LocationListener);
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("WGS84");//可选，默认gcj02，设置返回的定位结果坐标系
        int span=5000;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认false，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        mLocationClient.setLocOption(option);
    }

    public BDLocationListener LocationListener = new BDLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation location) {
            if (null != location && location.getLocType() != BDLocation.TypeServerError) {
                Map<String,Object> sb = new HashMap<String,Object>();
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                sb.put("Time",df.format(new Date()));
                sb.put("LocType",location.getLocType());
                sb.put("Latitude",location.getLatitude());
                sb.put("Longitude",location.getLongitude());
                sb.put("Radius",location.getRadius());
                sb.put("CountryCode",location.getCountryCode());
                sb.put("Country",location.getCountry());
                sb.put("CityCode",location.getCityCode());
                sb.put("City",location.getCity());
                sb.put("District",location.getDistrict());
                sb.put("Street",location.getStreet());
                sb.put("AddrStr",location.getAddrStr());
                sb.put("LocationDescribe",location.getLocationDescribe());
                sb.put("Direction",location.getDirection());
                sb.put("DeviceId",_tm.getDeviceId());

                if (location.getPoiList() != null && !location.getPoiList().isEmpty()) {
                    StringBuffer pois = new StringBuffer(256);
                    for (int i = 0; i < location.getPoiList().size(); i++) {
                        Poi poi = (Poi) location.getPoiList().get(i);
                        pois.append(poi.getName() + ";");
                    }
                    sb.put("Poi",pois.toString());
                }

                JSONObject json = JSONObject.fromObject(sb);

                new RabbitClient().doPost(json.toString());

                MyLocationData locData = new MyLocationData.Builder()
                        .accuracy(location.getRadius())
                        // 此处设置开发者获取到的方向信息，顺时针0-360
                        .direction(100).latitude(location.getLatitude())
                        .longitude(location.getLongitude()).build();
                // 设置定位数据
                mMapView.getMap().setMyLocationData(locData);

                Log.i("BaiduLocationApiDem", json.toString());
                //Toast.makeText(_context, json.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    };

    public void onDestroy(){
        mLocationClient.unRegisterLocationListener(LocationListener);
    }

    public void onStart(){
        if (!nfalg) {
            mLocationClient.start();// 定位SDK
            // start之后会默认发起一次定位请求，开发者无须判断isstart并主动调用request
            mMapView.getMap().setMyLocationEnabled(true);
            nfalg = true;
        } else {
            mLocationClient.stop();
            mMapView.getMap().setMyLocationEnabled(false);
            nfalg = false;
        }
    }
}
