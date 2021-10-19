package com.example.everylive.mypage;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.everylive.R;
import com.example.everylive.mypage.Request.RequestGetUseInfo;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;

import de.hdodenhof.circleimageview.CircleImageView;

public class Activity_My extends AppCompatActivity {

    CircleImageView profileIMG;
    TextView userNickName, userID, cnt_fan, cnt_star;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_my);

        profileIMG = findViewById(R.id.profileIMG);
        userNickName = findViewById(R.id.userNickName);
        userID = findViewById(R.id.userID);
        cnt_fan = findViewById(R.id.cnt_fan);
        cnt_star = findViewById(R.id.cnt_star);

        // 쉐어드에서 idx_user 값을 가져와서 DB에서 정보 가져오기.
        SharedPreferences sharedPreferences = this.getSharedPreferences("userInfo", MODE_PRIVATE);
        String idx_user = sharedPreferences.getString("idx_user",null);
        System.out.println(idx_user);
        getUserInfo(idx_user);
    }

    public void getUserInfo(String idx_user){
        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.d("response", response);

                try {
                    JSONObject jsonObject = new JSONObject(response);

                    Boolean success = jsonObject.getBoolean("success");

                    if(success){
                        String nickName_server = jsonObject.getString("nickName");
                        String userID_server = jsonObject.getString("userID");
                        int cnt_fan_server = jsonObject.getInt("cnt_fan");
                        int cnt_star_server = jsonObject.getInt("cnt_star");
                        String profileIMG_server = jsonObject.getString("profileIMG");

                        userNickName.setText(nickName_server);
                        userID.setText(userID_server);
                        cnt_fan.setText(Integer.toString(cnt_fan_server));
                        cnt_star.setText(Integer.toString(cnt_star_server));

                        Glide.with(Activity_My.this)
                                .load(profileIMG_server)
                                .override(400, 400)
                                .centerCrop()
                                .into(profileIMG);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        RequestGetUseInfo requestGetUseInfo = new RequestGetUseInfo(idx_user, responseListener);
        RequestQueue queue = Volley.newRequestQueue(Activity_My.this);
        queue.add(requestGetUseInfo);

    }
}
