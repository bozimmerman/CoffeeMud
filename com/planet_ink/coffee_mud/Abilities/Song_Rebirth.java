package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.db.*;
import com.planet_ink.coffee_mud.Items.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
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

		addQualifyingClass(new Bard().ID(),25);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Song_Rebirth();
	}

	public boolean invoke(MOB mob, Vector commands)
	{
		if(!super.invoke(mob,commands))
			return false;

		if(!Sense.canSpeak(mob))
		{
			mob.tell("You can't sing!");
			return false;
		}

		boolean success=profficiencyCheck(0);
		if(success)
		{
			String str="<S-NAME> begin(s) to sing the Song of "+name()+".";
			if(mob.fetchAffect(this.ID())!=null)
				str="<S-NAME> start(s) the Song of "+name()+" over again.";
			unsing(mob);

			FullMsg msg=new FullMsg(mob,null,this,Affect.SOUND_MAGIC,str,Affect.SOUND_WORDS,str,Affect.SOUND_WORDS,str);
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				boolean foundOne=false;
				int i=0;
				while(i<mob.location().numItems())
				{
					Item body=mob.location().fetchItem(i);
					int x=0;
					if((body instanceof DeadBody)&&((x=body.name().toUpperCase().indexOf("BODY OF"))>=0))
					{
						String mobName=body.name().substring(x+7).trim();
						MOB rejuvedMOB=(MOB)MOBloader.MOBs.get(mobName);
						if(rejuvedMOB!=null)
						{
							rejuvedMOB.tell(rejuvedMOB,null,"You are being resusitated.");
							if(rejuvedMOB.location()!=mob.location())
							{
								rejuvedMOB.location().delInhabitant(rejuvedMOB);
								rejuvedMOB.location().showOthers(rejuvedMOB,null,Affect.VISUAL_WNOISE,"<S-NAME> disappears!");
								mob.location().addInhabitant(rejuvedMOB);
								rejuvedMOB.setLocation(mob.location());
							}
							int it=0;
							while(it<mob.location().numItems())
							{
								Item item=mob.location().fetchItem(it);
								if(item.location()==body)
								{
									FullMsg msg2=new FullMsg(mob,body,item,Affect.HANDS_GET,null,Affect.HANDS_GET,null,Affect.NO_EFFECT,null);
									rejuvedMOB.location().send(rejuvedMOB,msg2);
									it=0;
								}
								else
									it++;
							}
							body.destroyThis();
							mob.location().recoverRoomStats();
							foundOne=true;
							rejuvedMOB.location().show(rejuvedMOB,null,Affect.VISUAL_WNOISE,"<S-NAME> gets up!");
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
			mob.location().show(mob,null,Affect.SOUND_NOISE,"<S-NAME> hit(s) a foul note.");

		return success;
	}
}
