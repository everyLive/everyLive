package com.example.everylive.livestream;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import android.util.Log;


import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;


public class Activity_ChatService extends Service {

    // 소켓연결 쓰레드
    private ConnectionThread thread;
    // 서버와 연결되어있는 소켓 객체
    Socket member_socket;

    // 어플 종료시 스레드 중지를 위해
    boolean isRunning = false;
    // 서버 접속 여부를 판별하기 위한 변수
    boolean isConnect = false;

    // json에서 사용됨.
    String jname,jenter,jcontent,jimg,jvideo,jtime;

    // 리눅스 서버 주소.
    private static String IP_ADDRESS = "3.36.159.193";

    // 액티비티-서비스 데이터전달
    public static final int MSG_REGISTER_CLIENT = 1;
    public static final int MSG_UNREGISTER_CLIENT = 2;
    public static final int MSG_SEND_TO_SERVICE = 3;
    public static final int MSG_SEND_TO_ACTIVITY = 4;
    public static final int MSG_UPDATE_LIVEINFO = 5;
    private Messenger mClient = null;   // Activity 에서 가져온 Messenger


    @Override
    public IBinder onBind(Intent intent) {
        // Service 객체와 (화면단 Activity 사이에서)
        // 통신(데이터를 주고받을) 때 사용하는 메서드
        // 데이터를 전달할 필요가 없으면 return null;
        // return null;
        return mMessenger.getBinder(); // 서비스 객체를 리턴
        }

