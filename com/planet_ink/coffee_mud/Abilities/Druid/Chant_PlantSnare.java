package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_PlantSnare extends Chant
{
	public String ID() { return "Chant_PlantSnare"; }
	public String name(){ return "Plant Snare";}
	public String displayText(){return "(Snared)";}
	public int quality(){return Ability.MALICIOUS;}
	public int maxRange(){return 2;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}
	public int amountRemaining=0;
	public Environmental newInstance(){	return new Chant_PlantSnare();}
	public long flags(){return Ability.FLAG_BINDING;}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_BOUND);
	}
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		// when this spell is on a MOBs Affected list,
		// it should consistantly prevent the mob
		// from trying to do ANYTHING except sleep
		if(msg.amISource(mob))
		{
			if((!Util.bset(msg.sourceMajor(),CMMsg.MASK_GENERAL))
			&&((Util.bset(msg.sourceMajor(),CMMsg.MASK_HANDS))
			||(Util.bset(msg.sourceMajor(),CMMsg.MASK_MOVE))))
			{
				if(mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> struggle(s) against the snaring plants."))
				{
					amountRemaining-=(mob.charStats().getStat(CharStats.STRENGTH)*4);
					if(amountRemaining<0)
						unInvoke();
				}
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked())
		{
			if(!mob.amDead())
				mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> manage(s) to break <S-HIS-HER> way free of the plants.");
			CommonMsgs.stand(mob,true);
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Hashtable h=properTargets(mob,givenTarget,auto);
		if(h==null)
		{
			mob.tell("There doesn't appear to be anyone here worth snaring.");
			return false;
		}
		Room room=mob.location();
		if((room.domainType()!=Room.DOMAIN_OUTDOORS_WOODS)
		&&(room.domainType()!=Room.DOMAIN_OUTDOORS_PLAINS)
		&&((room.myResource()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_WOODEN)
		&&((room.myResource()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_VEGETATION)
		&&(room.domainType()!=Room.DOMAIN_OUTDOORS_HILLS)
		&&(room.domainType()!=Room.DOMAIN_OUTDOORS_JUNGLE)
		&&(room.domainType()!=Room.DOMAIN_OUTDOORS_SWAMP))
		{
			mob.tell("There doesn't seem to be a large enough mass of plant life around here...\n\r");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			if(mob.location().show(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> chant(s) to the plants around <S-HIM-HER>.^?"))
			for(Enumeration f=h.elements();f.hasMoreElements();)
			{
				MOB target=(MOB)f.nextElement();

				// it worked, so build a copy of this ability,
				// and add it to the affects list of the
				// affected MOB.  Then tell everyone else
				// what happened.
				FullMsg msg=new FullMsg(mob,target,this,affectType(auto),null);
				if((mob.location().okMessage(mob,msg))&&(target.fetchEffect(this.ID())==null))
				{
					mob.location().send(mob,msg);
					if(msg.value()<=0)
					{
						amountRemaining=400;
						if(target.location()==mob.location())
						{
							success=maliciousAffect(mob,target,(adjustedLevel(mob)*10),-1);
							target.location().show(target,null,CMMsg.MSG_OK_ACTION,"<S-NAME> become(s) stuck as tangling mass of plant life grows onto <S-HIM-HER>!");
						}
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,null,"<S-NAME> chant(s), but the magic fades.");


		// return whether it worked
		return success;
	}
}