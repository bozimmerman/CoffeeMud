package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_BestowName extends Chant
{
	public String ID() { return "Chant_BestowName"; }
	public String name(){ return "Bestow Name";}
	public int quality(){ return OK_OTHERS;}
	public String displayText(){return "";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public Environmental newInstance(){	return new Chant_BestowName();}
	public String newName="";

	public void affectEnvStats(Environmental affected, EnvStats affectedStats)
	{
		super.affectEnvStats(affected,affectedStats);
		if((affected instanceof MOB)
		&&(((MOB)affected).amFollowing()==null)
		&&(Sense.isInTheGame(affected)))
			affected.delEffect(affected.fetchEffect(ID()));
		else							   
		if((newName!=null)&&(newName.length()>0))
			affectedStats.setName(newName);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		if((!Sense.isAnimalIntelligence(target))||(!target.isMonster())||(mob.getGroupMembers(new HashSet()).contains(target)))
		{
			mob.tell("This chant only works on non-player animals in your group.");
			return false;
		}
		String myName=Util.combine(commands,0).trim();
		if(myName.length()==0)
		{
			mob.tell("You must specify a name.");
			return false;
		}
		if(myName.indexOf(" ")>=0)
		{
			mob.tell("Your name may not contain a space.");
			return false;
		}
		
		if((target.name().toUpperCase().startsWith("A "))
		||(target.name().toUpperCase().startsWith("AN "))
		||(target.name().toUpperCase().startsWith("SOME ")))
			myName=target.name()+" named "+myName;
		else
		if(target.name().indexOf(" ")>=0)
			myName=myName+", "+target.name();
			

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> chant(s) to <T-NAMESELF>, bestowing the name '"+myName+"'.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Chant_BestowName A=(Chant_BestowName)copyOf();
				A.newName=myName;
				target.addNonUninvokableEffect(A);
				mob.location().recoverRoomStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> chant(s) to <T-NAMESELF>, but nothing happens.");


		// return whether it worked
		return success;
	}
}