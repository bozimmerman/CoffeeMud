package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_Mug extends ThiefSkill
{
	public String ID() { return "Thief_Mug"; }
	public String name(){ return "Mug";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	private static final String[] triggerStrings = {"MUG"};
	public String[] triggerStrings(){return triggerStrings;}
	public Environmental newInstance(){	return new Thief_Mug();}
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}
	public int code=0;

	public int abilityCode(){return code;}
	public void setAbilityCode(int newCode){code=newCode;}
	
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
		MOB target=mob.getVictim();
		if(!mob.isInCombat())
		{
			mob.tell("You can only mug someone you are fighting!");
			return false;
		}
		if(commands.size()<1)
		{
			mob.tell("Mug what from "+target.name()+"?");
			return false;
		}
		String itemToSteal=Util.combine(commands,0);
		int levelDiff=target.envStats().level()-(mob.envStats().level()+abilityCode());
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		Item stolen=target.fetchCarried(null,itemToSteal);
		boolean success=profficiencyCheck(mob,levelDiff,auto);
		if(!success)
		{
			FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSG_NOISYMOVEMENT,auto?"":"You fumble the attempt to steal; <T-NAME> spots you!",CMMsg.MSG_NOISYMOVEMENT,auto?"":"<S-NAME> tries to steal from you and fails!",CMMsg.MSG_NOISYMOVEMENT,auto?"":"<S-NAME> tries to mug <T-NAME> and fails!");
			if(mob.location().okMessage(mob,msg))
				mob.location().send(mob,msg);
		}
		else
		{
			String str=null;
			int code=(auto?CMMsg.MASK_GENERAL:0)|CMMsg.MSG_THIEF_ACT;
			if(!auto)
				if((stolen!=null)&&(stolen.amWearingAt(Item.INVENTORY)))
					str="<S-NAME> steal(s) "+stolen.name()+" from <T-NAMESELF>.";
				else
				{
					code=CMMsg.MSG_QUIETMOVEMENT;
					str="<S-NAME> attempt(s) to mug <T-HIM-HER>, but it doesn't appear "+target.charStats().heshe()+" has that in <T-HIS-HER> inventory!";
				}

			boolean alreadyFighting=(mob.getVictim()==target)||(target.getVictim()==mob);
			FullMsg msg=new FullMsg(mob,target,this,code,str,(auto?CMMsg.MASK_GENERAL:0)|CMMsg.MSG_THIEF_ACT|CMMsg.MASK_MALICIOUS,str,CMMsg.NO_EFFECT,null);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				msg=new FullMsg(target,stolen,null,CMMsg.MSG_DROP,CMMsg.MSG_DROP,CMMsg.MSG_NOISE,null);
				if(target.location().okMessage(target,msg))
				{
					target.location().send(mob,msg);
					msg=new FullMsg(mob,stolen,null,CMMsg.MSG_GET,CMMsg.MSG_GET,CMMsg.MSG_NOISE,null);
					if(mob.location().okMessage(mob,msg))
						mob.location().send(mob,msg);
				}
			}
		}
		return success;
	}

}
