package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import java.util.*;


public class Song_Friendship extends Song
{

	public Song_Friendship()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Friendship";
		displayText="(Song of Friendship)";
		miscText="";

		canBeUninvoked=true;
		isAutoinvoked=false;

		skipStandardSongInvoke=true;
		malicious=true;

		baseEnvStats().setLevel(19);

		addQualifyingClass(new Bard().ID(),19);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Song_Friendship();
	}

	public boolean tick(int tickID)
	{
		if(!super.tick(tickID))
			return false;

		MOB mob=(MOB)affected;
		if(mob==null) return false;
		if(mob==invoker) return true;
		if(mob.amFollowing()!=invoker)
			return false;
		return true;
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
				Song newOne=(Song)this.copyOf();
				newOne.referenceSong=newOne;

				Hashtable h=null;
				if(!malicious)
					h=Grouping.getAllFollowers(mob);
				else
				if(mob.isInCombat())
					h=TheFight.allCombatants(mob);
				else
					h=TheFight.allPossibleCombatants(mob);

				if(h==null)
					return false;

				// malicious songs must not affect the invoker!
				if(h.get(mob.ID())==null) h.put(mob.ID(),mob);

				for(Enumeration f=h.elements();f.hasMoreElements();)
				{
					MOB follower=(MOB)f.nextElement();
					int targetCode=Affect.SOUND_MAGIC;
					if((malicious)&&(follower!=mob))
						targetCode=Affect.STRIKE_MAGIC;

					if((Sense.canBeHeardBy(invoker,follower)&&(follower.fetchAffect(this.ID())==null)))
					{
						FullMsg msg2=new FullMsg(mob,follower,this,Affect.NO_EFFECT,targetCode,Affect.NO_EFFECT,null);
						FullMsg msg3=msg2;
						if((mindAttack)&&(follower!=mob))
							msg2=new FullMsg(mob,follower,this,Affect.NO_EFFECT,Affect.STRIKE_MIND,Affect.NO_EFFECT,null);
						int levelDiff=follower.envStats().level()-mob.envStats().level();

						if((levelDiff>10)&&(mindAttack))
							mob.tell(mob,follower,"<T-NAME> looks too powerful.");
						else
						if((mob.location().okAffect(msg2))&&(mob.location().okAffect(msg3)))
						{
							mob.location().send(mob,msg2);
							if(!msg2.wasModified())
							{
								mob.location().send(mob,msg3);
								if(!msg3.wasModified())
								{
									if((follower.amFollowing()!=mob)&&(follower!=mob))
									{
										follower.location().show(follower,mob,Affect.VISUAL_WNOISE,"<S-NAME> follow(s) <T-NAME>.");
										follower.setFollowing(mob);
									}
									if(follower!=mob)
										follower.addAffect((Ability)newOne.copyOf());
									else
										follower.addAffect(newOne);
								}
							}
						}
					}
				}
			}
		}
		else
			mob.location().show(mob,null,Affect.SOUND_NOISE,"<S-NAME> hit(s) a foul note.");

		return success;
	}
}