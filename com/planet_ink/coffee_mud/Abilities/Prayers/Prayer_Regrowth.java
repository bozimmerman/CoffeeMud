package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.Misc.Amputation;
import java.util.*;

// ****************************************************************************
// False Realities 4.2.4
// Created by Tulath, 4/10/04.
// Prayer regrows only a single amputated limb.
// Order of importance to the limbs listed on lines ~27-37
// ****************************************************************************

public class Prayer_Regrowth extends Prayer 
{
	public String ID() { return "Prayer_Regrowth"; }
	public String name(){ return "Regrowth";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	public long flags(){return Ability.FLAG_HOLY|Ability.FLAG_HEALING;}
	public Environmental newInstance(){	return new Prayer_Regrowth();}
	protected int overrideMana(){return Integer.MAX_VALUE;}
	private static Vector limbsToRegrow = null;

	public Prayer_Regrowth() 
	{
		if(limbsToRegrow==null) 
		{
			limbsToRegrow = new Vector();
			limbsToRegrow.addElement("EYE");
			limbsToRegrow.addElement("LEG");
			limbsToRegrow.addElement("FOOT");
			limbsToRegrow.addElement("ARM");
			limbsToRegrow.addElement("HAND");
			limbsToRegrow.addElement("EAR");
			limbsToRegrow.addElement("NOSE");
			limbsToRegrow.addElement("TAIL");
			limbsToRegrow.addElement("WING");
			limbsToRegrow.addElement("ANTENEA");
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null)return false;
		if(!super.invoke(mob,commands,givenTarget,auto))
		    return false;
		boolean success=profficiencyCheck(mob,0,auto);
		if(success) 
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<T-NAME> become(s) surrounded by a bright light.":"^S<S-NAME> "+prayWord(mob)+" over <T-NAMESELF> for restorative healing.^?");
			if(mob.location().okMessage(mob,msg)) 
			{
		        mob.location().send(mob,msg);
		        Ability A=target.fetchEffect("Amputation");
		        if(A!=null)
		        {
					Amputation Amp=(Amputation)A;
					Vector missing = Amp.missingLimbNameSet();
					String LookingFor = null;
					boolean found = false;
					for(int i=0;i<limbsToRegrow.size();i++) 
					{
						LookingFor = (String)limbsToRegrow.elementAt(i);
						for(int j=0;j<missing.size();j++) 
						{
							String missLimb = (String)missing.elementAt(j);
							if(missLimb.toUpperCase().indexOf(LookingFor)>=0) 
							{
								found = true;
								break;
							}
						}
						if(found) break;
					}
					if(found) 
					{
						Amp.unamputate(target, Amp, LookingFor.toLowerCase());
					}
					target.recoverCharStats();
					target.recoverEnvStats();
					target.recoverMaxState();
		        }
				mob.location().recoverRoomStats();
			}
		}
		else
		    beneficialWordsFizzle(mob,target,"<S-NAME> "+prayWord(mob)+" over <T-NAMESELF>, but "+hisHerDiety(mob)+" does not heed.");
		// return whether it worked
		return success;
	}
}
