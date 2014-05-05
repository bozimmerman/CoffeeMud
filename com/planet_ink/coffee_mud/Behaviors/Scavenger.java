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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;

/*
   Copyright 2000-2014 Bo Zimmerman

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
@SuppressWarnings({"unchecked","rawtypes"})
public class Scavenger extends ActiveTicker
{
	@Override public String ID(){return "Scavenger";}
	@Override protected int canImproveCode(){return Behavior.CAN_MOBS;}
	int origItems=-1;

	public Scavenger()
	{
		super();
		minTicks=1; maxTicks=10; chance=99;
		origItems=-1;
		tickReset();
	}

	@Override
	public String accountForYourself()
	{
		return "refuse scavenging";
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);

		if((canAct(ticking,tickID))&&(ticking instanceof MOB))
		{
			final MOB mob=(MOB)ticking;
			final Room thisRoom=mob.location();
			if(origItems<0) origItems=mob.numItems();
			if((mob.phyStats().weight()>=(int)Math.round(CMath.mul(mob.maxCarry(),0.9)))
			||(mob.numItems()>=mob.maxItems()))
			{
				if(CMLib.flags().isATrackingMonster(mob)) return true;
				final String trashRoomID=CMParms.getParmStr(getParms(),"TRASH","");
				if(trashRoomID.equalsIgnoreCase("NO"))
					return true;
				final Room R=CMLib.map().getRoom(trashRoomID);
				if(mob.location()==R)
				{
					Container C=null;
					int maxCapacity=0;
					for(int i=0;i<R.numItems();i++)
					{
						final Item I=R.getItem(i);
						if((I instanceof Container)&&(I.container()==null)&&(!CMLib.flags().isGettable(I)))
						{
							if(((Container)I).capacity()>maxCapacity)
							{
								C=(Container)I;
								maxCapacity=((Container)I).capacity();
							}
						}
					}
					if(C!=null)
						mob.doCommand(new XVector("PUT","ALL",C.Name()),Command.METAFLAG_FORCED);
					else
						mob.doCommand(new XVector("DROP","ALL"),Command.METAFLAG_FORCED);
					CMLib.tracking().wanderAway(mob,false,true);
				}
				else
				if(R!=null)
				{
					final Ability A=CMLib.flags().isTracking(mob) ? null : CMClass.getAbility("Skill_Track");
					if(A!=null)
						A.invoke(mob,CMParms.parse("\""+CMLib.map().getExtendedRoomID(R)+"\""),R,true,0);
				}
				else
				if((origItems>=0)&&(mob.numItems()>origItems))
				{
					while((origItems>=0)&&(mob.numItems()>origItems))
					{
						final Item I=mob.getItem(origItems);
						if(I==null)
						{
							if(origItems>0)
								origItems--;
							break;
						}
						if(I.owner()==null) I.setOwner(mob);
						I.destroy();
					}
					mob.recoverPhyStats();
					mob.recoverCharStats();
					mob.recoverMaxState();
				}
			}
			if((thisRoom==null)||(thisRoom.numItems()==0))
				return true;
			if(thisRoom.numPCInhabitants()>0)
				return true;
			Vector choices=new Vector(thisRoom.numItems()<1000?thisRoom.numItems():1000);
			for(int i=0;(i<thisRoom.numItems())&&(choices.size()<1000);i++)
			{
				final Item thisItem=thisRoom.getItem(i);
				if((thisItem!=null)
				&&(thisItem.container()==null)
				&&(CMLib.flags().isGettable(thisItem))
				&&(!(thisItem instanceof DeadBody)))
					choices.addElement(thisItem);
			}
			if(choices.size()==0) return true;
			final Item I=(Item)choices.elementAt(CMLib.dice().roll(1,choices.size(),-1));
			if((I!=null)&&(CMLib.flags().isGettable(I)&&(CMLib.flags().canBeSeenBy(I, mob))))
				mob.doCommand(new XVector("GET",I.Name()),Command.METAFLAG_FORCED);
			choices.clear();
			choices=null;
		}
		return true;
	}
}
