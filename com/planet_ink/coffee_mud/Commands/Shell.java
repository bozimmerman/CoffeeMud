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
            {"*","FINDFILE","FF"},
            {"&","SEARCHTEXT","GREP","ST"},
            {"/","EDIT"},
            //{"?","COMPAREFILES","DIFF","CF"},
    };
    
    protected final static String[] badTextExtensions={
        ".ZIP",".JPE",".JPG",".GIF",".CLASS",".WAV",".BMP",".JPEG",".GZ",".TGZ",".JAR"
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
            CMFile[] dirs=CMFile.getFileList(pwd,CMParms.combine(commands,1),mob,false);
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
                    msg.append("^y"+CMStrings.padRight(entry.getName(),25));
                    msg.append("^w"+CMStrings.padRight(CMLib.time().date2String(entry.lastModified()),20));
                    msg.append("^w"+CMStrings.padRight(entry.author(),20));
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
                    msg.append("^w"+CMStrings.padRight(entry.getName(),25));
                    msg.append("^w"+CMStrings.padRight(CMLib.time().date2String(entry.lastModified()),20));
                    msg.append("^w"+CMStrings.padRight(entry.author(),20));
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
            String target=CMParms.combine(commands,2);
            CMFile[] dirs=CMFile.getFileList(pwd,source,mob,false);
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
            if((dirs.length==1)&&(!target.trim().startsWith("::")&&(!target.trim().startsWith("//"))))
                target=(dirs[0].isLocalFile())?"//"+target.trim():"::"+target.trim();
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
            CMFile newDir=new CMFile(pwd,CMParms.combine(commands,1),mob,false);
            String changeTo=newDir.getVFSPathAndName();
            if(!newDir.exists())
            {
                mob.tell("^xError: Directory '"+CMParms.combine(commands,1)+"' does not exist.^N");
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
            CMFile[] dirs=CMFile.getFileList(pwd,CMParms.combine(commands,1),mob,false);
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
            CMFile[] dirs=CMFile.getFileList(pwd,CMParms.combine(commands,1),mob,false);
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
                    mob.session().colorOnlyPrintln("\n\r^xFile /"+CF.getVFSPathAndName()+"^.^N");
                    mob.session().rawPrint(CF.text().toString(),25);
                }
            }
            break;
        }
        case 5: // makedirectory
        {
            CMFile CF=new CMFile(pwd,CMParms.combine(commands,1),mob,false);
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
        case 6: // findfiles
        {
            String substring=CMParms.combine(commands,1).trim();
            if(substring.length()==0) substring="*";
            CMFile[] dirs=CMFile.getFileList(pwd,substring,mob,true);
            StringBuffer msg=new StringBuffer("");
            if(dirs.length==0)
            {
                mob.tell("^xError: no files matched^N");
                return false;
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
                    msg.append("^w"+entry.getVFSPathAndName());
                    msg.append("\n\r");
                }
            }
            if(mob.session()!=null)
                mob.session().colorOnlyPrintln(msg.toString());
            return false;
        }
        case 7: // searchtext
        {
            String substring=CMParms.combine(commands,1).trim();
            if(substring.length()==0)
            {
                mob.tell("^xError: you must specify a search string^N");
                return false;
            }
            CMFile[] dirs=CMFile.getFileList(pwd,"*",mob,true);
            if(dirs.length==0)
            {
                mob.tell("^xError: no files found!^N");
                return false;
            }
            mob.session().print("\n\rSearching...");
            substring=substring.toUpperCase();
            Vector dirs2=new Vector();
            for(int d=0;d<dirs.length;d++)
            {
                CMFile entry=dirs[d];
                if(!entry.isDirectory())
                {
                    boolean proceed=true;
                    for(int i=0;i<badTextExtensions.length;i++)
                        if(entry.getName().toUpperCase().endsWith(badTextExtensions[i]))
                        { proceed=false; break;}
                    if(proceed)
                    {
                        StringBuffer text=entry.textUnformatted();
                        if(text.toString().toUpperCase().indexOf(substring)>=0)
                            dirs2.addElement(entry);
                    }
                }
            }
            if(dirs2.size()==0)
            {
                mob.tell("\n\r^xError: no files matched^N");
                return false;
            }
            StringBuffer msg=new StringBuffer("\n\r");
            for(int d=0;d<dirs2.size();d++)
            {
                CMFile entry=(CMFile)dirs2.elementAt(d);
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
        case 8: // edit
        {
            CMFile file=new CMFile(pwd,CMParms.combine(commands,1),mob,false);
            if((!file.canWrite())
            ||(file.isDirectory()))
            {
                mob.tell("^xError: You are not authorized to create/modify that file.^N");
                return false;
            }
            Vector vbuf=Resources.getFileLineVector(file.textUnformatted());
            mob.tell(desc(file)+" has been loaded.\n\r\n\r");
            final String help=
                "^HCoffeeMud Message Maker Options:^N\n\r"+
                "^XA)^.^Wdd new lines (go into ADD mode)\n\r"+
                "^XD)^.^Welete one or more lines\n\r"+
                "^XL)^.^Wist the entire text file\n\r"+
                "^XI)^.^Wnsert a line\n\r"+
                "^XE)^.^Wdit a line\n\r"+
                "^XR)^.^Weplace text in the file\n\r"+
                "^XS)^.^Wave the file\n\r"+
                "^XQ)^.^Wuit without saving";
            mob.tell("^HCoffeeMud Message Maker^N");
            boolean menuMode=true;
            while((mob.session()!=null)&&(!mob.session().killFlag()))
            {
                mob.session().setAfkFlag(false);
                if(!menuMode)
                {
                    String line=mob.session().prompt("^X"+CMStrings.padRight(""+vbuf.size(),3)+")^.^N ","");
                    if(line.trim().equals("."))
                        menuMode=true;
                    else
                        vbuf.addElement(line);
                }
                else
                {
                    String option=mob.session().choose("^HMenu ^N(?/A/D/L/I/E/R/S/Q)^H: ^N","ADLIESQ?","?");
                    switch(option.charAt(0))
                    {
                    case 'S':
                        if(mob.session().confirm("Save and exit, are you sure (N/y)? ","N"))
                        {
                            StringBuffer text=new StringBuffer("");
                            for(int i=0;i<vbuf.size();i++)
                                text.append(((String)vbuf.elementAt(i))+"\r\n");
                            if(file.saveText(text))
                                mob.tell("File saved.");
                            else
                                mob.tell("^XError: could not save the file!^N^.");
                            return true;
                        }
                        break;
                    case 'Q':
                        if(mob.session().confirm("Quit without saving (N/y)? ","N"))
                            return true;
                        break;
                    case 'R':
                    {
                        if(vbuf.size()==0)
                            mob.tell("The file is empty!");
                        else
                        {
                            String line=mob.session().prompt("Text to search for (case sensitive): ","");
                            if(line.length()>0)
                            {
                                String str=mob.session().prompt("Text to replace it with: ","");
                                for(int i=0;i<vbuf.size();i++)
                                    vbuf.setElementAt(CMStrings.replaceAll((String)vbuf.elementAt(i),line,str),i);
                            }
                            else
                                mob.tell("(aborted)");
                        }
                        break;
                    }
                    case 'E':
                    {
                        if(vbuf.size()==0)
                            mob.tell("The file is empty!");
                        else
                        {
                            String line=mob.session().prompt("Line to edit (0-"+(vbuf.size()-1)+"): ","");
                            if((CMath.isInteger(line))&&(CMath.s_int(line)>=0)&&(CMath.s_int(line)<(vbuf.size())))
                            {
                                int ln=CMath.s_int(line);
                                mob.tell("Current: \n\r"+CMStrings.padRight(""+ln,3)+") "+(String)vbuf.elementAt(ln));
                                String str=mob.session().prompt("Rewrite: \n\r");
                                vbuf.setElementAt(str,ln);
                            }
                            else
                                mob.tell("'"+line+"' is not a valid line number.");
                        }
                        break;
                    }
                    case 'D':
                    {
                        if(vbuf.size()==0)
                            mob.tell("The file is empty!");
                        else
                        {
                            String line=mob.session().prompt("Line to delete (0-"+(vbuf.size()-1)+"): ","");
                            if((CMath.isInteger(line))&&(CMath.s_int(line)>=0)&&(CMath.s_int(line)<(vbuf.size())))
                            {
                                int ln=CMath.s_int(line);
                                vbuf.removeElementAt(ln);
                                mob.tell("Line "+ln+" deleted.");
                            }
                            else
                                mob.tell("'"+line+"' is not a valid line number.");
                        }
                        break;
                    }
                    case '?': mob.tell(help); break;
                    case 'A': mob.tell("^ZYou are now in Add Text mode.\n\r^ZEnter . on a blank line to exit.^.^N"); 
                              menuMode=false;
                              break;
                    case 'L':
                    {
                        StringBuffer list=new StringBuffer("File: "+file.getVFSPathAndName()+"\n\r");
                        for(int v=0;v<vbuf.size();v++)
                            list.append(CMLib.coffeeFilter().colorOnlyFilter("^X"+CMStrings.padRight(""+v,3)+")^.^N ",mob.session())+(String)vbuf.elementAt(v)+"\n\r");
                        mob.session().rawPrint(list.toString(),25);
                        break;
                    }
                    case 'I':
                    {
                        if(vbuf.size()==0)
                            mob.tell("The file is empty!");
                        else
                        {
                            String line=mob.session().prompt("Line to insert before (0-"+(vbuf.size()-1)+"): ","");
                            if((CMath.isInteger(line))&&(CMath.s_int(line)>=0)&&(CMath.s_int(line)<(vbuf.size())))
                            {
                                int ln=CMath.s_int(line);
                                String str=mob.session().prompt("Enter text to insert here.\n\r: ");
                                vbuf.insertElementAt(str,ln);
                            }
                            else
                                mob.tell("'"+line+"' is not a valid line number.");
                        }
                        break;
                    }
                    }
                }
                    
            }
            return false;
        }
        case 9: // compare files
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