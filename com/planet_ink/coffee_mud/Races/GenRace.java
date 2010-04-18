package com.planet_ink.coffee_mud.Races;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.GenCharClass;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
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
public class GenRace extends StdRace
{
	protected String ID="GenRace";
	public String ID(){	return ID; }
	protected String name="GenRace";
	public String name(){ return name; }
	public int practicesAtFirstLevel(){return 0;}
	public int trainsAtFirstLevel(){return 0;}
	public int availability=0;
	public int availabilityCode(){return availability;}
	public int[] agingChart=null;
    protected String[] xtraValues=null;
	public int[] getAgingChart()
	{
	    if(agingChart==null)
	        agingChart=(int[])super.getAgingChart().clone();
        return agingChart;
	}

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
	public String racialCategory="Unknown";
	public String racialCategory(){return racialCategory;}
	public boolean isGeneric(){return true;}

	protected int disableFlags=0;
	public boolean classless(){return (disableFlags&Race.GENFLAG_NOCLASS)==Race.GENFLAG_NOCLASS;}
	public boolean leveless(){return (disableFlags&Race.GENFLAG_NOLEVELS)==Race.GENFLAG_NOLEVELS;}
	public boolean expless(){return (disableFlags&Race.GENFLAG_NOEXP)==Race.GENFLAG_NOEXP;}
	public boolean fertile(){return !((disableFlags&Race.GENFLAG_NOFERTILE)==Race.GENFLAG_NOFERTILE);}
	protected boolean uncharmable(){return ((disableFlags&Race.GENFLAG_NOCHARM)==Race.GENFLAG_NOCHARM);}

	//                     an ey ea he ne ar ha to le fo no gi mo wa ta wi
	protected int[] parts={0 ,2 ,2 ,1 ,1 ,2 ,2 ,1 ,2 ,2 ,1 ,0 ,1 ,1 ,0 ,0 };
	public int[] bodyMask(){return parts;}

	protected CharStats setStats=null;
	protected CharStats adjStats=null;
	protected EnvStats adjEStats=null;
	protected CharState adjState=null;
	protected CharState startAdjState=null;
	protected Vector resourceChoices=null;
	protected Race healthBuddy=null;
	protected Race eventBuddy=null;
	protected Race weaponBuddy=null;
	protected String helpEntry = "";

	protected String[] racialEffectNames=null;
	protected int[] racialEffectLevels=null;
	protected String[] racialEffectParms=null;
	protected String[] racialEffectNames(){return racialEffectNames;}
	protected int[] racialEffectLevels(){return racialEffectLevels;}
	protected String[] racialEffectParms(){return racialEffectParms;}

	protected String[] racialAbilityNames=null;
	protected int[] racialAbilityLevels=null;
	protected int[] racialAbilityProficiencies=null;
	protected boolean[] racialAbilityQuals=null;
	protected String[] culturalAbilityNames=null;
	protected int[] culturalAbilityProficiencies=null;

	protected String[] racialAbilityNames(){return racialAbilityNames;}
	protected int[] racialAbilityLevels(){return racialAbilityLevels;}
	protected int[] racialAbilityProficiencies(){return racialAbilityProficiencies;}
	protected boolean[] racialAbilityQuals(){return racialAbilityQuals;}
	public String[] culturalAbilityNames(){return culturalAbilityNames;}
	public int[] culturalAbilityProficiencies(){return culturalAbilityProficiencies;}
	protected boolean destroyBodyAfterUse=false;
	protected boolean destroyBodyAfterUse(){return destroyBodyAfterUse;}

	public GenRace()
	{
	    super();
	    xtraValues=CMProps.getExtraStatCodesHolder(this);
	}
	
    public CMObject newInstance(){try{return (CMObject)getClass().newInstance();}catch(Exception e){return new GenRace();}}
	public CMObject copyOf()
	{
		GenRace E=new GenRace();
		E.setRacialParms(racialParms());
		return E;
	}
	public Weapon myNaturalWeapon()
	{
		if(weaponBuddy!=null)
			return weaponBuddy.myNaturalWeapon();
		if(naturalWeapon!=null)
			return naturalWeapon;
		return funHumanoidWeapon();
	}

