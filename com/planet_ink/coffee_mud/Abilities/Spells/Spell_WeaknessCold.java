package com.planet_ink.coffee_mud.Abilities.Spells;
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
   Copyright 2002-2025 Bo Zimmerman

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
public class Spell_WeaknessCold extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_WeaknessCold";
	}

	private final static String localizedName = CMLib.lang().L("Weakness to Cold");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Weakness to Cold)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_TRANSMUTATION;
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;
		if(canBeUninvoked())
			mob.tell(L("Your cold weakness is now gone."));

		super.unInvoke();

	}

	@Override
	public void affectCharStats(final MOB affectedMOB, final CharStats affectedStats)
	{
		super.affectCharStats(affectedMOB,affectedStats);
		affectedStats.setStat(CharStats.STAT_SAVE_COLD,affectedStats.getStat(CharStats.STAT_SAVE_COLD)-100);
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(tickID!=Tickable.TICKID_MOB)
			return false;
		if((affecting()!=null)&&(affecting() instanceof MOB))
		{
			final MOB M=(MOB)affecting();
			final Room room=M.location();
			final MOB invoker=(invoker()!=null) ? invoker() : M;
			if(room!=null)
			{
				if((room.getArea().getClimateObj().weatherType(room)==Climate.WEATHER_WINDY)
				&&((room.getClimateType()&Places.CLIMASK_COLD)>0)
				&&(CMLib.dice().rollPercentage()>M.charStats().getSave(CharStats.STAT_SAVE_COLD)))
					CMLib.combat().postDamage(invoker,M,null,1,CMMsg.MASK_MALICIOUS|CMMsg.MASK_ALWAYS|CMMsg.TYP_COLD,Weapon.TYPE_FROSTING,L("The cold biting wind <DAMAGE> <T-NAME>!"));
				else
				if((room.getArea().getClimateObj().weatherType(room)==Climate.WEATHER_WINTER_COLD)
				&&(CMLib.dice().rollPercentage()>M.charStats().getSave(CharStats.STAT_SAVE_COLD)))
					CMLib.combat().postDamage(invoker,M,null,1,CMMsg.MASK_MALICIOUS|CMMsg.MASK_ALWAYS|CMMsg.TYP_COLD,Weapon.TYPE_FROSTING,L("The biting cold <DAMAGE> <T-NAME>!"));
				else
				if((room.getArea().getClimateObj().weatherType(room)==Climate.WEATHER_SNOW)
				&&(CMLib.dice().rollPercentage()>M.charStats().getSave(CharStats.STAT_SAVE_COLD)))
				{
					final int damage=CMLib.dice().roll(1,8,0);
					CMLib.combat().postDamage(invoker,M,null,damage,CMMsg.MASK_MALICIOUS|CMMsg.MASK_ALWAYS|CMMsg.TYP_COLD,Weapon.TYPE_FROSTING,L("The blistering snow <DAMAGE> <T-NAME>!"));
				}
				else
				if((room.getArea().getClimateObj().weatherType(room)==Climate.WEATHER_BLIZZARD)
				&&(CMLib.dice().rollPercentage()>M.charStats().getSave(CharStats.STAT_SAVE_COLD)))
				{
					final int damage=CMLib.dice().roll(1,16,0);
					CMLib.combat().postDamage(invoker,M,null,damage,CMMsg.MASK_MALICIOUS|CMMsg.MASK_ALWAYS|CMMsg.TYP_COLD,Weapon.TYPE_FROSTING,L("The blizzard <DAMAGE> <T-NAME>!"));
				}
				else
				if((room.getArea().getClimateObj().weatherType(room)==Climate.WEATHER_HAIL)
				&&(CMLib.dice().rollPercentage()>M.charStats().getSave(CharStats.STAT_SAVE_COLD)))
				{
					final int damage=CMLib.dice().roll(1,8,0);
					CMLib.combat().postDamage(invoker,M,null,damage,CMMsg.MASK_MALICIOUS|CMMsg.MASK_ALWAYS|CMMsg.TYP_COLD,Weapon.TYPE_FROSTING,L("The biting hail <DAMAGE> <T-NAME>!"));
				}
				else
					return true;
				CMLib.combat().postRevengeAttack(M, invoker);
			}
		}
		return true;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if(!(affected instanceof MOB))
			return true;

		final MOB mob=(MOB)affected;
		if((msg.amITarget(mob))&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		   &&(msg.sourceMinor()==CMMsg.TYP_COLD))
		{
			final int recovery=(int)Math.round(CMath.mul((msg.value()),0.3+CMath.mul(super.getXLEVELLevel(mob), 0.05)));
			msg.setValue(msg.value()+recovery);
		}
		return true;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final MOB target=getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?L("A shimmering frost absorbing field appears around <T-NAMESELF>."):L("^S<S-NAME> invoke(s) a shimmering frost absorbing field around <T-NAMESELF>.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
					success=maliciousAffect(mob,target,asLevel,0,-1)!=null;
			}
		}
		else
			maliciousFizzle(mob,target,L("<S-NAME> attempt(s) to invoke weakness to cold, but fail(s)."));

		return success;
	}
}
