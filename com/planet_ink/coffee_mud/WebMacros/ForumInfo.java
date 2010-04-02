package com.planet_ink.coffee_mud.WebMacros;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.JournalsLibrary.ForumJournalFlags;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;

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
public class ForumInfo extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		String last=httpReq.getRequestParameter("JOURNAL");
		if(last==null) 
			return " @break@";
		
		MOB M = Authenticate.getAuthenticatedMob(httpReq);
		if((CMLib.journals().isArchonJournalName(last))&&((M==null)||(!CMSecurity.isASysOp(M))))
		    return " @break@";
		JournalsLibrary.ForumJournal journal = CMLib.journals().getForumJournal(last);
		if(journal == null) 
			return " @break@";
		
		if(parms.containsKey("CANADMIN")||parms.containsKey("ISADMIN"))
			return ""+journal.authorizationCheck(M, ForumJournalFlags.ADMIN);
		
		if(parms.containsKey("CANPOST"))
			return ""+journal.authorizationCheck(M, ForumJournalFlags.POST);
		
		if(parms.containsKey("CANREAD"))
			return ""+journal.authorizationCheck(M, ForumJournalFlags.READ);
		
		if(parms.containsKey("CANREPLY"))
			return ""+journal.authorizationCheck(M, ForumJournalFlags.REPLY);
		
		if(parms.containsKey("ADMINMASK"))
			return ""+journal.adminMask();
		
		if(parms.containsKey("READMASK"))
			return ""+journal.readMask();
		
		if(parms.containsKey("POSTMASK"))
			return ""+journal.postMask();
		
		if(parms.containsKey("REPLYMASK"))
			return ""+journal.replyMask();
		
		JournalsLibrary.JournalSummaryStats stats = CMLib.journals().getJournalStats(last);
		if(journal == null) 
			return " @break@";
		
		if(parms.containsKey("POSTS"))
			return ""+stats.posts;
		
		if(parms.containsKey("THREADS"))
			return ""+stats.threads;
		
		if(parms.containsKey("SHORTDESC"))
			return ""+stats.shortIntro;
		
		if(parms.containsKey("LONGDESC"))
			return ""+stats.longIntro;
		
		if(parms.containsKey("IMAGEPATH"))
		{
			if((stats.imagePath==null)
			||(stats.imagePath.trim().length()==0))
				return "images/lilcm.jpg";
			return ""+stats.threads;
		}
		
		return "";
	}
}
