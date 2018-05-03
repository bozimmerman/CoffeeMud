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
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary;
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
public class Spell_Duplicate extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_Duplicate";
	}

	private final static String	localizedName	= CMLib.lang().L("Duplicate");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_ITEMS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL | Ability.DOMAIN_ALTERATION;
	}

	@Override
	protected int overrideMana()
	{
		return Ability.COST_ALL;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Item target=getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_UNWORNONLY);
		if(target==null)
			return false;

		if(!mob.isMine(target))
		{
			mob.tell(L("You'll need to pick it up first."));
			return false;
		}
		if(target instanceof ClanItem)
		{
			mob.tell(L("Clan items can not be duplicated."));
			return false;
		}
		if(target instanceof ArchonOnly)
		{
			mob.tell(L("That item can not be duplicated."));
			return false;
		}
		if(target instanceof BoardableShip)
		{
			mob.tell(L("That item can not be duplicated."));
			return false;
		}
		if(target instanceof PackagedItems)
		{
			mob.tell(L("That item can not be duplicated."));
			return false;
		}

		int value=0;
		value = (target instanceof Coins)?(int)Math.round(((Coins)target).getTotalValue()):target.value();
		int multiPlier=5+(((target.phyStats().weight())+value)/2);
		multiPlier+=(target.numEffects()*10);
		multiPlier+=(target instanceof Potion)?10:0;
		multiPlier+=(target instanceof Pill)?10:0;
		multiPlier+=(target instanceof Wand)?5:0;

		int level=target.phyStats().level();
		if(level<=0)
			level=1;
		int expLoss=(level*multiPlier);
		if((mob.getExperience()-expLoss)<0)
		{
			mob.tell(L("You don't have enough experience to cast this spell."));
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		expLoss=getXPCOSTAdjustment(mob,-expLoss);
		mob.tell(L("You lose @x1 experience points.",""+(-expLoss)));
		CMLib.leveler().postExperience(mob,null,null,expLoss,false);

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> hold(s) <T-NAMESELF> and cast(s) a spell.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final Item newTarget=(Item)target.copyOf();
				mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,L("@x1 blurs and divides into two!",target.name()));
				CMLib.utensils().disenchantItem(newTarget);
				if(newTarget.amDestroyed())
					mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,L("<T-NAME> fades away!"));
				else
				{
					newTarget.recoverPhyStats();
					if(target.owner() instanceof MOB)
						((MOB)target.owner()).addItem(newTarget);
					else
					if(target.owner() instanceof Room)
						((Room)target.owner()).addItem(newTarget,ItemPossessor.Expire.Player_Drop);
					else
						mob.addItem(newTarget);
					if(newTarget instanceof Coins)
						((Coins)newTarget).putCoinsBack();
					else
					if(newTarget instanceof RawMaterial)
						((RawMaterial)newTarget).rebundle();
					target.recoverPhyStats();
					mob.recoverPhyStats();
				}
			}
		}
		else
			beneficialWordsFizzle(mob,target,L("<S-NAME> hold(s) <T-NAMESELF> tightly and incant(s), the spell fizzles."));

		// return whether it worked
		return success;
	}
}
