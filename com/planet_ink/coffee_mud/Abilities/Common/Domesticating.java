package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Domesticating extends CommonSkill
{
	public String ID() { return "Domesticating"; }
	public String name(){ return "Domesticating";}
	private static final String[] triggerStrings = {"DOMESTICATE","DOMESTICATING"};
	public String[] triggerStrings(){return triggerStrings;}

	private MOB taming=null;
	private boolean messedUp=false;
	private static boolean mapped=false;
	public Domesticating()
	{
		super();
		displayText="You are domesticating...";
		verb="domesticating";
		if(!mapped){mapped=true;
					CMAble.addCharAbilityMapping("Archon",1,ID(),false);
		}
	}
	public Environmental newInstance(){	return new Domesticating();}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(tickID==MudHost.TICK_MOB))
		{
			MOB mob=(MOB)affected;
			if((taming==null)||(!mob.location().isInhabitant(taming)))
				unInvoke();
		}
		return super.tick(ticking,tickID);
	}

	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)&&(affected instanceof MOB))
			{
				MOB mob=(MOB)affected;
				if((taming!=null)&&(!aborted))
				{
					if(messedUp)
						commonTell(mob,"You've failed to domesticate "+taming.name()+"!");
					else
					{
						if(taming.amFollowing()==mob)
							commonTell(mob,taming.name()+" is already domesticated.");
						else
						{
							CommonMsgs.follow(taming,mob,true);
							if(taming.amFollowing()==mob)
                                mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT," manage(s) to domesticate "+taming.name()+".");
						}
					}
				}
			}
		}
		super.unInvoke();
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		taming=null;
		String str=Util.combine(commands,0);
		MOB M=mob.location().fetchInhabitant(str);
		if((M==null)||(!Sense.canBeSeenBy(M,mob)))
		{
			commonTell(mob,"You don't see anyone called '"+str+"' here.");
			return false;
		}
		if(!M.isMonster())
		{
			commonTell(mob,"You can't domesticate "+M.name()+".");
			return false;
		}
		if(!Sense.isAnimalIntelligence(M))
		{
			commonTell(mob,"You don't know how to domesticate "+M.name()+".");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		taming=M;
		verb="domesticating "+M.name();
		int levelDiff=taming.envStats().level()-mob.envStats().level();
		if(levelDiff>0) levelDiff=0;
		messedUp=!profficiencyCheck(mob,-(levelDiff*5),auto);
		int duration=35+levelDiff;
		if(duration<10) duration=10;
		FullMsg msg=new FullMsg(mob,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> start(s) domesticating "+M.name()+".");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,duration);
		}
		return true;
	}
}