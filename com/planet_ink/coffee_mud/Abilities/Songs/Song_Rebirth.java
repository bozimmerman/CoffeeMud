package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Song_Rebirth extends Song
{
	public String ID() { return "Song_Rebirth"; }
	public String name(){ return "Rebirth";}
	public int quality(){ return INDIFFERENT;}
	protected boolean skipStandardSongInvoke(){return true;}
	public Environmental newInstance(){	return new Song_Rebirth();}

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
			String str=auto?"The "+songOf()+" begins to play!":"^S<S-NAME> begin(s) to sing the "+songOf()+".^?";
			if((!auto)&&(mob.fetchAffect(this.ID())!=null))
				str="^S<S-NAME> start(s) the "+songOf()+" over again.^?";

			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),str);
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				boolean foundOne=false;
				int i=0;
				while(i<mob.location().numItems())
				{
					Item body=mob.location().fetchItem(i);
					int x=0;
					if((body!=null)&&(body instanceof DeadBody)&&((x=body.Name().toUpperCase().indexOf("BODY OF"))>=0))
					{
						String mobName=body.Name().substring(x+7).trim();
						MOB rejuvedMOB=CMMap.getPlayer(mobName);
						if(rejuvedMOB!=null)
						{
							rejuvedMOB.tell("You are being resusitated.");
							if(rejuvedMOB.location()!=mob.location())
							{
								rejuvedMOB.location().delInhabitant(rejuvedMOB);
								rejuvedMOB.location().showOthers(rejuvedMOB,null,Affect.MSG_OK_VISUAL,"<S-NAME> disappear(s)!");
								mob.location().addInhabitant(rejuvedMOB);
								rejuvedMOB.setLocation(mob.location());
							}
							int it=0;
							while(it<rejuvedMOB.location().numItems())
							{
								Item item=rejuvedMOB.location().fetchItem(it);
								if((item!=null)&&(item.container()==body))
								{
									FullMsg msg2=new FullMsg(rejuvedMOB,body,item,Affect.MSG_GET,null);
									rejuvedMOB.location().send(rejuvedMOB,msg2);
									FullMsg msg3=new FullMsg(rejuvedMOB,item,null,Affect.MSG_GET,null);
									rejuvedMOB.location().send(rejuvedMOB,msg3);
									it=0;
								}
								else
									it++;
							}
							body.destroyThis();
							mob.location().recoverRoomStats();
							foundOne=true;
							rejuvedMOB.location().show(rejuvedMOB,null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> get(s) up!");
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
			mob.location().show(mob,null,Affect.MSG_NOISE,"<S-NAME> hit(s) a foul note.");

		return success;
	}
}
