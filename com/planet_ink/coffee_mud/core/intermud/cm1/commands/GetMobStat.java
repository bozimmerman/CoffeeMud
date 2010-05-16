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
public class GetMobStat extends CM1Command
{
	public String getCommandWord(){ return "GETMOBSTAT";}
	public GetMobStat(RequestHandler req, String parameters) {
		super(req, parameters);
	}
	
	protected static final String[] TYPES={"SESSION","MOB","CHAR","STATE","PHYSICAL","BASECHAR","MAXSTATE","BASESTATE","BASEPHYSICAL","PLAYERSTATS"};
	public Modifiable getModifiable(String type, MOB M)
	{
		int x=CMParms.indexOf(TYPES,type.toUpperCase().trim());
		if(x<0) return null;
		switch(x)
		{
		case 0: return M.session();
		case 1: return M;
		case 2: return M.charStats();
		case 3: return M.curState();
		case 4: return M.phyStats();
		case 5: return M.baseCharStats();
		case 6: return M.maxState();
		case 7: return M.baseState();
		case 8: return M.basePhyStats();
		case 9: return M.playerStats();
		}
		return null;
	}
	
	public boolean isStdMOB(MOB M, Modifiable m)
	{
		return (M!=null)&&(!M.isGeneric())&&(m instanceof MOB);
	}

	public String[] getStatCodes(MOB M, Modifiable m)
	{
		if(!isStdMOB(M,m))
			return m.getStatCodes();
		return GenericBuilder.GENMOBCODES;
	}

	public boolean isAStat(MOB M, Modifiable m, String stat)
	{
		if(!isStdMOB(M,m))
			return m.isStat(stat);
        for(int i=0;i<GenericBuilder.GENMOBCODES.length;i++)
            if(GenericBuilder.GENMOBCODES[i].equalsIgnoreCase(stat))
            	return true;
        return false;
	}

	public void run()
	{
		try
		{
			MOB M=null;
			if(req.getTarget() instanceof MOB)
				M=(MOB)req.getTarget();
			if(M==null)
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
			Modifiable mod=getModifiable(type,M);
			if(mod==null)
			{
				req.sendMsg("[FAIL "+getHelp(M)+"]");
				return;
			}
			if((stat.length()==0)||(!isAStat(M,mod,stat)))
			{
				req.sendMsg("[FAIL USAGE: GETMOBSTAT "+type+" "+CMParms.toStringList(getStatCodes(M,mod))+"]");
				return;
			}
			if(!isStdMOB(M,mod))
				req.sendMsg("[OK "+mod.getStat(stat)+"]");
			else
		        for(int i=0;i<GenericBuilder.GENMOBCODES.length;i++)
		            if(GenericBuilder.GENMOBCODES[i].equalsIgnoreCase(stat))
						req.sendMsg("[OK "+CMLib.coffeeMaker().getGenMobStat(M, stat)+"]");
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
			return false;
	}
	public String getHelp(MOB user)
	{
		return "USAGE: GETMOBSTAT "+CMParms.toStringList(TYPES);
	}
}
