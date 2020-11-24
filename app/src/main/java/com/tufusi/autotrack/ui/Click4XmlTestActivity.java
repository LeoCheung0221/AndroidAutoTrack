package com.tufusi.autotrack.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.tufusi.autotrack.R;

public class Click4XmlTestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_android_click4_xml);
    }

    public void xmlOnClick(View view) {
        Toast.makeText(this, "XML OnClick", Toast.LENGTH_SHORT).show();
    }
}