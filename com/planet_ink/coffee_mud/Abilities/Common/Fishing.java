package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Fishing extends CommonSkill
{
	public String ID() { return "Fishing"; }
	public String name(){ return "Fishing";}
	private static final String[] triggerStrings = {"FISH"};
	public String[] triggerStrings(){return triggerStrings;}
	
	private Item found=null;
	private String foundShortName="";
	private static boolean mapped=false;
	public Fishing()
	{
		super();
		displayText="You are fishing...";
		verb="fishing";
		if(!mapped){mapped=true;
					CMAble.addCharAbilityMapping("All",1,ID(),false);}
	}
	public Environmental newInstance(){	return new Fishing();}
	
	public boolean tick(int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Host.MOB_TICK))
		{
			MOB mob=(MOB)affected;
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
		if(canBeUninvoked)
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
						mob.location().addItemRefuse(newFound,Item.REFUSE_PLAYER_DROP);
						ExternalPlay.get(mob,null,newFound,true);
					}
				}
			}
		}
		super.unInvoke();
	}
	
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if((mob.location().domainType()!=Room.DOMAIN_OUTDOORS_WATERSURFACE)
		&&(mob.location().domainType()!=Room.DOMAIN_OUTDOORS_UNDERWATER)
		&&(mob.location().domainType()!=Room.DOMAIN_INDOORS_UNDERWATER)
		&&(mob.location().domainType()!=Room.DOMAIN_INDOORS_WATERSURFACE))
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
			foundShortName="nothing";
			if(found!=null)
				foundShortName=EnvResource.RESOURCE_DESCS[found.material()&EnvResource.RESOURCE_MASK].toLowerCase();
		}
		int duration=35-mob.envStats().level();
		if(duration<10) duration=10;
		FullMsg msg=new FullMsg(mob,null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> start(s) fishing.");
		if(mob.location().okAffect(msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,duration);
		}
		return true;
	}
}