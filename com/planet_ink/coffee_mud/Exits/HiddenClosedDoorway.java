package com.planet_ink.coffee_mud.Exits;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class HiddenClosedDoorway extends StdClosedDoorway
{
	public String ID(){	return "HiddenClosedDoorway";}
	public String description(){return "a cleverly concealed door.";}
	public HiddenClosedDoorway()
	{
		super();
		baseEnvStats().setDisposition(baseEnvStats().disposition()|EnvStats.IS_HIDDEN);
		recoverEnvStats();
	}
	public Environmental newInstance()
	{
		return new HiddenClosedDoorway();
	}

}
