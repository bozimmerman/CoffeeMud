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

	public boolean tick(int tickID)
	{
		if(tickID==Host.MOB_TICK)
			doneThisRound=false;
		return super.tick(tickID);
	}
	
	public Environmental newInstance(){	return new Skill_Parry();}

	public boolean okAffect(Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		if(affect.amITarget(mob)
		   &&(Sense.aliveAwakeMobile(mob,true))
		   &&(affect.targetMinor()==Affect.TYP_WEAPONATTACK)
		   &&(!doneThisRound)
		   &&(mob.rangeToTarget()==0))
		{
			if((affect.tool()!=null)&&(affect.tool() instanceof Item))
			{
				Item attackerWeapon=(Item)affect.tool();
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
					FullMsg msg=new FullMsg(mob,affect.source(),null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> parry(s) "+attackerWeapon.name()+" attack from <T-NAME>!");
					if((profficiencyCheck(mob.charStats().getStat(CharStats.DEXTERITY)-70,false))
					&&(mob.location().okAffect(msg)))
					{
						doneThisRound=true;
						mob.location().send(mob,msg);
						helpProfficiency(mob);
						return false;
					}
				}
			}
		}
		return true;
	}
}