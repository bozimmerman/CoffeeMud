package com.planet_ink.coffee_mud.Exits;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class FlyingExit extends StdExit
{
	public String ID(){	return "FlyingExit";}
	public String Name(){ return "the open air";}
	public String displayText(){ return "";}
	public String description(){ return "Looks like you'll have to fly up there.";}
	public FlyingExit()
	{
		super();
		baseEnvStats().setDisposition(baseEnvStats().disposition()|EnvStats.IS_FLYING);
		recoverEnvStats();
	}
	public Environmental newInstance()
	{
		return new FlyingExit();
	}
}
