package com.planet_ink.coffee_mud.Abilities.Thief;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Expire;
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
   Copyright 2020-2022 Bo Zimmerman

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
public class Thief_Graverobbing extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_Graverobbing";
	}

	private final static String localizedName = CMLib.lang().L("Graverobbing");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ITEMS;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_ITEMS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	private static final String[] triggerStrings =I(new String[] {"GRAVEROBBING","GRAVEROB","GROB"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public boolean disregardsArmorCheck(final MOB mob)
	{
		return true;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_CRIMINAL;
	}

	static final String[] gravyWords= new String[] { "grave",  "graves", "graveyard", "crypt", "tomb", "coffin"};

	public boolean isGravyRoom(final Room R)
	{
		for(final String wd : gravyWords)
		{
			if(CMLib.english().containsString(R.displayText(), wd))
				return true;
		}
		final String roomID=CMLib.map().getExtendedRoomID(R);
		if(roomID.length()>0)
		{
			for(final Enumeration<Clan> c=CMLib.clans().clans();c.hasMoreElements();)
			{
				final Clan C=c.nextElement();
				final String morgueID=C.getMorgue();
				if(morgueID.equalsIgnoreCase(roomID))
					return true;
			}
			for(final Enumeration<String> i = CMLib.login().getBodyRoomIDs();i.hasMoreElements();)
			{
				final String id=i.nextElement();
				if(roomID.equalsIgnoreCase(id))
					return true;
			}
		}
		return false;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(commands.size()<1)
		{
			mob.tell(L("Which corpse or grave would you like to rob?"));
			return false;
		}
		final String what=CMParms.combine(commands,0);
		final Room R=mob.location();
		if(R==null)
			return false;
		Physical target=null;
		if(what.equalsIgnoreCase("here")
		||what.equalsIgnoreCase("room"))
			target=R;
		else
		{
			target=super.getAnyTarget(mob, R, true, commands, givenTarget, Wearable.FILTER_UNWORNONLY, true);
			if((target==null)||(!CMLib.flags().canBeSeenBy(target,mob)))
			{
				if(CMLib.english().containsString(R.displayText(), what)
				||CMLib.english().containsString(R.description(), what))
					target=R;
				else
				{

					final Item graveI=R.findItem("HoleInTheGround");
					if(graveI!=null)
						target=super.getTarget(mob, R, givenTarget, graveI, commands, Wearable.FILTER_UNWORNONLY, true);
					if((target==null)||(!CMLib.flags().canBeSeenBy(target,mob)))
					{
						mob.tell(L("You don't see '@x1' at this place.",what));
						return false;
					}
				}
			}
		}
		if(target instanceof Room)
		{
			if(!isGravyRoom((Room)target))
			{
				mob.tell(L("This place doesn't look much like a graveyard.",what));
				return false;
			}
		}
		else
		if(target instanceof DeadBody)
		{
			if(((DeadBody)target).isPlayerCorpse())
			{
				mob.tell(L("You can not rob @x1.",target.name(mob)));
				return false;
			}
		}
		else
		{
			mob.tell(L("You can not grave-rob @x1.",target.name(mob)));
			return false;
		}
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell(L("@x1 has already been graverobbed.",target.name(mob)));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int levelDiff=target.phyStats().level()-(mob.phyStats().level()+abilityCode()+(2*getXLEVELLevel(mob)));
		if(levelDiff<0)
			levelDiff=0;
		levelDiff*=5;
		final boolean success=proficiencyCheck(mob,-levelDiff,auto);

		final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_DELICATE_SMALL_HANDS_ACT,L("<S-NAME> rob(s) <T-NAMESELF>."));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			final double amtFound = 5+CMLib.dice().rollInRange(5+super.getXLEVELLevel(mob),(5*(1+super.getXLEVELLevel(mob))));
			final Coins C=(Coins)CMClass.getItem("StdCoins");
			final String currency=CMLib.beanCounter().getCurrency(R);
			final double denomination=CMLib.beanCounter().getBestDenomination(C.getCurrency(), amtFound);
			C.setCurrency(currency);
			C.setDenomination(denomination);
			C.setNumberOfCoins((int)Math.round(amtFound/denomination));
			final Container cI=(target instanceof DeadBody)?(DeadBody)target:null;
			C.setContainer(cI);
			C.recoverPhyStats();
			R.addItem(C, Expire.Monster_EQ);
			CMLib.commands().postGet(mob, cI, C, false);
			if(target instanceof Room)
				this.beneficialAffect(mob, target, asLevel, (int)CMProps.getTicksPerDay());
			else
				this.beneficialAffect(mob, target, asLevel, Integer.MAX_VALUE/4);
		}
		return success;
	}

}
