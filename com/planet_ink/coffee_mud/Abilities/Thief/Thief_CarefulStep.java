package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_CarefulStep extends ThiefSkill
{
	public String ID() { return "Thief_CarefulStep"; }
	public String name(){ return "Careful Step";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}
	public int castingTime(){return 2;}
	public int combatCastingTime(){return 2;}
	public int quality(){return Ability.INDIFFERENT;}
	private static final String[] triggerStrings = {"CARESTEP","CAREFULSTEP"};
	public String[] triggerStrings(){return triggerStrings;}
	public Environmental newInstance(){	return new Thief_CarefulStep();}
	public int usageType(){return USAGE_MOVEMENT;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		String dir=Util.combine(commands,0);
		if(commands.size()>0) dir=(String)commands.lastElement();
		int dirCode=Directions.getGoodDirectionCode(dir);
		if(dirCode<0)
		{
			mob.tell("Step where?");
			return false;
		}
		if(mob.isInCombat())
		{
			mob.tell("Not while you are fighting!");
			return false;
		}

		if((mob.location().getRoomInDir(dirCode)==null)||(mob.location().getExitInDir(dirCode)==null))
		{
			mob.tell("Step where?");
			return false;
		}

		HashSet H=mob.getGroupMembers(new HashSet());
		int highestLevel=0;
		for(int i=0;i<mob.location().numInhabitants();i++)
		{
			MOB M=mob.location().fetchInhabitant(i);
			if((M!=null)&&((M!=mob)&&(!H.contains(M)))&&(highestLevel<M.envStats().level()))
				highestLevel=mob.envStats().level();
		}
		int levelDiff=mob.envStats().level()-highestLevel;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=false;
		FullMsg msg=new FullMsg(mob,null,this,auto?CMMsg.MSG_OK_VISUAL:CMMsg.MSG_DELICATE_HANDS_ACT,"<S-NAME> walk(s) carefully "+Directions.getDirectionName(dirCode)+".");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			if(levelDiff<0)
				levelDiff=levelDiff*8;
			else
				levelDiff=levelDiff*10;
			success=profficiencyCheck(mob,levelDiff,auto);
			int oldDex=mob.baseCharStats().getStat(CharStats.DEXTERITY);
			if(success)
				mob.baseCharStats().setStat(CharStats.DEXTERITY,oldDex+100);
			mob.recoverCharStats();
			MUDTracker.move(mob,dirCode,false,false);
			if(oldDex!=mob.baseCharStats().getStat(CharStats.DEXTERITY))
				mob.baseCharStats().setStat(CharStats.DEXTERITY,oldDex);
			mob.recoverCharStats();
		}
		return success;
	}

}
