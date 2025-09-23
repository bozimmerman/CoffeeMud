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
   Copyright 2020-2025 Bo Zimmerman

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
public class Prayer_Fluency extends Prayer
{
	@Override
	public String ID()
	{
		return "Prayer_Fluency";
	}

	private final static String localizedName = CMLib.lang().L("Fluency");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Fluency)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER|Ability.DOMAIN_COMMUNING;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_HOLY;
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

	protected Map<String,Language> langs=new HashMap<String,Language>();
	protected Language lastLang = null;
	protected MOB lastSpeaker = null;

	protected Language getLang(final MOB target)
	{
		final Physical affected = this.affected;
		if(affected==null)
			return null;
		final MOB tM=target;
		Language L=CMLib.utensils().getLanguageSpoken(tM);
		if((L==null)
		&&(lastSpeaker==target)
		&&(lastLang != null))
			L=lastLang;
		if((L!=null)
		&&(L.isANaturalLanguage())
		&&(!L.ID().equalsIgnoreCase("Common")))
		{
			if(langs.containsKey(L.ID()))
				return langs.get(L.ID());
			if(langs.size()>10)
				langs.clear();
			final Language myL=(Language)L.copyOf();
			myL.setAffectedOne(affected);
			myL.setBeingSpoken(myL.ID(), true);
			myL.setProficiency(100);
			langs.put(L.ID(), myL);
			return myL;
		}
		return null;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if((msg.source()==affected)
		&&(msg.targetMinor()==CMMsg.TYP_SPEAK)
		&&(msg.target() instanceof MOB)
		&&(!CMLib.flags().isAnimalIntelligence((MOB)msg.target())))
		{
			final Language L=getLang((MOB)msg.target());
			if((L!=null)&&(!L.okMessage(myHost, msg)))
				return false;
		}
		return true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost, msg);
		if((msg.source()==affected)
		&&(msg.targetMinor()==CMMsg.TYP_SPEAK)
		&&(msg.target() instanceof MOB)
		&&(!CMLib.flags().isAnimalIntelligence((MOB)msg.target())))
		{
			final Language L=getLang((MOB)msg.target());
			if(L!=null)
				L.executeMsg(myHost, msg);
		}
		else
		if((msg.source()!=affected)
		&&(msg.targetMinor()==CMMsg.TYP_SPEAK)
		&&(msg.target() instanceof MOB)
		&&(msg.tool() instanceof Language)
		&&(((Language)msg.tool()).isANaturalLanguage())
		&&(!CMLib.flags().isAnimalIntelligence(msg.source())))
		{
			lastLang=(Language)msg.tool();
			lastSpeaker=msg.source();
		}
	}

	@Override
	public void unInvoke()
	{
		final MOB mob=(MOB)affected;
		if(canBeUninvoked() && (mob!=null))
			mob.tell(L("Your fluency fades."));
		super.unInvoke();
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;

		if(target.fetchEffect(this.ID())!=null)
		{
			failureTell(mob,target,auto,L("<S-NAME> <S-IS-ARE> already fluent."), commands);
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?L("<T-NAME> gain(s) fluency!"):L("^S<S-NAME> @x1, and gain(s) fluency!^?",prayWord(mob)));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					final Ability A=beneficialAffect(mob,target,asLevel,0);
					success=A!=null;
					if(success)
					{
						target.delEffect(A);
						target.addPriorityEffect(A); // to beat the other languages to the punch
					}
					target.recoverPhyStats();
				}
			}
		}
		else
			return maliciousFizzle(mob,target,L("<S-NAME> attempt(s) to pray fluently, but fail(s)."));

		// return whether it worked
		return success;
	}
}
