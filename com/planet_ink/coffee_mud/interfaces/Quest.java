package com.planet_ink.coffee_mud.interfaces;
import java.util.Vector;

public interface Quest extends Tickable
{
	// the unique name of the quest
	public String name();
	public void setName(String newName);
	
	// the duration, in ticks
	public int duration();
	public void setDuration(int newTicks);
	
	// the rest of the script.  This may be semicolon-seperated instructions, 
	// or a LOAD command followed by the quest script path.
	public void setScript(String parm);
	public String script();
	
	// this will execute the quest script.  If the quest is running, it 
	// will call stopQuest first to shut it down.
	public void startQuest();
	
	// this will stop executing of the quest script.  It will clean up 
	// any objects or mobs which may have been loaded, restoring map 
	// mobs to their previous state.  If the quest is autorandom, it 
	// will restart the waiting process
	public void stopQuest();
	
	// these refer the objects designated during the quest
	public int wasQuestMob(String name);
	public int wasQuestItem(String name);
	public int wasQuestObject(String name);
	public boolean isQuestObject(String name, int i);
	public boolean isQuestObject(Environmental E);
	public String getQuestObjectName(int i);
	public String getQuestMobName(int i);
	public String getQuestItemName(int i);
	public Environmental getQuestObject(int i);
	public MOB getQuestMob(int i);
	public Item getQuestItem(int i);
	
	// if the quest has a winner, this is him.
	public void declareWinner(String mobName);
	// retreive the list of previous winners
	public Vector getWinners();
	// retreive the list of previous winners as a string
	public String getWinnerStr();
	// was a previous winner
	public boolean wasWinner(String name);
	// set winners list from a ; delimited string
	public void setWinners(String list);
	
	// for waiting...
	public int minWait();
	public void setMinWait(int wait);
	public int waitInterval();
	public void setWaitInterval(int wait);
	public void autostartup();
	
	// informational
	public boolean running();
	public boolean waiting();
	public int ticksRemaining();
	public int minsRemaining();
	public int waitRemaining();
}
