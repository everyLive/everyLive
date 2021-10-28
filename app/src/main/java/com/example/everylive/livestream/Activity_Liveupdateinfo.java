package com.example.everylive.livestream;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;
import com.android.volley.toolbox.Volley;
import com.example.everylive.R;

import org.json.JSONException;
import org.json.JSONObject;

public class Activity_Liveupdateinfo extends AppCompatActivity {

    // 서버 주소
    private static String IP_ADDRESS = "http://3.36.159.193";

    private static final String TAG = "방송정보변경";

    Spinner spinner_category;
    EditText updatetitle, updatecontents;
    TextView cnt_title, cnt_contents;
    Button btn_update;
    String category, nowtitle;
    String aftertitle;

    // 서비스와 데이터 주고받기 위해 사용
    private Messenger mServiceMessenger = null;
    private boolean mIsBound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liveupdateinfo);

        spinner_category = findViewById(R.id.spinner_category);
        updatetitle = findViewById(R.id.updatetitle);
        updatecontents = findViewById(R.id.updatecontents);
        cnt_title = findViewById(R.id.cnt_title);
        cnt_contents = findViewById(R.id.cnt_contents);
        btn_update = findViewById(R.id.btn_update);


        // 기존 정보 불러와서 카테고리, 제목, 인삿말에 넣어준다.
        getDatafromDB();

        // 라이브방 정보수정하면 시청자화면도 변화주기 위해서 사용
        setbindService();


        // 문자열 배열과 기본 스피너 레이아웃을 사용하여 ArrayAdapter 만들기
        ArrayAdapter livecateAdapter = ArrayAdapter.createFromResource(this,
                R.array.live_category, R.layout.spinner_color);
        // 선택목록이 나타날때 사용할 레이아웃을 지정
        livecateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // 스피너에 어댑터 적용
        spinner_category.setAdapter(livecateAdapter);

        spinner_category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override   // position 으로 몇번째 것이 선택됬는지 값을 넘겨준다
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                category = spinner_category.getSelectedItem().toString();

            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        // 제목에 텍스트 입력할때마다 우측에 글자수 늘어감 표시
        updatetitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String msg = updatetitle.getText().toString();
                cnt_title.setText(String.valueOf(msg.length()));
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // 인삿말에 텍스트 입력할때마다 우측에 글자수 늘어감 표시
        updatecontents.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String msg = updatecontents.getText().toString();
                cnt_contents.setText(String.valueOf(msg.length()));
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    } // 온크리에이트


    @Override
    protected void onResume() {
        super.onResume();

        // 수정하기 버튼 누르면 DB에 수정한 값 들어가고, 액티비티 닫힌다.
        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateData(); // 본인액티비티의 방제목 변경

                finish();


            }
        });

    } // 온리쥼


    // 서비스 바인드하기
    private void setbindService() {
        bindService(new Intent(this, com.example.everylive.livestream.Activity_ChatService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }


    // 액티비티와 서비스 클래스를 IBinder 연결
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d("test","onServiceConnected");
            mServiceMessenger = new Messenger(iBinder);
            try {
                Message msg = Message.obtain(null, com.example.everylive.livestream.Activity_ChatService.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mServiceMessenger.send(msg);
            }
            catch (RemoteException e) {
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };

    /** Service 로 부터 message를 수신하여 처리하는곳
     * 이곳에서 리싸이클러뷰에 데이터를 넣고 띄워준다.
     * */
    private final Messenger mMessenger = new Messenger(new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Log.i("handleMessage","act : what "+msg.what);

            switch (msg.what) {
                case com.example.everylive.livestream.Activity_ChatService.MSG_SEND_TO_ACTIVITY:
                    // 서비스에서 번들로 넘긴 키값을 넣는다.
                    String value2 = msg.getData().getString("sendServiceMsgToActivity");
                    Log.i("서비스에서 넘어온 데이터",value2);


                    break; // case 끝나면 break필수
            }
            return false;
        }
    }));

    /** Service 로 메시지를 송신하는 메서드
     * json 형태로 값을 보내준다.
     * */
    private void sendMessageToService(String str) {
        if (mIsBound) {
            if (mServiceMessenger != null) {
                try {
                    Message msg = Message.obtain(null, com.example.everylive.livestream.Activity_ChatService.MSG_SEND_TO_SERVICE, str);
                    msg.replyTo = mMessenger;
                    mServiceMessenger.send(msg);
                } catch (RemoteException e) {
                }
            }
        }
    }


    // 수정할때 기존 정보 불러와서 띄워준다
    public void getDatafromDB() {
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences( // 쉐어드
                "userInfo", Context.MODE_PRIVATE);

        String idx_user = sharedPref.getString("idx_user","defalut");

        String serverUrl=IP_ADDRESS + "/everyLive/livestream/liveinformation.php";

        SimpleMultiPartRequest smpr= new SimpleMultiPartRequest(Request.Method.POST, serverUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jsonObject = new JSONObject(response);

                    updatetitle.setText(jsonObject.getString("titleLive"));
                    nowtitle = jsonObject.getString("titleLive");
                    updatecontents.setText(jsonObject.getString("contentsLive"));
                    if(jsonObject.getString("categoryLive").equals("일상")){
                        spinner_category.setSelection(0);
                        category = spinner_category.getSelectedItem().toString();
                        Log.d(TAG, category);
                    }else if(jsonObject.getString("categoryLive").equals("노래/연주")){
                        spinner_category.setSelection(1);
                        category = spinner_category.getSelectedItem().toString();
                        Log.d(TAG, category);
                    }else if(jsonObject.getString("categoryLive").equals("수다/챗")){
                        spinner_category.setSelection(2);
                        category = spinner_category.getSelectedItem().toString();
                        Log.d(TAG, category);
                    }else if(jsonObject.getString("categoryLive").equals("기타")){
                        spinner_category.setSelection(3);
                        category = spinner_category.getSelectedItem().toString();
                        Log.d(TAG, category);
                    }



                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        //요청 객체에 보낼 데이터를 추가
        smpr.addStringParam("idx_user", idx_user);

        //요청객체를 서버로 보낼 우체통 같은 객체 생성
        RequestQueue requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(smpr);

    }


    // 수정한 정보를 DB에 넣는다
    public void updateData() {
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences( // 쉐어드
                "userInfo", Context.MODE_PRIVATE);

        String idx_user = sharedPref.getString("idx_user","defalut");

        String serverUrl=IP_ADDRESS + "/everyLive/livestream/updateinfo.php";

        SimpleMultiPartRequest smpr= new SimpleMultiPartRequest(Request.Method.POST, serverUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                afterDatafromupdate(); // 시청자액티비티 방제목 변경

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        //요청 객체에 보낼 데이터를 추가
        smpr.addStringParam("category", category);
        smpr.addStringParam("title", updatetitle.getText().toString());
        smpr.addStringParam("contents", updatecontents.getText().toString());
        smpr.addStringParam("idx_user", idx_user);
        smpr.addStringParam("nowtitle", nowtitle);

        //요청객체를 서버로 보낼 우체통 같은 객체 생성
        RequestQueue requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(smpr);

    }


    // 수정이후의 데이터 가져온다
    public void afterDatafromupdate() {
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences( // 쉐어드
                "userInfo", Context.MODE_PRIVATE);

        String idx_user = sharedPref.getString("idx_user","defalut");

        String serverUrl=IP_ADDRESS + "/everyLive/livestream/liveinformation.php";

        SimpleMultiPartRequest smpr= new SimpleMultiPartRequest(Request.Method.POST, serverUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {


                try {
                    JSONObject jsonObject = new JSONObject(response);
                    aftertitle = jsonObject.getString("titleLive");

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                JSONObject jso = new JSONObject();
                try {
                    jso.put("idx_user", idx_user);
                    jso.put("newtitle",aftertitle);
                    jso.put("whois","updateroominfo");

                    sendMessageToService(jso.toString());

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        //요청 객체에 보낼 데이터를 추가
        smpr.addStringParam("idx_user", idx_user);

        //요청객체를 서버로 보낼 우체통 같은 객체 생성
        RequestQueue requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(smpr);

    }
}