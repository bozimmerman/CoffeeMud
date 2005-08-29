package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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

public class Falling extends StdAbility
{
	public String ID() { return "Falling"; }
	public String name(){ return "Falling";}
	public String displayText(){ return "(Falling)";}
	protected int canAffectCode(){return CAN_ITEMS|Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	boolean temporarilyDisable=false;
	public Room room=null;
	int damageToTake=0;

	private boolean reversed(){return profficiency()==100;}

	private boolean isWaterSurface(Room R)
	{
		if(R==null) return false;
		if((R.domainType()==Room.DOMAIN_INDOORS_WATERSURFACE)
		||(R.domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE))
			return true;
		return false;
	}
	private boolean isUnderWater(Room R)
	{
		if(R==null) return false;
		if((R.domainType()==Room.DOMAIN_INDOORS_UNDERWATER)
		||(R.domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER))
			return true;
		return false;
	}

	private boolean isAirRoom(Room R)
	{
		if(R==null) return false;
		if((R.domainType()==Room.DOMAIN_INDOORS_AIR)
		||(R.domainType()==Room.DOMAIN_OUTDOORS_AIR))
			return true;
		return false;
	}

	private boolean canFallFrom(Room fromHere, int direction)
	{
		if((fromHere==null)||(direction<0)||(direction>=Directions.NUM_DIRECTIONS))
			return false;

		Room toHere=fromHere.getRoomInDir(direction);
		if((toHere==null)
		||(fromHere.getExitInDir(direction)==null)
		||(!fromHere.getExitInDir(direction).isOpen()))
			return false;
		if(isWaterSurface(fromHere)&&isUnderWater(toHere))
			return false;
		return true;
	}

	private boolean stopFalling(MOB mob)
	{
		if(reversed()) return true;
		unInvoke();
		if(isAirRoom(mob.location()))
			mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> stop(s) falling."+CommonStrings.msp("splat.wav",50));
		else
		if(isWaterSurface(mob.location())||isUnderWater(mob.location()))
			mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> hit(s) the water."+CommonStrings.msp("splat.wav",50));
		else
			mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> hit(s) the ground."+CommonStrings.msp("splat.wav",50));
		MUDFight.postDamage(mob,mob,this,damageToTake,CMMsg.MSG_OK_VISUAL,-1,null);
		mob.delEffect(this);
		return false;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;

		if(tickID!=MudHost.TICK_MOB)
			return true;

		if(affected==null)
			return false;

		int direction=Directions.DOWN;
		String addStr="down";
		if(reversed())
		{
			direction=Directions.UP;
			addStr="upwards";
		}
		if(affected instanceof MOB)
		{
			MOB mob=(MOB)affected;
			if(mob==null) return false;
			if(mob.location()==null) return false;

			if(Sense.isInFlight(mob))
			{
				damageToTake=0;
				unInvoke();
				return false;
			}
			else
			if(!canFallFrom(mob.location(),direction))
				return stopFalling(mob);
			else
			{
				if(mob.envStats().weight()<1)
				{
					mob.tell("\n\r\n\rYou are floating gently "+addStr+".\n\r\n\r");
				}
				else
				{
					mob.tell("\n\r\n\rYOU ARE FALLING "+addStr.toUpperCase()+"!!\n\r\n\r");
					if(!reversed())
						damageToTake+=Dice.roll(1,6,0);
				}
				temporarilyDisable=true;
				MUDTracker.move(mob,direction,false,false);
				temporarilyDisable=false;
				if(!canFallFrom(mob.location(),direction))
					return stopFalling(mob);
				return true;
			}
		}
		else
		if(affected instanceof Item)
		{
			Item item=(Item)affected;
			if((room==null)
			&&(item.owner()!=null)
			&&(item.owner() instanceof Room))
				room=(Room)item.owner();

			if((room==null)
			||((room!=null)&&(!room.isContent(item)))
			||(!Sense.isGettable(item))
			||(Sense.isInFlight(item.ultimateContainer()))
			||(room.getRoomInDir(direction)==null))
			{
				unInvoke();
				return false;
			}
			Room nextRoom=room.getRoomInDir(direction);
			if(canFallFrom(room,direction))
			{
				room.show(invoker,null,item,CMMsg.MSG_OK_ACTION,"<O-NAME> falls "+addStr+".");
				Vector V=new Vector();
				recursiveRoomItems(V,item,room);
				for(int v=0;v<V.size();v++)
				{
					Item thisItem=(Item)V.elementAt(v);
					room.delItem(thisItem);
					nextRoom.addItemRefuse(thisItem,Item.REFUSE_PLAYER_DROP);
				}
				room=nextRoom;
				nextRoom.show(invoker,null,item,CMMsg.MSG_OK_ACTION,"<O-NAME> falls in from "+(reversed()?"below":"above")+".");
				return true;
			}
			if(reversed())
				return true;
			unInvoke();
			return false;
		}

		return false;
	}

	public void recursiveRoomItems(Vector V, Item item, Room room)
	{
		V.addElement(item);
		for(int i=0;i<room.numItems();i++)
		{
			Item newItem=room.fetchItem(i);
			if((newItem!=null)&&(newItem.container()==item))
				recursiveRoomItems(V,newItem,room);
		}
	}


	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if(temporarilyDisable)
			return true;
		MOB mob=msg.source();
		if((affected!=null)&&(affected instanceof MOB))
			if(msg.amISource((MOB)affected))
			{
				if(Sense.isInFlight(mob))
				{
					damageToTake=0;
					unInvoke();
					return true;
				}
				if(Util.bset(msg.targetMajor(),CMMsg.MASK_MOVE))
				{
					msg.source().tell("You are too busy falling to do that right now.");
					return false;
				}
			}
		return true;
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((affected!=null)&&(affected instanceof MOB)&&(msg.amISource((MOB)affected)))
		{
			if(msg.sourceMinor()==CMMsg.TYP_RECALL)
			{
				damageToTake=0;
				unInvoke();
			}
			else
			if((msg.tool()!=null)
			&&(msg.tool() instanceof Ability)
			&&(Util.bset(((Ability)msg.tool()).flags(),Ability.FLAG_TRANSPORTING)))
			{
				damageToTake=0;
				unInvoke();
			}
		}
	}
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if((affectableStats.disposition()&EnvStats.IS_FLYING)==0)
			affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_FALLING);
	}
	public void setAffectedOne(Environmental being)
	{
		if(being instanceof Room)
			room=(Room)being;
		else
			super.setAffectedOne(being);
	}
	public boolean invoke(MOB mob, Vector commands, Environmental target, boolean auto, int asLevel)
	{
		if(!auto) return false;
		Environmental E=target;
		if(E==null) return false;
		if((E instanceof Item)&&(room==null)) return false;
		if(E.fetchEffect("Falling")==null)
		{
			Falling F=new Falling();
			F.setProfficiency(profficiency());
			F.invoker=null;
			if(E instanceof MOB)
				F.invoker=(MOB)E;
			else
				F.invoker=CMClass.getMOB("StdMOB");
			F.setBorrowed(E,true);
			F.makeLongLasting();
			E.addEffect(F);
			if(!(E instanceof MOB))
				CMClass.ThreadEngine().startTickDown(F,MudHost.TICK_MOB,1);
			E.recoverEnvStats();

		}
		return true;
	}
}
