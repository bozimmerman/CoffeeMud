package com.planet_ink.coffee_mud.Abilities.Skills;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import java.util.*;


public class Skill_FindWater extends StdAbility
{
	Room lastRoom=null;
	private Vector theTrail=null;
	private Hashtable lookedIn=null;
	public int nextDirection=-2;
	protected final static int TRACK_ATTEMPTS=25;
	public Skill_FindWater()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Find Water";
		displayText="(finding water)";
		miscText="";
		triggerStrings.addElement("FINDWATER");

		baseEnvStats().setLevel(1);

		canBeUninvoked=true;
		isAutoinvoked=false;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Skill_FindWater();
	}
	public int classificationCode()
	{
		return Ability.SKILL;
	}
	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		lastRoom=null;
		super.unInvoke();
	}
	public int nextDirectionFromHere(Room location)
	{
		if(theTrail==null)
			return -1;
		if(location==theTrail.elementAt(0))
			return 999;

		Room nextRoom=null;
		int bestDirection=-1;
		int trailLength=Integer.MAX_VALUE;
		for(int dirs=0;dirs<Directions.NUM_DIRECTIONS;dirs++)
		{
			Room thisRoom=location.getRoomInDir(dirs);
			Exit thisExit=location.getExitInDir(dirs);
			if((thisRoom!=null)&&(thisExit!=null))
			{
				for(int trail=0;trail<theTrail.size();trail++)
				{
					if((theTrail.elementAt(trail)==thisRoom)
					&&(trail<trailLength))
					{
						bestDirection=dirs;
						trailLength=trail;
						nextRoom=thisRoom;
					}
				}
			}
		}
		return bestDirection;
	}

	public boolean tick(int tickID)
	{
		if(!super.tick(tickID))
			return false;
		if(tickID==Host.MOB_TICK)
		{
			if(nextDirection==-999)
				return true;

			if((theTrail==null)
			||(affected == null)
			||(!(affected instanceof MOB)))
				return false;

			MOB mob=(MOB)affected;

			if(nextDirection==999)
			{
				mob.tell(waterHere(mob,mob.location(),null));
				nextDirection=-2;
				unInvoke();
			}
			else
			if(nextDirection==-1)
			{
				if(waterHere(mob,mob.location(),null).length()==0)
					mob.tell("The water trail dries up here.");
				nextDirection=-999;
				unInvoke();
			}
			else
			if(nextDirection>=0)
			{
				mob.tell("The water trail seems to continue "+Directions.getDirectionName(nextDirection)+".");
				nextDirection=-2;
			}

		}
		return true;
	}

	public void affect(Affect affect)
	{
		super.affect(affect);

		if((affected==null)||(!(affected instanceof MOB)))
			return;

		MOB mob=(MOB)affected;
		if((affect.amISource(mob))
		&&(affect.amITarget(mob.location()))
		&&(Sense.canBeSeenBy(mob.location(),mob))
		&&(affect.targetMinor()==Affect.TYP_EXAMINESOMETHING))
			nextDirection=nextDirectionFromHere(mob.location());
		else
		if((affected!=null)
		   &&(affected instanceof MOB)
		   &&(affect.target()!=null)
		   &&(affect.amISource((MOB)affected))
		   &&(affect.sourceMinor()==Affect.TYP_EXAMINESOMETHING))
		{
			if((affect.tool()!=null)&&(affect.tool().ID().equals(ID())))
			{
				String msg=waterHere((MOB)affected,affect.target(),null);
				if(msg.length()>0)
					((MOB)affected).tell(msg);
			}
			else
			{
				FullMsg msg=new FullMsg(affect.source(),affect.target(),this,affect.MSG_EXAMINESOMETHING,affect.NO_EFFECT,affect.NO_EFFECT,null);
				affect.addTrailerMsg(msg);
			}
		}
	}

	public boolean findTheBastard(Room location, MOB mob, int tryCode, Vector dirVec)
	{
		if(lookedIn==null) return false;
		if(lookedIn.get(location)!=null)
			return false;
		lookedIn.put(location,location);
		for(int x=0;x<dirVec.size();x++)
		{
			int i=((Integer)dirVec.elementAt(x)).intValue();
			Room nextRoom=location.getRoomInDir(i);
			Exit nextExit=location.getExitInDir(i);
			if((nextRoom!=null)&&(nextExit!=null))
			{
				if((waterHere(mob,nextRoom,null).length()>0)
				||(findTheBastard(nextRoom,mob,tryCode,dirVec)))
				{
					if(theTrail==null)
						theTrail=new Vector();
					theTrail.addElement(nextRoom);
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean findBastardTheBestWay(Room location, MOB mob)
	{
		
		Vector trailArray[] = new Vector[TRACK_ATTEMPTS];
		
		for(int t=0;t<TRACK_ATTEMPTS;t++)
		{
			lookedIn=new Hashtable();
			theTrail=null;
			Vector dirVec=new Vector();
			while(dirVec.size()<Directions.NUM_DIRECTIONS)
			{
				int direction=Dice.roll(1,Directions.NUM_DIRECTIONS,-1);
				for(int x=0;x<dirVec.size();x++)
					if(((Integer)dirVec.elementAt(x)).intValue()==direction)
						continue;
				dirVec.addElement(new Integer(direction));
			}
			findTheBastard(location,mob,2,dirVec);
			trailArray[t]=theTrail;
		}
		int winner=-1;
		int winningTotal=Integer.MAX_VALUE;
		for(int t=0;t<TRACK_ATTEMPTS;t++)
		{
			Vector V=trailArray[t];
			if((V!=null)&&(V.size()<winningTotal))
			{
				winningTotal=V.size();
				winner=t;
			}
		}
		if(winner<0)
		{
			theTrail=null;
			return false;
		}
		else
		{
			theTrail=trailArray[winner];
			return true;
		}
	}
	
	public String waterCheck(MOB mob, Item I, Item container, StringBuffer msg)
	{
		if(I==null) return "";
		if(I.location()==container)
		{
			if(((I instanceof Drink))
			&&(((Drink)I).containsDrink())
			&&(Sense.canBeSeenBy(I,mob)))
				msg.append(I.name()+" contains some sort of liquid.\n\r");
		}
		else
		if((I.location()!=null)&&(I.location().location()==container))
			if(msg.toString().indexOf(I.location().name()+" contains some sort of liquid.")<0)
				msg.append(I.location().name()+" contains some sort of liquid.\n\r");
		return msg.toString();
	}
	
	public String waterHere(MOB mob, Environmental E, Item container)
	{
		StringBuffer msg=new StringBuffer("");
		if(E==null) return msg.toString();
		if((E instanceof Room)&&(Sense.canBeSeenBy(E,mob)))
		{
			Room room=(Room)E;
			if((room.domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
			   ||(room.domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE))
				msg.append("Your water-finding senses are saturated.  This is a very wet place.\n\r");
			else
			if(room.domainConditions()==Room.CONDITION_WET)
				msg.append("Your water-finding senses are saturated.  This is a damp place.\n\r");
			else
			if((room.getArea().weatherType(room)==Area.WEATHER_RAIN)
			||(room.getArea().weatherType(room)==Area.WEATHER_THUNDERSTORM))
				msg.append("It is raining here! Your water-finding senses are saturated!\n\r");
			else
			if(room.getArea().weatherType(room)==Area.WEATHER_HAIL)
				msg.append("It is hailing here! Your water-finding senses are saturated!\n\r");
			else
			if(room.getArea().weatherType(room)==Area.WEATHER_SNOW)
				msg.append("It is snowing here! Your water-finding senses are saturated!\n\r");
			else
			{
				for(int i=0;i<room.numItems();i++)
				{
					Item I=room.fetchItem(i);
					waterCheck(mob,I,container,msg);
				}
				for(int m=0;m<room.numInhabitants();m++)
				{
					MOB M=room.fetchInhabitant(m);
					if((M!=null)&&(M!=mob))
						msg.append(waterHere(mob,M,null));
				}
			}
		}
		else
		if((E instanceof Item)&&(Sense.canBeSeenBy(E,mob)))
		{
			waterCheck(mob,(Item)E,container,msg);
			msg.append(waterHere(mob,((Item)E).myOwner(),(Item)E));
		}
		else
		if((E instanceof MOB)&&(Sense.canBeSeenBy(E,mob)))
		{
			for(int i=0;i<((MOB)E).inventorySize();i++)
			{
				Item I=((MOB)E).fetchInventory(i);
				StringBuffer msg2=new StringBuffer("");
				waterCheck(mob,I,container,msg2);
				if(msg2.length()>0)
					return E.name()+" is carrying some liquids.";
			}
			if(E instanceof ShopKeeper)
			{
				StringBuffer msg2=new StringBuffer("");
				Vector V=((ShopKeeper)E).getUniqueStoreInventory();
				for(int v=0;v<V.size();v++)
				{
					Environmental E2=(Environmental)V.elementAt(v);
					if(E2 instanceof Item)
						waterCheck(mob,(Item)E2,container,msg2);
					if(msg2.length()>0)
						return E.name()+" has some liquids in stock.";
				}
			}
		}
		return msg.toString();
	}
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(mob.fetchAffect(this.ID())!=null)
		{
			mob.tell("You are already trying to find water.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		String here=waterHere(mob,mob.location(),null);
		if(here.length()>0)
		{
			mob.tell(here);
			return true;
		}
		
		boolean success=profficiencyCheck(0,auto);

		lookedIn=new Hashtable();
		findBastardTheBestWay(mob.location(),mob);

		if((success)&&(theTrail!=null))
		{
			FullMsg msg=new FullMsg(mob,null,this,Affect.MSG_QUIETMOVEMENT,auto?"<S-NAME> begin(s) sniffing around for water!":"<S-NAME> begin(s) sensing water.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				Skill_FindWater newOne=(Skill_FindWater)this.copyOf();
				if(mob.fetchAffect(newOne.ID())==null)
					mob.addAffect(newOne);
				mob.recoverEnvStats();
				newOne.nextDirection=newOne.nextDirectionFromHere(mob.location());
			}
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> attempt(s) to find water, but fail(s).");

		return success;
	}
}