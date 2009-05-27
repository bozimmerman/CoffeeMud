package com.planet_ink.coffee_mud.Libraries.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
public interface CMLibrary extends CMObject
{
    public boolean activate();
    public boolean shutdown();
    public void propertiesLoaded();
    public ThreadEngine.SupportThread getSupportThread();
}
