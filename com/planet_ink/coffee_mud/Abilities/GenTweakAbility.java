package com.planet_ink.coffee_mud.Abilities;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMClass.CMObjectType;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;


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
public class GenTweakAbility extends StdAbility implements InvocationHandler
{

	private String ID="GenTweakAbility";
	private Ability originalA = null;
	private static final Map<String,Map<String, Object>> overrides = new Hashtable<String,Map<String,Object>>();

	@Override
	public String ID()
	{
		return ID;
	}

	public GenTweakAbility()
	{
		super();
	}

	public GenTweakAbility(final String originalID, final String newID)
	{
		super();
		if (!overrides.containsKey(newID))
			overrides.put(newID, new Hashtable<String, Object>());
		if (!overrides.get(newID).containsKey("originalID"))
			overrides.get(newID).put("originalID", originalID);
		this.ID = newID;
	}

	public Ability getDelegate()
	{
		if(overrides.containsKey(ID) && overrides.get(ID).containsKey("originalID"))
		{
			final String originalID = overrides.get(ID).get("originalID").toString();
			if(originalA == null)
				originalA = CMClass.getAbility(originalID);
			else
			if(!originalA.ID().equalsIgnoreCase(originalID))
				originalA = CMClass.getAbility(originalID);
			if (originalA == null)
			{
				Log.errOut(ID, "Failed to load original ability: " + originalID);
				originalA = this;
			}
		}
		return originalA;
	}

	public void overrideMethod(final String methodName, final Object returnValue)
	{
		if (!overrides.containsKey(ID))
			overrides.put(ID, new Hashtable<String, Object>());
		overrides.get(ID).put(methodName, returnValue);
	}

	public void removeOverrideMethod(final String methodName)
	{
		if (overrides.containsKey(ID))
			overrides.get(ID).remove(methodName);
	}

	@Override
	public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable
	{
		if(overrides.containsKey(ID))
		{
			final String methodName = method.getName();
			if(overrides.get(ID).containsKey(methodName))
			{
				final Object o = overrides.get(ID).get(methodName);
				if(methodName.equals("maxRange"))
					return Integer.valueOf(adjustedMaxInvokerRange(((Integer)o).intValue()));
				return o;
			}
			if(methodName.equals("newInstance") && overrides.get(ID).containsKey("originalID"))
				return GenTweakAbility.createProxy(overrides.get(ID).get("originalID").toString(), ID);
		}
		return method.invoke(getDelegate(), args);
	}

	@SuppressWarnings("unchecked")
	public static <T extends Ability> T createProxy(final String originalID, final String newID)
	{
		return (T) Proxy.newProxyInstance(Ability.class.getClassLoader(),
										  new Class<?>[]{Ability.class},
										  new GenTweakAbility(originalID, newID));
	}

	@Override
	public String L(String str, final String ... xs)
	{
		if (overrides.containsKey(ID) && overrides.get(ID).containsKey("STRINGS"))
		{
			@SuppressWarnings("unchecked")
			final Map<String,String> strings = (Map<String,String>)overrides.get(ID).get("STRINGS");
			if(strings.containsKey(str))
				str =strings.get(str);
			else
			{
				for (final String key : strings.keySet())
					str = CMStrings.replaceAll(str, key, strings.get(key));
			}
		}
		return CMLib.lang().fullSessionTranslation(getClass(), str, xs);
	}

	@Override
	public CMObject newInstance()
	{
		try
		{
			final GenTweakAbility A = this.getClass().getDeclaredConstructor().newInstance();
			A.ID=ID;
			A.originalA=null;
			return A;
		}
		catch(final Exception e)
		{
			Log.errOut(ID(),e);
		}
		return this;
	}

	@Override
	protected void cloneFix(final Ability E)
	{
	}

	@Override
	public boolean isGeneric()
	{
		return true;
	}

