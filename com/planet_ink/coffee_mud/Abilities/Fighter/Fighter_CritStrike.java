package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Fighter_CritStrike extends StdAbility
{
	private int oldDamage=0;

	public Fighter_CritStrike()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Critical Strike";
		displayText="";
		miscText="";

		canBeUninvoked=false;
		isAutoinvoked=true;
		quality=Ability.BENEFICIAL_SELF;

		baseEnvStats().setLevel(5);

		addQualifyingClass("Fighter",5);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Fighter_CritStrike();
	}

	public int classificationCode()
	{
		return Ability.SKILL;
	}

	public void affectEnvStats(Environmental affected, EnvStats affectedStats)
	{
		if((affected!=null)&&(affected instanceof MOB))
		{
			MOB mob=(MOB)affected;
			Item I=mob.fetchWieldedItem();
			if((I instanceof Weapon)&&(((Weapon)I).weaponClassification()!=Weapon.CLASS_NATURAL))
			{
				if((affectedStats.level()>=envStats().level())
				&&(profficiencyCheck(-65,false)))
					affectedStats.setDamage(affectedStats.damage()+(int)Math.round(Util.mul(I.envStats().damage(),(Util.div(profficiency(),100.0)))));
			}
		}
	}
}
