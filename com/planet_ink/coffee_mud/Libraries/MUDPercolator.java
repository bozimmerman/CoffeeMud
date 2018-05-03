package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.exceptions.CMException;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AreaGenerationLibrary.LayoutNode;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary.XMLTag;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary.XMLTag;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityMapper.AbilityMapping;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

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
public class MUDPercolator extends StdLibrary implements AreaGenerationLibrary
{
	@Override
	public String ID()
	{
		return "MUDPercolator";
	}

	protected final static char[] splitters=new char[]{'<','>','='};
	protected final static Triad<Integer,Integer,Class<?>[]> emptyMetacraftFilter = new Triad<Integer,Integer,Class<?>[]>(Integer.valueOf(-1),Integer.valueOf(-1),new Class<?>[0]);
	protected final static String POST_PROCESSING_STAT_SETS="___POST_PROCESSING_SETS___";
	protected final static Set<String> UPPER_REQUIRES_KEYWORDS=new XHashSet<String>(new String[]{"INT","INTEGER","$","STRING","ANY","DOUBLE","#","NUMBER"});
	protected final static CMParms.DelimiterChecker REQUIRES_DELIMITERS=CMParms.createDelimiter(new char[]{' ','\t',',','\r','\n'});

	private final SHashtable<String,Class<LayoutManager>> mgrs = new SHashtable<String,Class<LayoutManager>>();

	private interface BuildCallback
	{
		public void willBuild(Environmental E, XMLTag XMLTag);
	}

	@Override
	public LayoutManager getLayoutManager(String named)
	{
		final Class<LayoutManager> mgr = mgrs.get(named.toUpperCase().trim());
		if(mgr != null)
		{
			try
			{
				return mgr.newInstance();
			}
			catch(final Exception e)
			{
				return null;
			}
		}
		return null;
	}

