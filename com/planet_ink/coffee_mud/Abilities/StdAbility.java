package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class StdAbility implements Ability, Cloneable
{
	protected String myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
	protected String name="an ability";
	protected String description="&";
	protected boolean borrowed=false;
	public String displayText="What they see when affected.";
	public String miscText="";
	protected MOB invoker=null;
	protected Vector triggerStrings=new Vector();
	protected int uses=Integer.MAX_VALUE;
	protected int profficiency=0;
	protected boolean isAnAutoEffect=false;
	protected int maxRange=0;
	protected int minRange=0;

	protected EnvStats envStats=new DefaultEnvStats();
	protected EnvStats baseEnvStats=new DefaultEnvStats();
	protected Environmental affected=null;

	protected boolean canBeUninvoked=true;
	protected boolean isAutoinvoked=false;
	protected boolean unInvoked=false;
	protected boolean putInCommandlist=true;
	protected int trainsRequired=1;
	protected int practicesRequired=1;
	protected int practicesToPractice=1;

	protected int quality=Ability.INDIFFERENT;
	
	protected int tickDown=-1;
	public StdAbility()
	{
	}
	public boolean qualifies(MOB student)
	{
		int level=qualifyingLevel(student);
		if(level<0) return false;
		if(student.envStats().level()>=level)
			return true;
		else
			return false;
	}

	public boolean isAnAutoEffect()
	{ return isAnAutoEffect; }
	public boolean isBorrowed(Environmental toMe)
	{ return borrowed;	}
	public void setBorrowed(Environmental toMe, boolean truefalse)
	{ borrowed=truefalse; }

	public void startTickDown(Environmental affected, int tickTime)
	{
		if(affected.fetchAffect(this.ID())==null)
			affected.addAffect(this);
		if(affected instanceof MOB)
			((MOB)affected).location().recoverRoomStats();
		else
		{
			if(affected instanceof Room)
				((Room)affected).recoverRoomStats();
			else
				affected.recoverEnvStats();
			ExternalPlay.startTickDown(this,Host.MOB_TICK,1);
		}

		tickDown=tickTime;
	}

	public int maxRange(){return maxRange;}
	public int minRange(){return minRange;}
	
	public boolean putInCommandlist()
	{
		return putInCommandlist;
	}
	public int qualifyingLevel(MOB student)
	{
		if(student==null) return -1;

		if(student.charStats().getMyClass()==null)
			return -1;

		return CMAble.getQualifyingLevel(student.charStats().getMyClass().ID(),ID());
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
		if((targetName.length()==0)&&(mob.isInCombat())&&(quality==Ability.MALICIOUS)&&(mob.getVictim()!=null))
		   target=mob.getVictim();
		else
		if((targetName.length()==0)&&(quality!=Ability.MALICIOUS))
			target=mob;
		else
		if(targetName.length()>0)
		{
			target=mob.location().fetchInhabitant(targetName);
			if(target==null)
			{
				Environmental t=mob.location().fetchFromRoomFavorItems(null,targetName);
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

	public Environmental getAnyTarget(MOB mob, Vector commands, Environmental givenTarget)
	{
		String targetName=Util.combine(commands,0);
		Environmental target=null;
		if(givenTarget!=null)
			target=givenTarget;
		else
		if((targetName.length()==0)&&(mob.isInCombat())&&(quality==Ability.MALICIOUS)&&(mob.getVictim()!=null))
			target=mob.getVictim();
		else
		{
			target=mob.location().fetchFromRoomFavorMOBs(null,targetName);
			if(target==null)
				target=mob.location().fetchFromMOBRoomFavorsItems(mob,null,targetName);
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

	public Item getTarget(MOB mob, Room location, Environmental givenTarget, Vector commands)
	{
		String targetName=Util.combine(commands,0);

		Environmental target=null;
		if((givenTarget!=null)&&(givenTarget instanceof Item))
			target=givenTarget;
		
		if(location!=null)
			target=location.fetchFromRoomFavorItems(null,targetName);
		if(target==null)
		{
			if(location!=null)
				target=location.fetchFromMOBRoomFavorsItems(mob,null,targetName);
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

	public int classificationCode()
	{
		return Ability.SKILL;
	}

	public String ID()
	{
		return myID;
	}
	public String name(){ return name;}
	public void setName(String newName){name=newName;}

	public EnvStats envStats()
	{
		return envStats;
	}
	public EnvStats baseEnvStats()
	{
		return baseEnvStats;
	}
	public void recoverEnvStats()
	{
		envStats=baseEnvStats.cloneStats();
	}
	public void setBaseEnvStats(EnvStats newBaseEnvStats)
	{
		baseEnvStats=newBaseEnvStats.cloneStats();
	}

	public Environmental newInstance()
	{
		return new StdAbility();
	}
	private void cloneFix(Ability E)
	{
		baseEnvStats=E.baseEnvStats().cloneStats();
		envStats=E.envStats().cloneStats();
	}
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
	public String displayText()
	{ return displayText;}
	public void setDisplayText(String newDisplayText)
	{ displayText=newDisplayText;}
	public void setMiscText(String newMiscText)
	{ miscText=newMiscText;}
	public String text()
	{ return miscText;}
	public String description()
	{ return description;}
	public void setDescription(String newDescription)
	{ description=newDescription;}
	public int profficiency(){ return profficiency;}
	public void setProfficiency(int newProfficiency)
	{ 
		profficiency=newProfficiency;
		if(profficiency>100) profficiency=100;
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

		if(canBeUninvoked)
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

	public int usesRemaining()
	{
		return uses;
	}
	public void setUsesRemaining(int newUses)
	{
		uses=newUses;
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

	public Vector triggerStrings()
	{
		return triggerStrings;
	}

	public void helpProfficiency(MOB mob)
	{

		Ability A=(Ability)mob.fetchAbility(this.ID());
		if(A==null) return;
		if(A.profficiency()<100)
		{
			if(Math.round((Util.div(mob.charStats().getStat(CharStats.INTELLIGENCE),18.0))*100.0*Math.random())>50)
			{
				// very important, since these can be autoinvoked affects (copies)!
				A.setProfficiency(A.profficiency()+1);
				if((this!=A)&&(profficiency()<100))
					setProfficiency(profficiency()+1);
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
			int diff=mob.envStats().level()-envStats().level();
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
			if(mob.location().okAffect(msg))
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
			if(tickAdjustmentFromStandard<=0)
			{
				tickAdjustmentFromStandard=(mob.envStats().level()*3)+15;

				if(target!=null)
					tickAdjustmentFromStandard-=(target.envStats().level()*2);

				if(tickAdjustmentFromStandard<5)
					tickAdjustmentFromStandard=5;
			}

			newOne.startTickDown(target,tickAdjustmentFromStandard);
		}
		return ok;
	}

	public boolean beneficialWordsFizzle(MOB mob,
										Environmental target,
										String message)
	{
		// it didn't work, but tell everyone you tried.
		FullMsg msg=new FullMsg(mob,target,this,Affect.MSG_SPEAK,message);
		if(mob.location().okAffect(msg))
			mob.location().send(mob,msg);

		return false;
	}

	public boolean beneficialVisualFizzle(MOB mob,
										  Environmental target,
										  String message)
	{
		// it didn't work, but tell everyone you tried.
		FullMsg msg=new FullMsg(mob,target,this,Affect.MSG_OK_VISUAL,message);
		if(mob.location().okAffect(msg))
			mob.location().send(mob,msg);

		return false;
	}

	public boolean maliciousFizzle(MOB mob,
								   Environmental target,
									String message)
	{
		// it didn't work, but tell everyone you tried.
		FullMsg msg=new FullMsg(mob,target,this,Affect.MSG_OK_VISUAL|Affect.MASK_MALICIOUS,message);
		if(mob.location().okAffect(msg))
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

			if(tickAdjustmentFromStandard<=0)
				tickAdjustmentFromStandard=(mob.envStats().level()*3)+30;

			newOne.startTickDown(target,tickAdjustmentFromStandard);
		}
		return ok;
	}



	public boolean autoInvocation(MOB mob)
	{
		if(isAutoinvoked)
		{
			if(mob.envStats().level()>=envStats().level())
			{
				Ability thisAbility=mob.fetchAffect(this.ID());
				if(thisAbility!=null)
					return false;
				Ability thatAbility=(Ability)this.copyOf();
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
	}

	public String accountForYourself(){return name;}
	public int getTickDownRemaining(){return tickDown;}
	public void setTickDownRemaining(int newTick){tickDown=newTick;}

	public boolean canBeTaughtBy(MOB teacher, MOB student)
	{
		Ability yourAbility=teacher.fetchAbility(ID());
		if(yourAbility!=null)
		{
			if(yourAbility.profficiency()<25)
			{
				teacher.tell("You are not profficient enough to teach '"+ID()+"'");
				student.tell(teacher.name()+" is not profficient enough to teach '"+ID()+"'.");
				return false;
			}
			return true;
		}
		teacher.tell("You don't know '"+name()+"'.");
		student.tell(teacher.name()+" doesn't know '"+name()+"'.");
		return false;
	}

	public boolean canBeLearnedBy(MOB teacher, MOB student)
	{
		if(student.getPractices()<practicesRequired)
		{
			teacher.tell(student.name()+" does not have enough practices to learn '"+name()+"'.");
			student.tell("You do not have enough practices.");
			return false;
		}
		if(student.getTrains()<trainsRequired)
		{
			teacher.tell(student.name()+" does not have enough training points to learn '"+name()+"'.");
			student.tell("You do not have enough training points.");
			return false;
		}
		int qLevel=qualifyingLevel(student);
		if(qLevel<0)
		{
			teacher.tell(student.name()+" is not the right class to learn '"+name()+"'.");
			student.tell("You are not the right class to learn '"+name()+"'.");
			return false;
		}
		if((qLevel>student.envStats().level()))
		{
			teacher.tell(student.name()+" is not high enough level to learn '"+name()+"'.");
			student.tell("You are not high enough level to learn '"+name()+"'.");
			return false;
		}
		if(qLevel>student.charStats().getStat(CharStats.INTELLIGENCE)+7)
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
		if(student.getPractices()<practicesToPractice)
		{
			teacher.tell(student.name()+" does not have enough practices to practice '"+name()+"'.");
			student.tell("You do not have enough practices.");
			return false;
		}

		Ability yourAbility=student.fetchAbility(ID());
		Ability teacherAbility=teacher.fetchAbility(ID());
		if((yourAbility==null)||(yourAbility.qualifyingLevel(student)<0))
		{
			teacher.tell(student.name()+" has not learned '"+name()+"' yet.");
			student.tell("You havn't learned '"+name()+"' yet.");
			return false;
		}

		if(yourAbility.qualifyingLevel(student)>student.envStats().level())
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
		if(student.getPractices()<practicesRequired)
			return;
		if(student.getTrains()<trainsRequired)
			return;
		if(student.fetchAbility(ID())==null)
		{
			student.setPractices(student.getPractices()-practicesRequired);
			student.setTrains(student.getTrains()-trainsRequired);
			Ability newAbility=(Ability)newInstance();
			newAbility.setProfficiency((int)Math.round(Util.mul(profficiency(),((Util.div(teacher.charStats().getStat(CharStats.WISDOM)+student.charStats().getStat(CharStats.INTELLIGENCE),100.0))))));
			if(newAbility.profficiency()>75)
				newAbility.setProfficiency(75);
			int qLevel=qualifyingLevel(student);
			if(qLevel<1) qLevel=1;
			newAbility.envStats().setLevel(qLevel);
			student.addAbility(newAbility);
			newAbility.autoInvocation(student);
		}
	}

	public void practice(MOB teacher, MOB student)
	{
		if(student.getPractices()<practicesToPractice)
			return;

		Ability yourAbility=student.fetchAbility(ID());
		if(yourAbility!=null)
		{
			if(yourAbility.profficiency()<75)
			{
				student.setPractices(student.getPractices()-practicesToPractice);
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

	public int quality(){return this.quality;}

	/** this method defines how this thing responds
	 * to environmental changes.  It may handle any
	 * and every affect listed in the Affect class
	 * from the given Environmental source */
	public void affect(Affect affect)
	{
		return;
	}

	/** this method is used to tell the system whether
	 * a PENDING affect may take place
	 */
	public boolean okAffect(Affect affect)
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
	public boolean tick(int tickID)
	{
		if((unInvoked)&&(canBeUninvoked))
			return false;

		if((tickID==Host.MOB_TICK)
		&&(tickDown!=Integer.MAX_VALUE))
		{
			if(tickDown<0)
				return !unInvoked;
			else
			{
				tickDown-=1;
				if(tickDown<=0)
				{
					tickDown=-1;
					this.unInvoke();
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
	public boolean isGeneric(){return false;}

}
