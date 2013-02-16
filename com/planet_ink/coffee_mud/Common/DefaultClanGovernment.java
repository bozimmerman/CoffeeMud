package com.planet_ink.coffee_mud.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.threads.ServiceEngine;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Clan.Function;
import com.planet_ink.coffee_mud.Common.interfaces.Clan.Authority;
import com.planet_ink.coffee_mud.Common.interfaces.Clan.ClanVote;
import com.planet_ink.coffee_mud.Common.interfaces.Clan.MemberRecord;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine.PlayerData;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.StdRace;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.lang.ref.WeakReference;
import java.util.*;
/*
Copyright 2000-2012 Bo Zimmerman

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
public class DefaultClanGovernment implements ClanGovernment
{
	public String ID(){return "DefaultClanGovernment";}
	public String name() { return ID();}
	
	/** If this is a default government type, this is its ID, otherwise -1 */
	public int		ID;
	/** The name of this government type, which is its identifier when ID above is -1 */
	public String	name;
	/** The category of this government type.  Players can only belong to one of each category */
	public String category;
	/** The role automatically assigned to those who apply successfully */
	public int		autoRole;
	/** The role automatically assigned to those who are accepted */
	public int		acceptPos;
	/** A short description of this government type for players */
	public String	shortDesc;
	/** A long description of this government type for players */
	public String	longDesc;
	/** Zapper mask for requirements to even apply */
	public String	requiredMaskStr;
	/**  Whether this clan type is shown on the list  */
	public boolean	isPublic;
	/**  Whether mambers must all be in the same family */
	public boolean	isFamilyOnly;
	/** Whether clans made from this government are rivalrous by default */
	public boolean isRivalrous;
	/**  The number of minimum members for the clan to survive -- overrides coffeemud.ini */
	public Integer	overrideMinMembers;
	/** Whether conquest is enabled for this clan */
	public boolean	conquestEnabled;
	/** Whether clan items increase loyalty in conquered areas for this clan type */
	public boolean	conquestItemLoyalty;
	/** Whether loyalty and conquest are determined by what deity the mobs are */
	public boolean	conquestByWorship;
	/** maximum number of mud days a vote will go on for */
	public int		maxVoteDays;
	/** minimum % of voters who must have voted for a vote to be valid if time expires*/
	public int		voteQuorumPct;
	/** uncompiled level xp calculation formula */
	public String 	xpCalculationFormulaStr;
	/**  Whether this is the default government  */
	public boolean	isDefault 		 = false;
	/** The list of ClanPosition objects for each holdable position in this government */
	public ClanPosition[] 			positions;
	/** Whether an unfilled topRole is automatically filled by those who meet its innermask  */
	public Clan.AutoPromoteFlag 	autoPromoteBy;

	// derived variable
	public static final SearchIDList<Ability>  emptyIDs = new CMUniqSortSVec<Ability>(1);
	public static final List<Ability>		   empty 	= new ReadOnlyList<Ability>(new Vector<Ability>());
	public static final String 				   DEFAULT_XP_FORMULA = "(500 * @x1) + (1000 * @x1 * @x1 * @x1)";
	public LinkedList<CMath.CompiledOperation> xpCalculationFormula = CMath.compileMathExpression(DEFAULT_XP_FORMULA);

	// derived and internal vars
	protected Map<Integer,SearchIDList<Ability>>
						clanAbilityMap			=null;
	protected Map<Integer,SearchIDList<Ability>>
						clanEffectMap			=null;
	protected String[] 	clanEffectNames			=null;
	protected int[] 	clanEffectLevels		=null;
	protected String[] 	clanEffectParms			=null;
	protected String[] 	clanAbilityNames		=null;
	protected int[] 	clanAbilityLevels		=null;
	protected int[] 	clanAbilityProficiencies=null;
	protected boolean[] clanAbilityQuals		=null;
	
	/** return a new instance of the object*/
	public CMObject newInstance(){try{return (CMObject)getClass().newInstance();}catch(Exception e){return new DefaultClanGovernment();}}
	public void initializeClass(){}
	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
	public CMObject copyOf()
	{
		try
		{
			return (ClanGovernment)this.clone();
		}
		catch(CloneNotSupportedException e)
		{
			return new DefaultClanGovernment();
		}
	}

	public int getID() {
		return ID;
	}
	public void setID(int iD) {
		ID = iD;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category){
		if(category==null) category="";
		this.category=category.toUpperCase();
	}
	public int getAutoRole() {
		return autoRole;
	}
	public void setAutoRole(int autoRole) {
		this.autoRole = autoRole;
	}
	public boolean isRivalrous() { 
		return isRivalrous; 
	}
	public void setRivalrous(boolean isRivalrous) {
		this.isRivalrous=isRivalrous;
	}
	public int getAcceptPos() {
		return acceptPos;
	}
	public void setAcceptPos(int acceptPos) {
		this.acceptPos = acceptPos;
	}
	public String getShortDesc() {
		return shortDesc;
	}
	public void setShortDesc(String shortDesc) {
		this.shortDesc = shortDesc;
	}
	public String getLongDesc() {
		return longDesc;
	}
	public void setLongDesc(String longDesc) {
		this.longDesc = longDesc;
		this.helpStr = null;
	}
	public String getRequiredMaskStr() {
		return requiredMaskStr;
	}
	public void setRequiredMaskStr(String requiredMaskStr) {
		this.requiredMaskStr = requiredMaskStr;
	}
	public boolean isPublic() {
		return isPublic;
	}
	public void setPublic(boolean isPublic) {
		this.isPublic = isPublic;
	}
	public boolean isFamilyOnly() {
		return isFamilyOnly;
	}
	public void setFamilyOnly(boolean isFamilyOnly) {
		this.isFamilyOnly = isFamilyOnly;
	}
	public Integer getOverrideMinMembers() {
		return overrideMinMembers;
	}
	public void setOverrideMinMembers(Integer overrideMinMembers) {
		this.overrideMinMembers = overrideMinMembers;
	}
	public boolean isConquestEnabled() {
		return conquestEnabled;
	}
	public void setConquestEnabled(boolean conquestEnabled) {
		this.conquestEnabled = conquestEnabled;
	}
	public boolean isConquestItemLoyalty() {
		return conquestItemLoyalty;
	}
	public void setConquestItemLoyalty(boolean conquestItemLoyalty) {
		this.conquestItemLoyalty = conquestItemLoyalty;
	}
	public boolean isConquestByWorship() {
		return conquestByWorship;
	}
	public void setConquestByWorship(boolean conquestByWorship) {
		this.conquestByWorship = conquestByWorship;
	}
	public int getMaxVoteDays() {
		return maxVoteDays;
	}
	public void setMaxVoteDays(int maxVoteDays) {
		this.maxVoteDays = maxVoteDays;
	}
	public int getVoteQuorumPct() {
		return voteQuorumPct;
	}
	public void setVoteQuorumPct(int voteQuorumPct) {
		this.voteQuorumPct = voteQuorumPct;
	}
	public String getXpCalculationFormulaStr() {
		return xpCalculationFormulaStr==null?"":xpCalculationFormulaStr;
	}
	public LinkedList<CMath.CompiledOperation> getXPCalculationFormula()
	{
		return xpCalculationFormula;
	}
	public void setXpCalculationFormulaStr(String newXpCalculationFormula) {
		if(newXpCalculationFormula==null) newXpCalculationFormula="";
		xpCalculationFormulaStr = newXpCalculationFormula;
		if(xpCalculationFormulaStr.trim().length()==0)
			this.xpCalculationFormula = CMath.compileMathExpression(DEFAULT_XP_FORMULA);
		else
		try
		{
			this.xpCalculationFormula = CMath.compileMathExpression(xpCalculationFormulaStr);
		}
		catch(Exception e)
		{
			Log.errOut("DefaultClanGovernment",e.getMessage());
		}
	}
	public boolean isDefault() {
		return isDefault;
	}
	public void setDefault(boolean isDefault) {
		this.isDefault = isDefault;
	}
	public ClanPosition[] getPositions() {
		return positions;
	}
	public void setPositions(ClanPosition[] positions) {
		this.positions = positions;
	}
	public Clan.AutoPromoteFlag getAutoPromoteBy() {
		return autoPromoteBy;
	}
	public void setAutoPromoteBy(Clan.AutoPromoteFlag autoPromoteBy) {
		this.autoPromoteBy = autoPromoteBy;
	}
	public int[] getLevelProgression() {
		return levelProgression;
	}
	public void setLevelProgression(int[] levelProgression) {
		this.levelProgression = levelProgression;
	}
	
	// the follow are derived, or post-create set options:
	/** The list of xp amounts to progress in level */
	public int[] 	levelProgression = new int[0];
	/** A save help entry of this government type for players */
	public String	helpStr 		 = null;
	
	public ClanPosition getPosition(String pos)
	{
		if(pos==null) return null;
		pos=pos.trim();
		if(CMath.isInteger(pos))
		{
			int i=CMath.s_int(pos);
			if((i>=0)&&(i<positions.length))
				return positions[i];
		}
		for(ClanPosition P : positions)
			if(P.getID().equalsIgnoreCase(pos))
				return P;
		return null;
	}
	public void delPosition(ClanPosition pos)
	{
		List<ClanPosition> newPos=new LinkedList<ClanPosition>();
		for(ClanPosition P : positions)
			if(P!=pos) newPos.add(P);
		positions=newPos.toArray(new ClanPosition[0]);
	}
	public ClanPosition addPosition()
	{
		Authority[] pows=new Authority[Function.values().length];
		for(int i=0;i<pows.length;i++) pows[i]=Authority.CAN_NOT_DO;
		Set<Integer> roles=new HashSet<Integer>();
		int highestRank=0;
		for(ClanPosition pos : positions)
		{
			roles.add(Integer.valueOf(pos.getRoleID()));
			if(highestRank<pos.getRank())
				highestRank=pos.getRank();
		}
		if(positions.length>0)
			for(int i=0;i<pows.length;i++)
				pows[i]=positions[0].getFunctionChart()[i];
		positions=Arrays.copyOf(positions, positions.length+1);
		ClanPosition P=(ClanPosition)CMClass.getCommon("DefaultClanPosition");
		P.setID(positions.length+""+Math.random());
		P.setRoleID(0);
		P.setRank(highestRank);
		P.setName("Unnamed");
		P.setPluralName("Unnameds");
		P.setMax(Integer.MAX_VALUE);
		P.setInnerMaskStr("");
		P.setFunctionChart(pows);
		P.setPublic(true);
		positions[positions.length-1]=P;
		for(int i=0;i<positions.length;i++)
			if(!roles.contains(Integer.valueOf(i)))
			{
				P.setRoleID(i);
				break;
			}
		return P;
	}
	private static enum GOVT_STAT_CODES {
		NAME,AUTOROLE,ACCEPTPOS,SHORTDESC,REQUIREDMASK,ISPUBLIC,ISFAMILYONLY,OVERRIDEMINMEMBERS,
		CONQUESTENABLED,CONQUESTITEMLOYALTY,CONQUESTDEITYBASIS,MAXVOTEDAYS,VOTEQUORUMPCT,
		AUTOPROMOTEBY,VOTEFUNCS,LONGDESC,XPLEVELFORMULA,
		NUMRABLE,GETRABLE,GETRABLEPROF,GETRABLEQUAL,GETRABLELVL,
		NUMREFF,GETREFF,GETREFFPARM,GETREFFLVL,CATEGORY,ISRIVALROUS
	}
	
	public String[] getStatCodes() { return CMParms.toStringArray(GOVT_STAT_CODES.values());}
	public int getSaveStatIndex() { return GOVT_STAT_CODES.values().length;}
	private GOVT_STAT_CODES getStatIndex(String code) { return (GOVT_STAT_CODES)CMath.s_valueOf(GOVT_STAT_CODES.values(),code); }
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
		final GOVT_STAT_CODES stat = getStatIndex(code);
		if(stat==null){ return "";}
		switch(stat)
		{
		case NAME: return name;
		case AUTOROLE: return (autoRole < 0 || autoRole > positions.length) ? "" : positions[autoRole].getID();
		case ACCEPTPOS: return (acceptPos < 0 || acceptPos > positions.length) ? "" : positions[acceptPos].getID();
		case SHORTDESC: return shortDesc;
		case LONGDESC: return longDesc;
		case XPLEVELFORMULA: return xpCalculationFormulaStr==null?"":xpCalculationFormulaStr;
		case REQUIREDMASK: return requiredMaskStr;
		case ISPUBLIC: return Boolean.toString(isPublic);
		case ISFAMILYONLY: return Boolean.toString(isFamilyOnly);
		case OVERRIDEMINMEMBERS: return overrideMinMembers == null ? "" : overrideMinMembers.toString();
		case CONQUESTENABLED: return Boolean.toString(conquestEnabled);
		case CONQUESTITEMLOYALTY: return Boolean.toString(conquestItemLoyalty);
		case CONQUESTDEITYBASIS: return Boolean.toString(conquestByWorship);
		case MAXVOTEDAYS: return Integer.toString(maxVoteDays);
		case VOTEQUORUMPCT: return Integer.toString(voteQuorumPct);
		case AUTOPROMOTEBY: return autoPromoteBy.toString();
		case ISRIVALROUS: return Boolean.toString(isRivalrous);
		case VOTEFUNCS:{
			final StringBuilder str=new StringBuilder("");
			for(ClanPosition pos : positions)
			{
				for(int a=0;a<Function.values().length;a++)
					if(pos.getFunctionChart()[a]==Authority.MUST_VOTE_ON)
					{
						if(str.length()>0) str.append(",");
						str.append(Function.values()[a]);
					}
				break;
			}
			return str.toString();
		}
		case NUMRABLE: return (clanAbilityNames==null)?"0":(""+clanAbilityNames.length);
		case GETRABLE: return (clanAbilityNames==null)?"":(""+clanAbilityNames[num]);
		case GETRABLEPROF: return (clanAbilityProficiencies==null)?"0":(""+clanAbilityProficiencies[num]);
		case GETRABLEQUAL: return (clanAbilityQuals==null)?"false":(""+clanAbilityQuals[num]);
		case GETRABLELVL: return (clanAbilityLevels==null)?"0":(""+clanAbilityLevels[num]);
		case NUMREFF: return (clanEffectNames==null)?"0":(""+clanEffectNames.length);
		case GETREFF: return (clanEffectNames==null)?"":(""+clanEffectNames[num]);
		case GETREFFPARM: return (clanEffectParms==null)?"0":(""+clanEffectParms[num]);
		case GETREFFLVL: return (clanEffectLevels==null)?"0":(""+clanEffectLevels[num]);
		case CATEGORY: return category;
		default: Log.errOut("Clan","getStat:Unhandled:"+stat.toString()); break;
		}
		return "";
	}
	public boolean isStat(String code) { return getStatIndex(code)!=null;}
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
		final GOVT_STAT_CODES stat = getStatIndex(code);
		if(stat==null){ return;}
		switch(stat)
		{
		case NAME: name=val; break;
		case CATEGORY: category=val; break;
		case AUTOROLE: { ClanPosition P=getPosition(val); if(P!=null) autoRole=P.getRoleID(); break; }
		case ACCEPTPOS: { ClanPosition P=getPosition(val); if(P!=null) acceptPos=P.getRoleID(); break; }
		case SHORTDESC: shortDesc=val; break;
		case LONGDESC: longDesc=val; break;
		case XPLEVELFORMULA: setXpCalculationFormulaStr(val); break;
		case REQUIREDMASK: requiredMaskStr=val;break; 
		case ISPUBLIC: isPublic=CMath.s_bool(val); break;
		case ISFAMILYONLY: isFamilyOnly=CMath.s_bool(val); break;
		case OVERRIDEMINMEMBERS: {
			if(val.length()==0) overrideMinMembers = null; 
			else overrideMinMembers=Integer.valueOf(CMath.s_int(val)); 
			break;
		}
		case CONQUESTENABLED: conquestEnabled=CMath.s_bool(val); break;
		case CONQUESTITEMLOYALTY: conquestItemLoyalty=CMath.s_bool(val); break;
		case CONQUESTDEITYBASIS: conquestByWorship=CMath.s_bool(val); break;
		case MAXVOTEDAYS: maxVoteDays=CMath.s_int(val); break;
		case VOTEQUORUMPCT: voteQuorumPct=CMath.s_int(val); break;
		case ISRIVALROUS: this.isRivalrous=CMath.s_bool(val); break;
		case AUTOPROMOTEBY:{
			Clan.AutoPromoteFlag flag=(Clan.AutoPromoteFlag)CMath.s_valueOf(Clan.AutoPromoteFlag.values(),val);
			if(flag!=null) autoPromoteBy=flag;
			break;
		}
		case VOTEFUNCS:{
			final Vector<String> funcs=CMParms.parseCommas(val.toUpperCase().trim(), true);
			for(ClanPosition pos : positions)
			{
				for(int a=0;a<Function.values().length;a++)
					if(pos.getFunctionChart()[a]==Authority.MUST_VOTE_ON)
						pos.getFunctionChart()[a]=Authority.CAN_NOT_DO;
				for(final String funcName : funcs)
				{
					Clan.Function func=(Clan.Function)CMath.s_valueOf(Function.values(), funcName);
					if(func!=null) pos.getFunctionChart()[func.ordinal()] = Authority.MUST_VOTE_ON;
				}
			}
			break;
		}
		case NUMRABLE: 
				 clanAbilityMap=null;
				 if(CMath.s_int(val)==0){
					 clanAbilityNames=null;
					 clanAbilityProficiencies=null;
					 clanAbilityQuals=null;
					 clanAbilityLevels=null;
				 }
				 else{
					 clanAbilityNames=new String[CMath.s_int(val)];
					 clanAbilityProficiencies=new int[CMath.s_int(val)];
					 clanAbilityQuals=new boolean[CMath.s_int(val)];
					 clanAbilityLevels=new int[CMath.s_int(val)];
				 }
				 break;
		case GETRABLE: 
				 {   if(clanAbilityNames==null) clanAbilityNames=new String[num+1];
					 clanAbilityNames[num]=val;
					 break;
				 }
		case GETRABLEPROF: 
				 {   if(clanAbilityProficiencies==null) clanAbilityProficiencies=new int[num+1];
					 clanAbilityProficiencies[num]=CMath.s_parseIntExpression(val);
					 break;
				 }
		case GETRABLEQUAL: 
				 {   if(clanAbilityQuals==null) clanAbilityQuals=new boolean[num+1];
					 clanAbilityQuals[num]=CMath.s_bool(val);
					 break;
				 }
		case GETRABLELVL: 
				 {   if(clanAbilityLevels==null) clanAbilityLevels=new int[num+1];
					 clanAbilityLevels[num]=CMath.s_parseIntExpression(val);
					 break;
				 }
		case NUMREFF: 
				 clanEffectMap=null;
				 if(CMath.s_int(val)==0){
					 clanEffectNames=null;
					 clanEffectParms=null;
					 clanEffectLevels=null;
				 }
				 else{
					 clanEffectNames=new String[CMath.s_int(val)];
					 clanEffectParms=new String[CMath.s_int(val)];
					 clanEffectLevels=new int[CMath.s_int(val)];
				 }
				 break;
		case GETREFF: 
		 		 {   if(clanEffectNames==null) clanEffectNames=new String[num+1];
					 clanEffectNames[num]=val;
					 break;
				 }
		case GETREFFPARM: 
				 {   if(clanEffectParms==null) clanEffectParms=new String[num+1];
					 clanEffectParms[num]=val;
					 break;
				 }
		case GETREFFLVL: 
		 		 {   if(clanEffectLevels==null) clanEffectLevels=new int[num+1];
					 clanEffectLevels[num]=CMath.s_int(val);
					 break;
				 }
		default: Log.errOut("Clan","setStat:Unhandled:"+stat.toString()); break;
		}
	}
	
	public String getHelpStr() 
	{
		if(getLongDesc().length()==0)
			return null;
		if(helpStr==null)
		{
			StringBuilder str=new StringBuilder("\n\rOrganization type: "+getName()+"\n\r\n\r");
			str.append(getLongDesc()).append("\n\r");
			str.append("\n\rAuthority Chart:\n\r\n\r");
			final List<ClanPosition> showablePositions=new Vector<ClanPosition>();
			for(ClanPosition P : getPositions())
			{
				boolean showMe=false;
				for(Clan.Authority a : P.getFunctionChart())
					if(a==Authority.CAN_DO)
						showMe=true;
				if(showMe)
					showablePositions.add(P);
			}
			final List<ClanPosition> sortedPositions=new Vector<ClanPosition>();
			while(sortedPositions.size() < showablePositions.size())
			{
				ClanPosition highPos=null;
				for(ClanPosition P : showablePositions)
					if((!sortedPositions.contains(P))
					&&((highPos==null)||(highPos.getRank()<P.getRank())))
						highPos=P;
				sortedPositions.add(highPos);
			}
			final int[] posses=new int[sortedPositions.size()];
			int posTotalLen=0;
			for(int p=0;p<sortedPositions.size();p++)
			{
				posses[p]=sortedPositions.get(p).getName().length()+2;
				posTotalLen+=posses[p];
			}
			int funcMaxLen=0;
			int funcTotal=0;
			String[] functionNames=new String[Clan.Function.values().length];
			for(int f=0;f<Clan.Function.values().length;f++)
			{
				Clan.Function func=Clan.Function.values()[f];
				funcTotal+=func.name().length()+1;
				if(func.name().length() > funcMaxLen)
					funcMaxLen=func.name().length()+1;
				functionNames[f]=func.name();
			}
			int funcAvg = funcTotal / Clan.Function.values().length;
			int funcMaxAvg = (int)CMath.round((double)funcAvg * 1.3);
			while((funcMaxLen > funcMaxAvg)&&((funcMaxAvg + posTotalLen)>78))
				funcMaxLen--;
			if(posses.length>0)
				while((funcMaxLen + posTotalLen) > 78)
				{
					int highPos=0;
					for(int p=1;p<sortedPositions.size();p++)
						if(posses[p]>posses[highPos])
							highPos=p;
					posTotalLen--;
					posses[highPos]--;
				}
			
			final int commandColLen = funcMaxLen;
			str.append(CMStrings.padRight("Command",commandColLen-1)).append("!");
			for(int p=0;p<posses.length;p++)
			{
				ClanPosition pos = sortedPositions.get(p);
				String name=CMStrings.capitalizeAndLower(pos.getName().replace('_',' '));
				str.append(CMStrings.padRight(name,posses[p]-1));
				if(p<posses.length-1)
					str.append("!");
			}
			str.append("\n\r");
			Object lineDraw = new Object(){
				private static final String line = "----------------------------------------------------------------------------"; 
				public String toString() {
					StringBuilder s=new StringBuilder("");
					s.append(line.substring(0,commandColLen-1)).append("+");
					for(int p=0;p<posses.length;p++)
					{
						s.append(CMStrings.padRight(line,posses[p]-1));
						if(p<posses.length-1)
							s.append("+");
					}
					return s.toString();
				}
			};
			str.append(lineDraw.toString()).append("\n\r");
			for(Clan.Function func : Clan.Function.values())
			{
				String fname=CMStrings.capitalizeAndLower(func.toString().replace('_', ' '));
				str.append(CMStrings.padRight(fname,commandColLen-1)).append("!");
				for(int p=0;p<sortedPositions.size();p++)
				{
					ClanPosition pos = sortedPositions.get(p);
					Authority auth = pos.getFunctionChart()[func.ordinal()];
					String x = "";
					if(auth==Authority.CAN_DO)
						x="X";
					else
					if(auth==Authority.MUST_VOTE_ON)
						x="v";
					str.append(CMStrings.padCenter(x,posses[p]-1));
					if(p<posses.length-1)
						str.append("!");
				}
				str.append("\n\r").append(lineDraw.toString()).append("\n\r");
			}
			
			if((clanAbilityLevels!=null)&&(clanEffectLevels!=null)
			&&(clanAbilityLevels.length>0)&&(clanEffectLevels.length>0))
			{
				str.append("\n\rBenefits per Clan Level:\n\r");
				int maxLevel=-1;
				for(int x : clanEffectLevels) if(x>maxLevel) maxLevel=x;
				for(int x : clanAbilityLevels) if(x>maxLevel) maxLevel=x;
				for(int l=1;l<=maxLevel;l++)
				{
					final List<String> levelBenefits=new LinkedList<String>();
					for(int x=0;x<clanEffectLevels.length;x++)
						if(clanEffectLevels[x]==l) 
						{
							final Ability A=CMClass.getAbility(clanEffectNames[x]);
							if(A!=null)
							{
								A.setMiscText(clanEffectParms[x]);
								String desc=A.accountForYourself();
								if((desc==null)||(desc.length()==0))
									desc="Members gain the following effect: "+A.name();
								levelBenefits.add(desc);
							}
						}
					for(int x=0;x<clanAbilityLevels.length;x++)
						if(clanAbilityLevels[x]==l) 
						{
							final Ability A=CMClass.getAbility(clanAbilityNames[x]);
							if(A!=null)
							{
								if(clanAbilityQuals[x])
									levelBenefits.add("Members qualify for: "+A.name());
								else
									levelBenefits.add("Members automatically gain: "+A.name());
							}
						}
					for(final String bene : levelBenefits)
						str.append("Level "+l+": "+bene+"\n\r");
				}
			}
			helpStr=str.toString();
		}
		return helpStr;
	}

	public SearchIDList<Ability> getClanLevelAbilities(Integer level)
	{
		final String clanGvtID=name;
		if((clanAbilityMap==null)
		&&(clanAbilityNames!=null)
		&&(clanAbilityLevels!=null)
		&&(clanAbilityProficiencies!=null)
		&&(clanAbilityQuals!=null))
		{
			CMLib.ableMapper().delCharMappings(ID()); // necessary for a "clean start"
			clanAbilityMap=new Hashtable<Integer,SearchIDList<Ability>>();
			for(int i=0;i<clanAbilityNames.length;i++)
			{
				Ability A=CMClass.getAbility(clanAbilityNames[i]);
				if(A!=null)
				{
					CMLib.ableMapper().addDynaAbilityMapping(clanGvtID,
															 clanAbilityLevels[i],
															 A.ID(),
															 clanAbilityProficiencies[i],
															 "",
															 !clanAbilityQuals[i],
															 false);
				}
			}
		}
		if(clanAbilityMap==null) return emptyIDs;
		if(level==null) level=Integer.valueOf(Integer.MAX_VALUE);
		if(clanAbilityMap.containsKey(level))
			return clanAbilityMap.get(level);
		List<AbilityMapper.AbilityMapping> V=CMLib.ableMapper().getUpToLevelListings(clanGvtID,level.intValue(),true,true);
		CMUniqSortSVec<Ability> finalV=new CMUniqSortSVec<Ability>();
		for(AbilityMapper.AbilityMapping able : V)
		{
			Ability A=CMClass.getAbility(able.abilityID);
			if(A!=null)
			{
				A.setProficiency(CMLib.ableMapper().getDefaultProficiency(clanGvtID,false,A.ID()));
				A.setSavable(false);
				A.setMiscText(CMLib.ableMapper().getDefaultParm(clanGvtID,false,A.ID()));
				finalV.add(A);
			}
		}
		finalV.trimToSize();
		clanAbilityMap.put(level,finalV);
		return finalV;
	}

	public List<Ability> getClanLevelEffectsList(final MOB mob, final Integer level)
	{
		if(clanEffectNames==null)
			return empty;

		if((clanEffectMap==null)
		&&(clanEffectNames!=null)
		&&(clanEffectLevels!=null)
		&&(clanEffectParms!=null))
			clanEffectMap=new Hashtable<Integer,SearchIDList<Ability>>();

		if(clanEffectMap==null) return empty;
		
		if(clanEffectMap.containsKey(level))
			return clanEffectMap.get(level); 
		final CMSortSVec<Ability> finalV = new CMSortSVec<Ability>();
		for(int v=0;v<clanEffectLevels.length;v++)
		{
			if((clanEffectLevels[v]<=level.intValue())
			&&(clanEffectNames.length>v)
			&&(clanEffectParms.length>v))
			{
				Ability A=CMClass.getAbility(clanEffectNames[v]);
				if(A!=null)
				{
					// mob was set to null here to make the cache map actually relevant .. see caching below
					A.setProficiency(CMLib.ableMapper().getMaxProficiency((MOB)null, true, A.ID()));
					A.setMiscText(clanEffectParms[v]);
					A.makeNonUninvokable();
					A.setSavable(false); // must go AFTER the ablve
					finalV.add(A);
				}
			}
		}
		finalV.trimToSize();
		clanEffectMap.put(level,finalV);
		return finalV;
	}
	
	public int getClanLevelEffectsSize(final MOB mob, final Integer level)
	{
		return getClanLevelEffectsList(mob, level).size();
	}
	
	public ChameleonList<Ability> getEmptyClanLevelEffects(final MOB mob, final Clan clan)
	{
		return new ChameleonList<Ability>(empty,
			new ChameleonList.Signaler<Ability>(empty) 
			{
				public boolean isDeprecated() { return (mob!=null)&&(mob.clans().iterator().hasNext());}
				public void rebuild(final ChameleonList<Ability> me)
				{
					if((mob==null)||(clan==null)) return;
					if(mob.getClanRole(clan.clanID())!=null)
						me.changeMeInto(clan.getGovernment().getClanLevelEffects(mob, clan, Integer.valueOf(clan.getClanLevel())));
				}
			});
	}
	
	public ChameleonList<Ability> getClanLevelEffects(final MOB mob, final Clan clan, final Integer level)
	{
		if(level == null) return getEmptyClanLevelEffects(mob, clan);
		final DefaultClanGovernment myGovt = this;
		final List<Ability> myList=getClanLevelEffectsList(mob, level);
		final List<Ability> finalV=new Vector<Ability>(myList.size());
		for(final Ability A : myList)
		{
			Ability finalA=(Ability)A.copyOf();
			finalA.makeNonUninvokable();
			finalA.setSavable(false); // must come AFTER the above
			finalA.setAffectedOne(mob);
			finalV.add(finalA);
		}
		final ChameleonList<Ability> finalFinalV;
		if(mob==null)
		{
			finalFinalV = new ChameleonList<Ability>(finalV,
			new ChameleonList.Signaler<Ability>(myList) 
			{
				public boolean isDeprecated() { return false;}
				public void rebuild(final ChameleonList<Ability> me){}
			});
		}
		else
		{
			finalFinalV = new ChameleonList<Ability>(finalV,
				new ChameleonList.Signaler<Ability>(myList) 
				{
					public boolean isDeprecated()
					{
						if((mob==null)||(mob.amDestroyed())||(clan==null)||(mob.getClanRole(clan.clanID())==null)) 
							return true;
						if((clan.getGovernment() != myGovt)
						|| (getClanLevelEffectsList(mob, Integer.valueOf(clan.getClanLevel())) != oldReferenceListRef.get()))
							return true;
						return false;
					}
					public void rebuild(final ChameleonList<Ability> me)
					{
						
						final Clan C=(mob!=null)?clan:null;
						if((mob==null)||(mob.amDestroyed())||(C==null))
							me.changeMeInto(getEmptyClanLevelEffects(mob,C));
						else
							me.changeMeInto(C.getGovernment().getClanLevelEffects(mob, clan, Integer.valueOf(C.getClanLevel())));
					}
				});
		}
		return finalFinalV;
	}
}
