package com.example.everylive.login;

public class b {

    /// 수미

    /**
     * 인텐트로 액티비티 이동 및 데이터 송신
     */
            Intent intent = new Intent(getApplicationContext(), .class);
            startActivity(intent);
            intent.putExtra("key",value);
    /**
     * 인텐트로 데이터 수신
     */
            Intent intent = getIntent();
            String uri = intent.getStringExtra("key");
}
