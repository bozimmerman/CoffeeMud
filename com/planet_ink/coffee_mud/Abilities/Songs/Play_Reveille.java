package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Play_Reveille extends Play
{
	public String ID() { return "Play_Reveille"; }
	public String name(){ return "Reveille";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	protected int canAffectCode(){return 0;}
	public Environmental newInstance(){	return new Play_Reveille();}
	protected boolean skipStandardSongTick(){return true;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		boolean success=profficiencyCheck(0,auto);
		unplay(mob,mob,null);
		if(success)
		{
			String str=auto?"^S"+songOf()+" begins to play!^?":"^S<S-NAME> begin(s) to play "+songOf()+" on "+instrumentName()+".^?";
			if((!auto)&&(mob.fetchEffect(this.ID())!=null))
				str="^S<S-NAME> start(s) playing "+songOf()+" on "+instrumentName()+" again.^?";

			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),str);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				invoker=mob;
				Play newOne=(Play)this.copyOf();
				newOne.referencePlay=newOne;

				Hashtable h=new Hashtable();
				for(int i=0;i<mob.location().numInhabitants();i++)
				{ MOB M=mob.location().fetchInhabitant(i); h.put(M,M);}
				for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
				{
					Room R=mob.location().getRoomInDir(d);
					if(R!=null)
						for(int i=0;i<R.numInhabitants();i++)
						{ MOB M=R.fetchInhabitant(i); h.put(M,M);}
				}

				for(Enumeration f=h.elements();f.hasMoreElements();)
				{
					MOB follower=(MOB)f.nextElement();
					Room R=follower.location();

					// malicious songs must not affect the invoker!
					int affectType=CMMsg.MSG_CAST_SOMANTIC_SPELL;
					if(auto) affectType=affectType|CMMsg.MASK_GENERAL;
					if((Sense.canBeHeardBy(invoker,follower)&&(follower.fetchEffect(this.ID())==null)))
					{
						FullMsg msg2=new FullMsg(mob,follower,this,affectType,null);
						if(R.okMessage(mob,msg2))
						{
							follower.location().send(follower,msg2);
							if(Sense.isSleeping(follower))
							{
								try{ExternalPlay.doCommand(follower,Util.parse("WAKE"));}catch(Exception e){}
								if(!Sense.isSleeping(follower))
								{
									Ability A=CMClass.getAbility("Searching");
									if(A!=null)	A.invoke(follower,null,true);
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
