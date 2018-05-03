package com.planet_ink.coffee_mud.WebMacros;

import com.planet_ink.coffee_web.http.MultiPartData;
import com.planet_ink.coffee_web.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
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
   Copyright 2003-2018 Bo Zimmerman

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

public class FileMgr extends StdWebMacro
{
	@Override
	public String name()
	{
		return "FileMgr";
	}

	@Override
	public boolean isAdminMacro()
	{
		return true;
	}

	public boolean matches(String s1, String s2)
	{
		if(s1.length()==0)
			return true;
		if(s2.length()==0)
			return false;
		if(Pattern.matches(s1, s2))
			return true;
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

	public void compileFilenamesList(CMFile F, String regex, Vector<String> V)
	{
		if((!F.canRead())||(!F.isDirectory()))
			return;
		final String[] list=F.list();
		String path=F.getAbsolutePath();
		if(!path.endsWith("/"))
			path+="/";
		for(int l=0;l<list.length;l++)
		{
			final CMFile F2=new CMFile(path+list[l],null,CMFile.FLAG_LOGERRORS);
			if(F2.isDirectory())
			{
				if (!(path+list[l]).equalsIgnoreCase("/resources/map")
				&& !(path+list[l]).equalsIgnoreCase("/resources/catalog")
				&& !(path+list[l]).equalsIgnoreCase("/./.svn"))
					compileFilenamesList(F2,regex,V);
			}
			else
			if(matches(regex,F2.getName()))
				V.addElement(F.getAbsolutePath()+"/"+list[l]);
		}
	}

	public void compileTextListFromFiles(Vector<String> files, String regex, Vector<String> V)
	{
		final Pattern P=Pattern.compile(regex,Pattern.CASE_INSENSITIVE|Pattern.DOTALL|Pattern.MULTILINE);
		for(int f=0;f<files.size();f++)
		{
			final StringBuffer buf=new CMFile(files.elementAt(f),null).text();
			if(P.matcher(buf).find())
				V.addElement(files.elementAt(f));
		}
	}

	@Override
	public String runMacro(HTTPRequest httpReq, String parm, HTTPResponse httpResp)
	{
		final java.util.Map<String,String> parms=parseParms(parm);
		String path=httpReq.getUrlParameter("PATH");
		if(path==null)
			path="";
		String file=httpReq.getUrlParameter("FILE");
		if(file==null)
		{
			file="";
		}
		final MOB M = Authenticate.getAuthenticatedMob(httpReq);
		if(M==null)
			return "[authentication error]";
		try
		{
			String filePath=path;
			if((filePath.length()>2)&&(!filePath.endsWith("/")))
				filePath+="/";
			String prefix="";
			if(parms.containsKey("DEFAULT"))
				prefix = new CMFile(filePath,M).canVFSEquiv()?"::":"//";
			else
			if(parms.containsKey("VFS")
			||parms.containsKey("LOCAL")
			||parms.containsKey("BOTH"))
			{
				if(filePath.startsWith("//")||filePath.startsWith("::"))
					filePath=filePath.substring(2);
				if(parms.containsKey("VFS")||parms.containsKey("BOTH"))
					prefix="::";
				else
				if(parms.containsKey("LOCAL"))
					prefix="//";
			}
			CMFile F=new CMFile(prefix+filePath+file,M);
			String last=F.getVFSPathAndName();
			if(parms.containsKey("DELETE"))
			{
				if(F.delete())
				{
					for(final Iterator<String> i=Resources.findResourceKeys(file);i.hasNext();)
						Resources.removeResource(i.next());
					return "File `"+last+"` was deleted.";
				}
				return "File `"+last+"` was NOT deleted. Perhaps it`s read-only?";
			}
			else
			if(parms.containsKey("CREATE"))
			{
				final String s=httpReq.getUrlParameter("RAWTEXT");
				if(s==null)
					return "File `"+last+"` not updated -- no buffer!";
				if(parms.containsKey("VFS")||parms.containsKey("LOCAL")||parms.containsKey("BOTH"))
				{
					final StringBuilder returnMsg=new StringBuilder("");
					/*
					// code for "moving" files between vfs/local
					if(!parms.containsKey("VFS") && !parms.containsKey("BOTH"))
					{
						CMFile dF=new CMFile("::"+filePath+file,M);
						if(dF.canVFSEquiv())
						{
							if(!dF.deleteVFS())
								returnMsg.append(L("File `::@x1` not deleted -- error!  ",last));
							else
								returnMsg.append(L("File `::@x1` successfully deleted",last));
						}
					}
					if(!parms.containsKey("LOCAL") && !parms.containsKey("BOTH"))
					{
						CMFile dF=new CMFile("//"+filePath+file,M);
						if(dF.canLocalEquiv())
						{
							if(!dF.deleteLocal())
								returnMsg.append(L("File `//@x1` not deleted -- error!  ",last));
							else
								returnMsg.append(L("File `//@x1` successfully deleted",last));
						}
					}
					*/
					if((!F.canWrite())
					||(!F.saveText(s)))
						returnMsg.append(L("File `@x1@x2` not updated -- error!",prefix,last));
					if(parms.containsKey("BOTH"))
					{
						F=new CMFile("//"+filePath+file,M);
						if((!F.canWrite())
						||(!F.saveText(s)))
							returnMsg.append(L("File `//@x1` not updated -- error!",last));
					}
					if(returnMsg.length()>0)
						return returnMsg.toString();
				}
				else
				if((!F.canWrite())||(!F.saveText(s)))
				{
					F=new CMFile("::"+filePath+file,M);
					if((F.canWrite())&&(F.saveText(s)))
						return "File `"+last+"` updated.";
					return "File `"+last+"` not updated -- error!";
				}
				for(final Iterator<String> i=Resources.findResourceKeys(file);i.hasNext();)
					Resources.removeResource(i.next());
				return "File `"+last+"` updated.";
			}
			else
			if(parms.containsKey("NAMESEARCH"))
			{
				if(!F.isDirectory())
					return "Path not found! Search not completed.";
				final String s=parms.get("STR");
				if((s==null)||(s.length()==0))
					return "Search not completed! No expression given!";
				final Vector<String> compiledList=new Vector<String>();
				compileFilenamesList(F, s, compiledList);
				if(compiledList.size()==0)
					return "No files found matching your criteria.";
				final StringBuffer theList=new StringBuffer("");
				for(int c=0;c<compiledList.size();c++)
				{
					String name=compiledList.elementAt(c);
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
				final String s=parms.get("STR");
				if((s==null)||(s.length()==0))
					return "Search not completed! No expression given!";
				final Vector<String> fileList=new Vector<String>();
				compileFilenamesList(F,"", fileList);
				if(fileList.size()==0)
					return "No files found!";
				final Vector<String> compiledList=new Vector<String>();
				compileTextListFromFiles(fileList, s, compiledList);
				if(compiledList.size()==0)
					return "No files found matching your criteria.";
				final StringBuffer theList=new StringBuffer("");
				for(int c=0;c<compiledList.size();c++)
				{
					String name=compiledList.elementAt(c);
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
				if(newPath.endsWith("/"))
					newPath=newPath.substring(0,newPath.length()-1);
				final int x=newPath.lastIndexOf('/');
				if(x>0)
					newPath=newPath.substring(0,x);
				httpReq.addFakeUrlParameter("PATH",newPath);
				httpReq.removeUrlParameter("FILE");
				return "Deleted directory.";
			}
			else
			if(parms.containsKey("APPEND"))
			{
				final String s=httpReq.getUrlParameter("RAWTEXT");
				if(s==null)
					return "File `"+last+"` not appended -- no buffer!";
				final StringBuffer buf=F.textUnformatted();
				buf.append(s);
				if((!F.canWrite())||(!F.saveText(buf)))
					return "File `"+last+"` not appended -- error!";
				return "File `"+last+"` appended.";
			}
			else
			if(parms.containsKey("UPLOAD"))
			{
				byte[] buf=null;
				for(final MultiPartData d : httpReq.getMultiParts())
				{
					if((d.getVariables().containsKey("filename"))&&(d.getData()!=null))
					{
						F=new CMFile(prefix+filePath+d.getVariables().get("filename"),M);
						last=F.getVFSPathAndName();
						buf=d.getData();
					}
				}
				if(buf==null)
					return "File `"+last+"` not uploaded -- no buffer!";
				if((!F.canWrite())||(!F.saveRaw(buf)))
					return "File `"+F.getAbsolutePath()+"` not uploaded -- error!";
				return "File `"+F.getAbsolutePath()+"` uploaded.";
			}
		}
		catch(final Exception e)
		{
			return "[an error occurred performing the last operation]";
		}
		return "";
	}
}
