package com.planet_ink.coffee_mud.Abilities.Fighter;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import java.util.*;

public class Fighter_BullRush extends StdAbility
{
	public String ID() { return "Fighter_BullRush"; }
	public String name(){ return "Bullrush";}
	public int minRange(){return 0;}
	public int maxRange(){return 1;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public int quality(){return Ability.INDIFFERENT;}
	private static final String[] triggerStrings = {"BULLRUSH"};
	public String[] triggerStrings(){return triggerStrings;}
	public Environmental newInstance(){	return new Fighter_BullRush();}
	public long flags(){return Ability.FLAG_MOVING;}
	public int usageType(){return USAGE_MOVEMENT;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(commands.size()<2)
		{
			mob.tell("Bullrush whom which direction?");
			return false;
		}
		if(mob.isInCombat())
		{
			mob.tell("You can only do this in the rage of combat!");
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

		boolean success=profficiencyCheck(-(levelDiff*5),auto);

		str="<S-NAME> bullrush(es) <T-NAME> "+direction+".";
		FullMsg msg=new FullMsg(mob,target,this,(auto?CMMsg.MASK_GENERAL:0)|CMMsg.MASK_MOVE|CMMsg.MASK_SOUND|CMMsg.MASK_HANDS|CMMsg.TYP_JUSTICE,str);
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			MOB M1=mob.getVictim();
			MOB M2=target.getVictim();
			mob.makePeace();
			target.makePeace();
			if((success)&&(ExternalPlay.move(mob,dirCode,false,false))&&(Sense.canBeHeardBy(target,mob)))
			{
				ExternalPlay.move(target,dirCode,false,false);
				mob.setVictim(M1);
				target.setVictim(M2);
			}
		}
		return success;
	}

}