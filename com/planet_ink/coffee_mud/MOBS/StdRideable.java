package com.planet_ink.coffee_mud.MOBS;


import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class StdRideable extends StdMOB implements Rideable
{
	public String ID(){return "StdRideable";}
	protected int rideBasis=Rideable.RIDEABLE_LAND;
	protected int riderCapacity=2;
	protected Vector riders=new Vector();
	public StdRideable()
	{
		super();
		Username="a horse";
		setDescription("A brown riding horse looks sturdy and reliable.");
		setDisplayText("a horse stands here.");
		baseEnvStats().setWeight(700);
		recoverEnvStats();
	}
	public Environmental newInstance()
	{
		return new StdRideable();
	}
	protected void cloneFix(MOB E)
	{
		super.cloneFix(E);
		riders=new Vector();
	}
	public DeadBody killMeDead(boolean createBody)
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
		return super.killMeDead(createBody);
	}

	// common item/mob stuff
	public int rideBasis(){return rideBasis;}
	public void setRideBasis(int basis){rideBasis=basis;}
	public int riderCapacity(){ return riderCapacity;}
	public void setRiderCapacity(int newCapacity){riderCapacity=newCapacity;}
	public int numRiders(){return riders.size();}
	public boolean mobileRideBasis(){return true;}
	public Rider fetchRider(int which)
	{
		try	{ return (Rider)riders.elementAt(which);	}
		catch(java.lang.ArrayIndexOutOfBoundsException e){}
		return null;
	}
	public String putString(Rider R)
	{
		return "on";
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
	public boolean amRiding(Rider mob)
	{
		return riders.contains(mob);
	}
	public String stateString(Rider R)
	{
		return "riding on";
	}
	public String mountString(int commandType, Rider R)
	{
		return "mount(s)";
	}
	public String dismountString(Rider R)
	{
		return "dismount(s)";
	}
	public String stateStringSubject(Rider R)
	{
		if((R instanceof Rideable)&&((Rideable)R).rideBasis()==Rideable.RIDEABLE_WAGON)
			return "pulling along";
		else
			return "being ridden by";
	}

	public HashSet getRideBuddies(HashSet list)
	{
		if(list==null) return list;
		if(!list.contains(this)) list.add(this);
		for(int r=0;r<numRiders();r++)
		{
			Rider R=fetchRider(r);
			if((R instanceof MOB)
			&&(!list.contains(R)))
				list.add(R);
		}
		return list;
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		switch(msg.targetMinor())
		{
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
				// protects from standard mob rejection
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
			if(msg.amITarget(this))
			{
				msg.source().tell("You cannot simply sit on "+name()+", try 'mount'.");
				return false;
			}
			break;
		case CMMsg.TYP_SLEEP:
			if(amRiding(msg.source()))
			{
				msg.source().tell("You are "+stateString(msg.source())+" "+name()+"!");
				msg.source().setRiding(this);
				return false;
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
			   &&(msg.tool() instanceof Rider))
			{
				if(amRiding((Rider)msg.tool()))
				{
					msg.source().tell(msg.tool().name()+" is "+stateString((Rider)msg.tool())+" "+name()+"!");
					((Rider)msg.tool()).setRiding(this);
					return false;
				}
				if((!(msg.tool() instanceof Rideable))
				&&(msg.amITarget(this))
				&&(((Rideable)msg.tool()).rideBasis()!=Rideable.RIDEABLE_WAGON))
				{
					msg.source().tell(msg.tool().name()+" can not be mounted onto "+name()+"!");
					return false;
				}
				if((baseEnvStats().weight()*5<msg.tool().baseEnvStats().weight())
				&&(msg.amITarget(this)))
				{
					msg.source().tell(name()+" is too small to pull "+msg.tool().name()+".");
					return false;
				}
			}
			else
			if(amRiding(msg.source()))
			{
				msg.source().tell("You are "+stateString(msg.source())+" "+name()+"!");
				msg.source().setRiding(this);
				return false;
			}
			if((riding()!=msg.source())
			&&(msg.amITarget(this)))
			{
				if((numRiders()>=riderCapacity())
				&&(!amRiding(msg.source())))
				{
					// for items
					//msg.source().tell(name()+" is full.");
					// for mobs
					 msg.source().tell("No more can fit on "+name()+".");
					return false;
				}
				// protects from standard mob rejection
				return true;
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
				if((sourceRoom!=null)&&(!msg.amITarget(sourceRoom)))
				{
					Exit E=null;
					if((msg.tool()!=null)&&(msg.tool() instanceof Exit))
					   E=(Exit)msg.tool();
					boolean ok=(!((targetRoom.domainType()&Room.INDOORS)>0)
								||((targetRoom.maxRange()>4)&&((E==null)||(!E.hasADoor()))));
					switch(rideBasis)
					{
					case Rideable.RIDEABLE_LAND:
						if((targetRoom.domainType()==Room.DOMAIN_OUTDOORS_AIR)
						||(targetRoom.domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
						||(targetRoom.domainType()==Room.DOMAIN_INDOORS_UNDERWATER)
						||(targetRoom.domainType()==Room.DOMAIN_INDOORS_WATERSURFACE)
						||(targetRoom.domainType()==Room.DOMAIN_INDOORS_AIR)
						||(targetRoom.domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE))
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
					msg.source().tell(msg.source(),tmob,null,"<T-NAME> must dismount first.");
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
		if(Util.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))
		{
			if((msg.amITarget(this))
			   &&((msg.source().riding()==this)
				  ||(this.amRiding(msg.source()))))
			{
				msg.source().tell("You can't attack "+name()+" right now.");
				if(getVictim()==msg.source()) setVictim(null);
				if(msg.source().getVictim()==this) msg.source().setVictim(null);
				return false;
			}
			else
			if((msg.amISource(this))
			   &&(msg.target()!=null)
			   &&(msg.target() instanceof MOB)
			   &&((amRiding((MOB)msg.target()))
				  ||(((MOB)msg.target()).riding()==this)))

			{
				MOB targ=(MOB)msg.target();
				tell("You can't attack "+targ.name()+" right now.");
				if(getVictim()==targ) setVictim(null);
				if(targ.getVictim()==this) targ.setVictim(null);
				return false;
			}
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
				if(!Util.bset(msg.targetCode(),CMMsg.MASK_OPTIMIZE))
					if(msg.source().location()!=null)
						msg.source().location().recoverRoomStats();
			}
			else
			if(amRiding(msg.source()))
			{
				msg.source().setRiding(null);
				if(!Util.bset(msg.targetCode(),CMMsg.MASK_OPTIMIZE))
					if(msg.source().location()!=null)
						msg.source().location().recoverRoomStats();
			}
			break;
		case CMMsg.TYP_MOUNT:
			if(msg.amITarget(this))
			{
				if((msg.tool()!=null)
				   &&(msg.tool() instanceof Rider))
				{
					((Rider)msg.tool()).setRiding(this);
					if(!Util.bset(msg.targetCode(),CMMsg.MASK_OPTIMIZE))
						if(msg.source().location()!=null)
							msg.source().location().recoverRoomStats();
				}
				else
				if(!amRiding(msg.source()))
				{
					msg.source().setRiding(this);
					if(!Util.bset(msg.targetCode(),CMMsg.MASK_OPTIMIZE))
						if(msg.source().location()!=null)
							msg.source().location().recoverRoomStats();
				}
			}
			break;
		}
		switch(msg.sourceMinor())
		{
		case CMMsg.TYP_QUIT:
		case CMMsg.TYP_PANIC:
		case CMMsg.TYP_DEATH:
			if(amRiding(msg.source()))
			{
			   msg.source().setRiding(null);
				if(!Util.bset(msg.targetCode(),CMMsg.MASK_OPTIMIZE))
					if(msg.source().location()!=null)
						msg.source().location().recoverRoomStats();
			}
			break;
		}
	}
}
