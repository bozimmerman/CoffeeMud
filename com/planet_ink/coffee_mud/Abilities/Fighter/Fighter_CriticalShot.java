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
	public int classificationCode(){return Ability.SKILL;}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if(msg.amISource(mob)
		&&(Sense.aliveAwakeMobile(mob,true))
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&(msg.target()!=null)
		&&(mob.getVictim()==msg.target())
		&&(mob.rangeToTarget()>0)
		&&(msg.tool()!=null)
		&&(msg.tool() instanceof Weapon)
		&&((((Weapon)msg.tool()).weaponClassification()==Weapon.CLASS_RANGED)
			||(((Weapon)msg.tool()).weaponClassification()==Weapon.CLASS_THROWN))
		&&((mob.fetchAbility(ID())==null)||profficiencyCheck(null,(-75)+mob.charStats().getStat(CharStats.STRENGTH),false)))
		{
			double pctRecovery=(Util.div(profficiency(),100.0)*Math.random());
			int bonus=(int)Math.round(Util.mul((msg.value()),pctRecovery));
			msg.setValue(msg.value()+bonus);
			helpProfficiency(mob);
		}
		return true;
	}
}
