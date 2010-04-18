package com.planet_ink.coffee_mud.CharClasses;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.DefaultTimeClock;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;


/*
   Copyright 2000-2010 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
@SuppressWarnings("unchecked")
public class GenCharClass extends StdCharClass
{
	protected String ID="GenCharClass";
    protected Integer[] nameLevels={Integer.valueOf(0)};
	protected String baseClass="Commoner";
	protected int hpDivisor=3;
	protected int hpDice=1;
	protected int hpDie=6;
	protected int manaDivisor=6;
	protected int manaDice=1;
	protected int manaDie=3;
    protected int levelCap=-1;
	protected int bonusPracLevel=0;
	protected int bonusAttackLevel=0;
	protected int attackAttribute=CharStats.STAT_STRENGTH;
	protected int pracsFirstLevel=5;
	protected int trainsFirstLevel=3;
	protected int levelsPerBonusDamage=10;
	protected int movementMultiplier=5;
	protected int allowedArmorLevel=CharClass.ARMOR_ANY;
	protected String otherLimitations="";
	protected String otherBonuses="";
	protected String qualifications="";
    protected String[] xtraValues=null;
	protected int selectability=0;
	
    public int getHPDivisor(){return hpDivisor;}
    public int getHPDice(){return hpDice;}
    public int getHPDie(){return hpDie;}
    public int getManaDivisor(){return manaDivisor;}
    public int getManaDice(){return manaDice;}
    public int getManaDie(){return manaDie;}
    public int getLevelCap() {return levelCap;}
    
	protected int maxNonCraftingSkills=0;
	protected int maxCraftingSkills=0;
	protected int maxCommonSkills=0;
	protected int maxLanguages=0;
	
	public int maxNonCraftingSkills() { return maxNonCraftingSkills;}
	public int maxCraftingSkills() { return maxCraftingSkills;}
	public int maxCommonSkills() { return maxCommonSkills;}
	public int maxLanguages() { return maxLanguages;}
	
    // IS *only* used by stdcharclass for weaponliminatations, buildDisallowedWeaponClasses,  buildRequiredWeaponMaterials
    public int allowedWeaponLevel(){return CharClass.WEAPONS_ANY;}
    
    private HashSet requiredWeaponMaterials=null; // set of Integer material masks
    protected HashSet requiredWeaponMaterials(){return requiredWeaponMaterials;}
    
    protected int requiredArmorSourceMinor=-1;
    public int requiredArmorSourceMinor(){return requiredArmorSourceMinor;}
    
	protected HashSet disallowedWeaponSet=null; // set of Integers for weapon classes
	protected HashSet disallowedWeaponClasses(MOB mob){return disallowedWeaponSet;}
	protected CharStats setStats=null;
	protected CharStats adjStats=null;
	protected EnvStats adjEStats=null;
	protected CharState adjState=null;
	protected CharState startAdjState=null;
    protected CharClass statBuddy=null;
    protected CharClass eventBuddy=null;
	protected int disableFlags=0;
	public boolean raceless(){return (disableFlags&CharClass.GENFLAG_NORACE)==CharClass.GENFLAG_NORACE;}
	public boolean leveless(){return (disableFlags&CharClass.GENFLAG_NOLEVELS)==CharClass.GENFLAG_NOLEVELS;}
	public boolean expless(){return (disableFlags&CharClass.GENFLAG_NOEXP)==CharClass.GENFLAG_NOEXP;}
	//protected Vector outfitChoices=null; from stdcharclass -- but don't forget them!
    protected Vector[] securityGroups={};
    protected Integer[] securityGroupLevels={};
    protected Hashtable securityGroupCache=new Hashtable();
    protected String helpEntry = "";
    
    public Vector getSecurityGroups(int classLevel)
    {
        if(securityGroups.length==0)
            return super.getSecurityGroups(classLevel);
        Vector V=(Vector)securityGroupCache.get(Integer.valueOf(classLevel));
        if(V!=null) return V;
        V=new Vector();
        for(int i=securityGroupLevels.length-1;i>=0;i--)
            if((classLevel>=securityGroupLevels[i].intValue())
            &&(i<securityGroups.length))
                CMParms.addToVector(securityGroups[i],V);
        securityGroupCache.put(Integer.valueOf(classLevel),V);
        return V;
    }


	public boolean isGeneric(){return true;}
	public String ID(){return ID;}
	public String name(){return names[0];}
    public String name(int classLevel)
    {
        for(int i=nameLevels.length-1;i>=0;i--)
            if((classLevel>=nameLevels[i].intValue())
            &&(i<names.length))
                return names[i];
        return names[0];
    }
	public String baseClass(){return baseClass;}
	public int getBonusPracLevel(){return bonusPracLevel;}
	public int getBonusAttackLevel(){return bonusAttackLevel;}
	public int getAttackAttribute(){return attackAttribute;}
	public int getPracsFirstLevel(){return pracsFirstLevel;}
	public int getTrainsFirstLevel(){return trainsFirstLevel;}
	public int getLevelsPerBonusDamage(){ return levelsPerBonusDamage;}
	public int getMovementMultiplier(){return movementMultiplier;}
	public int allowedArmorLevel(){return allowedArmorLevel;}
	public String getOtherLimitsDesc(){return otherLimitations;}
	public String getOtherBonusDesc(){return otherBonuses;}
	public int availabilityCode(){return selectability;}

    public GenCharClass()
    {
        names=new String[1];
        names[0]="genmob";
        xtraValues=CMProps.getExtraStatCodesHolder(this);
    }

	public String getWeaponLimitDesc()
	{
	    StringBuffer str=new StringBuffer("");
		if((disallowedWeaponClasses(null)!=null)&&(disallowedWeaponClasses(null).size()>0))
        {
            str.append("The following weapon types may not be used: ");
    		for(Iterator i=disallowedWeaponClasses(null).iterator();i.hasNext();)
    		{
    			Integer I=(Integer)i.next();
    			str.append(CMStrings.capitalizeAndLower(Weapon.CLASS_DESCS[I.intValue()])+" ");
    		}
            str.append(".  ");
        }
        if((requiredWeaponMaterials()!=null)&&(requiredWeaponMaterials().size()>0))
        {
            str.append("Requires using weapons made of the following materials: ");
            for(Iterator i=requiredWeaponMaterials().iterator();i.hasNext();)
            {
                Integer I=(Integer)i.next();
                str.append(CMStrings.capitalizeAndLower(CMLib.materials().getMaterialDesc(I.intValue()))+" ");
            }
            str.append(".  ");
        }
        
        if(str.length()==0) str.append("No limitations.");
		return str.toString().trim();
	}

	public void cloneFix(CharClass C)
	{
	}

    public CMObject newInstance(){try{return (CMObject)getClass().newInstance();}catch(Exception e){return new GenCharClass();}}
	public CMObject copyOf()
	{
		GenCharClass E=new GenCharClass();
		E.setClassParms(classParms());
		return E;
	}

	public boolean loaded(){return true;}
	public void setLoaded(boolean truefalse){}

	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(!super.qualifiesForThisClass(mob,quiet))
			return false;
		if(mob != null)
		{
			if((!mob.isMonster())&&(mob.baseEnvStats().level()>0))
			{
				if(!CMLib.masking().maskCheck(qualifications,mob,true))
				{
					if(!quiet)
						mob.tell("You must meet the following qualifications to be a "+name()+":\n"+getStatQualDesc());
					return false;
				}
			}
		}
		return true;
	}
	public String getStatQualDesc(){return CMLib.masking().maskDesc(qualifications);}

    protected String getCharClassLocatorID(CharClass C)
    {
        if(C==null) return "";
        if(C.isGeneric()) return C.ID();
        if(C==CMClass.getCharClass(C.ID()))
            return C.ID();
        return C.getClass().getName();
    }
    
    
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
        if(statBuddy!=null)
            statBuddy.affectEnvStats(affected,affectableStats);
	}
    public boolean tick(Tickable myChar, int tickID)
    {
        if(eventBuddy!=null)
            if(!eventBuddy.tick(myChar,tickID))
                return false;
        return super.tick(myChar, tickID);
    }
    public void executeMsg(Environmental myHost, CMMsg msg)
    {
        if(eventBuddy!=null)
            eventBuddy.executeMsg(myHost, msg);
        super.executeMsg(myHost, msg);
    }
    public boolean okMessage(Environmental myHost, CMMsg msg)
    {
        if((eventBuddy!=null)
        &&(!eventBuddy.okMessage(myHost, msg)))
            return false;
        return super.okMessage(myHost, msg);
        
    }
	public void affectCharStats(MOB affectedMob, CharStats affectableStats)
	{
		if(adjStats!=null)
			for(int i: CharStats.CODES.ALL())
				affectableStats.setStat(i,affectableStats.getStat(i)+adjStats.getStat(i));
		if(setStats!=null)
			for(int i: CharStats.CODES.ALL())
				if(setStats.getStat(i)!=0)
					affectableStats.setStat(i,setStats.getStat(i));
        if(statBuddy!=null)
            statBuddy.affectCharStats(affectedMob,affectableStats);
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
        if(statBuddy!=null)
            statBuddy.affectCharState(affectedMob,affectableMaxState);
	}

	public String classParms()
	{
		StringBuffer str=new StringBuffer("");
		str.append("<CCLASS><ID>"+ID()+"</ID>");
        for(int i=0;i<names.length;i++)
        {
    		str.append(CMLib.xml().convertXMLtoTag("NAME"+i,names[i]));
            str.append(CMLib.xml().convertXMLtoTag("NAMELEVEL"+i,nameLevels[i].intValue()));
        }
		str.append(CMLib.xml().convertXMLtoTag("BASE",baseClass()));
		str.append(CMLib.xml().convertXMLtoTag("HPDIV",""+hpDivisor));
		str.append(CMLib.xml().convertXMLtoTag("HPDICE",""+hpDice));
		str.append(CMLib.xml().convertXMLtoTag("HPDIE",""+hpDie));
		str.append(CMLib.xml().convertXMLtoTag("LVLPRAC",""+bonusPracLevel));
		str.append(CMLib.xml().convertXMLtoTag("MANADIV",""+manaDivisor));
		str.append(CMLib.xml().convertXMLtoTag("MANADICE",""+manaDice));
		str.append(CMLib.xml().convertXMLtoTag("MANADIE",""+manaDie));
        str.append(CMLib.xml().convertXMLtoTag("LEVELCAP",""+levelCap));
		str.append(CMLib.xml().convertXMLtoTag("LVLATT",""+bonusAttackLevel));
		str.append(CMLib.xml().convertXMLtoTag("ATTATT",""+attackAttribute));
		str.append(CMLib.xml().convertXMLtoTag("FSTPRAC",""+pracsFirstLevel));
		str.append(CMLib.xml().convertXMLtoTag("FSTTRAN",""+trainsFirstLevel));
		str.append(CMLib.xml().convertXMLtoTag("LVLDAM",""+levelsPerBonusDamage));
		str.append(CMLib.xml().convertXMLtoTag("LVLMOVE",""+movementMultiplier));
		str.append(CMLib.xml().convertXMLtoTag("ARMOR",""+allowedArmorLevel));
		//str.append(CMLib.xml().convertXMLtoTag("STRWEAP",weaponLimitations));
		//str.append(CMLib.xml().convertXMLtoTag("STRARM",armorLimitations));
		str.append(CMLib.xml().convertXMLtoTag("STRLMT",otherLimitations));
		str.append(CMLib.xml().convertXMLtoTag("STRBON",otherBonuses));
		str.append(CMLib.xml().convertXMLtoTag("QUAL",qualifications));
		str.append(CMLib.xml().convertXMLtoTag("PLAYER",""+selectability));
		str.append(CMLib.xml().convertXMLtoTag("MAXNCS",""+maxNonCraftingSkills));
		str.append(CMLib.xml().convertXMLtoTag("MAXCRS",""+maxCraftingSkills));
		str.append(CMLib.xml().convertXMLtoTag("MAXCMS",""+maxCommonSkills));
		str.append(CMLib.xml().convertXMLtoTag("MAXLGS",""+maxLanguages));
		
        str.append(CMLib.xml().convertXMLtoTag("HELP",CMLib.xml().parseOutAngleBrackets(helpEntry)));
		if(adjEStats==null) str.append("<ESTATS/>");
		else
			str.append(CMLib.xml().convertXMLtoTag("ESTATS",CMLib.coffeeMaker().getEnvStatsStr(adjEStats)));
		if(adjStats==null) str.append("<ASTATS/>");
		else
			str.append(CMLib.xml().convertXMLtoTag("ASTATS",CMLib.coffeeMaker().getCharStatsStr(adjStats)));
		if(setStats==null) str.append("<CSTATS/>");
		else
			str.append(CMLib.xml().convertXMLtoTag("CSTATS",CMLib.coffeeMaker().getCharStatsStr(setStats)));
		if(adjState==null) str.append("<ASTATE/>");
		else
			str.append(CMLib.xml().convertXMLtoTag("ASTATE",CMLib.coffeeMaker().getCharStateStr(adjState)));
		if(startAdjState==null) str.append("<STARTASTATE/>");
		else
			str.append(CMLib.xml().convertXMLtoTag("STARTASTATE",CMLib.coffeeMaker().getCharStateStr(startAdjState)));
		str.append(CMLib.xml().convertXMLtoTag("DISFLAGS",""+disableFlags));

		Vector ables=getAbleSet();
		if((ables==null)||(ables.size()==0))
			str.append("<CABILITIES/>");
		else
		{
			str.append("<CABILITIES>");
			for(int r=0;r<ables.size();r++)
			{
				str.append("<CABILITY>");
				str.append("<CACLASS>"+((AbilityMapper.AbilityMapping)ables.elementAt(r)).abilityName+"</CACLASS>");
				str.append("<CALEVEL>"+((AbilityMapper.AbilityMapping)ables.elementAt(r)).qualLevel+"</CALEVEL>");
				str.append("<CAPROFF>"+((AbilityMapper.AbilityMapping)ables.elementAt(r)).defaultProficiency+"</CAPROFF>");
				str.append("<CAAGAIN>"+((AbilityMapper.AbilityMapping)ables.elementAt(r)).autoGain+"</CAAGAIN>");
				str.append("<CASECR>"+((AbilityMapper.AbilityMapping)ables.elementAt(r)).isSecret+"</CASECR>");
				str.append("<CAPARM>"+((AbilityMapper.AbilityMapping)ables.elementAt(r)).defaultParm+"</CAPARM>");
                str.append("<CAPREQ>"+((AbilityMapper.AbilityMapping)ables.elementAt(r)).originalSkillPreReqList+"</CAPREQ>");
                str.append("<CAMASK>"+((AbilityMapper.AbilityMapping)ables.elementAt(r)).extraMask+"</CAMASK>");
                str.append("<CAMAXP>"+((AbilityMapper.AbilityMapping)ables.elementAt(r)).maxProficiency+"</CAMAXP>");
				str.append("</CABILITY>");
			}
			str.append("</CABILITIES>");
		}

		if((disallowedWeaponSet==null)||(disallowedWeaponSet.size()==0))
			str.append("<NOWEAPS/>");
		else
		{
			str.append("<NOWEAPS>");
			for(Iterator i=disallowedWeaponSet.iterator();i.hasNext();)
			{
				Integer I=(Integer)i.next();
				str.append(CMLib.xml().convertXMLtoTag("WCLASS",""+I.intValue()));
			}
			str.append("</NOWEAPS>");
		}
        if((requiredWeaponMaterials==null)||(requiredWeaponMaterials.size()==0))
            str.append("<NOWMATS/>");
        else
        {
            str.append("<NOWMATS>");
            for(Iterator i=requiredWeaponMaterials.iterator();i.hasNext();)
            {
                Integer I=(Integer)i.next();
                str.append(CMLib.xml().convertXMLtoTag("WMAT",""+I.intValue()));
            }
            str.append("</NOWMATS>");
        }
		if((outfit(null)==null)||(outfit(null).size()==0))	str.append("<OUTFIT/>");
		else
		{
			str.append("<OUTFIT>");
			for(int i=0;i<outfit(null).size();i++)
			{
				Item I=(Item)outfit(null).elementAt(i);
				str.append("<OFTITEM>");
				str.append(CMLib.xml().convertXMLtoTag("OFCLASS",CMClass.classID(I)));
				str.append(CMLib.xml().convertXMLtoTag("OFDATA",CMLib.xml().parseOutAngleBrackets(I.text())));
				str.append("</OFTITEM>");
			}
			str.append("</OUTFIT>");
		}
        for(int i=0;i<securityGroups.length;i++)
        if(i<securityGroupLevels.length)
        {
            str.append(CMLib.xml().convertXMLtoTag("SSET"+i,CMParms.combineWithQuotes(securityGroups[i],0)));
            str.append(CMLib.xml().convertXMLtoTag("SSETLEVEL"+i,securityGroupLevels[i].intValue()));
        }
        str.append(CMLib.xml().convertXMLtoTag("ARMORMINOR",""+requiredArmorSourceMinor));
        str.append(CMLib.xml().convertXMLtoTag("STATCLASS",getCharClassLocatorID(statBuddy)));
        str.append(CMLib.xml().convertXMLtoTag("EVENTCLASS",getCharClassLocatorID(eventBuddy)));
        if(xtraValues==null)
            xtraValues=CMProps.getExtraStatCodesHolder(this);
        for(int i=this.getSaveStatIndex();i<getStatCodes().length;i++)
            str.append(CMLib.xml().convertXMLtoTag(getStatCodes()[i],getStat(getStatCodes()[i])));
		str.append("</CCLASS>");
		return str.toString();
	}
	
	public void setClassParms(String parms)
	{
		if(parms.trim().length()==0) return;
		Vector xml=CMLib.xml().parseAllXML(parms);
		if(xml==null)
		{
			Log.errOut("GenCharClass","Unable to parse: "+parms);
			return;
		}
		Vector classData=CMLib.xml().getRealContentsFromPieces(xml,"CCLASS");
		if(classData==null){	Log.errOut("GenCharClass","Unable to get CCLASS data."); return;}
        String classID=CMLib.xml().getValFromPieces(classData,"ID");
		if(classID.length()==0) return;
		ID=classID;
        String singleName=CMLib.xml().getValFromPieces(classData,"NAME");
        if((singleName!=null)&&(singleName.length()>0))
        {
            names=new String[1];
            names[0]=singleName;
            nameLevels=new Integer[1];
            nameLevels[0]=Integer.valueOf(0);
        }
        else
        {
            Vector nameSet=new Vector();
            Vector levelSet=new Vector();
            int index=0;
            int lastLevel=-1;
            while(true)
            {
                String name=CMLib.xml().getValFromPieces(classData,"NAME"+index);
                int level=CMLib.xml().getIntFromPieces(classData,"NAMELEVEL"+index);
                if((name.length()==0)||(level<=lastLevel))
                    break;
                nameSet.addElement(name);
                levelSet.addElement(Integer.valueOf(level));
                lastLevel=level;
                index++;
            }
            names=new String[nameSet.size()];
            nameLevels=new Integer[levelSet.size()];
            for(int i=0;i<nameSet.size();i++)
            {
                names[i]=(String)nameSet.elementAt(i);
                nameLevels[i]=(Integer)levelSet.elementAt(i);
            }
        }
		String base=CMLib.xml().getValFromPieces(classData,"BASE");
		if((base==null)||(base.length()==0))
			return;
		baseClass=base;
		hpDivisor=CMLib.xml().getIntFromPieces(classData,"HPDIV");
		if(hpDivisor==0) hpDivisor=3;
		hpDice=CMLib.xml().getIntFromPieces(classData,"HPDICE");
		hpDie=CMLib.xml().getIntFromPieces(classData,"HPDIE");
		bonusPracLevel=CMLib.xml().getIntFromPieces(classData,"LVLPRAC");
		manaDivisor=CMLib.xml().getIntFromPieces(classData,"MANADIV");
		if(manaDivisor==0) manaDivisor=3;
		manaDice=CMLib.xml().getIntFromPieces(classData,"MANADICE");
		manaDie=CMLib.xml().getIntFromPieces(classData,"MANADIE");
        levelCap=CMLib.xml().getIntFromPieces(classData,"LEVELCAP");
		bonusAttackLevel=CMLib.xml().getIntFromPieces(classData,"LVLATT");
		attackAttribute=CMLib.xml().getIntFromPieces(classData,"ATTATT");
		trainsFirstLevel=CMLib.xml().getIntFromPieces(classData,"FSTTRAN");
		pracsFirstLevel=CMLib.xml().getIntFromPieces(classData,"FSTPRAC");
		levelsPerBonusDamage=CMLib.xml().getIntFromPieces(classData,"LVLDAM");
		movementMultiplier=CMLib.xml().getIntFromPieces(classData,"LVLMOVE");
		allowedArmorLevel=CMLib.xml().getIntFromPieces(classData,"ARMOR");
		//weaponLimitations=CMLib.xml().getValFromPieces(classData,"STRWEAP");
		//armorLimitations=CMLib.xml().getValFromPieces(classData,"STRARM");
		otherLimitations=CMLib.xml().getValFromPieces(classData,"STRLMT");
		otherBonuses=CMLib.xml().getValFromPieces(classData,"STRBON");
		qualifications=CMLib.xml().getValFromPieces(classData,"QUAL");
		maxNonCraftingSkills=CMLib.xml().getIntFromPieces(classData,"MAXNCS");
		maxCraftingSkills=CMLib.xml().getIntFromPieces(classData,"MAXCRS");
		maxCommonSkills=CMLib.xml().getIntFromPieces(classData,"MAXCMS");
		maxLanguages=CMLib.xml().getIntFromPieces(classData,"MAXLGS");
		helpEntry=CMLib.xml().restoreAngleBrackets(CMLib.xml().getValFromPieces(classData,"HELP"));
		String s=CMLib.xml().getValFromPieces(classData,"PLAYER");
		if(CMath.isNumber(s))
		    selectability=CMath.s_int(s);
		else
			selectability=CMath.s_bool(s)?Area.THEME_FANTASY:0;
		adjEStats=null;
		String eStats=CMLib.xml().getValFromPieces(classData,"ESTATS");
		if(eStats.length()>0){ adjEStats=(EnvStats)CMClass.getCommon("DefaultEnvStats"); CMLib.coffeeMaker().setEnvStats(adjEStats,eStats);}
		adjStats=null;
		String aStats=CMLib.xml().getValFromPieces(classData,"ASTATS");
		if(aStats.length()>0){ adjStats=(CharStats)CMClass.getCommon("DefaultCharStats"); CMLib.coffeeMaker().setCharStats(adjStats,aStats);}
		setStats=null;
		String cStats=CMLib.xml().getValFromPieces(classData,"CSTATS");
		if(cStats.length()>0){ setStats=(CharStats)CMClass.getCommon("DefaultCharStats"); CMLib.coffeeMaker().setCharStats(setStats,cStats);}
		adjState=null;
		String aState=CMLib.xml().getValFromPieces(classData,"ASTATE");
		if(aState.length()>0){ adjState=(CharState)CMClass.getCommon("DefaultCharState"); CMLib.coffeeMaker().setCharState(adjState,aState);}
		startAdjState=null;
		disableFlags=CMLib.xml().getIntFromPieces(classData,"DISFLAGS");
		String saState=CMLib.xml().getValFromPieces(classData,"STARTASTATE");
		if(saState.length()>0){ startAdjState=(CharState)CMClass.getCommon("DefaultCharState"); startAdjState.setAllValues(0); CMLib.coffeeMaker().setCharState(startAdjState,saState);}

		Vector xV=CMLib.xml().getRealContentsFromPieces(classData,"CABILITIES");
		CMLib.ableMapper().delCharMappings(ID());
		if((xV!=null)&&(xV.size()>0))
			for(int x=0;x<xV.size();x++)
			{
				XMLLibrary.XMLpiece iblk=(XMLLibrary.XMLpiece)xV.elementAt(x);
				if((!iblk.tag.equalsIgnoreCase("CABILITY"))||(iblk.contents==null))
					continue;
				// I hate backwards compatibility.
				String maxProff=CMLib.xml().getValFromPieces(iblk.contents,"CAMAXP");
				if((maxProff==null)||(maxProff.trim().length()==0)) maxProff="100";
				CMLib.ableMapper().addCharAbilityMapping(ID(),
    								 CMLib.xml().getIntFromPieces(iblk.contents,"CALEVEL"),
    								 CMLib.xml().getValFromPieces(iblk.contents,"CACLASS"),
    								 CMLib.xml().getIntFromPieces(iblk.contents,"CAPROFF"),
    								 CMath.s_int(maxProff),
    								 CMLib.xml().getValFromPieces(iblk.contents,"CAPARM"),
    								 CMLib.xml().getBoolFromPieces(iblk.contents,"CAAGAIN"),
    								 CMLib.xml().getBoolFromPieces(iblk.contents,"CASECR"),
                                     CMParms.parseCommas(CMLib.xml().getValFromPieces(iblk.contents,"CAPREQ"),true),
                                     CMLib.xml().getValFromPieces(iblk.contents,"CAMASK"));
			}

		// now WEAPON RESTRICTIONS!
		xV=CMLib.xml().getRealContentsFromPieces(classData,"NOWEAPS");
		disallowedWeaponSet=null;
		if((xV!=null)&&(xV.size()>0))
		{
			disallowedWeaponSet=new HashSet();
			for(int x=0;x<xV.size();x++)
			{
				XMLLibrary.XMLpiece iblk=(XMLLibrary.XMLpiece)xV.elementAt(x);
				if((!iblk.tag.equalsIgnoreCase("WCLASS"))||(iblk.contents==null))
					continue;
				disallowedWeaponSet.add(Integer.valueOf(CMath.s_int(iblk.value)));
			}
		}

        // now WEAPON MATERIALS!
        xV=CMLib.xml().getRealContentsFromPieces(classData,"NOWMATS");
        requiredWeaponMaterials=null;
        if((xV!=null)&&(xV.size()>0))
        {
            requiredWeaponMaterials=new HashSet();
            for(int x=0;x<xV.size();x++)
            {
                XMLLibrary.XMLpiece iblk=(XMLLibrary.XMLpiece)xV.elementAt(x);
                if((!iblk.tag.equalsIgnoreCase("WMAT"))||(iblk.contents==null))
                    continue;
                requiredWeaponMaterials.add(Integer.valueOf(CMath.s_int(iblk.value)));
            }
        }
        
		// now OUTFIT!
		Vector oV=CMLib.xml().getRealContentsFromPieces(classData,"OUTFIT");
		outfitChoices=null;
		if((oV!=null)&&(oV.size()>0))
		{
			outfitChoices=new Vector();
			for(int x=0;x<oV.size();x++)
			{
				XMLLibrary.XMLpiece iblk=(XMLLibrary.XMLpiece)oV.elementAt(x);
				if((!iblk.tag.equalsIgnoreCase("OFTITEM"))||(iblk.contents==null))
					continue;
				Item newOne=CMClass.getItem(CMLib.xml().getValFromPieces(iblk.contents,"OFCLASS"));
				String idat=CMLib.xml().getValFromPieces(iblk.contents,"OFDATA");
				newOne.setMiscText(CMLib.xml().restoreAngleBrackets(idat));
				newOne.recoverEnvStats();
				outfitChoices.addElement(newOne);
			}
		}

        // security groups
        Vector groupSet=new Vector();
        Vector groupLevelSet=new Vector();
        int index=0;
        int lastLevel=-1;
        while(true)
        {
            String groups=CMLib.xml().getValFromPieces(classData,"SSET"+index);
            int groupLevel=CMLib.xml().getIntFromPieces(classData,"SSETLEVEL"+index);
            if((groups.length()==0)||(groupLevel<=lastLevel))
                break;
            groupSet.addElement(CMParms.parse(groups.toUpperCase()));
            groupLevelSet.addElement(Integer.valueOf(groupLevel));
            lastLevel=groupLevel;
            index++;
        }
        securityGroups=new Vector[groupSet.size()];
        securityGroupLevels=new Integer[groupLevelSet.size()];
        for(int i=0;i<groupSet.size();i++)
        {
            securityGroups[i]=(Vector)groupSet.elementAt(i);
            securityGroupLevels[i]=(Integer)groupLevelSet.elementAt(i);
        }
        securityGroupCache.clear();
        
        requiredArmorSourceMinor=CMLib.xml().getIntFromPieces(classData,"ARMORMINOR");
        setStat("STATCLASS",CMLib.xml().getValFromPieces(classData,"STATCLASS"));
        setStat("EVENTCLASS",CMLib.xml().getValFromPieces(classData,"EVENTCLASS"));
        xtraValues=CMProps.getExtraStatCodesHolder(this);
        for(int i=this.getSaveStatIndex();i<getStatCodes().length;i++)
            setStat(getStatCodes()[i],CMLib.xml().getValFromPieces(classData, getStatCodes()[i]));
	}

	protected Vector getAbleSet()
	{
		Vector VA=new Vector(9);
		DVector V=CMLib.ableMapper().getUpToLevelListings(ID(),Integer.MAX_VALUE,true,false);
		for(int v=0;v<V.size();v++)
		{
			String AID=(String)V.elementAt(v,1);
			AbilityMapper.AbilityMapping newMAP=new AbilityMapper.AbilityMapping();
			newMAP.abilityName = AID;
			newMAP.qualLevel = CMLib.ableMapper().getQualifyingLevel(ID(),false,AID); 
			newMAP.defaultProficiency = CMLib.ableMapper().getDefaultProficiency(ID(),false,AID);
			newMAP.autoGain = CMLib.ableMapper().getDefaultGain(ID(),false,AID);
			newMAP.isSecret = CMLib.ableMapper().getSecretSkill(ID(),false,AID);
			newMAP.defaultParm = CMLib.ableMapper().getDefaultParm(ID(),false,AID);
			newMAP.originalSkillPreReqList = CMLib.ableMapper().getPreReqStrings(ID(),false,AID);
			newMAP.extraMask = CMLib.ableMapper().getExtraMask(ID(),false,AID);
			newMAP.maxProficiency = CMLib.ableMapper().getMaxProficiency(ID(),false,AID);
			VA.addElement(newMAP);
		}
		return VA;
	}

	protected static String[] CODES={"ID","NAME","BASE","HPDIV","HPDICE",
									 "LVLPRAC","MANADIV","LVLATT","ATTATT","FSTTRAN",
									 "FSTPRAC","LVLDAM","LVLMOVE","ARMOR","STRWEAP",
									 "STRARM","STRLMT","STRBON","QUAL","PLAYER",
									 "ESTATS","ASTATS","CSTATS","ASTATE","NUMCABLE",
									 "GETCABLE","GETCABLELVL","GETCABLEPROF","GETCABLEGAIN","GETCABLESECR",
									 "GETCABLEPARM","NUMWEP","GETWEP", "NUMOFT","GETOFTID",
									 "GETOFTPARM","HPDIE","MANADICE","MANADIE","DISFLAGS",
									 "STARTASTATE","NUMNAME","NAMELEVEL","NUMSSET","SSET",
                                     "SSETLEVEL","NUMWMAT","GETWMAT","ARMORMINOR","STATCLASS",
                                     "EVENTCLASS","GETCABLEPREQ","GETCABLEMASK","HELP","LEVELCAP",
                                     "GETCABLEMAXP","MAXNCS","MAXCRS","MAXCMS","MAXLGS"
									 }; 
    
	public String getStat(String code)
	{
		int num=0;
        int numDex=code.length();
        while((numDex>0)&&(Character.isDigit(code.charAt(numDex-1)))) numDex--;
        if(numDex<code.length())
        {
            num=CMath.s_int(code.substring(numDex));
            code=code.substring(0,numDex);
        }
		switch(getCodeNum(code))
		{
		case 0: return ID;
		case 1: if(num<names.length)
                    return names[num];
                break;
		case 2: return baseClass;
		case 3: return ""+hpDivisor;
		case 4: return ""+hpDice;
		case 5: return ""+bonusPracLevel;
		case 6: return ""+manaDivisor;
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
		case 19: return ""+selectability;
		case 20: return (adjEStats==null)?"":CMLib.coffeeMaker().getEnvStatsStr(adjEStats);
		case 21: return (adjStats==null)?"":CMLib.coffeeMaker().getCharStatsStr(adjStats);
		case 22: return (setStats==null)?"":CMLib.coffeeMaker().getCharStatsStr(setStats);
		case 23: return (adjState==null)?"":CMLib.coffeeMaker().getCharStateStr(adjState);
		case 24: return Integer.toString(getAbleSet().size());
		case 25: return ((AbilityMapper.AbilityMapping)getAbleSet().elementAt(num)).abilityName;
		case 26: return Integer.toString(((AbilityMapper.AbilityMapping)getAbleSet().elementAt(num)).qualLevel);
		case 27: return Integer.toString(((AbilityMapper.AbilityMapping)getAbleSet().elementAt(num)).defaultProficiency);
		case 28: return Boolean.toString(((AbilityMapper.AbilityMapping)getAbleSet().elementAt(num)).autoGain);
		case 29: return Boolean.toString(((AbilityMapper.AbilityMapping)getAbleSet().elementAt(num)).isSecret);
		case 30: return ((AbilityMapper.AbilityMapping)getAbleSet().elementAt(num)).defaultParm;
		case 31: return ""+((disallowedWeaponSet!=null)?disallowedWeaponSet.size():0);
		case 32: return CMParms.toStringList(disallowedWeaponSet);
		case 33: return ""+((outfit(null)!=null)?outfit(null).size():0);
		case 34: return ""+((outfit(null)!=null)?((Item)outfit(null).elementAt(num)).ID():"");
		case 35: return ""+((outfit(null)!=null)?((Item)outfit(null).elementAt(num)).text():"");
		case 36: return ""+hpDie;
		case 37: return ""+manaDice;
		case 38: return ""+manaDie;
		case 39: return ""+disableFlags;
		case 40: return (startAdjState==null)?"":CMLib.coffeeMaker().getCharStateStr(startAdjState);
        case 41: return ""+names.length;
        case 42: if(num<nameLevels.length)
                    return ""+nameLevels[num].intValue();
                 break;
        case 43: return ""+securityGroups.length;
        case 44: if(num<securityGroups.length)
                    return CMParms.combineWithQuotes(securityGroups[num],0);
                 break;
        case 45: if(num<securityGroupLevels.length)
                    return ""+securityGroupLevels[num];
                 break;
        case 46: return ""+((requiredWeaponMaterials!=null)?requiredWeaponMaterials.size():0);
        case 47: return CMParms.toStringList(requiredWeaponMaterials);
        case 48: return ""+requiredArmorSourceMinor();
        case 49: return this.getCharClassLocatorID(statBuddy);
        case 50: return this.getCharClassLocatorID(eventBuddy);
		case 51: return ((AbilityMapper.AbilityMapping)getAbleSet().elementAt(num)).originalSkillPreReqList;
		case 52: return ((AbilityMapper.AbilityMapping)getAbleSet().elementAt(num)).extraMask;
        case 53: return helpEntry;
        case 54: return ""+levelCap;
		case 55: return Integer.toString(((AbilityMapper.AbilityMapping)getAbleSet().elementAt(num)).maxProficiency);
        case 56: return ""+maxNonCraftingSkills;
        case 57: return ""+maxCraftingSkills;
        case 58: return ""+maxCommonSkills;
        case 59: return ""+maxLanguages;
        default:
            return CMProps.getStatCodeExtensionValue(getStatCodes(), xtraValues, code);
        }
		return "";
	}
	protected String[] tempables=new String[9];
	public void setStat(String code, String val)
	{
        int num=0;
        int numDex=code.length();
        while((numDex>0)&&(Character.isDigit(code.charAt(numDex-1)))) numDex--;
        if(numDex<code.length())
        {
            num=CMath.s_int(code.substring(numDex));
            code=code.substring(0,numDex);
        }
		switch(getCodeNum(code))
		{
		case 0: ID=val; break;
		case 1: if(num<names.length)
                    names[num]=val;
                break;
		case 2: baseClass=val; break;
		case 3: hpDivisor=CMath.s_parseIntExpression(val); break;
		case 4: hpDice=CMath.s_parseIntExpression(val); break;
		case 5: bonusPracLevel=CMath.s_parseIntExpression(val); break;
		case 6: manaDivisor=CMath.s_parseIntExpression(val); break;
		case 7: bonusAttackLevel=CMath.s_parseIntExpression(val); break;
		case 8: attackAttribute=CMath.s_parseListIntExpression(CharStats.CODES.NAMES(),val); break;
		case 9: trainsFirstLevel=CMath.s_parseIntExpression(val); break;
		case 10: pracsFirstLevel=CMath.s_parseIntExpression(val); break;
		case 11: levelsPerBonusDamage=CMath.s_parseIntExpression(val); break;
		case 12: movementMultiplier=CMath.s_parseIntExpression(val); break;
		case 13: allowedArmorLevel=CMath.s_parseListIntExpression(CharClass.ARMOR_DESCS,val); break;
		case 14: break;//weaponLimitations=val;break;
		case 15: break;//armorLimitations=val;break;
		case 16: otherLimitations=val;break;
		case 17: otherBonuses=val;break;
		case 18: qualifications=val;break;
		case 19: selectability=CMath.s_parseBitIntExpression(Area.THEME_DESCS,val); break;
		case 20: adjEStats=null;if(val.length()>0){adjEStats=(EnvStats)CMClass.getCommon("DefaultEnvStats"); adjEStats.setAllValues(0); CMLib.coffeeMaker().setEnvStats(adjEStats,val);}break;
		case 21: adjStats=null;if(val.length()>0){adjStats=(CharStats)CMClass.getCommon("DefaultCharStats"); adjStats.setAllValues(0); CMLib.coffeeMaker().setCharStats(adjStats,val);}break;
		case 22: setStats=null;if(val.length()>0){setStats=(CharStats)CMClass.getCommon("DefaultCharStats"); setStats.setAllValues(0); CMLib.coffeeMaker().setCharStats(setStats,val);}break;
		case 23: adjState=null;if(val.length()>0){adjState=(CharState)CMClass.getCommon("DefaultCharState"); adjState.setAllValues(0); CMLib.coffeeMaker().setCharState(adjState,val);}break;
		case 24: CMLib.ableMapper().delCharMappings(ID()); break;
        case 25: CMLib.ableMapper().addCharAbilityMapping(ID(),
                                                         CMath.s_int(tempables[1]),
                                                         val,
                                                         CMath.s_int(tempables[2]),
                                                         CMath.s_int(tempables[8]),
                                                         tempables[5],
                                                         CMath.s_bool(tempables[3]),
                                                         CMath.s_bool(tempables[4]),
                                                         CMParms.parseCommas(tempables[6],true),
                                                         tempables[7]);
                                                         break;
		case 26: tempables[1]=val; break;
		case 27: tempables[2]=val; break;
		case 28: tempables[3]=val; break;
		case 29: tempables[4]=val; break;
        case 30: tempables[5]=val; break;
		case 31: if(CMath.s_int(val)==0)
					 disallowedWeaponSet=null;
				 else
					 disallowedWeaponSet=new HashSet();
				 break;
		case 32:
        {
                 Vector V=CMParms.parseCommas(val,true);
				 if(V.size()>0)
				 {
					disallowedWeaponSet=new HashSet();
					for(int v=0;v<V.size();v++)
						disallowedWeaponSet.add(Integer.valueOf(CMath.s_int((String)V.elementAt(v))));
				 }
				 else
					 disallowedWeaponSet=null;
				 break;
        }
		case 33: if(CMath.s_int(val)==0) outfitChoices=null; break;
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
		case 36: hpDie=CMath.s_int(val); break;
		case 37: manaDice=CMath.s_int(val); break;
		case 38: manaDie=CMath.s_int(val); break;
		case 39: disableFlags=CMath.s_int(val); break;
		case 40: startAdjState=null;if(val.length()>0){startAdjState=(CharState)CMClass.getCommon("DefaultCharState"); startAdjState.setAllValues(0); CMLib.coffeeMaker().setCharState(startAdjState,val);}break;
        case 41: num=CMath.s_int(val);
                 if(num>0)
                 {
                    String[] newNames=new String[num];
                    Integer[] newLevels=new Integer[num];
                    for(int i=0;i<names.length;i++)
                        if(i<num)
                        {
                            newNames[i]=names[i];
                            newLevels[i]=nameLevels[i];
                        }
                    if(newNames.length>names.length)
                    for(int i=names.length;i<newNames.length;i++)
                    {
                        newNames[i]=names[names.length-1];
                        newLevels[i]=Integer.valueOf(newLevels[i-1].intValue()+1);
                    }
                    names=newNames;
                    nameLevels=newLevels;
                 }
                 break;
        case 42: if(num<nameLevels.length)
                    nameLevels[num]=Integer.valueOf(CMath.s_int(val));
                 break;
        case 43:{  num=CMath.s_int(val);
                   if(num<0) num=0;
                   Vector[] newGroups=new Vector[num];
                   Integer[] newLevels=new Integer[num];
                   for(int i=0;i<securityGroups.length;i++)
                       if(i<num)
                       {
                           newGroups[i]=securityGroups[i];
                           newLevels[i]=securityGroupLevels[i];
                       }
                   if(newGroups.length>securityGroups.length)
                   for(int i=securityGroups.length;i<newGroups.length;i++)
                   {
                       newGroups[i]=new Vector();
                       if(i==0)
                           newLevels[0]=Integer.valueOf(0);
                       else
                           newLevels[i]=Integer.valueOf(newLevels[i-1].intValue()+1);
                   }
                   securityGroups=newGroups;
                   securityGroupLevels=newLevels;
                   securityGroupCache.clear();
                   break;
                }
        case 44: if(num<securityGroups.length)
                    securityGroups[num]=CMParms.parse(val.toUpperCase());
                 securityGroupCache.clear();
                 break;
        case 45: if(num<securityGroupLevels.length)
                    securityGroupLevels[num]=Integer.valueOf(CMath.s_int(val));
                securityGroupCache.clear();
                 break;
        case 46: if(CMath.s_int(val)==0)
                     requiredWeaponMaterials=null;
                 else
                     requiredWeaponMaterials=new HashSet();
                 break;
        case 47:
        {
                 Vector V=CMParms.parseCommas(val,true);
                 if(V.size()>0)
                 {
                     requiredWeaponMaterials=new HashSet();
                     for(int v=0;v<V.size();v++)
                         requiredWeaponMaterials.add(Integer.valueOf(CMath.s_int((String)V.elementAt(v))));
                 }
                 else
                     requiredWeaponMaterials=null;
                 break;
        }
        case 48: requiredArmorSourceMinor=CMath.s_int(val); break;
        case 49:
        {
            statBuddy=CMClass.getCharClass(val);
            try{
                if(statBuddy==null)
                    statBuddy=(CharClass)CMClass.unsortedLoadClass("CHARCLASS",val,true);
            }catch(Exception e){}
            break;
        }
        case 50:
        {
            eventBuddy=CMClass.getCharClass(val);
            try{
                if(eventBuddy==null)
                    eventBuddy=(CharClass)CMClass.unsortedLoadClass("CHARCLASS",val,true);
            }catch(Exception e){}
            break;
        }
        case 51: tempables[6]=val; break;
        case 52: tempables[7]=val; break;
        case 53: helpEntry=val; break;
        case 54: levelCap=CMath.s_int(val); break;
        case 55: tempables[8]=val; break;
        case 56: maxNonCraftingSkills=CMath.s_int(val); break;
        case 57: maxCraftingSkills=CMath.s_int(val); break;
        case 58: maxCommonSkills=CMath.s_int(val); break;
        case 59: maxLanguages=CMath.s_int(val); break;
        default:
            CMProps.setStatCodeExtensionValue(getStatCodes(), xtraValues, code, val);
            break;
        }
	}
	public void startCharacter(MOB mob, boolean isBorrowedClass, boolean verifyOnly)
	{
	    super.startCharacter(mob,isBorrowedClass,verifyOnly);
	    if((!verifyOnly)&&(startAdjState!=null))
	    {
			mob.baseState().setFatigue(mob.baseState().getFatigue()+startAdjState.getFatigue());
			mob.baseState().setHitPoints(mob.baseState().getHitPoints()+startAdjState.getHitPoints());
			mob.baseState().setHunger(mob.baseState().getHunger()+startAdjState.getHunger());
			mob.baseState().setMana(mob.baseState().getMana()+startAdjState.getMana());
			mob.baseState().setMovement(mob.baseState().getMovement()+startAdjState.getMovement());
			mob.baseState().setThirst(mob.baseState().getThirst()+startAdjState.getThirst());
	    }
	}
    public int getSaveStatIndex(){return (xtraValues==null)?getStatCodes().length:getStatCodes().length-xtraValues.length;}
    private static String[] codes=null;
    public String[] getStatCodes()
    {
        if(codes!=null) return codes;
        codes=CMProps.getStatCodesList(CODES,this);
        return codes;
    }
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
		if(E.classParms().equals(classParms()))
			return true;
		return false;
	}
}
