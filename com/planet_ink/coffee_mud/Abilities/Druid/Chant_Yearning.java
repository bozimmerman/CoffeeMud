package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_Yearning extends Chant
{
	public String ID() { return "Chant_Yearning"; }
	public String name(){ return "Yearning";}
	public String displayText(){return "(Sexual Yearnings)";}
	public int quality(){return Ability.MALICIOUS;}
	public Environmental newInstance(){	return new Chant_Yearning();}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(affected==null) return;
		int wis=affectableStats.getStat(CharStats.WISDOM);
		wis=wis-5;
		if(wis<1) wis=1;
		affectableStats.setStat(CharStats.WISDOM,wis);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
			mob.tell("Your yearning subsides.");
	}

	public void affect(Environmental myHost, Affect affect)
	{
		super.affect(myHost,affect);
		// the sex rules
		if(!(affected instanceof MOB)) return;
		MOB myChar=(MOB)affected;

		if((affect.amISource(myChar))
		&&(affect.target()!=null)
		&&(affect.target() instanceof MOB)
		&&(affect.tool()!=null)
		&&(affect.tool().ID().equals("Social"))
		&&(affect.tool().Name().equals("MATE <T-NAME>")
			||affect.tool().Name().equals("SEX <T-NAME>"))
		&&(myChar.location()==((MOB)affect.target()).location())
		&&(!myChar.amWearingSomethingHere(Item.ON_LEGS))
		&&(!((MOB)affect.target()).amWearingSomethingHere(Item.ON_LEGS))
		&&(!myChar.amWearingSomethingHere(Item.ON_WAIST))
		&&(!((MOB)affect.target()).amWearingSomethingHere(Item.ON_WAIST)))
			unInvoke();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto)|Affect.MASK_MALICIOUS,auto?"":"^S<S-NAME> chant(s) to <T-NAMESELF>.^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
				{
					mob.location().show(target,null,Affect.MSG_OK_VISUAL,"<S-NAME> seem(s) to yearn for something!");
					maliciousAffect(mob,target,(Integer.MAX_VALUE/2),-1);
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> chant(s) to <T-NAMESELF>, but the magic fades.");


		// return whether it worked
		return success;
	}
}