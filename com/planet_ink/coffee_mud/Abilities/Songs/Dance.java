package com.planet_ink.coffee_mud.Abilities.Songs;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Dance extends StdAbility
{
	public String ID() { return "Dance"; }
	public String name(){ return "a Dance";}
	public String displayText(){ return "("+danceOf()+")";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	private static final String[] triggerStrings = {"DANCE","DA"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.SONG;}
	public int maxRange(){return 2;}
	protected int invokerManaCost=-1;

	protected boolean skipStandardDanceInvoke(){return false;}
	protected boolean mindAttack(){return false;}
	protected boolean skipStandardDanceTick(){return false;}
	protected String danceOf(){return name();}

	public Dance referenceDance=null;
	
	public int prancerLevel()
	{
		if(invoker()==null) return CMAble.lowestQualifyingLevel(ID());
		int x=CMAble.qualifyingClassLevel(invoker(),this);
		if(x<=0) x=CMAble.lowestQualifyingLevel(ID());
		int charisma=(invoker().charStats().getStat(CharStats.CHARISMA)-10);
		if(charisma>10)
			return x+((charisma-10)/3);
		return x;
	}

	protected int affectType(boolean auto){
		int affectType=Affect.MASK_MAGIC|Affect.MSG_DELICATE_HANDS_ACT;
		if(quality()==Ability.MALICIOUS)
			affectType=affectType|Affect.MASK_MALICIOUS;
		if(auto) affectType=affectType|Affect.MASK_GENERAL;
		return affectType;
	}

	public Environmental newInstance(){	return new Dance();}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;

		if(skipStandardDanceTick())
			return true;

		if(affected==null) return false;
		if(affected instanceof MOB)
		{
			MOB mob=(MOB)affected;
			if((invoker==null)
			||(referenceDance==null)
			||(referenceDance.affected==null)
			||(referenceDance.invoker==null)
			||(invoker.location()!=mob.location())
			||(!Sense.aliveAwakeMobile(mob,true))
			||(!Sense.aliveAwakeMobile(invoker(),true))
			||(!Sense.canBeSeenBy(invoker,mob)))
			{
				undance(mob);
				return false;
			}
			if(invokerManaCost<0) invokerManaCost=manaCost(invoker());
			if(!mob.curState().adjMovement(-(invokerManaCost/15),mob.maxState()))
			{
				mob.tell("The dancing exhausts you.");
				undance(mob);
				return false;
			}
		}
		return true;
	}

	protected void undance(MOB mob)
	{
		if(mob==null) return;
		for(int a=mob.numAffects()-1;a>=0;a--)
		{
			Ability A=(Ability)mob.fetchAffect(a);
			if((A!=null)&&(A instanceof Dance))
				A.unInvoke();
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		if(skipStandardDanceInvoke())
			return true;

		if((!auto)&&(!Sense.aliveAwakeMobile(mob,false)))
			return false;

		boolean success=profficiencyCheck(0,auto);
		undance(mob);
		if(success)
		{
			String str=auto?"^SThe "+danceOf()+" begins!^?":"^S<S-NAME> begin(s) to dance the "+danceOf()+".^?";
			if((!auto)&&(mob.fetchAffect(this.ID())!=null))
				str="^S<S-NAME> start(s) the "+danceOf()+" over again.^?";

			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),str);
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				invoker=mob;
				Dance newOne=(Dance)this.copyOf();
				newOne.referenceDance=newOne;
				newOne.invokerManaCost=-1;

				Hashtable h=ExternalPlay.properTargets(this,mob,auto);
				if(h==null) return false;
				if(h.get(mob)==null) h.put(mob,mob);

				for(Enumeration f=h.elements();f.hasMoreElements();)
				{
					MOB follower=(MOB)f.nextElement();

					// malicious dances must not affect the invoker!
					int affectType=Affect.MASK_MAGIC|Affect.MSG_DELICATE_HANDS_ACT;
					if((quality()==Ability.MALICIOUS)&&(follower!=mob))
						affectType=affectType|Affect.MASK_MALICIOUS;
					if(auto) affectType=affectType|Affect.MASK_GENERAL;

					if((Sense.canBeSeenBy(invoker,follower)&&(follower.fetchAffect(this.ID())==null)))
					{
						FullMsg msg2=new FullMsg(mob,follower,this,affectType,null);
						FullMsg msg3=msg2;
						if((mindAttack())&&(follower!=mob))
							msg2=new FullMsg(mob,follower,this,Affect.MSK_CAST_MALICIOUS_SOMANTIC|Affect.TYP_MIND|(auto?Affect.MASK_GENERAL:0),null);
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
			mob.location().show(mob,null,Affect.MSG_NOISE,"<S-NAME> make(s) a false step.");

		return success;
	}
}
