package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


/* 
   Copyright 2000-2004 Bo Zimmerman

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
public class Song_SingleMindedness extends Song
{
	public String ID() { return "Song_SingleMindedness"; }
	public String name(){ return "Single Mindedness";}
	public int quality(){ return MALICIOUS;}
	protected CMMsg themsg=null;

	public void executeMsg(Environmental ticking, CMMsg msg)
	{
		super.executeMsg(ticking,msg);
		if((themsg==null)
		&&(msg.source()!=invoker())
		&&(msg.source()==affected)
		&&(msg.sourceMessage()!=null)
		&&(msg.sourceMessage().length()>0)
		&&(!Util.bset(msg.sourceCode(),CMMsg.MASK_GENERAL)))
			themsg=msg;
	}

	public boolean okMessage(Environmental ticking, CMMsg msg)
	{
		if((themsg!=null)
		&&(msg.source()!=invoker())
		&&(msg.source()==affected)
		&&(!Util.bset(msg.sourceCode(),CMMsg.MASK_GENERAL))
		&&(themsg.sourceMinor()!=msg.sourceMinor()))
		{
			msg.source().tell(msg.source(),null,null,"The only thing you have a mind to do is '"+themsg.sourceMessage()+"'.");
			return false;
		}
		return super.okMessage(ticking,msg);
	}
}
