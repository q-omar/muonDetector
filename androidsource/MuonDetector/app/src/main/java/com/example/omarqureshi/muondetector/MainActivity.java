package com.example.omarqureshi.muondetector;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    private Button recorderButton;
    private Button aboutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Cosmic Watch Communicator");


        recorderButton = (Button)findViewById(R.id.recorderButton);
        recorderButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){
                openRecorderScreen();
            }
        });


        aboutButton = (Button)findViewById(R.id.aboutButton);
        aboutButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                openAboutScreen();
            }
        });
    }

    public void openRecorderScreen(){
        Intent intent = new Intent(this,RecorderScreen.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

    }

    public void openAboutScreen(){
        Intent intent2 = new Intent(this,AboutScreen.class);
        startActivity(intent2);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }


}