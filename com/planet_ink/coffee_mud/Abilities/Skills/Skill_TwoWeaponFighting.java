package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Skill_TwoWeaponFighting extends StdAbility
{
	public String ID() { return "Skill_TwoWeaponFighting"; }
	public String name(){ return "Two Weapon Fighting";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.BENEFICIAL_SELF;}
	public int classificationCode(){return Ability.SKILL;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	
	private boolean middleOfTheFight=false;
	private Weapon lastWeapon=null;
	public Environmental newInstance(){	return new Skill_TwoWeaponFighting();	}

	private Weapon getSecondWeapon(MOB mob)
	{
		if((lastWeapon!=null)
		&&(lastWeapon.amWearingAt(Item.HELD))
		&&(!lastWeapon.amWearingAt(Item.WIELD))
		&&(lastWeapon.container()==null))
			return lastWeapon;
		Weapon weapon=null;
		for(int i=0;i<mob.inventorySize();i++)
		{
			Item item=mob.fetchInventory(i);
			if((item instanceof Weapon)
			    &&(item.amWearingAt(Item.HELD))
				&&(!item.amWearingAt(Item.WIELD))
			    &&(item.container()==null))
			{ weapon=(Weapon)item; break; }
		}
		lastWeapon=weapon;
		return weapon;
	}
	
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		if(affected instanceof MOB)
		{
			MOB mob=(MOB)affected;
			
			if(affectableStats.speed()>=2.0) 
				affectableStats.setSpeed(affectableStats.speed()-1.0);
			if(middleOfTheFight)
			{
				affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()-(affectableStats.attackAdjustment()/2));
				Item w=mob.fetchWieldedItem();
				if((w!=null)&&(lastWeapon!=null))
					affectableStats.setDamage(affectableStats.damage()-w.envStats().damage()+lastWeapon.envStats().damage());
			}
		}
	}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if((tickID==Host.MOB_TICK)&&(affected instanceof MOB))
		{
			MOB mob=(MOB)affected;
			if(mob.isInCombat())
			{
				if(Util.bset(mob.getBitmap(),MOB.ATT_AUTODRAW))
					ExternalPlay.drawIfNecessary(mob,true);
				Item weapon=getSecondWeapon(mob);
				if((weapon!=null) // try to wield anything!
				&&(mob.rangeToTarget()>=0)
				&&(mob.rangeToTarget()>=weapon.minRange())
				&&(mob.rangeToTarget()<=weapon.maxRange())
				&&(Sense.aliveAwakeMobile(mob,true))
				&&(!mob.amDead())
				&&(mob.curState().getHitPoints()>0)
				&&(!Sense.isSitting(mob)))
				{
					if(mob.fetchAffect(mob.numAffects()-1)!=this)
					{
						mob.delAffect(this);
						mob.addAffect(this);
					}
					middleOfTheFight=true;
					mob.recoverEnvStats();
					ExternalPlay.postAttack(mob,mob.getVictim(),weapon);
					middleOfTheFight=false;
					mob.recoverEnvStats();
					helpProfficiency(mob);
				}
			}
		}
		return super.tick(ticking,tickID);
	}
}
