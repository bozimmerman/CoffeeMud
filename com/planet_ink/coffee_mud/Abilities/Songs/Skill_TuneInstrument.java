package com.planet_ink.coffee_mud.Abilities.Songs;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.*;

public class Skill_TuneInstrument extends StdAbility
{
	public String ID() { return "Skill_TuneInstrument"; }
	public String name(){ return "Tune Instrument";}
	protected int canAffectCode(){return CAN_ITEMS;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.INDIFFERENT;}
	private static final String[] triggerStrings = {"TUNEINSTRUMENT","TUNE"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.SKILL;}
	public Environmental newInstance(){	return new Skill_TuneInstrument();}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setAbility(affectableStats.ability()+2);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Item target=Play.getInstrument(mob,-1,true);
		if(target==null) return false;
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell(target.name()+" is already tuned.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSG_DELICATE_SMALL_HANDS_ACT,"<S-NAME> tune(s) <T-NAMESELF>.");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
			}
		}
		else
			mob.location().show(mob,target,CMMsg.MSG_OK_ACTION,"<T-NAME> attempt(s) to tune <T-NAMESELF>, but mess(es) up.");


		// return whether it worked
		return success;
	}
}