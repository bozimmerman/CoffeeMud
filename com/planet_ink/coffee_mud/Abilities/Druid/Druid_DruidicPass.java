package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Druid_DruidicPass extends StdAbility
{
	public String ID() { return "Druid_DruidicPass"; }
	public String name(){ return "Druidic Pass";}
	public String displayText(){return "(druidic passage)";}
	public int quality(){return Ability.OK_SELF;}
	private static final String[] triggerStrings = {"PASS"};
	public String[] triggerStrings(){return triggerStrings;}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){	return new Druid_DruidicPass();	}
	
	public int classificationCode()
	{
		return Ability.SKILL;
	}
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_SNEAKING);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_INVISIBLE);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{

		if((mob.location().domainType()&Room.INDOORS)>0)
		{
			mob.tell("You must be outdoors to perform the Druidic Pass.");
			return false;
		}
		if(mob.location().domainType()==Room.DOMAIN_OUTDOORS_CITY)
		{
			mob.tell("You must be in the wild to perform the Druidic Pass.");
			return false;
		}
		String whatToOpen=Util.combine(commands,0);
		int dirCode=Directions.getGoodDirectionCode(whatToOpen);
		if(dirCode<0)
		{
			mob.tell("Pass which direction?!");
			return false;
		}

		Exit exit=mob.location().getExitInDir(dirCode);
		Room room=mob.location().getRoomInDir(dirCode);

		if((exit==null)||(room==null)||((exit!=null)&&(!Sense.canBeSeenBy(exit,mob))))
		{
			mob.tell("You can't see anywhere to pass that way.");
			return false;
		}
		Exit opExit=room.getReverseExit(dirCode);

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;


		boolean success=profficiencyCheck(0,auto);

		if(!success)
		{
			if(exit.isOpen())
				ExternalPlay.move(mob,dirCode,false,false);
			else
				beneficialVisualFizzle(mob,null,"<S-NAME> walk(s) "+Directions.getDirectionName(dirCode)+", but go(es) no further.");
		}
		else
		if(exit.isOpen())
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
		else
		{
			FullMsg msg=new FullMsg(mob,null,null,Affect.MSG_QUIETMOVEMENT|Affect.MASK_MAGIC,null);
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				boolean open=exit.isOpen();
				boolean locked=exit.isLocked();
				exit.setDoorsNLocks(exit.hasADoor(),true,exit.defaultsClosed(),exit.hasALock(),false,exit.defaultsLocked());
				if(opExit!=null)
					opExit.setDoorsNLocks(exit.hasADoor(),true,exit.defaultsClosed(),exit.hasALock(),false,exit.defaultsLocked());
				mob.tell("\n\r\n\r");
				if(mob.fetchAffect(ID())==null)
				{
					mob.addAffect(this);
					mob.recoverEnvStats();
				}
				ExternalPlay.move(mob,dirCode,false,false);
				mob.delAffect(this);
				mob.recoverEnvStats();
				exit.setDoorsNLocks(exit.hasADoor(),open,exit.defaultsClosed(),exit.hasALock(),locked,exit.defaultsLocked());
				if(opExit!=null)
					opExit.setDoorsNLocks(exit.hasADoor(),open,exit.defaultsClosed(),exit.hasALock(),locked,exit.defaultsLocked());
			}
		}

		return success;
	}
}