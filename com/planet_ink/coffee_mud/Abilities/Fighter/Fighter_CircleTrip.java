package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Fighter_CircleTrip extends StdAbility
{
	boolean doneTicking=false;
	public String ID() { return "Fighter_CircleTrip"; }
	public String name(){ return "Circle Trip";}
	public String displayText(){ return "(Tripped)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	private static final String[] triggerStrings = {"CIRCLETRIP","CTRIP"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.SKILL;}
	public Environmental newInstance(){	return new Fighter_CircleTrip();}

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
		if(!mob.amDead())
		{
			if(mob.location()!=null)
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
		if((!Sense.aliveAwakeMobile(mob,true)||(Sense.isSitting(mob))))
		{
			mob.tell("You need to stand up!");
			return false;
		}
		if(mob.isInCombat()&&(mob.rangeToTarget()>0))
		{
			mob.tell("You are too far away to circle trip!");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		Hashtable h=ExternalPlay.properTargets(this,mob,auto);
		if(h==null)
		{
			mob.tell("There doesn't appear to be anyone here worth tripping.");
			return false;
		}

		boolean success=true;
		FullMsg msg=new FullMsg(mob,null,this,Affect.MSK_MALICIOUS_MOVE|Affect.TYP_JUSTICE|(auto?Affect.MASK_GENERAL:0),auto?"":"^F<S-NAME> slide(s) into a circle trip!^?");
		if(mob.location().okAffect(mob,msg))
		{
			mob.location().send(mob,msg);
			for(Enumeration e=h.elements();e.hasMoreElements();)
			{
				MOB target=(MOB)e.nextElement();

				if((Sense.isSitting(target)||Sense.isSleeping(target)))
				{
					mob.tell(target.name()+" is already on the floor!");
					return false;
				}

				if(target.riding()!=null)
				{
					mob.tell("You can't trip someone "+target.riding().stateString(target)+" "+target.riding().name()+"!");
					return false;
				}
				if(Sense.isInFlight(target))
				{
					mob.tell(target.name()+" is flying and can't be tripped!");
					return false;
				}

				int levelDiff=target.envStats().level()-mob.envStats().level();
				if(levelDiff>0)
					levelDiff=levelDiff*5;
				else
					levelDiff=0;
				int adjustment=(-levelDiff)+(-(35+((int)Math.round((new Integer(target.charStats().getStat(CharStats.DEXTERITY)).doubleValue()-9.0)*3.0))));
				success=profficiencyCheck(adjustment,auto);
				success=success&&(target.charStats().getMyRace().bodyMask()[Race.BODY_LEG]>0);
				if(success)
				{
					msg=new FullMsg(mob,target,this,Affect.MSK_MALICIOUS_MOVE|Affect.TYP_JUSTICE|(auto?Affect.MASK_GENERAL:0),auto?"<T-NAME> trip(s)!":"^F<S-NAME> trip(s) <T-NAMESELF>!^?");
					if(mob.location().okAffect(mob,msg))
					{
						mob.location().send(mob,msg);
						maliciousAffect(mob,target,2,-1);
						target.location().show(target,null,Affect.MSG_OK_ACTION,"<S-NAME> hit(s) the floor!");
					}
				}
				else
					return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to circle trip <T-NAMESELF>, but fail(s).");
			}
		}
		else
			success=false;
		return success;
	}
}
