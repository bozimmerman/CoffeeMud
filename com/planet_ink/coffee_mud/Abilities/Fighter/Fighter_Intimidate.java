package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Fighter_Intimidate extends StdAbility
{
	public String ID() { return "Fighter_Intimidate"; }
	public String name(){ return "Intimidation";}
	public String displayText(){ return "";}
	public int quality(){return Ability.OK_SELF;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	public Environmental newInstance(){	return new Fighter_Intimidate();}
	public int classificationCode(){ return Ability.SKILL;}
	public Room lastRoom=null;

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(((msg.targetCode()&CMMsg.MASK_MALICIOUS)>0)
		&&((msg.amITarget(affected))))
		{
			MOB target=(MOB)msg.target();
			MOB mob=(MOB)affected;
			int levelDiff=((msg.source().envStats().level()-target.envStats().level())*10);
			// 1 level off = -10
			// 10 levels off = -100
			if((!target.isInCombat())
			&&(msg.source().getVictim()!=target)
			&&(levelDiff<0)
			&&((mob.fetchAbility(ID())==null)||profficiencyCheck((-(100+levelDiff))+(target.charStats().getStat(CharStats.CHARISMA)*2),false)))
			{
				msg.source().tell("You are too intimidated by "+target.name());
				if(msg.source().location()!=lastRoom)
				{
					lastRoom=msg.source().location();
					helpProfficiency(target);
				}
				if(target.getVictim()==msg.source())
				{
					target.makePeace();
					target.setVictim(null);
				}
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

}