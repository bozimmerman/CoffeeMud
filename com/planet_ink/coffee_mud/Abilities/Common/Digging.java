package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Digging extends CommonSkill
{
	private Item found=null;
	private String foundShortName="";
	public Digging()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Gem Digging";

		displayText="You are digging...";
		verb="digging";
		miscText="";
		triggerStrings.addElement("DIG");
		triggerStrings.addElement("DIGGING");
		quality=Ability.INDIFFERENT;

		recoverEnvStats();
		CMAble.addCharAbilityMapping("All",1,ID(),false);
	}

	public Environmental newInstance()
	{
		return new Digging();
	}
	public boolean tick(int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Host.MOB_TICK))
		{
			MOB mob=(MOB)affected;
			if(tickUp==3)
			{
				if(found!=null)
				{
					mob.tell("You have found some "+foundShortName+"!");
					displayText="You are digging out "+foundShortName;
					verb="digging out "+foundShortName;
				}
				else
				{
					StringBuffer str=new StringBuffer("You can't seem to find anything worth digging up here.\n\r");
					mob.tell(str.toString());
					unInvoke();
				}
				
			}
		}
		return super.tick(tickID);
	}

	public void unInvoke()
	{
		if(canBeUninvoked)
		{
			if((affected!=null)&&(affected instanceof MOB))
			{
				MOB mob=(MOB)affected;
				if((found!=null)&&(!aborted))
				{
					int amount=1;
					if(Dice.rollPercentage()>90)
						amount++;
					String s="s";
					if(amount==1) s="";
					mob.location().show(mob,null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> manage(s) to dig out "+amount+" "+foundShortName+s+".");
					for(int i=0;i<amount;i++)
					{
						Item newFound=(Item)found.copyOf();
						mob.location().addItemRefuse(newFound);
						ExternalPlay.get(mob,null,newFound,true);
					}
				}
			}
		}
		super.unInvoke();
	}
	
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		verb="digging";
		found=null;
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		int resourceType=mob.location().myResource();
		if((profficiencyCheck(0,auto))
		   &&((resourceType==EnvResource.RESOURCE_CRYSTAL)
		   ||(resourceType==EnvResource.RESOURCE_DIAMOND)
		   ||(resourceType==EnvResource.RESOURCE_GEM)
		   ||(resourceType==EnvResource.RESOURCE_AMETHYST)
		   ||(resourceType==EnvResource.RESOURCE_GARNET)
		   ||(resourceType==EnvResource.RESOURCE_AMBER)
		   ||(resourceType==EnvResource.RESOURCE_AQUAMARINE)
		   ||(resourceType==EnvResource.RESOURCE_CRYSOBERYL)
		   ||(resourceType==EnvResource.RESOURCE_STONE)
		   ||(resourceType==EnvResource.RESOURCE_TOPAZ)
		   ||(resourceType==EnvResource.RESOURCE_OPAL)
		   ||(resourceType==EnvResource.RESOURCE_PEARL)))
		{
			found=(Item)makeResource(resourceType);
			foundShortName=EnvResource.RESOURCE_DESCS[found.material()&EnvResource.RESOURCE_MASK].toLowerCase();
		}
		int duration=60-mob.envStats().level();
		if(duration<25) duration=25;
		FullMsg msg=new FullMsg(mob,null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> start(s) digging.");
		if(mob.location().okAffect(msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,duration);
		}
		return true;
	}
}
