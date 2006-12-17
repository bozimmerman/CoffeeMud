package com.planet_ink.coffee_mud.Libraries;

import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.core.CMClass;
import com.planet_ink.coffee_mud.core.Log;
import com.planet_ink.coffee_mud.core.interfaces.*;

public class StdLibrary implements CMLibrary
{
    public String ID(){return "StdLibrary";}
    public CMObject newInstance()
    {
        try
        {
            return (CMObject)this.getClass().newInstance();
        }
        catch(Exception e)
        {
            Log.errOut(ID(),e);
        }
        return new StdLibrary();
    }
    public CMObject copyOf(){try{return (CMObject)this.clone();}catch(Exception e){return newInstance();}}
    public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
    public void initializeClass(){}
}
