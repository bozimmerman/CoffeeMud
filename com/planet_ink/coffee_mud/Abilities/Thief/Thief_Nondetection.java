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
	
	
	public boolean okAffect(Environmental myHost, Affect msg)
	{
		if((affected!=null)&&(affected instanceof MOB))
		{
			if((msg.amISource((MOB)affected))
			&&((msg.sourceMinor()==Affect.TYP_ENTER)
			   ||(msg.sourceMinor()==Affect.TYP_LEAVE)
			   ||(msg.sourceMinor()==Affect.TYP_RECALL)))
			{
				active=false;
				affected.recoverEnvStats();
			}
		}
		return super.okAffect(myHost,msg);
	}
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(Sense.isHidden(affected)&&active)
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
