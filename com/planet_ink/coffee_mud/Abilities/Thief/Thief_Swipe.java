package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_Swipe extends ThiefSkill
{
	public String ID() { return "Thief_Swipe"; }
	public String name(){ return "Swipe gold";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	private static final String[] triggerStrings = {"SWIPE"};
	public String[] triggerStrings(){return triggerStrings;}
	public Environmental newInstance(){	return new Thief_Swipe();}

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

		int discoverChance=(target.charStats().getStat(CharStats.WISDOM)*5)-(levelDiff*3);
		if(!Sense.canBeSeenBy(mob,target))
			discoverChance+=50;
		if(discoverChance>95) discoverChance=95;
		if(discoverChance<5) discoverChance=5;
		
		boolean success=profficiencyCheck(-(levelDiff*((!Sense.canBeSeenBy(mob,target))?5:15)),auto);

		if(!success)
		{
			if(Dice.rollPercentage()>discoverChance)
			{
				FullMsg msg=new FullMsg(mob,target,this,Affect.MSG_NOISYMOVEMENT,auto?"":"You fumble the swipe; <T-NAME> spots you!",Affect.MSG_NOISYMOVEMENT,auto?"":"<S-NAME> tries to pick your pocket and fails!",Affect.MSG_OK_VISUAL,auto?"":"<S-NAME> tries to pick <T-NAME>'s pocket and fails!");
				if(mob.location().okAffect(msg))
					mob.location().send(mob,msg);
			}
			else
				mob.tell(auto?"":"You fumble the swipe.");
		}
		else
		{
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
			int hisCode=Affect.MSG_DELICATE_HANDS_ACT;
			if(Dice.rollPercentage()<discoverChance)
				hisStr=null;
			else
				hisCode=hisCode | ((target.mayIFight(mob))?Affect.MASK_MALICIOUS:0);
			FullMsg msg=new FullMsg(mob,target,this,Affect.MSG_THIEF_ACT,str,hisCode,hisStr,Affect.NO_EFFECT,null);
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
