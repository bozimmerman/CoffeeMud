package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
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

		quality=Ability.MALICIOUS;

		baseEnvStats().setLevel(1);

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Thief_Swipe();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if((commands.size()<1)&&(givenTarget==null))
		{
			mob.tell("Swipe from whom?");
			return false;
		}
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		int levelDiff=target.envStats().level()-mob.envStats().level();

		if((!target.mayIFight(mob))&&(levelDiff<15))
		{
			mob.tell("You cannot swipe from "+target.charStats().himher()+".");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(-(levelDiff*15),auto);

		if(!success)
		{
			int discoverChance=target.charStats().getStat(CharStats.WISDOM)+(levelDiff*3);
			if(discoverChance>95) discoverChance=95;
			if(discoverChance<5) discoverChance=5;
			if(Dice.rollPercentage()<discoverChance)
			{
				FullMsg msg=new FullMsg(mob,target,null,Affect.MSG_NOISYMOVEMENT,auto?"":"You fumble the swipe; <T-NAME> spots you!",Affect.MSG_NOISYMOVEMENT,auto?"":"<S-NAME> tries to pick your pocket and fails!",Affect.NO_EFFECT,null);
				if(mob.location().okAffect(msg))
					mob.location().send(mob,msg);
			}
			else
				mob.tell(auto?"":"You fumble the swipe.");
		}
		else
		{
			int discoverChance=target.charStats().getStat(CharStats.WISDOM)+(levelDiff*3);
			if(discoverChance>95) discoverChance=95;
			if(discoverChance<5) discoverChance=5;

			double pct=0.25;
			if(levelDiff>0) pct=0.15;
			if(levelDiff>5) pct=0.10;
			if(levelDiff>10) pct=0.05;
			int goldTaken=(int)Math.round(new Integer(target.getMoney()).doubleValue()*pct*Math.random());
			if(goldTaken>target.getMoney()) goldTaken=target.getMoney();

			String str=null;
			if(!auto)
				if(goldTaken > 0)
					str="<S-NAME> pick(s) <T-HIS-HER> pocket for "+goldTaken+" gold.";
				else
					str="<S-NAME> attempt(s) to pick <T-HIS-HER> pocket, but nothing was found to steal!";

			String hisStr=str;
			int hisCode=Affect.MSG_NOISYMOVEMENT;
			if(Dice.rollPercentage()>discoverChance)
				hisStr=null;
			else
				hisCode=hisCode | ((target.mayIFight(mob))?Affect.MASK_MALICIOUS:0);
			FullMsg msg=new FullMsg(mob,target,null,Affect.MSG_DELICATE_HANDS_ACT,str,hisCode,hisStr,Affect.NO_EFFECT,null);
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
