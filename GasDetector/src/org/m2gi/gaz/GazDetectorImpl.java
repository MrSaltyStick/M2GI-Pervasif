package org.m2gi.gaz;


import fr.liglab.adele.icasa.device.gasSensor.CarbonMonoxydeSensor;

import org.m2gi.devices.window.WindowDevice;

import java.util.HashMap;
import java.util.Map;
public class GazDetectorImpl implements Runnable {

	private Thread thread;

	/** Field for coSensors dependency */
	private CarbonMonoxydeSensor[] coSensors;
	/** Field for roomWindows dependency */
	private WindowDevice[] roomWindows;
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
			while(true) {
				System.out.println("======== Gas detector report ========");
				for(CarbonMonoxydeSensor sensor: coSensors) {
					String zone = (String) sensor.getPropertyValue("Location");
					System.out.println(zone.toUpperCase());
					
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
					System.out.print("   Last temperatures: [" + currentValue);
					for(int i = 0; i < zoneOldVals.length; i++) {
						System.out.print(", " + zoneOldVals[i]);
					}
					System.out.println("]");
					
					if(!windowsOpened.get(zone) && shouldOpenWindowInRoom(zoneOldVals, currentValue)) {
						windowsOpened.put(zone, true);
						openWindowsInRoom(zone);
					} else if(windowsOpened.get(zone) && shouldCloseWindowInRoom(zoneOldVals, currentValue)) {
						windowsOpened.put(zone, false);
						closeWindowsInRoom(zone);
					}
					System.out.println("   Windows opened: " + windowsOpened.get(zone));
					
					for(int i = zoneOldVals.length - 1; i > 0; i--) {
						zoneOldVals[i] = zoneOldVals[i - 1];
					}
					zoneOldVals[0] = currentValue;
					oldValues.put(zone, zoneOldVals);
					System.out.println();
				}
				Thread.sleep(detectionDelay);
			}
		} catch(InterruptedException e) {
		}
	}

	/** Bind Method for coSensors dependency */
	public void bindCoSensor(CarbonMonoxydeSensor carbonMonoxydeSensor, Map properties){
		System.out.println("Binding the COSensor device " + carbonMonoxydeSensor.getSerialNumber() + " to the gas detector");
	}

	/** Unbind Method for coSensors dependency */
	public void unbindCoSensor(CarbonMonoxydeSensor carbonMonoxydeSensor, Map properties){
		System.out.println("Unbinding the COSensor device " + carbonMonoxydeSensor.getSerialNumber() + "from the gas detector");
	}

	/** Bind Method for roomWindows dependency */
	public void bindRoomWindow(WindowDevice windowDevice, Map properties){
		System.out.println("Binding the window device " + windowDevice.getSerialNumber() + " to the gas detector");
	}

	/** Unbind Method for roomWindows dependency */
	public void unbindRoomWindow(WindowDevice windowDevice, Map properties){
		System.out.println("Unbinding the window device " + windowDevice.getSerialNumber() + " from the gas detector");
	}

	/** Component Lifecycle Method */
	public void stop(){
		thread.interrupt();
		System.out.println("Gas detector stopped");
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