	protected String arriveStr="arrives";
	public String arriveStr()
	{
		return arriveStr;
	}
	protected String leaveStr="leaves";
	public String leaveStr()
	{
		return leaveStr;
	}
	public Race makeGenRace(){return this;}
	public String healthText(MOB viewer, MOB mob)
	{
		if((healthBuddy!=null)&&(healthBuddy!=this))
			return healthBuddy.healthText(viewer,mob);
		return CMLib.combat().standardMobCondition(viewer,mob);
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
	}
	public void affectCharStats(MOB affectedMob, CharStats affectableStats)
	{
		if(adjStats!=null)
			for(int i: CharStats.CODES.ALL())
				affectableStats.setStat(i,affectableStats.getStat(i)+adjStats.getStat(i));
		if(setStats!=null)
			for(int i: CharStats.CODES.ALL())
				if(setStats.getStat(i)!=0)
					affectableStats.setRacialStat(i,setStats.getStat(i));
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

	protected String getRaceLocatorID(Race R)
	{
		if(R==null) return "";
		if(R.isGeneric()) return R.ID();
		if(R==CMClass.getRace(R.ID()))
			return R.ID();
		return R.getClass().getName();
	}

	public String racialParms()
	{
		StringBuffer str=new StringBuffer("");
		str.append("<RACE><ID>"+ID()+"</ID>");
		str.append(CMLib.xml().convertXMLtoTag("NAME",name()));
		str.append(CMLib.xml().convertXMLtoTag("CAT",racialCategory()));
		str.append(CMLib.xml().convertXMLtoTag("MHEIGHT",""+shortestMale()));
		str.append(CMLib.xml().convertXMLtoTag("FHEIGHT",""+shortestFemale()));
		str.append(CMLib.xml().convertXMLtoTag("VHEIGHT",""+heightVariance()));
		str.append(CMLib.xml().convertXMLtoTag("BWEIGHT",""+lightestWeight()));
		str.append(CMLib.xml().convertXMLtoTag("VWEIGHT",""+weightVariance()));
		str.append(CMLib.xml().convertXMLtoTag("WEAR",""+forbiddenWornBits()));
		str.append(CMLib.xml().convertXMLtoTag("AVAIL",""+availability));
		str.append(CMLib.xml().convertXMLtoTag("DESTROYBODY",""+destroyBodyAfterUse()));
		StringBuffer bbody=new StringBuffer("");
		for(int i=0;i<bodyMask().length;i++)
			bbody.append((""+bodyMask()[i])+";");
		str.append(CMLib.xml().convertXMLtoTag("BODY",bbody.toString()));
		str.append(CMLib.xml().convertXMLtoTag("HEALTHRACE",getRaceLocatorID(healthBuddy)));
		str.append(CMLib.xml().convertXMLtoTag("EVENTRACE",getRaceLocatorID(eventBuddy)));
		str.append(CMLib.xml().convertXMLtoTag("WEAPONRACE",getRaceLocatorID(weaponBuddy)));
		str.append(CMLib.xml().convertXMLtoTag("ARRIVE",arriveStr()));
		str.append(CMLib.xml().convertXMLtoTag("LEAVE",leaveStr()));
        str.append(CMLib.xml().convertXMLtoTag("HELP",CMLib.xml().parseOutAngleBrackets(helpEntry)));
		str.append(CMLib.xml().convertXMLtoTag("AGING",CMParms.toStringList(getAgingChart())));
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

		if(myResources().size()==0)	str.append("<RESOURCES/>");
		else
		{
			str.append("<RESOURCES>");
			for(int i=0;i<myResources().size();i++)
			{
				Item I=(Item)myResources().elementAt(i);
				str.append("<RSCITEM>");
				str.append(CMLib.xml().convertXMLtoTag("ICLASS",CMClass.classID(I)));
				str.append(CMLib.xml().convertXMLtoTag("IDATA",CMLib.xml().parseOutAngleBrackets(I.text())));
				str.append("</RSCITEM>");
			}
			str.append("</RESOURCES>");
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
		if(naturalWeapon==null) str.append("<WEAPON/>");
		else
		{
			str.append("<WEAPON>");
			str.append(CMLib.xml().convertXMLtoTag("ICLASS",CMClass.classID(naturalWeapon)));
			str.append(CMLib.xml().convertXMLtoTag("IDATA",CMLib.xml().parseOutAngleBrackets(naturalWeapon.text())));
			str.append("</WEAPON>");
		}
		if((racialAbilityNames==null)||(racialAbilityNames.length==0))
			str.append("<RABILITIES/>");
		else
		{
			str.append("<RABILITIES>");
			for(int r=0;r<racialAbilityNames.length;r++)
			{
				str.append("<RABILITY>");
				str.append("<RCLASS>"+racialAbilityNames[r]+"</RCLASS>");
				str.append("<RLEVEL>"+racialAbilityLevels[r]+"</RLEVEL>");
				str.append("<RPROFF>"+racialAbilityProficiencies[r]+"</RPROFF>");
				str.append("<RAGAIN>"+racialAbilityQuals[r]+"</RAGAIN>");
				str.append("</RABILITY>");
			}
			str.append("</RABILITIES>");
		}

		if((racialEffectNames==null)||(racialEffectNames.length==0))
			str.append("<REFFECTS/>");
		else
		{
			str.append("<REFFECTS>");
			for(int r=0;r<racialEffectNames.length;r++)
			{
				str.append("<REFFECT>");
				str.append("<RFCLASS>"+racialEffectNames[r]+"</RFCLASS>");
				str.append("<RFLEVEL>"+racialEffectLevels[r]+"</RFLEVEL>");
				str.append("<RFPARM>"+racialEffectParms[r]+"</RFPARM>");
				str.append("</REFFECT>");
			}
			str.append("</REFFECTS>");
		}


		if((culturalAbilityNames==null)||(culturalAbilityNames.length==0))
			str.append("<CABILITIES/>");
		else
		{
			str.append("<CABILITIES>");
			for(int r=0;r<culturalAbilityNames.length;r++)
			{
				str.append("<CABILITY>");
				str.append("<CCLASS>"+culturalAbilityNames[r]+"</CCLASS>");
				str.append("<CPROFF>"+culturalAbilityProficiencies[r]+"</CPROFF>");
				str.append("</CABILITY>");
			}
			str.append("</CABILITIES>");
		}
        if(xtraValues==null)
            xtraValues=CMProps.getExtraStatCodesHolder(this);
        for(int i=this.getSaveStatIndex();i<getStatCodes().length;i++)
            str.append(CMLib.xml().convertXMLtoTag(getStatCodes()[i],getStat(getStatCodes()[i])));
		str.append("</RACE>");
		return str.toString();
	}
	public void setRacialParms(String parms)
	{
		if(parms.trim().length()==0)
		{
			Log.errOut("GenRace","Unable to parse empty xml");
			return;
		}
		Vector xml=CMLib.xml().parseAllXML(parms);
		if(xml==null)
		{
			Log.errOut("GenRace","Unable to parse xml: "+parms);
			return;
		}
		Vector raceData=CMLib.xml().getRealContentsFromPieces(xml,"RACE");
		if(raceData==null){	Log.errOut("GenRace","Unable to get RACE data: ("+parms.length()+"): "+CMStrings.padRight(parms,30)+"."); return;}
		String id=CMLib.xml().getValFromPieces(raceData,"ID");
		if(id.length()==0)
		{
			Log.errOut("GenRace","Unable to parse: "+parms);
			return;
		}
		ID=id;
		name=CMLib.xml().getValFromPieces(raceData,"NAME");
		if((name==null)||(name.length()==0))
		{
			Log.errOut("GenRace","Not able to parse: "+parms);
			return;
		}

		String rcat=CMLib.xml().getValFromPieces(raceData,"CAT");
		if((rcat==null)||(rcat.length()==0))
		{
			rcat=name;
			return;
		}

		racialCategory=rcat;
		forbiddenWornBits=CMLib.xml().getLongFromPieces(raceData,"WEAR");
		weightVariance=CMLib.xml().getIntFromPieces(raceData,"VWEIGHT");
		lightestWeight=CMLib.xml().getIntFromPieces(raceData,"BWEIGHT");
		heightVariance=CMLib.xml().getIntFromPieces(raceData,"VHEIGHT");
		shortestFemale=CMLib.xml().getIntFromPieces(raceData,"FHEIGHT");
		shortestMale=CMLib.xml().getIntFromPieces(raceData,"MHEIGHT");
		helpEntry=CMLib.xml().restoreAngleBrackets(CMLib.xml().getValFromPieces(raceData,"HELP"));
		String playerval=CMLib.xml().getValFromPieces(raceData,"PLAYER").trim().toUpperCase();
		if(playerval.length()>0)
		{
			if(playerval.startsWith("T"))
				availability=Area.THEME_FANTASY;
			else
			if(playerval.startsWith("F"))
				availability=0;
			else
			switch(CMath.s_int(playerval))
			{
			case 0: availability=Area.THEME_FANTASY; break;
			case 1: availability=Area.THEME_FANTASY|Area.THEME_SKILLONLYMASK; break;
			case 2: availability=0; break;
			}
		}
		String avail=CMLib.xml().getValFromPieces(raceData,"AVAIL").trim().toUpperCase();
		if((avail!=null)&&(avail.length()>0)&&(CMath.isNumber(avail)))
		    availability=CMath.s_int(avail);
		destroyBodyAfterUse=CMLib.xml().getBoolFromPieces(raceData,"DESTROYBODY");
		leaveStr=CMLib.xml().getValFromPieces(raceData,"LEAVE");
		arriveStr=CMLib.xml().getValFromPieces(raceData,"ARRIVE");
		setStat("HEALTHRACE",CMLib.xml().getValFromPieces(raceData,"HEALTHRACE"));
		setStat("EVENTRACE",CMLib.xml().getValFromPieces(raceData,"EVENTRACE"));
		setStat("WEAPONRACE",CMLib.xml().getValFromPieces(raceData,"WEAPONRACE"));
		String body=CMLib.xml().getValFromPieces(raceData,"BODY");
		Vector V=CMParms.parseSemicolons(body,false);
		for(int v=0;v<V.size();v++)
			if(v<bodyMask().length)
				bodyMask()[v]=CMath.s_int((String)V.elementAt(v));
		adjEStats=null;
        String eStats=CMLib.xml().getValFromPieces(raceData,"ESTATS");
        if(eStats.length()>0){ adjEStats=(EnvStats)CMClass.getCommon("DefaultEnvStats"); adjEStats.setAllValues(0); CMLib.coffeeMaker().setEnvStats(adjEStats,eStats);}
        adjStats=null;
        String aStats=CMLib.xml().getValFromPieces(raceData,"ASTATS");
        if(aStats.length()>0){ adjStats=(CharStats)CMClass.getCommon("DefaultCharStats"); adjStats.setAllValues(0); CMLib.coffeeMaker().setCharStats(adjStats,aStats);}
        setStats=null;
        String cStats=CMLib.xml().getValFromPieces(raceData,"CSTATS");
        if(cStats.length()>0){ setStats=(CharStats)CMClass.getCommon("DefaultCharStats"); setStats.setAllValues(0); CMLib.coffeeMaker().setCharStats(setStats,cStats);}
        adjState=null;
        String aState=CMLib.xml().getValFromPieces(raceData,"ASTATE");
        if(aState.length()>0){ adjState=(CharState)CMClass.getCommon("DefaultCharState"); adjState.setAllValues(0); CMLib.coffeeMaker().setCharState(adjState,aState);}
        startAdjState=null;
		disableFlags=CMLib.xml().getIntFromPieces(raceData,"DISFLAGS");
		String saState=CMLib.xml().getValFromPieces(raceData,"STARTASTATE");
		if(saState.length()>0){ startAdjState=(CharState)CMClass.getCommon("DefaultCharState"); startAdjState.setAllValues(0); CMLib.coffeeMaker().setCharState(startAdjState,saState);}
		String aging=CMLib.xml().getValFromPieces(raceData,"AGING");
		Vector aV=CMParms.parseCommas(aging,true);
		for(int v=0;v<aV.size();v++)
		    getAgingChart()[v]=CMath.s_int((String)aV.elementAt(v));
		clrStatChgDesc();
		// now RESOURCES!
		Vector xV=CMLib.xml().getRealContentsFromPieces(raceData,"RESOURCES");
		resourceChoices=null;
		if((xV!=null)&&(xV.size()>0))
		{
			resourceChoices=new Vector();
			for(int x=0;x<xV.size();x++)
			{
				XMLLibrary.XMLpiece iblk=(XMLLibrary.XMLpiece)xV.elementAt(x);
				if((!iblk.tag.equalsIgnoreCase("RSCITEM"))||(iblk.contents==null))
					continue;
				Item newOne=CMClass.getItem(CMLib.xml().getValFromPieces(iblk.contents,"ICLASS"));
				String idat=CMLib.xml().getValFromPieces(iblk.contents,"IDATA");
				newOne.setMiscText(CMLib.xml().restoreAngleBrackets(idat));
				newOne.recoverEnvStats();
				resourceChoices.addElement(newOne);
			}
		}

		// now OUTFIT!
		Vector oV=CMLib.xml().getRealContentsFromPieces(raceData,"OUTFIT");
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
				if(newOne != null)
				{
					String idat=CMLib.xml().getValFromPieces(iblk.contents,"OFDATA");
					newOne.setMiscText(CMLib.xml().restoreAngleBrackets(idat));
					newOne.recoverEnvStats();
					outfitChoices.addElement(newOne);
				}
				else
					Log.errOut("GenRace","Unknown newOne race: " + CMLib.xml().getValFromPieces(iblk.contents,"OFCLASS"));
			}
		}

		naturalWeapon=null;
		Vector wblk=CMLib.xml().getRealContentsFromPieces(raceData,"WEAPON");
		if(wblk!=null)
		{
			naturalWeapon=CMClass.getWeapon(CMLib.xml().getValFromPieces(wblk,"ICLASS"));
			String idat=CMLib.xml().getValFromPieces(wblk,"IDATA");
			if((idat!=null)&&(naturalWeapon!=null))
			{
				naturalWeapon.setMiscText(CMLib.xml().restoreAngleBrackets(idat));
				naturalWeapon.recoverEnvStats();
			}
		}
		xV=CMLib.xml().getRealContentsFromPieces(raceData,"RABILITIES");
		racialAbilityNames=null;
		racialAbilityProficiencies=null;
		racialAbilityQuals=null;
		racialAbilityLevels=null;
		if((xV!=null)&&(xV.size()>0))
		{
			racialAbilityNames=new String[xV.size()];
			racialAbilityProficiencies=new int[xV.size()];
			racialAbilityQuals=new boolean[xV.size()];
			racialAbilityLevels=new int[xV.size()];
			for(int x=0;x<xV.size();x++)
			{
				XMLLibrary.XMLpiece iblk=(XMLLibrary.XMLpiece)xV.elementAt(x);
				if((!iblk.tag.equalsIgnoreCase("RABILITY"))||(iblk.contents==null))
					continue;
				racialAbilityNames[x]=CMLib.xml().getValFromPieces(iblk.contents,"RCLASS");
				racialAbilityProficiencies[x]=CMLib.xml().getIntFromPieces(iblk.contents,"RPROFF");
				racialAbilityQuals[x]=CMLib.xml().getBoolFromPieces(iblk.contents,"RAGAIN");
				racialAbilityLevels[x]=CMLib.xml().getIntFromPieces(iblk.contents,"RLEVEL");
			}
		}

		xV=CMLib.xml().getRealContentsFromPieces(raceData,"REFFECTS");
		racialEffectNames=null;
		racialEffectParms=null;
		racialEffectLevels=null;
		if((xV!=null)&&(xV.size()>0))
		{
			racialEffectNames=new String[xV.size()];
			racialEffectParms=new String[xV.size()];
			racialEffectLevels=new int[xV.size()];
			for(int x=0;x<xV.size();x++)
			{
				XMLLibrary.XMLpiece iblk=(XMLLibrary.XMLpiece)xV.elementAt(x);
				if((!iblk.tag.equalsIgnoreCase("REFFECT"))||(iblk.contents==null))
					continue;
				racialEffectNames[x]=CMLib.xml().getValFromPieces(iblk.contents,"RFCLASS");
				racialEffectParms[x]=CMLib.xml().getValFromPieces(iblk.contents,"RFPARM");
				racialEffectLevels[x]=CMLib.xml().getIntFromPieces(iblk.contents,"RFLEVEL");
			}
		}


		xV=CMLib.xml().getRealContentsFromPieces(raceData,"CABILITIES");
		culturalAbilityNames=null;
		culturalAbilityProficiencies=null;
		if((xV!=null)&&(xV.size()>0))
		{
			culturalAbilityNames=new String[xV.size()];
			culturalAbilityProficiencies=new int[xV.size()];
			for(int x=0;x<xV.size();x++)
			{
				XMLLibrary.XMLpiece iblk=(XMLLibrary.XMLpiece)xV.elementAt(x);
				if((!iblk.tag.equalsIgnoreCase("CABILITY"))||(iblk.contents==null))
					continue;
				culturalAbilityNames[x]=CMLib.xml().getValFromPieces(iblk.contents,"CCLASS");
				culturalAbilityProficiencies[x]=CMLib.xml().getIntFromPieces(iblk.contents,"CPROFF");
			}
		}
		
        xtraValues=CMProps.getExtraStatCodesHolder(this);
        for(int i=this.getSaveStatIndex();i<getStatCodes().length;i++)
            setStat(getStatCodes()[i],CMLib.xml().getValFromPieces(raceData, getStatCodes()[i]));
	}
	protected static String[] CODES={"ID","NAME","CAT","WEAR","VWEIGHT","BWEIGHT",
									 "VHEIGHT","FHEIGHT","MHEIGHT","AVAIL","LEAVE",
									 "ARRIVE","HEALTHRACE","BODY","ESTATS",
									 "ASTATS","CSTATS","ASTATE",
									 "NUMRSC","GETRSCID","GETRSCPARM",
									 "WEAPONCLASS","WEAPONXML",
									 "NUMRABLE","GETRABLE","GETRABLEPROF","GETRABLEQUAL","GETRABLELVL",
									 "NUMCABLE","GETCABLE","GETCABLEPROF",
									 "NUMOFT","GETOFTID","GETOFTPARM","BODYKILL",
									 "NUMREFF","GETREFF","GETREFFPARM","GETREFFLVL","AGING",
									 "DISFLAGS","STARTASTATE","EVENTRACE","WEAPONRACE", "HELP"
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
		case 0: return ID();
		case 1: return name();
		case 2: return racialCategory;
		case 3: return ""+forbiddenWornBits();
		case 4: return ""+weightVariance();
		case 5: return ""+lightestWeight();
		case 6: return ""+heightVariance();
		case 7: return ""+shortestFemale();
		case 8: return ""+shortestMale();
		case 9: return ""+availabilityCode();
		case 10: return leaveStr();
		case 11: return arriveStr();
		case 12: return getRaceLocatorID(healthBuddy);
		case 13:
		{
			StringBuffer bbody=new StringBuffer("");
			for(int i=0;i<bodyMask().length;i++)
				bbody.append((""+bodyMask()[i])+";");
			return bbody.toString();
		}
		case 14: return (adjEStats==null)?"":CMLib.coffeeMaker().getEnvStatsStr(adjEStats);
		case 15: return (adjStats==null)?"":CMLib.coffeeMaker().getCharStatsStr(adjStats);
		case 16: return (setStats==null)?"":CMLib.coffeeMaker().getCharStatsStr(setStats);
		case 17: return (adjState==null)?"":CMLib.coffeeMaker().getCharStateStr(adjState);
		case 18: return ""+myResources().size();
		case 19: return ""+((Item)myResources().elementAt(num)).ID();
		case 20: return ""+((Item)myResources().elementAt(num)).text();
		case 21: return (naturalWeapon==null)?"":naturalWeapon.ID();
		case 22: return (naturalWeapon==null)?"":naturalWeapon.text();
		case 23: return (racialAbilityNames==null)?"0":(""+racialAbilityNames.length);
		case 24: return (racialAbilityNames==null)?"":(""+racialAbilityNames[num]);
		case 25: return (racialAbilityProficiencies==null)?"0":(""+racialAbilityProficiencies[num]);
		case 26: return (racialAbilityQuals==null)?"false":(""+racialAbilityQuals[num]);
		case 27: return (racialAbilityLevels==null)?"0":(""+racialAbilityLevels[num]);
		case 28: return (culturalAbilityNames==null)?"0":(""+culturalAbilityNames.length);
		case 29: return (culturalAbilityNames==null)?"":(""+culturalAbilityNames[num]);
		case 30: return (culturalAbilityProficiencies==null)?"0":(""+culturalAbilityProficiencies[num]);
		case 31: return ""+((outfit(null)!=null)?outfit(null).size():0);
		case 32: return ""+((outfit(null)!=null)?((Item)outfit(null).elementAt(num)).ID():"");
		case 33: return ""+((outfit(null)!=null)?((Item)outfit(null).elementAt(num)).text():"");
		case 34: return ""+destroyBodyAfterUse();
		case 35: return (racialEffectNames==null)?"0":(""+racialEffectNames.length);
		case 36: return (racialEffectNames==null)?"":(""+racialEffectNames[num]);
		case 37: return (racialEffectParms==null)?"0":(""+racialEffectParms[num]);
		case 38: return (racialEffectLevels==null)?"0":(""+racialEffectLevels[num]);
		case 39: return CMParms.toStringList(getAgingChart());
		case 40: return ""+disableFlags;
		case 41: return (startAdjState==null)?"":CMLib.coffeeMaker().getCharStateStr(startAdjState);
		case 42: return getRaceLocatorID(eventBuddy);
		case 43: return getRaceLocatorID(weaponBuddy);
		case 44: return helpEntry;
        default:
            return CMProps.getStatCodeExtensionValue(getStatCodes(), xtraValues, code);
        }
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

