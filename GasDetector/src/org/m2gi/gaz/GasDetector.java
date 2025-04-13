package org.m2gi.gaz;

public interface GasDetector {
	
	boolean tooMuchCOInZone(String zone);
	
	boolean tooMuchCO2InZone(String zone);
	
	void fireStarted();
	
	void fireStopped();
	
}
