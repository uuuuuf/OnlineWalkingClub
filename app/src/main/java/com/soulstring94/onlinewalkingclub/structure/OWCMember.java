package com.soulstring94.onlinewalkingclub.structure;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OWCMember {

    @Expose
    @SerializedName("MEMBEREMAIL")
    private String memberEmail;

    @Expose
    @SerializedName("MEMBERNICKNAME")
    private String memberNickName;

    @Expose
    @SerializedName("LOGINCONNECT")
    private String loginConnect;

    @Expose
    @SerializedName("SUCCESS")
    private Boolean success;

    @Expose
    @SerializedName("MESSAGE")
    private String message;

    public String getMemberEmail() {
        return memberEmail;
    }

    public void setMemberEmail(String memberEmail) {
        this.memberEmail = memberEmail;
    }

    public String getMemberNickName() {
        return memberNickName;
    }

    public void setMemberNickName(String memberNickName) {
        this.memberNickName = memberNickName;
    }

    public String getLoginConnect() {
        return loginConnect;
    }

    public void setLoginConnect(String loginConnect) {
        this.loginConnect = loginConnect;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
