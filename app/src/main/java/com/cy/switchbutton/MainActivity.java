package com.cy.switchbutton;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.cy.switchbuttonniubility.SimpleSwitchButton;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.cy.switchbutton.R.layout.activity_main);
        SimpleSwitchButton simpleSwitchButton=findViewById(R.id.SimpleSwitchButton);
        simpleSwitchButton.setOnCheckedChangeListener(new SimpleSwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SimpleSwitchButton simpleSwitchButton, boolean isChecked) {
                LogUtils.log("onCheckedChanged",isChecked);
            }
        });
        simpleSwitchButton.setChecked(true);
    }
}