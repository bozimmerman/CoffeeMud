package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Falling extends StdAbility
{
	boolean temporarilyDisable=false;
	public Room room=null;

	public Falling()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Falling";
		displayText="(Falling)";
		miscText="";

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(1);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Falling();
	}

	public boolean tick(int tickID)
	{
		if(!super.tick(tickID))
			return false;

		if(tickID!=Host.MOB_TICK)
			return true;

		if(affected==null)
			return false;

		if(affected instanceof MOB)
		{
			MOB mob=(MOB)affected;
			if(mob==null) return false;
			if(mob.location()==null) return false;

			if((mob.location().doors()[Directions.DOWN]==null)
			||(mob.location().exits()[Directions.DOWN]==null)
			||(!mob.location().exits()[Directions.DOWN].isOpen()))
			{
				unInvoke();
				return false;
			}
			else
			if(Sense.isFlying(mob))
			{
				unInvoke();
				return false;
			}
			else
			{
				if(mob.envStats().weight()<1)
				{
					mob.tell("\n\r\n\rYou are floating gently down.\n\r\n\r");
				}
				else
				{
					mob.tell("\n\r\n\rYOU ARE FALLING!!\n\r\n\r");
					ExternalPlay.postDamage(mob,mob,this,Dice.roll(1,6,0));
				}
				temporarilyDisable=true;
				ExternalPlay.move(mob,Directions.DOWN,false);
				temporarilyDisable=false;
				return true;
			}
		}
		else
		if(affected instanceof Item)
		{
			Item item=(Item)affected;
			if((room==null)||((room!=null)&&(!room.isContent(item))))
			{
				unInvoke();
				return false;
			}
			else
			{
				Room nextRoom=room.doors()[Directions.DOWN];
				if((nextRoom!=null)
				&&(room.exits()[Directions.DOWN]!=null)
				&&(room.exits()[Directions.DOWN].isOpen()))
				{
					room.show(invoker,null,Affect.MSG_OK_ACTION,item.name()+" falls.");
					Vector V=new Vector();
					recursiveRoomItems(V,item,room);
					for(int v=0;v<V.size();v++)
					{
						Item thisItem=(Item)V.elementAt(v);
						room.delItem(thisItem);
						nextRoom.addItem(thisItem);
					}
					room=nextRoom;
					nextRoom.show(invoker,null,Affect.MSG_OK_ACTION,item.name()+" falls in from above.");
					return true;
				}
				else
				{
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
			if(newItem.location()==item)
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

		if(affect.amISource((MOB)affected))
		{
			if(Sense.isFlying(mob))
			{
				unInvoke();
				return true;
			}
			if(Util.bset(affect.targetMajor(),Affect.ACT_MOVE))
			{
				affect.source().tell("You are too busy falling to the ground to do that right now.");
				return false;
			}
		}
		return true;
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
			F.invoker=null;
			if(E instanceof MOB)
				F.invoker=(MOB)E;
			else
				F.invoker=CMClass.getMOB("StdMOB");
			E.addAffect(F);
			if(!(E instanceof MOB))
				ExternalPlay.startTickDown(F,Host.MOB_TICK,1);
		}
		return true;
	}
}
