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
    //Orbotix classes
    private DiscoveryAgentLE _discoveryAgent;
    private static ConvenienceRobot robot;
    private DiscoveryAgentEventListener _discoveryAgentEventListener = new DiscoveryAgentEventListener() {
        @Override
        public void handleRobotsAvailable(List<Robot> robots) {
            mainActivity.setTxtRobotStatus("Robot found!");
        }
    };
    private RobotChangedStateListener robotStateListener = new RobotChangedStateListener() {
        @Override
        public void handleRobotChangedState(Robot r, RobotChangedStateNotificationType robotChangedStateNotificationType) {
            switch (robotChangedStateNotificationType) {
                case Online:
                    stopDiscovery();


                    mainActivity.setTxtRobotStatus("Robot " + r.getName() + " is Online!");
                    robot = new Sphero(r);

                    // Finally for visual feedback let's turn the robot green saying that it's been connected
                    robot.setLed(0f, 1f, 0f);

                    robot.enableCollisions(true); // Enabling the collisions detector
                    mainActivity.BB8Connected(true);
                    break;
                case Offline:
                    mainActivity.setTxtRobotStatus("Robot " + r.getName() + " is now Offline!");
                    mainActivity.BB8Connected(false);
                    startDiscovery();
                    break;
                case Connecting:
                    mainActivity.setTxtRobotStatus("Connecting to " + r.getName());
                    mainActivity.BB8Connected(false);
                    break;
                case Connected:
                    mainActivity.setTxtRobotStatus("Connected to " + r.getName());
                    mainActivity.BB8Connected(false);
                    break;
                case Disconnected:
                    mainActivity.setTxtRobotStatus("Disconnected from " + r.getName());
                    mainActivity.BB8Connected(false);
                    startDiscovery();
                    break;
                case FailedConnect:
                    mainActivity.setTxtRobotStatus("Failed to connect to " + r.getName());
                    mainActivity.BB8Connected(false);
                    startDiscovery();
                    break;
            }
        }
    };

    BB8Connect(MainActivity ma){
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

    void setRobotLedRed(){
        robot.setLed(1,0,0);
    }

    void setRobotLedGreen(){
        robot.setLed(0,1,0);
    }

    void setRobotLedBlue(){
        robot.setLed(0,0,1);
    }

    void moveForward(double velocity ){
        robot.drive(0, (float) velocity);
    }

    void stopRobot(){
        robot.stop();
    }

    static ConvenienceRobot getRobot(){
        return robot;
    }

    @Override
    public void handleRobotChangedState(Robot robot, RobotChangedStateNotificationType robotChangedStateNotificationType) {

    }
}
