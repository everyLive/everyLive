package com.example.everylive.home;
import android.content.Intent;
import android.view.View;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.everylive.R;
import com.example.everylive.mypage.Activity_My;

public class Activity_Home extends AppCompatActivity {


    ImageView btn_mypage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        btn_mypage = findViewById(R.id.btn_mypage);


        btn_mypage .setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             /**
              * 인텐트로 액티비티 이동 및 데이터 송신
              */
                     Intent intent = new Intent(getApplicationContext(), Activity_My.class);
                     startActivity(intent);

                 }
                 });
    }
}
