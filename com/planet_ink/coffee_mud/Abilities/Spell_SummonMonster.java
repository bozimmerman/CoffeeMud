package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import java.util.*;

public class Spell_SummonMonster extends Spell
	implements SummoningDevotion
{
	public Spell_SummonMonster()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Monster Summoning";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Monster Summoning)";


		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(7);

		addQualifyingClass(new Mage().ID(),7);
		addQualifyingClass(new Ranger().ID(),baseEnvStats().level()+4);

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_SummonMonster();
	}

	public boolean invoke(MOB mob, Vector commands)
	{
		if(!super.invoke(mob,commands))
			return false;

		boolean success=profficiencyCheck(0);

		if(success)
		{
			MOB target = determineMonster(mob, mob.envStats().level());
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,"<S-NAME> chant(s) and summon(s) help from the Java Plain.");
			if(mob.location().okAffect(msg))
			{
				target.setFollowing(mob);
				mob.location().send(mob,msg);
			}
		}
		else
			return beneficialFizzle(mob,null,"<S-NAME> chant(s) and call(s), but chokes on the words.");

		// return whether it worked
		return success;
	}
	public MOB determineMonster(MOB caster, int level)
	{

		String mobID = "Dog";

		if (level >= 5)
		{
			mobID = "Orc";
		}

		if (level >= 10)
		{
			mobID = "Tiger";
		}

		if (level >= 15)
		{
			mobID = "Troll";
		}


		MOB newMOB=(MOB)MUD.getMOB(mobID);

		newMOB=(MOB)newMOB.newInstance();
		newMOB.setStartRoom(caster.location());
		newMOB.setLocation(caster.location());
		newMOB.recoverCharStats();
		newMOB.recoverEnvStats();
		newMOB.recoverMaxState();
		newMOB.bringToLife(caster.location());

		return(newMOB);


	}
}
