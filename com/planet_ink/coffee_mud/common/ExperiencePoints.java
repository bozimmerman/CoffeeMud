package com.planet_ink.coffee_mud.common;

import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.Util;

/**
 * something that is affected by, or affects
 * the environment around them.
 */
public class ExperiencePoints implements Environmental
{
	private int exp=0;
	public ExperiencePoints(int amount){exp=amount;}
	
	public String ID(){return "ExperiencePoints";}
	
	// the real name of the object
	public String name(){return "ExperiencePoints";}
	public String Name(){return "ExperiencePoints";}
	public void setName(String newName){}

	private boolean quiet=false;
	// how the object appears at rest, 
	public String displayText(){return ""+quiet;}
	public void setDisplayText(String newDisplayText){quiet=Util.s_bool(newDisplayText);}
	
	// Text displayed when this item is LOOKED at
	public String description(){return ""+exp;}
	public void setDescription(String newDescription){exp=Util.s_int(newDescription);}
	
	public Environmental copyOf(){return new ExperiencePoints(0);}
	public boolean isGeneric(){return false;}

	/** For internal use by items. This text
	 * is saved for each room instance of an item, and
	 * may be used for behavior modification, description
	 * change, or anything else.
	 */
	private String homage="";
	public void setMiscText(String newMiscText){homage=newMiscText;}
	public String text(){return homage;}

	/** return a new instance of the object*/
	public Environmental newInstance(){return new ExperiencePoints(0);}

	private static final DefaultEnvStats ESTATS=new DefaultEnvStats();
	/** some general statistics about such an item
	 * see class "EnvStats" for more information. */
	public EnvStats baseEnvStats(){return ESTATS;}
	public EnvStats envStats(){return ESTATS;}
	public void setBaseEnvStats(EnvStats newBaseEnvStats){}
	public void recoverEnvStats(){}

	public final static String[] STATCODES={"EXP"};
	/** quick and easy access to the basic values in this object */
	public String[] getStatCodes(){return STATCODES;}
	public String getStat(String code){return ""+exp;}
	public void setStat(String code, String val){exp=Util.s_int(val);}
	public boolean sameAs(Environmental E){return false;}

	/** Manipulation of affect objects, which includes
	 * spells, traits, skills, etc.*/
	public void addAffect(Ability to){}
	public void addNonUninvokableAffect(Ability to){}
	public void delAffect(Ability to){}
	public int numAffects(){return 0;}
	public Ability fetchAffect(int index){return null;}
	public Ability fetchAffect(String ID){return null;}

	/** Manipulation of Behavior objects, which includes
	 * movement, speech, spellcasting, etc, etc.*/
	public void addBehavior(Behavior to){}
	public void delBehavior(Behavior to){}
	public int numBehaviors(){return 0;}
	public Behavior fetchBehavior(int index){return null;}
	public Behavior fetchBehavior(String ID){return null;}

	/**
	 * Parameters for using in 3 dimensional space
	 */
	public int maxRange(){return exp;}
	public int minRange(){return exp;}
	
	
	public void affectEnvStats(Environmental affected, EnvStats affectableStats){}
	public void affectCharStats(MOB affectedMob, CharStats affectableStats){}
	public void affectCharState(MOB affectedMob, CharState affectableMaxState){}
	public void affect(Environmental myHost, Affect affect){}
	public boolean okAffect(Environmental myHost, Affect affect){return true;}
	
	public int compareTo(Object O){return -1;}
	public long getTickStatus(){return 0;}
	public boolean tick(Tickable ticking, int tickID)
	{ return false;}
}