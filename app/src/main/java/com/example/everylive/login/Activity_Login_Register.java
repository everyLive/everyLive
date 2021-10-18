package com.example.everylive.login;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.everylive.R;
import com.example.everylive.home.Activity_Home;

import java.util.ArrayList;

public class Activity_Login_Register extends AppCompatActivity {

    private static final String TAG = "로그인-회원가입";

    ImageView profileIMG;
    TextView nickname;
    Context mContext;
    String nickName, profileimgURL, birthday, gender;
    RadioGroup radioGroup;
    RadioButton select_man, select_woman;
    Button btn_Register;
//
    // 서버 주소
    private static String IP_ADDRESS = "3.36.159.193";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_agree);

        profileIMG = findViewById(R.id.profileIMG);
        nickname = findViewById(R.id.nickname);
        select_man = findViewById(R.id.select_man);
        select_woman = findViewById(R.id.select_woman);
        radioGroup = findViewById(R.id.radio_group);
        btn_Register = findViewById(R.id.btn_Register);


        /***
         * @auter 김태희
         * @brief 회원가입 버튼
         * 회원정보가 DB에 저장이되며 메인홈으로 이동한다
         */
        btn_Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendData(); // 회원정보 DB에 저장
//            Intent intent = new Intent(getApplicationContext(), Activity_Home.class);
//            startActivity(intent);

            }
        });





        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
                "ITSME", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        nickName = sharedPref.getString("nickName","defValue");
        profileimgURL = sharedPref.getString("profileIMG","defValue");
        birthday = sharedPref.getString("birthday","defValue");
        gender = sharedPref.getString("gender","defValue");


        /***
         * @auter 김태희
         * @brief 프로필 이미지
         * sns로그인 할때 프로필 사진 정보제공에 체크했다면 이미지를 띄워줌
         */
        Glide.with(this) // with() : View, Fragment 혹은 Activity로부터 Context를 가져온다.
                .load(profileimgURL) // load() :  이미지를 로드한다. 다양한 방법으로 이미지를 불러올 수 있다. (Bitmap, Drawable, String, Uri, File, ResourId(Int), ByteArray)
                .override(400, 400) // 이미지 사이즈 조절
//                   .error(R.drawable.) // error() : 리소스를 불러오다가 에러가 발생했을 때 보여줄 이미지를 설정한다.
//                   .fallback(R.drawable.) // fallback() : load할 url이 null인 경우 등 비어있을 때 보여줄 이미지를 설정한다.
                .into(profileIMG); // into() : 이미지를 보여줄 View를 지정한다.
        // 위는 기본. 아래는 추가.

        nickname.setText(nickName);


        /***
         * @auter 김태희
         * @brief 생년월일
         * 이용자의 생년월일을 DB에 저장시키기 위함
         */
        Spinner yearSpinner = (Spinner)findViewById(R.id.spinner_year);

        // 문자열 배열과 기본 스피너 레이아웃을 사용하여 ArrayAdapter 만들기
        ArrayAdapter yearAdapter = ArrayAdapter.createFromResource(this,
                R.array.date_year, android.R.layout.simple_spinner_item);
        // 선택목록이 나타날때 사용할 레이아웃을 지정
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // 스피너에 어댑터 적용
        yearSpinner.setAdapter(yearAdapter);
        // 스피너 선택값 가져온다
        String yspin = yearSpinner.getSelectedItem().toString();



        Spinner monthSpinner = (Spinner)findViewById(R.id.spinner_month);
        // 문자열 배열과 기본 스피너 레이아웃을 사용하여 ArrayAdapter 만들기
        ArrayAdapter monthAdapter = ArrayAdapter.createFromResource(this,
                R.array.date_month, android.R.layout.simple_spinner_item);
        // 선택목록이 나타날때 사용할 레이아웃을 지정
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // 스피너에 어댑터 적용
        monthSpinner.setAdapter(monthAdapter);
        // 스피너 선택값 가져온다
        String mspin = monthSpinner.getSelectedItem().toString();



        Spinner daySpinner = (Spinner)findViewById(R.id.spinner_day);
        // 문자열 배열과 기본 스피너 레이아웃을 사용하여 ArrayAdapter 만들기
        ArrayAdapter dayAdapter = ArrayAdapter.createFromResource(this,
                R.array.date_day, android.R.layout.simple_spinner_item);
        // 선택목록이 나타날때 사용할 레이아웃을 지정
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // 스피너에 어댑터 적용
        daySpinner.setAdapter(dayAdapter);
        // 스피너 선택값 가져온다
        String dspin = daySpinner.getSelectedItem().toString();

//       daySpinner.setSelection(2); // 스피너에 특정값을 초기값으로 고정시킬 수 있다
//       daySpinner.getSelectedItem().toString(); // 스피너 선택값 가져온다

        editor.putString("birthday2",yspin+"-"+mspin+"-"+dspin);
        editor.apply();


        // sns 로그인할때 정보제공 동의에 따라 남자, 여자 버튼 체크해줌
        if(gender.equals("남")){
            select_man.setChecked(true);
//           select_woman.setChecked(false);
        }else{
            select_man.setChecked(false);
//           select_woman.setChecked(true);
        }


        // 성별 체크한 값을 DB에 저장시키고자 함
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.select_man:
                        editor.putString("gender","남"); // 값 저장하기
                        break;
                    case R.id.select_woman:
                        editor.putString("gender","여"); // 값 저장하기
                        break;
                }
                editor.apply(); // 있어야 값 저장,삭제됨

            }
        });



    } // 온크리에이트



    public void sendData() {
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences( // 쉐어드
                "ITSME", Context.MODE_PRIVATE);

        //서버로 보낼 데이터
        String profileIMG = sharedPref.getString("profileIMG","defValue");
        String birthday = sharedPref.getString("birthday2","defValue");
        String gender= sharedPref.getString("gender","defValue");
        String nickName = sharedPref.getString("nickName","defValue");
        String snsType = sharedPref.getString("snsType","defValue");

        // 안드로이드에서 보낼 데이터를 받을 php 서버 주소
        String serverUrl="http://" + IP_ADDRESS + "/Register.php";


        //파일 전송 요청 객체 생성[결과를 String으로 받음]
        SimpleMultiPartRequest smpr= new SimpleMultiPartRequest(Request.Method.POST, serverUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                new AlertDialog.Builder(Activity_Login_Register.this).setMessage("응답:"+response).create().show();
//            Toast.makeText(Activity_Login_Register.this,response, Toast.LENGTH_LONG).show();

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(Activity_Login_Register.this, "ERROR", Toast.LENGTH_SHORT).show();
            }
        });

        //요청 객체에 보낼 데이터를 추가
        smpr.addStringParam("profileIMG", profileIMG);
        smpr.addStringParam("birthday", birthday);
        smpr.addStringParam("gender", gender);
        smpr.addStringParam("nickName", nickName);
        smpr.addStringParam("snsType", snsType);

//            //이미지 파일 추가
//            smpr.addFile("img", imgPath);

        //요청객체를 서버로 보낼 우체통 같은 객체 생성
        RequestQueue requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(smpr);

    }

}
