package com.planet_ink.coffee_mud.Behaviors;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class NoCombatAssist extends StdBehavior
{
	public String ID(){return "NoCombatAssist";}
	public Behavior newInstance()
	{
		return new NoCombatAssist();
	}
	
	int tickTocker=1;
	int tickTock=0;
	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);

		if(tickID!=Host.MOB_TICK) return true;
		if(--tickTock>0) return true;
		((MOB)ticking).setBitmap(((MOB)ticking).getBitmap()|MOB.ATT_AUTOASSIST);
		if((++tickTocker)==100) tickTocker=99;
		tickTock=tickTocker;
		return true;
	}
}
