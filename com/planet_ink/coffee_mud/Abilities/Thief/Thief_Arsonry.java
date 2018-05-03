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
   Copyright 2004-2018 Bo Zimmerman

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

public class Thief_Arsonry extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_Arsonry";
	}

	private final static String localizedName = CMLib.lang().L("Arsonry");

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
		return Ability.CAN_ITEMS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	private static final String[] triggerStrings =I(new String[] {"ARSON","ARSONRY"});
	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_CRIMINAL;
	}

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(commands.size()<1)
		{
			mob.tell(L("What or which direction is that which would you like to set on fire?"));
			return false;
		}
		final String str=CMParms.combine(commands,0);
		final int dir=CMLib.directions().getGoodDirectionCode(str);
		Room targetRoom=null;
		Physical target=null;
		if(dir>=0)
		{
			final Room room=mob.location().getRoomInDir(dir);
			if((room==null)||(mob.location().getExitInDir(dir)==null))
			{
				mob.tell(L("But there's nothing that way!"));
				return false;
			}
			if(!mob.location().getExitInDir(dir).isOpen())
			{
				mob.tell(L("That way isn't open!"));
				return false;
			}
			final Vector<Item> choices=new Vector<Item>();
			for(int i=0;i<room.numItems();i++)
			{
				final Item I=room.getItem(i);
				if((I!=null)
				&&(I.container()==null)
				&&(I.displayText().length()==0)
				&&(CMLib.flags().isGettable(I))
				&&(!(I instanceof ClanItem))
				&&(CMLib.materials().getBurnDuration(I)>0))
					choices.addElement(I);
			}
			if(choices.size()==0)
			{
				mob.tell(L("There's nothing that way you can burn!"));
				return false;
			}
			target=choices.elementAt(CMLib.dice().roll(1,choices.size(),-1));
			targetRoom=room;
		}
		else
		{
			final Item item=getTarget(mob,mob.location(),givenTarget,null,commands,Wearable.FILTER_UNWORNONLY);
			if(item==null)
				return false;
			target=item;
			targetRoom=mob.location();
		}
		boolean proceed=false;
		for(int i=0;i<mob.numItems();i++)
		{
			final Item I=mob.getItem(i);
			if((I!=null)&&(CMLib.flags().isOnFire(I))&&(CMLib.flags().canBeSeenBy(I,mob)))
			{
				proceed=true;
				break;
			}
		}
		if(!proceed)
		{
			mob.tell(L("You need to have something in your inventory on fire, like a torch, to use this skill."));
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int levelDiff=target.phyStats().level()-(mob.phyStats().level()+abilityCode()+(2*getXLEVELLevel(mob)));
		if(levelDiff<0)
			levelDiff=0;
		levelDiff*=5;
		final boolean success=proficiencyCheck(mob,-levelDiff,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_DELICATE_SMALL_HANDS_ACT,L("<S-NAME> commit(s) arsonry against <T-NAME>."));
			if((mob.location().okMessage(mob,msg))
			&&((targetRoom==mob.location())||(targetRoom.okMessage(mob,msg))))
			{
				mob.location().send(mob,msg);
				if(targetRoom!=mob.location())
					targetRoom.sendOthers(mob,msg);
				final Ability B=CMClass.getAbility("Burning");
				if(B!=null)
					B.invoke(mob,target,true,CMLib.materials().getBurnDuration(target));
			}
		}
		else
			beneficialVisualFizzle(mob,target,L("<S-NAME> attempt(s) arsonry against <T-NAME>, but fails."));
		return success;
	}

}