    /** activity로부터 binding 된 Messenger
     *  액티비티에서 서비스로 넘어온 메시지를 수신하는곳.
     * */
    private final Messenger mMessenger = new Messenger(new Handler(new Handler.Callback() {
    @Override
    public boolean handleMessage(Message msg) {
        // msg.obj가 액티비티에서 넘어온 값임.
        Log.w("test"," message what : "+msg.what +" , msg.obj "+ msg.obj);

        switch (msg.what) {
            case MSG_SEND_TO_SERVICE: // json 형식으로 온다 idx_user, 프로필이미지url, 닉네임, 채팅내용
                if(msg.obj!=null) {
                    Log.d("액티비티에서 받은 메세지", msg.obj.toString());
                    Log.d("member_socket", member_socket.toString());

                    try {
                        JSONObject jso = new JSONObject(msg.obj.toString());
                        if(jso.getString("whois").equals("Bj")){


                            // 텍스트 처리하는 쓰레드 가동
                            SendToServerThread stt = new SendToServerThread(member_socket,msg.obj.toString());
                            stt.start();


                        }else if(jso.getString("whois").equals("viewer")){

                            // 서버로 보내기
                            SendToServerThread stt = new SendToServerThread(member_socket,msg.obj.toString());
                            stt.start();

                        }else if(jso.get("whois").equals("enter")){

                            // 서버로 보내기
                            SendToServerThread stt = new SendToServerThread(member_socket,msg.obj.toString());
                            stt.start();

                        }else if(jso.get("whois").equals("updateroominfo")){

                            // 서버로 보내기
                            SendToServerThread stt = new SendToServerThread(member_socket,msg.obj.toString());
                            stt.start();

                        }else if(jso.get("whois").equals("exit")){

                            // 서버로 보내기
                            SendToServerThread stt = new SendToServerThread(member_socket,msg.obj.toString());
                            stt.start();

                        }else if(jso.get("whois").equals("crateroom")){

                            // 서버로 보내기
                            SendToServerThread stt = new SendToServerThread(member_socket,msg.obj.toString());
                            stt.start();

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }



                }


                break;

            case MSG_UPDATE_LIVEINFO:
                sendMsgToupdate("update");
                break;

            case MSG_REGISTER_CLIENT:
                mClient = msg.replyTo;  // activity로부터 가져온
                break;
        }
        return false;
        }
        }));

    /**
     * 액티비티로 메세지를 송신하는 메서드
     * 번들에 담는다. 액티비티에서 받을때 해당 키값이 필요하다.
     * 메세지 종류를 구분할 수 있다.
     * 액티비티의 Messenger 클래스에서 case로 판별한다.
     **/
    private void sendMsgToActivity(String sendValue) {
        try {
        Bundle bundle = new Bundle();
        bundle.putString("sendServiceMsgToActivity",sendValue);
        Message msg = Message.obtain(null, MSG_SEND_TO_ACTIVITY);
        msg.setData(bundle);
        mClient.send(msg);      // msg 보내기
        } catch (RemoteException e) {
        }
        }

    private void sendMsgToupdate(String sendValue) {
        try {
            Bundle bundle = new Bundle();
            bundle.putString("sendServiceMsgToActivity",sendValue);
            Message msg = Message.obtain(null, MSG_UPDATE_LIVEINFO);
            msg.setData(bundle);
            mClient.send(msg);      // msg 보내기
        } catch (RemoteException e) {
        }
    }



    /**
     * 서비스클래스 시작과 동시에 소켓연결 쓰레드 가동.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        // 서비스에서 가장 먼저 호출됨(최초에 한번만)
        Log.d("test", "서비스의 onCreate");

        // 소켓연결 쓰레드 가동
        thread = new ConnectionThread();
        thread.start();
        }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 서비스가 호출될 때마다 실행
        Log.d("test", "서비스의 onStartCommand");

        return super.onStartCommand(intent, flags, startId);
        }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 서비스가 종료될 때 실행
        thread.interrupt(); // 소켓연결 쓰레드 종료

        Log.d("test", "서비스의 onDestroy");
        }

    // 리눅스서버와 소켓연결하는 쓰레드
    class ConnectionThread extends Thread {

        SharedPreferences sharedPref = Activity_ChatService.this.getSharedPreferences(
            "userInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();


    @Override
    public void run() {
        try {
            Log.d("서버접속 처리 스레드", "run: ");
            // 접속한다.
            // 192.168.168.103 윈도우
            // 3.36.159.193 리눅스
            final Socket socket = new Socket("3.36.159.193", 8080);
            member_socket=socket;

            Log.d("소켓", socket.toString());

            // 닉네임을 서버로 전달할 것임.
//            JSONObject jsonObject = new JSONObject();
//            jsonObject.put("idx_user",sharedPref.getString("idx_user","def"));
//            jsonObject.put("whois","enter");
            String idx_user = sharedPref.getString("idx_user","def");
            String nickName = sharedPref.getString("nickName","def");
            Log.d("소켓연결시 보낼 값", idx_user);

            // 스트림을 추출. 보낼때는 데이터아웃풋스트림이 필요하다.
            OutputStream os = socket.getOutputStream();
            Log.d("OutputStream", os.toString());
            DataOutputStream dos = new DataOutputStream(os); // 아웃풋스트림
            Log.d("DataOutputStream", dos.toString());

            // 닉네임을 송신한다.
            dos.writeUTF(idx_user);

            // 접속 상태를 true로 셋팅한다.
            // 필수는 아니며, 조건을 위한 장치의 역할을 한다.
            isConnect=true;
            if(isConnect=true){
                Log.d("isConnect", "true");
                editor.putString("isConnect","true");
            }else{
                Log.d("isConnect", "false");
            }

            // 메세지 수신을 위한 스레드 가동
            isRunning=true;
            if(isRunning=true){
                Log.d("isRunning", "true");
                editor.putString("isRunning","true");
            }else{
                Log.d("isRunning", "false");
            }

            editor.apply(); // 있어야 쉐어드값 저장,삭제됨

            // 서버로부터 온 메세지 처리하는 쓰레드 가동
            MessageThread mthread=new MessageThread(socket);
            mthread.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    }


    // 서버로부터 온 메세지 처리
    // Socket이 있어야 한다. 각 소켓에는 로컬포트가 존재한다.
    // 데이터 받는 처리는 데이터인풋스트림.
    // SendToServerThread1,2,3로 (텍스트,이미지,동영상)을 송신했을때
    // 서버가 응답한 값 수신을 혼자 맡아 처리한다.
    class MessageThread extends Thread {
    Socket socket;
    DataInputStream dis;

    public MessageThread(Socket socket) {
        try {

            Log.d("MessageThread", "MessageThread: ");
            Log.d("Socket", socket.toString());
            this.socket = socket;
            InputStream is = socket.getInputStream();
            dis = new DataInputStream(is);
            Log.d("InputStream", is.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try{
            while (isRunning){
                // 서버로부터 데이터를 수신받는다. dis.readUTF()
                final String msg=dis.readUTF();
                Log.d("서버로부터 수신받은 데이터", msg);
                // 화면에 출력

                JSONObject jsonObject = new JSONObject(msg);

                // value값 구분하여 처리한다.
                if(jsonObject.getString("whois").equals("Bj")){

                    // 액티비티로 json형태의 값을 보낸다.
                    sendMsgToActivity(msg);


                }
                else if(jsonObject.getString("whois").equals("viewer")){
                    // 액티비티로 json형태의 값을 보낸다.
                    sendMsgToActivity(msg);

                }else if(jsonObject.getString("whois").equals("giftcoin")){

                }else if(jsonObject.getString("whois").equals("enter")){

                    sendMsgToActivity(msg);

                }else if(jsonObject.getString("whois").equals("exit")){
                    sendMsgToActivity(msg);

                }else if(jsonObject.getString("whois").equals("updateroominfo")){
                    sendMsgToActivity(msg);
                }
                else if(jsonObject.getString("whois").equals("crateroom")){
                    sendMsgToActivity(msg);
                }



            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    }


    // 서버에 데이터를 전달하는 스레드
    // 고유한 소켓이 필요.
    // 송신할땐 데이터아웃풋 스트림.
    class SendToServerThread extends Thread{
    Socket socket;
    String msg;
    DataOutputStream dos;

    public SendToServerThread(Socket socket, String msg){
        try{
            Log.d("SendToServerThread", "SendToServerThread: ");
            Log.d("Socket", socket.toString());
            this.socket=socket;
            this.msg=msg;
            OutputStream os=socket.getOutputStream();
            dos=new DataOutputStream(os);
            dos.flush();
            Log.d("SendToServerThread", msg);


        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @Override
    public void run() {
        try{

//            SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
//                    "userInfo", Context.MODE_PRIVATE);
//            String idx_user = sharedPref.getString("idx_user","def"); // 값 불러오기
//
//            JSONObject jsonObject = new JSONObject();
//            jsonObject.put("idx_user",idx_user);
//            jsonObject.put("content",msg);

            Log.d("서버로보낼텍스트데이터", msg);
            // writeUTF 서버로 데이터를 보낸다.
            dos.writeUTF(msg);
            dos.flush(); // 잔여물 배출하는 용도.


        }catch (Exception e){
            e.printStackTrace();
        }
    }
    }




}
