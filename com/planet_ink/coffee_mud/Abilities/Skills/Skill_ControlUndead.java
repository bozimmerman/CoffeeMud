package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Skill_ControlUndead extends StdAbility
{
	public String ID() { return "Skill_ControlUndead"; }
	public String name(){ return "Control Undead";}
	public String displayText(){ return "(Controlled)";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	private static final String[] triggerStrings = {"CONTROL"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.SKILL;}
	public Environmental newInstance(){	return new Skill_ControlUndead();}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected!=null)
		&&(affected instanceof MOB)
		&&((msg.targetCode()&CMMsg.MASK_MALICIOUS)>0)
		&&((msg.amISource((MOB)affected)))
		&&(msg.target()==invoker()))
		{
			if((!invoker().isInCombat())&&(msg.source().getVictim()!=invoker()))
			{
				msg.source().tell("You're too submissive towards "+invoker().name());
				if(invoker().getVictim()==msg.source())
				{
					invoker().makePeace();
					invoker().setVictim(null);
				}
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if((target.baseCharStats().getMyRace()==null)
		||(!target.baseCharStats().getMyRace().racialCategory().equals("Undead")))
		{
			mob.tell(auto?"Only the undead can be controlled.":"You can only control the undead.");
			return false;
		}

		if(mob.getAlignment()>650)
		{
			mob.tell("Only the wicked may control the undead.");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck((mob.envStats().level()-target.envStats().level())*30,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSG_CAST_ATTACK_SOMANTIC_SPELL|(auto?CMMsg.MASK_GENERAL:0),auto?"<T-NAME> seem(s) controlled.":"^S<S-NAME> control(s) <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					if((mob.envStats().level()-target.envStats().level())>6)
					{
						mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> is now controlled.");
						target.makePeace();
						CommonMsgs.follow(target,mob,false);
						MUDFight.makePeaceInGroup(mob);
						invoker=mob;
						if(target.amFollowing()!=mob)
							mob.tell(target.name()+" seems unwilling to follow you.");
					}
					else
					{
						mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> seem(s) submissive!");
						target.makePeace();
						beneficialAffect(mob,target,5);
					}

				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to control <T-NAMESELF>, but fail(s).");


		// return whether it worked
		return success;
	}
}