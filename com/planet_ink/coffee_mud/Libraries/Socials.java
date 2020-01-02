package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.sql.*;
import java.io.IOException;

// requires nothing to load
/*
   Copyright 2001-2020 Bo Zimmerman

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
public class Socials extends StdLibrary implements SocialsList
{
	@Override
	public String ID()
	{
		return "Socials";
	}

	@Override
	public void putSocialsInHash(final Map<String,List<Social>> soc, final List<String> lines)
	{
		for(int v=0;v<lines.size();v++)
		{
			String getline=lines.get(v);
			int x=getline.indexOf("\t");
			if(x>=0)
			{
				final Social socobj=(Social)CMClass.getCommon("DefaultSocial");
				final String s=getline.substring(0,x).toUpperCase();
				if(s.length()>0)
				switch(s.charAt(0))
				{
				case 'W':
					socobj.setSourceCode(CMMsg.MSG_SPEAK);
					break;
				case 'M':
					socobj.setSourceCode(CMMsg.MSG_HANDS);
					break;
				case 'S':
					socobj.setSourceCode(CMMsg.MSG_NOISE);
					break;
				case 'O':
					socobj.setSourceCode(CMMsg.MSG_NOISYMOVEMENT);
					break;
				case 'Q':
					socobj.setSourceCode(CMMsg.MSG_SUBTLEMOVEMENT);
					break;
				default:
					socobj.setSourceCode(CMMsg.MSG_HANDS);
					break;
				}
				if(s.length()>1)
				switch(s.charAt(1))
				{
				case 'T':
					socobj.setOthersCode(CMMsg.MSG_HANDS);
					socobj.setTargetCode(CMMsg.MSG_HANDS);
					break;
				case 'S':
					socobj.setOthersCode(CMMsg.MSG_NOISE);
					socobj.setTargetCode(CMMsg.MSG_NOISE);
					break;
				case 'W':
					socobj.setOthersCode(CMMsg.MSG_SPEAK);
					socobj.setTargetCode(CMMsg.MSG_SPEAK);
					break;
				case 'V':
					socobj.setOthersCode(CMMsg.MSG_NOISYMOVEMENT);
					socobj.setTargetCode(CMMsg.MSG_NOISYMOVEMENT);
					break;
				case 'O':
					socobj.setOthersCode(CMMsg.MSG_OK_VISUAL);
					socobj.setTargetCode(CMMsg.MSG_OK_VISUAL);
					break;
				case 'Q':
					socobj.setOthersCode(CMMsg.MSG_SUBTLEMOVEMENT);
					socobj.setTargetCode(CMMsg.MSG_SUBTLEMOVEMENT);
					break;
				default:
					socobj.setOthersCode(CMMsg.MSG_NOISYMOVEMENT);
					socobj.setTargetCode(CMMsg.MSG_NOISYMOVEMENT);
					break;
				}
				getline=getline.substring(x+1);
				x=getline.indexOf("\t");
				if(x>=0)
				{
					socobj.setName(getline.substring(0,x).toUpperCase());
					getline=getline.substring(x+1);
					x=getline.indexOf("\t");
					if(x>=0)
					{
						socobj.setSourceMessage(getline.substring(0,x));
						getline=getline.substring(x+1);
						x=getline.indexOf("\t");
						if(x>=0)
						{
							socobj.setOthersMessage(getline.substring(0,x));
							getline=getline.substring(x+1);
							x=getline.indexOf("\t");
							if(x>=0)
							{
								socobj.setTargetMessage(getline.substring(0,x));
								getline=getline.substring(x+1);
								x=getline.indexOf("\t");
								if(x>=0)
								{
									socobj.setFailedMessage(getline.substring(0,x));
									getline=getline.substring(x+1);
									x=getline.indexOf("\t");
									if(x>=0)
										socobj.setSoundFile(getline.substring(0,x));
									else
										socobj.setSoundFile(getline);
								}
								else
									socobj.setFailedMessage(getline);

							}
						}
						put(soc,socobj.Name(),socobj);
					}
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public Map<String, List<Social>> getSocialHash()
	{
		Map<String,List<Social>> soc=(Map<String,List<Social>>)Resources.getResource("PARSED_SOCIALS: "+filename);
		if(soc==null)
		{
			soc=new SHashtable<String,List<Social>>();
			Resources.submitResource("PARSED_SOCIALS: "+filename,soc);
			final List<String> V=Resources.getFileLineVector(new CMFile(filename,null,CMFile.FLAG_LOGERRORS).text());
			putSocialsInHash(soc,V);
			unloadDerivedResources();
		}
		return soc;
	}

	@Override
	public boolean isLoaded()
	{
		return Resources.getResource("PARSED_SOCIALS: "+filename)!=null;
	}

	private String realName(final String name)
	{
		String shortName=name.toUpperCase().trim();
		final int spdex=shortName.indexOf(' ');
		if(spdex>0)
			shortName=shortName.substring(0,spdex);
		return shortName;
	}

	private void put(final Map<String,List<Social>> H, String name, final Social S)
	{
		name=realName(name);
		List<Social> V2=H.get(name);
		if(V2==null)
		{
			V2=new Vector<Social>(4);
			H.put(name,V2);
		}
		for(int v=0;v<V2.size();v++)
		{
			if(V2.get(v).Name().equalsIgnoreCase(S.Name()))
			{
				V2.remove(v);
				break;
			}
		}
		V2.add(S);
	}

	@Override
	public void put(final String name, final Social S)
	{
		put(getSocialHash(),name,S);
	}

	@Override
	public void remove(final String name)
	{
		final String realName=realName(name);
		final List<Social> V2=getSocialHash().get(realName);
		if(V2==null)
			return;
		for(int v=0;v<V2.size();v++)
		{
			if(V2.get(v).Name().equalsIgnoreCase(name))
			{
				V2.remove(v);
				if(V2.size()==0)
				{
					getSocialHash().remove(realName);
					unloadDerivedResources();
				}
				break;
			}
		}
	}

	@Override
	public void addSocial(final Social S)
	{
		put(S.name(),S);
		unloadDerivedResources();
	}

	@Override
	public int numSocialSets()
	{
		return getSocialHash().size();
	}

	@Override
	public void unloadSocials()
	{
		Resources.removeResource("PARSED_SOCIALS: "+filename);
		unloadDerivedResources();
	}

	private void unloadDerivedResources()
	{
		Resources.removeResource("SOCIALS LIST");
		Resources.removeResource("SOCIALS TABLE");
		Resources.removeResource("WEB SOCIALS TBL");
	}

	@Override
	public boolean shutdown()
	{
		unloadSocials();
		return true;
	}

	@Override
	public void modifySocialOthersCode(final MOB mob, final Social me, final int showNumber, final int showFlag)
	throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		String actionDesc;
		switch(me.getOthersCode())
		{
		case CMMsg.MSG_HANDS:
			actionDesc = "HANDS";
			break;
		case CMMsg.MSG_OK_VISUAL:
			actionDesc = "VISUAL ONLY";
			break;
		case CMMsg.MSG_SPEAK:
			actionDesc = "HEARING WORDS";
			break;
		case CMMsg.MSG_NOISYMOVEMENT:
			actionDesc = "SEEING MOVEMENT";
			break;
		case CMMsg.MSG_SUBTLEMOVEMENT:
			actionDesc = "QUIET MOVE";
			break;
		default:
			actionDesc = "HEARING NOISE";
			break;
		}
		mob.session().safeRawPrintln(L("@x1. Others Effect type: @x2",""+showNumber,actionDesc));
		if((showFlag!=showNumber)&&(showFlag>-999))
			return;
		String newName=mob.session().choose(L("Change W)ords, M)ovement (w/noise), S)ound, V)isual, H)ands, Q)uiet move: "),L("WMSVHQ"),"");
		if((newName!=null)&&(newName.length()>0))
		{
			newName=newName.toUpperCase();
			switch(newName.charAt(0))
			{
			case 'H':
				me.setOthersCode(CMMsg.MSG_HANDS);
				me.setTargetCode(CMMsg.MSG_HANDS);
				break;
			case 'W':
				me.setOthersCode(CMMsg.MSG_SPEAK);
				me.setTargetCode(CMMsg.MSG_SPEAK);
				break;
			case 'M':
				me.setOthersCode(CMMsg.MSG_NOISYMOVEMENT);
				me.setTargetCode(CMMsg.MSG_NOISYMOVEMENT);
				break;
			case 'Q':
				me.setOthersCode(CMMsg.MSG_SUBTLEMOVEMENT);
				me.setTargetCode(CMMsg.MSG_SUBTLEMOVEMENT);
				break;
			case 'S':
				me.setOthersCode(CMMsg.MSG_NOISE);
				me.setTargetCode(CMMsg.MSG_NOISE);
				break;
			case 'V':
				me.setOthersCode(CMMsg.MSG_OK_VISUAL);
				me.setTargetCode(CMMsg.MSG_OK_VISUAL);
				break;
			}
		}
		else
			mob.session().println(L("(no change)"));
	}

	@Override
	public void modifySocialTargetCode(final MOB mob, final Social me, final int showNumber, final int showFlag)
	throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		mob.session().safeRawPrintln(L("@x1. Target Effect type: @x2",""+showNumber,((me.getTargetCode()==CMMsg.MSG_HANDS)?"HANDS":((me.getTargetCode()==CMMsg.MSG_OK_VISUAL)?"VISUAL ONLY":((me.getTargetCode()==CMMsg.MSG_SPEAK)?"HEARING WORDS":((me.getTargetCode()==CMMsg.MSG_NOISYMOVEMENT)?"BEING MOVED ON":"HEARING NOISE"))))));
		if((showFlag!=showNumber)&&(showFlag>-999))
			return;
		String newName=mob.session().choose(L("Change W)ords, M)ovement (w/noise), S)ound, V)isual, H)ands, Q)uiet move: "),L("WMSVHQ"),"");
		if((newName!=null)&&(newName.length()>0))
		{
			newName=newName.toUpperCase();
			switch(newName.charAt(0))
			{
			case 'W':
				me.setTargetCode(CMMsg.MSG_SPEAK);
				break;
			case 'M':
				me.setTargetCode(CMMsg.MSG_NOISYMOVEMENT);
				break;
			case 'Q':
				me.setTargetCode(CMMsg.MSG_SUBTLEMOVEMENT);
				break;
			case 'H':
				me.setTargetCode(CMMsg.MSG_HANDS);
				break;
			case 'S':
				me.setTargetCode(CMMsg.MSG_NOISE);
				break;
			case 'V':
				me.setTargetCode(CMMsg.MSG_OK_VISUAL);
				break;
			}
		}
		else
			mob.session().println(L("(no change)"));
	}

	@Override
	public void modifySocialSourceCode(final MOB mob, final Social me, final int showNumber, final int showFlag)
	throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		String actionDesc;
		switch(me.getSourceCode())
		{
		case CMMsg.MSG_NOISYMOVEMENT:
			actionDesc = "LARGE MOVEMENT";
			break;
		case CMMsg.MSG_SPEAK:
			actionDesc = "SPEAKING";
			break;
		case CMMsg.MSG_HANDS:
			actionDesc = "MOVEMENT";
			break;
		case CMMsg.MSG_SUBTLEMOVEMENT:
			actionDesc = "QUIET MOVE";
			break;
		default:
			actionDesc="MAKING NOISE";
			break;
		}
		mob.session().safeRawPrintln(L("@x1. Your action type: @x2",""+showNumber,actionDesc));
		if((showFlag!=showNumber)&&(showFlag>-999))
			return;
		String newName=mob.session().choose(L("Change W)ords, M)ovement (small), S)ound, L)arge Movement, Q)uiet Move: "),L("WMSLQ"),"");
		if((newName!=null)&&(newName.length()>0))
		{
			newName=newName.toUpperCase();
			switch(newName.charAt(0))
			{
			case 'W':
				me.setSourceCode(CMMsg.MSG_SPEAK);
				break;
			case 'M':
				me.setSourceCode(CMMsg.MSG_HANDS);
				break;
			case 'S':
				me.setSourceCode(CMMsg.MSG_NOISE);
				break;
			case 'L':
				me.setSourceCode(CMMsg.MSG_NOISYMOVEMENT);
				break;
			case 'Q':
				me.setSourceCode(CMMsg.MSG_SUBTLEMOVEMENT);
				break;
			}
		}
		else
			mob.session().println(L("(no change)"));
	}


	/**
	 * Finds the first and only the first instance of the second parameter string in the first string,
	 * and replaces it with the third string.  Returns the first string with or without changes.
	 * This method is case sensitive.
	 * @param str the string to look inside of
	 * @param thisStr the string to look for inside the first string
	 * @param withThisStr the string to replace the second string with, if found.
	 * @return the string modified, or not modified if no replacement was made.
	 */
	protected final static String replaceFirstWordStartsWith(final String str, final String thisStr, final String withThisStr)
	{
		if((str==null)
		||(thisStr==null)
		||(withThisStr==null)
		||(str.length()==0)
		||(thisStr.length()==0))
			return str;
		if(Character.toUpperCase(str.charAt(0))==Character.toUpperCase(thisStr.charAt(0)))
		{
			if(str.toUpperCase().startsWith(thisStr.toUpperCase())
			&&(str.toUpperCase().equals(thisStr.toUpperCase())
				||((str.length()>thisStr.length())&&(Character.isWhitespace(str.charAt(thisStr.length()))))))
				return withThisStr+str.substring(thisStr.length());
			if(thisStr.toUpperCase().startsWith(str.toUpperCase()))
				return withThisStr;
			final int x=str.indexOf(' ');
			if(x>1)
			{
				if(thisStr.toUpperCase().startsWith(str.substring(0,x).toUpperCase()))
					return withThisStr+str.substring(x+1);
			}
		}
		return str;
	}

	@Override
	public boolean modifySocialInterface(final MOB mob, final String socialString)
		throws IOException
	{
		final Vector<String> socialsParse=CMParms.parse(socialString);
		if(socialsParse.size()==0)
		{
			mob.tell(L("Which social?"));
			return false;
		}
		final String name=socialsParse.firstElement().toUpperCase().trim();
		String rest=socialsParse.size()>1?CMParms.combine(socialsParse,1):"";
		List<Social> socials=getSocialsSet(socialsParse.firstElement());
		if(((socials==null)||(socials.size()==0))
		&&((mob.session()==null)
			||(!mob.session().confirm(L("The social '@x1' does not exist.  Create it (y/N)? ",name),"N"))))
			return false;
		if(socials==null)
			socials=new Vector<Social>();
		boolean resaveSocials=true;
		while((resaveSocials)&&(mob.session()!=null)&&(!mob.session().isStopped()))
		{
			resaveSocials=false;
			Social soc=null;
			boolean pickNewSocial=true;
			while((pickNewSocial)&&(mob.session()!=null)&&(!mob.session().isStopped()))
			{
				pickNewSocial=false;
				final StringBuffer str=new StringBuffer(L("\n\rSelect a target:\n\r"));
				int selection=-1;
				for(int v=0;v<socials.size();v++)
				{
					final Social S=socials.get(v);
					final int x=S.Name().indexOf(' ');
					final int y=(x<0)?-1:S.Name().indexOf(' ',x+1);
					if(x<0)
					{
						str.append((v+1)+") No Target (NONE)\n\r");
						continue;
					}
					if((rest.length()>0)
					&&(S.Name().substring(x+1).toUpperCase().trim().equalsIgnoreCase(rest.toUpperCase().trim())))
						selection=(v+1);
					if(S.Name().substring(x+1).toUpperCase().trim().startsWith("<T-NAME>"))
					{
						str.append((v+1)+") MOB Targeted (MOBTARGET)");
						if(y>x)
							str.append(" with argument ("+S.Name().substring(y+1)+")");
						str.append("\n\r");
						continue;
					}
					if(S.Name().substring(x+1).toUpperCase().trim().startsWith("<I-NAME>"))
					{
						str.append((v+1)+") Room Item Targeted (ITEMTARGET)");
						if(y>x)
							str.append(" with argument ("+S.Name().substring(y+1)+")");
						str.append("\n\r");
						continue;
					}
					if(S.Name().substring(x+1).toUpperCase().trim().startsWith("<V-NAME>"))
					{
						str.append((v+1)+") Inventory Targeted (INVTARGET)");
						if(y>x)
							str.append(" with argument ("+S.Name().substring(y+1)+")");
						str.append("\n\r");
						continue;
					}
					if(S.Name().substring(x+1).toUpperCase().trim().startsWith("<E-NAME>"))
					{
						str.append((v+1)+") Equipment Targeted (EQUIPTARGET)");
						if(y>x)
							str.append(" with argument ("+S.Name().substring(y+1)+")");
						str.append("\n\r");
						continue;
					}
					str.append((v+1)+") "+S.Name().substring(x+1).toUpperCase().trim());
					if(y>x)
						str.append(" with argument ("+S.Name().substring(y+1)+")");
					str.append("\n\r");
				}
				str.append(L("@x1) Add a new target\n\r",""+(socials.size()+1)));
				String s=null;
				if((rest.length()>0)&&(selection<0))
					selection=(socials.size()+1);
				else
				if(selection<0)
				{
					mob.session().safeRawPrintln(str.toString());
					s=mob.session().prompt(L("\n\rSelect an option or RETURN: "),"");
					if(!CMath.isInteger(s))
					{
						soc=null;
						break;
					}
					selection=CMath.s_int(s);
				}
				if((selection>0)&&(selection<=socials.size()))
				{
					soc=socials.get(selection-1);
					break;
				}
				String newOne=rest;
				if(newOne.length()==0)
				{
					newOne="?";
					while((newOne.equals("?"))&&(!mob.session().isStopped()))
					{
						newOne=mob.session().prompt(L("\n\rNew target (?): "),"").toUpperCase().trim();
						if(newOne.equals("?"))
							mob.session().println(L("Choices: MOBTARGET, ITEMTARGET, INVTARGET, EQUIPTARGET, NONE, ALL, SELF"));
					}
					if(newOne.trim().length()==0)
					{
						pickNewSocial=true;
						continue;
					}
				}
				newOne=newOne.toUpperCase().trim();
				final String[] validTargets={"NONE","ALL","SELF","<T-NAME>","<I-NAME>","<V-NAME>","<E-NAME>"};
				final String[] friendlyTargets={"NONE","ALL","SELF","MOBS","ITEMS","INVENTORY","EQUIPMENT"};
				newOne=replaceFirstWordStartsWith(newOne, "TNAME", "<T-NAME>");
				newOne=replaceFirstWordStartsWith(newOne, "INAME", "<I-NAME>");
				newOne=replaceFirstWordStartsWith(newOne, "VNAME", "<V-NAME>");
				newOne=replaceFirstWordStartsWith(newOne, "ENAME", "<E-NAME>");
				newOne=replaceFirstWordStartsWith(newOne, "T-NAME", "<T-NAME>");
				newOne=replaceFirstWordStartsWith(newOne, "I-NAME", "<I-NAME>");
				newOne=replaceFirstWordStartsWith(newOne, "V-NAME", "<V-NAME>");
				newOne=replaceFirstWordStartsWith(newOne, "E-NAME", "<E-NAME>");
				newOne=replaceFirstWordStartsWith(newOne, "MOBTARGET", "<T-NAME>");
				newOne=replaceFirstWordStartsWith(newOne, "ITEMTARGET", "<I-NAME>");
				newOne=replaceFirstWordStartsWith(newOne, "INVTARGET", "<V-NAME>");
				newOne=replaceFirstWordStartsWith(newOne, "EQUIPTARGET", "<E-NAME>");
				final int spaceDex=newOne.indexOf(' ');
				final String tag=(spaceDex<0) ? newOne : newOne.substring(0,spaceDex).trim();
				final int foundDex=CMParms.indexOf(validTargets, tag);
				final String friendlyTag = (foundDex < 0) ? "" : friendlyTargets[foundDex];
				if(newOne.startsWith("<"))
				{
					if(foundDex<0)
					{
						mob.tell(L("@x1 is an invalid target.  Enter ? for some choices."));
						pickNewSocial=true;
						continue;
					}
				}
				if(newOne.equalsIgnoreCase("NONE"))
					newOne="";
				else
				if((foundDex<0)
				&&(!mob.session().confirm(L("'@x1' is a non-standard target.  Are you sure (y/N)? ",tag),"N")))
				{
					rest="";
					pickNewSocial=true;
				}
				else
				if((spaceDex > 0)
				&&(!mob.session().confirm(L("Target: '@x1' is a valid target, with an argument of @x2.  Is this OK (Y/n)? ",friendlyTag,newOne.substring(spaceDex+1)),"Y")))
				{
					rest="";
					pickNewSocial=true;
				}
				else
					newOne=" "+newOne;
				if(!pickNewSocial)
				{
					for(int i=0;i<socials.size();i++)
					{
						if(socials.get(i).Name().equals(name+newOne))
						{
							mob.tell(L("This social already exists.  Pick it off the list above."));
							pickNewSocial=true;
							break;
						}
					}
					if(!pickNewSocial)
					{
						soc=makeDefaultSocial(name,newOne);
						addSocial(soc);
						if(!socials.contains(soc))
							socials.add(soc);
						resaveSocials=true;
					}
				}
			}
			if(soc!=null)
			{
				boolean ok=false;
				int showFlag=-1;
				if(CMProps.getIntVar(CMProps.Int.EDITORTYPE)>0)
					showFlag=-999;
				while(!ok)
				{
					int showNumber=0;
					soc.setSourceMessage(CMLib.genEd().prompt(mob,soc.getSourceMessage(),++showNumber,showFlag,L("You-see string"),false,true));
					if(soc.getSourceCode()==CMMsg.MSG_OK_ACTION)
						soc.setSourceCode(CMMsg.MSG_HANDS);
					modifySocialSourceCode(mob,soc,++showNumber,showFlag);
					soc.setOthersMessage(CMLib.genEd().prompt(mob,soc.getOthersMessage(),++showNumber,showFlag,L("Others-see string"),false,true));
					if(soc.getOthersCode()==CMMsg.MSG_OK_ACTION)
						soc.setOthersCode(CMMsg.MSG_HANDS);
					modifySocialOthersCode(mob,soc,++showNumber,showFlag);

					if(soc.targetName().equals("<T-NAME>"))
					{
						soc.setTargetMessage(CMLib.genEd().prompt(mob,soc.getTargetMessage(),++showNumber,showFlag,L("Target-sees string"),false,true));
						if(soc.getTargetCode()==CMMsg.MSG_OK_ACTION)
							soc.setTargetCode(CMMsg.MSG_HANDS);
						modifySocialTargetCode(mob,soc,++showNumber,showFlag);
					}
					if(soc.targetName().equals("<T-NAME>")
					||soc.targetName().equals("<I-NAME>")
					||soc.targetName().equals("<V-NAME>")
					||soc.targetName().equals("<E-NAME>")
					||soc.targetName().equals("ALL"))
						soc.setFailedMessage(CMLib.genEd().prompt(mob,soc.getFailedTargetMessage(),++showNumber,showFlag,L("You-see when no target"),false,true));
					soc.setSoundFile(CMLib.genEd().prompt(mob,soc.getSoundFile(),++showNumber,showFlag,L("Sound file"),true,false));
					resaveSocials=true;
					if(showFlag<-900)
					{
						ok=true;
						break;
					}
					if(showFlag>0)
					{
						showFlag=-1;
						continue;
					}
					final String input = mob.session().prompt(L("Edit which (or DELETE)? "),"");
					showFlag=CMath.s_int(input);
					if((input!=null)&&(input.equalsIgnoreCase("DELETE")))
					{
						remove(soc.Name());
						socials.remove(soc);
						mob.session().rawOut(L("\n\rSocial variation '@x1' deleted.\n\r",soc.Name()));
						showFlag=-1;
						ok=true;
					}
					else
					if(showFlag<=0)
					{
						showFlag=-1;
						ok=true;
					}
				}
			}
			if((resaveSocials)&&(soc!=null))
			{
				save(mob);
				Log.sysOut("Socials",mob.Name()+" modified social "+soc.name()+".");
				soc=null;
				if(rest.length()>0)
					break;
			}
		}
		return true;
	}

	@Override
	public Social fetchSocial(final List<Social> set, String targetCode, String arg, final boolean exactOnly)
	{
		targetCode=targetCode.toUpperCase().trim();
		arg=arg.toUpperCase().trim();
		for(final Social S : set)
		{
			if(S.targetName().equals(targetCode) && S.argumentName().equals(arg))
				return S;
		}
		if(exactOnly)
			return null;
		for(final Social S : set)
		{
			if(S.targetName().equals(targetCode) && S.argumentName().startsWith(arg))
				return S;
		}
		return null;
	}

	@Override
	public Social fetchSocial(final List<Social> set, String fullSocialID, final boolean exactOnly)
	{
		fullSocialID=fullSocialID.toUpperCase().trim();
		for(int s=0;s<set.size();s++)
		{
			if(set.get(s).Name().equals(fullSocialID))
				return set.get(s);
		}
		if(exactOnly)
			return null;
		fullSocialID=fullSocialID.toUpperCase();
		for(int s=0;s<set.size();s++)
		{
			if(set.get(s).Name().toUpperCase().startsWith(fullSocialID))
				return set.get(s);
		}
		return null;
	}

	@Override
	public Social fetchSocial(final String baseName, final Environmental targetE, final String arg, final boolean exactOnly)
	{
		return fetchSocial(getSocialHash(), baseName, targetE, arg, exactOnly);
	}

	protected Social fetchSocial(final Map<String,List<Social>> soc, final String baseName, final Environmental targetE, final String arg, final boolean exactOnly)
	{
		if(targetE==null)
			return fetchSocial(soc, baseName, "", "", exactOnly);
		if(targetE instanceof MOB)
			return fetchSocial(soc, baseName,"<T-NAME>", arg, exactOnly);
		if(!(targetE instanceof Item))
			return null;
		final Item I=(Item)targetE;
		if(I.owner() instanceof Room)
			return fetchSocial(soc, baseName,"<I-NAME>", arg, exactOnly);
		if(!(I.owner() instanceof MOB))
			return null;
		if(I.amWearingAt(Wearable.IN_INVENTORY))
			return fetchSocial(soc, baseName, "<V-NAME>", arg, exactOnly);
		return fetchSocial(soc, baseName, "<E-NAME>", arg, exactOnly);
	}

	@Override
	public Social fetchSocial(final String fullSocialID, final boolean exactOnly)
	{
		final Map<String,List<Social>> soc = getSocialHash();
		final int x=fullSocialID.indexOf(' ');
		String baseID = ((x<0) ? fullSocialID : fullSocialID.substring(0,x)).toUpperCase().trim();
		final String rest = ((x<0) ? "" : fullSocialID.substring(x+1)).trim();
		List<Social> listS = soc.get(baseID);
		if((listS == null) && (exactOnly))
			return null;
		if(listS == null)
		{
			for(final String key : soc.keySet())
			{
				if(key.startsWith(baseID))
				{
					listS=soc.get(key);
					if(listS.size()>0)
						baseID = listS.get(0).baseName();
					break;
				}
			}
			if(listS == null)
				return null;
		}
		return fetchSocial(listS, (baseID + " " + rest).trim(), exactOnly);
	}

	protected Social fetchSocial(final Map<String,List<Social>> soc, String baseName, final String target, final String arg, final boolean exactOnly)
	{
		baseName=baseName.toUpperCase().trim();
		List<Social> listS=soc.get(baseName);
		if(listS==null)
		{
			if(!exactOnly)
			{
				for(final String key : soc.keySet())
				{
					if(key.startsWith(baseName))
					{
						listS=soc.get(key);
						return fetchSocial(listS,target,arg,exactOnly);
					}
				}
			}
			return null;
		}
		return fetchSocial(listS,target,arg,exactOnly);
	}

	@Override
	public Social fetchSocial(final List<String> C, final boolean exactOnly, final boolean checkItemTargets)
	{
		return fetchSocialFromSet(getSocialHash(),C,exactOnly,checkItemTargets);
	}

	@Override
	public Social fetchSocialFromSet(final Map<String,List<Social>> soc, final List<String> C, final boolean exactOnly, final boolean checkItemTargets)
	{
		if(C==null)
			return null;
		if(C.size()==0)
			return null;

		final String socialName=C.get(0);
		final String target= (C.size()>1) ? C.get(1).toUpperCase().trim() : "";
		final String arg = (C.size() > 2) ? C.get(2).toUpperCase().trim() : "";
		if((target.equals("SELF"))||(target.equals("ALL")))
			return fetchSocial(soc, socialName, target, arg, exactOnly);
		final List<Social> listS = getSocialsSet(socialName, exactOnly);
		if(listS == null)
			return null; // not a chance
		if(target.length()==0)
		{
			for(final Social S : listS)
			{
				if(!S.isTargetable()
				&&(S.targetName().length() == 0))
					return S;
			}
			return null;
		}
		for(final Social S : listS)
		{
			if(S.targetName().equals("<T-NAME>")
			&&(S.argumentName().equals(arg)
				||((!exactOnly)&&(S.argumentName().startsWith(arg)))))
					return S;
		}
		if(!checkItemTargets)
			return null;
		for(final Social S : listS)
		{
			if(S.targetName().startsWith("<")
			&&(S.argumentName().equals(arg)
				||((!exactOnly)&&(S.argumentName().startsWith(arg)))))
					return S;
		}
		if(exactOnly || (arg.length()==0))
			return null;
		for(final Social S : listS)
		{
			if(S.targetName().startsWith("<")
			&&(S.argumentName().startsWith(arg)))
				return S;
		}
		return null;
	}

	@Override
	public List<Social> enumSocialSet(final int index)
	{
		if((index<0)||(index>numSocialSets()))
			return null;
		int i=0;
		final Map<String,List<Social>> soc=getSocialHash();
		for (final String key : soc.keySet())
		{
			final List<Social> V=soc.get(key);
			if((i++)==index)
				return V;
		}
		return null;
	}

	@Override
	public Social makeDefaultSocial(final String name, String type)
	{
		final Social soc=(Social)CMClass.getCommon("DefaultSocial");
		if((type.length()>0)&&(!type.startsWith(" ")))
			type=" "+type;
		soc.setName(name+type);
		String funnyAppendage="";
		if(soc.argumentName().length()>0)
			funnyAppendage=" in the "+soc.argumentName().toLowerCase();
		if(type.trim().length()==0)
		{
			soc.setSourceMessage("You "+name.toLowerCase()+".");
			soc.setOthersMessage("<S-NAME> "+name.toLowerCase()+"s.");
			soc.setSourceCode(CMMsg.MSG_HANDS);
			soc.setOthersCode(CMMsg.MSG_HANDS);
		}
		else
		if(type.trim().equals("ALL"))
		{
			soc.setSourceMessage("You "+name.toLowerCase()+" everyone"+funnyAppendage+".");
			soc.setOthersMessage("<S-NAME> "+name.toLowerCase()+"s everyone"+funnyAppendage+".");
			soc.setFailedMessage(CMStrings.capitalizeAndLower(name)+" who?");
			soc.setSourceCode(CMMsg.MSG_SPEAK);
			soc.setOthersCode(CMMsg.MSG_SPEAK);
		}
		else
		if(type.trim().startsWith("<"))
		{
			soc.setSourceMessage("You "+name.toLowerCase()+" <T-NAME>"+funnyAppendage+".");
			soc.setTargetMessage("<S-NAME> "+name.toLowerCase()+"s you"+funnyAppendage+".");
			soc.setOthersMessage("<S-NAME> "+name.toLowerCase()+"s <T-NAMESELF>"+funnyAppendage+".");
			soc.setFailedMessage(CMStrings.capitalizeAndLower(name)+" who?");
			soc.setSourceCode(CMMsg.MSG_NOISYMOVEMENT);
			soc.setTargetCode(CMMsg.MSG_NOISYMOVEMENT);
			soc.setOthersCode(CMMsg.MSG_NOISYMOVEMENT);
		}
		else
		if(type.trim().equals("SELF"))
		{
			soc.setSourceMessage("You "+name.toLowerCase()+" yourself"+funnyAppendage+".");
			soc.setOthersMessage("<S-NAME> "+name.toLowerCase()+"s <S-HIM-HERSELF>"+funnyAppendage+".");
			soc.setSourceCode(CMMsg.MSG_NOISE);
			soc.setOthersCode(CMMsg.MSG_NOISE);
		}
		else
		{
			soc.setSourceMessage("You "+name.toLowerCase()+type.toLowerCase()+".");
			soc.setOthersMessage("<S-NAME> "+name.toLowerCase()+"s"+type.toLowerCase()+".");
			soc.setSourceCode(CMMsg.MSG_HANDS);
			soc.setOthersCode(CMMsg.MSG_HANDS);
		}
		return soc;
	}

	@Override
	public void save(final MOB whom)
	{
		if(!isLoaded())
			return;
		final Map<String,List<Social>> soc=getSocialHash();
		final StringBuffer buf=new StringBuffer("");
		Vector<Social> V2=new Vector<Social>();
		for (final String key : soc.keySet())
		{
			final List<Social> V1 = soc.get(key);
			for(int v1=0;v1<V1.size();v1++)
			{
				final Social S1=V1.get(v1);
				for(int v2=0;v2<V2.size();v2++)
				{
					final Social S2=V2.elementAt(v2);
					if(S1.equals(S2))
					{
						V2.insertElementAt(S1,v2);
						break;
					}
				}
				if(!V2.contains(S1))
					V2.addElement(S1);
			}
		}
		final Vector<Social> sorted=new Vector<Social>();
		while(V2.size()>0)
		{
			Social lowest=V2.firstElement();
			Social S=null;
			for(int i=1;i<V2.size();i++)
			{
				S=V2.elementAt(i);
				if(S.name().compareToIgnoreCase(lowest.Name())<=0)
					lowest=S;
			}
			V2.remove(lowest);
			sorted.add(lowest);
		}
		V2=sorted;
		for(int v=0;v<V2.size();v++)
		{
			final Social I=V2.elementAt(v);

			switch(I.getSourceCode())
			{
			case CMMsg.MSG_SPEAK:
				buf.append('w');
				break;
			case CMMsg.MSG_HANDS:
				buf.append('m');
				break;
			case CMMsg.MSG_NOISE:
				buf.append('s');
				break;
			case CMMsg.MSG_NOISYMOVEMENT:
				buf.append('o');
				break;
			case CMMsg.MSG_QUIETMOVEMENT:
			case CMMsg.MSG_SUBTLEMOVEMENT:
				buf.append('q');
				break;
			default:
				buf.append(' ');
				break;
			}
			switch(I.getTargetCode())
			{
			case CMMsg.MSG_HANDS:
				buf.append('t');
				break;
			case CMMsg.MSG_NOISE:
				buf.append('s');
				break;
			case CMMsg.MSG_SPEAK:
				buf.append('w');
				break;
			case CMMsg.MSG_NOISYMOVEMENT:
				buf.append('v');
				break;
			case CMMsg.MSG_QUIETMOVEMENT:
			case CMMsg.MSG_SUBTLEMOVEMENT:
				buf.append('q');
				break;
			case CMMsg.MSG_OK_VISUAL:
				buf.append('o');
				break;
			default:
				buf.append(' ');
				break;
			}
			final String[] stuff=new String[6];
			stuff[0]=I.name();
			stuff[1]=I.getSourceMessage();
			stuff[2]=I.getOthersMessage();
			stuff[3]=I.getTargetMessage();
			stuff[4]=I.getFailedTargetMessage();
			stuff[5]=I.getSoundFile();
			buf.append('\t');
			for (final String element : stuff)
			{
				if(element==null)
					buf.append("\t");
				else
					buf.append(element+"\t");
			}
			buf.setCharAt(buf.length()-1,'\r');
			buf.append('\n');
		}
		// allowed is forced because this is already protected by SOCIALS security flag
		if(!new CMFile(filename,whom,CMFile.FLAG_FORCEALLOW).saveText(buf))
			Log.errOut("Socials","Unable to save socials.txt!");
		unloadDerivedResources();
	}

	@Override
	public List<Social> getSocialsSet(String named)
	{
		named=realName(named);
		return getSocialHash().get(named);
	}

	@Override
	public String findSocialName(final String named, final boolean exactOnly)
	{
		return findSocialName(getSocialHash(),named,exactOnly);
	}

	protected List<Social> getSocialsSet(String named, final boolean exactOnly)
	{
		if(named == null)
			return null;
		named=realName(named);
		final List<Social> listS=getSocialsSet(named);
		if(listS != null)
			return listS;
		if(exactOnly)
			return null;
		final Map<String,List<Social>> soc=getSocialHash();
		for(final String key : soc.keySet())
		{
			if(key.startsWith(named))
				return soc.get(key);
		}
		return null;
	}

	protected String findSocialName(final Map<String,List<Social>> soc, final String named, final boolean exactOnly)
	{
		if(named==null)
			return null;
		final int x=named.indexOf(' ');
		final String baseID = ((x<0) ? named : named.substring(0,x)).toUpperCase().trim();
		List<Social> listS = soc.get(baseID);
		if((listS == null) && (exactOnly))
			return null;
		if(listS == null)
		{
			for(final String key : soc.keySet())
			{
				if(key.startsWith(baseID))
				{
					listS=soc.get(key);
					if(listS.size()>0)
						return listS.get(0).baseName();
				}
			}
			return null;
		}

		if(listS.size()>0)
			return listS.get(0).baseName();
		return null;
	}

	@Override
	public String getSocialsHelp(final MOB mob, final String named, final boolean exact)
	{
		final String realName=findSocialName(named,exact);
		if(realName==null)
			return null;
		final List<Social> list=getSocialsSet(realName.toUpperCase());
		if((list==null)||(list.size()==0))
			return null;
		final StringBuffer help=new StringBuffer("");
		help.append("^H\n\r");
		help.append("Social     : ^x"+realName+"^.^N\n\r");
		final Session session=(mob!=null)?mob.session():null;
		final MOB tgtMOB=CMClass.getFactoryMOB();
		tgtMOB.setName(L("the target"));
		final MOB othMOB=CMClass.getFactoryMOB();
		othMOB.setName(L("someone"));
		for(int l=0;l<list.size();l++)
		{
			final Social S=list.get(l);
			final int x=S.Name().indexOf(' ');
			final String rest=(x>0)?S.Name().substring(x+1).trim().toUpperCase():"";
			if(rest.length()==0)
			{
				help.append("\n\r");
				help.append("^H");
				help.append("Target     ^?: ^xnone^.^N\n\r");
				help.append("You see    : "+CMLib.coffeeFilter().fullOutFilter(session, mob, mob, null, null, S.getSourceMessage(), false)+"\n\r");
				help.append("Others see : "+CMLib.coffeeFilter().fullOutFilter(session, othMOB, mob, null, null, S.getOthersMessage(), false)+"\n\r");
			}
			else
			if(rest.equals("<T-NAME>"))
			{
				help.append("\n\r");
				help.append("^H");
				help.append("Target     ^?: ^xsomeone^.^N\n\r");
				help.append("No Target  : "+CMLib.coffeeFilter().fullOutFilter(session, mob, mob, tgtMOB, null, S.getFailedTargetMessage(), false)+"\n\r");
				help.append("You see    : "+CMLib.coffeeFilter().fullOutFilter(session, mob, mob, tgtMOB, null, S.getSourceMessage(), false)+"\n\r");
				help.append("Target sees: "+CMLib.coffeeFilter().fullOutFilter(session, tgtMOB, mob, tgtMOB, null, S.getTargetMessage(), false)+"\n\r");
				help.append("Others see : "+CMLib.coffeeFilter().fullOutFilter(session, othMOB, mob, tgtMOB, null, S.getOthersMessage(), false)+"\n\r");
			}
			else
			if(rest.equals("<I-NAME>"))
			{
				help.append("\n\r");
				help.append("^H");
				help.append("Target     ^?: ^xroom item^.^N\n\r");
				help.append("No Target  : "+CMLib.coffeeFilter().fullOutFilter(session, mob, mob, tgtMOB, null, S.getFailedTargetMessage(), false)+"\n\r");
				help.append("You see    : "+CMLib.coffeeFilter().fullOutFilter(session, mob, mob, tgtMOB, null, S.getSourceMessage(), false)+"\n\r");
				help.append("Others see : "+CMLib.coffeeFilter().fullOutFilter(session, othMOB, mob, tgtMOB, null, S.getOthersMessage(), false)+"\n\r");
			}
			else
			if(rest.equals("<V-NAME>"))
			{
				help.append("\n\r");
				help.append("^H");
				help.append("Target     ^?: ^xinventory item^.^N\n\r");
				help.append("No Target  : "+CMLib.coffeeFilter().fullOutFilter(session, mob, mob, tgtMOB, null, S.getFailedTargetMessage(), false)+"\n\r");
				help.append("You see    : "+CMLib.coffeeFilter().fullOutFilter(session, mob, mob, tgtMOB, null, S.getSourceMessage(), false)+"\n\r");
				help.append("Others see : "+CMLib.coffeeFilter().fullOutFilter(session, othMOB, mob, tgtMOB, null, S.getOthersMessage(), false)+"\n\r");
			}
			else
			if(rest.equals("<E-NAME>"))
			{
				help.append("\n\r");
				help.append("^H");
				help.append("Target     ^?: ^xequipped item^.^N\n\r");
				help.append("No Target  : "+CMLib.coffeeFilter().fullOutFilter(session, mob, mob, tgtMOB, null, S.getFailedTargetMessage(), false)+"\n\r");
				help.append("You see    : "+CMLib.coffeeFilter().fullOutFilter(session, mob, mob, tgtMOB, null, S.getSourceMessage(), false)+"\n\r");
				help.append("Others see : "+CMLib.coffeeFilter().fullOutFilter(session, othMOB, mob, tgtMOB, null, S.getOthersMessage(), false)+"\n\r");
			}
			else
			{
				help.append("\n\r");
				help.append("^H");
				help.append("Target     ^?: ^x"+rest.toLowerCase()+"^.^N\n\r");
				help.append("You see    : "+CMLib.coffeeFilter().fullOutFilter(session, mob, mob, null, null, S.getSourceMessage(), false)+"\n\r");
				help.append("Others see : "+CMLib.coffeeFilter().fullOutFilter(session, othMOB, mob, null, null, S.getOthersMessage(), false)+"\n\r");
			}
		}
		tgtMOB.destroy();
		othMOB.destroy();
		return help.toString();
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<String> getSocialsList()
	{
		final List<String> socialsList=(List<String>)Resources.getResource("SOCIALS LIST");
		if(socialsList!=null)
			return socialsList;

		final List<String> socialVec=new Vector<String>();
		for(int s=0;s<CMLib.socials().numSocialSets();s++)
		{
			final List<Social> V=CMLib.socials().enumSocialSet(s);
			if((V==null)||(V.size()==0))
				continue;
			final Social S=V.get(0);
			socialVec.add(realName(S.Name()));
		}
		Collections.sort(socialVec);
		Resources.submitResource("SOCIALS LIST",socialVec);
		return socialVec;
	}

	@Override
	public Enumeration<Social> getAllSocials()
	{
		MultiEnumeration<Social> m=null;
		for(int s=0;s<CMLib.socials().numSocialSets();s++)
		{
			final List<Social> V=CMLib.socials().enumSocialSet(s);
			if((V==null)||(V.size()==0))
				continue;
			if(m==null)
				m=new MultiEnumeration<Social>(new IteratorEnumeration<Social>(V.iterator()));
			else
				m.addEnumeration(new IteratorEnumeration<Social>(V.iterator()));
		}
		return m;
	}

	@Override
	public String getSocialsTable()
	{
		StringBuffer socialsList=(StringBuffer)Resources.getResource("SOCIALS TABLE");
		if(socialsList!=null)
			return socialsList.toString();
		final List<String> socialVec=getSocialsList();
		socialsList=new StringBuffer("");
		int col=0;
		for(int i=0;i<socialVec.size();i++)
		{
			if((++col)>4)
			{
				socialsList.append("\n\r");
				col=1;
			}
			socialsList.append(CMStrings.padRight(socialVec.get(i),19));
		}
		Resources.submitResource("SOCIALS TABLE",socialsList);
		return socialsList.toString();
	}
}
