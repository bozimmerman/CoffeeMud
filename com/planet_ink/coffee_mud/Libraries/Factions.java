package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMLib.Library;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Faction.FRange;
import com.planet_ink.coffee_mud.Common.interfaces.Faction.FacTag;
import com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.IOException;
import java.util.*;

/**
 * Portions Copyright (c) 2003 Jeremy Vyska
 * Portions Copyright (c) 2004-2025 Bo Zimmerman
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   	 http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class Factions extends StdLibrary implements FactionManager
{
	@Override
	public String ID()
	{
		return "Factions";
	}

	public SHashtable<String, Faction>		factionMap			= new SHashtable<String, Faction>();
	public SHashtable<String, FRange>		hashedFactionRanges	= new SHashtable<String, FRange>();
	public Map<Faction.Align, List<FRange>>	hashedFactionAligns	= new SHashtable<Faction.Align, List<FRange>>();

	protected List<String>	autoReactions	= null;

	@Override
	public Enumeration<Faction> factions()
	{
		return factionMap.elements();
	}

	@Override
	public int numFactions()
	{
		return factionMap.size();
	}

	@Override
	public void clearFactions()
	{
		factionMap.clear();
		hashedFactionRanges.clear();
		hashedFactionAligns.clear();
		autoReactions = null;
	}

	@Override
	public void propertiesLoaded()
	{
		super.propertiesLoaded();
		autoReactions = null;
	}

	@Override
	public Faction getFactionByNumber(final int index)
	{
		final Enumeration<Faction> fe=factions();
		Faction F=null;
		for(int facIndex=0; (facIndex<=index) && fe.hasMoreElements(); facIndex++)
			F=fe.nextElement();
		return F;
	}

	@Override
	public void reloadFactions(final String factionList)
	{
		final List<String> preLoadFactions=CMParms.parseSemicolons(factionList,true);
		clearFactions();
		for(int i=0;i<preLoadFactions.size();i++)
			getFaction(preLoadFactions.get(i));
	}

	public java.util.Map<String, FRange> rangeCodeNames()
	{
		return hashedFactionRanges;
	}

	@Override
	public boolean isRangeCodeName(final String key)
	{
		return rangeCodeNames().containsKey(key.toUpperCase());
	}

	@Override
	public FRange getFactionRangeByCodeName(final String rangeCodeName)
	{
		if(hashedFactionRanges.containsKey(rangeCodeName.toUpperCase()))
			return hashedFactionRanges.get(rangeCodeName.toUpperCase());
		return null;
	}

	@Override
	public boolean isFactionedThisWay(final MOB mob, final Faction.FRange rangeCode)
	{
		final Faction.FRange FR=rangeCode;
		if(FR==null)
			return false;
		final Faction F=rangeCode.getFaction();
		final Faction.FRange FR2=F.fetchRange(mob.fetchFaction(F.factionID()));
		if(FR2==null)
			return false;
		return FR2.codeName().equalsIgnoreCase(FR.codeName());
	}

	@Override
	public String rangeDescription(final Faction.FRange FR, final String andOr)
	{
		if(FR==null)
			return "";
		final Faction F=FR.getFaction();
		final Vector<FRange> relevantFactions=new Vector<FRange>();
		for(final Enumeration<FRange> e=F.ranges();e.hasMoreElements();)
		{
			final Faction.FRange FR2=e.nextElement();
			if(FR2.codeName().equalsIgnoreCase(FR.codeName()))
				relevantFactions.addElement(FR2);
		}
		if(relevantFactions.size()==0)
			return "";
		if(relevantFactions.size()==1)
			return F.name()+" of "+relevantFactions.firstElement().name();
		final StringBuffer buf=new StringBuffer(F.name()+" of ");
		for(int i=0;i<relevantFactions.size()-1;i++)
			buf.append(relevantFactions.elementAt(i).name()+", ");
		buf.append(andOr+relevantFactions.lastElement().name());
		return buf.toString();
	}

	private Faction buildFactionFromXML(final StringBuffer buf, final String factionID)
	{
		final Faction F=(Faction)CMClass.getCommon("DefaultFaction");
		F.initializeFaction(buf,factionID);
		for(final Enumeration<FRange> e=F.ranges();e.hasMoreElements();)
		{
			final Faction.FRange FR=e.nextElement();
			final String codeName=(FR.codeName().length()>0)?FR.codeName().toUpperCase():FR.name().toUpperCase();
			if(!hashedFactionRanges.containsKey(codeName))
				hashedFactionRanges.put(codeName,FR);
			final String simpleUniqueCodeName = F.name().toUpperCase()+"."+FR.name().toUpperCase();
			if(!hashedFactionRanges.containsKey(simpleUniqueCodeName))
				hashedFactionRanges.put(simpleUniqueCodeName,FR);
			final String uniqueCodeName = simpleUniqueCodeName.replace(' ','_');
			if(!hashedFactionRanges.containsKey(uniqueCodeName))
				hashedFactionRanges.put(uniqueCodeName,FR);
			final String simpleUniqueIDName = F.factionID().toUpperCase()+"."+FR.name().toUpperCase();
			if(!hashedFactionRanges.containsKey(simpleUniqueIDName))
				hashedFactionRanges.put(simpleUniqueIDName,FR);
			if(FR.alignEquiv() != null)
			{
				if(!hashedFactionAligns.containsKey(FR.alignEquiv()))
					hashedFactionAligns.put(FR.alignEquiv(), new ArrayList<FRange>());
				hashedFactionAligns.get(FR.alignEquiv()).add(FR);
			}
		}
		return addFaction(F) ? F : null;
	}

	@Override
	public boolean addFaction(final Faction F)
	{
		if(F!=null)
		{
			F.disable(CMSecurity.isFactionDisabled(F.factionID().toUpperCase().trim()));
			factionMap.put(F.factionID().toUpperCase().trim(),F);
			return !F.isDisabled();
		}
		return false;
	}

	@Override
	public String makeFactionFilename(final String factionID)
	{
		String filename;
		if(factionID.endsWith(".ini"))
			filename="factions/"+factionID;
		else
			filename="factions/"+factionID+".ini";
		if(new CMFile(Resources.makeFileResourceName(filename),null).exists())
			return filename;
		filename="factions/"+factionID;
		if(new CMFile(Resources.makeFileResourceName(filename),null).exists())
			return filename;
		filename=factionID+".ini";
		if(new CMFile(Resources.makeFileResourceName(filename),null).exists())
			return filename;
		if(new CMFile(Resources.makeFileResourceName(factionID),null).exists())
			return factionID;
		filename=factionID;
		if(filename.indexOf('/')<0)
			filename="factions/"+factionID;
		if(!filename.toLowerCase().endsWith(".ini"))
			filename=filename+".ini";
		return filename;
	}

	@Override
	public boolean isFactionID(String key)
	{
		if(key==null)
			return false;
		key=key.toUpperCase().trim();
		if(factionMap.containsKey(key))
			return true;
		final CMFile f=new CMFile(Resources.makeFileResourceName(makeFactionFilename(key)),null,CMFile.FLAG_LOGERRORS);
		if(f.exists())
			return getFaction(key)!=null;
		return false;
	}

	@Override
	public boolean isFactionLoaded(String key)
	{
		if(key==null)
			return false;
		key=key.toUpperCase().trim();
		if(factionMap.containsKey(key))
			return true;
		if((!key.endsWith(".INI"))
		&&(factionMap.containsKey(key+".INI")))
			return true;
		return false;
	}

	@Override
	public boolean isAlignmentLoaded(final Faction.Align align)
	{
		if(align==null)
			return false;
		return hashedFactionAligns.containsKey(align)
				&& (hashedFactionAligns.get(align).size()>0);
	}

	@Override
	public Faction getFaction(final String factionID)
	{
		if(factionID==null)
			return null;
		Faction F=factionMap.get(factionID.toUpperCase());
		if(F==null)
		{
			if(!factionID.toLowerCase().endsWith(".ini"))
				F=getFaction(factionID+".ini");
		}
		if(F!=null)
		{
			if(F.isDisabled())
				return null;
			if(!F.amDestroyed())
				return F;
			factionMap.remove(F.factionID().toUpperCase());
			Resources.removeResource(F.factionID());
			return null;
		}
		if(CMSecurity.isFactionDisabled(factionID))
			return null;
		final CMFile f=new CMFile(Resources.makeFileResourceName(makeFactionFilename(factionID)),null,CMFile.FLAG_LOGERRORS);
		if(!f.exists())
			return null;
		final StringBuffer buf=f.text();
		if((buf!=null)&&(buf.length()>0))
		{
			final Faction newF=buildFactionFromXML(buf, factionID);
			if(newF != null)
				factionMap.put(factionID.toUpperCase(), newF);
			return newF;
		}
		return null;
	}

	@Override
	public Faction getFactionByRangeCodeName(final String rangeCodeName)
	{
		if(hashedFactionRanges.containsKey(rangeCodeName.toUpperCase()))
		{
			final Faction.FRange FR=hashedFactionRanges.get(rangeCodeName.toUpperCase());
			if(FR!=null)
			{
				final Faction F=FR.getFaction();
				if(!F.amDestroyed())
					return F;
				factionMap.remove(F.factionID().toUpperCase());
				Resources.removeResource(F.factionID());
				return null;
			}
		}
		return null;
	}

	@Override
	public Faction getFactionByName(final String factionNamed)
	{
		Faction F;
		for(final Enumeration<String> e=factionMap.keys();e.hasMoreElements();)
		{
			F=factionMap.get(e.nextElement());
			if(F.name().equalsIgnoreCase(factionNamed))
			{
				if(!F.amDestroyed())
					return F;
				factionMap.remove(F.factionID().toUpperCase());
				Resources.removeResource(F.factionID());
				return null;
			}
		}
		return null;
	}

	@Override
	public boolean removeFaction(final String factionID)
	{
		Faction F;
		if(factionID==null)
		{
			for(final Enumeration<Faction> e=factionMap.elements();e.hasMoreElements();)
			{
				F=e.nextElement();
				if(F!=null)
					removeFaction(F.factionID());
			}
			return true;
		}
		F=getFactionByName(factionID);
		if(F==null)
			F=getFaction(factionID);
		if(F==null)
			return false;
		Resources.removeResource(F.factionID());
		factionMap.remove(F.factionID().toUpperCase());
		return true;
	}

	@Override
	public String listFactions()
	{
		final StringBuffer msg=new StringBuffer();
		msg.append("\n\r^.^N");
		msg.append("+--------------------------------+-----------------------------------------+\n\r");
		msg.append(L("| ^HFaction Name^N                   | ^HFaction INI Source File (Faction ID)^N    |\n\r"));
		msg.append("+--------------------------------+-----------------------------------------+\n\r");
		final XVector<Faction> sorted = new XVector<Faction>(factionMap.elements());
		Collections.sort(sorted,new Comparator<Faction>()
		{
			@Override
			public int compare(final Faction o1, final Faction o2)
			{
				return o1.name().compareTo(o2.name());
			}

		});
		for(final Enumeration<Faction> e=sorted.elements();e.hasMoreElements();)
		{
			final Faction f=e.nextElement();
			msg.append("| ");
			msg.append(CMStrings.padRight(f.name(),30));
			msg.append(" | ");
			msg.append(CMStrings.padRight(f.factionID(),39));
			msg.append(" |\n\r");
		}
		msg.append("+--------------------------------+-----------------------------------------+\n\r");
		msg.append("\n\r");
		return msg.toString();
	}

	@Override
	public String name()
	{
		return "Factions";
	}

	@Override
	public int getTickStatus()
	{
		return Tickable.STATUS_NOT;
	}

	@Override
	public String getName(final String factionID)
	{
		final Faction f = getFaction(factionID);
		if (f != null)
			return f.name();
		return "";
	}

	@Override
	public int getMinimum(final String factionID)
	{
		final Faction f = getFaction(factionID);
		if (f != null)
			return f.minimum();
		return 0;
	}

	@Override
	public int getMaximum(final String factionID)
	{
		final Faction f = getFaction(factionID);
		if (f != null)
			return f.maximum();
		return 0;
	}

	@Override
	public int getPercent(final String factionID, final int faction)
	{
		final Faction f = getFaction(factionID);
		if (f != null)
			return f.asPercent(faction);
		return 0;
	}

	@Override
	public int getPercentFromAvg(final String factionID, final int faction)
	{
		final Faction f = getFaction(factionID);
		if (f != null)
			return f.asPercentFromAvg(faction);
		return 0;
	}

	@Override
	public Faction.FRange getRange(final String factionID, final int faction)
	{
		final Faction f = getFaction(factionID);
		if (f != null)
			return f.fetchRange(faction);
		return null;
	}

	@Override
	public Enumeration<Faction.FRange> getRanges(final String factionID)
	{
		final Faction f=getFaction(factionID);
		if(f!=null)
			return f.ranges();
		return null;
	}

	@Override
	public double getRangePercent(final String factionID, final int faction)
	{
		final Faction F=getFaction(factionID);
		if(F==null)
			return 0.0;
		return CMath.div((int)Math.round(CMath.div((faction - F.minimum()),(F.maximum() - F.minimum())) * 10000.0),100.0);
	}

	@Override
	public int getTotal(final String factionID)
	{
		final Faction f = getFaction(factionID);
		if (f != null)
			return (f.maximum() - f.minimum());
		return 0;
	}

	@Override
	public int getRandom(final String factionID)
	{
		final Faction f = getFaction(factionID);
		if (f != null)
			return f.randomFaction();
		return 0;
	}

	@Override
	public String getAlignmentID()
	{
		return "alignment.ini";
	}

	@Override
	public String getInclinationID()
	{
		return "inclination.ini";
	}

	@Override
	public void setAlignment(final MOB mob, final Faction.Align newAlignment)
	{
		if(factionMap.containsKey(this.getAlignmentID().toUpperCase()))
			mob.addFaction(getAlignmentID(),getAlignMedianFacValue(newAlignment));
	}

	@Override
	public void setAlignmentOldRange(final MOB mob, final int oldRange)
	{
		if(factionMap.containsKey(this.getAlignmentID().toUpperCase()))
		{
			if(oldRange>=650)
				setAlignment(mob,Faction.Align.GOOD);
			else
			if(oldRange>=350)
				setAlignment(mob,Faction.Align.NEUTRAL);
			else
			if(oldRange>=0)
				setAlignment(mob,Faction.Align.EVIL);
			else{ /* a -1 value is the new norm */}
		}
	}

	@Override
	public boolean postChangeAllFactions(final MOB mob, final MOB victim, final int amount, final boolean quiet)
	{
		if((mob==null))
			return false;
		final CMMsg msg=CMClass.getMsg(mob,victim,null,CMMsg.MASK_ALWAYS|CMMsg.TYP_FACTIONCHANGE,null,CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,""+quiet);
		msg.setValue(amount);
		final Room R=mob.location();
		if(R!=null)
		{
			if(R.okMessage(mob,msg))
				R.send(mob,msg);
			else
				return false;
		}
		return true;
	}

	@Override
	public boolean postSkillFactionChange(final MOB mob,final Ability skillA, final String factionID, final int amount)
	{
		if((mob==null))
			return false;
		final CMMsg msg=CMClass.getMsg(mob,null,skillA,CMMsg.MASK_ALWAYS|CMMsg.TYP_FACTIONCHANGE,null,CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,factionID);
		msg.setValue(amount);
		final Room R=mob.location();
		if(R!=null)
		{
			if(R.okMessage(mob,msg))
				R.send(mob,msg);
			else
				return false;
		}
		return true;
	}

	@Override
	public Faction makeReactionFaction(final String prefix, final String classID, final String Name, final String code, final String baseTemplateFilename)
	{
		final String codedName=Name.toUpperCase().trim().replace(' ','_');
		final String factionID=prefix+codedName;
		Faction templateF=getFaction(this.makeFactionFilename(codedName.toLowerCase()));
		if(templateF==null)
			templateF=getFaction(baseTemplateFilename);
		if(templateF==null)
		{
			Log.errOut("Factions","Could not find base template '"+baseTemplateFilename+"'");
			return null;
		}
		StringBuffer buf = rebuildFactionProperties(templateF);
		factionMap.remove(templateF.factionID().toUpperCase().trim());
		String bufStr = buf.toString();
		bufStr = CMStrings.replaceAll(bufStr,"<CODE>",code);
		bufStr = CMStrings.replaceAll(bufStr,"<NAME>",Name);
		bufStr = CMStrings.replaceAll(bufStr,"<FACTIONID>",factionID);
		bufStr = CMStrings.replaceAll(bufStr,"<CLASSID>",classID);
		buf=new StringBuffer(bufStr);
		final Faction F=buildFactionFromXML(buf, factionID);
		F.setInternalFlags(F.getInternalFlags() | Faction.IFLAG_IGNOREAUTO);
		F.setInternalFlags(F.getInternalFlags() | Faction.IFLAG_NEVERSAVE);
		F.setInternalFlags(F.getInternalFlags() | Faction.IFLAG_CUSTOMTICK);
		return addFaction(F) ? F : null;
	}

	@Override
	public Faction getSpecialAreaFaction(final Area A)
	{
		if((A!=null)
		&&(!CMath.bset(A.flags(), Area.FLAG_INSTANCE_CHILD))
		&&(!(A instanceof Boardable)))
		{
			final String areaCode = A.Name().toUpperCase().trim().replace(' ','_');
			Faction F=getFaction("AREA_"+areaCode);
			if(F==null)
				F=makeReactionFaction("AREA_",A.ID(),A.Name(),areaCode,"examples/areareaction.ini");
			return F;
		}
		return null;
	}

	@Override
	public Faction[] getSpecialFactions(final MOB mob, final Room R)
	{
		if((mob==null)||(R==null))
			return null;
		Faction F=null;
		final String autoReactionTypeStr=CMProps.getVar(CMProps.Str.AUTOREACTION).toUpperCase().trim();
		if((autoReactionTypeStr!=null)
		&&(autoReactionTypeStr.length()>0))
		{
			if(autoReactions == null)
			{
				autoReactions = CMParms.parseCommas(autoReactionTypeStr,true);
				if(autoReactions.contains("PLANAR"))
				{
					final Ability A=CMClass.getAbility("StdPlanarAbility");
					if(A!=null)
					{
						try
						{
							A.setMiscText("Doesn't Exist"); // initialize the planes list
						}
						catch(final Exception e)
						{
						}
					}
				}
			}
			final List<Faction> Fs=new ArrayList<Faction>(1);
			for(final String autoType : autoReactions)
			{
				if(autoType.equals("AREA"))
				{
					final Area A=R.getArea();
					F=getSpecialAreaFaction(A);
					if(F!=null)
						Fs.add(F);
				}
				else
				if(autoType.equals("NAME"))
				{
					for(int i=0;i<R.numInhabitants();i++)
					{
						final MOB M=R.fetchInhabitant(i);
						if((M!=null)&&(M!=mob)&&(M.isMonster()))
						{
							final String nameCode = M.Name().toUpperCase().trim().replace(' ','_');
							F=getFaction("NAME_"+nameCode);
							if(F==null)
								F=makeReactionFaction("NAME_",M.ID(),M.Name(),nameCode,"examples/namereaction.ini");
							if(F!=null)
								Fs.add(F);
						}
					}
				}
				else
				if(autoType.equals("RACE"))
				{
					final Set<Race> done=new HashSet<Race>(2);
					for(int i=0;i<R.numInhabitants();i++)
					{
						final MOB M=R.fetchInhabitant(i);
						if((M!=null)&&(M!=mob)&&(M.isMonster())&&(!done.contains(M.charStats().getMyRace())))
						{
							final Race rR=M.charStats().getMyRace();
							done.add(rR);
							final String nameCode = rR.name().toUpperCase().trim().replace(' ','_');
							F=getFaction("RACE_"+nameCode);
							if(F==null)
								F=makeReactionFaction("RACE_",rR.ID(),rR.name(),nameCode,"examples/racereaction.ini");
							if(F!=null)
								Fs.add(F);
						}
					}
				}
				else
				if(autoType.equals("PLANAR"))
				{
					final Area A=R.getArea();
					if(A!=null)
					{
						final String planeName = CMLib.flags().getPlaneOfExistence(A);
						if(planeName != null)
						{
							F=getFaction("PLANE_"+planeName.toUpperCase().trim());
							if(F!=null)
								Fs.add(F);
						}
					}
				}
				else
				if(autoType.equals("DEITY"))
				{
					final Deity D=(mob instanceof Deity)?(Deity)mob:mob.baseCharStats().getMyDeity();
					if(D!=null)
					{
						final String nameCode = D.Name().toUpperCase().trim().replace(' ', '_');
						F=getFaction("DEITY_"+nameCode);
						if(F==null)
							F=makeReactionFaction("DEITY_",D.ID(),D.Name(),nameCode,"examples/deityreaction.ini");
						if(mob==D)
						{
							for(final Enumeration<Deity> d= CMLib.map().deities(); d.hasMoreElements();)
							{
								final Deity D2=d.nextElement();
								if(D2!=D)
								{
									final String nameCode2 = D2.Name().toUpperCase().trim().replace(' ', '_');
									final Faction F2=getFaction("DEITY_"+nameCode2);
									if(F2!=null)
										Fs.add(F2);
								}
							}
						}
						if(F!=null)
							Fs.add(F);
					}
				}
			}
			if(Fs.size()==0)
				return null;
			return Fs.toArray(new Faction[Fs.size()]);
		}
		return null;
	}

	@Override
	public void updatePlayerFactions(final MOB mob, final Room R, final boolean forceAutoCheck)
	{
		if((mob==null)
		||(R==null))
			return;
		else
		{
			Faction F=null;
			for(final Enumeration<Faction> e=factions();e.hasMoreElements();)
			{
				F=e.nextElement();
				if(F!=null)
				{
					if(((forceAutoCheck || (F.getInternalFlags()&Faction.IFLAG_IGNOREAUTO)==0))
					&&(!F.hasFaction(mob))
					&&(F.findAutoDefault(mob)!=Integer.MAX_VALUE))
						mob.addFaction(F.factionID(),F.findAutoDefault(mob));
				}
			}
		}
		final Faction[] Fs=getSpecialFactions(mob,R);
		if(Fs!=null)
		{
			for(final Faction F : Fs)
			{
				if((F!=null)
				&&(!F.hasFaction(mob))
				&&(F.findAutoDefault(mob)!=Integer.MAX_VALUE))
					mob.addFaction(F.factionID(), F.findAutoDefault(mob));
			}
		}
	}

	protected void addOutsidersAndTimers(final Faction F, final List<Faction.FactionChangeEvent> outSiders, final List<Faction.FactionChangeEvent> timers)
	{
		Faction.FactionChangeEvent[] CEs=null;
		CEs=F.getChangeEvents("ADDOUTSIDER");
		if(CEs!=null)
		{
			for (final FactionChangeEvent ce : CEs)
				outSiders.add(ce);
		}
		CEs=F.getChangeEvents("TIME");
		if(CEs!=null)
		{
			for (final FactionChangeEvent ce : CEs)
			{
				if(ce.triggerParameters().length()==0)
					timers.add(ce);
				else
				{
					int[] ctr=(int[])ce.stateVariable(0);
					if(ctr==null)
					{
						ctr=new int[]{CMath.s_int(ce.getTriggerParm("ROUNDS"))};
						ce.setStateVariable(0,ctr);
					}
					if((--ctr[0])<=0)
					{
						ctr[0]=CMath.s_int(ce.getTriggerParm("ROUNDS"));
						timers.add(ce);
					}
				}
			}
		}
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!CMLib.sessions().sessions().hasNext())
			return true;
		try
		{
			MOB mob=null;
			Faction F=null;
			Faction.FactionChangeEvent CE=null;
			final List<Faction.FactionChangeEvent> outSiders=new ArrayList<Faction.FactionChangeEvent>();
			final List<Faction.FactionChangeEvent> timers=new ArrayList<Faction.FactionChangeEvent>();
			final Set<Faction> factionsDone=new HashSet<Faction>();
			for(final Enumeration<Faction> e=factionMap.elements();e.hasMoreElements();)
			{
				F=e.nextElement();
				if((F.getInternalFlags()&Faction.IFLAG_CUSTOMTICK)==0)
				{
					addOutsidersAndTimers(F, outSiders, timers);
					factionsDone.add(F);
				}
			}
			Room R;
			Faction[] Fs;
			for(final Session S : CMLib.sessions().allIterable())
			{
				mob=(!S.isStopped())?S.mob():null;
				R=(mob==null)?null:mob.location();
				if(R!=null)
				{
					Fs=getSpecialFactions(mob, R);
					if(Fs!=null)
					{
						for(final Faction sF : Fs)
						{
							if(!factionsDone.contains(sF))
							{
								addOutsidersAndTimers(sF, outSiders, timers);
								factionsDone.add(sF);
							}
						}
					}
					for(int o=0;o<outSiders.size();o++)
					{
						CE=outSiders.get(o);
						if((CE.applies(mob,mob))
						&&(!CE.getFaction().hasFaction(mob)))
							CE.getFaction().executeChange(mob,mob,CE);
					}
					for(int o=0;o<timers.size();o++)
					{
						CE=timers.get(o);
						if((CE.applies(mob,mob))
						&&(CE.getFaction().hasFaction(mob)))
							CE.getFaction().executeChange(mob,mob,CE);
					}
				}
			}
		}
		catch (final Exception e)
		{
			Log.errOut("Factions", e);
		}
		return true;
	}

	@Override
	public int getAlignPurity(final int faction, final Faction.Align eq)
	{
		if(eq==null)
			return 0;
		if(eq.isInclination)
			return getInclinationPurity(faction,eq);
		if(!factionMap.containsKey(this.getAlignmentID().toUpperCase()))
			return 0;
		int bottom=Integer.MAX_VALUE;
		int top=Integer.MIN_VALUE;
		final int pct=getPercent(getAlignmentID(),faction);
		final Enumeration<FRange> e = getRanges(getAlignmentID());
		if(e!=null)
		{
			for(;e.hasMoreElements();)
			{
				final Faction.FRange R=e.nextElement();
				if(R.alignEquiv()==eq)
				{
					if(R.low()<bottom)
						bottom=R.low();
					if(R.high()>top)
						top=R.high();
				}
			}
		}
		switch(eq)
		{
			case GOOD:
				return Math.abs(pct - getPercent(getAlignmentID(),top));
			case EVIL:
				return Math.abs(getPercent(getAlignmentID(),bottom) - pct);
			case NEUTRAL:
				return Math.abs(getPercent(getAlignmentID(),(int)Math.round(CMath.div((top+bottom),2))) - pct);
			default:
				return 0;
		}
	}

	@Override
	public int getInclinationPurity(final int faction, final Faction.Align eq)
	{
		if(eq == null)
			return 0;
		if(!eq.isInclination)
			return getAlignPurity(faction,eq);
		if(!factionMap.containsKey(this.getInclinationID().toUpperCase()))
			return 0;
		int bottom=Integer.MAX_VALUE;
		int top=Integer.MIN_VALUE;
		final int pct=getPercent(getInclinationID(),faction);
		final Enumeration<FRange> e = getRanges(getInclinationID());
		if(e!=null)
		for(;e.hasMoreElements();)
		{
			final Faction.FRange R=e.nextElement();
			if(R.alignEquiv()==eq)
			{
				if(R.low()<bottom)
					bottom=R.low();
				if(R.high()>top)
					top=R.high();
			}
		}
		switch(eq)
		{
			case LAWFUL:
				return Math.abs(pct - getPercent(getInclinationID(),top));
			case CHAOTIC:
				return Math.abs(getPercent(getInclinationID(),bottom) - pct);
			case INDIFF:
				return Math.abs(getPercent(getInclinationID(),(int)Math.round(CMath.div((top+bottom),2))) - pct);
			default:
				return 0;
		}
	}

	// Please don't mock the name, I couldn't think of a better one.  Sadly.
	@Override
	public int getAlignMedianFacValue(final Faction.Align eq)
	{
		if(eq==null)
			return 0;
		final String aid = eq.isInclination?getInclinationID():getAlignmentID();
		if(!factionMap.containsKey(aid.toUpperCase().trim()))
			return 0;
		int bottom=Integer.MAX_VALUE;
		int top=Integer.MIN_VALUE;
		final Enumeration<FRange> e = getRanges(aid);
		if(e==null)
			return 0;
		for(;e.hasMoreElements();)
		{
			final Faction.FRange R=e.nextElement();
			if(R.alignEquiv()==eq)
			{
				if(R.low()<bottom)
					bottom=R.low();
				if(R.high()>top)
					top=R.high();
			}
		}
		switch(eq)
		{
			case GOOD:
			case LAWFUL:
				return top;
			case EVIL:
			case CHAOTIC:
				return bottom;
			case NEUTRAL:
			case MODERATE:
				return (int)Math.round(CMath.div((top+bottom),2));
			default:
				return 0;
		}
	}

	@Override
	public Faction.FacTag getFactionTag(String tag)
	{
		if(tag == null)
			return null;
		tag=tag.toUpperCase().trim();
		try
		{
			return Faction.FacTag.valueOf(tag);
		}
		catch(final Exception e) {}
		for(final Faction.FacTag t : Faction.FacTag.values())
		{
			if((t.maskPrefix != null)
			&&(tag.startsWith(t.maskPrefix)))
				return t;
		}
		return null;
	}

	@Override
	public Faction.Align getAlignEnum(final String str)
	{
		final Faction.Align A=(Faction.Align)CMath.s_valueOf(Faction.Align.class, str.toUpperCase().trim());
		if(A!=null)
			return A;
		return  Faction.Align.INDIFF;
	}

	private String getWordAffOrBehav(final String ID)
	{
		if(CMClass.getBehavior(ID)!=null)
			return "behavior";
		if(CMClass.getAbility(ID)!=null)
			return "ability";
		if(CMClass.getCommand(ID)!=null)
			return "command";
		return null;
	}

	@Override
	public void modifyFaction(final MOB mob, final Faction meF) throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		int showFlag=-1;
		if(CMProps.getIntVar(CMProps.Int.EDITORTYPE)>0)
			showFlag=-999;
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(!ok))
		{
			int showNumber=0;
			// name
			meF.setName(CMLib.genEd().prompt(mob,meF.name(),++showNumber,showFlag,L("Name")));

			// ranges
			++showNumber;
			if(!meF.ranges().hasMoreElements())
				meF.addRange("0;100;Sample Range;SAMPLE;");
			while((mob.session()!=null)&&(!mob.session().isStopped())&&(!((showFlag>0)&&(showFlag!=showNumber))))
			{
				final StringBuffer list=new StringBuffer(showNumber+". Faction Division/Ranges List:\n\r");
				list.append(CMStrings.padRight(L("   Code"),16)+CMStrings.padRight(L("Name"),21)+CMStrings.padRight(L("Min"),11)+CMStrings.padRight(L("Max"),11)+CMStrings.padRight(L("Align"),6)+"\n\r");
				for(final Enumeration<FRange> e=meF.ranges();e.hasMoreElements();)
				{
					final Faction.FRange FR=e.nextElement();
					list.append(CMStrings.padRight("   "+FR.codeName(),15)+" ");
					list.append(CMStrings.padRight(FR.name(),20)+" ");
					list.append(CMStrings.padRight(""+FR.low(),10)+" ");
					list.append(CMStrings.padRight(""+FR.high(),10)+" ");
					list.append(CMStrings.padRight(FR.alignEquiv().toString(),5)+"\n\r");
				}
				mob.tell(list.toString());
				if((showFlag!=showNumber)&&(showFlag>-999))
					break;
				String which=mob.session().prompt(L("Enter a CODE to add, remove, or modify:"),"");
				if(which.length()==0)
					break;
				which=which.trim().toUpperCase();
				if(which.indexOf(' ')>=0)
				{
					mob.tell(L("Faction Range code names may not contain spaces."));
					break;
				}
				Faction.FRange FR=meF.fetchRange(which);
				if(FR==null)
				{
					if(mob.session().confirm(L("Create a new range code named '@x1' (y/N): ",which),"N"))
					{
						FR=meF.addRange("0;100;Change My Name;"+which+";");
					}
				}
				else
				if(mob.session().choose(L("Would you like to M)odify or D)elete this range (M/d): "),"MD","M").toUpperCase().startsWith("D"))
				{
					meF.delRange(FR);
					mob.tell(L("Range deleted."));
					FR=null;
				}
				if(FR!=null)
				{
					String newName=mob.session().prompt(L("Enter a new name (@x1)\n\r: ",FR.name()),FR.name());
					boolean error99=false;
					if(newName.length()==0)
						error99=true;
					else
					for(final Enumeration<FRange> e=meF.ranges();e.hasMoreElements();)
					{
						final Faction.FRange FR3=e.nextElement();
						if(FR3.name().equalsIgnoreCase(FR.name())&&(FR3!=FR))
						{
							mob.tell(L("A range already exists with that name!"));
							error99=true;
							break;
						}
					}
					if(error99)
						mob.tell(L("(no change)"));
					else
						FR.setName(newName);
					newName=mob.session().prompt(L("Enter the low end of the range (@x1)\n\r: ",""+FR.low()),""+FR.low());
					if(!CMath.isInteger(newName))
						mob.tell(L("(no change)"));
					else
						FR.setLow(CMath.s_int(newName));
					newName=mob.session().prompt(L("Enter the high end of the range (@x1)\n\r: ",""+FR.high()),""+FR.high());
					if((!CMath.isInteger(newName))||(CMath.s_int(newName)<FR.low()))
						mob.tell(L("(no change)"));
					else
						FR.setHigh(CMath.s_int(newName));
					final StringBuffer prompt=new StringBuffer("Select the 'virtue' (if any) of this range:\n\r");
					final StringBuffer choices=new StringBuffer("");
					for(final Faction.Align i : Faction.Align.values())
					{
						choices.append(""+i.ordinal());
						if(i==Faction.Align.INDIFF)
							prompt.append(i.ordinal()+") Not applicable\n\r");
						else
							prompt.append(i.ordinal()+") "+i.toString().toLowerCase()+"\n\r");
					}
					FR.setAlignEquiv(Faction.Align.values()[CMath.s_int(mob.session().choose(L("@x1Enter alignment equivalency or 0: ",prompt.toString()),choices.toString(),""+FR.alignEquiv().ordinal()))]);
				}
			}

			// show in score
			meF.setShowInScore(CMLib.genEd().prompt(mob,meF.showInScore(),++showNumber,showFlag,L("Show in 'Score'")));

			// show in factions
			meF.setShowInFactionsCommand(CMLib.genEd().prompt(mob,meF.showInFactionsCommand(),++showNumber,showFlag,L("Show in 'Factions' command")));

			// show in special reports
			boolean alreadyReporter=false;
			for(final Enumeration<Faction> e=CMLib.factions().factions();e.hasMoreElements();)
			{
				final Faction F2=e.nextElement();
				if(F2.showInSpecialReported())
					alreadyReporter=true;
			}
			if(!alreadyReporter)
				meF.setShowInSpecialReported(CMLib.genEd().prompt(mob,meF.showInSpecialReported(),++showNumber,showFlag,L("Show in Reports")));

			// show in editor
			meF.setShowInEditor(CMLib.genEd().prompt(mob,meF.showInEditor(),++showNumber,showFlag,L("Show in MOB Editor")));

			// isinherited
			meF.setInherited(CMLib.genEd().prompt(mob,meF.isInheritable(),++showNumber,showFlag,L("Is inherited by children")));

			// auto defaults
			boolean error=true;
			meF.setAutoDefaults(CMParms.parseSemicolons(CMLib.genEd().prompt(mob,CMParms.toSemicolonListString(meF.autoDefaults()),++showNumber,showFlag,L("Optional automatic assigned values with zapper masks (semicolon delimited).\n\r    ")),true));

			// non-auto defaults
			error=true;
			if(!meF.defaults().hasMoreElements())
				meF.setDefaults(new XVector<String>("0"));
			++showNumber;
			while(error&&(mob.session()!=null)&&(!mob.session().isStopped()))
			{
				error=false;
				final String newDefaults=CMLib.genEd().prompt(mob,CMParms.toSemicolonListString(meF.defaults()),showNumber,showFlag,L("Other default values with zapper masks (semicolon delimited).\n\r    "));
				if((showFlag!=showNumber)&&(showFlag>-999))
					break;
				final List<String> V=CMParms.parseSemicolons(newDefaults,true);
				if(V.size()==0)
				{
					mob.tell(L("This field may not be empty."));
					error=true;
				}
				meF.setDefaults(CMParms.parseSemicolons(newDefaults,true));
			}

			// choices and choice intro
			meF.setChoices(CMParms.parseSemicolons(CMLib.genEd().prompt(mob,CMParms.toSemicolonListString(meF.choices()),++showNumber,showFlag,L("Optional new player value choices (semicolon-delimited).\n\r    ")),true));
			if(meF.choices().hasMoreElements())
				meF.setChoiceIntro(CMLib.genEd().prompt(mob,meF.choiceIntro(),++showNumber,showFlag,L("Optional choices introduction text. Filename")));

			// rate modifier
			final String newModifier=CMLib.genEd().prompt(mob,CMath.toPct(meF.rateModifier()),++showNumber,showFlag,L("Rate modifier"));
			if((CMath.isNumber(newModifier))||(CMath.isPct(newModifier)))
				meF.setRateModifier(CMath.s_pct(newModifier));

			// experience flag
			boolean error2=true;
			++showNumber;
			while(error2&&(mob.session()!=null)&&(!mob.session().isStopped())&&(!((showFlag>0)&&(showFlag!=showNumber))))
			{
				error2=false;
				final StringBuffer nextPrompt=new StringBuffer("\n\r");
				int myval=-1;
				for(int i=0;i<Faction.EXPAFFECT_NAMES.length;i++)
				{
					if(meF.experienceFlag().equalsIgnoreCase(Faction.EXPAFFECT_NAMES[i]))
						myval=i;
					nextPrompt.append("  "+(i+1)+") "+CMStrings.capitalizeAndLower(Faction.EXPAFFECT_NAMES[i].toLowerCase())+"\n\r");
				}
				if(myval<0)
				{
					meF.setExperienceFlag("NONE");
					myval=0;
				}
				if((showFlag!=showNumber)&&(showFlag>-999))
				{
					mob.tell(L("@x1. Affect on experience: @x2",""+showNumber,Faction.EXPAFFECT_NAMES[myval]));
					break;
				}
				final String prompt="Affect on experience:  "+Faction.EXPAFFECT_NAMES[myval]+nextPrompt.toString()+"\n\rSelect a value: ";
				final int mynewval=CMLib.genEd().prompt(mob,myval+1,showNumber,showFlag,prompt);
				if((showFlag!=showNumber)&&(showFlag>-999))
					break;
				if((mynewval<=0)||(mynewval>Faction.EXPAFFECT_NAMES.length))
				{
					mob.tell(L("That value is not valid."));
					error2=true;
				}
				else
					meF.setExperienceFlag(Faction.EXPAFFECT_NAMES[mynewval-1]);
			}

			// factors by mask
			++showNumber;
			while((mob.session()!=null)&&(!mob.session().isStopped())&&(!((showFlag>0)&&(showFlag!=showNumber))))
			{
				final StringBuffer list=new StringBuffer(showNumber+". Faction change adjustment Factors with Zapper Masks:\n\r");
				list.append("    #) "+CMStrings.padRight(L("Zapper Mask"),31)+CMStrings.padRight(L("Gain"),6)+CMStrings.padRight(L("Loss"),6)+"\n\r");
				final StringBuffer choices=new StringBuffer("");
				int numFactors=0;
				for(final Enumeration<Faction.FZapFactor> e=meF.factors();e.hasMoreElements();)
				{
					final Faction.FZapFactor factor=e.nextElement();
					choices.append(((char)('A'+numFactors)));
					list.append("    "+(((char)('A'+numFactors))+") "));
					list.append(CMStrings.padRight(factor.MOBMask(),30)+" ");
					list.append(CMStrings.padRight(""+CMath.toPct(factor.gainFactor()),5)+" ");
					list.append(CMStrings.padRight(""+CMath.toPct(factor.lossFactor()),5)+"\n\r");
					numFactors++;
				}
				mob.tell(list.toString());
				if((showFlag!=showNumber)&&(showFlag>-999))
					break;
				final String which=mob.session().choose(L("Enter a # to remove, or modify, or enter 0 to Add:"),"0"+choices.toString(),"").trim().toUpperCase();
				final int factorNum=choices.toString().indexOf(which);
				if((which.length()!=1)
				||((!which.equalsIgnoreCase("0"))
					&&((factorNum<0)||(factorNum>=numFactors))))
					break;
				Faction.FZapFactor factor=null;
				if(!which.equalsIgnoreCase("0"))
				{
					factor=meF.getFactor(factorNum);
					if(factor!=null)
					{
						if(mob.session().choose(L("Would you like to M)odify or D)elete this range (M/d): "),"MD","M").toUpperCase().startsWith("D"))
						{
							meF.delFactor(factor);
							mob.tell(L("Factor deleted."));
							factor=null;
						}
					}
				}
				else
					factor=meF.addFactor(1.0,1.0,"");
				if(factor!=null)
				{
					final String mask=mob.session().prompt(L("Enter a new zapper mask (@x1)\n\r: ",factor.MOBMask()),factor.MOBMask());
					double newHigh=factor.gainFactor();
					String newName=mob.session().prompt(L("Enter gain adjustment (@x1): ",CMath.toPct(newHigh)),CMath.toPct(newHigh));
					if((!CMath.isNumber(newName))&&(!CMath.isPct(newName)))
						mob.tell(L("(no change)"));
					else
						newHigh=CMath.s_pct(newName);

					double newLow=factor.lossFactor();
					newName=mob.session().prompt(L("Enter loss adjustment (@x1): ",CMath.toPct(newLow)),CMath.toPct(newLow));
					if((!CMath.isNumber(newName))&&(!CMath.isPct(newName)))
						mob.tell(L("(no change)"));
					else
						newLow=CMath.s_pct(newName);
					meF.delFactor(factor);
					factor=meF.addFactor(newHigh,newLow,mask);
				}
			}

			// relations between factions
			++showNumber;
			while((mob.session()!=null)&&(!mob.session().isStopped())&&(!((showFlag>0)&&(showFlag!=showNumber))))
			{
				final StringBuffer list=new StringBuffer(showNumber+". Cross-Faction Relations:\n\r");
				list.append("    Faction"+CMStrings.padRight("",25)+"Percentage change\n\r");
				for(final Enumeration<String> e=meF.relationFactions();e.hasMoreElements();)
				{
					final String key=e.nextElement();
					final double value=meF.getRelation(key);
					final Faction F=CMLib.factions().getFaction(key);
					if(F!=null)
					{
						list.append("    "+CMStrings.padRight(F.name(),31)+" ");
						list.append(CMath.toPct(value));
						list.append("\n\r");
					}
				}
				mob.tell(list.toString());
				if((showFlag!=showNumber)&&(showFlag>-999))
					break;
				final String which=mob.session().prompt(L("Enter a faction to add, remove, or modify relations:"),"");
				if(which.length()==0)
					break;
				Faction theF=null;
				for(final Enumeration<String> e=meF.relationFactions();e.hasMoreElements();)
				{
					final String key=e.nextElement();
					final Faction F=CMLib.factions().getFaction(key);
					if((F!=null)&&(F.name().equalsIgnoreCase(which)))
						theF=F;
				}
				if(theF==null)
				{
					Faction possibleF=CMLib.factions().getFaction(which);
					if(possibleF==null)
						possibleF=CMLib.factions().getFactionByName(which);
					if(possibleF==null)
						mob.tell(L("'@x1' is not a valid faction.",which));
					else
					if(mob.session().confirm(L("Create a new relation for faction  '@x1' (y/N):",possibleF.name()),"N"))
					{
						theF=possibleF;
						meF.addRelation(theF.factionID(),1.0);
					}
				}
				else
				if(mob.session().choose(L("Would you like to M)odify or D)elete this relation (M/d): "),"MD","M").toUpperCase().startsWith("D"))
				{
					meF.delRelation(theF.factionID());
					mob.tell(L("Relation deleted."));
					theF=null;
				}
				if(theF!=null)
				{
					final String amount=CMath.toPct(meF.getRelation(theF.factionID()));
					final String newName=mob.session().prompt(L("Enter a relation amount (@x1): ",amount),""+amount);
					if((!CMath.isNumber(newName))&&(!CMath.isPct(newName)))
						mob.tell(L("(no change)"));
					meF.delRelation(theF.factionID());
					meF.addRelation(theF.factionID(),CMath.s_pct(newName));
				}
			}

			// faction change triggers
			++showNumber;
			while((mob.session()!=null)&&(!mob.session().isStopped())&&(!((showFlag>0)&&(showFlag!=showNumber))))
			{
				final StringBuffer list=new StringBuffer(showNumber+". Faction Change Triggers:\n\r");
				list.append("    "+CMStrings.padRight(L("Type"),15)
						+" "+CMStrings.padRight(L("Direction"),10)
						+" "+CMStrings.padRight(L("Factor"),10)
						+" "+CMStrings.padRight(L("Flags"),20)
						+" Mask\n\r");
				int numChanges=0;
				final StringBuffer choices=new StringBuffer("");
				final Hashtable<Character,Faction.FactionChangeEvent> choicesHashed=new Hashtable<Character,Faction.FactionChangeEvent>();
				for(final Enumeration<String> e=meF.changeEventKeys();e.hasMoreElements();)
				{
					final Faction.FactionChangeEvent[] CEs=meF.getChangeEvents(e.nextElement());
					if(CEs!=null)
					{
						for (final FactionChangeEvent CE : CEs)
						{
							choices.append(((char)('A'+numChanges)));
							list.append(" "+(((char)('A'+numChanges))+") "));
							choicesHashed.put(Character.valueOf((char)('A'+numChanges)), CE);
							if(CE.triggerParameters().trim().length()==0)
								list.append(CMStrings.padRight(CE.eventID(),15)+" ");
							else
								list.append(CMStrings.padRight(CE.eventID()+":"+CE.triggerParameters(),15)+" ");
							list.append(CMStrings.padRight(Faction.FactionChangeEvent.CHANGE_DIRECTION_DESCS[CE.direction()],10)+" ");
							list.append(CMStrings.padRight(CMath.toPct(CE.factor()),10)+" ");
							list.append(CMStrings.padRight(CE.flagCache(),20)+" ");
							list.append(CE.targetZapper()+"\n\r");
							numChanges++;
						}
					}
				}
				mob.tell(list.toString());
				if((showFlag!=showNumber)&&(showFlag>-999))
					break;
				String which=mob.session().prompt(L("Select an ID to add, remove, or modify:"),"");
				which=which.toUpperCase().trim();
				if(which.length()==0)
					break;
				Faction.FactionChangeEvent CE=(which.length()>0)?choicesHashed.get(Character.valueOf(which.charAt(0))):null;
				if(CE==null)
				{
					final String newID=mob.session().prompt(L("Enter a new change ID (?): ")).toUpperCase().trim();
					if(newID.length()==0)
						break;
					if(newID.equalsIgnoreCase("?"))
					{
						mob.tell(L("Valid triggers: \n\r@x1",meF.ALL_CHANGE_EVENT_TYPES()));
						continue;
					}
					CE=meF.createChangeEvent("CHOICE"+Math.random(), newID);
					if(CE==null)
					{
						mob.tell(L("That ID is invalid.  Try '?'."));
						continue;
					}
					else
					if(!mob.session().confirm(L("Create a new trigger using ID '@x1' (y/N): ",newID),"N"))
					{
						meF.delChangeEvent(CE);
						CE=null;
						break;
					}
				}
				else
				if(mob.session().choose(L("Would you like to M)odify or D)elete this trigger (M/d): "),"MD","M").toUpperCase().startsWith("D"))
				{
					meF.delChangeEvent(CE);
					mob.tell(L("Trigger deleted."));
					CE=null;
				}

				if(CE!=null)
				{
					final String newFlags=mob.session().prompt(L("Trigger parms (@x1): ",CE.triggerParameters()),CE.triggerParameters());
					if((newFlags.length()==0)||(newFlags.equals(CE.triggerParameters())))
						mob.tell(L("(no change)"));
					else
						CE.setTriggerParameters(newFlags.trim());
				}
				if(CE!=null)
				{
					final StringBuffer directions=new StringBuffer("Valid directions:\n\r");
					final StringBuffer cmds=new StringBuffer("");
					for(int i=0;i<Faction.FactionChangeEvent.CHANGE_DIRECTION_DESCS.length;i++)
					{
						directions.append(((char)('A'+i))+") "+Faction.FactionChangeEvent.CHANGE_DIRECTION_DESCS[i]+"\n\r");
						cmds.append((char)('A'+i));
					}
					final String str=mob.session().choose(L("@x1\n\rSelect a new direction (@x2): ",directions.toString(),Faction.FactionChangeEvent.CHANGE_DIRECTION_DESCS[CE.direction()]),cmds.toString()+"\n\r","");
					if((str.length()==0)||str.equals("\n")||str.equals("\r")||(cmds.toString().indexOf(str.charAt(0))<0))
						mob.tell(L("(no change)"));
					else
						CE.setDirection((cmds.toString().indexOf(str.charAt(0))));
				}
				if(CE!=null)
				{
					if(CE.factor()==0.0)
						CE.setFactor(1.0);
					final String amount=CMath.toPct(CE.factor());
					final String newName=mob.session().prompt(L("Enter the amount factor (@x1): ",amount),""+amount);
					if((!CMath.isNumber(newName))&&(!CMath.isPct(newName)))
						mob.tell(L("(no change)"));
					else
						CE.setFactor(CMath.s_pct(newName));
				}
				if(CE!=null)
				{
					mob.tell(L("Valid flags/keys include: @x1\n\r",CMParms.toListString(CMParms.combine(Faction.FactionChangeEvent.FLAG_DESCS,Faction.FactionChangeEvent.FLAG_KEYVALS))));
					final String newFlags=mob.session().prompt(L("Enter new flag(s) (@x1): @x2",CE.flagCache(),CE.flagCache()),CE.flagCache());
					if((newFlags.length()==0)||(newFlags.equals(CE.flagCache())))
						mob.tell(L("(no change)"));
					else
						CE.setFlags(newFlags);
				}
				if(CE!=null)
				{
					final String newFlags=mob.session().prompt(L("Zapper mask (@x1): @x2",CE.targetZapper(),CE.targetZapper()),CE.targetZapper());
					if((newFlags.length()==0)||(newFlags.equals(CE.targetZapper())))
						mob.tell(L("(no change)"));
					else
						CE.setTargetZapper(newFlags);
				}
			}

			// Ability allowances
			++showNumber;
			while((mob.session()!=null)&&(!mob.session().isStopped())&&(!((showFlag>0)&&(showFlag!=showNumber))))
			{
				if((showFlag>0)&&(showFlag!=showNumber))
					break;
				final StringBuffer list=new StringBuffer(showNumber+". Ability allowances:\n\r");
				list.append("    #) "
						+CMStrings.padRight(L("Ability masks"),40)
						+" "+CMStrings.padRight(L("Low value"),10)
						+" "+CMStrings.padRight(L("High value"),10)
						+"\n\r");
				int numUsages=0;
				final StringBuffer choices=new StringBuffer("0\n\r");
				for(final Enumeration<Faction.FAbilityUsage> e=meF.abilityUsages();e.hasMoreElements();)
				{
					final Faction.FAbilityUsage CA=e.nextElement();
					if(CA!=null)
					{
						list.append("    "+((char)('A'+numUsages)+") "));
						list.append(CMStrings.padRight(CA.abilityFlags(),40)+" ");
						list.append(CMStrings.padRight((CA.notRange()?"!":"")+CA.low()+"",10)+" ");
						list.append(CMStrings.padRight(CA.high()+"",10)+" ");
						list.append("\n\r");
						choices.append((char)('A'+numUsages));
						numUsages++;
					}
				}
				mob.tell(list.toString());
				if((showFlag!=showNumber)&&(showFlag>-999))
					break;
				String which=mob.session().choose(L("Select an allowance to remove or modify, or enter 0 to Add:"),choices.toString(),"");
				if(which.length()!=1)
					break;
				which=which.toUpperCase().trim();
				Faction.FAbilityUsage CA=null;
				if(!which.equalsIgnoreCase("0"))
				{
					final int num=(which.charAt(0)-'A');
					if((num<0)||(num>=numUsages))
						break;
					CA=meF.getAbilityUsage(num);
					if(CA==null)
					{
						mob.tell(L("That allowance is invalid.."));
						continue;
					}
					if(mob.session().choose(L("Would you like to M)odify or D)elete this allowance (M/d): "),"MD","M").toUpperCase().startsWith("D"))
					{
						meF.delAbilityUsage(CA);
						mob.tell(L("Allowance deleted."));
						CA=null;
					}
				}
				else
				if(!mob.session().confirm(L("Create a new allowance (y/N): "),"N"))
				{
					continue;
				}
				else
					CA=meF.addAbilityUsage(null);
				if(CA!=null)
				{
					boolean cont=false;
					while((!cont)&&(!mob.session().isStopped()))
					{
						final String newFlags=mob.session().prompt(L("Ability determinate masks or ? (@x1): @x2",CA.abilityFlags(),CA.abilityFlags()),CA.abilityFlags());
						if(newFlags.equalsIgnoreCase("?"))
						{
							final StringBuffer vals=new StringBuffer("Valid masks: \n\r");
							for (final String element : Ability.ACODE.DESCS)
								vals.append(element+", ");
							for (final String element : Ability.DOMAIN.DESCS)
								vals.append(element+", ");
							for (final String element : Ability.FLAG_DESCS)
								vals.append(element+", ");
							vals.append(" * Any ABILITY ID (skill/prayer/spell/etc)");
							mob.tell(vals.toString());
							cont=false;
						}
						else
						{
							cont=true;
							if((newFlags.length()==0)||(newFlags.equals(CA.abilityFlags())))
								mob.tell(L("(no change)"));
							else
							{
								final List<String> unknowns=CA.setAbilityFlag(newFlags);
								if(unknowns.size()>0)
									for(int i=unknowns.size()-1;i>=0;i--)
										if(CMClass.getAbility(unknowns.get(i))!=null)
											unknowns.remove(i);
								if(unknowns.size()>0)
								{
									mob.tell(L("The following are unknown masks: '@x1'.  Please correct them.",CMParms.toListString(unknowns)));
									cont=false;
								}
							}
						}
					}
					final String curLowVal = (CA.notRange()?"!":"")+CA.low();
					String newName=mob.session().prompt(L("Enter the minimum value to use the ability. Prefix with ! to negate the range.\n\r(@x1): ",""+curLowVal),""+curLowVal);
					if(newName.equals(curLowVal)
					||((!CMath.isInteger(newName))
						&&((newName.length()==1)
							||(!newName.startsWith("!"))
							||(!CMath.isInteger(newName.substring(1))))))
						mob.tell(L("(no change)"));
					else
						CA.setLow(newName);
					newName=mob.session().prompt(L("Enter the maximum value to use the ability (@x1): ",""+CA.high()),""+CA.high());
					if((!CMath.isInteger(newName))||(CA.high()==CMath.s_int(newName)))
						mob.tell(L("(no change)"));
					else
						CA.setHigh(CMath.s_int(newName));
					if(CA.high()<CA.low())
						CA.setHigh(CA.low());
				}
			}

			// Affects/Behaviors
			++showNumber;
			while((mob.session()!=null)&&(!mob.session().isStopped())&&(!((showFlag>0)&&(showFlag!=showNumber))))
			{
				if((showFlag>0)&&(showFlag!=showNumber))
					break;
				final StringBuffer list=new StringBuffer(showNumber+". Effects/Behaviors:\n\r");
				list.append("    #) "
						+CMStrings.padRight(L("Ability/Behavior ID"),25)
						+" "+CMStrings.padRight(L("MOB Mask"),20)
						+" "+CMStrings.padRight(L("Parameters"),20)
						+"\n\r");
				int numAffBehavs=0;
				final StringBuffer choices=new StringBuffer("0\n\r");
				final Vector<String> IDs=new Vector<String>();
				String ID=null;
				for(final Enumeration<String> e=meF.affectsBehavs();e.hasMoreElements();)
				{
					ID=e.nextElement();
					final String[] parms=meF.getAffectBehav(ID);
					list.append("    "+((char)('A'+numAffBehavs)+") "));
					list.append(CMStrings.padRight(ID,25)+" ");
					list.append(CMStrings.padRight(parms[1]+"",20)+" ");
					list.append(CMStrings.padRight(parms[0]+"",20)+" ");
					list.append("\n\r");
					choices.append((char)('A'+numAffBehavs));
					IDs.addElement(ID);
					numAffBehavs++;
				}
				mob.tell(list.toString());
				if((showFlag!=showNumber)&&(showFlag>-999))
					break;
				String which=mob.session().choose(L("Select an ability/behavior ID to remove or modify, or enter 0 to Add:"),choices.toString(),"");
				if(which.length()!=1)
					break;
				which=which.toUpperCase().trim();
				if(!which.equalsIgnoreCase("0"))
				{
					final int num=(which.charAt(0)-'A');
					if((num<0)||(num>=IDs.size()))
						break;
					ID=IDs.elementAt(num);
					final String type=getWordAffOrBehav(ID);
					if(mob.session().choose(L("Would you like to M)odify or D)elete this @x1 (M/d): ",type),"MD","M").toUpperCase().startsWith("D"))
					{
						meF.delAffectBehav(ID);
						mob.tell(L("@x1 deleted.",CMStrings.capitalizeAndLower(type)));
						ID=null;
					}
				}
				else
				{
					boolean cont=true;
					while((cont)&&(!mob.session().isStopped()))
					{
						cont=false;
						ID=mob.session().prompt(L("Enter a new Ability or Behavior ID or ?: "));
						if(ID.equalsIgnoreCase("?"))
						{
							final StringBuffer vals=new StringBuffer("Valid IDs: \n\r");
							vals.append(CMParms.toCMObjectListString(CMClass.abilities()));
							vals.append(CMParms.toCMObjectListString(CMClass.behaviors()));
							mob.tell(vals.toString());
							cont=true;
						}
						else
						{
							if((ID.length()==0)||(meF.getAffectBehav(ID)!=null))
							{
								mob.tell(L("(nothing done)"));
								ID=null;
								break;
							}
							final String type=getWordAffOrBehav(ID);
							if(type==null)
							{
								mob.tell(L("'@x1 is neither a valid behavior ID or ability ID.  Use ? for a list.",ID));
								cont=true;
							}
							else
							if(!mob.session().confirm(L("Create a new @x1 (y/N): ",type),"N"))
							{
								ID=null;
								break;
							}
							else
								meF.addAffectBehav(ID,"","");
						}
					}
				}
				if(ID!=null)
				{
					final String type=getWordAffOrBehav(ID);
					final String[] oldData=meF.getAffectBehav(ID);
					final String[] newData=new String[2];
					boolean cont=true;
					while((cont)&&(!mob.session().isStopped()))
					{
						cont=false;
						final String mask=mob.session().prompt(L("Enter a new Zapper Mask or ? (@x1)\n\r: ",oldData[1]),oldData[1]);
						if(mask.equalsIgnoreCase("?"))
						{
							mob.tell(CMLib.masking().maskHelp("\n\r","disallow"));
							cont=true;
						}
						else
						if(!mask.equals(oldData[1]))
							newData[1]=mask;
					}

					cont=true;
					while((cont)&&(!mob.session().isStopped()))
					{
						cont=false;
						final String parms=mob.session().prompt(L("Enter new @x1 parameters for @x2 or ? (@x3)\n\r: ",type,ID,oldData[0]),oldData[0]);
						if(parms.equalsIgnoreCase("?"))
						{
							mob.tell(CMLib.help().getHelpText(ID,mob,true).toString());
							cont=true;
						}
						else
							if(!parms.equals(oldData[0]))
								newData[0]=parms;
					}
					if((newData[0]==null)&&(newData[1]!=null))
						newData[0]=oldData[0];
					else
					if((newData[0]!=null)&&(newData[1]==null))
						newData[1]=oldData[1];
					if((newData[0]!=null)&&(newData[1]!=null))
					{
						meF.delAffectBehav(ID);
						meF.addAffectBehav(ID,newData[0],newData[1]);
					}
				}
			}

			// Reaction Command/Affects/Behaviors
			++showNumber;
			while((mob.session()!=null)&&(!mob.session().isStopped())&&(!((showFlag>0)&&(showFlag!=showNumber))))
			{
				if((showFlag>0)&&(showFlag!=showNumber))
					break;
				final StringBuffer list=new StringBuffer(showNumber+". Reaction Commands/Effects/Behaviors:\n\r");
				list.append("    #) "
						+CMStrings.padRight(L("Range"),15)
						+" "+CMStrings.padRight(L("MOB Mask"),18)
						+" "+CMStrings.padRight(L("Able/Beh/Cmd"),15)
						+" "+CMStrings.padRight(L("Parameters"),18)
						+"\n\r");
				int numReactions=0;
				final StringBuffer choices=new StringBuffer("0\n\r");
				final Vector<Faction.FReactionItem> reactions=new Vector<Faction.FReactionItem>();
				Faction.FReactionItem item=null;
				for(final Enumeration<Faction.FReactionItem> e=meF.reactions();e.hasMoreElements();)
				{
					item=e.nextElement();
					list.append("    "+((char)('A'+numReactions)+") "));
					list.append(CMStrings.padRight(item.rangeCodeName(),15)+" ");
					list.append(CMStrings.padRight(item.presentMOBMask()+"",18)+" ");
					list.append(CMStrings.padRight(item.reactionObjectID()+"",15)+" ");
					list.append(CMStrings.padRight(item.parameters()+"",18)+" ");
					list.append("\n\r");
					choices.append((char)('A'+numReactions));
					reactions.addElement(item);
					numReactions++;
				}
				mob.tell(list.toString());
				if((showFlag!=showNumber)&&(showFlag>-999))
					break;
				String which=mob.session().choose(L("Select one to remove or modify, or enter 0 to Add:"),choices.toString(),"");
				if(which.length()!=1)
					break;
				which=which.toUpperCase().trim();
				item=null;
				if(!which.equalsIgnoreCase("0"))
				{
					final int num=(which.charAt(0)-'A');
					if((num<0)||(num>=reactions.size()))
						break;
					item=reactions.elementAt(num);
					final String type=getWordAffOrBehav(item.reactionObjectID());
					if(mob.session().choose(L("Would you like to M)odify or D)elete this @x1 (M/d): ",type),"MD","M").toUpperCase().startsWith("D"))
					{
						meF.delReaction(item);
						mob.tell(L("@x1 deleted.",CMStrings.capitalizeAndLower(type)));
						item=null;
					}
				}

				String type="";
				String[] oldData=new String[]{"","","",""};
				if(item != null)
				{
					type=getWordAffOrBehav(item.reactionObjectID());
					oldData=new String[]{item.rangeCodeName(),item.presentMOBMask(),item.reactionObjectID(),item.parameters()};
				}

				final String[] newData=new String[4];

				boolean cont=true;

				cont=true;
				while((cont)&&(!mob.session().isStopped()))
				{
					cont=false;

					final String rangeCode=mob.session().prompt(L("Enter a new range code or ? (@x1)\n\r: ",oldData[0]),oldData[0]).toUpperCase().trim();
					if(rangeCode.equalsIgnoreCase("?"))
					{
						final StringBuffer str=new StringBuffer("");
						for(final Enumeration<Faction.FRange> e=meF.ranges();e.hasMoreElements();)
						{
							final Faction.FRange FR=e.nextElement();
							str.append(FR.codeName()+" ");
						}
						mob.tell(str.toString().trim()+"\n\r");
						cont=true;
					}
					else
					if(!rangeCode.equals(oldData[0]))
					{
						cont=true;
						for(final Enumeration<Faction.FRange> e=meF.ranges();e.hasMoreElements();)
						{
							if(e.nextElement().codeName().equalsIgnoreCase(rangeCode))
							{
								newData[0]=rangeCode;
								cont=false;
							}
						}
						if(cont)
							mob.tell(L("'@x1' is not a valid range code.  Use ?",rangeCode));
					}
				}

				cont = true;
				while((cont)&&(!mob.session().isStopped()))
				{
					cont=false;

					final String mask=mob.session().prompt(L("Enter a new Zapper Mask or ? (@x1)\n\r: ",oldData[1]),oldData[1]);
					if(mask.equalsIgnoreCase("?"))
					{
						mob.tell(CMLib.masking().maskHelp("\n\r","disallow"));
						cont=true;
					}
					else
					if(!mask.equals(oldData[1]))
						newData[1]=mask;
				}

				cont=true;
				while((cont)&&(!mob.session().isStopped()))
				{
					cont=false;
					final String ID=mob.session().prompt(L("Enter a new Ability, Behavior, or Command ID (@x1)\n\r: ",oldData[2]),oldData[2]);
					if(ID.equalsIgnoreCase("?"))
					{
						final StringBuffer vals=new StringBuffer("Valid IDs: \n\r");
						vals.append(CMParms.toCMObjectListString(CMClass.abilities()));
						vals.append(CMParms.toCMObjectListString(CMClass.behaviors()));
						vals.append(CMParms.toCMObjectListString(CMClass.commands()));
						mob.tell(vals.toString());
						cont=true;
					}
					else
					{
						type=getWordAffOrBehav(ID);
						if(type==null)
						{
							mob.tell(L("'@x1 is neither a valid behavior, command, ability ID. Use ? for a list.",ID));
							cont=true;
						}
						else
							newData[2]=ID;
					}
				}

				cont=true;
				while((cont)&&(!mob.session().isStopped()))
				{
					cont=false;
					final String parms=mob.session().prompt(L("Enter new @x1 parameters for @x2 or ? (@x3)\n\r: ",type,newData[2],oldData[3]),oldData[3]);
					if(parms.equalsIgnoreCase("?"))
					{
						mob.tell(CMLib.help().getHelpText(newData[3],mob,true).toString());
						cont=true;
					}
					else
					if(!parms.equals(oldData[3]))
						newData[3]=parms;
				}

				for(int n=0;n<oldData.length;n++)
				{
					if(newData[n]==null)
						newData[n]=oldData[n];
				}
				if(item==null)
					meF.addReaction(newData[0], newData[1], newData[2], newData[3]);
				else
				{
					item.setRangeName(newData[0]);
					item.setPresentMOBMask(newData[1]);
					item.setReactionObjectID(newData[2]);
					item.setParameters(newData[3]);
				}
			}
			if(meF.reactions().hasMoreElements())
				meF.setLightReactions(CMLib.genEd().prompt(mob,meF.useLightReactions(),++showNumber,showFlag,L("Use 'Light' Reactions")));
			else
				meF.setLightReactions(false);

			if(showFlag<-900)
			{
				ok=true;
				break;
			}
			if(showFlag>0)
			{
				showFlag=-1;
				continue;
			}
			showFlag=CMath.s_int(mob.session().prompt(L("Edit which? "),""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
			}
		}

		final String errMsg=resaveFaction(meF);
		if(errMsg.length()>0)
			mob.tell(errMsg);
	}

	private StringBuffer rebuildFactionProperties(final Faction F)
	{
		final FactionManager facLib=CMLib.factions();
		List<String> oldV=Resources.getFileLineVector(Resources.getFileResource(makeFactionFilename(F.factionID()),true));
		if(oldV.size()<10)
		{
			final StringBuffer template=new CMFile(Resources.buildResourcePath("examples")+"factiontemplate.ini",null,CMFile.FLAG_LOGERRORS).text();
			oldV=Resources.getFileLineVector(template);
		}
		final boolean[] defined=new boolean[FacTag.values().length];
		for(int i=0;i<defined.length;i++)
			defined[i]=false;
		for(int v=0;v<oldV.size();v++)
		{
			final String s=oldV.get(v);
			if(!(s.trim().startsWith("#")||s.trim().length()==0||(s.indexOf('=')<0)))
			{
				final String tag=s.substring(0,s.indexOf('=')).trim().toUpperCase();
				final FacTag tagRef=facLib.getFactionTag(tag);
				if(tagRef!=null)
					defined[tagRef.ordinal()]=true;
			}
		}
		final boolean[] done=new boolean[FacTag.values().length];
		for(int i=0;i<done.length;i++)
			done[i]=false;
		int lastCommented=-1;
		final String CR="\r\n";
		final StringBuffer buf=new StringBuffer("");
		for(int v=0;v<oldV.size();v++)
		{
			String s=oldV.get(v);
			if(s.trim().length()==0)
			{
				if((lastCommented>=0)&&(!done[lastCommented]))
				{
					done[lastCommented]=true;
					buf.append(F.getINIDef(Faction.FacTag.values()[lastCommented].name(),CR)+CR);
					lastCommented=-1;
				}
			}
			else
			if(s.trim().startsWith("#")||(s.indexOf('=')<0))
			{
				buf.append(s+CR);
				final int x=s.indexOf('=');
				if(x>=0)
				{
					s=s.substring(0,x).trim();
					int first=s.length()-1;
					for(;first>=0;first--)
					{
						if(!Character.isLetterOrDigit(s.charAt(first)))
							break;
					}
					final FacTag g = facLib.getFactionTag(s.substring(first).trim().toUpperCase());
					first = (g!=null)?g.ordinal():-1;
					if(first>=0)
						lastCommented=first;
				}
			}
			else
			{
				final String tag=s.substring(0,s.indexOf('=')).trim().toUpperCase();
				final FacTag tagRef=facLib.getFactionTag(tag);
				if(tagRef==null)
					buf.append(s+CR);
				else
				if(!done[tagRef.ordinal()])
				{
					done[tagRef.ordinal()]=true;
					buf.append(F.getINIDef(tag,CR)+CR);
				}
			}
		}
		if((lastCommented>=0)&&(!done[lastCommented]))
			buf.append(F.getINIDef(FacTag.values()[lastCommented].name(),CR)+CR);
		return buf;
	}

	protected PlayerLibrary[] getOtherPlayerLibAllHosts()
	{
		final List<PlayerLibrary> list=new ArrayList<PlayerLibrary>();
		final FactionManager fman=this;
		for(final Enumeration<CMLibrary> pl=CMLib.libraries(CMLib.Library.PLAYERS); pl.hasMoreElements(); )
		{
			final PlayerLibrary pLib2 = (PlayerLibrary)pl.nextElement();
			if((pLib2 != null)
			&&(!list.contains(pLib2))
			&&(fman == CMLib.library(CMLib.getLibraryThreadID(Library.PLAYERS, pLib2), Library.FACTIONS)))
				list.add(pLib2);
		}
		return list.toArray(new PlayerLibrary[0]);
	}

	@Override
	public String resaveFaction(final Faction F)
	{
		if((F.factionID().length()>0)
		&&(CMLib.factions().getFaction(F.factionID())!=null))
		{
			if(!CMath.bset(F.getInternalFlags(), Faction.IFLAG_NEVERSAVE))
			{
				final StringBuffer buf = rebuildFactionProperties(F);
				String factionFilename = makeFactionFilename(F.factionID());
				final CMFile file = new CMFile(makeFactionFilename(F.factionID()),null);
				if(!file.exists())
					factionFilename = "::"+factionFilename;
				if(!Resources.updateFileResource(factionFilename,buf))
					return "Faction File '"+F.factionID()+"' could not be modified.  Make sure it is not READ-ONLY.";
				else
				if((!F.factionID().toLowerCase().endsWith(".ini"))
				&&(factionFilename.toLowerCase().endsWith(".ini")))
				{
					final String oldFactionID = F.factionID();
					CMLib.factions().removeFaction(F.factionID());
					F.setFactionID(F.factionID()+".INI");
					CMLib.factions().addFaction(F);
					for(final PlayerLibrary plib : getOtherPlayerLibAllHosts())
					{
						for(final Enumeration<MOB> m = plib.players();m.hasMoreElements();)
						{
							final MOB M = m.nextElement();
							if(M != null)
							{
								final Faction.FData data = M.fetchFactionData(oldFactionID);
								if(data != null)
								{
									final int fact = M.fetchFaction(oldFactionID);
									M.removeFaction(oldFactionID);
									M.addFaction(F.factionID(), fact);
								}
							}
						}
					}
				}
			}
		}
		else
			return "Can not save a blank faction";
		return "";
	}

	@Override
	public FAbilityMaskType getAbilityFlagType(String strflag)
	{
		for (final String element : Ability.ACODE.DESCS)
		{
			if(element.equalsIgnoreCase(strflag))
				return FAbilityMaskType.ACODE;
		}
		for (final String element : Ability.DOMAIN.DESCS)
		{
			if(element.equalsIgnoreCase(strflag))
				return FAbilityMaskType.DOMAIN;
		}
		if(strflag.startsWith("!"))
			strflag=strflag.substring(1);
		for (final String element : Ability.FLAG_DESCS)
		{
			if(element.equalsIgnoreCase(strflag))
				return FAbilityMaskType.FLAG;
		}
		if(CMClass.getAbility(strflag)!=null)
			return FAbilityMaskType.ID;
		return null;
	}
}
