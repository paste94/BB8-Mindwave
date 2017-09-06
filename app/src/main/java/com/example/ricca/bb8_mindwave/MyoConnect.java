package com.example.ricca.bb8_mindwave;

import android.widget.Toast;

import com.thalmic.myo.AbstractDeviceListener;
import com.thalmic.myo.Arm;
import com.thalmic.myo.DeviceListener;
import com.thalmic.myo.Hub;
import com.thalmic.myo.Myo;
import com.thalmic.myo.Pose;
import com.thalmic.myo.Quaternion;
import com.thalmic.myo.XDirection;

class MyoConnect {

    private MainActivity mainActivity;
    private boolean b = false;
    private boolean rotate = false;
    private double gap = 0;
    private DeviceListener lockedListener = new AbstractDeviceListener() {
        @Override
        public void onAttach(Myo myo, long timestamp) {
            mainActivity.setTxtMyoStatus("Myo band found! Bring near the device to connect it");
            mainActivity.myoConnected(false);
        }
        @Override
        public void onDetach(Myo myo, long timestamp){
            mainActivity.setTxtMyoStatus("No bracelets nearby");
            mainActivity.myoConnected(false);
        }
        @Override
        public void onConnect (Myo myo, long timestamp){
            mainActivity.setTxtMyoStatus("Connected!");
            mainActivity.myoConnected(true);
        }
        @Override
        public void onDisconnect (Myo myo, long timestamp){
            mainActivity.setTxtMyoStatus("Disconnected!");
            mainActivity.myoConnected(false);
        }
        @Override
        public void onArmSync (Myo myo, long timestamp, Arm arm, XDirection xDirection){
            mainActivity.setTxtMyoStatus("Connected!");
            mainActivity.myoConnected(true);
        }
        @Override
        public void onArmUnsync (Myo myo, long timestamp){
            mainActivity.setTxtMyoStatus("Please put the bracelet on your arm");
            mainActivity.myoConnected(false);
        }
        @Override
        public void onUnlock (Myo myo, long timestamp){
            super.onUnlock(myo, timestamp);
            if(!mainActivity.getIsBB8Connected()){
                myo.lock();
                mainActivity.showToast("Wait until robot is connected!", Toast.LENGTH_SHORT);
            }
        }
        @Override
        public void onPose (Myo myo, long timestamp, Pose pose){
            if(pose.equals(Pose.FIST)){
                if(mainActivity.getIsBB8Connected()) {
                    myo.vibrate(Myo.VibrationType.SHORT);
                    myo.unlock(Myo.UnlockType.HOLD);
                    if(!mainActivity.isDriving()) {
                        mainActivity.startCalibratingRobot();
                    }
                    else {
                        mainActivity.stopCalibratingRobot();
                    }
                    b = true;
                }
            }
            else if(pose.equals(Pose.REST)){
                myo.unlock(Myo.UnlockType.TIMED);
                //myo.lock();
                if(mainActivity.getIsBB8Connected()) {
                    mainActivity.stopCalibratingRobot();
                    rotate = false;
                    gap = 0;
                }
            }
            else if(pose.equals(Pose.FINGERS_SPREAD)){
                if(mainActivity.getIsBB8Connected()) {
                    myo.vibrate(Myo.VibrationType.SHORT);
                    mainActivity.mindwaveStartListener(null);
                }
            }
            else if (pose.equals(Pose.DOUBLE_TAP)){
                if(mainActivity.getIsBB8Connected()){
                    myo.vibrate(Myo.VibrationType.SHORT);
                    mainActivity.btnEmergencyBrakeListener(null);
                }
            }
        }
        @Override
        public void onOrientationData(Myo myo, long timestamp, Quaternion rotation) {
            if(b){
                gap = Quaternion.roll(rotation);
                b = false;
                rotate = true;
            }
            if(rotate){
                float rot = (float)(Math.toDegrees(Quaternion.roll(rotation) - gap));
                mainActivity.rotateRobot(calculateNewPosition((float) (rot * 1.5)));
                mainActivity.setTxtMyoStatus((rot*1.5) + "");
            }
        }
    };

    MyoConnect(MainActivity mainActivity){
        this.mainActivity = mainActivity;
        Hub hub = Hub.getInstance();
        if (!hub.init(mainActivity.getApplicationContext())) {
            mainActivity.finish();
        }
    }

    void connect(){
        Hub.getInstance().attachToAdjacentMyo();
        Hub.getInstance().setLockingPolicy(Hub.LockingPolicy.STANDARD);
        Hub.getInstance().addListener(lockedListener);
    }
    void disconnect(){
        Hub.getInstance().detach(Hub.getInstance().getConnectedDevices().get(0).getMacAddress());
    }

    private float calculateNewPosition(float rotation){
        if(rotation < 0){
            return rotation + 360;
        }
        else if(rotation > 360){
            return rotation - 360;
        }
        else{
            return rotation;
        }
    }
}
