package com.planet_ink.coffee_mud.WebMacros.grinder;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Clan.MemberRecord;
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
public class GrinderClans
{
    public String name()    {return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

    public static String membersList(Clan C, ExternalHTTPRequests httpReq)
    {
        Vector newMembersNames=new Vector();
        List<MemberRecord> DV=C.getMemberList();
        if(httpReq.isRequestParameter("MEMB1"))
        {
            int num=1;
            String aff=httpReq.getRequestParameter("MEMB"+num);
            while(aff!=null)
            {
                if(aff.length()>0)
                {
                    MOB M=CMLib.players().getLoadPlayer(aff);
                    if(M==null) return "Unknown player '"+aff+"'.";
                    newMembersNames.addElement(M.Name());
                    int newRole=CMath.s_int(httpReq.getRequestParameter("ROLE"+num));
                    if(!M.getClanID().equalsIgnoreCase(C.clanID()))
                    {
                        if(M.getClanID().length()>0)
                        {
                            Clan oldClan=CMLib.clans().getClan(M.getClanID());
                            if(oldClan!=null) oldClan.delMember(M);
                        }
                        C.addMember(M,newRole);
                    }
                    else
                    if(M.getClanRole()!=newRole)
                        C.addMember(M,newRole);
                }
                num++;
                aff=httpReq.getRequestParameter("MEMB"+num);
            }
			for(MemberRecord member : DV)
            {
                if(!newMembersNames.contains(member.name))
                {
                    MOB M=CMLib.players().getLoadPlayer(member.name);
                    if(M!=null) C.delMember(M);
                }
            }
        }
        return "";
    }

    public static String relationsList(Clan C, ExternalHTTPRequests httpReq)
    {
        if(httpReq.isRequestParameter("RELATION1"))
        {
            int relat=0;
            Clan CC=null;
            for(Enumeration e=CMLib.clans().allClans();e.hasMoreElements();)
            {
                CC=(Clan)e.nextElement();
                if(CC==C) continue;
                relat++;
                String aff=httpReq.getRequestParameter("RELATION"+relat);
                if((aff!=null)&&(aff.length()>0))
                {
                    if(C.getClanRelations(CC.clanID())!=CMath.s_int(aff))
                        C.setClanRelations(CC.clanID(),CMath.s_int(aff),System.currentTimeMillis());
                }
                else
                    return "No relation for clan "+CC.clanID();
            }
        }
        return "";
    }

    public String runMacro(ExternalHTTPRequests httpReq, String parm)
    {
        String last=httpReq.getRequestParameter("CLAN");
        if(last==null) return " @break@";
        if(last.length()>0)
        {
            Clan C=CMLib.clans().getClan(last);
            if(C!=null)
            {
                String str=null;
                str=httpReq.getRequestParameter("PREMISE");
                if(str!=null) C.setPremise(str);
                str=httpReq.getRequestParameter("RECALLID");
                if(str!=null)
                {
                    Room R=CMLib.map().getRoom(str);
                    if(R!=null) C.setRecall(CMLib.map().getExtendedRoomID(R));
                }
                str=httpReq.getRequestParameter("MORGUEID");
                if(str!=null)
                {
                    Room R=CMLib.map().getRoom(str);
                    if(R!=null) C.setMorgue(CMLib.map().getExtendedRoomID(R));
                }
                str=httpReq.getRequestParameter("AUTOPOSITIONID");
                if(str!=null) C.setAutoPosition(CMath.s_int(str));
                str=httpReq.getRequestParameter("DONATIONID");
                if(str!=null)
                {
                    Room R=CMLib.map().getRoom(str);
                    if(R!=null) C.setDonation(CMLib.map().getExtendedRoomID(R));
                }
                str=httpReq.getRequestParameter("TAX");
                if(str!=null) C.setTaxes(CMath.s_pct(str));
                str=httpReq.getRequestParameter("CCLASSID");
                if(str!=null)
                {
                    CharClass CC=CMClass.getCharClass(str);
                    if(CC==null)CC=CMClass.findCharClass(str);
                    if(CC!=null) C.setClanClass(CC.ID());
                }
                str=httpReq.getRequestParameter("EXP");
                if(str!=null) C.setExp(CMath.s_int(str));
                str=httpReq.getRequestParameter("STATUSID");
                if(str!=null) C.setStatus(CMath.s_int(str));
                str=httpReq.getRequestParameter("ACCEPTANCEID");
                if(str!=null) C.setAcceptanceSettings(str);
                str=httpReq.getRequestParameter("TYPEID");
                if(str!=null) C.setGovernment(CMath.s_int(str));
                String err=GrinderClans.membersList(C,httpReq);
                if(err.length()>0) return err;
                err=GrinderClans.relationsList(C,httpReq);
                if(err.length()>0) return err;
            }
        }
        return "";
    }
}