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
import org.mozilla.javascript.*;

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
public class JRun extends StdCommand
{
    public JRun(){}

    private String[] access={"JRUN"};
    public String[] getAccessWords(){return access;}
    public boolean execute(MOB mob, Vector commands, int metaFlags)
        throws java.io.IOException
    {
        if(commands.size()<2)
        {
            mob.tell("jrun filename1 parm1 parm2 ...");
            return false;
        }
        commands.removeElementAt(0);

        String fn = (String)commands.elementAt(0);
        StringBuffer ft = new CMFile(fn,mob,true).text();
        if((ft==null)||(ft.length()==0))
        {
            mob.tell("File '"+fn+"' could not be found.");
            return false;
        }
        commands.removeElementAt(0);
        Context cx = Context.enter();
        try
        {
            JScriptWindow scope = new JScriptWindow(mob,commands);
            cx.initStandardObjects(scope);
            scope.defineFunctionProperties(JScriptWindow.functions,
                                           JScriptWindow.class,
                                           ScriptableObject.DONTENUM);
            cx.evaluateString(scope, ft.toString(),"<cmd>", 1, null);
        }
        catch(Exception e)
        {
            mob.tell("JavaScript error: "+e.getMessage());
        }
        Context.exit();
        return false;
    }

    protected static class JScriptWindow extends ScriptableObject
    {
        public String getClassName(){ return "JScriptWindow";}
        static final long serialVersionUID=45;
        MOB s=null;
        Vector v=null;
        public MOB mob(){return s;}
        public int numParms(){return (v==null)?0:v.size();}
        public String getParm(int i)
        {
            if(v==null) return "";
            if((i<0)||(i>=v.size())) return "";
            return (String)v.elementAt(i);
        }
        public static String[] functions = { "mob", "numParms", "getParm", "getParms", "toJavaString"};
        public String getParms(){return (v==null)?"":CMParms.combineWithQuotes(v,0);}
        public JScriptWindow(MOB executor, Vector parms){s=executor; v=parms;}
        public String toJavaString(Object O){return Context.toString(O);}
    }


    public boolean canBeOrdered(){return false;}
    public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,mob.location(),"JSCRIPTS");}

    public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}

}
