package com.planet_ink.coffee_mud.Exits;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class OpenDescriptable extends StdExit
{
	public String ID(){	return "OpenDescriptable";}
	public String Name(){ return "the ground";}
	public String displayText(){ return miscText;}
	public String description(){ return miscText;}
	public Environmental newInstance()
	{
		return new OpenDescriptable();
	}
}
