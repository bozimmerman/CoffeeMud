package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_Steal extends ThiefSkill
{

	public Thief_Steal()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Steal";
		displayText="(in a dark realm of thievery)";
		miscText="";

		triggerStrings.addElement("STEAL");

		canBeUninvoked=true;
		isAutoinvoked=false;

		quality=Ability.MALICIOUS;

		baseEnvStats().setLevel(8);

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Thief_Steal();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(commands.size()<2)
		{
			mob.tell("Steal what from whom?");
			return false;
		}
		String itemToSteal=(String)commands.elementAt(0);

		MOB target=mob.location().fetchInhabitant(Util.combine(commands,1));
		if((target==null)||(target.amDead())||(!Sense.canBeSeenBy(target,mob)))
		{
			mob.tell("You don't see '"+Util.combine(commands,1)+"' here.");
			return false;
		}
		int levelDiff=target.envStats().level()-mob.envStats().level();

		if((!target.isMonster())&&(levelDiff<10))
		{
			mob.tell("You cannot steal from "+target.charStats().himher()+".");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		Item stolen=target.fetchCarried(null,itemToSteal);

		boolean success=profficiencyCheck(-(levelDiff*15),auto);

		if(!success)
		{
			int discoverChance=target.charStats().getWisdom()+(levelDiff*5);
			if(discoverChance>95) discoverChance=95;
			if(discoverChance<5) discoverChance=5;
			if(Dice.rollPercentage()<discoverChance)
			{
				FullMsg msg=new FullMsg(mob,target,null,Affect.MSG_NOISYMOVEMENT,auto?"":"You fumble the attempt to steal; <T-NAME> spots you!",Affect.MSG_NOISYMOVEMENT,auto?"":"<S-NAME> tries to steal from you and fails!",Affect.NO_EFFECT,null);
				if(mob.location().okAffect(msg))
					mob.location().send(mob,msg);
			}
			else
				mob.tell(auto?"":"You fumble the attempt to steal.");
		}
		else
		{
			int discoverChance=target.charStats().getWisdom()+(levelDiff*5);
			if(discoverChance>95) discoverChance=95;
			if(discoverChance<5) discoverChance=5;

			String str=null;
			if(!auto)
				if((stolen!=null)&&(stolen.amWearingAt(Item.INVENTORY)))
					str="<S-NAME> steal(s) "+stolen.name()+" from <T-NAMESELF>.";
				else
					str="<S-NAME> attempt(s) to steal from <T-HIS-HER>, but it doesn't appear "+target.charStats().heshe()+" has that in <T-HIS-HER> inventory!";

			String hisStr=str;
			int hisCode=Affect.MSG_NOISYMOVEMENT;
			if(Dice.rollPercentage()>discoverChance)
				hisStr=null;
			else
				hisCode=hisCode | ((target.isMonster())?Affect.MASK_MALICIOUS:0);
			
			FullMsg msg=new FullMsg(mob,target,null,Affect.MSG_DELICATE_HANDS_ACT,str,hisCode,hisStr,Affect.NO_EFFECT,null);
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				msg=new FullMsg(target,stolen,null,Affect.MSG_DROP,Affect.MSG_DROP,Affect.MSG_NOISE,null);
				if(target.location().okAffect(msg))
				{
					target.location().send(mob,msg);
					msg=new FullMsg(mob,stolen,null,Affect.MSG_GET,Affect.MSG_GET,Affect.MSG_NOISE,null);
					if(mob.location().okAffect(msg))
						mob.location().send(mob,msg);
				}
			}
		}
		return success;
	}

}
