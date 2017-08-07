package com.example.ricca.bb8_mindwave;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private TextView txtSignalQuality;
    private TextView txtState;
    private TextView txtAttention;
    private TextView daStatus;
    private TextView robotStatus;
    private Button btnRed;
    private Button btnGreen;
    private Button btnBlue;
    private ProgressBar attentionProgressBar;
    private MindwaveConnect mindwaveConnect;
    private BB8Connect bb8Connect;
    private BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.txtSignalQuality = (TextView) findViewById(R.id.txtSignalQuality);
        this.txtState = (TextView) findViewById(R.id.txtState);
        this.txtAttention = (TextView) findViewById(R.id.txtAttention);
        this.attentionProgressBar = (ProgressBar) findViewById(R.id.attentionProgressBar);
        this.daStatus = (TextView) findViewById(R.id.daStatus);
        this.robotStatus = (TextView) findViewById(R.id.robotStatus);
        this.btnRed = (Button) findViewById(R.id.btnRed);
        this.btnGreen = (Button) findViewById(R.id.btnGreen);
        this.btnBlue = (Button) findViewById(R.id.btnBlue);
        this.mindwaveConnect = new MindwaveConnect(this);
        this.bb8Connect = new BB8Connect(this);
        this.enableColorButtons(false);
        this.setBtnMove();

        // Register for broadcasts on BluetoothAdapter state change
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        this.registerReceiver(bb8Connect.getReceiver(), filter);

        // Check if bluetooth or location are still enabled
        this.reactivateBluetoothOrLocation();


        mindwaveConnect.connect();

        bb8Connect.startDiscovery();

        //mindwaveConnect.setAlgos();
        //mindwaveConnect.mindwaveStart();
    }

    public void setBtnMove(){
        Button btnMove = (Button)findViewById(R.id.btnMove);
        btnMove.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    bb8Connect.moveForward(0, (float) 0.1);
                    //System.out.println("pressed");
                    return true;
                }
                if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                    bb8Connect.stopRobot();
                }
                return false;
            }
        });
    }

    public void reactivateBluetoothOrLocation(){
        // Check if location is still enabled
        final LocationManager mLocationManager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            startActivity(new Intent(MainActivity.this, BluetoothConnectionActivity.class));
        }

        // Check if bluetooth is still enabled
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()){
            startActivity(new Intent(MainActivity.this, BluetoothConnectionActivity.class));
        }
    }

    public void setTxtSignalQuality(String s){
        this.txtSignalQuality.setText(s);
    }
    public void setTxtState(String s){
        this.txtState.setText(s);
    }
    public void setTxtAttention(String s){
        this.txtAttention.setText(s);
    }
    public void setAttentionProgressBar(int n){
        this.attentionProgressBar.setProgress(n);
    }
    public void setDaStatus(String s){
        this.daStatus.setText(s);
    }
    public void setRobotStatus(String s){
        this.robotStatus.setText(s);
    }

    public void ledColorRed(View v){
        bb8Connect.setRobotLed(1, 0, 0);
    }
    public void ledColorGreen(View v){
        bb8Connect.setRobotLed(0, 1, 0);
    }
    public void ledColorBlue(View v){
        bb8Connect.setRobotLed(0, 0, 1);
    }
    public void mindwaveStartListener(View v){ mindwaveConnect.mindwaveStart();}
    public void moveRobot(View v){
        bb8Connect.moveForward(0, (float) 0.1);
    }
    public void stopRobot(View v){
        bb8Connect.stopRobot();
    }

    public String getDaStatus(){
        return (String) this.daStatus.getText();
    }

    public void showToast(final String msg, final int timeStyle) {
        this.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), msg, timeStyle).show();
            }

        });
    }

    public void enableColorButtons(boolean b){
        btnRed.setEnabled(b);
        btnGreen.setEnabled(b);
        btnBlue.setEnabled(b);
    }
}
