package com.planet_ink.coffee_mud.Abilities.Ranger;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import java.util.*;

public class Ranger_Sneak extends StdAbility
{
	public String ID() { return "Ranger_Sneak"; }
	public String name(){ return "Woodland Sneak";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.INDIFFERENT;}
	private static final String[] triggerStrings = {"SNEAK"};
	public String[] triggerStrings(){return triggerStrings;}
	public Environmental newInstance(){	return new Ranger_Sneak();}
	public int classificationCode(){return Ability.SKILL;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		int dirCode=Directions.getGoodDirectionCode(Util.combine(commands,0));
		if(dirCode<0)
		{
			mob.tell("Sneak where?");
			return false;
		}
		
		if((((mob.location().domainType()&Room.INDOORS)>0))&&(!auto))
		{
			mob.tell("You must be outdoors to do this.");
			return false;
		}
		if((mob.location().domainType()==Room.DOMAIN_OUTDOORS_CITY)&&(!auto))
		{
			mob.tell("You don't know how to sneak around a place like this.");
			return false;
		}

		if((mob.location().getRoomInDir(dirCode)==null)||(mob.location().getExitInDir(dirCode)==null))
		{
			mob.tell("Sneak where?");
			return false;
		}
		
		Hashtable H=mob.getGroupMembers(new Hashtable());
		int highestLevel=0;
		for(int i=0;i<mob.location().numInhabitants();i++)
		{
			MOB M=mob.location().fetchInhabitant(i);
			if(((M!=mob)&&(!H.contains(M)))&&(highestLevel<M.envStats().level()))
				highestLevel=mob.envStats().level();
		}
		int levelDiff=mob.envStats().level()-highestLevel;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=false;
		FullMsg msg=new FullMsg(mob,null,this,auto?Affect.MSG_OK_VISUAL:Affect.MSG_DELICATE_HANDS_ACT,"You quietly sneak "+Directions.getDirectionName(dirCode)+".",Affect.NO_EFFECT,null,Affect.NO_EFFECT,null);
		if(mob.location().okAffect(msg))
		{
			mob.location().send(mob,msg);
			if(levelDiff<0) 
				levelDiff=levelDiff*10;
			else 
				levelDiff=levelDiff*5;
			success=profficiencyCheck(levelDiff,auto);
			
			if(success)
			{
				mob.baseEnvStats().setDisposition(mob.baseEnvStats().disposition()|EnvStats.IS_SNEAKING);
				mob.recoverEnvStats();
			}
			ExternalPlay.move(mob,dirCode,false,false);
			if(success)
			{
				
				int disposition=mob.baseEnvStats().disposition();
				if((disposition&EnvStats.IS_SNEAKING)>0)
				{
					mob.baseEnvStats().setDisposition(disposition-EnvStats.IS_SNEAKING);
					mob.recoverEnvStats();
				}
			}
		}
		return success;
	}

}
