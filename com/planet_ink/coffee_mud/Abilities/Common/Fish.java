package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Fish extends CommonSkill
{
	private Item found=null;
	private String foundShortName="";
	public Fish()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Fish";

		displayText="You are fishing...";
		verb="fishing";
		miscText="";
		triggerStrings.addElement("FISH");
		quality=Ability.INDIFFERENT;

		recoverEnvStats();
		CMAble.addCharAbilityMapping("All",1,ID(),false);
	}

	public Environmental newInstance()
	{
		return new Fish();
	}
	public boolean tick(int tickID)
	{
		MOB mob=(MOB)affected;
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Host.MOB_TICK))
		{
			if(tickUp==6)
			{
				if(found!=null)
					mob.tell("You got a tug on the line!");
				else
				{
					StringBuffer str=new StringBuffer("Nothing is biting around here.\n\r");
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
				int amount=Dice.roll(1,5,0);
				String s="s";
				if(amount==1) s="";
				mob.location().show(mob,null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> manage(s) to catch "+amount+" pound"+s+" of "+foundShortName+".");
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
		if((mob.location().domainType()!=Room.DOMAIN_OUTDOORS_WATERSURFACE)
		&&(mob.location().domainType()!=Room.DOMAIN_OUTDOORS_UNDERWATER))
		{
			mob.tell("You are kidding, right?  Fish on dry land?");
			return false;
		}
		verb="fishing";
		found=null;
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		int resourceType=mob.location().myResource();
		if((profficiencyCheck(0,auto))
		   &&(resourceType==EnvResource.RESOURCE_FISH))
		{
			found=(Item)makeResource(resourceType);
			foundShortName=EnvResource.RESOURCE_DESCS[found.material()&EnvResource.RESOURCE_MASK].toLowerCase();
		}
		int duration=35-mob.envStats().level();
		if(duration<10) duration=10;
		beneficialAffect(mob,mob,duration);
		return true;
	}
}