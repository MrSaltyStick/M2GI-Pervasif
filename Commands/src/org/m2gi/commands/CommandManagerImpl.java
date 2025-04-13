package org.m2gi.commands;

import java.util.HashMap;
import java.util.List;

import fr.liglab.adele.icasa.ContextManager;
import fr.liglab.adele.icasa.command.handler.Command;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Requires;

import fr.liglab.adele.icasa.command.handler.CommandProvider;
import fr.liglab.adele.icasa.location.Zone;
import fr.liglab.adele.icasa.service.preferences.Preferences;

import org.apache.felix.ipojo.annotations.Component;

@Component
@Instantiate(name = "org.m2gi.commands.CommandManager")
@CommandProvider(namespace = "org.m2gi")
public class CommandManagerImpl {
	
	@Requires
	private Preferences preferences;
	
	private HashMap<String, Thread> gasLeakThreads;
	
	@Requires
	private ContextManager contextManager;
	
	public CommandManagerImpl() {
		gasLeakThreads = new HashMap<String, Thread>();
	}
	
	
	@Command
	public void startFire(String zoneID) {		
		boolean success = setZoneVar(zoneID, "RealTemperature", 150.0 + 272.15);
		success = success && setZoneVar(zoneID, "CO2Concentration", 2100.0);
		success = success && setZoneVar(zoneID, "COConcentration", 160.0);
		
		if(success) {
			System.out.println("Fire started in the zone " + zoneID);
		}
	}
	
	@Command
	public void stopFire(String zoneID) {
		boolean success = setZoneVar(zoneID, "RealTemperature", 21.0 + 272.15);
		success = success && setZoneVar(zoneID, "CO2Concentration", 103.0);
		success = success && setZoneVar(zoneID, "COConcentration", 20.0);
		if(success) {
			System.out.println("Fire stopped in the zone " + zoneID);
		}
	}
	
	@Command
	public void startGasLeak(String zoneID) {		
		Zone zone = getZoneByID(zoneID);
		if(zone == null) {
			System.out.println("There is no zone with the ID " + zoneID);
		} else {
			if(!gasLeakThreads.containsKey(zoneID)) {
				Thread thread = new GasLeakThread(zone);
				thread.start();
				gasLeakThreads.put(zoneID, thread);
			} else if(gasLeakThreads.get(zoneID).isInterrupted()) {
				gasLeakThreads.get(zoneID).start();
			}
			System.out.println("Gas leak started in the zone " + zoneID);
		}
	}
	
	@Command
	public void stopGasLeak(String zoneID) {
		Zone zone = getZoneByID(zoneID);
		if(zone == null) {
			System.out.println("There is no zone with the ID " + zoneID);
		} else {
			if(gasLeakThreads.containsKey(zoneID)) {
				gasLeakThreads.get(zoneID).interrupt();
				System.out.println("Gas leak stopped in the zone " + zoneID);
			} else {
				System.out.println("There is no gas leak in the zone " + zoneID);
			}
		}
	}

	@Command
	public void setOutdoorTemperature(String temperature) {
		Float kelvinTemperature = Float.parseFloat(temperature) + 273.15f;
		preferences.setGlobalPropertyValue("OUTDOOR_TEMPERATURE", kelvinTemperature);
		System.out.println("Outdoor temperature set to " + temperature + " celsius degrees");
	}
	
	@Command
	public void setZoneTemperature(String zoneID, String temperature) {
		if(setZoneVar(zoneID, "RealTemperature", Double.parseDouble(temperature) + 273.15)) {
			System.out.println("Temperature set to " + temperature + " celsius degrees in the zone " + zoneID);
		}
	}

	@Command
	public void setCOLevelInZone(String zoneID, String coLevel) {
		if(setZoneVar(zoneID, "COConcentration", Double.parseDouble(coLevel))) {
			System.out.println("Co level set to " + coLevel + " in the zone " + zoneID);
		}
	}

	@Command
	public void setCO2LevelInZone(String zoneID, String co2Level) {
		if(setZoneVar(zoneID, "CO2Concentration", Double.parseDouble(co2Level))) {
			System.out.println("Co2 level set to " + co2Level + " in the zone " + zoneID);
		}
	}


	private Zone getZoneByID(String zoneID) {
		int i = 0;
		final String id = zoneID.toUpperCase();
		List<Zone> zones = contextManager.getZones();
		
		while(i < zones.size() && !zones.get(i).getId().toUpperCase().equals(id)) {
			i++;
		}
		
		return i < zones.size() ? zones.get(i) : null;
	}
	
	private boolean setZoneVar(String zoneID, String varName, Object value) {
		Zone zone = getZoneByID(zoneID);
		if(zone == null) {
			System.out.println("There is no zone with the ID " + zoneID);
		} else {
			zone.setVariableValue(varName, value);
			return true;
		}
		return false;
	}
	
	private static class GasLeakThread extends Thread {
		
		private Zone zone;
		
		public GasLeakThread(Zone zone) {
			super();
			this.zone = zone;
		}



		@Override
		public void run() {
			try {
				final double STEP = 5.0;
				
				while(true) {
					Double coLevel = (Double) zone.getVariableValue("COConcentration");
					
					coLevel += STEP;
					
					zone.setVariableValue("COConcentration", coLevel);
					
					Thread.sleep(2000);
				}
			} catch(InterruptedException ignored) {
			}
		}
		
	}
}
