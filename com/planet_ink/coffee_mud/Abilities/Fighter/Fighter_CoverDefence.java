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
	public int quality(){return Ability.BENEFICIAL_SELF;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){	return new Fighter_CoverDefence();}
	public int classificationCode(){ return Ability.SKILL; }

	boolean lastTime=false;
	public boolean okAffect(Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		if(affect.amITarget(mob)
		   &&(affect.targetMinor()==Affect.TYP_WEAPONATTACK)
		   &&(Sense.aliveAwakeMobile(mob,true))
		   &&(affect.source().rangeToTarget()>0)
		   &&(affect.tool()!=null)
		   &&(affect.tool() instanceof Weapon)
		   &&((((Weapon)affect.tool()).weaponClassification()==Weapon.CLASS_RANGED)
			  ||(((Weapon)affect.tool()).weaponClassification()==Weapon.CLASS_THROWN)))
		{
			FullMsg msg=new FullMsg(mob,affect.source(),null,Affect.MSG_QUIETMOVEMENT,"<T-NAME> can't get a clear shot at <S-NAME>!");
			if((profficiencyCheck(mob.charStats().getStat(CharStats.DEXTERITY)-90,false))
			&&(!lastTime)
			&&(affect.source().getVictim()==mob)
			&&(mob.location().okAffect(msg)))
			{
				lastTime=true;
				mob.location().send(mob,msg);
				helpProfficiency(mob);
				return false;
			}
			else
				lastTime=false;
		}
		return true;
	}
}