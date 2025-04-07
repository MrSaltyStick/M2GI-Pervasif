package org.m2gi.commands;

import fr.liglab.adele.icasa.command.handler.Command;
import org.apache.felix.ipojo.annotations.Instantiate;
import fr.liglab.adele.icasa.command.handler.CommandProvider;
import fr.liglab.adele.icasa.service.preferences.Preferences;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;

@Component
@Instantiate(name = "CommandManager")
@CommandProvider(namespace = "CommandManager")
public class CommandManagerImpl implements Commands {
	
	
	/** Field for preferences dependency */
	private Preferences preferences;
	
	
	@Command
	@Override
	public void CreateFireFlat(String room) {
		
		SetHeat(400);
		SetCo2(8000);
		SetCo(100);

	}
	
	@Command
	@Override
	public void CreateFireLog(String room) {
		// TODO Auto-generated method stub

	}

	@Override
	@Command
	public void CreateGasLeak(String room) {
		// TODO Auto-generated method stub

	}

	@Override
	@Command
	public void SetHeat(float heat) {
		System.out.println("hey test");

		preferences.setGlobalPropertyValue("OUTDOOR_TEMPERATURE", heat);
	}

	@Override
	@Command
	public void SetCo2(float gas) {
		
		preferences.setGlobalPropertyValue("OUTDOOR_TEMPERATURE", gas);
		preferences.setGlobalPropertyValue("OUTDOOR_TEMPERATURE", gas);

	}

	@Override
	@Command
	public void SetCo(float gas) {
		// TODO Auto-generated method stub

	}

}
