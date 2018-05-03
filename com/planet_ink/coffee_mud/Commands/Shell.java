package com.planet_ink.coffee_mud.Commands;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.JournalsLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.JournalsLibrary.MsgMkrCallback;
import com.planet_ink.coffee_mud.Libraries.interfaces.JournalsLibrary.MsgMkrResolution;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2005-2018 Bo Zimmerman

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

public class Shell extends StdCommand
{
	public Shell()
	{
	}

	private final String[]	access	= I(new String[] { "SHELL", "CMFS", "." });

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	protected static DVector pwds=new DVector(2);
	
	protected enum SubCmds
	{
		DIRECTORY('$',"DIRECTORY","LS"),
		COPY('>',"COPY","CP"),
		CHANGEDIRECTORY('.',"CHANGEDIRECTORY","CD","GO"),
		DELETE('-',"DELETE","RM","RD"),
		TYPE('\\',"TYPE","CAT","TP"),
		MAKEDIRECTORY('+',"MAKEDIRECTORY","MKDIR","MD"),
		FINDFILE('*',"FINDFILE","FF"),
		SEARCHTEXT('&',"SEARCHTEXT","GREP","ST"),
		EDIT('/',"EDIT"),
		MOVE('~',"MOVE","MV"),
		COMPAREFILES('?',"COMPAREFILES","DIFF","CF")
		;
		public String c;
		public String[] rest;
		private SubCmds(char shortName, String... longers)
		{
			c=""+shortName;
			rest=longers;
		}
	}
	
	protected final static String[] badTextExtensions={
		".ZIP",".JPE",".JPG",".GIF",".CLASS",".WAV",".BMP",".JPEG",".GZ",".TGZ",".JAR"
	};

	private class cp_options
	{
		boolean recurse=false;
		boolean forceOverwrites=false;
		boolean preservePaths=false;
		public cp_options(List<String> cmds)
		{
			for(int c=cmds.size()-1;c>=0;c--)
			{
				final String s=cmds.get(c);
				if(s.startsWith("-"))
				{
					for(int c2=1;c2<s.length();c2++)
					switch(s.charAt(c2))
					{
					case 'r':
					case 'R':
						recurse=true;
						break;
					case 'f':
					case 'F':
						forceOverwrites=true;
						break;
					case 'p':
					case 'P':
						preservePaths=true;
						break;
					}
					cmds.remove(c);
				}
			}
		}
	}

	private java.util.List<CMFile> sortDirsUp(CMFile[] files)
	{
		final Vector<CMFile> dirs=new Vector<CMFile>();
		CMFile CF=null;
		final Vector<CMFile> finalList=new Vector<CMFile>();
		for(int v=files.length-1;v>=0;v--)
		{
			CF=files[v];
			if((CF.isDirectory())&&(CF.exists()))
			{
				int x=0;
				while(x<=dirs.size())
				{
					if(x==dirs.size())
					{
						dirs.add(CF);
						break;
					}
					else
					if(dirs.get(x).getVFSPathAndName().length()<CF.getVFSPathAndName().length())
						x++;
					else
					{
						dirs.insertElementAt(CF,x);
						break;
					}
				}
			}
			else
				finalList.add(CF);

		}
		finalList.addAll(dirs);
		return finalList;
	}

	private java.util.List<CMFile>  sortDirsDown(CMFile[] files)
	{
		final Vector<CMFile> dirs=new Vector<CMFile>();
		final HashSet<CMFile> dirsH=new HashSet<CMFile>();
		CMFile CF=null;
		for(int v=files.length-1;v>=0;v--)
		{
			CF=files[v];
			if((CF.isDirectory())&&(CF.exists()))
			{
				int x=0;
				while(x<=dirs.size())
				{
					if(x==dirs.size())
					{
						dirs.add(CF);
						dirsH.add(CF);
						break;
					}
					else
					if(dirs.get(x).getVFSPathAndName().length()>CF.getVFSPathAndName().length())
						x++;
					else
					{
						dirs.insertElementAt(CF,x);
						dirsH.add(CF);
						break;
					}
				}
			}
		}
		for(final CMFile F : files)
		{
			if(!dirsH.contains(F))
				dirs.add(F);
		}
		return dirs;
	}

