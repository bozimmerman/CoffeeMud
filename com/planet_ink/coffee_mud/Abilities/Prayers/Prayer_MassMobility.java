package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_MassMobility extends Prayer
{
	public Prayer_MassMobility()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Mass Mobility";
		displayText="(Mass Mobility)";
		quality=Ability.BENEFICIAL_OTHERS;
		holyQuality=Prayer.HOLY_NEUTRAL;
		baseEnvStats().setLevel(20);

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Prayer_MassMobility();
	}



	public boolean okAffect(Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if((affect.amITarget(mob))
		&&(Util.bset(affect.targetCode(),Affect.MASK_MALICIOUS))
		&&(affect.targetMinor()==Affect.TYP_CAST_SPELL)
		&&(affect.tool()!=null)
		&&(affect.tool() instanceof Ability)
		&&(!mob.amDead()))
		{
			Ability A=(Ability)affect.tool();
			MOB newMOB=(MOB)CMClass.getMOB("StdMOB").newInstance();
			FullMsg msg=new FullMsg(newMOB,null,null,Affect.MSG_SIT,null);
			newMOB.recoverEnvStats();
			try
			{
				A.affectEnvStats(newMOB,newMOB.envStats());
				if((!Sense.aliveAwakeMobile(newMOB,true))
				   ||(!A.okAffect(msg)))
				{
					affect.addTrailerMsg(new FullMsg(mob,null,Affect.MSG_OK_VISUAL,"The mobile aura around <S-NAME> repels the "+A.name()+"."));
					return false;
				}
			}
			catch(Exception e)
			{}
		}
		return true;
	}


	public void affectCharStats(MOB affectedMOB, CharStats affectedStats)
	{
		super.affectCharStats(affectedMOB,affectedStats);
		affectedStats.setStat(CharStats.SAVE_PARALYSIS,affectedStats.getStat(CharStats.SAVE_PARALYSIS)+100);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		mob.tell("The aura of mobility around you fades.");
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		Room room=mob.location();
		if(room!=null)
		for(int i=0;i<room.numInhabitants();i++)
		{
			MOB target=room.fetchInhabitant(i);
			if(target==null) break;
			
			affectType=Affect.MSG_CAST_VERBAL_SPELL;
			if(auto) affectType=affectType|Affect.ACT_GENERAL;
			if(success)
			{
				// it worked, so build a copy of this ability,
				// and add it to the affects list of the
				// affected MOB.  Then tell everyone else
				// what happened.
				FullMsg msg=new FullMsg(mob,target,this,affectType,auto?"Mobility is invoked upon <T-NAME>.":"<S-NAME> pray(s) to <S-HIS-HER> god for an aura of mobility!");
				if(mob.location().okAffect(msg))
				{
					mob.location().send(mob,msg);
					beneficialAffect(mob,target,0);
				}
			}
			else
			{
				beneficialWordsFizzle(mob,target,"<S-NAME> pray(s) to <S-HIS-HER> god, but nothing happens.");
				return false;
			}
		}
		return success;
	}
}