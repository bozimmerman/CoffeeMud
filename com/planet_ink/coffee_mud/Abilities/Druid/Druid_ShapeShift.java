package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Druid_ShapeShift extends Druid
{
	Race newRace=null;
	String raceName="";
	private static String[] shapes={
		"Mouse",
		"Kitten",
		"Puppy",
		"Alley Cat",
		"Rottweiller",
		"Raven",
		"Wolf",
		"Snake",
		"Lion",
		"Giant Eagle"};
	private static String[] races={
		"Rodent",
		"Cat",
		"Dog",
		"Cat",
		"Dog",
		"Raven",
		"Wolf",
		"Snake",
		"GreatCat",
		"GreatBird"};
	
	public Druid_ShapeShift()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Shape Shift";

		baseEnvStats().setLevel(16);
		quality=Ability.OK_SELF;

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Druid_ShapeShift();
	}

	public void setMiscText(String newText)
	{
		if(newText.length()==0)
			newText=shapes[Dice.roll(1,shapes.length,-1)];
		super.setMiscText(newText);
	}
	
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if((newRace!=null)&&(affected instanceof MOB))
		{
			affectableStats.setReplacementName(raceName);
			newRace.setHeightWeight(affectableStats,(char)((MOB)affected).charStats().getStat(CharStats.GENDER));
		}
	}
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(newRace!=null) affectableStats.setMyRace(newRace);
	}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		super.unInvoke();
		mob.tell("You have reverted to your normal form.");
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Ability A=mob.fetchAffect(ID());
		if(A!=null)
		{
			A.unInvoke();
			return true;
		}
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,null,this,affectType,"<S-NAME> chant(s) to <S-HIM-HER>self...");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,mob,0);
				for(int i=0;i<shapes.length;i++)
				{
					if(text().equals(shapes[i]))
					{
						raceName=text();
						newRace=CMClass.getRace(text());
						if(("AEIOU").indexOf(raceName.charAt(0))>=0)
							raceName="An "+raceName;
						else
							raceName="A "+raceName;
					}
				}
				mob.makePeace();
			}
		}
		else
			beneficialWordsFizzle(mob,null,"<S-NAME> chant(s) to <S-HIM-HER>self, but nothing happens.");


		// return whether it worked
		return success;
	}
}