package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_MarkInvisibility extends ThiefSkill
{
	public String ID() { return "Thief_MarkInvisibility"; }
	public String name(){ return "Invisibility to Mark";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.OK_SELF;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	public Environmental newInstance(){	return new Thief_MarkInvisibility();}
	public boolean active=false;
	public MOB mark=null;

	public MOB getMark(MOB mob)
	{
		Thief_Mark A=(Thief_Mark)mob.fetchAffect("Thief_Mark");
		if(A!=null)
			return A.mark;
		return null;
	}
	public int getMarkTicks(MOB mob)
	{
		Thief_Mark A=(Thief_Mark)mob.fetchAffect("Thief_Mark");
		if((A!=null)&&(A.mark!=null))
			return A.ticks;
		return -1;
	}
	
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(active)
			affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_INVISIBLE);
	}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID)) return false;
		if((affected!=null)&&(affected instanceof MOB))
		{
			MOB mob=(MOB)affected;
			mark=getMark(mob);
			if((mark!=null)
			&&(mob.location()!=null)
			&&(mob.location().isInhabitant(mark))
			&&((mob.fetchAbility(ID())==null)||profficiencyCheck(0,false)))
			{
				if(!active)
				{
					active=true;
					helpProfficiency(mob);
					mob.recoverEnvStats();
				}
			}
			else
			if(active)
			{
				active=false;
				mob.recoverEnvStats();
			}
		}
		return true;
	}
}
