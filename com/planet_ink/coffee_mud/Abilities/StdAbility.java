package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import java.util.*;

public class StdAbility implements Ability, Cloneable
{
	protected String myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
	protected String name="an ability";
	protected String description="&";
	public String displayText="What they see when affected.";
	public String miscText="";
	protected MOB invoker=null;
	protected Vector triggerStrings=new Vector();
	protected int uses=Integer.MAX_VALUE;
	protected int profficiency=0;
	
	private Vector qualifyingClassNames=new Vector();
	private Vector qualifyingClassLevels=new Vector();
	
	protected Stats envStats=new Stats();
	protected Stats baseEnvStats=new Stats();
	protected Environmental affected=null;
	
	protected boolean canBeUninvoked=true;
	protected boolean isAutoinvoked=false;
	protected boolean unInvoked=false;
	protected boolean putInCommandlist=true;
	
	public boolean malicious=false;
	
	protected long tickDown=-1;
	
	private int lowestQualifyingLevel=Integer.MAX_VALUE;
	
	public StdAbility()
	{
	}
	
	protected void addQualifyingClass(String className, int atLevel)
	{
		qualifyingClassNames.addElement(className);
		qualifyingClassLevels.addElement(new Integer(atLevel));
		if(atLevel<lowestQualifyingLevel)
			lowestQualifyingLevel=atLevel;
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
	
	public void startTickDown(Environmental affected, long tickTime)
	{
		affected.addAffect(this);
		if(affected instanceof MOB)
			((MOB)affected).location().recoverRoomStats();
		else
		{
			if(affected instanceof Room)
				((Room)affected).recoverRoomStats();
			else
				affected.recoverEnvStats();
			ServiceEngine.startTickDown(this,ServiceEngine.MOB_TICK,1);
		}
			
		tickDown=tickTime;
	}
	
	public boolean putInCommandlist()
	{
		return putInCommandlist;
	}
	public int qualifyingLevel(MOB student)
	{
		if(student==null) return -1;
		
		if(student.charStats().getMyClass()==null) 
			return -1;
		
		if(lowestQualifyingLevel==Integer.MAX_VALUE)
			return -1;
		
		if(student.charStats().getMyClass() instanceof Archon)
			return lowestQualifyingLevel;
		
		for(int i=0;i<qualifyingClassNames.size();i++)
			if(student.charStats().getMyClass().ID().equals((String)qualifyingClassNames.elementAt(i)))
				return ((Integer)qualifyingClassLevels.elementAt(i)).intValue();
		
		return -1;
	}
	
	public MOB getTarget(MOB mob, Vector commands)
	{
		String targetName=CommandProcessor.combine(commands,0);
		if((targetName.length()==0)&&(mob.isInCombat())&&(malicious))
		   targetName=mob.getVictim().ID();
		else
		if((targetName.length()==0)&&(!malicious))
		   targetName=mob.ID();
			
		MOB target=mob.location().fetchInhabitant(targetName);
		if((target==null)||((target!=null)&&(!Sense.canBeSeenBy(target,mob))))
		{
			mob.tell("You don't see '"+targetName+"' here.");
			return null;
		}
		
		if(target.fetchAffect(this.ID())!=null)
		{
			if(target==mob)
				mob.tell("You is already affected by "+name()+".");
			else
				mob.tell(target.name()+" is already affected by "+name()+".");
			return null;
		}
		return target;
	}
	
	public Item getTarget(MOB mob, Room location, Vector commands)
	{
		String targetName=CommandProcessor.combine(commands,0);
		
		Environmental target=location.fetchFromRoom(null,targetName);
		if(target==null)
			target=location.fetchFromMOBRoom(mob,null,targetName);
		if((target==null)||((target!=null)&&((!Sense.canBeSeenBy(target,mob))||(!(target instanceof Item)))))
		{
			mob.tell("You don't see '"+targetName+"' here.");
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
	
	public Stats envStats()
	{
		return envStats;
	}
	public Stats baseEnvStats()
	{
		return baseEnvStats;
	}
	public void recoverEnvStats()
	{
		envStats=baseEnvStats.cloneStats();
	}
	public void setBaseEnvStats(Stats newBaseEnvStats)
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
	{ profficiency=newProfficiency;}
	
	public boolean profficiencyCheck(int adjustment)
	{
		int pctChance=profficiency()+adjustment;
		if(pctChance>95) pctChance=95;
		if(pctChance<5) pctChance=5;
		
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
	
	public void affectEnvStats(Environmental affected, Stats affectableStats)
	{}
	public void affectCharStats(MOB affectedMob, CharStats affectableStats)
	{
	}
	
	public MOB invoker()
	{
		return invoker;
	}
	
	public Vector triggerStrings()
	{
		return triggerStrings;
	}
	
    public boolean invoke(MOB mob, Environmental target, boolean automatic)
    {
		int oldProfficiency=profficiency();
		int oldMana=mob.curState().getMana();
		Vector V=new Vector();
		V.addElement(target.name());
		
		
		if(automatic)
			setProfficiency(999);
		
		mob.curState().setMana(999);
		boolean success=invoke(mob,V);
		mob.curState().setMana(oldMana);
		
		if(automatic)
			setProfficiency(oldProfficiency);
		
		return success;
    }

	public void helpProfficiency(MOB mob)
	{
		
		Ability A=(Ability)mob.fetchAbility(this.ID());
		if(A==null) return;
		if(A.profficiency()<100)
		{
			if(Math.round((Util.div(mob.charStats().getIntelligence(),25.0))*100.0*Math.random())>50)
			   A.setProfficiency(A.profficiency()+1);
		}
		
	}
	
	public boolean invoke(MOB mob, Vector commands)
	{
		// if you can't move, you can't cast!
		if(!Sense.canPerformAction(mob))
			return false;
		
		Integer levelDiff=new Integer(mob.envStats().level()-envStats().level());
		int manaConsumed=50-(int)Math.round(50.0*(levelDiff.doubleValue()/10.0));
		
		if(manaConsumed<5) manaConsumed=5;
		if(manaConsumed>50) manaConsumed=50;
		if(!mob.curState().adjMana(-manaConsumed,mob.maxState()))
		{
			mob.tell("You don't have enough mana to do that.");
			return false;
		}
		helpProfficiency(mob);
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
				tickAdjustmentFromStandard=(mob.envStats().level()*2)+5;
				
				if(target!=null)
					tickAdjustmentFromStandard-=(target.envStats().level()*2);
				
				if(tickAdjustmentFromStandard<5)
					tickAdjustmentFromStandard=5;
			}
			
			newOne.startTickDown(target,tickAdjustmentFromStandard);
		}
		return ok;
	}
	
	public boolean beneficialFizzle(MOB mob, 
									Environmental target, 
									String message)
	{
		// it didn't work, but tell everyone you tried.
		FullMsg msg=new FullMsg(mob,target,this,Affect.SOUND_WORDS,Affect.SOUND_WORDS,Affect.SOUND_WORDS,message);
		if(mob.location().okAffect(msg))
			mob.location().send(mob,msg);
		
		return false;
	}
	
	public boolean maliciousFizzle(MOB mob, 
								   Environmental target, 
									String message)
	{
		// it didn't work, but tell everyone you tried.
		FullMsg msg=new FullMsg(mob,target,this,Affect.STRIKE_JUSTICE,Affect.STRIKE_JUSTICE,Affect.STRIKE_JUSTICE,message);
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
				tickAdjustmentFromStandard=(mob.envStats().level()*2)+5;
			
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
				
				mob.addAffect((Ability)this.copyOf());
				return true;
			}
		}
		return false;
	}
	
