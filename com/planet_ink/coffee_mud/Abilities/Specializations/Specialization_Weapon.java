package com.planet_ink.coffee_mud.Abilities.Specializations;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Specialization_Weapon extends StdAbility
{
	protected boolean activated=false;
	protected int weaponType=-1;
	protected int secondWeaponType=-1;

	public Specialization_Weapon()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Weapon Specialization";
		displayText="";
		miscText="";

		quality=Ability.BENEFICIAL_SELF;

		canTargetCode=0;
		canAffectCode=Ability.CAN_MOBS;
		
		canBeUninvoked=false;
		isAutoinvoked=true;

		baseEnvStats().setLevel(1);

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Specialization_Weapon();
	}


	public int classificationCode()
	{
		return Ability.SKILL;
	}

	public void affect(Affect affect)
	{
		if((activated)
		&&(Dice.rollPercentage()<25)
		&&(affected instanceof MOB)
		&&(affect.amISource((MOB)affected))
		&&(affect.targetMinor()==Affect.TYP_WEAPONATTACK)
		&&((affect.tool()!=null)
		&&(affect.tool() instanceof Weapon)
		&&((((Weapon)affect.tool()).weaponClassification()==weaponType)
 		 ||(weaponType<0)
		 ||(((Weapon)affect.tool()).weaponClassification()==secondWeaponType))))
			helpProfficiency((MOB)affected);
	}


	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		activated=false;
		if(affected instanceof MOB)
		{
			Item myWeapon=((MOB)affected).fetchWieldedItem();
			if((myWeapon!=null)
			&&(myWeapon instanceof Weapon)
			&&((((Weapon)myWeapon).weaponClassification()==weaponType)
 			 ||(weaponType<0)
			 ||(((Weapon)myWeapon).weaponClassification()==secondWeaponType)))
			{
				activated=true;
				affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+(int)Math.round(15.0*(Util.div(profficiency(),100.0))));
			}
		}
	}
}
