package com.planet_ink.coffee_mud.Common.interfaces;
import java.util.Vector;

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
public interface Poll extends CMCommon
{

    public static class PollOption
    {
        public String text="";
    }
    
    public static class PollResult
    {
        public String user="";
        public String ip="";
        public String answer="";
    }
    public static final int FLAG_ACTIVE=1;
    public static final int FLAG_PREVIEWRESULTS=2;
    public static final int FLAG_ABSTAIN=4;
    public static final int FLAG_VOTEBYIP=8;
    public static final int FLAG_HIDERESULTS=16;
    public static final int FLAG_NOTATLOGIN=32;
    
    public String getName();
    public void setName(String newname);
    public String getSubject();
    public void setSubject(String newsubject);
    public String getDescription();
    public void setDescription(String newdescription);
    public String getAuthor();
    public void setAuthor(String newname);
    public long getFlags();
    public void setFlags(long flag);
    public String getQualZapper();
    public void setQualZapper(String newZap);
    public long getExpiration();
    public void setExpiration(long time);
    public Vector getOptions();
    public void setOptions(Vector V);
    public Vector getResults();
    public void setResults(Vector V);
    public String getOptionsXML();
    public String getResultsXML();
    public void dbcreate();
    public void dbupdateresults();
    public void dbupdateall(String oldName);
    public boolean loaded();
    public void dbdelete();
    public boolean dbloadbyname();
    public PollResult getMyVote(MOB mob);
    public void addVoteResult(PollResult R);
    public boolean mayIVote(MOB mob);
    public boolean mayISeeResults(MOB mob);
    public void processVote(MOB mob);
    public void modifyVote(MOB mob) throws java.io.IOException;
    public void processResults(MOB mob);
}
