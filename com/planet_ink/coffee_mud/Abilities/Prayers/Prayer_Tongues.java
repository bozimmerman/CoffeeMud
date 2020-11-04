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
   Copyright 2020-2020 Bo Zimmerman

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
public class Prayer_Tongues extends Prayer
{
	@Override
	public String ID()
	{
		return "Prayer_Tongues";
	}

	private final static String localizedName = CMLib.lang().L("Tongues");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Tongues)");

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
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER|Ability.DOMAIN_CURSING;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_UNHOLY;
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

	protected Map<String,Language> langs = null;

	protected static List<Language> allLangs = null;


	protected Language getLang(final Physical affected)
	{
		if(langs == null)
			langs=new Hashtable<String,Language>();

		if(allLangs == null)
		{
			allLangs=new ArrayList<Language>();
			for(final Enumeration<Ability> a=CMClass.abilities();a.hasMoreElements();)
			{
				final Ability A=a.nextElement();
				if((A instanceof Language)
				&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_LANGUAGE)
				&&(!A.ID().startsWith("Std"))
				&&(!A.ID().startsWith("GenL")))
				{
					allLangs.add((Language)A);
				}
			}
		}
		final String pickedID=allLangs.get(CMLib.dice().roll(1, allLangs.size(), -1)).ID();
		Language lang = langs.get(pickedID);
		if(lang == null)
		{
			if(langs.size()>20)
				langs.clear();
			lang=(Language)CMClass.getAbility(pickedID);
			lang.setAffectedOne(affected);
			lang.setProficiency(100);
			lang.setBeingSpoken(lang.ID(), true);
			langs.put(pickedID, lang);
		}
		return lang;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if(msg.source()==affected)
		{
			final Physical affected = this.affected;
			if(affected==null)
				return true;
			return getLang(affected).okMessage(myHost, msg);
		}
		return true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost, msg);
		if(msg.source()==affected)
		{
			final Physical affected = this.affected;
			if(affected==null)
				return;
			getLang(affected).executeMsg(myHost, msg);
		}
	}

	@Override
	public void unInvoke()
	{
		final MOB mob=(MOB)affected;
		if(canBeUninvoked())
			mob.tell(L("The tongues curse is lifted."));
		super.unInvoke();
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto)|CMMsg.MASK_MALICIOUS,auto?L("<T-NAME> <T-IS-ARE> cursed!"):L("^S<S-NAME> curse(s) <T-NAMESELF>.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					langs=null;
					final int tickTime = super.getMaliciousTickdownTime(mob,target,0,asLevel) * 50;
					final Ability A=maliciousAffect(mob,target,asLevel,tickTime,-1);
					success = A!=null;
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
			return maliciousFizzle(mob,target,L("<S-NAME> attempt(s) to curse <T-NAMESELF>, but nothing happens."));

		// return whether it worked
		return success;
	}
}
