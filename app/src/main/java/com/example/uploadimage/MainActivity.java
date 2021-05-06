package com.example.uploadimage;

import androidx.annotation.LongDef;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static androidx.core.content.PermissionChecker.checkSelfPermission;

public class MainActivity extends AppCompatActivity {
    //Bkav TienNVh :Địa chỉ server
    public static final String AD_SERVER="http://45.76.144.34:8888/";
    public  static String PATH_ROOT = Environment.getExternalStorageDirectory().toString();
    String TAG ="Tiennvh";
    //Bkav TienNVh :Mã key để xác thực là Bphone
    private String key = "BMOS@bkav.com";
    Button BTUpload , BTDownload, BTGetAlbum , BTGetImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView status = findViewById(R.id.status_load_notification);
        ProgressBar progressBar = findViewById(R.id.progress_bar_notification);
        BTUpload = findViewById(R.id.bt_upload);
        BTDownload = findViewById(R.id.bt_download);
        BTGetAlbum = findViewById(R.id.bt_getAllAlbum);
        BTGetImage = findViewById(R.id.bt_getImage);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                &&isReadStoragePermissionGranted() && isWriteStoragePermissionGranted()) {

        }
        Callback callback = new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.d("Tiennvh", "onFailure: "+e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                //Bkav TienNVh :Upload thành công
                Log.d("Tiennvh", "onResponse: "+response.body().string());


            }
        };

        BTUpload.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {

                File directory = new File(PATH_ROOT+"/Album");
                listFilesForFolder(directory,callback,progressBar,status);
            }
        });

        BTDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Bkav TienNVh : Path
                String path_default = "/root/Background/Data/";// Path mặc định ko sửa
                String path_Image ="Thiennhien/B86/thiennhien2340_1080.jpeg"; // Cấu trúc là  Album/devices/nameimage
                String path_local = PATH_ROOT+"/CompressionFile/namethiennhien1.jpeg";// path luu image trên devices
                download(path_default+path_Image ,path_local);
            }
        });

        BTGetAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAllAlbum("/root/Background/Data");// Mặc định ko sửa
            }
        });

        BTGetImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAllAlbum("/root/Background/Data/Thiennhien");// Cấu trúc: Path mặc định + Name Album
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void listFilesForFolder(final File folder, Callback callback, ProgressBar progressBar , TextView status) {
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry,callback, progressBar, status);
            } else {
                String path =fileEntry.getPath();
                File path1 = fileEntry.getParentFile();
                upload("/root/Background/Data","upload" ,path , path1.getName(),callback, progressBar, status);

            }
        }

    }
    public void upload(String  pathSave , String path, String namePath , String nameAlbum , Callback callback, ProgressBar progressBar , TextView status){
        OkHttpClient client = new OkHttpClient();
        String url = AD_SERVER + path;
        File file = new File(namePath);
        MultipartBody body = RequestBuilder.uploadRequestBody(key , pathSave, file, nameAlbum);
        CountingRequestBody monitoredRequest = new CountingRequestBody(body, new CountingRequestBody.Listener() {
            @Override
            public void onRequestProgress(long bytesWritten, long contentLength) {
                //Update a progress bar with the following percentage
                float percentage = 100f * bytesWritten / contentLength;
                if (percentage >= 0) {
                    Log.d("progress ", percentage + "");
                    int percen = Math.round(percentage);
                    status.setText("Sao lưu "+percen+"%");
                    progressBar.setProgress(percen);
                } else {
                    //Something went wrong
                    status.setText("sao lưu lỗi ...");
                    Log.d("No progress ", 0 + "");
                }
            }
        });
        Request request = new Request.Builder()
                .url(url)
                .method("POST", monitoredRequest)
                .build();

        client.newCall(request).enqueue(callback);

    }

    public  void download(String path , String pathLocal){
        Callback mCallback1= new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.d("Tiennvh", "onFailure: "+e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.d("Tiennvh", "download: Successful ");
                    FileOutputStream fos = new FileOutputStream(pathLocal);
                    fos.write(response.body().bytes());
                    fos.close();
                }
            }
        };

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("key", key);
            jsonObject.put("path", path);
            RequestToServer.post("download", jsonObject,  mCallback1);
        } catch ( JSONException e) {
            e.printStackTrace();
        }
    }

    public void getAllAlbum(String path){
        Callback mCallback1= new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.d("Tiennvh", "onFailure: "+e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String mJsonData = response.body().string();
                    try {
                        JSONObject Jobject = new JSONObject(mJsonData);
                        JSONArray listData = Jobject.getJSONArray("list");
                        for(int i=0;i<listData.length();i++){
                            String ob =listData.get(i).toString();
                            JSONObject Jobject1 = new JSONObject(ob);
                            //data
                            String name = Jobject1.getString("name");
                            long size = Long.parseLong(Jobject1.getString("size"));
                            String path = Jobject1.getString("path");
                            String modifi = Jobject1.getString("modifi");
                            String create = Jobject1.getString("create");
                            Log.d("Tiennvh", "onResponse: path: " +path +" // name :"+name +" // size: "+ size +" // modifi :"+modifi+" // create : "+ create);
                        }
                    } catch (JSONException e) {
                        Log.d("Tiennvh", "handleMessage: " + e);
                    }
                }
            }
        };

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("key", key);
            jsonObject.put("path", path);
            RequestToServer.post("getallalbum", jsonObject,  mCallback1);
        } catch ( JSONException e) {
            e.printStackTrace();
        }

    }


    @SuppressLint("WrongConstant")
    public  boolean isReadStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission( Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted1");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked1");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted1");
            return true;
        }
    }

    @SuppressLint("WrongConstant")
    public  boolean isWriteStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted2");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked2");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted2");
            return true;
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1:
                if(grantResults[0]== PackageManager.PERMISSION_GRANTED){

                    //resume tasks needing this permission
                    Log.d("Tiennvh", "onRequestPermissionsResult: ");
                }else{
                    Log.d("Tiennvh", "onRequestPermissionsResult: FALSE");
                }
                break;
            case 2:
                Log.d(TAG, "External storage1");
                if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    Log.v(TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
                    //resume tasks needing this permission

                }else{
                    Log.d("Tiennvh", "onRequestPermissionsResult: FALSE");
                }
                break;


        }
    }

}