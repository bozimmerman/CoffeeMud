package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.Weapons.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import java.util.*;

public class Fighter_Rescue extends StdAbility
{
	public Fighter_Rescue()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Rescue";
		displayText="(Rescued)";
		miscText="";

		triggerStrings.addElement("RESCUE");
		triggerStrings.addElement("RES");

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(7);

		addQualifyingClass(new Fighter().ID(),7);
		addQualifyingClass(new Paladin().ID(),baseEnvStats().level()+2);
		addQualifyingClass(new Ranger().ID(),baseEnvStats().level()+2);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Fighter_Rescue();
	}

	public int classificationCode()
	{
		return Ability.SKILL;
	}

	public boolean invoke(MOB mob, Vector commands)
	{
		MOB target=this.getTarget(mob,commands);
		if(target==null) return false;

		if((target.amDead())||(!target.isInCombat()))
		{
			mob.tell(target.charStats().HeShe()+" isn't fighting anyone!");
			return false;
		}

		MOB monster=target.getVictim();

		if(monster.getVictim()==mob)
		{
			mob.tell("You are already taking the blows from "+mob.getVictim().name()+".");
			return false;
		}

		if(!super.invoke(mob,commands))
			return false;

		boolean success=profficiencyCheck(0);

		String str=null;
		if(success)
		{
			str="<S-NAME> rescue(s) <T-NAME>!";
			FullMsg msg=new FullMsg(mob,target,null,Affect.VISUAL_WNOISE,str,Affect.VISUAL_WNOISE,str,Affect.VISUAL_WNOISE,str);
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				monster.setVictim(mob);
			}
		}
		else
		{
			str="<S-NAME> attempt(s) to rescue <T-NAME>, but fail(s).";
			FullMsg msg=new FullMsg(mob,target,null,Affect.VISUAL_WNOISE,str,Affect.VISUAL_WNOISE,str,Affect.VISUAL_WNOISE,str);
			if(mob.location().okAffect(msg))
				mob.location().send(mob,msg);
		}

		return success;
	}

}
