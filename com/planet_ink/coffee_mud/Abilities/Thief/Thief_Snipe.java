package com.planet_ink.coffee_mud.Abilities.Thief;
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

public class Thief_Snipe extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_Snipe";
	}

	private final static String localizedName = CMLib.lang().L("Snipe");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		return "";
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_OTHERS;
	}

	private static final String[] triggerStrings =I(new String[] {"SNIPE"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	protected int overrideMana()
	{
		return 100;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT|USAGE_MANA;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_DIRTYFIGHTING;
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(mob.isInCombat())
				return Ability.QUALITY_INDIFFERENT;
			if(CMLib.flags().isSitting(mob))
				return Ability.QUALITY_INDIFFERENT;
			if(!CMLib.flags().isAliveAwakeMobileUnbound(mob,false))
				return Ability.QUALITY_INDIFFERENT;
			if(target instanceof MOB)
			{
				if(CMLib.flags().canBeSeenBy(mob,(MOB)target))
					return Ability.QUALITY_INDIFFERENT;
				final Item w=mob.fetchWieldedItem();
				if((w==null)
				||(!(w instanceof Weapon)))
					return Ability.QUALITY_INDIFFERENT;
				final Weapon ww=(Weapon)w;
				if(((ww.weaponClassification()!=Weapon.CLASS_RANGED)&&(ww.weaponClassification()!=Weapon.CLASS_THROWN))
				||(w.maxRange()<=0))
					return Ability.QUALITY_INDIFFERENT;
				return Ability.QUALITY_MALICIOUS;
			}
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(mob.isInCombat())
		{
			mob.tell(L("Not while in combat!"));
			return false;
		}
		final MOB target=getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(CMLib.flags().isSitting(mob))
		{
			mob.tell(L("You need to stand up!"));
			return false;
		}
		if(!CMLib.flags().isAliveAwakeMobileUnbound(mob,false))
			return false;
		if(CMLib.flags().canBeSeenBy(mob,target))
		{
			mob.tell(L("@x1 is watching you too closely.",target.name(mob)));
			return false;
		}
		final Item w=mob.fetchWieldedItem();
		if((w==null)
		||(!(w instanceof Weapon)))
		{
			mob.tell(L("You need a weapon to snipe."));
			return false;
		}
		final Weapon ww=(Weapon)w;
		if(((ww.weaponClassification()!=Weapon.CLASS_RANGED)&&(ww.weaponClassification()!=Weapon.CLASS_THROWN))
		||(w.maxRange()<=0))
		{
			mob.tell(L("You need a ranged weapon to snipe."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		final int code=CMMsg.MASK_MALICIOUS|CMMsg.MSG_THIEF_ACT;
		final String str=auto?"":L("<S-NAME> strike(s) <T-NAMESELF> from the shadows!");
		final int otherCode=success?code:CMMsg.NO_EFFECT;
		final String otherStr=success?str:null;
		final CMMsg msg=CMClass.getMsg(mob,target,this,code,str,otherCode,otherStr,otherCode,otherStr);
		if(mob.location().okMessage(mob,msg))
		{
			final boolean alwaysInvis=CMath.bset(mob.basePhyStats().disposition(),PhyStats.IS_INVISIBLE);
			if(!alwaysInvis)
				mob.basePhyStats().setDisposition(mob.basePhyStats().disposition()|PhyStats.IS_INVISIBLE);
			mob.recoverPhyStats();
			mob.location().send(mob,msg);
			CMLib.combat().postAttack(mob,target,w);
			if(!alwaysInvis)
				mob.basePhyStats().setDisposition(mob.basePhyStats().disposition()-PhyStats.IS_INVISIBLE);
			mob.recoverPhyStats();
			if(success)
			{
				final MOB oldVictim=target.getVictim();
				final MOB oldVictim2=mob.getVictim();
				if(oldVictim==mob)
					target.makePeace(true);
				if(oldVictim2==target)
					mob.makePeace(true);
				if(mob.fetchEffect("Thief_Hide")==null)
				{
					final Ability hide=mob.fetchAbility("Thief_Hide");
					if(hide!=null)
						hide.invoke(mob,null,false,asLevel);

					mob.location().recoverRoomStats();
					if(CMLib.flags().canBeSeenBy(mob,target))
					{
						target.setVictim(oldVictim);
						mob.setVictim(oldVictim2);
					}
				}
			}
		}
		return success;
	}
}
