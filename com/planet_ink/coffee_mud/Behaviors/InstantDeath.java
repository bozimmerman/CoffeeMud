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
			if((M!=null)&&((M.baseEnvStats().level()<=CommonStrings.getIntVar(CommonStrings.SYSTEMI_LASTPLAYERLEVEL))
							||(CommonStrings.getIntVar(CommonStrings.SYSTEMI_LASTPLAYERLEVEL)==0)))
				V.addElement(M);
		}
		for(int v=0;v<V.size();v++)
		{
			MOB M=(MOB)V.elementAt(v);
			MUDFight.postDeath(null,M,null);
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
					MUDFight.postDeath(null,(MOB)E,null);
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
	 * and every message listed in the CMMsg interface
	 * from the given Environmental source */
	public void executeMsg(Environmental affecting, CMMsg msg)
	{
		super.executeMsg(affecting,msg);
		if(activated) return;
		if(msg.amITarget(affecting))
		{
			if(affecting instanceof MOB)
			{
				if(((msg.targetMajor()&CMMsg.MASK_MALICIOUS)>0)
				&&(!msg.source().isMonster()))
					activated=true;
			}
			else
			if((affecting instanceof Food)
			||(affecting instanceof Drink))
			{
				if((msg.targetMinor()==CMMsg.TYP_EAT)
				||(msg.targetMinor()==CMMsg.TYP_DRINK))
					activated=true;
			}
			else
			if((affecting instanceof Armor)
			||(affecting instanceof Weapon))
			{
				if((msg.targetMinor()==CMMsg.TYP_WEAR)
				||(msg.targetMinor()==CMMsg.TYP_HOLD)
				||(msg.targetMinor()==CMMsg.TYP_WIELD))
					activated=true;
			}
			else
			if(affecting instanceof Item)
			{
				if(msg.targetMinor()==CMMsg.TYP_GET)
					activated=true;
			}
			else
				activated=true;
		}
	}
}