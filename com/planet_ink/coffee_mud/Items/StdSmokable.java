package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
public class StdSmokable extends StdContainer implements Light
{
	public String ID(){	return "StdSmokable";}
	protected boolean lit=false;
	protected long puffTicks=30000/Host.TICK_TIME;
	protected int durationTicks=200;
	protected boolean destroyedWhenBurnedOut=true;
	protected boolean goesOutInTheRain=true;

	public StdSmokable()
	{
		super();
		setName("a cigar");
		setDisplayText("a cigar has been left here.");
		setDescription("Woven of fine leaf, it looks like a fine smoke!");

		capacity=0;
		containType=Container.CONTAIN_SMOKEABLES;
		properWornBitmap=Item.ON_MOUTH;
		setMaterial(EnvResource.RESOURCE_PIPEWEED);
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
		return new StdSmokable();
	}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		MOB mob=affect.source();

		if(!affect.amITarget(this))
			return super.okAffect(myHost,affect);
		else
		switch(affect.targetMinor())
		{
		case Affect.TYP_WEAR:
			if(capacity>0)
			{
				if(getContents().size()>0)
					durationTicks=200;
				else
					durationTicks=0;
			}
			if(getDuration()==0)
			{
				mob.tell(name()+" looks empty.");
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
			affect.modify(affect.source(),affect.target(),affect.tool(),
						  affect.sourceCode(),"<S-NAME> light(s) up <T-NAME>.",
						  affect.targetCode(),"<S-NAME> light(s) up <T-NAME>.",
						  affect.othersCode(),"<S-NAME> light(s) up <T-NAME>.");
			return super.okAffect(myHost,affect);
		case Affect.TYP_EXTINGUISH:
			if((getDuration()==0)||(!isLit()))
			{
				mob.tell(name()+" is not lit!");
				return false;
			}
			return true;
		}
		return super.okAffect(myHost,affect);
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((tickID==Host.LIGHT_FLICKERS)
		&&(isLit())
		&&(owner()!=null))
		{
			if((--durationTicks)>0)
			{
				if(((durationTicks%puffTicks)==0)
				&&(owner() instanceof MOB))
				{
					MOB mob=(MOB)owner();
					if((mob.location()!=null)
					&&(Sense.aliveAwakeMobile(mob,true)))
					{
						mob.location().show(mob,this,this,Affect.MSG_QUIETMOVEMENT,"<S-NAME> puff(s) on <T-NAME>.");
					}
				}
				return true;
			}
			else
			{
				if(owner() instanceof Room)
				{
					if(((Room)owner()).numInhabitants()>0)
						((Room)owner()).showHappens(Affect.MSG_OK_VISUAL,name()+" burns out.");
					if(destroyedWhenBurnedOut())
						destroy();
					((Room)owner()).recoverRoomStats();
				}
				else
				if(owner() instanceof MOB)
				{
					((MOB)owner()).tell(((MOB)owner()),null,this,"<O-NAME> burns out.");
					setDuration(0);
					if(destroyedWhenBurnedOut())
						destroy();
					((MOB)owner()).recoverEnvStats();
					((MOB)owner()).recoverCharStats();
					((MOB)owner()).recoverMaxState();
					((MOB)owner()).recoverEnvStats();
					((MOB)owner()).location().recoverRoomStats();
				}
				light(false);
				setDuration(0);
				setDescription("It looks all used up.");
			}
		}
		return false;
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

	public void affect(Environmental myHost, Affect affect)
	{
		MOB mob=affect.source();
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
				tick(this,Host.LIGHT_FLICKERS);
			}
		}

		if(affect.amITarget(this))
			switch(affect.targetMinor())
			{
			case Affect.TYP_EXTINGUISH:
				if(isLit())
				{
					light(false);
					ExternalPlay.deleteTick(this,Host.LIGHT_FLICKERS);
					recoverEnvStats();
					room.recoverRoomStats();
				}
				break;
			case Affect.TYP_WEAR:
				if(getDuration()>0)
				{
					if(capacity>0)
					{
						Vector V=getContents();
						for(int v=0;v<V.size();v++)
							((Item)V.elementAt(v)).destroy();
					}
					
					light(true);
					ExternalPlay.startTickDown(this,Host.LIGHT_FLICKERS,1);
					recoverEnvStats();
					room.recoverRoomStats();
				}
				break;
			}
		super.affect(myHost,affect);
		if(affect.amITarget(this))
		{
			switch(affect.targetMinor())
			{
			case Affect.TYP_DROP:
			case Affect.TYP_THROW:
			case Affect.TYP_GET:
			case Affect.TYP_REMOVE:
				if(affect.source()!=null)
				{
					if(!Util.bset(affect.targetCode(),Affect.MASK_OPTIMIZE))
					{
						affect.source().recoverEnvStats();
						if(affect.source().location()!=null)
							affect.source().location().recoverRoomStats();
						if((affect.tool()!=null)
						&&(affect.tool()!=affect.source().location())
						&&(affect.tool() instanceof Room))
							((Room)affect.tool()).recoverRoomStats();
					}
				}
				break;
			}
		}
	}

}
