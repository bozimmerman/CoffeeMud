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
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary.XMLpiece;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;


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
public class MUDPercolator extends StdLibrary implements AreaGenerationLibrary
{
	public String ID(){return "MUDPercolator";}

	protected final static char[] splitters=new char[]{'<','>','='}; 
	protected final static Triad<Integer,Integer,Class<?>[]> emptyMetacraftFilter = new Triad<Integer,Integer,Class<?>[]>(Integer.valueOf(-1),Integer.valueOf(-1),new Class<?>[0]);
	
	private SHashtable<String,Class<LayoutManager>> mgrs = new SHashtable<String,Class<LayoutManager>>();
	
	public LayoutManager getLayoutManager(String named) 
	{
		Class<LayoutManager> mgr = mgrs.get(named.toUpperCase().trim());
		if(mgr != null){
			try {
				return mgr.newInstance();
			}catch(Exception e) {
				return null;
			}
		}
		return null;
	}
	
	public void buildDefinedIDSet(List<XMLpiece> xmlRoot, Map<String,Object> defined)
	{
		if(xmlRoot==null) return;
		for(int v=0;v<xmlRoot.size();v++)
		{
			XMLLibrary.XMLpiece piece = xmlRoot.get(v);
			String id = CMLib.xml().getParmValue(piece.parms,"ID");
			if((id!=null)&&(id.length()>0))
				defined.put(id.toUpperCase().trim(),piece);
			String load = CMLib.xml().getParmValue(piece.parms,"LOAD");
			if((load!=null)&&(load.length()>0))
			{
				piece.parms.remove("LOAD");
				CMFile file = new CMFile(load,null,CMFile.FLAG_LOGERRORS|CMFile.FLAG_FORCEALLOW);
				if(file.exists() && file.canRead())
				{
					List<XMLpiece> addPieces=CMLib.xml().parseAllXML(file.text());
					piece.contents.addAll(addPieces);
				}
			}
			buildDefinedIDSet(piece.contents,defined);
		}
	}
	
	@SuppressWarnings("unchecked")
    public boolean activate()
	{ 
		String filePath="com/planet_ink/coffee_mud/Libraries/layouts";
		CMProps page = CMProps.instance();
		Vector<Object> layouts=CMClass.loadClassList(filePath,page.getStr("LIBRARY"),"/layouts",LayoutManager.class,true);
		for(int f=0;f<layouts.size();f++) {
			LayoutManager lmgr= (LayoutManager)layouts.elementAt(f);
			Class<LayoutManager> lmgrClass=(Class<LayoutManager>)lmgr.getClass();
			mgrs.put(lmgr.name().toUpperCase().trim(),lmgrClass);
		}
		return true;
	}
	
	public boolean shutdown(){ 
		mgrs.clear();
		return true;
	}
	
