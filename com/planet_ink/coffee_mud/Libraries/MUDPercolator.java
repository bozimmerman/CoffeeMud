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
    
    public void buildDefinedTagSet(Vector xmlRoot, Hashtable defined)
    {
        if(xmlRoot==null) return;
        for(int v=0;v<xmlRoot.size();v++)
        {
            XMLLibrary.XMLpiece piece = (XMLLibrary.XMLpiece)xmlRoot.elementAt(v);
            String tag = CMLib.xml().getParmValue(piece.parms,"TAG");
            if((tag!=null)&&(tag.length()>0))
                defined.put(tag.toUpperCase().trim(),piece);
            buildDefinedTagSet(piece.contents,defined);
        }
    }
    
    public boolean activate()
    { 
        String filePath="com/planet_ink/coffee_mud/Libraries/layouts";
        CMProps page = CMProps.instance();
        Vector layouts=CMClass.loadClassList(filePath,page.getStr("LIBRARY"),"/layouts",LayoutManager.class,true);
    	for(int f=0;f<layouts.size();f++)
    	{
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
    
    // tags created: ROOM_CLASS, ROOM_TITLE, ROOM_DESCRIPTION, ROOM_CLASSES, ROOM_TITLES, ROOM_DESCRIPTIONS
    public Room buildRoom(XMLLibrary.XMLpiece piece, Hashtable defined, int direction) throws CMException
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
        Vector V;
        V = findMobs(piece,defined);
        for(int i=0;i<V.size();i++)
        {
        	MOB M=(MOB)V.elementAt(i);
        	R.bringMobHere(M,false);
        }
        V = findItems(piece,defined);
        for(int i=0;i<V.size();i++)
        {
        	Item I=(Item)V.elementAt(i);
        	R.bringItemHere(I, 0,false);
        }
        V = findAffects(piece,defined);
        for(int i=0;i<V.size();i++)
        {
        	Ability A=(Ability)V.elementAt(i);
        	R.addNonUninvokableEffect(A);
        }
        V = findBehaviors(piece,defined);
        for(int i=0;i<V.size();i++)
        {
        	Behavior B=(Behavior)V.elementAt(i);
        	R.addBehavior(B);
        }
        return R;
    }
    
    // tags created: LINK_DIRECTION, AREA_CLASS, AREA_NAME, AREA_DESCRIPTION, AREA_LAYOUT, AREA_SIZE
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
        String description = findString("description",piece,defined);
        A.setDescription(description);
        defined.put("AREA_DESCRIPTION",description);
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
        while(roomsLayout.size() > 0)
        {
        	//TODO: the group breaking.
        }
        // make CERTAIN that the magic first room in the layout always
        // gets ID#0.
        for(int g=0;g<roomGroups.size();g++)
        {
        	Vector<LayoutNode> group=(Vector<LayoutNode>)roomGroups.elementAt(g);
        	if(group.contains(magicRoom))
        	{
        		if((group.size()>1)&&(group.firstElement() != magicRoom))
        		{
	        		group.remove(magicRoom);
	        		group.insertElementAt(magicRoom,0);
        		}
        		if((roomGroups.size()>1)&&(roomGroups.firstElement() != group))
        		{
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
        		for(String key : node.tags().keySet())
        			groupDefined.put("ROOMTAG_"+key.toUpperCase().trim(),node.tags().get(key));
        		Room R=buildRoom(piece, groupDefined, direction);
        		R.setRoomID(A.getNewRoomID(null,-1));
        		R.setArea(A);
        		A.addProperRoom(R);
        		for(String key : node.tags().keySet())
        			groupDefined.remove("ROOMTAG_"+key.toUpperCase().trim());
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
        choices = selectChoices(choices,piece,defined);
        for(int c=0;c<choices.size();c++)
        {
            XMLLibrary.XMLpiece valPiece = (XMLLibrary.XMLpiece)choices.elementAt(c);
            MOB M=buildMob(valPiece,defined);
            V.addElement(M);
        }
    	return V;
    }
    
    private MOB buildMob(XMLLibrary.XMLpiece piece, Hashtable defined) throws CMException
    {
        String classID = findString("class",piece,defined);
        MOB M = CMClass.getMOB(classID);
        if(M == null) throw new CMException("Unable to build mob on classID '"+classID+"', Data: "+piece.value);
        addDefinition("MOB_CLASS",classID,defined);
        String[] ignoreTags={"CLASS"};
        String[] statCodes = M.getStatCodes();
        for(int s=0;s<statCodes.length;s++)
        {
        	String stat=statCodes[s];
        	if(!CMParms.contains(ignoreTags, stat))
        	{
	            String value = findOptionalString(stat,piece,defined);
	            if(value != null)
	            {
	            	M.setStat(stat, value);
		            addDefinition("MOB_"+stat,value,defined);
	            }
        	}
        }
        Vector items = findItems(piece,defined);
        for(int i=0;i<items.size();i++)
        {
        	Item I=(Item)items.elementAt(i);
        	M.addInventory(I);
        	I.wearIfPossible(M);
        }
        Vector V = findAffects(piece,defined);
        for(int i=0;i<V.size();i++)
        {
        	Ability A=(Ability)V.elementAt(i);
        	M.addNonUninvokableEffect(A);
        }
        V = findBehaviors(piece,defined);
        for(int i=0;i<V.size();i++)
        {
        	Behavior B=(Behavior)V.elementAt(i);
        	M.addBehavior(B);
        }
        V = findAbilities(piece,defined);
        for(int i=0;i<V.size();i++)
        {
        	Ability A=(Ability)V.elementAt(i);
        	M.addAbility(A);
        }
        M.text();
        M.setMiscText(M.text());
        M.recoverCharStats();
        M.recoverEnvStats();
        M.recoverMaxState();
        return M;
    }
    
    public Vector findItems(XMLLibrary.XMLpiece piece, Hashtable defined) throws CMException
    {
    	Vector V = new Vector();
    	String tagName="ITEM";
        Vector choices = getAllChoices(tagName, piece, defined);
        if((choices==null)||(choices.size()==0)) return V;
        choices = selectChoices(choices,piece,defined);
        for(int c=0;c<choices.size();c++)
        {
            XMLLibrary.XMLpiece valPiece = (XMLLibrary.XMLpiece)choices.elementAt(c);
            V.addAll(buildItem(valPiece,defined));
        }
    	return V;
    }

    private Vector findContents(XMLLibrary.XMLpiece piece, Hashtable defined) throws CMException
    {
    	Vector V = new Vector();
    	String tagName="CONTENT";
        Vector choices = getAllChoices(tagName, piece, defined);
        if((choices==null)||(choices.size()==0)) return V;
        choices = selectChoices(choices,piece,defined);
        for(int c=0;c<choices.size();c++)
        {
            XMLLibrary.XMLpiece valPiece = (XMLLibrary.XMLpiece)choices.elementAt(c);
            V.addAll(findItems(valPiece,defined));
        }
    	return V;
    }

    private Vector buildItem(XMLLibrary.XMLpiece piece, Hashtable defined) throws CMException
    {
        String classID = findString("class",piece,defined);
        Item I = CMClass.getItem(classID);
        if(I == null) throw new CMException("Unable to build item on classID '"+classID+"', Data: "+piece.value);
        addDefinition("ITEM_CLASS",classID,defined);
        String[] ignoreTags={"CLASS"};
        String[] statCodes = I.getStatCodes();
        for(int s=0;s<statCodes.length;s++)
        {
        	String stat=statCodes[s];
        	if(!CMParms.contains(ignoreTags, stat))
        	{
	            String value = findOptionalString(stat,piece,defined);
	            if(value != null)
	            {
	            	I.setStat(stat, value);
		            addDefinition("ITEM_"+stat,value,defined);
	            }
        	}
        }
        Vector contents = new Vector();
        contents.addElement(I);
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
    
    private Vector findAffects(XMLLibrary.XMLpiece piece, Hashtable defined) throws CMException
    { return findAbilities("AFFECT",piece,defined);}

    private Vector findAbilities(XMLLibrary.XMLpiece piece, Hashtable defined) throws CMException
    { return findAbilities("ABILITY",piece,defined);}
    
    private Vector findAbilities(String tagName, XMLLibrary.XMLpiece piece, Hashtable defined) throws CMException
    {
    	Vector V = new Vector();
        Vector choices = getAllChoices(tagName, piece, defined);
        if((choices==null)||(choices.size()==0)) return V;
        choices = selectChoices(choices,piece,defined);
        for(int c=0;c<choices.size();c++)
        {
            XMLLibrary.XMLpiece valPiece = (XMLLibrary.XMLpiece)choices.elementAt(c);
            Ability A=buildAbility(valPiece,defined);
            V.addElement(A);
        }
    	return V;
    }

    private Vector findBehaviors(XMLLibrary.XMLpiece piece, Hashtable defined) throws CMException
    {
    	Vector V = new Vector();
    	String tagName="BEHAVIOR";
        Vector choices = getAllChoices(tagName, piece, defined);
        if((choices==null)||(choices.size()==0)) return V;
        choices = selectChoices(choices,piece,defined);
        for(int c=0;c<choices.size();c++)
        {
            XMLLibrary.XMLpiece valPiece = (XMLLibrary.XMLpiece)choices.elementAt(c);
            Behavior B=buildBehavior(valPiece,defined);
            V.addElement(B);
        }
    	return V;
    }

    private Ability buildAbility(XMLLibrary.XMLpiece piece, Hashtable defined) throws CMException
    {
        String classID = findString("class",piece,defined);
        Ability A=CMClass.getAbility(classID);
        if(A == null) throw new CMException("Unable to build ability on classID '"+classID+"', Data: "+piece.value);
        String value = findOptionalString("PARMS",piece,defined);
        if(value != null)
        	A.setMiscText(value);
        return A;
    }
    
    private Behavior buildBehavior(XMLLibrary.XMLpiece piece, Hashtable defined) throws CMException
    {
        String classID = findString("class",piece,defined);
        Behavior B=CMClass.getBehavior(classID);
        if(B == null) throw new CMException("Unable to build behavior on classID '"+classID+"', Data: "+piece.value);
        String value = findOptionalString("PARMS",piece,defined);
        if(value != null)
        	B.setParms(value);
        return B;
    }
    
    private void addDefinition(String definition, String value, Hashtable defined) 
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
    
    private String findOptionalString(String tagName, XMLLibrary.XMLpiece piece, Hashtable defined)
    {
        try {
            return findString(tagName, piece, defined);
        } catch(CMException x) {
            return null;
        }
    }
    
    public String findString(String tagName, XMLLibrary.XMLpiece piece, Hashtable defined) throws CMException
    {
        String asParm = CMLib.xml().getParmValue(piece.parms,tagName.toUpperCase().trim());
        if(asParm != null) return strFilter(asParm,defined);
        Vector choices = getAllChoices(tagName, piece, defined);
        if((choices==null)||(choices.size()==0)) {
            if((piece.tag.equalsIgnoreCase(tagName))&&(piece.contents.size()==0))
                return piece.value;
            throw new CMException("Unable to find tag '"+tagName+"' on piece '"+piece.tag+"', Data: "+piece.value);
        }
        choices = selectChoices(choices,piece,defined);
        StringBuffer finalValue = new StringBuffer("");
        for(int c=0;c<choices.size();c++)
        {
            XMLLibrary.XMLpiece valPiece = (XMLLibrary.XMLpiece)choices.elementAt(c);
            String value = findString("VALUE",valPiece,defined);
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

    private void checkRequirements(Hashtable defined, String requirements) throws CMException
    {
    	if(requirements==null) return;
    	requirements = requirements.trim();
    	Vector reqs = CMParms.parseCommas(requirements,true);
    	for(int r=0;r<reqs.size();r++)
    	{
    		String req=(String)reqs.elementAt(r);
    		if(req.startsWith("$")) req=req.substring(1).trim();
    		if((!defined.containsKey(req))&&(!defined.containsKey(req.toUpperCase())))
    			throw new CMException("Required variable not defined: '"+req+"'.  Please define this variable.");
    	}
    }
    
    private Vector getAllChoices(String tagName, XMLLibrary.XMLpiece piece, Hashtable defined) throws CMException
    {
        Vector choices = new Vector();
        for(int p=0;p<piece.contents.size();p++)
            if(((XMLLibrary.XMLpiece)piece.contents.elementAt(p)).tag.equalsIgnoreCase(tagName))
                choices.addElement(piece.contents.elementAt(p));
        String inserter = CMLib.xml().getParmValue(piece.parms,"INSERT");
        if(inserter!=null)
        {
            Vector V=CMParms.parseCommas(inserter,true);
            for(int v=0;v<V.size();v++)
            {
                String s = (String)V.elementAt(v);
                if(!s.startsWith("$"))
                    throw new CMException("Insert missing $: '"+s+"' on piece '"+piece.tag+"', Data: "+piece.value);
                XMLLibrary.XMLpiece insertPiece =(XMLLibrary.XMLpiece)defined.get(s.substring(1).toUpperCase().trim());
                if(insertPiece == null)
                    throw new CMException("Undefined insert: '"+s+"' on piece '"+piece.tag+"', Data: "+piece.value);
                if(insertPiece.contents.size()==0)
                {
                    if(insertPiece.tag.equalsIgnoreCase(tagName))
                        choices.addElement(insertPiece);
                    else
                        throw new CMException("Can't insert: '"+insertPiece.tag+"' as '"+tagName+"' on piece '"+piece.tag+"', Data: "+piece.value);
                }
                for(int i=0;i<insertPiece.contents.size();i++)
                {
                    XMLLibrary.XMLpiece insertPieceChild =(XMLLibrary.XMLpiece)insertPiece.contents.elementAt(i);
                    if(insertPieceChild.tag.equalsIgnoreCase(tagName))
                        choices.addElement(insertPieceChild);
                    else
                        throw new CMException("Can't insert: '"+insertPieceChild.tag+"' as '"+tagName+"' on piece '"+piece.tag+"', Data: "+piece.value);
                }
            }
        }
        for(int c=0;c<choices.size();c++)
        {
            XMLLibrary.XMLpiece lilP =(XMLLibrary.XMLpiece)choices.elementAt(c); 
            checkRequirements(defined,CMLib.xml().getParmValue(lilP.parms,"REQUIRES"));
            String condition=CMLib.xml().getParmValue(lilP.parms,"CONDITION");
            try {
	            if((condition != null) && (!CMStrings.parseStringExpression(condition,defined, true)))
	            {
	                choices.removeElementAt(c);
	                c--;
	            }
            } 
            catch(Exception e)
            {
                choices.removeElementAt(c);
                c--;
                Log.errOut("Generate",e);
            }
        }
        return choices;
    }

    public void checkRequirements(XMLLibrary.XMLpiece piece, Hashtable defined) throws CMException
    {
        checkRequirements(defined,CMLib.xml().getParmValue(piece.parms,"REQUIRES"));
    }
    
    private Vector selectChoices(Vector choices, XMLLibrary.XMLpiece piece, Hashtable defined) throws CMException
    {
        String selection = CMLib.xml().getParmValue(piece.parms,"SELECT");
        if(selection == null) return choices;
        selection=selection.toUpperCase().trim();
        if(selection.equals("NONE"))  return new Vector();
        if(choices.size()==0) throw new CMException("Can't make selection among NONE: on piece '"+piece.tag+"', Data: "+piece.value);
        if(selection.equals("ALL"))  return choices;
        if(selection.equals("FIRST"))  return CMParms.makeVector(choices.firstElement());
        if(selection.startsWith("FIRST-"))
        {
            int num=CMath.s_int(selection.substring(selection.indexOf('-')+1));
            if((num<=0)||(num>choices.size())) throw new CMException("Can't pick first "+num+" of "+choices.size()+" on piece '"+piece.tag+"', Data: "+piece.value);
            Vector V=new Vector();
            for(int v=0;v<num;v++)
                V.addElement(choices.elementAt(v));
            return V;
        }
        if(selection.equals("LAST"))  return CMParms.makeVector(choices.lastElement());
        if(selection.startsWith("LAST-"))
        {
            int num=CMath.s_int(selection.substring(selection.indexOf('-')+1));
            if((num<=0)||(num>choices.size())) throw new CMException("Can't pick last "+num+" of "+choices.size()+" on piece '"+piece.tag+"', Data: "+piece.value);
            Vector V=new Vector();
            for(int v=V.size()-num;v<V.size();v++)
                V.addElement(choices.elementAt(v));
            return V;
        }
        if(selection.startsWith("PICK-"))
        {
            int num=CMath.s_int(selection.substring(selection.indexOf('-')+1));
            if((num<=0)||(num>choices.size())) throw new CMException("Can't pick "+num+" of "+choices.size()+" on piece '"+piece.tag+"', Data: "+piece.value);
            Vector V=new Vector();
            Vector cV=(Vector)choices.clone();
            for(int v=0;v<num;v++)
            {
                int[] weights=new int[cV.size()];
                int total=0;
                for(int c=0;c<cV.size();c++)
                {
                    XMLLibrary.XMLpiece lilP=(XMLLibrary.XMLpiece)cV.elementAt(c);
                    int weight=CMath.s_int(CMLib.xml().getParmValue(lilP.parms,"WEIGHT"));
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
                V.addElement(cV.elementAt(c));
                cV.removeElementAt(c);
            }
            return V;
        }
        if(selection.equals("ANY"))    return CMParms.makeVector(choices.elementAt(CMLib.dice().roll(1,choices.size(),-1)));
        if(selection.startsWith("ANY-"))
        {
            int num=CMath.s_int(selection.substring(selection.indexOf('-')+1));
            if((num<=0)||(num>choices.size())) throw new CMException("Can't pick last "+num+" of "+choices.size()+" on piece '"+piece.tag+"', Data: "+piece.value);
            Vector V=new Vector();
            Vector cV=(Vector)choices.clone();
            for(int v=0;v<num;v++)
            {
                int x=CMLib.dice().roll(1,cV.size(),-1);
                V.addElement(cV.elementAt(x));
                cV.removeElementAt(x);
            }
            return V;
        }
        throw new CMException("Illegal select type '"+selection+"' on piece '"+piece.tag+"', Data: "+piece.value);
    }
    
    private String strFilter(String str, Hashtable defined) throws CMException
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
    		if(val instanceof XMLLibrary.XMLpiece) val = findString(((XMLLibrary.XMLpiece)val).tag,(XMLLibrary.XMLpiece)val,defined);
    		if(val == null) throw new CMException("Unknown variable '$"+var+"' in str '"+str+"'");
    		str=str.substring(0,start)+val.toString()+str.substring(x);
    	}
        return str;
    }
}
