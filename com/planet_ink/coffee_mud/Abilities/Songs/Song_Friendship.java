package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Song_Friendship extends Song
{
	public String ID() { return "Song_Friendship"; }
	public String name(){ return "Friendship";}
	public String displayText(){ return "(Song of Friendship)";}
	public int quality(){ return MALICIOUS;}
	public Environmental newInstance(){	return new Song_Friendship();}
	protected boolean skipStandardSongInvoke(){return true;}

	public boolean tick(int tickID)
	{
		if(!super.tick(tickID))
			return false;

		MOB mob=(MOB)affected;
		if(mob==null) return false;
		if(mob==invoker) return true;
		if(mob.amFollowing()!=invoker)
			return false;
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

			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),str);
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				invoker=mob;
				Song newOne=(Song)this.copyOf();
				newOne.referenceSong=newOne;

				Hashtable h=ExternalPlay.properTargets(this,mob,auto);
				if(h==null) return false;

				// malicious songs must not affect the invoker!
				if(h.get(mob)==null) h.put(mob,mob);

				for(Enumeration f=h.elements();f.hasMoreElements();)
				{
					MOB follower=(MOB)f.nextElement();
					// malicious songs must not affect the invoker!
					int affectType=Affect.MSG_CAST_VERBAL_SPELL;
					if((quality()==Ability.MALICIOUS)&&(follower!=mob))
						affectType=Affect.MSG_CAST_ATTACK_VERBAL_SPELL;
					if(auto) affectType=affectType|Affect.ACT_GENERAL;

					if((Sense.canBeHeardBy(invoker,follower)&&(follower.fetchAffect(this.ID())==null)))
					{
						FullMsg msg2=new FullMsg(mob,follower,this,affectType,null);
						FullMsg msg3=msg2;
						if((mindAttack())&&(follower!=mob))
							msg2=new FullMsg(mob,follower,this,Affect.MSK_CAST_MALICIOUS_VERBAL|Affect.TYP_MIND|(auto?Affect.ACT_GENERAL:0),null);
						int levelDiff=follower.envStats().level()-mob.envStats().level();

						if((levelDiff>3)&&(mindAttack()))
							mob.tell(mob,follower,"<T-NAME> looks too powerful.");
						else
						if((mob.location().okAffect(msg2))&&(mob.location().okAffect(msg3)))
						{
							mob.location().send(mob,msg2);
							if(!msg2.wasModified())
							{
								mob.location().send(mob,msg3);
								if((!msg3.wasModified())&&(follower.fetchAffect(newOne.ID())==null))
								{
									if((follower.amFollowing()!=mob)&&(follower!=mob))
									{
										ExternalPlay.follow(follower,mob,false);
										if(follower.amFollowing()==mob)
										{
											if(follower!=mob)
												follower.addAffect((Ability)newOne.copyOf());
											else
												follower.addAffect(newOne);
											ExternalPlay.makePeaceInGroup(mob);
										}
									}
								}
							}
						}
					}
				}
			}
		}
		else
			mob.location().show(mob,null,Affect.MSG_NOISE,"<S-NAME> hit(s) a foul note.");

		return success;
	}
}