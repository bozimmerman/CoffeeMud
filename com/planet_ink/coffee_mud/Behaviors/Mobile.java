package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.Exits.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.Abilities.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import java.util.*;

public class Mobile extends StdBehavior
{
	private int tickDown=0;
	
	public Mobile()
	{
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		tickReset();
	}
	
	private void tickReset()
	{
		tickDown=(int)Math.round(Math.random()*20)+10;
	}
	
	public Behavior newInstance()
	{ 
		return new Mobile();
	}
	
	
	public void tick(Environmental ticking, int tickID)
	{
		if(tickID!=ServiceEngine.MOB_TICK) return;
		if(((--tickDown)<1)&&(ticking instanceof MOB))
		{
			tickReset();
			if(!canBehave(ticking)) return;
			MOB mob=(MOB)ticking;
			Room thisRoom=mob.location();
			
			int tries=0;
			int direction=-1;
			while((tries++<10)&&(direction<0))
			{
				direction=(int)Math.round(Math.floor(Math.random()*6));
				Room otherRoom=thisRoom.getRoom(direction);
				Exit otherExit=thisRoom.getExit(direction);
				if((otherRoom!=null)&&(otherExit!=null))
				{
					Exit opExit=otherRoom.getExit(Directions.getOpDirectionCode(direction));
					for(int a=0;a<otherExit.numAffects();a++)
						if(otherExit.fetchAffect(a) instanceof Trap)
							direction=-1;
					
					if(opExit!=null)
					{
						for(int a=0;a<opExit.numAffects();a++)
							if(opExit.fetchAffect(a) instanceof Trap)
								direction=-1;
					}
					
					if(!otherRoom.getAreaID().equals(thisRoom.getAreaID()))
						direction=-1;
					else
						break;
				}
				else
					direction=-1;
			}
			
			if(direction<0)
				return;
			
			boolean move=true;
			for(int m=0;m<thisRoom.numInhabitants();m++)
				if(thisRoom.fetchInhabitant(m).isASysOp())
					move=false;
			if(move)
			{
				Movement.move(mob,direction,false);
				if(mob.location()==thisRoom)
					tickDown=0;
			}
		}
		return;
	}
}
