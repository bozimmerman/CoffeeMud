package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Locales.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import java.util.*;
public class LightSource extends StdItem
{
	protected boolean lit=false;
	public boolean burnedOut=false;
	protected int durationTicks=50;
	protected boolean destroyedWhenBurnedOut=true;

	protected MOB invoker=null;
	protected Environmental heldBy=null;


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
		switch(affect.targetCode())
		{
		case Affect.HANDS_HOLD:
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
		if((mob.location()!=null)
		   &&(mob.location() instanceof UnderWater)
		   &&(this.lit)
		   &&(!this.burnedOut)
		   &&(mob.isMine(this)))
		{
			mob.tell("The water makes "+name()+" go out.");
			tick(ServiceEngine.LIGHT_FLICKERS);
		}

		if(affect.amITarget(this))
			switch(affect.targetCode())
			{
			case Affect.HANDS_HOLD:
				if((!burnedOut)&&(mob.location()!=null))
				{
					mob.location().show(mob,null,Affect.VISUAL_WNOISE,"<S-NAME> light(s) up "+name()+".");
					invoker=mob;
					heldBy=mob;
					lit=true;
					ServiceEngine.startTickDown(this,ServiceEngine.LIGHT_FLICKERS,durationTicks);
					this.recoverEnvStats();
					mob.location().recoverRoomStats();
				}
				break;
			}
		super.affect(affect);
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
	public void affectEnvStats(Environmental affected, Stats affectableStats)
	{
		if((!burnedOut)&&(lit))
		{
			if(affected instanceof MOB)
			{
				invoker=(MOB)affected;
				if(!this.amWearingAt(Item.INVENTORY))
					heldBy=affected;
			}
			else
			if(affected instanceof Room)
			{
				heldBy=affected;
			}
		}
		super.affectEnvStats(affected,affectableStats);
	}

	public boolean tick(int tickID)
	{
		if(tickID==ServiceEngine.LIGHT_FLICKERS)
		{
			if((heldBy!=null)
			&&(lit)
			&&(!burnedOut))
			{
				if(heldBy instanceof Room)
				{
					burnedOut=true;
					if(invoker!=null)
						((Room)heldBy).show(invoker,null,Affect.VISUAL_ONLY,name()+" flickers and burns out.");
					if(destroyedWhenBurnedOut)
						this.destroyThis();
					((Room)heldBy).recoverRoomStats();
				}
				else
				if(heldBy instanceof MOB)
				{
					((MOB)heldBy).tell(((MOB)heldBy),null,name()+" flickers and burns out.");
					burnedOut=true;
					if(destroyedWhenBurnedOut)
						this.destroyThis();
					((MOB)heldBy).location().recoverRoomStats();
				}
				if(destroyedWhenBurnedOut)
				{
					invoker=null;
					heldBy=null;
				}
			}

			lit=false;
			burnedOut=true;
			description="It looks all used up.";
		}
		return false;
	}
}
