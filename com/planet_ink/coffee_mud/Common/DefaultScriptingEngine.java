package com.planet_ink.coffee_mud.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Expire;
import com.planet_ink.coffee_mud.core.exceptions.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.AccountStats.PrideStat;
import com.planet_ink.coffee_mud.Common.interfaces.Session.InputCallback;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.ChannelsLibrary.CMChannel;
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine.PlayerData;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary.XMLTag;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import org.mozilla.javascript.*;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
   Copyright 2008-2018 Bo Zimmerman

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
@SuppressWarnings({"unchecked","rawtypes"})
public class DefaultScriptingEngine implements ScriptingEngine
{
	@Override
	public String ID()
	{
		return "DefaultScriptingEngine";
	}

	@Override
	public String name()
	{
		return "Default Scripting Engine";
	}

	protected static final Map<String,Integer>	funcH	= new Hashtable<String,Integer>();
	protected static final Map<String,Integer>	methH	= new Hashtable<String,Integer>();
	protected static final Map<String,Integer>	progH	= new Hashtable<String,Integer>();
	protected static final Map<String,Integer>	connH	= new Hashtable<String,Integer>();
	protected static final Map<String,Integer>	gstatH	= new Hashtable<String,Integer>();
	protected static final Map<String,Integer>	signH	= new Hashtable<String,Integer>();

	protected static final Map<String,Pattern>	patterns= new Hashtable<String,Pattern>();

	protected boolean 				noDelay			 = CMSecurity.isDisabled(CMSecurity.DisFlag.SCRIPTABLEDELAY);

	protected String				scope			 = "";
	protected int					tickStatus		 = Tickable.STATUS_NOT;
	protected boolean				isSavable		 = true;
	protected boolean				alwaysTriggers	 = false;

	protected MOB					lastToHurtMe	 = null;
	protected Room					lastKnownLocation= null;
	protected Tickable				altStatusTickable= null;
	protected List<DVector>			oncesDone		 = new Vector<DVector>();
	protected Map<Integer,Integer>	delayTargetTimes = new Hashtable<Integer,Integer>();
	protected Map<Integer,int[]>	delayProgCounters= new Hashtable<Integer,int[]>();
	protected Map<Integer,Integer>	lastTimeProgsDone= new Hashtable<Integer,Integer>();
	protected Map<Integer,Integer>	lastDayProgsDone = new Hashtable<Integer,Integer>();
	protected Set<Integer>			registeredEvents = new HashSet<Integer>();
	protected Map<Integer,Long>		noTrigger		 = new Hashtable<Integer,Long>();
	protected MOB					backupMOB		 = null;
	protected CMMsg					lastMsg			 = null;
	protected Resources				resources		 = Resources.instance();
	protected Environmental			lastLoaded		 = null;
	protected String				myScript		 = "";
	protected String				defaultQuestName = "";
	protected String				scriptKey		 = null;
	protected boolean				runInPassiveAreas= true;
	protected boolean				debugBadScripts	 = false;
	protected List<ScriptableResponse>que			 = new Vector<ScriptableResponse>();
	protected final AtomicInteger	recurseCounter	 = new AtomicInteger();

	public DefaultScriptingEngine()
	{
		super();
		//CMClass.bumpCounter(this,CMClass.CMObjectType.COMMON);//removed for mem & perf
		debugBadScripts=CMSecurity.isDebugging(CMSecurity.DbgFlag.BADSCRIPTS);
	}

	@Override
	public boolean isSavable()
	{
		return isSavable;
	}

	@Override
	public void setSavable(boolean truefalse)
	{
		isSavable = truefalse;
	}

	@Override
	public String defaultQuestName()
	{
		return defaultQuestName;
	}

	protected Quest defaultQuest()
	{
		if(defaultQuestName.length()==0)
			return null;
		return CMLib.quests().fetchQuest(defaultQuestName);
	}

	@Override
	public void setVarScope(String newScope)
	{
		if((newScope==null)||(newScope.trim().length()==0)||newScope.equalsIgnoreCase("GLOBAL"))
		{
			scope="";
			resources=Resources.instance();
		}
		else
			scope=newScope.toUpperCase().trim();
		if(scope.equalsIgnoreCase("*")||scope.equals("INDIVIDUAL"))
			resources = Resources.newResources();
		else
		{
			resources=(Resources)Resources.getResource("VARSCOPE-"+scope);
			if(resources==null)
			{
				resources = Resources.newResources();
				Resources.submitResource("VARSCOPE-"+scope,resources);
			}
		}
	}

	@Override
	public String getVarScope()
	{
		return scope;
	}

	protected Object[] newObjs()
	{
		return new Object[ScriptingEngine.SPECIAL_NUM_OBJECTS];
	}

	@Override
	public String getLocalVarXML()
	{
		if((scope==null)||(scope.length()==0))
			return "";
		final StringBuffer str=new StringBuffer("");
		for(final Iterator<String> k = resources._findResourceKeys("SCRIPTVAR-");k.hasNext();)
		{
			final String key=k.next();
			if(key.startsWith("SCRIPTVAR-"))
			{
				str.append("<"+key.substring(10)+">");
				final Hashtable<String,String> H=(Hashtable)resources._getResource(key);
				for(final Enumeration<String> e=H.keys();e.hasMoreElements();)
				{
					final String vn=e.nextElement();
					final String val=H.get(vn);
					str.append("<"+vn+">"+CMLib.xml().parseOutAngleBrackets(val)+"</"+vn+">");
				}
				str.append("</"+key.substring(10)+">");
			}
		}
		return str.toString();
	}

	@Override
	public void setLocalVarXML(String xml)
	{
		for(final Iterator<String> k = Resources.findResourceKeys("SCRIPTVAR-");k.hasNext();)
		{
			final String key=k.next();
			if(key.startsWith("SCRIPTVAR-"))
				resources._removeResource(key);
		}
		final List<XMLLibrary.XMLTag> V=CMLib.xml().parseAllXML(xml);
		for(int v=0;v<V.size();v++)
		{
			final XMLTag piece=V.get(v);
			if((piece.contents()!=null)&&(piece.contents().size()>0))
			{
				final String kkey="SCRIPTVAR-"+piece.tag();
				final Hashtable<String,String> H=new Hashtable<String,String>();
				for(int c=0;c<piece.contents().size();c++)
				{
					final XMLTag piece2=piece.contents().get(c);
					H.put(piece2.tag(),piece2.value());
				}
				resources._submitResource(kkey,H);
			}
		}
	}

	private Quest getQuest(String named)
	{
		if((defaultQuestName.length()>0)&&(named.equals("*")||named.equalsIgnoreCase(defaultQuestName)))
			return defaultQuest();

		Quest Q=null;
		for(int i=0;i<CMLib.quests().numQuests();i++)
		{
			try
			{
				Q = CMLib.quests().fetchQuest(i);
			}
			catch (final Exception e)
			{
			}
			if(Q!=null)
			{
				if(Q.name().equalsIgnoreCase(named))
				{
					if(Q.running())
						return Q;
				}
			}
		}
		return CMLib.quests().fetchQuest(named);
	}

	@Override
	public int getTickStatus()
	{
		final Tickable T=altStatusTickable;
		if(T!=null)
			return T.getTickStatus();
		return tickStatus;
	}

	@Override
	public void registerDefaultQuest(String qName)
	{
		if((qName==null)||(qName.trim().length()==0))
			defaultQuestName="";
		else
			defaultQuestName=qName.trim();
	}

	@Override
	public CMObject newInstance()
	{
		try
		{
			return this.getClass().newInstance();
		}
		catch(final Exception e)
		{
			Log.errOut(ID(),e);
		}
		return new DefaultScriptingEngine();
	}

	@Override
	public CMObject copyOf()
	{
		try
		{
			final DefaultScriptingEngine S=(DefaultScriptingEngine)this.clone();
			//CMClass.bumpCounter(S,CMClass.CMObjectType.COMMON);//removed for mem & perf
			S.reset();
			return S;
		}
		catch(final CloneNotSupportedException e)
		{
			return new DefaultScriptingEngine();
		}
	}
	//protected void finalize(){CMClass.unbumpCounter(this,CMClass.CMObjectType.COMMON);}//removed for mem & perf

	/*
	 * c=clean bit, r=pastbitclean, p=pastbit, s=remaining clean bits, t=trigger
	 */
	protected String[] parseBits(DVector script, int row, String instructions)
	{
		final String line=(String)script.elementAt(row,1);
		final String[] newLine=parseBits(line,instructions);
		script.setElementAt(row,2,newLine);
		return newLine;
	}

	protected String[] parseSpecial3PartEval(String[][] eval, int t)
	{
		String[] tt=eval[0];
		final String funcParms=tt[t];
		final String[] tryTT=parseBits(funcParms,"ccr");
		if(signH.containsKey(tryTT[1]))
			tt=parseBits(eval,t,"ccr"); /* tt[t+0] */
		else
		{
			String[] parsed=null;
			if(CMParms.cleanBit(funcParms).equals(funcParms))
				parsed=parseBits("'"+funcParms+"' . .","cr");
			else
				parsed=parseBits(funcParms+" . .","cr");
			tt=insertStringArray(tt,parsed,t);
			eval[0]=tt;
		}
		return tt;
	}

	/*
	 * c=clean bit, r=pastbitclean, p=pastbit, s=remaining clean bits, t=trigger
	 */
	protected String[] parseBits(String line, String instructions)
	{
		String[] newLine=new String[instructions.length()];
		for(int i=0;i<instructions.length();i++)
		{
			switch(instructions.charAt(i))
			{
			case 'c':
				newLine[i] = CMParms.getCleanBit(line, i);
				break;
			case 'C':
				newLine[i] = CMParms.getCleanBit(line, i).toUpperCase().trim();
				break;
			case 'r':
				newLine[i] = CMParms.getPastBitClean(line, i - 1);
				break;
			case 'R':
				newLine[i] = CMParms.getPastBitClean(line, i - 1).toUpperCase().trim();
				break;
			case 'p':
				newLine[i] = CMParms.getPastBit(line, i - 1);
				break;
			case 'P':
				newLine[i] = CMParms.getPastBit(line, i - 1).toUpperCase().trim();
				break;
			case 'S':
				line = line.toUpperCase();
				//$FALL-THROUGH$
			case 's':
			{
				final String s = CMParms.getPastBit(line, i - 1);
				final int numBits = CMParms.numBits(s);
				final String[] newNewLine = new String[newLine.length - 1 + numBits];
				for (int x = 0; x < i; x++)
					newNewLine[x] = newLine[x];
				for (int x = 0; x < numBits; x++)
					newNewLine[i + x] = CMParms.getCleanBit(s, i - 1);
				newLine = newNewLine;
				i = instructions.length();
				break;
			}
			case 'T':
				line = line.toUpperCase();
				//$FALL-THROUGH$
			case 't':
			{
				final String s = CMParms.getPastBit(line, i - 1);
				String[] newNewLine = null;
				if (CMParms.getCleanBit(s, 0).equalsIgnoreCase("P"))
				{
					newNewLine = new String[newLine.length + 1];
					for (int x = 0; x < i; x++)
						newNewLine[x] = newLine[x];
					newNewLine[i] = "P";
					newNewLine[i + 1] = CMParms.getPastBitClean(s, 0);
				}
				else
				{

					final int numNewBits = (s.trim().length() == 0) ? 1 : CMParms.numBits(s);
					newNewLine = new String[newLine.length - 1 + numNewBits];
					for (int x = 0; x < i; x++)
						newNewLine[x] = newLine[x];
					for (int x = 0; x < numNewBits; x++)
						newNewLine[i + x] = CMParms.getCleanBit(s, x);
				}
				newLine = newNewLine;
				i = instructions.length();
				break;
			}
			}
		}
		return newLine;
	}

	protected String[] insertStringArray(String[] oldS, String[] inS, int where)
	{
		final String[] newLine=new String[oldS.length+inS.length-1];
		for(int i=0;i<where;i++)
			newLine[i]=oldS[i];
		for(int i=0;i<inS.length;i++)
			newLine[where+i]=inS[i];
		for(int i=where+1;i<oldS.length;i++)
			newLine[inS.length+i-1]=oldS[i];
		return newLine;
	}

	/*
	 * c=clean bit, r=pastbitclean, p=pastbit, s=remaining clean bits, t=trigger
	 */
	protected String[] parseBits(String[][] oldBits, int start, String instructions)
	{
		final String[] tt=oldBits[0];
		final String parseMe=tt[start];
		final String[] parsed=parseBits(parseMe,instructions);
		if(parsed.length==1)
		{
			tt[start]=parsed[0];
			return tt;
		}
		final String[] newLine=insertStringArray(tt,parsed,start);
		oldBits[0]=newLine;
		return newLine;
	}

	@Override
	public boolean endQuest(PhysicalAgent hostObj, MOB mob, String quest)
	{
		if(mob!=null)
		{
			final List<DVector> scripts=getScripts();
			if(!mob.amDead())
				lastKnownLocation=mob.location();
			String trigger="";
			String[] tt=null;
			for(int v=0;v<scripts.size();v++)
			{
				final DVector script=scripts.get(v);
				if(script.size()>0)
				{
					trigger=((String)script.elementAt(0,1)).toUpperCase().trim();
					tt=(String[])script.elementAt(0,2);
					if((getTriggerCode(trigger,tt)==13) //questtimeprog
					&&(!oncesDone.contains(script)))
					{
						if(tt==null)
							tt=parseBits(script,0,"CCC");
						if((tt!=null)
						&&((tt[1].equals(quest)||(tt[1].equals("*"))))
						&&(CMath.s_int(tt[2])<0))
						{
							oncesDone.add(script);
							execute(hostObj,mob,mob,mob,null,null,script,null,newObjs());
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	@Override
	public List<String> externalFiles()
	{
		final Vector<String> xmlfiles=new Vector<String>();
		parseLoads(getScript(), 0, xmlfiles, null);
		return xmlfiles;
	}

	protected String getVarHost(Environmental E,
								String rawHost,
								MOB source,
								Environmental target,
								PhysicalAgent scripted,
								MOB monster,
								Item primaryItem,
								Item secondaryItem,
								String msg,
								Object[] tmp)
	{
		if(!rawHost.equals("*"))
		{
			if(E==null)
				rawHost=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,rawHost);
			else
			if(E instanceof Room)
				rawHost=CMLib.map().getExtendedRoomID((Room)E);
			else
				rawHost=E.Name();
		}
		return rawHost;
	}

	@Override
	public boolean isVar(String host, String var)
	{
		if(host.equalsIgnoreCase("*"))
		{
			String val=null;
			Hashtable H=null;
			String key=null;
			var=var.toUpperCase();
			for(final Iterator<String> k = resources._findResourceKeys("SCRIPTVAR-");k.hasNext();)
			{
				key=k.next();
				if(key.startsWith("SCRIPTVAR-"))
				{
					H=(Hashtable)resources._getResource(key);
					val=(String)H.get(var);
					if(val!=null)
						return true;
				}
			}
			return false;
		}
		final Hashtable H=(Hashtable)resources._getResource("SCRIPTVAR-"+host);
		String val=null;
		if(H!=null)
			val=(String)H.get(var.toUpperCase());
		return (val!=null);
	}

	public String getVar(Environmental E, String rawHost, String var, MOB source, Environmental target,
						 PhysicalAgent scripted, MOB monster, Item primaryItem, Item secondaryItem, String msg,
						 Object[] tmp)
	{
		return getVar(getVarHost(E,rawHost,source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp),var);
	}

	@Override
	public String getVar(String host, String var)
	{
		String varVal = getVar(resources,host,var,null);
		if(varVal != null)
			return varVal;
		if((this.defaultQuestName!=null)&&(this.defaultQuestName.length()>0))
		{
			final Resources questResources=(Resources)Resources.getResource("VARSCOPE-"+this.defaultQuestName);
			if((questResources != null)&&(resources!=questResources))
			{
				varVal = getVar(questResources,host,var,null);
				if(varVal != null)
					return varVal;
			}
		}
		if(resources == Resources.instance())
			return "";
		return getVar(Resources.instance(),host,var,"");
	}
	
	public String getVar(final Resources resources, final String host, String var, String defaultVal)
	{
		if(host.equalsIgnoreCase("*"))
		{
			if(var.equals("COFFEEMUD_SYSTEM_INTERNAL_NONFILENAME_SCRIPT"))
			{
				final StringBuffer str=new StringBuffer("");
				parseLoads(getScript(),0,null,str);
				return str.toString();
			}
			String val=null;
			Hashtable H=null;
			String key=null;
			var=var.toUpperCase();
			for(final Iterator<String> k = resources._findResourceKeys("SCRIPTVAR-");k.hasNext();)
			{
				key=k.next();
				if(key.startsWith("SCRIPTVAR-"))
				{
					H=(Hashtable)resources._getResource(key);
					val=(String)H.get(var);
					if(val!=null)
						return val;
				}
			}
			return defaultVal;
		}
		final Hashtable H=(Hashtable)resources._getResource("SCRIPTVAR-"+host);
		String val=null;
		if(H!=null)
			val=(String)H.get(var.toUpperCase());
		else
		if((defaultQuestName!=null)&&(defaultQuestName.length()>0))
		{
			final MOB M=CMLib.players().getPlayer(host);
			if(M!=null)
			{
				for(final Enumeration<ScriptingEngine> e=M.scripts();e.hasMoreElements();)
				{
					final ScriptingEngine SE=e.nextElement();
					if((SE!=null)
					&&(SE!=this)
					&&(defaultQuestName.equalsIgnoreCase(SE.defaultQuestName()))
					&&(SE.isVar(host,var)))
						return SE.getVar(host,var);
				}
			}
		}
		if(val==null) 
			return defaultVal;
		return val;
	}

	private StringBuffer getResourceFileData(String named, boolean showErrors)
	{
		if(getQuest("*")!=null)
			return getQuest("*").getResourceFileData(named, showErrors);
		return new CMFile(Resources.makeFileResourceName(named),null,CMFile.FLAG_LOGERRORS).text();
	}

	@Override
	public String getScript()
	{
		return myScript;
	}

	public void reset()
	{
		que = new Vector<ScriptableResponse>();
		lastToHurtMe = null;
		lastKnownLocation= null;
		altStatusTickable= null;
		oncesDone = new Vector<DVector>();
		delayTargetTimes = new Hashtable<Integer,Integer>();
		delayProgCounters= new Hashtable<Integer,int[]>();
		lastTimeProgsDone= new Hashtable<Integer,Integer>();
		lastDayProgsDone = new Hashtable<Integer,Integer>();
		registeredEvents = new HashSet<Integer>();
		noTrigger = new Hashtable<Integer,Long>();
		backupMOB = null;
		lastMsg = null;
	}

	@Override
	public void setScript(String newParms)
	{
		newParms=CMStrings.replaceAll(newParms,"'","`");
		if(newParms.startsWith("+"))
		{
			final String superParms=getScript();
			Resources.removeResource(getScriptResourceKey());
			newParms=superParms+";"+newParms.substring(1);
		}
		myScript=newParms;
		reset();
		if(myScript.length()>100)
			scriptKey="PARSEDPRG: "+myScript.substring(0,100)+myScript.length()+myScript.hashCode();
		else
			scriptKey="PARSEDPRG: "+myScript;
	}

	public boolean isFreeToBeTriggered(Tickable affecting)
	{
		if(alwaysTriggers)
			return CMLib.flags().canActAtAll(affecting);
		else
			return CMLib.flags().canFreelyBehaveNormal(affecting);
	}

	protected String parseLoads(String text, int depth, Vector<String> filenames, StringBuffer nonFilenameScript)
	{
		final StringBuffer results=new StringBuffer("");
		String parse=text;
		if(depth>10) return "";  // no including off to infinity
		String p=null;
		while(parse.length()>0)
		{
			final int y=parse.toUpperCase().indexOf("LOAD=");
			if(y>=0)
			{
				p=parse.substring(0,y).trim();
				if((!p.endsWith(";"))
				&&(!p.endsWith("\n"))
				&&(!p.endsWith("~"))
				&&(!p.endsWith("\r"))
				&&(p.length()>0))
				{
					if(nonFilenameScript!=null)
						nonFilenameScript.append(parse.substring(0,y+1));
					results.append(parse.substring(0,y+1));
					parse=parse.substring(y+1);
					continue;
				}
				results.append(p+"\n");
				int z=parse.indexOf('~',y);
				while((z>0)&&(parse.charAt(z-1)=='\\'))
					z=parse.indexOf('~',z+1);
				if(z>0)
				{
					final String filename=parse.substring(y+5,z).trim();
					parse=parse.substring(z+1);
					if((filenames!=null)&&(!filenames.contains(filename)))
						filenames.addElement(filename);
					results.append(parseLoads(getResourceFileData(filename, true).toString(),depth+1,filenames,null));
				}
				else
				{
					final String filename=parse.substring(y+5).trim();
					if((filenames!=null)&&(!filenames.contains(filename)))
						filenames.addElement(filename);
					results.append(parseLoads(getResourceFileData(filename, true).toString(),depth+1,filenames,null));
					break;
				}
			}
			else
			{
				if(nonFilenameScript!=null)
					nonFilenameScript.append(parse);
				results.append(parse);
				break;
			}
		}
		return results.toString();
	}

	protected void buildHashes()
	{
		synchronized(funcH)
		{
			if(funcH.size()==0)
			{
				for(int i=0;i<funcs.length;i++)
					funcH.put(funcs[i],Integer.valueOf(i+1));
				for(int i=0;i<methods.length;i++)
					methH.put(methods[i],Integer.valueOf(i+1));
				for(int i=0;i<progs.length;i++)
					progH.put(progs[i],Integer.valueOf(i+1));
				for(int i=0;i<CONNECTORS.length;i++)
					connH.put(CONNECTORS[i],Integer.valueOf(i));
				for(int i=0;i<GSTATCODES_ADDITIONAL.length;i++)
					gstatH.put(GSTATCODES_ADDITIONAL[i],Integer.valueOf(i));
				for(int i=0;i<SIGNS.length;i++)
					signH.put(SIGNS[i],Integer.valueOf(i));
			}
		}
	}
	
	protected Vector<DVector> parseScripts(String text)
	{
		buildHashes();
		text=parseLoads(text,0,null,null);
		final List<List<String>> V = CMParms.parseDoubleDelimited(text,'~',';');
		final Vector<DVector> V2=new Vector<DVector>(2);
		for(final List<String> ls : V)
		{
			final DVector DV=new DVector(2);
			for(final String s : ls)
				DV.addElement(s,null);
			V2.add(DV);
		}
		return V2;
	}

	protected Room getRoom(String thisName, Room imHere)
	{
		if(thisName.length()==0)
			return null;
		if((imHere!=null)&&(imHere.roomID().equalsIgnoreCase(thisName)))
			return imHere;
		if((imHere!=null)&&(thisName.startsWith("#"))&&(CMath.isLong(thisName.substring(1))))
			return CMLib.map().getRoom(imHere.getArea().Name()+thisName);
		final Room room=CMLib.map().getRoom(thisName);
		if((room!=null)&&(room.roomID().equalsIgnoreCase(thisName)))
		{
			if(CMath.bset(room.getArea().flags(),Area.FLAG_INSTANCE_PARENT)
			&&(imHere!=null)
			&&(CMath.bset(imHere.getArea().flags(),Area.FLAG_INSTANCE_CHILD))
			&&(imHere.getArea().Name().endsWith("_"+room.getArea().Name()))
			&&(thisName.indexOf('#')>=0))
			{
				final Room otherRoom=CMLib.map().getRoom(imHere.getArea().Name()+thisName.substring(thisName.indexOf('#')));
				if((otherRoom!=null)&&(otherRoom.roomID().endsWith(thisName)))
					return otherRoom;
			}
			return room;
		}

		List<Room> rooms=new Vector<Room>(1);
		if((imHere!=null)&&(imHere.getArea()!=null))
			rooms=CMLib.map().findAreaRoomsLiberally(null, imHere.getArea(), thisName, "RIEPM",100);
		if(rooms.size()==0)
		{
			if(debugBadScripts)
				Log.debugOut("ScriptingEngine","World room search called for: "+thisName);
			rooms=CMLib.map().findWorldRoomsLiberally(null,thisName, "RIEPM",100,2000);
		}
		if(rooms.size()>0)
			return rooms.get(CMLib.dice().roll(1,rooms.size(),-1));
		if(room == null)
		{
			final int x=thisName.indexOf('@');
			if(x>0)
			{
				Room R=CMLib.map().getRoom(thisName.substring(x+1));
				if((R==null)||(R==imHere))
				{
					final Area A=CMLib.map().getArea(thisName.substring(x+1));
					R=(A!=null)?A.getRandomMetroRoom():null;
				}
				if((R!=null)&&(R!=imHere))
					return getRoom(thisName.substring(0,x),R);
			}
		}
		return room;
	}

	protected void logError(Environmental scripted, String cmdName, String errType, String errMsg)
	{
		if(scripted!=null)
		{
			final Room R=CMLib.map().roomLocation(scripted);
			Log.errOut("Scripting",scripted.name()+"/"+CMLib.map().getDescriptiveExtendedRoomID(R)+"/"+ cmdName+"/"+errType+"/"+errMsg);
			if(R!=null)
				R.showHappens(CMMsg.MSG_OK_VISUAL,L("Scripting Error: @x1/@x2/@x3/@x4/@x5/@x6",scripted.name(),CMLib.map().getExtendedRoomID(R),CMParms.toListString(externalFiles()),cmdName,errType,errMsg));
		}
		else
			Log.errOut("Scripting","*/*/"+CMParms.toListString(externalFiles())+"/"+cmdName+"/"+errType+"/"+errMsg);

	}

	protected boolean simpleEvalStr(Environmental scripted, String arg1, String arg2, String cmp, String cmdName)
	{
		final int x=arg1.compareToIgnoreCase(arg2);
		final Integer SIGN=signH.get(cmp);
		if(SIGN==null)
		{
			logError(scripted,cmdName,"Syntax",arg1+" "+cmp+" "+arg2);
			return false;
		}
		switch(SIGN.intValue())
		{
		case SIGN_EQUL:
			return (x == 0);
		case SIGN_EQGT:
		case SIGN_GTEQ:
			return (x == 0) || (x > 0);
		case SIGN_EQLT:
		case SIGN_LTEQ:
			return (x == 0) || (x < 0);
		case SIGN_GRAT:
			return (x > 0);
		case SIGN_LEST:
			return (x < 0);
		case SIGN_NTEQ:
			return (x != 0);
		default:
			return (x == 0);
		}
	}

	protected boolean simpleEval(Environmental scripted, String arg1, String arg2, String cmp, String cmdName)
	{
		final long val1=CMath.s_long(arg1.trim());
		final long val2=CMath.s_long(arg2.trim());
		final Integer SIGN=signH.get(cmp);
		if(SIGN==null)
		{
			logError(scripted,cmdName,"Syntax",val1+" "+cmp+" "+val2);
			return false;
		}
		switch(SIGN.intValue())
		{
		case SIGN_EQUL:
			return (val1 == val2);
		case SIGN_EQGT:
		case SIGN_GTEQ:
			return val1 >= val2;
		case SIGN_EQLT:
		case SIGN_LTEQ:
			return val1 <= val2;
		case SIGN_GRAT:
			return (val1 > val2);
		case SIGN_LEST:
			return (val1 < val2);
		case SIGN_NTEQ:
			return (val1 != val2);
		default:
			return (val1 == val2);
		}
	}

	protected boolean simpleExpressionEval(Environmental scripted, String arg1, String arg2, String cmp, String cmdName)
	{
		final double val1=CMath.s_parseMathExpression(arg1.trim());
		final double val2=CMath.s_parseMathExpression(arg2.trim());
		final Integer SIGN=signH.get(cmp);
		if(SIGN==null)
		{
			logError(scripted,cmdName,"Syntax",val1+" "+cmp+" "+val2);
			return false;
		}
		switch(SIGN.intValue())
		{
		case SIGN_EQUL:
			return (val1 == val2);
		case SIGN_EQGT:
		case SIGN_GTEQ:
			return val1 >= val2;
		case SIGN_EQLT:
		case SIGN_LTEQ:
			return val1 <= val2;
		case SIGN_GRAT:
			return (val1 > val2);
		case SIGN_LEST:
			return (val1 < val2);
		case SIGN_NTEQ:
			return (val1 != val2);
		default:
			return (val1 == val2);
		}
	}

	protected List<PhysicalAgent> loadMobsFromFile(Environmental scripted, String filename)
	{
		filename=filename.trim();
		List monsters=(List)Resources.getResource("RANDOMMONSTERS-"+filename);
		if(monsters!=null)
			return monsters;
		final StringBuffer buf=getResourceFileData(filename, true);
		String thangName="null";
		final Room R=CMLib.map().roomLocation(scripted);
		if(R!=null)
			thangName=scripted.name()+" at "+CMLib.map().getExtendedRoomID((Room)scripted);
		else
		if(scripted!=null)
			thangName=scripted.name();
		if((buf==null)||(buf.length()<20))
		{
			logError(scripted,"XMLLOAD","?","Unknown XML file: '"+filename+"' in "+thangName);
			return null;
		}
		final List<XMLLibrary.XMLTag> xml=CMLib.xml().parseAllXML(buf.toString());
		monsters=new Vector<PhysicalAgent>();
		if(xml!=null)
		{
			if(CMLib.xml().getContentsFromPieces(xml,"MOBS")!=null)
			{
				final String error=CMLib.coffeeMaker().addMOBsFromXML(xml,monsters,null);
				if(error.length()>0)
				{
					logError(scripted,"XMLLOAD","?","Error in XML file: '"+filename+"'");
					return null;
				}
				if(monsters.size()<=0)
				{
					logError(scripted,"XMLLOAD","?","Empty XML file: '"+filename+"'");
					return null;
				}
				Resources.submitResource("RANDOMMONSTERS-"+filename,monsters);
			}
			else
			{
				logError(scripted,"XMLLOAD","?","No MOBs in XML file: '"+filename+"' in "+thangName);
				return null;
			}
		}
		else
		{
			logError(scripted,"XMLLOAD","?","Invalid XML file: '"+filename+"' in "+thangName);
			return null;
		}
		return monsters;
	}

	protected List<PhysicalAgent> generateMobsFromFile(Environmental scripted, String filename, String tagName, String rest)
	{
		filename=filename.trim();
		List monsters=(List)Resources.getResource("RANDOMGENMONSTERS-"+filename+"."+tagName+"-"+rest);
		if(monsters!=null)
			return monsters;
		final StringBuffer buf=getResourceFileData(filename, true);
		String thangName="null";
		final Room R=CMLib.map().roomLocation(scripted);
		if(R!=null)
			thangName=scripted.name()+" at "+CMLib.map().getExtendedRoomID((Room)scripted);
		else
		if(scripted!=null)
			thangName=scripted.name();
		if((buf==null)||(buf.length()<20))
		{
			logError(scripted,"XMLLOAD","?","Unknown XML file: '"+filename+"' in "+thangName);
			return null;
		}
		final List<XMLLibrary.XMLTag> xml=CMLib.xml().parseAllXML(buf.toString());
		monsters=new Vector<PhysicalAgent>();
		if(xml!=null)
		{
			if(CMLib.xml().getContentsFromPieces(xml,"AREADATA")!=null)
			{
				final Hashtable<String,Object> definedIDs = new Hashtable<String,Object>();
				final Map<String,String> eqParms=new HashMap<String,String>();
				eqParms.putAll(CMParms.parseEQParms(rest.trim()));
				final String idName=tagName.toUpperCase();
				try
				{
					CMLib.percolator().buildDefinedIDSet(xml,definedIDs);
					if((!(definedIDs.get(idName) instanceof XMLTag))
					||(!((XMLTag)definedIDs.get(idName)).tag().equalsIgnoreCase("MOB")))
					{
						logError(scripted,"XMLLOAD","?","Non-MOB tag '"+idName+"' for XML file: '"+filename+"' in "+thangName);
						return null;
					}
					final XMLTag piece=(XMLTag)definedIDs.get(idName);
					definedIDs.putAll(eqParms);
					try
					{
						CMLib.percolator().checkRequirements(piece, definedIDs);
					}
					catch(final CMException cme)
					{
						logError(scripted,"XMLLOAD","?","Required ids for "+idName+" were missing for XML file: '"+filename+"' in "+thangName+": "+cme.getMessage());
						return null;
					}
					CMLib.percolator().preDefineReward(piece, definedIDs);
					CMLib.percolator().defineReward(piece,definedIDs);
					monsters.addAll(CMLib.percolator().findMobs(piece, definedIDs));
					CMLib.percolator().postProcess(definedIDs);
					if(monsters.size()<=0)
					{
						logError(scripted,"XMLLOAD","?","Empty XML file: '"+filename+"'");
						return null;
					}
					Resources.submitResource("RANDOMGENMONSTERS-"+filename+"."+tagName+"-"+rest,monsters);
				}
				catch(final CMException cex)
				{
					logError(scripted,"XMLLOAD","?","Unable to generate "+idName+" from XML file: '"+filename+"' in "+thangName+": "+cex.getMessage());
					return null;
				}
			}
			else
			{
				logError(scripted,"XMLLOAD","?","Invalid GEN XML file: '"+filename+"' in "+thangName);
				return null;
			}
		}
		else
		{
			logError(scripted,"XMLLOAD","?","Empty or Invalid XML file: '"+filename+"' in "+thangName);
			return null;
		}
		return monsters;
	}

	protected List<PhysicalAgent> loadItemsFromFile(Environmental scripted, String filename)
	{
		filename=filename.trim();
		List items=(List)Resources.getResource("RANDOMITEMS-"+filename);
		if(items!=null)
			return items;
		final StringBuffer buf=getResourceFileData(filename, true);
		String thangName="null";
		final Room R=CMLib.map().roomLocation(scripted);
		if(R!=null)
			thangName=scripted.name()+" at "+CMLib.map().getExtendedRoomID((Room)scripted);
		else
		if(scripted!=null)
			thangName=scripted.name();
		if((buf==null)||(buf.length()<20))
		{
			logError(scripted,"XMLLOAD","?","Unknown XML file: '"+filename+"' in "+thangName);
			return null;
		}
		items=new Vector<PhysicalAgent>();
		final List<XMLLibrary.XMLTag> xml=CMLib.xml().parseAllXML(buf.toString());
		if(xml!=null)
		{
			if(CMLib.xml().getContentsFromPieces(xml,"ITEMS")!=null)
			{
				final String error=CMLib.coffeeMaker().addItemsFromXML(buf.toString(),items,null);
				if(error.length()>0)
				{
					logError(scripted,"XMLLOAD","?","Error in XML file: '"+filename+"' in "+thangName);
					return null;
				}
				if(items.size()<=0)
				{
					logError(scripted,"XMLLOAD","?","Empty XML file: '"+filename+"'");
					return null;
				}
				Resources.submitResource("RANDOMITEMS-"+filename,items);
			}
			else
			{
				logError(scripted,"XMLLOAD","?","No ITEMS in XML file: '"+filename+"' in "+thangName);
				return null;
			}
		}
		else
		{
			logError(scripted,"XMLLOAD","?","Empty or invalid XML file: '"+filename+"' in "+thangName);
			return null;
		}
		return items;
	}

	protected List<PhysicalAgent> generateItemsFromFile(Environmental scripted, String filename, String tagName, String rest)
	{
		filename=filename.trim();
		List items=(List)Resources.getResource("RANDOMGENITEMS-"+filename+"."+tagName+"-"+rest);
		if(items!=null)
			return items;
		final StringBuffer buf=getResourceFileData(filename, true);
		String thangName="null";
		final Room R=CMLib.map().roomLocation(scripted);
		if(R!=null)
			thangName=scripted.name()+" at "+CMLib.map().getExtendedRoomID((Room)scripted);
		else
		if(scripted!=null)
			thangName=scripted.name();
		if((buf==null)||(buf.length()<20))
		{
			logError(scripted,"XMLLOAD","?","Unknown XML file: '"+filename+"' in "+thangName);
			return null;
		}
		items=new Vector<PhysicalAgent>();
		final List<XMLLibrary.XMLTag> xml=CMLib.xml().parseAllXML(buf.toString());
		if(xml!=null)
		{
			if(CMLib.xml().getContentsFromPieces(xml,"AREADATA")!=null)
			{
				final Hashtable<String,Object> definedIDs = new Hashtable<String,Object>();
				final Map<String,String> eqParms=new HashMap<String,String>();
				eqParms.putAll(CMParms.parseEQParms(rest.trim()));
				final String idName=tagName.toUpperCase();
				try
				{
					CMLib.percolator().buildDefinedIDSet(xml,definedIDs);
					if((!(definedIDs.get(idName) instanceof XMLTag))
					||(!((XMLTag)definedIDs.get(idName)).tag().equalsIgnoreCase("ITEM")))
					{
						logError(scripted,"XMLLOAD","?","Non-ITEM tag '"+idName+"' for XML file: '"+filename+"' in "+thangName);
						return null;
					}
					final XMLTag piece=(XMLTag)definedIDs.get(idName);
					definedIDs.putAll(eqParms);
					try
					{
						CMLib.percolator().checkRequirements(piece, definedIDs);
					}
					catch(final CMException cme)
					{
						logError(scripted,"XMLLOAD","?","Required ids for "+idName+" were missing for XML file: '"+filename+"' in "+thangName+": "+cme.getMessage());
						return null;
					}
					CMLib.percolator().preDefineReward(piece, definedIDs);
					CMLib.percolator().defineReward(piece,definedIDs);
					items.addAll(CMLib.percolator().findItems(piece, definedIDs));
					CMLib.percolator().postProcess(definedIDs);
					if(items.size()<=0)
					{
						logError(scripted,"XMLLOAD","?","Empty XML file: '"+filename+"' in "+thangName);
						return null;
					}
					Resources.submitResource("RANDOMGENITEMS-"+filename+"."+tagName+"-"+rest,items);
				}
				catch(final CMException cex)
				{
					logError(scripted,"XMLLOAD","?","Unable to generate "+idName+" from XML file: '"+filename+"' in "+thangName+": "+cex.getMessage());
					return null;
				}
			}
			else
			{
				logError(scripted,"XMLLOAD","?","Not a GEN XML file: '"+filename+"' in "+thangName);
				return null;
			}
		}
		else
		{
			logError(scripted,"XMLLOAD","?","Empty or Invalid XML file: '"+filename+"' in "+thangName);
			return null;
		}
		return items;
	}

	protected Environmental findSomethingCalledThis(String thisName, MOB meMOB, Room imHere, List<Environmental> OBJS, boolean mob)
	{
		if(thisName.length()==0)
			return null;
		Environmental thing=null;
		Environmental areaThing=null;
		if(thisName.toUpperCase().trim().startsWith("FROMFILE "))
		{
			try
			{
				List V=null;
				if(mob)
					V=loadMobsFromFile(null,CMParms.getCleanBit(thisName,1));
				else
					V=loadItemsFromFile(null,CMParms.getCleanBit(thisName,1));
				if(V!=null)
				{
					final String name=CMParms.getPastBitClean(thisName,1);
					if(name.equalsIgnoreCase("ALL"))
						OBJS=V;
					else
					if(name.equalsIgnoreCase("ANY"))
					{
						if(V.size()>0)
							areaThing=(Environmental)V.get(CMLib.dice().roll(1,V.size(),-1));
					}
					else
					{
						areaThing=CMLib.english().fetchEnvironmental(V,name,true);
						if(areaThing==null)
							areaThing=CMLib.english().fetchEnvironmental(V,name,false);
					}
				}
			}
			catch(final Exception e)
			{
			}
		}
		else
		if(thisName.toUpperCase().trim().startsWith("FROMGENFILE "))
		{
			try
			{
				List V=null;
				final String filename=CMParms.getCleanBit(thisName, 1);
				final String name=CMParms.getCleanBit(thisName, 2);
				final String tagName=CMParms.getCleanBit(thisName, 3);
				final String theRest=CMParms.getPastBitClean(thisName,3);
				if(mob)
					V=generateMobsFromFile(null,filename, tagName, theRest);
				else
					V=generateItemsFromFile(null,filename, tagName, theRest);
				if(V!=null)
				{
					if(name.equalsIgnoreCase("ALL"))
						OBJS=V;
					else
					if(name.equalsIgnoreCase("ANY"))
					{
						if(V.size()>0)
							areaThing=(Environmental)V.get(CMLib.dice().roll(1,V.size(),-1));
					}
					else
					{
						areaThing=CMLib.english().fetchEnvironmental(V,name,true);
						if(areaThing==null)
							areaThing=CMLib.english().fetchEnvironmental(V,name,false);
					}
				}
			}
			catch(final Exception e)
			{
			}
		}
		else
		{
			if(!mob)
				areaThing=(meMOB!=null)?meMOB.findItem(thisName):null;
			try
			{
				if(areaThing==null)
				{
					final Area A=imHere.getArea();
					final Vector<Environmental> all=new Vector<Environmental>();
					if(mob)
					{
						all.addAll(CMLib.map().findInhabitants(A.getProperMap(),null,thisName,100));
						if(all.size()==0)
							all.addAll(CMLib.map().findShopStock(A.getProperMap(), null, thisName,100));
						for(int a=all.size()-1;a>=0;a--)
						{
							if(!(all.elementAt(a) instanceof MOB))
								all.removeElementAt(a);
						}
						if(all.size()>0)
							areaThing=all.elementAt(CMLib.dice().roll(1,all.size(),-1));
						else
						{
							all.addAll(CMLib.map().findInhabitantsFavorExact(CMLib.map().rooms(),null,thisName,false,100));
							if(all.size()==0)
								all.addAll(CMLib.map().findShopStock(CMLib.map().rooms(), null, thisName,100));
							for(int a=all.size()-1;a>=0;a--)
							{
								if(!(all.elementAt(a) instanceof MOB))
									all.removeElementAt(a);
							}
							if(all.size()>0)
								thing=all.elementAt(CMLib.dice().roll(1,all.size(),-1));
						}
					}
					if(all.size()==0)
					{
						all.addAll(CMLib.map().findRoomItems(A.getProperMap(), null,thisName,true,100));
						if(all.size()==0)
							all.addAll(CMLib.map().findInventory(A.getProperMap(), null,thisName,100));
						if(all.size()==0)
							all.addAll(CMLib.map().findShopStock(A.getProperMap(), null,thisName,100));
						if(all.size()>0)
							areaThing=all.elementAt(CMLib.dice().roll(1,all.size(),-1));
						else
						{
							all.addAll(CMLib.map().findRoomItems(CMLib.map().rooms(), null,thisName,true,100));
							if(all.size()==0)
								all.addAll(CMLib.map().findInventory(CMLib.map().rooms(), null,thisName,100));
							if(all.size()==0)
								all.addAll(CMLib.map().findShopStock(CMLib.map().rooms(), null,thisName,100));
							if(all.size()>0)
								thing=all.elementAt(CMLib.dice().roll(1,all.size(),-1));
						}
					}
				}
			}
			catch(final NoSuchElementException nse)
			{
			}
		}
		if(areaThing!=null)
			OBJS.add(areaThing);
		else
		if(thing!=null)
			OBJS.add(thing);
		if(OBJS.size()>0)
			return OBJS.get(0);
		return null;
	}

	protected PhysicalAgent getArgumentMOB(String str,
												MOB source,
												MOB monster,
												Environmental target,
												Item primaryItem,
												Item secondaryItem,
												String msg,
												Object[] tmp)
	{
		return getArgumentItem(str,source,monster,monster,target,primaryItem,secondaryItem,msg,tmp);
	}

	protected PhysicalAgent getArgumentItem(String str,
											MOB source,
											MOB monster,
											PhysicalAgent scripted,
											Environmental target,
											Item primaryItem,
											Item secondaryItem,
											String msg,
											Object[] tmp)
	{
		if(str.length()<2)
			return null;
		if(str.charAt(0)=='$')
		{
			if(Character.isDigit(str.charAt(1)))
			{
				Object O=tmp[CMath.s_int(Character.toString(str.charAt(1)))];
				if(O instanceof PhysicalAgent)
					return (PhysicalAgent)O;
				else
				if((O instanceof List)&&(str.length()>3)&&(str.charAt(2)=='.'))
				{
					final List V=(List)O;
					String back=str.substring(2);
					if(back.charAt(1)=='$')
						back=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,back);
					if((back.length()>1)&&Character.isDigit(back.charAt(1)))
					{
						int x=1;
						while((x<back.length())&&(Character.isDigit(back.charAt(x))))
							x++;
						final int y=CMath.s_int(back.substring(1,x).trim());
						if((V.size()>0)&&(y>=0))
						{
							if(y>=V.size())
								return null;
							O=V.get(y);
							if(O instanceof PhysicalAgent)
								return (PhysicalAgent)O;
						}
						str=O.toString(); // will fall through
					}
				}
				else
				if(O!=null)
					str=O.toString(); // will fall through
				else
					return null;
			}
			else
			switch(str.charAt(1))
			{
				case 'a':
					return (lastKnownLocation != null) ? lastKnownLocation.getArea() : null;
				case 'B':
				case 'b':
					return (lastLoaded instanceof PhysicalAgent) ? (PhysicalAgent) lastLoaded : null;
				case 'N':
				case 'n':
					return ((source == backupMOB) && (backupMOB != null) && (monster != scripted)) ? scripted : source;
				case 'I':
				case 'i':
					return scripted;
				case 'T':
				case 't':
					return ((target == backupMOB) && (backupMOB != null) && (monster != scripted)) 
							? scripted : (target instanceof PhysicalAgent) ? (PhysicalAgent) target : null;
				case 'O':
				case 'o':
					return primaryItem;
				case 'P':
				case 'p':
					return secondaryItem;
				case 'd':
				case 'D':
					return lastKnownLocation;
				case 'F':
				case 'f':
					if ((monster != null) && (monster.amFollowing() != null))
						return monster.amFollowing();
					return null;
				case 'r':
				case 'R':
					return getRandPC(monster, tmp, lastKnownLocation);
				case 'c':
				case 'C':
					return getRandAnyone(monster, tmp, lastKnownLocation);
				case 'w':
					return primaryItem != null ? primaryItem.owner() : null;
				case 'W':
					return secondaryItem != null ? secondaryItem.owner() : null;
				case 'x':
				case 'X':
					if (lastKnownLocation != null)
					{
						if ((str.length() > 2) && (CMLib.directions().getGoodDirectionCode("" + str.charAt(2)) >= 0))
							return lastKnownLocation.getExitInDir(CMLib.directions().getGoodDirectionCode("" + str.charAt(2)));
						int i = 0;
						Exit E = null;
						while (((++i) < 100) || (E != null))
							E = lastKnownLocation.getExitInDir(CMLib.dice().roll(1, Directions.NUM_DIRECTIONS(), -1));
						return E;
					}
					return null;
				case '[':
				{
					final int x = str.substring(2).indexOf(']');
					if (x >= 0)
					{
						String mid = str.substring(2).substring(0, x);
						final int y = mid.indexOf(' ');
						if (y > 0)
						{
							final int num = CMath.s_int(mid.substring(0, y).trim());
							mid = mid.substring(y + 1).trim();
							final Quest Q = getQuest(mid);
							if (Q != null)
								return Q.getQuestItem(num);
						}
					}
					break;
				}
				case '{':
				{
					final int x = str.substring(2).indexOf('}');
					if (x >= 0)
					{
						String mid = str.substring(2).substring(0, x).trim();
						final int y = mid.indexOf(' ');
						if (y > 0)
						{
							final int num = CMath.s_int(mid.substring(0, y).trim());
							mid = mid.substring(y + 1).trim();
							final Quest Q = getQuest(mid);
							if (Q != null)
								return Q.getQuestMob(num);
						}
					}
					break;
				}
			}
		}
		if(lastKnownLocation!=null)
		{
			str=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,str);
			Environmental E=lastKnownLocation.fetchFromRoomFavorMOBs(null,str);
			if(E==null) 
				E=lastKnownLocation.fetchFromMOBRoomFavorsItems(monster,null,str,Wearable.FILTER_ANY);
			if(E==null) 
				E=lastKnownLocation.findItem(str);
			if((E==null)&&(monster!=null)) 
				E=monster.findItem(str);
			if(E==null) 
				E=CMLib.players().getPlayer(str);
			if((E==null)&&(source!=null))
				E=source.findItem(str);
			if(E instanceof PhysicalAgent)
				return (PhysicalAgent)E;
		}
		return null;
	}

	private String makeNamedString(Object O)
	{
		if(O instanceof List)
			return makeParsableString((List)O);
		else
		if(O instanceof Room)
			return ((Room)O).displayText(null);
		else
		if(O instanceof Environmental)
			return ((Environmental)O).Name();
		else
		if(O!=null)
			return O.toString();
		return "";
	}

	private String makeParsableString(List V)
	{
		if((V==null)||(V.size()==0))
			return "";
		if(V.get(0) instanceof String)
			return CMParms.combineQuoted(V,0);
		final StringBuffer ret=new StringBuffer("");
		String S=null;
		for(int v=0;v<V.size();v++)
		{
			S=makeNamedString(V.get(v)).trim();
			if(S.length()==0)
				ret.append("? ");
			else
			if(S.indexOf(' ')>=0)
				ret.append("\""+S+"\" ");
			else
				ret.append(S+" ");
		}
		return ret.toString();
	}

	@Override
	public String varify(final MOB source,
						 final Environmental target,
						 final PhysicalAgent scripted,
						 final MOB monster,
						 final Item primaryItem,
						 final Item secondaryItem,
						 final String msg,
						 final Object[] tmp,
						 String varifyable)
	{
		int t=varifyable.indexOf('$');
		if((monster!=null)&&(monster.location()!=null))
			lastKnownLocation=monster.location();
		if(lastKnownLocation==null)
			lastKnownLocation=source.location();
		MOB randMOB=null;
		while((t>=0)&&(t<varifyable.length()-1))
		{
			final char c=varifyable.charAt(t+1);
			String middle="";
			final String front=varifyable.substring(0,t);
			String back=varifyable.substring(t+2);
			if(Character.isDigit(c))
				middle=makeNamedString(tmp[CMath.s_int(Character.toString(c))]);
			else
			switch(c)
			{
				case '@':
					if ((t < varifyable.length() - 2) && Character.isLetter(varifyable.charAt(t + 2)))
					{
						final Environmental E = getArgumentItem("$" + varifyable.charAt(t + 2),
								source, monster, scripted, target, primaryItem, secondaryItem, msg, tmp);
						middle = (E == null) ? "null" : "" + E;
					}
					break;
				case 'a':
					if (lastKnownLocation != null)
						middle = lastKnownLocation.getArea().name();
					break;
				// case 'a':
				case 'A':
					// unnecessary, since, in coffeemud, this is part of the
					// name
					break;
				case 'b':
					middle = lastLoaded != null ? lastLoaded.name() : "";
					break;
				case 'B':
					middle = lastLoaded != null ? lastLoaded.displayText() : "";
					break;
				case 'c':
				case 'C':
					randMOB = getRandAnyone(monster, tmp, lastKnownLocation);
					if (randMOB != null)
						middle = randMOB.name();
					break;
				case 'd':
					middle = (lastKnownLocation != null) ? lastKnownLocation.displayText(monster) : "";
					break;
				case 'D':
					middle = (lastKnownLocation != null) ? lastKnownLocation.description(monster) : "";
					break;
				case 'e':
					if (source != null)
						middle = source.charStats().heshe();
					break;
				case 'E':
					if ((target != null) && (target instanceof MOB))
						middle = ((MOB) target).charStats().heshe();
					break;
				case 'f':
					if ((monster != null) && (monster.amFollowing() != null))
						middle = monster.amFollowing().name();
					break;
				case 'F':
					if ((monster != null) && (monster.amFollowing() != null))
						middle = monster.amFollowing().charStats().heshe();
					break;
				case 'g':
					middle = ((msg == null) ? "" : msg.toLowerCase());
					break;
				case 'G':
					middle = ((msg == null) ? "" : msg);
					break;
				case 'h':
					if (monster != null)
						middle = monster.charStats().himher();
					break;
				case 'H':
					randMOB = getRandPC(monster, tmp, lastKnownLocation);
					if (randMOB != null)
						middle = randMOB.charStats().himher();
					break;
				case 'i':
					if (monster != null)
						middle = monster.name();
					break;
				case 'I':
					if (monster != null)
						middle = monster.displayText();
					break;
				case 'j':
					if (monster != null)
						middle = monster.charStats().heshe();
					break;
				case 'J':
					randMOB = getRandPC(monster, tmp, lastKnownLocation);
					if (randMOB != null)
						middle = randMOB.charStats().heshe();
					break;
				case 'k':
					if (monster != null)
						middle = monster.charStats().hisher();
					break;
				case 'K':
					randMOB = getRandPC(monster, tmp, lastKnownLocation);
					if (randMOB != null)
						middle = randMOB.charStats().hisher();
					break;
				case 'l':
					if (lastKnownLocation != null)
					{
						final StringBuffer str = new StringBuffer("");
						for (int i = 0; i < lastKnownLocation.numInhabitants(); i++)
						{
							final MOB M = lastKnownLocation.fetchInhabitant(i);
							if ((M != null) && (M != monster) && (CMLib.flags().canBeSeenBy(M, monster)))
								str.append("\"" + M.name() + "\" ");
						}
						middle = str.toString();
					}
					break;
				case 'L':
					if (lastKnownLocation != null)
					{
						final StringBuffer str = new StringBuffer("");
						for (int i = 0; i < lastKnownLocation.numItems(); i++)
						{
							final Item I = lastKnownLocation.getItem(i);
							if ((I != null) && (I.container() == null) && (CMLib.flags().canBeSeenBy(I, monster)))
								str.append("\"" + I.name() + "\" ");
						}
						middle = str.toString();
					}
					break;
				case 'm':
					if (source != null)
						middle = source.charStats().hisher();
					break;
				case 'M':
					if ((target != null) && (target instanceof MOB))
						middle = ((MOB) target).charStats().hisher();
					break;
				case 'n':
				case 'N':
					if (source != null)
						middle = source.name();
					break;
				case 'o':
				case 'O':
					if (primaryItem != null)
						middle = primaryItem.name();
					break;
				case 'p':
				case 'P':
					if (secondaryItem != null)
						middle = secondaryItem.name();
					break;
				case 'r':
				case 'R':
					randMOB = getRandPC(monster, tmp, lastKnownLocation);
					if (randMOB != null)
						middle = randMOB.name();
					break;
				case 's':
					if (source != null)
						middle = source.charStats().himher();
					break;
				case 'S':
					if ((target != null) && (target instanceof MOB))
						middle = ((MOB) target).charStats().himher();
					break;
				case 't':
				case 'T':
					if (target != null)
						middle = target.name();
					break;
				case 'w':
					middle = primaryItem != null ? primaryItem.owner().Name() : middle;
					break;
				case 'W':
					middle = secondaryItem != null ? secondaryItem.owner().Name() : middle;
					break;
				case 'x':
				case 'X':
					if (lastKnownLocation != null)
					{
						middle = "";
						Exit E = null;
						int dir = -1;
						if ((t < varifyable.length() - 2) && (CMLib.directions().getGoodDirectionCode("" + varifyable.charAt(t + 2)) >= 0))
						{
							dir = CMLib.directions().getGoodDirectionCode("" + varifyable.charAt(t + 2));
							E = lastKnownLocation.getExitInDir(dir);
						}
						else
						{
							int i = 0;
							while (((++i) < 100) || (E != null))
							{
								dir = CMLib.dice().roll(1, Directions.NUM_DIRECTIONS(), -1);
								E = lastKnownLocation.getExitInDir(dir);
							}
						}
						if ((dir >= 0) && (E != null))
						{
							if (c == 'x')
								middle = CMLib.directions().getDirectionName(dir);
							else
								middle = E.name();
						}
					}
					break;
				case 'y':
					if (source != null)
						middle = source.charStats().sirmadam();
					break;
				case 'Y':
					if ((target != null) && (target instanceof MOB))
						middle = ((MOB) target).charStats().sirmadam();
					break;
				case '<':
				{
					final int x = back.indexOf('>');
					if (x >= 0)
					{
						String mid = back.substring(0, x);
						final int y = mid.indexOf(' ');
						Environmental E = null;
						String arg1 = "";
						if (y >= 0)
						{
							arg1 = mid.substring(0, y).trim();
							E = getArgumentItem(arg1, source, monster, monster, target, primaryItem, secondaryItem, msg, tmp);
							mid = mid.substring(y + 1).trim();
						}
						if (arg1.length() > 0)
							middle = getVar(E, arg1, mid, source, target, scripted, monster, primaryItem, secondaryItem, msg, tmp);
						back = back.substring(x + 1);
					}
					break;
				}
				case '[':
				{
					middle = "";
					final int x = back.indexOf(']');
					if (x >= 0)
					{
						String mid = back.substring(0, x);
						final int y = mid.indexOf(' ');
						if (y > 0)
						{
							final int num = CMath.s_int(mid.substring(0, y).trim());
							mid = mid.substring(y + 1).trim();
							final Quest Q = getQuest(mid);
							if (Q != null)
								middle = Q.getQuestItemName(num);
						}
						back = back.substring(x + 1);
					}
					break;
				}
				case '{':
				{
					middle = "";
					final int x = back.indexOf('}');
					if (x >= 0)
					{
						String mid = back.substring(0, x).trim();
						final int y = mid.indexOf(' ');
						if (y > 0)
						{
							final int num = CMath.s_int(mid.substring(0, y).trim());
							mid = mid.substring(y + 1).trim();
							final Quest Q = getQuest(mid);
							if (Q != null)
								middle = Q.getQuestMobName(num);
						}
						back = back.substring(x + 1);
					}
					break;
				}
				case '%':
				{
					middle = "";
					final int x = back.indexOf('%');
					if (x >= 0)
					{
						middle = functify(monster, source, target, monster, primaryItem, secondaryItem, msg, tmp, back.substring(0, x).trim());
						back = back.substring(x + 1);
					}
					break;
				}
			}
			if((back.startsWith("."))
			&&(back.length()>1))
			{
				if(back.charAt(1)=='$')
					back=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,back);
				if(back.equalsIgnoreCase(".LENGTH#"))
				{
					middle=""+CMParms.parse(middle).size();
					back="";
				}
				else
				if((back.length()>1)&&Character.isDigit(back.charAt(1)))
				{
					int x=1;
					while((x<back.length())
					&&(Character.isDigit(back.charAt(x))))
						x++;
					final int y=CMath.s_int(back.substring(1,x).trim());
					back=back.substring(x);
					final boolean rest=back.startsWith("..");
					if(rest)
						back=back.substring(2);
					final Vector<String> V=CMParms.parse(middle);
					if((V.size()>0)&&(y>=0))
					{
						if(y>=V.size())
							middle="";
						else
						if(rest)
							middle=CMParms.combine(V,y);
						else
							middle=V.elementAt(y);
					}
				}
			}
			varifyable=front+middle+back;
			t=varifyable.indexOf('$');
		}
		return varifyable;
	}

	protected DVector getScriptVarSet(String mobname, String varname)
	{
		final DVector set=new DVector(2);
		if(mobname.equals("*"))
		{
			for(final Iterator<String> k = resources._findResourceKeys("SCRIPTVAR-");k.hasNext();)
			{
				final String key=k.next();
				if(key.startsWith("SCRIPTVAR-"))
				{
					final Hashtable H=(Hashtable)resources._getResource(key);
					if(varname.equals("*"))
					{
						for(final Enumeration e=H.keys();e.hasMoreElements();)
						{
							final String vn=(String)e.nextElement();
							set.addElement(key.substring(10),vn);
						}
					}
					else
						set.addElement(key.substring(10),varname);
				}
			}
		}
		else
		{
			final Hashtable H=(Hashtable)resources._getResource("SCRIPTVAR-"+mobname);
			if(varname.equals("*"))
			{
				for(final Enumeration e=H.keys();e.hasMoreElements();)
				{
					final String vn=(String)e.nextElement();
					set.addElement(mobname,vn);
				}
			}
			else
				set.addElement(mobname,varname);
		}
		return set;
	}

	protected String getStatValue(Environmental E, String arg2)
	{
		boolean found=false;
		String val="";
		for(int i=0;i<E.getStatCodes().length;i++)
		{
			if(E.getStatCodes()[i].equalsIgnoreCase(arg2))
			{
				val=E.getStat(arg2);
				found=true;
				break;
			}
		}
		if((!found)&&(E instanceof MOB))
		{
			final MOB M=(MOB)E;
			for(final int i : CharStats.CODES.ALLCODES())
			{
				if(CharStats.CODES.NAME(i).equalsIgnoreCase(arg2)||CharStats.CODES.DESC(i).equalsIgnoreCase(arg2))
				{
					val=""+M.charStats().getStat(CharStats.CODES.NAME(i)); //yes, this is right
					found=true;
					break;
				}
			}
			if(!found)
			{
				for(int i=0;i<M.curState().getStatCodes().length;i++)
				{
					if(M.curState().getStatCodes()[i].equalsIgnoreCase(arg2))
					{
						val=M.curState().getStat(M.curState().getStatCodes()[i]);
						found=true;
						break;
					}
				}
			}
			if(!found)
			{
				for(int i=0;i<M.phyStats().getStatCodes().length;i++)
				{
					if(M.phyStats().getStatCodes()[i].equalsIgnoreCase(arg2))
					{
						val=M.phyStats().getStat(M.phyStats().getStatCodes()[i]);
						found=true;
						break;
					}
				}
			}
			if((!found)&&(M.playerStats()!=null))
			{
				for(int i=0;i<M.playerStats().getStatCodes().length;i++)
				{
					if(M.playerStats().getStatCodes()[i].equalsIgnoreCase(arg2))
					{
						val=M.playerStats().getStat(M.playerStats().getStatCodes()[i]);
						found=true;
						break;
					}
				}
			}
			if((!found)&&(arg2.toUpperCase().startsWith("BASE")))
			{
				for(int i=0;i<M.baseState().getStatCodes().length;i++)
				{
					if(M.baseState().getStatCodes()[i].equalsIgnoreCase(arg2.substring(4)))
					{
						val=M.baseState().getStat(M.baseState().getStatCodes()[i]);
						found=true;
						break;
					}
				}
			}
			if((!found)&&(gstatH.containsKey(arg2.toUpperCase())))
			{
				found=true;
				switch(gstatH.get(arg2.toUpperCase()).intValue())
				{
				case GSTATADD_STINK:
					if(M.playerStats()!=null)
						val=CMath.toPct(M.playerStats().getHygiene()/PlayerStats.HYGIENE_DELIMIT);
					break;
				case GSTATADD_DEITY:
					val = M.getWorshipCharID();
					break;
				case GSTATADD_CLAN:
				{
					Clan C = CMLib.clans().findRivalrousClan(M);
					if (C == null)
						C = M.clans().iterator().hasNext() ? M.clans().iterator().next().first : null;
					val = (C != null) ? C.clanID() : "";
					break;
				}
				case GSTATADD_CLANROLE:
				{
					Clan C = CMLib.clans().findRivalrousClan(M);
					if (C == null)
						C = M.clans().iterator().hasNext() ? M.clans().iterator().next().first : null;
					if (C != null)
					{
						final Pair<Clan, Integer> p = M.getClanRole(C.clanID());
						val = (p != null) ? p.second.toString() : "";
					}
					break;
				}
				}
			}
		}
		if(!found)
			return null;
		return val;
	}

	protected String getGStatValue(Environmental E, String arg2)
	{
		if(E==null)
			return null;
		boolean found=false;
		String val="";
		for(int i=0;i<E.getStatCodes().length;i++)
		{
			if(E.getStatCodes()[i].equalsIgnoreCase(arg2))
			{
				val=E.getStat(arg2);
				found=true; break;
			}
		}
		if(!found)
		if(E instanceof MOB)
		{
			arg2=arg2.toUpperCase().trim();
			final GenericBuilder.GenMOBCode element = (GenericBuilder.GenMOBCode)CMath.s_valueOf(GenericBuilder.GenMOBCode.class,arg2);
			if(element != null)
			{
				val=CMLib.coffeeMaker().getGenMobStat((MOB)E,element.name());
				found=true; 
			}
			if(!found)
			{
				final MOB M=(MOB)E;
				for(final int i : CharStats.CODES.ALLCODES())
				{
					if(CharStats.CODES.NAME(i).equals(arg2)||CharStats.CODES.DESC(i).equals(arg2))
					{
						val=""+M.charStats().getStat(CharStats.CODES.NAME(i));
						found=true;
						break;
					}
				}
				if(!found)
				{
					for(int i=0;i<M.curState().getStatCodes().length;i++)
					{
						if(M.curState().getStatCodes()[i].equals(arg2))
						{
							val=M.curState().getStat(M.curState().getStatCodes()[i]);
							found=true;
							break;
						}
					}
				}
				if(!found)
				{
					for(int i=0;i<M.phyStats().getStatCodes().length;i++)
					{
						if(M.phyStats().getStatCodes()[i].equals(arg2))
						{
							val=M.phyStats().getStat(M.phyStats().getStatCodes()[i]);
							found=true;
							break;
						}
					}
				}
				if((!found)&&(M.playerStats()!=null))
				{
					for(int i=0;i<M.playerStats().getStatCodes().length;i++)
					{
						if(M.playerStats().getStatCodes()[i].equals(arg2))
						{
							val=M.playerStats().getStat(M.playerStats().getStatCodes()[i]);
							found=true;
							break;
						}
					}
				}
				if((!found)&&(arg2.toUpperCase().startsWith("BASE")))
				{
					for(int i=0;i<M.baseState().getStatCodes().length;i++)
					{
						if(M.baseState().getStatCodes()[i].equals(arg2.substring(4)))
						{
							val=M.baseState().getStat(M.baseState().getStatCodes()[i]);
							found=true;
							break;
						}
					}
				}
				if((!found)&&(gstatH.containsKey(arg2)))
				{
					found=true;
					switch(gstatH.get(arg2).intValue())
					{
						case GSTATADD_STINK:
							if(M.playerStats()!=null)
								val = CMath.toPct(M.playerStats().getHygiene()/PlayerStats.HYGIENE_DELIMIT);
							break;
						case GSTATADD_DEITY:
							val = M.getWorshipCharID();
							break;
						case GSTATADD_CLAN:
						{
							Clan C = CMLib.clans().findRivalrousClan(M);
							if (C == null)
								C = M.clans().iterator().hasNext() ? M.clans().iterator().next().first : null;
							val = (C != null) ? C.clanID() : "";
							break;
						}
						case GSTATADD_CLANROLE:
						{
							Clan C = CMLib.clans().findRivalrousClan(M);
							if (C == null)
								C = M.clans().iterator().hasNext() ? M.clans().iterator().next().first : null;
							if (C != null)
							{
								final Pair<Clan, Integer> p = M.getClanRole(C.clanID());
								val = (p != null) ? p.second.toString() : "";
							}
							break;
						}
					}
				}
			}
		}
		else
		if(E instanceof Item)
		{
			final GenericBuilder.GenItemCode code = (GenericBuilder.GenItemCode)CMath.s_valueOf(GenericBuilder.GenItemCode.class, arg2.toUpperCase().trim());
			if(code != null)
			{
				val=CMLib.coffeeMaker().getGenItemStat((Item)E,code.name());
				found=true; 
			}
		}
		if(found)
			return val;
		return null;
	}

	@Override
	public void setVar(String baseName, String key, String val)
	{
		final DVector V=getScriptVarSet(baseName,key);
		for(int v=0;v<V.size();v++)
		{
			final String name=(String)V.elementAt(v,1);
			key=((String)V.elementAt(v,2)).toUpperCase();
			Hashtable<String,String> H=(Hashtable)resources._getResource("SCRIPTVAR-"+name);
			if((H==null)&&(defaultQuestName!=null)&&(defaultQuestName.length()>0))
			{
				final MOB M=CMLib.players().getPlayer(name);
				if(M!=null)
				{
					for(final Enumeration<ScriptingEngine> e=M.scripts();e.hasMoreElements();)
					{
						final ScriptingEngine SE=e.nextElement();
						if((SE!=null)
						&&(SE!=this)
						&&(defaultQuestName.equalsIgnoreCase(SE.defaultQuestName()))
						&&(SE.isVar(name,key)))
						{
							SE.setVar(name,key,val);
							return;
						}
					}
				}
			}
			if(H==null)
			{
				if(val.length()==0)
					continue;

				H=new Hashtable<String,String>();
				resources._submitResource("SCRIPTVAR-"+name,H);
			}
			if(val.equals("++"))
			{
				String num=H.get(key);
				if(num==null)
					num="0";
				val=Integer.toString(CMath.s_int(num.trim())+1);
			}
			else
			if(val.equals("--"))
			{
				String num=H.get(key);
				if(num==null)
					num="0";
				val=Integer.toString(CMath.s_int(num.trim())-1);
			}
			else
			if(val.startsWith("+"))
			{
				// add via +number form
				val=val.substring(1);
				final int amount=CMath.s_int(val.trim());
				String num=H.get(key);
				if(num==null)
					num="0";
				val=Integer.toString(CMath.s_int(num.trim())+amount);
			}
			else
			if(val.startsWith("-"))
			{
				// subtract -number form
				val=val.substring(1);
				final int amount=CMath.s_int(val.trim());
				String num=H.get(key);
				if(num==null)
					num="0";
				val=Integer.toString(CMath.s_int(num.trim())-amount);
			}
			else
			if(val.startsWith("*"))
			{
				// multiply via *number form
				val=val.substring(1);
				final int amount=CMath.s_int(val.trim());
				String num=H.get(key);
				if(num==null)
					num="0";
				val=Integer.toString(CMath.s_int(num.trim())*amount);
			}
			else
			if(val.startsWith("/"))
			{
				// divide /number form
				val=val.substring(1);
				final int amount=CMath.s_int(val.trim());
				String num=H.get(key);
				if(num==null)
					num="0";
				if(amount==0)
					Log.errOut("Scripting","Scripting SetVar error: Division by 0: "+name+"/"+key+"="+val);
				else
					val=Integer.toString(CMath.s_int(num.trim())/amount);
			}
			if(H.containsKey(key))
				H.remove(key);
			if(val.trim().length()>0)
				H.put(key,val);
			if(H.size()==0)
				resources._removeResource("SCRIPTVAR-"+name);
		}
	}

	@Override
	public String[] parseEval(String evaluable) throws ScriptParseException
	{
		final int STATE_MAIN=0;
		final int STATE_INFUNCTION=1;
		final int STATE_INFUNCQUOTE=2;
		final int STATE_POSTFUNCTION=3;
		final int STATE_POSTFUNCEVAL=4;
		final int STATE_POSTFUNCQUOTE=5;
		final int STATE_MAYFUNCTION=6;

		buildHashes();
		final Vector<String> V=new Vector<String>();
		if((evaluable==null)||(evaluable.trim().length()==0))
			return new String[]{};
		final char[] evalC=evaluable.toCharArray();
		int state=0;
		int dex=0;
		char lastQuote='\0';
		String s=null;
		int depth=0;
		for(int c=0;c<evalC.length;c++)
		{
			switch(state)
			{
			case STATE_MAIN:
			{
				if(Character.isWhitespace(evalC[c]))
				{
					s=new String(evalC,dex,c-dex).trim();
					if(s.length()>0)
					{
						s=s.toUpperCase();
						V.addElement(s);
						dex=c+1;
						if(funcH.containsKey(s))
							state=STATE_MAYFUNCTION;
						else
						if(!connH.containsKey(s))
							throw new ScriptParseException("Unknown keyword: "+s);
					}
				}
				else
				if(Character.isLetter(evalC[c])
				||(Character.isDigit(evalC[c])&&(c>0)&&Character.isLetter(evalC[c-1])&&(c<evalC.length-1)&&Character.isLetter(evalC[c+1])))
				{ /* move along */
				}
				else
				{
					switch(evalC[c])
					{
					case '!':
					{
						if(c==evalC.length-1)
							throw new ScriptParseException("Bad Syntax on last !");
						V.addElement("NOT");
						dex=c+1;
						break;
					}
					case '(':
					{
						s=new String(evalC,dex,c-dex).trim();
						if(s.length()>0)
						{
							s=s.toUpperCase();
							V.addElement(s);
							V.addElement("(");
							dex=c+1;
							if(funcH.containsKey(s))
								state=STATE_INFUNCTION;
							else
							if(connH.containsKey(s))
								state=STATE_MAIN;
							else
								throw new ScriptParseException("Unknown keyword: "+s);
						}
						else
						{
							V.addElement("(");
							depth++;
							dex=c+1;
						}
						break;
					}
					case ')':
						s=new String(evalC,dex,c-dex).trim();
						if(s.length()>0)
							throw new ScriptParseException("Bad syntax before ) at: "+s);
						if(depth==0)
							throw new ScriptParseException("Unmatched ) character");
						V.addElement(")");
						depth--;
						dex=c+1;
						break;
					default:
						throw new ScriptParseException("Unknown character at: "+new String(evalC,dex,c-dex+1).trim()+": "+evaluable);
					}
				}
				break;
			}
			case STATE_MAYFUNCTION:
			{
				if(evalC[c]=='(')
				{
					V.addElement("(");
					dex=c+1;
					state=STATE_INFUNCTION;
				}
				else
				if(!Character.isWhitespace(evalC[c]))
					throw new ScriptParseException("Expected ( at "+evalC[c]+": "+evaluable);
				break;
			}
			case STATE_POSTFUNCTION:
			{
				if(!Character.isWhitespace(evalC[c]))
				{
					switch(evalC[c])
					{
					case '=': case '>': case '<': case '!':
					{
						if(c==evalC.length-1)
							throw new ScriptParseException("Bad Syntax on last "+evalC[c]);
						if(!Character.isWhitespace(evalC[c+1]))
						{
							s=new String(evalC,c,2);
							if((!signH.containsKey(s))&&(evalC[c]!='!'))
								s=""+evalC[c];
						}
						else
							s=""+evalC[c];
						if(!signH.containsKey(s))
						{
							c=dex-1;
							state=STATE_MAIN;
							break;
						}
						V.addElement(s);
						dex=c+(s.length());
						c=c+(s.length()-1);
						state=STATE_POSTFUNCEVAL;
						break;
					}
					default:
						c=dex-1;
						state=STATE_MAIN;
						break;
					}
				}
				break;
			}
			case STATE_INFUNCTION:
			{
				if(evalC[c]==')')
				{
					V.addElement(new String(evalC,dex,c-dex));
					V.addElement(")");
					dex=c+1;
					state=STATE_POSTFUNCTION;
				}
				else
				if((evalC[c]=='\'')||(evalC[c]=='`'))
				{
					lastQuote=evalC[c];
					state=STATE_INFUNCQUOTE;
				}
				break;
			}
			case STATE_INFUNCQUOTE:
			{
				if(evalC[c]==lastQuote)
					state=STATE_INFUNCTION;
				break;
			}
			case STATE_POSTFUNCQUOTE:
			{
				if(evalC[c]==lastQuote)
				{
					if((V.size()>2)
					&&(signH.containsKey(V.lastElement()))
					&&(V.elementAt(V.size()-2).equals(")")))
					{
						final String sign=V.lastElement();
						V.removeElementAt(V.size()-1);
						V.removeElementAt(V.size()-1);
						final String prev=V.lastElement();
						if(prev.equals("("))
							s=sign+" "+new String(evalC,dex+1,c-dex);
						else
						{
							V.removeElementAt(V.size()-1);
							s=prev+" "+sign+" "+new String(evalC,dex+1,c-dex);
						}
						V.addElement(s);
						V.addElement(")");
						dex=c+1;
						state=STATE_MAIN;
					}
					else
						throw new ScriptParseException("Bad postfunc Eval somewhere");
				}
				break;
			}
			case STATE_POSTFUNCEVAL:
			{
				if(Character.isWhitespace(evalC[c]))
				{
					s=new String(evalC,dex,c-dex).trim();
					if(s.length()>0)
					{
						if((V.size()>1)
						&&(signH.containsKey(V.lastElement()))
						&&(V.elementAt(V.size()-2).equals(")")))
						{
							final String sign=V.lastElement();
							V.removeElementAt(V.size()-1);
							V.removeElementAt(V.size()-1);
							final String prev=V.lastElement();
							if(prev.equals("("))
								s=sign+" "+new String(evalC,dex+1,c-dex);
							else
							{
								V.removeElementAt(V.size()-1);
								s=prev+" "+sign+" "+new String(evalC,dex+1,c-dex);
							}
							V.addElement(s);
							V.addElement(")");
							dex=c+1;
							state=STATE_MAIN;
						}
						else
							throw new ScriptParseException("Bad postfunc Eval somewhere");
					}
				}
				else
				if(Character.isLetterOrDigit(evalC[c]))
				{ /* move along */
				}
				else
				if((evalC[c]=='\'')||(evalC[c]=='`'))
				{
					s=new String(evalC,dex,c-dex).trim();
					if(s.length()==0)
					{
						lastQuote=evalC[c];
						state=STATE_POSTFUNCQUOTE;
					}
				}
				break;
			}
			}
		}
		if((state==STATE_POSTFUNCQUOTE)
		||(state==STATE_INFUNCQUOTE))
			throw new ScriptParseException("Unclosed "+lastQuote+" somewhere");
		if(depth>0)
			throw new ScriptParseException("Unclosed ( somewhere");
		return CMParms.toStringArray(V);
	}

	public void pushEvalBoolean(Vector<Object> stack, boolean trueFalse)
	{
		if(stack.size()>0)
		{
			final Object O=stack.elementAt(stack.size()-1);
			if(O instanceof Integer)
			{
				final int connector=((Integer)O).intValue();
				stack.removeElementAt(stack.size()-1);
				if((stack.size()>0)
				&&((stack.elementAt(stack.size()-1) instanceof Boolean)))
				{
					final boolean preTrueFalse=((Boolean)stack.elementAt(stack.size()-1)).booleanValue();
					stack.removeElementAt(stack.size()-1);
					switch(connector)
					{
					case CONNECTOR_AND:
						trueFalse = preTrueFalse && trueFalse;
						break;
					case CONNECTOR_OR:
						trueFalse = preTrueFalse || trueFalse;
						break;
					case CONNECTOR_ANDNOT:
						trueFalse = preTrueFalse && (!trueFalse);
						break;
					case CONNECTOR_NOT:
					case CONNECTOR_ORNOT:
						trueFalse = preTrueFalse || (!trueFalse);
						break;
					}
				}
				else
				switch(connector)
				{
					case CONNECTOR_ANDNOT:
					case CONNECTOR_NOT:
					case CONNECTOR_ORNOT:
						trueFalse = !trueFalse;
						break;
					default:
						break;
				}
			}
			else
			if(O instanceof Boolean)
			{
				final boolean preTrueFalse=((Boolean)stack.elementAt(stack.size()-1)).booleanValue();
				stack.removeElementAt(stack.size()-1);
				trueFalse=preTrueFalse&&trueFalse;
			}
		}
		stack.addElement(trueFalse?Boolean.TRUE:Boolean.FALSE);
	}

	/**
	 * Returns the index, in the given string vector, of the given string, starting
	 * from the given index.  If the string to search for contains more than one
	 * "word", where a word is defined in space-delimited terms respecting double-quotes,
	 * then it will return the index at which all the words in the parsed search string
	 * are found in the given string list.
	 * @param V the string list to search in
	 * @param str the string to search for
	 * @param start the index to start at (0 is good)
	 * @return the index at which the search string was found in the string list, or -1
	 */
	private static int strIndex(final Vector<String> V, final String str, final int start)
	{
		if(str.indexOf(' ')<0) 
			return V.indexOf(str,start);
		final List<String> V2=CMParms.parse(str);
		if(V2.size()==0) 
			return -1;
		int x=V.indexOf(V2.get(0),start);
		boolean found=false;
		while((x>=0)&&((x+V2.size())<=V.size())&&(!found))
		{
			found=true;
			for(int v2=1;v2<V2.size();v2++)
			{
				if(!V.get(x+v2).equals(V2.get(v2)))
				{ 
					found=false; 
					break;
				}
			}
			if(!found) 
				x=V.indexOf(V2.get(0),x+1);
		}
		if(found) 
			return x;
		return -1;
	}

	/**
	 * Weird method.  Accepts a string list, a combiner (see below), a string buffer to search for, 
	 * and a previously found index. The stringbuffer is always cleared during this call.
	 * If the stringbuffer was empty, the previous found index is returned. Otherwise: 
	 * If the combiner is '&', the first index of the given stringbuffer in the string list is returned (or -1).  
	 * If the combiner is '|', the previous index is returned if it was found, otherwise the first index 
	 * of the given stringbuffer in the string list is returned (or -1).
	 * If the combiner is '&gt;', then the previous index is returned if it was not found (-1), otherwise the
	 * next highest found stringbuffer since the last string list search is returned, or (-1) if no more found.
	 * If the combiner is '&lt;', then the previous index is returned if it was not found (-1), otherwise the
	 * first found stringbuffer index is returned if it is lower than the previously found index.
	 * Other combiners return -1.
	 * @param V the string list to search
	 * @param combiner the combiner, either &,|,&lt;,or &gt;.
	 * @param buf the stringbuffer to search for, which is always cleared
	 * @param lastIndex the previously found index
	 * @return the result of the search
	 */
	private static int stringContains(final Vector<String> V, final char combiner, final StringBuffer buf, int lastIndex)
	{
		final String str=buf.toString().trim();
		if(str.length()==0) 
			return lastIndex;
		buf.setLength(0);
		switch (combiner)
		{
		case '&':
			lastIndex = strIndex(V, str, 0);
			return lastIndex;
		case '|':
			if (lastIndex >= 0)
				return lastIndex;
			return strIndex(V, str, 0);
		case '>':
			if (lastIndex < 0)
				return lastIndex;
			return strIndex(V, str, lastIndex + 1);
		case '<':
		{
			if (lastIndex < 0)
				return lastIndex;
			final int newIndex = strIndex(V, str, 0);
			if (newIndex < lastIndex)
				return newIndex;
			return -1;
		}
		}
		return -1;
	}
	
	/**
	 * Main workhorse of the stringcontains mobprog function.
	 * @param V parsed string to search
	 * @param str the coded search function
	 * @param index a 1-dim array of the index in the coded search str to start the search at
	 * @param depth the number of close parenthesis to expect
	 * @return the last index in the coded search function evaluated
	 */
	private static int stringContains(final Vector<String> V, final char[] str, final int[] index, final int depth)
	{
		final StringBuffer buf=new StringBuffer("");
		int lastIndex=0;
		boolean quoteMode=false;
		char combiner='&';
		for(int i=index[0];i<str.length;i++)
		{
			switch(str[i])
			{
			case ')':
				if((depth>0)&&(!quoteMode))
				{
					index[0]=i;
					return stringContains(V,combiner,buf,lastIndex);
				}
				buf.append(str[i]);
				break;
			case ' ':
				buf.append(str[i]);
				break;
			case '&':
			case '|':
			case '>':
			case '<':
				if(quoteMode)
					buf.append(str[i]);
				else
				{
					lastIndex=stringContains(V,combiner,buf,lastIndex);
					combiner=str[i];
				}
				break;
			case '(':
				if(!quoteMode)
				{
					lastIndex=stringContains(V,combiner,buf,lastIndex);
					index[0]=i+1;
					final int newIndex=stringContains(V,str,index,depth+1);
					i=index[0];
					switch(combiner)
					{
					case '&':
						if((lastIndex<0)||(newIndex<0))
							lastIndex=-1;
						break;
					case '|':
						if(newIndex>=0)
							lastIndex=newIndex;
						break;
					case '>':
						if(newIndex<=lastIndex)
							lastIndex=-1;
						else
							lastIndex=newIndex;
						break;
					case '<':
						if((newIndex<0)||(newIndex>=lastIndex))
							lastIndex=-1;
						else
							lastIndex=newIndex;
						break;
					}
				}
				else
					buf.append(str[i]);
				break;
			case '\"':
				quoteMode=(!quoteMode);
				break;
			case '\\':
				if(i<str.length-1)
				{
					buf.append(str[i+1]);
					i++;
				}
				break;
			default:
				if(Character.isLetter(str[i]))
					buf.append(Character.toLowerCase(str[i]));
				else
					buf.append(str[i]);
				break;
			}
		}
		return stringContains(V,combiner,buf,lastIndex);
	}

	/**
	 * As the name implies, this is the implementation of the stringcontains mobprog function
	 * @param str1 the string to search in
	 * @param str2 the coded search expression
	 * @return the index of the found string in the first string
	 */
	protected final static int stringContainsFunctionImpl(final String str1, final String str2)
	{
		final StringBuffer buf1=new StringBuffer(str1.toLowerCase());
		for(int i=buf1.length()-1;i>=0;i--)
		{
			if((buf1.charAt(i)!=' ')
			&&(buf1.charAt(i)!='\'')
			&&(buf1.charAt(i)!='\"')
			&&(buf1.charAt(i)!='`')
			&&(!Character.isLetterOrDigit(buf1.charAt(i))))
				buf1.setCharAt(i,' ');
		}
		final Vector<String> V=CMParms.parse(buf1.toString());
		return stringContains(V,str2.toCharArray(),new int[]{0},0);
	}

	@Override
	public boolean eval(final PhysicalAgent scripted,
						final MOB source,
						final Environmental target,
						final MOB monster,
						final Item primaryItem,
						final Item secondaryItem,
						final String msg,
						Object[] tmp,
						final String[][] eval,
						final int startEval)
	{
		String[] tt=eval[0];
		if(tmp == null)
			tmp = newObjs();
		final Vector<Object> stack=new Vector<Object>();
		for(int t=startEval;t<tt.length;t++)
		if(tt[t].equals("("))
			stack.addElement(tt[t]);
		else
		if(tt[t].equals(")"))
		{
			if((!(stack.lastElement() instanceof Boolean))
			||(stack.size()==1)
			||(!(stack.elementAt(stack.size()-2)).equals("(")))
			{
				logError(scripted,"EVAL","SYNTAX",") Format error: "+CMParms.toListString(tt));
				return false;
			}
			final boolean b=((Boolean)stack.lastElement()).booleanValue();
			stack.removeElementAt(stack.size()-1);
			stack.removeElementAt(stack.size()-1);
			pushEvalBoolean(stack,b);
		}
		else
		if(connH.containsKey(tt[t]))
		{
			Integer curr=connH.get(tt[t]);
			if((stack.size()>0)&&(stack.lastElement() instanceof Integer))
			{
				final int old=((Integer)stack.lastElement()).intValue();
				stack.removeElementAt(stack.size()-1);
				curr=Integer.valueOf(CONNECTOR_MAP[old][curr.intValue()]);
			}
			stack.addElement(curr);
		}
		else
		if(funcH.containsKey(tt[t]))
		{
			final Integer funcCode=funcH.get(tt[t]);
			if((t==tt.length-1)
			||(!tt[t+1].equals("(")))
			{
				logError(scripted,"EVAL","SYNTAX","No ( for fuction "+tt[t]+": "+CMParms.toListString(tt));
				return false;
			}
			t+=2;
			int tlen=0;
			while(((t+tlen)<tt.length)&&(!tt[t+tlen].equals(")")))
				tlen++;
			if((t+tlen)==tt.length)
			{
				logError(scripted,"EVAL","SYNTAX","No ) for fuction "+tt[t-1]+": "+CMParms.toListString(tt));
				return false;
			}
			tickStatus=Tickable.STATUS_MISC+funcCode.intValue();
			final String funcParms=tt[t];
			boolean returnable=false;
			switch(funcCode.intValue())
			{
			case 1: // rand
			{
				String num=funcParms;
				if(num.endsWith("%"))
					num=num.substring(0,num.length()-1);
				final int arg=CMath.s_int(num);
				if(CMLib.dice().rollPercentage()<arg)
					returnable=true;
				else
					returnable=false;
				break;
			}
			case 2: // has
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"cr"); /* tt[t+0] */
				final String arg1=tt[t+0];
				final String arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+1]);
				final Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if(arg2.length()==0)
				{
					logError(scripted,"HAS","Syntax",funcParms);
					return returnable;
				}
				final Environmental E2=getArgumentItem(arg2,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if(E==null)
					returnable=false;
				else
				if(E instanceof MOB)
				{
					if(E2!=null)
						returnable=((MOB)E).isMine(E2);
					else
						returnable=(((MOB)E).findItem(arg2)!=null);
				}
				else
				if(E instanceof Item)
					returnable=CMLib.english().containsString(E.name(),arg2);
				else
				if(E instanceof Room)
				{
					if(E2 instanceof Item)
						returnable=((Room)E).isContent((Item)E2);
					else
						returnable=(((Room)E).findItem(null,arg2)!=null);
				}
				else
					returnable=false;
				break;
			}
			case 74: // hasnum
			{
				if (tlen == 1)
					tt = parseBits(eval, t, "cccr"); /* tt[t+0] */
				final String arg1=tt[t+0];
				final String item=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+1]);
				final String cmp=tt[t+2];
				final String value=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+3]);
				final Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((value.length()==0)||(item.length()==0)||(cmp.length()==0))
				{
					logError(scripted,"HASNUM","Syntax",funcParms);
					return returnable;
				}
				Item I=null;
				int num=0;
				if(E==null)
					returnable=false;
				else
				if(E instanceof MOB)
				{
					final MOB M=(MOB)E;
					for(int i=0;i<M.numItems();i++)
					{
						I=M.getItem(i);
						if(I==null)
							break;
						if((item.equalsIgnoreCase("all"))
						||(CMLib.english().containsString(I.Name(),item)))
							num++;
					}
					returnable=simpleEval(scripted,""+num,value,cmp,"HASNUM");
				}
				else
				if(E instanceof Item)
				{
					num=CMLib.english().containsString(E.name(),item)?1:0;
					returnable=simpleEval(scripted,""+num,value,cmp,"HASNUM");
				}
				else
				if(E instanceof Room)
				{
					final Room R=(Room)E;
					for(int i=0;i<R.numItems();i++)
					{
						I=R.getItem(i);
						if(I==null)
							break;
						if((item.equalsIgnoreCase("all"))
						||(CMLib.english().containsString(I.Name(),item)))
							num++;
					}
					returnable=simpleEval(scripted,""+num,value,cmp,"HASNUM");
				}
				else
					returnable=false;
				break;
			}
			case 67: // hastitle
			{
				if (tlen == 1)
					tt = parseBits(eval, t, "cr"); /* tt[t+0] */
				final String arg1=tt[t+0];
				final String arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+1]);
				final Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if(arg2.length()==0)
				{
					logError(scripted,"HASTITLE","Syntax",funcParms);
					return returnable;
				}
				if(E instanceof MOB)
				{
					final MOB M=(MOB)E;
					returnable=(M.playerStats()!=null)&&(M.playerStats().getTitles().contains(arg2));
				}
				else
					returnable=false;
				break;
			}
			case 3: // worn
			{
				if (tlen == 1)
					tt = parseBits(eval, t, "cr"); /* tt[t+0] */
				final String arg1=tt[t+0];
				final String arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+1]);
				final Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if(arg2.length()==0)
				{
					logError(scripted,"WORN","Syntax",funcParms);
					return returnable;
				}
				if(E==null)
					returnable=false;
				else
				if(E instanceof MOB)
					returnable=(((MOB)E).fetchItem(null,Wearable.FILTER_WORNONLY,arg2)!=null);
				else
				if(E instanceof Item)
					returnable=(CMLib.english().containsString(E.name(),arg2)&&(!((Item)E).amWearingAt(Wearable.IN_INVENTORY)));
				else
					returnable=false;
				break;
			}
			case 4: // isnpc
			{
				final String arg1=CMParms.cleanBit(funcParms);
				final Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
					returnable=((MOB)E).isMonster();
				break;
			}
			case 87: // isbirthday
			{
				final String arg1=CMParms.cleanBit(funcParms);
				final Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
				{
					final MOB mob=(MOB)E;
					if(mob.playerStats()==null)
						 returnable=false;
					else
					{
						final TimeClock C=CMLib.time().localClock(mob.getStartRoom());
						final int month=C.getMonth();
						final int day=C.getDayOfMonth();
						final int bday=mob.playerStats().getBirthday()[PlayerStats.BIRTHDEX_DAY];
						final int bmonth=mob.playerStats().getBirthday()[PlayerStats.BIRTHDEX_MONTH];
						if((C.getYear()>mob.playerStats().getBirthday()[PlayerStats.BIRTHDEX_LASTYEARCELEBRATED])
						&&((month==bmonth)&&(day==bday)))
							returnable=true;
						else
							returnable=false;
					}
				}
				break;
			}
			case 5: // ispc
			{
				final String arg1=CMParms.cleanBit(funcParms);
				final Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
					returnable=!((MOB)E).isMonster();
				break;
			}
			case 6: // isgood
			{
				final String arg1=CMParms.cleanBit(funcParms);
				final Physical P=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if(P==null)
					returnable=false;
				else
					returnable=CMLib.flags().isGood(P);
				break;
			}
			case 8: // isevil
			{
				final String arg1=CMParms.cleanBit(funcParms);
				final Physical P=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if(P==null)
					returnable=false;
				else
					returnable=CMLib.flags().isEvil(P);
				break;
			}
			case 9: // isneutral
			{
				final String arg1=CMParms.cleanBit(funcParms);
				final Physical P=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if(P==null)
					returnable=false;
				else
					returnable=CMLib.flags().isNeutral(P);
				break;
			}
			case 54: // isalive
			{
				final String arg1=CMParms.cleanBit(funcParms);
				final Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if((E!=null)&&((E instanceof MOB))&&(!((MOB)E).amDead()))
					returnable=true;
				else
					returnable=false;
				break;
			}
			case 58: // isable
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"cr"); /* tt[t+0] */
				final String arg1=tt[t+0];
				final String arg2=tt[t+1];
				final Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if((E!=null)&&((E instanceof MOB))&&(!((MOB)E).amDead()))
				{
					final ExpertiseLibrary X=(ExpertiseLibrary)CMLib.expertises().findDefinition(arg2,true);
					if(X!=null)
						returnable=((MOB)E).fetchExpertise(X.ID())!=null;
					else
						returnable=((MOB)E).findAbility(arg2)!=null;
				}
				else
					returnable=false;
				break;
			}
			case 59: // isopen
			{
				final String arg1=CMParms.cleanBit(funcParms);
				final int dir=CMLib.directions().getGoodDirectionCode(arg1);
				returnable=false;
				if(dir<0)
				{
					final Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
					if((E!=null)&&(E instanceof Container))
						returnable=((Container)E).isOpen();
					else
					if((E!=null)&&(E instanceof Exit))
						returnable=((Exit)E).isOpen();
				}
				else
				if(lastKnownLocation!=null)
				{
					final Exit E=lastKnownLocation.getExitInDir(dir);
					if(E!=null)
						returnable= E.isOpen();
				}
				break;
			}
			case 60: // islocked
			{
				final String arg1=CMParms.cleanBit(funcParms);
				final int dir=CMLib.directions().getGoodDirectionCode(arg1);
				returnable=false;
				if(dir<0)
				{
					final Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
					if((E!=null)&&(E instanceof Container))
						returnable=((Container)E).isLocked();
					else
					if((E!=null)&&(E instanceof Exit))
						returnable=((Exit)E).isLocked();
				}
				else
				if(lastKnownLocation!=null)
				{
					final Exit E=lastKnownLocation.getExitInDir(dir);
					if(E!=null)
						returnable= E.isLocked();
				}
				break;
			}
			case 10: // isfight
			{
				final String arg1=CMParms.cleanBit(funcParms);
				final Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
					returnable=((MOB)E).isInCombat();
				break;
			}
			case 11: // isimmort
			{
				final String arg1=CMParms.cleanBit(funcParms);
				final Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
					returnable=CMSecurity.isAllowed(((MOB)E),lastKnownLocation,CMSecurity.SecFlag.IMMORT);
				break;
			}
			case 12: // ischarmed
			{
				final String arg1=CMParms.cleanBit(funcParms);
				final Physical E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
					returnable=CMLib.flags().flaggedAffects(E,Ability.FLAG_CHARMING).size()>0;
				break;
			}
			case 15: // isfollow
			{
				final String arg1=CMParms.cleanBit(funcParms);
				final Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
				if(((MOB)E).amFollowing()==null)
					returnable=false;
				else
				if(((MOB)E).amFollowing().location()!=lastKnownLocation)
					returnable=false;
				else
					returnable=true;
				break;
			}
			case 73: // isservant
			{
				final String arg1=CMParms.cleanBit(funcParms);
				final Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if((E==null)||(!(E instanceof MOB))||(lastKnownLocation==null))
					returnable=false;
				else
				if((((MOB)E).getLiegeID()==null)||(((MOB)E).getLiegeID().length()==0))
					returnable=false;
				else
				if(lastKnownLocation.fetchInhabitant("$"+((MOB)E).getLiegeID()+"$")==null)
					returnable=false;
				else
					returnable=true;
				break;
			}
			case 95: // isspeaking
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"cr"); /* tt[t+0] */
				final String arg1=tt[t+0];
				final String arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+1]);
				final Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if((E==null)||(!(E instanceof MOB))||(lastKnownLocation==null))
					returnable=false;
				else
				{
					final MOB TM=(MOB)E;
					final Language L=CMLib.utensils().getLanguageSpoken(TM);
					if((L!=null)
					&&(!L.ID().equalsIgnoreCase("Common"))
					&&(L.ID().equalsIgnoreCase(arg2)||L.Name().equalsIgnoreCase(arg2)||arg2.equalsIgnoreCase("any")))
						returnable=true;
					else
					if(arg2.equalsIgnoreCase("common")||arg2.equalsIgnoreCase("none"))
						returnable=true;
					else
						returnable=false;
				}
				break;
			}
			case 55: // ispkill
			{
				final String arg1=CMParms.cleanBit(funcParms);
				final Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
				if(((MOB)E).isAttributeSet(MOB.Attrib.PLAYERKILL))
					returnable=true;
				else
					returnable=false;
				break;
			}
			case 7: // isname
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"cr"); /* tt[t+0] */
				final String arg1=tt[t+0];
				final String arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+1]);
				final Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if(E==null)
					returnable=false;
				else
					returnable=CMLib.english().containsString(E.name(),arg2);
				break;
			}
			case 56: // name
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"ccr"); /* tt[t+0] */
				final String arg1=tt[t+0];
				final String arg2=tt[t+1];
				final String arg3=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]);
				final Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if(E==null)
					returnable=false;
				else
					returnable=simpleEvalStr(scripted,E.Name(),arg3,arg2,"NAME");
				break;
			}
			case 75: // currency
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"ccr"); /* tt[t+0] */
				final String arg1=tt[t+0];
				final String arg2=tt[t+1];
				final String arg3=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]);
				final Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if(E==null)
					returnable=false;
				else
					returnable=simpleEvalStr(scripted,CMLib.beanCounter().getCurrency(E),arg3,arg2,"CURRENCY");
				break;
			}
			case 61: // strin
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"cr"); /* tt[t+0] */
				final String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+0]);
				final String arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+1]);
				final Vector<String> V=CMParms.parse(arg1.toUpperCase());
				returnable=V.contains(arg2.toUpperCase());
				break;
			}
			case 62: // callfunc
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"cr"); /* tt[t+0] */
				final String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+0]);
				final String arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+1]);
				String found=null;
				boolean validFunc=false;
				final List<DVector> scripts=getScripts();
				String trigger=null;
				String[] ttrigger=null;
				for(int v=0;v<scripts.size();v++)
				{
					final DVector script2=scripts.get(v);
					if(script2.size()<1)
						continue;
					trigger=((String)script2.elementAt(0,1)).toUpperCase().trim();
					ttrigger=(String[])script2.elementAt(0,2);
					if(getTriggerCode(trigger,ttrigger)==17)
					{
						final String fnamed=
							(ttrigger!=null)
							?ttrigger[1]
							:CMParms.getCleanBit(trigger,1);
						if(fnamed.equalsIgnoreCase(arg1))
						{
							validFunc=true;
							found=
							execute(scripted,
									source,
									target,
									monster,
									primaryItem,
									secondaryItem,
									script2,
									varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,arg2),
									tmp);
							if(found==null)
								found="";
							break;
						}
					}
				}
				if(!validFunc)
					logError(scripted,"CALLFUNC","Unknown","Function: "+arg1);
				else
				if(found!=null)
					returnable=!(found.trim().length()==0);
				else
					returnable=false;
				break;
			}
			case 14: // affected
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"cr"); /* tt[t+0] */
				final String arg1=tt[t+0];
				String arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+1]);
				final Physical P=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if(P==null)
					returnable=false;
				else
				{
					final Ability A=CMClass.findAbility(arg2);
					if(A!=null)
						arg2=A.ID();
					returnable=(P.fetchEffect(arg2)!=null);
				}
				break;
			}
			case 69: // isbehave
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"cr"); /* tt[t+0] */
				final String arg1=tt[t+0];
				String arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+1]);
				final PhysicalAgent P=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if(P==null)
					returnable=false;
				else
				{
					final Behavior B=CMClass.findBehavior(arg2);
					if(B!=null)
						arg2=B.ID();
					returnable=(P.fetchBehavior(arg2)!=null);
				}
				break;
			}
			case 70: // ipaddress
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"ccr"); /* tt[t+0] */
				final String arg1=tt[t+0];
				final String arg2=tt[t+1];
				final String arg3=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]);
				final Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if((E==null)||(!(E instanceof MOB))||(((MOB)E).isMonster()))
					returnable=false;
				else
					returnable=simpleEvalStr(scripted,((MOB)E).session().getAddress(),arg3,arg2,"ADDRESS");
				break;
			}
			case 28: // questwinner
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"cr"); /* tt[t+0] */
				String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+0]);
				final String arg2=tt[t+1];
				final Environmental E=getArgumentMOB(tt[t+0],source,monster,target,primaryItem,secondaryItem,msg,tmp);
				final Quest Q=getQuest(arg2);
				if(Q==null)
					returnable=false;
				else
				{
					if(E!=null)
						arg1=E.Name();
					returnable=Q.wasWinner(arg1);
				}
				break;
			}
			case 93: // questscripted
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"cr"); /* tt[t+0] */
				final PhysicalAgent E=getArgumentMOB(tt[t+0],source,monster,target,primaryItem,secondaryItem,msg,tmp);
				final String arg2=tt[t+1];
				final Quest Q=getQuest(arg2);
				returnable=false;
				if((Q!=null)&&(E!=null))
				{
					for(final Enumeration<ScriptingEngine> e=E.scripts();e.hasMoreElements();)
					{
						final ScriptingEngine SE=e.nextElement();
						if((SE!=null)&&(SE.defaultQuestName().equalsIgnoreCase(Q.name())))
							returnable=true;
					}
				}
				break;
			}
			case 94: // questroom
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"cr"); /* tt[t+0] */
				final String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+0]);
				final String arg2=tt[t+1];
				final Quest Q=getQuest(arg2);
				if(Q==null)
					returnable=false;
				else
					returnable=(Q.getQuestRoomIndex(arg1)>=0);
				break;
			}
			case 29: // questmob
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"cr"); /* tt[t+0] */
				final String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+0]);
				final String arg2=tt[t+1];
				final Quest Q=getQuest(arg2);
				if(Q==null)
					returnable=false;
				else
					returnable=(Q.getQuestMobIndex(arg1)>=0);
				break;
			}
			case 31: // isquestmobalive
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"cr"); /* tt[t+0] */
				final String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+0]);
				final String arg2=tt[t+1];
				final Quest Q=getQuest(arg2);
				if(Q==null)
					returnable=false;
				else
				{
					MOB M=null;
					if(CMath.s_int(arg1.trim())>0)
						M=Q.getQuestMob(CMath.s_int(arg1.trim()));
					else
						M=Q.getQuestMob(Q.getQuestMobIndex(arg1));
					if(M==null)
						returnable=false;
					else returnable=!M.amDead();
				}
				break;
			}
			case 32: // nummobsinarea
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"ccr"); /* tt[t+0] */
				final String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+0]).toUpperCase();
				final String arg2=tt[t+1];
				String arg3=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]);
				int num=0;
				MaskingLibrary.CompiledZMask MASK=null;
				if((arg3.toUpperCase().startsWith("MASK")&&(arg3.substring(4).trim().startsWith("="))))
				{
					arg3=arg3.substring(4).trim();
					arg3=arg3.substring(1).trim();
					MASK=CMLib.masking().maskCompile(arg3);
				}
				for(final Enumeration<Room> e=lastKnownLocation.getArea().getProperMap();e.hasMoreElements();)
				{
					final Room R=e.nextElement();
					if(arg1.equals("*"))
						num+=R.numInhabitants();
					else
					{
						for(int m=0;m<R.numInhabitants();m++)
						{
							final MOB M=R.fetchInhabitant(m);
							if(M==null)
								continue;
							if(MASK!=null)
							{
								if(CMLib.masking().maskCheck(MASK,M,true))
									num++;
							}
							else
							if(CMLib.english().containsString(M.name(),arg1))
								num++;
						}
					}
				}
				returnable=simpleEval(scripted,""+num,arg3,arg2,"NUMMOBSINAREA");
				break;
			}
			case 33: // nummobs
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"ccr"); /* tt[t+0] */
				final String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+0]).toUpperCase();
				final String arg2=tt[t+1];
				String arg3=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]);
				int num=0;
				MaskingLibrary.CompiledZMask MASK=null;
				if((arg3.toUpperCase().startsWith("MASK")&&(arg3.substring(4).trim().startsWith("="))))
				{
					arg3=arg3.substring(4).trim();
					arg3=arg3.substring(1).trim();
					MASK=CMLib.masking().maskCompile(arg3);
				}
				try
				{
					for(final Enumeration e=CMLib.map().rooms();e.hasMoreElements();)
					{
						final Room R=(Room)e.nextElement();
						for(int m=0;m<R.numInhabitants();m++)
						{
							final MOB M=R.fetchInhabitant(m);
							if(M==null)
								continue;
							if(MASK!=null)
							{
								if(CMLib.masking().maskCheck(MASK,M,true))
									num++;
							}
							else
							if(CMLib.english().containsString(M.name(),arg1))
								num++;
						}
					}
				}
				catch (final NoSuchElementException nse)
				{
				}
				returnable=simpleEval(scripted,""+num,arg3,arg2,"NUMMOBS");
				break;
			}
			case 34: // numracesinarea
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"ccr"); /* tt[t+0] */
				final String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+0]).toUpperCase();
				final String arg2=tt[t+1];
				final String arg3=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]);
				int num=0;
				Room R=null;
				MOB M=null;
				for(final Enumeration e=lastKnownLocation.getArea().getProperMap();e.hasMoreElements();)
				{
					R=(Room)e.nextElement();
					for(int m=0;m<R.numInhabitants();m++)
					{
						M=R.fetchInhabitant(m);
						if((M!=null)&&(M.charStats().raceName().equalsIgnoreCase(arg1)))
							num++;
					}
				}
				returnable=simpleEval(scripted,""+num,arg3,arg2,"NUMRACESINAREA");
				break;
			}
			case 35: // numraces
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"ccr"); /* tt[t+0] */
				final String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+0]).toUpperCase();
				final String arg2=tt[t+1];
				final String arg3=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]);
				int num=0;
				try
				{
					for(final Enumeration e=CMLib.map().rooms();e.hasMoreElements();)
					{
						final Room R=(Room)e.nextElement();
						for(int m=0;m<R.numInhabitants();m++)
						{
							final MOB M=R.fetchInhabitant(m);
							if((M!=null)&&(M.charStats().raceName().equalsIgnoreCase(arg1)))
								num++;
						}
					}
				}
				catch (final NoSuchElementException nse)
				{
				}
				returnable=simpleEval(scripted,""+num,arg3,arg2,"NUMRACES");
				break;
			}
			case 30: // questobj
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"cr"); /* tt[t+0] */
				final String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+0]);
				final String arg2=tt[t+1];
				final Quest Q=getQuest(arg2);
				if(Q==null)
					returnable=false;
				else
					returnable=(Q.getQuestItemIndex(arg1)>=0);
				break;
			}
			case 85: // islike
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"cr"); /* tt[t+0] */
				final String arg1=tt[t+0];
				final String arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+1]);
				final Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if(E==null)
					returnable=false;
				else
					returnable=CMLib.masking().maskCheck(arg2, E,false);
				break;
			}
			case 86: // strcontains
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"cr"); /* tt[t+0] */
				final String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+0]);
				final String arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+1]);
				returnable=stringContainsFunctionImpl(arg1,arg2)>=0;
				break;
			}
			case 92: // isodd
			{
				final String val=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.cleanBit(funcParms)).trim();
				boolean isodd = false;
				if( CMath.isLong( val ) )
				{
					isodd = (CMath.s_long(val) %2 == 1);
				}
				returnable = isodd;
				break;
			}
			case 16: // hitprcnt
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"ccr"); /* tt[t+0] */
				final String arg1=tt[t+0];
				final String arg2=tt[t+1];
				final String arg3=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]);
				final Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if((arg2.length()==0)||(arg3.length()==0))
				{
					logError(scripted,"HITPRCNT","Syntax",funcParms);
					return returnable;
				}
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
				{
					final double hitPctD=CMath.div(((MOB)E).curState().getHitPoints(),((MOB)E).maxState().getHitPoints());
					final int val1=(int)Math.round(hitPctD*100.0);
					returnable=simpleEval(scripted,""+val1,arg3,arg2,"HITPRCNT");
				}
				break;
			}
			case 50: // isseason
			{
				String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.cleanBit(funcParms));
				returnable=false;
				if(monster.location()!=null)
				{
					arg1=arg1.toUpperCase();
					for(final TimeClock.Season season : TimeClock.Season.values())
					{
						if(season.toString().startsWith(arg1.toUpperCase())
						&&(monster.location().getArea().getTimeObj().getSeasonCode()==season))
						{
							returnable=true;
							break;
						}
					}
				}
				break;
			}
			case 51: // isweather
			{
				final String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.cleanBit(funcParms));
				returnable=false;
				if(monster.location()!=null)
				for(int a=0;a<Climate.WEATHER_DESCS.length;a++)
				{
					if((Climate.WEATHER_DESCS[a]).startsWith(arg1.toUpperCase())
					&&(monster.location().getArea().getClimateObj().weatherType(monster.location())==a))
					{
						returnable = true;
						break;
					}
				}
				break;
			}
			case 57: // ismoon
			{
				String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.cleanBit(funcParms));
				returnable=false;
				if(monster.location()!=null)
				{
					if(arg1.length()==0)
						returnable=monster.location().getArea().getClimateObj().canSeeTheStars(monster.location());
					else
					{
						arg1=arg1.toUpperCase();
						for(final TimeClock.MoonPhase phase : TimeClock.MoonPhase.values())
						{
							if(phase.toString().startsWith(arg1)
							&&(monster.location().getArea().getTimeObj().getMoonPhase(monster.location())==phase))
							{
								returnable=true;
								break;
							}
						}
					}
				}
				break;
			}
			case 38: // istime
			{
				final String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.cleanBit(funcParms)).toLowerCase().trim();
				if(monster.location()==null)
					returnable=false;
				else
				if(("daytime").startsWith(arg1)
				&&(monster.location().getArea().getTimeObj().getTODCode()==TimeClock.TimeOfDay.DAY))
					returnable=true;
				else
				if(("dawn").startsWith(arg1)
				&&(monster.location().getArea().getTimeObj().getTODCode()==TimeClock.TimeOfDay.DAWN))
					returnable=true;
				else
				if(("dusk").startsWith(arg1)
				&&(monster.location().getArea().getTimeObj().getTODCode()==TimeClock.TimeOfDay.DUSK))
					returnable=true;
				else
				if(("nighttime").startsWith(arg1)
				&&(monster.location().getArea().getTimeObj().getTODCode()==TimeClock.TimeOfDay.NIGHT))
					returnable=true;
				else
				if((monster.location().getArea().getTimeObj().getHourOfDay()==CMath.s_int(arg1)))
					returnable=true;
				else
					returnable=false;
				break;
			}
			case 39: // isday
			{
				final String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.cleanBit(funcParms));
				if((monster.location()!=null)&&(monster.location().getArea().getTimeObj().getDayOfMonth()==CMath.s_int(arg1.trim())))
					returnable=true;
				else
					returnable=false;
				break;
			}
			case 45: // nummobsroom
			{
				if(tlen==1)
				{
					if(CMParms.numBits(funcParms)>2)
						tt=parseBits(eval,t,"ccr"); /* tt[t+0] */
					else
						tt=parseBits(eval,t,"cr"); /* tt[t+0] */
				}
				int num=0;
				int startbit=0;
				if(lastKnownLocation!=null)
				{
					num=lastKnownLocation.numInhabitants();
					if(signH.containsKey(tt[t+1]))
					{
						String name=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+0]);
						startbit++;
						if(!name.equalsIgnoreCase("*"))
						{
							num=0;
							MaskingLibrary.CompiledZMask MASK=null;
							if((name.toUpperCase().startsWith("MASK")&&(name.substring(4).trim().startsWith("="))))
							{
								final boolean usePreCompiled = (name.equals(tt[t+0]));
								name=name.substring(4).trim();
								name=name.substring(1).trim();
								MASK=usePreCompiled?CMLib.masking().getPreCompiledMask(name): CMLib.masking().maskCompile(name);
							}
							for(int i=0;i<lastKnownLocation.numInhabitants();i++)
							{
								final MOB M=lastKnownLocation.fetchInhabitant(i);
								if(M==null)
									continue;
								if(MASK!=null)
								{
									if(CMLib.masking().maskCheck(MASK,M,true))
										num++;
								}
								else
								if(CMLib.english().containsString(M.Name(),name)
								||CMLib.english().containsString(M.displayText(),name))
									num++;
							}
						}
					}
				}
				else
				if(!signH.containsKey(tt[t+0]))
				{
					logError(scripted,"NUMMOBSROOM","Syntax","No SIGN found: "+funcParms);
					return returnable;
				}

				final String comp=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+startbit]);
				final String arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+startbit+1]);
				if(lastKnownLocation!=null)
					returnable=simpleEval(scripted,""+num,arg2,comp,"NUMMOBSROOM");
				break;
			}
			case 63: // numpcsroom
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"cr"); /* tt[t+0] */
				final String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+0]);
				final String arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+1]);
				if(lastKnownLocation!=null)
					returnable=simpleEval(scripted,""+lastKnownLocation.numPCInhabitants(),arg2,arg1,"NUMPCSROOM");
				break;
			}
			case 79: // numpcsarea
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"cr"); /* tt[t+0] */
				final String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+0]);
				final String arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+1]);
				if(lastKnownLocation!=null)
				{
					int num=0;
					for(final Session S : CMLib.sessions().localOnlineIterable())
					{
						if((S.mob().location()!=null)&&(S.mob().location().getArea()==lastKnownLocation.getArea()))
							num++;
					}
					returnable=simpleEval(scripted,""+num,arg2,arg1,"NUMPCSAREA");
				}
				break;
			}
			case 77: // explored
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"cccr"); /* tt[t+0] */
				final String whom=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+0]);
				final String where=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+1]);
				final String cmp=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]);
				final String arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+3]);
				final Environmental E=getArgumentMOB(whom,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if((E==null)||(!(E instanceof MOB)))
				{
					logError(scripted,"EXPLORED","Unknown Code",whom);
					return returnable;
				}
				Area A=null;
				if(!where.equalsIgnoreCase("world"))
				{
					A=CMLib.map().getArea(where);
					if(A==null)
					{
						final Environmental E2=getArgumentItem(where,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
						if(E2 != null)
							A=CMLib.map().areaLocation(E2);
					}
					if(A==null)
					{
						logError(scripted,"EXPLORED","Unknown Area",where);
						return returnable;
					}
				}
				if(lastKnownLocation!=null)
				{
					int pct=0;
					final MOB M=(MOB)E;
					if(M.playerStats()!=null)
						pct=M.playerStats().percentVisited(M,A);
					returnable=simpleEval(scripted,""+pct,arg2,cmp,"EXPLORED");
				}
				break;
			}
			case 72: // faction
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"cccr"); /* tt[t+0] */
				final String whom=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+0]);
				final String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+1]);
				final String cmp=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]);
				final String arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+3]);
				final Environmental E=getArgumentMOB(whom,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				final Faction F=CMLib.factions().getFaction(arg1);
				if((E==null)||(!(E instanceof MOB)))
				{
					logError(scripted,"FACTION","Unknown Code",whom);
					return returnable;
				}
				if(F==null)
				{
					logError(scripted,"FACTION","Unknown Faction",arg1);
					return returnable;
				}
				final MOB M=(MOB)E;
				String value=null;
				if(!M.hasFaction(F.factionID()))
					value="";
				else
				{
					final int myfac=M.fetchFaction(F.factionID());
					if(CMath.isNumber(arg2.trim()))
						value=Integer.toString(myfac);
					else
					{
						final Faction.FRange FR=CMLib.factions().getRange(F.factionID(),myfac);
						if(FR==null)
							value="";
						else
							value=FR.name();
					}
				}
				if(lastKnownLocation!=null)
					returnable=simpleEval(scripted,value,arg2,cmp,"FACTION");
				break;
			}
			case 46: // numitemsroom
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"cr"); /* tt[t+0] */
				final String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+0]);
				final String arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+1]);
				int ct=0;
				if(lastKnownLocation!=null)
				{
					for(int i=0;i<lastKnownLocation.numItems();i++)
					{
						final Item I=lastKnownLocation.getItem(i);
						if((I!=null)&&(I.container()==null))
							ct++;
					}
				}
				returnable=simpleEval(scripted,""+ct,arg2,arg1,"NUMITEMSROOM");
				break;
			}
			case 47: //mobitem
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"ccr"); /* tt[t+0] */
				final String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+0]);
				final String arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+1]);
				final String arg3=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]);
				MOB M=null;
				if(lastKnownLocation!=null)
				{
					if(CMath.isInteger(arg1.trim()))
						M=lastKnownLocation.fetchInhabitant(CMath.s_int(arg1.trim())-1);
					else
						M=lastKnownLocation.fetchInhabitant(arg1.trim());
				}
				Item which=null;
				int ct=1;
				if(M!=null)
				{
					for(int i=0;i<M.numItems();i++)
					{
						final Item I=M.getItem(i);
						if((I!=null)&&(I.container()==null))
						{
							if(ct==CMath.s_int(arg2.trim()))
							{
								which = I;
								break;
							}
							ct++;
						}
					}
				}
				if(which==null)
					returnable=false;
				else
					returnable=(CMLib.english().containsString(which.name(),arg3)
								||CMLib.english().containsString(which.Name(),arg3)
								||CMLib.english().containsString(which.displayText(),arg3));
				break;
			}
			case 49: // hastattoo
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"cr"); /* tt[t+0] */
				final String arg1=tt[t+0];
				final String arg2=tt[t+1];
				final Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if(arg2.length()==0)
				{
					logError(scripted,"HASTATTOO","Syntax",funcParms);
					break;
				}
				else
				if((E!=null)&&(E instanceof MOB))
					returnable=(((MOB)E).findTattoo(arg2)!=null);
				else
					returnable=false;
				break;
			}
			case 99: // hasacctattoo
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"cr"); /* tt[t+0] */
				final String arg1=tt[t+0];
				final String arg2=tt[t+1];
				final Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if(arg2.length()==0)
				{
					logError(scripted,"HASACCTATTOO","Syntax",funcParms);
					break;
				}
				else
				if((E!=null)&&(E instanceof MOB)&&(((MOB)E).playerStats()!=null)&&(((MOB)E).playerStats().getAccount()!=null))
					returnable=((MOB)E).playerStats().getAccount().findTattoo(arg2)!=null;
				else
					returnable=false;
				break;
			}
			case 48: // numitemsmob
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"ccr"); /* tt[t+0] */
				final String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+0]);
				final String arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+1]);
				final String arg3=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]);
				MOB which=null;
				if(lastKnownLocation!=null)
				{
					if(CMath.isInteger(arg1.trim()))
						which=lastKnownLocation.fetchInhabitant(CMath.s_int(arg1.trim())-1);
					else
						which=lastKnownLocation.fetchInhabitant(arg1);
				}
				int ct=0;
				if(which!=null)
				{
					for(int i=0;i<which.numItems();i++)
					{
						final Item I=which.getItem(i);
						if((I!=null)&&(I.container()==null))
							ct++;
					}
				}
				returnable=simpleEval(scripted,""+ct,arg3,arg2,"NUMITEMSMOB");
				break;
			}
			case 101: // numitemsshop
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"ccr"); /* tt[t+0] */
				final String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+0]);
				final String arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+1]);
				final String arg3=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]);
				PhysicalAgent which=null;
				if(lastKnownLocation!=null)
				{
					if(CMath.isInteger(arg1.trim()))
						which=lastKnownLocation.fetchInhabitant(CMath.s_int(arg1.trim())-1);
					else
						which=lastKnownLocation.fetchInhabitant(arg1);
					if(which == null)
						which=this.getArgumentItem(tt[t+0], source, monster, scripted, target, primaryItem, secondaryItem, msg, tmp);
					if(which == null)
						which=this.getArgumentMOB(tt[t+0], source, monster, target, primaryItem, secondaryItem, msg, tmp);
				}
				int ct=0;
				if(which!=null)
				{
					ShopKeeper shopHere = CMLib.coffeeShops().getShopKeeper(which);
					if((shopHere == null)&&(scripted instanceof Item))
						shopHere=CMLib.coffeeShops().getShopKeeper(((Item)which).owner());
					if((shopHere == null)&&(scripted instanceof MOB))
						shopHere=CMLib.coffeeShops().getShopKeeper(((MOB)which).location());
					if(shopHere == null)
						shopHere=CMLib.coffeeShops().getShopKeeper(lastKnownLocation);
					if(shopHere!=null)
					{
						CoffeeShop shop = shopHere.getShop();
						if(shop != null)
						{
							for(Iterator<Environmental> i=shop.getStoreInventory();i.hasNext();i.next())
							{
								ct++;
							}
						}
					}
				}
				returnable=simpleEval(scripted,""+ct,arg3,arg2,"NUMITEMSSHOP");
				break;
			}
			case 100: // shopitem
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"ccr"); /* tt[t+0] */
				final String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+0]);
				final String arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+1]);
				final String arg3=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]);
				PhysicalAgent where=null;
				if(lastKnownLocation!=null)
				{
					if(CMath.isInteger(arg1.trim()))
						where=lastKnownLocation.fetchInhabitant(CMath.s_int(arg1.trim())-1);
					else
						where=lastKnownLocation.fetchInhabitant(arg1.trim());
					if(where == null)
						where=this.getArgumentItem(tt[t+0], source, monster, scripted, target, primaryItem, secondaryItem, msg, tmp);
					if(where == null)
						where=this.getArgumentMOB(tt[t+0], source, monster, target, primaryItem, secondaryItem, msg, tmp);
				}
				Environmental which=null;
				int ct=1;
				if(where!=null)
				{
					ShopKeeper shopHere = CMLib.coffeeShops().getShopKeeper(where);
					if((shopHere == null)&&(scripted instanceof Item))
						shopHere=CMLib.coffeeShops().getShopKeeper(((Item)where).owner());
					if((shopHere == null)&&(scripted instanceof MOB))
						shopHere=CMLib.coffeeShops().getShopKeeper(((MOB)where).location());
					if(shopHere == null)
						shopHere=CMLib.coffeeShops().getShopKeeper(lastKnownLocation);
					if(shopHere!=null)
					{
						CoffeeShop shop = shopHere.getShop();
						if(shop != null)
						{
							for(Iterator<Environmental> i=shop.getStoreInventory();i.hasNext();)
							{
								Environmental E=i.next();
								if(ct==CMath.s_int(arg2.trim()))
								{
									which = E;
									break;
								}
								ct++;
							}
						}
					}
					if(which==null)
						returnable=false;
					else
					{
						returnable=(CMLib.english().containsString(which.name(),arg3)
									||CMLib.english().containsString(which.Name(),arg3)
									||CMLib.english().containsString(which.displayText(),arg3));
						if(returnable)
							setShopPrice(shopHere,which,tmp);
					}
				}
				else
					returnable=false;
				break;
			}
			case 102: // shophas
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"cr"); /* tt[t+0] */
				final String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+0]);
				final String arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+1]);
				PhysicalAgent where=null;
				if(lastKnownLocation!=null)
				{
					if(CMath.isInteger(arg1.trim()))
						where=lastKnownLocation.fetchInhabitant(CMath.s_int(arg1.trim())-1);
					else
						where=lastKnownLocation.fetchInhabitant(arg1.trim());
					if(where == null)
						where=this.getArgumentItem(tt[t+0], source, monster, scripted, target, primaryItem, secondaryItem, msg, tmp);
					if(where == null)
						where=this.getArgumentMOB(tt[t+0], source, monster, target, primaryItem, secondaryItem, msg, tmp);
				}
				returnable=false;
				if(where!=null)
				{
					ShopKeeper shopHere = CMLib.coffeeShops().getShopKeeper(where);
					if((shopHere == null)&&(scripted instanceof Item))
						shopHere=CMLib.coffeeShops().getShopKeeper(((Item)where).owner());
					if((shopHere == null)&&(scripted instanceof MOB))
						shopHere=CMLib.coffeeShops().getShopKeeper(((MOB)where).location());
					if(shopHere == null)
						shopHere=CMLib.coffeeShops().getShopKeeper(lastKnownLocation);
					if(shopHere!=null)
					{
						CoffeeShop shop = shopHere.getShop();
						if(shop != null)
						{
							final Environmental E=shop.getStock(arg2.trim(), null);
							returnable = (E!=null);
							if(returnable)
								setShopPrice(shopHere,E,tmp);
						}
					}
				}
				break;
			}
			case 43: // roommob
			{
				if(tlen==1)
					tt=parseBits(eval,t,"cr"); /* tt[t+0] */
				final String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+0]);
				final String arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+1]);
				Environmental which=null;
				if(lastKnownLocation!=null)
				{
					if(CMath.isInteger(arg1.trim()))
						which=lastKnownLocation.fetchInhabitant(CMath.s_int(arg1.trim())-1);
					else
						which=lastKnownLocation.fetchInhabitant(arg1.trim());
				}
				if(which==null)
					returnable=false;
				else
					returnable=(CMLib.english().containsString(which.name(),arg2)
								||CMLib.english().containsString(which.Name(),arg2)
								||CMLib.english().containsString(which.displayText(),arg2));
				break;
			}
			case 44: // roomitem
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"cr"); /* tt[t+0] */
				final String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+0]);
				final String arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+1]);
				Environmental which=null;
				int ct=1;
				if(lastKnownLocation!=null)
				for(int i=0;i<lastKnownLocation.numItems();i++)
				{
					final Item I=lastKnownLocation.getItem(i);
					if((I!=null)&&(I.container()==null))
					{
						if(ct==CMath.s_int(arg1.trim()))
						{
							which = I;
							break;
						}
						ct++;
					}
				}
				if(which==null)
					returnable=false;
				else
					returnable=(CMLib.english().containsString(which.name(),arg2)
								||CMLib.english().containsString(which.Name(),arg2)
								||CMLib.english().containsString(which.displayText(),arg2));
				break;
			}
			case 36: // ishere
			{
				final String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.cleanBit(funcParms));
				if(lastKnownLocation!=null)
					returnable=((lastKnownLocation.findItem(arg1)!=null)||(lastKnownLocation.fetchInhabitant(arg1)!=null));
				else
					returnable=false;
				break;
			}
			case 17: // inroom
			{
				if(tlen==1)
					tt=parseSpecial3PartEval(eval,t);
				String comp="==";
				Environmental E=monster;
				String arg2;
				if(signH.containsKey(tt[t+1]))
				{
					E=getArgumentItem(tt[t+0],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
					comp=tt[t+1];
					arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]);
				}
				else
					arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+0]);
				Room R=null;
				if(arg2.startsWith("$"))
					R=CMLib.map().roomLocation(this.getArgumentItem(arg2,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp));
				if(R==null)
					R=getRoom(arg2,lastKnownLocation);
				if(E==null)
					returnable=false;
				else
				{
					final Room R2=CMLib.map().roomLocation(E);
					if((R==null)&&((arg2.length()==0)||(R2==null)))
						returnable=true;
					else
					if((R==null)||(R2==null))
						returnable=false;
					else
						returnable=simpleEvalStr(scripted,CMLib.map().getExtendedRoomID(R2),CMLib.map().getExtendedRoomID(R),comp,"INROOM");
				}
				break;
			}
			case 90: // inarea
			{
				if(tlen==1)
					tt=parseSpecial3PartEval(eval,t);
				String comp="==";
				Environmental E=monster;
				String arg3;
				if(signH.containsKey(tt[t+1]))
				{
					E=getArgumentItem(tt[t+0],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
					comp=tt[t+1];
					arg3=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]);
				}
				else
					arg3=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+0]);
				Room R=null;
				if(arg3.startsWith("$"))
					R=CMLib.map().roomLocation(this.getArgumentItem(arg3,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp));
				if(R==null)
				{
					try
					{
						final String lnAstr=(lastKnownLocation!=null)?lastKnownLocation.getArea().Name():null;
						if((lnAstr!=null)&&(lnAstr.equalsIgnoreCase(arg3)))
							R=lastKnownLocation;
						if(R==null)
						{
							for(final Enumeration<Area> a=CMLib.map().areas();a.hasMoreElements();)
							{
								final Area A=a.nextElement();
								if((A!=null)&&(A.Name().equalsIgnoreCase(arg3)))
								{
									if((lnAstr!=null)
									&&(lnAstr.equals(A.Name())))
										R=lastKnownLocation;
									else
									if(!A.isProperlyEmpty())
										R=A.getRandomProperRoom();
								}
							}
						}
						if(R==null)
						{
							for(final Enumeration<Area> a=CMLib.map().areas();a.hasMoreElements();)
							{
								final Area A=a.nextElement();
								if((A!=null)&&(CMLib.english().containsString(A.Name(),arg3)))
								{
									if((lnAstr!=null)
									&&(lnAstr.equals(A.Name())))
										R=lastKnownLocation;
									else
									if(!A.isProperlyEmpty())
										R=A.getRandomProperRoom();
								}
							}
						}
					}
					catch (final NoSuchElementException nse)
					{
					}
				}
				if(R==null)
					R=getRoom(arg3,lastKnownLocation);
				if((R!=null)
				&&(CMath.bset(R.getArea().flags(),Area.FLAG_INSTANCE_PARENT))
				&&(lastKnownLocation!=null)
				&&(lastKnownLocation.getArea()!=R.getArea())
				&&(CMath.bset(lastKnownLocation.getArea().flags(),Area.FLAG_INSTANCE_CHILD))
				&&(CMLib.map().getModelArea(lastKnownLocation.getArea())==R.getArea()))
					R=lastKnownLocation;
					
				if(E==null)
					returnable=false;
				else
				{
					final Room R2=CMLib.map().roomLocation(E);
					if((R==null)&&((arg3.length()==0)||(R2==null)))
						returnable=true;
					else
					if((R==null)||(R2==null))
						returnable=false;
					else
						returnable=simpleEvalStr(scripted,R2.getArea().Name(),R.getArea().Name(),comp,"INAREA");
				}
				break;
			}
			case 89: // isrecall
			{
				if(tlen==1)
					tt=parseSpecial3PartEval(eval,t);
				String comp="==";
				Environmental E=monster;
				String arg2;
				if(signH.containsKey(tt[t+1]))
				{
					E=getArgumentItem(tt[t+0],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
					comp=tt[t+1];
					arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]);
				}
				else
					arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+0]);
				Room R=null;
				if(arg2.startsWith("$"))
					R=CMLib.map().getStartRoom(this.getArgumentItem(arg2,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp));
				if(R==null)
					R=getRoom(arg2,lastKnownLocation);
				if(E==null)
					returnable=false;
				else
				{
					final Room R2=CMLib.map().getStartRoom(E);
					if((R==null)&&((arg2.length()==0)||(R2==null)))
						returnable=true;
					else
					if((R==null)||(R2==null))
						returnable=false;
					else
						returnable=simpleEvalStr(scripted,CMLib.map().getExtendedRoomID(R2),CMLib.map().getExtendedRoomID(R),comp,"ISRECALL");
				}
				break;
			}
			case 37: // inlocale
			{
				if(tlen==1)
				{
					if(CMParms.numBits(funcParms)>1)
						tt=parseBits(eval,t,"cr"); /* tt[t+0] */
					else
					{
						final int numBits=2;
						String[] parsed=null;
						if(CMParms.cleanBit(funcParms).equals(funcParms))
							parsed=parseBits("'"+funcParms+"'"+CMStrings.repeat(" .",numBits-1),"cr");
						else
							parsed=parseBits(funcParms+CMStrings.repeat(" .",numBits-1),"cr");
						tt=insertStringArray(tt,parsed,t);
						eval[0]=tt;
					}
				}
				String arg2=null;
				Environmental E=monster;
				if(tt[t+1].equals("."))
					arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+0]);
				else
				{
					E=getArgumentItem(tt[t+0],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
					arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+1]);
				}
				if(E==null)
					returnable=false;
				else
				if(arg2.length()==0)
					returnable=true;
				else
				{
					final Room R=CMLib.map().roomLocation(E);
					if(R==null)
						returnable=false;
					else
					if(CMClass.classID(R).toUpperCase().indexOf(arg2.toUpperCase())>=0)
						returnable=true;
					else
						returnable=false;
				}
				break;
			}
			case 18: // sex
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"CcR"); /* tt[t+0] */
				final String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+0]);
				final String arg2=tt[t+1];
				String arg3=tt[t+2];
				if(CMath.isNumber(arg3.trim()))
				{
					switch(CMath.s_int(arg3.trim()))
					{
					case 0:
						arg3 = "NEUTER";
						break;
					case 1:
						arg3 = "MALE";
						break;
					case 2:
						arg3 = "FEMALE";
						break;
					}
				}
				final Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if((arg2.length()==0)||(arg3.length()==0))
				{
					logError(scripted,"SEX","Syntax",funcParms);
					return returnable;
				}
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
				{
					final String sex=(""+((char)((MOB)E).charStats().getStat(CharStats.STAT_GENDER))).toUpperCase();
					if(arg2.equals("=="))
						returnable=arg3.startsWith(sex);
					else
					if(arg2.equals("!="))
						returnable=!arg3.startsWith(sex);
					else
					{
						logError(scripted,"SEX","Syntax",funcParms);
						return returnable;
					}
				}
				break;
			}
			case 91: // datetime
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"Ccr"); /* tt[t+0] */
				final String arg1=tt[t+0];
				final String arg2=tt[t+1];
				final String arg3=tt[t+2];
				final int index=CMParms.indexOf(ScriptingEngine.DATETIME_ARGS,arg1.trim());
				if(index<0)
					logError(scripted,"DATETIME","Syntax","Unknown arg: "+arg1+" for "+scripted.name());
				else
				if(CMLib.map().areaLocation(scripted)!=null)
				{
					String val=null;
					switch(index)
					{
					case 2:
						val = "" + CMLib.map().areaLocation(scripted).getTimeObj().getDayOfMonth();
						break;
					case 3:
						val = "" + CMLib.map().areaLocation(scripted).getTimeObj().getDayOfMonth();
						break;
					case 4:
						val = "" + CMLib.map().areaLocation(scripted).getTimeObj().getMonth();
						break;
					case 5:
						val = "" + CMLib.map().areaLocation(scripted).getTimeObj().getYear();
						break;
					default:
						val = "" + CMLib.map().areaLocation(scripted).getTimeObj().getHourOfDay();
						break;
					}
					returnable=simpleEval(scripted,val,arg3,arg2,"DATETIME");
				}
				break;
			}
			case 13: // stat
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"cccr"); /* tt[t+0] */
				final String arg1=tt[t+0];
				final String arg2=tt[t+1];
				final String arg3=tt[t+2];
				final String arg4=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+3]);
				final Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((arg2.length()==0)||(arg3.length()==0))
				{
					logError(scripted,"STAT","Syntax",funcParms);
					break;
				}
				if(E==null)
					returnable=false;
				else
				{
					final String val=getStatValue(E,arg2);
					if(val==null)
					{
						logError(scripted,"STAT","Syntax","Unknown stat: "+arg2+" for "+E.name());
						break;
					}

					if(arg3.equals("=="))
						returnable=val.equalsIgnoreCase(arg4);
					else
					if(arg3.equals("!="))
						returnable=!val.equalsIgnoreCase(arg4);
					else
						returnable=simpleEval(scripted,val,arg4,arg3,"STAT");
				}
				break;
			}
			case 52: // gstat
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"cccr"); /* tt[t+0] */
				final String arg1=tt[t+0];
				final String arg2=tt[t+1];
				final String arg3=tt[t+2];
				final String arg4=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+3]);
				final Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((arg2.length()==0)||(arg3.length()==0))
				{
					logError(scripted,"GSTAT","Syntax",funcParms);
					break;
				}
				if(E==null)
					returnable=false;
				else
				{
					final String val=getGStatValue(E,arg2);
					if(val==null)
					{
						logError(scripted,"GSTAT","Syntax","Unknown stat: "+arg2+" for "+E.name());
						break;
					}

					if(arg3.equals("=="))
						returnable=val.equalsIgnoreCase(arg4);
					else
					if(arg3.equals("!="))
						returnable=!val.equalsIgnoreCase(arg4);
					else
						returnable=simpleEval(scripted,val,arg4,arg3,"GSTAT");
				}
				break;
			}
			case 19: // position
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"ccR"); /* tt[t+0] */
				final String arg1=tt[t+0];
				final String arg2=tt[t+1];
				final String arg3=tt[t+2];
				final Physical P=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((arg2.length()==0)||(arg3.length()==0))
				{
					logError(scripted,"POSITION","Syntax",funcParms);
					return returnable;
				}
				if(P==null)
					returnable=false;
				else
				{
					String sex="STANDING";
					if(CMLib.flags().isSleeping(P))
						sex="SLEEPING";
					else
					if(CMLib.flags().isSitting(P))
						sex="SITTING";
					if(arg2.equals("=="))
						returnable=sex.startsWith(arg3);
					else
					if(arg2.equals("!="))
						returnable=!sex.startsWith(arg3);
					else
					{
						logError(scripted,"POSITION","Syntax",funcParms);
						return returnable;
					}
				}
				break;
			}
			case 20: // level
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"ccr"); /* tt[t+0] */
				final String arg1=tt[t+0];
				final String arg2=tt[t+1];
				final String arg3=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]);
				final Physical P=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((arg2.length()==0)||(arg3.length()==0))
				{
					logError(scripted,"LEVEL","Syntax",funcParms);
					return returnable;
				}
				if(P==null)
					returnable=false;
				else
				{
					final int val1=P.phyStats().level();
					returnable=simpleEval(scripted,""+val1,arg3,arg2,"LEVEL");
				}
				break;
			}
			case 80: // questpoints
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"ccr"); /* tt[t+0] */
				final String arg1=tt[t+0];
				final String arg2=tt[t+1];
				final String arg3=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]);
				final Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if((arg2.length()==0)||(arg3.length()==0))
				{
					logError(scripted,"QUESTPOINTS","Syntax",funcParms);
					return returnable;
				}
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
				{
					final int val1=((MOB)E).getQuestPoint();
					returnable=simpleEval(scripted,""+val1,arg3,arg2,"QUESTPOINTS");
				}
				break;
			}
			case 83: // qvar
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"cccr"); /* tt[t+0] */
				final String arg1=tt[t+0];
				final String arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+1]);
				final String arg3=tt[t+2];
				final String arg4=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+3]);
				final Quest Q=getQuest(arg1);
				if((arg2.length()==0)||(arg3.length()==0))
				{
					logError(scripted,"QVAR","Syntax",funcParms);
					return returnable;
				}
				if(Q==null)
					returnable=false;
				else
					returnable=simpleEvalStr(scripted,Q.getStat(arg2),arg4,arg3,"QVAR");
				break;
			}
			case 84: // math
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"ccr"); /* tt[t+0] */
				final String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+0]);
				final String arg2=tt[t+1];
				final String arg3=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]);
				if(!CMath.isMathExpression(arg1))
				{
					logError(scripted,"MATH","Syntax",funcParms);
					return returnable;
				}
				if(!CMath.isMathExpression(arg3))
				{
					logError(scripted,"MATH","Syntax",funcParms);
					return returnable;
				}
				returnable=simpleExpressionEval(scripted,arg1,arg3,arg2,"MATH");
				break;
			}
			case 81: // trains
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"ccr"); /* tt[t+0] */
				final String arg1=tt[t+0];
				final String arg2=tt[t+1];
				final String arg3=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]);
				final Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if((arg2.length()==0)||(arg3.length()==0))
				{
					logError(scripted,"TRAINS","Syntax",funcParms);
					return returnable;
				}
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
				{
					final int val1=((MOB)E).getTrains();
					returnable=simpleEval(scripted,""+val1,arg3,arg2,"TRAINS");
				}
				break;
			}
			case 82: // pracs
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"ccr"); /* tt[t+0] */
				final String arg1=tt[t+0];
				final String arg2=tt[t+1];
				final String arg3=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]);
				final Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if((arg2.length()==0)||(arg3.length()==0))
				{
					logError(scripted,"PRACS","Syntax",funcParms);
					return returnable;
				}
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
				{
					final int val1=((MOB)E).getPractices();
					returnable=simpleEval(scripted,""+val1,arg3,arg2,"PRACS");
				}
				break;
			}
			case 66: // clanrank
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"ccr"); /* tt[t+0] */
				final String arg1=tt[t+0];
				final String arg2=tt[t+1];
				final String arg3=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]);
				final Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if((arg2.length()==0)||(arg3.length()==0))
				{
					logError(scripted,"CLANRANK","Syntax",funcParms);
					return returnable;
				}
				if(!(E instanceof MOB))
					returnable=false;
				else
				{
					int val1=-1;
					Clan C=CMLib.clans().findRivalrousClan((MOB)E);
					if(C==null)
						C=((MOB)E).clans().iterator().hasNext()?((MOB)E).clans().iterator().next().first:null;
					if(C!=null)
						val1=((MOB)E).getClanRole(C.clanID()).second.intValue();
					returnable=simpleEval(scripted,""+val1,arg3,arg2,"CLANRANK");
				}
				break;
			}
			case 64: // deity
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"ccR"); /* tt[t+0] */
				final String arg1=tt[t+0];
				final String arg2=tt[t+1];
				final String arg3=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]);
				final Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if(arg2.length()==0)
				{
					logError(scripted,"DEITY","Syntax",funcParms);
					return returnable;
				}
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
				{
					final String sex=((MOB)E).getWorshipCharID();
					if(arg2.equals("=="))
						returnable=sex.equalsIgnoreCase(arg3);
					else
					if(arg2.equals("!="))
						returnable=!sex.equalsIgnoreCase(arg3);
					else
					{
						logError(scripted,"DEITY","Syntax",funcParms);
						return returnable;
					}
				}
				break;
			}
			case 68: // clandata
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"cccR"); /* tt[t+0] */
				final String arg1=tt[t+0];
				final String arg2=tt[t+1];
				final String arg3=tt[t+2];
				final String arg4=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+3]);
				final Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if((arg2.length()==0)||(arg3.length()==0))
				{
					logError(scripted,"CLANDATA","Syntax",funcParms);
					return returnable;
				}
				String clanID=null;
				if((E!=null)&&(E instanceof MOB))
				{
					Clan C=CMLib.clans().findRivalrousClan((MOB)E);
					if(C==null)
						C=((MOB)E).clans().iterator().hasNext()?((MOB)E).clans().iterator().next().first:null;
					if(C!=null)
						clanID=C.clanID();
				}
				else
				{
					clanID=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,arg1);
					if((scripted instanceof MOB)&&(CMLib.clans().getClan(clanID)==null))
					{
						final List<Pair<Clan,Integer>> Cs=CMLib.clans().getClansByCategory((MOB)scripted, clanID);
						if((Cs!=null)&&(Cs.size()>0))
							clanID=Cs.get(0).first.clanID();
					}
				}
				final Clan C=CMLib.clans().findClan(clanID);
				if(C!=null)
				{
					if(!C.isStat(arg2))
						logError(scripted,"CLANDATA","RunTime",arg2+" is not a valid clan variable.");
					else
					{
						final String whichVal=C.getStat(arg2).trim();
						if(CMath.isNumber(whichVal)&&CMath.isNumber(arg4.trim()))
							returnable=simpleEval(scripted,whichVal,arg4,arg3,"CLANDATA");
						else
							returnable=simpleEvalStr(scripted,whichVal,arg4,arg3,"CLANDATA");
					}
				}
				break;
			}
			case 98: // clanqualifies
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"ccR"); /* tt[t+0] */
				final String arg1=tt[t+0];
				final String arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+1]);
				final Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if(arg2.length()==0)
				{
					logError(scripted,"CLANQUALIFIES","Syntax",funcParms);
					return returnable;
				}
				final Clan C=CMLib.clans().findClan(arg2);
				if((C!=null)&&(E instanceof MOB))
				{
					final MOB mob=(MOB)E;
					if(C.isOnlyFamilyApplicants()
					&&(!CMLib.clans().isFamilyOfMembership(mob,C.getMemberList())))
						returnable=false;
					else
					if(CMLib.clans().getClansByCategory(mob, C.getCategory()).size()>CMProps.getMaxClansThisCategory(C.getCategory()))
						returnable=false;
					if(returnable && (!CMLib.masking().maskCheck(C.getBasicRequirementMask(), mob, true)))
						returnable=false;
					else
					if(returnable && (CMLib.masking().maskCheck(C.getAcceptanceSettings(),mob,true)))
						returnable=false;
				}
				else
				{
					logError(scripted,"CLANQUALIFIES","Unknown clan "+arg2+" or "+arg1+" is not a mob",funcParms);
					return returnable;
				}
				break;
			}
			case 65: // clan
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"ccR"); /* tt[t+0] */
				final String arg1=tt[t+0];
				final String arg2=tt[t+1];
				final String arg3=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]);
				final Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if(arg2.length()==0)
				{
					logError(scripted,"CLAN","Syntax",funcParms);
					return returnable;
				}
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
				{
					String clanID="";
					Clan C=CMLib.clans().findRivalrousClan((MOB)E);
					if(C==null)
						C=((MOB)E).clans().iterator().hasNext()?((MOB)E).clans().iterator().next().first:null;
					if(C!=null)
						clanID=C.clanID();
					if(arg2.equals("=="))
						returnable=clanID.equalsIgnoreCase(arg3);
					else
					if(arg2.equals("!="))
						returnable=!clanID.equalsIgnoreCase(arg3);
					else
					if(arg2.equals("in"))
						returnable=((MOB)E).getClanRole(arg3)!=null;
					else
					if(arg2.equals("notin"))
						returnable=((MOB)E).getClanRole(arg3)==null;
					else
					{
						logError(scripted,"CLAN","Syntax",funcParms);
						return returnable;
					}
				}
				break;
			}
			case 88: // mood
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"ccR"); /* tt[t+0] */
				final String arg1=tt[t+0];
				final String arg2=tt[t+1];
				final String arg3=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]);
				final Physical P=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if(arg2.length()==0)
				{
					logError(scripted,"MOOD","Syntax",funcParms);
					return returnable;
				}
				if((P==null)||(!(P instanceof MOB)))
					returnable=false;
				else
				{
					final Ability moodA=P.fetchEffect("Mood");
					if(moodA!=null)
					{
						final String sex=moodA.text();
						if(arg2.equals("=="))
							returnable=sex.equalsIgnoreCase(arg3);
						else
						if(arg2.equals("!="))
							returnable=!sex.equalsIgnoreCase(arg3);
						else
						{
							logError(scripted,"MOOD","Syntax",funcParms);
							return returnable;
						}
					}
				}
				break;
			}
			case 21: // class
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"ccR"); /* tt[t+0] */
				final String arg1=tt[t+0];
				final String arg2=tt[t+1];
				final String arg3=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]);
				final Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if((arg2.length()==0)||(arg3.length()==0))
				{
					logError(scripted,"CLASS","Syntax",funcParms);
					return returnable;
				}
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
				{
					final String sex=((MOB)E).charStats().displayClassName().toUpperCase();
					if(arg2.equals("=="))
						returnable=sex.startsWith(arg3);
					else
					if(arg2.equals("!="))
						returnable=!sex.startsWith(arg3);
					else
					{
						logError(scripted,"CLASS","Syntax",funcParms);
						return returnable;
					}
				}
				break;
			}
			case 22: // baseclass
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"ccR"); /* tt[t+0] */
				final String arg1=tt[t+0];
				final String arg2=tt[t+1];
				final String arg3=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]);
				final Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if((arg2.length()==0)||(arg3.length()==0))
				{
					logError(scripted,"CLASS","Syntax",funcParms);
					return returnable;
				}
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
				{
					final String sex=((MOB)E).charStats().getCurrentClass().baseClass().toUpperCase();
					if(arg2.equals("=="))
						returnable=sex.startsWith(arg3);
					else
					if(arg2.equals("!="))
						returnable=!sex.startsWith(arg3);
					else
					{
						logError(scripted,"CLASS","Syntax",funcParms);
						return returnable;
					}
				}
				break;
			}
			case 23: // race
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"ccR"); /* tt[t+0] */
				final String arg1=tt[t+0];
				final String arg2=tt[t+1];
				final String arg3=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]);
				final Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if((arg2.length()==0)||(arg3.length()==0))
				{
					logError(scripted,"RACE","Syntax",funcParms);
					return returnable;
				}
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
				{
					final String sex=((MOB)E).charStats().raceName().toUpperCase();
					if(arg2.equals("=="))
						returnable=sex.startsWith(arg3);
					else
					if(arg2.equals("!="))
						returnable=!sex.startsWith(arg3);
					else
					{
						logError(scripted,"RACE","Syntax",funcParms);
						return returnable;
					}
				}
				break;
			}
			case 24: //racecat
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"ccR"); /* tt[t+0] */
				final String arg1=tt[t+0];
				final String arg2=tt[t+1];
				final String arg3=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]);
				final Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if((arg2.length()==0)||(arg3.length()==0))
				{
					logError(scripted,"RACECAT","Syntax",funcParms);
					return returnable;
				}
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
				{
					final String sex=((MOB)E).charStats().getMyRace().racialCategory().toUpperCase();
					if(arg2.equals("=="))
						returnable=sex.startsWith(arg3);
					else
					if(arg2.equals("!="))
						returnable=!sex.startsWith(arg3);
					else
					{
						logError(scripted,"RACECAT","Syntax",funcParms);
						return returnable;
					}
				}
				break;
			}
			case 25: // goldamt
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"ccr"); /* tt[t+0] */
				final String arg1=tt[t+0];
				final String arg2=tt[t+1];
				final String arg3=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]);
				final Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((arg2.length()==0)||(arg3.length()==0))
				{
					logError(scripted,"GOLDAMT","Syntax",funcParms);
					break;
				}
				if(E==null)
					returnable=false;
				else
				{
					int val1=0;
					if(E instanceof MOB)
						val1=(int)Math.round(CMLib.beanCounter().getTotalAbsoluteValue((MOB)E,CMLib.beanCounter().getCurrency(scripted)));
					else
					if(E instanceof Coins)
						val1=(int)Math.round(((Coins)E).getTotalValue());
					else
					if(E instanceof Item)
						val1=((Item)E).value();
					else
					{
						logError(scripted,"GOLDAMT","Syntax",funcParms);
						return returnable;
					}

					returnable=simpleEval(scripted,""+val1,arg3,arg2,"GOLDAMT");
				}
				break;
			}
			case 78: // exp
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"ccr"); /* tt[t+0] */
				final String arg1=tt[t+0];
				final String arg2=tt[t+1];
				final String arg3=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]);
				final Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if((arg2.length()==0)||(arg3.length()==0))
				{
					logError(scripted,"EXP","Syntax",funcParms);
					break;
				}
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
				{
					final int val1=((MOB)E).getExperience();
					returnable=simpleEval(scripted,""+val1,arg3,arg2,"EXP");
				}
				break;
			}
			case 76: // value
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"cccr"); /* tt[t+0] */
				final String arg1=tt[t+0];
				final Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				final String arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+1]);
				final String arg3=tt[t+2];
				final String arg4=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+3]);
				if((arg2.length()==0)||(arg3.length()==0)||(arg4.length()==0))
				{
					logError(scripted,"VALUE","Syntax",funcParms);
					break;
				}
				if(!CMLib.beanCounter().getAllCurrencies().contains(arg2.toUpperCase()))
				{
					logError(scripted,"VALUE","Syntax",arg2+" is not a valid designated currency.");
					break;
				}
				if(E==null)
					returnable=false;
				else
				{
					int val1=0;
					if(E instanceof MOB)
						val1=(int)Math.round(CMLib.beanCounter().getTotalAbsoluteValue((MOB)E,arg2.toUpperCase()));
					else
					if(E instanceof Coins)
					{
						if(((Coins)E).getCurrency().equalsIgnoreCase(arg2))
							val1=(int)Math.round(((Coins)E).getTotalValue());
					}
					else
					if(E instanceof Item)
						val1=((Item)E).value();
					else
					{
						logError(scripted,"VALUE","Syntax",funcParms);
						return returnable;
					}

					returnable=simpleEval(scripted,""+val1,arg4,arg3,"GOLDAMT");
				}
				break;
			}
			case 26: // objtype
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"ccR"); /* tt[t+0] */
				final String arg1=tt[t+0];
				final String arg2=tt[t+1];
				final String arg3=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]);
				final Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((arg2.length()==0)||(arg3.length()==0))
				{
					logError(scripted,"OBJTYPE","Syntax",funcParms);
					return returnable;
				}
				if(E==null)
					returnable=false;
				else
				{
					final String sex=CMClass.classID(E).toUpperCase();
					if(arg2.equals("=="))
						returnable=sex.indexOf(arg3)>=0;
					else
					if(arg2.equals("!="))
						returnable=sex.indexOf(arg3)<0;
					else
					{
						logError(scripted,"OBJTYPE","Syntax",funcParms);
						return returnable;
					}
				}
				break;
			}
			case 27: // var
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"cCcr"); /* tt[t+0] */
				final String arg1=tt[t+0];
				final String arg2=tt[t+1];
				final String arg3=tt[t+2];
				final String arg4=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+3]);
				final Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((arg2.length()==0)||(arg3.length()==0))
				{
					logError(scripted,"VAR","Syntax",funcParms);
					return returnable;
				}
				final String val=getVar(E,arg1,arg2,source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp);
				if(arg3.equals("=="))
					returnable=val.equals(arg4);
				else
				if(arg3.equals("!="))
					returnable=!val.equals(arg4);
				else
				if(arg3.equals(">"))
					returnable=CMath.s_int(val.trim())>CMath.s_int(arg4.trim());
				else
				if(arg3.equals("<"))
					returnable=CMath.s_int(val.trim())<CMath.s_int(arg4.trim());
				else
				if(arg3.equals(">="))
					returnable=CMath.s_int(val.trim())>=CMath.s_int(arg4.trim());
				else
				if(arg3.equals("<="))
					returnable=CMath.s_int(val.trim())<=CMath.s_int(arg4.trim());
				else
				{
					logError(scripted,"VAR","Syntax",funcParms);
					return returnable;
				}
				break;
			}
			case 41: // eval
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"ccr"); /* tt[t+0] */
				final String val=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+0]);
				final String arg3=tt[t+1];
				final String arg4=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]);
				if(arg3.length()==0)
				{
					logError(scripted,"EVAL","Syntax",funcParms);
					return returnable;
				}
				if(arg3.equals("=="))
					returnable=val.equals(arg4);
				else
				if(arg3.equals("!="))
					returnable=!val.equals(arg4);
				else
				if(arg3.equals(">"))
					returnable=CMath.s_int(val.trim())>CMath.s_int(arg4.trim());
				else
				if(arg3.equals("<"))
					returnable=CMath.s_int(val.trim())<CMath.s_int(arg4.trim());
				else
				if(arg3.equals(">="))
					returnable=CMath.s_int(val.trim())>=CMath.s_int(arg4.trim());
				else
				if(arg3.equals("<="))
					returnable=CMath.s_int(val.trim())<=CMath.s_int(arg4.trim());
				else
				{
					logError(scripted,"EVAL","Syntax",funcParms);
					return returnable;
				}
				break;
			}
			case 40: // number
			{
				final String val=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.cleanBit(funcParms)).trim();
				boolean isnumber=(val.length()>0);
				for(int i=0;i<val.length();i++)
				{
					if(!Character.isDigit(val.charAt(i)))
					{
						isnumber = false;
						break;
					}
				}
				returnable=isnumber;
				break;
			}
			case 42: // randnum
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"ccr"); /* tt[t+0] */
				final String arg1s=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+0]).toUpperCase().trim();
				int arg1=0;
				if(CMath.isMathExpression(arg1s.trim()))
					arg1=CMath.s_parseIntExpression(arg1s.trim());
				else
					arg1=CMParms.parse(arg1s.trim()).size();
				final String arg2=tt[t+1];
				final String arg3s=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]).trim();
				int arg3=0;
				if(CMath.isMathExpression(arg3s.trim()))
					arg3=CMath.s_parseIntExpression(arg3s.trim());
				else
					arg3=CMParms.parse(arg3s.trim()).size();
				arg1=CMLib.dice().roll(1,arg1,0);
				returnable=simpleEval(scripted,""+arg1,""+arg3,arg2,"RANDNUM");
				break;
			}
			case 71: // rand0num
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"ccr"); /* tt[t+0] */
				final String arg1s=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+0]).toUpperCase().trim();
				int arg1=0;
				if(CMath.isMathExpression(arg1s))
					arg1=CMath.s_parseIntExpression(arg1s);
				else
					arg1=CMParms.parse(arg1s).size();
				final String arg2=tt[t+1];
				final String arg3s=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[t+2]).trim();
				int arg3=0;
				if(CMath.isMathExpression(arg3s))
					arg3=CMath.s_parseIntExpression(arg3s);
				else
					arg3=CMParms.parse(arg3s).size();
				arg1=CMLib.dice().roll(1,arg1,-1);
				returnable=simpleEval(scripted,""+arg1,""+arg3,arg2,"RAND0NUM");
				break;
			}
			case 53: // incontainer
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"cr"); /* tt[t+0] */
				final String arg1=tt[t+0];
				final Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				final String arg2=tt[t+1];
				final Environmental E2=getArgumentItem(arg2,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if(E==null)
					returnable=false;
				else
				if(E instanceof MOB)
				{
					if(arg2.length()==0)
						returnable=(((MOB)E).riding()==null);
					else
					if(E2!=null)
						returnable=(((MOB)E).riding()==E2);
					else
						returnable=false;
				}
				else
				if(E instanceof Item)
				{
					if(arg2.length()==0)
						returnable=(((Item)E).container()==null);
					else
					if(E2!=null)
						returnable=(((Item)E).container()==E2);
					else
						returnable=false;
				}
				else
					returnable=false;
				break;
			}
			case 96: // iscontents
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"cr"); /* tt[t+0] */
				final String arg1=tt[t+0];
				final Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				final String arg2=varify(source, target, scripted, monster, primaryItem, secondaryItem, msg, tmp, tt[t+1]);
				if(E==null)
					returnable=false;
				else
				if(E instanceof Rideable)
				{
					if(arg2.length()==0)
						returnable=((Rideable)E).numRiders()==0;
					else
						returnable=CMLib.english().fetchEnvironmental(new XVector<Rider>(((Rideable)E).riders()), arg2, false)!=null;
				}
				if(E instanceof Container)
				{
					if(arg2.length()==0)
						returnable=!((Container)E).hasContent();
					else
						returnable=CMLib.english().fetchEnvironmental(((Container)E).getDeepContents(), arg2, false)!=null;
				}
				else
					returnable=false;
				break;
			}
			case 97: // wornon
			{
				if(tlen==1) 
					tt=parseBits(eval,t,"ccr"); /* tt[t+0] */
				final String arg1=tt[t+0];
				final PhysicalAgent E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				final String arg2=varify(source, target, scripted, monster, primaryItem, secondaryItem, msg, tmp, tt[t+1]);
				final String arg3=varify(source, target, scripted, monster, primaryItem, secondaryItem, msg, tmp, tt[t+2]);
				if((arg2.length()==0)||(arg3.length()==0))
				{
					logError(scripted,"WORNON","Syntax",funcParms);
					return returnable;
				}
				final int wornLoc = CMParms.indexOf(Wearable.CODES.NAMESUP(), arg2.toUpperCase().trim());
				returnable=false;
				if(wornLoc<0)
					logError(scripted,"EVAL","BAD WORNON LOCATION",arg2);
				else
				if(E instanceof MOB)
				{
					final List<Item> items=((MOB)E).fetchWornItems(Wearable.CODES.GET(wornLoc),(short)-2048,(short)0);
					if((items.size()==0)&&(arg3.length()==0))
						returnable=true;
					else
						returnable = CMLib.english().fetchEnvironmental(items, arg3, false)!=null;
				}
				break;
			}
			default:
				logError(scripted,"EVAL","UNKNOWN",CMParms.toListString(tt));
				return false;
			}
			pushEvalBoolean(stack,returnable);
			while((t<tt.length)&&(!tt[t].equals(")")))
				t++;
		}
		else
		{
			logError(scripted,"EVAL","SYNTAX","BAD CONJUCTOR "+tt[t]+": "+CMParms.toListString(tt));
			return false;
		}
		if((stack.size()!=1)||(!(stack.firstElement() instanceof Boolean)))
		{
			logError(scripted,"EVAL","SYNTAX","Unmatched (: "+CMParms.toListString(tt));
			return false;
		}
		return ((Boolean)stack.firstElement()).booleanValue();
	}

	protected void setShopPrice(ShopKeeper shopHere, Environmental E, Object[] tmp)
	{
		if(shopHere instanceof MOB)
		{
			ShopKeeper.ShopPrice price = CMLib.coffeeShops().sellingPrice((MOB)shopHere, null, E, shopHere, shopHere.getShop(), true);
			if(price.experiencePrice>0)
				tmp[SPECIAL_9SHOPHASPRICE] = price.experiencePrice+"xp";
			else
			if(price.questPointPrice>0)
				tmp[SPECIAL_9SHOPHASPRICE] = price.questPointPrice+"qp";
			else
				tmp[SPECIAL_9SHOPHASPRICE] = CMLib.beanCounter().abbreviatedPrice((MOB)shopHere,price.absoluteGoldPrice);
		}
	}
	
	@Override
	public String functify(final PhysicalAgent scripted,
						   final MOB source,
						   final Environmental target,
						   final MOB monster,
						   final Item primaryItem,
						   final Item secondaryItem,
						   final String msg,
						   final Object[] tmp,
						   final String evaluable)
	{
		if(evaluable.length()==0)
			return "";
		final StringBuffer results = new StringBuffer("");
		final int y=evaluable.indexOf('(');
		final int z=evaluable.indexOf(')',y);
		final String preFab=(y>=0)?evaluable.substring(0,y).toUpperCase().trim():"";
		Integer funcCode=funcH.get(preFab);
		if(funcCode==null)
			funcCode=Integer.valueOf(0);
		if((y<0)||(z<y))
		{
			logError(scripted,"()","Syntax",evaluable);
			return "";
		}
		else
		{
			tickStatus=Tickable.STATUS_MISC2+funcCode.intValue();
			final String funcParms=evaluable.substring(y+1,z).trim();
			switch(funcCode.intValue())
			{
			case 1: // rand
			{
				results.append(CMLib.dice().rollPercentage());
				break;
			}
			case 2: // has
			{
				final String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.cleanBit(funcParms));
				final Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				ArrayList<Item> choices=new ArrayList<Item>();
				if(E==null)
					choices=new ArrayList<Item>();
				else
				if(E instanceof MOB)
				{
					for(int i=0;i<((MOB)E).numItems();i++)
					{
						final Item I=((MOB)E).getItem(i);
						if((I!=null)&&(I.amWearingAt(Wearable.IN_INVENTORY))&&(I.container()==null))
							choices.add(I);
					}
				}
				else
				if(E instanceof Item)
				{
					if(E instanceof Container)
						choices.addAll(((Container)E).getDeepContents());
					else
						choices.add((Item)E);
				}
				else
				if(E instanceof Room)
				{
					for(int i=0;i<((Room)E).numItems();i++)
					{
						final Item I=((Room)E).getItem(i);
						if((I!=null)&&(I.container()==null))
							choices.add(I);
					}
				}
				if(choices.size()>0)
					results.append(choices.get(CMLib.dice().roll(1,choices.size(),-1)).name());
				break;
			}
			case 74: // hasnum
			{
				final String arg1=CMParms.getCleanBit(funcParms,0);
				final String item=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(funcParms,1));
				final Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((item.length()==0)||(E==null))
					logError(scripted,"HASNUM","Syntax",funcParms);
				else
				{
					Item I=null;
					int num=0;
					if(E instanceof MOB)
					{
						final MOB M=(MOB)E;
						for(int i=0;i<M.numItems();i++)
						{
							I=M.getItem(i);
							if(I==null)
								break;
							if((item.equalsIgnoreCase("all"))
							||(CMLib.english().containsString(I.Name(),item)))
								num++;
						}
						results.append(""+num);
					}
					else
					if(E instanceof Item)
					{
						num=CMLib.english().containsString(E.name(),item)?1:0;
						results.append(""+num);
					}
					else
					if(E instanceof Room)
					{
						final Room R=(Room)E;
						for(int i=0;i<R.numItems();i++)
						{
							I=R.getItem(i);
							if(I==null)
								break;
							if((item.equalsIgnoreCase("all"))
							||(CMLib.english().containsString(I.Name(),item)))
								num++;
						}
						results.append(""+num);
					}
				}
				break;
			}
			case 3: // worn
			{
				final String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.cleanBit(funcParms));
				final Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				ArrayList<Item> choices=new ArrayList<Item>();
				if(E==null)
					choices=new ArrayList<Item>();
				else
				if(E instanceof MOB)
				{
					for(int i=0;i<((MOB)E).numItems();i++)
					{
						final Item I=((MOB)E).getItem(i);
						if((I!=null)&&(!I.amWearingAt(Wearable.IN_INVENTORY))&&(I.container()==null))
							choices.add(I);
					}
				}
				else
				if((E instanceof Item)&&(!(((Item)E).amWearingAt(Wearable.IN_INVENTORY))))
				{
					if(E instanceof Container)
						choices.addAll(((Container)E).getDeepContents());
					else
						choices.add((Item)E);
				}
				if(choices.size()>0)
					results.append(choices.get(CMLib.dice().roll(1,choices.size(),-1)).name());
				break;
			}
			case 4: // isnpc
			case 5: // ispc
				results.append("[unimplemented function]");
				break;
			case 87: // isbirthday
			{
				final String arg1=CMParms.cleanBit(funcParms);
				final Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if((E!=null)&&(E instanceof MOB)&&(((MOB)E).playerStats()!=null)&&(((MOB)E).playerStats().getBirthday()!=null))
				{
					final MOB mob=(MOB)E;
					final TimeClock C=CMLib.time().localClock(mob.getStartRoom());
					final int day=C.getDayOfMonth();
					final int month=C.getMonth();
					int year=C.getYear();
					final int bday=mob.playerStats().getBirthday()[PlayerStats.BIRTHDEX_DAY];
					final int bmonth=mob.playerStats().getBirthday()[PlayerStats.BIRTHDEX_MONTH];
					if((month>bmonth)||((month==bmonth)&&(day>bday)))
						year++;

					final StringBuffer timeDesc=new StringBuffer("");
					if(C.getDaysInWeek()>0)
					{
						long x=((long)year)*((long)C.getMonthsInYear())*C.getDaysInMonth();
						x=x+((long)(bmonth-1))*((long)C.getDaysInMonth());
						x=x+bmonth;
						timeDesc.append(C.getWeekNames()[(int)(x%C.getDaysInWeek())]+", ");
					}
					timeDesc.append("the "+bday+CMath.numAppendage(bday));
					timeDesc.append(" day of "+C.getMonthNames()[bmonth-1]);
					if(C.getYearNames().length>0)
						timeDesc.append(", "+CMStrings.replaceAll(C.getYearNames()[year%C.getYearNames().length],"#",""+year));
					results.append(timeDesc.toString());
				}
				break;
			}
			case 6: // isgood
			{
				final String arg1=CMParms.cleanBit(funcParms);
				final Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if((E!=null)&&((E instanceof MOB)))
				{
					final Faction.FRange FR=CMLib.factions().getRange(CMLib.factions().AlignID(),((MOB)E).fetchFaction(CMLib.factions().AlignID()));
					if(FR!=null)
						results.append(FR.name());
					else
						results.append(((MOB)E).fetchFaction(CMLib.factions().AlignID()));
				}
				break;
			}
			case 8: // isevil
			{
				final String arg1=CMParms.cleanBit(funcParms);
				final Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if((E!=null)&&((E instanceof MOB)))
					results.append(CMLib.flags().getAlignmentName(E).toLowerCase());
				break;
			}
			case 9: // isneutral
			{
				final String arg1=CMParms.cleanBit(funcParms);
				final Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if((E!=null)&&((E instanceof MOB)))
					results.append(((MOB)E).fetchFaction(CMLib.factions().AlignID()));
				break;
			}
			case 11: // isimmort
				results.append("[unimplemented function]");
				break;
			case 54: // isalive
			{
				final String arg1=CMParms.cleanBit(funcParms);
				final Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if((E!=null)&&((E instanceof MOB))&&(!((MOB)E).amDead()))
					results.append(((MOB)E).healthText(null));
				else
				if(E!=null)
					results.append(E.name()+" is dead.");
				break;
			}
			case 58: // isable
			{
				final String arg1=CMParms.getCleanBit(funcParms,0);
				final Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				final String arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(funcParms,0));
				if((E!=null)&&((E instanceof MOB))&&(!((MOB)E).amDead()))
				{
					final ExpertiseLibrary X=(ExpertiseLibrary)CMLib.expertises().findDefinition(arg2,true);
					if(X!=null)
					{
						final Pair<String,Integer> s=((MOB)E).fetchExpertise(X.ID());
						if(s!=null)
							results.append(s.getKey()+((s.getValue()!=null)?s.getValue().toString():""));
					}
					else
					{
						final Ability A=((MOB)E).findAbility(arg2);
						if(A!=null)
							results.append(""+A.proficiency());
					}
				}
				break;
			}
			case 59: // isopen
			{
				final String arg1=CMParms.cleanBit(funcParms);
				final int dir=CMLib.directions().getGoodDirectionCode(arg1);
				boolean returnable=false;
				if(dir<0)
				{
					final Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
					if((E!=null)&&(E instanceof Container))
						returnable=((Container)E).isOpen();
					else
					if((E!=null)&&(E instanceof Exit))
						returnable=((Exit)E).isOpen();
				}
				else
				if(lastKnownLocation!=null)
				{
					final Exit E=lastKnownLocation.getExitInDir(dir);
					if(E!=null)
						returnable= E.isOpen();
				}
				results.append(""+returnable);
				break;
			}
			case 60: // islocked
			{
				final String arg1=CMParms.cleanBit(funcParms);
				final int dir=CMLib.directions().getGoodDirectionCode(arg1);
				if(dir<0)
				{
					final Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
					if((E!=null)&&(E instanceof Container))
						results.append(((Container)E).keyName());
					else
					if((E!=null)&&(E instanceof Exit))
						results.append(((Exit)E).keyName());
				}
				else
				if(lastKnownLocation!=null)
				{
					final Exit E=lastKnownLocation.getExitInDir(dir);
					if(E!=null)
						results.append(E.keyName());
				}
				break;
			}
			case 62: // callfunc
			{
				final String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(funcParms,0));
				final String arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(funcParms,0));
				String found=null;
				boolean validFunc=false;
				final List<DVector> scripts=getScripts();
				String trigger=null;
				String[] ttrigger=null;
				for(int v=0;v<scripts.size();v++)
				{
					final DVector script2=scripts.get(v);
					if(script2.size()<1)
						continue;
					trigger=((String)script2.elementAt(0,1)).toUpperCase().trim();
					ttrigger=(String[])script2.elementAt(0,2);
					if(getTriggerCode(trigger,ttrigger)==17)
					{
						final String fnamed=CMParms.getCleanBit(trigger,1);
						if(fnamed.equalsIgnoreCase(arg1))
						{
							validFunc=true;
							found=
							execute(scripted,
									source,
									target,
									monster,
									primaryItem,
									secondaryItem,
									script2,
									varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,arg2),
									tmp);
							if(found==null)
								found="";
							break;
						}
					}
				}
				if(!validFunc)
					logError(scripted,"CALLFUNC","Unknown","Function: "+arg1);
				else
					results.append(found);
				break;
			}
			case 61: // strin
			{
				final String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(funcParms,0));
				final String arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(funcParms,0));
				final List<String> V=CMParms.parse(arg1.toUpperCase());
				results.append(V.indexOf(arg2.toUpperCase()));
				break;
			}
			case 55: // ispkill
			{
				final String arg1=CMParms.cleanBit(funcParms);
				final Physical E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if((E==null)||(!(E instanceof MOB)))
					results.append("false");
				else
				if(((MOB)E).isAttributeSet(MOB.Attrib.PLAYERKILL))
					results.append("true");
				else
					results.append("false");
				break;
			}
			case 10: // isfight
			{
				final String arg1=CMParms.cleanBit(funcParms);
				final Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if((E!=null)&&((E instanceof MOB))&&(((MOB)E).isInCombat()))
					results.append(((MOB)E).getVictim().name());
				break;
			}
			case 12: // ischarmed
			{
				final String arg1=CMParms.cleanBit(funcParms);
				final Physical E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if(E!=null)
				{
					final List<Ability> V=CMLib.flags().flaggedAffects(E,Ability.FLAG_CHARMING);
					for(int v=0;v<V.size();v++)
						results.append((V.get(v).name())+" ");
				}
				break;
			}
			case 15: // isfollow
			{
				final String arg1=CMParms.cleanBit(funcParms);
				final Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if((E!=null)&&(E instanceof MOB)&&(((MOB)E).amFollowing()!=null)
				&&(((MOB)E).amFollowing().location()==lastKnownLocation))
					results.append(((MOB)E).amFollowing().name());
				break;
			}
			case 73: // isservant
			{
				final String arg1=CMParms.cleanBit(funcParms);
				final Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if((E!=null)&&(E instanceof MOB)&&(((MOB)E).getLiegeID()!=null)&&(((MOB)E).getLiegeID().length()>0))
					results.append(((MOB)E).getLiegeID());
				break;
			}
			case 95: // isspeaking
			{
				final String arg1=CMParms.cleanBit(funcParms);
				final Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if((E!=null)&&(E instanceof MOB))
				{
					final MOB TM=(MOB)E;
					final Language L=CMLib.utensils().getLanguageSpoken(TM);
					if(L!=null)
						results.append(L.Name());
					else
						results.append("Common");
				}
				break;
			}
			case 56: // name
			case 7: // isname
			{
				final String arg1=CMParms.cleanBit(funcParms);
				final Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if(E!=null)
					results.append(E.name());
				break;
			}
			case 75: // currency
			{
				final String arg1=CMParms.cleanBit(funcParms);
				final Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if(E!=null)
					results.append(CMLib.beanCounter().getCurrency(E));
				break;
			}
			case 14: // affected
			{
				final String arg1=CMParms.cleanBit(funcParms);
				final Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((E instanceof Physical)&&(((Physical)E).numEffects()>0))
					results.append(((Physical)E).effects().nextElement().name());
				break;
			}
			case 69: // isbehave
			{
				final String arg1=CMParms.cleanBit(funcParms);
				final PhysicalAgent E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if(E!=null)
				{
					for(final Enumeration<Behavior> e=E.behaviors();e.hasMoreElements();)
					{
						final Behavior B=e.nextElement();
						if(B!=null)
							results.append(B.ID()+" ");
					}
				}
				break;
			}
			case 70: // ipaddress
			{
				final String arg1=CMParms.cleanBit(funcParms);
				final Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if((E!=null)&&(E instanceof MOB)&&(!((MOB)E).isMonster()))
					results.append(((MOB)E).session().getAddress());
				break;
			}
			case 28: // questwinner
			{
				final String arg1=CMParms.cleanBit(funcParms);
				final Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if((E!=null)&&(E instanceof MOB)&&(!((MOB)E).isMonster()))
				{
					for(int q=0;q<CMLib.quests().numQuests();q++)
					{
						final Quest Q=CMLib.quests().fetchQuest(q);
						if((Q!=null)&&(Q.wasWinner(E.Name())))
							results.append(Q.name()+" ");
					}
				}
				break;
			}
			case 93: // questscripted
			{
				final String arg1=CMParms.cleanBit(funcParms);
				final PhysicalAgent E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if((E!=null)&&(E instanceof MOB)&&(!((MOB)E).isMonster()))
				{
					for(final Enumeration<ScriptingEngine> e=E.scripts();e.hasMoreElements();)
					{
						final ScriptingEngine SE=e.nextElement();
						if((SE!=null)&&(SE.defaultQuestName()!=null)&&(SE.defaultQuestName().length()>0))
						{
							final Quest Q=CMLib.quests().fetchQuest(SE.defaultQuestName());
							if(Q!=null)
								results.append(Q.name()+" ");
							else
								results.append(SE.defaultQuestName()+" ");
						}
					}
				}
				break;
			}
			case 30: // questobj
			{
				String questName=CMParms.cleanBit(funcParms);
				questName=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,questName);
				final Quest Q=getQuest(questName);
				if(Q==null)
				{
					logError(scripted,"QUESTOBJ","Unknown","Quest: "+questName);
					break;
				}
				final StringBuffer list=new StringBuffer("");
				int num=1;
				Environmental E=Q.getQuestItem(num);
				while(E!=null)
				{
					if(E.Name().indexOf(' ')>=0)
						list.append("\""+E.Name()+"\" ");
					else
						list.append(E.Name()+" ");
					num++;
					E=Q.getQuestItem(num);
				}
				results.append(list.toString().trim());
				break;
			}
			case 94: // questroom
			{
				String questName=CMParms.cleanBit(funcParms);
				questName=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,questName);
				final Quest Q=getQuest(questName);
				if(Q==null)
				{
					logError(scripted,"QUESTOBJ","Unknown","Quest: "+questName);
					break;
				}
				final StringBuffer list=new StringBuffer("");
				int num=1;
				Environmental E=Q.getQuestRoom(num);
				while(E!=null)
				{
					final String roomID=CMLib.map().getExtendedRoomID((Room)E);
					if(roomID.indexOf(' ')>=0)
						list.append("\""+roomID+"\" ");
					else
						list.append(roomID+" ");
					num++;
					E=Q.getQuestRoom(num);
				}
				results.append(list.toString().trim());
				break;
			}
			case 29: // questmob
			{
				String questName=CMParms.cleanBit(funcParms);
				questName=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,questName);
				final Quest Q=getQuest(questName);
				if(Q==null)
				{
					logError(scripted,"QUESTOBJ","Unknown","Quest: "+questName);
					break;
				}
				final StringBuffer list=new StringBuffer("");
				int num=1;
				Environmental E=Q.getQuestMob(num);
				while(E!=null)
				{
					if(E.Name().indexOf(' ')>=0)
						list.append("\""+E.Name()+"\" ");
					else
						list.append(E.Name()+" ");
					num++;
					E=Q.getQuestMob(num);
				}
				results.append(list.toString().trim());
				break;
			}
			case 31: // isquestmobalive
			{
				String questName=CMParms.cleanBit(funcParms);
				questName=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,questName);
				final Quest Q=getQuest(questName);
				if(Q==null)
				{
					logError(scripted,"QUESTOBJ","Unknown","Quest: "+questName);
					break;
				}
				final StringBuffer list=new StringBuffer("");
				int num=1;
				MOB E=Q.getQuestMob(num);
				while(E!=null)
				{
					if(CMLib.flags().isInTheGame(E,true))
					{
						if(E.Name().indexOf(' ')>=0)
							list.append("\""+E.Name()+"\" ");
						else
							list.append(E.Name()+" ");
					}
					num++;
					E=Q.getQuestMob(num);
				}
				results.append(list.toString().trim());
				break;
			}
			case 49: // hastattoo
			{
				final String arg1=CMParms.cleanBit(funcParms);
				final Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if((E!=null)&&(E instanceof MOB))
				{
					for(Enumeration<Tattoo> t = ((MOB)E).tattoos();t.hasMoreElements();)
						results.append(t.nextElement().ID()).append(" ");
				}
				break;
			}
			case 99: // hasacctattoo
			{
				final String arg1=CMParms.cleanBit(funcParms);
				final Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if((E!=null)&&(E instanceof MOB)&&(((MOB)E).playerStats()!=null)&&(((MOB)E).playerStats().getAccount()!=null))
				{
					for(Enumeration<Tattoo> t = ((MOB)E).playerStats().getAccount().tattoos();t.hasMoreElements();)
						results.append(t.nextElement().ID()).append(" ");
				}
				break;
			}
			case 32: // nummobsinarea
			{
				String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.cleanBit(funcParms));
				int num=0;
				MaskingLibrary.CompiledZMask MASK=null;
				if((arg1.toUpperCase().startsWith("MASK")&&(arg1.substring(4).trim().startsWith("="))))
				{
					arg1=arg1.substring(4).trim();
					arg1=arg1.substring(1).trim();
					MASK=CMLib.masking().maskCompile(arg1);
				}
				for(final Enumeration<Room> e=lastKnownLocation.getArea().getProperMap();e.hasMoreElements();)
				{
					final Room R=e.nextElement();
					if(arg1.equals("*"))
						num+=R.numInhabitants();
					else
					{
						for(int m=0;m<R.numInhabitants();m++)
						{
							final MOB M=R.fetchInhabitant(m);
							if(M==null)
								continue;
							if(MASK!=null)
							{
								if(CMLib.masking().maskCheck(MASK,M,true))
									num++;
							}
							else
							if(CMLib.english().containsString(M.name(),arg1))
								num++;
						}
					}
				}
				results.append(num);
				break;
			}
			case 33: // nummobs
			{
				int num=0;
				String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.cleanBit(funcParms));
				MaskingLibrary.CompiledZMask MASK=null;
				if((arg1.toUpperCase().startsWith("MASK")&&(arg1.substring(4).trim().startsWith("="))))
				{
					arg1=arg1.substring(4).trim();
					arg1=arg1.substring(1).trim();
					MASK=CMLib.masking().maskCompile(arg1);
				}
				try
				{
					for(final Enumeration e=CMLib.map().rooms();e.hasMoreElements();)
					{
						final Room R=(Room)e.nextElement();
						for(int m=0;m<R.numInhabitants();m++)
						{
							final MOB M=R.fetchInhabitant(m);
							if(M==null)
								continue;
							if(MASK!=null)
							{
								if(CMLib.masking().maskCheck(MASK,M,true))
									num++;
							}
							else
							if(CMLib.english().containsString(M.name(),arg1))
								num++;
						}
					}
				}
				catch(final NoSuchElementException nse)
				{
				}
				results.append(num);
				break;
			}
			case 34: // numracesinarea
			{
				int num=0;
				final String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.cleanBit(funcParms));
				Room R=null;
				MOB M=null;
				for(final Enumeration e=lastKnownLocation.getArea().getProperMap();e.hasMoreElements();)
				{
					R=(Room)e.nextElement();
					for(int m=0;m<R.numInhabitants();m++)
					{
						M=R.fetchInhabitant(m);
						if((M!=null)&&(M.charStats().raceName().equalsIgnoreCase(arg1)))
							num++;
					}
				}
				results.append(num);
				break;
			}
			case 35: // numraces
			{
				int num=0;
				final String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.cleanBit(funcParms));
				Room R=null;
				MOB M=null;
				try
				{
					for(final Enumeration e=CMLib.map().rooms();e.hasMoreElements();)
					{
						R=(Room)e.nextElement();
						for(int m=0;m<R.numInhabitants();m++)
						{
							M=R.fetchInhabitant(m);
							if((M!=null)&&(M.charStats().raceName().equalsIgnoreCase(arg1)))
								num++;
						}
					}
				}
				catch (final NoSuchElementException nse)
				{
				}
				results.append(num);
				break;
			}
			case 16: // hitprcnt
			{
				final String arg1=CMParms.cleanBit(funcParms);
				final Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if((E!=null)&&(E instanceof MOB))
				{
					final double hitPctD=CMath.div(((MOB)E).curState().getHitPoints(),((MOB)E).maxState().getHitPoints());
					final int val1=(int)Math.round(hitPctD*100.0);
					results.append(val1);
				}
				break;
			}
			case 50: // isseason
			{
				if(monster.location()!=null)
					results.append(monster.location().getArea().getTimeObj().getSeasonCode().toString());
				break;
			}
			case 51: // isweather
			{
				if(monster.location()!=null)
					results.append(Climate.WEATHER_DESCS[monster.location().getArea().getClimateObj().weatherType(monster.location())]);
				break;
			}
			case 57: // ismoon
			{
				if(monster.location()!=null)
					results.append(monster.location().getArea().getTimeObj().getMoonPhase(monster.location()).toString());
				break;
			}
			case 38: // istime
			{
				if(lastKnownLocation!=null)
					results.append(lastKnownLocation.getArea().getTimeObj().getTODCode().getDesc().toLowerCase());
				break;
			}
			case 39: // isday
			{
				if(lastKnownLocation!=null)
					results.append(""+lastKnownLocation.getArea().getTimeObj().getDayOfMonth());
				break;
			}
			case 43: // roommob
			{
				final String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.cleanBit(funcParms));
				Environmental which=null;
				if(lastKnownLocation!=null)
				{
					if(CMath.isInteger(arg1.trim()))
						which=lastKnownLocation.fetchInhabitant(CMath.s_int(arg1.trim())-1);
					else
						which=lastKnownLocation.fetchInhabitant(arg1.trim());
					if(which!=null)
					{
						final Vector<MOB> list=new Vector<MOB>();
						for(int i=0;i<lastKnownLocation.numInhabitants();i++)
						{
							final MOB M=lastKnownLocation.fetchInhabitant(i);
							if(M!=null)
								list.addElement(M);
						}
						results.append(CMLib.english().getContextName(list,which));
					}
				}
				break;
			}
			case 44: // roomitem
			{
				final String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.cleanBit(funcParms));
				Environmental which=null;
				int ct=1;
				if(lastKnownLocation!=null)
				{
					final Vector<Item> list=new Vector<Item>();
					for(int i=0;i<lastKnownLocation.numItems();i++)
					{
						final Item I=lastKnownLocation.getItem(i);
						if((I!=null)&&(I.container()==null))
						{
							list.addElement(I);
							if(ct==CMath.s_int(arg1.trim()))
							{
								which = I;
								break;
							}
							ct++;
						}
					}
					if(which!=null)
						results.append(CMLib.english().getContextName(list,which));
				}
				break;
			}
			case 45: // nummobsroom
			{
				int num=0;
				if(lastKnownLocation!=null)
				{
					num=lastKnownLocation.numInhabitants();
					String name=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.cleanBit(funcParms));
					if((name.length()>0)&&(!name.equalsIgnoreCase("*")))
					{
						num=0;
						MaskingLibrary.CompiledZMask MASK=null;
						if((name.toUpperCase().startsWith("MASK")&&(name.substring(4).trim().startsWith("="))))
						{
							name=name.substring(4).trim();
							name=name.substring(1).trim();
							MASK=CMLib.masking().maskCompile(name);
						}
						for(int i=0;i<lastKnownLocation.numInhabitants();i++)
						{
							final MOB M=lastKnownLocation.fetchInhabitant(i);
							if(M==null)
								continue;
							if(MASK!=null)
							{
								if(CMLib.masking().maskCheck(MASK,M,true))
									num++;
							}
							else
							if(CMLib.english().containsString(M.Name(),name)
							||CMLib.english().containsString(M.displayText(),name))
								num++;
						}
					}
				}
				results.append(""+num);
				break;
			}
			case 63: // numpcsroom
			{
				if(lastKnownLocation!=null)
					results.append(""+lastKnownLocation.numPCInhabitants());
				break;
			}
			case 79: // numpcsarea
			{
				if(lastKnownLocation!=null)
				{
					int num=0;
					for(final Session S : CMLib.sessions().localOnlineIterable())
					{
						if((S.mob().location()!=null)&&(S.mob().location().getArea()==lastKnownLocation.getArea()))
							num++;
					}
					results.append(""+num);
				}
				break;
			}
			case 77: // explored
			{
				final String whom=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(funcParms,0));
				final String where=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(funcParms,1));
				final Environmental E=getArgumentMOB(whom,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if(E instanceof MOB)
				{
					Area A=null;
					if(!where.equalsIgnoreCase("world"))
					{
						A=CMLib.map().getArea(where);
						if(A==null)
						{
							final Environmental E2=getArgumentItem(where,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
							if(E2!=null)
								A=CMLib.map().areaLocation(E2);
						}
					}
					if((lastKnownLocation!=null)
					&&((A!=null)||(where.equalsIgnoreCase("world"))))
					{
						int pct=0;
						final MOB M=(MOB)E;
						if(M.playerStats()!=null)
							pct=M.playerStats().percentVisited(M,A);
						results.append(""+pct);
					}
				}
				break;
			}
			case 72: // faction
			{
				final String arg1=CMParms.getCleanBit(funcParms,0);
				final String arg2=CMParms.getPastBit(funcParms,0);
				final Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				final Faction F=CMLib.factions().getFaction(arg2);
				if(F==null)
					logError(scripted,"FACTION","Unknown Faction",arg1);
				else
				if((E!=null)&&(E instanceof MOB)&&(((MOB)E).hasFaction(F.factionID())))
				{
					final int value=((MOB)E).fetchFaction(F.factionID());
					final Faction.FRange FR=CMLib.factions().getRange(F.factionID(),value);
					if(FR!=null)
						results.append(FR.name());
				}
				break;
			}
			case 46: // numitemsroom
			{
				int ct=0;
				if(lastKnownLocation!=null)
				for(int i=0;i<lastKnownLocation.numItems();i++)
				{
					final Item I=lastKnownLocation.getItem(i);
					if((I!=null)&&(I.container()==null))
						ct++;
				}
				results.append(""+ct);
				break;
			}
			case 47: //mobitem
			{
				final String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(funcParms,0));
				final String arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(funcParms,0));
				MOB M=null;
				if(lastKnownLocation!=null)
				{
					if(CMath.isInteger(arg1.trim()))
						M=lastKnownLocation.fetchInhabitant(CMath.s_int(arg1.trim())-1);
					else
						M=lastKnownLocation.fetchInhabitant(arg1.trim());
				}
				Item which=null;
				int ct=1;
				if(M!=null)
				{
					for(int i=0;i<M.numItems();i++)
					{
						final Item I=M.getItem(i);
						if((I!=null)&&(I.container()==null))
						{
							if(ct==CMath.s_int(arg2.trim()))
							{
								which = I;
								break;
							}
							ct++;
						}
					}
				}
				if(which!=null)
					results.append(which.name());
				break;
			}
			case 100: // shopitem
			{
				final String arg1raw=CMParms.getCleanBit(funcParms,0);
				final String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,arg1raw);
				final String arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(funcParms,0));
				PhysicalAgent where=null;
				if(lastKnownLocation!=null)
				{
					if(CMath.isInteger(arg1.trim()))
						where=lastKnownLocation.fetchInhabitant(CMath.s_int(arg1.trim())-1);
					else
						where=lastKnownLocation.fetchInhabitant(arg1.trim());
					if(where == null)
						where=this.getArgumentItem(arg1raw, source, monster, scripted, target, primaryItem, secondaryItem, msg, tmp);
					if(where == null)
						where=this.getArgumentMOB(arg1raw, source, monster, target, primaryItem, secondaryItem, msg, tmp);
				}
				Environmental which=null;
				int ct=1;
				if(where!=null)
				{
					ShopKeeper shopHere = CMLib.coffeeShops().getShopKeeper(where);
					if((shopHere == null)&&(scripted instanceof Item))
						shopHere=CMLib.coffeeShops().getShopKeeper(((Item)where).owner());
					if((shopHere == null)&&(scripted instanceof MOB))
						shopHere=CMLib.coffeeShops().getShopKeeper(((MOB)where).location());
					if(shopHere == null)
						shopHere=CMLib.coffeeShops().getShopKeeper(lastKnownLocation);
					if(shopHere!=null)
					{
						CoffeeShop shop = shopHere.getShop();
						if(shop != null)
						{
							for(Iterator<Environmental> i=shop.getStoreInventory();i.hasNext();ct++)
							{
								Environmental E=i.next();
								if(ct==CMath.s_int(arg2.trim()))
								{
									which = E;
									setShopPrice(shopHere,E,tmp);
									break;
								}
							}
						}
					}
				}
				if(which!=null)
					results.append(which.name());
				break;
			}
			case 101: // numitemsshop
			{
				String arg1raw = CMParms.cleanBit(funcParms);
				final String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,arg1raw);
				PhysicalAgent which=null;
				if(lastKnownLocation!=null)
				{
					if(CMath.isInteger(arg1.trim()))
						which=lastKnownLocation.fetchInhabitant(CMath.s_int(arg1.trim())-1);
					else
						which=lastKnownLocation.fetchInhabitant(arg1);
					if(which == null)
						which=this.getArgumentItem(arg1raw, source, monster, scripted, target, primaryItem, secondaryItem, msg, tmp);
					if(which == null)
						which=this.getArgumentMOB(arg1raw, source, monster, target, primaryItem, secondaryItem, msg, tmp);
				}
				int ct=0;
				if(which!=null)
				{
					ShopKeeper shopHere = CMLib.coffeeShops().getShopKeeper(which);
					if((shopHere == null)&&(scripted instanceof Item))
						shopHere=CMLib.coffeeShops().getShopKeeper(((Item)which).owner());
					if((shopHere == null)&&(scripted instanceof MOB))
						shopHere=CMLib.coffeeShops().getShopKeeper(((MOB)which).location());
					if(shopHere == null)
						shopHere=CMLib.coffeeShops().getShopKeeper(lastKnownLocation);
					if(shopHere!=null)
					{
						CoffeeShop shop = shopHere.getShop();
						if(shop != null)
						{
							for(Iterator<Environmental> i=shop.getStoreInventory();i.hasNext();i.next())
							{
								ct++;
							}
						}
					}
				}
				results.append(""+ct);
				break;
			}
			case 102: // shophas
			{
				final String arg1raw=CMParms.cleanBit(funcParms);
				final String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,arg1raw);
				PhysicalAgent where=null;
				if(lastKnownLocation!=null)
				{
					if(CMath.isInteger(arg1.trim()))
						where=lastKnownLocation.fetchInhabitant(CMath.s_int(arg1.trim())-1);
					else
						where=lastKnownLocation.fetchInhabitant(arg1.trim());
					if(where == null)
						where=this.getArgumentItem(arg1raw, source, monster, scripted, target, primaryItem, secondaryItem, msg, tmp);
					if(where == null)
						where=this.getArgumentMOB(arg1raw, source, monster, target, primaryItem, secondaryItem, msg, tmp);
				}
				if(where!=null)
				{
					ShopKeeper shopHere = CMLib.coffeeShops().getShopKeeper(where);
					if((shopHere == null)&&(scripted instanceof Item))
						shopHere=CMLib.coffeeShops().getShopKeeper(((Item)where).owner());
					if((shopHere == null)&&(scripted instanceof MOB))
						shopHere=CMLib.coffeeShops().getShopKeeper(((MOB)where).location());
					if(shopHere == null)
						shopHere=CMLib.coffeeShops().getShopKeeper(lastKnownLocation);
					if(shopHere!=null)
					{
						CoffeeShop shop = shopHere.getShop();
						if(shop != null)
						{
							int ct=0;
							for(Iterator<Environmental> i=shop.getStoreInventory();i.hasNext();i.next())
								ct++;
							int which=CMLib.dice().roll(1, ct, -1);
							ct=0;
							for(Iterator<Environmental> i=shop.getStoreInventory();i.hasNext();ct++)
							{
								final Environmental E=i.next();
								if(which == ct)
								{
									results.append(E.Name());
									setShopPrice(shopHere,E,tmp);
									break;
								}
							}
						}
					}
				}
				break;
			}
			case 48: // numitemsmob
			{
				final String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.cleanBit(funcParms));
				MOB which=null;
				if(lastKnownLocation!=null)
				{
					if(CMath.isInteger(arg1.trim()))
						which=lastKnownLocation.fetchInhabitant(CMath.s_int(arg1.trim())-1);
					else
						which=lastKnownLocation.fetchInhabitant(arg1);
				}
				int ct=0;
				if(which!=null)
				{
					for(int i=0;i<which.numItems();i++)
					{
						final Item I=which.getItem(i);
						if((I!=null)&&(I.container()==null))
							ct++;
					}
				}
				results.append(""+ct);
				break;
			}
			case 36: // ishere
			{
				if(lastKnownLocation!=null)
					results.append(lastKnownLocation.getArea().name());
				break;
			}
			case 17: // inroom
			{
				final String arg1=CMParms.cleanBit(funcParms);
				final Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((E==null)||arg1.length()==0)
					results.append(CMLib.map().getExtendedRoomID(lastKnownLocation));
				else
					results.append(CMLib.map().getExtendedRoomID(CMLib.map().roomLocation(E)));
				break;
			}
			case 90: // inarea
			{
				final String arg1=CMParms.cleanBit(funcParms);
				final Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((E==null)||arg1.length()==0)
					results.append(lastKnownLocation==null?"Nowhere":lastKnownLocation.getArea().Name());
				else
				{
					final Room R=CMLib.map().roomLocation(E);
					results.append(R==null?"Nowhere":R.getArea().Name());
				}
				break;
			}
			case 89: // isrecall
			{
				final String arg1=CMParms.cleanBit(funcParms);
				final Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if(E!=null)
					results.append(CMLib.map().getExtendedRoomID(CMLib.map().getStartRoom(E)));
				break;
			}
			case 37: // inlocale
			{
				final String parms=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.cleanBit(funcParms));
				if(parms.trim().length()==0)
				{
					if(lastKnownLocation!=null)
						results.append(lastKnownLocation.name());
				}
				else
				{
					final Environmental E=getArgumentItem(parms,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
					if(E!=null)
					{
						final Room R=CMLib.map().roomLocation(E);
						if(R!=null)
							results.append(R.name());
					}
				}
				break;
			}
			case 18: // sex
			{
				final String arg1=CMParms.cleanBit(funcParms);
				final Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if((E!=null)&&(E instanceof MOB))
					results.append(((MOB)E).charStats().genderName());
				break;
			}
			case 91: // datetime
			{
				final String arg1=CMParms.getCleanBit(funcParms,0);
				final int index=CMParms.indexOf(ScriptingEngine.DATETIME_ARGS,arg1.toUpperCase().trim());
				if(index<0)
					logError(scripted,"DATETIME","Syntax","Unknown arg: "+arg1+" for "+scripted.name());
				else
				if(CMLib.map().areaLocation(scripted)!=null)
				{
					switch(index)
					{
					case 2:
						results.append(CMLib.map().areaLocation(scripted).getTimeObj().getDayOfMonth());
						break;
					case 3:
						results.append(CMLib.map().areaLocation(scripted).getTimeObj().getDayOfMonth());
						break;
					case 4:
						results.append(CMLib.map().areaLocation(scripted).getTimeObj().getMonth());
						break;
					case 5:
						results.append(CMLib.map().areaLocation(scripted).getTimeObj().getYear());
						break;
					default:
						results.append(CMLib.map().areaLocation(scripted).getTimeObj().getHourOfDay()); break;
					}
				}
				break;
			}
			case 13: // stat
			{
				final String arg1=CMParms.getCleanBit(funcParms,0);
				final String arg2=CMParms.getPastBitClean(funcParms,0);
				final Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if(E!=null)
				{
					final String val=getStatValue(E,arg2);
					if(val==null)
					{
						logError(scripted,"STAT","Syntax","Unknown stat: "+arg2+" for "+E.name());
						break;
					}
					results.append(val);
					break;
				}
				break;
			}
			case 52: // gstat
			{
				final String arg1=CMParms.getCleanBit(funcParms,0);
				final String arg2=CMParms.getPastBitClean(funcParms,0);
				final Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if(E!=null)
				{
					final String val=getGStatValue(E,arg2);
					if(val==null)
					{
						logError(scripted,"GSTAT","Syntax","Unknown stat: "+arg2+" for "+E.name());
						break;
					}
					results.append(val);
					break;
				}
				break;
			}
			case 19: // position
			{
				final String arg1=CMParms.cleanBit(funcParms);
				final Physical P=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if(P!=null)
				{
					final String sex;
					if(CMLib.flags().isSleeping(P))
						sex="SLEEPING";
					else
					if(CMLib.flags().isSitting(P))
						sex="SITTING";
					else
						sex="STANDING";
					results.append(sex);
					break;
				}
				break;
			}
			case 20: // level
			{
				final String arg1=CMParms.cleanBit(funcParms);
				final Physical P=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if(P!=null)
					results.append(P.phyStats().level());
				break;
			}
			case 80: // questpoints
			{
				final String arg1=CMParms.cleanBit(funcParms);
				final Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if(E instanceof MOB)
					results.append(((MOB)E).getQuestPoint());
				break;
			}
			case 83: // qvar
			{
				final String arg1=CMParms.getCleanBit(funcParms,0);
				final String arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(funcParms,0));
				if((arg1.length()!=0)&&(arg2.length()!=0))
				{
					final Quest Q=getQuest(arg1);
					if(Q!=null)
						results.append(Q.getStat(arg2));
				}
				break;
			}
			case 84: // math
			{
				final String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.cleanBit(funcParms));
				results.append(""+Math.round(CMath.s_parseMathExpression(arg1)));
				break;
			}
			case 85: // islike
			{
				final String arg1=CMParms.cleanBit(funcParms);
				results.append(CMLib.masking().maskDesc(arg1));
				break;
			}
			case 86: // strcontains
			{
				results.append("[unimplemented function]");
				break;
			}
			case 81: // trains
			{
				final String arg1=CMParms.cleanBit(funcParms);
				final Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if(E instanceof MOB)
					results.append(((MOB)E).getTrains());
				break;
			}
			case 92: // isodd
			{
				final String val=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp ,CMParms.cleanBit(funcParms)).trim();
				boolean isodd = false;
				if( CMath.isLong( val ) )
				{
					isodd = (CMath.s_long(val) %2 == 1);
				}
				if( isodd )
				{
					results.append( CMath.s_long( val.trim() ) );
				}
				break;
			}
			case 82: // pracs
			{
				final String arg1=CMParms.cleanBit(funcParms);
				final Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if(E instanceof MOB)
					results.append(((MOB)E).getPractices());
				break;
			}
			case 68: // clandata
			{
				final String arg1=CMParms.getCleanBit(funcParms,0);
				final String arg2=CMParms.getPastBitClean(funcParms,0);
				final Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				String clanID=null;
				if((E!=null)&&(E instanceof MOB))
				{
					Clan C=CMLib.clans().findRivalrousClan((MOB)E);
					if(C==null)
						C=((MOB)E).clans().iterator().hasNext()?((MOB)E).clans().iterator().next().first:null;
					if(C!=null)
						clanID=C.clanID();
				}
				else
					clanID=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,arg1);
				final Clan C=CMLib.clans().findClan(clanID);
				if(C!=null)
				{
					if(!C.isStat(arg2))
						logError(scripted,"CLANDATA","RunTime",arg2+" is not a valid clan variable.");
					else
						results.append(C.getStat(arg2));
				}
				break;
			}
			case 98: // clanqualifies
			{
				final String arg1=CMParms.cleanBit(funcParms);
				Clan C=CMLib.clans().getClan(arg1);
				if(C==null)
					C=CMLib.clans().findClan(arg1);
				if(C!=null)
				{
					if(C.getAcceptanceSettings().length()>0)
						results.append(CMLib.masking().maskDesc(C.getAcceptanceSettings()));
					if(C.getBasicRequirementMask().length()>0)
						results.append(CMLib.masking().maskDesc(C.getBasicRequirementMask()));
					if(C.isOnlyFamilyApplicants())
						results.append("Must belong to the family.");
					final int total=CMProps.getMaxClansThisCategory(C.getCategory());
					if(C.getCategory().length()>0)
						results.append("May belong to only "+total+" "+C.getCategory()+" clan. ");
					else
						results.append("May belong to only "+total+" standard clan. ");
				}
				break;
			}
			case 67: // hastitle
			{
				final String arg1=CMParms.getCleanBit(funcParms,0);
				final String arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(funcParms,0));
				final Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if((arg2.length()>0)&&(E instanceof MOB)&&(((MOB)E).playerStats()!=null))
				{
					final MOB M=(MOB)E;
					results.append(M.playerStats().getActiveTitle());
				}
				break;
			}
			case 66: // clanrank
			{
				final String arg1=CMParms.cleanBit(funcParms);
				final Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				Clan C=null;
				if(E instanceof MOB)
				{
					C=CMLib.clans().findRivalrousClan((MOB)E);
					if(C==null)
						C=((MOB)E).clans().iterator().hasNext()?((MOB)E).clans().iterator().next().first:null;
				}
				else
					C=CMLib.clans().findClan(varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,arg1));
				if(C!=null)
				{
					final Pair<Clan,Integer> p=((MOB)E).getClanRole(C.clanID());
					if(p!=null)
						results.append(p.second.toString());
				}
				break;
			}
			case 21: // class
			{
				final String arg1=CMParms.cleanBit(funcParms);
				final Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if((E!=null)&&(E instanceof MOB))
					results.append(((MOB)E).charStats().displayClassName());
				break;
			}
			case 64: // deity
			{
				final String arg1=CMParms.cleanBit(funcParms);
				final Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if((E!=null)&&(E instanceof MOB))
				{
					final String sex=((MOB)E).getWorshipCharID();
					results.append(sex);
				}
				break;
			}
			case 65: // clan
			{
				final String arg1=CMParms.cleanBit(funcParms);
				final Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if((E!=null)&&(E instanceof MOB))
				{
					Clan C=CMLib.clans().findRivalrousClan((MOB)E);
					if(C==null)
						C=((MOB)E).clans().iterator().hasNext()?((MOB)E).clans().iterator().next().first:null;
					if(C!=null)
						results.append(C.clanID());
				}
				break;
			}
			case 88: // mood
			{
				final String arg1=CMParms.cleanBit(funcParms);
				final Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if(E instanceof MOB)
				{
					final Ability moodA=((MOB)E).fetchEffect("Mood");
					if(moodA!=null)
						results.append(CMStrings.capitalizeAndLower(moodA.text()));
				}
				break;
			}
			case 22: // baseclass
			{
				final String arg1=CMParms.cleanBit(funcParms);
				final Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if((E!=null)&&(E instanceof MOB))
					results.append(((MOB)E).charStats().getCurrentClass().baseClass());
				break;
			}
			case 23: // race
			{
				final String arg1=CMParms.cleanBit(funcParms);
				final Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if((E!=null)&&(E instanceof MOB))
					results.append(((MOB)E).charStats().raceName());
				break;
			}
			case 24: //racecat
			{
				final String arg1=CMParms.cleanBit(funcParms);
				final Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if((E!=null)&&(E instanceof MOB))
					results.append(((MOB)E).charStats().getMyRace().racialCategory());
				break;
			}
			case 25: // goldamt
			{
				final String arg1=CMParms.cleanBit(funcParms);
				final Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if(E==null)
					results.append(false);
				else
				{
					int val1=0;
					if(E instanceof MOB)
						val1=(int)Math.round(CMLib.beanCounter().getTotalAbsoluteValue((MOB)E,CMLib.beanCounter().getCurrency(scripted)));
					else
					if(E instanceof Coins)
						val1=(int)Math.round(((Coins)E).getTotalValue());
					else
					if(E instanceof Item)
						val1=((Item)E).value();
					else
					{
						logError(scripted,"GOLDAMT","Syntax",funcParms);
						return results.toString();
					}
					results.append(val1);
				}
				break;
			}
			case 78: // exp
			{
				final String arg1=CMParms.cleanBit(funcParms);
				final Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if(E==null)
					results.append(false);
				else
				{
					int val1=0;
					if(E instanceof MOB)
						val1=((MOB)E).getExperience();
					results.append(val1);
				}
				break;
			}
			case 76: // value
			{
				final String arg1=CMParms.getCleanBit(funcParms,0);
				final String arg2=CMParms.getPastBitClean(funcParms,0);
				if(!CMLib.beanCounter().getAllCurrencies().contains(arg2.toUpperCase()))
				{
					logError(scripted,"VALUE","Syntax",arg2+" is not a valid designated currency.");
					return results.toString();
				}
				final Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if(E==null)
					results.append(false);
				else
				{
					int val1=0;
					if(E instanceof MOB)
						val1=(int)Math.round(CMLib.beanCounter().getTotalAbsoluteValue((MOB)E,arg2));
					else
					if(E instanceof Coins)
					{
						if(((Coins)E).getCurrency().equalsIgnoreCase(arg2))
							val1=(int)Math.round(((Coins)E).getTotalValue());
					}
					else
					if(E instanceof Item)
						val1=((Item)E).value();
					else
					{
						logError(scripted,"GOLDAMT","Syntax",funcParms);
						return results.toString();
					}
					results.append(val1);
				}
				break;
			}
			case 26: // objtype
			{
				final String arg1=CMParms.cleanBit(funcParms);
				final Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if(E!=null)
				{
					final String sex=CMClass.classID(E).toLowerCase();
					results.append(sex);
				}
				break;
			}
			case 53: // incontainer
			{
				final String arg1=CMParms.cleanBit(funcParms);
				final Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if(E!=null)
				{
					if(E instanceof MOB)
					{
						if(((MOB)E).riding()!=null)
							results.append(((MOB)E).riding().Name());
					}
					else
					if(E instanceof Item)
					{
						if(((Item)E).riding()!=null)
							results.append(((Item)E).riding().Name());
						else
						if(((Item)E).container()!=null)
							results.append(((Item)E).container().Name());
						else
						if(E instanceof Container)
						{
							final List<Item> V=((Container)E).getDeepContents();
							for(int v=0;v<V.size();v++)
								results.append("\""+V.get(v).Name()+"\" ");
						}
					}
				}
				break;
			}
			case 27: // var
			{
				final String arg1=CMParms.getCleanBit(funcParms,0);
				final String arg2=CMParms.getPastBitClean(funcParms,0).toUpperCase();
				final Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				final String val=getVar(E,arg1,arg2,source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp);
				results.append(val);
				break;
			}
			case 41: // eval
				results.append("[unimplemented function]");
				break;
			case 40: // number
			{
				final String val=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.cleanBit(funcParms)).trim();
				boolean isnumber=(val.length()>0);
				for(int i=0;i<val.length();i++)
				{
					if(!Character.isDigit(val.charAt(i)))
					{
						isnumber = false;
						break;
					}
				}
				if(isnumber)
					results.append(CMath.s_long(val.trim()));
				break;
			}
			case 42: // randnum
			{
				final String arg1String=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.cleanBit(funcParms)).toUpperCase();
				int arg1=0;
				if(CMath.isMathExpression(arg1String))
					arg1=CMath.s_parseIntExpression(arg1String.trim());
				else
					arg1=CMParms.parse(arg1String.trim()).size();
				results.append(CMLib.dice().roll(1,arg1,0));
				break;
			}
			case 71: // rand0num
			{
				final String arg1String=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,CMParms.cleanBit(funcParms)).toUpperCase();
				int arg1=0;
				if(CMath.isMathExpression(arg1String))
					arg1=CMath.s_parseIntExpression(arg1String.trim());
				else
					arg1=CMParms.parse(arg1String.trim()).size();
				results.append(CMLib.dice().roll(1,arg1,-1));
				break;
			}
			case 96: // iscontents
			{
				final String arg1=CMParms.cleanBit(funcParms);
				final Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if(E instanceof Rideable)
				{
					for(final Enumeration<Rider> r=((Rideable)E).riders();r.hasMoreElements();)
						results.append(CMParms.quoteIfNecessary(r.nextElement().name())).append(" ");
				}
				if(E instanceof Container)
				{
					for (final Item item : ((Container)E).getDeepContents())
						results.append(CMParms.quoteIfNecessary(item.name())).append(" ");
				}
				break;
			}
			case 97: // wornon
			{
				final String arg1=CMParms.getCleanBit(funcParms,0);
				final String arg2=CMParms.getPastBitClean(funcParms,0).toUpperCase();
				final PhysicalAgent E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				int wornLoc=-1;
				if(arg2.length()>0)
					wornLoc = CMParms.indexOf(Wearable.CODES.NAMESUP(), arg2.toUpperCase().trim());
				if(wornLoc<0)
					logError(scripted,"EVAL","BAD WORNON LOCATION",arg2);
				else
				if(E instanceof MOB)
				{
					final List<Item> items=((MOB)E).fetchWornItems(Wearable.CODES.GET(wornLoc),(short)-2048,(short)0);
					for(final Item item : items)
						results.append(CMParms.quoteIfNecessary(item.name())).append(" ");
				}
				break;
			}
			default:
				logError(scripted,"Unknown Val",preFab,evaluable);
				return results.toString();
			}
		}
		return results.toString();
	}

	protected MOB getRandPC(MOB monster, Object[] tmp, Room room)
	{
		if((tmp[SPECIAL_RANDPC]==null)||(tmp[SPECIAL_RANDPC]==monster))
		{
			MOB M=null;
			if(room!=null)
			{
				final Vector<MOB> choices = new Vector<MOB>();
				for(int p=0;p<room.numInhabitants();p++)
				{
					M=room.fetchInhabitant(p);
					if((!M.isMonster())&&(M!=monster))
					{
						final HashSet<MOB> seen=new HashSet<MOB>();
						while((M.amFollowing()!=null)&&(!M.amFollowing().isMonster())&&(!seen.contains(M)))
						{
							seen.add(M);
							M=M.amFollowing();
						}
						choices.addElement(M);
					}
				}
				if(choices.size() > 0)
					tmp[SPECIAL_RANDPC] = choices.elementAt(CMLib.dice().roll(1,choices.size(),-1));
			}
		}
		return (MOB)tmp[SPECIAL_RANDPC];
	}

	protected MOB getRandAnyone(MOB monster, Object[] tmp, Room room)
	{
		if((tmp[SPECIAL_RANDANYONE]==null)||(tmp[SPECIAL_RANDANYONE]==monster))
		{
			MOB M=null;
			if(room!=null)
			{
				final Vector<MOB> choices = new Vector<MOB>();
				for(int p=0;p<room.numInhabitants();p++)
				{
					M=room.fetchInhabitant(p);
					if(M!=monster)
					{
						final HashSet<MOB> seen=new HashSet<MOB>();
						while((M.amFollowing()!=null)&&(!M.amFollowing().isMonster())&&(!seen.contains(M)))
						{
							seen.add(M);
							M=M.amFollowing();
						}
						choices.addElement(M);
					}
				}
				if(choices.size() > 0)
					tmp[SPECIAL_RANDANYONE] = choices.elementAt(CMLib.dice().roll(1,choices.size(),-1));
			}
		}
		return (MOB)tmp[SPECIAL_RANDANYONE];
	}

	@Override
	public String execute(final PhysicalAgent scripted,
						  final MOB source,
						  final Environmental target,
						  final MOB monster,
						  final Item primaryItem,
						  final Item secondaryItem,
						  final DVector script,
						  final String msg,
						  final Object[] tmp)
	{
		return execute(scripted,source,target,monster,primaryItem,secondaryItem,script,msg,tmp,1);
	}

	public String execute(PhysicalAgent scripted,
						  MOB source,
						  Environmental target,
						  MOB monster,
						  Item primaryItem,
						  Item secondaryItem,
						  final DVector script,
						  String msg,
						  final Object[] tmp,
						  final int startLine)
	{
		tickStatus=Tickable.STATUS_START;
		String s=null;
		String[] tt=null;
		String cmd=null;
		for(int si=startLine;si<script.size();si++)
		{
			s=((String)script.elementAt(si,1)).trim();
			tt=(String[])script.elementAt(si,2);
			if(tt!=null)
				cmd=tt[0];
			else
				cmd=CMParms.getCleanBit(s,0).toUpperCase();
			if(cmd.length()==0)
				continue;

			Integer methCode=methH.get(cmd);
			if((methCode==null)&&(cmd.startsWith("MP")))
			{
				for(int i=0;i<methods.length;i++)
				{
					if(methods[i].startsWith(cmd))
						methCode=Integer.valueOf(i);
				}
			}
			if(methCode==null)
				methCode=Integer.valueOf(0);
			tickStatus=Tickable.STATUS_MISC3+methCode.intValue();
			switch(methCode.intValue())
			{
			case 57: // <SCRIPT>
			{
				if(tt==null)
					tt=parseBits(script,si,"C");
				final StringBuffer jscript=new StringBuffer("");
				while((++si)<script.size())
				{
					s=((String)script.elementAt(si,1)).trim();
					tt=(String[])script.elementAt(si,2);
					if(tt!=null)
						cmd=tt[0];
					else
						cmd=CMParms.getCleanBit(s,0).toUpperCase();
					if(cmd.equals("</SCRIPT>"))
					{
						if(tt==null)
							tt=parseBits(script,si,"C");
						break;
					}
					jscript.append(s+"\n");
				}
				if(CMSecurity.isApprovedJScript(jscript))
				{
					final Context cx = Context.enter();
					try
					{
						final JScriptEvent scope = new JScriptEvent(this,scripted,source,target,monster,primaryItem,secondaryItem,msg,tmp);
						cx.initStandardObjects(scope);
						final String[] names = { "host", "source", "target", "monster", "item", "item2", "message" ,"getVar", "setVar", "toJavaString"};
						scope.defineFunctionProperties(names, JScriptEvent.class,
													   ScriptableObject.DONTENUM);
						cx.evaluateString(scope, jscript.toString(),"<cmd>", 1, null);
					}
					catch(final Exception e)
					{
						Log.errOut("Scripting",scripted.name()+"/"+CMLib.map().getDescriptiveExtendedRoomID(lastKnownLocation)+"/JSCRIPT Error: "+e.getMessage());
					}
					Context.exit();
				}
				else
				if(CMProps.getIntVar(CMProps.Int.JSCRIPTS)==CMSecurity.JSCRIPT_REQ_APPROVAL)
				{
					if(lastKnownLocation!=null)
						lastKnownLocation.showHappens(CMMsg.MSG_OK_ACTION,L("A Javascript was not authorized.  Contact an Admin to use MODIFY JSCRIPT to authorize this script."));
				}
				break;
			}
			case 19: // if
			{
				if(tt==null)
				{
					try
					{
						final String[] ttParms=parseEval(s.substring(2));
						tt=new String[ttParms.length+1];
						tt[0]="IF";
						for(int i=0;i<ttParms.length;i++)
							tt[i+1]=ttParms[i];
						script.setElementAt(si,2,tt);
					}
					catch(final Exception e)
					{
						logError(scripted,"IF","Syntax",e.getMessage());
						tickStatus=Tickable.STATUS_END;
						return null;
					}
				}
				final String[][] EVAL={tt};
				boolean condition=eval(scripted,source,target,monster,primaryItem,secondaryItem,msg,tmp,EVAL,1);
				if(EVAL[0]!=tt)
				{
					tt=EVAL[0];
					script.setElementAt(si,2,tt);
				}
				final DVector V=new DVector(2);
				V.addElement("",null);
				int depth=0;
				boolean foundendif=false;
				boolean ignoreUntilEndScript=false;
				si++;
				while(si<script.size())
				{
					s=((String)script.elementAt(si,1)).trim();
					tt=(String[])script.elementAt(si,2);
					if(tt!=null)
						cmd=tt[0];
					else
						cmd=CMParms.getCleanBit(s,0).toUpperCase();
					if(cmd.equals("<SCRIPT>"))
					{
						if(tt==null)
							tt=parseBits(script,si,"C");
						ignoreUntilEndScript=true;
					}
					else
					if(cmd.equals("</SCRIPT>"))
					{
						if(tt==null)
							tt=parseBits(script,si,"C");
						ignoreUntilEndScript=false;
					}
					else
					if(ignoreUntilEndScript)
					{
					}
					else
					if(cmd.equals("ENDIF")&&(depth==0))
					{
						if(tt==null)
							tt=parseBits(script,si,"C");
						foundendif=true;
						break;
					}
					else
					if(cmd.equals("ELSE")&&(depth==0))
					{
						condition=!condition;
						if(s.substring(4).trim().length()>0)
						{
							script.setElementAt(si,1,"ELSE");
							script.setElementAt(si,2,new String[]{"ELSE"});
							script.insertElementAt(si+1,s.substring(4).trim(),null);
						}
						else
						if(tt==null)
							tt=parseBits(script,si,"C");
					}
					else
					{
						if(cmd.equals("IF"))
							depth++;
						else
						if(cmd.equals("ENDIF"))
						{
							if(tt==null)
								tt=parseBits(script,si,"C");
							depth--;
						}
						if(condition)
							V.addSharedElements(script.elementsAt(si));
					}
					si++;
				}
				if(!foundendif)
				{
					logError(scripted,"IF","Syntax"," Without ENDIF!");
					tickStatus=Tickable.STATUS_END;
					return null;
				}
				if(V.size()>1)
				{
					//source.tell(L("Starting @x1",conditionStr));
					//for(int v=0;v<V.size();v++)
					//  source.tell(L("Statement @x1",((String)V.elementAt(v))));
					final String response=execute(scripted,source,target,monster,primaryItem,secondaryItem,V,msg,tmp);
					if(response!=null)
					{
						tickStatus=Tickable.STATUS_END;
						return response;
					}
					//source.tell(L("Stopping @x1",conditionStr));
				}
				break;
			}
			case 70: // switch
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"Cr");
					if(tt==null)
						return null;
				}
				final String var=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[1]).trim();
				final DVector V=new DVector(2);
				V.addElement("",null);
				int depth=0;
				boolean foundendif=false;
				boolean ignoreUntilEndScript=false;
				boolean inCase=false;
				boolean matchedCase=false;
				si++;
				String s2=null;
				while(si<script.size())
				{
					s=((String)script.elementAt(si,1)).trim();
					tt=(String[])script.elementAt(si,2);
					if(tt!=null)
						cmd=tt[0];
					else
						cmd=CMParms.getCleanBit(s,0).toUpperCase();
					if(cmd.equals("<SCRIPT>"))
					{
						if(tt==null)
							tt=parseBits(script,si,"C");
						ignoreUntilEndScript=true;
					}
					else
					if(cmd.equals("</SCRIPT>"))
					{
						if(tt==null)
							tt=parseBits(script,si,"C");
						ignoreUntilEndScript=false;
					}
					else
					if(ignoreUntilEndScript)
					{
					}
					else
					if(cmd.equals("ENDSWITCH")&&(depth==0))
					{
						if(tt==null)
							tt=parseBits(script,si,"C");
						foundendif=true;
						break;
					}
					else
					if(cmd.equals("CASE")&&(depth==0))
					{
						if(tt==null)
						{
							tt=parseBits(script,si,"Ccr");
							if(tt==null)
								return null;
						}
						s2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[1]).trim();
						inCase=var.equalsIgnoreCase(s2);
						matchedCase=matchedCase||inCase;
					}
					else
					if(cmd.equals("DEFAULT")&&(depth==0))
					{
						inCase=!matchedCase;
					}
					else
					{
						if(inCase)
							V.addElement(s,tt);
						if(cmd.equals("SWITCH"))
						{
							if(tt==null)
								tt=parseBits(script,si,"Cr");
							depth++;
						}
						else
						if(cmd.equals("ENDSWITCH"))
						{
							if(tt==null)
								tt=parseBits(script,si,"C");
							depth--;
						}
					}
					si++;
				}
				if(!foundendif)
				{
					logError(scripted,"SWITCH","Syntax"," Without ENDSWITCH!");
					tickStatus=Tickable.STATUS_END;
					return null;
				}
				if(V.size()>1)
				{
					final String response=execute(scripted,source,target,monster,primaryItem,secondaryItem,V,msg,tmp);
					if(response!=null)
					{
						tickStatus=Tickable.STATUS_END;
						return response;
					}
				}
				break;
			}
			case 62: // for x = 1 to 100
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"CcccCr");
					if(tt==null)
						return null;
				}
				if(tt[5].length()==0)
				{
					logError(scripted,"FOR","Syntax","5 parms required!");
					tickStatus=Tickable.STATUS_END;
					return null;
				}
				final String varStr=tt[1];
				if((varStr.length()!=2)||(varStr.charAt(0)!='$')||(!Character.isDigit(varStr.charAt(1))))
				{
					logError(scripted,"FOR","Syntax","'"+varStr+"' is not a tmp var $1, $2..");
					tickStatus=Tickable.STATUS_END;
					return null;
				}
				final int whichVar=CMath.s_int(Character.toString(varStr.charAt(1)));
				if((tmp[whichVar] instanceof String)
				&&(((String)tmp[whichVar]).length()>0)
				&&(CMath.isInteger(((String)tmp[whichVar]).trim())))
				{
					logError(scripted,"FOR","Syntax","'"+whichVar+"' is already in use! Use a different one!");
					tickStatus=Tickable.STATUS_END;
					return null;
				}
				if(!tt[2].equals("="))
				{
					logError(scripted,"FOR","Syntax","'"+s+"' is illegal for syntax!");
					tickStatus=Tickable.STATUS_END;
					return null;
				}

				int toAdd=0;
				if(tt[4].equals("TO<"))
					toAdd=-1;
				else
				if(tt[4].equals("TO>"))
					toAdd=1;
				else
				if(!tt[4].equals("TO"))
				{
					logError(scripted,"FOR","Syntax","'"+s+"' is illegal for syntax!");
					tickStatus=Tickable.STATUS_END;
					return null;
				}
				final String from=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[3]).trim();
				final String to=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[5]).trim();
				if((!CMath.isInteger(from))||(!CMath.isInteger(to)))
				{
					logError(scripted,"FOR","Syntax","'"+from+"-"+to+"' is illegal range!");
					tickStatus=Tickable.STATUS_END;
					return null;
				}
				final DVector V=new DVector(2);
				V.addElement("",null);
				int depth=0;
				boolean foundnext=false;
				boolean ignoreUntilEndScript=false;
				si++;
				while(si<script.size())
				{
					s=((String)script.elementAt(si,1)).trim();
					tt=(String[])script.elementAt(si,2);
					if(tt!=null)
						cmd=tt[0];
					else
						cmd=CMParms.getCleanBit(s,0).toUpperCase();
					if(cmd.equals("<SCRIPT>"))
					{
						if(tt==null)
							tt=parseBits(script,si,"C");
						ignoreUntilEndScript=true;
					}
					else
					if(cmd.equals("</SCRIPT>"))
					{
						if(tt==null)
							tt=parseBits(script,si,"C");
						ignoreUntilEndScript=false;
					}
					else
					if(ignoreUntilEndScript)
					{
					}
					else
					if(cmd.equals("NEXT")&&(depth==0))
					{
						if(tt==null)
							tt=parseBits(script,si,"C");
						foundnext=true;
						break;
					}
					else
					{
						if(cmd.equals("FOR"))
						{
							if(tt==null)
								tt=parseBits(script,si,"CcccCr");
							depth++;
						}
						else
						if(cmd.equals("NEXT"))
						{
							if(tt==null)
								tt=parseBits(script,si,"C");
							depth--;
						}
						V.addSharedElements(script.elementsAt(si));
					}
					si++;
				}
				if(!foundnext)
				{
					logError(scripted,"FOR","Syntax"," Without NEXT!");
					tickStatus=Tickable.STATUS_END;
					return null;
				}
				if(V.size()>1)
				{
					//source.tell(L("Starting @x1",conditionStr));
					//for(int v=0;v<V.size();v++)
					//  source.tell(L("Statement @x1",((String)V.elementAt(v))));
					final int fromInt=CMath.s_int(from);
					int toInt=CMath.s_int(to);
					final int increment=(toInt>=fromInt)?1:-1;
					String response=null;
					if(((increment>0)&&(fromInt<=(toInt+toAdd)))
					||((increment<0)&&(fromInt>=(toInt+toAdd))))
					{
						toInt+=toAdd;
						final long tm=System.currentTimeMillis()+(10 * 1000);
						for(int forLoop=fromInt;forLoop!=toInt;forLoop+=increment)
						{
							tmp[whichVar]=""+forLoop;
							response=execute(scripted,source,target,monster,primaryItem,secondaryItem,V,msg,tmp);
							if(response!=null)
								break;
							if(System.currentTimeMillis()>tm)
							{
								logError(scripted,"FOR","Runtime","For loop violates 10 second rule: " +s);
								break;
							}
						}
						tmp[whichVar]=""+toInt;
						if(response == null)
							response=execute(scripted,source,target,monster,primaryItem,secondaryItem,V,msg,tmp);
						else
						if(response.equalsIgnoreCase("break"))
							response=null;
					}
					if(response!=null)
					{
						tickStatus=Tickable.STATUS_END;
						return response;
					}
					tmp[whichVar]=null;
					//source.tell(L("Stopping @x1",conditionStr));
				}
				break;
			}
			case 50: // break;
				if(tt==null)
					tt=parseBits(script,si,"C");
				tickStatus=Tickable.STATUS_END;
				return "BREAK";
			case 1: // mpasound
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"Cp");
					if(tt==null)
						return null;
				}
				final String echo=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[1]);
				//lastKnownLocation.showSource(monster,null,CMMsg.MSG_OK_ACTION,echo);
				for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
				{
					final Room R2=lastKnownLocation.getRoomInDir(d);
					final Exit E2=lastKnownLocation.getExitInDir(d);
					if((R2!=null)&&(E2!=null)&&(E2.isOpen()))
						R2.showOthers(monster,null,null,CMMsg.MSG_OK_ACTION,echo);
				}
				break;
			}
			case 4: // mpjunk
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"CR");
					if(tt==null)
						return null;
				}
				if(tt[1].equals("ALL") && (monster!=null))
				{
					monster.delAllItems(true);
				}
				else
				{
					final Environmental E=getArgumentItem(tt[1],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
					Item I=null;
					if(E instanceof Item)
						I=(Item)E;
					if((I==null)&&(monster!=null))
						I=monster.findItem(varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[1]));
					if((I==null)&&(scripted instanceof Room))
						I=((Room)scripted).findItem(varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[1]));
					if(I!=null)
						I.destroy();
				}
				break;
			}
			case 2: // mpecho
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"Cp");
					if(tt==null)
						return null;
				}
				if(lastKnownLocation!=null)
					lastKnownLocation.show(monster,null,CMMsg.MSG_OK_ACTION,varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[1]));
				break;
			}
			case 13: // mpunaffect
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"Ccr");
					if(tt==null)
						return null;
				}
				final Physical newTarget=getArgumentItem(tt[1],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				String which=tt[2];
				if(newTarget!=null)
				if(which.equalsIgnoreCase("all")||(which.length()==0))
				{
					for(int a=newTarget.numEffects()-1;a>=0;a--)
					{
						final Ability A=newTarget.fetchEffect(a);
						if(A!=null)
							A.unInvoke();
					}
				}
				else
				{
					final Ability A2=CMClass.findAbility(which);
					if(A2!=null)
						which=A2.ID();
					final Ability A=newTarget.fetchEffect(which);
					if(A!=null)
					{
						if((newTarget instanceof MOB)&&(!((MOB)newTarget).isMonster()))
							Log.sysOut("Scripting",newTarget.Name()+" was MPUNAFFECTED by "+A.Name());
						A.unInvoke();
						if(newTarget.fetchEffect(which)==A)
							newTarget.delEffect(A);
					}
				}
				break;
			}
			case 3: // mpslay
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"Cr");
					if(tt==null)
						return null;
				}
				final Environmental newTarget=getArgumentItem(tt[1],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((newTarget!=null)&&(newTarget instanceof MOB))
					CMLib.combat().postDeath(monster,(MOB)newTarget,null);
				break;
			}
			case 73: // mpsetinternal
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"CCr");
					if(tt==null)
						return null;
				}
				final String arg2=tt[1];
				final String arg3=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]);
				if(arg2.equals("SCOPE"))
					setVarScope(arg3);
				else
				if(arg2.equals("NODELAY"))
					noDelay=CMath.s_bool(arg3);
				else
				if(arg2.equals("ACTIVETRIGGER")||arg2.equals("ACTIVETRIGGERS"))
					alwaysTriggers=CMath.s_bool(arg3);
				else
				if(arg2.equals("DEFAULTQUEST"))
					registerDefaultQuest(arg3);
				else
				if(arg2.equals("SAVABLE"))
					setSavable(CMath.s_bool(arg3));
				else
				if(arg2.equals("PASSIVE"))
					this.runInPassiveAreas = CMath.s_bool(arg3); 
				else
					logError(scripted,"MPSETINTERNAL","Syntax","Unknown stat: "+arg2);
				break;
			}
			case 74: // mpprompt
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"CCCr");
					if(tt==null)
						return null;
				}
				final Environmental newTarget=getArgumentItem(tt[1],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				final String var=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]);
				final String promptStr=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[3]);
				if((newTarget!=null)&&(newTarget instanceof MOB)&&((((MOB)newTarget).session()!=null)))
				{
					try
					{
						final String value=((MOB)newTarget).session().prompt(promptStr,120000);
						setVar(newTarget.Name(),var,value);
					}
					catch(final Exception e) 
					{
						return "";
					}
				}
				break;
			}
			case 75: // mpconfirm
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"CCCCr");
					if(tt==null)
						return null;
				}
				final Environmental newTarget=getArgumentItem(tt[1],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				final String var=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]);
				final String defaultVal=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[3]);
				final String promptStr=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[4]);
				if((newTarget!=null)&&(newTarget instanceof MOB)&&((((MOB)newTarget).session()!=null)))
				{
					try
					{
						final String value=((MOB)newTarget).session().confirm(promptStr,defaultVal,60000)?"Y":"N";
						setVar(newTarget.Name(),var,value);
					}
					catch(final Exception e)
					{
						return "";
					}
					/*
					 * this is how to do non-blocking, which doesn't help stuff waiting
					 * for a response from the original execute method
					final Session session = ((MOB)newTarget).session();
					if(session != null)
					{
						try
						{
							final int lastLineNum=si;
							final JScriptEvent continueEvent=new JScriptEvent(
																		this,
																		scripted,
																		source,
																		target,
																		monster,
																		primaryItem,
																		secondaryItem,
																		msg,
																		tmp);
							((MOB)newTarget).session().prompt(new InputCallback(InputCallback.Type.PROMPT,"",0){
								private final JScriptEvent event=continueEvent;
								private final int lineNum=lastLineNum;
								private final String scope=newTarget.Name();
								private final String varName=var;
								private final String promptStrMsg=promptStr;
								private final DVector lastScript=script;

								@Override
								public void showPrompt()
								{
									session.promptPrint(promptStrMsg);
								}
	
								@Override
								public void timedOut()
								{
									event.executeEvent(lastScript, lineNum+1);
								}
								
								@Override
								public void callBack()
								{
									final String value=this.input;
									if((value.trim().length()==0)||(value.indexOf('<')>=0))
										return;
									setVar(scope,varName,value);
									event.executeEvent(lastScript, lineNum+1);
								}
							});
						}
						catch(final Exception e) 
						{
							return "";
						}
					}
					 */
				}
				break;
			}
			case 76: // mpchoose
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"CCCCCr");
					if(tt==null)
						return null;
				}
				final Environmental newTarget=getArgumentItem(tt[1],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				final String var=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]);
				final String choices=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[3]);
				final String defaultVal=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[4]);
				final String promptStr=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[5]);
				if((newTarget!=null)&&(newTarget instanceof MOB)&&((((MOB)newTarget).session()!=null)))
				{
					try
					{
						final String value=((MOB)newTarget).session().choose(promptStr,choices,defaultVal,60000);
						setVar(newTarget.Name(),var,value);
					}
					catch(final Exception e)
					{
						return "";
					}
				}
				break;
			}
			case 16: // mpset
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"CCcr");
					if(tt==null)
						return null;
				}
				final Physical newTarget=getArgumentItem(tt[1],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				final String arg2=tt[2];
				String arg3=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[3]);
				if(newTarget!=null)
				{
					if((newTarget instanceof MOB)&&(!((MOB)newTarget).isMonster()))
						Log.sysOut("Scripting",newTarget.Name()+" has "+arg2+" MPSETTED to "+arg3);
					boolean found=false;
					for(int i=0;i<newTarget.getStatCodes().length;i++)
					{
						if(newTarget.getStatCodes()[i].equalsIgnoreCase(arg2))
						{
							if(arg3.equals("++"))
								arg3=""+(CMath.s_int(newTarget.getStat(newTarget.getStatCodes()[i]))+1);
							if(arg3.equals("--"))
								arg3=""+(CMath.s_int(newTarget.getStat(newTarget.getStatCodes()[i]))-1);
							newTarget.setStat(arg2,arg3);
							found=true;
							break;
						}
					}
					if((!found)&&(newTarget instanceof MOB))
					{
						final MOB M=(MOB)newTarget;
						for(final int i : CharStats.CODES.ALLCODES())
						{
							if(CharStats.CODES.NAME(i).equalsIgnoreCase(arg2)||CharStats.CODES.DESC(i).equalsIgnoreCase(arg2))
							{
								if(arg3.equals("++"))
									arg3=""+(M.baseCharStats().getStat(i)+1);
								if(arg3.equals("--"))
									arg3=""+(M.baseCharStats().getStat(i)-1);
								M.baseCharStats().setStat(i,CMath.s_int(arg3.trim()));
								M.recoverCharStats();
								if(arg2.equalsIgnoreCase("RACE"))
									M.charStats().getMyRace().startRacing(M,false);
								found=true;
								break;
							}
						}
						if(!found)
						{
							for(int i=0;i<M.curState().getStatCodes().length;i++)
							{
								if(M.curState().getStatCodes()[i].equalsIgnoreCase(arg2))
								{
									if(arg3.equals("++"))
										arg3=""+(CMath.s_int(M.curState().getStat(M.curState().getStatCodes()[i]))+1);
									if(arg3.equals("--"))
										arg3=""+(CMath.s_int(M.curState().getStat(M.curState().getStatCodes()[i]))-1);
									M.curState().setStat(arg2,arg3);
									found=true;
									break;
								}
							}
						}
						if(!found)
						{
							for(int i=0;i<M.basePhyStats().getStatCodes().length;i++)
							{
								if(M.basePhyStats().getStatCodes()[i].equalsIgnoreCase(arg2))
								{
									if(arg3.equals("++"))
										arg3=""+(CMath.s_int(M.basePhyStats().getStat(M.basePhyStats().getStatCodes()[i]))+1);
									if(arg3.equals("--"))
										arg3=""+(CMath.s_int(M.basePhyStats().getStat(M.basePhyStats().getStatCodes()[i]))-1);
									M.basePhyStats().setStat(arg2,arg3);
									found=true;
									break;
								}
							}
						}
						if((!found)&&(M.playerStats()!=null))
						{
							for(int i=0;i<M.playerStats().getStatCodes().length;i++)
							{
								if(M.playerStats().getStatCodes()[i].equalsIgnoreCase(arg2))
								{
									if(arg3.equals("++"))
										arg3=""+(CMath.s_int(M.playerStats().getStat(M.playerStats().getStatCodes()[i]))+1);
									if(arg3.equals("--"))
										arg3=""+(CMath.s_int(M.playerStats().getStat(M.playerStats().getStatCodes()[i]))-1);
									M.playerStats().setStat(arg2,arg3);
									found=true;
									break;
								}
							}
						}
						if((!found)&&(arg2.toUpperCase().startsWith("BASE")))
						{
							for(int i=0;i<M.baseState().getStatCodes().length;i++)
							{
								if(M.baseState().getStatCodes()[i].equalsIgnoreCase(arg2.substring(4)))
								{
									if(arg3.equals("++"))
										arg3=""+(CMath.s_int(M.baseState().getStat(M.baseState().getStatCodes()[i]))+1);
									if(arg3.equals("--"))
										arg3=""+(CMath.s_int(M.baseState().getStat(M.baseState().getStatCodes()[i]))-1);
									M.baseState().setStat(arg2.substring(4),arg3);
									found=true;
									break;
								}
							}
						}
						if((!found)&&(gstatH.containsKey(arg2.toUpperCase())))
						{
							found=true;
							switch(gstatH.get(arg2.toUpperCase()).intValue())
							{
							case GSTATADD_STINK:
								if(M.playerStats()!=null)
									M.playerStats().setHygiene(Math.round(CMath.s_pct(arg3)*PlayerStats.HYGIENE_DELIMIT));
								break;
							case GSTATADD_DEITY:
								M.setWorshipCharID(arg3);
								break;
							case GSTATADD_CLAN:
							{
								Pair<Clan, Integer> p = M.getClanRole(arg3);
								if (p == null)
								{
									final Clan C = CMLib.clans().getClan(arg3);
									if (C != null)
										p = new Pair<Clan, Integer>(C, Integer.valueOf(C.getGovernment().getAcceptPos()));
								}
								if (p != null)
									M.setClan(p.first.clanID(), p.second.intValue());
								break;
							}
							case GSTATADD_CLANROLE:
							{
								Clan C = CMLib.clans().findRivalrousClan((MOB) newTarget);
								if (C == null)
									C = ((MOB) newTarget).clans().iterator().hasNext() ? ((MOB) newTarget).clans().iterator().next().first : null;
								if (C != null)
									M.setClan(C.clanID(), CMath.s_int(arg3));
								break;
							}
							}
						}
					}

					if(!found)
					{
						logError(scripted,"MPSET","Syntax","Unknown stat: "+arg2+" for "+newTarget.Name());
						break;
					}
					if(newTarget instanceof MOB)
						((MOB)newTarget).recoverCharStats();
					newTarget.recoverPhyStats();
					if(newTarget instanceof MOB)
					{
						((MOB)newTarget).recoverMaxState();
						if(arg2.equalsIgnoreCase("LEVEL"))
						{
							CMLib.leveler().fillOutMOB(((MOB)newTarget),((MOB)newTarget).basePhyStats().level());
							((MOB)newTarget).recoverMaxState();
							((MOB)newTarget).recoverCharStats();
							((MOB)newTarget).recoverPhyStats();
							((MOB)newTarget).resetToMaxState();
						}
					}
				}
				break;
			}
			case 63: // mpargset
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"Ccr");
					if(tt==null)
						return null;
				}
				final String arg1=tt[1];
				final String arg2=tt[2];
				if((arg1.length()!=2)||(!arg1.startsWith("$")))
				{
					logError(scripted,"MPARGSET","Syntax","Mangled argument var: "+arg1+" for "+scripted.Name());
					break;
				}
				Object O=getArgumentMOB(arg2,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if(O==null)
					O=getArgumentItem(arg2,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((O==null)
				&&((!arg2.trim().startsWith("$"))
					||(arg2.length()<2)
					||((!Character.isDigit(arg2.charAt(1)))
						&&(!Character.isLetter(arg2.charAt(1))))))
					O=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,arg2);
				final char c=arg1.charAt(1);
				if(Character.isDigit(c))
				{
					if((O instanceof String)&&(((String)O).equalsIgnoreCase("null")))
						O=null;
					tmp[CMath.s_int(Character.toString(c))]=O;
				}
				else
				{
					switch(arg1.charAt(1))
					{
					case 'N':
					case 'n':
						if (O instanceof MOB)
							source = (MOB) O;
						break;
					case 'B':
					case 'b':
						if (O instanceof Environmental)
							lastLoaded = (Environmental) O;
						break;
					case 'I':
					case 'i':
						if (O instanceof PhysicalAgent)
							scripted = (PhysicalAgent) O;
						if (O instanceof MOB)
							monster = (MOB) O;
						break;
					case 'T':
					case 't':
						if (O instanceof Environmental)
							target = (Environmental) O;
						break;
					case 'O':
					case 'o':
						if (O instanceof Item)
							primaryItem = (Item) O;
						break;
					case 'P':
					case 'p':
						if (O instanceof Item)
							secondaryItem = (Item) O;
						break;
					case 'd':
					case 'D':
						if (O instanceof Room)
							lastKnownLocation = (Room) O;
						break;
					case 'g':
					case 'G':
						if (O instanceof String)
							msg = (String) O;
						break;
					default:
						logError(scripted, "MPARGSET", "Syntax", "Invalid argument var: " + arg1 + " for " + scripted.Name());
						break;
					}
				}
				break;
			}
			case 35: // mpgset
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"Cccr");
					if(tt==null)
						return null;
				}
				final Physical newTarget=getArgumentItem(tt[1],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				final String arg2=tt[2];
				String arg3=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[3]);
				if(newTarget!=null)
				{
					if((newTarget instanceof MOB)&&(!((MOB)newTarget).isMonster()))
						Log.sysOut("Scripting",newTarget.Name()+" has "+arg2+" MPGSETTED to "+arg3);
					boolean found=false;
					for(int i=0;i<newTarget.getStatCodes().length;i++)
					{
						if(newTarget.getStatCodes()[i].equalsIgnoreCase(arg2))
						{
							if(arg3.equals("++"))
								arg3=""+(CMath.s_int(newTarget.getStat(newTarget.getStatCodes()[i]))+1);
							if(arg3.equals("--"))
								arg3=""+(CMath.s_int(newTarget.getStat(newTarget.getStatCodes()[i]))-1);
							newTarget.setStat(newTarget.getStatCodes()[i],arg3);
							found=true; 
							break;
						}
					}
					if(!found)
					{
						if(newTarget instanceof MOB)
						{
							final GenericBuilder.GenMOBCode element = (GenericBuilder.GenMOBCode)CMath.s_valueOf(GenericBuilder.GenMOBCode.class,arg2.toUpperCase().trim());
							if(element != null)
							{
								if(arg3.equals("++"))
									arg3=""+(CMath.s_int(CMLib.coffeeMaker().getGenMobStat((MOB)newTarget,element.name()))+1);
								if(arg3.equals("--"))
									arg3=""+(CMath.s_int(CMLib.coffeeMaker().getGenMobStat((MOB)newTarget,element.name()))-1);
								CMLib.coffeeMaker().setGenMobStat((MOB)newTarget,element.name(),arg3);
								found=true;
							}
							if(!found)
							{
								final MOB M=(MOB)newTarget;
								for(final int i : CharStats.CODES.ALLCODES())
								{
									if(CharStats.CODES.NAME(i).equalsIgnoreCase(arg2)||CharStats.CODES.DESC(i).equalsIgnoreCase(arg2))
									{
										if(arg3.equals("++"))
											arg3=""+(M.baseCharStats().getStat(i)+1);
										if(arg3.equals("--"))
											arg3=""+(M.baseCharStats().getStat(i)-1);
										if((arg3.length()==1)&&(Character.isLetter(arg3.charAt(0))))
											M.baseCharStats().setStat(i,arg3.charAt(0));
										else
											M.baseCharStats().setStat(i,CMath.s_int(arg3.trim()));
										M.recoverCharStats();
										if(arg2.equalsIgnoreCase("RACE"))
											M.charStats().getMyRace().startRacing(M,false);
										found=true;
										break;
									}
								}
								if(!found)
								{
									for(int i=0;i<M.curState().getStatCodes().length;i++)
									{
										if(M.curState().getStatCodes()[i].equalsIgnoreCase(arg2))
										{
											if(arg3.equals("++"))
												arg3=""+(CMath.s_int(M.curState().getStat(M.curState().getStatCodes()[i]))+1);
											if(arg3.equals("--"))
												arg3=""+(CMath.s_int(M.curState().getStat(M.curState().getStatCodes()[i]))-1);
											M.curState().setStat(arg2,arg3);
											found=true;
											break;
										}
									}
								}
								if(!found)
								{
									for(int i=0;i<M.basePhyStats().getStatCodes().length;i++)
									{
										if(M.basePhyStats().getStatCodes()[i].equalsIgnoreCase(arg2))
										{
											if(arg3.equals("++"))
												arg3=""+(CMath.s_int(M.basePhyStats().getStat(M.basePhyStats().getStatCodes()[i]))+1);
											if(arg3.equals("--"))
												arg3=""+(CMath.s_int(M.basePhyStats().getStat(M.basePhyStats().getStatCodes()[i]))-1);
											M.basePhyStats().setStat(arg2,arg3);
											found=true;
											break;
										}
									}
								}
								if((!found)&&(M.playerStats()!=null))
								{
									for(int i=0;i<M.playerStats().getStatCodes().length;i++)
									{
										if(M.playerStats().getStatCodes()[i].equalsIgnoreCase(arg2))
										{
											if(arg3.equals("++"))
												arg3=""+(CMath.s_int(M.playerStats().getStat(M.playerStats().getStatCodes()[i]))+1);
											if(arg3.equals("--"))
												arg3=""+(CMath.s_int(M.playerStats().getStat(M.playerStats().getStatCodes()[i]))-1);
											M.playerStats().setStat(arg2,arg3);
											found=true;
											break;
										}
									}
								}
								if((!found)&&(arg2.toUpperCase().startsWith("BASE")))
								{
									for(int i=0;i<M.baseState().getStatCodes().length;i++)
									{
										if(M.baseState().getStatCodes()[i].equalsIgnoreCase(arg2.substring(4)))
										{
											if(arg3.equals("++"))
												arg3=""+(CMath.s_int(M.baseState().getStat(M.baseState().getStatCodes()[i]))+1);
											if(arg3.equals("--"))
												arg3=""+(CMath.s_int(M.baseState().getStat(M.baseState().getStatCodes()[i]))-1);
											M.baseState().setStat(arg2.substring(4),arg3);
											found=true;
											break;
										}
									}
								}
								if((!found)&&(gstatH.containsKey(arg2.toUpperCase())))
								{
									found=true;
									switch(gstatH.get(arg2.toUpperCase()).intValue())
									{
									case GSTATADD_STINK:
										if(M.playerStats()!=null)
											M.playerStats().setHygiene(Math.round(CMath.s_pct(arg3)*PlayerStats.HYGIENE_DELIMIT));
										break;
									case GSTATADD_DEITY:
										M.setWorshipCharID(arg3);
										break;
									case GSTATADD_CLAN:
									{
										Pair<Clan, Integer> p = M.getClanRole(arg3);
										if (p == null)
										{
											final Clan C = CMLib.clans().getClan(arg3);
											if (C != null)
												p = new Pair<Clan, Integer>(C, Integer.valueOf(C.getGovernment().getAcceptPos()));
										}
										if (p != null)
											M.setClan(p.first.clanID(), p.second.intValue());
										break;
									}
									case GSTATADD_CLANROLE:
									{
										Clan C = CMLib.clans().findRivalrousClan((MOB) newTarget);
										if (C == null)
											C = ((MOB) newTarget).clans().iterator().hasNext() ? ((MOB) newTarget).clans().iterator().next().first : null;
										if (C != null)
											M.setClan(C.clanID(), CMath.s_int(arg3));
										break;
									}
									}
								}
							}
						}
					}
					else
					if(newTarget instanceof Item)
					{
						
						final GenericBuilder.GenItemCode element = (GenericBuilder.GenItemCode)CMath.s_valueOf(GenericBuilder.GenItemCode.class,arg2.toUpperCase().trim());
						if(element != null)
						{
							if(arg3.equals("++"))
								arg3=""+(CMath.s_int(CMLib.coffeeMaker().getGenItemStat((Item)newTarget,element.name()))+1);
							if(arg3.equals("--"))
								arg3=""+(CMath.s_int(CMLib.coffeeMaker().getGenItemStat((Item)newTarget,element.name()))-1);
							CMLib.coffeeMaker().setGenItemStat((Item)newTarget,element.name(),arg3);
							found=true;
						}
					}

					if(!found)
					{
						logError(scripted,"MPGSET","Syntax","Unknown stat: "+arg2+" for "+newTarget.Name());
						break;
					}
					if(newTarget instanceof MOB)
						((MOB)newTarget).recoverCharStats();
					newTarget.recoverPhyStats();
					if(newTarget instanceof MOB)
					{
						((MOB)newTarget).recoverMaxState();
						if(arg2.equalsIgnoreCase("LEVEL"))
						{
							CMLib.leveler().fillOutMOB(((MOB)newTarget),((MOB)newTarget).basePhyStats().level());
							((MOB)newTarget).recoverMaxState();
							((MOB)newTarget).recoverCharStats();
							((MOB)newTarget).recoverPhyStats();
							((MOB)newTarget).resetToMaxState();
						}
					}
				}
				break;
			}
			case 11: // mpexp
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"Ccr");
					if(tt==null)
						return null;
				}
				final Physical newTarget=getArgumentItem(tt[1],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				final String amtStr=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]).trim();
				int t=CMath.s_int(amtStr);
				if((newTarget!=null)&&(newTarget instanceof MOB))
				{
					if((amtStr.endsWith("%"))
					&&(((MOB)newTarget).getExpNeededLevel()<Integer.MAX_VALUE))
					{
						final int baseLevel=newTarget.basePhyStats().level();
						final int lastLevelExpNeeded=(baseLevel<=1)?0:CMLib.leveler().getLevelExperience(baseLevel-1);
						final int thisLevelExpNeeded=CMLib.leveler().getLevelExperience(baseLevel);
						t=(int)Math.round(CMath.mul(thisLevelExpNeeded-lastLevelExpNeeded,
											CMath.div(CMath.s_int(amtStr.substring(0,amtStr.length()-1)),100.0)));
					}
					if(t!=0)
						CMLib.leveler().postExperience((MOB)newTarget,null,null,t,false);
				}
				break;
			}
			case 77: // mpmoney
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"Ccr");
					if(tt==null)
						return null;
				}
				Environmental newTarget=getArgumentMOB(tt[1],source,monster,scripted,primaryItem,secondaryItem,msg,tmp);
				if(newTarget==null)
					newTarget=getArgumentItem(tt[1],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				String amtStr=tt[2];
				amtStr=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,amtStr).trim();
				final boolean plus=!amtStr.startsWith("-");
				if(amtStr.startsWith("+")||amtStr.startsWith("-"))
					amtStr=amtStr.substring(1).trim();
				final String currency = CMLib.english().numPossibleGoldCurrency(source, amtStr);
				final long amt = CMLib.english().numPossibleGold(source, amtStr);
				final double denomination = CMLib.english().numPossibleGoldDenomination(source, currency, amtStr);
				Container container = null;
				if(newTarget instanceof Item)
				{
					container = (newTarget instanceof Container)?(Container)newTarget:null;
					newTarget = ((Item)newTarget).owner();
				}
				if(newTarget instanceof MOB)
				{
					if(plus)
						CMLib.beanCounter().giveSomeoneMoney((MOB)newTarget, currency, amt * denomination);
					else
						CMLib.beanCounter().subtractMoney((MOB)newTarget, currency, amt * denomination);
				}
				else
				{
					if(!(newTarget instanceof Room))
						newTarget=lastKnownLocation;
					if(plus)
						CMLib.beanCounter().dropMoney((Room)newTarget, container, currency, amt * denomination);
					else
						CMLib.beanCounter().removeMoney((Room)newTarget, container, currency, amt * denomination);
				}
				break;
			}
			case 59: // mpquestpoints
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"Ccr");
					if(tt==null)
						return null;
				}
				final Environmental newTarget=getArgumentItem(tt[1],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				final String val=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]).trim();
				if(newTarget instanceof MOB)
				{
					if(CMath.isNumber(val))
					{
						final int ival=CMath.s_int(val);
						final int aval=ival-((MOB)newTarget).getQuestPoint();
						((MOB)newTarget).setQuestPoint(CMath.s_int(val));
						if(aval>0)
							CMLib.players().bumpPrideStat((MOB)newTarget,PrideStat.QUESTPOINTS_EARNED, aval);
					}
					else
					if(val.startsWith("++")&&(CMath.isNumber(val.substring(2).trim())))
					{
						((MOB)newTarget).setQuestPoint(((MOB)newTarget).getQuestPoint()+CMath.s_int(val.substring(2).trim()));
						CMLib.players().bumpPrideStat((MOB)newTarget,PrideStat.QUESTPOINTS_EARNED, CMath.s_int(val.substring(2).trim()));
					}
					else
					if(val.startsWith("--")&&(CMath.isNumber(val.substring(2).trim())))
						((MOB)newTarget).setQuestPoint(((MOB)newTarget).getQuestPoint()-CMath.s_int(val.substring(2).trim()));
					else
						logError(scripted,"QUESTPOINTS","Syntax","Bad syntax "+val+" for "+scripted.Name());
				}
				break;
			}
			case 65: // MPQSET
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"Cccr");
					if(tt==null)
						return null;
				}
				final String qstr=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[1]);
				final String var=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]);
				final PhysicalAgent obj=getArgumentItem(tt[3],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				String val=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[3]);
				final Quest Q=getQuest(qstr);
				if(Q==null)
					logError(scripted,"MPQSET","Syntax","Unknown quest "+qstr+" for "+scripted.Name());
				else
				if(var.equalsIgnoreCase("QUESTOBJ"))
				{
					if(obj==null)
						logError(scripted,"MPQSET","Syntax","Unknown object "+tt[3]+" for "+scripted.Name());
					else
					{
						obj.basePhyStats().setDisposition(obj.basePhyStats().disposition()|PhyStats.IS_UNSAVABLE);
						obj.recoverPhyStats();
						Q.runtimeRegisterObject(obj);
					}
				}
				else
				if(var.equalsIgnoreCase("STATISTICS")&&(val.equalsIgnoreCase("ACCEPTED")))
					CMLib.coffeeTables().bump(Q,CoffeeTableRow.STAT_QUESTACCEPTED);
				else
				if(var.equalsIgnoreCase("STATISTICS")&&(val.equalsIgnoreCase("SUCCESS")||val.equalsIgnoreCase("WON")))
					CMLib.coffeeTables().bump(Q,CoffeeTableRow.STAT_QUESTSUCCESS);
				else
				if(var.equalsIgnoreCase("STATISTICS")&&(val.equalsIgnoreCase("FAILED")))
					CMLib.coffeeTables().bump(Q,CoffeeTableRow.STAT_QUESTFAILED);
				else
				{
					if(val.equals("++"))
						val=""+(CMath.s_int(Q.getStat(var))+1);
					if(val.equals("--"))
						val=""+(CMath.s_int(Q.getStat(var))-1);
					Q.setStat(var,val);
				}
				break;
			}
			case 66: // MPLOG
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"CCcr");
					if(tt==null)
						return null;
				}
				final String type=tt[1];
				final String head=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]);
				final String val=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[3]);
				if(type.startsWith("E"))
					Log.errOut("Script","["+head+"] "+val);
				else
				if(type.startsWith("I")||type.startsWith("S"))
					Log.infoOut("Script","["+head+"] "+val);
				else
				if(type.startsWith("D"))
					Log.debugOut("Script","["+head+"] "+val);
				else
					logError(scripted,"MPLOG","Syntax","Unknown log type "+type+" for "+scripted.Name());
				break;
			}
			case 67: // MPCHANNEL
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"Ccr");
					if(tt==null)
						return null;
				}
				String channel=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[1]);
				final boolean sysmsg=channel.startsWith("!");
				if(sysmsg)
					channel=channel.substring(1);
				final String val=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]);
				if(CMLib.channels().getChannelCodeNumber(channel)<0)
					logError(scripted,"MPCHANNEL","Syntax","Unknown channel "+channel+" for "+scripted.Name());
				else
					CMLib.commands().postChannel(monster,channel,val,sysmsg);
				break;
			}
			case 68: // MPUNLOADSCRIPT
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"Cc");
					if(tt==null)
						return null;
				}
				String scriptname=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[1]);
				if(!new CMFile(Resources.makeFileResourceName(scriptname),null,CMFile.FLAG_FORCEALLOW).exists())
					logError(scripted,"MPUNLOADSCRIPT","Runtime","File does not exist: "+Resources.makeFileResourceName(scriptname));
				else
				{
					final ArrayList<String> delThese=new ArrayList<String>();
					scriptname=scriptname.toUpperCase().trim();
					final String parmname=scriptname;
					for(final Iterator<String> k = Resources.findResourceKeys(parmname);k.hasNext();)
					{
						final String key=k.next();
						if(key.startsWith("PARSEDPRG: ")&&(key.toUpperCase().endsWith(parmname)))
						{
							delThese.add(key);
						}
					}
					for(int i=0;i<delThese.size();i++)
						Resources.removeResource(delThese.get(i));
				}

				break;
			}
			case 60: // MPTRAINS
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"Ccr");
					if(tt==null)
						return null;
				}
				final Environmental newTarget=getArgumentItem(tt[1],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				final String val=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]).trim();
				if(newTarget instanceof MOB)
				{
					if(CMath.isNumber(val))
						((MOB)newTarget).setTrains(CMath.s_int(val));
					else
					if(val.startsWith("++")&&(CMath.isNumber(val.substring(2).trim())))
						((MOB)newTarget).setTrains(((MOB)newTarget).getTrains()+CMath.s_int(val.substring(2).trim()));
					else
					if(val.startsWith("--")&&(CMath.isNumber(val.substring(2).trim())))
						((MOB)newTarget).setTrains(((MOB)newTarget).getTrains()-CMath.s_int(val.substring(2).trim()));
					else
						logError(scripted,"TRAINS","Syntax","Bad syntax "+val+" for "+scripted.Name());
				}
				break;
			}
			case 61: // mppracs
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"Ccr");
					if(tt==null)
						return null;
				}
				final Environmental newTarget=getArgumentItem(tt[1],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				final String val=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]).trim();
				if(newTarget instanceof MOB)
				{
					if(CMath.isNumber(val))
						((MOB)newTarget).setPractices(CMath.s_int(val));
					else
					if(val.startsWith("++")&&(CMath.isNumber(val.substring(2).trim())))
						((MOB)newTarget).setPractices(((MOB)newTarget).getPractices()+CMath.s_int(val.substring(2).trim()));
					else
					if(val.startsWith("--")&&(CMath.isNumber(val.substring(2).trim())))
						((MOB)newTarget).setPractices(((MOB)newTarget).getPractices()-CMath.s_int(val.substring(2).trim()));
					else
						logError(scripted,"PRACS","Syntax","Bad syntax "+val+" for "+scripted.Name());
				}
				break;
			}
			case 5: // mpmload
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"Cr");
					if(tt==null)
						return null;
				}
				final String name=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[1]);
				final ArrayList<Environmental> Ms=new ArrayList<Environmental>();
				MOB m=CMClass.getMOB(name);
				if(m!=null)
					Ms.add(m);
				if(lastKnownLocation!=null)
				{
					if(Ms.size()==0)
						findSomethingCalledThis(name,monster,lastKnownLocation,Ms,true);
					for(int i=0;i<Ms.size();i++)
					{
						if(Ms.get(i) instanceof MOB)
						{
							m=(MOB)((MOB)Ms.get(i)).copyOf();
							m.text();
							m.recoverPhyStats();
							m.recoverCharStats();
							m.resetToMaxState();
							m.bringToLife(lastKnownLocation,true);
							lastLoaded=m;
						}
					}
				}
				break;
			}
			case 6: // mpoload
			{
				// if not mob
				Physical addHere;
				if(scripted instanceof MOB)
					addHere=monster;
				else
				if(scripted instanceof Item)
					addHere=((Item)scripted).owner();
				else
				if(scripted instanceof Room)
					addHere=scripted;
				else
					addHere=lastKnownLocation;
				if(addHere!=null)
				{
					if(tt==null)
					{
						tt=parseBits(script,si,"Cr");
						if(tt==null)
							return null;
					}
					String name=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[1]);
					final int containerIndex=name.toUpperCase().indexOf(" INTO ");
					Container container=null;
					if(containerIndex>=0)
					{
						final ArrayList<Environmental> containers=new ArrayList<Environmental>();
						findSomethingCalledThis(name.substring(containerIndex+6).trim(),monster,lastKnownLocation,containers,false);
						for(int c=0;c<containers.size();c++)
						{
							if((containers.get(c) instanceof Container)
							&&(((Container)containers.get(c)).capacity()>0))
							{
								container=(Container)containers.get(c);
								name=name.substring(0,containerIndex).trim();
								break;
							}
						}
					}
					final long coins=CMLib.english().numPossibleGold(null,name);
					if(coins>0)
					{
						final String currency=CMLib.english().numPossibleGoldCurrency(scripted,name);
						final double denom=CMLib.english().numPossibleGoldDenomination(scripted,currency,name);
						final Coins C=CMLib.beanCounter().makeCurrency(currency,denom,coins);
						if(addHere instanceof MOB)
							((MOB)addHere).addItem(C);
						else
						if(addHere instanceof Room)
							((Room)addHere).addItem(C, Expire.Monster_EQ);
						C.putCoinsBack();
					}
					else
					if(lastKnownLocation!=null)
					{
						final ArrayList<Environmental> Is=new ArrayList<Environmental>();
						Item m=CMClass.getItem(name);
						if(m!=null)
							Is.add(m);
						else
							findSomethingCalledThis(name,monster,lastKnownLocation,Is,false);
						for(int i=0;i<Is.size();i++)
						{
							if(Is.get(i) instanceof Item)
							{
								m=(Item)Is.get(i);
								if((m!=null)
								&&(!(m instanceof ArchonOnly)))
								{
									m=(Item)m.copyOf();
									m.recoverPhyStats();
									m.setContainer(container);
									if(container instanceof MOB)
										((MOB)container.owner()).addItem(m);
									else
									if(container instanceof Room)
										((Room)container.owner()).addItem(m,ItemPossessor.Expire.Player_Drop);
									else
									if(addHere instanceof MOB)
										((MOB)addHere).addItem(m);
									else
									if(addHere instanceof Room)
										((Room)addHere).addItem(m, Expire.Monster_EQ);
									lastLoaded=m;
								}
							}
						}
						if(addHere instanceof MOB)
						{
							((MOB)addHere).recoverCharStats();
							((MOB)addHere).recoverMaxState();
						}
						addHere.recoverPhyStats();
						lastKnownLocation.recoverRoomStats();
					}
				}
				break;
			}
			case 41: // mpoloadroom
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"Cr");
					if(tt==null)
						return null;
				}
				String name=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[1]);
				if(lastKnownLocation!=null)
				{
					final ArrayList<Environmental> Is=new ArrayList<Environmental>();
					final int containerIndex=name.toUpperCase().indexOf(" INTO ");
					Container container=null;
					if(containerIndex>=0)
					{
						final ArrayList<Environmental> containers=new ArrayList<Environmental>();
						findSomethingCalledThis(name.substring(containerIndex+6).trim(),null,lastKnownLocation,containers,false);
						for(int c=0;c<containers.size();c++)
						{
							if((containers.get(c) instanceof Container)
							&&(((Container)containers.get(c)).capacity()>0))
							{
								container=(Container)containers.get(c);
								name=name.substring(0,containerIndex).trim();
								break;
							}
						}
					}
					final long coins=CMLib.english().numPossibleGold(null,name);
					if(coins>0)
					{
						final String currency=CMLib.english().numPossibleGoldCurrency(monster,name);
						final double denom=CMLib.english().numPossibleGoldDenomination(monster,currency,name);
						final Coins C=CMLib.beanCounter().makeCurrency(currency,denom,coins);
						Is.add(C);
					}
					else
					{
						final Item I=CMClass.getItem(name);
						if(I!=null)
							Is.add(I);
						else
							findSomethingCalledThis(name,monster,lastKnownLocation,Is,false);
					}
					for(int i=0;i<Is.size();i++)
					{
						if(Is.get(i) instanceof Item)
						{
							Item I=(Item)Is.get(i);
							if((I!=null)
							&&(!(I instanceof ArchonOnly)))
							{
								I=(Item)I.copyOf();
								I.recoverPhyStats();
								lastKnownLocation.addItem(I,ItemPossessor.Expire.Monster_EQ);
								I.setContainer(container);
								if(I instanceof Coins)
									((Coins)I).putCoinsBack();
								if(I instanceof RawMaterial)
									((RawMaterial)I).rebundle();
								lastLoaded=I;
							}
						}
					}
					lastKnownLocation.recoverRoomStats();
				}
				break;
			}
			case 84: // mpoloadshop
			{
				// if not mob
				ShopKeeper addHere = CMLib.coffeeShops().getShopKeeper(scripted);
				if((addHere == null)&&(scripted instanceof Item))
					addHere=CMLib.coffeeShops().getShopKeeper(((Item)scripted).owner());
				if((addHere == null)&&(scripted instanceof MOB))
					addHere=CMLib.coffeeShops().getShopKeeper(((MOB)scripted).location());
				if(addHere == null)
					addHere=CMLib.coffeeShops().getShopKeeper(lastKnownLocation);
				if(addHere!=null)
				{
					if(tt==null)
					{
						tt=parseBits(script,si,"Cr");
						if(tt==null)
							return null;
					}
					String name=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[1]);
					if(lastKnownLocation!=null)
					{
						final ArrayList<Environmental> Is=new ArrayList<Environmental>();
						int price=-1;
						if((price = name.indexOf(" PRICE="))>=0)
						{
							String rest = name.substring(price+7).trim();
							name=name.substring(0,price).trim();
							if(CMath.isInteger(rest))
								price=CMath.s_int(rest);
						}
						Item I=CMClass.getItem(name);
						if(I!=null)
							Is.add(I);
						else
							findSomethingCalledThis(name,monster,lastKnownLocation,Is,false);
						for(int i=0;i<Is.size();i++)
						{
							if(Is.get(i) instanceof Item)
							{
								I=(Item)Is.get(i);
								if((I!=null)
								&&(!(I instanceof ArchonOnly)))
								{
									I=(Item)I.copyOf();
									I.recoverPhyStats();
									CoffeeShop shop = addHere.getShop();
									if(shop != null)
									{
										Environmental E=shop.addStoreInventory(I,1,price);
										if(E!=null)
											setShopPrice(addHere, E, tmp);
									}
									I.destroy();
								}
							}
						}
						lastKnownLocation.recoverRoomStats();
					}
				}
				break;
			}
			case 85: // mpmloadshop
			{
				// if not mob
				ShopKeeper addHere = CMLib.coffeeShops().getShopKeeper(scripted);
				if((addHere == null)&&(scripted instanceof Item))
					addHere=CMLib.coffeeShops().getShopKeeper(((Item)scripted).owner());
				if((addHere == null)&&(scripted instanceof MOB))
					addHere=CMLib.coffeeShops().getShopKeeper(((MOB)scripted).location());
				if(addHere == null)
					addHere=CMLib.coffeeShops().getShopKeeper(lastKnownLocation);
				if(addHere!=null)
				{
					if(tt==null)
					{
						tt=parseBits(script,si,"Cr");
						if(tt==null)
							return null;
					}
					String name=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[1]);
					int price=-1;
					if((price = name.indexOf(" PRICE="))>=0)
					{
						String rest = name.substring(price+7).trim();
						name=name.substring(0,price).trim();
						if(CMath.isInteger(rest))
							price=CMath.s_int(rest);
					}
					final ArrayList<Environmental> Ms=new ArrayList<Environmental>();
					MOB m=CMClass.getMOB(name);
					if(m!=null)
						Ms.add(m);
					if(lastKnownLocation!=null)
					{
						if(Ms.size()==0)
							findSomethingCalledThis(name,monster,lastKnownLocation,Ms,true);
						for(int i=0;i<Ms.size();i++)
						{
							if(Ms.get(i) instanceof MOB)
							{
								m=(MOB)((MOB)Ms.get(i)).copyOf();
								m.text();
								m.recoverPhyStats();
								m.recoverCharStats();
								m.resetToMaxState();
								CoffeeShop shop = addHere.getShop();
								if(shop != null)
								{
									Environmental E=shop.addStoreInventory(m,1,price);
									if(E!=null)
										setShopPrice(addHere, E, tmp);
								}
								m.destroy();
							}
						}
					}
				}
				break;
			}
			case 42: // mphide
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"Cr");
					if(tt==null)
						return null;
				}
				final Physical newTarget=getArgumentItem(tt[1],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if(newTarget!=null)
				{
					newTarget.basePhyStats().setDisposition(newTarget.basePhyStats().disposition()|PhyStats.IS_NOT_SEEN);
					newTarget.recoverPhyStats();
					if(lastKnownLocation!=null)
						lastKnownLocation.recoverRoomStats();
				}
				break;
			}
			case 58: // mpreset
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"Cr");
					if(tt==null)
						return null;
				}
				final String arg=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[1]);
				if(arg.equalsIgnoreCase("area"))
				{
					if(lastKnownLocation!=null)
						CMLib.map().resetArea(lastKnownLocation.getArea());
				}
				else
				if(arg.equalsIgnoreCase("room"))
				{
					if(lastKnownLocation!=null)
						CMLib.map().resetRoom(lastKnownLocation, true);
				}
				else
				{
					final Room R=CMLib.map().getRoom(arg);
					if(R!=null)
						CMLib.map().resetRoom(R, true);
					else
					{
						final Area A=CMLib.map().findArea(arg);
						if(A!=null)
							CMLib.map().resetArea(A);
						else
							logError(scripted,"MPRESET","Syntax","Unknown location: "+arg+" for "+scripted.Name());
					}
				}
				break;
			}
			case 71: // mprejuv
			{
				if(tt==null)
				{
					final String rest=CMParms.getPastBitClean(s,1);
					if(rest.equals("item")||rest.equals("items"))
						tt=parseBits(script,si,"Ccr");
					else
					if(rest.equals("mob")||rest.equals("mobs"))
						tt=parseBits(script,si,"Ccr");
					else
						tt=parseBits(script,si,"Cr");
					if(tt==null)
						return null;
				}
				final String next=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[1]);
				String rest="";
				if(tt.length>2)
					rest=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]);
				int tickID=-1;
				if(rest.equalsIgnoreCase("item")||rest.equalsIgnoreCase("items"))
					tickID=Tickable.TICKID_ROOM_ITEM_REJUV;
				else
				if(rest.equalsIgnoreCase("mob")||rest.equalsIgnoreCase("mobs"))
					tickID=Tickable.TICKID_MOB;
				if(next.equalsIgnoreCase("area"))
				{
					if(lastKnownLocation!=null)
						for(final Enumeration<Room> e=lastKnownLocation.getArea().getProperMap();e.hasMoreElements();)
							CMLib.threads().rejuv(e.nextElement(),tickID);
				}
				else
				if(next.equalsIgnoreCase("room"))
				{
					if(lastKnownLocation!=null)
						CMLib.threads().rejuv(lastKnownLocation,tickID);
				}
				else
				{
					final Room R=CMLib.map().getRoom(next);
					if(R!=null)
						CMLib.threads().rejuv(R,tickID);
					else
					{
						final Area A=CMLib.map().findArea(next);
						if(A!=null)
						{
							for(final Enumeration e=A.getProperMap();e.hasMoreElements();)
								CMLib.threads().rejuv((Room)e.nextElement(),tickID);
						}
						else
							logError(scripted,"MPREJUV","Syntax","Unknown location: "+next+" for "+scripted.Name());
					}
				}
				break;
			}
			case 56: // mpstop
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"Cr");
					if(tt==null)
						return null;
				}
				final Vector<MOB> V=new Vector<MOB>();
				final String who=tt[1];
				if(who.equalsIgnoreCase("all"))
				{
					for(int i=0;i<lastKnownLocation.numInhabitants();i++)
						V.addElement(lastKnownLocation.fetchInhabitant(i));
				}
				else
				{
					final Environmental newTarget=getArgumentItem(who,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
					if(newTarget instanceof MOB)
						V.addElement((MOB)newTarget);
				}
				for(int v=0;v<V.size();v++)
				{
					final Environmental newTarget=V.elementAt(v);
					if(newTarget instanceof MOB)
					{
						final MOB mob=(MOB)newTarget;
						Ability A=null;
						for(int a=mob.numEffects()-1;a>=0;a--)
						{
							A=mob.fetchEffect(a);
							if((A!=null)
							&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_COMMON_SKILL)
							&&(A.canBeUninvoked())
							&&(!A.isAutoInvoked()))
								A.unInvoke();
						}
						mob.makePeace(false);
						if(lastKnownLocation!=null)
							lastKnownLocation.recoverRoomStats();
					}
				}
				break;
			}
			case 43: // mpunhide
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"Cr");
					if(tt==null)
						return null;
				}
				final Physical newTarget=getArgumentItem(tt[1],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((newTarget!=null)&&(CMath.bset(newTarget.basePhyStats().disposition(),PhyStats.IS_NOT_SEEN)))
				{
					newTarget.basePhyStats().setDisposition(newTarget.basePhyStats().disposition()-PhyStats.IS_NOT_SEEN);
					newTarget.recoverPhyStats();
					if(lastKnownLocation!=null)
						lastKnownLocation.recoverRoomStats();
				}
				break;
			}
			case 44: // mpopen
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"Cr");
					if(tt==null)
						return null;
				}
				final Environmental newTarget=getArgumentItem(tt[1],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((newTarget instanceof Exit)&&(((Exit)newTarget).hasADoor()))
				{
					final Exit E=(Exit)newTarget;
					E.setDoorsNLocks(E.hasADoor(),true,E.defaultsClosed(),E.hasALock(),false,E.defaultsLocked());
					if(lastKnownLocation!=null)
						lastKnownLocation.recoverRoomStats();
				}
				else
				if((newTarget instanceof Container)&&(((Container)newTarget).hasADoor()))
				{
					final Container E=(Container)newTarget;
					E.setDoorsNLocks(E.hasADoor(),true,E.defaultsClosed(),E.hasALock(),false,E.defaultsLocked());
					if(lastKnownLocation!=null)
						lastKnownLocation.recoverRoomStats();
				}
				break;
			}
			case 45: // mpclose
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"Cr");
					if(tt==null)
						return null;
				}
				final Environmental newTarget=getArgumentItem(tt[1],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((newTarget instanceof Exit)
				&&(((Exit)newTarget).hasADoor())
				&&(((Exit)newTarget).isOpen()))
				{
					final Exit E=(Exit)newTarget;
					E.setDoorsNLocks(E.hasADoor(),false,E.defaultsClosed(),E.hasALock(),false,E.defaultsLocked());
					if(lastKnownLocation!=null)
						lastKnownLocation.recoverRoomStats();
				}
				else
				if((newTarget instanceof Container)
				&&(((Container)newTarget).hasADoor())
				&&(((Container)newTarget).isOpen()))
				{
					final Container E=(Container)newTarget;
					E.setDoorsNLocks(E.hasADoor(),false,E.defaultsClosed(),E.hasALock(),false,E.defaultsLocked());
					if(lastKnownLocation!=null)
						lastKnownLocation.recoverRoomStats();
				}
				break;
			}
			case 46: // mplock
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"Cr");
					if(tt==null)
						return null;
				}
				final Environmental newTarget=getArgumentItem(tt[1],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((newTarget instanceof Exit)
				&&(((Exit)newTarget).hasALock()))
				{
					final Exit E=(Exit)newTarget;
					E.setDoorsNLocks(E.hasADoor(),false,E.defaultsClosed(),E.hasALock(),true,E.defaultsLocked());
					if(lastKnownLocation!=null)
						lastKnownLocation.recoverRoomStats();
				}
				else
				if((newTarget instanceof Container)
				&&(((Container)newTarget).hasALock()))
				{
					final Container E=(Container)newTarget;
					E.setDoorsNLocks(E.hasADoor(),false,E.defaultsClosed(),E.hasALock(),true,E.defaultsLocked());
					if(lastKnownLocation!=null)
						lastKnownLocation.recoverRoomStats();
				}
				break;
			}
			case 47: // mpunlock
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"Cr");
					if(tt==null)
						return null;
				}
				final Environmental newTarget=getArgumentItem(tt[1],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((newTarget instanceof Exit)
				&&(((Exit)newTarget).isLocked()))
				{
					final Exit E=(Exit)newTarget;
					E.setDoorsNLocks(E.hasADoor(),false,E.defaultsClosed(),E.hasALock(),false,E.defaultsLocked());
					if(lastKnownLocation!=null)
						lastKnownLocation.recoverRoomStats();
				}
				else
				if((newTarget instanceof Container)
				&&(((Container)newTarget).isLocked()))
				{
					final Container E=(Container)newTarget;
					E.setDoorsNLocks(E.hasADoor(),false,E.defaultsClosed(),E.hasALock(),false,E.defaultsLocked());
					if(lastKnownLocation!=null)
						lastKnownLocation.recoverRoomStats();
				}
				break;
			}
			case 48: // return
				if(tt==null)
				{
					tt=parseBits(script,si,"Cr");
					if(tt==null)
						return null;
				}
				tickStatus=Tickable.STATUS_END;
				return varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[1]);
			case 7: // mpechoat
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"Ccp");
					if(tt==null)
						return null;
				}
				final String parm=tt[1];
				final Environmental newTarget=getArgumentMOB(parm,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if((newTarget!=null)&&(newTarget instanceof MOB)&&(lastKnownLocation!=null))
				{
					if(newTarget==monster)
						lastKnownLocation.showSource(monster,null,null,CMMsg.MSG_OK_ACTION,varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]));
					else
						lastKnownLocation.show(monster,newTarget,null,CMMsg.MSG_OK_ACTION,null,CMMsg.MSG_OK_ACTION,varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]),CMMsg.NO_EFFECT,null);
				}
				else
				if(parm.equalsIgnoreCase("world"))
				{
					lastKnownLocation.showSource(monster,null,CMMsg.MSG_OK_ACTION,varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]));
					for(final Enumeration e=CMLib.map().rooms();e.hasMoreElements();)
					{
						final Room R=(Room)e.nextElement();
						if(R.numInhabitants()>0)
							R.showOthers(monster,null,CMMsg.MSG_OK_ACTION,varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]));
					}
				}
				else
				if(parm.equalsIgnoreCase("area")&&(lastKnownLocation!=null))
				{
					lastKnownLocation.showSource(monster,null,CMMsg.MSG_OK_ACTION,varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]));
					for(final Enumeration e=lastKnownLocation.getArea().getProperMap();e.hasMoreElements();)
					{
						final Room R=(Room)e.nextElement();
						if(R.numInhabitants()>0)
							R.showOthers(monster,null,CMMsg.MSG_OK_ACTION,varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]));
					}
				}
				else
				if(CMLib.map().getRoom(parm)!=null)
					CMLib.map().getRoom(parm).show(monster,null,CMMsg.MSG_OK_ACTION,varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]));
				else
				if(CMLib.map().findArea(parm)!=null)
				{
					lastKnownLocation.showSource(monster,null,CMMsg.MSG_OK_ACTION,varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]));
					for(final Enumeration e=CMLib.map().findArea(parm).getMetroMap();e.hasMoreElements();)
					{
						final Room R=(Room)e.nextElement();
						if(R.numInhabitants()>0)
							R.showOthers(monster,null,CMMsg.MSG_OK_ACTION,varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]));
					}
				}
				break;
			}
			case 8: // mpechoaround
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"Ccp");
					if(tt==null)
						return null;
				}
				final Environmental newTarget=getArgumentMOB(tt[1],source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if((newTarget!=null)&&(newTarget instanceof MOB)&&(lastKnownLocation!=null))
				{
					lastKnownLocation.showOthers((MOB)newTarget,null,CMMsg.MSG_OK_ACTION,varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]));
				}
				break;
			}
			case 9: // mpcast
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"Ccr");
					if(tt==null)
						return null;
				}
				final String cast=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[1]);
				final Physical newTarget=getArgumentItem(tt[2],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				Ability A=null;
				if(cast!=null)
					A=CMClass.findAbility(cast);
				if((newTarget!=null)&&(A!=null))
				{
					A.setProficiency(100);
					A.invoke(monster,newTarget,false,0);
				}
				break;
			}
			case 30: // mpaffect
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"Cccp");
					if(tt==null)
						return null;
				}
				final String cast=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[1]);
				final Physical newTarget=getArgumentItem(tt[2],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				final String m2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[3]);
				Ability A=null;
				if(cast!=null)
					A=CMClass.findAbility(cast);
				if((newTarget!=null)&&(A!=null))
				{
					if((newTarget instanceof MOB)&&(!((MOB)newTarget).isMonster()))
						Log.sysOut("Scripting",newTarget.Name()+" was MPAFFECTED by "+A.Name());
					A.setMiscText(m2);
					if((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PROPERTY)
						newTarget.addNonUninvokableEffect(A);
					else
						A.invoke(monster,CMParms.parse(m2),newTarget,true,0);
				}
				break;
			}
			case 80: // mpspeak
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"Ccr");
					if(tt==null)
						return null;
				}
				final String language=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]);
				final Environmental newTarget=getArgumentMOB(tt[1],source,monster,target,primaryItem,secondaryItem,msg,tmp);
				final Ability A=CMClass.getAbility(language);
				if((A instanceof Language)&&(newTarget instanceof MOB))
				{
					((Language)A).setProficiency(100);
					((Language)A).autoInvocation((MOB)newTarget, false);
					final Ability langA=((MOB)newTarget).fetchEffect(A.ID());
					if(langA!=null)
					{
						if(((MOB)newTarget).isMonster())
							langA.setProficiency(100);
						langA.invoke((MOB)newTarget,CMParms.parse(""),(MOB)newTarget,false,0);
					}
					else
						A.invoke((MOB)newTarget,CMParms.parse(""),(MOB)newTarget,false,0);
				}
				break;
			}
			case 81: // mpsetclan
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"Cccr");
					if(tt==null)
						return null;
				}
				final PhysicalAgent newTarget=getArgumentMOB(tt[1],source,monster,target,primaryItem,secondaryItem,msg,tmp);
				final String clan=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]);
				final String roleStr=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[3]);
				Clan C=CMLib.clans().getClan(clan);
				if(C==null)
					C=CMLib.clans().findClan(clan);
				if((newTarget instanceof MOB)&&(C!=null))
				{
					int role=Integer.MIN_VALUE;
					if(CMath.isInteger(roleStr))
						role=CMath.s_int(roleStr);
					else
					for(int i=0;i<C.getRolesList().length;i++)
					{
						if(roleStr.equalsIgnoreCase(C.getRolesList()[i]))
							role=i;
					}
					if(role!=Integer.MIN_VALUE)
					{
						if(((MOB)newTarget).isPlayer())
							C.addMember((MOB)newTarget, role);
						else
							((MOB)newTarget).setClan(C.clanID(), role);
					}
				}
				break;
			}
			case 31: // mpbehave
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"Cccp");
					if(tt==null)
						return null;
				}
				String cast=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[1]);
				final PhysicalAgent newTarget=getArgumentItem(tt[2],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				final String m2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[3]);
				Behavior B=null;
				final Behavior B2=(cast==null)?null:CMClass.findBehavior(cast);
				if(B2!=null)
					cast=B2.ID();
				if((cast!=null)&&(newTarget!=null))
				{
					B=newTarget.fetchBehavior(cast);
					if(B==null)
						B=CMClass.getBehavior(cast);
				}
				if((newTarget!=null)&&(B!=null))
				{
					if((newTarget instanceof MOB)&&(!((MOB)newTarget).isMonster()))
						Log.sysOut("Scripting",newTarget.Name()+" was MPBEHAVED with "+B.name());
					B.setParms(m2);
					if(newTarget.fetchBehavior(B.ID())==null)
					{
						newTarget.addBehavior(B);
						if((defaultQuestName()!=null)&&(defaultQuestName().length()>0))
						B.registerDefaultQuest(defaultQuestName());
					}
				}
				break;
			}
			case 72: // mpscript
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"Ccp");
					if(tt==null)
						return null;
				}
				final PhysicalAgent newTarget=getArgumentItem(tt[1],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				String m2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]);
				boolean proceed=true;
				boolean savable=false;
				boolean execute=false;
				String scope=getVarScope();
				while(proceed)
				{
					proceed=false;
					if(m2.toUpperCase().startsWith("SAVABLE"))
					{
						savable=true;
						m2=m2.substring(8).trim();
						proceed=true;
					}
					else
					if(m2.toUpperCase().startsWith("EXECUTE"))
					{
						execute=true;
						m2=m2.substring(8).trim();
						proceed=true;
					}
					else
					if(m2.toUpperCase().startsWith("GLOBAL"))
					{
						scope="";
						proceed=true;
						m2=m2.substring(6).trim();
					}
					else
					if(m2.toUpperCase().startsWith("INDIVIDUAL")||m2.equals("*"))
					{
						scope="*";
						proceed=true;
						m2=m2.substring(10).trim();
					}
				}
				if((newTarget!=null)&&(m2.length()>0))
				{
					if((newTarget instanceof MOB)&&(!((MOB)newTarget).isMonster()))
						Log.sysOut("Scripting",newTarget.Name()+" was MPSCRIPTED: "+defaultQuestName);
					final ScriptingEngine S=(ScriptingEngine)CMClass.getCommon("DefaultScriptingEngine");
					S.setSavable(savable);
					S.setVarScope(scope);
					S.setScript(m2);
					if((defaultQuestName()!=null)&&(defaultQuestName().length()>0))
						S.registerDefaultQuest(defaultQuestName());
					newTarget.addScript(S);
					if(execute)
					{
						S.tick(newTarget,Tickable.TICKID_MOB);
						for(int i=0;i<5;i++)
							S.dequeResponses();
						newTarget.delScript(S);
					}
				}
				break;
			}
			case 32: // mpunbehave
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"Ccr");
					if(tt==null)
						return null;
				}
				String cast=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[1]);
				final PhysicalAgent newTarget=getArgumentItem(tt[2],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((newTarget!=null)&&(cast!=null))
				{
					Behavior B=CMClass.findBehavior(cast);
					if(B!=null)
						cast=B.ID();
					B=newTarget.fetchBehavior(cast);
					if(B!=null)
						newTarget.delBehavior(B);
					if((newTarget instanceof MOB)&&(!((MOB)newTarget).isMonster())&&(B!=null))
						Log.sysOut("Scripting",newTarget.Name()+" was MPUNBEHAVED with "+B.name());
				}
				break;
			}
			case 33: // mptattoo
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"Ccr");
					if(tt==null)
						return null;
				}
				final Environmental newTarget=getArgumentItem(tt[1],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				String tattooName=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]);
				if((newTarget!=null)&&(tattooName.length()>0)&&(newTarget instanceof MOB))
				{
					final MOB themob=(MOB)newTarget;
					final boolean tattooMinus=tattooName.startsWith("-");
					if(tattooMinus)
						tattooName=tattooName.substring(1);
					final Tattoo pT=((Tattoo)CMClass.getCommon("DefaultTattoo")).parse(tattooName);
					final Tattoo T=themob.findTattoo(pT.getTattooName());
					if(T!=null)
					{
						if(tattooMinus)
							themob.delTattoo(T);
					}
					else
					if(!tattooMinus)
						themob.addTattoo(pT);
				}
				break;
			}
			case 83: // mpacctattoo
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"Ccr");
					if(tt==null)
						return null;
				}
				final Environmental newTarget=getArgumentItem(tt[1],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				String tattooName=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]);
				if((newTarget!=null)
				&&(tattooName.length()>0)
				&&(newTarget instanceof MOB)
				&&(((MOB)newTarget).playerStats()!=null)
				&&(((MOB)newTarget).playerStats().getAccount()!=null))
				{
					final Tattooable themob=((MOB)newTarget).playerStats().getAccount();
					final boolean tattooMinus=tattooName.startsWith("-");
					if(tattooMinus)
						tattooName=tattooName.substring(1);
					final Tattoo pT=((Tattoo)CMClass.getCommon("DefaultTattoo")).parse(tattooName);
					final Tattoo T=themob.findTattoo(pT.getTattooName());
					if(T!=null)
					{
						if(tattooMinus)
							themob.delTattoo(T);
					}
					else
					if(!tattooMinus)
						themob.addTattoo(pT);
				}
				break;
			}
			case 55: // mpnotrigger
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"Ccr");
					if(tt==null)
						return null;
				}
				final String trigger=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[1]);
				final String time=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]);
				int triggerCode=-1;
				for(int i=0;i<progs.length;i++)
				{
					if(trigger.equalsIgnoreCase(progs[i]))
						triggerCode=i;
				}
				if(triggerCode<0)
					logError(scripted,"MPNOTRIGGER","RunTime",trigger+" is not a valid trigger name.");
				else
				if(!CMath.isInteger(time.trim()))
					logError(scripted,"MPNOTRIGGER","RunTime",time+" is not a valid milisecond time.");
				else
				{
					noTrigger.remove(Integer.valueOf(triggerCode));
					noTrigger.put(Integer.valueOf(triggerCode),Long.valueOf(System.currentTimeMillis()+CMath.s_long(time.trim())));
				}
				break;
			}
			case 54: // mpfaction
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"Cccr");
					if(tt==null)
						return null;
				}
				final Environmental newTarget=getArgumentMOB(tt[1],source,monster,target,primaryItem,secondaryItem,msg,tmp);
				final String faction=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]);
				String range=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[3]).trim();
				final Faction F=CMLib.factions().getFaction(faction);
				if((newTarget!=null)&&(F!=null)&&(newTarget instanceof MOB))
				{
					final MOB themob=(MOB)newTarget;
					int curFaction = themob.fetchFaction(F.factionID());
					if((curFaction == Integer.MAX_VALUE)||(curFaction == Integer.MIN_VALUE))
						curFaction = F.findDefault(themob);
					if((range.startsWith("--"))&&(CMath.isInteger(range.substring(2).trim())))
					{
						final int amt=CMath.s_int(range.substring(2).trim());
						themob.tell(L("You lose @x1 faction with @x2.",""+amt,F.name()));
						range=""+(curFaction-amt);
					}
					else
					if((range.startsWith("+"))&&(CMath.isInteger(range.substring(1).trim())))
					{
						final int amt=CMath.s_int(range.substring(1).trim());
						themob.tell(L("You gain @x1 faction with @x2.",""+amt,F.name()));
						range=""+(curFaction+amt);
					}
					else
					if(CMath.isInteger(range))
						themob.tell(L("Your faction with @x1 is now @x2.",F.name(),""+CMath.s_int(range.trim())));
					
					if(CMath.isInteger(range))
						themob.addFaction(F.factionID(),CMath.s_int(range.trim()));
					else
					{
						Faction.FRange FR=null;
						final Enumeration<Faction.FRange> e=CMLib.factions().getRanges(CMLib.factions().AlignID());
						if(e!=null)
						for(;e.hasMoreElements();)
						{
							final Faction.FRange FR2=e.nextElement();
							if(FR2.name().equalsIgnoreCase(range))
							{
								FR = FR2;
								break;
							}
						}
						if(FR==null)
							logError(scripted,"MPFACTION","RunTime",range+" is not a valid range for "+F.name()+".");
						else
						{
							themob.tell(L("Your faction with @x1 is now @x2.",F.name(),FR.name()));
							themob.addFaction(F.factionID(),FR.low()+((FR.high()-FR.low())/2));
						}
					}
				}
				break;
			}
			case 49: // mptitle
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"Ccr");
					if(tt==null)
						return null;
				}
				final Environmental newTarget=getArgumentItem(tt[1],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				String titleStr=varify(monster, newTarget, scripted, monster, secondaryItem, secondaryItem, msg, tmp, tt[2]);
				if((newTarget!=null)&&(titleStr.length()>0)&&(newTarget instanceof MOB))
				{
					final MOB themob=(MOB)newTarget;
					final boolean tattooMinus=titleStr.startsWith("-");
					if(tattooMinus)
						titleStr=titleStr.substring(1);
					if(themob.playerStats()!=null)
					{
						if(themob.playerStats().getTitles().contains(titleStr))
						{
							if(tattooMinus)
								themob.playerStats().getTitles().remove(titleStr);
						}
						else
						if(!tattooMinus)
							themob.playerStats().getTitles().add(0,titleStr);
					}
				}
				break;
			}
			case 10: // mpkill
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"Cr");
					if(tt==null)
						return null;
				}
				final Environmental newTarget=getArgumentMOB(tt[1],source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if((newTarget!=null)&&(newTarget instanceof MOB)&&(monster!=null))
					monster.setVictim((MOB)newTarget);
				break;
			}
			case 51: // mpsetclandata
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"Cccr");
					if(tt==null)
						return null;
				}
				final Environmental newTarget=getArgumentItem(tt[1],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				String clanID=null;
				if((newTarget!=null)&&(newTarget instanceof MOB))
				{
					Clan C=CMLib.clans().findRivalrousClan((MOB)newTarget);
					if(C==null)
						C=((MOB)newTarget).clans().iterator().hasNext()?((MOB)newTarget).clans().iterator().next().first:null;
					if(C!=null)
						clanID=C.clanID();
				}
				else
					clanID=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[1]);
				final String clanvar=tt[2];
				final String clanval=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[3]);
				final Clan C=CMLib.clans().getClan(clanID);
				if(C!=null)
				{
					if(!C.isStat(clanvar))
						logError(scripted,"MPSETCLANDATA","RunTime",clanvar+" is not a valid clan variable.");
					else
					{
						C.setStat(clanvar,clanval.trim());
						if(C.getStat(clanvar).equalsIgnoreCase(clanval))
							C.update();
					}
				}
				break;
			}
			case 52: // mpplayerclass
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"Ccr");
					if(tt==null)
						return null;
				}
				final Environmental newTarget=getArgumentItem(tt[1],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((newTarget!=null)&&(newTarget instanceof MOB))
				{
					final Vector<String> V=CMParms.parse(tt[2]);
					for(int i=0;i<V.size();i++)
					{
						if(CMath.isInteger(V.elementAt(i).trim()))
							((MOB)newTarget).baseCharStats().setClassLevel(((MOB)newTarget).baseCharStats().getCurrentClass(),CMath.s_int(V.elementAt(i).trim()));
						else
						{
							final CharClass C=CMClass.findCharClass(V.elementAt(i));
							if((C!=null)&&(C.availabilityCode()!=0))
								((MOB)newTarget).baseCharStats().setCurrentClass(C);
						}
					}
					((MOB)newTarget).recoverCharStats();
				}
				break;
			}
			case 12: // mppurge
			{
				if(lastKnownLocation!=null)
				{
					int flag=0;
					if(tt==null)
					{
						final String s2=CMParms.getCleanBit(s,1).toLowerCase();
						if(s2.equals("room"))
							tt=parseBits(script,si,"Ccr");
						else
						if(s2.equals("my"))
							tt=parseBits(script,si,"Ccr");
						else
							tt=parseBits(script,si,"Cr");
						if(tt==null)
							return null;
					}
					String s2=tt[1];
					if(s2.equalsIgnoreCase("room"))
					{
						flag=1;
						s2=tt[2];
					}
					else
					if(s2.equalsIgnoreCase("my"))
					{
						flag=2;
						s2=tt[2];
					}
					Environmental E=null;
					if(s2.equalsIgnoreCase("self")||s2.equalsIgnoreCase("me"))
						E=scripted;
					else
					if(flag==1)
					{
						s2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,s2);
						E=lastKnownLocation.fetchFromRoomFavorItems(null,s2);
					}
					else
					if(flag==2)
					{
						s2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,s2);
						if(monster!=null)
							E=monster.findItem(s2);
					}
					else
						E=getArgumentItem(s2,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
					if(E!=null)
					{
						if(E instanceof MOB)
						{
							if(!((MOB)E).isMonster())
							{
								if(((MOB)E).getStartRoom()!=null)
									((MOB)E).getStartRoom().bringMobHere((MOB)E,false);
								((MOB)E).session().stopSession(false,false,false);
							}
							else
							if(((MOB)E).getStartRoom()!=null)
								((MOB)E).killMeDead(false);
							else
								((MOB)E).destroy();
						}
						else
						if(E instanceof Item)
						{
							final ItemPossessor oE=((Item)E).owner();
							((Item)E).destroy();
							if(oE!=null)
								oE.recoverPhyStats();
						}
					}
					lastKnownLocation.recoverRoomStats();
				}
				break;
			}
			case 14: // mpgoto
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"Cr");
					if(tt==null)
						return null;
				}
				final String roomID=tt[1].trim();
				if((roomID.length()>0)&&(lastKnownLocation!=null))
				{
					Room goHere=null;
					if(roomID.startsWith("$"))
						goHere=CMLib.map().roomLocation(this.getArgumentItem(roomID,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp));
					if(goHere==null)
						goHere=getRoom(varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,roomID),lastKnownLocation);
					if(goHere!=null)
					{
						if(scripted instanceof MOB)
							goHere.bringMobHere((MOB)scripted,true);
						else
						if(scripted instanceof Item)
							goHere.moveItemTo((Item)scripted,ItemPossessor.Expire.Player_Drop,ItemPossessor.Move.Followers);
						else
						{
							goHere.bringMobHere(monster,true);
							if(!(scripted instanceof MOB))
								goHere.delInhabitant(monster);
						}
						if(CMLib.map().roomLocation(scripted)==goHere)
							lastKnownLocation=goHere;
					}
				}
				break;
			}
			case 15: // mpat
				if(lastKnownLocation!=null)
				{
					if(tt==null)
					{
						tt=parseBits(script,si,"Ccp");
						if(tt==null)
							return null;
					}
					final Room lastPlace=lastKnownLocation;
					final String roomName=tt[1];
					if(roomName.length()>0)
					{
						final String doWhat=tt[2].trim();
						Room goHere=null;
						if(roomName.startsWith("$"))
							goHere=CMLib.map().roomLocation(this.getArgumentItem(roomName,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp));
						if(goHere==null)
							goHere=getRoom(varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,roomName),lastKnownLocation);
						if(goHere!=null)
						{
							goHere.bringMobHere(monster,true);
							final DVector DV=new DVector(2);
							DV.addElement("",null);
							DV.addElement(doWhat,null);
							lastKnownLocation=goHere;
							execute(scripted,source,target,monster,primaryItem,secondaryItem,DV,msg,tmp);
							lastKnownLocation=lastPlace;
							lastPlace.bringMobHere(monster,true);
							if(!(scripted instanceof MOB))
							{
								goHere.delInhabitant(monster);
								lastPlace.delInhabitant(monster);
							}
						}
					}
				}
				break;
			case 17: // mptransfer
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"Ccr");
					if(tt==null)
						return null;
				}
				String mobName=tt[1];
				String roomName=tt[2].trim();
				Room newRoom=null;
				if(roomName.startsWith("$"))
					newRoom=CMLib.map().roomLocation(this.getArgumentItem(roomName,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp));
				if((roomName.length()==0)&&(lastKnownLocation!=null))
					roomName=lastKnownLocation.roomID();
				if(roomName.length()>0)
				{
					if(newRoom==null)
						newRoom=getRoom(varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,roomName),lastKnownLocation);
					if(newRoom!=null)
					{
						final ArrayList<Environmental> V=new ArrayList<Environmental>();
						if(mobName.startsWith("$"))
						{
							final Environmental E=getArgumentItem(mobName,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
							if(E!=null)
								V.add(E);
						}
						if(V.size()==0)
						{
							mobName=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,mobName);
							if(mobName.equalsIgnoreCase("all"))
							{
								if(lastKnownLocation!=null)
								{
									for(int x=0;x<lastKnownLocation.numInhabitants();x++)
									{
										final MOB m=lastKnownLocation.fetchInhabitant(x);
										if((m!=null)&&(m!=monster)&&(!V.contains(m)))
											V.add(m);
									}
								}
							}
							else
							{
								MOB findOne=null;
								Area A=null;
								if(lastKnownLocation!=null)
								{
									findOne=lastKnownLocation.fetchInhabitant(mobName);
									A=lastKnownLocation.getArea();
									if((findOne!=null)&&(findOne!=monster))
										V.add(findOne);
								}
								if(findOne==null)
								{
									findOne=CMLib.players().getPlayer(mobName);
									if((findOne!=null)&&(!CMLib.flags().isInTheGame(findOne,true)))
										findOne=null;
									if((findOne!=null)&&(findOne!=monster))
										V.add(findOne);
								}
								if((findOne==null)&&(A!=null))
								{
									for(final Enumeration r=A.getProperMap();r.hasMoreElements();)
									{
										final Room R=(Room)r.nextElement();
										findOne=R.fetchInhabitant(mobName);
										if((findOne!=null)&&(findOne!=monster))
											V.add(findOne);
									}
								}
							}
						}
						for(int v=0;v<V.size();v++)
						{
							if(V.get(v) instanceof MOB)
							{
								final MOB mob=(MOB)V.get(v);
								final Set<MOB> H=mob.getGroupMembers(new HashSet<MOB>());
								for (final Object element : H)
								{
									final MOB M=(MOB)element;
									if((!V.contains(M))&&(M.location()==mob.location()))
										V.add(M);
								}
							}
						}
						for(int v=0;v<V.size();v++)
						{
							if(V.get(v) instanceof MOB)
							{
								final MOB follower=(MOB)V.get(v);
								final Room thisRoom=follower.location();
								final int dispmask=(PhyStats.IS_SLEEPING | PhyStats.IS_SITTING);
								final int dispo1 = follower.basePhyStats().disposition() &  dispmask;
								final int dispo2 = follower.phyStats().disposition() &  dispmask;
								follower.basePhyStats().setDisposition(follower.basePhyStats().disposition() & (~dispmask));
								follower.phyStats().setDisposition(follower.phyStats().disposition() & (~dispmask));
								// scripting guide calls for NO text -- empty is probably req tho
								final CMMsg enterMsg=CMClass.getMsg(follower,newRoom,null,CMMsg.MSG_ENTER|CMMsg.MASK_ALWAYS,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER," "+CMLib.protocol().msp("appear.wav",10));
								final CMMsg leaveMsg=CMClass.getMsg(follower,thisRoom,null,CMMsg.MSG_LEAVE|CMMsg.MASK_ALWAYS," ");
								if((thisRoom!=null)
								&&thisRoom.okMessage(follower,leaveMsg)
								&&newRoom.okMessage(follower,enterMsg))
								{
									if(follower.isInCombat())
									{
										CMLib.commands().postFlee(follower,("NOWHERE"));
										follower.makePeace(true);
									}
									thisRoom.send(follower,leaveMsg);
									newRoom.bringMobHere(follower,false);
									newRoom.send(follower,enterMsg);
									follower.basePhyStats().setDisposition(follower.basePhyStats().disposition() | dispo1);
									follower.phyStats().setDisposition(follower.phyStats().disposition() | dispo2);
									if(!CMLib.flags().isSleeping(follower))
									{
										follower.tell(CMLib.lang().L("\n\r\n\r"));
										CMLib.commands().postLook(follower,true);
									}
								}
								else
								{
									follower.basePhyStats().setDisposition(follower.basePhyStats().disposition() | dispo1);
									follower.phyStats().setDisposition(follower.phyStats().disposition() | dispo2);
								}
							}
							else
							if((V.get(v) instanceof Item)
							&&(newRoom!=CMLib.map().roomLocation(V.get(v))))
								newRoom.moveItemTo((Item)V.get(v),ItemPossessor.Expire.Player_Drop,ItemPossessor.Move.Followers);
							if(V.get(v)==scripted)
								lastKnownLocation=newRoom;
						}
					}
				}
				break;
			}
			case 25: // mpbeacon
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"Ccr");
					if(tt==null)
						return null;
				}
				final String roomName=tt[1];
				Room newRoom=null;
				if((roomName.length()>0)&&(lastKnownLocation!=null))
				{
					final String beacon=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]);
					if(roomName.startsWith("$"))
						newRoom=CMLib.map().roomLocation(this.getArgumentItem(roomName,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp));
					if(newRoom==null)
						newRoom=getRoom(varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,roomName),lastKnownLocation);
					if(newRoom == null)
						logError(scripted,"MPBEACON","RunTime",tt[1]+" is not a room.");
					else
					if(lastKnownLocation!=null)
					{
						final Vector<MOB> V=new Vector<MOB>();
						if(beacon.equalsIgnoreCase("all"))
						{
							for(int x=0;x<lastKnownLocation.numInhabitants();x++)
							{
								final MOB m=lastKnownLocation.fetchInhabitant(x);
								if((m!=null)&&(m!=monster)&&(!m.isMonster())&&(!V.contains(m)))
									V.addElement(m);
							}
						}
						else
						{
							final MOB findOne=lastKnownLocation.fetchInhabitant(beacon);
							if((findOne!=null)&&(findOne!=monster)&&(!findOne.isMonster()))
								V.addElement(findOne);
						}
						for(int v=0;v<V.size();v++)
						{
							final MOB follower=V.elementAt(v);
							if(!follower.isMonster())
								follower.setStartRoom(newRoom);
						}
					}
				}
				break;
			}
			case 18: // mpforce
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"Ccp");
					if(tt==null)
						return null;
				}
				final PhysicalAgent newTarget=getArgumentMOB(tt[1],source,monster,target,primaryItem,secondaryItem,msg,tmp);
				final String force=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]).trim();
				if(newTarget!=null)
				{
					final DVector vscript=new DVector(2);
					vscript.addElement("FUNCTION_PROG MPFORCE_"+System.currentTimeMillis()+Math.random(),null);
					vscript.addElement(force,null);
					// this can not be permanently parsed because it is variable
					execute(newTarget, source, target, getMakeMOB(newTarget), primaryItem, secondaryItem, vscript, msg, tmp);
				}
				break;
			}
			case 79: // mppossess
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"Cr");
					if(tt==null)
						return null;
				}
				final PhysicalAgent newSource=getArgumentMOB(tt[1],source,monster,target,primaryItem,secondaryItem,msg,tmp);
				final PhysicalAgent newTarget=getArgumentMOB(tt[2],source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if((!(newSource instanceof MOB))||(((MOB)newSource).isMonster()))
					logError(scripted,"MPPOSSESS","RunTime",tt[1]+" is not a player.");
				else
				if((!(newTarget instanceof MOB))||(!((MOB)newTarget).isMonster())||CMSecurity.isASysOp((MOB)newTarget))
					logError(scripted,"MPPOSSESS","RunTime",tt[2]+" is not a mob.");
				else
				{
					final MOB mobM=(MOB)newSource;
					final MOB targetM=(MOB)newTarget;
					final Session S=mobM.session();
					S.setMob(targetM);
					targetM.setSession(S);
					targetM.setSoulMate(mobM);
					mobM.setSession(null);
				}
				break;
			}
			case 20: // mpsetvar
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"Cccr");
					if(tt==null)
						return null;
				}
				String which=tt[1];
				final Environmental E=getArgumentItem(which,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				final String arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]);
				final String arg3=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[3]);
				if(!which.equals("*"))
				{
					if(E==null)
						which=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,which);
					else
					if(E instanceof Room)
						which=CMLib.map().getExtendedRoomID((Room)E);
					else
						which=E.Name();
				}
				if((which.length()>0)&&(arg2.length()>0))
					setVar(which,arg2,arg3);
				break;
			}
			case 36: // mpsavevar
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"CcR");
					if(tt==null)
						return null;
				}
				String which=tt[1];
				String arg2=tt[2];
				final Environmental E=getArgumentItem(which,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				which=getVarHost(E,which,source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp);
				if((which.length()>0)&&(arg2.length()>0))
				{
					final DVector V=getScriptVarSet(which,arg2);
					for(int v=0;v<V.size();v++)
					{
						which=(String)V.elementAt(0,1);
						arg2=((String)V.elementAt(0,2)).toUpperCase();
						final Hashtable H=(Hashtable)resources._getResource("SCRIPTVAR-"+which);
						String val="";
						if(H!=null)
						{
							val=(String)H.get(arg2);
							if(val==null)
								val="";
						}
						if(val.length()>0)
							CMLib.database().DBReCreatePlayerData(which,"SCRIPTABLEVARS",which.toUpperCase()+"_SCRIPTABLEVARS_"+arg2,val);
						else
							CMLib.database().DBDeletePlayerData(which,"SCRIPTABLEVARS",which.toUpperCase()+"_SCRIPTABLEVARS_"+arg2);
					}
				}
				break;
			}
			case 39: // mploadvar
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"CcR");
					if(tt==null)
						return null;
				}
				String which=tt[1];
				final String arg2=tt[2];
				final Environmental E=getArgumentItem(which,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if(arg2.length()>0)
				{
					List<PlayerData> V=null;
					which=getVarHost(E,which,source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp);
					if(arg2.equals("*"))
						V=CMLib.database().DBReadPlayerData(which,"SCRIPTABLEVARS");
					else
						V=CMLib.database().DBReadPlayerData(which,"SCRIPTABLEVARS",which.toUpperCase()+"_SCRIPTABLEVARS_"+arg2);
					if((V!=null)&&(V.size()>0))
					for(int v=0;v<V.size();v++)
					{
						final DatabaseEngine.PlayerData VAR=V.get(v);
						String varName=VAR.key();
						if(varName.startsWith(which.toUpperCase()+"_SCRIPTABLEVARS_"))
							varName=varName.substring((which+"_SCRIPTABLEVARS_").length());
						setVar(which,varName,VAR.xml());
					}
				}
				break;
			}
			case 40: // MPM2I2M
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"Cccr");
					if(tt==null)
						return null;
				}
				final String arg1=tt[1];
				final Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if(E instanceof MOB)
				{
					final String arg2=tt[2];
					final String arg3=tt[3];
					final CagedAnimal caged=(CagedAnimal)CMClass.getItem("GenCaged");
					if(caged!=null)
					{
						((Item)caged).basePhyStats().setAbility(1);
						((Item)caged).recoverPhyStats();
					}
					if((caged!=null)&&caged.cageMe((MOB)E)&&(lastKnownLocation!=null))
					{
						if(arg2.length()>0)
							((Item)caged).setName(arg2);
						if(arg3.length()>0)
							((Item)caged).setDisplayText(arg3);
						lastKnownLocation.addItem(caged,ItemPossessor.Expire.Player_Drop);
						((MOB)E).killMeDead(false);
					}
				}
				else
				if(E instanceof CagedAnimal)
				{
					final MOB M=((CagedAnimal)E).unCageMe();
					if((M!=null)&&(lastKnownLocation!=null))
					{
						M.bringToLife(lastKnownLocation,true);
						((Item)E).destroy();
					}
				}
				else
					logError(scripted,"MPM2I2M","RunTime",arg1+" is not a mob or a caged item.");
				break;
			}
			case 28: // mpdamage
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"Ccccr");
					if(tt==null)
						return null;
				}
				final Environmental newTarget=getArgumentItem(tt[1],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				final String arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]);
				final String arg3=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[3]);
				final String arg4=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[4]);
				if((newTarget!=null)&&(arg2.length()>0))
				{
					if(newTarget instanceof MOB)
					{
						final MOB deadM=(MOB)newTarget;
						MOB killerM=(MOB)newTarget;
						if(arg4.equalsIgnoreCase("MEKILL")||arg4.equalsIgnoreCase("ME"))
							killerM=monster;
						final int min=CMath.s_int(arg2.trim());
						int max=CMath.s_int(arg3.trim());
						if(max<min)
							max=min;
						if(min>0)
						{
							int dmg=(max==min)?min:CMLib.dice().roll(1,max-min,min);
							if((dmg>=deadM.curState().getHitPoints())
							&&(!arg4.equalsIgnoreCase("KILL"))
							&&(!arg4.equalsIgnoreCase("MEKILL")))
								dmg=deadM.curState().getHitPoints()-1;
							if(dmg>0)
								CMLib.combat().postDamage(killerM,deadM,null,dmg,CMMsg.MASK_ALWAYS|CMMsg.TYP_CAST_SPELL,-1,null);
						}
					}
					else
					if(newTarget instanceof Item)
					{
						final Item E=(Item)newTarget;
						final int min=CMath.s_int(arg2.trim());
						int max=CMath.s_int(arg3.trim());
						if(max<min)
							max=min;
						if(min>0)
						{
							int dmg=(max==min)?min:CMLib.dice().roll(1,max-min,min);
							boolean destroy=false;
							if(E.subjectToWearAndTear())
							{
								if((dmg>=E.usesRemaining())&&(!arg4.equalsIgnoreCase("kill")))
									dmg=E.usesRemaining()-1;
								if(dmg>0)
									E.setUsesRemaining(E.usesRemaining()-dmg);
								if(E.usesRemaining()<=0)
									destroy=true;
							}
							else
							if(arg4.equalsIgnoreCase("kill"))
								destroy=true;
							if(destroy)
							{
								if(lastKnownLocation!=null)
									lastKnownLocation.showHappens(CMMsg.MSG_OK_VISUAL,L("@x1 is destroyed!",E.name()));
								E.destroy();
							}
						}
					}
				}
				break;
			}
			case 78: // mpheal
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"Cccr");
					if(tt==null)
						return null;
				}
				final Environmental newTarget=getArgumentItem(tt[1],source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				final String arg2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]);
				final String arg3=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[3]);
				if((newTarget!=null)&&(arg2.length()>0))
				{
					if(newTarget instanceof MOB)
					{
						final MOB healedM=(MOB)newTarget;
						final MOB healerM=(MOB)newTarget;
						final int min=CMath.s_int(arg2.trim());
						int max=CMath.s_int(arg3.trim());
						if(max<min)
							max=min;
						if(min>0)
						{
							final int amt=(max==min)?min:CMLib.dice().roll(1,max-min,min);
							if(amt>0)
								CMLib.combat().postHealing(healerM,healedM,null,amt,CMMsg.MASK_ALWAYS|CMMsg.TYP_CAST_SPELL,null);
						}
					}
					else
					if(newTarget instanceof Item)
					{
						final Item E=(Item)newTarget;
						final int min=CMath.s_int(arg2.trim());
						int max=CMath.s_int(arg3.trim());
						if(max<min)
							max=min;
						if(min>0)
						{
							final int amt=(max==min)?min:CMLib.dice().roll(1,max-min,min);
							if(E.subjectToWearAndTear())
							{
								E.setUsesRemaining(E.usesRemaining()+amt);
								if(E.usesRemaining()>100)
									E.setUsesRemaining(100);
							}
						}
					}
				}
				break;
			}
			case 29: // mptrackto
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"Cr");
					if(tt==null)
						return null;
				}
				final String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[1]);
				final Ability A=CMClass.getAbility("Skill_Track");
				if(A!=null)
				{
					altStatusTickable=A;
					A.invoke(monster,CMParms.parse(arg1),null,true,0);
					altStatusTickable=null;
				}
				break;
			}
			case 53: // mpwalkto
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"Cr");
					if(tt==null)
						return null;
				}
				final String arg1=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[1]);
				final Ability A=CMClass.getAbility("Skill_Track");
				if(A!=null)
				{
					altStatusTickable=A;
					A.invoke(monster,CMParms.parse(arg1+" LANDONLY"),null,true,0);
					altStatusTickable=null;
				}
				break;
			}
			case 21: //MPENDQUEST
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"Cr");
					if(tt==null)
						return null;
				}
				final String q=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[1].trim());
				final Quest Q=getQuest(q);
				if(Q!=null)
					Q.stopQuest();
				else
				if((tt[1].length()>0)&&(defaultQuestName!=null)&&(defaultQuestName.length()>0))
				{
					final PhysicalAgent newTarget=getArgumentMOB(tt[1].trim(),source,monster,target,primaryItem,secondaryItem,msg,tmp);
					if(newTarget==null)
						logError(scripted,"MPENDQUEST","Unknown","Quest or MOB: "+s);
					else
					{
						for(int i=newTarget.numScripts()-1;i>=0;i--)
						{
							final ScriptingEngine S=newTarget.fetchScript(i);
							if((S!=null)
							&&(S.defaultQuestName()!=null)
							&&(S.defaultQuestName().equalsIgnoreCase(defaultQuestName)))
								newTarget.delScript(S);
						}
					}
				}
				else
					logError(scripted,"MPENDQUEST","Unknown","Quest: "+s);
				break;
			}
			case 69: // MPSTEPQUEST
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"Cr");
					if(tt==null)
						return null;
				}
				final String qName=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[1].trim());
				final Quest Q=getQuest(qName);
				if(Q!=null)
					Q.stepQuest();
				else
					logError(scripted,"MPSTEPQUEST","Unknown","Quest: "+s);
				break;
			}
			case 23: //MPSTARTQUEST
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"Cr");
					if(tt==null)
						return null;
				}
				final String qName=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[1].trim());
				final Quest Q=getQuest(qName);
				if(Q!=null)
					Q.startQuest();
				else
					logError(scripted,"MPSTARTQUEST","Unknown","Quest: "+s);
				break;
			}
			case 64: //MPLOADQUESTOBJ
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"Cccr");
					if(tt==null)
						return null;
				}
				final String questName=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[1].trim());
				final Quest Q=getQuest(questName);
				if(Q==null)
				{
					logError(scripted,"MPLOADQUESTOBJ","Unknown","Quest: "+questName);
					break;
				}
				final Object O=Q.getDesignatedObject(varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]));
				if(O==null)
				{
					logError(scripted,"MPLOADQUESTOBJ","Unknown","Unknown var "+tt[2]+" for Quest: "+questName);
					break;
				}
				final String varArg=tt[3];
				if((varArg.length()!=2)||(!varArg.startsWith("$")))
				{
					logError(scripted,"MPLOADQUESTOBJ","Syntax","Invalid argument var: "+varArg+" for "+scripted.Name());
					break;
				}

				final char c=varArg.charAt(1);
				if(Character.isDigit(c))
					tmp[CMath.s_int(Character.toString(c))]=O;
				else
				switch(c)
				{
				case 'N':
				case 'n':
					if (O instanceof MOB)
						source = (MOB) O;
					break;
				case 'I':
				case 'i':
					if (O instanceof PhysicalAgent)
						scripted = (PhysicalAgent) O;
					if (O instanceof MOB)
						monster = (MOB) O;
					break;
				case 'B':
				case 'b':
					if (O instanceof Environmental)
						lastLoaded = (Environmental) O;
					break;
				case 'T':
				case 't':
					if (O instanceof Environmental)
						target = (Environmental) O;
					break;
				case 'O':
				case 'o':
					if (O instanceof Item)
						primaryItem = (Item) O;
					break;
				case 'P':
				case 'p':
					if (O instanceof Item)
						secondaryItem = (Item) O;
					break;
				case 'd':
				case 'D':
					if (O instanceof Room)
						lastKnownLocation = (Room) O;
					break;
				default:
					logError(scripted, "MPLOADQUESTOBJ", "Syntax", "Invalid argument var: " + varArg + " for " + scripted.Name());
					break;
				}
				break;
			}
			case 22: //MPQUESTWIN
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"Ccr");
					if(tt==null)
						return null;
				}
				String whoName=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[1]);
				MOB M=null;
				if(lastKnownLocation!=null)
					M=lastKnownLocation.fetchInhabitant(whoName);
				if(M==null)
					M=CMLib.players().getPlayer(whoName);
				if(M!=null)
					whoName=M.Name();
				if(whoName.length()>0)
				{
					final Quest Q=getQuest(tt[2]);
					if(Q!=null)
					{
						if(M!=null)
							CMLib.achievements().possiblyBumpAchievement(M, AchievementLibrary.Event.QUESTOR, 1, Q);
						Q.declareWinner(whoName);
						CMLib.players().bumpPrideStat(M,AccountStats.PrideStat.QUESTS_COMPLETED, 1);
					}
					else
						logError(scripted,"MPQUESTWIN","Unknown","Quest: "+s);
				}
				break;
			}
			case 24: // MPCALLFUNC
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"Ccr");
					if(tt==null)
						return null;
				}
				final String named=tt[1];
				final String parms=tt[2].trim();
				boolean found=false;
				final List<DVector> scripts=getScripts();
				for(int v=0;v<scripts.size();v++)
				{
					final DVector script2=scripts.get(v);
					if(script2.size()<1)
						continue;
					final String trigger=((String)script2.elementAt(0,1)).toUpperCase().trim();
					final String[] ttrigger=(String[])script2.elementAt(0,2);
					if(getTriggerCode(trigger,ttrigger)==17) // function_prog
					{
						final String fnamed=CMParms.getCleanBit(trigger,1);
						if(fnamed.equalsIgnoreCase(named))
						{
							found=true;
							execute(scripted,
									source,
									target,
									monster,
									primaryItem,
									secondaryItem,
									script2,
									varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,parms),
									tmp);
							break;
						}
					}
				}
				if(!found)
					logError(scripted,"MPCALLFUNC","Unknown","Function: "+named);
				break;
			}
			case 27: // MPWHILE
			{
				if(tt==null)
				{
					final ArrayList<String> V=new ArrayList<String>();
					V.add("MPWHILE");
					String conditionStr=(s.substring(7).trim());
					if(!conditionStr.startsWith("("))
					{
						logError(scripted,"MPWHILE","Syntax"," NO Starting (: "+s);
						break;
					}
					conditionStr=conditionStr.substring(1).trim();
					int x=-1;
					int depth=0;
					for(int i=0;i<conditionStr.length();i++)
					{
						if(conditionStr.charAt(i)=='(')
							depth++;
						else
						if((conditionStr.charAt(i)==')')&&((--depth)<0))
						{
							x=i;
							break;
						}
					}
					if(x<0)
					{
						logError(scripted,"MPWHILE","Syntax"," no closing ')': "+s);
						break;
					}
					final String DO=conditionStr.substring(x+1).trim();
					conditionStr=conditionStr.substring(0,x);
					try
					{
						final String[] EVAL=parseEval(conditionStr);
						V.add("(");
						V.addAll(Arrays.asList(EVAL));
						V.add(")");
						V.add(DO);
						tt=CMParms.toStringArray(V);
						script.setElementAt(si,2,tt);
					}
					catch(final Exception e)
					{
						logError(scripted,"MPWHILE","Syntax",e.getMessage());
						break;
					}
					if(tt==null)
						return null;
				}
				int evalEnd=2;
				int depth=0;
				while((evalEnd<tt.length)&&((!tt[evalEnd].equals(")"))||(depth>0)))
				{
					if(tt[evalEnd].equals("("))
						depth++;
					else
					if(tt[evalEnd].equals(")"))
						depth--;
					evalEnd++;
				}
				if(evalEnd==tt.length)
				{
					logError(scripted,"MPWHILE","Syntax"," no closing ')': "+s);
					break;
				}
				final String[] EVAL=new String[evalEnd-2];
				for(int y=2;y<evalEnd;y++)
					EVAL[y-2]=tt[y];
				String DO=tt[evalEnd+1];
				String[] DOT=null;
				final int doLen=(tt.length-evalEnd)-1;
				if(doLen>1)
				{
					DOT=new String[doLen];
					for(int y=0;y<DOT.length;y++)
					{
						DOT[y]=tt[evalEnd+y+1];
						if(y>0)
							DO+=" "+tt[evalEnd+y+1];
					}
				}
				final String[][] EVALO={EVAL};
				final DVector vscript=new DVector(2);
				vscript.addElement("FUNCTION_PROG MPWHILE_"+Math.random(),null);
				vscript.addElement(DO,DOT);
				final long time=System.currentTimeMillis();
				while((eval(scripted,source,target,monster,primaryItem,secondaryItem,msg,tmp,EVALO,0))
				&&((System.currentTimeMillis()-time)<4000))
					execute(scripted,source,target,monster,primaryItem,secondaryItem,vscript,msg,tmp);
				if(vscript.elementAt(1,2)!=DOT)
				{
					final int oldDotLen=(DOT==null)?1:DOT.length;
					final String[] newDOT=(String[])vscript.elementAt(1,2);
					final String[] newTT=new String[tt.length-oldDotLen+newDOT.length];
					int end=0;
					for(end=0;end<tt.length-oldDotLen;end++)
						newTT[end]=tt[end];
					for(int y=0;y<newDOT.length;y++)
						newTT[end+y]=newDOT[y];
					tt=newTT;
					script.setElementAt(si,2,tt);
				}
				if(EVALO[0]!=EVAL)
				{
					final Vector<String> lazyV=new Vector<String>();
					lazyV.addElement("MPWHILE");
					lazyV.addElement("(");
					final String[] newEVAL=EVALO[0];
					for (final String element : newEVAL)
						lazyV.addElement(element);
					for(int i=evalEnd;i<tt.length;i++)
						lazyV.addElement(tt[i]);
					tt=CMParms.toStringArray(lazyV);
					script.setElementAt(si,2,tt);
				}
				if((System.currentTimeMillis()-time)>=4000)
				{
					logError(scripted,"MPWHILE","RunTime","4 second limit exceeded: "+s);
					break;
				}
				break;
			}
			case 26: // MPALARM
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"Ccp");
					if(tt==null)
						return null;
				}
				final String time=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[1]);
				final String parms=tt[2].trim();
				if(CMath.s_int(time.trim())<=0)
				{
					logError(scripted,"MPALARM","Syntax","Bad time "+time);
					break;
				}
				if(parms.length()==0)
				{
					logError(scripted,"MPALARM","Syntax","No command!");
					break;
				}
				final DVector vscript=new DVector(2);
				vscript.addElement("FUNCTION_PROG ALARM_"+time+Math.random(),null);
				vscript.addElement(parms,null);
				prequeResponse(scripted,source,target,monster,primaryItem,secondaryItem,vscript,CMath.s_int(time.trim()),msg);
				break;
			}
			case 37: // mpenable
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"Ccccp");
					if(tt==null)
						return null;
				}
				final Environmental newTarget=getArgumentMOB(tt[1],source,monster,target,primaryItem,secondaryItem,msg,tmp);
				final String cast=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]);
				String p2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[3]);
				final String m2=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[4]);
				Ability A=null;
				if(cast!=null)
				{
					if(newTarget instanceof MOB)
						A=((MOB)newTarget).fetchAbility(cast);
					if(A==null)
						A=CMClass.getAbility(cast);
					if(A==null)
					{
						final ExpertiseLibrary.ExpertiseDefinition D=CMLib.expertises().findDefinition(cast,false);
						if(D==null)
							logError(scripted,"MPENABLE","Syntax","Unknown skill/expertise: "+cast);
						else
						if((newTarget!=null)&&(newTarget instanceof MOB))
							((MOB)newTarget).addExpertise(D.ID());
					}
				}
				if((newTarget!=null)
				&&(A!=null)
				&&(newTarget instanceof MOB))
				{
					if(!((MOB)newTarget).isMonster())
						Log.sysOut("Scripting",newTarget.Name()+" was MPENABLED with "+A.Name());
					if(p2.trim().startsWith("++"))
						p2=""+(CMath.s_int(p2.trim().substring(2))+A.proficiency());
					else
					if(p2.trim().startsWith("--"))
						p2=""+(A.proficiency()-CMath.s_int(p2.trim().substring(2)));
					A.setProficiency(CMath.s_int(p2.trim()));
					A.setMiscText(m2);
					if(((MOB)newTarget).fetchAbility(A.ID())==null)
					{
						((MOB)newTarget).addAbility(A);
						A.autoInvocation((MOB)newTarget, false);
					}
				}
				break;
			}
			case 38: // mpdisable
			{
				if(tt==null)
				{
					tt=parseBits(script,si,"Ccr");
					if(tt==null)
						return null;
				}
				final Environmental newTarget=getArgumentMOB(tt[1],source,monster,target,primaryItem,secondaryItem,msg,tmp);
				final String cast=varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,tt[2]);
				if((newTarget!=null)&&(newTarget instanceof MOB))
				{
					final Ability A=((MOB)newTarget).findAbility(cast);
					if(A!=null)
						((MOB)newTarget).delAbility(A);
					if((!((MOB)newTarget).isMonster())&&(A!=null))
						Log.sysOut("Scripting",newTarget.Name()+" was MPDISABLED with "+A.Name());
					final ExpertiseLibrary.ExpertiseDefinition D=CMLib.expertises().findDefinition(cast,false);
					if((newTarget instanceof MOB)&&(D!=null))
						((MOB)newTarget).delExpertise(D.ID());
				}
				break;
			}
			default:
				if(cmd.length()>0)
				{
					final Vector<String> V=CMParms.parse(varify(source,target,scripted,monster,primaryItem,secondaryItem,msg,tmp,s));
					if((V.size()>0)&&(monster!=null))
						monster.doCommand(V,MUDCmdProcessor.METAFLAG_MPFORCED);
				}
				break;
			}
		}
		tickStatus=Tickable.STATUS_END;
		return null;
	}

	protected static final Vector<DVector> empty=new ReadOnlyVector<DVector>();

	@Override
	public String getScriptResourceKey()
	{
		return scriptKey;
	}

	protected List<DVector> getScripts()
	{
		if(CMSecurity.isDisabled(CMSecurity.DisFlag.SCRIPTABLE)||CMSecurity.isDisabled(CMSecurity.DisFlag.SCRIPTING))
			return empty;
		List scripts=(List)Resources.getResource(getScriptResourceKey());
		if(scripts==null)
		{
			String scr=getScript();
			scr=CMStrings.replaceAll(scr,"`","'");
			scripts=parseScripts(scr);
			Resources.submitResource(getScriptResourceKey(),scripts);
		}
		return scripts;
	}

	protected boolean match(String str, String patt)
	{
		if(patt.trim().equalsIgnoreCase("ALL"))
			return true;
		if(patt.length()==0)
			return true;
		if(str.length()==0)
			return false;
		if(str.equalsIgnoreCase(patt))
			return true;
		return false;
	}

	private Item makeCheapItem(Environmental E)
	{
		Item product=null;
		if(E instanceof Item)
			product=(Item)E;
		else
		{
			product=CMClass.getItem("StdItem");
			product.setName(E.Name());
			product.setDisplayText(E.displayText());
			product.setDescription(E.description());
			if(E instanceof Physical)
				product.setBasePhyStats((PhyStats)((Physical)E).basePhyStats().copyOf());
			product.recoverPhyStats();
		}
		return product;
	}

	@Override
	public boolean okMessage(Environmental host, CMMsg msg)
	{
		if((!(host instanceof PhysicalAgent))||(msg.source()==null)||(recurseCounter.get()>2))
			return true;

		try 
		{
			// atomic recurse counter
			recurseCounter.addAndGet(1);
	
			final PhysicalAgent affecting = (PhysicalAgent)host;
	
			final List<DVector> scripts=getScripts();
			DVector script=null;
			boolean tryIt=false;
			String trigger=null;
			String[] t=null;
			int triggerCode=0;
			String str=null;
			for(int v=0;v<scripts.size();v++)
			{
				tryIt=false;
				script=scripts.get(v);
				if(script.size()<1)
					continue;
	
				trigger=((String)script.elementAt(0,1)).toUpperCase().trim();
				t=(String[])script.elementAt(0,2);
				triggerCode=getTriggerCode(trigger,t);
				switch(triggerCode)
				{
				case 42: // cnclmsg_prog
					if(canTrigger(42))
					{
						if(t==null)
							t=parseBits(script,0,"CCT");
						if(t!=null)
						{
							final String command=t[1];
							boolean chk=false;
							final int x=command.indexOf('=');
							if(x>0)
							{
								chk=true;
								boolean minorOnly = false;
								boolean majorOnly = false;
								for(int i=0;i<x;i++)
								{
									switch(command.charAt(i))
									{
									case 'S':
										if(majorOnly)
											chk=chk&&msg.isSourceMajor(command.substring(x+1)); 
										else
										if(minorOnly)
											chk=chk&&msg.isSourceMinor(command.substring(x+1)); 
										else
											chk=chk&&msg.isSource(command.substring(x+1)); 
										break;
									case 'T': 
										if(majorOnly)
											chk=chk&&msg.isTargetMajor(command.substring(x+1)); 
										else
										if(minorOnly)
											chk=chk&&msg.isTargetMinor(command.substring(x+1)); 
										else
											chk=chk&&msg.isTarget(command.substring(x+1)); 
										break;
									case 'O': 
										if(majorOnly)
											chk=chk&&msg.isOthersMajor(command.substring(x+1)); 
										else
										if(minorOnly)
											chk=chk&&msg.isOthersMinor(command.substring(x+1)); 
										else
											chk=chk&&msg.isOthers(command.substring(x+1)); 
										break;
									case '<':
										minorOnly=true;
										majorOnly=false;
										break;
									case '>':
										majorOnly=true;
										minorOnly=false;
										break;
									case '?':
										majorOnly=false;
										minorOnly=false;
										break;
									default: 
										chk=false; 
										break;
									}
								}
							}
							else
							if(command.startsWith(">"))
							{
								String cmd=command.substring(1);
								chk=msg.isSourceMajor(cmd)||msg.isTargetMajor(cmd)||msg.isOthersMajor(cmd);
							}
							else
							if(command.startsWith("<"))
							{
								String cmd=command.substring(1);
								chk=msg.isSourceMinor(cmd)||msg.isTargetMinor(cmd)||msg.isOthersMinor(cmd);
							}
							else
							if(command.startsWith("?"))
							{
								String cmd=command.substring(1);
								chk=msg.isSource(cmd)||msg.isTarget(cmd)||msg.isOthers(cmd);
							}
							else
								chk=msg.isSource(command)||msg.isTarget(command)||msg.isOthers(command);
							if(chk)
							{
								str="";
								if((msg.source().session()!=null)&&(msg.source().session().getPreviousCMD()!=null))
									str=" "+CMParms.combine(msg.source().session().getPreviousCMD(),0).toUpperCase()+" ";
								if((t[2].length()==0)||(t[2].equals("ALL")))
									tryIt=true;
								else
								if((t[2].equals("P"))&&(t.length>3))
								{
									if(match(str.trim(),t[3]))
										tryIt=true;
								}
								else
								for(int i=2;i<t.length;i++)
								{
									if(str.indexOf(" "+t[i]+" ")>=0)
									{
										str=(t[i].trim()+" "+str.trim()).trim();
										tryIt=true;
										break;
									}
								}
							}
						}
					}
					break;
				}
				if(tryIt)
				{
					final MOB monster=getMakeMOB(affecting);
					if(lastKnownLocation==null)
						lastKnownLocation=msg.source().location();
					if((monster==null)||(monster.amDead())||(lastKnownLocation==null))
						return true;
					final Item defaultItem=(affecting instanceof Item)?(Item)affecting:null;
					Item Tool=null;
					if(msg.tool() instanceof Item)
						Tool=(Item)msg.tool();
					if(Tool==null)
						Tool=defaultItem;
					String resp=null;
					if(msg.target() instanceof MOB)
						resp=execute(affecting,msg.source(),msg.target(),monster,Tool,defaultItem,script,str,newObjs());
					else
					if(msg.target() instanceof Item)
						resp=execute(affecting,msg.source(),msg.target(),monster,Tool,(Item)msg.target(),script,str,newObjs());
					else
						resp=execute(affecting,msg.source(),msg.target(),monster,Tool,defaultItem,script,str,newObjs());
					if((resp!=null)&&(resp.equalsIgnoreCase("CANCEL")))
						return false;
				}
			}
		}
		finally 
		{ 
			recurseCounter.addAndGet(-1); 
		}
		return true;
	}

	protected String standardTriggerCheck(DVector script, String[] t, Environmental E)
	{
		if(E==null)
			return null;
		if(t==null)
			t=parseBits(script,0,"CT");
		final String NAME=E.Name().toUpperCase();
		if((t[1].length()==0)
		||(t[1].equals("ALL"))
		||(t[1].equals("P")
			&&(t.length==3)
			&&((t[2].equalsIgnoreCase(NAME))
				||(t[2].equalsIgnoreCase("ALL")))))
			return t[1];
		for(int i=1;i<t.length;i++)
		{
			if(t[i].equals("P") && (i < t.length-1))
			{
				if( t[i+1].equalsIgnoreCase(NAME)
				|| t[i+1].equalsIgnoreCase("ALL"))
					return t[i];
				i++;
			}
			else
			if(((" "+NAME+" ").indexOf(" "+t[i]+" ")>=0)
			||(E.ID().equalsIgnoreCase(t[i]))
			||(t[i].equalsIgnoreCase("ALL")))
				return t[i];
		}
		return null;

	}

	@Override
	public void executeMsg(Environmental host, CMMsg msg)
	{
		if((!(host instanceof PhysicalAgent))||(msg.source()==null)||(recurseCounter.get() > 2))
			return;

		try 
		{ 
			// atomic recurse counter
			recurseCounter.addAndGet(1);
	
			final PhysicalAgent affecting = (PhysicalAgent)host;
	
			final MOB monster=getMakeMOB(affecting);
	
			if(lastKnownLocation==null)
				lastKnownLocation=msg.source().location();
			if((monster==null)||(monster.amDead())||(lastKnownLocation==null))
				return;
	
			final Item defaultItem=(affecting instanceof Item)?(Item)affecting:null;
			MOB eventMob=monster;
			if((defaultItem!=null)&&(defaultItem.owner() instanceof MOB))
				eventMob=(MOB)defaultItem.owner();
	
			final List<DVector> scripts=getScripts();
	
			if(msg.amITarget(eventMob)
			&&(!msg.amISource(monster))
			&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
			&&(msg.source()!=monster))
				lastToHurtMe=msg.source();
			DVector script=null;
			String trigger=null;
			String[] t=null;
			for(int v=0;v<scripts.size();v++)
			{
				script=scripts.get(v);
				if(script.size()<1)
					continue;
	
				trigger=((String)script.elementAt(0,1)).toUpperCase().trim();
				t=(String[])script.elementAt(0,2);
				final int triggerCode=getTriggerCode(trigger,t);
				int targetMinorTrigger=-1;
				switch(triggerCode)
				{
				case 1: // greet_prog
					if((msg.targetMinor()==CMMsg.TYP_ENTER)
					&&(msg.amITarget(lastKnownLocation))
					&&(!msg.amISource(eventMob))
					&&((!(affecting instanceof MOB)) || isFreeToBeTriggered(monster))
					&&canTrigger(1)
					&&((!(affecting instanceof MOB))||CMLib.flags().canSenseEnteringLeaving(msg.source(),(MOB)affecting)))
					{
						if(t==null)
							t=parseBits(script,0,"CR");
						if(t!=null)
						{
							final int prcnt=CMath.s_int(t[1]);
							if(CMLib.dice().rollPercentage()<prcnt)
							{
								enqueResponse(affecting,msg.source(),monster,monster,defaultItem,null,script,1,null);
								return;
							}
						}
					}
					break;
				case 45: // arrive_prog
					if(((msg.targetMinor()==CMMsg.TYP_ENTER)||(msg.sourceMinor()==CMMsg.TYP_LIFE))
					&&(msg.amISource(eventMob))
					&&((!(affecting instanceof MOB)) || isFreeToBeTriggered(monster))
					&&canTrigger(45))
					{
						if(t==null)
							t=parseBits(script,0,"CR");
						if(t!=null)
						{
							final int prcnt=CMath.s_int(t[1]);
							if(CMLib.dice().rollPercentage()<prcnt)
							{
								enqueResponse(affecting,msg.source(),monster,monster,defaultItem,null,script,1,null);
								return;
							}
						}
					}
					break;
				case 2: // all_greet_prog
					if((msg.targetMinor()==CMMsg.TYP_ENTER)&&canTrigger(2)
					&&(msg.amITarget(lastKnownLocation))
					&&(!msg.amISource(eventMob))
					&&((!(affecting instanceof MOB)) || isFreeToBeTriggered(monster))
					&&((!(affecting instanceof MOB)) ||CMLib.flags().canActAtAll(monster)))
					{
						if(t==null)
							t=parseBits(script,0,"CR");
						if(t!=null)
						{
							final int prcnt=CMath.s_int(t[1]);
							if(CMLib.dice().rollPercentage()<prcnt)
							{
								enqueResponse(affecting,msg.source(),monster,monster,defaultItem,null,script,1,null);
								return;
							}
						}
					}
					break;
				case 3: // speech_prog
					if(((msg.sourceMinor()==CMMsg.TYP_SPEAK)||(msg.targetMinor()==CMMsg.TYP_SPEAK))&&canTrigger(3)
					&&(!msg.amISource(monster))
					&&(!msg.othersMajor(CMMsg.MASK_CHANNEL))
					&&(((msg.othersMessage()!=null)&&((msg.tool()==null)||(!(msg.tool() instanceof Ability))||((((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES)!=Ability.ACODE_LANGUAGE)))
					   ||((msg.target()==monster)&&(msg.targetMessage()!=null)&&(msg.tool()==null)))
					&&((!(affecting instanceof MOB)) || isFreeToBeTriggered(monster)))
					{
						if(t==null)
						{
							t=parseBits(script,0,"CT");
							for(int i=0;i<t.length;i++)
							{
								if(t[i]!=null)
									t[i]=t[i].replace('`', '\'');
							}
						}
						String str=null;
						if(msg.othersMessage() != null)
							str=CMStrings.getSayFromMessage(msg.othersMessage().toUpperCase());
						else
						if(msg.targetMessage() != null)
							str=CMStrings.getSayFromMessage(msg.targetMessage().toUpperCase());
						if(str != null)
						{
							str=(" "+str.replace('`', '\'')+" ").toUpperCase();
							str=CMStrings.removeColors(str);
							str=CMStrings.replaceAll(str,"\n\r"," ");
							if((t[1].length()==0)||(t[1].equals("ALL")))
							{
								enqueResponse(affecting,msg.source(),msg.target(),monster,defaultItem,null,script,1,str);
								return;
							}
							else
							if((t[1].equals("P"))&&(t.length>2))
							{
								if(match(str.trim(),t[2]))
								{
									enqueResponse(affecting,msg.source(),msg.target(),monster,defaultItem,null,script,1,str);
									return;
								}
							}
							else
							for(int i=1;i<t.length;i++)
							{
								final int x=str.indexOf(" "+t[i]+" ");
								if(x>=0)
								{
									enqueResponse(affecting,msg.source(),msg.target(),monster,defaultItem,null,script,1,str.substring(x).trim());
									return;
								}
							}
						}
					}
					break;
				case 4: // give_prog
					if((msg.targetMinor()==CMMsg.TYP_GIVE)
					&&canTrigger(4)
					&&((msg.amITarget(monster))
						||(msg.tool()==affecting)
						||(affecting instanceof Room)
						||(affecting instanceof Area))
					&&(!msg.amISource(monster))
					&&(msg.tool() instanceof Item)
					&&((!(affecting instanceof MOB)) || isFreeToBeTriggered(monster)))
					{
						final String check=standardTriggerCheck(script,t,msg.tool());
						if(check!=null)
						{
							if(lastMsg==msg)
								break;
							lastMsg=msg;
							enqueResponse(affecting,msg.source(),monster,monster,(Item)msg.tool(),defaultItem,script,1,check);
							return;
						}
					}
					break;
				case 40: // llook_prog
					if((msg.targetMinor()==CMMsg.TYP_EXAMINE)&&canTrigger(40)
					&&(!msg.amISource(monster))
					&&((!(affecting instanceof MOB)) || isFreeToBeTriggered(monster)))
					{
						final String check=standardTriggerCheck(script,t,msg.target());
						if(check!=null)
						{
							enqueResponse(affecting,msg.source(),msg.target(),monster,(Item)msg.target(),defaultItem,script,1,check);
							return;
						}
					}
					break;
				case 41: // execmsg_prog
					if(canTrigger(41))
					{
						if(t==null)
							t=parseBits(script,0,"CCT");
						if(t!=null)
						{
							final String command=t[1];
							boolean chk=false;
							final int x=command.indexOf('=');
							if(x>0)
							{
								chk=true;
								boolean minorOnly = false;
								boolean majorOnly = false;
								for(int i=0;i<x;i++)
								{
									switch(command.charAt(i))
									{
										case 'S':
											if(majorOnly)
												chk=chk&&msg.isSourceMajor(command.substring(x+1)); 
											else
											if(minorOnly)
												chk=chk&&msg.isSourceMinor(command.substring(x+1)); 
											else
												chk=chk&&msg.isSource(command.substring(x+1)); 
											break;
										case 'T': 
											if(majorOnly)
												chk=chk&&msg.isTargetMajor(command.substring(x+1)); 
											else
											if(minorOnly)
												chk=chk&&msg.isTargetMinor(command.substring(x+1)); 
											else
												chk=chk&&msg.isTarget(command.substring(x+1)); 
											break;
										case 'O': 
											if(majorOnly)
												chk=chk&&msg.isOthersMajor(command.substring(x+1)); 
											else
											if(minorOnly)
												chk=chk&&msg.isOthersMinor(command.substring(x+1)); 
											else
												chk=chk&&msg.isOthers(command.substring(x+1)); 
											break;
										case '<':
											minorOnly=true;
											majorOnly=false;
											break;
										case '>':
											majorOnly=true;
											minorOnly=false;
											break;
										case '?':
											majorOnly=false;
											minorOnly=false;
											break;
										default: 
											chk=false; 
											break;
									}
								}
							}
							else
							if(command.startsWith(">"))
							{
								String cmd=command.substring(1);
								chk=msg.isSourceMajor(cmd)||msg.isTargetMajor(cmd)||msg.isOthersMajor(cmd);
							}
							else
							if(command.startsWith("<"))
							{
								String cmd=command.substring(1);
								chk=msg.isSourceMinor(cmd)||msg.isTargetMinor(cmd)||msg.isOthersMinor(cmd);
							}
							else
							if(command.startsWith("?"))
							{
								String cmd=command.substring(1);
								chk=msg.isSource(cmd)||msg.isTarget(cmd)||msg.isOthers(cmd);
							}
							else
								chk=msg.isSource(command)||msg.isTarget(command)||msg.isOthers(command);
							if(chk)
							{
								String str="";
								if((msg.source().session()!=null)&&(msg.source().session().getPreviousCMD()!=null))
									str=" "+CMParms.combine(msg.source().session().getPreviousCMD(),0).toUpperCase()+" ";
								boolean doIt=false;
								if((t[2].length()==0)||(t[2].equals("ALL")))
									doIt=true;
								else
								if((t[2].equals("P"))&&(t.length>3))
								{
									if(match(str.trim(),t[3]))
										doIt=true;
								}
								else
								for(int i=2;i<t.length;i++)
								{
									if(str.indexOf(" "+t[i]+" ")>=0)
									{
										str=(t[i].trim()+" "+str.trim()).trim();
										doIt=true;
										break;
									}
								}
								if(doIt)
								{
									Item Tool=null;
									if(msg.tool() instanceof Item)
										Tool=(Item)msg.tool();
									if(Tool==null)
										Tool=defaultItem;
									if(msg.target() instanceof MOB)
										enqueResponse(affecting,msg.source(),msg.target(),monster,Tool,defaultItem,script,1,str);
									else
									if(msg.target() instanceof Item)
										enqueResponse(affecting,msg.source(),msg.target(),monster,Tool,(Item)msg.target(),script,1,str);
									else
										enqueResponse(affecting,msg.source(),msg.target(),monster,Tool,defaultItem,script,1,str);
									return;
								}
							}
						}
					}
					break;
				case 39: // look_prog
					if((msg.targetMinor()==CMMsg.TYP_LOOK)&&canTrigger(39)
					&&(!msg.amISource(monster))
					&&((!(affecting instanceof MOB)) || isFreeToBeTriggered(monster)))
					{
						final String check=standardTriggerCheck(script,t,msg.target());
						if(check!=null)
						{
							enqueResponse(affecting,msg.source(),msg.target(),monster,defaultItem,defaultItem,script,1,check);
							return;
						}
					}
					break;
				case 20: // get_prog
					if((msg.targetMinor()==CMMsg.TYP_GET)&&canTrigger(20)
					&&(msg.amITarget(affecting)
						||(affecting instanceof Room)
						||(affecting instanceof Area)
						||(affecting instanceof MOB))
					&&(!msg.amISource(monster))
					&&(msg.target() instanceof Item)
					&&((!(affecting instanceof MOB)) || isFreeToBeTriggered(monster)))
					{
						Item checkInE=(Item)msg.target();
						if((msg.tool() instanceof Item)
						&&(((Item)msg.tool()).container()==msg.target()))
							checkInE=(Item)msg.tool();
						final String check=standardTriggerCheck(script,t,checkInE);
						if(check!=null)
						{
							if(lastMsg==msg)
								break;
							lastMsg=msg;
							enqueResponse(affecting,msg.source(),msg.target(),monster,checkInE,defaultItem,script,1,check);
							return;
						}
					}
					break;
				case 22: // drop_prog
					if((msg.targetMinor()==CMMsg.TYP_DROP)&&canTrigger(22)
					&&((msg.amITarget(affecting))
						||(affecting instanceof Room)
						||(affecting instanceof Area)
						||(affecting instanceof MOB))
					&&(!msg.amISource(monster))
					&&(msg.target() instanceof Item)
					&&((!(affecting instanceof MOB)) || isFreeToBeTriggered(monster)))
					{
						final String check=standardTriggerCheck(script,t,msg.target());
						if(check!=null)
						{
							if(lastMsg==msg)
								break;
							lastMsg=msg;
							if(msg.target() instanceof Coins)
								execute(affecting,msg.source(),msg.target(),monster,(Item)msg.target(),(Item)((Item)msg.target()).copyOf(),script,check,newObjs());
							else
								enqueResponse(affecting,msg.source(),msg.target(),monster,(Item)msg.target(),defaultItem,script,1,check);
							return;
						}
					}
					break;
				case 24: // remove_prog
					if((msg.targetMinor()==CMMsg.TYP_REMOVE)&&canTrigger(24)
					&&((msg.amITarget(affecting))
						||(affecting instanceof Room)
						||(affecting instanceof Area)
						||(affecting instanceof MOB))
					&&(!msg.amISource(monster))
					&&(msg.target() instanceof Item)
					&&((!(affecting instanceof MOB)) || isFreeToBeTriggered(monster)))
					{
						final String check=standardTriggerCheck(script,t,msg.target());
						if(check!=null)
						{
							enqueResponse(affecting,msg.source(),msg.target(),monster,(Item)msg.target(),defaultItem,script,1,check);
							return;
						}
					}
					break;
				case 34: // open_prog
					if(targetMinorTrigger<0)
						targetMinorTrigger=CMMsg.TYP_OPEN;
					//$FALL-THROUGH$
				case 35: // close_prog
					if(targetMinorTrigger<0)
						targetMinorTrigger=CMMsg.TYP_CLOSE;
					//$FALL-THROUGH$
				case 36: // lock_prog
					if(targetMinorTrigger<0)
						targetMinorTrigger=CMMsg.TYP_LOCK;
					//$FALL-THROUGH$
				case 37: // unlock_prog
				{
					if(targetMinorTrigger<0)
						targetMinorTrigger=CMMsg.TYP_UNLOCK;
					if((msg.targetMinor()==targetMinorTrigger)&&canTrigger(triggerCode)
					&&((msg.amITarget(affecting))
						||(affecting instanceof Room)
						||(affecting instanceof Area)
						||(affecting instanceof MOB))
					&&(!msg.amISource(monster))
					&&((!(affecting instanceof MOB)) || isFreeToBeTriggered(monster)))
					{
						final Item I=(msg.target() instanceof Item)?(Item)msg.target():defaultItem;
						final String check=standardTriggerCheck(script,t,msg.target());
						if(check!=null)
						{
							enqueResponse(affecting,msg.source(),msg.target(),monster,I,defaultItem,script,1,check);
							return;
						}
					}
					break;
				}
				case 25: // consume_prog
					if(((msg.targetMinor()==CMMsg.TYP_EAT)||(msg.targetMinor()==CMMsg.TYP_DRINK))
					&&((msg.amITarget(affecting))||(affecting instanceof Room)||(affecting instanceof Area)||(affecting instanceof MOB))
					&&(!msg.amISource(monster))&&canTrigger(25)
					&&(msg.target() instanceof Item)
					&&((!(affecting instanceof MOB)) || isFreeToBeTriggered(monster)))
					{
						final String check=standardTriggerCheck(script,t,msg.target());
						if(check!=null)
						{
							if((msg.target() == affecting)
							&&(affecting instanceof Food))
								execute(affecting,msg.source(),msg.target(),monster,(Item)msg.target(),(Item)((Item)msg.target()).copyOf(),script,check,newObjs());
							else
								enqueResponse(affecting,msg.source(),msg.target(),monster,(Item)msg.target(),defaultItem,script,1,check);
							return;
						}
					}
					break;
				case 21: // put_prog
					if((msg.targetMinor()==CMMsg.TYP_PUT)&&canTrigger(21)
					&&((msg.amITarget(affecting))||(affecting instanceof Room)||(affecting instanceof Area)||(affecting instanceof MOB))
					&&(msg.tool() instanceof Item)
					&&(!msg.amISource(monster))
					&&(msg.target() instanceof Item)
					&&((!(affecting instanceof MOB)) || isFreeToBeTriggered(monster)))
					{
						final String check=standardTriggerCheck(script,t,msg.target());
						if(check!=null)
						{
							if(lastMsg==msg)
								break;
							lastMsg=msg;
							if((msg.tool() instanceof Coins)&&(((Item)msg.target()).owner() instanceof Room))
								execute(affecting,msg.source(),msg.target(),monster,(Item)msg.target(),(Item)((Item)msg.target()).copyOf(),script,check,newObjs());
							else
								enqueResponse(affecting,msg.source(),msg.target(),monster,(Item)msg.target(),(Item)msg.tool(),script,1,check);
							return;
						}
					}
					break;
				case 27: // buy_prog
					if((msg.targetMinor()==CMMsg.TYP_BUY)&&canTrigger(27)
					&&((!(affecting instanceof ShopKeeper))
						||msg.amITarget(affecting))
					&&(!msg.amISource(monster))
					&&((!(affecting instanceof MOB)) || isFreeToBeTriggered(monster)))
					{
						final String check=standardTriggerCheck(script,t,msg.tool());
						if(check!=null)
						{
							final Item product=makeCheapItem(msg.tool());
							if((product instanceof Coins)
							&&(product.owner() instanceof Room))
								execute(affecting,msg.source(),monster,monster,product,(Item)product.copyOf(),script,check,newObjs());
							else
								enqueResponse(affecting,msg.source(),monster,monster,product,product,script,1,check);
							return;
						}
					}
					break;
				case 28: // sell_prog
					if((msg.targetMinor()==CMMsg.TYP_SELL)&&canTrigger(28)
					&&((msg.amITarget(affecting))||(!(affecting instanceof ShopKeeper)))
					&&(!msg.amISource(monster))
					&&((!(affecting instanceof MOB)) || isFreeToBeTriggered(monster)))
					{
						final String check=standardTriggerCheck(script,t,msg.tool());
						if(check!=null)
						{
							final Item product=makeCheapItem(msg.tool());
							if((product instanceof Coins)
							&&(product.owner() instanceof Room))
								execute(affecting,msg.source(),monster,monster,product,(Item)product.copyOf(),script,null,newObjs());
							else
								enqueResponse(affecting,msg.source(),monster,monster,product,product,script,1,check);
							return;
						}
					}
					break;
				case 23: // wear_prog
					if(((msg.targetMinor()==CMMsg.TYP_WEAR)
						||(msg.targetMinor()==CMMsg.TYP_HOLD)
						||(msg.targetMinor()==CMMsg.TYP_WIELD))
					&&((msg.amITarget(affecting))||(affecting instanceof Room)||(affecting instanceof Area)||(affecting instanceof MOB))
					&&(!msg.amISource(monster))&&canTrigger(23)
					&&(msg.target() instanceof Item)
					&&((!(affecting instanceof MOB)) || isFreeToBeTriggered(monster)))
					{
						final String check=standardTriggerCheck(script,t,msg.target());
						if(check!=null)
						{
							enqueResponse(affecting,msg.source(),monster,monster,(Item)msg.target(),defaultItem,script,1,check);
							return;
						}
					}
					break;
				case 19: // bribe_prog
					if((msg.targetMinor()==CMMsg.TYP_GIVE)
					&&(msg.amITarget(eventMob)||(!(affecting instanceof MOB)))
					&&(!msg.amISource(monster))&&canTrigger(19)
					&&(msg.tool() instanceof Coins)
					&&((!(affecting instanceof MOB)) || isFreeToBeTriggered(monster)))
					{
						if(t==null)
							t=parseBits(script,0,"CR");
						if(t!=null)
						{
							if(t[1].startsWith("ANY")||t[1].startsWith("ALL"))
								t[1]=t[1].trim();
							else
							if(!((Coins)msg.tool()).getCurrency().equals(CMLib.beanCounter().getCurrency(monster)))
								break;
							double d=0.0;
							if(CMath.isDouble(t[1]))
								d=CMath.s_double(t[1]);
							else
								d=CMath.s_int(t[1]);
							if((((Coins)msg.tool()).getTotalValue()>=d)
							||(t[1].equals("ALL"))
							||(t[1].equals("ANY")))
							{
								enqueResponse(affecting,msg.source(),monster,monster,(Item)msg.tool(),defaultItem,script,1,null);
								return;
							}
						}
					}
					break;
				case 8: // entry_prog
					if((msg.targetMinor()==CMMsg.TYP_ENTER)&&canTrigger(8)
					&&(msg.amISource(eventMob)
						||(msg.target()==affecting)
						||(msg.tool()==affecting)
						||(affecting instanceof Item))
					&&((!(affecting instanceof MOB)) || isFreeToBeTriggered(monster)))
					{
						if(t==null)
							t=parseBits(script,0,"CR");
						if(t!=null)
						{
							final int prcnt=CMath.s_int(t[1]);
							if(CMLib.dice().rollPercentage()<prcnt)
							{
								final List<ScriptableResponse> V=new XVector<ScriptableResponse>(que);
								ScriptableResponse SB=null;
								String roomID=null;
								if(msg.target()!=null)
									roomID=CMLib.map().getExtendedRoomID(CMLib.map().roomLocation(msg.target()));
								for(int q=0;q<V.size();q++)
								{
									SB=V.get(q);
									if((SB.scr==script)&&(SB.s==msg.source()))
									{
										if(que.remove(SB))
											execute(SB.h,SB.s,SB.t,SB.m,SB.pi,SB.si,SB.scr,SB.message,newObjs());
										break;
									}
								}
								enqueResponse(affecting,msg.source(),monster,monster,defaultItem,null,script,1,roomID);
								return;
							}
						}
					}
					break;
				case 9: // exit_prog
					if((msg.targetMinor()==CMMsg.TYP_LEAVE)&&canTrigger(9)
					&&(msg.amITarget(lastKnownLocation))
					&&(!msg.amISource(eventMob))
					&&((!(affecting instanceof MOB)) || isFreeToBeTriggered(monster)))
					{
						if(t==null)
							t=parseBits(script,0,"CR");
						if(t!=null)
						{
							final int prcnt=CMath.s_int(t[1]);
							if(CMLib.dice().rollPercentage()<prcnt)
							{
								final List<ScriptableResponse> V=new XVector<ScriptableResponse>(que);
								ScriptableResponse SB=null;
								String roomID=null;
								if(msg.target()!=null)
									roomID=CMLib.map().getExtendedRoomID(CMLib.map().roomLocation(msg.target()));
								for(int q=0;q<V.size();q++)
								{
									SB=V.get(q);
									if((SB.scr==script)&&(SB.s==msg.source()))
									{
										if(que.remove(SB))
											execute(SB.h,SB.s,SB.t,SB.m,SB.pi,SB.si,SB.scr,SB.message,newObjs());
										break;
									}
								}
								enqueResponse(affecting,msg.source(),monster,monster,defaultItem,null,script,1,roomID);
								return;
							}
						}
					}
					break;
				case 10: // death_prog
					if((msg.sourceMinor()==CMMsg.TYP_DEATH)&&canTrigger(10)
					&&(msg.amISource(eventMob)||(!(affecting instanceof MOB))))
					{
						if(t==null)
							t=parseBits(script,0,"C");
						final MOB ded=msg.source();
						MOB src=lastToHurtMe;
						if(msg.tool() instanceof MOB)
							src=(MOB)msg.tool();
						if((src==null)||(src.location()!=monster.location()))
							src=ded;
						execute(affecting,src,ded,ded,defaultItem,null,script,null,newObjs());
						return;
					}
					break;
				case 44: // kill_prog
					if((msg.sourceMinor()==CMMsg.TYP_DEATH)&&canTrigger(44)
					&&((msg.tool()==affecting)||(!(affecting instanceof MOB))))
					{
						if(t==null)
							t=parseBits(script,0,"C");
						final MOB ded=msg.source();
						MOB src=lastToHurtMe;
						if(msg.tool() instanceof MOB)
							src=(MOB)msg.tool();
						if((src==null)||(src.location()!=monster.location()))
							src=ded;
						execute(affecting,src,ded,ded,defaultItem,null,script,null,newObjs());
						return;
					}
					break;
				case 26: // damage_prog
					if((msg.targetMinor()==CMMsg.TYP_DAMAGE)&&canTrigger(26)
					&&(msg.amITarget(eventMob)||(msg.tool()==affecting)))
					{
						if(t==null)
							t=parseBits(script,0,"C");
						Item I=null;
						if(msg.tool() instanceof Item)
							I=(Item)msg.tool();
						execute(affecting,msg.source(),msg.target(),eventMob,defaultItem,I,script,""+msg.value(),newObjs());
						return;
					}
					break;
				case 29: // login_prog
					if(!registeredEvents.contains(Integer.valueOf(CMMsg.TYP_LOGIN)))
					{
						CMLib.map().addGlobalHandler(affecting,CMMsg.TYP_LOGIN);
						registeredEvents.add(Integer.valueOf(CMMsg.TYP_LOGIN));
					}
					if((msg.sourceMinor()==CMMsg.TYP_LOGIN)&&canTrigger(29)
					&&((!(affecting instanceof MOB)) || isFreeToBeTriggered(monster))
					&&(!CMLib.flags().isCloaked(msg.source())))
					{
						if(t==null)
							t=parseBits(script,0,"CR");
						if(t!=null)
						{
							final int prcnt=CMath.s_int(t[1]);
							if(CMLib.dice().rollPercentage()<prcnt)
							{
								enqueResponse(affecting,msg.source(),monster,monster,defaultItem,null,script,1,null);
								return;
							}
						}
					}
					break;
				case 32: // level_prog
					if(!registeredEvents.contains(Integer.valueOf(CMMsg.TYP_LEVEL)))
					{
						CMLib.map().addGlobalHandler(affecting,CMMsg.TYP_LEVEL);
						registeredEvents.add(Integer.valueOf(CMMsg.TYP_LEVEL));
					}
					if((msg.sourceMinor()==CMMsg.TYP_LEVEL)&&canTrigger(32)
					&&((!(affecting instanceof MOB)) || isFreeToBeTriggered(monster))
					&&(msg.value() > msg.source().basePhyStats().level())
					&&(!CMLib.flags().isCloaked(msg.source())))
					{
						if(t==null)
							t=parseBits(script,0,"CR");
						if(t!=null)
						{
							final int prcnt=CMath.s_int(t[1]);
							if(CMLib.dice().rollPercentage()<prcnt)
							{
								enqueResponse(affecting,msg.source(),monster,monster,defaultItem,null,script,1,null);
								return;
							}
						}
					}
					break;
				case 30: // logoff_prog
					if((msg.sourceMinor()==CMMsg.TYP_QUIT)&&canTrigger(30)
					&&((!(affecting instanceof MOB)) || isFreeToBeTriggered(monster))
					&&(!CMLib.flags().isCloaked(msg.source())))
					{
						if(t==null)
							t=parseBits(script,0,"CR");
						if(t!=null)
						{
							final int prcnt=CMath.s_int(t[1]);
							if(CMLib.dice().rollPercentage()<prcnt)
							{
								enqueResponse(affecting,msg.source(),monster,monster,defaultItem,null,script,1,null);
								return;
							}
						}
					}
					break;
				case 12: // mask_prog
				{
					if(!canTrigger(12))
						break;
				}
				//$FALL-THROUGH$
				case 18: // act_prog
					if((msg.amISource(monster))
					||((triggerCode==18)&&(!canTrigger(18))))
						break;
				//$FALL-THROUGH$
				case 43: // imask_prog
					if((triggerCode!=43)||(msg.amISource(monster)&&canTrigger(43)))
					{
						if(t==null)
						{
							t=parseBits(script,0,"CT");
							for(int i=1;i<t.length;i++)
								t[i]=CMLib.english().stripPunctuation(CMStrings.removeColors(t[i]));
						}
						boolean doIt=false;
						String str=msg.othersMessage();
						if(str==null)
							str=msg.targetMessage();
						if(str==null)
							str=msg.sourceMessage();
						if(str==null)
							break;
						str=CMLib.coffeeFilter().fullOutFilter(null,monster,msg.source(),msg.target(),msg.tool(),str,false);
						str=CMLib.english().stripPunctuation(CMStrings.removeColors(str));
						str=" "+CMStrings.replaceAll(str,"\n\r"," ").toUpperCase().trim()+" ";
						if((t[1].length()==0)||(t[1].equals("ALL")))
							doIt=true;
						else
						if((t[1].equals("P"))&&(t.length>2))
						{
							if(match(str.trim(),t[2]))
								doIt=true;
						}
						else
						for(int i=1;i<t.length;i++)
						{
							if(str.indexOf(" "+t[i]+" ")>=0)
							{
								str=t[i];
								doIt=true;
								break;
							}
						}
						if(doIt)
						{
							Item Tool=null;
							if(msg.tool() instanceof Item)
								Tool=(Item)msg.tool();
							if(Tool==null)
								Tool=defaultItem;
							if(msg.target() instanceof MOB)
								enqueResponse(affecting,msg.source(),msg.target(),monster,Tool,defaultItem,script,1,str);
							else
							if(msg.target() instanceof Item)
								enqueResponse(affecting,msg.source(),msg.target(),monster,Tool,(Item)msg.target(),script,1,str);
							else
								enqueResponse(affecting,msg.source(),msg.target(),monster,Tool,defaultItem,script,1,str);
							return;
						}
					}
					break;
				case 38: // social_prog
					if(!msg.amISource(monster)
					&&canTrigger(38)
					&&(msg.tool() instanceof Social))
					{
						if(t==null)
							t=parseBits(script,0,"CR");
						if((t!=null)
						&&((Social)msg.tool()).Name().toUpperCase().startsWith(t[1]))
						{
							final Item Tool=defaultItem;
							if(msg.target() instanceof MOB)
								enqueResponse(affecting,msg.source(),msg.target(),monster,Tool,defaultItem,script,1,msg.tool().Name());
							else
							if(msg.target() instanceof Item)
								enqueResponse(affecting,msg.source(),msg.target(),monster,Tool,(Item)msg.target(),script,1,msg.tool().Name());
							else
								enqueResponse(affecting,msg.source(),msg.target(),monster,Tool,defaultItem,script,1,msg.tool().Name());
							return;
						}
					}
					break;
				case 33: // channel_prog
					if(!registeredEvents.contains(Integer.valueOf(CMMsg.TYP_CHANNEL)))
					{
						CMLib.map().addGlobalHandler(affecting,CMMsg.TYP_CHANNEL);
						registeredEvents.add(Integer.valueOf(CMMsg.TYP_CHANNEL));
					}
					if(!msg.amISource(monster)
					&&(msg.othersMajor(CMMsg.MASK_CHANNEL))
					&&canTrigger(33))
					{
						if(t==null)
							t=parseBits(script,0,"CCT");
						boolean doIt=false;
						if(t!=null)
						{
							final String channel=t[1];
							final int channelInt=msg.othersMinor()-CMMsg.TYP_CHANNEL;
							String str=null;
							final CMChannel officialChannel=CMLib.channels().getChannel(channelInt);
							if(officialChannel==null)
								Log.errOut("Script","Unknown channel for code '"+channelInt+"': "+msg.othersMessage());
							else
							if(channel.equalsIgnoreCase(officialChannel.name()))
							{
								str=msg.sourceMessage();
								if(str==null)
									str=msg.othersMessage();
								if(str==null)
									str=msg.targetMessage();
								if(str==null)
									break;
								str=CMLib.coffeeFilter().fullOutFilter(null,monster,msg.source(),msg.target(),msg.tool(),str,false).toUpperCase().trim();
								int dex=str.indexOf("["+channel+"]");
								if(dex>0)
									str=str.substring(dex+2+channel.length()).trim();
								else
								{
									dex=str.indexOf('\'');
									final int edex=str.lastIndexOf('\'');
									if(edex>dex)
										str=str.substring(dex+1,edex);
								}
								str=" "+CMStrings.removeColors(str)+" ";
								str=CMStrings.replaceAll(str,"\n\r"," ");
								if((t[2].length()==0)||(t[2].equals("ALL")))
									doIt=true;
								else
								if(t[2].equals("P")&&(t.length>2))
								{
									if(match(str.trim(),t[3]))
										doIt=true;
								}
								else
								for(int i=2;i<t.length;i++)
								{
									if(str.indexOf(" "+t[i]+" ")>=0)
									{
										str=t[i];
										doIt=true;
										break;
									}
								}
							}
							if(doIt)
							{
								Item Tool=null;
								if(msg.tool() instanceof Item)
									Tool=(Item)msg.tool();
								if(Tool==null)
									Tool=defaultItem;
								if(msg.target() instanceof MOB)
									enqueResponse(affecting,msg.source(),msg.target(),monster,Tool,defaultItem,script,1,str);
								else
								if(msg.target() instanceof Item)
									enqueResponse(affecting,msg.source(),msg.target(),monster,Tool,(Item)msg.target(),script,1,str);
								else
									enqueResponse(affecting,msg.source(),msg.target(),monster,Tool,defaultItem,script,1,str);
								return;
							}
						}
					}
					break;
				case 31: // regmask_prog
					if(!msg.amISource(monster)&&canTrigger(31))
					{
						boolean doIt=false;
						String str=msg.othersMessage();
						if(str==null)
							str=msg.targetMessage();
						if(str==null)
							str=msg.sourceMessage();
						if(str==null)
							break;
						str=CMLib.coffeeFilter().fullOutFilter(null,monster,msg.source(),msg.target(),msg.tool(),str,false);
						if(t==null)
							t=parseBits(script,0,"Cp");
						if(t!=null)
						{
							if(CMParms.getCleanBit(t[1],0).equalsIgnoreCase("p"))
								doIt=str.trim().equals(t[1].substring(1).trim());
							else
							{
								Pattern P=patterns.get(t[1]);
								if(P==null)
								{
									P=Pattern.compile(t[1], Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
									patterns.put(t[1],P);
								}
								final Matcher M=P.matcher(str);
								doIt=M.find();
								if(doIt)
									str=str.substring(M.start()).trim();
							}
						}
						if(doIt)
						{
							Item Tool=null;
							if(msg.tool() instanceof Item)
								Tool=(Item)msg.tool();
							if(Tool==null)
								Tool=defaultItem;
							if(msg.target() instanceof MOB)
								enqueResponse(affecting,msg.source(),msg.target(),monster,Tool,defaultItem,script,1,str);
							else
							if(msg.target() instanceof Item)
								enqueResponse(affecting,msg.source(),msg.target(),monster,Tool,(Item)msg.target(),script,1,str);
							else
								enqueResponse(affecting,msg.source(),msg.target(),monster,Tool,defaultItem,script,1,str);
							return;
						}
					}
					break;
				}
			}
		}
		finally 
		{ 
			recurseCounter.addAndGet(-1); 
		}
	}

	protected int getTriggerCode(String trigger, String[] ttrigger)
	{
		Integer I=null;
		if((ttrigger!=null)&&(ttrigger.length>0))
			I=progH.get(ttrigger[0]);
		else
		{
			final int x=trigger.indexOf(' ');
			if(x<0)
				I=progH.get(trigger.toUpperCase().trim());
			else
				I=progH.get(trigger.substring(0,x).toUpperCase().trim());
		}
		if(I==null)
			return 0;
		return I.intValue();
	}

	@Override
	public MOB getMakeMOB(Tickable ticking)
	{
		MOB mob=null;
		if(ticking instanceof MOB)
		{
			mob=(MOB)ticking;
			if(!mob.amDead())
				lastKnownLocation=mob.location();
		}
		else
		if(ticking instanceof Environmental)
		{
			final Room R=CMLib.map().roomLocation((Environmental)ticking);
			if(R!=null)
				lastKnownLocation=R;

			if((backupMOB==null)
			||(backupMOB.amDestroyed())
			||(backupMOB.amDead()))
			{
				backupMOB=CMClass.getMOB("StdMOB");
				if(backupMOB!=null)
				{
					backupMOB.setName(ticking.name());
					backupMOB.setDisplayText(L("@x1 is here.",ticking.name()));
					backupMOB.setDescription("");
					backupMOB.setAgeMinutes(-1);
					mob=backupMOB;
					if(backupMOB.location()!=lastKnownLocation)
						backupMOB.setLocation(lastKnownLocation);
				}
			}
			else
			{
				backupMOB.setAgeMinutes(-1);
				mob=backupMOB;
				if(backupMOB.location()!=lastKnownLocation)
				{
					backupMOB.setLocation(lastKnownLocation);
					backupMOB.setName(ticking.name());
					backupMOB.setDisplayText(L("@x1 is here.",ticking.name()));
				}
			}
		}
		return mob;
	}

	protected boolean canTrigger(int triggerCode)
	{
		final Long L=noTrigger.get(Integer.valueOf(triggerCode));
		if(L==null)
			return true;
		if(System.currentTimeMillis()<L.longValue())
			return false;
		noTrigger.remove(Integer.valueOf(triggerCode));
		return true;
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		final MOB mob=getMakeMOB(ticking);
		final Item defaultItem=(ticking instanceof Item)?(Item)ticking:null;

		if((mob==null)||(lastKnownLocation==null))
		{
			altStatusTickable=null;
			return true;
		}

		final PhysicalAgent affecting=(ticking instanceof PhysicalAgent)?((PhysicalAgent)ticking):null;

		final List<DVector> scripts=getScripts();
		
		if(!runInPassiveAreas)
		{
			final Area A=CMLib.map().areaLocation(ticking);
			if((A!=null)&&(A.getAreaState() != Area.State.ACTIVE))
			{
				return true;
			}
		}

		int triggerCode=-1;
		String trigger="";
		String[] t=null;
		for(int thisScriptIndex=0;thisScriptIndex<scripts.size();thisScriptIndex++)
		{
			final DVector script=scripts.get(thisScriptIndex);
			if(script.size()<2)
				continue;
			trigger=((String)script.elementAt(0,1)).toUpperCase().trim();
			t=(String[])script.elementAt(0,2);
			triggerCode=getTriggerCode(trigger,t);
			tickStatus=Tickable.STATUS_SCRIPT+triggerCode;
			switch(triggerCode)
			{
			case 5: // rand_Prog
				if((!mob.amDead())&&canTrigger(5))
				{
					if(t==null)
						t=parseBits(script,0,"CR");
					if(t!=null)
					{
						final int prcnt=CMath.s_int(t[1]);
						if(CMLib.dice().rollPercentage()<prcnt)
							execute(affecting,mob,mob,mob,defaultItem,null,script,null,newObjs());
					}
				}
				break;
			case 16: // delay_prog
				if((!mob.amDead())&&canTrigger(16))
				{
					int targetTick=-1;
					final Integer thisScriptIndexI=Integer.valueOf(thisScriptIndex);
					final int[] delayProgCounter;
					synchronized(thisScriptIndexI)
					{
						if(delayTargetTimes.containsKey(thisScriptIndexI))
							targetTick=delayTargetTimes.get(thisScriptIndexI).intValue();
						else
						{
							if(t==null)
								t=parseBits(script,0,"CCR");
							if(t!=null)
							{
								final int low=CMath.s_int(t[1]);
								int high=CMath.s_int(t[2]);
								if(high<low)
									high=low;
								targetTick=CMLib.dice().roll(1,high-low+1,low-1);
								delayTargetTimes.put(thisScriptIndexI,Integer.valueOf(targetTick));
							}
						}
						if(delayProgCounters.containsKey(thisScriptIndexI))
							delayProgCounter=delayProgCounters.get(thisScriptIndexI);
						else
						{
							delayProgCounter=new int[]{0};
							delayProgCounters.put(thisScriptIndexI,delayProgCounter);
						}
					}
					boolean exec=false;
					synchronized(delayProgCounter)
					{
						if(delayProgCounter[0]>=targetTick)
						{
							exec=true;
							delayProgCounter[0]=0;
						}
						else
							delayProgCounter[0]++;
					}
					if(exec)
						execute(affecting,mob,mob,mob,defaultItem,null,script,null,newObjs());
				}
				break;
			case 7: // fight_Prog
				if((mob.isInCombat())&&(!mob.amDead())&&canTrigger(7))
				{
					if(t==null)
						t=parseBits(script,0,"CR");
					if(t!=null)
					{
						final int prcnt=CMath.s_int(t[1]);
						if(CMLib.dice().rollPercentage()<prcnt)
							execute(affecting,mob.getVictim(),mob,mob,defaultItem,null,script,null,newObjs());
					}
				}
				else
				if((ticking instanceof Item)
				&&canTrigger(7)
				&&(((Item)ticking).owner() instanceof MOB)
				&&(((MOB)((Item)ticking).owner()).isInCombat()))
				{
					if(t==null)
						t=parseBits(script,0,"CR");
					if(t!=null)
					{
						final int prcnt=CMath.s_int(t[1]);
						if(CMLib.dice().rollPercentage()<prcnt)
						{
							final MOB M=(MOB)((Item)ticking).owner();
							if(!M.amDead())
								execute(affecting,M,mob.getVictim(),mob,defaultItem,null,script,null,newObjs());
						}
					}
				}
				break;
			case 11: // hitprcnt_prog
				if((mob.isInCombat())&&(!mob.amDead())&&canTrigger(11))
				{
					if(t==null)
						t=parseBits(script,0,"CR");
					if(t!=null)
					{
						final int prcnt=CMath.s_int(t[1]);
						final int floor=(int)Math.round(CMath.mul(CMath.div(prcnt,100.0),mob.maxState().getHitPoints()));
						if(mob.curState().getHitPoints()<=floor)
							execute(affecting,mob.getVictim(),mob,mob,defaultItem,null,script,null,newObjs());
					}
				}
				else
				if((ticking instanceof Item)
				&&canTrigger(11)
				&&(((Item)ticking).owner() instanceof MOB)
				&&(((MOB)((Item)ticking).owner()).isInCombat()))
				{
					final MOB M=(MOB)((Item)ticking).owner();
					if(!M.amDead())
					{
						if(t==null)
							t=parseBits(script,0,"CR");
						if(t!=null)
						{
							final int prcnt=CMath.s_int(t[1]);
							final int floor=(int)Math.round(CMath.mul(CMath.div(prcnt,100.0),M.maxState().getHitPoints()));
							if(M.curState().getHitPoints()<=floor)
								execute(affecting,M,mob.getVictim(),mob,defaultItem,null,script,null,newObjs());
						}
					}
				}
				break;
			case 6: // once_prog
				if(!oncesDone.contains(script)&&canTrigger(6))
				{
					if(t==null)
						t=parseBits(script,0,"C");
					oncesDone.add(script);
					execute(affecting,mob,mob,mob,defaultItem,null,script,null,newObjs());
				}
				break;
			case 14: // time_prog
				if((mob.location()!=null)
				&&canTrigger(14)
				&&(!mob.amDead()))
				{
					if(t==null)
						t=parseBits(script,0,"CT");
					int lastTimeProgDone=-1;
					if(lastTimeProgsDone.containsKey(Integer.valueOf(thisScriptIndex)))
						lastTimeProgDone=lastTimeProgsDone.get(Integer.valueOf(thisScriptIndex)).intValue();
					final int time=mob.location().getArea().getTimeObj().getHourOfDay();
					if((t!=null)&&(lastTimeProgDone!=time))
					{
						boolean done=false;
						for(int i=1;i<t.length;i++)
						{
							if(time==CMath.s_int(t[i]))
							{
								done=true;
								execute(affecting,mob,mob,mob,defaultItem,null,script,""+time,newObjs());
								lastTimeProgsDone.remove(Integer.valueOf(thisScriptIndex));
								lastTimeProgsDone.put(Integer.valueOf(thisScriptIndex),Integer.valueOf(time));
								break;
							}
						}
						if(!done)
							lastTimeProgsDone.remove(Integer.valueOf(thisScriptIndex));
					}
				}
				break;
			case 15: // day_prog
				if((mob.location()!=null)&&canTrigger(15)
				&&(!mob.amDead()))
				{
					if(t==null)
						t=parseBits(script,0,"CT");
					int lastDayProgDone=-1;
					if(lastDayProgsDone.containsKey(Integer.valueOf(thisScriptIndex)))
						lastDayProgDone=lastDayProgsDone.get(Integer.valueOf(thisScriptIndex)).intValue();
					final int day=mob.location().getArea().getTimeObj().getDayOfMonth();
					if((t!=null)&&(lastDayProgDone!=day))
					{
						boolean done=false;
						for(int i=1;i<t.length;i++)
						{
							if(day==CMath.s_int(t[i]))
							{
								done=true;
								execute(affecting,mob,mob,mob,defaultItem,null,script,null,newObjs());
								lastDayProgsDone.remove(Integer.valueOf(thisScriptIndex));
								lastDayProgsDone.put(Integer.valueOf(thisScriptIndex),Integer.valueOf(day));
								break;
							}
						}
						if(!done)
							lastDayProgsDone.remove(Integer.valueOf(thisScriptIndex));
					}
				}
				break;
			case 13: // quest_time_prog
				if(!oncesDone.contains(script)&&canTrigger(13))
				{
					if(t==null)
						t=parseBits(script,0,"CCC");
					if(t!=null)
					{
						final Quest Q=getQuest(t[1]);
						if((Q!=null)&&(Q.running())&&(!Q.stopping()))
						{
							final int time=CMath.s_int(t[2]);
							if(time>=Q.minsRemaining())
							{
								oncesDone.add(script);
								execute(affecting,mob,mob,mob,defaultItem,null,script,null,newObjs());
							}
						}
					}
				}
				break;
			default:
				break;
			}
		}
		tickStatus=Tickable.STATUS_SCRIPT+100;
		dequeResponses();
		altStatusTickable=null;
		return true;
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

	public void enqueResponse(PhysicalAgent host,
							  MOB source,
							  Environmental target,
							  MOB monster,
							  Item primaryItem,
							  Item secondaryItem,
							  DVector script,
							  int ticks,
							  String msg)
	{
		if(!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
			return;
		if(que.size()>25)
		{
			this.logError(monster, "UNK", "SYS", "Attempt to que more than 25 events.");
			que.clear();
		}
		if(noDelay)
			execute(host,source,target,monster,primaryItem,secondaryItem,script,msg,newObjs());
		else
			que.add(new ScriptableResponse(host,source,target,monster,primaryItem,secondaryItem,script,ticks,msg));
	}

	public void prequeResponse(PhysicalAgent host,
							   MOB source,
							   Environmental target,
							   MOB monster,
							   Item primaryItem,
							   Item secondaryItem,
							   DVector script,
							   int ticks,
							   String msg)
	{
		if(!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
			return;
		if(que.size()>25)
		{
			this.logError(monster, "UNK", "SYS", "Attempt to que more than 25 events.");
			que.clear();
		}
		que.add(0,new ScriptableResponse(host,source,target,monster,primaryItem,secondaryItem,script,ticks,msg));
	}

	@Override
	public void dequeResponses()
	{
		try
		{
			tickStatus=Tickable.STATUS_SCRIPT+100;
			for(int q=que.size()-1;q>=0;q--)
			{
				ScriptableResponse SB=null;
				try
				{
					SB=que.get(q);
				}
				catch(final ArrayIndexOutOfBoundsException x)
				{
					continue;
				}
				if(SB.checkTimeToExecute())
				{
					execute(SB.h,SB.s,SB.t,SB.m,SB.pi,SB.si,SB.scr,SB.message,newObjs());
					que.remove(SB);
				}
			}
		}
		catch (final Exception e)
		{
			Log.errOut("DefaultScriptingEngine", e);
		}
	}

	public String L(final String str, final String ... xs)
	{
		return CMLib.lang().fullSessionTranslation(str, xs);
	}
	
	protected static class JScriptEvent extends ScriptableObject
	{
		@Override
		public String getClassName()
		{
			return "JScriptEvent";
		}

		static final long				serialVersionUID	= 43;
		final PhysicalAgent				h;
		final MOB						s;
		final Environmental				t;
		final MOB						m;
		final Item						pi;
		final Item						si;
		final Object[]					objs;
		Vector<String>					scr;
		final String					message;
		final DefaultScriptingEngine	c;

		public Environmental host()
		{
			return h;
		}

		public MOB source()
		{
			return s;
		}

		public Environmental target()
		{
			return t;
		}

		public MOB monster()
		{
			return m;
		}

		public Item item()
		{
			return pi;
		}

		public Item item2()
		{
			return si;
		}

		public String message()
		{
			return message;
		}

		public void setVar(String host, String var, String value)
		{
			c.setVar(host,var.toUpperCase(),value);
		}

		public String getVar(String host, String var)
		{
			return c.getVar(host, var);
		}

		public String toJavaString(Object O){return Context.toString(O);}

		@Override
		public Object get(final String name, Scriptable start)
		{
			if (super.has(name, start))
				return super.get(name, start);
			if (methH.containsKey(name) || funcH.containsKey(name)
			|| (name.endsWith("$")&&(funcH.containsKey(name.substring(0,name.length()-1)))))
			{
				return new Function()
				{
					@Override
					public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args)
					{
						if(methH.containsKey(name))
						{
							final StringBuilder strb=new StringBuilder(name);
							if(args.length==1)
								strb.append(" ").append(String.valueOf(args[0]));
							else
							for(int i=0;i<args.length;i++)
							{
								if(i==args.length-1)
									strb.append(" ").append(String.valueOf(args[i]));
								else
									strb.append(" ").append("'"+String.valueOf(args[i])+"'");
							}
							final DVector DV=new DVector(2);
							DV.addElement("JS_PROG",null);
							DV.addElement(strb.toString(),null);
							return c.execute(h,s,t,m,pi,si,DV,message,objs);
						}
						if(name.endsWith("$"))
						{
							final StringBuilder strb=new StringBuilder(name.substring(0,name.length()-1)).append("(");
							if(args.length==1)
								strb.append(" ").append(String.valueOf(args[0]));
							else
							for(int i=0;i<args.length;i++)
							{
								if(i==args.length-1)
									strb.append(" ").append(String.valueOf(args[i]));
								else
									strb.append(" ").append("'"+String.valueOf(args[i])+"'");
							}
							strb.append(" ) ");
							return c.functify(h,s,t,m,pi,si,message,objs,strb.toString());
						}
						final String[] sargs=new String[args.length+3];
						sargs[0]=name;
						sargs[1]="(";
						for(int i=0;i<args.length;i++)
							sargs[i+2]=String.valueOf(args[i]);
						sargs[sargs.length-1]=")";
						final String[][] EVAL={sargs};
						return Boolean.valueOf(c.eval(h,s,t,m,pi,si,message,objs,EVAL,0));
					}

					@Override
					public void delete(String arg0)
					{
					}

					@Override
					public void delete(int arg0)
					{
					}

					@Override
					public Object get(String arg0, Scriptable arg1)
					{
						return null;
					}

					@Override
					public Object get(int arg0, Scriptable arg1)
					{
						return null;
					}

					@Override
					public String getClassName()
					{
						return null;
					}

					@Override
					public Object getDefaultValue(Class<?> arg0)
					{
						return null;
					}

					@Override
					public Object[] getIds()
					{
						return null;
					}

					@Override
					public Scriptable getParentScope()
					{
						return null;
					}

					@Override
					public Scriptable getPrototype()
					{
						return null;
					}

					@Override
					public boolean has(String arg0, Scriptable arg1)
					{
						return false;
					}

					@Override
					public boolean has(int arg0, Scriptable arg1)
					{
						return false;
					}

					@Override
					public boolean hasInstance(Scriptable arg0)
					{
						return false;
					}

					@Override
					public void put(String arg0, Scriptable arg1, Object arg2)
					{
					}

					@Override
					public void put(int arg0, Scriptable arg1, Object arg2)
					{
					}

					@Override
					public void setParentScope(Scriptable arg0)
					{
					}

					@Override
					public void setPrototype(Scriptable arg0)
					{
					}

					@Override
					public Scriptable construct(Context arg0, Scriptable arg1, Object[] arg2)
					{
						return null;
					}
				};
			}
			return super.get(name, start);
		}

		public void executeEvent(final DVector script, final int lineNum)
		{
			c.execute(h, s, t, m, pi, si, script, message, objs, lineNum);
		}

		public JScriptEvent(DefaultScriptingEngine scrpt,
							PhysicalAgent host,
							MOB source,
							Environmental target,
							MOB monster,
							Item primaryItem,
							Item secondaryItem,
							String msg,
							Object[] tmp)
		{
			c=scrpt;
			h=host;
			s=source;
			t=target;
			m=monster;
			pi=primaryItem;
			si=secondaryItem;
			message=msg;
			objs=tmp;
		}
	}
}
