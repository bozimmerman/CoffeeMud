package com.planet_ink.coffee_mud.Abilities.Songs;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Song extends StdAbility
{

	boolean skipStandardSongInvoke=false;
	boolean mindAttack=false;
	boolean skipStandardSongTick=false;

	public Song referenceSong=null;

	protected int affectType=Affect.MSG_CAST_VERBAL_SPELL;

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
		maxRange=1;
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

		if(skipStandardSongTick)
			return true;
		
		MOB mob=(MOB)affected;
		if(mob==null)
			return false;

		if((invoker==null)
		||(referenceSong==null)
		||(referenceSong.affected==null)
		||(referenceSong.invoker==null)
		||(invoker.location()!=mob.location())
		||(Sense.isSleeping(invoker))
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
			if((A!=null)&&(A instanceof Song))
				A.unInvoke();
			if(mob.numAffects()==n)
				a++;
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		affectType=Affect.MSG_CAST_VERBAL_SPELL;
		if(quality()==Ability.MALICIOUS)
			affectType=Affect.MSG_CAST_ATTACK_VERBAL_SPELL;
		if(auto) affectType=affectType|Affect.ACT_GENERAL;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		if(skipStandardSongInvoke)
			return true;

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
				invoker=mob;
				Song newOne=(Song)this.copyOf();
				newOne.referenceSong=newOne;

				Hashtable h=ExternalPlay.properTargets(this,mob);
				if(h==null) return false;
				if(h.get(mob)==null) h.put(mob,mob);

				for(Enumeration f=h.elements();f.hasMoreElements();)
				{
					MOB follower=(MOB)f.nextElement();

					// malicious songs must not affect the invoker!
					affectType=Affect.MSG_CAST_VERBAL_SPELL;
					if(auto) affectType=affectType|Affect.ACT_GENERAL;
					if((quality==Ability.MALICIOUS)&&(follower!=mob))
						affectType=affectType|Affect.MASK_MALICIOUS;

					if((Sense.canBeHeardBy(invoker,follower)&&(follower.fetchAffect(this.ID())==null)))
					{
						FullMsg msg2=new FullMsg(mob,follower,this,affectType,null);
						FullMsg msg3=msg2;
						if((mindAttack)&&(follower!=mob))
							msg2=new FullMsg(mob,follower,this,Affect.MSK_CAST_MALICIOUS_VERBAL|Affect.TYP_MIND,null);
						if((mob.location().okAffect(msg2))&&(mob.location().okAffect(msg3)))
						{
							follower.location().send(follower,msg2);
							if(!msg2.wasModified())
							{
								follower.location().send(follower,msg3);
								if((!msg3.wasModified())&&(follower.fetchAffect(newOne.ID())==null))
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
			mob.location().show(mob,null,Affect.MSG_NOISE,"<S-NAME> hit(s) a foul note.");

		return success;
	}
}
