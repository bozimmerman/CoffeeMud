package com.planet_ink.coffee_mud.common;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class Quests implements Cloneable, Quest
{
	public String ID(){return "Quests";}
	protected static Vector quests=new Vector();
	protected String name="";
	protected int duration=450; // about 30 minutes
	protected String parms="";
	protected Vector stuff=new Vector();
	protected Vector winners=new Vector();
	protected int minWait=-1;
	protected int maxWait=-1;
	protected int waitRemaining=-1;
	protected int ticksRemaining=-1;
	private boolean stoppingQuest=false;

	protected Vector addons=new Vector();
	// contains a set of vectors, vectors are formatted as such:
	// 0=environmental item/mob/etc
	// 1=Ability, 2=Ability (for an ability added)
	// 1=Ability, 2=Ability, 3=String (for an ability modified)
	// 1=Effect(for an Effect added)
	// 1=Effect, 2=String (for an Effect modified)
	// 1=Behavior (for an Behavior added)
	// 1=Behavior, 2=String (for an Behavior modified)

	// the unique name of the quest
	public String name(){return name;}
	public void setName(String newName){name=newName;}

	// the duration, in ticks
	public int duration(){return duration;}
	public void setDuration(int newTicks){duration=newTicks;}

	// the rest of the script.  This may be semicolon-separated instructions,
	// or a LOAD command followed by the quest script path.
	public void setScript(String parm){
		parms=parm;
		setVars(parseScripts(parm));
	}
	public String script(){return parms;}

	public void autostartup()
	{
		if((minWait()<0)||(waitInterval()<0))
			CMClass.ThreadEngine().deleteTick(this,MudHost.TICK_QUEST);
		else
		if(!running())
		{
			waitRemaining=minWait+(Dice.roll(1,maxWait,0));
			CMClass.ThreadEngine().startTickDown(this,MudHost.TICK_QUEST,1);
		}
	}
    
	protected void setVars(Vector script)
	{
		name="";
		duration=-1;
		minWait=-1;
		maxWait=-1;
		for(int v=0;v<script.size();v++)
		{
			String s=(String)script.elementAt(v);
			Vector p=Util.parse(s);
			if(p.size()>0)
			{
				String cmd=((String)p.elementAt(0)).toUpperCase();
				if((cmd.equals("SET"))&&(p.size()>1))
				{
					cmd=((String)p.elementAt(1)).toUpperCase();
					if((cmd.equals("NAME"))&&(p.size()>2))
						setName(Util.combine(p,2));
					else
					if((cmd.equals("DURATION"))&&(p.size()>2))
						setDuration(Util.s_int((String)p.elementAt(2)));
					else
					if((cmd.equals("WAIT"))&&(p.size()>2))
						setMinWait(Util.s_int((String)p.elementAt(2)));
					else
					if((cmd.equals("INTERVAL"))&&(p.size()>2))
						setWaitInterval(Util.s_int((String)p.elementAt(2)));
				}
			}
		}
	}

	public Vector sortSelect(Environmental E, String str,
							 Vector choices,
							 Vector choices0,
							 Vector choices1,
							 Vector choices2,
							 Vector choices3)
	{
		String mname=E.name().toUpperCase();
		String mdisp=E.displayText().toUpperCase();
		String mdesc=E.description().toUpperCase();
		if(str.equalsIgnoreCase("any"))
		{
			choices=choices0;
			choices0.addElement(E);
		}
		else
		if(mname.equalsIgnoreCase(str))
		{
			choices=choices0;
			choices0.addElement(E);
		}
		else
		if(EnglishParser.containsString(mname,str))
		{
			if((choices==null)||(choices==choices2)||(choices==choices3))
				choices=choices1;
			choices1.addElement(E);
		}
		else
		if(EnglishParser.containsString(mdisp,str))
		{
			if((choices==null)||(choices==choices3))
				choices=choices2;
			choices2.addElement(E);
		}
		else
		if(EnglishParser.containsString(mdesc,str))
		{
			if(choices==null) choices=choices3;
			choices3.addElement(E);
		}
		return choices;
	}
	
	
	// this will execute the quest script.  If the quest is running, it
	// will call stopQuest first to shut it down.
	public void startQuest()
	{
		if(running()) stopQuest();
		Vector script=parseScripts(script());
		Vector loadedMobs=new Vector();
		Vector loadedItems=new Vector();
        stuff.clear();
		Area A=null;
		Room R=null;
		MOB M=null;
		Vector MG=null;
		Item I=null;
		Environmental E=null;
		boolean error=false;
		boolean done=false;
		boolean beQuiet=false;
		for(int v=0;v<script.size();v++)
		{
			String s=(String)script.elementAt(v);
			Vector p=Util.parse(s);
			boolean isQuiet=beQuiet;
			if(p.size()>0)
			{
				String cmd=((String)p.elementAt(0)).toUpperCase();
				if(cmd.equals("QUIET"))
				{
					if(p.size()<2)
					{
						beQuiet=true;
						continue;
					}
					isQuiet=true;
					p.removeElementAt(0);
					cmd=((String)p.elementAt(0)).toUpperCase();
				}

                if(cmd.equals("RESET"))
                {
                    if((A==null)&&(R==null))
                    {
                        Log.errOut("Quests","Quest '"+name()+"', no resettable room or area set.");
                        error=true; 
                        break;
                    }
                    if(R==null)
                        CoffeeUtensils.resetArea(A);
                    else
                        CoffeeUtensils.resetRoom(R);
                }
                else
				if(cmd.equals("SET"))
				{
					if(p.size()<2)
					{
						if(!isQuiet)
							Log.errOut("Quests","Quest '"+name()+"', unfound variable on set.");
						error=true; break;
					}
					cmd=((String)p.elementAt(1)).toUpperCase();
					if(cmd.equals("AREA"))
					{
						A=null;
						if(p.size()<3) continue;
                        Vector names=new Vector();
                        Vector areas=new Vector();
                        if((p.size()>3)&&(((String)p.elementAt(2)).equalsIgnoreCase("any")))
                            for(int ip=3;ip<p.size();ip++)
                                names.addElement(p.elementAt(ip));
                        else
                            names.addElement(Util.combine(p,2));
                        for(int n=0;n<names.size();n++)
                        {
                            String areaName=(String)names.elementAt(n);
                            int oldSize=areas.size();
                            if(areaName.equalsIgnoreCase("any"))
                                areas.addElement(CMMap.getRandomArea());
                            if(oldSize==areas.size())
							for (Enumeration e = CMMap.areas(); e.hasMoreElements(); )
							{
								Area A2 = (Area) e.nextElement();
								if (A2.Name().equalsIgnoreCase(areaName))
								{
                                    areas.addElement(A2);
								    break;
								}
							}
                            if(oldSize==areas.size())
							for(Enumeration e=CMMap.areas();e.hasMoreElements();)
							{
								Area A2=(Area)e.nextElement();
								if(EnglishParser.containsString(A2.Name(),areaName))
								{
                                    areas.addElement(A2);
                                    break;
								}
							}
                        }
                        if(areas.size()>0)
                            A=(Area)areas.elementAt(Dice.roll(1,areas.size(),-1));
						if(A==null)
						{
							if(!isQuiet)
								Log.errOut("Quests","Quest '"+name()+"', unknown area '"+Util.combine(p,2)+"'.");
							error=true; break;
						}
					}
					else
					if(cmd.equals("MOBTYPE"))
					{
						M=null;
						if(p.size()<3) continue;
						Vector choices=new Vector();
						Vector mobTypes=Util.parse(Util.combine(p,2).toUpperCase());
						for(int t=0;t<mobTypes.size();t++)
						{
							String mobType=(String)mobTypes.elementAt(t);
							if(mobType.startsWith("-")) continue;
							if(MG==null)
							{
							    try
							    {
									Enumeration e=CMMap.rooms();
									if(A!=null) e=A.getMetroMap();
									for(;e.hasMoreElements();)
									{
										Room R2=(Room)e.nextElement();
										for(int i=0;i<R2.numInhabitants();i++)
										{
											MOB M2=R2.fetchInhabitant(i);
											if((M2!=null)&&(M2.isMonster())&&(objectInUse(M2)==null))
											{
												if(mobType.equalsIgnoreCase("any"))
													choices.addElement(M2);
												else
												if((CMClass.className(M2).toUpperCase().indexOf(mobType)>=0)
												||(M2.charStats().getMyRace().racialCategory().toUpperCase().indexOf(mobType)>=0)
												||(M2.charStats().getMyRace().name().toUpperCase().indexOf(mobType)>=0)
												||(M2.charStats().getCurrentClass().name(M2.charStats().getCurrentClassLevel()).toUpperCase().indexOf(mobType)>=0))
													choices.addElement(M2);
											}
										}
									}
							    }catch(NoSuchElementException e){}
							}
							else
							{
							    try
							    {
									for(Enumeration e=MG.elements();e.hasMoreElements();)
									{
										MOB M2=(MOB)e.nextElement();
										if((M2!=null)&&(M2.isMonster())&&(objectInUse(M2)==null))
										{
											if(mobType.equalsIgnoreCase("any"))
												choices.addElement(M2);
											else
											if((CMClass.className(M2).toUpperCase().indexOf(mobType)>=0)
											||(M2.charStats().getMyRace().racialCategory().toUpperCase().indexOf(mobType)>=0)
											||(M2.charStats().getMyRace().name().toUpperCase().indexOf(mobType)>=0)
											||(M2.charStats().getCurrentClass().name(M2.charStats().getCurrentClassLevel()).toUpperCase().indexOf(mobType)>=0))
												choices.addElement(M2);
										}
									}
							    }catch(NoSuchElementException e){}
							}
						}
						if(choices!=null)
						for(int t=0;t<mobTypes.size();t++)
						{
							String mobType=(String)mobTypes.elementAt(t);
							if(!mobType.startsWith("-")) continue;
							mobType=mobType.substring(1);
							for(int i=choices.size()-1;i>=0;i--)
							{
								MOB M2=(MOB)choices.elementAt(i);
								if((M2!=null)&&(M2.isMonster()))
								{
									if((CMClass.className(M2).toUpperCase().indexOf(mobType)>=0)
									||(M2.charStats().getMyRace().racialCategory().toUpperCase().indexOf(mobType)>=0)
									||(M2.charStats().getMyRace().name().toUpperCase().indexOf(mobType)>=0)
									||(M2.charStats().getCurrentClass().name(M2.charStats().getCurrentClassLevel()).toUpperCase().indexOf(mobType)>=0)
									||(M2.name().toUpperCase().indexOf(mobType)>=0)
									||(M2.displayText().toUpperCase().indexOf(mobType)>=0))
										choices.removeElement(M2);
								}
							}
						}
						if((choices!=null)&&(choices.size()>0))
							M=(MOB)choices.elementAt(Dice.roll(1,choices.size(),-1));
						if(M==null)
						{
							if(!isQuiet)
								Log.errOut("Quests","Quest '"+name()+"', !mob '"+p+"'.");
							error=true; break;
						}
						if(R!=null)
							R.bringMobHere(M,false);
						else
							R=M.location();
						A=R.getArea();
						E=M;
						if(!stuff.contains(M))
							stuff.addElement(M);
						R.recoverRoomStats();
						R.showHappens(CMMsg.MSG_OK_ACTION,null);
					}
					else
					if(cmd.equals("MOBGROUP"))
					{
						MG=null;
						if(p.size()<3) continue;
						Vector choices=null;
						Vector choices0=new Vector();
						Vector choices1=new Vector();
						Vector choices2=new Vector();
						Vector choices3=new Vector();
						String mobName=Util.combine(p,2).toUpperCase();
						String mask="";
                        int x=s.lastIndexOf("MASK=");
                        if(x>=0)
                        {
                            mask=s.substring(x+5).trim();
                            mobName=Util.combine(Util.parse(s.substring(0,x).trim()),2).toUpperCase();
                        }
						if(mobName.length()==0) mobName="ANY";
						try
						{
							Enumeration e=CMMap.rooms();
							if(A!=null) e=A.getMetroMap();
							for(;e.hasMoreElements();)
							{
								Room R2=(Room)e.nextElement();
								for(int i=0;i<R2.numInhabitants();i++)
								{
									MOB M2=R2.fetchInhabitant(i);
									if((M2!=null)&&(M2.isMonster())&&(objectInUse(M2)==null))
									{
										if(!MUDZapper.zapperCheck(mask,M2))
											continue;
										choices=sortSelect(M2,mobName,choices,choices0,choices1,choices2,choices3);
									}
								}
							}
					    }catch(NoSuchElementException e){}
						if((choices!=null)&&(choices.size()>0))
							MG=choices;
						else
						{
							if(!isQuiet)
								Log.errOut("Quests","Quest '"+name()+"', !mobgroup '"+mobName+"'.");
							error=true; break;
						}
					}
					else
					if(cmd.equals("ITEMTYPE"))
					{
						I=null;
						if(p.size()<3) continue;
						Vector choices=new Vector();
						Vector itemTypes=new Vector();
                        for(int i=2;i<p.size();i++)
                            itemTypes.addElement(p.elementAt(i));
						for(int t=0;t<itemTypes.size();t++)
						{
							String itemType=((String)itemTypes.elementAt(t)).toUpperCase();
							if(itemType.startsWith("-")) continue;
							try
							{
								Enumeration e=CMMap.rooms();
								if(A!=null) e=A.getMetroMap();
								for(;e.hasMoreElements();)
								{
									Room R2=(Room)e.nextElement();
									for(int i=0;i<R2.numItems();i++)
									{
										Item I2=R2.fetchItem(i);
										if((I2!=null)&&(objectInUse(I2)==null))
										{
											if(itemType.equalsIgnoreCase("any"))
												choices.addElement(I2);
											else
											if(CMClass.className(I2).toUpperCase().indexOf(itemType)>=0)
												choices.addElement(I2);
										}
									}
								}
						    }catch(NoSuchElementException e){}
						}
						if(choices!=null)
						for(int t=0;t<itemTypes.size();t++)
						{
							String itemType=(String)itemTypes.elementAt(t);
							if(!itemType.startsWith("-")) continue;
							itemType=itemType.substring(1);
							for(int i=choices.size()-1;i>=0;i--)
							{
								Item I2=(Item)choices.elementAt(i);
								if((CMClass.className(I2).toUpperCase().indexOf(itemType)>=0)
								||(I2.name().toUpperCase().indexOf(itemType)>=0)
								||(I2.displayText().toUpperCase().indexOf(itemType)>=0))
									choices.removeElement(I2);
							}
						}
						if((choices!=null)&&(choices.size()>0))
							I=(Item)choices.elementAt(Dice.roll(1,choices.size(),-1));
						if(I==null)
						{
							if(!isQuiet)
								Log.errOut("Quests","Quest '"+name()+"', !item '"+p+"'.");
							error=true; break;
						}
						if(R!=null)
							R.bringItemHere(I,-1);
						else
						if(I.owner() instanceof Room)
							R=(Room)I.owner();
						A=R.getArea();
						E=I;
						R.recoverRoomStats();
						R.showHappens(CMMsg.MSG_OK_ACTION,null);
					}
					else
					if(cmd.equals("LOCALE"))
					{
						R=null;
						if(p.size()<3) continue;
                        Vector names=new Vector();
                        if((p.size()>3)&&(((String)p.elementAt(2)).equalsIgnoreCase("any")))
                            for(int ip=3;ip<p.size();ip++)
                                names.addElement(p.elementAt(ip));
                        else
                            names.addElement(Util.combine(p,2));
						Vector choices=new Vector();
                        for(int n=0;n<names.size();n++)
                        {
                            String localeName=((String)names.elementAt(n)).toUpperCase();
    						try
    						{
    							Enumeration e=CMMap.rooms();
    							if(A!=null) e=A.getMetroMap();
    							for(;e.hasMoreElements();)
    							{
    								Room R2=(Room)e.nextElement();
    								if(localeName.equalsIgnoreCase("any"))
    									choices.addElement(R2);
    								else
    								if(CMClass.className(R2).toUpperCase().indexOf(localeName)>=0)
    									choices.addElement(R2);
    								else
    								{
    									int dom=R2.domainType();
    									if((dom&Room.INDOORS)>0)
    									{
    										if(Room.indoorDomainDescs[dom-Room.INDOORS].indexOf(localeName)>=0)
    											choices.addElement(R2);
    									}
    									else
    									if(Room.outdoorDomainDescs[dom].indexOf(localeName)>=0)
    										choices.addElement(R2);
    								}
    							}
    					    }catch(NoSuchElementException e){}
                        }
						if((choices!=null)&&(choices.size()>0))
							R=(Room)choices.elementAt(Dice.roll(1,choices.size(),-1));
						if(R==null)
						{
							if(!isQuiet)
								Log.errOut("Quests","Quest '"+name()+"', !locale '"+Util.combine(p,2)+"'.");
							error=true; break;
						}
						A=R.getArea();
					}
                    else
                    if(cmd.equals("ROOM"))
                    {
                        R=null;
                        if(p.size()<3) continue;
                        Vector choices=null;
                        Vector choices0=new Vector();
                        Vector choices1=new Vector();
                        Vector choices2=new Vector();
                        Vector choices3=new Vector();
                        Vector names=new Vector();
                        if((p.size()>3)&&(((String)p.elementAt(2)).equalsIgnoreCase("any")))
                            for(int ip=3;ip<p.size();ip++)
                                names.addElement(p.elementAt(ip));
                        else
                            names.addElement(Util.combine(p,2));
                        for(int n=0;n<names.size();n++)
                        {
                            String localeName=((String)names.elementAt(n)).toUpperCase();
                            try
                            {
                                Enumeration e=CMMap.rooms();
                                if(A!=null) e=A.getMetroMap();
                                for(;e.hasMoreElements();)
                                {
                                    Room R2=(Room)e.nextElement();
                                    String display=R2.displayText().toUpperCase();
                                    String desc=R2.description().toUpperCase();
                                    if(localeName.equalsIgnoreCase("any"))
                                    {
                                        choices=choices0;
                                        choices0.addElement(R2);
                                    }
                                    else
                                    if(CMMap.getExtendedRoomID(R2).equalsIgnoreCase(localeName))
                                    {
                                        choices=choices0;
                                        choices0.addElement(R2);
                                    }
                                    else
                                    if(display.equalsIgnoreCase(localeName))
                                    {
                                        if((choices==null)||(choices==choices2)||(choices==choices3))
                                            choices=choices1;
                                        choices1.addElement(R2);
                                    }
                                    else
                                    if(EnglishParser.containsString(display,localeName))
                                    {
                                        if((choices==null)||(choices==choices3))
                                            choices=choices2;
                                        choices2.addElement(R2);
                                    }
                                    else
                                    if(EnglishParser.containsString(desc,localeName))
                                    {
                                        if(choices==null) choices=choices3;
                                        choices3.addElement(R2);
                                    }
                                }
                            }catch(NoSuchElementException e){}
                        }
                        if((choices!=null)&&(choices.size()>0))
                            R=(Room)choices.elementAt(Dice.roll(1,choices.size(),-1));
                        if(R==null)
                        {
                            if(!isQuiet)
                                Log.errOut("Quests","Quest '"+name()+"', !locale '"+Util.combine(p,2)+"'.");
                            error=true; break;
                        }
                        A=R.getArea();
                    }
					else
					if(cmd.equals("MOB"))
					{
						M=null;
						if(p.size()<3) continue;
						Vector choices=null;
						Vector choices0=new Vector();
						Vector choices1=new Vector();
						Vector choices2=new Vector();
						Vector choices3=new Vector();
						String mobName=Util.combine(p,2).toUpperCase();
						String mask="";
						int x=s.lastIndexOf("MASK=");
						if(x>=0)
						{
							mask=s.substring(x+5).trim();
							mobName=Util.combine(Util.parse(s.substring(0,x).trim()),2).toUpperCase();
						}
						if(mobName.length()==0) mobName="ANY";
						if(MG!=null)
						{
							for(Enumeration e=MG.elements();e.hasMoreElements();)
							{
								MOB M2=(MOB)e.nextElement();
								if((M2!=null)&&(M2.isMonster())&&(objectInUse(M2)==null))
								{
									if(!MUDZapper.zapperCheck(mask,M2))
										continue;
									choices=sortSelect(M2,mobName,choices,choices0,choices1,choices2,choices3);
								}
							}
						}
						else
						{
						    try
						    {
								Enumeration e=CMMap.rooms();
								if(A!=null) e=A.getMetroMap();
								for(;e.hasMoreElements();)
								{
									Room R2=(Room)e.nextElement();
									for(int i=0;i<R2.numInhabitants();i++)
									{
										MOB M2=R2.fetchInhabitant(i);
										if((M2!=null)&&(M2.isMonster())&&(objectInUse(M2)==null))
										{
											if(!MUDZapper.zapperCheck(mask,M2))
												continue;
											choices=sortSelect(M2,mobName,choices,choices0,choices1,choices2,choices3);
										}
									}
								}
						    }catch(NoSuchElementException e){}
						}
						if((choices!=null)&&(choices.size()>0))
							M=(MOB)choices.elementAt(Dice.roll(1,choices.size(),-1));
						if(M==null)
						{
							if(!isQuiet)
								Log.errOut("Quests","Quest '"+name()+"', !mob '"+mobName+"'.");
							error=true; break;
						}
						if(R!=null)
							R.bringMobHere(M,false);
						else
							R=M.location();
						A=R.getArea();
						E=M;
						if(!stuff.contains(M))
							stuff.addElement(M);
						R.recoverRoomStats();
						R.showHappens(CMMsg.MSG_OK_ACTION,null);
					}
					else
					if(cmd.equals("ITEM"))
					{
						I=null;
						if(p.size()<3) continue;
						Vector choices=null;
						Vector choices0=new Vector();
						Vector choices1=new Vector();
						Vector choices2=new Vector();
						Vector choices3=new Vector();
						String itemName=Util.combine(p,2).toUpperCase();
						try
						{
							Enumeration e=CMMap.rooms();
							if(A!=null) e=A.getMetroMap();
							for(;e.hasMoreElements();)
							{
								Room R2=(Room)e.nextElement();
								for(int i=0;i<R2.numItems();i++)
								{
									Item I2=R2.fetchItem(i);
									if((I2!=null)&&(objectInUse(I2)==null))
										choices=sortSelect(I2,itemName,choices,choices0,choices1,choices2,choices3);
								}
							}
					    }catch(NoSuchElementException e){}
						if((choices!=null)&&(choices.size()>0))
							I=(Item)choices.elementAt(Dice.roll(1,choices.size(),-1));
						if(I==null)
						{
							if(!isQuiet)
								Log.errOut("Quests","Quest '"+name()+"', !item '"+itemName+"'.");
							error=true; break;
						}
						if(R!=null)
							R.bringItemHere(I,-1);
						else
						if(I.owner() instanceof Room)
							R=(Room)I.owner();
						A=R.getArea();
						E=I;
						R.recoverRoomStats();
						R.showHappens(CMMsg.MSG_OK_ACTION,null);
					}
					else
					if(cmd.equals("NAME")){}
					else
					if(cmd.equals("DURATION")){}
					else
					if(cmd.equals("WAIT")){}
					else
					if(cmd.equals("INTERVAL")){}
					else
					{
						if(!isQuiet)
							Log.errOut("Quests","Quest '"+name()+"', unknown variable '"+cmd+"'.");
						error=true; break;
					}
				}
				else
				if(cmd.equals("IMPORT"))
				{
					if(p.size()<2)
					{
						if(!isQuiet)
							Log.errOut("Quests","Quest '"+name()+"', no IMPORT type.");
						error=true; break;
					}
					cmd=((String)p.elementAt(1)).toUpperCase();
					if(cmd.equals("MOBS"))
					{
						if(p.size()<3)
						{
							if(!isQuiet)
								Log.errOut("Quests","Quest '"+name()+"', no IMPORT MOBS file.");
							error=true; break;
						}
						StringBuffer buf=Resources.getFileResource(Util.combine(p,2));
						if((buf==null)||((buf!=null)&&(buf.length()<20)))
						{
							if(!isQuiet)
								Log.errOut("Quests","Unknown XML file: '"+Util.combine(p,2)+"' for '"+name()+"'.");
							error=true; break;
						}
						if(buf.substring(0,20).indexOf("<MOBS>")<0)
						{
							if(!isQuiet)
								Log.errOut("Quests","Invalid XML file: '"+Util.combine(p,2)+"' for '"+name()+"'.");
							error=true; break;
						}
						loadedMobs=new Vector();
						String errorStr=CoffeeMaker.addMOBsFromXML(buf.toString(),loadedMobs,null);
						if(errorStr.length()>0)
						{
							if(!isQuiet)
								Log.errOut("Quests","Error on import of: '"+Util.combine(p,2)+"' for '"+name()+"': "+errorStr+".");
							error=true; break;
						}
						if(loadedMobs.size()<=0)
						{
							if(!isQuiet)
								Log.errOut("Quests","No mobs loaded: '"+Util.combine(p,2)+"' for '"+name()+"'.");
							error=true; break;
						}
					}
					else
					if(cmd.equals("ITEMS"))
					{
						if(p.size()<3)
						{
							if(!isQuiet)
								Log.errOut("Quests","Quest '"+name()+"', no import filename!");
							error=true; break;
						}
						StringBuffer buf=Resources.getFileResource(Util.combine(p,2));
						if((buf==null)||((buf!=null)&&(buf.length()<20)))
						{
							if(!isQuiet)
								Log.errOut("Quests","Unknown XML file: '"+Util.combine(p,2)+"' for '"+name()+"'.");
							error=true; break;
						}
						if(buf.substring(0,20).indexOf("<ITEMS>")<0)
						{
							if(!isQuiet)
								Log.errOut("Quests","Invalid XML file: '"+Util.combine(p,2)+"' for '"+name()+"'.");
							error=true; break;
						}
						loadedItems=new Vector();
						String errorStr=CoffeeMaker.addItemsFromXML(buf.toString(),loadedItems,null);
						if(errorStr.length()>0)
						{
							if(!isQuiet)
								Log.errOut("Quests","Error on import of: '"+Util.combine(p,2)+"' for '"+name()+"': "+errorStr+".");
							error=true; break;
						}
						if(loadedItems.size()<=0)
						{
							if(!isQuiet)
								Log.errOut("Quests","No items loaded: '"+Util.combine(p,2)+"' for '"+name()+"'.");
							error=true; break;
						}
					}
					else
					{
						if(!isQuiet)
							Log.errOut("Quests","Quest '"+name()+"', unknown import type '"+cmd+"'.");
						error=true; break;
					}
				}
				else
				if(cmd.equals("LOAD"))
				{
					if(p.size()<2)
					{
						if(!isQuiet)
							Log.errOut("Quests","Quest '"+name()+"', unfound type on load.");
						error=true; break;
					}
					cmd=((String)p.elementAt(1)).toUpperCase();
					if(cmd.equals("MOB"))
					{
						if(loadedMobs.size()==0)
						{
							if(!isQuiet)
								Log.errOut("Quests","Quest '"+name()+"', cannot load mob, no mobs imported.");
							error=true; break;
						}
						if(p.size()<3)
						{
							if(!isQuiet)
								Log.errOut("Quests","Quest '"+name()+"', no mob name to load!");
							error=true; break;
						}
						String mobName=Util.combine(p,2);
						String mask="";
                        int x=s.lastIndexOf("MASK=");
                        if(x>=0)
                        {
                            mask=s.substring(x+5).trim();
                            mobName=Util.combine(Util.parse(s.substring(0,x).trim()),2).toUpperCase();
                        }
						if(mobName.length()==0) mobName="ANY";
						Vector choices=new Vector();
						for(int i=0;i<loadedMobs.size();i++)
						{
							MOB M2=(MOB)loadedMobs.elementAt(i);
							if(!MUDZapper.zapperCheck(mask,M2))
								continue;
							if((mobName.equalsIgnoreCase("any"))
							||(EnglishParser.containsString(M2.name(),mobName))
							||(EnglishParser.containsString(M2.displayText(),mobName))
							||(EnglishParser.containsString(M2.description(),mobName)))
								choices.addElement(M2.copyOf());
						}
						if(choices.size()==0)
						{
							if(!isQuiet)
								Log.errOut("Quests","Quest '"+name()+"', no mob found to load '"+mobName+"'!");
							error=true; break;
						}
						M=(MOB)choices.elementAt(Dice.roll(1,choices.size(),-1));
						if(R==null)
						{
							if(A!=null)
								R=A.getRandomMetroRoom();
							else
								R=CMMap.getRandomRoom();
						}
						if(R!=null)
						{
							M.setStartRoom(null);
							M.baseEnvStats().setRejuv(0);
							M.recoverEnvStats();
							M.text();
							M.bringToLife(R,true);
							A=R.getArea();
						}
						E=M;
						if(!stuff.contains(M))
							stuff.addElement(M);
						R.recoverRoomStats();
						R.showHappens(CMMsg.MSG_OK_ACTION,null);
					}
					else
					if(cmd.equals("ITEM"))
					{
						if(loadedItems.size()==0)
						{
							if(!isQuiet)
								Log.errOut("Quests","Quest '"+name()+"', cannot load item, no items imported.");
							error=true; break;
						}
						if(p.size()<3)
						{
							if(!isQuiet)
								Log.errOut("Quests","Quest '"+name()+"', no item name to load!");
							error=true; break;
						}
						String itemName=Util.combine(p,2);
						Vector choices=new Vector();
						for(int i=0;i<loadedItems.size();i++)
						{
							Item I2=(Item)loadedItems.elementAt(i);
							if((itemName.equalsIgnoreCase("any"))
							||(EnglishParser.containsString(I2.name(),itemName))
							||(EnglishParser.containsString(I2.displayText(),itemName))
							||(EnglishParser.containsString(I2.description(),itemName)))
								choices.addElement(I2.copyOf());
						}
						if(choices.size()==0)
						{
							if(!isQuiet)
								Log.errOut("Quests","Quest '"+name()+"', no item found to load '"+itemName+"'!");
							error=true; break;
						}
						I=(Item)choices.elementAt(Dice.roll(1,choices.size(),-1));
						if(R==null)
						{
							if(A!=null)
								R=A.getRandomMetroRoom();
							else
								R=CMMap.getRandomRoom();
						}
						if(R!=null)
						{
							I.baseEnvStats().setRejuv(0);
							I.recoverEnvStats();
							I.text();
							R.addItem(I);
							A=R.getArea();
							R.recoverRoomStats();
							R.showHappens(CMMsg.MSG_OK_ACTION,null);
						}
						E=I;
						if(!stuff.contains(I))
							stuff.addElement(I);

					}
					else
					{
						if(!isQuiet)
							Log.errOut("Quests","Quest '"+name()+"', unknown load type '"+cmd+"'.");
						error=true; break;
					}

				}
				else
				if(cmd.equals("GIVE"))
				{
					if(p.size()<2)
					{
						if(!isQuiet)
							Log.errOut("Quests","Quest '"+name()+"', unfound type on give.");
						error=true; break;
					}
					cmd=((String)p.elementAt(1)).toUpperCase();
					if(cmd.equals("FOLLOWER"))
					{
						if(M==null)
						{
							if(!isQuiet)
								Log.errOut("Quests","Quest '"+name()+"', cannot give follower, no mob set.");
							error=true; break;
						}
						if(p.size()<3)
						{
							if(!isQuiet)
								Log.errOut("Quests","Quest '"+name()+"', cannot give follower, follower name not given.");
							error=true; break;
						}
						String mobName=Util.combine(p,2);
						Vector choices=null;
						for(int i=stuff.size()-1;i>=0;i--)
						{
							Environmental E2=(Environmental)stuff.elementAt(i);
							if((E2!=M)&&(E2 instanceof MOB))
							{
								MOB M2=(MOB)E2;
								if((mobName.equalsIgnoreCase("any"))
								||(EnglishParser.containsString(M2.name(),mobName))
								||(EnglishParser.containsString(M2.displayText(),mobName))
								||(EnglishParser.containsString(M2.description(),mobName)))
									choices.addElement(M2);
							}
						}
						if(choices.size()==0)
						{
							if(!isQuiet)
								Log.errOut("Quests","Quest '"+name()+"', cannot give follower, no mobs called '"+mobName+"' previously set in script.");
							error=true; break;
						}
						MOB M2=(MOB)choices.elementAt(Dice.roll(1,choices.size(),-1));
						M2.setFollowing(M);
					}
					else
					if(cmd.equals("ITEM"))
					{
						if(I==null)
						{
							if(!isQuiet)
								Log.errOut("Quests","Quest '"+name()+"', cannot give item, no item set.");
							error=true; break;
						}
						if((M==null)&&(MG==null))
						{
							if(!isQuiet)
								Log.errOut("Quests","Quest '"+name()+"', cannot give item, no mob set.");
							error=true; break;
						}
						if(p.size()>2)
						{
							if(!isQuiet)
								Log.errOut("Quests","Quest '"+name()+"', cannot give item, parameter unnecessarily given: '"+Util.combine(p,2)+"'.");
							error=true; break;
						}
						Vector toSet=new Vector();
						if(M!=null) 
							toSet.addElement(M);
						else
						if(MG!=null) 
							toSet=MG;
						for(int i=0;i<toSet.size();i++)
						{
							MOB M2=(MOB)toSet.elementAt(i);
							if(!stuff.contains(M2))
								stuff.addElement(M2);
							M2.giveItem(I);
							I=(Item)I.copyOf();
						}
					}
					else
					if(cmd.equals("ABILITY"))
					{
						if((M==null)&&(MG==null))
						{
							if(!isQuiet)
								Log.errOut("Quests","Quest '"+name()+"', cannot give ability, no mob set.");
							error=true; break;
						}
						if(p.size()<3)
						{
							if(!isQuiet)
								Log.errOut("Quests","Quest '"+name()+"', cannot give ability, ability name not given.");
							error=true; break;
						}
						Ability A3=CMClass.findAbility((String)p.elementAt(2));
						if(A3==null)
						{
							if(!isQuiet)
								Log.errOut("Quests","Quest '"+name()+"', cannot give ability, ability name unknown '"+((String)p.elementAt(2))+".");
							error=true; break;
						}
						Vector toSet=new Vector();
						if(M!=null) 
							toSet.addElement(M);
						else
						if(MG!=null) 
							toSet=MG;
						for(int i=0;i<toSet.size();i++)
						{
							MOB M2=(MOB)toSet.elementAt(i);
							if(!stuff.contains(M2))
								stuff.addElement(M2);
							Vector V=new Vector();
							V.addElement(M2);
							Ability A4=(Ability)A3.copyOf();
							if(M2.fetchAbility(A3.ID())!=null)
							{
								A4=M2.fetchAbility(A4.ID());
								V.addElement(A4);
								V.addElement(A4);
								V.addElement(A4.text());
								A4.setMiscText(Util.combineWithQuotes(p,3));
								A4.setProfficiency(100);
							}
							else
							{
								A4.setMiscText(Util.combineWithQuotes(p,3));
								V.addElement(A4);
								V.addElement(A4);
								A4.setProfficiency(100);
								M2.addAbility(A4);
							}
							addons.addElement(V);
						}
					}
					else
					if(cmd.equals("BEHAVIOR"))
					{
						if((E==null)&&(MG==null))
						{
							if(!isQuiet)
								Log.errOut("Quests","Quest '"+name()+"', cannot give behavior, no mob or item set.");
							error=true; break;
						}
						if(p.size()<3)
						{
							if(!isQuiet)
								Log.errOut("Quests","Quest '"+name()+"', cannot give behavior, behavior name not given.");
							error=true; break;
						}
						Behavior B=CMClass.getBehavior((String)p.elementAt(2));
						if(B==null)
						{
							if(!isQuiet)
								Log.errOut("Quests","Quest '"+name()+"', cannot give behavior, behavior name unknown '"+((String)p.elementAt(2))+".");
							error=true; break;
						}
						Vector toSet=new Vector();
						if(E!=null) 
							toSet.addElement(E);
						else
						if(MG!=null) 
							toSet=MG;
						for(int i=0;i<toSet.size();i++)
						{
							Environmental E2=(Environmental)toSet.elementAt(i);
							if(!stuff.contains(E2)) stuff.addElement(E2);
							Vector V=new Vector();
							V.addElement(E2);
							if(E2.fetchBehavior(B.ID())!=null)
							{
								B=E2.fetchBehavior(B.ID());
								V.addElement(B);
								V.addElement(B.getParms());
								B.setParms(Util.combineWithQuotes(p,3));
							}
							else
							{
								V.addElement(B);
								B.setParms(Util.combineWithQuotes(p,3));
								E2.addBehavior(B);
							}
							addons.addElement(V);
						}
					}
					else
					if(cmd.equals("AFFECT"))
					{
						if((E==null)&&(MG==null))
						{
							if(!isQuiet)
								Log.errOut("Quests","Quest '"+name()+"', cannot give Effect, no mob or item set.");
							error=true; break;
						}
						if(p.size()<3)
						{
							if(!isQuiet)
								Log.errOut("Quests","Quest '"+name()+"', cannot give Effect, ability name not given.");
							error=true; break;
						}
						Ability A3=CMClass.findAbility((String)p.elementAt(2));
						if(A3==null)
						{
							if(!isQuiet)
								Log.errOut("Quests","Quest '"+name()+"', cannot give Effect, ability name unknown '"+((String)p.elementAt(2))+".");
							error=true; break;
						}
						Vector toSet=new Vector();
						if(E!=null) 
							toSet.addElement(E);
						else
						if(MG!=null) 
							toSet=MG;
						for(int i=0;i<toSet.size();i++)
						{
							Environmental E2=(Environmental)toSet.elementAt(i);
							if(!stuff.contains(E2))
								stuff.addElement(E2);
							Vector V=new Vector();
							V.addElement(E2);
							Ability A4=(Ability)A3.copyOf();
							if(E2.fetchEffect(A4.ID())!=null)
							{
								A4=E2.fetchEffect(A4.ID());
								V.addElement(A4);
								V.addElement(A4.text());
								A4.makeLongLasting();
								A4.setMiscText(Util.combineWithQuotes(p,3));
							}
							else
							{
								V.addElement(A4);
								A4.setMiscText(Util.combineWithQuotes(p,3));
								if(M!=null)
									A4.startTickDown(M,E2,99999);
								else
									A4.startTickDown(null,E2,99999);
								A4.makeLongLasting();
							}
							addons.addElement(V);
						}
					}
					else
					{
						if(!isQuiet)
							Log.errOut("Quests","Quest '"+name()+"', unknown give type '"+cmd+"'.");
						error=true; break;
					}
				}
				else
				{
					if(!isQuiet)
						Log.errOut("Quests","Quest '"+name()+"', unknown command '"+cmd+"'.");
					error=true; break;
				}
				done=true;
			}
		}
		if(error)
		{
			if(!beQuiet)
				Log.errOut("Quests","One or more errors in '"+name()+"', quest not started");
		}
		else
		if(!done)
			Log.errOut("Quests","Nothing parsed in '"+name()+"', quest not started");
		else
		if(duration()<0)
			Log.errOut("Quests","No duration, quest '"+name()+"' not started.");

		if((!error)&&(done)&&(duration()>=0))
		{
			waitRemaining=-1;
			ticksRemaining=duration();
			CMClass.ThreadEngine().startTickDown(this,MudHost.TICK_QUEST,1);
		}
	}

	public static Quest objectInUse(Environmental E)
	{
		if(E==null) return null;
		for(int q=0;q<numQuests();q++)
		{
			Quest Q=fetchQuest(q);
			if(Q.isQuestObject(E)) return Q;
		}
		return null;
	}
	
	// this will stop executing of the quest script.  It will clean up
	// any objects or mobs which may have been loaded, restoring map
	// mobs to their previous state.
	public void stopQuest()
	{
		if(stoppingQuest) return;
		stoppingQuest=true;
		if(stuff.size()>0)
		{
			for(int i=0;i<stuff.size();i++)
			{
				Environmental E=(Environmental)stuff.elementAt(i);
				if(E instanceof Item)
					((Item)E).destroy();
				else
				if(E instanceof MOB)
				{
					MOB M=(MOB)E;
					Behavior B=((MOB)E).fetchBehavior("Scriptable");
					if(B!=null)	B.modifyBehavior(E,M,"endquest "+name());
					MUDTracker.wanderAway(M,true,false);
					if(M.getStartRoom()!=null)
					{
						if(M.location()!=null)
							M.location().delInhabitant(M);
						M.setLocation(null);
						M.destroy();
						CoffeeUtensils.resetRoom(M.getStartRoom());
					}
					else
					{
						if(M.location()!=null)
							M.location().delInhabitant(M);
						M.setLocation(null);
						M.destroy();
					}
				}
			}
			stuff.clear();
		}
		if(addons.size()>0)
		{
			for(int i=0;i<addons.size();i++)
			{
				Vector V=(Vector)addons.elementAt(i);
				if(V.size()<2) continue;
				Environmental E=(Environmental)V.elementAt(0);
				Object O=V.elementAt(1);
				if(O instanceof Behavior)
				{
					Behavior B=E.fetchBehavior(((Behavior)O).ID());
					if(B==null) continue;
					if((E instanceof MOB)&&(B.ID().equals("Scriptable")))
						B.modifyBehavior(E,(MOB)E,"endquest "+name());
					if((V.size()>2)&&(V.elementAt(2) instanceof String))
						B.setParms((String)V.elementAt(2));
					else
						E.delBehavior(B);
				}
				else
				if(O instanceof Ability)
				{
					if((V.size()>2)
					&&(V.elementAt(2) instanceof Ability)
					&&(E instanceof MOB))
					{
						Ability A=((MOB)E).fetchAbility(((Ability)O).ID());
						if(A==null) continue;
						if((V.size()>3)&&(V.elementAt(3) instanceof String))
							A.setMiscText((String)V.elementAt(3));
						else
							((MOB)E).delAbility(A);
					}
					else
					{
						Ability A=E.fetchEffect(((Ability)O).ID());
						if(A==null) continue;
						if((V.size()>2)&&(V.elementAt(2) instanceof String))
							A.setMiscText((String)V.elementAt(2));
						else
						{
							A.unInvoke();
							E.delEffect(A);
						}
					}
				}
				else
				if(O instanceof Item)
					((Item)O).destroy();
			}
			addons.clear();
		}
		if(running())
		{
			ticksRemaining=-1;
			if((minWait()<0)||(maxWait<0))
				CMClass.ThreadEngine().deleteTick(this,MudHost.TICK_QUEST);
			else
				waitRemaining=minWait+(Dice.roll(1,maxWait,0));
		}
		stoppingQuest=false;
	}

	public int minWait(){return minWait;}
	public void setMinWait(int wait){minWait=wait;}
	public int waitInterval(){return maxWait;}
	public void setWaitInterval(int wait){maxWait=wait;}
	public int waitRemaining(){return waitRemaining;}

	// if the quest has a winner, this is him.
	public void declareWinner(String name)
	{
		name=name.trim();
		if(name.length()==0)
			return;
		if(!wasWinner(name))
		{
			getWinners().addElement(name);
			CMClass.DBEngine().DBUpdateQuest(this);
		}
	}
	public String getWinnerStr()
	{
		StringBuffer list=new StringBuffer("");
		Vector V=getWinners();
		for(int i=0;i<V.size();i++)
			list.append(((String)V.elementAt(i))+";");
		return list.toString();
	}
	public void setWinners(String list)
	{
		Vector V=getWinners();
		V.clear();
		list=list.trim();
		int x=list.indexOf(";");
		while(x>0)
		{
			String s=list.substring(0,x).trim();
			list=list.substring(x+1).trim();
			if(s.length()>0)
				V.addElement(s);
			x=list.indexOf(";");
		}
		if(list.trim().length()>0)
			V.addElement(list.trim());
	}
	// retreive the list of previous winners
	public Vector getWinners()
	{
		return winners;
	}
	// was a previous winner
	public boolean wasWinner(String name)
	{
		Vector V=getWinners();
		for(int i=0;i<V.size();i++)
		{
			if(((String)V.elementAt(i)).equalsIgnoreCase(name))
				return true;
		}
		return false;
	}

	// informational
	public boolean running(){return ticksRemaining>=0;}
	public boolean stopping(){return stoppingQuest;}
	public boolean waiting(){return waitRemaining>=0;}
	public int ticksRemaining(){return ticksRemaining;}
	public int minsRemaining(){return new Long(ticksRemaining*MudHost.TICK_TIME/60000).intValue();}
	private long tickStatus=Tickable.STATUS_NOT;
	public long getTickStatus(){return tickStatus;}
	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID!=MudHost.TICK_QUEST)
			return false;
		if(CMSecurity.isDisabled("QUESTS")) return true;
		
		tickStatus=Tickable.STATUS_START;
		if(running())
		{
			tickStatus=Tickable.STATUS_ALIVE;
			ticksRemaining--;
			if(ticksRemaining<0)
			{
				stopQuest();
				if((minWait()<0)||(maxWait<0))
					return false;
				waitRemaining=minWait+(Dice.roll(1,maxWait,0));
			}
			tickStatus=Tickable.STATUS_END;
		}
		else
		{
			tickStatus=Tickable.STATUS_DEAD;
			waitRemaining--;
			if(waitRemaining<0)
			{
				ticksRemaining=duration();
				startQuest();
			}
		}
		tickStatus=Tickable.STATUS_NOT;
		return true;
	}

	public int wasQuestMob(String name)
	{
		int num=1;
		for(int i=0;i<stuff.size();i++)
		{
			Environmental E=(Environmental)stuff.elementAt(i);
			if(E instanceof MOB)
			{
				if(E.name().equalsIgnoreCase(name))
					return num;
				num++;
			}
		}
		return -1;
	}
	public int wasQuestItem(String name)
	{
		int num=1;
		for(int i=0;i<stuff.size();i++)
		{
			Environmental E=(Environmental)stuff.elementAt(i);
			if(E instanceof Item)
			{
				if(E.name().equalsIgnoreCase(name))
					return num;
				num++;
			}
		}
		return -1;
	}
	
	public boolean isQuestObject(Environmental E)
	{ return ((stuff!=null)&&(stuff.contains(E)));}
	
	public String getQuestObjectName(int i)
	{
		Environmental E=getQuestObject(i);
		if(E!=null) return E.Name();
		return "";
	}
	public Environmental getQuestObject(int i)
	{
		i=i-1; // starts counting at 1
		if((i>=0)&&(i<stuff.size()))
		{
			Environmental E=(Environmental)stuff.elementAt(i);
			return E;
		}
		return null;
	}
	public MOB getQuestMob(int i)
	{
		int num=1;
		for(int x=0;x<stuff.size();x++)
		{
			Environmental E=(Environmental)stuff.elementAt(x);
			if(E instanceof MOB)
			{
				if(num==i) return (MOB)E;
				num++;
			}
		}
		return null;
	}
	public Item getQuestItem(int i)
	{
		int num=1;
		for(int x=0;x<stuff.size();x++)
		{
			Environmental E=(Environmental)stuff.elementAt(x);
			if(E instanceof Item)
			{
				if(num==i) return (Item)E;
				num++;
			}
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
	public int wasQuestObject(String name)
	{
		for(int i=0;i<stuff.size();i++)
		{
			Environmental E=(Environmental)stuff.elementAt(i);
			if(E.name().equalsIgnoreCase(name))
				return (i+1);
		}
		return -1;
	}
	public boolean isQuestObject(String name, int i)
	{
		if((i>=0)&&(i<stuff.size()))
		{
			Environmental E=(Environmental)stuff.elementAt(i);
			if(E.name().equalsIgnoreCase(name))
				return true;
		}
		return false;
	}
	private Vector parseScripts(String text)
	{
		if(text.toUpperCase().startsWith("LOAD="))
		{
			StringBuffer buf=Resources.getFileResource(text.substring(5));
			if(buf!=null) text=buf.toString();
		}
		Vector script=new Vector();
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
				script.addElement(Util.replaceAll(cmd,"\\;",";"));
			}
		}
		return script;
	}

	public static int numQuests(){return quests.size();}
	public static Quests fetchQuest(int i){
		try{
			return (Quests)quests.elementAt(i);
		}catch(Exception e){}
		return null;
	}
	public static Quests fetchQuest(String qname)
	{
		for(int i=0;i<numQuests();i++)
		{
			Quests Q=fetchQuest(i);
			if(Q.name().equalsIgnoreCase(qname))
				return Q;
		}
		return null;
	}
	public static void addQuest(Quest Q)
	{
		if((fetchQuest(Q.name())==null)
		&&(!quests.contains(Q)))
		{
			quests.addElement(Q);
			Q.autostartup();
		}
	}
	public static void shutdown()
	{
		for(int i=numQuests();i>=0;i--)
		{
			Quest Q=fetchQuest(i);
			delQuest(Q);
		}
		quests.clear();
	}
	public static void delQuest(Quest Q)
	{
		if(quests.contains(Q))
		{
			Q.stopQuest();
			CMClass.ThreadEngine().deleteTick(Q,MudHost.TICK_QUEST);
			quests.removeElement(Q);
		}
	}
	public static void save()
	{
		CMClass.DBEngine().DBUpdateQuests(quests);
	}
}