	// vars created: ROOM_CLASS, ROOM_TITLE, ROOM_DESCRIPTION, ROOM_CLASSES, ROOM_TITLES, ROOM_DESCRIPTIONS
	public Room buildRoom(XMLLibrary.XMLpiece piece, Map<String,Object> defined, Exit[] exits, int direction) throws CMException
	{
		addDefinition("DIRECTION",Directions.getDirectionName(direction).toLowerCase(),defined);
		
		String classID = findString("class",piece,defined);
		Room R = CMClass.getLocale(classID);
		if(R == null) throw new CMException("Unable to build room on classID '"+classID+"', Data: "+CMParms.toStringList(piece.parms)+":"+piece.value);
		addDefinition("ROOM_CLASS",classID,defined);
		final List<String> ignoreStats=new XVector<String>(new String[]{"CLASS","DISPLAY","DESCRIPTION"});
		String title = findString(R,ignoreStats,"ROOM_","title",piece,defined);
		R.setDisplayText(title);
		addDefinition("ROOM_TITLE",title,defined);
		String description = findString(R,ignoreStats,"ROOM_","description",piece,defined);
		R.setDescription(description);
		addDefinition("ROOM_DESCRIPTION",description,defined);
		fillOutStatCodes(R, ignoreStats, "ROOM_", piece, defined);
		List<MOB> mV = findMobs(piece,defined);
		for(int i=0;i<mV.size();i++) {
			MOB M=mV.get(i);
			M.setSavable(true);
			M.bringToLife(R,true);
		}
		List<Item> iV = findItems(piece,defined);
		for(int i=0;i<iV.size();i++) {
			Item I=iV.get(i);
			R.addItem(I);
			I.setSavable(true);
			I.setExpirationDate(0);
		}
		List<Ability> aV = findAffects(piece,defined);
		for(int i=0;i<aV.size();i++) {
			Ability A=aV.get(i);
			A.setSavable(true);
			R.addNonUninvokableEffect(A);
		}
		List<Behavior> bV = findBehaviors(piece,defined);
		for(int i=0;i<bV.size();i++) {
			Behavior B=bV.get(i);
			B.setSavable(true);
			R.addBehavior(B);
		}
		for(int dir=0;dir<Directions.NUM_DIRECTIONS();dir++) {
			Exit E=exits[dir];
			if((E==null)&&(defined.containsKey("ROOMLINK_"+Directions.getDirectionChar(dir).toUpperCase())))
			{
				defined.put("ROOMLINK_DIR",Directions.getDirectionChar(dir).toUpperCase());
				Exit E2=findExit(piece, defined);
				if(E2!=null) E=E2;
				defined.remove("ROOMLINK_DIR");
				if(CMSecurity.isDebugging(CMSecurity.DbgFlag.MUDPERCOLATOR)) 
					Log.debugOut("MUDPercolator","EXIT:NEW:"+((E==null)?"null":E.ID())+":DIR="+Directions.getDirectionChar(dir).toUpperCase()+":ROOM="+title);
			}
			else
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.MUDPERCOLATOR)&&defined.containsKey("ROOMLINK_"+Directions.getDirectionChar(dir).toUpperCase()))
				Log.debugOut("MUDPercolator","EXIT:OLD:"+((E==null)?"null":E.ID())+":DIR="+Directions.getDirectionChar(dir).toUpperCase()+":ROOM="+title);
			R.setRawExit(dir, E);
			R.startItemRejuv();
		}
		return R;
	}

	protected void layoutRecursiveFill(LayoutNode n, HashSet<LayoutNode> nodesDone, Vector<LayoutNode> group, LayoutTypes type)
	{
		if(n != null)
		for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
			if(n.links().containsKey(Integer.valueOf(d))) {
				LayoutNode offN=n.links().get(Integer.valueOf(d));
				if((offN.type()==type)
				&&(!group.contains(offN))
				&&(!nodesDone.contains(offN))) {
					group.addElement(offN);
					nodesDone.add(offN);
					layoutRecursiveFill(offN,nodesDone,group,type);
				}
			}
	}
	
	protected void layoutFollow(LayoutNode n, LayoutTypes type, int direction, HashSet<LayoutNode> nodesDone, List<LayoutNode> group)
	{
		n=n.links().get(Integer.valueOf(direction));
		while((n != null) &&(n.type()==LayoutTypes.street) &&(!group.contains(n) &&(!nodesDone.contains(n))))
		{
			nodesDone.add(n);
			group.add(n);
			n=n.links().get(Integer.valueOf(direction));
		}
	}
	
	
	
	public Area findArea(XMLLibrary.XMLpiece piece, Map<String,Object> defined, int directions) throws CMException
	{
		String tagName="AREA";
		List<XMLLibrary.XMLpiece> choices = getAllChoices(null,null,null,tagName, piece, defined,true);
		if((choices==null)||(choices.size()==0)) 
		{
			return null;
		}
		while(choices.size()>0)
		{
			XMLLibrary.XMLpiece valPiece = choices.get(CMLib.dice().roll(1,choices.size(),-1));
			choices.remove(valPiece);
			if(valPiece.parms.containsKey("VALIDATE") && !testCondition(CMLib.xml().restoreAngleBrackets(CMLib.xml().getParmValue(valPiece.parms,"VALIDATE")),valPiece, defined))
				continue;
			Map<String,Object> rDefined=new Hashtable<String,Object>();
			rDefined.putAll(defined);
			defineReward(null,null,null,valPiece,null,rDefined);
			Area A=buildArea(valPiece,rDefined,directions);
			for(Iterator<String> e=rDefined.keySet().iterator();e.hasNext();)
			{
				String key=e.next();
				if(key.startsWith("_"))
					defined.put(key,rDefined.get(key));
			}
			return A;
		}
		return null;
	}
	
	public Area buildArea(XMLLibrary.XMLpiece piece, Map<String,Object> defined, int direction) throws CMException
	{
		defined.put("DIRECTION",Directions.getDirectionName(direction).toLowerCase());

		String classID = findString("class",piece,defined);
		Area A = CMClass.getAreaType(classID);
		if(A == null) 
			throw new CMException("Unable to build area on classID '"+classID+"', Data: "+CMParms.toStringList(piece.parms)+":"+piece.value);
		defined.put("AREA_CLASS",classID);
		String name = findString(A,null,"AREA_","NAME",piece,defined);
		if(CMLib.map().getArea(name)!=null)
		{
			A.destroy();
			throw new CMException("Unable to create area '"+A.Name()+"', you must destroy the old one first.");
		}
		
		A.setName(name);
		
		defined.put("AREA_NAME",name);
		String author = findOptionalString(A,null,"AREA_","author",piece,defined);
		if(author != null)
			A.setAuthorID(author);
		String description = findOptionalString(A,null,"AREA_","description",piece,defined);
		if(description != null)
		{
			A.setDescription(description);
			defined.put("AREA_DESCRIPTION",description);
		}
		if(fillInArea(piece, defined, A, direction))
			return A;
		throw new CMException("Unable to build area for some reason.");
	}
	
	// vars created: LINK_DIRECTION, AREA_CLASS, AREA_NAME, AREA_DESCRIPTION, AREA_LAYOUT, AREA_SIZE
	@SuppressWarnings("unchecked")
    public boolean fillInArea(XMLLibrary.XMLpiece piece, Map<String,Object> defined, Area A, int direction) throws CMException
	{
		String layoutType = findString("layout",piece,defined);
		if((layoutType==null)||(layoutType.trim().length()==0))
			throw new CMException("Unable to build area without defined layout");
		LayoutManager layoutManager = getLayoutManager(layoutType);
		if(layoutManager == null)
			throw new CMException("Undefined Layout "+layoutType);
		defined.put("AREA_LAYOUT",layoutManager.name());
		String size = findString("size",piece,defined);
		if((!CMath.isInteger(size))||(CMath.s_int(size)<=0))
			throw new CMException("Unable to build area of size "+size);
		defined.put("AREA_SIZE",size);
		final List<String> ignoreStats=new XVector<String>(new String[]{"CLASS","NAME","DESCRIPTION","LAYOUT","SIZE"});
		fillOutStatCodes(A, ignoreStats,"AREA_",piece,defined);
		Vector<LayoutNode> roomsLayout = layoutManager.generate(CMath.s_int(size),direction);
		if((roomsLayout==null)||(roomsLayout.size()==0))
			throw new CMException("Unable to fill area of size "+size+" off layout "+layoutManager.name());
		
		List<Ability> aV = findAffects(piece,defined);
		for(int i=0;i<aV.size();i++)
		{
			Ability AB=aV.get(i);
			A.setSavable(true);
			A.addNonUninvokableEffect(AB);
		}
		List<Behavior> bV = findBehaviors(piece,defined);
		for(int i=0;i<bV.size();i++)
		{
			Behavior B=bV.get(i);
			B.setSavable(true);
			A.addBehavior(B);
		}
		
		CMLib.map().addArea(A); // necessary for proper naming.
		
		// now break our rooms into logical groups, generate those rooms.
		List<List<LayoutNode>> roomGroups = new Vector<List<LayoutNode>>();
		LayoutNode magicRoom = roomsLayout.firstElement();
		HashSet<LayoutNode> nodesDone=new HashSet<LayoutNode>();
		boolean keepLooking=true;
		while(keepLooking)
		{
			keepLooking=false;
			for(int i=0;i<roomsLayout.size();i++)
			{
				LayoutNode node=roomsLayout.elementAt(i);
				if(node.type()==LayoutTypes.leaf) 
				{
					Vector<LayoutNode> group=new Vector<LayoutNode>();
					group.add(node);
					nodesDone.add(node);
					for(Integer linkDir : node.links().keySet())
					{
						LayoutNode dirNode=node.links().get(linkDir);
						if(!nodesDone.contains(dirNode))
						{
							if((dirNode.type()==LayoutTypes.leaf)
							||(dirNode.type()==LayoutTypes.interior)&&(node.isFlagged(LayoutFlags.offleaf)))
							{
								group.addElement(dirNode);
								nodesDone.add(node);
							}
						}
					}
					for(LayoutNode n : group)
						roomsLayout.remove(n);
					if(group.size()>0)
						roomGroups.add(group);
					keepLooking=true;
					break;
				}
			}
		}
		keepLooking=true;
		while(keepLooking)
		{
			keepLooking=false;
			for(int i=0;i<roomsLayout.size();i++)
			{
				LayoutNode node=roomsLayout.elementAt(i);
				if(node.type()==LayoutTypes.street) 
				{
					List<LayoutNode> group=new Vector<LayoutNode>();
					group.add(node);
					nodesDone.add(node);
					LayoutRuns run=node.getFlagRuns();
					if(run==LayoutRuns.ns) {
						layoutFollow(node,LayoutTypes.street,Directions.NORTH,nodesDone,group);
						layoutFollow(node,LayoutTypes.street,Directions.SOUTH,nodesDone,group);
					} else
					if(run==LayoutRuns.ew) {
						layoutFollow(node,LayoutTypes.street,Directions.EAST,nodesDone,group);
						layoutFollow(node,LayoutTypes.street,Directions.WEST,nodesDone,group);
					} else
					if(run==LayoutRuns.nesw) {
						layoutFollow(node,LayoutTypes.street,Directions.NORTHEAST,nodesDone,group);
						layoutFollow(node,LayoutTypes.street,Directions.SOUTHWEST,nodesDone,group);
					} else
					if(run==LayoutRuns.nwse) {
						layoutFollow(node,LayoutTypes.street,Directions.NORTHWEST,nodesDone,group);
						layoutFollow(node,LayoutTypes.street,Directions.SOUTHEAST,nodesDone,group);
					}
					else 
					{
						int topDir=-1;
						int topSize=-1;
						for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
							if(node.links().get(Integer.valueOf(d))!=null)
							{
								List<LayoutNode> grpCopy=new XVector<LayoutNode>(group);
								HashSet<LayoutNode> nodesDoneCopy=(HashSet<LayoutNode>)nodesDone.clone();
								layoutFollow(node,LayoutTypes.street,d,nodesDoneCopy,grpCopy);
								if(node.links().get(Integer.valueOf(Directions.getOpDirectionCode(d)))!=null)
									layoutFollow(node,LayoutTypes.street,Directions.getOpDirectionCode(d),nodesDoneCopy,grpCopy);
								if(grpCopy.size()>topSize)
								{
									topSize=grpCopy.size();
									topDir=d;
								}
							}
						if(topDir>=0)
						{
							layoutFollow(node,LayoutTypes.street,topDir,nodesDone,group);
							if(node.links().get(Integer.valueOf(Directions.getOpDirectionCode(topDir)))!=null)
								layoutFollow(node,LayoutTypes.street,Directions.getOpDirectionCode(topDir),nodesDone,group);
						}
					}
					for(LayoutNode n : group)
						roomsLayout.remove(n);
					if(group.size()>0)
						roomGroups.add(group);
					keepLooking=true;
				} 
			}
		}
		
		while(roomsLayout.size() >0)
		{
			LayoutNode node=roomsLayout.firstElement();
			Vector<LayoutNode> group=new Vector<LayoutNode>();
			group.add(node);
			nodesDone.add(node);
			layoutRecursiveFill(node,nodesDone,group,node.type());
			for(LayoutNode n : group)
				roomsLayout.remove(n);
			roomGroups.add(group);
		}
		// make CERTAIN that the magic first room in the layout always
		// gets ID#0.
		List<LayoutNode> magicGroup=null;
		for(int g=0;g<roomGroups.size();g++)
		{
			List<LayoutNode> group=roomGroups.get(g);
			if(group.contains(magicRoom)) {
				magicGroup=group;
				break;
			}
		}
		if(CMSecurity.isDebugging(CMSecurity.DbgFlag.MUDPERCOLATOR))
		for(int g=0;g<roomGroups.size();g++)
		{
			Vector<LayoutNode> group=(Vector<LayoutNode>)roomGroups.get(g);
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.MUDPERCOLATOR)) 
				Log.debugOut("MudPercolator","GROUP:"+A.Name()+": "+group.firstElement().type().toString()+": "+group.size());
		}
		Map<List<LayoutNode>,Map<String,Object>> groupDefinitions=new Hashtable<List<LayoutNode>,Map<String,Object>>();
		for(List<LayoutNode> group : roomGroups)
		{
			Map<String,Object> newDefined=new Hashtable<String,Object>();
			newDefined.putAll(defined);
			groupDefinitions.put(group, newDefined);
		}
		Map<String,Object> groupDefined = groupDefinitions.get(magicGroup);
		processRoom(A,direction,piece,magicRoom,groupDefined);
		
		try {
			//now generate the rooms and add them to the area
			for(List<LayoutNode> group : roomGroups)
			{
				groupDefined = groupDefinitions.get(group);
				
				for(LayoutNode node : group)
					if(node!=magicRoom)
						processRoom(A,direction,piece,node,groupDefined);
				for(Iterator<String> e=groupDefined.keySet().iterator();e.hasNext();)
				{
					String key=e.next();
					if(key.startsWith("__"))
					{
						if(CMSecurity.isDebugging(CMSecurity.DbgFlag.MUDPERCOLATOR)) 
							Log.debugOut("AREADEF:"+key+"="+CMStrings.limit(groupDefined.get(key).toString(), 10));
						defined.put(key, groupDefined.get(key));
						for(List<LayoutNode> group2 : roomGroups)
						{
							Map<String,Object> groupDefined2 = groupDefinitions.get(group2);
							if(groupDefined2!=groupDefined)
								groupDefined2.put(key, groupDefined.get(key));
						}
					}
				}
			}

			//now do a final-link on the rooms
			for(List<LayoutNode> group : roomGroups)
			{
				for(LayoutNode node : group)
				{
					Room R=node.room();
					for(Integer linkDir : node.links().keySet()) {
						LayoutNode linkNode=node.getLink(linkDir.intValue());
						R.rawDoors()[linkDir.intValue()]=linkNode.room();
					}
				}
			}
			CMLib.map().delArea(A); // we added it for id assignment, now we are done.
		}
		catch(Exception t)
		{
			CMLib.map().delArea(A);
			CMLib.map().emptyArea(A);
			A.destroy();
			if(t instanceof CMException)
				throw (CMException)t;
			throw new CMException(t.getMessage(),t);
		}
		return true;
	}

	public void processRoom(Area A, int direction, XMLLibrary.XMLpiece piece, LayoutNode node, Map<String,Object> groupDefined)
		throws CMException
	{
		for(LayoutTags key : node.tags().keySet())
			groupDefined.put("ROOMTAG_"+key.toString().toUpperCase(),node.tags().get(key));
		Exit[] exits=new Exit[Directions.NUM_DIRECTIONS()];
		for(Integer linkDir : node.links().keySet()) {
			LayoutNode linkNode = node.links().get(linkDir);
			if(linkNode.room() != null) {
				int opDir=Directions.getOpDirectionCode(linkDir.intValue());
				exits[linkDir.intValue()]=linkNode.room().getExitInDir(opDir);
				groupDefined.put("ROOMTITLE_"+Directions.getDirectionChar(linkDir.intValue()).toUpperCase(),linkNode.room().displayText(null));
			}
			groupDefined.put("ROOMLINK_"+Directions.getDirectionChar(linkDir.intValue()).toUpperCase(),"true");
		}
		if(CMSecurity.isDebugging(CMSecurity.DbgFlag.MUDPERCOLATOR))
		{
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.MUDPERCOLATOR)) 
				Log.debugOut("MUDPercolator",A.Name()+": type: "+node.type().toString());
			StringBuffer defs=new StringBuffer("");
			for(Iterator<String> e=groupDefined.keySet().iterator();e.hasNext();)
			{
				String key=e.next();
				defs.append(key+"="+CMStrings.limit(groupDefined.get(key).toString(),10)+",");
			}
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.MUDPERCOLATOR)) 
				Log.debugOut("MUDPercolator","DEFS: "+defs.toString());
		}
		Room R=findRoom(piece, groupDefined, exits, direction);
		if(R==null)
			throw new CMException("Failure to generate room from "+piece.value);
		R.setRoomID(A.getNewRoomID(null,-1));
		if(CMSecurity.isDebugging(CMSecurity.DbgFlag.MUDPERCOLATOR)) 
			Log.debugOut("MUDPercolator","ROOMID: "+R.roomID());
		R.setArea(A);
		A.addProperRoom(R);
		node.setRoom(R);
		for(LayoutTags key : node.tags().keySet())
			groupDefined.remove("ROOMTAG_"+key.toString().toUpperCase());
		for(Integer linkDir : node.links().keySet())
		{
			groupDefined.remove("ROOMLINK_"+Directions.getDirectionChar(linkDir.intValue()).toUpperCase());
			groupDefined.remove("ROOMTITLE_"+Directions.getDirectionChar(linkDir.intValue()).toUpperCase());
		}
	}
	
	public List<MOB> findMobs(XMLLibrary.XMLpiece piece, Map<String,Object> defined) throws CMException
	{
		List<MOB> V = new Vector<MOB>();
		String tagName="MOB";
		List<XMLLibrary.XMLpiece> choices = getAllChoices(null,null,null,tagName, piece, defined,true);
		if((choices==null)||(choices.size()==0)) return V;
		for(int c=0;c<choices.size();c++) 
		{
			XMLLibrary.XMLpiece valPiece = choices.get(c);
			if(valPiece.parms.containsKey("VALIDATE") && !testCondition(CMLib.xml().restoreAngleBrackets(CMLib.xml().getParmValue(valPiece.parms,"VALIDATE")),valPiece, defined))
				continue;
			defineReward(null,null,null,valPiece,null,defined);
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.MUDPERCOLATOR)) 
				Log.debugOut("MUDPercolator","Build Mob: "+CMStrings.limit(valPiece.value,80)+"...");
			Set<String> definedSet=getPrevouslyDefined(defined,tagName+"_");
			MOB M=buildMob(valPiece,defined);
			V.add(M);
			clearNewlyDefined(defined, definedSet, tagName+"_");
		}
		return V;
	}
	
	public Room findRoom(XMLLibrary.XMLpiece piece, Map<String,Object> defined, Exit[] exits, int directions) throws CMException
	{
		String tagName="ROOM";
		List<XMLLibrary.XMLpiece> choices = getAllChoices(null,null,null,tagName, piece, defined,true);
		if((choices==null)||(choices.size()==0)) 
		{
			return null;
		}
		while(choices.size()>0)
		{
			XMLLibrary.XMLpiece valPiece = choices.get(CMLib.dice().roll(1,choices.size(),-1));
			choices.remove(valPiece);
			if(valPiece.parms.containsKey("VALIDATE") && !testCondition(CMLib.xml().restoreAngleBrackets(CMLib.xml().getParmValue(valPiece.parms,"VALIDATE")),valPiece, defined))
				continue;
			Map<String,Object> rDefined=new Hashtable<String,Object>();
			rDefined.putAll(defined);
			Exit[] rExits=exits.clone();
			defineReward(null,null,null,valPiece,null,rDefined);
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.MUDPERCOLATOR)) 
				Log.debugOut("MUDPercolator","Build Room: "+CMStrings.limit(valPiece.value,80)+"...");
			Room R=buildRoom(valPiece,rDefined,rExits,directions);
			for(Iterator<String> e=rDefined.keySet().iterator();e.hasNext();)
			{
				String key=e.next();
				if(key.startsWith("_"))
				{
					if(CMSecurity.isDebugging(CMSecurity.DbgFlag.MUDPERCOLATOR)) 
						Log.debugOut("RGDEF:"+key+"="+CMStrings.limit(rDefined.get(key).toString(), 10));
					defined.put(key,rDefined.get(key));
				}
			}
			for(int e=0;e<rExits.length;e++)
				exits[e]=rExits[e];
			return R;
		}
		return null;
	}
	
	public DVector findRooms(XMLLibrary.XMLpiece piece, Map<String,Object> defined, Exit[] exits, int direction) throws CMException
	{
		DVector DV = new DVector(2);
		String tagName="ROOM";
		List<XMLLibrary.XMLpiece> choices = getAllChoices(null,null,null,tagName, piece, defined,true);
		if((choices==null)||(choices.size()==0)) return DV;
		for(int c=0;c<choices.size();c++) {
			XMLLibrary.XMLpiece valPiece = choices.get(c);
			if(valPiece.parms.containsKey("VALIDATE") && !testCondition(CMLib.xml().restoreAngleBrackets(CMLib.xml().getParmValue(valPiece.parms,"VALIDATE")),valPiece, defined))
				continue;
			defineReward(null,null,null,valPiece,null,defined);
			Exit[] theseExits=exits.clone();
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.MUDPERCOLATOR)) 
				Log.debugOut("MUDPercolator","Build Room: "+CMStrings.limit(valPiece.value,80)+"...");
			Room R=buildRoom(valPiece,defined,theseExits,direction);
			DV.addElement(R,theseExits);
		}
		return DV;
	}
	
	public Exit findExit(XMLLibrary.XMLpiece piece, Map<String,Object> defined) throws CMException
	{
		String tagName="EXIT";
		List<XMLLibrary.XMLpiece> choices = getAllChoices(null,null,null,tagName, piece, defined,true);
		if((choices==null)||(choices.size()==0)) return null;
		List<Exit> exitChoices = new Vector<Exit>();
		for(int c=0;c<choices.size();c++)
		{
			XMLLibrary.XMLpiece valPiece = choices.get(c);
			if(valPiece.parms.containsKey("VALIDATE") && !testCondition(CMLib.xml().restoreAngleBrackets(CMLib.xml().getParmValue(valPiece.parms,"VALIDATE")),valPiece, defined))
				continue;
			defineReward(null,null,null,valPiece,null,defined);
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.MUDPERCOLATOR)) 
				Log.debugOut("MUDPercolator","Build Exit: "+CMStrings.limit(valPiece.value,80)+"...");
			Exit E=buildExit(valPiece,defined);
			if(E!=null)
				exitChoices.add(E);
		}
		if(exitChoices.size()==0) return null;
		return exitChoices.get(CMLib.dice().roll(1,exitChoices.size(),-1));
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
		List<Varidentifier> list=new XVector<Varidentifier>();
		while((x>=0)&&(x<str.length()-1))
		{
			Varidentifier var = new Varidentifier();
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
						var.toPlural=true;
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
				int varstart=var.outerStart;
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
	
	protected String fillOutStatCode(Modifiable E, List<String> ignoreStats, String defPrefix, String stat, XMLLibrary.XMLpiece piece, Map<String,Object> defined)
	{
		String value = null;
		if(!ignoreStats.contains(stat.toUpperCase().trim())) 
		{
			value = findOptionalString(E,ignoreStats,defPrefix,stat,piece,defined);
			if(value != null) 
			{
				E.setStat(stat, value);
				if((defPrefix!=null)&&(defPrefix.length()>0))
					addDefinition(defPrefix+stat,value,defined);
			}
		}
		return value;
	}
	
	protected void fillOutStatCodes(Modifiable E, List<String> ignoreStats, String defPrefix, XMLLibrary.XMLpiece piece, Map<String,Object> defined)
	{
		String[] statCodes = E.getStatCodes();
		for(int s=0;s<statCodes.length;s++) 
		{
			String stat=statCodes[s];
			fillOutStatCode(E,ignoreStats,defPrefix,stat,piece,defined);
		}
	}
	
	protected MOB buildMob(XMLLibrary.XMLpiece piece, Map<String,Object> defined) throws CMException
	{
		String classID = findString("class",piece,defined);
		MOB M = null;
		final List<String> ignoreStats=new XVector<String>();
		if(classID.equalsIgnoreCase("catalog"))
		{
			String name = findString("NAME",piece,defined);
			if((name == null)||(name.length()==0)) 
				throw new CMException("Unable to build a catalog mob without a name, Data: "+CMParms.toStringList(piece.parms)+":"+piece.value);
			M = CMLib.catalog().getCatalogMob(name);
			if(M==null)
				throw new CMException("Unable to find cataloged mob called '"+name+"', Data: "+CMParms.toStringList(piece.parms)+":"+piece.value);
			M=(MOB)M.copyOf();
			CMLib.catalog().changeCatalogUsage(M,true);
			addDefinition("MOB_CLASS",M.ID(),defined);
		}
		else
		{
			M = CMClass.getMOB(classID);
			if(M == null) throw new CMException("Unable to build mob on classID '"+classID+"', Data: "+CMParms.toStringList(piece.parms)+":"+piece.value);
			addDefinition("MOB_CLASS",classID,defined);
			
			if(M.isGeneric())
			{
				String name = fillOutStatCode(M,ignoreStats,"MOB_","NAME",piece,defined);
				if((name == null)||(name.length()==0)) 
					throw new CMException("Unable to build a mob without a name, Data: "+CMParms.toStringList(piece.parms)+":"+piece.value);
				M.setName(name);
			}
		}
		addDefinition("MOB_NAME",M.Name(),defined);
		M.baseCharStats().setMyRace(CMClass.getRace("StdRace"));
		
		String value = fillOutStatCode(M,ignoreStats,"MOB_","LEVEL",piece,defined);
		if(value != null) {
			CMLib.leveler().fillOutMOB(M,M.basePhyStats().level());
		}
		value = fillOutStatCode(M,ignoreStats,"MOB_","GENDER",piece,defined);
		if(value != null)
			M.baseCharStats().setStat(CharStats.STAT_GENDER,value.charAt(0));
		else
			M.baseCharStats().setStat(CharStats.STAT_GENDER,CMLib.dice().rollPercentage()>50?'M':'F');
		value = fillOutStatCode(M,ignoreStats,"MOB_","RACE",piece,defined);
		if(value != null)
		{
			Race R=CMClass.getRace(value);
			if(R!=null)
				R.setHeightWeight(M.basePhyStats(),(char)M.baseCharStats().getStat(CharStats.STAT_GENDER));
		}
		
		List<XMLLibrary.XMLpiece> choices = getAllChoices(M,ignoreStats,"MOB_","FACTION", piece, defined, true);
		if((choices!=null)&&(choices.size()>0)) 
		{
			for(int c=0;c<choices.size();c++)
			{
				XMLLibrary.XMLpiece valPiece = choices.get(c);
				String f = CMLib.xml().getParmValue(valPiece.parms,"ID");
				String v = CMLib.xml().getParmValue(valPiece.parms,"VALUE");
				M.addFaction(f, Integer.parseInt(v));
			}
		}
		ignoreStats.addAll(Arrays.asList(new String[]{"CLASS","NAME","LEVEL","GENDER"}));
		fillOutStatCodes(M,ignoreStats,"MOB_",piece,defined);
		M.recoverCharStats();
		M.recoverPhyStats();
		M.recoverMaxState();
		
		List<Item> items = findItems(piece,defined);
		for(int i=0;i<items.size();i++) {
			Item I=items.get(i);
			M.addItem(I);
			I.setSavable(true);
			I.wearIfPossible(M);
		}
		List<Ability> aV = findAffects(piece,defined);
		for(int i=0;i<aV.size();i++) {
			Ability A=aV.get(i);
			A.setSavable(true);
			M.addNonUninvokableEffect(A);
		}
		List<Behavior> bV= findBehaviors(piece,defined);
		for(int i=0;i<bV.size();i++) {
			Behavior B=bV.get(i);
			B.setSavable(true);
			M.addBehavior(B);
		}
		List<Ability> abV = findAbilities(piece,defined);
		for(int i=0;i<abV.size();i++) {
			Ability A=abV.get(i);
			A.setSavable(true);
			M.addAbility(A);
		}
		ShopKeeper SK=CMLib.coffeeShops().getShopKeeper(M);
		if(SK!=null)
		{
			List<Environmental> iV = findShopInventory(piece,defined);
			if(iV.size()>0)
				SK.getShop().emptyAllShelves();
			for(int i=0;i<iV.size();i++)
				SK.getShop().addStoreInventory(iV.get(i));
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
	
	public List<Exit> findExits(XMLLibrary.XMLpiece piece, Map<String,Object> defined) throws CMException
	{
		List<Exit> V = new Vector<Exit>();
		String tagName="EXIT";
		List<XMLLibrary.XMLpiece> choices = getAllChoices(null,null,null,tagName, piece, defined,true);
		if((choices==null)||(choices.size()==0)) return V;
		for(int c=0;c<choices.size();c++)
		{
			XMLLibrary.XMLpiece valPiece = choices.get(c);
			if(valPiece.parms.containsKey("VALIDATE") && !testCondition(CMLib.xml().restoreAngleBrackets(CMLib.xml().getParmValue(valPiece.parms,"VALIDATE")),valPiece, defined))
				continue;
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.MUDPERCOLATOR)) 
				Log.debugOut("MUDPercolator","Build Exit: "+CMStrings.limit(valPiece.value,80)+"...");
			defineReward(null,null,null,valPiece,null,defined);
			Set<String> definedSet=getPrevouslyDefined(defined,tagName+"_");
			Exit E=buildExit(valPiece,defined);
			V.add(E);
			clearNewlyDefined(defined, definedSet, tagName+"_");
	   }
		return V;
	}
	
	// remember to check ROOMLINK_DIR for N,S,E,W,U,D,etc..
	protected Exit buildExit(XMLLibrary.XMLpiece piece, Map<String,Object> defined) throws CMException
	{
		final List<String> ignoreStats=new XVector<String>();
		String classID = findString("class",piece,defined);
		Exit E = CMClass.getExit(classID);
		if(E == null) throw new CMException("Unable to build exit on classID '"+classID+"', Data: "+CMParms.toStringList(piece.parms)+":"+piece.value);
		addDefinition("EXIT_CLASS",classID,defined);
		ignoreStats.add("CLASS");
		fillOutStatCodes(E,ignoreStats,"EXIT_",piece,defined);
		List<Ability> aV = findAffects(piece,defined);
		for(int i=0;i<aV.size();i++)
		{
			Ability A=aV.get(i);
			A.setSavable(true);
			E.addNonUninvokableEffect(A);
		}
		List<Behavior> bV= findBehaviors(piece,defined);
		for(int i=0;i<bV.size();i++)
		{
			Behavior B=bV.get(i);
			B.setSavable(true);
			E.addBehavior(B);
		}
		E.text();
		E.setMiscText(E.text());
		E.recoverPhyStats();
		return E;
	}
	
	protected List<Environmental> findShopInventory(XMLLibrary.XMLpiece piece, Map<String,Object> defined) throws CMException
	{
		List<Environmental> V = new Vector<Environmental>();
		List<XMLLibrary.XMLpiece> choices = getAllChoices(null,null,null,"SHOPINVENTORY", piece, defined,true);
		if((choices==null)||(choices.size()==0)) return V;
		XMLLibrary.XMLpiece shopPiece = choices.get(CMLib.dice().roll(1,choices.size(),-1));
		V.addAll(findItems(shopPiece,defined));
		V.addAll(findMobs(shopPiece,defined));
		V.addAll(findAbilities(shopPiece,defined));
		return V;
	}

	public Set<String> getPrevouslyDefined(Map<String,Object> defined, String prefix)
	{
		Set<String> prevSet=new HashSet<String>();
		for(String key : defined.keySet())
			if(key.toUpperCase().startsWith(prefix.toUpperCase()))
				prevSet.add(key.toUpperCase());
		return prevSet;
	}
	
	public void clearNewlyDefined(Map<String,Object> defined, Set<String> exceptSet, String prefix)
	{
		Set<String> clearSet=new HashSet<String>();
		for(String key : defined.keySet())
			if(key.toUpperCase().startsWith(prefix.toUpperCase()) 
			&& (!exceptSet.contains(key.toUpperCase()))
			&& (!key.startsWith("_")))
				clearSet.add(key);
		for(String key : clearSet)
			defined.remove(key);
	}
	
	public List<Item> findItems(XMLLibrary.XMLpiece piece, Map<String,Object> defined) throws CMException
	{
		List<Item> V = new Vector<Item>();
		String tagName="ITEM";
		List<XMLLibrary.XMLpiece> choices = getAllChoices(null,null,null,tagName, piece, defined,true);
		if((choices==null)||(choices.size()==0)) return V;
		for(int c=0;c<choices.size();c++)
		{
			XMLLibrary.XMLpiece valPiece = choices.get(c);
			if(valPiece.parms.containsKey("VALIDATE") && !testCondition(CMLib.xml().restoreAngleBrackets(CMLib.xml().getParmValue(valPiece.parms,"VALIDATE")),valPiece, defined))
				continue;
			defineReward(null,null,null,valPiece,null,defined);
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.MUDPERCOLATOR)) 
				Log.debugOut("MUDPercolator","Build Item: "+CMStrings.limit(valPiece.value,80)+"...");
			Set<String> definedSet=getPrevouslyDefined(defined,tagName+"_");
			try{
				V.addAll(buildItem(valPiece,defined));
			}catch(CMException e){ 
				throw e;
			}
			clearNewlyDefined(defined, definedSet, tagName+"_");
		}
		return V;
	}

	protected List<Item> findContents(XMLLibrary.XMLpiece piece, Map<String,Object> defined) throws CMException
	{
		List<Item> V = new Vector<Item>();
		String tagName="CONTENT";
		List<XMLLibrary.XMLpiece> choices = getAllChoices(null,null,null,tagName, piece, defined,true);
		if((choices==null)||(choices.size()==0)) return V;
		for(int c=0;c<choices.size();c++)
		{
			XMLLibrary.XMLpiece valPiece = choices.get(c);
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.MUDPERCOLATOR)) 
				Log.debugOut("MUDPercolator","Found Content: "+valPiece.value);
			V.addAll(findItems(valPiece,defined));
		}
		return V;
	}

	protected String getMetacraftFilter(String recipe, XMLLibrary.XMLpiece piece, Map<String,Object> defined, Triad<Integer,Integer,Class<?>[]> filter) throws CMException
	{
		int levelLimit=-1;
		int levelFloor=-1;
		Class<?>[] deriveClasses=new Class[0]; 
		Map.Entry<Character, String>[] otherParms=CMStrings.splitMulti(recipe, splitters);
		recipe=otherParms[0].getValue();
		if(otherParms.length==1) 
			return recipe;
		for(int i=1;i<otherParms.length;i++)
		{
			if(otherParms[i].getKey().charValue()=='<')
			{
				String lvlStr=strFilter(null,null,null,otherParms[i].getValue().trim(),piece, defined);
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
				String lvlStr=strFilter(null,null,null,otherParms[i].getValue().trim(),piece, defined);
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
				String classStr=strFilter(null,null,null,otherParms[i].getValue().trim(),piece, defined);
				Object O=CMClass.getItemPrototype(classStr);
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
		for(Class<?> C : deriveClasses)
			if(C.isAssignableFrom(I.getClass()))
				return true;
		return false;
	}
	
	protected List<Item> buildItem(XMLLibrary.XMLpiece piece, Map<String,Object> defined) throws CMException
	{
		String classID = findString("class",piece,defined);
		List<Item> contents = new Vector<Item>();
		final List<String> ignoreStats=new XVector<String>();
		if(classID.toLowerCase().startsWith("metacraft"))
		{
			String classRest=classID.substring(9).toLowerCase().trim();
			Triad<Integer,Integer,Class<?>[]> filter = new Triad<Integer,Integer,Class<?>[]>(Integer.valueOf(-1),Integer.valueOf(-1),new Class<?>[0]);
			String recipe="anything";
			if(classRest.startsWith(":"))
			{
				recipe=getMetacraftFilter(classRest.substring(1).trim(), piece, defined, filter);
			}
			else
			{
				recipe = findString("NAME",piece,defined);
				if((recipe == null)||(recipe.length()==0)) 
					throw new CMException("Unable to metacraft with malformed class Data: "+CMParms.toStringList(piece.parms)+":"+piece.value);
			}
			
			String materialStr = findOptionalString(null,null,null,"material",piece,defined);
			int material=-1;
			if(materialStr!=null)
				 material = RawMaterial.CODES.FIND_IgnoreCase(materialStr);
			List<ItemCraftor> craftors=new Vector<ItemCraftor>();
			for(Enumeration<Ability> e=CMClass.abilities();e.hasMoreElements();)
			{
				Ability A=e.nextElement();
				if(A instanceof ItemCraftor)
					craftors.add((ItemCraftor)A);
			}
			if(recipe.equalsIgnoreCase("anything"))
			{
				long startTime=System.currentTimeMillis();
				while((contents.size()==0)&&((System.currentTimeMillis()-startTime)<1000))
				{
					ItemCraftor skill=craftors.get(CMLib.dice().roll(1,craftors.size(),-1));
					if(skill.fetchRecipes().size()>0)
					{
						List<ItemCraftor.ItemKeyPair> skillContents=null;
						if(material>=0)
							skillContents=skill.craftAllItemSets(material);
						else
						{
							skillContents=new Vector<ItemCraftor.ItemKeyPair>();
							List<ItemCraftor.ItemKeyPair> V=skill.craftAllItemSets();
							for(ItemCraftor.ItemKeyPair pair : V)
								skillContents.add(pair);
						}
						if(skillContents.size()==0) // preliminary error messaging, just for the craft skills themselves
							Log.errOut("MUDPercolator","Tried metacrafting anything, got "+Integer.toString(skillContents.size())+" from "+skill.ID());
						else
						for(int i=skillContents.size()-1;i>=0;i--)
						{
							Item I=skillContents.get(i).item;
							if(!checkMetacraftItem(I, filter))
								skillContents.remove(i);
						}
						if(skillContents.size()>0)
							contents.add(skillContents.get(CMLib.dice().roll(1,skillContents.size(),-1)).item);
					}
				}
			}
			else
			if(recipe.toLowerCase().startsWith("any-"))
			{
				List<ItemCraftor.ItemKeyPair> skillContents=null;
				recipe=recipe.substring(4);
				for(ItemCraftor skill : craftors)
				{
					if(skill.ID().equalsIgnoreCase(recipe))
					{
						if(material>=0)
							skillContents=skill.craftAllItemSets(material);
						else
							skillContents=skill.craftAllItemSets();
						if((skillContents==null)||(skillContents.size()==0)) // this is just for checking the skills themselves
							Log.errOut("MUDPercolator","Tried metacrafting any-"+recipe+", got "+Integer.toString(contents.size())+" from "+skill.ID());
						else
						for(int i=skillContents.size()-1;i>=0;i--)
						{
							Item I=skillContents.get(i).item;
							if(!checkMetacraftItem(I, filter))
								skillContents.remove(i);
						}
						break;
					}
				}
				if((skillContents!=null)&&(skillContents.size()>0))
					contents.add(skillContents.get(CMLib.dice().roll(1,skillContents.size(),-1)).item);
			}
			else
			if(recipe.toLowerCase().startsWith("all"))
			{
				List<ItemCraftor.ItemKeyPair> skillContents=null;
				recipe=recipe.substring(3).startsWith("-")?recipe.substring(4):"";
				for(ItemCraftor skill : craftors)
				{
					if(skill.ID().equalsIgnoreCase(recipe)||(recipe.length()==0))
					{
						if(material>=0)
							skillContents=skill.craftAllItemSets(material);
						else
							skillContents=skill.craftAllItemSets();
						if((skillContents==null)||(skillContents.size()==0)) // this is just for checking the skills themselves
							Log.errOut("MUDPercolator","Tried metacrafting any-"+recipe+", got "+Integer.toString(contents.size())+" from "+skill.ID());
						else
						for(int i=skillContents.size()-1;i>=0;i--)
						{
							Item I=skillContents.get(i).item;
							if(!checkMetacraftItem(I, filter))
								skillContents.remove(i);
						}
						while((skillContents!=null)&&(skillContents.size()>0))
							contents.add(skillContents.remove(0).item);
						if(recipe.length()>0)
							break;
					}
				}
			}
			else
			{
				for(ItemCraftor skill : craftors)
				{
					List<List<String>> V=skill.matchingRecipeNames(recipe,false);
					if((V!=null)&&(V.size()>0))
					{
						ItemCraftor.ItemKeyPair pair;
						if(material>=0)
							pair=skill.craftItem(recipe,material);
						else
							pair=skill.craftItem(recipe);
						if(pair!=null)
						{
							contents.add(pair.item);
							break;
						}
					}
				}
				for(int i=contents.size()-1;i>=0;i--)
				{
					Item I=contents.get(i);
					if(!checkMetacraftItem(I, filter))
						contents.remove(i);
				}
				if(contents.size()==0)
					for(ItemCraftor skill : craftors)
					{
						List<List<String>> V=skill.matchingRecipeNames(recipe,true);
						if((V!=null)&&(V.size()>0))
						{
							ItemCraftor.ItemKeyPair pair;
							if(material>=0)
								pair=skill.craftItem(recipe,material);
							else
								pair=skill.craftItem(recipe);
							if(pair!=null)
							{
								contents.add(pair.item);
								break;
							}
						}
					}
				for(int i=contents.size()-1;i>=0;i--)
				{
					Item I=contents.get(i);
					if(!checkMetacraftItem(I, filter))
						contents.remove(i);
				}
			}
			if(contents.size()==0)
			{
				if(filter.equals(emptyMetacraftFilter))
					throw new CMException("Unable to metacraft an item called '"+recipe+"', Data: "+CMParms.toStringList(piece.parms)+":"+piece.value);
				else
					return new ArrayList<Item>(0);
			}
			for(Item I : contents)
			{
				addDefinition("ITEM_CLASS",I.ID(),defined);
				addDefinition("ITEM_NAME",I.Name(),defined); // define so we can mess with it
				addDefinition("ITEM_LEVEL",""+I.basePhyStats().level(),defined); // define so we can mess with it
				fillOutStatCode(I,ignoreStats,"ITEM_","NAME",piece,defined);
				fillOutStatCode(I,ignoreStats,"ITEM_","LEVEL",piece,defined);
			}
			ignoreStats.addAll(Arrays.asList(new String[]{"CLASS","MATERIAL","NAME","LEVEL"}));
		}
		else
		if(classID.equalsIgnoreCase("catalog"))
		{
			String name = findString("NAME",piece,defined);
			if((name == null)||(name.length()==0)) 
				throw new CMException("Unable to build a catalog item without a name, Data: "+CMParms.toStringList(piece.parms)+":"+piece.value);
			Item I = CMLib.catalog().getCatalogItem(name);
			if(I==null)
				throw new CMException("Unable to find cataloged item called '"+name+"', Data: "+CMParms.toStringList(piece.parms)+":"+piece.value);
			I=(Item)I.copyOf();
			CMLib.catalog().changeCatalogUsage(I,true);
			contents.add(I);
			addDefinition("ITEM_CLASS",I.ID(),defined);
			addDefinition("ITEM_NAME",I.Name(),defined); // define so we can mess with it
			ignoreStats.addAll(Arrays.asList(new String[]{"CLASS","NAME"}));
		}
		else
		{
			Item I = CMClass.getItem(classID);
			if(I == null) throw new CMException("Unable to build item on classID '"+classID+"', Data: "+CMParms.toStringList(piece.parms)+":"+piece.value);
			contents.add(I);
			addDefinition("ITEM_CLASS",classID,defined);
			
			if(I.isGeneric())
			{
				String name = fillOutStatCode(I,ignoreStats,"ITEM_","NAME",piece,defined);
				if((name == null)||(name.length()==0)) 
				{
					name = fillOutStatCode(I,ignoreStats,"ITEM_","NAME",piece,defined);
					throw new CMException("Unable to build an item without a name, Data: "+CMParms.toStringList(piece.parms)+":"+piece.value);
				}
				I.setName(name);
			}
			ignoreStats.addAll(Arrays.asList(new String[]{"CLASS","NAME"}));
			addDefinition("ITEM_NAME",I.Name(),defined); // define so we can mess with it
		}
		
		for(int it=0;it<contents.size();it++) // no iterator, please!!
		{
			Item I=contents.get(it);
			fillOutStatCodes(I,ignoreStats,"ITEM_",piece,defined);
			I.recoverPhyStats();
			CMLib.itemBuilder().balanceItemByLevel(I);
			I.recoverPhyStats();
			fillOutStatCodes(I,ignoreStats,"ITEM_",piece,defined);
			I.recoverPhyStats();
			
			if(I instanceof Container)
			{
				List<Item> V= findContents(piece,defined);
				for(int i=0;i<V.size();i++)
				{
					Item I2=V.get(i);
					I2.setContainer((Container)I);
					contents.add(I2);
				}
			}
			{
				List<Ability> V= findAffects(piece,defined);
				for(int i=0;i<V.size();i++)
				{
					Ability A=V.get(i);
					A.setSavable(true);
					I.addNonUninvokableEffect(A);
				}
			}
			List<Behavior> V = findBehaviors(piece,defined);
			for(int i=0;i<V.size();i++)
			{
				Behavior B=V.get(i);
				B.setSavable(true);
				I.addBehavior(B);
			}
			I.recoverPhyStats();
			I.text();
			I.setMiscText(I.text());
			I.recoverPhyStats();
		}
		return contents;
	}
	
	protected List<Ability> findAffects(XMLLibrary.XMLpiece piece, Map<String,Object> defined) throws CMException
	{ return findAbilities("AFFECT",piece,defined);}

	protected List<Ability> findAbilities(XMLLibrary.XMLpiece piece, Map<String,Object> defined) throws CMException
	{ return findAbilities("ABILITY",piece,defined);}
	
	protected List<Ability> findAbilities(String tagName, XMLLibrary.XMLpiece piece, Map<String,Object> defined) throws CMException
	{
		List<Ability> V = new Vector<Ability>();
		List<XMLLibrary.XMLpiece> choices = getAllChoices(null,null,null,tagName, piece, defined,true);
		if((choices==null)||(choices.size()==0)) return V;
		for(int c=0;c<choices.size();c++)
		{
			XMLLibrary.XMLpiece valPiece = choices.get(c);
			if(valPiece.parms.containsKey("VALIDATE") && !testCondition(CMLib.xml().restoreAngleBrackets(CMLib.xml().getParmValue(valPiece.parms,"VALIDATE")),valPiece, defined))
				continue;
			defineReward(null,null,null,valPiece,null,defined);
			Set<String> definedSet=getPrevouslyDefined(defined,tagName+"_");
			Ability A=buildAbility(valPiece,defined);
			V.add(A);
			clearNewlyDefined(defined, definedSet, tagName+"_");
		}
		return V;
	}

	protected List<Behavior> findBehaviors(XMLLibrary.XMLpiece piece, Map<String,Object> defined) throws CMException
	{
		List<Behavior> V = new Vector<Behavior>();
		String tagName="BEHAVIOR";
		List<XMLLibrary.XMLpiece> choices = getAllChoices(null,null,null,tagName, piece, defined,true);
		if((choices==null)||(choices.size()==0)) return V;
		for(int c=0;c<choices.size();c++)
		{
			XMLLibrary.XMLpiece valPiece = choices.get(c);
			if(valPiece.parms.containsKey("VALIDATE") && !testCondition(CMLib.xml().restoreAngleBrackets(CMLib.xml().getParmValue(valPiece.parms,"VALIDATE")),valPiece, defined))
				continue;
			defineReward(null,null,null,valPiece,null,defined);
			Set<String> definedSet=getPrevouslyDefined(defined,tagName+"_");
			Behavior B=buildBehavior(valPiece,defined);
			V.add(B);
			clearNewlyDefined(defined, definedSet, tagName+"_");
		}
		return V;
	}

	protected Ability buildAbility(XMLLibrary.XMLpiece piece, Map<String,Object> defined) throws CMException
	{
		String classID = findString("class",piece,defined);
		Ability A=CMClass.getAbility(classID);
		if(A == null) A=CMClass.findAbility(classID);
		if(A == null) throw new CMException("Unable to build ability on classID '"+classID+"', Data: "+CMParms.toStringList(piece.parms)+":"+piece.value);
		String value = findOptionalString(null,null,null,"PARMS",piece,defined);
		if(value != null)
			A.setMiscText(value);
		return A;
	}
	
	protected Behavior buildBehavior(XMLLibrary.XMLpiece piece, Map<String,Object> defined) throws CMException
	{
		String classID = findString("class",piece,defined);
		Behavior B=CMClass.getBehavior(classID);
		if(B == null) B=CMClass.findBehavior(classID);
		if(B == null) throw new CMException("Unable to build behavior on classID '"+classID+"', Data: "+CMParms.toStringList(piece.parms)+":"+piece.value);
		String value = findOptionalString(null,null,null,"PARMS",piece,defined);
		if(value != null)
			B.setParms(value);
		return B;
	}
	
	protected void addDefinition(String definition, String value, Map<String,Object> defined) 
	{
		definition=definition.toUpperCase().trim();
		defined.put(definition, value);
		if(definition.toUpperCase().endsWith("S"))
			definition+="ES";
		else
			definition+="S";
		String def = (String)defined.get(definition);
		if(def==null) defined.put(definition, value);
		else defined.put(definition, def+","+value);
	}
	
	protected String findOptionalString(Modifiable E, List<String> ignoreStats, String defPrefix, String tagName, XMLLibrary.XMLpiece piece, Map<String,Object> defined)
	{
		try {
			return findString(E,ignoreStats,defPrefix,tagName, piece, defined);
		} catch(CMException x) {
			return null;
		}
	}
	
	public void defineReward(Modifiable E, List<String> ignoreStats, String defPrefix, XMLLibrary.XMLpiece piece, String value, Map<String,Object> defined) throws CMException
	{
		defineReward(E,ignoreStats,defPrefix,CMLib.xml().getParmValue(piece.parms,"DEFINE"),piece,value,defined,true);
	}
	
	public void preDefineReward(Modifiable E, List<String> ignoreStats, String defPrefix, XMLLibrary.XMLpiece piece, Map<String,Object> defined) throws CMException
	{
		defineReward(E,ignoreStats,defPrefix,CMLib.xml().getParmValue(piece.parms,"PREDEFINE"),piece,piece.value,defined,false);
	}
	
	protected void defineReward(Modifiable E, List<String> ignoreStats, String defPrefix, String defineString, XMLLibrary.XMLpiece piece, String value, Map<String,Object> defined, boolean recurseAllowed) throws CMException
	{
		if((defineString!=null)&&(defineString.trim().length()>0))
		{
			Vector<String> V=CMParms.parseCommas(defineString,true);
			for(Enumeration<String> e=V.elements();e.hasMoreElements();)
			{
				String defVar=e.nextElement();
				String definition=value;
				int x=defVar.indexOf('=');
				if(x==0) continue;
				if(x>0)
				{
					definition=defVar.substring(x+1).trim();
					defVar=defVar.substring(0,x).toUpperCase().trim();
					switch(defVar.charAt(defVar.length()-1))
					{
						case '+': case '-': case '*': case '/':
						{
							char plusMinus=defVar.charAt(defVar.length()-1);
							defVar=defVar.substring(0,defVar.length()-1).trim();
							String oldVal=(String)defined.get(defVar.toUpperCase().trim());
							if((oldVal==null)||(oldVal.trim().length()==0)) oldVal="0";
							definition=oldVal+plusMinus+definition;
							break;
						}
					}
				}
				if(definition==null) definition="!";
				definition=strFilter(E,ignoreStats,defPrefix,definition,piece, defined);
				if(CMath.isMathExpression(definition))
					definition=Integer.toString(CMath.s_parseIntExpression(definition));
				if(defVar.trim().length()>0)
					defined.put(defVar.toUpperCase().trim(), definition);
				if(CMSecurity.isDebugging(CMSecurity.DbgFlag.MUDPERCOLATOR)) 
					Log.debugOut("MudPercolator","DEFINE:"+defVar.toUpperCase().trim()+"="+definition);
			}
		}
		if((piece.parent!=null)&&(piece.parent.tag.equalsIgnoreCase(piece.tag))&&(recurseAllowed))
			defineReward(E,ignoreStats,defPrefix,piece.parent,value,defined);
	}

	public String findString(Modifiable E, List<String> ignoreStats, String defPrefix, String tagName, XMLLibrary.XMLpiece piece, Map<String,Object> defined) throws CMException
	{
		tagName=tagName.toUpperCase().trim();
		String asParm = CMLib.xml().getParmValue(piece.parms,tagName);
		if(asParm != null) return strFilter(E,ignoreStats,defPrefix,asParm,piece, defined);
		
		Object asDefined = defined.get(tagName);
		if(asDefined instanceof String) 
			return (String)asDefined;
		
		List<XMLLibrary.XMLpiece> choices = getAllChoices(E,ignoreStats,defPrefix,tagName, piece, defined,true);
		if((choices==null)||(choices.size()==0))
			throw new CMException("Unable to find tag '"+tagName+"' on piece '"+piece.tag+"', Data: "+CMParms.toStringList(piece.parms)+":"+piece.value);
		StringBuffer finalValue = new StringBuffer("");
		
		for(int c=0;c<choices.size();c++)
		{
			XMLLibrary.XMLpiece valPiece = choices.get(c);
			if(valPiece.parms.containsKey("VALIDATE") && !testCondition(CMLib.xml().restoreAngleBrackets(CMLib.xml().getParmValue(valPiece.parms,"VALIDATE")),valPiece, defined))
				continue;
				
			String value=strFilter(E,ignoreStats,defPrefix,valPiece.value,piece, defined);
			defineReward(E,ignoreStats,defPrefix,valPiece,value,defined);
			
			String action = CMLib.xml().getParmValue(valPiece.parms,"ACTION");
			if((action==null)||(action.length()==0)) action="APPEND";
			if(action.equalsIgnoreCase("REPLACE"))
				finalValue = new StringBuffer(value);
			else
			if(action.equalsIgnoreCase("APPEND"))
				finalValue.append(" ").append(value);
			else
			if(action.equalsIgnoreCase("PREPEND"))
				finalValue.insert(0,' ').insert(0,value);
			else
				throw new CMException("Unknown action '"+action+" on subPiece "+valPiece.tag+" on piece '"+piece.tag+"', Data: "+CMParms.toStringList(piece.parms)+":"+piece.value);
		}
		return finalValue.toString().trim();
	}

	protected List<XMLpiece> getAllChoices(Modifiable E, List<String> ignoreStats, String defPrefix, String tagName, XMLLibrary.XMLpiece piece, Map<String,Object> defined, boolean skipTest) throws CMException
	{
		if((!skipTest)&&(!testCondition(piece,defined)))
			return new Vector<XMLpiece>(1);
		
		preDefineReward(E,ignoreStats,defPrefix,piece, defined);
		
		List<XMLpiece> choices = new Vector<XMLpiece>();
		String inserter = CMLib.xml().getParmValue(piece.parms,"INSERT");
		if(inserter!=null)
		{
			Vector<String> V=CMParms.parseCommas(inserter,true);
			for(int v=0;v<V.size();v++)
			{
				String s = V.elementAt(v);
				if(s.startsWith("$")) s=s.substring(1).trim();
				XMLLibrary.XMLpiece insertPiece =(XMLLibrary.XMLpiece)defined.get(s.toUpperCase().trim());
				if(insertPiece == null)
					throw new CMException("Undefined insert: '"+s+"' on piece '"+piece.tag+"', Data: "+CMParms.toStringList(piece.parms)+":"+piece.value);
				if(insertPiece.tag.equalsIgnoreCase(tagName))
					choices.addAll(getAllChoices(E,ignoreStats,defPrefix,tagName,insertPiece,defined,false));
			}
		}
		else
		if(piece.tag.equalsIgnoreCase(tagName))
		{
			boolean container=false;
			for(int p=0;p<piece.contents.size();p++)
				if(piece.contents.get(p).tag.equalsIgnoreCase(tagName))
				{	container=true; break;}
			if(!container)
			{
				String like = CMLib.xml().getParmValue(piece.parms,"LIKE");
				if(like!=null)
				{
					Vector<String> V=CMParms.parseCommas(like,true);
					XMLLibrary.XMLpiece origPiece = piece; 
					piece=piece.copyOf();
					for(int v=0;v<V.size();v++)
					{
						String s = V.elementAt(v);
						if(s.startsWith("$")) s=s.substring(1).trim();
						XMLLibrary.XMLpiece likePiece =(XMLLibrary.XMLpiece)defined.get(s.toUpperCase().trim());
						if((likePiece == null)||(!likePiece.tag.equalsIgnoreCase(tagName)))
							throw new CMException("Invalid like: '"+s+"' on piece '"+piece.tag+"', Data: "+CMParms.toStringList(piece.parms)+":"+piece.value);
						piece.contents.addAll(likePiece.contents);
						piece.parms.putAll(likePiece.parms);
						piece.parms.putAll(origPiece.parms);
						choices.addAll(getAllChoices(E,ignoreStats,defPrefix,tagName,likePiece,defined,false));
					}
				}
				return new XVector<XMLpiece>(piece);
			}
		}
		
		for(int p=0;p<piece.contents.size();p++)
		{
			XMLLibrary.XMLpiece subPiece = piece.contents.get(p);
			if(subPiece.tag.equalsIgnoreCase(tagName))
				choices.addAll(getAllChoices(E,ignoreStats,defPrefix,tagName,piece.contents.get(p),defined,false));
		}
		return selectChoices(E,ignoreStats,defPrefix,choices,piece,defined);
	}

	protected boolean testCondition(String condition, XMLLibrary.XMLpiece piece, Map<String,Object> defined)
	{
		Map<String,Object> fixed=new HashMap<String,Object>();
		try {
			if(condition == null) return true;
			fixed.putAll(defined);
			List<Varidentifier> ids=parseVariables(condition);
			for(Varidentifier id : ids)
			{
				try {
					String value=findString(id.var, piece, defined);
					if(CMath.isMathExpression(value))
					{
						double val=CMath.parseMathExpression(value);
						if(Math.round(val)==val)
							value=""+Math.round(val);
						else
							value=""+val;
					}
					fixed.put(id.var.toUpperCase(),value);
				} catch(CMException e) {}
			}
			boolean test= CMStrings.parseStringExpression(condition.toUpperCase(),fixed, true);
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.MUDPERCOLATOR))
				Log.debugOut("MudPercolator","TEST:"+condition+"="+test);
			return test;
		} 
		catch(Exception e)
		{
			Log.errOut("Generate",e.getMessage()+": "+condition);
			try { 
				CMStrings.parseStringExpression(condition,fixed, true); 
			} catch(Exception e1) {}
			return false;
		}
	}
	protected boolean testCondition(XMLLibrary.XMLpiece piece, Map<String,Object> defined)
	{
		return testCondition(CMLib.xml().restoreAngleBrackets(CMLib.xml().getParmValue(piece.parms,"CONDITION")),piece,defined);
	}
	
	protected String getRequirementsDescription(String values) 
	{
		if(values==null) return "";
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
		if(validValue==null) return value != null;
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
		if(values==null) return value;
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
			String[] arrayStr=CMParms.toStringArray(CMParms.parseSemicolons(values,true));
			int x=CMParms.indexOfIgnoreCase(arrayStr,value);
			if(x<0) x=0;
			return arrayStr[x];
		}
		return value;
	}
	
	public Map<String,String> getUnfilledRequirements(Map<String,Object> defined, XMLLibrary.XMLpiece piece)
	{
		String requirements = CMLib.xml().getParmValue(piece.parms,"REQUIRES");
		Map<String,String> set=new Hashtable<String,String>();
		if(requirements==null) return set;
		requirements = requirements.trim();
		Vector<String> reqs = CMParms.parseCommas(requirements,true);
		for(int r=0;r<reqs.size();r++)
		{
			String reqVariable=reqs.elementAt(r);
			if(reqVariable.startsWith("$")) reqVariable=reqVariable.substring(1).trim();
			String validValues=null;
			int x=reqVariable.indexOf('=');
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
					set.put(reqVariable.toUpperCase(), CMParms.toStringList(CMParms.parseSemicolons(validValues,true)));
			}
		}
		return set;
	}
	
	protected void checkRequirements(Map<String,Object> defined, String requirements) throws CMException
	{
		if(requirements==null) return;
		requirements = requirements.trim();
		Vector<String> reqs = CMParms.parseCommas(requirements,true);
		for(int r=0;r<reqs.size();r++)
		{
			String reqVariable=reqs.elementAt(r);
			if(reqVariable.startsWith("$")) reqVariable=reqVariable.substring(1).trim();
			String validValues=null;
			int x=reqVariable.indexOf('=');
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
	
	public void checkRequirements(XMLLibrary.XMLpiece piece, Map<String,Object> defined) throws CMException
	{
		checkRequirements(defined,CMLib.xml().getParmValue(piece.parms,"REQUIRES"));
	}
	
	protected List<XMLpiece> selectChoices(Modifiable E, List<String> ignoreStats, String defPrefix, List<XMLpiece> choices, XMLLibrary.XMLpiece piece, Map<String,Object> defined) throws CMException
	{
		String selection = CMLib.xml().getParmValue(piece.parms,"SELECT");
		if(selection == null) return choices;
		selection=selection.toUpperCase().trim();
		List<XMLLibrary.XMLpiece> selectedChoicesV=null;
		if(selection.equals("NONE")) 
			selectedChoicesV= new Vector<XMLpiece>();
		else
		if(selection.equals("ALL")) 
			selectedChoicesV=choices;
		else
		if((choices.size()==0)&&(!selection.startsWith("ANY-0"))
		&&(!selection.startsWith("FIRST-0"))
		&&(!selection.startsWith("LAST-0"))
		&&(!selection.startsWith("PICK-0"))
		&&(!selection.startsWith("LIMIT-"))
		&&(!selection.equals("ANY"))) 
			throw new CMException("Can't make selection among NONE: on piece '"+piece.tag+"', Data: "+CMParms.toStringList(piece.parms)+":"+piece.value);
		else
		if(selection.equals("FIRST"))
			selectedChoicesV= new XVector<XMLpiece>(choices.get(0));
		else
		if(selection.startsWith("FIRST-"))
		{
			int num=CMath.parseIntExpression(strFilter(E,ignoreStats,defPrefix,selection.substring(selection.indexOf('-')+1),piece, defined));
			if((num<0)||(num>choices.size())) throw new CMException("Can't pick first "+num+" of "+choices.size()+" on piece '"+piece.tag+"', Data: "+CMParms.toStringList(piece.parms)+":"+piece.value);
			selectedChoicesV=new Vector<XMLpiece>();
			for(int v=0;v<num;v++)
				selectedChoicesV.add(choices.get(v));
		}
		else
		if(selection.startsWith("LIMIT-"))
		{
			int num=CMath.parseIntExpression(strFilter(E,ignoreStats,defPrefix,selection.substring(selection.indexOf('-')+1),piece, defined));
			if(num<0) throw new CMException("Can't pick limit "+num+" of "+choices.size()+" on piece '"+piece.tag+"', Data: "+CMParms.toStringList(piece.parms)+":"+piece.value);
			selectedChoicesV=new Vector<XMLpiece>();
			if(choices.size()<=num)
				selectedChoicesV.addAll(choices);
			else
			while(selectedChoicesV.size()<num)
				selectedChoicesV.add(choices.remove(CMLib.dice().roll(1, choices.size(), -1)));
		}
		else
		if(selection.equals("LAST"))  
			selectedChoicesV=new XVector<XMLpiece>(choices.get(choices.size()-1));
		else
		if(selection.startsWith("LAST-"))
		{
			int num=CMath.parseIntExpression(strFilter(E,ignoreStats,defPrefix,selection.substring(selection.indexOf('-')+1),piece, defined));
			if((num<0)||(num>choices.size())) throw new CMException("Can't pick last "+num+" of "+choices.size()+" on piece '"+piece.tag+"', Data: "+CMParms.toStringList(piece.parms)+":"+piece.value);
			selectedChoicesV=new Vector<XMLpiece>();
			for(int v=choices.size()-num;v<choices.size();v++)
				selectedChoicesV.add(choices.get(v));
		}
		else
		if(selection.startsWith("PICK-"))
		{
			int num=CMath.parseIntExpression(strFilter(E,ignoreStats,defPrefix,selection.substring(selection.indexOf('-')+1),piece, defined));
			if((num<0)||(num>choices.size())) throw new CMException("Can't pick "+num+" of "+choices.size()+" on piece '"+piece.tag+"', Data: "+CMParms.toStringList(piece.parms)+":"+piece.value);
			selectedChoicesV=new Vector<XMLpiece>();
			List<XMLLibrary.XMLpiece> cV=new XVector<XMLLibrary.XMLpiece>(choices);
			for(int v=0;v<num;v++)
			{
				int[] weights=new int[cV.size()];
				int total=0;
				for(int c=0;c<cV.size();c++)
				{
					XMLLibrary.XMLpiece lilP=cV.get(c);
					int weight=CMath.s_parseIntExpression(CMLib.xml().getParmValue(lilP.parms,"PICKWEIGHT"));
					if(weight<1) weight=1;
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
			selectedChoicesV=new XVector<XMLpiece>(choices.get(CMLib.dice().roll(1,choices.size(),-1)));
		else
		if(selection.startsWith("ANY-"))
		{
			int num=CMath.parseIntExpression(strFilter(E,ignoreStats,defPrefix,selection.substring(selection.indexOf('-')+1),piece, defined));
			if((num<0)||(num>choices.size())) throw new CMException("Can't pick last "+num+" of "+choices.size()+" on piece '"+piece.tag+"', Data: "+CMParms.toStringList(piece.parms)+":"+piece.value);
			selectedChoicesV=new Vector<XMLpiece>();
			List<XMLLibrary.XMLpiece> cV=new XVector<XMLLibrary.XMLpiece>(choices);
			for(int v=0;v<num;v++)
			{
				int x=CMLib.dice().roll(1,cV.size(),-1);
				selectedChoicesV.add(cV.get(x));
				cV.remove(x);
			}
		}
		else
		if(selection.startsWith("REPEAT-"))
		{
			int num=CMath.parseIntExpression(strFilter(E,ignoreStats,defPrefix,selection.substring(selection.indexOf('-')+1),piece, defined));
			if(num<0) throw new CMException("Can't pick last "+num+" of "+choices.size()+" on piece '"+piece.tag+"', Data: "+CMParms.toStringList(piece.parms)+":"+piece.value);
			selectedChoicesV=new Vector<XMLpiece>();
			List<XMLLibrary.XMLpiece> cV=new XVector<XMLLibrary.XMLpiece>(choices);
			for(int v=0;v<num;v++)
			{
				int x=CMLib.dice().roll(1,cV.size(),-1);
				selectedChoicesV.add(cV.get(x));
			}
		}
		else
		if((selection.trim().length()>0)&&CMath.isMathExpression(selection))
		{
			int num=CMath.parseIntExpression(strFilter(E,ignoreStats,defPrefix,selection.substring(selection.indexOf('-')+1),piece, defined));
			if((num<0)||(num>choices.size())) throw new CMException("Can't pick any "+num+" of "+choices.size()+" on piece '"+piece.tag+"', Data: "+CMParms.toStringList(piece.parms)+":"+piece.value);
			selectedChoicesV=new Vector<XMLpiece>();
			List<XMLLibrary.XMLpiece> cV=new XVector<XMLLibrary.XMLpiece>(choices);
			for(int v=0;v<num;v++)
			{
				int x=CMLib.dice().roll(1,cV.size(),-1);
				selectedChoicesV.add(cV.get(x));
				cV.remove(x);
			}
		}
		else
			throw new CMException("Illegal select type '"+selection+"' on piece '"+piece.tag+"', Data: "+CMParms.toStringList(piece.parms)+":"+piece.value);
		return selectedChoicesV;
	}
	
	protected String strFilter(Modifiable E, List<String> ignoreStats, String defPrefix, String str, XMLpiece piece, Map<String,Object> defined) throws CMException
	{
		List<Varidentifier> vars=parseVariables(str);
		boolean killArticles=str.toLowerCase().startsWith("(a(n))");
		while(vars.size()>0)
		{
			Varidentifier V=vars.remove(vars.size()-1);
			Object val;
			if(V.isMathExpression)
			{
				String expression=strFilter(E,ignoreStats,defPrefix,V.var,piece, defined);
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
			if(V.var.toUpperCase().startsWith("STAT:") && (E!=null) && (E.isStat(V.var.substring(5))))
				val=E.getStat(V.var.substring(5));
			else
				val = defined.get(V.var.toUpperCase().trim());
			if(val instanceof XMLLibrary.XMLpiece) 
				val = findString(E,ignoreStats,defPrefix,"STRING",(XMLLibrary.XMLpiece)val,defined);
			if((val == null)&&(defPrefix!=null)&&(defPrefix.length()>0)&&(E!=null))
			{
				String preValue=V.var;
				if(preValue.toUpperCase().startsWith(defPrefix.toUpperCase()))
				{
					preValue=preValue.toUpperCase().substring(defPrefix.length());
					if((E.isStat(preValue))
					&&((ignoreStats==null)||(!ignoreStats.contains(preValue.toUpperCase()))))
					{
						val=fillOutStatCode(E,ignoreStats,defPrefix,preValue,piece,defined);
						if(ignoreStats!=null)
							ignoreStats.add(preValue.toUpperCase());
					}
				}
			}
			if(val == null) 
				throw new CMException("Unknown variable '$"+V.var+"' in str '"+str+"'",new CMException("$"+V.var));
			if(V.toUpperCase) val=val.toString().toUpperCase();
			if(V.toLowerCase) val=val.toString().toLowerCase();
			if(V.toPlural) val=CMLib.english().makePlural(val.toString());
			if(V.toCapitalized) val=CMStrings.capitalizeAndLower(val.toString());
			if(killArticles) val=CMLib.english().cleanArticles(val.toString());
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
	public String findString(String tagName, XMLpiece piece, Map<String, Object> defined) throws CMException 
	{
		return findString(null,null,null,tagName,piece,defined);
	}
}
