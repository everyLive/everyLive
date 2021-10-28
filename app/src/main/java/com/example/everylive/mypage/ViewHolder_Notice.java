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
import com.example.everylive.mypage.Adapter.AdapterNotice;
import com.example.everylive.mypage.Item.Item_notice_Comment;
import com.example.everylive.mypage.Item.Item_notice_writing;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewHolder_Notice extends RecyclerView.ViewHolder {

    CircleImageView notice_userImg;
    EditText noticeMSG;
    ImageView btn_noticeMSG;
    RecyclerView recycler_notice;
    AdapterNotice adapterNotice;
    ArrayList<Item_notice_writing> dataListForNotice;

    SharedPreferences sharedPreferences;
    String page_owner; // 이 페이지 주인
    String idx_viewer;

    RequestQueue requestQueue;
    Context context;

    public ViewHolder_Notice(@NonNull View itemView, int viewType, Context context, String page_owner) { // 뷰홀더에서 작업 실행.
        super(itemView);

        this.page_owner = page_owner;
        this.context = context;
        requestQueue = Volley.newRequestQueue(itemView.getContext());

        notice_userImg = itemView.findViewById(R.id.notice_userImg);
        noticeMSG = itemView.findViewById(R.id.noticeMSG);
        btn_noticeMSG = itemView.findViewById(R.id.btn_noticeMSG);

        // 공지사항 리사이클러뷰 설정.
        recycler_notice = itemView.findViewById(R.id.recycler_notice);

        dataListForNotice = new ArrayList<>(); // 서버에서 데이터 불러오기 전에 Adapter가 생성되기 때문에, new ArrayList 필수.
        adapterNotice = new AdapterNotice(context, dataListForNotice);
        recycler_notice.setAdapter(adapterNotice);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        recycler_notice.setLayoutManager(layoutManager);

        sharedPreferences = itemView.getContext().getSharedPreferences("userInfo", MODE_PRIVATE);
        idx_viewer = sharedPreferences.getString("idx_user",null);

        if(page_owner.equals(idx_viewer)){
            notice_userImg.setVisibility(View.VISIBLE);
            noticeMSG.setVisibility(View.VISIBLE);
            btn_noticeMSG.setVisibility(View.VISIBLE);

            getUserProfileIMG(idx_viewer);

            btn_noticeMSG.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String noticeMsg = noticeMSG.getText().toString();
                    noticeMSG.setText("");

                    writeNotice(page_owner, noticeMsg, "0"); // fragment_notice에 이미지 넣는걸 만들어야함. 일단 "0"으로 처리하기.
                }
            });

        }else{
            notice_userImg.setVisibility(View.GONE);
            noticeMSG.setVisibility(View.GONE);
            btn_noticeMSG.setVisibility(View.GONE);
        }

        // 공지사항 정보 가져오기.
        getNoticeData(page_owner);

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
                                        .into(notice_userImg);
                                notice_userImg.bringToFront();
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

    // 공지사항 정보 가져오기.
    public void getNoticeData(String page_owner){

        String serverUrl = "http://3.36.159.193/everyLive/mypage/RequestGetNoticeData.php";

        SimpleMultiPartRequest smpr= new SimpleMultiPartRequest(Request.Method.POST, serverUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.d("getNoticeData", response);

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    Boolean success = jsonObject.getBoolean("success");

                    if(success){
                        JSONArray jsonArray = new JSONArray(jsonObject.getString("notice"));

                        for(int i=0; i<jsonArray.length(); i++){
                            JSONObject jo = jsonArray.getJSONObject(i);

                            String idx_notice = jo.getString("idx_notice");
                            String textContents = jo.getString("textContents");
                            String imgContents = jo.getString("imgContents");
                            String writer = jo.getString("writer"); // writer_idx
                            String nickName = jo.getString("nickName");
                            String profileIMG = jo.getString("profileIMG");
                            String writeDate = jo.getString("writeDate");
                            String writeTime = jo.getString("writeTime");

                            String commentWriterIdx = jo.getString("commentWriterIdx");
                            String commentWriterProfileIMG = jo.getString("commentWriterProfileIMG");

                            // 공지사항 dataListForNotice에 들어갈 Item_notice_writing 생성.
                            Item_notice_writing item_notice_writing = new Item_notice_writing(context);

                            // 공지사항 댓글 제외 값들 set
                            item_notice_writing.setIdx_notice_writing(idx_notice);
                            item_notice_writing.setTextContents(textContents);
                            item_notice_writing.setImgContents(imgContents);
                            item_notice_writing.setWriter_nickName(nickName);
                            item_notice_writing.setWriter_imgUrl(profileIMG);
                            item_notice_writing.setWriteDate(writeDate);
                            item_notice_writing.setIdx_comment_writer(commentWriterIdx);
                            item_notice_writing.set_comment_imgUrl(commentWriterProfileIMG);
                            item_notice_writing.setIdx_writer(writer);

                            // 해당 공지에 댓글이 있는지 확인.
                            Boolean comments_stat = jo.getBoolean("comments_stat");

                            if(comments_stat){ // true : 댓글이 있다.
                                JSONArray ja = jo.getJSONArray("comments");

                                ArrayList<Item_notice_Comment> aic = new ArrayList<>();
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

                                    item_notice_writing.addCommentArrayList(new Item_notice_Comment(idx_comments, idx_writer, userImgUrl, userNickName, commentWriteDate, commentContents));
                                }

                            }else{ // false : 댓글이 없다.
                                item_notice_writing.setCommentArrayList(null);
                            }
                            adapterNotice.addItem_notice(item_notice_writing);
                            // dataListForNotice.add(item_notice_writing);
                            // -> dataListForNotice는 서버 데이터 가져오기 전 null 에러 나기 전에 임시로 넣은 값이기 때문에
                            // 여기가 아니라 어댑터 내의 함수 이용해서 리스트에 add 시켜주기.
                        }
                        adapterNotice.notifyDataSetChanged();
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

    // 공지사항 쓰기
    public void writeNotice(String page_owner, String textContents, String imgContents){
        String serverUrl = "http://3.36.159.193/everyLive/mypage/RequestWriteNotice.php";

        SimpleMultiPartRequest smpr= new SimpleMultiPartRequest(Request.Method.POST, serverUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.d("writeNotice", response);

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    Boolean success = jsonObject.getBoolean("success");

                    if(success){
                        String idx_notice = jsonObject.getString("idx_notice");
                        String textContents = jsonObject.getString("textContents");
                        String imgContents = jsonObject.getString("imgContents");
                        String writer = jsonObject.getString("writer");
                        String nickName = jsonObject.getString("nickName");
                        String profileIMG = jsonObject.getString("profileIMG");

                        String writeDate = jsonObject.getString("writeDate");
                        String writeTime = jsonObject.getString("writeTime");

                        Item_notice_writing item_notice_writing = new Item_notice_writing(context);
                        item_notice_writing.setIdx_notice_writing(idx_notice);
                        item_notice_writing.setIdx_writer(writer);
                        item_notice_writing.setWriter_nickName(nickName);
                        item_notice_writing.setWriter_imgUrl(profileIMG);
                        item_notice_writing.setWriteDate(writeDate);
                        item_notice_writing.setTextContents(textContents);
                        item_notice_writing.setImgContents(imgContents);
                        item_notice_writing.setCommentArrayList(null);

                        // 어댑터 안에 있는 어레이리스트에 add 해야한다.
                        adapterNotice.addItem_notice(item_notice_writing);
                        //dataListForNotice.add(0, item_notice_writing);
                        adapterNotice.notifyDataSetChanged();

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
        smpr.addStringParam("textContents", textContents);
        smpr.addStringParam("imgContents", imgContents);

        requestQueue.add(smpr);
    }
}
