package com.soulstring94.onlinewalkingclub.retrofit2;

import com.soulstring94.onlinewalkingclub.structure.OWCMember;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiInterface {

    @GET("OWC_Member_Check.php")
    Call<List<OWCMember>> OWCMemberCheck(
            @Query("MEMBEREMAIL") String memberEmail,
            @Query("MEMBERNICKNAME") String memberNickName,
            @Query("LOGINCONNECT") String loginConnect
    );

    @FormUrlEncoded
    @POST("OWC_Member_Insert.php")
    Call<OWCMember> OWCMemberInsert(
            @Field("MEMBEREMAIL") String memberEmail,
            @Field("MEMBERNICKNAME") String memberNickName,
            @Field("LOGINCONNECT") String memberLoginConnect
    );
}
