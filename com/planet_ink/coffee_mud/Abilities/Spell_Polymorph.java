package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import com.planet_ink.coffee_mud.Races.*;
import java.util.*;

public class Spell_Polymorph extends Spell
	implements AlterationDevotion
{

	Race newRace=null;

	public Spell_Polymorph()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Polymorph";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Polymorph)";

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(15);

		addQualifyingClass(new Mage().ID(),15);
		addQualifyingClass(new Ranger().ID(),baseEnvStats().level()+4);

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_Polymorph();
	}


	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(newRace!=null)
			affectableStats.setMyRace(newRace);
	}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		super.unInvoke();

		mob.tell("You feel more like yourself again.");
	}



	public boolean invoke(MOB mob, Vector commands)
	{
		MOB target=this.getTarget(mob,commands);
		if(target==null) return false;

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
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,"<S-NAME> form(s) a spell around <T-NAME>.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
				{
					int numRaces=MUD.races.size();
					newRace=null;
					while(newRace==null)
					{
						int raceNum=(int)Math.round(Math.random()*numRaces);
						for(int r=0;r<MUD.races.size();r++)
						{
							Race thisRace=(Race)MUD.races.elementAt(r);
							if(raceNum==0)
								newRace=thisRace;
							else
								raceNum--;
						}
						if(newRace.ID().equals(new StdRace().ID()))
							newRace=null;
					}
					mob.location().show(mob,target,Affect.VISUAL_WNOISE,"<T-NAME> become(s) a "+newRace.name()+"!");
					success=beneficialAffect(mob,target,0);
				}
			}
		}
		else
			return beneficialFizzle(mob,target,"<S-NAME> form(s) a spell around <T-NAME>, but the spell fizzles.");

		// return whether it worked
		return success;
	}
}