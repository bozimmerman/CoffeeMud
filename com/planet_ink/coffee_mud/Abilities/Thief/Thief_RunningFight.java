package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_RunningFight extends ThiefSkill
{
	public String ID() { return "Thief_RunningFight"; }
	public String name(){ return "Running Fight";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.OK_SELF;}
	public Environmental newInstance(){	return new Thief_RunningFight();}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	private MOB lastOpponent=null;

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(lastOpponent!=null))
		{
			MOB mob=(MOB)affected;
			if((mob.location()!=null)&&(mob.location().isInhabitant(lastOpponent)))
			{
				mob.setVictim(lastOpponent);
				lastOpponent.setVictim(mob);
				lastOpponent=null;
			}
		}
		super.executeMsg(myHost,msg);
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return super.okMessage(myHost,msg);

		MOB mob=(MOB)affected;
		if(msg.amISource(mob)
		&&(msg.targetMinor()==CMMsg.TYP_LEAVE)
		&&(mob.isInCombat())
		&&(mob.getVictim()!=null)
		&&(msg.target()!=null)
		&&(msg.target() instanceof Room)
		&&(msg.tool()!=null)
		&&(msg.tool() instanceof Exit)
		&&((mob.fetchAbility(ID())==null)||profficiencyCheck(0,false))
		&&(Dice.rollPercentage()>mob.getVictim().charStats().getSave(CharStats.SAVE_TRAPS))
		&&(Dice.rollPercentage()>mob.getVictim().charStats().getSave(CharStats.SAVE_MIND)))
		{
			MOB M=mob.getVictim();
			if((M==null)||(M.getVictim()!=mob))
			{
				mob.tell(M,null,null,"<S-NAME> is not fighting you!");
				return false;
			}
			int dir=-1;
			for(int i=0;i<Directions.NUM_DIRECTIONS;i++)
			{
				if(mob.location().getRoomInDir(i)!=null)
				{
					if((mob.location().getRoomInDir(i)!=null)
					&&(mob.location().getReverseExit(i)==msg.tool()))
					{
						dir=i; break;
					}
				}
			}
			if(dir<0) return super.okMessage(myHost,msg);
			mob.makePeace();
			if(MUDTracker.move(M,dir,false,false))
			{
				M.setVictim(mob);
				lastOpponent=M;
			}
			else
			{
				M.setVictim(mob);
				mob.setVictim(M);
			}
		}
		return super.okMessage(myHost,msg);
	}
}
