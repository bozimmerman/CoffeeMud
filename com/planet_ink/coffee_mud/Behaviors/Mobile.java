package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Mobile extends ActiveTicker
{
	public Mobile()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		minTicks=10; maxTicks=30; chance=100;
		tickReset();
		mobileType=true;
		canImproveCode=Behavior.CAN_MOBS;
	}
	public Behavior newInstance()
	{
		return new Mobile();
	}

	public void tick(Environmental ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if((canAct(ticking,tickID))&&(ticking instanceof MOB))
		{
			// ridden things dont wander!
			if(ticking instanceof Rideable)
				if(((Rideable)ticking).numRiders()>0)
					return;
			
			MOB mob=(MOB)ticking;
			Room thisRoom=mob.location();
			if(thisRoom instanceof GridLocale)
			{
				Vector V=((GridLocale)thisRoom).getAllRooms();
				Room R=(Room)(V.elementAt(Dice.roll(1,V.size(),-1)));
				if(R!=null) R.bringMobHere(mob,true);
				thisRoom=mob.location();
			}

			int tries=0;
			int direction=-1;
			while((tries++<10)&&(direction<0))
			{
				direction=(int)Math.round(Math.floor(Math.random()*6));
				Room otherRoom=thisRoom.getRoomInDir(direction);
				Exit otherExit=thisRoom.getExitInDir(direction);
				if((otherRoom!=null)&&(otherExit!=null))
				{
					Exit opExit=otherRoom.getExitInDir(Directions.getOpDirectionCode(direction));
					for(int a=0;a<otherExit.numAffects();a++)
					{
						Ability aff=otherExit.fetchAffect(a);
						if((aff!=null)&&(aff instanceof Trap))
							direction=-1;
					}

					if(opExit!=null)
					{
						for(int a=0;a<opExit.numAffects();a++)
						{
							Ability aff=opExit.fetchAffect(a);
							if((aff!=null)&&(aff instanceof Trap))
								direction=-1;
						}
					}

					if((!otherRoom.getArea().name().equals(thisRoom.getArea().name()))
					   &&(this.getParms().toUpperCase().indexOf("WANDER")<0))
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
			{
				MOB inhab=thisRoom.fetchInhabitant(m);
				if((inhab!=null)&&(inhab.isASysOp(thisRoom)))
					move=false;
			}
			if(move)
			{
				Ability A=mob.fetchAbility("Thief_Sneak");
				if((A!=null)&&(Dice.rollPercentage()<50))
				{
					Vector V=new Vector();
					V.add(Directions.getDirectionName(direction));
					if(A.profficiency()<100)
						A.setProfficiency(Dice.roll(1,50,(mob.baseEnvStats().level()-A.qualifyingLevel(mob))*15));
					A.invoke(mob,V,null,false);
				}
				else
					ExternalPlay.move(mob,direction,false);

				if(mob.location()==thisRoom)
					tickDown=0;
			}
		}
		return;
	}
}
