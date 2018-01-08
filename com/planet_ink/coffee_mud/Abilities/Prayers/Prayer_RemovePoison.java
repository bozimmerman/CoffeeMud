package com.planet_ink.coffee_mud.Abilities.Prayers;
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

public class Prayer_RemovePoison extends Prayer implements MendingSkill
{
	@Override
	public String ID()
	{
		return "Prayer_RemovePoison";
	}

	private final static String localizedName = CMLib.lang().L("Remove Poison");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER|Ability.DOMAIN_RESTORATION;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_OTHERS;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_HOLY;
	}

	@Override
	public boolean supportsMending(Physical item)
	{
		if(!(item instanceof MOB))
			return false;
		final boolean canMend=returnOffensiveAffects(item).size()>0;
		return canMend;
	}

	public static List<Ability> returnOffensiveAffects(Physical fromMe)
	{
		final Vector<Ability> offenders=new Vector<Ability>();

		for(int a=0;a<fromMe.numEffects();a++) // personal
		{
			final Ability A=fromMe.fetchEffect(a);
			if((A!=null)&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_POISON))
				offenders.addElement(A);
		}
		return offenders;
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(target instanceof MOB)
			{
				if(supportsMending(target))
					return super.castingQuality(mob, target,Ability.QUALITY_BENEFICIAL_OTHERS);
			}
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Physical target=getAnyTarget(mob,commands,givenTarget,Wearable.FILTER_UNWORNONLY);
		if(target==null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		final List<Ability> offensiveAffects=returnOffensiveAffects(target);

		if((success)&&(offensiveAffects.size()>0))
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?L("<T-NAME> feel(s) purified of <T-HIS-HER> poisons."):L("^S<S-NAME> @x1 that <T-NAME> be purified of <T-HIS-HER> poisons.^?",prayWord(mob)));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				for(int a=offensiveAffects.size()-1;a>=0;a--)
					offensiveAffects.get(a).unInvoke();
				if((target instanceof Drink)&&(((Drink)target).liquidType()==RawMaterial.RESOURCE_POISON))
				{
					((Drink)target).setLiquidType(RawMaterial.RESOURCE_FRESHWATER);
					target.basePhyStats().setAbility(0);
				}
				if(!CMLib.flags().isStillAffectedBy(target,offensiveAffects,false))
				{
					if(target instanceof MOB)
					{
						((MOB)target).tell(L("You feel much better!"));
						((MOB)target).recoverCharStats();
						((MOB)target).recoverMaxState();
					}
				}
				target.recoverPhyStats();
			}
		}
		else
		if(!auto)
			beneficialWordsFizzle(mob,target,auto?"":L("<S-NAME> @x1 that <T-NAME> be purified of <T-HIS-HER> poisons, but there is no answer.",prayWord(mob)));

		// return whether it worked
		return success;
	}
}
