package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Skill_Swim extends StdAbility
{

	public Skill_Swim()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Swim";
		displayText="(in a wet place)";
		miscText="";

		triggerStrings.addElement("SWIM");

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(1);

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Skill_Swim();
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|Sense.IS_SWIMMING);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		int dirCode=Directions.getDirectionCode(Util.combine(commands,0));
		if(dirCode<0)
		{
			mob.tell("Swim where?");
			return false;
		}
		Room r2=mob.location();
		if((r2==null)||((r2.domainType()!=Room.DOMAIN_OUTDOORS_WATERSURFACE)&&(r2.domainType()!=Room.DOMAIN_OUTDOORS_UNDERWATER)))
		{
			Room r=mob.location().doors()[dirCode];
			if((r==null)||((r.domainType()!=Room.DOMAIN_OUTDOORS_WATERSURFACE)&&(r.domainType()!=Room.DOMAIN_OUTDOORS_UNDERWATER)))
			{
				mob.tell("There is no water to swim on that way.");
				return false;
			}
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);
		FullMsg msg=new FullMsg(mob,null,null,Affect.MSG_DELICATE_HANDS_ACT,null);
		if(mob.location().okAffect(msg))
		{
			mob.location().send(mob,msg);
			success=profficiencyCheck(0,auto);
			if(!success)
				mob.location().show(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> struggle(s) against the water, making no progress.");
			else
			{
				if(mob.fetchAffect(ID())==null)
				{
					mob.addAffect(this);
					mob.recoverEnvStats();
				}

				ExternalPlay.move(mob,dirCode,false);
				mob.delAffect(this);
				mob.recoverEnvStats();
			}
		}
		return success;
	}

	public boolean canBePracticedBy(MOB teacher, MOB student)
	{
		if(!super.canBePracticedBy(teacher,student))
			return false;
		if(student.location()==null)
			return false;
		Ability myAbility=student.fetchAbility(ID());
		if(myAbility.profficiency()<20)
			return true;
		if((student.location().domainType()!=Room.DOMAIN_OUTDOORS_UNDERWATER)
		 &&(student.location().domainType()!=Room.DOMAIN_OUTDOORS_WATERSURFACE))
		{
			student.tell("You need to be on or in the water to learn any more about swimming!");
			teacher.tell("You need to be on or in the water to teach more about swimming!");
			return false;
		}
		return true;
	}
}