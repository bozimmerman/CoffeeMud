package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_Peek extends ThiefSkill
{

	public Thief_Peek()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Peek";
		displayText="(in a dark realm of thievery)";
		miscText="";

		triggerStrings.addElement("PEEK");

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(2);

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Thief_Peek();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(commands.size()<1)
		{
			mob.tell("Peek at whom?");
			return false;
		}
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		int levelDiff=target.envStats().level()-mob.envStats().level();

		boolean success=profficiencyCheck(-(levelDiff*10),auto);

		if(!success)
		{
			int discoverChance=(int)Math.round(Util.div(target.charStats().getStat(CharStats.WISDOM),30.0))+(levelDiff*5);
			if(discoverChance>95) discoverChance=95;
			if(discoverChance<5) discoverChance=5;

			if(Dice.rollPercentage()<discoverChance)
			{
				FullMsg msg=new FullMsg(mob,target,null,Affect.MSG_OK_VISUAL,auto?"":"Your peek attempt fails; <T-NAME> spots you!",Affect.MSG_OK_VISUAL,auto?"":"<S-NAME> tries to peek at your inventory and fails!",Affect.NO_EFFECT,null);
				if(mob.location().okAffect(msg))
					mob.location().send(mob,msg);
			}
			else
			{
				mob.tell(auto?"":"Your peek attempt fails.");
				return false;
			}
		}
		else
		{
			int discoverChance=(int)Math.round(Util.div(target.charStats().getStat(CharStats.WISDOM),30.0))+(levelDiff*5);
			if(discoverChance>95) discoverChance=95;
			if(discoverChance<5) discoverChance=5;

			String str=null;
			if(Dice.rollPercentage()<discoverChance)
				str=auto?"":"<S-NAME> peek(s) at your inventory.";

			FullMsg msg=new FullMsg(mob,target,null,auto?Affect.MSG_OK_VISUAL:(Affect.MSG_DELICATE_HANDS_ACT|Affect.ACT_EYES),auto?"":"<S-NAME> peek(s) at <T-NAME>s inventory.",Affect.MSG_EXAMINESOMETHING,str,Affect.NO_EFFECT,null);
			if(mob.location().okAffect(msg))
			{
				msg=new FullMsg(mob,target,null,Affect.MSG_OK_VISUAL,auto?"":"<S-NAME> peek(s) at <T-NAME>s inventory.",Affect.MSG_OK_VISUAL,str,Affect.NO_EFFECT,null);
				mob.location().send(mob,msg);
				StringBuffer msg2=ExternalPlay.getInventory(mob,target);
				if(msg2.length()==0)
					mob.tell(target.charStats().HeShe()+" is carrying:\n\rNothing!\n\r");
				else
					mob.session().unfilteredPrintln(target.charStats().HeShe()+" is carrying:\n\r"+msg2.toString());
			}
		}
		return success;
	}

}
