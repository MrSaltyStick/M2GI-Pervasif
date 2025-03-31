package org.m2gi.fire;

import fr.liglab.adele.icasa.device.temperature.Thermometer;

import java.util.HashMap;
import java.util.Map;
import fr.liglab.adele.icasa.service.preferences.Preferences;
import fr.liglab.adele.icasa.device.presence.PresenceSensor;

public class FireDetectorImpl implements FireDetector, Runnable {

	private static final String OUTDOOR_TEMPERATURE = "OUTDOOR_TEMPERATURE";
	
	private Thread thread;
	
	private boolean hasFireStarted;
	
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
	
	public FireDetectorImpl() {
		super();
		this.hasFireStarted = false;
		preferences.setGlobalPropertyValue(OUTDOOR_TEMPERATURE, 50.5f);
	}

	/** Bind Method for thermometers dependency */
	public void bindThermometer(Thermometer thermometer, Map properties) {
	}

	/** Unbind Method for thermometers dependency */
	public void unbindThermometer(Thermometer thermometer, Map properties) {
	}

	/** Component Lifecycle Method */
	public void stop() {
		thread.interrupt();
	}

	/** Component Lifecycle Method */
	public void start() {
		thread = new Thread(this);
		thread.start();
		System.out.println("Fire detector started");
	}

	@Override
	public void run() {
		try {
			HashMap<String, Double[]> oldValues = new HashMap<String, Double[]>();
			while(true) {
				Thread.sleep(detectionDelay);
				
				for(Thermometer thermometer: thermometers) {
					String zone = (String) thermometer.getPropertyValue("Location");
					if(!oldValues.containsKey(zone)) {
						oldValues.put(zone, new Double[]{ 0.0, 0.0, 0.0 });
					}
					
					double celsiusTemperature = thermometer.getTemperature() - 272.15;
					Double zoneOldVals[] = oldValues.get(zone);					
					if(hasFireStarted(zoneOldVals, celsiusTemperature)) {
						fireStarted(zone);
					}
					
					for(int i = zoneOldVals.length - 1; i > 0; i--) {
						zoneOldVals[i] = zoneOldVals[i - 1];
					}
					zoneOldVals[0] = celsiusTemperature;
					oldValues.put(zone, zoneOldVals);
				}
			}
		} catch(InterruptedException e) {
		}
	}
	
	private boolean hasFireStarted(Double[] zoneOldVals, double currentTemperature) {
		float outdoorTemperature = (Float) preferences.getGlobalPropertyValue(OUTDOOR_TEMPERATURE);
		double threshold = Math.max(temperatureThreshold, outdoorTemperature * 1.2);
		
		int i = 0;
		while(i < zoneOldVals.length && zoneOldVals[i] > threshold) {
			i++;
		}
		return i == zoneOldVals.length && currentTemperature > threshold;
	}
	
	private void fireStarted(String location) {
		System.out.println("ALERT a fire has started in the room " + location);
		this.hasFireStarted = true;
		for(PresenceSensor sensor: presenceSensors) {
			if(sensor.getSensedPresence()) {
				System.out.println("There is at least one person in the room " + sensor.getPropertyValue("Location"));
			}
		}
	}

	/** Bind Method for presenceSensors dependency */
	public void bindPresenceSensor(PresenceSensor presenceSensor, Map properties){
		System.out.println("Test");
	}

	/** Unbind Method for presenceSensors dependency */
	public void unbindPresenceSensor(PresenceSensor presenceSensor, Map properties){
	}

	@Override
	public boolean hasFireStarted() {
		return this.hasFireStarted;
	}

	
}
