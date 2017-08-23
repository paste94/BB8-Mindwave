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
import com.neurosky.AlgoSdk.NskAlgoType;
import com.neurosky.connection.ConnectionStates;
import com.neurosky.connection.DataType.MindDataType;
import com.neurosky.connection.TgStreamHandler;
import com.neurosky.connection.TgStreamReader;


public class MindwaveConnect{
    final String TAG = "MainActivityTag";

    public int algoTypes = 0;// = NskAlgoType.NSK_ALGO_TYPE_CR.value;
    private NskAlgoSdk nskAlgoSdk;
    private TgStreamReader tgStreamReader;
    private BluetoothAdapter mBluetoothAdapter;
    private MainActivity mainActivity;
    private boolean bRunning = false;
    private NskAlgoType currentSelectedAlgo;
    private boolean bInited = false;
    private int limit;
    private TgStreamHandler callback = new TgStreamHandler() {
        @Override
        public void onStatesChanged(int connectionStates) {
            // TODO Auto-generated method stub
            Log.d(TAG, "connectionStates change to: " + connectionStates);
            switch (connectionStates) {
                case ConnectionStates.STATE_CONNECTING:
                    mainActivity.showToast("MindWave is connecting... ", Toast.LENGTH_SHORT);
                    System.out.println("MindWave: Connecting... ");
                    mainActivity.mindwaveConnected(false);
                    // Do something when connecting
                    break;
                case ConnectionStates.STATE_CONNECTED:
                    // Do something when connected
                    tgStreamReader.start();
                    mainActivity.showToast("Connected", Toast.LENGTH_SHORT);
                    System.out.println("MindWave: Connected!");
                    mainActivity.mindwaveConnected(true);
                    break;
                // TODO: Attivare automaticamente quello che fa il bottone SetAlgos!!!
                case ConnectionStates.STATE_WORKING:
                    mainActivity.showToast("WORKING", Toast.LENGTH_SHORT);
                    // Do something when working

                    //(9) demo of recording raw data , stop() will call stopRecordRawData,
                    //or you can add a button to control it.
                    //You can change the save path by calling setRecordStreamFilePath(String filePath) before startRecordRawData

                    setAlgos();

                    break;
                case ConnectionStates.STATE_GET_DATA_TIME_OUT:
                    // Do something when getting data timeout

                    //(9) demo of recording raw data, exception handling
                    //tgStreamReader.stopRecordRawData();

                    mainActivity.showToast("Get data time out!", Toast.LENGTH_SHORT);

                    if (tgStreamReader != null && tgStreamReader.isBTConnected()) {
                        tgStreamReader.stop();
                        tgStreamReader.close();
                    }
                    //connect();
                    break;
                case ConnectionStates.STATE_STOPPED:
                    mainActivity.showToast("STOPPED", Toast.LENGTH_SHORT);
                    break;
                case ConnectionStates.STATE_DISCONNECTED: //Quando viene spento mentre è connesso
                    // Do something when disconnected
                    mainActivity.showToast("DISCONECTED", Toast.LENGTH_SHORT);

                    mainActivity.mindwaveConnected(false);
                    mainActivity.setTxtSignalQuality("--");
                    mainActivity.setTxtAttention("--");
                    mainActivity.setAttentionProgressBar(0);
                    mainActivity.btnEmergencyBrakeListener(null);
                    //nskAlgoSdk.NskAlgoStop();
                    //nskAlgoSdk.NskAlgoStop();
                    nskAlgoSdk.NskAlgoUninit();
                    connect();

                    break;
                case ConnectionStates.STATE_ERROR:

                    mainActivity.mindwaveConnected(false);
                    // Do something when you get error message
                    //connect();
                    break;
                case ConnectionStates.STATE_FAILED: //Quando è spento
                    mainActivity.showToast("FAILED", Toast.LENGTH_SHORT);

                    // Do something when you get failed message
                    // It always happens when open the BluetoothSocket error or timeout
                    // Maybe the device is not working normal.
                    // Maybe you have to try again
                    connect();
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

    public MindwaveConnect(MainActivity mainActivity, int limit){
        this.mainActivity = mainActivity;
        this.limit = limit;
    }

    public void connect(){
        nskAlgoSdk = new NskAlgoSdk();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Example of constructor public TgStreamReader(BluetoothAdapter ba, TgStreamHandler tgStreamHandler)
        tgStreamReader = new TgStreamReader(mBluetoothAdapter,callback);

        //TODO: Continua a provare a connetterti finchè puoi. Se il while blocca, crea thread separato.
        if(tgStreamReader != null && tgStreamReader.isBTConnected()) {

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
                mainActivity.setTxtSignalQuality(NskAlgoSignalQuality.values()[level].toString());
            }
        });
/*
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

                    }
                });
            }
        });
*/
        nskAlgoSdk.setOnSignalQualityListener(new NskAlgoSdk.OnSignalQualityListener() {
            @Override
            public void onSignalQuality(final int level) {
                mainActivity.setTxtSignalQuality(NskAlgoSignalQuality.values()[level].toString());
            }
        });

        nskAlgoSdk.setOnAttAlgoIndexListener(new NskAlgoSdk.OnAttAlgoIndexListener() {
            @Override
            public void onAttAlgoIndex(int value) {
                Log.d(TAG, "NskAlgoAttAlgoIndexListener: Attention:" + value);
                String attStr = "[" + value + "]";
                final String finalAttStr = attStr;
                final int finalValueAtt = value;
                if(value<50){
                    mainActivity.performLowLevelAction();
                }
                else if(value>=50 && value<80){
                    mainActivity.performMidLevelAction();
                }
                else if(value >=80){
                    mainActivity.performHighLevelAction();
                }
                mainActivity.setTxtAttention(finalAttStr);
                mainActivity.setAttentionProgressBar(finalValueAtt);
            }
        });
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
            bInited = false;
        }
        int ret = nskAlgoSdk.NskAlgoInit(algoTypes, mainActivity.getFilesDir().getAbsolutePath());
        if (ret == 0) {
            bInited = true;
        }
    }

    public void mindwaveStart(){
        nskAlgoSdk.NskAlgoStart(false);
    }

    public void mindwavePause(){
        nskAlgoSdk.NskAlgoPause();
    }


}