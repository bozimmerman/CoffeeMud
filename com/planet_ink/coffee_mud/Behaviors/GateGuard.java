package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GateGuard extends StdBehavior
{
	int tickTock=0;
	public GateGuard()
	{
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
	}
	public Behavior newInstance()
	{
		return new GateGuard();
	}
	
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
	
	public void tick(Environmental ticking, int tickID)
	{
		super.tick(ticking,tickID);

		if(tickID!=Host.MOB_TICK) return;
		if(!canFreelyBehaveNormal(ticking)) return;
		MOB mob=(MOB)ticking;
		int dir=findGate(mob);
		if(dir<0) return;
		Exit e=mob.location().getExitInDir(dir);
		int numPlayers=mob.location().numPCInhabitants();
		boolean nightTime=(mob.location().getArea().getTODCode()==Area.TIME_NIGHT);
		tickTock++;
		if(tickTock>2)
		{
			tickTock=0;
			if(nightTime)
			{
				if((!e.isLocked())&&(e.hasALock()))
				{
					if(getMyKeyTo(mob,e)!=null)
					{
						FullMsg msg=new FullMsg(mob,e,Affect.MSG_LOCK,"<S-NAME> lock(s) <T-NAME>.");
						if(mob.location().okAffect(msg))
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
						if(mob.location().okAffect(msg))
							ExternalPlay.roomAffectFully(msg,mob.location(),dir);
					}
				}
				if((numPlayers>0)&&(!e.isOpen()))
				{
					FullMsg msg=new FullMsg(mob,e,Affect.MSG_OPEN,"<S-NAME> open(s) <T-NAME>.");
					if(mob.location().okAffect(msg))
						ExternalPlay.roomAffectFully(msg,mob.location(),dir);
				}
				else
				if((numPlayers==0)&&(e.isOpen()))
				{
					FullMsg msg=new FullMsg(mob,e,Affect.MSG_OPEN,"<S-NAME> close(s) <T-NAME>.");
					if(mob.location().okAffect(msg))
						ExternalPlay.roomAffectFully(msg,mob.location(),dir);
				}
			}
		}
	}
}