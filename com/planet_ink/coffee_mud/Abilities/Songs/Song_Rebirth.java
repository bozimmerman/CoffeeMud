package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class Song_Rebirth extends Song
{
	public String ID() { return "Song_Rebirth"; }
	public String name(){ return "Rebirth";}
	public int quality(){ return INDIFFERENT;}
	protected boolean skipStandardSongInvoke(){return true;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		if((!auto)&&(!Sense.canSpeak(mob)))
		{
			mob.tell("You can't sing!");
			return false;
		}

		boolean success=profficiencyCheck(mob,0,auto);
		unsing(mob,mob,true);
		if(success)
		{
			String str=auto?"The "+songOf()+" begins to play!":"^S<S-NAME> begin(s) to sing the "+songOf()+".^?";
			if((!auto)&&(mob.fetchEffect(this.ID())!=null))
				str="^S<S-NAME> start(s) the "+songOf()+" over again.^?";

			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),str);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				boolean foundOne=false;
				int i=0;
				while(i<mob.location().numItems())
				{
					Item body=mob.location().fetchItem(i);
					if((body!=null)
					&&(body instanceof DeadBody)
					&&(((DeadBody)body).playerCorpse())
					&&(((DeadBody)body).mobName().length()>0))
					{
						MOB rejuvedMOB=CMMap.getPlayer(((DeadBody)body).mobName());
						if(rejuvedMOB!=null)
						{
							rejuvedMOB.tell("You are being resusitated.");
							if(rejuvedMOB.location()!=mob.location())
							{
								rejuvedMOB.location().showOthers(rejuvedMOB,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> disappear(s)!");
								mob.location().bringMobHere(rejuvedMOB,false);
							}

							Ability A=rejuvedMOB.fetchAbility("Prop_AstralSpirit");
							if(A!=null) rejuvedMOB.delAbility(A);
							A=rejuvedMOB.fetchEffect("Prop_AstralSpirit");
							if(A!=null) rejuvedMOB.delEffect(A);

							int it=0;
							while(it<rejuvedMOB.location().numItems())
							{
								Item item=rejuvedMOB.location().fetchItem(it);
								if((item!=null)&&(item.container()==body))
								{
									FullMsg msg2=new FullMsg(rejuvedMOB,body,item,CMMsg.MSG_GET,null);
									rejuvedMOB.location().send(rejuvedMOB,msg2);
									FullMsg msg3=new FullMsg(rejuvedMOB,item,null,CMMsg.MSG_GET,null);
									rejuvedMOB.location().send(rejuvedMOB,msg3);
									it=0;
								}
								else
									it++;
							}
							body.destroy();
							mob.location().recoverRoomStats();
							foundOne=true;
							rejuvedMOB.location().show(rejuvedMOB,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> get(s) up!");
							i=0;
						}
						else
							i++;
					}
					else
						i++;
				}
				if(!foundOne)
					mob.tell("Nothing seems to happen.");
			}
		}
		else
			mob.location().show(mob,null,CMMsg.MSG_NOISE,"<S-NAME> hit(s) a foul note.");

		return success;
	}
}
