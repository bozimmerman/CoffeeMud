package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import java.util.*;

public class Prayer_ProtectHealth extends Prayer
{
	public Prayer_ProtectHealth()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Protect Health";
		displayText="(Protection of Mind and Body)";

		isNeutral=true;

		baseEnvStats().setLevel(13);

		addQualifyingClass(new Cleric().ID(),baseEnvStats().level());
		addQualifyingClass(new Paladin().ID(),baseEnvStats().level()+4);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Prayer_ProtectHealth();
	}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		mob.tell("Your bodies natural defenses take over.");
	}

	public boolean okAffect(Affect affect)
	{
		if(!super.okAffect(affect))
			return false;
		if(invoker==null) return true;
		if(affected==null) return true;
		if(!(affected instanceof MOB)) return true;

		if(affect.target()==invoker)
		{
			if((affect.targetCode()==Affect.STRIKE_MIND)
			||(affect.targetCode()==Affect.STRIKE_POISON)
			||(affect.tool() instanceof Prayer_Plague)
			||(affect.tool() instanceof Thief_Poison)
			||((affect.tool() != null)&&(affect.tool().name().toUpperCase().indexOf("DISEASE")>=0))
			||((affect.tool() != null)&&(affect.tool().name().toUpperCase().indexOf("VIRUS")>=0)))
			{
				affect.source().location().show(invoker,null,Affect.VISUAL_WNOISE,"The holy field around <S-NAME> protect(s) <S-HIS-HER> body from unhealthy assaults.");
				return false;
			}

		}
		return true;
	}


	public boolean invoke(MOB mob, Vector commands)
	{
		MOB target=this.getTarget(mob,commands);
		if(target==null) return false;

		if(!super.invoke(mob,commands))
			return false;

		boolean success=profficiencyCheck(0);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,"<S-NAME> pray(s) that <T-NAME> have a healthy mind and body.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
			}
		}
		else
			return beneficialFizzle(mob,target,"<S-NAME> pray(s) for a healthy body and mind, but nothing happens.");


		// return whether it worked
		return success;
	}
}
