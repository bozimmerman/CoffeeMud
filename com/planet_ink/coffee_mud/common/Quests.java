package com.planet_ink.coffee_mud.common;
import com.planet_ink.coffee_mud.interfaces.*;

public class DefaultQuest implements Cloneable, Quest
{
	public String ID(){return "DefaultQuest";}
	protected String name="unnamed quest";
	public String name(){return name;}
	public boolean tick(Tickable ticking, int tickID)
	{
		return true;
	}
}
