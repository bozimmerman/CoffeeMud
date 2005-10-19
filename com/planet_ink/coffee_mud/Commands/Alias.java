package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
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
public class Alias extends StdCommand
{
    private String[] access={"ALIAS"};
    public String[] getAccessWords(){return access;}
    public boolean execute(MOB mob, Vector commands)
        throws java.io.IOException
    {
        if((mob.playerStats()==null)||(mob.session()==null))
            return false;
        PlayerStats ps=mob.playerStats();
        while((mob.session()!=null)&&(!mob.session().killFlag()))
        {
            StringBuffer menu=new StringBuffer("^xAlias definitions:^.^?\n\r");
            String[] aliasNames=ps.getAliasNames();
            for(int i=0;i<aliasNames.length;i++)
                menu.append(Util.padRight((i+1)+". "+aliasNames[i],15)+": "+ps.getAlias(aliasNames[i])+"\n\r");
            menu.append((aliasNames.length+1)+". Add a new alias\n\r");
            mob.tell(menu.toString());
            String which=mob.session().prompt("Enter a selection: ","");
            if(which.length()==0)
                break;
            int num=Util.s_int(which);
            String selection=null;
            if((num>0)&&(num<=(aliasNames.length)))
            {
                selection=aliasNames[num-1];
                if(mob.session().choose("\n\rAlias selected '"+selection+"'.\n\rWould you like to D)elete or M)odify this alias (d/M)? ","MD","M").equals("D"))
                {
                    ps.delAliasName(selection);
                    mob.tell("Alias deleted.");
                    selection=null;
                }
            }
            else
            if(num<=0)
                break;
            else
            {
               selection=mob.session().prompt("Enter a new alias string consisting of letters and numbers only.\n\r: ","").trim().toUpperCase();
               if(selection.length()==0)
                   selection=null;
               else
               if(ps.getAlias(selection).length()>0)
               {
                   selection=null;
                   mob.tell("That alias already exists.  Select it from the menu to delete or modify.");
               }
               else
               {
                   for(int i=0;i<selection.length();i++)
                       if(!Character.isLetterOrDigit(selection.charAt(i)))
                       {
                           selection=null;
                           break;
                       }
                   if(selection==null)
                       mob.tell("Your alias name may only contain letters and numbers without spaces. ");
                   else
                       ps.addAliasName(selection);
               }
            }
            if(selection!=null)
            {
                mob.session().rawPrintln("Enter a value for alias '"+selection+"'.  Use ~ to separate commands.");
                String value=mob.session().prompt(": ","").trim();
                value=Util.replaceAll(value,"<","");
                value=Util.replaceAll(value,"&","");
                if((value.length()==0)&&(ps.getAlias(selection).length()>0))
                    mob.tell("(No change)");
                else
                if(value.length()==0)
                {
                    mob.tell("Aborted.");
                    ps.delAliasName(selection);
                }
                else
                {
                    ps.setAlias(selection,value);
                    mob.tell("The alias was successfully changed.");
                }
            }
        }
        return true;
    }
    public int ticksToExecute(){return 0;}
    public boolean canBeOrdered(){return true;}

    public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}