	public void startRacing(MOB mob, boolean verifyOnly)
	{
	    super.startRacing(mob,verifyOnly);
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
		case 1: name=val; break;
		case 2: racialCategory=val; break;
		case 3: forbiddenWornBits=CMath.s_long(val); break;
		case 4: weightVariance=CMath.s_parseIntExpression(val); break;
		case 5: lightestWeight=CMath.s_parseIntExpression(val); break;
		case 6: heightVariance=CMath.s_parseIntExpression(val); break;
		case 7: shortestFemale=CMath.s_parseIntExpression(val); break;
		case 8: shortestMale=CMath.s_parseIntExpression(val); break;
		case 9: availability=CMath.s_parseBitIntExpression(Area.THEME_DESCS,val); break;
		case 10: leaveStr=val;break;
		case 11: arriveStr=val;break;
		case 12:
		{
			healthBuddy=CMClass.getRace(val);
			try{
				if(healthBuddy==null)
					healthBuddy=(Race)CMClass.unsortedLoadClass("RACE",val,true);
			}catch(Exception e){}
			break;
		}
		case 13:
		{
			Vector V=CMParms.parseSemicolons(val,false);
			for(int v=0;v<V.size();v++)
				if(v<bodyMask().length)
					bodyMask()[v]=CMath.s_int((String)V.elementAt(v));
			break;
		}
		case 14: adjEStats=null;clrStatChgDesc();if(val.length()>0){adjEStats=(EnvStats)CMClass.getCommon("DefaultEnvStats"); adjEStats.setAllValues(0); CMLib.coffeeMaker().setEnvStats(adjEStats,val);}break;
		case 15: adjStats=null;clrStatChgDesc();if(val.length()>0){adjStats=(CharStats)CMClass.getCommon("DefaultCharStats"); adjStats.setAllValues(0); CMLib.coffeeMaker().setCharStats(adjStats,val);}break;
		case 16: setStats=null;clrStatChgDesc();if(val.length()>0){setStats=(CharStats)CMClass.getCommon("DefaultCharStats"); setStats.setAllValues(0); CMLib.coffeeMaker().setCharStats(setStats,val);}break;
		case 17: adjState=null;clrStatChgDesc();if(val.length()>0){adjState=(CharState)CMClass.getCommon("DefaultCharState"); adjState.setAllValues(0); CMLib.coffeeMaker().setCharState(adjState,val);}break;
		case 18: if(CMath.s_int(val)==0) resourceChoices=null; else resourceChoices=new Vector(CMath.s_int(val)); break;
		case 19: {   if(resourceChoices==null) resourceChoices=new Vector();
					 if(num>=resourceChoices.size())
						resourceChoices.addElement(CMClass.getItem(val));
					 else
				        resourceChoices.setElementAt(CMClass.getItem(val),num);
					 break;
				 }
		case 20: {
                     if((resourceChoices!=null)&&(num<resourceChoices.size()))
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
		case 23: racialAbilityMap=null;
				 if(CMath.s_int(val)==0){
					 racialAbilityNames=null;
					 racialAbilityProficiencies=null;
					 racialAbilityQuals=null;
					 racialAbilityLevels=null;
				 }
				 else{
					 racialAbilityNames=new String[CMath.s_int(val)];
					 racialAbilityProficiencies=new int[CMath.s_int(val)];
					 racialAbilityQuals=new boolean[CMath.s_int(val)];
					 racialAbilityLevels=new int[CMath.s_int(val)];
				 }
				 break;
		case 24: {   if(racialAbilityNames==null) racialAbilityNames=new String[num+1];
				     racialAbilityNames[num]=val;
					 break;
				 }
		case 25: {   if(racialAbilityProficiencies==null) racialAbilityProficiencies=new int[num+1];
				     racialAbilityProficiencies[num]=CMath.s_parseIntExpression(val);
					 break;
				 }
		case 26: {   if(racialAbilityQuals==null) racialAbilityQuals=new boolean[num+1];
				     racialAbilityQuals[num]=CMath.s_bool(val);
					 break;
				 }
		case 27: {   if(racialAbilityLevels==null) racialAbilityLevels=new int[num+1];
				     racialAbilityLevels[num]=CMath.s_parseIntExpression(val);
					 break;
				 }
		case 28: if(CMath.s_int(val)==0){
					 culturalAbilityNames=null;
					 culturalAbilityProficiencies=null;
				 }
				 else{
					 culturalAbilityNames=new String[CMath.s_int(val)];
					 culturalAbilityProficiencies=new int[CMath.s_int(val)];
				 }
				 break;
		case 29: {   if(culturalAbilityNames==null) culturalAbilityNames=new String[num+1];
				     culturalAbilityNames[num]=val;
					 break;
				 }
		case 30: {   if(culturalAbilityProficiencies==null) culturalAbilityProficiencies=new int[num+1];
				     culturalAbilityProficiencies[num]=CMath.s_int(val);
					 break;
				 }
		case 31: if(CMath.s_int(val)==0) outfitChoices=null; else outfitChoices=new Vector(CMath.s_int(val)); break;
		case 32: {   if(outfitChoices==null) outfitChoices=new Vector();
					 if(num>=outfitChoices.size())
						outfitChoices.addElement(CMClass.getItem(val));
					 else
				        outfitChoices.setElementAt(CMClass.getItem(val),num);
					 break;
				 }
		case 33: {   if((outfitChoices!=null)&&(num<outfitChoices.size()))
					 {
						Item I=(Item)outfitChoices.elementAt(num);
						I.setMiscText(val);
						I.recoverEnvStats();
					 }
					 break;
				 }
		case 34: destroyBodyAfterUse=CMath.s_bool(val); break;
		case 35: racialEffectMap=null;
				 if(CMath.s_int(val)==0){
					 racialEffectNames=null;
					 racialEffectParms=null;
					 racialEffectLevels=null;
				 }
				 else{
					 racialEffectNames=new String[CMath.s_int(val)];
					 racialEffectParms=new String[CMath.s_int(val)];
					 racialEffectLevels=new int[CMath.s_int(val)];
				 }
				 break;
		case 36: {   if(racialEffectNames==null) racialEffectNames=new String[num+1];
				     racialEffectNames[num]=val;
					 break;
				 }
		case 37: {   if(racialEffectParms==null) racialEffectParms=new String[num+1];
				     racialEffectParms[num]=val;
					 break;
				 }
		case 38: {   if(racialEffectLevels==null) racialEffectLevels=new int[num+1];
				     racialEffectLevels[num]=CMath.s_int(val);
					 break;
				 }
		case 39: {
					Vector aV=CMParms.parseCommas(val,true);
					for(int v=0;v<aV.size();v++)
					    getAgingChart()[v]=CMath.s_int((String)aV.elementAt(v));
		    		break;
				 }
		case 40: disableFlags=CMath.s_int(val); break;
		case 41: startAdjState=null;clrStatChgDesc();if(val.length()>0){startAdjState=(CharState)CMClass.getCommon("DefaultCharState"); startAdjState.setAllValues(0); CMLib.coffeeMaker().setCharState(startAdjState,val);}break;
		case 42:
		{
			eventBuddy=CMClass.getRace(val);
			if(eventBuddy==null)
				eventBuddy=(Race)CMClass.unsortedLoadClass("RACE",val,true);
			break;
		}
		case 43:
		{
			weaponBuddy=CMClass.getRace(val);
			if(weaponBuddy==null)
				weaponBuddy=(Race)CMClass.unsortedLoadClass("RACE",val,true);
			break;
		}
		case 44:
		{
		    helpEntry=val;
		    break;
		}
        default:
            CMProps.setStatCodeExtensionValue(getStatCodes(), xtraValues, code, val);
            break;
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
	public boolean sameAs(Race E)
	{
		if(!(E instanceof GenRace)) return false;
		if(((GenRace)E).racialParms().equals(racialParms()))
			return true;
		return false;
	}
}
