package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Song_Thanks extends Song
{
	public String ID() { return "Song_Thanks"; }
	public String name(){ return "Thanks";}
	public int quality(){ return MALICIOUS;}
	protected boolean skipStandardSongInvoke(){return true;}
	public Environmental newInstance(){	return new Song_Thanks();}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
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
				CommonMsgs.say(mob,invoker,"Thank you "+invoker.name()+"!",false,false);
				break;
			case 2:
				CommonMsgs.say(mob,invoker,"Thanks for being you, "+invoker.name()+"!",false,false);
				break;
			case 3:
				CommonMsgs.say(mob,invoker,"Thanks "+invoker.name()+"!",false,false);
				break;
			case 4:
				CommonMsgs.say(mob,invoker,"You are great, "+invoker.name()+"!  Thanks!",false,false);
				break;
			case 5:
				CommonMsgs.say(mob,invoker,"I appreciate you, "+invoker.name()+"!",false,false);
				break;
			case 6:
				CommonMsgs.say(mob,invoker,"Keep it up, "+invoker.name()+"! Thanks!",false,false);
				break;
			case 7:
				CommonMsgs.say(mob,invoker,"Thanks a lot, "+invoker.name()+"!",false,false);
				break;
			case 8:
				CommonMsgs.say(mob,invoker,"Thank you dearly, "+invoker.name()+"!",false,false);
				break;
			case 9:
				CommonMsgs.say(mob,invoker,"Thank you always, "+invoker.name()+"!",false,false);
				break;
			case 10:
				CommonMsgs.say(mob,invoker,"You're the best, "+invoker.name()+"! Thanks!",false,false);
				break;
			}
			mob.doCommand(Util.parse("GIVE 1 "+invoker.name()));
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

		boolean success=profficiencyCheck(mob,0,auto);
		unsing(mob,mob,null);
		if(success)
		{
			String str=auto?"^SThe "+songOf()+" begins to play!^?":"^S<S-NAME> begin(s) to sing the "+songOf()+".^?";
			if((!auto)&&(mob.fetchEffect(this.ID())!=null))
				str="^S<S-NAME> start(s) the "+songOf()+" over again.^?";

			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),str);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				invoker=mob;
				Song newOne=(Song)this.copyOf();
				newOne.referenceSong=newOne;

				HashSet h=properTargets(mob,givenTarget,auto);
				if(h==null) return false;
				if(!h.contains(mob)) h.add(mob);

				for(Iterator f=h.iterator();f.hasNext();)
				{
					MOB follower=(MOB)f.next();

					// malicious songs must not affect the invoker!
					int affectType=CMMsg.MSG_CAST_VERBAL_SPELL;
					if(auto) affectType=affectType|CMMsg.MASK_GENERAL;

					if((Sense.canBeHeardBy(invoker,follower)&&(follower.fetchEffect(this.ID())==null)))
					{
						FullMsg msg2=new FullMsg(mob,follower,this,affectType,null);
						FullMsg msg3=msg2;
						if((mob.location().okMessage(mob,msg2))&&(mob.location().okMessage(mob,msg3)))
						{
							follower.location().send(follower,msg2);
							if(msg2.value()<=0)
							{
								follower.location().send(follower,msg3);
								if((msg3.value()<=0)&&(follower.fetchEffect(newOne.ID())==null))
								{
									if(follower!=mob)
										follower.addEffect((Ability)newOne.copyOf());
									else
										follower.addEffect(newOne);
								}
							}
						}
					}
				}
				mob.location().recoverRoomStats();
			}
		}
		else
			mob.location().show(mob,null,CMMsg.MSG_NOISE,"<S-NAME> hit(s) a foul note.");

		return success;
	}
}