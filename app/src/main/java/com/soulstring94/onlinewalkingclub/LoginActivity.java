package com.soulstring94.onlinewalkingclub;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;
import com.kakao.sdk.user.UserApiClient;
import com.soulstring94.onlinewalkingclub.retrofit2.ApiClient;
import com.soulstring94.onlinewalkingclub.retrofit2.ApiInterface;
import com.soulstring94.onlinewalkingclub.structure.OWCMember;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    TextView btnGoogleLogin;
    ImageView btnKakaoLogin;
    CheckBox cbLogin;

    private ActivityResultLauncher<Intent> resultLauncher;

    GoogleSignInClient googleSignInClient;

    String googleLoginFlag = "false";
    String kakaoLoginFlag = "false";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initLogin();

        SharedPreferences sharedPreferences = getSharedPreferences("loginInfo", MODE_PRIVATE);
        if(sharedPreferences.getBoolean("loginMemory", false)) {
            if(!sharedPreferences.getString("googleLoginEmail", "").equals("")) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra("googleLoginFlag", "true");
                startActivity(intent);
                finish();
            } else if(!sharedPreferences.getString("kakaoLoginEmail", "").equals("")) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra("kakaoLoginFlag", "true");
                startActivity(intent);
                finish();
            }
        }

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

        cbLogin.setOnClickListener(v -> {
            if(cbLogin.isChecked()) {
                cbLogin.setChecked(true);
                SharedPreferences checkboxShared = getSharedPreferences("loginInfo", MODE_PRIVATE);
                SharedPreferences.Editor editor = checkboxShared.edit();
                editor.putBoolean("loginMemory", true);
                editor.apply();
            } else {
                cbLogin.setChecked(false);
                SharedPreferences checkboxShared = getSharedPreferences("loginInfo", MODE_PRIVATE);
                SharedPreferences.Editor editor = checkboxShared.edit();
                editor.putBoolean("loginMemory", false);
                editor.apply();
            }
        });
    }

    private void initLogin() {
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
        btnKakaoLogin = findViewById(R.id.btnKakaoLogin);
        cbLogin = findViewById(R.id.cbLogin);
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

        googleLoginFlag = "true";

        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("googleLoginFlag", googleLoginFlag);
        startActivity(intent);
        finish();

        SharedPreferences sharedPreferences = getSharedPreferences("loginInfo", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("googleLoginEmail", email);
        editor.putString("googleLoginNickName", displayName);
        editor.apply();

        checkMember(email, displayName, "google");
    }

    private void kakaoLogin() {
        if(UserApiClient.getInstance().isKakaoTalkLoginAvailable(LoginActivity.this)) {
            UserApiClient.getInstance().loginWithKakaoTalk(LoginActivity.this,(oAuthToken, error) -> {
                if (error != null) {
                    Toast.makeText(getApplicationContext(), R.string.login_failed, Toast.LENGTH_SHORT).show();
                    Log.e("loginWithKakaoTalk", error.toString());
                } else if (oAuthToken != null) {
                    UserApiClient.getInstance().me((user, throwable) -> {
                        if(throwable != null) {
                            return null;
                        }

                        String userNickName = user.getKakaoAccount().getProfile().getNickname();
                        String userEmail = String.valueOf(user.getKakaoAccount().getEmail());

                        SharedPreferences sharedPreferences = getSharedPreferences("loginInfo", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("kakaoLoginEmail", userEmail);
                        editor.putString("kakaoLoginNickName", userNickName);
                        editor.apply();

                        checkMember(userEmail, userNickName, "kakao");

                        return null;
                    });
                    getUserKakaoInfo();
                }
                return null;
            });
        } else {
            UserApiClient.getInstance().loginWithKakaoAccount(LoginActivity.this,(oAuthToken, error) -> {
                if (error != null) {
                    Toast.makeText(getApplicationContext(), R.string.login_failed, Toast.LENGTH_SHORT).show();
                    Log.e("loginWithKakaoAccount", error.toString());
                } else if (oAuthToken != null) {
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
                Toast.makeText(getApplicationContext(), R.string.login_failed, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "사용자 정보 요청 실패", meError);
            } else {
                kakaoLoginFlag = "true";

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra("kakaoLoginFlag", kakaoLoginFlag);
                startActivity(intent);
                finish();
            }
            return null;
        });
    }

    private void checkMember(String memberEmail, String memberNickName, String loginPlatform) {
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<List<OWCMember>> call = apiInterface.OWCMemberCheck(memberEmail, memberNickName, loginPlatform);
        call.enqueue(new Callback<List<OWCMember>>() {
            @Override
            public void onResponse(Call<List<OWCMember>> call, Response<List<OWCMember>> response) {
                if(response.isSuccessful() && response.body().size() > 0) {
                    Toast.makeText(LoginActivity.this, R.string.exist_member, Toast.LENGTH_SHORT).show();
                } else {
                    addMember(memberEmail, memberNickName, loginPlatform);
                }
            }

            @Override
            public void onFailure(Call<List<OWCMember>> call, Throwable t) {
                Log.e("checkMember Error", t.getMessage());
                Toast.makeText(LoginActivity.this, R.string.member_check_failed, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addMember(String memberEmail, String memberNickName, String loginPlatform) {
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<OWCMember> call = apiInterface.OWCMemberInsert(memberEmail, memberNickName, loginPlatform);
        call.enqueue(new Callback<OWCMember>() {
            @Override
            public void onResponse(Call<OWCMember> call, Response<OWCMember> response) {
                if(response.isSuccessful() && response.body() != null) {
                    Boolean success = response.body().getSuccess();
                    if(success) {
                        Toast.makeText(LoginActivity.this, R.string.new_member, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(LoginActivity.this, R.string.insert_member_failed, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<OWCMember> call, Throwable t) {
                Log.e("addMember Error", t.getMessage());
                Toast.makeText(LoginActivity.this, R.string.insert_member_failed, Toast.LENGTH_SHORT).show();
            }
        });
    }
}