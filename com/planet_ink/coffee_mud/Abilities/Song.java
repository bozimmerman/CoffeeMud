package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import java.util.*;

public class Song extends StdAbility
{

	boolean skipStandardSongInvoke=false;
	boolean mindAttack=false;

	public Song referenceSong=null;

	public Song()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a Song";
		displayText="(in a musical home and hearth)";
		miscText="";
		triggerStrings.addElement("SING");
		triggerStrings.addElement("SI");

		canBeUninvoked=true;
		isAutoinvoked=false;
	}

	public int classificationCode()
	{
		return Ability.SONG;
	}

	public Environmental newInstance()
	{
		return new Song();
	}

	public boolean tick(int tickID)
	{
		if(!super.tick(tickID))
			return false;

		MOB mob=(MOB)affected;
		if(mob==null)
			return false;

		if((invoker==null)
		||(referenceSong==null)
		||(referenceSong.affected==null)
		||(referenceSong.invoker==null)
		||(invoker.location()!=mob.location())
		||(!Sense.canBeHeardBy(invoker,mob)))
			return false;
		return true;
	}

	protected void unsing(MOB mob)
	{
		int a=0;
		while(a<mob.numAffects())
		{
			int n=mob.numAffects();
			Ability A=(Ability)mob.fetchAffect(a);
			if(A instanceof Song)
				A.unInvoke();
			if(mob.numAffects()==n)
				a++;
		}
	}

	public boolean invoke(MOB mob, Vector commands)
	{
		if(!super.invoke(mob,commands))
			return false;

		if(skipStandardSongInvoke)
			return true;

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

				if(h.get(mob.ID())==null) h.put(mob.ID(),mob);


				for(Enumeration f=h.elements();f.hasMoreElements();)
				{
					MOB follower=(MOB)f.nextElement();

					// malicious songs must not affect the invoker!
					int targetCode=Affect.SOUND_MAGIC;
					if((malicious)&&(follower!=mob))
						targetCode=Affect.STRIKE_MAGIC;
					else
					if(follower==mob)
						targetCode=Affect.VISUAL_WNOISE;
					if((Sense.canBeHeardBy(invoker,follower)&&(follower.fetchAffect(this.ID())==null)))
					{
						FullMsg msg2=new FullMsg(mob,follower,this,Affect.NO_EFFECT,targetCode,Affect.NO_EFFECT,null);
						FullMsg msg3=msg2;
						if((mindAttack)&&(follower!=mob))
							msg2=new FullMsg(mob,follower,this,Affect.NO_EFFECT,Affect.STRIKE_MIND,Affect.NO_EFFECT,null);
						if((mob.location().okAffect(msg2))&&(mob.location().okAffect(msg3)))
						{
							follower.location().send(follower,msg2);
							if(!msg2.wasModified())
							{
								follower.location().send(follower,msg3);
								if(!msg3.wasModified())
								{
									if(follower!=mob)
										follower.addAffect((Ability)newOne.copyOf());
									else
										follower.addAffect(newOne);
								}
							}
						}
					}
				}
				mob.location().recoverRoomStats();
			}
		}
		else
			mob.location().show(mob,null,Affect.SOUND_NOISE,"<S-NAME> hit(s) a foul note.");

		return success;
	}
}
