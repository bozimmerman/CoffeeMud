package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Fighter_BodyFlip extends StdAbility
{
	boolean doneTicking=false;
	public String ID() { return "Fighter_BodyFlip"; }
	public String name(){ return "Body Flip";}
	public String displayText(){ return "(Flipped and stunned)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	private static final String[] triggerStrings = {"BODYFLIP"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.SKILL;}
	public Environmental newInstance(){	return new Fighter_BodyFlip();}
	public int usageType(){return USAGE_MOVEMENT;}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(!doneTicking)
			affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_SITTING);
	}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if((doneTicking)&&(affect.amISource(mob)))
			unInvoke();
		else
		if(affect.amISource(mob)&&(affect.sourceMinor()==Affect.TYP_STAND))
			return false;
		return true;
	}

	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(canBeUninvoked())
			doneTicking=true;
		super.unInvoke();
		if(canBeUninvoked())
		{
			if((mob.location()!=null)&&(!mob.amDead()))
			{
				FullMsg msg=new FullMsg(mob,null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> regain(s) <S-HIS-HER> feet.");
				if(mob.location().okAffect(mob,msg))
				{
					mob.location().send(mob,msg);
					ExternalPlay.standIfNecessary(mob);
				}
			}
			else
				mob.tell("You regain your feet.");
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if((Sense.isSitting(target)||Sense.isSleeping(target)))
		{
			mob.tell(target.name()+" is already on the floor!");
			return false;
		}

		if((!Sense.aliveAwakeMobile(mob,true)||(Sense.isSitting(mob))))
		{
			mob.tell("You need to stand up!");
			return false;
		}
		if(mob.isInCombat()&&(mob.rangeToTarget()>0))
		{
			mob.tell("You are too far away to do a body flip!");
			return false;
		}
		if(target.riding()!=null)
		{
			mob.tell("You can't flip someone "+target.riding().stateString(target)+" "+target.riding().name()+"!");
			return false;
		}
		if(Sense.isInFlight(target))
		{
			mob.tell(target.name()+" is flying and can't be flipped over!");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		int levelDiff=target.envStats().level()-mob.envStats().level();
		if(levelDiff>0)
			levelDiff=levelDiff*5;
		else
			levelDiff=0;
		int adjustment=(-levelDiff)+(-(35+((int)Math.round((new Integer(target.charStats().getStat(CharStats.DEXTERITY)).doubleValue()-9.0)*3.0))));
		boolean success=profficiencyCheck(adjustment,auto);
		success=success&&(target.charStats().getBodyPart(Race.BODY_LEG)>0);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,Affect.MSK_MALICIOUS_MOVE|Affect.TYP_JUSTICE|(auto?Affect.MASK_GENERAL:0),auto?"<T-NAME> flip(s) over!":"^F<S-NAME> flip(s) <T-NAMESELF> over!^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				maliciousAffect(mob,target,2,-1);
				target.location().show(target,null,Affect.MSG_OK_ACTION,"<S-NAME> hit(s) the floor!");
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to flip <T-NAMESELF> over, but fail(s).");
		return success;
	}
}
