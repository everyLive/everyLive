package com.example.everylive.mypage.setting;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;
import com.android.volley.toolbox.Volley;
import com.example.everylive.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class Activity_Report extends AppCompatActivity {

    String str_startDate, str_endDate;
    TextView choiceDate, firstDate, totalBroadcastTime, totalCoinCount;
    Button btn_change_nickname;
    ImageView btn_back;

    RecyclerView recycler_live;
    ArrayList<Item_live> itemLiveArrayList;
    AdpaterForLive adpaterForLive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        choiceDate = findViewById(R.id.choiceDate);
        btn_change_nickname = findViewById(R.id.btn_change_nickname);
        btn_back = findViewById(R.id.btn_back);

        firstDate = findViewById(R.id.firstDate);
        totalBroadcastTime = findViewById(R.id.totalBroadcastTime);
        totalCoinCount = findViewById(R.id.totalCoinCount);
        recycler_live = findViewById(R.id.recycler_live);

        itemLiveArrayList = new ArrayList<>();
        adpaterForLive = new AdpaterForLive(itemLiveArrayList);
        recycler_live.setAdapter(adpaterForLive);
        recycler_live.setLayoutManager(new LinearLayoutManager(Activity_Report.this));

        // 날짜 선택기 변수
        MaterialDatePicker.Builder<androidx.core.util.Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
        MaterialDatePicker<androidx.core.util.Pair<Long, Long>> picker = builder.build();

        // 선택했을 때, 값 set하기
        picker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<androidx.core.util.Pair<Long, Long>>() {
            @Override
            public void onPositiveButtonClick(androidx.core.util.Pair<Long, Long> selection) {
                Long startDate = selection.first;
                Long endDate = selection.second;

                DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                str_startDate = df.format(startDate); // 선택한 시작 날짜
                str_endDate = df.format(endDate); // 선택한 종료 날짜

                choiceDate.setText(str_startDate+" ~ "+str_endDate);
            }
        });

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        // 날짜 선택
        choiceDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                picker.show(getSupportFragmentManager(), picker.toString());
            }
        });

        // 조회하기 버튼 : 시작날짜 ~ 종료날짜, idx_user
        btn_change_nickname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(str_startDate!=null&&str_endDate!=null){
                    itemLiveArrayList.clear();
                    getBroadCastInfo(str_startDate, str_endDate,"137"); // 임시로
                    /// ** 쉐어드에서 idx_user 꺼내서 보여주기.
                }else{
                    Toast.makeText(Activity_Report.this,"기간을 입력하세요 !",Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    public void getBroadCastInfo(String startDate, String endDate, String idx_user){
        String serverUrl = "http://3.36.159.193/everyLive/mypage/RequestGetBroadCastInfo.php";

        //파일 전송 요청 객체 생성[결과를 String으로 받음]
        SimpleMultiPartRequest smpr= new SimpleMultiPartRequest(Request.Method.POST, serverUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("getBroadCastInfo", response);

                try {
                    JSONObject jsonObject = new JSONObject(response);

                    Boolean success = jsonObject.getBoolean("success");

                    if(success){
                        String firstLive = jsonObject.getString("firstLive");
                        String currentDate = jsonObject.getString("currentDate");
                        int diff = jsonObject.getInt("diff");
                        String sumLiveTime = jsonObject.getString("sumLiveTime");
                        int sumCoin = jsonObject.getInt("sumCoin");

                        JSONArray jsonArray = jsonObject.getJSONArray("liveData");

                        for(int i=0; i<jsonArray.length(); i++){
                            JSONObject json = jsonArray.getJSONObject(i);

                            String idx_live = json.getString("idx_live");
                            String categoryLive = json.getString("categoryLive");
                            String startDate_origin = json.getString("startDate_origin");
                            String startDate = json.getString("startDate");
                            String startTime = json.getString("startTime");
                            String endTime = json.getString("endTime");
                            String minute = json.getString("minute");
                            String totalTime = json.getString("totalTime");
                            String totalCoin = json.getString("totalCoin");

                            itemLiveArrayList.add(new Item_live(startDate_origin, startDate, totalTime, totalCoin, minute));
                        }

                        setData_firstLive(firstLive, diff, true); // 첫 방송 날짜
                        setData_sum(sumLiveTime, sumCoin); // 총 방송시간, 총 받은 코인수

                    }else{ // 넣을 값이 없다.
                        setData_firstLive("", 0, false);
                        setData_sum("00:00:00",0);
                    }
                    adpaterForLive.notifyDataSetChanged();
                    drawChart(itemLiveArrayList);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });

        //요청 객체에 보낼 데이터를 추가
        smpr.addStringParam("startDate", startDate);
        smpr.addStringParam("endDate", endDate);
        smpr.addStringParam("idx_user", idx_user); // 리포트 보고 싶은 사람 idx

        RequestQueue requestQueue = Volley.newRequestQueue(Activity_Report.this);
        requestQueue.add(smpr);
    }

    public void drawChart(ArrayList<Item_live> itemLiveArrayList){
        BarChart chart = findViewById(R.id.chart);

        ArrayList NoOfEmp = new ArrayList();
        ArrayList x_axis = new ArrayList();

        for(int i=0; i<itemLiveArrayList.size(); i++){
            NoOfEmp.add(new BarEntry(Float.parseFloat(itemLiveArrayList.get(i).getMinute()), i));
            x_axis.add(itemLiveArrayList.get(i).getStartDate());
        }

        BarDataSet bardataset = new BarDataSet(NoOfEmp, "총 방송 시간 (분) ");

        BarData data = new BarData(x_axis, bardataset);
        bardataset.setColor(Color.rgb(249, 170, 51));
        bardataset.setAxisDependency(YAxis.AxisDependency.LEFT);

        chart.setData(data);

        // x축 설정
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        // y축 오른쪽면 비활성화
        YAxis yAxisRight = chart.getAxisRight();
        yAxisRight.setDrawLabels(false);

        // Description 설정 안함
        chart.setDescription(null);

        // 확대하지 못하게 설정
        chart.setTouchEnabled(false);

        // 애니메이션 설정
        //chart.animateY(1000);

        chart.invalidate();
    }

    public void setData_firstLive(String firstLive, int diff, boolean stat){

        if(stat){
            firstDate.setText(firstLive+" ( + "+Integer.toString(diff)+"일 ) ");
        }else{
            firstDate.setText("방송 기록이 없습니다");
        }

    }

    public void setData_sum(String sumLiveTime, int sumCoin){
        totalBroadcastTime.setText(sumLiveTime);
        totalCoinCount.setText(Integer.toString(sumCoin)+"개");
    }
}