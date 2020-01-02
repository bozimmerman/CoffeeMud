package com.planet_ink.coffee_mud.Abilities.Skills;
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

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.*;

/*
   Copyright 2018-2020 Bo Zimmerman

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
public class Skill_Nippletwist extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_Nippletwist";
	}

	private final static String localizedName = CMLib.lang().L("Nipple Twist");

	@Override
	public String name()
	{
		return localizedName;
	}


	private static final String[] triggerStrings =I(new String[] {"NIPPLETWIST"});
	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_DIRTYFIGHTING;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT;
	}

	@Override
	public long flags()
	{
		return super.flags() | Ability.FLAG_TORTURING;
	}

	@Override
	public int castingQuality(final MOB mob, final Physical target)
	{
		if((mob!=null)&&(target!=null))
		{
			if(mob.isInCombat()&&(mob.rangeToTarget()>0))
				return Ability.QUALITY_INDIFFERENT;
			if(target instanceof MOB)
			{
				final MOB tMOB=(MOB)target;
				if(tMOB.fetchWornItems(Wearable.WORN_TORSO, (short)-2048, (short)0).size()>0)
					return Ability.QUALITY_INDIFFERENT;
			}
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(mob.isInCombat()&&(mob.rangeToTarget()>0))
		{
			mob.tell(L("You are too far away from your target to kick them!"));
			return false;
		}

		if(target.fetchWornItems(Wearable.WORN_TORSO, (short)-2048, (short)0).size()>0)
		{
			mob.tell(L("@x1 is wearing something on the torso that prevents you.",target.name(mob)));
			return false;
		}

		final List<Item> wornsNWields=mob.fetchWornItems(Wearable.WORN_WIELD, (short)-2048, (short)0);
		for(final Item I : mob.fetchWornItems(Wearable.WORN_HELD, (short)-2048, (short)0))
		{
			if(!wornsNWields.contains(I))
				wornsNWields.add(I);
		}

		if(wornsNWields.size() >= (mob.charStats().getBodyPart(Race.BODY_HAND)))
		{
			mob.tell(L("You need at least one free hand to do that."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int levelDiff=target.phyStats().level()-(mob.phyStats().level()+(2*getXLEVELLevel(mob)));
		if(levelDiff>0)
			levelDiff=levelDiff*10;
		else
			levelDiff=0;

		// now see if it worked
		final boolean hit=(auto)||CMLib.combat().rollToHit(mob,target);
		final boolean success=proficiencyCheck(mob,(-levelDiff)+(-((target.charStats().getStat(CharStats.STAT_DEXTERITY)-mob.charStats().getStat(CharStats.STAT_STRENGTH)))),auto)&&(hit);

		if(success)
		{
			invoker=mob;
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_JUSTICE|(auto?CMMsg.MASK_ALWAYS:0),
					L(auto?"<T-NAME> <T-IS-ARE> in obvious pain!":
						"^F<S-NAME> grab(s) <T-YOUPOSS> nipples and give(s) them a sharp TWIST!^?"));
			CMLib.color().fixSourceFightColor(msg);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				int topDamage=(adjustedLevel(mob,asLevel)/10)+2;
				if(target.findTattooStartsWith("left nipple:")!=null)
					topDamage *= 2;
				if(target.findTattooStartsWith("right nipple:")!=null)
					topDamage *= 2;
				if(target.findTattooStartsWith("nipples:")!=null)
					topDamage *= 5;
				int damage=CMLib.dice().roll(1,topDamage,0);
				if(msg.value()>0)
					damage = (int)Math.round(CMath.div(damage,2.0));
				if(damage >= target.curState().getHitPoints())
					damage=target.curState().getHitPoints()-1;
				CMLib.combat().postDamage(mob,target,this,damage,CMMsg.MASK_ALWAYS|CMMsg.MASK_SOUND|CMMsg.MASK_MOVE|CMMsg.TYP_JUSTICE,Weapon.TYPE_BASHING,
						L("^F^<FIGHT^>The nipple twist <DAMAGES> <T-NAME>!^</FIGHT^>^?@x1",CMLib.protocol().msp("bashed1.wav",30)));
			}
		}
		else
			return maliciousFizzle(mob,target,L("<S-NAME> attempt(s) to twist <T-YOUPOSS> nipples, but fail(s) to get a good grip."));

		// return whether it worked
		return success;
	}
}
