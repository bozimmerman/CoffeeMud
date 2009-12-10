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
	protected int fallTickDown=1;

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

    protected boolean isAirRoom(Room R)
	{
		if(R==null) return false;
		if((R.domainType()==Room.DOMAIN_INDOORS_AIR)
		||(R.domainType()==Room.DOMAIN_OUTDOORS_AIR))
			return true;
		return false;
	}

    protected boolean canFallFrom(Room fromHere, int direction)
	{
		if((fromHere==null)||(direction<0)||(direction>=Directions.NUM_DIRECTIONS()))
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

    protected boolean stopFalling(MOB mob)
	{
		if(reversed()) return true;
		unInvoke();
		Room R=mob.location();
		if(R!=null)
		{
			if(isAirRoom(R))
				R.show(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> stop(s) falling."+CMProps.msp("splat.wav",50));
			else
			if(isWaterSurface(R)||isUnderWater(R))
				R.show(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> hit(s) the water."+CMProps.msp("splat.wav",50));
			else
				R.show(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> hit(s) the ground."+CMProps.msp("splat.wav",50));
			CMLib.combat().postDamage(mob,mob,this,damageToTake,CMMsg.MASK_ALWAYS|CMMsg.TYP_JUSTICE,-1,null);
		}
		mob.delEffect(this);
		return false;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;

		if(tickID!=Tickable.TICKID_MOB)
			return true;

		if(affected==null)
			return false;
		if(--fallTickDown>0)
			return true;
		fallTickDown=1;

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

			if(CMLib.flags().isInFlight(mob))
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
						damageToTake+=CMLib.dice().roll(1,(int)Math.round(CMath.mul(CMath.mul(mob.maxState().getHitPoints(),0.1),CMath.div(mob.baseWeight(),150.0))),0);
				}
				temporarilyDisable=true;
				CMLib.tracking().move(mob,direction,false,false);
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
			||(!CMLib.flags().isGettable(item))
            ||(item.container()!=null)
			||(CMLib.flags().isInFlight(item.ultimateContainer()))
			||(room.getRoomInDir(direction)==null))
			{
				unInvoke();
				return false;
			}
			if(room.numItems()>100)
			{
				fallTickDown=CMLib.dice().roll(1,room.numItems()/50,0);
				if((--fallTickDown)>0)
					return true;
			}
			Room nextRoom=room.getRoomInDir(direction);
			if(canFallFrom(room,direction))
			{
				room.show(invoker,null,item,CMMsg.MSG_OK_ACTION,"<O-NAME> falls "+addStr+".");
                nextRoom.bringItemHere(item,CMProps.getIntVar(CMProps.SYSTEMI_EXPIRE_PLAYER_DROP),false);
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
				if(CMLib.flags().isInFlight(mob))
				{
					damageToTake=0;
					unInvoke();
					return true;
				}
				if(CMath.bset(msg.targetMajor(),CMMsg.MASK_MOVE))
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
			&&(CMath.bset(((Ability)msg.tool()).flags(),Ability.FLAG_TRANSPORTING)))
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
			F.setProficiency(proficiency());
			F.invoker=null;
			if(E instanceof MOB)
				F.invoker=(MOB)E;
			else
				F.invoker=CMClass.getMOB("StdMOB");
			F.setSavable(false);
			F.makeLongLasting();
			E.addEffect(F);
			if(!(E instanceof MOB))
				CMLib.threads().startTickDown(F,Tickable.TICKID_MOB,1);
			E.recoverEnvStats();

		}
		return true;
	}
}
