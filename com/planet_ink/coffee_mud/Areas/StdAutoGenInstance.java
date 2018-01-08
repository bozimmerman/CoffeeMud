package com.planet_ink.coffee_mud.Areas;
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
import com.planet_ink.coffee_mud.Items.Basic.StdItem;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary.XMLTag;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary.XMLTag;
import com.planet_ink.coffee_mud.MOBS.GenShopkeeper;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
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
public class StdAutoGenInstance extends StdArea implements AutoGenArea
{
	@Override
	public String ID()
	{
		return "StdAutoGenInstance";
	}

	private long	flags	= Area.FLAG_INSTANCE_PARENT;

	@Override
	public long flags()
	{
		return flags;
	}

	protected CList<AreaInstanceChild>	instanceChildren= new SVector<AreaInstanceChild>();
	protected volatile int				instanceCounter	= 0;
	protected long						childCheckDown	= CMProps.getMillisPerMudHour() / CMProps.getTickMillis();
	protected WeakReference<Area>		parentArea		= null;
	protected String					filePath		= "randareas/example.xml";
	protected Map<String, String>		varMap			= new Hashtable<String, String>(1);

	protected String getStrippedRoomID(String roomID)
	{
		final int x=roomID.indexOf('#');
		if(x<0)
			return null;
		return roomID.substring(x);
	}

	protected String convertToMyArea(String roomID)
	{
		final String strippedID=getStrippedRoomID(roomID);
		if(strippedID==null)
			return null;
		return Name()+strippedID;
	}

	protected Area getParentArea()
	{
		if((parentArea!=null)&&(parentArea.get()!=null))
			return parentArea.get();
		final int x=Name().indexOf('_');
		if(x<0)
			return null;
		if(!CMath.isNumber(Name().substring(0,x)))
			return null;
		final Area parentA = CMLib.map().getArea(Name().substring(x+1));
		if((parentA==null)
		||(!CMath.bset(parentA.flags(),Area.FLAG_INSTANCE_PARENT))
		||(CMath.bset(parentA.flags(),Area.FLAG_INSTANCE_CHILD)))
			return null;
		parentArea=new WeakReference<Area>(parentA);
		return parentA;
	}

	@Override 
	public int getPercentRoomsCached()
	{
		return (getParentArea()==null)?0:100;
	}

