package com.planet_ink.coffee_mud.Races;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GenRace extends StdRace
{
	private String ID="GenRace";
	public String ID(){	return ID; }
	private String name="GenRace";
	public String name(){ return name; }
	public int practicesAtFirstLevel(){return 0;}
	public int trainsAtFirstLevel(){return 0;}
	public boolean playerSelectable(){return false;}

	public int shortestMale=24;
	public int shortestMale(){return shortestMale;}
	public int shortestFemale=24;
	public int shortestFemale(){return shortestFemale;}
	public int heightVariance=5;
	public int heightVariance(){return heightVariance;}
	public int lightestWeight=60;
	public int lightestWeight(){return lightestWeight;}
	public int weightVariance=10;
	public int weightVariance(){return weightVariance;}
	public long forbiddenWornBits=0;
	public long forbiddenWornBits(){return forbiddenWornBits;}
	private String racialCategory="Unknown";
	public String racialCategory(){return racialCategory;}
	public boolean isGeneric(){return true;}

	//                   an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private int[] parts={-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1};
	public int[] bodyMask(){return parts;}

	protected CharStats setStats=null;
	protected CharStats adjStats=null;
	protected EnvStats adjEStats=null;
	protected CharState adjState=null;
	protected Weapon naturalWeapon=null;
	protected Vector naturalWeaponChoices=null;
	protected Vector resourceChoices=null;
	protected Race healthBuddy=null;

	public Race copyOf()
	{
		try
		{
			GenRace E=(GenRace)this.clone();
			return E;

		}
		catch(CloneNotSupportedException e)
		{
			return this;
		}
	}

	private String arriveStr="arrives";
	public String arriveStr()
	{
		return arriveStr;
	}
	private String leaveStr="leaves";
	public String leaveStr()
	{
		return leaveStr;
	}
	public String healthText(MOB mob)
	{
		if(healthBuddy()!=null) return healthBuddy.healthText(mob);
		return CommonStrings.standardMobCondition(mob);
	}

	/** some general statistics about such an item
	 * see class "EnvStats" for more information. */
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		if(adjEStats!=null)
		{
			affectableStats.setAbility(affectableStats.ability()+adjEStats.ability());
			affectableStats.setArmor(affectableStats.armor()+adjEStats.armor());
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+adjEStats.attackAdjustment());
			affectableStats.setDamage(affectableStats.damage()+adjEStats.damage());
			affectableStats.setDisposition(affectableStats.disposition()|adjEStats.disposition());
			affectableStats.setHeight(affectableStats.height()+adjEStats.height());
			affectableStats.setLevel(affectableStats.level()+adjEStats.level());
			affectableStats.setSensesMask(affectableStats.sensesMask()|adjEStats.sensesMask());
			affectableStats.setSpeed(affectableStats.speed()+adjEStats.speed());
			affectableStats.setWeight(affectableStats.weight()+adjEStats.weight());
		}
	}
	public void affectCharStats(MOB affectedMob, CharStats affectableStats)
	{
		if(adjStats!=null)
			for(int i=0;i<CharStats.NUM_STATS;i++)
				affectableStats.setStat(i,affectableStats.getStat(i)+adjStats.getStat(i));
		if(setStats!=null)
			for(int i=0;i<CharStats.NUM_STATS;i++)
				affectableStats.setStat(i,setStats.getStat(i));
	}
	public void affectCharState(MOB affectedMob, CharState affectableMaxState)
	{
		if(adjState!=null)
		{
			affectableMaxState.setFatigue(affectableMaxState.getFatigue()+adjState.getFatigue());
			affectableMaxState.setHitPoints(affectableMaxState.getHitPoints()+adjState.getHitPoints());
			affectableMaxState.setHunger(affectableMaxState.getHunger()+adjState.getHunger());
			affectableMaxState.setMana(affectableMaxState.getMana()+adjState.getMana());
			affectableMaxState.setMovement(affectableMaxState.getMovement()+adjState.getMovement());
			affectableMaxState.setThirst(affectableMaxState.getThirst()+adjState.getThirst());
		}
	}
	public Vector myResources(){
		if(resourceChoices==null)
			return new Vector();
		return resourceChoices;
	}

	public String racialParms()
	{
		StringBuffer str=new StringBuffer("");
		str.append("<RACE ID=\""+ID()+"\">");
		str.append(XMLManager.convertXMLtoTag("NAME",name()));
		str.append(XMLManager.convertXMLtoTag("CAT",racialCategory()));
		str.append(XMLManager.convertXMLtoTag("MHEIGHT",""+shortestMale()));
		str.append(XMLManager.convertXMLtoTag("FHEIGHT",""+shortestFemale()));
		str.append(XMLManager.convertXMLtoTag("VHEIGHT",""+heightVariance()));
		str.append(XMLManager.convertXMLtoTag("BWEIGHT",""+lightestWeight()));
		str.append(XMLManager.convertXMLtoTag("VWEIGHT",""+weightVariance()));
		str.append(XMLManager.convertXMLtoTag("WEAR",""+forbiddenWornBits()));
		str.append(XMLManager.convertXMLtoTag("BODY",""+bodyMask().toString()));
		str.append(XMLManager.convertXMLtoTag("HEALTHRACE",(healthBuddy()!=null)?healthBuddy().ID():""));
		str.append(XMLManager.convertXMLtoTag("ARRIVE",arriveStr()));
		str.append(XMLManager.convertXMLtoTag("LEAVE",leaveStr()));
		if(adjEStats==null) str.append("<ESTATS/>");
		else
			str.append(XMLManager.convertXMLtoTag("ESTATS",Generic.getEnvStatsStr(adjEStats)));
		if(adjStats==null) str.append("<ASTATS/>");
		else
			str.append(XMLManager.convertXMLtoTag("ASTATS",Generic.getCharStatsStr(adjStats)));
		if(setStats==null) str.append("<CSTATS/>");
		else
			str.append(XMLManager.convertXMLtoTag("CSTATS",Generic.getCharStatsStr(setStats)));
		if(adjState==null) str.append("<ASTATE/>");
		else
			str.append(XMLManager.convertXMLtoTag("ASTATE",Generic.getCharStateStr(adjState)));
		
		if(myResources().size()==0)	str.append("<RESOURCES/>");
		else
		{
			str.append("<RESOURCES>");
			for(int i=0;i<myResources().size();i++)
			{
				Item I=(Item)myResources().elementAt(i);
				str.append("<RSCITEM>");
				str.append(XMLManager.convertXMLtoTag("ICLASS",CMClass.className(I)));
				str.append(XMLManager.convertXMLtoTag("IDATA",Generic.getPropertiesStr(I,true)));
				str.append("</RSCITEM>");
			}
			str.append("</RESOURCES>");
		}
		if(naturalWeapon==null) str.append("<WEAPON/>");
		else
		{
			str.append("<WEAPON>");
			str.append(XMLManager.convertXMLtoTag("ICLASS",CMClass.className(myNaturalWeapon())));
			str.append(XMLManager.convertXMLtoTag("IDATA",Generic.getPropertiesStr(myNaturalWeapon(),true)));
			str.append("</WEAPON>");
		}
		str.append("</RACE>");
		return str.toString();
	}
	public void setRacialParms(String parms)
	{
	}
	protected static String[] CODES={"CLASS","PARMS"};
	public String getStat(String code){
		switch(getCodeNum(code))
		{
		case 0: return ID();
		case 1: return ""+racialParms();
		}
		return "";
	}
	public void setStat(String code, String val)
	{
		switch(getCodeNum(code))
		{
		case 0: return;
		case 1: setRacialParms(val); break;
		}
	}
	public String[] getStatCodes(){return CODES;}
	protected int getCodeNum(String code){
		for(int i=0;i<CODES.length;i++)
			if(code.equalsIgnoreCase(CODES[i])) return i;
		return -1;
	}
	public boolean sameAs(Race E)
	{
		if(!(E instanceof GenRace)) return false;
		for(int i=0;i<CODES.length;i++)
			if(!E.getStat(CODES[i]).equals(getStat(CODES[i])))
				return false;
		return true;
	}
}
