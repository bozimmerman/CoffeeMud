package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Song_Thanks extends Song
{

	public Song_Thanks()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Thanks";
		displayText="(Song of Thanks)";
		miscText="";

		canBeUninvoked=true;
		isAutoinvoked=false;
		skipStandardSongInvoke=true;

		quality=Ability.INDIFFERENT;
		mindAttack=true;

		baseEnvStats().setLevel(11);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Song_Thanks();
	}
	
	public boolean tick(int tickID)
	{
		if(!super.tick(tickID))
			return false;

		MOB mob=(MOB)affected;
		if(mob==null) return true;
		if(mob==invoker) return true;
		if(invoker==null) return true;
		if(mob.location()!=invoker.location()) return true;
		//if(!mob.isMonster()) return true;
		if((Dice.rollPercentage()<6)
		   &&(Dice.rollPercentage()>mob.charStats().getSave(CharStats.SAVE_MIND))
		   &&(Dice.rollPercentage()>mob.charStats().getSave(CharStats.SAVE_MAGIC))
		   &&(Sense.canMove(mob))
		   &&(Sense.canBeSeenBy(invoker,mob))
		   &&(mob.getMoney()>0))
		{
			switch(Dice.roll(1,10,0))
			{
			case 1:
				ExternalPlay.quickSay(mob,invoker,"Thank you "+invoker.name()+"!",false,false);
				break;
			case 2:
				ExternalPlay.quickSay(mob,invoker,"Thanks for being you, "+invoker.name()+"!",false,false);
				break;
			case 3:
				ExternalPlay.quickSay(mob,invoker,"Thanks "+invoker.name()+"!",false,false);
				break;
			case 4:
				ExternalPlay.quickSay(mob,invoker,"You are great, "+invoker.name()+"!  Thanks!",false,false);
				break;
			case 5:
				ExternalPlay.quickSay(mob,invoker,"I appreciate you, "+invoker.name()+"!",false,false);
				break;
			case 6:
				ExternalPlay.quickSay(mob,invoker,"Keep it up, "+invoker.name()+"! Thanks!",false,false);
				break;
			case 7:
				ExternalPlay.quickSay(mob,invoker,"Thanks a lot, "+invoker.name()+"!",false,false);
				break;
			case 8:
				ExternalPlay.quickSay(mob,invoker,"Thank you dearly, "+invoker.name()+"!",false,false);
				break;
			case 9:
				ExternalPlay.quickSay(mob,invoker,"Thank you always, "+invoker.name()+"!",false,false);
				break;
			case 10:
				ExternalPlay.quickSay(mob,invoker,"You're the best, "+invoker.name()+"! Thanks!",false,false);
				break;
			}
			try
			{	ExternalPlay.doCommand(mob,Util.parse("GIVE 1 "+invoker.name()));	}
			catch(Exception e){}
		}
		return true;
	}
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		if((!auto)&&(!Sense.canSpeak(mob)))
		{
			mob.tell("You can't sing!");
			return false;
		}

		boolean success=profficiencyCheck(0,auto);
		unsing(mob);
		if(success)
		{
			String str=auto?"^SThe song of "+name()+" begins to play!^?":"^S<S-NAME> begin(s) to sing the Song of "+name()+".^?";
			if((!auto)&&(mob.fetchAffect(this.ID())!=null))
				str="^S<S-NAME> start(s) the Song of "+name()+" over again.^?";

			FullMsg msg=new FullMsg(mob,null,this,affectType,str);
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				invoker=mob;
				Song newOne=(Song)this.copyOf();
				newOne.referenceSong=newOne;

				quality=Ability.MALICIOUS;
				Hashtable h=ExternalPlay.properTargets(this,mob,auto);
				quality=Ability.INDIFFERENT;
				if(h==null) return false;
				if(h.get(mob)==null) h.put(mob,mob);

				for(Enumeration f=h.elements();f.hasMoreElements();)
				{
					MOB follower=(MOB)f.nextElement();

					// malicious songs must not affect the invoker!
					affectType=Affect.MSG_CAST_VERBAL_SPELL;
					if(auto) affectType=affectType|Affect.ACT_GENERAL;
					
					if((Sense.canBeHeardBy(invoker,follower)&&(follower.fetchAffect(this.ID())==null)))
					{
						FullMsg msg2=new FullMsg(mob,follower,this,affectType,null);
						FullMsg msg3=msg2;
						if((mob.location().okAffect(msg2))&&(mob.location().okAffect(msg3)))
						{
							follower.location().send(follower,msg2);
							if(!msg2.wasModified())
							{
								follower.location().send(follower,msg3);
								if((!msg3.wasModified())&&(follower.fetchAffect(newOne.ID())==null))
								{
									if(follower!=mob)
										follower.addAffect((Ability)newOne.copyOf());
									else
										follower.addAffect(newOne);
								}
							}
						}
					}
				}
				mob.location().recoverRoomStats();
			}
		}
		else
			mob.location().show(mob,null,Affect.MSG_NOISE,"<S-NAME> hit(s) a foul note.");

		return success;
	}
}