package com.vedantamadam.raksha;

import androidx.annotation.RequiresApi;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.util.Linkify;
import android.view.View;
import android.widget.TextView;


public class about extends AppCompatActivity {
    Toolbar toolBar;
    TextView textView4;




    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        toolBar = (Toolbar) findViewById(R.id.toolbarAbout);





        toolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
            }
        });

        textView4 = (TextView)findViewById(R.id.t4);
        Linkify.addLinks(textView4,Linkify.WEB_URLS);

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void onStop() {



            super.onStop();


    }

}