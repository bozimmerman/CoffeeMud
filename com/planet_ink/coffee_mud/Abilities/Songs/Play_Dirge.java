package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Play_Dirge extends Play
{
	public String ID() { return "Play_Dirge"; }
	public String name(){ return "Dirge";}
	protected int canAffectCode(){return 0;}
	public int quality(){ return BENEFICIAL_OTHERS;}
	public Environmental newInstance(){	return new Play_Dirge();}
	protected boolean persistantSong(){return false;}
	protected boolean skipStandardSongTick(){return true;}
	protected String songOf(){return "a "+name();}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if((mob.location().domainType()&Room.INDOORS)>0)
		{
			mob.tell("The dead cannot be buried here");
			return false;
		}
		if((mob.location().domainType()==Room.DOMAIN_OUTDOORS_CITY)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE))
		{
			mob.tell("The dead cannot be buried here");
			return false;
		}
		Item target=getTarget(mob,mob.location(),givenTarget,commands,Item.WORN_REQ_UNWORNONLY);
		if(target==null) return false;

		if(!(target instanceof DeadBody))
		{
			mob.tell("You may only play this for the dead.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		
		boolean success=profficiencyCheck(0,auto);
		unplay(mob);
		if(success)
		{
			String str=auto?"^S"+songOf()+" begins to play!^?":"^S<S-NAME> begin(s) to play "+songOf()+" on "+instrumentName()+".^?";
			if((!auto)&&(mob.fetchAffect(this.ID())!=null))
				str="^S<S-NAME> start(s) playing "+songOf()+" on "+instrumentName()+" again.^?";

			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),str);
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				invoker=mob;
				Play newOne=(Play)this.copyOf();
				newOne.referencePlay=newOne;

				Hashtable h=ExternalPlay.properTargets(this,mob,auto);
				if(h==null) return false;
				if(h.get(mob)==null) h.put(mob,mob);
				
				for(Enumeration f=h.elements();f.hasMoreElements();)
				{
					MOB follower=(MOB)f.nextElement();
					Room R=follower.location();

					double exp=10.0;
					int levelLimit=CommonStrings.getIntVar(CommonStrings.SYSTEMI_EXPRATE);
					int levelDiff=follower.envStats().level()-target.envStats().level();
					if(levelDiff>levelLimit) exp=0.0;
					int expGained=(int)Math.round(exp);

					// malicious songs must not affect the invoker!
					if(Sense.canBeHeardBy(invoker,follower)&&(expGained>0))
						follower.charStats().getCurrentClass().gainExperience(follower,null,follower.getLeigeID(),expGained,false);
				}
				mob.location().recoverRoomStats();
			}
		}
		else
			mob.location().show(mob,null,Affect.MSG_NOISE,"<S-NAME> hit(s) a foul note.");

		return success;
	}
}
	