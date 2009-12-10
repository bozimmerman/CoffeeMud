package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.DefaultPoll;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.io.IOException;
import java.util.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;

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
public class Polls extends StdLibrary implements PollManager
{
    public String ID(){return "Polls";}
    
    public Vector pollCache=null;
    public boolean shutdown(){
        pollCache=null;
        return true;
    }
    
    public void addPoll(Poll P){if(getCache()!=null) getCache().addElement(P);}
    public void removePoll(Poll P){if(getCache()!=null) getCache().removeElement(P);}
    
    public synchronized Vector getCache()
    {
        if(CMSecurity.isDisabled("POLLCACHE")) 
            return null;
        if(pollCache==null) 
        {
            pollCache=new Vector();
            Vector list=CMLib.database().DBReadPollList();
            Vector V=null;
            Poll P=null;
            for(int l=0;l<list.size();l++)
            {
                V=(Vector)list.elementAt(l);
                P=loadPollByName((String)V.firstElement());
                if(P!=null)
                    pollCache.addElement(P);
            }
        }
        return pollCache;
    }
    public Poll getPoll(String named)
    {
        Vector V=getCache();
        if(V!=null)
        {
            Poll P=null;
            for(int c=0;c<V.size();c++)
            {
                P=(Poll)V.elementAt(c);
                if(P.getName().equalsIgnoreCase(named))
                    return P;
            }
        }
        else
        {
            Poll P=loadPollByName(named);
            return P;
        }
        return null;
    }
    
    public Poll getPoll(int x)
    {
        if(x<0) return null;
        Vector V=getPollList();
        if(x<V.size())
        {
            Poll P=(Poll)V.elementAt(x);
            if(loadPollIfNecessary(P))
                return P;
        }
        return null;
    }
    
