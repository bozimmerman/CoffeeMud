package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Potter extends CommonSkill
{
	private Item found=null;
	private String foundShortName="";
	public Potter()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Potting";

		displayText="You are looking for stuff to pot...";
		verb="searching";
		miscText="";
		triggerStrings.addElement("POT");
		quality=Ability.INDIFFERENT;

		recoverEnvStats();
		CMAble.addCharAbilityMapping("All",1,ID(),false);
	}

	public Environmental newInstance()
	{
		return new Potter();
	}
	public boolean tick(int tickID)
	{
		MOB mob=(MOB)affected;
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Host.MOB_TICK))
		{
			if(tickUp==2)
			{
				if(found!=null)
				{
					mob.tell("You have found some "+foundShortName+"!");
					displayText="You are collecting "+foundShortName;
					verb="collecting "+foundShortName;
				}
				else
				{
					StringBuffer str=new StringBuffer("You can't seem to find any good stuff to collect around here.\n\r");
					int d=lookingFor(EnvResource.MATERIAL_VEGETATION,mob.location());
					if(d<0)
						str.append("You might try elsewhere.");
					else
						str.append("You might try "+Directions.getInDirectionName(d)+".");
					mob.tell(str.toString());
					unInvoke();
				}
				
			}
		}
		return super.tick(tickID);
	}

	public void unInvoke()
	{
		if((affected!=null)&&(affected instanceof MOB))
		{
			MOB mob=(MOB)affected;
			if((found!=null)&&(!aborted))
			{
				int amount=Dice.roll(1,5,20);
				String s="s";
				if(amount==1) s="";
				mob.location().show(mob,null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> manage(s) to collect "+amount+" pound"+s+" of "+foundShortName+".");
				for(int i=0;i<amount;i++)
				{
					Item newFound=(Item)found.copyOf();
					mob.location().addItem(newFound);
					ExternalPlay.get(mob,null,newFound,true);
				}
			}
		}
		super.unInvoke();
	}
	
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		verb="searching";
		found=null;
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		int resourceType=mob.location().myResource();
		if((profficiencyCheck(0,auto))
		   &&((resourceType==EnvResource.RESOURCE_SAND)
		   ||(resourceType==EnvResource.RESOURCE_CHINA)
		   ||(resourceType==EnvResource.RESOURCE_GLASS)
		   ||(resourceType==EnvResource.RESOURCE_CLAY)))
		{
			found=(Item)makeResource(resourceType);
			foundShortName=EnvResource.RESOURCE_DESCS[found.material()&EnvResource.RESOURCE_MASK].toLowerCase();
		}
		int duration=20-mob.envStats().level();
		if(duration<5) duration=5;
		beneficialAffect(mob,mob,duration);
		return true;
	}
}