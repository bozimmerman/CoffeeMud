package com.planet_ink.coffee_mud.Exits;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class ClimbableExit extends StdExit
{
	public String ID(){	return "ClimbableExit";}
	public String Name(){ return "a sheer surface";}
	public String displayText(){ return "a sheer surface";}
	public String description(){ return "Looks like you'll have to climb it.";}
	
	public ClimbableExit()
	{
		super();
		baseEnvStats().setDisposition(baseEnvStats().disposition()|EnvStats.IS_CLIMBING);
		recoverEnvStats();
	}
	public Environmental newInstance()
	{
		return new ClimbableExit();
	}
}