	// lots of work to be done here
	@Override
	public int getSaveStatIndex()
	{
		return getStatCodes().length;
	}

	private static final String[] CODES={"CLASS",//0
										 "TEXT",//1
										 "NAME",//2S
										 "DISPLAY",//3S
										 "TRIGSTR",//4S[]
										 "MAXRANGE",//5I
										 "MINRANGE",//6I
										 "AUTOINVOKE",//7B
										 "FLAGS",//8I
										 "CLASSIFICATION",//9I
										 "OVERRIDEMANA",//10I
										 "USAGEMASK",//11I
										 "QUALITY",//12I
										 "TICKSBETWEENCASTS",//13I
										 "HELP",//14I
										 "MAYENCHANT",//15I
										 "ORIGINAL",//16S
										 "STRING",//17MAP
										};

	@Override
	public String[] getStatCodes()
	{
		return CODES;
	}

	@Override
	protected int getCodeNum(final String code)
	{
		for(int i=0;i<CODES.length;i++)
		{
			if(code.equalsIgnoreCase(CODES[i]))
				return i;
		}
		return -1;
	}

	private Object getLocalOverride(final String methodName)
	{
		if(overrides.containsKey(ID))
			return overrides.get(ID).get(methodName);
		return null;
	}

	@Override
	public String getStat(String code)
	{
		int num=0;
		int numDex=code.length();
		while((numDex>0)&&(Character.isDigit(code.charAt(numDex-1))))
			numDex--;
		if(numDex<code.length())
		{
			num=CMath.s_int(code.substring(numDex));
			code=code.substring(0,numDex);
		}
		switch(getCodeNum(code))
		{
		case 0:
			return ID();
		case 1:
			return getDelegate().text();
		case 2:
			return getLocalOverride("Name") == null ? "" :
				getLocalOverride("Name").toString();
		case 3:
			return getLocalOverride("displayText") == null ? "" :
				getLocalOverride("displayText").toString().length()==0?"null":getLocalOverride("displayText").toString();
		case 4:
			return getLocalOverride("triggerStrings") == null ? "" :
				CMParms.toListString((String[])getLocalOverride("triggerStrings"));
		case 5:
			return getLocalOverride("maxRange") == null ? "" :
				convert(Ability.RANGE_CHOICES, ((Integer) getLocalOverride("maxRange")).intValue(), false);
		case 6:
			return getLocalOverride("minRange") == null ? "" :
				convert(Ability.RANGE_CHOICES, ((Integer)getLocalOverride("minRange")).intValue(), false);
		case 7:
			return getLocalOverride("isAutoInvoked") == null ? "" :
				((Boolean) getLocalOverride("isAutoInvoked")).toString();
		case 8:
			return getLocalOverride("flags") == null ? "" :
				convert(Ability.FLAG_DESCS, ((Long) getLocalOverride("flags")).longValue(), true);
		case 9:
			return getLocalOverride("classificationCode") == null ? "" :
				convertClassAndDomain(((Integer) getLocalOverride("classificationCode")).intValue());
		case 10:
			return getLocalOverride("overrideMana") == null ? "" :
				((Integer) getLocalOverride("overrideMana")).toString();
		case 11:
			return getLocalOverride("usageType") == null ? "" :
				convert(Ability.USAGE_DESCS, ((Integer)getLocalOverride("usageType")).intValue(), true);
		case 12:
			return getLocalOverride("abstractQuality") == null ? "" :
				convert(Ability.QUALITY_DESCS, ((Integer) getLocalOverride("abstractQuality")).intValue(), false);
		case 13:
			return getLocalOverride("getTicksBetweenCasts") == null ? "" :
				((Integer) getLocalOverride("getTicksBetweenCasts")).toString();
		case 14:
			return getLocalOverride("HELP") == null ? "" : getLocalOverride("HELP").toString();
		case 15:
			return getLocalOverride("mayBeEnchanted") == null ? "" :
				((Boolean) getLocalOverride("mayBeEnchanted")).toString();
		case 16:
			return getLocalOverride("originalID") == null ? "GenTweakAbility" :
				getLocalOverride("originalID").toString();
		case 17:
			if(overrides.containsKey(ID) && overrides.get(ID).containsKey("STRINGS"))
			{
				@SuppressWarnings("unchecked")
				final Map<String,String> strings = (Map<String,String>)overrides.get(ID).get("STRINGS");
				if(num == 0)
					return CMParms.combine(new ArrayList<String>(strings.keySet()),'\n');
				else
				{
					int dex = 1;
					for (final String key : strings.keySet())
					{
						if (dex == num)
							return strings.get(key);
						dex++;
					}
				}
			}
			return "";
		default:
			if (code.equalsIgnoreCase("javaclass"))
				return "GenTweakAbility";
			else
			if (code.equalsIgnoreCase("allxml"))
				return getAllXML();
			else
			{
				final Ability delegate = getDelegate();
				if(delegate == this)
					return super.getStat(code);
				return delegate.getStat(code);
			}
		}
	}

