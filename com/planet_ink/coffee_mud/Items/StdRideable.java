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
	}
	public Environmental newInstance()
	{
		return new StdRideable();
	}
	public void destroy()
	{
		while(riders.size()>0)
		{
			Rider mob=fetchRider(0);
			if(mob!=null)
			{
				mob.setRiding(null);
				delRider(mob);
			}
		}
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
	public HashSet getRideBuddies(HashSet list)
	{
		if(list==null) return list;
		for(int r=0;r<numRiders();r++)
		{
			Rider R=fetchRider(r);
			if((R instanceof MOB)
			&&(!list.contains(R)))
				list.add(R);
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
	public String putString(Rider R)
	{
		switch(rideBasis)
		{
		case Rideable.RIDEABLE_AIR:
		case Rideable.RIDEABLE_LAND:
		case Rideable.RIDEABLE_WAGON:
		case Rideable.RIDEABLE_WATER:
		case Rideable.RIDEABLE_SLEEP:
		case Rideable.RIDEABLE_ENTERIN:
			return "in";
		case Rideable.RIDEABLE_SIT:
		case Rideable.RIDEABLE_TABLE:
		case Rideable.RIDEABLE_LADDER:
			return "on";
		}
		return "in";
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
			if(commandType==CMMsg.TYP_SIT)
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
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_ADVANCE:
			if((rideBasis()==Rideable.RIDEABLE_LADDER)
			&&(amRiding(msg.source())))
			{
				msg.source().tell("You cannot advance while "+stateString(msg.source())+" "+name()+"!");
				return false;
			}
			break;
		case CMMsg.TYP_RETREAT:
			if((rideBasis()==Rideable.RIDEABLE_LADDER)
			&&(amRiding(msg.source())))
			{
				msg.source().tell("You cannot retreat while "+stateString(msg.source())+" "+name()+"!");
				return false;
			}
			break;
		case CMMsg.TYP_DISMOUNT:
			if(msg.amITarget(this))
			{
				if((msg.tool()!=null)
				   &&(msg.tool() instanceof Rider))
				{
					if(!amRiding((Rider)msg.tool()))
					{
						msg.source().tell(msg.tool()+" is not "+stateString((Rider)msg.tool())+" "+name()+"!");
						if(((Rider)msg.tool()).riding()==this)
							((Rider)msg.tool()).setRiding(null);
						return false;
					}
				}
				else
				if(!amRiding(msg.source()))
				{
					msg.source().tell("You are not "+stateString(msg.source())+" "+name()+"!");
					if(msg.source().riding()==this)
						msg.source().setRiding(null);
					return false;
				}
				// protects from standard item rejection
				return true;
			}
			break;
		case CMMsg.TYP_SIT:
			if(amRiding(msg.source()))
			{
				msg.source().tell("You are "+stateString(msg.source())+" "+name()+"!");
				msg.source().setRiding(this);
				return false;
			}
			else
			if((riding()!=msg.source())
			&&((rideBasis()==Rideable.RIDEABLE_SIT)
			||(rideBasis()==Rideable.RIDEABLE_ENTERIN)
			||(rideBasis()==Rideable.RIDEABLE_TABLE)
			||(rideBasis()==Rideable.RIDEABLE_SLEEP)))
			{
				if(msg.amITarget(this)
				&&(numRiders()>=riderCapacity())
				&&(!amRiding(msg.source())))
				{
					// for items
					msg.source().tell(name()+" is full.");
					// for mobs
					// msg.source().tell("No more can fit on "+name()+".");
					return false;
				}
				return true;
			}
			else
			if(msg.amITarget(this))
			{
				msg.source().tell("You cannot sit on "+name()+".");
				return false;
			}
			break;
		case CMMsg.TYP_SLEEP:
			if((amRiding(msg.source()))
			&&(((!msg.amITarget(this))&&(msg.target()!=null))
			   ||((rideBasis()!=Rideable.RIDEABLE_SLEEP)&&(rideBasis()!=Rideable.RIDEABLE_ENTERIN))))
			{
				msg.source().tell("You are "+stateString(msg.source())+" "+name()+"!");
				msg.source().setRiding(this);
				return false;
			}
			else
			if((riding()!=msg.source())
			&&((rideBasis()==Rideable.RIDEABLE_SLEEP)
			||(rideBasis()==Rideable.RIDEABLE_ENTERIN)))
			{
				if(msg.amITarget(this)
				&&(numRiders()>=riderCapacity())
				&&(!amRiding(msg.source())))
				{
					// for items
					msg.source().tell(name()+" is full.");
					// for mobs
					// msg.source().tell("No more can fit on "+name()+".");
					return false;
				}
				return true;
			}
			else
			if(msg.amITarget(this))
			{
				msg.source().tell("You cannot lie down on "+name()+".");
				return false;
			}
			break;
		case CMMsg.TYP_MOUNT:
			if((msg.tool()!=null)
			   &&(msg.amITarget(this))
			   &&(msg.tool() instanceof Rider))
			{
				msg.source().tell(msg.tool().name()+" can not be mounted to "+name()+"!");
				return false;
			}
			else
			if(amRiding(msg.source()))
			{
				msg.source().tell("You are "+stateString(msg.source())+" "+name()+"!");
				msg.source().setRiding(this);
				return false;
			}
			if((riding()!=msg.source())
			&&((rideBasis()==Rideable.RIDEABLE_LAND)
			   ||(rideBasis()==Rideable.RIDEABLE_AIR)
			   ||(rideBasis()==Rideable.RIDEABLE_WAGON)
			   ||(rideBasis()==Rideable.RIDEABLE_LADDER)
			   ||(rideBasis()==Rideable.RIDEABLE_WATER)))
			{
				if(msg.amITarget(this))
				{
					if((numRiders()>=riderCapacity())
					&&(!amRiding(msg.source())))
					{
						// for items
						msg.source().tell(name()+" is full.");
						// for mobs
						// msg.source().tell("No more can fit on "+name()+".");
						return false;
					}
					// protects from standard item rejection
					return true;
				}
			}
			else
			if(msg.amITarget(this))
			{
				msg.source().tell("You cannot mount "+name()+".");
				return false;
			}
			break;
		case CMMsg.TYP_ENTER:
			if(amRiding(msg.source())
			&&(msg.target()!=null)
			&&(msg.target() instanceof Room))
			{
				Room sourceRoom=(Room)msg.source().location();
				Room targetRoom=(Room)msg.target();
				Exit E=null;
				if((sourceRoom!=null)&&(!msg.amITarget(sourceRoom)))
				{
					if((msg.tool()!=null)&&(msg.tool() instanceof Exit))
					   E=(Exit)msg.tool();
					boolean ok=(!((targetRoom.domainType()&Room.INDOORS)>0)
								||((targetRoom.maxRange()>4)&&((E==null)||(!E.hasADoor()))));
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
								msg.source().tell(name()+" doesn't seem to be moving.");
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
						else
							ok=true;
						if((targetRoom.domainType()==Room.DOMAIN_INDOORS_AIR)
						||(targetRoom.domainType()==Room.DOMAIN_OUTDOORS_AIR)
						||(targetRoom.domainType()==Room.DOMAIN_INDOORS_UNDERWATER)
						||(targetRoom.domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER))
							ok=false;
						break;
					}
					if(!ok)
					{
						msg.source().tell("You cannot ride "+name()+" that way.");
						return false;
					}
					if(Sense.isSitting(msg.source()))
					{
						msg.source().tell("You cannot crawl while "+stateString(msg.source())+" "+name()+".");
						return false;
					}
				}
			}
			break;
		case CMMsg.TYP_GIVE:
			if(msg.target() instanceof MOB)
			{
				MOB tmob=(MOB)msg.target();
				if((amRiding(tmob))&&(!amRiding(msg.source())))
				{
					if(rideBasis()==Rideable.RIDEABLE_ENTERIN)
						msg.source().tell(msg.source(),tmob,null,"<T-NAME> must exit first.");
					else
						msg.source().tell(msg.source(),tmob,null,"<T-NAME> must disembark first.");
					return false;
				}
			}
			break;
		case CMMsg.TYP_BUY:
		case CMMsg.TYP_SELL:
			if(amRiding(msg.source()))
			{
				msg.source().tell("You cannot do that while "+stateString(msg.source())+" "+name()+".");
				return false;
			}
			break;
		}
		if((Util.bset(msg.sourceMajor(),CMMsg.MASK_HANDS))
		&&(amRiding(msg.source()))
		&&((msg.sourceMessage()!=null)||(msg.othersMessage()!=null))
		&&(((!CoffeeUtensils.reachableItem(msg.source(),msg.target())))
			|| ((!CoffeeUtensils.reachableItem(msg.source(),msg.tool())))
			|| ((msg.sourceMinor()==CMMsg.TYP_GIVE)&&(msg.target()!=null)&&(msg.target() instanceof MOB)&&(msg.target()!=this)&&(!amRiding((MOB)msg.target())))))
		{

			msg.source().tell("You cannot do that while "+stateString(msg.source())+" "+name()+".");
			return false;
		}
		return super.okMessage(myHost,msg);
	}
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_DISMOUNT:
			if((msg.tool()!=null)
			   &&(msg.tool() instanceof Rider))
			{
				((Rider)msg.tool()).setRiding(null);
				if(msg.source().location()!=null)
					msg.source().location().recoverRoomStats();
			}
			else
			if(amRiding(msg.source()))
			{
				msg.source().setRiding(null);
				if(msg.source().location()!=null)
					msg.source().location().recoverRoomStats();
			}
			break;
		case CMMsg.TYP_ENTER:
		case CMMsg.TYP_LEAVE:
		case CMMsg.TYP_FLEE:
			if((rideBasis()==Rideable.RIDEABLE_LADDER)
			&&(amRiding(msg.source())))
			{
				msg.source().setRiding(null);
				if(msg.source().location()!=null)
					msg.source().location().recoverRoomStats();
			}
			break;
		case CMMsg.TYP_MOUNT:
		case CMMsg.TYP_SIT:
		case CMMsg.TYP_SLEEP:
			if(msg.amITarget(this))
			{
				if((msg.tool()!=null)
				   &&(msg.tool() instanceof Rider))
				{
					((Rider)msg.tool()).setRiding(this);
					if(msg.source().location()!=null)
						msg.source().location().recoverRoomStats();
				}
				else
				if(!amRiding(msg.source()))
				{
					msg.source().setRiding(this);
					if(msg.source().location()!=null)
						msg.source().location().recoverRoomStats();
				}
			}
			break;
		}
		switch(msg.sourceMinor())
		{
		case CMMsg.TYP_STAND:
		case CMMsg.TYP_QUIT:
		case CMMsg.TYP_PANIC:
		case CMMsg.TYP_DEATH:
			if(amRiding(msg.source()))
			{
			   msg.source().setRiding(null);
				if(msg.source().location()!=null)
					msg.source().location().recoverRoomStats();
			}
			break;
		}
	}
}
