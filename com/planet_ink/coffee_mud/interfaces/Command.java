package com.planet_ink.coffee_mud.interfaces;

import java.util.Vector;
public interface Command
{
	public Vector getAccessWords();
	public boolean execute(MOB mob, Vector commands);
}
