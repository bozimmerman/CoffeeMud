package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Fighter_BodyShield extends StdAbility
{
	public String ID() { return "Fighter_BodyShield"; }
	public String name(){ return "Body Shield";}
	public String displayText(){ return "";}
	public int quality(){return Ability.OK_SELF;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	public int classificationCode(){ return Ability.SKILL;}
	public boolean doneThisRound=false;

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if(msg.amITarget(mob)
		&&(Sense.aliveAwakeMobile(mob,true))
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&(msg.source()!=mob.getVictim())
		&&(msg.source()!=mob)
		&&((msg.value())>0)
		&&(msg.tool()!=null)
		&&(msg.tool() instanceof Weapon)
		&&(mob.getVictim()!=null)
		&&(mob.getVictim().fetchEffect("Fighter_Pin")!=null)
		&&(!doneThisRound))
		{
			Ability A=mob.fetchEffect("Fighter_Pin");
			if((A!=null)&&(A.invoker()==mob))
			{
				doneThisRound=true;
				int regain=(int)Math.round(Util.mul((msg.value()),Util.div(profficiency(),100.0)));
				msg.setValue(msg.value()-regain);
				FullMsg msg2=new FullMsg(mob,mob.getVictim(),this,CMMsg.MSG_DAMAGE,"<S-NAME> use(s) <T-NAMESELF> as a body shield!");
				msg2.setValue(regain);
				msg.addTrailerMsg(msg2);
				helpProfficiency(mob);
			}
		}
		return true;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==MudHost.TICK_MOB)
			doneThisRound=false;
		return super.tick(ticking,tickID);
	}
}