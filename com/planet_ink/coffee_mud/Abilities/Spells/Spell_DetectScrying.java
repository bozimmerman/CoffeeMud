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
   Copyright 2003-2024 Bo Zimmerman

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
public class Spell_DetectScrying extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_DetectScrying";
	}

	private final static String localizedName = CMLib.lang().L("Detect Scrying");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public int enchantQuality()
	{
		return Ability.QUALITY_BENEFICIAL_SELF;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	public long flags()
	{
		return super.flags() | Ability.FLAG_DIVINING;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_DIVINATION;
	}

	@Override
	public int castingQuality(final MOB mob, final Physical target)
	{
		if(mob!=null)
		{
			if(target instanceof MOB)
			{
				if(((MOB)target).isInCombat()||((MOB)target).isMonster())
					return Ability.QUALITY_INDIFFERENT;
			}
		}
		return super.castingQuality(mob,target);
	}

	public Ability foundEffect(final Physical P, final String abilityID)
	{
		if(P == null)
			return null;
		final Ability A=P.fetchEffect(abilityID);
		if(A!=null)
			return A;
		if(P instanceof MOB)
			return foundEffect(((MOB)P).location(), abilityID);
		if(P instanceof Item)
			return foundEffect(CMLib.map().roomLocation(P), abilityID);
		if(P instanceof Room)
			return foundEffect(((Room)P).getArea(), abilityID);
		if(P instanceof Boardable)
			return foundEffect(((Boardable)P).getBoardableItem(), abilityID);
		return null;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final MOB target=getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> incant(s) softly to <T-NAMESELF>!^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final StringBuffer str=new StringBuffer("");
				final Session tS=target.session();
				if(tS!=null)
				{
					for(final Session S1 : CMLib.sessions().localOnlineIterable())
						if(tS.isBeingSnoopedBy(S1))
							str.append(L("@x1 is snooping on <T-NAME>.  ",S1.mob().name()));
				}
				Ability A=foundEffect(target, "Spell_Scry");
				if((A!=null)&&(A.invoker()!=null))
					str.append(L("@x1 is scrying on <T-NAME>.",A.invoker().name()));
				A=foundEffect(target, "Spell_Claireaudience");
				if((A!=null)&&(A.invoker()!=null))
					str.append(L("@x1 is listening to <T-NAME>.",A.invoker().name()));
				A=foundEffect(target, "Spell_GreaterClaireaudience");
				if((A!=null)&&(A.invoker()!=null))
					str.append(L("@x1 is listening to everyone around <T-NAME>.",A.invoker().name()));
				A=foundEffect(target, "Spell_Clairevoyance");
				if((A!=null)&&(A.invoker()!=null))
					str.append(L("@x1 is watching <T-NAME>.",A.invoker().name()));
				A=foundEffect(target, "Spell_GreaterClairevoyance");
				if((A!=null)&&(A.invoker()!=null))
					str.append(L("@x1 is watching everyone around <T-NAME>.",A.invoker().name()));
				if(str.length()==0)
					str.append(L("There doesn't seem to be anyone scrying on <T-NAME>."));
				CMLib.commands().postSay(mob,target,str.toString(),false,false);
			}
		}
		else
			beneficialVisualFizzle(mob,target,L("<S-NAME> incant(s) to <T-NAMESELF>, but the spell fizzles."));

		return success;
	}
}
