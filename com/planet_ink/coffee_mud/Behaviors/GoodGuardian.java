package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GoodGuardian extends StdBehavior
{
	public String ID(){return "GoodGuardian";}
	public Behavior newInstance()
	{
		return new GoodGuardian();
	}

	public static MOB anyPeaceToMake(Room room, MOB observer)
	{
		if(room==null) return null;
		MOB victim=null;
		for(int i=0;i<room.numInhabitants();i++)
		{
			MOB inhab=room.fetchInhabitant(i);
			if((inhab!=null)&&(inhab.isInCombat()))
			{
				for(int b=0;b<inhab.numBehaviors();b++)
				{
					Behavior B=inhab.fetchBehavior(b);
					if((B!=null)&&(B.grantsAggressivenessTo(inhab.getVictim())))
						return inhab;
				}
				
				if((BrotherHelper.isBrother(inhab,observer))&&(victim==null))
					victim=inhab.getVictim();
				
				if((inhab.getAlignment()<350)
				||(inhab.charStats().getCurrentClass().baseClass().equalsIgnoreCase("Thief")))
					victim=inhab;
			}
		}
		return victim;
	}
	
	public static void keepPeace(MOB observer, MOB victim)
	{
		if(!canFreelyBehaveNormal(observer)) return;
		
		if(victim!=null)
		{
			if(!BrotherHelper.isBrother(victim,observer))
			{
				boolean yep=Aggressive.startFight(observer,victim,true);
				if(yep)	ExternalPlay.quickSay(observer,null,"PROTECT THE INNOCENT!",false,false);
			}
		}
		else
		{
			Room room=observer.location();
			for(int i=0;i<room.numInhabitants();i++)
			{
				MOB inhab=room.fetchInhabitant(i);
				if((inhab!=null)
				   &&(inhab.isInCombat())
				   &&(inhab.getVictim().isInCombat())
				&&((observer.envStats().level()>(inhab.envStats().level()+5))))
				{
					String msg="<S-NAME> stop(s) <T-NAME> from fighting with "+inhab.getVictim().name();
					FullMsg msgs=new FullMsg(observer,inhab,Affect.MSG_NOISYMOVEMENT,msg);
					if(observer.location().okAffect(msgs))
					{
						observer.location().send(observer,msgs);
						if(inhab.getVictim()!=null)
							inhab.getVictim().makePeace();
						inhab.makePeace();
					}
				}
			}
		}
	}

	public void tick(Environmental ticking, int tickID)
	{
		super.tick(ticking,tickID);

		if(tickID!=Host.MOB_TICK) return;
		if(!canFreelyBehaveNormal(ticking)) return;
		MOB mob=(MOB)ticking;
		MOB victim=anyPeaceToMake(mob.location(),mob);
		keepPeace(mob,victim);
	}
}
