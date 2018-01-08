package com.planet_ink.coffee_mud.WebMacros;

import com.planet_ink.coffee_web.http.MultiPartData;
import com.planet_ink.coffee_web.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2003-2018 Bo Zimmerman

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
public class PlayerOnline extends StdWebMacro
{
	@Override
	public String name()
	{
		return "PlayerOnline";
	}

	public static final int MAX_IMAGE_SIZE=50*1024;

	@Override
	public String runMacro(HTTPRequest httpReq, String parm, HTTPResponse httpResp)
	{
		if(!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
			return CMProps.getVar(CMProps.Str.MUDSTATUS);

		final String last=httpReq.getUrlParameter("PLAYER");
		final java.util.Map<String,String> parms=parseParms(parm);
		if(last==null)
			return " @break@";
		if(last.length()>0)
		{
			if(parms.size()==0)
			{
				final MOB M = CMLib.players().getPlayer(last);
				return String.valueOf((M!=null)&&(M.session()!=null)&&(!M.session().isStopped()));
			}
			else
			{
				MOB M=CMLib.players().getLoadPlayer(last);
				if(M==null)
				{
					final MOB authM=Authenticate.getAuthenticatedMob(httpReq);
					if((authM!=null)&&(authM.Name().equalsIgnoreCase(last)))
						M=authM;
				}
				if(M!=null)
				{
					final String login=Authenticate.getLogin(httpReq);
					final String pass=Authenticate.getPassword(httpReq);
					if(Authenticate.authenticated(httpReq,login,pass))
					{
						boolean canBan=false;
						boolean canModify=false;
						boolean canBoot=false;

						final MOB authM=CMLib.players().getLoadPlayer(login);
						if((authM!=null)&&(authM.Name().equals(M.Name())))
						{
							canBan=true;
							canModify=true;
							canBoot=true;
						}
						else
						if(authM!=null)
						{
							if(CMSecurity.isAllowedEverywhere(authM,CMSecurity.SecFlag.BAN))
								canBan=true;
							if(CMSecurity.isAllowedEverywhere(authM,CMSecurity.SecFlag.CMDPLAYERS))
								canModify=true;
							if(CMSecurity.isAllowedEverywhere(authM,CMSecurity.SecFlag.BOOT))
								canBoot=true;
						}

						if(canBan&&(parms.containsKey("BANBYNAME")))
							CMSecurity.ban(last);
						if(canBan&&(parms.containsKey("BANBYIP")))
							CMSecurity.ban(M.session().getAddress());
						if(canModify&&(parms.containsKey("DELIMG")))
						{
							if(M.rawImage().length()>0)
							{
								M.setImage("");
								CMLib.database().DBUpdatePlayerMOBOnly(M);
							}
						}
						if(canModify&&(parms.containsKey("EXPIRENEVER")))
						{
							final PlayerStats P=M.playerStats();
							if(P!=null)
							{
								final List<String> secFlags=CMParms.parseSemicolons(P.getSetSecurityFlags(null),true);
								if(!secFlags.contains(CMSecurity.SecFlag.NOEXPIRE.name()))
								{
									secFlags.add(CMSecurity.SecFlag.NOEXPIRE.name());
									P.getSetSecurityFlags(CMParms.toSemicolonListString(secFlags));
								}
							}
							CMLib.database().DBUpdatePlayerMOBOnly(M);
						}
						if(canModify&&(parms.containsKey("EXPIRENOW")))
						{
							final PlayerStats P=M.playerStats();
							if(P!=null)
							{
								final List<String> secFlags=CMParms.parseSemicolons(P.getSetSecurityFlags(null),true);
								if(secFlags.contains(CMSecurity.SecFlag.NOEXPIRE.name()))
								{
									secFlags.remove(CMSecurity.SecFlag.NOEXPIRE.name());
									P.getSetSecurityFlags(CMParms.toSemicolonListString(secFlags));
								}
								P.setAccountExpiration(System.currentTimeMillis());
							}
							CMLib.database().DBUpdatePlayerMOBOnly(M);
						}
						if(canBan&&(parms.containsKey("BANBYEMAIL")))
							CMSecurity.ban(M.playerStats().getEmail());
						if(canModify&&(parms.containsKey("NEWIMAGE")))
						{
							Resources.removeResource("CMPORTRAIT-"+M.Name());
							String file="";
							byte[] buf=null;
							for(final MultiPartData data : httpReq.getMultiParts())
							{
								if(data.getVariables().containsKey("filename")
								&& (data.getContentType().startsWith("image")))
								{
									file=data.getVariables().get("filename");
									if(file==null)
										file="";
									buf=data.getData();
								}
							}
							if(file.length()==0)
								return "File not uploaded -- no name!";
							if(file.toUpperCase().endsWith(".GIF")
							||file.toUpperCase().endsWith(".JPG")
							||file.toUpperCase().endsWith(".JPEG")
							||file.toUpperCase().endsWith(".BMP"))
							{
								if(buf==null)
									return "File `"+file+"` not uploaded -- no buffer!";
								if(buf.length>MAX_IMAGE_SIZE)
									return "File `"+file+"` not uploaded -- size exceeds "+MAX_IMAGE_SIZE+" byte limit!";
								final String encoded=B64Encoder.B64encodeBytes(buf);
								M.setImage("PlayerPortrait?PLAYER="+M.Name()+"&FILENAME="+M.Name()+System.currentTimeMillis()+file);
								CMLib.database().DBUpdatePlayerMOBOnly(M);
								CMLib.database().DBReCreatePlayerData(M.Name(),"CMPORTRAIT","CMPORTRAIT-"+M.Name(),encoded);
								Resources.submitResource("CMPORTRAIT-"+M.Name(),buf);
								return "Image successfully uploaded.";
							}
							return "File not uploaded -- wrong type!";
						}

						if(M.session()!=null)
						{
							if(canBoot&&(parms.containsKey("BOOT")))
							{
								M.session().stopSession(false,false,false);
								return "false";
							}
							return "true";
						}
					}
				}
			}
		}
		return "false";
	}
}