	@Override
	public void setStat(String code, final String val)
	{
		int num=0;
		int numDex=code.length();
		while((numDex>0)&&(Character.isDigit(code.charAt(numDex-1))))
			numDex--;
		if(numDex<code.length())
		{
			num=CMath.s_int(code.substring(numDex));
			code=code.substring(0,numDex);
		}
		switch(getCodeNum(code))
		{
		case 0:
			if (val.trim().length() > 0)
			{
				if (num != 9)
					CMClass.delClass(CMObjectType.ABILITY, this);
				if(overrides.containsKey(ID))
					overrides.put(val, overrides.get(ID));
				overrides.remove(ID);
				ID = val;
				if (num != 9)
					CMClass.addClass(CMObjectType.ABILITY, this);
			}
			break;
		case 1:
			getDelegate().setMiscText(val);
			break;
		case 2:
			if(val.length()==0)
				this.removeOverrideMethod("Name");
			else
				this.overrideMethod("Name", val);
			break;
		case 3:
			if(val.length()==0)
				this.removeOverrideMethod("displayText");
			else
			if(val.equalsIgnoreCase("null"))
				this.overrideMethod("displayText", "");
			else
				this.overrideMethod("displayText", val);
			break;
		case 4:
			if(val.length()==0)
				this.removeOverrideMethod("triggerStrings");
			else
				this.overrideMethod("triggerStrings", CMParms.toStringArray(CMParms.parseCommas(val.toUpperCase(), true)));
			break;
		case 5:
			if(val.length()==0)
				this.removeOverrideMethod("maxRange");
			else
				this.overrideMethod("maxRange", Integer.valueOf((int)convert(Ability.RANGE_CHOICES, val, false)));
			break;
		case 6:
			if(val.length()==0)
				this.removeOverrideMethod("minRange");
			else
				this.overrideMethod("minRange", Integer.valueOf((int)convert(Ability.RANGE_CHOICES, val, false)));
			break;
		case 7:
			if(val.length()==0)
				this.removeOverrideMethod("isAutoInvoked");
			else
				this.overrideMethod("isAutoInvoked", Boolean.valueOf(CMath.s_bool(val)));
			break;
		case 8:
			if(val.length()==0)
				this.removeOverrideMethod("flags");
			else
				this.overrideMethod("flags", Long.valueOf(convert(Ability.FLAG_DESCS, val, true)));
			break;
		case 9:
			if(val.length()==0)
				this.removeOverrideMethod("classificationCode");
			else
				this.overrideMethod("classificationCode", Integer.valueOf(convertClassAndDomain(val)));
			break;
		case 10:
			if(val.length()==0)
				this.removeOverrideMethod("overrideMana");
			else
				this.overrideMethod("overrideMana", Integer.valueOf(CMath.s_parseIntExpression(val)));
			getHardOverrideManaCache().remove(ID());
			break;
		case 11:
			if(val.length()==0)
				this.removeOverrideMethod("usageType");
			else
				this.overrideMethod("usageType", Integer.valueOf((int)convert(Ability.USAGE_DESCS, val, true)));
			break;
		case 12:
			if(val.length()==0)
				this.removeOverrideMethod("abstractQuality");
			else
				this.overrideMethod("abstractQuality", Integer.valueOf((int)convert(Ability.QUALITY_DESCS, val, false)));
			break;
		case 13:
			if(val.length()==0)
				this.removeOverrideMethod("getTicksBetweenCasts");
			else
				this.overrideMethod("getTicksBetweenCasts", Integer.valueOf(CMath.s_parseIntExpression(val)));
			break;
		case 14:
			if(val.length()==0)
				this.removeOverrideMethod("HELP");
			else
				this.overrideMethod("HELP", val);
			break;
		case 15:
			if(val.length()==0)
				this.removeOverrideMethod("mayBeEnchanted");
			else
				this.overrideMethod("mayBeEnchanted", Boolean.valueOf(CMath.s_bool(val)));
			break;
		case 16:
			if((val.length()>0)&&(!val.equals(getLocalOverride("originalID"))))
			{
				this.overrideMethod("originalID", val);
				this.originalA = null;
			}
			break;
		case 17:
			if((val.length()==0)&&(overrides.containsKey(ID)))
			{
				if(num==0)
					overrides.get(ID).remove("STRINGS");
				else
				{
					if (overrides.get(ID).containsKey("STRINGS"))
					{
						@SuppressWarnings("unchecked")
						final Map<String, String> strings=(Map<String, String>)overrides.get(ID).get("STRINGS");
						int dex = 1;
						for(final String key : strings.keySet())
						{
							if(dex == num)
							{
								strings.remove(key);
								break;
							}
							dex++;
						}
						if (strings.size() == 0)
							overrides.get(ID).remove("STRINGS");
					}
				}
			}
			else
			if((val.length()>0)&&(overrides.containsKey(ID)))
			{
				if (!overrides.get(ID).containsKey("STRINGS"))
					overrides.get(ID).put("STRINGS", new Hashtable<String, String>());
				@SuppressWarnings("unchecked")
				final Map<String, String> strings=(Map<String, String>)overrides.get(ID).get("STRINGS");
				if(num == 0)
					strings.put(val, val);
				else
				{
					int dex = 1;
					for(final String key : strings.keySet())
					{
						if(dex == num)
						{
							strings.put(key, val);
							break;
						}
						dex++;
					}
				}
			}
			break;
		default:
			if (code.equalsIgnoreCase("allxml") && ID.equalsIgnoreCase("GenTweakAbility"))
				parseAllXML(val);
			else
				super.setStat(code, val);
			break;
		}
	}

