package com.example.everylive.home;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.toolbox.Volley;
import com.example.everylive.R;
import com.example.everylive.livestream.Activity_ChatService;
import com.example.everylive.livestream.Activity_Chat_Items;
import com.example.everylive.livestream.Activity_Liveinsertinfo;
import com.example.everylive.mypage.Activity_My;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class Activity_Home extends AppCompatActivity {


    ImageView btn_mypage;
    FloatingActionButton btn_livestreaming;

    // 방송들이 리싸이클러뷰에 보이도록 한다
    RecyclerView recyclerView;
    private Activity_Adapter adapter = new Activity_Adapter(null); // 어댑터클래스를 연결시킴.
    ArrayList<Activity_Items> items = new ArrayList<>(); // 아이템클래스를 연결시킴

    // 서비스와 데이터 주고받기 위해 사용
    private Messenger mServiceMessenger = null;
    private boolean mIsBound;
    String whois;



    // 서버 주소
    private static String IP_ADDRESS = "http://3.36.159.193";


    // 오디오, 카메라, 내부저장소 권한 확인
    private final String[] PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        btn_mypage = findViewById(R.id.btn_mypage);
        btn_livestreaming = findViewById(R.id.btn_livestreaming);
        recyclerView = findViewById(R.id.recyclerView);


        // 뷰의 모습 -> 리니어
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // 리사이클러뷰에 adapter 객체 지정.
        recyclerView.setAdapter(adapter);

//        // 아이템 불러오기(서버에 저장된 값을 불러온다)
//        adapter.setItems(this.getItems());





        // 버튼 클릭시, Activity_My로 이동.
        btn_mypage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Activity_Home.this, Activity_My.class);
                startActivity(intent);
            }
        });

        // 버튼 클릭시, Activity_Livestartinfo로 이동.
        // Activity_Livestartinfo -> 방송 시작전 방정보입력 화면
        btn_livestreaming.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!hasPermissions(Activity_Home.this, PERMISSIONS)) {
                    ActivityCompat.requestPermissions(Activity_Home.this, PERMISSIONS, 1);

                }else{
                    Intent intent = new Intent(getApplicationContext(), Activity_Liveinsertinfo.class);
                    startActivity(intent);
                }



            }
        });


        // 채팅 서비스 시작하기
        Intent intent = new Intent(
                getApplicationContext(),//현재제어권자
                Activity_ChatService.class); // 이동할 컴포넌트
        Log.d("test", "서비스 시작");
        startService(intent); // 서비스 시작


        // 방송종료에 따라 리싸이클러뷰 갱신
        setbindService();



    } // 온크리에이트




    @Override
    protected void onStart() {
        super.onStart();

//        getDataFromServer();
//        adapter.removeItems(items);
//        //아이템 불러오기(서버에 저장된 값을 불러온다)
        adapter.removeItems();
        adapter.setItems(this.getItems());
//        adapter.setItems(this.getItems2());

//        updateview();

    } // 온스타트

    @Override
    protected void onStop() {
            super.onStop();


    } // 온스탑

    @Override
    protected void onDestroy() {
            super.onDestroy();

        // 채팅 서비스 종료하기
        Intent intent2 = new Intent(
                getApplicationContext(),//현재제어권자
                Activity_ChatService.class); // 이동할 컴포넌트
        stopService(intent2); // 서비스 종료
    } // 온디스트로이


    // 서비스 바인드하기
    private void setbindService() {
        bindService(new Intent(this, Activity_ChatService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }


    // 액티비티와 서비스 클래스를 IBinder 연결
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d("test","onServiceConnected");
            mServiceMessenger = new Messenger(iBinder);
            try {
                Message msg = Message.obtain(null, Activity_ChatService.MSG_REGISTER_CLIENT);
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
                case Activity_ChatService.MSG_SEND_TO_ACTIVITY:
                    // 서비스에서 번들로 넘긴 키값을 넣는다.
                    String value2 = msg.getData().getString("sendServiceMsgToActivity");
                    Log.i("서비스에서 넘어온 데이터",value2);



                    try {
                        JSONObject jsonObject = new JSONObject(value2);

                        if(jsonObject.getString("whois").equals("enter")){
                            whois = jsonObject.getString("whois");

                        }else if(jsonObject.getString("whois").equals("exit")){
                            whois = jsonObject.getString("whois");

                            adapter.removeItems(); // 시연을 위해 야매로 해둠.
//                            adapter.setItems(getItems());

                        }else if(jsonObject.getString("whois").equals("crateroom")){
                            whois = jsonObject.getString("whois");

                            Log.d("동작해야할곳", "handleMessage: ");
//                            adapter.removeItems(items);
//                            adapter.setItems(getItems2());

                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    break; // case 끝나면 break필수
            }
            return false;
        }
    }));

    // 권한 체크
    private boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission)
                        != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }




    // 서버와 통신하여 DB에서 값 가져오기
    public ArrayList<Activity_Items> getItems(){
        getDataFromServer();
        return items;
    }

    // 리싸이클러뷰 업데이트
    public ArrayList<Activity_Items> getItems2(){
        updateview();
        return items;
    }

    // 서버와 통신하여 DB에서 값 가져오기
    public void getDataFromServer() {

        //안드로이드에서 보낼 데이터를 받을 php 서버 주소
        String serverUrl=IP_ADDRESS + "/everyLive/livestream/roomRead.php";

        //파일 전송 요청 객체 생성[결과를 String으로 받음]
        SimpleMultiPartRequest smpr= new SimpleMultiPartRequest(Request.Method.POST, serverUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
//                    new AlertDialog.Builder(Fm1_chat_together2.this).setMessage(response).create().show();
                Log.d("서버에서 가져온 전체 내용", response);

                String nickName, roomIMG,gender,roomcategory,roomtitle,roomcontents,participants; // 아이템클래스로 보낼 변수선언
                String roomIMGurl,idx_user;
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    for (int i = 0; i < jsonArray.length(); i++) { // 순서대로 뽑아서
                        JSONObject item = jsonArray.getJSONObject(i); // json객체


                        if (item.getString("success").equals("true")) {
                            nickName = item.getString("nickName");
                            roomIMG = item.getString("roomIMG");
                            gender = item.getString("gender");
                            roomcategory = item.getString("categoryLive");
                            roomtitle = item.getString("titleLive");
                            roomcontents = item.getString("contentsLive");
                            roomIMGurl = "http://3.36.159.193/everyLive/livestream/roomIMG/" + roomIMG;
                            participants = item.getString("participants");
                            idx_user = item.getString("idx_user");


                            // 아이템 클래스에서 만든 생성자가 메인클래스 에서 값을 전달하기 위해 쓰인다.
                            Activity_Items itle = new Activity_Items(idx_user, nickName, roomIMGurl, gender, roomcategory, roomtitle, roomcontents,participants); // 객체를 생성하고
                            items.add(itle); // 어레이리스트에 추가시킨다.
                            adapter.notifyDataSetChanged(); // 변경사항 반영 메소드(추가되었으니 리사이클러뷰 새로고침)

                        }else if(item.getString("success").equals("false")){
                            Log.d("홈화면", "아무도 방송중이 아님");

                         }
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }
            } // 온리스폰스
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //                Toast.makeText(Createband_2.this, "ERROR", Toast.LENGTH_SHORT).show();
            }
        });

        //요청 객체에 보낼 데이터를 추가
