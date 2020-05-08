package com.planet_ink.coffee_mud.WebMacros.grinder;

import com.planet_ink.coffee_web.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.ColorLibrary.Color;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Clan.MemberRecord;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import com.planet_ink.coffee_mud.WebMacros.Authenticate;

import java.util.*;

/*
   Copyright 2020-2020 Bo Zimmerman

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
public class GrinderPlanes
{
	public String name()
	{
		return "GrinderPlanes";
	}

	public String standardField(final PlanarAbility.PlanarVar var, final String httpVal)
	{
		if((httpVal != null)&&(httpVal.trim().length()>0))
		{
			if(CMath.isNumber(httpVal.trim()))
				return var.toString().toLowerCase()+"="+httpVal+" ";
			return var.toString().toLowerCase()+"=\""+CMStrings.replaceAll(httpVal,"\"","\\\"")+"\" ";
		}
		return "";
	}

	public String runMacro(final HTTPRequest httpReq, final String parm)
	{
		String last=httpReq.getUrlParameter("PLANE");
		if(last==null)
			return " @break@";
		if(last.length()>0)
		{
			final PlanarAbility planeSet = (PlanarAbility)CMClass.getAbilityPrototype("StdPlanarAbility");
			final StringBuilder finalStr = new StringBuilder("");
			for(final PlanarAbility.PlanarVar var: PlanarAbility.PlanarVar.values())
			{
				final String key=var.name().toUpperCase().trim();
				String httpVal = httpReq.getUrlParameter(key);
				if(httpVal == null)
					httpVal="";
				switch(var)
				{
				case ABSORB:
					finalStr.append(standardField(var,httpVal));
					break;
				case ADJSIZE:
				{
					final String weight=httpReq.getUrlParameter(key+"_WEIGHT");
					final String height=httpReq.getUrlParameter(key+"_HEIGHT");
					if(((weight != null)&&(weight.trim().length()>0))
					||((height != null)&&(height.trim().length()>0)))
					{
						finalStr.append(var.toString().toLowerCase()).append("=\"");
						if((height != null)&&(height.trim().length()>0))
						{
							if(CMath.isInteger(height))
								finalStr.append("height="+height);
							else
								return var.toString()+":height is not valid.";
						}
						if((weight != null)&&(weight.trim().length()>0))
						{
							if(CMath.isInteger(weight))
							{
								if((height != null)&&(height.trim().length()>0))
									finalStr.append(" ");
								finalStr.append("weight="+weight);
							}
							else
								return var.toString()+":weight is not valid.";
						}
						finalStr.append("\" ");
					}
					break;
				}
				case ADJSTAT:
					finalStr.append(standardField(var,httpVal));
					break;
				case ADJUST:
					finalStr.append(standardField(var,httpVal));
					break;
				case ALIGNMENT:
					if(CMath.isInteger(httpVal))
						finalStr.append(standardField(var,httpVal));
					else
					if(httpVal.length()>0)
						return var.toString()+" is not valid.";
					break;
				case AREABLURBS:
				{
					final Map<String,String> parsed = CMParms.parseEQParms(httpVal);
					if(httpReq.isUrlParameter(key+"_1"))
					{
						parsed.clear();
						int i=1;
						while(httpReq.isUrlParameter(key+"_"+i))
						{
							final String chg=httpReq.getUrlParameter(key+"_"+i);
							final String to=httpReq.getUrlParameter(key+"_V"+i);
							if(chg.length()>0)
								parsed.put(chg, to);
							i++;
						}
					}
					if(parsed.size()>0)
					{
						finalStr.append(var.toString().toLowerCase()).append("=\"");
						for(final String p : parsed.keySet())
						{
							finalStr.append(p).append("=\\\"");
							final String val = parsed.get(p);
							finalStr.append(CMStrings.replaceAll(val, "\"", "\\\\\""));
							finalStr.append("\\\"");
							finalStr.append(" ");
						}
						finalStr.append("\" ");
					}
					break;
				}
				case ATMOSPHERE:
				{
					finalStr.append(standardField(var,httpVal.toLowerCase()));
					break;
				}
				case BEHAVAFFID:
				{
					if(httpReq.isUrlParameter(key+"_1"))
					{
						final List<Pair<String,String>> parsed = new ArrayList<Pair<String,String>>();
						int i=1;
						while(httpReq.isUrlParameter(key+"_"+i))
						{
							final String chg=httpReq.getUrlParameter(key+"_"+i);
							final String cp=httpReq.getUrlParameter(key+"_S"+i);
							final String to=httpReq.getUrlParameter(key+"_V"+i);
							if((chg!=null)&&(chg.length()>0)&&(to.length()>0))
								parsed.add(new Pair<String,String>(chg,("on".equalsIgnoreCase(cp)?"*":"")+to));
							i++;
						}
						if(parsed.size()>0)
						{
							finalStr.append(var.toString().toLowerCase()).append("=\"");
							for(final Pair<String,String> p : parsed)
							{
								finalStr.append(p.first);
								if(p.second.trim().length()>0)
									finalStr.append("=").append(p.second);
								if(p != parsed.get(parsed.size()-1))
									finalStr.append(" ");
							}
							finalStr.append("\" ");
						}
					}
					break;
				}
				case BEHAVE:
				{
					if(httpReq.isUrlParameter(key+"_1"))
					{
						final List<Pair<String,String>> parsed = new ArrayList<Pair<String,String>>();
						int i=1;
						while(httpReq.isUrlParameter(key+"_"+i))
						{
							final String chg=httpReq.getUrlParameter(key+"_"+i);
							final String to=httpReq.getUrlParameter(key+"_V"+i);
							if(chg.length()>0)
								parsed.add(new Pair<String,String>(chg,to));
							i++;
						}
						if(parsed.size()>0)
						{
							finalStr.append(var.toString().toLowerCase()).append("=\"");
							for(final Pair<String,String> p : parsed)
							{
								finalStr.append(p.first);
								if(p.second.trim().length()>0)
									finalStr.append("(").append(p.second).append(")");
								if(p != parsed.get(parsed.size()-1))
									finalStr.append(" ");
							}
							finalStr.append("\" ");
						}
					}
					break;
				}
				case BONUSDAMAGESTAT:
					finalStr.append(standardField(var,httpVal));
					break;
				case CATEGORY:
				{
					final List<String> selected = CMParms.parseSpaces(httpVal,true);
					if(httpReq.isUrlParameter(key))
					{
						selected.add(httpReq.getUrlParameter(key));
						for(int i=1;httpReq.isUrlParameter(key+i);i++)
							selected.add(CMStrings.capitalizeAllFirstLettersAndLower(httpReq.getUrlParameter(key+i)));
					}
					finalStr.append(standardField(var,CMParms.toListString(selected)));
					break;
				}
				case DESCRIPTION:
				{
					httpVal=CMStrings.replaceAll(httpVal,"\n", "%0D");
					httpVal=CMStrings.replaceAll(httpVal,"\r", "");
					finalStr.append(standardField(var,httpVal));
					break;
				}
				case ELITE:
					if(CMath.isInteger(httpVal))
						finalStr.append(standardField(var,httpVal));
					else
					if(httpVal.length()>0)
						return var.toString()+" is not valid.";
					break;
				case ENABLE:
				{
					if(httpReq.isUrlParameter(key+"_1"))
					{
						final List<Pair<String,String>> parsed = new ArrayList<Pair<String,String>>();
						int i=1;
						while(httpReq.isUrlParameter(key+"_"+i))
						{
							final String chg=httpReq.getUrlParameter(key+"_"+i);
							final String to=httpReq.getUrlParameter(key+"_V"+i);
							if(chg.length()>0)
								parsed.add(new Pair<String,String>(chg,to));
							i++;
						}
						if(parsed.size()>0)
						{
							finalStr.append(var.toString().toLowerCase()).append("=\"");
							for(final Pair<String,String> p : parsed)
							{
								if(p.first.equalsIgnoreCase("number"))
								{
									final int x=p.second.indexOf('/');
									if((x<0)
									||(!CMath.isInteger(p.second.substring(0,x)))
									||(!CMath.isInteger(p.second.substring(x+1))))
										return var.toString()+":+number is not valid.";
								}
								finalStr.append(p.first);
								if(p.second.trim().length()>0)
									finalStr.append("(").append(p.second).append(")");
								if(p != parsed.get(parsed.size()-1))
									finalStr.append(" ");
							}
							finalStr.append("\" ");
						}
					}
					break;
				}
				case FACTIONS:
				{
					if(httpReq.isUrlParameter(key+"_1"))
					{
						final List<Pair<String,String>> parsed = new ArrayList<Pair<String,String>>();
						int i=1;
						while(httpReq.isUrlParameter(key+"_"+i))
						{
							final String chg=httpReq.getUrlParameter(key+"_"+i);
							final String to=httpReq.getUrlParameter(key+"_V"+i);
							if(chg.length()>0)
								parsed.add(new Pair<String,String>(chg,to));
							i++;
						}
						if(parsed.size()>0)
						{
							finalStr.append(var.toString().toLowerCase()).append("=\"");
							for(final Pair<String,String> p : parsed)
							{
								if(CMath.isInteger(p.second))
								{
									finalStr.append(p.first.toLowerCase()+"("+p.second+")");
								}
								else
								if(!p.first.equals("*"))
									return var.toString()+":"+p.first+" has invalid number.";
								else
									finalStr.append(p.first.toLowerCase());
								if(p != parsed.get(parsed.size()-1))
									finalStr.append(" ");
							}
							finalStr.append("\" ");
						}
					}
					break;
				}
				case FATIGUERATE:
					if(CMath.isInteger(httpVal.trim()))
						finalStr.append(standardField(var,httpVal));
					else
					if(httpVal.length()>0)
						return var.toString()+" is not valid.";
					break;
				case HOURS:
					if(CMath.isInteger(httpVal))
						finalStr.append(standardField(var,httpVal));
					else
					if(httpVal.length()>0)
						return var.toString()+" is not valid.";
					break;
				case ID:
					last = httpVal;
					break;
				case LEVELADJ:
					if(CMath.isInteger(httpVal)||CMath.isMathExpression(httpVal))
						finalStr.append(standardField(var,httpVal));
					else
					if(httpVal.length()>0)
						return var.toString()+" is not valid.";
					break;
				case LIKE:
				{
					finalStr.append(standardField(var,httpVal));
					break;
				}
				case MIXRACE:
				{
					finalStr.append(standardField(var,httpVal));
					break;
				}
				case MOBCOPY:
					if(CMath.isInteger(httpVal))
						finalStr.append(standardField(var,httpVal));
					else
					if(httpVal.length()>0)
						return var.toString()+" is not valid.";
					break;
				case MOBRESIST:
					finalStr.append(standardField(var,httpVal));
					break;
				case PREFIX:
					finalStr.append(standardField(var,httpVal));
					break;
				case PROMOTIONS:
				{
					if(httpReq.isUrlParameter(key+"_1"))
					{
						final List<Pair<String,String>> parsed = new ArrayList<Pair<String,String>>();
						int i=1;
						while(httpReq.isUrlParameter(key+"_"+i))
						{
							final String chg=httpReq.getUrlParameter(key+"_"+i);
							final String to=httpReq.getUrlParameter(key+"_V"+i);
							if(chg.length()>0)
							{
								if(!CMath.isInteger(to))
									return var.toString()+":"+chg+" pct is not valid.";
								parsed.add(new Pair<String,String>(chg,to));
							}
							i++;
						}
						if(parsed.size()>0)
						{
							finalStr.append(var.toString().toLowerCase()).append("=\"");
							for(final Pair<String,String> p : parsed)
							{
								finalStr.append(p.first);
								if(p.second.trim().length()>0)
									finalStr.append("(").append(p.second).append(")");
								if(p != parsed.get(parsed.size()-1))
									finalStr.append(",");
							}
							finalStr.append("\" ");
						}
					}
					break;
				}
				case RECOVERRATE:
					if(CMath.isInteger(httpVal))
						finalStr.append(standardField(var,httpVal));
					else
					if(httpVal.length()>0)
						return var.toString()+" is not valid.";
					break;
				case AEFFECT:
				case REFFECT:
				{
					if(httpReq.isUrlParameter(key+"_1"))
					{
						final List<Pair<String,String>> parsed = new ArrayList<Pair<String,String>>();
						int i=1;
						while(httpReq.isUrlParameter(key+"_"+i))
						{
							final String chg=httpReq.getUrlParameter(key+"_"+i);
							final String to=httpReq.getUrlParameter(key+"_V"+i);
							if(chg.length()>0)
								parsed.add(new Pair<String,String>(chg,to));
							i++;
						}
						if(parsed.size()>0)
						{
							finalStr.append(var.toString().toLowerCase()).append("=\"");
							for(final Pair<String,String> p : parsed)
							{
								finalStr.append(p.first);
								if(p.second.trim().length()>0)
									finalStr.append("(").append(p.second).append(")");
								if(p != parsed.get(parsed.size()-1))
									finalStr.append(" ");
							}
							finalStr.append("\" ");
						}
					}
					break;
				}
				case REQWEAPONS:
				{
					if(httpReq.isUrlParameter(key))
					{
						final StringBuilder str=new StringBuilder("");
						str.append(httpReq.getUrlParameter(key));
						for(int i=1;httpReq.isUrlParameter(key+i);i++)
							str.append(" ").append(httpReq.getUrlParameter(key+i));
						finalStr.append(standardField(var,str.toString()));
					}
					break;
				}
				case ROOMADJS:
				{
					if((httpVal != null)&&(httpVal.trim().length()>0))
					{
						final String chance = httpReq.getUrlParameter(key+"_CHANCE");
						if(chance.length()>0)
						{
							if(CMath.isInteger(chance.trim()))
								httpVal=chance+" "+httpVal;
							else
								return var.toString()+":chance is not valid.";
						}
						if("on".equalsIgnoreCase(httpReq.getUrlParameter(key+"_UP")))
							httpVal = "UP "+httpVal;
						finalStr.append(var.toString().toLowerCase()+"=\"").append(httpVal).append("\" ");
					}
					break;
				}
				case ROOMCOLOR:
				{
					if(httpVal.trim().length()>0)
					{
						httpVal = ("on".equalsIgnoreCase(httpReq.getUrlParameter(key+"_UP"))?"UP ":"") + httpVal;
						finalStr.append(standardField(var,httpVal));
					}
					break;
				}
				case SETSTAT:
					finalStr.append(standardField(var,httpVal));
					break;
				case SPECFLAGS:
				{
					final List<String> selected = new ArrayList<String>(2);
					if(httpReq.isUrlParameter("SPECFLAGS"))
					{
						selected.add(httpReq.getUrlParameter("SPECFLAGS"));
						int x=1;
						while(httpReq.getUrlParameter("SPECFLAGS"+x)!=null)
						{
							selected.add(httpReq.getUrlParameter("SPECFLAGS"+x));
							x++;
						}
					}
					if(selected.size()>0)
					{
						finalStr.append(var.toString().toLowerCase()).append("=\"");
						for(final String s : selected)
						{
							finalStr.append(s);
							if(s != selected.get(selected.size()-1))
								finalStr.append(" ");
						}
						finalStr.append("\" ");
					}
					break;
				}
				case TRANSITIONAL:
				{
					if(CMath.s_bool(httpVal))
						finalStr.append(standardField(var,httpVal));
					break;
				}
				case WEAPONMAXRANGE:
					if(CMath.isInteger(httpVal))
						finalStr.append(standardField(var,httpVal));
					else
					if(httpVal.length()>0)
						return var.toString()+" is not valid.";
					break;
				default:
					break;

				}
			}
			final MOB M = Authenticate.getAuthenticatedMob(httpReq);
			if(M==null)
				return "[authentication error]";
			final boolean exists = planeSet.getPlanarVars(last)!=null;
			final String err = planeSet.addOrEditPlane(last, finalStr.toString());
			if(exists)
			{
				if(err.startsWith("ERROR"))
					return err;
				if(err.trim().length()>0)
					Log.infoOut(M.Name()+" modified plane: "+last+": \n\r"+err);
			}
			else
			{
				// adding new
				if(err != null)
					return err;
				Log.infoOut(M.Name()+" successfully added plane: "+last);
			}
		}
		return "";
	}
}
