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
   Copyright 2014-2018 Bo Zimmerman

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

public class Spell_DisguiseUndead extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_DisguiseUndead";
	}

	private final static String localizedName = CMLib.lang().L("Disguise Undead");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Disguise Undead)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_ILLUSION;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_OTHERS;
	}

	@Override
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(affected==null)
			return;
		affectableStats.setRaceName("Human");
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(affected==null)
			return;
		if(text().length()>0)
			affectableStats.setName(text());
		else
			affectableStats.setName(L("a normal person"));
	}

	@Override
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if((msg.source()==affected)&&(msg.sourceMinor()==CMMsg.TYP_AROMA))
			return false;
		return true;
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
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("<S-YOUPOSS> disguise fades."));
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(mob.isInCombat())
				return Ability.QUALITY_INDIFFERENT;
			if(mob.isMonster())
				return Ability.QUALITY_INDIFFERENT;
			if(target instanceof MOB)
			{
				if((!((MOB)target).charStats().getMyRace().racialCategory().equalsIgnoreCase("Undead"))
				||(((MOB)target).charStats().raceName().equalsIgnoreCase("Human")))
					return Ability.QUALITY_INDIFFERENT;
			}
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if((!target.charStats().getMyRace().racialCategory().equalsIgnoreCase("Undead"))
		||(target.charStats().raceName().equalsIgnoreCase("Human")))
		{
			mob.tell(L("This spell only works on the undead."));
			return false;
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),L(auto?"<T-NAME> gain(s) a disguise!":"^S<S-NAME> casts a spell for <T-NAMESELF>, causing <T-HIS-HER> appearance to change.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Ability A=beneficialAffect(mob,target,asLevel,0);
				if(A!=null)
				{
					String genderName;
					String genderFormal;
					String genderInformal;
					String genderPersonal;
					switch(target.charStats().getStat(CharStats.STAT_GENDER))
					{
					case 'M': genderName="male"; genderFormal="man"; genderInformal="guy"; genderPersonal="joe"; break;
					case 'F': genderName="female"; genderFormal="woman"; genderInformal="gal"; genderPersonal="jane"; break;
					default: genderName="person"; genderFormal="person"; genderInformal="person"; genderPersonal="joe"; break;
					}
					String adjective;
					switch(CMLib.dice().roll(1, 3, -1))
					{
					case 0: adjective="normal"; break;
					case 1: adjective="regular"; break;
					default: adjective="average"; break;
					}
					String noun;
					switch(CMLib.dice().roll(1, 5, -1))
					{
					case 0: noun=genderName; break;
					case 1: noun=genderFormal; break;
					case 2: noun=genderInformal; break;
					case 3: noun=genderPersonal; break;
					default: noun="person"; break;
					}
					A.setMiscText(L(CMLib.english().startWithAorAn(adjective+" "+noun).toLowerCase()));
				}
				target.recoverPhyStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,target,L("<S-NAME> cast(s) a spell for <T-NAMESELF>, but nothing happens."));

		// return whether it worked
		return success;
	}
}
