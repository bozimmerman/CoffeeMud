package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import java.util.*;


public class Spell_ResistCold extends Spell
	implements AbjurationDevotion
{

	public Spell_ResistCold()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Resist Cold";
		displayText="(Resist Cold)";
		miscText="";

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(4);

		addQualifyingClass(new Mage().ID(),4);
		addQualifyingClass(new Ranger().ID(),baseEnvStats().level()+4);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_ResistCold();
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		mob.tell("Your warm protection cools.");

		super.unInvoke();

	}


	public void affect(Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;

		MOB mob=(MOB)affected;

		if(affect.amITarget(mob))
		{
			switch(affect.targetCode())
			{
			case Affect.STRIKE_COLD:
				if(Dice.rollPercentage()>35)
				{
					mob.location().show(mob,null,Affect.VISUAL_WNOISE,"The warm field around <S-NAME> absorbs the cold blast.");
					affect.tagModified(true);
				}
				break;
			default:
				break;
			}
		}
	}


	public boolean invoke(MOB mob, Vector commands)
	{
		MOB target=getTarget(mob,commands);
		if(target==null)
			return false;

		if(!super.invoke(mob,commands))
			return false;

		boolean success=profficiencyCheck(0);

		FullMsg msg=new FullMsg(mob,target,this,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,Affect.VISUAL_WNOISE,"<S-NAME> invoke(s) a warm field of protection around <T-NAME>.");
		if(mob.location().okAffect(msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,target,0);
		}
		else
			beneficialFizzle(mob,target,"<S-NAME> attempt(s) to invoke warmth, but fail(s).");

		return success;
	}
}