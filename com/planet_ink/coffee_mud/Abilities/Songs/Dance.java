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
	public int usageType(){return USAGE_MOVEMENT;}
	public int maxRange(){return 2;}
	protected int invokerManaCost=-1;

	protected boolean skipStandardDanceInvoke(){return false;}
	protected boolean mindAttack(){return quality()==Ability.MALICIOUS;}
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
		int affectType=CMMsg.MASK_MAGIC|CMMsg.MSG_CAST_SOMANTIC_SPELL;
		if(quality()==Ability.MALICIOUS)
			affectType=affectType|CMMsg.MASK_MALICIOUS;
		if(auto) affectType=affectType|CMMsg.MASK_GENERAL;
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
				undance(mob,null,this);
				return false;
			}
			if(invokerManaCost<0) invokerManaCost=usageCost(invoker())[1];
			if(!mob.curState().adjMovement(-(invokerManaCost/15),mob.maxState()))
			{
				mob.tell("The dancing exhausts you.");
				undance(mob,null,this);
				return false;
			}
		}
		return true;
	}

	protected void undance(MOB mob, MOB invoker, Ability song)
	{
		if(mob==null) return;
		if(song!=null)
		{
			song=mob.fetchEffect(song.ID());
			if(song!=null) song.unInvoke();
		}
		else
		for(int a=mob.numEffects()-1;a>=0;a--)
		{
			Ability A=(Ability)mob.fetchEffect(a);
			if((A!=null)
			&&(A instanceof Dance)
			&&((invoker==null)||(A.invoker()==null)||(A.invoker()==invoker)))
				A.unInvoke();
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		if((!auto)
		&&(!mob.isMonster())
		&&(CMAble.getQualifyingLevel(mob.charStats().getCurrentClass().ID(),ID())<0)
		&&(!CoffeeUtensils.armorCheck(mob,CharClass.ARMOR_LEATHER))
		&&(mob.isMine(this))
		&&(mob.location()!=null)
		&&(Dice.rollPercentage()<50))
		{
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> fumble(s) the "+name()+" due to <S-HIS-HER> armor!");
			return false;
		}

		if(skipStandardDanceInvoke())
			return true;

		if((!auto)&&(!Sense.aliveAwakeMobile(mob,false)))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);
		undance(mob,null,null);
		if(success)
		{
			String str=auto?"^SThe "+danceOf()+" begins!^?":"^S<S-NAME> begin(s) to dance the "+danceOf()+".^?";
			if((!auto)&&(mob.fetchEffect(this.ID())!=null))
				str="^S<S-NAME> start(s) the "+danceOf()+" over again.^?";

			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),str);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				invoker=mob;
				Dance newOne=(Dance)this.copyOf();
				newOne.invoker=mob;
				newOne.referenceDance=newOne;
				newOne.invokerManaCost=-1;

				Hashtable h=properTargets(mob,givenTarget,auto);
				if(h==null) return false;
				if(h.get(mob)==null) h.put(mob,mob);

				Room R=mob.location();
				for(Enumeration f=h.elements();f.hasMoreElements();)
				{
					MOB follower=(MOB)f.nextElement();
					Room R2=follower.location();

					// malicious dances must not affect the invoker!
					int affectType=CMMsg.MSG_CAST_SOMANTIC_SPELL;
					if((quality()==Ability.MALICIOUS)&&(follower!=mob))
						affectType=affectType|CMMsg.MASK_MALICIOUS;
					if(auto) affectType=affectType|CMMsg.MASK_GENERAL;

					if((R!=null)&&(R2!=null)&&(Sense.canBeSeenBy(invoker,follower)&&(follower.fetchEffect(this.ID())==null)))
					{
						FullMsg msg2=new FullMsg(mob,follower,this,affectType,null);
						FullMsg msg3=msg2;
						if((mindAttack())&&(follower!=mob))
							msg2=new FullMsg(mob,follower,this,CMMsg.MSK_CAST_MALICIOUS_SOMANTIC|CMMsg.TYP_MIND|(auto?CMMsg.MASK_GENERAL:0),null);
						if((R.okMessage(mob,msg2))&&(R.okMessage(mob,msg3)))
						{
							R2.send(follower,msg2);
							if(msg2.value()<=0)
							{
								R2.send(follower,msg3);
								if((msg3.value()<=0)&&(follower.fetchEffect(newOne.ID())==null))
								{
									undance(follower,null,null);
									if(follower!=mob)
										follower.addEffect((Ability)newOne.copyOf());
									else
										follower.addEffect(newOne);
								}
							}
						}
					}
				}
				R.recoverRoomStats();
			}
		}
		else
			mob.location().show(mob,null,CMMsg.MSG_NOISE,"<S-NAME> make(s) a false step.");

		return success;
	}
}
