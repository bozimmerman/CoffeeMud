package com.planet_ink.coffee_mud.Abilities.Ranger;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;


/* 
   Copyright 2000-2010 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
@SuppressWarnings("unchecked")
public class Ranger_FindWater extends StdAbility
{
	public String ID() { return "Ranger_FindWater"; }
	public String name(){ return "Find Water";}
	public String displayText(){ return "(finding water)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int abstractQuality(){return Ability.QUALITY_OK_SELF;}
	private static final String[] triggerStrings = {"FINDWATER"};
	public String[] triggerStrings(){return triggerStrings;}
    public int classificationCode(){return Ability.ACODE_SKILL|Ability.DOMAIN_NATURELORE;}
	public long flags(){return Ability.FLAG_TRACKING;}

	protected Vector theTrail=null;
	public int nextDirection=-2;
	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		super.unInvoke();
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(tickID==Tickable.TICKID_MOB)
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
				if(mob.isMonster())
				{
					Room nextRoom=mob.location().getRoomInDir(nextDirection);
					if((nextRoom!=null)&&(nextRoom.getArea()==mob.location().getArea()))
					{
						int dir=nextDirection;
						nextDirection=-2;
						CMLib.tracking().move(mob,dir,false,false);
					}
					else
						unInvoke();
				}
				else
					nextDirection=-2;
			}

		}
		return true;
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);

		if((affected==null)||(!(affected instanceof MOB)))
			return;

		MOB mob=(MOB)affected;
		if((msg.amISource(mob))
		&&(msg.amITarget(mob.location()))
		&&(CMLib.flags().canBeSeenBy(mob.location(),mob))
		&&(msg.targetMinor()==CMMsg.TYP_LOOK))
			nextDirection=CMLib.tracking().trackNextDirectionFromHere(theTrail,mob.location(),false);
		else
		if((affected!=null)
		   &&(affected instanceof MOB)
		   &&(msg.target()!=null)
		   &&(msg.amISource((MOB)affected))
		   &&((msg.sourceMinor()==CMMsg.TYP_LOOK)||(msg.sourceMinor()==CMMsg.TYP_EXAMINE)))
		{
			if((msg.tool()!=null)&&(msg.tool().ID().equals(ID())))
			{
				String str=waterHere((MOB)affected,msg.target(),null);
				if(str.length()>0)
					((MOB)affected).tell(str);
			}
			else
			if((msg.target()!=null)
			&&(waterHere((MOB)affected,msg.target(),null).length()>0)
			&&(msg.source()!=msg.target()))
			{
				CMMsg msg2=CMClass.getMsg(msg.source(),msg.target(),this,CMMsg.MSG_LOOK,CMMsg.NO_EFFECT,CMMsg.NO_EFFECT,null);
				msg.addTrailerMsg(msg2);
			}
		}
	}

	public String waterCheck(MOB mob, Item I, Item container, StringBuffer msg)
	{
		if(I==null) return "";
		if(I.container()==container)
		{
			if(((I instanceof Drink))
			&&(((Drink)I).containsDrink())
			&&(CMLib.flags().canBeSeenBy(I,mob)))
				msg.append(I.name()+" contains some sort of liquid.\n\r");
		}
		else
		if((I.container()!=null)&&(I.container().container()==container))
			if(msg.toString().indexOf(I.container().name()+" contains some sort of liquid.")<0)
				msg.append(I.container().name()+" contains some sort of liquid.\n\r");
		return msg.toString();
	}

	public String waterHere(MOB mob, Environmental E, Item container)
	{
		StringBuffer msg=new StringBuffer("");
		if(E==null) return msg.toString();
		if((E instanceof Room)&&(CMLib.flags().canBeSeenBy(E,mob)))
		{
			Room room=(Room)E;
			if((room.domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
			   ||(room.domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE)
			   ||(room.domainType()==Room.DOMAIN_INDOORS_UNDERWATER)
			   ||(room.domainType()==Room.DOMAIN_INDOORS_WATERSURFACE))
				msg.append("Your water-finding senses are saturated.  This is a very wet place.\n\r");
			else
			if(room.domainConditions()==Room.CONDITION_WET)
				msg.append("Your water-finding senses are saturated.  This is a damp place.\n\r");
			else
			if((room.getArea().getClimateObj().weatherType(room)==Climate.WEATHER_RAIN)
			||(room.getArea().getClimateObj().weatherType(room)==Climate.WEATHER_THUNDERSTORM))
				msg.append("It is raining here! Your water-finding senses are saturated!\n\r");
			else
			if(room.getArea().getClimateObj().weatherType(room)==Climate.WEATHER_HAIL)
				msg.append("It is hailing here! Your water-finding senses are saturated!\n\r");
			else
			if(room.getArea().getClimateObj().weatherType(room)==Climate.WEATHER_SNOW)
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
		if((E instanceof Item)&&(CMLib.flags().canBeSeenBy(E,mob)))
		{
			waterCheck(mob,(Item)E,container,msg);
			msg.append(waterHere(mob,((Item)E).owner(),(Item)E));
		}
		else
		if((E instanceof MOB)&&(CMLib.flags().canBeSeenBy(E,mob)))
		{
			for(int i=0;i<((MOB)E).inventorySize();i++)
			{
				Item I=((MOB)E).fetchInventory(i);
				StringBuffer msg2=new StringBuffer("");
				waterCheck(mob,I,container,msg2);
				if(msg2.length()>0)
					return E.name()+" is carrying some liquids.";
			}
			if(CMLib.coffeeShops().getShopKeeper(E)!=null)
			{
				StringBuffer msg2=new StringBuffer("");
				Vector V=CMLib.coffeeShops().getShopKeeper(E).getShop().getStoreInventory();
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


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Vector V=CMLib.flags().flaggedAffects(mob,Ability.FLAG_TRACKING);
		for(int v=0;v<V.size();v++)	((Ability)V.elementAt(v)).unInvoke();
		if(V.size()>0)
		{
			mob.tell("You stop tracking.");
			if(commands.size()==0) return true;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		String here=waterHere(mob,mob.location(),null);
		if(here.length()>0)
		{
			mob.tell(here);
			return true;
		}

		boolean success=proficiencyCheck(mob,0,auto);

		Vector rooms=new Vector();
		TrackingLibrary.TrackingFlags flags;
		flags = new TrackingLibrary.TrackingFlags()
				.add(TrackingLibrary.TrackingFlag.NOEMPTYGRIDS)
				.add(TrackingLibrary.TrackingFlag.NOAIR);
		Vector checkSet=CMLib.tracking().getRadiantRooms(mob.location(),flags,60+(2*getXLEVELLevel(mob)));
		for(Enumeration r=checkSet.elements();r.hasMoreElements();)
		{
			Room R=CMLib.map().getRoom((Room)r.nextElement());
			if(waterHere(mob,R,null).length()>0)
				rooms.addElement(R);
		}

		if(rooms.size()>0)
			theTrail=CMLib.tracking().findBastardTheBestWay(mob.location(),rooms,flags,60+(2*getXLEVELLevel(mob)));

		if((success)&&(theTrail!=null))
		{
			CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,auto?"<S-NAME> begin(s) sniffing around for water!":"<S-NAME> begin(s) sensing water.");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Ranger_FindWater newOne=(Ranger_FindWater)this.copyOf();
				if(mob.fetchEffect(newOne.ID())==null)
					mob.addEffect(newOne);
				mob.recoverEnvStats();
				newOne.nextDirection=CMLib.tracking().trackNextDirectionFromHere(newOne.theTrail,mob.location(),false);
			}
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> attempt(s) to find water, but fail(s).");

		return success;
	}
}
