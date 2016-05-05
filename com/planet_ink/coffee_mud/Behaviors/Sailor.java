package com.planet_ink.coffee_mud.Behaviors;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;


public class Sailor extends StdBehavior
{

	@Override
	public String ID()
	{
		return "Aggressive";
	}

	@Override
	public long flags()
	{
		return Behavior.FLAG_POTENTIALLYAGGRESSIVE | Behavior.FLAG_TROUBLEMAKING;
	}

	public Sailor()
	{
		// TODO Auto-generated constructor stub
	}

	protected volatile int	tickDown	= -1;
	protected int			tickWait	= -1;
	protected BoardableShip	loyalShip	= null;
	
	@Override
	public String accountForYourself()
	{
		if(getParms().trim().length()>0)
			return "aggression against "+CMLib.masking().maskDesc(getParms(),true).toLowerCase();
		else
			return "aggressiveness";
	}

	@Override
	public void setParms(String newParms)
	{
		super.setParms(newParms);
		tickWait = CMParms.getParmInt(newParms, "TICKDELAY", -1);
		loyalShip	= null;
	}
	
	@Override
	public void executeMsg(Environmental affecting, CMMsg msg)
	{
		super.executeMsg(affecting, msg);
		if(((msg.sourceMinor()==CMMsg.TYP_ENTER)||(msg.sourceMinor()==CMMsg.TYP_LEAVE))
		&&(msg.source().riding() instanceof BoardableShip))
		{
			//TODO: stop combat signal
		}
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if((tickID!=Tickable.TICKID_MOB)
		||(!(ticking instanceof MOB)))
			return true;
		if(tickWait < 0)
		{
			if(ticking instanceof Physical)
				tickWait = ((Physical)ticking).phyStats().level() / 4;
			else
				tickWait = 10000;
		}
		
		if((--tickDown)<0)
		{
			tickDown=tickWait;
			if(ticking instanceof MOB)
			{
				final MOB mob=(MOB)ticking;
				if((loyalShip==null)
				&&(mob.location()!=null)
				&&(mob.location().getArea() instanceof BoardableShip))
					loyalShip = (BoardableShip)mob.location().getArea();
			}
		}
		return true;
	}
}
