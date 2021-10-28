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
import com.example.everylive.mypage.Adapter.AdapterFanboard;
import com.example.everylive.mypage.Adapter.AdapterNotice;
import com.example.everylive.mypage.Item.Item_fanboard_writing;
import com.example.everylive.mypage.Item.Item_notice_Comment;
import com.example.everylive.mypage.Item.Item_notice_writing;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewHolder_Fanboard extends RecyclerView.ViewHolder {

    CircleImageView fanboard_userImg;
    EditText fanboardMSG;
    ImageView btn_fanboardMSG;
    RecyclerView recycler_fanboard;
    AdapterFanboard adapterFanboard;
    ArrayList<Item_fanboard_writing> dataListForFanboard;

    SharedPreferences sharedPreferences;
    String page_owner; // 이 페이지 주인
    String idx_viewer; // 이 페이지 보는 사람

    RequestQueue requestQueue;
    Context context;

    public ViewHolder_Fanboard(@NonNull View itemView, int viewType, Context context, String page_owner) { // 뷰홀더에서 작업 실행.
        super(itemView);

        this.context = context;
        this.page_owner = page_owner;
        requestQueue = Volley.newRequestQueue(itemView.getContext());

        // 팬보드 게시글 다는 곳 이미지 가져오기.
        sharedPreferences = itemView.getContext().getSharedPreferences("userInfo", MODE_PRIVATE);
        idx_viewer = sharedPreferences.getString("idx_user",null);
        getUserProfileIMG(idx_viewer);

        fanboard_userImg = itemView.findViewById(R.id.fanboard_userImg);
        fanboardMSG = itemView.findViewById(R.id.fanboardMSG);
        btn_fanboardMSG = itemView.findViewById(R.id.btn_fanboardMSG);

        // 팬보드 리사이클러뷰
        recycler_fanboard = itemView.findViewById(R.id.recycler_fanboard);
        dataListForFanboard = new ArrayList<>(); // 서버에서 데이터 불러오기 전에 Adapter가 생성되기 때문에, new ArrayList 필수.
        adapterFanboard = new AdapterFanboard(context, dataListForFanboard);
        recycler_fanboard.setAdapter(adapterFanboard);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        recycler_fanboard.setLayoutManager(layoutManager);
        
        // 메세지 전송 누르면, 변경.
        btn_fanboardMSG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = fanboardMSG.getText().toString();
                fanboardMSG.setText("");

                // 서버에 팬보드 저장하기
                writeFanboard(msg);
            }
        });

        // 팬보드 데이터 가져오기
        getFanboardData(page_owner);
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

    // 팬보드 글 가져오기
    public void getFanboardData(String page_owner){ // 페이지 주인인
        String serverUrl = "http://3.36.159.193/everyLive/mypage/RequestGetFanboardData.php";

        SimpleMultiPartRequest smpr= new SimpleMultiPartRequest(Request.Method.POST, serverUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.d("getFanboradData", response);

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    Boolean success = jsonObject.getBoolean("success");

                    if(success){
                        JSONArray jsonArray = new JSONArray(jsonObject.getString("notice"));

                        for(int i=0; i<jsonArray.length(); i++){
                            JSONObject jo = jsonArray.getJSONObject(i);

                            String idx_fanBoard = jo.getString("idx_fanBoard");
                            String textContents = jo.getString("textContents");

                            String writer = jo.getString("writer"); // writer_idx
                            String nickName = jo.getString("nickName");
                            String profileIMG = jo.getString("profileIMG");
                            String writeDate = jo.getString("writeDate");
                            String writeTime = jo.getString("writeTime");

                            String commentWriterIdx = jo.getString("commentWriterIdx");
                            String commentWriterProfileIMG = jo.getString("commentWriterProfileIMG");

                            // 공지사항 dataListForNotice에 들어갈 Item_notice_writing 생성.
                            Item_fanboard_writing item_fanboard_writing = new Item_fanboard_writing();

                            // 공지사항 댓글 제외 값들 set
                            item_fanboard_writing.setIdx_fanboard_writing(idx_fanBoard);
                            item_fanboard_writing.setTextContents(textContents);
                            item_fanboard_writing.setWriter_nickName(nickName);
                            item_fanboard_writing.setWriter_imgUrl(profileIMG);
                            item_fanboard_writing.setWriteDate(writeDate);
                            item_fanboard_writing.setIdx_writer(writer);

                            item_fanboard_writing.setIdx_comment_writer(commentWriterIdx);
                            item_fanboard_writing.setComment_imgUrl(commentWriterProfileIMG);

                            //해당 공지에 댓글이 있는지 확인.
                            Boolean comments_stat = jo.getBoolean("comments_stat");

                            if(comments_stat){ // true : 댓글이 있다.
                                JSONArray ja = jo.getJSONArray("comments");

                                Log.d("댓글 갯수", String.valueOf(ja.length()));

                                for(int j=0; j<ja.length(); j++){
                                    Log.d("댓글 카운트", String.valueOf(j));

                                    JSONObject item_comment = ja.getJSONObject(j); // j를 넣어야하는데 i를 넣어서 계속 ~ 원하는 값이 안나옴 ^^ 너무 화가난다.
                                    Log.d("item", String.valueOf(item_comment));

                                    String idx_comments = item_comment.getString("idx_comments");
                                    String commentContents = item_comment.getString("textComments");
                                    String idx_writer = item_comment.getString("writer");
                                    String userNickName = item_comment.getString("nickName");
                                    String userImgUrl = item_comment.getString("profileIMG");
                                    String commentWriteDate = item_comment.getString("writeDate");
                                    item_comment.getString("writeTime");

                                    item_fanboard_writing.addCommentArrayList(new Item_notice_Comment(idx_comments, idx_writer, userImgUrl, userNickName, commentWriteDate, commentContents));
                                }

                            }else{ // false : 댓글이 없다.
                                item_fanboard_writing.setCommentArrayList(null);
                            }

                            adapterFanboard.addItem_fanBoard(item_fanboard_writing);
                            // dataListForNotice.add(item_notice_writing);
                            // -> dataListForNotice는 서버 데이터 가져오기 전 null 에러 나기 전에 임시로 넣은 값이기 때문에
                            // 여기가 아니라 어댑터 내의 함수 이용해서 리스트에 add 시켜주기.
                        }
                        adapterFanboard.notifyDataSetChanged();
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
        smpr.addStringParam("page_owner", page_owner);
        smpr.addStringParam("idx_viewer", idx_viewer);

        //요청객체를 서버로 보낼 우체통 같은 객체 생성
        requestQueue.add(smpr);
    }

    // 팬보드에 글 쓰기
    public void writeFanboard(String msg){
        String serverUrl = "http://3.36.159.193/everyLive/mypage/RequestWriteFanboard.php";

        SimpleMultiPartRequest smpr= new SimpleMultiPartRequest(Request.Method.POST, serverUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.d("writeFanboard", response);

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    Boolean success = jsonObject.getBoolean("success");

                    if(success){
                        String idx_fanboard = jsonObject.getString("idx_fanBoard");
                        String textContents = jsonObject.getString("textContents");
                        String author = jsonObject.getString("author"); // 페이지 주인
                        String writer = jsonObject.getString("writer"); // 팬보드 작성자
                        String nickName = jsonObject.getString("nickName");
                        String profileIMG = jsonObject.getString("profileIMG");
                        String writeDate = jsonObject.getString("writeDate");
                        String writeTime = jsonObject.getString("writeTime");


                        Item_fanboard_writing item_fanboard_writing = new Item_fanboard_writing();
                        item_fanboard_writing.setIdx_fanboard_writing(idx_fanboard);
                        item_fanboard_writing.setIdx_writer(writer);
                        item_fanboard_writing.setWriter_nickName(nickName);
                        item_fanboard_writing.setWriter_imgUrl(profileIMG);
                        item_fanboard_writing.setWriteDate(writeDate);
                        item_fanboard_writing.setTextContents(textContents);
                        item_fanboard_writing.setCommentArrayList(null);

                        // 어댑터 안에 있는 어레이리스트에 add 해야한다.
                        adapterFanboard.addItem_fanBoard(item_fanboard_writing);
                        adapterFanboard.notifyDataSetChanged();

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
        smpr.addStringParam("page_owner", page_owner); // 이 페이지 주인.
        smpr.addStringParam("idx_user", idx_viewer); // 글쓰는 사람
        smpr.addStringParam("msg", msg); // 팬보드 내용

        //요청객체를 서버로 보낼 우체통 같은 객체 생성
        requestQueue.add(smpr);
    }
}
