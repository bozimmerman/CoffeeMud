package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class StdAbility implements Ability, Cloneable
{
	public String ID() { return "StdAbility"; }
	public String name(){ return "an ability";}
	public String description(){return "&";}
	public String displayText(){return "What they see when affected.";}
	public static final String[] empty={};
	public String[] triggerStrings(){return empty;}
	public int maxRange(){return 0;}
	public int minRange(){return 0;}
	public boolean putInCommandlist(){return true;}
	public boolean isAutoInvoked(){return false;}
	public boolean bubbleAffect(){return false;}
	protected int trainsRequired(){return 1;}
	protected int practicesRequired(){return 0;}
	protected int practicesToPractice(){return 1;}
	protected int overrideMana(){return -1;}
	public int quality(){return Ability.INDIFFERENT;}
	protected int canAffectCode(){return Ability.CAN_AREAS|
										 Ability.CAN_ITEMS|
										 Ability.CAN_MOBS|
										 Ability.CAN_ROOMS|
										 Ability.CAN_EXITS;}
	protected int canTargetCode(){return Ability.CAN_AREAS|
										 Ability.CAN_ITEMS|
										 Ability.CAN_MOBS|
										 Ability.CAN_ROOMS|
										 Ability.CAN_EXITS;}

	protected boolean isAnAutoEffect=false;
	protected int profficiency=0;
	protected boolean borrowed=false;
	public String miscText="";
	protected MOB invoker=null;
	protected Environmental affected=null;
	protected boolean canBeUninvoked=true;
	protected boolean unInvoked=false;
	protected int tickDown=-1;
	protected long lastProfHelp=0;

	public StdAbility()
	{
	}

	public Environmental newInstance()	{ return new StdAbility(); }
	public int classificationCode(){ return Ability.SKILL; }

	protected static final EnvStats envStats=new DefaultEnvStats();
	public EnvStats envStats(){return envStats;}
	public EnvStats baseEnvStats(){return envStats;}

	public boolean isNowAnAutoEffect(){ return isAnAutoEffect; }
	public boolean isBorrowed(Environmental toMe){ return borrowed;	}
	public void setBorrowed(Environmental toMe, boolean truefalse) { borrowed=truefalse; }
	public void setName(String newName){}
	public void recoverEnvStats() {}
	public void setBaseEnvStats(EnvStats newBaseEnvStats){}
	public void setDisplayText(String newDisplayText){}
	public void setDescription(String newDescription){}
	public int abilityCode(){return 0;}
	public void setAbilityCode(int newCode){}

	// ** For most abilities, the following stuff actually matters */
	public void setMiscText(String newMiscText)	{ miscText=newMiscText;}
	public String text(){ return miscText;}
	public int profficiency(){ return profficiency;}
	public void setProfficiency(int newProfficiency)
	{
		profficiency=newProfficiency;
		if(profficiency>100) profficiency=100;
	}

	public void startTickDown(MOB invokerMOB, Environmental affected, int tickTime)
	{
		if(invokerMOB!=null) invoker=invokerMOB;
		
		borrowed=true; // makes it so that the affect does not save!
		
		if(invoker()!=null)
			tickTime=invoker().charStats().getCurrentClass().classDurationModifier(invoker(),this,tickTime);
		if(affected instanceof MOB)
		{
			if(((MOB)affected).location()==null) return;
			if(affected.fetchAffect(this.ID())==null) affected.addAffect(this);
			((MOB)affected).location().recoverRoomStats();
			if(invoker()!=affected)
				tickTime=((MOB)affected).charStats().getCurrentClass().classDurationModifier(((MOB)affected),this,tickTime);
		}
		else
		{
			if(affected.fetchAffect(this.ID())==null)
				affected.addAffect(this);

			if(affected instanceof Room)
				((Room)affected).recoverRoomStats();
			else
				affected.recoverEnvStats();
			ExternalPlay.startTickDown(this,Host.MOB_TICK,1);
		}
		tickDown=tickTime;
	}

	public int adjustedLevel(MOB caster)
	{
		if(caster==null) return 1;
		int lowestQualifyingLevel=CMAble.lowestQualifyingLevel(this.ID());
		int adjLevel=lowestQualifyingLevel;
		int qualifyingLevel=CMAble.qualifyingLevel(caster,this);
		if((caster.isMonster())||(qualifyingLevel>=0))
			adjLevel+=(CMAble.qualifyingClassLevel(caster,this)-qualifyingLevel);
		else
		{
			adjLevel=caster.envStats().level()-lowestQualifyingLevel-25;
			if(adjLevel<lowestQualifyingLevel)
				adjLevel=lowestQualifyingLevel;
		}
		if(adjLevel<1) return 1;
		return adjLevel;
	}

	public boolean canAffect(Environmental E)
	{
		if((E==null)&&(canAffectCode()==0)) return true;
		if(E==null) return false;
		if((E instanceof MOB)&&((canAffectCode()&Ability.CAN_MOBS)>0)) return true;
		if((E instanceof Item)&&((canAffectCode()&Ability.CAN_ITEMS)>0)) return true;
		if((E instanceof Exit)&&((canAffectCode()&Ability.CAN_EXITS)>0)) return true;
		if((E instanceof Room)&&((canAffectCode()&Ability.CAN_ROOMS)>0)) return true;
		if((E instanceof Area)&&((canAffectCode()&Ability.CAN_AREAS)>0)) return true;
		return false;
	}

	public boolean canTarget(Environmental E)
	{
		if((E==null)&&(canTargetCode()==0)) return true;
		if(E==null) return false;
		if((E instanceof MOB)&&((canTargetCode()&Ability.CAN_MOBS)>0)) return true;
		if((E instanceof Item)&&((canTargetCode()&Ability.CAN_ITEMS)>0)) return true;
		if((E instanceof Room)&&((canTargetCode()&Ability.CAN_ROOMS)>0)) return true;
		if((E instanceof Area)&&((canTargetCode()&Ability.CAN_AREAS)>0)) return true;
		return false;
	}

	public MOB getTarget(MOB mob, Vector commands, Environmental givenTarget)
	{ return getTarget(mob,commands,givenTarget,false);	}

	public MOB getTarget(MOB mob, Vector commands, Environmental givenTarget, boolean quiet)
	{
		String targetName=Util.combine(commands,0);
		MOB target=null;
		if((givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		else
		if((targetName.length()==0)&&(mob.isInCombat())&&(quality()==Ability.MALICIOUS)&&(mob.getVictim()!=null))
		   target=mob.getVictim();
		else
		if((targetName.length()==0)&&(quality()!=Ability.MALICIOUS))
			target=mob;
		else
		if(targetName.length()>0)
		{
			target=mob.location().fetchInhabitant(targetName);
			if(target==null)
			{
				Environmental t=mob.location().fetchFromRoomFavorItems(null,targetName,Item.WORN_REQ_UNWORNONLY);
				if((t!=null)&&(!(t instanceof MOB)))
				{
					if(!quiet)
						mob.tell("You can't do that to '"+targetName+"'.");
					return null;
				}
			}
		}

		if(target!=null)
			targetName=target.name();

		if((target==null)||((!Sense.canBeSeenBy(target,mob))&&((!Sense.canBeHeardBy(target,mob))||(!target.isInCombat()))))
		{
			if(!quiet)
			{
				if(targetName.trim().length()==0)
					mob.tell("You don't see them here.");
				else
					mob.tell("You don't see '"+targetName+"' here.");
			}
			return null;
		}

		if(target.fetchAffect(this.ID())!=null)
		{
			if(!quiet)
			{
				if(target==mob)
					mob.tell("You are already affected by "+name()+".");
				else
					mob.tell(target.name()+" is already affected by "+name()+".");
			}
			return null;
		}
		return target;
	}

	public Environmental getAnyTarget(MOB mob, Vector commands, Environmental givenTarget, int wornReqCode)
	{
		String targetName=Util.combine(commands,0);
		Environmental target=null;
		if(givenTarget!=null)
			target=givenTarget;
		else
		if((targetName.length()==0)&&(mob.isInCombat())&&(quality()==Ability.MALICIOUS)&&(mob.getVictim()!=null))
			target=mob.getVictim();
		else
		{
			target=mob.location().fetchFromRoomFavorMOBs(null,targetName, wornReqCode);
			if(target==null)
				target=mob.location().fetchFromMOBRoomFavorsItems(mob,null,targetName,wornReqCode);
		}
		if(target!=null) targetName=target.name();
		if((target==null)||((!Sense.canBeSeenBy(target,mob))&&((!Sense.canBeHeardBy(target,mob))||((target instanceof MOB)&&(!((MOB)target).isInCombat())))))
		{
			if(targetName.trim().length()==0)
				mob.tell("You don't see that here.");
			else
				mob.tell("You don't see '"+targetName+"' here.");
			return null;
		}

		if(target.fetchAffect(this.ID())!=null)
		{
			if(target==mob)
				mob.tell("You are already affected by "+name()+".");
			else
				mob.tell(targetName+" is already affected by "+name()+".");
			return null;
		}
		return target;
	}

	public Item getTarget(MOB mob, Room location, Environmental givenTarget, Vector commands, int wornReqCode)
	{
		String targetName=Util.combine(commands,0);

		Environmental target=null;
		if((givenTarget!=null)&&(givenTarget instanceof Item))
			target=givenTarget;

		if(location!=null)
			target=location.fetchFromRoomFavorItems(null,targetName,wornReqCode);
		if(target==null)
		{
			if(location!=null)
				target=location.fetchFromMOBRoomFavorsItems(mob,null,targetName,wornReqCode);
			else
				target=mob.fetchCarried(null,targetName);
		}
		if(target!=null) targetName=target.name();
		if((target==null)||((target!=null)&&((!Sense.canBeSeenBy(target,mob))||(!(target instanceof Item)))))
		{
			if(targetName.length()==0)
				mob.tell("You need to be more specific.");
			else
			if((target==null)||(target instanceof Item))
			{
				if(targetName.trim().length()==0)
					mob.tell("You don't see that here.");
				else
					mob.tell("You don't see '"+targetName+"' here.");
			}
			else
				mob.tell("You can't do that to '"+targetName+"'.");
			return null;
		}
		return (Item)target;
	}


	private void cloneFix(Ability E){}
	public Environmental copyOf()
	{
		try
		{
			StdAbility E=(StdAbility)this.clone();
			E.cloneFix(this);
			return E;

		}
		catch(CloneNotSupportedException e)
		{
			return this.newInstance();
		}
	}

	public boolean profficiencyCheck(int adjustment, boolean auto)
	{
		if(auto)
		{
			this.isAnAutoEffect=true;
			this.setProfficiency(100);
			return true;
		}

		isAnAutoEffect=false;
		int pctChance=profficiency();
		if(pctChance>95) pctChance=95;
		if(pctChance<5) pctChance=5;

		if(adjustment>=0)
			pctChance+=adjustment;
		else
		if(Dice.rollPercentage()>(100+adjustment))
			return false;
		return (Dice.rollPercentage()<pctChance);
	}

	public Environmental affecting()
	{
		return affected;
	}
	public void setAffectedOne(Environmental being)
	{
		affected=being;
	}

	public void unInvoke()
	{
		unInvoked=true;

		if(affected==null) return;
		Environmental being=affected;

		if(canBeUninvoked())
		{
			being.delAffect(this);
			if(being instanceof Room)
				((Room)being).recoverRoomStats();
			else
			if(being instanceof MOB)
			{
				if(((MOB)being).location()!=null)
					((MOB)being).location().recoverRoomStats();
				else
				{
					being.recoverEnvStats();
					((MOB)being).recoverCharStats();
					((MOB)being).recoverMaxState();
				}
			}
			else
				being.recoverEnvStats();
		}
	}

	public boolean canBeUninvoked()
	{
		return canBeUninvoked;
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{}
	public void affectCharStats(MOB affectedMob, CharStats affectableStats)
	{}
	public void affectCharState(MOB affectedMob, CharState affectableMaxState)
	{}

	public MOB invoker()
	{
		return invoker;
	}
	public void setInvoker(MOB mob){invoker=mob;}

	public void helpProfficiency(MOB mob)
	{
		Ability A=(Ability)mob.fetchAbility(ID());
		if(A==null) return;
		
		if((System.currentTimeMillis()
		-((StdAbility)A).lastProfHelp)<60000)
			return;

		if(A.profficiency()<100)
		{
			if(((int)Math.round(Math.sqrt(new Integer(mob.charStats().getStat(CharStats.INTELLIGENCE)).doubleValue())*34.0*Math.random()))>=A.profficiency())
			{
				// very important, since these can be autoinvoked affects (copies)!
				A.setProfficiency(A.profficiency()+1);
				if((this!=A)&&(profficiency()<100))
				{
					setProfficiency(profficiency()+1);
					((StdAbility)A).lastProfHelp=System.currentTimeMillis();
				}
			}
		}
		else
			A.setProfficiency(100);
	}

	public boolean invoke(MOB mob, Environmental target, boolean auto)
	{
		Vector V=new Vector();
		if(target!=null)
			V.addElement(target.name());
		return invoke(mob,V,target,auto);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental target, boolean auto)
	{
		if(!auto)
		{
			isAnAutoEffect=false;

			// if you can't move, you can't cast! Not even verbal!
			if(!Sense.aliveAwakeMobile(mob,false))
				return false;

			int manaConsumed=50;
			int qualifyingLevel=CMAble.qualifyingLevel(mob,this);
			
			int diff=0;
			if(qualifyingLevel<0)
				diff=0;
			else
				diff=CMAble.qualifyingClassLevel(mob,this)-qualifyingLevel;

			if(diff>0)
			switch(diff)
			{
			case 1: manaConsumed=35; break;
			case 2: manaConsumed=25; break;
			case 3: manaConsumed=20; break;
			case 4: manaConsumed=15; break;
			case 5: manaConsumed=10; break;
			default: manaConsumed=5; break;
			}

			if(overrideMana()>=0) manaConsumed=overrideMana();

			if(mob.curState().getMana()<manaConsumed)
			{
				mob.tell("You don't have enough mana to do that.");
				return false;
			}
			mob.curState().adjMana(-manaConsumed,mob.maxState());
			helpProfficiency(mob);
		}
		else
			isAnAutoEffect=true;
		return true;
	}



	public boolean maliciousAffect(MOB mob,
								   Environmental target,
								   int tickAdjustmentFromStandard,
								   int additionAffectCheckCode)
	{
		boolean ok=true;
		if(additionAffectCheckCode>=0)
		{
			FullMsg msg=new FullMsg(mob,target,this,Affect.NO_EFFECT,additionAffectCheckCode,Affect.NO_EFFECT,null);
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				ok=(!msg.wasModified());
			}
			else
				ok=false;
		}
		if(ok)
		{
			invoker=mob;
			Ability newOne=(Ability)this.copyOf();
			((StdAbility)newOne).canBeUninvoked=true;
			if(tickAdjustmentFromStandard<=0)
			{
				tickAdjustmentFromStandard=(adjustedLevel(mob)*4)+25;

				if(target!=null)
					tickAdjustmentFromStandard-=(target.envStats().level()*2);

				if(tickAdjustmentFromStandard<5)
					tickAdjustmentFromStandard=5;
			}

			newOne.startTickDown(invoker,target,tickAdjustmentFromStandard);
		}
		return ok;
	}

	public boolean beneficialWordsFizzle(MOB mob,
										Environmental target,
										String message)
	{
		// it didn't work, but tell everyone you tried.
		FullMsg msg=new FullMsg(mob,target,this,Affect.MSG_SPEAK,"^T"+message+"^?");
		if(mob.location().okAffect(mob,msg))
			mob.location().send(mob,msg);

		return false;
	}

	public boolean beneficialVisualFizzle(MOB mob,
										  Environmental target,
										  String message)
	{
		// it didn't work, but tell everyone you tried.
		FullMsg msg=new FullMsg(mob,target,this,Affect.MSG_OK_VISUAL,message);
		if(mob.location().okAffect(mob,msg))
			mob.location().send(mob,msg);

		return false;
	}

	public boolean maliciousFizzle(MOB mob,
								   Environmental target,
									String message)
	{
		// it didn't work, but tell everyone you tried.
		FullMsg msg=new FullMsg(mob,target,this,Affect.MSG_OK_VISUAL|Affect.MASK_MALICIOUS,message);
		if(mob.location().okAffect(mob,msg))
			mob.location().send(mob,msg);

		return false;
	}


	public boolean beneficialAffect(MOB mob,
								   Environmental target,
								   int tickAdjustmentFromStandard)
	{
		boolean ok=true;
		if(ok)
		{
			invoker=mob;
			Ability newOne=(Ability)this.copyOf();
			((StdAbility)newOne).canBeUninvoked=true;

			if(tickAdjustmentFromStandard<=0)
				tickAdjustmentFromStandard=(adjustedLevel(mob)*7)+60;

			newOne.startTickDown(invoker,target,tickAdjustmentFromStandard);
		}
		return ok;
	}

	public boolean autoInvocation(MOB mob)
	{
		if(isAutoInvoked())
		{
			if(CMAble.qualifiesByLevel(mob,this))
			{
				Ability thisAbility=mob.fetchAffect(this.ID());
				if(thisAbility!=null)
					return false;
				Ability thatAbility=(Ability)this.copyOf();
				((StdAbility)thatAbility).canBeUninvoked=true;
				mob.addAffect(thatAbility);
				return true;
			}
		}
		return false;
	}
	public void makeNonUninvokable()
	{
		unInvoked=false;
		canBeUninvoked=false;
		borrowed=false;
	}

	public String accountForYourself(){return name();}
	public int getTickDownRemaining(){return tickDown;}
	public void setTickDownRemaining(int newTick){tickDown=newTick;}

	public boolean canBeTaughtBy(MOB teacher, MOB student)
	{
		Ability yourAbility=teacher.fetchAbility(ID());
		if(yourAbility!=null)
		{
			if(yourAbility.profficiency()<25)
			{
				teacher.tell("You are not profficient enough to teach '"+name()+"'");
				student.tell(teacher.name()+" is not profficient enough to teach '"+name()+"'.");
				return false;
			}
			return true;
		}
		teacher.tell("You don't know '"+name()+"'.");
		student.tell(teacher.name()+" doesn't know '"+name()+"'.");
		return false;
	}

	public String requirements()
	{
		String returnable="";
		if(trainsRequired()==1)
			returnable="1 train";
		else
		if(trainsRequired()>1)
			returnable=trainsRequired()+" trains";
		if((returnable.length()>0)&&(practicesRequired()>0))
			returnable+=", ";
		if(practicesRequired()==1)
			returnable+="1 practice";
		else
		if(practicesRequired()>1)
			returnable+=practicesRequired()+" practices";
		if(returnable.length()==0)
			return "free!";
		else
			return returnable;
	}

	public boolean canBeLearnedBy(MOB teacher, MOB student)
	{
		if(student.getPractices()<practicesRequired())
		{
			teacher.tell(student.name()+" does not have enough practice points to learn '"+name()+"'.");
			student.tell("You do not have enough practice points.");
			return false;
		}
		if(student.getTrains()<trainsRequired())
		{
			teacher.tell(student.name()+" does not have enough training sessions to learn '"+name()+"'.");
			student.tell("You do not have enough training sessions.");
			return false;
		}
		int qLevel=CMAble.qualifyingLevel(student,this);
		if(qLevel<0)
		{
			teacher.tell(student.name()+" is not the right class to learn '"+name()+"'.");
			student.tell("You are not the right class to learn '"+name()+"'.");
			return false;
		}
		if(!CMAble.qualifiesByLevel(student,this))
		{
			teacher.tell(student.name()+" is not high enough level to learn '"+name()+"'.");
			student.tell("You are not high enough level to learn '"+name()+"'.");
			return false;
		}
		if(qLevel>(student.charStats().getStat(CharStats.INTELLIGENCE)+15))
		{
			teacher.tell(student.name()+" is too stupid to learn '"+name()+"'.");
			student.tell("You are not of high enough intelligence to learn '"+name()+"'.");
			return false;
		}
		Ability yourAbility=student.fetchAbility(ID());
		Ability teacherAbility=teacher.fetchAbility(ID());
		if(yourAbility!=null)
		{
			teacher.tell(student.name()+" already knows '"+name()+"'.");
			student.tell("You already know '"+name()+"'.");
			return false;
		}

		if(teacherAbility!=null)
		{
			if(teacherAbility.profficiency()<25)
			{
				teacher.tell("You aren't profficient enough to teach '"+name()+"'.");
				student.tell(teacher.name()+" isn't profficient enough to teach you '"+name()+"'.");
				return false;
			}
		}
		else
		{
			student.tell(teacher.name()+" does not know anything about that.");
			teacher.tell("You don't know that.");
			return false;
		}

		return true;
	}

	public boolean canBePracticedBy(MOB teacher, MOB student)
	{
		if(student.getPractices()<practicesToPractice())
		{
			teacher.tell(student.name()+" does not have enough practices to practice '"+name()+"'.");
			student.tell("You do not have enough practices.");
			return false;
		}

		Ability yourAbility=student.fetchAbility(ID());
		Ability teacherAbility=teacher.fetchAbility(ID());
		if((yourAbility==null)||(CMAble.qualifyingLevel(student,yourAbility)<0))
		{
			teacher.tell(student.name()+" has not learned '"+name()+"' yet.");
			student.tell("You havn't learned '"+name()+"' yet.");
			return false;
		}

		if(!CMAble.qualifiesByLevel(student,yourAbility))
		{
			teacher.tell(student.name()+" is not high enough level to practice '"+name()+"'.");
			student.tell("You are not high enough level to practice '"+name()+"'.");
			return false;
		}

		if(teacherAbility==null)
		{
			student.tell(teacher.name()+" does not know anything about '"+name()+"'.");
			teacher.tell("You don't know '"+name()+"'.");
			return false;
		}

		if(yourAbility.profficiency()>teacherAbility.profficiency())
		{
			teacher.tell("You aren't profficient enough to teach any more about '"+name()+"'.");
			student.tell(teacher.name()+" isn't profficient enough to teach any more about '"+name()+"'.");
			return false;
		}
		else
		if(yourAbility.profficiency()>74)
		{
			teacher.tell("You can't teach "+student.charStats().himher()+" any more about '"+name()+"'.");
			student.tell("You can't learn any more about '"+name()+"' except through dilligence.");
			return false;
		}

		if(teacherAbility.profficiency()<25)
		{
			teacher.tell("You aren't profficient enough to teach '"+name()+"'.");
			student.tell(teacher.name()+" isn't profficient enough to teach you '"+name()+"'.");
			return false;
		}

		return true;
	}


	public void teach(MOB teacher, MOB student)
	{
		if(student.getPractices()<practicesRequired())
			return;
		if(student.getTrains()<trainsRequired())
			return;
		if(student.fetchAbility(ID())==null)
		{
			student.setPractices(student.getPractices()-practicesRequired());
			student.setTrains(student.getTrains()-trainsRequired());
			Ability newAbility=(Ability)newInstance();
			newAbility.setProfficiency((int)Math.round(Util.mul(profficiency(),((Util.div(teacher.charStats().getStat(CharStats.WISDOM)+student.charStats().getStat(CharStats.INTELLIGENCE),100.0))))));
			if(newAbility.profficiency()>75)
				newAbility.setProfficiency(75);
			student.addAbility(newAbility);
			newAbility.autoInvocation(student);
		}
	}

	public void practice(MOB teacher, MOB student)
	{
		if(student.getPractices()<practicesToPractice())
			return;

		Ability yourAbility=student.fetchAbility(ID());
		if(yourAbility!=null)
		{
			if(yourAbility.profficiency()<75)
			{
				student.setPractices(student.getPractices()-practicesToPractice());
				yourAbility.setProfficiency(yourAbility.profficiency()+(int)Math.round(25.0*(Util.div(teacher.charStats().getStat(CharStats.WISDOM)+student.charStats().getStat(CharStats.INTELLIGENCE),36.0))));
				if(yourAbility.profficiency()>75)
					yourAbility.setProfficiency(75);
			}
		}
	}
	public void makeLongLasting()
	{
		tickDown=Integer.MAX_VALUE;
	}


	/** this method defines how this thing responds
	 * to environmental changes.  It may handle any
	 * and every affect listed in the Affect class
	 * from the given Environmental source */
	public void affect(Environmental myHost, Affect affect)
	{
		return;
	}

	/** this method is used to tell the system whether
	 * a PENDING affect may take place
	 */
	public boolean okAffect(Environmental myHost, Affect affect)
	{
		return true;
	}

	/**
	 * this method allows any environmental object
	 * to behave according to a timed response.  by
	 * default, it will never be called unless the
	 * object uses the ServiceEngine to setup service.
	 * The tickID allows granularity with the type
	 * of service being requested.
	 */
	public boolean tick(Tickable ticking, int tickID)
	{
		if((unInvoked)&&(canBeUninvoked()))
			return false;

		if((tickID==Host.MOB_TICK)
		&&(tickDown!=Integer.MAX_VALUE)
		&&(canBeUninvoked()))
		{
			if(tickDown<0)
				return !unInvoked;
			else
			{
				tickDown-=1;
				if(tickDown<=0)
				{
					tickDown=-1;
					unInvoke();
					return false;
				}
			}
		}
		return true;
	}
	public boolean appropriateToMyAlignment(int alignment){return true;}

	public void addAffect(Ability to){}
	public void addNonUninvokableAffect(Ability to){}
	public void delAffect(Ability to){}
	public int numAffects(){ return 0;}
	public Ability fetchAffect(int index){return null;}
	public Ability fetchAffect(String ID){return null;}
	public void addBehavior(Behavior to){}
	public void delBehavior(Behavior to){}
	public int numBehaviors(){return 0;}
	public Behavior fetchBehavior(int index){return null;}
	public Behavior fetchBehavior(String ID){return null;}
	public boolean isGeneric(){return false;}

	private static final String[] CODES={"CLASS","TEXT"};
	public String[] getStatCodes(){return CODES;}
	private int getCodeNum(String code){
		for(int i=0;i<CODES.length;i++)
			if(code.equalsIgnoreCase(CODES[i])) return i;
		return -1;
	}
	public String getStat(String code){
		switch(getCodeNum(code))
		{
		case 0: return ID();
		case 1: return text();
		}
		return "";
	}
	public void setStat(String code, String val)
	{
		switch(getCodeNum(code))
		{
		case 0: return;
		case 1: setMiscText(val); break;
		}
	}
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof StdAbility)) return false;
		for(int i=0;i<CODES.length;i++)
			if(!E.getStat(CODES[i]).equals(getStat(CODES[i])))
				return false;
		return true;
	}
}
