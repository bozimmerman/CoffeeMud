package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Skill_Dirt extends StdAbility
{
	boolean doneTicking=false;

	public Skill_Dirt()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Dirt";
		displayText="(Dirt in your eyes)";
		miscText="";

		triggerStrings.addElement("DIRT");

		quality=Ability.MALICIOUS;

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(11);

		addQualifyingClass("Fighter",11);
		addQualifyingClass("Ranger",11);
		addQualifyingClass("Paladin",11);
		addQualifyingClass("Thief",22);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Skill_Dirt();
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(!doneTicking)
			affectableStats.setSensesMask(affectableStats.sensesMask()|Sense.CAN_SEE);
	}

	public int classificationCode()
	{
		return Ability.SKILL;
	}

	public boolean okAffect(Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		if((doneTicking)&&(affect.amISource(mob)))
			unInvoke();
		return true;
	}

	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		mob.tell("You can see again!");
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		if((mob.location().domainConditions()==Room.CONDITION_WET)
		 ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_AIR)
		 ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_CITY)
		 ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
		 ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE)
		 ||(mob.location().domainType()==Room.DOMAIN_INDOORS_MAGIC)
		 ||(mob.location().domainType()==Room.DOMAIN_INDOORS_STONE)
		 ||(mob.location().domainType()==Room.DOMAIN_INDOORS_CAVE)
		 ||(mob.location().domainType()==Room.DOMAIN_INDOORS_WOOD))
		{
			mob.tell("There's no dirt here to kick!");
			return false;
		}
		boolean success=profficiencyCheck(-(target.charStats().getDexterity()*3),auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,Affect.MSK_MALICIOUS_MOVE|Affect.TYP_JUSTICE|(auto?Affect.ACT_GENERAL:0),auto?"Dirt flys at <T-NAME>!":"<S-NAME> kick(s) dirt at <T-NAMESELF>.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(target,null,Affect.MSG_OK_VISUAL,"<S-NAME> is blinded!");
				maliciousAffect(mob,target,3,-1);
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to kick dirt at <T-NAMESELF>, but miss(es).");
		return success;
	}
}
