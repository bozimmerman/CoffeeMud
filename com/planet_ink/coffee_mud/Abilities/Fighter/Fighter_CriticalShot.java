package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Fighter_CriticalShot extends StdAbility
{
	private int oldDamage=0;
	public String ID() { return "Fighter_CriticalShot"; }
	public String name(){ return "Critical Shot";}
	public String displayText(){ return "";}
	public int quality(){return Ability.BENEFICIAL_SELF;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	public Environmental newInstance(){	return new Fighter_CriticalShot();}
	public int classificationCode(){return Ability.SKILL;}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if(!super.okAffect(myHost,affect))
			return false;

		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if(affect.amISource(mob)
		&&(Sense.aliveAwakeMobile(mob,true))
		&&(Util.bset(affect.targetCode(),Affect.MASK_HURT))
		&&(affect.target()!=null)
		&&(mob.getVictim()==affect.target())
		&&(mob.rangeToTarget()>0)
		&&(affect.tool()!=null)
		&&(affect.tool() instanceof Weapon)
		&&((((Weapon)affect.tool()).weaponClassification()==Weapon.CLASS_RANGED)
			||(((Weapon)affect.tool()).weaponClassification()==Weapon.CLASS_THROWN))
		&&((mob.fetchAbility(ID())==null)||profficiencyCheck((-75)+mob.charStats().getStat(CharStats.STRENGTH),false)))
		{
			double pctRecovery=(Util.div(profficiency(),100.0)*Math.random());
			int bonus=(int)Math.round(Util.mul((affect.targetCode()-Affect.MASK_HURT),pctRecovery));
			affect.modify(affect.source(),affect.target(),affect.tool(),affect.sourceCode(),affect.sourceMessage(),affect.targetCode()+bonus,affect.targetMessage(),affect.othersCode(),affect.othersMessage());
			helpProfficiency(mob);
		}
		return true;
	}
}
