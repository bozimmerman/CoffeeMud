package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.exceptions.CMException;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;


/* 
   Copyright 2000-2008 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class MUDPercolator extends StdLibrary implements AreaGenerationLibrary
{
    public String ID(){return "MUDPercolator";}

    private Hashtable<String,Class<LayoutManager>> mgrs = new Hashtable<String,Class<LayoutManager>>();
    
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
    
    public void buildDefinedIDSet(Vector xmlRoot, Hashtable defined)
    {
        if(xmlRoot==null) return;
        for(int v=0;v<xmlRoot.size();v++)
        {
            XMLLibrary.XMLpiece piece = (XMLLibrary.XMLpiece)xmlRoot.elementAt(v);
            String id = CMLib.xml().getParmValue(piece.parms,"ID");
            if((id!=null)&&(id.length()>0))
                defined.put(id.toUpperCase().trim(),piece);
            buildDefinedIDSet(piece.contents,defined);
        }
    }
    
    public boolean activate()
    { 
        String filePath="com/planet_ink/coffee_mud/Libraries/layouts";
        CMProps page = CMProps.instance();
        Vector layouts=CMClass.loadClassList(filePath,page.getStr("LIBRARY"),"/layouts",LayoutManager.class,true);
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
    public Room buildRoom(XMLLibrary.XMLpiece piece, Hashtable defined, Exit[] exits, int direction) throws CMException
    {
        addDefinition("DIRECTION",Directions.getDirectionName(direction).toLowerCase(),defined);
        
        String classID = findString("class",piece,defined);
        Room R = CMClass.getLocale(classID);
        if(R == null) throw new CMException("Unable to build room on classID '"+classID+"', Data: "+piece.value);
        addDefinition("ROOM_CLASS",classID,defined);
        String title = findString("title",piece,defined);
        R.setDisplayText(title);
        addDefinition("ROOM_TITLE",title,defined);
        String description = findString("description",piece,defined);
        R.setDescription(description);
        addDefinition("ROOM_DESCRIPTION",description,defined);
    	final String[] ignoreStats={"CLASS","DISPLAY","DESCRIPTION"};
    	fillOutStats(R, ignoreStats, "ROOM_", piece, defined);
        Vector V;
        V = findMobs(piece,defined);
        for(int i=0;i<V.size();i++) {
        	MOB M=(MOB)V.elementAt(i);
        	R.bringMobHere(M,false);
        }
        V = findItems(piece,defined);
        for(int i=0;i<V.size();i++) {
        	Item I=(Item)V.elementAt(i);
        	R.bringItemHere(I, 0,false);
        }
        V = findAffects(piece,defined);
        for(int i=0;i<V.size();i++) {
        	Ability A=(Ability)V.elementAt(i);
        	R.addNonUninvokableEffect(A);
        }
        V = findBehaviors(piece,defined);
        for(int i=0;i<V.size();i++) {
        	Behavior B=(Behavior)V.elementAt(i);
        	R.addBehavior(B);
        }
        for(int dir=0;dir<Directions.NUM_DIRECTIONS();dir++) {
        	Exit E=exits[dir];
        	if((E==null)&&(defined.containsKey("ROOMLINK_"+Directions.getDirectionChar(dir).toUpperCase())))
        	{
        		defined.put("ROOMLINK_DIR",Directions.getDirectionChar(dir).toUpperCase());
        		E=findExit(piece, defined);
        		defined.remove("ROOMLINK_DIR");
        	}
    		R.setRawExit(dir, E);
        }
        return R;
    }

    protected void layoutRecursiveFill(LayoutNode n, Vector<LayoutNode> group, LayoutTypes type)
    {
    	if(n != null)
		for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
			if(n.links().containsKey(Integer.valueOf(d))) {
				LayoutNode offN=n.links().get(Integer.valueOf(d));
				if((offN.type()==type)
				&&(!group.contains(offN))) {
					group.addElement(offN);
					layoutRecursiveFill(offN,group,type);
				}
			}
    }
    
    protected void layoutFollow(LayoutNode n, LayoutTypes type, int direction, Vector<LayoutNode> group)
    {
		n=n.links().get(Integer.valueOf(direction));
		while((n != null) &&(n.type()==LayoutTypes.street) &&(!group.contains(n)))
		{
			group.addElement(n);
			n=n.links().get(Integer.valueOf(Directions.NORTH));
		}
    }
    
    // vars created: LINK_DIRECTION, AREA_CLASS, AREA_NAME, AREA_DESCRIPTION, AREA_LAYOUT, AREA_SIZE
    public Area buildArea(XMLLibrary.XMLpiece piece, Hashtable defined, int direction) throws CMException
    {
    	defined.put("DIRECTION",Directions.getDirectionName(direction).toLowerCase());
        String classID = findString("class",piece,defined);
        Area A = CMClass.getAreaType(classID);
        if(A == null) 
        	throw new CMException("Unable to build area on classID '"+classID+"', Data: "+piece.value);
        defined.put("AREA_CLASS",classID);
        String name = findString("name",piece,defined);
        A.setName(name);
        defined.put("AREA_NAME",name);
        String author = findOptionalString("author",piece,defined);
        if(author != null)
	        A.setAuthorID(author);
        String description = findOptionalString("description",piece,defined);
        if(description != null)
        {
	        A.setDescription(description);
        	defined.put("AREA_DESCRIPTION",description);
        }
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
        String[] ignoreStats={"CLASS","NAME","DESCRIPTION","LAYOUT","SIZE"};
        fillOutStats(A, ignoreStats,"AREA_",piece,defined);
        Vector roomsLayout = layoutManager.generate(CMath.s_int(size),direction);
        if((roomsLayout==null)||(roomsLayout.size()==0))
        	throw new CMException("Unable to fill area of size "+size+" off layout "+layoutManager.name());
        
        Vector V;
        V = findAffects(piece,defined);
        for(int i=0;i<V.size();i++)
        {
        	Ability AB=(Ability)V.elementAt(i);
        	A.addNonUninvokableEffect(AB);
        }
        V = findBehaviors(piece,defined);
        for(int i=0;i<V.size();i++)
        {
        	Behavior B=(Behavior)V.elementAt(i);
        	A.addBehavior(B);
        }
        
        CMLib.map().addArea(A); // necessary for proper naming.
        
        // now break our rooms into logical groups, generate those rooms.
        Vector<Vector<LayoutNode>> roomGroups = new Vector<Vector<LayoutNode>>();
        LayoutNode magicRoom = (LayoutNode)roomsLayout.firstElement();
        int lastSize = -1;
        while(roomsLayout.size() < lastSize)
        {
        	lastSize = roomsLayout.size();
        	LayoutNode node=(LayoutNode)roomsLayout.firstElement();
        	Vector<LayoutNode> group=new Vector<LayoutNode>();
        	if(node.type()==LayoutTypes.street) {
            	group.add(node);
        		LayoutRuns run=node.getFlagRuns();
        		if(run==LayoutRuns.ns) {
        			layoutFollow(node,LayoutTypes.street,Directions.NORTH,group);
        			layoutFollow(node,LayoutTypes.street,Directions.SOUTH,group);
        		} else
        		if(run==LayoutRuns.ew) {
        			layoutFollow(node,LayoutTypes.street,Directions.EAST,group);
        			layoutFollow(node,LayoutTypes.street,Directions.WEST,group);
	    		} else
        		if(run==LayoutRuns.nesw) {
        			layoutFollow(node,LayoutTypes.street,Directions.NORTHEAST,group);
        			layoutFollow(node,LayoutTypes.street,Directions.SOUTHWEST,group);
	    		} else
        		if(run==LayoutRuns.nwse) {
        			layoutFollow(node,LayoutTypes.street,Directions.NORTHWEST,group);
        			layoutFollow(node,LayoutTypes.street,Directions.SOUTHEAST,group);
        		}
        	} 
        	if(node.type()==LayoutTypes.leaf) {
            	group.add(node);
        		for(Integer linkDir : node.links().keySet())
        			if(roomsLayout.contains(node.links().get(linkDir)))
        			{
	        			if((node.links().get(linkDir).type()==LayoutTypes.leaf)
	        			||(node.links().get(linkDir).type()==LayoutTypes.interior)&&(node.isFlagged(LayoutFlags.offleaf)))
	        				group.addElement(node.links().get(linkDir));
        			}
        	}
        	for(LayoutNode n : group)
        		roomsLayout.remove(n);
        	if(group.size()>0)
        		roomGroups.add(group);
        }
        
        while(roomsLayout.size() >0)
        {
        	lastSize = roomsLayout.size();
        	LayoutNode node=(LayoutNode)roomsLayout.firstElement();
        	Vector<LayoutNode> group=new Vector<LayoutNode>();
        	group.add(node);
    		layoutRecursiveFill(node,group,node.type());
        	for(LayoutNode n : group)
        		roomsLayout.remove(n);
			roomGroups.add(group);
        }
        // make CERTAIN that the magic first room in the layout always
        // gets ID#0.
        for(int g=0;g<roomGroups.size();g++)
        {
        	Vector<LayoutNode> group=(Vector<LayoutNode>)roomGroups.elementAt(g);
        	if(group.contains(magicRoom)) {
        		if((group.size()>1)&&(group.firstElement() != magicRoom)) {
	        		group.remove(magicRoom);
	        		group.insertElementAt(magicRoom,0);
        		}
        		if((roomGroups.size()>1)&&(roomGroups.firstElement() != group)) {
        			roomGroups.remove(group);
        			roomGroups.insertElementAt(group,0);
        		}
        		break;
        	}
        }
        //now generate the rooms and add them to the area
        for(Vector<LayoutNode> group : roomGroups)
        {
        	Hashtable groupDefined = (Hashtable)defined.clone();
        	for(LayoutNode node : group)
        	{
        		for(LayoutTags key : node.tags().keySet())
        			groupDefined.put("ROOMTAG_"+key.toString().toUpperCase(),node.tags().get(key));
        		Exit[] exits=new Exit[Directions.NUM_DIRECTIONS()];
        		for(Integer linkDir : node.links().keySet()) {
        			LayoutNode linkNode = node.links().get(linkDir);
        			if(linkNode.room() != null) {
	        			int opDir=Directions.getOpDirectionCode(linkDir.intValue());
	        			exits[linkDir.intValue()]=linkNode.room().getExitInDir(opDir);
        			}
        			groupDefined.put("ROOMLINK_"+Directions.getDirectionChar(linkDir.intValue()).toUpperCase(),"true");
        		}
        		Room R=findRoom(piece, groupDefined, exits, direction);
        		R.setRoomID(A.getNewRoomID(null,-1));
        		R.setArea(A);
        		A.addProperRoom(R);
        		node.setRoom(R);
        		for(LayoutTags key : node.tags().keySet())
        			groupDefined.remove("ROOMTAG_"+key.toString().toUpperCase());
        		for(Integer linkDir : node.links().keySet())
        			groupDefined.remove("ROOMLINK_"+Directions.getDirectionChar(linkDir.intValue()).toUpperCase());
        	}
            for(Enumeration e=groupDefined.keys();e.hasMoreElements();)
            {
            	String key=(String)e.nextElement();
            	if(key.startsWith("_")&&(!defined.containsKey(key)))
            		defined.put(key, groupDefined.get(key));
            }
        }
        return A;
    }
    
    public Vector findMobs(XMLLibrary.XMLpiece piece, Hashtable defined) throws CMException
    {
    	Vector V = new Vector();
    	String tagName="MOB";
        Vector choices = getAllChoices(tagName, piece, defined);
        if((choices==null)||(choices.size()==0)) return V;
        for(int c=0;c<choices.size();c++) {
            XMLLibrary.XMLpiece valPiece = (XMLLibrary.XMLpiece)choices.elementAt(c);
            defineReward(valPiece,null,defined);
            try {
	            MOB M=buildMob(valPiece,defined);
	            V.addElement(M);
	        }catch(Exception e){}
        }
    	return V;
    }
    
    public Room findRoom(XMLLibrary.XMLpiece piece, Hashtable defined, Exit[] exits, int directions) throws CMException
    {
    	String tagName="ROOM";
        Vector choices = getAllChoices(tagName, piece, defined);
        if((choices==null)||(choices.size()==0)) return null;
        DVector roomChoices = new DVector(3);
        for(int c=0;c<choices.size();c++)
        {
        	XMLLibrary.XMLpiece valPiece = (XMLLibrary.XMLpiece)choices.elementAt(c);
            defineReward(valPiece,null,defined);
        	Exit[] rExits=exits.clone();
        	Hashtable rDefined=(Hashtable)defined.clone();
            try {
	            Room R=buildRoom(valPiece,rDefined,rExits,directions);
	            if(R!=null)
	            	roomChoices.addElement(R,rExits,rDefined);
            } catch(Exception e){}
        }
        if((roomChoices==null)||(roomChoices.size()==0)) return null;
        
        int dex=CMLib.dice().roll(1,roomChoices.size(),-1);
        Room room=(Room)roomChoices.elementAt(dex,1);
        Exit[] rExits=(Exit[])roomChoices.elementAt(dex,2);
        Hashtable rDefined=(Hashtable)roomChoices.elementAt(dex,3);
        roomChoices.clear();
        
        defined.clear();
        defined.putAll(rDefined);
        
        for(int e=0;e<rExits.length;e++)
        	exits[e]=rExits[e];
        return room;
    }
    
    public DVector findRooms(XMLLibrary.XMLpiece piece, Hashtable defined, Exit[] exits, int direction) throws CMException
    {
    	DVector DV = new DVector(2);
    	String tagName="ROOM";
        Vector choices = getAllChoices(tagName, piece, defined);
        if((choices==null)||(choices.size()==0)) return DV;
        for(int c=0;c<choices.size();c++) {
            XMLLibrary.XMLpiece valPiece = (XMLLibrary.XMLpiece)choices.elementAt(c);
            defineReward(valPiece,null,defined);
            Exit[] theseExits=exits.clone();
            try {
	            Room R=buildRoom(valPiece,defined,theseExits,direction);
	            DV.addElement(R,theseExits);
	        }catch(Exception e){}
        }
    	return DV;
    }
    
    public Exit findExit(XMLLibrary.XMLpiece piece, Hashtable defined) throws CMException
    {
    	String tagName="EXIT";
        Vector choices = getAllChoices(tagName, piece, defined);
        if((choices==null)||(choices.size()==0)) return null;
        Vector exitChoices = new Vector();
        for(int c=0;c<choices.size();c++)
        {
            try {
            	XMLLibrary.XMLpiece valPiece = (XMLLibrary.XMLpiece)choices.elementAt(c);
                defineReward(valPiece,null,defined);
	            Exit E=buildExit(valPiece,defined);
	            if(E!=null)
	            	exitChoices.addElement(E);
            }catch(Exception e){}
        }
        if((exitChoices==null)||(exitChoices.size()==0)) return null;
        return (Exit)exitChoices.elementAt(CMLib.dice().roll(1,exitChoices.size(),-1));
    }

    protected void fillOutStatCodes(CMModifiable E, String[] ignoreStats, String defPrefix, XMLLibrary.XMLpiece piece, Hashtable defined)
    {
        String[] statCodes = E.getStatCodes();
        for(int s=0;s<statCodes.length;s++) {
        	String stat=statCodes[s];
        	if(!CMParms.contains(ignoreStats, stat)) {
	            String value = findOptionalString(stat,piece,defined);
	            if(value != null) {
	            	E.setStat(stat, value);
	            	if(defPrefix.length()>0)
			            addDefinition(defPrefix+stat,E.getStat(stat),defined);
	            }
        	}
        }
    }
    
    protected void fillOutStats(Environmental E, String[] ignoreStats, String defPrefix, XMLLibrary.XMLpiece piece, Hashtable defined)
    {
    	fillOutStatCodes(E,ignoreStats,defPrefix,piece,defined);
    	fillOutStatCodes(E.baseEnvStats(),ignoreStats,defPrefix,piece,defined);
    	if(E instanceof MOB)
    		fillOutStatCodes(((MOB)E).baseCharStats(),ignoreStats,defPrefix,piece,defined);
    }
    
    protected MOB buildMob(XMLLibrary.XMLpiece piece, Hashtable defined) throws CMException
    {
        String classID = findString("class",piece,defined);
        MOB M = null;
        String[] ignoreStats={};
        if(classID.equalsIgnoreCase("catalog"))
        {
            String name = findString("name",piece,defined);
            if((name == null)||(name.length()==0)) 
            	throw new CMException("Unable to build a catalog mob without a name, Data: "+piece.value);
            M = CMLib.catalog().getCatalogMob(name);
            if(M==null)
	        	throw new CMException("Unable to find cataloged mob called '"+name+"', Data: "+piece.value);
            M=(MOB)M.copyOf();
            CMLib.catalog().changeCatalogUsage(M,true);
            addDefinition("MOB_CLASS",M.ID(),defined);
            ignoreStats=new String[]{"CLASS","NAME","LEVEL"};
        }
        else
        {
	        M = CMClass.getMOB(classID);
	        if(M == null) throw new CMException("Unable to build mob on classID '"+classID+"', Data: "+piece.value);
	        addDefinition("MOB_CLASS",classID,defined);
            ignoreStats=new String[]{"CLASS","LEVEL"};
        }
        
        String value = findOptionalString("LEVEL",piece,defined);
        if(value != null) {
        	M.setStat("LEVEL", value);
            addDefinition("MOB_LEVEL",M.getStat("LEVEL"),defined);
            CharClass origClass =M.baseCharStats().getCurrentClass();
            origClass.fillOutMOB(M,M.baseEnvStats().level());
        }
        fillOutStats(M,ignoreStats,"MOB_",piece,defined);
        
        Vector items = findItems(piece,defined);
        for(int i=0;i<items.size();i++) {
        	Item I=(Item)items.elementAt(i);
        	M.addInventory(I);
        	I.wearIfPossible(M);
        }
        Vector V = findAffects(piece,defined);
        for(int i=0;i<V.size();i++) {
        	Ability A=(Ability)V.elementAt(i);
        	M.addNonUninvokableEffect(A);
        }
        V = findBehaviors(piece,defined);
        for(int i=0;i<V.size();i++) {
        	Behavior B=(Behavior)V.elementAt(i);
        	M.addBehavior(B);
        }
        V = findAbilities(piece,defined);
        for(int i=0;i<V.size();i++) {
        	Ability A=(Ability)V.elementAt(i);
        	M.addAbility(A);
        }
        ShopKeeper SK=CMLib.coffeeShops().getShopKeeper(M);
        if(SK!=null)
        {
	        V = findShopInventory(piece,defined);
	        if(V.size()>0)
	        	SK.getShop().emptyAllShelves();
	        for(int i=0;i<V.size();i++)
	        	SK.getShop().addStoreInventory((Environmental)V.elementAt(i));
        }
        
        M.text();
        M.setMiscText(M.text());
        M.recoverCharStats();
        M.recoverEnvStats();
        M.recoverMaxState();
        return M;
    }
    
    public Vector findExits(XMLLibrary.XMLpiece piece, Hashtable defined) throws CMException
    {
    	Vector V = new Vector();
    	String tagName="EXIT";
        Vector choices = getAllChoices(tagName, piece, defined);
        if((choices==null)||(choices.size()==0)) return V;
        for(int c=0;c<choices.size();c++)
        {
            XMLLibrary.XMLpiece valPiece = (XMLLibrary.XMLpiece)choices.elementAt(c);
            defineReward(valPiece,null,defined);
            try {
	            Exit E=buildExit(valPiece,defined);
	            V.addElement(E);
	        }catch(Exception e){}
       }
    	return V;
    }
    
    // remember to check ROOMLINK_DIR for N,S,E,W,U,D,etc..
    protected Exit buildExit(XMLLibrary.XMLpiece piece, Hashtable defined) throws CMException
    {
        String classID = findString("class",piece,defined);
        Exit E = CMClass.getExit(classID);
        if(E == null) throw new CMException("Unable to build exit on classID '"+classID+"', Data: "+piece.value);
        addDefinition("EXIT_CLASS",classID,defined);
        String[] ignoreStats={"CLASS"};
        fillOutStats(E,ignoreStats,"EXIT_",piece,defined);
        Vector V = findAffects(piece,defined);
        for(int i=0;i<V.size();i++)
        {
        	Ability A=(Ability)V.elementAt(i);
        	E.addNonUninvokableEffect(A);
        }
        V = findBehaviors(piece,defined);
        for(int i=0;i<V.size();i++)
        {
        	Behavior B=(Behavior)V.elementAt(i);
        	E.addBehavior(B);
        }
        E.text();
        E.setMiscText(E.text());
        E.recoverEnvStats();
        return E;
    }
    
    protected Vector findShopInventory(XMLLibrary.XMLpiece piece, Hashtable defined) throws CMException
    {
    	Vector V = new Vector();
        Vector choices = getAllChoices("SHOPINVENTORY", piece, defined);
        if((choices==null)||(choices.size()==0)) return V;
        XMLLibrary.XMLpiece shopPiece = (XMLLibrary.XMLpiece)choices.elementAt(CMLib.dice().roll(1,choices.size(),-1));
        V.addAll(findItems(shopPiece,defined));
        V.addAll(findMobs(shopPiece,defined));
        V.addAll(findAbilities(shopPiece,defined));
        return V;
    }
    
    public Vector findItems(XMLLibrary.XMLpiece piece, Hashtable defined) throws CMException
    {
    	Vector V = new Vector();
    	String tagName="ITEM";
        Vector choices = getAllChoices(tagName, piece, defined);
        if((choices==null)||(choices.size()==0)) return V;
        for(int c=0;c<choices.size();c++)
        {
            XMLLibrary.XMLpiece valPiece = (XMLLibrary.XMLpiece)choices.elementAt(c);
            defineReward(valPiece,null,defined);
            try{
	            V.addAll(buildItem(valPiece,defined));
	        }catch(Exception e){}
        }
    	return V;
    }

    protected Vector findContents(XMLLibrary.XMLpiece piece, Hashtable defined) throws CMException
    {
    	Vector V = new Vector();
    	String tagName="CONTENT";
        Vector choices = getAllChoices(tagName, piece, defined);
        if((choices==null)||(choices.size()==0)) return V;
        for(int c=0;c<choices.size();c++)
        {
            XMLLibrary.XMLpiece valPiece = (XMLLibrary.XMLpiece)choices.elementAt(c);
            V.addAll(findItems(valPiece,defined));
        }
    	return V;
    }

    protected Vector buildItem(XMLLibrary.XMLpiece piece, Hashtable defined) throws CMException
    {
        String classID = findString("class",piece,defined);
        Vector contents = new Vector();
        String[] ignoreStats={};
        if(classID.equalsIgnoreCase("metacraft"))
        {
            String recipe = findString("name",piece,defined);
            if((recipe == null)||(recipe.length()==0)) 
            	throw new CMException("Unable to metacraft an item without a name, Data: "+piece.value);
            String materialStr = findOptionalString("material",piece,defined);
            int material=-1;
            if((materialStr!=null)&&(CMParms.containsIgnoreCase(RawMaterial.RESOURCE_DESCS,materialStr)))
            	material=RawMaterial.RESOURCE_DATA[CMParms.indexOfIgnoreCase(RawMaterial.RESOURCE_DESCS,materialStr)][0];
            Vector craftors=new Vector();
			for(Enumeration e=CMClass.abilities();e.hasMoreElements();)
			{
				Ability A=(Ability)e.nextElement();
				if(A instanceof ItemCraftor)
					craftors.addElement(A);
			}
			if(recipe.equalsIgnoreCase("anything"))
			{
				ItemCraftor skill=(ItemCraftor)craftors.elementAt(CMLib.dice().roll(1,craftors.size(),-1));
				if(material>=0)
					contents=skill.craftAllItemsVectors(material);
				else
					contents=skill.craftAllItemsVectors();
				if((contents!=null)&&(contents.size()>0))
					contents=(Vector)contents.elementAt(CMLib.dice().roll(1,contents.size(),-1));
			}
			else
			if(recipe.toLowerCase().startsWith("any-"))
			{
				recipe=recipe.substring(4);
				for(Enumeration e=craftors.elements();e.hasMoreElements();)
				{
					ItemCraftor skill=(ItemCraftor)e.nextElement();
					if(skill.ID().equalsIgnoreCase(recipe))
					{
						if(material>=0)
							contents=skill.craftAllItemsVectors(material);
						else
							contents=skill.craftAllItemsVectors();
					}
				}
				if((contents!=null)&&(contents.size()>0))
					contents=(Vector)contents.elementAt(CMLib.dice().roll(1,contents.size(),-1));
			}
			else
			for(Enumeration e=craftors.elements();e.hasMoreElements();)
			{
				ItemCraftor skill=(ItemCraftor)e.nextElement();
				Vector V=skill.matchingRecipeNames(recipe,false);
				if((V!=null)&&(V.size()>0))
				{
					if(material>=0)
						contents=skill.craftItem(recipe,material);
					else
						contents=skill.craftItem(recipe);
					break;
				}
			}
			if((contents==null)||(contents.size()==0))
	        	throw new CMException("Unable to metacraft an item called '"+recipe+"', Data: "+piece.value);
            addDefinition("ITEM_CLASS",((MOB)contents.firstElement()).ID(),defined);
            ignoreStats=new String[]{"CLASS","NAME"};
        }
        else
        if(classID.equalsIgnoreCase("catalog"))
        {
            String name = findString("name",piece,defined);
            if((name == null)||(name.length()==0)) 
            	throw new CMException("Unable to build a catalog item without a name, Data: "+piece.value);
            Item I = CMLib.catalog().getCatalogItem(name);
            if(I==null)
	        	throw new CMException("Unable to find cataloged item called '"+name+"', Data: "+piece.value);
            I=(Item)I.copyOf();
            CMLib.catalog().changeCatalogUsage(I,true);
            contents.addElement(I);
            addDefinition("ITEM_CLASS",I.ID(),defined);
            ignoreStats=new String[]{"CLASS","NAME"};
        }
        else
        {
	        Item I = CMClass.getItem(classID);
	        if(I == null) throw new CMException("Unable to build item on classID '"+classID+"', Data: "+piece.value);
	        contents.addElement(I);
	        addDefinition("ITEM_CLASS",classID,defined);
	        ignoreStats=new String[]{"CLASS"};
        }
        Item I=(Item)contents.firstElement();
        fillOutStats(I,ignoreStats,"ITEM_",piece,defined);
        CMLib.itemBuilder().balanceItemByLevel(I);
        fillOutStats(I,ignoreStats,"ITEM_",piece,defined);
        
        Vector V;
        if(I instanceof Container)
        {
            V= findContents(piece,defined);
            for(int i=0;i<V.size();i++)
            {
            	Item I2=(Item)V.elementAt(i);
            	I2.setContainer(I);
            	contents.addElement(I2);
            }
        }
        V= findAffects(piece,defined);
        for(int i=0;i<V.size();i++)
        {
        	Ability A=(Ability)V.elementAt(i);
        	I.addNonUninvokableEffect(A);
        }
        V = findBehaviors(piece,defined);
        for(int i=0;i<V.size();i++)
        {
        	Behavior B=(Behavior)V.elementAt(i);
        	I.addBehavior(B);
        }
        I.text();
        I.setMiscText(I.text());
        I.recoverEnvStats();
        return contents;
    }
    
    protected Vector findAffects(XMLLibrary.XMLpiece piece, Hashtable defined) throws CMException
    { return findAbilities("AFFECT",piece,defined);}

    protected Vector findAbilities(XMLLibrary.XMLpiece piece, Hashtable defined) throws CMException
    { return findAbilities("ABILITY",piece,defined);}
    
    protected Vector findAbilities(String tagName, XMLLibrary.XMLpiece piece, Hashtable defined) throws CMException
    {
    	Vector V = new Vector();
        Vector choices = getAllChoices(tagName, piece, defined);
        if((choices==null)||(choices.size()==0)) return V;
        for(int c=0;c<choices.size();c++)
        {
            XMLLibrary.XMLpiece valPiece = (XMLLibrary.XMLpiece)choices.elementAt(c);
            defineReward(valPiece,null,defined);
            try {
	            Ability A=buildAbility(valPiece,defined);
	            V.addElement(A);
	        }catch(Exception e){}
        }
    	return V;
    }

    protected Vector findBehaviors(XMLLibrary.XMLpiece piece, Hashtable defined) throws CMException
    {
    	Vector V = new Vector();
    	String tagName="BEHAVIOR";
        Vector choices = getAllChoices(tagName, piece, defined);
        if((choices==null)||(choices.size()==0)) return V;
        for(int c=0;c<choices.size();c++)
        {
            XMLLibrary.XMLpiece valPiece = (XMLLibrary.XMLpiece)choices.elementAt(c);
            defineReward(valPiece,null,defined);
            try {
	            Behavior B=buildBehavior(valPiece,defined);
	            V.addElement(B);
	        }catch(Exception e){}
        }
    	return V;
    }

    protected Ability buildAbility(XMLLibrary.XMLpiece piece, Hashtable defined) throws CMException
    {
        String classID = findString("class",piece,defined);
        Ability A=CMClass.getAbility(classID);
        if(A == null) throw new CMException("Unable to build ability on classID '"+classID+"', Data: "+piece.value);
        String value = findOptionalString("PARMS",piece,defined);
        if(value != null)
        	A.setMiscText(value);
        return A;
    }
    
    protected Behavior buildBehavior(XMLLibrary.XMLpiece piece, Hashtable defined) throws CMException
    {
        String classID = findString("class",piece,defined);
        Behavior B=CMClass.getBehavior(classID);
        if(B == null) throw new CMException("Unable to build behavior on classID '"+classID+"', Data: "+piece.value);
        String value = findOptionalString("PARMS",piece,defined);
        if(value != null)
        	B.setParms(value);
        return B;
    }
    
    protected void addDefinition(String definition, String value, Hashtable defined) 
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
    
    protected String findOptionalString(String tagName, XMLLibrary.XMLpiece piece, Hashtable defined)
    {
        try {
            return findString(tagName, piece, defined);
        } catch(CMException x) {
            return null;
        }
    }
    
    protected void defineReward(XMLLibrary.XMLpiece piece, String value, Hashtable defined) throws CMException
    {
        String defineString = CMLib.xml().getParmValue(piece.parms,"DEFINE");
        if((defineString!=null)&&(defineString.trim().length()>0))
        {
        	Vector V=CMParms.parseCommas(defineString,true);
        	for(Enumeration e=V.elements();e.hasMoreElements();)
        	{
        		String defVar=(String)e.nextElement();
        		String definition=value;
        		int x=defVar.indexOf('=');
        		if(x==0) continue;
        		if(x>0)
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
        		if(definition==null) definition="!";
        		definition=strFilter(definition,defined);
        		if(CMath.isMathExpression(definition))
        			definition=Integer.toString(CMath.s_parseIntExpression(definition));
        		if(defVar.trim().length()>0)
            		defined.put(defVar.toUpperCase().trim(), definition);
        	}
        }
    }
    
    public String findString(String tagName, XMLLibrary.XMLpiece piece, Hashtable defined) throws CMException
    {
    	tagName=tagName.toUpperCase().trim();
        String asParm = CMLib.xml().getParmValue(piece.parms,tagName);
        if(asParm != null) return strFilter(asParm,defined);
        Vector choices = getAllChoices(tagName, piece, defined);
        if((choices==null)||(choices.size()==0))
            throw new CMException("Unable to find tag '"+tagName+"' on piece '"+piece.tag+"', Data: "+piece.value);
        StringBuffer finalValue = new StringBuffer("");
        
        for(int c=0;c<choices.size();c++)
        {
            XMLLibrary.XMLpiece valPiece = (XMLLibrary.XMLpiece)choices.elementAt(c);
        	String value=strFilter(valPiece.value,defined);
            defineReward(valPiece,value,defined);
            /**
             * Ok, here's the problem: 
             * this is the logical place for a top level selected "define" reward.
             * As the value of the actual piece finally selected and USED for a string.
             * However, by this point, everything has been sub-selected and passed any
             * condition checks that rely ON the reward to inform the decision. No sub-tag
             * can ever really *know* that it will be finally selected, and thus whether to
             * trigger its define cmd until this point and yet it is THIS point that informs
             * the conditions on whether to pick it.  Total catch-22.  
             * 
             * A solution might be to have getAllChoices finally set the define cmd, but that
             * just pushes the problem down one level.  
             */
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
                throw new CMException("Unknown action '"+action+" on subPiece "+valPiece.tag+" on piece '"+piece.tag+"', Data: "+piece.value);
        }
        return finalValue.toString().trim();
    }

    protected Vector getAllChoices(String tagName, XMLLibrary.XMLpiece piece, Hashtable defined) throws CMException
    {
        Vector choices = new Vector();
        String inserter = CMLib.xml().getParmValue(piece.parms,"INSERT");
        if(inserter!=null)
        {
            Vector V=CMParms.parseCommas(inserter,true);
            for(int v=0;v<V.size();v++)
            {
                String s = (String)V.elementAt(v);
        		if(s.startsWith("$")) s=s.substring(1).trim();
                XMLLibrary.XMLpiece insertPiece =(XMLLibrary.XMLpiece)defined.get(s.toUpperCase().trim());
                if(insertPiece == null)
                    throw new CMException("Undefined insert: '"+s+"' on piece '"+piece.tag+"', Data: "+piece.value);
                if(insertPiece.tag.equalsIgnoreCase(tagName))
                    choices.addAll(getAllChoices(tagName,insertPiece,defined));
            }
        }
        else
        if((piece.contents.size()==0)&&(piece.tag.equalsIgnoreCase(tagName)))
        	return CMParms.makeVector(piece);
        
        for(int p=0;p<piece.contents.size();p++)
            if(((XMLLibrary.XMLpiece)piece.contents.elementAt(p)).tag.equalsIgnoreCase(tagName))
                choices.addAll(getAllChoices(tagName,piece.contents.elementAt(p),defined));
        
        Vector finalChoices = new Vector(choices.size());
        for(int c=0;c<choices.size();c++)
        {
            XMLLibrary.XMLpiece lilP =(XMLLibrary.XMLpiece)choices.elementAt(c); 
            if(finalChoices.contains(lilP)) continue;
            if(testCondition(lilP,defined))
            	finalChoices.addElement(lilP);
        }
        return selectChoices(finalChoices,piece,defined);
    }

    protected boolean testCondition(XMLLibrary.XMLpiece piece, Hashtable defined)
    {
        String condition=CMLib.xml().restoreAngleBrackets(CMLib.xml().getParmValue(piece.parms,"CONDITION"));
        try {
            if(condition == null) return true; 
            return CMStrings.parseStringExpression(condition,defined, true);
        } 
        catch(Exception e)
        {
            Log.errOut("Generate",e);
            return false;
        }
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
    
    protected void checkRequirements(Hashtable defined, String requirements) throws CMException
    {
    	if(requirements==null) return;
    	requirements = requirements.trim();
    	Vector reqs = CMParms.parseCommas(requirements,true);
    	for(int r=0;r<reqs.size();r++)
    	{
    		String reqVariable=(String)reqs.elementAt(r);
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
    
    public void checkRequirements(XMLLibrary.XMLpiece piece, Hashtable defined) throws CMException
    {
        checkRequirements(defined,CMLib.xml().getParmValue(piece.parms,"REQUIRES"));
    }
    
    protected Vector selectChoices(Vector choices, XMLLibrary.XMLpiece piece, Hashtable defined) throws CMException
    {
        String selection = CMLib.xml().getParmValue(piece.parms,"SELECT");
        if(selection == null) return choices;
        selection=selection.toUpperCase().trim();
        Vector selectedChoicesV=null;
        if(selection.equals("NONE")) 
        	selectedChoicesV= new Vector();
        else
        if(choices.size()==0) 
        	throw new CMException("Can't make selection among NONE: on piece '"+piece.tag+"', Data: "+piece.value);
        else
        if(selection.equals("ALL")) 
        	selectedChoicesV=choices;
        else
        if(selection.equals("FIRST"))
        	selectedChoicesV= CMParms.makeVector(choices.firstElement());
        else
        if(selection.startsWith("FIRST-"))
        {
            int num=CMath.parseIntExpression(selection.substring(selection.indexOf('-')+1));
            if((num<=0)||(num>choices.size())) throw new CMException("Can't pick first "+num+" of "+choices.size()+" on piece '"+piece.tag+"', Data: "+piece.value);
            selectedChoicesV=new Vector();
            for(int v=0;v<num;v++)
            	selectedChoicesV.addElement(choices.elementAt(v));
        }
        else
        if(selection.equals("LAST"))  
        	selectedChoicesV=CMParms.makeVector(choices.lastElement());
        else
        if(selection.startsWith("LAST-"))
        {
            int num=CMath.parseIntExpression(selection.substring(selection.indexOf('-')+1));
            if((num<=0)||(num>choices.size())) throw new CMException("Can't pick last "+num+" of "+choices.size()+" on piece '"+piece.tag+"', Data: "+piece.value);
            selectedChoicesV=new Vector();
            for(int v=choices.size()-num;v<choices.size();v++)
            	selectedChoicesV.addElement(choices.elementAt(v));
        }
        else
        if(selection.startsWith("PICK-"))
        {
            int num=CMath.parseIntExpression(selection.substring(selection.indexOf('-')+1));
            if((num<=0)||(num>choices.size())) throw new CMException("Can't pick "+num+" of "+choices.size()+" on piece '"+piece.tag+"', Data: "+piece.value);
            selectedChoicesV=new Vector();
            Vector cV=(Vector)choices.clone();
            for(int v=0;v<num;v++)
            {
                int[] weights=new int[cV.size()];
                int total=0;
                for(int c=0;c<cV.size();c++)
                {
                    XMLLibrary.XMLpiece lilP=(XMLLibrary.XMLpiece)cV.elementAt(c);
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
                selectedChoicesV.addElement(cV.elementAt(c));
                cV.removeElementAt(c);
            }
        }
        else
        if(selection.equals("ANY"))
        	selectedChoicesV=CMParms.makeVector(choices.elementAt(CMLib.dice().roll(1,choices.size(),-1)));
        else
        if(selection.startsWith("ANY-"))
        {
            int num=CMath.parseIntExpression(selection.substring(selection.indexOf('-')+1));
            if((num<=0)||(num>choices.size())) throw new CMException("Can't pick last "+num+" of "+choices.size()+" on piece '"+piece.tag+"', Data: "+piece.value);
            selectedChoicesV=new Vector();
            Vector cV=(Vector)choices.clone();
            for(int v=0;v<num;v++)
            {
                int x=CMLib.dice().roll(1,cV.size(),-1);
                selectedChoicesV.addElement(cV.elementAt(x));
                cV.removeElementAt(x);
            }
        }
        else
	        throw new CMException("Illegal select type '"+selection+"' on piece '"+piece.tag+"', Data: "+piece.value);
        return selectedChoicesV;
    }
    
    protected String strFilter(String str, Hashtable defined) throws CMException
    {
    	int x=str.indexOf('$');
    	while((x>=0)&&(x<str.length()-1))
    	{
    		int start=x;
    		x++;
    		while((x<str.length())&&((str.charAt(x)=='_')||Character.isLetterOrDigit(str.charAt(x))))
    			x++;
    		String var = str.substring(start+1,x);
    		Object val = defined.get(var.toUpperCase().trim());
    		if(val instanceof XMLLibrary.XMLpiece) 
    			val = findString("STRING",(XMLLibrary.XMLpiece)val,defined);
    		if(val == null) throw new CMException("Unknown variable '$"+var+"' in str '"+str+"'");
    		str=str.substring(0,start)+val.toString()+str.substring(x);
    		x=str.indexOf('$');
    	}
    	x=str.toLowerCase().indexOf("(a(n))");
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
}
