package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class Spell_DetectWater extends Spell
{
	public String ID() { return "Spell_DetectWater"; }
	public String name(){return "Detect Water";}
	public String displayText(){return "(Detecting Water)";}
	public int quality(){ return OK_SELF;}
	protected int canAffectCode(){return CAN_MOBS;}
	Room lastRoom=null;
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_DIVINATION;	}

	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(canBeUninvoked())
			lastRoom=null;
		super.unInvoke();
		if(canBeUninvoked())
			mob.tell("Your senses are no longer sensitive to liquids.");
	}
	public String waterCheck(MOB mob, Item I, Item container, StringBuffer msg)
	{
		if(I==null) return "";
		if(I.container()==container)
		{
			if(((I instanceof Drink))
			&&(((Drink)I).containsDrink())
			&&(Sense.canBeSeenBy(I,mob)))
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
		if((E instanceof Room)&&(Sense.canBeSeenBy(E,mob)))
		{
			Room room=(Room)E;
			if((room.domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
			||(room.domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE)
			||(room.domainType()==Room.DOMAIN_INDOORS_UNDERWATER)
			||(room.domainType()==Room.DOMAIN_INDOORS_WATERSURFACE))
				msg.append("Your liquid senses are saturated.  This is a very wet place.\n\r");
			else
			if(room.domainConditions()==Room.CONDITION_WET)
				msg.append("Your liquid senses are saturated.  This is a damp place.\n\r");
			else
			if((room.getArea().getClimateObj().weatherType(room)==Climate.WEATHER_RAIN)
			||(room.getArea().getClimateObj().weatherType(room)==Climate.WEATHER_THUNDERSTORM))
				msg.append("It is raining here! Your liquid senses are saturated!\n\r");
			else
			if(room.getArea().getClimateObj().weatherType(room)==Climate.WEATHER_HAIL)
				msg.append("It is hailing here! Your liquid senses are saturated!\n\r");
			else
			if(room.getArea().getClimateObj().weatherType(room)==Climate.WEATHER_SNOW)
				msg.append("It is snowing here! Your liquid senses are saturated!\n\r");
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
			msg.append(waterHere(mob,((Item)E).owner(),(Item)E));
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
			if(CoffeeUtensils.getShopKeeper((MOB)E)!=null)
			{
				StringBuffer msg2=new StringBuffer("");
				Vector V=CoffeeUtensils.getShopKeeper((MOB)E).getUniqueStoreInventory();
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
	public void messageTo(MOB mob)
	{
		String last="";
		String dirs="";
		for(int d=0;d<=Directions.NUM_DIRECTIONS;d++)
		{
			Room R=null;
			Exit E=null;
			if(d<Directions.NUM_DIRECTIONS)
			{
				R=mob.location().getRoomInDir(d);
				E=mob.location().getExitInDir(d);
			}
			else
			{
				R=mob.location();
				E=CMClass.getExit("StdExit");
			}
			if((R!=null)&&(E!=null))
			{
				boolean metalFound=false;
				if(waterHere(mob,R,null).length()>0)
					metalFound=true;
				else
				for(int m=0;m<R.numInhabitants();m++)
				{
					MOB M=R.fetchInhabitant(m);
					if((M!=null)&&(M!=mob)&&(waterHere(mob,M,null).length()>0))
					{ metalFound=true; break;}
				}

				if(metalFound)
				{
					if(last.length()>0)
						dirs+=", "+last;
					if(d>=Directions.NUM_DIRECTIONS)
						last="here";
					else
						last=Directions.getFromDirectionName(d);
				}
			}
		}

		if((dirs.length()!=0)||(last.length()!=0))
		{
			if(dirs.length()==0)
				mob.tell("Water smells are coming from "+last+".");
			else
				mob.tell("Water smells are coming from "+dirs.substring(2)+", and "+last+".");
		}
	}
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((tickID==MudHost.TICK_MOB)
		   &&(affected!=null)
		   &&(affected instanceof MOB)
		   &&(((MOB)affected).location()!=null)
		   &&((lastRoom==null)||(((MOB)affected).location()!=lastRoom)))
		{
			lastRoom=((MOB)affected).location();
			messageTo((MOB)affected);
		}
		return true;
	}


	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((affected!=null)
		   &&(affected instanceof MOB)
		   &&(msg.target()!=null)
		   &&(msg.amISource((MOB)affected))
		   &&(msg.sourceMinor()==CMMsg.TYP_EXAMINESOMETHING))
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
				FullMsg msg2=new FullMsg(msg.source(),msg.target(),this,CMMsg.MSG_EXAMINESOMETHING,CMMsg.NO_EFFECT,CMMsg.NO_EFFECT,null);
				msg.addTrailerMsg(msg2);
			}
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target,null,null,"<S-NAME> <S-IS-ARE> already detecting liquid things.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;


		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<T-NAME> gain(s) liquid sensitivities!":"^S<S-NAME> incant(s) softly, and gain(s) liquid sensitivities!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> incant(s) and open(s) <S-HIS-HER> liquified eyes, but the spell fizzles.");

		return success;
	}
}
