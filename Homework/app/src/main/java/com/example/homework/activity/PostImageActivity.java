package com.example.homework.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.homework.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PostImageActivity extends AppCompatActivity {
    private ImageView photo;
    private byte[] fileBuf;
    private LinearLayout post;
    private String uploadFileName;
    private EditText mes;
    private String uploadUrl = "http://47.98.246.55:3000/addFace";
    private Uri imgUri; //记录拍照后的照片文件的地址(临时文件)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_postimage);
        photo = findViewById(R.id.photo);
        post = findViewById(R.id.post);
        mes = findViewById(R.id.mes);
    }

    //按钮点击事件
    public void selectPhoto(View view) {
        String[] permissions = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,permissions,1);
        }else{
            openGallery();
        }
    }

    //获取读写权限
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case 1:
                if(grantResults.length>0 && grantResults[0]==getPackageManager().PERMISSION_GRANTED){
                    openGallery();
                }else{
                    Toast.makeText(this,"读取相册操作被拒绝",Toast.LENGTH_LONG).show();
                }
        }
    }
    //打开相册
    private void openGallery(){
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode){
            case 1:
                handleSelect(data);
            case 2:
                //此时，相机拍照完毕
                if (resultCode == RESULT_OK) {
                    try {
                        System.out.println("拍照完毕");
                        //利用ContentResolver,查询临时文件，并使用BitMapFactory,从输入流中创建BitMap
                        //同样需要配合Provider,在Manifest.xml中加以配置
//                        Bitmap map = BitmapFactory.decodeStream(getContentResolver().openInputStream(imgUri));
//                        photo.setImageBitmap(map);
                        InputStream inputStream = getContentResolver().openInputStream(imgUri);
                        fileBuf=convertToBytes(inputStream);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(fileBuf, 0, fileBuf.length);
                        photo.setImageBitmap(bitmap);
                        post.setVisibility(View.VISIBLE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        }
    }
    //选择后照片的读取
    private void handleSelect(Intent intent){
        Cursor cursor = null;
        Uri uri = intent.getData();

//        if("content".equalsIgnoreCase(uri.getScheme())){
//            cursor= getContentResolver().query(uri,null,null,null,null);
//            if(cursor.moveToFirst()){
//                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
//                String path = cursor.getString(columnIndex);
//                Bitmap bitmap = BitmapFactory.decodeFile(path);
//                photo.setImageBitmap(bitmap);
//            }
//        }else{
//            Log.i("other","其它数据类型");
//        }
//        cursor.close();
        cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
            uploadFileName = cursor.getString(columnIndex);
            System.out.println(uploadFileName);
            System.out.println(columnIndex);
        }
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            fileBuf=convertToBytes(inputStream);
            Bitmap bitmap = BitmapFactory.decodeByteArray(fileBuf, 0, fileBuf.length);
            photo.setImageBitmap(bitmap);
//            Glide.with(this).load(uri)
//                    .fitCenter()
//                    .into(photo);
            post.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        cursor.close();
    }
    private byte[] convertToBytes(InputStream inputStream) throws Exception{
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int len = 0;
        while ((len = inputStream.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        out.close();
        inputStream.close();
        return  out.toByteArray();
    }

    //文件上传的处理
    public void upload(View view){
        new Thread() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                //上传文件域的请求体部分
                RequestBody formBody = RequestBody
                        .create(fileBuf, MediaType.parse("image/jpeg"));
                //整个上传的请求体部分（普通表单+文件上传域）
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("mes", mes.getText().toString())
                        //filename:avatar,originname:abc.jpg
                        .addFormDataPart("avatar", uploadFileName, formBody)
                        .build();
                Request request = new Request.Builder()
                        .url(uploadUrl)
                        .post(requestBody)
                        .build();

                try {
                    Response response = client.newCall(request).execute();
                    JSONObject data =JSON.parseObject(response.body().string());
                    if(data.getString("error_msg").equals("SUCCESS")){
                        Looper.prepare();
                        alertSet();
                        Looper.loop();
                    }
                    System.out.println(data);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
    public void alertSet(){
        new AlertDialog.Builder(PostImageActivity.this)
                .setTitle("结果")
                .setMessage("上传成功")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(PostImageActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                }).create().show();
    }

    public void takePhoto(View view) throws Exception{

        //删除并创建临时文件，用于保存拍照后的照片
        //android 6以后，写Sdcard是危险权限，需要运行时申请，但此处使用的是"关联目录"，无需！
        String[] permissions = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,permissions,1);
        }else {
            File outImg = new File(getExternalCacheDir(), "temp.jpg");
            if (outImg.exists()) outImg.delete();
            outImg.createNewFile();

            //复杂的Uri创建方式
            if (Build.VERSION.SDK_INT >= 24)
                //这是Android 7后，更加安全的获取文件uri的方式（需要配合Provider,在Manifest.xml中加以配置）
                imgUri = FileProvider.getUriForFile(this, "cn.kpy.app1.fileprovider", outImg);
            else
                imgUri = Uri.fromFile(outImg);

            //利用actionName和Extra,启动《相机Activity》
            Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);
            startActivityForResult(intent, 2);
        }
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode,resultCode,data);
//        switch (requestCode) {
//            case 1:
//                //此时，相机拍照完毕
//                if (resultCode == RESULT_OK) {
//                    try {
//                        //利用ContentResolver,查询临时文件，并使用BitMapFactory,从输入流中创建BitMap
//                        //同样需要配合Provider,在Manifest.xml中加以配置
//                        Bitmap map = BitmapFactory.decodeStream(getContentResolver().openInputStream(imgUri));
//                        imgView.setImageBitmap(map);
//                    } catch (FileNotFoundException e) {
//                        e.printStackTrace();
//                    }
//                }
//        }
//    }
}


