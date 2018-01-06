package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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

import java.io.IOException;
import java.util.*;

public class Fighter_SnapKick extends StdAbility
{
	@Override
	public String ID()
	{
		return "Fighter_SnapKick";
	}

	@Override
	public String name()
	{
		return "Snap Kick";
	}

	@Override
	public String displayText()
	{
		return "";
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_KICKING;
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
	public Environmental newInstance()
	{
		return new Fighter_SnapKick();
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((tickID==Tickable.TICKID_MOB)
		&&(affected!=null)
		&&(affected instanceof MOB))
		{
			MOB mob=(MOB)affected;
			if((mob.isInCombat())
			&&(CMLib.flags().isAliveAwakeMobile(mob,true))
			&&(mob.rangeToTarget()==0)
			&&(!anyWeapons(mob)))
			{
				if(CMLib.dice().rollPercentage()>95)
					helpProficiency(mob,0);
				Weapon naturalWeapon=CMClass.getWeapon("GenWeapon");
				naturalWeapon.setName(L("a snap kick"));
				naturalWeapon.setWeaponDamageType(Weapon.TYPE_BASHING);
				naturalWeapon.recoverPhyStats();
				CMLib.combat().postAttack(mob,mob.getVictim(),naturalWeapon);
			}
		}
		return true;
	}

	public boolean anyWeapons(MOB mob)
	{
		for(int i=0;i<mob.numItems();i++)
		{
			Item I=mob.getItem(i);
			if((I!=null)
			&&((I.amWearingAt(Item.WORN_WIELD))
				||(I.amWearingAt(Item.WORN_HELD))))
				return true;
		}
		return false;
	}
}
