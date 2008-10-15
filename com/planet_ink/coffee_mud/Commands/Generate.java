package com.planet_ink.coffee_mud.Commands;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

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
public class Generate extends StdCommand
{
    public Generate(){}

    private String[] access={"GENERATE"};
    public String[] getAccessWords(){return access;}
    public boolean execute(MOB mob, Vector commands, int metaFlags)
        throws java.io.IOException
    {
        CMFile file = new CMFile(CMLib.resources().buildResourcePath("randomdata.xml"),mob,false);
        if(!file.canRead())
        {
            mob.tell("Can't comply. '"+file.getCanonicalPath()+"' not found.");
            return false;
        }
        StringBuffer xml = file.textUnformatted();
        Vector xmlRoot = CMLib.xml().parseAllXML(xml);
        Hashtable definedTags = new Hashtable();
        buildTagSet(xmlRoot,definedTags);
        mob.tell("Not yet implemented");
        return false;
    }
    
    private void buildTagSet(Vector xmlRoot, Hashtable defined)
    {
        if(xmlRoot==null) return;
        for(int v=0;v<xmlRoot.size();v++)
        {
            XMLLibrary.XMLpiece piece = (XMLLibrary.XMLpiece)xmlRoot.elementAt(v);
            String tag = CMLib.xml().getParmValue(piece.parms,"TAG");
            if((tag!=null)&&(tag.length()>0))
                defined.put(tag.toUpperCase().trim(),piece);
            buildTagSet(piece.contents,defined);
        }
    }
    
    private Room buildRoom(XMLLibrary.XMLpiece piece, Hashtable defined) throws CMException
    {
        String classID = findString("class",piece,defined);
        Room R = CMClass.getLocale(classID);
        if(R == null) throw new CMException("Unable to build room on classID '"+classID+"', Data: "+piece.value);
        String title = findString("title",piece,defined);
        String description = findString("description",piece,defined);
        R.setDisplayText(title);
        R.setDescription(description);
        //TODO: items, mobs, behaviors, affects
        return R;
    }
    
    private String findOptionalString(String tagName, XMLLibrary.XMLpiece piece, Hashtable defined)
    {
        try {
            return findString(tagName, piece, defined);
        } catch(CMException x) {
            return null;
        }
    }
    
    private String findString(String tagName, XMLLibrary.XMLpiece piece, Hashtable defined) throws CMException
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
            String condition=CMLib.xml().getParmValue(lilP.parms,"CONDITION");
            if((condition != null) && (!CMStrings.parseStringExpression(condition.toCharArray(),0,defined)))
            {
                choices.removeElementAt(c);
                c--;
            }
        }
        return choices;
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
        //TODO: something here
        return str;
    }
    
    public boolean canBeOrdered(){return false;}

    public boolean securityCheck(MOB mob){return CMSecurity.isAllowedAnywhere(mob,"CMDAREAS");}
}
