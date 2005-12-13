package com.planet_ink.coffee_mud.Commands;
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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;


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
            {"$","DIRECTORY","LS"},
            {">","COPY","CP"},
            {".","CHANGEDIRECTORY","CD","GO"},
            {"-","DELETE","RM","RD"},
            {"\\","TYPE","TP"},
            {"+","MAKEDIRECTORY","MKDIR","MD"},
            //{"/","EDIT"},
            //{"?","COMPAREFILES","DIFF","CF"},
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
        case 0: // directory
        {
            CMFile[] dirs=CMFile.getFileList(pwd,Util.combine(commands,1),mob);
            if(dirs==null)
            {
                mob.tell("^xError: invalid directory!^N");
                return false;
            }
            StringBuffer msg=new StringBuffer("\n\r^y .\n\r^y ..\n\r");
            for(int d=0;d<dirs.length;d++)
            {
                CMFile entry=dirs[d];
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
                    msg.append("^y"+Util.padRight(entry.getName(),25));
                    msg.append("^w"+Util.padRight(CMLib.time().date2String(entry.lastModified()),20));
                    msg.append("^w"+Util.padRight(entry.author(),20));
                    msg.append("\n\r");
                }
            }
            for(int d=0;d<dirs.length;d++)
            {
                CMFile entry=dirs[d];
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
                    msg.append("^w"+Util.padRight(entry.getName(),25));
                    msg.append("^w"+Util.padRight(CMLib.time().date2String(entry.lastModified()),20));
                    msg.append("^w"+Util.padRight(entry.author(),20));
                    msg.append("\n\r");
                }
            }
            if(mob.session()!=null)
                mob.session().colorOnlyPrintln(msg.toString());
            break;
        }
        case 1: // copy
        {
            if(commands.size()==2)
                commands.addElement(".");
            if(commands.size()<3)
            {
                mob.tell("^xError: source and destination must be specified!^N");
                return false;
            }
            String source=(String)commands.elementAt(1);
            String target=Util.combine(commands,2);
            CMFile[] dirs=CMFile.getFileList(pwd,source,mob);
            if(dirs==null)
            {
                mob.tell("^xError: invalid source!^N");
                return false;
            }
            if(dirs.length==0)
            {
                mob.tell("^xError: no source files matched^N");
                return false;
            }
            CMFile DD=new CMFile(pwd,target,mob,false);
            for(int d=0;d<dirs.length;d++)
            {
                CMFile SF=dirs[d];
                if((SF==null)||(!SF.exists())){ mob.tell("^xError: source "+desc(SF)+" does not exist!^N"); return false;}
                if(!SF.canRead()){mob.tell("^xError: access denied to source "+desc(SF)+"!^N"); return false;}
                if(SF.isDirectory())
                {
                    if(dirs.length==1)
                    {
                        mob.tell("^xError: source can not be a directory!^N"); 
                        return false;
                    }
                    continue;
                }
                CMFile DF=DD;
                target=DD.getVFSPathAndName();
                if(DD.isDirectory())
                {
                    if(target.length()>0) 
                        target=target+"/"+SF.getName();
                    else
                        target=SF.getName();
                    target=(DD.isLocalFile())?"//"+target:"::"+target;
                    DF=new CMFile(target,mob,false);
                }
                else
                if(dirs.length>1)
                {
                    mob.tell("^xError: destination must be a directory!^N"); 
                    return false;
                }
                if(DF.canRead()){ mob.tell("^xError: destination "+desc(DF)+" already exists!^N"); return false;}
                if(!DF.canWrite()){ mob.tell("^xError: access denied to destination "+desc(DF)+"!^N"); return false;}
                byte[] O=SF.raw();
                if(O.length==0){ mob.tell("^xWarning: "+desc(SF)+" file had no data^N");}
                if(!DF.saveRaw(O))
                    mob.tell("^xWarning: write failed to "+desc(DF)+" ^N");
                else
                    mob.tell(desc(SF)+" copied to "+desc(DF));
            }
            break;
        }
        case 2: // cd
        {
            CMFile newDir=new CMFile(pwd,Util.combine(commands,1),mob,false);
            String changeTo=newDir.getVFSPathAndName();
            if(!newDir.exists())
            {
                mob.tell("^xError: Directory '"+Util.combine(commands,1)+"' does not exist.^N");
                return false;
            }
            if((!newDir.canRead())||(!newDir.isDirectory()))
            {
                mob.tell("^xError: You are not authorized enter that directory.^N");
                return false;
            }
            pwd=changeTo;
            mob.tell("Directory is now: /"+pwd);
            pwds.removeElement(mob);
            pwds.addElement(mob,pwd);
            return true;
        }
        case 3: // delete
        {
            CMFile[] dirs=CMFile.getFileList(pwd,Util.combine(commands,1),mob);
            if(dirs==null)
            {
                mob.tell("^xError: invalid filename!^N");
                return false;
            }
            if(dirs.length==0)
            {
                mob.tell("^xError: no files matched^N");
                return false;
            }
            for(int d=0;d<dirs.length;d++)
            {
                CMFile CF=dirs[d];
                if((CF==null)||(!CF.exists()))
                {
                    mob.tell("^xError: "+desc(CF)+"does not exist!^N");
                    return false;
                }
                if(!CF.canWrite())
                {
                    mob.tell("^xError: access denied to "+desc(CF)+"!^N");
                    return false;
                }
                if(!CF.delete())
                {
                    mob.tell("^xError: delete of "+desc(CF)+" failed.  If this is a directory, are you sure it's empty?^N");
                    return false;
                }
                mob.tell(desc(CF)+" deleted.");
            }
            break;
        }
        case 4: // type
        {
            CMFile[] dirs=CMFile.getFileList(pwd,Util.combine(commands,1),mob);
            if(dirs==null)
            {
                mob.tell("^xError: invalid filename!^N");
                return false;
            }
            if(dirs.length==0)
            {
                mob.tell("^xError: no files matched^N");
                return false;
            }
            for(int d=0;d<dirs.length;d++)
            {
                CMFile CF=dirs[d];
                if((CF==null)||(!CF.exists()))
                {
                    mob.tell("^xError: file does not exist!^N");
                    return false;
                }
                if(!CF.canRead())
                {
                    mob.tell("^xError: access denied!^N");
                    return false;
                }
                if(mob.session()!=null)
                {
                    mob.session().rawPrintln("\n\r^xFile /"+CF.getVFSPathAndName()+"^.^N");
                    mob.session().rawPrint(CF.text().toString(),25);
                }
            }
            break;
        }
        case 5: // makedirectory
        {
            CMFile CF=new CMFile(pwd,Util.combine(commands,1),mob,false);
            if(CF.exists())
            {
                mob.tell("^xError: file already exists!^N");
                return false;
            }
            if(!CF.canWrite())
            {
                mob.tell("^xError: access denied!^N");
                return false;
            }
            if(!CF.mkdir())
            {
                mob.tell("^xError: makedirectory failed.^N");
                return false;
            }
            mob.tell("Directory '/"+CF.getAbsolutePath()+"' created.");
            break;
        }
        case 6: // edit
        {
            mob.tell("^xNot yet implemented.^N");
            return false;
        }
        case 7: // compare files
        {
            mob.tell("^xNot yet implemented.^N");
            return false;
        }
        default:
            mob.tell("'"+first+"' is an unknown command.  Valid commands are: "+allcmds.toString()+"and VFS alone to check your current directory.");
            return false;
        }
		return true;
	}
    
    public String desc(CMFile CF){ return (CF.isLocalFile()?"Local file ":"VFS file ")+"'/"+CF.getVFSPathAndName()+"'";}
    public int ticksToExecute(){return 0;}
    public boolean canBeOrdered(){return false;}
    public boolean securityCheck(MOB mob){return CMSecurity.hasAccessibleDir(mob,null);}
    public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}