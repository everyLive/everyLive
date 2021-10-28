package com.example.everylive.livestream;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.method.TextKeyListener;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Bundle;
import android.widget.ProgressBar;

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
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.rtmp.RtmpDataSourceFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.video.VideoListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Activity_Liveplayer extends AppCompatActivity {

    private static final String TAG = "방송시청";

    // 서버 주소
    private static String IP_ADDRESS = "http://3.36.159.193";

    // 방송 시청하기
    SimpleExoPlayer mSimpleExoPlayer;
    private boolean mPlayWhenReady = true;
    private int mCurrentWindow;
    private long mPlaybackPosition;
    private ComponentListener mComponentListener;
    private ProgressBar mProgressBar;
    PlayerView mPlayerView;
    private static final DefaultBandwidthMeter BANDWIDTH_METER =
            new DefaultBandwidthMeter();

    // 서비스와 데이터 주고받기 위해 사용
    private Messenger mServiceMessenger = null;
    private boolean mIsBound;

    // 채팅 리싸이클러뷰에 사용
    RecyclerView recyclerView;
    private Activity_Chat_Adapter adapter = new Activity_Chat_Adapter(null); // 어댑터클래스를 연결시킴.
    ArrayList<Activity_Chat_Items> items = new ArrayList<>(); // 아이템클래스를 연결시킴
    String whois, profileIMGurl, nickName, content;

    // 서버에서 가져온 유저정보
    String idx_user, nickName_user, profileIMG_user;

    // 방 출입에 따라 시청자 인원수 증감
    String idx_room,exit_room;

    ImageView btn_giftcoin;
    Button btn_send;
    EditText sendMSG;
    TextView title, participants;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liveplayer);

        // 방송제목과 시청자수
        title = findViewById(R.id.updatetitle2);
        participants = findViewById(R.id.participants2);

        // 시청화면 띄우기
        mProgressBar = findViewById(R.id.loading);
        mComponentListener = new ComponentListener();
        mPlayerView = findViewById(R.id.video_view);

        // 채팅하기
        btn_giftcoin = findViewById(R.id.btn_giftcoin);
        btn_send = findViewById(R.id.btn_send);
        sendMSG = findViewById(R.id.sendMSG);
        recyclerView = findViewById(R.id.playerview);

        // 서버로부터 유저 정보 가져오기
        getuserData();

        // 클릭하여 들어온 방 식별하기
        Intent intent = getIntent();
        idx_room = intent.getStringExtra("idx_room");

        // 현재 입장한 방의 정보를 가져와 화면에 띄워준다(방송제목, 시청자수)
        viewUIfromServer();

        // 뷰의 모습 -> 리니어
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // 리사이클러뷰에 adapter 객체 지정.
        recyclerView.setAdapter(adapter);




        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("idx_user", idx_user);
                    jsonObject.put("nickName_user", nickName_user);
                    jsonObject.put("profileIMG_user", profileIMG_user);
                    jsonObject.put("content",sendMSG.getText().toString());
                    jsonObject.put("whois","viewer");

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


    } // 온크리에이트

    @Override
    protected void onResume() {
            super.onResume();
            initializePlayer();

            setbindService();

        adapter.notifyDataSetChanged();
        adapter.setItems(items);
    } // 온리쥼

    private void initializePlayer(){
        if( mSimpleExoPlayer == null) {

            // a factory to create an AdaptiveVideoTrackSelection
            TrackSelection.Factory adaptiveTrackSelectionFactory =
                    new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);

            mSimpleExoPlayer = ExoPlayerFactory.newSimpleInstance(
                    new DefaultRenderersFactory(this),
                    new DefaultTrackSelector(adaptiveTrackSelectionFactory),
                    new DefaultLoadControl());

            mSimpleExoPlayer.addListener(mComponentListener);
            mSimpleExoPlayer.addVideoListener(mComponentListener);
            mSimpleExoPlayer.setPlayWhenReady(mPlayWhenReady);
            mSimpleExoPlayer.seekTo(mCurrentWindow, mPlaybackPosition);

            mPlayerView.setPlayer(mSimpleExoPlayer);
            // 화면 꽉채우기 RESIZE_MODE_FILL
            // 가로세로 비율 맞추기 RESIZE_MODE_FIT
            mPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);


        }
        // Preparing media

        Uri uri = Uri.parse("rtmp://3.36.159.193:1935/live/streamkey");
        MediaSource media = buildMediaSource(uri);
        mSimpleExoPlayer.prepare(media);

    }

    private MediaSource buildMediaSource(Uri uri) {

        /**
         * RTMP for boradcasting live streaming vidoe to server and also to playback from server
         * Here we fetch from server
         */
        RtmpDataSourceFactory rtmpDataSourceFactory = new RtmpDataSourceFactory();
        MediaSource mediaSource = new ExtractorMediaSource
                .Factory(rtmpDataSourceFactory)
                .createMediaSource(uri);

        return mediaSource;

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    private void releasePlayer() {
        if (mSimpleExoPlayer != null) {
            mCurrentWindow = mSimpleExoPlayer.getCurrentWindowIndex();
            mPlayWhenReady = mSimpleExoPlayer.getPlayWhenReady();
            mPlaybackPosition = mSimpleExoPlayer.getCurrentPosition();
            mSimpleExoPlayer.removeListener(mComponentListener);
            mSimpleExoPlayer.removeVideoListener(mComponentListener);
            mSimpleExoPlayer.release();
            mSimpleExoPlayer = null;

        }
    }

    private class ComponentListener extends Player.DefaultEventListener implements
            VideoListener {

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            String stateString;
            switch (playbackState) {
                case Player.STATE_IDLE:
//                    stateString = "Player.STATE_IDLE      -";
                    break;
                case Player.STATE_BUFFERING:
//                    stateString = "Player.STATE_BUFFERING -";
                    break;
                case Player.STATE_READY:
                    stateString = "Player.STATE_READY     -";
                    mProgressBar.setVisibility(View.GONE);
                    break;
                case Player.STATE_ENDED:
                    stateString = "Player.STATE_ENDED     -";
                    break;
                default:
                    stateString = "UNKNOWN_STATE             -";
                    break;
            }
//            Log.d(TAG, "changed state to " + stateString
//                    + " playWhenReady: " + playWhenReady);

            Bundle params = new Bundle();
//            params.putString("Player_State" , stateString);
            params.putBoolean("Player_ready", playWhenReady);

//            Toast.makeText(Fm1_liveplayer2.this, "changed state" + stateString, Toast.LENGTH_SHORT).show();

        }

        /**
         * Called each time there's a change in the size of the video being rendered.
         *
         */
        @Override
        public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees,
                                       float pixelWidthHeightRatio) {



        }
        /**
         * Called when a frame is rendered for the first time since setting the surface, and when a
         * frame is rendered for the first time since a video track was selected.
         */
        @Override
        public void onRenderedFirstFrame() {
            Log.d(TAG, "onRenderedFirstFrame: ");
            Toast.makeText(Activity_Liveplayer.this, "First frame", Toast.LENGTH_SHORT).show();

            if(mPlayWhenReady == true) {
                Bundle params = new Bundle();
            }
        }
        @Override
        public void onTracksChanged(TrackGroupArray trackGroups,
                                    TrackSelectionArray trackSelections) {
            super.onTracksChanged(trackGroups, trackSelections);
            Log.d(TAG, "onTracksChanged: ");


        }

        @Override
        public void onLoadingChanged(boolean isLoading) {
            super.onLoadingChanged(isLoading);

            Log.d(TAG, "onLoadingChanged: ");
            if(isLoading) {
//                Toast.makeText(Fm1_liveplayer2.this, "loading changed", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            super.onPlayerError(error);
            Log.d(TAG, "onPlayerError: ");
            Toast.makeText(Activity_Liveplayer.this, "Error occured :" + error , Toast.LENGTH_SHORT).show();

            releasePlayer();
        }
    }







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

                        if(jsonObject.getString("whois").equals("enter")){
                            whois = jsonObject.getString("whois");

                            // 입장이면 인원수 +1
//                            viewerplusminus(whois);

                        }else if(jsonObject.getString("whois").equals("exit")){
                            whois = jsonObject.getString("whois");

                            // 퇴장이면 인원수 -1
//                            viewerplusminus(whois);

                            finish();
                            /**
                             * 인텐트로 액티비티 이동 및 데이터 송신
                             */
                                    Intent intent = new Intent(getApplicationContext(), Activity_Liveplayerend.class);
                                    startActivity(intent);

//                            AlertDialog.Builder builder = new AlertDialog.Builder(Activity_Liveplayer.this);
//                            builder.setTitle("방송종료").setMessage("종료된 방송입니다.");
//                            builder.setPositiveButton("확인", new DialogInterface.OnClickListener(){
//                                @Override
//
//                                public void onClick(DialogInterface dialog, int id)
//                                {
//                                    finish();
//                                }
//                            });


                        }else if(jsonObject.getString("whois").equals("giftcoin")){
                            whois = jsonObject.getString("whois");

                            // 코인 선물오면 뷰에 보여주고, DB 값 업데이트 한다.

                        }else if(jsonObject.getString("whois").equals("Bj")){

                            whois = jsonObject.getString("whois");
                            profileIMGurl = jsonObject.getString("profileIMG_user");
                            nickName = jsonObject.getString("nickName_user");
                            content = jsonObject.getString("content");

                            Activity_Chat_Items itle = new Activity_Chat_Items(whois, profileIMGurl, nickName, content); // 객체를 생성하고
                            items.add(itle); // 아이템에 추가시킨다.
                            adapter.notifyDataSetChanged(); // 변경사항 반영 메소드(추가되었으니 리사이클러뷰 새로고침)
                            adapter.setItems(items);
                            scrollDown();

                        }else if(jsonObject.getString("whois").equals("viewer")){

                            whois = jsonObject.getString("whois");
                            profileIMGurl = jsonObject.getString("profileIMG_user");
                            nickName = jsonObject.getString("nickName_user");
                            content = jsonObject.getString("content");

                            Activity_Chat_Items itle = new Activity_Chat_Items(whois, profileIMGurl, nickName, content); // 객체를 생성하고
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
                    Message msg = Message.obtain(null, Activity_ChatService.MSG_SEND_TO_SERVICE, str);
                    msg.replyTo = mMessenger;
                    mServiceMessenger.send(msg);
                } catch (RemoteException e) {
                }
            }
        }
    }


    // 채팅 받을때마다 리싸이클러뷰 스크롤 맨 아래로
    public void scrollDown(){
        recyclerView.post(new Runnable() {
              @Override
              public void run() {
                  recyclerView.scrollToPosition(items.size()-1);
              }
          });
      }



    // 유저 데이터 가져오기
    public void getuserData() {
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences( // 쉐어드
                "userInfo", Context.MODE_PRIVATE);


        //서버로 보낼 데이터
        String user = sharedPref.getString("idx_user",null);

        String serverUrl=IP_ADDRESS + "/everyLive/livestream/getuserdata.php";

        SimpleMultiPartRequest smpr= new SimpleMultiPartRequest(Request.Method.POST, serverUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {


                // 서버에서 가져온 정보
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    idx_user = jsonObject.getString("idx_user");
                    nickName_user = jsonObject.getString("nickName");
                    profileIMG_user = jsonObject.getString("profileIMG");


                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // 방 입장할때 시청자 수 변화주기
                try {
                    JSONObject viewercheck = new JSONObject();
                    viewercheck.put("idx_user", idx_user);
                    viewercheck.put("idx_room", idx_room);
                    viewercheck.put("nickName_user", nickName_user);
                    viewercheck.put("profileIMG_user", profileIMG_user);
                    viewercheck.put("whois","enter");

                    sendMessageToService(viewercheck.toString());

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
        smpr.addStringParam("idx_user", user);

        //요청객체를 서버로 보낼 우체통 같은 객체 생성
        RequestQueue requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(smpr);
    }



    // 방송제목과 시청자 수를 서버에서 가져와 그려준다.
    public void viewUIfromServer() {

        String serverUrl=IP_ADDRESS + "/everyLive/livestream/playerviewUI.php";

        //파일 전송 요청 객체 생성[결과를 String으로 받음]
        SimpleMultiPartRequest smpr= new SimpleMultiPartRequest(Request.Method.POST, serverUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.d("viewUIfromServer", response);

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    title.setText(jsonObject.getString("titleLive"));
//                    participants.setText(jsonObject.getString("participants"));
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
        smpr.addStringParam("idx_room", idx_room);

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

//                    title.setText("titleLive");
//                    participants.setText("시청자 : "+jsonObject.getString("participants")+"명");

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
        smpr.addStringParam("idx_room", idx_room);
        smpr.addStringParam("plusminus", who);

        //요청객체를 서버로 보낼 우체통 같은 객체 생성
        RequestQueue requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(smpr);
    }



}
