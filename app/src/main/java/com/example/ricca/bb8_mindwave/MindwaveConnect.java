package com.example.ricca.bb8_mindwave;

/**
 * Created by ricca on 03/08/17.
 */

import android.bluetooth.BluetoothAdapter;
import android.util.Log;
import android.widget.Toast;

import com.neurosky.AlgoSdk.NskAlgoDataType;
import com.neurosky.AlgoSdk.NskAlgoSdk;
import com.neurosky.AlgoSdk.NskAlgoSignalQuality;
import com.neurosky.AlgoSdk.NskAlgoState;
import com.neurosky.AlgoSdk.NskAlgoType;
import com.neurosky.connection.ConnectionStates;
import com.neurosky.connection.DataType.MindDataType;
import com.neurosky.connection.TgStreamHandler;
import com.neurosky.connection.TgStreamReader;


public class MindwaveConnect{
    final String TAG = "MainActivityTag";

    private NskAlgoSdk nskAlgoSdk;
    //private short raw_data[] = {0};
    //private int raw_data_index= 0;
    private TgStreamReader tgStreamReader;
    private BluetoothAdapter mBluetoothAdapter;
    private MainActivity mainActivity;
    private boolean bRunning = false;
    private TgStreamHandler callback = new TgStreamHandler() {
        @Override
        public void onStatesChanged(int connectionStates) {
            // TODO Auto-generated method stub
            Log.d(TAG, "connectionStates change to: " + connectionStates);
            switch (connectionStates) {
                case ConnectionStates.STATE_CONNECTING:
                    showToast("MindWave is connecting... ", Toast.LENGTH_LONG);
                    System.out.println("MindWave: Connecting... ");
                    // Do something when connecting
                    break;
                case ConnectionStates.STATE_CONNECTED:
                    // Do something when connected
                    tgStreamReader.start();
                    showToast("Connected", Toast.LENGTH_SHORT);
                    System.out.println("MindWave: Connected!");
                    break;
                // TODO: Attivare automaticamente quello che fa il bottone SetAlgos!!!
                case ConnectionStates.STATE_WORKING:
                    // Do something when working

                    //(9) demo of recording raw data , stop() will call stopRecordRawData,
                    //or you can add a button to control it.
                    //You can change the save path by calling setRecordStreamFilePath(String filePath) before startRecordRawData
                    //tgStreamReader.startRecordRawData();

                    setAlgos();
                    System.out.println("MindWave: Algos setted");

                    break;
                case ConnectionStates.STATE_GET_DATA_TIME_OUT:
                    // Do something when getting data timeout

                    //(9) demo of recording raw data, exception handling
                    //tgStreamReader.stopRecordRawData();

                    showToast("Get data time out!", Toast.LENGTH_SHORT);

                    if (tgStreamReader != null && tgStreamReader.isBTConnected()) {
                        tgStreamReader.stop();
                        tgStreamReader.close();
                    }

                    break;
                case ConnectionStates.STATE_STOPPED:
                    // Do something when stopped
                    // We have to call tgStreamReader.stop() and tgStreamReader.close() much more than
                    // tgStreamReader.connectAndstart(), because we have to prepare for that.

                    break;
                case ConnectionStates.STATE_DISCONNECTED:
                    // Do something when disconnected
                    break;
                case ConnectionStates.STATE_ERROR:
                    // Do something when you get error message
                    break;
                case ConnectionStates.STATE_FAILED:
                    // Do something when you get failed message
                    // It always happens when open the BluetoothSocket error or timeout
                    // Maybe the device is not working normal.
                    // Maybe you have to try again
                    break;
            }
        }
        @Override
        public void onRecordFail(int flag) {
            // You can handle the record error message here
            Log.e(TAG,"onRecordFail: " +flag);

        }

        @Override
        public void onChecksumFail(byte[] payload, int length, int checksum) {
            // You can handle the bad packets here.
        }

        @Override
        public void onDataReceived(int datatype, int data, Object obj) {
            // You can handle the received data here
            // You can feed the raw data to algo sdk here if necessary.
            //Log.i(TAG,"onDataReceived");
            switch (datatype) {
                case MindDataType.CODE_ATTENTION:
                    short attValue[] = {(short)data};
                    nskAlgoSdk.NskAlgoDataStream(NskAlgoDataType.NSK_ALGO_DATA_TYPE_ATT.value, attValue, 1);
                    break;
                case MindDataType.CODE_MEDITATION:
                    /*
                    short medValue[] = {(short)data};
                    nskAlgoSdk.NskAlgoDataStream(NskAlgoDataType.NSK_ALGO_DATA_TYPE_MED.value, medValue, 1);
                    */
                    break;
                case MindDataType.CODE_POOR_SIGNAL:
                    short pqValue[] = {(short)data};
                    nskAlgoSdk.NskAlgoDataStream(NskAlgoDataType.NSK_ALGO_DATA_TYPE_PQ.value, pqValue, 1);
                    break;
                case MindDataType.CODE_RAW:
                    /*
                    raw_data[raw_data_index++] = (short)data;
                    if (raw_data_index == 512) {
                        nskAlgoSdk.NskAlgoDataStream(NskAlgoDataType.NSK_ALGO_DATA_TYPE_EEG.value, raw_data, raw_data_index);
                        raw_data_index = 0;
                    }
                    */
                    break;
                default:
                    break;
            }
        }
    };
    private NskAlgoType currentSelectedAlgo;
    private boolean bInited = false;
    int algoTypes = 0;// = NskAlgoType.NSK_ALGO_TYPE_CR.value;


