package com.planet_ink.coffee_mud.Abilities.Misc;
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
public class Sinking extends StdAbility
{
	public String ID() { return "Sinking"; }
	public String name(){ return "Sinking";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_ITEMS|Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}
    protected boolean isTreading=false;
	public Room room=null;
	protected int sinkTickDown=1;

    protected boolean reversed(){return proficiency()==100;}

    protected boolean isWaterSurface(Room R)
	{
		if(R==null) return false;
		if((R.domainType()==Room.DOMAIN_INDOORS_WATERSURFACE)
		||(R.domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE))
			return true;
		return false;
	}
    protected boolean isUnderWater(Room R)
	{
		if(R==null) return false;
		if((R.domainType()==Room.DOMAIN_INDOORS_UNDERWATER)
		||(R.domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER))
			return true;
		return false;
	}

    protected boolean canSinkFrom(Room fromHere, int direction)
	{
		if((fromHere==null)||(direction<0)||(direction>=Directions.NUM_DIRECTIONS()))
			return false;

		Room toHere=fromHere.getRoomInDir(direction);
		if((toHere==null)
		||(fromHere.getExitInDir(direction)==null)
		||(!fromHere.getExitInDir(direction).isOpen()))
			return false;
		if((!isWaterSurface(toHere))&&(!isUnderWater(toHere)))
			return false;
		return true;
	}

    protected boolean stopSinking(MOB mob)
	{
		unInvoke();
		mob.delEffect(this);
		return false;
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if((affected!=null)&&(affected instanceof MOB)&&(msg.amISource((MOB)affected)))
		{
			if((msg.sourceMinor()==CMMsg.TYP_ENTER)
			&&(msg.target() instanceof Room)
			&&((((Room)msg.target()).domainType()==Room.DOMAIN_INDOORS_AIR)
			   ||(((Room)msg.target()).domainType()==Room.DOMAIN_OUTDOORS_AIR))
			&&(!CMLib.flags().isFlying(msg.source())))
			{
				msg.source().tell("You can't seem to get there from here.");
				return false;
			}
		}
		return true;
	}
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		MOB mob=msg.source();
		if((affected!=null)&&(affected instanceof MOB)&&(msg.amISource((MOB)affected)))
		{
			if(msg.sourceMinor()==CMMsg.TYP_RECALL)
				stopSinking(mob);
			else
			if((msg.tool()!=null)
			&&(msg.tool() instanceof Ability)
			&&(CMath.bset(((Ability)msg.tool()).flags(),Ability.FLAG_TRANSPORTING)))
				stopSinking(mob);
		}
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;

		if(tickID!=Tickable.TICKID_MOB)
			return true;

		if(affected==null)
			return false;

		if((--sinkTickDown)>0)
			return true;
		sinkTickDown=1;
		
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
			Room R=mob.location();
			if(R==null) return false;

			if(!isWaterSurface(R)
			||(CMLib.flags().isWaterWorthy(mob))
			||(CMLib.flags().isInFlight(mob))
			||(mob.envStats().weight()<1)
			||(!canSinkFrom(R,direction)))
				return stopSinking(mob);
			
			Ability A=mob.fetchAbility("Skill_Swim");
			if(((direction==Directions.DOWN)&&(A!=null))
			&&(A.proficiencyCheck(mob,25,(A.proficiency()>=75))
			&&(mob.curState().getMovement()>0)))
			{
				if((R.show(mob,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> tread(s) water."))
				&&(!mob.isMonster()))
				{
                    isTreading=true;
					mob.curState().expendEnergy(mob,mob.maxState(),true);
                    mob.recoverEnvStats();
					return true;
				}
			}
            isTreading=false;
            mob.recoverEnvStats();
			mob.tell("\n\r\n\rYOU ARE SINKING "+addStr.toUpperCase()+"!!\n\r\n\r");
			CMLib.tracking().move(mob,direction,false,false);
			R=mob.location();
			if((R!=null)&&(!canSinkFrom(R,direction)))
			{
				return stopSinking(mob);
			}
			return true;
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
			||(!CMLib.flags().isGettable(item))
			||CMLib.flags().isInFlight(item.ultimateContainer())
			||(CMLib.flags().isWaterWorthy(item.ultimateContainer()))
			||(item.envStats().weight()<1))
			{
				unInvoke();
				return false;
			}
			if(room.numItems()>100)
			{
				sinkTickDown=CMLib.dice().roll(1,room.numItems()/50,0);
				if((--sinkTickDown)>0)
					return true;
			}
			Room nextRoom=room.getRoomInDir(direction);
			if((nextRoom!=null)&&(canSinkFrom(room,direction)))
			{
				room.show(invoker,null,item,CMMsg.MSG_OK_ACTION,"<O-NAME> sinks "+addStr+".");
				Vector V=new Vector();
				recursiveRoomItems(V,item,room);
				for(int v=0;v<V.size();v++)
				{
					Item thisItem=(Item)V.elementAt(v);
					room.delItem(thisItem);
					nextRoom.addItemRefuse(thisItem,CMProps.getIntVar(CMProps.SYSTEMI_EXPIRE_PLAYER_DROP));
				}
				room=nextRoom;
				nextRoom.show(invoker,null,item,CMMsg.MSG_OK_ACTION,"<O-NAME> sinks in from "+(reversed()?"below":"above")+".");
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

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if((!CMLib.flags().isWaterWorthy(affected))
		&&(!CMLib.flags().isInFlight(affected))
        &&(!isTreading)
		&&(affected.envStats().weight()>=1))
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
		if(E.fetchEffect("Sinking")==null)
		{
			Sinking F=new Sinking();
			F.setProficiency(proficiency());
			F.invoker=null;
			if(E instanceof MOB)
				F.invoker=(MOB)E;
			else
				F.invoker=CMClass.getMOB("StdMOB");
			E.addEffect(F);
			F.setSavable(false);
			F.makeLongLasting();
			if(!(E instanceof MOB))
				CMLib.threads().startTickDown(F,Tickable.TICKID_MOB,1);
			E.recoverEnvStats();
		}
		return true;
	}
}
