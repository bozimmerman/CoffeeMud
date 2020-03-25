package com.planet_ink.coffee_mud.Behaviors;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.CMException;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary.XMLTag;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.concurrent.atomic.AtomicBoolean;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.*;

/*
   Copyright 2003-2020 Bo Zimmerman

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

public class RandomQuests extends ActiveTicker
{
	@Override
	public String ID()
	{
		return "RandomQuests";
	}

	@Override
	protected int canImproveCode()
	{
		return Behavior.CAN_MOBS|Behavior.CAN_AREAS;
	}

	@Override
	public long flags()
	{
		return Behavior.FLAG_MOBILITY;
	}

	protected int					minQuests	= 1;
	protected int					maxQuests	= 1;
	protected int					numQuests	= -1;
	protected int					maxAttempts	= 3;
	protected boolean				inline		= false;
	protected String				expireTime	= "3 hours";
	protected String				tagId		= "all_quests";
	protected String				filePath	= "randareas/example.xml";
	protected Map<String, String>	varMap		= new Hashtable<String, String>(1);

	protected SVector<Reference<Quest>> myQuests = new SVector<Reference<Quest>>();

	public RandomQuests()
	{
		super();
		minTicks=10; maxTicks=20; chance=100;
		tickReset();
	}

	@Override
	public String accountForYourself()
	{
		return "random quest generation";
	}

	@Override
	public void setParms(final String newParms)
	{
		super.setParms(newParms);
		filePath=CMParms.getParmStr(parms, "path", filePath);
		minQuests=CMParms.getParmInt(parms,"minquests",minQuests);
		maxQuests=CMParms.getParmInt(parms,"maxquests",maxQuests);
		maxAttempts=CMParms.getParmInt(parms,"maxattempts",maxAttempts);
		expireTime=CMParms.getParmStr(parms, "expire", expireTime);
		tagId=CMParms.getParmStr(parms, "tagid", tagId);
		inline=CMParms.getParmBool(parms, "inline", false);
		numQuests=-1;
		final String[] igore= {"PATH", "MINQUESTS", "MAXQUESTS", "MIN", "MAX", "CHANCE", "EXPIRE", "TAGID", "MAXATTEMPTS", "INLINE"};
		final Map<String,String> parms=CMParms.parseEQParms(newParms);
		varMap.clear();
		for(final String key :parms.keySet())
		{
			if(CMParms.indexOfIgnoreCase(igore, key)<0)
				varMap.put(key.toUpperCase(), parms.get(key));
		}
		if(!varMap.containsKey("TEMPLATE"))
			varMap.put("TEMPLATE", "random");
		varMap.put("EXPIRATION", expireTime);
	}


	@Override
	public void endBehavior(final PhysicalAgent forMe)
	{
		for(final Reference<Quest> r : myQuests)
		{
			final Quest Q=r.get();
			if(Q!=null)
			{
				if(Q.running())
					Q.stopQuest();
				CMLib.quests().delQuest(Q);
			}
		}
	}

	public String getGeneratorXmlPath()
	{
		return filePath;
	}

	public Map<String, String> getAutoGenVariables()
	{
		return varMap;
	}

	public void setGeneratorXmlPath(final String path)
	{
		filePath = path;
	}

	public void setAutoGenVariables(final Map<String, String> vars)
	{
		varMap = vars;
	}

	protected final AtomicBoolean disable = new AtomicBoolean(false);
	protected final AtomicBoolean processing = new AtomicBoolean(false);

	public final class GenerateAQuest implements Runnable
	{
		protected final Tickable ticking;

		public GenerateAQuest(final Tickable ticking)
		{
			this.ticking=ticking;
		}

		public void run()
		{
			try
			{
				processing.set(true);
				for(int i=0;i<maxAttempts;i++)
				{
					try
					{
						final StringBuffer xml = Resources.getFileResource(getGeneratorXmlPath(), true);
						if((xml==null)||(xml.length()==0))
						{
							Log.errOut("Unable to generate a quest for "+ticking.name()+" because file not found: "+getGeneratorXmlPath());
							disable.set(true);
							return;
						}
						final List<XMLLibrary.XMLTag> xmlRoot = CMLib.xml().parseAllXML(xml);
						final Hashtable<String,Object> definedIDs = new Hashtable<String,Object>();
						definedIDs.putAll(getAutoGenVariables());
						CMLib.percolator().buildDefinedIDSet(xmlRoot,definedIDs, getAutoGenVariables().keySet());
						final String idName = tagId.toUpperCase().trim();
						if((!(definedIDs.get(idName) instanceof XMLTag))
						||(!((XMLTag)definedIDs.get(idName)).tag().equalsIgnoreCase("quest")))
						{
							Log.errOut(L("The quest id '@x1' has not been defined in the data file for @x2.",idName,ticking.name()));
							disable.set(true);
							return;
						}
						final XMLTag piece=(XMLTag)definedIDs.get(idName);
						try
						{
							CMLib.percolator().checkRequirements(piece, definedIDs);
						}
						catch(final CMException cme)
						{
							Log.errOut(L("Required ids for @x1 were missing: @x2: for @x3",idName,cme.getMessage(),ticking.name()));
							disable.set(true);
							return;
						}
						final Modifiable obj = (ticking instanceof Modifiable)?(Modifiable)ticking:null;
						final String s=CMLib.percolator().buildQuestScript(piece, definedIDs, obj);
						if(s.length()==0)
							throw new CMException("Failed to create any sort of quest at all! WTF!!");
						CMLib.percolator().postProcess(definedIDs);
						if((!definedIDs.containsKey("QUEST_ID"))
						||(!(definedIDs.get("QUEST_ID") instanceof String)))
							throw new CMException("Unable to create your quest because a quest_id was not generated");
						final String name=(String)definedIDs.get("QUEST_ID");
						final Quest Q=(Quest)CMClass.getCommon("DefaultQuest");
						Q.setScript(s,true);
						if((Q.name().trim().length()==0)||(Q.duration()<0))
						{
							System.out.println(s);
							throw new CMException("Unable to create your quest.  Please consult the log.");
						}
						final Quest badQ=CMLib.quests().fetchQuest(name);
						if(badQ!=null)
							throw new CMException("Unable to create your quest.  One of that name already exists!");
						//mob.tell("Generated quest '"+Q.name()+"'");
						//Log.sysOut("Generate",mob.Name()+" created quest '"+Q.name()+"'");
						CMLib.quests().addQuest(Q);
						if(!Q.running())
						{
							if(!Q.startQuest())
							{
								CMLib.quests().delQuest(Q);
								throw new CMException("Unable to start the quest.  Something went wrong.  Perhaps the problem was logged?");
							}
						}
						Q.setCopy(true);
						myQuests.add(new WeakReference<Quest>(Q));
						break;
					}
					catch(final CMException cme)
					{
						if(i==maxAttempts-1)
						{
							Log.errOut("RandomQuests",cme);
							Log.errOut(L("Failed to finish creating a quest for @x1",ticking.name()));
							disable.set(true);
							return;
						}
					}
				}
			}
			finally
			{
				processing.set(false);
			}
		}
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		super.tick(ticking,tickID);
		if(canAct(ticking,tickID))
		{
			if(processing.get())
				return false;
			if(disable.get())
				return false;
			if(numQuests < 0)
				numQuests=minQuests + ((maxQuests > minQuests)?CMLib.dice().roll(1, maxQuests-minQuests+1, -1):0);
			synchronized(myQuests)
			{
				for(int i=myQuests.size()-1;i>=0;i--)
				{
					final Reference<Quest> Q=myQuests.get(i);
					if(Q.get() == null)
						myQuests.remove(i);
				}
			}
			final GenerateAQuest generator=new GenerateAQuest(ticking);
			if(myQuests.size() < numQuests)
			{
				if(inline)
				{
					while(myQuests.size() < numQuests)
						generator.run();
				}
				else
					CMLib.threads().executeRunnable(generator);
			}
		}
		return true;
	}

	@Override
	public String getStat(final String code)
	{
		if(code.equalsIgnoreCase("NUMQUESTS"))
			return ""+numQuests;
		else
		if(code.equalsIgnoreCase("QUEST"))
		{
			if(myQuests.size()==0)
				return "";
			for(final Reference<Quest> r : myQuests)
			{
				if(r.get()!=null)
					return r.get().name();
			}
			return "";
		}
		return super.getStat(code);
	}
}
