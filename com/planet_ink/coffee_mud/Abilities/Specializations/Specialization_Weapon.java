package com.planet_ink.coffee_mud.Abilities.Specializations;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Specialization_Weapon extends StdAbility
{
	public String ID() { return "Specialization_Weapon"; }
	public String name(){ return "Weapon Specialization";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.BENEFICIAL_SELF;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	
	protected boolean activated=false;
	protected int weaponType=-1;
	protected int secondWeaponType=-1;

	public int classificationCode(){return Ability.SKILL;}
	public Environmental newInstance(){	return new Specialization_Weapon();}

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
