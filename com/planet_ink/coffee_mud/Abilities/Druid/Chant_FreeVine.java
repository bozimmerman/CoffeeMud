package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_FreeVine extends Chant
{
	public String ID() { return "Chant_FreeVine"; }
	public String name(){ return "Free Vine";}
	public String displayText(){return "";}
	public int quality(){return Ability.OK_OTHERS;}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public Environmental newInstance(){	return new Chant_FreeVine();}

	public void affect(Environmental myHost, Affect affect)
	{
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(affect.amISource((MOB)affected)))
		{
			if(((affect.targetMinor()==Affect.TYP_LEAVE)
			||(affect.sourceMinor()==Affect.TYP_ADVANCE)
			||(affect.sourceMinor()==Affect.TYP_RETREAT)))
				unInvoke();
		}
		super.affect(myHost,affect);
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		
		if(!target.charStats().getMyRace().ID().equals("Vine"))
		{
			mob.tell(target.name()+" can not be uprooted.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto)|Affect.MASK_MALICIOUS,auto?"":"^S<S-NAME> chant(s)freely to <T-NAMESELF>.^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
				{
					mob.location().show(target,null,Affect.MSG_OK_VISUAL,"<S-NAME> pull(s) <S-HIS-HER> roots up!");
					beneficialAffect(mob,target,0);
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> chant(s) freely to <T-NAMESELF>, but the magic fades");


		// return whether it worked
		return success;
	}
}