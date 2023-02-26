package com.soulstring94.onlinewalkingclub;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;
import com.kakao.sdk.user.UserApiClient;

public class LoginActivity extends AppCompatActivity {

    TextView btnGoogleLogin;
    ImageView btnKakaoLogin;

    private ActivityResultLauncher<Intent> resultLauncher;

    GoogleSignInClient googleSignInClient;

    boolean googleLoginFlag = false;
    boolean kakaoLoginFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initLogin();

        resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if(result.getResultCode() == Activity.RESULT_OK) {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                handleSignResult(task);
            } else {
                Log.e("failed", result.toString());
            }
        });

        View.OnClickListener clickListener = view -> {
            switch (view.getId()) {
                case R.id.btnGoogleLogin:
                    googleLogin();
                    break;
                case R.id.btnKakaoLogin:
                    kakaoLogin();
                    break;
            }
        };

        btnGoogleLogin.setOnClickListener(clickListener);
        btnKakaoLogin.setOnClickListener(clickListener);
    }

    private void initLogin() {
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
        btnKakaoLogin = findViewById(R.id.btnKakaoLogin);
    }

    private void googleLogin() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .build();

        googleSignInClient = GoogleSignIn.getClient(LoginActivity.this, gso);

        Intent googleSignInIntent = googleSignInClient.getSignInIntent();

        resultLauncher.launch(googleSignInIntent);
    }

    private void handleSignResult(Task<GoogleSignInAccount> completedTask) {
        GoogleSignInAccount account = completedTask.getResult();
        String email = account.getEmail();
        String displayName = account.getDisplayName();

        googleLoginFlag = true;

        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("googleLoginFlag", googleLoginFlag);
        startActivity(intent);
        finish();

        Log.e("loginWithGoogle", "email: " + email + "\ndisplayName: " + displayName);
    }

    private void kakaoLogin() {
        if(UserApiClient.getInstance().isKakaoTalkLoginAvailable(LoginActivity.this)) {
            UserApiClient.getInstance().loginWithKakaoTalk(LoginActivity.this,(oAuthToken, error) -> {
                if (error != null) {
                    Toast.makeText(getApplicationContext(), "로그인 실패, 다시 시도해주세요", Toast.LENGTH_SHORT).show();
                    Log.e("loginWithKakaoTalk", error.toString());
                } else if (oAuthToken != null) {
                    //Toast.makeText(getApplicationContext(), "카카오톡 계정으로 로그인 하였습니다.", Toast.LENGTH_SHORT).show();
                    Log.e("loginWithKakaoTalk", "로그인 성공(토큰) : " + oAuthToken.getAccessToken());
                    getUserKakaoInfo();
                }
                return null;
            });
        } else {
            UserApiClient.getInstance().loginWithKakaoAccount(LoginActivity.this,(oAuthToken, error) -> {
                if (error != null) {
                    Toast.makeText(getApplicationContext(), "로그인 실패, 다시 시도해주세요", Toast.LENGTH_SHORT).show();
                    Log.e("loginWithKakaoAccount", error.toString());
                } else if (oAuthToken != null) {
                    //Toast.makeText(getApplicationContext(), "카카오톡 계정으로 로그인 하였습니다.", Toast.LENGTH_SHORT).show();
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
                kakaoLoginFlag = true;

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra("kakaoLoginFlag", kakaoLoginFlag);
                startActivity(intent);
                finish();
            }
            return null;
        });
    }
}