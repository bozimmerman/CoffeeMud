package com.planet_ink.coffee_mud.Abilities.Common;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Herbology extends CommonSkill
{
	public String ID() { return "Herbology"; }
	public String name(){ return "Herbology";}
	private static final String[] triggerStrings = {"HERBOLOGY"};
	public String[] triggerStrings(){return triggerStrings;}

	private Item found=null;
	private String writing="";
	private static boolean mapped=false;
	private boolean messedUp=false;
	private static final String[] herbList={"angelica","mustard","anise",
											"myrrh","cassia","peppermint",
											"chamomile","poppy","cloves",
											"rosemary","lemon grass","mint",
											"sage","damiana","sarsaparilla",
											"elder","thyme","gentian",
											"valerian","marjoram","yerba mate"};

	public Herbology()
	{
		super();
		displayText="You are evaluating...";
		verb="evaluating";
		if(!mapped){mapped=true;
					CMAble.addCharAbilityMapping("All",1,ID(),false);}
	}
	public Environmental newInstance(){	return new Herbology();	}

	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)&&(affected instanceof MOB)&&(!aborted))
			{
				MOB mob=(MOB)affected;
				if(messedUp)
					commonTell(mob,"You lose your concentration on "+found.name()+".");
				else
				{
					String herb=herbList[Dice.roll(1,herbList.length,-1)].toLowerCase();
					commonTell(mob,found.name()+" appears to be "+herb+".");
					String name=found.Name();
					name=name.substring(0,name.length()-5).trim();
					if(name.length()>0)
						found.setName(name+" "+herb);
					else
						found.setName("some "+herb);
					found.setDisplayText(found.Name()+" is here");
					found.setDescription("");
					found.text();
				}
			}
		}
		super.unInvoke();
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(commands.size()<1)
		{
			commonTell(mob,"You must specify what herb you want to identify.");
			return false;
		}
		Item target=mob.fetchCarried(null,Util.combine(commands,0));
		if((target==null)||(!Sense.canBeSeenBy(target,mob)))
		{
			commonTell(mob,"You don't seem to have a '"+((String)commands.firstElement())+"'.");
			return false;
		}
		else
			commands.remove(commands.firstElement());

		if((target.material()!=EnvResource.RESOURCE_HERBS)
		||((!target.Name().toUpperCase().endsWith(" HERBS"))
		   &&(!target.Name().equalsIgnoreCase("herbs")))
		||(!(target instanceof EnvResource))
		||(!target.isGeneric()))
		{
			commonTell(mob,"You can only identify unknown herbs.");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		verb="studying "+target.name();
		displayText="You are "+verb;
		found=target;
		messedUp=false;
		if(!profficiencyCheck(0,auto)) messedUp=true;
		int duration=10-(mob.envStats().level()/3);
		if(duration<2) duration=2;
		FullMsg msg=new FullMsg(mob,null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> stud(ys) "+target.name()+".");
		if(mob.location().okAffect(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,duration);
		}
		return true;
	}
}