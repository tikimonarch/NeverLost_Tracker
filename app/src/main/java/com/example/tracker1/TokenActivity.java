package com.example.tracker1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

public class TokenActivity extends AppCompatActivity {
    static int PERMISSION_CODE = 100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_token);
        if(ContextCompat.checkSelfPermission(TokenActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(TokenActivity.this, new String[]{Manifest.permission.CALL_PHONE},PERMISSION_CODE);
    }

    public void ClickCall(View view) {
        String phoneNo = "91519673";
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:"+phoneNo));
        startActivity(intent);
    }
}