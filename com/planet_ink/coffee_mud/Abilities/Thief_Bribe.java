package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import java.util.*;

public class Thief_Bribe extends ThiefSkill
{

	public Thief_Bribe()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Peek";
		displayText="(in a dark realm of thievery)";
		miscText="";

		triggerStrings.addElement("BRIBE");

		canBeUninvoked=true;
		isAutoinvoked=false;

		malicious=true;

		baseEnvStats().setLevel(20);

		addQualifyingClass(new Thief().ID(),20);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Thief_Bribe();
	}

	public boolean invoke(MOB mob, Vector commands)
	{
		if(commands.size()<1)
		{
			mob.tell("Bribe whom?");
			return false;
		}
		MOB target=this.getTarget(mob,commands);
		if(target==null) return false;

		commands.removeElementAt(0);

		if(!target.isMonster())
		{
			mob.tell("You can't bribe a sentient player.");
			return false;
		}

		if(((String)commands.elementAt(0)).toUpperCase().startsWith("FOL"))
		{
			mob.tell("You can't bribe someone to follow.");
			return false;
		}


		if(!super.invoke(mob,commands))
			return false;

		int levelDiff=target.envStats().level()-mob.envStats().level();

		int amountRequired=target.getMoney()+(int)(Math.round(100.0*(Util.div(25.0,mob.charStats().getCharisma())))*target.envStats().level());

		boolean success=profficiencyCheck(0);

		if((!success)||(mob.getMoney()<amountRequired))
		{
			FullMsg msg=new FullMsg(mob,target,this,Affect.SOUND_WORDS,"<S-NAME> try to bribe <T-NAME> to '"+CommandProcessor.combine(commands,0)+"', but no deal is reached.",Affect.SOUND_WORDS,"<S-NAME> tries to bribe <T-NAME> to '"+CommandProcessor.combine(commands,0)+"', but no deal is reached.",Affect.SOUND_WORDS,"<S-NAME> tries to bribe <T-NAME> to '"+CommandProcessor.combine(commands,0)+"', but no deal is reached.");
			if(mob.location().okAffect(msg))
				mob.location().send(mob,msg);
			if(mob.getMoney()<amountRequired)
				mob.tell(target.charStats().HeShe()+" requires "+amountRequired+" coins to do this.");
			success=false;
		}
		else
		{
			FullMsg msg=new FullMsg(mob,target,null,Affect.SOUND_WORDS,Affect.SOUND_WORDS,Affect.SOUND_WORDS,"<S-NAME> bribe(s) <T-NAME> to '"+CommandProcessor.combine(commands,0)+"' for "+amountRequired+" coins.");
			mob.setMoney(mob.getMoney()-amountRequired);
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				try
				{
					CommandProcessor.doCommand(target,commands);
				}
				catch(Exception e)
				{
					mob.tell(target.charStats().HeShe()+" takes your money and smiles, saying '"+e.getMessage()+"'.");
				}
			}
			target.setMoney(mob.getMoney()+amountRequired);
		}
		return success;
	}

}
