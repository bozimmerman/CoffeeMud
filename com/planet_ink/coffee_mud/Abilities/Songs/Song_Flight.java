package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Song_Flight extends Song
{
	public String ID() { return "Song_Flight"; }
	public String name(){ return "Flight";}
	public String displayText(){ return "(Song of Flight)";}
	public int quality(){ return INDIFFERENT;}
	public Environmental newInstance(){	return new Song_Flight();}
	protected boolean skipStandardSongInvoke(){return true;}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
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
					if(auto) affectType=affectType|Affect.MASK_GENERAL;

					if((Sense.canBeHeardBy(invoker,follower)&&(follower.fetchAffect(this.ID())==null)))
					{
						FullMsg msg2=new FullMsg(mob,follower,this,affectType,null);
						if(mob.location().okAffect(msg2))
						{
							mob.location().send(mob,msg2);
							if(!msg2.wasModified())
							{
								int directionCode=-1;
								String direction="";
								for(int d=0;d<7;d++)
								{
									Exit thisExit=mob.location().getExitInDir(d);
									if(thisExit!=null)
									{
										if(thisExit.isOpen())
										{
											direction=Directions.getDirectionName(d);
											break;
										}
									}
								}
								directionCode=Directions.getDirectionCode(direction);
								if(directionCode<0)
								{
									mob.tell("Flee where?!");
									return false;
								}
								ExternalPlay.move(follower,directionCode,true,false);
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