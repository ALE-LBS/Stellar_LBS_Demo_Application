package com.ale.proserv.geoapp3;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class DebugActivity extends AppCompatActivity {

    private EditText editTextBrestMWZ;
    private EditText editTextIBM;
    private EditText editTextArgentine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);
        editTextBrestMWZ = (EditText)findViewById(R.id.editTextBrestMWZ);
        editTextIBM = (EditText)findViewById(R.id.editTextIBM);
        editTextArgentine = (EditText)findViewById(R.id.editTextArgentine);
        getPrefences();
    }

    public void getPrefences(){
        SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        editTextBrestMWZ.setText(sharedPreferences.getString("debugBrestMWZ","Escaliers"));
        editTextIBM.setText(sharedPreferences.getString("debugIbm","Hospital"));
        editTextArgentine.setText(sharedPreferences.getString("debugArgentine","Hall"));
    }

    public void savePreferences(View v){
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("debugBrestMWZ",editTextBrestMWZ.getText().toString());
        editor.putString("debugIbm",editTextIBM.getText().toString());
        editor.putString("debugArgentine",editTextArgentine.getText().toString());
        editor.commit();
        sendData();
    }

    public void sendData(){
        Log.i("DebugPlace","send data");
        Intent returnIntent = new Intent();
        returnIntent.putExtra("BrestMWZ",editTextBrestMWZ.getText().toString());
        returnIntent.putExtra("IBM",editTextIBM.getText().toString());
        returnIntent.putExtra("Argentine",editTextArgentine.getText().toString());
        setResult(RESULT_OK,returnIntent);
        finish();

    }
}
