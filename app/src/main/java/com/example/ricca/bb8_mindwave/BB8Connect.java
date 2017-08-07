package com.example.ricca.bb8_mindwave;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
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

/**
 * Created by ricca on 07/08/17.
 */

public class BB8Connect implements RobotChangedStateListener {
    MainActivity mainActivity;
    //Orbotix classes
    private DiscoveryAgentLE _discoveryAgent;
    private static ConvenienceRobot robot;
    private DiscoveryAgentEventListener _discoveryAgentEventListener = new DiscoveryAgentEventListener() {
        @Override
        public void handleRobotsAvailable(List<Robot> robots) {
            mainActivity.setDaStatus("Found " + robots.size() + " robots");

            for (Robot robot : robots) {
                mainActivity.setDaStatus(mainActivity.getDaStatus().toString() + "\n" + robot.getName());
            }
        }

    };
    private RobotChangedStateListener robotStateListener = new RobotChangedStateListener() {
        @Override
        public void handleRobotChangedState(Robot r, RobotChangedStateNotificationType robotChangedStateNotificationType) {
            switch (robotChangedStateNotificationType) {
                case Online:
                    stopDiscovery();


                    mainActivity.setRobotStatus("Robot " + r.getName() + " is Online!");
                    robot = new Sphero(r);

                    // Finally for visual feedback let's turn the robot green saying that it's been connected
                    robot.setLed(0f, 1f, 0f);

                    robot.enableCollisions(true); // Enabling the collisions detector

                    mainActivity.enableColorButtons(true);

                    //newGameButton.setEnabled(true);
                    //newGameButton.setClickable(true);
                    break;
                case Offline:
                    mainActivity.setRobotStatus("Robot " + r.getName() + " is now Offline!");
                    //newGameButton.setClickable(false);
                    //newGameButton.setEnabled(false);
                    startDiscovery();
                    break;
                case Connecting:
                    mainActivity.setRobotStatus("Connecting to " + r.getName());
                    //newGameButton.setClickable(false);
                    //newGameButton.setEnabled(false);
                    break;
                case Connected:
                    mainActivity.setRobotStatus("Connected to " + r.getName());
                    //newGameButton.setClickable(false);
                    //newGameButton.setEnabled(false);
                    break;
                case Disconnected:
                    mainActivity.setRobotStatus("Disconnected from " + r.getName());
                    //newGameButton.setClickable(false);
                    //newGameButton.setEnabled(false);
                    startDiscovery();
                    break;
                case FailedConnect:
                    mainActivity.setRobotStatus("Failed to connect to " + r.getName());
                    //newGameButton.setClickable(false);
                    //newGameButton.setEnabled(false);
                    startDiscovery();
                    break;
            }
        }
    };


    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        mainActivity.reactivateBluetoothOrLocation();
                        break;
                    case BluetoothAdapter.STATE_ON:
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        break;
                }
            }
        }
    };

    public BB8Connect(MainActivity ma){
        this.mainActivity = ma;
        DiscoveryAgentLE.getInstance().addRobotStateListener(this);
    }

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

    public void startDiscovery() {
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

    public BroadcastReceiver getReceiver(){
        return this.mReceiver;
    }

    public void setRobotLed(int r, int g, int b){
        this.robot.setLed(r, g, b);
    }
    
    public void moveForward(float heading, float velocity ){
        this.robot.drive(heading, velocity);
    }

    public void stopRobot(){
        this.robot.stop();
    }

    public static ConvenienceRobot getRobot(){
        return robot;
    }

    @Override
    public void handleRobotChangedState(Robot robot, RobotChangedStateNotificationType robotChangedStateNotificationType) {

    }
}
