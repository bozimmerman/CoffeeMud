package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_DetectLife extends Prayer
{
	public Prayer_DetectLife()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Detect Life";
		baseEnvStats().setLevel(1);

		addQualifyingClass("Cleric",baseEnvStats().level());
		addQualifyingClass("Paladin",baseEnvStats().level()+4);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Prayer_DetectLife();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		// now see if it worked
		boolean success=profficiencyCheck(0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType,auto?"":"<S-NAME> listen(s) for a message from <S-HIS-HER> god.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				String last="";
				String dirs="";
				for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
				{
					Room R=mob.location().doors()[d];
					Exit E=mob.location().exits()[d];
					if((R!=null)&&(E!=null))
					{
						if(R.numInhabitants()>0)
						{
							if(last.length()>0)
								dirs+=", "+last;
							last=Directions.getFromDirectionName(d);
						}
					}
				}
				if(mob.location().numInhabitants()>1)
				{
					if(last.length()>0)
						dirs+=", "+last;
					last="here";
				}

				if((dirs.length()==0)&&(last.length()==0))
					mob.tell("You do not sense any life beyond your own.");
				else
				if(dirs.length()==0)
					mob.tell("You sense a life force coming from "+last+".");
				else
					mob.tell("You sense a life force coming from "+dirs.substring(2)+", and "+last+".");
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> listen(s) to <S-HIS-HER> god for a message, but there is no answer.");

		// return whether it worked
		return success;
	}
}
