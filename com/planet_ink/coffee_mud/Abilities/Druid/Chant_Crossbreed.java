package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_Crossbreed extends Chant
{
	public String ID() { return "Chant_Crossbreed"; }
	public String name(){ return "Crossbreed";}
	public String displayText(){return "(Crossbreed)";}
	public int quality(){return Ability.OK_OTHERS;}
	public Environmental newInstance(){	return new Chant_Crossbreed();}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
			mob.tell("Your strange cross-fertility subsides.");
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		// the sex rules
		if(!(affected instanceof MOB)) return;

		MOB myChar=(MOB)affected;
		if((msg.target()!=null)&&(msg.target() instanceof MOB))
		{
			MOB mate=(MOB)msg.target();
			if((msg.amISource(myChar))
			&&(msg.tool()!=null)
			&&(msg.tool().ID().equals("Social"))
			&&(msg.tool().Name().equals("MATE <T-NAME>")
				||msg.tool().Name().equals("SEX <T-NAME>"))
			&&(myChar.charStats().getStat(CharStats.GENDER)!=mate.charStats().getStat(CharStats.GENDER))
			&&((mate.charStats().getStat(CharStats.GENDER)==((int)'M'))
			   ||(mate.charStats().getStat(CharStats.GENDER)==((int)'F')))
			&&((myChar.charStats().getStat(CharStats.GENDER)==((int)'M'))
			   ||(myChar.charStats().getStat(CharStats.GENDER)==((int)'F')))
			&&(!myChar.charStats().getMyRace().ID().equals("Human"))
			&&(!mate.charStats().getMyRace().ID().equals("Human"))
			&&(!mate.charStats().getMyRace().ID().equals(myChar.charStats().getMyRace().ID()))
			&&(myChar.location()==mate.location())
			&&(myChar.numWearingHere(Item.ON_LEGS)==0)
			&&(mate.numWearingHere(Item.ON_LEGS)==0)
			&&(myChar.numWearingHere(Item.ON_WAIST)==0)
			&&(mate.numWearingHere(Item.ON_WAIST)==0))
			{
				MOB female=myChar;
				MOB male=mate;
				if((mate.charStats().getStat(CharStats.GENDER)==((int)'F')))
				{
					female=mate;
					male=myChar;
				}
				Ability A=CMClass.getAbility("Pregnancy");
				if((A!=null)
				&&(female.fetchAbility(A.ID())==null)
				&&(female.fetchEffect(A.ID())==null))
				{
					A.invoke(male,female,true);
					unInvoke();
				}
			}
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> chant(s) to <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> seem(s) strangely fertile!");
				beneficialAffect(mob,target,(Integer.MAX_VALUE/2));
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> chant(s) to <T-NAMESELF>, but the magic fades.");


		// return whether it worked
		return success;
	}
}