package com.planet_ink.coffee_mud.Exits;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;


public class Grate extends StdClosedDoorway
{
	public String ID(){	return "Grate";}
	public String Name(){return "a barred grate";}
	public String doorName(){return "grate";}
	public String closedText(){return "a closed grate";}
	public String description(){return "A metal grate of thick steel bars is inset here.";}
	public String closeWord(){return "close";}
	public String openWord(){return "remove";}
	public Environmental newInstance()
	{
		return new Grate();
	}

}
