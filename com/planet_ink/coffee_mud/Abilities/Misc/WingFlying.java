package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class WingFlying extends StdAbility
{
	public String ID() { return "WingFlying"; }
	public String name(){ return "Winged Flight";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	public Environmental newInstance(){	return new WingFlying();}
	public boolean putInCommandlist(){return false;}
	private static final String[] triggerStrings = {"FLAP"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.SKILL;}
	private boolean flying=true;

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected==null) return;
		if(!(affected instanceof MOB)) return;

		if(flying)
			affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_FLYING);
		else
			affectableStats.setDisposition(Util.unsetb(affectableStats.disposition(),EnvStats.IS_FLYING));
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=mob;
		if(target==null) return false;
		if(target.charStats().getBodyPart(Race.BODY_WING)<=0)
		{
			mob.tell("You can't flap without wings.");
			return false;
		}
		
		boolean wasFlying=Sense.isFlying(target);
		Ability A=target.fetchAffect(ID());
		if(A!=null) A.unInvoke();
		target.recoverEnvStats();
		String str="";
		if(wasFlying)
		{
			flying=false;
			str="<S-NAME> stop(s) flapping <S-HIS-HER> wings.";
		}
		else
		{
			flying=true;
			str="<S-NAME> start(s) flapping <S-HIS-HER> wings.";
		}
			

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,Affect.MSG_NOISYMOVEMENT,str);
			if(target.location().okAffect(target,msg))
			{
				target.location().send(target,msg);
				beneficialAffect(mob,target,9999);
				A=target.fetchAffect(ID());
				if(A!=null) A.makeLongLasting();
			}
		}
		else
			return beneficialVisualFizzle(mob,target,"<T-NAME> fumble(s) trying to use <T-HIS-HER> wings.");


		// return whether it worked
		return success;
	}
}