package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Hunt extends CommonSkill
{
	private MOB found=null;
	private String foundShortName="";
	public Hunt()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Forage";

		displayText="You are foraging...";
		verb="foraging";
		miscText="";
		triggerStrings.addElement("FORAGE");
		quality=Ability.INDIFFERENT;

		recoverEnvStats();
		CMAble.addCharAbilityMapping("All",1,ID(),false);
	}

	public Environmental newInstance()
	{
		return new Hunt();
	}
	
	public void moveFound()
	{
		Vector possibilities=new Vector();
		for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
		{
			if(d!=Directions.UP)
			{
				Room room=found.location().getRoomInDir(d);
				Exit exit=found.location().getExitInDir(d);
				if((room!=null)&&(exit!=null)&&(exit.isOpen()))
					possibilities.addElement(new Integer(d));
			}
		}
		if(possibilities.size()>0)
		{
			int dir=((Integer)possibilities.elementAt(Dice.roll(1,possibilities.size(),-1))).intValue();
			ExternalPlay.move(found,dir,true);
		}
	}
	
	public boolean tick(int tickID)
	{
		MOB mob=(MOB)affected;
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Host.MOB_TICK))
		{
			activityRoom=mob.location();
			if((found!=null)&&(found.amDead()))
			{
				found.setLocation(null);
				found.destroy();
				unInvoke();
			}
			else
			if((found!=null)&&(Sense.aliveAwakeMobile(found,true))&&(!found.isInCombat()))
			{
				if(found.location()==mob.location())
					moveFound();
			}
			
			if(tickUp==0)
			{
				if(found!=null)
				{
					mob.tell("You have found some "+foundShortName+" tracks!");
					displayText="You are hunting for "+found.name();
					verb="hunting for "+found.name();
				}
				else
				{
					StringBuffer str=new StringBuffer("You can't seem to find any game around here.\n\r");
					int d=lookingFor(EnvResource.MATERIAL_FLESH,mob.location());
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
			if((found!=null)&&(found instanceof MOB)&&(!found.amDead())&&(found.location()!=null)&&(!found.isInCombat()))
			{
				if(found.location()==mob.location())
					moveFound();
				found.location().delInhabitant(found);
				found.setLocation(null);
				found.destroy();
				mob.location().show(mob,null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> <S-HAS-HAVE> lost the trail.");
			}
		}
		super.unInvoke();
	}
	
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		verb="hunting";
		found=null;
		activityRoom=null;
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		int resourceType=mob.location().myResource();
		if((profficiencyCheck(0,auto))
		   &&((resourceType&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_VEGETATION))
		{
			found=(MOB)makeResource(resourceType);
			foundShortName=found.name();
			int x=0;
			if((x=foundShortName.lastIndexOf(" "))>=0)
				foundShortName=foundShortName.substring(x).trim().toLowerCase();
			found.envStats().setDisposition(EnvStats.IS_SEEN);
			found.bringToLife(mob.location());
			found.recoverEnvStats();
			moveFound();
		}
		int duration=10+(mob.envStats().level()/4);
		beneficialAffect(mob,mob,duration);
		return true;
	}
}
