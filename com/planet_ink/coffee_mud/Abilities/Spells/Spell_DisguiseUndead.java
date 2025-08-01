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
   Copyright 2014-2025 Bo Zimmerman

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
	public void affectCharStats(final MOB affected, final CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(affected==null)
			return;
		affectableStats.setRaceName("Human");
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
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
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
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
	public int castingQuality(final MOB mob, final Physical target)
	{
		if(mob!=null)
		{
			if(mob.isInCombat())
				return Ability.QUALITY_INDIFFERENT;
			if(mob.isMonster())
				return Ability.QUALITY_INDIFFERENT;
			if(target instanceof MOB)
			{
				if((!CMLib.flags().isUndead((MOB)target))
				||(((MOB)target).charStats().raceName().equalsIgnoreCase("Human")))
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

		if((!CMLib.flags().isUndead(target))
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
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),
					L(auto?"<T-NAME> gain(s) a disguise!":"^S<S-NAME> casts a spell for <T-NAMESELF>, causing <T-HIS-HER> appearance to change.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final Ability A=beneficialAffect(mob,target,asLevel,0);
				if(A!=null)
				{
					String noun;
					switch(CMLib.dice().roll(1, 5, -1))
					{
					case 0:
						switch(target.charStats().reproductiveCode())
						{
						case 'M': noun = L("male"); break;
						case 'F': noun = L("female"); break;
						default:  noun = L("person"); break;
						}
						break;
					case 1:
						switch(target.charStats().reproductiveCode())
						{
						case 'M': noun = L("man"); break;
						case 'F': noun = L("woman"); break;
						default:  noun = L("person"); break;
						}
						break;
					case 2:
						switch(target.charStats().reproductiveCode())
						{
						case 'M': noun = L("guy"); break;
						case 'F': noun = L("gal"); break;
						default:  noun = L("joe"); break;
						}
						break;
					case 3:
						switch(target.charStats().reproductiveCode())
						{
						case 'M': noun = "joe"; break;
						case 'F': noun = "jane"; break;
						default:  noun = "joe"; break;
						}
						break;
					default:
						noun = "person";
						break;
					}
					switch(CMLib.dice().roll(1, 3, -1))
					{
					case 0:
						A.setMiscText(L("a normal @x1",noun));
						break;
					case 1:
						A.setMiscText(L("a regular @x1",noun));
						break;
					default:
						A.setMiscText(L("an average @x1",noun));
						break;
					}
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
