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
public class DefaultPoll implements Poll
{
    public String ID(){return "DefaultPoll";}
    public CMObject newInstance(){try{return (CMObject)getClass().newInstance();}catch(Exception e){return new DefaultPoll();}}
    public void initializeClass(){}
    public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
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
    
    public void dbcreate()
    {
        CMLib.polls().addPoll(this);
        CMLib.database().DBCreatePoll(name,author,subject,description,getOptionsXML(),(int)bitmap,qualZapper,getResultsXML(),expiration);
    }
    public void dbupdateresults()
    {
        CMLib.database().DBUpdatePollResults(name,getResultsXML());
    }
    public void dbupdateall(String oldName)
    {
        CMLib.database().DBUpdatePoll(oldName,name,author,subject,description,getOptionsXML(),(int)bitmap,qualZapper,getResultsXML(),expiration);
    }
    public void dbdelete()
    {
        CMLib.polls().removePoll(this);
        CMLib.database().DBDeletePoll(name);
    }
    public boolean dbloadbyname()
    {
        Vector V=CMLib.database().DBReadPoll(name);
        if((V==null)||(V.size()==0)) return false;
        name=(String)V.elementAt(0);
        author=(String)V.elementAt(1);
        subject=(String)V.elementAt(2);
        description=(String)V.elementAt(3);
        options=new Vector();
        String optionsXML=(String)V.elementAt(4);
        Vector V2=CMLib.xml().parseAllXML(optionsXML);
        XMLLibrary.XMLpiece OXV=CMLib.xml().getPieceFromPieces(V2,"OPTIONS");
        if((OXV!=null)&&(OXV.contents!=null)&&(OXV.contents.size()>0))
        for(int v2=0;v2<OXV.contents.size();v2++)
        {
            XMLLibrary.XMLpiece XP=(XMLLibrary.XMLpiece)OXV.contents.elementAt(v2);
            if(!XP.tag.equalsIgnoreCase("option"))
                continue;
            PollOption PO=new PollOption();
            PO.text=CMLib.xml().restoreAngleBrackets(CMLib.xml().getValFromPieces(XP.contents,"TEXT"));
            options.addElement(PO);
        }
        bitmap=((Long)V.elementAt(5)).longValue();
        qualZapper=(String)V.elementAt(6);
        results=new Vector();
        String resultsXML=(String)V.elementAt(7);
        V2=CMLib.xml().parseAllXML(resultsXML);
        OXV=CMLib.xml().getPieceFromPieces(V2,"RESULTS");
        if((OXV!=null)&&(OXV.contents!=null)&&(OXV.contents.size()>0))
        for(int v2=0;v2<OXV.contents.size();v2++)
        {
            XMLLibrary.XMLpiece XP=(XMLLibrary.XMLpiece)OXV.contents.elementAt(v2);
            if(!XP.tag.equalsIgnoreCase("result"))
                continue;
            PollResult PR=new PollResult();
            PR.user=CMLib.xml().getValFromPieces(XP.contents,"USER");
            PR.ip=CMLib.xml().getValFromPieces(XP.contents,"IP");
            PR.answer=CMLib.xml().getValFromPieces(XP.contents,"ANS");
            results.addElement(PR);
        }
        expiration=((Long)V.elementAt(8)).longValue();
        loaded=true;
        return true;
    }
    
