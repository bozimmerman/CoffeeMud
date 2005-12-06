package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.sun.rsasign.f;

import java.util.*;
import java.io.*;


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
public class Shell extends StdCommand
{
	public Shell(){}

	private String[] access={"SHELL","VFS","."};
	public String[] getAccessWords(){return access;}
    
    protected static DVector pwds=new DVector(2);
    protected String[][] SUB_CMDS={
            {">>","MOVE","MV"},
            {"<<","REMOVE","RM"},
            {">","COPY","CP"},
            {"<","BACKUP","BK"},
            {".","CHANGEDIRECTORY","CD","GO"},
            {"-","DELETE","RM","RD"},
            {"/","EDIT"},
            {"$","DIRECTORY","LS"},
            {"@","MAKEDIRECTORY","MD"},
            {"\\","TYPE","TP"},
    };
    
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
        String pwd=(pwds.contains(mob))?(String)pwds.elementAt(pwds.indexOf(mob),2):"";
        commands.removeElementAt(0);
        if(commands.size()==0)
        {
            mob.tell("Current directory: /"+pwd);
            return false;
        }
        int cmd=-1;
        String first=((String)commands.firstElement()).toUpperCase();
        StringBuffer allcmds=new StringBuffer("");
        for(int i=0;i<SUB_CMDS.length;i++)
        {
            String shortcut=SUB_CMDS[i][0];
            if(first.startsWith(shortcut))
            {
                first=first.substring(shortcut.length()).trim();
                if(first.length()>0)
                {
                    if(commands.size()>1)
                        commands.setElementAt(first,1);
                    else
                        commands.addElement(first);
                }
                cmd=i;
                break;
            }
            for(int x=1;x<SUB_CMDS[i].length;x++)
            {
                if(SUB_CMDS[i][x].startsWith(first.toUpperCase()))
                {
                    cmd=i;
                    break;
                }
                if(x==1)
                {
                    allcmds.append(SUB_CMDS[i][x]+" (");
                    for(int x2=0;x2<SUB_CMDS[i].length;x2++)
                        if(x2!=x)
                        {
                            allcmds.append(SUB_CMDS[i][x2]);
                            if(x2<SUB_CMDS[i].length-1)allcmds.append("/");
                        }
                    allcmds.append("), ");
                }
            }
            if(cmd>=0) break;
        }
        switch(cmd)
        {
        case 0: // move
        {
            break;
        }
        case 1: // remove
        {
            break;
        }
        case 2: // copy
        {
            break;
        }
        case 3: // backup
        {
            break;
        }
        case 4: // cd
        {
            String changeTo=Util.combine(commands,1);
            if(changeTo.equals("."))
            {
                mob.tell("Directory is now: /"+pwd);
                return true;
            }
            if(changeTo.equals(".."))
            {
                if(pwd.trim().length()==0)
                {
                    mob.tell("^xError: can't go back any farther!^N");
                    return true;
                }
                int x=pwd.lastIndexOf("/");
                if(x>=0)
                    changeTo=pwd.substring(0,x);
                else
                    changeTo="";
            }
            else
            if(!changeTo.startsWith("/"))
                changeTo=pwd+"/"+changeTo;
            if(changeTo.startsWith("/"))
                changeTo=changeTo.substring(1);
            changeTo=Resources.fixFilename(changeTo);
            if(!CMSecurity.canTraverseDir(mob,mob.location(),changeTo))
            {
                mob.tell("^xError: you are not authorized enter that directory.^N");
                return false;
            }
            pwd=changeTo;
            mob.tell("Directory is now: /"+pwd);
            pwds.removeElement(mob);
            pwds.addElement(mob,pwd);
            return true;
        }
        case 5: // delete
        {
            break;
        }
        case 6: // edit
        {
            break;
        }
        case 7: // directory
        {
            DVector dir=Resources.allFilesInDir(mob,mob.location(),pwd);
            StringBuffer msg=new StringBuffer("\n^xFile list for directory: /"+pwd+"^.^N\n\r^y .\n\r");
            if(pwd.length()>0) msg.append("^y ..\n\r");
            for(int d=0;d<dir.size();d++)
            {
                int bits=((Integer)dir.elementAt(d,Resources.VFS_INFO_BITS+1)).intValue();
                if(Util.bset(bits,Resources.VFS_MASK_DIRECTORY))
                {
                    if(Util.bset(bits,Resources.VFS_MASK_NOTVFS))
                        msg.append(" ");
                    else
                    if(Util.bset(bits,Resources.VFS_MASK_ALSONOTVFS))
                        msg.append("^R+");
                    else
                        msg.append("^r-");
                    msg.append("^y"+Util.padRight((String)dir.elementAt(d,Resources.VFS_INFO_FILENAME+1),25));
                    msg.append("^w"+Util.padRight(IQCalendar.d2String(((Long)dir.elementAt(d,Resources.VFS_INFO_DATE+1)).longValue()),20));
                    msg.append("^w"+Util.padRight((String)dir.elementAt(d,Resources.VFS_INFO_WHOM+1),20));
                    msg.append("\n\r");
                }
            }
            for(int d=0;d<dir.size();d++)
            {
                int bits=((Integer)dir.elementAt(d,Resources.VFS_INFO_BITS+1)).intValue();
                if(!Util.bset(bits,Resources.VFS_MASK_DIRECTORY))
                {
                    if(Util.bset(bits,Resources.VFS_MASK_NOTVFS))
                        msg.append(" ");
                    else
                    if(Util.bset(bits,Resources.VFS_MASK_ALSONOTVFS))
                        msg.append("^R+");
                    else
                        msg.append("^r-");
                    msg.append("^w"+Util.padRight((String)dir.elementAt(d,Resources.VFS_INFO_FILENAME+1),25));
                    msg.append("^w"+Util.padRight(IQCalendar.d2String(((Long)dir.elementAt(d,Resources.VFS_INFO_DATE+1)).longValue()),20));
                    msg.append("^w"+Util.padRight((String)dir.elementAt(d,Resources.VFS_INFO_WHOM+1),20));
                    msg.append("\n\r");
                }
            }
            if(mob.session()!=null)
                mob.session().colorOnlyPrintln(msg.toString());
            break;
        }
        case 8: // makedirectory
        {
            break;
        }
        case 9: // type
        {
            Vector V=getFileData(mob,mob.location(),pwd,Util.combine(commands,1));
            if(V==null)
            {
                mob.tell("^xError: file does not exist!^N");
                return false;
            }
            String fullPath=(String)V.elementAt(0);
            fullPath="/"+(String)V.elementAt(1);
            while(fullPath.startsWith("/"))
                fullPath=fullPath.substring(1);
            if(!CMSecurity.canAccessFile(mob,mob.location(),fullPath,Resources.isVFSFile(fullPath)))
            {
                mob.tell("^xError: access denied!^N");
                return false;
            }
            StringBuffer buf=Resources.getFile(fullPath,false);
            if((buf==null)||(buf.length()==0))
            {
                mob.tell("^xError: file is empty or doesn't exist!^N");
                return false;
            }
            if(mob.session()!=null) mob.session().colorOnlyPrintln(buf.toString());
            break;
        }
        default:
            mob.tell("'"+first+"' is an unknown command.  Valid commands are: "+allcmds.toString()+"and VFS alone to check your current directory.");
            return false;
        }
		return true;
	}

    public Vector getFileData(MOB mob, Room room, String pwd, String path)
    {
        if(path.startsWith("./"))
            path=path.substring(2);
        if(path.startsWith(".."))
        {
            while(path.startsWith(".."))
            {
                if(pwd.trim().length()==0)
                    return null;
                path=path.substring(2);
                int x=pwd.lastIndexOf("/");
                if(x>=0)
                    path=pwd.substring(0,x)+path;
            }
        }
        else
        if(!path.startsWith("/"))
            path=pwd+"/"+path;
        while(path.startsWith("/"))
            path=path.substring(1);
        int x=path.lastIndexOf("/");
        String file=(x>=0)?path.substring(x+1):path;
        path=(x>=0)?path.substring(0,x):"";
        path=Resources.fixFilename(path);
        if(!CMSecurity.canTraverseDir(mob,room,path))
            return null;
        DVector dir=Resources.allFilesInDir(mob,room,path);
        for(int d=0;d<dir.size();d++)
            if(((String)dir.elementAt(d,Resources.VFS_INFO_FILENAME+1)).equalsIgnoreCase(file))
                return Util.makeVector(path,file,dir.elementAt(d,Resources.VFS_INFO_BITS+1));
        return null;
    }
    
    public int ticksToExecute(){return 0;}
    public boolean canBeOrdered(){return false;}
    public boolean securityCheck(MOB mob){return CMSecurity.hasAccessibleDir(mob,null);}
    public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}