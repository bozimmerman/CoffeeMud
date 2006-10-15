package com.planet_ink.coffee_mud.Libraries;
import java.util.Vector;
import com.planet_ink.coffee_mud.Common.interfaces.Session;
import com.planet_ink.coffee_mud.Libraries.interfaces.SessionsList;
import com.planet_ink.coffee_mud.core.CMLib;

/* 
   Copyright 2000-2006 Bo Zimmerman

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
public class Sessions extends StdLibrary implements SessionsList
{
    public String ID(){return "Sessions";}
    public Vector all=new Vector();
	public Session elementAt(int x)
	{
		return (Session)all.elementAt(x);
	}
	public int size()
	{
		return all.size();
	}
	public void addElement(Session S)
	{
		all.addElement(S);
	}
	public void removeElementAt(int x)
	{
		all.removeElementAt(x);
	}
	public void removeElement(Session S)
	{
		all.removeElement(S);
	}
    public void stopSessionAtAllCosts(Session S)
    {
        if(S==null) return;
        S.logoff(true);
        try{Thread.sleep(10);}catch(Exception e){}
        int tries=100;
        while((S.getStatus()!=Session.STATUS_LOGOUTFINAL)
        &&((--tries)>=0))
        {
            S.logoff(true);
            try{Thread.sleep(100);}catch(Exception e){}
        }
        removeElement(S);
    }
}
