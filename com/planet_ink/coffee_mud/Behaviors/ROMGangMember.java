package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class ROMGangMember extends StdBehavior
{
	public String ID(){return "ROMGangMember";}
	public Behavior newInstance()
	{
		return new ROMGangMember();
	}

	int tickTock=5;
	public void pickAFight(MOB observer)
	{
		if(!canFreelyBehaveNormal(observer)) return;
		if(observer.location().numPCInhabitants()==0)
			return;

		MOB victim=null;
		String vicParms="";
		for(int i=0;i<observer.location().numInhabitants();i++)
		{
			MOB inhab=observer.location().fetchInhabitant(i);
			if(inhab!=null)
			{
				for(int b=0;b<inhab.numBehaviors();b++)
				{
					Behavior B=inhab.fetchBehavior(b);
					if(B.ID().equals(ID())&&(!B.getParms().equals(getParms())))
					{
					   victim=inhab;
					   vicParms=B.getParms();
					}
					else
					if((B.ID().indexOf("GoodGuardian")>=0)||(B.ID().indexOf("Patrolman")>=0))
						return;
				}
			}
		}


		if(victim==null) return;
		Item weapon=observer.fetchWieldedItem();
		if(weapon==null) weapon=observer.myNaturalWeapon();

		/* say something, then raise hell */
		switch (Dice.roll(1,7,-1))
		{
		case 0:
			observer.location().show(observer,null,CMMsg.MSG_SPEAK,"^T<S-NAME> yell(s) 'I've been looking for you, punk!'^?");
			break;
		case 1:
			observer.location().show(observer,victim,CMMsg.MSG_NOISYMOVEMENT,"With a scream of rage, <S-NAME> attack(s) <T-NAME>.");
			break;
		case 2:
			observer.location().show(observer,victim,CMMsg.MSG_SPEAK,"^T<S-NAME> say(s) 'What's slimy "+vicParms+" trash like you doing around here?'^?");
			break;
		case 3:
			observer.location().show(observer,victim,CMMsg.MSG_SPEAK,"^T<S-NAME> crack(s) <S-HIS-HER> knuckles and say(s) 'Do ya feel lucky?'^?");
			break;
		case 4:
			observer.location().show(observer,victim,CMMsg.MSG_SPEAK,"^T<S-NAME> say(s) 'There's no cops to save you this time!'^?");
			break;
		case 5:
			observer.location().show(observer,victim,CMMsg.MSG_SPEAK,"^T<S-NAME> say(s) 'Time to join your brother, spud.'^?");
			break;
		case 6:
			observer.location().show(observer,victim,CMMsg.MSG_SPEAK,"^T<S-NAME> say(s) 'Let's rock.'^?");
			break;
		}

		ExternalPlay.postAttack(observer,victim,weapon);
	}


	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);

		if(tickID!=Host.TICK_MOB) return true;
		MOB mob=(MOB)ticking;
		tickTock--;
		if(tickTock<=0)
		{
			tickTock=Dice.roll(1,10,0);
			pickAFight(mob);
		}
		return true;
	}
}
