package com.example.everylive.livestream;
import android.view.View;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.everylive.R;

public class Activity_Liveendinfo extends AppCompatActivity {

    private static final String TAG = "방송종료";

    TextView totaltime, totalcoin;
    Button btn_ok;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liveendinfo);

        totaltime = findViewById(R.id.totaltime);
        totalcoin = findViewById(R.id.totalcoin);
        btn_ok = findViewById(R.id.btn_ok);


        Intent intent = getIntent();
        String tottime = intent.getStringExtra("totaltime");
        String totcoin = intent.getStringExtra("totalcoin");

        totaltime.setText("방송시간 "+tottime);
        totalcoin.setText("획득코인 "+totcoin);

        btn_ok .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    } // 온크리에이트
}