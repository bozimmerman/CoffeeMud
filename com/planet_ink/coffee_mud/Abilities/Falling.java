package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.MOBS.*;
import com.planet_ink.coffee_mud.Items.*;
import com.planet_ink.coffee_mud.Locales.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.CharClasses.*;
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

		if(tickID!=ServiceEngine.MOB_TICK)
			return true;

		if(affected==null)
			return false;

		if(affected instanceof MOB)
		{
			MOB mob=(MOB)affected;
			if(mob==null) return false;
			if(mob.location()==null) return false;

			if((mob.location().getRoom(Directions.DOWN)==null)
			||(mob.location().getExit(Directions.DOWN)==null)
			||(!mob.location().getExit(Directions.DOWN).isOpen()))
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
					TheFight.doDamage(mob,Dice.roll(1,10,0));
				}
				temporarilyDisable=true;
				Movement.move(mob,Directions.DOWN,false);
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
				Room nextRoom=room.getRoom(Directions.DOWN);
				if((nextRoom!=null)
				&&(room.getExit(Directions.DOWN)!=null)
				&&(room.getExit(Directions.DOWN).isOpen()))
				{
					room.show(invoker,null,Affect.VISUAL_WNOISE,item.name()+" falls.");
					Vector V=new Vector();
					recursiveRoomItems(V,item,room);
					for(int v=0;v<V.size();v++)
					{
						Item thisItem=(Item)V.elementAt(v);
						room.delItem(thisItem);
						nextRoom.addItem(thisItem);
					}
					room=nextRoom;
					nextRoom.show(invoker,null,Affect.VISUAL_WNOISE,item.name()+" falls in from above.");
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


	public static void startFalling(Environmental E, Room location)
	{
		if(E==null) return;
		if((E instanceof Item)&&(location==null)) return;

		if(E.fetchAffect(new Falling().ID())==null)
		{
			Falling F=new Falling();
			F.invoker=null;
			F.room=location;
			if(E instanceof MOB)
				F.invoker=(MOB)E;
			else
				F.invoker=new StdMOB();
			E.addAffect(F);
			if(!(E instanceof MOB))
				ServiceEngine.startTickDown(F,ServiceEngine.MOB_TICK,1);
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
			if(affect.sourceType()==Affect.SOUND)
				return true;

			if(Sense.isFlying(affect.source()))
			{
				unInvoke();
				return true;
			}
			if(affect.targetCode()==Affect.VISUAL_LOOK)
			{
				return true;
			}

			switch(affect.sourceType())
			{
			case Affect.AIR:
			case Affect.MOVE:
			case Affect.HANDS:
			case Affect.VISUAL:
			case Affect.SOUND:
			case Affect.STRIKE:
			case Affect.TASTE:
				affect.source().tell("You are too busy falling to the ground to do that right now.");
				return false;
			}
		}
		return true;
	}
}
