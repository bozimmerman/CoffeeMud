package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_Steal extends ThiefSkill
{
	public String ID() { return "Thief_Steal"; }
	public String name(){ return "Steal";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	private static final String[] triggerStrings = {"STEAL"};
	public String[] triggerStrings(){return triggerStrings;}
	public Environmental newInstance(){	return new Thief_Steal();}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(commands.size()<2)
		{
			mob.tell("Steal what from whom?");
			return false;
		}
		String itemToSteal=(String)commands.elementAt(0);

		MOB target=mob.location().fetchInhabitant(Util.combine(commands,1));
		if((target==null)&&(givenTarget!=null)&&(givenTarget instanceof MOB)) target=(MOB)givenTarget;
		if((target==null)||(target.amDead())||(!Sense.canBeSeenBy(target,mob)))
		{
			mob.tell("You don't see '"+Util.combine(commands,1)+"' here.");
			return false;
		}
		int levelDiff=target.envStats().level()-mob.envStats().level();

		if((!target.mayIFight(mob))&&(levelDiff<10))
		{
			mob.tell("You cannot steal from "+target.charStats().himher()+".");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		Item stolen=target.fetchCarried(null,itemToSteal);

		int discoverChance=(target.charStats().getStat(CharStats.WISDOM)*5)-(levelDiff*5);
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
				FullMsg msg=new FullMsg(mob,target,this,Affect.MSG_NOISYMOVEMENT,auto?"":"You fumble the attempt to steal; <T-NAME> spots you!",Affect.MSG_NOISYMOVEMENT,auto?"":"<S-NAME> tries to steal from you and fails!",Affect.MSG_NOISYMOVEMENT,auto?"":"<S-NAME> tries to steal from <T-NAME> and fails!");
				if(mob.location().okAffect(mob,msg))
					mob.location().send(mob,msg);
			}
			else
				mob.tell(auto?"":"You fumble the attempt to steal.");
		}
		else
		{
			String str=null;
			int code=Affect.MSG_THIEF_ACT;
			if(!auto)
				if((stolen!=null)&&(stolen.amWearingAt(Item.INVENTORY)))
					str="<S-NAME> steal(s) "+stolen.name()+" from <T-NAMESELF>.";
				else
				{
					code=Affect.MSG_QUIETMOVEMENT;
					str="<S-NAME> attempt(s) to steal from <T-HIM-HER>, but it doesn't appear "+target.charStats().heshe()+" has that in <T-HIS-HER> inventory!";
				}

			boolean alreadyFighting=(mob.getVictim()==target)||(target.getVictim()==mob);
			String hisStr=str;
			int hisCode=Affect.MSG_THIEF_ACT | ((target.mayIFight(mob))?Affect.MASK_MALICIOUS:0);
			if(Dice.rollPercentage()<discoverChance)
				hisStr=null;

			FullMsg msg=new FullMsg(mob,target,this,code,str,hisCode,hisStr,Affect.NO_EFFECT,null);
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				if(((hisStr==null)||mob.isMonster())&&(!alreadyFighting))
				{
					if(target.getVictim()==mob)
						target.makePeace();
				}
				msg=new FullMsg(target,stolen,null,Affect.MSG_DROP,Affect.MSG_DROP,Affect.MSG_NOISE,null);
				if(target.location().okAffect(target,msg))
				{
					target.location().send(mob,msg);
					msg=new FullMsg(mob,stolen,null,Affect.MSG_GET,Affect.MSG_GET,Affect.MSG_NOISE,null);
					if(mob.location().okAffect(mob,msg))
						mob.location().send(mob,msg);
				}
			}
		}
		return success;
	}

}
