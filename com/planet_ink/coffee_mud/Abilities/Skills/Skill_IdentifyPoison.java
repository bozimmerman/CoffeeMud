package com.planet_ink.coffee_mud.Abilities.Skills;

import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Skill_IdentifyPoison extends StdAbility
{
	public String ID() { return "Skill_IdentifyPoison"; }
	public String name(){ return "Identify Poison";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_ITEMS;}
	public int quality(){return Ability.INDIFFERENT;}
	private static final String[] triggerStrings = {"IDPOISON","IDENTIFYPOISON"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.SKILL;}
	public Environmental newInstance(){	return new Skill_IdentifyPoison();	}

	public Vector returnOffensiveAffects(Environmental fromMe)
	{
		Vector offenders=new Vector();

		for(int a=0;a<fromMe.numAffects();a++)
		{
			Ability A=fromMe.fetchAffect(a);
			if((A!=null)&&(A.classificationCode()==Ability.POISON))
				offenders.addElement(A);
		}
		return offenders;
	}

	public boolean canBeLearnedBy(MOB teacher, MOB student)
	{
		if(!super.canBeLearnedBy(teacher,student))
			return false;
		if(student==null) return true;
		if(student.fetchAbility("Apothecary")==null)
		{
			teacher.tell(student.displayName()+" has not yet learned to be an apothecary.");
			student.tell("You need to learn apothecary before you can learn "+displayName()+".");
			return false;
		}

		return true;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Item target=getTarget(mob,mob.location(),givenTarget,commands,Item.WORN_REQ_UNWORNONLY);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);
		Vector offensiveAffects=returnOffensiveAffects(target);

		if((success)&&((offensiveAffects.size()>0)
					   ||((target instanceof Drink)&&(((Drink)target).liquidHeld()==EnvResource.RESOURCE_POISON))))
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,Affect.MSG_DELICATE_HANDS_ACT|(auto?Affect.MASK_GENERAL:0),auto?"":"^S<S-NAME> carefully sniff(s) and taste(s) <T-NAME>.^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				StringBuffer buf=new StringBuffer(target.displayName()+" contains: ");
				if(offensiveAffects.size()==0)
					buf.append("weak impurities, ");
				else
				for(int i=0;i<offensiveAffects.size();i++)
					buf.append(((Ability)offensiveAffects.elementAt(i)).displayName()+", ");
				mob.tell(buf.toString().substring(0,buf.length()-2));
			}
		}
		else
			beneficialWordsFizzle(mob,target,auto?"":"<S-NAME> sniff(s) and taste(s) <T-NAME>, but receives no insight.");


		// return whether it worked
		return success;
	}
}