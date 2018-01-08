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
   Copyright 2006-2018 Bo Zimmerman

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
public class Spell_WellDressed extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_WellDressed";
	}

	private final static String	localizedName	= CMLib.lang().L("Well Dressed");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Well Dressed)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_BENEFICIAL_OTHERS;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int classificationCode()
	{
		//return Ability.ACODE_SPELL | Ability.DOMAIN_ILLUSION;
		return Ability.ACODE_SKILL;
	}

	protected int					dressCode		= 1;

	private static final String[]	triggerStrings	= I(new String[] { "CAST" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		affectableStats.setStat(CharStats.STAT_CHARISMA,affectableStats.getStat(CharStats.STAT_CHARISMA)+dressCode);
	}

	@Override
	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		if(CMath.isInteger(newText))
			dressCode=CMath.s_int(newText);
	}

	@Override
	public String text()
	{
		return "" + dressCode;
	}

	@Override
	public void unInvoke()
	{
		if(!(affected instanceof MOB))
			return;
		// MOB mob=(MOB)affected;

		super.unInvoke();
		/*
		if(canBeUninvoked())
		{
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("<S-YOUPOSS> clothe(s) aren't quite as appealing any more."));
		}
		*/
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(target instanceof MOB)
			{
				if((((MOB)target).isInCombat())
				&&(!((MOB)target).charStats().getCurrentClass().baseClass().equalsIgnoreCase("Bard")))
					return Ability.QUALITY_INDIFFERENT;
			}
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final int newDressCode=1;
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		// now see if it worked
		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> speak(s) exquisitely to <T-NAMESELF>.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(target.location()==mob.location())
				{
					//target.location().show(target,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> <S-IS-ARE> very well dressed."));
					beneficialAffect(mob,target,asLevel,0);
					final Ability A=target.fetchEffect(ID());
					if(A!=null)
						A.setMiscText(""+newDressCode);
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,target,L("<S-NAME> speak(s) exquisitely to <T-NAMESELF>, but nothing more happens."));

		// return whether it worked
		return success;
	}
}
