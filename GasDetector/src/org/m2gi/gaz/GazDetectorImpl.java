package org.m2gi.gaz;


import fr.liglab.adele.icasa.device.gasSensor.CarbonMonoxydeSensor;

import org.m2gi.devices.window.WindowDevice;

import java.util.HashMap;
import java.util.Map;
import fr.liglab.adele.icasa.device.gasSensor.CarbonDioxydeSensor;
public class GazDetectorImpl implements GasDetector, Runnable {

	private Thread thread;

	/** Field for coSensors dependency */
	private CarbonMonoxydeSensor[] coSensors;
	/** Field for co2Sensors dependency */
	private CarbonDioxydeSensor[] co2Sensors;
	/** Field for roomWindows dependency */
	private WindowDevice[] roomWindows;
	/** Injected field for the component property coThresholdMin */
	private Double coThresholdMin;
	/** Injected field for the component property coThresholdMax */
	private Double coThresholdMax;
	/** Injected field for the component property detectionDelay */
	private Long detectionDelay;
	/** Injected field for the component property co2ThresholdMin */
	private Double co2ThresholdMin;
	/** Injected field for the component property co2ThresholdMax */
	private Double co2ThresholdMax;

	private HashMap<String, Boolean> tooMuchCO;
	
	private HashMap<String, Boolean> tooMuchCO2;
	
	private boolean fireStarted;

	public GazDetectorImpl() {
		super();
		tooMuchCO = new HashMap<String, Boolean>();
		tooMuchCO2 = new HashMap<String, Boolean>();
		fireStarted = false;
	}

	@Override
	public void run() {
		try {
			Thread.sleep(detectionDelay);
			HashMap<String, Double[]> coOldValues = new HashMap<String, Double[]>();
			HashMap<String, Double[]> co2OldValues = new HashMap<String, Double[]>();
			HashMap<String, Boolean> windowsOpened = new HashMap<String, Boolean>();
			
			while(true) {
				HashMap<String, Boolean> windowsToOpen = new HashMap<String, Boolean>();
				HashMap<String, Boolean> windowsToClose = new HashMap<String, Boolean>();
				for(CarbonMonoxydeSensor sensor: coSensors) {
					String zone = (String) sensor.getPropertyValue("Location");
					
					if(!coOldValues.containsKey(zone)) {
						Double arr[] = {0.0, 0.0, 0.0};
						coOldValues.put(zone, arr);
					}
					if(!windowsOpened.containsKey(zone)) {
						windowsOpened.put(zone, false);
					}
					windowsToOpen.put(zone, false);
					windowsToClose.put(zone, false);
					
					Double currentValue = (Double) sensor.getCOConcentration();
					if(currentValue == null) {
						currentValue = 0.0;
					}

					Double zoneCOOldVals[] = coOldValues.get(zone);
					
					if(!fireStarted && !windowsOpened.get(zone) && shouldOpenWindowInRoom(zoneCOOldVals, currentValue, coThresholdMax)) {
						windowsToOpen.put(zone, true);
					} else if(windowsOpened.get(zone) && shouldCloseWindowInRoom(zoneCOOldVals, currentValue, coThresholdMin)) {
						windowsToClose.put(zone, true);
					}
					
					tooMuchCO.put(zone, shouldOpenWindowInRoom(zoneCOOldVals, currentValue, coThresholdMax));
					
					for(int i = zoneCOOldVals.length - 1; i > 0; i--) {
						zoneCOOldVals[i] = zoneCOOldVals[i - 1];
					}
					zoneCOOldVals[0] = currentValue;
					coOldValues.put(zone, zoneCOOldVals);
				}
				
				for(CarbonDioxydeSensor sensor: co2Sensors) {
					String zone = (String) sensor.getPropertyValue("Location");
					
					if(!co2OldValues.containsKey(zone)) {
						Double arr[] = {0.0, 0.0, 0.0};
						co2OldValues.put(zone, arr);
					}
					if(!windowsOpened.containsKey(zone)) {
						windowsOpened.put(zone, false);
					}
					windowsToOpen.put(zone, false);
					windowsToClose.put(zone, false);
					
					Double currentValue = (Double) sensor.getCO2Concentration();
					if(currentValue == null) {
						currentValue = 0.0;
					}

					Double zoneCO2OldVals[] = co2OldValues.get(zone);
					
					if(!fireStarted && !windowsOpened.get(zone) && shouldOpenWindowInRoom(zoneCO2OldVals, currentValue, co2ThresholdMax)) {
						windowsToOpen.put(zone, true);
					} else if(windowsOpened.get(zone) && shouldCloseWindowInRoom(zoneCO2OldVals, currentValue, co2ThresholdMin)) {
						windowsToClose.put(zone, true);
					}
					
					tooMuchCO2.put(zone, shouldOpenWindowInRoom(zoneCO2OldVals, currentValue, co2ThresholdMax));
					
					for(int i = zoneCO2OldVals.length - 1; i > 0; i--) {
						zoneCO2OldVals[i] = zoneCO2OldVals[i - 1];
					}
					zoneCO2OldVals[0] = currentValue;
					co2OldValues.put(zone, zoneCO2OldVals);
				}
				
				for(String zone: windowsOpened.keySet()) {
					if(windowsToOpen.get(zone)) {
						windowsOpened.put(zone, true);
						openWindowsInRoom(zone);
					} else if(windowsToClose.get(zone)) {
						windowsOpened.put(zone, false);
						closeWindowsInRoom(zone);
					}
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
	
	private boolean shouldOpenWindowInRoom(Double oldValues[], Double currentValue, double thresholdMax) {
		int i = 0;
		while(i < oldValues.length && oldValues[i] > thresholdMax) {
			i++;
		}
		return i == oldValues.length && currentValue > thresholdMax;
	}
	
	private boolean shouldCloseWindowInRoom(Double oldValues[], Double currentValue, double thresholdMin) {
		int i = 0;
		while(i < oldValues.length && oldValues[i] < thresholdMin) {
			i++;
		}
		return i == oldValues.length && currentValue < thresholdMin;
	}

	private void openWindowsInRoom(String location) {
		for(WindowDevice window: roomWindows) {
			if(window.getPropertyValue("Location").equals(location)) {
				System.out.println("Opening the window " + window.getSerialNumber() + " in the zone " + location);
				window.open();
			}
		}
	}
	
	private void closeWindowsInRoom(String location) {
		for(WindowDevice window: roomWindows) {
			if(window.getPropertyValue("Location").equals(location)) {
				System.out.println("Closing the window " + window.getSerialNumber() + " in the zone " + location);
				window.close();
			}
		}
	}

	/** Bind Method for co2Sensors dependency */
	public void bindCO2Sensor(CarbonDioxydeSensor carbonDioxydeSensor, Map properties){
	}

	/** Unbind Method for co2Sensors dependency */
	public void unbindCO2Sensor(CarbonDioxydeSensor carbonDioxydeSensor, Map properties){
	}

	@Override
	public boolean tooMuchCOInZone(String zone) {
		return tooMuchCO.containsKey(zone) && tooMuchCO.get(zone);
	}

	@Override
	public boolean tooMuchCO2InZone(String zone) {
		return tooMuchCO2.containsKey(zone) && tooMuchCO2.get(zone);
	}

	@Override
	public void fireStarted() {
		fireStarted = true;
	}

	@Override
	public void fireStopped() {
		fireStarted = false;
	}

}
