package com.planet_ink.coffee_mud.Abilities.Properties;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.Vector;

public class Property implements Ability, Cloneable
{
	public String ID() { return "Property"; }
	public String name(){ return "a Property";}
	public String description(){return "";}
	public String displayText(){return "";}
	protected boolean borrowed=false;
	protected String miscText="";
	protected Environmental affected=null;
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}
	public int usesRemaining(){return 0;}
	public void setUsesRemaining(int newUses){}

	public void setName(String newName){}
	public void setDescription(String newDescription){}
	public void setDisplayText(String newDisplayText){}
	public MOB invoker(){return null;}
	public static final String[] empty={};
	public String[] triggerStrings(){return empty;}
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

	public int classificationCode(){ return Ability.PROPERTY;}
	public boolean isBorrowed(Environmental toMe){ return borrowed;	}
	public void setBorrowed(Environmental toMe, boolean truefalse)	{ borrowed=truefalse; }

	public EnvStats envStats(){ return new DefaultEnvStats();}
	public EnvStats baseEnvStats(){ return new DefaultEnvStats();}
	public void recoverEnvStats(){}
	public void setBaseEnvStats(EnvStats newBaseEnvStats){}
	public Environmental newInstance()
	{ return new Property();}
	
	private void cloneFix(Ability E){}
	
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
	public void setMiscText(String newMiscText)
	{ miscText=newMiscText;}
	public String text()
	{ return miscText;}
	public boolean appropriateToMyAlignment(int alignment){return true;}
	public String accountForYourself(){return "";}
	public int affectType(){return 0;}
	public String requirements(){return "";}

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
	{ return false;}

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
