package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Decay extends ActiveTicker
{
	public String ID(){return "Decay";}
	protected int canImproveCode(){return Behavior.CAN_ITEMS|Behavior.CAN_MOBS;}
	public Decay()
	{
		minTicks=50;maxTicks=50;chance=100;
		tickReset();
	}

	boolean activated=false;

	public Behavior newInstance()
	{
		return new Decay();
	}

	public void setParms(String newParms)
	{
		super.setParms(newParms);
		tickDown=getParmVal(parms,"remain",tickDown);
	}

	public String getParms()
	{
		String s=getParms();
		int x=s.toUpperCase().indexOf("REMAIN=");
		if(x<0) return "remain="+tickDown+" "+s;
		int y=s.indexOf(" ",x+1);
		if(y<0) y=s.length();
		return ("remain="+tickDown+" "+s.substring(0,x)+s.substring(y).trim()).trim();
	}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if(!activated) return true;
		if(canAct(ticking,tickID))
		{
			if(ticking instanceof MOB)
			{
				MOB mob=(MOB)ticking;
				Room room=mob.location();
				if(room!=null)
				{
					if(mob.amDead()) mob.setLocation(null);
					mob.destroy();
					room.recoverRoomStats();
				}
			}
			else
			if(ticking instanceof Item)
			{
				Item item=(Item)ticking;
				Environmental E=item.owner();
				if(E==null) return true;
				Room room=getBehaversRoom(ticking);
				if(room==null) return true;
				item.destroy();
				if(E instanceof MOB)
				{
					((MOB)E).tell(item.name()+" vanishes!");
					((MOB)E).recoverEnvStats();
					((MOB)E).recoverCharStats();
					((MOB)E).recoverMaxState();
				}
				else
				if(E instanceof Room)
					((Room)E).showHappens(Affect.MSG_OK_VISUAL,item.name()+" vanishes!");
				room.recoverRoomStats();
			}
		}
		return true;
	}

	/** this method defines how this thing responds
	 * to environmental changes.  It may handle any
	 * and every affect listed in the Affect class
	 * from the given Environmental source */
	public void affect(Environmental affecting, Affect affect)
	{
		super.affect(affecting,affect);
		if(activated) return;
		if(affect.amITarget(affecting))
		{
			if(affecting instanceof MOB)
			{
				if(((affect.targetMajor()&Affect.MASK_MALICIOUS)>0)
				&&(!affect.source().isMonster()))
					activated=true;
			}
			else
			if((affecting instanceof Armor)
			||(affecting instanceof Weapon))
			{
				if((affect.targetMinor()==Affect.TYP_WEAR)
				||(affect.targetMinor()==Affect.TYP_HOLD)
				||(affect.targetMinor()==Affect.TYP_WIELD))
					activated=true;
			}
			else
			if(affecting instanceof Item)
			{
				if(affect.targetMinor()==Affect.TYP_GET)
					activated=true;
			}
		}
	}
}
