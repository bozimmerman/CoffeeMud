package com.planet_ink.coffee_mud.Abilities.Misc;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import java.util.*;

public class DiseaseCure extends StdAbility
{
	public String ID() { return "DiseaseCure"; }
	public String name(){ return "A Cure";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	public String displayText(){ return "";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int classificationCode(){return Ability.SKILL;}

	public Vector returnOffensiveAffects(Environmental fromMe)
	{
		Vector offenders=new Vector();

		for(int a=0;a<fromMe.numEffects();a++)
		{
			Ability A=fromMe.fetchEffect(a);
			if((A!=null)
			&&(A instanceof DiseaseAffect)
			&&((text().length()==0)||(A.name().toUpperCase().indexOf(text().toUpperCase())>=0)||(A.ID().toUpperCase().indexOf(text().toUpperCase())>=0)))
				offenders.addElement(A);
		}
		return offenders;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);
		Vector offensiveAffects=returnOffensiveAffects(target);

		if((success)&&(offensiveAffects.size()>0))
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSG_OK_VISUAL,null);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				int old=target.numEffects();
				for(int a=offensiveAffects.size()-1;a>=0;a--)
					((Ability)offensiveAffects.elementAt(a)).unInvoke();
				if((old>target.numEffects())&&(target.location()!=null))
					target.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<T-NAME> feel(s) much better.");
			}
		}

		// return whether it worked
		return success;
	}
}
