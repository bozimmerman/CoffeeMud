package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Fighter_CounterAttack extends StdAbility
{
	public String ID() { return "Fighter_CounterAttack"; }
	public String name(){ return "Counter-Attack";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.BENEFICIAL_SELF;}
	public int classificationCode(){return Ability.SKILL;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}

	public Environmental newInstance(){	return new Fighter_CounterAttack();}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		if(msg.amISource(mob)
		&&(Sense.aliveAwakeMobile(mob,true))
		&&(msg.target() instanceof MOB)
		&&(msg.tool() instanceof Ability)
		&&((mob.fetchAbility(ID())==null)||profficiencyCheck(mob,0,false))
		&&(mob.rangeToTarget()==0))
		{
			if(msg.tool().ID().equals("Skill_Parry"))
			{
				FullMsg msg2=new FullMsg(mob,(MOB)msg.target(),this,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> position(s) <S-HIM-HERSELF> for a counterattack!");
				msg.addTrailerMsg(msg2);
			}
			else
			if(msg.tool().ID().equals(ID()))
				MUDFight.postAttack(mob,(MOB)msg.target(),mob.fetchWieldedItem());
		}
		return true;
	}
}
