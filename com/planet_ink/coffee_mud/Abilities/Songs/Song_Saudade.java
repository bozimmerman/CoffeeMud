package com.planet_ink.coffee_mud.Abilities.Songs;
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
   Copyright 2020-2020 Bo Zimmerman

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
public class Song_Saudade extends Song
{
	@Override
	public String ID()
	{
		return "Song_Saudade";
	}

	private final static String localizedName = CMLib.lang().L("Saudade");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_OTHERS;
	}

	@Override
	protected boolean HAS_QUANTITATIVE_ASPECT()
	{
		return true;
	}

	protected volatile int count=0;

	protected boolean forceRecall(final MOB mob, Room recallRoom)
	{
		final Room recalledRoom = mob.location();
		if(recalledRoom == null)
			return false;
		if(recalledRoom == recallRoom)
			return true;
		final CMMsg msg=CMClass.getMsg(mob,recalledRoom,this,CMMsg.MSG_RECALL,CMMsg.MSG_LEAVE,CMMsg.MSG_RECALL,L("<S-NAME> disappear(s) into the Java Plane!"));
		final CMMsg msg2=CMClass.getMsg(mob,recallRoom,this,CMMsg.MASK_MOVE|CMMsg.TYP_RECALL,CMMsg.MASK_MOVE|CMMsg.MSG_ENTER,CMMsg.MASK_MOVE|CMMsg.TYP_RECALL,null);
		recalledRoom.okMessage(mob,msg);
		recallRoom.okMessage(mob,msg2);
		recallRoom=(Room)msg2.target();
		if(mob.isInCombat())
			mob.makePeace(false);
		recalledRoom.send(mob,msg);
		recallRoom.send(mob,msg2);
		if(!recallRoom.isInhabitant(mob))
		{
			recallRoom.bringMobHere(mob, false);
			CMLib.commands().postLook(mob, true);
		}
		if(recalledRoom.isInhabitant(mob))
			recalledRoom.delInhabitant(mob);
		return recallRoom.isInhabitant(mob) && (!recalledRoom.isInhabitant(mob));
	}

	protected boolean canPersonallyRecall(final MOB mob, final Room recallRoom)
	{
		final Room recalledRoom = mob.location();
		if(recalledRoom == null)
			return false;
		if(recalledRoom == recallRoom)
			return true;
		final CMMsg msg=CMClass.getMsg(mob,recalledRoom,this,CMMsg.MSG_RECALL,CMMsg.MSG_LEAVE,CMMsg.MSG_RECALL,L("<S-NAME> disappear(s) into the Java Plane!"));
		return mob.okMessage(mob, msg);
	}

	protected boolean attemptNormalRecall(final MOB mob, Room recallRoom)
	{
		final Room recalledRoom = mob.location();
		if(recalledRoom == null)
			return false;
		if(recalledRoom == recallRoom)
			return true;
		final CMMsg msg=CMClass.getMsg(mob,recalledRoom,this,CMMsg.MSG_RECALL,CMMsg.MSG_LEAVE,CMMsg.MSG_RECALL,L("<S-NAME> disappear(s) into the Java Plane!"));
		final CMMsg msg2=CMClass.getMsg(mob,recallRoom,this,CMMsg.MASK_MOVE|CMMsg.TYP_RECALL,CMMsg.MASK_MOVE|CMMsg.MSG_ENTER,CMMsg.MASK_MOVE|CMMsg.TYP_RECALL,null);
		if(recalledRoom.okMessage(mob,msg))
		{
			if(recallRoom.okMessage(mob,msg2))
			{
				recallRoom=(Room)msg2.target();
				if(mob.isInCombat())
					mob.makePeace(false);
				recalledRoom.send(mob,msg);
				recallRoom.send(mob,msg2);
				return recallRoom.isInhabitant(mob) && (!recalledRoom.isInhabitant(mob));
			}
		}
		return false;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(!(affected instanceof MOB))
			return true;
		final Room targetRoom = CMLib.map().getStartRoom(invoker());
		final MOB mob=(MOB)affected;
		if((targetRoom != null) && (mob!=null))
		{
			count++;
			if((targetRoom == mob.location())
			&&(mob != invoker()))
				super.unSingMe(mob, invoker(), true);
			else
			{
				switch(count)
				{
				case 3:
				{
					if(attemptNormalRecall(mob,targetRoom))
						return super.unSingMe(mob, invoker(), true);
					break;
				}
				case 6:
				{
					if(attemptNormalRecall(mob,targetRoom))
						return super.unSingMe(mob, invoker(), true);
					final Area sA=CMLib.map().areaLocation(mob);
					final Area tA=CMLib.map().areaLocation(targetRoom);
					if((sA != null)
					&&(tA != null)
					&&(sA.fetchEffect("Prop_NoRecall")!=null)
					&&(CMLib.flags().getPlaneOfExistence(sA)==null)
					&&(canPersonallyRecall(mob, targetRoom))
					&&(sA.getTimeObj() == tA.getTimeObj()))
					{
						if(forceRecall(mob,targetRoom))
							return super.unSingMe(mob, invoker(), true);
					}
					break;
				}
				case 8:
				{
					if(attemptNormalRecall(mob,targetRoom))
						return super.unSingMe(mob, invoker(), true);
					final Area sA=CMLib.map().areaLocation(mob);
					final Area tA=CMLib.map().areaLocation(targetRoom);
					if((sA != null)
					&&(tA != null)
					&&(!canPersonallyRecall(mob, targetRoom))
					&&(CMLib.flags().getPlaneOfExistence(sA)==null)
					&&(sA.getTimeObj() == tA.getTimeObj()))
					{
						if(forceRecall(mob,targetRoom))
							return super.unSingMe(mob, invoker(), true);
					}
					break;
				}
				case 10:
				{
					if(attemptNormalRecall(mob,targetRoom))
						return super.unSingMe(mob, invoker(), true);
					final Area sA=CMLib.map().areaLocation(mob);
					final Area tA=CMLib.map().areaLocation(targetRoom);
					if((sA != null)
					&&(tA != null)
					&&(sA.getTimeObj() != tA.getTimeObj())
					&&(CMLib.flags().getPlaneOfExistence(sA)==null)
					)
					{
						if(forceRecall(mob,targetRoom))
							return super.unSingMe(mob, invoker(), true);
					}
					break;
				}
				default:
				{
					if(count >= 15)
					{
						if(forceRecall(mob,targetRoom))
							return super.unSingMe(mob, invoker(), true);
					}
					break;
				}
				}
			}
		}
		return true;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		count=super.getXLEVELLevel(mob) / 2;
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		count=super.getXLEVELLevel(mob) / 2;
		return true;
	}
}
