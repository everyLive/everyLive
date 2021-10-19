package com.example.everylive.login;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

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

import org.json.JSONException;
import org.json.JSONObject;

public class Activity_Register extends AppCompatActivity {

    private static final String TAG = "회원가입";

    ImageView profileIMG;
    EditText nickname;
    Context mContext;
    String nickName, snsType, profileimgURL, birthday, gender;
    String monthspinner, dayspinner;
    RadioGroup radioGroup;
    RadioButton select_man, select_woman;
    Button btn_Register, btn_nicknamecheck;
    Boolean checkID;

    // 서버 주소
    private static String IP_ADDRESS = "http://3.36.159.193";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

//        // 회원가입하여 idx_user 번호가 있으면 자동 로그인
//        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
//                    "userInfo", Context.MODE_PRIVATE);
//        if(sharedPref.contains("idx_user")){
//
//                    Intent intent = new Intent(getApplicationContext(), Activity_Home.class);
//                    startActivity(intent);
//                    finish();
//
//        }

        // 닉네임 중복체크여부 검사의 디폴트값. true 여야 다음 단계로 넘어갈 수 있다.
        checkID = false;


        profileIMG = findViewById(R.id.profileIMG);
        nickname = findViewById(R.id.nickname);
        select_man = findViewById(R.id.select_man);
        select_woman = findViewById(R.id.select_woman);
        radioGroup = findViewById(R.id.radio_group);
        btn_Register = findViewById(R.id.btn_Register);
        btn_nicknamecheck = findViewById(R.id.btn_nicknamecheck);


        /***
         * @auter 김태희
         * @brief 인텐트 데이터 수신, 회원정보 입력(프로필, 닉네임)
         */

        Intent intent = getIntent();
        snsType = intent.getStringExtra("snsType");
        nickName = intent.getStringExtra("nickName");
        profileimgURL = intent.getStringExtra("profileimgURL");
        birthday = intent.getStringExtra("birthday");
        gender = intent.getStringExtra("gender");

        Log.d(TAG, snsType);
        if(nickName!=null) {
            Log.d(TAG, nickName);
            nickname.setText(nickName);
        }
        if(profileimgURL!=null) {
            Log.d(TAG, profileimgURL);
            Glide.with(this) // with() : View, Fragment 혹은 Activity로부터 Context를 가져온다.
                    .load(profileimgURL) // load() :  이미지를 로드한다. 다양한 방법으로 이미지를 불러올 수 있다. (Bitmap, Drawable, String, Uri, File, ResourId(Int), ByteArray)
                    .override(400, 400) // 이미지 사이즈 조절
                    .centerCrop()
//                   .error(R.drawable.) // error() : 리소스를 불러오다가 에러가 발생했을 때 보여줄 이미지를 설정한다.
//                .fallback(R.drawable.blankprofile) // fallback() : load할 url이 null인 경우 등 비어있을 때 보여줄 이미지를 설정한다.
                    .into(profileIMG); // into() : 이미지를 보여줄 View를 지정한다.
        }
        if(birthday!=null){
            Log.d(TAG, birthday);
        }
        if(gender!=null){
            Log.d(TAG, gender);
        }

        // 성별 남자가 디폴트값
        select_man.setChecked(true);



        /***
         * @auter 김태희
         * @brief 회원가입 버튼
         * 회원정보가 DB에 저장이되며 메인홈으로 이동한다
         */
        btn_Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(checkID==true) {

                    sendDataToServer(); // 회원정보 DB에 저장
                    Intent intent = new Intent(getApplicationContext(), Activity_Home.class);
                    startActivity(intent);
                }else if(checkID==false){
                    Toast.makeText(Activity_Register.this,"닉네임 중복체크 해주세요", Toast.LENGTH_LONG).show();

                    Log.d("checkID", "false");
                }



            }
        });











        /***
         * @auter 김태희
         * @brief 생일
         * 이용자의 생년월일을 DB에 저장시키기 위함
         */

        Spinner monthSpinner = (Spinner)findViewById(R.id.spinner_month);
        // 문자열 배열과 기본 스피너 레이아웃을 사용하여 ArrayAdapter 만들기
        ArrayAdapter monthAdapter = ArrayAdapter.createFromResource(this,
                R.array.date_month, android.R.layout.simple_spinner_item);
        // 선택목록이 나타날때 사용할 레이아웃을 지정
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // 스피너에 어댑터 적용
        monthSpinner.setAdapter(monthAdapter);
        // 스피너 선택값 가져온다
//        String mspin = monthSpinner.getSelectedItem().toString();
//        mspin = mspin.substring(0, mspin.indexOf(" "));
//        if(mspin.length()<2){ // 1자리 숫자면 앞에 0을 붙이게 된다. DB생일 컬럼에 01-01 형식으로 넣어진다.
//            mspin = "0"+mspin;
//            Log.d("몇월?", mspin);
//        }
        monthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override   // position 으로 몇번째 것이 선택됬는지 값을 넘겨준다
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                monthspinner = monthSpinner.getSelectedItem().toString();
                monthspinner = monthspinner.substring(0, monthspinner.indexOf(" "));
                if(monthspinner.length()<2){ // 1자리 숫자면 앞에 0을 붙이게 된다. DB생일 컬럼에 01-01 형식으로 넣어진다.
                    monthspinner = "0"+monthspinner;
                    Log.d("선택한 월", monthspinner);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });


        Spinner daySpinner = (Spinner)findViewById(R.id.spinner_day);
        // 문자열 배열과 기본 스피너 레이아웃을 사용하여 ArrayAdapter 만들기
        ArrayAdapter dayAdapter = ArrayAdapter.createFromResource(this,
                R.array.date_day, android.R.layout.simple_spinner_item);
        // 선택목록이 나타날때 사용할 레이아웃을 지정
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // 스피너에 어댑터 적용
        daySpinner.setAdapter(dayAdapter);
        // 스피너 선택값 가져온다
