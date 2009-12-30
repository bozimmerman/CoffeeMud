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
import com.planet_ink.coffee_mud.core.exceptions.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.CMLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import com.planet_ink.coffee_mud.WebMacros.interfaces.WebMacro;

import java.util.*;

/* 
   Copyright 2000-2010 Bo Zimmerman

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
public class Generate extends StdCommand
{
    public Generate(){}
    private static final Hashtable OBJECT_TYPES=CMParms.makeHashtable(new Object[][]{
    		{"STRING",Integer.valueOf(Integer.MAX_VALUE)},
    		{"AREA",Integer.valueOf(CMClass.OBJECT_AREA)},
    		{"MOB",Integer.valueOf(CMClass.OBJECT_MOB)},
    		{"ROOM",Integer.valueOf(CMClass.OBJECT_LOCALE)},
    		{"ITEM",Integer.valueOf(CMClass.OBJECT_ITEM)},
    });

    private String[] access={"GENERATE"};
    public String[] getAccessWords(){return access;}

    public void createNewPlace(MOB mob, Room oldR, Room R, int direction) {
    	Exit E=R.getExitInDir(Directions.getOpDirectionCode(direction));
    	if(E==null) E = CMClass.getExit("Open");
		oldR.setRawExit(direction, E);
		oldR.rawDoors()[direction]=R;
		int opDir=Directions.getOpDirectionCode(direction);
		if(R.getRoomInDir(opDir)!=null)
			mob.tell("An error has caused the following exit to be one-way.");
		else
		{
			R.setRawExit(opDir, E);
			R.rawDoors()[opDir]=oldR;
		}
		CMLib.database().DBUpdateExits(oldR);
		oldR.showHappens(CMMsg.MSG_OK_VISUAL,"A new place materializes to the "+Directions.getDirectionName(direction));
    }
    
    public boolean execute(MOB mob, Vector commands, int metaFlags)
        throws java.io.IOException
    {
        if(commands.size()<3)
        {
        	mob.tell("Generate what? Try GENERATE [TYPE] [ID] (FROM [DATA_FILE_PATH]) ([VAR=VALUE]..) [DIRECTION]");
        	return false;
        }
        String finalLog = mob.Name()+" called generate command with parms: " + CMParms.combine(commands, 1);
        CMFile file = null;
        if((commands.size()>3)&&((String)commands.elementAt(3)).equalsIgnoreCase("FROM"))
		{
        	file = new CMFile(Resources.buildResourcePath((String)commands.elementAt(4)),mob,false);	
        	commands.removeElementAt(3);
        	commands.removeElementAt(3);
		}
        else
        	file = new CMFile(Resources.buildResourcePath("examples/randomdata.xml"),mob,false);
        if(!file.canRead())
        {
            mob.tell("Random data file '"+file.getCanonicalPath()+"' not found.  Aborting.");
            return false;
        }
        StringBuffer xml = file.textUnformatted();
        Vector xmlRoot = CMLib.xml().parseAllXML(xml);
        Hashtable definedIDs = new Hashtable();
        CMLib.percolator().buildDefinedIDSet(xmlRoot,definedIDs);
        String typeName = (String)commands.elementAt(1);
        String objectType = typeName.toUpperCase().trim();
        Integer codeI=(Integer)OBJECT_TYPES.get(objectType);
        if(codeI==null)
        {
        	for(Enumeration e=OBJECT_TYPES.keys();e.hasMoreElements();)
        	{
        		String key =(String)e.nextElement(); 
        		if(key.startsWith(typeName.toUpperCase().trim()))
        		{
        			objectType = key;
        			codeI=(Integer)OBJECT_TYPES.get(key);
        		}
        	}
        	if(codeI==null)
        	{
        		mob.tell("'"+typeName+"' is an unknown object type.  Try: "+CMParms.toStringList(OBJECT_TYPES.keys()));
        		return false;
        	}
        }
        int direction=-1;
        if((codeI.intValue()==CMClass.OBJECT_AREA)||(codeI.intValue()==CMClass.OBJECT_LOCALE))
        {
        	String possDir=(String)commands.lastElement();
        	direction = Directions.getGoodDirectionCode(possDir);
        	if(direction<0)
        	{
        		mob.tell("When creating an area or room, the LAST parameter to this command must be a direction to link to this room by.");
        		return false;
        	}
        	if(mob.location().getRoomInDir(direction)!=null) 
        	{
        		mob.tell("A room already exists in direction "+Directions.getDirectionName(direction)+". Action aborted.");
        		return false;
        	}
        }
        String idName = ((String)commands.elementAt(2)).toUpperCase().trim();
        if((!(definedIDs.get(idName) instanceof XMLLibrary.XMLpiece))
        ||(!((XMLLibrary.XMLpiece)definedIDs.get(idName)).tag.equalsIgnoreCase(objectType)))
        {
        	mob.tell("The "+objectType+" id '"+idName+"' has not been defined in the data file.");
        	StringBuffer foundIDs=new StringBuffer("");
        	for(Enumeration tkeye=OBJECT_TYPES.keys();tkeye.hasMoreElements();)
        	{
        		String tKey=(String)tkeye.nextElement();
        		foundIDs.append("^H"+tKey+"^N: \n\r");
            	Vector xmlTagsV=new Vector();
	        	for(Enumeration keys=definedIDs.keys();keys.hasMoreElements();)
	        	{
	        		String key=(String)keys.nextElement();
	                if((definedIDs.get(key) instanceof XMLLibrary.XMLpiece)
	                &&(((XMLLibrary.XMLpiece)definedIDs.get(key)).tag.equalsIgnoreCase(tKey)))
	                	xmlTagsV.addElement(key.toLowerCase());
	        	}
	        	foundIDs.append(CMParms.toStringList(xmlTagsV)+"\n\r");
        	}
        	mob.tell("Found ids include: \n\r"+foundIDs.toString());
        	return false;
        }
        
        XMLLibrary.XMLpiece piece=(XMLLibrary.XMLpiece)definedIDs.get(idName);
        definedIDs.putAll(CMParms.parseEQParms(commands,3,commands.size()));
        try 
        {
        	CMLib.percolator().checkRequirements(piece, definedIDs);
        } 
        catch(CMException cme) 
        {
        	mob.tell("Required ids for "+idName+" were missing: "+cme.getMessage());
        	return false;
        }
        Vector V = new Vector();
        try{
	        switch(codeI.intValue())
	        {
	        case Integer.MAX_VALUE:
	        {
	        	String s=CMLib.percolator().findString("STRING", piece, definedIDs);
	        	if(s!=null)
	        		V.addElement(s);
	        	break;
	        }
	    	case CMClass.OBJECT_AREA:
	    		Area A=CMLib.percolator().buildArea(piece, definedIDs, direction);
	    		if(A!=null)
	    			V.addElement(A);
	    		break;
			case CMClass.OBJECT_MOB:
				V=CMLib.percolator().findMobs(piece, definedIDs);
				break;
			case CMClass.OBJECT_LOCALE:
			{
        		Exit[] exits=new Exit[Directions.NUM_DIRECTIONS()];
				Room R=CMLib.percolator().buildRoom(piece, definedIDs, exits, direction);
				if(R!=null)
					V.addElement(R);
				break;
			}
			case CMClass.OBJECT_ITEM:
				V=CMLib.percolator().findItems(piece, definedIDs);
				break;
	        }
        } catch(CMException cex) {
        	mob.tell("Unable to generate: "+cex.getMessage());
        	if(CMSecurity.isDebugging("MUDPERCOLATOR"))
	        	Log.debugOut("Generate",cex);
        	return false;
        }
        if(V.size()==0)
        	mob.tell("Nothing generated.");
        else
        for(int v=0;v<V.size();v++)
        	if(V.elementAt(v) instanceof MOB)
        	{
        		((MOB)V.elementAt(v)).bringToLife(mob.location(),true);
        		mob.location().showHappens(CMMsg.MSG_OK_VISUAL,((MOB)V.elementAt(v)).name()+" appears.");
        		Log.sysOut("Generate",mob.Name()+" generated mob "+((MOB)V.elementAt(v)).name());
        	}
        	else
        	if(V.elementAt(v) instanceof Item)
        	{
        		mob.location().addItem((Item)V.elementAt(v));
        		mob.location().showHappens(CMMsg.MSG_OK_VISUAL,((Item)V.elementAt(v)).name()+" appears.");
        		Log.sysOut("Generate",mob.Name()+" generated item "+((Item)V.elementAt(v)).name());
        	}
        	else
        	if(V.elementAt(v) instanceof String)
        		mob.tell((String)V.elementAt(v));
        	else
        	if(V.elementAt(v) instanceof Room)
        	{
        		Room R=(Room)V.elementAt(v);
        		createNewPlace(mob,mob.location(),R,direction);
	    		Log.sysOut("Generate",mob.Name()+" generated room "+R.roomID());
        	}
        	else
        	if(V.elementAt(v) instanceof Area)
        	{
        		Area A=(Area)V.elementAt(v);
    			CMLib.map().addArea(A);
        		CMLib.database().DBCreateArea(A);
        		Room R=A.getRoom(A.Name()+"#0");
        		if(R==null) R=(Room)A.getFilledProperMap().nextElement();
        		createNewPlace(mob,mob.location(),R,direction);
        		mob.tell("Saving remaining rooms for area '"+A.name()+"'...");
        		for(Enumeration e=A.getFilledProperMap();e.hasMoreElements();)
        		{
        			R=(Room)e.nextElement();
    				CMLib.database().DBCreateRoom(R);
    				CMLib.database().DBUpdateExits(R);
    				CMLib.database().DBUpdateItems(R);
    				CMLib.database().DBUpdateMOBs(R);
        		}
        		mob.tell("Done saving remaining rooms for area '"+A.name()+"'");
	    		Log.sysOut("Generate",mob.Name()+" generated area "+A.name());
        	}
		Log.sysOut("Generate",finalLog);
        return true;
    }
    
    public boolean canBeOrdered(){return false;}

    public boolean securityCheck(MOB mob){return CMSecurity.isAllowedAnywhere(mob,"CMDAREAS");}
}
