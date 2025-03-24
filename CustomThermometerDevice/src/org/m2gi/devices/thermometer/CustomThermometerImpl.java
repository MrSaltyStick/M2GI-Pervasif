package org.m2gi.devices.thermometer;

import java.util.List;
import java.util.Set;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.StaticServiceProperty;

import fr.liglab.adele.icasa.device.DeviceListener;
import fr.liglab.adele.icasa.device.temperature.Thermometer;
import fr.liglab.adele.icasa.device.util.AbstractDevice;
import fr.liglab.adele.icasa.location.LocatedDevice;
import fr.liglab.adele.icasa.location.Position;
import fr.liglab.adele.icasa.location.Zone;
import fr.liglab.adele.icasa.location.ZoneListener;
import fr.liglab.adele.icasa.location.util.EmptyZoneListener;
import fr.liglab.adele.icasa.simulator.SimulatedDevice;

@Component(name = "iCasa.CustomThermometer")
@Provides(properties = { @StaticServiceProperty(type = "java.lang.String", name = "") })
public class CustomThermometerImpl extends AbstractDevice implements Thermometer, SimulatedDevice {

	@ServiceProperty(name = Thermometer.DEVICE_SERIAL_NUMBER, mandatory = true)
	private String m_serialNumber;
	
	private Zone m_zone;
	
	private CustomZoneListener m_zoneListener;
	
	public CustomThermometerImpl() {
		super();
		
		m_zone = null;
		m_zoneListener = new CustomZoneListener();
		
		super.setPropertyValue(SimulatedDevice.LOCATION_PROPERTY_NAME, SimulatedDevice.LOCATION_UNKNOWN);
		
		setPropertyValue(THERMOMETER_CURRENT_TEMPERATURE, 0.0);
	}

	@Override
	public String getSerialNumber() {
		return m_serialNumber;
	}

	@Override
	public void enterInZones(List<Zone> zones) {
		if(zones.size() > 1) {
			throw new RuntimeException("A custom thermometer can't be in two rooms at the same time");
		} else if(zones.size() == 1) {
			if(m_zone != null) {
				m_zone.removeListener(m_zoneListener);
			}
			
			m_zone = zones.get(0);
			m_zone.addListener(m_zoneListener);
			setPropertyValue(THERMOMETER_CURRENT_TEMPERATURE, m_zone.getVariableValue("RealTemperature"));
		}
	}

	@Override
	public double getTemperature() {
		Double val = (Double) getPropertyValue(THERMOMETER_CURRENT_TEMPERATURE);
		if(val == null) {
			return 0.0;
		} else {
			return val;
		}
	}
	
	private class CustomZoneListener extends EmptyZoneListener {

		@Override
		public void zoneVariableModified(Zone zone, String variableName,
				Object oldValue, Object newValue) {
			if(variableName.equals("RealTemperature")) {
				setPropertyValue(THERMOMETER_CURRENT_TEMPERATURE, newValue);
			}
		}

	}

}
