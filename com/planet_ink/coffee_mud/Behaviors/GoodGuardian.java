package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GoodGuardian extends StdBehavior
{

	public GoodGuardian()
	{
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
	}
	public Behavior newInstance()
	{
		return new GoodGuardian();
	}

	public static void keepPeace(MOB observer)
	{
		if(!canFreelyBehaveNormal(observer)) return;
		MOB victim=null;
		boolean anythingToDo=false;
		for(int i=0;i<observer.location().numInhabitants();i++)
		{
			MOB inhab=observer.location().fetchInhabitant(i);
			if((inhab!=null)&&(inhab.isInCombat()))
			{
				if((inhab.getAlignment()>350)&&(inhab.getVictim().getAlignment()<350))
				{
					victim=inhab.getVictim();
					break;
				}
				else
				if((observer.envStats().level()>(inhab.envStats().level()+5))&&(observer.getAlignment()>350))
					anythingToDo=true;
			}
		}
		if(victim!=null)
		{
			if(!BrotherHelper.isBrother(victim,observer))
			{
				boolean yep=Aggressive.startFight(observer,victim,true);
				if(yep)	ExternalPlay.quickSay(observer,null,"PROTECT THE INNOCENT!",false,false);
			}
		}
		else
		if(anythingToDo)
		{
			Room room=observer.location();
			for(int i=0;i<room.numInhabitants();i++)
			{
				MOB inhab=room.fetchInhabitant(i);
				if((inhab!=null)
				&&(inhab.isInCombat())
				&&(inhab.getVictim().isInCombat())
				&&((observer.envStats().level()>(inhab.envStats().level()+5))
				&&(observer.getAlignment()>350)))
				{
					String msg="<S-NAME> stop(s) <T-NAME> from fighting with "+inhab.getVictim().name();
					FullMsg msgs=new FullMsg(observer,inhab,Affect.MSG_NOISYMOVEMENT,msg);
					if(observer.location().okAffect(msgs))
					{
						//observer.location().send(observer,msgs);
						inhab.getVictim().makePeace();
						inhab.makePeace();
					}
				}
			}
		}
	}



	/** this method defines how this thing responds
	 * to environmental changes.  It may handle any
	 * and every affect listed in the Affect class
	 * from the given Environmental source */
	public boolean okAffect(Environmental oking, Affect affect)
	{
		if(!super.okAffect(oking,affect)) return false;
		return GoodGuardianOkAffect(oking,affect);
	}

	public static boolean GoodGuardianOkAffect(Environmental oking, Affect affect)
	{
		MOB source=affect.source();
		if(!canFreelyBehaveNormal(oking)) return true;
		MOB monster=(MOB)oking;
		if(affect.target()==null) return true;
		if(!(affect.target() instanceof MOB))
			return true;
		MOB target=(MOB)affect.target();

		if((source!=monster)
		&&(target!=monster)
		&&(Sense.canBeSeenBy(source,monster))
		&&(Sense.canBeSeenBy(target,monster))
		&&(target.getAlignment()>650)
		&&(!target.isInCombat())
		&&(Util.bset(affect.targetCode(),Affect.MASK_MALICIOUS)))
		{
			String msg="<S-NAME> stop(s) <T-NAME> from hurting "+target.name();
			if(target.name().equals(monster.name()))
			{
				String name=monster.name();
				if(name.lastIndexOf(" ")>0)
					name=name.substring(name.lastIndexOf(" ")).trim();
				msg="The other "+name+" stops <T-NAME> from hurting "+target.name()+".";
			}
			FullMsg msgs=new FullMsg(monster,source,Affect.MSG_NOISYMOVEMENT,msg);
			if(monster.location().okAffect(msgs))
			{
				monster.location().send(monster,msgs);
				source.makePeace();
				if(target.getVictim()==source)
					target.makePeace();
				return false;
			}
		}
		return true;
	}

	public void tick(Environmental ticking, int tickID)
	{
		super.tick(ticking,tickID);

		if(tickID!=Host.MOB_TICK) return;
		if(!canFreelyBehaveNormal(ticking)) return;
		MOB mob=(MOB)ticking;
		keepPeace(mob);
	}
}
