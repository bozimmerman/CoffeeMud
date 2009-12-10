package com.planet_ink.coffee_mud.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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
public class DefaultPoll implements Poll
{
    public String ID(){return "DefaultPoll";}
    public CMObject newInstance(){try{return (CMObject)getClass().newInstance();}catch(Exception e){return new DefaultPoll();}}
    public void initializeClass(){}
    public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
    public CMObject copyOf()
    {
        try
        {
            return (Clan)this.clone();
        }
        catch(CloneNotSupportedException e)
        {
            return newInstance();
        }
    }
    public boolean loaded=false;
    protected String name="POLL";
    protected String subject="Poll Results Title";
    protected String description="This is a Poll! Choose from the following:";
    protected String author="noone";
    protected long expiration=0;
    protected long bitmap=0;
    protected String qualZapper="";
    protected Vector options=new Vector();
    protected Vector results=new Vector();
    
    public boolean loaded(){return loaded;}
    public void setLoaded(boolean truefalse){ loaded=truefalse;}
    
    public String getName(){return name;}
    public void setName(String newname){name=newname;}
    
    public String getSubject(){return subject;}
    public void setSubject(String newsubject){subject=newsubject;}
    
    public String getDescription(){return description;}
    public void setDescription(String newdescription){description=newdescription;}
    
    public String getAuthor(){return author;}
    public void setAuthor(String newname){author=newname;}
    
    public long getFlags(){return bitmap;}
    public void setFlags(long flag){bitmap=flag;}
    
    public String getQualZapper(){return qualZapper;}
    public void setQualZapper(String newZap){qualZapper=newZap;}
    
    public long getExpiration(){return expiration;}
    public void setExpiration(long time){expiration=time;}
    
    public Vector getOptions(){return options;}
    public void setOptions(Vector V){ options=V;}
    
    public Vector getResults(){return results;}
    public void setResults(Vector V){results=V;}
    
    public String getOptionsXML()
    {
        if(options.size()==0) return "<OPTIONS />";
        StringBuffer str=new StringBuffer("<OPTIONS>");
        PollOption PO=null;
        for(int i=0;i<options.size();i++)
        {
            PO=(PollOption)options.elementAt(i);
            str.append("<OPTION>");
            str.append(CMLib.xml().convertXMLtoTag("TEXT",CMLib.xml().parseOutAngleBrackets(PO.text)));
            str.append("</OPTION>");
        }
        str.append("</OPTIONS>");        
        return str.toString();
    }
    
    public String getResultsXML()
    {
        if(results.size()==0) return "<RESULTS />";
        StringBuffer str=new StringBuffer("<RESULTS>");
        PollResult PR=null;
        for(int i=0;i<results.size();i++)
        {
            PR=(PollResult)results.elementAt(i);
            str.append("<RESULT>");
            str.append(CMLib.xml().convertXMLtoTag("USER",PR.user));
            str.append(CMLib.xml().convertXMLtoTag("IP",PR.ip));
            str.append(CMLib.xml().convertXMLtoTag("ANS",PR.answer));
            str.append("</RESULT>");
        }
        str.append("</RESULTS>");
        return str.toString();
    }
    
    public PollResult getMyVote(MOB mob)
    {
        if(mob==null) return null;
        CMLib.polls().loadPollIfNecessary(this);
        PollResult R=null;
        Session S=mob.session();
        for(int r=0;r<results.size();r++)
        {
            R=(PollResult)results.elementAt(r);
            if((mob.Name().equals(R.user)))
                return R;
            if((R.ip.length()>0)&&(S!=null)&&(S.getAddress().equals(R.ip)))
                return R;
        }
        return null;
    }
    
    public void addVoteResult(PollResult R)
    {
        CMLib.polls().loadPollIfNecessary(this);
        results.addElement(R);
        CMLib.polls().updatePollResults(this);
    }
    
    public boolean mayIVote(MOB mob)
    {
        if(mob==null) return false;
        if(!CMath.bset(bitmap,FLAG_ACTIVE))
            return false;
        if(!CMLib.masking().maskCheck(qualZapper,mob,true))
            return false;
        if((expiration>0)&&(System.currentTimeMillis()>expiration))
        {
            bitmap=CMath.unsetb(bitmap,FLAG_ACTIVE);
            CMLib.polls().updatePoll(name, this);
            return false;
        }
        if(getMyVote(mob)!=null) return false;
        return true;
    }
    
    public boolean mayISeeResults(MOB mob)
    {
        if(mob==null) return false;
        if(!CMLib.masking().maskCheck(qualZapper,mob,true))
            return false;
        if(CMath.bset(bitmap,FLAG_HIDERESULTS)&&(!CMSecurity.isAllowedAnywhere(mob,"POLLS")))
            return false;
        if(CMath.bset(bitmap,FLAG_PREVIEWRESULTS))
            return true;
        if((expiration>0)
        &&(System.currentTimeMillis()<expiration))
            return false;
        if((getMyVote(mob)==null)&&(!CMath.bset(bitmap,FLAG_ABSTAIN))) 
            return false;
        return true;
    }
}