    public MindwaveConnect(MainActivity mainActivity){
        this.mainActivity = mainActivity;
    }

    public void connect(){
        nskAlgoSdk = new NskAlgoSdk();

        //raw_data = new short[512];
        //raw_data_index = 0;

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Example of constructor public TgStreamReader(BluetoothAdapter ba, TgStreamHandler tgStreamHandler)
        tgStreamReader = new TgStreamReader(mBluetoothAdapter,callback);

        if(tgStreamReader != null && tgStreamReader.isBTConnected()){

            // Prepare for connecting
            tgStreamReader.stop();
            tgStreamReader.close();
        }

        // (4) Demo of  using connect() and start() to replace connectAndStart(),
        // please call start() when the state is changed to STATE_CONNECTED
        tgStreamReader.connect();

        nskAlgoSdk.setOnSignalQualityListener(new NskAlgoSdk.OnSignalQualityListener() {
            @Override
            public void onSignalQuality(int level) {
                //Log.d(TAG, "NskAlgoSignalQualityListener: level: " + level);
                final int fLevel = level;
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // change UI elements here
                        String stateQuality = NskAlgoSignalQuality.values()[fLevel].toString();
                        mainActivity.setTxtSignalQuality(stateQuality);
                    }
                });
            }
        });

        nskAlgoSdk.setOnStateChangeListener(new NskAlgoSdk.OnStateChangeListener() {
            @Override
            public void onStateChange(int state, int reason) {
                String stateStr = "";
                String reasonStr = "";
                for (NskAlgoState s : NskAlgoState.values()) {
                    if (s.value == state) {
                        stateStr = s.toString();
                    }
                }
                for (NskAlgoState r : NskAlgoState.values()) {
                    if (r.value == reason) {
                        reasonStr = r.toString();
                    }
                }
                Log.d(TAG, "NskAlgoSdkStateChangeListener: state: " + stateStr + ", reason: " + reasonStr);
                final String finalStateStr = stateStr + " | " + reasonStr;
                final int finalState = state;
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // change UI elements here
                        mainActivity.setTxtState(finalStateStr);
                        /*
                        if (finalState == NskAlgoState.NSK_ALGO_STATE_RUNNING.value || finalState == NskAlgoState.NSK_ALGO_STATE_COLLECTING_BASELINE_DATA.value) {
                            bRunning = true;
                            startButton.setText("Pausa");
                            startButton.setEnabled(true);
                            stopButton.setEnabled(true);
                        } else if (finalState == NskAlgoState.NSK_ALGO_STATE_STOP.value) {
                            bRunning = false;
                            raw_data = null;
                            raw_data_index = 0;
                            startButton.setText("Start");
                            startButton.setEnabled(true);
                            stopButton.setEnabled(false);

                            connectButton.setEnabled(true);
                            //cannedButton.setEnabled(true);

                            if (tgStreamReader != null && tgStreamReader.isBTConnected()) {

                                // Prepare for connecting
                                tgStreamReader.stop();
                                tgStreamReader.close();
                            }

                            output_data_count = 0;
                            output_data = null;

                            System.gc();
                        } else if (finalState == NskAlgoState.NSK_ALGO_STATE_PAUSE.value) {
                            bRunning = false;
                            startButton.setText("Start");
                            startButton.setEnabled(true);
                            stopButton.setEnabled(true);
                        } else if (finalState == NskAlgoState.NSK_ALGO_STATE_ANALYSING_BULK_DATA.value) {
                            bRunning = true;
                            startButton.setText("Start");
                            startButton.setEnabled(false);
                            stopButton.setEnabled(true);
                        } else if (finalState == NskAlgoState.NSK_ALGO_STATE_INITED.value || finalState == NskAlgoState.NSK_ALGO_STATE_UNINTIED.value) {
                            bRunning = false;
                            startButton.setText("Start");
                            startButton.setEnabled(true);
                            stopButton.setEnabled(false);
                        }
                        */
                    }
                });
            }
        });

        nskAlgoSdk.setOnSignalQualityListener(new NskAlgoSdk.OnSignalQualityListener() {
            @Override
            public void onSignalQuality(final int level) {
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // change UI elements here
                        String sqStr = NskAlgoSignalQuality.values()[level].toString();
                        mainActivity.setTxtSignalQuality(sqStr);
                    }
                });
            }
        });
