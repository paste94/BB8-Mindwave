package com.example.ricca.bb8_mindwave;

import android.util.Log;
import android.widget.Toast;

import com.orbotix.ConvenienceRobot;
import com.orbotix.Sphero;
import com.orbotix.common.DiscoveryAgentEventListener;
import com.orbotix.common.DiscoveryException;
import com.orbotix.common.Robot;
import com.orbotix.common.RobotChangedStateListener;
import com.orbotix.le.DiscoveryAgentLE;
import com.orbotix.le.RobotRadioDescriptor;

import java.util.List;

class BB8Connect implements RobotChangedStateListener {
    private MainActivity mainActivity;
    private DiscoveryAgentLE _discoveryAgent;
    private static ConvenienceRobot robot;
    private DiscoveryAgentEventListener _discoveryAgentEventListener = new DiscoveryAgentEventListener() {
        @Override
        public void handleRobotsAvailable(List<Robot> robots) {
            mainActivity.setTxtRobotStatus("Robot found! Bring near the device to connect it");
        }
    };
    private RobotChangedStateListener robotStateListener = new RobotChangedStateListener() {
        @Override
        public void handleRobotChangedState(Robot r, RobotChangedStateNotificationType robotChangedStateNotificationType) {
            switch (robotChangedStateNotificationType) {
                case Online:
                    online(r);
                    break;
                case Offline:
                    offline(r);
                    break;
                case Connecting:
                    connecting(r);
                    break;
                case Connected:
                    connected(r);
                    break;
                case Disconnected:
                    disconnected(r);
                    break;
                case FailedConnect:
                    failedConnect(r);
                    break;
            }
        }
    };

    BB8Connect(MainActivity ma){
        this.mainActivity = ma;
        DiscoveryAgentLE.getInstance().addRobotStateListener(this);
    }

    //Ricerca bluetooth
    private void stopDiscovery() {
        // When a robot is connected, this is a good time to stop discovery. Discovery takes a lot of system
        // resources, and if left running, will cause your app to eat the user's battery up, and may cause
        // your application to run slowly. To do this, use DiscoveryAgent#stopDiscovery().
        _discoveryAgent.stopDiscovery();

        // It is also proper form to not allow yourself to re-register for the discovery listeners, so let's
        // unregister for the available notifications here using DiscoveryAgent#removeDiscoveryListener().
        _discoveryAgent.removeDiscoveryListener(_discoveryAgentEventListener);
        _discoveryAgent.removeRobotStateListener(robotStateListener);
        _discoveryAgent = null;
    }
    void startDiscovery() {
        try {
            mainActivity.showToast("Inizio ricerca robot", Toast.LENGTH_LONG);
            _discoveryAgent = DiscoveryAgentLE.getInstance();

            // DiscoveryAgentLE serve a mandare una notifica appena trova un robot.
            // Per fare ciò ha bisogno di un elenco di handler degli eventi, fornitigli
            // dall'implementazione del metodo handleRobotsAvailable nella classe DiscoveryAgentEventListener
            _discoveryAgent.addDiscoveryListener(_discoveryAgentEventListener);

            // Allo stesso modo settiamo l'handler per il cambiamento di stato del robot
            _discoveryAgent.addRobotStateListener(robotStateListener);

            // Creating a new radio descriptor to be able to connect to the BB8 robots
            RobotRadioDescriptor robotRadioDescriptor = new RobotRadioDescriptor();
            robotRadioDescriptor.setNamePrefixes(new String[]{"BB-"});
            _discoveryAgent.setRadioDescriptor(robotRadioDescriptor);

            // Then to start looking for a BB8, you use DiscoveryAgent#startDiscovery()
            // You do need to handle the discovery exception. This can occur in cases where the user has
            // Bluetooth off, or when the discovery cannot be started for some other reason.
            _discoveryAgent.startDiscovery(mainActivity.getApplicationContext());
        } catch (DiscoveryException e) {
            Log.e("Sphero", "Discovery Error: " + e);
            e.printStackTrace();
        }
    }

    //Led del robot
    void setRobotLedRed(){
        robot.setLed(1,0,0);
    }
    void setRobotLedGreen(){
        robot.setLed(0,1,0);
    }
    void setRobotLedBlue(){
        robot.setLed(0,0,1);
    }

    //Movimento
    void moveForward(double rotation, double velocity ){
        robot.drive((float) rotation, (float) velocity);
    }
    void stopRobot(){
        //robot.stop();
        robot.drive(0, 0);
    }

    //get-set
    static ConvenienceRobot getRobot(){
        return robot;
    }

    //cambi di stato del robot
    private void online(Robot r){
        stopDiscovery();
        mainActivity.setTxtRobotStatus("Robot " + r.getName() + " is Online!");
        robot = new Sphero(r);
        robot.setLed(0f, 1f, 0f);
        mainActivity.BB8Connected(true);
    }
    private void offline(Robot r){
        mainActivity.setTxtRobotStatus("Robot " + r.getName() + " is now Offline!");
        mainActivity.BB8Connected(false);
        startDiscovery();
    }
    private void connecting(Robot r){
        mainActivity.setTxtRobotStatus("Connecting to " + r.getName());
        mainActivity.BB8Connected(false);
    }
    private void connected(Robot r){
        mainActivity.setTxtRobotStatus("Connected to " + r.getName());
        mainActivity.BB8Connected(false);
    }
    private void disconnected(Robot r){
        mainActivity.setTxtRobotStatus("Disconnected from " + r.getName());
        mainActivity.BB8Connected(false);
        startDiscovery();
    }
    private void failedConnect(Robot r){
        mainActivity.setTxtRobotStatus("Failed to connect to " + r.getName());
        mainActivity.BB8Connected(false);
        startDiscovery();
    }
    void startCalibrating(){
        robot.calibrating(true);
    }
    void stopClaibrating(){
        robot.calibrating(false);
    }
    void rotate(float angle){
        //robot.rotate(angle);
        robot.drive(angle, 0);
    }
    float getDirection(){
        return robot.getLastHeading();
    }
    @Override
    public void handleRobotChangedState(Robot robot, RobotChangedStateNotificationType robotChangedStateNotificationType) {
    }
}
