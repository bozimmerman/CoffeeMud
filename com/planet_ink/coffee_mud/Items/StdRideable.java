package com.planet_ink.coffee_mud.Items;


import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class StdRideable extends StdContainer implements Rideable
{
	public String ID(){	return "StdRideable";}
	protected int rideBasis=Rideable.RIDEABLE_WATER;
	protected int mobCapacity=4;
	protected Vector riders=new Vector();
	public StdRideable()
	{
		super();
		name="a boat";
		displayText="a boat is docked here.";
		description="Looks like a boat";
		baseEnvStats().setWeight(2000);
		recoverEnvStats();
		capacity=3000;
		material=EnvResource.RESOURCE_OAK;
		isReadable=false;
	}
	public Environmental newInstance()
	{
		return new StdRideable();
	}
	public void destroyThis()
	{
		while(riders.size()>0)
			fetchRider(0).setRiding(null);
		super.destroyThis();
	}
	
	// common item/mob stuff
	public int rideBasis(){return rideBasis;}
	public void setRideBasis(int basis){rideBasis=basis;}
	public int mobCapacity(){ return mobCapacity;}
	public void setMobCapacity(int newCapacity){mobCapacity=newCapacity;}
	public int numRiders(){return riders.size();}
	public MOB fetchRider(int which)
	{
		try	{ return (MOB)riders.elementAt(which);	}
		catch(java.lang.ArrayIndexOutOfBoundsException e){}
		return null;
	}
	public void addRider(MOB mob)
	{ 
		if(mob!=null)
			riders.addElement(mob);
	}
	public void delRider(MOB mob)
	{ 
		if(mob!=null)
			riders.removeElement(mob);
	}
	
	public Hashtable getRideBuddies(Hashtable list)
	{
		if(list==null) return list;
		for(int r=0;r<numRiders();r++)
		{
			MOB R=fetchRider(r);
			if(list.get(R)==null) list.put(R,R);
		}
		return list;
	}
	
	public boolean mobileRideBasis()
	{
		switch(rideBasis)
		{
		case Rideable.RIDEABLE_AIR:
		case Rideable.RIDEABLE_LAND:
		case Rideable.RIDEABLE_WATER:
			return true;
		}
		return false;
	}
	public String stateString()
	{
		switch(rideBasis)
		{
		case Rideable.RIDEABLE_AIR:
		case Rideable.RIDEABLE_LAND:
		case Rideable.RIDEABLE_WATER:
			return "riding in";
		case Rideable.RIDEABLE_SIT:
		case Rideable.RIDEABLE_TABLE:
			return "on";
		case Rideable.RIDEABLE_SLEEP:
			return "on";
		}
		return "riding in";
	}
	public String mountString(int commandType)
	{
		switch(rideBasis)
		{
		case Rideable.RIDEABLE_AIR:
		case Rideable.RIDEABLE_LAND:
		case Rideable.RIDEABLE_WATER:
			return "board(s)";
		case Rideable.RIDEABLE_SIT:
			return "sit(s) on";
		case Rideable.RIDEABLE_TABLE:
			return "sit(s) on";
		case Rideable.RIDEABLE_SLEEP:
			if(commandType==Affect.TYP_SIT)
				return "sit(s) down on";
			else
				return "lie(s) down on";
		}
		return "board(s)";
	}
	public String dismountString()
	{
		switch(rideBasis)
		{
		case Rideable.RIDEABLE_AIR:
		case Rideable.RIDEABLE_LAND:
		case Rideable.RIDEABLE_WATER:
			return "disembark(s) from";
		case Rideable.RIDEABLE_SIT:
		case Rideable.RIDEABLE_SLEEP:
		case Rideable.RIDEABLE_TABLE:
			return "get(s) off of";
		}
		return "disembark(s) from";
	}
	public String stateStringSubject()
	{
		switch(rideBasis)
		{
		case Rideable.RIDEABLE_AIR:
		case Rideable.RIDEABLE_LAND:
		case Rideable.RIDEABLE_WATER:
			return "being ridden by";
		case Rideable.RIDEABLE_SIT:	return "";
		case Rideable.RIDEABLE_TABLE: return "";
		case Rideable.RIDEABLE_SLEEP: return "";
		}
		return "";
	}
	
	public void recoverEnvStats()
	{
		super.recoverEnvStats();
		if(rideBasis==Rideable.RIDEABLE_AIR)
			envStats().setDisposition(envStats().disposition()|EnvStats.IS_FLYING);
		else
		if(rideBasis==Rideable.RIDEABLE_WATER)
			envStats().setDisposition(envStats().disposition()|EnvStats.IS_SWIMMING);
	}
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected instanceof MOB)
		{
			MOB mob=(MOB)affected;
			if((mob.isInCombat())&&(mob.rangeToTarget()==0)&&(amRiding(mob)))
			{
				affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()-mob.baseEnvStats().attackAdjustment());
				affectableStats.setDamage(affectableStats.damage()-mob.baseEnvStats().damage());
			}
		}
	}
	public String displayText()
	{
 		if((numRiders()>0)&&(stateStringSubject().length()>0))
		{
			StringBuffer sendBack=new StringBuffer(name());
			sendBack.append(" "+stateStringSubject()+" ");
			for(int r=0;r<numRiders();r++)
			{
				MOB rider=fetchRider(r);
				if(rider!=null)
					if(r>0)
					{
						sendBack.append(", ");
						if(r==numRiders()-1)
							sendBack.append("and ");
					}
					sendBack.append(rider.name());
					
			}
			return sendBack.toString();
		}
		else
			return displayText;
	}
	public boolean amRiding(MOB mob)
	{
		return riders.contains(mob);
	}
	public boolean okAffect(Affect affect)
	{
		switch(affect.targetMinor())
		{
		case Affect.TYP_DISMOUNT:
			if(affect.amITarget(this))
			{
				if(!amRiding(affect.source()))
				{
					affect.source().tell("You are not "+stateString()+" "+name()+"!");
					if(affect.source().riding()==this)
						affect.source().setRiding(null);
					return false;
				}
				// protects from standard item rejection
				return true;
			}
			break;
		case Affect.TYP_SIT:
			if(amRiding(affect.source()))
			{
				affect.source().tell("You are "+stateString()+" "+name()+"!");
				affect.source().setRiding(this);
				return false;
			}
			else
			if(((rideBasis()==Rideable.RIDEABLE_SIT)
			||(rideBasis()==Rideable.RIDEABLE_SLEEP)))
			{
				if(affect.amITarget(this)&&(numRiders()>=mobCapacity()))
				{
					// for items
					affect.source().tell(name()+" is full.");
					// for mobs
					// affect.source().tell("No more can fit on "+name()+".");
					return false;
				}
				return true;
			}
			else
			if(affect.amITarget(this))
			{
				affect.source().tell("You cannot sit on "+name()+".");
				return false;
			}
			break;
		case Affect.TYP_SLEEP:
			if(amRiding(affect.source()))
			{
				affect.source().tell("You are "+stateString()+" "+name()+"!");
				affect.source().setRiding(this);
				return false;
			}
			else
			if(rideBasis()==Rideable.RIDEABLE_SLEEP)
			{
				if(affect.amITarget(this)&&(numRiders()>=mobCapacity()))
				{
					// for items
					affect.source().tell(name()+" is full.");
					// for mobs
					// affect.source().tell("No more can fit on "+name()+".");
					return false;
				}
				return true;
			}
			else
			if(affect.amITarget(this))
			{
				affect.source().tell("You cannot lie down on "+name()+".");
				return false;
			}
			break;
		case Affect.TYP_MOUNT:
			if(amRiding(affect.source()))
			{
				affect.source().tell("You are "+stateString()+" "+name()+"!");
				affect.source().setRiding(this);
				return false;
			}
			else
			if((rideBasis()==Rideable.RIDEABLE_LAND)
			   ||(rideBasis()==Rideable.RIDEABLE_AIR)
			   ||(rideBasis()==Rideable.RIDEABLE_WATER))
			{
				if(affect.amITarget(this))
				{
					if(numRiders()>=mobCapacity())
					{
						// for items
						affect.source().tell(name()+" is full.");
						// for mobs
						// affect.source().tell("No more can fit on "+name()+".");
						return false;
					}
					// protects from standard item rejection
					return true;
				}
			}
			else
			if(affect.amITarget(this))
			{
				affect.source().tell("You cannot mount "+name()+".");
				return false;
			}
			break;
		case Affect.TYP_ENTER:
			if(amRiding(affect.source())
			   &&(affect.target()!=null)
			   &&(affect.target() instanceof Room))
			{
				Room sourceRoom=(Room)affect.source().location();
				Room targetRoom=(Room)affect.target();
				if((sourceRoom!=null)&&(!affect.amITarget(sourceRoom)))
				{
					boolean ok=!((targetRoom.domainType()&Room.INDOORS)>0);
					switch(rideBasis)
					{
					case Rideable.RIDEABLE_LAND:
						if((targetRoom.domainType()==Room.DOMAIN_OUTDOORS_AIR)
						  ||(targetRoom.domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
						  ||(targetRoom.domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE)
						  ||(targetRoom.domainType()==Room.DOMAIN_INDOORS_AIR)
						  ||(targetRoom.domainType()==Room.DOMAIN_INDOORS_UNDERWATER)
						  ||(targetRoom.domainType()==Room.DOMAIN_INDOORS_WATERSURFACE))
							ok=false;
						break;
					case Rideable.RIDEABLE_AIR:
						break;
					case Rideable.RIDEABLE_WATER:
						if((sourceRoom.domainType()!=Room.DOMAIN_OUTDOORS_WATERSURFACE)
						&&(targetRoom.domainType()!=Room.DOMAIN_OUTDOORS_WATERSURFACE)
						&&(sourceRoom.domainType()!=Room.DOMAIN_INDOORS_WATERSURFACE)
						&&(targetRoom.domainType()!=Room.DOMAIN_INDOORS_WATERSURFACE))
							ok=false;
						break;
					}
					if(!ok)
					{
						affect.source().tell("You cannot ride "+name()+" that way.");
						return false;
					}
					if(Sense.isSitting(affect.source()))
					{
						affect.source().tell("You cannot crawl while "+stateString()+" "+name()+".");
						return false;
					}
				}
			}
			break;
		case Affect.TYP_GIVE:
			if(affect.target() instanceof MOB)
			{
				MOB tmob=(MOB)affect.target();
				if((amRiding(tmob))&&(!amRiding(affect.source())))
				{
					affect.source().tell(affect.source(),tmob,"<T-NAME> must disembark first.");
					return false;
				}
			}
			break;
		case Affect.TYP_BUY:
		case Affect.TYP_SELL:
			if(amRiding(affect.source()))
			{
				affect.source().tell("You cannot do that while "+stateString()+" "+name()+".");
				return false;
			}
			break;
		}
		if((Util.bset(affect.sourceMajor(),Affect.ACT_HANDS))
		   &&(amRiding(affect.source())))
		{
			if(((affect.target()!=null)&&(affect.target() instanceof Item)&&(affect.target()!=this)&&(affect.source().location()!=null)&&(affect.source().location().isContent((Item)affect.target())))
			|| ((affect.tool()!=null)&&(affect.tool() instanceof Item)&&(affect.tool()!=this)&&(affect.source().location()!=null)&&(affect.source().location().isContent((Item)affect.tool())))
			|| ((affect.sourceMinor()==Affect.TYP_GIVE)&&(affect.target()!=null)&&(affect.target() instanceof MOB)&&(affect.target()!=this)&&(!amRiding((MOB)affect.target()))))
			{
				
				affect.source().tell("You cannot do that while "+stateString()+" "+name()+".");
				return false;
			}
		}
		return super.okAffect(affect);
	}
	public void affect(Affect affect)
	{
		super.affect(affect);
		switch(affect.targetMinor())
		{
		case Affect.TYP_DISMOUNT:
			if(amRiding(affect.source()))
				affect.source().setRiding(null);
			break;
		case Affect.TYP_MOUNT:
		case Affect.TYP_SIT:
		case Affect.TYP_SLEEP:
			if((affect.amITarget(this))&&(!amRiding(affect.source())))
				affect.source().setRiding(this);
			break;
		}
		if((affect.sourceMinor()==Affect.TYP_STAND)
		&&(amRiding(affect.source())))
		   affect.source().setRiding(null);
	}
}