	private String convert(final String[] options, final long val, final boolean mask)
	{
		if(mask)
		{
			final StringBuffer str=new StringBuffer("");
			for(int i=0;i<options.length;i++)
			{
				if((val&(1L<<i))>0)
					str.append(options[i]+",");
			}
			if(str.length()>0)
			{
				String sstr=str.toString();
				if(sstr.endsWith(","))
					sstr=sstr.substring(0,sstr.length()-1);
				return sstr;
			}
		}
		else
		if((val>=0)&&(val<options.length))
			return options[(int)val];
		return ""+val;
	}

	private int convertClassAndDomain(String val)
	{
		if(CMath.isInteger(val))
			return CMath.s_int(val);
		int dom=0;
		int acod=Ability.ACODE_SKILL;
		final List<String> V=CMParms.parseCommas(val,true);
		for(int v=0;v<V.size();v++)
		{
			val=V.get(v);
			int tacod=-1;
			for(int a=0;a<Ability.ACODE.DESCS.size();a++)
			{
				if(val.equalsIgnoreCase(Ability.ACODE.DESCS.get(a)))
					tacod=a;
			}
			if(tacod<0)
			{
				for(int i=0;i<Ability.ACODE.DESCS.size();i++)
				{
					if(Ability.ACODE.DESCS.get(i).toUpperCase().startsWith(val.toUpperCase()))
						tacod=i;
				}
				if(tacod<0)
				{
					int tdom=-1;
					for(int a=0;a<Ability.DOMAIN.DESCS.size();a++)
					{
						if(val.equalsIgnoreCase(Ability.DOMAIN.DESCS.get(a)))
							tdom=a<<5;
					}
					if(tdom<0)
					{
						for(int i=0;i<Ability.DOMAIN.DESCS.size();i++)
						{
							if(Ability.DOMAIN.DESCS.get(i).toUpperCase().startsWith(val.toUpperCase())
									||Ability.DOMAIN.DESCS.get(i).toUpperCase().endsWith(val.toUpperCase()))
							{
								tdom = i << 5;
								break;
							}
						}
					}
					if(tdom>=0)
						dom=tdom;
				}
			}
			else
				acod=tacod;
		}
		return acod|dom;
	}

