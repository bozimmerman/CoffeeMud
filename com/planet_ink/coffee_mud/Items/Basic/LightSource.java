package com.planet_ink.coffee_mud.Items.Basic;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
/* 
   Copyright 2000-2004 Bo Zimmerman

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
public class LightSource extends StdItem implements Light
{
	public String ID(){	return "LightSource";}
	protected boolean lit=false;
	protected int durationTicks=200;
	protected boolean destroyedWhenBurnedOut=true;
	protected boolean goesOutInTheRain=true;

	public LightSource()
	{
		super();
		setName("a light source");
		setDisplayText("an ordinary light source sits here doing nothing.");
		setDescription("It looks like a light source of some sort.  I`ll bet it would help you see in the dark.");

		properWornBitmap=Item.HELD;
		setMaterial(EnvResource.RESOURCE_OAK);
		wornLogicalAnd=false;
		baseGoldValue=5;
		recoverEnvStats();
	}

	public void setDuration(int duration){durationTicks=duration;}
	public int getDuration(){return durationTicks;}
	public boolean destroyedWhenBurnedOut(){return destroyedWhenBurnedOut;}
	public void setDestroyedWhenBurntOut(boolean truefalse){destroyedWhenBurnedOut=truefalse;}
	public boolean goesOutInTheRain(){return this.goesOutInTheRain;}
	public boolean isLit(){return lit;}
	public void light(boolean isLit){lit=isLit;}



	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		MOB mob=msg.source();
		if(!msg.amITarget(this))
			return super.okMessage(myHost,msg);
		else
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_HOLD:
			if(getDuration()==0)
			{
				mob.tell(name()+" looks used up.");
				return false;
			}
			Room room=mob.location();
			if(room!=null)
			{
				if(((LightSource.inTheRain(room)&&(goesOutInTheRain()))
					||(LightSource.inTheWater(room)&&(mob.riding()==null)))
				   &&(getDuration()>0)
				   &&(mob.isMine(this)))
				{
					mob.tell("It's too wet to light "+name()+" here.");
					return false;
				}
			}
			return super.okMessage(myHost,msg);
		case CMMsg.TYP_EXTINGUISH:
			if((getDuration()==0)||(!isLit()))
			{
				mob.tell(name()+" is not lit!");
				return false;
			}
			return true;
		}
		return super.okMessage(myHost,msg);
	}

	public void recoverEnvStats()
	{
		if((getDuration()>0)&&(isLit()))
			baseEnvStats().setDisposition(baseEnvStats().disposition()|EnvStats.IS_LIGHTSOURCE);
		else
		if((baseEnvStats().disposition()&EnvStats.IS_LIGHTSOURCE)==EnvStats.IS_LIGHTSOURCE)
			baseEnvStats().setDisposition(baseEnvStats().disposition()-EnvStats.IS_LIGHTSOURCE);
		super.recoverEnvStats();
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==MudHost.TICK_LIGHT_FLICKERS)
		{
			if((owner()!=null)
			&&(isLit())
			&&(getDuration()>0))
			{
				if(owner() instanceof Room)
				{
					if(((Room)owner()).numInhabitants()>0)
						((Room)owner()).showHappens(CMMsg.MSG_OK_VISUAL,name()+" flickers and burns out.");
					if(destroyedWhenBurnedOut())
						destroy();
					((Room)owner()).recoverRoomStats();
				}
				else
				if(owner() instanceof MOB)
				{
					((MOB)owner()).tell(((MOB)owner()),null,this,"<O-NAME> flickers and burns out.");
					setDuration(0);
					if(destroyedWhenBurnedOut())
						destroy();
					((MOB)owner()).recoverEnvStats();
					((MOB)owner()).recoverCharStats();
					((MOB)owner()).recoverMaxState();
					((MOB)owner()).recoverEnvStats();
					((MOB)owner()).location().recoverRoomStats();
				}
			}
			light(false);
			setDuration(0);
			setDescription("It looks all used up.");
			return false;
		}
		return super.tick(ticking,tickID);
	}

	public static boolean inTheRain(Room room)
	{
		if(room==null) return false;
		return (((room.domainType()&Room.INDOORS)==0)
				&&((room.getArea().getClimateObj().weatherType(room)==Climate.WEATHER_RAIN)
				   ||(room.getArea().getClimateObj().weatherType(room)==Climate.WEATHER_THUNDERSTORM)));
	}
	public static boolean inTheWater(Room room)
	{
		if(room==null) return false;
		return (room.domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
			   ||(room.domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE)
			   ||(room.domainType()==Room.DOMAIN_INDOORS_UNDERWATER)
			   ||(room.domainType()==Room.DOMAIN_INDOORS_WATERSURFACE);
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		MOB mob=msg.source();
		if(mob==null) return;
		Room room=mob.location();
		if(room==null) return;
		if(room!=null)
		{
			if(((LightSource.inTheRain(room)&&goesOutInTheRain())||(LightSource.inTheWater(room)&&(mob.riding()==null)))
			&&(isLit())
			&&(getDuration()>0)
			&&(mob.isMine(this))
			&&((!Sense.isInFlight(mob))
			   ||(LightSource.inTheRain(room))
			   ||((room.domainType()!=Room.DOMAIN_OUTDOORS_WATERSURFACE)&&(room.domainType()!=Room.DOMAIN_INDOORS_WATERSURFACE))))
			{
				if(LightSource.inTheWater(room))
					mob.tell("The water makes "+name()+" go out.");
				else
					mob.tell("The rain makes "+name()+" go out.");
				tick(this,MudHost.TICK_LIGHT_FLICKERS);
			}
		}

		if(msg.amITarget(this))
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_EXTINGUISH:
				if(isLit())
				{
					light(false);
					CMClass.ThreadEngine().deleteTick(this,MudHost.TICK_LIGHT_FLICKERS);
					recoverEnvStats();
					room.recoverRoomStats();
				}
				break;
			case CMMsg.TYP_HOLD:
				if(getDuration()>0)
				{
					if(!isLit())
						msg.addTrailerMsg(new FullMsg(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> light(s) up "+name()+"."));
					else
						mob.tell(name()+" is already lit.");
					light(true);
					CMClass.ThreadEngine().startTickDown(this,MudHost.TICK_LIGHT_FLICKERS,getDuration());
					recoverEnvStats();
					msg.source().recoverEnvStats();
					room.recoverRoomStats();
				}
				break;
			}
			if((msg.tool()==this)
			&&(msg.sourceMinor()==CMMsg.TYP_THROW)
			&&(msg.source()!=null))
			{
				msg.source().recoverEnvStats();
				if(!Util.bset(msg.sourceCode(),CMMsg.MASK_OPTIMIZE))
				{
					if(msg.source().location()!=null)
						msg.source().location().recoverRoomStats();
					Room R=CoffeeUtensils.roomLocation(msg.target());
					if((R!=null)&&(R!=msg.source().location()))
						R.recoverRoomStats();
				}
			}
			else
			if(msg.amITarget(this))
			{
				switch(msg.targetMinor())
				{
				case CMMsg.TYP_DROP:
				case CMMsg.TYP_GET:
				case CMMsg.TYP_REMOVE:
					if(msg.source()!=null)
					{
						if(!Util.bset(msg.targetCode(),CMMsg.MASK_OPTIMIZE))
						{
							msg.source().recoverEnvStats();
							if(msg.source().location()!=null)
								msg.source().location().recoverRoomStats();
							Room R=CoffeeUtensils.roomLocation(msg.tool());
							if((R!=null)&&(R!=msg.source().location()))
								R.recoverRoomStats();
						}
					}
					break;
				}
			}
		}
}
