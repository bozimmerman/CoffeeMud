package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Skill_Swim extends StdAbility
{
	public String ID() { return "Skill_Swim"; }
	public String name(){ return "Swim";}
	public String displayText(){ return "(Swimming)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.INDIFFERENT;}
	private static final String[] triggerStrings = {"SWIM"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.SKILL;}
	public Environmental newInstance(){	return new Skill_Swim();}
	protected int trainsRequired(){return 0;}
	protected int practicesRequired(){return 2;}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_SWIMMING);
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
		if((r2==null)
		||((r2.domainType()!=Room.DOMAIN_OUTDOORS_WATERSURFACE)
		&&(r2.domainType()!=Room.DOMAIN_OUTDOORS_UNDERWATER)
		&&(r2.domainType()!=Room.DOMAIN_INDOORS_UNDERWATER)
		&&(r2.domainType()!=Room.DOMAIN_INDOORS_WATERSURFACE)))
		{
			Room r=mob.location().getRoomInDir(dirCode);
			if((r==null)||((r.domainType()!=Room.DOMAIN_OUTDOORS_WATERSURFACE)
						   &&(r.domainType()!=Room.DOMAIN_OUTDOORS_UNDERWATER)
						   &&(r.domainType()!=Room.DOMAIN_INDOORS_UNDERWATER)
						   &&(r.domainType()!=Room.DOMAIN_INDOORS_WATERSURFACE)
						   ))
			{
				mob.tell("There is no water to swim on that way.");
				return false;
			}
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);
		FullMsg msg=new FullMsg(mob,null,this,Affect.MSG_NOISYMOVEMENT,null);
		if(mob.location().okAffect(mob,msg))
		{
			mob.location().send(mob,msg);
			success=profficiencyCheck(0,auto);
			if(!success)
				mob.location().show(mob,null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> struggle(s) against the water, making no progress.");
			else
			{
				if(mob.fetchAffect(ID())==null)
				{
					mob.addAffect(this);
					mob.recoverEnvStats();
				}

				ExternalPlay.move(mob,dirCode,false,false);
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
		 &&(student.location().domainType()!=Room.DOMAIN_OUTDOORS_WATERSURFACE)
		 &&(student.location().domainType()!=Room.DOMAIN_INDOORS_UNDERWATER)
		 &&(student.location().domainType()!=Room.DOMAIN_INDOORS_WATERSURFACE))
		{
			student.tell("You need to be on or in the water to learn any more about swimming!");
			teacher.tell("You need to be on or in the water to teach more about swimming!");
			return false;
		}
		return true;
	}
}