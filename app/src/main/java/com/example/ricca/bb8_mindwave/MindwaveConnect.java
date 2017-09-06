package com.example.ricca.bb8_mindwave;

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


class MindwaveConnect{
    private final String TAG = "MainActivityTag";

    private int algoTypes = 0;// = NskAlgoType.NSK_ALGO_TYPE_CR.value;
    private NskAlgoSdk nskAlgoSdk = new NskAlgoSdk();
    private TgStreamReader tgStreamReader;
    private MainActivity mainActivity;
    private boolean bInited = false;
    private TgStreamHandler callback = new TgStreamHandler() {
        @Override
        public void onStatesChanged(int connectionStates) {
            Log.d(TAG, "connectionStates change to: " + connectionStates);
            switch (connectionStates) {
                case ConnectionStates.STATE_CONNECTING:
                    connecting();
                    break;
                case ConnectionStates.STATE_CONNECTED:
                    connected();
                    break;
                case ConnectionStates.STATE_WORKING:
                    working();
                    break;
                case ConnectionStates.STATE_GET_DATA_TIME_OUT:
                    getDataTimeOut();
                    break;
                case ConnectionStates.STATE_STOPPED:
                    stopped();
                    break;
                case ConnectionStates.STATE_DISCONNECTED:
                    disconnected();
                    break;
                case ConnectionStates.STATE_ERROR:
                    error();
                    break;
                case ConnectionStates.STATE_FAILED: //Quando è spento
                    failed();
                    break;
            }
        }
        @Override
        public void onRecordFail(int flag) {
            Log.e(TAG,"onRecordFail: " +flag);
        }
        @Override
        public void onChecksumFail(byte[] payload, int length, int checksum) {
        }
        @Override
        public void onDataReceived(int datatype, int data, Object obj) {
            switch (datatype) {
                case MindDataType.CODE_ATTENTION:
                    short attValue[] = {(short)data};
                    NskAlgoSdk.NskAlgoDataStream(NskAlgoDataType.NSK_ALGO_DATA_TYPE_ATT.value, attValue, 1);
                    break;
                case MindDataType.CODE_POOR_SIGNAL:
                    short pqValue[] = {(short)data};
                    NskAlgoSdk.NskAlgoDataStream(NskAlgoDataType.NSK_ALGO_DATA_TYPE_PQ.value, pqValue, 1);
                    break;
                default:
                    break;
            }
        }
    };

    MindwaveConnect(MainActivity mainActivity){
        this.mainActivity = mainActivity;
    }

    void connect(){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Example of constructor public TgStreamReader(BluetoothAdapter ba, TgStreamHandler tgStreamHandler)
        tgStreamReader = new TgStreamReader(mBluetoothAdapter,callback);

        //Prima c'era anche il controllo: tgStreamReader != null &&
        if(tgStreamReader.isBTConnected()) {
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
        nskAlgoSdk.setOnAttAlgoIndexListener(new NskAlgoSdk.OnAttAlgoIndexListener() {
            @Override
            public void onAttAlgoIndex(int value) {
                Log.d(TAG, "NskAlgoAttAlgoIndexListener: Attention:" + value);
                String attStr = "[" + value + "]";
                final int midValue = 1;
                final int highValue = 90;
                if(value<midValue){
                    mainActivity.performLowLevelAction();
                }
                else if(value>=midValue && value<highValue){
                    mainActivity.performMidLevelAction();
                }
                else if(value >=highValue){
                    mainActivity.performHighLevelAction();
                }
                mainActivity.setTxtAttention(attStr);
                mainActivity.setAttentionProgressBar(value);
            }
        });
    }

    private void setAlgos() {
        algoTypes += NskAlgoType.NSK_ALGO_TYPE_ATT.value;
        if (bInited) {
            NskAlgoSdk.NskAlgoUninit();
            bInited = false;
        }
        int ret = NskAlgoSdk.NskAlgoInit(algoTypes, mainActivity.getFilesDir().getAbsolutePath());
        if (ret == 0) {
            bInited = true;
        }
    }

    void mindwaveStart(){
        nskAlgoSdk.setOnSignalQualityListener(new NskAlgoSdk.OnSignalQualityListener() {
            @Override
            public void onSignalQuality(int level) {
                //Log.d(TAG, "NskAlgoSignalQualityListener: level: " + level);
                mainActivity.setTxtSignalQuality(NskAlgoSignalQuality.values()[level].toString());
                if(NskAlgoSignalQuality.values()[level].toString().equals("POOR") ||
                        NskAlgoSignalQuality.values()[level].toString().equals("NOT DETECTED")){
                    mainActivity.btnEmergencyBrakeListener(null);
                }
            }
        });
        NskAlgoSdk.NskAlgoStart(false);
    }

    void mindwavePause(){
        NskAlgoSdk.NskAlgoPause();
    }

    private void connecting(){
        //mainActivity.showToast("MindWave is connecting... ", Toast.LENGTH_SHORT);
        System.out.println("MindWave: Connecting... ");
        mainActivity.mindwaveConnected(false);
    }
    private void connected(){
        tgStreamReader.start();
        //mainActivity.showToast("Connected", Toast.LENGTH_SHORT);
        System.out.println("MindWave: Connected!");
        mainActivity.mindwaveConnected(true);
    }
    private void working(){
        //mainActivity.showToast("WORKING", Toast.LENGTH_SHORT);
        setAlgos();
    }
    private void getDataTimeOut(){
        //mainActivity.showToast("Get data time out!", Toast.LENGTH_SHORT);
        if (tgStreamReader != null && tgStreamReader.isBTConnected()) {
            tgStreamReader.stop();
            tgStreamReader.close();
        }
    }
    private void stopped(){
        //mainActivity.showToast("STOPPED", Toast.LENGTH_SHORT);
    }
    private void disconnected(){
        //mainActivity.showToast("DISCONECTED", Toast.LENGTH_SHORT);
        mainActivity.mindwaveConnected(false);
        mainActivity.setTxtSignalQuality("--");
        mainActivity.setTxtAttention("--");
        mainActivity.setAttentionProgressBar(0);
        mainActivity.btnEmergencyBrakeListener(null);
        NskAlgoSdk.NskAlgoUninit();
        connect();
    }
    private void error(){
        mainActivity.mindwaveConnected(false);
    }
    private void failed(){
        mainActivity.showToast("FAILED", Toast.LENGTH_SHORT);
        connect();
    }




}