package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Skill_Parry extends StdAbility
{
	public String ID() { return "Skill_Parry"; }
	public String name(){ return "Parry";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.BENEFICIAL_SELF;}
	public int classificationCode(){return Ability.SKILL;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	private boolean doneThisRound=false;

	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==MudHost.TICK_MOB)
			doneThisRound=false;
		return super.tick(ticking,tickID);
	}


	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		if(msg.amITarget(mob)
		   &&(Sense.aliveAwakeMobile(mob,true))
		   &&(msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
		   &&(!doneThisRound)
		   &&(mob.rangeToTarget()==0))
		{
			if((msg.tool()!=null)&&(msg.tool() instanceof Item))
			{
				Item attackerWeapon=(Item)msg.tool();
				Item myWeapon=mob.fetchWieldedItem();
				if((myWeapon!=null)
				&&(attackerWeapon!=null)
				&&(myWeapon instanceof Weapon)
				&&(attackerWeapon instanceof Weapon)
				&&(((Weapon)myWeapon).weaponClassification()!=Weapon.CLASS_FLAILED)
				&&(((Weapon)myWeapon).weaponClassification()!=Weapon.CLASS_RANGED)
				&&(((Weapon)myWeapon).weaponClassification()!=Weapon.CLASS_THROWN)
				&&(((Weapon)myWeapon).weaponClassification()!=Weapon.CLASS_NATURAL)
				&&(((Weapon)attackerWeapon).weaponClassification()!=Weapon.CLASS_FLAILED)
				&&(((Weapon)attackerWeapon).weaponClassification()!=Weapon.CLASS_NATURAL)
				&&(((Weapon)attackerWeapon).weaponClassification()!=Weapon.CLASS_RANGED)
				&&(((Weapon)attackerWeapon).weaponClassification()!=Weapon.CLASS_THROWN))
				{
					FullMsg msg2=new FullMsg(mob,msg.source(),this,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> parr(ys) "+attackerWeapon.name()+" attack from <T-NAME>!");
					if((profficiencyCheck(null,mob.charStats().getStat(CharStats.DEXTERITY)-90,false))
					&&(mob.location().okMessage(mob,msg2)))
					{
						doneThisRound=true;
						mob.location().send(mob,msg2);
						helpProfficiency(mob);
						return false;
					}
				}
			}
		}
		return true;
	}
}
