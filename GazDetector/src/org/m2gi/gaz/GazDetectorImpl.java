package org.m2gi.gaz;


import fr.liglab.adele.icasa.device.gasSensor.CarbonMonoxydeSensor;
import org.m2gi.devices.window.WindowDevice;
import org.m2gi.fire.FireDetector;

import java.util.HashMap;
import java.util.Map;
public class GazDetectorImpl implements Runnable {

	private Thread thread;

	/** Field for coSensors dependency */
	private CarbonMonoxydeSensor[] coSensors;
	/** Field for roomWindows dependency */
	private WindowDevice[] roomWindows;
	/** Field for fireDetector dependency */
	private FireDetector fireDetector;
	/** Injected field for the component property coThresholdMax */
	private Double coThresholdMax;
	/** Injected field for the component property detectionDelay */
	private Long detectionDelay;
	/** Injected field for the component property coThresholdMin */
	private Double coThresholdMin;

	public GazDetectorImpl() {
		super();
	}

	@Override
	public void run() {
		try {
			Thread.sleep(detectionDelay);
			HashMap<String, Double[]> oldValues = new HashMap<String, Double[]>();
			HashMap<String, Boolean> windowsOpened = new HashMap<String, Boolean>();
			System.out.println("Nb detectors: " + coSensors.length);
			System.out.println("Nb windows: " + roomWindows.length);
			System.out.println("Max: " + coThresholdMax);
			while(true) {
				
				for(CarbonMonoxydeSensor sensor: coSensors) {
					if(fireDetector.hasFireStarted()) {
						break;
					}
					
					String zone = (String) sensor.getPropertyValue("Location");
					
					if(!oldValues.containsKey(zone)) {
						Double arr[] = {0.0, 0.0, 0.0};
						oldValues.put(zone, arr);
					}
					if(!windowsOpened.containsKey(zone)) {
						windowsOpened.put(zone, false);
					}
					
					Double currentValue = (Double) sensor.getCOConcentration();
					if(currentValue == null) {
						currentValue = 0.0;
					}

					Double zoneOldVals[] = oldValues.get(zone);
					System.out.println(zone + ": " + currentValue);
					if(!windowsOpened.get(zone) && shouldOpenWindowInRoom(zoneOldVals, currentValue)) {
						windowsOpened.put(zone, true);
						System.out.println("Opening windows in the room " + zone);
						openWindowsInRoom(zone);
					} else if(windowsOpened.get(zone) && shouldCloseWindowInRoom(zoneOldVals, currentValue)) {
						windowsOpened.put(zone, false);
						System.out.println("Closing windows in the room " + zone);
						closeWindowsInRoom(zone);
					}
					
					for(int i = zoneOldVals.length - 1; i > 0; i--) {
						zoneOldVals[i] = zoneOldVals[i - 1];
					}
					zoneOldVals[0] = currentValue;
					oldValues.put(zone, zoneOldVals);
				}
				Thread.sleep(detectionDelay);
			}
		} catch(InterruptedException e) {
		}
	}

	/** Bind Method for coSensors dependency */
	public void bindCoSensor(CarbonMonoxydeSensor carbonMonoxydeSensor, Map properties){
	}

	/** Unbind Method for coSensors dependency */
	public void unbindCoSensor(CarbonMonoxydeSensor carbonMonoxydeSensor, Map properties){
	}

	/** Bind Method for roomWindows dependency */
	public void bindRoomWindow(WindowDevice windowDevice, Map properties){
	}

	/** Unbind Method for roomWindows dependency */
	public void unbindRoomWindow(WindowDevice windowDevice, Map properties){
	}

	/** Component Lifecycle Method */
	public void stop(){
		thread.interrupt();
	}

	/** Component Lifecycle Method */
	public void start(){
		thread = new Thread(this);
		thread.start();
		System.out.println("Gas detector started");
	}
	
	private boolean shouldOpenWindowInRoom(Double oldValues[], Double currentValue) {
		int i = 0;
		while(i < oldValues.length && oldValues[i] > coThresholdMax) {
			i++;
		}
		return i == oldValues.length && currentValue > coThresholdMax;
	}
	
	private boolean shouldCloseWindowInRoom(Double oldValues[], Double currentValue) {
		int i = 0;
		while(i < oldValues.length && oldValues[i] < coThresholdMin) {
			i++;
		}
		return i == oldValues.length && currentValue < coThresholdMin;
	}

	private void openWindowsInRoom(String location) {
		for(WindowDevice window: roomWindows) {
			if(window.getPropertyValue("Location").equals(location)) {
				window.open();
			}
		}
	}
	
	private void closeWindowsInRoom(String location) {
		for(WindowDevice window: roomWindows) {
			if(window.getPropertyValue("Location").equals(location)) {
				window.close();
			}
		}
	}

}