    public PollResult getMyVote(MOB mob)
    {
        if(mob==null) return null;
        if(!loaded) dbloadbyname();
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
        if(!loaded) dbloadbyname();
        results.addElement(R);
        dbupdateresults();
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
            dbupdateall(name);
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
    public void processVote(MOB mob)
    {
        if(!mayIVote(mob)) 
            return;
        try
        {
            if((!loaded)&&(!dbloadbyname())) 
                return;
            StringBuffer present=new StringBuffer("");
            present.append("^O"+description+"^N\n\r\n\r");
            if(options.size()==0) 
            {
                mob.tell(present.toString()+"Oops! No options defined!");
                return;
            }
            PollOption PO=null;
            for(int o=0;o<options.size();o++)
            {
                PO=(PollOption)options.elementAt(o);
                present.append("^H"+CMStrings.padLeft(""+(o+1),2)+": ^N"+PO.text+"\n\r");
            }
            if(CMath.bset(bitmap,FLAG_ABSTAIN))
                present.append("^H  : ^NPress ENTER to abstain from voting.^?\n\r");
            
            mob.tell(present.toString());
            int choice=-1;
            while((choice<0)&&(mob.session()!=null)&&(!mob.session().killFlag()))
            {
                
                String s=mob.session().prompt("Please make your selection (1-"+options.size()+"): ");
                if((s.length()==0)&&(CMath.bset(bitmap,FLAG_ABSTAIN)))
                    break;
                if(CMath.isInteger(s)&&(CMath.s_int(s)>=1)&&(CMath.s_int(s)<=options.size()))
                    choice=CMath.s_int(s);
            }
            PollResult R=new PollResult();
            R.user=mob.Name();
            if(CMath.bset(bitmap,FLAG_VOTEBYIP))
                R.ip=mob.session().getAddress();
            R.answer=""+choice;
            addVoteResult(R);
        }
        catch(java.io.IOException x)
        {
        	if(Log.isMaskedErrMsg(x.getMessage()))
	            Log.errOut("Polls",x.getMessage());
        	else
	            Log.errOut("Polls",x);
        }
    }
    
    public void modifyVote(MOB mob) throws java.io.IOException
    {
        if((mob.isMonster())||(!CMSecurity.isAllowedAnywhere(mob,"POLLS")))
            return;
        boolean ok=false;
        int showFlag=-1;
        if(CMProps.getIntVar(CMProps.SYSTEMI_EDITORTYPE)>0)
            showFlag=-999;
        String oldName=name;
        while(!ok)
        {
            int showNumber=0;
            String possName=CMLib.english().promptText(mob,name,++showNumber,showFlag,"Name");
            while((!possName.equalsIgnoreCase(name))&&(CMLib.polls().getPoll(possName)!=null))
                possName=possName+"!";
            name=possName;
            description=CMLib.english().promptText(mob,description,++showNumber,showFlag,"Introduction");
            subject=CMLib.english().promptText(mob,subject,++showNumber,showFlag,"Results Header");
            if(subject.length()>250) subject=subject.substring(0,250);
            if(author.length()==0) author=mob.Name();
            qualZapper=CMLib.english().promptText(mob,qualZapper,++showNumber,showFlag,"Qual. Mask",true);
            bitmap=(CMLib.english().promptBool(mob,CMath.bset(bitmap,FLAG_ACTIVE),++showNumber,showFlag,"Poll Active"))?
                CMath.setb(bitmap,FLAG_ACTIVE):CMath.unsetb(bitmap,FLAG_ACTIVE);
            bitmap=(CMLib.english().promptBool(mob,CMath.bset(bitmap,FLAG_PREVIEWRESULTS),++showNumber,showFlag,"Preview Results"))?
                    CMath.setb(bitmap,FLAG_PREVIEWRESULTS):CMath.unsetb(bitmap,FLAG_PREVIEWRESULTS);
            bitmap=(CMLib.english().promptBool(mob,CMath.bset(bitmap,FLAG_ABSTAIN),++showNumber,showFlag,"Allow Abstention"))?
                    CMath.setb(bitmap,FLAG_ABSTAIN):CMath.unsetb(bitmap,FLAG_ABSTAIN);
            bitmap=(CMLib.english().promptBool(mob,CMath.bset(bitmap,FLAG_VOTEBYIP),++showNumber,showFlag,"Use IP Addresses"))?
                    CMath.setb(bitmap,FLAG_VOTEBYIP):CMath.unsetb(bitmap,FLAG_VOTEBYIP);
            bitmap=(CMLib.english().promptBool(mob,CMath.bset(bitmap,FLAG_HIDERESULTS),++showNumber,showFlag,"Hide Results"))?
                    CMath.setb(bitmap,FLAG_HIDERESULTS):CMath.unsetb(bitmap,FLAG_HIDERESULTS);
            bitmap=(CMLib.english().promptBool(mob,CMath.bset(bitmap,FLAG_NOTATLOGIN),++showNumber,showFlag,"POLL CMD only"))?
                    CMath.setb(bitmap,FLAG_NOTATLOGIN):CMath.unsetb(bitmap,FLAG_NOTATLOGIN);
            String expirationDate="NA";
            if(expiration>0) expirationDate=CMLib.time().date2String(expiration);
            
            expirationDate=CMLib.english().promptText(mob,expirationDate,++showNumber,showFlag,"Exp. Date (MM/DD/YYYY HH:MM AP)",true);
            if((expirationDate.trim().length()==0)||(expirationDate.equalsIgnoreCase("NA")))
                expiration=0;
            else
            { try{expiration=CMLib.time().string2Millis(expirationDate.trim());}catch(Exception e){}}
                    
            Vector del=new Vector();
            for(int i=0;i<options.size();i++)
            {
                PollOption PO=(PollOption)options.elementAt(i);
                PO.text=CMLib.english().promptText(mob,PO.text,++showNumber,showFlag,"Vote Option",true);
                if(PO.text.length()==0) del.addElement(PO);
            }
            for(int i=0;i<del.size();i++)
                options.removeElement(del.elementAt(i));
            
            PollOption PO=new PollOption();
            while((PO!=null)&&(!mob.session().killFlag()))
            {
                PO=new PollOption();
                PO.text=CMLib.english().promptText(mob,PO.text,++showNumber,showFlag,"New Vote Option",true);
                if(PO.text.length()==0) 
                    break;
                options.addElement(PO);
            }
            if(showFlag<-900){ ok=true; break;}
            if(showFlag>0){ showFlag=-1; continue;}
            showFlag=CMath.s_int(mob.session().prompt("Edit which? ",""));
            if(showFlag<=0)
            {
                showFlag=-1;
                ok=true;
            }
        }
        dbupdateall(oldName);
    }
    
    public void processResults(MOB mob)
    {
        if(!mayISeeResults(mob))
            return;
        if((!loaded)&&(!dbloadbyname())) 
            return;
        StringBuffer present=new StringBuffer("");
        present.append("^O"+subject+"^N\n\r\n\r");
        if(options.size()==0) 
        {
            mob.tell(present.toString()+"Oops! No options defined!");
            return;
        }
        int total=0;
        int[] votes=new int[options.size()+(CMath.bset(bitmap,FLAG_ABSTAIN)?1:0)];
        PollResult R=null;
        int choice=0;
        for(int r=0;r<results.size();r++)
        {
            R=(PollResult)results.elementAt(r);
            choice=CMath.s_int(R.answer);
            if(((choice<=0)&&CMath.bset(bitmap,FLAG_ABSTAIN))
            ||((choice>=0)&&(choice<=options.size())))
            {
                total++;
                if(choice<=0)
                    votes[votes.length-1]++;
                else
                    votes[choice-1]++;
            }
        }
        PollOption O=null;
        for(int o=0;o<options.size();o++)
        {
            O=(PollOption)options.elementAt(o);
            int pct=0;
            if(total>0)
                pct=(int)Math.round(CMath.div(votes[o],total)*100.0);
            present.append(CMStrings.padRight("^H"+(o+1),2)+": ^N"+O.text+" ^O(Votes: "+votes[o]+" - "+pct+"%)^N\n\r");
        }
        if(CMath.bset(bitmap,FLAG_ABSTAIN))
        {
            int pct=0;
            if(total>0)
                pct=(int)Math.round(CMath.div(votes[votes.length-1],total)*100.0);
            present.append("    ^NAbstentions ^O("+votes[votes.length-1]+" - "+pct+"%)^N\n\r");
        }
        mob.tell(present.toString());
    }

}
