package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GateGuard extends StdBehavior
{
	public String ID(){return "GateGuard";}
	public Behavior newInstance()
	{
		return new GateGuard();
	}

	int noticeTock=4;

	private int findGate(MOB mob)
	{
		if(mob.location()==null) return -1;
		if(!mob.location().isInhabitant(mob))
			return -1;
		for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
		{
			if(mob.location().getRoomInDir(d)!=null)
			{
				Exit e=mob.location().getExitInDir(d);
				if(e.hasADoor())
					return d;
			}
		}
		return -1;
	}

	private Key getMyKeyTo(MOB mob, Exit e)
	{
		Key key=null;
		String keyCode=e.keyName();
		for(int i=0;i<mob.inventorySize();i++)
		{
			Item item=mob.fetchInventory(i);
			if((item instanceof Key)&&(((Key)item).getKey().equals(keyCode)))
			{
				key=(Key)item;
				break;
			}
		}
		if(key==null)
		{
			key=(Key)CMClass.getItem("StdKey");
			key.setKey(keyCode);
			mob.addInventory(key);
		}
		return key;
	}

	private int numValidPlayers(MOB mob, Room room)
	{
		if(room==null) return 0;
		int num=0;
		for(int i=0;i<room.numInhabitants();i++)
		{
			MOB M=room.fetchInhabitant(i);
			if((M!=null)
			&&(!M.isMonster())
			&&(Sense.canBeSeenBy(M,mob))
			&&(ExternalPlay.zapperCheck(getParms(),M)))
				num++;
		}
		return num;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);

		if(tickID!=Host.MOB_TICK) return true;
		if(!canFreelyBehaveNormal(ticking)) return true;
		MOB mob=(MOB)ticking;
		int dir=findGate(mob);
		if(dir<0) return true;
		Exit e=mob.location().getExitInDir(dir);
		int numPlayers=numValidPlayers(mob,mob.location());
		if(noticeTock==0)
		{
			if((mob.location().getArea().getTODCode()==Area.TIME_NIGHT))
			{
				if((!e.isLocked())&&(e.hasALock()))
				{
					if(getMyKeyTo(mob,e)!=null)
					{
						FullMsg msg=new FullMsg(mob,e,Affect.MSG_LOCK,"<S-NAME> lock(s) <T-NAME>.");
						if(mob.location().okAffect(mob,msg))
							ExternalPlay.roomAffectFully(msg,mob.location(),dir);
					}
				}
			}
			else
			{
				if(e.isLocked())
				{
					if(getMyKeyTo(mob,e)!=null)
					{
						FullMsg msg=new FullMsg(mob,e,Affect.MSG_UNLOCK,"<S-NAME> unlock(s) <T-NAME>.");
						if(mob.location().okAffect(mob,msg))
							ExternalPlay.roomAffectFully(msg,mob.location(),dir);
					}
				}
				if((numPlayers>0)&&(!e.isOpen()))
				{
					FullMsg msg=new FullMsg(mob,e,Affect.MSG_OPEN,"<S-NAME> open(s) <T-NAME>.");
					if(mob.location().okAffect(mob,msg))
						ExternalPlay.roomAffectFully(msg,mob.location(),dir);
				}
				else
				if((numPlayers==0)&&(e.isOpen()))
				{
					FullMsg msg=new FullMsg(mob,e,Affect.MSG_CLOSE,"<S-NAME> close(s) <T-NAME>.");
					if(mob.location().okAffect(mob,msg))
						ExternalPlay.roomAffectFully(msg,mob.location(),dir);
				}
			}
			noticeTock--;
		}
		else
		if(noticeTock<0)
		{
			if(mob.location().getArea().getTODCode()==Area.TIME_NIGHT)
				noticeTock=5;
			else
			if((e.isLocked())
			||((numPlayers==0)&&(e.isOpen())))
				noticeTock=3;
			else
			if((numPlayers>0)&&(!e.isOpen()))
				noticeTock=0;
		}
		else
			noticeTock--;
		return true;
	}
}