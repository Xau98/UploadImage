package com.example.uploadimage;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.File;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class RequestToServer {

    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private Activity mActivity;
    public static final String AD_SERVER="http://45.76.144.34:8888/";
    public RequestToServer(Activity activity) {
        this.mActivity = activity;
    }

// Bkav TienNVh : method post
    public static void post(String path, JSONObject jsonObject, Callback callback) {
        OkHttpClient client = new OkHttpClient();
        String url = AD_SERVER + path;
        RequestBody requestBody = RequestBody.create(JSON, String.valueOf(jsonObject));
        Request request = new Request.Builder()
                .url(url)
                .method("POST", requestBody)
                .build();
        client.newCall(request).enqueue(callback);
    }

// Bkav TienNVh : Method GET
    public static void get(String path, Callback callback) {
        OkHttpClient client = new OkHttpClient();
        String url = AD_SERVER + path;
        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(callback);
    }

// RequestToServer.upload("uploadfile",handleFile.PATH_ROOT+"/Android/test2.zip", callback );
   /* public static  void upload(Context context,String  pathSave , String path, String namePath , Callback callback, ProgressBar progressBar , TextView status){
        OkHttpClient client = new OkHttpClient();
        String url = AD_SERVER + path;
        File file = new File(namePath);
        *//*String fileExtention = getFileExt(file.getName());
        String filename = file.getName();*//*
        SharedPreferences sharedPref =  context.getSharedPreferences(MainActivity.SHAREPREFENCE,  context.MODE_PRIVATE);
        String id = sharedPref.getString("id", null);
        String token = sharedPref.getString("token", null);
        MultipartBody body = RequestBuilder.uploadRequestBody(id, token , pathSave, file);
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

    }*/
    //


    public static String getFileExt(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
    }
}
