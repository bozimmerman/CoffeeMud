package com.planet_ink.coffee_mud.Abilities.Songs;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Song extends StdAbility
{
	public String ID() { return "Song"; }
	public String name(){ return "a Song";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	private static final String[] triggerStrings = {"SING","SI"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.SONG;}
	public int maxRange(){return 2;}

	protected boolean skipStandardSongInvoke(){return false;}
	protected boolean mindAttack(){return false;}
	protected boolean skipStandardSongTick(){return false;}

	public Song referenceSong=null;

	protected int affectType(boolean auto){
		int affectType=Affect.MSG_CAST_VERBAL_SPELL;
		if(quality()==Ability.MALICIOUS)
			affectType=Affect.MSG_CAST_ATTACK_VERBAL_SPELL;
		if(auto) affectType=affectType|Affect.MASK_GENERAL;
		return affectType;
	}

	public Environmental newInstance(){	return new Song();}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;

		if(skipStandardSongTick())
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
		{
			unsing(mob);
			return false;
		}
		return true;
	}

	protected void unsing(MOB mob)
	{
		if(mob==null) return;
		for(int a=mob.numAffects()-1;a>=0;a--)
		{
			Ability A=(Ability)mob.fetchAffect(a);
			if((A!=null)&&(A instanceof Song))
				A.unInvoke();
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		if(skipStandardSongInvoke())
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
			String str=auto?"^SThe song of "+displayName()+" begins to play!^?":"^S<S-NAME> begin(s) to sing the Song of "+displayName()+".^?";
			if((!auto)&&(mob.fetchAffect(this.ID())!=null))
				str="^S<S-NAME> start(s) the Song of "+displayName()+" over again.^?";

			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),str);
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				invoker=mob;
				Song newOne=(Song)this.copyOf();
				newOne.referenceSong=newOne;

				Hashtable h=ExternalPlay.properTargets(this,mob,auto);
				if(h==null) return false;
				if(h.get(mob)==null) h.put(mob,mob);

				for(Enumeration f=h.elements();f.hasMoreElements();)
				{
					MOB follower=(MOB)f.nextElement();

					// malicious songs must not affect the invoker!
					int affectType=Affect.MSG_CAST_VERBAL_SPELL;
					if(auto) affectType=affectType|Affect.MASK_GENERAL;
					if((quality()==Ability.MALICIOUS)&&(follower!=mob))
						affectType=affectType|Affect.MASK_MALICIOUS;

					if((Sense.canBeHeardBy(invoker,follower)&&(follower.fetchAffect(this.ID())==null)))
					{
						FullMsg msg2=new FullMsg(mob,follower,this,affectType,null);
						FullMsg msg3=msg2;
						if((mindAttack())&&(follower!=mob))
							msg2=new FullMsg(mob,follower,this,Affect.MSK_CAST_MALICIOUS_VERBAL|Affect.TYP_MIND|(auto?Affect.MASK_GENERAL:0),null);
						if((mob.location().okAffect(mob,msg2))&&(mob.location().okAffect(mob,msg3)))
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
