package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Skill_Climb extends StdAbility
{
	public String ID() { return "Skill_Climb"; }
	public String name(){ return "Climb";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.INDIFFERENT;}
	private static final String[] triggerStrings = {"CLIMB"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.SKILL;}
	protected int trainsRequired(){return 0;}
	protected int practicesRequired(){return 2;}
	public Environmental newInstance(){	return new Skill_Climb();}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_CLIMBING);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		int dirCode=Directions.getDirectionCode(Util.combine(commands,0));
		if(dirCode<0)
		{
			mob.tell("Climb where?");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);
		FullMsg msg=new FullMsg(mob,null,this,Affect.MSG_NOISYMOVEMENT,null);
		if(mob.location().okAffect(msg))
		{
			mob.location().send(mob,msg);
			success=profficiencyCheck(0,auto);

			if(mob.fetchAffect(ID())==null)
			{
				mob.addAffect(this);
				mob.recoverEnvStats();
			}

			ExternalPlay.move(mob,dirCode,false,false);
			mob.delAffect(this);
			mob.recoverEnvStats();
			if(!success)
				mob.location().affect(new FullMsg(mob,mob.location(),Affect.MASK_MOVE|Affect.TYP_GENERAL,null));
		}
		return success;
	}

}
