package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Fighter_CoverDefence extends StdAbility
{
	public int hits=0;
	public String ID() { return "Fighter_CoverDefence"; }
	public String name(){ return "Cover Defence";}
	public String displayText(){return "";};
	public int quality(){return Ability.BENEFICIAL_SELF;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	public Environmental newInstance(){	return new Fighter_CoverDefence();}
	public int classificationCode(){ return Ability.SKILL; }

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		if(affect.amITarget(mob)
		   &&(affect.targetMinor()==Affect.TYP_WEAPONATTACK)
		   &&(Sense.aliveAwakeMobile(mob,true))
		   &&(affect.source().rangeToTarget()>0)
		   &&(mob.envStats().height()<84)
		   &&(affect.tool()!=null)
		   &&(affect.tool() instanceof Weapon)
		   &&((((Weapon)affect.tool()).weaponClassification()==Weapon.CLASS_RANGED)
			  ||(((Weapon)affect.tool()).weaponClassification()==Weapon.CLASS_THROWN)))
		{
			FullMsg msg=new FullMsg(affect.source(),mob,null,Affect.MSG_QUIETMOVEMENT,"<T-NAME> take(s) cover from <S-YOUPOSS> attack!");
			if((profficiencyCheck(mob.charStats().getStat(CharStats.DEXTERITY)-90,false))
			&&(affect.source().getVictim()==mob)
			&&(mob.location().okAffect(mob,msg)))
			{
				mob.location().send(mob,msg);
				helpProfficiency(mob);
				return false;
			}
		}
		return true;
	}
}