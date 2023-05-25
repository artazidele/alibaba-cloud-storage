package com.example.alibabastorage;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSStsTokenCredentialProvider;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.DeleteObjectRequest;
import com.alibaba.sdk.android.oss.model.DeleteObjectResult;
import com.alibaba.sdk.android.oss.model.GetObjectRequest;
import com.alibaba.sdk.android.oss.model.GetObjectResult;
import com.alibaba.sdk.android.oss.model.ListObjectsRequest;
import com.alibaba.sdk.android.oss.model.ListObjectsResult;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    private TextView textView = null;

    String endpoint = "oss-eu-central-1.aliyuncs.com";
    String accessKeyId = "";
    String accessKeySecret = "";
    String securityToken = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.text_view);

        // Poga "Augšupielādēt failu"
        Button uploadBtn = findViewById(R.id.upload_file_btn);
        uploadBtn.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        // Pārbauda, vai lietotnei ir atļauts piekļūt ierīces failiem
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                                String[] permissions = new String[]{(android.Manifest.permission.READ_EXTERNAL_STORAGE)};
                                requestPermissions(permissions, 1001);
                            } else {
                                uploadObject();
                            }
                        } else {
                            uploadObject();
                        }
                    }
                });

        // Poga "Lejupielādēt failu"
        Button downloadBtn = findViewById(R.id.download_file_btn);
        downloadBtn.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        downloadObject();
                    }
                });

        // Poga "Iegūt failu sarakstu"
        Button listBtn = findViewById(R.id.list_files_btn);
        listBtn.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        getObjectList();
                    }
                });

        // Poga "Izdzēst failu"
        Button deleteBtn = findViewById(R.id.delete_file_btn);
        deleteBtn.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        deleteObject();
                    }
                });
    }

    // Funkcija objekta dzēšanai
    public void deleteObject() {
        OSSCredentialProvider credentialProvider = new OSSStsTokenCredentialProvider(accessKeyId, accessKeySecret, securityToken);
        OSSClient oss = new OSSClient(getApplicationContext(), endpoint, credentialProvider);
        DeleteObjectRequest delete = new DeleteObjectRequest("grozsalibabacloud", "uploadObject2");
        OSSAsyncTask task = oss.asyncDeleteObject(delete, new OSSCompletedCallback<DeleteObjectRequest, DeleteObjectResult>() {

            @Override
            public void onSuccess(DeleteObjectRequest request, DeleteObjectResult result) {
                textView.setText("Objekts ir dzēsts.");
            }

            @Override
            public void onFailure(DeleteObjectRequest request, ClientException clientException, ServiceException serviceException) {
                textView.setText("Objektu neizdevās izdzēst.");
            }
        });
    }

    // Funkcija objekta augšupielādei
    public void uploadObject() {
        OSSCredentialProvider credentialProvider = new OSSStsTokenCredentialProvider(accessKeyId, accessKeySecret, securityToken);
        OSSClient oss = new OSSClient(getApplicationContext(), endpoint, credentialProvider);
        PutObjectRequest put = new PutObjectRequest("grozsalibabacloud", "uploadObject2", "/sdcard/Download/file3.txt");
        put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
            @Override
            public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
                //
            }
        });
        OSSAsyncTask task = oss.asyncPutObject(put, new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
            @Override
            public void onSuccess(PutObjectRequest request, PutObjectResult result) {
                textView.setText("Objekts ir augšupielādēts.");
            }

            @Override
            public void onFailure(PutObjectRequest request, ClientException clientExcepion, ServiceException serviceException) {
                textView.setText("Objektu neizdevās augšupielādēt.");
            }
        });
    }

    // Funkcija objecta lejupielādei
    public void downloadObject() {
        OSSCredentialProvider credentialProvider = new OSSStsTokenCredentialProvider(accessKeyId, accessKeySecret, securityToken);
        OSSClient oss = new OSSClient(getApplicationContext(), endpoint, credentialProvider);
        GetObjectRequest get = new GetObjectRequest("grozsalibabacloud", "uploadObject2");

        OSSAsyncTask task = oss.asyncGetObject(get, new OSSCompletedCallback<GetObjectRequest, GetObjectResult>() {
            @Override
            public void onSuccess(GetObjectRequest request, GetObjectResult result) {
                InputStream inputStream = result.getObjectContent();
                byte[] buffer = new byte[2048];
                int len;

                File file = new File("/sdcard/Download/file123.txt");
                try (OutputStream output = new FileOutputStream(file)) {
                    while ((len = inputStream.read(buffer)) != -1) {
                        output.write(buffer, 0, len);
                    }
                    textView.setText("Objekts ir lejupielādēts.");
                } catch (IOException e) {
                    textView.setText("Objektu neizdevās saglabāt.");
                }
            }

            @Override
            public void onFailure(GetObjectRequest request, ClientException clientExcepion, ServiceException serviceException) {
                textView.setText("Objektu neizdevās lejupielādēt.");
            }
        });
    }

    // Funkcija objektu saraksta ieguvei
    public void getObjectList() {
        OSSCredentialProvider credentialProvider = new OSSStsTokenCredentialProvider(accessKeyId, accessKeySecret, securityToken);
        OSSClient oss = new OSSClient(getApplicationContext(), endpoint, credentialProvider);
        ListObjectsRequest list = new ListObjectsRequest("grozsalibabacloud");
        OSSAsyncTask task = oss.asyncListObjects(list, new OSSCompletedCallback<ListObjectsRequest, ListObjectsResult>() {

            @Override
            public void onSuccess(ListObjectsRequest request, ListObjectsResult result) {
                String objectList = "Objekti: ";
                for (int i = 0; i < result.getObjectSummaries().size(); i++) {
                    objectList += result.getObjectSummaries().get(i).getKey();
                    if (i == result.getObjectSummaries().size() - 1) {
                        objectList += ".";
                    } else {
                        objectList += ", ";
                    }
                }
                textView.setText(objectList);
            }

            @Override
            public void onFailure(ListObjectsRequest request, ClientException clientException, ServiceException serviceException) {
                textView.setText("Neizdevās iegūt objektu sarakstu.");
            }
        });
    }
}
