package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Hunting extends CommonSkill
{
	public String ID() { return "Hunting"; }
	public String name(){ return "Hunting";}
	private static final String[] triggerStrings = {"HUNT","HUNTING"};
	public String[] triggerStrings(){return triggerStrings;}
	
	private MOB found=null;
	private String foundShortName="";
	private static boolean mapped=false;
	public Hunting()
	{
		super();
		displayText="You are hunting...";
		verb="hunting";
		if(!mapped){mapped=true;
					CMAble.addCharAbilityMapping("All",1,ID(),false);}
	}
	public Environmental newInstance(){	return new Hunting();}
	
	public Room nearByRoom()
	{
		Vector possibilities=new Vector();
		for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
		{
			if(d!=Directions.UP)
			{
				Room room=activityRoom.getRoomInDir(d);
				Exit exit=activityRoom.getExitInDir(d);
				if((room!=null)&&(exit!=null)&&(exit.isOpen()))
					possibilities.addElement(new Integer(d));
			}
		}
		if(possibilities.size()>0)
		{
			int dir=((Integer)possibilities.elementAt(Dice.roll(1,possibilities.size(),-1))).intValue();
			return activityRoom.getRoomInDir(dir);
		}
		return null;
	}
	
	public void moveFound()
	{
		if(found.location()==null) return;
		
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
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Host.MOB_TICK))
		{
			MOB mob=(MOB)affected;
			activityRoom=mob.location();
			if((found!=null)&&(found.amDead()))
			{
				found.setLocation(null);
				found.destroy();
				unInvoke();
			}
			else
			if((found!=null)
			&&(found.location()!=null)
			&&(Sense.aliveAwakeMobile(found,true))
			&&(!found.isInCombat()))
			{
				if(found.location()==mob.location())
					moveFound();
			}
			
			if(tickUp==0)
			{
				if(found!=null)
				{
					commonTell(mob,"You have found some "+foundShortName+" tracks!");
					commonTell(mob,"You need to find the "+foundShortName+" nearby before the trail goes cold!");
					displayText="You are hunting for "+found.name();
					verb="hunting for "+found.name();
					found.bringToLife(nearByRoom(),true);
				}
				else
				{
					StringBuffer str=new StringBuffer("You can't seem to find any game around here.\n\r");
					int d=lookingFor(EnvResource.MATERIAL_FLESH,mob.location());
					if(d<0)
						str.append("You might try elsewhere.");
					else
						str.append("You might try "+Directions.getInDirectionName(d)+".");
					commonTell(mob,str.toString());
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
				if((found!=null)&&(!found.amDead())&&(found.location()!=null)&&(!found.isInCombat()))
				{
					if(found.location()==mob.location())
						moveFound();
					found.location().delInhabitant(found);
					found.setLocation(null);
					found.destroy();
					mob.location().show(mob,null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> <S-HAS-HAVE> lost the trail.");
				}
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
		   &&(nearByRoom()!=null)
		   &&(resourceType!=EnvResource.RESOURCE_FISH)
		   &&(((resourceType&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_FLESH)
		   ||(resourceType==EnvResource.RESOURCE_BLOOD)
		   ||(resourceType==EnvResource.RESOURCE_BONE)
		   ||(resourceType==EnvResource.RESOURCE_EGGS)
		   ||(resourceType==EnvResource.RESOURCE_FEATHERS)
		   ||(resourceType==EnvResource.RESOURCE_FUR)
		   ||(resourceType==EnvResource.RESOURCE_HIDE)
		   ||(resourceType==EnvResource.RESOURCE_MILK)
		   ||(resourceType==EnvResource.RESOURCE_SCALES)
		   ||(resourceType==EnvResource.RESOURCE_WOOL)
		   ||((resourceType&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_LEATHER)))
		{
			found=(MOB)makeResource(resourceType);
			foundShortName="nothing";
			if(found!=null)
			{
				foundShortName=found.name();
				int x=0;
				if((x=foundShortName.lastIndexOf(" "))>=0)
					foundShortName=foundShortName.substring(x).trim().toLowerCase();
				found.setLocation(null);
			}
		}
		int duration=10+(mob.envStats().level()/4);
		FullMsg msg=new FullMsg(mob,null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> start(s) hunting.");
		if(mob.location().okAffect(msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,duration);
		}
		return true;
	}
}