	@Override
	public void buildDefinedIDSet(List<XMLTag> xmlRoot, Map<String,Object> defined)
	{
		if(xmlRoot==null)
			return;
		for(int v=0;v<xmlRoot.size();v++)
		{
			final XMLLibrary.XMLTag piece = xmlRoot.get(v);
			final String id = piece.getParmValue("ID");
			if((id!=null)&&(id.length()>0))
			{
				final Object o=defined.get(id.toUpperCase());
				if(o != null)
				{
					if(!(o instanceof XMLTag))
						Log.errOut("Duplicate ID: "+id+" (first tag did not resolve to a complex piece -- it wins.)");
					else
					{
						final Boolean pMergeVal;
						if((piece.parms()==null)||(!piece.parms().containsKey("MERGE")))
							pMergeVal = null;
						else
							pMergeVal = Boolean.valueOf(CMath.s_bool(piece.parms().get("MERGE")));
						final Boolean oMergeVal;
						if((((XMLTag)o).parms()==null)||(!((XMLTag)o).parms().containsKey("MERGE")))
							oMergeVal = null;
						else
							oMergeVal = Boolean.valueOf(CMath.s_bool(((XMLTag)o).parms().get("MERGE")));
						if((pMergeVal == null)||(oMergeVal == null))
							Log.errOut("Duplicate ID: "+id+" (no MERGE tag found to permit this operation -- first tag wins.)");
						else
						if(pMergeVal.booleanValue() && oMergeVal.booleanValue())
						{
							final XMLTag src=piece;
							final XMLTag tgt=(XMLTag)o;
							if(!src.tag().equalsIgnoreCase(tgt.tag()))
								Log.errOut("Unable to merge tags with ID: "+id+", they are of different types.");
							else
							for(final String parm : src.parms().keySet())
							{
								final String srcParmVal=src.parms().get(parm);
								final String tgtParmVal=tgt.parms().get(parm);
								if(tgtParmVal==null)
									tgt.parms().put(parm,srcParmVal);
								else
								if(tgtParmVal.equalsIgnoreCase(srcParmVal)||parm.equalsIgnoreCase("ID"))
								{
									/* do nothing -- nothing to do */
								}
								else
								if(parm.equalsIgnoreCase("REQUIRES"))
								{
									final Map<String,String> srcParms=CMParms.parseEQParms(srcParmVal, REQUIRES_DELIMITERS);
									final Map<String,String> tgtParms=CMParms.parseEQParms(tgtParmVal, REQUIRES_DELIMITERS);
									for(final String srcKey : srcParms.keySet())
									{
										final String srcVal=srcParms.get(srcKey);
										final String tgtVal=tgtParms.get(srcKey);
										if(tgtVal == null)
											tgtParms.put(srcKey, srcVal);
										else
										if(UPPER_REQUIRES_KEYWORDS.contains(srcVal.toUpperCase()))
										{
											if(!srcVal.equalsIgnoreCase(tgtVal))
												Log.errOut("Unable to merge REQUIRES parm on tags with ID: "+id+", mismatch in requirements for '"+srcKey+"'.");
										}
										else
										if(UPPER_REQUIRES_KEYWORDS.contains(tgtVal.toUpperCase()))
											Log.errOut("Unable to merge REQUIRES parm on tags with ID: "+id+", mismatch in requirements for '"+srcKey+"'.");
										else
											tgtParms.put(srcKey,srcVal+";"+tgtVal);
									}
									tgt.parms().put(parm, CMParms.combineEQParms(tgtParms, ','));
								}
								else
								if(parm.equalsIgnoreCase("CONDITION")||parm.equalsIgnoreCase("VALIDATE"))
									tgt.parms().put(parm, tgtParmVal+" and "+srcParmVal);
								else
								if(parm.equalsIgnoreCase("INSERT")||parm.equalsIgnoreCase("DEFINE")||parm.equalsIgnoreCase("PREDEFINE"))
									tgt.parms().put(parm, tgtParmVal+","+srcParmVal);
								else
									Log.errOut("Unable to merge SELECT parm on tags with ID: "+id+".");
							}
							for(final XMLLibrary.XMLTag subPiece : src.contents())
							{
								final Boolean subMergeVal;
								if((subPiece.parms()==null)||(!subPiece.parms().containsKey("MERGE")))
									subMergeVal = null;
								else
									subMergeVal = Boolean.valueOf(CMath.s_bool(subPiece.parms().get("MERGE")));
								if((subMergeVal == null)||(subMergeVal.booleanValue()))
									tgt.contents().add(subPiece);
							}
						}
					}
				}
				else
					defined.put(id.toUpperCase().trim(),piece);
			}
			final String load = piece.getParmValue("LOAD");
			if((load!=null)&&(load.length()>0))
			{
				piece.parms().remove("LOAD");
				XMLTag loadedPiece=(XMLTag)defined.get("SYSTEM_LOADED_XML_FILES");
				if(loadedPiece==null)
				{
					loadedPiece=CMLib.xml().createNewTag("SYSTEM_LOADED_XML_FILES","");
					defined.put("SYSTEM_LOADED_XML_FILES", loadedPiece);
				}
				final CMFile file = new CMFile(load,null,CMFile.FLAG_LOGERRORS|CMFile.FLAG_FORCEALLOW);
				if(loadedPiece.getPieceFromPieces( file.getAbsolutePath().toUpperCase())==null)
				{
					loadedPiece.contents().add(CMLib.xml().createNewTag(file.getAbsolutePath().toUpperCase(),"true"));
					if(file.exists() && file.canRead())
					{
						final List<XMLTag> addPieces=CMLib.xml().parseAllXML(file.text());
						piece.contents().addAll(addPieces);
					}
				}
			}
			buildDefinedIDSet(piece.contents(),defined);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean activate()
	{
		final String filePath="com/planet_ink/coffee_mud/Libraries/layouts";
		final CMProps page = CMProps.instance();
		final Vector<Object> layouts=CMClass.loadClassList(filePath,page.getStr("LIBRARY"),"/layouts",LayoutManager.class,true);
		for(int f=0;f<layouts.size();f++)
		{
			final LayoutManager lmgr= (LayoutManager)layouts.elementAt(f);
			final Class<LayoutManager> lmgrClass=(Class<LayoutManager>)lmgr.getClass();
			mgrs.put(lmgr.name().toUpperCase().trim(),lmgrClass);
		}
		return true;
	}

	@Override
	public boolean shutdown()
	{
		mgrs.clear();
		return true;
	}

	private final static class PostProcessException extends Exception
	{
		private static final long serialVersionUID = -8797769166795010761L;
		public PostProcessException(String s) 
		{
			super(s);
		}
	}

	protected abstract class PostProcessAttempt
	{
		protected Map<String,Object> defined=null;
		protected boolean firstRun=true;
		public abstract String attempt() throws CMException,PostProcessException;
	}

	@SuppressWarnings("unchecked")
	protected final String PostProcessAttempter(final Map<String,Object> defined, final PostProcessAttempt attempter) throws CMException
	{
		try
		{
			attempter.defined=defined;
			final String result = attempter.attempt();
			return result;
		}
		catch(PostProcessException pe)
		{
			List<PostProcessAttempt> posties=(List<PostProcessAttempt>)defined.get(MUDPercolator.POST_PROCESSING_STAT_SETS);
			if(posties==null)
			{
				posties=new Vector<PostProcessAttempt>();
				defined.put(MUDPercolator.POST_PROCESSING_STAT_SETS, posties);
			}
			attempter.firstRun=false;
			final Map<String,Object> definedCopy;
			if(defined instanceof Hashtable)
				definedCopy=(Map<String,Object>)((Hashtable<String,Object>)defined).clone();
			else
				definedCopy=new Hashtable<String,Object>(defined);
			attempter.defined=definedCopy;
			posties.add(attempter);
			return null;
		}
	}

	protected void fillOutRequiredStatCodeSafe(final Modifiable E, final List<String> ignoreStats, final String defPrefix, 
			final String tagName, final String statName, final XMLTag piece, final Map<String,Object> defined) throws CMException
	{
		PostProcessAttempter(defined,new PostProcessAttempt() {
			@Override
			public String attempt() throws CMException, PostProcessException 
			{
				final String val = findString(E,ignoreStats,defPrefix,tagName,piece,this.defined);
				E.setStat(statName, val);
				if((defPrefix!=null)&&(defPrefix.length()>0))
					addDefinition(defPrefix+tagName,val,defined);
				return val;
			}
		});
	}

	// vars created: ROOM_CLASS, ROOM_TITLE, ROOM_DESCRIPTION, ROOM_CLASSES, ROOM_TITLES, ROOM_DESCRIPTIONS
	@Override
	public Room buildRoom(XMLTag piece, Map<String,Object> defined, Exit[] exits, int direction) throws CMException
	{
		addDefinition("DIRECTION",CMLib.directions().getDirectionName(direction).toLowerCase(),defined);

		final String classID = findStringNow("class",piece,defined);
		final Room R = CMClass.getLocale(classID);
		if(R == null)
			throw new CMException("Unable to build room on classID '"+classID+"', Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
		addDefinition("ROOM_CLASS",classID,defined);
		final List<String> ignoreStats=new XVector<String>(new String[]{"CLASS","DISPLAY","DESCRIPTION"});
		fillOutRequiredStatCodeSafe(R, ignoreStats, "ROOM_", "TITLE", "DISPLAY", piece, defined);
		fillOutRequiredStatCodeSafe(R, ignoreStats, "ROOM_", "DESCRIPTION", "DESCRIPTION", piece, defined);
		fillOutCopyCodes(R, ignoreStats, "ROOM_", piece, defined);
		fillOutStatCodes(R, ignoreStats, "ROOM_", piece, defined);
		final List<MOB> mV = findMobs(piece,defined);
		for(int i=0;i<mV.size();i++)
		{
			final MOB M=mV.get(i);
			M.setSavable(true);
			M.bringToLife(R,true);
		}
		final List<Item> iV = findItems(piece,defined);
		for(int i=0;i<iV.size();i++)
		{
			final Item I=iV.get(i);
			R.addItem(I);
			I.setSavable(true);
			I.setExpirationDate(0);
		}
		final List<Ability> aV = findAffects(R,piece,defined,null);
		for(int i=0;i<aV.size();i++)
		{
			final Ability A=aV.get(i);
			A.setSavable(true);
			R.addNonUninvokableEffect(A);
		}
		final List<Behavior> bV = findBehaviors(R,piece,defined);
		for(int i=0;i<bV.size();i++)
		{
			final Behavior B=bV.get(i);
			B.setSavable(true);
			R.addBehavior(B);
		}
		for(int dir=0;dir<Directions.NUM_DIRECTIONS();dir++)
		{
			Exit E=exits[dir];
			if((E==null)&&(defined.containsKey("ROOMLINK_"+CMLib.directions().getDirectionChar(dir).toUpperCase())))
			{
				defined.put("ROOMLINK_DIR",CMLib.directions().getDirectionChar(dir).toUpperCase());
				final Exit E2=findExit(R,piece, defined);
				if(E2!=null)
					E=E2;
				defined.remove("ROOMLINK_DIR");
				if(CMSecurity.isDebugging(CMSecurity.DbgFlag.MUDPERCOLATOR))
					Log.debugOut("MUDPercolator","EXIT:NEW:"+((E==null)?"null":E.ID())+":DIR="+CMLib.directions().getDirectionChar(dir).toUpperCase()+":ROOM="+R.getStat("DISPLAY"));
			}
			else
			if((CMSecurity.isDebugging(CMSecurity.DbgFlag.MUDPERCOLATOR))&&defined.containsKey("ROOMLINK_"+CMLib.directions().getDirectionChar(dir).toUpperCase()))
				Log.debugOut("MUDPercolator","EXIT:OLD:"+((E==null)?"null":E.ID())+":DIR="+CMLib.directions().getDirectionChar(dir).toUpperCase()+":ROOM="+R.getStat("DISPLAY"));
			R.setRawExit(dir, E);
			R.startItemRejuv();
		}
		return R;
	}

	@SuppressWarnings("unchecked")

	@Override
	public void postProcess(final Map<String,Object> defined) throws CMException
	{
		final List<PostProcessAttempt> posties=(List<PostProcessAttempt>)defined.get(MUDPercolator.POST_PROCESSING_STAT_SETS);
		if(posties == null)
			return;
		try
		{
			for(final PostProcessAttempt stat : posties)
			{
				try
				{
					stat.attempt();
				}
				catch(PostProcessException pe)
				{
					throw pe;
				}
			}
		}
		catch(PostProcessException pe)
		{
			throw new CMException("Unsatisfied Post Process Exception: "+pe.getMessage(),pe);
		}
	}

	protected void layoutRecursiveFill(LayoutNode n, HashSet<LayoutNode> nodesDone, Vector<LayoutNode> group, LayoutTypes type)
	{
		if(n != null)
		for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
		{
			if(n.links().containsKey(Integer.valueOf(d)))
			{
				final LayoutNode offN=n.links().get(Integer.valueOf(d));
				if((offN.type()==type)
				&&(!group.contains(offN))
				&&(!nodesDone.contains(offN)))
				{
					group.addElement(offN);
					nodesDone.add(offN);
					layoutRecursiveFill(offN,nodesDone,group,type);
				}
			}
		}
	}

	protected void layoutFollow(LayoutNode n, LayoutTypes type, int direction, HashSet<LayoutNode> nodesAlreadyGrouped, List<LayoutNode> group)
	{
		n=n.links().get(Integer.valueOf(direction));
		while((n != null) &&(n.type()==LayoutTypes.street) &&(!group.contains(n) &&(!nodesAlreadyGrouped.contains(n))))
		{
			nodesAlreadyGrouped.add(n);
			group.add(n);
			n=n.links().get(Integer.valueOf(direction));
		}
	}

	@Override
	public Area findArea(XMLTag piece, Map<String,Object> defined, int directions) throws CMException
	{
		try
		{
			final String tagName="AREA";
			final List<XMLLibrary.XMLTag> choices = getAllChoices(null,null,null,tagName, piece, defined,true);
			if((choices==null)||(choices.size()==0))
			{
				return null;
			}
			while(choices.size()>0)
			{
				final XMLLibrary.XMLTag valPiece = choices.get(CMLib.dice().roll(1,choices.size(),-1));
				choices.remove(valPiece);
				if(valPiece.parms().containsKey("VALIDATE") && !testCondition(null,null,null,CMLib.xml().restoreAngleBrackets(valPiece.getParmValue("VALIDATE")),valPiece, defined))
					continue;
				final Map<String,Object> rDefined=new Hashtable<String,Object>();
				rDefined.putAll(defined);
				defineReward(null,null,null,valPiece.getParmValue("DEFINE"),valPiece,null,rDefined,true);
				final Area A=buildArea(valPiece,rDefined,directions);
				for (final String key : rDefined.keySet())
				{
					if(key.startsWith("_"))
						defined.put(key,rDefined.get(key));
				}
				return A;
			}
		}
		catch(PostProcessException pe)
		{
			throw new CMException("Unable to post process this object type: "+pe.getMessage(),pe);
		}
		return null;
	}

	protected Area buildArea(final XMLTag piece, final Map<String,Object> defined, int direction) throws CMException
	{
		defined.put("DIRECTION",CMLib.directions().getDirectionName(direction).toLowerCase());

		final String classID = findStringNow("class",piece,defined);
		final Area A = CMClass.getAreaType(classID);
		if(A == null)
			throw new CMException("Unable to build area on classID '"+classID+"', Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
		defined.put("AREA_CLASS",classID);
		final String name = findStringNow(A,null,"AREA_","NAME",piece,defined);
		if(CMLib.map().getArea(name)!=null)
		{
			A.destroy();
			throw new CMException("Unable to create area '"+name+"', you must destroy the old one first.");
		}
		A.setName(name);
		defined.put("AREA_NAME",name);
		
		PostProcessAttempter(defined,new PostProcessAttempt() {
			@Override
			public String attempt() throws CMException, PostProcessException 
			{
				final String author = findOptionalString(A,null,"AREA_","author",piece,this.defined, false);
				if(author != null)
				{
					A.setAuthorID(author);
					defined.put("AREA_AUTHOR",author);
				}
				return author;
			}
		});
		PostProcessAttempter(defined,new PostProcessAttempt() {
			@Override
			public String attempt() throws CMException, PostProcessException 
			{
				final String description = findOptionalString(A,null,"AREA_","description",piece,this.defined, false);
				if(description != null)
				{
					A.setDescription(description);
					defined.put("AREA_DESCRIPTION",description);
				}
				return description;
			}
		});

		if(fillInArea(piece, defined, A, direction))
			return A;
		throw new CMException("Unable to build area for some reason.");
	}

	protected void updateLayoutDefinitions(Map<String,Object> defined, Map<String,Object> groupDefined, 
										   Map<List<LayoutNode>,Map<String,Object>> groupDefinitions, List<List<LayoutNode>> roomGroups)
	{
		for (final String key : groupDefined.keySet())
		{
			if(key.startsWith("__"))
			{
				if(CMSecurity.isDebugging(CMSecurity.DbgFlag.MUDPERCOLATOR))
					Log.debugOut("AREADEF:"+key+"="+CMStrings.limit(groupDefined.get(key).toString(), 10));
				defined.put(key, groupDefined.get(key));
				for(final List<LayoutNode> group2 : roomGroups)
				{
					final Map<String,Object> groupDefined2 = groupDefinitions.get(group2);
					if(groupDefined2!=groupDefined)
						groupDefined2.put(key, groupDefined.get(key));
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected Room layOutRooms(Area A, LayoutManager layoutManager, int size, int direction, XMLTag piece, Map<String,Object> defined) throws CMException
	{
		if(CMSecurity.isDebugging(CMSecurity.DbgFlag.MUDPERCOLATOR))
			Log.debugOut("MudPercolator","Using LayoutManager:"+layoutManager.name());
		final Random random=new Random(System.currentTimeMillis());
		final List<LayoutNode> roomsToLayOut = layoutManager.generate(size,direction);
		if((roomsToLayOut==null)||(roomsToLayOut.size()==0))
			throw new CMException("Unable to fill area of size "+size+" off layout "+layoutManager.name());
		int numLeafs=0;
		for(int i=0;i<roomsToLayOut.size();i++)
		{
			final LayoutNode node=roomsToLayOut.get(i);
			if(node.type()==LayoutTypes.leaf)
				numLeafs++;
		}
		defined.put("AREA_NUMLEAFS", ""+numLeafs);

		// now break our rooms into logical groups, generate those rooms.
		final List<List<LayoutNode>> roomGroups = new Vector<List<LayoutNode>>();
		final LayoutNode magicRoomNode = roomsToLayOut.get(0);
		final HashSet<LayoutNode> nodesAlreadyGrouped=new HashSet<LayoutNode>();
		boolean keepLooking=true;
		while(keepLooking)
		{
			keepLooking=false;
			for(int i=0;i<roomsToLayOut.size();i++)
			{
				final LayoutNode node=roomsToLayOut.get(i);
				if(node.type()==LayoutTypes.leaf)
				{
					final Vector<LayoutNode> group=new Vector<LayoutNode>();
					group.add(node);
					nodesAlreadyGrouped.add(node);
					for(final Integer linkDir : node.links().keySet())
					{
						final LayoutNode dirNode=node.links().get(linkDir);
						if(!nodesAlreadyGrouped.contains(dirNode))
						{
							if((dirNode.type()==LayoutTypes.leaf)
							||(dirNode.type()==LayoutTypes.interior)&&(node.isFlagged(LayoutFlags.offleaf)))
							{
								group.addElement(dirNode);
								nodesAlreadyGrouped.add(dirNode);
							}
						}
					}
					for(final LayoutNode n : group)
						roomsToLayOut.remove(n);
					if(group.size()>0)
					{
						// randomize the leafs a bit
						if(roomGroups.size()==0)
							roomGroups.add(group);
						else 
							roomGroups.add(random.nextInt(roomGroups.size()),group);
					}
					keepLooking=true;
					break;
				}
			}
		}
		keepLooking=true;
		while(keepLooking)
		{
			keepLooking=false;
			for(int i=0;i<roomsToLayOut.size();i++)
			{
				final LayoutNode node=roomsToLayOut.get(i);
				if(node.type()==LayoutTypes.street)
				{
					final List<LayoutNode> group=new Vector<LayoutNode>();
					group.add(node);
					nodesAlreadyGrouped.add(node);
					final LayoutRuns run=node.getFlagRuns();
					if(run==LayoutRuns.ns)
					{
						layoutFollow(node,LayoutTypes.street,Directions.NORTH,nodesAlreadyGrouped,group);
						layoutFollow(node,LayoutTypes.street,Directions.SOUTH,nodesAlreadyGrouped,group);
					}
					else
					if(run==LayoutRuns.ew)
					{
						layoutFollow(node,LayoutTypes.street,Directions.EAST,nodesAlreadyGrouped,group);
						layoutFollow(node,LayoutTypes.street,Directions.WEST,nodesAlreadyGrouped,group);
					}
					else
					if(run==LayoutRuns.nesw)
					{
						layoutFollow(node,LayoutTypes.street,Directions.NORTHEAST,nodesAlreadyGrouped,group);
						layoutFollow(node,LayoutTypes.street,Directions.SOUTHWEST,nodesAlreadyGrouped,group);
					}
					else
					if(run==LayoutRuns.nwse)
					{
						layoutFollow(node,LayoutTypes.street,Directions.NORTHWEST,nodesAlreadyGrouped,group);
						layoutFollow(node,LayoutTypes.street,Directions.SOUTHEAST,nodesAlreadyGrouped,group);
					}
					else
					{
						int topDir=-1;
						int topSize=-1;
						for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
						{
							if(node.links().get(Integer.valueOf(d))!=null)
							{
								final List<LayoutNode> grpCopy=new XVector<LayoutNode>(group);
								final HashSet<LayoutNode> nodesAlreadyGroupedCopy=(HashSet<LayoutNode>)nodesAlreadyGrouped.clone();
								layoutFollow(node,LayoutTypes.street,d,nodesAlreadyGroupedCopy,grpCopy);
								if(node.links().get(Integer.valueOf(Directions.getOpDirectionCode(d)))!=null)
									layoutFollow(node,LayoutTypes.street,Directions.getOpDirectionCode(d),nodesAlreadyGroupedCopy,grpCopy);
								if(grpCopy.size()>topSize)
								{
									topSize=grpCopy.size();
									topDir=d;
								}
							}
						}
						if(topDir>=0)
						{
							layoutFollow(node,LayoutTypes.street,topDir,nodesAlreadyGrouped,group);
							if(node.links().get(Integer.valueOf(Directions.getOpDirectionCode(topDir)))!=null)
								layoutFollow(node,LayoutTypes.street,Directions.getOpDirectionCode(topDir),nodesAlreadyGrouped,group);
						}
					}
					for(final LayoutNode n : group)
						roomsToLayOut.remove(n);
					if(group.size()>0)
						roomGroups.add(group);
					keepLooking=true;
				}
			}
		}

		while(roomsToLayOut.size() >0)
		{
			final LayoutNode node=roomsToLayOut.get(0);
			final Vector<LayoutNode> group=new Vector<LayoutNode>();
			group.add(node);
			nodesAlreadyGrouped.add(node);
			layoutRecursiveFill(node,nodesAlreadyGrouped,group,node.type());
			for(final LayoutNode n : group)
				roomsToLayOut.remove(n);
			roomGroups.add(group);
		}
		
		// make CERTAIN that the magic first room in the layout always
		// gets ID#0.
		List<LayoutNode> magicGroup=null;
		for(int g=0;g<roomGroups.size();g++)
		{
			final List<LayoutNode> group=roomGroups.get(g);
			if(group.contains(magicRoomNode))
			{
				magicGroup=group;
				break;
			}
		}
		if(CMSecurity.isDebugging(CMSecurity.DbgFlag.MUDPERCOLATOR))
		for(int g=0;g<roomGroups.size();g++)
		{
			final Vector<LayoutNode> group=(Vector<LayoutNode>)roomGroups.get(g);
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.MUDPERCOLATOR))
				Log.debugOut("MudPercolator","GROUP:"+A.Name()+": "+group.firstElement().type().toString()+": "+group.size());
		}
		final Map<List<LayoutNode>,Map<String,Object>> groupDefinitions=new Hashtable<List<LayoutNode>,Map<String,Object>>();
		for(final List<LayoutNode> group : roomGroups)
		{
			final Map<String,Object> newDefined=new Hashtable<String,Object>();
			newDefined.putAll(defined);
			groupDefinitions.put(group, newDefined);
		}
		
		Map<String,Object> groupDefined = groupDefinitions.get(magicGroup);
		final Room magicRoom = processRoom(A,direction,piece,magicRoomNode,groupDefined);
		for(Map<String,Object> otherDefineds : groupDefinitions.values())
		{
			otherDefineds.remove("ROOMTAG_NODEGATEEXIT");
			otherDefineds.remove("ROOMTAG_GATEEXITROOM");
		}
		updateLayoutDefinitions(defined,groupDefined,groupDefinitions,roomGroups);

		//now generate the rooms and add them to the area
		for(final List<LayoutNode> group : roomGroups)
		{
			groupDefined = groupDefinitions.get(group);

			for(final LayoutNode node : group)
			{
				if(node!=magicRoomNode)
					processRoom(A,direction,piece,node,groupDefined);
			}
			updateLayoutDefinitions(defined,groupDefined,groupDefinitions,roomGroups);
		}

		for(final Integer linkDir : magicRoomNode.links().keySet())
		{
			final LayoutNode linkNode=magicRoomNode.getLink(linkDir.intValue());
			if((magicRoom.getRawExit(linkDir.intValue())==null) || (linkNode.room() == null))
				Log.errOut("MUDPercolator","Generated an unpaired node for "+magicRoom.roomID());
			else
				magicRoom.rawDoors()[linkDir.intValue()]=linkNode.room();
		}
		
		//now do a final-link on the rooms
		for(final List<LayoutNode> group : roomGroups)
		{
			for(final LayoutNode node : group)
			{
				final Room R=node.room();
				if(node != magicRoomNode)
				{
					if((R==null)||(node.links().keySet().size()==0))
						Log.errOut("MUDPercolator",layoutManager.name()+" generated a linkless node: "+node.toString());
					else
					for(final Integer linkDir : node.links().keySet())
					{
						final LayoutNode linkNode=node.getLink(linkDir.intValue());
						if((R.getRawExit(linkDir.intValue())==null) || (linkNode.room() == null))
							Log.errOut("MUDPercolator","Generated an unpaired node for "+R.roomID());
						else
							R.rawDoors()[linkDir.intValue()]=linkNode.room();
					}
				}
			}
		}
		return magicRoom;
	}

	// vars created: LINK_DIRECTION, AREA_CLASS, AREA_NAME, AREA_DESCRIPTION, AREA_LAYOUT, AREA_SIZE
	@Override
	public boolean fillInArea(XMLTag piece, Map<String,Object> defined, Area A, int direction) throws CMException
	{
		final String layoutType = findStringNow("layout",piece,defined);
		if((layoutType==null)||(layoutType.trim().length()==0))
			throw new CMException("Unable to build area without defined layout");
		final LayoutManager layoutManager = getLayoutManager(layoutType);
		if(layoutManager == null)
			throw new CMException("Undefined Layout "+layoutType);
		defined.put("AREA_LAYOUT",layoutManager.name());
		final String size = findStringNow("size",piece,defined);
		if((!CMath.isInteger(size))||(CMath.s_int(size)<=0))
			throw new CMException("Unable to build area of size "+size);
		defined.put("AREA_SIZE",size);
		final List<String> ignoreStats=new XVector<String>(new String[]{"CLASS","NAME","DESCRIPTION","LAYOUT","SIZE"});
		fillOutStatCodes(A, ignoreStats,"AREA_",piece,defined);

		final List<Ability> aV = findAffects(A,piece,defined,null);
		for(int i=0;i<aV.size();i++)
		{
			final Ability AB=aV.get(i);
			A.setSavable(true);
			A.addNonUninvokableEffect(AB);
		}
		final List<Behavior> bV = findBehaviors(A,piece,defined);
		for(int i=0;i<bV.size();i++)
		{
			final Behavior B=bV.get(i);
			B.setSavable(true);
			A.addBehavior(B);
		}

		CMLib.map().addArea(A); // necessary for proper naming.

		try
		{
			layOutRooms(A, layoutManager, CMath.s_int(size), direction, piece, defined);
			CMLib.map().delArea(A); // we added it for id assignment, now we are done.
		}
		catch(final Exception t)
		{
			CMLib.map().delArea(A);
			CMLib.map().emptyAreaAndDestroyRooms(A);
			A.destroy();
			if(t instanceof CMException)
				throw (CMException)t;
			throw new CMException(t.getMessage(),t);
		}
		return true;
	}

	protected Room processRoom(Area A, int direction, XMLTag piece, LayoutNode node, Map<String,Object> groupDefined)
		throws CMException
	{
		for(final LayoutTags key : node.tags().keySet())
			groupDefined.put("ROOMTAG_"+key.toString().toUpperCase(),node.tags().get(key));
		final Exit[] exits=new Exit[Directions.NUM_DIRECTIONS()];
		for(final Integer linkDir : node.links().keySet())
		{
			final LayoutNode linkNode = node.links().get(linkDir);
			if(linkNode.room() != null)
			{
				final int opDir=Directions.getOpDirectionCode(linkDir.intValue());
				exits[linkDir.intValue()]=linkNode.room().getExitInDir(opDir);
				groupDefined.put("ROOMTITLE_"+CMLib.directions().getDirectionChar(linkDir.intValue()).toUpperCase(),linkNode.room().displayText(null));
			}
			groupDefined.put("NODETYPE_"+CMLib.directions().getDirectionChar(linkDir.intValue()).toUpperCase(),linkNode.type().name());
			//else groupDefined.put("ROOMTITLE_"+CMLib.directions().getDirectionChar(linkDir.intValue()).toUpperCase(),"");
			groupDefined.put("ROOMLINK_"+CMLib.directions().getDirectionChar(linkDir.intValue()).toUpperCase(),"true");
		}
		if(CMSecurity.isDebugging(CMSecurity.DbgFlag.MUDPERCOLATOR))
		{
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.MUDPERCOLATOR))
				Log.debugOut("MUDPercolator",A.Name()+": type: "+node.type().toString());
			final StringBuffer defs=new StringBuffer("");
			for (final String key : groupDefined.keySet())
			{
				defs.append(key+"="+CMStrings.limit(groupDefined.get(key).toString(),10)+",");
			}
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.MUDPERCOLATOR))
				Log.debugOut("MUDPercolator","DEFS: "+defs.toString());
		}
		final Room R=findRoom(A,piece, groupDefined, exits, direction);
		if(R==null)
			throw new CMException("Failure to generate room from "+piece.value());
		R.setRoomID(A.getNewRoomID(null,-1));
		if(CMSecurity.isDebugging(CMSecurity.DbgFlag.MUDPERCOLATOR))
			Log.debugOut("MUDPercolator","ROOMID: "+R.roomID());
		R.setArea(A);
		A.addProperRoom(R);
		node.setRoom(R);
		groupDefined.remove("ROOMTAG_NODEGATEEXIT");
		groupDefined.remove("ROOMTAG_GATEEXITROOM");
		for(final LayoutTags key : node.tags().keySet())
			groupDefined.remove("ROOMTAG_"+key.toString().toUpperCase());
		for(final Integer linkDir : node.links().keySet())
		{
			groupDefined.remove("NODETYPE_"+CMLib.directions().getDirectionChar(linkDir.intValue()).toUpperCase());
			groupDefined.remove("ROOMLINK_"+CMLib.directions().getDirectionChar(linkDir.intValue()).toUpperCase());
			groupDefined.remove("ROOMTITLE_"+CMLib.directions().getDirectionChar(linkDir.intValue()).toUpperCase());
		}
		return R;
	}

	@Override
	public List<MOB> findMobs(XMLTag piece, Map<String,Object> defined) throws CMException
	{
		return findMobs(null,piece, defined, null);
	}

	protected List<MOB> findMobs(Modifiable E,XMLTag piece, Map<String,Object> defined, BuildCallback callBack) throws CMException
	{
		try
		{
			final List<MOB> V = new Vector<MOB>();
			final String tagName="MOB";
			final List<XMLLibrary.XMLTag> choices = getAllChoices(E,null,null,tagName, piece, defined,true);
			if((choices==null)||(choices.size()==0))
				return V;
			for(int c=0;c<choices.size();c++)
			{
				final XMLTag valPiece = choices.get(c);
				if(valPiece.parms().containsKey("VALIDATE") && !testCondition(E,null,null,CMLib.xml().restoreAngleBrackets(valPiece.getParmValue("VALIDATE")),valPiece, defined))
					continue;
				defineReward(E,null,null,valPiece.getParmValue("DEFINE"),valPiece,null,defined,true);
				if(CMSecurity.isDebugging(CMSecurity.DbgFlag.MUDPERCOLATOR))
					Log.debugOut("MUDPercolator","Build Mob: "+CMStrings.limit(valPiece.value(),80)+"...");
				final Set<String> definedSet=getPrevouslyDefined(defined,tagName+"_");
				final MOB M=buildMob(valPiece,defined);
				if(callBack != null)
					callBack.willBuild(M, valPiece);
				V.add(M);
				clearNewlyDefined(defined, definedSet, tagName+"_");
			}
			return V;
		}
		catch(PostProcessException pe)
		{
			throw new CMException("Unable to post process this object type: "+pe.getMessage(),pe);
		}
	}

	protected Room findRoom(Area A, XMLTag piece, Map<String,Object> defined, Exit[] exits, int directions) throws CMException
	{
		try
		{
			final String tagName="ROOM";
			final List<XMLLibrary.XMLTag> choices = getAllChoices(null,null,null,tagName, piece, defined,true);
			if((choices==null)||(choices.size()==0))
			{
				return null;
			}
			while(choices.size()>0)
			{
				final XMLLibrary.XMLTag valPiece = choices.get(CMLib.dice().roll(1,choices.size(),-1));
				choices.remove(valPiece);
				if(valPiece.parms().containsKey("VALIDATE") && !testCondition(null,null,null,CMLib.xml().restoreAngleBrackets(valPiece.getParmValue("VALIDATE")),valPiece, defined))
					continue;
				final Map<String,Object> rDefined=new Hashtable<String,Object>();
				rDefined.putAll(defined);
				defineReward(null,null,null,valPiece.getParmValue("DEFINE"),valPiece,null,rDefined,true);
				if(CMSecurity.isDebugging(CMSecurity.DbgFlag.MUDPERCOLATOR))
					Log.debugOut("MUDPercolator","Build Room: "+CMStrings.limit(valPiece.value(),80)+"...");
				Room R;
				final String layoutType=valPiece.parms().get("LAYOUT");
				if((layoutType!=null)&&(layoutType.length()>0))
				{
					final LayoutManager layoutManager = getLayoutManager(layoutType);
					if(layoutManager == null)
						throw new CMException("Undefined room Layout "+layoutType);
					rDefined.put("ROOM_LAYOUT",layoutManager.name());
					final String size = findStringNow("size",valPiece,rDefined);
					if((!CMath.isInteger(size))||(CMath.s_int(size)<=0))
						throw new CMException("Unable to build room layout of size "+size);
					defined.put("ROOM_SIZE",size);
					R=layOutRooms(A, layoutManager, CMath.s_int(size), directions, valPiece, rDefined);
				}
				else
				{
					final Exit[] rExits=exits.clone();
					R=buildRoom(valPiece,rDefined,rExits,directions);
					for(int e=0;e<rExits.length;e++)
						exits[e]=rExits[e];
				}
				for (final String key : rDefined.keySet())
				{
					if(key.startsWith("_"))
					{
						if(CMSecurity.isDebugging(CMSecurity.DbgFlag.MUDPERCOLATOR))
							Log.debugOut("RGDEF:"+key+"="+CMStrings.limit(rDefined.get(key).toString(), 10));
						defined.put(key,rDefined.get(key));
					}
				}
				return R;
			}
		}
		catch(PostProcessException pe)
		{
			throw new CMException("Unable to post process this object type: "+pe.getMessage(),pe);
		}
		return null;
	}

	protected PairVector<Room,Exit[]> findRooms(XMLTag piece, Map<String,Object> defined, Exit[] exits, int direction) throws CMException
	{
		try
		{
			final PairVector<Room,Exit[]> DV = new PairVector<Room,Exit[]>();
			final String tagName="ROOM";
			final List<XMLLibrary.XMLTag> choices = getAllChoices(null,null,null,tagName, piece, defined,true);
			if((choices==null)||(choices.size()==0)) 
				return DV;
			for(int c=0;c<choices.size();c++)
			{
				final XMLTag valPiece = choices.get(c);
				if(valPiece.parms().containsKey("VALIDATE") && !testCondition(null,null,null,CMLib.xml().restoreAngleBrackets(valPiece.getParmValue("VALIDATE")),valPiece, defined))
					continue;
				defineReward(null,null,null,valPiece.getParmValue("DEFINE"),valPiece,null,defined,true);
				final Exit[] theseExits=exits.clone();
				if(CMSecurity.isDebugging(CMSecurity.DbgFlag.MUDPERCOLATOR))
					Log.debugOut("MUDPercolator","Build Room: "+CMStrings.limit(valPiece.value(),80)+"...");
				final Room R=buildRoom(valPiece,defined,theseExits,direction);
				DV.addElement(R,theseExits);
			}
			return DV;
		}
		catch(PostProcessException pe)
		{
			throw new CMException("Unable to post process this object type: "+pe.getMessage(),pe);
		}
	}

	protected Exit findExit(final Modifiable M, XMLTag piece, Map<String,Object> defined) throws CMException
	{
		try
		{
			final String tagName="EXIT";
			final List<XMLLibrary.XMLTag> choices = getAllChoices(M,null,null,tagName, piece, defined,true);
			if((choices==null)||(choices.size()==0))
				return null;
			final List<Exit> exitChoices = new Vector<Exit>();
			for(int c=0;c<choices.size();c++)
			{
				final XMLTag valPiece = choices.get(c);
				if(valPiece.parms().containsKey("VALIDATE") && !testCondition(M,null,null,CMLib.xml().restoreAngleBrackets(valPiece.getParmValue("VALIDATE")),valPiece, defined))
					continue;
				defineReward(M,null,null,valPiece.getParmValue("DEFINE"),valPiece,null,defined,true);
				if(CMSecurity.isDebugging(CMSecurity.DbgFlag.MUDPERCOLATOR))
					Log.debugOut("MUDPercolator","Build Exit: "+CMStrings.limit(valPiece.value(),80)+"...");
				final Exit E=buildExit(valPiece,defined);
				if(E!=null)
					exitChoices.add(E);
			}
			if(exitChoices.size()==0)
				return null;
			return exitChoices.get(CMLib.dice().roll(1,exitChoices.size(),-1));
		}
		catch(PostProcessException pe)
		{
			throw new CMException("Unable to post process this object type: "+pe.getMessage(),pe);
		}
	}

	private static class Varidentifier
	{
		public int outerStart=-1;
		public int outerEnd=-1;
		public String var=null;
		public boolean toLowerCase=false;
		public boolean toUpperCase=false;
		public boolean toCapitalized=false;
		public boolean toPlural=false;
		public boolean isMathExpression=false;
	}

	protected List<Varidentifier> parseVariables(String str)
	{
		int x=str.indexOf('$');
		final List<Varidentifier> list=new XVector<Varidentifier>();
		while((x>=0)&&(x<str.length()-1))
		{
			final Varidentifier var = new Varidentifier();
			var.outerStart=x;
			x++;
			if((x<str.length())&&(str.charAt(x)=='{'))
			{
				int varstart=var.outerStart;
				x++;
				while((x<str.length()-2)&&(str.charAt(x+1)==':'))
				{
					if((str.charAt(x)=='l')||(str.charAt(x)=='L'))
						var.toLowerCase=true;
					else
					if((str.charAt(x)=='u')||(str.charAt(x)=='U'))
						var.toUpperCase=true;
					else
					if((str.charAt(x)=='p')||(str.charAt(x)=='P'))
						var.toPlural=true;
					else
					if((str.charAt(x)=='c')||(str.charAt(x)=='C'))
						var.toCapitalized=true;
					x+=2;
					varstart+=2;
				}
				while((x<str.length())&&(str.charAt(x)!='}'))
					x++;
				var.var = str.substring(varstart+2,x);
				if(x<str.length())
					x++;
				var.outerEnd=x;
			}
			else
			if((x<str.length())&&(str.charAt(x)=='['))
			{
				final int varstart=var.outerStart;
				x++;
				while((x<str.length())&&(str.charAt(x)!=']'))
					x++;
				var.var = str.substring(varstart+2,x);
				var.isMathExpression=true;
				if(x<str.length())
					x++;
				var.outerEnd=x;
			}
			else
			{
				while((x<str.length())&&((str.charAt(x)=='_')||Character.isLetterOrDigit(str.charAt(x))))
					x++;
				var.var = str.substring(var.outerStart+1,x);
				var.outerEnd=x;
			}
			list.add(var);
			x=str.indexOf('$',var.outerEnd);
		}
		return list;
	}

	protected String fillOutStatCode(final Modifiable E, final List<String> ignoreStats, final String defPrefix, final String stat, final XMLTag piece, final Map<String,Object> defined, final boolean debug)
	{
		if(!ignoreStats.contains(stat.toUpperCase().trim()))
		{
			try
			{
				return PostProcessAttempter(defined,new PostProcessAttempt() {
					@Override
					public String attempt() throws CMException, PostProcessException 
					{
						String value;
						if((E instanceof MOB) && (stat.equals("ABILITY")))
							value = findOptionalString(E,ignoreStats,defPrefix,"HPMOD",piece,this.defined, debug);
						else
							value = findOptionalString(E,ignoreStats,defPrefix,stat,piece,this.defined, debug);
						if(value != null)
						{
							E.setStat(stat, value);
							if((defPrefix!=null)&&(defPrefix.length()>0))
								addDefinition(defPrefix+stat,value,this.defined);
						}
						return value;
					}
				});
			}
			catch(CMException e)
			{
				Log.errOut(e);
				//should never happen
			}
		}
		return null;
	}

	protected void fillOutStatCodes(Modifiable E, List<String> ignoreStats, String defPrefix, XMLTag piece, Map<String,Object> defined)
	{
		final String[] statCodes = E.getStatCodes();
		for (final String stat : statCodes)
		{
			fillOutStatCode(E,ignoreStats,defPrefix,stat,piece,defined, false);
		}
	}

	protected void fillOutCopyStats(final Modifiable E, final Modifiable E2)
	{
		if((E2 instanceof MOB)&&(!((MOB)E2).isGeneric()))
		{
			for(final GenericBuilder.GenMOBCode stat : GenericBuilder.GenMOBCode.values())
			{
				if(stat != GenericBuilder.GenMOBCode.ABILITY) // because this screws up gen hit points
				{
					E.setStat(stat.name(), CMLib.coffeeMaker().getGenMobStat((MOB)E2,stat.name()));
				}
			}
		}
		else
		if((E2 instanceof Item)&&(!((Item)E2).isGeneric()))
		{
			for(final GenericBuilder.GenItemCode stat : GenericBuilder.GenItemCode.values())
			{
				E.setStat(stat.name(),CMLib.coffeeMaker().getGenItemStat((Item)E2,stat.name()));
			}
		}
		else
		for(final String stat : E2.getStatCodes())
		{
			E.setStat(stat, E2.getStat(stat));
		}
	}

	protected boolean fillOutCopyCodes(final Modifiable E, List<String> ignoreStats, String defPrefix, XMLTag piece, Map<String,Object> defined) throws CMException
	{
		final String copyStatID = findOptionalStringNow(E,ignoreStats,defPrefix,"COPYOF",piece,defined, false);
		if(copyStatID!=null)
		{
			final List<String> V=CMParms.parseCommas(copyStatID,true);
			for(int v=0;v<V.size();v++)
			{
				String s = V.get(v);
				if(s.startsWith("$"))
					s=s.substring(1).trim();
				final XMLTag statPiece =(XMLTag)defined.get(s.toUpperCase().trim());
				if(statPiece == null)
				{
					Object o=CMClass.getMOBPrototype(s);
					if(o==null)
						o=CMClass.getItemPrototype(s);
					if(o==null)
						o=CMClass.getObjectOrPrototype(s);
					if(!(o instanceof Modifiable))
						throw new CMException("Invalid copystat: '"+s+"' on piece '"+piece.tag()+"', Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
					final Modifiable E2=(Modifiable)o;
					if(o instanceof MOB)
					{
						((MOB)E2).setBaseCharStats((CharStats)((MOB)o).baseCharStats().copyOf());
						((MOB)E2).setBasePhyStats((PhyStats)((MOB)o).basePhyStats().copyOf());
						((MOB)E2).setBaseState((CharState)((MOB)o).baseState().copyOf());
					}
					fillOutCopyStats(E,E2);
				}
				else
				{
					final XMLTag likePiece =(XMLTag)defined.get(s.toUpperCase().trim());
					if(likePiece == null)
						throw new CMException("Invalid copystat: '"+s+"' on piece '"+piece.tag()+"', Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
					final BuildCallback callBack=new BuildCallback()
					{
						@Override 
						public void willBuild(Environmental E2, XMLTag XMLTag)
						{
							fillOutCopyStats(E,E2);
						}
					};
					findItems(E,likePiece,defined,callBack);
					findMobs(E,likePiece,defined,callBack);
					findAbilities(E,likePiece,defined,callBack);
					findExits(E,likePiece,defined,callBack);
				}
			}
			return V.size()>0;
		}
		return false;
	}

	protected MOB buildMob(final XMLTag piece, final Map<String,Object> defined) throws CMException
	{
		final String classID = findStringNow("class",piece,defined);
		MOB M = null;
		final List<String> ignoreStats=new XVector<String>();
		boolean copyFilled = false;
		if(classID.equalsIgnoreCase("catalog"))
		{
			final String name = findStringNow("NAME",piece,defined);
			if((name == null)||(name.length()==0))
				throw new CMException("Unable to build a catalog mob without a name, Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
			M = CMLib.catalog().getCatalogMob(name);
			if(M==null)
				throw new CMException("Unable to find cataloged mob called '"+name+"', Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
			M=(MOB)M.copyOf();
			CMLib.catalog().changeCatalogUsage(M,true);
			addDefinition("MOB_CLASS",M.ID(),defined);
			copyFilled=true;
		}
		else
		{
			M = CMClass.getMOB(classID);
			if(M == null)
				throw new CMException("Unable to build mob on classID '"+classID+"', Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
			addDefinition("MOB_CLASS",classID,defined);

			if(M.isGeneric())
			{
				copyFilled = fillOutCopyCodes(M,ignoreStats,"MOB_",piece,defined);
				String name = fillOutStatCode(M,ignoreStats,"MOB_","NAME",piece,defined, false);
				if((!copyFilled) && ((name == null)||(name.length()==0)))
					name = fillOutStatCode(M,ignoreStats,"MOB_","NAME",piece,defined, false);
				if((!copyFilled) && ((name == null)||(name.length()==0)))
				{
					name = fillOutStatCode(M,ignoreStats,"MOB_","NAME",piece,defined, true);
					if((!copyFilled) && ((name == null)||(name.length()==0)))
						throw new CMException("Unable to build a mob without a name, Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
				}
				if((name != null)&&(name.length()>0))
					M.setName(name);
			}
		}
		final MOB mob=M;
		addDefinition("MOB_NAME",M.Name(),defined);
		if(!copyFilled)
			M.baseCharStats().setMyRace(CMClass.getRace("StdRace"));

		PostProcessAttempter(defined,new PostProcessAttempt() {
			@Override
			public String attempt() throws CMException, PostProcessException 
			{
				String value = findOptionalString(mob,ignoreStats,"MOB_","LEVEL",piece,this.defined, false);
				if((value != null)&&(value.length()>0))
				{
					mob.setStat("LEVEL",value);
					addDefinition("MOB_LEVEL",value,this.defined);
					CMLib.leveler().fillOutMOB(mob,mob.basePhyStats().level());
					CMLib.leveler().fillOutMOB(mob,mob.basePhyStats().level());
				}
				return value;
			}
		});
		
		PostProcessAttempter(defined,new PostProcessAttempt() {
			@Override
			public String attempt() throws CMException, PostProcessException 
			{
				String value = findOptionalString(mob,ignoreStats,"MOB_","GENDER",piece,this.defined, false);
				if((value != null)&&(value.length()>0))
				{
					mob.baseCharStats().setStat(CharStats.STAT_GENDER,value.charAt(0));
					addDefinition("MOB_GENDER",value,this.defined);
				}
				else
					mob.baseCharStats().setStat(CharStats.STAT_GENDER,CMLib.dice().rollPercentage()>50?'M':'F');
				PostProcessAttempter(this.defined,new PostProcessAttempt() {
					@Override
					public String attempt() throws CMException, PostProcessException 
					{
						String value = findOptionalString(mob,ignoreStats,"MOB_","RACE",piece,this.defined, false);
						if((value != null)&&(value.length()>0))
						{
							mob.setStat("RACE",value);
							addDefinition("MOB_RACE",value,this.defined);
							Race R=CMClass.getRace(value);
							if(R==null)
							{
								final List<Race> races=findRaces(mob,piece, this.defined);
								if(races.size()>0)
									R=races.get(CMLib.dice().roll(1, races.size(), -1));
							}
							if(R!=null)
								R.setHeightWeight(mob.basePhyStats(),(char)mob.baseCharStats().getStat(CharStats.STAT_GENDER));
						}
						return value;
					}
				});
				return value;
			}
		});

		PostProcessAttempter(defined, new PostProcessAttempt()
		{
			@Override
			public String attempt() throws CMException, PostProcessException 
			{
				final List<XMLLibrary.XMLTag> choices = getAllChoices(mob,ignoreStats,"MOB_","FACTION", piece, this.defined, true);
				if((choices!=null)&&(choices.size()>0))
				{
					for(int c=0;c<choices.size();c++)
					{
						final XMLTag valPiece = choices.get(c);
						final String f = valPiece.getParmValue("ID");
						final String v = valPiece.getParmValue("VALUE");
						mob.addFaction(f, Integer.parseInt(v));
					}
				}
				return "";
			}
		});
		ignoreStats.addAll(Arrays.asList(new String[]{"CLASS","NAME","LEVEL","GENDER"}));
		fillOutStatCodes(M,ignoreStats,"MOB_",piece,defined);
		M.recoverCharStats();
		M.recoverPhyStats();
		M.recoverMaxState();

		final List<Item> items = findItems(piece,defined);
		for(int i=0;i<items.size();i++)
		{
			final Item I=items.get(i);
			final boolean wearable = (I.phyStats().sensesMask()&PhyStats.SENSE_ITEMNOAUTOWEAR)==0;
			M.addItem(I);
			I.setSavable(true);
			if(wearable)
				I.wearIfPossible(M);
		}
		final List<Ability> aV = findAffects(M,piece,defined,null);
		for(int i=0;i<aV.size();i++)
		{
			final Ability A=aV.get(i);
			A.setSavable(true);
			M.addNonUninvokableEffect(A);
		}
		final List<Behavior> bV= findBehaviors(M,piece,defined);
		for(int i=0;i<bV.size();i++)
		{
			final Behavior B=bV.get(i);
			B.setSavable(true);
			M.addBehavior(B);
		}
		final List<Ability> abV = findAbilities(M,piece,defined,null);
		for(int i=0;i<abV.size();i++)
		{
			final Ability A=abV.get(i);
			A.setSavable(true);
			M.addAbility(A);
		}
		final ShopKeeper SK=CMLib.coffeeShops().getShopKeeper(M);
		if(SK!=null)
		{
			final List<Triad<Environmental,Integer,Long>> iV = findShopInventory(M,piece,defined);
			if(iV.size()>0)
				SK.getShop().emptyAllShelves();
			for(int i=0;i<iV.size();i++)
				SK.getShop().addStoreInventory(iV.get(i).first,iV.get(i).second.intValue(),iV.get(i).third.intValue());
		}

		M.recoverCharStats();
		M.recoverPhyStats();
		M.recoverMaxState();
		M.text();
		M.setMiscText(M.text());
		M.recoverCharStats();
		M.recoverPhyStats();
		M.recoverMaxState();
		return M;
	}

	protected List<Exit> findExits(Modifiable M,XMLTag piece, Map<String,Object> defined, BuildCallback callBack) throws CMException
	{
		try
		{
			final List<Exit> V = new Vector<Exit>();
			final String tagName="EXIT";
			final List<XMLLibrary.XMLTag> choices = getAllChoices(null,null,null,tagName, piece, defined,true);
			if((choices==null)||(choices.size()==0))
				return V;
			for(int c=0;c<choices.size();c++)
			{
				final XMLTag valPiece = choices.get(c);
				if(valPiece.parms().containsKey("VALIDATE") && !testCondition(M,null,null,CMLib.xml().restoreAngleBrackets(valPiece.getParmValue("VALIDATE")),valPiece, defined))
					continue;
				if(CMSecurity.isDebugging(CMSecurity.DbgFlag.MUDPERCOLATOR))
					Log.debugOut("MUDPercolator","Build Exit: "+CMStrings.limit(valPiece.value(),80)+"...");
				defineReward(M,null,null,valPiece.getParmValue("DEFINE"),valPiece,null,defined,true);
				final Set<String> definedSet=getPrevouslyDefined(defined,tagName+"_");
				final Exit E=buildExit(valPiece,defined);
				if(callBack != null)
					callBack.willBuild(E, valPiece);
				V.add(E);
				clearNewlyDefined(defined, definedSet, tagName+"_");
			}
			return V;
		}
		catch(PostProcessException pe)
		{
			throw new CMException("Unable to post process this object type: "+pe.getMessage(),pe);
		}
	}

	// remember to check ROOMLINK_DIR for N,S,E,W,U,D,etc..
	protected Exit buildExit(XMLTag piece, Map<String,Object> defined) throws CMException
	{
		final List<String> ignoreStats=new XVector<String>();
		final String classID = findStringNow("class",piece,defined);
		final Exit E = CMClass.getExit(classID);
		if(E == null)
			throw new CMException("Unable to build exit on classID '"+classID+"', Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
		addDefinition("EXIT_CLASS",classID,defined);
		ignoreStats.add("CLASS");
		fillOutCopyCodes(E,ignoreStats,"EXIT_",piece,defined);
		fillOutStatCodes(E,ignoreStats,"EXIT_",piece,defined);
		final List<Ability> aV = findAffects(E,piece,defined,null);
		for(int i=0;i<aV.size();i++)
		{
			final Ability A=aV.get(i);
			A.setSavable(true);
			E.addNonUninvokableEffect(A);
		}
		final List<Behavior> bV= findBehaviors(E,piece,defined);
		for(int i=0;i<bV.size();i++)
		{
			final Behavior B=bV.get(i);
			B.setSavable(true);
			E.addBehavior(B);
		}
		E.text();
		E.setMiscText(E.text());
		E.recoverPhyStats();
		return E;
	}

	protected List<Triad<Environmental,Integer,Long>> findShopInventory(final Modifiable E,final XMLTag piece, final Map<String,Object> defined) throws CMException
	{
		try
		{
			final List<Triad<Environmental,Integer,Long>> V = new Vector<Triad<Environmental,Integer,Long>>();
			final List<XMLLibrary.XMLTag> choices = getAllChoices(E,null,null,"SHOPINVENTORY", piece, defined,true);
			if((choices==null)||(choices.size()==0))
				return V;
			for(int c=0;c<choices.size();c++)
			{
				final XMLTag shopPiece = choices.get(c);
				if(shopPiece.parms().containsKey("VALIDATE") && !testCondition(E,null,null,CMLib.xml().restoreAngleBrackets(shopPiece.getParmValue("VALIDATE")),shopPiece, defined))
					continue;
				final String baseNumber[] = { "1" };
				final String basePrice[] = { "-1" };
				try
				{
					final String baseStr=shopPiece.getParmValue("NUMBER");
					if(baseStr != null)
						baseNumber[0]=baseStr;
				}
				catch(final Exception e){ }
				try
				{
					final String baseStr=shopPiece.getParmValue("PRICE");
					if(baseStr != null)
						basePrice[0]=baseStr;
				}
				catch(final Exception e){ }
				final BuildCallback callBack=new BuildCallback()
				{
					@Override 
					public void willBuild(Environmental E, XMLTag XMLTag)
					{
						String numbStr=XMLTag.getParmValue("NUMBER");
						if(numbStr == null)
							numbStr=baseNumber[0];
						int number;
						try{ number=CMath.parseIntExpression(strFilter(E,null,null,numbStr.trim(),piece, defined)); } catch(final Exception e){ number=1; }
						numbStr=XMLTag.getParmValue("PRICE");
						if(numbStr == null)
							numbStr=basePrice[0];
						long price;
						try{ price=CMath.parseLongExpression(strFilter(E,null,null,numbStr.trim(),piece, defined)); } catch(final Exception e){ price=-1; }
						V.add(new Triad<Environmental,Integer,Long>(E,Integer.valueOf(number),Long.valueOf(price)));
					}
				};
				findItems(E,shopPiece,defined,callBack);
				findMobs(E,shopPiece,defined,callBack);
				findAbilities(E,shopPiece,defined,callBack);
			}
			return V;
		}
		catch(PostProcessException pe)
		{
			throw new CMException("Unable to post process this object type: "+pe.getMessage(),pe);
		}
	}

	protected Set<String> getPrevouslyDefined(Map<String,Object> defined, String prefix)
	{
		final Set<String> prevSet=new HashSet<String>();
		for(final String key : defined.keySet())
		{
			if(key.toUpperCase().startsWith(prefix.toUpperCase()))
				prevSet.add(key.toUpperCase());
		}
		return prevSet;
	}

	protected void clearNewlyDefined(Map<String,Object> defined, Set<String> exceptSet, String prefix)
	{
		final Set<String> clearSet=new HashSet<String>();
		for(final String key : defined.keySet())
			if(key.toUpperCase().startsWith(prefix.toUpperCase())
			&& (!exceptSet.contains(key.toUpperCase()))
			&& (!key.startsWith("_")))
				clearSet.add(key);
		for(final String key : clearSet)
			defined.remove(key);
	}

	@Override
	public List<Item> findItems(XMLTag piece, Map<String,Object> defined) throws CMException
	{
		return findItems(null,piece, defined, null);
	}

	protected List<Item> findItems(Modifiable E,XMLTag piece, Map<String,Object> defined, BuildCallback callBack) throws CMException
	{
		try
		{
			final List<Item> V = new Vector<Item>();
			final String tagName="ITEM";
			final List<XMLLibrary.XMLTag> choices = getAllChoices(E,null,null,tagName, piece, defined,true);
			if((choices==null)||(choices.size()==0))
				return V;
			for(int c=0;c<choices.size();c++)
			{
				final XMLTag valPiece = choices.get(c);
				if(valPiece.parms().containsKey("VALIDATE") && !testCondition(E,null,null,CMLib.xml().restoreAngleBrackets(valPiece.getParmValue("VALIDATE")),valPiece, defined))
					continue;
				defineReward(E,null,null,valPiece.getParmValue("DEFINE"),valPiece,null,defined,true);
				if(CMSecurity.isDebugging(CMSecurity.DbgFlag.MUDPERCOLATOR))
					Log.debugOut("MUDPercolator","Build Item: "+CMStrings.limit(valPiece.value(),80)+"...");
				final Set<String> definedSet=getPrevouslyDefined(defined,tagName+"_");
				try
				{
					for(final Item I : buildItem(valPiece,defined))
					{
						if(callBack != null)
							callBack.willBuild(I, valPiece);
						V.add(I);
					}
				}
				catch(final CMException e)
				{
					throw e;
				}
				clearNewlyDefined(defined, definedSet, tagName+"_");
			}
			return V;
		}
		catch(PostProcessException pe)
		{
			throw new CMException("Unable to post process this object type: "+pe.getMessage(),pe);
		}
	}

	protected List<Item> findContents(XMLTag piece, Map<String,Object> defined) throws CMException
	{
		try
		{
			final List<Item> V = new Vector<Item>();
			final String tagName="CONTENT";
			final List<XMLLibrary.XMLTag> choices = getAllChoices(null,null,null,tagName, piece, defined,true);
			if((choices==null)||(choices.size()==0))
				return V;
			for(int c=0;c<choices.size();c++)
			{
				final XMLTag valPiece = choices.get(c);
				if(CMSecurity.isDebugging(CMSecurity.DbgFlag.MUDPERCOLATOR))
					Log.debugOut("MUDPercolator","Found Content: "+valPiece.value());
				V.addAll(findItems(valPiece,defined));
			}
			return V;
		}
		catch(PostProcessException pe)
		{
			throw new CMException("Unable to post process this object type: "+pe.getMessage(),pe);
		}
	}

	protected String getMetacraftFilter(String recipe, XMLTag piece, Map<String,Object> defined, Triad<Integer,Integer,Class<?>[]> filter) throws CMException
	{
		int levelLimit=-1;
		int levelFloor=-1;
		Class<?>[] deriveClasses=new Class[0];
		final Map.Entry<Character, String>[] otherParms=CMStrings.splitMulti(recipe, splitters);
		recipe=otherParms[0].getValue();
		if(otherParms.length==1)
			return recipe;
		for(int i=1;i<otherParms.length;i++)
		{
			if(otherParms[i].getKey().charValue()=='<')
			{
				final String lvlStr=strFilterNow(null,null,null,otherParms[i].getValue().trim(),piece, defined);
				if(CMath.isMathExpression(lvlStr))
				{
					levelLimit=CMath.parseIntExpression(lvlStr);
					if((levelLimit==0)||(levelLimit<levelFloor))
						levelLimit=-1;
				}
			}
			else
			if(otherParms[i].getKey().charValue()=='>')
			{
				final String lvlStr=strFilterNow(null,null,null,otherParms[i].getValue().trim(),piece, defined);
				if(CMath.isMathExpression(lvlStr))
				{
					levelFloor=CMath.parseIntExpression(lvlStr);
					if((levelFloor==0)||((levelFloor>levelLimit)&&(levelLimit>0)))
						levelFloor=-1;
				}
			}
			else
			if(otherParms[i].getKey().charValue()=='=')
			{
				final String classStr=strFilterNow(null,null,null,otherParms[i].getValue().trim(),piece, defined);
				final Object O=CMClass.getItemPrototype(classStr);
				if(O!=null)
				{
					deriveClasses=Arrays.copyOf(deriveClasses, deriveClasses.length+1);
					deriveClasses[deriveClasses.length-1]=O.getClass();
				}
				else
					throw new CMException("Unknown metacraft class= "+classStr);
			}
		}
		if(levelLimit>0)
			filter.first=Integer.valueOf(levelLimit);
		if(levelFloor>0)
			filter.second=Integer.valueOf(levelFloor);
		if(deriveClasses.length>0)
			filter.third=deriveClasses;
		return recipe;
	}

	@SuppressWarnings("unchecked")
	protected List<ItemCraftor.ItemKeyPair> craftAllOfThisRecipe(ItemCraftor skill, int material, Map<String,Object> defined)
	{
		List<ItemCraftor.ItemKeyPair> skillContents=(List<ItemCraftor.ItemKeyPair>)defined.get("____COFFEEMUD_"+skill.ID()+"_"+material+"_true");
		if(skillContents==null)
		{
			if(material>=0)
				skillContents=skill.craftAllItemSets(material, true);
			else
				skillContents=skill.craftAllItemSets(true);
			if(skillContents==null)
				return null;
			defined.put("____COFFEEMUD_"+skill.ID()+"_"+material+"_true",skillContents);
		}
		final List<ItemCraftor.ItemKeyPair> skillContentsCopy=new Vector<ItemCraftor.ItemKeyPair>(skillContents.size());
		skillContentsCopy.addAll(skillContents);
		return skillContentsCopy;
	}

	protected boolean checkMetacraftItem(Item I, Triad<Integer,Integer,Class<?>[]> filter)
	{
		final int levelLimit=filter.first.intValue();
		final int levelFloor=filter.second.intValue();
		final Class<?>[] deriveClasses=filter.third;
		if(((levelLimit>0) && (I.basePhyStats().level() > levelLimit))
		||((levelFloor>0) && (I.basePhyStats().level() < levelFloor)))
			return false;
		if(deriveClasses.length==0)
			return true;
		for(final Class<?> C : deriveClasses)
		{
			if(C.isAssignableFrom(I.getClass()))
				return true;
		}
		return false;
	}

	protected List<Item> buildItem(XMLTag piece, Map<String,Object> defined) throws CMException
	{
		final Map<String,Object> preContentDefined = new SHashtable<String,Object>(defined);
		final String classID = findStringNow("class",piece,defined);
		final List<Item> contents = new Vector<Item>();
		final List<String> ignoreStats=new XVector<String>();
		final int senseFlag = CMath.s_bool(findOptionalStringNow(null,null,null,"nowear",piece,defined,false)) ? PhyStats.SENSE_ITEMNOAUTOWEAR : 0;
		if(classID.toLowerCase().startsWith("metacraft"))
		{
			final String classRest=classID.substring(9).toLowerCase().trim();
			final Triad<Integer,Integer,Class<?>[]> filter = new Triad<Integer,Integer,Class<?>[]>(Integer.valueOf(-1),Integer.valueOf(-1),new Class<?>[0]);
			String recipe="anything";
			if(classRest.startsWith(":"))
			{
				recipe=getMetacraftFilter(classRest.substring(1).trim(), piece, defined, filter);
			}
			else
			{
				recipe = findStringNow("NAME",piece,defined);
				if((recipe == null)||(recipe.length()==0))
					throw new CMException("Unable to metacraft with malformed class Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
			}

			final String materialStr = findOptionalStringNow(null,null,null,"material",piece,defined, false);
			int material=-1;
			if(materialStr!=null)
				 material = RawMaterial.CODES.FIND_IgnoreCase(materialStr);
			final List<ItemCraftor> craftors=new Vector<ItemCraftor>();
			for(final Enumeration<Ability> e=CMClass.abilities();e.hasMoreElements();)
			{
				final Ability A=e.nextElement();
				if(A instanceof ItemCraftor)
					craftors.add((ItemCraftor)A);
			}
			if(recipe.equalsIgnoreCase("anything"))
			{
				final long startTime=System.currentTimeMillis();
				while((contents.size()==0)&&((System.currentTimeMillis()-startTime)<1000))
				{
					final ItemCraftor skill=craftors.get(CMLib.dice().roll(1,craftors.size(),-1));
					if(skill.fetchRecipes().size()>0)
					{
						final List<ItemCraftor.ItemKeyPair> skillContents=craftAllOfThisRecipe(skill,material,defined);
						if(skillContents.size()==0) // preliminary error messaging, just for the craft skills themselves
							Log.errOut("MUDPercolator","Tried metacrafting anything, got "+Integer.toString(skillContents.size())+" from "+skill.ID());
						else
						for(int i=skillContents.size()-1;i>=0;i--)
						{
							final Item I=skillContents.get(i).item;
							if(!checkMetacraftItem(I, filter))
								skillContents.remove(i);
						}
						if(skillContents.size()>0)
						{
							final Item I=(Item)skillContents.get(CMLib.dice().roll(1,skillContents.size(),-1)).item.copyOf();
							contents.add(I);
						}
					}
				}
			}
			else
			if(recipe.toLowerCase().startsWith("any-"))
			{
				List<ItemCraftor.ItemKeyPair> skillContents=null;
				recipe=recipe.substring(4).trim();
				for(final ItemCraftor skill : craftors)
				{
					if(skill.ID().equalsIgnoreCase(recipe))
					{
						skillContents=craftAllOfThisRecipe(skill,material,defined);
						if((skillContents==null)||(skillContents.size()==0)) // this is just for checking the skills themselves
							Log.errOut("MUDPercolator","Tried metacrafting any-"+recipe+", got "+Integer.toString(contents.size())+" from "+skill.ID());
						else
						for(int i=skillContents.size()-1;i>=0;i--)
						{
							final Item I=skillContents.get(i).item;
							if(!checkMetacraftItem(I, filter))
								skillContents.remove(i);
						}
						break;
					}
				}
				if((skillContents!=null)&&(skillContents.size()>0))
				{
					final Item I=(Item)skillContents.get(CMLib.dice().roll(1,skillContents.size(),-1)).item.copyOf();
					contents.add(I);
				}
			}
			else
			if(recipe.toLowerCase().startsWith("all"))
			{
				List<ItemCraftor.ItemKeyPair> skillContents=null;
				recipe=recipe.substring(3).startsWith("-")?recipe.substring(4).trim():"";
				for(final ItemCraftor skill : craftors)
				{
					if(skill.ID().equalsIgnoreCase(recipe)||(recipe.length()==0))
					{
						skillContents=craftAllOfThisRecipe(skill,material,defined);
						if((skillContents==null)||(skillContents.size()==0)) // this is just for checking the skills themselves
							Log.errOut("MUDPercolator","Tried metacrafting any-"+recipe+", got "+Integer.toString(contents.size())+" from "+skill.ID());
						else
						for(int i=skillContents.size()-1;i>=0;i--)
						{
							final Item I=skillContents.get(i).item;
							if(!checkMetacraftItem(I, filter))
								skillContents.remove(i);
						}
						while((skillContents!=null)&&(skillContents.size()>0))
						{
							final Item I=(Item)skillContents.remove(0).item.copyOf();
							contents.add(I);
						}
						if(recipe.length()>0)
							break;
					}
				}
			}
			else
			{
				for(final ItemCraftor skill : craftors)
				{
					final List<List<String>> V=skill.matchingRecipeNames(recipe,false);
					if((V!=null)&&(V.size()>0))
					{
						ItemCraftor.ItemKeyPair pair;
						if(material>=0)
							pair=skill.craftItem(recipe,material,true, false);
						else
							pair=skill.craftItem(recipe,-1,true, false);
						if(pair!=null)
						{
							contents.add(pair.item);
							break;
						}
					}
				}
				for(int i=contents.size()-1;i>=0;i--)
				{
					final Item I=contents.get(i);
					if(!checkMetacraftItem(I, filter))
						contents.remove(i);
				}
				if(contents.size()==0)
				{
					for(final ItemCraftor skill : craftors)
					{
						final List<List<String>> V=skill.matchingRecipeNames(recipe,true);
						if((V!=null)&&(V.size()>0))
						{
							ItemCraftor.ItemKeyPair pair;
							if(material>=0)
								pair=skill.craftItem(recipe,material,true, false);
							else
								pair=skill.craftItem(recipe,0,true, false);
							if(pair!=null)
							{
								contents.add(pair.item);
								break;
							}
						}
					}
				}
				for(int i=contents.size()-1;i>=0;i--)
				{
					final Item I=contents.get(i);
					if(!checkMetacraftItem(I, filter))
						contents.remove(i);
				}
			}
			if(contents.size()==0)
			{
				if(filter.equals(emptyMetacraftFilter))
					throw new CMException("Unable to metacraft an item called '"+recipe+"', Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
				else
					return new ArrayList<Item>(0);
			}
			for(final Item I : contents)
			{
				addDefinition("ITEM_CLASS",I.ID(),defined);
				addDefinition("ITEM_NAME",I.Name(),defined); // define so we can mess with it
				addDefinition("ITEM_LEVEL",""+I.basePhyStats().level(),defined); // define so we can mess with it
				fillOutStatCode(I,ignoreStats,"ITEM_","NAME",piece,defined, false);
				fillOutStatCode(I,ignoreStats,"ITEM_","LEVEL",piece,defined, false);
			}
			ignoreStats.addAll(Arrays.asList(new String[]{"CLASS","MATERIAL","NAME","LEVEL"}));
		}
		else
		if(classID.equalsIgnoreCase("catalog"))
		{
			final String name = findStringNow("NAME",piece,defined);
			if((name == null)||(name.length()==0))
				throw new CMException("Unable to build a catalog item without a name, Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
			Item I = CMLib.catalog().getCatalogItem(name);
			if(I==null)
				throw new CMException("Unable to find cataloged item called '"+name+"', Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
			I=(Item)I.copyOf();
			CMLib.catalog().changeCatalogUsage(I,true);
			contents.add(I);
			addDefinition("ITEM_CLASS",I.ID(),defined);
			addDefinition("ITEM_NAME",I.Name(),defined); // define so we can mess with it
			ignoreStats.addAll(Arrays.asList(new String[]{"CLASS","NAME"}));
		}
		else
		{
			final Item I = CMClass.getItem(classID);
			if(I == null)
				throw new CMException("Unable to build item on classID '"+classID+"', Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
			contents.add(I);
			addDefinition("ITEM_CLASS",classID,defined);

			if(I.isGeneric())
			{
				final boolean filledOut = fillOutCopyCodes(I,ignoreStats,"ITEM_",piece,defined);
				final String name = fillOutStatCode(I,ignoreStats,"ITEM_","NAME",piece,defined, false);
				if((!filledOut) && ((name == null)||(name.length()==0)))
					throw new CMException("Unable to build an item without a name, Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
				if((name != null)&&(name.length()>0))
					I.setName(name);
			}
			ignoreStats.addAll(Arrays.asList(new String[]{"CLASS","NAME"}));
			addDefinition("ITEM_NAME",I.Name(),defined); // define so we can mess with it
		}

		final int contentSize=contents.size();
		for(int it=0;it<contentSize;it++) // no iterator, please!!
		{
			final Item I=contents.get(it);
			fillOutStatCodes(I,ignoreStats,"ITEM_",piece,defined);
			I.recoverPhyStats();
			CMLib.itemBuilder().balanceItemByLevel(I);
			I.recoverPhyStats();
			fillOutStatCodes(I,ignoreStats,"ITEM_",piece,defined);
			I.recoverPhyStats();

			if(I instanceof Container)
			{
				final List<Item> V= findContents(piece,new SHashtable<String,Object>(preContentDefined));
				for(int i=0;i<V.size();i++)
				{
					final Item I2=V.get(i);
					I2.setContainer((Container)I);
					contents.add(I2);
				}
			}
			{
				final List<Ability> V= findAffects(I,piece,defined,null);
				for(int i=0;i<V.size();i++)
				{
					final Ability A=V.get(i);
					A.setSavable(true);
					I.addNonUninvokableEffect(A);
				}
			}
			final List<Behavior> V = findBehaviors(I,piece,defined);
			for(int i=0;i<V.size();i++)
			{
				final Behavior B=V.get(i);
				B.setSavable(true);
				I.addBehavior(B);
			}
			I.recoverPhyStats();
			I.text();
			I.setMiscText(I.text());
			I.recoverPhyStats();
			I.phyStats().setSensesMask(I.phyStats().sensesMask()|senseFlag);
		}
		return contents;
	}

	protected List<Ability> findAffects(final Modifiable E, XMLTag piece, Map<String,Object> defined, BuildCallback callBack) throws CMException
	{
		return findAbilities(E,"AFFECT",piece,defined,callBack);
	}

	protected List<Ability> findAbilities(final Modifiable E, XMLTag piece, Map<String,Object> defined, BuildCallback callBack) throws CMException
	{
		return findAbilities(E,"ABILITY",piece,defined,callBack);
	}

	protected List<Ability> findAbilities(final Modifiable E, String tagName, XMLTag piece, Map<String,Object> defined, BuildCallback callBack) throws CMException
	{
		try
		{
			final List<Ability> V = new Vector<Ability>();
			final List<XMLLibrary.XMLTag> choices = getAllChoices(E,null,null,tagName, piece, defined,true);
			if((choices==null)||(choices.size()==0))
				return V;
			for(int c=0;c<choices.size();c++)
			{
				final XMLTag valPiece = choices.get(c);
				if(valPiece.parms().containsKey("VALIDATE") && !testCondition(E,null,null,CMLib.xml().restoreAngleBrackets(valPiece.getParmValue("VALIDATE")),valPiece, defined))
					continue;
				defineReward(E,null,null,valPiece.getParmValue("DEFINE"),valPiece,null,defined,true);
				final Set<String> definedSet=getPrevouslyDefined(defined,tagName+"_");
				final Ability A=buildAbility(E,valPiece,defined);
				if(callBack != null)
					callBack.willBuild(A, valPiece);
				V.add(A);
				clearNewlyDefined(defined, definedSet, tagName+"_");
			}
			return V;
		}
		catch(PostProcessException pe)
		{
			throw new CMException("Unable to post process this object type: "+pe.getMessage(),pe);
		}
	}

	protected List<Behavior> findBehaviors(final Modifiable E,XMLTag piece, Map<String,Object> defined) throws CMException
	{
		try
		{
			final List<Behavior> V = new Vector<Behavior>();
			final String tagName="BEHAVIOR";
			final List<XMLLibrary.XMLTag> choices = getAllChoices(E,null,null,tagName, piece, defined,true);
			if((choices==null)||(choices.size()==0))
				return V;
			for(int c=0;c<choices.size();c++)
			{
				final XMLTag valPiece = choices.get(c);
				if(valPiece.parms().containsKey("VALIDATE") && !testCondition(E,null,null,CMLib.xml().restoreAngleBrackets(valPiece.getParmValue("VALIDATE")),valPiece, defined))
					continue;
				defineReward(E,null,null,valPiece.getParmValue("DEFINE"),valPiece,null,defined,true);
				final Set<String> definedSet=getPrevouslyDefined(defined,tagName+"_");
				final Behavior B=buildBehavior(E,valPiece,defined);
				V.add(B);
				clearNewlyDefined(defined, definedSet, tagName+"_");
			}
			return V;
		}
		catch(PostProcessException pe)
		{
			throw new CMException("Unable to post process this object type: "+pe.getMessage(),pe);
		}
	}

	protected List<Race> findRaces(final Modifiable E, XMLTag piece, Map<String,Object> defined) throws CMException, PostProcessException
	{
		final List<Race> V = new Vector<Race>();
		final String tagName="RACE";
		final List<XMLLibrary.XMLTag> choices = getAllChoices(E,null,null,tagName, piece, defined,true);
		if((choices==null)||(choices.size()==0))
			return V;
		for(int c=0;c<choices.size();c++)
		{
			final XMLTag valPiece = choices.get(c);
			if(valPiece.parms().containsKey("VALIDATE") && !testCondition(E,null,null,CMLib.xml().restoreAngleBrackets(valPiece.getParmValue("VALIDATE")),valPiece, defined))
				continue;
			defineReward(E,null,null,valPiece.getParmValue("DEFINE"),valPiece,null,defined,true);
			final Set<String> definedSet=getPrevouslyDefined(defined,tagName+"_");
			final Race R=buildGenRace(E,valPiece,defined);
			V.add(R);
			clearNewlyDefined(defined, definedSet, tagName+"_");
		}
		return V;
	}

	protected Ability buildAbility(final Modifiable E, final XMLTag piece, final Map<String,Object> defined) throws CMException
	{
		final String classID = findStringNow("class",piece,defined);
		Ability A=CMClass.getAbility(classID);
		if(A == null)
			A=CMClass.findAbility(classID);
		if(A == null)
			throw new CMException("Unable to build ability on classID '"+classID+"', Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
		final Ability aA=A;
		PostProcessAttempter(defined,new PostProcessAttempt() {
			@Override
			public String attempt() throws CMException, PostProcessException 
			{
				final String value = findOptionalString(E,null,null,"PARMS",piece,this.defined, false);
				if(value != null)
					aA.setMiscText(value);
				return value;
			}
		});
		return A;
	}

	protected Behavior buildBehavior(final Modifiable E, final XMLTag piece, final Map<String,Object> defined) throws CMException
	{
		final String classID = findStringNow("class",piece,defined);
		Behavior B=CMClass.getBehavior(classID);
		if(B == null)
			B=CMClass.findBehavior(classID);
		if(B == null)
			throw new CMException("Unable to build behavior on classID '"+classID+"', Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
		final Behavior bB=B;
		PostProcessAttempter(defined,new PostProcessAttempt() {
			@Override
			public String attempt() throws CMException, PostProcessException 
			{
				final String value = findOptionalString(E,null,null,"PARMS",piece,this.defined, false);
				if(value != null)
					bB.setParms(value);
				return value;
			}
		});
		return B;
	}

	protected List<AbilityMapping> findRaceAbles(final Modifiable E, String tagName, final String prefix, final XMLTag piece, Map<String,Object> defined) throws CMException
	{
		try
		{
			final List<AbilityMapping> V = new Vector<AbilityMapping>();
			final List<XMLLibrary.XMLTag> choices = getAllChoices(E,null,prefix,tagName, piece, defined,true);
			if((choices==null)||(choices.size()==0))
				return V;
			for(int c=0;c<choices.size();c++)
			{
				final XMLTag valPiece = choices.get(c);
				if(valPiece.parms().containsKey("VALIDATE") && !testCondition(E,null,prefix,CMLib.xml().restoreAngleBrackets(valPiece.getParmValue("VALIDATE")),valPiece, defined))
					continue;
				defineReward(E,null,prefix,valPiece.getParmValue("DEFINE"),valPiece,null,defined,true);
				final Set<String> definedSet=getPrevouslyDefined(defined,tagName+"_");
				final String classID = findStringNow("CLASS",valPiece,defined);
				Ability A=CMClass.getAbility(classID);
				if(A == null)
					A=CMClass.findAbility(classID);
				if(A == null)
					throw new CMException("Unable to build ability on classID '"+classID+"', Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
				defined.put(prefix+"CLASS", classID);
				final AbilityMapping mapA=CMLib.ableMapper().newAbilityMapping().ID(classID);
				String value;
				value=findOptionalStringNow(E, null, prefix, "PARMS", valPiece, defined, false);
				if(value != null)
				{
					mapA.defaultParm(value);
					defined.put(prefix+"PARMS", value);
				}
				value=findOptionalStringNow(E, null, prefix, "PROFF", valPiece, defined, false);
				if(value != null)
				{
					mapA.defaultProficiency(CMath.parseIntExpression(value));
					defined.put(prefix+"PROFF", Integer.valueOf(mapA.defaultProficiency()));
				}
				value=findOptionalStringNow(E, null, prefix, "LEVEL", valPiece, defined, false);
				if(value != null)
				{
					mapA.qualLevel(CMath.parseIntExpression(value));
					defined.put(prefix+"LEVEL", Integer.valueOf(mapA.qualLevel()));
				}
				value=findOptionalStringNow(E, null, prefix, "QUALIFY", valPiece, defined, false);
				if(value != null)
				{
					mapA.autoGain(!CMath.s_bool(value));
					defined.put(prefix+"QUALIFY", value);
				}
				V.add(mapA);
				clearNewlyDefined(defined, definedSet, tagName+"_");
			}
			return V;
		}
		catch(PostProcessException pe)
		{
			throw new CMException("Unable to post process this object type: "+pe.getMessage(),pe);
		}
	}

	protected List<Item> getRaceItems(final Modifiable E, String tagName, final String prefix, final XMLTag piece, Map<String,Object> defined) throws CMException
	{
		try
		{
			final List<Item> V = new Vector<Item>();
			final List<XMLLibrary.XMLTag> choices = getAllChoices(E,null,prefix,tagName, piece, defined,true);
			if((choices==null)||(choices.size()==0))
				return V;
			for(int c=0;c<choices.size();c++)
			{
				final XMLTag valPiece = choices.get(c);
				if(CMSecurity.isDebugging(CMSecurity.DbgFlag.MUDPERCOLATOR))
					Log.debugOut("MUDPercolator","Found Race Item: "+valPiece.value());
				V.addAll(findItems(valPiece,defined));
			}
			return V;
		}
		catch(PostProcessException pe)
		{
			throw new CMException("Unable to post process this object type: "+pe.getMessage(),pe);
		}
	}
	
	protected Race buildGenRace(Modifiable E, XMLTag piece, Map<String,Object> defined) throws CMException
	{
		final String classID = findStringNow("class",piece,defined);
		Race R=CMClass.getRace(classID);
		if( R != null)
			return R;
		R=(Race)CMClass.getRace("GenRace").copyOf();
		int numStatsFound=0;
		for(String stat : R.getStatCodes())
		{
			try
			{
				if(findOptionalString(null, null, null, stat, piece, defined, false)!=null)
					numStatsFound++;
			}
			catch(PostProcessException pe)
			{
				numStatsFound++;
			}
		}
		if(numStatsFound<5)
			throw new CMException("Too few fields to build race on classID '"+classID+"', Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
		R.setRacialParms("<RACE><ID>"+CMStrings.capitalizeAndLower(classID)+"</ID><NAME>"+CMStrings.capitalizeAndLower(classID)+"</NAME></RACE>");
		CMClass.addRace(R);
		addDefinition("RACE_CLASS",R.ID(),defined); // define so we can mess with it
		R.setStat("NAME", findStringNow("name",piece,defined));
		addDefinition("RACE_NAME",R.name(),defined); // define so we can mess with it
		final List<String> ignoreStats=new XVector<String>(new String[]{"CLASS","NAME"});
		
		final List<Item> raceWeapons=getRaceItems(E,"WEAPON","RACE_WEAPON_",piece,defined);
		if(raceWeapons.size()>0)
		{
			Item I=raceWeapons.get(CMLib.dice().roll(1, raceWeapons.size(), -1));
			R.setStat("WEAPONCLASS", I.ID());
			R.setStat("WEAPONXML", I.text());
		}
		ignoreStats.addAll(Arrays.asList(new String[]{"WEAPONCLASS","WEAPONXML"}));
		
		final List<Item> raceResources=getRaceItems(E,"RESOURCES","RACE_RESOURCE_",piece,defined);
		R.setStat("NUMRSC", ""+raceResources.size());
		for(int i=0;i<raceResources.size();i++)
		{
			final Item I=raceResources.get(i);
			R.setStat("GETRSCID"+i, I.ID());
			R.setStat("GETRSCPARM"+i, ""+I.text());
		}
		ignoreStats.addAll(Arrays.asList(new String[]{"NUMRSC","GETRSCID","GETRSCPARM"}));
		final List<Item> raceOutfit=getRaceItems(E,"OUTFIT","RACE_RESOURCE_",piece,defined);
		R.setStat("NUMOFT", ""+raceOutfit.size());
		for(int i=0;i<raceOutfit.size();i++)
		{
			final Item I=raceOutfit.get(i);
			R.setStat("GETOFTID"+i, I.ID());
			R.setStat("GETOFTPARM"+i, ""+I.text());
		}
		ignoreStats.addAll(Arrays.asList(new String[]{"NUMOFT","GETOFTID","GETOFTPARM"}));
		final List<AbilityMapping> rables = findRaceAbles(E,"ABILITY","RACE_ABLE_",piece,defined);
		R.setStat("NUMRABLE", ""+rables.size());
		for(int i=0;i<rables.size();i++)
		{
			final AbilityMapping ableMap=rables.get(i);
			R.setStat("GETRABLE"+i, ableMap.abilityID());
			R.setStat("GETRABLEPROF"+i, ""+ableMap.defaultProficiency());
			R.setStat("GETRABLEQUAL"+i, ""+(!ableMap.autoGain()));
			R.setStat("GETRABLELVL"+i, ""+ableMap.qualLevel());
			R.setStat("GETRABLEPARM"+i, ableMap.defaultParm());
		}
		ignoreStats.addAll(Arrays.asList(new String[]{"NUMRABLE","GETRABLE","GETRABLEPROF","GETRABLEQUAL","GETRABLELVL","GETRABLEPARM"}));
		final List<AbilityMapping> cables = findRaceAbles(E,"CULTUREABILITY","RACE_CULT_ABLE_",piece,defined);
		R.setStat("NUMCABLE", ""+cables.size());
		for(int i=0;i<cables.size();i++)
		{
			final AbilityMapping ableMap=cables.get(i);
			R.setStat("GETCABLE"+i, ableMap.abilityID());
			R.setStat("GETCABLEPROF"+i, ""+ableMap.defaultProficiency());
		}
		ignoreStats.addAll(Arrays.asList(new String[]{"NUMCABLE","GETCABLE","GETCABLEPROF"}));
		final List<AbilityMapping> reffs = findRaceAbles(E,"AFFECT","RACE_EFFECT_",piece,defined);
		R.setStat("NUMREFF", ""+reffs.size());
		for(int i=0;i<reffs.size();i++)
		{
			final AbilityMapping ableMap=reffs.get(i);
			R.setStat("GETREFF"+i, ableMap.abilityID());
			R.setStat("GETREFFPARM"+i, ""+ableMap.defaultParm());
			R.setStat("GETREFFLVL"+i, ""+ableMap.qualLevel());
		}
		ignoreStats.addAll(Arrays.asList(new String[]{"NUMREFF","GETREFF","GETREFFPARM","GETREFFLVL"}));
		
		final List<AbilityMapping> iables = findRaceAbles(E,"IMMUNITY","RACE_IMMUNITY_",piece,defined);
		R.setStat("NUMIABLE", ""+reffs.size());
		for(int i=0;i<iables.size();i++)
		{
			final AbilityMapping ableMap=reffs.get(i);
			R.setStat("GETIABLE"+i, ableMap.abilityID());
		}
		ignoreStats.addAll(Arrays.asList(new String[]{"NUMIABLE","GETIABLE"}));
		
		fillOutStatCodes(R,ignoreStats,"RACE_",piece,defined);
		CMLib.database().DBCreateRace(R.ID(),R.racialParms());
		return R;
	}

	protected void addDefinition(String definition, String value, Map<String,Object> defined)
	{
		definition=definition.toUpperCase().trim();
		defined.put(definition, value);
		if(definition.toUpperCase().endsWith("S"))
			definition+="ES";
		else
			definition+="S";
		final String def = (String)defined.get(definition);
		if(def==null)
			defined.put(definition, value);
		else defined.put(definition, def+","+value);
	}

	protected String findOptionalStringNow(Modifiable E, List<String> ignoreStats, String defPrefix, String tagName, XMLTag piece, Map<String,Object> defined, boolean debug)
	{
		try
		{
			return findOptionalString(E,ignoreStats,defPrefix,tagName, piece, defined, false);
		}
		catch(final PostProcessException x)
		{
			if(debug)
				Log.errOut(x);
			return null;
		}
	}

	protected String findOptionalString(Modifiable E, List<String> ignoreStats, String defPrefix, String tagName, XMLTag piece, Map<String,Object> defined, boolean debug) throws PostProcessException
	{
		try
		{
			return findString(E,ignoreStats,defPrefix,tagName, piece, defined);
		}
		catch(final CMException x)
		{
			if(debug)
				Log.errOut(x);
			return null;
		}
	}

	@Override
	public void defineReward(XMLTag piece, Map<String,Object> defined) throws CMException
	{
		defineReward(null, null, null,piece,piece.value(),defined);
	}

	protected void defineReward(Modifiable E, List<String> ignoreStats, String defPrefix, XMLTag piece, String value, Map<String,Object> defined) throws CMException
	{
		try
		{
			defineReward(E,ignoreStats,defPrefix,piece.getParmValue("DEFINE"),piece,value,defined,true);
		}
		catch(PostProcessException pe)
		{
			throw new CMException("Post-processing not permitted: "+pe.getMessage(),pe);
		}
	}

	@Override
	public void preDefineReward(XMLTag piece, Map<String,Object> defined) throws CMException
	{
		preDefineReward(null,null,null,piece,defined);
	}
	
	protected void preDefineReward(Modifiable E, List<String> ignoreStats, String defPrefix, XMLTag piece, Map<String,Object> defined) throws CMException
	{
		try
		{
			defineReward(E,ignoreStats,defPrefix,piece.getParmValue("PREDEFINE"),piece,piece.value(),defined,false);
		}
		catch(PostProcessException pe)
		{
			throw new CMException("Post-processing not permitted: "+pe.getMessage(),pe);
		}
	}

	protected void defineReward(Modifiable E, List<String> ignoreStats, String defPrefix, String defineString, XMLTag piece, String value, Map<String,Object> defined, boolean recurseAllowed) throws CMException, PostProcessException
	{
		if((defineString!=null)&&(defineString.trim().length()>0))
		{
			final List<String> V=CMParms.parseCommas(defineString,true);
			for (String defVar : V)
			{
				String definition=value;
				final int x=defVar.indexOf('=');
				if(x==0)
					continue;
				if(x>0)
				{
					definition=defVar.substring(x+1).trim();
					defVar=defVar.substring(0,x).toUpperCase().trim();
					switch(defVar.charAt(defVar.length()-1))
					{
						case '+': case '-': case '*': case '/':
						{
							final char plusMinus=defVar.charAt(defVar.length()-1);
							defVar=defVar.substring(0,defVar.length()-1).trim();
							String oldVal=(String)defined.get(defVar.toUpperCase().trim());
							if((oldVal==null)||(oldVal.trim().length()==0))
								oldVal="0";
							definition=oldVal+plusMinus+definition;
							break;
						}
					}
				}
				if(definition==null)
					definition="!";
				definition=strFilter(E,ignoreStats,defPrefix,definition,piece, defined);
				if(CMath.isMathExpression(definition))
					definition=Integer.toString(CMath.s_parseIntExpression(definition));
				if(defVar.trim().length()>0)
					defined.put(defVar.toUpperCase().trim(), definition);
				if(CMSecurity.isDebugging(CMSecurity.DbgFlag.MUDPERCOLATOR))
					Log.debugOut("MudPercolator","DEFINE:"+defVar.toUpperCase().trim()+"="+definition);
			}
		}
		final XMLTag parentPiece = piece.parent();
		if((parentPiece!=null)&&(parentPiece.tag().equalsIgnoreCase(piece.tag()))&&(recurseAllowed))
			defineReward(E,ignoreStats,defPrefix,parentPiece.getParmValue("DEFINE"),parentPiece,value,defined,recurseAllowed);
	}

	protected String findStringNow(Modifiable E, List<String> ignoreStats, String defPrefix, String tagName, XMLTag piece, Map<String,Object> defined) throws CMException
	{
		try
		{
			return findString(E,ignoreStats,defPrefix,tagName,piece,defined);
		}
		catch(PostProcessException pe)
		{
			throw new CMException("Post processing not permitted",pe);
		}
	}
	
	protected String findString(Modifiable E, List<String> ignoreStats, String defPrefix, String tagName, XMLTag piece, Map<String,Object> defined) throws CMException,PostProcessException
	{
		tagName=tagName.toUpperCase().trim();
		
		if(tagName.startsWith("SYSTEM_RANDOM_NAME:"))
		{
			String[] split=tagName.substring(19).split("-");
			if((split.length==2)&&(CMath.isInteger(split[0]))&&(CMath.isInteger(split[1])))
				return CMLib.login().generateRandomName(CMath.s_int(split[0]), CMath.s_int(split[1]));
			throw new CMException("Bad random name range in '"+tagName+"' on piece '"+piece.tag()+"', Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
		}
		else
		if(tagName.equals("ROOM_AREAGATE"))
		{
			if(E instanceof Environmental)
			{
				final Room R=CMLib.map().roomLocation((Environmental)E);
				if(R!=null)
				{
					boolean foundOne=false;
					for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
					{
						final Room R2=R.getRoomInDir(d);
						foundOne=(R2!=null) || foundOne;
						if((R2!=null) && (R2.roomID().length()>0) && (R.getArea()!=R2.getArea()))
							return CMLib.directions().getDirectionName(d);
					}
					if(!foundOne)
						throw new PostProcessException("No exits at all on on object "+R.roomID()+" in variable '"+tagName+"'");
				}
			}
			return "";
		}
		if(defPrefix != null)
		{
			final Object asPreviouslyDefined = defined.get((defPrefix+tagName).toUpperCase());
			if(asPreviouslyDefined instanceof String)
				return strFilter(E,ignoreStats,defPrefix,(String)asPreviouslyDefined,piece, defined);
		}
		
		final String asParm = piece.getParmValue(tagName);
		if(asParm != null)
			return strFilter(E,ignoreStats,defPrefix,asParm,piece, defined);

		final Object asDefined = defined.get(tagName);
		if(asDefined instanceof String)
			return (String)asDefined;
		XMLTag processDefined=null;
		if(asDefined instanceof XMLTag)
		{
			piece=(XMLTag)asDefined;
			processDefined=piece;
			tagName=piece.tag();
		}
		List<XMLLibrary.XMLTag> choices = getAllChoices(E,ignoreStats,defPrefix,tagName, piece, defined,true);
		if((choices==null)||(choices.size()==0))
			throw new CMException("Unable to find tag '"+tagName+"' on piece '"+piece.tag()+"', Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
		StringBuffer finalValue = new StringBuffer("");

		for(int c=0;c<choices.size();c++)
		{
			final XMLTag valPiece = choices.get(c);
			if(valPiece.parms().containsKey("VALIDATE") && !testCondition(E,null,null,CMLib.xml().restoreAngleBrackets(valPiece.getParmValue("VALIDATE")),valPiece, defined))
				continue;

			final String value=strFilter(E,ignoreStats,defPrefix,valPiece.value(),valPiece, defined);
			if(processDefined!=valPiece)
				defineReward(E,ignoreStats,defPrefix,valPiece.getParmValue("DEFINE"),valPiece,value,defined,true);

			String action = valPiece.getParmValue("ACTION");
			if(action==null) 
				finalValue.append(" ").append(value);
			else
			{
				action=action.toUpperCase().trim();
				if((action.length()==0)||(action.equals("APPEND")))
					finalValue.append(" ").append(value);
				else
				if(action.equals("REPLACE"))
					finalValue = new StringBuffer(value);
				else
				if(action.equals("PREPEND"))
					finalValue.insert(0,' ').insert(0,value);
				else
					throw new CMException("Unknown action '"+action+" on subPiece "+valPiece.tag()+" on piece '"+piece.tag()+"', Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
			}
		}
		final String finalFinalValue=finalValue.toString().trim();
		if(processDefined!=null)
			defineReward(E,ignoreStats,defPrefix,piece.getParmValue("DEFINE"),processDefined,finalFinalValue,defined,true);
		return finalFinalValue;
	}

	protected XMLTag processLikeParm(String tagName, XMLTag piece, Map<String,Object> defined) throws CMException
	{
		final String like = piece.getParmValue("LIKE");
		if(like!=null)
		{
			final List<String> V=CMParms.parseCommas(like,true);
			final XMLTag origPiece = piece;
			piece=piece.copyOf();
			for(int v=0;v<V.size();v++)
			{
				String s = V.get(v);
				if(s.startsWith("$"))
					s=s.substring(1).trim();
				final XMLTag likePiece =(XMLTag)defined.get(s.toUpperCase().trim());
				if((likePiece == null)||(!likePiece.tag().equalsIgnoreCase(tagName)))
					throw new CMException("Invalid like: '"+s+"' on piece '"+piece.tag()+"', Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
				piece.contents().addAll(likePiece.contents());
				piece.parms().putAll(likePiece.parms());
				piece.parms().putAll(origPiece.parms());
			}
		}
		return piece;
	}
	
	@Override
	public List<XMLTag> getAllChoices(String tagName, XMLTag piece, Map<String,Object> defined) throws CMException
	{
		try
		{
			return getAllChoices(null,null,null,tagName,piece,defined,true);
		}
		catch(PostProcessException pe)
		{
			throw new CMException("Unable to post process this object: "+pe.getMessage(),pe);
		}
	}
	
	protected List<XMLTag> getAllChoices(Modifiable E, List<String> ignoreStats, String defPrefix, String tagName, XMLTag piece, Map<String,Object> defined, boolean skipTest) throws CMException, PostProcessException
	{
		if((!skipTest)&&(!testCondition(E,ignoreStats,defPrefix,CMLib.xml().restoreAngleBrackets(piece.getParmValue("CONDITION")),piece,defined)))
			return new Vector<XMLTag>(1);

		defineReward(E,ignoreStats,defPrefix,piece.getParmValue("PREDEFINE"),piece,piece.value(),defined,false); // does pre-define

		final List<XMLTag> choices = new Vector<XMLTag>();
		final String inserter = piece.getParmValue("INSERT");
		if(inserter != null)
		{
			final List<String> V=CMParms.parseCommas(inserter,true);
			for(int v=0;v<V.size();v++)
			{
				String s = V.get(v);
				if(s.startsWith("$"))
					s=s.substring(1).trim();
				final XMLTag insertPiece =(XMLTag)defined.get(s.toUpperCase().trim());
				if(insertPiece == null)
					throw new CMException("Undefined insert: '"+s+"' on piece '"+piece.tag()+"', Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
				if(insertPiece.tag().equalsIgnoreCase(tagName))
					choices.addAll(getAllChoices(E,ignoreStats,defPrefix,tagName,insertPiece,defined,false));
			}
		}
		else
		if(piece.tag().equalsIgnoreCase(tagName))
		{
			if((piece.parms().containsKey("LAYOUT")) && (piece.tag().equalsIgnoreCase("ROOM")) && (!defined.containsKey("ROOM_LAYOUT")))
				return new XVector<XMLTag>(processLikeParm(tagName,piece,defined));

			boolean container=false;
			for(int p=0;p<piece.contents().size();p++)
			{
				final XMLTag ppiece=piece.contents().get(p);
				if(ppiece.tag().equalsIgnoreCase(tagName))
				{
					container=true;
					break;
				}
			}
			if(!container)
				return new XVector<XMLTag>(processLikeParm(tagName,piece,defined));
		}

		for(int p=0;p<piece.contents().size();p++)
		{
			final XMLTag subPiece = piece.contents().get(p);
			if(subPiece.tag().equalsIgnoreCase(tagName))
				choices.addAll(getAllChoices(E,ignoreStats,defPrefix,tagName,subPiece,defined,false));
		}
		return selectChoices(E,tagName,ignoreStats,defPrefix,choices,piece,defined);
	}

	protected boolean testCondition(Modifiable E, List<String> ignoreStats, String defPrefix, String condition, XMLTag piece, Map<String,Object> defined) throws PostProcessException
	{
		final Map<String,Object> fixed=new HashMap<String,Object>();
		try
		{
			if(condition == null) 
				return true;
			fixed.putAll(defined);
			final List<Varidentifier> ids=parseVariables(condition);
			for(final Varidentifier id : ids)
			{
				try
				{
					if((defPrefix!=null)&&(defined!=null)&&(id.var.toUpperCase().startsWith(defPrefix))&&(!defined.containsKey(defPrefix)))
					{
						XMLTag newPiece=piece;
						while((newPiece.parent()!=null)&&(newPiece.tag().equals(piece.tag())))
							newPiece=newPiece.parent();
						fillOutStatCode(E, ignoreStats, defPrefix, id.var.substring(defPrefix.length()), newPiece, defined, false);
					}
					String value=findString(E,ignoreStats,defPrefix,id.var, piece, defined);
					if(CMath.isMathExpression(value))
					{
						final double val=CMath.parseMathExpression(value);
						if(Math.round(val)==val)
							value=""+Math.round(val);
						else
							value=""+val;
					}
					fixed.put(id.var.toUpperCase(),value);
				}
				catch(final CMException e)
				{
				}
			}
			final boolean test= CMStrings.parseStringExpression(condition.toUpperCase(),fixed, true);
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.MUDPERCOLATOR))
				Log.debugOut("MudPercolator","TEST "+piece.tag()+": "+condition+"="+test);
			return test;
		}
		catch(final Exception e)
		{
			if(e instanceof PostProcessException)
				throw (PostProcessException)e;
			Log.errOut("Generate",e.getMessage()+": "+condition);
			try {
				CMStrings.parseStringExpression(condition,fixed, true);
			}
			catch(final Exception e1)
			{
			}
			return false;
		}
	}
	
	protected String getRequirementsDescription(String values)
	{
		if(values==null)
			return "";
		if(values.equalsIgnoreCase("integer")||values.equalsIgnoreCase("int"))
			return " as an integer or integer expression";
		else
		if(values.equalsIgnoreCase("double")||values.equalsIgnoreCase("#")||values.equalsIgnoreCase("number"))
			return " as a number or numeric expression";
		else
		if(values.equalsIgnoreCase("string")||values.equalsIgnoreCase("$"))
			return " as an open string";
		else
		if(values.trim().length()>0)
			return " as one of the following values: "+values;
		return "";
	}

	protected boolean checkRequirementsValue(String validValue, String value)
	{
		if(validValue==null)
			return value != null;
		if(validValue.equalsIgnoreCase("integer")||validValue.equalsIgnoreCase("int"))
			return CMath.isMathExpression(value);
		else
		if(validValue.equalsIgnoreCase("double")||validValue.equalsIgnoreCase("#")||validValue.equalsIgnoreCase("number"))
			return CMath.isMathExpression(value);
		else
		if(validValue.equalsIgnoreCase("string")||validValue.equalsIgnoreCase("$"))
			return value.length()>0;
		else
		if(validValue.trim().length()>0)
			return CMParms.containsIgnoreCase(CMParms.toStringArray(CMParms.parseSemicolons(validValue,true)),value);
		return value.length()==0;
	}

	protected String cleanRequirementsValue(String values, String value)
	{
		if(values==null)
			return value;
		if(values.equalsIgnoreCase("integer")||values.equalsIgnoreCase("int"))
			return Integer.toString(CMath.s_parseIntExpression(value));
		else
		if(values.equalsIgnoreCase("double")||values.equalsIgnoreCase("#")||values.equalsIgnoreCase("number"))
			return Double.toString(CMath.s_parseMathExpression(value));
		else
		if(values.equalsIgnoreCase("string")||values.equalsIgnoreCase("$"))
			return value;
		else
		if(values.trim().length()>0)
		{
			final String[] arrayStr=CMParms.toStringArray(CMParms.parseSemicolons(values,true));
			int x=CMParms.indexOfIgnoreCase(arrayStr,value);
			if(x<0)
				x=0;
			return arrayStr[x];
		}
		return value;
	}

	@Override
	public Map<String,String> getUnfilledRequirements(Map<String,Object> defined, XMLTag piece)
	{
		String requirements = piece.getParmValue("REQUIRES");
		final Map<String,String> set=new Hashtable<String,String>();
		if(requirements==null)
			return set;
		requirements = requirements.trim();
		final List<String> reqs = CMParms.parseCommas(requirements,true);
		for(int r=0;r<reqs.size();r++)
		{
			String reqVariable=reqs.get(r);
			if(reqVariable.startsWith("$"))
				reqVariable=reqVariable.substring(1).trim();
			String validValues=null;
			final int x=reqVariable.indexOf('=');
			if(x>=0)
			{
				validValues=reqVariable.substring(x+1).trim();
				reqVariable=reqVariable.substring(0,x).trim();
			}
			if((!defined.containsKey(reqVariable.toUpperCase()))
			||(!checkRequirementsValue(validValues, defined.get(reqVariable.toUpperCase()).toString())))
			{
				if(validValues==null)
					set.put(reqVariable.toUpperCase(), "any");
				else
				if(validValues.equalsIgnoreCase("integer")||validValues.equalsIgnoreCase("int"))
					set.put(reqVariable.toUpperCase(), "int");
				else
				if(validValues.equalsIgnoreCase("double")||validValues.equalsIgnoreCase("#")||validValues.equalsIgnoreCase("number"))
					set.put(reqVariable.toUpperCase(), "double");
				else
				if(validValues.equalsIgnoreCase("string")||validValues.equalsIgnoreCase("$"))
					set.put(reqVariable.toUpperCase(), "string");
				else
				if(validValues.trim().length()>0)
					set.put(reqVariable.toUpperCase(), CMParms.toListString(CMParms.parseSemicolons(validValues,true)));
			}
		}
		return set;
	}

	protected void checkRequirements(Map<String,Object> defined, String requirements) throws CMException
	{
		if(requirements==null)
			return;
		requirements = requirements.trim();
		final List<String> reqs = CMParms.parseCommas(requirements,true);
		for(int r=0;r<reqs.size();r++)
		{
			String reqVariable=reqs.get(r);
			if(reqVariable.startsWith("$"))
				reqVariable=reqVariable.substring(1).trim();
			String validValues=null;
			final int x=reqVariable.indexOf('=');
			if(x>=0)
			{
				validValues=reqVariable.substring(x+1).trim();
				reqVariable=reqVariable.substring(0,x).trim();
			}
			if(!defined.containsKey(reqVariable.toUpperCase()))
				throw new CMException("Required variable not defined: '"+reqVariable+"'.  Please define this variable"+getRequirementsDescription(validValues)+".");
			if(!checkRequirementsValue(validValues, defined.get(reqVariable.toUpperCase()).toString()))
				throw new CMException("The required variable '"+reqVariable+"' is not properly defined.  Please define this variable"+getRequirementsDescription(validValues)+".");
		}
	}

	@Override
	public void checkRequirements(XMLTag piece, Map<String,Object> defined) throws CMException
	{
		checkRequirements(defined,piece.getParmValue("REQUIRES"));
	}

	protected List<XMLTag> selectChoices(Modifiable E, String tagName, List<String> ignoreStats, String defPrefix, List<XMLTag> choices, XMLTag piece, Map<String,Object> defined) throws CMException
	{
		String selection = piece.getParmValue("SELECT");
		if(selection == null)
			return choices;
		selection=selection.toUpperCase().trim();
		List<XMLLibrary.XMLTag> selectedChoicesV=null;
		if(selection.equals("NONE"))
			selectedChoicesV= new Vector<XMLTag>();
		else
		if(selection.equals("ALL"))
			selectedChoicesV=choices;
		else
		if((choices.size()==0)
		&&(!selection.startsWith("ANY-0"))
		&&(!selection.startsWith("FIRST-0"))
		&&(!selection.startsWith("LAST-0"))
		&&(!selection.startsWith("PICK-0"))
		&&(!selection.startsWith("LIMIT-")))
		{
			throw new CMException("Can't make selection among NONE: on piece '"+piece.tag()+"', Tag: "+tagName+", Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
		}
		else
		if(selection.equals("FIRST"))
			selectedChoicesV= new XVector<XMLTag>(choices.get(0));
		else
		if(selection.startsWith("FIRST-"))
		{
			final int num=CMath.parseIntExpression(strFilterNow(E,ignoreStats,defPrefix,selection.substring(selection.indexOf('-')+1),piece, defined));
			if((num<0)||(num>choices.size()))
				throw new CMException("Can't pick first "+num+" of "+choices.size()+" on piece '"+piece.tag()+"', Tag: "+tagName+", Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
			selectedChoicesV=new Vector<XMLTag>();
			for(int v=0;v<num;v++)
				selectedChoicesV.add(choices.get(v));
		}
		else
		if(selection.startsWith("LIMIT-"))
		{
			final int num=CMath.parseIntExpression(strFilterNow(E,ignoreStats,defPrefix,selection.substring(selection.indexOf('-')+1),piece, defined));
			if(num<0)
				throw new CMException("Can't pick limit "+num+" of "+choices.size()+" on piece '"+piece.tag()+"', Tag: "+tagName+", Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
			selectedChoicesV=new Vector<XMLTag>();
			if(choices.size()<=num)
				selectedChoicesV.addAll(choices);
			else
			while(selectedChoicesV.size()<num)
				selectedChoicesV.add(choices.remove(CMLib.dice().roll(1, choices.size(), -1)));
		}
		else
		if(selection.equals("LAST"))
			selectedChoicesV=new XVector<XMLTag>(choices.get(choices.size()-1));
		else
		if(selection.startsWith("LAST-"))
		{
			final int num=CMath.parseIntExpression(strFilterNow(E,ignoreStats,defPrefix,selection.substring(selection.indexOf('-')+1),piece, defined));
			if((num<0)||(num>choices.size()))
				throw new CMException("Can't pick last "+num+" of "+choices.size()+" on piece '"+piece.tag()+"', Tag: "+tagName+", Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
			selectedChoicesV=new Vector<XMLTag>();
			for(int v=choices.size()-num;v<choices.size();v++)
				selectedChoicesV.add(choices.get(v));
		}
		else
		if(selection.startsWith("PICK-"))
		{
			final int num=CMath.parseIntExpression(strFilterNow(E,ignoreStats,defPrefix,selection.substring(selection.indexOf('-')+1),piece, defined));
			if((num<0)||(num>choices.size()))
				throw new CMException("Can't pick "+num+" of "+choices.size()+" on piece '"+piece.tag()+"', Tag: "+tagName+", Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
			selectedChoicesV=new Vector<XMLTag>();
			final List<XMLLibrary.XMLTag> cV=new XVector<XMLLibrary.XMLTag>(choices);
			for(int v=0;v<num;v++)
			{
				final int[] weights=new int[cV.size()];
				int total=0;
				for(int c=0;c<cV.size();c++)
				{
					final XMLTag lilP=cV.get(c);
					int weight=CMath.s_parseIntExpression(lilP.getParmValue("PICKWEIGHT"));
					if(weight<1)
						weight=1;
					weights[c]=weight;
					total+=weight;
				}

				int choice=CMLib.dice().roll(1,total,0);
				int c=-1;
				while(choice>0)
				{
					c++;
					choice-=weights[c];
				}
				selectedChoicesV.add(cV.get(c));
				cV.remove(c);
			}
		}
		else
		if(selection.equals("ANY"))
			selectedChoicesV=new XVector<XMLTag>(choices.get(CMLib.dice().roll(1,choices.size(),-1)));
		else
		if(selection.startsWith("ANY-"))
		{
			final int num=CMath.parseIntExpression(strFilterNow(E,ignoreStats,defPrefix,selection.substring(selection.indexOf('-')+1),piece, defined));
			if((num<0)||(num>choices.size()))
				throw new CMException("Can't pick last "+num+" of "+choices.size()+" on piece '"+piece.tag()+"', Tag: "+tagName+", Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
			selectedChoicesV=new Vector<XMLTag>();
			final List<XMLLibrary.XMLTag> cV=new XVector<XMLLibrary.XMLTag>(choices);
			for(int v=0;v<num;v++)
			{
				final int x=CMLib.dice().roll(1,cV.size(),-1);
				selectedChoicesV.add(cV.get(x));
				cV.remove(x);
			}
		}
		else
		if(selection.startsWith("REPEAT-"))
		{
			final int num=CMath.parseIntExpression(strFilterNow(E,ignoreStats,defPrefix,selection.substring(selection.indexOf('-')+1),piece, defined));
			if(num<0)
				throw new CMException("Can't pick last "+num+" of "+choices.size()+" on piece '"+piece.tag()+"', Tag: "+tagName+", Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
			selectedChoicesV=new Vector<XMLTag>();
			final List<XMLLibrary.XMLTag> cV=new XVector<XMLLibrary.XMLTag>(choices);
			for(int v=0;v<num;v++)
			{
				final int x=CMLib.dice().roll(1,cV.size(),-1);
				selectedChoicesV.add(cV.get(x));
			}
		}
		else
		if((selection.trim().length()>0)&&CMath.isMathExpression(selection))
		{
			final int num=CMath.parseIntExpression(strFilterNow(E,ignoreStats,defPrefix,selection.substring(selection.indexOf('-')+1),piece, defined));
			if((num<0)||(num>choices.size()))
				throw new CMException("Can't pick any "+num+" of "+choices.size()+" on piece '"+piece.tag()+"', Tag: "+tagName+", Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
			selectedChoicesV=new Vector<XMLTag>();
			final List<XMLLibrary.XMLTag> cV=new XVector<XMLLibrary.XMLTag>(choices);
			for(int v=0;v<num;v++)
			{
				final int x=CMLib.dice().roll(1,cV.size(),-1);
				selectedChoicesV.add(cV.get(x));
				cV.remove(x);
			}
		}
		else
			throw new CMException("Illegal select type '"+selection+"' on piece '"+piece.tag()+"', Tag: "+tagName+", Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
		return selectedChoicesV;
	}

	protected String strFilterNow(Modifiable E, List<String> ignoreStats, String defPrefix, String str, XMLTag piece, Map<String,Object> defined) throws CMException
	{
		try
		{
			return strFilter(E,ignoreStats,defPrefix,str,piece,defined);
		}
		catch(PostProcessException pe)
		{
			throw new CMException("Post processing not permitted",pe);
		}
		
	}

	protected String strFilter(Modifiable E, List<String> ignoreStats, String defPrefix, String str, XMLTag piece, Map<String,Object> defined) throws CMException,PostProcessException
	{
		List<Varidentifier> vars=parseVariables(str);
		final boolean killArticles=str.toLowerCase().startsWith("(a(n))");
		while(vars.size()>0)
		{
			final Varidentifier V=vars.remove(vars.size()-1);
			Object val;
			if(V.isMathExpression)
			{
				final String expression=strFilter(E,ignoreStats,defPrefix,V.var,piece, defined);
				if(CMath.isMathExpression(expression))
				{
					if(expression.indexOf('.')>0)
						val=""+CMath.parseMathExpression(expression);
					else
						val=""+CMath.parseLongExpression(expression);
				}
				else
					throw new CMException("Invalid math expression '$"+expression+"' in str '"+str+"'");
			}
			else
			if(V.var.toUpperCase().startsWith("STAT:") && (E!=null))
			{
				final String[] parts=V.var.toUpperCase().split(":");
				if(E instanceof Environmental)
				{
					Environmental E2=(Environmental)E;
					for(int p=1;p<parts.length-1;p++)
					{
						final Room R=CMLib.map().roomLocation(E2);
						Environmental E3;
						if(parts[p].equals("ROOM"))
							E3=CMLib.map().roomLocation(E2);
						else
						if(parts[p].equals("AREA"))
							E3=CMLib.map().areaLocation(E2);
						else
						if(CMLib.directions().getDirectionCode(parts[p])>=0)
						{
							if(R==null)
								throw new PostProcessException("Unknown room on object "+E2.ID()+" in variable '"+V.var+"'");
							final int dir=CMLib.directions().getDirectionCode(parts[p]);
							E3=R.getRoomInDir(dir);
						}
						else
						if(parts[p].equals("ANYROOM"))
						{
							if(R==null)
								throw new PostProcessException("Unknown room on object "+E2.ID()+" in variable '"+V.var+"'");
							final List<Room> dirs=new ArrayList<Room>();
							for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
							{
								if((R.getRoomInDir(d)!=null)&&(R.getRoomInDir(d).roomID().length()>0))
									dirs.add(R.getRoomInDir(d));
							}
							if(dirs.size()==0)
								throw new PostProcessException("No anyrooms on object "+E2.ID()+" in variable '"+V.var+"'");
							E3=dirs.get(CMLib.dice().roll(1, dirs.size(), -1));
						}
						else
						if(parts[p].equals("MOB"))
						{
							if(R==null)
								throw new PostProcessException("Unknown room on object "+E2.ID()+" in variable '"+V.var+"'");
							if((R.numInhabitants()==0)||((E2 instanceof MOB)&&(R.numInhabitants()==1)))
								throw new PostProcessException("No mobs in room for "+E2.ID()+" in variable '"+V.var+"'");
							E3=R.fetchInhabitant(CMLib.dice().roll(1, R.numInhabitants(), -1));
						}
						else
						if(parts[p].equals("ITEM"))
						{
							if(R==null)
								throw new PostProcessException("Unknown room on object "+E2.ID()+" in variable '"+V.var+"'");
							if((R.numItems()==0)||((E2 instanceof Item)&&(R.numItems()==1)))
								throw new PostProcessException("No items in room for "+E2.ID()+" in variable '"+V.var+"'");
							E3=R.getItem(CMLib.dice().roll(1, R.numItems(), -1));
						}
						else
						if(parts[p].equals("AREAGATE"))
						{
							if(R==null)
								throw new PostProcessException("Unknown room on object "+E2.ID()+" in variable '"+V.var+"'");
							final List<Room> dirs=new ArrayList<Room>();
							for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
							{
								final Room R2=R.getRoomInDir(d);
								if((R2!=null) && (R2.roomID().length()>0) && (R.getArea()!=R2.getArea()))
									dirs.add(R.getRoomInDir(d));
							}
							if(dirs.size()==0)
							{
								if(defined.get("ROOMTAG_GATEEXITROOM") instanceof Room)
									dirs.add((Room)defined.get("ROOMTAG_GATEEXITROOM"));
								if(dirs.size()==0)
									throw new PostProcessException("No areagates on object "+E2.ID()+" in variable '"+V.var+"'");
							}
							E3=dirs.get(CMLib.dice().roll(1, dirs.size(), -1));
						}
						else
							throw new PostProcessException("Unknown stat code '"+parts[p]+"' on object "+E2.ID()+" in variable '"+V.var+"'");
						if(E3==null)
							throw new PostProcessException("Unknown '"+parts[p]+"' on object "+E2.ID()+" in variable '"+V.var+"'");
						else
							E2=E3;
					}
					E=E2;
				}
				if (E.isStat(parts[parts.length-1]))
					val=E.getStat(parts[parts.length-1]);
				else
					throw new CMException("Unknown stat code '"+parts[parts.length-1]+"' on object "+E+" in variable '"+V.var+"'");
			}
			else
				val = defined.get(V.var.toUpperCase().trim());
			if(val instanceof XMLTag)
			{
				val = findString(E,ignoreStats,defPrefix,"STRING",(XMLTag)val,defined);
			}
			if((val == null)&&(defPrefix!=null)&&(defPrefix.length()>0)&&(E!=null))
			{
				String preValue=V.var;
				if(preValue.toUpperCase().startsWith(defPrefix.toUpperCase()))
				{
					preValue=preValue.toUpperCase().substring(defPrefix.length());
					if((E.isStat(preValue))
					&&((ignoreStats==null)||(!ignoreStats.contains(preValue.toUpperCase()))))
					{
						val=fillOutStatCode(E,ignoreStats,defPrefix,preValue,piece,defined, false);
						XMLTag statPiece=piece;
						while((val == null)
						&&(statPiece.parent()!=null)
						&&(!(defPrefix.startsWith(statPiece.tag())&&(!defPrefix.startsWith(statPiece.parent().tag())))))
						{
							statPiece=statPiece.parent();
							val=fillOutStatCode(E,ignoreStats,defPrefix,preValue,statPiece,defined, false);
						}
						if((ignoreStats!=null)&&(val!=null))
							ignoreStats.add(preValue.toUpperCase());
					}
				}
			}
			if(val == null)
				throw new CMException("Unknown variable '$"+V.var+"' in str '"+str+"'",new CMException("$"+V.var));
			if(V.toUpperCase)
				val=val.toString().toUpperCase();
			if(V.toLowerCase)
				val=val.toString().toLowerCase();
			if(V.toPlural)
				val=CMLib.english().makePlural(val.toString());
			if(V.toCapitalized)
				val=CMStrings.capitalizeAndLower(val.toString());
			if(killArticles)
				val=CMLib.english().cleanArticles(val.toString());
			str=str.substring(0,V.outerStart)+val.toString()+str.substring(V.outerEnd);
			if(vars.size()==0)
				vars=parseVariables(str);
		}
		int x=str.toLowerCase().indexOf("(a(n))");
		while((x>=0)&&(x<str.length()-8))
		{
			if((Character.isWhitespace(str.charAt(x+6)))
			&&(Character.isLetter(str.charAt(x+7))))
			{
				if(CMStrings.isVowel(str.charAt(x+7)))
					str=str.substring(0,x)+"an"+str.substring(x+6);
				else
					str=str.substring(0,x)+"a"+str.substring(x+6);
			}
			else
				str=str.substring(0,x)+"a"+str.substring(x+6);
			x=str.toLowerCase().indexOf("(a(n))");
		}
		return CMLib.xml().restoreAngleBrackets(str).replace('\'','`');
	}

	@Override
	public String findString(String tagName, XMLTag piece, Map<String, Object> defined) throws CMException
	{
		return findStringNow(null,null,null,tagName,piece,defined);
	}
	
	protected String findStringNow(String tagName, XMLTag piece, Map<String, Object> defined) throws CMException
	{
		return findStringNow(null,null,null,tagName,piece,defined);
	}
	
	protected int makeNewLevel(int level, int oldMin, int oldMax, int newMin, int newMax)
	{
		final double oldRange = oldMax-oldMin;
		final double myOldRange = level - oldMin;
		final double pctOfOldRange = myOldRange/oldRange;
		final double newRange = newMax-newMin;
		return newMin + (int)Math.round(pctOfOldRange * newRange);
	}
	
	@Override
	public boolean relevelRoom(Room room, int oldMin, int oldMax, int newMin, int newMax)
	{
		boolean changeMade=false;
		try
		{
			room.toggleMobility(false);
			CMLib.threads().suspendResumeRecurse(room, false, true);
			if(CMLib.law().getLandTitle(room)!=null)
				return false;
			for(Enumeration<Item> i=room.items();i.hasMoreElements();)
			{
				final Item I=i.nextElement();
				if(I==null)
					continue;
				if((I instanceof Weapon)||(I instanceof Armor))
				{
					int newILevel=makeNewLevel(I.phyStats().level(),oldMin,oldMax,newMin,newMax);
					if(newILevel <= 0)
						newILevel = 1;
					if(newILevel != I.phyStats().level())
					{
						final int levelDiff = newILevel - I.phyStats().level();
						changeMade=true;
						final int effectiveLevel =CMLib.itemBuilder().timsLevelCalculator(I) + levelDiff; 
						I.basePhyStats().setLevel(effectiveLevel);
						I.phyStats().setLevel(effectiveLevel);
						CMLib.itemBuilder().itemFix(I, effectiveLevel, null);
						I.basePhyStats().setLevel(newILevel);
						I.phyStats().setLevel(newILevel);
						I.text();
					}
				}
			}
			for(Enumeration<MOB> m=room.inhabitants();m.hasMoreElements();)
			{
				final MOB M=m.nextElement();
				if((M!=null)
				&&(M.isMonster())
				&&(M.getStartRoom()==room))
				{
					int newLevel=makeNewLevel(M.phyStats().level(),oldMin,oldMax,newMin,newMax);
					if(newLevel <= 0)
						newLevel = 1;
					if(newLevel != M.phyStats().level())
					{
						changeMade=true;
						final double spdDif = M.basePhyStats().speed() - CMLib.leveler().getLevelMOBSpeed(M);
						final int armDif = M.basePhyStats().armor()-CMLib.leveler().getLevelMOBArmor(M);
						final int dmgDif = M.basePhyStats().damage()-CMLib.leveler().getLevelMOBDamage(M);
						final int attDif = M.basePhyStats().attackAdjustment()-CMLib.leveler().getLevelAttack(M);
						M.basePhyStats().setLevel(newLevel);
						M.phyStats().setLevel(newLevel);
						CMLib.leveler().fillOutMOB(M,M.basePhyStats().level());
						M.basePhyStats().setSpeed(M.basePhyStats().speed()+spdDif);
						M.basePhyStats().setArmor(M.basePhyStats().armor()+armDif);
						M.basePhyStats().setDamage(M.basePhyStats().damage()+dmgDif);
						M.basePhyStats().setAttackAdjustment(M.basePhyStats().attackAdjustment()+attDif);
					}
					for(Enumeration<Item> mi=M.items();mi.hasMoreElements();)
					{
						Item mI=mi.nextElement();
						if(mI!=null)
						{
							int newILevel=makeNewLevel(mI.phyStats().level(),oldMin,oldMax,newMin,newMax);
							if(newILevel <= 0)
								newILevel = 1;
							if(newILevel != mI.phyStats().level())
							{
								final int levelDiff = newILevel - mI.phyStats().level();
								changeMade=true;
								final int effectiveLevel =CMLib.itemBuilder().timsLevelCalculator(mI) + levelDiff; 
								mI.basePhyStats().setLevel(effectiveLevel);
								mI.phyStats().setLevel(effectiveLevel);
								CMLib.itemBuilder().itemFix(mI, effectiveLevel, null);
								mI.basePhyStats().setLevel(newILevel);
								mI.phyStats().setLevel(newILevel);
								mI.text();
							}
						}
					}
					final ShopKeeper SK=CMLib.coffeeShops().getShopKeeper(M);
					if(SK!=null)
					{
						for(final Iterator<CoffeeShop.ShelfProduct> i=SK.getShop().getStoreShelves();i.hasNext();)
						{
							final CoffeeShop.ShelfProduct P=i.next();
							final Environmental E2=P.product;
							if((E2 instanceof Item)||(E2 instanceof MOB))
							{
								final Physical P2=(Physical)E2;
								newLevel=makeNewLevel(P2.phyStats().level(),oldMin,oldMax,newMin,newMax);
								if(newLevel <= 0)
									newLevel = 1;
								if(newLevel != P2.phyStats().level())
								{
									changeMade=true;
									if(E2 instanceof Item)
									{
										Item I2=(Item)E2;
										final int levelDiff = newLevel - I2.phyStats().level();
										final int effectiveLevel =CMLib.itemBuilder().timsLevelCalculator(I2) + levelDiff; 
										I2.basePhyStats().setLevel(effectiveLevel);
										I2.phyStats().setLevel(effectiveLevel);
										CMLib.itemBuilder().itemFix(I2, effectiveLevel, null);
									}
									P2.basePhyStats().setLevel(newLevel);
									P2.phyStats().setLevel(newLevel);
									if(E2 instanceof MOB)
										CMLib.leveler().fillOutMOB((MOB)E2,P2.basePhyStats().level());
									E2.text();
								}
							}
						}
					}
					M.text();
					M.recoverCharStats();
					M.recoverMaxState();
					M.recoverPhyStats();
					M.recoverCharStats();
					M.recoverMaxState();
					M.recoverPhyStats();
					M.resetToMaxState();
				}
			}
		}
		catch(Exception e)
		{
			Log.errOut(e);
			changeMade=false;
		}
		finally
		{
			room.recoverRoomStats();
			CMLib.threads().suspendResumeRecurse(room, false, false);
			room.toggleMobility(true);
			room.recoverRoomStats();
		}
		return changeMade;
	}

}
