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
	private boolean doneThisRound=false;

	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==Host.TICK_MOB)
			doneThisRound=false;
		return super.tick(ticking,tickID);
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		if(msg.amITarget(mob)
		   &&(msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
		   &&(Sense.aliveAwakeMobile(mob,true))
		   &&(msg.source().rangeToTarget()==0)
		   &&(msg.tool()!=null)
		   &&(!doneThisRound)
		   &&(msg.tool() instanceof Weapon)
		   &&(((Weapon)msg.tool()).weaponClassification()!=Weapon.CLASS_RANGED)
		   &&(((Weapon)msg.tool()).weaponClassification()!=Weapon.CLASS_THROWN))
		{
			FullMsg msg2=new FullMsg(mob,msg.source(),null,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> dodge(s) the attack by <T-NAME>!");
			if((profficiencyCheck(mob.charStats().getStat(CharStats.DEXTERITY)-90,false))
			&&(msg.source().getVictim()==mob)
			&&(mob.location().okMessage(mob,msg2)))
			{
				doneThisRound=true;
				mob.location().send(mob,msg2);
				helpProfficiency(mob);
				return false;
			}
		}
		return true;
	}
}
