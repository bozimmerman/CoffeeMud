package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
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
		quality=Ability.OK_OTHERS;

		skipStandardSongInvoke=true;

		baseEnvStats().setLevel(16);

		addQualifyingClass("Bard",16);

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
		MOB newMOB=(MOB)CMClass.getMOB("StdMOB").newInstance();
		Vector offenders=new Vector();

		FullMsg msg=new FullMsg(newMOB,newMOB,null,Affect.MSG_SPEAK,null);
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
				  &&(A.invoker().envStats().level()<=caster.envStats().level()+1)))
					offenders.addElement(A);
		}
		return offenders;
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
		if(success)
		{
			String str=auto?"The song of "+name()+" begins to play.":"<S-NAME> begin(s) to sing the Song of "+name()+".";
			if((!auto)&&(mob.fetchAffect(this.ID())!=null))
				str="<S-NAME> start(s) the Song of "+name()+" over again.";
			unsing(mob);

			FullMsg msg=new FullMsg(mob,null,this,affectType,str);
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				invoker=mob;

				Hashtable h=ExternalPlay.properTargets(this,mob);
				if(h==null) return false;

				// malicious songs must not affect the invoker!
				if(h.get(mob)==null) h.put(mob,mob);

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
			mob.location().show(mob,null,Affect.MSG_NOISE,"<S-NAME> hit(s) a foul note.");

		return success;
	}
}