package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import java.util.*;

public class Thief_Swipe extends ThiefSkill
{

	public Thief_Swipe()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Swipe gold";
		displayText="(in a dark realm of thievery)";
		miscText="";

		triggerStrings.addElement("SWIPE");

		canBeUninvoked=true;
		isAutoinvoked=false;

		malicious=true;

		baseEnvStats().setLevel(1);

		addQualifyingClass(new Thief().ID(),1);
		addQualifyingClass(new Bard().ID(),19);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Thief_Swipe();
	}

	public boolean invoke(MOB mob, Vector commands)
	{
		if(commands.size()<1)
		{
			mob.tell("Swipe from whom?");
			return false;
		}
		MOB target=this.getTarget(mob,commands);
		if(target==null) return false;

		int levelDiff=target.envStats().level()-mob.envStats().level();

		if((!target.isMonster())&&(levelDiff<15))
		{
			mob.tell("You cannot swipe from "+target.charStats().himher()+".");
			return false;
		}
		if(!super.invoke(mob,commands))
			return false;

		boolean success=profficiencyCheck(-(levelDiff*10));

		if(!success)
		{
			int discoverChance=(int)Math.round(Util.div(target.charStats().getWisdom(),30.0))+(levelDiff*5);
			if(discoverChance>95) discoverChance=95;
			if(discoverChance<5) discoverChance=5;
			if(Dice.rollPercentage()<discoverChance)
			{
				FullMsg msg=new FullMsg(mob,target,null,Affect.VISUAL_WNOISE,"You fumble the swipe; <T-NAME> spots you!",Affect.STRIKE_JUSTICE,"<S-NAME> tries to pick your pocket and fails!",Affect.NO_EFFECT,null);
				if(mob.location().okAffect(msg))
					mob.location().send(mob,msg);
			}
			else
				mob.tell("You fumble the swipe.");
		}
		else
		{
			int discoverChance=(int)Math.round(Util.div(target.charStats().getWisdom(),30.0))+(levelDiff*5);
			if(discoverChance>95) discoverChance=95;
			if(discoverChance<5) discoverChance=5;

			int goldTaken=(int)Math.round(new Integer(target.getMoney()).doubleValue()*((new Integer(mob.envStats().level()-1).doubleValue()*.10)+.10)*Math.random());
			if(goldTaken>target.getMoney()) goldTaken=target.getMoney();

			String str=null;
			if(goldTaken > 0)
				str="<S-NAME> pick(s) <T-HIS-HER> pocket for "+goldTaken+" gold.";
			else
				str="<S-NAME> attempt(s) to pick <T-HIS-HER> pocket, but nothing was found to steal!";

			String hisStr=null;
			int hisCode=Affect.NO_EFFECT;
			if(Dice.rollPercentage()<discoverChance)
			{
				hisStr=str;
				hisCode=Affect.STRIKE_JUSTICE;
			}
			FullMsg msg=new FullMsg(mob,target,null,Affect.HANDS_DELICATE,str,hisCode,hisStr,Affect.NO_EFFECT,null);
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				mob.setMoney(mob.getMoney()+goldTaken);
				target.setMoney(target.getMoney()-goldTaken);
			}
		}
		return success;
	}

}
