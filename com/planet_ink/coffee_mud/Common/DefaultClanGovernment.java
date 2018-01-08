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
   Copyright 2011-2018 Bo Zimmerman

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
	@Override
	public String ID()
	{
		return "DefaultClanGovernment";
	}

	@Override
	public String name()
	{
		return ID();
	}

	/** If this is a default government type, this is its ID, otherwise -1 */
	public int		ID;
	/** The name of this government type, which is its identifier when ID above is -1 */
	public String	name;
	/** The category of this government type.  Players can only belong to one of each category */
	public String 	category;
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
	/** Entry script parameter */
	public String	entryScriptParam;
	/** Exit script parameter */
	public String	exitScriptParam;
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
	public ClanPosition[] 				positions;
	/** Whether an unfilled topRole is automatically filled by those who meet its innermask  */
	public Clan.AutoPromoteFlag 		autoPromoteBy;
	/** A fast-lookup cache for positions */
	protected Map<Object,ClanPosition>	positionMap=new Hashtable<Object,ClanPosition>();

	// derived variable
	public static final SearchIDList<Ability>  emptyIDs = new CMUniqSortSVec<Ability>(1);
	public static final List<Ability>		   empty 	= new ReadOnlyList<Ability>(new Vector<Ability>());
	public static final String 				   DEFAULT_XP_FORMULA = "(500 * @x1) + (1000 * @x1 * @x1 * @x1)";
	public CMath.CompiledFormula xpCalculationFormula = CMath.compileMathExpression(DEFAULT_XP_FORMULA);

	// derived and internal vars
	protected Map<Integer,Map<Integer,SearchIDList<Ability>>>
							clanAbilityMap			=null;
	protected String[]		clanEffectNames			=null;
	protected int[]			clanEffectLevels		=null;
	protected String[]		clanEffectParms			=null;
	protected Set<Integer>[]clanEffectRoles			=null;
	
	protected Map<Integer,Map<Integer,SearchIDList<Ability>>>
							clanEffectMap			=null;
	protected String[]		clanAbilityNames		=null;
	protected int[]			clanAbilityLevels		=null;
	protected int[]			clanAbilityProficiencies=null;
	protected boolean[]		clanAbilityQuals		=null;
	protected Set<Integer>[]clanAbilityRoles		=null;
	protected String[]		clanAbilityParms		=null;

	/** return a new instance of the object */
	@Override
	public CMObject newInstance()
	{
		try
		{
			return getClass().newInstance();
		}
		catch (final Exception e)
		{
			return new DefaultClanGovernment();
		}
	}

	@Override
	public void initializeClass()
	{
	}

	@Override
	public int compareTo(CMObject o)
	{
		return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
	}

	@Override
	public CMObject copyOf()
	{
		try
		{
			return (ClanGovernment)this.clone();
		}
		catch(final CloneNotSupportedException e)
		{
			return new DefaultClanGovernment();
		}
	}

	@Override
	public int getID()
	{
		return ID;
	}

	@Override
	public void setID(int iD)
	{
		ID = iD;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public void setName(String name)
	{
		this.name = name;
	}

	@Override
	public String getCategory()
	{
		return category;
	}

	@Override
	public void setCategory(String category)
	{
		if(category==null)
			category="";
		this.category=category.toUpperCase();
	}

	@Override
	public int getAutoRole()
	{
		return autoRole;
	}

	@Override
	public void setAutoRole(int autoRole)
	{
		this.autoRole = autoRole;
	}

	@Override
	public boolean isRivalrous() 
	{
		return isRivalrous;
	}

	@Override
	public void setRivalrous(boolean isRivalrous)
	{
		this.isRivalrous=isRivalrous;
	}

	@Override
	public int getAcceptPos()
	{
		return acceptPos;
	}

	@Override
	public void setAcceptPos(int acceptPos)
	{
		this.acceptPos = acceptPos;
	}

	@Override
	public String getShortDesc()
	{
		return shortDesc;
	}

	@Override
	public void setShortDesc(String shortDesc)
	{
		this.shortDesc = shortDesc;
	}

	@Override
	public String getLongDesc()
	{
		return longDesc;
	}

	@Override
	public void setLongDesc(String longDesc)
	{
		this.longDesc = longDesc;
		this.helpStr = null;
	}

	@Override
	public String getRequiredMaskStr()
	{
		return requiredMaskStr;
	}

	@Override
	public void setRequiredMaskStr(String requiredMaskStr)
	{
		this.requiredMaskStr = requiredMaskStr;
	}

	@Override
	public boolean isPublic()
	{
		return isPublic;
	}

	@Override
	public void setPublic(boolean isPublic)
	{
		this.isPublic = isPublic;
	}

	@Override
	public boolean isFamilyOnly()
	{
		return isFamilyOnly;
	}

	@Override
	public void setFamilyOnly(boolean isFamilyOnly)
	{
		this.isFamilyOnly = isFamilyOnly;
	}

	@Override
	public Integer getOverrideMinMembers()
	{
		return overrideMinMembers;
	}

	@Override
	public void setOverrideMinMembers(Integer overrideMinMembers)
	{
		this.overrideMinMembers = overrideMinMembers;
	}

	@Override
	public boolean isConquestEnabled()
	{
		return conquestEnabled;
	}

	@Override
	public void setConquestEnabled(boolean conquestEnabled)
	{
		this.conquestEnabled = conquestEnabled;
	}

	@Override
	public boolean isConquestItemLoyalty()
	{
		return conquestItemLoyalty;
	}

	@Override
	public void setConquestItemLoyalty(boolean conquestItemLoyalty)
	{
		this.conquestItemLoyalty = conquestItemLoyalty;
	}

	@Override
	public boolean isConquestByWorship()
	{
		return conquestByWorship;
	}

	@Override
	public void setConquestByWorship(boolean conquestByWorship)
	{
		this.conquestByWorship = conquestByWorship;
	}

	@Override
	public int getMaxVoteDays()
	{
		return maxVoteDays;
	}

	@Override
	public void setMaxVoteDays(int maxVoteDays)
	{
		this.maxVoteDays = maxVoteDays;
	}

	@Override
	public int getVoteQuorumPct()
	{
		return voteQuorumPct;
	}

	@Override
	public void setVoteQuorumPct(int voteQuorumPct)
	{
		this.voteQuorumPct = voteQuorumPct;
	}

	@Override
	public String getXpCalculationFormulaStr()
	{
		return xpCalculationFormulaStr==null?"":xpCalculationFormulaStr;
	}

	@Override
	public CMath.CompiledFormula getXPCalculationFormula()
	{
		return xpCalculationFormula;
	}

	@Override
	public void setXpCalculationFormulaStr(String newXpCalculationFormula)
	{
		if(newXpCalculationFormula==null)
			newXpCalculationFormula="";
		xpCalculationFormulaStr = newXpCalculationFormula;
		if(xpCalculationFormulaStr.trim().length()==0)
			this.xpCalculationFormula = CMath.compileMathExpression(DEFAULT_XP_FORMULA);
		else
		try
		{
			this.xpCalculationFormula = CMath.compileMathExpression(xpCalculationFormulaStr);
		}
		catch(final Exception e)
		{
			Log.errOut("DefaultClanGovernment",e.getMessage());
		}
	}

	@Override
	public boolean isDefault()
	{
		return isDefault;
	}

	@Override
	public void setDefault(boolean isDefault)
	{
		this.isDefault = isDefault;
	}

	@Override
	public ClanPosition[] getPositions()
	{
		return positions;
	}

	@Override
	public void setPositions(ClanPosition[] positions)
	{
		this.positionMap.clear();
		this.positions = positions;
	}

	@Override
	public Clan.AutoPromoteFlag getAutoPromoteBy()
	{
		return autoPromoteBy;
	}

	@Override
	public void setAutoPromoteBy(Clan.AutoPromoteFlag autoPromoteBy)
	{
		this.autoPromoteBy = autoPromoteBy;
	}

	@Override
	public int[] getLevelProgression()
	{
		return levelProgression;
	}

	@Override
	public void setLevelProgression(int[] levelProgression)
	{
		this.levelProgression = levelProgression;
	}

	@Override
	public String getEntryScript()
	{
		return entryScriptParam;
	}

	@Override
	public void setEntryScript(String scriptParm)
	{
		entryScriptParam=scriptParm;
	}

	@Override
	public String getExitScript()
	{
		return exitScriptParam;
	}

	@Override
	public void setExitScript(String scriptParm)
	{
		exitScriptParam=scriptParm;
	}

	// the follow are derived, or post-create set options:
	/** The list of xp amounts to progress in level */
	public int[] 	levelProgression = new int[0];
	/** A save help entry of this government type for players */
	public String	helpStr 		 = null;

	@Override
	public ClanPosition getPosition(String pos)
	{
		if(pos==null)
			return null;
		pos=pos.trim();
		if(CMath.isInteger(pos))
		{
			final int i=CMath.s_int(pos);
			if((i>=0)&&(i<positions.length))
				return positions[i];
		}
		for(final ClanPosition P : positions)
		{
			if(P.getID().equalsIgnoreCase(pos))
				return P;
		}
		return null;
	}
	
	@Override
	public ClanPosition findPositionRole(String pos)
	{
		if(pos==null)
			return null;
		pos=pos.trim();
		if(positionMap.containsKey(pos))
			return positionMap.get(pos);
		if(CMath.isInteger(pos))
		{
			int ipos=CMath.s_int(pos);
			for(final ClanPosition P : positions)
			{
				if(P.getRoleID() == ipos)
				{
					positionMap.put(pos,P);
					return P;
				}
			}
		}
		for(final ClanPosition P : positions)
		{
			if(P.getID().equalsIgnoreCase(pos))
			{
				positionMap.put(pos,P);
				return P;
			}
		}
		for(final ClanPosition P : positions)
		{
			if(P.getName().equalsIgnoreCase(pos))
			{
				positionMap.put(pos,P);
				return P;
			}
		}
		for(final ClanPosition P : positions)
		{
			if(P.getID().toUpperCase().startsWith(pos.toUpperCase()))
			{
				positionMap.put(pos,P);
				return P;
			}
		}
		for(final ClanPosition P : positions)
		{
			if(P.getName().toUpperCase().startsWith(pos.toUpperCase()))
			{
				positionMap.put(pos,P);
				return P;
			}
		}
		return null;
	}
	
	@Override
	public ClanPosition findPositionRole(Integer roleID)
	{
		if(roleID==null) 
			return null;
		if(positionMap.containsKey(roleID))
			return positionMap.get(roleID);
		final int ipos=roleID.intValue();
		for(final ClanPosition P : positions)
		{
			if(P.getRoleID() == ipos)
			{
				positionMap.put(roleID,P);
				return P;
			}
		}
		return null;
	}
	
	@Override
	public void delPosition(ClanPosition pos)
	{
		final List<ClanPosition> newPos=new LinkedList<ClanPosition>();
		for(final ClanPosition P : positions)
		{
			if(P!=pos)
				newPos.add(P);
		}
		positions=newPos.toArray(new ClanPosition[0]);
		positionMap.clear();
	}

	@Override
	public ClanPosition addPosition()
	{
		final Authority[] pows=new Authority[Function.values().length];
		for(int i=0;i<pows.length;i++) pows[i]=Authority.CAN_NOT_DO;
		final Set<Integer> roles=new HashSet<Integer>();
		int highestRank=0;
		for(final ClanPosition pos : positions)
		{
			roles.add(Integer.valueOf(pos.getRoleID()));
			if(highestRank<pos.getRank())
				highestRank=pos.getRank();
		}
		if(positions.length>0)
		{
			for(int i=0;i<pows.length;i++)
				pows[i]=positions[0].getFunctionChart()[i];
		}
		positions=Arrays.copyOf(positions, positions.length+1);
		final ClanPosition P=(ClanPosition)CMClass.getCommon("DefaultClanPosition");
		P.setID(positions.length+""+Math.random());
		P.setRoleID(0);
		P.setRank(highestRank);
		P.setName(CMLib.lang().L("Unnamed"));
		P.setPluralName("Unnameds");
		P.setMax(Integer.MAX_VALUE);
		P.setInnerMaskStr("");
		P.setFunctionChart(pows);
		P.setPublic(true);
		positions[positions.length-1]=P;
		for(int i=0;i<positions.length;i++)
		{
			if(!roles.contains(Integer.valueOf(i)))
			{
				P.setRoleID(i);
				break;
			}
		}
		positionMap.clear();
		return P;
	}

	private static enum GOVT_STAT_CODES 
	{
		NAME,AUTOROLE,ACCEPTPOS,SHORTDESC,REQUIREDMASK,ISPUBLIC,ISFAMILYONLY,OVERRIDEMINMEMBERS,
		CONQUESTENABLED,CONQUESTITEMLOYALTY,CONQUESTDEITYBASIS,MAXVOTEDAYS,VOTEQUORUMPCT,
		AUTOPROMOTEBY,VOTEFUNCS,LONGDESC,XPLEVELFORMULA,
		NUMRABLE,GETRABLE,GETRABLEPROF,GETRABLEQUAL,GETRABLELVL,GETRABLEPARM,
		NUMREFF,GETREFF,GETREFFPARM,GETREFFLVL,CATEGORY,ISRIVALROUS,
		ENTRYSCRIPT,EXITSCRIPT,GETREFFROLE,GETRABLEROLE
	}

	@Override
	public String[] getStatCodes()
	{
		return CMParms.toStringArray(GOVT_STAT_CODES.values());
	}

	@Override
	public int getSaveStatIndex()
	{
		return GOVT_STAT_CODES.values().length;
	}

	private GOVT_STAT_CODES getStatIndex(String code)
	{
		return (GOVT_STAT_CODES) CMath.s_valueOf(GOVT_STAT_CODES.values(), code);
	}

	@Override
	public String getStat(String code)
	{
		int num=0;
		int numDex=code.length();
		while((numDex>0)&&(Character.isDigit(code.charAt(numDex-1))))
			numDex--;
		if(numDex<code.length())
		{
			num=CMath.s_int(code.substring(numDex));
			code=code.substring(0,numDex);
		}
		final GOVT_STAT_CODES stat = getStatIndex(code);
		if (stat == null)
		{
			return "";
		}
		switch(stat)
		{
		case NAME:
			return name;
		case AUTOROLE:
			return (autoRole < 0 || autoRole > positions.length) ? "" : positions[autoRole].getID();
		case ACCEPTPOS:
			return (acceptPos < 0 || acceptPos > positions.length) ? "" : positions[acceptPos].getID();
		case SHORTDESC:
			return shortDesc;
		case LONGDESC:
			return longDesc;
		case XPLEVELFORMULA:
			return xpCalculationFormulaStr == null ? "" : xpCalculationFormulaStr;
		case REQUIREDMASK:
			return requiredMaskStr;
		case ISPUBLIC:
			return Boolean.toString(isPublic);
		case ISFAMILYONLY:
			return Boolean.toString(isFamilyOnly);
		case OVERRIDEMINMEMBERS:
			return overrideMinMembers == null ? "" : overrideMinMembers.toString();
		case CONQUESTENABLED:
			return Boolean.toString(conquestEnabled);
		case CONQUESTITEMLOYALTY:
			return Boolean.toString(conquestItemLoyalty);
		case CONQUESTDEITYBASIS:
			return Boolean.toString(conquestByWorship);
		case MAXVOTEDAYS:
			return Integer.toString(maxVoteDays);
		case VOTEQUORUMPCT:
			return Integer.toString(voteQuorumPct);
		case AUTOPROMOTEBY:
			return autoPromoteBy.toString();
		case ISRIVALROUS:
			return Boolean.toString(isRivalrous);
		case ENTRYSCRIPT:
			return entryScriptParam;
		case EXITSCRIPT:
			return exitScriptParam;
		case VOTEFUNCS:
		{
			final StringBuilder str = new StringBuilder("");
			for (final ClanPosition pos : positions)
			{
				for (int a = 0; a < Function.values().length; a++)
					if (pos.getFunctionChart()[a] == Authority.MUST_VOTE_ON)
					{
						if (str.length() > 0)
							str.append(",");
						str.append(Function.values()[a]);
					}
				break;
			}
			return str.toString();
		}
		case NUMRABLE:
			return (clanAbilityNames == null) ? "0" : ("" + clanAbilityNames.length);
		case GETRABLE:
			return (clanAbilityNames == null) ? "" : ("" + clanAbilityNames[num]);
		case GETRABLEPROF:
			return (clanAbilityProficiencies == null) ? "0" : ("" + clanAbilityProficiencies[num]);
		case GETRABLEQUAL:
			return (clanAbilityQuals == null) ? "false" : ("" + clanAbilityQuals[num]);
		case GETRABLELVL:
			return (clanAbilityLevels == null) ? "0" : ("" + clanAbilityLevels[num]);
		case GETRABLEROLE:
			return (clanAbilityRoles == null) ? "" : (CMParms.toListString(clanAbilityRoles[num]));
		case GETRABLEPARM:
			return (clanAbilityParms == null) ? "0" : ("" + clanAbilityParms[num]);
		case NUMREFF:
			return (clanEffectNames == null) ? "0" : ("" + clanEffectNames.length);
		case GETREFF:
			return (clanEffectNames == null) ? "" : ("" + clanEffectNames[num]);
		case GETREFFPARM:
			return (clanEffectParms == null) ? "0" : ("" + clanEffectParms[num]);
		case GETREFFLVL:
			return (clanEffectLevels == null) ? "0" : ("" + clanEffectLevels[num]);
		case GETREFFROLE:
			return (clanEffectRoles == null) ? "" : (CMParms.toListString(clanEffectRoles[num]));
		case CATEGORY:
			return category;
		default:
			Log.errOut("Clan", "getStat:Unhandled:" + stat.toString());
			break;
		}
		return "";
	}

	@Override
	public boolean isStat(String code)
	{
		return getStatIndex(code) != null;
	}

	@SuppressWarnings("unchecked")

	@Override
	public void setStat(String code, String val)
	{
		int num=0;
		int numDex=code.length();
		while((numDex>0)&&(Character.isDigit(code.charAt(numDex-1))))
			numDex--;
		if(numDex<code.length())
		{
			num=CMath.s_int(code.substring(numDex));
			code=code.substring(0,numDex);
		}
		final GOVT_STAT_CODES stat = getStatIndex(code);
		final boolean isNull = (stat == null);
		if (isNull)
		{
			return;
		}
		switch(stat)
		{
		case NAME:
			name = val;
			break;
		case CATEGORY:
			category = val;
			break;
		case AUTOROLE:
		{
			final ClanPosition P = getPosition(val);
			if (P != null)
				autoRole = P.getRoleID();
			break;
		}
		case ACCEPTPOS:
		{
			final ClanPosition P = getPosition(val);
			if (P != null)
				acceptPos = P.getRoleID();
			break;
		}
		case SHORTDESC:
			shortDesc = val;
			break;
		case LONGDESC:
			longDesc = val;
			break;
		case XPLEVELFORMULA:
			setXpCalculationFormulaStr(val);
			break;
		case REQUIREDMASK:
			requiredMaskStr = val;
			break;
		case ISPUBLIC:
			isPublic = CMath.s_bool(val);
			break;
		case ISFAMILYONLY:
			isFamilyOnly = CMath.s_bool(val);
			break;
		case OVERRIDEMINMEMBERS:
		{
			if (val.length() == 0)
				overrideMinMembers = null;
			else
				overrideMinMembers = Integer.valueOf(CMath.s_int(val));
			break;
		}
		case CONQUESTENABLED:
			conquestEnabled = CMath.s_bool(val);
			break;
		case CONQUESTITEMLOYALTY:
			conquestItemLoyalty = CMath.s_bool(val);
			break;
		case CONQUESTDEITYBASIS:
			conquestByWorship = CMath.s_bool(val);
			break;
		case MAXVOTEDAYS:
			maxVoteDays = CMath.s_int(val);
			break;
		case VOTEQUORUMPCT:
			voteQuorumPct = CMath.s_int(val);
			break;
		case ISRIVALROUS:
			this.isRivalrous = CMath.s_bool(val);
			break;
		case ENTRYSCRIPT:
			this.entryScriptParam = val;
			break;
		case EXITSCRIPT:
			this.exitScriptParam = val;
			break;
		case AUTOPROMOTEBY:
		{
			final Clan.AutoPromoteFlag flag = (Clan.AutoPromoteFlag) CMath.s_valueOf(Clan.AutoPromoteFlag.values(), val);
			if (flag != null)
				autoPromoteBy = flag;
			break;
		}
		case VOTEFUNCS:
		{
			final List<String> funcs = CMParms.parseCommas(val.toUpperCase().trim(), true);
			for (final ClanPosition pos : positions)
			{
				for (int a = 0; a < Function.values().length; a++)
				{
					if (pos.getFunctionChart()[a] == Authority.MUST_VOTE_ON)
						pos.getFunctionChart()[a] = Authority.CAN_NOT_DO;
				}
				for (final String funcName : funcs)
				{
					final Clan.Function func = (Clan.Function) CMath.s_valueOf(Function.values(), funcName);
					if (func != null)
						pos.getFunctionChart()[func.ordinal()] = Authority.MUST_VOTE_ON;
				}
			}
			break;
		}
		case NUMRABLE:
			clanAbilityMap = null;
			if (CMath.s_int(val) == 0)
			{
				clanAbilityNames = null;
				clanAbilityProficiencies = null;
				clanAbilityQuals = null;
				clanAbilityLevels = null;
				clanAbilityRoles = null;
				clanAbilityParms = null;
			}
			else
			{
				clanAbilityParms = new String[CMath.s_int(val)];
				clanAbilityNames = new String[CMath.s_int(val)];
				clanAbilityProficiencies = new int[CMath.s_int(val)];
				clanAbilityQuals = new boolean[CMath.s_int(val)];
				clanAbilityLevels = new int[CMath.s_int(val)];
				clanAbilityRoles = new Set[CMath.s_int(val)];
			}
			break;
		case GETRABLE:
		{
			if (clanAbilityNames == null)
				clanAbilityNames = new String[num + 1];
			clanAbilityNames[num] = val;
			break;
		}
		case GETRABLEPROF:
		{
			if (clanAbilityProficiencies == null)
				clanAbilityProficiencies = new int[num + 1];
			clanAbilityProficiencies[num] = CMath.s_parseIntExpression(val);
			break;
		}
		case GETRABLEPARM:
		{
			if (clanAbilityParms == null)
				clanAbilityParms = new String[num + 1];
			clanAbilityParms[num] = val;
			break;
		}
		case GETRABLEQUAL:
		{
			if (clanAbilityQuals == null)
				clanAbilityQuals = new boolean[num + 1];
			clanAbilityQuals[num] = CMath.s_bool(val);
			break;
		}
		case GETRABLELVL:
		{
			if (clanAbilityLevels == null)
				clanAbilityLevels = new int[num + 1];
			clanAbilityLevels[num] = CMath.s_parseIntExpression(val);
			break;
		}
		case GETRABLEROLE:
		{
			if (clanAbilityRoles == null)
				clanAbilityRoles = new Set[num + 1];
			final List<String> partsList = CMParms.parseCommas(val, true);
			final Set<Integer> roleSet = new HashSet<Integer>();
			for (final String part : partsList)
			{
				final ClanPosition P = findPositionRole(part);
				if (P != null)
					roleSet.add(Integer.valueOf(P.getRoleID()));
			}
			clanAbilityRoles[num] = roleSet;
			break;
		}
		case NUMREFF:
			clanEffectMap = null;
			if (CMath.s_int(val) == 0)
			{
				clanEffectNames = null;
				clanEffectParms = null;
				clanEffectLevels = null;
				clanEffectRoles = null;
			}
			else
			{
				clanEffectNames = new String[CMath.s_int(val)];
				clanEffectParms = new String[CMath.s_int(val)];
				clanEffectLevels = new int[CMath.s_int(val)];
				clanEffectRoles = new Set[CMath.s_int(val)];
			}
			break;
		case GETREFF:
		{
			if (clanEffectNames == null)
				clanEffectNames = new String[num + 1];
			clanEffectNames[num] = val;
			break;
		}
		case GETREFFPARM:
		{
			if (clanEffectParms == null)
				clanEffectParms = new String[num + 1];
			clanEffectParms[num] = val;
			break;
		}
		case GETREFFROLE:
		{
			if (clanEffectRoles == null)
				clanEffectRoles = new Set[num + 1];
			final List<String> partsList = CMParms.parseCommas(val, true);
			final Set<Integer> roleSet = new HashSet<Integer>();
			for (final String part : partsList)
			{
				final ClanPosition P = findPositionRole(part);
				if (P != null)
					roleSet.add(Integer.valueOf(P.getRoleID()));
			}
			clanEffectRoles[num] = roleSet;
			break;
		}
		case GETREFFLVL:
		{
			if (clanEffectLevels == null)
				clanEffectLevels = new int[num + 1];
			clanEffectLevels[num] = CMath.s_int(val);
			break;
		}
		default:
			break;
		}
	}

	@Override
	public String getHelpStr()
	{
		if(getLongDesc().length()==0)
			return null;
		if(helpStr==null)
		{
			final StringBuilder str=new StringBuilder(CMLib.lang().L("\n\rOrganization type: "+getName()+"\n\r\n\r"));
			str.append(getLongDesc()).append("\n\r");
			str.append(CMLib.lang().L("\n\rAuthority Chart:\n\r\n\r"));
			final List<ClanPosition> showablePositions=new Vector<ClanPosition>();
			for(final ClanPosition P : getPositions())
			{
				boolean showMe=false;
				for(final Clan.Authority a : P.getFunctionChart())
				{
					if(a==Authority.CAN_DO)
						showMe=true;
				}
				if(showMe)
					showablePositions.add(P);
			}
			final List<ClanPosition> sortedPositions=new Vector<ClanPosition>();
			while(sortedPositions.size() < showablePositions.size())
			{
				ClanPosition highPos=null;
				for(final ClanPosition P : showablePositions)
				{
					if((!sortedPositions.contains(P))
					&&((highPos==null)||(highPos.getRank()<P.getRank())))
						highPos=P;
				}
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
			final String[] functionNames=new String[Clan.Function.values().length];
			for(int f=0;f<Clan.Function.values().length;f++)
			{
				final Clan.Function func=Clan.Function.values()[f];
				funcTotal+=func.name().length()+1;
				if(func.name().length() > funcMaxLen)
					funcMaxLen=func.name().length()+1;
				functionNames[f]=func.name();
			}
			final int funcAvg = funcTotal / Clan.Function.values().length;
			final int funcMaxAvg = (int)CMath.round(funcAvg * 1.3);
			while((funcMaxLen > funcMaxAvg)&&((funcMaxAvg + posTotalLen)>78))
				funcMaxLen--;
			if(posses.length>0)
			{
				while((funcMaxLen + posTotalLen) > 78)
				{
					int highPos=0;
					for(int p=1;p<sortedPositions.size();p++)
					{
						if(posses[p]>posses[highPos])
							highPos=p;
					}
					posTotalLen--;
					posses[highPos]--;
				}
			}

			final int commandColLen = funcMaxLen;
			str.append(CMStrings.padRight(CMLib.lang().L("Command"),commandColLen-1)).append("!");
			for(int p=0;p<posses.length;p++)
			{
				final ClanPosition pos = sortedPositions.get(p);
				final String name=CMStrings.capitalizeAndLower(pos.getName().replace('_',' '));
				str.append(CMStrings.padRight(name,posses[p]-1));
				if(p<posses.length-1)
					str.append("!");
			}
			str.append("\n\r");
			final Object lineDraw = new Object()
			{
				private static final String line = "----------------------------------------------------------------------------";

				@Override
				public String toString()
				{
					final StringBuilder s=new StringBuilder("");
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
			for(final Clan.Function func : Clan.Function.values())
			{
				final String fname=CMStrings.capitalizeAndLower(func.toString().replace('_', ' '));
				str.append(CMStrings.padRight(fname,commandColLen-1)).append("!");
				for(int p=0;p<sortedPositions.size();p++)
				{
					final ClanPosition pos = sortedPositions.get(p);
					final Authority auth = pos.getFunctionChart()[func.ordinal()];
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
				str.append(CMLib.lang().L("\n\rBenefits per Clan Level:\n\r"));
				int maxLevel=-1;
				for(final int x : clanEffectLevels) if(x>maxLevel) maxLevel=x;
				for(final int x : clanAbilityLevels) if(x>maxLevel) maxLevel=x;
				for(int l=1;l<=maxLevel;l++)
				{
					final List<String> levelBenefits=new LinkedList<String>();
					for(int x=0;x<clanEffectLevels.length;x++)
					{
						if(clanEffectLevels[x]==l)
						{
							final Ability A=CMClass.getAbility(clanEffectNames[x]);
							if(A!=null)
							{
								final String roleNames;
								final Set<Integer> roleSet=clanEffectRoles[x];
								if((roleSet!=null)&&(roleSet.size()>0))
								{
									final List<String> roleList=new ArrayList<String>();
									for(final Integer posI : roleSet)
									{
										final ClanPosition P=findPositionRole(posI);
										if(P!=null)
											roleList.add(CMStrings.capitalizeAndLower(P.getName()));
									}
									roleNames=CMLib.english().toEnglishStringList(roleList);
								}
								else
									roleNames=CMLib.lang().L("Members");
								A.setMiscText(clanEffectParms[x]);
								String desc=A.accountForYourself();
								if((desc==null)||(desc.length()==0))
									desc=CMLib.lang().L("@x1 gain the following effect: @x2",roleNames,A.name());
								levelBenefits.add(desc);
							}
						}
					}
					for(int x=0;x<clanAbilityLevels.length;x++)
					{
						if(clanAbilityLevels[x]==l)
						{
							final Ability A=CMClass.getAbility(clanAbilityNames[x]);
							if(A!=null)
							{
								final String roleNames;
								final Set<Integer> roleSet=clanAbilityRoles[x];
								if((roleSet!=null)&&(roleSet.size()>0))
								{
									final List<String> roleList=new ArrayList<String>();
									for(final Integer posI : roleSet)
									{
										final ClanPosition P=findPositionRole(posI);
										if(P!=null)
											roleList.add(CMStrings.capitalizeAndLower(P.getName()));
									}
									roleNames=CMLib.english().toEnglishStringList(roleList);
								}
								else
									roleNames=CMLib.lang().L("Members");
								if(clanAbilityQuals[x])
									levelBenefits.add(CMLib.lang().L("@x1 qualify for: @x2",roleNames,A.name()));
								else
									levelBenefits.add(CMLib.lang().L("@x1 automatically gain: @x2",roleNames,A.name()));
							}
						}
					}
					for(final String bene : levelBenefits)
						str.append(CMLib.lang().L("Level @x1: @x2\n\r",""+l,bene));
				}
			}
			helpStr=str.toString();
		}
		return helpStr;
	}

	@Override
	public SearchIDList<Ability> getClanLevelAbilities(MOB mob, Clan clan, Integer level)
	{
		final String clanGvtID=name;
		if((clanAbilityMap==null)
		&&(clanAbilityNames!=null)
		&&(clanAbilityLevels!=null)
		&&(clanAbilityRoles!=null)
		&&(clanAbilityProficiencies!=null)
		&&(clanAbilityQuals!=null))
		{
			CMLib.ableMapper().delCharMappings(clanGvtID); // necessary for a "clean start"
			clanAbilityMap=new Hashtable<Integer,Map<Integer,SearchIDList<Ability>>>();
			for(int i=0;i<clanAbilityNames.length;i++)
			{
				final Ability A=CMClass.getAbility(clanAbilityNames[i]);
				if(A!=null)
				{
					String extraMask;
					if(clanAbilityRoles[i].size()>0)
					{
						extraMask="-CLAN \"+*(";
						for(Integer I : clanAbilityRoles[i])
						{
							ClanPosition P=this.findPositionRole(I);
							if(P==null)
								extraMask=extraMask+I.toString()+" ";
							else
								extraMask=extraMask+P.getID()+" ";
						}
						extraMask=extraMask.trim()+")\"";
					}
					else
						extraMask="";
						
					final AbilityMapper.AbilityMapping ableMap=
					CMLib.ableMapper().addDynaAbilityMapping(clanGvtID,
															 clanAbilityLevels[i],
															 A.ID(),
															 clanAbilityProficiencies[i],
															 clanAbilityParms[i],
															 !clanAbilityQuals[i],
															 false,
															 extraMask);
					if(ableMap != null)
					{
						for(Integer I : clanAbilityRoles[i])
							ableMap.extFields().put(I.toString(), I.toString());
					}
				}
			}
		}
		if(clanAbilityMap==null)
			return emptyIDs;
		final Integer mobClanRole;
		if((mob==null)||(clan==null))
			mobClanRole=Integer.valueOf(Integer.MAX_VALUE);
		else
		{
			final Pair<Clan,Integer> mobClanRolePair=mob.getClanRole(clan.clanID());
			if(mobClanRolePair == null)
				return emptyIDs;
			else
				mobClanRole=mobClanRolePair.second;
		}
		if(level==null) 
			level=Integer.valueOf(Integer.MAX_VALUE);
		Map<Integer,SearchIDList<Ability>> subClanAbilityMap=clanAbilityMap.get(level);
		if(subClanAbilityMap==null) 
		{
			subClanAbilityMap=new Hashtable<Integer,SearchIDList<Ability>>();
			clanAbilityMap.put(level, subClanAbilityMap);
		}
		if(subClanAbilityMap.containsKey(mobClanRole))
			return subClanAbilityMap.get(mobClanRole);
		final List<AbilityMapper.AbilityMapping> V=CMLib.ableMapper().getUpToLevelListings(clanGvtID,level.intValue(),true,((mob!=null)&&(clan!=null)));
		final CMUniqSortSVec<Ability> finalV=new CMUniqSortSVec<Ability>();
		for(final AbilityMapper.AbilityMapping able : V)
		{
			final Ability A=CMClass.getAbility(able.abilityID());
			if((A!=null)
			&&((mobClanRole==null)
			 ||(mobClanRole.intValue()==Integer.MAX_VALUE)
			 ||(able.extFields().size()==0)
			 ||(able.extFields().containsKey(mobClanRole.toString()))))
			{
				A.setProficiency(CMLib.ableMapper().getDefaultProficiency(clanGvtID,false,A.ID()));
				A.setSavable(false);
				A.setMiscText(CMLib.ableMapper().getDefaultParm(clanGvtID,false,A.ID()));
				finalV.add(A);
			}
		}
		finalV.trimToSize();
		subClanAbilityMap.put(mobClanRole,finalV);
		return finalV;
	}

	public List<Ability> getClanLevelEffectsList(Integer mobClanRole, Integer level)
	{
		if(clanEffectNames==null)
			return empty;

		if((clanEffectMap==null)
		&&(clanEffectNames!=null)
		&&(clanEffectLevels!=null)
		&&(clanEffectRoles!=null)
		&&(clanEffectParms!=null))
			clanEffectMap=new Hashtable<Integer,Map<Integer,SearchIDList<Ability>>>();

		if(clanEffectMap==null) 
			return empty;

		if(mobClanRole==null)
			mobClanRole = Integer.valueOf(Integer.MAX_VALUE);
		
		if(level==null)
			level = Integer.valueOf(Integer.MAX_VALUE);

		Map<Integer,SearchIDList<Ability>> subClanEffectMap=clanEffectMap.get(level);
		if(subClanEffectMap == null)
		{
			subClanEffectMap = new Hashtable<Integer,SearchIDList<Ability>>();
			clanEffectMap.put(level, subClanEffectMap);
		}
		
		if(subClanEffectMap.containsKey(mobClanRole))
			return subClanEffectMap.get(mobClanRole);

		final CMSortSVec<Ability> finalV = new CMSortSVec<Ability>();
		for(int v=0;v<clanEffectLevels.length;v++)
		{
			if((clanEffectLevels[v]<=level.intValue())
			&&(clanEffectNames.length>v)
			&&(clanEffectParms.length>v)
			&&(clanEffectRoles.length>v))
			{
				final Set<Integer> clanRolesMap=clanEffectRoles[v];
				final Ability A=CMClass.getAbility(clanEffectNames[v]);
				if((A!=null)
				&&((clanRolesMap==null)
				 ||(clanRolesMap.size()==0)
				 ||(mobClanRole.intValue()==Integer.MAX_VALUE)
				 ||(clanRolesMap.contains(mobClanRole))))
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
		subClanEffectMap.put(mobClanRole,finalV);
		return finalV;
	}

	public ChameleonList<Ability> getEmptyClanLevelEffects(final MOB mob, final Clan clan)
	{
		return new ChameleonList<Ability>(empty,
			new ChameleonList.Signaler<Ability>(empty)
			{
				@Override
				public boolean isDeprecated()
				{
					return (mob != null) && (mob.clans().iterator().hasNext());
				}
	
				@Override
				public void rebuild(final ChameleonList<Ability> me)
				{
					if((mob==null)||(clan==null))
						return;
					if(mob.getClanRole(clan.clanID())!=null)
						me.changeMeInto(clan.getGovernment().getClanLevelEffects(mob, clan, Integer.valueOf(clan.getClanLevel())));
				}
			});
	}
	
	private Integer getMobClanRoleOrNull(final MOB mob, final Clan clan)
	{
		if((mob!=null)&&(clan!=null))
		{
			final Pair<Clan,Integer> mobClanRolePair=mob.getClanRole(clan.clanID());
			if(mobClanRolePair != null)
				return mobClanRolePair.second;
			else
				return null;
		}
		else
			return null;
	}

	@Override
	public ChameleonList<Ability> getClanLevelEffects(final MOB mob, final Clan clan, final Integer level)
	{
		if(level == null)
			return getEmptyClanLevelEffects(mob, clan);
		final DefaultClanGovernment myGovt = this;
		final Integer mobClanRole=getMobClanRoleOrNull(mob, clan);
		final List<Ability> myList=getClanLevelEffectsList(mobClanRole, level);
		final List<Ability> finalV=new Vector<Ability>(myList.size());
		for(final Ability A : myList)
		{
			final Ability finalA=(Ability)A.copyOf();
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
				@Override
				public boolean isDeprecated()
				{
					return false;
				}

				@Override
				public void rebuild(final ChameleonList<Ability> me)
				{
				}
			});
		}
		else
		{
			finalFinalV = new ChameleonList<Ability>(finalV,
				new ChameleonList.Signaler<Ability>(myList)
				{
					@Override
					public boolean isDeprecated()
					{
						if((mob.amDestroyed())||(clan==null))
							return true;
						final Integer mobClanRole=getMobClanRoleOrNull(mob, clan);
						if(mobClanRole==null)
							return true;
						if((clan.getGovernment() != myGovt)
						|| (getClanLevelEffectsList(mobClanRole, Integer.valueOf(clan.getClanLevel())) != oldReferenceListRef.get()))
							return true;
						return false;
					}

					@Override
					public void rebuild(final ChameleonList<Ability> me)
					{
						final Clan C=clan;
						if((mob.amDestroyed())||(C==null))
							me.changeMeInto(getEmptyClanLevelEffects(mob,C));
						else
							me.changeMeInto(C.getGovernment().getClanLevelEffects(mob, clan, Integer.valueOf(C.getClanLevel())));
					}
				});
		}
		return finalFinalV;
	}
}
