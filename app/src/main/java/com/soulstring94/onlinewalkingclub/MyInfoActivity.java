package com.soulstring94.onlinewalkingclub;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MyInfoActivity extends AppCompatActivity {

    TextView txtNickName, txtEmail;
    ImageView imgProfile;

    LinearLayout popupNickNameChange;
    EditText editPopupNickName;
    Button btnPopupOK, btnPopupCancel;

    String deliverNickName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_info);

        initMyInfo();

        SharedPreferences sharedPreferences = getSharedPreferences("loginInfo", MODE_PRIVATE);
        if(!sharedPreferences.getString("googleLoginEmail", "").equals("")) {
            String nickName = sharedPreferences.getString("googleLoginNickName", "");
            String email = sharedPreferences.getString("googleLoginEmail", "");
            txtNickName.setText(nickName);
            txtEmail.setText(email);
        } else if(!sharedPreferences.getString("kakaoLoginEmail", "").equals("")) {
            String nickName = sharedPreferences.getString("kakaoLoginNickName", "");
            String email = sharedPreferences.getString("kakaoLoginEmail", "");
            txtNickName.setText(nickName);
            txtEmail.setText(email);
        }

        txtNickName.setOnLongClickListener(v -> {
            SharedPreferences sharedPopup = getSharedPreferences("loginInfo", MODE_PRIVATE);
            if(!sharedPopup.getString("googleLoginEmail", "").equals("")) {
                String nickName = sharedPopup.getString("googleLoginNickName", "");
                editPopupNickName.setText(nickName);
            } else if(!sharedPopup.getString("kakaoLoginEmail", "").equals("")) {
                String nickName = sharedPopup.getString("kakaoLoginNickName", "");
                editPopupNickName.setText(nickName);
            }

            popupNickNameChange.setVisibility(View.VISIBLE);

            return true;
        });

        View.OnClickListener clickListener = view -> {
            switch (view.getId()) {
                case R.id.btnPopupOK:
                    popupOK();
                    break;
                case R.id.btnPopupCancel:
                    popupCancel();
                    break;
            }
        };

        btnPopupOK.setOnClickListener(clickListener);
        btnPopupCancel.setOnClickListener(clickListener);
    }

    private void initMyInfo() {
        txtNickName = findViewById(R.id.txtNickName);
        txtEmail = findViewById(R.id.txtEmail);
        imgProfile = findViewById(R.id.imgProfile);
        popupNickNameChange = findViewById(R.id.popupNickNameChange);
        editPopupNickName = findViewById(R.id.editPopupNickName);
        btnPopupOK = findViewById(R.id.btnPopupOK);
        btnPopupCancel = findViewById(R.id.btnPopupCancel);
    }

    private void popupOK() {
        SharedPreferences sharedPreferences = getSharedPreferences("loginInfo", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String nickname = editPopupNickName.getText().toString();
        deliverNickName = nickname;
        if(!sharedPreferences.getString("googleLoginEmail", "").equals("")) {
            editor.putString("googleLoginNickName", nickname);
        } else if(!sharedPreferences.getString("kakaoLoginEmail", "").equals("")) {
            editor.putString("kakaoLoginNickName", nickname);
        }
        editor.apply();

        txtNickName.setText(nickname);

        popupNickNameChange.setVisibility(View.GONE);

        Intent resultIntent = new Intent();
        resultIntent.putExtra("nickname", deliverNickName);  // 데이터 전달
        setResult(RESULT_OK, resultIntent);
    }

    private void popupCancel() {
        editPopupNickName.setText("");
        popupNickNameChange.setVisibility(View.GONE);
    }
}