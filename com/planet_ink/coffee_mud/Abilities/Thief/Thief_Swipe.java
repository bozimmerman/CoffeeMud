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
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}
	
	private DVector lastOnes=new DVector(2);
	private int timesPicked(MOB target)
	{
		int times=0;
		for(int x=0;x<lastOnes.size();x++)
		{
			MOB M=(MOB)lastOnes.elementAt(x,1);
			Integer I=(Integer)lastOnes.elementAt(x,2);
			if(M==target)
			{
				times=I.intValue();
				lastOnes.removeElement(M);
				break;
			}
		}
		if(lastOnes.size()>=50)
			lastOnes.removeElementAt(0);
		lastOnes.addElement(target,new Integer(times+1)); 
		return times+1;
	}
	
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if((commands.size()<1)&&(givenTarget==null))
		{
			mob.tell("Swipe from whom?");
			return false;
		}
		if(mob.isInCombat())
		{
			mob.tell("Not while you are fighting!");
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
		int times=timesPicked(target);
		if(times>5) discoverChance-=(20*(times-5));
		if(!Sense.canBeSeenBy(mob,target))
			discoverChance+=50;
		if(discoverChance>95) discoverChance=95;
		if(discoverChance<5) discoverChance=5;

		if(levelDiff>0) 
			levelDiff=-(levelDiff*((!Sense.canBeSeenBy(mob,target))?5:15));
		else
			levelDiff=-(levelDiff*((!Sense.canBeSeenBy(mob,target))?1:2));
		boolean success=profficiencyCheck(levelDiff,auto);

		if(!success)
		{
			if(Dice.rollPercentage()>discoverChance)
			{
				FullMsg msg=new FullMsg(mob,target,this,Affect.MSG_NOISYMOVEMENT,auto?"":"You fumble the swipe; <T-NAME> spots you!",Affect.MSG_NOISYMOVEMENT,auto?"":"<S-NAME> tries to pick your pocket and fails!",Affect.MSG_OK_VISUAL,auto?"":"<S-NAME> tries to pick <T-NAME>'s pocket and fails!");
				if(mob.location().okAffect(mob,msg))
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
			if((goldTaken<CMAble.qualifyingClassLevel(mob,this)))
				goldTaken=CMAble.qualifyingClassLevel(mob,this);
			if(goldTaken>target.getMoney())
				goldTaken=target.getMoney();

			String str=null;
			int code=Affect.MSG_THIEF_ACT;
			if(!auto)
				if(goldTaken > 0)
					str="<S-NAME> pick(s) <T-HIS-HER> pocket for "+goldTaken+" gold.";
				else
				{
					str="<S-NAME> attempt(s) to pick <T-HIS-HER> pocket, but nothing was found to steal!";
					code=Affect.MSG_QUIETMOVEMENT;
				}

			boolean alreadyFighting=(mob.getVictim()==target)||(target.getVictim()==mob);
			String hisStr=str;
			int hisCode=Affect.MSG_THIEF_ACT| ((target.mayIFight(mob))?Affect.MASK_MALICIOUS:0);
			if(Dice.rollPercentage()<discoverChance)
				hisStr=null;
			else
				str+=" <T-NAME> spots you!";

			FullMsg msg=new FullMsg(mob,target,this,code,str,hisCode,hisStr,Affect.NO_EFFECT,null);
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				if(((hisStr==null)||mob.isMonster())&&(!alreadyFighting))
				{
					if(target.getVictim()==mob)
						target.makePeace();
					if(mob.getVictim()==target)
						mob.makePeace();
				}
				mob.setMoney(mob.getMoney()+goldTaken);
				mob.recoverEnvStats();
				target.setMoney(target.getMoney()-goldTaken);
				target.recoverEnvStats();
			}
		}
		return success;
	}

}
