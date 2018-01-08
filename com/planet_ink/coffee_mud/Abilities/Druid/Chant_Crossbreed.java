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

import java.util.List;

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
public class Chant_Crossbreed extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_Crossbreed";
	}

	private final static String	localizedName	= CMLib.lang().L("Crossbreed");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Crossbreed)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT | Ability.DOMAIN_BREEDING;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_OTHERS;
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
			mob.tell(L("Your strange cross-fertility subsides."));
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		// the sex rules
		if(!(affected instanceof MOB))
			return;

		final MOB myChar=(MOB)affected;
		if(msg.target() instanceof MOB)
		{
			final MOB mate=(MOB)msg.target();
			if((msg.amISource(myChar))
			&&(msg.tool() instanceof Social)
			&&(msg.tool().Name().equals("MATE <T-NAME>")
				||msg.tool().Name().equals("SEX <T-NAME>"))
			&&(msg.sourceMinor()!=CMMsg.TYP_CHANNEL)
			&&(myChar.charStats().getStat(CharStats.STAT_GENDER)!=mate.charStats().getStat(CharStats.STAT_GENDER))
			&&((mate.charStats().getStat(CharStats.STAT_GENDER)==('M'))
				||(mate.charStats().getStat(CharStats.STAT_GENDER)==('F')))
			&&((myChar.charStats().getStat(CharStats.STAT_GENDER)==('M'))
				||(myChar.charStats().getStat(CharStats.STAT_GENDER)==('F')))
			&&(!mate.charStats().getMyRace().canBreedWith(myChar.charStats().getMyRace()))
			&&(myChar.location()==mate.location())
			&&(myChar.fetchWornItems(Wearable.WORN_LEGS|Wearable.WORN_WAIST,(short)-2048,(short)0).size()==0)
			&&(mate.fetchWornItems(Wearable.WORN_LEGS|Wearable.WORN_WAIST,(short)-2048,(short)0).size()==0)
			&&((mate.charStats().getStat(CharStats.STAT_AGE)==0)
				||((mate.charStats().ageCategory()>Race.AGE_CHILD)
					&&(mate.charStats().ageCategory()<Race.AGE_OLD)))
			&&((myChar.charStats().getStat(CharStats.STAT_AGE)==0)
				||((myChar.charStats().ageCategory()>Race.AGE_CHILD)
					&&(myChar.charStats().ageCategory()<Race.AGE_OLD))))
			{
				MOB female=myChar;
				MOB male=mate;
				if((mate.charStats().getStat(CharStats.STAT_GENDER)==('F')))
				{
					female=mate;
					male=myChar;
				}
				final Ability A=CMClass.getAbility("Pregnancy");
				if((A!=null)
				&&(female.fetchAbility(A.ID())==null)
				&&(female.fetchEffect(A.ID())==null))
				{
					A.invoke(male,female,true,0);
					unInvoke();
				}
			}
		}
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> chant(s) to <T-NAMESELF>.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> seem(s) strangely fertile!"));
				beneficialAffect(mob,target,asLevel,Ability.TICKS_ALMOST_FOREVER);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,L("<S-NAME> chant(s) to <T-NAMESELF>, but the magic fades."));

		// return whether it worked
		return success;
	}
}
