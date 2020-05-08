package com.planet_ink.coffee_mud.WebMacros;

import com.planet_ink.coffee_web.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.PlanarAbility.PlanarVar;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.ColorLibrary.Color;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

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
public class PlanarData extends StdWebMacro
{
	@Override
	public String name()
	{
		return "PlanarData";
	}

	@Override
	public boolean isAdminMacro()
	{
		return true;
	}

	@Override
	public String runMacro(final HTTPRequest httpReq, final String parm, final HTTPResponse httpResp)
	{
		final java.util.Map<String,String> parms=parseParms(parm);
		final String last=httpReq.getUrlParameter("PLANE");
		final StringBuilder str=new StringBuilder("");
		if((last != null) && (last.length()>0))
		{
			if(!httpReq.getRequestObjects().containsKey("SYSTEM_PLANE_CACHE_"+last.toUpperCase()))
			{
				final PlanarAbility planeSet = (PlanarAbility)CMClass.getAbility("StdPlanarAbility");
				if(!planeSet.getAllPlaneKeys().contains(last.toUpperCase().trim()))
				{
					planeSet.setMiscText("DEFAULT_NEW");
					planeSet.getPlaneVars().put(PlanarVar.ID.name(), last);
				}
				else
					planeSet.setMiscText(last);
				httpReq.getRequestObjects().put("SYSTEM_PLANE_CACHE_"+last.toUpperCase(), planeSet);
			}
			final PlanarAbility planeObj = (PlanarAbility)httpReq.getRequestObjects().get("SYSTEM_PLANE_CACHE_"+last.toUpperCase());
			final Map<String,String> planarData = planeObj.getPlaneVars();
			if(planarData != null)
			{
				for(final String p : parms.keySet())
				{
					final String key=p.toUpperCase().trim();
					String httpVal = httpReq.getUrlParameter(key);
					if(httpVal == null)
						httpVal = planarData.get(key);
					if(httpVal == null)
						httpVal="";
					final PlanarAbility.PlanarVar var = (PlanarAbility.PlanarVar)CMath.s_valueOf(PlanarAbility.PlanarVar.class, key);
					if(var == null)
						continue;
					switch(var)
					{
					case ABSORB:
						str.append(httpVal).append(", ");
						break;
					case ADJSIZE:
						if(parms.containsKey("HEIGHT"))
						{
							if(httpReq.isUrlParameter(key+"_HEIGHT"))
								httpVal="HEIGHT="+httpReq.getUrlParameter(key+"_HEIGHT");
							str.append(CMParms.getParmStr(httpVal, "HEIGHT", "")).append(", ");
						}
						if(parms.containsKey("WEIGHT"))
						{
							if(httpReq.isUrlParameter(key+"_WEIGHT"))
								httpVal="WEIGHT="+httpReq.getUrlParameter(key+"_WEIGHT");
							str.append(CMParms.getParmStr(httpVal, "WEIGHT", "")).append(", ");
						}
						break;
					case ADJSTAT:
						str.append(httpVal).append(", ");
						break;
					case ADJUST:
						str.append(httpVal).append(", ");
						break;
					case ALIGNMENT:
						str.append(httpVal).append(", ");
						break;
					case AREABLURBS:
					{
						final Map<String,String> parsed = CMParms.parseEQParmsLow(httpVal);
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
						int i=1;
						for(final String k : parsed.keySet())
						{
							if(parms.containsKey("EXISTS_"+i))
							{
								str.append("true").append(", ");
								break;
							}
							if(parms.containsKey(""+i))
								str.append(k).append(", ");
							if(parms.containsKey("V"+i))
								str.append(super.htmlOutgoingFilter(parsed.get(k))).append(", ");
							i++;
						}
						break;
					}
					case ATMOSPHERE:
					{
						str.append("<OPTION VALUE=\"\" "+(((httpVal==null)||(httpVal.length()==0))?"SELECTED":"")+">").append("Normal");
						for(final String opt : RawMaterial.CODES.NAMES())
							str.append("<OPTION VALUE=\""+opt+"\" "+(opt.equalsIgnoreCase(httpVal)?"SELECTED":"")+">").append(opt);
						str.append(", ");
						break;
					}
					case BEHAVAFFID:
					{
						final PairList<String,String> parsed = CMParms.parseEQParmsList(httpVal);
						if(httpReq.isUrlParameter(key+"_1"))
						{
							parsed.clear();
							int i=1;
							while(httpReq.isUrlParameter(key+"_"+i))
							{
								final String chg=httpReq.getUrlParameter(key+"_"+i);
								final String cp=httpReq.getUrlParameter(key+"_S"+i);
								final String to=httpReq.getUrlParameter(key+"_V"+i);
								if(chg.length()>0)
									parsed.add(chg, ("on".equalsIgnoreCase(cp)?"*":"")+to);
								i++;
							}
						}
						int i=1;
						for(final Pair<String,String> k : parsed)
						{
							if(parms.containsKey("EXISTS_"+i))
							{
								str.append("true").append(", ");
								break;
							}
							if(parms.containsKey(""+i))
							{
								str.append("<OPTION VALUE=\"\" >").append("Delete");
								for(final Enumeration<Behavior> b=CMClass.behaviors();b.hasMoreElements();)
								{
									final Behavior B=b.nextElement();
									str.append("<OPTION VALUE=\""+B.ID().toLowerCase()+"\" "+(B.ID().equalsIgnoreCase(k.first)?"SELECTED":"")+">").append(B.ID());
								}
								str.append(", ");
							}
							if(parms.containsKey("S"+i))
							{
								final boolean st=k.second.startsWith("*");
								str.append(st?"CHECKED":"").append(", ");
							}
							if(parms.containsKey("V"+i))
							{
								String val=k.second;
								if((val != null) && val.startsWith("*"))
									val=val.substring(1);
								for(final Enumeration<Behavior> b=CMClass.behaviors();b.hasMoreElements();)
								{
									final Behavior B=b.nextElement();
									str.append("<OPTION VALUE=\""+B.ID().toLowerCase()+"\" "+(B.ID().equalsIgnoreCase(val)?"SELECTED":"")+">").append(B.ID());
								}
								str.append(", ");
							}
							i++;
						}
						if(parms.containsKey(""+(parsed.size()+1))||parms.containsKey("V"+(parsed.size()+1)))
						{
							str.append("<OPTION VALUE=\"\" SELECTED>").append("Select");
							for(final Enumeration<Behavior> b=CMClass.behaviors();b.hasMoreElements();)
							{
								final Behavior B=b.nextElement();
								str.append("<OPTION VALUE=\""+B.ID().toLowerCase()+"\" >").append(B.ID());
							}
							str.append(", ");
						}
						break;
					}
					case BEHAVE:
					{
						final List<Pair<String,String>> parsed = CMParms.parseSpaceParenList(httpVal);
						if(httpReq.isUrlParameter(key+"_1"))
						{
							parsed.clear();
							int i=1;
							while(httpReq.isUrlParameter(key+"_"+i))
							{
								final String chg=httpReq.getUrlParameter(key+"_"+i);
								final String to=httpReq.getUrlParameter(key+"_V"+i);
								if(chg.length()>0)
									parsed.add(new Pair<String,String>(chg,to));
								i++;
							}
						}
						int i=1;
						for(final Pair<String,String> k : parsed)
						{
							if(parms.containsKey("EXISTS_"+i))
							{
								str.append("true").append(", ");
								break;
							}
							if(parms.containsKey(""+i))
							{
								str.append("<OPTION VALUE=\"\" >").append("Delete");
								for(final Enumeration<Behavior> b=CMClass.behaviors();b.hasMoreElements();)
								{
									final Behavior B=b.nextElement();
									str.append("<OPTION VALUE=\""+B.ID()+"\" "+(B.ID().equalsIgnoreCase(k.first)?"SELECTED":"")+">").append(B.ID());
								}
								str.append(", ");
							}
							if(parms.containsKey("V"+i))
								str.append(super.htmlOutgoingFilter(k.second)).append(", ");
							i++;
						}
						if(parms.containsKey(""+(parsed.size()+1)))
						{
							str.append("<OPTION VALUE=\"\" SELECTED>").append("Select");
							for(final Enumeration<Behavior> b=CMClass.behaviors();b.hasMoreElements();)
							{
								final Behavior B=b.nextElement();
								str.append("<OPTION VALUE=\""+B.ID()+"\" >").append(B.ID());
							}
							str.append(", ");
						}
						break;
					}
					case BONUSDAMAGESTAT:
						str.append("<OPTION VALUE=\"\" "+(((httpVal==null)||(httpVal.length()==0))?"SELECTED":"")+">").append("None");
						for(final int stat : CharStats.CODES.BASECODES())
						{
							str.append("<OPTION VALUE=\""+CharStats.CODES.NAME(stat).toLowerCase()+"\" "+(CharStats.CODES.NAME(stat).equalsIgnoreCase(httpVal)?"SELECTED":"")+">")
							   .append(CMStrings.capitalizeAndLower(CharStats.CODES.NAME(stat)));
						}
						break;
					case OPPOSED:
					{
						final List<String> selected = CMParms.parseSpaces(httpVal,true);
						if(httpReq.isUrlParameter(key))
						{
							selected.add(httpReq.getUrlParameter(key));
							for(int i=1;httpReq.isUrlParameter(key+i);i++)
								selected.add(httpReq.getUrlParameter(key+i).toLowerCase());
						}
						for(final String pkey : planeObj.getAllPlaneKeys())
						{
							str.append("<OPTION VALUE=\""+CMStrings.capitalizeAllFirstLettersAndLower(pkey)+"\" "+(selected.contains(pkey.toLowerCase())?"SELECTED":"")+">")
							   .append(CMStrings.capitalizeAllFirstLettersAndLower(pkey));
						}
						break;
					}
					case CATEGORY:
						str.append(httpVal).append(", ");
						break;
					case DESCRIPTION:
						str.append(httpVal).append(", ");
						break;
					case ELITE:
						str.append(httpVal).append(", ");
						break;
					case ENABLE:
					{
						final List<Pair<String,String>> parsed = CMParms.parseSpaceParenList(httpVal);
						if(httpReq.isUrlParameter(key+"_1"))
						{
							parsed.clear();
							int i=1;
							while(httpReq.isUrlParameter(key+"_"+i))
							{
								final String chg=httpReq.getUrlParameter(key+"_"+i);
								final String to=httpReq.getUrlParameter(key+"_V"+i);
								if(chg.length()>0)
									parsed.add(new Pair<String,String>(chg,to));
								i++;
							}
						}
						@SuppressWarnings("unchecked")
						List<String> options = (List<String>)httpReq.getRequestObjects().get("SYS_PLANE_ENOPTIONS");
						if(options == null)
						{
							options = new ArrayList<String>();
							options.add("number");
							final List<String> flags = new ConvertingList<String,String>(Arrays.asList(Ability.FLAG_DESCS),Converter.toLowerCase);
							options.addAll(new ConvertingList<String,String>(Arrays.asList(Ability.DOMAIN_DESCS),Converter.toLowerCase));
							options.addAll(flags);
							options.addAll(new XVector<String>(
									new ConvertingEnumeration<Ability,String>(
											new FilteredEnumeration<Ability>(CMClass.abilities(), new Filterer<Ability>(){
												@Override
												public boolean passesFilter(final Ability obj)
												{
													if((obj.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_ARCHON)
														return false;
													return !CMParms.containsIgnoreCase(flags,obj.ID().toLowerCase());
												}
											})
										, new Converter<Ability,String>()
										  {
												@Override
												public String convert(final Ability obj)
												{
													return obj.ID();
												}
										  }
									)));
							httpReq.getRequestObjects().put("SYS_PLANE_ENOPTIONS",options);
						}
						int i=1;
						for(final Pair<String,String> k : parsed)
						{
							if(parms.containsKey("EXISTS_"+i))
							{
								str.append("true").append(", ");
								break;
							}
							if(parms.containsKey(""+i))
							{
								str.append("<OPTION VALUE=\"\" >").append("Delete");
								for(final String opt : options)
									str.append("<OPTION VALUE=\""+opt+"\" "+(opt.equalsIgnoreCase(k.first)?"SELECTED":"")+">").append(opt);
								str.append(", ");
							}
							if(parms.containsKey("V"+i))
								str.append(super.htmlOutgoingFilter(k.second)).append(", ");
							i++;
						}
						if(parms.containsKey(""+(parsed.size()+1)))
						{
							str.append("<OPTION VALUE=\"\" SELECTED>").append("Select");
							for(final String opt : options)
								str.append("<OPTION VALUE=\""+opt+"\" >").append(opt);
							str.append(", ");
						}
						break;
					}
					case FACTIONS:
					{
						final List<Pair<String,String>> parsed = CMParms.parseSpaceParenList(httpVal);
						if(httpReq.isUrlParameter(key+"_1"))
						{
							parsed.clear();
							int i=1;
							while(httpReq.isUrlParameter(key+"_"+i))
							{
								final String chg=httpReq.getUrlParameter(key+"_"+i);
								final String to=httpReq.getUrlParameter(key+"_V"+i);
								if(chg.length()>0)
									parsed.add(new Pair<String,String>(chg,to));
								i++;
							}
						}
						@SuppressWarnings("unchecked")
						List<String> options = (List<String>)httpReq.getRequestObjects().get("SYS_PLANE_FACTIONS");
						if(options == null)
						{
							options = new ArrayList<String>();
							options.add("*");
							options.addAll(new XVector<String>(new ConvertingEnumeration<Faction,String>(CMLib.factions().factions(), new Converter<Faction,String>()
							{
								@Override
								public String convert(final Faction obj)
								{
									if(obj.name().indexOf(' ')<0)
										return obj.name();
									else
										return obj.factionID();
								}
							})));
							httpReq.getRequestObjects().put("SYS_PLANE_FACTIONS",options);
						}
						int i=1;
						for(final Pair<String,String> k : parsed)
						{
							if(parms.containsKey("EXISTS_"+i))
							{
								str.append("true").append(", ");
								break;
							}
							if(parms.containsKey(""+i))
							{
								str.append("<OPTION VALUE=\"\" >").append("Delete");
								for(final String opt : options)
									str.append("<OPTION VALUE=\""+opt+"\" "+(opt.equalsIgnoreCase(k.first)?"SELECTED":"")+">").append(opt);
								str.append(", ");
							}
							if(parms.containsKey("V"+i))
								str.append(super.htmlOutgoingFilter(k.second)).append(", ");
							i++;
						}
						if(parms.containsKey(""+(parsed.size()+1)))
						{
							str.append("<OPTION VALUE=\"\" SELECTED>").append("Select");
							for(final String opt : options)
								str.append("<OPTION VALUE=\""+opt+"\" >").append(opt);
							str.append(", ");
						}
						break;
					}
					case FATIGUERATE:
						str.append(httpVal).append(", ");
						break;
					case HOURS:
						str.append(httpVal).append(", ");
						break;
					case ID:
						str.append(httpVal).append(", ");
						break;
					case LEVELADJ:
						str.append(httpVal).append(", ");
						break;
					case LIKE:
					{
						str.append("<OPTION VALUE=\"\" "+(((httpVal==null)||(httpVal.length()==0))?"SELECTED":"")+">").append("None");
						for(final String like : planeObj.getAllPlaneKeys())
						{
							if(!like.equalsIgnoreCase(last))
								str.append("<OPTION VALUE=\""+like+"\" "+(like.equalsIgnoreCase(httpVal)?"SELECTED":"")+">").append(like);
						}
						str.append(", ");
						break;
					}
					case MIXRACE:
					{
						str.append("<OPTION VALUE=\"\" "+(((httpVal==null)||(httpVal.length()==0))?"SELECTED":"")+">").append("None");
						for(final Enumeration<Race> r=CMClass.races();r.hasMoreElements();)
						{
							final Race R=r.nextElement();
							str.append("<OPTION VALUE=\""+R.ID()+"\" "+(R.ID().equalsIgnoreCase(httpVal)?"SELECTED":"")+">").append(R.name());
						}
						str.append(", ");
						break;
					}
					case MOBCOPY:
						str.append(httpVal).append(", ");
						break;
					case MOBRESIST:
						str.append(httpVal).append(", ");
						break;
					case PREFIX:
						str.append(httpVal).append(", ");
						break;
					case PROMOTIONS:
					{
						final List<Pair<String,String>> parsed = CMParms.parseCommaParenListLow(httpVal);
						if(httpReq.isUrlParameter(key+"_1"))
						{
							parsed.clear();
							int i=1;
							while(httpReq.isUrlParameter(key+"_"+i))
							{
								final String chg=httpReq.getUrlParameter(key+"_"+i);
								final String to=httpReq.getUrlParameter(key+"_V"+i);
								if(chg.length()>0)
									parsed.add(new Pair<String,String>(chg,to));
								i++;
							}
						}
						int i=1;
						for(final Pair<String,String> k : parsed)
						{
							if(parms.containsKey("EXISTS_"+i))
							{
								str.append("true").append(", ");
								break;
							}
							if(parms.containsKey(""+i))
								str.append(k.first).append(", ");
							if(parms.containsKey("V"+i))
								str.append(super.htmlOutgoingFilter(k.second)).append(", ");
							i++;
						}
						break;
					}
					case RECOVERRATE:
						str.append(httpVal).append(", ");
						break;
					case AEFFECT:
					case REFFECT:
					{
						final List<Pair<String,String>> parsed = CMParms.parseSpaceParenList(httpVal);
						if(httpReq.isUrlParameter(key+"_1"))
						{
							parsed.clear();
							int i=1;
							while(httpReq.isUrlParameter(key+"_"+i))
							{
								final String chg=httpReq.getUrlParameter(key+"_"+i);
								final String to=httpReq.getUrlParameter(key+"_V"+i);
								if(chg.length()>0)
									parsed.add(new Pair<String,String>(chg,to));
								i++;
							}
						}
						int i=1;
						for(final Pair<String,String> k : parsed)
						{
							if(parms.containsKey("EXISTS_"+i))
							{
								str.append("true").append(", ");
								break;
							}
							if(parms.containsKey(""+i))
							{
								str.append("<OPTION VALUE=\"\" >").append("Delete");
								for(final Enumeration<Ability> a=CMClass.abilities();a.hasMoreElements();)
								{
									final Ability A=a.nextElement();
									if((A.classificationCode()&Ability.ALL_DOMAINS)!=Ability.DOMAIN_ARCHON)
										str.append("<OPTION VALUE=\""+A.ID()+"\" "+(A.ID().equalsIgnoreCase(k.first)?"SELECTED":"")+">").append(A.ID());
								}
								for(final Enumeration<Behavior> b=CMClass.behaviors();b.hasMoreElements();)
								{
									final Behavior B=b.nextElement();
									str.append("<OPTION VALUE=\""+B.ID()+"\" "+(B.ID().equalsIgnoreCase(k.first)?"SELECTED":"")+">").append(B.ID());
								}
								str.append(", ");
							}
							if(parms.containsKey("V"+i))
								str.append(super.htmlOutgoingFilter(k.second)).append(", ");
							i++;
						}
						if(parms.containsKey(""+(parsed.size()+1)))
						{
							str.append("<OPTION VALUE=\"\" SELECTED>").append("Select");
							for(final Enumeration<Ability> a=CMClass.abilities();a.hasMoreElements();)
							{
								final Ability A=a.nextElement();
								if((A.classificationCode()&Ability.ALL_DOMAINS)!=Ability.DOMAIN_ARCHON)
									str.append("<OPTION VALUE=\""+A.ID()+"\" >").append(A.ID());
							}
							for(final Enumeration<Behavior> b=CMClass.behaviors();b.hasMoreElements();)
							{
								final Behavior B=b.nextElement();
								str.append("<OPTION VALUE=\""+B.ID()+"\" >").append(B.ID());
							}
							str.append(", ");
						}
						break;
					}
					case REQWEAPONS:
					{
						final List<String> selected = CMParms.parseSpaces(httpVal,true);
						if(httpReq.isUrlParameter(key))
						{
							selected.add(httpReq.getUrlParameter(key));
							for(int i=1;httpReq.isUrlParameter(key+i);i++)
								selected.add(httpReq.getUrlParameter(key+i));
						}
						final List<String> options = new ArrayList<String>();
						options.add("");
						options.add("magical");
						options.addAll(new ConvertingList<String,String>(Arrays.asList(Weapon.TYPE_DESCS),Converter.toLowerCase));
						options.addAll(new ConvertingList<String,String>(Arrays.asList(Weapon.CLASS_DESCS),Converter.toLowerCase));
						for(final String opt : options)
							str.append("<OPTION VALUE=\""+opt+"\" "+(CMParms.containsIgnoreCase(selected, opt)?"SELECTED":"")+">").append(opt);
						str.append(", ");
						break;
					}
					case ROOMADJS:
					{
						if(parms.containsKey("UP"))
						{
							if(httpReq.isUrlParameter(key+"_UP"))
								str.append("on".equalsIgnoreCase(httpReq.getUrlParameter(key+"_UP"))?"CHECKED":"");
							else
								str.append(CMParms.contains(httpVal,"UP")?"CHECKED":"");
						}
						else
						if(parms.containsKey("CHANCE"))
						{
							if(httpReq.isUrlParameter(key+"_CHANCE"))
								str.append(httpReq.getUrlParameter(key+"_CHANCE")).append(", ");
							else
							{
								String chance="";
								if(httpVal != null)
								{
									if(httpVal.startsWith("UP "))
										httpVal = httpVal.substring(3);
								}
								if(httpVal != null)
								{
									final int x=httpVal.indexOf(' ');
									if((x>0)&&(CMath.isInteger(httpVal.substring(0,x).trim())))
										chance=""+CMath.s_int(httpVal.substring(0,x).trim());
								}
								str.append(chance).append(", ");
							}
						}
						else
						{
							if(httpVal != null)
							{
								if(httpVal.startsWith("UP "))
									httpVal = httpVal.substring(3);
							}
							if(httpVal != null)
							{
								final int x=httpVal.indexOf(' ');
								if((x>0)&&(CMath.isInteger(httpVal.substring(0,x).trim())))
									httpVal = httpVal.substring(x).trim();
							}
							str.append(httpVal).append(", ");
						}
						break;
					}
					case ROOMCOLOR:
					{
						if(parms.containsKey("UP"))
						{
							if(httpReq.isUrlParameter(key+"_UP"))
								str.append("on".equalsIgnoreCase(httpReq.getUrlParameter(key+"_UP"))?"CHECKED":"");
							else
								str.append(CMParms.contains(httpVal,"UP")?"CHECKED":"");
						}
						else
						{
							str.append("<OPTION VALUE=\"\" "+((httpVal.length()==0)?"SELECTED":"")+">").append("None");
							for(final Color C : ColorLibrary.Color.values())
							{
								if((C.getCodeChar() != '0')&&(C.getCodeChar() != ' ')&&(C.getCodeChar() != '\0'))
									str.append("<OPTION VALUE=\"^"+C.getCodeChar()+"\" "+(CMParms.contains(httpVal,"^"+C.getCodeChar())?"SELECTED":"")+">").append(C.name());
							}
						}
						break;
					}
					case SETSTAT:
						str.append(httpVal).append(", ");
						break;
					case SPECFLAGS:
					{
						List<String> selected = new ArrayList<String>(2);
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
						else
							selected = CMParms.parseSpaces(httpVal, true);
						for(final PlanarAbility.PlanarSpecFlag flag : PlanarAbility.PlanarSpecFlag.values())
							str.append("<OPTION VALUE=\""+flag.toString()+"\" "+(selected.contains(flag.toString())?"SELECTED":"")+">").append(flag.toString());
						str.append(", ");
						break;
					}
					case TRANSITIONAL:
					{
						if(httpVal.length()==0)
							httpVal="false";
						for(final String k : new String[] {"true", "false"})
							str.append("<OPTION VALUE=\""+k+"\" "+(k.equalsIgnoreCase(httpVal)?"SELECTED":"")+">").append(k);
						str.append(", ");
						break;
					}
					case WEAPONMAXRANGE:
						str.append(httpVal).append(", ");
						break;
					default:
						break;

					}
				}
			}
		}
		String strstr=str.toString();
		if(strstr.endsWith(", "))
			strstr=strstr.substring(0,strstr.length()-2);
		return clearWebMacros(strstr);
	}
}
