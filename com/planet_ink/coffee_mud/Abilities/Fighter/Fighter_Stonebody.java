package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Fighter_Stonebody extends StdAbility
{
	int regain=-1;
	public String ID() { return "Fighter_Stonebody"; }
	public String name(){ return "Stone Body";}
	public String displayText(){ return "";}
	public int quality(){return Ability.OK_SELF;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	public Environmental newInstance(){	return new Fighter_Stonebody();}
	public int classificationCode(){ return Ability.SKILL;}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		regain=-1;
		if(!super.okMessage(myHost,msg))
			return false;

		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if(msg.amITarget(mob)
		&&(Sense.aliveAwakeMobile(mob,true))
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&((msg.value())>0)
		&&(msg.tool()!=null)
		&&(msg.tool() instanceof Weapon)
		&&(mob.rangeToTarget()==0)
		&&((mob.fetchAbility(ID())==null)||profficiencyCheck(-85+mob.charStats().getStat(CharStats.CONSTITUTION),false)))
		{
			int regain=(int)Math.round(Util.mul(Util.div(profficiency(),100.0),2.0));
			msg.setValue(msg.value()-regain);
		}
		return true;
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);

		if((affected==null)||(!(affected instanceof MOB)))
			return;

		MOB mob=(MOB)affected;
		if((msg.amITarget(mob))
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&(regain>0))
		{
			helpProfficiency(mob);
			regain=-1;
		}
	}
}