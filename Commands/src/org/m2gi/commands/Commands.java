package org.m2gi.commands;

public interface Commands {
	
	void CreateFireLog(String room);
	
	
	void CreateFireFlat(String room);
	
	void CreateGasLeak(String room);
	
	void SetHeat(float heat);
	
	void SetCo2(float gas);
	
	void SetCo(float gas);

}
