package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

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
	public Environmental newInstance(){	return new Falling();}
	
	private boolean reversed(){return profficiency()==100;}
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;

		if(tickID!=Host.MOB_TICK)
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
			if((mob.location().getRoomInDir(direction)==null)
			||(mob.location().getExitInDir(direction)==null)
			||(!mob.location().getExitInDir(direction).isOpen()))
			{
				if(reversed()) 
					return true;
				unInvoke();
				mob.location().show(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> hit(s) the ground.");
				ExternalPlay.postDamage(mob,mob,this,damageToTake,Affect.NO_EFFECT,-1,null);
				return false;
			}
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
				ExternalPlay.move(mob,direction,false,false);
				temporarilyDisable=false;
				if((mob.location().getRoomInDir(direction)==null)
				||(mob.location().getExitInDir(direction)==null)
				||(!mob.location().getExitInDir(direction).isOpen()))
				{
					if(reversed()) 
						return true;
					unInvoke();
					mob.location().show(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> hit(s) the ground.");
					ExternalPlay.postDamage(mob,mob,this,damageToTake,Affect.NO_EFFECT,-1,null);
					return false;
				}
				else
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
			||(!item.isGettable())
			||(Sense.isFlying(item.ultimateContainer())))
			{
				unInvoke();
				return false;
			}
			else
			{
				Room nextRoom=room.getRoomInDir(direction);
				if((nextRoom!=null)
				&&(room.getExitInDir(direction)!=null)
				&&(room.getExitInDir(direction).isOpen()))
				{
					room.show(invoker,null,Affect.MSG_OK_ACTION,item.name()+" falls "+addStr+".");
					Vector V=new Vector();
					recursiveRoomItems(V,item,room);
					for(int v=0;v<V.size();v++)
					{
						Item thisItem=(Item)V.elementAt(v);
						room.delItem(thisItem);
						nextRoom.addItemRefuse(thisItem,Item.REFUSE_PLAYER_DROP);
					}
					room=nextRoom;
					nextRoom.show(invoker,null,Affect.MSG_OK_ACTION,item.name()+" falls in from "+(reversed()?"below":"above")+".");
					return true;
				}
				else
				{
					if(reversed())
						return true;
					unInvoke();
					return false;
				}
			}

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


	public boolean okAffect(Affect affect)
	{
		if(!super.okAffect(affect))
			return false;

		if(temporarilyDisable)
			return true;
		MOB mob=affect.source();
		if((affected!=null)&&(affected instanceof MOB))
			if(affect.amISource((MOB)affected))
			{
				if(Sense.isInFlight(mob))
				{
					damageToTake=0;
					unInvoke();
					return true;
				}
				if(Util.bset(affect.targetMajor(),Affect.MASK_MOVE))
				{
					affect.source().tell("You are too busy falling to do that right now.");
					return false;
				}
			}
		return true;
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
	public boolean invoke(MOB mob, Vector commands, Environmental target, boolean auto)
	{
		if(!auto) return false;
		Environmental E=target;
		if(E==null) return false;
		if((E instanceof Item)&&(room==null)) return false;
		if(E.fetchAffect("Falling")==null)
		{
			Falling F=new Falling();
			F.setProfficiency(profficiency());
			F.invoker=null;
			if(E instanceof MOB)
				F.invoker=(MOB)E;
			else
				F.invoker=CMClass.getMOB("StdMOB");
			E.addAffect(F);
			if(!(E instanceof MOB))
				ExternalPlay.startTickDown(F,Host.MOB_TICK,1);
			E.recoverEnvStats();
			
		}
		return true;
	}
}
