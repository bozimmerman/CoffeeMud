package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_ImprovedHiding extends ThiefSkill
{
	public String ID() { return "Thief_ImprovedHiding"; }
	public String name(){ return "Improved Hiding";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.OK_SELF;}
	public Environmental newInstance(){	return new Thief_ImprovedHiding();}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	public boolean active=false;

	public void improve(MOB mob, boolean yesorno)
	{
		Ability A=mob.fetchAffect("Thief_Hide");
		if(A!=null)
		{
			if(yesorno)
				A.setAbilityCode(1);
			else
				A.setAbilityCode(0);
		}
	}
	
	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return super.okAffect(myHost,affect);

		MOB mob=(MOB)affected;
		if((!Sense.isHidden(mob))&&(active))
		{
			active=false;
			improve(mob,false);
			mob.recoverEnvStats();
		}
		return super.okAffect(myHost,affect);
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
					improve((MOB)affected,true);
				}
			}
			else
			if(active)
			{
				active=false;
				improve((MOB)affected,false);
			}
		}
		return true;
	}
}
