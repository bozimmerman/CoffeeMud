package com.planet_ink.coffee_mud.Abilities.Songs;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Play extends StdAbility
{
	public String ID() { return "Play"; }
	public String name(){ return "a song played";}
	public String displayText(){ return "("+songOf()+")";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	private static final String[] triggerStrings = {"PLAY","PL","PLA"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.SONG;}
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}
	public int maxRange(){return 2;}
	
	protected int requiredInstrumentType(){return -1;}
	protected boolean skipStandardSongInvoke(){return false;}
	protected boolean mindAttack(){return quality()==Ability.MALICIOUS;}
	protected boolean skipStandardSongTick(){return false;}
	protected boolean persistantSong(){return true;}
	protected String songOf(){return name();}

	public Play referencePlay=null;
	protected MusicalInstrument instrument=null;

	protected int affectType(boolean auto){
		int affectType=Affect.MSG_CAST_SOMANTIC_SPELL;
		if(quality()==Ability.MALICIOUS)
			affectType=Affect.MSG_CAST_ATTACK_SOMANTIC_SPELL;
		if(auto) affectType=affectType|Affect.MASK_GENERAL;
		return affectType;
	}

	public Environmental newInstance(){	return new Play();}
	public String instrumentName(){
		if(instrument!=null) return instrument.name();
		return "something";
	}

	public int invokerLevel()
	{
		if(invoker()!=null)
		{
			if(instrument!=null)
				return invoker().envStats().level()+instrument.envStats().ability();
			else
				return invoker().envStats().level();
		}
		else
		if(affected!=null)
			return affected.envStats().level();
		else
			return 1;
	}
	
	protected void inpersistantAffect(MOB mob)
	{
	}
	
	public static boolean usingInstrument(MusicalInstrument I, MOB mob)
	{
		if((I==null)||(mob==null)) return false;
		if(I instanceof Rideable)
			return (((Rideable)I).amRiding(mob)
					&&(mob.fetchFirstWornItem(Item.WIELD)==null)
					&&(mob.fetchFirstWornItem(Item.HELD)==null));
		else
			return mob.isMine(I)&&(!I.amWearingAt(Item.INVENTORY));
	}
	
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
		||(referencePlay==null)
		||(referencePlay.affected==null)
		||(referencePlay.invoker==null)
		||((instrument!=null)&&(!usingInstrument(instrument,invoker)))
		||(invoker.location()!=mob.location())
		||(!Sense.aliveAwakeMobile(invoker,true))
		||(!Sense.canBeHeardBy(invoker,mob)))
		{
			unplay(mob,null,this);
			return false;
		}
		return true;
	}

	public void affect(Environmental host, Affect msg)
	{
		super.affect(host,msg);
		if((affected==invoker)
		&&(msg.amISource(invoker))
		&&(!unInvoked)
		&&(instrument!=null))
		{
			if((msg.sourceMinor()==Affect.TYP_SPEAK)
			&&(instrument.amWearingAt(Item.ON_MOUTH)))
				unInvoke();
			else
			if(((msg.sourceMinor()==Affect.TYP_REMOVE)
			   ||(msg.sourceMinor()==Affect.TYP_WEAR)
			   ||(msg.sourceMinor()==Affect.TYP_WIELD))
			&&(instrument.amWearingAt(Item.HELD)))
				unInvoke();
		}
	}

	protected static void unplay(MOB mob, MOB invoker, Ability song)
	{
		if(mob==null) return;
		if(song!=null)
		{
			song=mob.fetchAffect(song.ID());
			if(song!=null) song.unInvoke();
		}
		else
		for(int a=mob.numAffects()-1;a>=0;a--)
		{
			Ability A=(Ability)mob.fetchAffect(a);
			if(((A!=null)&&(A instanceof Play))
			&&((invoker==null)||(A.invoker()==null)||(A.invoker()==invoker)))
				A.unInvoke();
		}
	}
	
	public static MusicalInstrument getInstrument(MOB mob, int requiredInstrumentType, boolean noisy)
	{
		MusicalInstrument instrument=null;
		if((mob.riding()!=null)&&(mob.riding() instanceof MusicalInstrument))
		{
			if(!usingInstrument((MusicalInstrument)mob.riding(),mob))
			{
				if(noisy)
					mob.tell("You need to free your hands to play "+mob.riding().name()+".");
				return null;
			}
			instrument=(MusicalInstrument)mob.riding();
		}
		if(instrument==null)
		for(int i=0;i<mob.inventorySize();i++)
		{
			Item I=mob.fetchInventory(i);
			if((I!=null)
			&&(I instanceof MusicalInstrument)
			&&(I.container()==null)
			&&(usingInstrument((MusicalInstrument)I,mob)))
			{ instrument=(MusicalInstrument)I; break;}
		}
		if(instrument==null)
		{
			if(noisy)
				mob.tell("You need an instrument!");
			return null;
		}
		if((requiredInstrumentType>=0)&&(instrument.instrumentType()!=requiredInstrumentType))
		{
			if(noisy)
				mob.tell("This song can only be played on "+MusicalInstrument.TYPE_DESC[requiredInstrumentType].toLowerCase()+".");
			return null;
		}
		return instrument;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!auto)
		{
			instrument=getInstrument(mob,requiredInstrumentType(),true);
			if(instrument==null) return false;
			if((mob.riding()!=null)&&(mob.riding() instanceof MusicalInstrument))
			{
				if(!usingInstrument((MusicalInstrument)mob.riding(),mob))
				{
					mob.tell("You need to free your hands to play "+mob.riding().name()+".");
					return false;
				}
				instrument=(MusicalInstrument)mob.riding();
			}
			if(instrument==null)
			for(int i=0;i<mob.inventorySize();i++)
			{
				Item I=mob.fetchInventory(i);
				if((I!=null)
				&&(I instanceof MusicalInstrument)
				&&(I.container()==null)
				&&(usingInstrument((MusicalInstrument)I,mob)))
				{ instrument=(MusicalInstrument)I; break;}
			}
			if(instrument==null)
			{
				mob.tell("You need an instrument!");
				return false;
			}
			if((requiredInstrumentType()>=0)&&(instrument.instrumentType()!=requiredInstrumentType()))
			{
				mob.tell("This song can only be played on "+MusicalInstrument.TYPE_DESC[requiredInstrumentType()].toLowerCase()+".");
				return false;
			}
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		if(skipStandardSongInvoke())
			return true;

		if((!auto)&&(!Sense.aliveAwakeMobile(mob,false)))
			return false;

		boolean success=profficiencyCheck(0,auto);
		unplay(mob,mob,null);
		if(success)
		{
			String str=auto?"^S"+songOf()+" begins to play!^?":"^S<S-NAME> begin(s) to play "+songOf()+" on "+instrumentName()+".^?";
			if((!auto)&&(mob.fetchAffect(this.ID())!=null))
				str="^S<S-NAME> start(s) playing "+songOf()+" on "+instrumentName()+" again.^?";

			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),str);
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				invoker=mob;
				Play newOne=(Play)this.copyOf();
				newOne.referencePlay=newOne;

				Hashtable h=properTargets(mob,givenTarget,auto);
				if(h==null) return false;
				if(h.get(mob)==null) h.put(mob,mob);

				for(Enumeration f=h.elements();f.hasMoreElements();)
				{
					MOB follower=(MOB)f.nextElement();

					// malicious songs must not affect the invoker!
					int affectType=Affect.MSG_CAST_SOMANTIC_SPELL;
					if(auto) affectType=affectType|Affect.MASK_GENERAL;
					if((quality()==Ability.MALICIOUS)&&(follower!=mob))
						affectType=affectType|Affect.MASK_MALICIOUS;

					if((Sense.canBeHeardBy(invoker,follower)&&(follower.fetchAffect(this.ID())==null)))
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
									if(persistantSong())
									{
										if(follower!=mob)
											follower.addAffect((Ability)newOne.copyOf());
										else
											follower.addAffect(newOne);
									}
									else
										inpersistantAffect(follower);
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