package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


/* 
   Copyright 2000-2004 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
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
	public void setProfficiency(int newProfficiency){	super.setProfficiency(100);}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		boolean foundOne=false;
		for(int a=0;a<mob.numEffects();a++)
		{
			Ability A=(Ability)mob.fetchEffect(a);
			if((A!=null)&&(A instanceof Dance))
				foundOne=true;
		}
		undance(mob,null,false);
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
