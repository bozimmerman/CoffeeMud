package com.planet_ink.coffee_mud.Items;


import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class StdRideable extends StdContainer implements Rideable
{
	public String ID(){	return "StdRideable";}
	protected int rideBasis=Rideable.RIDEABLE_WATER;
	protected int riderCapacity=4;
	protected Vector riders=new Vector();
	public StdRideable()
	{
		super();
		setName("a boat");
		setDisplayText("a boat is docked here.");
		setDescription("Looks like a boat");
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
	public void destroy()
	{
		while(riders.size()>0)
			fetchRider(0).setRiding(null);
		super.destroy();
	}

	// common item/mob stuff
	public int rideBasis(){return rideBasis;}
	public void setRideBasis(int basis){rideBasis=basis;}
	public int riderCapacity(){ return riderCapacity;}
	public void setRiderCapacity(int newCapacity){riderCapacity=newCapacity;}
	public int numRiders(){return riders.size();}
	public Rider fetchRider(int which)
	{
		try	{ return (Rider)riders.elementAt(which);	}
		catch(java.lang.ArrayIndexOutOfBoundsException e){}
		return null;
	}
	public void addRider(Rider mob)
	{
		if((mob!=null)&&(!riders.contains(mob)))
			riders.addElement(mob);
	}
	public void delRider(Rider mob)
	{
		if(mob!=null)
			while(riders.removeElement(mob));
	}

	protected void cloneFix(Item E)
	{
		super.cloneFix(E);
		riders=new Vector();
	}
	public Hashtable getRideBuddies(Hashtable list)
	{
		if(list==null) return list;
		for(int r=0;r<numRiders();r++)
		{
			Rider R=fetchRider(r);
			if((R instanceof MOB)
			&&(list.get(R)==null))
				list.put(R,R);
		}
		return list;
	}

	public boolean mobileRideBasis()
	{
		switch(rideBasis)
		{
		case Rideable.RIDEABLE_AIR:
		case Rideable.RIDEABLE_LAND:
		case Rideable.RIDEABLE_WAGON:
		case Rideable.RIDEABLE_WATER:
			return true;
		}
		return false;
	}
	public String stateString(Rider R)
	{
		switch(rideBasis)
		{
		case Rideable.RIDEABLE_AIR:
		case Rideable.RIDEABLE_LAND:
		case Rideable.RIDEABLE_WAGON:
		case Rideable.RIDEABLE_WATER:
			return "riding in";
		case Rideable.RIDEABLE_ENTERIN:
			return "in";
		case Rideable.RIDEABLE_SIT:
			return "on";
		case Rideable.RIDEABLE_TABLE:
			return "at";
		case Rideable.RIDEABLE_LADDER:
			return "climbing on";
		case Rideable.RIDEABLE_SLEEP:
			return "on";
		}
		return "riding in";
	}
	public String mountString(int commandType, Rider R)
	{
		switch(rideBasis)
		{
		case Rideable.RIDEABLE_AIR:
		case Rideable.RIDEABLE_LAND:
		case Rideable.RIDEABLE_WAGON:
		case Rideable.RIDEABLE_WATER:
			return "board(s)";
		case Rideable.RIDEABLE_SIT:
			return "sit(s) on";
		case Rideable.RIDEABLE_TABLE:
			return "sit(s) at";
		case Rideable.RIDEABLE_ENTERIN:
			return "get(s) into";
		case Rideable.RIDEABLE_LADDER:
			return "climb(s) onto";
		case Rideable.RIDEABLE_SLEEP:
			if(commandType==Affect.TYP_SIT)
				return "sit(s) down on";
			else
				return "lie(s) down on";
		}
		return "board(s)";
	}
	public String dismountString(Rider R)
	{
		switch(rideBasis)
		{
		case Rideable.RIDEABLE_AIR:
		case Rideable.RIDEABLE_LAND:
		case Rideable.RIDEABLE_WATER:
			return "disembark(s) from";
		case Rideable.RIDEABLE_TABLE:
			return "get(s) up from";
		case Rideable.RIDEABLE_SIT:
		case Rideable.RIDEABLE_SLEEP:
		case Rideable.RIDEABLE_WAGON:
		case Rideable.RIDEABLE_LADDER:
			return "get(s) off of";
		case Rideable.RIDEABLE_ENTERIN:
			return "get(s) out of";
		}
		return "disembark(s) from";
	}
	public String stateStringSubject(Rider R)
	{
		switch(rideBasis)
		{
		case Rideable.RIDEABLE_AIR:
		case Rideable.RIDEABLE_LAND:
		case Rideable.RIDEABLE_WATER:
		case Rideable.RIDEABLE_WAGON:
			return "being ridden by";
		case Rideable.RIDEABLE_TABLE:
			return "occupied by";
		case Rideable.RIDEABLE_SIT:	return "";
		case Rideable.RIDEABLE_SLEEP: return "";
		case Rideable.RIDEABLE_ENTERIN: return "occupied by";
		case Rideable.RIDEABLE_LADDER: return "occupied by";
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
			if((rideBasis()==Rideable.RIDEABLE_LADDER)
			&&(amRiding(mob)))
			{
				affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_CLIMBING);
				affectableStats.setSpeed(affectableStats.speed()/2);
			}
		}
	}
	
	public String displayText()
	{
 		if((numRiders()>0)&&(stateStringSubject(this).length()>0))
		{
			StringBuffer sendBack=new StringBuffer(name());
			sendBack.append(" "+stateStringSubject(this)+" ");
			for(int r=0;r<numRiders();r++)
			{
				Rider rider=fetchRider(r);
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
	public boolean amRiding(Rider mob)
	{
		return riders.contains(mob);
	}
	public boolean okAffect(Environmental myHost, Affect affect)
	{
		switch(affect.targetMinor())
		{
		case Affect.TYP_ADVANCE:
			if((rideBasis()==Rideable.RIDEABLE_LADDER)
			&&(amRiding(affect.source())))
			{
				affect.source().tell("You cannot advance while "+stateString(affect.source())+" "+name()+"!");
				return false;
			}
			break;
		case Affect.TYP_RETREAT:
			if((rideBasis()==Rideable.RIDEABLE_LADDER)
			&&(amRiding(affect.source())))
			{
				affect.source().tell("You cannot retreat while "+stateString(affect.source())+" "+name()+"!");
				return false;
			}
			break;
		case Affect.TYP_DISMOUNT:
			if(affect.amITarget(this))
			{
				if((affect.tool()!=null)
				   &&(affect.tool() instanceof Rider))
				{
					if(!amRiding((Rider)affect.tool()))
					{
						affect.source().tell(affect.tool()+" is not "+stateString((Rider)affect.tool())+" "+name()+"!");
						if(((Rider)affect.tool()).riding()==this)
							((Rider)affect.tool()).setRiding(null);
						return false;
					}
				}
				else
				if(!amRiding(affect.source()))
				{
					affect.source().tell("You are not "+stateString(affect.source())+" "+name()+"!");
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
				affect.source().tell("You are "+stateString(affect.source())+" "+name()+"!");
				affect.source().setRiding(this);
				return false;
			}
			else
			if((riding()!=affect.source())
			&&((rideBasis()==Rideable.RIDEABLE_SIT)
			||(rideBasis()==Rideable.RIDEABLE_ENTERIN)
			||(rideBasis()==Rideable.RIDEABLE_TABLE)
			||(rideBasis()==Rideable.RIDEABLE_SLEEP)))
			{
				if(affect.amITarget(this)
				&&(numRiders()>=riderCapacity())
				&&(!amRiding(affect.source())))
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
			if((amRiding(affect.source()))
			&&(((!affect.amITarget(this))&&(affect.target()!=null))
			   ||((rideBasis()!=Rideable.RIDEABLE_SLEEP)&&(rideBasis()!=Rideable.RIDEABLE_ENTERIN))))
			{
				affect.source().tell("You are "+stateString(affect.source())+" "+name()+"!");
				affect.source().setRiding(this);
				return false;
			}
			else
			if((riding()!=affect.source())
			&&((rideBasis()==Rideable.RIDEABLE_SLEEP)
			||(rideBasis()==Rideable.RIDEABLE_ENTERIN)))
			{
				if(affect.amITarget(this)
				&&(numRiders()>=riderCapacity())
				&&(!amRiding(affect.source())))
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
			if((affect.tool()!=null)
			   &&(affect.amITarget(this))
			   &&(affect.tool() instanceof Rider))
			{
				affect.source().tell(affect.tool().name()+" can not be mounted to "+name()+"!");
				return false;
			}
			else
			if(amRiding(affect.source()))
			{
				affect.source().tell("You are "+stateString(affect.source())+" "+name()+"!");
				affect.source().setRiding(this);
				return false;
			}
			if((riding()!=affect.source())
			&&((rideBasis()==Rideable.RIDEABLE_LAND)
			   ||(rideBasis()==Rideable.RIDEABLE_AIR)
			   ||(rideBasis()==Rideable.RIDEABLE_WAGON)
			   ||(rideBasis()==Rideable.RIDEABLE_LADDER)
			   ||(rideBasis()==Rideable.RIDEABLE_WATER)))
			{
				if(affect.amITarget(this))
				{
					if((numRiders()>=riderCapacity())
					&&(!amRiding(affect.source())))
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
					case Rideable.RIDEABLE_WAGON:
						if((targetRoom.domainType()==Room.DOMAIN_OUTDOORS_AIR)
						  ||(targetRoom.domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
						  ||(targetRoom.domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE)
						  ||(targetRoom.domainType()==Room.DOMAIN_INDOORS_AIR)
						  ||(targetRoom.domainType()==Room.DOMAIN_INDOORS_UNDERWATER)
						  ||(targetRoom.domainType()==Room.DOMAIN_INDOORS_WATERSURFACE))
							ok=false;
							if((rideBasis==Rideable.RIDEABLE_WAGON)
							&&((riding()==null)
							   ||(!(riding() instanceof MOB))
							   ||(((MOB)riding()).baseEnvStats().weight()<(baseEnvStats().weight()/5))))
							{
								affect.source().tell(name()+" doesn't seem to be moving.");
								return false;
							}
						break;
					case Rideable.RIDEABLE_AIR:
						break;
					case Rideable.RIDEABLE_LADDER:
						ok=true;
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
						affect.source().tell("You cannot crawl while "+stateString(affect.source())+" "+name()+".");
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
					if(rideBasis()==Rideable.RIDEABLE_ENTERIN)
						affect.source().tell(affect.source(),tmob,null,"<T-NAME> must exit first.");
					else
						affect.source().tell(affect.source(),tmob,null,"<T-NAME> must disembark first.");
					return false;
				}
			}
			break;
		case Affect.TYP_BUY:
		case Affect.TYP_SELL:
			if(amRiding(affect.source()))
			{
				affect.source().tell("You cannot do that while "+stateString(affect.source())+" "+name()+".");
				return false;
			}
			break;
		}
		if((Util.bset(affect.sourceMajor(),Affect.MASK_HANDS))
		&&(amRiding(affect.source()))
		&&((affect.sourceMessage()!=null)||(affect.othersMessage()!=null))
		&&(((!CoffeeUtensils.reachableItem(affect.source(),affect.target())))
			|| ((!CoffeeUtensils.reachableItem(affect.source(),affect.tool())))
			|| ((affect.sourceMinor()==Affect.TYP_GIVE)&&(affect.target()!=null)&&(affect.target() instanceof MOB)&&(affect.target()!=this)&&(!amRiding((MOB)affect.target())))))
		{

			affect.source().tell("You cannot do that while "+stateString(affect.source())+" "+name()+".");
			return false;
		}
		return super.okAffect(myHost,affect);
	}
	public void affect(Environmental myHost, Affect affect)
	{
		super.affect(myHost,affect);
		switch(affect.targetMinor())
		{
		case Affect.TYP_DISMOUNT:
			if((affect.tool()!=null)
			   &&(affect.tool() instanceof Rider))
			{
				((Rider)affect.tool()).setRiding(null);
				if(affect.source().location()!=null)
					affect.source().location().recoverRoomStats();
			}
			else
			if(amRiding(affect.source()))
			{
				affect.source().setRiding(null);
				if(affect.source().location()!=null)
					affect.source().location().recoverRoomStats();
			}
			break;
		case Affect.TYP_ENTER:
		case Affect.TYP_LEAVE:
		case Affect.TYP_FLEE:
			if((rideBasis()==Rideable.RIDEABLE_LADDER)
			&&(amRiding(affect.source())))
			{
				affect.source().setRiding(null);
				if(affect.source().location()!=null)
					affect.source().location().recoverRoomStats();
			}
			break;
		case Affect.TYP_MOUNT:
		case Affect.TYP_SIT:
		case Affect.TYP_SLEEP:
			if(affect.amITarget(this))
			{
				if((affect.tool()!=null)
				   &&(affect.tool() instanceof Rider))
				{
					((Rider)affect.tool()).setRiding(this);
					if(affect.source().location()!=null)
						affect.source().location().recoverRoomStats();
				}
				else
				if(!amRiding(affect.source()))
				{
					affect.source().setRiding(this);
					if(affect.source().location()!=null)
						affect.source().location().recoverRoomStats();
				}
			}
			break;
		}
		switch(affect.sourceMinor())
		{
		case Affect.TYP_STAND:
		case Affect.TYP_QUIT:
		case Affect.TYP_PANIC:
		case Affect.TYP_DEATH:
			if(amRiding(affect.source()))
			{
			   affect.source().setRiding(null);
				if(affect.source().location()!=null)
					affect.source().location().recoverRoomStats();
			}
			break;
		}
	}

}
