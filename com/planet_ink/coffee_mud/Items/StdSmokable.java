package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
public class StdSmokable extends StdContainer implements Light
{
	public String ID(){	return "StdSmokable";}
	protected boolean lit=false;
	protected long puffTicks=30000/MudHost.TICK_TIME;
	protected int baseDuration=200;
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

	public void setDuration(int duration){baseDuration=duration;}
	public int getDuration(){return baseDuration;}
	public boolean destroyedWhenBurnedOut(){return this.destroyedWhenBurnedOut;}
	public boolean goesOutInTheRain(){return this.goesOutInTheRain;}
	public boolean isLit(){return lit;}
	public void light(boolean isLit){lit=isLit;}

	public Environmental newInstance()
	{
		return new StdSmokable();
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		MOB mob=msg.source();

		if(!msg.amITarget(this))
			return super.okMessage(myHost,msg);
		else
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_WEAR:
			if(capacity>0)
			{
				if(getContents().size()>0)
					durationTicks=baseDuration;
				else
					durationTicks=0;
			}
			if(durationTicks==0)
			{
				mob.tell(name()+" looks empty.");
				return false;
			}
			Room room=mob.location();
			if(room!=null)
			{
				if(((LightSource.inTheRain(room)&&(goesOutInTheRain()))
					||(LightSource.inTheWater(room)&&(mob.riding()==null)))
				   &&(durationTicks>0)
				   &&(mob.isMine(this)))
				{
					mob.tell("It's too wet to light "+name()+" here.");
					return false;
				}
			}
			msg.modify(msg.source(),msg.target(),msg.tool(),
						  msg.sourceCode(),"<S-NAME> light(s) up <T-NAME>.",
						  msg.targetCode(),"<S-NAME> light(s) up <T-NAME>.",
						  msg.othersCode(),"<S-NAME> light(s) up <T-NAME>.");
			return super.okMessage(myHost,msg);
		case CMMsg.TYP_EXTINGUISH:
			if((durationTicks==0)||(!isLit()))
			{
				mob.tell(name()+" is not lit!");
				return false;
			}
			return true;
		}
		return super.okMessage(myHost,msg);
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((tickID==MudHost.TICK_LIGHT_FLICKERS)
		&&(isLit())
		&&(owner()!=null))
		{
			if(((--durationTicks)>0)&&(!destroyed))
			{
				if(((durationTicks%puffTicks)==0)
				&&(owner() instanceof MOB)
				&&(!amWearingAt(Item.INVENTORY)))
				{
					MOB mob=(MOB)owner();
					if((mob.location()!=null)
					&&(Sense.aliveAwakeMobile(mob,true)))
					{
						mob.location().show(mob,this,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> puff(s) on <T-NAME>.");
					}
				}
				return true;
			}
			else
			{
				if(owner() instanceof Room)
				{
					if(((Room)owner()).numInhabitants()>0)
						((Room)owner()).showHappens(CMMsg.MSG_OK_VISUAL,name()+" burns out.");
					if(destroyedWhenBurnedOut())
						destroy();
					((Room)owner()).recoverRoomStats();
				}
				else
				if(owner() instanceof MOB)
				{
					((MOB)owner()).tell(((MOB)owner()),null,this,"<O-NAME> burns out.");
					durationTicks=0;
					if(destroyedWhenBurnedOut())
						destroy();
					((MOB)owner()).recoverEnvStats();
					((MOB)owner()).recoverCharStats();
					((MOB)owner()).recoverMaxState();
					((MOB)owner()).recoverEnvStats();
					((MOB)owner()).location().recoverRoomStats();
				}
				light(false);
				durationTicks=0;
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

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		MOB mob=msg.source();
		if(mob==null) return;
		Room room=mob.location();
		if(room==null) return;
		if(room!=null)
		{
			if(((LightSource.inTheRain(room)&&goesOutInTheRain())||(LightSource.inTheWater(room)&&(mob.riding()==null)))
			&&(isLit())
			&&(durationTicks>0)
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
			case CMMsg.TYP_WEAR:
				if(durationTicks>0)
				{
					if(capacity>0)
					{
						Vector V=getContents();
						for(int v=0;v<V.size();v++)
							((Item)V.elementAt(v)).destroy();
					}

					light(true);
					CMClass.ThreadEngine().startTickDown(this,MudHost.TICK_LIGHT_FLICKERS,1);
					recoverEnvStats();
					room.recoverRoomStats();
				}
				break;
			}
		super.executeMsg(myHost,msg);
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_DROP:
			case CMMsg.TYP_THROW:
			case CMMsg.TYP_GET:
			case CMMsg.TYP_REMOVE:
				if(msg.source()!=null)
				{
					if(!Util.bset(msg.targetCode(),CMMsg.MASK_OPTIMIZE))
					{
						msg.source().recoverEnvStats();
						if(msg.source().location()!=null)
							msg.source().location().recoverRoomStats();
						if((msg.tool()!=null)
						&&(msg.tool()!=msg.source().location())
						&&(msg.tool() instanceof Room))
							((Room)msg.tool()).recoverRoomStats();
					}
				}
				break;
			}
		}
	}

}
