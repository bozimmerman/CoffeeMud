package com.planet_ink.coffee_mud.Abilities.Fighter;
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

public class Fighter_ReturnProjectile extends FighterSkill
{
	@Override
	public String ID()
	{
		return "Fighter_ReturnProjectile";
	}

	private final static String localizedName = CMLib.lang().L("Return Projectile");

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
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
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

	@Override
	public boolean isAutoInvoked()
	{
		return true;
	}

	@Override
	public boolean canBeUninvoked()
	{
		return false;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_EVASIVE;
	}

	public boolean doneThisRound=false;

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if(!(affected instanceof MOB))
			return true;

		final MOB mob=(MOB)affected;
		if(msg.amITarget(mob)
		&&(!doneThisRound)
		&&(CMLib.flags().isAliveAwakeMobileUnbound(mob,true))
		&&(msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
		&&(msg.tool() instanceof Weapon)
		&&((((Weapon)msg.tool()).weaponClassification()==Weapon.CLASS_RANGED)
		   ||(((Weapon)msg.tool()).weaponClassification()==Weapon.CLASS_THROWN))
		&&(!(msg.tool() instanceof Electronics))
		&&(mob.rangeToTarget()>0)
		&&(mob.charStats().getBodyPart(Race.BODY_HAND)>1)
		&&((mob.fetchAbility(ID())==null)||proficiencyCheck(mob,-85+mob.charStats().getStat(CharStats.STAT_DEXTERITY)+(2*getXLEVELLevel(mob)),false))
		&&(mob.freeWearPositions(Wearable.WORN_HELD,(short)0,(short)0)>0))
		{
			Item w=(Item)msg.tool();
			if((((Weapon)w).weaponClassification()==Weapon.CLASS_THROWN)
			&&(msg.source().isMine(w)))
			{
				if(!w.amWearingAt(Wearable.IN_INVENTORY))
					CMLib.commands().postRemove(msg.source(),w,true);
				CMLib.commands().postDrop(msg.source(),w,true,false,false);
			}
			else
			if((w instanceof AmmunitionWeapon) && ((AmmunitionWeapon)w).requiresAmmunition())
			{
				final Weapon neww=CMClass.getWeapon("GenWeapon");
				String ammo="";
				if(neww instanceof AmmunitionWeapon)
					ammo=((AmmunitionWeapon)w).ammunitionType();
				if(ammo.length()==0)
					return true;
				if(ammo.endsWith("s"))
					ammo=ammo.substring(0,ammo.length()-1);
				ammo=CMLib.english().startWithAorAn(ammo);
				neww.setName(ammo);
				neww.setDisplayText(L("@x1 sits here.",ammo));
				neww.setUsesRemaining(1);
				neww.setMaterial(((Weapon)w).material());
				neww.setWeaponClassification(Weapon.CLASS_THROWN);
				neww.setWeaponDamageType(((Weapon)w).weaponDamageType());
				neww.basePhyStats().setWeight(1);
				neww.setBaseValue(0);
				neww.setRanges(w.minRange(),w.maxRange());
				neww.recoverPhyStats();
				w=neww;
				mob.location().addItem(neww,ItemPossessor.Expire.Player_Drop);
			}
			if(mob.location().isContent(w))
			{
				final CMMsg msg2=CMClass.getMsg(mob,w,msg.source(),CMMsg.MSG_GET,L("<S-NAME> catch(es) the <T-NAME> shot by <O-NAME>!"));
				if(mob.location().okMessage(mob,msg2))
				{
					mob.location().send(mob,msg2);
					if(mob.isMine(w))
						CMLib.combat().postAttack(mob,msg.source(),w);
					doneThisRound=true;
					helpProficiency(mob, 0);
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==Tickable.TICKID_MOB)
			doneThisRound=false;
		return super.tick(ticking,tickID);
	}
}
