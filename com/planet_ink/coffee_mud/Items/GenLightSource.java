package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GenLightSource extends GenItem implements Light
{
	protected boolean lit=false;
	protected boolean destroyedWhenBurnedOut=true;

	protected MOB invoker=null;
	protected Environmental heldBy=null;


	public GenLightSource()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a generic lightable thing";
		displayText="a generic lightable thing sits here.";
		description="Looks like something you can use for light.";
		isReadable=false;
		destroyedWhenBurnedOut=true;
	}

	public Environmental newInstance()
	{
		return new GenLightSource();
	}
	public boolean isGeneric(){return true;}

	public void setDuration(int duration){readableText=""+duration;}
	public int getDuration(){return Util.s_int(readableText);}

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
			if(getDuration()==0)
			{
				mob.tell(name()+" looks used up.");
				return false;
			}
			if((mob.location()!=null)
			   &&(mob.location().domainConditions()==Room.CONDITION_WET)
			   &&(getDuration()>0)
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
		   &&((mob.location().domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
				||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE))
		   &&(this.lit)
		   &&(getDuration()>0)
		   &&(mob.isMine(this)))
		{
			mob.tell("The water makes "+name()+" go out.");
			tick(Host.LIGHT_FLICKERS);
		}

		if(affect.amITarget(this))
			switch(affect.targetMinor())
			{
			case Affect.TYP_HOLD:
				if((getDuration()>0)&&(mob.location()!=null))
				{
					affect.addTrailerMsg(new FullMsg(mob,null,Affect.MSG_OK_VISUAL,"<S-NAME> light(s) up "+name()+"."));
					invoker=mob;
					heldBy=mob;
					lit=true;
					ExternalPlay.startTickDown(this,Host.LIGHT_FLICKERS,getDuration());
					this.recoverEnvStats();
					mob.location().recoverRoomStats();
				}
				break;
			}
		super.affect(affect);
	}

	public void recoverEnvStats()
	{
		if((getDuration()>0)&&(lit))
			baseEnvStats().setDisposition(baseEnvStats().disposition()|Sense.IS_LIGHT);
		else
		if((baseEnvStats().disposition()&Sense.IS_LIGHT)==Sense.IS_LIGHT)
			baseEnvStats().setDisposition(baseEnvStats().disposition()-Sense.IS_LIGHT);
		super.recoverEnvStats();
	}
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		if((getDuration()>0)&&(lit))
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
		if(tickID==Host.LIGHT_FLICKERS)
		{
			if((heldBy!=null)
			&&(lit)
			&&(getDuration()>0))
			{
				if(heldBy instanceof Room)
				{
					setDuration(0);
					if(invoker!=null)
						((Room)heldBy).show(invoker,null,Affect.MSG_OK_VISUAL,name()+" flickers and burns out.");
					recoverEnvStats();
					((Room)heldBy).recoverRoomStats();
					if(destroyedWhenBurnedOut)
						this.destroyThis();
				}
				else
				if(heldBy instanceof MOB)
				{
					((MOB)heldBy).tell(((MOB)heldBy),null,name()+" flickers and burns out.");
					setDuration(0);
					recoverEnvStats();
					((MOB)heldBy).recoverEnvStats();
					((MOB)heldBy).location().recoverRoomStats();
					if(destroyedWhenBurnedOut)
						this.destroyThis();
				}

				if(destroyedWhenBurnedOut)
				{
					invoker=null;
					heldBy=null;
				}
			}

			lit=false;
			setDuration(0);
			description="It looks all used up.";
		}
		return false;
	}
}
