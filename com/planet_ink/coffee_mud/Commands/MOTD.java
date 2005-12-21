package com.planet_ink.coffee_mud.Commands;
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
import com.planet_ink.coffee_mud.core.exceptions.HTTPRedirectException;

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
public class MOTD extends StdCommand
{
	public MOTD(){}

	private String[] access={"MOTD"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if((commands!=null)
		&&(commands.size()>1)
		&&(mob.playerStats()!=null)
		&&(CMParms.combine(commands,1).equalsIgnoreCase("AGAIN")
		   ||CMParms.combine(commands,1).equalsIgnoreCase("NEW")))
		{
			StringBuffer buf=new StringBuffer("");
			try
			{
				String msg = new CMFile(Resources.buildResourcePath("text")+"motd.txt",null,false).text().toString();
				if(msg.length()>0)
				{
					if(msg.startsWith("<cmvp>"))
						msg=new String(CMLib.httpUtils().doVirtualPage(msg.substring(6).getBytes()));
				    buf.append(msg+"\n\r--------------------------------------\n\r");
				}
		
				Vector journal=CMLib.database().DBReadJournal("CoffeeMud News");
				for(int which=0;which<journal.size();which++)
				{
					Vector entry=(Vector)journal.elementAt(which);
					String from=(String)entry.elementAt(1);
					long last=CMath.s_long((String)entry.elementAt(2));
					String to=(String)entry.elementAt(3);
					String subject=(String)entry.elementAt(4);
					String message=(String)entry.elementAt(5);
					long compdate=CMath.s_long((String)entry.elementAt(6));
					boolean mineAble=to.equalsIgnoreCase(mob.Name())||from.equalsIgnoreCase(mob.Name());
					if((compdate>mob.playerStats().lastDateTime())
					&&(to.equals("ALL")||mineAble))
					{
						if(message.startsWith("<cmvp>"))
							message=new String(CMLib.httpUtils().doVirtualPage(message.substring(6).getBytes()));
						buf.append("\n\rNews: "+CMLib.time().date2String(last)+"\n\r"+"FROM: "+CMStrings.padRight(from,15)+"\n\rTO  : "+CMStrings.padRight(to,15)+"\n\rSUBJ: "+subject+"\n\r"+message);
						buf.append("\n\r--------------------------------------\n\r");
					}
				}
                Vector postalChains=new Vector();
                Vector postalBranches=new Vector();
                PostOffice P=null;
                for(Enumeration e=CMLib.map().postOffices();e.hasMoreElements();)
                {
                    P=(PostOffice)e.nextElement();
                    if(!postalChains.contains(P.postalChain()))
                        postalChains.addElement(P.postalChain());
                    if(!postalBranches.contains(P.postalBranch()))
                        postalBranches.addElement(P.postalBranch());
                }
                if((postalChains.size()>0)&&(P!=null))
                {
                    Vector V=CMLib.database().DBReadData(mob.Name(),postalChains);
                    Hashtable res=getPostalResults(V,mob.playerStats().lastDateTime());
                    for(Enumeration e=res.keys();e.hasMoreElements();)
                    {
                        P=(PostOffice)e.nextElement();
                        int[] ct=(int[])res.get(P);
                        buf.append("\n\r"+report("You have",P,ct));
                    }
                    Hashtable res2=new Hashtable();
                    Clan C=null;
                    if(mob.getClanID().length()>0)
                    {
                        C=CMLib.clans().getClan(mob.getClanID());
                        if((C!=null)&&(C.allowedToDoThis(mob,Clan.FUNC_CLANWITHDRAW)>=0))
                        {
                            V=CMLib.database().DBReadData(C.name(),postalChains);
                            if(V.size()>0)
                                res=getPostalResults(V,mob.playerStats().lastDateTime());
                        }
                    }
                    if(C!=null)
                    for(Enumeration e=res2.keys();e.hasMoreElements();)
                    {
                        P=(PostOffice)e.nextElement();
                        int[] ct=(int[])res2.get(P);
                        buf.append("\n\r"+report("Your "+C.typeName()+" has",P,ct));
                    }
                    if((res.size()>0)||((C!=null)&&(res2.size()>0)))
                        buf.append("\n\r--------------------------------------\n\r");
                }
                
                Vector myEchoableCommandJournals=new Vector();
                for(int cj=0;cj<CMLib.journals().getNumCommandJournals();cj++)
                {
                    if((CMLib.journals().getCommandJournalFlags(cj).get("ADMINECHO")!=null)
                    &&((CMSecurity.isAllowed(mob,mob.location(),CMLib.journals().getCommandJournalName(cj)))
                            ||CMSecurity.isAllowed(mob,mob.location(),"KILL"+CMLib.journals().getCommandJournalName(cj)+"S")
                            ||CMSecurity.isAllowed(mob,mob.location(),"LISTADMIN")))
                        myEchoableCommandJournals.addElement(new Integer(cj));
                }
                boolean CJseparator=false;
                for(int cj=0;cj<myEchoableCommandJournals.size();cj++)
                {
                    Integer CJ=(Integer)myEchoableCommandJournals.elementAt(cj);
                    Vector items=CMLib.database().DBReadJournal("SYSTEM_"+CMLib.journals().getCommandJournalName(CJ.intValue())+"S");
                    if(items!=null)
                    for(int i=0;i<items.size();i++)
                    {
                        Vector entry=(Vector)items.elementAt(i);
                        String from=(String)entry.elementAt(1);
                        String message=(String)entry.elementAt(5);
                        long compdate=CMath.s_long((String)entry.elementAt(6));
                        if(compdate>mob.playerStats().lastDateTime())
                        {
                            buf.append("\n\rNEW "+CMLib.journals().getCommandJournalName(CJ.intValue())+" from "+from+": "+message+"\n\r");
                            CJseparator=true;
                        }
                    }
                }
                if(CJseparator)
                    buf.append("\n\r--------------------------------------\n\r");
                
                if((!CMath.bset(mob.getBitmap(),MOB.ATT_AUTOFORWARD))
                &&(CMProps.getVar(CMProps.SYSTEM_MAILBOX).length()>0))
                {
                    Vector msgs=CMLib.database().DBReadJournal(CMProps.getVar(CMProps.SYSTEM_MAILBOX));
                    int mymsgs=0;
                    for(int num=0;num<msgs.size();num++)
                    {
                        Vector thismsg=(Vector)msgs.elementAt(num);
                        String to=((String)thismsg.elementAt(3));
                        if(to.equalsIgnoreCase("all")||to.equalsIgnoreCase(mob.Name()))
                            mymsgs++;
                    }
                    if(mymsgs>0)
                        buf.append("\n\r^ZYou have mail waiting. Enter 'EMAIL BOX' to read.^?^.\n\r");
                }
                
				if(mob.session()!=null)
                    if(buf.length()>0)
                        mob.session().wraplessPrintln("\n\r--------------------------------------\n\r"+buf.toString());
                    else
                    if(CMParms.combine(commands,1).equalsIgnoreCase("AGAIN"))
                        mob.session().println("No MOTD to re-read.");
			}
			catch(HTTPRedirectException e){}
			return false;
		}
		
		if(CMath.bset(mob.getBitmap(),MOB.ATT_DAILYMESSAGE))
		{
			mob.setBitmap(CMath.unsetb(mob.getBitmap(),MOB.ATT_DAILYMESSAGE));
			mob.tell("The daily message has been turned on.");
		}
		else
		{
			mob.setBitmap(CMath.setb(mob.getBitmap(),MOB.ATT_DAILYMESSAGE));
			mob.tell("The daily message has been turned off.");
		}
		mob.tell("Enter MOTD AGAIN to see the message over again.");
		return false;
	}
    
