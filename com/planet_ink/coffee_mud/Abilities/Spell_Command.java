package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import java.util.*;

public class Spell_Command extends Spell
	implements CharmDevotion
{
	public Spell_Command()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Command";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Commanded)";


		malicious=true;

		baseEnvStats().setLevel(21);

		addQualifyingClass(new Mage().ID(),21);
		addQualifyingClass(new Ranger().ID(),baseEnvStats().level()+4);

		canBeUninvoked=true;
		isAutoinvoked=false;

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_Command();
	}

	public boolean invoke(MOB mob, Vector commands)
	{
		if(commands.size()<1)
		{
			mob.tell("Command whom?");
			return false;
		}
		MOB target=mob.location().fetchInhabitant((String)commands.elementAt(0));
		if(target==null) return false;

		commands.removeElementAt(0);

		if(!target.isMonster())
		{
			mob.tell("You can't command a sentient player.");
			return false;
		}

		if(((String)commands.elementAt(0)).toUpperCase().startsWith("FOL"))
		{
			mob.tell("You can't command someone to follow.");
			return false;
		}


		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands))
			return false;

		boolean success=profficiencyCheck(0);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,Affect.SOUND_MAGIC,Affect.STRIKE_MAGIC,Affect.SOUND_MAGIC,"<S-NAME> command(s) <T-NAME> to '"+CommandProcessor.combine(commands,0)+"'.");
			FullMsg msg2=new FullMsg(mob,target,this,Affect.SOUND_MAGIC,Affect.STRIKE_MIND,Affect.SOUND_MAGIC,null);
			if((mob.location().okAffect(msg))&&((mob.location().okAffect(msg2))))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
				{
					mob.location().send(mob,msg2);
					if(!msg2.wasModified())
					{
						invoker=mob;
						target.makePeace();
						try
						{
							CommandProcessor.doCommand(target,commands);
						}
						catch(Exception e)
						{
							mob.tell(target.charStats().HeShe()+" smiles, saying '"+e.getMessage()+"'.");
						}
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to command <T-NAME>, but it definitely didn't work.");


		// return whether it worked
		return success;
	}
}