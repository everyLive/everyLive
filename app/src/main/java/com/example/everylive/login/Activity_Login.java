package com.example.everylive.login;
import android.Manifest;
import android.content.ClipData;
import android.text.TextUtils;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.content.CursorLoader;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;
import com.android.volley.toolbox.Volley;
import com.example.everylive.R;
import com.example.everylive.home.Activity_Home;
import com.kakao.sdk.auth.model.OAuthToken;
import com.kakao.sdk.common.KakaoSdk;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.Account;
import com.kakao.sdk.user.model.Gender;
import com.kakao.sdk.user.model.Profile;
import com.nhn.android.naverlogin.OAuthLogin;
import com.nhn.android.naverlogin.OAuthLoginHandler;
import com.nhn.android.naverlogin.data.OAuthLoginState;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;

public class Activity_Login extends AppCompatActivity {

    private static final String TAG = "로그인";
    private ImageView btn_KakaoLogin, btn_NaverLogin;
    OAuthLogin mOAuthLoginModule;
    Context mContext;

    String snsType, nickName, profileIMG, birthday, gender, kakaogender, uniqueID;

    TextView textView; // 서버에 이미지 올라가는지 확인하기 위해 만듦

    String imgPath;

    // 서버 주소
    private static String IP_ADDRESS = "http://3.36.159.193";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btn_KakaoLogin = findViewById(R.id.btn_KakaoLogin);
        btn_NaverLogin = findViewById(R.id.btn_NaverLogin);


        // 서버에 이미지 올라가는지 확인하기 위해 만듦
        // 지금은 안씀.
//        textView =findViewById(R.id.textView);
//        textView .setOnClickListener(new View.OnClickListener() {
//         @Override
//         public void onClick(View v) {
//             checkSelfPermission();
//                 }
//                 });




        /**
         * @author 김수미
         * @brief 카카오 로그인을 위해 KakoSdk 초기화(삭제시 작동안함) */

        Log.i("GET_KEYHASH", getKeyHash());

        KakaoSdk.init(this,"a9ccf9d78e7ad93cfcaa07604daa04f7");

        btn_KakaoLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInKakao();
            }
        });




        /**
         * @author 김태희
         * @brief 네이버 아이디로 로그인
         * */

        btn_NaverLogin .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 인스턴스를 초기화
                mOAuthLoginModule = OAuthLogin.getInstance();
                mOAuthLoginModule.init(
                        getApplicationContext()
                        ,getString(R.string.naver_client_id)
                        ,getString(R.string.naver_client_secret)
                        ,getString(R.string.naver_client_name)

                );


                String accesstoken = mOAuthLoginModule.getAccessToken(getApplicationContext());
                String state = mOAuthLoginModule.getState(getApplicationContext()).toString();

                if(accesstoken!=null){

                    Log.d("getState", state); // OK. 접근 토큰이 있는 상태.
                    Log.d("getAccessToken", accesstoken);
                    Log.d(TAG, "ACCESS_TOKEN 존재하므로 회원가입 거치지 않고 바로 홈화면이동");
                    Intent intent = new Intent(getApplicationContext(), Activity_Home.class);
                                startActivity(intent);
                                finish(); // 현재 액티비티 종료

                }else{
                    Log.d("getAccessToken", "null");
                    Log.d(TAG, "ACCESS_TOKEN 존재하지 않으므로 sns 로그인후 회원가입");
                    Log.d("getState", state); // NEED_LOGIN. 로그인이 필요한 상태. 접근 토큰(access token)과 갱신 토큰(refresh token)이 모두 없습니다.



                        // 로그인창에서 로그인 완료되거나 뒤로가기 눌러서 취소되는 이벤트 받을 핸들러
                        @SuppressLint("HandlerLeak")
                        OAuthLoginHandler mOAuthLoginHandler = new OAuthLoginHandler() {
                            @Override
                            public void run(boolean success) {
                                if (success) {
                                    String accessToken = mOAuthLoginModule.getAccessToken(mContext);
                                    String refreshToken = mOAuthLoginModule.getRefreshToken(mContext);
                                    long expiresAt = mOAuthLoginModule.getExpiresAt(mContext);
                                    String tokenType = mOAuthLoginModule.getTokenType(mContext);

                                    Log.i("LoginData", "accessToken : " + accessToken);
                                    Log.i("LoginData", "refreshToken : " + refreshToken);
                                    Log.i("LoginData", "expiresAt : " + expiresAt);
                                    Log.i("LoginData", "tokenType : " + tokenType);

                                    new RequestApiTask(mContext, mOAuthLoginModule).execute();


                                } else {
                                    String errorCode = mOAuthLoginModule
                                            .getLastErrorCode(mContext).getCode();
                                    String errorDesc = mOAuthLoginModule.getLastErrorDesc(mContext);
                                    Toast.makeText(getApplicationContext(), "errorCode:" + errorCode
                                            + ", errorDesc:" + errorDesc, Toast.LENGTH_SHORT).show();
                                }
                            }

                            ;
                        };

                        // 로그인 시도
                        mOAuthLoginModule.startOauthLoginActivity(Activity_Login.this, mOAuthLoginHandler);

                    }
            }
        });




        /**
         * 네이버 아이디로 로그인
         * */




    } // 온크리에이트



        /***
         * 서버에 이미지 업로드 되는지 확인하기 위해 만들어짐
         * 지금은 안씀
         */
