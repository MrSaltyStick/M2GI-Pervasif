package org.m2gi.fire;

import fr.liglab.adele.icasa.device.temperature.Thermometer;

import java.util.HashMap;
import java.util.Map;
import fr.liglab.adele.icasa.service.preferences.Preferences;
import fr.liglab.adele.icasa.device.presence.PresenceSensor;

public class FireDetectorImpl implements Runnable, FireDetector {

	private static final String OUTDOOR_TEMPERATURE = "OUTDOOR_TEMPERATURE";
	
	private Thread thread;
	
	/** Field for thermometers dependency */
	private Thermometer[] thermometers;

	/** Injected field for the component property detectionDelay */
	private Long detectionDelay;

	/** Injected field for the component property temperatureThreshold */
	private Double temperatureThreshold;

	/** Field for preferences dependency */
	private Preferences preferences;

	/** Field for presenceSensors dependency */
	private PresenceSensor[] presenceSensors;
	
	private boolean fireStarted;
	
	public FireDetectorImpl() {
		super();
		fireStarted = false;
	}
	
	@Override
	public boolean fireStarted() {
		return fireStarted;
	}

	/** Bind Method for thermometers dependency */
	public void bindThermometer(Thermometer thermometer, Map properties) {
		System.out.println("Binding the thermometer " + thermometer.getSerialNumber() + " to the fire detector");
	}

	/** Unbind Method for thermometers dependency */
	public void unbindThermometer(Thermometer thermometer, Map properties) {
		System.out.println("Unbinding the thermometer " + thermometer.getSerialNumber() + " from the fire detector");
	}

	/** Component Lifecycle Method */
	public void stop() {
		thread.interrupt();
		System.out.println("Fire detector stopped");
	}

	/** Component Lifecycle Method */
	public void start() {
		thread = new Thread(this);
		thread.start();
		
		float outdoorTemperature = 29.5f;
		preferences.setGlobalPropertyValue("OUTDOOR_TEMPERATURE", outdoorTemperature);
		System.out.println("Outdoor temperature set to " + outdoorTemperature + " celsius degrees");
		
		System.out.println("Fire detector started");
	}

	@Override
	public void run() {
		try {
			HashMap<String, Double[]> oldValues = new HashMap<String, Double[]>();
			while(true) {
				Thread.sleep(detectionDelay);
//				System.out.println("======== Fire detector report ========");
				
				for(Thermometer thermometer: thermometers) {
					String zone = (String) thermometer.getPropertyValue("Location");
//					System.out.println(zone.toUpperCase());
					if(!oldValues.containsKey(zone)) {
						oldValues.put(zone, new Double[]{ 0.0, 0.0, 0.0 });
					}
					double celsiusTemperature = thermometer.getTemperature() - 272.15;
					Double zoneOldVals[] = oldValues.get(zone);
					
//					System.out.print("   Last temperatures: [" + celsiusTemperature);
//					for(int i = 0; i < zoneOldVals.length; i++) {
//						System.out.print(", " + zoneOldVals[i]);
//					}
//					System.out.println("]");
										
					if(hasFireStarted(zoneOldVals, celsiusTemperature)) {
						fireStarted(zone);
					}
					
					for(int i = zoneOldVals.length - 1; i > 0; i--) {
						zoneOldVals[i] = zoneOldVals[i - 1];
					}
					zoneOldVals[0] = celsiusTemperature;
					oldValues.put(zone, zoneOldVals);
//					System.out.println();
				}
			}
		} catch(InterruptedException e) {
		}
	}
	
	private boolean hasFireStarted(Double[] zoneOldVals, double currentTemperature) {
		Float outdoorTemperature = (Float) preferences.getGlobalPropertyValue(OUTDOOR_TEMPERATURE);
		double threshold = Math.max(temperatureThreshold, outdoorTemperature * 1.2);
		
		int i = 0;
		while(i < zoneOldVals.length && zoneOldVals[i] > threshold) {
			i++;
		}
		return i == zoneOldVals.length && currentTemperature > threshold;
	}
	
	private void fireStarted(String location) {
		System.out.println("ALERT a fire has started in the room " + location);
		for(PresenceSensor sensor: presenceSensors) {
			if(sensor.getSensedPresence()) {
				System.out.println("   There is at least one person in the room " + sensor.getPropertyValue("Location"));
			}
		}
	}

	/** Bind Method for presenceSensors dependency */
	public void bindPresenceSensor(PresenceSensor presenceSensor, Map properties){
		System.out.println("Binding the presence sensor " + presenceSensor.getSerialNumber() + " to the fire detector");
	}

	/** Unbind Method for presenceSensors dependency */
	public void unbindPresenceSensor(PresenceSensor presenceSensor, Map properties){
		System.out.println("Unbinding the presence sensor " + presenceSensor.getSerialNumber() + " from the fire detector");
	}
	
}
