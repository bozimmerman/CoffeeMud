package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Fighter_ImprovedShieldDefence extends StdAbility
{
	public String ID() { return "Fighter_ImprovedShieldDefence"; }
	public String name(){ return "Improved Shield Defence";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.BENEFICIAL_SELF;}
	public int classificationCode(){return Ability.SKILL;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	public Environmental newInstance(){	return new Fighter_ImprovedShieldDefence();}
	private boolean gettingBonus=false;

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		gettingBonus=false;
		if((affected==null)||(!(affected instanceof MOB))) return;
		Item w=((MOB)affected).fetchFirstWornItem(Item.HELD);
		if((w==null)||(!(w instanceof Shield))) return;
		gettingBonus=true;
		affectableStats.setArmor(affectableStats.armor()+((int)Math.round(Util.mul(w.envStats().armor(),(Util.div(profficiency(),100.0))))));
	}
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);

		if((affected==null)||(!(affected instanceof MOB)))
			return;

		MOB mob=(MOB)affected;

		if((msg.amITarget(mob))
		&&(gettingBonus)
		&&(msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
		&&(mob.isInCombat())
		&&(Dice.rollPercentage()==1)
		&&(!mob.amDead()))
			helpProfficiency(mob);
	}
	
}