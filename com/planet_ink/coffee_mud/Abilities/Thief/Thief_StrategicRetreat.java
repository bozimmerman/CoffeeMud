package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_StrategicRetreat extends ThiefSkill
{
	public String ID() { return "Thief_StrategicRetreat"; }
	public String name(){ return "Strategic Retreat";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.OK_SELF;}
	private static final String[] triggerStrings = {"FREEFLEE","STRATEGICRETREAT"};
	public String[] triggerStrings(){return triggerStrings;}
	public Environmental newInstance(){	return new Thief_StrategicRetreat();}
	public int usageType(){return USAGE_MOVEMENT;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!mob.isInCombat())
		{
			mob.tell("You can only retreat from combat!");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		String where=Util.combine(commands,0);
		if(!success)
			CommonMsgs.flee(mob,where);
		else
		{
			int directionCode=-1;
			if(!where.equals("NOWHERE"))
			{
				if(where.length()==0)
				{
					Vector directions=new Vector();
					for(int i=0;i<Directions.NUM_DIRECTIONS;i++)
					{
						Exit thisExit=mob.location().getExitInDir(i);
						Room thisRoom=mob.location().getRoomInDir(i);
						if((thisRoom!=null)&&(thisExit!=null)&&(thisExit.isOpen()))
							directions.addElement(new Integer(i));
					}
					// up is last resort
					if(directions.size()>1)
						directions.removeElement(new Integer(Directions.UP));
					if(directions.size()>0)
					{
						directionCode=((Integer)directions.elementAt(Dice.roll(1,directions.size(),-1))).intValue();
						where=Directions.getDirectionName(directionCode);
					}
				}
				else
					directionCode=Directions.getGoodDirectionCode(where);
				if(directionCode<0)
				{
					mob.tell(CommonStrings.getScr("Movement","fleeerr2"));
					return false;
				}
				mob.makePeace();
				MUDTracker.move(mob,directionCode,true,false);
			}
		}
		return success;
	}
}