    private String report(String whom, PostOffice P, int[] ct)
    {
        String branchName=P.postalBranch();
        if((P instanceof MOB)&&(((MOB)P).getStartRoom()!=null))
            branchName=((MOB)P).getStartRoom().getArea().Name();
        else
        {
            int x=branchName.indexOf("#");
            if(x>=0) branchName=branchName.substring(0,x);
        }
        if(ct[0]>0)
            return whom+" "+ct[0]+" new of "+ct[1]+" items at the "+branchName+" branch of the "+P.postalChain()+" post office.";
        return whom+" "+ct[1]+" items still waiting at the "+branchName+" branch of the "+P.postalChain()+" post office.";
    }
    
    private Hashtable getPostalResults(Vector mailData, long newTimeDate)
    {
        Hashtable results=new Hashtable();
        PostOffice P=null;
        for(int i=0;i<mailData.size();i++)
        {
            Vector letter=(Vector)mailData.elementAt(i);
            String chain=(String)letter.elementAt(1);
            String branch=(String)letter.elementAt(2);
            int x=branch.indexOf(";");
            if(x<0) continue;
            branch=branch.substring(0,x);
            P=CMLib.map().getPostOffice(chain,branch);
            if(P==null) continue;
            Vector pieces=P.parsePostalItemData((String)letter.elementAt(3));
            int[] ct=(int[])results.get(P);
            if(ct==null)
            {
                ct=new int[2];
                results.put(P,ct);
            }
            ct[1]++;
            if(CMath.s_long((String)pieces.elementAt(PostOffice.PIECE_TIME))>newTimeDate)
                ct[0]++;
        }
        return results;
    }
    
	public double actionsCost(){return 0.0;}
	public boolean canBeOrdered(){return true;}

	
}
