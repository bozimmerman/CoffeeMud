package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_MassCureDisease extends Prayer
{
	public String ID() { return "Prayer_MassCureDisease"; }
	public String name(){ return "Mass Cure Disease";}
	public int quality(){ return OK_OTHERS;}
	public long flags(){return Ability.FLAG_HOLY;}
	public Environmental newInstance(){	return new Prayer_MassCureDisease();}

	public static Vector returnOffensiveAffects(Environmental fromMe)
	{
		Vector offenders=new Vector();

		for(int a=0;a<fromMe.numEffects();a++)
		{
			Ability A=fromMe.fetchEffect(a);
			if((A!=null)&&(A instanceof DiseaseAffect))
				offenders.addElement(A);
		}
		return offenders;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,mob.location(),this,affectType(auto),auto?"A healing glow surrounds this place.":"^S<S-NAME> "+prayWord(mob)+" to cure disease here.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				for(Enumeration e=mob.location().getArea().getMap();e.hasMoreElements();)
				{
					Room R=(Room)e.nextElement();
					for(int m=0;m<R.numInhabitants();m++)
					{
						MOB target=(MOB)R.fetchInhabitant(m);
						if(target!=null)
						{
							Vector offensiveAffects=returnOffensiveAffects(target);
							if(offensiveAffects.size()>0)
							{
								int old=target.numEffects();
								for(int a=offensiveAffects.size()-1;a>=0;a--)
									((Ability)offensiveAffects.elementAt(a)).unInvoke();
								if(old>target.numEffects())
									target.tell("You feel much better!");
							}
						}
					}
				}
			}
		}
		else
			beneficialWordsFizzle(mob,mob.location(),auto?"":"<S-NAME> "+prayWord(mob)+", but nothing happens.");


		// return whether it worked
		return success;
	}
}