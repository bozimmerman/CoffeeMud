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

public class Spell_ChangeSex extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_ChangeSex";
	}

	private final static String localizedName = CMLib.lang().L("Change Sex");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Change Sex)");

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
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		char gender='M';
		if(affectableStats.getStat(CharStats.STAT_GENDER)!='F')
			gender='F';
		affectableStats.setStat(CharStats.STAT_GENDER,gender);
	}

	public void setChildStuff(MOB M, Environmental target)
	{
		if(CMLib.flags().isChild(M))
		{
			if(M.charStats().getStat(CharStats.STAT_GENDER)=='F')
			{
				M.setDescription(CMStrings.replaceWord(M.description(), "son", "daughter"));
				M.setName(CMStrings.replaceWord(M.Name(), "son", "daughter"));
				M.setDisplayText(CMStrings.replaceWord(M.displayText(), "son", "daughter"));
				M.setDescription(CMStrings.replaceWord(M.description(), "male", "female"));
				M.setName(CMStrings.replaceWord(M.Name(), "male", "female"));
				M.setDisplayText(CMStrings.replaceWord(M.displayText(), "male", "female"));
				M.setDescription(CMStrings.replaceWord(M.description(), "boy", "girl"));
				M.setName(CMStrings.replaceWord(M.Name(), "boy", "girl"));
				M.setDisplayText(CMStrings.replaceWord(M.displayText(), "boy", "girl"));
				if(target!=null)
				{
					target.setDescription(CMStrings.replaceWord(target.description(), "son", "daughter"));
					target.setName(CMStrings.replaceWord(target.Name(), "son", "daughter"));
					target.setDisplayText(CMStrings.replaceWord(target.displayText(), "son", "daughter"));
					target.setDescription(CMStrings.replaceWord(target.description(), "male", "female"));
					target.setName(CMStrings.replaceWord(target.Name(), "male", "female"));
					target.setDisplayText(CMStrings.replaceWord(target.displayText(), "male", "female"));
					target.setDescription(CMStrings.replaceWord(target.description(), "boy", "girl"));
					target.setName(CMStrings.replaceWord(target.Name(), "boy", "girl"));
					target.setDisplayText(CMStrings.replaceWord(target.displayText(), "boy", "girl"));
				}
			}
			else
			{
				M.setDescription(CMStrings.replaceWord(M.description(), "daughter", "son"));
				M.setName(CMStrings.replaceWord(M.Name(), "daughter", "son"));
				M.setDisplayText(CMStrings.replaceWord(M.displayText(), "daughter", "son"));
				M.setDescription(CMStrings.replaceWord(M.description(), "female", "male"));
				M.setName(CMStrings.replaceWord(M.Name(), "female", "male"));
				M.setDisplayText(CMStrings.replaceWord(M.displayText(), "female", "male"));
				M.setDescription(CMStrings.replaceWord(M.description(), "girl", "boy"));
				M.setName(CMStrings.replaceWord(M.Name(), "girl", "boy"));
				M.setDisplayText(CMStrings.replaceWord(M.displayText(), "girl", "boy"));
				if(target!=null)
				{
					target.setDescription(CMStrings.replaceWord(target.description(), "daughter", "son"));
					target.setName(CMStrings.replaceWord(target.Name(), "daughter", "son"));
					target.setDisplayText(CMStrings.replaceWord(target.displayText(), "daughter", "son"));
					target.setDescription(CMStrings.replaceWord(target.description(), "female", "male"));
					target.setName(CMStrings.replaceWord(target.Name(), "female", "male"));
					target.setDisplayText(CMStrings.replaceWord(target.displayText(), "female", "male"));
					target.setDescription(CMStrings.replaceWord(target.description(), "girl", "boy"));
					target.setName(CMStrings.replaceWord(target.Name(), "girl", "boy"));
					target.setDisplayText(CMStrings.replaceWord(target.displayText(), "girl", "boy"));
				}
			}
		}
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(affected instanceof CagedAnimal)
		{
			final CagedAnimal target=(CagedAnimal)affected;
			final MOB mob=target.unCageMe();
			super.unInvoke();
			if(canBeUninvoked())
			{
				final Ability A=mob.fetchEffect(ID());
				if(A!=null)
					mob.delEffect(A);
				mob.recoverCharStats();
				mob.recoverPhyStats();
				setChildStuff(mob, target);
				final Room R=CMLib.map().roomLocation(target);
				if(R!=null)
					mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> feel(s) like <S-HIS-HER> old self again."));

			}
		}
		else
		if(affected instanceof MOB)
		{
			final MOB mob=(MOB)affected;
			super.unInvoke();
			if(canBeUninvoked())
				if((mob.location()!=null)&&(!mob.amDead()))
					mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> feel(s) like <S-HIS-HER> old self again."));
		}
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Physical target=getAnyTarget(mob,commands,givenTarget,Wearable.FILTER_UNWORNONLY);
		if(target==null)
			return false;
		if(target instanceof Item)
		{
			if(!(target instanceof CagedAnimal))
			{
				mob.tell(L("This spell won't have much effect on @x1.",target.name(mob)));
				return false;
			}
		}
		else
		if(!(target instanceof MOB))
		{
			mob.tell(L("This spell won't have much effect on @x1.",target.name(mob)));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			invoker=mob;
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> sing(s) a spell to <T-NAMESELF>.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					MOB M=null;
					if(target instanceof MOB)
					{
						success=beneficialAffect(mob,target,asLevel,0)!=null;
						M=(MOB)target;
						M.recoverCharStats();
						M.recoverPhyStats();
					}
					else
					if(target instanceof CagedAnimal)
					{
						M=((CagedAnimal)target).unCageMe();
						char gender='M';
						if(M.baseCharStats().getStat(CharStats.STAT_GENDER)!='F')
							gender='F';
						M.baseCharStats().setStat(CharStats.STAT_GENDER,gender);
						M.recoverCharStats();
						M.recoverPhyStats();
						setChildStuff(M, target);
						M.text();
						((CagedAnimal)target).cageMe(M);
						target.text();
					}
					else
						return false;
					M.recoverCharStats();
					target.recoverPhyStats();
					mob.location().show(M,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> become(s) @x1!",M.charStats().genderName()));
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,target,L("<S-NAME> sing(s) a spell to <T-NAMESELF>, but the spell fizzles."));

		// return whether it worked
		return success;
	}
}
