package com.planet_ink.coffee_mud.MOBS;


import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class StdRideable extends StdMOB implements Rideable
{
	protected int rideBasis=Rideable.RIDEABLE_LAND;
	protected int mobCapacity=2;
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
	public DeadBody killMeDead()
	{
		while(riders.size()>0)
		{
			MOB mob=fetchRider(0);
			if(mob!=null)
			{
				mob.setRiding(null);
				delRider(mob);
			}
		}
		return super.killMeDead();
	}
	
	// common item/mob stuff
	public int rideBasis(){return rideBasis;}
	public void setRideBasis(int basis){rideBasis=basis;}
	public int mobCapacity(){ return mobCapacity;}
	public void setMobCapacity(int newCapacity){mobCapacity=newCapacity;}
	public int numRiders(){return riders.size();}
	public boolean mobileRideBasis(){return true;}
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
	public boolean amRiding(MOB mob)
	{
		return riders.contains(mob);
	}
	public String stateString(){return "riding on";}
	public String mountString(int commandType){return "mount(s)";}
	public String dismountString(){return "dismount(s)";}
	public String stateStringSubject(){return "being ridden by";}

	public Hashtable getRideBuddies(Hashtable list)
	{
		if(list==null) return list;
		if(list.get(this)==null) list.put(this,this);
		for(int r=0;r<numRiders();r++)
		{
			MOB R=fetchRider(r);
			if(list.get(R)==null) list.put(R,R);
		}
		return list;
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
				// protects from standard mob rejection
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
			if(affect.amITarget(this))
			{
				affect.source().tell("You cannot simply sit on "+name()+", try 'mount'.");
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
			if(affect.amITarget(this))
			{
				if(numRiders()>=mobCapacity())
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
					affect.source().tell(affect.source(),tmob,"<T-NAME> must dismount first.");
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
			if((affect.amITarget(this))&&(!amRiding(affect.source())))
				affect.source().setRiding(this);
			break;
		}
	}
}
