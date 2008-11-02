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
import com.planet_ink.coffee_mud.Libraries.interfaces.CMLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import com.planet_ink.coffee_mud.WebMacros.interfaces.WebMacro;

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
    public boolean execute(MOB mob, Vector commands, int metaFlags)
        throws java.io.IOException
    {
    	if(true)
    	{
            mob.tell("Not yet implemented");
            return false;
    	}
        CMFile file = new CMFile(Resources.buildResourcePath("randomdata.xml"),mob,false);
        if(!file.canRead())
        {
            mob.tell("Can't comply. '"+file.getCanonicalPath()+"' not found.");
            return false;
        }
        StringBuffer xml = file.textUnformatted();
        Vector xmlRoot = CMLib.xml().parseAllXML(xml);
        Hashtable definedTags = new Hashtable();
        CMLib.percolator().buildDefinedTagSet(xmlRoot,definedTags);
        if(commands.size()<3)
        {
        	mob.tell("Generate what? Try GENERATE [TYPE] [TAG_NAME] ([TAG_PARMS]..)");
        	return false;
        }
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
        //int objectTypeIndex = CMParms.indexOf(OBJECT_TYPES.keys(),objectType);
        String tagName = ((String)commands.elementAt(2)).toUpperCase().trim();
        if(!definedTags.contains(tagName))
        {
        	mob.tell("No tag called '"+tagName+"' has been defined in the /resources/randomdata.xml file.");
        	mob.tell("Found tags include: "+CMParms.toStringList(definedTags.keys()));
        	return false;
        }
        
        mob.tell("Not yet implemented");
        return false;
    }
    
    public boolean canBeOrdered(){return false;}

    public boolean securityCheck(MOB mob){return CMSecurity.isAllowedAnywhere(mob,"CMDAREAS");}
}
