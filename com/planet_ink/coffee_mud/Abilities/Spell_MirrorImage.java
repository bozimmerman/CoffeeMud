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

public class Spell_MirrorImage extends Spell
	implements IllusionistDevotion
{
	private	Random randomizer = new Random(System.currentTimeMillis());
	private int numberOfImages = 0;

	public Spell_MirrorImage()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Mirror Image";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Mirror Image spell)";


		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(11);

		addQualifyingClass(new Mage().ID(),11);
		addQualifyingClass(new Ranger().ID(),baseEnvStats().level()+4);

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public boolean okAffect(Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		if(affect.amITarget(mob))
		{
			switch(affect.targetCode())
			{
			case Affect.STRIKE_HANDS:
				if(invoker()!=null)
				{
					if(numberOfImages <= 0)
					{
						unInvoke();
						return true;
					}
					int numberOfTargets = numberOfImages + 1;
					if(randomizer.nextInt() % numberOfTargets == 0)
					{
						FullMsg msg=new FullMsg(mob,affect.source(),null,Affect.VISUAL_WNOISE,Affect.VISUAL_WNOISE,Affect.VISUAL_WNOISE,"<T-NAME> attack(s) a mirrored image!");
						mob.location().send(mob,msg);
						numberOfImages--;
						return false;
					}
				}
				break;
			default:
				break;
			}
		}
		return true;
	}

	public Environmental newInstance()
	{
		return new Spell_MirrorImage();
	}

	public void affectEnvStats(Environmental affected, Stats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setArmor(affectableStats.armor() - 10);
	}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		mob.tell("Your mirror images fade away.");
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
			for(int j=0; j < baseEnvStats().level(); j++)
				numberOfImages += randomizer.nextInt() % 4 + 1;
			FullMsg msg=new FullMsg(mob,target,this,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,"<S-NAME> cast(s) a spell at <T-NAME>, and suddenly " + numberOfImages + " copies appear.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
			}
		}
		else
		{
			numberOfImages = 0;
			return beneficialFizzle(mob,target,"<S-NAME> chant(s) reflectively to <T-NAME>, but nothing more happens.");
		}
		// return whether it worked
		return success;
	}
}
