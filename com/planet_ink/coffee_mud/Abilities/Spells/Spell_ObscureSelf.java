package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_ObscureSelf extends Spell
{
	public Spell_ObscureSelf()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Obscure Self";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Obscure Self)";

		canAffectCode=Ability.CAN_MOBS;
		canTargetCode=Ability.CAN_MOBS;

		quality=Ability.OK_SELF;
		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(2);

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_ObscureSelf();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.DOMAIN_ILLUSION;
	}


	public boolean okAffect(Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		
		String othersMessage=affect.othersMessage();
		String sourceMessage=affect.sourceMessage();
		String targetMessage=affect.targetMessage();
		boolean somethingsChanged=false;
		int x=0;
		if((affect.amITarget(mob))&&((affect.targetCode()&Affect.MASK_HURT)==0))
		{
			if((!affect.amISource(mob))&&((affect.targetMinor()==Affect.TYP_EXAMINESOMETHING)
										||(affect.targetMinor()==Affect.TYP_READSOMETHING)))
			{
				affect.source().tell("He or she is too vague to make out any details.");
				return false;
			}

			if(othersMessage!=null)
			{
				x=othersMessage.indexOf("<T-NAME>");
				if(x>=0)
				{
					somethingsChanged=true;
					othersMessage=othersMessage.substring(0,x)+"someone"+othersMessage.substring(x+("<T-NAME>").length());
				}
				x=othersMessage.indexOf("<T-HIS-HER>");
				if(x>=0)
				{
					somethingsChanged=true;
					othersMessage=othersMessage.substring(0,x)+"his or her"+othersMessage.substring(x+("<T-HIS-HER>").length());
				}
				x=othersMessage.indexOf("<T-NAMESELF>");
				if(x>=0)
				{
					somethingsChanged=true;
					othersMessage=othersMessage.substring(0,x)+"someone"+othersMessage.substring(x+("<T-NAMESELF>").length());
				}
			}
			if((!affect.amISource(mob))&&(sourceMessage!=null))
			{
				x=sourceMessage.indexOf("<T-NAME>");
				if(x>=0)
				{
					somethingsChanged=true;
					sourceMessage=sourceMessage.substring(0,x)+"someone"+sourceMessage.substring(x+("<T-NAME>").length());
				}
				x=sourceMessage.indexOf("<T-HIS-HER>");
				if(x>=0)
				{
					somethingsChanged=true;
					sourceMessage=sourceMessage.substring(0,x)+"his or her"+sourceMessage.substring(x+("<T-HIS-HER>").length());
				}
				x=sourceMessage.indexOf("<T-NAMESELF>");
				if(x>=0)
				{
					somethingsChanged=true;
					sourceMessage=sourceMessage.substring(0,x)+"someone"+sourceMessage.substring(x+("<T-NAMESELF>").length());
				}
			}
		}
		if(affect.amISource(mob))
		{
			if(othersMessage!=null)
			{
				x=othersMessage.indexOf("<S-NAME>");
				if(x>=0)
				{
					somethingsChanged=true;
					othersMessage=othersMessage.substring(0,x)+"someone"+othersMessage.substring(x+("<S-NAME>").length());
				}
				x=othersMessage.indexOf("<S-HIS-HER>");
				if(x>=0)
				{
					somethingsChanged=true;
					othersMessage=othersMessage.substring(0,x)+"his or her"+othersMessage.substring(x+("<S-HIS-HER>").length());
				}
			}
			if((!affect.amITarget(mob))&&(targetMessage!=null))
			{
				x=targetMessage.indexOf("<S-NAME>");
				if(x>=0)
				{
					somethingsChanged=true;
					targetMessage=targetMessage.substring(0,x)+"someone"+targetMessage.substring(x+("<S-NAME>").length());
				}
				x=targetMessage.indexOf("<S-HIS-HER>");
				if(x>=0)
				{
					somethingsChanged=true;
					targetMessage=targetMessage.substring(0,x)+"his or her"+targetMessage.substring(x+("<S-HIS-HER>").length());
				}
			}
		}
		if(somethingsChanged)
			affect.modify(affect.source(),affect.target(),affect.tool(),affect.sourceCode(),sourceMessage,affect.targetCode(),targetMessage,affect.othersCode(),othersMessage);
		return true;
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		super.unInvoke();

		if(canBeUninvoked)
			mob.tell("You begin to feel a bit less obscure.");
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=mob;

		if(target.fetchAffect(ID())!=null)
		{
			mob.tell("You are already obscure.");
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
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,affectType,auto?"":"^S<S-NAME> whisper(s) to <S-HIS-HERSELF>.^?");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,mob,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> whisper(s) to <S-HIS-HERSELF>, but nothing happens.");
		// return whether it worked
		return success;
	}
}
