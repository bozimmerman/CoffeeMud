package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_MOBEmoter extends Property
{
	public String ID(){return "Prop_MOBEmoter";}
	
	Behavior emoter=null;
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((ticking instanceof MOB)&&(tickID==MudHost.TICK_MOB))
		{
			if(emoter==null) 
			{
				emoter=CMClass.getBehavior("Emoter");
				emoter.setParms(text());
			}
			if(!emoter.tick(ticking,tickID))
			{
				if(Util.getParmInt(emoter.getParms(),"expires",0)>0)
					((MOB)ticking).delEffect(this);
			}
		}
		return true;
	}
}
