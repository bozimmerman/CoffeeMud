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
   Copyright 2001-2018 Bo Zimmerman

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
						socobj.setYou_see(getline.substring(0,x));
						getline=getline.substring(x+1);
						x=getline.indexOf("\t");
						if(x>=0)
						{
							socobj.setThird_party_sees(getline.substring(0,x));
							getline=getline.substring(x+1);
							x=getline.indexOf("\t");
							if(x>=0)
							{
								socobj.setTarget_sees(getline.substring(0,x));
								getline=getline.substring(x+1);
								x=getline.indexOf("\t");
								if(x>=0)
								{
									socobj.setSee_when_no_target(getline.substring(0,x));
									getline=getline.substring(x+1);
									x=getline.indexOf("\t");
									if(x>=0)
										socobj.setMSPfile(getline.substring(0,x));
									else
										socobj.setMSPfile(getline);
								}
								else
									socobj.setSee_when_no_target(getline);

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

	private String realName(String name)
	{
		String shortName=name.toUpperCase().trim();
		final int spdex=shortName.indexOf(' ');
		if(spdex>0)
			shortName=shortName.substring(0,spdex);
		return shortName;
	}

	private void put(Map<String,List<Social>> H, String name, Social S)
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
	public void put(String name, Social S)
	{
		put(getSocialHash(),name,S);
	}

	@Override
	public void remove(String name)
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
	public void addSocial(Social S)
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
	public void modifySocialOthersCode(MOB mob, Social me, int showNumber, int showFlag)
	throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		mob.session().safeRawPrintln(L("@x1. Others Effect type: @x2",""+showNumber,((me.othersCode()==CMMsg.MSG_HANDS)?"HANDS":((me.othersCode()==CMMsg.MSG_OK_VISUAL)?"VISUAL ONLY":((me.othersCode()==CMMsg.MSG_SPEAK)?"HEARING WORDS":((me.othersCode()==CMMsg.MSG_NOISYMOVEMENT)?"SEEING MOVEMENT":"HEARING NOISE"))))));
		if((showFlag!=showNumber)&&(showFlag>-999))
			return;
		String newName=mob.session().choose(L("Change W)ords, M)ovement (w/noise), S)ound, V)isual, H)ands: "),L("WMSVH"),"");
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
	public void modifySocialTargetCode(MOB mob, Social me, int showNumber, int showFlag)
	throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		mob.session().safeRawPrintln(L("@x1. Target Effect type: @x2",""+showNumber,((me.targetCode()==CMMsg.MSG_HANDS)?"HANDS":((me.targetCode()==CMMsg.MSG_OK_VISUAL)?"VISUAL ONLY":((me.targetCode()==CMMsg.MSG_SPEAK)?"HEARING WORDS":((me.targetCode()==CMMsg.MSG_NOISYMOVEMENT)?"BEING MOVED ON":"HEARING NOISE"))))));
		if((showFlag!=showNumber)&&(showFlag>-999))
			return;
		String newName=mob.session().choose(L("Change W)ords, M)ovement (w/noise), S)ound, V)isual, H)ands: "),L("WMSVH"),"");
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
	public void modifySocialSourceCode(MOB mob, Social me, int showNumber, int showFlag)
	throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber))
			return;
		mob.session().safeRawPrintln(L("@x1. Your action type: @x2",""+showNumber,((me.sourceCode()==CMMsg.MSG_NOISYMOVEMENT)?"LARGE MOVEMENT":((me.sourceCode()==CMMsg.MSG_SPEAK)?"SPEAKING":((me.sourceCode()==CMMsg.MSG_HANDS)?"MOVEMENT":"MAKING NOISE")))));
		if((showFlag!=showNumber)&&(showFlag>-999))
			return;
		String newName=mob.session().choose(L("Change W)ords, M)ovement (small), S)ound, L)arge Movement: "),L("WMSL"),"");
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
			}
		}
		else
			mob.session().println(L("(no change)"));
	}

	@Override
	public boolean modifySocialInterface(MOB mob, String socialString)
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
					if(x<0)
					{
						str.append((v+1)+") No Target (NONE)\n\r");
						continue;
					}
					if((rest.length()>0)
					&&(S.Name().substring(x+1).toUpperCase().trim().equalsIgnoreCase(rest.toUpperCase().trim())))
						selection=(v+1);
					if(S.Name().substring(x+1).toUpperCase().trim().equalsIgnoreCase("<T-NAME>"))
					{
						str.append((v+1)+") MOB Targeted (MOBTARGET)\n\r");
						continue;
					}
					if(S.Name().substring(x+1).toUpperCase().trim().equalsIgnoreCase("<I-NAME>"))
					{
						str.append((v+1)+") Room Item Targeted (ITEMTARGET)\n\r");
						continue;
					}
					if(S.Name().substring(x+1).toUpperCase().trim().equalsIgnoreCase("<V-NAME>"))
					{
						str.append((v+1)+") Inventory Targeted (INVTARGET)\n\r");
						continue;
					}
					if(S.Name().substring(x+1).toUpperCase().trim().equalsIgnoreCase("<E-NAME>"))
					{
						str.append((v+1)+") Equipment Targeted (EQUIPTARGET)\n\r");
						continue;
					}
					str.append((v+1)+") "+S.Name().substring(x+1).toUpperCase().trim()+"\n\r");
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
				if(newOne.startsWith("<T-")||(newOne.startsWith("T-")))
					newOne="TNAME";
				if(newOne.startsWith("<I-")||(newOne.startsWith("I-")))
					newOne="INAME";
				if(newOne.startsWith("<V-")||(newOne.startsWith("V-")))
					newOne="VNAME";
				if(newOne.startsWith("<E-")||(newOne.startsWith("E-")))
					newOne="ENAME";
				if(newOne.equalsIgnoreCase("TNAME")||newOne.equalsIgnoreCase("TARGET"))
					newOne=" <T-NAME>";
				else
				if(newOne.equalsIgnoreCase("INAME")||newOne.equalsIgnoreCase("ITEMTARGET"))
					newOne=" <I-NAME>";
				else
				if(newOne.equalsIgnoreCase("VNAME")||newOne.equalsIgnoreCase("INVTARGET"))
					newOne=" <V-NAME>";
				else
				if(newOne.equalsIgnoreCase("ENAME")||newOne.equalsIgnoreCase("EQUIPTARGET"))
					newOne=" <E-NAME>";
				else
				if(newOne.equalsIgnoreCase("NONE"))
					newOne="";
				else
				if(!newOne.equals("ALL")&&!newOne.equals("SELF")
				&&!mob.session().confirm(L("'@x1' is a non-standard target.  Are you sure (y/N)? ",newOne),"N"))
				{
					rest="";
					pickNewSocial=true;
				}
				else
					newOne=" "+newOne;
				if(!pickNewSocial)
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
			if(soc!=null)
			{
				boolean ok=false;
				int showFlag=-1;
				if(CMProps.getIntVar(CMProps.Int.EDITORTYPE)>0)
					showFlag=-999;
				while(!ok)
				{
					int showNumber=0;
					soc.setYou_see(CMLib.genEd().prompt(mob,soc.You_see(),++showNumber,showFlag,L("You-see string"),false,true));
					if(soc.sourceCode()==CMMsg.MSG_OK_ACTION)
						soc.setSourceCode(CMMsg.MSG_HANDS);
					modifySocialSourceCode(mob,soc,++showNumber,showFlag);
					soc.setThird_party_sees(CMLib.genEd().prompt(mob,soc.Third_party_sees(),++showNumber,showFlag,L("Others-see string"),false,true));
					if(soc.othersCode()==CMMsg.MSG_OK_ACTION)
						soc.setOthersCode(CMMsg.MSG_HANDS);
					modifySocialOthersCode(mob,soc,++showNumber,showFlag);
					if(soc.Name().endsWith(" <T-NAME>"))
					{
						soc.setTarget_sees(CMLib.genEd().prompt(mob,soc.Target_sees(),++showNumber,showFlag,L("Target-sees string"),false,true));
						if(soc.targetCode()==CMMsg.MSG_OK_ACTION)
							soc.setTargetCode(CMMsg.MSG_HANDS);
						modifySocialTargetCode(mob,soc,++showNumber,showFlag);
					}
					if(soc.Name().endsWith(" <T-NAME>")||soc.Name().endsWith(" <I-NAME>")||soc.Name().endsWith(" <V-NAME>")||soc.Name().endsWith(" <E-NAME>")||(soc.Name().endsWith(" ALL")))
						soc.setSee_when_no_target(CMLib.genEd().prompt(mob,soc.See_when_no_target(),++showNumber,showFlag,L("You-see when no target"),false,true));
					soc.setMSPfile(CMLib.genEd().prompt(mob,soc.MSPfile(),++showNumber,showFlag,L("Sound file"),true,false));
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
	public Social fetchSocial(List<Social> set, String name, boolean exactOnly)
	{
		for(int s=0;s<set.size();s++)
		{
			if(set.get(s).Name().equalsIgnoreCase(name))
				return set.get(s);
		}
		if(exactOnly)
			return null;
		name=name.toUpperCase();
		for(int s=0;s<set.size();s++)
		{
			if(set.get(s).Name().toUpperCase().startsWith(name))
				return set.get(s);
		}
		return null;
	}

	@Override
	public Social fetchSocial(String baseName, Environmental targetE, boolean exactOnly)
	{
		return fetchSocial(getSocialHash(),baseName,targetE,exactOnly);
	}

	protected Social fetchSocial(final Map<String,List<Social>> soc, final String baseName, final Environmental targetE, final boolean exactOnly)
	{
		if(targetE==null)
			return fetchSocial(soc,baseName,exactOnly);
		if(targetE instanceof MOB)
			return fetchSocial(soc,baseName+" <T-NAME>",exactOnly);
		if(!(targetE instanceof Item))
			return null;
		final Item I=(Item)targetE;
		if(I.owner() instanceof Room)
			return fetchSocial(soc,baseName+" <I-NAME>",exactOnly);
		if(!(I.owner() instanceof MOB))
			return null;
		if(I.amWearingAt(Wearable.IN_INVENTORY))
			return fetchSocial(soc,baseName+" <V-NAME>",exactOnly);
		return fetchSocial(soc,baseName+" <E-NAME>",exactOnly);
	}

	@Override
	public Social fetchSocial(String name, boolean exactOnly)
	{
		return fetchSocial(getSocialHash(),name,exactOnly);
	}

	protected Social fetchSocial(final Map<String,List<Social>> soc, final String name, final boolean exactOnly)
	{
		final String realName=realName(name);
		final List<Social> V=soc.get(realName);
		if((V==null)&&(exactOnly))
			return null;
		Social S=null;
		if(V!=null)
			S=fetchSocial(V,name,exactOnly);
		if(S!=null)
			return S;
		if(V==null)
			return null;
		for(final String key : soc.keySet())
		{
			if(key.startsWith(name))
				return fetchSocial(V,name,false);
		}
		return null;
	}

	@Override
	public Social fetchSocial(List<String> C, boolean exactOnly, boolean checkItemTargets)
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

		String socialName=C.get(0);
		String theRest="";
		Social S=null;
		boolean tryTargets=false;
		if(C.size()>1)
		{
			final String Target=C.get(1).toUpperCase();
			S=fetchSocial(soc,socialName+" "+Target,true);
			if((S==null)
			&&((!Target.equals("SELF"))&&(!Target.equals("ALL"))))
			{
				if(checkItemTargets)
					tryTargets=true;
				else
					theRest=" <T-NAME>";
			}
			else
			if(S==null)
				theRest=" <T-NAME>";
		}
		if(S==null)
		{
			if(!tryTargets)
				S=fetchSocial(soc,socialName+theRest,true);
			else
			if((S=fetchSocial(soc,socialName+" <T-NAME>",true))==null)
			{
				if((S=fetchSocial(soc,socialName+" <I-NAME>",true))==null)
				{
					if((S=fetchSocial(soc,socialName+" <E-NAME>",true))==null)
					{
						if((S=fetchSocial(soc,socialName+" <V-NAME>",true))==null)
						{
						}
					}
				}
			}
		}
		if((S==null)&&(!exactOnly))
		{
			String backupSocialName=null;
			final String socName=socialName.toUpperCase();
			socialName=null;
			for(final String key : soc.keySet())
			{
				if((key.startsWith(socName))&&(key.indexOf(' ')<0))
				{
					socialName=key;
					break;
				}
				else
				if(key.startsWith(socName))
				{
					backupSocialName=key;
					break;
				}
			}
			if(socialName==null)
			{
				if(backupSocialName == null)
					socialName=C.get(0);
				else
					socialName=backupSocialName;
			}
			if(socialName!=null)
			{
				if(!tryTargets)
					S=fetchSocial(soc,socialName+theRest,true);
				else
				if((S=fetchSocial(soc,socialName+" <T-NAME>",true))==null)
				{
					if((S=fetchSocial(soc,socialName+" <I-NAME>",true))==null)
					{
						if((S=fetchSocial(soc,socialName+" <E-NAME>",true))==null)
						{
							if((S=fetchSocial(soc,socialName+" <V-NAME>",true))==null)
							{
							}
						}
					}
				}
			}
		}
		return S;
	}

	@Override
	public List<Social> enumSocialSet(int index)
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
	public Social makeDefaultSocial(String name, String type)
	{
		final Social soc=(Social)CMClass.getCommon("DefaultSocial");
		if((type.length()>0)&&(!type.startsWith(" ")))
			type=" "+type;
		soc.setName(name+type);
		if(type.trim().length()==0)
		{
			soc.setYou_see("You "+name.toLowerCase()+".");
			soc.setThird_party_sees("<S-NAME> "+name.toLowerCase()+"s.");
			soc.setSourceCode(CMMsg.MSG_HANDS);
			soc.setOthersCode(CMMsg.MSG_HANDS);
		}
		else
		if(type.trim().equals("ALL"))
		{
			soc.setYou_see("You "+name.toLowerCase()+" everyone.");
			soc.setThird_party_sees("<S-NAME> "+name.toLowerCase()+"s everyone.");
			soc.setSee_when_no_target(CMStrings.capitalizeAndLower(name)+" who?");
			soc.setSourceCode(CMMsg.MSG_SPEAK);
			soc.setOthersCode(CMMsg.MSG_SPEAK);
		}
		else
		if(type.trim().endsWith("-NAME>"))
		{
			soc.setYou_see("You "+name.toLowerCase()+" <T-NAME>.");
			soc.setTarget_sees("<S-NAME> "+name.toLowerCase()+"s you.");
			soc.setThird_party_sees("<S-NAME> "+name.toLowerCase()+"s <T-NAMESELF>.");
			soc.setSee_when_no_target(CMStrings.capitalizeAndLower(name)+" who?");
			soc.setSourceCode(CMMsg.MSG_NOISYMOVEMENT);
			soc.setTargetCode(CMMsg.MSG_NOISYMOVEMENT);
			soc.setOthersCode(CMMsg.MSG_NOISYMOVEMENT);
		}
		else
		if(type.trim().equals("SELF"))
		{
			soc.setYou_see("You "+name.toLowerCase()+" yourself.");
			soc.setThird_party_sees("<S-NAME> "+name.toLowerCase()+"s <S-HIM-HERSELF>.");
			soc.setSourceCode(CMMsg.MSG_NOISE);
			soc.setOthersCode(CMMsg.MSG_NOISE);
		}
		else
		{
			soc.setYou_see("You "+name.toLowerCase()+type.toLowerCase()+".");
			soc.setThird_party_sees("<S-NAME> "+name.toLowerCase()+"s"+type.toLowerCase()+".");
			soc.setSourceCode(CMMsg.MSG_HANDS);
			soc.setOthersCode(CMMsg.MSG_HANDS);
		}
		return soc;
	}

	@Override
	public void save(MOB whom)
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

			switch(I.sourceCode())
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
			default:
				buf.append(' ');
				break;
			}
			switch(I.targetCode())
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
			case CMMsg.MSG_OK_VISUAL:
				buf.append('o');
				break;
			default:
				buf.append(' ');
				break;
			}
			final String[] stuff=new String[6];
			stuff[0]=I.name();
			stuff[1]=I.You_see();
			stuff[2]=I.Third_party_sees();
			stuff[3]=I.Target_sees();
			stuff[4]=I.See_when_no_target();
			stuff[5]=I.MSPfile();
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
	public String findSocialName(String named, boolean exactOnly)
	{
		return findSocialName(getSocialHash(),named,exactOnly);
	}

	protected String findSocialName(final Map<String,List<Social>> soc, final String named, final boolean exactOnly)
	{
		if(named==null)
			return null;
		final Social S = fetchSocial(soc, named,exactOnly);
		if(S!=null)
			return realName(S.Name()).toLowerCase();
		return null;
	}

	@Override
	public String getSocialsHelp(MOB mob, String named, boolean exact)
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
				help.append("You see    : "+CMLib.coffeeFilter().fullOutFilter(session, mob, mob, null, null, S.You_see(), false)+"\n\r");
				help.append("Others see : "+CMLib.coffeeFilter().fullOutFilter(session, othMOB, mob, null, null, S.Third_party_sees(), false)+"\n\r");
			}
			else
			if(rest.equals("<T-NAME>"))
			{
				help.append("\n\r");
				help.append("^H");
				help.append("Target     ^?: ^xsomeone^.^N\n\r");
				help.append("No Target  : "+CMLib.coffeeFilter().fullOutFilter(session, mob, mob, tgtMOB, null, S.See_when_no_target(), false)+"\n\r");
				help.append("You see    : "+CMLib.coffeeFilter().fullOutFilter(session, mob, mob, tgtMOB, null, S.You_see(), false)+"\n\r");
				help.append("Target sees: "+CMLib.coffeeFilter().fullOutFilter(session, tgtMOB, mob, tgtMOB, null, S.Target_sees(), false)+"\n\r");
				help.append("Others see : "+CMLib.coffeeFilter().fullOutFilter(session, othMOB, mob, tgtMOB, null, S.Third_party_sees(), false)+"\n\r");
			}
			else
			if(rest.equals("<I-NAME>"))
			{
				help.append("\n\r");
				help.append("^H");
				help.append("Target     ^?: ^xroom item^.^N\n\r");
				help.append("No Target  : "+CMLib.coffeeFilter().fullOutFilter(session, mob, mob, tgtMOB, null, S.See_when_no_target(), false)+"\n\r");
				help.append("You see    : "+CMLib.coffeeFilter().fullOutFilter(session, mob, mob, tgtMOB, null, S.You_see(), false)+"\n\r");
				help.append("Others see : "+CMLib.coffeeFilter().fullOutFilter(session, othMOB, mob, tgtMOB, null, S.Third_party_sees(), false)+"\n\r");
			}
			else
			if(rest.equals("<V-NAME>"))
			{
				help.append("\n\r");
				help.append("^H");
				help.append("Target     ^?: ^xinventory item^.^N\n\r");
				help.append("No Target  : "+CMLib.coffeeFilter().fullOutFilter(session, mob, mob, tgtMOB, null, S.See_when_no_target(), false)+"\n\r");
				help.append("You see    : "+CMLib.coffeeFilter().fullOutFilter(session, mob, mob, tgtMOB, null, S.You_see(), false)+"\n\r");
				help.append("Others see : "+CMLib.coffeeFilter().fullOutFilter(session, othMOB, mob, tgtMOB, null, S.Third_party_sees(), false)+"\n\r");
			}
			else
			if(rest.equals("<E-NAME>"))
			{
				help.append("\n\r");
				help.append("^H");
				help.append("Target     ^?: ^xequipped item^.^N\n\r");
				help.append("No Target  : "+CMLib.coffeeFilter().fullOutFilter(session, mob, mob, tgtMOB, null, S.See_when_no_target(), false)+"\n\r");
				help.append("You see    : "+CMLib.coffeeFilter().fullOutFilter(session, mob, mob, tgtMOB, null, S.You_see(), false)+"\n\r");
				help.append("Others see : "+CMLib.coffeeFilter().fullOutFilter(session, othMOB, mob, tgtMOB, null, S.Third_party_sees(), false)+"\n\r");
			}
			else
			{
				help.append("\n\r");
				help.append("^H");
				help.append("Target     ^?: ^x"+rest.toLowerCase()+"^.^N\n\r");
				help.append("You see    : "+CMLib.coffeeFilter().fullOutFilter(session, mob, mob, null, null, S.You_see(), false)+"\n\r");
				help.append("Others see : "+CMLib.coffeeFilter().fullOutFilter(session, othMOB, mob, null, null, S.Third_party_sees(), false)+"\n\r");
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
