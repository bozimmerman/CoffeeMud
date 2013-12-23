package com.planet_ink.coffee_mud.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;

/*
   Copyright 2000-2013 Bo Zimmerman

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
public class DefaultQuest implements Quest, Tickable, CMObject
{
	public String ID(){return "DefaultQuest";}

	protected String name="";
	protected String author="";
	protected String displayName="";
	protected String startDate="";
	protected int duration=450; // about 30 minutes
	protected String rawScriptParameter="";
	protected SVector<String> winners=new SVector<String>();
	protected boolean durable=false;
	protected int minWait=-1;
	protected int minPlayers=-1;
	protected String playerMask="";
	protected int runLevel=-1;
	protected int maxWait=-1;
	protected int waitRemaining=-1;
	protected int ticksRemaining=-1;
	protected long lastStartDateTime=System.currentTimeMillis();
	private boolean stoppingQuest=false;
	protected int spawn=SPAWN_NO;
	private QuestState questState=new QuestState();
	private boolean copy=false;
	private boolean suspended=false;
	private Hashtable stepEllapsedTimes=new Hashtable();
	public DVector internalFiles=null;
	private int[] resetData=null;

	// the unique name of the quest
	public String name(){return name;}
	public void setName(String newName){name=newName;}

	// the author of the quest
	public String author(){return author;}
	public void setAuthor(String newName){author=newName;}
	
	// the display name of the quest
	public String displayName(){ return displayName;}
	public void setDisplayName(String newName){ displayName=newName;}

	public CMObject copyOf()
	{
		try
		{
			Object O=this.clone();
			return (CMObject)O;
		}
		catch(CloneNotSupportedException e)
		{
			return newInstance();
		}
	}

	public boolean suspended(){ return suspended;}
	public void setSuspended(boolean truefalse){suspended=truefalse;}

	public CMObject newInstance(){try{return getClass().newInstance();}catch(Exception e){return new DefaultQuest();}}
	public void initializeClass(){}

	public Object getDesignatedObject(String named)
	{
		int code=-1;
		for(int i=0;i<QCODES.length;i++)
			if(named.equalsIgnoreCase(QCODES[i]))
			{ code=i; break;}
		switch(code)
		{
		case 0: return ID();
		case 1: return name();
		case 2: return ""+duration();
		case 3: return ""+minWait();
		case 4: return ""+minPlayers();
		case 5: return ""+playerMask();
		case 6: return ""+runLevel();
		case 7: return ""+startDate();
		case 8: return ""+startDate();
		case 9: return ""+waitInterval();
		case 10: return SPAWN_DESCS[getSpawn()];
		case 11: return displayName();
		case 12: break; // instructions should fall through
		case 13: return Boolean.toString(durable);
		case 14: return ""+author();
		}
		return questState.getStat(named);
	}

	public void internalQuestDelete()
	{
		if(isCopy()) return;
		if((internalFiles!=null)&&(internalFiles.size()>0))
		{
			for(int i=0;i<internalFiles.size();i++)
			{
				String filename=((String)internalFiles.elementAt(i,1)).toUpperCase();
				Vector<String> delThese=new Vector<String>();
				boolean foundKey=false;
				for(Iterator<String> k=Resources.findResourceKeys(filename);k.hasNext();)
				{
					String key=k.next();
					if(key.startsWith("PARSEDPRG: ")&&(key.toUpperCase().endsWith(filename)))
					{ foundKey=true; delThese.addElement(key);}
				}
				if(foundKey)
					for(int d=0;d<delThese.size();d++)
						Resources.removeResource(delThese.elementAt(d));
			}
			internalFiles.clear();
			internalFiles=null;
		}
	}

	// the unique name of the quest
	public String startDate(){return startDate;}
	public void setStartDate(String newDate){
		int x=newDate.indexOf('-');
		if((x>0)
		&&(CMath.isMathExpression(newDate.substring(0,x)))
		&&(CMath.isMathExpression(newDate.substring(x+1))))
			startDate=newDate;
	}
	public void setStartMudDate(String newDate){
		setStartDate(newDate);
		if(startDate.equals(newDate))
			startDate="MUDDAY "+startDate;
	}

	// the duration, in ticks
	public int duration(){return duration;}
	public void setDuration(int newTicks){duration=newTicks;}

	public void setCopy(boolean truefalse){copy=truefalse;}
	public boolean isCopy(){return copy;}

	public void setSpawn(int spawnFlag){spawn=(spawnFlag<0)?0:spawnFlag;}
	public int getSpawn(){return spawn;}

	public int minPlayers(){return minPlayers;}
	public void setMinPlayers(int players){minPlayers=players;}
	public int runLevel(){return runLevel;}
	public void setRunLevel(int level){runLevel=level;}
	public String playerMask(){return playerMask;}
	public void setPlayerMask(String mask){playerMask=mask;}

	// the rest of the script.  This may be semicolon-separated instructions,
	// or a LOAD command followed by the quest script path.
	public void setScript(String parm){
		rawScriptParameter=parm;
		name="";
		author="";
		displayName="";
		startDate="";
		duration=-1;
		minWait=-1;
		maxWait=-1;
		minPlayers=-1;
		spawn=SPAWN_NO;
		playerMask="";
		runLevel=-1;
		internalFiles=null;
		durable=false;
		setVars(parseLoadScripts(parm,new Vector(),new Vector()),0);
		if(isCopy()) spawn=SPAWN_NO;
	}
	public String script(){return rawScriptParameter;}

	public void autostartup()
	{
		if(!resetWaitRemaining(0))
			CMLib.threads().deleteTick(this,Tickable.TICKID_QUEST);
		else
		if(!running())
			CMLib.threads().startTickDown(this,Tickable.TICKID_QUEST,1);
	}

	public void setVars(List<?> script, int startAtLine)
	{
		List<String> parsedLine=null;
		String var=null;
		String val=null;
		List<List<String>> setScripts=CMLib.quests().parseQuestCommandLines(script,"SET",startAtLine);
		for(int v=0;v<setScripts.size();v++)
		{
			parsedLine=setScripts.get(v);
			if(parsedLine.size()>1)
			{
				var=parsedLine.get(1).toUpperCase();
				val=CMParms.combine(parsedLine,2);
				if(isStat(var))
					setStat(var,val);
			}
		}
	}

	public StringBuffer getResourceFileData(String named)
	{
		int index=-1;
		if(internalFiles!=null){
			index=internalFiles.indexOf(named.toUpperCase().trim());
			if(index>=0) return (StringBuffer)internalFiles.elementAt(index,2);
		}
		StringBuffer buf=new CMFile(Resources.makeFileResourceName(named),null,CMFile.FLAG_LOGERRORS).text();
		return buf;
	}

	private void questifyScriptableBehavs(PhysicalAgent E)
	{
		if(E==null) return;
		Behavior B=null;
		for(Enumeration<Behavior> e=E.behaviors();e.hasMoreElements();)
		{
			B=e.nextElement();
			if(B instanceof ScriptingEngine)
				((ScriptingEngine)B).registerDefaultQuest(this.name());
		}
	}

	private Enumeration<Room> getAppropriateRoomSet(QuestState q)
	{
		if(q.roomGroup!=null)
			return new IteratorEnumeration(q.roomGroup.iterator());
		else
		if(q.area!=null)
			return q.area.getMetroMap();
		return CMLib.map().rooms();
	}

	private final Iterable<Room> buildAppropriateRoomIterable(final QuestState q, final Iterable useThese)
	{
		final Enumeration<Room> e;
		if(useThese!=null)
			e=new IteratorEnumeration<Room>(useThese.iterator());
		else
		if(q.area!=null)
			e=q.area.getMetroMap();
		else
			e=CMLib.map().rooms();
		final LinkedList<Room> list=new LinkedList<Room>();
		for(;e.hasMoreElements();)
			list.add(e.nextElement());
		return list;
	}
	
	private List sortSelect(Environmental E, String str,
							List choices,
							List choices0,
							List choices1,
							List choices2,
							List choices3)
	{
		String mname=E.name().toUpperCase();
		String mdisp=E.displayText().toUpperCase();
		String mdesc=E.description().toUpperCase();
		if(str.equalsIgnoreCase("any"))
		{
			choices=choices0;
			choices0.add(E);
		}
		else
		if(mname.equalsIgnoreCase(str))
		{
			choices=choices0;
			choices0.add(E);
		}
		else
		if(CMLib.english().containsString(mname,str))
		{
			if((choices==null)||(choices==choices2)||(choices==choices3))
				choices=choices1;
			choices1.add(E);
		}
		else
		if(CMLib.english().containsString(mdisp,str))
		{
			if((choices==null)||(choices==choices3))
				choices=choices2;
			choices2.add(E);
		}
		else
		if(CMLib.english().containsString(mdesc,str))
		{
			if(choices==null) choices=choices3;
			choices3.add(E);
		}
		return choices;
	}

	private TimeClock getMysteryTimeNowFromState()
	{
		TimeClock NOW=null;
		if(questState.mysteryData==null) return (TimeClock)CMLib.time().globalClock().copyOf();
		if((questState.mysteryData.whereAt!=null)&&(questState.mysteryData.whereAt.getArea()!=null))
			NOW=(TimeClock)questState.mysteryData.whereAt.getArea().getTimeObj().copyOf();
		else
		if((questState.mysteryData.whereHappened!=null)&&(questState.mysteryData.whereHappened.getArea()!=null))
			NOW=(TimeClock)questState.mysteryData.whereHappened.getArea().getTimeObj().copyOf();
		else
		if((questState.room!=null)&&(questState.room.getArea()!=null))
			NOW=(TimeClock)questState.room.getArea().getTimeObj().copyOf();
		else
		if(questState.area!=null)
			NOW=(TimeClock)questState.area.getTimeObj().copyOf();
		else
			NOW=(TimeClock)CMLib.time().globalClock().copyOf();
		return NOW;
	}

	public void parseQuestScriptWArgs(Vector script, List args)
	{
		if(args==null) args=new Vector();
		if(args.size()==0)
			parseQuestScript(script, args, -1);
		else
		{
			Vector allArgs=new Vector();
			for(int i=0;i<args.size();i++)
			{
				Object O=args.get(i);
				if(O instanceof List)
				{
					List V=(List)O;
					if(allArgs.size()==0)
						for(int v=0;v<V.size();v++)
							allArgs.addElement(new XVector(V.get(v)));
					else
					{
						List allArgsCopy=(List)allArgs.clone();
						allArgs.clear();
						for(int aa=0;aa<allArgsCopy.size();aa++)
						{
							List argSet=(List)allArgsCopy.get(aa);
							for(int v=0;v<V.size();v++)
							{
								List V2=new XVector(argSet);
								V2.add(V.get(v));
								allArgs.addElement(V2);
							}
						}
					}
				}
				else
				if(allArgs.size()==0)
					allArgs.addElement(new XVector(O));
				else
				for(int aa=0;aa<allArgs.size();aa++)
					((List)allArgs.elementAt(aa)).add(O);
			}
			for(int a=0;a<allArgs.size();a++)
				parseQuestScript(script, (List)allArgs.elementAt(a),-1);
		}
	}

	protected void errorOccurred(QuestState q, boolean quietFlag, String msg)
	{
		if(!quietFlag) Log.errOut("Quest",msg);
		q.error=true;
	}

	private void sizeDownTo(List V, int num)
	{
		if(num<0) return;
		if(num==0) V.clear();
		else
		while(V.size()>num)
			V.remove(CMLib.dice().roll(1,V.size(),-1));
	}

	public void parseQuestScript(Vector script, List args, int startLine)
	{
		Vector finalScript=new Vector();
		for(int v=0;v<script.size();v++)
		{
			if(script.elementAt(v) instanceof String)
				finalScript.addElement(script.elementAt(v));
			else
			if(script.elementAt(v) instanceof List)
			{
				int vs=v;
				while((v<script.size())&&(script.elementAt(v) instanceof List))
					v++;
				int rnum=vs+CMLib.dice().roll(1,v-vs,-1);
				if(rnum<script.size())
				{
					List V=(List)script.elementAt(rnum);
					for(int v2=0;v2<V.size();v2++)
						if(V.get(v2) instanceof String)
							finalScript.addElement(V.get(v2));
				}
			}
		}

		script=finalScript;
		QuestState q=questState;
		int vStart=startLine;
		if(vStart<0) vStart=0;
		q.done=false;
		if(vStart>=script.size())
			return;
		q.startLine=vStart;
		for(int v=vStart;v<script.size();v++)
		{
			if(startLine>=0) q.lastLine=v;
			String s=modifyStringFromArgs((String)script.elementAt(v),args);
			Vector<String> p=CMParms.parse(s);
			boolean isQuiet=q.beQuiet;
			if(p.size()>0)
			{
				String cmd=p.elementAt(0).toUpperCase();
				if(cmd.equals("<SCRIPT>"))
				{
					StringBuffer jscript=new StringBuffer("");
					while(((++v)<script.size())
					&&(!((String)script.elementAt(v)).trim().toUpperCase().startsWith("</SCRIPT>")))
						jscript.append(((String)script.elementAt(v))+"\n");
					if(v>=script.size())
					{
						errorOccurred(q,false,"Quest '"+name()+"', <SCRIPT> command without </SCRIPT> found.");
						break;
					}
					if(!CMSecurity.isApprovedJScript(jscript))
					{
						errorOccurred(q,false,"Quest '"+name()+"', <SCRIPT> not approved.  Use MODIFY JSCRIPT to approve.");
						break;
					}
					Context cx = Context.enter();
					try
					{
						JScriptQuest scope = new JScriptQuest(this,q);
						cx.initStandardObjects(scope);
						scope.defineFunctionProperties(JScriptQuest.functions,
													   JScriptQuest.class,
													   ScriptableObject.DONTENUM);
						cx.evaluateString(scope, jscript.toString(),"<cmd>", 1, null);
					}
					catch(Exception e)
					{
					   	errorOccurred(q,false,"Quest '"+name()+"', JScript q.error: "+e.getMessage()+".");
						Context.exit();
						break;
					}
					Context.exit();
					continue;
				}
				if(cmd.equals("QUIET"))
				{
					if(p.size()<2)
					{
						q.beQuiet=true;
						continue;
					}
					isQuiet=true;
					p.removeElementAt(0);
					cmd=p.elementAt(0).toUpperCase();
				}
				if(cmd.equals("STEP"))
				{
					q.autoStepAfterDuration=false;
					if((p.size()>1)&&(p.elementAt(1).equalsIgnoreCase("BREAK")))
					{
						q.lastLine=script.size();
						q.done=true;
					}
					else
					if((p.size()>1)&&(p.elementAt(1).equalsIgnoreCase("BACK")))
					{
						if(startLine>=0) q.lastLine=q.startLine;
					}
					else
					if((p.size()>1)&&(p.elementAt(1).equalsIgnoreCase("AUTO")))
					{
						if(startLine>=0) q.lastLine=v+1;
						q.autoStepAfterDuration=true;
					}
					else
						if(startLine>=0) q.lastLine=v+1;
					return;
				}
				if(cmd.equals("RESET"))
				{
					if(q.room!=null)
						CMLib.map().resetRoom(q.room, true);
					else
					if(q.roomGroup!=null)
					{
						for(int r=0;r<q.roomGroup.size();r++)
							CMLib.map().resetRoom(q.roomGroup.get(r), true);
					}
					else
					if(q.area!=null)
						CMLib.map().resetArea(q.area);
					else
					{
						errorOccurred(q,false,"Quest '"+name()+"', no resettable room, roomgroup, area, or areagroup set.");
						break;
					}
				}
				else
				if(cmd.equals("SET"))
				{
					if(p.size()<2)
					{
						errorOccurred(q,isQuiet,"Quest '"+name()+"', unfound variable on set.");
						break;
					}
					cmd=p.elementAt(1).toUpperCase();
					if(cmd.equals("AREA"))
					{
						if(p.size()<3){
							q.area=null;
							continue;
						}
						try{
							q.area=(Area)getObjectIfSpecified(p,args,2,0); q.envObject=q.area; continue;
						}catch(CMException ex){
							q.area=null;
						}
						Vector names=new Vector();
						Vector areas=new Vector();
						if((p.size()>3)&&(p.elementAt(2).equalsIgnoreCase("any")))
							for(int ip=3;ip<p.size();ip++)
								names.addElement(p.elementAt(ip));
						else
							names.addElement(CMParms.combine(p,2));
						for(int n=0;n<names.size();n++)
						{
							String areaName=(String)names.elementAt(n);
							int oldSize=areas.size();
							if(areaName.equalsIgnoreCase("any"))
								areas.addElement(CMLib.map().getRandomArea());
							if(oldSize==areas.size())
							for (Enumeration e = CMLib.map().areas(); e.hasMoreElements(); )
							{
								Area A2 = (Area) e.nextElement();
								if (A2.Name().equalsIgnoreCase(areaName))
								{
									areas.addElement(A2);
									break;
								}
							}
							if(oldSize==areas.size())
							for(Enumeration e=CMLib.map().areas();e.hasMoreElements();)
							{
								Area A2=(Area)e.nextElement();
								if(CMLib.english().containsString(A2.Name(),areaName))
								{
									areas.addElement(A2);
									break;
								}
							}
						}
						if(areas.size()>0)
							q.area=(Area)areas.elementAt(CMLib.dice().roll(1,areas.size(),-1));
						if(q.area==null)
						{
							errorOccurred(q,isQuiet,"Quest '"+name()+"', unknown area '"+CMParms.combine(p,2)+"'.");
							break;
						}
					}
					else
					if(cmd.equals("AREAGROUP"))
					{
						q.area=null;
						q.roomGroup=null;
						if(p.size()<3) continue;
						Vector<String> names=new Vector<String>();
						Vector<Area> areas=new Vector<Area>();
						for(int ip=2;ip<p.size();ip++)
							names.addElement(p.elementAt(ip));
						for(int n=0;n<names.size();n++)
						{
							String areaName=names.elementAt(n);
							int oldSize=areas.size();
							if(areaName.equalsIgnoreCase("any"))
								areas.addElement(CMLib.map().getRandomArea());
							boolean addAll=areaName.equalsIgnoreCase("all");
							if(oldSize==areas.size())
							{
								if(addAll)
								{
									for (Enumeration<Area> e = CMLib.map().areas(); e.hasMoreElements(); )
									{
										Area A2=e.nextElement();
										if(!areas.contains(A2))
											areas.add(e.nextElement());
									}
								}
								else
								{
									Area A2=CMLib.map().findArea(areaName);
									if((A2!=null)&&(!areas.contains(A2)))
										areas.add(A2);
								}
							}
						}
						if(areas.size()>0)
						{
							q.roomGroup=new Vector<Room>();
							Area A=null;
							Room R=null;
							for(Enumeration<Area> e=areas.elements();e.hasMoreElements();)
							{
								A=e.nextElement();
								for(Enumeration<Room> e2=A.getMetroMap();e2.hasMoreElements();)
								{
									R=e2.nextElement();
									if(!q.roomGroup.contains(R))
										q.roomGroup.add(R);
								}
							}
							q.envObject=q.roomGroup;
						}
						else
						{
							errorOccurred(q,isQuiet,"Quest '"+name()+"', unknown areas '"+CMParms.combine(p,2)+"'.");
							break;
						}
					}
					else
					if(cmd.equals("MOBTYPE"))
					{
						boolean reselect=false;
						if((p.size()>2)&&(p.elementAt(2).equalsIgnoreCase("reselect")))
						{
							p.removeElementAt(2);
							reselect=true;
						}
						if(p.size()<3){
							q.mob=null;
							continue;
						}
						try{
							q.mob=(MOB)getObjectIfSpecified(p,args,2,0);
						}catch(CMException ex){
							q.mob=null;
							List<MOB> choices=new Vector<MOB>();
							Vector<String> mobTypes=CMParms.parse(CMParms.combine(p,2).toUpperCase());
							for(int t=0;t<mobTypes.size();t++)
							{
								String mobType=mobTypes.elementAt(t);
								if(mobType.startsWith("-")) continue;
								if(q.mobGroup==null)
								{
									try
									{
										for(Enumeration e=getAppropriateRoomSet(q);e.hasMoreElements();)
										{
											Room R2=(Room)e.nextElement();
											for(int i=0;i<R2.numInhabitants();i++)
											{
												MOB M2=R2.fetchInhabitant(i);
												if((M2!=null)
												&&(M2.isMonster())
												&&((M2.amUltimatelyFollowing()==null)||(M2.amUltimatelyFollowing().isMonster())))
												{
													if(mobType.equalsIgnoreCase("any"))
														choices.add(M2);
													else
													if((CMClass.classID(M2).toUpperCase().indexOf(mobType)>=0)
													||(M2.charStats().getMyRace().racialCategory().toUpperCase().indexOf(mobType)>=0)
													||(M2.charStats().getMyRace().name().toUpperCase().indexOf(mobType)>=0)
													||(M2.charStats().getCurrentClass().name(M2.charStats().getCurrentClassLevel()).toUpperCase().indexOf(mobType)>=0))
														choices.add(M2);
												}
											}
										}
									}catch(NoSuchElementException e){}
								}
								else
								{
									try
									{
										for(MOB M2 : q.mobGroup)
										{
											if((M2!=null)
											&&(M2.isMonster())
											&&((M2.amUltimatelyFollowing()==null)||(M2.amUltimatelyFollowing().isMonster())))
											{
												if(mobType.equalsIgnoreCase("any"))
													choices.add(M2);
												else
												if((CMClass.classID(M2).toUpperCase().indexOf(mobType)>=0)
												||(M2.charStats().getMyRace().racialCategory().toUpperCase().indexOf(mobType)>=0)
												||(M2.charStats().getMyRace().name().toUpperCase().indexOf(mobType)>=0)
												||(M2.charStats().getCurrentClass().name(M2.charStats().getCurrentClassLevel()).toUpperCase().indexOf(mobType)>=0))
													choices.add(M2);
											}
										}
									}catch(NoSuchElementException e){}
								}
							}

							for(int t=0;t<mobTypes.size();t++)
							{
								String mobType=mobTypes.elementAt(t);
								if(!mobType.startsWith("-")) continue;
								mobType=mobType.substring(1);
								for(int i=choices.size()-1;i>=0;i--)
								{
									MOB M2=choices.get(i);
									if((M2!=null)
									&&(M2.isMonster())
									&&((M2.amUltimatelyFollowing()==null)||(M2.amUltimatelyFollowing().isMonster())))
									{
										if((CMClass.classID(M2).toUpperCase().indexOf(mobType)>=0)
										||(M2.charStats().getMyRace().racialCategory().toUpperCase().indexOf(mobType)>=0)
										||(M2.charStats().getMyRace().name().toUpperCase().indexOf(mobType)>=0)
										||(M2.charStats().getCurrentClass().name(M2.charStats().getCurrentClassLevel()).toUpperCase().indexOf(mobType)>=0)
										||(M2.name().toUpperCase().indexOf(mobType)>=0)
										||(M2.displayText().toUpperCase().indexOf(mobType)>=0))
											choices.remove(M2);
									}
								}
							}
							if(choices.size()>0)
							{
								for(int c=choices.size()-1;c>=0;c--)
									if(((!reselect)||(!q.reselectable.contains(choices.get(c))))
									&&(CMLib.quests().objectInUse(choices.get(c))!=null))
										choices.remove(c);
								if((choices.size()==0)&&(!isQuiet))
									errorOccurred(q,isQuiet,"Quest '"+name()+"', all choices were taken: '"+p+"'.");
							}
							if(choices.size()>0)
								q.mob=choices.get(CMLib.dice().roll(1,choices.size(),-1));
						}
						if(q.mob==null)
						{
							errorOccurred(q,isQuiet,"Quest '"+name()+"', !mobtype '"+p+"'.");
							break;
						}
						if(reselect) q.reselectable.add(q.mob);

						// why is this being done -- atm, this is a simple map mob, minding his own business.
						questifyScriptableBehavs(q.mob);

						if(q.room!=null)
							q.room.bringMobHere(q.mob,false);
						else
							q.room=q.mob.location();
						q.envObject=q.mob;
						runtimeRegisterObject(q.mob);
						if(q.room!=null)
						{
							q.area=q.room.getArea();
							q.room.recoverRoomStats();
							q.room.showHappens(CMMsg.MSG_OK_ACTION,null);
						}
					}
					else
					if(cmd.equals("MOBGROUP"))
					{
						q.mobGroup=null;
						boolean reselect=false;
						if((p.size()>2)&&(p.elementAt(2).equalsIgnoreCase("reselect")))
						{
							p.removeElementAt(2);
							reselect=true;
						}
						if(p.size()<3) continue;
						List<MOB> choices=null;
						String mobName=CMParms.combine(p,2).toUpperCase();
						String maskStr=CMLib.quests().breakOutMaskString(s,p);
						MaskingLibrary.CompiledZapperMask mask=(maskStr.trim().length()==0)?null:CMLib.masking().maskCompile(maskStr);
						if(mask!=null) mobName=CMParms.combine(p,2).toUpperCase();
						try{
							choices=(List)getObjectIfSpecified(p,args,2,1);
						}catch(CMException ex){
							if(mobName.length()==0) mobName="ANY";
							boolean addAll=mobName.equalsIgnoreCase("all");
							List choices0=new Vector();
							List choices1=new Vector();
							List choices2=new Vector();
							List choices3=new Vector();
							try
							{
								for(Enumeration e=getAppropriateRoomSet(q);e.hasMoreElements();)
								{
									Room R2=(Room)e.nextElement();
									for(int i=0;i<R2.numInhabitants();i++)
									{
										MOB M2=R2.fetchInhabitant(i);
										if((M2!=null)
										&&(M2.isMonster())
										&&((M2.amUltimatelyFollowing()==null)||(M2.amUltimatelyFollowing().isMonster())))
										{
											if(CMLib.masking().maskCheck(mask,M2,true))
											{
												if(addAll)
												{
													choices = choices0;
													choices.add(M2);
												}
												else
													choices=sortSelect(M2,mobName,choices,choices0,choices1,choices2,choices3);
											}
										}
									}
								}
							}catch(NoSuchElementException e){}

							if((choices!=null)&&(choices.size()>0))
							{
								for(int c=choices.size()-1;c>=0;c--)
									if(((!reselect)||(!q.reselectable.contains(choices.get(c))))
									&&(CMLib.quests().objectInUse(choices.get(c))!=null))
										choices.remove(c);
								if((choices.size()==0)&&(!isQuiet))
									errorOccurred(q,isQuiet,"Quest '"+name()+"', all choices were taken: '"+p+"'.");
							}
						}
						if((choices!=null)&&(choices.size()>0))
						{
							q.mobGroup=choices;
							if(reselect) q.reselectable.addAll(choices);
						}
						else
						{
							errorOccurred(q,isQuiet,"Quest '"+name()+"', !mobgroup '"+mobName+":"+maskStr+"'.");
							break;
						}
						q.envObject=q.mobGroup;
					}
					else
					if(cmd.equals("ITEMGROUP"))
					{
						q.itemGroup=null;
						boolean reselect=false;
						if((p.size()>2)&&(p.elementAt(2).equalsIgnoreCase("reselect")))
						{
							p.removeElementAt(2);
							reselect=true;
						}
						if(p.size()<3) continue;
						List<Item> choices=null;
						String itemName=CMParms.combine(p,2).toUpperCase();
						String maskStr=CMLib.quests().breakOutMaskString(s,p);
						MaskingLibrary.CompiledZapperMask mask=(maskStr.trim().length()==0)?null:CMLib.masking().maskCompile(maskStr);
						if(mask!=null) itemName=CMParms.combine(p,2).toUpperCase();
						try{
							choices=(List)getObjectIfSpecified(p,args,2,1);
						}catch(CMException ex){
							List choices0=new Vector();
							List choices1=new Vector();
							List choices2=new Vector();
							List choices3=new Vector();
							if(itemName.length()==0) itemName="ANY";
							boolean addAll=itemName.equalsIgnoreCase("all");
							try
							{
								for(Enumeration e=getAppropriateRoomSet(q);e.hasMoreElements();)
								{
									Room R2=(Room)e.nextElement();
									for(int i=0;i<R2.numItems();i++)
									{
										Item I2=R2.getItem(i);
										if(I2!=null)
										{
											if(CMLib.masking().maskCheck(mask,I2,true))
											{
												if(addAll)
												{
													choices = choices0;
													choices.add(I2);
												}
												else
													choices=sortSelect(I2,itemName,choices,choices0,choices1,choices2,choices3);
											}
										}
									}
								}
							}catch(NoSuchElementException e){}
							if((choices!=null)&&(choices.size()>0))
							{
								for(int c=choices.size()-1;c>=0;c--)
									if(((!reselect)||(!q.reselectable.contains(choices.get(c))))
									&&(CMLib.quests().objectInUse(choices.get(c))!=null))
										choices.remove(c);
								if((choices.size()==0)&&(!isQuiet))
									errorOccurred(q,isQuiet,"Quest '"+name()+"', all choices were taken: '"+p+"'.");
							}
						}
						if((choices!=null)&&(choices.size()>0))
						{
							if(reselect) q.reselectable.addAll(choices);
							q.itemGroup=choices;
						}
						else
						{
							errorOccurred(q,isQuiet,"Quest '"+name()+"', !itemgroup '"+itemName+":"+maskStr+"'.");
							break;
						}
						q.envObject=q.itemGroup;
					}
					else
					if(cmd.equals("ITEMTYPE"))
					{
						boolean reselect=false;
						if((p.size()>2)&&(p.elementAt(2).equalsIgnoreCase("reselect")))
						{
							p.removeElementAt(2);
							reselect=true;
						}
						if(p.size()<3){
							q.item=null;
							continue;
						}
						try{
							q.item=(Item)getObjectIfSpecified(p,args,2,0);
						}catch(CMException ex){
							q.item=null;
							List<Item> choices=new ArrayList();
							Vector itemTypes=new Vector();
							for(int i=2;i<p.size();i++)
								itemTypes.addElement(p.elementAt(i));
							for(int t=0;t<itemTypes.size();t++)
							{
								String itemType=((String)itemTypes.elementAt(t)).toUpperCase();
								if(itemType.startsWith("-")) continue;
								try
								{
									if(q.itemGroup==null)
									{
										for(Enumeration e=getAppropriateRoomSet(q);e.hasMoreElements();)
										{
											Room R2=(Room)e.nextElement();
											for(int i=0;i<R2.numItems();i++)
											{
												Item I2=R2.getItem(i);
												if((I2!=null))
												{
													if(itemType.equalsIgnoreCase("any"))
														choices.add(I2);
													else
													if(CMClass.classID(I2).toUpperCase().indexOf(itemType)>=0)
														choices.add(I2);
												}
											}
										}
									}
									else
									{
										for(Item I2 : q.itemGroup)
										{
											if((I2!=null))
											{
												if(itemType.equalsIgnoreCase("any"))
													choices.add(I2);
												else
												if(CMClass.classID(I2).toUpperCase().indexOf(itemType)>=0)
													choices.add(I2);
											}
										}
									}
								}catch(NoSuchElementException e){}
							}

							for(int t=0;t<itemTypes.size();t++)
							{
								String itemType=(String)itemTypes.elementAt(t);
								if(!itemType.startsWith("-")) continue;
								itemType=itemType.substring(1);
								for(int i=choices.size()-1;i>=0;i--)
								{
									Item I2=choices.get(i);
									if((CMClass.classID(I2).toUpperCase().indexOf(itemType)>=0)
									||(I2.name().toUpperCase().indexOf(itemType)>=0)
									||(I2.displayText().toUpperCase().indexOf(itemType)>=0))
										choices.remove(I2);
								}
							}
							if(choices.size()>0)
							{
								for(int c=choices.size()-1;c>=0;c--)
									if(((!reselect)||(!q.reselectable.contains(choices.get(c))))
									&&(CMLib.quests().objectInUse(choices.get(c))!=null))
										choices.remove(c);
								if((choices.size()==0)&&(!isQuiet))
									errorOccurred(q,isQuiet,"Quest '"+name()+"', all choices were taken: '"+p+"'.");
							}
							if(choices.size()>0)
								q.item=choices.get(CMLib.dice().roll(1,choices.size(),-1));
						}
						if(q.item==null)
						{
							errorOccurred(q,isQuiet,"Quest '"+name()+"', !itemtype '"+p+"'.");
							break;
						}
						questifyScriptableBehavs(q.item); // this really makes little sense, though is harmless
						if(reselect) q.reselectable.add(q.item);
						if(q.room!=null)
							q.room.moveItemTo(q.item,ItemPossessor.Expire.Never,ItemPossessor.Move.Followers);
						else
						if(q.item.owner() instanceof Room)
							q.room=(Room)q.item.owner();
						q.envObject=q.item;
						if(q.room!=null)
						{
							q.area=q.room.getArea();
							q.room.recoverRoomStats();
							q.room.showHappens(CMMsg.MSG_OK_ACTION,null);
						}
					}
					else
					if(cmd.equals("PRESERVE"))
						q.preserveState=CMath.parseIntExpression(p.elementAt(2));
					else
					if(cmd.equals("LOCALE")||cmd.equals("LOCALEGROUP")||cmd.equals("LOCALEGROUPAROUND"))
					{
						int range=0;
						if(cmd.equals("LOCALE"))
						{
							try{
								q.room=(Room)getObjectIfSpecified(p,args,2,0);
								if(q.room!=null)
								{
									q.area=q.room.getArea();
									q.envObject=q.room;
								}
								continue;
							}catch(CMException ex){
								q.room=null;
							}
						}
						else
						if(cmd.equals("LOCALEGROUPAROUND"))
						{
							q.roomGroup=null;
							if(p.size()<3) continue;
							range=CMath.parseIntExpression(p.elementAt(2));
							if(range<=0)
							{
								errorOccurred(q,isQuiet,"Quest '"+name()+"', !localegrouparound #'"+(p.elementAt(2)+"'."));
								break;
							}
							p.removeElementAt(2);
							if(q.room==null)
							{
								errorOccurred(q,isQuiet,"Quest '"+name()+"', localegrouparound !room.");
								break;
							}
						}
						else
						{
							try{
								q.roomGroup=(List)getObjectIfSpecified(p,args,2,1);
								q.envObject=q.roomGroup;
								continue;
							}catch(CMException ex){
								q.roomGroup=null;
							}
						}
						if(p.size()<3) continue;
						Vector names=new Vector();
						if((p.size()>3)&&(p.elementAt(2).equalsIgnoreCase("any")))
							for(int ip=3;ip<p.size();ip++)
								names.addElement(p.elementAt(ip));
						else
							names.addElement(CMParms.combine(p,2));
						List<Room> choices=new ArrayList();
						List<Room> useThese=null;
						if(range>0)
						{
							TrackingLibrary.TrackingFlags flags;
							flags = new TrackingLibrary.TrackingFlags()
									.plus(TrackingLibrary.TrackingFlag.AREAONLY);
							useThese=CMLib.tracking().getRadiantRooms(q.room,flags,range);
						}
						final Iterable<Room> list=buildAppropriateRoomIterable(q,useThese);
						for(int n=0;n<names.size();n++)
						{
							final String localeName=((String)names.elementAt(n)).toUpperCase();
							try
							{
								final Iterator<Room> e=list.iterator();
								boolean addAll=(localeName.equalsIgnoreCase("any")
												||localeName.equalsIgnoreCase("all"));
								for(;e.hasNext();)
								{
									final Room R2=e.next();
									if(addAll||CMClass.classID(R2).toUpperCase().indexOf(localeName)>=0)
										choices.add(R2);
									else
									{
										int dom=R2.domainType();
										if((dom&Room.INDOORS)>0)
										{
											if(Room.indoorDomainDescs[dom-Room.INDOORS].indexOf(localeName)>=0)
												choices.add(R2);
										}
										else
										if(Room.outdoorDomainDescs[dom].indexOf(localeName)>=0)
											choices.add(R2);
									}
								}
							}catch(NoSuchElementException e){}
						}
						if(cmd.equalsIgnoreCase("LOCALEGROUP")||cmd.equalsIgnoreCase("LOCALEGROUPAROUND"))
						{
							if(choices.size()>0)
								q.roomGroup=choices;
							else
							{
								errorOccurred(q,isQuiet,"Quest '"+name()+"', !localegroup '"+CMParms.combine(p,2)+"'.");
								break;
							}
							q.envObject=q.roomGroup;
						}
						else
						{
							if(choices.size()>0)
								q.room=choices.get(CMLib.dice().roll(1,choices.size(),-1));
							if(q.room==null)
							{
								errorOccurred(q,isQuiet,"Quest '"+name()+"', !locale '"+CMParms.combine(p,2)+"'.");
								break;
							}
							q.area=q.room.getArea();
							q.envObject=q.room;
						}
					}
					else
					if(cmd.equals("ROOM")||cmd.equals("ROOMGROUP")||cmd.equals("ROOMGROUPAROUND"))
					{
						int range=0;
						if(cmd.equals("ROOM"))
						{
							try{
								q.room=(Room)getObjectIfSpecified(p,args,2,0);
								if(q.room!=null)
								{
									q.area=q.room.getArea();
									q.envObject=q.room;
								}
								continue;
							}catch(CMException ex){
								q.room=null;
							}
						}
						else
						if(cmd.equals("ROOMGROUPAROUND"))
						{
							q.roomGroup=null;
							if(p.size()<3) continue;
							range=CMath.s_parseIntExpression(p.elementAt(2));
							if(range<=0)
							{
								errorOccurred(q,isQuiet,"Quest '"+name()+"' roomgrouparound #'"+(p.elementAt(2)+"'."));
								break;
							}
							p.removeElementAt(2);
							if(q.room==null)
							{
								errorOccurred(q,isQuiet,"Quest '"+name()+"', roomgrouparound !room.");
								break;
							}
						}
						else
						{
							try{
								q.roomGroup=(List)getObjectIfSpecified(p,args,2,1);
								q.envObject=q.roomGroup;
								continue;
							}catch(CMException ex){
								q.roomGroup=null;
							}
						}
						if(p.size()<3) continue;
						List<Room> choices=null;
						List choices0=new Vector();
						List choices1=new Vector();
						List choices2=new Vector();
						List choices3=new Vector();
						Vector names=new Vector();
						String maskStr=CMLib.quests().breakOutMaskString(s,p);
						MaskingLibrary.CompiledZapperMask mask=(maskStr.trim().length()==0)?null:CMLib.masking().maskCompile(maskStr);
						if((p.size()>3)&&(p.elementAt(2).equalsIgnoreCase("any")))
							for(int ip=3;ip<p.size();ip++)
								names.addElement(p.elementAt(ip));
						else
							names.addElement(CMParms.combine(p,2));
						List<Room> useThese=null;
						if(range>0)
						{
							TrackingLibrary.TrackingFlags flags;
							flags = new TrackingLibrary.TrackingFlags()
									.plus(TrackingLibrary.TrackingFlag.AREAONLY);
							useThese=CMLib.tracking().getRadiantRooms(q.room,flags,range);
						}
						final Iterable<Room> list=buildAppropriateRoomIterable(q,useThese);
						for(int n=0;n<names.size();n++)
						{
							final String localeName=((String)names.elementAt(n)).toUpperCase();
							try
							{
								final Iterator<Room> e=list.iterator();
								boolean addAll=localeName.equalsIgnoreCase("any")
											 ||localeName.equalsIgnoreCase("all");
								for(;e.hasNext();)
								{
									final Room R2=e.next();
									final String display=R2.displayText().toUpperCase();
									final String desc=R2.description().toUpperCase();
									if((mask!=null)&&(!CMLib.masking().maskCheck(mask,R2,true)))
										continue;
									if(addAll)
									{
										choices=choices0;
										choices0.add(R2);
									}
									else
									if(CMLib.map().getExtendedRoomID(R2).equalsIgnoreCase(localeName))
									{
										choices=choices0;
										choices0.add(R2);
									}
									else
									if(display.equals(localeName))
									{
										if((choices==null)||(choices==choices2)||(choices==choices3))
											choices=choices1;
										choices1.add(R2);
									}
									else
									if(CMLib.english().containsString(display,localeName))
									{
										if((choices==null)||(choices==choices3))
											choices=choices2;
										choices2.add(R2);
									}
									else
									if(CMLib.english().containsString(desc,localeName))
									{
										if(choices==null) choices=choices3;
										choices3.add(R2);
									}
								}
							}catch(NoSuchElementException e){}
						}
						if(cmd.equalsIgnoreCase("ROOMGROUP")||cmd.equalsIgnoreCase("ROOMGROUPAROUND"))
						{
							if((choices!=null)&&(choices.size()>0))
								q.roomGroup=choices;
							else
							{
								errorOccurred(q,isQuiet,"Quest '"+name()+"', !roomgroup '"+CMParms.combine(p,2)+"'.");
								break;
							}
							q.envObject=q.roomGroup;
						}
						else
						{
							if((choices!=null)&&(choices.size()>0))
								q.room=choices.get(CMLib.dice().roll(1,choices.size(),-1));
							if(q.room==null)
							{
								errorOccurred(q,isQuiet,"Quest '"+name()+"', !room '"+CMParms.combine(p,2)+"'.");
								break;
							}
							q.area=q.room.getArea();
							q.envObject=q.room;
						}
					}
					else
					if(cmd.equals("MOB"))
					{
						boolean reselect=false;
						if((p.size()>2)&&(p.elementAt(2).equalsIgnoreCase("reselect")))
						{
							p.removeElementAt(2);
							reselect=true;
						}
						if(p.size()<3){
							q.mob=null;
							continue;
						}
						String mobName=CMParms.combine(p,2).toUpperCase();
						String maskStr=CMLib.quests().breakOutMaskString(s,p);
						MaskingLibrary.CompiledZapperMask mask=(maskStr.trim().length()==0)?null:CMLib.masking().maskCompile(maskStr);
						if(mask!=null) mobName=CMParms.combine(p,2).toUpperCase();
						try{
							q.mob=(MOB)getObjectIfSpecified(p,args,2,0);
						}catch(CMException ex){
							q.mob=null;
							List<MOB> choices=null;
							List choices0=new Vector();
							List choices1=new Vector();
							List choices2=new Vector();
							List choices3=new Vector();
							if(mobName.length()==0) mobName="ANY";
							if(q.mobGroup!=null)
							{
								for(MOB M2 : q.mobGroup)
								{
									if((M2!=null)
									&&(M2.isMonster())
									&&((M2.amUltimatelyFollowing()==null)||(M2.amUltimatelyFollowing().isMonster())))
									{
										if(!CMLib.masking().maskCheck(mask,M2,true))
											continue;
										choices=sortSelect(M2,mobName,choices,choices0,choices1,choices2,choices3);
									}
								}
							}
							else
							{
								try
								{
									for(Enumeration e=getAppropriateRoomSet(q);e.hasMoreElements();)
									{
										Room R2=(Room)e.nextElement();
										for(int i=0;i<R2.numInhabitants();i++)
										{
											MOB M2=R2.fetchInhabitant(i);
											if((M2!=null)
											&&(M2.isMonster())
											&&((M2.amUltimatelyFollowing()==null)||(M2.amUltimatelyFollowing().isMonster())))
											{
												if(!CMLib.masking().maskCheck(mask,M2,true))
													continue;
												choices=sortSelect(M2,mobName,choices,choices0,choices1,choices2,choices3);
											}
										}
									}
								}catch(NoSuchElementException e){}
							}
							if((choices!=null)
							&&(choices.size()>0))
							{
								for(int c=choices.size()-1;c>=0;c--)
									if(((!reselect)||(!q.reselectable.contains(choices.get(c))))
									&&(CMLib.quests().objectInUse(choices.get(c))!=null))
										choices.remove(c);
								if((choices.size()==0)&&(!isQuiet))
									errorOccurred(q,isQuiet,"Quest '"+name()+"', all choices were taken: '"+p+"'.");
							}
							if((choices!=null)&&(choices.size()>0))
								q.mob=choices.get(CMLib.dice().roll(1,choices.size(),-1));
						}
						if(q.mob==null)
						{
							errorOccurred(q,isQuiet,"Quest '"+name()+"', !mob '"+mobName+"'.");
							break;
						}
						if(reselect) q.reselectable.add(q.mob);
						questifyScriptableBehavs(q.mob);  // just wierd
						if(q.room!=null)
							q.room.bringMobHere(q.mob,false);
						else
							q.room=q.mob.location();
						if(q.room!=null)
						{
							q.area=q.room.getArea();
							q.envObject=q.mob;
							runtimeRegisterObject(q.mob);
							q.room.recoverRoomStats();
							q.room.showHappens(CMMsg.MSG_OK_ACTION,null);
						}
					}
					else
					if(cmd.equals("ITEM"))
					{
						boolean reselect=false;
						if((p.size()>2)&&(p.elementAt(2).equalsIgnoreCase("reselect")))
						{
							p.removeElementAt(2);
							reselect=true;
						}
						if(p.size()<3){
							q.item=null;
							continue;
						}
						String itemName=CMParms.combine(p,2).toUpperCase();
						String maskStr=CMLib.quests().breakOutMaskString(s,p);
						MaskingLibrary.CompiledZapperMask mask=(maskStr.trim().length()==0)?null:CMLib.masking().maskCompile(maskStr);
						if(mask!=null) itemName=CMParms.combine(p,2).toUpperCase();
						try{
							q.item=(Item)getObjectIfSpecified(p,args,2,0);
						}catch(CMException ex){
							q.item=null;
							List<Item> choices=null;
							List choices0=new Vector();
							List choices1=new Vector();
							List choices2=new Vector();
							List choices3=new Vector();
							if(itemName.trim().length()==0) itemName="ANY";
							try
							{
							if(q.itemGroup!=null)
							{
								for(Item I2 : q.itemGroup)
								{
									if(I2!=null)
									{
										if(!CMLib.masking().maskCheck(mask,I2,true))
											continue;
										choices=sortSelect(I2,itemName,choices,choices0,choices1,choices2,choices3);
									}
								}
							}
							else
							{
								for(Enumeration e=getAppropriateRoomSet(q);e.hasMoreElements();)
								{
									Room R2=(Room)e.nextElement();
									for(int i=0;i<R2.numItems();i++)
									{
										Item I2=R2.getItem(i);
										if(I2!=null)
										{
											if(!CMLib.masking().maskCheck(mask,I2,true))
												continue;
											choices=sortSelect(I2,itemName,choices,choices0,choices1,choices2,choices3);
										}
									}
								}
							}
							}catch(NoSuchElementException e){}
							if((choices!=null)&&(choices.size()>0))
							{
								for(int c=choices.size()-1;c>=0;c--)
									if(((!reselect)||(!q.reselectable.contains(choices.get(c))))
									&&(CMLib.quests().objectInUse(choices.get(c))!=null))
										choices.remove(c);
								if((choices.size()==0)&&(!isQuiet))
									errorOccurred(q,isQuiet,"Quest '"+name()+"', all choices were taken: '"+p+"'.");
							}
							if((choices!=null)&&(choices.size()>0))
								q.item=choices.get(CMLib.dice().roll(1,choices.size(),-1));
						}
						if(q.item==null)
						{
							errorOccurred(q,isQuiet,"Quest '"+name()+"', !item '"+itemName+"'.");
							break;
						}
						if(reselect) q.reselectable.add(q.item);
						questifyScriptableBehavs(q.item); // here we go again
						if(q.room!=null)
							q.room.moveItemTo(q.item,ItemPossessor.Expire.Never,ItemPossessor.Move.Followers);
						else
						if(q.item.owner() instanceof Room)
							q.room=(Room)q.item.owner();
						q.envObject=q.item;
						if(q.room!=null)
						{
							q.area=q.room.getArea();
							q.room.recoverRoomStats();
							q.room.showHappens(CMMsg.MSG_OK_ACTION,null);
						}
					}
					else
					if(cmd.equals("AGENT"))
					{
						if(q.mysteryData==null) q.mysteryData=new MysteryData();
						try{
							q.mob=(MOB)getObjectIfSpecified(p,args,2,0);
						}catch(CMException ex){
							if(p.size()>2)
							{
								errorOccurred(q,isQuiet,"Quest '"+name()+"', agent syntax '"+CMParms.combine(p,2)+"'.");
								break;
							}
						}
						if(q.mob==null)
						{
							errorOccurred(q,isQuiet,"Quest '"+name()+"', agent !mob.");
							break;
						}
						questifyScriptableBehavs(q.mob);  // should be done to loaded or q-scripted mobs only
						q.mysteryData.agent=q.mob;
						q.mob=q.mysteryData.agent;
						q.envObject=q.mob;
					}
					else
					if(cmd.equals("FACTION"))
					{
						if(q.mysteryData==null) q.mysteryData=new MysteryData();
						if(p.size()<3) continue;
						String numStr=CMParms.combine(p,2);
						Faction F=null;
						try{
							F=(Faction)getObjectIfSpecified(p,args,2,0);
						}catch(CMException ex){
							if(numStr.equalsIgnoreCase("ANY"))
							{
								int numFactions=CMLib.factions().numFactions();
								int whichFaction=CMLib.dice().roll(1,numFactions,-1);
								int curFaction=0;
								for(Enumeration<Faction> e=CMLib.factions().factions();e.hasMoreElements();curFaction++)
								{
									F=e.nextElement();
									if(curFaction==whichFaction)
										break;
								}
							}
							else
							{
								F=CMLib.factions().getFaction(numStr);
								if(F==null) F=CMLib.factions().getFactionByName(numStr);
							}
						}
						if(F==null)
						{
							errorOccurred(q,isQuiet,"Quest '"+name()+"', !faction #'"+numStr+"'.");
							break;
						}
						q.mysteryData.faction=F;
					}
					else
					if(cmd.equals("FACTIONGROUP"))
					{
						if(q.mysteryData==null) q.mysteryData=new MysteryData();
						if(p.size()<3){
							q.mysteryData.factionGroup=null;
							continue;
						}
						try{
							q.mysteryData.factionGroup=(List)getObjectIfSpecified(p,args,2,1);
						}catch(CMException ex){
							q.mysteryData.factionGroup=null;
							String numStr=CMParms.combine(p,2);
							Faction F=null;
							if(q.mysteryData.faction!=null)
								q.mysteryData.factionGroup.add(q.mysteryData.faction);
							if(CMath.isMathExpression(numStr)||numStr.equalsIgnoreCase("ALL"))
							{
								int numFactions=CMLib.factions().numFactions();
								if(numStr.equalsIgnoreCase("ALL")) numStr=""+numFactions;
								int num=CMath.s_parseIntExpression(numStr);
								if(num>=numFactions) num=numFactions;
								int tries=500;
								while((q.mysteryData.factionGroup.size()<num)&&(--tries>0))
								{
									int whichFaction=CMLib.dice().roll(1,numFactions,-1);
									int curFaction=0;
									for(Enumeration<Faction> e=CMLib.factions().factions();e.hasMoreElements();curFaction++)
									{
										F=e.nextElement();
										if(curFaction==whichFaction)
											break;
									}
									if(!q.mysteryData.factionGroup.contains(F))
										q.mysteryData.factionGroup.add(F);
								}
							}
							else
							{
								for(int pi=2;pi<p.size();pi++)
								{
									F=CMLib.factions().getFaction(p.elementAt(pi));
									if(F==null) F=CMLib.factions().getFactionByName(p.elementAt(pi));
									if(F==null)
									{
										errorOccurred(q,isQuiet,"Quest '"+name()+"', !factiongroup '"+p.elementAt(pi)+"'.");
										break;
									}
									if(!q.mysteryData.factionGroup.contains(F))
										q.mysteryData.factionGroup.add(F);
								}
								if(q.error) break;
							}
						}
						if((q.mysteryData.factionGroup!=null)
						&&(q.mysteryData.factionGroup.size()>0)
						&&(q.mysteryData.faction==null))
							q.mysteryData.faction=q.mysteryData.factionGroup.get(CMLib.dice().roll(1,q.mysteryData.factionGroup.size(),-1));
					}
					else
					if(cmd.equals("AGENTGROUP"))
					{
						if(q.mysteryData==null) q.mysteryData=new MysteryData();
						if(p.size()<3){
							q.mysteryData.agentGroup=null;
							continue;
						}
						try{
							q.mysteryData.agentGroup=(List)getObjectIfSpecified(p,args,2,1);
							if((q.mysteryData.agentGroup!=null)
							&&(q.mysteryData.agentGroup.size()>0)
							&&(q.mysteryData.agent==null))
								q.mysteryData.agent=q.mysteryData.agentGroup.get(CMLib.dice().roll(1,q.mysteryData.agentGroup.size(),-1));
						}catch(CMException ex){
							q.mysteryData.agentGroup=null;
							String numStr=CMParms.combine(p,2).toUpperCase();
							if(!CMath.isMathExpression(numStr))
							{
								errorOccurred(q,isQuiet,"Quest '"+name()+"', !agentgroup #'"+numStr+"'.");
								break;
							}
							if((q.mobGroup==null)||(q.mobGroup.size()==0))
							{
								errorOccurred(q,isQuiet,"Quest '"+name()+"', !agentgroup mobgroup.");
								break;
							}
							List V=new ArrayList();
							V.addAll(q.mobGroup);
							q.mysteryData.agentGroup=new Vector();
							if(q.mysteryData.agent!=null)
								q.mysteryData.agentGroup.add(q.mysteryData.agent);
							int num=CMath.parseIntExpression(numStr);
							if(num>=V.size()) num=V.size();
							while((q.mysteryData.agentGroup.size()<num)&&(V.size()>0))
							{
								int dex=CMLib.dice().roll(1,V.size(),-1);
								Object O=V.get(dex);
								V.remove(dex);
								q.mysteryData.agentGroup.add((MOB)O);
								if(q.mysteryData.agent==null) q.mysteryData.agent=(MOB)O;
							}
							questifyScriptableBehavs(q.mob);
						}
						q.mob=q.mysteryData.agent;
						if(q.mysteryData.agentGroup!=null)
						{
							q.mobGroup=new Vector<MOB>();
							q.mobGroup.addAll(q.mysteryData.agentGroup);
						}
						q.envObject=q.mysteryData.agentGroup;
					}
					else
					if(cmd.equals("WHEREHAPPENEDGROUP")||cmd.equals("WHEREATGROUP"))
					{
						if(q.mysteryData==null) q.mysteryData=new MysteryData();
						if(cmd.equals("WHEREHAPPENEDGROUP"))
						{
							try{
								q.mysteryData.whereHappenedGroup=(List)getObjectIfSpecified(p,args,2,1);
								q.roomGroup=q.mysteryData.whereHappenedGroup;
								q.mysteryData.whereHappened=((q.roomGroup==null)||(q.roomGroup.size()==0))?null:
															(Room)q.roomGroup.get(CMLib.dice().roll(1,q.roomGroup.size(),-1));
								q.envObject=q.mysteryData.whereHappenedGroup;
								continue;
							}catch(CMException ex){
								q.mysteryData.whereHappenedGroup=null;
							}
						}
						else
						{
							try{
								q.mysteryData.whereAtGroup=(List)getObjectIfSpecified(p,args,2,1);
								q.roomGroup=q.mysteryData.whereAtGroup;
								q.mysteryData.whereAt=((q.roomGroup==null)||(q.roomGroup.size()==0))?null:
													  (Room)q.roomGroup.get(CMLib.dice().roll(1,q.roomGroup.size(),-1));
								q.envObject=q.mysteryData.whereAtGroup;
								continue;
							}catch(CMException ex){
								q.mysteryData.whereAtGroup=null;
							}
						}
						if(p.size()<3) continue;
						String numStr=CMParms.combine(p,2).toUpperCase();
						if(!CMath.isMathExpression(numStr))
						{
							errorOccurred(q,isQuiet,"Quest '"+name()+"', !"+cmd.toLowerCase()+" #'"+numStr+"'.");
							break;
						}
						if((q.roomGroup==null)||(q.roomGroup.size()==0))
						{
							errorOccurred(q,isQuiet,"Quest '"+name()+"', !"+cmd.toLowerCase()+" roomGroup.");
							break;
						}
						List<Room> V=new ArrayList<Room>();
						V.addAll(q.roomGroup);
						List<Room> V2=new Vector();
						Room R=null;
						if(cmd.equals("WHEREHAPPENEDGROUP"))
						{
							q.mysteryData.whereHappenedGroup=V2;
							R=q.mysteryData.whereHappened;
						}
						else
						{
							q.mysteryData.whereAtGroup=null;
							R=q.mysteryData.whereAt;
						}
						if(R!=null) V2.add(R);
						int num=CMath.parseIntExpression(numStr);
						if(num>=V.size()) num=V.size();
						while((V2.size()<num)&&(V.size()>0))
						{
							int dex=CMLib.dice().roll(1,V.size(),-1);
							Room O=V.get(dex);
							V.remove(dex);
							if(!V2.contains(O)) V2.add(O);
							if(R==null) R=O;
						}
						q.roomGroup=new Vector<Room>();
						q.roomGroup.addAll(V2);
						q.room=R;
						q.envObject=q.roomGroup;
						if(cmd.equals("WHEREHAPPENEDGROUP"))
							q.mysteryData.whereHappened=R;
						else
							q.mysteryData.whereAt=R;
					}
					else
					if(cmd.equals("WHENHAPPENEDGROUP")||cmd.equals("WHENATGROUP"))
					{
						if(q.mysteryData==null) q.mysteryData=new MysteryData();
						List V2;
						TimeClock TC=null;
						if(cmd.equals("WHENHAPPENEDGROUP"))
						{
							try{
								q.mysteryData.whenHappenedGroup=(List)getObjectIfSpecified(p,args,2,1);
								V2=q.mysteryData.whenHappenedGroup;
								if((V2!=null)&&(V2.size()>0))
									q.mysteryData.whenHappened=(TimeClock)V2.get(CMLib.dice().roll(1,V2.size(),-1));
								continue;
							}catch(CMException ex){
								q.mysteryData.whenHappenedGroup=null;
							}
						}
						else
						{
							try{
								q.mysteryData.whenAtGroup=(List)getObjectIfSpecified(p,args,2,1);
								V2=q.mysteryData.whenAtGroup;
								if((V2!=null)&&(V2.size()>0))
									q.mysteryData.whenAt=(TimeClock)V2.get(CMLib.dice().roll(1,V2.size(),-1));
								continue;
							}catch(CMException ex){
								q.mysteryData.whenAtGroup=null;
							}
						}
						if(p.size()<3) continue;
						V2=new Vector();
						TimeClock NOW=getMysteryTimeNowFromState();
						if(cmd.equals("WHENHAPPENEDGROUP"))
						{
							q.mysteryData.whenHappenedGroup=V2;
							TC=q.mysteryData.whenHappened;
						}
						else
						{
							q.mysteryData.whenAtGroup=V2;
							TC=q.mysteryData.whenAt;
						}
						for(int pi=2;pi<p.size();pi++)
						{
							String numStr=p.elementAt(pi);
							if(!CMath.isMathExpression(numStr))
							{
								errorOccurred(q,isQuiet,"Quest '"+name()+"', "+cmd.toLowerCase()+" !relative hour #: "+numStr+".");
								break;
							}
							TimeClock TC2=(TimeClock)NOW.copyOf();
							TC2.tickTock(CMath.parseIntExpression(numStr));
							V2.add(TC2);
						}
						if(q.error) break;
						if((V2.size()>0)&&(TC==null))
							TC=(TimeClock)V2.get(CMLib.dice().roll(1,V2.size(),-1));
						if(cmd.equals("WHENHAPPENEDGROUP"))
							q.mysteryData.whenHappened=TC;
						else
							q.mysteryData.whenAt=TC;
					}
					else
					if(cmd.equals("WHENHAPPENED")
					||cmd.equals("WHENAT"))
					{
						if(q.mysteryData==null) q.mysteryData=new MysteryData();
						if(cmd.equals("WHENHAPPENED"))
						{
							try{
								q.mysteryData.whenHappened=(TimeClock)getObjectIfSpecified(p,args,2,0);
								continue;
							}catch(CMException ex){
								q.mysteryData.whenHappened=null;
							}
						}
						else
						{
							try{
								q.mysteryData.whenAt=(TimeClock)getObjectIfSpecified(p,args,2,0);
								continue;
							}catch(CMException ex){
								q.mysteryData.whenAt=null;
							}
						}
						if(p.size()<3) continue;
						TimeClock NOW=getMysteryTimeNowFromState();
						TimeClock TC=null;
						String numStr=CMParms.combine(p,2);
						if(!CMath.isMathExpression(numStr))
						{
							errorOccurred(q,isQuiet,"Quest '"+name()+"', "+cmd.toLowerCase()+" !relative hour #: "+numStr+".");
							break;
						}
						TC=(TimeClock)NOW.copyOf();
						TC.tickTock(CMath.parseIntExpression(numStr));
						if(cmd.equals("WHENHAPPENED"))
							q.mysteryData.whenHappened=TC;
						else
							q.mysteryData.whenAt=TC;
					}
					else
					if(cmd.equals("MOTIVEGROUP")||cmd.equals("ACTIONGROUP"))
					{
						if(q.mysteryData==null) q.mysteryData=new MysteryData();
						List<String> V2=null;
						int num=-1;
						if((p.size()>2)&&(CMath.isMathExpression(p.elementAt(2))))
						{
							num=CMath.s_parseIntExpression(p.elementAt(2));
							p.removeElementAt(2);
						}
						if(cmd.equals("MOTIVEGROUP"))
						{
							try{
								q.mysteryData.motiveGroup=(List)getObjectIfSpecified(p,args,2,1);
								sizeDownTo(q.mysteryData.motiveGroup,num);
								V2=q.mysteryData.motiveGroup;
								if((V2!=null)&&(V2.size()>0))
									q.mysteryData.motive=V2.get(CMLib.dice().roll(1,V2.size(),-1));
								continue;
							}catch(CMException ex){
								q.mysteryData.motiveGroup=null;
							}
						}
						else
						{
							try{
								q.mysteryData.actionGroup=(List)getObjectIfSpecified(p,args,2,1);
								sizeDownTo(q.mysteryData.actionGroup,num);
								V2=q.mysteryData.actionGroup;
								if((V2!=null)&&(V2.size()>0))
									q.mysteryData.action=V2.get(CMLib.dice().roll(1,V2.size(),-1));
								continue;
							}catch(CMException ex){
								q.mysteryData.actionGroup=null;
							}
						}
						if(p.size()<3) continue;
						V2=new Vector();
						String Mstr=null;
						if(cmd.equals("MOTIVEGROUP"))
						{
							q.mysteryData.motiveGroup=V2;
							Mstr=q.mysteryData.motive;
						}
						else
						{
							q.mysteryData.actionGroup=V2;
							Mstr=q.mysteryData.action;
						}
						if(Mstr!=null) V2.add(Mstr);
						for(int pi=2;pi<p.size();pi++)
							if(!V2.contains(p.elementAt(pi)))
								V2.add(p.elementAt(pi));
						sizeDownTo(V2,num);
						if((V2.size()>0)&&(Mstr==null))
							Mstr=V2.get(CMLib.dice().roll(1,V2.size(),-1));
						if(cmd.equals("MOTIVEGROUP"))
							q.mysteryData.motive=Mstr;
						else
							q.mysteryData.action=Mstr;
					}
					else
					if(cmd.equals("MOTIVE"))
					{
						if(q.mysteryData==null) q.mysteryData=new MysteryData();
						if(p.size()<3) continue;
						try{
							q.mysteryData.motive=(String)getObjectIfSpecified(p,args,2,0);
							continue;
						}catch(CMException ex){
							q.mysteryData.motive=null;
						}
						q.mysteryData.motive=CMParms.combine(p,2);
					}
					else
					if(cmd.equals("ACTION"))
					{
						if(q.mysteryData==null) q.mysteryData=new MysteryData();
						if(p.size()<3) continue;
						try{
							q.mysteryData.action=(String)getObjectIfSpecified(p,args,2,0);
							continue;
						}catch(CMException ex){
							q.mysteryData.action=null;
						}
						q.mysteryData.action=CMParms.combine(p,2);
					}
					else
					if(cmd.equals("WHEREHAPPENED")
					||cmd.equals("WHEREAT"))
					{
						if(q.mysteryData==null) q.mysteryData=new MysteryData();
						if(cmd.equals("WHEREHAPPENED"))
						{
							try{
								q.mysteryData.whereHappened=(Room)getObjectIfSpecified(p,args,2,0);
								q.room=q.mysteryData.whereHappened;
								q.envObject=q.room;
								continue;
							}catch(CMException ex){
								q.mysteryData.whereHappened=null;
							}
						}
						else
						{
							try{
								q.mysteryData.whereAt=(Room)getObjectIfSpecified(p,args,2,0);
								q.room=q.mysteryData.whereAt;
								q.envObject=q.room;
								continue;
							}catch(CMException ex){
								q.mysteryData.whereAt=null;
							}
						}
						if(p.size()>2)
						{
							errorOccurred(q,isQuiet,"Quest '"+name()+"', "+cmd.toLowerCase()+" syntax '"+CMParms.combine(p,2)+"'.");
							break;
						}
						if(q.room==null)
						{
							errorOccurred(q,isQuiet,"Quest '"+name()+"', "+cmd.toLowerCase()+" !room.");
							break;
						}
						if(cmd.equals("WHEREHAPPENED"))
							q.mysteryData.whereHappened=q.room;
						else
							q.mysteryData.whereAt=q.room;
						q.envObject=q.room;
					}
					else
					if(cmd.equals("TARGETGROUP")
					||cmd.equals("TOOLGROUP"))
					{
						if(q.mysteryData==null) q.mysteryData=new MysteryData();
						List V2=null;
						if(cmd.equals("TARGETGROUP"))
						{
							try{
								q.mysteryData.targetGroup=(List)getObjectIfSpecified(p,args,2,1);
								V2=q.mysteryData.targetGroup;
								if((V2!=null)&&(V2.size()>0))
								{
									if(V2.get(0) instanceof MOB)
									{
										q.mobGroup=V2;
										q.mob=(MOB)V2.get(CMLib.dice().roll(1,V2.size(),-1));
										q.envObject=q.mobGroup;
										q.mysteryData.target=q.mob;
									}
									if(V2.get(0) instanceof Item)
									{
										q.itemGroup=V2;
										q.item=(Item)V2.get(CMLib.dice().roll(1,V2.size(),-1));
										q.mysteryData.target=q.item;
										q.envObject=q.itemGroup;
									}
								}
								continue;
							}catch(CMException ex){
								q.mysteryData.targetGroup=null;
							}
						}
						else
						{
							try{
								q.mysteryData.toolGroup=(List)getObjectIfSpecified(p,args,2,1);
								V2=q.mysteryData.toolGroup;
								if((V2!=null)&&(V2.size()>0))
								{
									if(V2.get(0) instanceof MOB)
									{
										q.mobGroup=V2;
										q.mob=(MOB)V2.get(CMLib.dice().roll(1,V2.size(),-1));
										q.envObject=q.mobGroup;
										q.mysteryData.tool=q.mob;
									}
									if(V2.get(0) instanceof Item)
									{
										q.itemGroup=V2;
										q.item=(Item)V2.get(CMLib.dice().roll(1,V2.size(),-1));
										q.envObject=q.itemGroup;
										q.mysteryData.tool=q.item;
									}
								}
								continue;
							}catch(CMException ex){
								q.mysteryData.toolGroup=null;
							}
						}
						if(p.size()<3) continue;
						String numStr=CMParms.combine(p,2).toUpperCase();
						if(!CMath.isMathExpression(numStr))
						{
							errorOccurred(q,isQuiet,"Quest '"+name()+"', !"+cmd.toLowerCase()+" #'"+numStr+"'.");
							break;
						}
						if(((q.mobGroup==null)||(q.mobGroup.size()==0))
						&&((q.itemGroup==null)||(q.itemGroup.size()==0)))
						{
							errorOccurred(q,isQuiet,"Quest '"+name()+"', !"+cmd.toLowerCase()+" mobgroup itemgroup.");
							break;
						}
						List V=new ArrayList();
						if((q.mobGroup!=null)&&(q.mobGroup.size()>0))
							V.addAll(q.mobGroup);
						else
							V.addAll(q.itemGroup);
						V2=new Vector();
						Environmental finalE=null;
						if(cmd.equals("TARGETGROUP"))
						{
							q.mysteryData.targetGroup=V2;
							finalE=q.mysteryData.target;
						}
						else
						{
							q.mysteryData.toolGroup=V2;
							finalE=q.mysteryData.tool;
						}
						if(finalE!=null)V2.add(finalE);
						int num=CMath.parseIntExpression(numStr);
						if(num>=V.size()) num=V.size();
						Object O;
						while((V2.size()<num)&&(V.size()>0))
						{
							int dex=CMLib.dice().roll(1,V.size(),-1);
							O=V.get(dex);
							V.remove(dex);
							if(!V2.contains(O)) V2.add(O);
							if(finalE==null) finalE=(Environmental)O;
						}
						if(finalE instanceof MOB)
						{
							q.mobGroup=new Vector<MOB>();
							q.mobGroup.addAll(V2);
							q.mob=(MOB)finalE;
							questifyScriptableBehavs(q.mob); // i just dont get it
						}
						else
						if(finalE instanceof Item)
						{
							q.itemGroup=new Vector<Item>();
							q.itemGroup.addAll(V2);
							q.item=(Item)finalE;
							questifyScriptableBehavs(q.item);
						}
						q.envObject=V2;
						if(cmd.equals("TARGETGROUP"))
							q.mysteryData.target=finalE;
						else
							q.mysteryData.tool=finalE;
					}
					else
					if(cmd.equals("TARGET")
					||cmd.equals("TOOL"))
					{
						if(q.mysteryData==null) q.mysteryData=new MysteryData();
						if(cmd.equals("TARGET"))
						{
							try{
								q.mysteryData.target=(Environmental)getObjectIfSpecified(p,args,2,0);
								if(q.mysteryData.target instanceof MOB)
									q.mob=(MOB)q.mysteryData.target;
								if(q.mysteryData.target instanceof Item)
									q.item=(Item)q.mysteryData.target;
								q.envObject=q.mysteryData.target;
								continue;
							}catch(CMException ex){
								q.mysteryData.target=null;
							}
						}
						else
						{
							try{
								q.mysteryData.tool=(Environmental)getObjectIfSpecified(p,args,2,0);
								if(q.mysteryData.tool instanceof MOB)
									q.mob=(MOB)q.mysteryData.tool;
								if(q.mysteryData.tool instanceof Item)
									q.item=(Item)q.mysteryData.tool;
								q.envObject=q.mysteryData.tool;
								continue;
							}catch(CMException ex){
								q.mysteryData.tool=null;
							}
						}
						if(p.size()>2)
						{
							errorOccurred(q,isQuiet,"Quest '"+name()+"', "+cmd.toLowerCase()+" syntax '"+CMParms.combine(p,2)+"'.");
							break;
						}
						if((q.envObject instanceof List)
						&&(((List)q.envObject).size()>0)
						&&(((List)q.envObject).get(0) instanceof Environmental))
							q.envObject=((List)q.envObject).get(CMLib.dice().roll(1,((List)q.envObject).size(),-1));
						if(!(q.envObject instanceof Environmental))
						{
							errorOccurred(q,isQuiet,"Quest '"+name()+"', "+cmd.toLowerCase()+" !object.");
							break;
						}
						if(cmd.equals("TARGET"))
							q.mysteryData.target=(Environmental)q.envObject;
						else
							q.mysteryData.tool=(Environmental)q.envObject;
						if(q.envObject instanceof MOB)
						{
							q.mob=(MOB)q.envObject;
							questifyScriptableBehavs(q.mob);  // useless, but harmless
						}
						else
						if(q.envObject instanceof Item)
						{
							q.item=(Item)q.envObject;
							questifyScriptableBehavs(q.item);
						}
					}
					else
					if(!isStat(cmd))
					{
						errorOccurred(q,isQuiet,"Quest '"+name()+"', unknown variable '"+cmd+"'.");
						break;
					}
				}
				else
				if(cmd.equals("IMPORT"))
				{
					if(p.size()<2)
					{
						errorOccurred(q,isQuiet,"Quest '"+name()+"', no IMPORT type.");
						break;
					}
					cmd=p.elementAt(1).toUpperCase();
					if(cmd.equals("MOBS"))
					{
						if(p.size()<3)
						{
							errorOccurred(q,isQuiet,"Quest '"+name()+"', no IMPORT MOBS file.");
							break;
						}
						StringBuffer buf=getResourceFileData(CMParms.combine(p,2));
						if((buf==null)||(buf.length()<20))
						{
							errorOccurred(q,isQuiet,"Quest '"+name()+"',Unknown XML file: '"+CMParms.combine(p,2)+"' for '"+name()+"'.");
							break;
						}
						if(buf.substring(0,20).indexOf("<MOBS>")<0)
						{
							errorOccurred(q,isQuiet,"Quest '"+name()+"', Invalid XML file: '"+CMParms.combine(p,2)+"' for '"+name()+"'.");
							break;
						}
						q.loadedMobs=new Vector();
						String errorStr=CMLib.coffeeMaker().addMOBsFromXML(buf.toString(),q.loadedMobs,null);
						if(errorStr.length()>0)
						{
							errorOccurred(q,isQuiet,"Quest '"+name()+"',Error on import of: '"+CMParms.combine(p,2)+"' for '"+name()+"': "+errorStr+".");
							break;
						}
						if(q.loadedMobs.size()<=0)
						{
							errorOccurred(q,isQuiet,"Quest '"+name()+"',No mobs loaded: '"+CMParms.combine(p,2)+"' for '"+name()+"'.");
							break;
						}
					}
					else
					if(cmd.equals("ITEMS"))
					{
						if(p.size()<3)
						{
							errorOccurred(q,isQuiet,"Quest '"+name()+"', no import filename!");
							break;
						}
						StringBuffer buf=getResourceFileData(CMParms.combine(p,2));
						if((buf==null)||(buf.length()<20))
						{
							errorOccurred(q,isQuiet,"Quest '"+name()+"',Unknown XML file: '"+CMParms.combine(p,2)+"' for '"+name()+"'.");
							break;
						}
						if(buf.substring(0,20).indexOf("<ITEMS>")<0)
						{
							errorOccurred(q,isQuiet,"Quest '"+name()+"',Invalid XML file: '"+CMParms.combine(p,2)+"' for '"+name()+"'.");
							break;
						}
						q.loadedItems=new Vector();
						String errorStr=CMLib.coffeeMaker().addItemsFromXML(buf.toString(),q.loadedItems,null);
						if(errorStr.length()>0)
						{
							errorOccurred(q,isQuiet,"Quest '"+name()+"',Error on import of: '"+CMParms.combine(p,2)+"' for '"+name()+"': "+errorStr+".");
							break;
						}
						if(q.loadedItems.size()<=0)
						{
							errorOccurred(q,isQuiet,"Quest '"+name()+"',No items loaded: '"+CMParms.combine(p,2)+"' for '"+name()+"'.");
							break;
						}
					}
					else
					{
						errorOccurred(q,isQuiet,"Quest '"+name()+"', unknown import type '"+cmd+"'.");
						break;
					}
				}
				else
				if(cmd.startsWith("LOAD="))
				{
					boolean error=q.error;
					List args2=new Vector();
					parseQuestScriptWArgs(parseLoadScripts(s,args,args2),args2);
					if((!error)&&(q.error))
						break;
				}
				else
				if(cmd.equals("LOAD"))
				{
					if(p.size()<2)
					{
						errorOccurred(q,isQuiet,"Quest '"+name()+"', unfound type on load.");
						break;
					}
					cmd=p.elementAt(1).toUpperCase();
					if(cmd.equals("MOB")||cmd.equals("MOBGROUP"))
					{
						if(q.loadedMobs.size()==0)
						{
							errorOccurred(q,isQuiet,"Quest '"+name()+"', cannot load mob, no mobs imported.");
							break;
						}
						int maxToLoad=Integer.MAX_VALUE;
						if((p.size()>2)&&(CMath.isMathExpression(p.elementAt(2))))
						{
							maxToLoad=CMath.parseIntExpression(p.elementAt(2));
							p.removeElementAt(2);
						}
						if(p.size()<3)
						{
							errorOccurred(q,isQuiet,"Quest '"+name()+"', no mob name to load!");
							break;
						}
						String mobName=CMParms.combine(p,2);
						String maskStr=CMLib.quests().breakOutMaskString(s,p);
						MaskingLibrary.CompiledZapperMask mask=(maskStr.trim().length()==0)?null:CMLib.masking().maskCompile(maskStr);
						if(mask!=null) mobName=CMParms.combine(p,2).toUpperCase();
						if(mobName.length()==0) mobName="ANY";
						boolean addAll=mobName.equalsIgnoreCase("ALL")
										||mobName.equalsIgnoreCase("ANY");
						List<MOB> choices=new Vector();
						for(int i=0;i<q.loadedMobs.size();i++)
						{
							MOB M2=q.loadedMobs.get(i);
							if((CMLib.masking().maskCheck(mask,M2,true))
							&&(addAll
								||(CMLib.english().containsString(M2.name(),mobName))
								||(CMLib.english().containsString(M2.displayText(),mobName))
								||(CMLib.english().containsString(M2.description(),mobName))))
									choices.add((MOB)M2.copyOf());
						}
						if(choices.size()==0)
						{
							errorOccurred(q,isQuiet,"Quest '"+name()+"', no mob found to load '"+mobName+"'!");
							break;
						}
						List<MOB> mobsToDo=new Vector<MOB>();
						if(cmd.equalsIgnoreCase("MOB"))
							mobsToDo.add(choices.get(CMLib.dice().roll(1,choices.size(),-1)));
						else
						{
							mobsToDo.addAll(choices);
							q.mobGroup=mobsToDo;
						}
						while((mobsToDo.size()>maxToLoad)&&(maxToLoad>0))
							mobsToDo.remove(CMLib.dice().roll(1,mobsToDo.size(),-1));
						while((mobsToDo.size()<maxToLoad)&&(maxToLoad>0)&&(maxToLoad<Integer.MAX_VALUE))
							mobsToDo.add((MOB)mobsToDo.get(CMLib.dice().roll(1,mobsToDo.size(),-1)).copyOf());
						Room choiceRoom=q.room;
						for(int m=0;m<mobsToDo.size();m++)
						{
							q.mob=mobsToDo.get(m);
							q.room=choiceRoom;
							if(q.room==null)
							{
								if(q.roomGroup!=null)
									q.room=q.roomGroup.get(CMLib.dice().roll(1,q.roomGroup.size(),-1));
								else
								if(q.area!=null)
									q.room=q.area.getRandomMetroRoom();
								else
									q.room=CMLib.map().getRandomRoom();
							}
							if(q.room!=null)
							{
								q.mob.setStartRoom(null);
								q.mob.basePhyStats().setRejuv(PhyStats.NO_REJUV);
								q.mob.basePhyStats().setDisposition(q.mob.basePhyStats().disposition()|PhyStats.IS_UNSAVABLE);
								q.mob.recoverPhyStats();
								q.mob.text();
								q.mob.bringToLife(q.room,true);
							}
							questifyScriptableBehavs(q.mob);
							runtimeRegisterObject(q.mob);
							if(q.room!=null)
							{
								q.room.recoverRoomStats();
								q.room.showHappens(CMMsg.MSG_OK_ACTION,null);
							}
							if(q.mob!=null)
								q.mob.setStartRoom(null); // necessary to tell qm to clean him UP!
						}
						q.envObject=mobsToDo;
						if(q.room!=null)
							q.area=q.room.getArea();
					}
					else
					if(cmd.equals("ITEM")||cmd.equals("ITEMGROUP"))
					{
						if(q.loadedItems.size()==0)
						{
							errorOccurred(q,isQuiet,"Quest '"+name()+"', cannot load item, no items imported.");
							break;
						}
						int maxToLoad=Integer.MAX_VALUE;
						if((p.size()>2)&&(CMath.isMathExpression(p.elementAt(2))))
						{
							maxToLoad=CMath.parseIntExpression(p.elementAt(2));
							p.removeElementAt(2);
						}
						if(p.size()<3)
						{
							errorOccurred(q,isQuiet,"Quest '"+name()+"', no item name to load!");
							break;
						}
						String itemName=CMParms.combine(p,2);
						List<Item> choices=new Vector();
						for(int i=0;i<q.loadedItems.size();i++)
						{
							Item I2=q.loadedItems.get(i);
							if((itemName.equalsIgnoreCase("any"))
							||(CMLib.english().containsString(I2.name(),itemName))
							||(CMLib.english().containsString(I2.displayText(),itemName))
							||(CMLib.english().containsString(I2.description(),itemName)))
								choices.add((Item)I2.copyOf());
						}
						if(choices.size()==0)
						{
							errorOccurred(q,isQuiet,"Quest '"+name()+"', no item found to load '"+itemName+"'!");
							break;
						}
						List<Item> itemsToDo=new Vector<Item>();
						if(cmd.equalsIgnoreCase("ITEM"))
							itemsToDo.add(choices.get(CMLib.dice().roll(1,choices.size(),-1)));
						else
						{
							itemsToDo.addAll(choices);
							q.itemGroup=choices;
						}
						while((itemsToDo.size()>maxToLoad)&&(maxToLoad>0))
							itemsToDo.remove(CMLib.dice().roll(1,itemsToDo.size(),-1));
						while((itemsToDo.size()<maxToLoad)&&(maxToLoad>0)&&(maxToLoad<Integer.MAX_VALUE))
							itemsToDo.add((Item)(itemsToDo.get(CMLib.dice().roll(1,itemsToDo.size(),-1))).copyOf());
						Room choiceRoom=q.room;
						for(int m=0;m<itemsToDo.size();m++)
						{
							q.item=itemsToDo.get(m);
							q.room=choiceRoom;
							if(q.room==null)
							{
								if(q.roomGroup!=null)
									q.room=q.roomGroup.get(CMLib.dice().roll(1,q.roomGroup.size(),-1));
								else
								if(q.area!=null)
									q.room=q.area.getRandomMetroRoom();
								else
									q.room=CMLib.map().getRandomRoom();
							}
							if(q.room!=null)
							{
								q.item.basePhyStats().setRejuv(PhyStats.NO_REJUV);
								q.item.basePhyStats().setDisposition(q.item.basePhyStats().disposition()|PhyStats.IS_UNSAVABLE);
								q.item.recoverPhyStats();
								q.item.text();
								q.room.addItem(q.item);
								q.room.recoverRoomStats();
								q.room.showHappens(CMMsg.MSG_OK_ACTION,null);
							}
							questifyScriptableBehavs(q.item);
							runtimeRegisterObject(q.item);
						}
						if(q.room!=null)
							q.area=q.room.getArea();
						q.envObject=itemsToDo;
					}
					else
					{
						errorOccurred(q,isQuiet,"Quest '"+name()+"', unknown load type '"+cmd+"'.");
						break;
					}

				}
				else
				if(cmd.equals("GIVE"))
				{
					if(p.size()<2)
					{
						errorOccurred(q,isQuiet,"Quest '"+name()+"', unfound type on give.");
						break;
					}
					cmd=p.elementAt(1).toUpperCase();
					if(cmd.equals("FOLLOWER"))
					{
						if(q.mob==null)
						{
							errorOccurred(q,isQuiet,"Quest '"+name()+"', cannot give follower, no mob set.");
							break;
						}
						if(p.size()<3)
						{
							errorOccurred(q,isQuiet,"Quest '"+name()+"', cannot give follower, follower name not given.");
							break;
						}
						String mobName=CMParms.combine(p,2);
						List<MOB> choices=new Vector();
						ReverseEnumeration<PreservedQuestObject> e;
						for(e=new ReverseEnumeration<PreservedQuestObject>(q.worldObjects);e.hasMoreElements();)
						{
							PreservedQuestObject PO=e.nextElement();
							if((PO.obj!=q.mob)&&(PO.obj instanceof MOB))
							{
								MOB M2=(MOB)PO.obj;
								if((mobName.equalsIgnoreCase("any"))
								||(CMLib.english().containsString(M2.name(),mobName))
								||(CMLib.english().containsString(M2.displayText(),mobName))
								||(CMLib.english().containsString(M2.description(),mobName)))
									choices.add(M2);
							}
						}
						if(choices.size()==0)
						{
							errorOccurred(q,isQuiet,"Quest '"+name()+"', cannot give follower, no mobs called '"+mobName+"' previously set in script.");
							break;
						}
						MOB M2=choices.get(CMLib.dice().roll(1,choices.size(),-1));
						M2.setFollowing(q.mob);
					}
					else
					if(cmd.equals("ITEM")||(cmd.equalsIgnoreCase("ITEMS")))
					{
						if((q.item==null)&&(q.itemGroup==null)&&(q.loadedItems==null))
						{
							errorOccurred(q,isQuiet,"Quest '"+name()+"', cannot give item(s), no item(s) set or loaded.");
							break;
						}
						if((q.mob==null)&&(q.mobGroup==null))
						{
							errorOccurred(q,isQuiet,"Quest '"+name()+"', cannot give item(s), no mob set.");
							break;
						}
						if(p.size()>2)
						{
							errorOccurred(q,isQuiet,"Quest '"+name()+"', cannot give item(s), parameter unnecessarily given: '"+CMParms.combine(p,2)+"'.");
							break;
						}
						List<MOB> toSet=new Vector<MOB>();
						if(q.mob!=null)
							toSet.add(q.mob);
						else
						if(q.mobGroup!=null)
							toSet.addAll(q.mobGroup);
						List<Item> itemSet=new Vector<Item>();
						if(q.item!=null)
							itemSet.add(q.item);
						else
						if(q.itemGroup!=null)
							itemSet.addAll(q.itemGroup);
						else
						if(q.loadedItems!=null)
							itemSet.addAll(q.loadedItems);
						for(int i=0;i<toSet.size();i++)
						{
							MOB M2=toSet.get(i);
							runtimeRegisterObject(M2);
							if(cmd.equals("ITEMS"))
							{
								for(int i3=0;i3<itemSet.size();i3++)
								{
									Item I3=itemSet.get(i3);
									if(q.item==I3)
									{
										M2.moveItemTo(I3);
										q.item=(Item)q.item.copyOf();
										questifyScriptableBehavs(q.item);
									}
									else
									{
										I3=(Item)I3.copyOf();
										questifyScriptableBehavs(I3);
										M2.moveItemTo(I3);
									}
								}
							}
							else
							if(cmd.equals("ITEM"))
							{
								Item I3=itemSet.get(CMLib.dice().roll(1,itemSet.size(),-1));
								questifyScriptableBehavs(I3);
								if(q.item==I3)
								{
									M2.moveItemTo(I3);
									q.item=(Item)q.item.copyOf();
									questifyScriptableBehavs(q.item);
								}
								else
								{
									I3=(Item)I3.copyOf();
									questifyScriptableBehavs(I3);
									M2.moveItemTo(I3);
								}
							}
						}
					}
					else
					if(cmd.equals("ABILITY"))
					{
						if((q.mob==null)&&(q.mobGroup==null))
						{
							errorOccurred(q,isQuiet,"Quest '"+name()+"', cannot give ability, no mob set.");
							break;
						}
						if(p.size()<3)
						{
							errorOccurred(q,isQuiet,"Quest '"+name()+"', cannot give ability, ability name not given.");
							break;
						}
						Ability A3=CMClass.findAbility(p.elementAt(2));
						if(A3==null)
						{
							errorOccurred(q,isQuiet,"Quest '"+name()+"', cannot give ability, ability name unknown '"+(p.elementAt(2))+".");
							break;
						}
						List<MOB> toSet=new Vector<MOB>();
						if(q.mob!=null)
							toSet.add(q.mob);
						else
						if(q.mobGroup!=null)
							toSet.addAll(q.mobGroup);
						for(int i=0;i<toSet.size();i++)
						{
							MOB M2=toSet.get(i);
							runtimeRegisterAbility(M2,A3.ID(),CMParms.combineWithQuotes(p,3),true);
						}
					}
					else
					if(cmd.equals("BEHAVIOR"))
					{
						if(q.envObject==null)
						{
							errorOccurred(q,isQuiet,"Quest '"+name()+"', cannot give behavior, no mob or item set.");
							break;
						}
						if(p.size()<3)
						{
							errorOccurred(q,isQuiet,"Quest '"+name()+"', cannot give behavior, behavior name not given.");
							break;
						}
						Behavior B=CMClass.getBehavior(p.elementAt(2));
						if(B==null)
						{
							errorOccurred(q,isQuiet,"Quest '"+name()+"', cannot give behavior, behavior name unknown '"+(p.elementAt(2))+".");
							break;
						}
						List toSet=new Vector();
						if(q.envObject instanceof List)
							toSet=(List)q.envObject;
						else
						if(q.envObject!=null)
							toSet.add(q.envObject);
						for(int i=0;i<toSet.size();i++)
						{
							Environmental E2=(Environmental)toSet.get(i);
							if(E2 instanceof PhysicalAgent)
								runtimeRegisterBehavior((PhysicalAgent)E2,B.ID(),CMParms.combineWithQuotes(p,3),true);
						}
					}
					else
					if(cmd.equals("STAT"))
					{
						if(q.envObject==null)
						{
							errorOccurred(q,isQuiet,"Quest '"+name()+"', cannot give stat, no mob or item set.");
							break;
						}
						if(p.size()<3)
						{
							errorOccurred(q,isQuiet,"Quest '"+name()+"', cannot give stat, stat name not given.");
							break;
						}
						String stat=p.elementAt(2);
						String val=CMParms.combineWithQuotes(p,3);
						List toSet=new Vector();
						if(q.envObject instanceof List)
							toSet=(List)q.envObject;
						else
						if(q.envObject!=null)
							toSet.add(q.envObject);
						for(int i=0;i<toSet.size();i++)
						{
							Environmental E2=(Environmental)toSet.get(i);
							if(stat.equalsIgnoreCase("KEYPLAYER") && (E2 instanceof Physical))
							{
								Ability A=((Physical)E2).fetchEffect("QuestBound");
								if(A!=null) A.setStat("KEY",val);
							}
							else
								runtimeRegisterStat(E2,stat,val,true);
						}
					}
					else
					if(cmd.equals("SCRIPT"))
					{
						if(q.envObject==null)
						{
							errorOccurred(q,isQuiet,"Quest '"+name()+"', cannot give script, no object set.");
							break;
						}
						boolean proceed=true;
						boolean savable=false;
						String word=null;
						String scope=CMStrings.replaceAll(name()," ","_").toUpperCase().trim();
						while(proceed&&(p.size()>2))
						{
							word=p.elementAt(2);
							proceed=false;
							if(word.equalsIgnoreCase("SAVABLE"))
							{
								savable=true;
								proceed=true;
							}
							else
							if(word.equalsIgnoreCase("GLOBAL"))
							{
								scope="";
								proceed=true;
							}
							else
							if(word.equalsIgnoreCase("INDIVIDUAL")||word.equals("*"))
							{
								scope="*";
								proceed=true;
							}
							if(proceed) p.removeElementAt(2);
						}
						if(p.size()<3)
						{
							errorOccurred(q,isQuiet,"Quest '"+name()+"', cannot give script, script not given.");
							break;
						}
						String val=CMParms.combineWithQuotes(p,2);
						List toSet=new Vector();
						if(q.envObject instanceof List)
							toSet=(List)q.envObject;
						else
						if(q.envObject!=null)
							toSet.add(q.envObject);
						for(int i=0;i<toSet.size();i++)
						{
							Environmental E2=(Environmental)toSet.get(i);
							if(E2 instanceof PhysicalAgent)
							{
								ScriptingEngine S=(ScriptingEngine)CMClass.getCommon("DefaultScriptingEngine");
								S.setSavable(savable);
								S.registerDefaultQuest(name());
								S.setVarScope(scope);
								S.setScript(val);
								((PhysicalAgent)E2).addScript(S);
								runtimeRegisterObject(((PhysicalAgent)E2));
								synchronized(questState)
								{
									questState.addons.addElement(new XVector(E2,S),Integer.valueOf(questState.preserveState));
								}
							}
						}
					}
					else
					if(cmd.equals("AFFECT"))
					{
						if(q.envObject==null)
						{
							errorOccurred(q,isQuiet,"Quest '"+name()+"', cannot give Effect, no mob, room or item set.");
							break;
						}
						if(p.size()<3)
						{
							errorOccurred(q,isQuiet,"Quest '"+name()+"', cannot give Effect, ability name not given.");
							break;
						}
						Ability A3=CMClass.findAbility(p.elementAt(2));
						if(A3==null)
						{
							errorOccurred(q,isQuiet,"Quest '"+name()+"', cannot give Effect, ability name unknown '"+(p.elementAt(2))+".");
							break;
						}
						List toSet=new Vector();
						if(q.envObject instanceof List)
							toSet=(List)q.envObject;
						else
						if(q.envObject!=null)
							toSet.add(q.envObject);
						for(Object o : toSet)
							if(o instanceof PhysicalAgent)
								runtimeRegisterEffect((PhysicalAgent)o,A3.ID(),CMParms.combineWithQuotes(p,3),true);
					}
					else
					{
						errorOccurred(q,isQuiet,"Quest '"+name()+"', unknown give type '"+cmd+"'.");
						break;
					}
				}
				else
				if(cmd.equals("TAKE"))
				{
					if(p.size()<2)
					{
						errorOccurred(q,isQuiet,"Quest '"+name()+"', unfound type on take.");
						break;
					}
					cmd=p.elementAt(1).toUpperCase();
					if(cmd.equals("ABILITY"))
					{
						if((q.mob==null)&&(q.mobGroup==null))
						{
							errorOccurred(q,isQuiet,"Quest '"+name()+"', cannot take ability, no mob set.");
							break;
						}
						if(p.size()<3)
						{
							errorOccurred(q,isQuiet,"Quest '"+name()+"', cannot take ability, ability name not given.");
							break;
						}
						Ability A3=CMClass.findAbility(p.elementAt(2));
						if(A3==null)
						{
							errorOccurred(q,isQuiet,"Quest '"+name()+"', cannot take ability, ability name unknown '"+(p.elementAt(2))+".");
							break;
						}
						List<MOB> toSet=new Vector<MOB>();
						if(q.mob!=null)
							toSet.add(q.mob);
						else
						if(q.mobGroup!=null)
							toSet.addAll(q.mobGroup);
						for(int i=0;i<toSet.size();i++)
						{
							MOB M2=toSet.get(i);
							runtimeRegisterAbility(M2,A3.ID(),CMParms.combineWithQuotes(p,3),false);
						}
					}
					else
					if(cmd.equals("BEHAVIOR"))
					{
						if(q.envObject==null)
						{
							errorOccurred(q,isQuiet,"Quest '"+name()+"', cannot take behavior, no mob or item set.");
							break;
						}
						if(p.size()<3)
						{
							errorOccurred(q,isQuiet,"Quest '"+name()+"', cannot take behavior, behavior name not given.");
							break;
						}
						Behavior B=CMClass.getBehavior(p.elementAt(2));
						if(B==null)
						{
							errorOccurred(q,isQuiet,"Quest '"+name()+"', cannot take behavior, behavior name unknown '"+(p.elementAt(2))+".");
							break;
						}
						List toSet=new Vector();
						if(q.envObject instanceof List)
							toSet=(List)q.envObject;
						else
						if(q.envObject!=null)
							toSet.add(q.envObject);
						for(int i=0;i<toSet.size();i++)
						{
							Environmental E2=(Environmental)toSet.get(i);
							if(E2 instanceof PhysicalAgent)
								runtimeRegisterBehavior((PhysicalAgent)E2,B.ID(),CMParms.combineWithQuotes(p,3),false);
						}
					}
					else
					if(cmd.equals("AFFECT"))
					{
						if(q.envObject==null)
						{
							errorOccurred(q,isQuiet,"Quest '"+name()+"', cannot take Effect, no mob, room or item set.");
							break;
						}
						if(p.size()<3)
						{
							errorOccurred(q,isQuiet,"Quest '"+name()+"', cannot take Effect, ability name not given.");
							break;
						}
						Ability A3=CMClass.findAbility(p.elementAt(2));
						if(A3==null)
						{
							errorOccurred(q,isQuiet,"Quest '"+name()+"', cannot take Effect, ability name unknown '"+(p.elementAt(2))+".");
							break;
						}
						List toSet=new Vector();
						if(q.envObject instanceof List)
							toSet=(List)q.envObject;
						else
						if(q.envObject!=null)
							toSet.add(q.envObject);
						for(Object o : toSet)
							if(o instanceof PhysicalAgent)
								runtimeRegisterEffect((PhysicalAgent)o,A3.ID(),CMParms.combineWithQuotes(p,3),false);
					}
					else
					{
						errorOccurred(q,isQuiet,"Quest '"+name()+"', unknown take type '"+cmd+"'.");
						break;
					}
				}
				else
				{
					errorOccurred(q,isQuiet,"Quest '"+name()+"', unknown command '"+cmd+"'.");
					break;
				}
				q.done=true;
			}
		}
	}

	public boolean spawnQuest(String script, Vector baseVars, boolean reTime)
	{
		DefaultQuest Q2=(DefaultQuest)CMClass.getCommon("DefaultQuest");
		Q2.setCopy(true);
		Q2.setVars(baseVars,0);
		Q2.setScript(script);
		CMLib.quests().addQuest(Q2);
		if(reTime)
		{
			Long ellapsed=(Long)stepEllapsedTimes.get(script);
			if(ellapsed==null) ellapsed=Long.valueOf(0);
			stepEllapsedTimes.remove(script);
			ellapsed=Long.valueOf(ellapsed.longValue()+(System.currentTimeMillis()-lastStartDateTime));
			stepEllapsedTimes.put(script,ellapsed);
			Q2.resetWaitRemaining(ellapsed.longValue());
			if(Q2.startQuestOnTime())
			{
				stepEllapsedTimes.remove(script);
				return true;
			}
		}
		else
		if(Q2.startQuestInternal())
			return true;
		Q2.enterDormantState();
		return false;
	}


	public boolean startQuest() 
	{
		if((!running())&&(!isCopy()))
			CMLib.coffeeTables().bump(this,CoffeeTableRow.STAT_QUESTSTARTATTEMPT);
		return startQuestInternal();
	}
	
	// this will execute the quest script.  If the quest is running, it
	// will call stopQuestInternal first to shut it down.
	public boolean startQuestInternal()
	{
		if(running()) {
			stopQuestInternal();
			resetData=null;
		}

		List args=new Vector();
		questState=new QuestState();
		Vector baseScript=parseLoadScripts(script(),new Vector(),args);
		if((!isCopy())&&(getSpawn()!=SPAWN_NO))
		{
			if(getSpawn()==SPAWN_FIRST)
				spawnQuest(script(),baseScript,false);
			else
			if(getSpawn()==SPAWN_ANY)
			{
				List<String> parsed=CMLib.quests().parseQuestSteps(baseScript,0,false);
				for(int p=0;p<parsed.size();p++)
					spawnQuest(parsed.get(p),baseScript,true);
			}
			lastStartDateTime=System.currentTimeMillis();
			enterDormantState();
			return false; // always return false, since, per se, this quest is NOT started.
		}
		try{
			parseQuestScript(baseScript,args,0);
		}catch(Exception t){
			questState.error=true;
			Log.errOut("DefaultQuest",t);
		}
		if(questState.error)
		{
			if(!questState.beQuiet)
			{
				int retry=0;
				if((durable)&&(resetData==null))
					retry=10;
				else
				if(resetData!=null)
					retry=resetData[0];
				Log.errOut("Quest","Errors starting '"
						+name()
						+"', quest not started"
						+((retry>0)?", retry in "+retry+".":"."));
			}
			if((durable)&&(resetData==null))
			{
				resetQuest(10);
				return false;
			}
			CMLib.coffeeTables().bump(this,CoffeeTableRow.STAT_QUESTFAILEDSTART);
		}
		else
		if(!questState.done)
			Log.errOut("Quest","Nothing parsed in '"+name()+"', quest not started.");
		else
		if(duration()<0)
		{
			Log.errOut("Quest","No duration, quest '"+name()+"' not started.");
			questState.error=true;
		}
		if((!questState.error)&&(questState.done))
		{
			enterRunningState();
			return true;
		}
		stopQuestInternal();
		return false;
	}

	public void enterRunningState()
	{
		if(duration()>=0)
		{
			waitRemaining=-1;
			if(duration()==0)
				ticksRemaining=1;
			else
			if((resetData!=null)&&(resetData[1]>0))
				ticksRemaining=resetData[1];
			else
				ticksRemaining=duration();
			CMLib.threads().startTickDown(this,Tickable.TICKID_QUEST,1);
		}
		resetData=null;
		lastStartDateTime=System.currentTimeMillis();
		stepEllapsedTimes.remove(script());
	}

	public void cleanQuestStep()
	{
		stoppingQuest=true;
		if(questState.worldObjects.size()>0)
		{
			synchronized(questState)
			{
				Enumeration<PreservedQuestObject> e;
				PreservedQuestObject PO;
				for(e=new ReverseEnumeration<PreservedQuestObject>(questState.worldObjects);e.hasMoreElements();)
				{
					PO=e.nextElement();
					if(PO.preserveState>0)
					{
						PO.preserveState--;
						continue;
					}
					PhysicalAgent P=PO.obj;
					Ability A=P.fetchEffect("QuestBound");
					if(A!=null)P.delEffect(A);
					questState.worldObjects.remove(PO);
					if(P instanceof Item)
					{
						if((CMath.bset(P.basePhyStats().disposition(),PhyStats.IS_UNSAVABLE))
						&&(!((Item)P).amDestroyed()))
							((Item)P).destroy();
					}
					else
					if(P instanceof MOB)
					{
						MOB M=(MOB)P;
						ScriptingEngine B=(ScriptingEngine)M.fetchBehavior("Scriptable");
						if(B!=null)
							B.endQuest(M,M,name());
						Room R=M.getStartRoom();
						if((R==null)||(CMath.bset(M.basePhyStats().disposition(),PhyStats.IS_UNSAVABLE)))
						{
							M.setFollowing(null);
							CMLib.tracking().wanderAway(M,true,false);
							if(M.location()!=null)
								M.location().delInhabitant(M);
							M.setLocation(null);
							M.destroy();
						}
						else
						if((!M.amDead())
						&&(!M.amDestroyed())
						&&((M.location()!=R)||(!R.isInhabitant(M))))
						{
							M.setFollowing(null);
							CMLib.tracking().wanderAway(M,false,true);
						}
					}
				}
			}
			if(questState.addons.size()>0)
			{
				synchronized(questState)
				{
					if(stoppingQuest)
					for(int i=questState.addons.size()-1;i>=0;i--)
					{
						Integer I=(Integer)questState.addons.elementAt(i,2);
						if(I.intValue()>0)
						{
							questState.addons.setElementAt(i,2,Integer.valueOf(I.intValue()-1));
							continue;
						}
						List V=(List)questState.addons.elementAt(i,1);
						questState.addons.removeElementAt(i);
						if(V.size()<2) continue;
						Environmental E=(Environmental)V.get(0);
						Object O=V.get(1);
						if(O instanceof String)
						{
							String stat=(String)O;
							String parms=(String)V.get(2);
							if(CMStrings.contains(E.getStatCodes(),stat.toUpperCase().trim()))
								E.setStat(stat,parms);
							else
							if((E instanceof MOB)&&CMStrings.contains(((Physical)E).basePhyStats().getStatCodes(),stat.toUpperCase().trim()))
							{
								((Physical)E).basePhyStats().setStat(stat.toUpperCase().trim(),parms);
								((Physical)E).recoverPhyStats();
							}
							else
							if((E instanceof MOB)&&(CMStrings.contains(CharStats.CODES.NAMES(),stat.toUpperCase().trim())))
							{
								((MOB)E).baseCharStats().setStat(CMParms.indexOf(CharStats.CODES.NAMES(),stat.toUpperCase().trim()),CMath.s_int(parms));
								((MOB)E).recoverCharStats();
							}
							else
							if((E instanceof MOB)&&CMStrings.contains(((MOB)E).baseState().getStatCodes(),stat))
							{
								((MOB)E).baseState().setStat(stat,parms);
								((MOB)E).recoverMaxState();
								((MOB)E).resetToMaxState();
							}
						}
						else
						if(O instanceof Behavior)
						{
							if(E instanceof PhysicalAgent)
							{
								PhysicalAgent BB=(PhysicalAgent)E;
								Behavior B=BB.fetchBehavior(((Behavior)O).ID());
								if((E instanceof MOB)&&(B instanceof ScriptingEngine))
									((ScriptingEngine)B).endQuest((PhysicalAgent)E,(MOB)E,name());
								if((V.size()>2)&&(V.get(2) instanceof String))
								{
									if(B==null){ B=(Behavior)O; BB.addBehavior(B);}
									B.setParms((String)V.get(2));
								}
								else
								if(B!=null)
									BB.delBehavior(B);
							}
						}
						else
						if(O instanceof ScriptingEngine)
						{
							ScriptingEngine S=(ScriptingEngine)O;
							if((E instanceof MOB)&&(!S.isSavable()))
							{
								S.endQuest((MOB)E,(MOB)E,name());
								((MOB)E).delScript(S);
							}
						}
						else
						if(O instanceof Ability)
						{
							if((V.size()>2)
							&&(V.get(2) instanceof Ability)
							&&(E instanceof MOB))
							{
								Ability A=((MOB)E).fetchAbility(((Ability)O).ID());
								if((V.size()>3)&&(V.get(3) instanceof String))
								{
									if(A==null){A=(Ability)O; ((MOB)E).addAbility(A);}
									A.setMiscText((String)V.get(3));
								}
								else
								if(A!=null)
									((MOB)E).delAbility(A);
							}
							else
							if(E instanceof Physical)
							{
								Ability A=((Physical)E).fetchEffect(((Ability)O).ID());
								if((V.size()>2)&&(V.get(2) instanceof String))
								{
									if(A==null){A=(Ability)O; ((Physical)E).addEffect(A);}
									A.setMiscText((String)V.get(2));
								}
								else
								if(A!=null)
								{
									A.unInvoke();
									((Physical)E).delEffect(A);
								}
							}
						}
						else
						if(O instanceof Item)
							((Item)O).destroy();
					}
				}
			}
		}
		stoppingQuest=false;
	}


	// this will cause a quest to begin parsing its next "step".
	// this will clear out unpreserved objects from previous
	// step and resume quest script processing.
	// if this is the LAST step, stopQuestInternal() is automatically called

	public boolean stepQuest()
	{
		if((questState==null)||(stoppingQuest))
			return false;
		cleanQuestStep();
		ticksRemaining=-1;
		setDuration(-1);

		List args=new Vector();
		Vector script=parseLoadScripts(script(),new Vector(),args);
		try{
			setVars(script,questState.lastLine);
			parseQuestScript(script,args,questState.lastLine);
		}catch(Exception t){
			questState.error=true;
			Log.errOut("DefaultQuest",t);
		}
		if(questState.error)
		{
			if(!questState.beQuiet)
				Log.errOut("Quest","One or more errors in '"+name()+"', quest not started");
		}
		else
		if(!questState.done)
		{
			// valid DONE state, when stepping over the end
		}
		else
		if(duration()<0)
		{
			Log.errOut("Quest","No duration, quest '"+name()+"' not started.");
			questState.error=true;
		}

		if((!questState.error)&&(questState.done)&&(duration()>=0))
		{
			enterRunningState();
			return true;
		}
		stopQuestInternal();
		return false;
	}

	public void resetQuest(int firstPauseTicks) {
		if(stoppingQuest) return;
		// ticksRemaining of -1 is OK, it will grab duration
		resetData=new int[]{firstPauseTicks,ticksRemaining};
		stopQuest();
	}
	
	public void stopQuest()
	{
		if((!stoppingQuest)&&(running()))
			CMLib.coffeeTables().bump(this,CoffeeTableRow.STAT_QUESTSUCCESS);
		stopQuestInternal();
	}
	
	// this will stop executing of the quest script.  It will clean up
	// any objects or mobs which may have been loaded, restoring map
	// mobs to their previous state.
	public void stopQuestInternal()
	{
		if(stoppingQuest) return;
		// first set everything to complete!
		synchronized(questState)
		{
			for(PreservedQuestObject PO : questState.worldObjects)
				PO.preserveState=0;
			for(int q=0;q<questState.addons.size();q++)
				questState.addons.setElementAt(q,2,Integer.valueOf(0));
			questState.autoStepAfterDuration=false;
		}
		cleanQuestStep();
		stoppingQuest=true;
		if(!isCopy())
			setScript(script()); // causes wait times/name to reload
		enterDormantState();
		stoppingQuest=false;
	}

	public boolean enterDormantState()
	{
		ticksRemaining=-1;
		if((isCopy())||(!resetWaitRemaining(0)))
		{
			CMLib.quests().delQuest(this);
			CMLib.threads().deleteTick(this,Tickable.TICKID_QUEST);
			return false;
		}
		return true;
	}

	public boolean resetWaitRemaining(long ellapsedTime)
	{
		
		if(resetData!=null) {
			waitRemaining=resetData[0];
			resetData[0]*=2;
			return true;
		}
		
		if(((minWait()<0)||(maxWait<0))
		&&(startDate().trim().length()==0))
			return false;
		if(running())
			return true;
		if(startDate().length()>0)
		{
			if(startDate().toUpperCase().startsWith("MUDDAY"))
			{
				String sd2=startDate().substring("MUDDAY".length()).trim();
				int x=sd2.indexOf('-');
				if(x<0) return false;
				int mudmonth=CMath.s_parseIntExpression(sd2.substring(0,x));
				int mudday=CMath.s_parseIntExpression(sd2.substring(x+1));
				TimeClock C=(TimeClock)CMClass.getCommon("DefaultTimeClock");
				TimeClock NOW=CMLib.time().globalClock();
				C.setMonth(mudmonth);
				C.setDayOfMonth(mudday);
				C.setTimeOfDay(0);
				if((mudmonth<NOW.getMonth())
				||((mudmonth==NOW.getMonth())&&(mudday<NOW.getDayOfMonth())))
					C.setYear(NOW.getYear()+1);
				else
					C.setYear(NOW.getYear());
				long distance=C.deriveMillisAfter(NOW);
				waitRemaining=(int)(distance/CMProps.getTickMillis());
			}
			else
			{
				int x=startDate.indexOf('-');
				if(x<0) return false;
				int month=CMath.s_parseIntExpression(startDate.substring(0,x));
				int day=CMath.s_parseIntExpression(startDate.substring(x+1));
				int year=Calendar.getInstance().get(Calendar.YEAR);
				long distance=CMLib.time().string2Millis(month+"/"+day+"/"+year+" 12:00 AM");
				while(distance<System.currentTimeMillis())
					distance=CMLib.time().string2Millis(month+"/"+day+"/"+(++year)+" 12:00 AM");
				waitRemaining=(int)((distance-System.currentTimeMillis())/CMProps.getTickMillis());
			}
		}
		else
			waitRemaining=(minWait+(CMLib.dice().roll(1,maxWait,0)))-(int)(ellapsedTime/CMProps.getTickMillis());
		return true;
	}

	public int minWait(){return minWait;}
	public void setMinWait(int wait){minWait=wait;}
	public int waitInterval(){return maxWait;}
	public void setWaitInterval(int wait){maxWait=wait;}
	public int waitRemaining(){return waitRemaining;}

	public Quest getMainQuestObject()
	{
		Quest Q=this;
		if(isCopy())
		{
			Quest Q2=null;
			for(int q=0;q<CMLib.quests().numQuests();q++)
			{
				Q2=CMLib.quests().fetchQuest(q);
				if((Q2!=null)&&(Q2.name().equals(name))&&(!isCopy()))
				{ Q=Q2; break;}
			}
		}
		return Q;
	}

	// if the quest has a winner, this is him.
	public void declareWinner(String name)
	{
		if(name==null) return;
		name=name.trim();
		if(name.length()==0) return;
		Quest Q=getMainQuestObject();
		if(name.startsWith("-"))
		{
			name=name.substring(1);
			if(Q.wasWinner(name))
			{
				List<String> V=Q.getWinners();
				for(int i=0;i<V.size();i++)
				{
					if(V.get(i).equalsIgnoreCase(name))
					{
						V.remove(i);
						break;
					}
				}
				CMLib.database().DBUpdateQuest(Q);
			}
		}
		else
		if(!Q.wasWinner(name))
		{
			Q.getWinners().add(name);
			CMLib.database().DBUpdateQuest(Q);
		}
	}

	public String getWinnerStr()
	{
		Quest Q=getMainQuestObject();
		StringBuffer list=new StringBuffer("");
		List<String> V=Q.getWinners();
		for(int i=0;i<V.size();i++)
			list.append((V.get(i))+";");
		return list.toString();
	}

	public void setWinners(String list)
	{
		Quest Q=getMainQuestObject();
		List<String> V=Q.getWinners();
		V.clear();
		list=list.trim();
		int x=list.indexOf(';');
		while(x>0)
		{
			String s=list.substring(0,x).trim();
			list=list.substring(x+1).trim();
			if(s.length()>0)
				V.add(s);
			x=list.indexOf(';');
		}
		if(list.trim().length()>0)
			V.add(list.trim());
	}

	// retreive the list of previous winners
	public List<String> getWinners()
	{
		Quest Q=getMainQuestObject();
		if(Q==this) return winners;
		return Q.getWinners();
	}

	// was a previous winner
	public boolean wasWinner(String name)
	{
		if(name==null) return false;
		name=name.trim();
		if(name.length()==0) return false;
		Quest Q=getMainQuestObject();
		List<String> V=Q.getWinners();
		for(int i=0;i<V.size();i++)
		{
			if(V.get(i).equalsIgnoreCase(name))
				return true;
		}
		return false;
	}

	// informational
	public boolean running(){return ticksRemaining>=0;}
	public boolean stopping(){return stoppingQuest;}
	public boolean waiting(){return waitRemaining>=0;}
	public int ticksRemaining(){return ticksRemaining;}
	public int minsRemaining(){return (int)(ticksRemaining*CMProps.getTickMillis()/60000);}
	private long tickStatus=Tickable.STATUS_NOT;
	public long getTickStatus(){return tickStatus;}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID!=Tickable.TICKID_QUEST)
			return false;
		if(CMSecurity.isDisabled(CMSecurity.DisFlag.QUESTS)
		||(CMProps.getBoolVar(CMProps.Bool.MUDSHUTTINGDOWN))
		||(!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
		||(suspended()))
			return true;

		tickStatus=Tickable.STATUS_START;
		if(running())
		{
			tickStatus=Tickable.STATUS_ALIVE;
			if(duration()>0)
				ticksRemaining--;
			if(ticksRemaining<0)
			{
				if((questState!=null)&&(questState.autoStepAfterDuration))
				{
					stepQuest();
				}
				else
				{
					CMLib.coffeeTables().bump(this,CoffeeTableRow.STAT_QUESTTIMESTOP);
					stopQuest();
				}
			}
			tickStatus=Tickable.STATUS_END;
		}
		else
		{
			if(startQuestOnTime())
			{
				CMLib.coffeeTables().bump(this,CoffeeTableRow.STAT_QUESTTIMESTART);
			}
		}
		tickStatus=Tickable.STATUS_NOT;
		return true;
	}

	protected boolean startQuestOnTime()
	{
		if((--waitRemaining)>=0) return false;

		boolean allowedToRun=true;
		if(runLevel()>=0)
		for(int q=0;q<CMLib.quests().numQuests();q++)
		{
			Quest Q=CMLib.quests().fetchQuest(q);
			if((!Q.name().equals(name))
			&&(Q.running())
			&&(Q.duration()!=0)
			&&(Q.runLevel()<=runLevel()))
			{ allowedToRun=false; break;}
		}
		if(allowedToRun)
		{
			int numElligiblePlayers=0;
			boolean isMask=(playerMask.length()>0);
			for(Session S : CMLib.sessions().localOnlineIterable())
				if((S.mob()!=null)&&((!isMask) || CMLib.masking().maskCheck(playerMask,S.mob(),true)))
					numElligiblePlayers++;
			ticksRemaining=-1;
			if((numElligiblePlayers>=minPlayers)||(duration()==0))
				return startQuestInternal();
		}
		enterDormantState();
		return false;
	}


	public void runtimeRegisterAbility(MOB mob, String abilityID, String parms, boolean give)
	{
		if(mob==null) return;
		runtimeRegisterObject(mob);
		Vector V=new Vector();
		V.addElement(mob);
		Ability A4=mob.fetchAbility(abilityID);
		if(A4!=null)
		{
			V.addElement(A4);
			V.addElement(A4);
			V.addElement(A4.text());
			if(give)
			{
				A4.setMiscText(parms);
				A4.setProficiency(100);
			}
			else
				mob.delAbility(A4);
		}
		else
		if(!give) return;
		else
		{
			A4=CMClass.getAbility(abilityID);
			if(A4==null) return;
			A4.setMiscText(parms);
			V.addElement(A4);
			V.addElement(A4);
			A4.setProficiency(100);
			mob.addAbility(A4);
		}
		synchronized(questState)
		{
			questState.addons.addElement(V,Integer.valueOf(questState.preserveState));
		}
	}

	public void runtimeRegisterObject(PhysicalAgent P)
	{
		synchronized(questState)
		{
			for(PreservedQuestObject PO : questState.worldObjects)
				if(PO.obj==P)
				{
					if(PO.preserveState<questState.preserveState)
						PO.preserveState=questState.preserveState;
					return;
				}
			questState.worldObjects.add(new PreservedQuestObject(P,questState.preserveState));
			Ability A=CMClass.getAbility("QuestBound");
			A.setMiscText(""+this);
			P.addNonUninvokableEffect(A);
		}
	}

	public void runtimeRegisterEffect(PhysicalAgent affected, String abilityID, String parms, boolean give)
	{
		if(affected==null) return;
		runtimeRegisterObject(affected);
		Vector V=new Vector();
		V.addElement(affected);
		Ability A4=affected.fetchEffect(abilityID);
		if(A4!=null)
		{
			V.addElement(A4);
			V.addElement(A4.text());
			if(give)
			{
				A4.makeLongLasting();
				A4.setMiscText(parms);
			}
			else
				affected.delEffect(A4);
		}
		else
		if(!give) return;
		else
		{
			A4=CMClass.getAbility(abilityID);
			if(A4==null) return;
			V.addElement(A4);
			A4.setMiscText(parms);
			if(affected instanceof MOB)
				A4.startTickDown((MOB)affected,affected,99999);
			else
				A4.startTickDown(null,affected,99999);
			A4.makeLongLasting();
		}
		synchronized(questState)
		{
			questState.addons.addElement(V,Integer.valueOf(questState.preserveState));
		}
	}

	public void runtimeRegisterBehavior(PhysicalAgent behaving, String behaviorID, String parms, boolean give)
	{
		if(behaving==null) return;
		runtimeRegisterObject(behaving);
		Vector V=new Vector();
		V.addElement(behaving);
		Behavior B=behaving.fetchBehavior(behaviorID);
		if(B!=null)
		{
			V.addElement(B);
			V.addElement(B.getParms());
			if(give)
				B.setParms(parms);
			else
				behaving.delBehavior(B);
		}
		else
		if(!give){
			return;
		}
		else
		{
			B=CMClass.getBehavior(behaviorID);
			if(B==null) return;
			V.addElement(B);
			B.setParms(parms);
			behaving.addBehavior(B);
		}
		B.registerDefaultQuest(name());
		synchronized(questState)
		{
			questState.addons.addElement(V,Integer.valueOf(questState.preserveState));
		}
	}

	public void runtimeRegisterStat(Environmental E, String stat, String parms, boolean give)
	{
		if(E==null) return;
		if(E instanceof PhysicalAgent)
			runtimeRegisterObject((PhysicalAgent)E);
		Vector V=new Vector();
		V.addElement(E);
		stat=stat.toUpperCase().trim();
		String oldVal="";
		if(CMStrings.contains(E.getStatCodes(),stat))
			oldVal=E.getStat(stat);
		else
		if((E instanceof Physical)&&CMStrings.contains(((Physical)E).basePhyStats().getStatCodes(),stat))
			oldVal=((Physical)E).basePhyStats().getStat(stat);
		else
		if((E instanceof MOB)&&(CMStrings.contains(CharStats.CODES.NAMES(),stat)))
			oldVal=""+((MOB)E).baseCharStats().getStat(CMParms.indexOf(CharStats.CODES.NAMES(),stat));
		else
		if((E instanceof MOB)&&CMStrings.contains(((MOB)E).baseState().getStatCodes(),stat))
			oldVal=((MOB)E).baseState().getStat(stat);
		V.addElement(stat);
		V.addElement(oldVal);
		if(!give) return;
		V.addElement(stat);
		V.addElement(oldVal);
		if(CMStrings.contains(E.getStatCodes(),stat))
			E.setStat(stat,parms);
		else
		if((E instanceof Physical)&&CMStrings.contains(((Physical)E).basePhyStats().getStatCodes(),stat))
		{
			((Physical)E).basePhyStats().setStat(stat,parms);
			((Physical)E).recoverPhyStats();
		}
		else
		if((E instanceof MOB)&&(CMStrings.contains(CharStats.CODES.NAMES(),stat)))
		{
			((MOB)E).baseCharStats().setStat(CMParms.indexOf(CharStats.CODES.NAMES(),stat),CMath.s_int(parms));
			((MOB)E).recoverCharStats();
		}
		else
		if((E instanceof MOB)&&CMStrings.contains(((MOB)E).baseState().getStatCodes(),stat))
		{
			((MOB)E).baseState().setStat(stat,parms);
			((MOB)E).recoverMaxState();
			((MOB)E).resetToMaxState();
		}
		synchronized(questState)
		{
			questState.addons.addElement(V,Integer.valueOf(questState.preserveState));
		}
	}

	public int getQuestThingIndex(Iterator<? extends Environmental> i, String name, CMClass.CMObjectType type, int[] num)
	{
		if(i==null) return -1;
		Environmental E;
		for(;i.hasNext();)
		{
			E=i.next();
			if(CMClass.isType(E,type))
			{
				switch(type)
				{
				case LOCALE:
					if(CMLib.map().getExtendedRoomID((Room)E).equalsIgnoreCase(name))
						return num[0];
					break;
				default:
					if(E.Name().equalsIgnoreCase(name))
						return num[0];
					break;
				}
				num[0]++;
			}
		}
		return -1;
	}

	public int getQuestMobIndex(String name)
	{
		int[] num={1};
		int x=-1;
		synchronized(questState)
		{
			if(questState.worldObjects!=null)
				x=getQuestThingIndex(PreservedQuestObject.getPOIter(questState.worldObjects),name,CMClass.CMObjectType.MOB,num);
			if((x<0)&&(questState.mobGroup!=null))
				x=getQuestThingIndex(questState.mobGroup.iterator(),name,CMClass.CMObjectType.MOB,num);
			if((x<0)&&(questState.mysteryData!=null)&&(questState.mysteryData.agentGroup!=null))
				x=getQuestThingIndex(questState.mysteryData.agentGroup.iterator(),name,CMClass.CMObjectType.MOB,num);
		}
		return x;
	}

	public int getQuestRoomIndex(String roomID)
	{
		int[] num={1};
		int x=-1;
		synchronized(questState)
		{
			if(questState.worldObjects!=null)
				x=getQuestThingIndex(PreservedQuestObject.getPOIter(questState.worldObjects),roomID,CMClass.CMObjectType.LOCALE,num);
			if((x<0)&&(questState.roomGroup!=null))
				x=getQuestThingIndex(questState.roomGroup.iterator(),roomID,CMClass.CMObjectType.LOCALE,num);
			if(questState.mysteryData!=null)
			{
				if((x<0)&&(questState.mysteryData.whereAtGroup!=null))
					x=getQuestThingIndex(questState.mysteryData.whereAtGroup.iterator(),roomID,CMClass.CMObjectType.LOCALE,num);
				if((x<0)&&(questState.mysteryData.whereHappenedGroup!=null))
					x=getQuestThingIndex(questState.mysteryData.whereHappenedGroup.iterator(),roomID,CMClass.CMObjectType.LOCALE,num);
			}
		}
		return x;
	}

	public int getQuestItemIndex(String name)
	{
		int[] num={1};
		int x=-1;
		synchronized(questState)
		{
			if(questState.worldObjects!=null)
				x=getQuestThingIndex(PreservedQuestObject.getPOIter(questState.worldObjects),name,CMClass.CMObjectType.ITEM,num);
			if((x<0)&&(questState.itemGroup!=null))
				x=getQuestThingIndex(questState.itemGroup.iterator(),name,CMClass.CMObjectType.ITEM,num);
			if(questState.mysteryData!=null)
			{
				if((x<0)&&(questState.mysteryData.toolGroup!=null))
					x=getQuestThingIndex(questState.mysteryData.toolGroup.iterator(),name,CMClass.CMObjectType.ITEM,num);
			}
		}
		return x;
	}

	public long getFlags()
	{
		return suspended()?FLAG_SUSPENDED:0;
	}
	
	public void setFlags(long flags)
	{
		if(CMath.bset(flags,FLAG_SUSPENDED))
		{
			setSuspended(true);
		}
	}
	
	public Environmental getQuestThing(Iterator<? extends Environmental> e, int dex, CMClass.CMObjectType type, int[] num)
	{
		if(e==null) return null;
		Environmental E;
		for(;e.hasNext();)
		{
			E=e.next();
			if(CMClass.isType(E,type))
			{
				if(dex==num[0]) return E;
				num[0]++;
			}
		}
		return null;
	}

	public MOB getQuestMob(int i)
	{
		int[] num={1};
		Environmental E=null;
		synchronized(questState)
		{
			if(questState.worldObjects!=null)
				E=getQuestThing(PreservedQuestObject.getPOIter(questState.worldObjects),i,CMClass.CMObjectType.MOB,num);
			if(E instanceof MOB) return (MOB)E;
			if(questState.mobGroup!=null)
				E=getQuestThing(questState.mobGroup.iterator(),i,CMClass.CMObjectType.MOB,num);
			if(E instanceof MOB) return (MOB)E;
			if((questState.mysteryData!=null)&&(questState.mysteryData.agentGroup!=null))
				E=getQuestThing(questState.mysteryData.agentGroup.iterator(),i,CMClass.CMObjectType.MOB,num);
			if(E instanceof MOB) return (MOB)E;
		}
		return null;
	}

	public Item getQuestItem(int i)
	{
		int[] num={1};
		Environmental E=null;
		synchronized(questState)
		{
			if(questState.worldObjects!=null)
				E=getQuestThing(PreservedQuestObject.getPOIter(questState.worldObjects),i,CMClass.CMObjectType.ITEM,num);
			if(E instanceof Item) return (Item)E;
			if(questState.itemGroup!=null)
				E=getQuestThing(questState.itemGroup.iterator(),i,CMClass.CMObjectType.ITEM,num);
			if(E instanceof Item) return (Item)E;
			if((questState.mysteryData!=null)&&(questState.mysteryData.toolGroup!=null))
				E=getQuestThing(questState.mysteryData.toolGroup.iterator(),i,CMClass.CMObjectType.ITEM,num);
			if(E instanceof Item) return (Item)E;
		}
		return null;
	}

	public Room getQuestRoom(int i)
	{
		int[] num={1};
		Environmental E=null;
		synchronized(questState)
		{
			if(questState.worldObjects!=null)
				E=getQuestThing(PreservedQuestObject.getPOIter(questState.worldObjects),i,CMClass.CMObjectType.LOCALE,num);
			if(E instanceof Room) return (Room)E;
			E=getQuestThing(questState.roomGroup.iterator(),i,CMClass.CMObjectType.LOCALE,num);
			if(E instanceof Room) return (Room)E;
			if((questState.mysteryData!=null)&&(questState.mysteryData.whereAtGroup!=null))
				E=getQuestThing(questState.mysteryData.whereAtGroup.iterator(),i,CMClass.CMObjectType.LOCALE,num);
			if(E instanceof Room) return (Room)E;
			if((questState.mysteryData!=null)&&(questState.mysteryData.whereHappenedGroup!=null))
				E=getQuestThing(questState.mysteryData.whereHappenedGroup.iterator(),i,CMClass.CMObjectType.LOCALE,num);
			if(E instanceof Room) return (Room)E;
		}
		return null;
	}

	public String getQuestMobName(int i)
	{
		MOB M=getQuestMob(i);
		if(M!=null) return M.name();
		return "";
	}

	public String getQuestItemName(int i)
	{
		Item I=getQuestItem(i);
		if(I!=null) return I.name();
		return "";
	}

	public String getQuestRoomID(int i)
	{
		Room R=getQuestRoom(i);
		if(R!=null) return CMLib.map().getExtendedRoomID(R);
		return "";
	}

	public int getObjectInUseIndex(String name)
	{
		synchronized(questState)
		{
			if(questState.worldObjects!=null)
				for(int i=0;i<questState.worldObjects.size();i++)
				{
					PhysicalAgent O=questState.worldObjects.get(i).obj;
					if(O.name().equalsIgnoreCase(name))
						return (i+1);
				}
		}
		return -1;
	}

	public boolean isObjectInUse(Environmental E)
	{
		if(questState.worldObjects!=null)
			for(PreservedQuestObject PO : questState.worldObjects)
				if(PO.obj==E)
					return true;
		return false;
	}

	public Vector parseLoadScripts(String text, List oldArgs, List args)
	{
		if(text.trim().toUpperCase().startsWith("LOAD="))
		{
			String filename=null;
			Vector<String> V=CMParms.parse(text.trim().substring(5).trim());
			if(V.size()>0)
			{
				filename=V.firstElement();
				Vector parms=null;
				try{
					for(int v=1;v<V.size();v++)
					{
						parms=CMParms.parse(V.elementAt(v));
						Object O=getObjectIfSpecified(parms,oldArgs,0,1);
						args.add((O==null)?"":O);
					}
					StringBuffer buf=getResourceFileData(filename);
					if(buf!=null) text=buf.toString();
				}catch(CMException ex){
					Log.errOut("DefaultQuest","'"+text+"' either has a space in the filename, or unknown parms.");
				}
			}
		}
		int x=text.toLowerCase().indexOf(XMLLibrary.FILE_XML_BOUNDARY.toLowerCase());
		if(x>=0)
		{
			String xml=text.substring(x+XMLLibrary.FILE_XML_BOUNDARY.length()).trim();
			text=text.substring(0,x);
			if((xml.length()>0)&&(internalFiles==null))
			{
				List<XMLLibrary.XMLpiece> topXMLV=CMLib.xml().parseAllXML(xml);
				for(int t=0;t<topXMLV.size();t++)
				{
					XMLLibrary.XMLpiece filePiece=topXMLV.get(t);
					String name=null;
					String data=null;
					if(filePiece.tag.equalsIgnoreCase("FILE")&&(filePiece.contents!=null))
						for(int p=0;p<filePiece.contents.size();p++)
						{
							XMLLibrary.XMLpiece piece=filePiece.contents.get(p);
							if(piece.tag.equalsIgnoreCase("NAME")) name=piece.value;
							if(piece.tag.equalsIgnoreCase("DATA")) data=piece.value;
						}
					if((name!=null)&&(data!=null)&&(name.trim().length()>0)&&(data.trim().length()>0))
					{
						if(internalFiles==null) internalFiles=new DVector(2);
						internalFiles.addElement(name.toUpperCase().trim(),new StringBuffer(data));
					}

				}
			}
		}
		Vector script=new Vector();
		Vector V=script;
		while(text.length()>0)
		{
			int y=-1;
			int yy=0;
			while(yy<text.length())
				if((text.charAt(yy)==';')&&((yy<=0)||(text.charAt(yy-1)!='\\'))) {y=yy;break;}
				else
				if(text.charAt(yy)=='\n'){y=yy;break;}
				else
				if(text.charAt(yy)=='\r'){y=yy;break;}
				else yy++;
			String cmd="";
			if(y<0)
			{
				cmd=text.trim();
				text="";
			}
			else
			{
				cmd=text.substring(0,y).trim();
				text=text.substring(y+1).trim();
			}
			if((cmd.length()>0)&&(!cmd.startsWith("#")))
			{
				if(cmd.toUpperCase().startsWith("<OPTION>"))
				{
					V=new Vector();
					script.addElement(V);
				}
				else
				if(cmd.toUpperCase().startsWith("</OPTION>"))
					V=script;
				else
					V.addElement(CMStrings.replaceAll(cmd,"\\;",";").trim());
			}
		}
		return script;
	}

	private static final String VALID_ASTR_CODES="_&|";
	private String modifyStringFromArgs(String s, List args)
	{
		int x=s.toUpperCase().indexOf('$');
		while((x>=0)&&(x<s.length()-1))
		{
			int y=x+1;
			if((y<s.length())&&(VALID_ASTR_CODES.indexOf(s.charAt(y))>=0)) y++;
			while((y<s.length())&&(Character.isLetterOrDigit(s.charAt(y)))) y++;
			try{
				String possObjName=(x+1>=y)?null:s.substring(x+1,y);
				if((possObjName!=null)&&(possObjName.length()>0))
				{
					char firstCode=possObjName.charAt(0);
					if(VALID_ASTR_CODES.indexOf(firstCode)>=0)
						possObjName=possObjName.substring(1);
					Object O=getObjectIfSpecified(CMParms.paramParse(possObjName),args,0,0);
					String replace=(O==null)?"null":O.toString();
					if(O instanceof Room)
						replace=((Room)O).displayText(null);
					else
					if(O instanceof Environmental)
						replace=((Environmental)O).Name();
					else
					if(O instanceof TimeClock)
						replace=((TimeClock)O).getShortTimeDescription();
					switch(firstCode){
					case '_':
						replace=CMStrings.capitalizeAndLower(replace);
						break;
					case '&':
						replace=CMLib.english().cleanArticles(replace);
						break;
					case '|':
						replace=CMLib.english().cleanArticles(replace).trim().replace(' ','|');
						break;
					}
					s=s.substring(0,x)+replace+s.substring(y);
				}
			}catch(CMException ex){}
			x=s.toUpperCase().indexOf('$',x+1);
		}
		return s;
	}

	private Object getObjectIfSpecified(Vector<String> parms, List args, int startp, int object0vector1)
		throws CMException
	{
		if(parms.size()-startp==0) throw new CMException("Not specified");
		StringBuffer allParms=new StringBuffer(parms.elementAt(startp));
		for(int p=startp+1;p<parms.size();p++)
			allParms.append(parms.elementAt(p));
		Object O=null;
		XVector V=new XVector();
		int lastI=0;
		String eval=null;
		char code=' ';
		for(int i=0;i<=allParms.length();i++)
		{
			eval=null;
			if((i==allParms.length())
			||((i<allParms.length())&&((allParms.charAt(i)=='+')||(allParms.charAt(i)=='-')||(allParms.charAt(i)=='#'))))
			{
				eval=allParms.substring(lastI,i).trim().toUpperCase();
				lastI=i+1;
			}
			if(eval!=null)
			{
				if(eval.startsWith("ARG")&&CMath.isMathExpression(eval.substring(3)))
				{
					int num=CMath.parseIntExpression(eval.substring(3));
					if((num<=0)||(num>args.size()))  throw new CMException ("Not specified: "+eval);
					O=args.get(num-1);
				}
				else
				{
					if(!questState.isStat(eval)) throw new CMException ("Not specified: "+eval);
					O=questState.getStat(eval);
				}
				switch(code)
				{
				case '#':
				{
					int index=0;
					if(CMath.isMathExpression(eval))
						index=CMath.parseIntExpression(eval);
					if((index>=0)&&(index<V.size()))
					{
						O=V.elementAt(index);
						V.clear();
						V.addElement(O);
					}
					break;
				}
				case '-':
					if(O instanceof List)
						V.removeAll((List)O);
					else
					if(O!=null)
						V.removeElement(O);
					break;
				case '+': case ' ':
					if(O instanceof List)
						V.addAll((List)O);
					else
					if(O!=null)
						V.addElement(O);
					break;
				}
				if(i<allParms.length()) code=allParms.charAt(i);
			}
		}
		switch(object0vector1)
		{
		case 0:
			if(V.size()==0) return null;
			return V.elementAt(CMLib.dice().roll(1,V.size(),-1));
		case 1:
			if(V.size()==0) return null;
			return V;
		}
		return null;
	}


	public String[] getStatCodes()
	{
		String[] CCODES=new String[QCODES.length+MYSTERY_QCODES.length];
		for(int i=0;i<QCODES.length;i++)
			CCODES[i]=QCODES[i];
		for(int i=0;i<MYSTERY_QCODES.length;i++)
			CCODES[QCODES.length+i]=MYSTERY_QCODES[i];
		return QCODES;
	}

	public int getSaveStatIndex(){return getStatCodes().length;}
	protected int getCodeNum(String code)
	{
		String[] CCODES=getStatCodes();
		for(int i=0;i<CCODES.length;i++)
			if(code.equalsIgnoreCase(CCODES[i])) return i;
		return -1;
	}

	public boolean sameAs(DefaultQuest E)
	{
		String[] CCODES=getStatCodes();
		for(int i=0;i<CCODES.length;i++)
			if(!E.getStat(CCODES[i]).equals(getStat(CCODES[i])))
			   return false;
		return true;
	}

	public void setStat(String code, String val)
	{
		switch(getCodeNum(code)){
		case 0: break;
		case 1: setName(val); break;
		case 2: setDuration(CMLib.time().parseTickExpression(val)); break;
		case 3: setMinWait(CMLib.time().parseTickExpression(val)); break;
		case 4: setMinPlayers(CMath.s_parseIntExpression(val)); break;
		case 5: setPlayerMask(val); break;
		case 6: setRunLevel(CMath.s_parseIntExpression(val)); break;
		case 7: setStartDate(val); break;
		case 8: setStartMudDate(val); break;
		case 9: setWaitInterval(CMLib.time().parseTickExpression(val)); break;
		case 10:
			setSpawn(CMParms.indexOf(SPAWN_DESCS,val.toUpperCase().trim()));
			break;
		case 11: setDisplayName(val); break;
		case 13: durable=CMath.s_bool(val); break;
		case 14: author=val; break;
		case 12: // instructions can and should fall through the default
		default:
			if((code.toUpperCase().trim().equalsIgnoreCase("REMAINING"))&&(running()))
				ticksRemaining=CMLib.time().parseTickExpression(val);
			else
				questState.vars.put(code.toUpperCase().trim(), val);
			break;
		}
	}

	public boolean isStat(String code)
	{
		if((getCodeNum(code)>=0)
		||(questState.vars.containsKey(code)))
			return true;
		return false;
	}

	public String getStat(String code)
	{
		switch(getCodeNum(code)){
		case 0: return ""+ID();
		case 1: return ""+name();
		case 2: return ""+duration();
		case 3: return ""+minWait();
		case 4: return ""+minPlayers();
		case 5: return ""+playerMask();
		case 6: return ""+runLevel();
		case 7: return ""+startDate();
		case 8: return ""+startDate();
		case 9: return ""+waitInterval();
		case 10: return SPAWN_DESCS[getSpawn()];
		case 11: return displayName();
		case 13: return Boolean.toString(durable);
		case 14: return author();
		case 12: // instructions can and should fall through the default
		default:
		{
			code=code.toUpperCase().trim();
			if((code.equalsIgnoreCase("REMAINING"))&&(running()))
				return ""+ticksRemaining;
			if(questState.vars.containsKey(code))
				return questState.vars.get(code);
			if(questState.isStat(code))
			{
				Object O=questState.getStat(code);
				if(O instanceof Room) return ((Room)O).displayText(null);
				if(O instanceof TimeClock) return ((TimeClock)O).getShortTimeDescription();
				if(O instanceof Environmental) return ((Environmental)O).Name();
				if(O instanceof List) return ""+((List)O).size();
				if(O!=null) return O.toString();
			}
			else
			if(code.endsWith("_ROOMID")&&(questState.isStat(code.substring(0,code.length()-7))))
			{
				code=code.substring(0,code.length()-7);
				Object O=questState.getStat(code);
				if(O instanceof List){
					if(((List)O).size()>0)
						O=((List)O).get(CMLib.dice().roll(1,((List)O).size(),-1));
				}
				if(O instanceof Environmental)
				{
					Room R=CMLib.map().roomLocation((Environmental)O);
					if(R!=null) return CMLib.map().getExtendedRoomID(R);
				}
			}
			else
			if(code.endsWith("_CLASS")&&(questState.isStat(code.substring(0,code.length()-6))))
			{
				code=code.substring(0,code.length()-6);
				Object O=questState.getStat(code);
				if(O instanceof List){
					if(((List)O).size()>0)
						O=((List)O).get(CMLib.dice().roll(1,((List)O).size(),-1));
				}
				if(O instanceof CMObject)
					return ((CMObject)O).ID();
				else
				if(O!=null)
					return O.getClass().getName();
			}
			return "";
		}
		}
	}

	public int compareTo(CMObject o)
	{
		return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
	}


	/**
	 * Objects selected for use by a quest can be preserved or released from
	 * quest-state to quest-state.  This object maps a world object in use
	 * to its quest-states remaining.
	 * @author bzimmerman
	 */
	public static class PreservedQuestObject
	{
		public int preserveState;
		public PhysicalAgent obj;
		private static Converter<PreservedQuestObject, PhysicalAgent> converter = new Converter<PreservedQuestObject, PhysicalAgent>() {
			public PhysicalAgent convert(PreservedQuestObject obj) { return obj.obj;}
		};
		public PreservedQuestObject(PhysicalAgent o, int state){ this.obj=o; this.preserveState=state;}
		public static Iterator<PhysicalAgent> getPOIter(List<PreservedQuestObject> o) {
			ConvertingIterator<PreservedQuestObject,PhysicalAgent> iter = 
				 new ConvertingIterator<PreservedQuestObject,PhysicalAgent>(o.iterator(),converter);
			return iter;
		}
	}
	
	/**
	 * A quest state class maps the parse-state of a quest, since quest parsing
	 * is highly state dependent, but the state is not explicit in the script itself.
	 * @author bzimmerman
	 *
	 */
	public static class QuestState implements Cloneable
	{
		public MysteryData mysteryData=new MysteryData();
		public List<MOB> loadedMobs=new Vector<MOB>();
		public List<Item> loadedItems=new Vector<Item>();
		public Area area=null;
		public Room room=null;
		public MOB mob=null;
		public List<MOB> mobGroup=null;
		public List<PhysicalAgent> reselectable=new Vector<PhysicalAgent>();
		public List<Item> itemGroup=null;
		public List<Room> roomGroup=null;
		public Item item=null;
		public Object envObject=null;
		public boolean error=false;
		public boolean done=false;
		public boolean beQuiet=false;
		public boolean autoStepAfterDuration=false;
		public int preserveState=0;
		public int lastLine=0;
		public int startLine;
		public SVector<PreservedQuestObject> worldObjects=new SVector<PreservedQuestObject>();
		// contains a set of vectors, vectors are formatted as such:
		// key 1=vector, below.  key 2=preserveState
		// 0=environmental item/mob/etc
		//  1=Ability, 2=Ability (for an ability added)
		//  1=Ability, 2=Ability, 3=String (for an ability modified)
		//  1=Effect(for an Effect added)
		//  1=Effect, 2=String (for an Effect modified)
		//  1=Behavior (for an Behavior added)
		//  1=Behavior, 2=String (for an Behavior modified)
		public DVector addons=new DVector(2);
		public STreeMap<String,String> vars=new STreeMap<String,String>();

		public boolean isStat(String statName)
		{
			int x=statName.indexOf('#');
			if(x>=0) statName=statName.substring(0,x);
			for(int i=0;i<QOBJS.length;i++)
				if(statName.equalsIgnoreCase(QOBJS[i]))
					return true;
			if(mysteryData!=null)
				return mysteryData.isStat(statName);
			return false;
		}

		public Object getStat(String statName)
		{
			int x=statName.indexOf('#');
			String whichStr=null;
			int whichNum=-1;
			if(x>=0){
				whichStr=statName.substring(x+1);
				if(whichStr.length()>0) whichNum=CMath.s_parseIntExpression(whichStr);
				statName=statName.substring(0,x);
			}
			Object O=null;
			int code=-1;
			for(int i=0;i<QOBJS.length;i++)
				if(statName.equalsIgnoreCase(QOBJS[i]))
				{ code=i; break;}
			switch(code)
			{
			case 0: O=loadedMobs; break;
			case 1: O=loadedItems; break;
			case 2: O=area; break;
			case 3: O=room; break;
			case 4: O=mobGroup; break;
			case 5: O=itemGroup; break;
			case 6: O=roomGroup; break;
			case 7: O=item; break;
			case 8: O=envObject; break;
			case 9: O=new ConvertingList<PreservedQuestObject,PhysicalAgent>(worldObjects,PreservedQuestObject.converter); break;
			case 10: O=mob; break;
			default:
				if(mysteryData!=null)
					O=mysteryData.getStat(statName);
				break;
			}
			if(O instanceof List)
			{
				List V=(List)O;
				if((whichStr!=null)&&((whichNum<=0)||(whichNum>V.size())))
					return ""+V.size();
				if(whichStr!=null)
					return V.get(whichNum-1);
			}
			return O;
		}
	}

	/**
	 * MysteryData is a helper class for QuestState that stores quest
	 * state variables for the parsing of quests that make use of the
	 * "mystery" variables (mystery as in Sherlock Holmes, not as in
	 * unknown).  
	 * @author bzimmerman
	 */
	public static class MysteryData implements Cloneable
	{
		public List<Faction> factionGroup;
		public Faction faction;
		public MOB agent;
		public List<MOB> agentGroup;
		public Environmental target;
		public List<Environmental> targetGroup;
		public Environmental tool;
		public List<Environmental> toolGroup;
		public Room whereHappened;
		public List<Room> whereHappenedGroup;
		public Room whereAt;
		public List<Room> whereAtGroup;
		public String action;
		public List<String> actionGroup;
		public String motive;
		public List<String> motiveGroup;
		public TimeClock whenHappened;
		public List<TimeClock> whenHappenedGroup;
		public TimeClock whenAt;
		public List<TimeClock> whenAtGroup;
		public boolean isStat(String statName)
		{
			for(int i=0;i<MYSTERY_QCODES.length;i++)
				if(statName.equalsIgnoreCase(MYSTERY_QCODES[i]))
					return true;
			return false;
		}
		public Object getStat(String statName)
		{
			int code=-1;
			for(int i=0;i<MYSTERY_QCODES.length;i++)
				if(statName.equalsIgnoreCase(MYSTERY_QCODES[i]))
				{ code=i; break;}
			switch(code){
				case 0: return faction;  case 1: return factionGroup;
				case 2: return agent;  case 3: return agentGroup;
				case 4: return action;  case 5: return actionGroup;
				case 6: return target;  case 7: return targetGroup;
				case 8: return motive;  case 9: return motiveGroup;
				case 10: return whereHappened;  case 11: return whereHappenedGroup;
				case 12: return whereAt;  case 13: return whereAtGroup;
				case 14: return whenHappened;  case 15: return whenHappenedGroup;
				case 16: return whenAt;  case 17: return whenAtGroup;
				case 18: return tool;  case 19: return toolGroup;
			}
			return null;
		}
	}
	
	protected static class JScriptQuest extends ScriptableObject
	{
		public String getClassName(){ return "JScriptQuest";}
		static final long serialVersionUID=44;
		Quest quest=null;
		QuestState state=null;
		public Quest quest(){return quest;}
		public QuestState setupState(){return state;}
		public JScriptQuest(Quest Q, QuestState S){quest=Q; state=S;}
		public static String[] functions = { "quest", "setupState", "toJavaString"};
		public String toJavaString(Object O){return Context.toString(O);}
	}
}
