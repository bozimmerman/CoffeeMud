package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Chant_Reincarnation extends Chant
{
	public String ID() { return "Chant_Reincarnation"; }
	public String name(){ return "Reincarnation";}
	public String displayText(){return "(Reincarnation Geis)";}
	public int quality(){return Ability.OK_OTHERS;}
	public Environmental newInstance(){	return new Chant_Reincarnation();}
	public boolean canBeUninvoked(){return false;}
	protected int overrideMana(){return 200;}

	Race newRace=null;

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(newRace!=null)
		{
			if(affected.name().indexOf(" ")>0)
				affectableStats.setReplacementName("a "+newRace.name()+" called "+affected.name());
			else
				affectableStats.setReplacementName(affected.name()+" the "+newRace.name());
			newRace.setHeightWeight(affectableStats,'M');
		}
	}
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(newRace!=null)
			affectableStats.setMyRace(newRace);
	}

	public boolean tick(int tickID)
	{
		if(tickDown<=0)
		{
			// undo the affects of this spell
			if((affected==null)||(!(affected instanceof MOB)))
				return;
			MOB mob=(MOB)affected;
			if(canBeUninvoked())
			{
				mob.tell("Your reincarnation geis is lifted as your form solidifies.");
				if(newRace!=null)
					mob.baseCharStats().setMyRace(newRace);
			}
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		if(target.isMonster())
		{
			mob.tell("Your chant would have no effect on such a creature.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),"^S<S-NAME> chant(s) a reincarnation geis upon <T-NAMESELF>.^?");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,1800);
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> chant(s) for a reincarnation geis, but nothing happen(s).");

		return success;
	}
}