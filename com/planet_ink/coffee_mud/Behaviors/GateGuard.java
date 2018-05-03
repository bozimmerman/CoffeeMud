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

/*
   Copyright 2002-2018 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public class GateGuard extends StdBehavior
{
	@Override
	public String ID()
	{
		return "GateGuard";
	}

	protected int noticeTock=4;
	protected boolean heardKnock=false;
	protected boolean keepLocked=false;
	protected boolean allnight=false;

	@Override
	public String accountForYourself()
	{
		return "gate guarding";
	}

	@Override
	public void setParms(String parm)
	{
		super.setParms(parm);
		keepLocked=false;
		allnight=false;
		final Vector<String> V=CMParms.parse(parm);
		for(int v=0;v<V.size();v++)
		{
			if(V.elementAt(v).equalsIgnoreCase("keeplocked"))
			{
				keepLocked=true;
				V.removeElementAt(v);
				break;
			}
			else
			if(V.elementAt(v).equalsIgnoreCase("allnight"))
			{
				allnight=true;
				V.removeElementAt(v);
				break;
			}
		}
	}

	protected int findGate(MOB mob)
	{
		if(!CMLib.flags().isInTheGame(mob,false))
			return -1;
		final Room R=mob.location();
		if(R!=null)
		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		{
			if(R.getRoomInDir(d)!=null)
			{
				final Exit e=R.getExitInDir(d);
				if((e!=null)&&(e.hasADoor()))
					return d;
			}
		}
		return -1;
	}

	protected DoorKey getMyKeyTo(MOB mob, Exit e)
	{
		DoorKey key=null;
		final String keyCode=e.keyName();
		for(int i=0;i<mob.numItems();i++)
		{
			final Item item=mob.getItem(i);
			if((item instanceof DoorKey)&&(((DoorKey)item).getKey().equals(keyCode)))
			{
				key=(DoorKey)item;
				break;
			}
		}
		if(key==null)
		{
			key=(DoorKey)CMClass.getItem("StdKey");
			key.setKey(keyCode);
			mob.addItem(key);
		}
		return key;
	}

	protected int numValidPlayers(MOB mob, Room room)
	{
		if(room==null)
			return 0;
		int num=0;
		for(int i=0;i<room.numInhabitants();i++)
		{
			final MOB M=room.fetchInhabitant(i);
			if((M!=null)
			&&(!M.isMonster())
			&&(CMLib.flags().canBeSeenBy(M,mob))
			&&(CMLib.masking().maskCheck(getParms(),M,false)))
				num++;
		}
		return num;
	}

	@Override
	public void executeMsg(Environmental host, CMMsg msg)
	{
		if(host instanceof MOB)
		{
			final MOB mob=(MOB)host;
			if((msg.targetMinor()==CMMsg.TYP_KNOCK)
			&&(!msg.amISource(mob))
			&&(mob.location()!=null)
			&&(mob.location()!=msg.source().location())
			&&(!heardKnock)
			&&(CMLib.flags().canHear(mob))
			&&(canFreelyBehaveNormal(host)))
			{
				final int dir=findGate(mob);
				if((dir>=0)
				&&(CMLib.masking().maskCheck(getParms(),msg.source(),false)))
				{
					final Exit e=mob.location().getExitInDir(dir);
					if(msg.amITarget(e))
						heardKnock=true;
				}
			}
		}
		super.executeMsg(host,msg);
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);

		if(tickID!=Tickable.TICKID_MOB)
			return true;
		if(!canFreelyBehaveNormal(ticking))
			return true;
		final MOB mob=(MOB)ticking;
		final int dir=findGate(mob);
		if(dir<0)
			return true;
		final Exit e=mob.location().getExitInDir(dir);
		int numPlayers=numValidPlayers(mob,mob.location());
		if(noticeTock==0)
		{
			if(heardKnock)
				numPlayers++;
			if((!allnight)&&(mob.location().getArea().getTimeObj().getTODCode()==TimeClock.TimeOfDay.NIGHT))
			{
				if((!e.isLocked())&&(e.hasALock()))
				{
					if(getMyKeyTo(mob,e)!=null)
					{
						final CMMsg msg=CMClass.getMsg(mob,e,CMMsg.MSG_LOCK,L("<S-NAME> lock(s) <T-NAME>."));
						if(mob.location().okMessage(mob,msg))
							CMLib.utensils().roomAffectFully(msg,mob.location(),dir);
					}
				}
			}
			else
			{
				if((e.isLocked())&&((!keepLocked)||(numPlayers>0)))
				{
					if(getMyKeyTo(mob,e)!=null)
					{
						final CMMsg msg=CMClass.getMsg(mob,e,CMMsg.MSG_UNLOCK,L("<S-NAME> unlock(s) <T-NAME>."));
						if(mob.location().okMessage(mob,msg))
							CMLib.utensils().roomAffectFully(msg,mob.location(),dir);
					}
				}
				if((numPlayers>0)&&(!e.isOpen())&&(!e.isLocked()))
				{
					mob.doCommand(CMParms.parse("OPEN "+CMLib.directions().getDirectionName(dir)),MUDCmdProcessor.METAFLAG_FORCED);
				}
				if((numPlayers==0)&&(e.isOpen()))
				{
					mob.doCommand(CMParms.parse("CLOSE "+CMLib.directions().getDirectionName(dir)),MUDCmdProcessor.METAFLAG_FORCED);
				}
				if((numPlayers==0)&&(!e.isOpen())&&(!e.isLocked())&&(e.hasALock())&&(keepLocked))
				{
					if(getMyKeyTo(mob,e)!=null)
					{
						final CMMsg msg=CMClass.getMsg(mob,e,CMMsg.MSG_LOCK,L("<S-NAME> lock(s) <T-NAME>."));
						if(mob.location().okMessage(mob,msg))
							CMLib.utensils().roomAffectFully(msg,mob.location(),dir);
					}
				}
			}
			heardKnock=false;
			noticeTock--;
		}
		else
		if(noticeTock<0)
		{
			if(heardKnock)
				numPlayers++;
			if(mob.location().getArea().getTimeObj().getTODCode()==TimeClock.TimeOfDay.NIGHT)
				noticeTock=5;
			else
			if((e.isLocked())||((numPlayers==0)&&(e.isOpen())))
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