    public Vector[] getMyPolls(MOB mob, boolean login)
    {
        Vector V=getPollList();
        Vector list[]=new Vector[3];
        for(int l=0;l<3;l++)
            list[l]=new Vector();
        
        Poll P=null;
        for(int v=0;v<V.size();v++)
        {
            P=(Poll)V.elementAt(v);
            if(loadPollIfNecessary(P))
            {
                if((P.mayIVote(mob))&&(login)&&(CMath.bset(P.getFlags(),Poll.FLAG_NOTATLOGIN)))
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
    
    public Vector getPollList()
    {
        Vector V=getCache();
        if(V!=null) 
            return ((Vector)V.clone());
        V=CMLib.database().DBReadPollList();
        Vector list=new Vector();
        Vector V2=null;
        Poll P=null;
        for(int v=0;v<V.size();v++)
        {
            V2=(Vector)V.elementAt(v);
            P=(Poll)CMClass.getCommon("DefaultPoll");
            P.setName((String)V2.elementAt(0));
            P.setFlags(((Long)V2.elementAt(1)).longValue());
            P.setQualZapper((String)V2.elementAt(2));
            P.setExpiration(((Long)V2.elementAt(3)).longValue());
            P.setLoaded(false);
            list.addElement(P);
        }
        return list;
    }
    
    public void processVote(Poll P, MOB mob)
    {
        if(!P.mayIVote(mob)) 
            return;
        try
        {
            if(!loadPollIfNecessary(P))
                return;
            StringBuffer present=new StringBuffer("");
            present.append("^O"+P.getDescription()+"^N\n\r\n\r");
            if(P.getOptions().size()==0) 
            {
                mob.tell(present.toString()+"Oops! No options defined!");
                return;
            }
            Poll.PollOption PO=null;
            for(int o=0;o<P.getOptions().size();o++)
            {
                PO=(Poll.PollOption)P.getOptions().elementAt(o);
                present.append("^H"+CMStrings.padLeft(""+(o+1),2)+": ^N"+PO.text+"\n\r");
            }
            if(CMath.bset(P.getFlags(),Poll.FLAG_ABSTAIN))
                present.append("^H  : ^NPress ENTER to abstain from voting.^?\n\r");
            
            mob.tell(present.toString());
            int choice=-1;
            while((choice<0)&&(mob.session()!=null)&&(!mob.session().killFlag()))
            {
                
                String s=mob.session().prompt("Please make your selection (1-"+P.getOptions().size()+"): ");
                if((s.length()==0)&&(CMath.bset(P.getFlags(),Poll.FLAG_ABSTAIN)))
                    break;
                if(CMath.isInteger(s)&&(CMath.s_int(s)>=1)&&(CMath.s_int(s)<=P.getOptions().size()))
                    choice=CMath.s_int(s);
            }
            Poll.PollResult R=new Poll.PollResult(mob.name(),"",""+choice);
            if(CMath.bset(P.getFlags(),Poll.FLAG_VOTEBYIP))
                R.ip=mob.session().getAddress();
            P.addVoteResult(R);
        }
        catch(java.io.IOException x)
        {
            if(Log.isMaskedErrMsg(x.getMessage()))
                Log.errOut("Polls",x.getMessage());
            else
                Log.errOut("Polls",x);
        }
    }
    
    public void modifyVote(Poll P, MOB mob) throws java.io.IOException
    {
        if((mob.isMonster())||(!CMSecurity.isAllowedAnywhere(mob,"POLLS")))
            return;
        boolean ok=false;
        int showFlag=-1;
        if(CMProps.getIntVar(CMProps.SYSTEMI_EDITORTYPE)>0)
            showFlag=-999;
        String oldName=P.getName();
        while(!ok)
        {
            int showNumber=0;
            String possName=CMLib.genEd().prompt(mob,P.getName(),++showNumber,showFlag,"Name");
            while((!possName.equalsIgnoreCase(P.getName()))&&(CMLib.polls().getPoll(possName)!=null))
                possName=possName+"!";
            P.setName(possName);
            P.setDescription(CMLib.genEd().prompt(mob,P.getDescription(),++showNumber,showFlag,"Introduction"));
            P.setSubject(CMLib.genEd().prompt(mob,P.getSubject(),++showNumber,showFlag,"Results Header"));
            if(P.getSubject().length()>250) P.setSubject(P.getSubject().substring(0,250));
            if(P.getAuthor().length()==0) P.setAuthor(mob.Name());
            P.setQualZapper(CMLib.genEd().prompt(mob,P.getQualZapper(),++showNumber,showFlag,"Qual. Mask",true));
            P.setFlags((CMLib.genEd().prompt(mob,CMath.bset(P.getFlags(),Poll.FLAG_ACTIVE),++showNumber,showFlag,"Poll Active"))?
                CMath.setb(P.getFlags(),Poll.FLAG_ACTIVE):CMath.unsetb(P.getFlags(),Poll.FLAG_ACTIVE));
            P.setFlags((CMLib.genEd().prompt(mob,CMath.bset(P.getFlags(),Poll.FLAG_PREVIEWRESULTS),++showNumber,showFlag,"Preview Results"))?
                    CMath.setb(P.getFlags(),Poll.FLAG_PREVIEWRESULTS):CMath.unsetb(P.getFlags(),Poll.FLAG_PREVIEWRESULTS));
            P.setFlags((CMLib.genEd().prompt(mob,CMath.bset(P.getFlags(),Poll.FLAG_ABSTAIN),++showNumber,showFlag,"Allow Abstention"))?
                    CMath.setb(P.getFlags(),Poll.FLAG_ABSTAIN):CMath.unsetb(P.getFlags(),Poll.FLAG_ABSTAIN));
            P.setFlags((CMLib.genEd().prompt(mob,CMath.bset(P.getFlags(),Poll.FLAG_VOTEBYIP),++showNumber,showFlag,"Use IP Addresses"))?
                    CMath.setb(P.getFlags(),Poll.FLAG_VOTEBYIP):CMath.unsetb(P.getFlags(),Poll.FLAG_VOTEBYIP));
            P.setFlags((CMLib.genEd().prompt(mob,CMath.bset(P.getFlags(),Poll.FLAG_HIDERESULTS),++showNumber,showFlag,"Hide Results"))?
                    CMath.setb(P.getFlags(),Poll.FLAG_HIDERESULTS):CMath.unsetb(P.getFlags(),Poll.FLAG_HIDERESULTS));
            P.setFlags((CMLib.genEd().prompt(mob,CMath.bset(P.getFlags(),Poll.FLAG_NOTATLOGIN),++showNumber,showFlag,"POLL CMD only"))?
                    CMath.setb(P.getFlags(),Poll.FLAG_NOTATLOGIN):CMath.unsetb(P.getFlags(),Poll.FLAG_NOTATLOGIN));
            String expirationDate="NA";
            if(P.getExpiration()>0) expirationDate=CMLib.time().date2String(P.getExpiration());
            
            expirationDate=CMLib.genEd().prompt(mob,expirationDate,++showNumber,showFlag,"Exp. Date (MM/DD/YYYY HH:MM AP)",true);
            if((expirationDate.trim().length()==0)||(expirationDate.equalsIgnoreCase("NA")))
                P.setExpiration(0);
            else
            { try{P.setExpiration(CMLib.time().string2Millis(expirationDate.trim()));}catch(Exception e){}}
                    
            Vector del=new Vector();
            for(int i=0;i<P.getOptions().size();i++)
            {
                Poll.PollOption PO=(Poll.PollOption)P.getOptions().elementAt(i);
                PO.text=CMLib.genEd().prompt(mob,PO.text,++showNumber,showFlag,"Vote Option",true);
                if(PO.text.length()==0) del.addElement(PO);
            }
            for(int i=0;i<del.size();i++)
                P.getOptions().removeElement(del.elementAt(i));
            
            Poll.PollOption PO=null;
            while(!mob.session().killFlag())
            {
                PO=new Poll.PollOption(
                        CMLib.genEd().prompt(mob,"",++showNumber,showFlag,"New Vote Option",true)
                );
                if(PO.text.length()==0) 
                    break;
                P.getOptions().addElement(PO);
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
        updatePoll(oldName,P);
    }
    
    public void processResults(Poll P, MOB mob)
    {
        if(!P.mayISeeResults(mob))
            return;
        if(!loadPollIfNecessary(P))
            return;
        StringBuffer present=new StringBuffer("");
        present.append("^O"+P.getSubject()+"^N\n\r\n\r");
        if(P.getOptions().size()==0) 
        {
            mob.tell(present.toString()+"Oops! No options defined!");
            return;
        }
        int total=0;
        int[] votes=new int[P.getOptions().size()+(CMath.bset(P.getFlags(),Poll.FLAG_ABSTAIN)?1:0)];
        Poll.PollResult R=null;
        int choice=0;
        for(int r=0;r<P.getResults().size();r++)
        {
            R=(Poll.PollResult)P.getResults().elementAt(r);
            choice=CMath.s_int(R.answer);
            if(((choice<=0)&&CMath.bset(P.getFlags(),Poll.FLAG_ABSTAIN))
            ||((choice>=0)&&(choice<=P.getOptions().size())))
            {
                total++;
                if(choice<=0)
                    votes[votes.length-1]++;
                else
                    votes[choice-1]++;
            }
        }
        Poll.PollOption O=null;
        for(int o=0;o<P.getOptions().size();o++)
        {
            O=(Poll.PollOption)P.getOptions().elementAt(o);
            int pct=0;
            if(total>0)
                pct=(int)Math.round(CMath.div(votes[o],total)*100.0);
            present.append(CMStrings.padRight("^H"+(o+1),2)+": ^N"+O.text+" ^O(Votes: "+votes[o]+" - "+pct+"%)^N\n\r");
        }
        if(CMath.bset(P.getFlags(),Poll.FLAG_ABSTAIN))
        {
            int pct=0;
            if(total>0)
                pct=(int)Math.round(CMath.div(votes[votes.length-1],total)*100.0);
            present.append("    ^NAbstentions ^O("+votes[votes.length-1]+" - "+pct+"%)^N\n\r");
        }
        mob.tell(present.toString());
    }
    
    public void createPoll(Poll P)
    {
        addPoll(P);
        CMLib.database().DBCreatePoll(P.getName(),
                                      P.getAuthor(),
                                      P.getSubject(),
                                      P.getDescription(),
                                      P.getOptionsXML(),
                                      (int)P.getFlags(),
                                      P.getQualZapper(),
                                      P.getResultsXML(),
                                      P.getExpiration());
    }
    public void updatePollResults(Poll P)
    {
        CMLib.database().DBUpdatePollResults(P.getName(),P.getResultsXML());
    }
    public void updatePoll(String oldName, Poll P)
    {
        CMLib.database().DBUpdatePoll(oldName,
                                      P.getName(),
                                      P.getAuthor(),
                                      P.getSubject(),
                                      P.getDescription(),
                                      P.getOptionsXML(),
                                      (int)P.getFlags(),
                                      P.getQualZapper(),
                                      P.getResultsXML(),
                                      P.getExpiration());
    }
    public void deletePoll(Poll P)
    {
        removePoll(P);
        CMLib.database().DBDeletePoll(P.getName());
    }
    
    public boolean loadPollIfNecessary(Poll P) 
    {
        if(P.loaded()) return true;
        Vector V=CMLib.database().DBReadPoll(P.getName());
        if((V==null)||(V.size()==0)) return false;
        P.setName((String)V.elementAt(0));
        P.setAuthor((String)V.elementAt(1));
        P.setSubject((String)V.elementAt(2));
        P.setDescription((String)V.elementAt(3));
        Vector options=new Vector();
        P.setOptions(options);
        String optionsXML=(String)V.elementAt(4);
        Vector V2=CMLib.xml().parseAllXML(optionsXML);
        XMLLibrary.XMLpiece OXV=CMLib.xml().getPieceFromPieces(V2,"OPTIONS");
        if((OXV!=null)&&(OXV.contents!=null)&&(OXV.contents.size()>0))
        for(int v2=0;v2<OXV.contents.size();v2++)
        {
            XMLLibrary.XMLpiece XP=(XMLLibrary.XMLpiece)OXV.contents.elementAt(v2);
            if(!XP.tag.equalsIgnoreCase("option"))
                continue;
            Poll.PollOption PO=new Poll.PollOption(
                    CMLib.xml().restoreAngleBrackets(CMLib.xml().getValFromPieces(XP.contents,"TEXT"))
            );
            options.addElement(PO);
        }
        P.setFlags(((Long)V.elementAt(5)).longValue());
        P.setQualZapper((String)V.elementAt(6));
        Vector results=new Vector();
        P.setResults(results);
        String resultsXML=(String)V.elementAt(7);
        V2=CMLib.xml().parseAllXML(resultsXML);
        OXV=CMLib.xml().getPieceFromPieces(V2,"RESULTS");
        if((OXV!=null)&&(OXV.contents!=null)&&(OXV.contents.size()>0))
        for(int v2=0;v2<OXV.contents.size();v2++)
        {
            XMLLibrary.XMLpiece XP=(XMLLibrary.XMLpiece)OXV.contents.elementAt(v2);
            if(!XP.tag.equalsIgnoreCase("result"))
                continue;
            Poll.PollResult PR=new Poll.PollResult(
                    CMLib.xml().getValFromPieces(XP.contents,"USER"),
                    CMLib.xml().getValFromPieces(XP.contents,"IP"),
                    CMLib.xml().getValFromPieces(XP.contents,"ANS"));
            results.addElement(PR);
        }
        P.setExpiration(((Long)V.elementAt(8)).longValue());
        P.setLoaded(true);
        return true;
    }

    
    public Poll loadPollByName(String name)
    {
        Poll P=(Poll)CMClass.getCommon("DefaultPoll");
        P.setLoaded(false);
        P.setName(name);
        return loadPollIfNecessary(P)?P:null;
    }
    
}
