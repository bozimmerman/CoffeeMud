package com.planet_ink.coffee_mud.MOBS;


import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class StdRideable extends StdMOB implements Rideable
{
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
	public void addRider(Rider mob)
	{ 
		if(mob!=null)
			riders.addElement(mob);
	}
	public void delRider(Rider mob)
	{ 
		if(mob!=null)
			riders.removeElement(mob);
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

	public Hashtable getRideBuddies(Hashtable list)
	{
		if(list==null) return list;
		if(list.get(this)==null) list.put(this,this);
		for(int r=0;r<numRiders();r++)
		{
			Rider R=fetchRider(r);
			if((R instanceof MOB)
			&&(list.get(R)==null))
				list.put(R,R);
		}
		return list;
	}
	
	public boolean okAffect(Environmental myHost, Affect affect)
	{
		switch(affect.targetMinor())
		{
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
				// protects from standard mob rejection
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
			if(affect.amITarget(this))
			{
				affect.source().tell("You cannot simply sit on "+name()+", try 'mount'.");
				return false;
			}
			break;
		case Affect.TYP_SLEEP:
			if(amRiding(affect.source()))
			{
				affect.source().tell("You are "+stateString(affect.source())+" "+name()+"!");
				affect.source().setRiding(this);
				return false;
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
			   &&(affect.tool() instanceof Rider))
			{
				if(amRiding((Rider)affect.tool()))
				{
					affect.source().tell(affect.tool().name()+" is "+stateString((Rider)affect.tool())+" "+name()+"!");
					((Rider)affect.tool()).setRiding(this);
					return false;
				}
				if((!(affect.tool() instanceof Rideable))
				&&(affect.amITarget(this))
				&&(((Rideable)affect.tool()).rideBasis()!=Rideable.RIDEABLE_WAGON))
				{
					affect.source().tell(affect.tool().name()+" can not be mounted onto "+name()+"!");
					return false;
				}
				if((baseEnvStats().weight()*5<affect.tool().baseEnvStats().weight())
				&&(affect.amITarget(this)))
				{
					affect.source().tell(name()+" is too small to pull "+affect.tool().name()+".");
					return false;
				}
			}
			else
			if(amRiding(affect.source()))
			{
				affect.source().tell("You are "+stateString(affect.source())+" "+name()+"!");
				affect.source().setRiding(this);
				return false;
			}
			if((riding()!=affect.source())
			&&(affect.amITarget(this)))
			{
				if(numRiders()>=riderCapacity())
				{
					// for items
					//affect.source().tell(name()+" is full.");
					// for mobs
					 affect.source().tell("No more can fit on "+name()+".");
					return false;
				}
				// protects from standard mob rejection
				return true;
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
					affect.source().tell(affect.source(),tmob,null,"<T-NAME> must dismount first.");
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
		   &&(amRiding(affect.source())))
		{
			if(((affect.target()!=null)&&(affect.target() instanceof Item)&&(affect.target()!=this)&&(affect.source().location()!=null)&&(affect.source().location().isContent((Item)affect.target())))
			|| ((affect.tool()!=null)&&(affect.tool() instanceof Item)&&(affect.tool()!=this)&&(affect.source().location()!=null)&&(affect.source().location().isContent((Item)affect.tool())))
			|| ((affect.sourceMinor()==Affect.TYP_GIVE)&&(affect.target()!=null)&&(affect.target() instanceof MOB)&&(affect.target()!=this)&&(!amRiding((MOB)affect.target()))))
			{
				affect.source().tell("You cannot do that while "+stateString(affect.source())+" "+name()+".");
				return false;
			}
		}
		if(Util.bset(affect.targetCode(),Affect.MASK_MALICIOUS))
		{
			if((affect.amITarget(this))
			   &&((affect.source().riding()==this)
				  ||(this.amRiding(affect.source()))))
			{
				affect.source().tell("You can't attack "+name()+" right now.");
				if(getVictim()==affect.source()) setVictim(null);
				if(affect.source().getVictim()==this) affect.source().setVictim(null);
				return false;
			}
			else
			if((affect.amISource(this))
			   &&(affect.target()!=null)
			   &&(affect.target() instanceof MOB)
			   &&((amRiding((MOB)affect.target()))
				  ||(((MOB)affect.target()).riding()==this)))
			   
			{
				MOB targ=(MOB)affect.target();
				tell("You can't attack "+targ.name()+" right now.");
				if(getVictim()==targ) setVictim(null);
				if(targ.getVictim()==this) targ.setVictim(null);
				return false;
			}
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
		case Affect.TYP_MOUNT:
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
	}
}
