package com.planet_ink.coffee_mud.Abilities.Properties;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.Vector;

public class Property implements Ability, Cloneable
{
	protected boolean borrowed=false;
	protected String myID="";
	protected String name="";
	protected String displayText="";
	protected String miscText="";
	protected String description="";
	protected Environmental affected=null;
	protected EnvStats envStats=new DefaultEnvStats();
	protected EnvStats baseEnvStats=new DefaultEnvStats();

	public Property()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="A special property or affect";
		displayText="";
		miscText="";
	}
	public int usesRemaining(){return 0;}
	public void setUsesRemaining(int newUses){}

	public MOB invoker(){return null;}
	public Vector triggerStrings(){return new Vector();}
	public boolean invoke(MOB mob, Vector commands, Environmental target, boolean auto){return false;}
	public boolean invoke(MOB mob, Environmental target, boolean auto){return false;}
	public boolean autoInvocation(MOB mob){return false;}
	public void unInvoke(){}
	public boolean canBeUninvoked(){return false;}
	public boolean isAutoInvoked(){return true;}
	public boolean isNowAnAutoEffect(){return true;}

	public boolean canBeTaughtBy(MOB teacher, MOB student){return false;}
	public boolean canBePracticedBy(MOB teacher, MOB student){return false;}
	public boolean canBeLearnedBy(MOB teacher, MOB student){return false;}
	public void teach(MOB teacher, MOB student){}
	public void practice(MOB teacher, MOB student){}
	public int maxRange(){return Integer.MAX_VALUE;}
	public int minRange(){return Integer.MIN_VALUE;}

	public boolean qualifiesByLevel(MOB student){return false;}
	public int qualifyingLevel(MOB student){return -1;}
	public void startTickDown(Environmental affected, int tickTime){}

	public int profficiency(){return 0;}
	public void setProfficiency(int newProfficiency){}
	public boolean profficiencyCheck(int adjustment, boolean auto){return false;}
	public void helpProfficiency(MOB mob){}

	public Environmental affecting(){return affected;}
	public void setAffectedOne(Environmental being){affected=being;}

	public boolean putInCommandlist(){return false;}
	public int quality(){return Ability.INDIFFERENT;}

	public int classificationCode()
	{
		return Ability.PROPERTY;
	}
	public String ID()
	{
		return myID;
	}

	public String name(){ return name;}
	public void setName(String newName){name=newName;}

	public boolean isBorrowed(Environmental toMe)
	{ return borrowed;	}
	public void setBorrowed(Environmental toMe, boolean truefalse)
	{ borrowed=truefalse; }

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
		return new Property();
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
			Property E=(Property)this.clone();
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
	public boolean appropriateToMyAlignment(int alignment){return true;}
	public String accountForYourself(){return "";}
	public int affectType(){return 0;}


	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{}
	public void affectCharStats(MOB affectedMob, CharStats affectableStats)
	{}
	public void affectCharState(MOB affectedMob, CharState affectableMaxState)
	{}
	public void affect(Affect affect)
	{
		return;
	}
	public boolean okAffect(Affect affect)
	{
		return true;
	}
	public boolean tick(int tickID)
	{ return true;	}
	public void makeLongLasting(){}
	public void makeNonUninvokable(){}


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
