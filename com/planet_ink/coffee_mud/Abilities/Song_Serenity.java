package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import java.util.*;


public class Song_Serenity extends Song
{

	public Song_Serenity()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Serenity";
		displayText="(Song of Serenity)";
		miscText="";

		canBeUninvoked=true;
		isAutoinvoked=false;

		skipStandardSongInvoke=true;
		mindAttack=true;

		baseEnvStats().setLevel(9);

		addQualifyingClass(new Bard().ID(),9);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Song_Serenity();
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
				invoker=mob;
				Ability newOne=(Ability)this.copyOf();

				for(int m=0;m<mob.location().numInhabitants();m++)
				{
					MOB roomMOB=mob.location().fetchInhabitant(m);
					if(Sense.canBeHeardBy(invoker,roomMOB))
						roomMOB.makePeace();
				}
			}
		}
		else
			mob.location().show(mob,null,Affect.SOUND_NOISE,"<S-NAME> hit(s) a foul note.");

		return success;
	}
}