/*
        nskAlgoSdk.setOnBPAlgoIndexListener(new NskAlgoSdk.OnBPAlgoIndexListener() {
            @Override
            public void onBPAlgoIndex(float delta, float theta, float alpha, float beta, float gamma) {
                Log.d(TAG, "NskAlgoBPAlgoIndexListener: BP: D[" + delta + " dB] T[" + theta + " dB] A[" + alpha + " dB] B[" + beta + " dB] G[" + gamma + "]");

                final float fDelta = delta, fTheta = theta, fAlpha = alpha, fBeta = beta, fGamma = gamma;
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // change UI elements here
                        AddValueToPlot(bp_deltaSeries, fDelta);
                        AddValueToPlot(bp_thetaSeries, fTheta);
                        AddValueToPlot(bp_alphaSeries, fAlpha);
                        AddValueToPlot(bp_betaSeries, fBeta);
                        AddValueToPlot(bp_gammaSeries, fGamma);
                    }
                });
            }
        });
*/
        nskAlgoSdk.setOnAttAlgoIndexListener(new NskAlgoSdk.OnAttAlgoIndexListener() {
            @Override
            public void onAttAlgoIndex(int value) {
                Log.d(TAG, "NskAlgoAttAlgoIndexListener: Attention:" + value);
                String attStr = "[" + value + "]";
                final String finalAttStr = attStr;
                final int finalValueAtt = value;
                //TODO add here code to show

                if(value<30){
                    mainActivity.ledColorBlue(null);
                }
                else if(value>=30 && value<80){
                    mainActivity.ledColorGreen(null);
                }
                else if(value >=80){
                    mainActivity.ledColorRed(null);
                }

                /*
                VolleyClient.volleyRequest("http://192.168.4.1/attenzione?value="+value, getApplicationContext());

                if(value>50){
                    VolleyClient.volleyRequest("http://192.168.4.1/sinistra", getApplicationContext());
                }
                if(value>80){
                    System.out.println("la lampadina si accende");
                    VolleyClient.volleyRequest("http://192.168.4.1/sinistra", getApplicationContext());
                }
                */



                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // change UI elements here
                        mainActivity.setTxtAttention(finalAttStr);
                        mainActivity.setAttentionProgressBar(finalValueAtt);
                        /*
                        if(finalValueAtt>70){
                            blinkImage.setImageResource(R.mipmap.led_on);
                        }
                        if(finalValueAtt<40){
                            blinkImage.setImageResource(R.mipmap.led_off);
                        }
                        */

                    }
                });
            }
        });
