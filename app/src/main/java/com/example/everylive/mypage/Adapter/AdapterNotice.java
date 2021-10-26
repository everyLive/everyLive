package com.example.everylive.mypage.Adapter;

import static android.content.Context.MODE_PRIVATE;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
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
import com.example.everylive.mypage.Activity_Mypage_Modify;
import com.example.everylive.mypage.Item.Item_notice_Comment;
import com.example.everylive.mypage.Item.Item_notice_writing;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterNotice extends RecyclerView.Adapter<AdapterNotice.MyViewHolder> {

    RequestQueue requestQueue;
    Context context;
    private ArrayList<Item_notice_writing> dataSet;
    String idx_viewer;

    public AdapterNotice(Context context, ArrayList<Item_notice_writing> dataSet){
        requestQueue = Volley.newRequestQueue(context);
        this.context = context;
        this.dataSet = dataSet;

        SharedPreferences sharedPreferences = context.getSharedPreferences("userInfo", MODE_PRIVATE);
        idx_viewer = sharedPreferences.getString("idx_user",null);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notice ,parent,false);
        return new MyViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Item_notice_writing currentItem = dataSet.get(position);

        holder.local_array_position = holder.getAdapterPosition();

        String idx_notice_writing = currentItem.getIdx_notice_writing(); // 공지글 idx
        String writer_imgUrl = currentItem.getWriter_imgUrl(); // 공지글 작성자 프로필 이미지
        String writer_nickName = currentItem.getWriter_nickName(); // 공지글 작성자 닉네임
        String write_date = currentItem.getWriteDate(); // 공지글 쓴 날짜
        String write_contents_text = currentItem.getTextContents(); // 공지글 내용
        String write_contents_img = currentItem.getImgContents(); // 공지글 이미지

        holder.idx_notice_writing = idx_notice_writing;
        Glide.with(context).load(writer_imgUrl).into(holder.notice_writer_img);
        holder.notice_writer_nickName.setText(writer_nickName);
        holder.notice_write_date.setText(write_date);
        holder.notice_content_text.setText(write_contents_text);

        if(write_contents_img.equals("0")){
            holder.notice_content_img.setVisibility(View.GONE);
        }else{
            Glide.with(context).load(write_contents_img).into(holder.notice_content_img);
        }

        // 댓글 작성자 관련
        holder.idx_comment_writer = currentItem.getIdx_comment_writer();
        Glide.with(context).load(currentItem.get_comment_imgUrl()).into(holder.notice_userImg);

        // 댓글창 기본으로 닫혀있는 상태.
        holder.checkbox_comment.setChecked(false);
        holder.comment_area.setVisibility(View.GONE);

        // 댓글 내용
        if(currentItem.getCommentArrayList() == null){ // 댓글이 없다
            holder.arrayList = new ArrayList<>();
        }else{
            holder.arrayList = currentItem.getCommentArrayList();
        }

        // 댓글 리사이클러뷰에 어댑터랑 레이아웃 매니저 설정하기.
        AdapterNoticeComment adapterNoticeComment = new AdapterNoticeComment(context, holder.arrayList);
        holder.recycler_comments.setAdapter(adapterNoticeComment);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        holder.recycler_comments.setLayoutManager(linearLayoutManager);

        // 댓글 남기기
        holder.btn_notice_commentMSG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String comment = holder.notice_comment_MSG.getText().toString();
                holder.notice_comment_MSG.setText("");

                saveCommentMSG(idx_notice_writing, holder.idx_comment_writer, comment, holder.arrayList, holder.adapterNoticeComment);
            }
        });

        if(currentItem.getIdx_writer().equals(idx_viewer)) { // 공지글 단 사람 = 로그인한 사람 -> 삭제가능
            holder.btn_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialogDeleteComment(holder.idx_notice_writing, holder.local_array_position);
                }
            });
        }else{ // 내가 단 댓글이 아니면, 수정 버튼 가리기.
            holder.btn_delete.setVisibility(View.INVISIBLE);
        }
    }

    private void dialogDeleteComment(String idx_notice_writing, int position){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("확인 메세지"); // 제목
        builder.setMessage("공지글을 삭제할까요?"); // 내용
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                requestRemoveComment(idx_notice_writing, position);
            }
        });
        builder.setNeutralButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                return;
            }
        });
        builder.create().show();
    }

    private void requestRemoveComment(String idx_notice_writing, int position){
        String serverUrl = "http://3.36.159.193/everyLive/mypage/RequestRemoveNotice.php";

        //파일 전송 요청 객체 생성[결과를 String으로 받음]
        SimpleMultiPartRequest smpr= new SimpleMultiPartRequest(Request.Method.POST, serverUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("RemoveComment", response);

                try {
                    JSONObject jsonObject = new JSONObject(response);

                    Boolean success = jsonObject.getBoolean("success");

                    if(success){
                        dataSet.remove(position);
                        notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("ERROR", "성별 변경");
            }
        });

        //요청 객체에 보낼 데이터를 추가
        smpr.addStringParam("idx_notice", idx_notice_writing); // 공지글 idx : idx_writing

        requestQueue.add(smpr);
    }

    // 댓글 달기
    public void saveCommentMSG(String idx_notice_writing, String idx_comment_writer, String commentContents, ArrayList<Item_notice_Comment> commentArrayList, AdapterNoticeComment adapterNoticeComment){
        String serverUrl = "http://3.36.159.193/everyLive/mypage/RequestSaveComment.php";

        //파일 전송 요청 객체 생성[결과를 String으로 받음]
        SimpleMultiPartRequest smpr= new SimpleMultiPartRequest(Request.Method.POST, serverUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("saveCommentMSG", response);

                try {
                    JSONObject jsonObject = new JSONObject(response);

                    Boolean success = jsonObject.getBoolean("success");

                    if(success){
                        String idx_comments = jsonObject.getString("idx_comments");
                        String textComments = jsonObject.getString("textComments");
                        String idx_writing = jsonObject.getString("idx_writing");
                        String writer = jsonObject.getString("writer");
                        String writeDate = jsonObject.getString("writeDate");
                        String writeTime = jsonObject.getString("writeTime");
                        String commentType = jsonObject.getString("commentType");
                        String idx_user = jsonObject.getString("idx_user");
                        String nickName = jsonObject.getString("nickName");
                        String profileIMG = jsonObject.getString("profileIMG");

                        // 디비 저장된 값을 로컬에서 array에 추가하기.
                        Item_notice_Comment item_notice_comment = new Item_notice_Comment(idx_comments, idx_user, profileIMG, nickName, writeDate, textComments);
                        commentArrayList.add(item_notice_comment);
                        adapterNoticeComment.notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("ERROR", "성별 변경");
            }
        });

        //요청 객체에 보낼 데이터를 추가
        smpr.addStringParam("idx_writing", idx_notice_writing); // 공지글 idx : idx_writing
        smpr.addStringParam("writer", idx_comment_writer); // 댓글 작성자 idx : writer
        smpr.addStringParam("textComments", commentContents); // 댓글내용 : textComments
        smpr.addStringParam("type", "notice"); // type : notice

        requestQueue.add(smpr);
    }

    public void addItem_notice(Item_notice_writing item_notice_writing){
        dataSet.add(0, item_notice_writing);
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        int local_array_position;
        String idx_notice_writing;

        CircleImageView notice_writer_img;
        TextView notice_writer_nickName, notice_write_date, notice_content_text;
        ImageView btn_delete, notice_content_img;
        CheckBox checkbox_comment;
        LinearLayout comment_area;

        // 댓글달기
        String idx_comment_writer; // 댓글 남기는 사람 idx
        CircleImageView notice_userImg; // 댓글 남기는 사람 이미지 프로필
        EditText notice_comment_MSG; // 댓글 내용
        ImageView btn_notice_commentMSG; // 댓글 달기 버튼

        RecyclerView recycler_comments; // 댓글 리사이클러뷰
        ArrayList<Item_notice_Comment> arrayList;
        AdapterNoticeComment adapterNoticeComment;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            notice_writer_img = itemView.findViewById(R.id.notice_writer_img);
            notice_writer_nickName = itemView.findViewById(R.id.notice_writer_nickName);
            notice_write_date = itemView.findViewById(R.id.notice_write_date);
            notice_content_text = itemView.findViewById(R.id.notice_content_text);
            btn_delete = itemView.findViewById(R.id.btn_delete);
            notice_content_img = itemView.findViewById(R.id.notice_content_img);
            checkbox_comment = itemView.findViewById(R.id.checkbox_comment);
            recycler_comments = itemView.findViewById(R.id.recycler_comments);
            notice_userImg = itemView.findViewById(R.id.notice_userImg);
            notice_comment_MSG = itemView.findViewById(R.id.notice_comment_MSG);
            btn_notice_commentMSG = itemView.findViewById(R.id.btn_notice_commentMSG);
            comment_area = itemView.findViewById(R.id.comment_area);

            // 댓글창 여닫기
            checkbox_comment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(checkbox_comment.isChecked()){
                        comment_area.setVisibility(View.VISIBLE);
                        notice_userImg.bringToFront(); // EditText에 가져진다.

                        adapterNoticeComment = new AdapterNoticeComment(context, arrayList); // 여기가 동작을 안하는게 아닌가?
                        recycler_comments.setAdapter(adapterNoticeComment);
                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
                        recycler_comments.setLayoutManager(linearLayoutManager);
                    }else{
                        comment_area.setVisibility(View.GONE);
                    }
                }
            });

        }
    }
}
