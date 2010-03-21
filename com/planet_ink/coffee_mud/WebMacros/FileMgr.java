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
import java.util.regex.Pattern;




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
public class FileMgr extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}
	public boolean isAdminMacro()	{return true;}

    
    public boolean matches(String s1, String s2)
    {
        if(s1.length()==0) return true;
        if(s2.length()==0) return false;
        if(s1.startsWith("*")&&(s1.endsWith("*")))
            return s2.toUpperCase().indexOf(s1.toUpperCase().substring(1,s1.length()-1))>=0;
        else
        if(s1.startsWith("*"))
            return s2.toUpperCase().endsWith(s1.toUpperCase().substring(1));
        else
        if(s1.endsWith("*"))
            return s2.toUpperCase().startsWith(s1.toUpperCase().substring(0,s1.length()-1));
        return s1.equalsIgnoreCase(s2);
    }
    
    public void compileFilenamesList(CMFile F, String regex, Vector V)
    {
        if((!F.canRead())||(!F.isDirectory())) return;
        String[] list=F.list();
        String path=F.getAbsolutePath();
        if(!path.endsWith("/")) path+="/";
        for(int l=0;l<list.length;l++)
        {
            CMFile F2=new CMFile(path+list[l],null,true);
            if(F2.isDirectory())
                compileFilenamesList(F2,regex,V);
            else
            if(matches(regex,F2.getName()))
                V.addElement(F.getAbsolutePath()+"/"+list[l]);
        }
    }

    public void compileTextListFromFiles(Vector files, String regex, Vector V)
    {
        Pattern P=Pattern.compile(regex,Pattern.CASE_INSENSITIVE|Pattern.DOTALL|Pattern.MULTILINE);
        for(int f=0;f<files.size();f++)
        {
            StringBuffer buf=new CMFile((String)files.elementAt(f),null,false).text();
            if(P.matcher(buf).find())
                V.addElement(files.elementAt(f));
        }
    }
    
    
	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		String path=httpReq.getRequestParameter("PATH");
		if(path==null) path="";
		String file=httpReq.getRequestParameter("FILE");
		if(file==null) file="";
		MOB M = Authenticate.getAuthenticatedMob(httpReq);
        if(M==null) return "[authentication error]";
		try
		{
			String filePath=path;
			if((filePath.length()>2)&&(!filePath.endsWith("/")))
				filePath+="/";
            CMFile F=new CMFile(filePath+file,M,false);
            String last=F.getVFSPathAndName();
			if(parms.containsKey("DELETE"))
			{
				if(F.delete())
					return "File `"+last+"` was deleted.";
				return "File `"+last+"` was NOT deleted. Perhaps it`s read-only?";
			}
			else
			if(parms.containsKey("CREATE"))
			{
				String s=httpReq.getRequestParameter("RAWTEXT");
				if(s==null) return "File `"+last+"` not updated -- no buffer!";
                if((!F.canWrite())||(!F.saveText(s)))
                {
            		F=new CMFile("::"+filePath+file,M,false);
                    if((F.canWrite())&&(F.saveText(s)))
        				return "File `"+last+"` updated.";
                    return "File `"+last+"` not updated -- error!";
                }
				return "File `"+last+"` updated.";
			}
            else
            if(parms.containsKey("NAMESEARCH"))
            {
                if(!F.isDirectory())
                    return "Path not found! Search not completed.";
                String s=(String)parms.get("STR");
                if((s==null)||(s.length()==0)) 
                    return "Search not completed! No expression given!";
                Vector compiledList=new Vector();
                compileFilenamesList(F, s, compiledList);
                if(compiledList.size()==0)
                    return "No files found matching your criteria.";
                StringBuffer theList=new StringBuffer("");
                for(int c=0;c<compiledList.size();c++)
                {
                    String name=((String)compiledList.elementAt(c));
                    if(name.startsWith(F.getAbsolutePath()+"/"))
                        name=name.substring(F.getAbsolutePath().length()+1);
                    theList.append(name+"<BR>");
                }
                return theList.toString();
            }
            else
            if(parms.containsKey("SEARCH"))
            {
                if(!F.isDirectory())
                    return "Path not found! Search not completed.";
                String s=(String)parms.get("STR");
                if((s==null)||(s.length()==0)) 
                    return "Search not completed! No expression given!";
                Vector fileList=new Vector();
                compileFilenamesList(F,"", fileList);
                if(fileList.size()==0)
                    return "No files found!";
                Vector compiledList=new Vector();
                compileTextListFromFiles(fileList, s, compiledList);
                if(compiledList.size()==0)
                    return "No files found matching your criteria.";
                StringBuffer theList=new StringBuffer("");
                for(int c=0;c<compiledList.size();c++)
                {
                    String name=((String)compiledList.elementAt(c));
                    if(name.startsWith(F.getAbsolutePath()+"/"))
                        name=name.substring(F.getAbsolutePath().length()+1);
                    theList.append(name+"<BR>");
                }
                return theList.toString();
            }
			else
			if(parms.containsKey("CREATEDIR"))
			{
				if(F.exists())
					return "File exists! Directory not created!";
				if(!F.mkdir())
					return "Error creating directory!";
				return "Created dir //"+filePath+file;
			}
            else
            if(parms.containsKey("DELETEDIR"))
            {
                if(!F.exists())
                    return "Directory '"+F.getAbsolutePath()+"' does not exists -- directory not deleted!";
                if(!F.delete())
                    return "Error deleting directory!";
                if(F.getAbsolutePath().equals("/"))
                    return  "Error deleting directory!";
                String newPath=F.getAbsolutePath();
                if(newPath.endsWith("/")) newPath=newPath.substring(0,newPath.length()-1);
                int x=newPath.lastIndexOf("/");
                if(x>0) newPath=newPath.substring(0,x);
                httpReq.addRequestParameters("PATH",newPath);
                httpReq.removeRequestParameter("FILE");
                return "Deleted directory.";
            }
			else
			if(parms.containsKey("APPEND"))
			{
				String s=httpReq.getRequestParameter("RAWTEXT");
				if(s==null) return "File `"+last+"` not appended -- no buffer!";
                StringBuffer buf=F.textUnformatted();
                buf.append(s);
                if((!F.canWrite())||(!F.saveText(buf)))
                    return "File `"+last+"` not appended -- error!";
				return "File `"+last+"` appended.";
			}
            else
            if(parms.containsKey("UPLOAD"))
            {
                byte[] buf=(byte[])httpReq.getRequestObjects().get("FILE");
                if(buf==null) return "File `"+last+"` not uploaded -- no buffer!";
                if((!F.canWrite())||(!F.saveRaw(buf)))
                    return "File `"+last+"` not uploaded -- error!";
                return "File `"+last+"` uploaded.";
            }
		}
		catch(Exception e)
		{
			return "[an error occurred performing the last operation]";
		}
		return "";
	}
}
