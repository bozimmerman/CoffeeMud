package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import java.util.*;

public class Prayer_BlessItem extends Prayer
{
	public Prayer_BlessItem()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Bless Item";
		displayText="(Blessed)";

		baseEnvStats().setLevel(24);

		addQualifyingClass(new Cleric().ID(),baseEnvStats().level());
		addQualifyingClass(new Paladin().ID(),baseEnvStats().level()+4);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Prayer_BlessItem();
	}

	public boolean invoke(MOB mob, Vector commands)
	{
		if(mob.curState().getMana()<mob.maxState().getMana())
		{
			mob.tell(mob,null,"You must have all of your mana to pray for this.");
			return false;
		}

		if(mob.getExperience()<(mob.envStats().level()-1)*1000)
		{
			mob.tell("You need to gain more experience before you can cast this.");
			return false;
		}

		Item target=this.getTarget(mob,mob.location(),commands);
		if(target==null) return false;

		if(!super.invoke(mob,commands))
			return false;

		mob.curState().setMana(0);
		mob.setExperience(mob.getExperience()-50);

		boolean success=profficiencyCheck(mob.envStats().level()-target.envStats().level());
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,"<S-NAME> bless(es) <T-NAME>.");
			if(mob.location().okAffect(msg))
			{
				invoker=mob;
				mob.location().send(mob,msg);
				int a=0;
				while(a<target.numAffects())
				{
					Ability A=target.fetchAffect(a);
					int b=target.numAffects();
					if(A instanceof Prayer_CurseItem)
						A.unInvoke();
					if(b==target.numAffects())
						a++;
				}
				if(target.envStats().ability()>5)
					mob.tell(target.name()+" cannot be blessed further.");
				else
				{
					mob.location().show(mob,target,Affect.VISUAL_WNOISE,target.name()+" glows!");
					target.baseEnvStats().setAbility(target.baseEnvStats().ability()+1);
					target.baseEnvStats().setLevel(target.baseEnvStats().level()+2);
				}

				target.recoverEnvStats();
			}
		}
		else
			beneficialFizzle(mob,target,"<S-NAME> call(s) on <S-HIS-HER> god, but nothing happens.");


		// return whether it worked
		return success;
	}
}
