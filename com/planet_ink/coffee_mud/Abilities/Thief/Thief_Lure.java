package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_Lure extends ThiefSkill
{
	public String ID() { return "Thief_Lure"; }
	public String name(){ return "Lure";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public int quality(){return Ability.INDIFFERENT;}
	private static final String[] triggerStrings = {"LURE"};
	public String[] triggerStrings(){return triggerStrings;}
	public Environmental newInstance(){	return new Thief_Lure();}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(commands.size()<2)
		{
			mob.tell("Lure whom which direction?");
			return false;
		}
		String str=(String)commands.lastElement();
		commands.removeElementAt(commands.size()-1);
		int dirCode=Directions.getGoodDirectionCode(str);
		if((dirCode<0)||(mob.location()==null)||(mob.location().getRoomInDir(dirCode)==null)||(mob.location().getExitInDir(dirCode)==null))
		{
			mob.tell("'"+str+"' is not a valid direction.");
			return false;
		}
		String direction=Directions.getInDirectionName(dirCode);
		
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		int levelDiff=target.envStats().level()-mob.envStats().level();

		boolean success=profficiencyCheck(-(levelDiff*(!Sense.canBeSeenBy(mob,target)?5:10)),auto);

		str="<S-NAME> lure(s) <T-NAME> "+direction+".";
		FullMsg msg=new FullMsg(mob,target,this,(auto?Affect.MASK_GENERAL:0)|Affect.MSG_SPEAK,str);
		if(mob.location().okAffect(mob,msg))
		{
			mob.location().send(mob,msg);
			if((success)&&(ExternalPlay.move(mob,dirCode,false,false))&&(Sense.canBeHeardBy(target,mob)))
				ExternalPlay.move(mob,dirCode,false,false);
		}
		return success;
	}

}