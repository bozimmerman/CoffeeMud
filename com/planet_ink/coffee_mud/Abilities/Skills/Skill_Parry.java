package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Skill_Parry extends StdAbility
{
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

		addQualifyingClass("Fighter",3);
		addQualifyingClass("Ranger",3);
		addQualifyingClass("Paladin",3);
		addQualifyingClass("Thief",9);
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

		if(affect.amITarget(mob)&&(Sense.aliveAwakeMobile(mob,true))&&(affect.targetMinor()==Affect.TYP_WEAPONATTACK))
		{
			if((affect.tool()!=null)&&(affect.tool() instanceof Item))
			{
				Item attackerWeapon=(Item)affect.tool();
				if((mob.fetchWieldedItem()!=null)
				&&((attackerWeapon!=null)
				&&((attackerWeapon instanceof Weapon))
				&&(((Weapon)attackerWeapon).weaponType()!=Weapon.TYPE_NATURAL)))
				{
					FullMsg msg=new FullMsg(mob,affect.source(),null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> parry(s) "+affect.source().fetchWieldedItem().name()+" attack from <T-NAME>!");
					if((profficiencyCheck(mob.charStats().getDexterity()-70,false))
					&&(mob.location().okAffect(msg)))
					{
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