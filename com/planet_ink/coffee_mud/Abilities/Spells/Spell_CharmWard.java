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

public class Spell_CharmWard extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_CharmWard";
	}

	private final static String localizedName = CMLib.lang().L("Charm Ward");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Charm Ward)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_OTHERS;
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
			mob.tell(L("Your charm ward dissipates."));

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
			&&(msg.tool() instanceof Ability)
			&&(CMath.bset(((Ability)msg.tool()).flags(),Ability.FLAG_CHARMING))
			&&(!mob.amDead()))
			{
				final Ability A=(Ability)msg.tool();
				if(((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_CHANT)
				||((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_SPELL)
				||((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PRAYER)
				||((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_SONG))
					msg.source().location().showHappens(CMMsg.MSG_OK_VISUAL,L("Magical energy fizzles and is absorbed into the air!"));
				return false;
			}
		}
		else
		if(affected instanceof Room)
		{
			final Room R=(Room)affected;
			if((msg.tool() instanceof Ability)
			&&(CMath.bset(((Ability)msg.tool()).flags(),Ability.FLAG_CHARMING)))
			{
				if((msg.source().location()!=null)&&(msg.source().location()!=R))
					msg.source().location().showHappens(CMMsg.MSG_OK_VISUAL,L("Magical energy fizzles and is absorbed into the air!"));
				R.showHappens(CMMsg.MSG_OK_VISUAL,L("Magical energy fizzles and is absorbed into the air!"));
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(target instanceof MOB)
			{
				final MOB victim=((MOB)target).getVictim();
				if((victim!=null)&&(CMLib.flags().flaggedAbilities(victim,Ability.FLAG_CHARMING).size()>0))
					return super.castingQuality(mob, target,Ability.QUALITY_BENEFICIAL_SELF);
			}
		}
		return super.castingQuality(mob,target);
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
		{
			if(mob.isMonster())
				target=mob.location();
			else
				return false;
		}
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell(L("This place is already charmed."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?L("<T-NAME> seem(s) magically protected."):L("^S<S-NAME> invoke(s) a charm ward upon <T-NAMESELF>.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if((target instanceof Room)
				&&(CMLib.law().doesOwnThisLand(mob,((Room)target))))
				{
					target.addNonUninvokableEffect((Ability)this.copyOf());
					CMLib.database().DBUpdateRoom((Room)target);
				}
				else
					beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			beneficialWordsFizzle(mob,target,L("<S-NAME> attempt(s) to invoke a charm ward, but fail(s)."));

		return success;
	}
}
