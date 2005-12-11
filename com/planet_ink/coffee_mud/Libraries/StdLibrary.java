package com.planet_ink.coffee_mud.Libraries;

import com.planet_ink.coffee_mud.Items.Basic.StdItem;
import com.planet_ink.coffee_mud.core.CMClass;
import com.planet_ink.coffee_mud.core.Log;
import com.planet_ink.coffee_mud.core.interfaces.CMObject;
import com.planet_ink.coffee_mud.core.intermud.IMudClient;

public class StdLibrary implements CMObject
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
        return new StdItem();
    }
    public CMObject copyOf(){try{return (CMObject)this.clone();}catch(Exception e){return newInstance();}}
    public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
