package com.planet_ink.coffee_mud.Behaviors;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class CorpseLooter extends StdBehavior
{
	public String ID(){return "CorpseLooter";}
	public Behavior newInstance()
	{
		return new CorpseLooter();
	}

	int tickTocker=1;
	int tickTock=0;
	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);

		if(tickID!=Host.TICK_MOB) return true;
		if(--tickTock>0) return true;
		((MOB)ticking).setBitmap(Util.setb(((MOB)ticking).getBitmap(),MOB.ATT_AUTOLOOT));
		if((++tickTocker)==100) tickTocker=99;
		tickTock=tickTocker;
		return true;
	}
}
