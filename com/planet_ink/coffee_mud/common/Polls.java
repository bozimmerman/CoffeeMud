package com.planet_ink.coffee_mud.common;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.system.DBConnections;
import com.planet_ink.coffee_mud.utils.*;

import java.io.IOException;
import java.util.*;

/* 
Copyright 2000-2005 Bo Zimmerman

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

public class Polls
{
    private static Vector pollCache=null;
    public static final int FLAG_ACTIVE=1;
    public static final int FLAG_PREVIEWRESULTS=2;
    public static final int FLAG_ABSTAIN=4;
    public static final int FLAG_VOTEBYIP=8;
    public static final int FLAG_HIDERESULTS=16;
    public static final int FLAG_NOTATLOGIN=32;
    public static void unload(){pollCache=null;}
    
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
            str.append(XMLManager.convertXMLtoTag("TEXT",CoffeeMaker.parseOutAngleBrackets(PO.text)));
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
            str.append(XMLManager.convertXMLtoTag("USER",PR.user));
            str.append(XMLManager.convertXMLtoTag("IP",PR.ip));
            str.append(XMLManager.convertXMLtoTag("ANS",PR.answer));
            str.append("</RESULT>");
        }
        str.append("</RESULTS>");
        return str.toString();
    }
    
    public void dbcreate()
    {
        if(getCache()!=null) getCache().addElement(this);
        CMClass.DBEngine().DBCreatePoll(name,author,subject,description,getOptionsXML(),(int)bitmap,qualZapper,getResultsXML(),expiration);
    }
    public void dbupdateresults()
    {
        CMClass.DBEngine().DBUpdatePollResults(name,getResultsXML());
    }
    public void dbupdateall(String oldName)
    {
        CMClass.DBEngine().DBUpdatePoll(oldName,name,author,subject,description,getOptionsXML(),(int)bitmap,qualZapper,getResultsXML(),expiration);
    }
    public void dbdelete()
    {
        if(getCache()!=null) getCache().removeElement(this);
        CMClass.DBEngine().DBDeletePoll(name);
    }
    public boolean dbloadbyname()
    {
        Vector V=CMClass.DBEngine().DBReadPoll(name);
        if((V==null)||(V.size()==0)) return false;
        name=(String)V.elementAt(0);
        author=(String)V.elementAt(1);
        subject=(String)V.elementAt(2);
        description=(String)V.elementAt(3);
        options=new Vector();
        String optionsXML=(String)V.elementAt(4);
        Vector V2=XMLManager.parseAllXML(optionsXML);
        XMLManager.XMLpiece OXV=XMLManager.getPieceFromPieces(V2,"OPTIONS");
        if((OXV!=null)&&(OXV.contents!=null)&&(OXV.contents.size()>0))
        for(int v2=0;v2<OXV.contents.size();v2++)
        {
            XMLManager.XMLpiece XP=(XMLManager.XMLpiece)OXV.contents.elementAt(v2);
            if(!XP.tag.equalsIgnoreCase("option"))
                continue;
            PollOption PO=new PollOption();
            PO.text=CoffeeMaker.restoreAngleBrackets(XMLManager.getValFromPieces(XP.contents,"TEXT"));
            options.addElement(PO);
        }
        bitmap=((Long)V.elementAt(5)).longValue();
        qualZapper=(String)V.elementAt(6);
        results=new Vector();
        String resultsXML=(String)V.elementAt(7);
        V2=XMLManager.parseAllXML(resultsXML);
        OXV=XMLManager.getPieceFromPieces(V2,"RESULTS");
        if((OXV!=null)&&(OXV.contents!=null)&&(OXV.contents.size()>0))
        for(int v2=0;v2<OXV.contents.size();v2++)
        {
            XMLManager.XMLpiece XP=(XMLManager.XMLpiece)OXV.contents.elementAt(v2);
            if(!XP.tag.equalsIgnoreCase("result"))
                continue;
            PollResult PR=new PollResult();
            PR.user=XMLManager.getValFromPieces(XP.contents,"USER");
            PR.ip=XMLManager.getValFromPieces(XP.contents,"IP");
            PR.answer=XMLManager.getValFromPieces(XP.contents,"ANS");
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
        if(!Util.bset(bitmap,FLAG_ACTIVE))
            return false;
        if(!MUDZapper.zapperCheck(qualZapper,mob))
            return false;
        if((expiration>0)&&(System.currentTimeMillis()>expiration))
        {
            bitmap=Util.unsetb(bitmap,FLAG_ACTIVE);
            dbupdateall(name);
            return false;
        }
        if(getMyVote(mob)!=null) return false;
        return true;
    }
    
    public boolean mayISeeResults(MOB mob)
    {
        if(mob==null) return false;
        if(!MUDZapper.zapperCheck(qualZapper,mob))
            return false;
        if(Util.bset(bitmap,FLAG_HIDERESULTS)&&(!CMSecurity.isAllowedAnywhere(mob,"POLLS")))
            return false;
        if(Util.bset(bitmap,FLAG_PREVIEWRESULTS))
            return true;
        if((expiration>0)
        &&(System.currentTimeMillis()<expiration))
            return false;
        if((getMyVote(mob)==null)&&(!Util.bset(bitmap,FLAG_ABSTAIN))) 
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
                present.append("^H"+Util.padLeft(""+(o+1),2)+": ^N"+PO.text+"\n\r");
            }
            if(Util.bset(bitmap,FLAG_ABSTAIN))
                present.append("^H  : ^NPress ENTER to abstain from voting.^?\n\r");
            
            mob.tell(present.toString());
            int choice=-1;
            while((choice<0)&&(mob.session()!=null)&&(!mob.session().killFlag()))
            {
                
                String s=mob.session().prompt("Please make your selection (1-"+options.size()+"): ");
                if((s.length()==0)&&(Util.bset(bitmap,FLAG_ABSTAIN)))
                    break;
                if(Util.isInteger(s)&&(Util.s_int(s)>=1)&&(Util.s_int(s)<=options.size()))
                    choice=Util.s_int(s);
            }
            PollResult R=new PollResult();
            R.user=mob.Name();
            if(Util.bset(bitmap,FLAG_VOTEBYIP))
                R.ip=mob.session().getAddress();
            R.answer=""+choice;
            addVoteResult(R);
        }
        catch(java.io.IOException x)
        {
            Log.errOut("Polls",x);
        }
    }
    
    public void modifyVote(MOB mob)
    throws IOException
    {
        if((mob.isMonster())||(!CMSecurity.isAllowedAnywhere(mob,"POLLS")))
            return;
        boolean ok=false;
        int showFlag=-1;
        if(CommonStrings.getIntVar(CommonStrings.SYSTEMI_EDITORTYPE)>0)
            showFlag=-999;
        String oldName=name;
        while(!ok)
        {
            int showNumber=0;
            String possName=EnglishParser.promptText(mob,name,++showNumber,showFlag,"Name");
            while((!possName.equalsIgnoreCase(name))&&(getPoll(possName)!=null))
                possName=possName+"!";
            name=possName;
            description=EnglishParser.promptText(mob,description,++showNumber,showFlag,"Introduction");
            subject=EnglishParser.promptText(mob,subject,++showNumber,showFlag,"Results Header");
            if(subject.length()>250) subject=subject.substring(0,250);
            if(author.length()==0) author=mob.Name();
            qualZapper=EnglishParser.promptText(mob,qualZapper,++showNumber,showFlag,"Qual. Mask",true);
            bitmap=(EnglishParser.promptBool(mob,Util.bset(bitmap,FLAG_ACTIVE),++showNumber,showFlag,"Poll Active"))?
                Util.setb(bitmap,FLAG_ACTIVE):Util.unsetb(bitmap,FLAG_ACTIVE);
            bitmap=(EnglishParser.promptBool(mob,Util.bset(bitmap,FLAG_PREVIEWRESULTS),++showNumber,showFlag,"Preview Results"))?
                    Util.setb(bitmap,FLAG_PREVIEWRESULTS):Util.unsetb(bitmap,FLAG_PREVIEWRESULTS);
            bitmap=(EnglishParser.promptBool(mob,Util.bset(bitmap,FLAG_ABSTAIN),++showNumber,showFlag,"Allow Abstention"))?
                    Util.setb(bitmap,FLAG_ABSTAIN):Util.unsetb(bitmap,FLAG_ABSTAIN);
            bitmap=(EnglishParser.promptBool(mob,Util.bset(bitmap,FLAG_VOTEBYIP),++showNumber,showFlag,"Use IP Addresses"))?
                    Util.setb(bitmap,FLAG_VOTEBYIP):Util.unsetb(bitmap,FLAG_VOTEBYIP);
            bitmap=(EnglishParser.promptBool(mob,Util.bset(bitmap,FLAG_HIDERESULTS),++showNumber,showFlag,"Hide Results"))?
                    Util.setb(bitmap,FLAG_HIDERESULTS):Util.unsetb(bitmap,FLAG_HIDERESULTS);
            bitmap=(EnglishParser.promptBool(mob,Util.bset(bitmap,FLAG_NOTATLOGIN),++showNumber,showFlag,"POLL CMD only"))?
                    Util.setb(bitmap,FLAG_NOTATLOGIN):Util.unsetb(bitmap,FLAG_NOTATLOGIN);
            String expirationDate="NA";
            IQCalendar C=null;
            if(expiration>0)
            {
                C=new IQCalendar(expiration);
                if(C!=null) expirationDate=C.d2String();
            }
            expirationDate=EnglishParser.promptText(mob,expirationDate,++showNumber,showFlag,"Exp. Date (MM/DD/YYYY HH:MM AP)",true);
            if((expirationDate.trim().length()==0)||(expirationDate.equalsIgnoreCase("NA")))
                expiration=0;
            else
            { try{expiration=IQCalendar.string2Millis(expirationDate.trim());}catch(Exception e){}}
                    
            Vector del=new Vector();
            for(int i=0;i<options.size();i++)
            {
                PollOption PO=(PollOption)options.elementAt(i);
                PO.text=EnglishParser.promptText(mob,PO.text,++showNumber,showFlag,"Vote Option",true);
                if(PO.text.length()==0) del.addElement(PO);
            }
            for(int i=0;i<del.size();i++)
                options.removeElement(del.elementAt(i));
            
            PollOption PO=new PollOption();
            while((PO!=null)&&(!mob.session().killFlag()))
            {
                PO=new PollOption();
                PO.text=EnglishParser.promptText(mob,PO.text,++showNumber,showFlag,"New Vote Option",true);
                if(PO.text.length()==0) 
                    break;
                options.addElement(PO);
            }
            if(showFlag<-900){ ok=true; break;}
            if(showFlag>0){ showFlag=-1; continue;}
            showFlag=Util.s_int(mob.session().prompt("Edit which? ",""));
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
        int[] votes=new int[options.size()+(Util.bset(bitmap,FLAG_ABSTAIN)?1:0)];
        PollResult R=null;
        int choice=0;
        for(int r=0;r<results.size();r++)
        {
            R=(PollResult)results.elementAt(r);
            choice=Util.s_int(R.answer);
            if(((choice<=0)&&Util.bset(bitmap,FLAG_ABSTAIN))
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
                pct=(int)Math.round(Util.div(votes[o],total)*100.0);
            present.append(Util.padRight("^H"+(o+1),2)+": ^N"+O.text+" ^O(Votes: "+votes[o]+" - "+pct+"%)^N\n\r");
        }
        if(Util.bset(bitmap,FLAG_ABSTAIN))
        {
            int pct=0;
            if(total>0)
                pct=(int)Math.round(Util.div(votes[votes.length-1],total)*100.0);
            present.append("    ^NAbstentions ^O("+votes[votes.length-1]+" - "+pct+"%)^N\n\r");
        }
        mob.tell(present.toString());
    }
    
    public static synchronized Vector getCache()
    {
        if(CMSecurity.isDisabled("POLLCACHE")) 
            return null;
        if(pollCache==null) 
        {
            pollCache=new Vector();
            Vector list=CMClass.DBEngine().DBReadPollList();
            Vector V=null;
            Polls P=null;
            for(int l=0;l<list.size();l++)
            {
                V=(Vector)list.elementAt(l);
                P=new Polls();
                P.setName((String)V.firstElement());
                if(P.dbloadbyname())
                    pollCache.addElement(P);
            }
        }
        return pollCache;
    }
    public static Polls getPoll(String named)
    {
        Vector V=getCache();
        if(V!=null)
        {
            Polls P=null;
            for(int c=0;c<V.size();c++)
            {
                P=(Polls)V.elementAt(c);
                if(P.getName().equalsIgnoreCase(named))
                    return P;
            }
        }
        else
        {
            Polls P=new Polls();
            P.setName(named);
            if(P.dbloadbyname())
                return P;
        }
        return null;
    }
    
    public static Polls getPoll(int x)
    {
        if(x<0) return null;
        Vector V=getPollList();
        if(x<V.size())
        {
            Polls P=(Polls)V.elementAt(x);
            if((P.loaded)||(P.dbloadbyname()))
                return P;
        }
        return null;
    }
    
    public static Vector[] getMyPolls(MOB mob, boolean login)
    {
        Vector V=getPollList();
        Vector list[]=new Vector[3];
        for(int l=0;l<3;l++)
            list[l]=new Vector();
        
        Polls P=null;
        for(int v=0;v<V.size();v++)
        {
            P=(Polls)V.elementAt(v);
            if((P.loaded)||(P.dbloadbyname()))
            {
                if((P.mayIVote(mob))&&(login)&&(Util.bset(P.getFlags(),FLAG_NOTATLOGIN)))
                    list[1].addElement(P);
                else
                if(P.mayIVote(mob))
                    list[0].addElement(P);
                else
                if(P.mayISeeResults(mob))
                    list[2].addElement(P);
            }
        }
        return list;
    }
    
    public static Vector getPollList()
    {
        Vector V=getCache();
        if(V!=null) 
            return ((Vector)V.clone());
        V=CMClass.DBEngine().DBReadPollList();
        Vector list=new Vector();
        Vector V2=null;
        Polls P=null;
        for(int v=0;v<V.size();v++)
        {
            V2=(Vector)V.elementAt(v);
            P=new Polls();
            P.setName((String)V2.elementAt(0));
            P.setFlags(((Long)V2.elementAt(1)).longValue());
            P.setQualZapper((String)V2.elementAt(2));
            P.setExpiration(((Long)V2.elementAt(3)).longValue());
            list.addElement(P);
        }
        return list;
    }
    
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
}
