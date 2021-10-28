package com.example.everylive.livestream;
import android.Manifest;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.loader.content.CursorLoader;

import android.content.Context;
import android.content.SharedPreferences;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.everylive.R;

import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;

public class Activity_Liveinsertinfo extends AppCompatActivity {

    private static final String TAG = "방송준비화면";

    CircleImageView select_IMG;
    Spinner spinner_category;
    EditText title, contents;
    TextView cnt_title, cnt_contents;
    Button btn_start;

    String category, imgPath;
    String startDate, startTime;

    // 현재날짜, 시간 나타내기
    long mNow;
    Date mDate,mTime;
    SimpleDateFormat mFormat1 = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat mFormat2 = new SimpleDateFormat("HH:mm:ss");

    // 서버 주소
    private static String IP_ADDRESS = "http://3.36.159.193";

    // 서비스와 데이터 주고받기 위해 사용
    private Messenger mServiceMessenger = null;
    private boolean mIsBound;


   @Override
   protected void onCreate(Bundle savedInstanceState) {
           super.onCreate(savedInstanceState);
           setContentView(R.layout.activity_liveinsertinfo);

       select_IMG = findViewById(R.id.select_IMG);
       btn_start = findViewById(R.id.btn_start);
       spinner_category = findViewById(R.id.spinner_category);
       title = findViewById(R.id.updatetitle);
       contents = findViewById(R.id.updatecontents);
       cnt_title = findViewById(R.id.cnt_title);
       cnt_contents = findViewById(R.id.cnt_contents);

       setbindService();


       // 방송 커버 사진을 앨범에서 선택하게 된다.
       select_IMG.setOnClickListener(new View.OnClickListener() {

           @Override
           public void onClick(View v) {
               openAlbum();

           }
       });



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
       title.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String msg = title.getText().toString();
                cnt_title.setText(String.valueOf(msg.length()));
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });

       // 인삿말에 텍스트 입력할때마다 우측에 글자수 늘어감 표시
       contents.addTextChangedListener(new TextWatcher() {
           @Override
           public void beforeTextChanged(CharSequence s, int start, int count, int after) {

           }

           @Override
           public void onTextChanged(CharSequence s, int start, int before, int count) {
               String msg = contents.getText().toString();
               cnt_contents.setText(String.valueOf(msg.length()));
           }
           @Override
           public void afterTextChanged(Editable s) {

           }
       });



       // 방송 시작하기 버튼을 누르면 방송을 송출하는 화면으로 이동한다
       btn_start.setOnClickListener(new View.OnClickListener() {


           @Override
           public void onClick(View v) {

               // 입력한 정보들을 DB에 저장한다.
               // DB에 저장된 데이터를 이용하여 홈화면 리싸이클러뷰에 목록을 표시할 수 있다.
               sendDatatoServer();

               Intent intent = new Intent(getApplicationContext(), Activity_Livestreamer.class);
               startActivity(intent);

               finish();


               JSONObject jsonObject = new JSONObject();
               try {
                   jsonObject.put("whois","crateroom");

               } catch (JSONException e) {
                   e.printStackTrace();
               }

               sendMessageToService(jsonObject.toString());


           }
       });

   } // 온크리에이트


    // 서비스 바인드하기
    private void setbindService() {
        // startService(new Intent(MainActivity.this, Service.class)); // 서비스 시작
        bindService(new Intent(this, Activity_ChatService.class), mConnection, Context.BIND_AUTO_CREATE);
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
                    Message msg = Message.obtain(null, Activity_ChatService.MSG_SEND_TO_SERVICE, str);
                    msg.replyTo = mMessenger;
                    mServiceMessenger.send(msg);
                } catch (RemoteException e) {
                }
            }
        }
    }



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



    // 권한확인, 앨범열기
    public void openAlbum() {
        String temp = "";
        //파일 읽기 권한 확인
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            temp += Manifest.permission.READ_EXTERNAL_STORAGE + " ";
        } //파일 쓰기 권한 확인
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            temp += Manifest.permission.WRITE_EXTERNAL_STORAGE + " ";

        }if (TextUtils.isEmpty(temp) == false) { // 권한 요청
            ActivityCompat.requestPermissions(this, temp.trim().split(" "),1);

        }else { // 모두 허용 상태

            Intent intent= new Intent(Intent.ACTION_PICK);
//            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // 다중이미지 선택가능.
            intent.setType("image/*"); // 이미지를 볼 수 있음(모든형식)
            startActivityForResult(intent,10);
        }
    }

    // 앨범에서 사진 클릭하여 가져와 뷰에 넣는다.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10) { // 이미지
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData(); // 상대경로
                imgPath= getRealPathFromUri(uri); // (상대경로)이미지 상태를 절대경로로 만들기


                Glide.with(this) // with() : View, Fragment 혹은 Activity로부터 Context를 가져온다.
                        .load(uri) // load() :  이미지를 로드한다. 다양한 방법으로 이미지를 불러올 수 있다. (Bitmap, Drawable, String, Uri, File, ResourId(Int), ByteArray)
                        .override(130, 130) // 이미지 사이즈 조절
                        .centerCrop()
                        .into(select_IMG); // into() : 이미지를 보여줄 View를 지정한다.

            }
        }
    }

    // Uri -- > 절대경로로 바꿔서 리턴시켜주는 메소드(서버에 이미지 저장시킬때 필요)
    String getRealPathFromUri(Uri uri){
        String[] proj= {MediaStore.Images.Media.DATA};
        CursorLoader loader= new CursorLoader(this, uri, proj, null, null, null);
        Cursor cursor= loader.loadInBackground();
        int column_index= cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result= cursor.getString(column_index);
        cursor.close();
        return  result;
    }


    public void sendDatatoServer() { // 온크리에이트때 채팅 내역 불러오기

        String serverUrl=IP_ADDRESS + "/everyLive/livestream/insertinfo.php";

        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
                    "userInfo", Context.MODE_PRIVATE);
        String idx_user = sharedPref.getString("idx_user","defValue"); // 값 불러오기
        String startDate = getDate();
        String startTime = getTime();

        //파일 전송 요청 객체 생성[결과를 String으로 받음]
        SimpleMultiPartRequest smpr= new SimpleMultiPartRequest(Request.Method.POST, serverUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
//                    new AlertDialog.Builder(Fm1_chat_together2.this).setMessage(response).create().show();
//                Log.d("서버에서 가져온 기존 채팅내역", response);


            } // 온리스폰스
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //                Toast.makeText(Createband_2.this, "ERROR", Toast.LENGTH_SHORT).show();
            }
        });

        //요청 객체에 보낼 데이터를 추가
        smpr.addStringParam("category", category);
        smpr.addStringParam("title", title.getText().toString());
        smpr.addStringParam("contents", contents.getText().toString());
        smpr.addStringParam("idx_user", idx_user);
        smpr.addStringParam("startDate", startDate);
        smpr.addStringParam("startTime", startTime);

        smpr.addFile("imgPath", imgPath);

        //요청객체를 서버로 보낼 우체통 같은 객체 생성
        RequestQueue requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(smpr);

    }

}





