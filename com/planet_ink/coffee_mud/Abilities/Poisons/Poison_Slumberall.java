package com.planet_ink.coffee_mud.Abilities.Poisons;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Poison_Slumberall extends Poison
{
	public String ID() { return "Poison_Slumberall"; }
	public String name(){ return "Slumberall";}
	private static final String[] triggerStrings = {"POISONSLEEP"};
	public String[] triggerStrings(){return triggerStrings;}
	public Environmental newInstance(){	return new Poison_Slumberall();}

	protected int POISON_TICKS(){return 50;} // 0 means no adjustment!
	protected int POISON_DELAY(){return 1;}
	protected String POISON_DONE(){return "You don't feel so drowsy anymore.";}
	protected String POISON_START(){return null;}
	protected String POISON_AFFECT(){return "";}
	protected String POISON_CAST(){return "^F<S-NAME> poison(s) <T-NAMESELF>!^?";}
	protected String POISON_FAIL(){return "<S-NAME> attempt(s) to poison <T-NAMESELF>, but fail(s).";}
	protected int POISON_DAMAGE(){return 0;}
	protected boolean fallenYet=false;

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		if(affected instanceof MOB)
			affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_SLEEPING);
	}

	public void unInvoke()
	{
		if((affected!=null)&&(affected instanceof MOB))
		{
			MOB mob=(MOB)affected;
			ExternalPlay.standIfNecessary(mob);
		}
		super.unInvoke();
	}
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		// when this spell is on a MOBs Affected list,
		// it should consistantly prevent the mob
		// from trying to do ANYTHING except sleep
		if(msg.amITarget(mob)&&(fallenYet)&&(msg.targetMinor()==CMMsg.TYP_DAMAGE))
			unInvoke();
		else
		if((msg.amISource(mob))
		&&(!Util.bset(msg.sourceMajor(),CMMsg.MASK_GENERAL))
		&&(msg.sourceMajor()>0))
		{
			mob.tell("You are way too drowsy.");
			return false;
		}
		return super.okMessage(myHost,msg);
	}


	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;

		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if(mob==null) return false;
		if((!fallenYet)&&(mob.location()!=null))
		{
			fallenYet=true;
			mob.location().show(mob,null,CMMsg.MSG_SLEEP,"<S-NAME> fall(s) asleep!");
			mob.recoverEnvStats();
		}
		return true;
	}
}