	public static final String incorporateBaseDir(String currentPath, String filename)
	{
		String starter="";
		if(filename.startsWith("::")||filename.startsWith("//"))
		{
			starter=filename.substring(0,2);
			filename=filename.substring(2);
		}
		if(!filename.startsWith("/"))
		{
			boolean didSomething=true;
			while(didSomething)
			{
				didSomething=false;
				if(filename.startsWith(".."))
				{
					filename=filename.substring(2);
					final int x=currentPath.lastIndexOf('/');
					if(x>=0)
						currentPath=currentPath.substring(0,x);
					else
						currentPath="";
					didSomething=true;
				}
				if((filename.startsWith("."))&&(!(filename.startsWith(".."))))
				{
					filename=filename.substring(1);
					didSomething=true;
				}
				while(filename.startsWith("/"))
					filename=filename.substring(1);
			}
			if((currentPath.length()>0)&&(filename.length()>0))
				filename=currentPath+"/"+filename;
			else
			if(currentPath.length()>0)
				filename=currentPath;
		}
		return starter+filename;
	}

	@Override
	public Object executeInternal(MOB mob, int metaFlags, Object... args) throws java.io.IOException
	{
		String pwd=(pwds.contains(mob))?(String)pwds.get(pwds.indexOf(mob),2):"";
		if((args.length>0)&&(args[0].equals(".")))
			return pwd;
		return null;
	}

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		String pwd=(pwds.contains(mob))?(String)pwds.get(pwds.indexOf(mob),2):"";
		commands.remove(0);
		if(commands.size()==0)
		{
			mob.tell(L("Current directory: /@x1",pwd));
			return false;
		}
		String first=commands.get(0).toUpperCase();
		final StringBuffer allcmds=new StringBuffer("");
		SubCmds cmd = null;
		for(final SubCmds sub : SubCmds.values())
		{
			final String shortcut=""+sub.c;
			if(first.startsWith(shortcut))
			{
				first=first.substring(shortcut.length()).trim();
				if(first.length()>0)
				{
					if(commands.size()>1)
						commands.set(1,first);
					else
						commands.add(first);
				}
				cmd=sub;
				break;
			}
			for(final String s : sub.rest)
			{
				if(s.startsWith(first.toUpperCase()))
				{
					cmd=sub;
					break;
				}
				if(s==sub.rest[0])
				{
					allcmds.append(s+" (");
					for(String s2 : sub.rest)
					{
						if(s2 != s)
						{
							allcmds.append(s2);
							if(s2!=sub.rest[sub.rest.length-1])
								allcmds.append("/");
						}
					}
					allcmds.append("), ");
				}
			}
			if(cmd != null)
				break;
		}
		if(cmd == null)
		{
			mob.tell(L("'@x1' is an unknown command.  Valid commands are: @x2and SHELL alone to check your current directory.",first,allcmds.toString()));
			return false;
		}
		final Set<String> skipHash = new XHashSet<String>(new String[]{"resources/map","resources/catalog","./.svn"});
		final boolean killOnSession=((mob.session()!=null)&&(!mob.session().isStopped())); 
		switch(cmd)
		{
		case DIRECTORY: // directory
		{
			final cp_options opts=new cp_options(commands);
			final CMFile[] dirs=CMFile.getFileList(incorporateBaseDir(pwd,CMParms.combine(commands,1)),mob,opts.recurse,true,skipHash);
			if(dirs==null)
			{
				mob.tell(L("^xError: invalid directory!^N"));
				return false;
			}
			final StringBuffer msg=new StringBuffer("\n\r^y .\n\r^y ..\n\r");
			final int COL1_LEN=CMLib.lister().fixColWidth(35.0, mob);
			final int COL2_LEN=CMLib.lister().fixColWidth(20.0, mob);
			final int COL3_LEN=CMLib.lister().fixColWidth(20.0, mob);
			for (final CMFile dir : dirs)
			{
				final CMFile entry=dir;
				if(entry.isDirectory())
				{
					if(entry.isLocalFile()&&(!entry.canVFSEquiv()))
						msg.append(" ");
					else
					if((entry.isLocalFile()&&(entry.canVFSEquiv()))
					||((entry.isVFSFile())&&(entry.canLocalEquiv())))
						msg.append("^R+");
					else
						msg.append("^r-");
					msg.append("^y"+CMStrings.padRight(entry.getName(),COL1_LEN));
					msg.append("^w"+CMStrings.padRight(CMLib.time().date2String(entry.lastModified()),COL2_LEN));
					msg.append("^w"+CMStrings.padRight(entry.author(),COL3_LEN));
					msg.append("\n\r");
				}
			}
			for (final CMFile dir : dirs)
			{
				if(killOnSession && ((mob.session()==null)||(mob.session().isStopped())))
					break;
				final CMFile entry=dir;
				if(!entry.isDirectory())
				{
					if(entry.isLocalFile()&&(!entry.canVFSEquiv()))
						msg.append(" ");
					else
					if((entry.isLocalFile()&&(entry.canVFSEquiv()))
					||((entry.isVFSFile())&&(entry.canLocalEquiv())))
						msg.append("^R+");
					else
						msg.append("^r-");
					msg.append("^w"+CMStrings.padRight(entry.getName(),COL1_LEN));
					msg.append("^w"+CMStrings.padRight(CMLib.time().date2String(entry.lastModified()),COL2_LEN));
					msg.append("^w"+CMStrings.padRight(entry.author(),COL3_LEN));
					msg.append("\n\r");
				}
			}
			if(mob.session()!=null)
				mob.session().colorOnlyPrintln(msg.toString());
			break;
		}
		case COPY: // copy
		{
			final cp_options opts=new cp_options(commands);
			if(commands.size()==2)
				commands.add(".");
			if(commands.size()<3)
			{
				mob.tell(L("^xError  : source and destination must be specified!^N"));
				mob.tell(L("^xOptions: -r = recurse into directories.^N"));
				mob.tell(L("^x       : -p = preserve paths.^N"));
				return false;
			}
			final String source=commands.get(1);
			String target=CMParms.combine(commands,2);
			final CMFile[] dirs=CMFile.getFileList(incorporateBaseDir(pwd,source),mob,opts.recurse,true,skipHash);
			if(dirs==null)
			{
				mob.tell(L("^xError: invalid source!^N"));
				return false;
			}
			if(dirs.length==0)
			{
				mob.tell(L("^xError: no source files matched^N"));
				return false;
			}
			if((dirs.length==1)&&(!target.trim().startsWith("::")&&(!target.trim().startsWith("//"))))
			{
				String ttarget1=(dirs[0].isLocalFile())?"//"+target.trim():"::"+target.trim();
				final CMFile TD1=new CMFile(incorporateBaseDir(pwd,ttarget1),mob);
				String ttargetd1=(dirs[0].isLocalFile())?"//"+TD1.getParent():"::"+TD1.getParent();
				final CMFile TDp1=new CMFile(incorporateBaseDir(pwd,ttargetd1),mob);
				String ttarget2=(dirs[0].isLocalFile())?"::"+target.trim():"//"+target.trim();
				final CMFile TD2=new CMFile(incorporateBaseDir(pwd,ttarget2),mob);
				String ttargetd2=(dirs[0].isLocalFile())?"::"+TD2.getParent():"//"+TD2.getParent();
				final CMFile TDp2=new CMFile(incorporateBaseDir(pwd,ttargetd2),mob);
				if(TD1.exists() && TD1.isDirectory())
					target=ttarget1;
				else
				if(TD2.exists() && TD2.isDirectory())
					target=ttarget2;
				else
				if(TDp1.exists() && TDp1.isDirectory())
					target=ttarget1;
				else
				if(TDp2.exists() && TDp2.isDirectory())
					target=ttarget2;
			}
			final CMFile DD=new CMFile(incorporateBaseDir(pwd,target),mob);
			final java.util.List<CMFile> ddirs=sortDirsUp(dirs);
			for(final CMFile SF: ddirs)
			{
				if(killOnSession && ((mob.session()==null)||(mob.session().isStopped())))
					break;
				if ((SF == null) || (!SF.exists()))
				{
					mob.tell(L("^xError: source @x1 does not exist!^N", desc(SF)));
					return false;
				}
				if (!SF.canRead())
				{
					mob.tell(L("^xError: access denied to source @x1!^N", desc(SF)));
					return false;
				}
				if((SF.isDirectory())&&(!opts.preservePaths))
				{
					if(dirs.length==1)
					{
						mob.tell(L("^xError: source can not be a directory!^N"));
						return false;
					}
					continue;
				}
				CMFile DF=DD;
				target=DD.getVFSPathAndName();
				if(DD.isDirectory())
				{
					String name=SF.getName();
					if((opts.recurse)&&(opts.preservePaths))
					{
						final String srcPath=SF.getVFSPathAndName();
						if(srcPath.startsWith(pwd+"/"))
							name=srcPath.substring(pwd.length()+1);
						else
							name=srcPath;
					}
					if(target.length()>0)
						target=target+"/"+name;
					else
						target=name;
					if(DD.demandedVFS())
						target="::"+target;
					else
					if(DD.demandedLocal())
						target="//"+target;
					else
						target=(SF.isLocalFile()&&DD.canLocalEquiv())?"//"+target:"::"+target;
					DF=new CMFile(target,mob);
				}
				else
				if(dirs.length>1)
				{
					mob.tell(L("^xError: destination must be a directory!^N"));
					return false;
				}
				if (DF.mustOverwrite())
				{
					mob.tell(L("^xError: destination @x1 already exists!^N", desc(DF)));
					return false;
				}
				if (!DF.canWrite())
				{
					mob.tell(L("^xError: access denied to destination @x1!^N", desc(DF)));
					return false;
				}
				if((SF.isDirectory())&&(opts.recurse))
				{
					if(!DF.mkdir())
						mob.tell(L("^xWarning: failed to mkdir @x1 ^N",desc(DF)));
					else
						mob.tell(L("@x1 copied to @x2",desc(SF),desc(DF)));
				}
				else
				{
					final byte[] O=SF.raw();
					if (O.length == 0)
					{
						mob.tell(L("^xWarning: @x1 file had no data^N", desc(SF)));
					}
					if(!DF.saveRaw(O))
						mob.tell(L("^xWarning: write failed to @x1 ^N",desc(DF)));
					else
						mob.tell(L("@x1 copied to @x2",desc(SF),desc(DF)));
				}
			}
			break;
		}
		case CHANGEDIRECTORY: // cd
		{
			final CMFile newDir=new CMFile(incorporateBaseDir(pwd,CMParms.combine(commands,1)),mob);
			final String changeTo=newDir.getVFSPathAndName();
			if(!newDir.exists())
			{
				mob.tell(L("^xError: Directory '@x1' does not exist.^N",CMParms.combine(commands,1)));
				return false;
			}
			if((!newDir.canRead())||(!newDir.isDirectory()))
			{
				mob.tell(L("^xError: You are not authorized enter that directory.^N"));
				return false;
			}
			pwd=changeTo;
			mob.tell(L("Directory is now: /@x1",pwd));
			pwds.removeElement(mob);
			pwds.add(mob,pwd);
			return true;
		}
		case DELETE: // delete
		{
			final cp_options opts=new cp_options(commands);
			final CMFile[] dirs=CMFile.getFileList(incorporateBaseDir(pwd,CMParms.combine(commands,1)),mob,opts.recurse,false,skipHash);
			if(dirs==null)
			{
				mob.tell(L("^xError: invalid filename!^N"));
				return false;
			}
			if(dirs.length==0)
			{
				mob.tell(L("^xError: no files matched^N"));
				return false;
			}
			final java.util.List<CMFile> ddirs=sortDirsDown(dirs);
			for(int d=0;d<ddirs.size();d++)
			{
				if(killOnSession && ((mob.session()==null)||(mob.session().isStopped())))
					break;
				final CMFile CF=ddirs.get(d);
				if((CF==null)||(!CF.exists()))
				{
					mob.tell(L("^xError: @x1 does not exist!^N",desc(CF)));
					return false;
				}
				if(!CF.canWrite())
				{
					mob.tell(L("^xError: access denied to @x1!^N",desc(CF)));
					return false;
				}
				if((!CF.delete())&&(CF.exists()))
				{
					mob.tell(L("^xError: delete of @x1 failed.^N",desc(CF)));
					return false;
				}
				mob.tell(L("@x1 deleted.",desc(CF)));
			}
			break;
		}
		case TYPE: // type
		{
			final CMFile[] dirs=CMFile.getFileList(incorporateBaseDir(pwd,CMParms.combine(commands,1)),mob,false,false,skipHash);
			if(dirs==null)
			{
				mob.tell(L("^xError: invalid filename!^N"));
				return false;
			}
			if(dirs.length==0)
			{
				mob.tell(L("^xError: no files matched^N"));
				return false;
			}
			for (final CMFile dir : dirs)
			{
				if(killOnSession && ((mob.session()==null)||(mob.session().isStopped())))
					break;
				final CMFile CF=dir;
				if((CF==null)||(!CF.exists()))
				{
					mob.tell(L("^xError: file does not exist!^N"));
					return false;
				}
				if(!CF.canRead())
				{
					mob.tell(L("^xError: access denied!^N"));
					return false;
				}
				if(mob.session()!=null)
				{
					mob.session().colorOnlyPrintln(L("\n\r^xFile /@x1^.^N\n\r",CF.getVFSPathAndName()));
					mob.session().rawPrint(CF.text().toString());
				}
			}
			break;
		}
		case MAKEDIRECTORY: // makedirectory
		{
			final CMFile CF=new CMFile(incorporateBaseDir(pwd,CMParms.combine(commands,1)),mob);
			if(CF.exists())
			{
				mob.tell(L("^xError: file already exists!^N"));
				return false;
			}
			if(!CF.canWrite())
			{
				mob.tell(L("^xError: access denied!^N"));
				return false;
			}
			if(!CF.mkdir())
			{
				mob.tell(L("^xError: makedirectory failed.^N"));
				return false;
			}
			mob.tell(L("Directory '/@x1' created.",CF.getAbsolutePath()));
			break;
		}
		case FINDFILE: // findfiles
		{
			String substring=CMParms.combine(commands,1).trim();
			if(substring.length()==0)
				substring="*";
			final CMFile[] dirs=CMFile.getFileList(incorporateBaseDir(pwd,substring),mob,true,true,skipHash);
			final StringBuffer msg=new StringBuffer("");
			if(dirs.length==0)
			{
				mob.tell(L("^xError: no files matched^N"));
				return false;
			}
			for (final CMFile dir : dirs)
			{
				if(killOnSession && ((mob.session()==null)||(mob.session().isStopped())))
					break;
				final CMFile entry=dir;
				if(!entry.isDirectory())
				{
					if(entry.isLocalFile()&&(!entry.canVFSEquiv()))
						msg.append(" ");
					else
					if((entry.isLocalFile()&&(entry.canVFSEquiv()))
					||((entry.isVFSFile())&&(entry.canLocalEquiv())))
						msg.append("^R+");
					else
						msg.append("^r-");
					msg.append("^w"+entry.getVFSPathAndName());
					msg.append("\n\r");
				}
			}
			if(mob.session()!=null)
				mob.session().colorOnlyPrintln(msg.toString());
			return false;
		}
		case SEARCHTEXT: // searchtext
		{
			String substring=CMParms.combine(commands,1).trim();
			if(substring.length()==0)
			{
				mob.tell(L("^xError: you must specify a search string^N"));
				return false;
			}
			final CMFile[] dirs=CMFile.getFileList(incorporateBaseDir(pwd,"*"),mob,true,true,skipHash);
			if(dirs.length==0)
			{
				mob.tell(L("^xError: no files found!^N"));
				return false;
			}
			mob.session().print(L("\n\rSearching..."));
			substring=substring.toUpperCase();
			final Vector<CMFile> dirs2=new Vector<CMFile>();
			for (final CMFile dir : dirs)
			{
				if(killOnSession && ((mob.session()==null)||(mob.session().isStopped())))
					break;
				final CMFile entry=dir;
				if(!entry.isDirectory())
				{
					boolean proceed=true;
					for (final String badTextExtension : badTextExtensions)
					{
						if(entry.getName().toUpperCase().endsWith(badTextExtension))
						{
							proceed = false;
							break;
						}
					}
					if(proceed)
					{
						final StringBuffer text=entry.textUnformatted();
						if(text.toString().toUpperCase().indexOf(substring)>=0)
							dirs2.add(entry);
					}
				}
			}
			if(dirs2.size()==0)
			{
				mob.tell(L("\n\r^xError: no files matched^N"));
				return false;
			}
			final StringBuffer msg=new StringBuffer("\n\r");
			for(int d=0;d<dirs2.size();d++)
			{
				final CMFile entry=dirs2.get(d);
				if(entry.isLocalFile()&&(!entry.canVFSEquiv()))
					msg.append(" ");
				else
				if((entry.isLocalFile()&&(entry.canVFSEquiv()))
				||((entry.isVFSFile())&&(entry.canLocalEquiv())))
					msg.append("^R+");
				else
					msg.append("^r-");
				msg.append("^w"+entry.getVFSPathAndName());
				msg.append("\n\r");
			}
			if(mob.session()!=null)
				mob.session().colorOnlyPrintln(msg.toString());
			return false;
		}
		case EDIT: // edit
		{
			final CMFile file=new CMFile(incorporateBaseDir(pwd,CMParms.combine(commands,1)),mob);
			if((!file.canWrite())
			||(file.isDirectory()))
			{
				mob.tell(L("^xError: You are not authorized to create/modify that file.^N"));
				return false;
			}
			StringBuffer buf=file.textUnformatted();
			final String CR=Resources.getEOLineMarker(buf);
			final List<String> vbuf=Resources.getFileLineVector(buf);
			buf=null;
			mob.tell(L("@x1 has been loaded.\n\r\n\r",desc(file)));
			final String messageTitle="File: "+file.getVFSPathAndName();
			CMLib.journals().makeMessageASync(mob, messageTitle, vbuf, false, new MsgMkrCallback()
			{
				@Override
				public void callBack(final MOB mob, final Session sess, final MsgMkrResolution resolution)
				{
					if(resolution==JournalsLibrary.MsgMkrResolution.SAVEFILE)
					{
						final StringBuffer text=new StringBuffer("");
						for(int i=0;i<vbuf.size();i++)
							text.append((vbuf.get(i))+CR);
						if(file.saveText(text))
						{
							for(final Iterator<String> i=Resources.findResourceKeys(file.getName());i.hasNext();)
								Resources.removeResource(i.next());
							mob.tell(L("File saved."));
						}
						else
							mob.tell(L("^XError: could not save the file!^N^."));
					}
				}
			});
			return false;
		}
		case MOVE: // move
		{
			final cp_options opts=new cp_options(commands);
			if(commands.size()==2)
				commands.add(".");
			if(commands.size()<3)
			{
				mob.tell(L("^xError  : source and destination must be specified!^N"));
				mob.tell(L("^xOptions: -r = recurse into directories.^N"));
				mob.tell(L("^x       : -f = force overwrites.^N"));
				mob.tell(L("^x       : -p = preserve paths.^N"));
				return false;
			}
			final String source=commands.get(1);
			String target=CMParms.combine(commands,2);
			final CMFile[] dirs=CMFile.getFileList(incorporateBaseDir(pwd,source),mob,opts.recurse,true,skipHash);
			if(dirs==null)
			{
				mob.tell(L("^xError: invalid source!^N"));
				return false;
			}
			if(dirs.length==0)
			{
				mob.tell(L("^xError: no source files matched^N"));
				return false;
			}
			if((dirs.length==1)&&(!target.trim().startsWith("::")&&(!target.trim().startsWith("//"))))
			{
				String ttarget1=(dirs[0].isLocalFile())?"//"+target.trim():"::"+target.trim();
				final CMFile TD1=new CMFile(incorporateBaseDir(pwd,ttarget1),mob);
				String ttargetd1=(dirs[0].isLocalFile())?"//"+TD1.getParent():"::"+TD1.getParent();
				final CMFile TDp1=new CMFile(incorporateBaseDir(pwd,ttargetd1),mob);
				String ttarget2=(dirs[0].isLocalFile())?"::"+target.trim():"//"+target.trim();
				final CMFile TD2=new CMFile(incorporateBaseDir(pwd,ttarget2),mob);
				String ttargetd2=(dirs[0].isLocalFile())?"::"+TD2.getParent():"//"+TD2.getParent();
				final CMFile TDp2=new CMFile(incorporateBaseDir(pwd,ttargetd2),mob);
				if(TD1.exists() && TD1.isDirectory())
					target=ttarget1;
				else
				if(TD2.exists() && TD2.isDirectory())
					target=ttarget2;
				else
				if(TDp1.exists() && TDp1.isLocalDirectory())
					target=ttarget1;
				else
				if(TDp2.exists() && TDp2.isVFSDirectory())
					target=ttarget2;
			}
			final CMFile DD=new CMFile(incorporateBaseDir(pwd,target),mob);
			final java.util.List<CMFile> ddirs=sortDirsUp(dirs);
			java.util.List<CMFile> dirsLater=new Vector<CMFile>();
			for(int d=0;d<ddirs.size();d++)
			{
				if(killOnSession && ((mob.session()==null)||(mob.session().isStopped())))
					break;
				final CMFile SF=ddirs.get(d);
				if ((SF == null) || (!SF.exists()))
				{
					mob.tell(L("^xError: source @x1 does not exist!^N", desc(SF)));
					return false;
				}
				if (!SF.canRead())
				{
					mob.tell(L("^xError: access denied to source @x1!^N", desc(SF)));
					return false;
				}
				if((SF.isDirectory())&&(!opts.preservePaths))
				{
					if(dirs.length==1)
					{
						mob.tell(L("^xError: source can not be a directory!^N"));
						return false;
					}
					continue;
				}
				CMFile DF=DD;
				target=DD.getVFSPathAndName();
				if(DD.isDirectory())
				{
					String name=SF.getName();
					if((opts.recurse)&&(opts.preservePaths))
					{
						final String srcPath=SF.getVFSPathAndName();
						if(srcPath.startsWith(pwd+"/"))
							name=srcPath.substring(pwd.length()+1);
						else
							name=srcPath;
					}
					if(target.length()>0)
						target=target+"/"+name;
					else
						target=name;
					if(DD.demandedVFS())
						target="::"+target;
					else
					if(DD.demandedLocal())
						target="//"+target;
					else
						target=(SF.isLocalFile()&&DD.canLocalEquiv())?"//"+target:"::"+target;
					DF=new CMFile(target,mob);
				}
				else
				if(dirs.length>1)
				{
					mob.tell(L("^xError: destination must be a directory!^N"));
					return false;
				}
				if (DF.mustOverwrite() && (!opts.forceOverwrites))
				{
					mob.tell(L("^xError: destination @x1 already exists!^N", desc(DF)));
					return false;
				}
				if (!DF.canWrite())
				{
					mob.tell(L("^xError: access denied to destination @x1!^N", desc(DF)));
					return false;
				}
				if((SF.isDirectory())&&(opts.recurse))
				{
					if((!DF.mustOverwrite())&&(!DF.mkdir()))
						mob.tell(L("^xWarning: failed to mkdir @x1 ^N",desc(DF)));
					else
						mob.tell(L("@x1 copied to @x2",desc(SF),desc(DF)));
					dirsLater.add(SF);
				}
				else
				{
					final byte[] O=SF.raw();
					if (O.length == 0)
					{
						mob.tell(L("^xWarning: @x1 file had no data^N", desc(SF)));
					}
					if(!DF.saveRaw(O))
					{
						mob.tell(L("^xWarning: write failed to @x1 ^N",desc(DF)));
						break;
					}
					else
						mob.tell(L("@x1 moved to @x2",desc(SF),desc(DF)));
					if((!SF.delete())&&(SF.exists()))
					{
						mob.tell(L("^xError: Unable to delete file @x1",desc(SF)));
						break;
					}
				}
			}
			dirsLater=sortDirsDown(dirsLater.toArray(new CMFile[0]));
			for(int d=0;d<dirsLater.size();d++)
			{
				if(killOnSession && ((mob.session()==null)||(mob.session().isStopped())))
					break;
				final CMFile CF=dirsLater.get(d);
				if((!CF.delete())&&(CF.exists()))
				{
					mob.tell(L("^xError: Unable to delete dir @x1",desc(CF)));
					break;
				}
			}
			break;
		}
		case COMPAREFILES: // compare files
		{
			if(commands.size()==2)
				commands.add(".");
			if(commands.size()<3)
			{
				mob.tell(L("^xError  : first and second files be specified!^N"));
				return false;
			}
			final String firstFilename=commands.get(1);
			String secondFilename=CMParms.combine(commands,2);
			final CMFile file1=new CMFile(incorporateBaseDir(pwd,firstFilename),mob);
			if((!file1.canRead())
			||(file1.isDirectory()))
			{
				mob.tell(L("^xError: You are not authorized to read the first file.^N"));
				return false;
			}
			String prefix="";
			if(secondFilename.equals("."))
			{
				if(file1.isVFSFile())
				{
					prefix="//";
					secondFilename=CMFile.vfsifyFilename(firstFilename);
				}
				else
				if(file1.isLocalFile())
				{
					prefix="::";
					secondFilename=CMFile.vfsifyFilename(firstFilename);
				}
				else
				{
					mob.tell(L("^xError  : first and second files be specified!^N"));
					return false;
				}
			}
			final CMFile file2=new CMFile(prefix+incorporateBaseDir(pwd,secondFilename),mob);
			if((!file2.canRead())||(file2.isDirectory()))
			{
				mob.tell(L("^xError: You are not authorized to read the second file.^N"));
				return false;
			}
			final StringBuilder text1=new StringBuilder("");
			for(final String s : Resources.getFileLineVector(file1.text()))
			{
				if(s.trim().length()>0)
					text1.append(s.trim()).append("\n\r");
			}
			final StringBuilder text2=new StringBuilder("");
			for(final String s : Resources.getFileLineVector(file2.text()))
			{
				if(s.trim().length()>0)
					text2.append(s.trim()).append("\n\r");
			}
			final LinkedList<CMStrings.Diff> diffs=CMStrings.diff_main(text1.toString(), text2.toString(), false);
			boolean flipFlop=false;
			for(final CMStrings.Diff d : diffs)
			{
				final StringBuilder str=new StringBuilder("\n\r^H"+d.operation.toString()+": ");
				str.append(flipFlop?"^N":"^w");
				flipFlop=!flipFlop;
				str.append(d.text);
				mob.session().colorOnlyPrintln(str.toString());
			}
			mob.tell(L("^HDONE."));
			return false;
		}
		default:
			mob.tell(L("'@x1' is an unknown command.  Valid commands are: @x2and SHELL alone to check your current directory.",first,allcmds.toString()));
			return false;
		}
		return true;
	}

	public String desc(CMFile CF)
	{
		return (CF.isLocalFile() ? "Local file " : "VFS file ") + "'/" + CF.getVFSPathAndName() + "'";
	}

	@Override
	public boolean canBeOrdered()
	{
		return false;
	}

	@Override
	public boolean securityCheck(MOB mob)
	{
		return CMSecurity.hasAccessibleDir(mob, null);
	}
}