/*
        nskAlgoSdk.setOnMedAlgoIndexListener(new NskAlgoSdk.OnMedAlgoIndexListener() {
            @Override
            public void onMedAlgoIndex(int value) {
                Log.d(TAG, "NskAlgoMedAlgoIndexListener: Meditation:" + value);
                String medStr = "[" + value + "]";
                final String finalMedStr = medStr;
                VolleyClient.volleyRequest("http://192.168.4.1/meditazione?value="+value, getApplicationContext());
                if(value>80){
                    System.out.println("sei rilassatissimo!");
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // change UI elements here
                        medValue.setText(finalMedStr);
                    }
                });
            }
        });
*/
/*
        nskAlgoSdk.setOnEyeBlinkDetectionListener(new NskAlgoSdk.OnEyeBlinkDetectionListener() {
            @Override
            public void onEyeBlinkDetect(int strength) {
                Log.d(TAG, "NskAlgoEyeBlinkDetectionListener: Eye blink detected: " + strength);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //blinkImage.setImageResource(R.mipmap.led_on);
                        Timer timer = new Timer();

                        timer.schedule(new TimerTask() {
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //blinkImage.setImageResource(R.mipmap.led_off);
                                    }
                                });
                            }
                        }, 500);
                    }
                });
            }
        });
*/

    }

    public void setAlgos() {
        // check selected algos

        //startButton.setEnabled(false);

        //stopButton.setEnabled(false);
        //clearAllSeries();
        //text.setVisibility(View.INVISIBLE);
        //text.setText("");

        //bpText.setEnabled(false);

        currentSelectedAlgo = NskAlgoType.NSK_ALGO_TYPE_INVALID;
        //intervalSeekBar.setEnabled(false);
        //setIntervalButton.setEnabled(false);
        //intervalText.setText("--");

        //attValue.setText("--");
        //medValue.setText("--");

        //stateText.setText("");
        //sqText.setText("");

        //algoTypes += NskAlgoType.NSK_ALGO_TYPE_MED.value;
        algoTypes += NskAlgoType.NSK_ALGO_TYPE_ATT.value;
        //algoTypes += NskAlgoType.NSK_ALGO_TYPE_BLINK.value;
        //algoTypes += NskAlgoType.NSK_ALGO_TYPE_BP.value;
        //bpText.setEnabled(true);
        //bp_deltaSeries = createSeries("Delta");
        //bp_thetaSeries = createSeries("Theta");
        //bp_alphaSeries = createSeries("Alpha");
        //bp_betaSeries = createSeries("Beta");
        //bp_gammaSeries = createSeries("Gamma");

        if (bInited) {
            nskAlgoSdk.NskAlgoUninit();
            System.out.println("Algo settati!");
            bInited = false;
        }
        int ret = nskAlgoSdk.NskAlgoInit(algoTypes, mainActivity.getFilesDir().getAbsolutePath());
        if (ret == 0) {
            bInited = true;
        }
    }

    public void mindwaveStart(){
        //setAlgos();
        if (bRunning == false) {
            nskAlgoSdk.NskAlgoStart(false);
            System.out.println("Start!!!");
        } else {
            nskAlgoSdk.NskAlgoPause();
        }
        System.out.println("MindWave: Algo started!");
    }

    /*
        private void clearAllSeries () {
            if (bp_deltaSeries != null) {
                plot.removeSeries(bp_deltaSeries);
                bp_deltaSeries = null;
            }
            if (bp_thetaSeries != null) {
                plot.removeSeries(bp_thetaSeries);
                bp_thetaSeries = null;
            }
            if (bp_alphaSeries != null) {
                plot.removeSeries(bp_alphaSeries);
                bp_alphaSeries = null;
            }
            if (bp_betaSeries != null) {
                plot.removeSeries(bp_betaSeries);
                bp_betaSeries = null;
            }
            if (bp_gammaSeries != null) {
                plot.removeSeries(bp_gammaSeries);
                bp_gammaSeries = null;
            }
            plot.setVisibility(View.INVISIBLE);
            System.gc();
        }
    */
    public void showToast(final String msg, final int timeStyle) {
        mainActivity.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(mainActivity.getApplicationContext(), msg, timeStyle).show();
            }

        });
    }
}