	@Override 
	public int[] getAreaIStats()
	{
		if(!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
			return emptyStats;
		final Area parentArea=getParentArea();
		final String areaName = (parentArea==null)?Name():parentArea.Name();
		int[] statData=(int[])Resources.getResource("STATS_"+areaName.toUpperCase());
		if(statData!=null)
			return statData;
		List<Area> workList = new LinkedList<Area>();
		synchronized(("STATS_"+Name()).intern())
		{
			if(parentArea==null)
			{
				for(final Enumeration<AreaInstanceChild> childE=instanceChildren.elements();childE.hasMoreElements();)
					workList.add(childE.nextElement().A);
			}
			else
			{
				statData=buildAreaIStats();
			}
		}
		if(parentArea == null)
		{
			int ct=0;
			statData=new int[Area.Stats.values().length];
			for(Area childA : workList)
			{
				final int[] theseStats=childA.getAreaIStats();
				if(theseStats != emptyStats)
				{
					ct++;
					for(int i=0;i<theseStats.length;i++)
						statData[i]+=theseStats[i];
				}
			}
			if(ct==0)
				return emptyStats;
			for(int i=0;i<statData.length;i++)
				statData[i]=statData[i]/ct;
			Resources.removeResource("HELP_"+areaName.toUpperCase());
			Resources.submitResource("STATS_"+areaName.toUpperCase(),statData);
		}
		return statData;
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		if(CMath.bset(flags(),Area.FLAG_INSTANCE_CHILD))
			return true;
		if((--childCheckDown)<=0)
		{
			childCheckDown=CMProps.getMillisPerMudHour()/CMProps.getTickMillis();
			LinkedList<AreaInstanceChild> workList = new LinkedList<AreaInstanceChild>();
			final List<AreaInstanceChild> children = this.instanceChildren;
			if(children == null)
				return true;
			synchronized(children)
			{
				for(int i=children.size()-1;i>=0;i--)
				{
					final AreaInstanceChild child = children.get(i);
					final Area childA=child.A;
					if(childA.getAreaState() != Area.State.ACTIVE)
					{
						workList.add(child);
					}
				}
			}
			for(AreaInstanceChild child : workList)
			{
				final Area childA=child.A;
				final List<WeakReference<MOB>> V=child.mobs;
				boolean anyInside=false;
				for(final WeakReference<MOB> wmob : V)
				{
					final MOB M=wmob.get();
					if((M!=null)
					&&CMLib.flags().isInTheGame(M,true)
					&&(M.location()!=null)
					&&(M.location().getArea()==childA))
					{
						anyInside=true;
						break;
					}
				}
				if(!anyInside)
				{
					synchronized(children)
					{
						children.remove(child);
					}
					for(final WeakReference<MOB> wmob : V)
					{
						final MOB M=wmob.get();
						if((M!=null)
						&&(M.location()!=null)
						&&(M.location().getArea()==this))
							M.setLocation(M.getStartRoom());
					}
					final MOB mob=CMClass.sampleMOB();
					for(final Enumeration<Room> e=childA.getProperMap();e.hasMoreElements();)
					{
						final Room R=e.nextElement();
						R.executeMsg(mob,CMClass.getMsg(mob,R,null,CMMsg.MSG_EXPIRE,null));
					}
					CMLib.map().delArea(childA);
					childA.destroy();
				}
			}
		}
		return true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost, msg);
		if(CMath.bset(flags(),Area.FLAG_INSTANCE_CHILD))
		{
			if((msg.sourceMinor()==CMMsg.TYP_SPEAK)
			&&(msg.sourceMessage()!=null)
			&&((msg.sourceMajor()&CMMsg.MASK_MAGIC)==0))
			{
				final String said=CMStrings.getSayFromMessage(msg.sourceMessage());
				if("RESET INSTANCE".equalsIgnoreCase(said))
				{
					Room returnToRoom=null;
					final Room thisRoom=msg.source().location();
					if(thisRoom.getArea()==this)
					{
						for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
						{
							final Room R=thisRoom.getRoomInDir(d);
							if((R!=null)&&(R.getArea()!=null)&&(R.getArea()!=this))
								returnToRoom=R;
						}
					}
					if(returnToRoom==null)
					{
						msg.addTrailerMsg(CMClass.getMsg(msg.source(),null,null,CMMsg.MSG_OK_ACTION,CMMsg.NO_EFFECT,CMMsg.NO_EFFECT, L("You must be at an entrance to reset the area.")));
						return;
					}
					final Area A=this.getParentArea();
					if(A instanceof StdAutoGenInstance)
					{
						final StdAutoGenInstance parentA=(StdAutoGenInstance)A;
						AreaInstanceChild rec = null;
						synchronized(parentA.instanceChildren)
						{
							for(int i=parentA.instanceChildren.size()-1;i>=0;i--)
							{
								if(parentA.instanceChildren.get(i).A==this)
								{
									rec = parentA.instanceChildren.get(i);
									break;
								}
							}
						}
						if(rec != null)
						{
							final List<WeakReference<MOB>> V=rec.mobs;
							for(final WeakReference<MOB> wM : V)
							{
								final MOB M=wM.get();
								if((M!=null)
								&&CMLib.flags().isInTheGame(M,true)
								&&(M.location()!=null)
								&&(M.location()!=returnToRoom)
								&&(M.location().getArea()==this))
								{
									returnToRoom.bringMobHere(M, true);
									CMLib.commands().postLook(M, true);
								}
							}
							synchronized(parentA.instanceChildren)
							{
								parentA.instanceChildren.remove(rec);
							}
							final MOB mob=CMClass.sampleMOB();
							for(final Enumeration<Room> e=getProperMap();e.hasMoreElements();)
							{
								final Room R=e.nextElement();
								R.executeMsg(mob,CMClass.getMsg(mob,R,null,CMMsg.MSG_EXPIRE,null));
							}
							msg.addTrailerMsg(CMClass.getMsg(msg.source(),CMMsg.MSG_OK_ACTION,L("The instance has been reset.")));
							CMLib.map().delArea(this);
							destroy();
							return;
						}
					}
					msg.addTrailerMsg(CMClass.getMsg(msg.source(),CMMsg.MSG_OK_ACTION,L("The instance failed to reset.")));
				}
			}
			else
			if((msg.sourceMinor()==CMMsg.TYP_QUIT)&&(CMLib.map().isHere(msg.source(), this)))
			{
				final MOB mob = msg.source();
				CMLib.tracking().forceRecall(mob, true);
			}
		}
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if(CMath.bset(flags(),Area.FLAG_INSTANCE_CHILD))
			return true;
		setAreaState(Area.State.PASSIVE);
		if((msg.sourceMinor()==CMMsg.TYP_ENTER)
		&&(msg.target() instanceof Room)
		&&(CMath.bset(flags(),Area.FLAG_INSTANCE_PARENT))
		&&(isRoom((Room)msg.target()))
		&&(((msg.source().getStartRoom()==null)||(msg.source().getStartRoom().getArea()!=this))))
		{
			if(msg.source().isMonster())
			{
				final Set<MOB> friends=msg.source().getGroupMembers(new HashSet<MOB>());
				boolean playerInvolved=false;
				for(final MOB M : friends)
					playerInvolved = playerInvolved || (!M.isMonster());
				if(!playerInvolved)
				{
					msg.source().tell(L("You'll need to be accompanied by an adult to enter there."));
					return false;
				}
			}
			final Set<MOB> grp = msg.source().getGroupMembers(new HashSet<MOB>());
			final List<AreaInstanceChild> childSearchGroup;
			synchronized(instanceChildren)
			{
				childSearchGroup = new XVector<AreaInstanceChild>(instanceChildren);
			}
			int myDex=-1;
			AreaInstanceChild myRec = null;
			for(int i=0;i<childSearchGroup.size();i++)
			{
				final List<WeakReference<MOB>> V=childSearchGroup.get(i).mobs;
				for (final WeakReference<MOB> weakReference : V)
				{
					if(msg.source() == weakReference.get())
					{
						myDex = i;
						myRec=childSearchGroup.get(i);
						break;
					}
				}
			}
			for(int i=0;i<childSearchGroup.size();i++)
			{
				if(i!=myDex)
				{
					final AreaInstanceChild instRec = childSearchGroup.get(i);
					final List<WeakReference<MOB>> V=instRec.mobs;
					for(int v=V.size()-1;v>=0;v--)
					{
						final WeakReference<MOB> wmob=V.get(v);
						if(wmob==null)
							continue;
						final MOB M=wmob.get();
						if(grp.contains(M))
						{
							if(myDex<0)
							{
								myDex=i;
								myRec = childSearchGroup.get(i);
								break;
							}
							else
							if((CMLib.flags().isInTheGame(M,true))
							&&(M.location().getArea()!=instRec.A))
							{
								V.remove(wmob);
								if((myRec!=null)&&(myRec.mobs!=null))
									myRec.mobs.add(new WeakReference<MOB>(M));
							}
						}
					}
				}
			}
			Area redirectA = null;
			int direction = CMLib.map().getRoomDir(msg.source().location(), (Room)msg.target());
			if((direction<0)&&(msg.tool() instanceof Exit))
				direction = CMLib.map().getExitDir(msg.source().location(), (Exit)msg.tool());
			if(direction < 0)
			{
				msg.source().tell(L("Can't figure out where you're coming from?!"));
				return false;
			}
			if(myDex<0)
			{
				final StdAutoGenInstance newA=(StdAutoGenInstance)this.copyOf();
				newA.properRooms=new STreeMap<String, Room>(new Area.RoomIDComparator());
				newA.properRoomIDSet = null;
				newA.metroRoomIDSet = null;
				newA.blurbFlags=new STreeMap<String,String>();
				newA.setName((++instanceCounter)+"_"+Name());
				newA.flags |= Area.FLAG_INSTANCE_CHILD;
				final Set<MOB> myGroup=msg.source().getGroupMembers(new HashSet<MOB>());
				final StringBuffer xml = Resources.getFileResource(getGeneratorXmlPath(), true);
				if((xml==null)||(xml.length()==0))
				{
					msg.source().tell(L("Unable to load this area.  Please try again later."));
					return false;
				}
				final List<XMLLibrary.XMLTag> xmlRoot = CMLib.xml().parseAllXML(xml);
				final Hashtable<String,Object> definedIDs = new Hashtable<String,Object>();
				CMLib.percolator().buildDefinedIDSet(xmlRoot,definedIDs);
				String idName = "";
				final List<String> idChoices = new Vector<String>();
				for(final String key : getAutoGenVariables().keySet())
				{
					if(key.equalsIgnoreCase("AREA_ID")||key.equalsIgnoreCase("AREA_IDS")||key.equalsIgnoreCase("AREAID")||key.equalsIgnoreCase("AREAIDS"))
						idChoices.addAll(CMParms.parseCommas(getAutoGenVariables().get(key),true));
				}
				if(idChoices.size()==0)
				{
					for(final Object key : definedIDs.keySet())
					{
						final Object val=definedIDs.get(key);
						if((key instanceof String)
						&&(val instanceof XMLTag)
						&&(((XMLTag)val).tag().equalsIgnoreCase("area")))
						{
							final XMLTag piece=(XMLTag)val; 
							final String inserter = piece.getParmValue("INSERT");
							if(inserter!=null)
							{
								final List<String> V=CMParms.parseCommas(inserter,true);
								for(int v=0;v<V.size();v++)
								{
									String s = V.get(v);
									if(s.startsWith("$"))
										s=s.substring(1).trim();
									final XMLTag insertPiece =(XMLTag)definedIDs.get(s.toUpperCase().trim());
									if(insertPiece == null)
										continue;
									if(insertPiece.tag().equalsIgnoreCase("area"))
									{
										if(!idChoices.contains(s.toUpperCase().trim()))
											idChoices.add(s.toUpperCase().trim());
									}
								}
							}
							else
								idChoices.add((String)key);
						}
					}
				}

				if(idChoices.size()>0)
					idName=idChoices.get(CMLib.dice().roll(1, idChoices.size(), -1)).toUpperCase().trim();

				if((!(definedIDs.get(idName) instanceof XMLTag))
				||(!((XMLTag)definedIDs.get(idName)).tag().equalsIgnoreCase("area")))
				{
					msg.source().tell(L("The area id '@x1' has not been defined in the data file.",idName));
					return false;
				}
				final ScriptingEngine scrptEng=(ScriptingEngine)CMClass.getCommon("DefaultScriptingEngine");
				final Object[] scriptObjs = new Object[ScriptingEngine.SPECIAL_NUM_OBJECTS];
				final List<Double> levels=new ArrayList<Double>();
				final Set<MOB> followers=msg.source().getGroupMembers(new HashSet<MOB>());
				if(!followers.contains(msg.source()))
					followers.add(msg.source());
				double totalLevels=0.0;
				for(final MOB M : followers)
				{
					final Double D=Double.valueOf(M.basePhyStats().level());
					levels.add(D);
					totalLevels+=D.doubleValue();
				}
				final Double[] sortedLevels=levels.toArray(new Double[0]);
				final double lowestLevel=sortedLevels[0].doubleValue();
				final double medianLevel=sortedLevels[(int)Math.round(Math.floor(sortedLevels.length/2))].doubleValue();
				final double averageLevel=Math.round(10.0*totalLevels/(sortedLevels.length))/10.0;
				final double highestLevel=sortedLevels[sortedLevels.length-1].doubleValue();
				final double groupSize=Double.valueOf(followers.size()).doubleValue();
				final double values[]={msg.source().basePhyStats().level(),lowestLevel,medianLevel,averageLevel,highestLevel,totalLevels,groupSize};
				for(final String key : getAutoGenVariables().keySet())
				{
					if(!(key.equalsIgnoreCase("AREA_ID")||key.equalsIgnoreCase("AREA_IDS")||key.equalsIgnoreCase("AREAID")||key.equalsIgnoreCase("AREAIDS")))
					{
						final String rawValue = CMath.replaceVariables(getAutoGenVariables().get(key),values);
						final String val=scrptEng.varify(msg.source(), newA, msg.source(), msg.source(), null, null, msg.sourceMessage(), scriptObjs, rawValue);
						definedIDs.put(key.toUpperCase(),val);
					}
				}
				definedIDs.put("AREANAME", Name());
				if(!definedIDs.containsKey("AREASIZE"))
					definedIDs.put("AREASIZE", "50");
				if(!definedIDs.containsKey("LEVEL_RANGE"))
					definedIDs.put("LEVEL_RANGE", (msg.source().basePhyStats().level()-4)+"?"+(msg.source().basePhyStats().level()));
				if(!definedIDs.containsKey("AGGROCHANCE"))
					definedIDs.put("AGGROCHANCE", ""+msg.source().basePhyStats().level());
				try
				{
					XMLTag piece=(XMLTag)definedIDs.get(idName);
					final List<XMLTag> pieces = CMLib.percolator().getAllChoices(piece.tag(), piece, definedIDs);
					if(pieces.size()>0)
						piece=pieces.get(CMLib.dice().roll(1, pieces.size(), -1));
//TODO: fix this
//FIXME: this can maybe pick town.
					if(!definedIDs.containsKey("THEME"))
					{
						final Map<String,String> unfilled = CMLib.percolator().getUnfilledRequirements(definedIDs,piece);
						final List<String> themes = CMParms.parseCommas(unfilled.get("THEME"), true);
						if(themes.size()>0)
							definedIDs.put("THEME", themes.get(CMLib.dice().roll(1, themes.size(), -1)).toUpperCase().trim());
					}
					try
					{
						CMLib.percolator().checkRequirements(piece, definedIDs);
					}
					catch(final CMException cme)
					{
						msg.source().tell(L("Required ids for @x1 were missing: @x2",idName,cme.getMessage()));
						return false;
					}
					for(final MOB M : myGroup)
						M.tell(L("^x------------------------------------------------------\n\rPreparing to enter @x1, please stand by...\n\r------------------------------------------------------^N^.",Name()));
					definedIDs.put("ROOMTAG_NODEGATEEXIT", CMLib.directions().getDirectionName(Directions.getOpDirectionCode(direction)));
					definedIDs.put("ROOMTAG_GATEEXITROOM", msg.source().location());
					if(!CMLib.percolator().fillInArea(piece, definedIDs, newA, direction))
					{
						msg.source().tell(L("Failed to enter the new area.  Try again later."));
						return false;
					}
					CMLib.percolator().postProcess(definedIDs);
				}
				catch(final CMException cme)
				{
					Log.errOut("StdAutoGenInstance",cme);
					msg.source().tell(L("Failed to finish entering the new area.  Try again later."));
					return false;
				}
				redirectA=newA;
				CMLib.map().addArea(newA);
				newA.setAreaState(Area.State.ACTIVE); // starts ticking
				final List<WeakReference<MOB>> newMobList = new SVector<WeakReference<MOB>>(5);
				newMobList.add(new WeakReference<MOB>(msg.source()));
				final AreaInstanceChild child = new AreaInstanceChild(redirectA,newMobList);
				synchronized(instanceChildren)
				{
					instanceChildren.add(child);
				}

				getAreaIStats(); // if this is the first child ever, this will force stat making

				final Room R=redirectA.getRoom(redirectA.Name()+"#0");
				if(R!=null)
				{
					Exit E=R.getExitInDir(Directions.getOpDirectionCode(direction));
					if(E==null)
						E = CMClass.getExit("Open");
					final int opDir=Directions.getOpDirectionCode(direction);
					if(R.getRoomInDir(opDir)!=null)
						msg.source().tell(L("An error has caused the following exit to be one-way."));
					else
					{
						R.setRawExit(opDir, E);
						R.rawDoors()[opDir]=msg.source().location();
					}
				}
			}
			else
			if(myRec != null)
				redirectA=myRec.A;
			if(redirectA instanceof StdAutoGenInstance)
			{
				final Room R=redirectA.getRoom(redirectA.Name()+"#0");
				if(R!=null)
				{
					msg.setTarget(R);
				}
			}
		}
		return true;
	}

