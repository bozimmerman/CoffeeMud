package com.planet_ink.coffee_mud.common;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;

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
	
	// the unique name of the quest
	public String name(){return name;}
	public void setName(String newName){name=newName;}
	
	// the duration, in ticks
	public int duration(){return duration;}
	public void setDuration(int newTicks){duration=newTicks;}
	
	// the rest of the script.  This may be semicolon-seperated instructions, 
	// or a LOAD command followed by the quest script path.
	public void setScript(String parm){
		parms=parm;
		setVars(parseScripts(parm));
	}
	public String script(){return parms;}

	public void autostartup()
	{
		if((minWait()<0)||(waitInterval()<0))
			ExternalPlay.deleteTick(this,Host.QUEST_TICK);
		else
		if(!running())
		{
			waitRemaining=minWait+(Dice.roll(1,maxWait,0));
			ExternalPlay.startTickDown(this,Host.QUEST_TICK,1);
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
	
	// this will execute the quest script.  If the quest is running, it 
	// will call stopQuest first to shut it down.
	public void startQuest()
	{
		if(running()) stopQuest();
		Vector script=parseScripts(script());
		Vector loadedMobs=new Vector();
		Vector loadedItems=new Vector();
		Area A=null;
		Room R=null;
		MOB M=null;
		Item I=null;
		Environmental E=null;
		boolean done=false;
		for(int v=0;v<script.size();v++)
		{
			String s=(String)script.elementAt(v);
			Vector p=Util.parse(s);
			if(p.size()>0)
			{
				String cmd=((String)p.elementAt(0)).toUpperCase();
				if(cmd.equals("SET"))
				{
					if(p.size()<2)
					{
						Log.errOut("Quest '"+name()+"', unfound variable on set.");
						continue;
					}
					cmd=((String)p.elementAt(1)).toUpperCase();
					if(cmd.equals("AREA"))
					{
						A=null;
						if(p.size()<3) continue;
						String areaName=Util.combine(p,2);
						if(areaName.equalsIgnoreCase("any"))
							A=CMMap.getRandomArea();
						else
						for(Enumeration e=CMMap.areas();e.hasMoreElements();)
						{
							Area A2=(Area)e.nextElement();
							if(CoffeeUtensils.containsString(A2.name(),areaName))
							{
								A=A2; break;
							}
						}
						if(A==null)
							Log.errOut("Quest '"+name()+"', unknown area '"+areaName+"'.");
					}
					else
					if(cmd.equals("MOBTYPE"))
					{
						M=null;
						if(p.size()<3) continue;
						Vector choices=new Vector();
						String mobType=Util.combine(p,2).toUpperCase();
						Enumeration e=CMMap.rooms();
						if(A!=null) e=A.getMap();
						for(;e.hasMoreElements();)
						{
							Room R2=(Room)e.nextElement();
							for(int i=0;i<R2.numInhabitants();i++)
							{
								MOB M2=R2.fetchInhabitant(i);
								if(M2!=null)
								{
									if(mobType.equalsIgnoreCase("any"))
										choices.addElement(M2);
									else
									if((CMClass.className(M2).toUpperCase().indexOf(mobType)>=0)
									||(M2.charStats().getMyRace().racialCategory().toUpperCase().indexOf(mobType)>=0)
									||(M2.charStats().getMyRace().name().toUpperCase().indexOf(mobType)>=0)
									||(M2.charStats().getCurrentClass().name().toUpperCase().indexOf(mobType)>=0))
										choices.addElement(M2);
								}
							}
						}
						if(choices.size()>0)
							M=(MOB)choices.elementAt(Dice.roll(1,choices.size(),-1));
						if(M==null)
						{
							Log.errOut("Quest '"+name()+"', unfound mobtype '"+mobType+"'.");
							if((E!=null)&&(E instanceof MOB)) E=I;
						}
						else
						{
							if(R!=null)
								R.bringMobHere(M,false);
							else
								R=M.location();
							A=R.getArea();
							E=M;
							if(!stuff.contains(M))
								stuff.addElement(M);
						}
					}
					else
					if(cmd.equals("ITEMTYPE"))
					{
						I=null;
						if(p.size()<3) continue;
						Vector choices=new Vector();
						String itemType=Util.combine(p,2).toUpperCase();
						Enumeration e=CMMap.rooms();
						if(A!=null) e=A.getMap();
						for(;e.hasMoreElements();)
						{
							Room R2=(Room)e.nextElement();
							for(int i=0;i<R2.numItems();i++)
							{
								Item I2=R2.fetchItem(i);
								if(I2!=null)
								{
									if(itemType.equalsIgnoreCase("any"))
										choices.addElement(I2);
									else
									if(CMClass.className(I2).toUpperCase().indexOf(itemType)>=0)
										choices.addElement(I2);
								}
							}
						}
						if(choices.size()>0)
							I=(Item)choices.elementAt(Dice.roll(1,choices.size(),-1));
						if(I==null)
						{
							Log.errOut("Quest '"+name()+"', unfound itemtype '"+itemType+"'.");
							if((E!=null)&&(E instanceof Item)) E=M;
						}
						else
						{
							if(R!=null)
								R.bringItemHere(I);
							else
							if(I.owner() instanceof Room)
								R=(Room)I.owner();
							A=R.getArea();
							E=I;
						}
					}
					else
					if(cmd.equals("LOCALE"))
					{
						R=null;
						if(p.size()<3) continue;
						String localeName=Util.combine(p,2).toUpperCase();
						Vector choices=new Vector();
						Enumeration e=CMMap.rooms();
						if(A!=null) e=A.getMap();
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
						if(choices.size()>0)
							R=(Room)choices.elementAt(Dice.roll(1,choices.size(),-1));
						if(R==null)
							Log.errOut("Quest '"+name()+"', unknown locale '"+localeName+"'.");
						else
							A=R.getArea();
					}
					else
					if(cmd.equals("ROOM"))
					{
						R=null;
						if(p.size()<3) continue;
						String localeName=Util.combine(p,2).toUpperCase();
						Vector choices=null;
						Vector choices0=new Vector();
						Vector choices1=new Vector();
						Vector choices2=new Vector();
						Vector choices3=new Vector();
						Enumeration e=CMMap.rooms();
						if(A!=null) e=A.getMap();
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
							if(R2.ID().equalsIgnoreCase(localeName))
							{
								choices=choices0;
								choices0.addElement(R2);
							}
							else
							if(display.equals(localeName))
							{
								if((choices==null)||(choices==choices2)||(choices==choices3))
									choices=choices1;
								choices1.addElement(R2);
							}
							else
							if(CoffeeUtensils.containsString(display,localeName))
							{
								if((choices==null)||(choices==choices3))
									choices=choices2;
								choices2.addElement(R2);
							}
							else
							if(CoffeeUtensils.containsString(desc,localeName))
							{
								if(choices==null) choices=choices3;
								choices3.addElement(R2);
							}
						}
						if((choices!=null)&&(choices.size()>0))
							R=(Room)choices.elementAt(Dice.roll(1,choices.size(),-1));
						if(R==null)
							Log.errOut("Quest '"+name()+"', unknown locale '"+localeName+"'.");
						else
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
						Enumeration e=CMMap.rooms();
						if(A!=null) e=A.getMap();
						for(;e.hasMoreElements();)
						{
							Room R2=(Room)e.nextElement();
							for(int i=0;i<R2.numInhabitants();i++)
							{
								MOB M2=R2.fetchInhabitant(i);
								if(M2!=null)
								{
									String mname=M2.name().toUpperCase();
									String mdisp=M2.displayText().toUpperCase();
									String mdesc=M2.description().toUpperCase();
									if(mobName.equalsIgnoreCase("any"))
									{
										choices=choices0;
										choices0.addElement(M2);
									}
									else
									if(mname.equalsIgnoreCase(mobName))
									{
										choices=choices0;
										choices0.addElement(M2);
									}
									else
									if(CoffeeUtensils.containsString(mname,mobName))
									{
										if((choices==null)||(choices==choices3))
											choices=choices2;
										choices2.addElement(M2);
									}
									else
									if(CoffeeUtensils.containsString(mdisp,mobName))
									{
										if((choices==null)||(choices==choices3))
											choices=choices2;
										choices2.addElement(M2);
									}
									else
									if(CoffeeUtensils.containsString(mdesc,mobName))
									{
										if(choices==null) choices=choices3;
										choices3.addElement(M2);
									}
								}
							}
						}
						if(choices.size()>0)
							M=(MOB)choices.elementAt(Dice.roll(1,choices.size(),-1));
						if(M==null)
						{
							Log.errOut("Quest '"+name()+"', unfound mobname '"+mobName+"'.");
							if((E!=null)&&(E instanceof MOB)) E=I;
						}
						else
						{
							if(R!=null)
								R.bringMobHere(M,false);
							else
								R=M.location();
							A=R.getArea();
							E=M;
							if(!stuff.contains(M))
								stuff.addElement(M);
						}
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
						Enumeration e=CMMap.rooms();
						if(A!=null) e=A.getMap();
						for(;e.hasMoreElements();)
						{
							Room R2=(Room)e.nextElement();
							for(int i=0;i<R2.numItems();i++)
							{
								Item I2=R2.fetchItem(i);
								if(I2!=null)
								{
									String iname=I2.name().toUpperCase();
									String idisp=I2.displayText().toUpperCase();
									String idesc=I2.description().toUpperCase();
									if(itemName.equalsIgnoreCase("any"))
									{
										choices=choices0;
										choices0.addElement(I2);
									}
									else
									if(iname.equalsIgnoreCase(itemName))
									{
										choices=choices0;
										choices0.addElement(I2);
									}
									else
									if(CoffeeUtensils.containsString(iname,itemName))
									{
										if((choices==null)||(choices==choices3))
											choices=choices2;
										choices2.addElement(I2);
									}
									else
									if(CoffeeUtensils.containsString(idisp,itemName))
									{
										if((choices==null)||(choices==choices3))
											choices=choices2;
										choices2.addElement(I2);
									}
									else
									if(CoffeeUtensils.containsString(idesc,itemName))
									{
										if(choices==null) choices=choices3;
										choices3.addElement(I2);
									}
								}
							}
						}
						if(choices.size()>0)
							I=(Item)choices.elementAt(Dice.roll(1,choices.size(),-1));
						if(I==null)
						{
							Log.errOut("Quest '"+name()+"', unfound itemname '"+itemName+"'.");
							if((E!=null)&&(E instanceof Item)) E=M;
						}
						else
						{
							if(R!=null)
								R.bringItemHere(I);
							else
							if(I.owner() instanceof Room)
								R=(Room)I.owner();
							A=R.getArea();
							E=I;
						}
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
						Log.errOut("Quest '"+name()+"', unknown variable '"+cmd+"'.");
				}
				else
				if(cmd.equals("IMPORT"))
				{
					if(p.size()<2)
					{
						Log.errOut("Quest '"+name()+"', unfound type on import.");
						continue;
					}
					cmd=((String)p.elementAt(1)).toUpperCase();
					if(cmd.equals("MOBS"))
					{
						if(p.size()<3)
						{
							Log.errOut("Quest '"+name()+"', no import filename!");
							continue;
						}
						StringBuffer buf=Resources.getFileResource(Util.combine(p,2));
						if((buf==null)||((buf!=null)&&(buf.length()==0)))
						{
							Log.errOut("Quest","Unknown XML file: '"+Util.combine(p,2)+"' for '"+name()+"'.");
							continue;
						}
						if(buf.substring(0,20).indexOf("<MOBS>")<0)
						{
							Log.errOut("Quest","Invalid XML file: '"+Util.combine(p,2)+"' for '"+name()+"'.");
							continue;
						}
						loadedMobs=new Vector();
						String error=com.planet_ink.coffee_mud.common.Generic.addMOBsFromXML(buf.toString(),loadedMobs,null);
						if(error.length()>0)
						{
							Log.errOut("Quest","Error on import of: '"+Util.combine(p,2)+"' for '"+name()+"': "+error+".");
							continue;
						}
						if(loadedMobs.size()<=0)
						{
							Log.errOut("Quest","No mobs loaded: '"+Util.combine(p,2)+"' for '"+name()+"'.");
							continue;
						}
					}
					else
					if(cmd.equals("ITEMS"))
					{
						if(p.size()<3)
						{
							Log.errOut("Quest '"+name()+"', no import filename!");
							continue;
						}
						StringBuffer buf=Resources.getFileResource(Util.combine(p,2));
						if((buf==null)||((buf!=null)&&(buf.length()==0)))
						{
							Log.errOut("Quest","Unknown XML file: '"+Util.combine(p,2)+"' for '"+name()+"'.");
							continue;
						}
						if(buf.substring(0,20).indexOf("<ITEMS>")<0)
						{
							Log.errOut("Quest","Invalid XML file: '"+Util.combine(p,2)+"' for '"+name()+"'.");
							continue;
						}
						loadedItems=new Vector();
						String error=com.planet_ink.coffee_mud.common.Generic.addItemsFromXML(buf.toString(),loadedItems,null);
						if(error.length()>0)
						{
							Log.errOut("Quest","Error on import of: '"+Util.combine(p,2)+"' for '"+name()+"': "+error+".");
							continue;
						}
						if(loadedItems.size()<=0)
						{
							Log.errOut("Quest","No items loaded: '"+Util.combine(p,2)+"' for '"+name()+"'.");
							continue;
						}
					}
					else
						Log.errOut("Quest '"+name()+"', unknown import type '"+cmd+"'.");
				}
				else
				if(cmd.equals("LOAD"))
				{
					if(p.size()<2)
					{
						Log.errOut("Quest '"+name()+"', unfound type on load.");
						continue;
					}
					cmd=((String)p.elementAt(1)).toUpperCase();
					if(cmd.equals("MOB"))
					{
						if(loadedMobs.size()==0)
						{
							Log.errOut("Quest '"+name()+"', cannot load mob, no mobs imported.");
							continue;
						}
						if(p.size()<3)
						{
							Log.errOut("Quest '"+name()+"', no mob name to load!");
							continue;
						}
						String mobName=Util.combine(p,2);
						Vector choices=new Vector();
						for(int i=0;i<loadedMobs.size();i++)
						{
							MOB M2=(MOB)loadedMobs.elementAt(i);
							if((mobName.equalsIgnoreCase("any"))
							||(CoffeeUtensils.containsString(M2.name(),mobName))
							||(CoffeeUtensils.containsString(M2.displayText(),mobName))
							||(CoffeeUtensils.containsString(M2.description(),mobName)))
								choices.addElement(M2);
						}
						if(choices.size()==0)
						{
							Log.errOut("Quest '"+name()+"', no mob found to load '"+mobName+"'!");
							continue;
						}
						M=(MOB)choices.elementAt(Dice.roll(1,choices.size(),-1));
						if(R==null)
						{
							if(A!=null)
								R=A.getRandomRoom();
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
					}
					else
					if(cmd.equals("ITEM"))
					{
						if(loadedItems.size()==0)
						{
							Log.errOut("Quest '"+name()+"', cannot load item, no items imported.");
							continue;
						}
						if(p.size()<3)
						{
							Log.errOut("Quest '"+name()+"', no item name to load!");
							continue;
						}
						String itemName=Util.combine(p,2);
						Vector choices=new Vector();
						for(int i=0;i<loadedItems.size();i++)
						{
							Item I2=(Item)loadedItems.elementAt(i);
							if((itemName.equalsIgnoreCase("any"))
							||(CoffeeUtensils.containsString(I2.name(),itemName))
							||(CoffeeUtensils.containsString(I2.displayText(),itemName))
							||(CoffeeUtensils.containsString(I2.description(),itemName)))
								choices.addElement(I2);
						}
						if(choices.size()==0)
						{
							Log.errOut("Quest '"+name()+"', no item found to load '"+itemName+"'!");
							continue;
						}
						I=(Item)choices.elementAt(Dice.roll(1,choices.size(),-1));
						if(R==null)
						{
							if(A!=null)
								R=A.getRandomRoom();
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
						}
						E=I;
						if(!stuff.contains(I))
							stuff.addElement(I);
						
					}
					else
						Log.errOut("Quest '"+name()+"', unknown load type '"+cmd+"'.");
					
				}
				else
				if(cmd.equals("GIVE"))
				{
					if(p.size()<2)
					{
						Log.errOut("Quest '"+name()+"', unfound type on give.");
						continue;
					}
					cmd=((String)p.elementAt(1)).toUpperCase();
					if(cmd.equals("FOLLOWER"))
					{
						if(M==null)
						{
							Log.errOut("Quest '"+name()+"', cannot give follower, no mob set.");
							continue;
						}
						if(p.size()<3)
						{
							Log.errOut("Quest '"+name()+"', cannot give follower, follower name not given.");
							continue;
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
								||(CoffeeUtensils.containsString(M2.name(),mobName))
								||(CoffeeUtensils.containsString(M2.displayText(),mobName))
								||(CoffeeUtensils.containsString(M2.description(),mobName)))
									choices.addElement(M2);
							}
						}
						if(choices.size()==0)
						{
							Log.errOut("Quest '"+name()+"', cannot give follower, no mobs called '"+mobName+"' previously set in script.");
							continue;
						}
						MOB M2=(MOB)choices.elementAt(Dice.roll(1,choices.size(),-1));
						M2.setFollowing(M);
					}
					else
					if(cmd.equals("ITEM"))
					{
						if(I==null)
						{
							Log.errOut("Quest '"+name()+"', cannot give item, no item set.");
							continue;
						}
						if(M==null)
						{
							Log.errOut("Quest '"+name()+"', cannot give item, no mob set.");
							continue;
						}
						if(p.size()>2)
						{
							Log.errOut("Quest '"+name()+"', cannot give item, parameter unnecessarily given: '"+Util.combine(p,2)+"'.");
							continue;
						}
						I.removeThis();
						M.addInventory(I);
					}
					else
					if(cmd.equals("ABILITY"))
					{
						if(M==null)
						{
							Log.errOut("Quest '"+name()+"', cannot give ability, no mob set.");
							continue;
						}
						if(p.size()<3)
						{
							Log.errOut("Quest '"+name()+"', cannot give ability, ability name not given.");
							continue;
						}
						Ability A3=CMClass.findAbility((String)p.elementAt(2));
						if(A3==null)
						{
							Log.errOut("Quest '"+name()+"', cannot give ability, ability name unknown '"+((String)p.elementAt(2))+".");
							continue;
						}
						if(M.fetchAbility(A3.ID())!=null)
						{
							A3=M.fetchAbility(A3.ID());
							A3.setMiscText(Util.combine(p,3));
							A3.setProfficiency(100);
						}
						else 
						{
							A3.setMiscText(Util.combine(p,3));
							A3.setProfficiency(100);
							M.addAbility(A3);
						}
					}
					else
					if(cmd.equals("BEHAVIOR"))
					{
						if(E==null)
						{
							Log.errOut("Quest '"+name()+"', cannot give behavior, no mob or item set.");
							continue;
						}
						if(p.size()<3)
						{
							Log.errOut("Quest '"+name()+"', cannot give behavior, behavior name not given.");
							continue;
						}
						Behavior B=CMClass.getBehavior((String)p.elementAt(2));
						if(B==null)
						{
							Log.errOut("Quest '"+name()+"', cannot give behavior, behavior name unknown '"+((String)p.elementAt(2))+".");
							continue;
						}
						if(E.fetchBehavior(B.ID())!=null)
						{
							B=E.fetchBehavior(B.ID());
							B.setParms(Util.combine(p,3));
						}
						else 
						{
							B.setParms(Util.combine(p,3));
							E.addBehavior(B);
						}
					}
					else
					if(cmd.equals("AFFECT"))
					{
						if(E==null)
						{
							Log.errOut("Quest '"+name()+"', cannot give affect, no mob or item set.");
							continue;
						}
						if(p.size()<3)
						{
							Log.errOut("Quest '"+name()+"', cannot give affect, ability name not given.");
							continue;
						}
						Ability A3=CMClass.findAbility((String)p.elementAt(2));
						if(A3==null)
						{
							Log.errOut("Quest '"+name()+"', cannot give affect, ability name unknown '"+((String)p.elementAt(2))+".");
							continue;
						}
						if(M.fetchAffect(A3.ID())!=null)
						{
							A3=E.fetchAffect(A3.ID());
							A3.setMiscText(Util.combine(p,3));
							A3.makeNonUninvokable();
						}
						else 
						{
							A3.setMiscText(Util.combine(p,3));
							E.addNonUninvokableAffect(A3);
						}
					}
					else
					{
						Log.errOut("Quest '"+name()+"', unknown give type '"+cmd+"'.");
						continue;
					}
				}
				else
				{
					Log.errOut("Quest '"+name()+"', unknown command '"+cmd+"'.");
					continue;
				}
				done=true;
			}
		}
		if(!done)
			Log.errOut("Quest","Nothing parsed in '"+name()+"'");
		else
		if(duration()<0)
			Log.errOut("Quest","No duration, quest '"+name()+"' not started.");
		else
		{
			waitRemaining=-1;
			ticksRemaining=duration();
			ExternalPlay.startTickDown(this,Host.QUEST_TICK,1);
		}
	}
	
	// this will stop executing of the quest script.  It will clean up 
	// any objects or mobs which may have been loaded, restoring map 
	// mobs to their previous state.
	public void stopQuest()
	{
		if(stuff.size()>0)
		{
			for(int i=0;i<stuff.size();i++)
			{
				Environmental E=(Environmental)stuff.elementAt(i);
				if(E instanceof Item)
					((Item)E).destroyThis();
				else
				if(E instanceof MOB)
				{
					MOB M=(MOB)E;
					if((M.location()!=null)&&(M.location().numPCInhabitants()>0))
					{
						Behavior B=CMClass.getBehavior("Mobile");
						for(int i2=0;i2<100;i2++)
						{
							B.tick(M,Host.MOB_TICK);
							if((M.location()!=null)&&(M.location().numPCInhabitants()==0))
								break;
						}
					}
					if(M.getStartRoom()!=null)
					{
						M.destroy();
						ExternalPlay.resetRoom(M.getStartRoom());
					}
					else
						M.destroy();
				}
			}
			stuff.clear();
		}
		if(running())
		{
			ticksRemaining=-1;
			if((minWait()<0)||(maxWait<0))
				ExternalPlay.deleteTick(this,Host.QUEST_TICK);
			else
				waitRemaining=minWait+(Dice.roll(1,maxWait,0));
		}
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
		Vector V=getWinners();
		if(!wasWinner(name))
		{
			V.addElement(name);
			ExternalPlay.DBUpdateQuest(this);
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
	public boolean waiting(){return waitRemaining>=0;}
	public int ticksRemaining(){return ticksRemaining;}
	public int minsRemaining(){return ticksRemaining*Host.TICK_TIME/60000;}
	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID!=Host.QUEST_TICK) 
			return false;
		
		if(running())
		{
			ticksRemaining--;
			if(ticksRemaining<0)
			{
				stopQuest();
				if((minWait()<0)||(maxWait<0))
					return false;
				waitRemaining=minWait+(Dice.roll(1,maxWait,0));
			}
		}
		else
		{
			waitRemaining--;
			if(waitRemaining<0)
			{
				ticksRemaining=duration();
				startQuest();
			}
		}
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
	public String getQuestObjectName(int i)
	{
		i=i-1; // starts counting at 1
		if((i>=0)&&(i<stuff.size()))
		{
			Environmental E=(Environmental)stuff.elementAt(i);
			return E.name();
		}
		return "";
	}
	public String getQuestMobName(int i)
	{
		int num=1;
		for(int x=0;x<stuff.size();x++)
		{
			Environmental E=(Environmental)stuff.elementAt(x);
			if(E instanceof MOB)
			{
				if(num==i) return E.name();
				num++;
			}
		}
		return "";
	}
	public String getQuestItemName(int i)
	{
		int num=1;
		for(int x=0;x<stuff.size();x++)
		{
			Environmental E=(Environmental)stuff.elementAt(x);
			if(E instanceof Item)
			{
				if(num==i) return E.name();
				num++;
			}
		}
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
				if(text.charAt(yy)==';'){y=yy;break;}
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
				script.addElement(cmd);
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
			ExternalPlay.deleteTick(Q,Host.QUEST_TICK);
			quests.removeElement(Q);
		}
	}
	public static void save()
	{
		ExternalPlay.DBUpdateQuests(quests);
	}
}
