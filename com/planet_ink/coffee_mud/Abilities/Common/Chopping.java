package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chopping extends CommonSkill
{
	public String ID() { return "Chopping"; }
	public String name(){ return "Wood Chopping";}
	private static final String[] triggerStrings = {"CHOP","CHOPPING"};
	public String[] triggerStrings(){return triggerStrings;}

	private Item found=null;
	private String foundShortName="";
	private static boolean mapped=false;
	public Chopping()
	{
		super();
		displayText="You are chopping wood...";
		verb="chopping";
		if(!mapped){mapped=true;
					CMAble.addCharAbilityMapping("All",1,ID(),false);}
	}
	public Environmental newInstance(){	return new Chopping();}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==MudHost.TICK_MOB))
		{
			MOB mob=(MOB)affected;
			if(tickUp==6)
			{
				if(found!=null)
				{
					commonTell(mob,"You have a good tree for "+foundShortName+".");
					displayText="You are chopping up "+foundShortName;
					verb="chopping "+foundShortName;
				}
				else
				{
					StringBuffer str=new StringBuffer("You can't seem to find any trees worth cutting around here.\n\r");
					int d=lookingFor(EnvResource.MATERIAL_WOODEN,mob.location());
					if(d<0)
						str.append("You might try elsewhere.");
					else
						str.append("You might try "+Directions.getInDirectionName(d)+".");
					commonTell(mob,str.toString());
					unInvoke();
				}

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
				if((found!=null)&&(!aborted))
				{
					int amount=Dice.roll(1,20,10)*(abilityCode());
					String s="s";
					if(amount==1) s="";
					mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> manage(s) to chop up "+amount+" pound"+s+" of "+foundShortName+".");
					for(int i=0;i<amount;i++)
					{
						Item newFound=(Item)found.copyOf();
						mob.location().addItemRefuse(newFound,Item.REFUSE_RESOURCE);
						//CommonMsgs.get(mob,null,newFound,true);
					}
				}
			}
		}
		super.unInvoke();
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		verb="chopping";
		found=null;
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		int resourceType=mob.location().myResource();
		if((profficiencyCheck(0,auto))
		   &&((resourceType&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_WOODEN))
		{
			found=(Item)makeResource(resourceType,false);
			foundShortName="nothing";
			if(found!=null)
				foundShortName=EnvResource.RESOURCE_DESCS[found.material()&EnvResource.RESOURCE_MASK].toLowerCase();
		}
		int duration=40-mob.envStats().level();
		if(duration<15) duration=15;
		FullMsg msg=new FullMsg(mob,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> start(s) chopping wood.");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,duration);
		}
		return true;
	}
}
