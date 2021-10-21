package com.example.everylive.mypage;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.UserManager;
import android.util.Log;
import android.view.View;


import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;
import com.android.volley.toolbox.Volley;
import com.example.everylive.R;
import com.example.everylive.home.Activity_Home;
import com.example.everylive.login.Activity_Login;
import com.kakao.sdk.common.KakaoSdk;
import com.kakao.sdk.user.UserApiClient;
import com.nhn.android.naverlogin.OAuthLogin;

public class Activity_My extends AppCompatActivity {

    private static final String TAG = "마이페이지";

    OAuthLogin mOAuthLoginModule;
    Context mContext;

    TextView btn_logout, btn_secession;

    // 서버 주소
    private static String IP_ADDRESS = "http://3.36.159.193";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        btn_logout = findViewById(R.id.btn_logout);
        btn_secession = findViewById(R.id.btn_secession);

//        SharedPreferences sharedPref2 = getApplicationContext().getSharedPreferences(
//                    "NaverOAuthLoginPreferenceData", Context.MODE_PRIVATE);
//            SharedPreferences.Editor editor2 = sharedPref2.edit();
//
//            editor2.remove("ACCESS_TOKEN"); // 값 삭제하기
//
//        editor2.remove("REFRESH_TOKEN"); // 값 삭제하기
//            editor2.apply(); // 있어야 값 저장,삭제됨



        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
                "userInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        // 로그아웃에 사용. naver냐 kakao냐에 따라 다른 로그아웃 처리 필요.
        String snsType = sharedPref.getString("snsType","defValue");
//        editor.putString("snsType","naver");
//        editor.apply();


        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d("setOnClickListener", snsType);

             if(snsType.equals("naver")){

                 // 인스턴스를 초기화
                 mOAuthLoginModule = OAuthLogin.getInstance();
                 mOAuthLoginModule.init(
                         getApplicationContext()
                         ,getString(R.string.naver_client_id)
                         ,getString(R.string.naver_client_secret)
                         ,getString(R.string.naver_client_name)

                 );


                 // 네이버 연동해제(클라이언트 및 서버의 토큰 사라짐)


                 //로그아웃
                 mOAuthLoginModule.logout(mContext);

                 //탈퇴하기(연동해제)
                 new DeleteTokenTask().execute();
//                 Toast.makeText(Activity_My.this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
                 Log.d(TAG, "네이버 로그아웃");

//                 SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
//                         "userInfo", Context.MODE_PRIVATE);
//                 SharedPreferences.Editor editor = sharedPref.edit();
                 editor.remove("uniqueID");
                 editor.remove("birthday"); // 값 삭제하기
                 editor.remove("nickName");
                 editor.remove("gender"); // 값 삭제하기
                 editor.remove("snsType"); // 값 삭제하기
                 editor.remove("profileIMG"); // 값 삭제하기
                 editor.remove("idx_user"); // 값 삭제하기




             }else if(snsType.equals("kakao")){
                 // 인스턴스를 초기화
                 KakaoSdk.init(getApplicationContext(),"a9ccf9d78e7ad93cfcaa07604daa04f7");



                 // 로그아웃
                 UserApiClient.getInstance().logout(error -> {
                             if (error != null) {
                                 Log.e(TAG, "로그아웃 실패. SDK에서 토큰 삭제됨", error);
                             } else {
                                 Log.i(TAG, "로그아웃 성공. SDK에서 토큰 삭제됨");
                             }
                     return null;
                 });

                 // 회원탈퇴(연동해제)
//                 UserApiClient.getInstance().unlink(error ->{
//                     if (error != null) {
//                         Log.e(TAG, "연결 끊기 실패", error);
//                     }
//                     else {
//                         Log.i(TAG, "연결 끊기 성공. SDK에서 토큰 삭제 됨");
//                     }
//                     return null;
//                 });



                 editor.remove("uniqueID");
                 editor.remove("birthday"); // 값 삭제하기
                 editor.remove("nickName");
                 editor.remove("gender"); // 값 삭제하기
                 editor.remove("snsType"); // 값 삭제하기
                 editor.remove("profileIMG"); // 값 삭제하기
                 editor.remove("idx_user"); // 값 삭제하기

             }
                editor.apply();


