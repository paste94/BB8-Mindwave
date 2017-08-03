"Algo SDK Sample" is a sample project to demonstrate how to manipulate data from MindWare Mobile Headset (realtime mode) or canned data (offline mode) and pass to EEG Algo SDK for EEG algorithm analysis. This document explains the use of "libAlgoSdk.so".

Wrapper layer and SDK related data type defines are also included.

Running on Android device (Realtime mode)
=========================================
1. Pairing NeuroSky MindWave Mobile Headset with Android device
2. Import the Algo SDK Sample project (gradle build) with Android Studio
3. Select Build –> Rebuild Project to build the "app" and install the “Algo SDK Sample” app by Run -> Run 'app'
4. In the app,
	4.1. connect the headset to the app by tapping "Headset” button
	4.2. select algorithm(s) by checking the checkboxes on the top-right corner
	4.3. tap “Set Algos” to initialise the Algo SDK with the selected algorithm(s) (by invoking “NskAlgoInit()” method)
	4.4. tap "Start“ to start process any incoming headset data (by invoking "NskAlgoStart()" method)
	4.5. tap "Pause" to pause EEG Algo SDK (by invoking “NskAlgoPause()” method)
	4.6. tap "Stop" to stop EEG Algo SDK (by invoking "NskAlgoStop()" method)
	4.7. tap “AP Index” / “ME Index” / “ME2 Index” to enable the corresponding visual view
	4.8. slide on SeekBar to adjust the output interval of the selected algorithm on step 4.7 and tap “Interval” to confirm

Running on Android device (Offline mode)
========================================
1. Import the Algo SDK Sample project (gradle build) with Android Studio
2. Select Build –> Rebuild Project to build the "app" and install the “Algo SDK Sample” app by Run -> Run 'app'
3. In the app,
	4.2. select algorithm(s) by checking the checkboxes on the top-right corner
	4.3. tap “Set Algos” to initialise the Algo SDK with the selected algorithm(s) (by invoking “NskAlgoInit()” method)
	4.4. tap "Start“ to start process any incoming headset data (by invoking "NskAlgoStart()" method)
	4.5. tap "Pause" to pause EEG Algo SDK (by invoking “NskAlgoPause()” method)
	4.6. tap "Stop" to stop EEG Algo SDK (by invoking "NskAlgoStop()" method)
	4.7. tap “AP Index” / “ME Index” / “ME2 Index” to enable the corresponding visual view
	4.8. slide on SeekBar to adjust the output interval of the selected algorithm on step 4.7 and tap “Interval” to confirm
	4.9. tap “Use Canned Data" to start feeding data to the EEG Algo SDK (by invoking NskAlgoDataStream()” method) from "raw_data_em.bin" and Algorithm Index output will be compared with the canned output in "output_data.bin"
