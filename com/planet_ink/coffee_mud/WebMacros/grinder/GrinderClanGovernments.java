package com.planet_ink.coffee_mud.WebMacros.grinder;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
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
   Copyright 2000-2011 Bo Zimmerman

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
public class GrinderClanGovernments
{
    public String name()    {return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

    public String runMacro(ExternalHTTPRequests httpReq, String parm)
    {
        String last=httpReq.getRequestParameter("GOVERNMENT");
        if(last==null) return " @break@";
        if(last.length()>0)
        {
			int lastID=CMath.s_int(last);
			Clan.Government G=CMLib.clans().getStockGovernment(lastID);
            String str=null;
            str=httpReq.getRequestParameter("NAME");
            if(str!=null)
            {
            	if(G==null)
            		G=CMLib.clans().createGovernment(str);
            	else
	            	G.name=str;
            }
            else
            	return " @break@";
            
            str=httpReq.getRequestParameter("ACCEPTPOS");
            if(str!=null) G.autoRole=CMath.s_int(str);
            str=httpReq.getRequestParameter("AUTOROLE");
            if(str!=null) G.acceptPos=CMath.s_int(str);
            str=httpReq.getRequestParameter("SHORTDESC");
            if(str!=null) G.shortDesc=str;
            str=httpReq.getRequestParameter("REQUIREDMASK");
            if(str!=null){ G.requiredMaskStr=str; G.requiredMask=CMLib.masking().maskCompile(G.requiredMaskStr); }
            str=httpReq.getRequestParameter("ISPUBLIC");
            G.isPublic = (str==null)?false:str.equalsIgnoreCase("on");
            str=httpReq.getRequestParameter("ISFAMILYONLY");
            G.isFamilyOnly = (str==null)?false:str.equalsIgnoreCase("on");
            str=httpReq.getRequestParameter("OVERRIDEMINMEMBERS");
            if(str!=null) G.overrideMinMembers = (str.trim().length()==0)?null:Integer.valueOf(CMath.s_int(str));
            str=httpReq.getRequestParameter("CONQUESTENABLED");
            G.conquestEnabled = (str==null)?false:str.equalsIgnoreCase("on");
            str=httpReq.getRequestParameter("CONQUESTITEMLOYALTY");
            G.conquestItemLoyalty = (str==null)?false:str.equalsIgnoreCase("on");
            str=httpReq.getRequestParameter("CONQUESTDEITYBASIS");
            G.conquestDeityBasis = (str==null)?false:str.equalsIgnoreCase("on");
            str=httpReq.getRequestParameter("MAXVOTEDAYS");
            if(str!=null) G.maxVoteDays=CMath.s_int(str);
            str=httpReq.getRequestParameter("VOTEQUORUMPCT");
            if(str!=null) G.voteQuorumPct=CMath.s_int(str);
            str=httpReq.getRequestParameter("AUTOPROMOTEBY");
            if(str!=null) G.autoPromoteBy=(Clan.AutoPromoteFlag)CMath.s_valueOf(Clan.AutoPromoteFlag.values(), str);
            str=httpReq.getRequestParameter("LONGDESC");
            if(str!=null) G.longDesc=str;
            G.helpStr=null;
            
			List<Clan.Position> posList=new Vector<Clan.Position>();
			String posDexStr="";
			int posDex=0;
			while(httpReq.isRequestParameter("GPOSID_"+posDexStr) && httpReq.getRequestParameter("GPOSID_"+posDexStr).trim().length()>0)
			{
				String oldID=httpReq.getRequestParameter("GPOSID_"+posDexStr);
				String oldName=httpReq.getRequestParameter("GPOSNAME_"+posDexStr);
				String oldPluralName=httpReq.getRequestParameter("GPOSPLURALNAME_"+posDexStr);
				int oldRoleID=CMath.s_int(httpReq.getRequestParameter("GPOSROLEID_"+posDexStr));
				int oldRank=CMath.s_int(httpReq.getRequestParameter("GPOSRANK_"+posDexStr));
				int oldMax=CMath.s_int(httpReq.getRequestParameter("GPOSMAX_"+posDexStr));
				String oldMask=httpReq.getRequestParameter("GPOSINNERMASK_"+posDexStr);
				String oldIsPublicStr=httpReq.getRequestParameter("GPOSISPUBLIC_"+posDexStr);
				boolean oldIsPublic=oldIsPublicStr==null?false:oldIsPublicStr.equalsIgnoreCase("on");
				Clan.Authority powerFuncs[]=new Clan.Authority[Clan.Function.values().length];
				for(int f=0;f<Clan.Function.values().length;f++)
					powerFuncs[f]=Clan.Authority.CAN_NOT_DO;
				String authDexStr="";
				int authDex=0;
				while(httpReq.getRequestParameter("GPOSPOWER_"+posDexStr+"_"+authDexStr)!=null)
				{
					Clan.Function auth = (Clan.Function)CMath.s_valueOf(Clan.Function.values(),httpReq.getRequestParameter("GPOSPOWER_"+posDexStr+"_"+authDexStr));
					powerFuncs[auth.ordinal()]=Clan.Authority.CAN_DO;
					authDex++;
					authDexStr=Integer.toString(authDex);
				}
				Clan.Position pos = new Clan.Position(oldID, oldRoleID, oldRank, oldName, oldPluralName, oldMax, oldMask, powerFuncs, oldIsPublic);
				posList.add(pos);
				posDex++;
				posDexStr=Integer.toString(posDex);
			}
			G.positions=posList.toArray(new Clan.Position[0]);
        }
        return "";
    }
}
