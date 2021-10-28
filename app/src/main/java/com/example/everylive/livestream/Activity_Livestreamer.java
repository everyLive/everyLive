package com.example.everylive.livestream;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.TextKeyListener;
import android.util.Log;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;
import com.android.volley.toolbox.Volley;
import com.example.everylive.R;
import com.pedro.rtmp.utils.ConnectCheckerRtmp;
import com.pedro.rtplibrary.rtmp.RtmpCamera1;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Activity_Livestreamer extends AppCompatActivity
        implements ConnectCheckerRtmp, SurfaceHolder.Callback {

    private static final String TAG = "방송화면";
    RtmpCamera1 rtmpCamera1;
    Button btn_send;
    ImageView settings;
    SurfaceView surfaceView;
    TextView title, participants;
    EditText sendMSG;

    // 서버 주소
    private static String IP_ADDRESS = "http://3.36.159.193";

    // 현재날짜, 시간 나타내기
    long mNow;
    Date mDate,mTime;
    SimpleDateFormat mFormat1 = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat mFormat2 = new SimpleDateFormat("HH:mm:ss");

    // 서비스와 데이터 주고받기 위해 사용
    private Messenger mServiceMessenger = null;
    private boolean mIsBound;

    // 채팅 리싸이클러뷰에 사용
    RecyclerView recyclerView;
    private com.example.everylive.livestream.Activity_Chat_Adapter adapter = new com.example.everylive.livestream.Activity_Chat_Adapter(null); // 어댑터클래스를 연결시킴.
    ArrayList<com.example.everylive.livestream.Activity_Chat_Items> items = new ArrayList<>(); // 아이템클래스를 연결시킴
    String whois, profileIMGurl, nickName, content;

    // 서버에서 가져온 유저정보
    String idx_user, nickName_user, profileIMG_user;

    // 방정보 수정 누르면 보이는 뷰들
    TextView textView2, textView6, cnt_title1, cnt_title2,textView3, cnt_contents1, cnt_contents2;
    Spinner spinner_category;
    EditText updatetitle2, updatecontents;
    Button btn_update;
    View view;
    // 방정보 수정에 필요
    String category, nowtitle, aftertitle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_livestreamer);

        settings = findViewById(R.id.settings);
        surfaceView = findViewById(R.id.surfaceView);
        title = findViewById(R.id.updatetitle);
        participants = findViewById(R.id.participants);
        sendMSG = findViewById(R.id.sendMSG);
        btn_send = findViewById(R.id.btn_send);
        recyclerView = findViewById(R.id.streamerview);

        rtmpCamera1 = new RtmpCamera1(surfaceView, this);
        rtmpCamera1.setReTries(10);
        surfaceView.getHolder().addCallback(this);


        // 방정보 수정에 필요함
        textView2 = findViewById(R.id.textView2);
        textView6 = findViewById(R.id.textView6);
        cnt_title1 = findViewById(R.id.cnt_title1);
        cnt_title2 = findViewById(R.id.cnt_title2);
        textView3 = findViewById(R.id.textView3);
        cnt_contents1 = findViewById(R.id.cnt_contents1);
        cnt_contents2 = findViewById(R.id.cnt_contents2);
        spinner_category = findViewById(R.id.spinner_category);
        updatetitle2 = findViewById(R.id.updatetitle2);
        updatecontents = findViewById(R.id.updatecontents);
        btn_update = findViewById(R.id.btn_update);
        view = findViewById(R.id.view);


        // 서버로부터 유저 정보 가져오기
        getuserData();

        // 서버에서 방 정보를 가져와 방 제목을 화면에 띄워준다
        getDatafromServer();

        // 뷰의 모습 -> 리니어
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // 리사이클러뷰에 adapter 객체 지정.
        recyclerView.setAdapter(adapter);

        // 방송 송출을 바로하면 카메라가 안켜져서 2초 뒤에 동작하도록 조치함.
        Handler hd = new Handler();
        hd.postDelayed(new liveonHandler(),2000);


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
        updatetitle2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String msg = updatetitle2.getText().toString();
                cnt_title1.setText(String.valueOf(msg.length()));
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
                cnt_contents1.setText(String.valueOf(msg.length()));
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        // 톱니이미지 누르면 뜨는 팝업메뉴.
        // 화면전환, 방 정보 수정, 방송종료 선택 가능
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup= new PopupMenu(getApplicationContext(), v);

                getMenuInflater().inflate(R.menu.setting_livestreming, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch(item.getItemId()){
                            case R.id.liveturncamara:
                                // 카메라 화면 전환
                                Log.d(TAG, "화면전환: ");
                                rtmpCamera1.switchCamera();

                                break;

                            case R.id.livesetting:
                                // intent로 정보수정화면 다이얼로그

//                                Intent intent = new Intent(getApplicationContext(), Activity_Liveupdateinfo.class);
//                                startActivity(intent);

                                view.setVisibility(View.VISIBLE);
                                textView2.setVisibility(View.VISIBLE);
                                textView6.setVisibility(View.VISIBLE);
                                cnt_title1.setVisibility(View.VISIBLE);
                                cnt_title2.setVisibility(View.VISIBLE);
                                textView3.setVisibility(View.VISIBLE);
                                cnt_contents1.setVisibility(View.VISIBLE);
                                cnt_contents2.setVisibility(View.VISIBLE);
                                spinner_category.setVisibility(View.VISIBLE);
                                updatetitle2.setVisibility(View.VISIBLE);
                                updatecontents.setVisibility(View.VISIBLE);
                                btn_update.setVisibility(View.VISIBLE);

                                // 기존 정보 불러와서 카테고리, 제목, 인삿말에 넣어준다.
                                getDatafromDB();



                                break;

                            case R.id.liveend:
                                // 방송종료.
                                // 총방송시간, 얻은 코인수를 보여주는 다이얼로그 화면 띄워주기

//                                JSONObject jso = new JSONObject();
//                                try {
//                                    jso.put("idx_user", idx_user);
//                                    jso.put("whois","exit");
//
//                                    // 시청자 화면도 같이 종료시키고, 홈화면 리싸이클러뷰 갱신
//                                    sendMessageToService(jso.toString());
//
//
//                                } catch (JSONException e) {
//                                    e.printStackTrace();
//                                }


                                // 방송종료시, 종료시간 DB저장
                                endLivestreaming();



                                break;
                        }
                        return false;
                    }
                });

                popup.show();//Popup Menu 보이기
            }
        });

        // 수정하기 버튼 누르면 DB에 수정한 값 들어가고, 액티비티 닫힌다.
        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateData(); // 본인액티비티의 방제목 변경

                view.setVisibility(View.INVISIBLE);
                textView2.setVisibility(View.INVISIBLE);
                textView6.setVisibility(View.INVISIBLE);
                cnt_title1.setVisibility(View.INVISIBLE);
                cnt_title2.setVisibility(View.INVISIBLE);
                textView3.setVisibility(View.INVISIBLE);
                cnt_contents1.setVisibility(View.INVISIBLE);
                cnt_contents2.setVisibility(View.INVISIBLE);
                spinner_category.setVisibility(View.INVISIBLE);
                updatetitle2.setVisibility(View.INVISIBLE);
                updatecontents.setVisibility(View.INVISIBLE);
                btn_update.setVisibility(View.INVISIBLE);

            }
        });


        // 채팅 전송하면 서비스-소켓서버-서비스-액티비티 돌아와서 리싸이클러뷰에 보여줌
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("idx_user", idx_user);
                    jsonObject.put("nickName_user", nickName_user);
                    jsonObject.put("profileIMG_user", profileIMG_user);
                    jsonObject.put("content",sendMSG.getText().toString());
                    jsonObject.put("whois","Bj");

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                sendMessageToService(jsonObject.toString());

                // 텍스트 입력창 초기화 시키지 않으면 이전 텍스트 남아 있음.
                if (sendMSG.length() > 0) {
                    TextKeyListener.clear(sendMSG.getText());
                }

            }
        });


        setbindService();

    } // 온크리에이트


    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");
    } // 온스타트


    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");

