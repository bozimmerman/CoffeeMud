package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
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

		baseEnvStats().setLevel(5);

		addQualifyingClass("Fighter",5);
		addQualifyingClass("Paladin",2);
		addQualifyingClass("Ranger",2);
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

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(mob==null) return false;
		MOB imfighting=mob.getVictim();
		MOB target=null;
		
		if((commands.size()==0)
		&&(imfighting!=null)
		&&(imfighting!=mob)
		&&(imfighting.getVictim()!=null)
		&&(imfighting.getVictim()!=mob))
			target=imfighting.getVictim();
		
		if(target==null)
			target=getTarget(mob,commands,givenTarget);
		
		if(target==null) return false;
		MOB monster=target.getVictim();

		if((target.amDead())||(monster==null)||(monster.amDead()))
		{
			mob.tell(target.charStats().HeShe()+" isn't fighting anyone!");
			return false;
		}

		if(monster.getVictim()==mob)
		{
			mob.tell("You are already taking the blows from "+monster.name()+".");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		String str=null;
		if(success)
		{
			str="<S-NAME> rescue(s) <T-NAMESELF>!";
			FullMsg msg=new FullMsg(mob,target,null,Affect.MSG_NOISYMOVEMENT,str);
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				monster.setVictim(mob);
			}
		}
		else
		{
			str="<S-NAME> attempt(s) to rescue <T-NAMESELF>, but fail(s).";
			FullMsg msg=new FullMsg(mob,target,null,Affect.MSG_NOISYMOVEMENT,str);
			if(mob.location().okAffect(msg))
				mob.location().send(mob,msg);
		}

		return success;
	}

}
