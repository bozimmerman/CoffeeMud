package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Dance_Stop extends Dance
{
	public String ID() { return "Dance_Stop"; }
	public String name(){ return "Stop";}
	public int quality(){ return INDIFFERENT;}
	protected boolean skipStandardSongInvoke(){return true;}
	public Dance_Stop()
	{
		super();
		setProfficiency(100);
	}
	public Environmental newInstance(){	return new Dance_Stop();}
	public void setProfficiency(int newProfficiency){	super.setProfficiency(100);}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		boolean foundOne=false;
		for(int a=0;a<mob.numEffects();a++)
		{
			Ability A=(Ability)mob.fetchEffect(a);
			if((A!=null)&&(A instanceof Dance))
				foundOne=true;
		}
		undance(mob,null,null);
		if(!foundOne)
		{
			mob.tell(auto?"There is no dance going.":"You aren't dancing.");
			return true;
		}

		mob.location().show(mob,null,CMMsg.MSG_NOISE,auto?"Rest.":"<S-NAME> stop(s) dancing.");
		mob.location().recoverRoomStats();
		return true;
	}
}