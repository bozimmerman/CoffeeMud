package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
public class LightSource extends StdItem implements Light
{
	public String ID(){	return "LightSource";}
	protected boolean lit=false;
	protected int durationTicks=150;
	protected boolean destroyedWhenBurnedOut=true;
	protected boolean goesOutInTheRain=true;

	public LightSource()
	{
		super();
		name="a light source";
		displayText="an ordinary light source sits here doing nothing.";
		description="It looks like a light source of some sort.  I'll bet it would help you see in the dark.";

		properWornBitmap=Item.HELD;
		setMaterial(EnvResource.RESOURCE_OAK);
		wornLogicalAnd=false;
		baseGoldValue=5;
		recoverEnvStats();
	}

	public void setDuration(int duration){durationTicks=duration;}
	public int getDuration(){return durationTicks;}
	public boolean destroyedWhenBurnedOut(){return this.destroyedWhenBurnedOut;}
	public boolean goesOutInTheRain(){return this.goesOutInTheRain;}
	public boolean isLit(){return lit;}
	public void light(boolean isLit){lit=isLit;}

	public Environmental newInstance()
	{
		return new LightSource();
	}

	public static int isAnOkAffect(Light myLight, Affect affect)
	{
		MOB mob=affect.source();

		if(!affect.amITarget(myLight))
			return 1;
		else
		switch(affect.targetMinor())
		{
		case Affect.TYP_HOLD:
			if(myLight.getDuration()==0)
			{
				mob.tell(myLight.name()+" looks used up.");
				return 0;
			}
			Room room=mob.location();
			if(room!=null)
			{
				if(((LightSource.inTheRain(room)&&(myLight.goesOutInTheRain()))
					||(LightSource.inTheWater(room)&&(mob.riding()==null)))
				   &&(myLight.getDuration()>0)
				   &&(mob.isMine(myLight)))
				{
					mob.tell("It's too wet to light "+myLight.name()+" here.");
					return 0;
				}
			}
			return 1;
		case Affect.TYP_EXTINGUISH:
			if((myLight.getDuration()==0)||(!myLight.isLit()))
			{
				mob.tell(myLight.name()+" is not lit!");
				return 0;
			}
			return -1;
		}
		return 1;
	}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		switch(LightSource.isAnOkAffect(this,affect))
		{
		case 0: return false;
		case 1: return super.okAffect(myHost,affect);
		default: return true;
		}
	}

	public static void recoverMyEnvStats(Light myLight)
	{
		if((myLight.getDuration()>0)&&(myLight.isLit()))
			myLight.baseEnvStats().setDisposition(myLight.baseEnvStats().disposition()|EnvStats.IS_LIGHTSOURCE);
		else
		if((myLight.baseEnvStats().disposition()&EnvStats.IS_LIGHTSOURCE)==EnvStats.IS_LIGHTSOURCE)
			myLight.baseEnvStats().setDisposition(myLight.baseEnvStats().disposition()-EnvStats.IS_LIGHTSOURCE);
	}
	public void recoverEnvStats()
	{
		recoverMyEnvStats(this);
		super.recoverEnvStats();
	}

	public static void doAffect(Light myLight,
								Affect affect)
	{
	}

	public static boolean pleaseTickLightly(Light myLight,
											int tickID)
	{
		if(tickID==Host.LIGHT_FLICKERS)
		{
			if((myLight.owner()!=null)
			&&(myLight.isLit())
			&&(myLight.getDuration()>0))
			{
				if(myLight.owner() instanceof Room)
				{
					if(((Room)myLight.owner()).numInhabitants()>0)
						((Room)myLight.owner()).showHappens(Affect.MSG_OK_VISUAL,myLight.name()+" flickers and burns out.");
					if(myLight.destroyedWhenBurnedOut())
						myLight.destroy();
					((Room)myLight.owner()).recoverRoomStats();
				}
				else
				if(myLight.owner() instanceof MOB)
				{
					((MOB)myLight.owner()).tell(((MOB)myLight.owner()),null,myLight,"<O-NAME> flickers and burns out.");
					myLight.setDuration(0);
					if(myLight.destroyedWhenBurnedOut())
						myLight.destroy();
					((MOB)myLight.owner()).recoverEnvStats();
					((MOB)myLight.owner()).recoverCharStats();
					((MOB)myLight.owner()).recoverMaxState();
					((MOB)myLight.owner()).recoverEnvStats();
					((MOB)myLight.owner()).location().recoverRoomStats();
				}
			}
			myLight.light(false);
			myLight.setDuration(0);
			myLight.setDescription("It looks all used up.");
		}
		return false;
	}
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!LightSource.pleaseTickLightly(this,tickID))
			return false;
		return super.tick(ticking,tickID);
	}

	public static boolean inTheRain(Room room)
	{
		if(room==null) return false;
		return (((room.domainType()&Room.INDOORS)==0)
				&&((room.getArea().weatherType(room)==Area.WEATHER_RAIN)
				   ||(room.getArea().weatherType(room)==Area.WEATHER_THUNDERSTORM)));
	}
	public static boolean inTheWater(Room room)
	{
		if(room==null) return false;
		return (room.domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
			   ||(room.domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE)
			   ||(room.domainType()==Room.DOMAIN_INDOORS_UNDERWATER)
			   ||(room.domainType()==Room.DOMAIN_INDOORS_WATERSURFACE);
	}

	public static void lightAffect(Light myLight, Affect affect)
	{
		MOB mob=affect.source();
		if(mob==null) return;
		Room room=mob.location();
		if(room==null) return;
		if(room!=null)
		{
			if(((LightSource.inTheRain(room)&&myLight.goesOutInTheRain())||(LightSource.inTheWater(room)&&(mob.riding()==null)))
			&&(myLight.isLit())
			&&(myLight.getDuration()>0)
			&&(mob.isMine(myLight))
			&&((!Sense.isInFlight(mob))
			   ||(LightSource.inTheRain(room))
			   ||((room.domainType()!=Room.DOMAIN_OUTDOORS_WATERSURFACE)&&(room.domainType()!=Room.DOMAIN_INDOORS_WATERSURFACE))))
			{
				if(LightSource.inTheWater(room))
					mob.tell("The water makes "+myLight.name()+" go out.");
				else
					mob.tell("The rain makes "+myLight.name()+" go out.");
				myLight.tick(myLight,Host.LIGHT_FLICKERS);
			}
		}

		if(affect.amITarget(myLight))
			switch(affect.targetMinor())
			{
			case Affect.TYP_EXTINGUISH:
				if(myLight.isLit())
				{
					myLight.light(false);
					ExternalPlay.deleteTick(myLight,Host.LIGHT_FLICKERS);
					myLight.recoverEnvStats();
					room.recoverRoomStats();
				}
				break;
			case Affect.TYP_HOLD:
				if(myLight.getDuration()>0)
				{
					if(!myLight.isLit())
						affect.addTrailerMsg(new FullMsg(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> light(s) up "+myLight.name()+"."));
					else
						mob.tell(myLight.name()+" is already lit.");
					myLight.light(true);
					ExternalPlay.startTickDown(myLight,Host.LIGHT_FLICKERS,myLight.getDuration());
					myLight.recoverEnvStats();
					room.recoverRoomStats();
				}
				break;
			}
	}

	public void affect(Environmental myHost, Affect affect)
	{
		LightSource.lightAffect(this,affect);
		super.affect(myHost,affect);
		if(affect.amITarget(this))
		{
			switch(affect.targetMinor())
			{
			case Affect.TYP_DROP:
			case Affect.TYP_THROW:
			case Affect.TYP_GET:
				if(affect.source()!=null)
				{
					affect.source().recoverEnvStats();
					if(affect.source().location()!=null)
						affect.source().location().recoverRoomStats();
					if((affect.tool()!=null)
					&&(affect.tool()!=affect.source().location())
					&&(affect.tool() instanceof Room))
						((Room)affect.tool()).recoverRoomStats();
				}
				break;
			}
		}
	}

}
