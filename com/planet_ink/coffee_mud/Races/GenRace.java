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
	public boolean playerSelectable=false;
	public boolean playerSelectable(){return playerSelectable;}

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
		str.append(XMLManager.convertXMLtoTag("PLAYER",""+playerSelectable));
		StringBuffer bbody=new StringBuffer("");
		for(int i=0;i<bodyMask().length;i++)
			bbody.append(bodyMask()[i]+';');
		str.append(XMLManager.convertXMLtoTag("BODY",bbody.toString()));
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
		if(parms.trim().length()==0) return;
		Vector xml=XMLManager.parseAllXML(parms);
		if(xml==null)
		{
			Log.errOut("GenRace","Unable to parse: "+parms);
			return;
		}
		Vector raceData=XMLManager.getRealContentsFromPieces(xml,"RACE");
		if(raceData==null){	Log.errOut("GenRace","Unable to get RACE data."); return;}
		String id=XMLManager.returnXMLParm(parms,"ID");
		if(id.length()==0) return;
		ID=id;
		name=XMLManager.getValFromPieces(raceData,"NAME");
		racialCategory=XMLManager.getValFromPieces(raceData,"CAT");
		forbiddenWornBits=XMLManager.getLongFromPieces(raceData,"WEAR");
		weightVariance=XMLManager.getIntFromPieces(raceData,"VWEIGHT");
		lightestWeight=XMLManager.getIntFromPieces(raceData,"BWEIGHT");
		heightVariance=XMLManager.getIntFromPieces(raceData,"VHEIGHT");
		shortestFemale=XMLManager.getIntFromPieces(raceData,"FHEIGHT");
		shortestMale=XMLManager.getIntFromPieces(raceData,"MHEIGHT");
		playerSelectable=XMLManager.getBoolFromPieces(raceData,"PLAYER");
		leaveStr=XMLManager.getValFromPieces(raceData,"LEAVE");
		arriveStr=XMLManager.getValFromPieces(raceData,"ARRIVE");
		healthBuddy=CMClass.getRace(XMLManager.getValFromPieces(raceData,"HEALTHRACE"));
		String body=XMLManager.getValFromPieces(raceData,"BODY");
		Vector V=Util.parseSemicolons(body);
		for(int v=0;v<V.size();v++)
			if(v<bodyMask().length)
				bodyMask()[v]=Util.s_int((String)V.elementAt(v));
		adjEStats=null;
		String eStats=XMLManager.getValFromPieces(raceData,"ESTATS");
		if(eStats.length()>0){ adjEStats=new DefaultEnvStats(); Generic.setEnvStats(adjEStats,eStats);}
		adjStats=null;
		String aStats=XMLManager.getValFromPieces(raceData,"ASTATS");
		if(aStats.length()>0){ adjStats=new DefaultCharStats(); Generic.setCharStats(adjStats,aStats);}
		setStats=null;
		String cStats=XMLManager.getValFromPieces(raceData,"CSTATS");
		if(cStats.length()>0){ setStats=new DefaultCharStats(); Generic.setCharStats(setStats,cStats);}
		adjState=null;
		String aState=XMLManager.getValFromPieces(raceData,"ASTATE");
		if(aState.length()>0){ adjState=new DefaultCharState(); Generic.setCharState(adjState,aState);}

		// now RESOURCES!
		Vector xV=XMLManager.getRealContentsFromPieces(xml,"RESOURCES");
		resourceChoices=null;
		if((xV!=null)&&(xV.size()>0))
		{
			resourceChoices=new Vector();
			for(int x=0;x<xV.size();x++)
			{
				XMLManager.XMLpiece iblk=(XMLManager.XMLpiece)xV.elementAt(x);
				if((!iblk.tag.equalsIgnoreCase("RSCITEM"))||(iblk.contents==null))
					continue;
				Item newOne=CMClass.getItem(XMLManager.getValFromPieces(iblk.contents,"ICLASS"));
				Vector idat=XMLManager.getRealContentsFromPieces(iblk.contents,"IDATA");
				if((idat==null)||(newOne==null)) continue;
				Generic.setPropertiesStr(newOne,idat,true);
				newOne.recoverEnvStats();
				resourceChoices.addElement(newOne);
			}
		}
		naturalWeapon=null;
		XMLManager.XMLpiece iblk=XMLManager.getPieceFromPieces(xml,"WEAPON");
		if((!iblk.tag.equalsIgnoreCase("WEAPON"))&&(iblk.contents!=null))
		{
			naturalWeapon=(Weapon)CMClass.getItem(XMLManager.getValFromPieces(iblk.contents,"ICLASS"));
			Vector idat=XMLManager.getRealContentsFromPieces(iblk.contents,"IDATA");
			if((idat!=null)&&(naturalWeapon!=null))
			{
				Generic.setPropertiesStr(naturalWeapon,idat,true);
				naturalWeapon.recoverEnvStats();
			}
		}
	}
	protected static String[] CODES={"CLASS","PARMS","CAT","WEAR","VWEIGHT","BWEIGHT",
									 "VHEIGHT","FHEIGHT","MHEIGHT","PLAYER","LEAVE",
									 "ARRIVE","HEALTHRACE","BODY","ESTATS",
									 "ASTATS","CSTATS","ASTATE",
									 "NUMRSC","GETRSCID","GETRSCPARM",
									 "WEAPONCLASS","WEAPONXML"
									 };
	public String getStat(String code){
		int num=0;
		while((code.length()>0)&&(Character.isDigit(code.charAt(code.length()-1))))
		{
			num=(num*10)+code.charAt(code.length()-1);
			code=code.substring(0,code.length()-1);
		}
		switch(getCodeNum(code))
		{
		case 0: return ID();
		case 1: return ""+racialParms();
		case 2: return racialCategory;
		case 3: return ""+forbiddenWornBits();
		case 4: return ""+weightVariance();
		case 5: return ""+lightestWeight();
		case 6: return ""+heightVariance();
		case 7: return ""+shortestFemale();
		case 8: return ""+shortestMale();
		case 9: return ""+playerSelectable();
		case 10: return leaveStr();
		case 11: return arriveStr();
		case 12: return ((healthBuddy==null)?"":healthBuddy.ID());
		case 13: 
		{
			StringBuffer bbody=new StringBuffer("");
			for(int i=0;i<bodyMask().length;i++)
				bbody.append(bodyMask()[i]+';');
			return bbody.toString();
		}
		case 14: return (adjEStats==null)?"":Generic.getEnvStatsStr(adjEStats);
		case 15: return (adjStats==null)?"":Generic.getCharStatsStr(adjStats);
		case 16: return (setStats==null)?"":Generic.getCharStatsStr(setStats);
		case 17: return (adjState==null)?"":Generic.getCharStateStr(adjState);
		case 18: return ""+myResources().size();
		case 19: return ""+((Item)myResources().elementAt(num)).ID();
		case 20: return ""+((Item)myResources().elementAt(num)).text();
		case 21: return (naturalWeapon==null)?"":naturalWeapon.ID();
		case 22: return (naturalWeapon==null)?"":naturalWeapon.text();
		}
		return "";
	}
	public void setStat(String code, String val)
	{
		int num=0;
		while((code.length()>0)&&(Character.isDigit(code.charAt(code.length()-1))))
		{
			num=(num*10)+code.charAt(code.length()-1);
			code=code.substring(0,code.length()-1);
		}
		switch(getCodeNum(code))
		{
		case 0: ID=val; break;
		case 1: setRacialParms(val); break;
		case 2: racialCategory=val; break;
		case 3: forbiddenWornBits=Util.s_long(val); break;
		case 4: weightVariance=Util.s_int(val); break;
		case 5: lightestWeight=Util.s_int(val); break;
		case 6: heightVariance=Util.s_int(val); break;
		case 7: shortestFemale=Util.s_int(val); break;
		case 8: shortestMale=Util.s_int(val); break;
		case 9: playerSelectable=Util.s_bool(val); break;
		case 10: leaveStr=val;break;
		case 11: arriveStr=val;break;
		case 12: healthBuddy=CMClass.getRace(val); break;
		case 13: 
		{
			Vector V=Util.parseSemicolons(val);
			for(int v=0;v<V.size();v++)
				if(v<bodyMask().length)
					bodyMask()[v]=Util.s_int((String)V.elementAt(v));
			break;
		}
		case 14: adjEStats=null;if(val.length()>0){adjEStats=new DefaultEnvStats(0); Generic.setEnvStats(adjEStats,val);}break;
		case 15: adjStats=null;if(val.length()>0){adjStats=new DefaultCharStats(0); Generic.setCharStats(adjStats,val);}break;
		case 16: setStats=null;if(val.length()>0){setStats=new DefaultCharStats(0); Generic.setCharStats(setStats,val);}break;
		case 17: adjState=null;if(val.length()>0){adjState=new DefaultCharState(0); Generic.setCharState(adjState,val);}break;
		case 18: if(val.length()==0) resourceChoices=null; break;
		case 19: {   if(resourceChoices==null) resourceChoices=new Vector();
					 if(num>=resourceChoices.size())
						resourceChoices.addElement(CMClass.getItem(val));
					 else
				        resourceChoices.setElementAt(CMClass.getItem(val),num);
					 break;
				 }
		case 20: {   if((resourceChoices!=null)&&(num<resourceChoices.size()))
					 {
						Item I=(Item)resourceChoices.elementAt(num);
						I.setMiscText(val);
						I.recoverEnvStats();
					 }
					 break;
				 }
		case 21: naturalWeapon=null; 
				 if(val.length()>0) naturalWeapon=CMClass.getWeapon(val);
				 break;
		case 22: if(naturalWeapon!=null){
					 naturalWeapon.setMiscText(val); 
					 naturalWeapon.recoverEnvStats();
				 }
				 break;
		}
	}
	public String[] getStatCodes(){return CODES;}
	protected int getCodeNum(String code){
		while((code.length()>0)&&(Character.isDigit(code.charAt(code.length()-1))))
			code=code.substring(0,code.length()-1);
		for(int i=0;i<CODES.length;i++)
			if(code.equalsIgnoreCase(CODES[i])) return i;
		return -1;
	}
	public boolean sameAs(Race E)
	{
		if(!(E instanceof GenRace)) return false;
		if(((GenRace)E).racialParms().equals(racialParms()))
			return true;
		return false;
	}
}
