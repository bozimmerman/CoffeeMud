package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Dance_Square extends Dance
{
	public String ID() { return "Dance_Square"; }
	public String name(){ return "Square";}
	public int quality(){ return INDIFFERENT;}
	public Environmental newInstance(){	return new Dance_Square();}
	protected boolean skipStandardDanceInvoke(){return true;}
	protected String danceOf(){return name()+" Dance";}
	
	public void affect(Environmental myHost, Affect msg)
	{
		if(msg.amISource(invoker())
		&&(affected!=invoker())
		&&(msg.sourceMinor()==Affect.TYP_SPEAK)
		&&(msg.sourceMessage()!=null)
		&&(msg.sourceMessage().length()>0))
		{
			int start=msg.sourceMessage().indexOf("'");
			int end=msg.sourceMessage().lastIndexOf("'");
			if((start>0)&&(end>start))
			{
				String cmd=msg.sourceMessage().substring(start+1,end);
				MOB M=(MOB)affected;
				try{
					if(Sense.canBeHeardBy(invoker(),M)
					&&Sense.canBeSeenBy(invoker(),M)
					&&(M.location()==invoker().location()))
						ExternalPlay.doCommand(M,Util.parse(cmd));
				}
				catch(Exception e){}
			}
		}
		super.affect(myHost,msg);
		   
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		if((!auto)&&(!Sense.aliveAwakeMobile(mob,false)))
			return false;

		boolean success=profficiencyCheck(0,auto);
		undance(mob);
		if(success)
		{
			String str=auto?"^SThe "+danceOf()+" begins!^?":"^S<S-NAME> begin(s) to dance the "+danceOf()+".^?";
			if((!auto)&&(mob.fetchAffect(this.ID())!=null))
				str="^S<S-NAME> start(s) the "+danceOf()+" over again.^?";

			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),str);
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				invoker=mob;
				Dance newOne=(Dance)this.copyOf();
				newOne.referenceDance=newOne;
				newOne.invokerManaCost=-1;

				Hashtable friends=mob.getGroupMembers(new Hashtable());

				for(int i=0;i<mob.location().numInhabitants();i++)
				{
					MOB follower=(MOB)mob.location().fetchInhabitant(i);

					// malicious dances must not affect the invoker!
					int affectType=Affect.MSG_CAST_SOMANTIC_SPELL;
					if((!friends.contains(follower))&&(follower!=mob))
						affectType=affectType|Affect.MASK_MALICIOUS;
					if(auto) affectType=affectType|Affect.MASK_GENERAL;

					if((Sense.canBeSeenBy(invoker,follower)
						&&(follower.fetchAffect(this.ID())==null)))
					{
						FullMsg msg2=new FullMsg(mob,follower,this,affectType,null);
						FullMsg msg3=msg2;
						if((!friends.contains(follower))&&(follower!=mob))
							msg2=new FullMsg(mob,follower,this,Affect.MSK_CAST_MALICIOUS_SOMANTIC|Affect.TYP_MIND|(auto?Affect.MASK_GENERAL:0),null);
						if((mob.location().okAffect(mob,msg2))&&(mob.location().okAffect(mob,msg3)))
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
			mob.location().show(mob,null,Affect.MSG_NOISE,"<S-NAME> make(s) a false step.");

		return success;
	}
}
