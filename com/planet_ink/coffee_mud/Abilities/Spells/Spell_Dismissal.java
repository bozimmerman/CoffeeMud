package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Dismissal extends Spell
{
	public Spell_Dismissal()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Dismissal";

		quality=Ability.MALICIOUS;

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(25);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_Dismissal();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.DOMAIN_CONJURATION;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=false;
		affectType=affectType;
		if(target.getStartRoom()==null)
			success=profficiencyCheck(-(target.envStats().level()*3),auto);
		else
			success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType,auto?"":"<S-NAME> point(s) at <T-NAMESELF> and utter(s) a dismissive spell!");
			if(mob.location().okAffect(msg))
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