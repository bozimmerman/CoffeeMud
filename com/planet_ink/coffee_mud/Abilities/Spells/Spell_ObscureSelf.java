package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_ObscureSelf extends Spell
{
	public String ID() { return "Spell_ObscureSelf"; }
	public String name(){return "Obscure Self";}
	public String displayText(){return "(Obscure Self)";}
	public int quality(){ return OK_SELF;}
	protected int canAffectCode(){return CAN_MOBS;}
	public Environmental newInstance(){	return new Spell_ObscureSelf();	}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_ILLUSION;}
	private final static String[][] stuff={
		{"<S-NAME>","<T-NAME>","someone"},
		{"<S-HIS-HER>","<T-HIS-HER>","him or her"},
		{"<S-HIM-HER>","<T-HIS-HER>","his or her"},
		{"<S-NAMESELF>","<T-NAMESELF>","someone"},
		{"<S-HE-SHE>","<T-HE-SHE>","he or she"},
		{"<S-YOUPOSS>","<T-YOUPOSS>","someone's"},
		{"<S-HIM-HERSELF>","<T-HIM-HERSELF>","him or herself"},
		{"<S-HIS-HERSELF>","<T-HIS-HERSELF>","his or herself"}
	};


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
				for(int i=0;i<stuff.length;i++)
				{
					x=othersMessage.indexOf(stuff[i][1]);
					while(x>=0)
					{
						somethingsChanged=true;
						othersMessage=othersMessage.substring(0,x)+stuff[i][2]+othersMessage.substring(x+(stuff[i][1]).length());
						x=othersMessage.indexOf(stuff[i][1]);
					}
				}
			}
			if((!affect.amISource(mob))&&(sourceMessage!=null))
			{
				for(int i=0;i<stuff.length;i++)
				{
					x=sourceMessage.indexOf(stuff[i][1]);
					while(x>=0)
					{
						somethingsChanged=true;
						sourceMessage=sourceMessage.substring(0,x)+stuff[i][2]+sourceMessage.substring(x+(stuff[i][1]).length());
						x=sourceMessage.indexOf(stuff[i][1]);
					}
				}
			}
		}
		if(affect.amISource(mob))
		{
			if(othersMessage!=null)
			{
				for(int i=0;i<stuff.length;i++)
				{
					x=othersMessage.indexOf(stuff[i][0]);
					while(x>=0)
					{
						somethingsChanged=true;
						othersMessage=othersMessage.substring(0,x)+stuff[i][2]+othersMessage.substring(x+(stuff[i][0]).length());
						x=othersMessage.indexOf(stuff[i][0]);
					}
				}
			}
			if((!affect.amITarget(mob))&&(targetMessage!=null))
			{
				for(int i=0;i<stuff.length;i++)
				{
					x=targetMessage.indexOf(stuff[i][0]);
					while(x>=0)
					{
						somethingsChanged=true;
						targetMessage=targetMessage.substring(0,x)+stuff[i][2]+targetMessage.substring(x+(stuff[i][0]).length());
						x=targetMessage.indexOf(stuff[i][0]);
					}
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

		if(canBeUninvoked())
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
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> whisper(s) to <S-HIS-HERSELF>.^?");
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