	private String convertClassAndDomain(final int val)
	{
		final int dom=(val&Ability.ALL_DOMAINS)>>5;
		final int acod=val&Ability.ALL_ACODES;
		if((acod>=0)&&(acod<Ability.ACODE.DESCS.size())
		&&(dom>=0)&&(dom<Ability.DOMAIN.DESCS.size()))
			return Ability.ACODE.DESCS.get(acod)+","+Ability.DOMAIN.DESCS.get(dom);
		return ""+val;
	}

	private long convert(final String[] options, final String val, final boolean mask)
	{
		if(CMath.isLong(val))
			return CMath.s_long(val);
		for(int i=0;i<options.length;i++)
		{
			if(val.equalsIgnoreCase(options[i]))
				return mask?(1L<<i):i;
		}
		if(val.length()>0)
		{
			for(int i=0;i<options.length;i++)
			{
				if(options[i].toUpperCase().startsWith(val.toUpperCase()))
					return mask?(1L<<i):i;
			}
		}
		if(mask)
		{
			final List<String> V=CMParms.parseCommas(val,true);
			long num=0;
			for(int v=0;v<V.size();v++)
				num=num|(1L<<convert(options,V.get(v),false));
			return num;
		}
		return 0;
	}

	@Override
	public boolean sameAs(final Environmental E)
	{
		if(!(E instanceof GenTweakAbility))
			return false;
		if(!((GenTweakAbility)E).ID().equals(ID))
			return false;
		if(!((GenTweakAbility)E).getDelegate().text().equals(this.getDelegate().text()))
			return false;
		return true;
	}

	private void parseAllXML(final String xml)
	{
		final List<XMLLibrary.XMLTag> V=CMLib.xml().parseAllXML(xml);
		if((V==null)||(V.size()==0))
			return;
		for(int c=0;c<getStatCodes().length;c++)
		{
			final String statCode = getStatCodes()[c];
			final String value = CMLib.xml().getValFromPieces(V, statCode);
			if(statCode.equals("CLASS"))
				ID=CMLib.xml().restoreAngleBrackets(value);
			else
			if(!statCode.equals("TEXT"))
				setStat(statCode,CMLib.xml().restoreAngleBrackets(value));
		}
	}

	private String getAllXML()
	{
		final StringBuffer str=new StringBuffer("");
		for(int c=0;c<getStatCodes().length;c++)
		{
			if(!getStatCodes()[c].equals("TEXT"))
			{
				str.append("<"+getStatCodes()[c]+">"
						  +CMLib.xml().parseOutAngleBrackets(getStat(getStatCodes()[c]))
						  +"</"+getStatCodes()[c]+">");
			}
		}
		return str.toString();
	}
}
