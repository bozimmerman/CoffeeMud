package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.ShopKeeper.ViewType;
import com.planet_ink.coffee_mud.core.exceptions.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMClass.CMObjectType;
import com.planet_ink.coffee_mud.core.CMProps.ListFile;
import com.planet_ink.coffee_mud.core.CMProps.Str;
import com.planet_ink.coffee_mud.core.CMSecurity.SecGroup;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityMapper.SecretFlag;
import com.planet_ink.coffee_mud.Libraries.interfaces.GenericEditor.CMEval;
import com.planet_ink.coffee_mud.Libraries.interfaces.ListingLibrary.ListStringer;
import com.planet_ink.coffee_mud.Libraries.interfaces.MoneyLibrary.MoneyDenomination;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.PlanarAbility.PlanarVar;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.AbilityComponent.CompConnector;
import com.planet_ink.coffee_mud.Common.interfaces.Clan.MemberRecord;
import com.planet_ink.coffee_mud.Common.interfaces.PlayerAccount.AccountFlag;
import com.planet_ink.coffee_mud.Common.interfaces.PlayerStats.PlayerFlag;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.ClanItem.ClanItemType;
import com.planet_ink.coffee_mud.Items.interfaces.MusicalInstrument.InstrumentType;
import com.planet_ink.coffee_mud.Items.interfaces.ShipDirectional.ShipDir;
import com.planet_ink.coffee_mud.Items.interfaces.Technical.TechType;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.*;

/*
   Copyright 2008-2024 Bo Zimmerman

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
public class CMGenEditor extends StdLibrary implements GenericEditor
{
	@Override
	public String ID()
	{
		return "CMGenEditor";
	}

	private final long maxLength=Long.MAX_VALUE;
	// showNumber should always be a valid number no less than 1
	// showFlag should be a valid number for editing, or -1 for skipping

	private static CMEval CMEVAL_INSTANCE = new CMEval()
	{
		@Override
		public Object eval(final Object val, final Object[] choices, final boolean emptyOK) throws CMException
		{
			if(choices.length==0)
				return "";
			final String str=val.toString().trim();
			for(final Object o : choices)
			{
				if(str.equalsIgnoreCase(o.toString()))
					return o.toString();
			}
			throw new CMException("That was not one of your choices.");
		}
	};

	@Override
	public Collection<? extends Object> promptEnumChoices(final MOB mob, final Collection<? extends Object> flags, final Object[] values, final int showNumber, final int showFlag, final String fieldDisplayStr) throws IOException
	{
		final String help=CMParms.toListString(values);
		final String oldVal = CMParms.toListString(flags.toArray());
		final String newVal = CMLib.genEd().prompt(mob, oldVal, showNumber, showFlag, fieldDisplayStr, true, help);
		String[] newVals;
		if(newVal.indexOf(',')>0)
			newVals = CMParms.parseCommas(newVal.toUpperCase().trim(), true).toArray(new String[0]);
		else
		if(newVal.indexOf(';')>0)
			newVals = CMParms.parseSemicolons(newVal.toUpperCase().trim(), true).toArray(new String[0]);
		else
			newVals = CMParms.parse(newVal.toUpperCase().trim()).toArray(new String[0]);
		final Collection<Object> newFlags = new ArrayList<Object>();
		final List<? extends Object> lst = Arrays.asList(values);
		for(int i=0;i<newVals.length;i++)
		{
			final int index=CMParms.indexOfIgnoreCase(lst, newVals[i]);
			if(index>=0)
				newFlags.add(values[index]);
		}
		return newFlags;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enum<? extends Enum> promptEnumChoice(final MOB mob, final Enum<? extends Enum> val, final Enum<? extends Enum>[] cs, final int showNumber, final int showFlag, final String fieldDisplayStr) throws IOException
	{
		final String help=CMParms.toListString(cs);
		final String oldVal = val.name();
		while((mob.session()!=null)&&(!mob.session().isStopped()))
		{
			final String newVal = CMLib.genEd().prompt(mob, oldVal, showNumber, showFlag, fieldDisplayStr, false, help);
			if(newVal.equalsIgnoreCase(oldVal)||(newVal==null)||(newVal.length()==0))
				return val;
			final Enum<? extends Enum> newEnum= CMath.s_valueOf(val.getClass(), newVal.toUpperCase().trim());
			if(newEnum != null)
				return newEnum;
			mob.tell(L("@x1 is not a proper value, try '@x2'.",newVal,help));
		}
		return val;
	}

	@Override
	public void promptStatDouble(final MOB mob, final Modifiable E, final int showNumber, final int showFlag, final String fieldDisplayStr, final String field) throws IOException
	{
		promptStatDouble(mob, E, null, showNumber, showFlag, fieldDisplayStr, field);
	}

	@Override
	public void promptStatInt(final MOB mob, final Modifiable E, final int showNumber, final int showFlag, final String fieldDisplayStr, final String field) throws IOException
	{
		promptStatInt(mob, E, null, showNumber, showFlag, fieldDisplayStr, field);
	}

	@Override
	public void promptStatInt(final MOB mob, final Modifiable E, final String help, final int showNumber, final int showFlag, final String fieldDisplayStr, final String field) throws IOException
	{
		E.setStat(field, "" + prompt(mob, CMath.s_long(E.getStat(field)), showNumber, showFlag, fieldDisplayStr, help));
	}

	@Override
	public void promptStatDouble(final MOB mob, final Modifiable E, final String help, final int showNumber, final int showFlag, final String fieldDisplayStr, final String field) throws IOException
	{
		E.setStat(field, "" + prompt(mob, CMath.s_double(E.getStat(field)), showNumber, showFlag, fieldDisplayStr, help));
	}

	@Override
	public void promptStatBool(final MOB mob, final Modifiable E, final int showNumber, final int showFlag, final String fieldDisplayStr, final String field) throws IOException
	{
		promptStatBool(mob, E, null, showNumber, showFlag, fieldDisplayStr, field);
	}

	@Override
	public void promptStatBool(final MOB mob, final Modifiable E, final String help, final int showNumber, final int showFlag, final String fieldDisplayStr, final String field) throws IOException
	{
		E.setStat(field, "" + prompt(mob, CMath.s_bool(E.getStat(field)), showNumber, showFlag, fieldDisplayStr, help));
	}

	@Override
	public void promptStatStr(final MOB mob, final Modifiable E, final int showNumber, final int showFlag, final String fieldDisplayStr, final String field) throws IOException
	{
		promptStatStr(mob, E, null, showNumber, showFlag, fieldDisplayStr, field, true);
	}

	@Override
	public void promptStatStr(final MOB mob, final Modifiable E, final String help, final int showNumber, final int showFlag, final String fieldDisplayStr, final String field, final boolean emptyOK) throws IOException
	{
		E.setStat(field, prompt(mob, E.getStat(field), showNumber, showFlag, fieldDisplayStr, emptyOK, false, help, null, null));
	}

	public void promptRawStatStr(final MOB mob, final Modifiable E, final String help, final int showNumber, final int showFlag, final String fieldDisplayStr, final String field, final boolean emptyOK) throws IOException
	{
		E.setStat(field, prompt(mob, E.getStat(field), showNumber, showFlag, fieldDisplayStr, emptyOK, true, help, null, null));
	}

	public void promptStatStr(final MOB mob, final Modifiable E, final String help, final int showNumber, final int showFlag, final String fieldDisplayStr, final String field, final int maxChars) throws IOException
	{
		E.setStat(field, prompt(mob, E.getStat(field), showNumber, showFlag, fieldDisplayStr, false, false, maxChars, help, null, null));
	}

	@Override
	public void promptStatChoices(final MOB mob, final Modifiable E, final String help, final int showNumber, final int showFlag, final String fieldDisplayStr, final String field, final Object[] choices) throws IOException
	{
		final boolean emptyOk = choices != null && choices.length>1 && choices[0] != null && choices[0].toString().equals("");
		E.setStat(field, prompt(mob, E.getStat(field), showNumber, showFlag, fieldDisplayStr, emptyOk, false, help, CMEVAL_INSTANCE, choices));
	}

	@Override
	public void promptStatCommaChoices(final MOB mob, final Modifiable E, final String help, final int showNumber, final int showFlag, final String fieldDisplayStr, final String field, final Object[] choices) throws IOException
	{
		E.setStat(field, this.promptCommaList(mob, E.getStat(field), showNumber, showFlag, fieldDisplayStr, help, CMEVAL_INSTANCE, choices));
	}

	@Override
	public String prompt(final MOB mob, final String oldVal, final int showNumber, final int showFlag, final String fieldDisplayStr) throws IOException
	{
		return prompt(mob, oldVal, showNumber, showFlag, fieldDisplayStr, false, false, null, null, null);
	}

	@Override
	public String prompt(final MOB mob, final String oldVal, final int showNumber, final int showFlag, final String fieldDisplayStr, final String help) throws IOException
	{
		return prompt(mob, oldVal, showNumber, showFlag, fieldDisplayStr, false, false, help, null, null);
	}

	@Override
	public String prompt(final MOB mob, final String oldVal, final int showNumber, final int showFlag, final String fieldDisplayStr, final boolean emptyOK) throws IOException
	{
		return prompt(mob, oldVal, showNumber, showFlag, fieldDisplayStr, emptyOK, false, null, null, null);
	}

	@Override
	public String prompt(final MOB mob, final String oldVal, final int showNumber, final int showFlag, final String fieldDisplayStr, final boolean emptyOK, final String help) throws IOException
	{
		return prompt(mob, oldVal, showNumber, showFlag, fieldDisplayStr, emptyOK, false, help);
	}

	@Override
	public String prompt(final MOB mob, final String oldVal, final int showNumber, final int showFlag, final String fieldDisplayStr, final boolean emptyOK, final boolean rawPrint) throws IOException
	{
		return prompt(mob, oldVal, showNumber, showFlag, fieldDisplayStr, emptyOK, rawPrint, null, null, null);
	}

	@Override
	public String prompt(final MOB mob, final String oldVal, final int showNumber, final int showFlag, final String fieldDisplayStr, final boolean emptyOK, final boolean rawPrint, final String help) throws IOException
	{
		return prompt(mob, oldVal, showNumber, showFlag, fieldDisplayStr, emptyOK, rawPrint, help, null, null);
	}

	@Override
	public boolean prompt(final MOB mob, final boolean oldVal, final int showNumber, final int showFlag, final String fieldDisplayStr) throws IOException
	{
		return prompt(mob, oldVal, showNumber, showFlag, fieldDisplayStr, null);
	}

	@Override
	public double prompt(final MOB mob, final double oldVal, final int showNumber, final int showFlag, final String fieldDisplayStr) throws IOException
	{
		return prompt(mob, oldVal, showNumber, showFlag, fieldDisplayStr, null);
	}

	@Override
	public int prompt(final MOB mob, final int oldVal, final int showNumber, final int showFlag, final String fieldDisplayStr) throws IOException
	{
		return prompt(mob, oldVal, showNumber, showFlag, fieldDisplayStr, null);
	}

	@Override
	public long prompt(final MOB mob, final long oldVal, final int showNumber, final int showFlag, final String fieldDisplayStr) throws IOException
	{
		return prompt(mob, oldVal, showNumber, showFlag, fieldDisplayStr, null);
	}

	@Override
	public boolean promptToggle(final MOB mob, final int showNumber, final int showFlag, final String fieldDisplayStr)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return false;
		mob.tell(showNumber+". "+fieldDisplayStr);
		if((showFlag!=showNumber)&&(showFlag>-999))
			return false;
		if(showFlag!=showNumber)
			return mob.session().confirm(L("Toggle (y/N)?"),"N");
		return true;
	}

	protected String promptCommaList(final MOB mob,
									 final String oldVal,
									 final int showNumber,
									 final int showFlag,
									 final String fieldDisplayStr,
									 final String secondDisplayStr,
									 final String help,
									 final CMEval eval,
									 final Object[] choices) throws IOException
	{
		return promptDelimitedList(mob,oldVal,showNumber,showFlag,fieldDisplayStr,secondDisplayStr,',',help,eval,choices);
	}

	protected String promptCommaList(final MOB mob,
									 final String oldVal,
									 final int showNumber,
									 final int showFlag,
									 final String fieldDisplayStr,
									 final String help,
									 final CMEval eval,
									 final Object[] choices) throws IOException
	{
		return promptDelimitedList(mob,oldVal,showNumber,showFlag,fieldDisplayStr,null,',',help,eval,choices);
	}

	protected String promptDelimitedList(final MOB mob,
										 String oldVal,
										 final int showNumber,
										 final int showFlag,
										 final String fieldDisplayStr,
										 final String secondDisplayStr,
										 final char delimiter,
										 final String help,
										 final CMEval eval,
										 final Object[] choices) throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return oldVal;
		mob.tell(showNumber+". "+fieldDisplayStr+": '"+oldVal+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999))
			return oldVal;
		String fieldDisplayStr2=secondDisplayStr;
		String prefix2=null;
		String suffix2=null;
		if((fieldDisplayStr2!=null)&&(fieldDisplayStr2.length()>0))
		{
			prefix2="";
			suffix2="";
			while((fieldDisplayStr2.length()>0)
			&&(!Character.isLetterOrDigit(fieldDisplayStr2.charAt(0))))
			{
				prefix2+= fieldDisplayStr2.charAt(0);
				fieldDisplayStr2 = fieldDisplayStr2.substring(1);
			}
			while((fieldDisplayStr2.length()>0)
			&&(!Character.isLetterOrDigit(fieldDisplayStr2.charAt(fieldDisplayStr2.length()-1))))
			{
				suffix2 = fieldDisplayStr2.charAt(fieldDisplayStr2.length()-1) + suffix2;
				fieldDisplayStr2 = fieldDisplayStr2.substring(0,fieldDisplayStr2.length()-1);
			}
		}
		String newName="?";
		final String promptStr=L("Enter a value to add/remove@x1\n\r:",(help!=null?" (?)":""));
		final String oldOldVal=oldVal;
		while((mob.session()!=null)&&(!mob.session().isStopped()))
		{
			newName=mob.session().prompt(promptStr,"");
			if(newName.equals("?")&&(help!=null))
				mob.tell(help);
			else
			if(newName.trim().length()==0)
			{
				if(oldVal.equals(oldOldVal))
					mob.tell(L("(no change)"));
				return oldVal;
			}
			else
			if(newName.equalsIgnoreCase("null"))
				oldVal="";
			else
			{
				if(eval!=null)
				{
					try
					{
						final Object value=eval.eval(newName,choices,false);
						if(value instanceof String)
							newName=(String)value;
					}
					catch(final CMException e)
					{
						mob.tell(e.getMessage());
						continue;
					}
				}
				final List<String> curSet;
				if((prefix2!=null)&&prefix2.equals("("))
				{
					curSet = new ArrayList<String>();
					int parDepth = 0;
					int start=0;
					for(int o=0;o<oldVal.length();o++)
					{
						if((oldVal.charAt(o)==delimiter)
						&&(parDepth == 0))
						{
							final String val = oldVal.substring(start,o).trim();
							if(val.length()>0)
								curSet.add(val);
							start=o+1;
						}
						else
						if(oldVal.charAt(o)=='(')
							parDepth++;
						else
						if(oldVal.charAt(o)==')')
							parDepth--;
					}
					if(start<oldVal.length())
					{
						final String val = oldVal.substring(start).trim();
						if(val.length()>0)
							curSet.add(val);
					}
				}
				else
					curSet=CMParms.parseAny(oldVal,delimiter,true);
				String oldOne=null;
				for(final String c : curSet)
				{
					if(c.equalsIgnoreCase(newName))
						oldOne=c;
				}
				if((oldOne == null) && (prefix2 != null))
				{
					for(final String c : curSet)
					{
						if(c.toLowerCase().startsWith((newName+prefix2).toLowerCase()))
							oldOne=c;
					}
				}
				if(oldOne!=null)
				{
					curSet.remove(oldOne);
					mob.tell(L("'@x1' removed.",oldOne));
				}
				else
				{
					if((fieldDisplayStr2!=null)&&(fieldDisplayStr2.length()>0))
					{
						final String val=mob.session().prompt("\n\r"+fieldDisplayStr2+": ");
						newName=newName+prefix2+val+suffix2;
					}
					curSet.add(newName);
					mob.tell(L("'@x1' added.",newName));
				}
				oldVal=CMParms.combineWith(curSet, delimiter);
			}
		}
		mob.tell(L("(no change)"));
		return oldVal;
	}

	@Override
	public String prompt(final MOB mob,
						final String oldVal,
						final int showNumber,
						final int showFlag,
						final String fieldDisplayStr,
						final boolean emptyOK,
						final boolean rawPrint,
						final String help,
						final CMEval eval,
						final Object[] choices)
	throws IOException
	{
		return prompt(mob,oldVal,showNumber,showFlag,fieldDisplayStr,emptyOK,rawPrint,0,help,eval,choices);
	}

	@Override
	public String prompt(final MOB mob,
						 final String oldVal,
						 final int showNumber,
						 final int showFlag,
						 final String fieldDisp,
						 final boolean emptyOK,
						 final boolean rawPrint,
						 final int maxChars,
						 final String help,
						 final CMEval eval,
						 final Object[] choices)
	throws IOException
	{
		if((mob==null)||(mob.session() == null))
			return oldVal;
		final Session sess=mob.session();
		if((showFlag>0)&&(showFlag!=showNumber))
			return oldVal;
		String showVal=oldVal;
		if((maxChars > 0)&&(showVal.length()>maxChars)&& (!((showFlag!=showNumber)&&(showFlag>-999))))
			showVal=showVal.substring(0,maxChars)+"...";
		final String numStr = (showNumber == 0)?"   ":(showNumber+". ");
		if(rawPrint)
		{
			if((showFlag==showNumber)||(showFlag<=-999))
			{
				sess.sendGMCPEvent("Siplet.Input", "{\"title\":\""+MiniJSON.toJSONString(fieldDisp)+"\",\"text\":\""+MiniJSON.toJSONString(oldVal)+"\"}");
				sess.safeRawPrintln(numStr+fieldDisp+": '"+showVal+"'.");
			}
			else
				sess.safeRawPrintln(numStr+fieldDisp+": '"+CMStrings.ellipse(showVal,60)+"'.");
		}
		else
			mob.tell(numStr+fieldDisp+": '"+showVal+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999))
			return oldVal;
		String newName="?";
		final boolean mcp =sess.isAllowedMcp("dns-org-mud-moo-simpleedit", (float)1.0);
		final String promptStr=L("Enter a new value@x1@x2@x3\n\r:",(emptyOK?" (or NULL)":""),(mcp?" (or \\#$#)":""),(help!=null?" (?)":""));
		while(newName.equals("?")&&(mob.session()!=null)&&(!sess.isStopped()))
		{
			newName=sess.prompt(promptStr,"");
			if(mcp && newName.equals("\\#$#"))
			{
				final int tag=Math.abs(new Random(System.currentTimeMillis()).nextInt());
				sess.sendMcpCommand("dns-org-mud-moo-simpleedit-content",
						" reference: #64.name name: Data type: string content*: \"\" _data-tag: "+tag);
				final List<String> strs = Resources.getFileLineVector(new StringBuffer(oldVal));
				for(final String s : strs)
					sess.rawPrintln("#$#* "+tag+" content: "+s);
				sess.rawPrintln("#$#: "+tag);
				newName = "?";
			}
			else
			if(newName.equals("?")&&((help!=null)||((choices!=null)&&(choices.length>0))))
			{
				if(help!=null)
					mob.tell(help);
				else
					mob.tell(L("You choices are: @x1",CMParms.toListString(choices)));
			}
			else
			{
				final boolean noEntry=(newName.trim().length()==0);
				if(noEntry)
					newName=oldVal;
				else
				if((newName.equalsIgnoreCase("null"))&&(emptyOK))
					newName="";

				if(eval!=null)
				try
				{
					final Object value=eval.eval(newName,choices,emptyOK);
					if(value instanceof String)
						newName=(String)value;
				}
				catch(final CMException e)
				{
					mob.tell(e.getMessage());
					mob.tell(L("You choices are: @x1",CMParms.toListString(choices)));
					newName="?";
					continue;
				}
				if((noEntry)&&(newName.equals(oldVal)))
					break;
				return newName;
			}
		}
		mob.tell(L("(no change)"));
		return oldVal;
	}

	@Override
	public boolean prompt(final MOB mob, final boolean oldVal, final int showNumber, final int showFlag, final String fieldDisplayStr, final String help)
	throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return oldVal;
		mob.tell(showNumber+". "+fieldDisplayStr+": '"+oldVal+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999))
			return oldVal;
		String newName="?";
		while(newName.equals("?")&&(mob.session()!=null)&&(!mob.session().isStopped()))
		{
			newName=mob.session().prompt(L("Enter true or false@x1:",(help!=null?" (?)":"")),"");
			if(newName.equals("?")&&(help!=null))
				mob.tell(help);
			else
			if(newName.toUpperCase().startsWith("T")||newName.toUpperCase().startsWith("F"))
				return newName.toUpperCase().startsWith("T");
			else
			if(newName.toUpperCase().startsWith("Y")||newName.toUpperCase().startsWith("N"))
				return newName.toUpperCase().startsWith("Y");
			else
				break;
		}
		mob.tell(L("(no change)"));
		return oldVal;
	}

	@Override
	public double prompt(final MOB mob, final double oldVal, final int showNumber, final int showFlag, final String fieldDisplayStr, final String help)
	throws IOException
	{
		return prompt(mob, oldVal, showNumber, showFlag, fieldDisplayStr, help,Double.MIN_VALUE,Double.MAX_VALUE);
	}

	public double prompt(final MOB mob, final double oldVal, final int showNumber, final int showFlag, final String fieldDisplayStr, final String help, final double minValue, final double maxValue)
	throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return oldVal;
		mob.tell(showNumber+". "+fieldDisplayStr+": '"+oldVal+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999))
			return oldVal;
		String newName="?";
		while(newName.equals("?")&&(mob.session()!=null)&&(!mob.session().isStopped()))
		{
			newName=mob.session().prompt(L("Enter a new value@x1:",(help!=null?" (?)":"")),"");
			if(newName.equals("?")&&(help!=null))
				mob.tell(help);
			else
			if(CMath.isNumber(newName))
			{
				final double d=CMath.s_double(newName);
				if(d<minValue)
					mob.session().println(L("Min value is: @x1",""+minValue));
				else
				if(d>maxValue)
					mob.session().println(L("Max value is: @x1",""+maxValue));
				else
					return d;
			}
			else
				break;
		}
		mob.tell(L("(no change)"));
		return oldVal;
	}

	@Override
	public int prompt(final MOB mob, final int oldVal, final int showNumber, final int showFlag, final String fieldDisplayStr, final String help)
	throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return oldVal;
		mob.tell(showNumber+". "+fieldDisplayStr+": '"+oldVal+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999))
			return oldVal;
		String newName="?";
		while(newName.equals("?")&&(mob.session()!=null)&&(!mob.session().isStopped()))
		{
			newName=mob.session().prompt(L("Enter a new value@x1:",(help!=null?" (?)":"")),"");
			if(newName.equals("?")&&(help!=null))
				mob.tell(help);
			else
			if(CMath.isInteger(newName))
				return CMath.s_int(newName);
			else
				break;
		}
		mob.tell(L("(no change)"));
		return oldVal;
	}

	@Override
	public long prompt(final MOB mob, final long oldVal, final int showNumber, final int showFlag, final String fieldDisplayStr, final String help)
	throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return oldVal;
		mob.tell(showNumber+". "+fieldDisplayStr+": '"+oldVal+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999))
			return oldVal;
		String newName="?";
		while(newName.equals("?")&&(mob.session()!=null)&&(!mob.session().isStopped()))
		{
			newName=mob.session().prompt(L("Enter a new value@x1:",(help!=null?" (?)":"")),"");
			if(newName.equals("?")&&(help!=null))
				mob.tell(help);
			else
			if(CMath.isInteger(newName))
				return CMath.s_long(newName);
			else
				break;
		}
		mob.tell(L("(no change)"));
		return oldVal;
	}

	@Override
	public int promptMulti(final MOB mob, final int oldVal, final int showNumber, final int showFlag, final String fieldDisplayStr, final PairList<String,String> choices)
	throws IOException
	{
		return CMath.s_int(promptMultiOrExtra(mob,""+oldVal,showNumber,showFlag,fieldDisplayStr,choices));
	}

	@Override
	public String promptMultiOrExtra(final MOB mob, final String oldVal, final int showNumber, final int showFlag, final String fieldDisplayStr, final PairList<String,String> choices)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return oldVal;
		final Vector<String> oldVals = new Vector<String>();
		if(CMath.s_int(oldVal) > 0)
		{
			for(int c=0;c<choices.size();c++)
				if(CMath.bset(CMath.s_int(oldVal),CMath.s_int(choices.get(c).first)))
					oldVals.addElement(choices.get(c).second);
		}
		else
		if(choices.containsFirst(oldVal.toUpperCase().trim()))
			oldVals.addElement(oldVal);
		mob.tell(showNumber+". "+fieldDisplayStr+": '"+CMParms.toListString(oldVals)+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999))
			return oldVal;
		String newVal=oldVal;
		String thisVal="?";
		while(thisVal.equals("?")&&(mob.session()!=null)&&(!mob.session().isStopped()))
		{
			thisVal=mob.session().prompt(L("Enter a new choice to add/remove (?):"),"").trim();
			if(thisVal.equals("?"))
				mob.tell(CMParms.toListString(choices.toArraySecond(new String[0])));
			else
			if(thisVal.length()==0)
				newVal = oldVal;
			else
			if(thisVal.equalsIgnoreCase("NULL"))
			{
				if(choices.containsFirst(""))
					newVal = "";
				else
					newVal = "0";
				oldVals.clear();
				break;
			}
			else
			{
				String foundChoice = null;
				String foundVal = "";
				for(int c=0;c<choices.size();c++)
				{
					if(choices.get(c).second.equalsIgnoreCase(thisVal))
					{
						foundChoice = choices.get(c).second;
						foundVal = choices.get(c).first.toString();
					}
				}
				if(foundChoice == null)
				{
					mob.tell(L("'@x1' is not an available option.  Use ? for a list.",newVal));
					thisVal = "?";
				}
				else
				if(!CMath.isInteger(foundVal))
				{
					oldVals.clear();
					newVal = foundVal;
					oldVals.addElement(foundVal);
				}
				else
				if(foundVal == "0")
				{
					newVal = "0";
					oldVals.clear();
				}
				else
				{
					if(oldVals.contains(foundChoice))
					{
						newVal = Integer.toString(CMath.s_int(newVal) - CMath.s_int(foundVal));
						oldVals.remove(foundChoice);
						mob.tell(L("'@x1' removed.",foundChoice));
						thisVal = "?";
					}
					else
					{
						oldVals.add(foundChoice);
						mob.tell(L("'@x1' added.",foundChoice));
						thisVal = "?";
						newVal = Integer.toString(CMath.s_int(newVal) | CMath.s_int(foundVal));
					}
				}
			}
		}
		if(oldVal.equals(newVal))
			mob.tell(L("(no change)"));
		return newVal;
	}

	@Override
	public String promptMultiSelectList(final MOB mob, final String oldVal, final String delimiter, final int showNumber, final int showFlag, final String fieldDisplayStr, final PairList<String,String> choices, final boolean nullOK)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return oldVal;
		final Vector<String> oldVals = new Vector<String>();
		for(final String s : CMParms.parseAny(oldVal,delimiter,!nullOK))
		{
			if(choices.containsFirst(s.toUpperCase().trim())&&(!oldVals.contains(s.toUpperCase().trim())))
				oldVals.addElement(s);
		}
		if((showFlag!=showNumber)&&(showFlag>-999))
		{
			mob.tell(showNumber+". "+fieldDisplayStr+": '"+CMParms.toListString(oldVals)+"'.");
			return oldVal;
		}
		String thisVal="?";
		while(thisVal.equals("?")&&(mob.session()!=null)&&(!mob.session().isStopped()))
		{
			mob.tell(showNumber+". "+fieldDisplayStr+": '"+CMParms.toListString(oldVals)+"'.");
			thisVal=mob.session().prompt(L("Enter a new choice to add/remove (?):"),"").trim();
			if(thisVal.equals("?"))
				mob.tell(CMParms.toListString(choices.toArraySecond(new String[0])));
			else
			if(thisVal.equalsIgnoreCase("NULL") && nullOK)
			{
				oldVals.clear();
				thisVal="?";
			}
			else
			if(thisVal.trim().length()>0)
			{
				String foundChoice = null;
				for(int c=0;c<choices.size();c++)
				{
					if(choices.get(c).second.equalsIgnoreCase(thisVal))
						foundChoice = choices.get(c).second;
				}
				if(foundChoice == null)
				{
					mob.tell(L("'@x1' is not an available option.  Use ? for a list.",thisVal));
					thisVal = "?";
				}
				else
				{
					if(oldVals.contains(foundChoice))
					{
						oldVals.remove(foundChoice);
						mob.tell(L("'@x1' removed.",foundChoice));
						thisVal = "?";
					}
					else
					{
						oldVals.add(foundChoice);
						mob.tell(L("'@x1' added.",foundChoice));
						thisVal = "?";
					}
				}
			}
		}
		final String newVal=CMParms.combineWith(oldVals, "|");
		if(oldVal.equals(newVal))
			mob.tell(L("(no change)"));
		return newVal;
	}

	@Override
	public String promptChoice(final MOB mob, final String oldVal, final int showNumber, final int showFlag, final String fieldDisplayStr, final PairList<String,String> choices)
	throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return oldVal;
		String oldShowVal = oldVal;
		for(int c=0;c<choices.size();c++)
		{
			if(choices.get(c).first.equalsIgnoreCase(oldVal))
				oldShowVal = choices.get(c).second;
		}
		mob.tell(showNumber+". "+fieldDisplayStr+": '"+oldShowVal+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999))
			return oldVal;
		String newVal="?";
		while(newVal.equals("?")&&(mob.session()!=null)&&(!mob.session().isStopped()))
		{
			newVal=mob.session().prompt(L("Enter a new choice (? or NULL):"),"").trim();
			if(newVal.equals("?"))
				mob.tell(CMParms.toListString(choices.toArraySecond(new String[0])));
			else
			if(newVal.length()==0)
				newVal = oldVal;
			else
			{
				if(newVal.equalsIgnoreCase("NULL"))
					newVal = "";
				String foundChoice = null;
				for(int c=0;c<choices.size();c++)
				{
					if(choices.get(c).second.equalsIgnoreCase(newVal))
						foundChoice = choices.get(c).first;
				}
				if(foundChoice == null)
					mob.tell(L("'@x1' is not an available choice.  Use ? for a list.",newVal));
				else
				{
					newVal = foundChoice;
					break;
				}
			}
		}
		if(oldVal.equals(newVal))
			mob.tell(L("(no change)"));
		return newVal;
	}

	@Override
	public void genName(final MOB mob, final Environmental E, final int showNumber, final int showFlag) throws IOException
	{
		final String newName=prompt(mob,E.Name(),showNumber,showFlag,"Name",false,false);
		if(newName.equals(E.Name()))
			return;
		if((mob.session()==null)
		||((!(E instanceof MOB))&&(!(E instanceof Item)))
		||(!CMLib.flags().isCataloged(E)))
		{
			final String oldName=E.Name();
			E.setName(newName);
			if(E.displayText().toLowerCase().startsWith(oldName.toLowerCase()))
				E.setDisplayText(newName+E.displayText().substring(oldName.length()));
			if(E instanceof Boardable)
				((Boardable)E).rename(newName);
			return;
		}
		if((E instanceof Physical)&&(CMLib.flags().isCataloged(E)))
		{
			final Physical P = (Physical)E;
			final Physical cataP=CMLib.catalog().getCatalogObj(P);
			if(cataP==null)
			{
				P.setName(newName);
				CMLib.catalog().changeCatalogUsage(P,false);
				return;
			}
			else
			if(mob.session().confirm(L("This object is cataloged.  Changing its name will detach it from the cataloged version, are you sure (y/N)?"),"N"))
			{
				CMLib.catalog().changeCatalogUsage(P,false);
				P.setName(newName);
			}
		}
	}

	protected void catalogCheckUpdate(final MOB mob, final Physical P)
		throws IOException
	{
		if((!CMLib.flags().isCataloged(P))
		||((!(P instanceof MOB))&&(!(P instanceof Item)))
		||(mob.session()==null))
		{
			if(P instanceof MOB)
				P.setMiscText(P.text());
			return;
		}

		final StringBuffer diffs=CMLib.catalog().checkCatalogIntegrity(P);
		if((diffs!=null)&&(diffs.length()>0))
		{
			final Physical origCataP = CMLib.catalog().getCatalogObj(P);
			final Physical cataP=(Physical)origCataP.copyOf();
			CMLib.catalog().changeCatalogUsage(cataP,true);
			final StringBuilder detailedDiff=new StringBuilder("");
			final List<String> V=CMParms.parseCommas(diffs.toString(),true);

			for(int v=0;v<V.size();v++)
			{
				final String stat=V.get(v);
				detailedDiff.append("CATALOG:"+stat+":'"+cataP.getStat(stat)+"'\n\r");
				detailedDiff.append("YOURS  :"+stat+":'"+P.getStat(stat)+"'\n\r");
			}
			cataP.destroy();
			mob.tell(L("You have modified the following fields: \n\r@x1",detailedDiff.toString()));
			final String message = "This object is cataloged.  Enter U to update the cataloged version, or D to detach this object from the catalog, or C to Cancel (u/d/C)?";
			final String choice = mob.session().choose(message, L("UDC"), L("C"));
			if(choice.equalsIgnoreCase("C"))
			{
				P.setMiscText(origCataP.text());
				P.recoverPhyStats();
				if(P instanceof MOB)
				{
					((MOB)P).recoverCharStats();
					((MOB)P).recoverMaxState();
				}
				CMLib.catalog().changeCatalogUsage(P, true);
			}
			else
			if(choice.equalsIgnoreCase("U"))
			{
				CMLib.catalog().updateCatalog(P);
				mob.tell(L("Catalog update complete."));
				Log.infoOut("BaseGenerics",mob.Name()+" updated catalog "+((P instanceof MOB)?"MOB":"ITEM")+" "+P.Name());
				P.setMiscText(P.text());
			}
			else
			if(choice.equalsIgnoreCase("D"))
			{
				CMLib.catalog().changeCatalogUsage(P,false);
				P.setMiscText(P.text());
			}
			else
				mob.tell(L("That wasn't a choice?!"));
		}
	}

	protected void genImage(final MOB mob, final Environmental E, final int showNumber, final int showFlag) throws IOException
	{
		E.setImage(prompt(mob, E.rawImage(), showNumber, showFlag, "MXP Image filename", true, false,
				"This is the path/filename of your MXP image file for this object."));
	}

	protected void genCorpseData(final MOB mob, final DeadBody I, final int showNumber, final int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		mob.tell(L("@x1. Corpse Data: '@x2/@x3'.",""+showNumber,I.getMobName(),I.getKillerName()));
		if((showFlag!=showNumber)&&(showFlag>-999))
			return;
		mob.tell(L("Dead MOB name: '@x1'.",I.getMobName()));
		String newName=mob.session().prompt(L("Enter a new name\n\r:"),"");
		if(newName.length()>0)
			I.setMobName(newName);
		else mob.tell(L("(no change)"));
		mob.tell(L("Dead MOB Description: '@x1'.",I.getMobDescription()));
		newName=mob.session().prompt(L("Enter a new description\n\r:"),"");
		if(newName.length()>0)
			I.setMobDescription(newName);
		else mob.tell(L("(no change)"));
		mob.tell(L("Is a Players corpse: @x1",""+I.isPlayerCorpse()));
		newName=mob.session().prompt(L("Enter a new true/false\n\r:"),"");
		if((newName.length()>0)&&(newName.equalsIgnoreCase("true")||newName.equalsIgnoreCase("false")))
			I.setIsPlayerCorpse(Boolean.valueOf(newName.toLowerCase()).booleanValue());
		else mob.tell(L("(no change)"));
		mob.tell(L("Dead mobs PK flag: @x1",""+I.getMobPKFlag()));
		newName=mob.session().prompt(L("Enter a new true/false\n\r:"),"");
		if((newName.length()>0)&&(newName.equalsIgnoreCase("true")||newName.equalsIgnoreCase("false")))
			I.setMobPKFlag(Boolean.valueOf(newName.toLowerCase()).booleanValue());
		else mob.tell(L("(no change)"));
		genCharStats(mob,I.charStats());
		mob.tell(L("Killers Name: '@x1'.",I.getKillerName()));
		newName=mob.session().prompt(L("Enter a new killer\n\r:"),"");
		if(newName.length()>0)
			I.setKillerName(newName);
		else mob.tell(L("(no change)"));
		mob.tell(L("Killer is a player: @x1",""+I.isKillerPlayer()));
		newName=mob.session().prompt(L("Enter a new true/false\n\r:"),"");
		if((newName.length()>0)&&(newName.equalsIgnoreCase("true")||newName.equalsIgnoreCase("false")))
			I.setIsKillerPlayer(Boolean.valueOf(newName.toLowerCase()).booleanValue());
		else mob.tell(L("(no change)"));
		mob.tell(L("Time of death: @x1",CMLib.time().date2String(I.getTimeOfDeath())));
		newName=mob.session().prompt(L("Enter a new value\n\r:"),"");
		if(newName.length()>0)
			I.setTimeOfDeath(CMLib.time().string2Millis(newName));
		else mob.tell(L("(no change)"));
		mob.tell(L("Last message string: @x1",I.getLastMessage()));
		newName=mob.session().prompt(L("Enter a new value\n\r:"),"");
		if(newName.length()>0)
			I.setLastMessage(newName);
		else mob.tell(L("(no change)"));
	}

	protected void genAuthor(final MOB mob, final Area A, final int showNumber, final int showFlag) throws IOException
	{
		A.setAuthorID(prompt(mob, A.getAuthorID(), showNumber, showFlag, "Author", true, false, "Area Author's Name"));
	}

	protected void genPanelType(final MOB mob, final ElecPanel S, final int showNumber, final int showFlag)
	throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		if(S.panelType()==null)
			return;
		final String componentType=CMStrings.capitalizeAndLower(S.panelType().name().toLowerCase());
		mob.tell(L("@x1. Panel Type: '@x2'.",""+showNumber,componentType));
		if((showFlag!=showNumber)&&(showFlag>-999))
			return;
		boolean continueThis=true;
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(continueThis))
		{
			continueThis=false;
			final String newName=mob.session().prompt(L("Enter a new one (?)\n\r:"),"");
			if(newName.length()>0)
			{
				if(newName.equalsIgnoreCase("?"))
				{
					mob.tell(L("Component Types: @x1",CMParms.toListString(ElecPanel.PANELTYPES)));
					continueThis=true;
				}
				else
				{
					TechType newType=null;
					for(int i=0;i<TechType.values().length;i++)
					{
						if(TechType.values()[i].name().equalsIgnoreCase(newName))
							newType=TechType.values()[i];
					}
					if(newType==null)
					{
						mob.tell(L("'@x1' is not recognized.  Try '?' for a list.",newName));
						continueThis=true;
					}
					else
						S.setPanelType(newType);
				}
			}
			else
				mob.tell(L("(no change)"));
		}
	}

	protected void genCurrency(final MOB mob, final Economics A, final int showNumber, final int showFlag)
	throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		final String currencyName=((A.getRawCurrency()==null)||(A.getRawCurrency().length()==0))?"Default":A.getRawCurrency();
		if(mob.session()!=null)
			mob.session().colorOnlyPrintln(L("@x1. Currency: '@x2'.",""+showNumber,currencyName));
		if((showFlag!=showNumber)&&(showFlag>-999))
			return;
		final String newName=mob.session().prompt(L("Enter a new one or 'DEFAULT'\n\r:"),"");
		if(newName.length()>0)
		{
			if(newName.equalsIgnoreCase("default"))
				A.setCurrency("");
			else
			if((newName.indexOf('=')<0)&&(!CMLib.beanCounter().getAllCurrencies().contains(newName.trim().toUpperCase())))
			{
				final List<String> V=CMLib.beanCounter().getAllCurrencies();
				mob.tell(L("'@x1' is not a known currency. Existing currencies include: DEFAULT@x2",newName.trim().toUpperCase(),CMParms.toListString(V)));
			}
			else
			if(newName.indexOf('=')>=0)
				A.setCurrency(newName.trim());
			else
				A.setCurrency(newName.toUpperCase().trim());
		}
		else
			mob.tell(L("(no change)"));
	}

	protected void genTimeClock(final MOB mob, final Area A, final int showNumber, final int showFlag)
	throws IOException
	{

		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		final TimeClock TC=A.getTimeObj();
		StringBuilder report=new StringBuilder("");
		boolean usingParent = false;
		for(final Enumeration<Area> a=A.getParents();a.hasMoreElements();)
		{
			if(a.nextElement().getTimeObj() == A.getTimeObj())
				usingParent=true;
		}
		if(TC==CMLib.time().globalClock())
			report.append("Default -- Can't be changed.");
		else
		if(usingParent)
			report.append("Inherited -- Can't be changed.");
		else
		{
			report.append(TC.getHoursInDay()+" hrs-day/");
			report.append(TC.getDaysInMonth()+" days-mn/");
			report.append(TC.getMonthsInYear()+" mnths-yr");
		}
		mob.tell(L("@x1. Calendar: '@x2'.",""+showNumber,report.toString()));
		if(TC==CMLib.time().globalClock() || usingParent)
			return;
		if((showFlag!=showNumber)&&(showFlag>-999))
			return;
		String newName="";
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(newName.length()==0))
		{
			report=new StringBuilder("\n\rCalendar/Clock settings:\n\r");
			report.append("1. "+TC.getHoursInDay()+" hours per day\n\r");
			report.append("2. Dawn Hour: "+TC.getDawnToDusk()[TimeClock.TimeOfDay.DAWN.ordinal()]+"\n\r");
			report.append("3. Day Hour: "+TC.getDawnToDusk()[TimeClock.TimeOfDay.DAY.ordinal()]+"\n\r");
			report.append("4. Dusk Hour: "+TC.getDawnToDusk()[TimeClock.TimeOfDay.DUSK.ordinal()]+"\n\r");
			report.append("5. Night Hour: "+TC.getDawnToDusk()[TimeClock.TimeOfDay.NIGHT.ordinal()]+"\n\r");
			report.append("6. Days/Month: "+TC.getDaysInMonth()+"\n\r");
			report.append("7. Weekdays: "+CMParms.toListString(TC.getWeekNames())+"\n\r");
			report.append("8. Months: "+CMParms.toListString(TC.getMonthNames())+"\n\r");
			report.append("9. Year Title(s): "+CMParms.toListString(TC.getYearNames()));
			mob.tell(report.toString());
			newName=mob.session().prompt(L("Enter one to change (or global):"),"");
			if(newName.length()==0)
				break;
			if(newName.equalsIgnoreCase("global"))
			{
				A.setTimeObj((TimeClock)CMLib.time().globalClock().copyOf());
				continue;
			}

			final int which=CMath.s_int(newName);

			if((which<0)||(which>9))
				mob.tell(L("Invalid: @x1",""+which));
			else
			if(which<=6)
			{
				newName="";
				final String newNum=mob.session().prompt(L("Enter a new number:"),"");
				final int val=CMath.s_int(newNum);
				if(newNum.length()==0)
					mob.tell(L("(no change)"));
				else
				switch(which)
				{
				case 1:
					TC.setHoursInDay(val);
					break;
				case 2:
					TC.getDawnToDusk()[TimeClock.TimeOfDay.DAWN.ordinal()]=val;
					break;
				case 3:
					if((val>=0)&&(TC.getDawnToDusk()[TimeClock.TimeOfDay.DAWN.ordinal()]>=val))
						mob.tell(L("That value is before the dawn!"));
					else
						TC.getDawnToDusk()[TimeClock.TimeOfDay.DAY.ordinal()]=val;
					break;
				case 4:
					if((val>=0)&&(TC.getDawnToDusk()[TimeClock.TimeOfDay.DAWN.ordinal()]>=val))
						mob.tell(L("That value is before the dawn!"));
					else
					if((val>=0)&&(TC.getDawnToDusk()[TimeClock.TimeOfDay.DAY.ordinal()]>=val))
						mob.tell(L("That value is before the day!"));
					else
						TC.getDawnToDusk()[TimeClock.TimeOfDay.DUSK.ordinal()]=val;
					break;
				case 5:
					if((val>=0)&&(TC.getDawnToDusk()[TimeClock.TimeOfDay.DAWN.ordinal()]>=val))
						mob.tell(L("That value is before the dawn!"));
					else
					if((val>=0)&&(TC.getDawnToDusk()[TimeClock.TimeOfDay.DAY.ordinal()]>=val))
						mob.tell(L("That value is before the day!"));
					else
					if((val>=0)&&(TC.getDawnToDusk()[TimeClock.TimeOfDay.DUSK.ordinal()]>=val))
						mob.tell(L("That value is before the dusk!"));
					else
						TC.getDawnToDusk()[TimeClock.TimeOfDay.NIGHT.ordinal()]=val;
					break;
				case 6:
					TC.setDaysInMonth(val);
					break;
				}
			}
			else
			{
				newName="";
				final String newNum=mob.session().prompt(L("Enter a new list (comma delimited)\n\r:"),"");
				if(newNum.length()==0)
					mob.tell(L("(no change)"));
				else
				switch(which)
				{
				case 7:
					TC.setWeekNames(CMParms.toStringArray(CMParms.parseCommas(newNum,true)));
					break;
				case 8:
					TC.setMonthsInYear(CMParms.toStringArray(CMParms.parseCommas(newNum,true)));
					break;
				case 9:
					TC.setYearNames(CMParms.toStringArray(CMParms.parseCommas(newNum,true)));
					break;
				}
			}
		}
		TC.save();
	}

	protected void genClan(final MOB mob, final MOB M, final int showNumber, final int showFlag)
		throws IOException
	{
		if((showFlag<=0)||(showFlag==showNumber))
		{
			boolean more=true;
			while(more && (mob.session()!=null) && (!mob.session().isStopped()))
			{
				more=false;
				final StringBuilder clanList=new StringBuilder("");
				for(final Pair<Clan,Integer> c : M.clans())
					clanList.append(c.first.getName()).append(" (").append(c.first.getRoleName(c.second.intValue(), false, false)).append("), ");
				if(clanList.length()>2)
					clanList.setLength(clanList.length()-2);
				mob.tell(L("@x1. Clan(s): '@x2'.",""+showNumber,clanList.toString()));
				if((showFlag==showNumber)||(showFlag<=-999))
				{
					more=true;
					final String newName=mob.session().prompt(L("Enter a new clan to add, remove, or change (?):"),"");
					Clan C=null;
					if(newName.trim().length()==0)
						more=false;
					else
					{
						C=CMLib.clans().getClanAnyHost(newName);
						if(C==null)
							C=CMLib.clans().findClan(newName);
						if(C!=null)
						{
							if(M.getClanRole(C.clanID())==null)
							{
								final String role=C.getRoleName(C.getGovernment().getAcceptPos(), false, false);
								final String newRole=mob.session().prompt(L("Enter role [@x1]\n\r: (@x2):",CMParms.toListString(C.getRolesList()),role),role);
								if(newRole.trim().length()>0)
								{
									int roleID=-1;
									if(CMath.isInteger(newRole.trim()))
										roleID=CMath.s_int(newRole.trim());
									else
										roleID=C.getRoleFromName(newRole.trim());
									if(roleID<0)
										mob.tell(L("Invalid role '@x1'",newRole));
									else
									{
										M.setClan(C.clanID(), roleID);
										mob.tell(L("Clan added."));
									}
								}
								else
									mob.tell(L("(no change)"));
							}
							else
							{
								M.setClan(C.clanID(), -1);
								mob.tell(L("Clan removed."));
							}
						}
						else
						{
							final StringBuilder list=new StringBuilder("(no clan '"+newName+"', try: ");
							for(final Enumeration<Clan> e=CMLib.clans().clans();e.hasMoreElements();)
								list.append(e.nextElement().getName()).append(", ");
							list.setLength(list.length()-2);
							mob.tell(list.toString()+")");
						}
					}
				}
			}
		}
	}

	protected void genDeity(final MOB mob, final MOB M, final int showNumber, final int showFlag)
	throws IOException
	{
		if((showFlag<=0)||(showFlag==showNumber))
		{
			mob.tell(L("@x1. Deity (ID): '@x2'.",""+showNumber,M.baseCharStats().getWorshipCharID()));
			if((showFlag==showNumber)||(showFlag<=-999))
			{
				final String newName=mob.session().prompt(L("Enter a new one (null)\n\r:"),"");
				if(newName.equalsIgnoreCase("null"))
					M.baseCharStats().setWorshipCharID("");
				else
				if(newName.length()>0)
				{
					if(CMLib.map().getDeity(newName)==null)
						mob.tell(L("That deity does not exist."));
					else
						M.baseCharStats().setWorshipCharID(CMLib.map().getDeity(newName).Name());
				}
				else
					mob.tell(L("(no change)"));
			}
			mob.recoverCharStats();
		}
	}

	@Override
	public Room changeRoomType(Room R, final Room newRoom)
	{
		if((R==null)||(newRoom==null))
			return R;
		synchronized(CMClass.getSync("SYNC"+R.roomID()))
		{
			R=CMLib.map().getRoom(R);
			final Room oldR=R;
			R=newRoom;
			final Vector<CMObject> oldBehavsNEffects=new Vector<CMObject>();
			for(final Enumeration<Ability> a=oldR.effects();a.hasMoreElements();)
			{
				final Ability A=a.nextElement();
				if(A!=null)
				{
					if(!A.canBeUninvoked())
					{
						oldBehavsNEffects.addElement(A);
						oldR.delEffect(A);
					}
					else
						A.unInvoke();
				}
			}
			for(final Enumeration<Behavior> e=oldR.behaviors();e.hasMoreElements();)
			{
				final Behavior B=e.nextElement();
				if(B!=null)
					oldBehavsNEffects.addElement(B);
			}
			CMLib.threads().deleteTick(oldR,-1);
			CMLib.threads().rejuv(R, Tickable.TICKID_ROOM_ITEM_REJUV);
			R.setRoomID(oldR.roomID());
			for(int d=0;d<R.rawDoors().length;d++)
				R.rawDoors()[d]=oldR.rawDoors()[d];
			for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
				R.setRawExit(d,oldR.getRawExit(d));
			final Area A=oldR.getArea();
			if(A!=null)
				A.delProperRoom(oldR);
			R.setSavable(oldR.getArea().isSavable() && oldR.isSavable());
			R.setArea(A);
			R.setDisplayText(oldR.displayText());
			R.setDescription(oldR.description());
			if(R.image().equalsIgnoreCase(CMLib.protocol().getDefaultMXPImage(oldR)))
				R.setImage(null);
			if((R instanceof GridLocale)&&(oldR instanceof GridLocale))
			{
				((GridLocale)R).setXGridSize(((GridLocale)oldR).xGridSize());
				((GridLocale)R).setYGridSize(((GridLocale)oldR).yGridSize());
				((GridLocale)R).clearGrid(null);
			}
			final List<MOB> allmobs=new ArrayList<MOB>();
			int skip=0;
			while(oldR.numInhabitants()>(skip))
			{
				final MOB M=oldR.fetchInhabitant(skip);
				if(M.isSavable())
				{
					if(!allmobs.contains(M))
						allmobs.add(M);
					oldR.delInhabitant(M);
				}
				else
				if(oldR!=R)
				{
					oldR.delInhabitant(M);
					R.bringMobHere(M,true);
				}
				else
					skip++;
			}
			final List<Item> allitems=new ArrayList<Item>();
			while(oldR.numItems()>0)
			{
				final Item I=oldR.getItem(0);
				if(!allitems.contains(I))
					allitems.add(I);
				oldR.delItem(I);
			}

			for(int i=0;i<allitems.size();i++)
			{
				final Item I=allitems.get(i);
				if(!R.isContent(I))
				{
					if(I.subjectToWearAndTear())
						I.setUsesRemaining(100);
					I.recoverPhyStats();
					R.addItem(I);
					R.recoverRoomStats();
				}
			}
			for(int m=0;m<allmobs.size();m++)
			{
				final MOB M=allmobs.get(m);
				if(!R.isInhabitant(M))
				{
					final MOB M2=(MOB)M.copyOf();
					M2.setStartRoom(R);
					M2.setLocation(R);
					long rejuv=CMProps.getTicksPerMinute()+CMProps.getTicksPerMinute()+(CMProps.getTicksPerMinute()/2);
					if(rejuv>(CMProps.getTicksPerMinute()*20))
						rejuv=(CMProps.getTicksPerMinute()*20);
					M2.phyStats().setRejuv((int)rejuv);
					M2.recoverCharStats();
					M2.recoverPhyStats();
					M2.recoverMaxState();
					M2.resetToMaxState();
					M2.bringToLife(R,true);
					R.recoverRoomStats();
					M.destroy();
				}
			}

			try
			{
				for(final Enumeration<Room> r=R.getArea().getFilledProperMap();r.hasMoreElements();)
				{
					final Room R2=r.nextElement();
					for(int d=0;d<R2.rawDoors().length;d++)
					{
						if(R2.rawDoors()[d]==oldR)
						{
							R2.rawDoors()[d]=R;
							if(R2 instanceof GridLocale)
								((GridLocale)R2).buildGrid();
						}
					}
				}
				for(final Enumeration<Room> r=CMLib.map().rooms();r.hasMoreElements();)
				{
					final Room R2=r.nextElement();
					for(int d=0;d<R2.rawDoors().length;d++)
					{
						if(R2.rawDoors()[d]==oldR)
						{
							R2.rawDoors()[d]=R;
							if(R2 instanceof GridLocale)
								((GridLocale)R2).buildGrid();
						}
					}
				}
			}
			catch (final NoSuchElementException e)
			{
			}
			try
			{
				for(final Enumeration<MOB> e=CMLib.players().players();e.hasMoreElements();)
				{
					final MOB M=e.nextElement();
					if(M.getStartRoom()==oldR)
						M.setStartRoom(R);
					else
					if(M.location()==oldR)
						M.setLocation(R);
				}
			}
			catch (final NoSuchElementException e)
			{
			}
			R.getArea().fillInAreaRoom(R);
			for(int i=0;i<oldBehavsNEffects.size();i++)
			{
				if(oldBehavsNEffects.elementAt(i) instanceof Behavior)
					R.addBehavior((Behavior)oldBehavsNEffects.elementAt(i));
				else
					R.addNonUninvokableEffect((Ability)oldBehavsNEffects.elementAt(i));
			}
			if(R.isSavable())
			{
				CMLib.database().DBUpdateRoom(R);
				CMLib.database().DBUpdateMOBs(R);
				CMLib.database().DBUpdateItems(R);
			}
			oldR.destroy();
			R.giveASky(0);
			R.getArea().addProperRoom(R); // necessary because of the destroy
			R.setImage(R.rawImage());
			R.startItemRejuv();
		}
		return R;
	}

	protected Room genRoomType(final MOB mob, Room R, final int showNumber, final int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return R;
		mob.tell(L("@x1. Type: '@x2'",""+showNumber,CMClass.classID(R)));
		if((showFlag!=showNumber)&&(showFlag>-999))
			return R;
		String newName="";
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(newName.length()==0))
		{
			newName=mob.session().prompt(L("Enter a new one (?)\n\r:"),"");
			if(newName.trim().equals("?"))
			{
				mob.tell(CMLib.lister().build2ColTable(mob,CMClass.locales()).toString()+"\n\r");
				newName="";
			}
			else
			if(newName.length()>0)
			{
				final Room newRoom=CMClass.getLocale(newName);
				if(newRoom==null)
					mob.tell(L("'@x1' does not exist. No Change.",newName));
				else
				if(mob.session().confirm(L("This will change the room type of room @x1. It will automatically save any mobs and items in this room permanently.  Are you absolutely sure (y/N)?",R.roomID()),"N"))
					R=changeRoomType(R,newRoom);
				R.recoverRoomStats();
			}
			else
			{
				mob.tell(L("(no change)"));
				break;
			}
		}
		return R;
	}

	@Override
	public void genDescription(final MOB mob, final Environmental E, final int showNumber, final int showFlag) throws IOException
	{
		E.setDescription(prompt(mob, E.description(), showNumber, showFlag, "Description", true, true, null));
	}

	protected void genNotes(final MOB mob, final MOB M, final int showNumber, final int showFlag) throws IOException
	{
		if(M.playerStats()!=null)
		M.playerStats().setNotes(prompt(mob,M.playerStats().getNotes(),showNumber,showFlag,"Private notes",true,false,null));
	}

	protected void genPassword(final MOB mob, final MOB M, final int showNumber, final int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		mob.tell(L("@x1. Password: ********.",""+showNumber));
		if((showFlag!=showNumber)&&(showFlag>-999))
			return;
		final String str=mob.session().prompt(L("Enter a new one to reset\n\r:"),"");
		if((str.length()>0)&&(M.playerStats()!=null))
		{
			M.playerStats().setPassword(str);
			CMLib.database().DBUpdatePassword(M.Name(),M.playerStats().getPasswordStr());
		}
		else
			mob.tell(L("(no change)"));
	}

	protected void genEmail(final MOB mob, final AccountStats A, final int showNumber, final int showFlag) throws IOException
	{
		if(A==null)
			return;
		A.setEmail(prompt(mob,A.getEmail(),showNumber,showFlag,"Email",true,false,null));
	}

	protected void genBirthday(final MOB mob, final PlayerStats A, final int showNumber, final int showFlag) throws IOException
	{
		if(A==null)
			return;
		A.setStat("BIRTHDAY",prompt(mob,A.getStat("BIRTHDAY"),showNumber,showFlag,"Birthday (d,m,y)",true,false,null));
	}

	@Override
	public void genDisplayText(final MOB mob, final Environmental E, final int showNumber, final int showFlag) throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		if(mob.session()!=null)
			mob.session().safeRawPrintln(L("@x1. Display: '@x2'.",""+showNumber,E.displayText()));
		if((showFlag!=showNumber)&&(showFlag>-999))
			return;
		String newName=null;
		if(E instanceof Item)
			newName=mob.session().prompt(L("Enter something new (null == blended)\n\r:"),"");
		else
		if(E instanceof Exit)
			newName=mob.session().prompt(L("Enter something new (null == see-through)\n\r:"),"");
		else
			newName=mob.session().prompt(L("Enter something new (null = empty)\n\r:"),"");
		if(newName.length()>0)
		{
			if(newName.trim().equalsIgnoreCase("null"))
				newName="";
			E.setDisplayText(newName);
		}
		else
			mob.tell(L("(no change)"));
		if((E instanceof Item)&&(E.displayText().length()==0))
			mob.tell(L("(blended)"));
	}

	protected void genMountText(final MOB mob, final Rideable E, final int showNumber, final int showFlag)
			throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		if(mob.session()==null)
			return;
		mob.session().safeRawPrintln(L("@x1. Mount Strings: '@x2'.",""+showNumber,
							E.putString(CMClass.sampleMOB())+"/"+
							E.mountString(0, CMClass.sampleMOB())+"/"+
							E.dismountString(CMClass.sampleMOB())));
		if((showFlag!=showNumber)&&(showFlag>-999))
			return;
		String newName;
		mob.session().safeRawPrintln(L("Enter new 'put' string (ENTER='"+E.putString(CMClass.sampleMOB())+"')"));
		newName=mob.session().prompt(":","");
		if(newName.length()>0)
			E.setPutString(newName);
		else
			mob.tell(L("(no change)"));
		mob.session().safeRawPrintln(L("Enter new 'mount' string (ENTER='"+E.mountString(0,CMClass.sampleMOB())+"')"));
		newName=mob.session().prompt(":","");
		if(newName.length()>0)
			E.setMountString(newName);
		else
			mob.tell(L("(no change)"));
		mob.session().safeRawPrintln(L("Enter new 'dismount' string (ENTER='"+E.dismountString(CMClass.sampleMOB())+"')"));
		newName=mob.session().prompt(":","");
		if(newName.length()>0)
			E.setDismountString(newName);
		else
			mob.tell(L("(no change)"));
	}

	protected void genMountRideMountText(final MOB mob, final Rideable E, final int showNumber, final int showFlag)
			throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		if(mob.session()==null)
			return;
		mob.session().safeRawPrintln(L("@x1. State Strings: '@x2'.",""+showNumber,
							E.stateString(CMClass.sampleMOB())+"/"+
							E.stateStringSubject(CMClass.sampleMOB())+"/"+
							E.rideString(CMClass.sampleMOB())));
		if((showFlag!=showNumber)&&(showFlag>-999))
			return;
		String newName;
		mob.session().safeRawPrintln(L("Enter new 'state' string (ENTER='"+E.stateString(CMClass.sampleMOB())+"')"));
		newName=mob.session().prompt(":","");
		if(newName.length()>0)
			E.setStateString(newName);
		else
			mob.tell(L("(no change)"));
		mob.session().safeRawPrintln(L("Enter new 'state subject' string (ENTER='"+E.stateStringSubject(CMClass.sampleMOB())+"')"));
		newName=mob.session().prompt(":","");
		if(newName.length()>0)
			E.setStateStringSubject(newName);
		else
			mob.tell(L("(no change)"));
		mob.session().safeRawPrintln(L("Enter new 'ride verb' string (ENTER='"+E.rideString(CMClass.sampleMOB())+"')"));
		newName=mob.session().prompt(":","");
		if(newName.length()>0)
			E.setRideString(newName);
		else
			mob.tell(L("(no change)"));
	}

	protected void genClosedText(final MOB mob, final Exit E, final int showNumber, final int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		if(E instanceof Item)
			mob.tell(L("@x1. Exit Closed Text: '@x2'.",""+showNumber,E.closedText()));
		else
			mob.tell(L("@x1. Closed Text: '@x2'.",""+showNumber,E.closedText()));
		if((showFlag!=showNumber)&&(showFlag>-999))
			return;
		final String newName=mob.session().prompt(L("Enter something new (null=blank)\n\r:"),"");
		if(newName.equals("null"))
			E.setExitParams(E.doorName(),E.closeWord(),E.openWord(),"");
		else
		if(newName.length()>0)
			E.setExitParams(E.doorName(),E.closeWord(),E.openWord(),newName);
		else
			mob.tell(L("(no change)"));
	}

	protected void genDoorName(final MOB mob, final Exit E, final int showNumber, final int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		if(E instanceof Item)
			mob.tell(L("@x1. Exit Direction (or 'null'): '@x2'.",""+showNumber,E.doorName()));
		else
			mob.tell(L("@x1. Door Name: '@x2'.",""+showNumber,E.doorName()));
		if((showFlag!=showNumber)&&(showFlag>-999))
			return;
		String newName=mob.session().prompt(L("Enter something new\n\r:"),"");
		if(newName.length()>0)
		{
			if((E instanceof Item)&&(newName.equalsIgnoreCase("null")))
				newName="";
			E.setExitParams(newName,E.closeWord(),E.openWord(),E.closedText());
		}
		else
			mob.tell(L("(no change)"));
	}

	protected void genBurnout(final MOB mob, final Light I, final int showNumber, final int showFlag)
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		mob.tell(L("@x1. Is destroyed after burnout: '@x2'.",""+showNumber,""+I.destroyedWhenBurnedOut()));
		if((showFlag!=showNumber)&&(showFlag>-999))
			return;
		I.setDestroyedWhenBurntOut(!I.destroyedWhenBurnedOut());
	}

	protected void genOpenWord(final MOB mob, final Exit E, final int showNumber, final int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		mob.tell(L("@x1. Open Word: '@x2'.",""+showNumber,E.openWord()));
		if((showFlag!=showNumber)&&(showFlag>-999))
			return;
		final String newName=mob.session().prompt(L("Enter something new\n\r:"),"");
		if(newName.length()>0)
			E.setExitParams(E.doorName(),E.closeWord(),newName,E.closedText());
		else
			mob.tell(L("(no change)"));
	}

	protected void genSubOps(final MOB mob, final Area A, final int showNumber, final int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		String str="Q";
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(str.length()>0))
		{
			mob.tell(L("@x1. Area staff names: @x2",""+showNumber,A.getSubOpList()));
			if((showFlag!=showNumber)&&(showFlag>-999))
				return;
			str=mob.session().prompt(L("Enter a name to add or remove\n\r:"),"");
			if(str.length()>0)
			{
				if(A.amISubOp(str))
				{
					A.delSubOp(str);
					mob.tell(L("Staff removed."));
				}
				else
				if(CMLib.players().playerExists(str))
				{
					A.addSubOp(str);
					mob.tell(L("Staff added."));
				}
				else
					mob.tell(L("'@x1' is not recognized as a valid user name.",str));
			}
		}
	}

	protected void genParentAreas(final MOB mob, final Area A, final int showNumber, final int showFlag, final Set<Area> alsoUpdateAreas)
			throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		String newArea="Q";
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(newArea.length()>0))
		{
			final StringBuilder str=new StringBuilder("");
			for(final Enumeration<Area> a = A.getParents(); a.hasMoreElements(); )
				str.append(a.nextElement().Name()).append(";");
			mob.tell(L("@x1. Parent Areas: @x2",""+showNumber,str.toString()));
			if((showFlag!=showNumber)&&(showFlag>-999))
				return;
			newArea=mob.session().prompt(L("Enter an area name to add or remove\n\r:"),"");
			if(newArea.equalsIgnoreCase("*clear*")
			||((newArea.equalsIgnoreCase("all"))&&(CMLib.map().getArea("map")==null)))
			{
				final List<Area> allParents=new LinkedList<Area>();
				for(final Enumeration<Area> e=A.getParents();e.hasMoreElements();)
					allParents.add(e.nextElement());
				for(final Area a : allParents)
				{
					A.removeParent(a);
					a.removeChild(A);
					alsoUpdateAreas.add(a);
				}
			}
			else
			if(newArea.length()>0)
			{
				final Area lookedUp=CMLib.map().getArea(newArea);
				if(lookedUp!=null)
				{
					if (lookedUp.isChild(A))
					{
						// this new area is already a parent to A,
						// they must want it removed
						A.removeParent(lookedUp);
						lookedUp.removeChild(A);
						alsoUpdateAreas.add(lookedUp);
						mob.tell(L("Area '@x1' removed.",lookedUp.Name()));
					}
					else
					{
						if(A.canParent(lookedUp))
						{
							A.addParent(lookedUp);
							lookedUp.addChild(A);
							alsoUpdateAreas.add(lookedUp);
							mob.tell(L("Area '@x1' added.",lookedUp.Name()));
						}
						else
						{
							mob.tell(L("Area '@x1' cannot be added because this would create a circular reference.",lookedUp.Name()));
						}
					}
				}
				else
					mob.tell(L("'@x1' is not recognized as a valid area name.",newArea));
			}
		}
	}

	protected void genChildAreas(final MOB mob, final Area A, final int showNumber, final int showFlag, final Set<Area> alsoUpdateAreas)
			throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		String newArea="Q";
		final Area defaultParentArea=CMLib.map().getDefaultParentArea();
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(newArea.length()>0))
		{
			final StringBuilder str=new StringBuilder("");
			if(defaultParentArea!=A)
			{
				for(final Enumeration<Area> a = A.getChildren(); a.hasMoreElements(); )
					str.append(a.nextElement().Name()).append(";");
			}
			else
				str.append(L("* This is the Default parent area *"));
			mob.tell(L("@x1. Children areas: @x2",""+showNumber,str.toString()));
			if((showFlag!=showNumber)&&(showFlag>-999))
				return;
			newArea=mob.session().prompt(L("Enter an area name to add or remove\n\r:"),"");
			if((newArea.equalsIgnoreCase("*clear*"))
			||((newArea.equalsIgnoreCase("all"))&&(CMLib.map().getArea("map")==null)))
			{
				final List<Area> allChildren=new LinkedList<Area>();
				for(final Enumeration<Area> e=A.getChildren();e.hasMoreElements();)
					allChildren.add(e.nextElement());
				for(final Area a : allChildren)
				{
					A.removeChild(a);
					a.removeParent(A);
					alsoUpdateAreas.add(a);
				}
			}
			else
			if(newArea.length()>0)
			{
				final Area lookedUp=CMLib.map().getArea(newArea);
				if(lookedUp!=null)
				{
					if (lookedUp.isParent(A))
					{
						// this area is already a child to A, they must want it removed
						A.removeChild(lookedUp);
						lookedUp.removeParent(A);
						alsoUpdateAreas.add(lookedUp);
						mob.tell(L("Area '@x1' removed.",lookedUp.Name()));
					}
					else
					{
						if(A.canChild(lookedUp))
						{
							A.addChild(lookedUp);
							lookedUp.addParent(A);
							alsoUpdateAreas.add(lookedUp);
							mob.tell(L("Area '@x1' added.",lookedUp.Name()));
						}
						else
						{
							mob.tell(L("Area '@x1' cannot be added because this would create a circular reference.",lookedUp.Name()));
						}
					}
				}
				else
					mob.tell(L("'@x1' is not recognized as a valid area name.",newArea));
			}
		}
	}

	protected void genCloseWord(final MOB mob, final Exit E, final int showNumber, final int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		mob.tell(L("@x1. Close Word: '@x2'.",""+showNumber,E.closeWord()));
		if((showFlag!=showNumber)&&(showFlag>-999))
			return;
		final String newName=mob.session().prompt(L("Enter something new\n\r:"),"");
		if(newName.length()>0)
			E.setExitParams(E.doorName(),newName,E.openWord(),E.closedText());
		else
			mob.tell(L("(no change)"));
	}

	protected void genExitMisc(final MOB mob, final Exit E, final int showNumber, final int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		if(E.hasALock())
		{
			E.setReadable(false);
			mob.tell(L("@x1. Assigned Key Item: '@x2'.",""+showNumber,E.keyName()));
			if((showFlag!=showNumber)&&(showFlag>-999))
				return;
			final String newName=mob.session().prompt(L("Enter something new (null=blank)\n\r:"),"");
			if(newName.equalsIgnoreCase("null"))
				E.setKeyName("");
			else
			if(newName.length()>0)
				E.setKeyName(newName);
			else
				mob.tell(L("(no change)"));
		}
		else
		{
			if((showFlag!=showNumber)&&(showFlag>-999))
			{
				if(!E.isReadable())
					mob.tell(L("@x1. Door not is readable.",""+showNumber));
				else
					mob.tell(L("@x1. Door is readable: @x2",""+showNumber,E.readableText()));
				return;
			}
			else
			if(genGenericPrompt(mob,"Is this door readable ",E.isReadable()))
			{
				E.setReadable(true);
				mob.tell(L("\n\rText: '@x1'.",E.readableText()));
				final String newName=mob.session().prompt(L("Enter something new (null=blank)\n\r:"),"");
				if(newName.equalsIgnoreCase("null"))
					E.setReadableText("");
				else
				if(newName.length()>0)
					E.setReadableText(newName);
				else
					mob.tell(L("(no change)"));
			}
			else
				E.setReadable(false);
		}
	}

	protected void genIsReadable(final MOB mob, final Item E, final int showNumber, final int showFlag)
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;

		if(E instanceof Computer)
			return;

		if((E instanceof Wand)
		||(E instanceof SpellHolder)
		||(E instanceof Light)
		||(E instanceof Container)
		||(E instanceof Ammunition)
		||((E instanceof ClanItem)
			&&((((ClanItem)E).getClanItemType()==ClanItem.ClanItemType.GATHERITEM)
				||(((ClanItem)E).getClanItemType()==ClanItem.ClanItemType.CRAFTITEM)
				||(((ClanItem)E).getClanItemType()==ClanItem.ClanItemType.SPECIALAPRON)))
		||(E instanceof DoorKey))
			CMLib.flags().setReadable(E,false);
		else
		if((CMClass.classID(E).endsWith("Readable"))
		||(E instanceof Recipes)
		||(E instanceof com.planet_ink.coffee_mud.Items.interfaces.RoomMap))
			CMLib.flags().setReadable(E,true);
		else
		if((showFlag!=showNumber)&&(showFlag>-999))
			mob.tell(L("@x1. Item is readable: @x2",""+showNumber,""+E.isReadable()));
		else
			CMLib.flags().setReadable(E,genGenericPrompt(mob,showNumber+". Is this item readable",E.isReadable()));
	}

	protected void genDrinkType(final MOB mob, final LiquidHolder E, final int showNumber, final int showFlag) throws IOException
	{
		mob.session().println(L("@x1. Current liquid type: @x2",""+showNumber,RawMaterial.CODES.NAME(E.liquidType())));
		if((showFlag!=showNumber)&&(showFlag>-999))
			return;
		boolean q=false;
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(!q))
		{
			final String newType=mob.session().prompt(L("Enter a new type (?)\n\r:"),RawMaterial.CODES.NAME(E.liquidType()));
			if(newType.equals("?"))
			{
				final StringBuilder say=new StringBuilder("");
				final List<Integer> liquids = RawMaterial.CODES.COMPOSE_RESOURCES(RawMaterial.MATERIAL_LIQUID);
				for(final Integer code : liquids)
					say.append(RawMaterial.CODES.NAME(code.intValue())+", ");
				mob.tell(say.toString().substring(0,say.length()-2));
				q=false;
			}
			else
			{
				q=true;
				int newValue=RawMaterial.CODES.FIND_IgnoreCase(newType);
				if((newValue&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_LIQUID)
					newValue=-1;
				if(newValue>=0)
					E.setLiquidType(newValue);
				else
					mob.tell(L("(no change)"));
			}
		}
	}

	protected String genAbilityID(final MOB mob, String oldID, final int showNumber, final int showFlag, final String fieldDisp, final boolean emptyOK)
			throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return oldID;
		boolean ok=false;
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(!ok))
		{
			ok=true;
			mob.tell(L("@x1. @x2: '@x3'.",""+showNumber,fieldDisp,oldID));
			if((showFlag!=showNumber)&&(showFlag>-999))
				return oldID;
			final String newName=mob.session().prompt(L("Enter something new (?)\n\r:"),"");
			if(newName.length()==0)
			{
				mob.tell(L("(no change)"));
				return oldID;
			}
			else
			if(newName.equalsIgnoreCase("?"))
			{
				if(emptyOK)
					mob.tell("\n\rNULL=No ability.\n\r");
				mob.tell(CMLib.lister().build3ColTable(mob,CMClass.abilities(),-1).toString());
			}
			else
			if(newName.equalsIgnoreCase("null") && (emptyOK))
				oldID="";
			else
			{
				final Ability A=CMClass.getAbility(newName);
				if((A==null)||((A.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_ARCHON))
				{
					mob.tell(L("'@x1' is not recognized.  Try '?'.",newName));
					ok=false;
				}
				else
					oldID = A.ID();
			}
		}
		return oldID;
	}

	protected void genReadableTextMisc(final MOB mob, final Item E, final int showNumber, final int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;

		if(E instanceof Computer)
			return;

		if(E.isReadable()
		||(E instanceof SpellHolder)
		||(E instanceof Ammunition)
		||(E instanceof Recipes)
		||(E instanceof Exit)
		||(E instanceof Wand)
		||(E instanceof ClanItem)
		||(E instanceof Light)
		||(E instanceof DoorKey))
		{
			boolean ok=false;
			while((mob.session()!=null)&&(!mob.session().isStopped())&&(!ok))
			{
				if(CMClass.classID(E).endsWith("SuperPill"))
				{
					mob.tell(L("@x1. Assigned Spell or Parameters: '@x2'.",""+showNumber,E.readableText()));
					ok=true;
				}
				else
				if(E instanceof SpellHolder)
					mob.tell(L("@x1. Assigned Spell(s) ( ';' delimited)\n: '@x2'.",""+showNumber,E.readableText()));
				else
				if(E instanceof Ammunition)
				{
					mob.tell(L("@x1. Ammunition type: '@x2'.",""+showNumber,E.readableText()));
					ok=true;
				}
				else
				if(E instanceof Exit)
				{
					mob.tell(L("@x1. Assigned Room IDs: '@x2'.",""+showNumber,E.readableText()));
					ok=true;
				}
				else
				if(E instanceof Wand)
				{
					final Wand W=(Wand)E;
					mob.tell(L("@x1. Assigned Spell Name: '@x2'.",""+showNumber,(W.getSpell()!=null)?W.getSpell().ID():""));
				}
				else
				if(E instanceof DoorKey)
				{
					mob.tell(L("@x1. Assigned Key Code: '@x2'.",""+showNumber,E.readableText()));
					ok=true;
				}
				else
				if(E instanceof com.planet_ink.coffee_mud.Items.interfaces.RoomMap)
				{
					mob.tell(L("@x1. Assigned Map Area(s): '@x2'.",""+showNumber,E.readableText()));
					ok=true;
				}
				else
				if(E instanceof Light)
				{
					mob.tell(L("@x1. Light duration (before burn out): '@x2'.",""+showNumber,""+CMath.s_int(E.readableText())));
					ok=true;
				}
				else
				if(E instanceof LandTitle)
				{
					mob.tell(L("@x1. Assigned Property ID: '@x2'.",""+showNumber,E.readableText()));
					ok=true;
				}
				else
				{
					mob.tell(L("@x1. Assigned Read Text: '@x2'.",""+showNumber,E.readableText()));
					ok=true;
				}

				if((showFlag!=showNumber)&&(showFlag>-999))
					return;
				String newName=null;

				if((E instanceof Wand)
				||((E instanceof SpellHolder)&&(!(CMClass.classID(E).endsWith("SuperPill")))))
				{
					newName=mob.session().prompt(L("Enter something new (?)\n\r:"),"");
					if(newName.length()==0)
						ok=true;
					else
					{
						final int ofType=(E instanceof Wand)?((Wand)E).getEnchantType():-1;
						if(newName.equalsIgnoreCase("?"))
							mob.tell(CMLib.lister().build3ColTable(mob,CMClass.abilities(),ofType).toString());
						else
						if(E instanceof Wand)
						{
							final Ability A=CMClass.getAbility(newName);
							if((A==null)
							||(((A.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_ARCHON)
								&&(mob.fetchAbility(A.ID())==null))
							||((ofType>0)
								&&((A.classificationCode()&Ability.ALL_ACODES)!=ofType)))
							{
								mob.tell(L("'@x1' is not recognized.  Try '?'.",newName));
							}
							else
								ok=true;
						}
						else
						if(E instanceof SpellHolder)
						{
							final String oldName=newName;
							if(!newName.endsWith(";"))
								newName+=";";
							int x=newName.indexOf(';');
							while(x>=0)
							{
								String spellName=newName.substring(0,x).trim();
								final int x1=spellName.indexOf('(');
								if((x1>0)&&(spellName.endsWith(")")))
									spellName=spellName.substring(0,x1);
								final Ability A=CMClass.getAbility(spellName);
								if((A==null)
								||(((A.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_ARCHON)
									&&(mob.fetchAbility(A.ID())==null)))
								{
									mob.tell(L("'@x1' is not recognized.  Try '?'.",spellName));
									break;
								}
								else
									ok=true;
								newName=newName.substring(x+1).trim();
								x=newName.indexOf(';');
							}
							newName=oldName;
						}
					}
				}
				else
					newName=mob.session().prompt(L("Enter something new (null=blank)\n\r:"),"");

				if(ok)
				{
					if(newName.equalsIgnoreCase("null"))
						E.setReadableText("");
					else
					if(newName.length()>0)
						E.setReadableText(newName);
					else
						mob.tell(L("(no change)"));
				}
			}
		}
		else
		if(E instanceof LiquidHolder)
		{
			genDrinkType(mob,(LiquidHolder)E,showNumber,showFlag);
		}
	}

	protected void genRecipe(final MOB mob, final RecipesBook E, final int showNumber, final int showFlag)
	throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		StringBuilder str=new StringBuilder(showNumber+". Recipe Data: "+E.getCommonSkillID()+" ("+E.getTotalRecipePages()+"): ");
		for(final String line : E.getRecipeCodeLines())
		{
			final int x=line.indexOf('\t');
			final int len=line.length()>10?10:line.length();
			str.append(line.substring(0,(x<0)?len:x)).append(' ');
		}
		mob.tell(str.toString());
		final Session S=mob.session();
		if((S==null)||((showFlag!=showNumber)&&(showFlag>-999)))
			return;
		while(!S.isStopped())
		{
			final String newName=S.prompt(L("Enter new skill id (?)\n\r:"),"");
			if(newName.equalsIgnoreCase("?"))
			{
				str=new StringBuilder("");
				Ability A=null;
				for(final Enumeration<Ability> e=CMClass.abilities();e.hasMoreElements();)
				{
					A=e.nextElement();
					if(((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_COMMON_SKILL)
					&&(((A.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_CRAFTINGSKILL)
						||((A.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_EPICUREAN)
						||((A.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_BUILDINGSKILL)))
						str.append(A.ID()+"\n\r");
				}
				mob.tell(L("\n\rCommon Skills:\n\r@x1\n\r",str.toString()));
			}
			else
			if((newName.length()>0)
			&&(CMClass.getAbility(newName)!=null)
			&&((CMClass.getAbility(newName).classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_COMMON_SKILL))
			{
				E.setCommonSkillID(CMClass.getAbility(newName).ID());
				break;
			}
			else
			if(newName.length()>0)
				mob.tell(L("'@x1' is not a valid common skill.  Try ?.",newName));
			else
			{
				mob.tell(L("(no change)"));
				break;
			}
		}
		final String newCount=mob.session().prompt(L("Enter new maximum recipe count (@x1):",""+E.getTotalRecipePages()),"");
		if((newCount.length()>0)&&(CMath.s_int(newCount)>0))
			E.setTotalRecipePages(CMath.s_int(newCount));
		else
			mob.tell(L("(no change)"));
		final Ability A=CMClass.getAbility(E.getCommonSkillID());
		final RecipeDriven C;
		if((A!=null)
		&&((A.classificationCode()==(Ability.ACODE_COMMON_SKILL|Ability.DOMAIN_CRAFTINGSKILL))
			||(A.classificationCode()==(Ability.ACODE_COMMON_SKILL|Ability.DOMAIN_EPICUREAN))
			||(A.classificationCode()==(Ability.ACODE_COMMON_SKILL|Ability.DOMAIN_BUILDINGSKILL)))
		&&(A instanceof RecipeDriven))
		{
			C=(RecipeDriven)A;
			mob.tell(L("Params: @x1",CMStrings.replaceAll(C.getRecipeFormat(), "\t", ",")));
		}
		else
			C = null;
		while(!S.isStopped())
		{
			final String[] recipes = E.getRecipeCodeLines();
			str=new StringBuilder("");
			for(int i=1;i<=recipes.length;i++)
				str.append(i+") "+CMStrings.replaceAll(recipes[i-1],"\t",",")).append("\n");
			if(recipes.length<E.getTotalRecipePages())
				str.append(L("(@x1) ADD NEW RECIPE",""+(recipes.length+1))).append("\n");
			if(mob.session()!=null)
				mob.session().rawPrint(str.toString());
			final String newName=mob.session().prompt(L("Enter a number to add/edit/remove\n\r:"),"");
			final int x=CMath.s_int(newName);
			if((x<=0)||(x>E.getTotalRecipePages()))
				break;
			final List<String> recipeList = new XVector<String>(recipes);
			class Checker
			{
				public String getErrors(final String line)
				{
					if((C==null)&&(E.getCommonSkillID()!=null)&&(E.getCommonSkillID().length()>0))
						return L("Skill @x1 is not a crafting skill!",E.getCommonSkillID());
					try
					{
						if(C!=null)
							CMLib.ableParms().testRecipeParsing(new StringBuffer(CMStrings.replaceAll(line,",","\t")), C.getRecipeFormat());
					}
					catch(final CMException cme)
					{
						return cme.getMessage();
					}
					return null;
				}
			}
			if(x<=recipes.length)
			{
				final String newLine=mob.session().prompt(L("Re-Enter this line, or NULL to delete (?).\n\r:"),"");
				if(newLine.equalsIgnoreCase("?"))
					mob.tell((C==null)?"?":CMStrings.replaceAll(C.getRecipeFormat(), "\t", ","));
				else
				if(newLine.equalsIgnoreCase("null"))
					recipeList.remove(x-1);
				else
				if(newLine.length()==0)
					mob.tell(L("(No change)"));
				else
				{
					final String errors = new Checker().getErrors(newLine);
					if((errors==null)||(errors.length()==0))
						recipeList.set(x-1, CMStrings.replaceAll(newLine,",","\t"));
					else
						mob.tell(L("Error: @x1, aborting change.",errors));
				}
			}
			else
			{
				final String newLine=mob.session().prompt(L("Enter a new line, or enter to cancel (?).\n\r:"),"");
				if((newLine!=null)&&(newLine.trim().length()>0))
				{
					if(newLine.equalsIgnoreCase("?"))
						mob.tell((C==null)?"?":CMStrings.replaceAll(C.getRecipeFormat(), "\t", ","));
					else
					{
						final String errors = new Checker().getErrors(newLine);
						if((errors==null)||(errors.length()==0))
							recipeList.add(CMStrings.replaceAll(newLine,",","\t"));
						else
							mob.tell(L("Error: @x1, aborting change.",errors));
					}
				}
				else
					mob.tell(L("(No change)"));
			}
			E.setRecipeCodeLines(recipeList.toArray(new String[0]));
		}
	}

	protected void genGettable(final MOB mob, final Item I, final int showNumber, final int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		if(I instanceof Potion)
			((Potion)I).setDrunk(false);

		String c="Q";
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(!c.equals("\n")))
		{
			mob.session().println(L("@x1. A) Is Gettable   : @x2",""+showNumber,""+(!CMath.bset(I.basePhyStats().sensesMask(),PhyStats.SENSE_ITEMNOTGET))));
			mob.session().println(L("    B) Is Droppable  : @x1",""+(!CMath.bset(I.basePhyStats().sensesMask(),PhyStats.SENSE_ITEMNODROP))));
			mob.session().println(L("    C) Is Removable  : @x1",""+(!CMath.bset(I.basePhyStats().sensesMask(),PhyStats.SENSE_ITEMNOREMOVE))));
			mob.session().println(L("    D) Non-Locatable : @x1",(((I.basePhyStats().sensesMask()&PhyStats.SENSE_UNLOCATABLE)>0)?"true":"false")));
			mob.session().println(L("    E) Blend Display : @x1",(((I.basePhyStats().sensesMask()&PhyStats.SENSE_ALWAYSCOMPRESSED)>0)?"true":"false")));
			mob.session().println(L("    F) Semi-Hidden   : @x1",(((I.basePhyStats().sensesMask()&PhyStats.SENSE_HIDDENINPLAINSIGHT)>0)?"true":"false")));
			if(I instanceof Container)
				mob.session().println(L("    G) Contents Acces: @x1",(((I.basePhyStats().sensesMask()&PhyStats.SENSE_INSIDEACCESSIBLE)>0)?"true":"false")));
			else
			if(I instanceof Weapon)
				mob.session().println(L("    G) Is Two-Handed : @x1",""+I.rawLogicalAnd()));
			if((showFlag!=showNumber)&&(showFlag>-999))
				return;
			c=mob.session().choose(L("Enter one to change, or ENTER when done:"),L("ABCDEFG\n"),"\n").toUpperCase();
			switch(Character.toUpperCase(c.charAt(0)))
			{
			case 'A':
				CMLib.flags().setGettable(I, (CMath.bset(I.basePhyStats().sensesMask(), PhyStats.SENSE_ITEMNOTGET)));
				break;
			case 'B':
				CMLib.flags().setDroppable(I, (CMath.bset(I.basePhyStats().sensesMask(), PhyStats.SENSE_ITEMNODROP)));
				break;
			case 'C':
				CMLib.flags().setRemovable(I, (CMath.bset(I.basePhyStats().sensesMask(), PhyStats.SENSE_ITEMNOREMOVE)));
				break;
			case 'D':
			{
				if((I.basePhyStats().sensesMask()&PhyStats.SENSE_UNLOCATABLE)>0)
					I.basePhyStats().setSensesMask(I.basePhyStats().sensesMask()-PhyStats.SENSE_UNLOCATABLE);
				else
					I.basePhyStats().setSensesMask(I.basePhyStats().sensesMask()|PhyStats.SENSE_UNLOCATABLE);
				break;
			}
			case 'E':
			{
				if((I.basePhyStats().sensesMask()&PhyStats.SENSE_ALWAYSCOMPRESSED)>0)
					I.basePhyStats().setSensesMask(I.basePhyStats().sensesMask()-PhyStats.SENSE_ALWAYSCOMPRESSED);
				else
					I.basePhyStats().setSensesMask(I.basePhyStats().sensesMask()|PhyStats.SENSE_ALWAYSCOMPRESSED);
				break;
			}
			case 'F':
			{
				if((I.basePhyStats().sensesMask()&PhyStats.SENSE_HIDDENINPLAINSIGHT)>0)
					I.basePhyStats().setSensesMask(I.basePhyStats().sensesMask()-PhyStats.SENSE_HIDDENINPLAINSIGHT);
				else
					I.basePhyStats().setSensesMask(I.basePhyStats().sensesMask()|PhyStats.SENSE_HIDDENINPLAINSIGHT);
				break;
			}
			case 'G':
			{
				if(I instanceof Container)
				{
					if((I.basePhyStats().sensesMask()&PhyStats.SENSE_INSIDEACCESSIBLE)>0)
						I.basePhyStats().setSensesMask(I.basePhyStats().sensesMask()-PhyStats.SENSE_INSIDEACCESSIBLE);
					else
						I.basePhyStats().setSensesMask(I.basePhyStats().sensesMask()|PhyStats.SENSE_INSIDEACCESSIBLE);
					break;
				}
				else
				if(I instanceof Weapon)
					I.setRawLogicalAnd(!I.rawLogicalAnd());
				break;
			}
			}
		}
	}

	protected void toggleDispositionMask(final PhyStats E, final int mask)
	{
		final int current=E.disposition();
		if((current&mask)==0)
			E.setDisposition(current|mask);
		else
			E.setDisposition(current&((int)(PhyStats.ALLMASK-mask)));
	}

	protected void genDisposition(final MOB mob, final PhyStats E, final int showNumber, final int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		final int[] disps={PhyStats.IS_INVISIBLE,
					 PhyStats.IS_HIDDEN,
					 PhyStats.IS_NOT_SEEN,
					 PhyStats.IS_BONUS,
					 PhyStats.IS_GLOWING,
					 PhyStats.IS_LIGHTSOURCE,
					 PhyStats.IS_FLYING,
					 PhyStats.IS_CLIMBING,
					 PhyStats.IS_SLEEPING,
					 PhyStats.IS_SITTING,
					 PhyStats.IS_SNEAKING,
					 PhyStats.IS_SWIMMING,
					 PhyStats.IS_EVIL,
					 PhyStats.IS_GOOD,
					 PhyStats.IS_UNATTACKABLE};
		final String[] briefs={"invisible",
						 "hide",
						 "unseen",
						 "magical",
						 "glowing",
						 "lightsrc",
						 "fly",
						 "climb",
						 "sleep",
						 "sit",
						 "sneak",
						 "swimmer",
						 "evil",
						 "good",
						 "unattackable"};
		if((showFlag!=showNumber)&&(showFlag>-999))
		{
			final StringBuilder buf=new StringBuilder(showNumber+". Dispositions: ");
			for(int i=0;i<disps.length;i++)
			{
				final int mask=disps[i];
				if((E.disposition()&mask)!=0)
					buf.append(briefs[i]+" ");
			}
			mob.tell(buf.toString());
			return;
		}
		String c="Q";
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(!c.equals("\n")))
		{
			char letter='A';
			String letters="";
			for (final int mask : disps)
			{
				for(int num=0;num<PhyStats.IS_DESCS.length;num++)
				{
					if(mask==CMath.pow(2,num))
					{
						mob.session().println("    "+letter+") "+CMStrings.padRight(PhyStats.IS_DESCS[num],20)+":"+((E.disposition()&mask)!=0));
						letters+=letter;
						break;
					}
				}
				letter++;
			}
			c=mob.session().choose(L("Enter one to change, or ENTER when done: "),letters+"\n","\n").toUpperCase();
			letter='A';
			for (final int mask : disps)
			{
				if(letter==Character.toUpperCase(c.charAt(0)))
				{
					toggleDispositionMask(E,mask);
					break;
				}
				letter++;
			}
		}
	}

	public boolean genGenericPrompt(final MOB mob, String prompt, final boolean val)
	{
		try
		{
			prompt=CMStrings.padRight(prompt,35);
			if(val)
				prompt+="(Y/n): ";
			else
				prompt+="(y/N): ";

			return mob.session().confirm(prompt,val?L("Y"):L("N"));
		}
		catch(final IOException e)
		{
			return val;
		}
	}

	protected void toggleSensesMask(final PhyStats E, final int mask)
	{
		final int current=E.sensesMask();
		if((current&mask)==0)
			E.setSensesMask(current|mask);
		else
			E.setSensesMask(current&((int)(PhyStats.ALLMASK-mask)));
	}

	protected void toggleClimateMask(final Places A, final int mask)
	{
		int current=A.getClimateTypeCode();
		if(current<0)
			current=0;
		if((current&mask)==0)
			A.setClimateType(current|mask);
		else
			A.setClimateType(current&((int)(PhyStats.ALLMASK-mask)));
	}

	protected void genClimateType(final MOB mob, final Places A, final int showNumber, final int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		if((showFlag!=showNumber)&&(showFlag>-999))
		{
			final List<String> conditions=new Vector<String>();
			if(A.getClimateTypeCode()==Places.CLIMASK_INHERIT)
				conditions.add("Inherited");
			else
			{
				if(CMath.bset(A.getClimateTypeCode(), Places.CLIMASK_WET))
					conditions.add("wet");
				if(CMath.bset(A.getClimateTypeCode(), Places.CLIMASK_HOT))
					conditions.add("hot");
				if(CMath.bset(A.getClimateTypeCode(), Places.CLIMASK_DRY))
					conditions.add("dry");
				if(CMath.bset(A.getClimateTypeCode(), Places.CLIMASK_VOID))
					conditions.add("void");
				if(CMath.bset(A.getClimateTypeCode(), Places.CLIMASK_COLD))
					conditions.add("cold");
				if(CMath.bset(A.getClimateTypeCode(), Places.CLIMASK_WINDY))
					conditions.add("windy");
				if(conditions.size()==0)
					conditions.add("normal");
			}
			mob.session().println(L("@x1. Climate: @x2",""+showNumber,
					CMLib.english().toEnglishStringList(conditions.toArray(new String[0]), true)));
			return;
		}
		String c="Q";
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(!c.equals("\n")))
		{
			mob.session().println(L("@x1. Climate:",""+showNumber));
			int type=A.getClimateTypeCode();
			mob.session().println(L("    I) Inherited        : @x1",""+(type==Places.CLIMASK_INHERIT)));
			if(type == Places.CLIMASK_INHERIT)
				type=0;
			mob.session().println(L("    R) Wet and Rainy    : @x1",""+((type&Places.CLIMASK_WET)>0)));
			mob.session().println(L("    H) Excessively hot  : @x1",""+((type&Places.CLIMASK_HOT)>0)));
			mob.session().println(L("    C) Excessively cold : @x1",""+((type&Places.CLIMASK_COLD)>0)));
			mob.session().println(L("    W) Very windy       : @x1",""+((type&Places.CLIMASK_WINDY)>0)));
			mob.session().println(L("    D) Very dry         : @x1",""+((type&Places.CLIMASK_DRY)>0)));
			mob.session().println(L("    V) Void             : @x1",""+((type&Places.CLIMASK_VOID)>0)));
			c=mob.session().choose(L("Enter one to change, or ENTER when done: "),L("RHCWDI\n"),"\n").toUpperCase();
			switch(c.charAt(0))
			{
			case 'I':
				A.setClimateType(Places.CLIMASK_INHERIT);
				break;
			case 'C':
				toggleClimateMask(A, Places.CLIMASK_COLD);
				break;
			case 'H':
				toggleClimateMask(A, Places.CLIMASK_HOT);
				break;
			case 'R':
				toggleClimateMask(A, Places.CLIMASK_WET);
				break;
			case 'W':
				toggleClimateMask(A, Places.CLIMASK_WINDY);
				break;
			case 'D':
				toggleClimateMask(A, Places.CLIMASK_DRY);
				break;
			case 'V':
				int current=A.getClimateTypeCode();
				if(current<0)
					current=0;
				if((current&Places.CLIMASK_VOID)==0)
					A.setClimateType(Places.CLIMASK_VOID);
				else
					A.setClimateType(Places.CLIMASK_NORMAL);
				break;
			}
		}
	}

	protected void genCharStats(final MOB mob, final CharStats E)
	throws IOException
	{
		String c="Q";
		final String commandStr="ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890!@#$%&*()=+-[]|{}_?,.;:~";
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(!c.equals("\n")))
		{
			for(final int i : CharStats.CODES.ALLCODES())
			{
				if(i!=CharStats.STAT_GENDER)
					mob.session().println("    "+commandStr.charAt(i)+") "+CMStrings.padRight(CharStats.CODES.DESC(i),20)+":"+((E.getStat(i))));
			}
			c=mob.session().choose(L("Enter one to change, or ENTER when done: "),commandStr.substring(0,CharStats.CODES.TOTAL())+"\n","\n").toUpperCase();
			final int num=commandStr.indexOf(c);
			if(num>=0)
			{
				final String newVal=mob.session().prompt(L("Enter a new value:  @x1 (@x2): ",CharStats.CODES.DESC(num),""+E.getStat(num)),"");
				if(((CMath.s_int(newVal)>0)||(newVal.trim().equals("0")))
				&&(num!=CharStats.STAT_GENDER))
					E.setStat(num,CMath.s_int(newVal));
				else
					mob.tell(L("(no change)"));
			}
		}
	}

	protected void genCommonBonus(final MOB mob, final PlayerStats pStats, final int showNumber, final int showFlag)
			throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		if((showFlag!=showNumber)&&(showFlag>-999))
		{
			final StringBuilder buf=new StringBuilder(showNumber+". New Char Bonuses: ");
			buf.append(" C:").append(pStats.getBonusCommonSkillLimits())
			   .append(" F:").append(pStats.getBonusCraftingSkillLimits())
			   .append(" G:").append(pStats.getBonusNonCraftingSkillLimits())
			   .append(" L:").append(pStats.getBonusLanguageLimits())
			   .append(" R:").append(pStats.getBonusCharStatPoints());
			mob.tell(buf.toString());
			return;
		}

		pStats.setBonusCommonSkillLimits(CMath.s_int(mob.session().prompt(L("*. Bonus Common Skills (@x1): ",""+pStats.getBonusCommonSkillLimits()),""+pStats.getBonusCommonSkillLimits())));
		pStats.setBonusCraftingSkillLimits(CMath.s_int(mob.session().prompt(L("*. Bonus Craft Skills (@x1): ",""+pStats.getBonusCraftingSkillLimits()),""+pStats.getBonusCraftingSkillLimits())));
		pStats.setBonusNonCraftingSkillLimits(CMath.s_int(mob.session().prompt(L("*. Bonus Gather Skills (@x1): ",""+pStats.getBonusNonCraftingSkillLimits()),""+pStats.getBonusNonCraftingSkillLimits())));
		pStats.setBonusLanguageLimits(CMath.s_int(mob.session().prompt(L("*. Bonus Languages (@x1): ",""+pStats.getBonusLanguageLimits()),""+pStats.getBonusLanguageLimits())));
		pStats.setBonusCharStatPoints(CMath.s_int(mob.session().prompt(L("*. Bonus Creation Pts (@x1): ",""+pStats.getBonusCharStatPoints()),""+pStats.getBonusCharStatPoints())));
	}

	protected void genCharStats(final MOB mob, final MOB E, final int showNumber, final int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		if((showFlag!=showNumber)&&(showFlag>-999))
		{
			final StringBuilder buf=new StringBuilder(showNumber+". Stats: ");
			for(final int i : CharStats.CODES.BASECODES())
				buf.append(CharStats.CODES.ABBR(i)+":"+E.baseCharStats().getStat(i)+" ");
			mob.tell(buf.toString());
			return;
		}
		String c="Q";
		final String commandStr="ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890!@#$%^&*()=+-[]{}|_;:<>,.?/";
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(!c.equals("\n")))
		{
			for(final int i : CharStats.CODES.ALLCODES())
			{
				if(i!=CharStats.STAT_GENDER)
					mob.session().println("    "+commandStr.charAt(i)+") "+CMStrings.padRight(CharStats.CODES.DESC(i),20)+":"+((E.baseCharStats().getStat(i))));
			}
			c=mob.session().choose(L("Enter one to change, or ENTER when done: "),commandStr.substring(0,CharStats.CODES.TOTAL())+"\n","\n").toUpperCase();
			final int num=commandStr.indexOf(c);
			if(num>=0)
			{
				final String newVal=mob.session().prompt(L("Enter a new value:  @x1 (@x2): ",CharStats.CODES.DESC(num),""+E.baseCharStats().getStat(num)),"");
				if(((CMath.s_int(newVal)>0)||(newVal.trim().equals("0")))
				&&(num!=CharStats.STAT_GENDER))
				{
					E.baseCharStats().setStat(num,CMath.s_int(newVal));
					if((num==CharStats.STAT_AGE)&&(E.playerStats()!=null)&&(E.playerStats().getBirthday()!=null))
					{
						final TimeClock C=CMLib.time().localClock(E.getStartRoom());
						E.playerStats().getBirthday()[PlayerStats.BIRTHDEX_YEAR]=C.getYear()-CMath.s_int(newVal);
					}
				}
				else
					mob.tell(L("(no change)"));
			}
		}
	}

	protected void genCharState(final MOB mob, final MOB E, final int showNumber, final int showFlag)
	throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		final CharState baseState=E.baseState();
		if((showFlag!=showNumber)&&(showFlag>-999))
		{
			final StringBuilder buf=new StringBuilder(showNumber+". State: ");
			for(final String i : baseState.getStatCodes())
				buf.append(i.substring(0,2)+":"+E.baseState().getStat(i)+" ");
			mob.tell(buf.toString());
			return;
		}
		String c="Q";
		final String commandStr="ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890!@#$%^&*()=+-";
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(!c.equals("\n")))
		{
			for(int i=0;i<baseState.getStatCodes().length;i++)
			{
				final String state=baseState.getStatCodes()[i];
				mob.session().println("    "+commandStr.charAt(i)+") "+CMStrings.padRight(state,20)+":"+((baseState.getStat(state))));
			}
			c=mob.session().choose(L("Enter one to change, or ENTER when done: "),commandStr.substring(0,baseState.getStatCodes().length)+"\n","\n").toUpperCase();
			final int num=commandStr.indexOf(c);
			if(num>=0)
			{
				final String state=baseState.getStatCodes()[num];
				final String newVal=mob.session().prompt(L("Enter a new value:  @x1 (@x2): ",state,baseState.getStat(state)),"");
				if(((CMath.s_int(newVal)>0)||(newVal.trim().equals("0"))))
				{
					baseState.setStat(state,Integer.toString(CMath.s_int(newVal)));
				}
				else
					mob.tell(L("(no change)"));
			}
		}
	}

	protected void genSensesMask(final MOB mob, final PhyStats E, final int showNumber, final int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		final int[] senses={
					  PhyStats.CAN_SEE_DARK,
					  PhyStats.CAN_SEE_HIDDEN,
					  PhyStats.CAN_SEE_INVISIBLE,
					  PhyStats.CAN_SEE_SNEAKERS,
					  PhyStats.CAN_SEE_INFRARED,
					  PhyStats.CAN_SEE_GOOD,
					  PhyStats.CAN_SEE_EVIL,
					  PhyStats.CAN_SEE_BONUS,
					  PhyStats.CAN_NOT_SPEAK,
					  PhyStats.CAN_NOT_HEAR,
					  PhyStats.CAN_NOT_SEE,
					  PhyStats.CAN_SEE_HIDDEN_ITEMS,
		};
		final String[] briefs={"darkvision",
								 "hidden",
								 "invisible",
								 "sneakers",
								 "infrared",
								 "good",
								 "evil",
								 "magic",
								 "mute",
								 "deaf",
								 "blind",
								 "items"};
		if((showFlag!=showNumber)&&(showFlag>-999))
		{
			final StringBuilder buf=new StringBuilder(showNumber+". Senses: ");
			for(int i=0;i<senses.length;i++)
			{
				final int mask=senses[i];
				if((E.sensesMask()&mask)!=0)
					buf.append(briefs[i]+" ");
			}
			mob.tell(buf.toString());
			return;
		}
		String c="Q";
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(!c.equals("\n")))
		{
			char letter='A';
			String letters="";
			for (final int mask : senses)
			{
				for(int num=0;num<PhyStats.CAN_SEE_DESCS.length;num++)
				{
					if(mask==CMath.pow(2,num))
					{
						letters+=letter;
						mob.session().println("    "+letter+") "+CMStrings.padRight(PhyStats.CAN_SEE_DESCS[num],20)+":"+((E.sensesMask()&mask)!=0));
						break;
					}
				}
				letter++;
			}
			c=mob.session().choose(L("Enter one to change, or ENTER when done: "),letters+"\n","\n").toUpperCase();
			letter='A';
			for (final int mask : senses)
			{
				if(letter==Character.toUpperCase(c.charAt(0)))
				{
					toggleSensesMask(E,mask);
					break;
				}
				letter++;
			}
		}
	}

	protected void genDoorsNLocks(final MOB mob, final CloseableLockable E, final String doorName, final int showNumber, final int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		boolean HasDoor=E.hasADoor();
		boolean Open=E.isOpen();
		boolean DefaultsClosed=E.defaultsClosed();
		boolean HasLock=E.hasALock();
		boolean Locked=E.isLocked();
		boolean DefaultsLocked=E.defaultsLocked();
		if((showFlag!=showNumber)&&(showFlag>-999))
		{
			mob.tell(L("@x1. Has a @x5: @x2\n\r   Has a lock  : @x3\n\r   Open ticks: @x4",""+showNumber,""+E.hasADoor(),""+E.hasALock(),""+E.openDelayTicks(),doorName));
			return;
		}

		if(genGenericPrompt(mob,"Has a "+doorName,E.hasADoor()))
		{
			HasDoor=true;
			DefaultsClosed=genGenericPrompt(mob,"Defaults closed",E.defaultsClosed());
			Open=!DefaultsClosed;
			if(genGenericPrompt(mob,"Has a lock",E.hasALock()))
			{
				HasLock=true;
				DefaultsLocked=genGenericPrompt(mob,"Defaults locked",E.defaultsLocked());
				Locked=DefaultsLocked;
			}
			else
			{
				HasLock=false;
				Locked=false;
				DefaultsLocked=false;
			}
			mob.tell(L("\n\rReset Delay (# ticks): '@x1'.",""+E.openDelayTicks()));
			final int newLevel=CMath.s_int(mob.session().prompt(L("Enter a new delay\n\r:"),""));
			if(newLevel>0)
				E.setOpenDelayTicks(newLevel);
			else
				mob.tell(L("(no change)"));
		}
		else
		{
			HasDoor=false;
			Open=true;
			DefaultsClosed=false;
			HasLock=false;
			Locked=false;
			DefaultsLocked=false;
		}
		E.setDoorsNLocks(HasDoor,Open,DefaultsClosed,HasLock,Locked,DefaultsLocked);
	}

	public String makeContainerTypes(final Container E)
	{
		String canContain=", "+Container.CONTAIN_DESCS[0];
		if(E.containTypes()>0)
		{
			canContain="";
			for(int i=0;i<Container.CONTAIN_DESCS.length-1;i++)
				if(CMath.isSet((int)E.containTypes(),i))
					canContain+=", "+Container.CONTAIN_DESCS[i+1];
		}
		return canContain.substring(2);
	}

	protected void genContainerTypes(final MOB mob, final Container E, final int showNumber, final int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		if((showFlag!=showNumber)&&(showFlag>-999))
		{
			mob.tell(L("@x1. Can contain : @x2",""+showNumber,makeContainerTypes(E))); // portals use keys as roomids, showing is OK
			return;
		}
		String change="NO";
		if(!(E instanceof Electronics))
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(change.length()>0))
		{
			mob.tell(L("\n\rCan only contain: @x1",makeContainerTypes(E)));
			change=mob.session().prompt(L("Enter a type to add/remove (?)\n\r:"),"");
			if(change.length()==0)
				break;
			int found=-1;
			if(change.equalsIgnoreCase("?"))
			{
				for (final String element : Container.CONTAIN_DESCS)
					mob.tell(element);
			}
			else
			{
				for(int i=0;i<Container.CONTAIN_DESCS.length;i++)
				{
					if(Container.CONTAIN_DESCS[i].startsWith(change.toUpperCase()))
						found=i;
				}
				if(found<0)
					mob.tell(L("Unknown type.  Try '?'."));
				else
				if(found==0)
					E.setContainTypes(0);
				else
				if(CMath.isSet((int)E.containTypes(),found-1))
					E.setContainTypes(E.containTypes()-CMath.pow(2,found-1));
				else
					E.setContainTypes(E.containTypes()|CMath.pow(2,found-1));
			}
		}
	}

	protected void genLevel(final MOB mob, final Physical P, final int showNumber, final int showFlag) throws IOException
	{
		if(P.basePhyStats().level()<0)
			P.basePhyStats().setLevel(1);
		P.basePhyStats().setLevel(prompt(mob,P.basePhyStats().level(),showNumber,showFlag,"Level"));
	}

	protected void genRejuv(final MOB mob, final Physical P, final int showNumber, final int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		String rejuvStr = ""+P.basePhyStats().rejuv();
		if((P.basePhyStats().rejuv()==0)||(P.basePhyStats().rejuv()==PhyStats.NO_REJUV))
			rejuvStr = "0";
		if(P instanceof Item)
		{
			if((((Item)P).owner() instanceof MOB)&&(((MOB)((Item)P).owner()).isMonster()))
				mob.tell(L("@x1. Rejuv/Pct: '@x2' (0=normal, -1=one time).",""+showNumber,rejuvStr));
			else
				mob.tell(L("@x1. Rejuv/Pct: '@x2' (0=never).",""+showNumber,rejuvStr));
		}
		else
			mob.tell(L("@x1. Rejuv Ticks: '@x2' (0=never).",""+showNumber,rejuvStr));
		if((showFlag!=showNumber)&&(showFlag>-999))
			return;
		final String rlevel=mob.session().prompt(L("Enter new amount\n\r:"),"");
		final int newLevel=CMath.s_int(rlevel);
		if((newLevel<0)
		&&((((Item)P).owner() instanceof MOB)&&(((MOB)((Item)P).owner()).isMonster())))
		{
			P.basePhyStats().setRejuv(PhyStats.ONE_JUV);
			mob.tell(L("@x1 will now exist only at boot.",P.Name()));
		}
		else
		if((newLevel<0)&&(rlevel.trim().startsWith("-")))
		{
			mob.tell(L("@x1 can only be set to exist at boot while in an npcs inventory.",P.Name()));
		}
		else
		if((newLevel>0)||(rlevel.trim().equals("0")))
		{
			P.basePhyStats().setRejuv(newLevel);
			if(((P.basePhyStats().rejuv()==0)||(P.basePhyStats().rejuv()==PhyStats.NO_REJUV))&&(P instanceof MOB))
			{
				P.basePhyStats().setRejuv(PhyStats.NO_REJUV);
				mob.tell(L("@x1 will now never rejuvenate.",P.Name()));
			}
		}
		else
			mob.tell(L("(no change)"));
	}

	protected void genUses(final MOB mob, final Item I, final int showNumber, final int showFlag) throws IOException
	{
		if(I instanceof Ammunition)
			I.setUsesRemaining(prompt(mob,((Ammunition)I).ammunitionRemaining(),showNumber,showFlag,"Ammo Remaining"));
		else
			I.setUsesRemaining(prompt(mob,I.usesRemaining(),showNumber,showFlag,"Uses Remaining"));
	}

	protected void genMaxCharges(final MOB mob, final Wand W, final int showNumber, final int showFlag) throws IOException
	{
		W.setMaxCharges(prompt(mob,W.getMaxCharges(),showNumber,showFlag,"Maximum Uses"));
	}

	protected void genCondition(final MOB mob, final Item I, final int showNumber, final int showFlag) throws IOException
	{
		I.setUsesRemaining(prompt(mob,I.usesRemaining(),showNumber,showFlag,"Condition"));
	}

	@Override
	public void genMiscSet(final MOB mob, final Environmental E, final int showFlag)
		throws IOException
	{
		try
		{
			if(E!=mob)
				CMLib.threads().suspendTicking(E, -1);
			if(CMLib.flags().isCataloged(E))
			{
				if(CMLib.catalog().isCatalogObj(E.Name()))
					mob.tell(L("*** This object is Cataloged **\n\r"));
				else
					mob.tell(L("*** This object WAS cataloged and is still tied **\n\r"));
			}

			if(E instanceof ShopKeeper)
				modifyGenShopkeeper(mob,(ShopKeeper)E,showFlag);
			else
			if(E instanceof MOB)
			{
				if(((MOB)E).playerStats()==null)
					modifyGenMOB(mob,(MOB)E,showFlag);
				else
					modifyPlayer(mob,(MOB)E,showFlag);
			}
			else
			if((E instanceof Exit)&&(!(E instanceof Item)))
				modifyGenExit(mob,(Exit)E,showFlag);
			else
			if(E instanceof com.planet_ink.coffee_mud.Items.interfaces.RoomMap)
				modifyGenMap(mob,(com.planet_ink.coffee_mud.Items.interfaces.RoomMap)E,showFlag);
			else
			if(E instanceof Armor)
				modifyGenArmor(mob,(Armor)E,showFlag);
			else
			if(E instanceof MusicalInstrument)
				modifyGenInstrument(mob,(MusicalInstrument)E,showFlag);
			else
			if(E instanceof Food)
				modifyGenFood(mob,(Food)E,showFlag);
			else
			if((E instanceof Drink)&&(E instanceof Item))
				modifyGenDrink(mob,(Drink)E,showFlag);
			else
			if(E instanceof Weapon)
				modifyGenWeapon(mob,(Weapon)E,showFlag);
			else
			if(E instanceof Container)
				modifyGenContainer(mob,(Container)E,showFlag);
			else
			if(E instanceof Item)
			{
				if(E.ID().equals("GenWallpaper"))
					modifyGenWallpaper(mob,(Item)E,showFlag);
				else
					modifyGenItem(mob,(Item)E,showFlag);
			}
			else
			if(E instanceof Room)
				modifyRoom(mob, (Room)E, showFlag);
			else
			if(E instanceof Area)
				modifyArea(mob, (Area)E, new HashSet<Area>(), showFlag);
			if((E instanceof Physical)&&(showFlag != -950))
				catalogCheckUpdate(mob, (Physical)E);
		}
		finally
		{
			if(E!=mob)
				CMLib.threads().resumeTicking(E, -1);
		}
	}

	@Override
	public void genMiscText(final MOB mob, final Environmental E, final int showNumber, final int showFlag)
		throws IOException
	{
		if(E.isGeneric())
			genMiscSet(mob,E,showFlag);
		else
		{
			E.setMiscText(prompt(mob, E.text(), showNumber, showFlag, "Misc Text", true, false));
		}
	}

	protected void genTitleRoom(final MOB mob, final LandTitle L, final int showNumber, final int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		mob.tell(L("@x1. Land plot ID: '@x2'.",""+showNumber,L.landPropertyID()));
		if((showFlag!=showNumber)&&(showFlag>-999))
			return;
		String newText="?!?!";
		while((mob.session()!=null)&&(!mob.session().isStopped())
			&&((newText.length()>0)&&(CMLib.map().getRoom(newText)==null)))
		{
			newText=mob.session().prompt(L("New Property ID:"),"");
			if((newText.length()==0)
			&&(CMLib.map().getRoom(newText)==null)
			&&(CMLib.map().getArea(newText)==null))
				mob.tell(L("That property (room ID) doesn't exist!"));
		}
		if(newText.length()>0)
			L.setLandPropertyID(newText);
		else
			mob.tell(L("(no change)"));

	}

	public void genAbility(final MOB mob, final Physical P, final int showNumber, final int showFlag, final String prompt) throws IOException
	{
		P.basePhyStats().setAbility(prompt(mob, P.basePhyStats().ability(), showNumber, showFlag, prompt));
	}

	@Override
	public void genAbility(final MOB mob, final Physical P, final int showNumber, final int showFlag) throws IOException
	{
		if(P instanceof Electronics)
			genAbility(mob,P,showNumber,showFlag,"Technical Level");
		else
			genAbility(mob,P,showNumber,showFlag,"Magical Ability");
	}

	protected void genCoinStuff(final MOB mob, final Coins I, final int showNumber, final int showFlag)
	throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		mob.tell(L("@x1. Money data: '@x2 x @x3'.",""+showNumber,""+I.getNumberOfCoins(),CMLib.beanCounter().getDenominationName(I.getCurrency(),I.getDenomination())));
		if((showFlag!=showNumber)&&(showFlag>-999))
			return;
		boolean gocontinue=true;
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(gocontinue))
		{
			gocontinue=false;
			String oldCurrency=I.getCurrency();
			if(oldCurrency.length()==0)
				oldCurrency="Default";
			oldCurrency=mob.session().prompt(L("Enter currency code (?):"),oldCurrency).trim().toUpperCase();
			if(oldCurrency.equalsIgnoreCase("Default"))
			{
				if(I.getCurrency().length()>0)
					I.setCurrency("");
				else
					mob.tell(L("(no change)"));
			}
			else
			if((oldCurrency.length()==0)||CMLib.beanCounter().isCurrencyMatch(oldCurrency,I.getCurrency()))
				mob.tell(L("(no change)"));
			else
			if(!CMLib.beanCounter().getAllCurrencies().contains(oldCurrency))
			{
				final List<String> V=CMLib.beanCounter().getAllCurrencies();
				for(int v=0;v<V.size();v++)
				{
					if(V.get(v).length()==0)
						V.set(v,"Default");
				}
				mob.tell(L("'@x1' is not a known currency. Existing currencies include: DEFAULT@x2",oldCurrency,CMParms.toListString(V)));
				gocontinue=true;
			}
			else
				I.setCurrency(oldCurrency.toUpperCase().trim());
		}
		gocontinue=true;
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(gocontinue))
		{
			gocontinue=false;
			String newDenom=mob.session().prompt(L("Enter denomination (?):"),""+I.getDenomination()).trim().toUpperCase();
			final MoneyLibrary.MoneyDefinition def=CMLib.beanCounter().getCurrencySet(I.getCurrency());
			if((newDenom.length()>0)
			&&(!CMath.isDouble(newDenom))
			&&(!newDenom.equalsIgnoreCase("?")))
			{
				final double denom=CMLib.english().matchAnyDenomination(I.getCurrency(),newDenom);
				if(denom>0.0)
					newDenom=""+denom;
			}
			if((newDenom.length()==0)
			||(CMath.isDouble(newDenom)
				&&(!newDenom.equalsIgnoreCase("?"))
				&&(CMath.s_double(newDenom)==I.getDenomination())))
					mob.tell(L("(no change)"));
			else
			if((newDenom.equalsIgnoreCase("?"))
			||(!CMath.isDouble(newDenom))
			||((def!=null)&&(CMLib.beanCounter().getDenominationIndex(I.getCurrency(), CMath.s_double(newDenom))<0)))
			{
				StringBuilder allDenoms=new StringBuilder("");
				if(def!=null)
				{
					final MoneyLibrary.MoneyDenomination[] DV=def.denominations();
					for (final MoneyDenomination element : DV)
						allDenoms.append(element.value()+"("+element.name()+"), ");
				}
				if(allDenoms.toString().endsWith(", "))
					allDenoms=new StringBuilder(allDenoms.substring(0,allDenoms.length()-2));
				mob.tell(L("'@x1' is not a defined denomination. Try one of these: @x2.",newDenom,allDenoms.toString()));
				gocontinue=true;
			}
			else
				I.setDenomination(CMath.s_double(newDenom));
		}
		if((mob.session()!=null)&&(!mob.session().isStopped()))
			I.setNumberOfCoins(CMath.s_int(mob.session().prompt(L("Enter stack size\n\r:"),""+I.getNumberOfCoins())));
	}

	protected void genHitPoints(final MOB mob, final MOB M, final int showNumber, final int showFlag) throws IOException
	{
		if(M.isMonster())
			M.basePhyStats().setAbility(prompt(mob,M.basePhyStats().ability(),showNumber,showFlag,"Hit Points Bonus Modifier","Hit points = (level*level) + (random*level*THIS)"));
		else
			M.basePhyStats().setAbility(prompt(mob,M.basePhyStats().ability(),showNumber,showFlag,"Ability -- unused"));
	}

	protected void genValue(final MOB mob, final Item I, final int showNumber, final int showFlag) throws IOException
	{
		I.setBaseValue(prompt(mob,I.baseGoldValue(),showNumber,showFlag,"Base Value"));
	}

	protected void genWeight(final MOB mob, final Physical P, final int showNumber, final int showFlag) throws IOException
	{
		P.basePhyStats().setWeight(prompt(mob,P.basePhyStats().weight(),showNumber,showFlag,"Weight"));
	}

	protected void genClanItem(final MOB mob, final ClanItem I, final int showNumber, final int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		mob.tell(L("@x1. Clan: '@x2', Type: @x3.",""+showNumber,I.clanID(),I.getClanItemType().toString()));
		if((showFlag!=showNumber)&&(showFlag>-999))
			return;
		final String clanID=I.clanID();
		I.setClanID(mob.session().prompt(L("Enter a new clan\n\r:"),clanID));
		if(I.clanID().equals(clanID))
			mob.tell(L("(no change)"));
		final String clanType=I.getClanItemType().toString();
		String s="?";
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(s.equals("?")))
		{
			s=mob.session().prompt(L("Enter a new type (?)\n\r:"),clanType);
			if(s.equalsIgnoreCase("?"))
				mob.tell(L("Types: @x1",CMParms.toListString(ClanItem.ClanItemType.ALL)));
			else
			if(s.equalsIgnoreCase(clanType))
			{
				mob.tell(L("(no change)"));
				break;
			}
			else
			{
				final ClanItemType type = ClanItemType.getValueOf(s);
				if(type != null)
				{
					I.setClanItemType(type);
				}
				else
				{
					mob.tell(L("'@x1' is unknown.  Try '?'",s));
					s="?";
				}
			}
		}
	}

	protected void genHeight(final MOB mob, final Physical P, final int showNumber, final int showFlag) throws IOException
	{
		P.basePhyStats().setHeight(prompt(mob, P.basePhyStats().height(), showNumber, showFlag, "Height"));
	}

	protected void genSize(final MOB mob, final Armor A, final int showNumber, final int showFlag) throws IOException
	{
		A.basePhyStats().setHeight(prompt(mob, A.basePhyStats().height(), showNumber, showFlag, "Size"));
	}

	@Override
	public void wornLayer(final MOB mob, final short[] layerAtt, final short[] clothingLayer, final int showNumber, final int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		final boolean seeThroughBool=CMath.bset(layerAtt[0],Armor.LAYERMASK_SEETHROUGH);
		final boolean multiWearBool=CMath.bset(layerAtt[0],Armor.LAYERMASK_MULTIWEAR);
		final String seeThroughStr=(!seeThroughBool)?" (opaque)":" (see-through)";
		final String multiWearStr=multiWearBool?" (multi)":"";
		mob.tell(L("@x1. Layer: '@x2'@x3@x4.",""+showNumber,""+clothingLayer[0],seeThroughStr,multiWearStr));
		if((showFlag!=showNumber)&&(showFlag>-999))
			return;
		if((mob.session()!=null)&&(!mob.session().isStopped()))
			clothingLayer[0] = CMath.s_short(mob.session().prompt(L("Enter a new layer\n\r:"),""+clothingLayer[0]));
		boolean newSeeThrough=seeThroughBool;
		if((mob.session()!=null)&&(!mob.session().isStopped()))
			newSeeThrough=mob.session().confirm(L("Is see-through (Y/N)? "),""+seeThroughBool);
		boolean multiWear=multiWearBool;
		if((mob.session()!=null)&&(!mob.session().isStopped()))
			multiWear=mob.session().confirm(L("Is multi-wear (Y/N)? "),""+multiWearBool);
		layerAtt[0] = (short)0;
		layerAtt[0] = (short)(layerAtt[0]|(newSeeThrough?Armor.LAYERMASK_SEETHROUGH:0));
		layerAtt[0] = (short)(layerAtt[0]|(multiWear?Armor.LAYERMASK_MULTIWEAR:0));
	}

	public void genTPQ(final MOB mob, final MOB me, final int showNumber, final int showFlag) throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		mob.tell(L("@x1. Trains: @x2, Pracs: @x3, Quest Pts: @x4",""+showNumber,""+me.getTrains(), ""+me.getPractices(), ""+me.getQuestPoint()));
		if((showFlag!=showNumber)&&(showFlag>-999))
			return;
		me.setTrains(CMath.s_int(mob.session().prompt(L("*. Training Sessions ("+me.getTrains()+"): ",""+me.getTrains())),me.getTrains()));
		me.setPractices(CMath.s_int(mob.session().prompt(L("*. Practice Points ("+me.getPractices()+"): ",""+me.getPractices())),me.getPractices()));
		me.setQuestPoint(CMath.s_int(mob.session().prompt(L("*. Quest Points ("+me.getQuestPoint()+"): ",""+me.getQuestPoint())),me.getQuestPoint()));
	}

	protected void genLayer(final MOB mob, final Armor E, final int showNumber, final int showFlag) throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		final short[] layerAtt = new short[]{E.getLayerAttributes()};
		final short[] clothingLayer = new short[]{E.getClothingLayer()};
		wornLayer(mob,layerAtt,clothingLayer,showNumber,showFlag);
		E.setClothingLayer(clothingLayer[0]);
		E.setLayerAttributes(layerAtt[0]);
	}

	protected void genCapacity(final MOB mob, final Container E, final int showNumber, final int showFlag) throws IOException
	{
		E.setCapacity(prompt(mob, E.capacity(), showNumber, showFlag, "Capacity"));
	}

	protected void genAttack(final MOB mob, final Physical P, final int showNumber, final int showFlag) throws IOException
	{
		P.basePhyStats().setAttackAdjustment(prompt(mob, P.basePhyStats().attackAdjustment(), showNumber, showFlag, "Attack Adjustment"));
	}

	protected void genDamage(final MOB mob, final Physical P, final int showNumber, final int showFlag) throws IOException
	{
		P.basePhyStats().setDamage(prompt(mob, P.basePhyStats().damage(), showNumber, showFlag, "Damage"));
	}

	protected void genBankerCoinInterest(final MOB mob, final Banker M, final int showNumber, final int showFlag) throws IOException
	{
		M.setCoinInterest(prompt(mob, M.getCoinInterest(), showNumber, showFlag, "Coin Interest [% per real day]"));
	}

	protected void genBankerItemInterest(final MOB mob, final Banker M, final int showNumber, final int showFlag) throws IOException
	{
		M.setItemInterest(prompt(mob, M.getItemInterest(), showNumber, showFlag, "Item Interest [% per real day]"));
	}

	protected void genBankerChain(final MOB mob, final Banker M, final int showNumber, final int showFlag) throws IOException
	{
		M.setBankChain(prompt(mob, M.bankChain(), showNumber, showFlag, "Bank Chain", false, false));
	}

	protected void genBankerLoanInterest(final MOB mob, final Banker M, final int showNumber, final int showFlag) throws IOException
	{
		M.setLoanInterest(prompt(mob, M.getLoanInterest(), showNumber, showFlag, "Loan Interest [% per mud month]"));
	}

	protected void genSpeed(final MOB mob, final Physical P, final int showNumber, final int showFlag) throws IOException
	{
		if(P instanceof SpaceObject)
		{
			if(P instanceof Weapon)
				P.basePhyStats().setSpeed(prompt(mob, P.basePhyStats().speed(), showNumber, showFlag, "Max Speed (% of Light)"));
		}
		else
			P.basePhyStats().setSpeed(prompt(mob, P.basePhyStats().speed(), showNumber, showFlag, "Actions/Attacks per tick"));
	}

	protected void genArmor(final MOB mob, final Physical P, final int showNumber, final int showFlag) throws IOException
	{
		if(P instanceof MOB)
			P.basePhyStats().setArmor(prompt(mob,P.basePhyStats().armor(),showNumber,showFlag,"Armor (lower-better)"));
		else
			P.basePhyStats().setArmor(prompt(mob,P.basePhyStats().armor(),showNumber,showFlag,"Armor (higher-better)"));
	}

	protected void genMoney(final MOB mob, final MOB M, final int showNumber, final int showFlag) throws IOException
	{
		if(M.getMoney()==0)
		{
			final double d=CMLib.beanCounter().getTotalAbsoluteNativeValue(M);
			CMLib.beanCounter().subtractMoney(M,d);
			M.setMoney((int)Math.round(d));
		}
		CMLib.beanCounter().setMoney(M,prompt(mob,M.getMoney(),showNumber,showFlag,"Money"));
	}

	protected void genWeaponAmmo(final MOB mob, final Weapon W, final int showNumber, final int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		if(!(W instanceof AmmunitionWeapon))
			return;
		final AmmunitionWeapon AW=(AmmunitionWeapon)W;
		final String defaultAmmo=(AW.requiresAmmunition())?"Y":"N";
		if((showFlag!=showNumber)&&(showFlag>-999))
		{
			mob.tell(L("@x1. Ammo required: @x2 (@x3/@x4)",""+showNumber,
					""+AW.requiresAmmunition(),
					""+AW.rawAmmunitionCapacity(),
					""+AW.ammunitionType()));
			return;
		}

		if(mob.session().confirm(L("Does this weapon require ammunition (default=@x1) (Y/N)?",defaultAmmo),defaultAmmo))
		{
			mob.tell(L("\n\rAmmo type: '@x1'.",AW.ammunitionType()));
			final String newName=mob.session().prompt(L("Enter a new one\n\r:"),"");
			if(newName.length()>0)
			{
				AW.setAmmunitionType(newName);
				mob.tell(L("(Remember to create a GenAmmunition item with '@x1' in the secret identity, and the uses remaining above 0!",AW.ammunitionType()));
			}
			else
				mob.tell(L("(no change)"));
			mob.tell(L("\n\rAmmo capacity: '@x1'.)",""+AW.rawAmmunitionCapacity()));
			final int newValue=CMath.s_int(mob.session().prompt(L("Enter a new value\n\r:"),""));
			if(newValue>0)
				AW.setAmmoCapacity(newValue);
			else
				mob.tell(L("(no change)"));
			AW.setAmmoRemaining(AW.rawAmmunitionCapacity());
		}
		else
		{
			AW.setAmmunitionType("");
			AW.setAmmoCapacity(0);
		}
	}

	protected void genWeaponRanges(final MOB mob, final Weapon W, final int showNumber, final int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		mob.tell(L("@x1. Minimum/Maximum Ranges: @x2/@x3.",""+showNumber,""+W.getRanges()[0],""+W.getRanges()[1]));
		if((showFlag!=showNumber)&&(showFlag>-999))
			return;
		final String newMinStr=mob.session().prompt(L("Enter a new minimum range\n\r:"),"");
		final String newMaxStr=mob.session().prompt(L("Enter a new maximum range\n\r:"),"");
		if((newMinStr.length()==0)&&(newMaxStr.length()==0))
			mob.tell(L("(no change)"));
		else
		{
			W.setRanges(CMath.s_int(newMinStr),CMath.s_int(newMaxStr));
			if((W.getRanges()[0]>W.getRanges()[1])||(W.getRanges()[0]<0)||(W.getRanges()[1]<0))
			{
				mob.tell(L("(defective entries.  resetting.)"));
				W.setRanges(0,0);
			}
		}
	}

	protected void genWeaponType(final MOB mob, final Weapon W, final int showNumber, final int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		mob.tell(L("@x1. Weapon Attack Type: '@x2'.",""+showNumber,Weapon.TYPE_DESCS[W.weaponDamageType()]));
		if((showFlag!=showNumber)&&(showFlag>-999))
			return;
		boolean q=false;
		final String sel="NSPBFMR";
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(!q))
		{
			final String newType=mob.session().choose(L("Enter a new value\n\r:"),sel+"?","");
			if(newType.equals("?"))
			{
				for(int i=0;i<sel.length();i++)
					mob.tell(sel.charAt(i)+") "+Weapon.TYPE_DESCS[i]);
				q=false;
			}
			else
			{
				q=true;
				int newValue=-1;
				if(newType.length()>0)
					newValue=sel.indexOf(newType.toUpperCase());
				if(newValue>=0)
					W.setWeaponDamageType(newValue);
				else
					mob.tell(L("(no change)"));
			}
		}
	}

	protected void getTheme(final MOB mob, final Area A, final int showNumber, final int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		mob.tell(L("@x1. Theme setting: '@x2'.",""+showNumber,Area.THEME_PHRASE[A.getThemeCode()]));
		if((showFlag!=showNumber)&&(showFlag>-999))
			return;
		boolean q=false;
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(!q))
		{
			final String newType=mob.session().prompt(L("Enter a new level (?)\n\r"),Area.THEME_PHRASE[A.getThemeCode()]);
			if(newType.equals("?"))
			{
				final StringBuilder say=new StringBuilder("");
				for(int i=0;i<Area.THEME_PHRASE.length;i++)
					say.append(i+") "+Area.THEME_PHRASE[i]+"\n\r");
				mob.tell(say.toString());
				q=false;
			}
			else
			if(newType.trim().length()==0)
				mob.tell(L("(no change)"));
			else
			{
				q=true;
				int newValue=-1;
				if(CMath.s_int(newType)>0)
					newValue=CMath.s_int(newType);
				else
				for(int i=0;i<Area.THEME_PHRASE.length;i++)
				{
					if(Area.THEME_PHRASE[i].toUpperCase().startsWith(newType.toUpperCase()))
						newValue=i;
				}
				if(newValue>=0)
					A.setTheme(newValue);
				else
					mob.tell(L("(no change)"));
			}
		}
	}

	protected void genConsumedMaterials(final MOB mob, final FuelConsumer E, final int showNumber, final int showFlag) throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		boolean q=false;
		final Session S=mob.session();
		while((S!=null)&&(!S.isStopped())&&(!q))
		{
			final StringBuilder str=new StringBuilder("");
			for(int i=0;i<E.getConsumedFuelTypes().length;i++)
			{
				if(i>0)
					str.append(", ");
				str.append(RawMaterial.CODES.NAME(E.getConsumedFuelTypes()[i]));
			}
			mob.tell(L("@x1. Consumed Resources: '@x2'.",""+showNumber,str.toString()));
			if((showFlag!=showNumber)&&(showFlag>-999))
				return;
			final String newType=mob.session().prompt(L("Enter a resource to add/remove (?)\n\r:"),"");
			if((newType==null)||(newType.length()==0))
				return;
			else
			if(newType.equals("?"))
			{
				final StringBuilder say=new StringBuilder("");
				for(final String codeName : RawMaterial.CODES.NAMES())
					say.append(codeName+", ");
				mob.tell(say.toString().substring(0,say.length()-2));
				q=false;
			}
			else
			{
				q=true;
				final int newValue=RawMaterial.CODES.FIND_IgnoreCase(newType);
				if(newValue>=0)
				{
					if(CMParms.contains(E.getConsumedFuelTypes(), newValue))
					{
						final int[] newSet=new int[E.getConsumedFuelTypes().length-1];
						for(int o=0,n=0;o<E.getConsumedFuelTypes().length;o++)
							if(E.getConsumedFuelTypes()[o]!=newValue)
							newSet[n++]=E.getConsumedFuelTypes()[o];
						E.setConsumedFuelType(newSet);
					}
					else
					{
						final int[] newSet=Arrays.copyOf(E.getConsumedFuelTypes(),E.getConsumedFuelTypes().length+1);
						newSet[newSet.length-1]=newValue;
						E.setConsumedFuelType(newSet);
					}
				}
				else
					mob.tell(L("Unknown resource: '@x1'.  Use ? for a list.",newType));
			}
		}
	}

	protected void genMessageTypes(final MOB mob, final ShipWarComponent E, final int showNumber, final int showFlag) throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		boolean q=false;
		final Session S=mob.session();
		while((S!=null)&&(!S.isStopped())&&(!q))
		{
			final StringBuilder str=new StringBuilder("");
			for(int i=0;i<E.getDamageMsgTypes().length;i++)
			{
				if(i>0)
					str.append(", ");
				str.append(CMMsg.TYPE_DESCS[E.getDamageMsgTypes()[i]]);
			}
			if((E.getTechType()==Technical.TechType.SHIP_WEAPON)||(E.getTechType()==Technical.TechType.PERSONAL_WEAPON))
				mob.tell(L("@x1. Weapon Types: '@x2'.",""+showNumber,str.toString()));
			else
				mob.tell(L("@x1. Shielded Types: '@x2'.",""+showNumber,str.toString()));
			if((showFlag!=showNumber)&&(showFlag>-999))
				return;
			final String newType=mob.session().prompt(L("Enter a type to add/remove (?)\n\r:"),"");
			if((newType==null)||(newType.length()==0))
				return;
			else
			if(newType.equals("?"))
			{
				final StringBuilder say=new StringBuilder("");
				for(final String codeName : CMMsg.TYPE_DESCS)
					say.append(codeName+", ");
				mob.tell(say.toString().substring(0,say.length()-2));
				q=false;
			}
			else
			{
				q=true;
				final int newValue=CMParms.indexOf(CMMsg.TYPE_DESCS, newType.toUpperCase().trim());
				if(newValue>=0)
				{
					if(CMParms.contains(E.getDamageMsgTypes(), newValue))
					{
						final int[] newSet=new int[E.getDamageMsgTypes().length-1];
						for(int o=0,n=0;o<E.getDamageMsgTypes().length;o++)
						{
							if(E.getDamageMsgTypes()[o]!=newValue)
								newSet[n++]=E.getDamageMsgTypes()[o];
						}
						E.setDamageMsgTypes(newSet);
					}
					else
					{
						final int[] newSet=Arrays.copyOf(E.getDamageMsgTypes(),E.getDamageMsgTypes().length+1);
						newSet[newSet.length-1]=newValue;
						E.setDamageMsgTypes(newSet);
					}
				}
				else
					mob.tell(L("Unknown type: '@x1'.  Use ? for a list.",newType));
			}
		}
	}

	protected void genMaterialCode(final MOB mob, final Item E, final int showNumber, final int showFlag)
			throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		E.setMaterial(genAnyMaterialCode(mob,"Material Type",E.material(),false,showNumber,showFlag));
	}

	protected int genAnyMaterialCode(final MOB mob, final String prompt, int currMat, final boolean inheritOk, final int showNumber, final int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return currMat;
		String matName=(currMat<0)?"Inherited":RawMaterial.CODES.NAME(currMat);
		mob.tell(showNumber+". "+prompt+": '"+matName+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999))
			return currMat;
		boolean q=false;
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(!q))
		{
			matName=(currMat<0)?"Inherited":RawMaterial.CODES.NAME(currMat);
			final String newType=mob.session().prompt(L("Enter a new material (?)\n\r:"),matName);
			if(newType.equals("?"))
			{
				final StringBuilder say=new StringBuilder("");
				if(inheritOk)
					say.append("Inherited, ");
				for(final String S : RawMaterial.CODES.NAMES())
					say.append(S+", ");
				mob.tell(say.toString().substring(0,say.length()-2));
				q=false;
			}
			else
			if(newType.equalsIgnoreCase("Inherited"))
			{
				q=true;
				currMat=-1;
			}
			else
			{
				q=true;
				final int newValue=RawMaterial.CODES.FIND_IgnoreCase(newType);
				if(newValue>=0)
					currMat=newValue;
				else
					mob.tell(L("(no change)"));
			}
		}
		return currMat;
	}

	protected void genBreathes(final MOB mob, final Race me, final int showNumber, final int showFlag) throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		boolean q=false;
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(!q))
		{
			final StringBuilder matName=new StringBuilder("");
			if(me.getBreathables().length==0)
				matName.append("Anything");
			else
			for(final int r : me.getBreathables())
				if(matName.length()>0)
					matName.append(", ").append(RawMaterial.CODES.NAME(r));
				else
					matName.append(RawMaterial.CODES.NAME(r));
			mob.tell(L("@x1. Can breathe: '@x2'.",""+showNumber,matName.toString()));
			if((showFlag!=showNumber)&&(showFlag>-999))
				return;
			final String newType=mob.session().prompt(L("Enter a material to add/remove, or ANYTHING (?)\n\r:"),"");
			if(newType.trim().length()==0)
				return;
			if(newType.equals("?"))
			{
				final StringBuilder say=new StringBuilder("");
				say.append("ANYTHING, ");
				for(final String S : RawMaterial.CODES.NAMES())
					say.append(S+", ");
				mob.tell(say.toString().substring(0,say.length()-2));
				q=false;
			}
			else
			if(newType.equalsIgnoreCase("ANYTHING"))
			{
				q=true;
				me.setStat("BREATHES","");
			}
			else
			{
				q=false;
				final int newValue=RawMaterial.CODES.FIND_IgnoreCase(newType);
				if(newValue>=0)
				{
					String newList;
					final int x=CMParms.indexOf(me.getBreathables(), newValue);
					if(x>=0)
					{
						if(me.getBreathables().length==1)
							newList=""+RawMaterial.RESOURCE_AIR;
						else
						{
							final StringBuilder str=new StringBuilder("");
							for(final int r : me.getBreathables())
							{
								if(r!=newValue)
								{
									if(str.length()>0)
										str.append(", ").append(r);
									else
										str.append(r);
								}
							}
							newList=str.toString();
						}
						mob.tell(L("You've removed @x1",RawMaterial.CODES.NAME(newValue)));
					}
					else
					{
						final StringBuilder str=new StringBuilder(CMParms.toListString(me.getBreathables()));
						if(str.length()>0)
							str.append(", ").append(newValue);
						else
							str.append(newValue);
						newList=str.toString();
					}
					me.setStat("BREATHES",newList);
					mob.tell(L("You've added @x1",RawMaterial.CODES.NAME(newValue)));
				}
				else
					mob.tell(L("Unknown resource '@x1' (no change)",newType));
			}
		}
	}

	protected void genInstrumentType(final MOB mob, final MusicalInstrument E, final int showNumber, final int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		mob.tell(L("@x1. Instrument Type: '@x2'.",""+showNumber,E.getInstrumentTypeName()));
		if((showFlag!=showNumber)&&(showFlag>-999))
			return;
		boolean q=false;
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(!q))
		{
			final String newType=mob.session().prompt(L("Enter a new type (?)\n\r:"),E.getInstrumentTypeName());
			if(newType.equals("?"))
			{
				final StringBuilder say=new StringBuilder("");
				for(final InstrumentType type : MusicalInstrument.InstrumentType.values())
					say.append(type.name()+", ");
				mob.tell(say.toString().substring(0,say.length()-2));
				q=false;
			}
			else
			{
				q=true;
				InstrumentType newValue=null;
				for(final InstrumentType type : MusicalInstrument.InstrumentType.values())
				{
					if(newType.equalsIgnoreCase(type.name()))
						newValue=type;
				}
				if(newValue != null)
					E.setInstrumentType(newValue);
				else
					mob.tell(L("(no change)"));
			}
		}
	}

	protected void genSpecialFaction(final MOB mob, final MOB E, final int showNumber, final int showFlag, final Faction F)
	throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		if(F==null)
			return;
		final Faction.FRange myFR=CMLib.factions().getRange(F.factionID(),E.fetchFaction(F.factionID()));
		mob.tell(showNumber+". "+F.name()+": "+((myFR!=null)?myFR.name():"UNDEFINED")+" ("+E.fetchFaction(F.factionID())+")");
		if((showFlag!=showNumber)&&(showFlag>-999))
			return;
		for(final Enumeration<Faction.FRange> e=F.ranges();e.hasMoreElements();)
		{
			final Faction.FRange FR=e.nextElement();
			mob.tell(CMStrings.padRight(FR.name(),20)+": "+FR.low()+" - "+FR.high()+")");
		}
		final String newOne=mob.session().prompt(L("Enter a new value\n\r:"));
		if(CMath.isInteger(newOne))
		{
			E.addFaction(F.factionID(),CMath.s_int(newOne));
			return;
		}
		for(final Enumeration<Faction.FRange> e=F.ranges();e.hasMoreElements();)
		{
			final Faction.FRange FR=e.nextElement();
			if(FR.name().toUpperCase().startsWith(newOne.toUpperCase()))
			{
				if(FR.low()==F.minimum())
					E.addFaction(F.factionID(),FR.low());
				else
				if(FR.high()==F.maximum())
					E.addFaction(F.factionID(),FR.high());
				else
					E.addFaction(F.factionID(),FR.low()+((FR.high()-FR.low())/2));
				return;
			}
		}
		mob.tell(L("(no change)"));
	}

	protected void genFaction(final MOB mob, final MOB E, final int showNumber, final int showFlag)
	throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		String newFact="Q";
		final int wrap=CMLib.lister().fixColWidth(50,mob.session());
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(newFact.length()>0))
		{
			String listing=E.getFactionListing();
			if((listing.length()>wrap)&&((showFlag!=showNumber)&&(showFlag>-999)))
				listing=CMStrings.limit(listing, wrap)+((listing.length()>wrap)?"...":"");
			mob.tell(L("@x1. Factions: @x2",""+showNumber,listing));
			if((showFlag!=showNumber)&&(showFlag>-999))
				return;
			newFact=mob.session().prompt(L("Enter a faction name to add or remove\n\r:"),"");
			if(newFact.length()>0)
			{
				Faction lookedUp=CMLib.factions().getFactionByName(newFact);
				if(lookedUp==null)
					lookedUp=CMLib.factions().getFaction(newFact);
				if(lookedUp!=null)
				{
					if (E.fetchFaction(lookedUp.factionID())!=Integer.MAX_VALUE)
					{
						// this mob already has this faction, they must want it removed
						E.removeFaction(lookedUp.factionID());
						mob.tell(L("Faction '@x1' removed.",lookedUp.name()));
					}
					else
					{
						final String howMuch = mob.session().prompt(L("How much faction (@x1)?",""+lookedUp.findDefault(E)),
								   Integer.toString(lookedUp.findDefault(E)));
						if(CMath.isInteger(howMuch))
						{
							int value =Integer.valueOf(howMuch).intValue();
							if(value<lookedUp.minimum())
								value=lookedUp.minimum();
							if(value>lookedUp.maximum())
								value=lookedUp.maximum();
							E.addFaction(lookedUp.factionID(),value);
							mob.tell(L("Faction '@x1' added.",lookedUp.name()));
						}
						else
							mob.tell(L("'@x1' is not a valid number.",howMuch));
					}
				 }
				 else
					mob.tell(L("'@x1' is not recognized as a valid faction name or file.",newFact));
			}
		}
	}

	protected void genGender(final MOB mob, final MOB E, final int showNumber, final int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		mob.tell(L("@x1. Gender: '@x2'.",""+showNumber,""+Character.toUpperCase((char)E.baseCharStats().getStat(CharStats.STAT_GENDER))));
		if((showFlag!=showNumber)&&(showFlag>-999))
			return;
		final StringBuilder str = new StringBuilder("");
		final StringBuilder cstr = new StringBuilder("");
		for(final Object[] gset : CMProps.getListFileStringChoices(ListFile.GENDERS))
		{
			if((gset.length>0)
			&&(gset[0].toString().length()>0))
			{
				if(str.length()>0)
					str.append("/");
				final char c = gset[0].toString().charAt(0);
				str.append(c);
				cstr.append(c);
			}
		}
		final String newType=mob.session().choose(L("Enter a new gender (@x1)\n\r:",str.toString()),cstr.toString(),"");
		int newValue=-1;
		if(newType.length()>0)
			newValue=cstr.toString().indexOf(newType.trim().toUpperCase());
		if(newValue>=0)
			E.baseCharStats().setStat(CharStats.STAT_GENDER,newType.trim().toUpperCase().charAt(0));
		else
			mob.tell(L("(no change)"));
	}

	protected void genWeaponClassification(final MOB mob, final Weapon E, final int showNumber, final int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		mob.tell(L("@x1. Weapon Classification: '@x2'.",""+showNumber,Weapon.CLASS_DESCS[E.weaponClassification()]));
		if((showFlag!=showNumber)&&(showFlag>-999))
			return;
		boolean q=false;
		final String sel=("ABEFHKPRSDTN");
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(!q))
		{
			final String newType=mob.session().choose(L("Enter a new value (?)\n\r:"),sel+"?","");
			if(newType.equals("?"))
			{
				for(int i=0;i<sel.length();i++)
					mob.tell(sel.charAt(i)+") "+Weapon.CLASS_DESCS[i]);
				q=false;
			}
			else
			{
				q=true;
				int newValue=-1;
				if(newType.length()>0)
					newValue=sel.indexOf(newType.toUpperCase());
				if(newValue>=0)
					E.setWeaponClassification(newValue);
				else
					mob.tell(L("(no change)"));
			}
		}
	}

	protected void genSecretIdentity(final MOB mob, final Item E, final int showNumber, final int showFlag) throws IOException
	{
		E.setSecretIdentity(prompt(mob, E.rawSecretIdentity(), showNumber, showFlag, "Secret Identity", true, false));
	}

	protected void genMaterialSubType(final MOB mob, final RawMaterial E, final int showNumber, final int showFlag) throws IOException
	{
		E.setSubType(prompt(mob, E.getSubType(), showNumber, showFlag, "Rsc Sub-Type", true, false).toUpperCase().trim());
	}

	protected void genNourishment(final MOB mob, final Food E, final int showNumber, final int showFlag) throws IOException
	{
		E.setNourishment(prompt(mob, E.nourishment(), showNumber, showFlag, "Nourishment/Eat"));
	}

	protected void genBiteSize(final MOB mob, final Food E, final int showNumber, final int showFlag) throws IOException
	{
		E.setBite(prompt(mob, E.bite(), showNumber, showFlag, "Bite/Eat (0=all)"));
	}

	protected void genRace(final MOB mob, final MOB M, final int showNumber, final int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		String raceID="begin!";
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(raceID.length()>0))
		{
			mob.tell(L("@x1. Race: '@x2'.",""+showNumber,M.baseCharStats().getMyRace().ID()));
			if((showFlag!=showNumber)&&(showFlag>-999))
				return;
			raceID=mob.session().prompt(L("Enter a new race (?)\n\r:"),"").trim();
			if(raceID.equalsIgnoreCase("?"))
				mob.tell(CMLib.lister().build3ColTable(mob,CMClass.races()).toString());
			else
			if(raceID.length()==0)
				mob.tell(L("(no change)"));
			else
			{
				final Race R=CMClass.getRace(raceID);
				if(R!=null)
				{
					CMLib.database().registerRaceUsed(R);
					M.baseCharStats().setMyRace(R);
					M.baseCharStats().getMyRace().startRacing(M,false);
					M.baseCharStats().getMyRace().setHeightWeight(M.basePhyStats(),M.baseCharStats().reproductiveCode());
				}
				else
					mob.tell(L("Unknown race! Try '?'."));
			}
		}
	}

	protected void genCharClass(final MOB mob, final MOB M, final int showNumber, final int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		String classID="begin!";
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(classID.length()>0))
		{
			final StringBuilder str=new StringBuilder("");
			for(int c=0;c<M.baseCharStats().numClasses();c++)
			{
				final CharClass C=M.baseCharStats().getMyClass(c);
				str.append(C.ID()+"("+M.baseCharStats().getClassLevel(C)+") ");
			}
			mob.tell(L("@x1. Class: '@x2'.",""+showNumber,str.toString()));
			if((showFlag!=showNumber)&&(showFlag>-999))
				return;
			classID=mob.session().prompt(L("Enter a class to add/remove(?)\n\r:"),"").trim();
			if(classID.equalsIgnoreCase("?"))
				mob.tell(CMLib.lister().build3ColTable(mob,CMClass.charClasses()).toString());
			else
			if(classID.length()==0)
				mob.tell(L("(no change)"));
			else
			{
				CharClass C=CMClass.getCharClass(classID);
				if((C!=null)&&((C.availabilityCode()>0)||CMSecurity.isASysOp(mob)))
				{
					if(M.baseCharStats().getClassLevel(C)>=0)
					{
						if(M.baseCharStats().numClasses()<2)
							mob.tell(L("Final class may not be removed.  To change a class, add the new one first."));
						else
						{
							final StringBuilder charClasses=new StringBuilder("");
							final StringBuilder classLevels=new StringBuilder("");
							for(int c=0;c<M.baseCharStats().numClasses();c++)
							{
								final CharClass C2=M.baseCharStats().getMyClass(c);
								final int L2=M.baseCharStats().getClassLevel(C2);
								if(C2!=C)
								{
									charClasses.append(";"+C2.ID());
									classLevels.append(";"+L2);
								}
							}
							M.baseCharStats().setAllClassInfo(charClasses.toString(), classLevels.toString());
						}
					}
					else
					{
						int highLvl=Integer.MIN_VALUE;
						CharClass highestC=null;
						for(int c=0;c<M.baseCharStats().numClasses();c++)
						{
							final CharClass C2=M.baseCharStats().getMyClass(c);
							if(M.baseCharStats().getClassLevel(C2)>highLvl)
							{
								highestC=C2;
								highLvl=M.baseCharStats().getClassLevel(C2);
							}
						}
						M.baseCharStats().setCurrentClass(C);
						int levels=M.baseCharStats().combinedSubLevels();
						levels=M.basePhyStats().level()-levels;
						String lvl=null;
						if(levels>0)
						{
							lvl=mob.session().prompt(L("Levels to give this class (@x1)\n\r:",""+levels),""+levels).trim();
							int lvl2=CMath.s_int(lvl);
							if(lvl2>levels)
								lvl2=levels;
							M.baseCharStats().setClassLevel(C,lvl2);
						}
						else
						if(highestC!=null)
						{
							lvl=mob.session().prompt(L("Levels to siphon from @x1 for this class (0)\n\r:",highestC.ID()),""+0).trim();
							int lvl2=CMath.s_int(lvl);
							if(lvl2>highLvl)
								lvl2=highLvl;
							M.baseCharStats().setClassLevel(highestC,highLvl-lvl2);
							M.baseCharStats().setClassLevel(C,lvl2);
						}
					}
					int levels=M.baseCharStats().combinedSubLevels();
					levels=M.basePhyStats().level()-levels;
					C=M.baseCharStats().getCurrentClass();
					M.baseCharStats().setClassLevel(C,levels);
				}
				else
					mob.tell(L("Unknown character class! Try '?'."));
			}
		}
	}

	protected void genTattoos(final MOB mob, final Tattooable M, final int showNumber, final int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		String behave="NO";
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(behave.length()>0))
		{
			String tattoostr="";
			for(final Enumeration<Tattoo> e=M.tattoos();e.hasMoreElements();)
				tattoostr+=e.nextElement().getTattooName()+", ";
			if(tattoostr.length()>0)
				tattoostr=tattoostr.substring(0,tattoostr.length()-2);
			if((tattoostr.length()>60)&&((showFlag!=showNumber)&&(showFlag>-999)))
				tattoostr=tattoostr.substring(0,60)+"...";
			mob.tell(L("@x1. Tattoos: '@x2'.",""+showNumber,tattoostr));
			if((showFlag!=showNumber)&&(showFlag>-999))
				return;
			behave=mob.session().prompt(L("Enter a tattoo to add/remove\n\r:"),"");
			if(behave.length()>0)
			{
				final Tattoo pT=((Tattoo)CMClass.getCommon("DefaultTattoo")).parse(behave);
				final Tattoo T=M.findTattoo(pT.getTattooName());
				if(T!=null)
				{
					mob.tell(L("@x1 removed.",pT.getTattooName().trim().toUpperCase()));
					M.delTattoo(T);
				}
				else
				{
					mob.tell(L("@x1 added.",behave.trim().toUpperCase()));
					M.addTattoo(pT);
				}
			}
			else
				mob.tell(L("(no change)"));
		}
	}

	protected void genTitles(final MOB mob, final MOB M, final int showNumber, final int showFlag)
	throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		if(M.playerStats()==null)
			return;
		String behave="NO";
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(behave.length()>0))
		{
			String behaviorstr="";
			if((showFlag==showNumber)||(showFlag<=-999))
				behaviorstr+="\n\r";
			for(int b=0;b<M.playerStats().getTitles().size();b++)
			{
				final String B=M.playerStats().getTitles().get(b);
				if(B!=null)
				{
					if((showFlag!=showNumber)&&(showFlag>-999))
						behaviorstr+=B+", ";
					else
						behaviorstr+="  "+(b+1)+") "+B+"\n\r";
				}
			}
			if(behaviorstr.length()>0)
				behaviorstr=behaviorstr.substring(0,behaviorstr.length()-2);
			mob.tell(L("@x1. Titles: '@x2'.",""+showNumber,behaviorstr));
			if((showFlag!=showNumber)&&(showFlag>-999))
				return;
			behave=mob.session().prompt(L("Enter a title to add or a number to remove\n\r:"),"");
			if(behave.length()>0)
			{
				String tattoo=behave;
				if((tattoo.length()>0)
				&&(CMath.isInteger(tattoo))
				&&(CMath.s_int(tattoo)>0)
				&&(CMath.s_int(tattoo)<=M.playerStats().getTitles().size()))
					tattoo=M.playerStats().getTitles().get(CMath.s_int(tattoo)-1);
				else
				if((tattoo.length()>0)
				&&(Character.isDigit(tattoo.charAt(0)))
				&&(tattoo.indexOf(' ')>0)
				&&(CMath.isNumber(tattoo.substring(0,tattoo.indexOf(' ')))))
					tattoo=tattoo.substring(tattoo.indexOf(' ')+1).trim();
				if(M.playerStats().getTitles().contains(tattoo))
				{
					mob.tell(L("@x1 removed.",tattoo.trim().toUpperCase()));
					M.playerStats().delTitle(tattoo);
				}
				else
				{
					mob.tell(L("@x1 added.",behave.trim().toUpperCase()));
					M.playerStats().addTitle(tattoo);
				}
			}
			else
				mob.tell(L("(no change)"));
		}
	}

	protected void genExpertises(final MOB mob, final MOB M, final int showNumber, final int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		String behave="NO";
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(behave.length()>0))
		{
			final StringBuilder behaviorstr=new StringBuilder("");
			for(final Enumeration<String> x=M.expertises();x.hasMoreElements();)
				behaviorstr.append(x.nextElement()).append(", ");
			if(behaviorstr.length()>0)
				behaviorstr.setLength(behaviorstr.length()-2);
			if((behaviorstr.length()>60)&&((showFlag!=showNumber)&&(showFlag>-999)))
			{
				behaviorstr.setLength(60);
				behaviorstr.append("...");
			}
			mob.tell(L("@x1. Expertises: '@x2'.",""+showNumber,behaviorstr.toString()));
			if((showFlag!=showNumber)&&(showFlag>-999))
				return;
			behave=mob.session().prompt(L("Enter a lesson to add/remove\n\r:"),"");
			if(behave.length()>0)
			{
				if(M.fetchExpertise(behave)!=null)
				{
					mob.tell(L("@x1 removed.",behave));
					M.delExpertise(behave);
				}
				else
				if(CMLib.expertises().getDefinition(behave)==null)
				{
					mob.tell(L("@x1 is undefined.",behave));
					continue;
				}
				else
				{
					mob.tell(L("@x1 added.",behave));
					M.addExpertise(behave);
				}
			}
			else
				mob.tell(L("(no change)"));
		}
	}

	protected void genSecurity(final MOB mob, final MOB M, final int showNumber, final int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		final PlayerStats P=M.playerStats();
		if(P==null)
			return;
		String behave="NO";
		final List<String> secFlags=CMParms.parseSemicolons(P.getSetSecurityFlags(null),true);
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(behave.length()>0))
		{
			mob.tell(L("@x1. Security Groups: '@x2'.",""+showNumber,CMParms.toListString(secFlags)));
			if((showFlag!=showNumber)&&(showFlag>-999))
				return;
			behave=mob.session().prompt(L("Enter a group to add/remove\n\r:"),"");
			if(behave.length()>0)
			{
				if(secFlags.contains(behave.trim().toUpperCase()))
				{
					secFlags.remove(behave.trim().toUpperCase());
					P.getSetSecurityFlags(CMParms.toSemicolonListString(secFlags));
					mob.tell(L("@x1 removed.",behave));
				}
				else
				{
					final Object o=CMSecurity.instance().parseSecurityFlag(behave.trim().toUpperCase());
					final CMSecurity.SecFlag flag=(CMSecurity.SecFlag)CMath.s_valueOf(CMSecurity.SecFlag.class, behave.toUpperCase().trim());
					final boolean isFs=(o instanceof CMSecurity.SecPath);
					final boolean isGroup=(o instanceof CMSecurity.SecGroup);
					final boolean isFlag=(o instanceof CMSecurity.SecFlag) && (flag!=null);
					final boolean isJournalFlag=(o instanceof String);
					behave=behave.trim().toUpperCase().replace(' ','_');
					if((!isFlag) && (!isGroup) && (!isJournalFlag)&& (!isFs))
					{
						final List<String> grpNames=new ArrayList<String>();
						for(final Enumeration<SecGroup> g=CMSecurity.getSecurityGroups();g.hasMoreElements();)
							grpNames.add(g.nextElement().getName().toUpperCase());
						final List<String> jFlagNames=new ArrayList<String>();
						for(final Enumeration<String> j=CMSecurity.getJournalSecurityFlags();j.hasMoreElements();)
							jFlagNames.add(j.nextElement());
						mob.tell(L("No such security flag: @x1.",behave));
						mob.tell(L("Value flags include: @x1",CMParms.toListString(CMSecurity.SecFlag.values())));
						mob.tell(L("Valid groups include: @x1",CMParms.toListString(grpNames)));
						mob.tell(L("Value journal flags include: @x1",CMParms.toListString(jFlagNames)));
					}
					else
					{
						if(flag != null)
						{
							if((flag.getAreaAlias()==flag)
							&&(!CMSecurity.isAllowedAnywhere(mob,flag)))
							{
								mob.tell(L("You do not have clearance to add security code '@x1' to this class.",behave));
								continue;
							}
							else
							if((flag.getRegularAlias()==flag)
							&&(!CMSecurity.isAllowedEverywhere(mob,flag)))
							{
								mob.tell(L("You do not have clearance to add security code '@x1' to this class.",behave));
								continue;
							}
						}
						else
						if(isJournalFlag)
						{
							if(!CMSecurity.isJournalAccessAllowed(mob,behave))
							{
								mob.tell(L("You do not have clearance to add security code '@x1' to this class.",behave));
								continue;
							}
						}
						else
						if(!CMSecurity.isASysOp(mob))
						{
							mob.tell(L("You do not have clearance to add security group '@x1' to this class.",behave));
							continue;
						}
						secFlags.add(behave.trim().toUpperCase());
						P.getSetSecurityFlags(CMParms.toSemicolonListString(secFlags));
						mob.tell(L("@x1 added.",behave));
					}
				}
			}
			else
				mob.tell(L("(no change)"));
		}
	}

	@Override
	public void genBehaviors(final MOB mob, final PhysicalAgent P, final int showNumber, final int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		String behave="NO";
		final ListStringer baseStringer = CMLib.lister().getListStringer();
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(behave.length()>0))
		{
			String behaviorstr="";
			for(final Enumeration<Behavior> e=P.behaviors();e.hasMoreElements();)
			{
				final Behavior B=e.nextElement();
				if((B!=null)&&(B.isSavable()))
				{
					behaviorstr+=B.ID();
					if(B.getParms().trim().length()>0)
						behaviorstr+="("+B.getParms().trim()+"), ";
					else
						behaviorstr+=", ";
				}
			}
			if(behaviorstr.length()>0)
				behaviorstr=behaviorstr.substring(0,behaviorstr.length()-2);
			mob.tell(L("@x1. Behaviors: '@x2'.",""+showNumber,behaviorstr.replace('@','_')));
			if((showFlag!=showNumber)&&(showFlag>-999))
				return;
			behave=mob.session().prompt(L("Enter a behavior to add/remove (?)\n\r:"),"");
			if(behave.length()>0)
			{
				if(behave.equalsIgnoreCase("?"))
				{
					final ListingLibrary.ListStringer stringer=new ListingLibrary.ListStringer()
					{
						@Override
						public String stringify(final Object o)
						{
							String s=baseStringer.stringify(o);
							if((s!=null)&&(s.length()>0)
							&&(o instanceof Behavior)
							&& (((Behavior)o).canImprove(P)))
								s="^H"+s+"^N";
							return s;
						}
					};
					mob.tell(CMLib.lister().build3ColTable(mob,CMClass.behaviors(),null,stringer).toString());
				}
				else
				{
					Behavior chosenOne=null;
					for(final Enumeration<Behavior> e=P.behaviors();e.hasMoreElements();)
					{
						final Behavior B=e.nextElement();
						if((B!=null)&&(B.ID().equalsIgnoreCase(behave)))
							chosenOne=B;
					}
					if(chosenOne!=null)
					{
						mob.tell(L("@x1 removed.",chosenOne.ID()));
						P.delBehavior(chosenOne);
					}
					else
					{
						chosenOne=CMClass.getBehavior(behave);
						if(chosenOne!=null)
						{
							boolean alreadyHasIt=false;
							for(final Enumeration<Behavior> e=P.behaviors();e.hasMoreElements();)
							{
								final Behavior B=e.nextElement();
								if((B!=null)&&(B.ID().equals(chosenOne.ID())))
								{
									alreadyHasIt=true;
									chosenOne=B;
								}
							}
							String parms="?";
							while(parms.equals("?"))
							{
								parms=chosenOne.getParms();
								parms=mob.session().prompt(L("Enter any behavior parameters (?)\n\r:@x1",parms));
								if(parms.equals("?"))
								{
									final String s2=CMLib.help().getHelpText(chosenOne.ID(),mob,true);
									if(s2!=null)
										mob.tell(s2.toString());
									else mob.tell(L("no help!"));
								}
							}
							chosenOne.setParms(parms.trim());
							if(!alreadyHasIt)
							{
								mob.tell(L("@x1 added.",chosenOne.ID()));
								P.addBehavior(chosenOne);
							}
							else
								mob.tell(L("@x1 re-added.",chosenOne.ID()));
						}
						else
						{
							mob.tell(L("'@x1' is not recognized.  Try '?'.",behave));
						}
					}
				}
			}
			else
				mob.tell(L("(no change)"));
		}
	}

	@Override
	public void genAffects(final MOB mob, final Physical P, final int showNumber, final int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		String behave="NO";
		final ListStringer baseStringer = CMLib.lister().getListStringer();
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(behave.length()>0))
		{
			String affectstr="";
			for(int b=0;b<P.numEffects();b++)
			{
				final Ability A=P.fetchEffect(b);
				if((A!=null)&&(A.isSavable()))
				{
					affectstr+=A.ID();
					if(A.text().trim().length()>0)
						affectstr+="("+A.text().trim()+"), ";
					else
						affectstr+=", ";
				}
			}
			if(affectstr.length()>0)
				affectstr=affectstr.substring(0,affectstr.length()-2);
			mob.tell(L("@x1. Effects: '@x2'.",""+showNumber,affectstr));
			if((showFlag!=showNumber)&&(showFlag>-999))
				return;
			behave=mob.session().prompt(L("Enter an effect to add/remove (?)\n\r:"),"");
			if(behave.length()>0)
			{
				if(behave.equalsIgnoreCase("?"))
				{
					final ListingLibrary.ListStringer stringer=new ListingLibrary.ListStringer()
					{
						@Override
						public String stringify(final Object o)
						{
							String s=baseStringer.stringify(o);
							if((s!=null)&&(s.length()>0)&&(o instanceof Ability) && (((Ability)o).canAffect(P)))
								s="^X"+s+"^N";
							return s;
						}
					};
					mob.tell(CMLib.lister().build3ColTable(mob,CMClass.abilities(),null,stringer).toString());
				}
				else
				{
					Ability chosenOne=null;
					for(int a=0;a<P.numEffects();a++)
					{
						final Ability A=P.fetchEffect(a);
						if((A!=null)&&(A.ID().equalsIgnoreCase(behave)))
							chosenOne=A;
					}
					if(chosenOne!=null)
					{
						mob.tell(L("@x1 removed.",chosenOne.ID()));
						P.delEffect(chosenOne);
					}
					else
					{
						chosenOne=CMClass.getAbility(behave);
						if((chosenOne!=null)
						&&((chosenOne.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_ARCHON)
						&&(mob.fetchAbility(chosenOne.ID())==null))
							chosenOne=null;
						if(chosenOne!=null)
						{
							String parms="";
							String s="?";
							while(s.equals("?"))
							{
								parms=chosenOne.text();
								s=mob.session().prompt(L("Enter any effect parameters (?)\n\r:@x1",parms));
								if(s.equals("?"))
								{
									final String s2=CMLib.help().getHelpText(chosenOne.ID(),mob,true);
									if(s2!=null)
										mob.tell(s2.toString());
									else
										mob.tell(L("no help!"));
								}
								else
								if(s.equalsIgnoreCase("null"))
									parms="";
								else
								if(s.length()>0)
									parms=s;
							}
							chosenOne.setMiscText(parms.trim());
							mob.tell(L("@x1 added.",chosenOne.ID()));
							P.addNonUninvokableEffect(chosenOne);
						}
						else
						{
							mob.tell(L("'@x1' is not recognized.  Try '?'.",behave));
						}
					}
				}
			}
			else
				mob.tell(L("(no change)"));
		}
	}

	protected void genRideableType(final MOB mob, final Rideable R, final int showNumber, final int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		mob.tell(L("@x1. Rideable Type: '@x2'.",""+showNumber,R.rideBasis().toString()));
		if((showFlag!=showNumber)&&(showFlag>-999))
			return;
		boolean q=false;
		final String sel="LWACBTEDGH";
		while(!q)
		{
			final String newType=mob.session().choose(L("Enter a new value (?)\n\r:"),sel+"?","");
			if(newType.equals("?"))
			{
				for(int i=0;i<sel.length();i++)
					mob.tell(sel.charAt(i)+") "+Rideable.Basis.values()[i].toString().toLowerCase());
				q=false;
			}
			else
			{
				q=true;
				int newValue=-1;
				if(newType.length()>0)
					newValue=sel.indexOf(newType.toUpperCase());
				if(newValue>=0)
					R.setRideBasis(Rideable.Basis.values()[newValue]);
				else
					mob.tell(L("(no change)"));
			}
		}
	}

	protected void genRideableRideCapacity(final MOB mob, final Rideable E, final int showNumber, final int showFlag) throws IOException
	{
		E.setRiderCapacity(prompt(mob, E.riderCapacity(), showNumber, showFlag, "Rider capacity"));
	}

	protected void genShopkeeperType(final MOB mob, final ShopKeeper M, final int showNumber, final int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		final long oldMask=M.getWhatIsSoldMask();
		while((mob.session()!=null)&&(!mob.session().isStopped()))
		{
			mob.tell(L("@x1. Shopkeeper type: '@x2'.",""+showNumber,M.storeKeeperString()));
			if((showFlag!=showNumber)&&(showFlag>-999))
				return;

			final StringBuilder buf=new StringBuilder("");
			final StringBuilder codes=new StringBuilder("");
			final String codeStr="0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ!=+-@#$%&*~;:,<.>_";
			if(M instanceof Banker)
			{
				int r=ShopKeeper.DEAL_BANKER;
				char c=codeStr.charAt(r);
				codes.append(c);
				buf.append(c+") "+ShopKeeper.DEAL_DESCS[r]+"\n\r");
				r=ShopKeeper.DEAL_CLANBANKER;
				c=codeStr.charAt(r);
				codes.append(c);
				buf.append(c+") "+ShopKeeper.DEAL_DESCS[r]+"\n\r");
			}
			else
			if(M instanceof PostOffice)
			{
				int r=ShopKeeper.DEAL_POSTMAN;
				char c=codeStr.charAt(r);
				codes.append(c);
				buf.append(c+") "+ShopKeeper.DEAL_DESCS[r]+"\n\r");
				r=ShopKeeper.DEAL_CLANPOSTMAN;
				c=codeStr.charAt(r);
				codes.append(c);
				buf.append(c+") "+ShopKeeper.DEAL_DESCS[r]+"\n\r");
			}
			else
			if(M instanceof Auctioneer)
			{
				int r=ShopKeeper.DEAL_AUCTIONEER;
				char c=codeStr.charAt(r);
				codes.append(c);
				buf.append(c+") "+ShopKeeper.DEAL_DESCS[r]+"\n\r");
				r=ShopKeeper.DEAL_AUCTIONEER;
				c=codeStr.charAt(r);
				codes.append(c);
				buf.append(c+") "+ShopKeeper.DEAL_DESCS[r]+"\n\r");
			}
			else
			for(int r=0;r<ShopKeeper.DEAL_DESCS.length;r++)
			{
				if((r!=ShopKeeper.DEAL_CLANBANKER)
				&&(r!=ShopKeeper.DEAL_BANKER)
				&&(r!=ShopKeeper.DEAL_CLANPOSTMAN)
				&&(r!=ShopKeeper.DEAL_POSTMAN))
				{
					final char c=codeStr.charAt(r);
					codes.append(c);
					buf.append(c+") "+ShopKeeper.DEAL_DESCS[r]+"\n\r");
				}
			}
			final String newType=mob.session().choose(L("@x1Enter a value to toggle on/off: ",buf.toString()),codes.toString(),"");
			int newValue=-1;
			if(newType.trim().length()==0)
			{
				if(M.getWhatIsSoldMask()==oldMask)
					mob.tell(L("(no change"));
				return;
			}
			if(newType.length()>0)
				newValue=codeStr.indexOf(newType.toUpperCase());
			if(newValue<=0)
				M.setWhatIsSoldMask(0);
			else
			if(M.isSold(newValue))
			{
				M.addSoldType(-newValue);
				final CoffeeShop shop=(M instanceof Librarian)?((Librarian)M).getBaseLibrary():M.getShop();
				for(final Iterator<Environmental> i=shop.getStoreInventory();i.hasNext();)
				{
					final Environmental E2=i.next();
					if(!M.doISellThis(E2))
						shop.delAllStoreInventory(E2);
				}
			}
			else
				M.addSoldType(newValue);
		}
	}

	protected void genShopkeeperShopInventory(final MOB mob, final ShopKeeper M, final int showNumber, final int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		String itemstr="NO";
		while(itemstr.length()>0)
		{
			String inventorystr="";
			final CoffeeShop shop=(M instanceof Librarian)?((Librarian)M).getBaseLibrary():M.getShop();
			for(final Iterator<Environmental> i=shop.getStoreInventory();i.hasNext();)
			{
				final Environmental E2=i.next();
				if(E2.isGeneric())
					inventorystr+=E2.name()+" ("+shop.numberInStock(E2)+"), ";
				else
					inventorystr+=CMClass.classID(E2)+" ("+shop.numberInStock(E2)+"), ";
			}
			if(inventorystr.length()>0)
				inventorystr=inventorystr.substring(0,inventorystr.length()-2);
			mob.tell(L("@x1. Inventory: '@x2'.",""+showNumber,inventorystr));
			if((showFlag!=showNumber)&&(showFlag>-999))
				return;
			itemstr=mob.session().prompt(L("Enter something to add/remove (?)\n\r:"),"");
			if(itemstr.length()>0)
			{
				if(itemstr.equalsIgnoreCase("?"))
				{
					mob.tell(CMLib.lister().build3ColTable(mob,CMClass.abilities()).toString());
					mob.tell(CMLib.lister().build3ColTable(mob,CMClass.armor()).toString());
					mob.tell(CMLib.lister().build3ColTable(mob,CMClass.weapons()).toString());
					mob.tell(CMLib.lister().build3ColTable(mob,CMClass.miscMagic()).toString());
					mob.tell(CMLib.lister().build3ColTable(mob,CMClass.tech()).toString());
					mob.tell(CMLib.lister().build3ColTable(mob,CMClass.clanItems()).toString());
					mob.tell(CMLib.lister().build3ColTable(mob,CMClass.basicItems()).toString());
					mob.tell(CMLib.lister().build3ColTable(mob,CMClass.mobTypes()).toString());
					mob.tell(L("* Plus! Any items on the ground."));
					mob.tell(L("* Plus! Any mobs hanging around in the room."));
				}
				else
				{
					Environmental item=shop.getStock(itemstr,null);
					if(item!=null)
					{
						mob.tell(L("@x1 removed.",item.ID()));
						shop.delAllStoreInventory((Environmental)item.copyOf());
					}
					else
					{
						item=CMClass.getUnknown(itemstr);
						if((item==null)&&(mob.location()!=null))
						{
							final Room R=mob.location();
							item=R.findItem(null,itemstr);
							if(item==null)
							{
								item=R.fetchInhabitant(itemstr);
								if((item instanceof MOB)&&(!((MOB)item).isMonster()))
									item=null;
							}
							if(item==null)
								item=mob.findItem(null,itemstr);
						}
						if((item!=null)
						&&((!(item instanceof ArchonOnly))
							||(CMSecurity.isASysOp(mob))))
						{
							item=(Environmental)item.copyOf();
							if(item !=null)
							{
								if(item instanceof Physical)
									((Physical)item).recoverPhyStats();
								boolean ok=M.doISellThis(item);
								if((item instanceof Ability)
								&&((M.isSold(ShopKeeper.DEAL_TRAINER))||(M.isSold(ShopKeeper.DEAL_CASTER))))
									ok=true;
								else
								if(M.isSold(ShopKeeper.DEAL_INVENTORYONLY))
									ok=true;
								if((ok)||((mob.session()!=null)&&mob.session().confirm(L("This shopkeeper type does not sell that. Are you sure (y/N)?"),"N")))
								{
									boolean alreadyHasIt=false;
									if(M.getShop().doIHaveThisInStock(item.Name(),null))
										alreadyHasIt=true;

									if(!alreadyHasIt)
									{
										mob.tell(L("@x1 added.",item.ID()));
										int num=1;
										if(!(item instanceof Ability))
											num=CMath.s_int(mob.session().prompt(L("How many? :"),""));
										final int price=CMath.s_int(mob.session().prompt(L("At what price? :"),""));
										M.getShop().addStoreInventory(item,num,price);
									}
								}
							}
						}
						else
						{
							mob.tell(L("'@x1' is not recognized.  Try '?'.",itemstr));
						}
					}
				}
			}
			else
				mob.tell(L("(no change)"));
		}
	}

	protected void genShopkeeperTypeFlags(final MOB mob, final ShopKeeper M, final int showNumber, final int showFlag)
			throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		final String oldMask=CMParms.toListString(M.viewFlags());
		while((mob.session()!=null)&&(!mob.session().isStopped()))
		{
			mob.tell(L("@x1. View flags/types: '@x2'.",""+showNumber,CMParms.toListString(M.viewFlags())));
			if((showFlag!=showNumber)&&(showFlag>-999))
				return;

			final StringBuilder buf=new StringBuilder("");
			final StringBuilder codes=new StringBuilder("");
			final String codeStr="0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ!=+-@#$%&*~;:,<.>_";
			for(int r=0;r<ViewType.values().length;r++)
			{
				final char c=codeStr.charAt(r+1);
				codes.append(c);
				buf.append(c+") "+ViewType.values()[r]+"\n\r");
			}
			final String newType=mob.session().choose(L("@x1Enter a value to toggle on/off: ",buf.toString()),codes.toString(),"");
			int newValue=-1;
			if(newType.trim().length()==0)
			{
				if(CMParms.toListString(M.viewFlags()).equals(oldMask))
					mob.tell(L("(no change"));
				return;
			}
			if(newType.length()>0)
				newValue=codeStr.indexOf(newType.toUpperCase());
			if(newValue<=0)
			{
			}
			else
			if(!M.viewFlags().contains(ViewType.values()[newValue-1]))
				M.viewFlags().add(ViewType.values()[newValue-1]);
			else
				M.viewFlags().remove(ViewType.values()[newValue-1]);
		}
	}

	protected void genEconomicsPrejudice(final MOB mob, final Economics E, final int showNumber, final int showFlag) throws IOException
	{
		E.setPrejudiceFactors(prompt(mob, E.getRawPrejudiceFactors(), showNumber, showFlag, "Prejudice", true, false));
	}

	protected void genEconomicsPriceFactors(final MOB mob, final Economics E, final int showNumber, final int showFlag)
	throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		final String header=L("@x1. Item Pricing Factors: ",""+showNumber);
		String[] prics=E.getRawItemPricingAdjustments();
		if((showFlag!=showNumber)&&(showFlag>-999))
		{
			if(prics.length<1)
				mob.tell(header+"''.");
			else
			if(prics.length==1)
				mob.tell(header+"'"+prics[0]+"'.");
			else
				mob.tell(L("@x1@x2 defined..",header,""+prics.length));
			return;
		}
		final String behave="NO";
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(behave.length()>0))
		{
			mob.tell(header+"\n\r");
			for(int p=0;p<prics.length;p++)
				mob.tell(CMStrings.SPACES.substring(0,header.length()-3)
						+(p+1)+") "+prics[p]+"\n\r");
			final String newValue=mob.session().prompt(L("Enter # to remove, or A to add:\n\r:"),"");
			if(CMath.isInteger(newValue))
			{
				final int x=CMath.s_int(newValue);
				if((x>0)&&(x<=prics.length))
				{
					final String[] newPrics=new String[prics.length-1];
					int y=0;
					for(int i=0;i<prics.length;i++)
					{
						if(i!=(x-1))
							newPrics[y++]=prics[i];
					}
					prics=newPrics;
				}
			}
			else
			if(newValue.toUpperCase().startsWith("A"))
			{
				final double dbl=CMath.s_double(mob.session().prompt(L("Enter a price multiplier between 0.0 and X.Y\n\r: ")));
				String mask="?";
				while(mask.equals("?"))
				{
					mask=mob.session().prompt(L("Now enter a mask that describes the item (? for syntax)\n\r: "));
					if(mask.equals("?"))
						mob.tell(CMLib.masking().maskHelp("\n\r","disallow"));
				}
				final String[] newPrics=new String[prics.length+1];
				for(int i=0;i<prics.length;i++)
					newPrics[i]=prics[i];
				newPrics[prics.length]=dbl+" "+mask;
				prics=newPrics;
			}
			else
			{
				mob.tell(L("(no change)"));
				break;
			}
		}
		E.setItemPricingAdjustments(prics);
	}

	protected void genAreaBlurbs(final MOB mob, final Area A, final int showNumber, final int showFlag)
	throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		final String header=showNumber+". Area Blurb Flags: ";
		if((showFlag!=showNumber)&&(showFlag>-999))
		{
			final int numFlags=A.numBlurbFlags();
			if(numFlags<1)
				mob.tell(header+"''.");
			else
			if(numFlags==1)
			{
				final String flag = A.areaBlurbFlags().nextElement();
				mob.tell(header+"'"+flag+": "+A.getBlurbFlag(flag)+"'.");
			}
			else
				mob.tell(L("@x1@x2 defined..",header,""+numFlags));
			return;
		}
		final String behave="NO";
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(behave.length()>0))
		{
			mob.tell(header+"\n\r");
			for(final Enumeration<String> f = A.areaBlurbFlags();f.hasMoreElements();)
			{
				final String flag = f.nextElement();
				mob.tell(flag+": "+A.getBlurbFlag(flag));
			}
			final String newValue=mob.session().prompt(L("Enter flag to remove, or A to add:\n\r:"),"");
			if(A.getBlurbFlag(newValue.toUpperCase().trim())!=null)
			{
				A.delBlurbFlag(newValue.toUpperCase().trim());
				mob.tell(L("@x1 removed",newValue.toUpperCase().trim()));
			}
			else
			if(newValue.toUpperCase().equals("A"))
			{
				final String flag=mob.session().prompt(L("Enter a new flag: "));
				if(flag.trim().length()==0)
					continue;
				final String desc=mob.session().prompt(L("Enter a flag blurb (or nothing): "));
				A.addBlurbFlag((flag.toUpperCase().trim()+" "+desc).trim());
				mob.tell(L("@x1 added",flag.toUpperCase().trim()));
			}
			else
			if(newValue.length()==0)
			{
				mob.tell(L("(no change)"));
				break;
			}
		}
	}

	protected void genEconomicsBudget(final MOB mob, final Economics E, final int showNumber, final int showFlag) throws IOException
	{
		E.setBudget(prompt(mob, E.getRawBbudget(), showNumber, showFlag, "Budget", true, false));
	}

	protected void genEconomicsDevaluationRate(final MOB mob, final Economics E, final int showNumber, final int showFlag) throws IOException
	{
		E.setDevalueRate(prompt(mob, E.getRawDevalueRate(), showNumber, showFlag, "Devaluation rate(s)", true, false));
	}

	protected void genEconomicsInventoryReset(final MOB mob, final Economics E, final int showNumber, final int showFlag) throws IOException
	{
		E.setInvResetRate(prompt(mob, E.getRawInvResetRate(), showNumber, showFlag, "Inventory reset rate [ticks]"));
	}

	protected void genEconomicsIgnoreMask(final MOB mob, final Economics E, final int showNumber, final int showFlag) throws IOException
	{
		E.setIgnoreMask(prompt(mob, E.getRawIgnoreMask(), showNumber, showFlag, "Ignore Mask", true, false));
	}

	protected void genItemXML(final MOB mob, final ItemCollection me, final String key, final int showNumber, final int showFlag, final String desc)
			throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		String itemstr="NO";
		while(itemstr.length()>0)
		{
			String inventorystr="";
			for(final Enumeration<Item> i=me.items();i.hasMoreElements();)
			{
				final Environmental E2=i.nextElement();
				if(E2.isGeneric())
					inventorystr+=E2.name()+", ";
				else
					inventorystr+=CMClass.classID(E2)+" , ";
			}
			if(inventorystr.length()>0)
				inventorystr=inventorystr.substring(0,inventorystr.length()-2);
			mob.tell(L("@x1. "+desc+": '@x2'.",""+showNumber,inventorystr));
			if((showFlag!=showNumber)&&(showFlag>-999))
				return;
			itemstr=mob.session().prompt(L("Enter something to add/remove (?)\n\r:"),"");
			if(itemstr.length()>0)
			{
				if(itemstr.equalsIgnoreCase("?"))
				{
					mob.tell(CMLib.lister().build3ColTable(mob,CMClass.armor()).toString());
					mob.tell(CMLib.lister().build3ColTable(mob,CMClass.weapons()).toString());
					mob.tell(CMLib.lister().build3ColTable(mob,CMClass.miscMagic()).toString());
					mob.tell(CMLib.lister().build3ColTable(mob,CMClass.tech()).toString());
					mob.tell(CMLib.lister().build3ColTable(mob,CMClass.clanItems()).toString());
					mob.tell(CMLib.lister().build3ColTable(mob,CMClass.basicItems()).toString());
					mob.tell(L("* Plus! Any items on the ground."));
				}
				else
				{
					Item item=me.findItem(itemstr);
					if(item!=null)
					{
						mob.tell(L("@x1 removed.",item.ID()));
						me.delItem(item);
					}
					else
					{
						item=CMClass.getItem(itemstr);
						if((item==null)&&(mob.location()!=null))
						{
							final Room R=mob.location();
							item=R.findItem(null,itemstr);
						}
						if((item!=null)
						&&((!(item instanceof ArchonOnly))
							||(CMSecurity.isASysOp(mob))))
						{
							item=(Item)item.copyOf();
							if(item !=null)
							{
								((Physical)item).recoverPhyStats();
								me.addItem(item);
							}
						}
						else
						{
							mob.tell(L("'@x1' is not recognized.  Try '?'.",itemstr));
						}
					}
				}
			}
			else
				mob.tell(L("(no change)"));
		}
	}

	protected void genAbilities(final MOB mob, final MOB M, final int showNumber, final int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		String behave="NO";
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(behave.length()>0))
		{
			String abilitiestr="";
			for(int a=0;a<M.numAbilities();a++)
			{
				final Ability A=M.fetchAbility(a);
				if((A!=null)&&(A.isSavable()))
				{
					abilitiestr+=A.ID();
					if(A.text().length()>0)
						abilitiestr+="(), ";
					else
						abilitiestr+=", ";
				}
			}
			if(abilitiestr.length()>0)
				abilitiestr=abilitiestr.substring(0,abilitiestr.length()-2);
			if((abilitiestr.length()>50)&&((showFlag!=showNumber)&&(showFlag>-999)))
				abilitiestr=abilitiestr.substring(0,50)+"...";
			mob.tell(L("@x1. Abilities: '@x2'.",""+showNumber,abilitiestr));
			if((showFlag!=showNumber)&&(showFlag>-999))
				return;
			behave=mob.session().prompt(L("Enter an ability to add/remove (?)\n\r:"),"");
			if(behave.length()>0)
			{
				if(behave.equalsIgnoreCase("?"))
					mob.tell(CMLib.lister().build3ColTable(mob,CMClass.abilities(),-1).toString());
				else
				{
					String parms = null;
					final int x = behave.indexOf('(');
					if((x>0)&&(behave.endsWith(")")))
					{
						parms = behave.substring(x+1,behave.length()-1);
						behave = behave.substring(0,x);
					}
					Ability chosenOne=null;
					for(int a=0;a<M.numAbilities();a++)
					{
						final Ability A=M.fetchAbility(a);
						if((A!=null)&&(A.ID().equalsIgnoreCase(behave)))
							chosenOne=A;
					}
					if(chosenOne!=null)
					{
						mob.tell(L("@x1 removed.",chosenOne.ID()));
						M.delAbility(chosenOne);
						if(M.fetchEffect(chosenOne.ID())!=null)
							M.delEffect(M.fetchEffect(chosenOne.ID()));
					}
					else
					{
						chosenOne=CMClass.getAbility(behave);
						if((chosenOne!=null)
						&&((chosenOne.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_ARCHON)
						&&(!CMSecurity.isASysOp(mob)))
							chosenOne=null;
						if(chosenOne!=null)
						{
							if(parms == null)
								parms = mob.session().prompt(L("Enter any arguments: "),"");
							final boolean alreadyHasIt=(M.fetchAbility(chosenOne.ID())!=null);
							if(!alreadyHasIt)
								mob.tell(L("@x1 added.",chosenOne.ID()));
							else
								mob.tell(L("@x1 re-added.",chosenOne.ID()));
							if(!alreadyHasIt)
							{
								chosenOne=(Ability)chosenOne.copyOf();
								M.addAbility(chosenOne);
								if((parms != null)&&(parms.length()>0))
									chosenOne.setMiscText(parms);
								chosenOne.setProficiency(75);
								chosenOne.autoInvocation(M, false);
							}
						}
						else
						{
							mob.tell(L("'@x1' is not recognized.  Try '?'.",behave));
						}
					}
				}
			}
			else
				mob.tell(L("(no change)"));
		}
	}

	@Override
	public void spellsOrBehavs(final MOB mob, final List<CMObject> V, final int showNumber, final int showFlag, final boolean inParms) throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		String behave="NO";
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(behave.length()>0))
		{
			String affectstr="";
			for(int b=0;b<V.size();b++)
			{
				final CMObject A=V.get(b);
				if((A instanceof Contingent)
				&&(((Contingent)A).isSavable()))
				{

					affectstr+=A.ID();
					final String txt=(A instanceof Ability)?(((Ability)A).text()):((Behavior)A).getParms();
					if(txt.trim().length()>0)
						affectstr+="("+txt.trim()+"), ";
					else
						affectstr+=", ";
				}

			}
			if(affectstr.length()>0)
				affectstr=affectstr.substring(0,affectstr.length()-2);
			mob.tell(L("@x1. Effects: '@x2'.",""+showNumber,affectstr));
			if((showFlag!=showNumber)&&(showFlag>-999))
				return;
			behave=mob.session().prompt(L("Enter a spell/behavior to add/remove (?)\n\r:"),"");
			if(behave.length()>0)
			{
				if(behave.equalsIgnoreCase("?"))
				{
					mob.tell(CMLib.lister().build3ColTable(mob,CMClass.abilities()).toString());
					mob.tell(CMLib.lister().build3ColTable(mob,CMClass.behaviors()).toString());
				}
				else
				{
					CMObject chosenOne=null;
					for(int a=0;a<V.size();a++)
					{
						final CMObject A=V.get(a);
						if((A!=null)&&(A.ID().equalsIgnoreCase(behave)))
							chosenOne=A;
					}
					if(chosenOne!=null)
					{
						mob.tell(L("@x1 removed.",chosenOne.ID()));
						V.remove(chosenOne);
					}
					else
					{
						chosenOne=CMClass.getAbility(behave);
						if(chosenOne==null)
							chosenOne=CMClass.getBehavior(behave);
						if((chosenOne instanceof Ability)
						&&((((Ability)chosenOne).classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_ARCHON)
						&&(!CMSecurity.isASysOp(mob)))
							chosenOne=null;
						if(chosenOne!=null)
						{
							if(inParms)
							{
								String parms="?";
								while(parms.equals("?"))
								{
									parms=(chosenOne instanceof Ability)?(((Ability)chosenOne).text()):((Behavior)chosenOne).getParms();
									final String s=mob.session().prompt(L("Enter any parameters (?)\n\r:@x1",parms));
									if(s.equals("?"))
									{
										final String s2=CMLib.help().getHelpText(chosenOne.ID(),mob,true);
										if(s2!=null)
											mob.tell(s2.toString());
										else
											mob.tell(L("no help!"));
									}
									else
									if(s.equalsIgnoreCase("null"))
										parms="";
									else
									if(s.length()>0)
										parms=s;
								}
								if(chosenOne instanceof Ability)
									((Ability)chosenOne).setMiscText(parms.trim());
								else
									((Behavior)chosenOne).setParms(parms.trim());
							}
							mob.tell(L("@x1 added.",chosenOne.ID()));
							V.add(chosenOne);
						}
						else
						{
							mob.tell(L("'@x1' is not recognized.  Try '?'.",behave));
						}
					}
				}
			}
			else
				mob.tell(L("(no change)"));
		}
	}

	@Override
	public void spellsOrBehaviors(final MOB mob, final List<CMObject> V, final int showNumber, final int showFlag, final boolean inParms) throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		String behave="NO";
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(behave.length()>0))
		{
			String affectstr="";
			for(int b=0;b<V.size();b++)
			{
				if(V.get(b) instanceof Ability)
				{
					final Ability A=(Ability)V.get(b);
					if((A!=null)&&(A.isSavable()))
					{
						affectstr+=A.ID();
						if(A.text().trim().length()>0)
							affectstr+="("+A.text().trim()+"), ";
						else
							affectstr+=", ";
					}
				}
				else
				if(V.get(b) instanceof Behavior)
				{
					final Behavior A=(Behavior)V.get(b);
					if((A!=null)&&(A.isSavable()))
					{
						affectstr+=A.ID();
						if(A.getParms().trim().length()>0)
							affectstr+="("+A.getParms().trim()+"), ";
						else
							affectstr+=", ";
					}
				}
			}
			if(affectstr.length()>0)
				affectstr=affectstr.substring(0,affectstr.length()-2);
			mob.tell(L("@x1. Effects/Behavs: '@x2'.",""+showNumber,affectstr));
			if((showFlag!=showNumber)&&(showFlag>-999))
				return;
			behave=mob.session().prompt(L("Enter a spell/behavior to add/remove (?)\n\r:"),"");
			if(behave.length()>0)
			{
				if(behave.equalsIgnoreCase("?"))
				{
					mob.tell(CMLib.lister().build3ColTable(mob,CMClass.abilities()).toString());
					mob.tell(CMLib.lister().build3ColTable(mob,CMClass.behaviors()).toString());
				}
				else
				{
					CMObject chosenOne=null;
					for(int a=0;a<V.size();a++)
					{
						final CMObject A=V.get(a);
						if((A!=null)&&(A.ID().equalsIgnoreCase(behave)))
							chosenOne=A;
					}
					if(chosenOne!=null)
					{
						mob.tell(L("@x1 removed.",chosenOne.ID()));
						V.remove(chosenOne);
					}
					else
					{
						chosenOne=CMClass.getAbility(behave);
						if((chosenOne instanceof Ability)
						&&((((Ability)chosenOne).classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_ARCHON)
						&&(!CMSecurity.isASysOp(mob)))
							chosenOne=null;
						if(chosenOne!=null)
						{
							if(inParms)
							{
								String parms="?";
								while(parms.equals("?"))
								{
									String oldParm;
									if(chosenOne instanceof Ability)
									{
										oldParm=((Ability)chosenOne).text();
										parms=mob.session().prompt(L("Enter any effect parameters (?)\n\r:@x1",oldParm));
									}
									else
									if(chosenOne instanceof Behavior)
									{
										oldParm=((Behavior)chosenOne).getParms();
										parms=mob.session().prompt(L("Enter any behavior parameters (?)\n\r:@x1",oldParm));
									}
									else
										oldParm="";
									if (parms.equals("?"))
									{
										final String s2 = CMLib.help().getHelpText(chosenOne.ID(), mob, true);
										if (s2 != null)
											mob.tell(s2.toString());
										else
											mob.tell(L("no help!"));
									}
									else
									if(parms.equalsIgnoreCase("null"))
										parms="";
									else
									if(parms.length()==0)
										parms=oldParm;
								}
								if(chosenOne instanceof Ability)
									((Ability)chosenOne).setMiscText(parms.trim());
								else
								if(chosenOne instanceof Behavior)
									((Behavior)chosenOne).setParms(parms.trim());
							}
							mob.tell(L("@x1 added.",chosenOne.ID()));
							V.add(chosenOne);
						}
						else
						{
							mob.tell(L("'@x1' is not recognized.  Try '?'.",behave));
						}
					}
				}
			}
			else
				mob.tell(L("(no change)"));
		}
	}

	protected void genClanMembers(final MOB mob, final Clan C, final int showNumber, final int showFlag) throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		String behave="NO";
		final List<MemberRecord> members=C.getMemberList();
		final List<MemberRecord> membersCopy=new XVector<MemberRecord>(members);
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(behave.length()>0))
		{
			String memberStr="";
			for(final Clan.MemberRecord member : members)
				memberStr+=member.name+" ("+C.getRoleName(member.role,true,false)+"), ";
			if(memberStr.length()>0)
				memberStr=memberStr.substring(0,memberStr.length()-2);
			mob.tell(L("@x1. Clan Members : '@x2'.",""+showNumber,memberStr));
			if((showFlag!=showNumber)&&(showFlag>-999))
				return;
			behave=mob.session().prompt(L("Enter a name to add/remove\n\r:"),"");
			if(behave.length()>0)
			{
				int chosenOne=-1;
				for(int m=0;m<members.size();m++)
				{
					if(behave.equalsIgnoreCase(members.get(m).name))
						chosenOne=m;
				}
				if(chosenOne>=0)
				{
					mob.tell(L("@x1 removed.",members.get(chosenOne).name));
					members.remove(chosenOne);
				}
				else
				{
					if(CMLib.players().playerExistsAllHosts(behave))
					{
						final String beName = CMStrings.capitalizeAndLower(behave);
						int oldNum=-1;
						for(int m=0;m<membersCopy.size();m++)
						{
							if(behave.equalsIgnoreCase(membersCopy.get(m).name))
							{
								oldNum=members.size();
								members.add(membersCopy.get(m));
								break;
							}
						}
						int index=oldNum;
						if(index<0)
						{
							index=members.size();
							members.add(new MemberRecord(beName,C.getGovernment().getAcceptPos()));
						}

						int newRole=-1;
						while((mob.session()!=null)&&(!mob.session().isStopped())&&(newRole<0))
						{
							final String newRoleStr=mob.session().prompt(L("Enter this members role (?) '@x1': ",C.getRoleName(members.get(index).role,true,false)),C.getRoleName(members.get(index).role,true,false));
							newRole =C.getRoleFromName(newRoleStr);
							if(newRole<0)
								mob.tell(L("That role is invalid.  Valid roles include: @x1",CMParms.toListString(C.getRolesList())));
							else
								break;
						}
						if(oldNum<0)
							mob.tell(L("@x1 added.",beName));
						else
							mob.tell(L("@x1 re-added.",beName));
						if(newRole>=0)
							members.get(index).role=newRole;
					}
					else
					{
						mob.tell(L("'@x1' is an unrecognized player name.",behave));
					}
				}
				// first add missing ones
				for(int m=0;m<members.size();m++)
				{
					final MemberRecord mR=members.get(m);
					final String newName=mR.name;
					if(!membersCopy.contains(mR))
					{
						final MOB M=CMLib.players().getLoadPlayer(newName);
						if((M!=null)&&(M.getClanRole(C.clanID())==null))
						{
							C.addMember(M, mR.role);
						}
					}
				}
				// now adjust changed roles
				for(int m=0;m<members.size();m++)
				{
					final String newName=members.get(m).name;
					boolean found=false;
					for(final MemberRecord R : membersCopy)
						found=found||R.name.equals(newName);
					if(found)
					{
						final MOB M=CMLib.players().getLoadPlayer(newName);
						final Pair<Clan,Integer> oldClanRole=M.getClanRole(C.clanID());
						final int newRole=members.get(m).role;
						if((oldClanRole!=null)&&(newRole!=oldClanRole.second.intValue()))
						{
							CMLib.database().DBUpdateClanMembership(M.Name(), C.clanID(), newRole);
							M.setClan(C.clanID(),newRole);
							C.updateClanPrivileges(M);
						}
					}
				}
				// now remove old members
				for(int m=0;m<membersCopy.size();m++)
				{
					final String newName=membersCopy.get(m).name;
					boolean found=false;
					for(final MemberRecord R : members)
						found=found||R.name.equals(newName);
					if(!found)
					{
						final MOB M=CMLib.players().getLoadPlayer(newName);
						if((M!=null)
						&&(M.getClanRole(C.clanID())!=null))
						{
							C.delMember(M);
							final String removeMsg =CMLib.achievements().removeClanAchievementAwards(M, C);
							if((removeMsg != null)&&(removeMsg.length()>0))
								M.tell(removeMsg);
						}
					}
				}
			}
			else
				mob.tell(L("(no change)"));
		}
	}

	protected String genDeityRitual(final MOB mob, final String oldVal, final int showNumber, final int showFlag, final String prompt) throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return oldVal;
		while((mob.session()!=null)&&(!mob.session().isStopped()))
		{
			final String ritual = prompt(mob, oldVal, showNumber, showFlag, prompt, false, false);
			if((ritual==null)||(ritual.trim().length()==0))
				return "";
			else
			if((showNumber==showFlag)||(showFlag<=-999))
			{
				final List<String> error = new ArrayList<String>(1);
				final Triggerer triggerer = (Triggerer)CMClass.getCommon("DefaultTriggerer");
				triggerer.addTrigger(new Object(), ritual, null, error);
				if(error.size()>0)
				{
					for(final String e : error)
						mob.tell(e);
					continue;
				}
				return ritual;
			}
			return oldVal;
		}
		return oldVal;
	}

	protected void genDeityClericReq(final MOB mob, final Deity E, final int showNumber, final int showFlag) throws IOException
	{
		E.setClericRequirements(prompt(mob, E.getClericRequirements(), showNumber, showFlag, "Cleric Requirements", false, false));
	}

	protected void genDeityClericRitual(final MOB mob, final Deity E, final int showNumber, final int showFlag) throws IOException
	{
		E.setClericRitual(genDeityRitual(mob, E.getClericRitual(), showNumber, showFlag, "Cleric Ritual"));
	}

	protected void genDeityWorshipReq(final MOB mob, final Deity E, final int showNumber, final int showFlag) throws IOException
	{
		E.setWorshipRequirements(prompt(mob, E.getWorshipRequirements(), showNumber, showFlag, "Worshiper Requirements",false,false));
	}

	protected void genDeityWorshipRitual(final MOB mob, final Deity E, final int showNumber, final int showFlag) throws IOException
	{
		E.setWorshipRitual(genDeityRitual(mob, E.getWorshipRitual(), showNumber, showFlag, "Worshiper Ritual"));
	}

	protected void genDeityBlessings(final MOB mob, final Deity E, final int showNumber, final int showFlag) throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		String behave="NO";
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(behave.length()>0))
		{
			String abilitiestr="";
			for(int a=0;a<E.numBlessings();a++)
			{
				final Ability A=E.fetchBlessing(a);
				if((A!=null)&&(A.isSavable()))
				{
					if(A.text().length()>0)
						abilitiestr+=A.ID()+"("+A.text()+"), ";
					else
						abilitiestr+=A.ID()+", ";
				}
			}
			if(abilitiestr.length()>0)
				abilitiestr=abilitiestr.substring(0,abilitiestr.length()-2);
			mob.tell(L("@x1. Blessings: '@x2'.",""+showNumber,abilitiestr));
			if((showFlag!=showNumber)&&(showFlag>-999))
				return;
			behave=mob.session().prompt(L("Enter an ability to add/remove (?)\n\r:"),"");
			if(behave.length()>0)
			{
				if(behave.equalsIgnoreCase("?"))
					mob.tell(CMLib.lister().build3ColTable(mob,CMClass.abilities(),-1).toString());
				else
				{
					Ability chosenOne=null;
					for(int a=0;a<E.numBlessings();a++)
					{
						final Ability A=E.fetchBlessing(a);
						if((A!=null)&&(A.ID().equalsIgnoreCase(behave)))
							chosenOne=A;
					}
					if(chosenOne!=null)
					{
						mob.tell(L("@x1 removed.",chosenOne.ID()));
						E.delBlessing(chosenOne);
					}
					else
					{
						chosenOne=CMClass.getAbility(behave);
						if((chosenOne!=null)
						&&((chosenOne.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_ARCHON)
						&&(mob.fetchAbility(chosenOne.ID())==null))
							chosenOne=null;
						if(chosenOne!=null)
						{
							boolean alreadyHasIt=false;
							for(int a=0;a<E.numBlessings();a++)
							{
								final Ability A=E.fetchBlessing(a);
								if((A!=null)&&(A.ID().equals(chosenOne.ID())))
									alreadyHasIt=true;
							}
							final String arg=mob.session().prompt(L("Enter any arguments: "),"");
							final boolean clericOnly=mob.session().confirm(L("Is this for clerics only (y/N)?"),"N");
							if(!alreadyHasIt)
								mob.tell(L("@x1 added.",chosenOne.ID()));
							else
								mob.tell(L("@x1 re-added.",chosenOne.ID()));
							if(arg.length()>0)
								chosenOne.setMiscText(arg);
							E.addBlessing(chosenOne,clericOnly);
						}
						else
						{
							mob.tell(L("'@x1' is not recognized.  Try '?'.",behave));
						}
					}
				}
			}
			else
				mob.tell(L("(no change)"));
		}
	}

	protected void genDeityCurses(final MOB mob, final Deity E, final int showNumber, final int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		String behave="NO";
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(behave.length()>0))
		{
			String abilitiestr="";
			for(int a=0;a<E.numCurses();a++)
			{
				final Ability A=E.fetchCurse(a);
				if((A!=null)&&(A.isSavable()))
				{
					if(A.text().length()>0)
						abilitiestr+=A.ID()+"("+A.text()+"), ";
					else
						abilitiestr+=A.ID()+", ";
				}
			}
			if(abilitiestr.length()>0)
				abilitiestr=abilitiestr.substring(0,abilitiestr.length()-2);
			mob.tell(L("@x1. Curses: '@x2'.",""+showNumber,abilitiestr));
			if((showFlag!=showNumber)&&(showFlag>-999))
				return;
			behave=mob.session().prompt(L("Enter an ability to add/remove (?)\n\r:"),"");
			if(behave.length()>0)
			{
				if(behave.equalsIgnoreCase("?"))
					mob.tell(CMLib.lister().build3ColTable(mob,CMClass.abilities(),-1).toString());
				else
				{
					Ability chosenOne=null;
					for(int a=0;a<E.numCurses();a++)
					{
						final Ability A=E.fetchCurse(a);
						if((A!=null)&&(A.ID().equalsIgnoreCase(behave)))
							chosenOne=A;
					}
					if(chosenOne!=null)
					{
						mob.tell(L("@x1 removed.",chosenOne.ID()));
						E.delCurse(chosenOne);
					}
					else
					{
						chosenOne=CMClass.getAbility(behave);
						if((chosenOne!=null)
						&&((chosenOne.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_ARCHON)
						&&(mob.fetchAbility(chosenOne.ID())==null))
							chosenOne=null;
						if(chosenOne!=null)
						{
							boolean alreadyHasIt=false;
							for(int a=0;a<E.numCurses();a++)
							{
								final Ability A=E.fetchCurse(a);
								if((A!=null)&&(A.ID().equals(chosenOne.ID())))
									alreadyHasIt=true;
							}
							final String arg=mob.session().prompt(L("Enter any arguments: "),"");
							final boolean clericOnly=mob.session().confirm(L("Is this for clerics only (y/N)?"),"N");
							if(!alreadyHasIt)
								mob.tell(L("@x1 added.",chosenOne.ID()));
							else
								mob.tell(L("@x1 re-added.",chosenOne.ID()));
							if(arg.length()>0)
								chosenOne.setMiscText(arg);
							E.addCurse(chosenOne,clericOnly);
						}
						else
						{
							mob.tell(L("'@x1' is not recognized.  Try '?'.",behave));
						}
					}
				}
			}
			else
				mob.tell(L("(no change)"));
		}
	}

	protected void genDeityPowers(final MOB mob, final Deity E, final int showNumber, final int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		String behave="NO";
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(behave.length()>0))
		{
			String abilitiestr="";
			for(int a=0;a<E.numPowers();a++)
			{
				final Ability A=E.fetchPower(a);
				if((A!=null)&&(A.isSavable()))
				{
					if(A.text().length()>0)
						abilitiestr+=A.ID()+"("+A.text()+"), ";
					else
						abilitiestr+=A.ID()+", ";
				}
			}
			if(abilitiestr.length()>0)
				abilitiestr=abilitiestr.substring(0,abilitiestr.length()-2);
			mob.tell(L("@x1. Granted Powers: '@x2'.",""+showNumber,abilitiestr));
			if((showFlag!=showNumber)&&(showFlag>-999))
				return;
			behave=mob.session().prompt(L("Enter an ability to add/remove (?)\n\r:"),"");
			if(behave.length()>0)
			{
				if(behave.equalsIgnoreCase("?"))
					mob.tell(CMLib.lister().build3ColTable(mob,CMClass.abilities(),-1).toString());
				else
				{
					Ability chosenOne=null;
					for(int a=0;a<E.numPowers();a++)
					{
						final Ability A=E.fetchPower(a);
						if((A!=null)&&(A.ID().equalsIgnoreCase(behave)))
							chosenOne=A;
					}
					if(chosenOne!=null)
					{
						mob.tell(L("@x1 removed.",chosenOne.ID()));
						E.delPower(chosenOne);
					}
					else
					{
						chosenOne=CMClass.getAbility(behave);
						if((chosenOne!=null)
						&&((chosenOne.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_ARCHON)
						&&(mob.fetchAbility(chosenOne.ID())==null))
							chosenOne=null;
						if(chosenOne!=null)
						{
							boolean alreadyHasIt=false;
							for(int a=0;a<E.numPowers();a++)
							{
								final Ability A=E.fetchPower(a);
								if((A!=null)&&(A.ID().equals(chosenOne.ID())))
									alreadyHasIt=true;
							}
							final String arg=mob.session().prompt(L("Enter any arguments: "),"");
							if(!alreadyHasIt)
								mob.tell(L("@x1 added.",chosenOne.ID()));
							else
								mob.tell(L("@x1 re-added.",chosenOne.ID()));
							if(arg.length()>0)
								chosenOne.setMiscText(arg);
							E.addPower(chosenOne);
						}
						else
						{
							mob.tell(L("'@x1' is not recognized.  Try '?'.",behave));
						}
					}
				}
			}
			else
				mob.tell(L("(no change)"));
		}
	}

	protected void genDeityClericSin(final MOB mob, final Deity E, final int showNumber, final int showFlag) throws IOException
	{
		E.setClericSin(genDeityRitual(mob,E.getClericSin(),showNumber,showFlag,"Cleric Sin"));
	}

	protected void genDeityWorhsipperSin(final MOB mob, final Deity E, final int showNumber, final int showFlag) throws IOException
	{
		E.setWorshipSin(genDeityRitual(mob,E.getWorshipSin(),showNumber,showFlag,"Worshiper Sin"));
	}

	protected void genDeityClericPowerRitual(final MOB mob, final Deity E, final int showNumber, final int showFlag) throws IOException
	{
		E.setClericPowerup(genDeityRitual(mob,E.getClericPowerup(),showNumber,showFlag,"Cleric Power Ritual"));
	}

	protected void genDeityServiceRitual(final MOB mob, final Deity E, final int showNumber, final int showFlag) throws IOException
	{
		E.setServiceRitual(genDeityRitual(mob,E.getServiceRitual(),showNumber,showFlag,"Service Ritual"));
	}

	protected void genPlayerLevel(final MOB mob, final Area A, final int showNumber, final int showFlag) throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		if((showFlag!=showNumber)&&(showFlag>-999))
		{
			final StringBuilder buf=new StringBuilder();
			buf.append(showNumber+". ");
			buf.append(L("Player Level: @x1",""+A.getPlayerLevel()));
			if(A.getPlayerLevel()==0)
				buf.append(L(" (Med MOB Lvl)"));
			mob.tell(buf.toString());
			return;
		}
		A.setPlayerLevel(prompt(mob,A.getPlayerLevel(),showNumber,showFlag,"New Player Level"));
	}

	protected void genGridLocaleX(final MOB mob, final GridZones E, final int showNumber, final int showFlag) throws IOException
	{
		E.setXGridSize(prompt(mob,E.xGridSize(),showNumber,showFlag,"Size (X)"));
	}

	protected void genGridLocaleY(final MOB mob, final GridZones E, final int showNumber, final int showFlag) throws IOException
	{
		E.setYGridSize(prompt(mob,E.yGridSize(),showNumber,showFlag,"Size (Y)"));
	}

	protected void genLocationCoords(final MOB mob, final LocationRoom E, final int showNumber, final int showFlag) throws IOException
	{
		final double[] newDir=new double[2];
		newDir[0]=Math.toRadians(prompt(mob,Math.toDegrees(E.getDirectionFromCore().xyd()),showNumber,showFlag,"Horiz. Dir From Core","This is a horizontal direction in degrees from 0 to 360.",0,360));
		newDir[1]=Math.toRadians(prompt(mob,Math.toDegrees(E.getDirectionFromCore().zd()),showNumber,showFlag,"Vert. Dir From Core","This is a vertical direction in degrees from 0 to 360.",0,360));
		E.setDirectionFromCore(new Dir3D(newDir));
	}

	public void genSpaceGate(final MOB mob, final SpaceObject.SpaceGateway E, final int showNumber, final int showFlag) throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		final StringBuilder buf=new StringBuilder();
		buf.append(showNumber+". ");
		final String targetName = E.knownTarget() == null ? "NONE" : E.knownTarget().Name();
		final String targetLoc = E.knownTarget() == null ? "N/A" : CMLib.english().coordDescShort(E.knownTarget().coordinates().toLongs());
		buf.append(L("Target: @x1  (Coords in space: @x2)",targetName,targetLoc));
		if((showFlag!=showNumber)&&(showFlag>-999))
			return;
		boolean continueThis=true;
		while((mob.session()!=null)
		&&(!mob.session().isStopped())
		&&(continueThis))
		{
			continueThis=false;
			final String newName=mob.session().prompt(L("Enter a new target in space\n\r:"),"");
			if(newName.trim().length()>0)
			{
				SpaceObject O=null;
				if(newName.trim().length()>0)
				{
					if(newName.equalsIgnoreCase("none")||newName.equalsIgnoreCase("null"))
						O=null;
					else
					{
						O=CMLib.space().findSpaceObject(newName, true);
						if(O==null)
							O=CMLib.space().findSpaceObject(newName, false);
						if(O==null)
						{
							mob.tell(L("Space object '@x1' not found.",newName));
							continueThis=true;
							continue;
						}
					}
					E.setKnownTarget(O);
				}
			}
			else
				mob.tell(L("(no change)"));
		}
	}

	public void genSpaceStuff(final MOB mob, final SpaceObject E, final int showNumber, final int showFlag) throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		if((showFlag!=showNumber)&&(showFlag>-999))
		{
			final StringBuilder buf=new StringBuilder();
			buf.append(showNumber+". ");
			buf.append(L("Radius: @x1, Coords in space: @x2\n\r",CMLib.english().sizeDescShort(E.radius()),CMLib.english().coordDescShort(E.coordinates().toLongs())));
			buf.append(L("@x1. Moving: ",CMStrings.SPACES.substring(0,(""+showNumber).length())));
			if(E.speed()<=0)
				buf.append("no");
			else
				buf.append(CMLib.english().speedDescShort(E.speed())+", Direction: "+CMLib.english().directionDescShort(E.direction().toDoubles()));
			mob.tell(buf.toString());
			return;
		}
		while((mob!=null)&&(mob.session()!=null)&&(!mob.session().isStopped()))
		{
			final String val=mob.session().prompt(L("@x1. Radius (ENTER=@x2): ",""+showNumber,(CMLib.english().sizeDescShort(E.radius()))));
			if((val==null)||(val.trim().length()==0))
			{
				mob.tell(L("(unchanged)"));
				break;
			}
			final BigDecimal newValue=CMLib.english().parseSpaceDistance(val);
			if((newValue==null)||(newValue.longValue()<0))
				mob.tell(L("Unknown radius: '@x1', valid units include: @x2.",val,SpaceObject.Distance.getFullList()));
			else
			{
				E.setRadius(newValue.longValue());
				break;
			}
		}
		final String coordHelp1=L("Distances may be any of the following:")+"\n\r"
				+ L("1. 3 distances: '[X],[Y],[Z]', positive or negative.")+"\n\r"
				+ L("2. Relative distance: '[X] FROM [name of another space object]', in random direction.")+"\n\r"
				+ L("3. Relative distance: '[X] FROM [name of another space object] TOWARD [Y],[Z]', where Y,Z are a direction in degrees.");
		final String coordHelp2=L("Valid distance units include: @x1.",SpaceObject.Distance.getFullList());
		while((mob!=null)&&(mob.session()!=null)&&(!mob.session().isStopped()))
		{
			String val=mob.session().prompt(L("@x1. Coordinates in Space (ENTER=@x2): ",""+showNumber,(CMLib.english().coordDescShort(E.coordinates().toLongs()))));
			if((val==null)||(val.trim().length()==0))
			{
				mob.tell(L("(unchanged)"));
				break;
			}
			final List<String> utokens=CMParms.parseSpaces(val.toUpperCase(),true);
			if(utokens.contains("FROM"))
			{
				final List<String> tokens=CMParms.parseSpaces(val,true);
				final int x=utokens.indexOf("FROM");
				final int y=utokens.indexOf("TOWARD");
				Dir3D direction;
				if(y>0)
				{
					final List<String> degreeStr=CMParms.parseCommas(CMParms.combine(tokens,y+1),true);
					if((degreeStr.size()!=2)
					||(!CMath.isInteger(degreeStr.get(0)))||(!CMath.isInteger(degreeStr.get(1)))
					||(CMath.s_int(degreeStr.get(0))<0)||(CMath.s_int(degreeStr.get(0))>359)
					||(CMath.s_int(degreeStr.get(0))<0)||(CMath.s_int(degreeStr.get(1))>359))
					{

						mob.tell(L("Invalid degrees (0-359): ")+CMParms.combine(tokens,y+1)+".\n\r"+coordHelp1+"\n\r"+coordHelp2);
						continue;
					}
					final double[] degreesDbl=CMParms.toDoubleArray(degreeStr);
					direction=new Dir3D(new double[]{Math.toRadians(degreesDbl[0]),Math.toRadians(degreesDbl[1])});
					while(tokens.size()>=y)
						tokens.remove(tokens.size()-1);
				}
				else
					direction=new Dir3D(new double[]{Math.toRadians(CMLib.dice().roll(1, 360, -1)),Math.toRadians(CMLib.dice().roll(1,180,-1))});

				final String distStr=CMParms.combine(tokens,0,x);
				final String objName=CMParms.combine(tokens,x+1);
				final BigDecimal dist=CMLib.english().parseSpaceDistance(distStr);
				if(dist==null)
				{
					mob.tell(L("Unknown distance:")+" '"+distStr+"'. "+coordHelp2);
					continue;
				}
				SpaceObject O=null;
				if(objName.trim().length()>0)
				{
					O=CMLib.space().findSpaceObject(objName, true);
					if(O==null)
						O=CMLib.space().findSpaceObject(objName, false);
				}
				if(O==null)
				{
					mob.tell(L("Unknown relative space object")+" '"+objName+"'.\n\r"+coordHelp2);
					continue;
				}
				val=CMParms.toListString(CMLib.space().getLocation(O.coordinates(), direction, dist.longValue()).toLongs());
			}

			final List<String> valsL=CMParms.parseCommas(val,true);
			if(valsL.size()!=3)
				mob.tell(coordHelp1+"\n\r"+coordHelp2);
			else
			{
				boolean fail=true;
				final BigDecimal[] valL=new BigDecimal[3];
				for(int i=0;i<3;i++)
				{
					final BigDecimal newValue=CMLib.english().parseSpaceDistance(valsL.get(i));
					if(newValue==null)
					{
						mob.tell(L("Unknown coord: '@x1'. @x2",valsL.get(i),coordHelp2));
						break;
					}
					else
					{
						valL[i]=newValue;
						if(i==2)
							fail=false;
					}
				}
				if(!fail)
				{
					E.setCoords(new Coord3D(valL));
					E.coordinates().x(E.coordinates().x().longValue() % SpaceObject.Distance.GalaxyRadius.dm);
					E.coordinates().y(E.coordinates().y().longValue() % SpaceObject.Distance.GalaxyRadius.dm);
					E.coordinates().z(E.coordinates().z().longValue() % SpaceObject.Distance.GalaxyRadius.dm);
					break;
				}
			}
		}
		while((mob!=null)&&(mob.session()!=null)&&(!mob.session().isStopped()))
		{
			String val=mob.session().prompt(L("@x1. Speed in Space (ENTER=@x2): ",""+showNumber,(CMLib.english().speedDescShort(E.speed()))));
			if((val==null)||(val.trim().length()==0))
			{
				mob.tell(L("(unchanged)"));
				break;
			}
			val=val.trim();
			if(val.trim().toLowerCase().endsWith("/sec"))
				val=val.substring(0,val.length()-4);
			if(val.trim().toLowerCase().endsWith("/second"))
				val=val.substring(0,val.length()-7);
			final BigDecimal newValue=CMLib.english().parseSpaceDistance(val);
			if((newValue==null)||(newValue.longValue()<0))
				mob.tell(L("Unknown speed/sec: '@x1', valid units include: @x2.",val,SpaceObject.Distance.getAbbrList()));
			else
			{
				E.setSpeed(newValue.doubleValue());
				break;
			}
		}
		while((mob!=null)&&(mob.session()!=null)&&(!mob.session().isStopped())&&(E.speed()>0))
		{
			String val=mob.session().prompt(L("@x1. Direction in Space (ENTER=@x2): ",""+showNumber,
					(CMLib.english().directionDescShort(E.direction().toDoubles()))));
			if((val==null)||(val.trim().length()==0))
			{
				mob.tell(L("(unchanged)"));
				break;
			}
			val=val.toLowerCase().trim();
			final int x=val.indexOf(" mark ");
			if((x<0)
			||(!CMath.isDouble(val.substring(0,x).trim()))
			||(!CMath.isDouble(val.substring(x+6).trim()))
			||(CMath.s_double(val.substring(0,x).trim())<0)
			||(CMath.s_double(val.substring(0,x).trim())>=360)
			||(CMath.s_double(val.substring(x+6).trim())<0)
			||(CMath.s_double(val.substring(x+6).trim())>=360))
				mob.tell(L("Invalid direction in degrees: '@x1', you might need to include 'mark' in the direction.",val));
			else
			{
				E.setDirection(new Dir3D(new double[]{Math.toRadians(CMath.s_double(val.substring(0,x).trim())),
						Math.toRadians(CMath.s_double(val.substring(x+6).trim()))}));
				break;
			}
		}
	}

	@Override
	public void wornLocation(final MOB mob, final long[] oldWornLocation, final boolean[] logicalAnd, final int showNumber, final int showFlag)
	throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		if((showFlag!=showNumber)&&(showFlag>-999))
		{
			final StringBuilder buf=new StringBuilder(showNumber+". ");
			if(!logicalAnd[0])
				buf.append(L("Wear on any one of: "));
			else
				buf.append(L("Worn on all of: "));
			final Wearable.CODES codes = Wearable.CODES.instance();
			for(int l=1;l<codes.all().length;l++)
			{
				final long wornCode=codes.all()[l];
				if((oldWornLocation[0]&wornCode)>0)
					buf.append(codes.name(l)+", ");
			}
			if(buf.toString().endsWith(", "))
				mob.tell(buf.substring(0,buf.length()-2));
			else
				mob.tell(buf.toString());
			return;
		}
		int codeVal=-1;
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(codeVal!=0))
		{
			mob.tell(L("Wearing parameters\n\r0: Done"));
			if(!logicalAnd[0])
				mob.tell(L("1: Able to wear on any ONE of these locations:"));
			else
				mob.tell(L("1: Must be worn on ALL of these locations:"));
			final Wearable.CODES codes = Wearable.CODES.instance();
			for(int l=0;l<codes.total();l++)
			{
				final long wornCode=codes.get(l);
				if(codes.name(wornCode).length()>0)
				{
					final String header=(l+2)+": ("+codes.name(wornCode)+") : "+(((oldWornLocation[0]&wornCode)==wornCode)?"YES":"NO");
					mob.tell(header);
				}
			}
			String codeStr=mob.session().prompt(L("Select an option number above to TOGGLE\n\r: "));
			codeVal=CMath.s_int(codeStr);
			if((codeVal == 0)&&(codeStr.trim().length()>0))
			{
				codeStr=codeStr.trim().toLowerCase();
				for(int l=0;l<codes.total();l++)
				{
					final long wornCode=codes.get(l);
					if(codes.name(wornCode).length()>0)
					{
						if(codes.name(wornCode).toLowerCase().startsWith(codeStr))
						{
							codeVal=l+2;
							break;
						}
					}
				}
			}
			if((codeVal>0)&&(codeVal<codes.total()+2))
			{
				if(codeVal==1)
					logicalAnd[0]=!logicalAnd[0];
				else
				{
					final long wornCode=codes.get(codeVal-2);
					if((oldWornLocation[0]&wornCode)==wornCode)
						oldWornLocation[0]=(oldWornLocation[0]-wornCode);
					else
						oldWornLocation[0]=(oldWornLocation[0]|wornCode);
				}
			}
		}
	}

	protected void genWornLocation(final MOB mob, final Item E, final int showNumber, final int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		final long[] wornLoc = new long[]{E.rawProperLocationBitmap()};
		final boolean[] logicalAnd = new boolean[]{E.rawLogicalAnd()};
		wornLocation(mob,wornLoc,logicalAnd,showNumber,showFlag);
		E.setRawProperLocationBitmap(wornLoc[0]);
		E.setRawLogicalAnd(logicalAnd[0]);
	}

	protected void genThirstQuenched(final MOB mob, final Drink E, final int showNumber, final int showFlag) throws IOException
	{
		E.setThirstQuenched(prompt(mob, E.thirstQuenched(), showNumber, showFlag, "Quenched/Drink"));
	}

	protected void genDrinkHeld(final MOB mob, final LiquidHolder E, final int showNumber, final int showFlag) throws IOException
	{
		E.setLiquidHeld(prompt(mob,E.liquidHeld(),showNumber,showFlag,"Amount of Drink Held"));
		E.setLiquidRemaining(E.liquidHeld());
	}

	protected void genAttackAttribute(final MOB mob, final CharClass E, final int showNumber, final int showFlag, final String fieldDisplayStr, final String field)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		mob.tell(showNumber+". "+fieldDisplayStr+": '"+CharStats.CODES.DESC(CMath.s_int(E.getStat(field)))+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999))
			return;
		final String newName=mob.session().prompt(L("Enter a new one\n\r:"),"");
		String newStat="";
		for(final int i : CharStats.CODES.BASECODES())
		{
			if(newName.equalsIgnoreCase(CharStats.CODES.DESC(i)))
				newStat=""+i;
		}
		if(newStat.length()>0)
			E.setStat(field,newStat);
		else
			mob.tell(L("(no change)"));
	}

	protected void genArmorCode(final MOB mob, final CharClass E, final int showNumber, final int showFlag, final String fieldDisplayStr, final String field)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		mob.tell(showNumber+". "+fieldDisplayStr+": '"+CharClass.ARMOR_LONGDESC[CMath.s_int(E.getStat(field))]+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999))
			return;
		final String newName=mob.session().prompt(L("Enter (@x1)\n\r:",CMParms.toListString(CharClass.ARMOR_DESCS)),"");
		String newStat="";
		for(int i=0;i<CharClass.ARMOR_DESCS.length;i++)
		{
			if(newName.equalsIgnoreCase(CharClass.ARMOR_DESCS[i]))
				newStat=""+i;
		}
		if(newStat.length()>0)
			E.setStat(field,newStat);
		else
			mob.tell(L("(no change)"));
	}

	protected void genQualifications(final MOB mob, final CharClass E, final int showNumber, final int showFlag, final String fieldDisplayStr, final String field)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		mob.tell(showNumber+". "+fieldDisplayStr+": '"+CMLib.masking().maskDesc(E.getStat(field))+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999))
			return;
		String newName="?";
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(newName.equals("?")))
		{
			newName=mob.session().prompt(L("Enter a new mask (?)\n\r:"),"");
			if(newName.equals("?"))
				mob.tell(CMLib.masking().maskHelp("\n","disallow"));
		}
		if((newName.length()>0)&&(!newName.equals("?")))
			E.setStat(field,newName);
		else
			mob.tell(L("(no change)"));
	}

	protected void genClanAccept(final MOB mob, final Clan E, final int showNumber, final int showFlag) throws IOException
	{
		E.setAcceptanceSettings(prompt(mob, E.getAcceptanceSettings(), showNumber, showFlag, "Clan Qualifications", false, false, CMLib.masking().maskHelp("\n", "disallow")));
	}

	protected void genWeaponRestr(final MOB mob, final CharClass E, final int showNumber, final int showFlag, final String fieldDisplayStr, final String FieldNum, final String field)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		final List<String> set=CMParms.parseCommas(E.getStat(field),true);
		final StringBuilder str=new StringBuilder("");
		for(int v=0;v<set.size();v++)
			str.append(" "+Weapon.CLASS_DESCS[CMath.s_int(set.get(v))].toLowerCase());

		mob.tell(showNumber+". "+fieldDisplayStr+": '"+str.toString()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999))
			return;
		String newName="?";
		boolean setChanged=false;
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(newName.equals("?")))
		{
			newName=mob.session().prompt(L("Enter a weapon class to add/remove (?)\n\r:"),"");
			if(newName.equals("?"))
				mob.tell(CMParms.toListString(Weapon.CLASS_DESCS));
			else
			if(newName.length()>0)
			{
				int foundCode=-1;
				for(int i=0;i<Weapon.CLASS_DESCS.length;i++)
				{
					if(Weapon.CLASS_DESCS[i].equalsIgnoreCase(newName))
						foundCode=i;
				}
				if(foundCode<0)
				{
					mob.tell(L("'@x1' is not recognized.  Try '?'.",newName));
					newName="?";
				}
				else
				{
					final int x=set.indexOf(""+foundCode);
					if(x>=0)
					{
						setChanged=true;
						set.remove(x);
						mob.tell(L("'@x1' removed.",newName));
						newName="?";
					}
					else
					{
						set.add(""+foundCode);
						setChanged=true;
						mob.tell(L("'@x1' added.",newName));
						newName="?";
					}
				}
			}
		}
		if(setChanged)
			E.setStat(field,CMParms.toListString(set));
		else
			mob.tell(L("(no change)"));
	}

	protected void genWeaponMaterials(final MOB mob, final CharClass E, final int showNumber, final int showFlag, final String fieldDisplayStr, final String FieldNum, final String field)
	throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		final List<String> set=CMParms.parseCommas(E.getStat(field),true);
		final StringBuilder str=new StringBuilder("");
		for(int v=0;v<set.size();v++)
			str.append(" "+CMLib.materials().getMaterialDesc(CMath.s_int(set.get(v))));

		mob.tell(showNumber+". "+fieldDisplayStr+": '"+str.toString()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999))
			return;
		String newName="?";
		boolean setChanged=false;
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(newName.equals("?")))
		{
			newName=mob.session().prompt(L("Enter a material type to add/remove to requirements (?)\n\r:"),"");
			if(newName.equals("?"))
				mob.tell(CMParms.toListString(RawMaterial.Material.values()));
			else
			if(newName.length()>0)
			{
				int foundCode=CMLib.materials().findMaterialCode(newName,true);
				if(foundCode<0)
					foundCode=CMLib.materials().findMaterialCode(newName,false);
				if(foundCode<0)
				{
					mob.tell(L("'@x1' is not recognized.  Try '?'.",newName));
					newName="?";
				}
				else
				{
					final int x=set.indexOf(""+foundCode);
					if(x>=0)
					{
						setChanged=true;
						set.remove(x);
						mob.tell(L("'@x1' removed.",newName));
						newName="?";
					}
					else
					{
						set.add(""+foundCode);
						setChanged=true;
						mob.tell(L("'@x1' added.",newName));
						newName="?";
					}
				}
			}
		}
		if(setChanged)
			E.setStat(field,CMParms.toListString(set));
		else
			mob.tell(L("(no change)"));
	}

	protected void genDisableFlags(final MOB mob, final Race E, final int showNumber, final int showFlag)
	throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		int flags=CMath.s_int(E.getStat("DISFLAGS"));
		String newName="?";
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(newName.equals("?")))
		{
			final List<String> disabledL=new ArrayList<String>();
			for(int i=0;i<Race.GENFLAG_DESCS.length;i++)
			{
				if(CMath.isSet(flags,i))
					disabledL.add(Race.GENFLAG_DESCS[i]);
			}

			mob.tell(L("@x1. Disabled: '@x2'.",""+showNumber,CMParms.toListString(disabledL)));
			if((showFlag!=showNumber)&&(showFlag>-999))
				return;

			newName=mob.session().prompt(L("Enter flag to toggle (?)\n\r:"),"").toUpperCase();
			if(newName.length()==0)
				mob.tell(L("(no change)"));
			else
			if(CMParms.contains(Race.GENFLAG_DESCS,newName))
			{
				final int bit=CMParms.indexOf(Race.GENFLAG_DESCS,newName);
				if(CMath.isSet(flags,bit))
					flags=flags-(int)CMath.pow(2,bit);
				else
					flags=flags+(int)CMath.pow(2,bit);
			}
			else
			if(newName.equalsIgnoreCase("?"))
			{
				final StringBuilder str=new StringBuilder(L("Valid values: \n\r"));
				for (final String element : Race.GENFLAG_DESCS)
					str.append(element+"\n\r");
				mob.tell(str.toString());
			}
			else
				mob.tell(L("(no change)"));
		}
		E.setStat("DISFLAGS",""+flags);
	}

	protected void genRaceWearFlags(final MOB mob, final Race E, final int showNumber, final int showFlag)
	throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		int flags=CMath.s_int(E.getStat("WEAR"));
		String newName="?";
		final Wearable.CODES codes = Wearable.CODES.instance();
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(newName.equals("?")))
		{
			final StringBuilder wearable=new StringBuilder("");
			for(int i=1;i<codes.total();i++)
			{
				if(CMath.isSet(flags,i-1))
					wearable.append(codes.name(i)+" ");
			}

			mob.tell(L("@x1. UNWearable locations: '@x2'.",""+showNumber,wearable.toString()));
			if((showFlag!=showNumber)&&(showFlag>-999))
				return;

			newName=mob.session().prompt(L("Enter a location to toggle (?)\n\r:"),"").toUpperCase();
			if(newName.length()==0)
				mob.tell(L("(no change)"));
			else
			if(CMParms.containsIgnoreCase(codes.names(),newName))
			{
				final int bit=CMParms.indexOfIgnoreCase(codes.names(),newName)-1;
				if(bit>=0)
				{
					if(CMath.isSet(flags,bit))
						flags=flags-(int)CMath.pow(2,bit);
					else
						flags=flags+(int)CMath.pow(2,bit);
				}
			}
			else
			if(newName.equalsIgnoreCase("?"))
			{
				final StringBuilder str=new StringBuilder(L("Valid values: \n\r"));
				for(final String name : codes.names())
					str.append(name+" ");
				mob.tell(str.toString());
			}
			else
				mob.tell(L("(no change)"));
		}
		E.setStat("WEAR",""+flags);
	}

	protected void genRaceAvailability(final MOB mob, final Race E, final int showNumber, final int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		mob.tell(L("@x1. Availability: '@x2'.",""+showNumber,Area.THEME_PHRASE_EXT[CMath.s_int(E.getStat("AVAIL"))]));
		if((showFlag!=showNumber)&&(showFlag>-999))
			return;
		String newName="?";
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(newName.equals("?")))
		{
			newName=mob.session().prompt(L("Enter a new value (?)\n\r:"),"");
			if(newName.length()==0)
				mob.tell(L("(no change)"));
			else
			if((CMath.isNumber(newName))&&(CMath.s_int(newName)<Area.THEME_PHRASE_EXT.length))
				E.setStat("AVAIL",""+CMath.s_int(newName));
			else
			if(newName.equalsIgnoreCase("?"))
			{
				final StringBuilder str=new StringBuilder(L("Valid values: \n\r"));
				for(int i=0;i<Area.THEME_PHRASE_EXT.length;i++)
					str.append(i+") "+Area.THEME_PHRASE_EXT[i]+"\n\r");
				mob.tell(str.toString());
			}
			else
				mob.tell(L("(no change)"));
		}
	}

	protected void genClassAvailability(final MOB mob, final CharClass E, final int showNumber, final int showFlag)
	throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		mob.tell(L("@x1. Availability: '@x2'.",""+showNumber,Area.THEME_PHRASE_EXT[CMath.s_int(E.getStat("PLAYER"))]));
		if((showFlag!=showNumber)&&(showFlag>-999))
			return;
		String newName="?";
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(newName.equals("?")))
		{
			newName=mob.session().prompt(L("Enter a new value (?)\n\r:"),"");
			if(newName.length()==0)
				mob.tell(L("(no change)"));
			else
			if((CMath.isNumber(newName))&&(CMath.s_int(newName)<Area.THEME_PHRASE_EXT.length))
				E.setStat("PLAYER",""+CMath.s_int(newName));
			else
			if(newName.equalsIgnoreCase("?"))
			{
				final StringBuilder str=new StringBuilder(L("Valid values: \n\r"));
				for(int i=0;i<Area.THEME_PHRASE_EXT.length;i++)
					str.append(i+") "+Area.THEME_PHRASE_EXT[i]+"\n\r");
				mob.tell(str.toString());
			}
			else
				mob.tell(L("(no change)"));
		}
	}

	protected void genCat(final MOB mob, final Race E, final int showNumber, final int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		mob.tell(L("@x1. Racial Category: '@x2'.",""+showNumber,E.racialCategory()));
		if((showFlag!=showNumber)&&(showFlag>-999))
			return;
		String newName=mob.session().prompt(L("Enter a new one\n\r:"),"");
		if(newName.length()>0)
		{
			boolean found=false;
			if(newName.startsWith("new "))
			{
				newName=CMStrings.capitalizeAndLower(newName.substring(4));
				if(newName.length()>0)
					found=true;
			}
			else
			for(final Enumeration<Race> r=CMClass.races();r.hasMoreElements();)
			{
				final Race R=r.nextElement();
				if(newName.equalsIgnoreCase(R.racialCategory()))
				{
					newName=R.racialCategory();
					found=true;
					break;
				}
			}
			if(!found)
			{
				StringBuilder str=new StringBuilder(L("That category does not exist.  Valid categories include: "));
				final HashSet<String> H=new HashSet<String>();
				for(final Enumeration<Race> r=CMClass.races();r.hasMoreElements();)
				{
					final Race R=r.nextElement();
					if(!H.contains(R.racialCategory()))
					{
						H.add(R.racialCategory());
						str.append(R.racialCategory()+", ");
					}
				}
				str=new StringBuilder(str.toString().substring(0,str.length()-2)+". ");
				str.append("To create a new category, put the word 'new' in front of it. ");
				mob.tell(str.toString());
			}
			else
				E.setStat("CAT",newName);
		}
		else
			mob.tell(L("(no change)"));
	}

	protected void genRaceBuddy(final MOB mob, final Race E, final int showNumber, final int showFlag, final String prompt, final String flag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		mob.tell(showNumber+". "+prompt+": '"+E.getStat(flag)+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999))
			return;
		final String newName=mob.session().prompt(L("Enter a new one\n\r:"),"");
		if(newName.length()>0)
		{
			Race R2=CMClass.getRace(newName);
			if(R2==null)
				R2=(Race)CMClass.getLoadNewClassInstance(CMObjectType.RACE,newName,true);
			if((R2!=null)&&(R2.isGeneric()))
				R2=null;
			if(R2==null)
			{
				final StringBuilder str=new StringBuilder(L("That race name is invalid or is generic.  Valid races include: "));
				for(final Enumeration<Race> r=CMClass.races();r.hasMoreElements();)
				{
					final Race R=r.nextElement();
					if(!R.isGeneric())
						str.append(R.ID()+", ");
				}
				mob.tell(str.toString().substring(0,str.length()-2)+".");
			}
			else
			if(CMClass.getRace(newName)==R2)
				E.setStat(flag,R2.ID());
			else
				E.setStat(flag,R2.getClass().getName());
		}
		else
			mob.tell(L("(no change)"));
	}

	protected void genClassBuddy(final MOB mob, final CharClass E, final int showNumber, final int showFlag, final String prompt, final String flag)
	throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		mob.tell(showNumber+". "+prompt+": '"+E.getStat(flag)+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999))
			return;
		final String newName=mob.session().prompt(L("Enter a new one\n\r:"),"");
		if(newName.length()>0)
		{
			CharClass C2=CMClass.getCharClass(newName);
			if(C2==null)
				C2=(CharClass)CMClass.getLoadNewClassInstance(CMObjectType.CHARCLASS,newName,true);
			if((C2!=null)&&(C2.isGeneric()))
				C2=null;
			if(C2==null)
			{
				final StringBuilder str=new StringBuilder(L("That char class name is invalid or is generic.  Valid char classes include: "));
				for(final Enumeration<CharClass> c=CMClass.charClasses();c.hasMoreElements();)
				{
					final CharClass C=c.nextElement();
					if(!C.isGeneric())
						str.append(C.ID()+", ");
				}
				mob.tell(str.toString().substring(0,str.length()-2)+".");
			}
			else
			if(CMClass.getCharClass(newName)==C2)
				E.setStat(flag,C2.ID());
			else
				E.setStat(flag,C2.getClass().getName());
		}
		else
			mob.tell(L("(no change)"));
	}

	protected void genClassRaceQuals(final MOB mob, final CharClass E, final int showNumber, final int showFlag, final String prompt, final String flag)
	throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		boolean cont=true;
		final List<String> newRaces=new XVector<String>(CMParms.parseCommas(E.getStat(flag), true));
		while((!mob.session().isStopped()) && (cont))
		{
			mob.tell(showNumber+". "+prompt+": '"+CMParms.toListString(newRaces)+"'.");
			if((showFlag!=showNumber)&&(showFlag>-999))
				return;
			final String newName=mob.session().prompt(L("Enter a race to add or remove\n\r:"),"");
			if(newName.length()>0)
			{
				final int x=CMParms.indexOfIgnoreCase(newRaces, newName);
				if(x>=0)
				{
					newRaces.remove(x);
					mob.tell(L("Race/RaceCat @x1 removed.",newName));
				}
				else
				{
					boolean found=newName.equalsIgnoreCase("All");
					if(found)
						newRaces.add("All");
					else
					{
						Race R=CMClass.findRace(newName);
						if(R!=null)
						{
							newRaces.add(R.ID());
							found=true;
						}
						else
						{
							for(final Enumeration<Race> r=CMClass.races();r.hasMoreElements();)
							{
								R=r.nextElement();
								if(R.racialCategory().equalsIgnoreCase(newName))
								{
									newRaces.add(R.racialCategory());
									found=true;
									break;
								}
							}
						}
					}
					if(found)
						mob.tell(L("Race/RaceCat @x1 added.",newName));
					else
						mob.tell(L("Could not find any race, racecat, or all."));
				}
			}
			else
				cont=false;
		}
		E.setStat(flag, CMParms.toListString(newRaces));
	}

	protected void genBodyParts(final MOB mob, final Race E, final int showNumber, final int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		final StringBuilder parts=new StringBuilder("");
		for(int i=0;i<Race.BODYPARTSTR.length;i++)
		{
			if(E.bodyMask()[i]!=0)
				parts.append(Race.BODYPARTSTR[i].toLowerCase()+"("+E.bodyMask()[i]+") ");
		}
		mob.tell(L("@x1. Body Parts: @x2.",""+showNumber,parts.toString()));
		if((showFlag!=showNumber)&&(showFlag>-999))
			return;
		while((mob.session()!=null)&&(!mob.session().isStopped()))
		{
			String newName=mob.session().prompt(L("Enter a body part\n\r:"),"");
			if(newName.length()>0)
			{
				final Integer partNum=Race.BODYPARTHASH.get(newName.toUpperCase().trim());
				if(partNum==null)
				{
					final StringBuilder str=new StringBuilder(L("That body part is invalid.  Valid parts include: "));
					for (final String element : Race.BODYPARTSTR)
						str.append(element+", ");
					mob.tell(str.toString().substring(0,str.length()-2)+".");
				}
				else
				{
					newName=mob.session().prompt(L("Enter new number (@x1), 0=none\n\r:",""+E.bodyMask()[partNum.intValue()]),""+E.bodyMask()[partNum.intValue()]);
					if(newName.length()>0)
						E.bodyMask()[partNum.intValue()]=CMath.s_int(newName);
					else
						mob.tell(L("(no change)"));
				}
			}
			else
			{
				mob.tell(L("(no change)"));
				break;
			}
		}
	}

	protected void genPStats(final MOB mob, final Race R, final int showNumber, final int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		final PhyStats S=(PhyStats)CMClass.getCommon("DefaultPhyStats");
		S.setAllValues(0);
		CMLib.coffeeMaker().setPhyStats(S,R.getStat("ESTATS"));
		final StringBuilder parts=new StringBuilder("");
		for(int i=0;i<S.getStatCodes().length;i++)
		{
			if(CMath.s_int(S.getStat(S.getStatCodes()[i]))!=0)
				parts.append(CMStrings.capitalizeAndLower(S.getStatCodes()[i])+"("+S.getStat(S.getStatCodes()[i])+") ");
		}
		mob.tell(L("@x1. PhysStat Adjustments: @x2.",""+showNumber,parts.toString()));
		if((showFlag!=showNumber)&&(showFlag>-999))
			return;
		boolean done=false;
		while((!done)&&(mob.session()!=null)&&(!mob.session().isStopped()))
		{
			String newName=mob.session().prompt(L("Enter a stat name\n\r:"),"");
			if(newName.length()>0)
			{
				String partName=null;
				for(int i=0;i<S.getStatCodes().length;i++)
				{
					if(newName.equalsIgnoreCase(S.getStatCodes()[i]))
					{
						partName=S.getStatCodes()[i];
						break;
					}
				}
				if(partName==null)
				{
					final StringBuilder str=new StringBuilder(L("That stat is invalid.  Valid stats include: "));
					for(int i=0;i<S.getStatCodes().length;i++)
						str.append(S.getStatCodes()[i]+", ");
					mob.tell(str.toString().substring(0,str.length()-2)+".");
				}
				else
				{
					boolean checkChange=false;
					if(partName.equals("DISPOSITION"))
					{
						genDisposition(mob,S,0,0);
						checkChange=true;
					}
					else
					if(partName.equals("SENSES"))
					{
						genSensesMask(mob,S,0,0);
						checkChange=true;
					}
					else
					{
						newName=mob.session().prompt(L("Enter a value\n\r:"),"");
						if(newName.length()>0)
						{
							S.setStat(partName,newName);
							checkChange=true;
						}
						else
							mob.tell(L("(no change)"));
					}
					if(checkChange)
					{
						boolean zereoed=true;
						for(int i=0;i<S.getStatCodes().length;i++)
						{
							if(CMath.s_int(S.getStat(S.getStatCodes()[i]))!=0)
							{
								zereoed=false;
								break;
							}
						}
						if(zereoed)
							R.setStat("ESTATS","");
						else
							R.setStat("ESTATS",CMLib.coffeeMaker().getPhyStatsStr(S));
					}
				}
			}
			else
			{
				mob.tell(L("(no change)"));
				done=true;
			}
		}
	}

	protected void genAState(final MOB mob,
						  final Race R,
						  final String field,
						  final String prompt,
						  final int showNumber,
						  final int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		final CharState S=(CharState)CMClass.getCommon("DefaultCharState"); S.setAllValues(0);
		CMLib.coffeeMaker().setCharState(S,R.getStat(field));
		final StringBuilder parts=new StringBuilder("");
		for(int i=0;i<S.getStatCodes().length;i++)
		{
			if(CMath.s_int(S.getStat(S.getStatCodes()[i]))!=0)
				parts.append(CMStrings.capitalizeAndLower(S.getStatCodes()[i])+"("+S.getStat(S.getStatCodes()[i])+") ");
		}
		mob.tell(showNumber+". "+prompt+": "+parts.toString()+".");
		if((showFlag!=showNumber)&&(showFlag>-999))
			return;
		boolean done=false;
		while((!done)&&(mob.session()!=null)&&(!mob.session().isStopped()))
		{
			String newName=mob.session().prompt(L("Enter a stat name\n\r:"),"");
			if(newName.length()>0)
			{
				String partName=null;
				for(int i=0;i<S.getStatCodes().length;i++)
				{
					if(newName.equalsIgnoreCase(S.getStatCodes()[i]))
					{
						partName=S.getStatCodes()[i];
						break;
					}
				}
				if(partName==null)
				{
					final StringBuilder str=new StringBuilder(L("That stat is invalid.  Valid stats include: "));
					for(int i=0;i<S.getStatCodes().length;i++)
						str.append(S.getStatCodes()[i]+", ");
					mob.tell(str.toString().substring(0,str.length()-2)+".");
				}
				else
				{
					newName=mob.session().prompt(L("Enter a value\n\r:"),"");
					if(newName.length()>0)
					{
						S.setStat(partName,newName);
						boolean zereoed=true;
						for(int i=0;i<S.getStatCodes().length;i++)
						{
							if(CMath.s_int(S.getStat(S.getStatCodes()[i]))!=0)
							{
								zereoed=false;
								break;
							}
						}
						if(zereoed)
							R.setStat(field,"");
						else
							R.setStat(field,CMLib.coffeeMaker().getCharStateStr(S));
					}
					else
						mob.tell(L("(no change)"));
				}
			}
			else
			{
				mob.tell(L("(no change)"));
				done=true;
			}
		}
	}

	protected void genAStats(final MOB mob, final Race R, final String field, final String FieldName, final int showNumber, final int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		final CharStats S=(CharStats)CMClass.getCommon("DefaultCharStats"); S.setAllValues(0);
		CMLib.coffeeMaker().setCharStats(S,R.getStat(field));
		final StringBuilder parts=new StringBuilder("");
		for(final int i : CharStats.CODES.ALLCODES())
		{
			if(S.getStat(i)!=0)
				parts.append(CMStrings.capitalizeAndLower(CharStats.CODES.DESC(i))+"("+S.getStat(i)+") ");
		}
		mob.tell(showNumber+". "+FieldName+": "+parts.toString()+".");
		if((showFlag!=showNumber)&&(showFlag>-999))
			return;
		boolean done=false;
		while((!done)&&(mob.session()!=null)&&(!mob.session().isStopped()))
		{
			String newName=mob.session().prompt(L("Enter a stat name\n\r:"),"");
			if(newName.length()>0)
			{
				int partNum=-1;
				for(final int i : CharStats.CODES.ALLCODES())
				{
					if(newName.equalsIgnoreCase(CharStats.CODES.DESC(i)))
					{
						partNum=i;
						break;
					}
				}
				if(partNum<0)
				{
					final StringBuilder str=new StringBuilder(L("That stat is invalid.  Valid stats include: "));
					for(final int i : CharStats.CODES.ALLCODES())
						str.append(CharStats.CODES.DESC(i)+", ");
					mob.tell(str.toString().substring(0,str.length()-2)+".");
				}
				else
				{
					newName=mob.session().prompt(L("Enter a value\n\r:"),"");
					if(newName.length()>0)
					{
						if(newName.trim().equalsIgnoreCase("0"))
							S.setStat(partNum,CMath.s_int(newName));
						else
						if(partNum==CharStats.STAT_GENDER)
							S.setStat(partNum,newName.charAt(0));
						else
							S.setStat(partNum,CMath.s_int(newName));
						boolean zereoed=true;
						for(final int i : CharStats.CODES.ALLCODES())
						{
							if(S.getStat(i)!=0)
							{
								zereoed=false;
								break;
							}
						}
						if(zereoed)
							R.setStat(field,"");
						else
							R.setStat(field,CMLib.coffeeMaker().getCharStatsStr(S));
					}
					else
						mob.tell(L("(no change)"));
				}
			}
			else
			{
				mob.tell(L("(no change)"));
				done=true;
			}
		}
	}

	protected void genPStats(final MOB mob, final CharClass R, final int showNumber, final int showFlag)
			throws IOException
	{
		genPStats(mob,R,showNumber,showFlag,false);
	}

	protected void genPStats(final MOB mob, final CharClass R, final int showNumber, final int showFlag, final boolean skipRejuv)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		final PhyStats S=(PhyStats)CMClass.getCommon("DefaultPhyStats");
		S.setAllValues(0);
		CMLib.coffeeMaker().setPhyStats(S,R.getStat("ESTATS"));
		final StringBuilder parts=new StringBuilder("");
		for(int i=0;i<S.getStatCodes().length;i++)
			if((i!=PhyStats.STAT_REJUV)||(!skipRejuv))
				if(CMath.s_int(S.getStat(S.getStatCodes()[i]))!=0)
					parts.append(CMStrings.capitalizeAndLower(S.getStatCodes()[i])+"("+S.getStat(S.getStatCodes()[i])+") ");
		mob.tell(L("@x1. PhysStat Adjustments: @x2.",""+showNumber,parts.toString()));
		if((showFlag!=showNumber)&&(showFlag>-999))
			return;
		boolean done=false;
		while((!done)&&(mob.session()!=null)&&(!mob.session().isStopped()))
		{
			String newName=mob.session().prompt(L("Enter a stat name\n\r:"),"");
			if(newName.length()>0)
			{
				String partName=null;
				for(int i=0;i<S.getStatCodes().length;i++)
				{
					if((i!=PhyStats.STAT_REJUV)||(!skipRejuv))
					{
						if(newName.equalsIgnoreCase(S.getStatCodes()[i]))
						{
							partName=S.getStatCodes()[i];
							break;
						}
					}
				}
				if(partName==null)
				{
					final StringBuilder str=new StringBuilder(L("That stat is invalid.  Valid stats include: "));
					for(int i=0;i<S.getStatCodes().length;i++)
						str.append(S.getStatCodes()[i]+", ");
					mob.tell(str.toString().substring(0,str.length()-2)+".");
				}
				else
				{
					boolean checkChange=false;
					if(partName.equals("DISPOSITION"))
					{
						genDisposition(mob,S,0,0);
						checkChange=true;
					}
					else
					if(partName.equals("SENSES"))
					{
						genSensesMask(mob,S,0,0);
						checkChange=true;
					}
					else
					{
						newName=mob.session().prompt(L("Enter a value\n\r:"),"");
						if(newName.length()>0)
						{
							S.setStat(partName,newName);
							checkChange=true;
						}
						else
							mob.tell(L("(no change)"));
					}
					if(checkChange)
					{
						boolean zereoed=true;
						for(int i=0;i<S.getStatCodes().length;i++)
						{
							if(CMath.s_int(S.getStat(S.getStatCodes()[i]))!=0)
							{
								zereoed=false;
								break;
							}
						}
						if(zereoed)
							R.setStat("ESTATS","");
						else
							R.setStat("ESTATS",CMLib.coffeeMaker().getPhyStatsStr(S));
					}
				}
			}
			else
			{
				mob.tell(L("(no change)"));
				done=true;
			}
		}
	}

	protected void genAState(final MOB mob,
							 final CharClass R,
							 final String field,
							 final String prompt,
							 final int showNumber,
							 final int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		final CharState S=(CharState)CMClass.getCommon("DefaultCharState"); S.setAllValues(0);
		CMLib.coffeeMaker().setCharState(S,R.getStat(field));
		final StringBuilder parts=new StringBuilder("");
		for(int i=0;i<S.getStatCodes().length;i++)
		{
			if(CMath.s_int(S.getStat(S.getStatCodes()[i]))!=0)
				parts.append(CMStrings.capitalizeAndLower(S.getStatCodes()[i])+"("+S.getStat(S.getStatCodes()[i])+") ");
		}
		mob.tell(showNumber+". "+prompt+": "+parts.toString()+".");
		if((showFlag!=showNumber)&&(showFlag>-999))
			return;
		boolean done=false;
		while((!done)&&(mob.session()!=null)&&(!mob.session().isStopped()))
		{
			String newName=mob.session().prompt(L("Enter a stat name\n\r:"),"");
			if(newName.length()>0)
			{
				String partName=null;
				for(int i=0;i<S.getStatCodes().length;i++)
				{
					if(newName.equalsIgnoreCase(S.getStatCodes()[i]))
					{
						partName=S.getStatCodes()[i];
						break;
					}
				}
				if(partName==null)
				{
					final StringBuilder str=new StringBuilder(L("That stat is invalid.  Valid stats include: "));
					for(int i=0;i<S.getStatCodes().length;i++)
						str.append(S.getStatCodes()[i]+", ");
					mob.tell(str.toString().substring(0,str.length()-2)+".");
				}
				else
				{
					newName=mob.session().prompt(L("Enter a value\n\r:"),"");
					if(newName.length()>0)
					{
						S.setStat(partName,newName);
						boolean zereoed=true;
						for(int i=0;i<S.getStatCodes().length;i++)
						{
							if(CMath.s_int(S.getStat(S.getStatCodes()[i]))!=0)
							{
								zereoed=false;
								break;
							}
						}
						if(zereoed)
							R.setStat(field,"");
						else
							R.setStat(field,CMLib.coffeeMaker().getCharStateStr(S));
					}
					else
						mob.tell(L("(no change)"));
				}
			}
			else
			{
				mob.tell(L("(no change)"));
				done=true;
			}
		}
	}

	protected void genAStats(final MOB mob, final CharClass R, final String field, final String FieldName, final int showNumber, final int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		final CharStats S=(CharStats)CMClass.getCommon("DefaultCharStats"); S.setAllValues(0);
		CMLib.coffeeMaker().setCharStats(S,R.getStat(field));
		final StringBuilder parts=new StringBuilder("");
		for(final int i : CharStats.CODES.ALLCODES())
		{
			if(S.getStat(i)!=0)
				parts.append(CMStrings.capitalizeAndLower(CharStats.CODES.DESC(i))+"("+S.getStat(i)+") ");
		}
		mob.tell(showNumber+". "+FieldName+": "+parts.toString()+".");
		if((showFlag!=showNumber)&&(showFlag>-999))
			return;
		boolean done=false;
		while((!done)&&(mob.session()!=null)&&(!mob.session().isStopped()))
		{
			String newName=mob.session().prompt(L("Enter a stat name\n\r:"),"");
			if(newName.length()>0)
			{
				int partNum=-1;
				for(final int i : CharStats.CODES.ALLCODES())
				{
					if(newName.equalsIgnoreCase(CharStats.CODES.DESC(i)))
					{
						partNum=i;
						break;
					}
				}
				if(partNum<0)
				{
					final StringBuilder str=new StringBuilder(L("That stat is invalid.  Valid stats include: "));
					for(final int i : CharStats.CODES.ALLCODES())
						str.append(CharStats.CODES.DESC(i)+", ");
					mob.tell(str.toString().substring(0,str.length()-2)+".");
				}
				else
				{
					newName=mob.session().prompt(L("Enter a value\n\r:"),"");
					if(newName.length()>0)
					{
						S.setStat(partNum,CMath.s_int(newName));
						boolean zereoed=true;
						for(final int i : CharStats.CODES.ALLCODES())
						{
							if(S.getStat(i)!=0)
							{
								zereoed=false;
								break;
							}
						}
						if(zereoed)
							R.setStat(field,"");
						else
							R.setStat(field,CMLib.coffeeMaker().getCharStatsStr(S));
					}
					else
						mob.tell(L("(no change)"));
				}
			}
			else
			{
				mob.tell(L("(no change)"));
				done=true;
			}
		}
	}

	protected void genResources(final MOB mob, final Race E, final int showNumber, final int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(true))
		{
			final StringBuilder parts=new StringBuilder("");
			final int numResources=CMath.s_int(E.getStat("NUMRSC"));
			final PairList<Item,Integer> DV=new PairArrayList<Item,Integer>();
			for(int r=0;r<numResources;r++)
			{
				final Item I=CMClass.getItem(E.getStat("GETRSCID"+r));
				if(I!=null)
				{
					I.setMiscText(E.getStat("GETRSCPARM"+r));
					I.recoverPhyStats();
					boolean done=false;
					for(int v=0;v<DV.size();v++)
					{
						if(I.sameAs(DV.get(v).first))
						{
							DV.get(v).second = Integer.valueOf(DV.get(v).second.intValue() + 1);
							done = true;
							break;
						}
					}
					if(!done)
						DV.add(I,Integer.valueOf(1));
				}
				else
					parts.append("Unknown: "+E.getStat("GETRSCID"+r)+", ");
			}
			for(int v=0;v<DV.size();v++)
			{
				final Item I=DV.get(v).first;
				final int i=DV.get(v).second.intValue();
				if(i<2)
					parts.append(I.name()+", ");
				else
					parts.append(I.name()+" ("+i+"), ");
			}
			if(parts.toString().endsWith(", "))
			{
				parts.deleteCharAt(parts.length() - 1);
				parts.deleteCharAt(parts.length() - 1);
			}
			mob.tell(L("@x1. Resources: @x2.",""+showNumber,parts.toString()));
			if((showFlag!=showNumber)&&(showFlag>-999))
				return;
			final String newName=mob.session().prompt(L("Enter a resource name to remove or\n\rthe word new and an item name to add from your inventory\n\r:"),"");
			if(newName.length()>0)
			{
				int partNum=-1;
				for(int i=0;i<DV.size();i++)
				{
					if(CMLib.english().containsString(DV.get(i).first.name(),newName))
					{
						partNum=i;
						break;
					}
				}
				boolean updateList=false;
				if(partNum<0)
				{
					if(!newName.toLowerCase().startsWith("new "))
						mob.tell(L("That is neither an existing resource name, or the word new followed by a valid item name."));
					else
					{
						Item I=mob.fetchItem(null,Wearable.FILTER_UNWORNONLY,newName.substring(4).trim());
						if(I!=null)
						{
							I=(Item)I.copyOf();
							if(I!=null)
							{
								boolean done=false;
								for(int v=0;v<DV.size();v++)
								{
									if(I.sameAs(DV.get(v).first))
									{
										DV.get(v).second = Integer.valueOf(DV.get(v).second.intValue()+1);
										done=true;
										break;
									}
								}
								if(!done)
									DV.add(I,Integer.valueOf(1));
								else
									I.destroy();
								mob.tell(L("@x1 added.",I.name()));
								updateList=true;
							}
						}
					}
				}
				else
				{
					final Item I=DV.get(partNum).first;
					final int i=DV.get(partNum).second.intValue();
					if(i<2)
						DV.remove(partNum);
					else
						DV.get(partNum).second = Integer.valueOf(i-1);
					mob.tell(L("@x1 removed.",I.name()));
					updateList=true;
				}
				if(updateList)
				{
					int dex=0;
					for(int i=0;i<DV.size();i++)
						dex+=DV.get(i).second.intValue();
					E.setStat("NUMRSC",""+dex);
					dex=0;
					Item I=null;
					Integer N=null;
					for(int i=0;i<DV.size();i++)
					{
						I=DV.get(i).first;
						N=DV.get(i).second;
						for(int n=0;n<N.intValue();n++)
							E.setStat("GETRSCID"+(dex++),I.ID());
					}
					dex=0;
					for(int i=0;i<DV.size();i++)
					{
						I=DV.get(i).first;
						N=DV.get(i).second;
						for(int n=0;n<N.intValue();n++)
							E.setStat("GETRSCPARM"+(dex++),I.text());
					}
				}
			}
			else
			{
				mob.tell(L("(no change)"));
				return;
			}
		}
	}

	protected void genOutfit(final MOB mob, final Race E, final int showNumber, final int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(true))
		{
			final StringBuilder parts=new StringBuilder("");
			final int numResources=CMath.s_int(E.getStat("NUMOFT"));
			final List<Item> V=new ArrayList<Item>();
			for(int v=0;v<numResources;v++)
			{
				final Item I=CMClass.getItem(E.getStat("GETOFTID"+v));
				if(I!=null)
				{
					I.setMiscText(E.getStat("GETOFTPARM"+v));
					I.recoverPhyStats();
					parts.append(I.name()+", ");
					V.add(I);
				}
			}
			if(parts.toString().endsWith(", "))
			{
				parts.deleteCharAt(parts.length()-1);
				parts.deleteCharAt(parts.length()-1);
			}
			mob.tell(L("@x1. Outfit: @x2.",""+showNumber,parts.toString()));
			if((showFlag!=showNumber)&&(showFlag>-999))
				return;
			final String newName=mob.session().prompt(L("Enter an item name to remove or\n\rthe word new and an item name to add from your inventory\n\r:"),"");
			if(newName.length()>0)
			{
				int partNum=-1;
				for(int i=0;i<V.size();i++)
				{
					if(CMLib.english().containsString(V.get(i).name(),newName))
					{
						partNum = i;
						break;
					}
				}
				boolean updateList=false;
				if(partNum<0)
				{
					if(!newName.toLowerCase().startsWith("new "))
						mob.tell(L("That is neither an existing item name, or the word new followed by a valid item name."));
					else
					{
						Item I=mob.fetchItem(null,Wearable.FILTER_UNWORNONLY,newName.substring(4).trim());
						if(I!=null)
						{
							I=(Item)I.copyOf();
							V.add(I);
							mob.tell(L("@x1 added.",I.name()));
							updateList=true;
						}

					}
				}
				else
				{
					final Item I=V.get(partNum);
					V.remove(partNum);
					mob.tell(L("@x1 removed.",I.name()));
					updateList=true;
				}
				if(updateList)
				{
					E.setStat("NUMOFT","");
					for(int i=0;i<V.size();i++)
						E.setStat("GETOFTID"+i,V.get(i).ID());
					for(int i=0;i<V.size();i++)
						E.setStat("GETOFTPARM"+i,V.get(i).text());
				}
			}
			else
			{
				mob.tell(L("(no change)"));
				return;
			}
		}
	}

	protected void genOutfit(final MOB mob, final CharClass E, final int showNumber, final int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(true))
		{
			final StringBuilder parts=new StringBuilder("");
			final int numResources=CMath.s_int(E.getStat("NUMOFT"));
			final List<Item> V=new ArrayList<Item>();
			for(int v=0;v<numResources;v++)
			{
				final Item I=CMClass.getItem(E.getStat("GETOFTID"+v));
				if(I!=null)
				{
					I.setMiscText(E.getStat("GETOFTPARM"+v));
					I.recoverPhyStats();
					parts.append(I.name()+", ");
					V.add(I);
				}
			}
			if(parts.toString().endsWith(", "))
			{
				parts.deleteCharAt(parts.length()-1);
				parts.deleteCharAt(parts.length()-1);
			}
			mob.tell(L("@x1. Outfit: @x2.",""+showNumber,parts.toString()));
			if((showFlag!=showNumber)&&(showFlag>-999))
				return;
			final String newName=mob.session().prompt(L("Enter an item name to remove or\n\rthe word new and an item name to add from your inventory\n\r:"),"");
			if(newName.length()>0)
			{
				int partNum=-1;
				for(int i=0;i<V.size();i++)
				{
					if(CMLib.english().containsString(V.get(i).name(),newName))
					{
						partNum = i;
						break;
					}
				}
				boolean updateList=false;
				if(partNum<0)
				{
					if(!newName.toLowerCase().startsWith("new "))
						mob.tell(L("That is neither an existing item name, or the word new followed by a valid item name."));
					else
					{
						Item I=mob.fetchItem(null,Wearable.FILTER_UNWORNONLY,newName.substring(4).trim());
						if(I!=null)
						{
							I=(Item)I.copyOf();
							V.add(I);
							mob.tell(L("@x1 added.",I.name()));
							updateList=true;
						}

					}
				}
				else
				{
					final Item I=V.get(partNum);
					V.remove(partNum);
					mob.tell(L("@x1 removed.",I.name()));
					updateList=true;
				}
				if(updateList)
				{
					E.setStat("NUMOFT","");
					for(int i=0;i<V.size();i++)
						E.setStat("GETOFTID"+i,V.get(i).ID());
					for(int i=0;i<V.size();i++)
						E.setStat("GETOFTPARM"+i,V.get(i).text());
				}
			}
			else
			{
				mob.tell(L("(no change)"));
				return;
			}
		}
	}

	protected void genMinimumStatQualifications(final MOB mob, final CharClass E, final int showNumber, final int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(true))
		{
			final StringBuilder parts=new StringBuilder("");
			final int numResources=CMath.s_int(E.getStat("NUMMINSTATS"));
			final Vector<Pair<String,Integer>> V=new Vector<Pair<String,Integer>>();
			for(int v=0;v<numResources;v++)
			{
				final Pair<String,Integer> p=new Pair<String,Integer>(E.getStat("GETMINSTAT"+v),Integer.valueOf(CMath.s_int(E.getStat("GETSTATMIN"+v))));
				V.add(p);
				parts.append(p.first+"("+p.second.toString()+"), ");
			}
			if(parts.toString().endsWith(", "))
			{
				parts.deleteCharAt(parts.length()-1);
				parts.deleteCharAt(parts.length()-1);
			}
			mob.tell(L("@x1. Min. Stats: @x2.",""+showNumber,parts.toString()));
			if((showFlag!=showNumber)&&(showFlag>-999))
				return;
			String newName=mob.session().prompt(L("Enter a stat name to remove or add:"),"");
			if(newName.length()>0)
			{
				int statNum=-1;
				for(final int stat : CharStats.CODES.BASECODES())
				{
					if(CharStats.CODES.NAME(stat).equalsIgnoreCase(newName))
					{
						statNum=stat;
						newName=CMStrings.capitalizeAndLower(newName);
						break;
					}
				}
				boolean updateList=false;
				if(statNum<0)
					mob.tell(L("That is not a stat, like one of these: @x1",CMParms.toListString(CharStats.CODES.BASENAMES())));
				else
				{
					int vNum=-1;
					for(int v=0;v<V.size();v++)
					{
						if(newName.equalsIgnoreCase(V.get(v).first))
							vNum=v;
					}
					if(vNum<0)
					{
						final String newMin=mob.session().prompt(L("Enter a minimum stat value:"),"");
						if((newMin.length()>0)&&(CMath.isInteger(newMin)))
						{
							V.add(new Pair<String,Integer>(newName,Integer.valueOf(CMath.s_int(newMin))));
							mob.tell(L("@x1 added.",newName));
							updateList=true;
						}
					}
					else
					{
						V.removeElementAt(vNum);
						mob.tell(L("@x1 removed.",newName));
						updateList=true;
					}
				}
				if(updateList)
				{
					E.setStat("NUMMINSTATS",""+V.size());
					for(int i=0;i<V.size();i++)
						E.setStat("GETMINSTAT"+i,V.get(i).first);
					for(int i=0;i<V.size();i++)
						E.setStat("GETSTATMIN"+i,V.get(i).second.toString());
				}
			}
			else
			{
				mob.tell(L("(no change)"));
				return;
			}
		}
	}

	protected void genWeapon(final MOB mob, final Race E, final int showNumber, final int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		final StringBuilder parts=new StringBuilder("");
		final String xmlStr = E.getStat("WEAPONXML");
		final List<Item> iV = new ArrayList<Item>();
		for(final Weapon W : E.getNaturalWeapons())
			iV.add(W);
		for(final Item I : iV)
			parts.append(I.name()).append(", ");
		if(parts.length()>0)
			parts.delete(parts.length()-2, parts.length());
		mob.tell(L("@x1. Natural Weapon(s): @x2.",""+showNumber,parts.toString()));
		if((showFlag!=showNumber)&&(showFlag>-999))
			return;
		String newName=mob.session().prompt(L("Enter a weapon name to add (from inv) or remove\n\r:"),"");
		while(newName.trim().length()>0)
		{
			Item delI=null;
			for(final Item I : iV)
			{
				if(I.name().equalsIgnoreCase(newName))
					delI=I;
			}
			if(delI!=null)
			{
				iV.remove(delI);
				mob.tell(delI.name()+" deleted.");
			}
			else
			{
				final Item addI=mob.findItem(null, newName);
				if(addI instanceof Weapon)
				{
					iV.add((Item)addI.copyOf());
					mob.tell(addI.name()+" added.");
				}
				else
				if(addI != null)
					mob.tell("Item in inventory is not a weapon: '"+newName+"'");
				else
					mob.tell("Item not found in the list, or in inventory: '"+newName+"'");
			}
			parts.setLength(0);
			for(final Item I : iV)
				parts.append(I.name()).append(", ");
			if(parts.length()>0)
				parts.delete(parts.length()-2, parts.length());
			mob.tell(L("@x1. Natural Weapon(s): @x2.",""+showNumber,parts.toString()));
			newName=mob.session().prompt(L("Enter a weapon name to add (from inv) or remove\n\r:"),"");
		}
		final StringBuilder x = new StringBuilder("");
		x.append("<ITEMS>");
		for(final Item I : iV)
			x.append(CMLib.coffeeMaker().getItemXML(I));
		x.append("</ITEMS>");
		if(x.toString().equalsIgnoreCase(xmlStr))
		{
			mob.tell(L("(no change)"));
			return;
		}
		E.setStat("WEAPONXML", x.toString());
	}

	protected void modifyDField(final DVector fields, final String fieldName, final String value)
	{
		final int x=fields.indexOf(fieldName.toUpperCase());
		if(x<0)
			return;
		fields.setElementAt(x,2,value);
	}

	protected void genAgingChart(final MOB mob, final Race E, final int showNumber, final int showFlag)
	throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;

		mob.tell(L("@x1. Aging Chart: @x2.",""+showNumber,CMParms.toListString(E.getAgingChart())));
		if((showFlag!=showNumber)&&(showFlag>-999))
			return;
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(true))
		{
			final String newName=mob.session().prompt(L("Enter a comma-delimited list of 9 numbers, running from infant -> ancient\n\r:"),"");
			if(newName.length()==0)
			{
				mob.tell(L("(no change)"));
				return;
			}
			final List<String> V=CMParms.parseCommas(newName,true);
			if(V.size()==9)
			{
				int highest=-1;
				boolean cont=false;
				for(int i=0;i<V.size();i++)
				{
					if(CMath.s_int(V.get(i))<highest)
					{
						mob.tell(L("Entry @x1 is out of place.",(V.get(i))));
						cont=true;
						break;
					}
					highest=CMath.s_int(V.get(i));
				}
				if(cont)
					continue;
				E.setStat("AGING",newName);
				break;
			}
		}
	}

	protected void genClassFlags(final MOB mob, final CharClass E, final int showNumber, final int showFlag)
	throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;

		int flags=CMath.s_int(E.getStat("DISFLAGS"));
		final StringBuilder sets=new StringBuilder("");
		if(CMath.bset(flags,CharClass.GENFLAG_NORACE))
			sets.append("Raceless ");
		if(CMath.bset(flags,CharClass.GENFLAG_NOLEVELS))
			sets.append("Leveless ");
		if(CMath.bset(flags,CharClass.GENFLAG_NOEXP))
			sets.append("Expless ");
		if(CMath.bset(flags,CharClass.GENFLAG_THINQUALLIST))
			sets.append("ThinQualList ");

		mob.tell(L("@x1. Extra CharClass Flags: @x2.",""+showNumber,sets.toString()));
		if((showFlag!=showNumber)&&(showFlag>-999))
			return;
		final String newName=mob.session().prompt(L("Enter: 1) Classless, 2) Leveless, 3) Expless\n\r:"),"");
		switch(CMath.s_int(newName))
		{
		case 1:
			if(CMath.bset(flags,CharClass.GENFLAG_NORACE))
				flags=CMath.unsetb(flags,CharClass.GENFLAG_NORACE);
			else
				flags=flags|CharClass.GENFLAG_NORACE;
			break;
		case 2:
			if(CMath.bset(flags,CharClass.GENFLAG_NOLEVELS))
				flags=CMath.unsetb(flags,CharClass.GENFLAG_NOLEVELS);
			else
				flags=flags|CharClass.GENFLAG_NOLEVELS;
			break;
		case 3:
			if(CMath.bset(flags,CharClass.GENFLAG_NOEXP))
				flags=CMath.unsetb(flags,CharClass.GENFLAG_NOEXP);
			else
				flags=flags|CharClass.GENFLAG_NOEXP;
			break;
		default:
			mob.tell(L("(no change)"));
			break;
		}
		E.setStat("DISFLAGS",""+flags);
	}

	protected void genDynamicAbilities(final MOB mob, final Modifiable E, final String typeName, final String levelName, final int showNumber, final int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(true))
		{
			final StringBuilder parts=new StringBuilder("");
			final int numResources=CMath.s_int(E.getStat("NUMRABLE"));
			final Vector<Ability> ables=new Vector<Ability>();
			final Vector<String> data=new Vector<String>();
			for(int v=0;v<numResources;v++)
			{
				final Ability A=CMClass.getAbility(E.getStat("GETRABLE"+v));
				if(A!=null)
				{
					parts.append("("+A.ID()+"/"+E.getStat("GETRABLELVL"+v)+"/"+E.getStat("GETRABLEQUAL"+v)+"/"+E.getStat("GETRABLEPROF"+v)+"/"+E.getStat("GETRABLEPARM"+v));
					if(CMParms.contains(E.getStatCodes(),"GETRABLEROLE") && (E instanceof ClanGovernment))
					{
						final List<String> roleIdsList=CMParms.parseCommas(E.getStat("GETRABLEROLE"+v), true);
						final List<String> roleNamesList=new ArrayList<String>(roleIdsList.size());
						for(final String roleId : roleIdsList)
						{
							final ClanPosition P=((ClanGovernment)E).findPositionRole(roleId);
							if(P!=null)
								roleNamesList.add(P.getID());
						}
						parts.append("/"+CMParms.toListString(roleNamesList)+"), ");
					}
					else
						parts.append("), ");
					ables.addElement(A);
					data.addElement(A.ID()+";"+E.getStat("GETRABLELVL"+v)+";"+E.getStat("GETRABLEQUAL"+v)+";"+E.getStat("GETRABLEPROF"+v)+";"+E.getStat("GETRABLEPARM"+v)+";"+E.getStat("GETRABLEROLE"+v));
				}
			}
			if(parts.toString().endsWith(", "))
			{
				parts.deleteCharAt(parts.length()-1);
				parts.deleteCharAt(parts.length()-1);
			}
			mob.tell(L("@x1. @x2 Abilities: @x3.",""+showNumber,typeName,parts.toString()));
			if((showFlag!=showNumber)&&(showFlag>-999))
				return;
			final String newName=mob.session().prompt(L("Enter an ability name to add or remove (?)\n\r:"),"");
			if(newName.equalsIgnoreCase("?"))
				mob.tell(CMLib.lister().build3ColTable(mob,CMClass.abilities(),-1).toString());
			else
			if(newName.length()>0)
			{
				int partNum=-1;
				for(int i=0;i<ables.size();i++)
				{
					if(CMLib.english().containsString(ables.elementAt(i).ID(),newName))
					{
						partNum=i;
						break;
					}
				}
				boolean updateList=false;
				if(partNum<0)
				{
					final Ability A=CMClass.getAbility(newName);
					if((A==null)
					||(((A.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_ARCHON)
						&&(mob.fetchAbility(A.ID())==null)))
					{
						mob.tell(L("That is neither an existing ability name, nor a valid one to add.  Use ? for a list."));
					}
					else
					if(A.isAutoInvoked() && (!(A instanceof Language)))
						mob.tell(L("'@x1' cannot be named, as it is autoinvoked.",A.name()));
					else
					if((A.triggerStrings()==null)||(A.triggerStrings().length==0))
						mob.tell(L("'@x1' cannot be named, as it has no trigger/command words.",A.name()));
					else
					{
						final StringBuilder str=new StringBuilder(A.ID()+";");
						final String level=mob.session().prompt(L("Enter the level of this skill (1): "),"1");
						str.append((""+CMath.s_int(level))+";");
						if(mob.session().confirm(L("Is this skill automatically gained (Y/n)?"),"Y"))
							str.append("false;");
						else
							str.append("true;");
						final String prof=mob.session().prompt(L("Enter the (perm) proficiency level (100): "),"100");
						str.append((""+CMath.s_int(prof))+";");
						final String parm=mob.session().prompt(L("Enter any default parameters: "),"");
						str.append(""+parm+";");
						String roles="";
						if((CMParms.contains(E.getStatCodes(),"GETRABLEROLE"))&&(E instanceof ClanGovernment))
						{
							final boolean repeat=true;
							while(repeat && (mob.session()!=null)&&(!mob.session().isStopped()))
							{
								final String s=mob.session().prompt(L("Enter one or more roles to restrict this to (?): "),"");
								if(s.trim().equalsIgnoreCase("?"))
									mob.tell(L("Roles: ")+CMParms.toCMObjectListString(((ClanGovernment)E).getPositions()));
								else
								if(s.trim().length()==0)
									break;
								else
								{
									final StringBuilder finalListBuilder=new StringBuilder("");
									final List<String> newRoleList=CMParms.parseCommas(s,true);
									for(final String nr : newRoleList)
									{
										final ClanPosition P=((ClanGovernment)E).findPositionRole(nr);
										if(P==null)
										{
											mob.tell(L("'@x1' is not a valid position. The list is comma-delimited.  Try ?"));
											finalListBuilder.setLength(0);
											break;
										}
										else
											finalListBuilder.append(", ").append(P.getRoleID());
									}
									if(finalListBuilder.length()>2)
									{
										roles=finalListBuilder.substring(2);
										break;
									}
								}
							}
						}
						str.append(roles+";");
						data.addElement(str.toString());
						ables.addElement(A);
						mob.tell(L("@x1 added.",A.name()));
						updateList=true;
					}
				}
				else
				{
					final Ability A=ables.elementAt(partNum);
					ables.removeElementAt(partNum);
					data.removeElementAt(partNum);
					updateList=true;
					mob.tell(L("@x1 removed.",A.name()));
				}
				if(updateList)
				{
					if(data.size()>0)
						E.setStat("NUMRABLE",""+data.size());
					else
						E.setStat("NUMRABLE","");
					for(int i=0;i<data.size();i++)
					{
						final List<String> V=CMParms.parseSemicolons(data.elementAt(i),false);
						E.setStat("GETRABLE"+i,(V.get(0)));
						E.setStat("GETRABLELVL"+i,(V.get(1)));
						E.setStat("GETRABLEQUAL"+i,(V.get(2)));
						E.setStat("GETRABLEPROF"+i,(V.get(3)));
						E.setStat("GETRABLEPARM"+i,(V.get(4)));
						if(CMParms.contains(E.getStatCodes(),"GETRABLEROLE")&&(V.size()>5))
							E.setStat("GETRABLEROLE"+i,(V.get(5)));
					}
				}
			}
			else
			{
				mob.tell(L("(no change)"));
				return;
			}
		}
	}

	protected void genDynamicEffects(final MOB mob, final Modifiable E, final String typeName, final String levelName, final int showNumber, final int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(true))
		{
			final StringBuilder parts=new StringBuilder("");
			final int numResources=CMath.s_int(E.getStat("NUMREFF"));
			final Vector<Ability> ables=new Vector<Ability>();
			final Vector<String> data=new Vector<String>();
			for(int v=0;v<numResources;v++)
			{
				final Ability A=CMClass.getAbility(E.getStat("GETREFF"+v));
				if(A!=null)
				{
					parts.append("("+A.ID()+"/"+E.getStat("GETREFFLVL"+v)+"/"+E.getStat("GETREFFPARM"+v));
					final StringBuilder roles=new StringBuilder("");
					if((E instanceof ClanGovernment)&&(CMParms.contains(E.getStatCodes(), "GETREFFROLE")))
					{
						roles.append("/");
						final List<String> roleIdsList=CMParms.parseCommas(E.getStat("GETREFFROLE"+v), true);
						final List<String> roleNamesList=new ArrayList<String>(roleIdsList.size());
						for(final String roleId : roleIdsList)
						{
							final ClanPosition P=((ClanGovernment)E).findPositionRole(roleId);
							if(P!=null)
								roleNamesList.add(P.getID());
						}
						roles.append(CMParms.toListString(roleNamesList));
					}
					parts.append(roles.toString()+"), ");
					ables.addElement(A);
					data.addElement(A.ID()+"~"+E.getStat("GETREFFLVL"+v)+"~"+E.getStat("GETREFFPARM"+v)+"~"+E.getStat("GETREFFROLE"+v));
				}
			}
			if(parts.toString().endsWith(", "))
			{
				parts.deleteCharAt(parts.length()-1);
				parts.deleteCharAt(parts.length()-1);
			}
			mob.tell(L("@x1. @x2 Effects: @x3.",""+showNumber,typeName,parts.toString()));
			if((showFlag!=showNumber)&&(showFlag>-999))
				return;
			final String newName=mob.session().prompt(L("Enter an effect name to add or remove\n\r:"),"");
			if(newName.equalsIgnoreCase("?"))
				mob.tell(CMLib.lister().build3ColTable(mob,CMClass.abilities(),-1).toString());
			else
			if(newName.length()>0)
			{
				int partNum=-1;
				for(int i=0;i<ables.size();i++)
				{
					if(CMLib.english().containsString(ables.elementAt(i).ID(),newName))
					{
						partNum=i;
						break;
					}
				}
				boolean updateList=false;
				if(partNum<0)
				{
					final Ability A=CMClass.getAbility(newName);
					if((A==null)
					||(((A.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_ARCHON)
						&&(mob.fetchAbility(A.ID())==null)))
					{
						mob.tell(L("That is neither an existing effect name, nor a valid one to add.  Use ? for a list."));
					}
					else
					{
						final StringBuilder str=new StringBuilder(A.ID()+"~");
						final String level=mob.session().prompt(L("Enter the @x1 level to gain this effect (1): ",levelName),"1");
						str.append((""+CMath.s_int(level))+"~");
						final String prof=mob.session().prompt(L("Enter any parameters: "),"");
						str.append(""+prof+"~");
						String roles="";
						if((CMParms.contains(E.getStatCodes(),"GETREFFROLE"))&&(E instanceof ClanGovernment))
						{
							final boolean repeat=true;
							while(repeat && (mob.session()!=null)&&(!mob.session().isStopped()))
							{
								final String s=mob.session().prompt(L("Enter one or more roles to restrict this to (?): "),"");
								if(s.trim().equalsIgnoreCase("?"))
									mob.tell(L("Roles: ")+CMParms.toCMObjectListString(((ClanGovernment)E).getPositions()));
								else
								if(s.trim().length()==0)
									break;
								else
								{
									final StringBuilder finalListBuilder=new StringBuilder("");
									final List<String> newRoleList=CMParms.parseCommas(s,true);
									for(final String nr : newRoleList)
									{
										final ClanPosition P=((ClanGovernment)E).findPositionRole(nr);
										if(P==null)
										{
											mob.tell(L("'@x1' is not a valid position. The list is comma-delimited.  Try ?"));
											finalListBuilder.setLength(0);
											break;
										}
										else
											finalListBuilder.append(", ").append(P.getRoleID());
									}
									if(finalListBuilder.length()>2)
									{
										roles=finalListBuilder.substring(2);
										break;
									}
								}
							}
						}
						str.append(roles+"~");
						data.addElement(str.toString());
						ables.addElement(A);
						mob.tell(L("@x1 added.",A.name()));
						updateList=true;
					}
				}
				else
				{
					final Ability A=ables.elementAt(partNum);
					ables.removeElementAt(partNum);
					data.removeElementAt(partNum);
					updateList=true;
					mob.tell(L("@x1 removed.",A.name()));
				}
				if(updateList)
				{
					if(data.size()>0)
						E.setStat("NUMREFF",""+data.size());
					else
						E.setStat("NUMREFF","");
					for(int i=0;i<data.size();i++)
					{
						final List<String> V=CMParms.parseSquiggleDelimited(data.elementAt(i),false);
						E.setStat("GETREFF"+i,V.get(0));
						E.setStat("GETREFFLVL"+i,V.get(1));
						E.setStat("GETREFFPARM"+i,V.get(2));
						if((CMParms.contains(E.getStatCodes(),"GETREFFROLE"))&&(V.size()>3))
							E.setStat("GETREFFROLE"+i,V.get(3));
					}
				}
			}
			else
			{
				mob.tell(L("(no change)"));
				return;
			}
		}
	}

	protected void genDynamicImmunitiess(final MOB mob, final Modifiable E, final String typeName, final int showNumber, final int showFlag)
			throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(true))
		{
			final StringBuilder parts=new StringBuilder("");
			final int numResources=CMath.s_int(E.getStat("NUMIABLE"));
			final Vector<Ability> ables=new Vector<Ability>();
			final Vector<String> data=new Vector<String>();
			for(int v=0;v<numResources;v++)
			{
				final Ability A=CMClass.getAbility(E.getStat("GETIABLE"+v));
				if(A!=null)
				{
					parts.append(A.ID());
					parts.append(", ");
					ables.addElement(A);
					data.addElement(A.ID());
				}
			}
			if(parts.toString().endsWith(", "))
			{
				parts.deleteCharAt(parts.length()-1);
				parts.deleteCharAt(parts.length()-1);
			}
			mob.tell(L("@x1. @x2 Immunities: @x3.",""+showNumber,typeName,parts.toString()));
			if((showFlag!=showNumber)&&(showFlag>-999))
				return;
			final String newName=mob.session().prompt(L("Enter an ability id to add or remove\n\r:"),"");
			if(newName.equalsIgnoreCase("?"))
				mob.tell(CMLib.lister().build3ColTable(mob,CMClass.abilities(),-1).toString());
			else
			if(newName.length()>0)
			{
				int partNum=-1;
				for(int i=0;i<ables.size();i++)
				{
					if(ables.elementAt(i).ID().equalsIgnoreCase(newName))
					{
						partNum=i;
						break;
					}
				}
				boolean updateList=false;
				if(partNum<0)
				{
					final Ability A=CMClass.getAbility(newName);
					if((A==null)
					||(((A.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_ARCHON)
						&&(mob.fetchAbility(A.ID())==null)))
					{
						mob.tell(L("That is neither an existing immunity ability id, nor a valid one to add.  Use ? for a list."));
					}
					else
					{
						data.addElement(A.ID());
						ables.addElement(A);
						mob.tell(L("@x1 added.",A.name()));
						updateList=true;
					}
				}
				else
				{
					final Ability A=ables.elementAt(partNum);
					ables.removeElementAt(partNum);
					data.removeElementAt(partNum);
					updateList=true;
					mob.tell(L("@x1 removed.",A.name()));
				}
				if(updateList)
				{
					if(data.size()>0)
						E.setStat("NUMIABLE",""+data.size());
					else
						E.setStat("NUMIABLE","");
					for(int i=0;i<data.size();i++)
					{
						E.setStat("GETIABLE"+i,data.get(i));
					}
				}
			}
			else
			{
				mob.tell(L("(no change)"));
				return;
			}
		}
	}

	protected PairList<Integer,List<AbilityMapper.AbilityMapping>> genClassAbleMod(final MOB mob,
								final PairList<Integer,List<AbilityMapper.AbilityMapping>> sets,
								final String ableID, final int origLevelIndex, int origAbleIndex)
										throws IOException
	{
		Integer level=null;
		if(origLevelIndex>=0)
		{
			if(mob.session().confirm(L("Enter Y to DELETE, or N to modify (y/N)?"),"N"))
			{
				final List<AbilityMapper.AbilityMapping> set=sets.get(origLevelIndex).second;
				set.remove(origAbleIndex);
				return null;
			}
			level=sets.get(origLevelIndex).first;
		}
		else
			level=Integer.valueOf(1);
		level=Integer.valueOf(CMath.s_int(mob.session().prompt(L("Enter the level of this skill (@x1): ",""+level),""+level)));
		if(level.intValue()<=0)
		{
			mob.tell(L("Aborted."));
			return null;
		}

		AbilityMapper.AbilityMapping aMAP=CMLib.ableMapper().newAbilityMapping().ID(ableID);
		if(origLevelIndex<0)
		{
			aMAP.abilityID(ableID);
			aMAP.defaultProficiency(0);
			aMAP.maxProficiency(100);
			aMAP.defaultParm("");
			aMAP.originalSkillPreReqList("");
			aMAP.extraMask("");
			aMAP.autoGain(false);
			aMAP.secretFlag(SecretFlag.PUBLIC);
		}
		else
		{
			final List<AbilityMapper.AbilityMapping> levelSet=sets.get(origLevelIndex).second;
			aMAP=levelSet.get(origAbleIndex);
			levelSet.remove(origAbleIndex);
			origAbleIndex=-1;
		}

		int newlevelIndex=sets.indexOfFirst(level);
		List<AbilityMapper.AbilityMapping> levelSet=null;
		if(newlevelIndex<0)
		{
			newlevelIndex=sets.size();
			levelSet=new Vector<AbilityMapper.AbilityMapping>();
			sets.add(level,levelSet);
		}
		else
			levelSet=sets.get(newlevelIndex).second;
		aMAP.defaultProficiency(CMath.s_int(mob.session().prompt(L("Enter the (default) proficiency level (@x1): ",""+aMAP.defaultProficiency()),aMAP.defaultProficiency()+"")));
		aMAP.maxProficiency(CMath.s_int(mob.session().prompt(L("Enter the (maximum) proficiency level (@x1): ",""+aMAP.maxProficiency()),aMAP.maxProficiency()+"")));
		aMAP.autoGain(mob.session().confirm(L("Is this skill automatically gained@x1?",(aMAP.autoGain()?"(Y/n)":"(y/N)")),""+aMAP.autoGain()));
		aMAP.secretFlag(SecretFlag.startsWithIgnoreCase(
				mob.session().choose(L("Is this skill P)ublic, S)ecret, or M)asked @x1?",
						(aMAP.secretFlag()==SecretFlag.PUBLIC?"(P/s/m)":aMAP.secretFlag()==SecretFlag.SECRET?"(p/S/m)":"(p/s/M)")),
						"PSM",""+aMAP.secretFlag().name().charAt(0))
				)
		);
		aMAP.defaultParm(mob.session().prompt(L("Enter any properties (@x1)\n\r: ",aMAP.defaultParm()),aMAP.defaultParm()));
		String s="?";
		while(s.equalsIgnoreCase("?"))
		{
			s=mob.session().prompt(L("Enter any pre-requisites (@x1)\n\r(?) : ",aMAP.originalSkillPreReqList()),aMAP.originalSkillPreReqList());
			if(s.equalsIgnoreCase("?"))
				mob.tell(""+CMLib.help().getHelpText("ABILITY_PREREQS",mob,true));
			else
				aMAP.originalSkillPreReqList(s);
		}
		s="?";
		while(s.equalsIgnoreCase("?"))
		{
			s=mob.session().prompt(L("Enter any requirement mask (@x1)\n\r(?) : ",aMAP.extraMask()),aMAP.extraMask());
			if(s.equalsIgnoreCase("?"))
				mob.tell(""+CMLib.help().getHelpText("MASKS",mob,true));
			else
				aMAP.extraMask(s);
		}
		levelSet.add(aMAP);
		return sets;
	}

	protected void genClassAbilities(final MOB mob, final CharClass E, final int showNumber, final int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		if((showFlag!=showNumber)&&(showFlag>-999))
		{
			mob.tell(L("@x1. Class Abilities: [...].",""+showNumber));
			return;
		}
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(true))
		{
			final StringBuilder parts=new StringBuilder("");
			int numAbles=CMath.s_int(E.getStat("NUMCABLE"));
			final PairList<Integer,List<AbilityMapper.AbilityMapping>> levelSets=
					new PairArrayList<Integer,List<AbilityMapper.AbilityMapping>>();
			int maxAbledLevel=Integer.MIN_VALUE;
			for(int v=0;v<numAbles;v++)
			{
				final Ability A=CMClass.getAbility(E.getStat("GETCABLE"+v));
				if((A!=null)
				&&(!CMLib.ableMapper().getAllQualified(E.ID(), true, A.ID())))
				{
					final AbilityMapper.AbilityMapping aMAP=CMLib.ableMapper().newAbilityMapping().ID(A.ID());
					aMAP.abilityID(A.ID());
					aMAP.autoGain(CMath.s_bool(E.getStat("GETCABLEGAIN"+v)));
					aMAP.defaultProficiency(CMath.s_int(E.getStat("GETCABLEPROF"+v)));
					aMAP.qualLevel(CMath.s_int(E.getStat("GETCABLELVL"+v)));
					SecretFlag secretFlag = (SecretFlag)CMath.s_valueOf(SecretFlag.class, E.getStat("GETCABLESECR"+v));
					if(secretFlag == null)
						secretFlag = CMath.s_bool(E.getStat("GETCABLESECR"+v))?SecretFlag.SECRET:SecretFlag.PUBLIC;
					aMAP.secretFlag(secretFlag);
					aMAP.maxProficiency(CMath.s_int(E.getStat("GETCABLEMAXP"+v)));
					aMAP.defaultParm(E.getStat("GETCABLEPARM"+v));
					aMAP.originalSkillPreReqList(E.getStat("GETCABLEPREQ"+v));
					aMAP.extraMask(E.getStat("GETCABLEMASK"+v));
					final int lvlIndex=levelSets.indexOfFirst(Integer.valueOf(aMAP.qualLevel()));
					List<AbilityMapper.AbilityMapping> set=null;
					if(lvlIndex<0)
					{
						set=new ArrayList<AbilityMapper.AbilityMapping>();
						levelSets.add(Integer.valueOf(aMAP.qualLevel()),set);
						if(aMAP.qualLevel()>maxAbledLevel)
							maxAbledLevel=aMAP.qualLevel();
					}
					else
						set=levelSets.get(lvlIndex).second;
					set.add(aMAP);
				}
			}
			final String header=showNumber+". Class Abilities: ";
			final String spaces=CMStrings.repeat(' ',2+(""+showNumber).length());
			parts.append("\n\r");
			parts.append(spaces+CMStrings.padRight(L("Lvl"),3)+" "
							   +CMStrings.padRight(L("Skill"),25)+" "
							   +CMStrings.padRight(L("Proff"),5)+" "
							   +CMStrings.padRight(L("Gain"),5)+" "
							   +CMStrings.padRight(L("Secret"),6)+" "
							   +CMStrings.padRight(L("Parm"),7)+" "
							   +CMStrings.padRight(L("Preq"),7)+" "
							   +CMStrings.padRight(L("Mask"),6)+"\n\r"
							   );
			for(int i=0;i<=maxAbledLevel;i++)
			{
				final int index=levelSets.indexOfFirst(Integer.valueOf(i));
				if(index<0)
					continue;
				final List<AbilityMapper.AbilityMapping> set=levelSets.get(index).second;
				for(int s=0;s<set.size();s++)
				{
					final AbilityMapper.AbilityMapping aMAP=set.get(s);
					parts.append(spaces+CMStrings.padRight(""+i,3)+" "
									   +CMStrings.padRight(""+aMAP.abilityID(),25)+" "
									   +CMStrings.padRight(""+aMAP.defaultProficiency(),5)+" "
									   +CMStrings.padRight(""+aMAP.autoGain(),5)+" "
									   +CMStrings.padRight(""+aMAP.secretFlag().name().substring(0,6),6)+" "
									   +CMStrings.padRight(""+aMAP.defaultParm(),7)+" "
									   +CMStrings.padRight(""+aMAP.originalSkillPreReqList(),7)+" "
									   +CMStrings.padRight(""+aMAP.extraMask(),6)+"\n\r"
									   );
				}
			}

			mob.session().wraplessPrintln(header+parts.toString());
			final String newName=mob.session().prompt(L("Enter an ability name to add or remove (?)\n\r:"),"");
			if(newName.equalsIgnoreCase("?"))
				mob.tell(CMLib.lister().build3ColTable(mob,CMClass.abilities(),-1).toString());
			else
			if(newName.length()>0)
			{
				int lvlIndex=-1;
				int ableIndex=-1;
				List<AbilityMapper.AbilityMapping> myLevelSet=null;
				for(int s=0;s<levelSets.size();s++)
				{
					final List<AbilityMapper.AbilityMapping> lvls=levelSets.get(s).second;
					for(int l=0;l<lvls.size();l++)
					{
						if(CMLib.english().containsString(lvls.get(l).abilityID(),newName))
						{
							lvlIndex=s;
							ableIndex=l;
							myLevelSet=lvls;
							break;
						}
					}
					if(lvlIndex>=0)
						break;
				}
				boolean updateList=false;
				if(ableIndex<0)
				{
					final Ability A=CMClass.getAbility(newName);
					if((A==null)
					||(((A.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_ARCHON)
						&&(mob.fetchAbility(A.ID())==null)))
					{
						mob.tell(L("That is neither an existing ability name, nor a valid one to add.  Use ? for a list."));
					}
					else
					{
						// add new one here
						if(genClassAbleMod(mob,levelSets,A.ID(),-1,-1)!=null)
						{
							mob.tell(L("@x1 added.",A.ID()));
							updateList=true;
							numAbles++;
						}
					}
				}
				else
				if(myLevelSet!=null)
				{
					final String aID=myLevelSet.get(ableIndex).abilityID();
					if(genClassAbleMod(mob,levelSets,aID,lvlIndex,ableIndex)!=null)
						mob.tell(L("@x1 modified.",aID));
					else
					{
						mob.tell(L("@x1 removed.",aID));
						numAbles--;
					}

					updateList=true;
				}
				if(updateList)
				{
					if(numAbles>0)
						E.setStat("NUMCABLE",""+numAbles);
					else
						E.setStat("NUMCABLE","");
					int dex=0;
					for(int s=0;s<levelSets.size();s++)
					{
						final Integer lvl=levelSets.get(s).first;
						final List<AbilityMapper.AbilityMapping> lvls=levelSets.get(s).second;
						for(int l=0;l<lvls.size();l++)
						{
							final AbilityMapper.AbilityMapping aMAP=lvls.get(l);
							E.setStat("GETCABLELVL"+dex,lvl.toString());
							E.setStat("GETCABLEGAIN"+dex,""+aMAP.autoGain());
							E.setStat("GETCABLEPROF"+dex,""+aMAP.defaultProficiency());
							E.setStat("GETCABLESECR"+dex,""+aMAP.secretFlag().name());
							E.setStat("GETCABLEPARM"+dex,""+aMAP.defaultParm());
							E.setStat("GETCABLEPREQ"+dex,aMAP.originalSkillPreReqList());
							E.setStat("GETCABLEMASK"+dex,aMAP.extraMask());
							E.setStat("GETCABLEMAXP"+dex,""+aMAP.maxProficiency());
							// CABLE MUST BE LAST
							E.setStat("GETCABLE"+dex,aMAP.abilityID());
							dex++;
						}
					}
				}
			}
			else
			{
				mob.tell(L("(no change)"));
				return;
			}
		}
	}

	protected void genRawMaterials(final MOB mob, final Modifiable me, final int showNumber, final int showFlag) throws IOException
	{
		if((showFlag<=0)||(showFlag==showNumber))
		{
			mob.tell(L("@x1. Raw materials: @x2",""+showNumber,me.getStat("MATLIST")));
			if((showFlag==showNumber)||(showFlag<=-999))
			{
				final String promptStr=L("Enter a material or resource to add or remove (?)\n\r:");
				while((mob.session()!=null)&&(!mob.session().isStopped()))
				{
					final String word=mob.session().prompt(promptStr,"");
					if(word.trim().length()==0)
					{
						break;
					}
					if(word.equalsIgnoreCase("?"))
					{
						final StringBuilder str=new StringBuilder(CMParms.toListString(RawMaterial.Material.values()));
						str.append(", ").append(CMParms.toListString(RawMaterial.CODES.NAMES()));
						mob.tell(str.toString());
						continue;
					}
					final List<String> curSet=CMParms.parseCommas(me.getStat("MATLIST").toUpperCase(),true);
					if(curSet.contains(word.toUpperCase().trim()))
					{
						curSet.remove(word.toUpperCase().trim());
						me.setStat("MATLIST", CMParms.toListString(curSet.toArray(new String[0])));
						mob.tell(L("Resource or Material '@x1' removed.",word));
					}
					else
					if((RawMaterial.Material.findIgnoreCase(word)!=null)
					||CMParms.containsIgnoreCase(RawMaterial.CODES.NAMES(), word))
					{
						curSet.add(word.toUpperCase().trim());
						me.setStat("MATLIST", CMParms.toListString(curSet.toArray(new String[0])));
						if(RawMaterial.Material.findIgnoreCase(word)!=null)
							mob.tell(L("Material type '@x1' added.",word));
						else
							mob.tell(L("Raw resource '@x1' added.",word));
					}
					else
						mob.tell(L("'@x1' is not a material or resource.  Try ?",word));
				}
			}
		}
	}

	protected void genCulturalAbilities(final MOB mob, final Race E, final int showNumber, final int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(true))
		{
			final StringBuilder parts=new StringBuilder("");
			final int numResources=CMath.s_int(E.getStat("NUMCABLE"));
			final Vector<Ability> ables=new Vector<Ability>();
			final Vector<String> data=new Vector<String>();
			for(int v=0;v<numResources;v++)
			{
				final Ability A=CMClass.getAbility(E.getStat("GETCABLE"+v));
				if(A!=null)
				{
					parts.append("("+A.ID()+"/"+E.getStat("GETCABLEPROF"+v)+"), ");
					ables.addElement(A);
					data.addElement(A.ID()+";"+
							E.getStat("GETCABLEPROF"+v)+";"+
							E.getStat("GETCABLELVL"+v)+";"+
							E.getStat("GETCABLEGAIN"+v)+";"+
							E.getStat("GETCABLEPARM"+v));
				}
			}
			if(parts.toString().endsWith(", "))
			{
				parts.deleteCharAt(parts.length() - 1);
				parts.deleteCharAt(parts.length() - 1);
			}
			mob.tell(L("@x1. Cultural Abilities: @x2.",""+showNumber,parts.toString()));
			if((showFlag!=showNumber)&&(showFlag>-999))
				return;
			final String newName=mob.session().prompt(L("Enter an ability name to add or remove (?)\n\r:"),"");
			if(newName.equalsIgnoreCase("?"))
				mob.tell(CMLib.lister().build3ColTable(mob,CMClass.abilities(),-1).toString());
			else
			if(newName.length()>0)
			{
				int partNum=-1;
				for(int i=0;i<ables.size();i++)
				{
					if(CMLib.english().containsString(ables.elementAt(i).ID(),newName))
					{
						partNum = i;
						break;
					}
				}
				boolean updateList=false;
				if(partNum<0)
				{
					final Ability A=CMClass.getAbility(newName);
					if((A==null)
					||(((A.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_ARCHON)
						&&(mob.fetchAbility(A.ID())==null)))
					{
						mob.tell(L("That is neither an existing ability name, nor a valid one to add.  Use ? for a list."));
					}
					else
					{
						final StringBuilder str=new StringBuilder(A.ID()+";");
						final String prof=mob.session().prompt(L("Enter the default proficiency level (100): "),"100");
						str.append((""+CMath.s_int(prof)));
						final String levelStr=mob.session().prompt(L("Enter the character level (0): "),"0");
						str.append(";");
						str.append((""+CMath.s_int(levelStr)));
						final String gainStr=Boolean.valueOf(mob.session().confirm(L("Enter Y if it is auto-gained (Y/n)? "),"Y")).toString();
						str.append(";");
						str.append(gainStr);
						final String parmStr=mob.session().prompt(L("Enter default params (): "),"");
						str.append(";");
						str.append(""+parmStr);
						data.addElement(str.toString());
						ables.addElement(A);
						mob.tell(L("@x1 added.",A.name()));
						updateList=true;
					}
				}
				else
				{
					final Ability A=ables.elementAt(partNum);
					ables.removeElementAt(partNum);
					data.removeElementAt(partNum);
					updateList=true;
					mob.tell(L("@x1 removed.",A.name()));
				}
				if(updateList)
				{
					if(data.size()>0)
						E.setStat("NUMCABLE",""+data.size());
					else
						E.setStat("NUMCABLE","");
					for(int i=0;i<data.size();i++)
					{
						final List<String> V=CMParms.parseSemicolons(data.elementAt(i),false);
						E.setStat("GETCABLE"+i,V.get(0));
						E.setStat("GETCABLEPROF"+i,V.get(1));
						E.setStat("GETCABLELVL"+i,V.get(2));
						E.setStat("GETCABLEGAIN"+i,V.get(3));
						E.setStat("GETCABLEPARM"+i,CMParms.combineWith(V,';',4,V.size()));
					}
				}
			}
			else
			{
				mob.tell(L("(no change)"));
				return;
			}
		}
	}

	@Override
	public void modifyGenClass(final MOB mob, final CharClass me, int showFlag)
		throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		if((showFlag == -1) && (CMProps.getIntVar(CMProps.Int.EDITORTYPE)>0))
			showFlag=-999;
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(!ok))
		{
			int showNumber=0;

			promptStatInt(mob,me,++showNumber,showFlag,"Number of Class Names: ","NUMNAME");
			final int numNames=CMath.s_int(me.getStat("NUMNAME"));
			if(numNames<=1)
				promptStatStr(mob,me,++showNumber,showFlag,"Class Name","NAME0");
			else
			for(int i=0;i<numNames;i++)
			{
				promptStatStr(mob,me,++showNumber,showFlag,"Class Name #"+i+": ","NAME"+i);
				if(i>0)
				while(!mob.session().isStopped())
				{
					final int oldNameLevel=CMath.s_int(me.getStat("NAMELEVEL"+i));
					promptStatInt(mob,me,++showNumber,showFlag,"Class Name #"+i+" class level: ","NAMELEVEL"+i);
					final int previousNameLevel=CMath.s_int(me.getStat("NAMELEVEL"+(i-1)));
					final int newNameLevel=CMath.s_int(me.getStat("NAMELEVEL"+i));
					if((oldNameLevel!=newNameLevel)&&(newNameLevel<(previousNameLevel+1)))
					{
						mob.tell(L("This level may not be less than @x1.",""+(previousNameLevel+1)));
						me.setStat("NAMELEVEL"+i,""+(previousNameLevel+1));
						showNumber--;
					}
					else
						break;
				}
			}
			promptStatInt(mob,me,"Use -1 to disable a class Level Cap",++showNumber,showFlag,"Level Cap (?)","LEVELCAP");
			promptStatStr(mob,me,++showNumber,showFlag,"Base Class","BASE");
			genClassAvailability(mob,me,++showNumber,showFlag);
			promptStatStr(mob,me,CMLib.help().getHelpText("MATHFORMULA",mob,true).toString(),++showNumber,showFlag,"HP/Level Formula","HITPOINTSFORMULA",false);
			promptStatStr(mob,me,CMLib.help().getHelpText("MATHFORMULA",mob,true).toString(),++showNumber,showFlag,"Mana/Level Formula","MANAFORMULA",false);
			promptStatInt(mob,me,++showNumber,showFlag,"Prac/Level","LVLPRAC");
			promptStatInt(mob,me,++showNumber,showFlag,"Attack/Level","LVLATT");
			genAttackAttribute(mob,me,++showNumber,showFlag,"Attack Attribute","ATTATT");
			promptStatInt(mob,me,++showNumber,showFlag,"Practices/1stLvl","FSTPRAC");
			promptStatInt(mob,me,++showNumber,showFlag,"Trains/1stLvl","FSTTRAN");
			promptStatInt(mob,me,++showNumber,showFlag,"Levels/Dmg Pt","LVLDAM");
			promptStatStr(mob,me,CMLib.help().getHelpText("MATHFORMULA",mob,true).toString(),++showNumber,showFlag,"Move/Level Formula","MOVEMENTFORMULA",false);
			genArmorCode(mob,me,++showNumber,showFlag,"Armor Restr.","ARMOR");

			final int armorMinorCode=CMath.s_int(me.getStat("ARMORMINOR"));
			final boolean newSpells=prompt(mob,armorMinorCode>0,++showNumber,showFlag,"Armor restricts only spells");
			me.setStat("ARMORMINOR",""+(newSpells?CMMsg.TYP_CAST_SPELL:-1));

			promptStatStr(mob,me,++showNumber,showFlag,"Limitations","STRLMT");
			promptStatStr(mob,me,++showNumber,showFlag,"Bonuses","STRBON");
			genQualifications(mob,me,++showNumber,showFlag,"Qualifications","QUAL");
			genMinimumStatQualifications(mob, me,++showNumber,showFlag);
			genClassRaceQuals(mob, me,++showNumber,showFlag,"Required Races", "RACQUAL");
			genPStats(mob,me,++showNumber,showFlag,true);
			genAStats(mob,me,"ASTATS","CharStat Adjustments",++showNumber,showFlag);
			genAStats(mob,me,"CSTATS","CharStat Settings",++showNumber,showFlag);
			genAState(mob,me,"ASTATE","CharState Adjustments",++showNumber,showFlag);
			genAState(mob,me,"STARTASTATE","New Player CharState Adj.",++showNumber,showFlag);
			genClassFlags(mob,me,++showNumber,showFlag);
			genWeaponRestr(mob,me,++showNumber,showFlag,"Weapon Restr.","NUMWEP","GETWEP");
			genWeaponMaterials(mob,me,++showNumber,showFlag,"Weapon Materials","NUMWMAT","GETWMAT");
			genOutfit(mob,me,++showNumber,showFlag);
			promptStatStr(mob,me,++showNumber,showFlag,"Starting Money","MONEY");
			genClassBuddy(mob,me,++showNumber,showFlag,"Stat-Modifying Class","STATCLASS");
			genClassBuddy(mob,me,++showNumber,showFlag,"Special Events Class","EVENTCLASS");
			promptStatChoices(mob,me,null,++showNumber,showFlag,"Sub Class Switch Rule","SUBRUL", CMParms.toStringArray(CharClass.SubClassRule.values()));
			promptStatInt(mob,me,++showNumber,showFlag,"Max Non-Crafting Skills","MAXNCS");
			promptStatInt(mob,me,++showNumber,showFlag,"Max Crafting Skills","MAXCRS");
			promptStatInt(mob,me,++showNumber,showFlag,"Max All-Common Skills","MAXCMS");
			promptStatInt(mob,me,++showNumber,showFlag,"Max Languages","MAXLGS");
			genClassAbilities(mob,me,++showNumber,showFlag);
			promptStatInt(mob,me,++showNumber,showFlag,"Number of Security Code Sets: ","NUMSSET");
			final int numGroups=CMath.s_int(me.getStat("NUMSSET"));
			for(int i=0;i<numGroups;i++)
			{
				promptStatStr(mob,me,++showNumber,showFlag,"Security Codes in Set #"+i,"SSET"+i);
				while(!mob.session().isStopped())
				{
					final int oldGroupLevel=CMath.s_int(me.getStat("SSETLEVEL"+i));
					promptStatInt(mob,me,++showNumber,showFlag,"Class Level for Security Set #"+i+": ","SSETLEVEL"+i);
					final int previousGroupLevel=CMath.s_int(me.getStat("SSETLEVEL"+(i-1)));
					final int newGroupLevel=CMath.s_int(me.getStat("SSETLEVEL"+i));
					if((oldGroupLevel!=newGroupLevel)
					&&(i>0)
					&&(newGroupLevel<(previousGroupLevel+1)))
					{
						mob.tell(L("This level may not be less than @x1.",""+(previousGroupLevel+1)));
						me.setStat("SSETLEVEL"+i,""+(previousGroupLevel+1));
						showNumber--;
					}
					else
						break;
				}
			}

			if (showFlag < -900)
			{
				ok = true;
				break;
			}
			if (showFlag > 0)
			{
				showFlag = -1;
				continue;
			}
			showFlag=CMath.s_int(mob.session().prompt(L("Edit which? "),""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
			}
		}
	}

	public void modifyClanPosition(final MOB mob, final ClanPosition me, int showFlag) throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		if((showFlag == -1) && (CMProps.getIntVar(CMProps.Int.EDITORTYPE)>0))
			showFlag=-999;
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(!ok))
		{
			int showNumber=0;
			promptStatStr(mob,me,null,++showNumber,showFlag,"Simple ID","ID",false);
			promptStatStr(mob,me,null,++showNumber,showFlag,"Name","NAME",false);
			promptStatStr(mob,me,null,++showNumber,showFlag,"Name (Plural)","PLURALNAME",false);
			promptStatInt(mob, me,++showNumber, showFlag,"Rank (low=better)", "RANK");
			if((me.getRank()<0)||(me.getRank()>99))
				me.setRank(0);
			promptStatDouble(mob, me,++showNumber, showFlag,"Maximum", "MAX");
			if((me.getMax()<0)||(me.getMax()>9999))
				me.setMax(Integer.MAX_VALUE);
			promptStatStr(mob,me,CMLib.masking().maskHelp("\n","disallow"),++showNumber,showFlag,"Position Mask","INNERMASK",false);
			promptStatBool(mob, me,++showNumber, showFlag,"Is Shown", "ISPUBLIC");
			promptStatCommaChoices(mob, me,CMParms.toListString(Clan.Function.values()),++showNumber, showFlag,"Powers", "FUNCTIONS",Clan.Function.values());
			promptStatStr(mob,me,L("Format: CR delimited. x1=name,x2=position"),++showNumber,showFlag,"Title Awards","TITLES",true);

			if (showFlag < -900)
			{
				ok = true;
				break;
			}
			if (showFlag > 0)
			{
				showFlag = -1;
				continue;
			}
			showFlag=CMath.s_int(mob.session().prompt(L("Edit which? "),""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
			}
		}
	}

	public void clanGovernmentPositions(final MOB mob, final ClanGovernment me, final int showNumber, final int showFlag) throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		String list = CMParms.toListString(me.getPositions());
		mob.tell(L("@x1. Positions: @x2",""+showNumber,list));
		if((showFlag!=showNumber)&&(showFlag>-999))
			return;
		final String promptStr=L("Enter a position ID to edit/remove or ADD\n\r:");
		while((mob.session()!=null)&&(!mob.session().isStopped()))
		{
			final String word=mob.session().prompt(promptStr,"");
			if(word.trim().length()==0)
			{
				return;
			}
			if(word.trim().equalsIgnoreCase("ADD"))
			{
				final ClanPosition P=me.addPosition();
				modifyClanPosition(mob,P,showFlag);
			}
			else
			{
				ClanPosition editMe=null;
				for(final ClanPosition pos : me.getPositions())
				{
					if(pos.getID().equalsIgnoreCase(word))
						editMe=pos;
				}
				if(editMe == null)
				{
					list = CMParms.toListString(me.getPositions());
					mob.tell(L("Position @x1 is not listed.  Try one of these: @x2",word,list));
				}
				else
				if(mob.session()!=null)
				{
					final String choice=mob.session().choose(L("Edit or Delete position @x1 (E/D/)?",editMe.getID()), L("ED"), "");
					if(choice.equalsIgnoreCase("E"))
						modifyClanPosition(mob,editMe,showFlag);
					else
					if(choice.equalsIgnoreCase("D"))
					{
						if(me.getPositions().length==1)
							mob.tell(L("You can't delete the last position."));
						else
							me.delPosition(editMe);
					}
				}
			}
		}
	}

	@Override
	public void modifyGovernment(final MOB mob, final ClanGovernment me, int showFlag) throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		if((showFlag == -1) && (CMProps.getIntVar(CMProps.Int.EDITORTYPE)>0))
			showFlag=-999;
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(!ok))
		{
			int showNumber=0;
			promptStatStr(mob,me,null,++showNumber,showFlag,"Type Name","NAME",false);
			promptStatStr(mob,me,null,++showNumber,showFlag,"Category","CATEGORY",false);
			promptStatStr(mob,me,null,++showNumber,showFlag,"Short Desc","SHORTDESC",false);
			promptStatStr(mob,me,null,++showNumber,showFlag,"Long Desc","LONGDESC",60);
			promptStatStr(mob,me,CMLib.masking().maskHelp("\n","disallow"),++showNumber,showFlag,"Member Mask","REQUIREDMASK",true);
			promptStatBool(mob, me,++showNumber, showFlag,"Is Public", "ISPUBLIC");
			promptStatBool(mob, me,++showNumber, showFlag,"Is Family", "ISFAMILYONLY");
			promptStatBool(mob, me,++showNumber, showFlag,"Is Rivalrous", "ISRIVALROUS");
			promptStatStr(mob, me,null,++showNumber, showFlag,"Minimum Members", "OVERRIDEMINMEMBERS",true);
			if((me.getOverrideMinMembers()!=null)&&((me.getOverrideMinMembers().intValue()<0)||(me.getOverrideMinMembers().intValue()>999)))
				me.setOverrideMinMembers(null);

			++showNumber;
			clanGovernmentPositions(mob,me,++showNumber,showFlag);
			promptStatStr(mob,me,"Use @x1 for the clan level.\n\r"+CMLib.help().getHelpText("FORMULA", mob, true),++showNumber,showFlag,"XP Per Level Formula","XPLEVELFORMULA",true);
			promptStatBool(mob, me,++showNumber, showFlag,"Conquest Enabled", "CONQUESTENABLED");
			if(CMath.s_bool(me.getStat("CONQUESTENABLED")))
			{
				promptStatBool(mob, me,++showNumber, showFlag,"Clan Item Loyalty", "CONQUESTITEMLOYALTY");
				promptStatBool(mob, me,++showNumber, showFlag,"Conq. by Worship", "CONQUESTDEITYBASIS");
			}
			promptStatCommaChoices(mob, me,CMParms.toListString(Clan.Function.values()),++showNumber, showFlag,"Vote Approved", "VOTEFUNCS",Clan.Function.values());
			if(me.getStat("VOTEFUNCS").length()>0)
			{
				promptStatInt(mob, me,++showNumber, showFlag,"Max Vote Days", "MAXVOTEDAYS");
				if((me.getMaxVoteDays()<0)||(me.getMaxVoteDays()>999999))
					me.setMaxVoteDays(10);
				promptStatInt(mob, me,++showNumber, showFlag,"Vote Quorum (Pct%)", "VOTEQUORUMPCT");
				if((me.getVoteQuorumPct()<0)||(me.getVoteQuorumPct()>100))
					me.setVoteQuorumPct(100);
			}
			promptStatChoices(mob,me,CMParms.toListString(Clan.AutoPromoteFlag.values()),++showNumber,showFlag,"Auto-Promotion","AUTOPROMOTEBY",Clan.AutoPromoteFlag.values());
			promptStatChoices(mob,me,CMParms.toListString(me.getPositions()),++showNumber,showFlag,"Apply Position","AUTOROLE",me.getPositions());
			promptStatChoices(mob,me,CMParms.toListString(me.getPositions()),++showNumber,showFlag,"Accept Position","ACCEPTPOS",me.getPositions());
			promptStatStr(mob,me,CMLib.help().getHelpText("SCRIPTABLE", mob, true).toString(),++showNumber,showFlag,"Entry Scriptable","ENTRYSCRIPT",true);
			promptStatStr(mob,me,CMLib.help().getHelpText("SCRIPTABLE", mob, true).toString(),++showNumber,showFlag,"Exit Scriptable","EXITSCRIPT",true);
			promptStatStr(mob,me,L("Format: VAR1=\"VALUE\" VAR2=\"VALUE\""),++showNumber,showFlag,"Misc Settings","MISCVARS",true);
			promptStatStr(mob,me,L("Format: CR delimited. x1=name,x2=position"),++showNumber,showFlag,"Title Awards","TITLES",true);

			genDynamicAbilities(mob,me,"Clan & Char","clan",++showNumber,showFlag);
			genDynamicEffects(mob,me,"Clan","clan",++showNumber,showFlag);

			if (showFlag < -900)
			{
				ok = true;
				break;
			}
			if (showFlag > 0)
			{
				showFlag = -1;
				continue;
			}
			showFlag=CMath.s_int(mob.session().prompt(L("Edit which? "),""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
			}
		}
	}

	@Override
	public void modifyGenAbility(final MOB mob, final Ability me, int showFlag) throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		if((showFlag == -1) && (CMProps.getIntVar(CMProps.Int.EDITORTYPE)>0))
			showFlag=-999;
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(!ok))
		{
			int showNumber=0;
			// id is bad to change.. make them delete it.
			//genText(mob,me,null,++showNumber,showFlag,"Enter the class","CLASS");
			final String varHelp = "You can insert variables into the arguments, such as: @x1=caster level, @x2=target level, @x3=level expertise, "
					+ "@x4-@x8=x1-x5 expertise, @x9=adjusted caster level.";
			promptStatStr(mob,me,null,++showNumber,showFlag,"Ability/Skill name","NAME",false);
			promptStatStr(mob,me,CMParms.toListString(Ability.ACODE.DESCS)+","+CMParms.toListString(Ability.DOMAIN.DESCS),++showNumber,showFlag,"Type, Domain","CLASSIFICATION",false);
			promptStatStr(mob,me,null,++showNumber,showFlag,"Command Words (comma sep)","TRIGSTR",false);
			promptStatStr(mob,me,CMParms.toListString(Ability.RANGE_CHOICES),++showNumber,showFlag,"Minimum Range","MINRANGE",false);
			promptStatStr(mob,me,CMParms.toListString(Ability.RANGE_CHOICES),++showNumber,showFlag,"Maximum Range","MAXRANGE",false);
			promptStatStr(mob,me,null,++showNumber,showFlag,"Ticks Between Casts","TICKSBETWEENCASTS",false);
			promptStatStr(mob,me,varHelp,++showNumber,showFlag,"Duration Override (0=NO)","TICKSOVERRIDE",false);
			promptStatStr(mob,me,null,++showNumber,showFlag,"Affect String","DISPLAY",true);
			promptStatBool(mob,me,++showNumber,showFlag,"Is Auto-invoking","AUTOINVOKE");
			promptStatStr(mob,me,"0,"+CMParms.toListString(Ability.FLAG_DESCS),++showNumber,showFlag,"Skill Flags (comma sep)","FLAGS",true);
			promptStatInt(mob,me,"-1,x,"+Integer.MAX_VALUE+","+Integer.MAX_VALUE+"-(1 to 100)",++showNumber,showFlag,"Override Cost","OVERRIDEMANA");
			promptStatStr(mob,me,CMParms.toListString(Ability.USAGE_DESCS),++showNumber,showFlag,"Cost Type","USAGEMASK",false);
			promptStatInt(mob,me,++showNumber,showFlag,"Num. Arguments","NUMARGS");
			promptStatStr(mob,me,"0,"+CMParms.toListString(Ability.CAN_DESCS),++showNumber,showFlag,"Can Affect","CANAFFECTMASK",true);
			promptStatBool(mob,me,++showNumber,showFlag,"Tick/Periodic Affects","TICKAFFECTS");
			promptStatStr(mob,me,"0,"+CMParms.toListString(Ability.CAN_DESCS),++showNumber,showFlag,"Can Target","CANTARGETMASK",true);
			promptStatStr(mob,me,CMParms.toListString(Ability.QUALITY_DESCS),++showNumber,showFlag,"Quality Code","QUALITY",true);
			promptStatStr(mob,me,"The parameters for this field are LIKE the parameters for this property:\n\r\n\r"+
					CMLib.help().getHelpText("Prop_HereAdjuster",mob,true).toString() + "\n\r"+varHelp,++showNumber,showFlag,"Affect Adjustments","HERESTATS",true);
			promptStatStr(mob,me,CMLib.masking().maskHelp("\n","disallow"),++showNumber,showFlag,"Caster Mask","CASTMASK",true);
			promptStatStr(mob,me,CMLib.help().getHelpText("Scriptable",mob,true).toString(),++showNumber,showFlag,"Scriptable Parm","SCRIPT",true);
			promptStatStr(mob,me,CMLib.masking().maskHelp("\n","disallow"),++showNumber,showFlag,"Target Mask","TARGETMASK",true);
			promptRawStatStr(mob,me,null,++showNumber,showFlag,"Fizzle Message","FIZZLEMSG",true);
			promptRawStatStr(mob,me,null,++showNumber,showFlag,"Auto-Cast Message","AUTOCASTMSG",true);
			promptRawStatStr(mob,me,null,++showNumber,showFlag,"Normal-Cast Message","CASTMSG",true);
			promptRawStatStr(mob,me,null,++showNumber,showFlag,"Post-Cast Message","POSTCASTMSG",true);
			promptRawStatStr(mob,me,null,++showNumber,showFlag,"Uninvoke Message","UNINVOKEMSG",true);
			promptStatStr(mob,me,CMParms.toListString(CMMsg.TYPE_DESCS),++showNumber,showFlag,"Attack-Type","ATTACKCODE",true);
			promptStatStr(mob,me,"The ability ID of a silent effect",++showNumber,showFlag,"Quiet effect ID","MOCKABILITY",true);
			promptStatStr(mob,me,"The parameters for this field are LIKE the parameters for this property:\n\r\n\r"+
					CMLib.help().getHelpText("Prop_HereSpellCast",mob,true).toString(),++showNumber,showFlag,"Public effects","POSTCASTAFFECT",true);
			promptStatStr(mob,me,"The parameters for this field are LIKE the parameters for this property:\n\r\n\r"+
					CMLib.help().getHelpText("Prop_HereSpellCast",mob,true).toString(),++showNumber,showFlag,"Extra castings","POSTCASTABILITY",true);
			promptStatStr(mob,me,"Enter a damage or healing formula. Use +-*/()?. "+varHelp+" Formula evaluates >0 for damage, <0 for healing. Requires Can Target!",++showNumber,showFlag,"Damage/Healing Formula","POSTCASTDAMAGE",true);
			promptStatStr(mob,me,null,++showNumber,showFlag,"Help Text","HELP",true);

			if (showFlag < -900)
			{
				ok = true;
				break;
			}
			if (showFlag > 0)
			{
				showFlag = -1;
				continue;
			}
			showFlag=CMath.s_int(mob.session().prompt(L("Edit which? "),""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
			}
		}
	}

	@Override
	public void modifyGenLanguage(final MOB mob, final Language me, int showFlag) throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		if((showFlag == -1) && (CMProps.getIntVar(CMProps.Int.EDITORTYPE)>0))
			showFlag=-999;
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(!ok))
		{
			int showNumber=0;
			// id is bad to change.. make them delete it.
			//genText(mob,me,null,++showNumber,showFlag,"Enter the class","CLASS");
			promptStatStr(mob,me,null,++showNumber,showFlag,"Language name","NAME",false);
			if(me.translationLists(me.ID())!=null)
			{
				for(int i=0;i<=me.translationLists(me.ID()).size();i++)
				{
					promptStatStr(mob,me,null,++showNumber,showFlag,(i+1)+" letter words","WORDS"+(i+1),true);
				}
			}
			++showNumber;
			if((showFlag<=0)||(showFlag==showNumber))
			{
				mob.tell(L("@x1. Hashed words: @x2",""+showNumber,me.getStat("HASHEDWORDS")));
				if((showFlag==showNumber)||(showFlag<=-999))
				{
					final String promptStr=L("Enter a word definition to add or remove\n\r:");
					while((mob.session()!=null)&&(!mob.session().isStopped()))
					{
						String word=mob.session().prompt(promptStr,"");
						if(word.trim().length()==0)
						{
							break;
						}
						final int x=word.indexOf("=");
						String val=null;
						if(x>0)
						{
							val=word.substring(x+1);
							word=word.substring(0,x);
						}
						if((val==null)&&(!me.translationHash(me.ID()).containsKey(word.toUpperCase().trim())))
							mob.tell(L("You can not remove @x1, it is not on the current list.",word));
						else
						if(me.translationHash(me.ID()).containsKey(word.toUpperCase().trim()))
						{
							me.translationHash(me.ID()).remove(word.toUpperCase().trim());
							mob.tell(L("Word '@x1' removed.",word));
						}
						else
						{
							me.translationHash(me.ID()).put(word.toUpperCase().trim(),val);
							mob.tell(L("Word '@x1' added.",word));
						}
					}
				}
			}
			++showNumber;
			if((showFlag<=0)||(showFlag==showNumber))
			{
				mob.tell(L("@x1. Translate IDs: @x2",""+showNumber,me.getStat("INTERPRETS")));
				if((showFlag==showNumber)||(showFlag<=-999))
				{
					final String promptStr=L("Enter a language ID to add or remove\n\r:");
					while((mob.session()!=null)&&(!mob.session().isStopped()))
					{
						String word=mob.session().prompt(promptStr,"");
						if(word.trim().length()==0)
						{
							break;
						}
						if(!(CMClass.findAbility(word) instanceof Language))
							mob.tell(L("You can not add or remove @x1, it is not a known language.",word));
						else
						{
							word = CMClass.findAbility(word).ID();
							if(me.languagesSupported().contains(word))
							{
								if(me.languagesSupported().size()==1)
									mob.tell(L("You must leave at least one translation id."));
								else
								{
									me.languagesSupported().remove(word);
									mob.tell(L("'@x1' removed.",word));
								}
							}
							else
							{
								me.languagesSupported().add(word);
								mob.tell(L("'@x1' added.",word));
							}
						}
					}
				}
			}

			promptStatBool(mob, me, ++showNumber, showFlag, "Is Natural", "NATURALLANG");
			promptStatStr(mob,me,null,++showNumber,showFlag,"Source Verb","VERB",true);
			promptStatStr(mob,me,null,++showNumber,showFlag,"Translation Verb","TRANVERB",true);
			promptStatStr(mob,me,null,++showNumber,showFlag,"Help Text","HELP",true);

			if (showFlag < -900)
			{
				ok = true;
				break;
			}
			if (showFlag > 0)
			{
				showFlag = -1;
				continue;
			}
			showFlag=CMath.s_int(mob.session().prompt(L("Edit which? "),""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
			}
		}
	}

	@Override
	public void modifyGenCraftSkill(final MOB mob, final Ability me, int showFlag) throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		if((showFlag == -1) && (CMProps.getIntVar(CMProps.Int.EDITORTYPE)>0))
			showFlag=-999;
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(!ok))
		{
			int showNumber=0;
			// id is bad to change.. make them delete it.
			//genText(mob,me,null,++showNumber,showFlag,"Enter the class","CLASS");
			promptStatStr(mob,me,null,++showNumber,showFlag,"Skill name","NAME",false);
			promptStatStr(mob,me,null,++showNumber,showFlag,"Skill verb","VERB",false);
			promptStatStr(mob,me,null,++showNumber,showFlag,"Command Words (comma sep)","TRIGSTR",false);
			promptStatStr(mob,me,null,++showNumber,showFlag,"Recipe filename","FILENAME",false);
			genRawMaterials(mob, me, ++showNumber, showFlag);
			promptStatBool(mob,me,null,++showNumber,showFlag,"Can mend","CANMEND");
			promptStatBool(mob,me,null,++showNumber,showFlag,"Can refit","CANREFIT");
			promptStatBool(mob,me,null,++showNumber,showFlag,"Can bundle","CANBUNDLE");
			promptStatBool(mob,me,null,++showNumber,showFlag,"Can sit","CANSIT");
			promptStatStr(mob,me,null,++showNumber,showFlag,"MSP file","SOUND",false);
			promptStatStr(mob,me,null,++showNumber,showFlag,"Help Text","HELP",true);

			if (showFlag < -900)
			{
				ok = true;
				break;
			}
			if (showFlag > 0)
			{
				showFlag = -1;
				continue;
			}
			showFlag=CMath.s_int(mob.session().prompt(L("Edit which? "),""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
			}
		}
	}

	@Override
	public void modifyGenWrightSkill(final MOB mob, final Ability me, int showFlag) throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		if((showFlag == -1) && (CMProps.getIntVar(CMProps.Int.EDITORTYPE)>0))
			showFlag=-999;
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(!ok))
		{
			int showNumber=0;
			// id is bad to change.. make them delete it.
			//genText(mob,me,null,++showNumber,showFlag,"Enter the class","CLASS");
			promptStatStr(mob,me,null,++showNumber,showFlag,"Skill name","NAME",false);
			promptStatStr(mob,me,null,++showNumber,showFlag,"Skill verb","VERB",false);
			promptStatStr(mob,me,null,++showNumber,showFlag,"Command Words (comma sep)","TRIGSTR",false);
			promptStatStr(mob,me,null,++showNumber,showFlag,"Recipe filename","FILENAME",false);
			genRawMaterials(mob, me, ++showNumber, showFlag);
			promptStatBool(mob,me,null,++showNumber,showFlag,"Can mend","CANMEND");
			promptStatBool(mob,me,null,++showNumber,showFlag,"Can make doors","CANDOOR");
			promptStatBool(mob,me,null,++showNumber,showFlag,"Can re-title","CANTITLE");
			promptStatBool(mob,me,null,++showNumber,showFlag,"Can re-desc","CANDESC");
			promptStatBool(mob,me,null,++showNumber,showFlag,"Clan only","CLANONLY");
			promptStatStr(mob,me,null,++showNumber,showFlag,"MSP file","SOUND",false);
			promptStatStr(mob,me,null,++showNumber,showFlag,"Help Text","HELP",true);

			if (showFlag < -900)
			{
				ok = true;
				break;
			}
			if (showFlag > 0)
			{
				showFlag = -1;
				continue;
			}
			showFlag=CMath.s_int(mob.session().prompt(L("Edit which? "),""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
			}
		}
	}

	@Override
	public void modifyGenTrap(final MOB mob, final Trap me, int showFlag) throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		if((showFlag == -1) && (CMProps.getIntVar(CMProps.Int.EDITORTYPE)>0))
			showFlag=-999;
		while((mob.session()!=null)
		&&(!mob.session().isStopped())
		&&(!ok))
		{
			int showNumber=0;
			// id is bad to change.. make them delete it.
			//genText(mob,me,null,++showNumber,showFlag,"Enter the class","CLASS");
			promptStatStr(mob,me,null,++showNumber,showFlag,"Trap name","NAME",false);
			promptStatInt(mob,me,null,++showNumber,showFlag,"Level","BASELEVEL");
			promptStatBool(mob,me,++showNumber,showFlag,"Is Bomb","ISBOMB");
			promptStatStr(mob,me,"0,"+CMParms.toListString(Ability.CAN_DESCS),++showNumber,showFlag,"Can Affect","CANAFFECTMASK",true);
			promptStatStr(mob,me,"0,"+CMParms.toListString(Ability.CAN_DESCS),++showNumber,showFlag,"Can Target","CANTARGETMASK",true);
			promptStatInt(mob,me,null,++showNumber,showFlag,"Reset Ticks","PERMRESET");
			me.setStat("ACOMP",modifyComponents(mob, me.getStat("ACOMP"), ++showNumber, showFlag, "Components"));
			promptStatStr(mob,me,null,++showNumber,showFlag,"Avoid Msg","AVOIDMSG",false);
			promptStatStr(mob,me,null,++showNumber,showFlag,"Spring Msg","TRIGMSG",false);
			final String dmgHelp = "@x1=trapLevel, @x2=abilityCode, @x3=invokerLevel, @x4=targetLevel";
			promptStatStr(mob,me,null,++showNumber,showFlag,"Damage Msg","DAMMSG",true);
			promptStatStr(mob,me,dmgHelp,++showNumber,showFlag,"Damage Formula","DMGF",true);
			promptStatStr(mob,me,"0,"+CMParms.toListString(Weapon.TYPE_DESCS),++showNumber,showFlag,"Damage Type","DMGT",false);
			promptStatStr(mob,me,"0,"+CMParms.toListString(CMMsg.TYPE_DESCS),++showNumber,showFlag,"Dmg Msg Type","DMGM",false);
			me.setStat("ABILITY",genAbilityID(mob, me.getStat("ABILITY"), ++showNumber, showFlag, "Ability ID", true));
			promptStatStr(mob,me,null,++showNumber,showFlag,"Ability Parms","ABILTXT",true);
			promptStatInt(mob,me,null,++showNumber,showFlag,"A.Tick Override","ABILTIK");
			promptStatStr(mob,me,CMLib.help().getHelpText("Scriptable",mob,true).toString(),++showNumber,showFlag,"Scriptable Parm","SCRIPT",true);
			promptStatStr(mob,me,null,++showNumber,showFlag,"Help Text","HELP",true);
			if (showFlag < -900)
			{
				ok = true;
				break;
			}
			if (showFlag > 0)
			{
				showFlag = -1;
				continue;
			}
			showFlag=CMath.s_int(mob.session().prompt(L("Edit which? "),""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
			}
		}
	}

	@Override
	public void modifyGenGatheringSkill(final MOB mob, final Ability me, int showFlag) throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		if((showFlag == -1) && (CMProps.getIntVar(CMProps.Int.EDITORTYPE)>0))
			showFlag=-999;
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(!ok))
		{
			int showNumber=0;
			// id is bad to change.. make them delete it.
			//genText(mob,me,null,++showNumber,showFlag,"Enter the class","CLASS");
			promptStatStr(mob,me,null,++showNumber,showFlag,"Skill name","NAME",false);
			promptStatStr(mob,me,null,++showNumber,showFlag,"Skill verb","VERB",false);
			promptStatStr(mob,me,null,++showNumber,showFlag,"Command Words (comma sep)","TRIGSTR",false);
			promptStatStr(mob,me,CMLib.masking().maskHelp("\n","disallow"),++showNumber,showFlag,"Room Mask","ROOMMASK",true);
			promptStatStr(mob,me,CMLib.masking().maskHelp("\n","disallow"),++showNumber,showFlag,"Player Mask","PLAYERMASK",true);
			genRawMaterials(mob, me, ++showNumber, showFlag);
			promptStatStr(mob,me,CMLib.help().getHelpText("MATHFORMULA",mob,true).toString(),++showNumber,showFlag,"Yield Formula","YIELDFORMULA",false);
			promptStatStr(mob,me,null,++showNumber,showFlag,"Start Msg","MSGSTART",false);
			promptStatStr(mob,me,null,++showNumber,showFlag,"Found Msg","MSGFOUND",false);
			promptStatStr(mob,me,null,++showNumber,showFlag,"Not Found Msg","MSGNOTFOUND",false);
			promptStatStr(mob,me,null,++showNumber,showFlag,"Complete Msg","MSGCOMPLETE",false);
			if(me instanceof ItemCollection)
				genItemXML(mob,(ItemCollection)me,"ITEMXML",++showNumber,showFlag,"Droppable Items");
			promptStatInt(mob,me,++showNumber, showFlag,"Min. Ticks", "MINDUR");
			promptStatInt(mob,me,++showNumber, showFlag,"Base Ticks", "BASEDUR");
			promptStatInt(mob,me,++showNumber, showFlag,"Found @ Tick", "FINDTICK");
			promptStatBool(mob,me,null,++showNumber,showFlag,"Is Cosmetic Only","ISCOSMETIC");
			promptStatBool(mob,me,null,++showNumber,showFlag,"Can sit","CANSIT");
			promptStatBool(mob,me,null,++showNumber,showFlag,"Can bundle","CANBUNDLE");
			promptStatStr(mob,me,null,++showNumber,showFlag,"MSP file","SOUND",false);
			promptStatStr(mob,me,null,++showNumber,showFlag,"Help Text","HELP",true);

			if (showFlag < -900)
			{
				ok = true;
				break;
			}
			if (showFlag > 0)
			{
				showFlag = -1;
				continue;
			}
			showFlag=CMath.s_int(mob.session().prompt(L("Edit which? "),""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
			}
		}
	}

	protected boolean genText(final MOB mob, final PairList<String,String> set, final String[] choices, final String help, final int showNumber, final int showFlag, final String fieldDisplayStr, final String field)
	throws IOException
	{
		final int setDex=set.indexOfFirst(field);
		if(((showFlag>0)&&(showFlag!=showNumber))||(setDex<0))
			return true;
		mob.tell(showNumber+". "+fieldDisplayStr+": '"+(set.get(setDex).second+"'."));
		if((showFlag!=showNumber)&&(showFlag>-999))
			return true;
		String newName=mob.session().prompt(L("Enter a new one\n\r:"),"");
		if(newName.trim().length()==0)
		{
			mob.tell(L("(no change)"));
			return false;
		}
		if((newName.equalsIgnoreCase("?"))&&(help!=null))
		{
			if((mob.session()==null)||(mob.session().isStopped()))
				return false;
			mob.tell(help);
			return genText(mob,set,choices,help,showNumber,showFlag,fieldDisplayStr,field);
		}
		if(newName.equalsIgnoreCase("null"))
			newName="";
		if((choices==null)||(choices.length==0))
		{
			set.get(setDex).second = newName;
			return true;
		}
		boolean found=false;
		for (final String choice : choices)
		{
			if(newName.equalsIgnoreCase(choice))
			{
				newName = choice;
				found = true;
				break;
			}
		}
		if(!found)
		{
			if((mob.session()==null)||(mob.session().isStopped()))
				return false;
			mob.tell(help);
			return genText(mob,set,choices,help,showNumber,showFlag,fieldDisplayStr,field);
		}
		set.get(setDex).second = newName;
		return true;
	}

	protected boolean genText(final MOB mob, final Map<String,String> map, final String[] choices, final String help, final int showNumber, final int showFlag, final String fieldDisplayStr, final String field)
	throws IOException
	{
		if(((showFlag>0)&&(showFlag!=showNumber))||(!map.containsKey(field)))
			return true;
		mob.tell(showNumber+". "+fieldDisplayStr+": '"+(map.get(field)+"'."));
		if((showFlag!=showNumber)&&(showFlag>-999))
			return true;
		String newName=mob.session().prompt(L("Enter a new one\n\r:"),"");
		if(newName.trim().length()==0)
		{
			mob.tell(L("(no change)"));
			return false;
		}
		if((newName.equalsIgnoreCase("?"))&&(help!=null))
		{
			if((mob.session()==null)||(mob.session().isStopped()))
				return false;
			mob.tell(help);
			return genText(mob,map,choices,help,showNumber,showFlag,fieldDisplayStr,field);
		}
		if(newName.equalsIgnoreCase("null"))
			newName="";
		if((choices==null)||(choices.length==0))
		{
			map.put(field,newName);
			return true;
		}
		boolean found=false;
		for (final String choice : choices)
		{
			if(newName.equalsIgnoreCase(choice))
			{
				newName = choice;
				found = true;
				break;
			}
		}
		if(!found)
		{
			if((mob.session()==null)||(mob.session().isStopped()))
				return false;
			mob.tell(help);
			return genText(mob,map,choices,help,showNumber,showFlag,fieldDisplayStr,field);
		}
		map.put(field,newName);
		return true;
	}

	protected boolean modifyComponent(final MOB mob, final AbilityComponent comp, int showFlag)
	throws IOException
	{
		PairList<String,String> decoded=CMLib.ableComponents().getAbilityComponentCoded(comp);
		if(mob.isMonster())
			return true;
		boolean ok=false;
		if((showFlag == -1) && (CMProps.getIntVar(CMProps.Int.EDITORTYPE)>0))
			showFlag=-999;
		else
		if(showFlag > 0)
			showFlag=1;
		final String choices="Your choices are: ";
		final String allComponents=CMParms.toListString(RawMaterial.Material.values())+","+CMParms.toListString(RawMaterial.CODES.NAMES());
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(!ok))
		{
			int showNumber=0;
			genText(mob,decoded,(new String[]{"&&","||","\"", "X"}),choices+" &&, ||, \", X",++showNumber,showFlag,"Conjunction (X Deletes) (?)","ANDOR");
			if(decoded.get(0).second.equalsIgnoreCase("X"))
				return false;
			if(!decoded.get(0).second.equalsIgnoreCase("\""))
			{
				final String oldT = (decoded.size()>2)?decoded.getSecond(1):"";
				genText(mob,decoded,(new String[]{"INVENTORY","HELD","WORN","ONGROUND","NEARBY","TRIGGER"}),
							choices+" INVENTORY, HELD, WORN, ONGROUND, NEARBY, TRIGGER",++showNumber,showFlag,"Component position (?)","DISPOSITION");
				final String newT = (decoded.size()>2)?decoded.getSecond(1):"";
				if((!oldT.equalsIgnoreCase(newT))
				&&(oldT.equalsIgnoreCase("TRIGGER")||newT.equalsIgnoreCase("TRIGGER")))
				{
					CMLib.ableComponents().setAbilityComponentCodedFromCodedPairs(decoded,comp);
					decoded=CMLib.ableComponents().getAbilityComponentCoded(comp);
				}
				if(newT.equalsIgnoreCase("TRIGGER"))
					genText(mob,decoded,null,null,++showNumber,showFlag,"Trigger Ritual","TRIGGER");
				else
				{
					genText(mob,decoded,(new String[]{"KEPT","CONSUMED"}),choices+" KEPT, CONSUMED",++showNumber,showFlag,"Component fate (?)","FATE");
					genText(mob,decoded,null,null,++showNumber,showFlag,"Amount of component","AMOUNT");
					genText(mob,decoded,null,allComponents,++showNumber,showFlag,"Type of component (?)","COMPONENTID");
					genText(mob,decoded,null,allComponents,++showNumber,showFlag,"Component Subtype","SUBTYPE");
				}
				genText(mob,decoded,null,CMLib.masking().maskHelp("\n","disallow"),++showNumber,showFlag,"Component applies-to mask (?)","MASK");
			}
			else
				genText(mob,decoded,null,null,++showNumber,showFlag,"Description","MASK");

			if (showFlag < -900)
			{
				ok = true;
				break;
			}
			if (showFlag > 0)
			{
				showFlag = -1;
				continue;
			}
			showFlag=CMath.s_int(mob.session().prompt(L("Edit which? "),""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
			}
		}
		CMLib.ableComponents().setAbilityComponentCodedFromCodedPairs(decoded,comp);
		return true;
	}

	protected void modifyComponents(final MOB mob, final List<AbilityComponent> codedDV, int showFlag) throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		if((showFlag == -1) && (CMProps.getIntVar(CMProps.Int.EDITORTYPE)>0))
			showFlag=-999;
		if(codedDV==null)
			return;
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(!ok))
		{
			int showNumber=0;
			for(int v=0;v<codedDV.size();v++)
			{
				if((mob.session()!=null)&&(!mob.session().isStopped()))
				{
					showNumber++;
					if((showFlag>0)&&(showFlag!=showNumber))
						continue;
					mob.tell(showNumber+": '"+CMLib.ableComponents().getAbilityComponentDesc(null,codedDV.get(v),v>0)+"'.");
					if((showFlag!=showNumber)&&(showFlag>-999))
						continue;
					if(!modifyComponent(mob,codedDV.get(v),showFlag))
					{
						codedDV.remove(v);
						v--;
					}
				}
			}
			while((mob.session()!=null)&&(!mob.session().isStopped()))
			{
				if((codedDV.size()==0)||(codedDV.get(codedDV.size()-1).getConnector()!=CompConnector.MESSAGE))
				{
					showNumber++;
					mob.tell(L("@x1. Add new component requirement.",""+showNumber));
					if((showFlag==showNumber)||(showFlag<=-999))
					{
						final AbilityComponent comp = CMLib.ableComponents().createBlankAbilityComponent("");
						final boolean success=modifyComponent(mob,comp,showFlag);
						if(!success)
						{
							// do nothing
						}
						else
						{
							codedDV.add(comp);
							if(showFlag<=-999)
								continue;
						}
					}
				}
				break;
			}

			if (showFlag < -900)
			{
				ok = true;
				break;
			}
			if (showFlag > 0)
			{
				showFlag = -1;
				continue;
			}
			showFlag=CMath.s_int(mob.session().prompt(L("Edit which? "),""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
			}
		}
	}

	@Override
	public void modifyComponents(final MOB mob, final String skillID, int showFlag) throws IOException
	{
		if(mob.isMonster())
			return;
		if((showFlag == -1) && (CMProps.getIntVar(CMProps.Int.EDITORTYPE)>0))
			showFlag=-999;
		final List<AbilityComponent> codedDV=CMLib.ableComponents().getAbilityComponents(skillID);
		modifyComponents(mob, codedDV, showFlag);
	}

	protected String modifyComponents(final MOB mob, final String oldVal, final int showNumber, final int showFlag, final String fieldDisp) throws IOException
	{
		if((mob==null)||(mob.session() == null))
			return oldVal;
		if((showFlag>0)&&(showFlag!=showNumber))
			return oldVal;
		final String showVal=oldVal;
		mob.tell(showNumber+". "+fieldDisp+": '"+CMStrings.limit(showVal,30)+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999))
			return oldVal;
		final Map<String,List<AbilityComponent>> m = new HashMap<String,List<AbilityComponent>>();
		final String ID="ID"+CMLib.dice().getRandomizer().nextDouble()+"!";
		final String err = CMLib.ableComponents().addAbilityComponent(ID+"="+oldVal, m);
		if(m.containsKey(ID))
		{
			final List<AbilityComponent> c = m.get(ID);
			this.modifyComponents(mob, c, showFlag);
			return CMLib.ableComponents().getAbilityComponentCodedString(c);
		}
		else
			mob.tell(err);
		return oldVal;
	}

	@Override
	public String modifyPlane(final MOB mob, final String planeName, final Map<String,String> planeSet, int showFlag) throws IOException
	{
		if(mob.isMonster())
			return null;
		boolean ok=false;
		if((showFlag == -1) && (CMProps.getIntVar(CMProps.Int.EDITORTYPE)>0))
			showFlag=-999;
		final PlanarAbility planeAble = (PlanarAbility)CMClass.getAbilityPrototype("StdPlanarAbility");
		final CModifiableStringMap modSet = new CModifiableStringMap(planeSet);
		final String[] bonusCharStats = new String[CharStats.CODES.BASECODES().length+1];
		bonusCharStats[0]="";
		int x=0;
		for(x=0;x<CharStats.CODES.BASECODES().length;x++)
			bonusCharStats[x+1] = CharStats.CODES.NAME(CharStats.CODES.BASECODES()[x]).toLowerCase();
		final String[] rawResourceNames = new String[RawMaterial.CODES.NAMES().length+1];
		rawResourceNames[0]="";
		for(x=0;x<RawMaterial.CODES.NAMES().length;x++)
			rawResourceNames[x+1] = RawMaterial.CODES.NAMES()[x].toLowerCase();
		final String[] otherPlaneNames = new String[planeAble.getAllPlaneKeys().size()+1];
		otherPlaneNames[0]="";
		for(x=0;x<planeAble.getAllPlaneKeys().size();x++)
			otherPlaneNames[x+1] = planeAble.getAllPlaneKeys().get(x).toLowerCase();
		final String[] otherRaceNames = new String[CMClass.numPrototypes(CMObjectType.RACE)+1];
		otherRaceNames[0]="";
		x=0;
		for(final Enumeration<Race> r=CMClass.races();r.hasMoreElements();)
			otherRaceNames[x+1] = r.nextElement().ID();
		final String[] factionNames = new String[CMLib.factions().numFactions()+1];
		factionNames[0]="*";
		x=0;
		for(final Enumeration<Faction> f=CMLib.factions().factions();f.hasMoreElements();)
			factionNames[++x] = f.nextElement().name().toLowerCase();
		final String[] specFlags = new String[PlanarAbility.PlanarSpecFlag.values().length];
		for(x=0;x<PlanarAbility.PlanarSpecFlag.values().length;x++)
			specFlags[x]=PlanarAbility.PlanarSpecFlag.values()[x].name();
		final List<String> ableBehavsV = new ArrayList<String>();
		final List<String> enableV = new ArrayList<String>();
		enableV.add("number");
		final List<String> flags = new ConvertingList<String,String>(Arrays.asList(Ability.FLAG_DESCS),Converter.toLowerCase);
		enableV.addAll(new ConvertingList<String,String>(Ability.DOMAIN.DESCS,Converter.toLowerCase));
		enableV.addAll(flags);
		enableV.addAll(new XVector<String>(
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
		final String[] enables = enableV.toArray(new String[enableV.size()]);
		final String[] behavs = new String[CMClass.numPrototypes(CMObjectType.BEHAVIOR)];
		for(final Enumeration<Ability> a=CMClass.abilities();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A.classificationCode()&Ability.ALL_DOMAINS)!=Ability.DOMAIN_ARCHON)
				ableBehavsV.add(A.ID());
		}
		x=0;
		for(final Enumeration<Behavior> b=CMClass.behaviors();b.hasMoreElements();)
		{
			ableBehavsV.add(b.nextElement().ID());
			behavs[x++]=ableBehavsV.get(ableBehavsV.size()-1);
		}
		final String[] lowerBehavs = Arrays.copyOf(behavs, behavs.length);
		for(x=0;x<lowerBehavs.length;x++)
			lowerBehavs[x]=lowerBehavs[x].toLowerCase();
		final String[] ableBehavs = ableBehavsV.toArray(new String[ableBehavsV.size()]);
		final List<String> factionsV = new ArrayList<String>();
		factionsV.add("*");
		factionsV.addAll(new XVector<String>(new ConvertingEnumeration<Faction,String>(CMLib.factions().factions(), new Converter<Faction,String>()
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
		final String[] factions=factionsV.toArray(new String[factionsV.size()]);
		final List<String> reqWeaponsV = new ArrayList<String>();
		reqWeaponsV.add("");
		reqWeaponsV.add("magical");
		reqWeaponsV.addAll(new ConvertingList<String,String>(Arrays.asList(Weapon.TYPE_DESCS),Converter.toLowerCase));
		reqWeaponsV.addAll(new ConvertingList<String,String>(Arrays.asList(Weapon.CLASS_DESCS),Converter.toLowerCase));
		final String[] reqWeapons=reqWeaponsV.toArray(new String[reqWeaponsV.size()]);
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(!ok))
		{
			final Session sess = mob.session();
			int showNumber=0;
			for(final PlanarVar var : PlanarVar.values())
			{
				switch(var)
				{
				case ABSORB:
					this.promptStatStr(mob, modSet, CMLib.help().getHelpText("Prop_AbsorbDamage",mob,true).toString(),
							++showNumber, showFlag, "MOB Absorptions", var.name(), true);
					break;
				case ADJSIZE:
				{
					++showNumber;
					if((showFlag>0)&&(showFlag!=showNumber))
						break;
					final String oldVal = modSet.getStat(var.name());
					mob.tell(showNumber+". MOB Adj Size(s): "+modSet.getStat(oldVal)+".");
					if((showFlag!=showNumber)&&(showFlag>-999))
						break;
					final String height = CMParms.getParmStr(oldVal,"HEIGHT","");
					String newHeight = sess.prompt("Height ("+height+"): ");
					if(newHeight.length()==0)
						newHeight=height;
					else
					if(!CMath.isNumber(newHeight))
						newHeight="";
					final String weight = CMParms.getParmStr(oldVal,"WEIGHT","");
					String newWeight = sess.prompt("Weight ("+weight+"): ");
					if(newWeight.length()==0)
						newWeight=weight;
					else
					if(!CMath.isNumber(newWeight))
						newWeight="";
					String str=(newHeight.length()>0)?("height="+newHeight):"";
					str += ((newWeight.length()>0) && (str.length()>0))?" ":"";
					str +=(newWeight.length()>0)?("weight="+newWeight):"";
					if(str.length()>0)
						modSet.setStat(var.name(), str);
					else
						modSet.remove(var.name());
					break;
				}
				case ADJSTAT:
					this.promptStatStr(mob, modSet, CMLib.help().getHelpText("Prop_StatAdjuster",mob,true).toString(),
							++showNumber, showFlag, "MOB Stat Adj", var.name(), true);
					break;
				case ADJUST:
					this.promptStatStr(mob, modSet, CMLib.help().getHelpText("Prop_Adjuster",mob,true).toString(),
							++showNumber, showFlag, "MOB Adjuster", var.name(), true);
					break;
				case AEFFECT:
					modSet.setStat(var.name(), promptDelimitedList(mob, modSet.getStat(var.name()), ++showNumber, showFlag,
							"Area Effects", "(Ability Arg)",' ',null, CMEVAL_INSTANCE, ableBehavs));
					break;
				case ALIGNMENT:
					this.promptStatStr(mob,modSet,null,++showNumber,showFlag,"Alignment",var.name(),true);
					if(modSet.containsKey(var.name()) && (modSet.get(var.name()).length()>0))
						modSet.put(var.name(), ""+CMath.s_int(modSet.get(var.name())));
					break;
				case AREABLURBS:
					modSet.setStat(var.name(), promptDelimitedList(mob, modSet.getStat(var.name()), ++showNumber, showFlag,
							"Area Blurbs", "=\"Value\" ",' ',null, null, null));
					break;
				case ATMOSPHERE:
					this.promptStatChoices(mob, modSet, CMParms.toListString(rawResourceNames), ++showNumber, showFlag, "Atmosphere", var.name(), rawResourceNames);
					break;
				case BEHAVAFFID:
					modSet.setStat(var.name(), promptDelimitedList(mob, modSet.getStat(var.name()), ++showNumber, showFlag,
							"MOB Behavior Conversion", "=NewBehaviorID",' ',null, CMEVAL_INSTANCE, lowerBehavs));
					break;
				case BEHAVE:
					modSet.setStat(var.name(), promptDelimitedList(mob, modSet.getStat(var.name()), ++showNumber, showFlag,
							"MOB Behaviors", "(Behave Parms)",' ',null, CMEVAL_INSTANCE, behavs));
					break;
				case BONUSDAMAGESTAT:
					this.promptStatChoices(mob, modSet, CMParms.toListString(bonusCharStats), ++showNumber, showFlag, "Bonus Damage Stat", var.name(), bonusCharStats);
					break;
				case CATEGORY:
					modSet.setStat(var.name(), promptCommaList(mob, modSet.getStat(var.name()), ++showNumber, showFlag, "Category(s)", null, null, null));
					break;
				case OPPOSED:
					modSet.setStat(var.name(), promptCommaList(mob, modSet.getStat(var.name()), ++showNumber, showFlag, "Opposed(s)",
							CMParms.toListString(planeAble.getAllPlaneKeys()), null, planeAble.getAllPlaneKeys().toArray(new String[0])));
					break;
				case DESCRIPTION:
					this.promptStatStr(mob,modSet,null,++showNumber,showFlag,"Description",var.name(),true);
					break;
				case ELITE:
					this.promptStatStr(mob,modSet,null,++showNumber,showFlag,"Elite Lvl",var.name(),true);
					if(modSet.containsKey(var.name()) && (modSet.get(var.name()).length()>0))
						modSet.put(var.name(), ""+CMath.s_int(modSet.get(var.name())));
					break;
				case ENABLE:
					modSet.setStat(var.name(), promptDelimitedList(mob, modSet.getStat(var.name()), ++showNumber, showFlag,
							"MOB Ability", "(Parms)",' ',null, CMEVAL_INSTANCE, enables));
					break;
				case FACTIONS:
					modSet.setStat(var.name(), promptDelimitedList(mob, modSet.getStat(var.name()), ++showNumber, showFlag,
							"MOB Factions", "(Numeric Value)",' ',null, CMEVAL_INSTANCE, factions));
					break;
				case FATIGUERATE:
					this.promptStatStr(mob,modSet,null,++showNumber,showFlag,"Fatigue Rate",var.name(),true);
					if(modSet.containsKey(var.name()) && (modSet.get(var.name()).length()>0))
						modSet.put(var.name(), ""+CMath.s_int(modSet.get(var.name())));
					break;
				case HOURS:
					this.promptStatStr(mob,modSet,null,++showNumber,showFlag,"Hours/Day",var.name(),true);
					if(modSet.containsKey(var.name()) && (modSet.get(var.name()).length()>0))
						modSet.put(var.name(), ""+CMath.s_int(modSet.get(var.name())));
					break;
				case ID:
					break;
				case LEVELADJ:
					this.promptStatStr(mob,modSet,
							"Number, of formula with @x1 = base areas median level, @x2 = specific mob/item level, @x2 = the plane traveling players level",
							++showNumber,showFlag,"Level Adjustments",var.name(),true);
					break;
				case LIKE:
					this.promptStatChoices(mob, modSet, CMParms.toListString(otherPlaneNames), ++showNumber, showFlag, "Like Plane", var.name(), otherPlaneNames);
					break;
				case MIXRACE:
					this.promptStatChoices(mob, modSet, CMParms.toListString(otherRaceNames), ++showNumber, showFlag, "Mix Race", var.name(), otherRaceNames);
					break;
				case MOBCOPY:
					this.promptStatStr(mob,modSet,null,++showNumber,showFlag,"MOB copies",var.name(),true);
					if(modSet.containsKey(var.name()) && (modSet.get(var.name()).length()>0))
						modSet.put(var.name(), ""+CMath.s_int(modSet.get(var.name())));
					break;
				case MOBRESIST:
					this.promptStatStr(mob, modSet, CMLib.help().getHelpText("Prop_Resistance",mob,true).toString(),
							++showNumber, showFlag, "MOB Resistances", var.name(), true);
					break;
				case PREFIX:
					modSet.setStat(var.name(), promptCommaList(mob, modSet.getStat(var.name()), ++showNumber, showFlag, "MOB Prefix(s)", null, null, null));
					break;
				case PROMOTIONS:
					modSet.setStat(var.name(), promptDelimitedList(mob, modSet.getStat(var.name()), ++showNumber, showFlag,
							"Promotions", "(Pct Chance)",',',null, null, null));
					break;
				case RECOVERRATE:
					this.promptStatStr(mob,modSet,null,++showNumber,showFlag,"Recover Rate",var.name(),true);
					if(modSet.containsKey(var.name()) && (modSet.get(var.name()).length()>0))
						modSet.put(var.name(), ""+CMath.s_int(modSet.get(var.name())));
					break;
				case REFFECT:
					modSet.setStat(var.name(), promptDelimitedList(mob, modSet.getStat(var.name()), ++showNumber, showFlag,
							"Room Effects", "(Ability Arg)",' ',null, CMEVAL_INSTANCE, ableBehavs));
					break;
				case REQWEAPONS:
					modSet.setStat(var.name(), promptDelimitedList(mob, modSet.getStat(var.name()), ++showNumber, showFlag,
							"Required Weapons", null,' ',null, CMEVAL_INSTANCE, reqWeapons));
					break;
				case ROOMADJS:
				{
					++showNumber;
					if((showFlag>0)&&(showFlag!=showNumber))
						break;
					mob.tell(showNumber+". Room Description Color: "+modSet.getStat(var.name())+".");
					if((showFlag!=showNumber)&&(showFlag>-999))
						break;
					final String varVal=modSet.getStat(var.name());
					final String defUp = varVal.startsWith("UP ")?"Y":"N";
					String rest = varVal;
					if(defUp.equals("Y"))
						rest=defUp.substring(3).trim();
					x=rest.indexOf(' ');
					String chanceStr="";
					if((x>0)&&(CMath.isInteger(rest.substring(0,x))))
					{
						chanceStr=rest.substring(0,x).trim();
						rest=rest.substring(x+1);
					}
					final boolean up = sess.confirm("Make uppercase ("+defUp+")? ", defUp);
					String newChance = sess.prompt("Pct chance ("+chanceStr+")?");
					if(newChance.trim().length()==0)
						newChance=chanceStr;
					else
					if(!CMath.isInteger(newChance))
						newChance="";
					final String adjList=promptCommaList(mob, rest, ++showNumber, showFlag, "Adjectives", null, null, null);
					if(adjList.trim().length()==0)
						modSet.remove(var.name());
					else
					{
						String finalVal = up?"UP ":"";
						finalVal += (newChance.trim().length()>0)?(" "+newChance):"";
						finalVal += " "+adjList;
						finalVal = finalVal.trim();
						modSet.setStat(var.name(), finalVal);
					}
					break;
				}
				case ROOMCOLOR:
				{
					++showNumber;
					if((showFlag>0)&&(showFlag!=showNumber))
						break;
					mob.session().rawPrintln(showNumber+". Room Title Color: "+modSet.getStat(var.name())+".");
					if((showFlag!=showNumber)&&(showFlag>-999))
						break;
					final String varVal=modSet.getStat(var.name());
					final String defUp = varVal.startsWith("UP ")?"Y":"N";
					String colorCode = varVal;
					if(defUp.equals("Y"))
						colorCode=defUp.substring(3).trim();
					final boolean up = sess.confirm("Make uppercase ("+defUp+")? ", defUp);
					String roomColorChar = sess.prompt("Color Code ("+colorCode+"): ");
					if(roomColorChar.trim().length()==0)
						roomColorChar=colorCode.trim();
					else
					if(!roomColorChar.startsWith("^"))
						roomColorChar="";
					if(roomColorChar.length()>0)
						modSet.setStat(var.name(), (up?"UP ":"")+roomColorChar);
					else
						modSet.remove(var.name());
					break;
				}
				case SETSTAT:
					this.promptStatStr(mob, modSet, CMLib.help().getHelpText("Prop_StatTrainer",mob,true).toString(),
							++showNumber, showFlag, "MOB Stat Trainer", var.name(), true);
					break;
				case SPECFLAGS:
					modSet.setStat(var.name(), CMStrings.replaceAll(this.promptCommaList(mob, modSet.getStat(var.name()), ++showNumber, showFlag, "Spec flag(s)",
									CMParms.toListString(specFlags), CMEVAL_INSTANCE, specFlags),","," ").trim());
					break;
				case TRANSITIONAL:
					promptStatBool(mob,modSet,++showNumber,showFlag,"Transitional",var.name());
					if(modSet.containsKey(var.name())
					&& CMath.s_bool(modSet.get(var.name())))
						modSet.put(var.name(), "true");
					else
						modSet.remove(var.name());
					break;
				case WEAPONMAXRANGE:
					this.promptStatStr(mob,modSet,null,++showNumber,showFlag,"Max Weap Range",var.name(),true);
					if(modSet.containsKey(var.name()) && (modSet.get(var.name()).length()>0))
						modSet.put(var.name(), ""+CMath.s_int(modSet.get(var.name())));
					break;
				}
				if(modSet.containsKey(var.name())
				&&(modSet.get(var.name()).trim().length()==0))
					modSet.remove(var.name());
			}

			if (showFlag < -900)
			{
				ok = true;
				break;
			}
			if (showFlag > 0)
			{
				showFlag = -1;
				continue;
			}
			showFlag=CMath.s_int(mob.session().prompt(L("Edit which? "),""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
			}
		}
		final StringBuilder str=new StringBuilder("");
		for(final String key : modSet.keySet())
		{
			if(modSet.containsKey(key))
			{
				String value=modSet.get(key).trim();
				str.append(key.toLowerCase()).append("=");
				if((!CMath.isNumber(value))||(value.indexOf(' ')>=0))
					value="\"" + CMStrings.replaceAll(value,"\"","\\\"")+"\"";
				str.append(value).append(' ');
			}
		}
		return str.toString().trim();
	}

	@Override
	public void modifyGenRace(final MOB mob, final Race me, int showFlag) throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		if((showFlag == -1) && (CMProps.getIntVar(CMProps.Int.EDITORTYPE)>0))
			showFlag=-999;
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(!ok))
		{
			int showNumber=0;
			promptStatStr(mob,me,++showNumber,showFlag,"Name","NAME");
			genCat(mob,me,++showNumber,showFlag);
			promptStatInt(mob,me,++showNumber,showFlag,"Base Weight","BWEIGHT");
			promptStatInt(mob,me,++showNumber,showFlag,"Weight Variance","VWEIGHT");
			promptStatInt(mob,me,++showNumber,showFlag,"Base Male Height","MHEIGHT");
			promptStatInt(mob,me,++showNumber,showFlag,"Base Female Height","FHEIGHT");
			promptStatInt(mob,me,++showNumber,showFlag,"Height Variance","VHEIGHT");
			genRaceAvailability(mob,me,++showNumber,showFlag);
			genDisableFlags(mob,me,++showNumber,showFlag);
			genBreathes(mob,me,++showNumber,showFlag);
			promptStatStr(mob,me,++showNumber,showFlag,"Leaving text","LEAVE");
			promptStatStr(mob,me,++showNumber,showFlag,"Arriving text","ARRIVE");
			genRaceBuddy(mob,me,++showNumber,showFlag,"Health Race","HEALTHRACE");
			genRaceBuddy(mob,me,++showNumber,showFlag,"Event Race","EVENTRACE");
			genBodyParts(mob,me,++showNumber,showFlag);
			genRaceWearFlags(mob,me,++showNumber,showFlag);
			genAgingChart(mob,me,++showNumber,showFlag);
			promptStatBool(mob,me,++showNumber,showFlag,"Never create corpse","BODYKILL");
			promptStatBool(mob,me,++showNumber,showFlag,"Rideable Player","CANRIDE");
			promptStatInt(mob, me, ++showNumber, showFlag, "XP Adjustment %", "XPADJ");
			genPStats(mob,me,++showNumber,showFlag);
			genAStats(mob,me,"ASTATS","CharStat Adjustments",++showNumber,showFlag);
			genAStats(mob,me,"CSTATS","CharStat Settings",++showNumber,showFlag);
			genAState(mob,me,"ASTATE","CharState Adjustments",++showNumber,showFlag);
			genAState(mob,me,"STARTASTATE","New Player CharState Adj.",++showNumber,showFlag);
			genResources(mob,me,++showNumber,showFlag);
			genOutfit(mob,me,++showNumber,showFlag);
			genWeapon(mob,me,++showNumber,showFlag);
			genRaceBuddy(mob,me,++showNumber,showFlag,"Weapons Race","WEAPONRACE");
			genDynamicAbilities(mob,me,"Racial","char",++showNumber,showFlag);
			genCulturalAbilities(mob,me,++showNumber,showFlag);
			genDynamicEffects(mob,me,"Racial","char",++showNumber,showFlag);
			genDynamicImmunitiess(mob,me,"Racial",++showNumber,showFlag);

			if (showFlag < -900)
			{
				ok = true;
				break;
			}
			if (showFlag > 0)
			{
				showFlag = -1;
				continue;
			}
			showFlag=CMath.s_int(mob.session().prompt(L("Edit which? "),""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
			}
		}
	}

	protected void modifyGenItem(final MOB mob, final Item me, int showFlag)
		throws IOException
	{
		boolean ok=false;
		if((showFlag == -1) && (CMProps.getIntVar(CMProps.Int.EDITORTYPE)>0))
			showFlag=-999;
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(!ok))
		{
			if(mob.isMonster())
				return;
			int showNumber=0;
			if(showFlag<0)
				mob.tell(L("*. Class: @x1",me.ID()));
			genName(mob,me,++showNumber,showFlag);
			genDisplayText(mob,me,++showNumber,showFlag);
			genDescription(mob,me,++showNumber,showFlag);
			genLevel(mob,me,++showNumber,showFlag);
			genSecretIdentity(mob,me,++showNumber,showFlag);
			genMaterialCode(mob,me,++showNumber,showFlag);
			if(me instanceof RawMaterial)
				genMaterialSubType(mob,(RawMaterial)me,++showNumber,showFlag);
			if(me instanceof Book)
			{
				((Book)me).setMaxPages(prompt(mob, ((Book)me).getMaxPages(), ++showNumber, showFlag, "Max Pages"));
				((Book)me).setMaxCharsPerPage(prompt(mob, ((Book)me).getMaxCharsPerPage(), ++showNumber, showFlag, "Chars/Page"));
			}
			if(me instanceof ClanItem)
				genClanItem(mob,(ClanItem)me,++showNumber,showFlag);
			if(me instanceof Technical)
			{
				final Technical E=(Technical)me;
				E.setManufacturerName(prompt(mob, E.getManufacturerName(), ++showNumber, showFlag, "Manufacturer"));
			}
			if(me instanceof Software)
			{
				final Software E=(Software)me;
				E.setInternalName(prompt(mob, E.getInternalName(), ++showNumber, showFlag, "Internal Name",true));
				E.setParentMenu(prompt(mob, E.getParentMenu(), ++showNumber, showFlag, "Parent Menu",true));
			}
			if(me instanceof Electronics)
			{
				final Electronics E=(Electronics)me;
				E.setPowerCapacity(prompt(mob, E.powerCapacity(), ++showNumber, showFlag, "Pow Capacity"));
				E.setPowerRemaining(prompt(mob, E.powerRemaining(), ++showNumber, showFlag, "Pow Remaining"));
				E.activate(prompt(mob, E.activated(), ++showNumber, showFlag, "Activated"));
			}
			if((me instanceof ElecPanel)&&(!(me instanceof Computer)))
				genPanelType(mob,(ElecPanel)me,++showNumber,showFlag);
			if(me instanceof FalseLimb)
				((FalseLimb)me).setRaceID(prompt(mob,((FalseLimb)me).getRaceID(),++showNumber,showFlag,"Race ID"));
			if(me instanceof TechComponent)
			{
				final TechComponent E=(TechComponent)me;
				E.setInstalledFactor((float)prompt(mob, E.getInstalledFactor(), ++showNumber, showFlag, L("Installed Factor")));
				E.setRechargeRate((float)prompt(mob, E.getRechargeRate(), ++showNumber, showFlag, L("Pwr Recharge Rate")));
			}
			if(me instanceof ShipEngine)
			{
				final ShipEngine E=(ShipEngine)me;
				E.setMinThrust(prompt(mob, E.getMinThrust(), ++showNumber, showFlag, "Min thrust"));
				E.setMaxThrust(prompt(mob, E.getMaxThrust(), ++showNumber, showFlag, "Max thrust"));
				E.setReactionEngine(prompt(mob, E.isReactionEngine(), ++showNumber, showFlag, "Reaction engine"));
				E.setSpecificImpulse(prompt(mob, E.getSpecificImpulse(), ++showNumber, showFlag, "Fuel Spec Impulse"));
				E.setFuelEfficiency(prompt(mob, E.getFuelEfficiency()*100.0, ++showNumber, showFlag, "Fuel Effic. %")/100.0);
				E.setAvailPorts(CMParms.parseEnumList(ShipDirectional.ShipDir.class,prompt(mob, CMParms.toListString(E.getAvailPorts()), ++showNumber, showFlag, "Avail. ports").toUpperCase(),',').toArray(new ShipDirectional.ShipDir[0]));
			}
			if(me instanceof ShipDirectional)
			{
				final ShipDirectional E=(ShipDirectional)me;
				E.setPermittedNumDirections(prompt(mob, E.getPermittedNumDirections(), ++showNumber, showFlag, "Max Ports"));
				E.setPermittedDirections(CMParms.parseEnumList(ShipDirectional.ShipDir.class,prompt(mob, CMParms.toListString(E.getPermittedDirections()), ++showNumber, showFlag, "Avail. ports").toUpperCase(),',').toArray(new ShipDirectional.ShipDir[0]));
			}
			if(me instanceof ShipWarComponent)
			{
				final ShipWarComponent E=(ShipWarComponent)me;
				genMessageTypes(mob, E, ++showNumber, showFlag);
				genDamage(mob,me,++showNumber,showFlag);
			}
			if(me instanceof PackagedItems)
				((PackagedItems)me).setNumberOfItemsInPackage(prompt(mob,((PackagedItems)me).numberOfItemsInPackage(),++showNumber,showFlag,"Number of items in the package"));
			if(me instanceof PrivateProperty)
			{
				promptStatStr(mob,me,null,++showNumber,showFlag,"Owner","OWNER",true);
				promptStatInt(mob,me,null,++showNumber,showFlag,"Price","PRICE");
			}
			genGettable(mob,me,++showNumber,showFlag);
			genIsReadable(mob,me,++showNumber,showFlag);
			genReadableTextMisc(mob,me,++showNumber,showFlag);
			if(me instanceof LiquidHolder)
			{
				genDrinkType(mob,(LiquidHolder)me,++showNumber,showFlag);
				genDrinkHeld(mob,(LiquidHolder)me,++showNumber,showFlag);
			}
			if(me instanceof RecipesBook)
				genRecipe(mob,(RecipesBook)me,++showNumber,showFlag);
			if(me instanceof Light)
				genBurnout(mob,(Light)me,++showNumber,showFlag);
			genRejuv(mob,me,++showNumber,showFlag);
			if(me instanceof Coins)
				genCoinStuff(mob,(Coins)me,++showNumber,showFlag);
			else
			if((me instanceof NavigableItem)&&(!(me instanceof SpaceObject)))
				genAbility(mob,me,++showNumber,showFlag,L("Moves per Tick"));
			else
				genAbility(mob,me,++showNumber,showFlag);
			genUses(mob,me,++showNumber,showFlag);
			if(me instanceof Wand)
			{
				genMaxCharges(mob,(Wand)me,++showNumber,showFlag);
				promptStatChoices(mob,me,null,++showNumber,showFlag,"Enchant Type","ENCHTYPE", CMParms.toStringArraySingle(Wand.WandUsage.WAND_OPTIONS, 1));
			}
			genValue(mob,me,++showNumber,showFlag);
			genWeight(mob,me,++showNumber,showFlag);
			genDisposition(mob,me.basePhyStats(),++showNumber,showFlag);
			if(me instanceof SpaceObject)
			{
				final SpaceObject spaceArea=(SpaceObject)me;
				genSpaceStuff(mob,spaceArea,++showNumber,showFlag);
				if(me instanceof SpaceObject.SpaceGateway)
					genSpaceGate(mob,(SpaceObject.SpaceGateway)me,++showNumber,showFlag);
				if(me instanceof Weapon)
					genSpeed(mob,me,++showNumber,showFlag);
			}
			genBehaviors(mob,me,++showNumber,showFlag);
			genAffects(mob,me,++showNumber,showFlag);
			if(me instanceof LandTitle)
				genTitleRoom(mob,(LandTitle)me,++showNumber,showFlag);
			genImage(mob,me,++showNumber,showFlag);
			for(int x=me.getSaveStatIndex();x<me.getStatCodes().length;x++)
				me.setStat(me.getStatCodes()[x],prompt(mob,me.getStat(me.getStatCodes()[x]),++showNumber,showFlag,CMStrings.capitalizeAndLower(me.getStatCodes()[x])));
			if(me.numScripts()>0)
				genScripts(mob,me,++showNumber,showFlag);

			if (showFlag < -900)
			{
				ok = true;
				break;
			}
			if (showFlag > 0)
			{
				showFlag = -1;
				continue;
			}
			showFlag=CMath.s_int(mob.session().prompt(L("Edit which? "),""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
			}
		}
		if(showFlag != -950)
		{
			me.recoverPhyStats();
			if(me.text().length()>=maxLength)
				mob.tell(L("\n\rThe data entered exceeds the string limit of @x1 characters.",""+maxLength));
		}
	}

	protected void modifyGenFood(final MOB mob, final Food me, int showFlag)
		throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		if((showFlag == -1) && (CMProps.getIntVar(CMProps.Int.EDITORTYPE)>0))
			showFlag=-999;
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(!ok))
		{
			int showNumber=0;
			if(showFlag<0)
				mob.tell(L("*. Class: @x1",me.ID()));
			genName(mob,me,++showNumber,showFlag);
			genDisplayText(mob,me,++showNumber,showFlag);
			genDescription(mob,me,++showNumber,showFlag);
			genSecretIdentity(mob,me,++showNumber,showFlag);
			genLevel(mob,me,++showNumber,showFlag);
			genValue(mob,me,++showNumber,showFlag);
			genRejuv(mob,me,++showNumber,showFlag);
			genWeight(mob,me,++showNumber,showFlag);
			genMaterialCode(mob,me,++showNumber,showFlag);
			if(me instanceof RawMaterial)
				genMaterialSubType(mob,(RawMaterial)me,++showNumber,showFlag);
			genNourishment(mob,me,++showNumber,showFlag);
			genBiteSize(mob,me,++showNumber,showFlag);
			genDisposition(mob,me.basePhyStats(),++showNumber,showFlag);
			genGettable(mob,me,++showNumber,showFlag);
			genIsReadable(mob,me,++showNumber,showFlag);
			genReadableTextMisc(mob,me,++showNumber,showFlag);
			if(me instanceof Light)
				genBurnout(mob,(Light)me,++showNumber,showFlag);
			genBehaviors(mob,me,++showNumber,showFlag);
			genAffects(mob,me,++showNumber,showFlag);
			genImage(mob,me,++showNumber,showFlag);
			for(int x=me.getSaveStatIndex();x<me.getStatCodes().length;x++)
				me.setStat(me.getStatCodes()[x],prompt(mob,me.getStat(me.getStatCodes()[x]),++showNumber,showFlag,CMStrings.capitalizeAndLower(me.getStatCodes()[x])));
			if(me.numScripts()>0)
				genScripts(mob,me,++showNumber,showFlag);

			if (showFlag < -900)
			{
				ok = true;
				break;
			}
			if (showFlag > 0)
			{
				showFlag = -1;
				continue;
			}
			showFlag=CMath.s_int(mob.session().prompt(L("Edit which? "),""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
			}
		}
		if(showFlag != -950)
		{
			me.recoverPhyStats();
			if(me.text().length()>=maxLength)
				mob.tell(L("\n\rThe data entered exceeds the string limit of @x1 characters.",""+maxLength));
		}
	}

	protected void genScripts(final MOB mob, final PhysicalAgent E, final int showNumber, final int showFlag)
	throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		String behave="NO";
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(behave.length()>0))
		{
			String behaviorstr="";
			int b=1;
			for(final Enumeration<ScriptingEngine> e=E.scripts();e.hasMoreElements();b++)
			{
				final ScriptingEngine SE=e.nextElement();
				if(SE!=null)
				if(SE.defaultQuestName().length()>0)
					behaviorstr+=b+":"+SE.defaultQuestName()+", ";
				else
				if((showFlag==showNumber)||(showFlag<=-999))
					behaviorstr+=b+":"+CMStrings.ellipse(SE.getScript(),200)+", ";
				else
					behaviorstr+=b+":"+CMStrings.ellipse(SE.getScript(),40)+", ";
			}
			if(behaviorstr.length()>0)
				behaviorstr=behaviorstr.substring(0,behaviorstr.length()-2);
			mob.tell(L("@x1. Scripts: '@x2'.",""+showNumber,behaviorstr));
			if((showFlag!=showNumber)&&(showFlag>-999))
				return;
			behave=mob.session().prompt(L("Enter a script number to remove\n\r:"),"");
			if(behave.length()>0)
			{
				final String tattoo=behave;
				if((tattoo.length()>0)
				&&(CMath.isInteger(tattoo))
				&&(CMath.s_int(tattoo)>0)
				&&(CMath.s_int(tattoo)<=E.numScripts()))
				{
					final int x=CMath.s_int(tattoo);
					mob.tell(L("Script #@x1 removed.",""+x));
					E.delScript(E.fetchScript(x-1));
				}
			}
			else
				mob.tell(L("(no change)"));
		}
	}

	protected void modifyGenDrink(final MOB mob, final Drink me, int showFlag)
		throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		if((showFlag == -1) && (CMProps.getIntVar(CMProps.Int.EDITORTYPE)>0))
			showFlag=-999;
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(!ok))
		{
			int showNumber=0;
			if(showFlag<0)
				mob.tell(L("*. Class: @x1",me.ID()));
			genName(mob,me,++showNumber,showFlag);
			genDisplayText(mob,me,++showNumber,showFlag);
			genDescription(mob,me,++showNumber,showFlag);
			genSecretIdentity(mob,(Item)me,++showNumber,showFlag);
			genValue(mob,(Item)me,++showNumber,showFlag);
			if(me instanceof Physical)
			{
				genLevel(mob,(Physical)me,++showNumber,showFlag);
				genWeight(mob,(Physical)me,++showNumber,showFlag);
				genRejuv(mob,(Physical)me,++showNumber,showFlag);
			}
			genThirstQuenched(mob,me,++showNumber,showFlag);
			genMaterialCode(mob,(Item)me,++showNumber,showFlag);
			if(me instanceof RawMaterial)
				genMaterialSubType(mob,(RawMaterial)me,++showNumber,showFlag);
			genDrinkHeld(mob,me,++showNumber,showFlag);
			genGettable(mob,(Item)me,++showNumber,showFlag);
			genIsReadable(mob,(Item)me,++showNumber,showFlag);
			genReadableTextMisc(mob,(Item)me,++showNumber,showFlag);
			if(me instanceof Light)
				genBurnout(mob,(Light)me,++showNumber,showFlag);
			if(me instanceof PhysicalAgent)
				genBehaviors(mob,(PhysicalAgent)me,++showNumber,showFlag);
			if(me instanceof Physical)
			{
				genAffects(mob,(Physical)me,++showNumber,showFlag);
				genDisposition(mob,((Physical)me).basePhyStats(),++showNumber,showFlag);
			}
			if(me instanceof Container)
			{
				genContainerTypes(mob,(Container)me,++showNumber,showFlag);
				genCapacity(mob,(Container)me,++showNumber,showFlag);
				genDoorsNLocks(mob,(Container)me,L("lid"),++showNumber,showFlag);
			}
			if(me instanceof Perfume)
				((Perfume)me).setSmellList(prompt(mob,((Perfume)me).getSmellList(),++showNumber,showFlag,"Smells list (; delimited)"));
			genImage(mob,me,++showNumber,showFlag);
			for(int x=me.getSaveStatIndex();x<me.getStatCodes().length;x++)
				me.setStat(me.getStatCodes()[x],prompt(mob,me.getStat(me.getStatCodes()[x]),++showNumber,showFlag,CMStrings.capitalizeAndLower(me.getStatCodes()[x])));
			if((me instanceof PhysicalAgent) && (((PhysicalAgent)me).numScripts()>0))
				genScripts(mob,(PhysicalAgent)me,++showNumber,showFlag);

			if (showFlag < -900)
			{
				ok = true;
				break;
			}
			if (showFlag > 0)
			{
				showFlag = -1;
				continue;
			}
			showFlag=CMath.s_int(mob.session().prompt(L("Edit which? "),""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
			}
		}
		if(showFlag != -950)
		{
			if(me instanceof Physical)
				((Physical)me).recoverPhyStats();
			if(me.text().length()>=maxLength)
				mob.tell(L("\n\rThe data entered exceeds the string limit of @x1 characters.",""+maxLength));
		}
	}

	protected void modifyGenWallpaper(final MOB mob, final Item me, int showFlag)
		throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		if((showFlag == -1) && (CMProps.getIntVar(CMProps.Int.EDITORTYPE)>0))
			showFlag=-999;
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(!ok))
		{
			int showNumber=0;
			if(showFlag<0)
				mob.tell(L("*. Class: @x1",me.ID()));
			genName(mob,me,++showNumber,showFlag);
			genDescription(mob,me,++showNumber,showFlag);
			genIsReadable(mob,me,++showNumber,showFlag);
			genReadableTextMisc(mob,me,++showNumber,showFlag);
			if(me instanceof Light)
				genBurnout(mob,(Light)me,++showNumber,showFlag);
			for(int x=me.getSaveStatIndex();x<me.getStatCodes().length;x++)
				me.setStat(me.getStatCodes()[x],prompt(mob,me.getStat(me.getStatCodes()[x]),++showNumber,showFlag,CMStrings.capitalizeAndLower(me.getStatCodes()[x])));

			if (showFlag < -900)
			{
				ok = true;
				break;
			}
			if (showFlag > 0)
			{
				showFlag = -1;
				continue;
			}
			showFlag=CMath.s_int(mob.session().prompt(L("Edit which? "),""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
			}
		}
		if(showFlag != -950)
		{
			me.recoverPhyStats();
			if(me.text().length()>=maxLength)
				mob.tell(L("\n\rThe data entered exceeds the string limit of @x1 characters.",""+maxLength));
		}
	}

	protected void modifyGenMap(final MOB mob, final com.planet_ink.coffee_mud.Items.interfaces.RoomMap me, int showFlag)
		throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		if((showFlag == -1) && (CMProps.getIntVar(CMProps.Int.EDITORTYPE)>0))
			showFlag=-999;
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(!ok))
		{
			int showNumber=0;
			if(showFlag<0)
				mob.tell(L("*. Class: @x1",me.ID()));
			genName(mob,me,++showNumber,showFlag);
			genDisplayText(mob,me,++showNumber,showFlag);
			genDescription(mob,me,++showNumber,showFlag);
			genLevel(mob,me,++showNumber,showFlag);
			genSecretIdentity(mob,me,++showNumber,showFlag);
			genGettable(mob,me,++showNumber,showFlag);
			genIsReadable(mob,me,++showNumber,showFlag);
			genReadableTextMisc(mob,me,++showNumber,showFlag);
			genValue(mob,me,++showNumber,showFlag);
			genWeight(mob,me,++showNumber,showFlag);
			genRejuv(mob,me,++showNumber,showFlag);
			genMaterialCode(mob,me,++showNumber,showFlag);
			genDisposition(mob,me.basePhyStats(),++showNumber,showFlag);
			genBehaviors(mob,me,++showNumber,showFlag);
			genAffects(mob,me,++showNumber,showFlag);
			genImage(mob,me,++showNumber,showFlag);
			for(int x=me.getSaveStatIndex();x<me.getStatCodes().length;x++)
				me.setStat(me.getStatCodes()[x],prompt(mob,me.getStat(me.getStatCodes()[x]),++showNumber,showFlag,CMStrings.capitalizeAndLower(me.getStatCodes()[x])));
			if(me.numScripts()>0)
				genScripts(mob,me,++showNumber,showFlag);

			if (showFlag < -900)
			{
				ok = true;
				break;
			}
			if (showFlag > 0)
			{
				showFlag = -1;
				continue;
			}
			showFlag=CMath.s_int(mob.session().prompt(L("Edit which? "),""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
			}
		}
		if(showFlag != -950)
		{
			me.recoverPhyStats();
			if(me.text().length()>=maxLength)
				mob.tell(L("\n\rThe data entered exceeds the string limit of @x1 characters.",""+maxLength));
		}
	}

	protected void modifyGenContainer(final MOB mob, final Container me, int showFlag)
		throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		if((showFlag == -1) && (CMProps.getIntVar(CMProps.Int.EDITORTYPE)>0))
			showFlag=-999;
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(!ok))
		{
			int showNumber=0;
			if(showFlag<0)
				mob.tell(L("*. Class: @x1",me.ID()));
			genName(mob,me,++showNumber,showFlag);
			genDisplayText(mob,me,++showNumber,showFlag);
			genDescription(mob,me,++showNumber,showFlag);
			genLevel(mob,me,++showNumber,showFlag);
			genRejuv(mob,me,++showNumber,showFlag);
			genCapacity(mob,me,++showNumber,showFlag);
			if(me instanceof Technical)
			{
				final Technical E=(Technical)me;
				E.setManufacturerName(prompt(mob, E.getManufacturerName(), ++showNumber, showFlag, "Manufacturer"));
			}
			if(me instanceof Electronics)
			{
				final Electronics E=(Electronics)me;
				E.setPowerCapacity(prompt(mob, E.powerCapacity(), ++showNumber, showFlag, "Pow Capacity"));
				E.setPowerRemaining(prompt(mob, E.powerRemaining(), ++showNumber, showFlag, "Pow Remaining"));
				E.activate(prompt(mob, E.activated(), ++showNumber, showFlag, "Activated"));
			}
			if((me instanceof ElecPanel)&&(!(me instanceof Computer)))
				genPanelType(mob,(ElecPanel)me,++showNumber,showFlag);
			if(me instanceof PowerGenerator)
			{
				final PowerGenerator E=(PowerGenerator)me;
				E.setGeneratedAmountPerTick(prompt(mob, E.getGeneratedAmountPerTick(), ++showNumber, showFlag, "Gen Amt/Tick"));
			}
			if(me instanceof FuelConsumer)
			{
				final FuelConsumer E=(FuelConsumer)me;
				genConsumedMaterials(mob, E, ++showNumber, showFlag);
			}
			if(me instanceof ShipEngine)
			{
				final ShipEngine E=(ShipEngine)me;
				E.setMinThrust(prompt(mob, E.getMinThrust(), ++showNumber, showFlag, "Min thrust"));
				E.setMaxThrust(prompt(mob, E.getMaxThrust(), ++showNumber, showFlag, "Max thrust"));
				E.setReactionEngine(prompt(mob, E.isReactionEngine(), ++showNumber, showFlag, "Reaction based"));
				E.setSpecificImpulse(prompt(mob, E.getSpecificImpulse(), ++showNumber, showFlag, "Fuel Spec Impulse"));
				E.setFuelEfficiency(prompt(mob, E.getFuelEfficiency()*100.0, ++showNumber, showFlag, "Fuel Effic. %")/100.0);
				E.setAvailPorts(CMParms.parseEnumList(ShipDirectional.ShipDir.class,prompt(mob, CMParms.toListString(E.getAvailPorts()), ++showNumber, showFlag, "Avail. ports").toUpperCase(),',').toArray(new ShipDirectional.ShipDir[0]));
			}
			if(me instanceof TechComponent)
			{
				final TechComponent E=(TechComponent)me;
				E.setInstalledFactor((float)prompt(mob, E.getInstalledFactor(), ++showNumber, showFlag, "Installed Factor"));
				E.setRechargeRate((float)prompt(mob, E.getRechargeRate(), ++showNumber, showFlag, L("Pwr Recharge Rate")));
			}
			if(!(me instanceof Electronics))
			{
				genContainerTypes(mob,me,++showNumber,showFlag);
			}
			if(me instanceof PrivateProperty)
			{
				promptStatStr(mob,me,null,++showNumber,showFlag,"Owner","OWNER",true);
				promptStatInt(mob,me,null,++showNumber,showFlag,"Price","PRICE");
			}
			genDoorsNLocks(mob,me,L("lid"),++showNumber,showFlag);
			if(me.hasADoor() && me.hasALock() && !CMLib.flags().isReadable(me))
				me.setKeyName(prompt(mob,me.keyName(),++showNumber,showFlag,"Key Code"));
			genMaterialCode(mob,me,++showNumber,showFlag);
			genSecretIdentity(mob,me,++showNumber,showFlag);
			genValue(mob,me,++showNumber,showFlag);
			genUses(mob,me,++showNumber,showFlag);
			genWeight(mob,me,++showNumber,showFlag);
			genDisposition(mob,me.basePhyStats(),++showNumber,showFlag);
			if(me instanceof DeadBody)
				genCorpseData(mob,(DeadBody)me,++showNumber,showFlag);
			if(me instanceof ClanItem)
				genClanItem(mob,(ClanItem)me,++showNumber,showFlag);
			genGettable(mob,me,++showNumber,showFlag);
			genIsReadable(mob,me,++showNumber,showFlag);
			genReadableTextMisc(mob,me,++showNumber,showFlag);
			if(me instanceof Light)
				genBurnout(mob,(Light)me,++showNumber,showFlag);
			genBehaviors(mob,me,++showNumber,showFlag);
			genAffects(mob,me,++showNumber,showFlag);
			if(me instanceof Rideable)
			{
				genRideableType(mob,(Rideable)me,++showNumber,showFlag);
				genRideableRideCapacity(mob,(Rideable)me,++showNumber,showFlag);
				genMountText(mob,(Rideable)me,++showNumber,showFlag);
				if(!(me instanceof Exit)) // doesn't make sense for portals
					genMountRideMountText(mob,(Rideable)me,++showNumber,showFlag);
			}
			if(me instanceof Exit)
			{
				genDoorName(mob,(Exit)me,++showNumber,showFlag);
				genClosedText(mob,(Exit)me,++showNumber,showFlag);
			}
			if((me instanceof NavigableItem)&&(!(me instanceof SpaceObject)))
				genAbility(mob,me,++showNumber,showFlag,L("Moves per Tick"));
			//if(me instanceof PrivateProperty)
			//	me.setStat("OWNER",prompt(mob,((PrivateProperty)me).getOwnerName(),++showNumber,showFlag,CMStrings.capitalizeAndLower("Property Owner")));
			genImage(mob,me,++showNumber,showFlag);
			for(int x=me.getSaveStatIndex();x<me.getStatCodes().length;x++)
				me.setStat(me.getStatCodes()[x],prompt(mob,me.getStat(me.getStatCodes()[x]),++showNumber,showFlag,CMStrings.capitalizeAndLower(me.getStatCodes()[x])));
			if(me.numScripts()>0)
				genScripts(mob,me,++showNumber,showFlag);

			if (showFlag < -900)
			{
				ok = true;
				break;
			}
			if (showFlag > 0)
			{
				showFlag = -1;
				continue;
			}
			showFlag=CMath.s_int(mob.session().prompt(L("Edit which? "),""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
			}
		}
		if(showFlag != -950)
		{
			me.recoverPhyStats();
			if(me.text().length()>=maxLength)
				mob.tell(L("\n\rThe data entered exceeds the string limit of @x1 characters.",""+maxLength));
		}
	}

	protected void modifyGenWeapon(final MOB mob, final Weapon me, int showFlag)
		throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		if((showFlag == -1) && (CMProps.getIntVar(CMProps.Int.EDITORTYPE)>0))
			showFlag=-999;
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(!ok))
		{
			int showNumber=0;
			if(showFlag<0)
				mob.tell(L("*. Class: @x1",me.ID()));
			genName(mob,me,++showNumber,showFlag);
			genDisplayText(mob,me,++showNumber,showFlag);
			genDescription(mob,me,++showNumber,showFlag);
			final int oldLevel = me.basePhyStats().level();
			genLevel(mob,me,++showNumber,showFlag);
			if(me.basePhyStats().level() != oldLevel)
				CMLib.itemBuilder().balanceItemByLevel(me);
			genAttack(mob,me,++showNumber,showFlag);
			genDamage(mob,me,++showNumber,showFlag);
			genMaterialCode(mob,me,++showNumber,showFlag);
			genWeaponType(mob,me,++showNumber,showFlag);
			genWeaponClassification(mob,me,++showNumber,showFlag);
			genWeaponRanges(mob,me,++showNumber,showFlag);
			if(me instanceof Rideable)
			{
				genRideableType(mob,(Rideable)me,++showNumber,showFlag);
				genRideableRideCapacity(mob,(Rideable)me,++showNumber,showFlag);
				genMountText(mob,(Rideable)me,++showNumber,showFlag);
				if(!(me instanceof Exit)) // doesn't make sense for portals
					genMountRideMountText(mob,(Rideable)me,++showNumber,showFlag);
			}
			if(me instanceof Wand)
			{
				genIsReadable(mob,me,++showNumber,showFlag);
				genReadableTextMisc(mob,me,++showNumber,showFlag);
				genUses(mob,me,++showNumber,showFlag);
				genMaxCharges(mob,(Wand)me,++showNumber,showFlag);
				promptStatChoices(mob,me,null,++showNumber,showFlag,"Enchant Type","ENCHTYPE", CMParms.toStringArraySingle(Wand.WandUsage.WAND_OPTIONS, 1));
				if(me instanceof Light)
					genBurnout(mob,(Light)me,++showNumber,showFlag);
			}
			else
			if(me instanceof AmmunitionWeapon)
				genWeaponAmmo(mob,me,++showNumber,showFlag);
			if(me instanceof Technical)
			{
				final Technical E=(Technical)me;
				E.setManufacturerName(prompt(mob, E.getManufacturerName(), ++showNumber, showFlag, "Manufacturer"));
			}
			if(me instanceof Electronics)
			{
				final Electronics E=(Electronics)me;
				E.setPowerCapacity(prompt(mob, E.powerCapacity(), ++showNumber, showFlag, "Pow Capacity"));
				E.setPowerRemaining(prompt(mob, E.powerRemaining(), ++showNumber, showFlag, "Pow Remaining"));
				E.activate(prompt(mob, E.activated(), ++showNumber, showFlag, "Activated"));
			}
			if(me instanceof SpaceObject)
			{
				genSpaceStuff(mob,(SpaceObject)me,++showNumber,showFlag);
				if(me instanceof SpaceObject.SpaceGateway)
					genSpaceGate(mob,(SpaceObject.SpaceGateway)me,++showNumber,showFlag);
				genSpeed(mob,me,++showNumber,showFlag);
			}
			genRejuv(mob,me,++showNumber,showFlag);
			if(((!(me instanceof AmmunitionWeapon)) || (!((AmmunitionWeapon)me).requiresAmmunition()))
			&&(!(me instanceof Wand)))
				genCondition(mob,me,++showNumber,showFlag);
			genAbility(mob,me,++showNumber,showFlag);
			genSecretIdentity(mob,me,++showNumber,showFlag);
			if(me instanceof ClanItem)
				genClanItem(mob,(ClanItem)me,++showNumber,showFlag);
			genGettable(mob,me,++showNumber,showFlag);
			genValue(mob,me,++showNumber,showFlag);
			genWeight(mob,me,++showNumber,showFlag);
			genDisposition(mob,me.basePhyStats(),++showNumber,showFlag);
			genBehaviors(mob,me,++showNumber,showFlag);
			genAffects(mob,me,++showNumber,showFlag);
			genImage(mob,me,++showNumber,showFlag);
			for(int x=me.getSaveStatIndex();x<me.getStatCodes().length;x++)
				me.setStat(me.getStatCodes()[x],prompt(mob,me.getStat(me.getStatCodes()[x]),++showNumber,showFlag,CMStrings.capitalizeAndLower(me.getStatCodes()[x])));
			if(me.numScripts()>0)
				genScripts(mob,me,++showNumber,showFlag);

			if (showFlag < -900)
			{
				ok = true;
				break;
			}
			if (showFlag > 0)
			{
				showFlag = -1;
				continue;
			}
			showFlag=CMath.s_int(mob.session().prompt(L("Edit which? "),""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
			}
		}
		if(showFlag != -950)
		{
			me.recoverPhyStats();
			if(me.text().length()>=maxLength)
				mob.tell(L("\n\rThe data entered exceeds the string limit of @x1 characters.",""+maxLength));
		}
	}

	protected void modifyGenArmor(final MOB mob, final Armor me, int showFlag)
		throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		if((showFlag == -1) && (CMProps.getIntVar(CMProps.Int.EDITORTYPE)>0))
			showFlag=-999;
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(!ok))
		{
			int showNumber=0;
			if(showFlag<0)
				mob.tell(L("*. Class: @x1",me.ID()));
			genName(mob,me,++showNumber,showFlag);
			genDisplayText(mob,me,++showNumber,showFlag);
			genDescription(mob,me,++showNumber,showFlag);
			final int oldLevel = me.basePhyStats().level();
			genLevel(mob,me,++showNumber,showFlag);
			if(me.basePhyStats().level() != oldLevel)
				CMLib.itemBuilder().balanceItemByLevel(me);
			genMaterialCode(mob,me,++showNumber,showFlag);
			genWornLocation(mob,me,++showNumber,showFlag);
			genLayer(mob,me,++showNumber,showFlag);
			genRejuv(mob,me,++showNumber,showFlag);
			genArmor(mob,me,++showNumber,showFlag);
			if(me.subjectToWearAndTear())
				genCondition(mob,me,++showNumber,showFlag);
			else
				genUses(mob,me,++showNumber,showFlag);
			genAbility(mob,me,++showNumber,showFlag);
			genSecretIdentity(mob,me,++showNumber,showFlag);
			if(me instanceof ClanItem)
				genClanItem(mob,(ClanItem)me,++showNumber,showFlag);
			genGettable(mob,me,++showNumber,showFlag);
			if(me instanceof Container)
			{
				genCapacity(mob,(Container)me,++showNumber,showFlag);
				if(!(me instanceof Electronics))
				{
					genContainerTypes(mob,(Container)me,++showNumber,showFlag);
				}
				if(me.ID().equalsIgnoreCase("GenCloak"))
					me.setStat("READABLETEXT", prompt(mob,me.getStat("READABLETEXT"),++showNumber,showFlag,L("Cloak Name")));
				else
					genDoorsNLocks(mob,(Container)me,L("lid"),++showNumber,showFlag);
			}
			if(me instanceof Light)
				genBurnout(mob,(Light)me,++showNumber,showFlag);
			genValue(mob,me,++showNumber,showFlag);
			genWeight(mob,me,++showNumber,showFlag);
			if(me instanceof Container) // because thin armor doesn't care about size
				genSize(mob,me,++showNumber,showFlag);
			genDisposition(mob,me.basePhyStats(),++showNumber,showFlag);
			genBehaviors(mob,me,++showNumber,showFlag);
			genAffects(mob,me,++showNumber,showFlag);
			genImage(mob,me,++showNumber,showFlag);
			for(int x=me.getSaveStatIndex();x<me.getStatCodes().length;x++)
				me.setStat(me.getStatCodes()[x],prompt(mob,me.getStat(me.getStatCodes()[x]),++showNumber,showFlag,CMStrings.capitalizeAndLower(me.getStatCodes()[x])));
			if(me.numScripts()>0)
				genScripts(mob,me,++showNumber,showFlag);

			if (showFlag < -900)
			{
				ok = true;
				break;
			}
			if (showFlag > 0)
			{
				showFlag = -1;
				continue;
			}
			showFlag=CMath.s_int(mob.session().prompt(L("Edit which? "),""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
			}
		}
		if(showFlag != -950)
		{
			me.recoverPhyStats();
			if(me.text().length()>=maxLength)
				mob.tell(L("\n\rThe data entered exceeds the string limit of @x1 characters.",""+maxLength));
		}
	}

	protected void modifyGenInstrument(final MOB mob, final MusicalInstrument me, int showFlag)
		throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		if((showFlag == -1) && (CMProps.getIntVar(CMProps.Int.EDITORTYPE)>0))
			showFlag=-999;
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(!ok))
		{
			int showNumber=0;
			if(showFlag<0)
				mob.tell(L("*. Class: @x1",me.ID()));
			genName(mob,me,++showNumber,showFlag);
			genDisplayText(mob,me,++showNumber,showFlag);
			genDescription(mob,me,++showNumber,showFlag);
			genLevel(mob,me,++showNumber,showFlag);
			genMaterialCode(mob,me,++showNumber,showFlag);
			genWornLocation(mob,me,++showNumber,showFlag);
			genRejuv(mob,me,++showNumber,showFlag);
			genAbility(mob,me,++showNumber,showFlag);
			genSecretIdentity(mob,me,++showNumber,showFlag);
			genGettable(mob,me,++showNumber,showFlag);
			genInstrumentType(mob,me,++showNumber,showFlag);
			if(me instanceof Wand)
			{
				promptStatChoices(mob,me,null,++showNumber,showFlag,"Enchant Type","ENCHTYPE", CMParms.toStringArraySingle(Wand.WandUsage.WAND_OPTIONS, 1));
				this.genReadableTextMisc(mob, me, ++showNumber, showFlag);
				this.genMaxCharges(mob, (Wand)me, ++showNumber, showFlag);
			}
			genValue(mob,me,++showNumber,showFlag);
			genWeight(mob,me,++showNumber,showFlag);
			genDisposition(mob,me.basePhyStats(),++showNumber,showFlag);
			genBehaviors(mob,me,++showNumber,showFlag);
			genAffects(mob,me,++showNumber,showFlag);
			genImage(mob,me,++showNumber,showFlag);
			for(int x=me.getSaveStatIndex();x<me.getStatCodes().length;x++)
				me.setStat(me.getStatCodes()[x],prompt(mob,me.getStat(me.getStatCodes()[x]),++showNumber,showFlag,CMStrings.capitalizeAndLower(me.getStatCodes()[x])));
			if(me.numScripts()>0)
				genScripts(mob,me,++showNumber,showFlag);

			if (showFlag < -900)
			{
				ok = true;
				break;
			}
			if (showFlag > 0)
			{
				showFlag = -1;
				continue;
			}
			showFlag=CMath.s_int(mob.session().prompt(L("Edit which? "),""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
			}
		}
		if(showFlag != -950)
		{
			me.recoverPhyStats();
			if(me.text().length()>=maxLength)
				mob.tell(L("\n\rThe data entered exceeds the string limit of @x1 characters.",""+maxLength));
		}
	}

	@Override
	public void modifyGenExit(final MOB mob, final Exit me, int showFlag)
		throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		if((showFlag == -1) && (CMProps.getIntVar(CMProps.Int.EDITORTYPE)>0))
			showFlag=-999;
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(!ok))
		{
			int showNumber=0;
			if(showFlag<0)
				mob.tell(L("*. Class: @x1",me.ID()));
			genName(mob,me,++showNumber,showFlag);
			genDisplayText(mob,me,++showNumber,showFlag);
			genDescription(mob,me,++showNumber,showFlag);
			genLevel(mob,me,++showNumber,showFlag);
			genDoorsNLocks(mob,me,L("door"),++showNumber,showFlag);
			if(me.hasADoor())
			{
				genClosedText(mob,me,++showNumber,showFlag);
				genDoorName(mob,me,++showNumber,showFlag);
				genOpenWord(mob,me,++showNumber,showFlag);
				genCloseWord(mob,me,++showNumber,showFlag);
			}
			genExitMisc(mob,me,++showNumber,showFlag);
			genDisposition(mob,me.basePhyStats(),++showNumber,showFlag);
			genBehaviors(mob,me,++showNumber,showFlag);
			genAffects(mob,me,++showNumber,showFlag);
			genImage(mob,me,++showNumber,showFlag);
			for(int x=me.getSaveStatIndex();x<me.getStatCodes().length;x++)
				me.setStat(me.getStatCodes()[x],prompt(mob,me.getStat(me.getStatCodes()[x]),++showNumber,showFlag,CMStrings.capitalizeAndLower(me.getStatCodes()[x])));
			if(me.numScripts()>0)
				genScripts(mob,me,++showNumber,showFlag);

			if (showFlag < -900)
			{
				ok = true;
				break;
			}
			if (showFlag > 0)
			{
				showFlag = -1;
				continue;
			}
			showFlag=CMath.s_int(mob.session().prompt(L("Edit which? "),""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
			}
		}
		if(showFlag != -950)
		{
			me.recoverPhyStats();
			if(me.text().length()>=maxLength)
				mob.tell(L("\n\rThe data entered exceeds the string limit of @x1 characters.",""+maxLength));
		}
	}

	protected MOB possibleCatalogSwap(final MOB editorMOB, final MOB me) throws IOException
	{
		if(!CMLib.flags().isCataloged(me))
			return me;
		final MOB cataM=CMLib.catalog().getCatalogMob(me.Name());
		if(cataM!=null)
		{
			final Session session = editorMOB.session();
			for(final Enumeration<Item> i=cataM.items();i.hasMoreElements();)
			{
				final Item I=i.nextElement();
				if((I!=null)&&(I.basePhyStats().rejuv()>0)&&(I.basePhyStats().rejuv()!=PhyStats.NO_REJUV)&&(session!=null))
				{
					if(session.confirm(L("\n\r**This mob has variable equipment in the catalog, would you like to reset it first (Y/n)? "),"Y"))
					{
						CMLib.coffeeMaker().unpackEnvironmentalMiscTextXML(me, cataM.text(),false);
						CMLib.catalog().changeCatalogUsage(me, true);
						break;
					}
				}
			}
		}
		return me;
	}

	protected void modifyGenMOB(final MOB mob, final MOB me, int showFlag)
		throws IOException
	{
		if(mob.isMonster())
			return;
		possibleCatalogSwap(mob,me);
		boolean ok=false;
		if((showFlag == -1) && (CMProps.getIntVar(CMProps.Int.EDITORTYPE)>0))
			showFlag=-999;
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(!ok))
		{
			int showNumber=0;
			if(showFlag<0)
				mob.tell(L("*. Class: @x1",me.ID()));
			genName(mob,me,++showNumber,showFlag);
			genDisplayText(mob,me,++showNumber,showFlag);
			genDescription(mob,me,++showNumber,showFlag);
			final int oldLevel=me.basePhyStats().level();
			genLevel(mob,me,++showNumber,showFlag);
			if((oldLevel<2)&&(me.basePhyStats().level()>1))
			{
				CMLib.leveler().fillOutMOB(me,me.basePhyStats().level());
				mob.tell("^ZCombat stats rescored.^.^N");
			}
			genRejuv(mob,me,++showNumber,showFlag);
			genRace(mob,me,++showNumber,showFlag);
			CMLib.factions().updatePlayerFactions(me,me.location(), false);
			Faction F=null;
			for(final Enumeration<Faction> e=CMLib.factions().factions();e.hasMoreElements();)
			{
				F=e.nextElement();
				if(F.showInEditor())
					genSpecialFaction(mob,me,++showNumber,showFlag,F);
			}
			genGender(mob,me,++showNumber,showFlag);
			genHeight(mob,me,++showNumber,showFlag);
			genWeight(mob,me,++showNumber,showFlag);
			genClan(mob,me,++showNumber,showFlag);
			genSpeed(mob,me,++showNumber,showFlag);
			genAttack(mob,me,++showNumber,showFlag);
			genDamage(mob,me,++showNumber,showFlag);
			genArmor(mob,me,++showNumber,showFlag);
			genHitPoints(mob,me,++showNumber,showFlag);
			genMoney(mob,me,++showNumber,showFlag);
			me.setMoneyVariation(CMath.s_double(prompt(mob,""+me.getMoneyVariation(),++showNumber,showFlag,"Money Variation")));
			genAbilities(mob,me,++showNumber,showFlag);
			genBehaviors(mob,me,++showNumber,showFlag);
			genAffects(mob,me,++showNumber,showFlag);
			genDisposition(mob,me.basePhyStats(),++showNumber,showFlag);
			genSensesMask(mob,me.basePhyStats(),++showNumber,showFlag);
			if(me instanceof Rideable)
			{
				genRideableType(mob,(Rideable)me,++showNumber,showFlag);
				genRideableRideCapacity(mob,(Rideable)me,++showNumber,showFlag);
				genMountText(mob,(Rideable)me,++showNumber,showFlag);
				genMountRideMountText(mob,(Rideable)me,++showNumber,showFlag);
			}
			if(me instanceof Drink)
			{
				genThirstQuenched(mob,(Drink)me,++showNumber,showFlag);
				genDrinkHeld(mob,(Drink)me,++showNumber,showFlag);
				genDrinkType(mob, (Drink)me, ++showNumber, showFlag);
			}
			if(me instanceof Deity)
			{
				genDeityClericReq(mob,(Deity)me,++showNumber,showFlag);
				genDeityClericRitual(mob,(Deity)me,++showNumber,showFlag);
				genDeityWorshipReq(mob,(Deity)me,++showNumber,showFlag);
				genDeityWorshipRitual(mob,(Deity)me,++showNumber,showFlag);
				genDeityBlessings(mob,(Deity)me,++showNumber,showFlag);
				genDeityClericSin(mob,(Deity)me,++showNumber,showFlag);
				genDeityWorhsipperSin(mob,(Deity)me,++showNumber,showFlag);
				genDeityCurses(mob,(Deity)me,++showNumber,showFlag);
				genDeityClericPowerRitual(mob,(Deity)me,++showNumber,showFlag);
				genDeityPowers(mob,(Deity)me,++showNumber,showFlag);
				genDeityServiceRitual(mob,(Deity)me,++showNumber,showFlag);
			}
			genFaction(mob,me,++showNumber,showFlag);
			genTattoos(mob,me,++showNumber,showFlag);
			genExpertises(mob,me,++showNumber,showFlag);
			if(me.numScripts()>0)
				genScripts(mob,me,++showNumber,showFlag);
			genImage(mob,me,++showNumber,showFlag);
			for(int x=me.getSaveStatIndex();x<me.getStatCodes().length;x++)
				me.setStat(me.getStatCodes()[x],prompt(mob,me.getStat(me.getStatCodes()[x]),++showNumber,showFlag,CMStrings.capitalizeAndLower(me.getStatCodes()[x])));

			if (showFlag < -900)
			{
				ok = true;
				break;
			}
			if (showFlag > 0)
			{
				showFlag = -1;
				continue;
			}
			showFlag=CMath.s_int(mob.session().prompt(L("Edit which? "),""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
			}
		}
		if(showFlag != -950)
		{
			me.recoverCharStats();
			me.recoverMaxState();
			me.recoverPhyStats();
			me.resetToMaxState();
			if(me.text().length()>=maxLength)
				mob.tell(L("\n\rThe data entered exceeds the string limit of @x1 characters.",""+maxLength));
		}
	}

	@Override
	public void modifyPlayer(final MOB mob, final MOB me, int showFlag) throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		if((showFlag == -1) && (CMProps.getIntVar(CMProps.Int.EDITORTYPE)>0))
			showFlag=-999;
		final String oldName=me.Name();
		if(CMProps.isUsingAccountSystem())
			mob.tell(L("*. Account: '@x1'.",((me.playerStats()!=null)&&(me.playerStats().getAccount()!=null))?me.playerStats().getAccount().getAccountName():L("None")));
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(!ok))
		{
			int showNumber=0;
			genName(mob,me,++showNumber,showFlag);
			String newName=CMStrings.capitalizeAndLower(me.Name());
			me.setName(oldName);
			while(
			(!newName.equals(oldName))
			&&(CMLib.players().playerExists(newName))
			&&(mob.session()!=null)
			&&(!mob.session().isStopped()))
			{
				mob.tell(L("The name given cannot be chosen, as it is already being used."));
				genName(mob,me,showNumber,showNumber); // showNumber twice to force the issue
				newName=CMStrings.capitalizeAndLower(me.Name());
				me.setName(oldName);
			}
			me.setName(newName);

			if(CMProps.isUsingAccountSystem())
			{
				final String oldAccountName = ((me.playerStats()!=null)&&(me.playerStats().getAccount()!=null))?me.playerStats().getAccount().getAccountName():"";
				String accountName =CMStrings.capitalizeAndLower(prompt(mob,oldAccountName,++showNumber,showFlag,"Account",true,false,null));
				while((!accountName.equals(oldAccountName))&&(CMLib.players().getLoadAccount(accountName)==null)
				&&(mob.session()!=null)&&(!mob.session().isStopped()))
				{
					mob.tell(L("The account can not be used, as it does not exist."));
					accountName =CMStrings.capitalizeAndLower(prompt(mob,oldAccountName,showNumber,showFlag,"Account",true,false,null));
				}
				if(!oldAccountName.equals(accountName))
				{
					final PlayerAccount newAccount = CMLib.players().getLoadAccount(accountName);
					me.playerStats().setAccount(newAccount);
					newAccount.addNewPlayer(me);
					final PlayerAccount oldAccount = CMLib.players().getLoadAccount(oldAccountName);
					if(oldAccount!=null)
					{
						oldAccount.delPlayer(me);
						CMLib.database().DBUpdateAccount(oldAccount);
					}
				}
				if(CMProps.getBoolVar(CMProps.Bool.ACCOUNTEXPIRATION))
					genAccountExpiration(mob,me.playerStats().getAccount(),++showNumber,showFlag);
			}
			else
			if(CMProps.getBoolVar(CMProps.Bool.ACCOUNTEXPIRATION))
				genAccountExpiration(mob,me.playerStats(),++showNumber,showFlag);
			genPassword(mob,me,++showNumber,showFlag);

			genDescription(mob,me,++showNumber,showFlag);
			genLevel(mob,me,++showNumber,showFlag);
			genRace(mob,me,++showNumber,showFlag);
			genCharClass(mob,me,++showNumber,showFlag);
			genCharStats(mob,me,++showNumber,showFlag);
			genCharState(mob,me,++showNumber,showFlag);
			CMLib.factions().updatePlayerFactions(me,me.location(), false);
			Faction F=null;
			for(final Enumeration<Faction> e=CMLib.factions().factions();e.hasMoreElements();)
			{
				F=e.nextElement();
				if(F.showInEditor())
					genSpecialFaction(mob,me,++showNumber,showFlag,F);
			}
			genGender(mob,me,++showNumber,showFlag);
			genHeight(mob,me,++showNumber,showFlag);
			genWeight(mob,me,++showNumber,showFlag);
			genClan(mob,me,++showNumber,showFlag);
			genDeity(mob,me,++showNumber,showFlag);
			genSpeed(mob,me,++showNumber,showFlag);
			genAttack(mob,me,++showNumber,showFlag);
			genDamage(mob,me,++showNumber,showFlag);
			genArmor(mob,me,++showNumber,showFlag);
			genTPQ(mob,me,++showNumber,showFlag);
			final PlayerStats pStats = me.playerStats();
			if(pStats != null)
				genCommonBonus(mob,pStats,++showNumber,showFlag);
			genAbilities(mob,me,++showNumber,showFlag);
			genAffects(mob,me,++showNumber,showFlag);
			genBehaviors(mob,me,++showNumber,showFlag);
			genDisposition(mob,me.basePhyStats(),++showNumber,showFlag);
			genSensesMask(mob,me.basePhyStats(),++showNumber,showFlag);
			if(me instanceof Rideable)
			{
				genRideableType(mob,(Rideable)me,++showNumber,showFlag);
				genRideableRideCapacity(mob,(Rideable)me,++showNumber,showFlag);
				genMountText(mob,(Rideable)me,++showNumber,showFlag);
				genMountRideMountText(mob,(Rideable)me,++showNumber,showFlag);
			}
			genFaction(mob,me,++showNumber,showFlag);
			genTattoos(mob,me,++showNumber,showFlag);
			genExpertises(mob,me,++showNumber,showFlag);
			genTitles(mob,me,++showNumber,showFlag);
			genBirthday(mob,me.playerStats(),++showNumber,showFlag);
			genEmail(mob,me.playerStats(),++showNumber,showFlag);
			genSecurity(mob,me,++showNumber,showFlag);
			genImage(mob,me,++showNumber,showFlag);
			genScripts(mob,me,++showNumber,showFlag);
			if(me.playerStats()!=null)
			{
				final String oldFlags = (me.playerStats()!=null)?me.playerStats().getStat("FLAGS"):"";
				promptStatStr(mob,me.playerStats(),PlayerStats.PlayerFlag.getListString(),++showNumber,showFlag,"Flags (?)","FLAGS",true);
				{
					final String flags=(me.playerStats()!=null)?me.playerStats().getStat("FLAGS"):"";
					if(((oldFlags.indexOf(PlayerFlag.NOTOP.name())>=0)&&(flags.indexOf(PlayerFlag.NOTOP.name())<0))
					||((oldFlags.indexOf(PlayerFlag.NOSTATS.name())>=0)&&(flags.indexOf(PlayerFlag.NOSTATS.name())<0))
					||((flags.indexOf(PlayerFlag.NOTOP.name())>=0)&&(oldFlags.indexOf(PlayerFlag.NOTOP.name())<0))
					||((flags.indexOf(PlayerFlag.NOSTATS.name())>=0)&&(oldFlags.indexOf(PlayerFlag.NOSTATS.name())<0)))
						CMLib.players().resetAllPrideStats();
				}
			}
			genNotes(mob,me,++showNumber,showFlag);
			for(int x=me.getSaveStatIndex();x<me.getStatCodes().length;x++)
				me.setStat(me.getStatCodes()[x],prompt(mob,me.getStat(me.getStatCodes()[x]),++showNumber,showFlag,CMStrings.capitalizeAndLower(me.getStatCodes()[x])));
			if(me.playerStats()!=null)
			{
				for(int x=me.playerStats().getSaveStatIndex();x<me.playerStats().getStatCodes().length;x++)
					me.playerStats().setStat(me.playerStats().getStatCodes()[x],prompt(mob,me.playerStats().getStat(me.playerStats().getStatCodes()[x]),++showNumber,showFlag,CMStrings.capitalizeAndLower(me.playerStats().getStatCodes()[x])));
			}

			if (showFlag < -900)
			{
				ok = true;
				break;
			}
			if (showFlag > 0)
			{
				showFlag = -1;
				continue;
			}
			showFlag=CMath.s_int(mob.session().prompt(L("Edit which? "),""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
			}
		}
		if(showFlag >= -900)
		{
			me.recoverCharStats();
			me.recoverMaxState();
			me.recoverPhyStats();
			me.resetToMaxState();
			if(!oldName.equals(me.Name()))
			{
				if(mob.session()!=null)
					mob.session().print(L("Changing player name..."));
				CMLib.players().renamePlayer(me, oldName);
				if(mob.session()!=null)
					mob.session().println(".");
				Log.sysOut("CMGenEditor",mob.Name()+" changed user "+oldName+" to "+me.name());
			}
			Log.sysOut("CMGenEditor",mob.Name()+" edited user "+me.name());
			CMLib.database().DBUpdatePlayer(me);
			CMLib.database().DBUpdateFollowers(me);
		}
	}

	protected void genClanStatus(final MOB mob, final Clan C, final int showNumber, final int showFlag)
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		mob.tell(L("@x1. Clan Status: @x2",""+showNumber,Clan.CLANSTATUS_DESC[C.getStatus()]));
		if((showFlag!=showNumber)&&(showFlag>-999))
			return;
		switch(C.getStatus())
		{
		case Clan.CLANSTATUS_ACTIVE:
			C.setStatus(Clan.CLANSTATUS_PENDING);
			mob.tell(L("Clan '@x1' has been changed from active to pending!",C.name()));
			break;
		case Clan.CLANSTATUS_PENDING:
			C.setStatus(Clan.CLANSTATUS_ACTIVE);
			mob.tell(L("Clan '@x1' has been changed from pending to active!",C.name()));
			break;
		case Clan.CLANSTATUS_FADING:
			C.setStatus(Clan.CLANSTATUS_ACTIVE);
			mob.tell(L("Clan '@x1' has been changed from fading to active!",C.name()));
			break;
		case Clan.CLANSTATUS_STAGNANT:
			C.setStatus(Clan.CLANSTATUS_ACTIVE);
			mob.tell(L("Clan '@x1' has been changed from stagnant to active!",C.name()));
			break;
		default:
			mob.tell(L("Clan '@x1' has not been changed!",C.name()));
			break;
		}
	}

	protected void genClanGovt(final MOB mob, final Clan C, final int showNumber, final int showFlag)
	throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		mob.tell(L("@x1. Government type: '@x2'.",""+showNumber,C.getGovernmentName()));
		if((showFlag!=showNumber)&&(showFlag>-999))
			return;
		while((mob.session()!=null)&&(!mob.session().isStopped()))
		{
			final String newName=mob.session().prompt(L("Enter a new one (?)\n\r:"),"");
			if(newName.trim().length()==0)
			{
				mob.tell(L("(no change)"));
				return;
			}
			int newGovt=-1;
			StringBuilder gvts=new StringBuilder();
			for(final ClanGovernment gvt : CMLib.clans().getStockGovernments())
			{
				gvts.append(gvt.getName()+", ");
				if(newName.equalsIgnoreCase(gvt.getName()))
					newGovt=gvt.getID();
			}
			gvts=new StringBuilder(gvts.substring(0,gvts.length()-2));
			if(newGovt<0)
				mob.tell(L("That government type is invalid.  Valid types include: @x1",gvts.toString()));
			else
			{
				C.setGovernmentID(newGovt);
				break;
			}
		}
	}

	protected double genAuctionPrompt(final MOB mob, final double oldVal, final int showNumber, final int showFlag, final String msg, final boolean pct)
	throws IOException
	{
		final String oldStr=(oldVal<0)?"":(pct?""+(oldVal*100.0)+"%":""+oldVal);
		final String newStr=prompt(mob,oldStr,showNumber,showFlag,msg);
		if(newStr.trim().length()==0)
			return -1.0;
		if((pct)&&(!CMath.isPct(newStr))&&(!CMath.isNumber(newStr)))
			return -1.0;
		else
		if((!pct)&&(!CMath.isNumber(newStr)))
			return -1.0;
		if(pct)
			return CMath.s_pct(newStr);
		return CMath.s_double(newStr);
	}

	protected int genAuctionPrompt(final MOB mob, final int oldVal, final int showNumber, final int showFlag, final String msg)
	throws IOException
	{
		final String oldStr=(oldVal<0)?"":""+oldVal;
		final String newStr=prompt(mob,oldStr,showNumber,showFlag,msg);
		if(newStr.trim().length()==0)
			return -1;
		if(!CMath.isNumber(newStr))
			return -1;
		return CMath.s_int(newStr);
	}

	protected void genClanRole(final MOB mob, final Clan C, final int showNumber, final int showFlag)
	throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		mob.tell(L("@x1. Apply Role: '@x2'.",""+showNumber,C.getRoleName(C.getAutoPosition(),true,false)));
		if((showFlag!=showNumber)&&(showFlag>-999))
			return;
		while((mob.session()!=null)&&(!mob.session().isStopped()))
		{
			final String newName=mob.session().prompt(L("Enter a new one (?)\n\r:"),"");
			if(newName.trim().length()==0)
			{
				mob.tell(L("(no change)"));
				return;
			}
			int newRole=-1;
			StringBuilder roles=new StringBuilder();
			for(int i=0;i<C.getRolesList().length;i++)
			{
				roles.append(C.getRolesList()[i]+", ");
				if(newName.equalsIgnoreCase(C.getRolesList()[i]))
					newRole=i;
			}
			roles=new StringBuilder(roles.substring(0,roles.length()-2));
			if(newRole<0)
				mob.tell(L("That role is invalid.  Valid roles include: @x1",roles.toString()));
			else
			{
				C.setAutoPosition(newRole);
				break;
			}
		}
	}

	protected void genClanClass(final MOB mob, final Clan C, final int showNumber, final int showFlag)
	throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		CharClass CC=CMClass.getCharClass(C.getClanClass());
		if(CC==null)
			CC=CMClass.findCharClass(C.getClanClass());
		final String clasName=(CC==null)?"NONE":CC.name();
		mob.tell(L("@x1. Clan Auto-Class: '@x2'.",""+showNumber,clasName));
		if((showFlag!=showNumber)&&(showFlag>-999))
			return;
		while((mob.session()!=null)&&(!mob.session().isStopped()))
		{
			final String newName=mob.session().prompt(L("Enter a new one (?)\n\r:"),"");
			if(newName.trim().equalsIgnoreCase("none"))
			{
				C.setClanClass("");
				return;
			}
			else
			if(newName.trim().length()==0)
			{
				mob.tell(L("(no change)"));
				return;
			}
			CharClass newC=null;
			StringBuilder clss=new StringBuilder();
			for(final Enumeration<CharClass> e=CMClass.charClasses();e.hasMoreElements();)
			{
				CC=e.nextElement();
				clss.append(CC.name()+", ");
				if(newName.equalsIgnoreCase(CC.name())||(newName.equalsIgnoreCase(CC.ID())))
					newC=CC;
			}
			clss=new StringBuilder(clss.substring(0,clss.length()-2));
			if((newC==null)||(newC.availabilityCode()==0))
				mob.tell(L("That class name is invalid.  Valid names include: @x1",clss.toString()));
			else
			{
				C.setClanClass(newC.ID());
				break;
			}
		}
	}

	protected String genClanRoom(final MOB mob, final Clan C, final String oldRoomID, final String promptCode, final int showNumber, final int showFlag)
	throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return oldRoomID;
		mob.tell(showNumber+CMStrings.replaceAll(promptCode,"@x1",oldRoomID));
		if((showFlag!=showNumber)&&(showFlag>-999))
			return oldRoomID;
		while((mob.session()!=null)&&(!mob.session().isStopped()))
		{
			final String newName=mob.session().prompt(L("Enter a new one (null)\n\r:"),"");
			if(newName.trim().equalsIgnoreCase("null"))
				return "";
			else
			if(newName.trim().length()==0)
			{
				mob.tell(L("(no change)"));
				return oldRoomID;
			}
			final Room newRoom=CMLib.map().getRoom(newName);
			if((newRoom==null)
			||(CMLib.map().getExtendedRoomID(newRoom).length()==0)
			||(!CMLib.law().isPropertyOwnersName(C.clanID(),newRoom)))
				mob.tell(L("That is either not a valid room id, or that room is not owned by the clan."));
			else
				return CMLib.map().getExtendedRoomID(newRoom);
		}
		return oldRoomID;
	}

	@Override
	public void modifyClan(final MOB mob, final Clan C, int showFlag)
	throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		if((showFlag == -1) && (CMProps.getIntVar(CMProps.Int.EDITORTYPE)>0))
			showFlag=-999;
		final String oldName=C.ID();
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(!ok))
		{
			mob.tell(L("*. Name: '@x1'.",C.name()));
			int showNumber=0;
			genClanGovt(mob,C,++showNumber,showFlag);
			C.setCategory(prompt(mob,C.getCategory(),++showNumber,showFlag,"Category",true));
			C.setPremise(prompt(mob,C.getPremise(),++showNumber,showFlag,"Clan Premise",true));
			C.setMinClanMembers(prompt(mob,C.getMinClanMembers(),++showNumber,showFlag,"Minimum members"));
			C.setExp(prompt(mob,C.getExp(),++showNumber,showFlag,"Clan Experience"));
			C.setTaxes(prompt(mob,C.getTaxes(),++showNumber,showFlag,"Clan Tax Rate (X 100%)"));
			C.setDues(prompt(mob,C.getDues(),++showNumber,showFlag,"Clan Dues/yr"));
			C.setMorgue(genClanRoom(mob,C,C.getMorgue(),". Morgue RoomID: '@x1'.",++showNumber,showFlag));
			C.setRecall(genClanRoom(mob,C,C.getRecall(),". Clan Home RoomID: '@x1'.",++showNumber,showFlag));
			C.setDonation(genClanRoom(mob,C,C.getDonation(),". Clan Donate RoomID: '@x1'.",++showNumber,showFlag));
			promptStatStr(mob,C,Clan.ClanFlag.getListString(),++showNumber,showFlag,"Flags (?)","FLAGS",true);
			genClanAccept(mob,C,++showNumber,showFlag);
			genClanClass(mob,C,++showNumber,showFlag);
			genClanRole(mob,C,++showNumber,showFlag);
			genClanStatus(mob,C,++showNumber,showFlag);
			C.setRivalrous(prompt(mob,C.isRivalrous(),++showNumber,showFlag,"Rivalrous Clan"));
			genTattoos(mob, C, ++showNumber, showFlag);
			genClanMembers(mob,C,++showNumber,showFlag);

			/*setClanRelations, votes?*/
			if (showFlag < -900)
			{
				ok = true;
				break;
			}
			if (showFlag > 0)
			{
				showFlag = -1;
				continue;
			}
			showFlag=CMath.s_int(mob.session().prompt(L("Edit which? "),""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
			}
		}
		if(!oldName.equals(C.ID()))
		{
			//cycle through everything changing the name
			CMLib.database().DBDeleteClan(C);
			CMLib.database().DBCreateClan(C);
		}
		C.update();
	}

	protected void modifyGenShopkeeper(final MOB mob, final ShopKeeper me, int showFlag)
		throws IOException
	{
		if(mob.isMonster())
			return;
		if(!(me instanceof MOB))
			return;
		final MOB M=(MOB)me;
		possibleCatalogSwap(mob,M);
		boolean ok=false;
		if((showFlag == -1) && (CMProps.getIntVar(CMProps.Int.EDITORTYPE)>0))
			showFlag=-999;
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(!ok))
		{
			int showNumber=0;
			if(showFlag<0)
				mob.tell(L("*. Class: @x1",me.ID()));
			genName(mob,me,++showNumber,showFlag);
			genDisplayText(mob,me,++showNumber,showFlag);
			genDescription(mob,me,++showNumber,showFlag);
			final int oldLevel=M.basePhyStats().level();
			genLevel(mob,M,++showNumber,showFlag);
			if((oldLevel<2)&&(M.basePhyStats().level()>1))
			{
				CMLib.leveler().fillOutMOB(M,M.basePhyStats().level());
				mob.tell("^ZCombat stats rescored.^.^N");
			}
			genRejuv(mob,M,++showNumber,showFlag);
			genRace(mob,M,++showNumber,showFlag);
			genHeight(mob,M,++showNumber,showFlag);
			genWeight(mob,M,++showNumber,showFlag);
			CMLib.factions().updatePlayerFactions(M,(M).location(), false);
			Faction F=null;
			for(final Enumeration<Faction> e=CMLib.factions().factions();e.hasMoreElements();)
			{
				F=e.nextElement();
				if(F.showInEditor())
					genSpecialFaction(mob,M,++showNumber,showFlag,F);
			}
			genGender(mob,M,++showNumber,showFlag);
			genClan(mob,M,++showNumber,showFlag);
			genSpeed(mob,M,++showNumber,showFlag);
			if((oldLevel<2)&&(M.basePhyStats().level()>1))
				M.basePhyStats().setDamage((int)Math.round(CMath.div(M.basePhyStats().damage(),M.basePhyStats().speed())));
			genAttack(mob,M,++showNumber,showFlag);
			genDamage(mob,M,++showNumber,showFlag);
			genArmor(mob,M,++showNumber,showFlag);
			if(me instanceof MOB)
				genHitPoints(mob,M,++showNumber,showFlag);
			genMoney(mob,M,++showNumber,showFlag);
			M.setMoneyVariation(CMath.s_double(prompt(mob,""+M.getMoneyVariation(),++showNumber,showFlag,"Money Variation")));
			genAbilities(mob,M,++showNumber,showFlag);
			if(me instanceof PhysicalAgent)
				genBehaviors(mob,(PhysicalAgent)me,++showNumber,showFlag);
			genAffects(mob,M,++showNumber,showFlag);
			if(!(me instanceof Auctioneer))
			{
				genShopkeeperType(mob,me,++showNumber,showFlag);
				me.setWhatIsSoldZappermask(prompt(mob,me.getWhatIsSoldZappermask(),++showNumber,showFlag,"Item Buy Mask (?)", true, CMLib.masking().maskHelp("\n\r", "disallow")));
				genShopkeeperShopInventory(mob,me,++showNumber,showFlag);
				genShopkeeperTypeFlags(mob,me,++showNumber,showFlag);
				genEconomicsPrejudice(mob,me,++showNumber,showFlag);
				genEconomicsInventoryReset(mob,me,++showNumber,showFlag);
			}
			genCurrency(mob,me,++showNumber,showFlag);
			genEconomicsIgnoreMask(mob,me,++showNumber,showFlag);
			if(me instanceof Banker)
			{
				genBankerCoinInterest(mob,(Banker)me,++showNumber,showFlag);
				genBankerItemInterest(mob,(Banker)me,++showNumber,showFlag);
				genBankerChain(mob,(Banker)me,++showNumber,showFlag);
				genBankerLoanInterest(mob,(Banker)me,++showNumber,showFlag);
			}
			else
			if(me instanceof PostOffice)
			{
				((PostOffice)me).setPostalChain(prompt(mob,((PostOffice)me).postalChain(),++showNumber,showFlag,"Postal chain"));
				((PostOffice)me).setFeeForNewBox(prompt(mob,((PostOffice)me).feeForNewBox(),++showNumber,showFlag,"Fee to open a new box"));
				((PostOffice)me).setMinimumPostage(prompt(mob,((PostOffice)me).minimumPostage(),++showNumber,showFlag,"Minimum postage cost"));
				((PostOffice)me).setPostagePerPound(prompt(mob,((PostOffice)me).postagePerPound(),++showNumber,showFlag,"Postage cost per pound after 1st pound"));
				((PostOffice)me).setHoldFeePerPound(prompt(mob,((PostOffice)me).holdFeePerPound(),++showNumber,showFlag,"Holding fee per pound per month"));
				((PostOffice)me).setMaxMudMonthsHeld(prompt(mob,((PostOffice)me).maxMudMonthsHeld(),++showNumber,showFlag,"Maximum number of months held"));
			}
			else
			if(me instanceof Librarian)
			{
				((Librarian)me).setLibraryChain(prompt(mob,((Librarian)me).libraryChain(),++showNumber,showFlag,"Library name"));
				((Librarian)me).setMaxBorrowed(prompt(mob,((Librarian)me).getMaxBorrowed(),++showNumber,showFlag,"Max borrowed"));
				((Librarian)me).setMinOverdueDays(prompt(mob,((Librarian)me).getMinOverdueDays(),++showNumber,showFlag,"Overdue mud-days"));
				((Librarian)me).setMaxOverdueDays(prompt(mob,((Librarian)me).getMaxOverdueDays(),++showNumber,showFlag,"Reclaim mud-days"));
				((Librarian)me).setOverdueCharge(prompt(mob,((Librarian)me).getOverdueCharge(),++showNumber,showFlag,"Overdue charge"));
				((Librarian)me).setOverdueChargePct(CMath.s_pct(prompt(mob,CMath.toPct(((Librarian)me).getOverdueChargePct()),++showNumber,showFlag,"Overdue charge pct")));
				((Librarian)me).setDailyOverdueCharge(prompt(mob,((Librarian)me).getDailyOverdueCharge(),++showNumber,showFlag,"Daily overdue charge"));
				((Librarian)me).setDailyOverdueChargePct(CMath.s_pct(prompt(mob,CMath.toPct(((Librarian)me).getDailyOverdueChargePct()),++showNumber,showFlag,"Daily overdue charge pct")));
			}
			else
			if(me instanceof Auctioneer)
			{
				((Auctioneer)me).setAuctionHouse(prompt(mob,((Auctioneer)me).auctionHouse(),++showNumber,showFlag,"Auction house"));
				((Auctioneer)me).setTimedListingPrice(genAuctionPrompt(mob,((Auctioneer)me).timedListingPrice(),++showNumber,showFlag,"Flat fee per auction",false));
				((Auctioneer)me).setTimedListingPct(genAuctionPrompt(mob,((Auctioneer)me).timedListingPct(),++showNumber,showFlag,"Listing Cut/%Pct per day",true));
				((Auctioneer)me).setTimedFinalCutPct(genAuctionPrompt(mob,((Auctioneer)me).timedFinalCutPct(),++showNumber,showFlag,"Cut/%Pct of final price",true));
				((Auctioneer)me).setMaxTimedAuctionDays(genAuctionPrompt(mob,((Auctioneer)me).maxTimedAuctionDays(),++showNumber,showFlag,"Maximum number of auction mud-days"));
				((Auctioneer)me).setMinTimedAuctionDays(genAuctionPrompt(mob,((Auctioneer)me).minTimedAuctionDays(),++showNumber,showFlag,"Minimum number of auction mud-days"));
			}
			else
			if(me instanceof CraftBroker)
			{
				((CraftBroker)me).setBrokerChain(prompt(mob,((CraftBroker)me).brokerChain(),++showNumber,showFlag,"Broker Chain"));
				((CraftBroker)me).setMaxTimedListingDays(genAuctionPrompt(mob,((CraftBroker)me).maxTimedListingDays(),++showNumber,showFlag,"Maximum number of listing mud-days"));
				((CraftBroker)me).setMaxListings(genAuctionPrompt(mob,((CraftBroker)me).maxListings(),++showNumber,showFlag,"Maximum number of listings"));
				((CraftBroker)me).setCommissionPct(genAuctionPrompt(mob,((CraftBroker)me).commissionPct(),++showNumber,showFlag,"Commission Pct%",true));
			}
			else
			{
				genEconomicsPriceFactors(mob,me,++showNumber,showFlag);
				genEconomicsBudget(mob,me,++showNumber,showFlag);
				genEconomicsDevaluationRate(mob,me,++showNumber,showFlag);
			}
			genDisposition(mob,M.basePhyStats(),++showNumber,showFlag);
			genSensesMask(mob,M.basePhyStats(),++showNumber,showFlag);
			genFaction(mob,M,++showNumber,showFlag);
			genTattoos(mob,M,++showNumber,showFlag);
			genExpertises(mob,M,++showNumber,showFlag);
			if(M.numScripts()>0)
				genScripts(mob,M,++showNumber,showFlag);
			genImage(mob,me,++showNumber,showFlag);
			for(int x=me.getSaveStatIndex();x<me.getStatCodes().length;x++)
				me.setStat(me.getStatCodes()[x],prompt(mob,me.getStat(me.getStatCodes()[x]),++showNumber,showFlag,CMStrings.capitalizeAndLower(me.getStatCodes()[x])));

			if (showFlag < -900)
			{
				ok = true;
				break;
			}
			if (showFlag > 0)
			{
				showFlag = -1;
				continue;
			}
			showFlag=CMath.s_int(mob.session().prompt(L("Edit which? "),""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
			}
		}
		if(showFlag != -950)
		{
			M.recoverCharStats();
			M.recoverMaxState();
			M.recoverPhyStats();
			M.resetToMaxState();
			if(me.text().length()>=maxLength)
				mob.tell(L("\n\rThe data entered exceeds the string limit of @x1 characters.",""+maxLength));
		}
	}

	@Override
	public Room modifyRoom(final MOB mob, Room R, int showFlag) throws IOException
	{
		if((showFlag == -1) && (CMProps.getIntVar(CMProps.Int.EDITORTYPE)>0))
			showFlag=-999;
		boolean ok=false;
		while(!ok)
		{
			int showNumber=0;
			R=genRoomType(mob,R,++showNumber,showFlag);
			genDisplayText(mob,R,++showNumber,showFlag);
			while(R.displayText().length()>253)
			{
				R.setDisplayText(R.displayText().substring(0, 253));
				mob.tell(L("253 character limit.  Please confirm:"));
				genDisplayText(mob,R,showNumber,showFlag);
			}
			genDescription(mob,R,++showNumber,showFlag);
			if(R instanceof GridZones)
			{
				genGridLocaleX(mob,(GridZones)R,++showNumber,showFlag);
				genGridLocaleY(mob,(GridZones)R,++showNumber,showFlag);
				//((GridLocale)mob.location()).buildGrid();
			}
			if(R instanceof LocationRoom)
			{
				genLocationCoords(mob,(LocationRoom)R, ++showNumber, showFlag);
			}
			//genClimateType(mob,R,++showNumber,showFlag);
			//R.setAtmosphere(genAnyMaterialCode(mob,"Atmosphere",R.getAtmosphereCode(),true,++showNumber,showFlag));
			genBehaviors(mob,R,++showNumber,showFlag);
			genAffects(mob,R,++showNumber,showFlag);
			for(int x=R.getSaveStatIndex();x<R.getStatCodes().length;x++)
				R.setStat(R.getStatCodes()[x],prompt(mob,R.getStat(R.getStatCodes()[x]),++showNumber,showFlag,CMStrings.capitalizeAndLower(R.getStatCodes()[x])));

			if (showFlag < -900)
			{
				ok = true;
				break;
			}
			if (showFlag > 0)
			{
				showFlag = -1;
				continue;
			}
			showFlag=CMath.s_int(mob.session().prompt(L("Edit which? "),""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
			}
		}
		return R;
	}

	protected void genAccountExpiration(final MOB mob, final AccountStats A, final int showNumber, final int showFlag) throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		mob.tell(L("@x1. Expires: @x2",""+showNumber,CMLib.time().date2String(A.getAccountExpiration())));
		if((showFlag!=showNumber)&&(showFlag>-999))
			return;
		final String s=mob.session().prompt(L("Enter a new value\n\r:"),"");
		if(s.length()>0)
			A.setAccountExpiration(CMLib.time().string2Millis(s));
		else
			mob.tell(L("(no change)"));
	}

	@Override
	public void modifyAccount(final MOB mob, final PlayerAccount A, int showFlag) throws IOException
	{
		if((showFlag == -1) && (CMProps.getIntVar(CMProps.Int.EDITORTYPE)>0))
			showFlag=-999;
		boolean ok=false;
		while(!ok)
		{
			int showNumber=0;
			String acctName=CMStrings.capitalizeAndLower(prompt(mob,A.getAccountName(),++showNumber,showFlag,"Name",true,false,null));
			while((!acctName.equals(A.getAccountName()))
			&&(CMLib.players().getLoadAccount(acctName)!=null)
			&&(mob.session()!=null)&&(!mob.session().isStopped()))
			{
				mob.tell(L("The name given cannot be chosen, as it is already being used."));
				acctName=CMStrings.capitalizeAndLower(prompt(mob,acctName,showNumber,showNumber,"Name",true,false,null));
			}
			A.setAccountName(acctName);
			genEmail(mob, A, ++showNumber, showFlag);
			if(CMProps.getBoolVar(CMProps.Bool.ACCOUNTEXPIRATION))
				genAccountExpiration(mob,A,++showNumber,showFlag);

			final String oldFlags = A.getStat("FLAGS");
			promptStatStr(mob,A,PlayerAccount.AccountFlag.getListString(),++showNumber,showFlag,"Flags (?)","FLAGS",true);
			{
				final String flags = A.getStat("FLAGS");
				if(((oldFlags.indexOf(AccountFlag.NOTOP.name())>=0)&&(flags.indexOf(AccountFlag.NOTOP.name())<0))
				||((oldFlags.indexOf(AccountFlag.NOSTATS.name())>=0)&&(flags.indexOf(AccountFlag.NOSTATS.name())<0))
				||((flags.indexOf(AccountFlag.NOTOP.name())>=0)&&(oldFlags.indexOf(AccountFlag.NOTOP.name())<0))
				||((flags.indexOf(AccountFlag.NOSTATS.name())>=0)&&(oldFlags.indexOf(AccountFlag.NOSTATS.name())<0)))
					CMLib.players().resetAllPrideStats();
			}
			promptStatInt(mob,A,++showNumber,showFlag,L("Bonus Languages: "),"BONUSLANGS");
			promptStatInt(mob,A,++showNumber,showFlag,L("Bonus Char Limit: "),"BONUSCHARLIMIT");
			promptStatInt(mob,A,++showNumber,showFlag,L("Bonus Char Online: "),"BONUSCHARONLINE");
			promptStatInt(mob,A,++showNumber,showFlag,L("Bonus Char Stat Points: "),"BONUSCHARSTATS");
			promptStatInt(mob,A,++showNumber,showFlag,L("Bonus All-Common Skills: "),"BONUSCOMMON");
			promptStatInt(mob,A,++showNumber,showFlag,L("Bonus Crafting Skills: "),"BONUSCRAFT");
			promptStatInt(mob,A,++showNumber,showFlag,L("Bonus Non-Craft Common Skills: "),"BONUSNONCRAFT");
			promptStatStr(mob,A,++showNumber,showFlag,"Notes: ","NOTES");
			genTattoos(mob,A,++showNumber,showFlag);
			for(int x=A.getSaveStatIndex();x<A.getStatCodes().length;x++)
				A.setStat(A.getStatCodes()[x],prompt(mob,A.getStat(A.getStatCodes()[x]),++showNumber,showFlag,CMStrings.capitalizeAndLower(A.getStatCodes()[x])));

			if (showFlag < -900)
			{
				ok = true;
				break;
			}
			if (showFlag > 0)
			{
				showFlag = -1;
				continue;
			}
			showFlag=CMath.s_int(mob.session().prompt(L("Edit which? "),""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
			}
		}
	}

	@Override
	public void modifyStdMob(final MOB mob, final MOB M, int showFlag) throws IOException
	{
		try
		{
			if(M!=mob)
				CMLib.threads().suspendTicking(M, -1);
			if((showFlag == -1) && (CMProps.getIntVar(CMProps.Int.EDITORTYPE)>0))
				showFlag=-999;
			boolean ok=false;
			while(!ok)
			{
				int showNumber=0;
				if(CMLib.flags().isCataloged(M))
				{
					if(CMLib.catalog().isCatalogObj(M.Name()))
						mob.tell(L("*** This object is Cataloged **\n\r"));
					else
						mob.tell(L("*** This object WAS cataloged and is still tied **\n\r"));
				}
				if(showFlag<0)
					mob.tell(L("*. Class: @x1",M.ID()));
				genLevel(mob,M,++showNumber,showFlag);
				genAbility(mob,M,++showNumber,showFlag);
				genRejuv(mob,M,++showNumber,showFlag);
				genMiscText(mob,M,++showNumber,showFlag);
				if (showFlag < -900)
				{
					ok = true;
					break;
				}
				if (showFlag > 0)
				{
					showFlag = -1;
					continue;
				}
				showFlag=CMath.s_int(mob.session().prompt(L("Edit which? "),""));
				if(showFlag<=0)
				{
					showFlag=-1;
					ok=true;
				}
			}
			catalogCheckUpdate(mob, M);
		}
		finally
		{
			if(M!=mob)
				CMLib.threads().resumeTicking(M, -1);
		}
	}

	@Override
	public void modifyStdItem(final MOB mob, final Item I, int showFlag) throws IOException
	{
		if((showFlag == -1) && (CMProps.getIntVar(CMProps.Int.EDITORTYPE)>0))
			showFlag=-999;
		boolean ok=false;
		while(!ok)
		{
			int showNumber=0;
			if(CMLib.flags().isCataloged(I))
			{
				if(CMLib.catalog().isCatalogObj(I.Name()))
					mob.tell(L("*** This object is Cataloged **\n\r"));
				else
					mob.tell(L("*** This object WAS cataloged and is still tied **\n\r"));
			}
			if(showFlag<0)
				mob.tell(L("*. Class: @x1",I.ID()));
			genLevel(mob,I,++showNumber,showFlag);
			genAbility(mob,I,++showNumber,showFlag);
			genRejuv(mob,I,++showNumber,showFlag);
			genUses(mob,I,++showNumber,showFlag);
			genMiscText(mob,I,++showNumber,showFlag);
			if (showFlag < -900)
			{
				ok = true;
				break;
			}
			if (showFlag > 0)
			{
				showFlag = -1;
				continue;
			}
			showFlag=CMath.s_int(mob.session().prompt(L("Edit which? "),""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
			}
		}
		catalogCheckUpdate(mob, I);
	}

	@Override
	public void modifyArea(final MOB mob, final Area myArea, final Set<Area> alsoUpdateAreas, int showFlag) throws IOException
	{
		if((showFlag == -1) && (CMProps.getIntVar(CMProps.Int.EDITORTYPE)>0))
			showFlag=-999;
		boolean ok=false;
		while(!ok)
		{
			int showNumber=0;
			if(showFlag<0)
				mob.tell(L("*. Class: @x1",myArea.ID()));
			final String oldName = myArea.Name();
			genName(mob,myArea,++showNumber,showFlag);
			if(!oldName.equals(myArea.Name()))
			{
				for(final Enumeration<Area> a = myArea.getParents();a.hasMoreElements();)
				{
					final Area A=a.nextElement();
					if((A!=myArea)&&(alsoUpdateAreas!=null))
						alsoUpdateAreas.add(A);
				}
				for(final Enumeration<Area> a = myArea.getChildren();a.hasMoreElements();)
				{
					final Area A=a.nextElement();
					if((A!=myArea)&&(alsoUpdateAreas!=null))
						alsoUpdateAreas.add(A);
				}
			}
			if(myArea instanceof Boardable)
				genDisplayText(mob,myArea,++showNumber,showFlag);
			genDescription(mob,myArea,++showNumber,showFlag);
			genAuthor(mob,myArea,++showNumber,showFlag);
			getTheme(mob,myArea,++showNumber,showFlag);
			genClimateType(mob,myArea,++showNumber,showFlag);
			myArea.setAtmosphere(genAnyMaterialCode(mob,"Atmosphere",myArea.getAtmosphereCode(),true,++showNumber,showFlag));
			genTimeClock(mob,myArea,++showNumber,showFlag);
			genPlayerLevel(mob,myArea,++showNumber,showFlag);
			genParentAreas(mob,myArea,++showNumber,showFlag,alsoUpdateAreas);
			genChildAreas(mob,myArea,++showNumber,showFlag,alsoUpdateAreas);
			genSubOps(mob,myArea,++showNumber,showFlag);
			genAreaBlurbs(mob,myArea,++showNumber,showFlag);
			if(myArea instanceof GridZones)
			{
				genGridLocaleX(mob,(GridZones)myArea,++showNumber,showFlag);
				genGridLocaleY(mob,(GridZones)myArea,++showNumber,showFlag);
			}
			if(myArea instanceof AutoGenArea)
			{
				promptStatStr(mob,myArea,++showNumber,showFlag,"AutoGen Xml File Path","GENERATIONFILEPATH");
				promptStatStr(mob,myArea,++showNumber,showFlag,"AutoGen Variables (VAR=VAL format)","OTHERVARS");
			}
			if(myArea instanceof SpaceObject)
			{
				final SpaceObject spaceArea=(SpaceObject)myArea;
				genSpaceStuff(mob,spaceArea,++showNumber,showFlag);
			}
			genBehaviors(mob,myArea,++showNumber,showFlag);
			genAffects(mob,myArea,++showNumber,showFlag);
			genImage(mob,myArea,++showNumber,showFlag);
			for(int x=myArea.getSaveStatIndex();x<myArea.getStatCodes().length;x++)
				myArea.setStat(myArea.getStatCodes()[x],prompt(mob,myArea.getStat(myArea.getStatCodes()[x]),++showNumber,showFlag,CMStrings.capitalizeAndLower(myArea.getStatCodes()[x])));
			if((showFlag<=0)||((showFlag>=showNumber)&&(showFlag<=showNumber+7)))
			mob.tell(L("*** Area Economics settings: "));
			genCurrency(mob,myArea,++showNumber,showFlag);
			genEconomicsPrejudice(mob,myArea,++showNumber,showFlag);
			genEconomicsPriceFactors(mob,myArea,++showNumber,showFlag);
			genEconomicsBudget(mob,myArea,++showNumber,showFlag);
			genEconomicsDevaluationRate(mob,myArea,++showNumber,showFlag);
			genEconomicsInventoryReset(mob,myArea,++showNumber,showFlag);
			genEconomicsIgnoreMask(mob,myArea,++showNumber,showFlag);
			if (showFlag < -900)
			{
				ok = true;
				break;
			}
			if (showFlag > 0)
			{
				showFlag = -1;
				continue;
			}
			showFlag=CMath.s_int(mob.session().prompt(L("Edit which? "),""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
			}
		}
	}

	@Override
	public AbilityMapper.AbilityMapping modifyAllQualifyEntry(final MOB mob, final String eachOrAll, final Ability me, int showFlag) throws IOException
	{
		if((showFlag == -1) && (CMProps.getIntVar(CMProps.Int.EDITORTYPE)>0))
			showFlag=-999;
		final Map<String,Map<String,AbilityMapper.AbilityMapping>> map=CMLib.ableMapper().getAllQualifiesMap(null);
		final Map<String,AbilityMapper.AbilityMapping> subMap=map.get(eachOrAll.toUpperCase().trim());
		AbilityMapper.AbilityMapping mapped = subMap.get(me.ID().toUpperCase());
		if(mapped==null)
			mapped=CMLib.ableMapper().makeAbilityMapping(me.ID(),1,me.ID(),0,100,"",true,SecretFlag.PUBLIC, true,new Vector<String>(),"",null);
		boolean ok=false;
		while(!ok)
		{
			int showNumber=0;
			mob.tell(L("* Ability Mapping for @x1",me.ID()));
			mapped.qualLevel(prompt(mob,mapped.qualLevel(),++showNumber,showFlag,"Qualifying Level: "));
			mapped.autoGain(prompt(mob,mapped.autoGain(),++showNumber,showFlag,"Auto-Gained: "));
			mapped.defaultProficiency(prompt(mob,mapped.defaultProficiency(),++showNumber,showFlag,"Def. Proficiency: "));
			mapped.secretFlag((SecretFlag)promptEnumChoice(mob, mapped.secretFlag(), SecretFlag.values(), ++showNumber, showFlag, "Visibility"));
			mapped.extraMask(prompt(mob,mapped.extraMask(),++showNumber,showFlag,"Qualifying Mask (?): ", true, CMLib.masking().maskHelp("\n\r", "disallow")));
			mapped.originalSkillPreReqList(prompt(mob,mapped.originalSkillPreReqList(),++showNumber,showFlag,"Required Skills (?): ", true,
					"Space delimited list of Ability IDs.  " +
					"Put a required proficiency level in parenthesis after the Ability ID if desired.  " +
					"For example: Skill_Write Skill_Trip Skill_Dirt(25) Hunting"));
			if (showFlag < -900)
			{
				ok = true;
				break;
			}
			if (showFlag > 0)
			{
				showFlag = -1;
				continue;
			}
			showFlag=CMath.s_int(mob.session().prompt(L("Edit which? "),""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
			}
		}
		return CMLib.ableMapper().makeAbilityMapping(mapped.abilityID(), mapped.qualLevel(), mapped.abilityID(), mapped.defaultProficiency(), 100, "",
													 mapped.autoGain(), mapped.secretFlag(), true, CMParms.parseSpaces(mapped.originalSkillPreReqList().trim(), true),
													 mapped.extraMask(), null);
	}

	@Override
	public void modifyManufacturer(final MOB mob, final Manufacturer me, int showFlag) throws IOException
	{
		if((showFlag == -1) && (CMProps.getIntVar(CMProps.Int.EDITORTYPE)>0))
			showFlag=-999;
		boolean ok=false;
		while(!ok)
		{
			int showNumber=0;
			final String newName=prompt(mob,me.name(),++showNumber,showFlag,"Name: ");
			if(!newName.equals(me.name()))
			{
				CMLib.tech().delManufacturer(me);
				me.setName(newName);
				CMLib.tech().addManufacturer(me);
			}
			me.setEfficiencyPct(CMath.div(prompt(mob,Math.round(me.getEfficiencyPct()*100),++showNumber,showFlag,"Efficiency % (50-150): "),100.0));
			me.setReliabilityPct(CMath.div(prompt(mob,Math.round(me.getReliabilityPct()*100),++showNumber,showFlag,"Reliability % (0-100): "),100.0));
			me.setItemMask(prompt(mob,me.getItemMaskStr(),++showNumber,showFlag,"Item Mask (?): "));
			me.setMinTechLevelDiff((byte)prompt(mob,me.getMinTechLevelDiff(),++showNumber,showFlag,"Min Tech Diff: "));
			me.setMaxTechLevelDiff((byte)prompt(mob,me.getMaxTechLevelDiff(),++showNumber,showFlag,"Max Tech Diff: "));
			if(me.getMaxTechLevelDiff()<me.getMinTechLevelDiff())
				me.setMaxTechLevelDiff(me.getMinTechLevelDiff());
			me.setManufactureredTypesList(promptCommaList(mob,me.getManufactureredTypesList(),++showNumber,showFlag,"Manufact. Types: ",
					"Choices: "+CMParms.toListString(TechType.values()),CMEVAL_INSTANCE,TechType.values()));
			if (showFlag < -900)
			{
				ok = true;
				break;
			}
			if (showFlag > 0)
			{
				showFlag = -1;
				continue;
			}
			showFlag=CMath.s_int(mob.session().prompt(L("Edit which? "),""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
			}
		}
	}
}
