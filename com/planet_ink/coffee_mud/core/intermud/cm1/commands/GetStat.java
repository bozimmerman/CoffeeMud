package com.planet_ink.coffee_mud.core.intermud.cm1.commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.intermud.cm1.RequestHandler;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.GenericBuilder;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.io.*;
import java.util.concurrent.atomic.*;

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
public class GetStat extends CM1Command
{
	public String getCommandWord(){ return "GETSTAT";}
	public GetStat(RequestHandler req, String parameters) {
		super(req, parameters);
	}
	
	protected static final String[] STATTYPES={"SESSION","MOB","CHAR","STATE","PHYSICAL","BASECHAR","MAXSTATE","BASESTATE","BASEPHYSICAL","PLAYERSTATS", "ITEM", "EXIT", "ROOM", "AREA"};
	protected static final String[] TYPESTYPE={"P",		 "MP", "MP",  "MP",   "MPIREA",  "MP",      "MP",      "MP",       "MPIREA",      "P",           "I",    "E",    "R",    "A"};
	public char getTypeCode(Physical P)
	{
		if(P instanceof MOB) return ((MOB)P).isMonster()?'M':'P';
		if(P instanceof Item) return 'I';
		if(P instanceof Room) return 'R';
		if(P instanceof Exit) return 'E';
		if(P instanceof Area) return 'A';
		return ' ';
	}
	
	public boolean isApplicableTypeCode(String type, Physical P)
	{
		char c=getTypeCode(P);
		for(int i=0;i<STATTYPES.length;i++)
			if(STATTYPES[i].equalsIgnoreCase(type))
				return TYPESTYPE[i].indexOf(c)>=0;
		return false;
	}
	
	public String[] getApplicableStatCodes(Physical P)
	{
		char c=getTypeCode(P);
		List<String> majorCodes = new LinkedList<String>();
		for(int i=0;i<STATTYPES.length;i++)
			if(TYPESTYPE[i].indexOf(c)>=0)
				majorCodes.add(STATTYPES[i]);
		return majorCodes.toArray(new String[0]);
	}
	
	public Modifiable getModifiable(String type, Physical E)
	{
		int x=CMParms.indexOf(STATTYPES,type.toUpperCase().trim());
		if(x<0) return null;
		if(!isApplicableTypeCode(type,E))
			return null;
		
		switch(x)
		{
		case 0: return ((MOB)E).session();
		case 1: return (Modifiable)E;
		case 2: return ((MOB)E).charStats();
		case 3: return ((MOB)E).curState();
		case 4: return ((Physical)E).phyStats();
		case 5: return ((MOB)E).baseCharStats();
		case 6: return ((MOB)E).maxState();
		case 7: return ((MOB)E).baseState();
		case 8: return ((Physical)E).basePhyStats();
		case 9: return ((MOB)E).playerStats();
		case 10:return (Modifiable)E;
		case 11:return (Modifiable)E;
		case 12:return (Modifiable)E;
		case 13:return (Modifiable)E;
		}
		return null;
	}
	
	public boolean UseGenBuilder(Physical P, Modifiable m)
	{
		return (P!=null)&&(!P.isGeneric())
				&&((m instanceof MOB)||(m instanceof Item));
	}

	public String[] getStatCodes(Physical P, Modifiable m)
	{
		if(!UseGenBuilder(P,m))
			return m.getStatCodes();
		if(m instanceof MOB)
			return GenericBuilder.GENMOBCODES;
		if(m instanceof Item)
			return GenericBuilder.GENITEMCODES;
		return null;
	}

	public boolean isAStat(Physical P, Modifiable m, String stat)
	{
		if(!UseGenBuilder(P,m))
			return m.isStat(stat);
		String[] codes = getStatCodes(P,m);
		if(codes != null)
	        for(int i=0;i<codes.length;i++)
	            if(codes[i].equalsIgnoreCase(stat))
	            	return true;
        return false;
	}

	public void run()
	{
		try
		{
			Physical P=null;
			if(req.getTarget() instanceof Physical)
				P=(Physical)req.getTarget();
			if(P==null)
			{
				req.sendMsg("[FAIL NOT LOGGED IN]");
				return;
			}
			String stat = "";
			String type = parameters.toUpperCase().trim();
			int x=parameters.indexOf(' ');
			if(x>0)
			{
				type=parameters.substring(0,x).toUpperCase().trim();
				stat=parameters.substring(x+1).toUpperCase().trim();
			}
			Modifiable mod=getModifiable(type,P);
			if(mod==null)
			{
				req.sendMsg("[FAIL "+getHelp(req.getUser(), P, "")+"]");
				return;
			}
			if((stat.length()==0)||(!isAStat(P,mod,stat)))
			{
				req.sendMsg("[FAIL USAGE: GETSTAT "+type+" "+CMParms.toStringList(getStatCodes(P,mod))+"]");
				return;
			}
			if(!UseGenBuilder(P,mod))
				req.sendMsg("[OK "+mod.getStat(stat)+"]");
			else
			{
				String[] codes = this.getStatCodes(P, mod);
		        for(int i=0;i<codes.length;i++)
		            if(codes[i].equalsIgnoreCase(stat))
		    			if(P instanceof MOB)
							req.sendMsg("[OK "+CMLib.coffeeMaker().getGenMobStat((MOB)P, stat)+"]");
		    			else
		    			if(P instanceof Item)
							req.sendMsg("[OK "+CMLib.coffeeMaker().getGenItemStat((Item)P, stat)+"]");
		        		
			}
		}
		catch(Exception ioe)
		{
			Log.errOut(className,ioe);
			req.close();
		}
	}
	public boolean passesSecurityCheck(MOB user, PhysicalAgent target)
	{
		if(user==null) return false;
		if(target instanceof MOB)
		{
			if(CMLib.players().playerExists(target.Name()))
				return CMSecurity.isAllowed(user,user.location(),"CMDPLAYERS");
			return CMSecurity.isAllowed(user,user.location(),"CMDMOBS");
		}
		else
		if(target instanceof Item)
			return CMSecurity.isAllowed(user,user.location(),"CMDITEMS");
		else
		if(target instanceof Room)
			return CMSecurity.isAllowed(user,user.location(),"CMDROOMS");
		else
		if(target instanceof Exit)
			return CMSecurity.isAllowed(user,user.location(),"CMDEXITS");
		else
		if(target instanceof Area)
			return CMSecurity.isAllowed(user,user.location(),"CMDAREAS");
		else
			return false;
	}
	public String getHelp(MOB user, Physical target, String rest)
	{
		return "USAGE: GETSTAT "+CMParms.toStringList(getApplicableStatCodes(target));
	}
}
