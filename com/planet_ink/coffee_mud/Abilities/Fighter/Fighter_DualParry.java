package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Fighter_DuelParry extends StdAbility
{
	public String ID() { return "Fighter_DuelParry"; }
	public String name(){ return "Duel Parry";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.BENEFICIAL_SELF;}
	public int classificationCode(){return Ability.SKILL;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	
	boolean lastTime=false;
	public Environmental newInstance(){	return new Fighter_DuelParry();}

	public boolean okAffect(Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		if(affect.amITarget(mob)
		   &&(Sense.aliveAwakeMobile(mob,true))
		   &&(affect.targetMinor()==Affect.TYP_WEAPONATTACK)
		   &&(mob.rangeToTarget()==0))
		{
			if((affect.tool()!=null)&&(affect.tool() instanceof Item))
			{
				Item attackerWeapon=(Item)affect.tool();
				Item myOtherWeapon=mob.fetchWornItem(Item.HELD);
				if((myOtherWeapon!=null)
				&&(attackerWeapon!=null)
				&&(myOtherWeapon instanceof Weapon)
				&&(attackerWeapon instanceof Weapon)
				&&(!myOtherWeapon.rawLogicalAnd())
				&&(((Weapon)myOtherWeapon).weaponClassification()!=Weapon.CLASS_FLAILED)
				&&(((Weapon)myOtherWeapon).weaponClassification()!=Weapon.CLASS_RANGED)
				&&(((Weapon)myOtherWeapon).weaponClassification()!=Weapon.CLASS_THROWN)
				&&(((Weapon)myOtherWeapon).weaponClassification()!=Weapon.CLASS_NATURAL)
				&&(((Weapon)attackerWeapon).weaponClassification()!=Weapon.CLASS_FLAILED)
				&&(((Weapon)attackerWeapon).weaponClassification()!=Weapon.CLASS_NATURAL)
				&&(((Weapon)attackerWeapon).weaponClassification()!=Weapon.CLASS_RANGED)
				&&(((Weapon)attackerWeapon).weaponClassification()!=Weapon.CLASS_THROWN))
				{
					FullMsg msg=new FullMsg(mob,affect.source(),null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> parry(s) "+attackerWeapon.name()+" attack with "+myOtherWeapon.name()+"!");
					if((profficiencyCheck(mob.charStats().getStat(CharStats.DEXTERITY)-85,false))
					&&(!lastTime)
					&&(mob.location().okAffect(msg)))
					{
						lastTime=true;
						mob.location().send(mob,msg);
						helpProfficiency(mob);
						return false;
					}
					else
						lastTime=false;
				}
			}
		}
		return true;
	}
}