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

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(tickID==MudHost.TICK_MOB))
		{
			MOB mob=(MOB)affected;
			if((taming==null)||(!mob.location().isInhabitant(taming)))
			{
				messedUp=true;
				unInvoke();
			}
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
		String newName=null;
		if((commands.size()>2)&&(((String)commands.firstElement()).equalsIgnoreCase("name")))
		{
			str=(String)commands.elementAt(1);
			newName=Util.combine(commands,2);
		}
		MOB M=mob.location().fetchInhabitant(str);
		if((M==null)||(!Sense.canBeSeenBy(M,mob)))
		{
			commonTell(mob,"You don't see anyone called '"+str+"' here.");
			return false;
		}
		if(!M.isMonster())
		{
			if(newName!=null)
				commonTell(mob,"You can't name "+M.name()+".");
			else
				commonTell(mob,"You can't domesticate "+M.name()+".");
			return false;
		}
		if(!Sense.isAnimalIntelligence(M))
		{
			if(newName!=null)
				commonTell(mob,"You can't name "+M.name()+".");
			else
				commonTell(mob,"You don't know how to domesticate "+M.name()+".");
			return false;
		}
		String theName=newName;
		if((newName!=null)&&(M.amFollowing()==null))
		{
			commonTell(mob,"You can only name someones pet.");
			return false;
		}
		else
		if((newName!=null)&&(M!=null))
		{
			if(newName.trim().length()==0)
			{
				mob.tell("You must specify a name.");
				return false;
			}
			if(newName.indexOf(" ")>=0)
			{
				mob.tell("The name may not contain a space.");
				return false;
			}
			if((M.name().toUpperCase().startsWith("A "))
			||(M.name().toUpperCase().startsWith("AN "))
			||(M.name().toUpperCase().startsWith("SOME ")))
				newName=M.name()+" named "+newName;
			else
			if(M.name().indexOf(" ")>=0)
				newName=newName+", "+M.name();
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
		FullMsg msg=new FullMsg(mob,null,this,CMMsg.MSG_NOISYMOVEMENT,
			(newName!=null)?"<S-NAME> name(s) "+M.name()+" '"+theName+"'.":"<S-NAME> start(s) domesticating "+M.name()+".");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			if(newName!=null)
			{
				Ability A=CMClass.getAbility("Chant_BestowName");
				if(A!=null)
				{
					A.setMiscText(newName);
					M.addNonUninvokableEffect(A);
					mob.location().recoverRoomStats();
				}
			}
			else
				beneficialAffect(mob,mob,duration);
		}
		return true;
	}
}