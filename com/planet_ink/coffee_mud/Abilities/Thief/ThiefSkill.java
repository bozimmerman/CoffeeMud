package com.planet_ink.coffee_mud.Abilities.Thief;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class ThiefSkill extends StdAbility
{
	public String ID() { return "ThiefSkill"; }
	public String name(){ return "a Thief Skill";}
	public int quality(){return Ability.INDIFFERENT;}
	public int classificationCode(){	return Ability.THIEF_SKILL;}
	public Environmental newInstance(){	return new ThiefSkill();}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		if((!auto)
		&&(!mob.isMonster())
		&&(!disregardsArmorCheck(mob))
		&&(!CoffeeUtensils.armorCheck(mob,CharClass.ARMOR_LEATHER))
		&&(mob.isMine(this))
		&&(mob.location()!=null)
		&&(Dice.rollPercentage()<50))
		{
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> fumble(s) "+name()+" due to <S-HIS-HER> clumsy armor!");
			return false;
		}
		return true;
	}
}
