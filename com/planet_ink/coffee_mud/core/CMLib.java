package com.planet_ink.coffee_mud.core;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
import java.lang.reflect.Modifier;


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
public class CMLib
{
    public static final int LIBRARY_DATABASE=0;
    public static final int LIBRARY_THREADS=1;
    public static final int LIBRARY_INTERMUD=2;
    public static final int LIBRARY_HTTP=3;
    public static final int LIBRARY_TOTAL=4;

    private static final CMObject[] libraries=new CMObject[LIBRARY_TOTAL];
    
    public static DatabaseEngine database(){return (DatabaseEngine)libraries[LIBRARY_DATABASE];}
    public static ThreadEngine threads(){return (ThreadEngine)libraries[LIBRARY_THREADS];}
    public static I3Interface intermud(){return (I3Interface)libraries[LIBRARY_INTERMUD];}
    public static ExternalHTTPRequests httpUtils(){return (ExternalHTTPRequests)libraries[LIBRARY_HTTP];}
    

    public static int convertToLibraryCode(Object O)
    {
        if(O instanceof DatabaseEngine) return LIBRARY_DATABASE;
        if(O instanceof ThreadEngine) return LIBRARY_THREADS;
        if(O instanceof I3Interface) return LIBRARY_INTERMUD;
        if(O instanceof ExternalHTTPRequests) return LIBRARY_HTTP;
        return -1;
    }
    
    public static void registerLibrary(CMObject O)
    {
        int code=convertToLibraryCode(O);
        if(code>=0) libraries[code]=O;
    }
    
    public static void registerLibraries(Enumeration e)
    {
        for(;e.hasMoreElements();)
            registerLibrary((CMObject)e.nextElement());
    }
}