// 방정보 수정하고 다시 되돌아오면 수정된 방제목이 화면에 보이도록 함
//                getDatafromServer();


    } // 온리쥼

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ");
    } // 온스탑




    //현재시간 나타내는 메소드(생명주기 밖에 위치)
    private String getDate(){
        mNow = System.currentTimeMillis();
        mDate = new Date(mNow);
        return mFormat1.format(mDate);
    }
    private String getTime(){
        mNow = System.currentTimeMillis();
        mTime = new Date(mNow);
        return mFormat2.format(mTime);
    }

    // 서비스 바인드하기
    private void setbindService() {
        // startService(new Intent(MainActivity.this, Service.class)); // 서비스 시작
        bindService(new Intent(this, com.example.everylive.livestream.Activity_ChatService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }
    // 서비스 중지
    private void setStopService() {
        if (mIsBound) {
            unbindService(mConnection);
            mIsBound = false;
        }
// stopService(new Intent(Fm1_chat_together2.this, Fm1_chat_service.class)); // 서비스 중지
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


                    try {
                        JSONObject jsonObject = new JSONObject(value2);

                        if(jsonObject.getString("whois").equals("enter")){
                            whois = jsonObject.getString("whois");
                            // 입장이면 인원수 +1
//                            viewerplusminus(whois);

                        }else if(jsonObject.getString("whois").equals("exit")){
                            whois = jsonObject.getString("whois");

                            // 퇴장이면 인원수 -1
//                            viewerplusminus(whois);

                            adapter.notifyDataSetChanged(); // 변경사항 반영 메소드(추가되었으니 리사이클러뷰 새로고침)
                            adapter.setItems(items);

                        }else if(jsonObject.getString("whois").equals("giftcoin")){
                            whois = jsonObject.getString("whois");

                            // 코인 선물오면 뷰에 보여주고, DB 값 업데이트 한다.

                        }else if(jsonObject.getString("whois").equals("Bj")){
                            whois = jsonObject.getString("whois");
                            profileIMGurl = jsonObject.getString("profileIMG_user");
                            nickName = jsonObject.getString("nickName_user");
                            content = jsonObject.getString("content");

                            com.example.everylive.livestream.Activity_Chat_Items itle = new com.example.everylive.livestream.Activity_Chat_Items(whois, profileIMGurl, nickName, content); // 객체를 생성하고
                            items.add(itle); // 아이템에 추가시킨다.
                            adapter.notifyDataSetChanged(); // 변경사항 반영 메소드(추가되었으니 리사이클러뷰 새로고침)
                            adapter.setItems(items);
                            scrollDown();

                        }else if(jsonObject.getString("whois").equals("viewer")){
                            whois = jsonObject.getString("whois");

                            profileIMGurl = jsonObject.getString("profileIMG_user");
                            nickName = jsonObject.getString("nickName_user");
                            content = jsonObject.getString("content");

                            com.example.everylive.livestream.Activity_Chat_Items itle = new com.example.everylive.livestream.Activity_Chat_Items(whois, profileIMGurl, nickName, content); // 객체를 생성하고
                            items.add(itle); // 아이템에 추가시킨다.
                            adapter.notifyDataSetChanged(); // 변경사항 반영 메소드(추가되었으니 리사이클러뷰 새로고침)
                            adapter.setItems(items);
                            scrollDown();

                        }else if(jsonObject.getString("whois").equals("updateroominfo")){

                            title.setText(jsonObject.getString("newtitle"));

                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
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

    public void scrollDown(){
        recyclerView.post(new Runnable() {
            @Override
            public void run() {
                recyclerView.scrollToPosition(items.size()-1);
            }
        });
    }


    // 카메라가 보는 화면 서버로 전송
    private class liveonHandler implements Runnable{

        public void run(){

            if(rtmpCamera1.prepareAudio() && rtmpCamera1.prepareVideo()){
                Log.d(TAG, "카메라 준비 성공: ");
//            Toast.makeText(Activity_Livestreamer.this,"카메라 준비 성공", Toast.LENGTH_LONG).show();
                rtmpCamera1.startStream("rtmp://3.36.159.193:1935/live/streamkey");
                Log.d(TAG, "startStream : ");

            } else {
                Log.d(TAG, "카메라 준비 실패: ");
                Toast.makeText(Activity_Livestreamer.this,"카메라 준비 실패", Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    public void onConnectionStartedRtmp(String rtmpUrl) {
        Log.d(TAG, "onConnectionStartedRtmp: ");
    }

    @Override
    public void onConnectionSuccessRtmp() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onConnectionSuccessRtmp: ");
            }
        });
    }

    @Override
    public void onConnectionFailedRtmp(final String reason) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onConnectionFailedRtmp: ");

                if (rtmpCamera1.reTry(5000, reason)) {
                    Toast.makeText(Activity_Livestreamer.this, "Retry", Toast.LENGTH_SHORT)
                            .show();
                } else {
                    Toast.makeText(Activity_Livestreamer.this, "Connection failed. " + reason, Toast.LENGTH_SHORT)
                            .show();
                    rtmpCamera1.stopStream();
                }
            }
        });
    }

    @Override
    public void onNewBitrateRtmp(long bitrate) {
        Log.d(TAG, "onNewBitrateRtmp: ");

    }

    @Override
    public void onDisconnectRtmp() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onDisconnectRtmp: ");

                Toast.makeText(Activity_Livestreamer.this, "방송 종료", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onAuthErrorRtmp() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onAuthErrorRtmp: ");
                Toast.makeText(Activity_Livestreamer.this, "Auth error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onAuthSuccessRtmp() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onAuthSuccessRtmp: ");
                Toast.makeText(Activity_Livestreamer.this, "Auth success", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Log.d(TAG, "surfaceCreated: ");

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        rtmpCamera1.startPreview();
        Log.d(TAG, "surfaceChanged: ");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if (rtmpCamera1.isStreaming()) {
            rtmpCamera1.stopStream();
            Log.d(TAG, "surfaceDestroyed: ");
        }
        rtmpCamera1.stopPreview();
    }



    // 서버 데이터 가져와서 방송화면에 적용하기
    public void getDatafromServer() {
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences( // 쉐어드
                "userInfo", Context.MODE_PRIVATE);

        //서버로 보낼 데이터
        String idx_user = sharedPref.getString("idx_user",null);

        String serverUrl=IP_ADDRESS + "/everyLive/livestream/liveon.php";

        //파일 전송 요청 객체 생성[결과를 String으로 받음]
        SimpleMultiPartRequest smpr= new SimpleMultiPartRequest(Request.Method.POST, serverUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jsonObject = new JSONObject(response);

                    title.setText(jsonObject.getString("titleLive"));
                    participants.setText("시청자 : "+jsonObject.getString("participants")+"명");
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

    // 서버 데이터 가져와서 방송화면에 적용하기
    public void endLivestreaming() {
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences( // 쉐어드
                "userInfo", Context.MODE_PRIVATE);

        // 방송 종료시간
        String endDate = getDate();
        String endTime = getTime();

        //서버로 보낼 데이터
        String idx_user = sharedPref.getString("idx_user",null);

        String serverUrl=IP_ADDRESS + "/everyLive/livestream/liveend.php";

        //파일 전송 요청 객체 생성[결과를 String으로 받음]
        SimpleMultiPartRequest smpr= new SimpleMultiPartRequest(Request.Method.POST, serverUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                // 여기서 어럴트 다이얼로그를 띄워서 방송시간과 받은 코인 수를 표시해준다.

                try {
                    JSONObject jsonObject = new JSONObject(response);

                    String totaltime = jsonObject.getString("totalTime");
                    String totalcoin = jsonObject.getString("totalCoin");

                    /**
                     * 인텐트로 액티비티 이동 및 데이터 송신
                     */
                    Intent intent = new Intent(getApplicationContext(), com.example.everylive.livestream.Activity_Liveendinfo.class);
                    intent.putExtra("totaltime",totaltime);
                    intent.putExtra("totalcoin",totalcoin);
                    startActivity(intent);

                    // 방송 들은 사람에게 날려준다.
                    JSONObject jso = new JSONObject();
                    try {
                        jso.put("idx_user", idx_user);
                        jso.put("whois","exit");

                        // 시청자 화면도 같이 종료시키고, 홈화면 리싸이클러뷰 갱신
                        sendMessageToService(jso.toString());


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }



                } catch (JSONException e) {
                    e.printStackTrace();
                }


                finish();



            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        //요청 객체에 보낼 데이터를 추가
        smpr.addStringParam("title", title.getText().toString());
        smpr.addStringParam("idx_user", idx_user);
        smpr.addStringParam("endDate", endDate);
        smpr.addStringParam("endTime", endTime);

        //요청객체를 서버로 보낼 우체통 같은 객체 생성
        RequestQueue requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(smpr);
    }


    // 유저 데이터 가져오기
    public void getuserData() {
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences( // 쉐어드
                "userInfo", Context.MODE_PRIVATE);


        //서버로 보낼 데이터
        String idx_userdate = sharedPref.getString("idx_user",null);

        String serverUrl=IP_ADDRESS + "/everyLive/livestream/getuserdata.php";

        //파일 전송 요청 객체 생성[결과를 String으로 받음]
        SimpleMultiPartRequest smpr= new SimpleMultiPartRequest(Request.Method.POST, serverUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                // 여기서 어럴트 다이얼로그를 띄워서 방송시간과 받은 코인 수를 표시해준다.

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    idx_user = jsonObject.getString("idx_user");
                    nickName_user = jsonObject.getString("nickName");
                    profileIMG_user = jsonObject.getString("profileIMG");

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
        smpr.addStringParam("idx_user", idx_userdate);

        //요청객체를 서버로 보낼 우체통 같은 객체 생성
        RequestQueue requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(smpr);
    }

    // 방송제목과 시청자 수를 서버에서 가져와 그려준다.
    public void viewerplusminus(String who) {

        String serverUrl=IP_ADDRESS + "/everyLive/livestream/viewerplusminus.php";


        //파일 전송 요청 객체 생성[결과를 String으로 받음]
        SimpleMultiPartRequest smpr= new SimpleMultiPartRequest(Request.Method.POST, serverUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {


                try {
                    JSONObject jsonObject = new JSONObject(response);

                    title.setText(jsonObject.getString("titleLive"));
                    participants.setText("시청자 : "+jsonObject.getString("participants")+"명");

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
        smpr.addStringParam("idx_room", idx_user);
        smpr.addStringParam("plusminus", who);

        //요청객체를 서버로 보낼 우체통 같은 객체 생성
        RequestQueue requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(smpr);
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

                    updatetitle2.setText(jsonObject.getString("titleLive"));
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
        smpr.addStringParam("title", updatetitle2.getText().toString());
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