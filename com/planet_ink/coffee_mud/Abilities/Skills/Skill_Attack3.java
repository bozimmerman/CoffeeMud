package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Skill_Attack3 extends StdAbility
{
	private boolean temporarilyDisable=false;

	public Skill_Attack3()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Third Attack";
		displayText="";
		miscText="";

		quality=Ability.BENEFICIAL_SELF;
		canBeUninvoked=false;
		isAutoinvoked=true;

		baseEnvStats().setLevel(18);

		addQualifyingClass("Fighter",18);
		addQualifyingClass("Ranger",20);
		addQualifyingClass("Paladin",20);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Skill_Attack3();
	}

	public int classificationCode()
	{
		return Ability.SKILL;
	}

	public void affect(Affect affect)
	{
		super.affect(affect);

		if((affected==null)||(!(affected instanceof MOB)))
			return;

		MOB mob=(MOB)affected;
		if((!temporarilyDisable)&&(affect.amISource(mob))&&(affect.sourceMinor()==Affect.TYP_WEAPONATTACK))
		{
			if((mob.isInCombat())&&(!mob.amDead())&&(affect.target() instanceof MOB))
			{
				if((mob.envStats().level()>=envStats().level())
				&&(profficiencyCheck(0,false)))
				{
					Weapon weapon=(Weapon)CMClass.getWeapon("Natural");
					if((affect.tool()!=null)&&(affect.tool() instanceof Weapon))
						weapon=(Weapon)affect.tool();
					temporarilyDisable=true;
					ExternalPlay.doAttack(mob,(MOB)affect.target(),weapon);
					temporarilyDisable=false;
					helpProfficiency(mob);
				}
			}
		}
	}
}
