package com.planet_ink.coffee_mud.Exits;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class HiddenWalkway extends Open
{
	public String ID(){	return "HiddenWalkway";}
	public HiddenWalkway()
	{
		super();
		baseEnvStats().setDisposition(baseEnvStats().disposition()|EnvStats.IS_HIDDEN);
		recoverEnvStats();
	}
	public Environmental newInstance()
	{
		return new HiddenWalkway();
	}

}
