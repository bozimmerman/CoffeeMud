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

public class Spell_Delude extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_Delude";
	}

	private final static String localizedName = CMLib.lang().L("Delude");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Delude spell)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	int previousAlignment=500;

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_TRANSMUTATION;
	}

	@Override
	public void unInvoke()
	{
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked())
		{
			if(mob.playerStats()!=null)
				mob.playerStats().setLastUpdated(0);
			CMLib.factions().postFactionChange(mob,this, CMLib.factions().AlignID(), previousAlignment-mob.fetchFaction(CMLib.factions().AlignID()));
			if(mob.fetchFaction(CMLib.factions().AlignID()) != previousAlignment)
				mob.addFaction(CMLib.factions().AlignID(), previousAlignment);
			mob.tell(L("Your attitude returns to normal."));
			CMLib.utensils().confirmWearability(mob);
		}
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target,null,null,L("<S-NAME> <S-IS-ARE> already deluding others."));
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if((success)&&(CMLib.factions().getFaction(CMLib.factions().AlignID())!=null))
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> incant(s) and meditate(s).^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					previousAlignment=target.fetchFaction(CMLib.factions().AlignID());

					target.location().show(target,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> undergo(es) a change of attitude"));
					success=beneficialAffect(mob,target,asLevel,0)!=null;
					if(success)
					{
						int which=0;
						if(CMLib.flags().isEvil(target))
							which=1;
						else
						if(CMLib.flags().isGood(target))
							which=2;
						else
						if(CMLib.dice().rollPercentage()>50)
							which=1;
						else
							which=2;
						Enumeration<Faction.FRange> e;
						switch(which)
						{
						case 1:
							// find a good range, set them within that
							int newAlign=0;
							e=CMLib.factions().getRanges(CMLib.factions().AlignID());
							if(e!=null)
							{
								for(;e.hasMoreElements();)
								{
									final Faction.FRange R=e.nextElement();
									if(R.alignEquiv()==Faction.Align.GOOD)
									{
										newAlign = R.random();
										break;
									}
								}
							}
							CMLib.factions().postFactionChange(target,this, CMLib.factions().AlignID(), newAlign-target.fetchFaction(CMLib.factions().AlignID()));
							CMLib.utensils().confirmWearability(target);
							return true;
						case 2:
							// find an evil range, set them within that
							newAlign=0;
							e=CMLib.factions().getRanges(CMLib.factions().AlignID());
							if(e!=null)
							{
								for(;e.hasMoreElements();)
								{
									final Faction.FRange R=e.nextElement();
									if(R.alignEquiv()==Faction.Align.EVIL)
									{
										newAlign = R.random();
										break;
									}
								}
							}
							CMLib.factions().postFactionChange(target,this, CMLib.factions().AlignID(), newAlign-target.fetchFaction(CMLib.factions().AlignID()));
							CMLib.utensils().confirmWearability(target);
							return true;
						}
					}
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,null,L("<S-NAME> incant(s) and meditate(s), but fizzle(s) the spell."));

		// return whether it worked
		return success;
	}
}
