package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
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

		malicious=true;

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(2);

		addQualifyingClass(new Thief().ID(),2);
		addQualifyingClass(new Bard().ID(),13);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Thief_Peek();
	}

	public boolean invoke(MOB mob, Vector commands)
	{
		if(commands.size()<1)
		{
			mob.tell("Peek at whom?");
			return false;
		}
		MOB target=this.getTarget(mob,commands);
		if(target==null) return false;

		if(!super.invoke(mob,commands))
			return false;

		int levelDiff=target.envStats().level()-mob.envStats().level();

		boolean success=profficiencyCheck(-(levelDiff*10));

		if(!success)
		{
			int discoverChance=(int)Math.round(Util.div(target.charStats().getWisdom(),30.0))+(levelDiff*5);
			if(discoverChance>95) discoverChance=95;
			if(discoverChance<5) discoverChance=5;

			if(Dice.rollPercentage()<discoverChance)
			{
				FullMsg msg=new FullMsg(mob,target,null,Affect.VISUAL_WNOISE,"Your peek attempt fails; <T-NAME> spots you!",Affect.VISUAL_WNOISE,"<S-NAME> tries to peek at your inventory and fails!",Affect.NO_EFFECT,null);
				if(mob.location().okAffect(msg))
					mob.location().send(mob,msg);
			}
			else
			{
				mob.tell("Your peek attempt fails.");
				return false;
			}
		}
		else
		{
			int discoverChance=(int)Math.round(Util.div(target.charStats().getWisdom(),30.0))+(levelDiff*5);
			if(discoverChance>95) discoverChance=95;
			if(discoverChance<5) discoverChance=5;

			String str=null;
			if(Dice.rollPercentage()<discoverChance)
				str="<S-NAME> peek(s) at your inventory.";

			FullMsg msg=new FullMsg(mob,target,null,Affect.HANDS_DELICATE,"<S-NAME> peek(s) at <T-NAME>s inventory.",Affect.VISUAL_LOOK,str,Affect.NO_EFFECT,null);
			if(mob.location().okAffect(msg))
			{
				msg=new FullMsg(mob,target,null,Affect.HANDS_DELICATE,"<S-NAME> peek(s) at <T-NAME>s inventory.",Affect.VISUAL_WNOISE,str,Affect.NO_EFFECT,null);
				mob.location().send(mob,msg);
				StringBuffer msg2=Scoring.getInventory(mob,target);
				if(msg2.length()==0)
					mob.tell(target.charStats().HeShe()+" is carrying:\n\rNothing!\n\r");
				else
					mob.session().rawPrintln(target.charStats().HeShe()+" is carrying:\n\r"+msg2.toString());
			}
		}
		return success;
	}

}
