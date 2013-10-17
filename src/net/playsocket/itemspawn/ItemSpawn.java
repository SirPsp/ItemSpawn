package net.playsocket.itemspawn;

import java.io.File;
import java.util.ArrayList;

import net.canarymod.Canary;
import net.canarymod.commandsys.CommandDependencyException;
import net.canarymod.plugin.Plugin;
import net.visualillusionsent.utils.PropertiesFile;

public class ItemSpawn extends Plugin {

	public static String directory = "config" + File.separator;
	public static PropertiesFile props = new PropertiesFile(directory + File.separator + "ItemSpawn.cfg");
	static ArrayList<Integer> disallowed = new ArrayList<Integer>();

	public void disable() {
		getLogman().logInfo(getName() + " v" + getVersion() + " disabled");
	}

	public boolean enable() {
		new File(directory).mkdir();
		getDisallowedList();
//		Canary.help().registerCommand(this,ItemSpawnCommands.class," <ID> [amount] [damage] [player] - Gives items");
//		Canary.help().registerCommand(this,"/clearinventory"," - removes all items from inventory");
        try {
			Canary.commands().registerCommands(new ItemSpawnCommands(), this, true);
		} catch (CommandDependencyException e) {
			e.printStackTrace();
		}
        getLogman().logInfo(getName() + " v"+ getVersion() + " by "+ getAuthor() +" enabled");
        getLogman().logInfo("ItemSpawn disallowed: "+disallowed);
        return true;
	}
	
	public static void getDisallowedList(){
		disallowed.clear();
		if (!props.containsKey("disallowed-items")){
			props.setString("disallowed-items", "0,7");
			props.save();
		}
		String[] list = props.getString("disallowed-items").split(",");
		if (list.length > 0){
			for (int i = 0; i < list.length; i++){
				disallowed.add(Integer.parseInt(list[i]));
			}
		}
	}

}
