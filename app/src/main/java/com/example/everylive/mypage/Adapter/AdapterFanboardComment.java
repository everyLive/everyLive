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
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.everylive.R;
import com.example.everylive.mypage.Item.Item_notice_Comment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterFanboardComment extends RecyclerView.Adapter<AdapterFanboardComment.MyViewHolder>{

    RequestQueue requestQueue;
    Context context;
    ArrayList<Item_notice_Comment> commentArrayList;
    String idx_viewer;

    public AdapterFanboardComment(Context context, ArrayList<Item_notice_Comment> commentArrayList){
        requestQueue = Volley.newRequestQueue(context);
        this.context = context;
        this.commentArrayList = commentArrayList;

        SharedPreferences sharedPreferences = context.getSharedPreferences("userInfo", MODE_PRIVATE);
        idx_viewer = sharedPreferences.getString("idx_user",null);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment ,parent,false);
        return new MyViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Item_notice_Comment currentItem = commentArrayList.get(position);

        holder.local_array_position = holder.getAdapterPosition();
        holder.idx_comment = currentItem.getIdx_comment();
        holder.commentContents.setText(currentItem.getCommentContents());
        holder.comment_write_date.setText(currentItem.getWriteDate());
        Glide.with(context).load(currentItem.getUserImgUrl()).into(holder.comment_userImg);
        holder.comment_writer_nickName.setText(currentItem.getUserNickName());

        if(currentItem.getIdx_writer().equals(idx_viewer)) { // 댓글 단 사람 = 로그인한 사람 -> 삭제가능
            holder.btn_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialogDeleteComment(holder.idx_comment, holder.local_array_position);
                }
            });
        }else{ // 내가 단 댓글이 아니면, 수정 버튼 가리기.
            holder.btn_delete.setVisibility(View.INVISIBLE);
        }
    }

    private void dialogDeleteComment(String idx_comment, int position){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("확인 메세지"); // 제목
        builder.setMessage("댓글을 삭제할까요?"); // 내용
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                requestRemoveComment(idx_comment, position);
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

    private void requestRemoveComment(String idx_comment, int position){
        String serverUrl = "http://3.36.159.193/everyLive/mypage/RequestRemoveComment.php";

        //파일 전송 요청 객체 생성[결과를 String으로 받음]
        SimpleMultiPartRequest smpr= new SimpleMultiPartRequest(Request.Method.POST, serverUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("RemoveComment", response);

                try {
                    JSONObject jsonObject = new JSONObject(response);

                    Boolean success = jsonObject.getBoolean("success");

                    if(success){
                        commentArrayList.remove(position);
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
        smpr.addStringParam("idx_comment", idx_comment); // 공지글 idx : idx_writing
        smpr.addStringParam("type", "fanboard"); // 댓글 작성자 idx : writer

        requestQueue.add(smpr);
    }

    @Override
    public int getItemCount() {
        //return 0; -> onBindViewHolder() 작동안함.
        return commentArrayList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        int local_array_position;

        String idx_comment;
        CircleImageView comment_userImg;
        TextView comment_writer_nickName, comment_write_date, commentContents;
        ImageView btn_delete;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            comment_userImg = itemView.findViewById(R.id.comment_userImg);
            comment_writer_nickName = itemView.findViewById(R.id.comment_writer_nickName);
            comment_write_date = itemView.findViewById(R.id.comment_write_date);
            commentContents = itemView.findViewById(R.id.commentContents);
            btn_delete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
