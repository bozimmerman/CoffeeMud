package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.MOBS.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import java.util.*;


public class Song_Voices extends Song
{

	public Song_Voices()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Voices";
		displayText="(Song of Voices)";
		miscText="";

		canBeUninvoked=true;
		isAutoinvoked=false;

		skipStandardSongInvoke=true;

		baseEnvStats().setLevel(16);

		addQualifyingClass(new Bard().ID(),16);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Song_Voices();
	}

	public Vector returnOffensiveAffects(MOB caster, Environmental fromMe)
	{
		MOB newMOB=new StdMOB();
		Vector offenders=new Vector();

		FullMsg msg=new FullMsg(newMOB,newMOB,null,Affect.SOUND_WORDS,"blah",Affect.SOUND_WORDS,"blah",Affect.SOUND_WORDS,"blah");
		for(int a=0;a<fromMe.numAffects();a++)
		{
			Ability A=fromMe.fetchAffect(a);
			newMOB.recoverEnvStats();
			A.affectEnvStats(newMOB,newMOB.envStats());
			if((!Sense.canHear(newMOB))
			   ||(!Sense.canSpeak(newMOB))
			   ||(!A.okAffect(msg)))
			if((A.invoker()==null)
			   ||((A.invoker()!=null)
				  &&(A.invoker().envStats().level()<caster.envStats().level())))
					offenders.addElement(A);
		}
		return offenders;
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

				Room thisRoom=mob.location();

				Vector V=returnOffensiveAffects(mob,thisRoom);
				for(int v=0;v<V.size();v++)
				{
					Ability A=(Ability)V.elementAt(v);
					A.unInvoke();
				}

				for(Enumeration f=h.elements();f.hasMoreElements();)
				{
					MOB follower=(MOB)f.nextElement();
					Vector V2=returnOffensiveAffects(mob,follower);
					for(int v=0;v<V2.size();v++)
					{
						Ability A=(Ability)V2.elementAt(v);
						A.unInvoke();
					}
				}
			}
		}
		else
			mob.location().show(mob,null,Affect.SOUND_NOISE,"<S-NAME> hit(s) a foul note.");

		return success;
	}
}