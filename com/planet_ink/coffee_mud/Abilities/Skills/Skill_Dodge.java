package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Skill_Dodge extends StdAbility
{
	public String ID() { return "Skill_Dodge"; }
	public String name(){ return "Dodge";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.BENEFICIAL_SELF;}
	public int classificationCode(){return Ability.SKILL;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	public Environmental newInstance(){	return new Skill_Dodge();}

	boolean lastTime=false;
	public boolean okAffect(Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		if(affect.amITarget(mob)
		   &&(affect.targetMinor()==Affect.TYP_WEAPONATTACK)
		   &&(Sense.aliveAwakeMobile(mob,true))
		   &&(affect.source().rangeToTarget()==0)
		   &&(affect.tool()!=null)
		   &&(affect.tool() instanceof Weapon)
		   &&(((Weapon)affect.tool()).weaponClassification()!=Weapon.CLASS_RANGED)
		   &&(((Weapon)affect.tool()).weaponClassification()!=Weapon.CLASS_THROWN))
		{
			FullMsg msg=new FullMsg(mob,affect.source(),null,Affect.MSG_QUIETMOVEMENT,"<S-NAME> dodge(s) the attack by <T-NAME>!");
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
