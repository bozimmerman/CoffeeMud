package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class InstantDeath extends ActiveTicker
{
	public String ID(){return "InstantDeath";}
	public InstantDeath()
	{
		minTicks=1;maxTicks=1;chance=100;
		tickReset();
	}
	
	boolean activated=false;

	public Behavior newInstance()
	{
		return new InstantDeath();
	}

	public void killEveryoneHere(MOB spareMe, Room R)
	{
		if(R==null) return;
		Vector V=new Vector();
		for(int i=0;i<R.numInhabitants();i++)
		{
			MOB M=R.fetchInhabitant(i);
			if((spareMe!=null)&&(spareMe==M))
				continue;
			if(M!=null) V.addElement(M);
		}
		for(int v=0;v<V.size();v++)
		{
			MOB M=(MOB)V.elementAt(v);
			ExternalPlay.postDeath(null,M,null);
		}
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
					killEveryoneHere(mob,room);
			}
			else
			if(ticking instanceof Item)
			{
				Item item=(Item)ticking;
				Environmental E=item.owner();
				if(E==null) return true;
				Room room=getBehaversRoom(ticking);
				if(room==null) return true;
				if(E instanceof MOB)
					ExternalPlay.postDeath(null,(MOB)E,null);
				else
				if(E instanceof Room)
					killEveryoneHere(null,(Room)E);
				room.recoverRoomStats();
			}
			else
			if(ticking instanceof Room)
				killEveryoneHere(null,(Room)ticking);
			else
			if(ticking instanceof Area)
			{
				for(Enumeration r=((Area)ticking).getMap();r.hasMoreElements();)
				{
					Room R=(Room)r.nextElement();
					killEveryoneHere(null,R);
				}
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
			if((affecting instanceof Food)
			||(affecting instanceof Drink))
			{
				if((affect.targetMinor()==Affect.TYP_EAT)
				||(affect.targetMinor()==Affect.TYP_DRINK))
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
			else
				activated=true;
		}
	}
}