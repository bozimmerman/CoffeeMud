package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Fighter_MonkeyPunch extends StdAbility
{
	public String ID() { return "Fighter_MonkeyPunch"; }
	public String name(){ return "Monkey Punch";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.MALICIOUS;}
	public int classificationCode(){return Ability.SKILL;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	public Environmental newInstance(){	return new Fighter_MonkeyPunch();}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((tickID==Host.TICK_MOB)
		   &&(affected!=null)
		   &&(affected instanceof MOB))
		{
			MOB mob=(MOB)affected;
			if((mob.isInCombat())
			&&(Sense.aliveAwakeMobile(mob,true))
			&&(mob.rangeToTarget()==0)
			&&(mob.charStats().getBodyPart(Race.BODY_HAND)>1)
			&&(!anyWeapons(mob)))
			{
				if(Dice.rollPercentage()>95)
					helpProfficiency(mob);
				Weapon naturalWeapon=CMClass.getWeapon("GenWeapon");
				naturalWeapon.setName("a monkey punch");
				naturalWeapon.setWeaponType(Weapon.TYPE_BASHING);
				naturalWeapon.recoverEnvStats();
				helpProfficiency(mob);
				ExternalPlay.postAttack(mob,mob.getVictim(),naturalWeapon);
			}
		}
		return true;
	}

	public boolean anyWeapons(MOB mob)
	{
		for(int i=0;i<mob.inventorySize();i++)
		{
			Item I=mob.fetchInventory(i);
			if((I!=null)
			   &&((I.amWearingAt(Item.WIELD))
			      ||(I.amWearingAt(Item.HELD))))
				return true;
		}
		return false;
	}
}
