package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
public class LightSource extends StdItem implements Light
{
	protected boolean lit=false;
	public boolean burnedOut=false;
	protected int durationTicks=50;
	protected boolean destroyedWhenBurnedOut=true;

	protected MOB invoker=null;

	public LightSource()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a light source";
		displayText="an ordinary light source sits here doing nothing.";
		description="It looks like a light source of some sort.  I'll bet it would help you see in the dark.";

		properWornBitmap=Item.HELD;
		material=Item.WOODEN;
		wornLogicalAnd=false;
		baseGoldValue=5;
		recoverEnvStats();
	}

	public void setDuration(int duration){durationTicks=duration;}
	public int getDuration(){return durationTicks;}

	public Environmental newInstance()
	{
		return new LightSource();
	}

	public boolean okAffect(Affect affect)
	{
		if(!super.okAffect(affect))
			return false;

		MOB mob=affect.source();

		if(!affect.amITarget(this))
			return true;
		else
		switch(affect.targetMinor())
		{
		case Affect.TYP_HOLD:
			if(burnedOut)
			{
				mob.tell(name()+" looks used up.");
				return false;
			}
			if((mob.location()!=null)
			   &&(mob.location().domainConditions()==Room.CONDITION_WET)
			   &&(!this.burnedOut)
			   &&(mob.isMine(this)))
			{
				mob.tell("It's too wet to light "+name()+" here.");
				return false;
			}
			return true;
		}
		return true;
	}

	public void affect(Affect affect)
	{
		MOB mob=affect.source();
		Room room=null;
		if(mob.location()!=null)
		{
			room=mob.location();
			if(((mob.location().domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
			  ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE))
			&&(this.lit)
			&&(!this.burnedOut)
			&&((mob.isMine(this))&&((!Sense.isFlying(mob))||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER))))
			{
				mob.tell("The water makes "+name()+" go out.");
				tick(Host.LIGHT_FLICKERS);
			}
		}
		if((myOwner()!=null)&&(myOwner() instanceof Room))
			room=(Room)myOwner();

		if(affect.amITarget(this))
			switch(affect.targetMinor())
			{
			case Affect.TYP_HOLD:
				if((!burnedOut)&&(mob.location()!=null))
				{
					affect.addTrailerMsg(new FullMsg(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> light(s) up "+name()+"."));
					invoker=mob;
					lit=true;
					ExternalPlay.startTickDown(this,Host.LIGHT_FLICKERS,durationTicks);
					this.recoverEnvStats();
					mob.location().recoverRoomStats();
				}
				break;
			}
		super.affect(affect);
		if(affect.amITarget(this))
		{
			switch(affect.targetMinor())
			{
			case Affect.TYP_DROP:
			case Affect.TYP_GET:
				if(room!=null) room.recoverRoomStats();
				else if(mob!=null) mob.recoverEnvStats();
				break;
			}
		}
	}

	public void recoverEnvStats()
	{
		if((!burnedOut)&&(lit))
			baseEnvStats().setDisposition(baseEnvStats().disposition()|Sense.IS_LIGHT);
		else
		if((baseEnvStats().disposition()&Sense.IS_LIGHT)==Sense.IS_LIGHT)
			baseEnvStats().setDisposition(baseEnvStats().disposition()-Sense.IS_LIGHT);
		super.recoverEnvStats();
	}
	
	public boolean tick(int tickID)
	{
		if(tickID==Host.LIGHT_FLICKERS)
		{
			if((owner!=null)
			&&(lit)
			&&(!burnedOut))
			{
				if(owner instanceof Room)
				{
					burnedOut=true;
					if(invoker!=null)
						((Room)owner).show(invoker,null,Affect.MSG_OK_VISUAL,name()+" flickers and burns out.");
					if(destroyedWhenBurnedOut)
						this.destroyThis();
					((Room)owner).recoverRoomStats();
				}
				else
				if(owner instanceof MOB)
				{
					((MOB)owner).tell(((MOB)owner),null,name()+" flickers and burns out.");
					burnedOut=true;
					if(destroyedWhenBurnedOut)
						this.destroyThis();
					((MOB)owner).location().recoverRoomStats();
				}
				if(destroyedWhenBurnedOut)
					invoker=null;
			}

			lit=false;
			burnedOut=true;
			description="It looks all used up.";
		}
		return false;
	}
}
