package com.planet_ink.coffee_mud.Items;


import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class StdRideable extends StdContainer implements Rideable
{
	protected int rideBasis=Rideable.RIDEABLE_WATER;
	protected int mobCapacity=4;
	protected Vector riders=new Vector();
	public StdRideable()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a boat";
		displayText="a boat is docked here.";
		description="Looks like a boat";
		baseEnvStats().setWeight(2000);
		recoverEnvStats();
		capacity=2000;
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
	public void recoverEnvStats()
	{
		super.recoverEnvStats();
		if(rideBasis==Rideable.RIDEABLE_AIR)
			envStats().setDisposition(envStats().disposition()|Sense.IS_FLYING);
		else
		if(rideBasis==Rideable.RIDEABLE_WATER)
			envStats().setDisposition(envStats().disposition()|Sense.IS_SWIMMING);
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
 		if(numRiders()>0)
		{
			StringBuffer sendBack=new StringBuffer(name());
			sendBack.append(" is being ridden by ");
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
			if(!amRiding(affect.source()))
			{
				affect.source().tell("You are not riding "+name()+"!");
				if(affect.source().riding()==this)
					affect.source().setRiding(null);
				return false;
			}
			// protects from standard item rejection
			return true;
		case Affect.TYP_MOUNT:
			if(amRiding(affect.source()))
			{
				affect.source().tell("You are already riding "+name()+"!");
				affect.source().setRiding(this);
				return false;
			}
			else
			if(affect.amITarget(this)&&(numRiders()>=mobCapacity()))
			{
				// for items
				affect.source().tell(name()+" is full.");
				// for mobs
				// affect.source().tell("No more can fit on "+name()+".");
				return false;
			}
			// protects from standard item rejection
			return true;
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
						  ||(targetRoom.domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE))
							ok=false;
						break;
					case Rideable.RIDEABLE_AIR:
						break;
					case Rideable.RIDEABLE_WATER:
						if((sourceRoom.domainType()!=Room.DOMAIN_OUTDOORS_WATERSURFACE)
							&&(targetRoom.domainType()!=Room.DOMAIN_OUTDOORS_WATERSURFACE))
							ok=false;
						break;
					}
					if(!ok)
					{
						affect.source().tell("You cannot ride "+name()+" that way.");
						return false;
					}
				}
			}
			break;
		case Affect.TYP_BUY:
		case Affect.TYP_SELL:
			if(amRiding(affect.source()))
			{
				affect.source().tell("You cannot do that while riding "+name()+".");
				return false;
			}
			break;
		}
		if((Util.bset(affect.sourceMajor(),Affect.ACT_HANDS))&&(amRiding(affect.source())))
		{
			if(((affect.target()!=null)&&(affect.target() instanceof Item)&&(affect.target()!=this)&&(affect.source().location()!=null)&&(affect.source().location().isContent((Item)affect.target())))
			|| ((affect.tool()!=null)&&(affect.tool() instanceof Item)&&(affect.tool()!=this)&&(affect.source().location()!=null)&&(affect.source().location().isContent((Item)affect.tool())))
			|| ((affect.sourceMinor()==Affect.TYP_GIVE)&&(affect.target()!=null)&&(affect.target() instanceof MOB)&&(affect.target()!=this)&&(!amRiding((MOB)affect.target()))))
			{
				
				affect.source().tell("You cannot do that while riding "+name()+".");
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
			if(!amRiding(affect.source()))
				affect.source().setRiding(this);
			break;
		}
	}
}
