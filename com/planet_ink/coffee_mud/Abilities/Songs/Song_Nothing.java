package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Song_Nothing extends Song
{
	public String ID() { return "Song_Nothing"; }
	public String name(){ return "Nothing";}
	public int quality(){ return MALICIOUS;}
	protected boolean skipStandardSongInvoke(){return true;}
	public Song_Nothing()
	{
		super();
		setProfficiency(100);
	}
	public Environmental newInstance(){	return new Song_Nothing();}
	public void setProfficiency(int newProfficiency){	super.setProfficiency(100);}
	

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		boolean foundOne=false;
		for(int a=0;a<mob.numAffects();a++)
		{
			Ability A=(Ability)mob.fetchAffect(a);
			if((A!=null)&&(A instanceof Song))
				foundOne=true;
		}
		unsing(mob);
		if(!foundOne)
		{
			mob.tell(auto?"There is no song playing.":"You aren't singing.");
			return true;
		}

		mob.location().show(mob,null,Affect.MSG_NOISE,auto?"Silence.":"<S-NAME> stop(s) singing.");
		mob.location().recoverRoomStats();
		return true;
	}
}