                // 로그아웃 했으니 로그인 화면으로 이동
                Intent intent = new Intent(getApplicationContext(), Activity_Login.class);
                startActivity(intent);
                finish(); // 현재 액티비티 종료
                 }
                 });


        btn_secession.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {

             if(snsType.equals("naver")){

                 // 인스턴스를 초기화
                 mOAuthLoginModule = OAuthLogin.getInstance();
                 mOAuthLoginModule.init(
                         getApplicationContext()
                         ,getString(R.string.naver_client_id)
                         ,getString(R.string.naver_client_secret)
                         ,getString(R.string.naver_client_name)

                 );


                 // 네이버 연동해제(클라이언트 및 서버의 토큰 사라짐)
                 // 탈퇴하기(연동해제)
                 new DeleteTokenTask().execute();
//                 Toast.makeText(Activity_My.this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
                 Log.d(TAG, "탈퇴하기");

                 // DB값  삭제
                 sendDataforDeleteUser();
                 editor.remove("uniqueID");
                 editor.remove("birthday"); // 값 삭제하기
                 editor.remove("nickName");
                 editor.remove("gender"); // 값 삭제하기
                 editor.remove("snsType"); // 값 삭제하기
                 editor.remove("profileIMG"); // 값 삭제하기
                 editor.remove("idx_user"); // 값 삭제하기




             }else if(snsType.equals("kakao")){
                 // 인스턴스를 초기화
                 KakaoSdk.init(getApplicationContext(),"a9ccf9d78e7ad93cfcaa07604daa04f7");


                 // 회원탈퇴(연동해제)
                 UserApiClient.getInstance().unlink(error ->{
                     if (error != null) {
                         Log.e(TAG, "연결 끊기 실패", error);
                     }
                     else {
                         Log.i(TAG, "연결 끊기 성공. SDK에서 토큰 삭제 됨");
                     }
                     return null;
                 });
                 // DB값  삭제
                 sendDataforDeleteUser();
                 editor.remove("uniqueID");
                 editor.remove("birthday"); // 값 삭제하기
                 editor.remove("nickName");
                 editor.remove("gender"); // 값 삭제하기
                 editor.remove("snsType"); // 값 삭제하기
                 editor.remove("profileIMG"); // 값 삭제하기
                 editor.remove("idx_user"); // 값 삭제하기

             }
             editor.apply();



             // 회원탈퇴 했으니 로그인 화면으로 이동
             Intent intent = new Intent(getApplicationContext(), Activity_Login.class);
             startActivity(intent);
             finish(); // 현재 액티비티 종료
                 }
                 });


    } // 온크리에이트





    // 네이버 로그인 토큰 연동해제
    private class DeleteTokenTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            boolean isSuccessDeleteToken = mOAuthLoginModule.logoutAndDeleteToken(mContext);
            if (!isSuccessDeleteToken) {
                // 서버에서 token 삭제에 실패했어도 클라이언트에 있는 token 은 삭제되어 로그아웃된 상태이다
                // 실패했어도 클라이언트 상에 token 정보가 없기 때문에 추가적으로 해줄 수 있는 것은 없음
                Log.d(TAG, "doInBackground: ");
            }
            return null;
        }
    }



    public void sendDataforDeleteUser() {

        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
                    "userInfo", Context.MODE_PRIVATE);

            String uniqueID = sharedPref.getString("uniqueID","defValue"); // 값 불러오기

            //안드로이드에서 보낼 데이터를 받을 php 서버 주소
            String serverUrl=IP_ADDRESS + "/everyLive/login_register/Deleteuser.php";


            //파일 전송 요청 객체 생성[결과를 String으로 받음]
            SimpleMultiPartRequest smpr= new SimpleMultiPartRequest(Request.Method.POST, serverUrl, new Response.Listener<String>() {
    @Override
    public void onResponse(String response) {
//            new AlertDialog.Builder(sss.this).setMessage("응답:"+response).create().show();
//            Toast.makeText(sss.this,response, Toast.LENGTH_LONG).show();

            }
            }, new Response.ErrorListener() {
    @Override
    public void onErrorResponse(VolleyError error) {
    //                Toast.makeText(Createband_2.this, "ERROR", Toast.LENGTH_SHORT).show();
            }
            });

            //요청 객체에 보낼 데이터를 추가
            smpr.addStringParam("uniqueID", uniqueID);

            //요청객체를 서버로 보낼 우체통 같은 객체 생성
            RequestQueue requestQueue= Volley.newRequestQueue(this);
            requestQueue.add(smpr);

            }



}