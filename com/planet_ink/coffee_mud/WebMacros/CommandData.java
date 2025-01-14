package com.planet_ink.coffee_mud.WebMacros;

import com.planet_ink.coffee_web.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMClass.CMObjectType;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.Command;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;

import java.util.*;

/*
   Copyright 2025-2025 Bo Zimmerman

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
public class CommandData extends StdWebMacro
{
	@Override
	public String name()
	{
		return "CommandData";
	}

	@Override
	public String runMacro(final HTTPRequest httpReq, final String parm, final HTTPResponse httpResp)
	{
		final java.util.Map<String,String> parms=parseParms(parm);

		final String replaceCommand=httpReq.getUrlParameter("REPLACE");
		if((replaceCommand != null)
		&& (replaceCommand.length()>0)
		&& (replaceCommand.indexOf('=')>0))
		{
			final int eq=replaceCommand.indexOf('=');
			final String field=replaceCommand.substring(0,eq);
			final String value=replaceCommand.substring(eq+1);
			httpReq.addFakeUrlParameter(field, value);
			httpReq.addFakeUrlParameter("REPLACE","");
		}

		String last=httpReq.getUrlParameter("COMMAND");
		if(last==null)
			return " @break@";
		if(last.length()>0)
		{
			if(parms.containsKey("ISGENERIC"))
			{
				final Command C2=CMClass.getCommand(last);
				return ""+((C2!=null)&&(C2.isGeneric()));
			}

			final String newCommandID=httpReq.getUrlParameter("NEWCOMMAND");
			Command C = (Command)httpReq.getRequestObjects().get("COMMAND-"+last);
			if((C==null)
			&&(newCommandID!=null)
			&&(newCommandID.length()>0)
			&&((CMClass.getCommand(newCommandID)==null)
				||(!CMClass.getCommand(newCommandID).isGeneric())))
			{
				C=(Command)CMClass.getCommand("GenCommand").copyOf();
				last=newCommandID;
				httpReq.addFakeUrlParameter("COMMAND",newCommandID);
				httpReq.addFakeUrlParameter("NEWCOMMAND",newCommandID);
			}
			if(C==null)
				C=CMClass.getCommand(last);
			if(parms.containsKey("ISNEWCOMMAND"))
				return ""+(((httpReq.isUrlParameter("NEWCOMMAND"))&&(httpReq.getUrlParameter("NEWCOMMAND").length()>0))
						||(!C.isGeneric()));

			if(C!=null)
			{
				final StringBuffer str=new StringBuffer("");
				if(parms.containsKey("HELP"))
				{
					String old=httpReq.getUrlParameter("HELP");
					if(old==null)
					{
						String word = C.ID();
						if((C.getAccessWords()!=null)&&(C.getAccessWords().length>0))
							word=C.getAccessWords()[0];
						if(C instanceof Modifiable)
							old=((Modifiable)C).getStat("HELP");
						else
						{
							final MOB M = CMClass.getFactoryMOB();
							try
							{
								old=CMStrings.replaceAll(CMLib.help().getHelpText(word,null,!C.securityCheck(M),true),"\r","");
							}
							finally
							{
								M.destroy();
							}
						}
					}
					int limit=Integer.MAX_VALUE;
					if(parms.containsKey("LIMIT"))
						limit=CMath.s_int(parms.get("LIMIT"));
					str.append(CMStrings.limit(old,limit));
				}
				if(parms.containsKey("SCRIPT"))
				{
					String old=httpReq.getUrlParameter("SCRIPT");
					if(old==null)
					{
						if(C instanceof Modifiable)
						{
							old=((Modifiable)C).getStat("SCRIPT");
							if(old.length()==0)
							{
								final StringBuilder script = new StringBuilder("");
								script.append("FUNCTION_PROG EXECUTE\n");
								script.append("  MPECHO Number of arguments: $0\n");
								script.append("  MPECHO All Arguments      : $g\n");
								script.append("  FOR $1 = 0 to $0\n");
								script.append("    MPECHO Argument#$1         : $g.$1\n");
								script.append("  NEXT\n");
								script.append("  RETURN true\n");
								script.append("~\n");
								old = script.toString();
							}
						}
						else
						if(!C.isGeneric())
						{
							final StringBuilder script = new StringBuilder("");
							final String fullClassName = C.getClass().getCanonicalName();
							script.append("FUNCTION_PROG EXECUTE\n");
							script.append("<SCRIPT>\n");
							script.append("  var c = new Packages."+fullClassName+"();\n");
							script.append("  var l = new Packages.com.planet_ink.coffee_mud.core.collections.XArrayList();\n");
							script.append("  for(var i=0;i<Number(objs()[0]);i++)\n");
							script.append("    if((''+objs()[i+1]).length>0)\n");
							script.append("      l.add(''+objs()[i+1]);\n");
							script.append("  var r = c.execute(source(),l,0);\n");
							script.append("  objs()[0] = ''+r;\n");
							script.append("</SCRIPT>\n");
							script.append("RETURN $0\n");
							script.append("~\n");
							old=script.toString();
						}
					}
					if(old != null)
						str.append(old+", ");
				}
				if(parms.containsKey("NAME"))
				{
					String old=httpReq.getUrlParameter("NAME");
					if(old==null)
						old=C.name();
					str.append(old+", ");
				}
				if(parms.containsKey("ISGENERIC"))
					str.append(C.isGeneric()+", ");
				if(parms.containsKey("SECMASK"))
				{
					String old=httpReq.getUrlParameter("SECMASK");
					if(old==null)
					{
						if(C instanceof Modifiable)
							old=((Modifiable)C).getStat("SECMASK");
						else
						{
							final MOB M = CMClass.getFactoryMOB();
							try
							{
								old=C.securityCheck(M)?"":"+SYSOP -NAMES";
							}
							finally
							{
								M.destroy();
							}
						}
					}
					str.append(old+", ");
				}
				if(parms.containsKey("WORDLIST"))
				{
					String old=httpReq.getUrlParameter("WORDLIST");
					if(old==null)
						old=CMParms.toListString(C.getAccessWords());
					str.append(old+", ");
				}
				if(parms.containsKey("CANORDER"))
				{
					String old=httpReq.getUrlParameter("CANORDER");
					if(old==null)
						old=""+C.canBeOrdered();
					str.append(old+", ");
				}
				if(parms.containsKey("CANCANCEL"))
				{
					String old=httpReq.getUrlParameter("CANCANCEL");
					if(old==null)
						old=""+C.canBeCancelled();
					str.append(old+", ");
				}
				if(parms.containsKey("ACOST"))
				{
					String old=httpReq.getUrlParameter("ACOST");
					if(old == null)
					{
						if(C instanceof Modifiable)
							old=""+((Modifiable)C).getStat("ACTCOST");
						else
						{
							final String cmdWord = (C.getAccessWords()!=null)&&C.getAccessWords().length>0
									?C.getAccessWords()[0]:"WORD";
							final MOB rM = Authenticate.getAuthenticatedMob(httpReq);
							if(rM != null)
								old=""+C.actionsCost(rM, new XArrayList<String>(cmdWord));
							else
							{
								final MOB M = CMClass.getFactoryMOB();
								try
								{
									old=""+C.actionsCost(M, new XArrayList<String>(cmdWord));
								}
								finally
								{
									M.destroy();
								}
							}
							if(old.equalsIgnoreCase(""+CMProps.getCommandActionCost(C.ID())))
								old="-1.0";
						}
					}
					str.append(old+", ");
				}
				if(parms.containsKey("CCOST"))
				{
					String old=httpReq.getUrlParameter("CCOST");
					if(C instanceof Modifiable)
						old=""+((Modifiable)C).getStat("CBTCOST");
					else
					{
						final String cmdWord = (C.getAccessWords()!=null)&&C.getAccessWords().length>0
								?C.getAccessWords()[0]:"WORD";
						final MOB rM = Authenticate.getAuthenticatedMob(httpReq);
						if(rM != null)
							old=""+C.combatActionsCost(rM, new XArrayList<String>(cmdWord));
						else
						{
							final MOB M = CMClass.getFactoryMOB();
							try
							{
								old=""+C.combatActionsCost(M, new XArrayList<String>(cmdWord));
							}
							finally
							{
								M.destroy();
							}
						}
						if(old.equalsIgnoreCase(""+CMProps.getCommandCombatActionCost(C.ID())))
							old="-1.0";
					}
					str.append(old+", ");
				}
				String strstr=str.toString();
				if(strstr.endsWith(", "))
					strstr=strstr.substring(0,strstr.length()-2);
				httpReq.getRequestObjects().put("COMMAND-"+last,C);
				return clearWebMacros(strstr);
			}
		}
		return "";
	}
}
