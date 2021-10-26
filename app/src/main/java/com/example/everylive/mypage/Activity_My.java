package com.example.everylive.mypage;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.everylive.R;
import com.example.everylive.mypage.Request.RequestGetUseInfo;
import org.json.JSONException;
import org.json.JSONObject;
import de.hdodenhof.circleimageview.CircleImageView;
import com.android.volley.Request;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;
import com.example.everylive.login.Activity_Login;
import com.kakao.sdk.common.KakaoSdk;
import com.kakao.sdk.user.UserApiClient;
import com.nhn.android.naverlogin.OAuthLogin;

public class Activity_My extends AppCompatActivity {

    CircleImageView profileIMG;
    TextView userNickName, userID, cnt_fan, cnt_star;
    ConstraintLayout go_to_activityMypage;

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

        /**
         * @author :김태희
         * @brief : 로그아웃 / 회원탈퇴 (~10/21)
         * */
        btn_logout = findViewById(R.id.btn_logout);
        btn_secession = findViewById(R.id.btn_secession);

        /**
         * @author : 김수미
         * @brief :
         * */
        profileIMG = findViewById(R.id.profileIMG);
        userNickName = findViewById(R.id.userNickName);
        userID = findViewById(R.id.userID);
        cnt_fan = findViewById(R.id.cnt_fan);
        cnt_star = findViewById(R.id.cnt_star);
        go_to_activityMypage = findViewById(R.id.go_to_activityMypage);

        /** @author : 김태희 */
        // snsType check : 로그아웃에 사용. naver냐 kakao냐에 따라 다른 로그아웃 처리 필요.
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
                "userInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        String snsType = sharedPref.getString("snsType","defValue");

        // 로그아웃
        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("setOnClickListener", snsType);

                if(snsType.equals("naver")){
                    // naver 인스턴스를 초기화
                    mOAuthLoginModule = OAuthLogin.getInstance();
                    mOAuthLoginModule.init(
                            getApplicationContext()
                            ,getString(R.string.naver_client_id)
                            ,getString(R.string.naver_client_secret)
                            ,getString(R.string.naver_client_name)

                    );

                    //로그아웃시, 로컬 토큰이 삭제되어야한다.
                    Log.d(TAG, "네이버 로그아웃");
                    mOAuthLoginModule.logout(mContext); // 로컬 토큰 삭제
                    //new DeleteTokenTask().execute(); // 서버 토큰 삭제

                } else if(snsType.equals("kakao")){
                    // kakao 인스턴스를 초기화
                    KakaoSdk.init(getApplicationContext(),"a9ccf9d78e7ad93cfcaa07604daa04f7");

                    UserApiClient.getInstance().logout(error -> {
                        if (error != null) {
                            Log.e(TAG, "로그아웃 실패. SDK에서 토큰 삭제됨", error);
                        } else {
                            Log.i(TAG, "로그아웃 성공. SDK에서 토큰 삭제됨");
                        }
                        return null;
                    });
                }

                // 자동로그인 방지를 위해 쉐어드 값 삭제하기
                editor.remove("uniqueID");
                editor.remove("birthday"); // 값 삭제하기
                editor.remove("nickName");
                editor.remove("gender"); // 값 삭제하기
                editor.remove("snsType"); // 값 삭제하기
                editor.remove("profileIMG"); // 값 삭제하기
                editor.remove("idx_user"); // 값 삭제하기

                editor.apply();


                // 로그아웃 했으니 로그인 화면으로 이동
                Intent intent = new Intent(getApplicationContext(), Activity_Login.class);
                startActivity(intent);
                finish(); // 현재 액티비티 종료
            }
        });

        // 회원탈퇴
        btn_secession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(Activity_My.this);
                builder.setTitle("회원탈퇴").setMessage("정말 탈퇴하시겠습니까?");
                builder.setPositiveButton("확인", new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialog, int id)
                    {
                        // 확인 눌렀을 때 naver, kakao 구분하고 탈퇴처리 및 토큰연동해제
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
                builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        // 취소 눌렀을 때 내용. 없으면 다이얼로그 닫히기만함.
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show(); // 다이얼로그 보여주기


            }
        });


        /** @author : 김수미 */
        // 레이아웃 영역 누르면, activity_mypage로 이동하기.
        go_to_activityMypage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Activity_My.this, Activity_Mypage.class);

                SharedPreferences sharedPreferences = getSharedPreferences("userInfo", MODE_PRIVATE);
                String idx_user = sharedPreferences.getString("idx_user",null);
                intent.putExtra("page_owner", idx_user);

                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // 쉐어드에서 idx_user 값을 가져와서 DB에서 정보 가져오기.
        SharedPreferences sharedPreferences = this.getSharedPreferences("userInfo", MODE_PRIVATE);
        String idx_user = sharedPreferences.getString("idx_user",null);
        System.out.println(idx_user);
        getUserInfo(idx_user);
    }

    public void getUserInfo(String idx_user){
        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.d("response", response);

                try {
                    JSONObject jsonObject = new JSONObject(response);

                    Boolean success = jsonObject.getBoolean("success");

                    if(success){
                        String nickName_server = jsonObject.getString("nickName");
                        String userID_server = jsonObject.getString("userID");
                        int cnt_fan_server = jsonObject.getInt("cnt_fan");
                        int cnt_star_server = jsonObject.getInt("cnt_star");
                        String profileIMG_server = jsonObject.getString("profileIMG");

                        userNickName.setText(nickName_server);
                        userID.setText(userID_server);
                        cnt_fan.setText(Integer.toString(cnt_fan_server));
                        cnt_star.setText(Integer.toString(cnt_star_server));

                        Glide.with(Activity_My.this)
                                .load(profileIMG_server)
                                .override(400, 400)
                                .centerCrop()
                                .into(profileIMG);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        RequestGetUseInfo requestGetUseInfo = new RequestGetUseInfo(idx_user, "null", responseListener);
        RequestQueue queue = Volley.newRequestQueue(Activity_My.this);
        queue.add(requestGetUseInfo);
    }

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

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(Createband_2.this, "ERROR", Toast.LENGTH_SHORT).show();
            }
        });

        //요청 객체에 보낼 데이터를 추가
        smpr.addStringParam("uniqueID", uniqueID);

        //요청객체를 서버로 보낼 우체통 같은 객체 생성
        RequestQueue requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(smpr);
    }
}