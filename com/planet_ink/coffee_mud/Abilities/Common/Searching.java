package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Searching extends CommonSkill
{
	public String ID() { return "Searching"; }
	public String name(){ return "Searching";}
	private static final String[] triggerStrings = {"SEARCH","SEARCHING"};
	public String[] triggerStrings(){return triggerStrings;}
	private static boolean mapped=false;
	private Room searchRoom=null;

	private boolean success=false;
	public Searching()
	{
		super();
		displayText="You are searching...";
		verb="searching";
		if(!mapped){mapped=true;
					CMAble.addCharAbilityMapping("All",1,ID(),false);}
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==MudHost.TICK_MOB))
		{
			MOB mob=(MOB)affected;
			if(tickUp==1)
			{
				if(success==false)
				{
					StringBuffer str=new StringBuffer("You get distracted from your search.\n\r");
					commonTell(mob,str.toString());
					unInvoke();
				}

			}
		}
		return super.tick(ticking,tickID);
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if((success)&&(affected instanceof MOB)&&(((MOB)affected).location()==searchRoom))
			affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_SEE_HIDDEN);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		verb="searching";
		success=false;
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		if(profficiencyCheck(mob,0,auto))
			success=true;
		int duration=3;
		FullMsg msg=new FullMsg(mob,null,this,CMMsg.MSG_NOISYMOVEMENT,(auto?"":"<S-NAME> start(s) searching."));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			searchRoom=mob.location();
			beneficialAffect(mob,mob,duration);
			mob.tell(" ");
			CommonMsgs.look(mob,true);
		}
		return true;
	}
}