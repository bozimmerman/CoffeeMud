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

public class Spell_KeenEdge extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_KeenEdge";
	}

	private final static String localizedName = CMLib.lang().L("Keen Edge");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_ITEMS;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_ITEMS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_ALTERATION;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition() | PhyStats.IS_BONUS);
		final int xlvl=CMath.s_int(text());
		affectableStats.setDamage(affectableStats.damage()+1+(int)Math.round(CMath.mul(affectableStats.damage(),0.2 + (0.01*xlvl))));
		affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+3+xlvl+(int)Math.round(CMath.mul(affectableStats.attackAdjustment(),0.1)));
	}

	@Override
	public void unInvoke()
	{
		final Physical affected = super.affected;
		super.unInvoke();
		if(canBeUninvoked() && (affected instanceof Item))
		{
			final Item item=(Item)affected;
			if(item.owner() instanceof Room)
				((Room)item.owner()).showHappens(CMMsg.MSG_OK_VISUAL, item, L("<S-NAME> loses its keen edge."));
			else
			if(item.owner() instanceof MOB)
				((MOB)item.owner()).tell(((MOB)item.owner()),item,null,L("<T-NAME> loses its keen edge."));
		}
	}
	
	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Item target=getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_ANY);
		if(target==null)
			return false;

		if((!(target instanceof Weapon))
		||((((Weapon)target).weaponClassification()!=Weapon.CLASS_AXE)
				&&(((Weapon)target).weaponClassification()!=Weapon.CLASS_DAGGER)
				&&(((Weapon)target).weaponClassification()!=Weapon.CLASS_EDGED)
				&&(((Weapon)target).weaponClassification()!=Weapon.CLASS_SWORD)
				&&(((Weapon)target).weaponDamageType()!=Weapon.TYPE_PIERCING)
				&&(((Weapon)target).weaponDamageType()!=Weapon.TYPE_SLASHING)))
		{
			mob.tell(mob,target,null,L("You can't enchant <T-NAME> with a Keen Edge spell!"));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> hold(s) <T-NAMESELF> and cast(s) a spell.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,L("<T-NAME> take(s) on a keen and sharp edge!"));
				Ability A=beneficialAffect(mob, target, asLevel, 0);
				if(A!=null)
					A.setMiscText(""+super.getXLEVELLevel(mob));
				target.recoverPhyStats();
				mob.recoverPhyStats();
			}
		}
		else
			beneficialWordsFizzle(mob,target,L("<S-NAME> hold(s) <T-NAMESELF> tightly and whisper(s), but fail(s) to cast a spell."));

		// return whether it worked
		return success;
	}
}
