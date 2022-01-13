package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
   Copyright 2020-2022 Bo Zimmerman

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
public class Fighter_AutoHammerRing extends FighterSkill
{
	@Override
	public String ID()
	{
		return "Fighter_AutoHammerRing";
	}

	private final static String localizedName = CMLib.lang().L("AutoHammerRing");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		return "";
	}

	private static final String[] triggerStrings =I(new String[] {"AUTOHAMMERRING"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_BENEFICIAL_SELF;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_DIRTYFIGHTING;
	}
	
	protected volatile int tickUp = 3;

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!(affected instanceof MOB))
			return super.tick(ticking,tickID);
		if(!super.tick(ticking,tickID))
			return false;

		final MOB mob=(MOB)affected;

		if(mob.isInCombat()
		&&(mob.rangeToTarget()==0)
		&&(CMLib.flags().isAliveAwakeMobileUnbound(mob,true))
		&&((++tickUp)>=3))
		{
			final Item I=mob.fetchWieldedItem();
			if((I instanceof Weapon)
			&&(((Weapon)I).weaponClassification()==Weapon.CLASS_HAMMER))
			{
				if(proficiencyCheck(null,0,false))
				{
					tickUp=0;
					final Ability A=mob.fetchAbility("Fighter_HammerRing");
					if(A!=null)
					{
						final int[][] abilityUsageCache = mob.getAbilityUsageCache(A.ID());
						final int[] cache = abilityUsageCache[Ability.CACHEINDEX_LASTTIME];
						final int oldCount = (cache == null)? 0 : cache[USAGEINDEX_COUNT];
						A.invoke(mob,mob.getVictim(),false,adjustedLevel(mob,0));
						if(cache == null)
							abilityUsageCache[Ability.CACHEINDEX_LASTTIME]=null;
						else
							cache[USAGEINDEX_COUNT]=oldCount;
					}
					if(CMLib.dice().rollPercentage()<5)
						helpProficiency(mob, 0);
				}
			}
		}
		return true;
	}

	@Override
	public int castingQuality(final MOB mob, final Physical target)
	{
		if((mob!=null)&&(target!=null))
		{
			if(!(target instanceof MOB))
				return Ability.QUALITY_INDIFFERENT;
			if(mob.isInCombat()&&(mob.rangeToTarget()>0))
				return Ability.QUALITY_INDIFFERENT;
			if(target.fetchEffect(ID())!=null)
				return Ability.QUALITY_INDIFFERENT;
			final Item I=mob.fetchWieldedItem();
			if((!(I instanceof Weapon))
			||(((Weapon)I).weaponClassification()!=Weapon.CLASS_HAMMER))
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if((mob.fetchEffect(ID())!=null))
		{
			mob.tell(L("You are no longer automatically HammerRinging opponents."));
			mob.delEffect(mob.fetchEffect(ID()));
			return false;
		}
		
		final Item I=mob.fetchWieldedItem();
		if((!(I instanceof Weapon))
		||(((Weapon)I).weaponClassification()!=Weapon.CLASS_HAMMER))
		{
			mob.tell(L("You need a hammer to do some hammerring!"));
			return false;
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			mob.tell(L("You will now automatically HammerRing opponents when you fight."));
			beneficialAffect(mob,mob,asLevel,0);
			final Ability A=mob.fetchEffect(ID());
			if(A!=null)
				A.makeLongLasting();
		}
		else
			beneficialVisualFizzle(mob,null,L("<S-NAME> attempt(s) to get into <S-HIS-HER> hammerringing mood, but fail(s)."));
		return success;
	}

	@Override
	public boolean autoInvocation(final MOB mob, final boolean force)
	{
		return super.autoInvocation(mob, force);
	}
}
