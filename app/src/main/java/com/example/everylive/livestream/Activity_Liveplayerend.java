package com.example.everylive.livestream;
import android.view.View;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.everylive.R;

public class Activity_Liveplayerend extends AppCompatActivity {

   private static final String TAG = "";

   Button btn_end;

   @Override
   protected void onCreate(Bundle savedInstanceState) {

       super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_liveplayerend);

       btn_end = findViewById(R.id.btn_end);

       btn_end.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {

               finish();
           }
       });


   } // 온크리에이트
}
