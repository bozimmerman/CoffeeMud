package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Dismissal extends Spell
{
	public String ID() { return "Spell_Dismissal"; }
	public String name(){return "Dismissal";}
	public int quality(){return MALICIOUS;};
	public Environmental newInstance(){	return new Spell_Dismissal();}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_CONJURATION;}
	public long flags(){return Ability.FLAG_MOVING|Ability.FLAG_TRANSPORTING;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=false;
		if(target.getStartRoom()==null)
			success=profficiencyCheck(-(target.envStats().level()*3),auto);
		else
			success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> point(s) at <T-NAMESELF> and utter(s) a dismissive spell!^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
				{
					if(target.getStartRoom()==null)
						target.destroy();
					else
					{
						mob.location().show(mob,target,Affect.MSG_OK_ACTION,"<T-NAME> vanish(es) in dismissal!");
						target.getStartRoom().show(target,null,Affect.MSG_OK_VISUAL,"<S-NAME> appears!");
						target.getStartRoom().bringMobHere(target,false);
					}
					mob.location().recoverRoomStats();
				}

			}

		}
		else
			maliciousFizzle(mob,target,"<S-NAME> point(s) at <T-NAMESELF> and utter(s) a dismissive but fizzled spell!");


		// return whether it worked
		return success;
	}
}