//        String dspin = daySpinner.getSelectedItem().toString();
//        dspin = dspin.substring(0, dspin.indexOf(" "));
//        if(dspin.length()<2){ // 1자리 숫자면 앞에 0을 붙이게 된다. DB생일 컬럼에 01-01 형식으로 넣어진다.
//            dspin = "0"+dspin;
//            Log.d("며칠?", dspin);
//        }
        daySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override   // position 으로 몇번째 것이 선택됬는지 값을 넘겨준다
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                dayspinner = daySpinner.getSelectedItem().toString();
                dayspinner = dayspinner.substring(0, dayspinner.indexOf(" "));
                if(dayspinner.length()<2){ // 1자리 숫자면 앞에 0을 붙이게 된다. DB생일 컬럼에 01-01 형식으로 넣어진다.
                    dayspinner = "0"+dayspinner;
                    Log.d("선택한 일", dayspinner);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

//       daySpinner.setSelection(2); // 스피너에 특정값을 초기값으로 고정시킬 수 있다
//       daySpinner.getSelectedItem().toString(); // 스피너 선택값 가져온다

        // 남자 = M, 여자 = F 으로 DB에 저장이 되어야 함
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.select_man:
                        gender = "M";
                        break;
                    case R.id.select_woman:
                        gender = "F";
                        break;
                }

            }
        });


        // 닉네임 중복체크 버튼 클릭하면 HTTP통신해서 서버에 같은 닉네임이 있나 체크한다.
        btn_nicknamecheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                nickName = nickname.getText().toString();
                sendDataForNicknamecheck();

            }
        });




    } // 온크리에이트



    public void sendDataToServer() {


        // 안드로이드에서 보낼 데이터를 받을 php 서버 주소
        String serverUrl=IP_ADDRESS + "/everyLive/login_register/Register.php";


        //파일 전송 요청 객체 생성[결과를 String으로 받음]
        SimpleMultiPartRequest smpr= new SimpleMultiPartRequest(Request.Method.POST, serverUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
//                new AlertDialog.Builder(Activity_Register.this).setMessage("응답:"+response).create().show();
//            Toast.makeText(Activity_Login_Register.this,response, Toast.LENGTH_LONG).show();
                Log.d(TAG, "sendDataToServer(): "+response);

                SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
                        "userInfo", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    editor.putString("idx_user",jsonObject.getString("idx_user"));
                    editor.putString("snsType",jsonObject.getString("snsType"));
                    editor.putString("nickName",jsonObject.getString("nickName"));
                    editor.putString("profileIMG",jsonObject.getString("profileIMG"));
                    editor.putString("gender",jsonObject.getString("gender"));
                    editor.putString("birthday",jsonObject.getString("birthday"));
                    editor.apply(); // 있어야 값 저장,삭제됨
                    finish();
                } catch (JSONException e) {
                    e.printStackTrace();
                }




            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(Activity_Register.this, "ERROR", Toast.LENGTH_SHORT).show();
            }
        });

        //요청 객체에 보낼 데이터를 추가
        smpr.addStringParam("profileIMG", profileimgURL);
        smpr.addStringParam("birthday", monthspinner+"-"+dayspinner);
        smpr.addStringParam("gender", gender);
        smpr.addStringParam("nickName", nickname.getText().toString());
        smpr.addStringParam("snsType", snsType);

//            //이미지 파일 추가
//            smpr.addFile("img", imgPath);

        //요청객체를 서버로 보낼 우체통 같은 객체 생성
        RequestQueue requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(smpr);

    }



    public void sendDataForNicknamecheck() {



        //안드로이드에서 보낼 데이터를 받을 php 서버 주소
        String serverUrl=IP_ADDRESS + "/everyLive/login_register/nicknamecheck.php";
//        http://3.36.159.193/everyLive/login_registernicknamecheck.php

        //파일 전송 요청 객체 생성[결과를 String으로 받음]
        SimpleMultiPartRequest smpr= new SimpleMultiPartRequest(Request.Method.POST, serverUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
//            new AlertDialog.Builder(sss.this).setMessage("응답:"+response).create().show();
//            Toast.makeText(Activity_Register.this,response, Toast.LENGTH_LONG).show();
                Log.d(TAG, "sendDataForNicknamecheck(): "+response);

                // 값이 1 이면 중복, 0 이면 중복x
                if(response.equals("1")){
                    AlertDialog.Builder builder = new AlertDialog.Builder(Activity_Register.this);
                    builder.setTitle("회원가입").setMessage("닉네임이 중복되었습니다\n새로 입력해 주세요.");
                    builder.setPositiveButton("확인", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int id)
                        {
                            nickname.setText(null); // 입력한 닉네임 지움. 중복되었으니까.

                        }
                    });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show(); // 다이얼로그 보여주기
                }else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(Activity_Register.this);
                    builder.setTitle("회원가입").setMessage("사용 가능한 닉네임 입니다");
                    builder.setPositiveButton("확인", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int id)
                        {
                            nickname.setEnabled(false); // 사용가능한 닉네임이면 수정 불가 처리
                            checkID=true;
                        }
                    });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show(); // 다이얼로그 보여주기
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //                Toast.makeText(Createband_2.this, "ERROR", Toast.LENGTH_SHORT).show();
            }
        });

        //요청 객체에 보낼 데이터를 추가
        smpr.addStringParam("nickName", nickName);

        //요청객체를 서버로 보낼 우체통 같은 객체 생성
        RequestQueue requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(smpr);

    }

}