	private final static String[] MYCODES={"GENERATIONFILEPATH","OTHERVARS"};

	@Override
	public String getStat(String code)
	{
		if(CMParms.indexOfIgnoreCase(STDAREACODES, code)>=0)
			return super.getStat(code);
		else
		switch(getCodeNum(code))
		{
			case 0:
				return this.getGeneratorXmlPath();
			case 1:
				return CMParms.toEqListString(this.getAutoGenVariables());
			default:
				break;
		}
		return "";
	}

	@Override
	public void setStat(String code, String val)
	{
		if(CMParms.indexOfIgnoreCase(STDAREACODES, code)>=0)
			super.setStat(code, val);
		else
		switch(getCodeNum(code))
		{
			case 0:
				setGeneratorXmlPath(val);
				break;
			case 1:
				setAutoGenVariables(val);
				break;
			default:
				break;
		}
	}

	@Override
	protected int getCodeNum(String code)
	{
		for(int i=0;i<MYCODES.length;i++)
		{
			if(code.equalsIgnoreCase(MYCODES[i]))
				return i;
		}
		return -1;
	}

	private static String[] codes=null;

	@Override
	public String[] getStatCodes()
	{
		if(codes!=null)
			return codes;
		final String[] MYCODES=CMProps.getStatCodesList(StdAutoGenInstance.MYCODES,this);
		final String[] superCodes=STDAREACODES;
		codes=new String[superCodes.length+MYCODES.length];
		int i=0;
		for(;i<superCodes.length;i++)
			codes[i]=superCodes[i];
		for(int x=0;x<MYCODES.length;i++,x++)
			codes[i]=MYCODES[x];
		return codes;
	}

	@Override
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof StdAutoGenInstance))
			return false;
		final String[] codes=getStatCodes();
		for(int i=0;i<codes.length;i++)
		{
			if(!E.getStat(codes[i]).equals(getStat(codes[i])))
				return false;
		}
		return true;
	}

	@Override
	public String getGeneratorXmlPath()
	{
		return filePath;
	}

	@Override
	public Map<String, String> getAutoGenVariables()
	{
		return varMap;
	}

	@Override
	public void setGeneratorXmlPath(String path)
	{
		filePath = path;
	}

	@Override
	public void setAutoGenVariables(Map<String, String> vars)
	{
		varMap = vars;
	}

	@Override
	public void setAutoGenVariables(String vars)
	{
		setAutoGenVariables(CMParms.parseEQParms(vars));
	}
}
