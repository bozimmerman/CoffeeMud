package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_Treemind extends Chant
{
	public String ID() { return "Chant_Treemind"; }
	public String name(){ return "Treemind";}
	public String displayText(){return "(Treemind)";}
	public int quality(){return Ability.BENEFICIAL_SELF;}
	int amountAbsorbed=0;
	public Environmental newInstance(){	return new Chant_Treemind();}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(canBeUninvoked())
			mob.tell("Your treemind fades.");

		super.unInvoke();

	}


	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if((affect.amITarget(mob))
		&&(!mob.amDead())
		&&(profficiencyCheck(0,false)))
		{
			boolean yep=(affect.targetMinor()==Affect.TYP_MIND);
			if((!yep)
			&&(affect.tool()!=null)
			&&(affect.tool() instanceof Ability))
			{
				Ability A=(Ability)affect.tool();
				if(((A.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_ILLUSION)
				||((A.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_ENCHANTMENT))
				   yep=true;
			}
			return !yep;
		}
		return true;
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=mob;
		if(mob.fetchAffect(this.ID())!=null)
		{
			mob.tell("You are already have the mind of a tree.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),(auto?"A treemind field envelopes <T-NAME>!":"^S<S-NAME> chant(s) for the hard protective mind of the tree.^?"));
			if(mob.location().okAffect(mob,msg))
			{
				amountAbsorbed=0;
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> chant(s), but nothing happens.");

		return success;
	}
}
