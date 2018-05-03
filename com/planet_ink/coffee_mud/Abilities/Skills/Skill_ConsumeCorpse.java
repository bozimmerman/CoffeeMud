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

import java.util.*;

/*
   Copyright 2016-2018 Bo Zimmerman

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

public class Skill_ConsumeCorpse extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_ConsumeCorpse";
	}

	private final static String	localizedName	= CMLib.lang().L("Consume Corpse");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_CORRUPTION;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_UNHOLY;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_ITEMS;
	}

	private static final String[]	triggerStrings	= I(new String[] { "CONSUMECORPSE" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int usageType()
	{
		return USAGE_MANA;
	}

	public static Item getBody(Room R)
	{
		if(R!=null)
		for(int i=0;i<R.numItems();i++)
		{
			final Item I=R.getItem(i);
			if((I instanceof DeadBody)
			&&(!((DeadBody)I).isPlayerCorpse())
			&&(((DeadBody)I).getMobName().length()>0))
				return I;
		}
		return null;
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(target instanceof MOB)
			{
				if(getBody(((MOB)target).location())!=null)
					return super.castingQuality(mob, target, Ability.QUALITY_BENEFICIAL_SELF);
			}
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		Item target=null;
		if((commands.size()==0)&&(!auto)&&(givenTarget==null))
			target=getBody(mob.location());
		if((mob.isMonster())&&(givenTarget instanceof MOB))
			target=getBody(mob.location());
		if(target==null)
			target=getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_UNWORNONLY);
		if(target==null)
			return false;

		if((!(target instanceof DeadBody))
		   ||(target.rawSecretIdentity().toUpperCase().indexOf("FAKE")>=0))
		{
			mob.tell(L("You may only consume the dead."));
			return false;
		}

		if((((DeadBody)target).isPlayerCorpse())
		&&(!((DeadBody)target).getMobName().equals(mob.Name()))
		&&(((DeadBody)target).hasContent()))
		{
			mob.tell(L("You are not allowed to consume that corpse."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?L("<T-NAME> consume(s) <T-HIM-HERSELF>."):L("^S<S-NAME> consume(s) <T-NAMESELF>.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				double amtBack = CMath.div(target.phyStats().level(), adjustedLevel(mob,asLevel));
				int hp=(int)Math.round(CMath.mul(mob.maxState().getHitPoints(),amtBack));
				int mana=(int)Math.round(CMath.mul(mob.maxState().getMana(),amtBack));
				int move=(int)Math.round(CMath.mul(mob.maxState().getMovement(),amtBack));
				CMLib.combat().postHealing(mob, mob, this, hp, CMMsg.MASK_ALWAYS|CMMsg.TYP_HANDS, L("<S-NAME> feel(s) better!"));
				mob.curState().adjMana(mana, mob.maxState());
				mob.curState().adjMovement(move, mob.maxState());
				target.destroy();
				mob.location().recoverRoomStats();
			}
		}
		else
			beneficialWordsFizzle(mob,target,auto?"":L("<S-NAME> attempt(s) to consume <T-NAMESELF>, but fail(s)."));

		// return whether it worked
		return success;
	}
}