//        public void checkSelfPermission() {
//            String temp = "";
//            //파일 읽기 권한 확인
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
//                    != PackageManager.PERMISSION_GRANTED) {
//                temp += Manifest.permission.READ_EXTERNAL_STORAGE + " ";
//            } //파일 쓰기 권한 확인
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                temp += Manifest.permission.WRITE_EXTERNAL_STORAGE + " ";
//
//            }if (TextUtils.isEmpty(temp) == false) { // 권한 요청
//                ActivityCompat.requestPermissions(this, temp.trim().split(" "),1);
//
//            }else { // 모두 허용 상태
//    //            Toast.makeText(this, "권한 이미 허용", Toast.LENGTH_SHORT).show();
//
//                // 갤러리 열어서 사진 선택하도록
//    //            Intent intent = new Intent(); //기기 기본 갤러리 접근
//    //            intent.setType(MediaStore.Images.Media.CONTENT_TYPE); //구글 갤러리 접근
//    //            intent.setType("image/*"); intent.setAction(Intent.ACTION_GET_CONTENT);
//    //            startActivityForResult(intent,101);
//                Intent intent= new Intent(Intent.ACTION_PICK);
//                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // 다중이미지 선택가능.
//                intent.setType("image/*"); // 이미지를 볼 수 있음(모든형식)
//                startActivityForResult(intent,10);
//            }
//        }
//
//        // 앨범에서 사진 클릭하여 가져왔을때
//        @Override
//        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//                super.onActivityResult(requestCode, resultCode, data);
//                if (requestCode == 10) { // 이미지
//                if (resultCode == RESULT_OK) {
//
//                // ClipData 또는 Uri를 가져온다
//                Uri uri = data.getData(); // 상대경로
//                ClipData clipData = data.getClipData();
//
//                // 다중이미지 처리
//                // 이미지 URI 를 이용하여 이미지뷰에 순서대로 세팅한다.
//                if (clipData != null) {
//                for (int i = 0; i < clipData.getItemCount(); i++) {
//                if (i < clipData.getItemCount()) {
//                Uri urione = clipData.getItemAt(i).getUri();
//
//                imgPath= getRealPathFromUri(urione); // (상대경로)이미지 상태를 절대경로로 만들기
//                System.out.println("선택한 이미지의 상대경로"+urione);
//                Log.d("선택한 이미지 절대경로", imgPath);
//
//                // 서버에 이미지가 올라가는지 잠깐 확인하기 위해 사용
//                    sendData();
//
//                }
//                }
//                }
//                }
//                }
//                }
//    //Uri -- > 절대경로로 바꿔서 리턴시켜주는 메소드
//    String getRealPathFromUri(Uri uri){
//        String[] proj= {MediaStore.Images.Media.DATA};
//        CursorLoader loader= new CursorLoader(this, uri, proj, null, null, null);
//        Cursor cursor= loader.loadInBackground();
//        int column_index= cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
//        cursor.moveToFirst();
//        String result= cursor.getString(column_index);
//        cursor.close();
//        return  result;
//    }
    /**
     * 서버에 이미지 업로드 되는지 확인하기 위해 만들어짐
     * 지금은 안씀
     */




    /**
     * 네이버 아이디로 로그인
     * */
    // 네이버 로그인 토큰 연동해제
    public class DeleteTokenTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            boolean isSuccessDeleteToken = mOAuthLoginModule.logoutAndDeleteToken(mContext);


            if (!isSuccessDeleteToken) {
                // 서버에서 token 삭제에 실패했어도 클라이언트에 있는 token 은 삭제되어 로그아웃된 상태이다
                // 실패했어도 클라이언트 상에 token 정보가 없기 때문에 추가적으로 해줄 수 있는 것은 없음
            }

            return null;
        }


    }


    // 네이버 로그인 결과값 가져오기
    public class RequestApiTask extends AsyncTask<Void, Void, String> {
        private final Context mContext;
        private final OAuthLogin mOAuthLoginModule;
        public RequestApiTask(Context mContext, OAuthLogin mOAuthLoginModule) {
            this.mContext = mContext;
            this.mOAuthLoginModule = mOAuthLoginModule;
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected String doInBackground(Void... params) {
            String url = "https://openapi.naver.com/v1/nid/me";
            String at = mOAuthLoginModule.getAccessToken(mContext);
            return mOAuthLoginModule.requestApi(mContext, at, url);
        }

        protected void onPostExecute(String content) {
            try {
                Log.d("로그인한사용자정보", content);
                JSONObject loginResult = new JSONObject(content);
                if (loginResult.getString("resultcode").equals("00")){
                    JSONObject response = loginResult.getJSONObject("response");
                    uniqueID = response.getString("id");


                    if(response.has("nickname")) {
                        nickName = response.getString("nickname");
                        Log.d("사용자정보-닉네임", nickName);


                    }
                    if(response.has("profile_image")) {
                        profileIMG = response.getString("profile_image");
                        Log.d("사용자정보-프로필", profileIMG);


                    }
                    if(response.has("gender")) {
                        gender = response.getString("gender");
                        Log.d("사용자정보-성별", gender);


                    }
                    if(response.has("birthday")) {
                        birthday = response.getString("birthday");
                        Log.d("사용자정보-생일", birthday);


                    }
//                    Toast.makeText(mContext, "email : "+email +" name : "+name+" birthyear : "+birthyear, Toast.LENGTH_LONG).show();

                    // 네이버 로그인할때 체크한 데이터를 Activity_Register에 인텐트로 넘긴다
//                    Intent intent = new Intent(getApplicationContext(), Activity_Register.class);
//
//                    intent.putExtra("birthday",birthday);
//                    intent.putExtra("gender",gender);
//                    intent.putExtra("profileimgURL",profileIMG);
//                    intent.putExtra("nickName",nickName);
//                    intent.putExtra("snsType","naver");
//                    intent.putExtra("uniqueID",uniqueID);
//                    startActivity(intent);
//                    finish();

                    snsType = "naver";
                    // 고유식별자 있는지 체크. 있으면 바로 홈화면. 없으면 회원가입화면.
                    sendDataForUniqueIDcheck();

//                    SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
//                            "userInfo", Context.MODE_PRIVATE);
//
//                    if(sharedPref.contains("uniqueID")){ // 고유식별자 있으면 바로 홈화면 이동
//                        Intent intent2 = new Intent(getApplicationContext(), Activity_Home.class);
//                        startActivity(intent2);
//                        finish();
//
//                    }else{ // 고유식별자 없으면. 회원가입 해야함. 회원가입하러 이동.

                        // 카카오 로그인할때 체크한 데이터를 Activity_Register에 인텐트로 넘긴다
//                        Intent intent = new Intent(getApplicationContext(), Activity_Register.class);
//
//                        intent.putExtra("birthday",birthday);
//                        intent.putExtra("gender",gender);
//                        intent.putExtra("profileimgURL",profileIMG);
//                        intent.putExtra("nickName",nickName);
//                        intent.putExtra("snsType","naver");
//                        intent.putExtra("uniqueID",uniqueID);
//                        startActivity(intent);
//                finish();
//                    }

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * 네이버 아이디로 로그인
     * */



    // 카카오 키해시 얻기
    private String getKeyHash(){
        try{
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            if(packageInfo == null) return null;
            for(Signature signature: packageInfo.signatures){
                try{
                    MessageDigest md = MessageDigest.getInstance("SHA");
                    md.update(signature.toByteArray());
                    return Base64.encodeToString(md.digest(), Base64.NO_WRAP);
                }catch (NoSuchAlgorithmException e){
                    Log.w("getKeyHash", "Unable to get MessageDigest. signature="+signature, e);
                }
            }
        }catch(PackageManager.NameNotFoundException e){
            Log.w("getPackageInfo", "Unable to getPackageInfo");
        }
        return null;
    }

    // 카카오 사용자 정보 요청
    private void requestMe(){
        Log.d("requestMe ", "함수안");
        UserApiClient.getInstance().me((user, meError) -> {
            if(meError != null) {
                Log.d("사용자 정보 요청 실패 ", meError.toString());
            }else{
                // 사용자 아이디: user.getId());
                long u = user.getId();
                Log.d("requestMe ", Long.toString(u));

                Account kakaoAccount = user.getKakaoAccount();
                Log.d("requestMe ", String.valueOf(kakaoAccount));

                if (kakaoAccount != null) {
                    Log.d("requestMe ", "kakaoAccount");
                    // 프로필
                    Profile profile = kakaoAccount.getProfile();
                    if (profile != null) {
                        String nickName = profile.getNickname();
                        String profileIMG = profile.getProfileImageUrl();

                        String birthday = kakaoAccount.getBirthday();
                        Gender gender = kakaoAccount.getGender();

                        Log.d("사용자정보-닉네임", nickName);
                        Log.d("사용자정보-프로필", profileIMG);
                        Log.d("사용자정보-생일", birthday);
                        Log.d("사용자정보-성별", gender.toString());
                    }
                }else{
                    Log.d("requestMe ", "kakaoAccountNull");
                }

            }
            Log.d("requestMe ", "??");
            return null;
        });
    }

    private void signInKakao() {
        /**
         * @author 김수미
         * @brief : 카카오톡이 설치되어 있으면 카카오톡으로 로그인, 아니면 카카오계정으로 로그인 */
        if (UserApiClient.getInstance().isKakaoTalkLoginAvailable(this))
            UserApiClient.getInstance().loginWithKakaoTalk(this, callback);
        else UserApiClient.getInstance().loginWithKakaoAccount(this, callback);
    }

    /**
     * @author 김수미
     * @brief : 로그인 결과 수행에 관한 콜백메서드
     * @see : token이 전달되면 로그인 성공, token 전달 안되면 로그인 실패  */
    Function2<OAuthToken, Throwable, Unit> callback = (oAuthToken, throwable) -> {
        if (oAuthToken != null) {
            Log.i("TAG_KAKAO", "성공");
//            Toast.makeText(this, "카카오 로그인이 정상적으로 수행됐습니다.", Toast.LENGTH_SHORT).show();
            updateKakaoLogin();


//                    Intent intent = new Intent(getApplicationContext(), Activity_Home.class);
//                    startActivity(intent);

//            requestMe();
        }
        if (throwable != null) {
            Log.i("TAG_KAKAO", "실패");
            Toast.makeText(this, "카카오 로그인을 실패했습니다.", Toast.LENGTH_SHORT).show();
            Log.e("signInKakao()", throwable.getLocalizedMessage());
        }
        return null;
    };

    /**
     * @author 김수미
     * @brief : 로그인 여부를 확인 및 update UI */
    private void updateKakaoLogin() {
        UserApiClient.getInstance().me((user, throwable) -> {
            /** @brief : 로그인 성공시 user변수에 카카오계정 정보가 넘어온다. */
            if (user != null) {
                Account kakaoAccount = user.getKakaoAccount();
                Profile profile = kakaoAccount.getProfile();

                Log.d(TAG, kakaoAccount.toString());
                nickName = profile.getNickname();
                profileIMG = profile.getProfileImageUrl(); // 프로필 사진 640*640
                String thumbnail_image = profile.getThumbnailImageUrl(); // 프로필 사진 110*110

                birthday = kakaoAccount.getBirthday();
                Gender gender2 = kakaoAccount.getGender();
                long uniquID = user.getId();

                uniqueID = String.valueOf(uniquID);
                snsType = "kakao";

                Log.i("사용자정보-고유번호", String.valueOf(uniquID));
                if(nickName!=null){
                    Log.i("사용자정보-닉네임", nickName);
                }
                if(profileIMG!=null){
                    Log.i("사용자정보-프로필", profileIMG);
                }
                if(birthday!=null){
                    Log.i("사용자정보-생일", birthday);
                }
                if(gender2!=null) {
                    Log.i("사용자정보-성별", gender2.toString());
                    if(gender2.toString().equals("MALE")){
                        gender = "M";
                    }else if(gender2.toString().equals("FEMALE")){
                        gender = "F";
                    }

                }


                // 고유식별자 있는지 체크. 있으면 바로 홈화면. 없으면 회원가입화면.
                sendDataForUniqueIDcheck();
//
//                SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
//                        "userInfo", Context.MODE_PRIVATE);
//
//                if(sharedPref.contains("uniqueID")){ // 고유식별자 있으면 바로 홈화면 이동
//                    Intent intent2 = new Intent(getApplicationContext(), Activity_Home.class);
//                    startActivity(intent2);
//                    finish();
//
//                }else{ // 고유식별자 없으면. 회원가입 해야함. 회원가입하러 이동.

                    // 카카오 로그인할때 체크한 데이터를 Activity_Register에 인텐트로 넘긴다
//                    Intent intent = new Intent(getApplicationContext(), Activity_Register.class);
//
//                    intent.putExtra("birthday",birthday);
//                    intent.putExtra("gender",kakaogender);
//                    intent.putExtra("profileimgURL",profileIMG);
//                    intent.putExtra("nickName",nickName);
//                    intent.putExtra("snsType","kakao");
//                    intent.putExtra("uniqueID",String.valueOf(uniquID));
//                    startActivity(intent);
//                finish();

//                }

                /** DB에서 있는 값인지 체크하고, 없으면 회원가입. 생일과 성별이 null인지 체크하고, 없으면 값을 받는 방식으로? */
            } else {   // 로그인 실패
                Log.i("updateKakaoLogin ", "kakaoAccount-Null");
            }
            return null;
        });
    }

    // 로그아웃 처리 (카카오톡)
//    UserApiClient.getInstance().logout(error -> {
//        return null;
//    });


    // sns 고유식별자 체크
    public void sendDataForUniqueIDcheck() {

        String serverUrl=IP_ADDRESS + "/everyLive/login_register/uniqueIDcheck.php";

        //파일 전송 요청 객체 생성[결과를 String으로 받음]
        SimpleMultiPartRequest smpr= new SimpleMultiPartRequest(Request.Method.POST, serverUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
                        "userInfo", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    Log.d("SimpleMultiPartRequest", response);

                    if(jsonObject.getString("success").equals("true")) {
                        editor.putString("idx_user", jsonObject.getString("idx_user"));
                        editor.putString("snsType", jsonObject.getString("snsType"));
                        editor.putString("nickName", jsonObject.getString("nickName"));
                        editor.putString("profileIMG", jsonObject.getString("profileIMG"));
                        editor.putString("gender", jsonObject.getString("gender"));
                        editor.putString("birthday", jsonObject.getString("birthday"));
                        editor.putString("uniqueID", jsonObject.getString("uniqueID"));
                        editor.apply(); // 있어야 값 저장,삭제됨

                        Intent intent2 = new Intent(getApplicationContext(), Activity_Home.class);
                        startActivity(intent2);
                        finish();
                    }else{
                        Intent intent = new Intent(getApplicationContext(), Activity_Register.class);

                        intent.putExtra("birthday",birthday);
                        intent.putExtra("gender",kakaogender);
                        intent.putExtra("profileimgURL",profileIMG);
                        intent.putExtra("nickName",nickName);
                        intent.putExtra("snsType",snsType);
                        intent.putExtra("uniqueID",uniqueID);
                        intent.putExtra("birthday",birthday);
                        intent.putExtra("gender",gender);
                        startActivity(intent);
                    }

                    Log.d("SimpleMultiPartRequest", response);

                } catch (JSONException e) {
                    e.printStackTrace();
                }



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



    // 서버에 이미지 올라가는지 확인하기 위해 잠깐 사용
    public void sendData() {
            //안드로이드에서 보낼 데이터를 받을 php 서버 주소
            String serverUrl=IP_ADDRESS + "/everyLive/login_register/fileup.php";

            //파일 전송 요청 객체 생성[결과를 String으로 받음]
            SimpleMultiPartRequest smpr= new SimpleMultiPartRequest(Request.Method.POST, serverUrl, new Response.Listener<String>() {
    @Override
    public void onResponse(String response) {
//        new AlertDialog.Builder(Activity_Login.this).setMessage("응답:"+response).create().show();

            }
            }, new Response.ErrorListener() {
    @Override
    public void onErrorResponse(VolleyError error) {
    //                Toast.makeText(Createband_2.this, "ERROR", Toast.LENGTH_SHORT).show();
            }
            });

            //요청 객체에 보낼 데이터를 추가
            //이미지 파일 추가
            smpr.addFile("imgsss", imgPath);

            //요청객체를 서버로 보낼 우체통 같은 객체 생성
            RequestQueue requestQueue= Volley.newRequestQueue(this);
            requestQueue.add(smpr);

            }





}


