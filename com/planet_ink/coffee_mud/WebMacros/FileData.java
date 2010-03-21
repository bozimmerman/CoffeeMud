package com.planet_ink.coffee_mud.WebMacros;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;
import com.planet_ink.coffee_mud.core.exceptions.HTTPServerException;


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
public class FileData extends StdWebMacro
{
    public String name()    {return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

    public boolean isAWebPath(){return true;}
    public boolean preferBinary(){return true;}
    
    public String getFilename(ExternalHTTPRequests httpReq, String filename)
    {
        String path=httpReq.getRequestParameter("PATH");
        if(path==null) return filename;
        String file=httpReq.getRequestParameter("FILE");
        if(file==null) return filename;
        return path+"/"+file;
    }
    
    public byte[] runBinaryMacro(ExternalHTTPRequests httpReq, String parm) throws HTTPServerException
    {
        String filename=getFilename(httpReq,"");
        if(filename.length()==0) return null;
		MOB M = Authenticate.getAuthenticatedMob(httpReq);
        if(M==null) return null;
        CMFile F=new CMFile(filename,M,false);
        if((!F.exists())||(!F.canRead())) return null;
        return F.raw();
    }
    
    public String runMacro(ExternalHTTPRequests httpReq, String parm) throws HTTPServerException
    {
        return "[Unimplemented string method!]";
    }
}