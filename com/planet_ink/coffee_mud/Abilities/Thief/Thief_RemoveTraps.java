package com.planet_ink.coffee_mud.Abilities.Thief;
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
   Copyright 2001-2018 Bo Zimmerman

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

public class Thief_RemoveTraps extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_RemoveTraps";
	}

	private final static String	localizedName	= CMLib.lang().L("Remove Traps");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_ITEMS | Ability.CAN_EXITS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	private static final String[]	triggerStrings	= I(new String[] { "DETRAP", "UNTRAP", "REMOVETRAPS" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL | Ability.DOMAIN_DETRAP;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT | USAGE_MANA;
	}

	public Environmental lastChecked=null;
	public LinkedList<String> lastDone=new LinkedList<String>();

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		return invoke(mob, commands, givenTarget, auto, asLevel, false, new Vector<Trap>(0));
	}
	
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel, boolean quiet, List<Trap> saveTrapsHere)
	{
		final String whatTounlock=CMParms.combine(commands,0);
		Physical unlockThis=null;
		int dirCode=CMLib.directions().getGoodDirectionCode(whatTounlock);
		final Room R=mob.location();
		Room nextRoom=null;
		if(dirCode>=0)
		{
			nextRoom=R.getRoomInDir(dirCode);
			unlockThis=R.getExitInDir(dirCode);
		}
		if((unlockThis==null)&&(whatTounlock.equalsIgnoreCase("room")||whatTounlock.equalsIgnoreCase("here")))
			unlockThis=R;
		if(unlockThis==null)
			unlockThis=getAnyTarget(mob,commands,givenTarget,Wearable.FILTER_UNWORNONLY);
		if(unlockThis==null)
			return false;
		final int oldProficiency=proficiency();

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,+(((mob.phyStats().level()+(getXLEVELLevel(mob)*2))
											 -unlockThis.phyStats().level())*3),auto);
		final Vector<Physical> permSetV=new Vector<Physical>();
		Trap theTrap=CMLib.utensils().fetchMyTrap(unlockThis);
		if(theTrap!=null)
			permSetV.addElement(unlockThis);
		Trap opTrap=null;
		boolean permanent=false;
		if((unlockThis instanceof Room)
		&&(CMLib.law().doesOwnThisLand(mob,((Room)unlockThis))))
			permanent=true;
		else
		if(unlockThis instanceof Exit)
		{
			Room R2=null;
			if(dirCode<0)
			for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
			{
				if (R.getExitInDir(d) == unlockThis)
				{
					dirCode = d;
					R2 = R.getRoomInDir(d);
					break;
				}
			}
			if((CMLib.law().doesOwnThisLand(mob,R))
			||((R2!=null)&&(CMLib.law().doesOwnThisLand(mob,R2))))
				permanent=true;
			if(dirCode>=0)
			{
				final Exit exit=R.getReverseExit(dirCode);
				if(exit!=null)
					opTrap=CMLib.utensils().fetchMyTrap(exit);
				if(opTrap!=null)
					permSetV.addElement(exit);
				Trap roomTrap=null;
				if(nextRoom!=null)
					roomTrap=CMLib.utensils().fetchMyTrap(nextRoom);
				if(roomTrap!=null)
					permSetV.addElement(nextRoom);
				if((theTrap!=null)&&(theTrap.disabled())&&(roomTrap!=null))
				{
					opTrap=null;
					unlockThis=nextRoom;
					theTrap=roomTrap;
				}
			}
		}
		if(unlockThis==null)
		{
			mob.tell(L("You can't seem to remember how this works."));
			return false;
		}
		final CMMsg msg=CMClass.getMsg(mob,unlockThis,this,auto?CMMsg.MSG_OK_ACTION:CMMsg.MSG_DELICATE_HANDS_ACT,CMMsg.MSG_DELICATE_HANDS_ACT,CMMsg.MSG_OK_ACTION,auto?L("@x1 begins to glow.",unlockThis.name()):L("<S-NAME> attempt(s) to safely deactivate a trap on @x1.",unlockThis.name()));
		if((success)&&(!lastDone.contains(""+unlockThis)))
		{
			while(lastDone.size()>40)
				lastDone.removeFirst();
			lastDone.addLast(""+unlockThis);
			msg.setValue(1); // this is to notify that the thief gets xp from doing this.
		}
		else
			msg.setValue(0);
		if(R.okMessage(mob,msg))
		{
			R.send(mob,msg);
			if((unlockThis==lastChecked)&&((theTrap==null)||(theTrap.disabled())))
				setProficiency(oldProficiency);
			if(success)
			{
				if(theTrap!=null)
				{
					theTrap.disable();
					if(saveTrapsHere != null)
						saveTrapsHere.add(theTrap);
				}
				if(opTrap!=null)
				{
					opTrap.disable();
					if(saveTrapsHere != null)
						saveTrapsHere.add(opTrap);
				}
				if(permanent)
				{
					for(int i=0;i<permSetV.size();i++)
					{
						if (theTrap != null)
						{
							theTrap.unInvoke();
							permSetV.elementAt(i).delEffect(theTrap);
						}
						if (opTrap != null)
						{
							opTrap.unInvoke();
							permSetV.elementAt(i).delEffect(opTrap);
						}
					}
					CMLib.database().DBUpdateRoom(R);
					CMLib.database().DBUpdateExits(R);
				}
			}
			if((!auto)&&(!quiet))
				mob.tell(L("You have completed your attempt."));
			lastChecked=unlockThis;
		}

		return success;
	}
}
