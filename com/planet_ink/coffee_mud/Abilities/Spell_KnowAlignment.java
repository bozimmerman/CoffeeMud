package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.MOBS.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import java.util.*;

public class Spell_KnowAlignment extends Spell
	implements DivinationDevotion
{
	public Spell_KnowAlignment()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Know Alignment";

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(2);

		addQualifyingClass(new Mage().ID(),2);
		addQualifyingClass(new Ranger().ID(),baseEnvStats().level()+4);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_KnowAlignment();
	}

	public boolean invoke(MOB mob, Vector commands)
	{
		MOB target=this.getTarget(mob,commands);
		if(target==null) return false;
		if(target==mob) return false;

		if(!super.invoke(mob,commands))
			return false;

		boolean success=profficiencyCheck(0);

		// it worked, so build a copy of this ability,
		// and add it to the affects list of the
		// affected MOB.  Then tell everyone else
		// what happened.
		FullMsg msg=new FullMsg(mob,target,this,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,"<S-NAME> draw(s) out the disposition of <T-NAME>.");
		if(mob.location().okAffect(msg))
		{
			mob.location().send(mob,msg);
			if(success)
				mob.tell(mob,target,"<T-NAME> seem(s) like "+target.charStats().heshe()+" is "+Scoring.alignmentStr(target)+".");
			else
			{
				StdMOB newMOB=new StdMOB();
				newMOB.setAlignment(Dice.rollPercentage()*10);
				mob.tell(mob,target,"<T-NAME> seem(s) like "+target.charStats().heshe()+" is "+Scoring.alignmentStr(newMOB)+".");
			}
		}


		// return whether it worked
		return success;
	}
}
