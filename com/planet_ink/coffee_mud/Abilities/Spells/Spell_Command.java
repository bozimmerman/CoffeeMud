package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Command extends Spell
{
	public Spell_Command()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Command";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Commanded)";


		quality=Ability.MALICIOUS;

		baseEnvStats().setLevel(21);

		canBeUninvoked=true;
		isAutoinvoked=false;

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_Command();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.DOMAIN_ENCHANTMENT;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Vector V=new Vector();
		if(commands.size()>0)
		{
			V.addElement(commands.elementAt(0));
			commands.removeElementAt(0);
		}

		MOB target=getTarget(mob,V,givenTarget);
		if(target==null) return false;

		if(commands.size()==0)
		{
			mob.tell("Command "+((String)V.elementAt(0))+" to do what?");
			return false;
		}

		if(!target.mayIFight(mob))
		{
			mob.tell("You can't command "+target.name()+".");
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
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType,auto?"":"<S-NAME> command(s) <T-NAMESELF> to '"+Util.combine(commands,0)+"'.");
			FullMsg msg2=new FullMsg(mob,target,this,Affect.MSK_CAST_MALICIOUS_VERBAL|Affect.TYP_MIND,null);
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
							ExternalPlay.doCommand(target,commands);
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
			return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to command <T-NAMESELF>, but it definitely didn't work.");


		// return whether it worked
		return success;
	}
}