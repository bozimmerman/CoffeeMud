package com.planet_ink.coffee_mud.Behaviors;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class CommonSpeaker extends StdBehavior
{
	public String ID(){return "CommonSpeaker";}
	public Behavior newInstance()
	{
		return new CommonSpeaker();
	}

	int tickTocker=1;
	int tickTock=0;
	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);

		if(tickID!=MudHost.TICK_MOB) return true;
		if(--tickTock>0) return true;

		Ability L=CMClass.getAbility("Common");
		if(L!=null) L.invoke((MOB)ticking,null,true);
		if((++tickTocker)==100) tickTocker=99;
		tickTock=tickTocker;
		return true;
	}
}
