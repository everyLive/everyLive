package com.example.everylive.mypage;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.loader.content.CursorLoader;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.everylive.R;
import com.example.everylive.login.Activity_Register;
import com.example.everylive.mypage.Request.RequestGetUseInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class Activity_Mypage_Modify extends AppCompatActivity {

    // IMG1,2,3가 눌리면, 프로필 사진 수정이 된다.
    CircleImageView userProfileIMG1;
    ImageView userProfileIMG2, userProfileIMG3, btn_back;
    EditText nickname, profileMSG;
    Button btn_change_nickname, btn_profile_msg_save;
    TextView tv_set_id, tv_set_gender, tv_set_birthday, btn_change_gender, btn_change_birthday, cnt_text;
    String idx_user;

    // uploadnewProfileIMG()에서 Queue를 공통으로 사용. 전역변수로 빼기.
    RequestQueue requestQueue;

    // 갤러리에서 이미지 업로드
    ActivityResultLauncher<Intent> launcher;
    String absolutePath;

    // 카메라로 사진 찍기
    ActivityResultLauncher<Intent> launcher_camera;
    String mCurrentPhotoPath;
    Bitmap rotatedBitmap = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_mypage_modify);

        requestQueue= Volley.newRequestQueue(Activity_Mypage_Modify.this);

        userProfileIMG1 = findViewById(R.id.userProfileIMG1);
        userProfileIMG2 = findViewById(R.id.userProfileIMG2);
        userProfileIMG3 = findViewById(R.id.userProfileIMG3);
        btn_back = findViewById(R.id.btn_back);
        nickname = findViewById(R.id.nickname);
        profileMSG = findViewById(R.id.profileMSG);
        btn_change_nickname = findViewById(R.id.btn_change_nickname);
        btn_profile_msg_save = findViewById(R.id.btn_profile_msg_save);
        tv_set_id = findViewById(R.id.tv_set_id);
        tv_set_gender = findViewById(R.id.tv_set_gender);
        tv_set_birthday = findViewById(R.id.tv_set_birthday);
        btn_change_gender = findViewById(R.id.btn_change_gender);
        btn_change_birthday = findViewById(R.id.btn_change_birthday);
        cnt_text = findViewById(R.id.cnt_text);

        // 쉐어드에서 idx_user 값을 가져와서 DB에서 정보 가져오기.
        SharedPreferences sharedPreferences = this.getSharedPreferences("userInfo", MODE_PRIVATE);
        idx_user = sharedPreferences.getString("idx_user",null);
        getUserInfo(idx_user);

        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if(result.getResultCode() == RESULT_OK){
                    // 갤러리에서 가져온 이미지의 로컬 URI
                    Uri imgUri = result.getData().getData();
                    uploadnewProfileIMG(imgUri);
                }
            }
        });

        launcher_camera = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK) {
                    File file = new File(mCurrentPhotoPath);
                    Bitmap bitmap = null;
                    ExifInterface ei = null;

                    try {
                        bitmap = MediaStore.Images.Media
                                .getBitmap(getContentResolver(), Uri.fromFile(file));

                        if (bitmap != null) {
                            ei = new ExifInterface(mCurrentPhotoPath);
                            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                                    ExifInterface.ORIENTATION_UNDEFINED);

//                            //사진해상도가 너무 높으면 비트맵으로 로딩
//                            BitmapFactory.Options options = new BitmapFactory.Options();
//                            options.inSampleSize = 8; //8분의 1크기로 비트맵 객체 생성
//                            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);

                            switch (orientation) {

                                case ExifInterface.ORIENTATION_ROTATE_90:
                                    rotatedBitmap = rotateImage(bitmap, 90);
                                    break;

                                case ExifInterface.ORIENTATION_ROTATE_180:
                                    rotatedBitmap = rotateImage(bitmap, 180);
                                    break;

                                case ExifInterface.ORIENTATION_ROTATE_270:
                                    rotatedBitmap = rotateImage(bitmap, 270);
                                    break;

                                case ExifInterface.ORIENTATION_NORMAL:
                                default:
                                    rotatedBitmap = bitmap;
                            }

                            // rotatedBitmap에서 uri추출해서 서버로 업로드.
                            Uri rotatedBitmapUri = getImageUri(Activity_Mypage_Modify.this, rotatedBitmap);
                            uploadnewProfileIMG(rotatedBitmapUri);

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    // 액티비티가 가려졌다 다시 보였을 때, 정보 수정하고 온 것일 수도 있음.
    // userInfo 가져오는 부분 생명주기 고려해서 onStart()에 배치.
    @Override
    protected void onStart() {
        super.onStart();
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
                        String profileIMG_server = jsonObject.getString("profileIMG");
                        String gender_server = jsonObject.getString("gender");
                        String birthday_server = jsonObject.getString("DateOfBirth");
                        String introduce_server = jsonObject.getString("introduce");

                        Glide.with(Activity_Mypage_Modify.this)
                                .load(profileIMG_server)
                                .into(userProfileIMG1);

                        // 닉네임
                        nickname.setText(nickName_server);

                        // 프로필 메시지
                        if(introduce_server.equals("null")){
                            profileMSG.setText("");
                            cnt_text.setText("0");
                        }else{
                            profileMSG.setText(introduce_server);
                            cnt_text.setText(String.valueOf(introduce_server.length())); // 글자수
                        }

                        // 아이디
                        tv_set_id.setText(userID_server);

                        // 성별
                        if(gender_server.equals("M")){
                            tv_set_gender.setText("남성");
                        }else{
                            tv_set_gender.setText("여성");
                        }

                        // 생일
                        tv_set_birthday.setText(birthday_server);

                        // profileMSG이 setText된 이후에 메소드에 리스너를 달아야함.
                        setMethod();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        RequestGetUseInfo requestGetUseInfo = new RequestGetUseInfo(idx_user, responseListener);
        RequestQueue queue = Volley.newRequestQueue(Activity_Mypage_Modify.this);
        queue.add(requestGetUseInfo);
    }

    public void setMethod(){

        // 뒤로가기
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        userProfileIMG1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAlertToEditPhoto();
            }
        });

        userProfileIMG2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAlertToEditPhoto();
            }
        });

        userProfileIMG3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAlertToEditPhoto();
            }
        });

        // 프로필 메세지 글자수 변할 때마다 개수 세기 -> cnt_text에 기록
        profileMSG.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String msg = profileMSG.getText().toString();
                cnt_text.setText(String.valueOf(msg.length()));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        // 닉네임 변경
        // 중복 닉네임이면, alert창 띄우고 원래 닉네임으로 setText
        btn_change_nickname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String serverUrl = "http://3.36.159.193/everyLive/mypage/RequestChangeNickname.php";

                //파일 전송 요청 객체 생성[결과를 String으로 받음]
                SimpleMultiPartRequest smpr= new SimpleMultiPartRequest(Request.Method.POST, serverUrl, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            Boolean success = jsonObject.getBoolean("success");

                            if(success){
                                Boolean duplicate = jsonObject.getBoolean("duplicate");

                                if(duplicate){ // 중복값이 있어서 해당 별명으로 변경 불가능
                                    nickname.setTextColor(Color.parseColor("#F44336"));

                                    Toast.makeText(Activity_Mypage_Modify.this,"다른 별명으로 입력하세요!",Toast.LENGTH_SHORT).show();
                                }else{
                                    String nickname_server = jsonObject.getString("nickname");
                                    nickname.setText(nickname_server);
                                    nickname.setTextColor(Color.parseColor("#FF000000"));

                                    Toast.makeText(Activity_Mypage_Modify.this,"별명 변경 완료!",Toast.LENGTH_SHORT).show();
                                }
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
                smpr.addStringParam("idx_user", idx_user);
                smpr.addStringParam("nickname", nickname.getText().toString());

                RequestQueue requestQueue= Volley.newRequestQueue(Activity_Mypage_Modify.this);
                requestQueue.add(smpr);
            }
        });

        // 프로필 메세지 저장
        btn_profile_msg_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String serverUrl = "http://3.36.159.193/everyLive/mypage/RequestChangeProfileMSG.php";

                //파일 전송 요청 객체 생성[결과를 String으로 받음]
                SimpleMultiPartRequest smpr= new SimpleMultiPartRequest(Request.Method.POST, serverUrl, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            Boolean success = jsonObject.getBoolean("success");

                            if(success){
                                String profileMSG_server = jsonObject.getString("profileMSG");
                                profileMSG.setText(profileMSG_server);

                                Toast.makeText(Activity_Mypage_Modify.this,"프로필 메시지 변경 완료!",Toast.LENGTH_SHORT).show();
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
                smpr.addStringParam("idx_user", idx_user);
                smpr.addStringParam("profileMSG", profileMSG.getText().toString());

                RequestQueue requestQueue= Volley.newRequestQueue(Activity_Mypage_Modify.this);
                requestQueue.add(smpr);
            }
        });

        // 성별 변경
        btn_change_gender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Dialog dialog = new Dialog(Activity_Mypage_Modify.this);
                dialog.setContentView(R.layout.dialog_gender);
                dialog.show();

                final RadioButton radio_male = dialog.findViewById(R.id.radio_male);
                final RadioButton radio_female = dialog.findViewById(R.id.radio_female);
                final TextView btn_cancel = dialog.findViewById(R.id.btn_cancel);
                final TextView btn_change_gender = dialog.findViewById(R.id.btn_change_gender);

                if(tv_set_gender.getText().toString().equals("남성")){
                    radio_male.setChecked(true);
                    radio_female.setChecked(false);
                }else{
                    radio_male.setChecked(true);
                    radio_female.setChecked(true);
                }

                btn_cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });

                btn_change_gender.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // 변경 클릭시, 선택한 성별로 DB UPDATE
                        String serverUrl = "http://3.36.159.193/everyLive/mypage/RequestChangeGender.php";

                        //파일 전송 요청 객체 생성[결과를 String으로 받음]
                        SimpleMultiPartRequest smpr= new SimpleMultiPartRequest(Request.Method.POST, serverUrl, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    Boolean success = jsonObject.getBoolean("success");

                                    if(success){
                                        String gender = jsonObject.getString("gender");

                                        if(gender.equals("M")){
                                            tv_set_gender.setText("남성");
                                        }else{
                                            tv_set_gender.setText("여성");
                                        }
                                    }

                                    dialog.dismiss();

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
                        smpr.addStringParam("idx_user", idx_user);

                        // 남성, 여성 선택 확인
                        if(radio_male.isChecked()){
                            smpr.addStringParam("gender", "M");
                        }else{
                            smpr.addStringParam("gender", "F");
                        }

                        RequestQueue requestQueue= Volley.newRequestQueue(Activity_Mypage_Modify.this);
                        requestQueue.add(smpr);
                    }
                });

            }
        });

        // 생일 변경
        btn_change_birthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Dialog dialog = new Dialog(Activity_Mypage_Modify.this);
                dialog.setContentView(R.layout.dialog_birthday);
                dialog.show();

                final Spinner spinner_month = dialog.findViewById(R.id.spinner_month);
                final Spinner spinner_day = dialog.findViewById(R.id.spinner_day);
                final TextView btn_cancel = dialog.findViewById(R.id.btn_cancel);
                final TextView btn_change_birthday = dialog.findViewById(R.id.btn_change_birthday);

                // 문자열 배열과 기본 스피너 레이아웃을 사용하여 ArrayAdapter 만들기
                ArrayAdapter monthAdapter = ArrayAdapter.createFromResource(Activity_Mypage_Modify.this,
                        R.array.date_month, android.R.layout.simple_spinner_item);
                // 선택목록이 나타날때 사용할 레이아웃을 지정
                monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                // 스피너에 어댑터 적용
                spinner_month.setAdapter(monthAdapter);

                ArrayAdapter dayAdapter = ArrayAdapter.createFromResource(Activity_Mypage_Modify.this,
                        R.array.date_day, android.R.layout.simple_spinner_item);
                dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner_day.setAdapter(dayAdapter);

                // 생일 초기값 설정하기.
                String birthday = tv_set_birthday.getText().toString();
                String[] birthdayArray = birthday.split("-");
                spinner_month.setSelection(Integer.parseInt(birthdayArray[0])-1);
                spinner_day.setSelection(Integer.parseInt(birthdayArray[1])-1);

                // 취소버튼
                btn_cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss(); // 다이얼로그 종료
                    }
                });

                // 변경버튼
                btn_change_birthday.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String selected_month = spinner_month.getSelectedItem().toString();
                        selected_month = selected_month.replaceAll("월", "").trim(); // 뒤에 붙은 문자 ""로 대체 + 생긴 공백 없애기.

                        String selected_day = spinner_day.getSelectedItem().toString();
                        selected_day = selected_day.replaceAll("일", "").trim();

                        if(Integer.parseInt(selected_month) < 10){
                            selected_month = "0"+selected_month;
                        }

                        if(Integer.parseInt(selected_day) < 10){
                            selected_day = "0"+selected_day;
                        }

                        // 변경 클릭시, 선택한 성별로 DB UPDATE
                        String serverUrl = "http://3.36.159.193/everyLive/mypage/RequestChangeBirthday.php";

                        //파일 전송 요청 객체 생성[결과를 String으로 받음]
                        SimpleMultiPartRequest smpr= new SimpleMultiPartRequest(Request.Method.POST, serverUrl, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    Boolean success = jsonObject.getBoolean("success");

                                    if(success){
                                        String birthday_server = jsonObject.getString("birthday");
                                        tv_set_birthday.setText(birthday_server);
                                    }
                                    dialog.dismiss();

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
                        smpr.addStringParam("idx_user", idx_user);
                        smpr.addStringParam("birthday", selected_month+"-"+selected_day);

                        RequestQueue requestQueue= Volley.newRequestQueue(Activity_Mypage_Modify.this);
                        requestQueue.add(smpr);

                    }
                });

                dialog.show();
            }
        });
    }

    public void showAlertToEditPhoto(){
        Dialog dialog = new Dialog(Activity_Mypage_Modify.this);
        dialog.setContentView(R.layout.diaglog_edit_profile_img);
        dialog.show();

        TextView btn_go_camera = dialog.findViewById(R.id.btn_go_camera);
        TextView btn_go_gallery = dialog.findViewById(R.id.btn_go_gallery);

        // 카메라로 사진 찍어서 프로필 바꾸기
        btn_go_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                // 촬영한 사진을 저장할 파일 생성
                File photoFile = null;

                try {
                    //임시로 사용할 파일이므로 경로는 캐시폴더로
                    File tempDir = getCacheDir();

                    //임시촬영파일 세팅
                    String timeStamp = new SimpleDateFormat("yyyyMMdd").format(new Date());
                    String imageFileName = "Capture_" + timeStamp + "_"; //ex) Capture_20201206_

                    File tempImage = File.createTempFile(
                            imageFileName,  /* 파일이름 */
                            ".jpg",         /* 파일형식 */
                            tempDir      /* 경로 */
                    );

                    // ACTION_VIEW 인텐트를 사용할 경로 (임시파일의 경로)
                    mCurrentPhotoPath = tempImage.getAbsolutePath();

                    photoFile = tempImage;

                    //파일이 정상적으로 생성되었다면 계속 진행
                    if (photoFile != null) {
                        //Uri 가져오기
                        Uri photoURI = FileProvider.getUriForFile(Activity_Mypage_Modify.this,
                                getPackageName() + ".fileprovider",
                                photoFile);
                        //인텐트에 Uri담기
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                        //인텐트 실행
                        launcher_camera.launch(takePictureIntent);
                    }
                }catch (IOException e) {
                        Log.w("카메라 에러", "파일 생성 에러!", e);
                }
            }
        });

        // 갤러리에서 사진 선택해서 프로필 바꾸기
        btn_go_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                launcher.launch(intent);

            }
        });

    }

    // uri 절대 경로 구하기
    public String getRealPathFromUri(Uri uri){
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader loader = new CursorLoader(Activity_Mypage_Modify.this, uri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        assert cursor != null;
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String abUri = cursor.getString(column_index);
        cursor.close();

        return abUri;
    }

    // 비트맵에서 Uri 가져오기
    private Uri getImageUri(Context context, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    // 사진 찍었을 때, 이미지 돌아가있으면 보는 위치에 맞게 조정.
    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    public void uploadnewProfileIMG(Uri uri){
        absolutePath = getRealPathFromUri(uri); // 절대경로
        System.out.println("absolutePath : "+absolutePath);

        // 3. 서버에 업로드 : 절대 경로를 보내기.
        String serverUrl = "http://3.36.159.193/everyLive/mypage/RequestEditProfileImg.php";

        SimpleMultiPartRequest smpr= new SimpleMultiPartRequest(Request.Method.POST, serverUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("response", response);

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    Boolean success = jsonObject.getBoolean("success");

                    if(success){
                        String img_server_url = jsonObject.getString("newProfileIMG");

                        // 서버에 저장한 이미지 보여주기.
                        Glide.with(Activity_Mypage_Modify.this)
                                .load(img_server_url)
                                .into(userProfileIMG1);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(Activity_Mypage_Modify.this, "ERROR", Toast.LENGTH_SHORT).show();
            }
        });

        //요청 객체에 보낼 데이터를 추가
        smpr.addStringParam("idx_user", idx_user);
        smpr.addFile("newProfileimg", absolutePath);

        requestQueue.add(smpr);
    }
}
