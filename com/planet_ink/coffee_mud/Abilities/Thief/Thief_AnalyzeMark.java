package com.planet_ink.coffee_mud.Abilities.Thief;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_AnalyzeMark extends ThiefSkill
{
	public String ID() { return "Thief_AnalyzeMark"; }
	public String name(){ return "Analyze Mark";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public int quality(){return Ability.INDIFFERENT;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	public Environmental newInstance(){	return new Thief_AnalyzeMark();}
	protected boolean exemptFromArmorReq(){return true;}

	public MOB getMark(MOB mob)
	{
		Thief_Mark A=(Thief_Mark)mob.fetchEffect("Thief_Mark");
		if(A!=null)
			return A.mark;
		return null;
	}
	public int getMarkTicks(MOB mob)
	{
		Thief_Mark A=(Thief_Mark)mob.fetchEffect("Thief_Mark");
		if((A!=null)&&(A.mark!=null))
			return A.ticks;
		return -1;
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if((affected!=null)&&(affected instanceof MOB))
		{
			MOB mob=(MOB)affected;
			if(msg.amISource(mob)
			&&(msg.targetMinor()==CMMsg.TYP_EXAMINESOMETHING)
			&&(msg.target()!=null)
			&&(getMark(mob)==msg.target())
			&&(getMarkTicks(mob)>15)
			&&((mob.fetchAbility(ID())==null)||profficiencyCheck(0,false)))
			{
				if(Dice.rollPercentage()>90) helpProfficiency((MOB)affected);
				StringBuffer str=CommonMsgs.getScore((MOB)msg.target());
				if(!mob.isMonster())
					mob.session().unfilteredPrintln(str.toString());
			}
		}
		super.executeMsg(myHost,msg);
	}
}