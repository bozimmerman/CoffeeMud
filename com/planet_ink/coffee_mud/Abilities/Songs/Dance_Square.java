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

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if(msg.amISource(invoker())
		&&(affected!=invoker())
		&&(msg.sourceMinor()==CMMsg.TYP_SPEAK)
		&&(msg.sourceMessage()!=null)
		&&(msg.sourceMessage().length()>0))
		{
			int start=msg.sourceMessage().indexOf("'");
			int end=msg.sourceMessage().lastIndexOf("'");
			if((start>0)&&(end>start))
			{
				String cmd=msg.sourceMessage().substring(start+1,end);
				MOB M=(MOB)affected;
				if(!cmd.toUpperCase().startsWith("FOL"))
				{
					if(Sense.canBeHeardBy(invoker(),M)
					&&Sense.canBeSeenBy(invoker(),M)
					&&(M.location()==invoker().location()))
						M.enqueCommand(Util.parse(cmd),0);
				}
			}
		}
		super.executeMsg(myHost,msg);

	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		if((!auto)&&(!Sense.aliveAwakeMobile(mob,false)))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);
		undance(mob,null,true);
		if(success)
		{
			String str=auto?"^SThe "+danceOf()+" begins!^?":"^S<S-NAME> begin(s) to dance the "+danceOf()+".^?";
			if((!auto)&&(mob.fetchEffect(this.ID())!=null))
				str="^S<S-NAME> start(s) the "+danceOf()+" over again.^?";

			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),str);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				invoker=mob;
				Dance newOne=(Dance)this.copyOf();
				newOne.invokerManaCost=-1;

				HashSet friends=mob.getGroupMembers(new HashSet());

				for(int i=0;i<mob.location().numInhabitants();i++)
				{
					MOB follower=(MOB)mob.location().fetchInhabitant(i);

					// malicious dances must not affect the invoker!
					int affectType=CMMsg.MSG_CAST_SOMANTIC_SPELL;
					if((!friends.contains(follower))&&(follower!=mob))
						affectType=affectType|CMMsg.MASK_MALICIOUS;
					if(auto) affectType=affectType|CMMsg.MASK_GENERAL;

					if((Sense.canBeSeenBy(invoker,follower)
						&&(follower.fetchEffect(this.ID())==null)))
					{
						FullMsg msg2=new FullMsg(mob,follower,this,affectType,null);
						FullMsg msg3=msg2;
						if((!friends.contains(follower))&&(follower!=mob))
							msg2=new FullMsg(mob,follower,this,CMMsg.MSK_CAST_MALICIOUS_SOMANTIC|CMMsg.TYP_MIND|(auto?CMMsg.MASK_GENERAL:0),null);
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
			mob.location().show(mob,null,CMMsg.MSG_NOISE,"<S-NAME> make(s) a false step.");

		return success;
	}
}
