package com.planet_ink.coffee_mud.Abilities.Ranger;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Ranger_WoodlandLore extends StdAbility
{
	public String ID() { return "Ranger_WoodlandLore"; }
	public String name(){ return "Woodland Lore";}
	public String displayText(){ return "";}
	public int quality(){return Ability.BENEFICIAL_SELF;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	public Environmental newInstance(){	return new Ranger_WoodlandLore();}
	public int classificationCode(){ return Ability.SKILL;}
	
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if((invoker==null)&&(affected instanceof MOB))
		   invoker=(MOB)affected;
		if((invoker!=null)
		   &&(invoker.location()!=null)
		   &&(((invoker.location().domainType()&Room.INDOORS)==0)
		   &&(invoker.location().domainType()!=Room.DOMAIN_OUTDOORS_SPACEPORT)
		   &&(invoker.location().domainType()!=Room.DOMAIN_OUTDOORS_CITY)))
		{
			affectableStats.setDamage(affectableStats.damage()+5);
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+10);
			affectableStats.setArmor(affectableStats.armor()-20);
		}
	}

}