	public boolean canBeTaughtBy(MOB mob)
	{
		Ability yourAbility=mob.fetchAbility(ID());
		if(yourAbility!=null)
		{
			if(yourAbility.profficiency()<25)
			{
				mob.tell("You are not profficient enough to teach that.");
				return false;
			}
			return true;
		}
		mob.tell("You don't know how to do that.");
		return false;
	}
	
	public boolean canBeLearnedBy(MOB teacher, MOB student)
	{
		if(student.getPractices()<=0)
		{
			teacher.tell(student.name()+" does not have enough practices to learn that.");
			student.tell("You do not have any practices.");
			return false;
		}
		if(student.getTrains()<=0)
		{
			teacher.tell(student.name()+" does not have enough training points to learn that.");
			student.tell("You do not have any training points.");
			return false;
		}
		int qLevel=qualifyingLevel(student);
		if(qLevel<0)
		{
			teacher.tell(student.name()+" is not the right class.");
			student.tell("You are not the right class.");
			return false;
		}
		if((qLevel>student.envStats().level()))
		{
			teacher.tell(student.name()+" is not high enough level to learn that.");
			student.tell("You are not high enough level to learn that.");
			return false;
		}
		if(qLevel>student.charStats().getIntelligence()+7)
		{
			teacher.tell(student.name()+" is too stupid to learn that.");
			student.tell("You are not of high enough intelligence to learn that.");
			return false;
		}
		Ability yourAbility=student.fetchAbility(ID());
		Ability teacherAbility=teacher.fetchAbility(ID());
		if(yourAbility!=null)
		{
			teacher.tell(student.name()+" already knows that.");
			student.tell("You already know that.");
			return false;
		}
		
		if(teacherAbility!=null)
		{
			if(teacherAbility.profficiency()<25)
			{
				teacher.tell("You aren't profficient enough to teach that.");
				student.tell(teacher.name()+" isn't profficient enough to teach you that.");
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
		if(student.getPractices()==0)
		{
			teacher.tell(student.name()+" does not have enough practices to practice that.");
			student.tell("You do not have any practices.");
			return false;
		}
		
		Ability yourAbility=student.fetchAbility(ID());
		Ability teacherAbility=teacher.fetchAbility(ID());
		if(yourAbility==null)
		{
			teacher.tell(student.name()+" has not learned that yet.");
			student.tell("You havn't learned that yet.");
			return false;
		}
			
		if(teacherAbility==null)
		{
			student.tell(teacher.name()+" does not know anything about that.");
			teacher.tell("You don't know how to do that.");
			return false;
		}
			
		if(yourAbility.profficiency()>teacherAbility.profficiency())
		{
			teacher.tell("You aren't profficient enough to teach any more.");
			student.tell(teacher.name()+" isn't profficient enough to teach any more.");
			return false;
		}
		else
		if(yourAbility.profficiency()>74)
		{
			teacher.tell("You can't teach "+student.charStats().himher()+" any more.");
			student.tell("You can't learn any more about that except through dilligence.");
			return false;
		}
		
		if(teacherAbility.profficiency()<25)
		{
			teacher.tell("You aren't profficient enough to teach that.");
			student.tell(teacher.name()+" isn't profficient enough to teach you that.");
			return false;
		}
		
		return true;	
	}

	
	public void teach(MOB teacher, MOB student)
	{
		if(student.getPractices()==0)
			return;
		if(student.getTrains()==0)
			return;
		if(student.fetchAbility(ID())==null)
		{
			student.setPractices(student.getPractices()-1);
			student.setTrains(student.getTrains()-1);
			Ability newAbility=(Ability)newInstance();
			newAbility.setProfficiency((int)Math.round(Util.mul(profficiency(),((Util.div(teacher.charStats().getWisdom()+student.charStats().getIntelligence(),100.0))))));
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
		if(student.getPractices()==0)
			return;
		
		Ability yourAbility=student.fetchAbility(ID());
		if(yourAbility!=null)
		{
			if(yourAbility.profficiency()<75)
			{
				student.setPractices(student.getPractices()-1);
				yourAbility.setProfficiency(yourAbility.profficiency()+(int)Math.round(25.0*(Util.div(teacher.charStats().getWisdom()+student.charStats().getIntelligence(),40.0))));
				if(yourAbility.profficiency()>75)
					yourAbility.setProfficiency(75);
			}
		}
	}
	
	public boolean isMalicious()
	{
		return malicious;
	}
	
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
		
		if(tickID==ServiceEngine.MOB_TICK)
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
	
	public void addAffect(Ability to){}
	public void delAffect(Ability to){}
	public int numAffects(){ return 0;}
	public Ability fetchAffect(int index){return null;}
	public Ability fetchAffect(String ID){return null;}
	public void addBehavior(Behavior to){}
	public void delBehavior(Behavior to){}
	public int numBehaviors(){return 0;}
	public Behavior fetchBehavior(int index){return null;}
	
}
