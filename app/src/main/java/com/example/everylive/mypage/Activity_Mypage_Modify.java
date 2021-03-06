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

    // IMG1,2,3??? ?????????, ????????? ?????? ????????? ??????.
    CircleImageView userProfileIMG1;
    ImageView userProfileIMG2, userProfileIMG3, btn_back;
    EditText nickname, profileMSG;
    Button btn_change_nickname, btn_profile_msg_save;
    TextView tv_set_id, tv_set_gender, tv_set_birthday, btn_change_gender, btn_change_birthday, cnt_text;
    String idx_user;

    // uploadnewProfileIMG()?????? Queue??? ???????????? ??????. ??????????????? ??????.
    RequestQueue requestQueue;

    // ??????????????? ????????? ?????????
    ActivityResultLauncher<Intent> launcher;
    String absolutePath;

    // ???????????? ?????? ??????
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

        // ??????????????? idx_user ?????? ???????????? DB?????? ?????? ????????????.
        SharedPreferences sharedPreferences = this.getSharedPreferences("userInfo", MODE_PRIVATE);
        idx_user = sharedPreferences.getString("idx_user",null);
        getUserInfo(idx_user);

        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if(result.getResultCode() == RESULT_OK){
                    // ??????????????? ????????? ???????????? ?????? URI
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

//                            //?????????????????? ?????? ????????? ??????????????? ??????
//                            BitmapFactory.Options options = new BitmapFactory.Options();
//                            options.inSampleSize = 8; //8?????? 1????????? ????????? ?????? ??????
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

                            // rotatedBitmap?????? uri???????????? ????????? ?????????.
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

    // ??????????????? ???????????? ?????? ????????? ???, ?????? ???????????? ??? ?????? ?????? ??????.
    // userInfo ???????????? ?????? ???????????? ???????????? onStart()??? ??????.
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

                        // ?????????
                        nickname.setText(nickName_server);

                        // ????????? ?????????
                        if(introduce_server.equals("null")){
                            profileMSG.setText("");
                            cnt_text.setText("0");
                        }else{
                            profileMSG.setText(introduce_server);
                            cnt_text.setText(String.valueOf(introduce_server.length())); // ?????????
                        }

                        // ?????????
                        tv_set_id.setText(userID_server);

                        // ??????
                        if(gender_server.equals("M")){
                            tv_set_gender.setText("??????");
                        }else{
                            tv_set_gender.setText("??????");
                        }

                        // ??????
                        tv_set_birthday.setText(birthday_server);

                        // profileMSG??? setText??? ????????? ???????????? ???????????? ????????????.
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

        // ????????????
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

        // ????????? ????????? ????????? ?????? ????????? ?????? ?????? -> cnt_text??? ??????
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

        // ????????? ??????
        // ?????? ???????????????, alert??? ????????? ?????? ??????????????? setText
        btn_change_nickname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String serverUrl = "http://3.36.159.193/everyLive/mypage/RequestChangeNickname.php";

                //?????? ?????? ?????? ?????? ??????[????????? String?????? ??????]
                SimpleMultiPartRequest smpr= new SimpleMultiPartRequest(Request.Method.POST, serverUrl, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            Boolean success = jsonObject.getBoolean("success");

                            if(success){
                                Boolean duplicate = jsonObject.getBoolean("duplicate");

                                if(duplicate){ // ???????????? ????????? ?????? ???????????? ?????? ?????????
                                    nickname.setTextColor(Color.parseColor("#F44336"));

                                    Toast.makeText(Activity_Mypage_Modify.this,"?????? ???????????? ???????????????!",Toast.LENGTH_SHORT).show();
                                }else{
                                    String nickname_server = jsonObject.getString("nickname");
                                    nickname.setText(nickname_server);
                                    nickname.setTextColor(Color.parseColor("#FF000000"));

                                    Toast.makeText(Activity_Mypage_Modify.this,"?????? ?????? ??????!",Toast.LENGTH_SHORT).show();
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("ERROR", "?????? ??????");
                    }
                });

                //?????? ????????? ?????? ???????????? ??????
                smpr.addStringParam("idx_user", idx_user);
                smpr.addStringParam("nickname", nickname.getText().toString());

                RequestQueue requestQueue= Volley.newRequestQueue(Activity_Mypage_Modify.this);
                requestQueue.add(smpr);
            }
        });

        // ????????? ????????? ??????
        btn_profile_msg_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String serverUrl = "http://3.36.159.193/everyLive/mypage/RequestChangeProfileMSG.php";

                //?????? ?????? ?????? ?????? ??????[????????? String?????? ??????]
                SimpleMultiPartRequest smpr= new SimpleMultiPartRequest(Request.Method.POST, serverUrl, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            Boolean success = jsonObject.getBoolean("success");

                            if(success){
                                String profileMSG_server = jsonObject.getString("profileMSG");
                                profileMSG.setText(profileMSG_server);

                                Toast.makeText(Activity_Mypage_Modify.this,"????????? ????????? ?????? ??????!",Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("ERROR", "?????? ??????");
                    }
                });

                //?????? ????????? ?????? ???????????? ??????
                smpr.addStringParam("idx_user", idx_user);
                smpr.addStringParam("profileMSG", profileMSG.getText().toString());

                RequestQueue requestQueue= Volley.newRequestQueue(Activity_Mypage_Modify.this);
                requestQueue.add(smpr);
            }
        });

        // ?????? ??????
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

                if(tv_set_gender.getText().toString().equals("??????")){
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
                        // ?????? ?????????, ????????? ????????? DB UPDATE
                        String serverUrl = "http://3.36.159.193/everyLive/mypage/RequestChangeGender.php";

                        //?????? ?????? ?????? ?????? ??????[????????? String?????? ??????]
                        SimpleMultiPartRequest smpr= new SimpleMultiPartRequest(Request.Method.POST, serverUrl, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    Boolean success = jsonObject.getBoolean("success");

                                    if(success){
                                        String gender = jsonObject.getString("gender");

                                        if(gender.equals("M")){
                                            tv_set_gender.setText("??????");
                                        }else{
                                            tv_set_gender.setText("??????");
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
                                Log.d("ERROR", "?????? ??????");
                            }
                        });

                        //?????? ????????? ?????? ???????????? ??????
                        smpr.addStringParam("idx_user", idx_user);

                        // ??????, ?????? ?????? ??????
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

        // ?????? ??????
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

                // ????????? ????????? ?????? ????????? ??????????????? ???????????? ArrayAdapter ?????????
                ArrayAdapter monthAdapter = ArrayAdapter.createFromResource(Activity_Mypage_Modify.this,
                        R.array.date_month, android.R.layout.simple_spinner_item);
                // ??????????????? ???????????? ????????? ??????????????? ??????
                monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                // ???????????? ????????? ??????
                spinner_month.setAdapter(monthAdapter);

                ArrayAdapter dayAdapter = ArrayAdapter.createFromResource(Activity_Mypage_Modify.this,
                        R.array.date_day, android.R.layout.simple_spinner_item);
                dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner_day.setAdapter(dayAdapter);

                // ?????? ????????? ????????????.
                String birthday = tv_set_birthday.getText().toString();
                String[] birthdayArray = birthday.split("-");
                spinner_month.setSelection(Integer.parseInt(birthdayArray[0])-1);
                spinner_day.setSelection(Integer.parseInt(birthdayArray[1])-1);

                // ????????????
                btn_cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss(); // ??????????????? ??????
                    }
                });

                // ????????????
                btn_change_birthday.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String selected_month = spinner_month.getSelectedItem().toString();
                        selected_month = selected_month.replaceAll("???", "").trim(); // ?????? ?????? ?????? ""??? ?????? + ?????? ?????? ?????????.

                        String selected_day = spinner_day.getSelectedItem().toString();
                        selected_day = selected_day.replaceAll("???", "").trim();

                        if(Integer.parseInt(selected_month) < 10){
                            selected_month = "0"+selected_month;
                        }

                        if(Integer.parseInt(selected_day) < 10){
                            selected_day = "0"+selected_day;
                        }

                        // ?????? ?????????, ????????? ????????? DB UPDATE
                        String serverUrl = "http://3.36.159.193/everyLive/mypage/RequestChangeBirthday.php";

                        //?????? ?????? ?????? ?????? ??????[????????? String?????? ??????]
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
                                Log.d("ERROR", "?????? ??????");
                            }
                        });

                        //?????? ????????? ?????? ???????????? ??????
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

        // ???????????? ?????? ????????? ????????? ?????????
        btn_go_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                // ????????? ????????? ????????? ?????? ??????
                File photoFile = null;

                try {
                    //????????? ????????? ??????????????? ????????? ???????????????
                    File tempDir = getCacheDir();

                    //?????????????????? ??????
                    String timeStamp = new SimpleDateFormat("yyyyMMdd").format(new Date());
                    String imageFileName = "Capture_" + timeStamp + "_"; //ex) Capture_20201206_

                    File tempImage = File.createTempFile(
                            imageFileName,  /* ???????????? */
                            ".jpg",         /* ???????????? */
                            tempDir      /* ?????? */
                    );

                    // ACTION_VIEW ???????????? ????????? ?????? (??????????????? ??????)
                    mCurrentPhotoPath = tempImage.getAbsolutePath();

                    photoFile = tempImage;

                    //????????? ??????????????? ?????????????????? ?????? ??????
                    if (photoFile != null) {
                        //Uri ????????????
                        Uri photoURI = FileProvider.getUriForFile(Activity_Mypage_Modify.this,
                                getPackageName() + ".fileprovider",
                                photoFile);
                        //???????????? Uri??????
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                        //????????? ??????
                        launcher_camera.launch(takePictureIntent);
                    }
                }catch (IOException e) {
                        Log.w("????????? ??????", "?????? ?????? ??????!", e);
                }
            }
        });

        // ??????????????? ?????? ???????????? ????????? ?????????
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

    // uri ?????? ?????? ?????????
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

    // ??????????????? Uri ????????????
    private Uri getImageUri(Context context, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    // ?????? ????????? ???, ????????? ?????????????????? ?????? ????????? ?????? ??????.
    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    public void uploadnewProfileIMG(Uri uri){
        absolutePath = getRealPathFromUri(uri); // ????????????
        System.out.println("absolutePath : "+absolutePath);

        // 3. ????????? ????????? : ?????? ????????? ?????????.
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

                        // ????????? ????????? ????????? ????????????.
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

        //?????? ????????? ?????? ???????????? ??????
        smpr.addStringParam("idx_user", idx_user);
        smpr.addFile("newProfileimg", absolutePath);

        requestQueue.add(smpr);
    }
}
