package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Skill_Parry extends StdAbility
{
	boolean lastTime=false;
	public Skill_Parry()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Parry";
		displayText="";
		miscText="";

		canBeUninvoked=false;
		isAutoinvoked=true;
		quality=Ability.BENEFICIAL_SELF;

		baseEnvStats().setLevel(3);

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Skill_Parry();
	}

	public int classificationCode()
	{
		return Ability.SKILL;
	}

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
				Item myWeapon=mob.fetchWieldedItem();
				if((myWeapon!=null)
				&&(attackerWeapon!=null)
				&&(myWeapon instanceof Weapon)
				&&(attackerWeapon instanceof Weapon)
				&&(((Weapon)myWeapon).weaponClassification()!=Weapon.CLASS_FLAILED)
				&&(((Weapon)myWeapon).weaponClassification()!=Weapon.CLASS_NATURAL)
				&&(((Weapon)attackerWeapon).weaponClassification()!=Weapon.CLASS_FLAILED)
				&&(((Weapon)attackerWeapon).weaponClassification()!=Weapon.CLASS_NATURAL))
				{
					FullMsg msg=new FullMsg(mob,affect.source(),null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> parry(s) "+attackerWeapon.name()+" attack from <T-NAME>!");
					if((profficiencyCheck(mob.charStats().getDexterity()-70,false))
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