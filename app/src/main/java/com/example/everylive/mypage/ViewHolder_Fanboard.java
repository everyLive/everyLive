package com.example.everylive.mypage;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.everylive.R;
import com.example.everylive.login.Activity_Register;

import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewHolder_Fanboard extends RecyclerView.ViewHolder {

    RequestQueue requestQueue;

    CircleImageView fanboard_userImg;
    EditText fanboardMSG;
    ImageView btn_fanboardMSG;
    RecyclerView recycler_fanboard;

    SharedPreferences sharedPreferences;
    String page_owner; // 이 페이지 주인
    String idx_viewer; // 이 페이지 보는 사람

    Context context;

    public ViewHolder_Fanboard(@NonNull View itemView, int viewType, Context context, String page_owner) { // 뷰홀더에서 작업 실행.
        super(itemView);

        this.context = context;
        this.page_owner = page_owner;
        requestQueue = Volley.newRequestQueue(itemView.getContext());

        // 팬보드 댓글 다는 곳 이미지 가져오기.
        sharedPreferences = itemView.getContext().getSharedPreferences("userInfo", MODE_PRIVATE);
        idx_viewer = sharedPreferences.getString("idx_user",null);
        getUserProfileIMG(idx_viewer);

        fanboard_userImg = itemView.findViewById(R.id.fanboard_userImg);
        fanboardMSG = itemView.findViewById(R.id.fanboardMSG);
        btn_fanboardMSG = itemView.findViewById(R.id.btn_fanboardMSG);

        recycler_fanboard = itemView.findViewById(R.id.recycler_fanboard);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        recycler_fanboard.setLayoutManager(layoutManager);
        
        // 메세지 전송 누르면, 변경.
        btn_fanboardMSG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = fanboardMSG.getText().toString();

                // 안드로이드에서 보낼 데이터를 받을 php 서버 주소
                String serverUrl = "http://3.36.159.193/everyLive/";


                //파일 전송 요청 객체 생성[결과를 String으로 받음]
                SimpleMultiPartRequest smpr= new SimpleMultiPartRequest(Request.Method.POST, serverUrl, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            // DB 저장된 데이터를 Array에 넣기.
                            // 갱신.

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(itemView.getContext(), "ERROR", Toast.LENGTH_SHORT).show();
                    }
                });

                //요청 객체에 보낼 데이터를 추가
                smpr.addStringParam("page_owner", page_owner); // 이 페이지 주인.
                smpr.addStringParam("idx_user", idx_viewer); // 글쓰는 사람
                smpr.addStringParam("msg", msg); // 팬보드 내용

                //요청객체를 서버로 보낼 우체통 같은 객체 생성
                requestQueue.add(smpr);
            }
        });
    }

    // 댓글 남기는 곳, 이미지뷰에 넣을 이미지url 가져오기.
    public void getUserProfileIMG(String idx_viewer){ // 페이지에 들어온 사람.

        String serverUrl = "http://3.36.159.193/everyLive/mypage/RequestGetUserProfileIMG.php";

        SimpleMultiPartRequest smpr= new SimpleMultiPartRequest(Request.Method.POST, serverUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.d("viewholder", response);

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    Boolean success = jsonObject.getBoolean("success");

                    if(success){
                        String profileimgURL = jsonObject.getString("profileIMG");

                        Log.d("profileimgURL", profileimgURL.trim());

                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Glide.with(context)
                                        .load(profileimgURL)
                                        .override(400, 400)
                                        .centerCrop()
                                        .into(fanboard_userImg);
                                fanboard_userImg.bringToFront();
                            }
                        }, 1000);

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(itemView.getContext(), "ERROR", Toast.LENGTH_SHORT).show();
            }
        });

        //요청 객체에 보낼 데이터를 추가
        smpr.addStringParam("idx_viewer", idx_viewer);

        //요청객체를 서버로 보낼 우체통 같은 객체 생성
        requestQueue.add(smpr);
    }

    public void getFanboradMSG(String idx_user){ // 페이지 주인인

   }
}
