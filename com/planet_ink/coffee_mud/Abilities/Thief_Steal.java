package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
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

		malicious=true;

		baseEnvStats().setLevel(8);

		addQualifyingClass(new Thief().ID(),8);
		addQualifyingClass(new Bard().ID(),23);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Thief_Steal();
	}

	public boolean invoke(MOB mob, Vector commands)
	{
		if(commands.size()<2)
		{
			mob.tell("Steal what from whom?");
			return false;
		}
		String itemToSteal=(String)commands.elementAt(0);

		MOB target=mob.location().fetchInhabitant(CommandProcessor.combine(commands,1));
		if((target==null)||(target.amDead())||(!Sense.canBeSeenBy(target,mob)))
		{
			mob.tell("You don't see that here.");
			return false;
		}
		int levelDiff=target.envStats().level()-mob.envStats().level();

		if((!target.isMonster())&&(levelDiff<10))
		{
			mob.tell("You cannot steal from "+target.charStats().himher()+".");
			return false;
		}
		if(!super.invoke(mob,commands))
			return false;

		Item stolen=target.fetchCarried(null,itemToSteal);

		boolean success=profficiencyCheck(-(levelDiff*10));

		if(!success)
		{
			int discoverChance=(int)Math.round(Util.div(target.charStats().getWisdom(),30.0))+(levelDiff*5);
			if(discoverChance>95) discoverChance=95;
			if(discoverChance<5) discoverChance=5;
			if(Dice.rollPercentage()<discoverChance)
			{
				FullMsg msg=new FullMsg(mob,target,null,Affect.VISUAL_WNOISE,"You fumble the attempt to steal; <T-NAME> spots you!",Affect.STRIKE_JUSTICE,"<S-NAME> tries to steal from you and fails!",Affect.NO_EFFECT,null);
				if(mob.location().okAffect(msg))
					mob.location().send(mob,msg);
			}
			else
				mob.tell("You fumble the attempt to steal.");
		}
		else
		{
			int discoverChance=(int)Math.round(Util.div(target.charStats().getWisdom(),30.0))+(levelDiff*5);
			if(discoverChance>95) discoverChance=95;
			if(discoverChance<5) discoverChance=5;

			String str=null;
			if((stolen!=null)&&(stolen.amWearingAt(Item.INVENTORY)))
				str="<S-NAME> steal(s) "+stolen.name()+" from <T-NAME>.";
			else
				str="<S-NAME> attempt(s) to steal from <T-HIS-HER>, but it doesn't appear "+target.charStats().heshe()+" has that in <T-HIS-HER> inventory!";

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
				msg=new FullMsg(target,stolen,null,Affect.HANDS_DROP,Affect.HANDS_DROP,Affect.VISUAL_WNOISE,null);
				if(target.location().okAffect(msg))
				{
					target.location().send(mob,msg);
					msg=new FullMsg(mob,stolen,null,Affect.HANDS_GET,Affect.HANDS_GET,Affect.VISUAL_WNOISE,null);
					if(mob.location().okAffect(msg))
						mob.location().send(mob,msg);
				}
			}
		}
		return success;
	}

}
