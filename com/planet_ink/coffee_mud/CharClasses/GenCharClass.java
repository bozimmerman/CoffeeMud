package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class GenCharClass extends StdCharClass
{
	protected String ID="GenCharClass";
	protected String name="genmob";
	protected String baseClass="Commoner";
	protected int minHitPointsLevel=2;
	protected int maxHitPointsLevel=12;
	protected int bonusPracLevel=0;
	protected int bonusManaLevel=15;
	protected int bonusAttackLevel=1;
	protected int attackAttribute=CharStats.STRENGTH;
	protected int pracsFirstLevel=5;
	protected int trainsFirstLevel=3;
	protected int levelsPerBonusDamage=1;
	protected int movementMultiplier=5;
	protected int allowedArmorLevel=CharClass.ARMOR_ANY;
	protected String otherLimitations="";
	protected String otherBonuses="";
	protected String qualifications="";
	protected boolean playerSelectable=false;
	protected HashSet disallowedWeapons=null; // set of Integers for weapon classes
	protected CharStats setStats=null;
	protected CharStats adjStats=null;
	protected EnvStats adjEStats=null;
	protected CharState adjState=null;
	//protected Vector outfitChoices=null; from stdcharclass -- but don't forget them!
	
	public boolean isGeneric(){return true;}
	public String ID(){return ID;}
	public String name(){return name;}
	public String baseClass(){return baseClass;}
	public int getMinHitPointsLevel(){return minHitPointsLevel;}
	public int getMaxHitPointsLevel(){return maxHitPointsLevel;}
	public int getBonusPracLevel(){return bonusPracLevel;}
	public int getBonusManaLevel(){return bonusManaLevel;}
	public int getBonusAttackLevel(){return bonusAttackLevel;}
	public int getAttackAttribute(){return attackAttribute;}
	public int getPracsFirstLevel(){return pracsFirstLevel;}
	public int getTrainsFirstLevel(){return trainsFirstLevel;}
	public int getLevelsPerBonusDamage(){ return levelsPerBonusDamage;}
	public int getMovementMultiplier(){return movementMultiplier;}
	public int allowedArmorLevel(){return allowedArmorLevel;}
	public String otherLimitations(){return otherLimitations;}
	public String otherBonuses(){return otherBonuses;}
	public boolean playerSelectable(){	return playerSelectable;}
	
	public String weaponLimitations()
	{
		if((disallowedWeapons==null)||(disallowedWeapons.size()==0))
			return "No limitations.";
		StringBuffer str=new StringBuffer("The following weapon types may not be used:");
		for(Iterator i=disallowedWeapons.iterator();i.hasNext();)
		{
			Integer I=(Integer)i.next();
			str.append(Weapon.classifictionDescription[I.intValue()]);
		}
		return str.toString();
	}
	public String armorLimitations()
	{ return ARMOR_LONGDESC[allowedArmorLevel];}

	public void cloneFix(CharClass C)
	{
	}

	public CharClass copyOf()
	{
		try
		{
			GenCharClass E=(GenCharClass)this.clone();
			E.cloneFix(this);
			return E;

		}
		catch(CloneNotSupportedException e)
		{
			return this;
		}
	}

	public boolean loaded(){return true;}
	public void setLoaded(boolean truefalse){};

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!(myHost instanceof MOB)) 
			return super.okMessage(myHost,msg);
		MOB myChar=(MOB)myHost;
		if(msg.amISource(myChar)
		&&(!myChar.isMonster()))
		{
			if((allowedArmorLevel()!=CharClass.ARMOR_ANY)
			&&(msg.tool()!=null)
			&&(msg.tool() instanceof Ability)
			&&(CMAble.getQualifyingLevel(ID(),msg.tool().ID())>0)
			&&(myChar.isMine(msg.tool()))
			&&(!armorCheck(myChar)))
			{
				if(Dice.rollPercentage()>myChar.charStats().getStat(getAttackAttribute())*2)
				{
					myChar.location().show(myChar,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> fumble(s) <S-HIS-HER> "+msg.tool().name()+" attempt due to <S-HIS-HER> armor!");
					return false;
				}
			}
			if((msg.sourceMinor()==CMMsg.TYP_WEAPONATTACK)
			&&(disallowedWeapons!=null)
			&&(msg.tool()!=null)
			&&(msg.tool() instanceof Weapon))
			{
				if(((disallowedWeapons.contains(new Integer(((Weapon)msg.tool()).weaponClassification())))
				&&(myChar.fetchWieldedItem()!=null)))
				{
					if(Dice.rollPercentage()>myChar.charStats().getStat(getAttackAttribute())*2)
					{
						myChar.location().show(myChar,null,CMMsg.MSG_OK_ACTION,"<S-NAME> fumble(s) horribly with "+msg.tool().name()+".");
						return false;
					}
				}
			}
		}
		return super.okMessage(myChar,msg);
	}
	
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(!super.qualifiesForThisClass(mob,quiet))
			return false;
		if((!mob.isMonster())&&(mob.baseEnvStats().level()>0))
		{
			if(!MUDZapper.zapperCheck(qualifications,mob))
			{
				if(!quiet)
					mob.tell("You must meet the following qualifications to be a "+name()+":\n"+statQualifications());
				return false;
			}
		}
		return true;
	}
	public String statQualifications(){return MUDZapper.zapperDesc(qualifications);}

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
				if(setStats.getStat(i)!=0)
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
	
	public String classParms()
	{
		StringBuffer str=new StringBuffer("");
		str.append("<CCLASS><ID>"+ID()+"</ID>");
		str.append(XMLManager.convertXMLtoTag("NAME",name()));
		str.append(XMLManager.convertXMLtoTag("BASE",baseClass()));
		str.append(XMLManager.convertXMLtoTag("LVLMINHP",""+minHitPointsLevel));
		str.append(XMLManager.convertXMLtoTag("LVLMAXHP",""+maxHitPointsLevel));
		str.append(XMLManager.convertXMLtoTag("LVLPRAC",""+bonusPracLevel));
		str.append(XMLManager.convertXMLtoTag("LVLMANA",""+bonusManaLevel));
		str.append(XMLManager.convertXMLtoTag("LVLATT",""+bonusAttackLevel));
		str.append(XMLManager.convertXMLtoTag("ATTATT",""+attackAttribute));
		str.append(XMLManager.convertXMLtoTag("FSTPRAC",""+pracsFirstLevel));
		str.append(XMLManager.convertXMLtoTag("FSTTRAN",""+trainsFirstLevel));
		str.append(XMLManager.convertXMLtoTag("LVLDAM",""+levelsPerBonusDamage));
		str.append(XMLManager.convertXMLtoTag("LVLMOVE",""+movementMultiplier));
		str.append(XMLManager.convertXMLtoTag("ARMOR",""+allowedArmorLevel));
		//str.append(XMLManager.convertXMLtoTag("STRWEAP",weaponLimitations));
		//str.append(XMLManager.convertXMLtoTag("STRARM",armorLimitations));
		str.append(XMLManager.convertXMLtoTag("STRLMT",otherLimitations));
		str.append(XMLManager.convertXMLtoTag("STRBON",otherBonuses));
		str.append(XMLManager.convertXMLtoTag("QUAL",qualifications));
		str.append(XMLManager.convertXMLtoTag("PLAYER",""+playerSelectable));
		if(adjEStats==null) str.append("<ESTATS/>");
		else
			str.append(XMLManager.convertXMLtoTag("ESTATS",CoffeeMaker.getEnvStatsStr(adjEStats)));
		if(adjStats==null) str.append("<ASTATS/>");
		else
			str.append(XMLManager.convertXMLtoTag("ASTATS",CoffeeMaker.getCharStatsStr(adjStats)));
		if(setStats==null) str.append("<CSTATS/>");
		else
			str.append(XMLManager.convertXMLtoTag("CSTATS",CoffeeMaker.getCharStatsStr(setStats)));
		if(adjState==null) str.append("<ASTATE/>");
		else
			str.append(XMLManager.convertXMLtoTag("ASTATE",CoffeeMaker.getCharStateStr(adjState)));
		
		DVector ables=getAbleSet();
		if((ables==null)||(ables.size()==0))
			str.append("<CABILITIES/>");
		else
		{
			str.append("<CABILITIES>");
			for(int r=0;r<ables.size();r++)
			{
				str.append("<CABILITY>");
				str.append("<CACLASS>"+ables.elementAt(r,1)+"</CACLASS>");
				str.append("<CALEVEL>"+ables.elementAt(r,2)+"</CALEVEL>");
				str.append("<CAPROFF>"+ables.elementAt(r,3)+"</CAPROFF>");
				str.append("<CAAGAIN>"+ables.elementAt(r,4)+"</CAAGAIN>");
				str.append("<CASECR>"+ables.elementAt(r,5)+"</CASECR>");
				str.append("<CAPARM>"+ables.elementAt(r,6)+"</CAPARM>");
				str.append("</CABILITY>");
			}
			str.append("</CABILITIES>");
		}
		
		if((disallowedWeapons==null)||(disallowedWeapons.size()==0))	
			str.append("<NOWEAPS/>");
		else
		{
			str.append("<NOWEAPS>");
			for(Iterator i=disallowedWeapons.iterator();i.hasNext();)
			{
				Integer I=(Integer)i.next();
				str.append(XMLManager.convertXMLtoTag("WCLASS",""+I.intValue()));
			}
			str.append("</NOWEAPS>");
		}
		if((outfit()==null)||(outfit().size()==0))	str.append("<OUTFIT/>");
		else
		{
			str.append("<OUTFIT>");
			for(int i=0;i<outfit().size();i++)
			{
				Item I=(Item)outfit().elementAt(i);
				str.append("<OFTITEM>");
				str.append(XMLManager.convertXMLtoTag("OFCLASS",CMClass.className(I)));
				str.append(XMLManager.convertXMLtoTag("OFDATA",CoffeeMaker.parseOutAngleBrackets(I.text())));
				str.append("</OFTITEM>");
			}
			str.append("</OUTFIT>");
		}
		str.append("</CCLASS>");
		return str.toString();
	}
	public void setClassParms(String parms)
	{
		if(parms.trim().length()==0) return;
		Vector xml=XMLManager.parseAllXML(parms);
		if(xml==null)
		{
			Log.errOut("GenCharClass","Unable to parse: "+parms);
			return;
		}
		Vector classData=XMLManager.getRealContentsFromPieces(xml,"CCLASS");
		if(classData==null){	Log.errOut("GenCharClass","Unable to get CCLASS data."); return;}
		String id=XMLManager.getValFromPieces(classData,"ID");
		if(id.length()==0) return;
		ID=id;
		name=XMLManager.getValFromPieces(classData,"NAME");
		String base=XMLManager.getValFromPieces(classData,"BASE");
		if((base==null)||(base.length()==0))
			return;
		baseClass=base;
		minHitPointsLevel=XMLManager.getIntFromPieces(classData,"LVLMINHP");
		maxHitPointsLevel=XMLManager.getIntFromPieces(classData,"LVLMAXHP");
		bonusPracLevel=XMLManager.getIntFromPieces(classData,"LVLPRAC");
		bonusManaLevel=XMLManager.getIntFromPieces(classData,"LVLMANA");
		bonusAttackLevel=XMLManager.getIntFromPieces(classData,"LVLATT");
		attackAttribute=XMLManager.getIntFromPieces(classData,"ATTATT");
		trainsFirstLevel=XMLManager.getIntFromPieces(classData,"FSTTRAN");
		pracsFirstLevel=XMLManager.getIntFromPieces(classData,"FSTPRAC");
		levelsPerBonusDamage=XMLManager.getIntFromPieces(classData,"LVLDAM");
		movementMultiplier=XMLManager.getIntFromPieces(classData,"LVLMOVE");
		allowedArmorLevel=XMLManager.getIntFromPieces(classData,"ARMOR");
		//weaponLimitations=XMLManager.getValFromPieces(classData,"STRWEAP");
		//armorLimitations=XMLManager.getValFromPieces(classData,"STRARM");
		otherLimitations=XMLManager.getValFromPieces(classData,"STRLMT");
		otherBonuses=XMLManager.getValFromPieces(classData,"STRBON");
		qualifications=XMLManager.getValFromPieces(classData,"QUAL");
		playerSelectable=XMLManager.getBoolFromPieces(classData,"PLAYER");
		adjEStats=null;
		String eStats=XMLManager.getValFromPieces(classData,"ESTATS");
		if(eStats.length()>0){ adjEStats=new DefaultEnvStats(); CoffeeMaker.setEnvStats(adjEStats,eStats);}
		adjStats=null;
		String aStats=XMLManager.getValFromPieces(classData,"ASTATS");
		if(aStats.length()>0){ adjStats=new DefaultCharStats(); CoffeeMaker.setCharStats(adjStats,aStats);}
		setStats=null;
		String cStats=XMLManager.getValFromPieces(classData,"CSTATS");
		if(cStats.length()>0){ setStats=new DefaultCharStats(); CoffeeMaker.setCharStats(setStats,cStats);}
		adjState=null;
		String aState=XMLManager.getValFromPieces(classData,"ASTATE");
		if(aState.length()>0){ adjState=new DefaultCharState(); CoffeeMaker.setCharState(adjState,aState);}

		Vector xV=XMLManager.getRealContentsFromPieces(classData,"CABILITIES");
		CMAble.delCharMappings(ID());
		if((xV!=null)&&(xV.size()>0))
			for(int x=0;x<xV.size();x++)
			{
				XMLManager.XMLpiece iblk=(XMLManager.XMLpiece)xV.elementAt(x);
				if((!iblk.tag.equalsIgnoreCase("CABILITY"))||(iblk.contents==null))
					continue;
				CMAble.addCharAbilityMapping(ID(),
											 XMLManager.getIntFromPieces(iblk.contents,"CALEVEL"),
											 XMLManager.getValFromPieces(iblk.contents,"CACLASS"),
											 XMLManager.getIntFromPieces(iblk.contents,"CAPROFF"),
											 XMLManager.getValFromPieces(iblk.contents,"CAPARM"),
											 XMLManager.getBoolFromPieces(iblk.contents,"CAAGAIN"),
											 XMLManager.getBoolFromPieces(iblk.contents,"CASECR"));
			}
		
		// now WEAPON RESTRICTIONS!
		xV=XMLManager.getRealContentsFromPieces(classData,"NOWEAPS");
		disallowedWeapons=null;
		if((xV!=null)&&(xV.size()>0))
		{
			disallowedWeapons=new HashSet();
			for(int x=0;x<xV.size();x++)
			{
				XMLManager.XMLpiece iblk=(XMLManager.XMLpiece)xV.elementAt(x);
				if((!iblk.tag.equalsIgnoreCase("WCLASS"))||(iblk.contents==null))
					continue;
				disallowedWeapons.add(new Integer(Util.s_int(iblk.value)));
			}
		}
		
		// now OUTFIT!
		Vector oV=XMLManager.getRealContentsFromPieces(classData,"OUTFIT");
		outfitChoices=null;
		if((oV!=null)&&(oV.size()>0))
		{
			outfitChoices=new Vector();
			for(int x=0;x<oV.size();x++)
			{
				XMLManager.XMLpiece iblk=(XMLManager.XMLpiece)oV.elementAt(x);
				if((!iblk.tag.equalsIgnoreCase("OFTITEM"))||(iblk.contents==null))
					continue;
				Item newOne=CMClass.getItem(XMLManager.getValFromPieces(iblk.contents,"OFCLASS"));
				String idat=XMLManager.getValFromPieces(iblk.contents,"OFDATA");
				newOne.setMiscText(CoffeeMaker.restoreAngleBrackets(idat));
				newOne.recoverEnvStats();
				outfitChoices.addElement(newOne);
			}
		}
	}
	
	protected DVector getAbleSet()
	{
		DVector VA=new DVector(6);
		Vector V=CMAble.getUpToLevelListings(ID(),Integer.MAX_VALUE,true,false);
		for(int v=0;v<V.size();v++)
		{
			String AID=(String)V.elementAt(v);
			VA.addElement(AID,
						  ""+CMAble.getQualifyingLevel(ID(),AID),
						  ""+CMAble.getDefaultProfficiency(ID(),AID),
						  ""+CMAble.getDefaultGain(ID(),AID),
						  ""+CMAble.getSecretSkill(ID(),AID),
						  ""+CMAble.getDefaultParm(ID(),AID));
		}
		return VA;
	}
	
	protected static String[] CODES={"ID","NAME","BASE","LVLMINHP","LVLMAXHP",
									 "LVLPRAC","LVLMANA","LVLATT","ATTATT","FSTTRAN",
									 "FSTPRAC","LVLDAM","LVLMOVE","ARMOR","STRWEAP",
									 "STRARM","STRLMT","STRBON","QUAL","PLAYER",
									 "ESTATS","ASTATS","CSTATS","ASTATE","NUMCABLE",
									 "GETCABLE","GETCABLELVL","GETCABLEPROF","GETCABLEGAIN","GETCABLESECR",
									 "GETCABLEPARM","NUMWEP","GETWEP", "NUMOFT","GETOFTID",
									 "GETOFTPARM"
									 };
	public String getStat(String code){
		int num=0;
		while((code.length()>0)&&(Character.isDigit(code.charAt(code.length()-1))))
		{
			num=(num*10)+Util.s_int(""+code.charAt(code.length()-1));
			code=code.substring(0,code.length()-1);
		}
		switch(getCodeNum(code))
		{
		case 0: return ID;
		case 1: return name;
		case 2: return baseClass;
		case 3: return ""+minHitPointsLevel;
		case 4: return ""+maxHitPointsLevel;
		case 5: return ""+bonusPracLevel;
		case 6: return ""+bonusManaLevel;
		case 7: return ""+bonusAttackLevel;
		case 8: return ""+attackAttribute;
		case 9: return ""+trainsFirstLevel;
		case 10: return ""+pracsFirstLevel;
		case 11: return ""+levelsPerBonusDamage;
		case 12: return ""+movementMultiplier;
		case 13: return ""+allowedArmorLevel;
		case 14: return "";//weaponLimitations;
		case 15: return "";//armorLimitations;
		case 16: return otherLimitations;
		case 17: return otherBonuses;
		case 18: return qualifications;
		case 19: return ""+playerSelectable;
		case 20: return (adjEStats==null)?"":CoffeeMaker.getEnvStatsStr(adjEStats);
		case 21: return (adjStats==null)?"":CoffeeMaker.getCharStatsStr(adjStats);
		case 22: return (setStats==null)?"":CoffeeMaker.getCharStatsStr(setStats);
		case 23: return (adjState==null)?"":CoffeeMaker.getCharStateStr(adjState);
		case 24: return ""+getAbleSet().size();
		case 25: return (String)getAbleSet().elementAt(num,1);
		case 26: return (String)getAbleSet().elementAt(num,2);
		case 27: return (String)getAbleSet().elementAt(num,3);
		case 28: return (String)getAbleSet().elementAt(num,4);
		case 29: return (String)getAbleSet().elementAt(num,5);
		case 30: return (String)getAbleSet().elementAt(num,6);
		case 31: return ""+((disallowedWeapons!=null)?disallowedWeapons.size():0);
		case 32: return Util.toStringList(disallowedWeapons);
		case 33: return ""+((outfit()!=null)?outfit().size():0);
		case 34: return ""+((outfit()!=null)?((Item)outfit().elementAt(num)).ID():"");
		case 35: return ""+((outfit()!=null)?((Item)outfit().elementAt(num)).text():"");
		}
		return "";
	}
	protected String[] tempables=new String[6];
	public void setStat(String code, String val)
	{
		int num=0;
		while((code.length()>0)&&(Character.isDigit(code.charAt(code.length()-1))))
		{
			num=(num*10)+Util.s_int(""+code.charAt(code.length()-1));
			code=code.substring(0,code.length()-1);
		}
		switch(getCodeNum(code))
		{
		case 0: ID=val; break;
		case 1: name=val; break;
		case 2: baseClass=val; break;
		case 3: minHitPointsLevel=Util.s_int(val); break;
		case 4: maxHitPointsLevel=Util.s_int(val); break;
		case 5: bonusPracLevel=Util.s_int(val); break;
		case 6: bonusManaLevel=Util.s_int(val); break;
		case 7: bonusAttackLevel=Util.s_int(val); break;
		case 8: attackAttribute=Util.s_int(val); break;
		case 9: trainsFirstLevel=Util.s_int(val); break;
		case 10: pracsFirstLevel=Util.s_int(val); break;
		case 11: levelsPerBonusDamage=Util.s_int(val); break;
		case 12: movementMultiplier=Util.s_int(val); break;
		case 13: allowedArmorLevel=Util.s_int(val); break;
		case 14: break;//weaponLimitations=val;break;
		case 15: break;//armorLimitations=val;break;
		case 16: otherLimitations=val;break;
		case 17: otherBonuses=val;break;
		case 18: qualifications=val;break;
		case 19: playerSelectable=Util.s_bool(val); break;
		case 20: adjEStats=null;if(val.length()>0){adjEStats=new DefaultEnvStats(0); CoffeeMaker.setEnvStats(adjEStats,val);}break;
		case 21: adjStats=null;if(val.length()>0){adjStats=new DefaultCharStats(0); CoffeeMaker.setCharStats(adjStats,val);}break;
		case 22: setStats=null;if(val.length()>0){setStats=new DefaultCharStats(0); CoffeeMaker.setCharStats(setStats,val);}break;
		case 23: adjState=null;if(val.length()>0){adjState=new DefaultCharState(0); CoffeeMaker.setCharState(adjState,val);}break;
		case 24: CMAble.delCharMappings(ID()); break;
		case 25: tempables[0]=val; break;
		case 26: tempables[1]=val; break;
		case 27: tempables[2]=val; break;
		case 28: tempables[3]=val; break;
		case 29: tempables[4]=val; break;
		case 30: CMAble.addCharAbilityMapping(ID(),
											  Util.s_int(tempables[1]),
											  tempables[0],
											  Util.s_int(tempables[2]),
											  val,
											  Util.s_bool(tempables[3]),
											  Util.s_bool(tempables[4]));
				break;
		case 31: if(Util.s_int(val)==0) 
					 disallowedWeapons=null; 
				 else 
					 disallowedWeapons=new HashSet();
				 break;
		case 32: Vector V=Util.parseCommas(val,true);
				 if(V.size()>0)
				 {
					disallowedWeapons=new HashSet();
					for(int v=0;v<V.size();v++)
						disallowedWeapons.add(new Integer(Util.s_int((String)V.elementAt(v))));
				 }
				 else
					 disallowedWeapons=null;
				 break;
		case 33: if(Util.s_int(val)==0) outfitChoices=null; break;
		case 34: {   if(outfitChoices==null) outfitChoices=new Vector();
					 if(num>=outfitChoices.size())
						outfitChoices.addElement(CMClass.getItem(val));
					 else
				        outfitChoices.setElementAt(CMClass.getItem(val),num);
					 break;
				 }
		case 35: {   if((outfitChoices!=null)&&(num<outfitChoices.size()))
					 {
						Item I=(Item)outfitChoices.elementAt(num);
						I.setMiscText(val);
						I.recoverEnvStats();
					 }
					 break;
				 }
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
	public boolean sameAs(CharClass E)
	{
		if(!(E instanceof GenCharClass)) return false;
		if(((CharClass)E).classParms().equals(classParms()))
			return true;
		return false;
	}
}
