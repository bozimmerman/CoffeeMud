package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_ImprovedPolymorph extends Spell
{

	Race newRace=null;
	public Spell_ImprovedPolymorph()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Improved Polymorph";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Improved Polymorph)";

		canBeUninvoked=true;
		isAutoinvoked=false;
		quality=Ability.INDIFFERENT;

		baseEnvStats().setLevel(15);

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_ImprovedPolymorph();
	}
	
	public int classificationCode()
	{
		return Ability.SPELL|Ability.DOMAIN_TRANSMUTATION;
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(newRace!=null)
		{
			if(affected.name().indexOf(" ")>0)
				affectableStats.setReplacementName("a "+newRace.name()+" called "+affected.name());
			else
				affectableStats.setReplacementName(affected.name()+" the "+newRace.name());
			newRace.setHeightWeight(affectableStats,'M');
		}
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



	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(commands.size()==0)
		{
			mob.tell("You need to specify what to turn your target into!");
			return false;
		}
		String race=(String)commands.lastElement();
		commands.removeElement(commands.lastElement());
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		if(target==mob)
		{
			mob.tell("You cannot hold enough energy to cast this on yourself.");
			return false;
		}
		Race R=CMClass.getRace(race);
		if(R==null)
		{
			mob.tell("You can't turn "+target.name()+" into a '"+race+"'!");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		int targetStatTotal=0;
		for(int s=0;s<CharStats.NUM_BASE_STATS;s++)
			targetStatTotal+=target.baseCharStats().getStat(s);
			
		MOB fakeMOB=CMClass.getMOB("StdMOB");
		for(int s=0;s<CharStats.NUM_BASE_STATS;s++)
			fakeMOB.baseCharStats().setStat(s,target.baseCharStats().getStat(s));
		fakeMOB.baseCharStats().setMyRace(R);
		fakeMOB.recoverCharStats();
		fakeMOB.recoverEnvStats();
		fakeMOB.recoverMaxState();
		int fakeStatTotal=0;
		for(int s=0;s<CharStats.NUM_BASE_STATS;s++)
			fakeStatTotal+=fakeMOB.charStats().getStat(s);

		int statDiff=targetStatTotal-fakeStatTotal;
		if(statDiff<0) statDiff=statDiff*-1;
		int levelDiff=mob.envStats().level()-target.envStats().level();
		boolean success=profficiencyCheck((levelDiff*5)-(statDiff*5),auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,affectType,auto?"":"<S-NAME> form(s) an improved spell around <T-NAMESELF>.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
				{
					newRace=R;
					mob.location().show(target,null,Affect.MSG_OK_VISUAL,"<S-NAME> become(s) a "+newRace.name()+"!");
					success=beneficialAffect(mob,target,0);
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> form(s) an improved spell around <T-NAMESELF>, but the spell fizzles.");

		// return whether it worked
		return success;
	}
}