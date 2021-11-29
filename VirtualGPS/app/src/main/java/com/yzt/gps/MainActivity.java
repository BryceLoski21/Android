package com.yzt.gps;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    TextView mockLocationText, currentLocationText;
    EditText latText, lngText;
    Button startMockButton, getNetworkLocationButton, getGpsLocatinButton, changeLocationButton;

    private boolean permissionFlag;
    private boolean clickFlag = false;
    private static Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init(); // 加载组件

        initListener(); // 给按钮监听
    }

    public void init() {
        mockLocationText = (TextView) findViewById(R.id.mockLocationText);
        startMockButton = (Button) findViewById(R.id.startMock);
//        latText = (EditText) findViewById(R.id.latitudeEdt);
//        lngText = (EditText) findViewById(R.id.longitudeEdt);
//        changeLocationButton = (Button)findViewById(R.id.changeLocation);
        getNetworkLocationButton = (Button) findViewById(R.id.getNetworkLocation);
        getGpsLocatinButton = (Button) findViewById(R.id.getGpsLocation);
        currentLocationText = (TextView) findViewById(R.id.getLocationText);
        context = MainActivity.this;
    }

    private void initListener() {
        getGpsLocatinButton.setOnClickListener(this);
        getNetworkLocationButton.setOnClickListener(this);
        startMockButton.setOnClickListener(this);
//        changeLocationButton.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initPermission();//针对6.0以上版本做权限适配
    }

    private void initPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //检查权限
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //请求权限
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                permissionFlag = true;
            }
        } else {
            permissionFlag = true;
        }
    }

    /**
     * 权限的结果回调函数
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            permissionFlag = grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.getGpsLocation:
                if (permissionFlag) {
                    getGPSLocation();
                } else {
                    Toast.makeText(this, "no permission", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.getNetworkLocation:
                if (permissionFlag) {
                    getNetworkLocation();
                } else {
                    Toast.makeText(this, "no permission", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.startMock:
                if (permissionFlag) {
                    if(clickFlag == false) {
                        mockLocation();
                        clickFlag = true;
                    }else{
                        Toast.makeText(MainActivity.this, "不要再点啦！！", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "no permission", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    /**
     * 通过GPS获取定位信息
     */
    public void getGPSLocation() {
        Location gps = LocationUtils.getGPSLocation(this);
        if (gps == null) {
            //设置定位监听，因为GPS定位，第一次进来可能获取不到，通过设置监听，可以在有效的时间范围内获取定位信息
            LocationUtils.addLocationListener(context, LocationManager.GPS_PROVIDER, location -> {
                if (location != null) {
                    Toast.makeText(MainActivity.this, "gps onSuccessLocation location:  lat==" + location.getLatitude() + "     lng==" + location.getLongitude(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "gps location is null", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            currentLocationText.setText("gpsLocation:\nlatitude:"+ gps.getLatitude() + "\nlongitude:"+ gps.getLongitude());
        }
    }

    /**
     * 通过网络等获取定位信息
     */
    private void getNetworkLocation() {
        Location net = LocationUtils.getNetWorkLocation(this);
        if (net == null) {
            Toast.makeText(this, "net location is null", Toast.LENGTH_SHORT).show();
        } else {
            currentLocationText.setText("netLocation:\nlatitude:"+ net.getLatitude() +"\nlongitude:"+ net.getLongitude());
        }
    }

    /*
     * 模拟定位
     * */
    public boolean mockLocation() {
        changeGpsLocation();
        changeNetworkLocation();
        return true;
    }

    // 修改networkLocation
    public boolean changeNetworkLocation() {
        LocationManager mLocationManager;//位置管理器
        boolean hasAddTestProvider = false;
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean canMockPosition = (Settings.Secure.getInt(getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION, 0) != 0 || Build.VERSION.SDK_INT > 22);
        if (canMockPosition && hasAddTestProvider == false)
            try {
                String providerStr = LocationManager.NETWORK_PROVIDER;
                LocationProvider provider = locationManager.getProvider(providerStr);
                if (provider != null) {
                    locationManager.addTestProvider(
                            provider.getName()
                            , provider.requiresNetwork()
                            , provider.requiresSatellite()
                            , provider.requiresCell()
                            , provider.hasMonetaryCost()
                            , provider.supportsAltitude()
                            , provider.supportsSpeed()
                            , provider.supportsBearing()
                            , provider.getPowerRequirement()
                            , provider.getAccuracy());
                } else {
                    locationManager.addTestProvider(
                            providerStr
                            , false, true, false, false, true, true, true
                            , Criteria.POWER_HIGH, Criteria.ACCURACY_FINE);
                }
                locationManager.setTestProviderEnabled(providerStr, true);
                locationManager.setTestProviderStatus(providerStr, LocationProvider.AVAILABLE, null, System.currentTimeMillis());

                // 模拟位置可用
                hasAddTestProvider = true;
                canMockPosition = true;
            }catch (Exception e){
                canMockPosition = false;
            }
        if (hasAddTestProvider == true) {
            // network_provider
            String providerStr = LocationManager.NETWORK_PROVIDER;
            Location mockLocation = new Location(providerStr);
            mockLocation.setLatitude(30.718978);  // 维度（度）
            mockLocation.setLongitude(114.517173); // 经度（度）
            mockLocation.setAltitude(30);  // 高程（米）
            mockLocation.setBearing(180);  // 方向（度）
            mockLocation.setSpeed(10);  //速度（米/秒）
            mockLocation.setAccuracy(0.1f);  // 精度（米）
            mockLocation.setTime(10);  // 本地时间
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                mockLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
            }
            locationManager.setTestProviderLocation(providerStr, mockLocation);
        } else {
            System.out.println("hasAddTestProvider" + hasAddTestProvider);
        }
        LocationManager locMgr = (LocationManager)
                getSystemService(LOCATION_SERVICE);
        LocationListener lis = new LocationListener() {
            public void onLocationChanged(Location location) {
                //You will get the mock location
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };
        //获取到位置管理器实例
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //获取到GPS_PROVIDER
        //高版本的权限检查
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        final Location location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        //侦听位置发生变化，2000毫秒更新一次，位置超过8米也更新一次
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 8, new LocationListener() {

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProviderEnabled(String provider) {
                // 当GPS Location Provider可用时，更新位置
                //高版本的权限检查
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mockLocationText.setText(updata(mLocationManager.getLastKnownLocation(provider)));
            }
            @Override
            public void onProviderDisabled(String provider) {
                // TODO Auto-generated method stub
            }
            @Override
            public void onLocationChanged(Location location) {
                // 当GPS定位信息发生改变时，更新位置
                String temp = updata(location);
                //postinfotoweb(temp);
                mockLocationText.setText(temp);
            }
        });
        //获取到NETWORK_PROVIDER
        //高版本的权限检查
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        final Location netLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        //侦听位置发生变化，2000毫秒更新一次，位置超过8米也更新一次
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 8, new LocationListener() {

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProviderEnabled(String provider) {
                // 当GPS Location Provider可用时，更新位置
                //高版本的权限检查
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mockLocationText.setText(updata(mLocationManager.getLastKnownLocation(provider)));
            }
            @Override
            public void onProviderDisabled(String provider) {
                // TODO Auto-generated method stub
            }
            @Override
            public void onLocationChanged(Location location) {
                // 当NETWORK定位信息发生改变时，更新位置
                String temp = updata(location);
                //postinfotoweb(temp);
                mockLocationText.setText(temp);
            }
        });
        return true;
    }

    // 修改gpsLocation
    public boolean changeGpsLocation(){
        LocationManager mLocationManager;//位置管理器
        boolean hasAddTestProvider = false;
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean canMockPosition = (Settings.Secure.getInt(getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION, 0) != 0 || Build.VERSION.SDK_INT > 22);
        if (canMockPosition && hasAddTestProvider == false)
            try {
                String providerStr = LocationManager.GPS_PROVIDER;
                LocationProvider provider = locationManager.getProvider(providerStr);
                if (provider != null) {
                    locationManager.addTestProvider(
                            provider.getName()
                            , provider.requiresNetwork()
                            , provider.requiresSatellite()
                            , provider.requiresCell()
                            , provider.hasMonetaryCost()
                            , provider.supportsAltitude()
                            , provider.supportsSpeed()
                            , provider.supportsBearing()
                            , provider.getPowerRequirement()
                            , provider.getAccuracy());
                } else {
                    locationManager.addTestProvider(
                            providerStr
                            , true, true, false, false, true, true, true
                            , Criteria.POWER_HIGH, Criteria.ACCURACY_FINE);
                }
                locationManager.setTestProviderEnabled(providerStr, true);
                locationManager.setTestProviderStatus(providerStr, LocationProvider.AVAILABLE, null, System.currentTimeMillis());

                // 模拟位置可用
                hasAddTestProvider = true;
                canMockPosition = true;
            } catch (SecurityException e) {
                canMockPosition = false;
            }
        if (hasAddTestProvider == true) {
            // gps_provider
            String providerStr = LocationManager.GPS_PROVIDER;
            Location mockLocation = new Location(providerStr);
            mockLocation.setLatitude(30.718978);  // 维度（度）
            mockLocation.setLongitude(114.517173); // 经度（度）
            mockLocation.setAltitude(30);  // 高程（米）
            mockLocation.setBearing(180);  // 方向（度）
            mockLocation.setSpeed(10);  //速度（米/秒）
            mockLocation.setAccuracy(0.1f);  // 精度（米）
            mockLocation.setTime(10);  // 本地时间
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                mockLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
            }
            locationManager.setTestProviderLocation(providerStr, mockLocation);
        } else {
            System.out.println("hasAddTestProvider" + hasAddTestProvider);
        }
//        LocationManager locMgr = (LocationManager)
//                getSystemService(LOCATION_SERVICE);
//        LocationListener lis = new LocationListener() {
//            public void onLocationChanged(Location location) {
//                //You will get the mock location
//            }
//
//            @Override
//            public void onStatusChanged(String s, int i, Bundle bundle) {
//
//            }
//
//            @Override
//            public void onProviderEnabled(String s) {
//
//            }
//
//            @Override
//            public void onProviderDisabled(String s) {
//
//            }
//        };
        //获取到位置管理器实例
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //获取到GPS_PROVIDER
        //高版本的权限检查
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        final Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        //侦听位置发生变化，2000毫秒更新一次，位置超过8米也更新一次
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 8, new LocationListener() {

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProviderEnabled(String provider) {
                // 当GPS Location Provider可用时，更新位置
                //高版本的权限检查
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mockLocationText.setText(updata(mLocationManager.getLastKnownLocation(provider)));
            }
            @Override
            public void onProviderDisabled(String provider) {
                // TODO Auto-generated method stub
            }
            @Override
            public void onLocationChanged(Location location) {
                // 当GPS定位信息发生改变时，更新位置
                String temp = updata(location);
                //postinfotoweb(temp);
                mockLocationText.setText(temp);
            }
        });
        //获取到NETWORK_PROVIDER
        //高版本的权限检查
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        final Location netLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        //侦听位置发生变化，2000毫秒更新一次，位置超过8米也更新一次
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 8, new LocationListener() {

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProviderEnabled(String provider) {
                // 当GPS Location Provider可用时，更新位置
                //高版本的权限检查
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mockLocationText.setText(updata(mLocationManager.getLastKnownLocation(provider)));
            }
            @Override
            public void onProviderDisabled(String provider) {
                // TODO Auto-generated method stub
            }
            @Override
            public void onLocationChanged(Location location) {
                // 当NETWORK定位信息发生改变时，更新位置
                String temp = updata(location);
                //postinfotoweb(temp);
                mockLocationText.setText(temp);
            }
        });
        return true;
    }

    // 更新文本
    private String updata(Location location){
        if(location != null){
            StringBuilder sb = new StringBuilder();
            sb.append("实时位置信息:\n");
            sb.append("纬度:");
           sb.append(location.getLatitude());
            sb.append("\n经度:");
            sb.append(location.getLongitude());
            sb.append("\n高度:");
            sb.append(location.getAltitude());
            sb.append("\n速度：");
            sb.append(location.getSpeed());
            sb.append("\n方向：");
            sb.append(location.getBearing());
            sb.append("\n当地时间：");
            sb.append(location.getTime());
            return sb.toString();
        }
        return  null;
    }
}