package com.soulstring94.onlinewalkingclub;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;
import com.kakao.sdk.user.UserApiClient;

public class MainActivity extends AppCompatActivity {

    ConstraintLayout layoutLogin, layoutMain;

    TextView btnGoogleLogin;
    ImageView btnKakaoLogin;
    Button btnLogout;

    private ActivityResultLauncher<Intent> resultLauncher;

    GoogleSignInClient googleSignInClient;

    boolean googleLoginFlag = false;
    boolean kakaoLoginFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        accessPermission();

        initMain();

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

        View.OnClickListener clickListener = view -> {
            switch (view.getId()) {
                case R.id.btnGoogleLogin:
                    googleLogin();
                    break;
                case R.id.btnKakaoLogin:
                    kakaoLogin();
                    break;
                case R.id.btnLogout:
                    logout();
                    break;
            }
        };

        btnGoogleLogin.setOnClickListener(clickListener);
        btnKakaoLogin.setOnClickListener(clickListener);
        btnLogout.setOnClickListener(clickListener);
    }

    private void initMain() {
        layoutLogin = findViewById(R.id.layoutLogin);
        layoutMain = findViewById(R.id.layoutMain);

        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
        btnKakaoLogin = findViewById(R.id.btnKakaoLogin);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void googleLogin() {
        Intent googleSignInIntent = googleSignInClient.getSignInIntent();
        resultLauncher.launch(googleSignInIntent);

        Toast.makeText(getApplicationContext(), "로그인", Toast.LENGTH_SHORT).show();
        layoutLogin.setVisibility(View.GONE);
        layoutMain.setVisibility(View.VISIBLE);
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
                layoutMain.setVisibility(View.VISIBLE);
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

        layoutMain.setVisibility(View.GONE);
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

}