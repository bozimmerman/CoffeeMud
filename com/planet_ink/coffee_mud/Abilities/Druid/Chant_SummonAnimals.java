package com.planet_ink.coffee_mud.Abilities.Druid;
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
   Copyright 2022-2022 Bo Zimmerman

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
public class Chant_SummonAnimals extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_SummonAnimals";
	}

	private final static String	localizedName	= CMLib.lang().L("Summon Animals");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT | Ability.DOMAIN_ANIMALAFFINITY;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_BENEFICIAL_SELF;
	}

	@Override
	public int enchantQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_SUMMONING;
	}

	@Override
	public int castingQuality(final MOB mob, final Physical target)
	{
		if(mob!=null)
		{
			final Room R=mob.location();
			if(R!=null)
			{
				if((R.domainType()&Room.INDOORS)>0)
					return Ability.QUALITY_INDIFFERENT;
				final List<Integer> choices=Chant_SummonAnimal.outdoorChoices(mob.location());
				if(choices.size()==0)
					return Ability.QUALITY_INDIFFERENT;
			}
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if((mob.location().domainType()&Room.INDOORS)>0)
		{
			mob.tell(L("You must be outdoors for this chant to work."));
			return false;
		}
		final List<Integer> choices=Chant_SummonAnimal.outdoorChoices(mob.location());
		int fromDir=-1;
		if(choices.size()==0)
		{
			mob.tell(L("You must be further outdoors to summon animals."));
			return false;
		}
		fromDir=choices.get(CMLib.dice().roll(1,choices.size(),-1)).intValue();
		final Room newRoom=mob.location().getRoomInDir(fromDir);
		final int opDir=mob.location().getReverseDir(fromDir);

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			invoker=mob;
			int num=CMLib.dice().roll(2, 2 + (super.getXLEVELLevel(mob)/3),0);
			String text=text();
			final int x=text.indexOf('/');
			if(x>0)
			{
				final String math = text.substring(0,x).trim();
				if(CMath.isMathExpression(math))
					num = CMath.parseIntExpression(math);
				text=text.substring(x+1).trim();
			}

			final CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),auto?"":L("^S<S-NAME> chant(s) and summon(s) forth companions from the Java Plane.^?"));
			final Room room=mob.location();
			if(room.okMessage(mob,msg))
			{
				room.send(mob,msg);
				final MOB templateMob = Chant_SummonAnimal.determineMonster(mob, adjustedLevel(mob,asLevel), text);
				for(int i=0;i<num;i++)
				{
					final MOB target = (MOB)templateMob.copyOf();
					target.bringToLife(newRoom,true);
					CMLib.beanCounter().clearZeroMoney(target,null);
					target.setMoneyVariation(0);
					newRoom.showOthers(target,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> appears!"));
					newRoom.recoverRoomStats();
					target.setStartRoom(null);
					if(target.isInCombat())
						target.makePeace(true);
					CMLib.tracking().walk(target,opDir,false,false);
					if(target.location()==room)
					{
						if(target.isInCombat())
							target.makePeace(true);
						CMLib.commands().postFollow(target,mob,true);
						Chant_SummonAnimal animalSummonerA = (Chant_SummonAnimal)mob.fetchAbility("Chant_SummonAnimal");
						if(animalSummonerA == null)
							animalSummonerA = (Chant_SummonAnimal)CMClass.getAbility("Chant_SummonAnimal");
						if(animalSummonerA != null)
						{
							animalSummonerA.setInvoker(mob);
							animalSummonerA.beneficialAffect(mob,target,asLevel,0);
						}
						if(target.amFollowing()!=mob)
							mob.tell(L("@x1 seems unwilling to follow you.",target.name(mob)));
					}
					else
					{
						if(target.amDead())
							target.setLocation(null);
						target.destroy();
					}
					invoker=mob;
				}
			}
			else
				return beneficialWordsFizzle(mob,null,L("<S-NAME> chant(s) and summon(s), but nothing happens."));
		}
		else
			return beneficialWordsFizzle(mob,null,L("<S-NAME> chant(s) and summon(s), but nothing happens."));

		// return whether it worked
		return success;
	}

}
