package com.example.everylive.payment;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;
import com.android.volley.toolbox.Volley;
import com.example.everylive.R;
import com.example.everylive.mypage.Activity_My;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import kr.co.bootpay.Bootpay;
import kr.co.bootpay.BootpayAnalytics;
import kr.co.bootpay.enums.Method;
import kr.co.bootpay.enums.PG;
import kr.co.bootpay.enums.UX;
import kr.co.bootpay.listener.CancelListener;
import kr.co.bootpay.listener.CloseListener;
import kr.co.bootpay.listener.ConfirmListener;
import kr.co.bootpay.listener.DoneListener;
import kr.co.bootpay.listener.ErrorListener;
import kr.co.bootpay.listener.ReadyListener;
import kr.co.bootpay.model.BootExtra;
import kr.co.bootpay.model.BootUser;

public class Activity_Selectcoin extends AppCompatActivity {

   private static final String TAG = "";


   TextView mycoin,coin50, coin100, coin300;
   Button btn_payment;
   Boolean co50, co100, co300;

   String muchSpend, coinname;
   int coinprice;
   private int stuck = 10;
   Context mContext;

    // 서버 주소
    private static String IP_ADDRESS = "http://3.36.159.193";

    // 현재시간 나타내기
    long mNow;
    Date mDate;
    SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_selectcoin);

        // 초기설정 - 해당 프로젝트(안드로이드)의 application id 값을 설정합니다. 결제와 통계를 위해 꼭 필요합니다.
        BootpayAnalytics.init(getApplicationContext(), "6177f8fd7b5ba4b3a352a342");


       coin50 = findViewById(R.id.coin50);
       coin100 = findViewById(R.id.coin100);
       coin300 = findViewById(R.id.coin300);
       btn_payment = findViewById(R.id.btn_payment);

       // 디폴트값, 결제하기 눌렀을때 전부 false면 선택먼저 하라고 알려줘야함.
       co50 = false;
       co100 = false;
       co300 = false;




       // 클릭했을때 뭘 클릭했는지 표시해줌 & 결제하기 버튼 누르기전 거쳐야 하는 작업
       coin50.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            // 클릭했으면 체크표시
            coin50.setBackgroundResource(R.drawable.boldline_red);
            coin100.setBackgroundResource(R.drawable.boldline_white);
            coin300.setBackgroundResource(R.drawable.boldline_white);

            // 결제하기 버튼 눌렀을때 true 켜져있는 부분 동작되어야 하
            co50 = true;
            co100 = false;
            co300 = false;
            Log.d(TAG, co50.toString());
            Log.d(TAG, co100.toString());
            Log.d(TAG, co300.toString());

        }
       });
       coin100.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {

               coin100.setBackgroundResource(R.drawable.boldline_red);
               coin50.setBackgroundResource(R.drawable.boldline_white);
               coin300.setBackgroundResource(R.drawable.boldline_white);
               co50 = false;
               co100 = true;
               co300 = false;
               Log.d(TAG, co50.toString());
               Log.d(TAG, co100.toString());
               Log.d(TAG, co300.toString());

           }
       });
       coin300.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {

               coin300.setBackgroundResource(R.drawable.boldline_red);
               coin50.setBackgroundResource(R.drawable.boldline_white);
               coin100.setBackgroundResource(R.drawable.boldline_white);
               co50 = false;
               co100 = false;
               co300 = true;
               Log.d(TAG, co50.toString());
               Log.d(TAG, co100.toString());
               Log.d(TAG, co300.toString());
           }
       });

       // 결제하기 버튼 누르면 물건 체크 했는지 확인하고, 체크 되어 있으면 해당 물건에 맞는 결제창으로 이동한다.
       btn_payment.setOnClickListener(new View.OnClickListener() {

           @Override
           public void onClick(View v) {

               if(co50==false && co100 == false && co300 == false){
                   Toast.makeText(Activity_Selectcoin.this,"구매할 코인을 선택해주세요", Toast.LENGTH_LONG).show();

               }else if(co50==true){
                   coinname = "50";
                   coinprice = 5500;
                   coin_request();
               }else if(co100==true){
                   coinname = "100";
                   coinprice = 11000;
                   coin_request();
               }else if(co300==true){
                   coinname = "300";
                   coinprice = 33000;
                   coin_request();
               }


           }
       });

   } // 온크리에이트



    //현재시간 나타내는 메소드
    private String getTime(){
        mNow = System.currentTimeMillis();
        mDate = new Date(mNow);
        return mFormat.format(mDate);
    }







    public void coin_request() {
        // 결제호출
        BootUser bootUser = new BootUser().setPhone("010-1234-5678");
        BootExtra bootExtra = new BootExtra().setQuotas(new int[] {0,2,3});

        Bootpay.init(getFragmentManager())
                .setApplicationId("6177f8fd7b5ba4b3a352a342") // 해당 프로젝트(안드로이드)의 application id 값
                .setPG(PG.INICIS) // 결제할 PG 사
                .setMethod(Method.CARD) // 결제수단
                .setContext(getApplicationContext())
                .setBootUser(bootUser)
                .setBootExtra(bootExtra)
                .setUX(UX.PG_DIALOG)
//                .setUserPhone("010-1234-5678") // 구매자 전화번호
                .setName(coinname) // 결제할 상품명
                .setOrderId("1234") // 결제 고유번호expire_month
                .setPrice(coinprice) // 결제할 금액
                .addItem("코인", 1, "ITEM_CODE_COIN", 100) // 주문정보에 담길 상품정보, 통계를 위해 사용
//                .addItem("키보드", 1, "ITEM_CODE_KEYBOARD", 200, "패션", "여성상의", "블라우스") // 주문정보에 담길 상품정보, 통계를 위해 사용
                .onConfirm(new ConfirmListener() { // 결제가 진행되기 바로 직전 호출되는 함수로, 주로 재고처리 등의 로직이 수행
                    @Override
                    public void onConfirm(@Nullable String message) {

                        if (0 < stuck) Bootpay.confirm(message); // 재고가 있을 경우.
                        else Bootpay.removePaymentWindow(); // 재고가 없어 중간에 결제창을 닫고 싶을 경우
                        Log.d("confirm", message);

                        //결제 승인이 되기 전 호출되는 함수입니다.
                        //승인 이전 관련 로직을 서버 혹은 클라이언트에서 수행 후 결제를 승인해도 될 경우
                        //Bootpay.confirm(data);
                        //코드를 실행해주시면 PG에서 결제 승인이 진행이 됩니다.
                    }
                })
                .onReady(new ReadyListener() { // 가상계좌 입금 계좌번호가 발급되면 호출되는 함수입니다.
                    @Override
                    public void onReady(@Nullable String message) {
                        Log.d("ready", message);
                    }
                })
                .onDone(new DoneListener() { // 결제완료시 호출, 아이템 지급 등 데이터 동기화 로직을 수행합니다
                    @Override
                    public void onDone(@Nullable String message) {
                        Log.d("done", message);
                        Log.d(TAG, "onDone: ");

                        try {
                            JSONObject jsonObject = new JSONObject(message);
                            muchSpend = jsonObject.getString("item_name");

                            finish();

                            sendbuyData();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                        //PG에서 거래 승인 이후에 호출 되는 함수입니다.
                        // 결제 완료 후 다음 결제 결과를 호출 할 수 있는 함수 입니다.
                    }
                })
                .onCancel(new CancelListener() { // 결제 취소시 호출
                    @Override
                    public void onCancel(@Nullable String message) {
                        Log.d("cancel", message);
                        Log.d(TAG, "onCancel: ");
//                        Toast.makeText(Activity_Selectcoin.this,"결제취소", Toast.LENGTH_LONG).show();
                        //결제 진행 중 사용자가 PG 결제창에서 취소 혹은 닫기 버튼을 눌러 나온 경우
                    }
                })
                .onError(new ErrorListener() { // 에러가 났을때 호출되는 부분
                    @Override
                    public void onError(@Nullable String message) {
                        Log.d("error", message);
                        Toast.makeText(Activity_Selectcoin.this,"에러", Toast.LENGTH_LONG).show();

                        //부트페이 관리자에서 활성화 하지 않은 PG, 결제수단을 사용하고자 할 때
                        //PG에서 보내온 결제 정보를 부트페이 관리자에 잘못 입력하거나 입력하지 않은 경우
                        //결제 진행 도중 한도초과, 카드정지, 휴대폰소액결제 막힘, 계좌이체 불가 등의 사유로 결제가 안되는 경우
                        //PG에서 리턴된 값이 다른 Client에 의해 변조된 경우
                    }
                })
                .onClose(
                        new CloseListener() { //결제창이 닫힐때 실행되는 부분
                            @Override
                            public void onClose(String message) {
                                Log.d("close", "close");
//                                Toast.makeText(Activity_Selectcoin.this,"결제창닫힘", Toast.LENGTH_LONG).show();

                            }
                        })
                .request();
    }



    public void sendbuyData() {
            SharedPreferences sharedPref = getApplicationContext().getSharedPreferences( // 쉐어드
            "userInfo", Context.MODE_PRIVATE);

            //서버로 보낼 데이터
            String idx_user = sharedPref.getString("idx_user",null);
            String timeStamp = getTime();

            //안드로이드에서 보낼 데이터를 받을 php 서버 주소
            String serverUrl=IP_ADDRESS + "/everyLive/payment/buycoin.php";

            //파일 전송 요청 객체 생성[결과를 String으로 받음]
            SimpleMultiPartRequest smpr= new SimpleMultiPartRequest(Request.Method.POST, serverUrl, new Response.Listener<String>() {
    @Override
    public void onResponse(String response) {

            }
            }, new Response.ErrorListener() {
    @Override
    public void onErrorResponse(VolleyError error) {
    //                Toast.makeText(Createband_2.this, "ERROR", Toast.LENGTH_SHORT).show();
            }
            });

            //요청 객체에 보낼 데이터를 추가
            smpr.addStringParam("idx_user", idx_user);
            smpr.addStringParam("muchSpend", muchSpend);
            smpr.addStringParam("timeStamp", timeStamp);

            //요청객체를 서버로 보낼 우체통 같은 객체 생성
            RequestQueue requestQueue= Volley.newRequestQueue(this);
            requestQueue.add(smpr);

            }

}

