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
   Copyright 2004-2025 Bo Zimmerman

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
public class Fighter_FarShot extends FighterSkill
{
	@Override
	public String ID()
	{
		return "Fighter_FarShot";
	}

	private final static String localizedName = CMLib.lang().L("Far Shot");

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
		return Ability.QUALITY_INDIFFERENT;
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
		return Ability.ACODE_SKILL|Ability.DOMAIN_MARTIALLORE;
	}

	public int checkDown=4;

	protected List<Weapon> qualifiedWeapons=new Vector<Weapon>();

	@Override
	protected void cloneFix(final Ability E)
	{
		super.cloneFix(E);
		qualifiedWeapons=new XVector<Weapon>(((Fighter_FarShot)E).qualifiedWeapons);
	}

	@Override
	public void setMiscText(final String newText)
	{
		super.setMiscText(newText);
		qualifiedWeapons=new Vector<Weapon>();
	}

	@Override
	public void executeMsg(final Environmental host, final CMMsg msg)
	{
		super.executeMsg(host,msg);
		if((affected instanceof Weapon)
		&&((Weapon)affected).amWearingAt(Wearable.IN_INVENTORY))
		{
			final Weapon targetW=(Weapon)affected;
			if(targetW != null)
			{
				qualifiedWeapons.remove(targetW);
				targetW.delEffect(targetW.fetchEffect(ID()));
				targetW.recoverPhyStats();
			}
		}
		else
		if((msg.source()==affected)
		&&(msg.target() instanceof AmmunitionWeapon))
		{
			final AmmunitionWeapon targetW=(AmmunitionWeapon)msg.target();
			if((targetW.weaponClassification()==Weapon.CLASS_RANGED)
			&&(targetW.ammunitionType().length()>0))
			{
				if(((msg.targetMinor()==CMMsg.TYP_WEAR)
					||(msg.targetMinor()==CMMsg.TYP_WIELD)
					||(msg.targetMinor()==CMMsg.TYP_HOLD))
				&&(!qualifiedWeapons.contains(msg.target()))
				&&((msg.source().fetchAbility(ID())==null)||proficiencyCheck(null,0,false)))
				{
					qualifiedWeapons.add(targetW);
					final Ability A=(Ability)this.copyOf();
					A.setSavable(false);
					A.setInvoker(invoker());
					targetW.addEffect(A);
					A.makeLongLasting();
					targetW.recoverPhyStats();
				}
				else
				if(((msg.targetMinor()==CMMsg.TYP_REMOVE)
					||(msg.targetMinor()==CMMsg.TYP_DROP))
				&&(qualifiedWeapons.contains(targetW)))
				{
					qualifiedWeapons.remove(targetW);
					targetW.delEffect(targetW.fetchEffect(ID()));
					targetW.recoverPhyStats();
				}
			}
		}
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(affected instanceof Weapon)
		{
			affectableStats.setArmor((affectableStats.armor()&Weapon.MASK_MAXRANGEZERO)
					+Weapon.MASK_MAXRANGEFLAG+100); // sets armor max-range bits to 100
		}
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(!(affected instanceof MOB))
			return true;

		final MOB mob=(MOB)affected;
		if(--checkDown<=0)
		{
			checkDown=5;
			final Item w=mob.fetchWieldedItem();
			if((w!=null)
			&&(w instanceof AmmunitionWeapon)
			&&(((Weapon)w).weaponClassification()==Weapon.CLASS_RANGED)
			&&(((AmmunitionWeapon)w).ammunitionType().length()>0)
			&&((mob.fetchAbility(ID())==null)||proficiencyCheck(null,0,false)))
			{
				if((CMLib.dice().rollPercentage()<10)&&(mob.isInCombat()) && (mob.rangeToTarget() > 0))
					helpProficiency(mob, 0);
				if(!qualifiedWeapons.contains(w))
				{
					qualifiedWeapons.add((Weapon)w);
					final Ability A=(Ability)this.copyOf();
					A.setSavable(false);
					A.setInvoker(invoker());
					w.addEffect(A);
					A.makeLongLasting();
					w.recoverPhyStats();
				}
			}
			for(int i=qualifiedWeapons.size()-1;i>=0;i--)
			{
				final Item I=qualifiedWeapons.get(i);
				if((I.amWearingAt(Wearable.IN_INVENTORY))||(I.owner()!=affected))
				{
					qualifiedWeapons.remove(I);
					I.delEffect(I.fetchEffect(ID()));
					I.recoverPhyStats();
				}
			}
		}
		return true;
	}
}
