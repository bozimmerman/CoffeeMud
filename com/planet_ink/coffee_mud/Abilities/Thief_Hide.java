package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import java.util.*;

public class Thief_Hide extends ThiefSkill
{
	public Thief_Hide()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Hide";
		displayText="(Ability to hide)";
		miscText="";

		triggerStrings.addElement("HIDE");

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(3);

		addQualifyingClass(new Thief().ID(),3);
		addQualifyingClass(new Bard().ID(),3);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Thief_Hide();
	}

	/** this method defines how this thing responds
	 * to environmental changes.  It may handle any
	 * and every affect listed in the Affect class
	 * from the given Environmental source */
	public void affect(Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;

		MOB mob=(MOB)affected;

		if(affect.amISource(mob))
		{
			switch(affect.sourceType())
			{
			case Affect.VISUAL:
				switch(affect.sourceCode())
				{
				case Affect.VISUAL_WNOISE:
					unInvoke();
					break;
				default:
					break;
				}
				break;
			case Affect.SOUND:
				unInvoke();
				break;
			case Affect.AIR:
				break;
			case Affect.TASTE:
				unInvoke();
				break;
			case Affect.HANDS:
				unInvoke();
				break;
			case Affect.STRIKE:
				unInvoke();
				break;
			case Affect.MOVE:
				unInvoke();
				break;
			default:
				break;
			}
		}
		return;
	}

	public void affectEnvStats(Environmental affected, Stats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|Sense.IS_HIDDEN);
		if(Sense.isSneaking(affected))
			affectableStats.setDisposition(affectableStats.disposition()-Sense.IS_SNEAKING);
	}

	public boolean invoke(MOB mob, Vector commands)
	{
		if(!super.invoke(mob,commands))
			return false;

		if(mob.fetchAffect(this.ID())!=null)
		{
			mob.tell("You are already hiding.");
			return false;
		}

		if(mob.isInCombat())
		{
			mob.tell("Not while in combat!");
			return false;
		}

		String str="You creep into a shadow and remain completely still.";

		boolean success=profficiencyCheck(0);

		if(!success)
		{
			FullMsg msg=new FullMsg(mob,null,null,Affect.VISUAL_WNOISE,str,Affect.NO_EFFECT,null,Affect.VISUAL_WNOISE,"<S-NAME> attempt(s) to hide and fail(s).");
			if(mob.location().okAffect(msg))
				mob.location().send(mob,msg);
		}
		else
		{
			FullMsg msg=new FullMsg(mob,null,null,Affect.HANDS_DELICATE,str,Affect.NO_EFFECT,null,Affect.VISUAL_WNOISE,null);
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				invoker=mob;
				Ability newOne=(Ability)this.copyOf();
				mob.addAffect(newOne);
				mob.recoverEnvStats();
			}
			else
				success=false;
		}
		return success;
	}
}
