package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Song_Rebirth extends Song
{

	public Song_Rebirth()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Rebirth";
		displayText="(Song of Rebirth)";
		miscText="";

		canBeUninvoked=true;
		isAutoinvoked=false;

		skipStandardSongInvoke=true;

		baseEnvStats().setLevel(25);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Song_Rebirth();
	}

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
			String str=auto?"The song of "+name()+" begins to play!":"<S-NAME> begin(s) to sing the Song of "+name()+".";
			if((!auto)&&(mob.fetchAffect(this.ID())!=null))
				str="<S-NAME> start(s) the Song of "+name()+" over again.";

			FullMsg msg=new FullMsg(mob,null,this,affectType,str);
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				boolean foundOne=false;
				int i=0;
				while(i<mob.location().numItems())
				{
					Item body=mob.location().fetchItem(i);
					int x=0;
					if((body!=null)&&(body instanceof DeadBody)&&((x=body.name().toUpperCase().indexOf("BODY OF"))>=0))
					{
						String mobName=body.name().substring(x+7).trim();
						MOB rejuvedMOB=(MOB)CMMap.MOBs.get(mobName);
						if(rejuvedMOB!=null)
						{
							rejuvedMOB.tell(rejuvedMOB,null,"You are being resusitated.");
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
								if((item!=null)&&(item.location()==body))
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
					mob.tell(mob,null,"Nothing seems to happen.");
			}
		}
		else
			mob.location().show(mob,null,Affect.MSG_NOISE,"<S-NAME> hit(s) a foul note.");

		return success;
	}
}
