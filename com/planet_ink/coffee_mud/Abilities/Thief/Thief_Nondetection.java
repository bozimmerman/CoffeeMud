package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_Nondetection extends ThiefSkill
{
	public String ID() { return "Thief_Nondetection"; }
	public String name(){ return "Nondetection";}
	public String displayText(){ if(active)return "(Nondetectable)";else return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.OK_SELF;}
	public Environmental newInstance(){	return new Thief_Nondetection();}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	public boolean active=false;
	
	
	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return super.okAffect(myHost,affect);

		MOB mob=(MOB)affected;
		if((!Sense.isHidden(mob))&&(active))
		{
			active=false;
			mob.recoverEnvStats();
		}
		else
		if(affect.amISource(mob))
		{
			if(((Util.bset(affect.sourceMajor(),Affect.MASK_SOUND)
				 ||(affect.sourceMinor()==Affect.TYP_SPEAK)
				 ||(affect.sourceMinor()==Affect.TYP_ENTER)
				 ||(affect.sourceMinor()==Affect.TYP_LEAVE)
				 ||(affect.sourceMinor()==Affect.TYP_RECALL)))
			 &&(active)
			 &&(!Util.bset(affect.sourceMajor(),Affect.MASK_GENERAL))
			 &&(affect.sourceMinor()!=Affect.TYP_EXAMINESOMETHING)
			 &&(affect.sourceMajor()>0))
			{
				active=false;
				mob.recoverEnvStats();
			}
		}
		return super.okAffect(myHost,affect);
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(active&&((affected.baseEnvStats().disposition()&EnvStats.IS_HIDDEN)==0))
			affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_NOT_SEEN);
	}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID)) return false;
		if((affected!=null)&&(affected instanceof MOB))
		{
			if(Sense.isHidden(affected))
			{
				if(!active)
				{
					active=true;
					helpProfficiency((MOB)affected);
					affected.recoverEnvStats();
				}
			}
			else
			if(active)
			{
				active=false;
				affected.recoverEnvStats();
			}
		}
		return true;
	}
}
