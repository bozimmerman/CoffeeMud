package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Mining extends CommonSkill
{
	public String ID() { return "Mining"; }
	public String name(){ return "Mining";}
	private static final String[] triggerStrings = {"MINE","MINING"};
	public String[] triggerStrings(){return triggerStrings;}

	private Item found=null;
	private String foundShortName="";
	private static boolean mapped=false;
	public Mining()
	{
		super();
		displayText="You are mining...";
		verb="mining";
		if(!mapped){mapped=true;
					CMAble.addCharAbilityMapping("All",1,ID(),false);}
	}
	public Environmental newInstance(){	return new Mining();}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Host.MOB_TICK))
		{
			MOB mob=(MOB)affected;
			if(tickUp==6)
			{
				if(found!=null)
				{
					commonTell(mob,"You have found a vein of "+foundShortName+"!");
					displayText="You are mining "+foundShortName;
					verb="mining "+foundShortName;
				}
				else
				{
					StringBuffer str=new StringBuffer("You can't seem to find anything worth mining here.\n\r");
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
					int amount=Dice.roll(1,10,0);
					if(((found.material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_ROCK)
					&&(found.material()!=EnvResource.RESOURCE_COAL))
						amount=Dice.roll(1,85,0);
					amount=amount*(abilityCode());
					String s="s";
					if(amount==1) s="";
					mob.location().show(mob,null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> manage(s) to mine "+amount+" pound"+s+" of "+foundShortName+".");
					for(int i=0;i<amount;i++)
					{
						Item newFound=(Item)found.copyOf();
						mob.location().addItemRefuse(newFound,Item.REFUSE_RESOURCE);
						//ExternalPlay.get(mob,null,newFound,true);
					}
				}
			}
		}
		super.unInvoke();
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		verb="mining";
		found=null;
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		int resourceType=mob.location().myResource();
		if((profficiencyCheck(0,auto))
		   &&((resourceType&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_PRECIOUS)
		   &&((resourceType&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_GLASS)
		   &&(resourceType!=EnvResource.RESOURCE_SAND)
		   &&(((resourceType&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_ROCK)
		   ||((resourceType&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_METAL)
		   ||((resourceType&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_MITHRIL)))
		{
			found=(Item)makeResource(resourceType,false);
			foundShortName="nothing";
			if(found!=null)
				foundShortName=EnvResource.RESOURCE_DESCS[found.material()&EnvResource.RESOURCE_MASK].toLowerCase();
		}
		int duration=50-mob.envStats().level();
		if(duration<15) duration=15;
		FullMsg msg=new FullMsg(mob,null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> start(s) mining.");
		if(mob.location().okAffect(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,duration);
		}
		return true;
	}
}
