package com.example.ricca.bb8_mindwave;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.orbotix.calibration.api.CalibrationEventListener;
import com.orbotix.calibration.api.CalibrationImageButtonView;
import com.orbotix.calibration.api.CalibrationView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private MindwaveConnect mindwaveConnect;
    private BB8Connect bb8Connect;
    private MyoConnect myoConnect;
    private CalibrationView calibrationView;
    private CalibrationImageButtonView calibrationButtonView;
    private boolean isMindwaveConnected;
    private boolean isBB8Connected;


    AlertDialog.Builder builder;

    File file;
    FileWriter writer;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        reactivateBluetoothOrLocation();
                        break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String s = new SimpleDateFormat("HH.mm.ss").format(new Date());
        file = new File(Environment.getExternalStorageDirectory(), "/datiNotteRicercatori/prova" + s + ".txt");

        this.mindwaveConnect = new MindwaveConnect(this);
        this.bb8Connect = new BB8Connect(this);
        this.myoConnect = new MyoConnect(this);
        this.isBB8Connected = false;
        this.isMindwaveConnected = false;
        findViewById(R.id.btnEmergencyBrake).setEnabled(false);
        findViewById(R.id.btnStartMovingRobot).setEnabled(false);



        // Register for broadcasts on BluetoothAdapter state change
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        this.registerReceiver(mReceiver, filter);

        showPopup();
        // Check if bluetooth or location are still enabled
        this.reactivateBluetoothOrLocation();
        setupCalibration();
        bb8Connect.connect();
        myoConnect.connect();
        mindwaveConnect.connect();
        enableCalibration();
        this.unregisterReceiver(mReceiver);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        this.myoConnect.disconnect();
    }

    //Metodi per il tasto di orientamento robot
    private void setupCalibration() {
        // Get the view from the xml file
        calibrationView = (CalibrationView)findViewById(R.id.calibrationView);
        // Set the glow. You might want to not turn this on if you're using any intense graphical elements.
        calibrationView.setShowGlow(true);
        // Register anonymously for the calibration life here. You could also have this class implement the interface
        // manually if you plan to do more with the callbacks.
        calibrationView.setCalibrationEventListener(new CalibrationEventListener() {
            /**
             * Invoked when the user begins the calibration process.
             */
            @Override
            public void onCalibrationBegan() {
                // The easy way to set up the robot for calibration is to use ConvenienceRobot#calibrating(true)
                if(BB8Connect.getRobot() != null){
                    BB8Connect.getRobot().calibrating(true);
                }
            }

            /**
             * Invoked when the user moves the calibration ring
             * @param angle The angle that the robot has rotated to.
             */
            @Override
            public void onCalibrationChanged(float angle) {
                // The usual thing to do when calibration happens is to send a roll command with this new angle, a speed of 0
                // and the calibrate flag set.
                if(BB8Connect.getRobot() != null)
                    BB8Connect.getRobot().rotate(angle);
            }

            /**
             * Invoked when the user stops the calibration process
             */
            @Override
            public void onCalibrationEnded() {
                // This is where the calibration process is "committed". Here you want to tell the robot to stop as well as
                // stop the calibration process.
                if(BB8Connect.getRobot() != null) {
                    BB8Connect.getRobot().stop();
                    BB8Connect.getRobot().calibrating(false);
                }
            }
        });
        // Like the joystick, turn this off until a robot connects.
        calibrationView.setEnabled(false);

        // To set up the button, you need a calibration view. You get the button view, and then set it to the
        // calibration view that we just configured.
        calibrationButtonView = (CalibrationImageButtonView) findViewById(R.id.calibrateButton);
        calibrationButtonView.setCalibrationView(calibrationView);
        calibrationButtonView.setEnabled(false);


    }
    private void enableCalibration(){
        // Here, you need to route all the touch life to the joystick and calibration view so that they know about
        // them. To do this, you need a way to reference the view (in this case, the id "entire_view") and attach
        // an onTouchListener which in this case is declared anonymously and invokes the
        // Controller#interpretMotionEvent() method on the joystick and the calibration view.
        findViewById(R.id.entire_view).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                calibrationView.interpretMotionEvent(event);
                return true;
            }
        });

        // Don't forget to turn on UI elements
        calibrationView.setEnabled(true);
        calibrationButtonView.setEnabled(true);
    }

    //Attivazione automatica di bluetooth e GPS

    public void reactivateBluetoothOrLocation(){
        // Check if location is still enabled
        final LocationManager mLocationManager = (LocationManager) getSystemService(
                Context.LOCATION_SERVICE );
        if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            startActivity(new Intent(MainActivity.this, BluetoothConnectionActivity.class));
        }

        // Check if bluetooth is still enabled
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()){
            startActivity(new Intent(MainActivity.this, BluetoothConnectionActivity.class));
        }
    }

    //Metodi per la gestione della UI
    public void setTxtSignalQuality(final String s){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView)findViewById(R.id.txtSignalQuality)).setText(s);
            }
        });
    }
    public void setTxtAttention(final String s){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView)findViewById(R.id.txtAttention)).setText(s);
            }
        });
    }
    public void setAttentionProgressBar(final int n){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((ProgressBar)findViewById(R.id.attentionProgressBar)).setProgress(n);
            }
        });
    }
    public void setTxtRobotStatus(final String s){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView)findViewById(R.id.robotStatus)).setText(s);
            }
        });
    }
    public void setTxtMyoStatus(final String s){
        ((TextView)findViewById(R.id.txtMyoStatus)).setText(s);
    }

    private void setImgMindwaveConnected(){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((ImageView)findViewById(R.id.imgMindwave)).setImageResource(R.drawable.connected);
            }
        });
    }
    private void setImgMindwaveNotConnected(){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((ImageView)findViewById(R.id.imgMindwave)).setImageResource(R.drawable.not_connected);
            }
        });    }
    private void setImgBB8Connected(){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((ImageView)findViewById(R.id.imgBB8)).setImageResource(R.drawable.connected);
            }
        });
    }
    private void setImgBB8NotConnected(){
        this.runOnUiThread(new Runnable() {
        @Override
        public void run() {
            ((ImageView)findViewById(R.id.imgBB8)).setImageResource(R.drawable.not_connected);
        }
    });
    }
    private void setImgMyoConnected(){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((ImageView)findViewById(R.id.imgMyo)).setImageResource(R.drawable.connected);
                ((ImageView)findViewById(R.id.imgDoubleTap)).setImageResource(R.drawable.blue_double_tap);
            }
        });
    }
    private void setImgMyoNotConnected(){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((ImageView)findViewById(R.id.imgMyo)).setImageResource(R.drawable.not_connected);
                ((ImageView)findViewById(R.id.imgDoubleTap)).setImageResource(R.drawable.grey_double_tap);
                ((ImageView)findViewById(R.id.imgFist)).setImageResource(R.drawable.grey_fist);
                ((ImageView)findViewById(R.id.imgSpreadFingers)).setImageResource(R.drawable.grey_spread_fingers);
            }
        });
    }
    public void myoUnlocked(){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((ImageView)findViewById(R.id.imgFist)).setImageResource(R.drawable.blue_fist);
                ((ImageView)findViewById(R.id.imgSpreadFingers)).setImageResource(R.drawable.blue_spread_fingers);
            }
        });
    }
    public void myoLocked(){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((ImageView)findViewById(R.id.imgFist)).setImageResource(R.drawable.grey_fist);
                ((ImageView)findViewById(R.id.imgSpreadFingers)).setImageResource(R.drawable.grey_spread_fingers);
            }
        });
    }
    private void doIHaveToEnableButtons(){
        if(this.isMindwaveConnected & this.isBB8Connected){
            enableControlButtons(true);
        }
        else {
            if(isBB8Connected) {
                btnEmergencyBrakeListener(null);
            }
            enableControlButtons(false);
        }
    }
    public void showToast(final String msg, final int timeStyle) {
        this.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), msg, timeStyle).show();
            }

        });
    }

    //Metodi di decisione: Cosa fare quando un device viene colleato o scollegato
    public void mindwaveConnected(boolean b){
        this.isMindwaveConnected = b;
        this.doIHaveToEnableButtons();
        if(b){
            setImgMindwaveConnected();
        }else {
            setImgMindwaveNotConnected();
        }
    }
    public void BB8Connected(boolean b){
        this.isBB8Connected = b;
        this.doIHaveToEnableButtons();
        if(b){
            setImgBB8Connected();
        }else{
            setImgBB8NotConnected();
        }
    }
    public void myoConnected(boolean b){
        this.doIHaveToEnableButtons();
        if(b){
            setImgMyoConnected();
        }
        else {
            setImgMyoNotConnected();
        }
    }
    private void enableControlButtons(final boolean enable){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.btnStartMovingRobot).setEnabled(enable);
                findViewById(R.id.btnEmergencyBrake).setEnabled(enable);
            }
        });
    }

    //listener dei bottoni
    public void mindwaveStartListener(View v){
        if(isBB8Connected) {
            if(isMindwaveConnected) {
                mindwaveConnect.mindwaveStart();
                this.write("Start");
                this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.btnStartMovingRobot).setEnabled(false);
                        ((TextView)findViewById(R.id.txtBB8State)).setText(R.string.moving);
                    }
                });
            }
            else{
                showToast("Wait until Mindwave is connected!", Toast.LENGTH_SHORT);
            }
        }
    }
    public void btnEmergencyBrakeListener(View v){
        BB8Movement.getInstance().setSpeed(0);
        this.mindwaveConnect.mindwavePause();
        this.bb8Connect.stopRobot();
        this.setTxtAttention("--");
        this.bb8Connect.setRobotLedYellow();
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((ProgressBar)findViewById(R.id.attentionProgressBar)).setProgress(0);
                findViewById(R.id.btnStartMovingRobot).setEnabled(true);
                ((TextView)findViewById(R.id.txtBB8State)).setText(R.string.standing);
            }
        });
    }

    //Azioni da eseguire quando il Mindwave registra un determinato valore
    public void performLowLevelAction(){
        BB8Movement.getInstance().setSpeed(0);
        moveRobot();
    }
    public void performMidLevelAction(){
        BB8Movement.getInstance().setSpeed(0.3);
        moveRobot();
    }
    public void performHighLevelAction(){
        BB8Movement.getInstance().setSpeed(0.5);
        moveRobot();
    }

    public void stopCalibratingRobot(){
        bb8Connect.stopClaibrating();
    }
    public void rotateRobot(float angle){
        bb8Connect.moveForward(angle, BB8Movement.getInstance().getSpeed());
    }

    public boolean getIsBB8Connected(){
        return this.isBB8Connected;
    }

    private void moveRobot(){
        double speed = BB8Movement.getInstance().getSpeed();
        if(speed == 0){
            bb8Connect.setRobotLedBlue();
        }
        else  if(speed == 0.3){
            bb8Connect.setRobotLedGreen();
        }
        else {
            bb8Connect.setRobotLedRed();
        }
        bb8Connect.moveForward(0, speed);
    }

    void write(String s){
        try{
            writer = new FileWriter(file, true);
            writer.append(s).append("\n");
            writer.flush();
            writer.close();
        }
        catch (IOException e){
            this.showToast("Cannot write on file! " + e.getMessage(), Toast.LENGTH_LONG);
        }
    }

    void showPopup(){
        builder = new AlertDialog.Builder(this);
        LayoutInflater layoutInflater = getLayoutInflater();

        final View v = layoutInflater.inflate(R.layout.statistics, null);
        ((RadioButton)v.findViewById(R.id.radioFemale)).setChecked(true);

        builder.setView(v).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                String gender;
                String age;
                if(((RadioButton)v.findViewById(R.id.radioFemale)).isChecked()){
                    gender = "F";
                }
                else{
                    gender = "M";
                }
                age = ((EditText)v.findViewById(R.id.editAge)).getText().toString();
                try {
                    writer = new FileWriter(file, true);
                    writer.append(gender).append(", ").append(age).append("\n");
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        builder.create();
        builder.show();
    }
}
