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
                    NskAlgoSdk.NskAlgoUninit();
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
                    NskAlgoSdk.NskAlgoDataStream(NskAlgoDataType.NSK_ALGO_DATA_TYPE_ATT.value, attValue, 1);
                    break;
                case MindDataType.CODE_MEDITATION:
                    /*
                    short medValue[] = {(short)data};
                    nskAlgoSdk.NskAlgoDataStream(NskAlgoDataType.NSK_ALGO_DATA_TYPE_MED.value, medValue, 1);
                    */
                    break;
                case MindDataType.CODE_POOR_SIGNAL:
                    short pqValue[] = {(short)data};
                    NskAlgoSdk.NskAlgoDataStream(NskAlgoDataType.NSK_ALGO_DATA_TYPE_PQ.value, pqValue, 1);
                    //mainActivity.btnEmergencyBrakeListener(null);
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

    MindwaveConnect(MainActivity mainActivity){
        this.mainActivity = mainActivity;
    }

    void connect(){

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Example of constructor public TgStreamReader(BluetoothAdapter ba, TgStreamHandler tgStreamHandler)
        tgStreamReader = new TgStreamReader(mBluetoothAdapter,callback);

        //Prima c'era anche il controllo: tgStreamReader != null &&
        if(tgStreamReader.isBTConnected()) {

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
        nskAlgoSdk.setOnAttAlgoIndexListener(new NskAlgoSdk.OnAttAlgoIndexListener() {
            @Override
            public void onAttAlgoIndex(int value) {
                Log.d(TAG, "NskAlgoAttAlgoIndexListener: Attention:" + value);
                String attStr = "[" + value + "]";
                if(value<50){
                    mainActivity.performLowLevelAction();
                }
                else if(value>=50 && value<80){
                    mainActivity.performMidLevelAction();
                }
                else if(value >=80){
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
                if(NskAlgoSignalQuality.values()[level].toString().equals("POOR")){
                    mainActivity.btnEmergencyBrakeListener(null);
                }
            }
        });
        NskAlgoSdk.NskAlgoStart(false);
    }

    void mindwavePause(){
        NskAlgoSdk.NskAlgoPause();
    }


}