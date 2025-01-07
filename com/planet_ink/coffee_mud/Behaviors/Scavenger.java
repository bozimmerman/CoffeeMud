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
import com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TimeOfDay;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary.CompiledZMask;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2001-2024 Bo Zimmerman

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
public class Scavenger extends ActiveTicker
{
	@Override
	public String ID()
	{
		return "Scavenger";
	}

	@Override
	protected int canImproveCode()
	{
		return Behavior.CAN_MOBS;
	}

	protected int			origItems	= -1;
	protected CompiledZMask	mask		= null;
	protected String		trashRoomID	= "";
	protected int 			lastHourDay = -1;
	protected boolean		doAtNight	= false;
	protected Room			returnR		= null;

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
		return "scavenging";
	}

	@Override
	public void setParms(final String newParms)
	{
		super.setParms(newParms);
		String argParms=newParms;
		String maskStr="";
		final int x=newParms.indexOf(';');
		if(x>=0)
		{
			argParms=newParms.substring(0,x);
			maskStr=newParms.substring(x+1);
		}
		trashRoomID=CMParms.getParmStr(argParms,"TRASH","");
		doAtNight=CMParms.getParmBool(argParms,"ATNIGHT",false);
		mask=(maskStr.length()==0)?null:CMLib.masking().getPreCompiledMask(maskStr);
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(ticking instanceof MOB)
		{
			final MOB mob=(MOB)ticking;
			final Room thisRoom=mob.location();
			if(thisRoom == null)
				return true;
			if(origItems<0)
				origItems=mob.numItems();
			boolean canAct = canAct(ticking,tickID);
			if(doAtNight && (CMLib.time().homeClock(mob).getTODCode()==TimeOfDay.NIGHT))
				canAct = true;
			if(canAct)
			{
				if((mob.phyStats().weight()>=(int)Math.round(CMath.mul(mob.maxCarry(),0.9)))
				||(mob.numItems()>=mob.maxItems())
				||(doAtNight && (mob.numItems()>origItems)
							&& ((CMLib.time().homeClock(mob).getTODCode()==TimeOfDay.NIGHT)
							&&(CMLib.time().homeClock(mob).getDayOfMonth()!=lastHourDay))))
				{
					if(CMLib.flags().isATrackingMonster(mob))
						return true;
					if(trashRoomID.equalsIgnoreCase("NO"))
						return true;
					final Room R=(trashRoomID.length()>0)?CMLib.map().getRoom(trashRoomID):null;
					if((mob.location()==R)&&(R!=null))
					{
						if(doAtNight)
							lastHourDay = CMLib.time().homeClock(mob).getDayOfMonth();
						Container C=null;
						int maxCapacity=0;
						for(int i=0;i<R.numItems();i++)
						{
							final Item I=R.getItem(i);
							if((I instanceof Container)
							&&(I.container()==null)
							&&(!CMLib.flags().isGettable(I)))
							{
								if(((Container)I).capacity()>maxCapacity)
								{
									C=(Container)I;
									maxCapacity=((Container)I).capacity();
								}
							}
						}
						if(C!=null)
							mob.doCommand(new XVector<String>("PUT","ALL",C.Name()),MUDCmdProcessor.METAFLAG_FORCED);
						else
							mob.doCommand(new XVector<String>("DROP","ALL"),MUDCmdProcessor.METAFLAG_FORCED);
						if(this.doAtNight) // a signal to include money
						{
							if(C!=null)
								mob.doCommand(new XVector<String>("PUT","ALL",C.Name()),MUDCmdProcessor.METAFLAG_FORCED);
							else
								mob.doCommand(new XVector<String>("DROP","ALL"),MUDCmdProcessor.METAFLAG_FORCED);
						}
						if(returnR == null)
							CMLib.tracking().wanderAway(mob,false,true);
						else
							CMLib.tracking().wanderCheckedFromTo(mob, returnR, false);
					}
					else
					if(R!=null)
					{
						final Ability A=CMLib.flags().isTracking(mob) ? null : CMClass.getAbility("Skill_Track");
						if(A!=null)
						{
							if(doAtNight)
								returnR = mob.location();
							A.invoke(mob,CMParms.parse("\""+CMLib.map().getExtendedRoomID(R)+"\" NPC"),R,true,0);
						}
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
							if(I.owner()==null)
								I.setOwner(mob);
							I.destroy();
						}
						mob.recoverPhyStats();
						mob.recoverCharStats();
						mob.recoverMaxState();
					}
				}
				if((thisRoom.numItems()==0)||(thisRoom.numPCInhabitants()>0))
					return true;
				List<Item> choices=new ArrayList<Item>(thisRoom.numItems()<1000?thisRoom.numItems():1000);
				for(int i=0;(i<thisRoom.numItems())&&(choices.size()<1000);i++)
				{
					final Item thisItem=thisRoom.getItem(i);
					if((thisItem!=null)
					&&(thisItem.container()==null)
					&&(CMLib.flags().isGettable(thisItem))
					&&(CMLib.flags().canBeSeenBy(thisItem, mob))
					&&(!(thisItem instanceof DeadBody))
					&&(mask==null)||(CMLib.masking().maskCheck(mask, thisItem, false)))
						choices.add(thisItem);
				}
				if(choices.size()==0)
					return true;
				final Item I=choices.get(CMLib.dice().roll(1,choices.size(),-1));
				if(I!=null)
					mob.doCommand(new XVector<String>("GET","$"+I.Name()+"$"),MUDCmdProcessor.METAFLAG_FORCED);
				choices.clear();
				choices=null;
			}
		}
		return true;
	}
}