//        smpr.addStringParam("username", username);
//        smpr.addStringParam("chattitle", chattitle);

        //요청객체를 서버로 보낼 우체통 같은 객체 생성
        RequestQueue requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(smpr);

    }

    // 서버와 통신하여 DB에서 값 가져오기
    public void updateview() {

        //안드로이드에서 보낼 데이터를 받을 php 서버 주소
        String serverUrl=IP_ADDRESS + "/everyLive/livestream/updateroomRead.php";

        //파일 전송 요청 객체 생성[결과를 String으로 받음]
        SimpleMultiPartRequest smpr= new SimpleMultiPartRequest(Request.Method.POST, serverUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
//                    new AlertDialog.Builder(Fm1_chat_together2.this).setMessage(response).create().show();
                Log.d("서버에서 가져온 전체 내용", response);

                String nickName, roomIMG,gender,roomcategory,roomtitle,roomcontents,participants; // 아이템클래스로 보낼 변수선언
                String roomIMGurl,idx_user;
                try {
                    JSONArray jsonArray = new JSONArray(response);

                    for (int i = 0; i < jsonArray.length(); i++) { // 순서대로 뽑아서
                        JSONObject item = jsonArray.getJSONObject(i); // json객체


                        if (item.getString("success").equals("true")) {
                            nickName = item.getString("nickName");
                            roomIMG = item.getString("roomIMG");
                            gender = item.getString("gender");
                            roomcategory = item.getString("categoryLive");
                            roomtitle = item.getString("titleLive");
                            roomcontents = item.getString("contentsLive");
                            roomIMGurl = "http://3.36.159.193/everyLive/livestream/roomIMG/" + roomIMG;
                            participants = item.getString("participants");
                            idx_user = item.getString("idx_user");


                            // 아이템 클래스에서 만든 생성자가 메인클래스 에서 값을 전달하기 위해 쓰인다.
                            Activity_Items itle = new Activity_Items(idx_user, nickName, roomIMGurl, gender, roomcategory, roomtitle, roomcontents,participants); // 객체를 생성하고
                            items.clear();
                            items.add(itle); // 어레이리스트에 추가시킨다.
                            adapter.notifyDataSetChanged(); // 변경사항 반영 메소드(추가되었으니 리사이클러뷰 새로고침)

                        }else if(item.getString("success").equals("false")){
                            Log.d("홈화면", "아무도 방송중이 아님");

                        }
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }
            } // 온리스폰스
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //                Toast.makeText(Createband_2.this, "ERROR", Toast.LENGTH_SHORT).show();
            }
        });

        //요청 객체에 보낼 데이터를 추가
//        smpr.addStringParam("username", username);
//        smpr.addStringParam("chattitle", chattitle);

        //요청객체를 서버로 보낼 우체통 같은 객체 생성
        RequestQueue requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(smpr);

    }

}


