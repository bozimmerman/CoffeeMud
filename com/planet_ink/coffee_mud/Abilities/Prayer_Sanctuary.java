package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import java.util.*;

public class Prayer_Sanctuary extends Prayer
{
	private int oldHP=0;

	public Prayer_Sanctuary()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Sanctuary";
		displayText="(Sanctuary)";

		isNeutral=true;

		baseEnvStats().setLevel(13);

		addQualifyingClass(new Cleric().ID(),baseEnvStats().level());
		addQualifyingClass(new Paladin().ID(),baseEnvStats().level()+4);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Prayer_Sanctuary();
	}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		mob.tell("The sanctuary around you fades.");
	}

	public boolean okAffect(Affect affect)
	{
		if(!super.okAffect(affect))
			return false;

		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if(affect.amITarget(mob))
		{
			switch(affect.targetType())
			{
			case Affect.STRIKE:
				switch(affect.targetCode())
				{
				case Affect.STRIKE_HANDS:
					oldHP=mob.curState().getHitPoints();
					break;
				}
				break;

			}
		}
		return true;
	}

	public void affect(Affect affect)
	{
		super.affect(affect);

		if((affected==null)||(!(affected instanceof MOB)))
			return;

		MOB mob=(MOB)affected;
		if(affect.amITarget(mob))
		{
			switch(affect.targetType())
			{
			case Affect.STRIKE:
				switch(affect.targetCode())
				{
				case Affect.STRIKE_HANDS:
					if(oldHP>mob.curState().getHitPoints())
					{
						int recovery=(int)Math.round(Util.div((mob.curState().getHitPoints()-oldHP),2.0));
						if(recovery>=0)
							mob.curState().adjHitPoints(recovery,mob.maxState());
					}

					break;
				}
				break;
			}
		}
	}
	public boolean invoke(MOB mob, Vector commands)
	{
		MOB target=this.getTarget(mob,commands);
		if(target==null) return false;

		if(!super.invoke(mob,commands))
			return false;

		boolean success=profficiencyCheck(0);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,"<S-NAME> pray(s) that <T-NAME> be given sanctuary from harm.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,target,Affect.VISUAL_WNOISE,"A white aura surrounds <T-NAME>.");
				beneficialAffect(mob,target,0);
			}
		}
		else
			return beneficialFizzle(mob,target,"<S-NAME> pray(s) for sanctuary, but <S-HIS-HER> plea is not answered.");


		// return whether it worked
		return success;
	}
}
