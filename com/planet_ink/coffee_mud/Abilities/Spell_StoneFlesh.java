package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.MOBS.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import java.util.*;

public class Spell_StoneFlesh extends Spell
	implements EvocationDevotion
{
	public Spell_StoneFlesh()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Stone Flesh";

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(19);

		addQualifyingClass(new Mage().ID(),19);
		addQualifyingClass(new Ranger().ID(),baseEnvStats().level()+4);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_StoneFlesh();
	}

	public boolean invoke(MOB mob, Vector commands)
	{

		if(commands.size()<1)
		{
			mob.tell("You must specify who or what to cast this on.");
			return false;
		}
		String castThisOn=CommandProcessor.combine(commands,0);
		Environmental target=mob.location().fetchFromRoom(null,castThisOn);

		if((target==null)||((target!=null)&&(!Sense.canBeSeenBy(target,mob))))
		{
			mob.tell("You don't see '"+castThisOn+"' here.");
			return false;
		}


		Ability revokeThis=null;
		for(int a=0;a<target.numAffects();a++)
		{
			Ability A=(Ability)target.fetchAffect(a);
			if((A.canBeUninvoked())&&(A instanceof Spell_FleshStone))
			{
				if((A.invoker()!=null)
				&&((A.invoker()==mob)
				||(A.invoker().envStats().level()<mob.envStats().level())))
						revokeThis=A;
			}
		}

		if(revokeThis==null)
		{
			mob.tell(mob,target,"<T-NAME> can not be affected by this spell.");
			return false;
		}


		if(!super.invoke(mob,commands))
			return false;

		boolean success=profficiencyCheck(0);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,Affect.VISUAL_WNOISE,"<S-NAME> dispel(s) "+revokeThis.name()+" from <T-NAME>.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				revokeThis.unInvoke();
			}
		}
		else
			beneficialFizzle(mob,target,"<S-NAME> attempt(s) to dispel "+revokeThis.name()+" from <T-NAME>, but flub(s) it.");


		// return whether it worked
		return success;
	}
}