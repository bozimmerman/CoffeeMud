package com.planet_ink.coffee_mud.interfaces;

import java.util.Vector;
public interface Command extends Comparable
{
	public String[] getAccessWords();
	public int ticksToExecute();
	public boolean canBeOrdered();
	public boolean securityCheck(MOB mob);
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException;
}
