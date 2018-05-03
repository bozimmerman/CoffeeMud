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
   Copyright 2003-2018 Bo Zimmerman

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

public class Spell_SummoningWard extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_SummoningWard";
	}

	private final static String localizedName = CMLib.lang().L("Summoning Ward");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Summoning Ward)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	protected int quality=Ability.QUALITY_INDIFFERENT;

	@Override
	public int abstractQuality()
	{
		return quality;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS|CAN_ROOMS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_ABJURATION;
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
		{
			super.unInvoke();
			return;
		}
		final MOB mob=(MOB)affected;
		if(canBeUninvoked())
			mob.tell(L("Your summoning ward dissipates."));

		super.unInvoke();

	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(affected==null)
			return super.okMessage(myHost,msg);

		if(affected instanceof MOB)
		{
			final MOB mob=(MOB)affected;
			if((msg.amITarget(mob))
			&&(!msg.amISource(mob))
			&&(mob.location()!=msg.source().location())
			&&(msg.tool() instanceof Ability)
			&&(CMath.bset(((Ability)msg.tool()).flags(),Ability.FLAG_SUMMONING))
			&&(!mob.amDead()))
			{
				msg.source().location().showHappens(CMMsg.MSG_OK_VISUAL,L("Magical energy fizzles and is absorbed into the air!"));
				return false;
			}
		}
		else
		if(affected instanceof Room)
		{
			final Room R=(Room)affected;
			if((msg.tool() instanceof Ability)
			&&(CMath.bset(((Ability)msg.tool()).flags(),Ability.FLAG_SUMMONING)))
			{
				final Ability A=(Ability)msg.tool();
				if(((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_CHANT)
				||((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_SPELL)
				||((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PRAYER)
				||((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_SONG))
				{
					if((msg.source().location()!=null)&&(msg.source().location()!=R))
						msg.source().location().showHappens(CMMsg.MSG_OK_VISUAL,L("Magical energy fizzles and is absorbed into the air!"));
					R.showHappens(CMMsg.MSG_OK_VISUAL,L("Magical energy fizzles and is absorbed into the air!"));
				}
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		Physical target=null;
		if(commands.size()>0)
		{
			final String s=CMParms.combine(commands,0);
			if(s.equalsIgnoreCase("room"))
				target=mob.location();
			else
			if(s.equalsIgnoreCase("here"))
				target=mob.location();
			else
			if(CMLib.english().containsString(mob.location().ID(),s)
			||CMLib.english().containsString(mob.location().name(),s)
			||CMLib.english().containsString(mob.location().displayText(),s))
				target=mob.location();
		}
		if(target==null)
			target=getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;
		if((target instanceof Room)&&(target.fetchEffect(ID())!=null))
		{
			mob.tell(L("This place is already under a summoning ward."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?L("<T-NAME> seem(s) magically protected."):L("^S<S-NAME> invoke(s) a summoning ward upon <T-NAMESELF>.^?"));
			if(target instanceof Room)
				quality=Ability.QUALITY_MALICIOUS;
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if((target instanceof Room)
				&&((CMLib.law().doesOwnThisLand(mob,((Room)target)))))
				{
					target.addNonUninvokableEffect((Ability)this.copyOf());
					CMLib.database().DBUpdateRoom((Room)target);
				}
				else
				{
					beneficialAffect(mob,target,asLevel,0);
					if(target instanceof Room)
					{
						final Spell_SummoningWard A=(Spell_SummoningWard)target.fetchEffect(ID());
						if(A!=null)
							A.quality=Ability.QUALITY_MALICIOUS;
					}
				}
			}
		}
		else
			beneficialWordsFizzle(mob,target,L("<S-NAME> attempt(s) to invoke a summoning ward, but fail(s)."));
		quality=Ability.QUALITY_INDIFFERENT;

		return success;
	}
}
