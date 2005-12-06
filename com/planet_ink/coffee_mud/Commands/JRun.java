package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;
import org.mozilla.javascript.*;

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
public class JRun extends StdCommand
{
    public JRun(){}

    private String[] access={"JRUN"};
    public String[] getAccessWords(){return access;}
    public boolean execute(MOB mob, Vector commands)
        throws java.io.IOException
    {
        if(commands.size()<2)
        {
            mob.tell("jrun filename1 parm1 parm2 ...");
            return false;
        }
        commands.removeElementAt(0);

        String fn = new String ( (String)commands.elementAt(0) );
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
            String[] names = { "mob", "numParms", "getParm", "getParms", "toJavaString"};
            scope.defineFunctionProperties(names, JScriptWindow.class,
                                           ScriptableObject.DONTENUM);
            cx.evaluateString(scope, ft.toString(),"<cmd>", 1, null);
        }
        catch(Exception e)
        {
            if(e!=null)
                mob.tell("JavaScript error: "+e.getMessage());
            else
                mob.tell("JavaScript error: unknown");
        }
        Context.exit();
        return false;
    }
    
    protected class JScriptWindow extends ScriptableObject
    {
        public String getClassName(){ return "window";}
        static final long serialVersionUID=43;
        MOB s=null;
        Vector v=null;
        public MOB mob(){return s;}
        public int numParms(){return (v==null)?0:v.size();}
        public String toJavaString(Object O){return Context.toString(O);}
        public String getParm(int i)
        {
            if(v==null) return "";
            if((i<0)||(i>=v.size())) return "";
            return (String)v.elementAt(i);
        }
        public String getParms(){return (v==null)?"":Util.combineWithQuotes(v,0);}
        public JScriptWindow(MOB executor, Vector parms){s=executor; v=parms;}
    }
    
    public int ticksToExecute(){return 0;}
    public boolean canBeOrdered(){return true;}
    public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,mob.location(),"JSCRIPTS");}

    public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
    
}
