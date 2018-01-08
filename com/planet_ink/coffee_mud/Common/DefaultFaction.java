package com.planet_ink.coffee_mud.Common;
import com.planet_ink.coffee_mud.core.database.DBInterface;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
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
 * Portions Copyright (c) 2004-2018 Bo Zimmerman
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
	public int compareTo(CMObject o)
	{
		return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
	}

	protected String	 ID					= "";
	protected String	 name				= "";
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
	protected boolean    destroyed			= false;
	
	protected CList<String>   					defaults		 = new SVector<String>();
	protected CList<String>   					autoDefaults	 = new SVector<String>();
	protected CMap<String,FRange> 				ranges			 = new SHashtable<String,FRange>();
	protected Map<Integer,FRange> 				rangeRangeMap	 = new PrioritizingLimitedMap<Integer,FRange>(10,60000,600000,100);
	protected CMap<String,String[]>   			affBehavs		 = new SHashtable<String,String[]>();
	protected double							rateModifier	 = 1.0;
	protected CMap<String,FactionChangeEvent[]>	changes			 = new SHashtable<String,FactionChangeEvent[]>();
	protected CMap<String,FactionChangeEvent[]>	abilChangeCache	 = new SHashtable<String,FactionChangeEvent[]>();
	protected CList<Faction.FZapFactor>			factors			 = new SVector<Faction.FZapFactor>();
	protected CMap<String,Double> 				relations		 = new SHashtable<String,Double>();
	protected CList<Faction.FAbilityUsage>		abilityUsages	 = new SVector<Faction.FAbilityUsage>();
	protected CList<String>   					choices			 = new SVector<String>();
	protected CList<Faction.FReactionItem>		reactions		 = new SVector<Faction.FReactionItem>();
	protected CMap<String,CList<FReactionItem>>	reactionHash	 = new SHashtable<String,CList<Faction.FReactionItem>>();

	@Override
	public Enumeration<Faction.FReactionItem> reactions()
	{
		return reactions.elements();
	}

	@Override
	public Enumeration<Faction.FReactionItem> reactions(String rangeName)
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
		return ID;
	}

	@Override
	public String name()
	{
		return name;
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
	public void disable(boolean truefalse)
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
	public void setLightReactions(boolean truefalse)
	{
		useLightReactions = truefalse;
	}

	@Override
	public boolean useLightReactions()
	{
		return useLightReactions;
	}

	@Override
	public void setFactionID(String newStr)
	{
		ID = newStr;
	}

	@Override
	public void setName(String newStr)
	{
		name = newStr;
	}

	@Override
	public void setInternalFlags(long bitmap)
	{
		internalFlagBitmap = bitmap;
	}

	@Override
	public void setChoiceIntro(String newStr)
	{
		choiceIntro = newStr;
	}

	@Override
	public void setExperienceFlag(String newStr)
	{
		experienceFlag = newStr;
	}

	@Override
	public void setShowInScore(boolean truefalse)
	{
		showInScore = truefalse;
	}

	@Override
	public void setShowInSpecialReported(boolean truefalse)
	{
		showInSpecialReport = truefalse;
	}

	@Override
	public void setShowInEditor(boolean truefalse)
	{
		showInEditor = truefalse;
	}

	@Override
	public void setShowInFactionsCommand(boolean truefalse)
	{
		showInFacCommand = truefalse;
	}

	@Override
	public void setChoices(List<String> v)
	{
		choices = new SVector<String>(v);
	}

	@Override
	public void setAutoDefaults(List<String> v)
	{
		autoDefaults = new SVector<String>(v);
	}

	@Override
	public void setDefaults(List<String> v)
	{
		defaults = new SVector<String>(v);
	}

	@Override
	public void setRateModifier(double d)
	{
		rateModifier = d;
	}

	@Override
	public boolean isPreLoaded()
	{
		return CMParms.parseSemicolons(CMProps.getVar(CMProps.Str.PREFACTIONS).toUpperCase(),true).contains(factionID().toUpperCase());
	}

	@Override
	public Faction.FAbilityUsage getAbilityUsage(int x)
	{
		return ((x>=0)&&(x<abilityUsages.size()))
				?(Faction.FAbilityUsage)abilityUsages.get(x)
				:null;
	}

	@Override
	public boolean delFactor(Faction.FZapFactor f)
	{
		if(!factors.remove(f))
			return false;
		factors.trimToSize();
		return true;
	}
	
	@Override
	public Faction.FZapFactor getFactor(int x)
	{
		return ((x >= 0) && (x < factors.size())) ? factors.get(x) : null;
	}

	@Override
	public Faction.FZapFactor addFactor(double gain, double loss, String mask)
	{
		final Faction.FZapFactor o=new DefaultFactionZapFactor(gain,loss,mask);
		factors.add(o);
		factors.trimToSize();
		return o;
	}

	@Override
	public boolean delRelation(String factionID)
	{
		if(relations.remove(factionID)==null)
			return false;
		return true;
	}

	@Override
	public boolean addRelation(String factionID, double relation)
	{
		if(relations.containsKey(factionID))
			return false;
		relations.put(factionID,Double.valueOf(relation));
		return true;
	}

	@Override
	public double getRelation(String factionID)
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
	public void initializeFaction(String aname)
	{
		ID=aname;
		name=aname;
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
	public void initializeFaction(StringBuffer file, String fID)
	{
		final boolean debug = false;

		ID = fID;
		final CMProps alignProp = new CMProps(new ByteArrayInputStream(CMStrings.strToBytes(file.toString())));
		if(alignProp.isEmpty())
			return;
		name=alignProp.getStr("NAME");
		choiceIntro=alignProp.getStr("CHOICEINTRO");
		minimum=alignProp.getInt("MINIMUM");
		maximum=alignProp.getInt("MAXIMUM");
		if(maximum<minimum)
		{
			minimum=maximum;
			maximum=alignProp.getInt("MINIMUM");
		}
		recalc();
		experienceFlag=alignProp.getStr("EXPERIENCE").toUpperCase().trim();
		if(experienceFlag.length()==0)
			experienceFlag="NONE";
		rateModifier=alignProp.getDouble("RATEMODIFIER");
		showInScore=alignProp.getBoolean("SCOREDISPLAY");
		showInFacCommand=alignProp.getBoolean("SHOWINFACTIONSCMD");
		showInSpecialReport=alignProp.getBoolean("SPECIALREPORTED");
		showInEditor=alignProp.getBoolean("EDITALONE");
		defaults=new SVector<String>(CMParms.parseSemicolons(alignProp.getStr("DEFAULT"),true));
		autoDefaults =new SVector<String>(CMParms.parseSemicolons(alignProp.getStr("AUTODEFAULTS"),true));
		choices =new SVector<String>(CMParms.parseSemicolons(alignProp.getStr("AUTOCHOICES"),true));
		useLightReactions=alignProp.getBoolean("USELIGHTREACTIONS");
		ranges=new SHashtable<String,FRange>();
		changes=new SHashtable<String,FactionChangeEvent[]>();
		factors=new SVector<FZapFactor>();
		relations=new SHashtable<String,Double>();
		abilityUsages=new SVector<FAbilityUsage>();
		reactions=new SVector<FReactionItem>();
		reactionHash=new SHashtable<String,CList<FReactionItem>>();
		for(final Enumeration<Object> e=alignProp.keys();e.hasMoreElements();)
		{
			if(debug)
				Log.sysOut("FACTIONS","Starting Key Loop");
			final String key = (String) e.nextElement();
			if(debug)
				Log.sysOut("FACTIONS","  Key Found     :"+key);
			final String words = (String) alignProp.get(key);
			if(debug)
				Log.sysOut("FACTIONS","  Words Found   :"+words);
			if(key.startsWith("RANGE"))
				addRange(words);
			if(key.startsWith("CHANGE"))
				createChangeEvent(words);
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
	public String getTagValue(String tag)
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
	public String getINIDef(String tag, String delimeter)
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
	public void updateFactionData(MOB mob, FData data)
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
	public FData makeFactionData(MOB mob)
	{
		final FData data=new DefaultFactionData(this);
		updateFactionData(mob,data);
		return data;
	}

	@Override
	public boolean delAffectBehav(String ID)
	{
		final boolean b=affBehavs.remove(ID.toUpperCase().trim())!=null;
		if(b)
			lastDataChange[0]=System.currentTimeMillis();
		return b;
	}

	@Override
	public boolean addAffectBehav(String ID, String parms, String gainMask)
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
	public String[] getAffectBehav(String ID)
	{
		if(affBehavs.containsKey(ID.toUpperCase().trim()))
			return CMParms.toStringArray(new XVector<String>(affBehavs.get(ID.toUpperCase().trim())));
		return null;
	}

	@Override
	public boolean delReaction(Faction.FReactionItem item)
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
	public boolean addReaction(String range, String mask, String abilityID, String parms)
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
	public FactionChangeEvent[] getChangeEvents(String key)
	{
		return changes.get(key);
	}

	@Override
	public List<Integer> findChoices(MOB mob)
	{
		final Vector<Integer> mine=new Vector<Integer>();
		String s;
		for(final Enumeration<String> e=choices.elements();e.hasMoreElements();)
		{
			s=e.nextElement();
			if(CMath.isInteger(s))
				mine.addElement(Integer.valueOf(CMath.s_int(s)));
			else
			if(CMLib.masking().maskCheck(s, mob,false))
			{
				final Vector<String> V=CMParms.parse(s);
				for(int j=0;j<V.size();j++)
				{
					if(CMath.isInteger(V.elementAt(j)))
						mine.addElement(Integer.valueOf(CMath.s_int(V.elementAt(j))));
				}
			}
		}
		return mine;
	}

	@Override
	public FactionChangeEvent[] findAbilityChangeEvents(Ability key)
	{
		if(key==null)
			return null;
		// Direct ability ID's
		if(abilChangeCache.containsKey(key.ID().toUpperCase()))
			return abilChangeCache.get(key.ID().toUpperCase());
		if(changes.containsKey(key.ID().toUpperCase()))
		{
			abilChangeCache.put(key.ID().toUpperCase(), changes.get(key.ID().toUpperCase()));
			return abilChangeCache.get(key.ID().toUpperCase());
		}
		// By TYPE or FLAGS
		FactionChangeEvent[] Cs =null;
		final Vector<FactionChangeEvent> events=new Vector<FactionChangeEvent>();
		for (final Enumeration<FactionChangeEvent[]> e=changes.elements();e.hasMoreElements();)
		{
			Cs=e.nextElement();
			for(final FactionChangeEvent C : Cs)
			{
				if((key.classificationCode()&Ability.ALL_ACODES)==C.IDclassFilter())
					events.addElement(C);
				else
				if((key.classificationCode()&Ability.ALL_DOMAINS)==C.IDdomainFilter())
					events.addElement(C);
				else
				if((C.IDflagFilter()>0)&&(CMath.bset(key.flags(),C.IDflagFilter())))
					events.addElement(C);
			}
		}
		final FactionChangeEvent[] evs = events.toArray(new FactionChangeEvent[0]);
		abilChangeCache.put(key.ID().toUpperCase(), evs);
		return evs;
	}

	@Override
	public Faction.FRange fetchRange(String codeName)
	{
		return ranges.get(codeName.toUpperCase().trim());
	}

	@Override
	public FRange fetchRange(int faction)
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
	public String fetchRangeName(int faction)
	{
		final FRange R= fetchRange(faction);
		if(R!=null)
			return R.name();
		return "";
	}

	@Override
	public int asPercent(int faction)
	{
		return (int)Math.round(CMath.mul(CMath.div(faction-minimum,(maximum-minimum)),100));
	}

	@Override
	public int asPercentFromAvg(int faction)
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
	public int findDefault(MOB mob)
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
	public int findAutoDefault(MOB mob)
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
	public boolean hasFaction(MOB mob)
	{
		return (mob.fetchFaction(ID)!=Integer.MAX_VALUE);
	}

	@Override
	public boolean hasUsage(Ability A)
	{
		for(final FAbilityUsage usage : abilityUsages)
		{
			if((usage.possibleAbilityID()&&usage.abilityFlags().equalsIgnoreCase(A.ID()))
			||(((usage.type()<0)||((A.classificationCode()&Ability.ALL_ACODES)==usage.type()))
				&&((usage.flag()<0)||(CMath.bset(A.flags(),usage.flag())))
				&&((usage.notflag()<0)||(!CMath.bset(A.flags(),usage.notflag())))
				&&((usage.domain()<0)||((A.classificationCode()&Ability.ALL_DOMAINS)==usage.domain()))))
				return true;
		}
		return false;
	}

	@Override
	public boolean canUse(MOB mob, Ability A)
	{
		for(final FAbilityUsage usage : abilityUsages)
		{
			if((usage.possibleAbilityID()&&usage.abilityFlags().equalsIgnoreCase(A.ID()))
			||(((usage.type()<0)||((A.classificationCode()&Ability.ALL_ACODES)==usage.type()))
				&&((usage.flag()<0)||(CMath.bset(A.flags(),usage.flag())))
				&&((usage.notflag()<0)||(!CMath.bset(A.flags(),usage.notflag())))
				&&((usage.domain()<0)||((A.classificationCode()&Ability.ALL_DOMAINS)==usage.domain()))))
			{
				final int faction=mob.fetchFaction(ID);
				if((faction < usage.low()) || (faction > usage.high()))
					return false;
			}
		}
		return true;
	}

	@Override
	public double findFactor(MOB mob, boolean gain)
	{
		for(final Faction.FZapFactor factor : factors)
		{
			if(CMLib.masking().maskCheck(factor.compiledMOBMask(),mob,false))
				return gain?factor.gainFactor():factor.lossFactor();
		}
		return 1.0;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		FactionChangeEvent[] events;
		if((msg.sourceMinor()==CMMsg.TYP_DEATH)    // A death occured
		&&((msg.source()==myHost)||(msg.tool()==myHost))
		&&(msg.tool() instanceof MOB))
		{
			final MOB killedM=msg.source();
			final MOB killingBlowM=(MOB)msg.tool();
			events=getChangeEvents((msg.source()==myHost)?"MURDER":"KILL");
			FactionChangeEvent eventC;
			if(events!=null)
			{
				for (final FactionChangeEvent event : events)
				{
					eventC=event;
					if(eventC.applies(killingBlowM,killedM))
					{
						final CharClass combatCharClass=CMLib.combat().getCombatDominantClass(killingBlowM,killedM);
						final Set<MOB> combatBeneficiaries=CMLib.combat().getCombatBeneficiaries(killingBlowM,killedM,combatCharClass);
						for (final MOB mob : combatBeneficiaries)
							executeChange(mob,killedM,eventC);
					}
				}
			}
		}

		if((msg.tool() instanceof Ability)
		&&(msg.target()==myHost)	// Arrested watching
		&&(msg.tool().ID().equals("Skill_Handcuff"))
		&&(msg.source().isMonster()))
		{
			final Room R=msg.source().location();
			if((R!=null)&&(R.getArea()!=null))
			{
				FactionChangeEvent eventC;
				events=getChangeEvents("ARRESTED");
				if(events!=null)
				{
					final LegalBehavior B=CMLib.law().getLegalBehavior(R);
					if((B!=null)&&(B.isAnyOfficer(R.getArea(), msg.source())))
					{
						for (final FactionChangeEvent event : events)
						{
							eventC=event;
							if(eventC.applies(msg.source(),(MOB)msg.target()))
								executeChange(msg.source(),(MOB)msg.target(),eventC);
						}
					}
				}
			}
		}

		if((msg.sourceMinor()==CMMsg.TYP_GIVE)    // Bribe watching
		&&(msg.source()==myHost)
		&&(msg.tool() instanceof Coins)
		&&(msg.target() instanceof MOB))
		{
			FactionChangeEvent eventC;
			events=getChangeEvents("BRIBE");
			if(events!=null)
			{
				for (final FactionChangeEvent event : events)
				{
					eventC=event;
					if(eventC.applies(msg.source(),(MOB)msg.target()))
					{
						double amount=CMath.s_double(eventC.getTriggerParm("AMOUNT"));
						final double pctAmount = CMath.s_pct(eventC.getTriggerParm("PCT"))
										 * CMLib.beanCounter().getTotalAbsoluteNativeValue((MOB)msg.target());
						if(pctAmount>amount)
							amount=pctAmount;
						if(amount==0)
							amount=1.0;
						if(((Coins)msg.tool()).getTotalValue()>=amount)
							executeChange(msg.source(),(MOB)msg.target(),eventC);
					}
				}
			}
		}

		if((msg.sourceMinor()==CMMsg.TYP_SPEAK)    // Talk watching
		&&(msg.othersMessage()!=null)
		&&(msg.source()==myHost))
		{
			FactionChangeEvent eventC;
			events=getChangeEvents("TALK");
			if((events!=null)&&(events.length>0))
			{
				final Room R=msg.source().location();
				final Vector<MOB> targets=new Vector<MOB>();
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
					for (final FactionChangeEvent event : events)
					{
						eventC=event;
						Long time=(Long)eventC.stateVariable(1);
						if(time==null)
							time=Long.valueOf(System.currentTimeMillis());
						if(System.currentTimeMillis()<time.longValue())
							continue;
						Pattern P=(Pattern)eventC.stateVariable(0);
						if(P==null)
						{
							String mask=eventC.getTriggerParm("REGEX");
							if((mask==null)||(mask.trim().length()==0))
								mask=".*";
							P=Pattern.compile(mask.toLowerCase());
							eventC.setStateVariable(0,P);
						}
						M=P.matcher(sayMsg);
						if(M.matches())
						{
							Long addTime=(Long)eventC.stateVariable(2);
							if(addTime==null)
							{
								addTime=Long.valueOf(CMath.s_long(eventC.getTriggerParm("WAIT"))*CMProps.getTickMillis());
								eventC.setStateVariable(2,addTime);
							}
							eventC.setStateVariable(1,Long.valueOf(System.currentTimeMillis()+addTime.longValue()));
							for(final MOB target : targets)
							{
								if(eventC.applies(msg.source(),target))
								{
									executeChange(msg.source(),target,eventC);
									break;
								}
							}
						}
					}
				}
			}
			events=getChangeEvents("MUDCHAT");
			if((events!=null)&&(events.length>0))
			{
				final Room R=msg.source().location();
				final Vector<MOB> targets=new Vector<MOB>();
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
							for (final FactionChangeEvent event : events)
							{
								eventC=event;
								Long time=(Long)eventC.stateVariable(0);
								if(time==null)
									time=Long.valueOf(System.currentTimeMillis());
								if(System.currentTimeMillis()<time.longValue())
									continue;
								Long addTime=(Long)eventC.stateVariable(1);
								if(addTime==null)
								{
									addTime=Long.valueOf(CMath.s_long(eventC.getTriggerParm("WAIT"))*CMProps.getTickMillis());
									if(addTime.longValue()<CMProps.getTickMillis())
										addTime=Long.valueOf(CMProps.getTickMillis());
									eventC.setStateVariable(1,addTime);
								}
								eventC.setStateVariable(0,Long.valueOf(System.currentTimeMillis()+addTime.longValue()));
								if(eventC.applies(msg.source(),target))
								{
									if((mudChatB.getLastRespondedTo()==msg.source())
									&&(mudChatB.getLastThingSaid()!=null)
									&&(!mudChatB.getLastThingSaid().equalsIgnoreCase(sayMsg)))
									{
										executeChange(msg.source(),target,eventC);
										foundOne=true;
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
		// Ability Watching
		if((msg.tool() instanceof Ability)
		&&(msg.othersMessage()!=null)
		&&((events=findAbilityChangeEvents((Ability)msg.tool()))!=null))
		{
			for (final FactionChangeEvent C : events)
			{
				if((msg.target() instanceof MOB)&&(C.applies(msg.source(),(MOB)msg.target())))
					executeChange(msg.source(),(MOB)msg.target(),C);
				else
				if (!(msg.target() instanceof MOB))
					executeChange(msg.source(),null,C);
			}
		}
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((msg.sourceMinor()==CMMsg.TYP_EXPCHANGE)  // Experience is being altered
		&&(msg.target() instanceof MOB) 		  // because a mob died
		&&(myHost==msg.source())	  // this Faction is on the mob that killed them
		&&(!experienceFlag.equals("NONE"))
		&&(msg.value()>0))
		{
			final MOB killer=msg.source();
			final MOB vic=(MOB)msg.target();

			if(experienceFlag.equals("HIGHER"))
				msg.setValue( (int)Math.round(((msg.value())*.75) +( ((msg.value())*.25) * CMath.div(Math.abs(killer.fetchFaction(ID)-minimum),(maximum - minimum)))));
			else
			if(experienceFlag.equals("LOWER"))
				msg.setValue( (int)Math.round(((msg.value())*.75) +( ((msg.value())*.25) * CMath.div(Math.abs(maximum-killer.fetchFaction(ID)),(maximum - minimum)))));
			else
			if(vic.fetchFaction(ID)!=Integer.MAX_VALUE)
			{
				if(experienceFlag.equals("EXTREME"))
					msg.setValue( (int)Math.round(((msg.value())*.75) +( ((msg.value())*.25) * CMath.div(Math.abs(vic.fetchFaction(ID) - killer.fetchFaction(ID)),(maximum - minimum)))));
				else
				if(experienceFlag.equals("FOLLOWHIGHER"))
					msg.setValue( (int)Math.round(((msg.value())*.75) +( ((msg.value())*.25) * CMath.div(Math.abs(vic.fetchFaction(ID)-minimum),(maximum - minimum)))));
				else
				if(experienceFlag.equals("FOLLOWLOWER"))
					msg.setValue( (int)Math.round(((msg.value())*.75) +( ((msg.value())*.25) * CMath.div(Math.abs(maximum-vic.fetchFaction(ID)),(maximum - minimum)))));
				if(msg.value()<=0)
					msg.setValue(0);
			}
		}
		return true;
	}

	@Override
	public void executeChange(MOB source, MOB target, FactionChangeEvent event)
	{
		final int sourceFaction= source.fetchFaction(ID);
		int targetFaction = sourceFaction * -1;
		if((source==target)&&(!event.selfTargetOK())&&(!event.eventID().equalsIgnoreCase("TIME")))
			return;

		if(target!=null)
		{
			if(hasFaction(target))
				targetFaction=target.fetchFaction(ID);
			else
			if(!event.outsiderTargetOK())
				return;
		}
		else
			target = source;

		double baseChangeAmount=100.0;
		if((source!=target)&&(!event.just100()))
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
				source.addFaction(ID,0);
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

		if(factionAdj==0)
			return;

		CMMsg FacMsg=CMClass.getMsg(source,target,null,CMMsg.MASK_ALWAYS|CMMsg.TYP_FACTIONCHANGE,null,CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,ID);
		FacMsg.setValue(factionAdj);
		final Room R=source.location();
		if(R!=null)
		{
			if(R.okMessage(source,FacMsg))
			{
				R.send(source, FacMsg);
				factionAdj=FacMsg.value();
				if((factionAdj!=Integer.MAX_VALUE)&&(factionAdj!=Integer.MIN_VALUE))
				{
					// Now execute the changes on the relation.  We do this AFTER the execution of the first so
					// that any changes from okMessage are incorporated
					for(final Enumeration<String> e=relations.keys();e.hasMoreElements();)
					{
						final String relID=(e.nextElement());
						FacMsg=CMClass.getMsg(source,target,null,CMMsg.MASK_ALWAYS|CMMsg.TYP_FACTIONCHANGE,null,CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,relID);
						FacMsg.setValue((int)Math.round(CMath.mul(factionAdj, relations.get(relID).doubleValue())));
						if(R.okMessage(source,FacMsg))
							R.send(source, FacMsg);
					}
				}
			}
		}
		else
		if((factionAdj==Integer.MAX_VALUE)||(factionAdj==Integer.MIN_VALUE))
			source.removeFaction(ID);
		else
			source.adjustFaction(ID,factionAdj);
	}

	@Override
	public String usageFactorRangeDescription(Ability A)
	{
		final StringBuffer rangeStr=new StringBuffer();
		final HashSet<String> namesAdded=new HashSet<String>();
		for(final FAbilityUsage usage : abilityUsages)
		{
			if((usage.possibleAbilityID()&&usage.abilityFlags().equalsIgnoreCase(A.ID()))
			||(((usage.type()<0)||((A.classificationCode()&Ability.ALL_ACODES)==usage.type()))
				&&((usage.flag()<0)||(CMath.bset(A.flags(),usage.flag())))
				&&((usage.notflag()<0)||(!CMath.bset(A.flags(),usage.notflag())))
				&&((usage.domain()<0)||((A.classificationCode()&Ability.ALL_DOMAINS)==usage.domain()))))
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
		for (final String element : Faction.FactionChangeEvent.MISC_TRIGGERS)
			ALL_TYPES.append(element+", ");
		for (final String element : Ability.ACODE_DESCS)
			ALL_TYPES.append(element+", ");
		for (final String element : Ability.DOMAIN_DESCS)
			ALL_TYPES.append(element+", ");
		for (final String element : Ability.FLAG_DESCS)
			ALL_TYPES.append(element+", ");
		_ALL_TYPES=ALL_TYPES.toString()+" a valid Skill, Spell, Chant, etc. ID.";
		return _ALL_TYPES;
	}

	@Override
	public Faction.FactionChangeEvent createChangeEvent(String key)
	{
		Faction.FactionChangeEvent event;
		if(key==null)
			return null;
		if(key.indexOf(';')<0)
		{
			event=new DefaultFaction.DefaultFactionChangeEvent(this);
			if(!event.setEventID(key))
				return null;
		}
		else
			event=new DefaultFaction.DefaultFactionChangeEvent(this,key);
		abilChangeCache.clear();
		Faction.FactionChangeEvent[] events=changes.get(event.eventID().toUpperCase().trim());
		if(events==null)
			events=new Faction.FactionChangeEvent[0];
		events=Arrays.copyOf(events, events.length+1);
		events[events.length-1]=event;
		changes.put(event.eventID().toUpperCase().trim(), events);
		return event;
	}

	private boolean replaceEvents(String key, Faction.FactionChangeEvent event, boolean strict)
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
			abilChangeCache.clear();
		return done;
	}

	@Override
	public void clearChangeEvents()
	{
		abilChangeCache.clear();
		changes.clear();
	}

	@Override
	public boolean delChangeEvent(Faction.FactionChangeEvent event)
	{
		for(final Enumeration<String> e=changes.keys();e.hasMoreElements();)
		{
			if(replaceEvents(e.nextElement(),event,true))
			{
				abilChangeCache.clear();
				return true;
			}
		}
		for(final Enumeration<String> e=changes.keys();e.hasMoreElements();)
		{
			if(replaceEvents(e.nextElement(),event,false))
			{
				abilChangeCache.clear();
				return true;
			}
		}
		return false;
	}

	public class DefaultFactionChangeEvent implements Faction.FactionChangeEvent
	{
		private String		ID				= "";
		private String		flagCache		= "";
		private int			IDclassFilter	= -1;
		private int			IDflagFilter	= -1;
		private int			IDdomainFilter	= -1;
		private int			direction		= 0;
		private double		factor			= 0.0;
		private String		targetZapperStr	= "";
		private boolean		outsiderTargetOK= false;
		private boolean		selfTargetOK	= false;
		private boolean		just100			= false;
		private Object[]	stateVariables	= new Object[0];
		private String		triggerParms	= "";
		
		private final Faction	myFaction;
		
		private Map<String,String> savedTriggerParms=new Hashtable<String,String>();
		private MaskingLibrary.CompiledZMask compiledTargetZapper=null;
		private MaskingLibrary.CompiledZMask compiledSourceZapper=null;

		@Override
		public String eventID()
		{
			return ID;
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
		public int IDflagFilter()
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
		public void setDirection(int newVal)
		{
			direction = newVal;
		}

		@Override
		public void setFactor(double newVal)
		{
			factor = newVal;
		}

		@Override
		public void setTargetZapper(String newVal)
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
		public String getTriggerParm(String parmName)
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
				return ID+"("+triggerParms.replace(';',',')+");"+CHANGE_DIRECTION_DESCS[direction]+";"+((int)Math.round(factor*100.0))+"%;"+flagCache+";"+targetZapperStr;
			else
				return ID+";"+CHANGE_DIRECTION_DESCS[direction]+";"+((int)Math.round(factor*100.0))+"%;"+flagCache+";"+targetZapperStr;
		}

		public DefaultFactionChangeEvent(Faction F)
		{
			myFaction = F;
		}

		public DefaultFactionChangeEvent(Faction F, String key)
		{
			myFaction=F;
			final List<String> v = CMParms.parseSemicolons(key,false);

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
		public boolean setEventID(String newID)
		{
			IDclassFilter=-1;
			IDflagFilter=-1;
			IDdomainFilter=-1;
			for (final String element : MISC_TRIGGERS)
			{
				if(element.equalsIgnoreCase(newID))
				{
					ID = element;
					return true;
				}
			}
			for(int i=0;i<Ability.ACODE_DESCS.length;i++)
			{
				if(Ability.ACODE_DESCS[i].equalsIgnoreCase(newID))
				{
					IDclassFilter = i;
					ID = newID;
					return true;
				}
			}
			for(int i=0;i<Ability.DOMAIN_DESCS.length;i++)
			{
				if(Ability.DOMAIN_DESCS[i].equalsIgnoreCase(newID))
				{
					IDdomainFilter = i << 5;
					ID = newID;
					return true;
				}
			}
			for(int i=0;i< Ability.FLAG_DESCS.length;i++)
			{
				if(Ability.FLAG_DESCS[i].equalsIgnoreCase(newID))
				{
					IDflagFilter = (int) CMath.pow(2, i);
					ID = newID;
					return true;
				}
			}
			if(CMClass.getAbility(newID)!=null)
			{
				ID = newID;
				return true;
			}
			return false;
		}

		public boolean setDirection(String d)
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
		public void setFlags(String newFlagCache)
		{
			flagCache=newFlagCache.toUpperCase().trim();
			final Vector<String> flags=CMParms.parse(flagCache);
			if(flags.contains("OUTSIDER"))
				outsiderTargetOK=true;
			if(flags.contains("SELFOK"))
				selfTargetOK=true;
			if(flags.contains("JUST100"))
				just100=true;
		}

		@Override
		public boolean applies(MOB source, MOB target)
		{
			if(!CMLib.masking().maskCheck(compiledTargetZapper(),target,false))
				return false;
			if(!CMLib.masking().maskCheck(compiledSourceZapper(),target,false))
				return false;
			return true;
		}

		@Override
		public String triggerParameters()
		{
			return triggerParms;
		}

		@Override
		public void setTriggerParameters(String newVal)
		{
			triggerParms=newVal;
			savedTriggerParms=CMParms.parseEQParms(newVal);
			compiledSourceZapper=null;
		}

		@Override
		public Object stateVariable(int x)
		{
			return ((x >= 0) && (x < stateVariables.length)) ? stateVariables[x] : null;
		}

		@Override
		public void setStateVariable(int x, Object newVal)
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
	public Faction.FRange addRange(String key)
	{
		final Faction.FRange FR=new DefaultFaction.DefaultFactionRange(this,key);
		ranges.put(FR.codeName().toUpperCase().trim(),FR);
		recalc();
		return FR;
	}

	@Override
	public boolean delRange(FRange FR)
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
		public String			Name		= "";
		public String			CodeName	= "";
		public Faction.Align	AlignEquiv;
		public Faction			myFaction	= null;

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
			return Name;
		}

		@Override
		public String codeName()
		{
			return CodeName;
		}

		@Override
		public Align alignEquiv()
		{
			return AlignEquiv;
		}

		@Override
		public Faction getFaction()
		{
			return myFaction;
		}

		@Override
		public void setLow(int newVal)
		{
			low = newVal;
		}

		@Override
		public void setHigh(int newVal)
		{
			high = newVal;
		}

		@Override
		public void setName(String newVal)
		{
			Name = newVal;
		}

		@Override
		public void setAlignEquiv(Faction.Align newVal)
		{
			AlignEquiv = newVal;
		}

		public DefaultFactionRange(Faction F, String key)
		{
			myFaction=F;
			final List<String> v = CMParms.parseSemicolons(key,false);
			Name = v.get(2);
			low = CMath.s_int( v.get(0));
			high = CMath.s_int( v.get(1));
			if(v.size()>3)
				CodeName=v.get(3);
			if(v.size()>4)
				AlignEquiv = CMLib.factions().getAlignEnum(v.get(4));
			else
				AlignEquiv = Faction.Align.INDIFF;
		}

		@Override
		public String toString()
		{
			return low +";"+high+";"+Name+";"+CodeName+";"+AlignEquiv.toString();
		}
		
		@Override
		public int random()
		{
			final Random gen = new Random();
			return high - gen.nextInt(high-low);
		}
		
		@Override
		public int compareTo(FRange o)
		{
			if(low < o.low())
				return -1;
			if(high > o.high())
				return 1;
			return 0;
		}
	}

	@Override
	public Faction.FAbilityUsage addAbilityUsage(String key)
	{
		final Faction.FAbilityUsage usage=
			(key==null)?new DefaultFaction.DefaultFactionAbilityUsage()
					  : new DefaultFaction.DefaultFactionAbilityUsage(key);
		abilityUsages.add(usage);
		abilityUsages.trimToSize();
		return usage;
	}
	
	@Override
	public boolean delAbilityUsage(Faction.FAbilityUsage usage)
	{
		if(!abilityUsages.remove(usage))
			return false;
		abilityUsages.trimToSize();
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

		public DefaultFactionData(Faction F)
		{
			resetFactionData(F);
		}
		
		@Override
		public void resetFactionData(Faction F)
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

		@SuppressWarnings("unchecked")

		@Override
		public void setValue(int newValue)
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
		public void affectPhyStats(Physical affected, PhyStats affectableStats)
		{
			if(!noStatAffectors)
				for(final Ability A : myEffects) A.affectPhyStats(affected, affectableStats);
		}
		
		@Override
		public void affectCharStats(MOB affectedMob, CharStats affectableStats)
		{
			if(!noStatAffectors)
				for(final Ability A : myEffects) A.affectCharStats(affectedMob, affectableStats);
		}
		
		@Override
		public void affectCharState(MOB affectedMob, CharState affectableMaxState)
		{
			if(!noStatAffectors)
				for(final Ability A : myEffects) A.affectCharState(affectedMob, affectableMaxState);
		}
		
		@Override
		public void addHandlers(List<Ability> listeners, List<Behavior> tickers)
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
		private Ability setPresenceReaction(MOB M, Physical myHost)
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
							if(A!=null){ // means yes, we are using light, and yes, heres a reaction to add
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
		public boolean tick(Tickable ticking, int tickID)
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
	}

	public class DefaultFactionZapFactor implements Faction.FZapFactor
	{
		private double gainF= 1.0;
		private double lossF= 1.0;
		private String mask	= "";
		private MaskingLibrary.CompiledZMask compiledMask=null;
		
		public DefaultFactionZapFactor(double gain, double loss, String mask)
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
		public void setGainFactor(double val)
		{
			gainF = val;
		}

		@Override
		public double lossFactor()
		{
			return lossF;
		}

		@Override
		public void setLossFactor(double val)
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
		public void setMOBMask(String str)
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
		public void setReactionObjectID(String str)
		{
			reactionObjectID = str;
		}

		@Override
		public String presentMOBMask()
		{
			return mobMask;
		}

		@Override
		public void setPresentMOBMask(String str)
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
		public void setRangeName(String str)
		{
			rangeName = str.toUpperCase().trim();
		}

		@Override
		public String parameters()
		{
			return parms;
		}

		@Override
		public String parameters(String name)
		{
			return CMStrings.replaceAll(parms, "<TARGET>", name);
		}

		@Override
		public void setParameters(String str)
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

		public DefaultFactionReactionItem(String key)
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
		public int		flag				= -1;
		public int		low					= 0;
		public int		high				= 0;
		public int		notflag				= -1;

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
		public int flag()
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
		public int notflag()
		{
			return notflag;
		}

		@Override
		public void setLow(int newVal)
		{
			low = newVal;
		}

		@Override
		public void setHigh(int newVal)
		{
			high = newVal;
		}

		public DefaultFactionAbilityUsage()
		{
		}

		public DefaultFactionAbilityUsage(String key)
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
		public List<String> setAbilityFlag(String str)
		{
			ID=str;
			final Vector<String> flags=CMParms.parse(ID);
			final Vector<String> unknowns=new Vector<String>();
			possibleAbilityID=false;
			for(int f=0;f<flags.size();f++)
			{
				String strflag=flags.elementAt(f);
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
						notflag=notflag|(int)CMath.pow(2,val);
					}
					else
					{
						if(flag<0)
							flag=0;
						flag=flag|(int)CMath.pow(2,val);
					}
					break;
				default:
					unknowns.addElement(strflag);
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
		factors.clear();
		relations.clear();
		abilityUsages.clear();
		choices.clear();
		reactions.clear();
		reactionHash.clear();
	}

	@Override
	public boolean isSavable()
	{
		return true;
	}

	@Override
	public void setSavable(boolean truefalse)
	{
	}

	@Override
	public boolean amDestroyed()
	{
		return destroyed;
	}
}
