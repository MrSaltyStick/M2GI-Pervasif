package org.m2gi.fire;

import fr.liglab.adele.icasa.device.temperature.Thermometer;

import java.util.HashMap;
import java.util.Map;

public class FireDetectorImpl implements Runnable {

	private Thread thread;
	
	/** Field for thermometers dependency */
	private Thermometer[] thermometers;

	/** Injected field for the component property detectionDelay */
	private Long detectionDelay;

	/** Injected field for the component property temperatureThreshold */
	private Double temperatureThreshold;
	
	public FireDetectorImpl() {
		super();
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
						System.out.println("Ho fada y a un incendie dans la pièce " + zone);
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
		int i = 0;
		while(i < zoneOldVals.length && zoneOldVals[i] > temperatureThreshold) {
			i++;
		}
		return i == zoneOldVals.length && currentTemperature > temperatureThreshold;
	}
	
}
