package com.soulstring94.onlinewalkingclub;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.kakao.sdk.user.UserApiClient;

import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapPolyline;
import net.daum.mf.map.api.MapView;

public class MainActivity extends AppCompatActivity implements MapView.CurrentLocationEventListener, MapView.MapViewEventListener {

    ConstraintLayout layoutLogin, layoutMain;

    TextView btnGoogleLogin, txtStep;
    ImageView btnKakaoLogin;
    Button btnStart, btnStop;

    private ActivityResultLauncher<Intent> resultLauncher;

    GoogleSignInClient googleSignInClient;

    boolean googleLoginFlag = false;
    boolean kakaoLoginFlag = false;

    // KakaoMap=================================================
    private MapView mapView;
    private ViewGroup mapViewContainer;

    MapPolyline mapPolyline;

    //=================================================
    Toolbar toolbar;
    DrawerLayout drawerLayout;
    NavigationView navigationView;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if("com.soulstring94.stepcounter.STEP_COUNT".equals(intent.getAction())) {
                int stepCount = intent.getIntExtra("count", 0);
                txtStep.setText(String.valueOf(stepCount));
            }
        }
    };

    private BroadcastReceiver gpsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if("com.soulstring94.stepcounter.GPS_INFO".equals(intent.getAction())) {
                String getLatLng = intent.getStringExtra("latlng");
                String[] latlng = getLatLng.split(",");
                double latitude = Double.parseDouble(latlng[0]);
                double longitude = Double.parseDouble(latlng[1]);

                mapPolyline.addPoint(MapPoint.mapPointWithGeoCoord(latitude, longitude));

                mapView.addPolyline(mapPolyline);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initMain();

        accessPermission();

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            switch (id) {
                case R.id.item_info:
                    Toast.makeText(getApplicationContext(), "내 정보", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.item_notice:
                    Toast.makeText(getApplicationContext(), "공지사항", Toast.LENGTH_SHORT).show();
                    break;
            }

            return true;
        });

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_dehaze_24);
        toolbar.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);

        mapPolyline = new MapPolyline();
        mapPolyline.setLineColor(Color.argb(255, 0, 153, 255));

        mapView = new MapView(this);
        mapViewContainer = (ViewGroup) findViewById(R.id.mapView);
        mapViewContainer.addView(mapView);
        mapView.setMapViewEventListener(this);
        mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);

        resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if(result.getResultCode() == Activity.RESULT_OK) {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                handleSignResult(task);
            } else {
                Log.e("failed", String.valueOf(result.getResultCode()));
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .build();

        googleSignInClient = GoogleSignIn.getClient(MainActivity.this, gso);

        IntentFilter filter = new IntentFilter("com.soulstring94.stepcounter.STEP_COUNT");
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, filter);

        Intent serviceIntent = new Intent(this, StepCounterService.class);

        IntentFilter filter2 = new IntentFilter("com.soulstring94.stepcounter.GPS_INFO");
        LocalBroadcastManager.getInstance(this).registerReceiver(gpsReceiver, filter2);

        Intent serviceIntent2 = new Intent(this, StepCounterService.class);

        View.OnClickListener clickListener = view -> {
            switch (view.getId()) {
                case R.id.btnGoogleLogin:
                    googleLogin();
                    break;
                case R.id.btnKakaoLogin:
                    kakaoLogin();
                    break;
                case R.id.btnStart:
                    startService(serviceIntent);
                    startService(serviceIntent2);
                    break;
                case R.id.btnStop:
                    stopService(serviceIntent);
                    stopService(serviceIntent2);
                    mapView.removeAllPolylines();
                    txtStep.setText("0");
                    break;
            }
        };

        btnGoogleLogin.setOnClickListener(clickListener);
        btnKakaoLogin.setOnClickListener(clickListener);

        btnStart.setOnClickListener(clickListener);
        btnStop.setOnClickListener(clickListener);
    }

    private void initMain() {
        toolbar = (Toolbar)findViewById(R.id.toolbar);

        layoutLogin = findViewById(R.id.layoutLogin);
        layoutMain = findViewById(R.id.layoutMain);

        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
        btnKakaoLogin = findViewById(R.id.btnKakaoLogin);
        btnStart = findViewById(R.id.btnStart);
        btnStop = findViewById(R.id.btnStop);

        txtStep = findViewById(R.id.txtStep);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        navigationView = (NavigationView) findViewById(R.id.navigationView);

        layoutLogin.setVisibility(View.VISIBLE);
        drawerLayout.setVisibility(View.GONE);
    }

    private void googleLogin() {
        Intent googleSignInIntent = googleSignInClient.getSignInIntent();
        resultLauncher.launch(googleSignInIntent);

        Toast.makeText(getApplicationContext(), "로그인", Toast.LENGTH_SHORT).show();
        layoutLogin.setVisibility(View.GONE);
        drawerLayout.setVisibility(View.VISIBLE);
        googleLoginFlag = true;
    }

    private void handleSignResult(Task<GoogleSignInAccount> completedTask) {
        GoogleSignInAccount account = completedTask.getResult();
        String email = account.getEmail();
        String displayName = account.getDisplayName();

        Log.e("loginWithGoogle", "email: " + email + "\ndisplayName: " + displayName);
    }

    private void kakaoLogin() {
        if(UserApiClient.getInstance().isKakaoTalkLoginAvailable(MainActivity.this)) {
            UserApiClient.getInstance().loginWithKakaoTalk(MainActivity.this,(oAuthToken, error) -> {
                if (error != null) {
                    Toast.makeText(getApplicationContext(), "로그인 실패, 다시 시도해주세요", Toast.LENGTH_SHORT).show();
                    Log.e("loginWithKakaoTalk", error.toString());
                } else if (oAuthToken != null) {
                    Toast.makeText(getApplicationContext(), "카카오톡 계정으로 로그인 하였습니다.", Toast.LENGTH_SHORT).show();
                    Log.e("loginWithKakaoTalk", "로그인 성공(토큰) : " + oAuthToken.getAccessToken());
                    getUserKakaoInfo();
                }
                return null;
            });
        } else {
            UserApiClient.getInstance().loginWithKakaoAccount(MainActivity.this,(oAuthToken, error) -> {
                if (error != null) {
                    Toast.makeText(getApplicationContext(), "로그인 실패, 다시 시도해주세요", Toast.LENGTH_SHORT).show();
                    Log.e("loginWithKakaoAccount", error.toString());
                } else if (oAuthToken != null) {
                    Toast.makeText(getApplicationContext(), "카카오톡 계정으로 로그인 하였습니다.", Toast.LENGTH_SHORT).show();
                    Log.e("loginWithKakaoAccount", "로그인 성공(토큰) : " + oAuthToken.getAccessToken());
                    getUserKakaoInfo();
                }
                return null;
            });
        }
    }

    public void getUserKakaoInfo(){
        String TAG = "getUserInfo()";
        UserApiClient.getInstance().me((user, meError) -> {
            if (meError != null) {
                Toast.makeText(getApplicationContext(), "로그인 실패, 다시 시도해주세요", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "사용자 정보 요청 실패", meError);
            } else {
                layoutLogin.setVisibility(View.GONE);
                drawerLayout.setVisibility(View.VISIBLE);
                kakaoLoginFlag = true;
            }
            return null;
        });
    }

    private void logout() {
        if(googleLoginFlag && kakaoLoginFlag) {
            googleLogout();
            kakaoLogout();
        } else if(googleLoginFlag) {
            googleLogout();
        } else if(kakaoLoginFlag) {
            kakaoLogout();
        }

        drawerLayout.setVisibility(View.GONE);
        layoutLogin.setVisibility(View.VISIBLE);
    }

    private void googleLogout() {
        googleSignInClient.signOut();
        googleSignInClient.revokeAccess();

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if(account == null) {
            Toast.makeText(getApplicationContext(), "로그아웃", Toast.LENGTH_SHORT).show();
            googleLoginFlag = false;
        } else {
            Toast.makeText(getApplicationContext(), "로그인", Toast.LENGTH_SHORT).show();
        }
    }

    private void kakaoLogout() {
        Toast.makeText(getApplicationContext(), "로그아웃", Toast.LENGTH_SHORT).show();
        UserApiClient.getInstance().unlink(throwable -> null);
        kakaoLoginFlag = false;
    }

    private void accessPermission() {
        // permission
        int internetPermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.INTERNET);
        int fineLocationPermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        int coarseLocationPermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION);

        if(internetPermission == PackageManager.PERMISSION_DENIED || fineLocationPermission == PackageManager.PERMISSION_DENIED
                || coarseLocationPermission == PackageManager.PERMISSION_DENIED) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(
                        new String[] {
                                android.Manifest.permission.INTERNET, android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION
                        }, 1000
                );
            }
            return;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 1000) {
            boolean checkResult = true;

            for(int result : grantResults) {
                if(result != PackageManager.PERMISSION_GRANTED) {
                    checkResult = false;
                    break;
                }
            }

            if(!checkResult) {
                finish();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                drawerLayout.openDrawer(GravityCompat.END);
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(gpsReceiver);
    }

    @Override
    public void onCurrentLocationUpdate(MapView mapView, MapPoint mapPoint, float v) {

    }

    @Override
    public void onCurrentLocationDeviceHeadingUpdate(MapView mapView, float v) {

    }

    @Override
    public void onCurrentLocationUpdateFailed(MapView mapView) {

    }

    @Override
    public void onCurrentLocationUpdateCancelled(MapView mapView) {

    }

    @Override
    public void onMapViewInitialized(MapView mapView) {

    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int i) {

    }

    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {

    }
}