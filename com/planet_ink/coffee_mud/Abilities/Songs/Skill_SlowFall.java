package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Skill_SlowFall extends BardSkill
{
	public String ID() { return "Skill_SlowFall"; }
	public String name(){return "Slow Fall";}
	public String displayText(){return activated?"(Slow Fall)":"";}
	public int quality(){ return INDIFFERENT;}
	protected int canAffectCode(){return CAN_MOBS;}
	public Environmental newInstance(){	return new Skill_SlowFall();}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	public int classificationCode(){return Ability.SKILL;}
	public boolean activated=false;

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(activated) affectableStats.setWeight(0);
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(affected!=null)
		{
			if((affected.fetchEffect("Falling")!=null)
			   &&((!(affected instanceof MOB))
				  ||(((MOB)affected).fetchAbility(ID())==null)
				  ||profficiencyCheck(0,false)))
			{
				activated=true;
				affected.recoverEnvStats();
				if(affected instanceof MOB)
					helpProfficiency((MOB)affected);
			}
			else
			if(activated)
			{
				activated=false;
				affected.recoverEnvStats();
			}
		}
		return super.tick(ticking,tickID);
	}
}
