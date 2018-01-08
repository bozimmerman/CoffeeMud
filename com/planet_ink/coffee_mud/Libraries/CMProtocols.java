package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.exceptions.BadEmailAddressException;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMProps.Str;
import com.planet_ink.coffee_mud.core.CMSecurity.DbgFlag;
import com.planet_ink.coffee_mud.core.MiniJSON.MJSONException;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.CharCreationLibrary.LoginSession;
import com.planet_ink.coffee_mud.Libraries.interfaces.PlayerLibrary.ThinPlayer;
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
import com.planet_ink.coffee_mud.core.MiniJSON;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.Map.Entry;

import org.mozilla.javascript.*;
import org.mozilla.javascript.optimizer.*;
/*
   Copyright 2013-2018 Bo Zimmerman

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
public class CMProtocols extends StdLibrary implements ProtocolLibrary
{
	@Override
	public String ID()
	{
		return "CMProtocols";
	}

	// this is the sound support method.
	// it builds a valid MSP sound code from built-in web server
	// info, and the info provided.
	public String msp(final String soundName, final int volume, final int priority)
	{
		if((soundName==null)||(soundName.length()==0)||CMSecurity.isDisabled(CMSecurity.DisFlag.MSP))
			return "";
		final String mspSoundPath=CMProps.getVar(Str.MSPPATH);
		if(mspSoundPath.length()>0)
			return " !!SOUND("+soundName+" V="+volume+" P="+priority+" U="+mspSoundPath+") ";
		return " !!SOUND("+soundName+" V="+volume+" P="+priority+") ";
	}

	private enum McpParseStartState
	{
		START,
		IN_COMMAND,
		FINISH_COMMAND,
		IN_MCPKEY,
		FINISH_MCPKEY,
		IN_KEY,
		FINISH_IN_KEY,
		FINISH_KEY,
		IN_VAL,
		IN_QUOTEVAL,
		FINISH_VAL
	}

	protected static String MCP_KEYSENT_KEY() 
	{ 
		return "_CMMCP_KEYSENT";
	}
	
	protected static String MCP_COMMAND_KEY() 
	{ 
		return "_CMMCP_COMMAND";
	}
	
	protected static String MCP_DATA_TAG()
	{
		return "_data-tag";
	}

	protected boolean containsMcpStarTag(Map<String,String> keyValuePairs)
	{
		for(String k : keyValuePairs.keySet())
		{
			if(k.endsWith("*"))
			{
				return true;
			}
		}
		return false;
	}
	
	protected boolean parseMcpStart(final String s, boolean[] exec, Map<String,String> keyValuePairs)
	{
		McpParseStartState state = McpParseStartState.START;
		String lastKey = "";
		int startIndex = 0;
		for(int i=0;i<s.length();i++)
		{
			char c=s.charAt(i);
			switch(state)
			{
			case FINISH_COMMAND:
				if(!Character.isWhitespace(c))
				{
					startIndex=i;
					state=McpParseStartState.IN_MCPKEY;
				}
				else
				{
					Log.errOut("Invalid MCP "+state.toString()+": "+c+": "+s);
					return false;
				}
				break;
			case FINISH_KEY:
				if(Character.isWhitespace(c))
				{
					keyValuePairs.put(lastKey, "");
					state = McpParseStartState.FINISH_VAL;
				}
				else
				{
					if(c=='\"')
					{
						startIndex = i+1;
						state = McpParseStartState.IN_QUOTEVAL;
					}
					else
					{
						startIndex = i;
						state = McpParseStartState.IN_VAL;
					}
				}
				break;
			case FINISH_MCPKEY:
				if(!Character.isWhitespace(c))
				{
					startIndex = i;
					state = McpParseStartState.IN_KEY;
				}
				exec[0]=true;
				break;
			case FINISH_VAL:
				if(!Character.isWhitespace(c))
				{
					startIndex = i;
					state = McpParseStartState.IN_KEY;
				}
				break;
			case IN_COMMAND:
				if(Character.isWhitespace(c))
				{
					keyValuePairs.put(MCP_COMMAND_KEY(), s.substring(startIndex,i));
					if(s.substring(startIndex,i).equalsIgnoreCase("mcp"))
						state = McpParseStartState.FINISH_MCPKEY;
					else
						state = McpParseStartState.FINISH_COMMAND;
				}
				break;
			case FINISH_IN_KEY:
				if(!Character.isWhitespace(c))
				{
					Log.errOut("Invalid MCP "+state.toString()+": "+c+": "+s);
					return false;
				}
				else
				{
					state = McpParseStartState.FINISH_KEY;
				}
				break;
			case IN_KEY:
				if(Character.isWhitespace(c))
				{
					Log.errOut("Invalid MCP "+state.toString()+": "+c+": "+s);
					return false;
				}
				else
				if(c==':')
				{
					lastKey = s.substring(startIndex,i);
					if(lastKey.trim().endsWith("*"))
					{
						exec[0]=false;
					}
					state = McpParseStartState.FINISH_IN_KEY;
				}
				break;
			case IN_MCPKEY:
				if(Character.isWhitespace(c))
				{
					keyValuePairs.put(MCP_KEYSENT_KEY(), s.substring(startIndex,i));
					state = McpParseStartState.FINISH_MCPKEY;
				}
				break;
			case IN_QUOTEVAL:
				if((c=='\"')&&(s.charAt(i-1)!='\\'))
				{
					keyValuePairs.put(lastKey, s.substring(startIndex,i));
					state = McpParseStartState.FINISH_VAL;
				}
				break;
			case IN_VAL:
				if(Character.isWhitespace(c))
				{
					keyValuePairs.put(lastKey, s.substring(startIndex,i));
					state = McpParseStartState.FINISH_VAL;
				}
				break;
			case START:
				if(!Character.isWhitespace(c))
				{
					startIndex=i;
					state=McpParseStartState.IN_COMMAND;
				}
				else
				{
					Log.errOut("Invalid MCP "+state.toString()+": "+c+": "+s);
					return false;
				}
				break;
			}
		}
		switch(state)
		{
		case IN_VAL:
			keyValuePairs.put(lastKey, s.substring(startIndex));
			break;
		case FINISH_KEY:
			keyValuePairs.put(lastKey, "");
			break;
		case IN_MCPKEY:
			keyValuePairs.put(MCP_KEYSENT_KEY(), s.substring(startIndex));
			exec[0]=true;
			break;
		default:
			Log.errOut("Invalid MCP END "+state.toString()+": " + s);
			return false;
		}
		if((!keyValuePairs.containsKey(MCP_COMMAND_KEY()))
		||((!keyValuePairs.get(MCP_COMMAND_KEY()).equalsIgnoreCase("mcp"))
			&&(!keyValuePairs.containsKey(MCP_KEYSENT_KEY()))))
		{
			Log.errOut("Invalid MCP -- missing Command or Key: " + s);
			return false;
		}
		if((!exec[0]) && (!keyValuePairs.containsKey(MCP_DATA_TAG())))
		{
			Log.errOut("Missing MCP Data Tag: " + s);
			return false;
		}
		if((!exec[0]) && (!containsMcpStarTag(keyValuePairs)))
		{
			Log.errOut("Missing MCP Star Tag: " + s);
			return false;
		}
		return true;
	}
	
	private enum McpParseContState
	{
		START,
		IN_CONTMCPKEY,
		FINISH_CONTMCPKEY,
		IN_CONTKEY,
		FINISH_IN_CONTKEY,
		FINISH_CONTKEY,
		IN_LINE
	}

	protected boolean parseMcpCont(final String s, boolean[] exec, Map<String,String> keyValuePairs)
	{
		McpParseContState state = McpParseContState.START;
		String lastKey = "";
		int startIndex = 0;
		exec[0] = false; // never end on a continue tag
		for(int i=1;i<s.length();i++)
		{
			char c=s.charAt(i);
			switch(state)
			{
			case FINISH_CONTKEY:
				startIndex = i;
				state = McpParseContState.IN_LINE;
				break;
			case FINISH_CONTMCPKEY:
				if(!Character.isWhitespace(c))
				{
					startIndex = i;
					state = McpParseContState.IN_CONTKEY;
				}
				break;
			case IN_CONTKEY:
				if(Character.isWhitespace(c))
				{
					Log.errOut("Invalid MCPC "+state.toString()+": "+c+": "+s);
					return false;
				}
				else
				if(c==':')
				{
					lastKey = s.substring(startIndex,i);
					if(!keyValuePairs.containsKey(lastKey+"*"))
					{
						Log.errOut("Unknown CONT KEY: "+lastKey+": "+state.toString()+": "+c+": "+s);
						return false;
					}
					state = McpParseContState.FINISH_IN_CONTKEY;
				}
				break;
			case FINISH_IN_CONTKEY:
				if(!Character.isWhitespace(c))
				{
					Log.errOut("Unknown/Invalid/Bad MCP: "+state.toString()+": "+c+": "+s);
					return false;
				}
				state = McpParseContState.FINISH_CONTKEY;
				break;
			case IN_CONTMCPKEY:
				if(Character.isWhitespace(c))
				{
					if((!keyValuePairs.containsKey(MCP_DATA_TAG()))
					||(!s.substring(startIndex,i).equals(keyValuePairs.get(MCP_DATA_TAG()))))
					{
						Log.errOut("Unknown/Invalid CONT MCP KEY: "+s.substring(startIndex,i)+": "+state.toString()+": "+c+": "+s);
						return false;
					}
					state = McpParseContState.FINISH_CONTMCPKEY;
				}
				break;
			case START:
				if(!Character.isWhitespace(c))
				{
					startIndex=i;
					state=McpParseContState.IN_CONTMCPKEY;
				}
				break;
			case IN_LINE:
				break;
			default:
				break;
			}
		}
		switch(state)
		{
		case IN_LINE:
			if(keyValuePairs.containsKey(lastKey))
				keyValuePairs.put(lastKey, keyValuePairs.get(lastKey)+"\n\r"+s.substring(startIndex));
			else
				keyValuePairs.put(lastKey, s.substring(startIndex));
			break;
		case FINISH_IN_CONTKEY:
		case FINISH_CONTKEY:
			if(keyValuePairs.containsKey(lastKey))
				keyValuePairs.put(lastKey, keyValuePairs.get(lastKey)+"\n\r");
			else
				keyValuePairs.put(lastKey, "");
			break;
		default:
			Log.errOut("Invalid MCP CONT "+state.toString()+": " + s);
			return false;
		}
		return true;
	}
	
	protected boolean parseMcpEnd(final String s, boolean[] exec, Map<String,String> keyValuePairs)
	{
		String endKey = s.substring(1).trim();
		if((!keyValuePairs.containsKey(MCP_COMMAND_KEY()))
		||((!keyValuePairs.get(MCP_COMMAND_KEY()).equalsIgnoreCase("mcp"))
			&&(!endKey.equalsIgnoreCase(keyValuePairs.get(MCP_DATA_TAG())))))
		{
			Log.errOut("Unknown/Invalid CONT MCP KEY: "+endKey+": "+s);
			return false;
		}
		exec[0] = true;
		return true;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public boolean mcp(final Session session, final StringBuilder str, String[] mcpKey, 
					   final Map<String,float[]> clientSupported, final Map<String,String> keyValuePairs)
	{
		Map<String,MCPPackage> mcpPackages = (Map<String,MCPPackage>)Resources.getResource("MCP_COMPILED_PACKAGES");
		if(mcpPackages == null)
		{
			synchronized(this)
			{
				mcpPackages = (Map<String,MCPPackage>)Resources.getResource("MCP_COMPILED_PACKAGES");
				if(mcpPackages == null)
				{
					mcpPackages = new Hashtable<String,MCPPackage>();
					List<MCPPackage> pkgs = new ArrayList<MCPPackage>();
					if(CMClass.loadObjectListToObj(pkgs, "com/planet_ink/coffee_mud/Libraries/mcppkgs/", CMProps.instance().getStr("MCPPACKAGES"), "com.planet_ink.coffee_mud.Libraries.interfaces.ProtocolLibrary$MCPPackage"))
					{
						for(MCPPackage pkg : pkgs )
						{
							mcpPackages.put(pkg.packageName(), pkg);
						}
					}
					Resources.submitResource("MCP_COMPILED_PACKAGES", mcpPackages);
				}
			}
		}
		
		String s = str.substring(3).trim();
		boolean[] execute = new boolean[1];
		if(s.length()==0)
			return false;
		if(s.charAt(0)=='*')
		{
			if(!parseMcpCont(s,execute,keyValuePairs))
			{
				return false;
			}
		}
		else
		if(s.charAt(0)==':')
		{
			if(!parseMcpEnd(s,execute,keyValuePairs))
			{
				return false;
			}
		}
		else
		{
			if(!parseMcpStart(s,execute,keyValuePairs))
			{
				return false;
			}
		}
		if(execute[0])
		{
			if((!keyValuePairs.containsKey(MCP_COMMAND_KEY()))
			||((!keyValuePairs.get(MCP_COMMAND_KEY()).equals("mcp"))
				&&(!keyValuePairs.containsKey(MCP_KEYSENT_KEY()))))
			{
				Log.errOut("Invalid MCP PROCESS -- no command or key sent: " + s);
				return false;
			}
			String pkgCmd = keyValuePairs.get(MCP_COMMAND_KEY());
			String keySent = keyValuePairs.get(MCP_KEYSENT_KEY());
			if((keySent != null)&&(mcpKey[0]!=null)&&(!keySent.equals(mcpKey[0])))
			{
				Log.errOut("Invalid MCP PROCESS -- invalid key sent: " + keySent+": "+mcpKey[0]+": "+s);
				return false;
			}
			else
			if(pkgCmd.equals("mcp"))
			{
				if(keyValuePairs.containsKey("authentication-key"))
				{
					mcpKey[0] = keyValuePairs.get("authentication-key");
				}
				else
				{
					Log.errOut("Invalid MCP PROCESS -- NO key Sent: "+s);
					return false;
				}
				for(String commandKey : mcpPackages.keySet())
				{
					MCPPackage pkg = mcpPackages.get(commandKey);
					session.rawPrintln("#$#mcp-negotiate-can "+mcpKey[0]+" package: "+pkg.packageName()+" min-version: "+pkg.minVersion()+" max-version: "+pkg.maxVersion());
				}
				session.rawPrintln("#$#mcp-negotiate-end "+mcpKey[0]);
			}
			else
			{
				MCPPackage pkg = mcpPackages.get(pkgCmd);
				if(pkg == null)
				{
					for(String commandKey : mcpPackages.keySet())
					{
						if(pkgCmd.startsWith(commandKey+"-"))
						{
							pkg = mcpPackages.get(commandKey);
							break;
						}
					}
				}
				if(pkg != null)
				{
					pkg.executePackage(session, pkgCmd, clientSupported, keyValuePairs);
				}
			}
			keyValuePairs.clear();
		}
		return true;
	}

	@Override
	public String[] mxpImagePath(String fileName)
	{
		if((fileName==null)||(fileName.trim().length()==0))
			return new String[]{"",""};
		final String mxpImagePath=CMProps.getVar(Str.MXPIMAGEPATH);
		if((mxpImagePath.length()==0)
		||(CMSecurity.isDisabled(CMSecurity.DisFlag.MXP)))
			return new String[]{"",""};
		int x=fileName.lastIndexOf('=');
		String preFilename="";
		if(x>=0)
		{
			preFilename=fileName.substring(0,x+1);
			fileName=fileName.substring(x+1);
		}
		x=fileName.lastIndexOf('/');
		if(x>=0)
		{
			preFilename+=fileName.substring(0,x+1);
			fileName=fileName.substring(x+1);
		}
		if(mxpImagePath.endsWith("/"))
			return new String[]{mxpImagePath+preFilename,fileName};
		return new String[]{mxpImagePath+"/"+preFilename,fileName};
	}

	@Override
	public String mxpImage(final Environmental E, final String parms)
	{
		if((CMProps.getVar(Str.MXPIMAGEPATH).length()==0)
		||(CMSecurity.isDisabled(CMSecurity.DisFlag.MXP)))
			return "";
		final String image=E.image();
		if(image.length()==0)
			return "";
		final String[] fixedFilenames=mxpImagePath(image);
		if(fixedFilenames[0].length()==0)
			return "";
		return "^<IMAGE '"+fixedFilenames[1]+"' URL=\""+fixedFilenames[0]+"\" "+parms+"^>^N";
	}

	@Override
	public String mxpImage(final Environmental E, final String parms, final String pre, final String post)
	{
		if((CMProps.getVar(Str.MXPIMAGEPATH).length()==0)
		||(CMSecurity.isDisabled(CMSecurity.DisFlag.MXP)))
			return "";
		final String image=E.image();
		if(image.length()==0)
			return "";
		final String[] fixedFilenames=mxpImagePath(image);
		if(fixedFilenames[0].length()==0)
			return "";
		return pre+"^<IMAGE '"+fixedFilenames[1]+"' URL=\""+fixedFilenames[0]+"\" "+parms+"^>^N"+post;
	}

	@SuppressWarnings({"unchecked","rawtypes"})
	public String getHashedMXPImage(final String key)
	{
		Map<String,String> H=(Map)Resources.getResource("MXP_IMAGES");
		if(H==null)
			getDefaultMXPImage(null);
		H=(Map)Resources.getResource("MXP_IMAGES");
		if(H==null)
			return "";
		return getHashedMXPImage(H,key);

	}

	@Override
	public String msp(final String soundName, final int priority)
	{
		return msp(soundName,50,CMLib.dice().roll(1,50,priority));
	}

	public String getHashedMXPImage(final Map<String, String> H, final String key)
	{
		if(H==null)
			return "";
		final String s=H.get(key);
		if(s==null)
			return null;
		if(s.trim().length()==0)
			return null;
		if(s.equalsIgnoreCase("NULL"))
			return "";
		return s;
	}

	@Override @SuppressWarnings({"unchecked","rawtypes"})
	public String getDefaultMXPImage(final Object O)
	{
		if((CMProps.getVar(Str.MXPIMAGEPATH).length()==0)
		||(CMSecurity.isDisabled(CMSecurity.DisFlag.MXP)))
			return "";
		Map<String,String> H=(Map)Resources.getResource("PARSED: mxp_images.ini");
		if(H==null)
		{
			H=new Hashtable<String,String>();
			final List<String> V=Resources.getFileLineVector(new CMFile("resources/mxp_images.ini",null).text());
			if((V!=null)&&(V.size()>0))
			{
				String s=null;
				int x=0;
				for(int v=0;v<V.size();v++)
				{
					s=V.get(v).trim();
					if(s.startsWith("//")||s.startsWith(";"))
						continue;
					x=s.indexOf('=');
					if(x<0)
						continue;
					if(s.substring(x+1).trim().length()>0)
						H.put(s.substring(0,x),s.substring(x+1));
				}
			}
			Resources.submitResource("PARSED: mxp_images.ini",H);
		}
		String image=null;
		if(O instanceof Race)
		{
			image=getHashedMXPImage(H,"RACE_"+((Race)O).ID().toUpperCase());
			if(image==null)
				image=getHashedMXPImage(H,"RACECAT_"+((Race)O).racialCategory().toUpperCase().replace(' ','_'));
			if(image==null)
				image=getHashedMXPImage(H,"RACE_*");
			if(image==null)
				image=getHashedMXPImage(H,"RACECAT_*");
		}
		else
		if(O instanceof MOB)
		{
			final String raceName=((MOB)O).charStats().raceName();
			Race R=null;
			for(final Enumeration<Race> e=CMClass.races();e.hasMoreElements();)
			{
				R=e.nextElement();
				if(raceName.equalsIgnoreCase(R.name()))
					image=getDefaultMXPImage(R);
			}
			if(image==null)
				image=getDefaultMXPImage(((MOB)O).charStats().getMyRace());
		}
		else
		if(O instanceof Room)
		{
			image=getHashedMXPImage(H,"ROOM_"+((Room)O).ID().toUpperCase());
			if(image==null)
				if(CMath.bset(((Room)O).domainType(),Room.INDOORS))
					image=getHashedMXPImage(H,"LOCALE_INDOOR_"+Room.DOMAIN_INDOORS_DESCS[((Room)O).domainType()-Room.INDOORS]);
				else
					image=getHashedMXPImage(H,"LOCALE_"+Room.DOMAIN_OUTDOOR_DESCS[((Room)O).domainType()]);
			if(image==null)
				image=getHashedMXPImage(H,"ROOM_*");
			if(image==null)
				image=getHashedMXPImage(H,"LOCALE_*");
		}
		else
		if(O instanceof Exit)
		{
			image=getHashedMXPImage(H,"EXIT_"+((Exit)O).ID().toUpperCase());
			if(image==null)
				image=getHashedMXPImage(H,"EXIT_"+((Exit)O).doorName().toUpperCase());
			if(image==null)
				if(((Exit)O).hasADoor())
					image=getHashedMXPImage(H,"EXIT_WITHDOOR");
				else
					image=getHashedMXPImage(H,"EXIT_OPEN");
			if(image==null)
				image=getHashedMXPImage(H,"EXIT_*");
		}
		else
		if(O instanceof Rideable)
		{
			image=getHashedMXPImage(H,"RIDEABLE_"+Rideable.RIDEABLE_DESCS[((Rideable)O).rideBasis()]);
			if(image==null)
				image=getHashedMXPImage(H,"RIDEABLE_*");
		}
		else
		if(O instanceof Shield)
		{
			image=getHashedMXPImage(H,"SHIELD_"+RawMaterial.Material.findByMask(((Shield)O).material()&RawMaterial.MATERIAL_MASK).desc());
			if(image==null)
				image=getHashedMXPImage(H,"SHIELD_*");
		}
		else
		if(O instanceof Coins)
		{
			image=getHashedMXPImage(H,"COINS_"+RawMaterial.CODES.NAME(((Coins)O).material()));
			if(image==null)
				image=getHashedMXPImage(H,"COINS_*");
		}
		else
		if(O instanceof Ammunition)
		{
			image=getHashedMXPImage(H,"AMMO_"+((Ammunition)O).ammunitionType().toUpperCase().replace(' ','_'));
			if(image==null)
				image=getHashedMXPImage(H,"AMMO_*");
		}
		else
		if(O instanceof CagedAnimal)
		{
			final MOB mob=((CagedAnimal)O).unCageMe();
			return getDefaultMXPImage(mob);
		}
		else
		if(O instanceof ClanItem)
		{
			image=getHashedMXPImage(H,"CLAN_"+((ClanItem)O).ID().toUpperCase());
			if(image==null)
				image=getHashedMXPImage(H,"CLAN_"+((ClanItem)O).getClanItemType().toString().toUpperCase());
			if(image==null)
				image=getHashedMXPImage(H,"CLAN_*");
		}
		else
		if(O instanceof DeadBody)
		{
			final Race R=((DeadBody)O).charStats().getMyRace();
			if(R!=null)
			{
				image=getHashedMXPImage(H,"CORPSE_"+R.ID().toUpperCase());
				if(image==null)
					image=getHashedMXPImage(H,"CORPSECAT_"+R.racialCategory().toUpperCase().replace(' ','_'));
			}
			if(image==null)
				image=getHashedMXPImage(H,"CORPSE_*");
			if(image==null)
				image=getHashedMXPImage(H,"CORPSECAT_*");
		}
		else
		if(O instanceof RawMaterial)
		{
			image=getHashedMXPImage(H,"RESOURCE_"+RawMaterial.CODES.NAME(((RawMaterial)O).material()));
			if(image==null)
				image=getHashedMXPImage(H,"RESOURCE_"+RawMaterial.Material.findByMask(((RawMaterial)O).material()&RawMaterial.MATERIAL_MASK).desc());
			if(image==null)
				image=getHashedMXPImage(H,"RESOURCE_*");
		}
		else
		if(O instanceof DoorKey)
		{
			image=getHashedMXPImage(H,"KEY_"+RawMaterial.CODES.NAME(((DoorKey)O).material()));
			image=getHashedMXPImage(H,"KEY_"+RawMaterial.Material.findByMask(((DoorKey)O).material()&RawMaterial.MATERIAL_MASK).desc());
			if(image==null)
				image=getHashedMXPImage(H,"KEY_*");
		}
		else
		if(O instanceof LandTitle)
			image=getHashedMXPImage(H,"ITEM_LANDTITLE");
		else
		if(O instanceof SpaceShip)
			image=getHashedMXPImage(H,"ITEM_SPACESHIP");
		else
		if(O instanceof MagicDust)
		{
			final List<Ability> V=((MagicDust)O).getSpells();
			if(V.size()>0)
				image=getHashedMXPImage(H,"DUST_"+V.get(0).ID().toUpperCase());
			if(image==null)
				image=getHashedMXPImage(H,"DUST_*");
		}
		else
		if(O instanceof com.planet_ink.coffee_mud.Items.interfaces.RoomMap)
			image=getHashedMXPImage(H,"ITEM_MAP");
		else
		if(O instanceof MusicalInstrument)
		{
			image=getHashedMXPImage(H,"MUSINSTR_"+((MusicalInstrument)O).getInstrumentTypeName());
			if(image==null)
				image=getHashedMXPImage(H,"MUSINSTR_*");
		}
		else
		if(O instanceof PackagedItems)
			image=getHashedMXPImage(H,"ITEM_PACKAGED");
		else
		if(O instanceof Perfume)
			image=getHashedMXPImage(H,"ITEM_PERFUME");
		else
		if(O instanceof Pill)
		{
			final List<Ability> V=((Pill)O).getSpells();
			if(V.size()>0)
				image=getHashedMXPImage(H,"PILL_"+V.get(0).ID().toUpperCase());
			if(image==null)
				image=getHashedMXPImage(H,"PILL_*");
		}
		else
		if(O instanceof Potion)
		{
			final List<Ability> V=((Potion)O).getSpells();
			if(V.size()>0)
				image=getHashedMXPImage(H,"POTION_"+V.get(0).ID().toUpperCase());
			if(image==null)
				image=getHashedMXPImage(H,"POTION_*");
		}
		else
		if(O instanceof Recipe)
			image=getHashedMXPImage(H,"ITEM_RECIPE");
		else
		if(O instanceof Scroll)
		{
			final List<Ability> V=((Scroll)O).getSpells();
			if(V.size()>0)
				image=getHashedMXPImage(H,"SCROLL_"+V.get(0).ID().toUpperCase());
			if(image==null)
				image=getHashedMXPImage(H,"SCROLL_*");
		}
		else
		if(O instanceof PowerGenerator)
		{
			final String key = "POWERGENERATOR_"+((Electronics)O).ID().toUpperCase();
			if(H.containsKey(key))
				image=getHashedMXPImage(H,"POWERGENERATOR_"+key);
			else
				image=getHashedMXPImage(H,"POWERGENERATOR");
		}
		else
		if(O instanceof PowerSource)
		{
			final String key = "POWERSOURCE_"+((Electronics)O).ID().toUpperCase();
			if(H.containsKey(key))
				image=getHashedMXPImage(H,key);
			else
				image=getHashedMXPImage(H,"POWERSOURCE");
		}
		else
		if(O instanceof ElecPanel)
		{
			final String key = "ELECPANEL_"+((Electronics)O).ID().toUpperCase();
			if(H.containsKey(key))
				image=getHashedMXPImage(H,key);
			else
			if(((ElecPanel) O).panelType()==null)
				image=getHashedMXPImage(H,"ELECPANEL");
			else
			if(H.containsKey(((ElecPanel) O).panelType().toString()))
				image=getHashedMXPImage(H,((ElecPanel) O).panelType().toString());
			else
			if(H.containsKey(((ElecPanel) O).getTechType().toString()))
				image=getHashedMXPImage(H,((ElecPanel) O).getTechType().toString());
			else
				image=getHashedMXPImage(H,"ELECPANEL");
		}
		else
		if(O instanceof TechComponent)
		{
			final String key = "SHIPCOMP_"+((TechComponent)O).ID().toUpperCase();
			if(H.containsKey(key))
				image=getHashedMXPImage(H,key);
			else
				image=getHashedMXPImage(H,((TechComponent) O).getTechType().toString());
			if(image==null)
				image=getHashedMXPImage(H,"SHIPCOMP_*");
		}
		else
		if(O instanceof Software)
			image=getHashedMXPImage(H,"ITEM_SOFTWARE");
		else
		if(O instanceof Technical)
		{
			String key;
			key = "TECHNICAL_"+((Technical)O).getTechType().toString();
			if(!H.containsKey(key))
				key = "ELECTRONICS_"+((Technical)O).getTechType().toString();
			if(!H.containsKey(key))
				key = "ELECTRONICS_*";
			image=getHashedMXPImage(H,key);
			if(image==null)
				image=getHashedMXPImage(H,"TECH_*");
		}
		else
		if(O instanceof Armor)
		{
			final Armor A=(Armor)O;
			final long[] bits=
			{Wearable.WORN_TORSO, Wearable.WORN_FEET, Wearable.WORN_LEGS, Wearable.WORN_HANDS, Wearable.WORN_ARMS,
			 Wearable.WORN_HEAD, Wearable.WORN_EARS, Wearable.WORN_EYES, Wearable.WORN_MOUTH, Wearable.WORN_NECK,
			 Wearable.WORN_LEFT_FINGER, Wearable.WORN_LEFT_WRIST, Wearable.WORN_BACK, Wearable.WORN_WAIST,
			 Wearable.WORN_ABOUT_BODY, Wearable.WORN_FLOATING_NEARBY, Wearable.WORN_HELD, Wearable.WORN_WIELD};
			final String[] bitdesc=
			{"TORSO","FEET","LEGS","HANDS","ARMS","HEAD","EARS","EYES","MOUTH",
			 "NECK","FINGERS","WRIST","BACK","WAIST","BODY","FLOATER","HELD","WIELDED"};
			for(int i=0;i<bits.length;i++)
			{
				if(CMath.bset(A.rawProperLocationBitmap(),bits[i]))
				{
					image=getHashedMXPImage(H,"ARMOR_"+bitdesc[i]);
					break;
				}
			}
			if(image==null)
				image=getHashedMXPImage(H,"ARMOR_*");
		}
		else
		if(O instanceof Weapon)
		{
			image=getHashedMXPImage(H,"WEAPON_"+Weapon.CLASS_DESCS[((Weapon)O).weaponClassification()]);
			if(image==null)
				image=getHashedMXPImage(H,"WEAPON_"+Weapon.TYPE_DESCS[((Weapon)O).weaponDamageType()]);
			if(O instanceof AmmunitionWeapon)
			{
				if(image==null)
					image=getHashedMXPImage(H,"WEAPON_"+((AmmunitionWeapon)O).ammunitionType().toUpperCase().replace(' ','_'));
			}
			if(image==null)
				image=getHashedMXPImage(H,"WEAPON_*");
		}
		else
		if(O instanceof Wand)
		{
			image=getHashedMXPImage(H,"WAND_"+((Wand)O).ID().toUpperCase());
			if(image==null)
			{
				final Ability A=((Wand)O).getSpell();
				if(A!=null)
					image=getHashedMXPImage(H,"WAND_"+A.ID().toUpperCase());
			}
			if(image==null)
				image=getHashedMXPImage(H,"WAND_*");
		}
		else
		if(O instanceof Food)
		{
			image=getHashedMXPImage(H,"FOOD_"+((Food)O).ID().toUpperCase());
			if(image==null)
				image=getHashedMXPImage(H,"FOOD_"+RawMaterial.CODES.NAME(((Food)O).material()));
			if(image==null)
				image=getHashedMXPImage(H,"FOOD_"+RawMaterial.Material.findByMask(((Food)O).material()&RawMaterial.MATERIAL_MASK).desc());
			if(image==null)
				image=getHashedMXPImage(H,"FOOD_*");
		}
		else
		if(O instanceof Drink)
		{
			image=getHashedMXPImage(H,"DRINK_"+((Drink)O).ID().toUpperCase());
			if(image==null)
				image=getHashedMXPImage(H,"DRINK_"+RawMaterial.CODES.NAME(((Item)O).material()));
			if(image==null)
				image=getHashedMXPImage(H,"DRINK_"+RawMaterial.Material.findByMask(((Item)O).material()&RawMaterial.MATERIAL_MASK).desc());
			if(image==null)
				image=getHashedMXPImage(H,"DRINK_*");
		}
		else
		if(O instanceof Light)
		{
			image=getHashedMXPImage(H,"LIGHT_"+((Light)O).ID().toUpperCase());
			image=getHashedMXPImage(H,"LIGHT_"+RawMaterial.Material.findByMask(((Light)O).material()&RawMaterial.MATERIAL_MASK).desc());
			if(image==null)
				image=getHashedMXPImage(H,"LIGHT_*");
		}
		else
		if(O instanceof Container)
		{
			image=getHashedMXPImage(H,"CONTAINER_"+((Container)O).ID().toUpperCase());
			final String lid=((Container)O).hasADoor()?"LID_":"";
			if(image==null)
				image=getHashedMXPImage(H,"CONTAINER_"+lid+RawMaterial.Material.findByMask(((Container)O).material()&RawMaterial.MATERIAL_MASK).desc());
			if(image==null)
				image=getHashedMXPImage(H,"CONTAINER_"+lid+"*");
		}
		else
		if(O instanceof Electronics)
			image=getHashedMXPImage(H,"ITEM_ELECTRONICS");
		else
		if(O instanceof MiscMagic)
			image=getHashedMXPImage(H,"ITEM_MISCMAGIC");
		if((image==null)&&(O instanceof Item))
		{
			image=getHashedMXPImage(H,"ITEM_"+((Item)O).ID().toUpperCase());
			if(image==null)
				image=getHashedMXPImage(H,"ITEM_"+RawMaterial.CODES.NAME(((Item)O).material()));
			if(image==null)
				image=getHashedMXPImage(H,"ITEM_"+RawMaterial.Material.findByMask(((Item)O).material()&RawMaterial.MATERIAL_MASK).desc());
			if(image==null)
				image=getHashedMXPImage(H,"ITEM_*");
		}
		if(image==null)
			image=getHashedMXPImage(H,"*");
		if(image==null)
			return "";
		return image;
	}

	@SuppressWarnings("rawtypes")
	protected Object msdpStringify(Object o)
	{
		if(o instanceof StringBuilder)
			return ((StringBuilder)o).toString();
		else
		if(o instanceof Map)
		{
			final Map<String,Object> newO=new HashMap<String,Object>();
			for(final Object key : ((Map)o).keySet())
			{
				if(key instanceof StringBuilder)
					newO.put(((StringBuilder)key).toString().toUpperCase(), msdpStringify(((Map)o).get(key)));
			}
			return newO;
		}
		else
		if(o instanceof List)
		{
			final List<Object> newO=new LinkedList<Object>();
			for(final Object subO : (List)o)
				newO.add(msdpStringify(subO));
			return newO;
		}
		else
			return o;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected Map<String,Object> buildMsdpMap(char[] data, int dataSize)
	{
		final Stack<Object> stack=new Stack<Object>();
		stack.push(new HashMap<StringBuilder,Object>());
		StringBuilder str=null;
		StringBuilder var=null;
		StringBuilder valVar=null;
		int x=-1;
		while(++x<dataSize)
		{
			switch(data[x])
			{
			case Session.MSDP_VAR: // start a string
				str=new StringBuilder("");
				var=str;
				if(stack.peek() instanceof Map)
					((Map)stack.peek()).put(str, "");
				else if(stack.peek() instanceof List)
					((List)stack.peek()).add(str);
				break;
			case Session.MSDP_VAL:
			{
				valVar=var;
				var=null;
				str=new StringBuilder("");
				break;
			}
			case Session.MSDP_TABLE_OPEN: // open a table
			{
				final Map<StringBuilder,Object> M=new HashMap<StringBuilder,Object>();
				if((stack.peek() instanceof Map)&&(valVar!=null))
					((Map)stack.peek()).put(valVar, M);
				else if(stack.peek() instanceof List)
					((List)stack.peek()).add(M);
				valVar=null;
				stack.push(M);
				break;
			}
			case Session.MSDP_TABLE_CLOSE: // done with table
				if((stack.size()>1)&&(stack.peek() instanceof Map))
					stack.pop();
				break;
			case Session.MSDP_ARRAY_OPEN: // open an array
			{
				final List<Object> M=new LinkedList<Object>();
				if((stack.peek() instanceof Map)&&(valVar!=null))
					((Map)stack.peek()).put(valVar, M);
				else if(stack.peek() instanceof List)
					((List)stack.peek()).add(M);
				valVar=null;
				stack.push(M);
				break;
			}
			case Session.MSDP_ARRAY_CLOSE: // close an array
				if((stack.size()>1)&&(stack.peek() instanceof List))
					stack.pop();
				break;
			default:
				if((stack.peek() instanceof Map)&&(valVar!=null))
					((Map)stack.peek()).put(valVar, str);
				else if((stack.peek() instanceof List)&&(!((List)stack.peek()).contains(str)))
					((List)stack.peek()).add(str);
				valVar=null;
				if(str!=null)
					str.append(data[x]);
				break;
			}
		}
		return (Map<String,Object>)msdpStringify(stack.firstElement());
	}

	protected enum MSDPListable {
		COMMANDS,LISTS,CONFIGURABLE_VARIABLES,REPORTABLE_VARIABLES,REPORTED_VARIABLES,SENDABLE_VARIABLES
	}

	protected enum MSDPCommand {
		LIST,SEND,REPORT,RESET,UNREPORT
	}

	protected enum MSDPVariable {
		ACCOUNT_NAME,CHARACTER_NAME,SERVER_ID,SERVER_TIME,SPECIFICATION,
		AFFECTS,ALIGNMENT,EXPERIENCE,EXPERIENCE_MAX,EXPERIENCE_TNL,EXPERIENCE_TNL_MAX,
		HEALTH,HEALTH_MAX,LEVEL,MANA,MANA_MAX,MONEY,MOVEMENT,MOVEMENT_MAX,
		OPPONENT_LEVEL,OPPONENT_HEALTH,OPPONENT_HEALTH_MAX,OPPONENT_NAME,OPPONENT_STRENGTH,
		WORLD_TIME,ROOM,LOCATION,ROOM_NAME,ROOM_VNUM,ROOM_AREA,ROOM_TERRAIN,ROOM_EXITS
	}

	protected enum MSDPConfigurableVar {
	}

	protected Object getMsdpComparable(final Session session, final MSDPVariable var)
	{
		final MOB M=session.mob();
		switch(var)
		{
		case ACCOUNT_NAME:
			if(M!=null)
				return M;
			break;
		case AFFECTS:
			if(M!=null)
				return Integer.valueOf(M.numAllEffects());
			break;
		case ALIGNMENT:
			if(M!=null)
			{
				final Faction.FRange FR=CMLib.factions().getRange(CMLib.factions().AlignID(),M.fetchFaction(CMLib.factions().AlignID()));
				if(FR!=null)
					return FR.name();
			}
			break;
		case CHARACTER_NAME:
			if(M!=null)
				return M.Name();
			break;
		case EXPERIENCE:
			if(M!=null)
				return Integer.valueOf(M.getExperience());
			break;
		case EXPERIENCE_MAX:
			if(M!=null)
				return Integer.valueOf(M.getExpNextLevel());
			break;
		case EXPERIENCE_TNL:
			if(M!=null)
				return Integer.valueOf(M.getExpNeededLevel());
			break;
		case EXPERIENCE_TNL_MAX:
			if(M!=null)
				return Integer.valueOf(M.getExpNeededLevel());
			break;
		case HEALTH:
			if(M!=null)
				return Integer.valueOf(M.curState().getHitPoints());
			break;
		case HEALTH_MAX:
			if(M!=null)
				return Integer.valueOf(M.maxState().getHitPoints());
			break;
		case LEVEL:
			if(M!=null)
				return Integer.valueOf(M.phyStats().level());
			break;
		case MANA:
			if(M!=null)
				return Integer.valueOf(M.curState().getMana());
			break;
		case MANA_MAX:
			if(M!=null)
				return Integer.valueOf(M.maxState().getMana());
			break;
		case MONEY:
			if(M!=null)
				return Double.valueOf(CMLib.beanCounter().getTotalAbsoluteNativeValue(M));
			break;
		case MOVEMENT:
			if(M!=null)
				return Integer.valueOf(M.curState().getMovement());
			break;
		case MOVEMENT_MAX:
			if(M!=null)
				return Integer.valueOf(M.maxState().getMovement());
			break;
		case OPPONENT_HEALTH:
			if((M!=null)&&(M.getVictim()!=null))
				return Integer.valueOf(M.getVictim().curState().getHitPoints());
			break;
		case OPPONENT_HEALTH_MAX:
			if((M!=null)&&(M.getVictim()!=null))
				return Integer.valueOf(M.getVictim().maxState().getHitPoints());
			break;
		case OPPONENT_LEVEL:
			if((M!=null)&&(M.getVictim()!=null))
				return Integer.valueOf(M.phyStats().level());
			break;
		case OPPONENT_NAME:
			if((M!=null)&&(M.getVictim()!=null))
				return M.name();
			break;
		case OPPONENT_STRENGTH:
			if(M!=null)
				return (M.getVictim()!=null)?M.getVictim():M;
			break;
		case LOCATION:
		case ROOM:
		case ROOM_NAME:
		case ROOM_VNUM:
		case ROOM_AREA:
		case ROOM_TERRAIN:
		case ROOM_EXITS:
			if((M!=null)&&(M.location()!=null))
				return M.location();
			break;
		case SERVER_ID:
		case SERVER_TIME:
		case SPECIFICATION:
			return this;
		case WORLD_TIME:
			return CMLib.time().globalClock().getShortestTimeDescription();
		default:
			break;
		}
		return "";
	}

	protected byte[] processMsdpSend(final Session session, final String var) throws UnsupportedEncodingException, IOException
	{
		final MSDPVariable type=(MSDPVariable)CMath.s_valueOf(MSDPVariable.class, var.toUpperCase().trim());
		ByteArrayOutputStream buf=new ByteArrayOutputStream();
		if(type == null)
		{
			buf.write(Session.MSDP_VAR);
			buf.write(var.toUpperCase().trim().getBytes(Session.MSDP_CHARSET));
			buf.write(Session.MSDP_VAL);
			return buf.toByteArray();
		}
		buf.write(Session.MSDP_VAR);
		buf.write(type.toString().getBytes(Session.MSDP_CHARSET));
		buf.write(Session.MSDP_VAL);
		final MOB M=session.mob();
		switch(type)
		{
		case ACCOUNT_NAME:
			if((M!=null)&&(M.playerStats()!=null))
			{
				if(M.playerStats().getAccount()!=null)
					buf.write(M.playerStats().getAccount().getAccountName().getBytes(Session.MSDP_CHARSET));
				else
					buf.write(M.Name().getBytes(Session.MSDP_CHARSET));
			}
			break;
		case AFFECTS:
			if(M!=null)
			{
				final List<String> affects=new Vector<String>();
				for(int a=0;a<M.numAllEffects();a++)
				{
					final Ability A=M.fetchEffect(a);
					if(A!=null)
						affects.add(A.name());
				}
				buf=new ByteArrayOutputStream();
				buf.write(Session.MSDP_VAR);buf.write(type.toString().getBytes(Session.MSDP_CHARSET));
				buf.write(msdpListToMsdpArray(affects.toArray(new String[0])));
			}
			break;
		case ALIGNMENT:
			if(M!=null)
			{
				final Faction.FRange FR=CMLib.factions().getRange(CMLib.factions().AlignID(),M.fetchFaction(CMLib.factions().AlignID()));
				if(FR!=null)
					buf.write(FR.name().toLowerCase().getBytes(Session.MSDP_CHARSET));
			}
			break;
		case CHARACTER_NAME:
			if(M!=null)
				buf.write(M.name().getBytes(Session.MSDP_CHARSET));
			break;
		case EXPERIENCE:
			if(M!=null)
				buf.write(Integer.toString(M.getExperience()).getBytes(Session.MSDP_CHARSET));
			break;
		case EXPERIENCE_MAX:
			if(M!=null)
				buf.write(Integer.toString(M.getExpNextLevel()).getBytes(Session.MSDP_CHARSET));
			break;
		case EXPERIENCE_TNL:
			if(M!=null)
				buf.write(Integer.toString(M.getExpNeededLevel()).getBytes(Session.MSDP_CHARSET));
			break;
		case EXPERIENCE_TNL_MAX:
			if(M!=null)
				buf.write(Integer.toString(M.getExpNeededLevel()).getBytes(Session.MSDP_CHARSET));
			break;
		case HEALTH:
			if(M!=null)
				buf.write(Integer.toString(M.curState().getHitPoints()).getBytes(Session.MSDP_CHARSET));
			break;
		case HEALTH_MAX:
			if(M!=null)
				buf.write(Integer.toString(M.maxState().getHitPoints()).getBytes(Session.MSDP_CHARSET));
			break;
		case LEVEL:
			if(M!=null)
				buf.write(Integer.toString(M.phyStats().level()).getBytes(Session.MSDP_CHARSET));
			break;
		case MANA:
			if(M!=null)
				buf.write(Integer.toString(M.curState().getMana()).getBytes(Session.MSDP_CHARSET));
			break;
		case MANA_MAX:
			if(M!=null)
				buf.write(Integer.toString(M.maxState().getMana()).getBytes(Session.MSDP_CHARSET));
			break;
		case MONEY:
			if(M!=null)
				buf.write(Double.toString(CMLib.beanCounter().getTotalAbsoluteNativeValue(M)).getBytes(Session.MSDP_CHARSET));
			break;
		case MOVEMENT:
			if(M!=null)
				buf.write(Integer.toString(M.curState().getMovement()).getBytes(Session.MSDP_CHARSET));
			break;
		case MOVEMENT_MAX:
			if(M!=null)
				buf.write(Integer.toString(M.maxState().getMovement()).getBytes(Session.MSDP_CHARSET));
			break;
		case OPPONENT_HEALTH:
			if((M!=null)&&(M.getVictim()!=null))
				buf.write(Integer.toString(M.getVictim().curState().getHitPoints()).getBytes(Session.MSDP_CHARSET));
			break;
		case OPPONENT_HEALTH_MAX:
			if((M!=null)&&(M.getVictim()!=null))
				buf.write(Integer.toString(M.getVictim().maxState().getHitPoints()).getBytes(Session.MSDP_CHARSET));
			break;
		case OPPONENT_LEVEL:
			if((M!=null)&&(M.getVictim()!=null))
				buf.write(Integer.toString(M.phyStats().level()).getBytes(Session.MSDP_CHARSET));
			break;
		case OPPONENT_NAME:
			if((M!=null)&&(M.getVictim()!=null))
				buf.write(M.name().getBytes(Session.MSDP_CHARSET));
			break;
		case OPPONENT_STRENGTH:
			if((M!=null)&&(M.getVictim()!=null))
			{
				Command C=CMClass.getCommand("CONSIDER");
				if(C==null)
					C=CMClass.getCommand("Consider");
				try
				{
					buf.write(C.executeInternal(M, 0, M.getVictim()).toString().getBytes(Session.MSDP_CHARSET));
				}
				catch (final IOException e)
				{
					buf.write(Integer.toString(M.getVictim().phyStats().level()).getBytes(Session.MSDP_CHARSET));
				}
			}
			break;
		case LOCATION:
		case ROOM:
			if((M!=null)&&(M.location()!=null))
			{
				final Room R=M.location();
				final String domType;
				if((R.domainType()&Room.INDOORS)==0)
					domType=Room.DOMAIN_OUTDOOR_DESCS[R.domainType()];
				else
					domType=Room.DOMAIN_INDOORS_DESCS[CMath.unsetb(R.domainType(),Room.INDOORS)];
				buf=new ByteArrayOutputStream();
				buf.write(Session.MSDP_VAR);buf.write(type.toString().getBytes(Session.MSDP_CHARSET));
				buf.write(Session.MSDP_VAL);
				buf.write(Session.MSDP_TABLE_OPEN);
				buf.write(Session.MSDP_VAR);buf.write("VNUM".getBytes(Session.MSDP_CHARSET));
				buf.write(Session.MSDP_VAL);
				buf.write(Integer.toString(CMLib.map().getExtendedRoomID(R).hashCode()).getBytes(Session.MSDP_CHARSET));
				buf.write(Session.MSDP_VAR);buf.write("NAME".getBytes(Session.MSDP_CHARSET));
				buf.write(Session.MSDP_VAL);
				buf.write(R.displayText(M).getBytes(Session.MSDP_CHARSET));
				buf.write(Session.MSDP_VAR);buf.write("AREA".getBytes(Session.MSDP_CHARSET));
				buf.write(Session.MSDP_VAL);
				buf.write(R.getArea().Name().getBytes(Session.MSDP_CHARSET));
				buf.write(Session.MSDP_VAR);buf.write("TERRAIN".getBytes(Session.MSDP_CHARSET));
				buf.write(Session.MSDP_VAL);
				buf.write(domType.getBytes(Session.MSDP_CHARSET));
				buf.write(Session.MSDP_VAR);buf.write("EXITS".getBytes(Session.MSDP_CHARSET));
				buf.write(Session.MSDP_VAL);
				buf.write(Session.MSDP_TABLE_OPEN);
				for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
				{
					final Room R2=R.getRoomInDir(d);
					if((R2!=null)&&(R.getExitInDir(d)!=null))
					{
						final String roomID=CMLib.map().getExtendedRoomID(R2);
						if(roomID.length()>0)
						{
							buf.write(Session.MSDP_VAR);buf.write(CMLib.directions().getDirectionChar(d).getBytes(Session.MSDP_CHARSET));
							buf.write(Session.MSDP_VAL);
							buf.write(Integer.toString(roomID.hashCode()).getBytes(Session.MSDP_CHARSET));
						}
					}
				}
				buf.write(Session.MSDP_TABLE_CLOSE);
				buf.write(Session.MSDP_TABLE_CLOSE);
			}
			break;
		case ROOM_NAME:
			if((M!=null)&&(M.location()!=null))
				buf.write(M.location().displayText().getBytes(Session.MSDP_CHARSET));
			break;
		case ROOM_VNUM:
			if((M!=null)&&(M.location()!=null))
				buf.write(Integer.toString(CMLib.map().getExtendedRoomID(M.location()).hashCode()).getBytes(Session.MSDP_CHARSET));
			break;
		case ROOM_AREA:
			if((M!=null)&&(M.location()!=null))
				buf.write(M.location().getArea().Name().getBytes(Session.MSDP_CHARSET));
			break;
		case ROOM_TERRAIN:
			if((M!=null)&&(M.location()!=null))
			{
				final Room R=M.location();
				final String domType;
				if((R.domainType()&Room.INDOORS)==0)
					domType=Room.DOMAIN_OUTDOOR_DESCS[R.domainType()];
				else
					domType=Room.DOMAIN_INDOORS_DESCS[CMath.unsetb(R.domainType(),Room.INDOORS)];
				buf.write(domType.getBytes(Session.MSDP_CHARSET));
			}
			break;
		case ROOM_EXITS:
			if((M!=null)&&(M.location()!=null))
			{
				final Room R=M.location();
				buf=new ByteArrayOutputStream();
				buf.write(Session.MSDP_VAR);buf.write("EXITS".getBytes(Session.MSDP_CHARSET));
				buf.write(Session.MSDP_VAL);
				buf.write(Session.MSDP_TABLE_OPEN);
				for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
				{
					final Room R2=R.getRoomInDir(d);
					if((R2!=null)&&(R.getExitInDir(d)!=null))
					{
						final String roomID=CMLib.map().getExtendedRoomID(R2);
						if(roomID.length()>0)
						{
							buf.write(Session.MSDP_VAR);buf.write(CMLib.directions().getDirectionChar(d).getBytes(Session.MSDP_CHARSET));
							buf.write(Session.MSDP_VAL);
							buf.write(Integer.toString(roomID.hashCode()).getBytes(Session.MSDP_CHARSET));
						}
					}
				}
				buf.write(Session.MSDP_TABLE_CLOSE);
			}
			break;
		case SERVER_ID:
			buf.write(CMProps.getVar(CMProps.Str.MUDNAME).getBytes(Session.MSDP_CHARSET));
			break;
		case SERVER_TIME:
			buf.write(CMLib.time().date2APTimeString(System.currentTimeMillis()).getBytes(Session.MSDP_CHARSET));
			break;
		case SPECIFICATION:
			buf.write("http://tintin.sourceforge.net/msdp/".getBytes(Session.MSDP_CHARSET));
			break;
		case WORLD_TIME:
			buf.write(CMLib.time().globalClock().getShortestTimeDescription().getBytes(Session.MSDP_CHARSET));
			break;
		default:
			break;
		}
		return buf.toByteArray();
	}

	protected byte[] msdpListToMsdpArray(final Object[] stuff) throws UnsupportedEncodingException, IOException
	{
		final ByteArrayOutputStream buf=new ByteArrayOutputStream();
		buf.write(Session.MSDP_ARRAY_OPEN);
		for(final Object s : stuff)
		{
			buf.write(Session.MSDP_VAL);
			buf.write(s.toString().getBytes(Session.MSDP_CHARSET));
		}
		buf.write(Session.MSDP_ARRAY_CLOSE);
		return buf.toByteArray();
	}

	protected byte[] processMsdpList(final Session session, final String var, final Map<Object,Object> reportables) throws UnsupportedEncodingException, IOException
	{
		final ByteArrayOutputStream buf=new ByteArrayOutputStream();
		final MSDPListable type=(MSDPListable)CMath.s_valueOf(MSDPListable.class, var.toUpperCase().trim());
		if(type == null)
			return buf.toByteArray();
		switch(type)
		{
		case COMMANDS:
			buf.write(msdpListToMsdpArray(MSDPCommand.values()));
			break;
		case LISTS:
			buf.write(msdpListToMsdpArray(MSDPListable.values()));
			break;
		case CONFIGURABLE_VARIABLES:
			buf.write(msdpListToMsdpArray(MSDPConfigurableVar.values()));
			break;
		case REPORTABLE_VARIABLES:
			buf.write(msdpListToMsdpArray(MSDPVariable.values()));
			break;
		case REPORTED_VARIABLES:
		{
			final List<String> set=new Vector<String>(reportables.size());
			for(final Object o : reportables.keySet())
				set.add(o.toString());
			buf.write(msdpListToMsdpArray(set.toArray(new Object[0])));
			break;
		}
		case SENDABLE_VARIABLES:
			buf.write(msdpListToMsdpArray(MSDPVariable.values()));
			break;
		default:
			buf.write((byte) '?');
			break;
		}
		return buf.toByteArray();
	}
	
	protected String getAbilityGroupName(final int code)
	{
		return Ability.ACODE_DESCS[code&Ability.ALL_ACODES].toLowerCase()+
				"-"+Ability.DOMAIN_DESCS[(code&Ability.ALL_DOMAINS)<<5].toLowerCase();
	}

	protected void resetMsdpConfigurable(final Session session, final String var)
	{
		final MSDPConfigurableVar type=(MSDPConfigurableVar)CMath.s_valueOf(MSDPConfigurableVar.class, var.toUpperCase().trim());
		if(type == null)
			return;
		//TODO:
	}

	@Override
	public byte[] pingMsdp(final Session session, final Map<Object,Object> reportables)
	{
		try
		{
			if(reportables.size()==0)
				return null;
			List<Object> broken=null;
			synchronized(reportables)
			{
				Object newValue;
				for(final Entry<Object,Object> e : reportables.entrySet())
				{
					newValue=getMsdpComparable(session, (MSDPVariable)e.getKey());
					if(!e.getValue().equals(newValue))
					{
						reportables.put(e.getKey(),newValue);
						if(broken==null)
							broken=new LinkedList<Object>();
						broken.add(e.getKey());
					}
				}
			}
			if(broken==null)
				return null;
			final ByteArrayOutputStream buf=new ByteArrayOutputStream();
			buf.write(Session.TELNET_IAC);buf.write(Session.TELNET_SB);buf.write(Session.TELNET_MSDP);
			for(final Object var : broken)
			{
				buf.write(processMsdpSend(session,var.toString()));
			}
			buf.write((char)Session.TELNET_IAC);buf.write((char)Session.TELNET_SE);
			return buf.toByteArray();
		}
		catch(final IOException e)
		{
			return null;
		}
	}

	@Override
	@SuppressWarnings("rawtypes")
	public byte[] processMsdp(final Session session, final char[] data, final int dataSize, final Map<Object,Object> reportables)
	{
		try
		{
			final Map<String,Object> cmds=this.buildMsdpMap(data, dataSize);
			final ByteArrayOutputStream buf=new ByteArrayOutputStream();
			if(cmds.containsKey(MSDPCommand.REPORT.toString()))
			{
				synchronized(reportables)
				{
					final Object o=cmds.get(MSDPCommand.REPORT.toString());
					if(o instanceof String)
					{
						final MSDPVariable type=(MSDPVariable)CMath.s_valueOf(MSDPVariable.class, ((String)o).toUpperCase().trim());
						if(type != null)
							reportables.put(type, getMsdpComparable(session, type));
						buf.write(processMsdpSend(session,(String)o));
					}
					else
					if(o instanceof List)
					{
						for(final Object o2 : ((List)o))
						{
							if(o2 instanceof String)
							{
								final MSDPVariable type=(MSDPVariable)CMath.s_valueOf(MSDPVariable.class, ((String)o2).toUpperCase().trim());
								if(type != null)
									reportables.put(type, getMsdpComparable(session, type));
								buf.write(processMsdpSend(session,(String)o2));
							}
						}
					}
				}
			}
			if(cmds.containsKey(MSDPCommand.SEND.toString()))
			{
				final Object o=cmds.get(MSDPCommand.SEND.toString());
				if(o instanceof String)
				{
					buf.write(processMsdpSend(session,(String)o));
				}
				else
				if(o instanceof List)
				{
					for(final Object o2 : ((List)o))
					{
						if(o2 instanceof String)
							buf.write(processMsdpSend(session,(String)o2));
					}
				}
			}
			if(cmds.containsKey(MSDPCommand.LIST.toString()))
			{
				final Object o=cmds.get(MSDPCommand.LIST.toString());
				if(o instanceof String)
				{
					buf.write(Session.MSDP_VAR);buf.write(((String)o).getBytes(Session.MSDP_CHARSET));buf.write(Session.MSDP_VAL);buf.write(processMsdpList(session,(String)o,reportables));
				}
				else
				if(o instanceof List)
				{
					for(final Object o2 : ((List)o))
					{
						if(o2 instanceof String)
							buf.write(Session.MSDP_VAR);buf.write(((String)o2).getBytes(Session.MSDP_CHARSET));buf.write(Session.MSDP_VAL);buf.write(processMsdpList(session,(String)o2,reportables));
					}
				}
			}
			if(cmds.containsKey(MSDPCommand.UNREPORT.toString()))
			{
				final Object o=cmds.get(MSDPCommand.UNREPORT.toString());
				if(o instanceof String)
				{
					final MSDPVariable type=(MSDPVariable)CMath.s_valueOf(MSDPVariable.class, ((String)o).toUpperCase().trim());
					if(type != null)
						reportables.remove(type);
				}
				else
				if(o instanceof List)
				{
					for(final Object o2 : ((List)o))
					{
						if(o2 instanceof String)
						{
							final MSDPVariable type=(MSDPVariable)CMath.s_valueOf(MSDPVariable.class, ((String)o2).toUpperCase().trim());
							if(type != null)
								reportables.remove(type);
						}
					}
				}
			}
			if(cmds.containsKey(MSDPCommand.RESET.toString()))
			{
				final Object o=cmds.get(MSDPCommand.RESET.toString());
				if(o instanceof String)
				{
					resetMsdpConfigurable(session, (String)o);
				}
				else
				if(o instanceof List)
				{
					for(final Object o2 : ((List)o))
					{
						if(o2 instanceof String)
							resetMsdpConfigurable(session, (String)o2);
					}
				}
			}
			if(buf.size()==0)
				return null;
			final ByteArrayOutputStream bout = new ByteArrayOutputStream();
			bout.write(Session.TELNET_IAC); bout.write(Session.TELNET_SB); bout.write(Session.TELNET_MSDP);
			bout.write(buf.toByteArray()); bout.write(Session.TELNET_IAC); bout.write(Session.TELNET_SE);
			return bout.toByteArray();
		}
		catch(final IOException e)
		{
			return null;
		}
	}

	@Override
	public byte[] buildGmcpResponse(final String json)
	{
		final ByteArrayOutputStream bout=new ByteArrayOutputStream();
		try
		{
			bout.write(Session.TELNETBYTES_GMCP_HEAD);
			bout.write(json.getBytes(Session.MSDP_CHARSET));
			bout.write(Session.TELNETBYTES_END_SB);
		}
		catch (final IOException e)
		{
		}
		return bout.toByteArray();
	}

	protected Map<Integer,List<Ability>> getSkillGroups(final MOB mob)
	{
		final Map<Integer,List<Ability>> allMyGroups=new TreeMap<Integer,List<Ability>>();
		for(int a=0;a<mob.numAllAbilities();a++)
		{
			final Ability A=mob.fetchAbility(a);
			final Integer I=Integer.valueOf(A.abilityCode());
			if(!allMyGroups.containsKey(I))
				allMyGroups.put(I, new LinkedList<Ability>());
			allMyGroups.get(I).add(A);
		}
		return allMyGroups;
	}
	
	protected String makeGMCPAttribs(final Item I)
	{
		final StringBuffer attribs=new StringBuffer("");
		if(I.amWearingAt(Item.WORN_WIELD))
			attribs.append("l");
		else
		if(!I.amWearingAt(Item.IN_INVENTORY))
			attribs.append("w");
		else
		if((I.rawProperLocationBitmap() != 0)&&(I.rawProperLocationBitmap() != Item.WORN_HELD))
			attribs.append("W");
		if(I instanceof Container)
			attribs.append("c");
		return attribs.toString();
	}

	protected String processGmcpStr(final Session session, final String jsonData, final Map<String,Double> supportables)
	{
		final MiniJSON jsonParser=new MiniJSON();
		try
		{
			final MOB mob=session.mob();
			final String allDoc=jsonData.trim();
			int pkgSepIndex=allDoc.indexOf(' ');
			String pkg;
			MiniJSON.JSONObject json;
			if(pkgSepIndex>0)
			{
				pkg=allDoc.substring(0,pkgSepIndex);
				final String jsonDoc=allDoc.substring(pkgSepIndex).trim();
				if(jsonDoc.length()==0)
					json=jsonParser.parseObject("{\"root\":{}}");
				else
				if(Character.isLetter(jsonDoc.charAt(0)))
					json=jsonParser.parseObject("{\"root\":\""+jsonDoc+"\"}");
				else
					json=jsonParser.parseObject("{\"root\":"+jsonDoc+"}");
			}
			else
			{
				pkg=allDoc;
				json=null;
			}
			GMCPCommand cmd;
			try
			{
				cmd=GMCPCommand.valueOf(pkg.toLowerCase().replace('.','_'));
				switch(cmd)
				{
				case client:
				case client_version:
				case maplevel:
					// what's this do?
					break;
				case request:
				{
					StringBuilder str=new StringBuilder(allDoc);
					str.setCharAt(pkgSepIndex, '_');
					return processGmcpStr(session,str.toString(),supportables);
				}
				case char_login:
				{
					if(mob==null)
					{
						if(json!=null)
							json=json.getCheckedJSONObject("root");
						if(json!=null)
						{
							final String name=json.getCheckedString("name");
							final String pw=json.getCheckedString("password");
							if(session.autoLogin(name, pw))
							{
								return processGmcpStr(session,"char.statusvars",supportables);
							}
						}
					}
					break;
				}
				case core_hello:
				{
					if(json!=null)
						json=json.getCheckedJSONObject("root");
					if(json != null)
					{
						final String client=json.getCheckedString("client");
						final Object o = json.get("version");
						double ver=0.0;
						if(o!=null)
						{
							String oStr=o.toString().trim();
							if(CMath.isNumber(oStr))
								ver=CMath.s_double(oStr);
						}
						if(client != null)
							supportables.put(client, Double.valueOf(ver));
					}
					break;
				}
				case core_supports_add:
				case core_supports_set:
				{
					Object[] list = null;
					if(json!=null)
						list=json.getCheckedArray("root");
					if(list != null)
					{
						for(final Object o : list)
						{
							String s=o.toString().toLowerCase().trim();
							double ver=1.0;
							final int x=s.indexOf(' ');
							if(x>0)
							{
								if(CMath.isNumber(s.substring(x+1).trim()))
									ver=CMath.s_double(s.substring(x+1).trim());
								s=s.substring(0,x).trim();
							}
							supportables.put(s, Double.valueOf(ver));
						}
					}
					break;
				}
				case core_supports_remove:
				{
					Object[] list = null;
					if(json!=null)
						list=json.getCheckedArray("root");
					if(list != null)
					{
						for(final Object o : list)
						{
							final String s=o.toString().trim();
							supportables.remove(s.toLowerCase());
						}
					}
					break;
				}
				case core_keepalive:
				{
					session.setIdleTimers();
					break;
				}
				case core_ping:
				{
					return pkg;
				}
				case core_goodbye:
				{
					session.stopSession(false,false,false);
					break;
				}
				case ire_composer_setbuffer:
				{
					String buf = null;
					if(json!=null)
						buf=json.getCheckedString("root");
					if(buf != null)
					{
						if(buf.indexOf('\n')>0)
						{
							buf=CMStrings.replaceAll(buf, "\n", "\\n");
							buf=CMStrings.replaceAll(buf, "\r", "");
						}
						else
						if(buf.indexOf('\r')>0)
						{
							buf=CMStrings.replaceAll(buf, "\r", "\\n");
							buf=CMStrings.replaceAll(buf, "\n", "");
						}
						session.setFakeInput(buf);
					}
					break;
				}
				case char_vitals:
				{
					if(mob != null)
					{
						final StringBuilder doc=new StringBuilder("char.vitals {");
						doc.append("\"hp\":").append(mob.curState().getHitPoints()).append(",");
						doc.append("\"mana\":").append(mob.curState().getMana()).append(",");
						doc.append("\"moves\":").append(mob.curState().getMovement());
						doc.append("}");
						return doc.toString();
					}
					break;
				}
				case room_items_inv:
				{
					if(mob != null)
					{
						final Room R=mob.location();
						if(R!=null)
						{
							final StringBuilder doc=new StringBuilder("room.items.list {");
							doc.append("\"location\":\"room\",");
							doc.append("\"items\":[");
							for(int i=0;i<R.numItems();i++)
							{
								final Item I=R.getItem(i);
								if((I!=null)&&(I.container()==null))
								{
									doc.append("{");
									doc.append("\"id\":").append(I.hashCode()).append(",");
									doc.append("\"name\":\"").append(MiniJSON.toJSONString(I.Name())).append("\",");
									final String attribs = makeGMCPAttribs(I);
									if(attribs.length()>0)
										doc.append("\"attrib\":\"").append(attribs.toString()).append("\",");
									doc.append("}");
								}
							}
							doc.append("]");
							doc.append("}");
							return doc.toString();
						}
					}
					break;
				}
				case room_items_contents:
				{
					if(mob != null)
					{
						final Room R=mob.location();
						if((R!=null) && (json != null))
						{
							final long hashCode = json.getCheckedLong("root").hashCode();
							final StringBuilder doc=new StringBuilder("room.items.list {");
							doc.append("\"location\":\""+hashCode+"\",");
							doc.append("\"items\":[");
							for(int i=0;i<R.numItems();i++)
							{
								final Item I=R.getItem(i);
								if((I!=null)
								&&(I.container()!=null)
								&&(I.container().hashCode()==hashCode))
								{
									doc.append("{");
									doc.append("\"id\":").append(I.hashCode()).append(",");
									doc.append("\"name\":\"").append(MiniJSON.toJSONString(I.Name())).append("\",");
									final String attribs = makeGMCPAttribs(I);
									if(attribs.length()>0)
										doc.append("\"attrib\":\"").append(attribs.toString()).append("\",");
									doc.append("}");
								}
							}
							doc.append("]");
							doc.append("}");
							return doc.toString();
						}
					}
					break;
				}
				case room_mobiles:
				{
					if(mob != null)
					{
						final Room R=mob.location();
						if(R!=null)
						{
							final StringBuilder doc=new StringBuilder("room.mobiles {");
							for(int r=0;r<R.numInhabitants();r++)
							{
								final MOB M=R.fetchInhabitant(r);
								if((M!=null)&&(!M.isPlayer()))
								{
									final String lname=(M.Name().equals(M.name())?M.titledName():M.name(mob));
									doc.append("\""+M.Name()+"\":\"").append(MiniJSON.toJSONString(lname)).append("\",");
								}
							}
							doc.append("}");
							return doc.toString();
						}
					}
					break;
				}
				case room_players:
				{
					if(mob != null)
					{
						final Room R=mob.location();
						if(R!=null)
						{
							final StringBuilder doc=new StringBuilder("room.players {");
							for(int r=0;r<R.numInhabitants();r++)
							{
								final MOB M=R.fetchInhabitant(r);
								if((M!=null)&&(M.isPlayer()))
								{
									final String lname=(M.Name().equals(M.name())?M.titledName():M.name(mob));
									doc.append("\""+M.Name()+"\":\"").append(MiniJSON.toJSONString(lname)).append("\",");
								}
							}
							doc.append("}");
							return doc.toString();
						}
					}
					break;
				}
				case char_items_inv:
				{
					if(mob != null)
					{
						final StringBuilder doc=new StringBuilder("char.items.list {");
						doc.append("\"location\":\"inv\",");
						doc.append("\"items\":[");
						for(int i=0;i<mob.numItems();i++)
						{
							final Item I=mob.getItem(i);
							if((I!=null)&&(I.container()==null))
							{
								doc.append("{");
								doc.append("\"id\":").append(I.hashCode()).append(",");
								doc.append("\"name\":\"").append(MiniJSON.toJSONString(I.Name())).append("\",");
								final String attribs = makeGMCPAttribs(I);
								if(attribs.length()>0)
									doc.append("\"attrib\":\"").append(attribs.toString()).append("\",");
								doc.append("}");
							}
						}
						doc.append("]");
						doc.append("}");
						return doc.toString();
					}
					break;
				}
				case char_items_contents:
				{
					if(mob != null)
					{
						if(json != null)
						{
							final long hashCode = json.getCheckedLong("root").hashCode();
							final StringBuilder doc=new StringBuilder("char.items.list {");
							doc.append("\"location\":\""+hashCode+"\",");
							doc.append("\"items\":[");
							for(int i=0;i<mob.numItems();i++)
							{
								final Item I=mob.getItem(i);
								if((I!=null)
								&&(I.container()!=null)
								&&(I.container().hashCode()==hashCode))
								{
									doc.append("{");
									doc.append("\"id\":").append(I.hashCode()).append(",");
									doc.append("\"name\":\"").append(MiniJSON.toJSONString(I.Name())).append("\",");
									final String attribs = makeGMCPAttribs(I);
									if(attribs.length()>0)
										doc.append("\"attrib\":\"").append(attribs.toString()).append("\",");
									doc.append("}");
								}
							}
							doc.append("]");
							doc.append("}");
							return doc.toString();
						}
					}
					break;
				}
				case char_statusvars:
					if(mob != null)
					{
						final StringBuilder doc=new StringBuilder("char.statusvars {");
						doc.append("\"level\":").append(mob.phyStats().level()).append(",");
						doc.append("\"race\":\"").append(MiniJSON.toJSONString(mob.charStats().raceName())).append("\"");
						final List<String> clans=new LinkedList<String>();
						for(final Pair<Clan,Integer> p : mob.clans())
							clans.add(MiniJSON.toJSONString(p.first.name()));
						if(clans.size()==1)
							doc.append(",\"guild\":\"").append(clans.get(0)).append("\"");
						else
						if(clans.size()>1)
						{
							doc.append(",\"guild\":[");
							for(int i=0;i<clans.size();i++)
							{
								if(i>0)
									doc.append(",");
								doc.append("\"").append(clans.get(i)).append("\"");
							}
							doc.append("]");
						}
						doc.append("}");
						return doc.toString();
					}
					break;
				case request_char:
				case char_base:
					if(mob != null)
					{
						final StringBuilder doc=new StringBuilder("char.base {");
						doc.append("\"name\":\"").append(mob.name()).append("\"").append(",");
						doc.append("\"class\":\"").append(mob.charStats().getCurrentClass().baseClass()).append("\"").append(",");
						doc.append("\"subclass\":\"").append(MiniJSON.toJSONString(mob.charStats().displayClassName())).append("\"").append(",");
						doc.append("\"race\":\"").append(MiniJSON.toJSONString(mob.charStats().raceName())).append("\"").append(",");
						doc.append("\"perlevel\":").append(mob.getExpNextLevel());
						final String title = (mob.playerStats()!=null)?mob.playerStats().getActiveTitle():null;
						if(title!=null)
							doc.append(",\"pretitle\":\"").append(MiniJSON.toJSONString(title)).append("\"");
						final List<String> clans=new LinkedList<String>();
						for(final Pair<Clan,Integer> p : mob.clans())
							clans.add(MiniJSON.toJSONString(p.first.name()));
						if(clans.size()==1)
							doc.append(",\"clan\":\"").append(clans.get(0)).append("\"");
						else
						if(clans.size()>1)
						{
							doc.append(",\"clan\":[");
							for(int i=0;i<clans.size();i++)
							{
								if(i>0)
									doc.append(",");
								doc.append("\"").append(clans.get(i)).append("\"");
							}
							doc.append("]");
						}
						doc.append("}");
						return doc.toString();
					}
					break;
				case char_maxstats:
					if(mob != null)
					{
						final StringBuilder doc=new StringBuilder("char.maxstats {");
						doc.append("\"maxhp\":").append(mob.maxState().getHitPoints()).append(",");
						doc.append("\"maxmana\":").append(mob.maxState().getMana()).append(",");
						doc.append("\"maxmoves\":").append(mob.maxState().getMovement());
						doc.append("}");
						return doc.toString();
					}
					break;
				case char_status:
					if(mob != null)
					{
						final StringBuilder doc=new StringBuilder("char.status {");
						doc.append("\"level\":").append(mob.phyStats().level()).append(",");
						doc.append("\"tnl\":").append(mob.getExpNeededLevel()).append(",");
						doc.append("\"hunger\":").append(mob.curState().getHunger()).append(",");
						doc.append("\"thirst\":").append(mob.curState().getThirst()).append(",");
						doc.append("\"fatigue\":").append(mob.curState().getFatigue()).append(",");
						if(mob.playerStats()!=null)
							doc.append("\"stink_pct\":").append(CMath.round(100.0 * mob.playerStats().getHygiene()/PlayerStats.HYGIENE_DELIMIT)).append(",");
						final int align=mob.fetchFaction(CMLib.factions().AlignID());
						if(align!=Integer.MAX_VALUE)
							doc.append("\"align\":").append(align).append(",");
						int state=3;
						if(session.isAfk())
							state=4;
						else
						if(CMLib.flags().isSleeping(mob))
							state=9;
						else
						if(CMLib.flags().isSitting(mob))
							state=11;
						else
						if(mob.getVictim()!=null)
							state=8;
						else
						if(mob.isAttributeSet(MOB.Attrib.SYSOPMSGS))
							state=6;
						else
						if(mob.isAttributeSet(MOB.Attrib.AUTORUN))
							state=12;
						doc.append("\"state\":").append(state).append(",");
						doc.append("\"pos\":\"").append(
									CMLib.flags().isSleeping(mob)?"Sleeping":
									CMLib.flags().isSitting(mob)?"Sitting":
									"Standing"
									).append("\"");
						final MOB vicM=mob.getVictim();
						if(vicM!=null)
						{
							doc.append(",");
							doc.append("\"enemy\":\"").append(vicM.name()).append("\",");
							doc.append("\"enemypct\":").append(Math.round((double)vicM.curState().getHitPoints()/(double)vicM.maxState().getHitPoints()*100.0));
						}
						doc.append("}");
						return doc.toString();
					}
					else
					{
						final StringBuilder doc=new StringBuilder("char.status {");
						doc.append("\"state\":").append(1);
						doc.append("}");
						return doc.toString();
					}
				case char_worth:
					if(mob != null)
					{
						final StringBuilder doc=new StringBuilder("char.worth {");
						doc.append("\"gold\":").append(CMLib.beanCounter().getTotalAbsoluteNativeValue(mob)).append(",");
						doc.append("\"qp\":").append(mob.getQuestPoint()).append(",");
						//doc.append("\"tp\":").append(mob.getTrains()).append(",");
						doc.append("\"trains\":").append(mob.getTrains()).append(",");
						doc.append("\"pracs\":").append(mob.getPractices());
						doc.append("}");
						return doc.toString();
					}
					break;
				case request_area:
				case request_sectors:
				case request_room:
				case room_info:
					if(mob!=null)
					{
						final Room room=mob.location();
						if(room != null)
						{
							final StringBuilder doc=new StringBuilder("room.info {");
							final String roomID=CMLib.map().getExtendedRoomID(room);
							final String domType;
							if((room.domainType()&Room.INDOORS)==0)
								domType=Room.DOMAIN_OUTDOOR_DESCS[room.domainType()];
							else
								domType=Room.DOMAIN_INDOORS_DESCS[CMath.unsetb(room.domainType(),Room.INDOORS)];
							doc.append("\"num\":").append(roomID.hashCode()).append(",")
								.append("\"id\":\"").append(roomID).append("\",")
								.append("\"name\":\"").append(MiniJSON.toJSONString(room.displayText(mob))).append("\",")
								.append("\"zone\":\"").append(MiniJSON.toJSONString(room.getArea().name())).append("\",")
								.append("\"desc\":\"").append(MiniJSON.toJSONString(room.description(mob))).append("\",")
								.append("\"terrain\":\"").append(domType.toLowerCase()).append("\",")
								.append("\"details\":\"").append("\",")
								.append("\"exits\":{");
							boolean comma=false;
							for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
							{
								final Room R2=room.getRoomInDir(d);
								if((R2!=null)&&(room.getExitInDir(d)!=null))
								{
									final String room2ID=CMLib.map().getExtendedRoomID(R2);
									if(room2ID.length()>0)
									{
										if(comma)
											doc.append(","); comma=true;
										doc.append("\""+CMLib.directions().getDirectionChar(d)+"\":").append(room2ID.hashCode());
									}
								}
							}
							doc.append("},\"coord\":{\"id\":0,\"x\":-1,\"y\":-1,\"cont\":0}");
							doc.append("}");
							return doc.toString();
						}
					}
					break;
				case request_quest:
					// comm.quest responds whenever quest stuff happens..
					// comm.quest {"action": "start", "targ": "a swamp ape", "room": "Swamp Ape Enclosure", "area": "Aardwolf Zoological Park", "timer": 52 }
					// for request quest, a simple response:
					// comm.quest {"action": "status", "targ": "A spirit of strong essence", "room": "The Path of the Dead", "area": "The Deadlights", "timer": 60 }
					return "comm.quest {\"action\": \"status\", \"status\": \"ready\" }";
				case rawcolor:
					// ardwolf stuff -- no idea
					break;
				case request_group:
				case group:
					if(mob!=null)
					{
						final StringBuilder doc=new StringBuilder("group {");
						final Set<MOB> group=mob.getGroupMembers(new HashSet<MOB>());
						final MOB leaderM=(mob.amFollowing()==null)?mob:mob.amUltimatelyFollowing();
						doc.append("\"groupname\":\"").append(leaderM.name(mob)).append("s group").append("\",")
							.append("\"leader\":\"").append(leaderM.name(mob)).append("\",")
							.append("\"status\":\"").append("Private").append("\",")
							.append("\"count\":").append(group.size()).append(",")
							.append("\"members\":[");
						boolean comma=false;
						for(final MOB M : group)
						{
							if(comma)
								doc.append(",");
							comma=true;
							doc.append("{\"name\":\"").append(M.name(mob)).append("\",")
								.append("{\"info\":{")
								.append("\"hp\":").append(M.curState().getHitPoints()).append(",")
								.append("\"mhp\":").append(M.maxState().getHitPoints()).append(",")
								.append("\"mn\":").append(M.curState().getMana()).append(",")
								.append("\"mmn\":").append(M.maxState().getMana()).append(",")
								.append("\"mv\":").append(M.curState().getMovement()).append(",")
								.append("\"mmv\":").append(M.maxState().getMovement()).append(",")
								.append("\"lvl\":").append(M.phyStats().level()).append(",");
							final int align=mob.fetchFaction(CMLib.factions().AlignID());
							if(align!=Integer.MAX_VALUE)
								doc.append("\"align\":").append(align).append(",");
							doc.append("\"tnl\":").append(M.getExpNeededLevel());
							doc.append("}");
						}
						doc.append("]");
						doc.append("}");
						return doc.toString();
					}
					break;
				case comm_channel:
					break;
				case comm_channel_players:
					if(mob != null)
					{
						final StringBuilder doc=new StringBuilder("comm.channel.players [");
						for(final Session sess : CMLib.sessions().allIterable())
						{
							if(sess != null)
							{
								MOB M=sess.mob();
								if((M!=null)&&(M.soulMate()!=null))
									M=M.soulMate();
								if((M != null)
								&&(M!=mob)
								&&(((!CMLib.flags().isCloaked(M))
									||((CMSecurity.isAllowedAnywhere(mob,CMSecurity.SecFlag.CLOAK)||CMSecurity.isAllowedAnywhere(mob,CMSecurity.SecFlag.WIZINV))&&(mob.phyStats().level()>=M.phyStats().level()))))
								&&(M.phyStats().level()>0))
								{
									doc.append("{");
									doc.append("\"name\":\""+MiniJSON.toJSONString(M.name(mob))).append(",");
									doc.append("\"channels\":[");
									for(int i=0;i<CMLib.channels().getNumChannels();i++)
									{
										ChannelsLibrary.CMChannel channel=CMLib.channels().getChannel(i);
										if(CMLib.channels().mayReadThisChannel(mob, true, M, i, false))
											doc.append("\"").append(MiniJSON.toJSONString(channel.name().toLowerCase())).append("\",");
									}
									if(doc.charAt(doc.length()-1)==',')
										doc.deleteCharAt(doc.length()-1);
									doc.append("]}");
								}
							}
						}
						doc.append("]");
					}
					break;
				case char_skills_get:
					if(json!=null)
						json=json.getCheckedJSONObject("root");
					if((json != null)&&(mob!=null))
					{
						String group=null;
						String name=null;
						if(json.containsKey("group"))
							group=json.getCheckedString("group").toLowerCase().trim();
						if(json.containsKey("name"))
							name=json.getCheckedString("name").toLowerCase().trim();
						if((group != null)
						&&(group.length()>0)
						&&((name==null)||(name.length()==0)))
						{
							final Map<Integer,List<Ability>> allMyGroups=getSkillGroups(mob);
							final StringBuilder doc=new StringBuilder("char.skills.list {");
							for(Integer grp : allMyGroups.keySet())
							{
								final String groupName=this.getAbilityGroupName(grp.intValue());
								if(groupName.equals(group))
								{
									doc.append("\"group\":\""+MiniJSON.toJSONString(groupName)+"\",");
									doc.append("\"list\":[");
									for(final Ability A : allMyGroups.get(grp))
										doc.append("\"").append(MiniJSON.toJSONString(A.name().toLowerCase())).append("\",");
									doc.setCharAt(doc.length()-1,']');
									break;
								}
							}
							doc.append("}");
							return doc.toString();
						}
						if((group != null)&&(group.length()>0)&&(name!=null)&&(name.length()!=0))
						{
							final StringBuilder doc=new StringBuilder("char.skills.info {");
							for(int a=0;a<mob.numAllAbilities();a++)
							{
								final Ability A=mob.fetchAbility(a);
								if((A!=null)&&(A.name().toLowerCase().equals(name)))
								{
									doc.append("\"group\":\""+MiniJSON.toJSONString(getAbilityGroupName(A.abilityCode()))+"\",");
									doc.append("\"skill\":\""+MiniJSON.toJSONString(name)+"\",");
									doc.append("\"info\":\""+MiniJSON.toJSONString(CMLib.help().getHelpText(A.Name().toUpperCase(), mob, false).toString())+"\",");
								}
							}
							doc.append("}");
							return doc.toString();
						}
						else
						{
							final StringBuilder doc=new StringBuilder("char.skills.groups [");
							final Map<Integer,List<Ability>> allMyGroups=getSkillGroups(mob);
							if(allMyGroups.size()>0)
							{
								for(Integer grp : allMyGroups.keySet())
								{
									final String groupName=this.getAbilityGroupName(grp.intValue());
									doc.append("\"").append(MiniJSON.toJSONString(groupName)).append("\",");
								}
								doc.setCharAt(doc.length()-1,']');
							}
							else
								doc.append("]");
							return doc.toString();
						}
					}
					break;
				default:
					break;
				}
				return null;
			}
			catch(final Exception e)
			{
				Log.errOut("Error parsing GMCP Package: "+pkg+": "+e.getMessage());
				if(CMSecurity.isDebugging(DbgFlag.GMCP))
					Log.errOut(e);
				return null;
			}
		}
		catch (final MJSONException e)
		{
			Log.errOut("Error parsing GMCP JSON: "+e.getMessage());
			if(CMSecurity.isDebugging(DbgFlag.GMCP))
				Log.errOut("JSON: "+jsonData);
			return null;
		}
	}

	@Override
	public byte[] processGmcp(final Session session, final String data, final Map<String,Double> supportables)
	{
		final String doc=processGmcpStr(session, data, supportables);
		if(doc != null)
		{
			if(CMSecurity.isDebugging(DbgFlag.GMCP))
				Log.debugOut("GMCP Sent: "+doc);
			return buildGmcpResponse(doc);
		}
		return null;
	}

	protected byte[] possiblePingGmcp(final Session session, final Map<String,Long> reporteds, final Map<String,Double> supportables, final String command)
	{
		final String chunkStr=processGmcpStr(session, command, supportables);
		if(chunkStr!=null)
		{
			final Long oldHash=reporteds.get(command);
			final long newHash=chunkStr.hashCode();
			if((oldHash==null)||(oldHash.longValue()!=newHash))
			{
				reporteds.put(command, Long.valueOf(newHash));
				if(CMSecurity.isDebugging(DbgFlag.GMCP))
					Log.debugOut("GMCP Sent: "+chunkStr);
				return buildGmcpResponse(chunkStr);
			}
		}
		return null;
	}
	
	@Override
	public byte[] invokeRoomChangeGmcp(final Session session, final Map<String,Long> reporteds, final Map<String,Double> supportables)
	{
		try
		{
			if(supportables.containsKey("room.info")||supportables.containsKey("room"))
			{
				final ByteArrayOutputStream bout=new ByteArrayOutputStream();
				final MOB mob=session.mob();
				byte[] buf;
				if(mob!=null)
				{
					final Room room;
					synchronized(mob)
					{
						room = mob.location();
					}
					if(room!=null)
					{
						final Long oldRoomHash=reporteds.get("system.currentRoom");
						if((oldRoomHash==null)||(room.hashCode()!=oldRoomHash.longValue()))
						{
							reporteds.put("system.currentRoom", Long.valueOf(room.hashCode()));
							final String command="room.info";
							final char[] cmd=command.toCharArray();
							buf=processGmcp(session, new String(cmd), supportables);
							if(buf!=null)
								bout.write(buf);
						}
					}
				}
				return (bout.size()==0) ? null: bout.toByteArray();
			}
		}
		catch(final java.io.IOException ioe)
		{
			if(CMSecurity.isDebugging(DbgFlag.TELNET))
				Log.errOut(ioe);
		}
		catch(final Throwable t)
		{
			Log.errOut(t);
		}
		return null;
	}

	@Override
	public byte[] pingGmcp(final Session session, final Map<String,Long> reporteds, final Map<String,Double> supportables)
	{
		try
		{
			final Long nextMedReport=reporteds.get("system.nextMedReport");
			final Long nextLongReport=reporteds.get("system.nextLongReport");
			final Long nextTruePingReport=reporteds.get("system.nextTruePing");
			final long now=System.currentTimeMillis();
			final boolean charSupported=supportables.containsKey("char");
			final ByteArrayOutputStream bout=new ByteArrayOutputStream();
			final MOB mob=session.mob();
			byte[] buf;
			if(charSupported||supportables.containsKey("char.vitals"))
			{
				buf=possiblePingGmcp(session, reporteds, supportables, "char.vitals");
				if(buf!=null)
					bout.write(buf);
			}
			if((nextTruePingReport==null)||(now>nextTruePingReport.longValue()))
			{
				final long tickMillis=CharState.REAL_TICK_ADJUST_FACTOR*CMProps.getTickMillis();
				reporteds.put("system.nextTruePing", new Long((nextTruePingReport==null)?(now+tickMillis):(nextTruePingReport.longValue()+tickMillis)));
				if(supportables.containsKey("comm.tick")||supportables.containsKey("comm"))
				{
					if(CMSecurity.isDebugging(DbgFlag.TELNET))
						Log.debugOut("GMCP Sent: comm.tick { }");
					bout.write(Session.TELNETBYTES_GMCP_HEAD);
					bout.write("comm.tick { }".getBytes());
					bout.write(Session.TELNETBYTES_END_SB);
				}
			}
			if((nextMedReport==null)||(now>nextMedReport.longValue()))
			{
				reporteds.put("system.nextMedReport", new Long(now+3999));
				if(charSupported||supportables.containsKey("char.status"))
				{
					buf=possiblePingGmcp(session, reporteds, supportables, "char.status");
					if(buf!=null)
						bout.write(buf);
				}
				if((mob!=null)&&((mob.amFollowing()!=null)||(mob.numFollowers()>0)))
				{
					if(supportables.containsKey("group"))
					{
						buf=possiblePingGmcp(session, reporteds, supportables, "group");
						if(buf!=null)
							bout.write(buf);
					}
				}
			}
			if((nextLongReport==null)||(now>nextLongReport.longValue()))
			{
				reporteds.put("system.nextLongReport", new Long(now+15996));
				if(charSupported||supportables.containsKey("char.worth"))
				{
					buf=possiblePingGmcp(session, reporteds, supportables, "char.worth");
					if(buf!=null)
						bout.write(buf);
				}
				if(charSupported||supportables.containsKey("char.maxstats"))
				{
					buf=possiblePingGmcp(session, reporteds, supportables, "char.maxstats");
					if(buf!=null)
						bout.write(buf);
				}
				if(charSupported||supportables.containsKey("char.base"))
				{
					buf=possiblePingGmcp(session, reporteds, supportables, "char.base");
					if(buf!=null)
						bout.write(buf);
				}
				if(charSupported||supportables.containsKey("char.statusvars"))
				{
					buf=possiblePingGmcp(session, reporteds, supportables, "char.statusvars");
					if(buf!=null)
						bout.write(buf);
				}
			}
			final byte[] roomStuff = this.invokeRoomChangeGmcp(session, reporteds, supportables);
			if(roomStuff != null)
				bout.write(roomStuff);
			return (bout.size()==0) ? null: bout.toByteArray();
		}
		catch(final java.io.IOException ioe)
		{
			if(CMSecurity.isDebugging(DbgFlag.TELNET))
				Log.errOut(ioe);
		}
		catch(final Throwable t)
		{
			Log.errOut(t);
		}
		return null;
	}
}
