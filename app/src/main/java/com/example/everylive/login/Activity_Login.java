package com.example.everylive.login;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.everylive.R;
import com.kakao.sdk.auth.model.OAuthToken;
import com.kakao.sdk.common.KakaoSdk;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.Account;
import com.kakao.sdk.user.model.Gender;
import com.kakao.sdk.user.model.Profile;
import com.nhn.android.naverlogin.OAuthLogin;
import com.nhn.android.naverlogin.OAuthLoginHandler;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btn_KakaoLogin = findViewById(R.id.btn_KakaoLogin);
        btn_NaverLogin = findViewById(R.id.btn_NaverLogin);



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

                            Log.i("LoginData","accessToken : "+ accessToken);
                            Log.i("LoginData","refreshToken : "+ refreshToken);
                            Log.i("LoginData","expiresAt : "+ expiresAt);
                            Log.i("LoginData","tokenType : "+ tokenType);

                            new RequestApiTask(mContext, mOAuthLoginModule).execute();

                            Intent intent = new Intent(getApplicationContext(), Activity_Login_Register.class);
                            startActivity(intent);

                        } else {
                            String errorCode = mOAuthLoginModule
                                    .getLastErrorCode(mContext).getCode();
                            String errorDesc = mOAuthLoginModule.getLastErrorDesc(mContext);
                            Toast.makeText(getApplicationContext(), "errorCode:" + errorCode
                                    + ", errorDesc:" + errorDesc, Toast.LENGTH_SHORT).show();
                        }
                    };
                };

                // 로그인 시도
                mOAuthLoginModule.startOauthLoginActivity(Activity_Login.this, mOAuthLoginHandler);

            }
        });


        Button DeleteToken = (Button)findViewById(R.id.DeleteToken);
        // 네이버 연동해제(클라이언트 및 서버의 토큰 사라짐)
        DeleteToken .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DeleteTokenTask().execute();
                Toast.makeText(Activity_Login.this, "연동해제 되었습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        Button logoutnaver = (Button)findViewById(R.id.logoutnaver);
        // 네이버 로그아웃(클라이언트에 저장된 토큰 사라짐
        logoutnaver .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOAuthLoginModule.logout(mContext);
                Toast.makeText(Activity_Login.this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
            }
        });
        /**
         * 네이버 아이디로 로그인
         * */


    } // 온크리에이트


    /**
     * 네이버 아이디로 로그인
     * */
    // 네이버 로그인 토큰 연동해제
    private class DeleteTokenTask extends AsyncTask<Void, Void, Void> {
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
                    String nickName = response.getString("nickname");
                    String profileIMG = response.getString("profile_image");
                    String gender = response.getString("gender");
                    String birthday = response.getString("birthday");
//                    Toast.makeText(mContext, "email : "+email +" name : "+name+" birthyear : "+birthyear, Toast.LENGTH_LONG).show();
                    Log.d("사용자정보-닉네임", nickName);
                    Log.d("사용자정보-프로필", profileIMG);
                    Log.d("사용자정보-생일", birthday);
                    Log.d("사용자정보-성별", gender);

                    SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
                            "ITSME", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();

                    editor.putString("snsType","naver");
                    editor.putString("nickName",nickName);
                    editor.putString("profileIMG",profileIMG);
                    editor.putString("birthday",birthday);
                    if(gender.equals("M")){
                        editor.putString("gender","남");
                    }else{
                        editor.putString("gender","여");
                    }
                    editor.apply(); // 있어야 값 저장,삭제됨


                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * 네이버 아이디로 로그인
     * */



    // 키해시 얻기
    private String getKeyHash(){
        try{
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            if(packageInfo == null) return null;
            for(Signature signature: packageInfo.signatures){
                try{
                    MessageDigest md = MessageDigest.getInstance("SHA");
                    md.update(signature.toByteArray());
                    return android.util.Base64.encodeToString(md.digest(), Base64.NO_WRAP);
                }catch (NoSuchAlgorithmException e){
                    Log.w("getKeyHash", "Unable to get MessageDigest. signature="+signature, e);
                }
            }
        }catch(PackageManager.NameNotFoundException e){
            Log.w("getPackageInfo", "Unable to getPackageInfo");
        }
        return null;
    }

    // 사용자 정보 요청
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
            Toast.makeText(this, "카카오 로그인이 정상적으로 수행됐습니다.", Toast.LENGTH_SHORT).show();
            updateKakaoLogin();
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

                String nickName = profile.getNickname();
                String profileIMG = profile.getProfileImageUrl(); // 프로필 사진 640*640
                String thumbnail_image = profile.getThumbnailImageUrl(); // 프로필 사진 110*110

                String birthday = kakaoAccount.getBirthday();
                Gender gender = kakaoAccount.getGender();

                Log.i("사용자정보-닉네임", nickName);
                Log.i("사용자정보-프로필", profileIMG);
                Log.i("사용자정보-생일", birthday);
                Log.i("사용자정보-성별", gender.toString());



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





}
