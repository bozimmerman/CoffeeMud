package com.planet_ink.coffee_mud.Abilities.Druid;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_SummonFear extends Chant
{
	public String ID() { return "Chant_SummonFear"; }
	public String name(){ return "Summon Fear";}
	public String displayText(){return "(Afraid)";}
	public int quality(){return Ability.MALICIOUS;}
	public int maxRange(){return 1;}
	public Environmental newInstance(){	return new Chant_SummonFear();}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Hashtable h=properTargets(mob,givenTarget,auto);
		if(h==null)
		{
			mob.tell("There doesn't appear to be anyone here worth scaring.");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			for(Enumeration f=h.elements();f.hasMoreElements();)
			{
				MOB target=(MOB)f.nextElement();

				// it worked, so build a copy of this ability,
				// and add it to the affects list of the
				// affected MOB.  Then tell everyone else
				// what happened.
				FullMsg msg=new FullMsg(mob,target,this,affectType(auto),"^S<S-NAME> frighten(s) <T-NAMESELF> with <S-HIS-HER> chant.^?");
				FullMsg msg2=new FullMsg(mob,target,this,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_MIND|(auto?CMMsg.MASK_GENERAL:0),null);
				if((mob.location().okMessage(mob,msg))&&((mob.location().okMessage(mob,msg2))))
				{
					mob.location().send(mob,msg);
					if(!msg.wasModified())
					{
						mob.location().send(mob,msg2);
						if(!msg2.wasModified())
						{
							invoker=mob;
							ExternalPlay.flee(target,"");
						}
					}
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> chant(s) in a frightening way, but the magic fades.");


		// return whether it worked
		return success;
	}
}