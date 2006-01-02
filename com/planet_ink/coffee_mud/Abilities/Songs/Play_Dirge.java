package com.planet_ink.coffee_mud.Abilities.Songs;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;


/* 
   Copyright 2000-2006 Bo Zimmerman

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
public class Play_Dirge extends Play
{
	public String ID() { return "Play_Dirge"; }
	public String name(){ return "Dirge";}
	protected int canAffectCode(){return 0;}
	public int abstractQuality(){ return BENEFICIAL_OTHERS;}
	protected boolean persistantSong(){return false;}
	protected boolean skipStandardSongTick(){return true;}
	protected String songOf(){return "a "+name();}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Item target=getTarget(mob,mob.location(),givenTarget,commands,Item.WORNREQ_UNWORNONLY);
		if(target==null) return false;

		if((!(target instanceof DeadBody))||(target.rawSecretIdentity().toUpperCase().indexOf("FAKE")>=0))
		{
			mob.tell("You may only play this for the dead.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);
		unplay(mob,mob,true);
		if(success)
		{
			String str=auto?"^S"+songOf()+" begins to play!^?":"^S<S-NAME> begin(s) to play "+songOf()+" on "+instrumentName()+".^?";
			if((!auto)&&(mob.fetchEffect(this.ID())!=null))
				str="^S<S-NAME> start(s) playing "+songOf()+" on "+instrumentName()+" again.^?";

			CMMsg msg=CMClass.getMsg(mob,null,this,somanticCastCode(mob,null,auto),str);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				invoker=mob;
				HashSet h=properTargets(mob,givenTarget,auto);
				if(h==null) return false;
				if(!h.contains(mob)) h.add(mob);

				for(Iterator f=h.iterator();f.hasNext();)
				{
					MOB follower=(MOB)f.next();

					double exp=10.0;
					int levelLimit=CMProps.getIntVar(CMProps.SYSTEMI_EXPRATE);
					int levelDiff=follower.envStats().level()-target.envStats().level();
					if(levelDiff>levelLimit) exp=0.0;
					int expGained=(int)Math.round(exp);

					// malicious songs must not affect the invoker!
					if(CMLib.flags().canBeHeardBy(invoker,follower)&&(expGained>0))
						CMLib.combat().postExperience(follower,null,null,expGained,false);
				}
				mob.location().recoverRoomStats();
				mob.location().showHappens(CMMsg.MSG_OK_VISUAL,target.name()+" fades away.");
				target.destroy();
			}
		}
		else
			mob.location().show(mob,null,CMMsg.MSG_NOISE,"<S-NAME> hit(s) a foul note.");

		return success;
	}
}
