package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.common.*;

import java.util.*;
import com.planet_ink.coffee_mud.utils.Sense;
import com.planet_ink.coffee_mud.utils.Util;
public class UnderWater extends StdRoom implements Drink
{
	public String ID(){return "UnderWater";}
	public UnderWater()
	{
		super();
		name="the water";
		baseEnvStats().setSensesMask(baseEnvStats().sensesMask()|EnvStats.CAN_NOT_BREATHE);
		baseEnvStats().setDisposition(baseEnvStats().disposition()|EnvStats.IS_SWIMMING);
		baseEnvStats.setWeight(3);
		recoverEnvStats();
		domainType=Room.DOMAIN_OUTDOORS_UNDERWATER;
		domainCondition=Room.CONDITION_WET;
		baseThirst=0;
	}

	public Environmental newInstance()
	{
		return new UnderWater();
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected instanceof MOB)
			affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_SWIMMING);
	}

	public static void makeSink(Environmental E, Room room, int avg)
	{
		if((E==null)||(room==null)) return;
		
		Room R=room.getRoomInDir(Directions.DOWN);
		if(avg>0) R=room.getRoomInDir(Directions.UP);
		if((R==null)
		||((R.domainType()!=Room.DOMAIN_INDOORS_UNDERWATER)
		   &&(R.domainType()!=Room.DOMAIN_OUTDOORS_UNDERWATER)))
			return;
		
		if(((E instanceof MOB)&&(!Sense.isWaterWorthy(E))&&(!Sense.isInFlight(E))&&(E.envStats().weight()>=1))
		||((E instanceof Item)&&(!Sense.isInFlight(((Item)E).ultimateContainer()))&&(!Sense.isWaterWorthy(((Item)E).ultimateContainer()))))
			if(E.fetchAffect("Sinking")==null)
			{
				Ability sinking=CMClass.getAbility("Sinking");
				if(sinking!=null) 
				{
					sinking.setProfficiency(avg);
					sinking.setAffectedOne(room);
					sinking.invoke(null,null,E,true);
				}
			}
	}
	
	public static void sinkAffects(Room room, Affect affect)
	{
		if(affect.amITarget(room)
		&&(affect.targetMinor()==Affect.TYP_DRINK)
		&&(room instanceof Drink))
		{
			MOB mob=affect.source();
			boolean thirsty=mob.curState().getThirst()<=0;
			boolean full=!mob.curState().adjThirst(((Drink)room).thirstQuenched(),mob.maxState());
			if(thirsty)
				mob.tell("You are no longer thirsty.");
			else
			if(full)
				mob.tell("You have drunk all you can.");
		}
		
		if(Sense.isSleeping(room)) 
			return;
		boolean foundReversed=false;
		boolean foundNormal=false;
		Vector needToFall=new Vector();
		Vector mightNeedAdjusting=new Vector();
		
		if((room.domainType()!=Room.DOMAIN_OUTDOORS_UNDERWATER)
		&&(room.domainType()!=Room.DOMAIN_INDOORS_UNDERWATER))
			for(int i=0;i<room.numInhabitants();i++)
			{
				MOB mob=room.fetchInhabitant(i);
				if((mob!=null)
				&&((mob.getStartRoom()==null)||(mob.getStartRoom()!=room)))
				{
					Ability A=mob.fetchAffect("Sinking");
					if(A!=null)
					{
						if(A.profficiency()>=100)
						{
							foundReversed=true;
							mightNeedAdjusting.addElement(mob);
						}
						foundNormal=foundNormal||(A.profficiency()<=0);
					}
					else
						needToFall.addElement(mob);
				}
			}
		for(int i=0;i<room.numItems();i++)
		{
			Item item=room.fetchItem(i);
			if(item!=null)
			{
				Ability A=item.fetchAffect("Sinking");
				if(A!=null)
				{
					if(A.profficiency()>=100)
					{
						foundReversed=true;
						mightNeedAdjusting.addElement(item);
					}
					foundNormal=foundNormal||(A.profficiency()<=0);
				}
				else
					needToFall.addElement(item);
			}
		}
		int avg=((foundReversed)&&(!foundNormal))?100:0;
		for(int i=0;i<mightNeedAdjusting.size();i++)
		{
			Environmental E=(Environmental)mightNeedAdjusting.elementAt(i);
			Ability A=E.fetchAffect("Sinking");
			if(A!=null) A.setProfficiency(avg);
		}
		for(int i=0;i<needToFall.size();i++)
			makeSink((Environmental)needToFall.elementAt(i),room,avg);
	}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		switch(UnderWater.isOkUnderWaterAffect(this,affect))
		{
		case -1: return false;
		case 1: return true;
		}
		return super.okAffect(myHost,affect);
	}
	public static int isOkUnderWaterAffect(Room room, Affect affect)
	{
		if(Sense.isSleeping(room)) 
			return 0;
			 
		if((affect.targetMinor()==affect.TYP_FIRE)
		||(affect.targetMinor()==affect.TYP_GAS)
		||(affect.sourceMinor()==affect.TYP_FIRE)
		||(affect.sourceMinor()==affect.TYP_GAS))
		{
			affect.source().tell("That won't work underwater.");
			return -1;
		}
		else
		if((Util.bset(affect.targetCode(),Affect.MASK_HURT))
		&&(affect.tool()!=null)
		&&(affect.tool() instanceof Weapon))
		{
			Weapon w=(Weapon)affect.tool();
			if((w.weaponType()==Weapon.TYPE_SLASHING)
			||(w.weaponType()==Weapon.TYPE_BASHING))
			{
				int damage=affect.targetCode()-Affect.MASK_HURT;
				damage=damage/3;
				if(damage<0) damage=0;
				affect.modify(affect.source(),
							  affect.target(),
							  affect.tool(),
							  affect.sourceCode(),
							  affect.sourceMessage(),
							  Affect.MASK_HURT+damage,
							  affect.targetMessage(),
							  affect.othersCode(),
							  affect.othersMessage());
			}
		}
		else
		if(affect.amITarget(room)
		&&(affect.targetMinor()==Affect.TYP_DRINK)
		&&(room instanceof Drink))
		{
			if(((Drink)room).liquidType()==EnvResource.RESOURCE_SALTWATER)
			{
				affect.source().tell("You don't want to be drinking saltwater.");
				return -1;
			}
			return 1;
		}
		return 0;
	}
	public void affect(Environmental myHost, Affect affect)
	{
		super.affect(myHost,affect);
		UnderWater.sinkAffects(this,affect);
	}
	
	public int thirstQuenched(){return 500;}
	public int liquidHeld(){return Integer.MAX_VALUE-1000;}
	public int liquidRemaining(){return Integer.MAX_VALUE-1000;}
	public int liquidType(){return EnvResource.RESOURCE_FRESHWATER;}
	public void setLiquidType(int newLiquidType){}
	public void setThirstQuenched(int amount){}
	public void setLiquidHeld(int amount){}
	public void setLiquidRemaining(int amount){}
	public boolean containsDrink(){return true;}
	public static final Integer[] resourceList={
		new Integer(EnvResource.RESOURCE_SEAWEED),
		new Integer(EnvResource.RESOURCE_FISH),
		new Integer(EnvResource.RESOURCE_SAND),
		new Integer(EnvResource.RESOURCE_CLAY),
		new Integer(EnvResource.RESOURCE_PEARL),
		new Integer(EnvResource.RESOURCE_LIMESTONE)};
	public static final Vector roomResources=new Vector(Arrays.asList(resourceList));
	public Vector resourceChoices(){return UnderWater.roomResources;}
}
