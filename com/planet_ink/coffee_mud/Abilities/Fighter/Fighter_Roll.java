package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Fighter_Roll extends StdAbility
{
	int regain=-1;
	public String ID() { return "Fighter_Roll"; }
	public String name(){ return "Roll With Blows";}
	public String displayText(){ return "";}
	public int quality(){return Ability.OK_SELF;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	public Environmental newInstance(){	return new Fighter_Roll();}
	public int classificationCode(){ return Ability.SKILL;}
	public boolean doneThisRound=false;

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
		&&(!doneThisRound)
		&&((mob.fetchAbility(ID())==null)||profficiencyCheck(-85+mob.charStats().getStat(CharStats.DEXTERITY),false)))
		{
			doneThisRound=true;
			double pctRecovery=(Util.div(profficiency(),100.0)*Math.random());
			regain=(int)Math.round(Util.mul((msg.value()),pctRecovery));
			msg.setValue(msg.value()-regain);
		}
		return true;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==Host.TICK_MOB)
			doneThisRound=false;
		return super.tick(ticking,tickID);
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
			msg.addTrailerMsg(new FullMsg(mob,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> roll(s) with the hit."));
			helpProfficiency(mob);
			regain=-1;
		}
	}
}