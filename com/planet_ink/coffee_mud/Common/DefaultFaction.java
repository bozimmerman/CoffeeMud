package com.planet_ink.coffee_mud.Common;
import com.planet_ink.coffee_mud.core.database.DBInterface;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMClass.CMObjectType;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.Misc.PresenceReaction;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Faction.FData;
import com.planet_ink.coffee_mud.Common.interfaces.Faction.FRange;
import com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent.MiscTrigger;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.ChannelsLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.EnglishParsing;
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
//import java.lang.reflect.*;
/**
 * Portions Copyright (c) 2003 Jeremy Vyska
 * Portions Copyright (c) 2004-2022 Bo Zimmerman
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  	  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class DefaultFaction implements Faction, MsgListener
{
	@Override
	public String ID()
	{
		return "DefaultFaction";
	}

	@Override
	public CMObject newInstance()
	{
		try
		{
			return getClass().newInstance();
		}
		catch (final Exception e)
		{
			return new DefaultFaction();
		}
	}

	@Override
	public void initializeClass()
	{
	}

	@Override
	public CMObject copyOf()
	{
		try
		{
			return (CMObject) this.clone();
		}
		catch (final Exception e)
		{
			return newInstance();
		}
	}

	@Override
	public int compareTo(final CMObject o)
	{
		return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
	}

	protected String	 _factionID			= "";
	protected String	 name				= "";
	protected String	 upName				= "";
	protected String	 choiceIntro		= "";
	protected long[]	 lastDataChange		= new long[1];
	protected int   	 minimum			= Integer.MIN_VALUE;
	protected int   	 middle				= 0;
	protected int   	 difference;
	protected int   	 maximum			= Integer.MAX_VALUE;
	protected int   	 highest			= Integer.MAX_VALUE;
	protected int   	 lowest				= Integer.MIN_VALUE;
	protected long  	 internalFlagBitmap	= 0;
	protected String	 experienceFlag		= "";
	protected boolean    useLightReactions	= false;
	protected boolean    isDisabled			= false;
	protected boolean    showInScore		= false;
	protected boolean    showInSpecialReport= false;
	protected boolean    showInEditor		= false;
	protected boolean    showInFacCommand	= true;
	protected boolean    isInherited		= true;
	protected boolean    destroyed			= false;

	protected CList<String>   					defaults		 = new SVector<String>();
	protected CList<String>   					autoDefaults	 = new SVector<String>();
	protected CMap<String,FRange> 				ranges			 = new SHashtable<String,FRange>();
	protected Map<Integer,FRange> 				rangeRangeMap	 = new PrioritizingLimitedMap<Integer,FRange>(10,60000,600000,100);
	protected CMap<String,String[]>   			affBehavs		 = new SHashtable<String,String[]>();
	protected double							rateModifier	 = 1.0;
	protected CMap<String,FactionChangeEvent[]>	changes			 = new SHashtable<String,FactionChangeEvent[]>();
	protected CMap<String,FactionChangeEvent[]>	abilChangeCache	 = new SHashtable<String,FactionChangeEvent[]>();
	protected CMap<String,FactionChangeEvent[]>	socChangeCache	 = new SHashtable<String,FactionChangeEvent[]>();
	protected CMap<Integer,FactionChangeEvent[]>msgChangeCache	 = new SHashtable<Integer,FactionChangeEvent[]>();
	protected CList<Faction.FZapFactor>			factors			 = new SVector<Faction.FZapFactor>();
	protected CMap<String,Double> 				relations		 = new SHashtable<String,Double>();
	protected CList<Faction.FAbilityUsage>		abilityUsages	 = new SVector<Faction.FAbilityUsage>();
	protected Map<String,Faction.FAbilityUsage> abilityUseCache	 = new STreeMap<String,Faction.FAbilityUsage>();
	protected Set<String> 						abilityUseMisses = new STreeSet<String>();
	protected CList<String>   					choices			 = new SVector<String>();
	protected CList<Faction.FReactionItem>		reactions		 = new SVector<Faction.FReactionItem>();
	protected CMap<String,CList<FReactionItem>>	reactionHash	 = new SHashtable<String,CList<Faction.FReactionItem>>();

	@Override
	public Enumeration<Faction.FReactionItem> reactions()
	{
		return reactions.elements();
	}

	@Override
	public Enumeration<Faction.FReactionItem> reactions(final String rangeName)
	{
		final CList<Faction.FReactionItem> V=reactionHash.get(rangeName.toUpperCase().trim());
		if(V!=null)
			return V.elements();
		return new Vector<Faction.FReactionItem>().elements();
	}

	protected Ability presenceReactionPrototype=null;

	@Override
	public String factionID()
	{
		return _factionID;
	}

	@Override
	public String name()
	{
		return name;
	}

	@Override
	public String upperName()
	{
		return upName;
	}

	@Override
	public long getInternalFlags()
	{
		return internalFlagBitmap;
	}

	@Override
	public String choiceIntro()
	{
		return choiceIntro;
	}

	@Override
	public boolean isInheritable()
	{
		return this.isInherited;
	}

	@Override
	public int minimum()
	{
		return minimum;
	}

	@Override
	public int middle()
	{
		return middle;
	}

	@Override
	public int difference()
	{
		return difference;
	}

	@Override
	public int maximum()
	{
		return maximum;
	}

	@Override
	public boolean isDisabled()
	{
		return isDisabled;
	}

	@Override
	public void disable(final boolean truefalse)
	{
		isDisabled = truefalse;
	}

	public int highest()
	{
		return highest;
	}

	public int lowest()
	{
		return lowest;
	}

	@Override
	public String experienceFlag()
	{
		return experienceFlag;
	}

	@Override
	public boolean showInScore()
	{
		return showInScore;
	}

	@Override
	public boolean showInSpecialReported()
	{
		return showInSpecialReport;
	}

	@Override
	public boolean showInEditor()
	{
		return showInEditor;
	}

	@Override
	public boolean showInFactionsCommand()
	{
		return showInFacCommand;
	}

	@Override
	public Enumeration<Faction.FRange> ranges()
	{
		return ranges.elements();
	}

	@Override
	public Enumeration<String> defaults()
	{
		return defaults.elements();
	}

	@Override
	public Enumeration<String> autoDefaults()
	{
		return autoDefaults.elements();
	}

	@Override
	public double rateModifier()
	{
		return rateModifier;
	}

	@Override
	public Enumeration<String> changeEventKeys()
	{
		return changes.keys();
	}

	@Override
	public Enumeration<Faction.FZapFactor> factors()
	{
		return factors.elements();
	}

	@Override
	public Enumeration<String> relationFactions()
	{
		return relations.keys();
	}

	@Override
	public Enumeration<Faction.FAbilityUsage> abilityUsages()
	{
		return abilityUsages.elements();
	}

	@Override
	public Enumeration<String> choices()
	{
		return choices.elements();
	}

	@Override
	public Enumeration<String> affectsBehavs()
	{
		return affBehavs.keys();
	}

	@Override
	public void setLightReactions(final boolean truefalse)
	{
		useLightReactions = truefalse;
	}

	@Override
	public boolean useLightReactions()
	{
		return useLightReactions;
	}

	@Override
	public void setFactionID(final String newStr)
	{
		_factionID = newStr;
	}

	@Override
	public void setName(final String newStr)
	{
		name = newStr;
	}

	@Override
	public void setInternalFlags(final long bitmap)
	{
		internalFlagBitmap = bitmap;
	}

	@Override
	public void setChoiceIntro(final String newStr)
	{
		choiceIntro = newStr;
	}

	@Override
	public void setExperienceFlag(final String newStr)
	{
		experienceFlag = newStr;
	}

	@Override
	public void setShowInScore(final boolean truefalse)
	{
		showInScore = truefalse;
	}

	@Override
	public void setInherited(final boolean truefalse)
	{
		this.isInherited=truefalse;
	}

	@Override
	public void setShowInSpecialReported(final boolean truefalse)
	{
		showInSpecialReport = truefalse;
	}

	@Override
	public void setShowInEditor(final boolean truefalse)
	{
		showInEditor = truefalse;
	}

	@Override
	public void setShowInFactionsCommand(final boolean truefalse)
	{
		showInFacCommand = truefalse;
	}

	@Override
	public void setChoices(final List<String> v)
	{
		choices = new SVector<String>(v);
	}

	@Override
	public void setAutoDefaults(final List<String> v)
	{
		autoDefaults = new SVector<String>(v);
	}

	@Override
	public void setDefaults(final List<String> v)
	{
		defaults = new SVector<String>(v);
	}

	@Override
	public void setRateModifier(final double d)
	{
		rateModifier = d;
	}

	@Override
	public boolean isPreLoaded()
	{
		return CMParms.parseSemicolons(CMProps.getVar(CMProps.Str.PREFACTIONS).toUpperCase(),true).contains(factionID().toUpperCase());
	}

	@Override
	public Faction.FAbilityUsage getAbilityUsage(final int x)
	{
		return ((x>=0)&&(x<abilityUsages.size()))
				?(Faction.FAbilityUsage)abilityUsages.get(x)
				:null;
	}

	@Override
	public boolean delFactor(final Faction.FZapFactor f)
	{
		if(!factors.remove(f))
			return false;
		factors.trimToSize();
		return true;
	}

	@Override
	public Faction.FZapFactor getFactor(final int x)
	{
		return ((x >= 0) && (x < factors.size())) ? factors.get(x) : null;
	}

	@Override
	public Faction.FZapFactor addFactor(final double gain, final double loss, final String mask)
	{
		final Faction.FZapFactor o=new DefaultFactionZapFactor(gain,loss,mask);
		factors.add(o);
		factors.trimToSize();
		return o;
	}

	@Override
	public boolean delRelation(final String factionID)
	{
		if(relations.remove(factionID)==null)
			return false;
		return true;
	}

	@Override
	public boolean addRelation(final String factionID, final double relation)
	{
		if(relations.containsKey(factionID))
			return false;
		relations.put(factionID,Double.valueOf(relation));
		return true;
	}

	@Override
	public double getRelation(final String factionID)
	{
		if(relations.containsKey(factionID))
			return relations.get(factionID).doubleValue();
		return 0.0;
	}

	public DefaultFaction()
	{
		super();
	}

	@Override
	public void initializeFaction(final String aname)
	{
		_factionID=aname;
		name=aname;
		upName=aname.toUpperCase();
		minimum=0;
		middle=50;
		maximum=100;
		highest=100;
		lowest=0;
		difference=CMath.abs(maximum-minimum);
		experienceFlag="EXTREME";
		addRange("0;100;Sample Range;SAMPLE;");
		defaults.add("0");
	}

	@Override
	public void initializeFaction(final StringBuffer file, final String fID)
	{
		final boolean debug = false;

		_factionID = fID;
		final CMProps facProps = new CMProps();
		for(final String line : Resources.getFileLineVector(file))
		{
			final String s=line.trim();
			if(s.startsWith("#")||(s.length()==0))
				continue;
			final int x=s.indexOf('=');
			if(x<0)
			{
				Log.errOut("Unknown line '"+s+"' in faction "+fID);
				continue;
			}
			final String key=s.substring(0,x);
			final String val=s.substring(x+1);
			facProps.put(key, val);
		}
		if(facProps.isEmpty())
			return;
		name=facProps.getStr("NAME");
		upName=name.toUpperCase();
		choiceIntro=facProps.getStr("CHOICEINTRO");
		minimum=facProps.getInt("MINIMUM");
		maximum=facProps.getInt("MAXIMUM");
		if(maximum<minimum)
		{
			minimum=maximum;
			maximum=facProps.getInt("MINIMUM");
		}
		recalc();
		experienceFlag=facProps.getStr("EXPERIENCE").toUpperCase().trim();
		if(experienceFlag.length()==0)
			experienceFlag="NONE";
		rateModifier=facProps.getDouble("RATEMODIFIER");
		showInScore=facProps.getBoolean("SCOREDISPLAY");
		showInFacCommand=facProps.getBoolean("SHOWINFACTIONSCMD");
		showInSpecialReport=facProps.getBoolean("SPECIALREPORTED");
		showInEditor=facProps.getBoolean("EDITALONE");
		final String s=facProps.getStr("INHERITED");
		if(s.trim().length()==0)
			isInherited=true;
		else
			isInherited=facProps.getBoolean("INHERITED");
		defaults=new SVector<String>(CMParms.parseSemicolons(facProps.getStr("DEFAULT"),true));
		autoDefaults =new SVector<String>(CMParms.parseSemicolons(facProps.getStr("AUTODEFAULTS"),true));
		choices =new SVector<String>(CMParms.parseSemicolons(facProps.getStr("AUTOCHOICES"),true));
		useLightReactions=facProps.getBoolean("USELIGHTREACTIONS");
		ranges=new SHashtable<String,FRange>();
		changes=new SHashtable<String,FactionChangeEvent[]>();
		factors=new SVector<FZapFactor>();
		relations=new SHashtable<String,Double>();
		abilityUsages=new SVector<FAbilityUsage>();
		abilityUseCache=new STreeMap<String,FAbilityUsage>();
		abilityUseMisses=new STreeSet<String>();
		reactions=new SVector<FReactionItem>();
		reactionHash=new SHashtable<String,CList<FReactionItem>>();
		for(final Enumeration<Object> e=facProps.keys();e.hasMoreElements();)
		{
			if(debug)
				Log.sysOut("FACTIONS","Starting Key Loop");
			final String key = (String) e.nextElement();
			if(debug)
				Log.sysOut("FACTIONS","  Key Found     :"+key);
			final String words = (String) facProps.get(key);
			if(debug)
				Log.sysOut("FACTIONS","  Words Found   :"+words);
			if(key.startsWith("RANGE"))
				addRange(words);
			if(key.startsWith("CHANGE"))
				createChangeEvent(key, words);
			if(key.startsWith("AFFBEHAV"))
			{
				final Object[] O=CMParms.parseSafeSemicolonList(words,false).toArray();
				if(O.length==3)
					addAffectBehav((String)O[0],(String)O[1],(String)O[2]);
			}
			if(key.startsWith("FACTOR"))
			{
				final List<String> factor=CMParms.parseSemicolons(words,false);
				if(factor.size()>2)
					factors.add(new DefaultFactionZapFactor(CMath.s_double(factor.get(0)),
											 CMath.s_double(factor.get(1)),
											 factor.get(2)));
			}
			if(key.startsWith("RELATION"))
			{
				final Vector<String> V=CMParms.parse(words);
				if(V.size()>=2)
				{
					final String who=V.elementAt(0);
					double factor;
					final String amt=V.elementAt(1).trim();
					if(amt.endsWith("%"))
						factor=CMath.s_pct(amt);
					else
						factor=1;
					relations.put(who,Double.valueOf(factor));
				}
			}
			if(key.startsWith("ABILITY"))
				addAbilityUsage(words);
			if(key.startsWith("REACTION"))
			{
				final DefaultFactionReactionItem item = new DefaultFactionReactionItem(words);
				addReaction(item.rangeCodeName(), item.presentMOBMask(), item.reactionObjectID(), item.parameters());
			}
		}
	}

	private void recalc()
	{
		minimum=Integer.MAX_VALUE;
		maximum=Integer.MIN_VALUE;
		int num=0;
		for(final Enumeration<Faction.FRange> e=ranges();e.hasMoreElements();)
		{
			final Faction.FRange FR=e.nextElement();
			if(FR.high()>maximum)
				maximum=FR.high();
			if(FR.low()<minimum)
				minimum=FR.low();
			num++;
		}
		if(minimum==Integer.MAX_VALUE)
			minimum=Integer.MIN_VALUE;
		if(maximum==Integer.MIN_VALUE)
			maximum=Integer.MAX_VALUE;
		if(maximum<minimum)
		{
			final int oldMin=minimum;
			minimum=maximum;
			maximum=oldMin;
		}
		middle=minimum+(int)Math.round(CMath.div(maximum-minimum,2.0));
		difference=CMath.abs(maximum-minimum);
		lastDataChange[0]=System.currentTimeMillis();
		rangeRangeMap=new PrioritizingLimitedMap<Integer,FRange>(num*5,60000,600000,100);
	}

	@Override
	public String getTagValue(final String tag)
	{
		final int tagRef=CMLib.factions().isFactionTag(tag);
		if(tagRef<0)
			return "";
		int numCall=-1;
		if((tagRef<TAG_NAMES.length)&&(TAG_NAMES[tagRef].endsWith("*")))
		{
			if(CMath.isInteger(tag.substring(TAG_NAMES[tagRef].length()-1)))
				numCall=CMath.s_int(tag.substring(TAG_NAMES[tagRef].length()-1));
		}
		switch(tagRef)
		{
		case TAG_NAME:
			return name;
		case TAG_MINIMUM:
			return "" + minimum;
		case TAG_MAXIMUM:
			return "" + maximum;
		case TAG_SCOREDISPLAY:
			return Boolean.toString(showInScore).toUpperCase();
		case TAG_SHOWINFACTIONSCMD:
			return Boolean.toString(showInFacCommand).toUpperCase();
		case TAG_SPECIALREPORTED:
			return Boolean.toString(showInSpecialReport).toUpperCase();
		case TAG_EDITALONE:
			return Boolean.toString(showInEditor).toUpperCase();
		case TAG_DEFAULT:
			return CMParms.toSemicolonListString(defaults);
		case TAG_AUTODEFAULTS:
			return CMParms.toSemicolonListString(autoDefaults);
		case TAG_CHOICEINTRO:
			return choiceIntro;
		case TAG_AUTOCHOICES:
			return CMParms.toSemicolonListString(choices);
		case TAG_RATEMODIFIER:
			return "" + rateModifier;
		case TAG_EXPERIENCE:
			return "" + experienceFlag;
		case TAG_INHERITABLE:
			return Boolean.toString(isInherited).toUpperCase();
		case TAG_RANGE_:
		{
			if((numCall<0)||(numCall>=ranges.size()))
				return ""+ranges.size();
			int x=0;
			for(final Enumeration<Faction.FRange> e=ranges();e.hasMoreElements();)
			{
				final Faction.FRange FR=e.nextElement();
				if(x==numCall)
					return FR.toString();
				x++;
			}
			return "";
		}
		case TAG_CHANGE_:
		{
			int sz=0;
			for(final Enumeration<Faction.FactionChangeEvent[]> es=changes.elements();es.hasMoreElements();)
				sz+=es.nextElement().length;
			if((numCall<0)||(numCall>=sz))
				return ""+sz;
			int i=0;
			for(final Enumeration<Faction.FactionChangeEvent[]> e=changes.elements();e.hasMoreElements();)
			{
				final Faction.FactionChangeEvent[] FCs=e.nextElement();
				for (final FactionChangeEvent fc : FCs)
				{
					if(i==numCall)
						return fc.toString();
					i++;
				}
			}
			return "";
		}
		case TAG_ABILITY_:
		{
			if((numCall<0)||(numCall>=abilityUsages.size()))
				return ""+abilityUsages.size();
			return abilityUsages.get(numCall).toString();
		}
		case TAG_FACTOR_:
		{
			if((numCall<0)||(numCall>=factors.size()))
				return ""+factors.size();
			return factors.get(numCall).toString();
		}
		case TAG_RELATION_:
		{
			if((numCall<0)||(numCall>=relations.size()))
				return ""+relations.size();
			int i=0;
			for(final Enumeration<String> e=relations.keys();e.hasMoreElements();)
			{
				final String factionName=e.nextElement();
				final Double D=relations.get(factionName);
				if(i==numCall)
					return factionName+" "+CMath.toPct(D.doubleValue());
				i++;
			}
			return "";
		}
		case TAG_AFFBEHAV_:
		{
			if((numCall<0)||(numCall>=affBehavs.size()))
				return ""+affBehavs.size();
			int i=0;
			for(final Enumeration<String> e=affBehavs.keys();e.hasMoreElements();)
			{
				final String ID=e.nextElement();
				final String[] data=affBehavs.get(ID);
				if(i==numCall)
					return ID+";"+CMParms.toSafeSemicolonListString(data);
				i++;
			}
			return "";
		}
		case TAG_REACTION_:
		{
			if((numCall<0)||(numCall>=reactions.size()))
				return ""+reactions.size();
			final Faction.FReactionItem item = reactions.get(numCall);
			return item.toString();
		}
		case TAG_USELIGHTREACTIONS:
			return "" + useLightReactions;
		}
		return "";
	}

	@Override
	public String getINIDef(final String tag, final String delimeter)
	{
		final int tagRef=CMLib.factions().isFactionTag(tag);
		if(tagRef<0)
			return "";
		final String rawTagName=TAG_NAMES[tagRef];
		if(TAG_NAMES[tagRef].endsWith("*"))
		{
			final int number=CMath.s_int(getTagValue(rawTagName));
			final StringBuffer str=new StringBuffer("");
			for(int i=0;i<number;i++)
			{
				final String value=getTagValue(rawTagName.substring(0,rawTagName.length()-1)+i);
				str.append(rawTagName.substring(0,rawTagName.length()-1)+(i+1)+"="+value+delimeter);
			}
			return str.toString();
		}
		return rawTagName+"="+getTagValue(tag)+delimeter;
	}

	@Override
	public void updateFactionData(final MOB mob, final FData data)
	{
		data.resetFactionData(this);
		final List<Ability> aV=new ArrayList<Ability>();
		final List<Behavior> bV=new ArrayList<Behavior>();
		String ID=null;
		String[] stuff=null;
		if(mob.isMonster())
		{
			for(final Enumeration<String> e=affectsBehavs();e.hasMoreElements();)
			{
				ID=e.nextElement();
				stuff=getAffectBehav(ID);
				if(CMLib.masking().maskCheck(stuff[1],mob,true))
				{
					final Behavior B=CMClass.getBehavior(ID);
					if(B!=null)
					{
						B.setParms(stuff[0]);
						bV.add(B);
					}
					else
					{
						final Ability A=CMClass.getAbility(ID);
						A.setMiscText(stuff[0]);
						A.setAffectedOne(mob);
						aV.add(A);
					}
				}
			}
		}
		data.addHandlers(aV,bV);
	}

	@Override
	public FData makeFactionData(final MOB mob)
	{
		final FData data=new DefaultFactionData(this);
		updateFactionData(mob,data);
		return data;
	}

	@Override
	public boolean delAffectBehav(final String ID)
	{
		final boolean b=affBehavs.remove(ID.toUpperCase().trim())!=null;
		if(b)
			lastDataChange[0]=System.currentTimeMillis();
		return b;
	}

	@Override
	public boolean addAffectBehav(final String ID, final String parms, final String gainMask)
	{
		if(affBehavs.containsKey(ID.toUpperCase().trim()))
			return false;
		if((CMClass.getBehavior(ID)==null)&&(CMClass.getAbility(ID)==null))
			return false;
		affBehavs.put(ID.toUpperCase().trim(),new String[]{parms,gainMask});
		lastDataChange[0]=System.currentTimeMillis();
		return true;
	}

	@Override
	public String[] getAffectBehav(final String ID)
	{
		if(affBehavs.containsKey(ID.toUpperCase().trim()))
			return CMParms.toStringArray(new XVector<String>(affBehavs.get(ID.toUpperCase().trim())));
		return null;
	}

	@Override
	public boolean delReaction(final Faction.FReactionItem item)
	{
		final CList<Faction.FReactionItem> V=reactionHash.get(item.rangeCodeName().toUpperCase().trim());
		if(V!=null)
			V.remove(item);
		final boolean res = reactions.remove(item);
		if(reactions.size()==0)
			reactionHash.clear();
		lastDataChange[0]=System.currentTimeMillis();
		return res;
	}

	@Override
	public boolean addReaction(final String range, final String mask, final String abilityID, final String parms)
	{
		CList<Faction.FReactionItem> V=reactionHash.get(range.toUpperCase().trim());
		final DefaultFactionReactionItem item = new DefaultFactionReactionItem();
		item.setRangeName(range);
		item.setPresentMOBMask(mask);
		item.setReactionObjectID(abilityID);
		item.setParameters(parms);
		if(V==null)
		{
			V=new SVector<Faction.FReactionItem>();
			reactionHash.put(range.toUpperCase().trim(), V);
		}
		V.add(item);
		reactions.add(item);
		lastDataChange[0]=System.currentTimeMillis();
		return true;
	}

	@Override
	public FactionChangeEvent[] getChangeEvents(final String key)
	{
		return changes.get(key);
	}

	@Override
	public List<Integer> findChoices(final MOB mob)
	{
		final List<Integer> mine=new Vector<Integer>();
		String s;
		for(final Enumeration<String> e=choices.elements();e.hasMoreElements();)
		{
			s=e.nextElement();
			if(CMath.isInteger(s))
				mine.add(Integer.valueOf(CMath.s_int(s)));
			else
			if(CMLib.masking().maskCheck(s, mob,false))
			{
				final List<String> V=CMParms.parse(s);
				for(int j=0;j<V.size();j++)
				{
					if(CMath.isInteger(V.get(j)))
						mine.add(Integer.valueOf(CMath.s_int(V.get(j))));
				}
			}
		}
		return mine;
	}

	protected Map<String,Integer> getCMMsgTypeMap()
	{
		@SuppressWarnings("unchecked")
		Map<String,Integer> msgTypeCache=(Map<String,Integer>)Resources.getResource("SYSTEM_FACTION_GLOBAL_CMMSG_CODES");
		if(msgTypeCache==null)
		{
			msgTypeCache=new HashMap<String,Integer>();
			for(int typ = 0; typ < CMMsg.TYPE_DESCS.length; typ++)
				msgTypeCache.put("MSG_"+CMMsg.TYPE_DESCS[typ],Integer.valueOf(typ));
			Resources.submitResource("SYSTEM_FACTION_GLOBAL_CMMSG_CODES", msgTypeCache);
		}
		return msgTypeCache;
	}

	@Override
	public FactionChangeEvent[] findMsgChangeEvents(final CMMsg msg)
	{
		if(msgChangeCache.size()==0)
		{
			final Map<String,Integer> msgTypeCache=getCMMsgTypeMap();
			final Map<Integer,List<FactionChangeEvent>> msgClassMap=new HashMap<Integer,List<FactionChangeEvent>>();
			for (final Enumeration<FactionChangeEvent[]> e=changes.elements();e.hasMoreElements();)
			{
				final FactionChangeEvent[] Cs=e.nextElement();
				for(final FactionChangeEvent C : Cs)
				{
					if(msgTypeCache.containsKey(C.eventID()))
					{
						final Integer code=msgTypeCache.get(C.eventID());
						if(!msgClassMap.containsKey(code))
							msgClassMap.put(code, new ArrayList<FactionChangeEvent>());
						msgClassMap.get(code).add(C);
					}
				}
			}
			for(final Integer code : msgClassMap.keySet())
			{
				final List<FactionChangeEvent> list=msgClassMap.get(code);
				msgChangeCache.put(code, list.toArray(new FactionChangeEvent[list.size()]));
			}
		}
		return msgChangeCache.get(Integer.valueOf(msg.sourceMinor()));
	}

	@Override
	public FactionChangeEvent[] findAbilityChangeEvents(final Ability key)
	{
		if(key==null)
			return null;
		if(abilChangeCache.size()==0)
		{
			if(CMClass.numPrototypes(CMObjectType.ABILITY)==0)
				return null;
			final Map<Integer,List<FactionChangeEvent>> abilityClassMap=new HashMap<Integer,List<FactionChangeEvent>>();
			for(int classificationCode = 0;classificationCode < Ability.ACODE_DESCS.length;classificationCode++)
			{
				for (final Enumeration<FactionChangeEvent[]> e=changes.elements();e.hasMoreElements();)
				{
					final FactionChangeEvent[] Cs=e.nextElement();
					for(final FactionChangeEvent C : Cs)
					{
						if(classificationCode==C.IDclassFilter())
						{
							if(!abilityClassMap.containsKey(Integer.valueOf(classificationCode)))
								abilityClassMap.put(Integer.valueOf(classificationCode), new ArrayList<FactionChangeEvent>());
							abilityClassMap.get(Integer.valueOf(classificationCode)).add(C);
						}
					}
				}
			}
			final Map<Integer,List<FactionChangeEvent>> abilityDomainMap=new HashMap<Integer,List<FactionChangeEvent>>();
			for(int domainCode = 0;domainCode < Ability.DOMAIN_DESCS.length;domainCode++)
			{
				final int domainID = domainCode << 5;
				for (final Enumeration<FactionChangeEvent[]> e=changes.elements();e.hasMoreElements();)
				{
					final FactionChangeEvent[] Cs=e.nextElement();
					for(final FactionChangeEvent C : Cs)
					{
						if(domainID==C.IDdomainFilter())
						{
							if(!abilityDomainMap.containsKey(Integer.valueOf(domainID)))
								abilityDomainMap.put(Integer.valueOf(domainID), new ArrayList<FactionChangeEvent>());
							abilityDomainMap.get(Integer.valueOf(domainID)).add(C);
						}
					}
				}
			}
			final Map<Long,List<FactionChangeEvent>> abilityFlagMap=new HashMap<Long,List<FactionChangeEvent>>();
			for(int flagIndex = 0;flagIndex < Ability.FLAG_DESCS.length;flagIndex++)
			{
				final long flagMask = Math.round(Math.pow(2, flagIndex));
				for (final Enumeration<FactionChangeEvent[]> e=changes.elements();e.hasMoreElements();)
				{
					final FactionChangeEvent[] Cs=e.nextElement();
					for(final FactionChangeEvent C : Cs)
					{
						if((C.IDflagFilter()>0)&&(CMath.bset(C.IDflagFilter(), flagMask)))
						{
							if(!abilityFlagMap.containsKey(Long.valueOf(flagMask)))
								abilityFlagMap.put(Long.valueOf(flagMask), new ArrayList<FactionChangeEvent>());
							abilityFlagMap.get(Long.valueOf(flagMask)).add(C);
						}
					}
				}
			}
			for(final Enumeration<Ability> a=CMClass.abilities();a.hasMoreElements();)
			{
				final Ability A=a.nextElement();
				final ArrayList<FactionChangeEvent> set=new ArrayList<FactionChangeEvent>(0);
				if(changes.containsKey(A.ID()))
					set.addAll(Arrays.asList(changes.get(A.ID())));
				if(abilityClassMap.containsKey(Integer.valueOf(A.classificationCode()&Ability.ALL_ACODES)))
					set.addAll(abilityClassMap.get(Integer.valueOf(A.classificationCode()&Ability.ALL_ACODES)));
				if(abilityDomainMap.containsKey(Integer.valueOf(A.classificationCode()&Ability.ALL_DOMAINS)))
					set.addAll(abilityDomainMap.get(Integer.valueOf(A.classificationCode()&Ability.ALL_DOMAINS)));
				if(A.flags()>0)
				{
					for(int flagIndex = 0;flagIndex < Ability.FLAG_DESCS.length;flagIndex++)
					{
						final long flagMask = Math.round(Math.pow(2, flagIndex));
						if(CMath.bset(A.flags(), flagMask))
						{
							if(abilityFlagMap.containsKey(Long.valueOf(flagMask)))
								set.addAll(abilityFlagMap.get(Long.valueOf(flagMask)));
						}
					}
				}
				if(set.size()>0)
					abilChangeCache.put(A.ID(), set.toArray(new FactionChangeEvent[0]));
			}
			if(abilChangeCache.size()==0)
				abilChangeCache.put("StdAbility", new FactionChangeEvent[0]);
		}
		return abilChangeCache.get(key.ID());
	}

	@Override
	public FactionChangeEvent[] findSocialChangeEvents(final Social soc)
	{
		if(soc==null)
			return null;
		if(socChangeCache.size()==0)
		{
			if(CMLib.socials().numSocialSets()==0)
				return null;
			for(final Enumeration<Social> s=CMLib.socials().getAllSocials();s.hasMoreElements();)
			{
				final Social S = s.nextElement();
				if(changes.containsKey(S.name()))
					socChangeCache.put(S.name(), changes.get(S.name()));
				else
				if(changes.containsKey(S.baseName()+" *"))
					socChangeCache.put(S.name(), changes.get(S.baseName()+" *"));
			}
			if(socChangeCache.size()==0)
				socChangeCache.put("DefaultSocial", new FactionChangeEvent[0]);
		}
		return socChangeCache.get(soc.name());
	}

	@Override
	public Faction.FRange fetchRange(final String codeName)
	{
		return ranges.get(codeName.toUpperCase().trim());
	}

	@Override
	public FRange fetchRange(final int faction)
	{
		final Integer I=Integer.valueOf(faction);
		FRange R=rangeRangeMap.get(I);
		if(R!=null)
			return R;
		for (final Enumeration<FRange> e=ranges.elements();e.hasMoreElements();)
		{
			R = e.nextElement();
			if ( (faction >= R.low()) && (faction <= R.high()))
			{
				rangeRangeMap.put(I, R);
				return R;
			}
		}
		return null;
	}

	@Override
	public String fetchRangeName(final int faction)
	{
		final FRange R= fetchRange(faction);
		if(R!=null)
			return R.name();
		return "";
	}

	@Override
	public int asPercent(final int faction)
	{
		return (int)Math.round(CMath.mul(CMath.div(faction-minimum,(maximum-minimum)),100));
	}

	@Override
	public int asPercentFromAvg(final int faction)
	{
		// =(( (B2+A2) / 2 ) - C2) / (B2-A2) * 100
		// C = current, A = min, B = Max
		return (int)Math.round(CMath.mul(CMath.div(((maximum+minimum)/2)-faction,maximum-minimum),100));
	}

	@Override
	public int randomFaction()
	{
		final Random gen = new Random();
		return maximum - gen.nextInt(maximum-minimum);
	}

	@Override
	public int findDefault(final MOB mob)
	{
		String s;
		for(final Enumeration<String> e=defaults.elements();e.hasMoreElements();)
		{
			s=e.nextElement();
			if(CMath.isNumber(s))
				return CMath.s_int(s);
			else
			if(CMLib.masking().maskCheck(s, mob,false))
			{
				final Vector<String> V=CMParms.parse(s);
				for(int j=0;j<V.size();j++)
				{
					if(CMath.isNumber(V.elementAt(j)))
						return CMath.s_int(V.elementAt(j));
				}
			}
		}
		return 0;
	}

	@Override
	public int findAutoDefault(final MOB mob)
	{
		String s;
		for(final Enumeration<String> e=autoDefaults.elements();e.hasMoreElements();)
		{
			s=e.nextElement();
			if(CMath.isNumber(s))
				return CMath.s_int(s);
			else
			if(CMLib.masking().maskCheck(s, mob,false))
			{
				final Vector<String> V=CMParms.parse(s);
				for(int j=0;j<V.size();j++)
				{
					if(CMath.isNumber(V.elementAt(j)))
						return CMath.s_int(V.elementAt(j));
				}
			}
		}
		return Integer.MAX_VALUE;
	}

	@Override
	public boolean hasFaction(final MOB mob)
	{
		return (mob.fetchFaction(_factionID)!=Integer.MAX_VALUE);
	}

	protected FAbilityUsage getAbilityUsage(final Ability A)
	{
		if(A==null)
			return null;
		if(abilityUseCache.containsKey(A.ID()))
			return abilityUseCache.get(A.ID());
		if(abilityUseMisses.contains(A.ID()))
			return null;

		for(final FAbilityUsage usage : abilityUsages)
		{
			if(usage.possibleAbilityID()
			&&(!usage.abilityFlags().equalsIgnoreCase(A.ID())))
				continue;

			if(usage.possibleAbilityID()
			||(((usage.type()<0)||((A.classificationCode()&Ability.ALL_ACODES)==usage.type()))
				&&((usage.flag()<0)||(CMath.bset(A.flags(),usage.flag())))
				&&((usage.notflag()<0)||(!CMath.bset(A.flags(),usage.notflag())))
				&&((usage.domain()<0)||((A.classificationCode()&Ability.ALL_DOMAINS)==usage.domain()))))
			{
				abilityUseCache.put(A.ID(), usage);
				return usage;
			}
		}
		abilityUseMisses.add(A.ID());
		return null;
	}

	@Override
	public boolean hasUsage(final Ability A)
	{
		return getAbilityUsage(A) != null;
	}

	@Override
	public boolean canUse(final MOB mob, final Ability A)
	{
		final FAbilityUsage usage = this.getAbilityUsage(A);
		if(usage == null)
			return true;
		final int faction=mob.fetchFaction(_factionID);
		if((faction < usage.low()) || (faction > usage.high()))
			return false;
		return true;
	}

	@Override
	public double findFactor(final MOB mob, final boolean gain)
	{
		for(final Faction.FZapFactor factor : factors)
		{
			if(CMLib.masking().maskCheck(factor.compiledMOBMask(),mob,false))
				return gain?factor.gainFactor():factor.lossFactor();
		}
		return 1.0;
	}

	private boolean checkApplyEventWait(final FactionChangeEvent event, final MOB mob)
	{
		final Faction.FData data = mob.fetchFactionData(factionID());
		if(data != null)
		{
			if(System.currentTimeMillis() < data.getNextChangeTimers(event))
				return false;
			final long newTime=event.getWaitBetweenMs();
			if(newTime > 0)
				data.setNextChangeTimers(event, System.currentTimeMillis()+newTime);
		}
		return true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		FactionChangeEvent[] events;
		switch(msg.sourceMinor())
		{
		case CMMsg.TYP_ENTER:
		{
			if((msg.source() == myHost)
			&&(msg.source().isPlayer())
			&&(msg.target() instanceof Room))
			{
				events=getChangeEvents(MiscTrigger.AREAEXPLORE.toString());
				if(events != null)
				{
					final Room R=(Room)msg.target();
					if(!msg.source().playerStats().hasVisited(R))
					{
						final Area A=R.getArea();
						for (final FactionChangeEvent event : events)
						{
							if(event.applies(msg.source(),msg.source()))
							{
								final int pct = CMath.s_int(event.getTriggerParm("PCT"));
								if(pct==0)
								{
									if(msg.source().playerStats().totalVisitedRooms(msg.source(), A)>0)
										continue;
								}
								else
								{
									if(msg.source().playerStats().percentVisited(msg.source(),A) < pct)
										continue;
								}
								if(this.checkApplyEventWait(event, msg.source()))
									executeChange(msg.source(),msg.source(),event);
							}
						}
					}
				}
			}
			break;
		}
		case CMMsg.TYP_DEATH:
			if((msg.source()==myHost)||(msg.tool()==myHost))
			{
				final MOB killedM=msg.source();
				if(msg.tool() instanceof MOB)
				{
					final MOB killingBlowM=(MOB)msg.tool();
					events=getChangeEvents(((msg.source()==myHost)?MiscTrigger.MURDER:MiscTrigger.KILL).toString());
					if(events!=null)
					{
						for (final FactionChangeEvent event : events)
						{
							if(event.applies(killingBlowM,killedM))
							{
								final CharClass combatCharClass=CMLib.combat().getCombatDominantClass(killingBlowM,killedM);
								final Set<MOB> combatBeneficiaries=CMLib.combat().getCombatBeneficiaries(killingBlowM,killedM,combatCharClass);
								for (final MOB mob : combatBeneficiaries)
								{
									if(checkApplyEventWait(event, mob))
										executeChange(mob,killedM,event);
								}
							}
						}
					}
					final Room R = msg.source().location();
					if((R!=null)&&(R.getArea()!=null))
					{
						events=getChangeEvents(MiscTrigger.AREAASS.toString());
						if((events!=null)
						&&(killedM!=null)
						&&(killedM.phyStats().level()==R.getArea().getAreaIStats()[Area.Stats.MAX_LEVEL.ordinal()])
						&&(killingBlowM!=killedM)
						&&(killedM.isMonster())
						&&(killedM.getStartRoom()!=null)
						&&(killedM.location()!=null)
						&&(killedM.getStartRoom().getArea()==killingBlowM.location().getArea()))
						{
							final Area A=R.getArea();
							for (final FactionChangeEvent event : events)
							{
								final CharClass combatCharClass=CMLib.combat().getCombatDominantClass(killingBlowM,killedM);
								final Set<MOB> combatBeneficiaries=CMLib.combat().getCombatBeneficiaries(killingBlowM,killedM,combatCharClass);
								final int pct = CMath.s_int(event.getTriggerParm("PCT"));
								for (final MOB mob : combatBeneficiaries)
								{
									if(mob.isPlayer())
									{
										if(event.applies(mob,killedM))
										{
											final double population = A.getAreaIStats()[Area.Stats.MAX_LEVEL_MOBS.ordinal()];
											final Faction.FData data = mob.fetchFactionData(factionID());
											final int count = data.getCounter(""+event)+1;
											data.setCounter(""+event, count);
											final int myPct = (int)Math.round(100.0*CMath.div(count, population));
											if(myPct < pct)
												continue;
											if(checkApplyEventWait(event, mob))
												executeChange(mob,killedM,event);
										}
									}
								}
							}
						}
						events=getChangeEvents(MiscTrigger.AREAKILL.toString());
						if((events!=null)
						&&(killedM!=null)
						&&(killingBlowM!=killedM)
						&&(killedM.isMonster())
						&&(killedM.getStartRoom()!=null)
						&&(killedM.location()!=null)
						&&(killedM.getStartRoom().getArea()==killedM.location().getArea()))
						{
							final Area A=R.getArea();
							for (final FactionChangeEvent event : events)
							{
								final int pct = CMath.s_int(event.getTriggerParm("PCT"));
								final CharClass combatCharClass=CMLib.combat().getCombatDominantClass(killingBlowM,killedM);
								final Set<MOB> combatBeneficiaries=CMLib.combat().getCombatBeneficiaries(killingBlowM,killedM,combatCharClass);
								for (final MOB mob : combatBeneficiaries)
								{
									if(mob.isPlayer())
									{
										if(event.applies(mob,killedM))
										{
											final Faction.FData data = mob.fetchFactionData(factionID());
											final double population = A.getAreaIStats()[Area.Stats.POPULATION.ordinal()];
											final int count = data.getCounter(""+event)+1;
											data.setCounter(""+event, count);
											final int myPct = (int)Math.round(100.0*CMath.div(count, population));
											if(myPct < pct)
												continue;
											if(checkApplyEventWait(event, mob))
												executeChange(mob,killedM,event);
										}
									}
								}
							}
						}
					}
				}
				if(msg.source()==myHost)
				{
					events=getChangeEvents(MiscTrigger.DYING.toString());
					if(events!=null)
					{
						for (final FactionChangeEvent event : events)
						{
							if(event.applies(killedM,killedM))
							{
								if(checkApplyEventWait(event, killedM))
									executeChange(killedM,killedM,event);
							}
						}
					}
				}
			}
			break;
		case CMMsg.TYP_CAUSESINK:
			if((myHost instanceof MOB)
			&&(msg.target() instanceof Boardable))
			{
				final MOB killerM=msg.source();
				final Area shipArea=((Boardable)msg.target()).getArea();
				if(CMLib.map().areaLocation(killerM)==shipArea)
					events=getChangeEvents(MiscTrigger.SUNK.toString());
				else
				if(killerM == myHost)
					events=getChangeEvents(MiscTrigger.SINK.toString());
				else
					events=null;
				if(events!=null)
				{
					final MOB targetM=(killerM==myHost)?null:(MOB)myHost;
					for (final FactionChangeEvent event : events)
					{
						if(event.applies(killerM, targetM))
							executeChange(killerM,targetM,event);
					}
				}
			}
			break;
		case CMMsg.TYP_GIVE:
			if((msg.source()==myHost)
			&&(msg.tool() instanceof Coins)
			&&(msg.target() instanceof MOB)
			&&(!(msg.target() instanceof ShopKeeper))
			)
			{
				events=getChangeEvents(MiscTrigger.BRIBE.toString());
				if(events!=null)
				{
					for (final FactionChangeEvent event : events)
					{
						if(event.applies(msg.source(),(MOB)msg.target()))
						{
							double amount=CMath.s_double(event.getTriggerParm("AMOUNT"));
							final double pctAmount = CMath.s_pct(event.getTriggerParm("PCT"))
											 * CMLib.beanCounter().getTotalAbsoluteNativeValue((MOB)msg.target());
							if(pctAmount>amount)
								amount=pctAmount;
							if(amount==0)
								amount=1.0;
							if(((Coins)msg.tool()).getTotalValue()>=amount)
								executeChange(msg.source(),(MOB)msg.target(),event);
						}
					}
				}
			}
			break;
		case CMMsg.TYP_TEACH:
		case CMMsg.TYP_CHANNEL:
			// don't do the defaults!
			break;
		case CMMsg.TYP_SPEAK:
			if((msg.othersMessage()!=null)
			&&(msg.source()==myHost))
			{
				events=getChangeEvents(MiscTrigger.TALK.toString());
				if((events!=null)&&(events.length>0))
				{
					final Room R=msg.source().location();
					final List<MOB> targets=new ArrayList<MOB>();
					if(msg.target() instanceof MOB)
						targets.add((MOB)msg.target());
					else
					for(int m=0;m<R.numInhabitants();m++)
					{
						final MOB M=R.fetchInhabitant(m);
						if((M!=null)&&(M.isMonster())
						&&(CMLib.flags().canBeHeardSpeakingBy(msg.source(),M))
						&&(M.amFollowing()!=msg.source()))
							targets.add(M);
					}
					final String sayMsg=CMStrings.getSayFromMessage(msg.othersMessage().toLowerCase());
					Matcher M=null;
					if((sayMsg!=null)&&(sayMsg.length()>0)&&(R!=null))
					{
						final MOB mob=msg.source();
						final Faction.FData data = mob.fetchFactionData(factionID());
						for (final FactionChangeEvent event : events)
						{
							Pattern P=(Pattern)event.stateVariable(0);
							if(P==null)
							{
								String mask=event.getTriggerParm("REGEX");
								if((mask==null)||(mask.trim().length()==0))
									mask=".*";
								P=Pattern.compile(mask.toLowerCase());
								event.setStateVariable(0,P);
							}
							M=P.matcher(sayMsg);
							if(M.matches())
							{
								if((data != null) && (System.currentTimeMillis() < data.getNextChangeTimers(event)))
									continue;
								boolean foundOne=false;
								for(final MOB target : targets)
								{
									if(event.applies(mob,target))
									{
										executeChange(mob,target,event);
										foundOne=true;
										break;
									}
								}
								if(foundOne)
									checkApplyEventWait(event, mob);
							}
						}
					}
				}
				events=getChangeEvents(MiscTrigger.MUDCHAT.toString());
				if((events!=null)&&(events.length>0))
				{
					final Room R=msg.source().location();
					final List<MOB> targets=new ArrayList<MOB>();
					if(msg.target() instanceof MOB)
						targets.add((MOB)msg.target());
					else
					for(int m=0;m<R.numInhabitants();m++)
					{
						final MOB M=R.fetchInhabitant(m);
						if((M!=null)&&(M.isMonster())
						&&(CMLib.flags().canBeHeardSpeakingBy(msg.source(),M))
						&&(M.amFollowing()!=msg.source()))
							targets.add(M);
					}
					boolean foundOne=false;
					for(final MOB target : targets)
					{
						ChattyBehavior mudChatB=null;
						Behavior B=null;
						for(final Enumeration<Behavior> e=target.behaviors();e.hasMoreElements();)
						{
							B=e.nextElement();
							if(B instanceof ChattyBehavior)
								mudChatB=(ChattyBehavior)B;
						}
						if(mudChatB!=null)
						{
							final String sayMsg=CMStrings.getSayFromMessage(msg.othersMessage().toLowerCase());
							if((sayMsg!=null)&&(sayMsg.length()>0))
							{
								final MOB mob=msg.source();
								for (final FactionChangeEvent event : events)
								{
									if(event.applies(mob,target))
									{
										if((mudChatB.getLastRespondedTo()==mob)
										&&(mudChatB.getLastThingSaid()!=null)
										&&(!mudChatB.getLastThingSaid().equalsIgnoreCase(sayMsg)))
										{
											if(checkApplyEventWait(event, mob))
											{
												executeChange(mob,target,event);
												foundOne=true;
											}
										}
									}
								}
							}
							if(foundOne)
								break;
						}
					}
				}
			}
			break;
		default:
			if((events=findMsgChangeEvents(msg))!=null)
			{
				final EnglishParsing elib=CMLib.english();
				for (final FactionChangeEvent C : events)
				{
					final MOB target;
					if((msg.target() instanceof MOB)
					&&(C.applies(msg.source(),(MOB)msg.target())))
						target=(MOB)msg.target();
					else
					if((!(msg.target() instanceof MOB))
					&&(C.applies(msg.source(), null)))
						target=null;
					else
						continue;
					String s;
					s=C.getTriggerParm("SMSG");
					if((s.length()>0)&&((msg.sourceMessage()==null)||(!elib.containsString(msg.sourceMessage(), s))))
						continue;
					s=C.getTriggerParm("TMSG");
					if((s.length()>0)&&((msg.targetMessage()==null)||(!elib.containsString(msg.targetMessage(), s))))
						continue;
					s=C.getTriggerParm("OMSG");
					if((s.length()>0)&&((msg.othersMessage()==null)||(!elib.containsString(msg.othersMessage(), s))))
						continue;
					s=C.getTriggerParm("SRC");
					if(s.length()>0)
					{
						if(s.equalsIgnoreCase("me") && (msg.source()!=myHost))
							continue;
						if(!elib.containsString(msg.source().Name(), s))
							continue;
					}
					s=C.getTriggerParm("TGT");
					if(s.length()>0)
					{
						if(s.equalsIgnoreCase("me") && (msg.target()!=myHost))
							continue;
						if((msg.target()==null)||(!elib.containsString(msg.target().Name(), s)))
							continue;
					}
					s=C.getTriggerParm("OTH");
					if(s.length()>0)
					{
						if(s.equalsIgnoreCase("me") && (msg.tool()!=myHost))
							continue;
						if((msg.tool()==null)||(!elib.containsString(msg.tool().Name(), s)))
							continue;
					}
					if(checkApplyEventWait(C, msg.source()))
						executeChange(msg.source(),target,C);
				}
			}
			if(msg.tool() instanceof Ability)
			{
				if((msg.target()==myHost)	// Arrested watching
				&&(msg.source().isMonster())
				&&(msg.tool().ID().equals("Skill_HandCuff"))
				&&(msg.sourceMinor()!=CMMsg.TYP_TEACH))
				{
					final Room R=msg.source().location();
					if((R!=null)&&(R.getArea()!=null))
					{
						events=getChangeEvents(MiscTrigger.ARRESTED.toString());
						if(events!=null)
						{
							final LegalBehavior B=CMLib.law().getLegalBehavior(R);
							if((B!=null)&&(B.isAnyOfficer(R.getArea(), msg.source())))
							{
								for (final FactionChangeEvent event : events)
								{
									// reversed because the target is the one getting factioned
									if(event.applies(msg.source(),(MOB)msg.target()))
										executeChange((MOB)msg.target(),msg.source(),event);
								}
							}
						}
					}
				}
				if((msg.othersMessage()!=null)
				&&(msg.source()==myHost)
				&&((events=findAbilityChangeEvents((Ability)msg.tool()))!=null))
				{
					for (final FactionChangeEvent C : events)
					{
						final MOB target;
						if((msg.target() instanceof MOB)&&(C.applies(msg.source(),(MOB)msg.target())))
							target=(MOB)msg.target();
						else
						if (!(msg.target() instanceof MOB))
							target=null;
						else
							continue;
						if(checkApplyEventWait(C, msg.source()))
							executeChange(msg.source(),target,C);
					}
				}
			}
			else
			if((msg.tool() instanceof Social)		// socials
			&&(msg.source()==myHost)
			&&(msg.target() instanceof MOB)
			&&(((Social)msg.tool()).targetable(msg.source()))
			&&(msg.sourceMinor()!=CMMsg.TYP_CHANNEL))
			{
				events=getChangeEvents(MiscTrigger.SOCIAL.toString());
				if(events == null)
					events=findSocialChangeEvents((Social)msg.tool());
				if(events!=null)
				{
					final Social social = (Social)msg.tool();
					final String socialName = social.baseName();
					final MOB mob=msg.source();
					for (final FactionChangeEvent event : events)
					{
						if(event.miscEvent()==MiscTrigger.SOCIAL)
						{
							final String triggerID=event.getTriggerParm("ID");
							if(triggerID.length()==0)
								continue;
							if(!socialName.equals(triggerID)
							&&(!triggerID.equalsIgnoreCase("ALL")))
								continue;
						}
						if((event.applies(mob,(MOB)msg.target())))
						{
							if(checkApplyEventWait(event, mob))
								executeChange(mob,(MOB)msg.target(),event);
						}
					}
				}
			}
			break;
		}
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(((msg.sourceMinor()==CMMsg.TYP_EXPCHANGE)  // Experience is being altered
			||(msg.sourceMinor()==CMMsg.TYP_RPXPCHANGE))
		&&(msg.target() instanceof MOB) 		  // because a mob died
		&&(myHost==msg.source())	  // this Faction is on the mob that killed them
		&&(!experienceFlag.equals("NONE"))
		&&(msg.value()>0))
		{
			final MOB killer=msg.source();
			final MOB vic=(MOB)msg.target();

			if(experienceFlag.equals("HIGHER"))
				msg.setValue( (int)Math.round(((msg.value())*.75) +( ((msg.value())*.25) * CMath.div(Math.abs(killer.fetchFaction(_factionID)-minimum),(maximum - minimum)))));
			else
			if(experienceFlag.equals("LOWER"))
				msg.setValue( (int)Math.round(((msg.value())*.75) +( ((msg.value())*.25) * CMath.div(Math.abs(maximum-killer.fetchFaction(_factionID)),(maximum - minimum)))));
			else
			if(vic.fetchFaction(_factionID)!=Integer.MAX_VALUE)
			{
				if(experienceFlag.equals("EXTREME"))
					msg.setValue( (int)Math.round(((msg.value())*.75) +( ((msg.value())*.25) * CMath.div(Math.abs(vic.fetchFaction(_factionID) - killer.fetchFaction(_factionID)),(maximum - minimum)))));
				else
				if(experienceFlag.equals("FOLLOWHIGHER"))
					msg.setValue( (int)Math.round(((msg.value())*.75) +( ((msg.value())*.25) * CMath.div(Math.abs(vic.fetchFaction(_factionID)-minimum),(maximum - minimum)))));
				else
				if(experienceFlag.equals("FOLLOWLOWER"))
					msg.setValue( (int)Math.round(((msg.value())*.75) +( ((msg.value())*.25) * CMath.div(Math.abs(maximum-vic.fetchFaction(_factionID)),(maximum - minimum)))));
				if(msg.value()<=0)
					msg.setValue(0);
			}
		}
		return true;
	}

	@Override
	public void executeChange(final MOB source, MOB target, final FactionChangeEvent event)
	{
		final int sourceFaction= source.fetchFaction(_factionID);
		int targetFaction = sourceFaction * -1;
		if((source==target)
		&&(!event.selfTargetOK())
		&&(event.miscEvent() != MiscTrigger.TIME))
			return;

		if(target!=null)
		{
			if(hasFaction(target))
				targetFaction=target.fetchFaction(_factionID);
			else
			if(!event.outsiderTargetOK())
				return;
		}
		else
			target = source;

		if((event.getPctChance()<100)
		&&(event.getPctChance()>0)
		&& (CMLib.dice().rollPercentage()<event.getPctChance()))
			return;

		double baseChangeAmount=100.0;
		if((source!=target)
		&&(!event.just100()))
		{
			final int levelLimit=CMProps.getIntVar(CMProps.Int.EXPRATE);
			final int levelDiff=target.phyStats().level()-source.phyStats().level();

			if(levelDiff<(-levelLimit) )
				baseChangeAmount=0.0;
			else
			if(levelLimit>0)
			{
				double levelFactor=CMath.div(levelDiff,levelLimit);
				if(levelFactor> (levelLimit))
					levelFactor=(levelLimit);
				baseChangeAmount=baseChangeAmount+CMath.mul(levelFactor,100);
			}
		}
		if(event.getBonusXP()!=0)
			CMLib.leveler().postExperience(source, target, "", event.getBonusXP(), false);
		if(event.getBonusRoleplayXP()!=0)
			CMLib.leveler().postRPExperience(source, target, "", event.getBonusRoleplayXP(), true);

		final String resetTimerEventID = event.getFlagValue("RESTIME");
		if(resetTimerEventID.length()>0)
		{
			for(String resEventID : CMParms.parseCommas(resetTimerEventID, true))
			{
				final Integer which;
				final int x=resEventID.indexOf('.');
				if(x>0)
				{
					which=Integer.valueOf(CMath.s_int(resEventID.substring(x+1)));
					resEventID=resEventID.substring(0,x);
				}
				else
					which=null;
				final boolean reset;
				Faction.FactionChangeEvent[] events;
				if(resetTimerEventID.startsWith("-"))
				{
					reset=false;
					events = getChangeEvents(resEventID.substring(1));
				}
				else
				{
					events = getChangeEvents(resEventID);
					reset=true;
				}
				if(events != null)
				{
					if((which != null)
					&&(events.length>=which.intValue())
					&&(which.intValue()>0))
						events=new Faction.FactionChangeEvent[] { events[which.intValue()-1] };
					final Faction.FData sdata = source.fetchFactionData(factionID());
					final Faction.FData tdata = target.fetchFactionData(factionID());
					for(final Faction.FactionChangeEvent event2 : events)
					{
						if(sdata != null)
						{
							if(reset)
								sdata.resetEventTimers(event2.eventID());
							else
								sdata.setNextChangeTimers(event2, Long.MAX_VALUE);
						}
						if(tdata != null)
						{
							if(reset)
								tdata.resetEventTimers(event2.eventID());
							else
								tdata.setNextChangeTimers(event2, Long.MAX_VALUE);
						}
					}
				}
			}
		}

		int factionAdj=1;
		int changeDir=0;
		switch(event.direction())
		{
		case FactionChangeEvent.CHANGE_DIRECTION_MAXIMUM:
			factionAdj=maximum-sourceFaction;
			break;
		case FactionChangeEvent.CHANGE_DIRECTION_MINIMUM:
			factionAdj=minimum-sourceFaction;
			break;
		case FactionChangeEvent.CHANGE_DIRECTION_UP:
			changeDir=1;
			break;
		case FactionChangeEvent.CHANGE_DIRECTION_DOWN:
			changeDir=-1;
			break;
		case FactionChangeEvent.CHANGE_DIRECTION_OPPOSITE:
			if(source!=target)
			{
				if(targetFaction==middle)
					changeDir=(sourceFaction>middle)?1:-1;
				else
					changeDir=(targetFaction>middle)?-1:1;
				if((sourceFaction>middle)&&(targetFaction>middle))
					changeDir=-1;
				baseChangeAmount=CMath.div(baseChangeAmount,2.0)
								+(int)Math.round(CMath.div(baseChangeAmount,2.0)
										*Math.abs((sourceFaction-targetFaction)
												/Math.abs((double)difference)));
			}
			else
				factionAdj=0;
			break;
		case FactionChangeEvent.CHANGE_DIRECTION_AWAY:
			if(source!=target)
				changeDir=targetFaction>=sourceFaction?-1:1;
			else
				factionAdj=0;
			break;
		case FactionChangeEvent.CHANGE_DIRECTION_TOWARD:
			if(source!=target)
				changeDir=targetFaction>=sourceFaction?1:-1;
			else
				factionAdj=0;
			break;
		case FactionChangeEvent.CHANGE_DIRECTION_REMOVE:
			factionAdj=Integer.MAX_VALUE;
			break;
		case FactionChangeEvent.CHANGE_DIRECTION_ADD:
			factionAdj=findDefault(source);
			if(!hasFaction(source))
				source.addFaction(_factionID,0);
			else
				factionAdj=0;
			break;
		}
		if(changeDir!=0)
		{
			//int baseExp=(int)Math.round(theAmount);

			// Pardon the completely random seeming 1.42 and 150.
			// They're the result of making graphs of scenarios and massaging the formula, nothing more or less.
			if((hasFaction(target))||(event.outsiderTargetOK()))
				factionAdj=changeDir*(int)Math.round(rateModifier*baseChangeAmount);
			else
				factionAdj=0;
			factionAdj*=event.factor();
			factionAdj=(int)Math.round(CMath.mul(factionAdj,findFactor(source,(factionAdj>=0))));
		}

		if((factionAdj==0)&&(event.factor()!=0.0))
			return;

		final String announceMsg = event.getFlagValue("ANNOUNCE");
		final String seenMsg = announceMsg.length()>0 ? announceMsg : null;
		if(seenMsg != null)
		{
			final List<String> channels=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.FACTIONANNOUNCEMENTS, source);
			if(channels.size()>0)
			{
				final String msgStr = source.Name()+" ("+name()+"): "+seenMsg;
				for(int i=0;i<channels.size();i++)
					CMLib.commands().postChannel(channels.get(i),source.clans(),msgStr,true);
			}
		}
		CMMsg facMsg=CMClass.getMsg(source,target,null,CMMsg.MASK_ALWAYS|CMMsg.TYP_FACTIONCHANGE,seenMsg,CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,_factionID);
		facMsg.setValue(factionAdj);
		final Room R=source.location();
		if(R!=null)
		{
			if(R.okMessage(source,facMsg))
			{
				R.send(source, facMsg);
				factionAdj=facMsg.value();
				if((factionAdj!=Integer.MAX_VALUE)&&(factionAdj!=Integer.MIN_VALUE))
				{
					// Now execute the changes on the relation.  We do this AFTER the execution of the first so
					// that any changes from okMessage are incorporated
					if(relations.size()>0)
					{
						for(final Enumeration<String> e=relations.keys();e.hasMoreElements();)
						{
							final String relID=(e.nextElement());
							facMsg=CMClass.getMsg(source,target,null,CMMsg.MASK_ALWAYS|CMMsg.TYP_FACTIONCHANGE,null,CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,relID);
							facMsg.setValue((int)Math.round(CMath.mul(factionAdj, relations.get(relID).doubleValue())));
							if(R.okMessage(source,facMsg))
								R.send(source, facMsg);
						}
					}
				}
			}
		}
		else
		if((factionAdj==Integer.MAX_VALUE)||(factionAdj==Integer.MIN_VALUE))
			source.removeFaction(_factionID);
		else
			source.adjustFaction(_factionID,factionAdj);
	}

	@Override
	public String usageFactorRangeDescription(final Ability A)
	{
		final StringBuffer rangeStr=new StringBuffer();
		final HashSet<String> namesAdded=new HashSet<String>();
		final FAbilityUsage usage = getAbilityUsage(A);
		if(usage != null)
		{
			for(final Enumeration<FRange> e=ranges();e.hasMoreElements();)
			{
				final FRange R=e.nextElement();
				if((((R.high()<=usage.high())&&(R.high()>=usage.low()))
					||((R.low()>=usage.low()))&&(R.low()<=usage.high()))
				&&(!namesAdded.contains(R.name())))
				{
					namesAdded.add(R.name());
					if(rangeStr.length()>0)
						rangeStr.append(", ");
					rangeStr.append(R.name());
				}
			}
		}
		return rangeStr.toString();
	}

	private static String _ALL_TYPES=null;

	@Override
	public String ALL_CHANGE_EVENT_TYPES()
	{
		final StringBuffer ALL_TYPES=new StringBuffer("");
		if(_ALL_TYPES!=null)
			return _ALL_TYPES;
		for (final MiscTrigger element : Faction.FactionChangeEvent.MiscTrigger.values())
			ALL_TYPES.append(element.name()+", ");
		for (final String element : Ability.ACODE_DESCS)
			ALL_TYPES.append(element+", ");
		for (final String element : Ability.DOMAIN_DESCS)
			ALL_TYPES.append(element+", ");
		for (final String element : Ability.FLAG_DESCS)
			ALL_TYPES.append(element+", ");
		final Set<String> doneSoc=new HashSet<String>();
		for(final Enumeration<Social> s = CMLib.socials().getAllSocials();s.hasMoreElements();)
		{
			final Social S=s.nextElement();
			ALL_TYPES.append(S.name()).append(", ");
			if(!doneSoc.contains(S.baseName()))
			{
				doneSoc.add(S.baseName());
				ALL_TYPES.append(S.baseName()+" *, ");
			}
		}
		_ALL_TYPES=ALL_TYPES.toString()+" a valid Skill, Spell, Chant, etc. ID.";
		return _ALL_TYPES;
	}

	@Override
	public Faction.FactionChangeEvent createChangeEvent(final String eventKey, final String eventData)
	{
		Faction.FactionChangeEvent event;
		if(eventData==null)
			return null;
		if(eventData.indexOf(';')<0)
		{
			event=new DefaultFaction.DefaultFactionChangeEvent(this);
			if(!event.setEventID(eventData))
				return null;
		}
		else
			event=new DefaultFaction.DefaultFactionChangeEvent(this,eventKey,eventData);
		abilChangeCache.clear();
		socChangeCache.clear();
		msgChangeCache.clear();
		Faction.FactionChangeEvent[] events=changes.get(event.eventID().toUpperCase().trim());
		if(events==null)
			events=new Faction.FactionChangeEvent[0];
		events=Arrays.copyOf(events, events.length+1);
		events[events.length-1]=event;
		changes.put(event.eventID().toUpperCase().trim(), events);
		return event;
	}

	private boolean replaceEvents(final String key, final Faction.FactionChangeEvent event, final boolean strict)
	{
		final Faction.FactionChangeEvent[] events=changes.get(key);
		if(events==null)
			return false;
		final Faction.FactionChangeEvent[] nevents=new Faction.FactionChangeEvent[events.length-1];
		int ne1=0;
		boolean done=false;
		for (final FactionChangeEvent event2 : events)
		{
			if((strict&&(event2 == event))||((!strict)&&(event2.toString().equals(event.toString()))))
			{
				if(nevents.length==0)
					changes.remove(key);
				else
					changes.put(key,nevents);
				done=true;
			}
			else
			if(ne1<nevents.length)
				nevents[ne1++]=event2;
		}
		if(done)
		{
			abilChangeCache.clear();
			msgChangeCache.clear();
			socChangeCache.clear();
		}
		return done;
	}

	@Override
	public void clearChangeEvents()
	{
		abilChangeCache.clear();
		socChangeCache.clear();
		changes.clear();
		msgChangeCache.clear();
	}

	@Override
	public boolean delChangeEvent(final Faction.FactionChangeEvent event)
	{
		for(final Enumeration<String> e=changes.keys();e.hasMoreElements();)
		{
			if(replaceEvents(e.nextElement(),event,true))
			{
				abilChangeCache.clear();
				socChangeCache.clear();
				msgChangeCache.clear();
				msgChangeCache.clear();
				return true;
			}
		}
		for(final Enumeration<String> e=changes.keys();e.hasMoreElements();)
		{
			if(replaceEvents(e.nextElement(),event,false))
			{
				abilChangeCache.clear();
				socChangeCache.clear();
				msgChangeCache.clear();
				msgChangeCache.clear();
				return true;
			}
		}
		return false;
	}

	public class DefaultFactionChangeEvent implements Faction.FactionChangeEvent
	{
		private String		eventTriggerID	= "";
		private MiscTrigger miscEventTrigger= null;
		private String		flagCache		= "";
		private int			IDclassFilter	= -1;
		private long		IDflagFilter	= -1;
		private int			IDdomainFilter	= -1;
		private int			direction		= 0;
		private double		factor			= 0.0;
		private String		targetZapperStr	= "";
		private boolean		outsiderTargetOK= false;
		private boolean		selfTargetOK	= false;
		private boolean		just100			= false;
		private int			bonusXP			= 0;
		private int			bonusRPXP		= 0;
		private Object[]	stateVariables	= new Object[0];
		private String		triggerParms	= "";
		private int			withinTicks		= -1;
		private long		waitMs			= -1;
		private int			pctChance		= 100;

		private final Faction	myFaction;

		private Map<String, String>				flags					= new Hashtable<String, String>();
		private Map<String, String>				savedTriggerParms		= new Hashtable<String, String>();
		private MaskingLibrary.CompiledZMask	compiledTargetZapper	= null;
		private MaskingLibrary.CompiledZMask	compiledSourceZapper	= null;

		@Override
		public String eventID()
		{
			return eventTriggerID;
		}

		@Override
		public MiscTrigger miscEvent()
		{
			return miscEventTrigger;
		}

		@Override
		public String flagCache()
		{
			return flagCache;
		}

		@Override
		public int IDclassFilter()
		{
			return IDclassFilter;
		}

		@Override
		public long IDflagFilter()
		{
			return IDflagFilter;
		}

		@Override
		public int IDdomainFilter()
		{
			return IDdomainFilter;
		}

		@Override
		public int direction()
		{
			return direction;
		}

		@Override
		public double factor()
		{
			return factor;
		}

		@Override
		public int getBonusXP()
		{
			return bonusXP;
		}

		@Override
		public int getWithinTicks()
		{
			return withinTicks;
		}

		@Override
		public long getWaitBetweenMs()
		{
			return this.waitMs;
		}

		@Override
		public int getPctChance()
		{
			return pctChance;
		}

		@Override
		public int getBonusRoleplayXP()
		{
			return bonusRPXP;
		}

		@Override
		public String targetZapper()
		{
			return targetZapperStr;
		}

		@Override
		public boolean outsiderTargetOK()
		{
			return outsiderTargetOK;
		}

		@Override
		public boolean selfTargetOK()
		{
			return selfTargetOK;
		}

		@Override
		public boolean just100()
		{
			return just100;
		}

		@Override
		public void setDirection(final int newVal)
		{
			direction = newVal;
		}

		@Override
		public void setFactor(final double newVal)
		{
			factor = newVal;
		}

		@Override
		public void setTargetZapper(final String newVal)
		{
			targetZapperStr = newVal;
		}

		@Override
		public MaskingLibrary.CompiledZMask compiledTargetZapper()
		{
			if(compiledTargetZapper == null)
			{
				if(targetZapperStr.trim().length()>0)
					compiledTargetZapper=CMLib.masking().maskCompile(targetZapperStr);
			}
			return compiledTargetZapper;
		}

		@Override
		public MaskingLibrary.CompiledZMask compiledSourceZapper()
		{
			if(compiledSourceZapper == null)
			{
				final String sourceZapperStr=savedTriggerParms.get("MASK");
				if((sourceZapperStr!=null)&&(sourceZapperStr.length()>0))
					compiledSourceZapper=CMLib.masking().maskCompile(sourceZapperStr);
			}
			return compiledSourceZapper;
		}

		@Override
		public String getTriggerParm(final String parmName)
		{
			if((triggerParms==null)||(triggerParms.length()==0))
				return "";
			final String S=savedTriggerParms.get(parmName);
			if(S!=null)
				return S;
			return "";
		}

		@Override
		public String toString()
		{
			if(triggerParms.trim().length()>0)
				return eventTriggerID+"("+triggerParms.replace(';',',')+");"+CHANGE_DIRECTION_DESCS[direction]+";"+((int)Math.round(factor*100.0))+"%;"+flagCache+";"+targetZapperStr;
			else
				return eventTriggerID+";"+CHANGE_DIRECTION_DESCS[direction]+";"+((int)Math.round(factor*100.0))+"%;"+flagCache+";"+targetZapperStr;
		}

		public DefaultFactionChangeEvent(final Faction F)
		{
			myFaction = F;
		}

		public DefaultFactionChangeEvent(final Faction F, final String eventKey, final String eventData)
		{
			myFaction=F;
			final List<String> v = CMParms.parseSemicolons(eventData,false);

			String trigger =v.get(0);
			triggerParms="";
			final int x=trigger.indexOf('(');
			if((x>0)&&(trigger.endsWith(")")))
			{
				setTriggerParameters(trigger.substring(x+1,trigger.length()-1));
				trigger=trigger.substring(0,x);
			}

			setEventID(trigger);
			setDirection(v.get(1));
			final String amt=v.get(2).trim();
			if(amt.endsWith("%"))
				setFactor(CMath.s_pct(amt));
			else
				setFactor(1.0);

			if(v.size()>3)
				setFlags(v.get(3));
			if(v.size()>4)
				setTargetZapper(v.get(4));
		}

		@Override
		public boolean setEventID(final String newID)
		{
			IDclassFilter=-1;
			IDflagFilter=-1;
			IDdomainFilter=-1;
			miscEventTrigger = null;
			for (final MiscTrigger element : MiscTrigger.values())
			{
				if(element.name().equalsIgnoreCase(newID))
				{
					eventTriggerID = element.name();
					miscEventTrigger = element;
					return true;
				}
			}
			for(int i=0;i<Ability.ACODE_DESCS.length;i++)
			{
				if(Ability.ACODE_DESCS[i].equalsIgnoreCase(newID))
				{
					IDclassFilter = i;
					eventTriggerID = newID;
					return true;
				}
			}
			for(int i=0;i<Ability.DOMAIN_DESCS.length;i++)
			{
				if(Ability.DOMAIN_DESCS[i].equalsIgnoreCase(newID))
				{
					IDdomainFilter = i << 5;
					eventTriggerID = newID;
					return true;
				}
			}
			for(int i=0;i< Ability.FLAG_DESCS.length;i++)
			{
				if(Ability.FLAG_DESCS[i].equalsIgnoreCase(newID))
				{
					IDflagFilter = Math.round(Math.pow(2, i));
					eventTriggerID = newID;
					return true;
				}
			}
			if(CMClass.getAbility(newID)!=null)
			{
				eventTriggerID = newID;
				return true;
			}
			if(CMLib.socials().fetchSocial(newID, true)!=null)
			{
				eventTriggerID=newID.toUpperCase();
				return true;
			}
			if((newID.endsWith(" *"))
			&&(CMLib.socials().fetchSocial(newID.substring(0,newID.length()-2).trim(), true)!=null))
			{
				eventTriggerID=newID.toUpperCase().trim();
				return true;
			}
			if(getCMMsgTypeMap().containsKey(newID.toUpperCase().trim()))
			{
				eventTriggerID=newID.toUpperCase().trim();
				return true;
			}
			return false;
		}

		public boolean setDirection(final String d)
		{
			if(CMath.isInteger(d))
				direction=CMath.s_int(d);
			else
			if(d.startsWith("U"))
			{
				direction = CHANGE_DIRECTION_UP;
			}
			else
			if(d.startsWith("D"))
			{
				direction = CHANGE_DIRECTION_DOWN;
			}
			else
			if(d.startsWith("OPP"))
			{
				direction = CHANGE_DIRECTION_OPPOSITE;
			}
			else
			if(d.startsWith("REM"))
			{
				direction = CHANGE_DIRECTION_REMOVE;
			}
			else
			if(d.startsWith("MIN"))
			{
				direction = CHANGE_DIRECTION_MINIMUM;
			}
			else
			if(d.startsWith("MAX"))
			{
				direction = CHANGE_DIRECTION_MAXIMUM;
			}
			else
			if(d.startsWith("ADD"))
			{
				direction = CHANGE_DIRECTION_ADD;
			}
			else
			if(d.startsWith("TOW"))
			{
				direction = CHANGE_DIRECTION_TOWARD;
			}
			else
			if(d.startsWith("AWA"))
			{
				direction = CHANGE_DIRECTION_AWAY;
			}
			else
				return false;
			return true;
		}

		@Override
		public void setFlags(final String newFlagCache)
		{
			flagCache=newFlagCache.toUpperCase().trim();
			flags=CMParms.parseStrictEQParms(newFlagCache);
			outsiderTargetOK=flags.containsKey("OUTSIDER");
			selfTargetOK=flags.containsKey("SELFOK");
			just100=flags.containsKey("JUST100");
			bonusXP=flags.containsKey("XP")?CMath.s_int(flags.get("XP")):0;
			bonusRPXP=flags.containsKey("RPXP")?CMath.s_int(flags.get("RPXP")):0;
		}

		@Override
		public String getFlagValue(String key)
		{
			key=key.toUpperCase().trim();
			return flags.containsKey(key) ? flags.get(key) : "";
		}

		@Override
		public boolean applies(final MOB source, final MOB target)
		{
			if(!CMLib.masking().maskCheck(compiledTargetZapper(),target,false))
				return false;
			if(!CMLib.masking().maskCheck(compiledSourceZapper(),source,false))
				return false;
			final Faction F=getFaction();
			if(F!=null)
			{
				if(withinTicks > 0)
				{
					final long now=System.currentTimeMillis() - (withinTicks * CMProps.getTickMillis());
					Faction.FData data;
					data = source.fetchFactionData(F.factionID());
					if(data != null)
					{
						if(now > data.getEventTime(eventTriggerID))
							return false;
					}
					if(source != target)
					{
						data = target.fetchFactionData(F.factionID());
						if(data != null)
						{
							if(now > data.getEventTime(eventTriggerID))
								return false;
						}
					}
				}
			}
			return true;
		}

		@Override
		public String triggerParameters()
		{
			return triggerParms;
		}

		@Override
		public void setTriggerParameters(final String newVal)
		{
			triggerParms=newVal;
			savedTriggerParms=CMParms.parseStrictEQParms(newVal);
			compiledSourceZapper=null;
			withinTicks=savedTriggerParms.containsKey("WITHIN")?CMath.s_int(savedTriggerParms.get("WITHIN")):-1;
			pctChance=savedTriggerParms.containsKey("CHANCE")?CMath.s_int(savedTriggerParms.get("CHANCE")):100;
			waitMs=savedTriggerParms.containsKey("WAIT")?(CMath.s_long(savedTriggerParms.get("WAIT"))*CMProps.getTickMillis()):-1;
		}

		@Override
		public Object stateVariable(final int x)
		{
			return ((x >= 0) && (x < stateVariables.length)) ? stateVariables[x] : null;
		}

		@Override
		public void setStateVariable(final int x, final Object newVal)
		{
			if(x<0)
				return;
			if(x>=stateVariables.length)
				stateVariables=Arrays.copyOf(stateVariables,x+1);
			stateVariables[x]=newVal;
		}

		@Override
		public Faction getFaction()
		{
			return myFaction;
		}
	}

	@Override
	public Faction.FRange addRange(final String key)
	{
		final Faction.FRange FR=new DefaultFaction.DefaultFactionRange(this,key);
		ranges.put(FR.codeName().toUpperCase().trim(),FR);
		recalc();
		return FR;
	}

	@Override
	public boolean delRange(final FRange FR)
	{
		if(!ranges.containsKey(FR.codeName().toUpperCase().trim()))
			return false;
		ranges.remove(FR.codeName().toUpperCase().trim());
		recalc();
		return true;
	}

	public class DefaultFactionRange implements Faction.FRange, Comparable<Faction.FRange>
	{
		public int				low;
		public int				high;
		public String			name		= "";
		public String			codeName	= "";
		public Faction.Align	alignEquiv;
		public Faction			faction		= null;

		@Override
		public int low()
		{
			return low;
		}

		@Override
		public int high()
		{
			return high;
		}

		@Override
		public String name()
		{
			return name;
		}

		@Override
		public String codeName()
		{
			return codeName;
		}

		@Override
		public Align alignEquiv()
		{
			return alignEquiv;
		}

		@Override
		public Faction getFaction()
		{
			return faction;
		}

		@Override
		public void setLow(final int newVal)
		{
			low = newVal;
		}

		@Override
		public void setHigh(final int newVal)
		{
			high = newVal;
		}

		@Override
		public void setName(final String newVal)
		{
			name = newVal;
		}

		@Override
		public void setAlignEquiv(final Faction.Align newVal)
		{
			alignEquiv = newVal;
		}

		public DefaultFactionRange(final Faction F, final String key)
		{
			faction=F;
			final List<String> v = CMParms.parseSemicolons(key,false);
			name = v.get(2);
			low = CMath.s_int( v.get(0));
			high = CMath.s_int( v.get(1));
			if(v.size()>3)
				codeName=v.get(3);
			if(v.size()>4)
				alignEquiv = CMLib.factions().getAlignEnum(v.get(4));
			else
				alignEquiv = Faction.Align.INDIFF;
		}

		@Override
		public String toString()
		{
			return low +";"+high+";"+name+";"+codeName+";"+alignEquiv.toString();
		}

		@Override
		public int random()
		{
			final Random gen = new Random();
			return high - gen.nextInt(high-low);
		}

		@Override
		public int med()
		{
			return high - (high-low)/2;
		}

		@Override
		public int compareTo(final FRange o)
		{
			if(low < o.low())
				return -1;
			if(high > o.high())
				return 1;
			return 0;
		}
	}

	@Override
	public Faction.FAbilityUsage addAbilityUsage(final String key)
	{
		final Faction.FAbilityUsage usage=
			(key==null)?new DefaultFaction.DefaultFactionAbilityUsage()
					  : new DefaultFaction.DefaultFactionAbilityUsage(key);
		abilityUsages.add(usage);
		abilityUsages.trimToSize();
		abilityUseCache.clear();
		return usage;
	}

	@Override
	public boolean delAbilityUsage(final Faction.FAbilityUsage usage)
	{
		if(!abilityUsages.remove(usage))
			return false;
		abilityUsages.trimToSize();
		abilityUseCache.clear();
		return true;
	}

	public class DefaultFactionData implements FData
	{
		private int				value;
		private boolean			noListeners;
		private boolean			noTickers;
		private boolean			noStatAffectors;
		private long			lastUpdated;
		private Ability[]		myEffects;
		private Behavior[]		myBehaviors;
		private Ability			lightPresenceAbilities[];
		private Faction.FRange	currentRange;
		private boolean			erroredOut;
		private Faction			myFaction;
		public boolean			isReset	= false;
		private DVector			currentReactionSets;
		private long			birthTime = System.currentTimeMillis();

		private final CMap<String, Long>				timers	= new SHashtable<String, Long>();
		private final CMap<String, int[]>				counters= new SHashtable<String, int[]>();
		private Map<Faction.FactionChangeEvent, Long>	nextChangeTime;

		public DefaultFactionData(final Faction F)
		{
			resetFactionData(F);
		}

		@Override
		public void resetFactionData(final Faction F)
		{
			if(!isReset)
			{
				myFaction=F;
				value=0;
				noListeners=false;
				noTickers=false;
				noStatAffectors=false;
				lastUpdated=System.currentTimeMillis();
				myEffects=new Ability[0];
				myBehaviors=new Behavior[0];
				currentReactionSets = new DVector(2);
				lightPresenceAbilities = new Ability[0];
				currentRange = null;
				erroredOut=false;
				isReset = true;
				nextChangeTime = new Hashtable<Faction.FactionChangeEvent, Long>();
				timers.clear();
				counters.clear();
			}
		}

		@Override
		public long getEventTime(final String eventID)
		{
			if(!timers.containsKey(eventID))
				return this.birthTime;
			return timers.get(eventID).longValue();
		}

		@Override
		public void resetEventTimers(final String eventID)
		{
			this.birthTime = System.currentTimeMillis();
			if(eventID == null)
			{
				timers.clear();
				nextChangeTime.clear();
			}
			else
			{
				timers.put(eventID, Long.valueOf(System.currentTimeMillis()));
				for(final Iterator<Faction.FactionChangeEvent> e = nextChangeTime.keySet().iterator();e.hasNext();)
				{
					final Faction.FactionChangeEvent event = e.next();
					if(event.eventID().equalsIgnoreCase(eventID))
					{
						nextChangeTime.remove(event);
						break;
					}
				}
			}
		}

		@Override
		public int value()
		{
			return value;
		}

		@Override
		public Faction getFaction()
		{
			return myFaction;
		}

		@Override
		public long getNextChangeTimers(final Faction.FactionChangeEvent event)
		{
			if(nextChangeTime.containsKey(event))
				return nextChangeTime.get(event).longValue();
			return 0;
		}

		@Override
		public void setNextChangeTimers(final Faction.FactionChangeEvent event, final long time)
		{
			nextChangeTime.put(event, Long.valueOf(time));
		}

		@SuppressWarnings("unchecked")
		@Override
		public void setValue(final int newValue)
		{
			this.value=newValue;
			if((currentRange==null)||(this.value<currentRange.low())||(this.value>currentRange.high()))
			{
				synchronized(this)
				{
					if((currentRange!=null)&&(this.value>=currentRange.low())&&(this.value<=currentRange.high()))
						return;
					currentRange = fetchRange(value);
					if(currentRange==null)
					{
						if(!erroredOut)
							Log.errOut("DefaultFactionData","Faction "+factionID()+" does not define a range for "+this.value);
						erroredOut=true;
					}
					else
					{
						erroredOut=false;
						currentReactionSets=new DVector(2);
						for(final Enumeration<Faction.FReactionItem> e=reactions();e.hasMoreElements();)
						{
							Faction.FReactionItem react = e.nextElement();
							if(!react.rangeCodeName().equalsIgnoreCase(currentRange.codeName()))
								continue;
							Faction.FReactionItem sampleReact = null;
							Vector<Faction.FReactionItem> reactSet=null;
							for(int r=0;r<currentReactionSets.size();r++)
							{
								reactSet=(Vector<Faction.FReactionItem>)currentReactionSets.elementAt(r,2);
								sampleReact=reactSet.firstElement();
								if(react.presentMOBMask().trim().equalsIgnoreCase(sampleReact.presentMOBMask().trim()))
								{
									reactSet.addElement(react);
									react=null; break;
								}
							}
							if(react!=null)
								currentReactionSets.addElement(react.compiledPresentMOBMask(),new XVector<Faction.FReactionItem>(react));
						}
						//noReactions=currentReactionSets.size()==0;
					}
					noListeners=(myEffects.length==0) && (myBehaviors.length==0) && (currentReactionSets.size()==0);
					noTickers=(myBehaviors.length==0) && (myEffects.length==0) &&((currentReactionSets.size()==0)||(!useLightReactions()));
				}
			}
		}

		@Override
		public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
		{
			if(!noStatAffectors)
				for(final Ability A : myEffects) A.affectPhyStats(affected, affectableStats);
		}

		@Override
		public void affectCharStats(final MOB affectedMob, final CharStats affectableStats)
		{
			if(!noStatAffectors)
				for(final Ability A : myEffects) A.affectCharStats(affectedMob, affectableStats);
		}

		@Override
		public void affectCharState(final MOB affectedMob, final CharState affectableMaxState)
		{
			if(!noStatAffectors)
				for(final Ability A : myEffects) A.affectCharState(affectedMob, affectableMaxState);
		}

		@Override
		public void addHandlers(final List<Ability> listeners, final List<Behavior> tickers)
		{
			this.myEffects=listeners.toArray(new Ability[0]);
			this.myBehaviors=tickers.toArray(new Behavior[0]);
			noListeners=(listeners.size()==0) && (tickers.size()==0) && (currentReactionSets.size()==0);
			noTickers=(listeners.size()==0) && (tickers.size()==0) && ((currentReactionSets.size()==0)||(!useLightReactions()));
			noStatAffectors=(listeners.size()==0);
			isReset = false;
		}

		@Override
		public boolean requiresUpdating()
		{
			return lastDataChange[0] > lastUpdated;
		}

		@SuppressWarnings("unchecked")
		private Ability setPresenceReaction(final MOB M, final Physical myHost)
		{
			if((!CMLib.flags().canBeSeenBy(myHost, M))
			&&(!CMLib.flags().canBeHeardMovingBy(myHost,M)))
				return null;
			if((M.amUltimatelyFollowing()!=null)
			&&(!M.amUltimatelyFollowing().isMonster()))
				return null;
			Vector<String> myReactions=null;
			List<Faction.FReactionItem> tempReactSet=null;
			for(int d=0;d<currentReactionSets.size();d++)
			{
				if(CMLib.masking().maskCheck((MaskingLibrary.CompiledZMask)currentReactionSets.elementAt(d,1),M,true))
				{
					if(myReactions==null)
						myReactions=new Vector<String>();
					tempReactSet=(List<Faction.FReactionItem>)currentReactionSets.elementAt(d,2);
					for(final Faction.FReactionItem reactionItem : tempReactSet)
						myReactions.add(reactionItem.reactionObjectID()+"="+reactionItem.parameters(myHost.Name()));
				}
			}
			if(myReactions!=null)
			{
				if(useLightReactions())
				{
					final Ability A=(Ability)presenceReactionPrototype.copyOf();
					A.invoke(M,myReactions,myHost,false,0);
					A.setInvoker(M);
					return A;
				}
				else
				if(M.fetchEffect(presenceReactionPrototype.ID())==null)
				{
					final Ability A=(Ability)presenceReactionPrototype.copyOf();
					A.invoke(M,myReactions,myHost,true,0);
				}
			}
			return null;
		}

		@Override
		public void executeMsg(final Environmental myHost, final CMMsg msg)
		{
			if(noListeners)
				return;
			synchronized(lightPresenceAbilities)
			{
				if((currentReactionSets.size()>0)
				&&(msg.sourceMinor()==CMMsg.TYP_ENTER)
				&&(msg.target() instanceof Room))
				{
					if(presenceReactionPrototype==null)
					{
						if((presenceReactionPrototype=CMClass.getAbility("PresenceReaction"))==null)
							return;
					}
					if((msg.source()==myHost)
					&&(!msg.source().isMonster()))
					{
						MOB M=null;
						final Room R=(Room)msg.target();
						final List<Ability> lightPresenceReactions=new LinkedList<Ability>();
						Ability A=null;
						for(int m=0;m<R.numInhabitants();m++)
						{
							M=R.fetchInhabitant(m);
							if((M!=null)
							&&(M!=myHost)
							&&(M.isMonster())) // follow checks are in setPresenceReaction
							{
								A=setPresenceReaction(M,msg.source());
								if(A!=null) // means yes, we are using light, and yes, heres a reaction to add
									lightPresenceReactions.add(A);
							}
						}
						lightPresenceAbilities = lightPresenceReactions.toArray(new Ability[0]);
					}
					else
					if((msg.source().isMonster())
					&&(msg.target()==CMLib.map().roomLocation(myHost))
					&&(myHost instanceof Physical))
					{
						if(!(myHost instanceof MOB))
						{
							// follow checks are in setPresenceReaction
							final Ability A=setPresenceReaction(msg.source(),(Physical)myHost);
							if(A!=null)
							{ // means yes, we are using light, and yes, heres a reaction to add
								lightPresenceAbilities = Arrays.copyOf(lightPresenceAbilities, lightPresenceAbilities.length+1);
								lightPresenceAbilities[lightPresenceAbilities.length-1]=A;
							}
						}
					}
				}
				else
				if((lightPresenceAbilities.length>0)
				&&(msg.sourceMinor()==CMMsg.TYP_LEAVE)
				&&(msg.target() instanceof Room))
				{
					if((msg.source()==myHost)
					&&(!msg.source().isMonster()))
					{
						final Room R=(Room)msg.target();
						MOB M=null;
						for(int m=0;m<R.numInhabitants();m++)
						{
							M=R.fetchInhabitant(m);
							if((M!=null)
							&&(M!=myHost)
							&&(M.isMonster()))
								presenceReactionPrototype.invoke(M,new Vector<String>(),null,true,0); // this shuts it down
						}
						lightPresenceAbilities=new Ability[0];
					}
					else
					if((msg.source().isMonster())
					&&(myHost instanceof Physical))
					{
						presenceReactionPrototype.invoke(msg.source(),new Vector<String>(),null,true,0);
						final Ability[] newAbilities = new Ability[lightPresenceAbilities.length];
						int l=0;
						for (final Ability lightPresenceAbilitie : lightPresenceAbilities)
						{
							if(lightPresenceAbilitie.affecting()==null)
							{
							}
							else
							if(lightPresenceAbilitie.affecting()==msg.source())
								lightPresenceAbilitie.invoke(msg.source(),new Vector<String>(),null,true,0);
							else
								newAbilities[l++]=lightPresenceAbilitie;
						}
						if(l==0)
							lightPresenceAbilities=new Ability[0];
						else
						if(l<lightPresenceAbilities.length)
							lightPresenceAbilities = Arrays.copyOf(newAbilities, l);
					}
				}
			}
			for(final Ability A : lightPresenceAbilities)
				A.executeMsg(A.invoker(), msg);
			for(final Ability A : myEffects)
				A.executeMsg(myHost, msg);
			for(final Behavior B : myBehaviors)
				B.executeMsg(myHost, msg);
		}

		@Override
		public boolean okMessage(final Environmental myHost, final CMMsg msg)
		{
			if(noListeners)
				return true;
			for(final Ability A : myEffects)
			{
				if(!A.okMessage(myHost, msg))
					return false;
			}
			for(final Behavior B : myBehaviors)
			{
				if(!B.okMessage(myHost, msg))
					return false;
			}
			for(final Ability A : lightPresenceAbilities)
			{
				if(!A.okMessage(A.invoker(), msg))
					return false;
			}
			return true;
		}

		@Override
		public boolean tick(final Tickable ticking, final int tickID)
		{
			if(noTickers)
				return true;
			for(final Ability A : myEffects)
			{
				if(!A.tick(ticking, tickID))
					return false;
			}
			for(final Behavior B : myBehaviors)
			{
				if(!B.tick(ticking, tickID))
					return false;
			}
			for(final Ability A : lightPresenceAbilities)
			{
				if(!A.tick(A.invoker(), tickID))
					return false;
			}
			return true;
		}

		@Override
		public int getCounter(final String key)
		{
			if(counters.containsKey(key))
				return counters.get(key)[0];
			return 0;
		}

		@Override
		public void setCounter(final String key, final int newValue)
		{
			if(key == null)
				counters.clear();
			else
			if(counters.containsKey(key))
				counters.get(key)[0]=newValue;
			else
				counters.put(key, new int[] {newValue});
		}
	}

	public class DefaultFactionZapFactor implements Faction.FZapFactor
	{
		private double gainF= 1.0;
		private double lossF= 1.0;
		private String mask	= "";
		private MaskingLibrary.CompiledZMask compiledMask=null;

		public DefaultFactionZapFactor(final double gain, final double loss, final String mask)
		{
			setGainFactor(gain);
			setLossFactor(loss);
			setMOBMask(mask);
		}

		@Override
		public double gainFactor()
		{
			return gainF;
		}

		@Override
		public void setGainFactor(final double val)
		{
			gainF = val;
		}

		@Override
		public double lossFactor()
		{
			return lossF;
		}

		@Override
		public void setLossFactor(final double val)
		{
			lossF = val;
		}

		@Override
		public String MOBMask()
		{
			return mask;
		}

		@Override
		public MaskingLibrary.CompiledZMask compiledMOBMask()
		{
			return compiledMask;
		}

		@Override
		public void setMOBMask(final String str)
		{
			mask=str;
			compiledMask=CMLib.masking().maskCompile(str);
		}

		@Override
		public String toString()
		{
			return gainF + ";" + lossF + ";" + mask;
		}
	}

	public class DefaultFactionReactionItem implements Faction.FReactionItem
	{
		private String reactionObjectID	="";
		private String mobMask			="";
		private String rangeName		="";
		private String parms			="";

		private MaskingLibrary.CompiledZMask compiledMobMask=null;

		@Override
		public String reactionObjectID()
		{
			return reactionObjectID;
		}

		@Override
		public void setReactionObjectID(final String str)
		{
			reactionObjectID = str;
		}

		@Override
		public String presentMOBMask()
		{
			return mobMask;
		}

		@Override
		public void setPresentMOBMask(final String str)
		{
			mobMask=str;
			if((str==null)||(str.trim().length()==0))
				compiledMobMask=null;
			else
				compiledMobMask=CMLib.masking().maskCompile(str);
		}

		@Override
		public MaskingLibrary.CompiledZMask compiledPresentMOBMask()
		{
			return compiledMobMask;
		}

		@Override
		public String rangeCodeName()
		{
			return rangeName;
		}

		@Override
		public void setRangeName(final String str)
		{
			rangeName = str.toUpperCase().trim();
		}

		@Override
		public String parameters()
		{
			return parms;
		}

		@Override
		public String parameters(final String name)
		{
			return CMStrings.replaceAll(parms, "<TARGET>", name);
		}

		@Override
		public void setParameters(final String str)
		{
			parms = str;
		}

		@Override
		public String toString()
		{
			return rangeName + ";" + mobMask + ";" + reactionObjectID + ";" + parms;
		}

		public DefaultFactionReactionItem()
		{
		}

		public DefaultFactionReactionItem(final String key)
		{
			int x=key.indexOf(';');
			String str = key.substring(0,x).toUpperCase().trim();
			String rest = key.substring(x+1);
			setRangeName(str);

			x=rest.indexOf(';');
			str = rest.substring(0,x).trim();
			rest = rest.substring(x+1);
			setPresentMOBMask(str);

			x=rest.indexOf(';');
			str = rest.substring(0,x).trim();
			rest = rest.substring(x+1);
			setReactionObjectID(str);
			setParameters(rest);
		}

	}

	public class DefaultFactionAbilityUsage implements Faction.FAbilityUsage
	{
		public String	ID					= "";
		public boolean	possibleAbilityID	= false;
		public int		type				= -1;
		public int		domain				= -1;
		public long		flag				= -1;
		public int		low					= 0;
		public int		high				= 0;
		public long		notflag				= -1;

		@Override
		public String abilityFlags()
		{
			return ID;
		}

		@Override
		public boolean possibleAbilityID()
		{
			return possibleAbilityID;
		}

		@Override
		public int type()
		{
			return type;
		}

		@Override
		public int domain()
		{
			return domain;
		}

		@Override
		public long flag()
		{
			return flag;
		}

		@Override
		public int low()
		{
			return low;
		}

		@Override
		public int high()
		{
			return high;
		}

		@Override
		public long notflag()
		{
			return notflag;
		}

		@Override
		public void setLow(final int newVal)
		{
			low = newVal;
		}

		@Override
		public void setHigh(final int newVal)
		{
			high = newVal;
		}

		public DefaultFactionAbilityUsage()
		{
		}

		public DefaultFactionAbilityUsage(final String key)
		{
			final List<String> v = CMParms.parseSemicolons(key,false);
			setAbilityFlag(v.get(0));
			low = CMath.s_int( v.get(1));
			high = CMath.s_int( v.get(2));
		}

		@Override
		public String toString()
		{
			return ID+";"+low+";"+high;
		}

		@Override
		public List<String> setAbilityFlag(final String str)
		{
			ID=str;
			final List<String> flags=CMParms.parse(ID);
			final List<String> unknowns=new Vector<String>();
			possibleAbilityID=false;
			for(int f=0;f<flags.size();f++)
			{
				String strflag=flags.get(f);
				final boolean not=strflag.startsWith("!");
				if(not)
					strflag=strflag.substring(1);
				switch(CMLib.factions().getAbilityFlagType(strflag))
				{
				case 1:
					type=CMParms.indexOfIgnoreCase(Ability.ACODE_DESCS, strflag);
					break;
				case 2:
					domain=CMParms.indexOfIgnoreCase(Ability.DOMAIN_DESCS, strflag);
					break;
				case 3:
					final int val=CMParms.indexOfIgnoreCase(Ability.FLAG_DESCS, strflag);
					if(not)
					{
						if(notflag<0)
							notflag=0;
						notflag=notflag|CMath.pow(2,val);
					}
					else
					{
						if(flag<0)
							flag=0;
						flag=flag|CMath.pow(2,val);
					}
					break;
				default:
					unknowns.add(strflag);
					break;
				}
			}
			if((type<0)&&(domain<0)&&(flag<0))
				possibleAbilityID=true;
			return unknowns;
		}
	}

	@Override
	public void destroy()
	{
		CMLib.factions().removeFaction(this.factionID());
		this.destroyed=true;
		defaults.clear();
		autoDefaults.clear();
		ranges.clear();
		affBehavs.clear();
		changes.clear();
		abilChangeCache.clear();
		msgChangeCache.clear();
		socChangeCache.clear();
		factors.clear();
		relations.clear();
		abilityUsages.clear();
		abilityUseCache.clear();
		choices.clear();
		reactions.clear();
		reactionHash.clear();
	}

	@Override
	public boolean isSavable()
	{
		return (internalFlagBitmap & Faction.IFLAG_NEVERSAVE) == 0;
	}

	@Override
	public void setSavable(final boolean truefalse)
	{
	}

	@Override
	public boolean amDestroyed()
	{
		return destroyed;
	}
}
