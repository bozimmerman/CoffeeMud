package com.planet_ink.coffee_mud.Libraries;

import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.core.CMClass;
import com.planet_ink.coffee_mud.core.Log;
import com.planet_ink.coffee_mud.core.interfaces.*;

/*
Copyright 2008-2013 Bo Zimmerman

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
public class StdLibrary implements CMLibrary, Tickable
{
	public String ID(){return "StdLibrary";}
	protected String name=ID();
	public String name(){return name; }
	protected long tickStatus=Tickable.STATUS_NOT;
	protected TickClient serviceClient=null; 
	public CMObject newInstance()
	{
		try
		{
			return this.getClass().newInstance();
		}
		catch(Exception e)
		{
			Log.errOut(ID(),e);
		}
		return new StdLibrary();
	}
	public CMObject copyOf(){try{return (CMObject)this.clone();}catch(Exception e){return newInstance();}}
	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
	public void initializeClass(){}
	public boolean activate(){ return true;}
	public void propertiesLoaded(){ }
	public boolean shutdown(){ return true;}
	public TickClient getServiceClient() { return serviceClient;}
	protected boolean isDebugging = false;
	public void setThreadStatus(TickClient C, String msg)
	{
		if(C!=null)
		{
			C.setStatus(msg);
			if(isDebugging)
				Log.debugOut(C.getName(),msg);
		}
	}
	@Override
	public long getTickStatus() {
		return Tickable.STATUS_NOT;
	}
	@Override
	public boolean tick(Tickable ticking, int tickID) {
		return false;
	}
}
