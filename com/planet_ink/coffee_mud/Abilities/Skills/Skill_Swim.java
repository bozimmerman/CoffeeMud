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
	protected int trainsRequired(){return CommonStrings.getIntVar(CommonStrings.SYSTEMI_COMMONTRAINCOST);}
	protected int practicesRequired(){return CommonStrings.getIntVar(CommonStrings.SYSTEMI_COMMONPRACCOST);}

	public boolean placeToSwim(Room r2)
	{
		if((r2==null)
		||((r2.domainType()!=Room.DOMAIN_OUTDOORS_WATERSURFACE)
		&&(r2.domainType()!=Room.DOMAIN_OUTDOORS_UNDERWATER)
		&&(r2.domainType()!=Room.DOMAIN_INDOORS_UNDERWATER)
		&&(r2.domainType()!=Room.DOMAIN_INDOORS_WATERSURFACE)))
			return false;
		return true;
	}
	public boolean placeToSwim(Environmental E)
	{ return placeToSwim(CoffeeUtensils.roomLocation(E));}
	
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
		if(!placeToSwim(mob.location()))
		{
			Room r=mob.location().getRoomInDir(dirCode);
			if(!placeToSwim(r))
			{
				mob.tell("There is no water to swim on that way.");
				return false;
			}
		}

		if((mob.riding()!=null)
		&&(mob.riding().rideBasis()!=Rideable.RIDEABLE_WATER)
		&&(mob.riding().rideBasis()!=Rideable.RIDEABLE_AIR))
		{
			mob.tell("You need to get off "+mob.riding().name()+" first!");
			return false;
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
					mob.addAffect(this);
				mob.recoverEnvStats();

				ExternalPlay.move(mob,dirCode,false,false);
			}
			mob.delAffect(this);
			mob.recoverEnvStats();
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
		if(!placeToSwim(student.location()))
		{
			student.tell("You need to be on or in the water to learn any more about swimming!");
			teacher.tell("You need to be on or in the water to teach more about swimming!");
			return false;
		}
		return true;
	}
}