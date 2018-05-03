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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2002-2018 Bo Zimmerman

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

public class CharClassData extends StdWebMacro
{
	@Override
	public String name()
	{
		return "CharClassData";
	}

	private String classDropDown(String old)
	{
		final StringBuffer str=new StringBuffer("");
		str.append("<OPTION VALUE=\"\" "+((old.length()==0)?"SELECTED":"")+">None");
		CharClass C2=null;
		String C2ID=null;
		for(final Enumeration<CharClass> e=CMClass.charClasses();e.hasMoreElements();)
		{
			C2=e.nextElement();
			C2ID="com.planet_ink.coffee_mud.CharClasses."+C2.ID();
			if(C2.isGeneric() && CMClass.checkForCMClass(CMObjectType.CHARCLASS,C2ID))
			{
				str.append("<OPTION VALUE=\""+C2.ID()+"\" "+((old.equalsIgnoreCase(C2.ID()))?"SELECTED":"")+">"+C2.ID()+" (Generic)");
				str.append("<OPTION VALUE=\""+C2ID+"\" "+((old.equalsIgnoreCase(C2ID))?"SELECTED":"")+">"+C2ID);
			}
			else
			if(C2.isGeneric())
				str.append("<OPTION VALUE=\""+C2.ID()+"\" "+((old.equalsIgnoreCase(C2.ID())||old.equalsIgnoreCase(C2ID))?"SELECTED":"")+">"+C2.ID()+" (Generic)");
			else
				str.append("<OPTION VALUE=\""+C2ID+"\" "+((old.equalsIgnoreCase(C2.ID())||old.equalsIgnoreCase(C2ID))?"SELECTED":"")+">"+C2ID);
		}
		return str.toString();
	}

	public static StringBuffer cabilities(MOB mob, CharClass E, HTTPRequest httpReq, java.util.Map<String,String> parms, int borderSize, String font)
	{
		final StringBuffer str=new StringBuffer("");
		final DVector theclasses=new DVector(9);
		final boolean showPreReqs=httpReq.isUrlParameter("SHOWPREREQS")&&httpReq.getUrlParameter("SHOWPREREQS").equalsIgnoreCase("on");
		final boolean showMasks=httpReq.isUrlParameter("SHOWMASKS")&&httpReq.getUrlParameter("SHOWMASKS").equalsIgnoreCase("on");
		final boolean showParms=httpReq.isUrlParameter("SHOWPARMS")&&httpReq.getUrlParameter("SHOWPARMS").equalsIgnoreCase("on");
		if(httpReq.isUrlParameter("CABLES1"))
		{
			int num=1;
			String behav=httpReq.getUrlParameter("CABLES"+num);
			while(behav!=null)
			{
				if(behav.length()>0)
				{
					String prof=httpReq.getUrlParameter("CABPOF"+num);
					if((prof==null)||(!CMath.isInteger(prof)))
						prof="0";
					String qual=httpReq.getUrlParameter("CABQUA"+num);
					if(qual==null) qual=""; // null means unchecked
					String levl=httpReq.getUrlParameter("CABLVL"+num);
					if((levl==null)||(!CMath.isInteger(levl)))
						levl="0";
					String secr=httpReq.getUrlParameter("CABSCR"+num);
					if(secr==null) secr=""; // null means unchecked
					String parm=httpReq.getUrlParameter("CABPRM"+num);
					if(parm==null)
						parm="";
					String prereqs=httpReq.getUrlParameter("CABPRE"+num);
					if(prereqs==null)
						prereqs="";
					String mask=httpReq.getUrlParameter("CABMSK"+num);
					if(mask==null)
						mask="";
					String maxp=httpReq.getUrlParameter("CABMPOF"+num);
					if((maxp==null)||(!CMath.isInteger(maxp)))
						maxp="100";
					theclasses.addElement(behav,levl,prof,qual,secr,parm,prereqs,mask,maxp);
				}
				num++;
				behav=httpReq.getUrlParameter("CABLES"+num);
			}
		}
		else
		{
			final List<AbilityMapper.AbilityMapping> data1=CMLib.ableMapper().getUpToLevelListings(E.ID(),Integer.MAX_VALUE,true,false);
			final DVector sortedData1=new DVector(2);
			String aID=null;
			int minLvl=Integer.MAX_VALUE;
			int maxLvl=Integer.MIN_VALUE;
			for(final AbilityMapper.AbilityMapping able : data1)
			{
				aID=able.abilityID();
				if(!CMLib.ableMapper().getAllQualified(E.ID(), true, aID))
				{
					final int qlvl=CMLib.ableMapper().getQualifyingLevel(E.ID(), false, aID);
					if(qlvl>maxLvl)
						maxLvl=qlvl;
					if(qlvl<minLvl)
						minLvl=qlvl;
					sortedData1.addElement(aID,Integer.valueOf(qlvl));
				}
			}
			Integer qLvl=null;
			for(int lvl=minLvl;lvl<=maxLvl;lvl++)
			{
				for(int i=0;i<sortedData1.size();i++)
				{
					qLvl=(Integer)sortedData1.elementAt(i,2);
					if(qLvl.intValue()==lvl)
					{
						aID=(String)sortedData1.elementAt(i,1);
						theclasses.addElement(aID,
											  qLvl.toString(),
											  Integer.toString(CMLib.ableMapper().getDefaultProficiency(E.ID(),false,aID)),
											  CMLib.ableMapper().getDefaultGain(E.ID(),false,aID)?"":"on",
											  CMLib.ableMapper().getSecretSkill(E.ID(),false,aID)?"on":"",
											  CMLib.ableMapper().getDefaultParm(E.ID(),false,aID),
											  CMLib.ableMapper().getPreReqStrings(E.ID(), false, aID),
											  CMLib.ableMapper().getExtraMask(E.ID(),false,aID),
											  Integer.toString(CMLib.ableMapper().getMaxProficiency(E.ID(),false,aID)));
					}
				}
			}
		}
		if(font==null)
			font="<FONT COLOR=WHITE><B>";
		str.append("<TABLE WIDTH=100% BORDER="+borderSize+" CELLSPACING=0 CELLPADDING=0>");
		final String sfont=(parms.containsKey("FONT"))?("<FONT "+(parms.get("FONT"))+">"):"";
		final String efont=(parms.containsKey("FONT"))?"</FONT>":"";
		if(parms.containsKey("HEADERCOL1")
		||parms.containsKey("HEADERCOL2")
		||parms.containsKey("HEADERCOL3")
		||parms.containsKey("HEADERCOL4")
		||parms.containsKey("HEADERCOL5"))
		{
			str.append("<TR><TD WIDTH=40%>");
			if(parms.containsKey("HEADERCOL1"))
				str.append(sfont + (parms.get("HEADERCOL1")) + efont);
			str.append("</TD><TD WIDTH=10%>");
			if(parms.containsKey("HEADERCOL2"))
				str.append(sfont + (parms.get("HEADERCOL2")) + efont);
			str.append("</TD><TD WIDTH=10%>");
			if(parms.containsKey("HEADERCOL3"))
				str.append(sfont + (parms.get("HEADERCOL3")) + efont);
			str.append("</TD><TD WIDTH=10%>");
			if(parms.containsKey("HEADERCOL4"))
				str.append(sfont + (parms.get("HEADERCOL4")) + efont);
			str.append("</TD><TD WIDTH=30%>");
			if(parms.containsKey("HEADERCOL5"))
				str.append(sfont + (parms.get("HEADERCOL5")) + efont);
			str.append("</TD></TR>");
		}
		final HashSet<String> used=new HashSet<String>();
		for(int i=0;i<theclasses.size();i++)
		{
			final String theclass=(String)theclasses.elementAt(i,1);
			used.add(theclass);
			str.append("<TR><TD WIDTH=40%>");
			str.append("<SELECT ONCHANGE=\"EditAffect(this);\" NAME=CABLES"+(i+1)+">");
			str.append("<OPTION VALUE=\"\">Delete!");
			str.append("<OPTION VALUE=\""+theclass+"\" SELECTED>"+theclass);
			str.append("</SELECT>");
			str.append("</TD>");
			str.append("<TD WIDTH=10%>");
			str.append("<INPUT TYPE=TEXT NAME=CABLVL"+(i+1)+" VALUE=\""+((String)theclasses.elementAt(i,2))+"\" SIZE=2 MAXLENGTH=3>");
			str.append("</TD>");
			str.append("<TD WIDTH=10%>");
			str.append("<INPUT TYPE=TEXT NAME=CABPOF"+(i+1)+" VALUE=\""+((String)theclasses.elementAt(i,3))+"\" SIZE=2 MAXLENGTH=3>"+font+"%</B></I></FONT>");
			str.append("</TD>");
			str.append("<TD WIDTH=10%>");
			str.append("<INPUT TYPE=TEXT NAME=CABMPOF"+(i+1)+" VALUE=\""+((String)theclasses.elementAt(i,9))+"\" SIZE=2 MAXLENGTH=3>"+font+"%</B></I></FONT>");
			str.append("</TD>");
			str.append("<TD WIDTH=30%>");
			str.append("<INPUT TYPE=CHECKBOX NAME=CABQUA"+(i+1)+" "+(((String)theclasses.elementAt(i,4)).equalsIgnoreCase("on")?"CHECKED":"")+">"+font+"Qualify Only</B></FONT></I>&nbsp;");
			str.append("<INPUT TYPE=CHECKBOX NAME=CABSCR"+(i+1)+" "+(((String)theclasses.elementAt(i,5)).equalsIgnoreCase("on")?"CHECKED":"")+">"+font+"Secret</B></FONT></I>");
			if(!showParms)
				str.append("<INPUT TYPE=HIDDEN NAME=CABPRM"+(i+1)+" VALUE=\""+((String)theclasses.elementAt(i,6))+"\">");
			if(!showMasks)
				str.append("<INPUT TYPE=HIDDEN NAME=CABMSK"+(i+1)+" VALUE=\""+((String)theclasses.elementAt(i,8))+"\">");
			if(!showPreReqs)
				str.append("<INPUT TYPE=HIDDEN NAME=CABPRE"+(i+1)+" VALUE=\""+((String)theclasses.elementAt(i,7))+"\">");
			str.append("</TD>");
			str.append("</TR>");
			if(showParms)
				str.append("<TR><TD WIDTH=100% COLSPAN=5>"+sfont+"Parameters: "+efont+"<INPUT TYPE=TEXT NAME=CABPRM"+(i+1)+" VALUE=\""+((String)theclasses.elementAt(i,6))+"\" SIZE=50 MAXLENGTH=255></TD></TR>");
			if(showMasks)
				str.append("<TR><TD WIDTH=100% COLSPAN=5>"+sfont+"Extra Mask: "+efont+"<INPUT TYPE=TEXT NAME=CABMSK"+(i+1)+" VALUE=\""+((String)theclasses.elementAt(i,8))+"\" SIZE=50 MAXLENGTH=255></TD></TR>");
			if(showPreReqs)
				str.append("<TR><TD WIDTH=100% COLSPAN=5>"+sfont+"Pre-Reqs list: "+efont+"<INPUT TYPE=TEXT NAME=CABPRE"+(i+1)+" VALUE=\""+((String)theclasses.elementAt(i,7))+"\" SIZE=50 MAXLENGTH=255></TD></TR>");
		}
		str.append("<TR><TD WIDTH=40%>");
		str.append("<SELECT ONCHANGE=\"AddAffect(this);\" NAME=CABLES"+(theclasses.size()+1)+">");
		str.append("<OPTION SELECTED VALUE=\"\">Select an Ability");
		for(final Enumeration<Ability> a=CMClass.abilities();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if(((A.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_ARCHON)
			&&(!CMSecurity.isASysOp(mob)))
				continue;
			final String ID=A.ID();
			if(!used.contains(ID))
				str.append("<OPTION VALUE=\""+ID+"\">"+A.Name());
		}
		str.append("</SELECT>");
		str.append("</TD>");
		str.append("<TD WIDTH=10%>");
		str.append("<INPUT TYPE=TEXT NAME=CABLVL"+(theclasses.size()+1)+" VALUE=\"\" SIZE=2 MAXLENGTH=3>");
		str.append("</TD>");
		str.append("<TD WIDTH=10%>");
		str.append("<INPUT TYPE=TEXT NAME=CABPOF"+(theclasses.size()+1)+" VALUE=\"\" SIZE=2 MAXLENGTH=3>"+font+"%</B></I></FONT>");
		str.append("</TD>");
		str.append("<TD WIDTH=10%>");
		str.append("<INPUT TYPE=TEXT NAME=CABMPOF"+(theclasses.size()+1)+" VALUE=\"\" SIZE=2 MAXLENGTH=3>"+font+"%</B></I></FONT>");
		str.append("</TD>");
		str.append("<TD WIDTH=30%>");
		str.append("<INPUT TYPE=CHECKBOX NAME=CABQUA"+(theclasses.size()+1)+" >"+font+"Qualify Only</B></I></FONT>&nbsp;");
		str.append("<INPUT TYPE=CHECKBOX NAME=CABSCR"+(theclasses.size()+1)+" >"+font+"Secret</B></I></FONT>");
		str.append("</TD>");
		str.append("</TR>");
		str.append("<TR><TD WIDTH=100% COLSPAN=5>"+sfont+"Parameters: "+efont+"<INPUT TYPE=TEXT NAME=CABPRM"+(theclasses.size()+1)+" VALUE=\"\" SIZE=50 MAXLENGTH=255></TD></TR>");
		str.append("<TR><TD WIDTH=100% COLSPAN=5>"+sfont+"Extra Mask: "+efont+"<INPUT TYPE=TEXT NAME=CABMSK"+(theclasses.size()+1)+" VALUE=\"\" SIZE=50 MAXLENGTH=255></TD></TR>");
		str.append("<TR><TD WIDTH=100% COLSPAN=5>"+sfont+"Pre-Reqs list: "+efont+"<INPUT TYPE=TEXT NAME=CABPRE"+(theclasses.size()+1)+" VALUE=\"\" SIZE=50 MAXLENGTH=255></TD></TR>");
		str.append("</TABLE>");
		return str;
	}

	// parameters include help, playable, max stats, pracs, trains, hitpoints,
	// mana, movement, attack, weapons, armorlimits, limits, bonuses,
	// prime, quals, startingeq
	@Override
	public String runMacro(HTTPRequest httpReq, String parm, HTTPResponse httpResp)
	{
		final java.util.Map<String,String> parms=parseParms(parm);
		final MOB mob=Authenticate.getAuthenticatedMob(httpReq);
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

		String last=httpReq.getUrlParameter("CLASS");
		if(last==null)
			return " @break@";
		if(last.length()>0)
		{
			if(parms.containsKey("ISGENERIC"))
			{
				final CharClass C2=CMClass.getCharClass(last);
				return ""+((C2!=null)&&(C2.isGeneric()));
			}

			final String newClassID=httpReq.getUrlParameter("NEWCLASS");
			CharClass C=(CharClass)httpReq.getRequestObjects().get("CLASS-"+last);
			if((C==null)
			&&(newClassID!=null)
			&&(newClassID.length()>0)
			&&(CMClass.getCharClass(newClassID)==null))
			{
				C=(CharClass)CMClass.getCharClass("GenCharClass").copyOf();
				C.setClassParms("<CCLASS><ID>"+newClassID+"</ID><NAME>"+CMStrings.capitalizeAndLower(newClassID)+"</NAME></CCLASS>");
				last=newClassID;
				httpReq.addFakeUrlParameter("CLASS",newClassID);
			}
			if(C==null)
				C=CMClass.getCharClass(last);
			if(parms.containsKey("ISNEWCLASS"))
				return ""+(CMClass.getCharClass(last)==null);
			if(C!=null)
			{
				final CharClass origC = C;
				final StringBuffer str=new StringBuffer("");
				if(parms.containsKey("NAME"))
				{
					String old=httpReq.getUrlParameter("NAME");
					if(old==null)
						old=C.name();
					str.append(old+", ");
				}
				if(parms.containsKey("NAMELIST"))
				{
					for(int c=0;c<C.nameSet().length;c++)
						str.append(C.nameSet()[c]+", ");
				}
				if(parms.containsKey("NAMES"))
				{
					final String old=httpReq.getUrlParameter("NAME1");
					final DVector nameSet=new DVector(2);
					int numNames=0;
					boolean cSrc=false;
					if(old==null)
					{
						C=C.makeGenCharClass();
						numNames=CMath.s_int(C.getStat("NUMNAME"));
						cSrc=true;
					}
					else
					{
						while(httpReq.isUrlParameter("NAME"+(numNames+1)))
							numNames++;

					}
					if(numNames<=0)
						nameSet.addElement(Integer.valueOf(0),C.name());
					else
					for(int i=0;i<numNames;i++)
					{
						final String lvlStr=cSrc?C.getStat("NAMELEVEL"+i):httpReq.getUrlParameter("NAMELEVEL"+(i+1));
						if(CMath.isInteger(lvlStr))
						{
							final int minLevel = CMath.s_int(lvlStr);
							final String name=cSrc?C.getStat("NAME"+i):httpReq.getUrlParameter("NAME"+(i+1));
							if((name!=null)&&(name.length()>0))
							{
								if(nameSet.size()==0)
									nameSet.addElement(Integer.valueOf(minLevel),name);
								else
								{
									boolean added=false;
									for(int n=0;n<nameSet.size();n++)
									{
										if(minLevel<((Integer)nameSet.elementAt(n,1)).intValue())
										{
											nameSet.insertElementAt(n,Integer.valueOf(minLevel),name);
											added=true;
											break;
										}
										else
										if(minLevel==((Integer)nameSet.elementAt(n,1)).intValue())
										{
											added=true;
											break;
										}
									}
									if(!added)
										nameSet.addElement(Integer.valueOf(minLevel),name);
								}
							}
						}
					}
					if(nameSet.size()==0)
						nameSet.addElement(Integer.valueOf(0),C.name());
					else
						nameSet.setElementAt(0,1,Integer.valueOf(0));
					final int borderSize=1;
					str.append("<TABLE WIDTH=100% BORDER=\""+borderSize+"\" CELLSPACING=0 CELLPADDING=0>");
					final String sfont=(parms.containsKey("FONT"))?("<FONT "+(parms.get("FONT"))+">"):"";
					final String efont=(parms.containsKey("FONT"))?"</FONT>":"";
					if(parms.containsKey("HEADERCOL1")||parms.containsKey("HEADERCOL2"))
					{
						str.append("<TR><TD WIDTH=20%>");
						if(parms.containsKey("HEADERCOL1"))
							str.append(sfont + (parms.get("HEADERCOL1")) + efont);
						str.append("</TD><TD WIDTH=80%>");
						if(parms.containsKey("HEADERCOL2"))
							str.append(sfont + (parms.get("HEADERCOL2")) + efont);
						str.append("</TD></TR>");
					}
					for(int i=0;i<nameSet.size();i++)
					{
						final Integer lvl=(Integer)nameSet.elementAt(i,1);
						final String name=(String)nameSet.elementAt(i,2);
						str.append("<TR><TD WIDTH=20%>");
						str.append("<INPUT TYPE=TEXT SIZE=5 NAME=NAMELEVEL"+(i+1)+" VALUE=\""+lvl.toString()+"\">");
						str.append("</TD><TD WIDTH=80%>");
						str.append("<INPUT TYPE=TEXT SIZE=30 NAME=NAME"+(i+1)+" VALUE=\""+name+"\">");
						str.append("</TD></TR>");
					}
					str.append("<TR><TD WIDTH=25%>");
					str.append("<INPUT TYPE=TEXT SIZE=5 NAME=NAMELEVEL"+(nameSet.size()+1)+" VALUE=\"\">");
					str.append("</TD><TD WIDTH=50%>");
					str.append("<INPUT TYPE=TEXT SIZE=30 NAME=NAME"+(nameSet.size()+1)+" VALUE=\"\">");
					str.append("</TD></TR>");
					str.append("</TABLE>");
					str.append(", ");
				}
				if(parms.containsKey("BASE"))
				{
					String old=httpReq.getUrlParameter("BASE");
					if(old==null)
						old=C.baseClass();
					else
					{
						CharClass pC=CMClass.getCharClass(old);
						if(pC==null)
							pC=CMClass.findCharClass(old);
						if(pC!=null)
							old=pC.ID();
					}
					str.append(old+", ");
				}
				if(parms.containsKey("HITPOINTSFORMULA"))
				{
					String old=httpReq.getUrlParameter("HITPOINTSFORMULA");
					if(old==null)
						old=""+C.getHitPointsFormula();
					str.append(old+", ");
				}
				if(parms.containsKey("MANAFORMULA"))
				{
					String old=httpReq.getUrlParameter("MANAFORMULA");
					if(old==null)
						old=""+C.getManaFormula();
					str.append(old+", ");
				}
				if(parms.containsKey("LVLPRAC"))
				{
					String old=httpReq.getUrlParameter("LVLPRAC");
					if(old==null)
						old=""+C.getBonusPracLevel();
					if(CMath.s_int(old)<=0)
						old="0";
					str.append(old+", ");
				}
				if(parms.containsKey("LVLATT"))
				{
					String old=httpReq.getUrlParameter("LVLATT");
					if(old==null)
						old=""+C.getBonusAttackLevel();
					if(CMath.s_int(old)<0)
						old="0";
					str.append(old+", ");
				}
				if(parms.containsKey("ATTATT"))
				{
					String old=httpReq.getUrlParameter("ATTATT");
					if(old==null)
						old=""+C.getAttackAttribute();
					if(CMath.s_int(old)<0)
						old="0";
					for(final int i : CharStats.CODES.BASECODES())
						str.append("<OPTION VALUE=\""+i+"\""+((CMath.s_int(old)==i)?" SELECTED":"")+">"+CMStrings.capitalizeAndLower(CharStats.CODES.DESC(i)));
					str.append(", ");
				}
				if(parms.containsKey("FSTTRAN"))
				{
					String old=httpReq.getUrlParameter("FSTTRAN");
					if(old==null)
						old=""+C.getTrainsFirstLevel();
					if(CMath.s_int(old)<0)
						old="0";
					str.append(old+", ");
				}
				if(parms.containsKey("FSTPRAC"))
				{
					String old=httpReq.getUrlParameter("FSTPRAC");
					if(old==null)
						old=""+C.getPracsFirstLevel();
					if(CMath.s_int(old)<0)
						old="0";
					str.append(old+", ");
				}
				if(parms.containsKey("MAXNCS"))
				{
					String old=httpReq.getUrlParameter("MAXNCS");
					if(old==null)
						old=""+C.maxNonCraftingSkills();
					if(CMath.s_int(old)<=0)
						old="Unlimited";
					str.append(old+", ");
				}
				if(parms.containsKey("MAXCRS"))
				{
					String old=httpReq.getUrlParameter("MAXCRS");
					if(old==null)
						old=""+C.maxCraftingSkills();
					if(CMath.s_int(old)<=0)
						old="Unlimited";
					str.append(old+", ");
				}
				if(parms.containsKey("MONEY"))
				{
					String old=httpReq.getUrlParameter("MONEY");
					if(old==null)
						old=C.getStartingMoney();
					str.append(old+", ");
				}
				if(parms.containsKey("MAXCMS"))
				{
					String old=httpReq.getUrlParameter("MAXCMS");
					if(old==null)
						old=""+C.maxCommonSkills();
					if(CMath.s_int(old)<=0)
						old="Unlimited";
					str.append(old+", ");
				}
				if(parms.containsKey("MAXLGS"))
				{
					String old=httpReq.getUrlParameter("MAXLGS");
					if(old==null)
						old=""+C.maxLanguages();
					if(CMath.s_int(old)<=0)
						old="Unlimited";
					str.append(old+", ");
				}
				if(parms.containsKey("LVLDAM"))
				{
					String old=httpReq.getUrlParameter("LVLDAM");
					if(old==null)
						old=""+C.getLevelsPerBonusDamage();
					if(CMath.s_int(old)<=0)
						old="1";
					str.append(old+", ");
				}
				if(parms.containsKey("LEVELCAP"))
				{
					String old=httpReq.getUrlParameter("LEVELCAP");
					if(old==null)
						old=""+C.getLevelCap();
					if(CMath.s_int(old)<0)
						old="-1";
					str.append(old+", ");
				}
				if(parms.containsKey("MOVEMENTFORMULA"))
				{
					String old=httpReq.getUrlParameter("MOVEMENTFORMULA");
					if(old==null)
						old=""+C.getMovementFormula();
					str.append(old+", ");
				}
				if(parms.containsKey("RACQUAL"))
				{
					String old=httpReq.getUrlParameter("RACQUAL");
					if(old==null)
						old=""+CMParms.toListString(C.getRequiredRaceList());
					str.append(old+", ");
				}
				if(parms.containsKey("GENHELP"))
				{
					String old=httpReq.getUrlParameter("GENHELP");
					if(old==null)
					{
						C=C.makeGenCharClass();
						old=C.getStat("HELP");
					}
					str.append(old+", ");
				}
				if(parms.containsKey("ARMOR"))
				{
					String old=httpReq.getUrlParameter("ARMOR");
					if(old==null)
					{
						C=C.makeGenCharClass();
						old=""+C.getStat("ARMOR");
					}
					if(CMath.s_int(old)<=0)
						old="0";
					for(int i=0;i<CharClass.ARMOR_LONGDESC.length;i++)
						str.append("<OPTION VALUE=\""+i+"\""+((CMath.s_int(old)==i)?" SELECTED":"")+">"+CMStrings.capitalizeAndLower(CharClass.ARMOR_LONGDESC[i]));
					str.append(", ");
				}
				if(parms.containsKey("SUBRUL"))
				{
					String old=httpReq.getUrlParameter("SUBRUL");
					if(old==null)
					{
						C=C.makeGenCharClass();
						old=""+C.getStat("SUBRUL");
					}
					for(final CharClass.SubClassRule rule : CharClass.SubClassRule.values())
						str.append("<OPTION VALUE=\""+rule.toString()+"\""+((rule.toString().equalsIgnoreCase(old))?" SELECTED":"")+">"+CMStrings.capitalizeAndLower(rule.toString()));
					str.append(", ");
				}
				if(parms.containsKey("STRLMT"))
				{
					String old=httpReq.getUrlParameter("STRLMT");
					if(old==null)
						old=""+C.getOtherLimitsDesc();
					str.append(old+", ");
				}
				if(parms.containsKey("STRBON"))
				{
					String old=httpReq.getUrlParameter("STRBON");
					if(old==null)
						old=""+C.getOtherBonusDesc();
					str.append(old+", ");
				}
				if(parms.containsKey("QUAL"))
				{
					String old=httpReq.getUrlParameter("QUAL");
					if(old==null)
					{
						C=C.makeGenCharClass();
						old=""+C.getStat("QUAL");
					}
					str.append(old+", ");
				}
				if(parms.containsKey("PLAYER"))
				{
					final String old=httpReq.getUrlParameter("PLAYER");
					long mask=0;
					if(old==null)
						mask=C.availabilityCode();
					else
						mask|=CMath.s_long(old);
					for(int i=0;i<Area.THEME_PHRASE_EXT.length;i++)
						str.append("<OPTION VALUE="+i+" "+((i==mask)?"SELECTED":"")+">"+Area.THEME_PHRASE_EXT[i]);
					str.append(", ");
				}
				if(parms.containsKey("ESTATS")||parms.containsKey("CSTATS")||parms.containsKey("ASTATS")||parms.containsKey("ASTATE")||parms.containsKey("STARTASTATE"))
				{
					C=C.makeGenCharClass();

					if(parms.containsKey("ESTATS"))
					{
						final String eStats=C.getStat("ESTATS");
						final PhyStats adjPStats=(PhyStats)CMClass.getCommon("DefaultPhyStats"); adjPStats.setAllValues(0);
						if(eStats.length()>0)
						{
							CMLib.coffeeMaker().setPhyStats(adjPStats,eStats);
						}
						str.append(RaceData.estats(adjPStats,'E',httpReq,parms,0)+", ");
					}
					if(parms.containsKey("CSTATS"))
					{
						final CharStats setStats=(CharStats)CMClass.getCommon("DefaultCharStats"); setStats.setAllValues(0);
						final String cStats=C.getStat("CSTATS");
						if(cStats.length()>0)
						{
							CMLib.coffeeMaker().setCharStats(setStats,cStats);
						}
						str.append(RaceData.cstats(setStats,'S',httpReq,parms,0)+", ");
					}
					if(parms.containsKey("ASTATS"))
					{
						final CharStats adjStats=(CharStats)CMClass.getCommon("DefaultCharStats"); adjStats.setAllValues(0);
						final String cStats=C.getStat("ASTATS");
						if(cStats.length()>0)
						{
							CMLib.coffeeMaker().setCharStats(adjStats,cStats);
						}
						str.append(RaceData.cstats(adjStats,'A',httpReq,parms,0)+", ");
					}
					if(parms.containsKey("ASTATE"))
					{
						final CharState adjState=(CharState)CMClass.getCommon("DefaultCharState"); adjState.setAllValues(0);
						final String aState=C.getStat("ASTATE");
						if(aState.length()>0)
						{
							CMLib.coffeeMaker().setCharState(adjState,aState);
						}
						str.append(RaceData.cstate(adjState,'A',httpReq,parms,0)+", ");
					}
					if(parms.containsKey("STARTASTATE"))
					{
						final CharState startAdjState=(CharState)CMClass.getCommon("DefaultCharState"); startAdjState.setAllValues(0);
						final String saState=C.getStat("STARTASTATE");
						if(saState.length()>0)
						{
							CMLib.coffeeMaker().setCharState(startAdjState,saState);
						}
						str.append(RaceData.cstate(startAdjState,'S',httpReq,parms,0)+", ");
					}
				}
				if(parms.containsKey("NOWEAPS"))
				{
					final String old=httpReq.getUrlParameter("NOWEAPS");
					List<String> set=null;
					if(old==null)
					{
						C=C.makeGenCharClass();
						final String weapList=C.getStat("GETWEP");
						set=CMParms.parseCommas(weapList,true);
					}
					else
					{
						String id="";
						set=new Vector<String>();
						for(int i=0;httpReq.isUrlParameter("NOWEAPS"+id);id=""+(++i))
							set.add(httpReq.getUrlParameter("NOWEAPS"+id));
					}
					for(int i=0;i<Weapon.CLASS_DESCS.length;i++)
					{
						str.append("<OPTION VALUE="+i);
						if(set.contains(""+i))
							str.append(" SELECTED");
						str.append(">"+Weapon.CLASS_DESCS[i]);
					}
					str.append(", ");
				}
				if(parms.containsKey("MINSTAT"))
				{
					final List<Pair<String,Integer>> minStats=new LinkedList<Pair<String,Integer>>();
					final String old=httpReq.getUrlParameter("MINSTAT0");
					if(old==null)
					{
						for(final Pair<String,Integer> P : C.getMinimumStatRequirements())
							minStats.add(P);
					}
					else
					{
						int x=0;
						while(httpReq.getUrlParameter("MINSTAT"+x)!=null)
						{
							final String minStat=httpReq.getUrlParameter("MINSTAT"+x);
							final String statMin=httpReq.getUrlParameter("STATMIN"+x);
							if((minStat!=null)&&(minStat.length()>0)&&(CMath.isInteger(statMin)))
								minStats.add(new Pair<String,Integer>(minStat,Integer.valueOf(CMath.s_int(statMin))));
							x++;
						}
					}
					for(int p=0;p<minStats.size();p++)
					{
						final Pair<String,Integer> P=minStats.get(p);
						str.append("<SELECT NAME=MINSTAT").append(p).append(">");
						str.append("<OPTION VALUE=\"\">Delete");
						str.append("<OPTION SELECTED VALUE=\"").append(P.first).append("\">"+P.first);
						str.append("</SELECT> Min Value: <INPUT NAME=STATMIN").append(p).append(" VALUE=").append(P.second.toString()).append(">");
						str.append("<BR>");
					}
					str.append("<SELECT NAME=MINSTAT").append(minStats.size()).append(">");
					for(final String statName : CharStats.CODES.BASENAMES())
						str.append("<OPTION VALUE=\"").append(CMStrings.capitalizeAndLower(statName)).append("\">").append(CMStrings.capitalizeAndLower(statName));
					str.append("</SELECT> Min Value: <INPUT NAME=STATMIN").append(minStats.size()).append(" VALUE=\"\">");
					str.append("<INPUT TYPE=BUTTON NAME=ADDSTATMIN VALUE=Add ONCLICK=\"ReShow();\">");
				}
				if(parms.containsKey("OUTFIT"))
					str.append(RaceData.itemList(C.outfit(null),'O',httpReq,parms,0,false)+", ");
				if(parms.containsKey("DISFLAGS"))
				{
					if(!httpReq.isUrlParameter("DISFLAGS"))
					{
						C=C.makeGenCharClass();
						httpReq.addFakeUrlParameter("DISFLAGS",C.getStat("DISFLAGS"));
					}
					final int flags=CMath.s_int(httpReq.getUrlParameter("DISFLAGS"));
					for(int i=0;i<CharClass.GENFLAG_DESCS.length;i++)
					{
						str.append("<OPTION VALUE="+CMath.pow(2,i));
						if(CMath.bset(flags,CMath.pow(2,i)))
							str.append(" SELECTED");
						str.append(">"+CharClass.GENFLAG_DESCS[i]);
					}
					str.append(", ");
				}
				if(parms.containsKey("SECURITYSETS"))
				{
					final String old=httpReq.getUrlParameter("SSET1");
					final DVector sSet=new DVector(2);
					int numSSet=0;
					boolean cSrc=false;
					if(old==null)
					{
						C=C.makeGenCharClass();
						numSSet=CMath.s_int(C.getStat("NUMSSET"));
						cSrc=true;
					}
					else
					{
						while(httpReq.isUrlParameter("SSET"+(numSSet+1)))
							numSSet++;

					}
					for(int i=0;i<numSSet;i++)
					{
						final String lvlStr=cSrc?C.getStat("SSETLEVEL"+i):httpReq.getUrlParameter("SSETLEVEL"+(i+1));
						if(CMath.isInteger(lvlStr))
						{
							final int minLevel = CMath.s_int(lvlStr);
							String sec = null;
							if(cSrc)
							{
								sec=C.getStat("SSET"+i);
								final Vector<String> V=CMParms.parse(sec);
								sec=CMParms.combineWithX(V,",",0);
							}
							else
								sec=httpReq.getUrlParameter("SSET"+(i+1));
							if((sec!=null)&&(sec.trim().length()>0)&&(CMParms.parseCommas(sec,true).size()>0))
							{
								sec=CMParms.combineWithX(CMParms.parseCommas(sec.toUpperCase().trim(),true),",",0);
								if(sSet.size()==0)
									sSet.addElement(Integer.valueOf(minLevel),sec);
								else
								{
									boolean added=false;
									for(int n=0;n<sSet.size();n++)
									{
										if(minLevel<((Integer)sSet.elementAt(n,1)).intValue())
										{
											sSet.insertElementAt(n,Integer.valueOf(minLevel),sec);
											added=true;
											break;
										}
										else
										if(minLevel==((Integer)sSet.elementAt(n,1)).intValue())
										{
											added=true;
											break;
										}
									}
									if(!added)
										sSet.addElement(Integer.valueOf(minLevel),sec);
								}
							}
						}
					}
					final int borderSize=1;
					str.append("<TABLE WIDTH=100% BORDER=\""+borderSize+"\" CELLSPACING=0 CELLPADDING=0>");
					final String sfont=(parms.containsKey("FONT"))?("<FONT "+(parms.get("FONT"))+">"):"";
					final String efont=(parms.containsKey("FONT"))?"</FONT>":"";
					if(parms.containsKey("HEADERCOL1")||parms.containsKey("HEADERCOL2"))
					{
						str.append("<TR><TD WIDTH=20%>");
						if(parms.containsKey("HEADERCOL1"))
							str.append(sfont + (parms.get("HEADERCOL1")) + efont);
						str.append("</TD><TD WIDTH=80%>");
						if(parms.containsKey("HEADERCOL2"))
							str.append(sfont + (parms.get("HEADERCOL2")) + efont);
						str.append("</TD></TR>");
					}
					for(int i=0;i<sSet.size();i++)
					{
						final Integer lvl=(Integer)sSet.elementAt(i,1);
						final String sec=(String)sSet.elementAt(i,2);
						str.append("<TR><TD WIDTH=20%>");
						str.append("<INPUT TYPE=TEXT SIZE=5 NAME=SSETLEVEL"+(i+1)+" VALUE=\""+lvl.toString()+"\">");
						str.append("</TD><TD WIDTH=80%>");
						str.append("<INPUT TYPE=TEXT SIZE=60 NAME=SSET"+(i+1)+" VALUE=\""+sec+"\">");
						str.append("</TD></TR>");
					}
					str.append("<TR><TD WIDTH=25%>");
					str.append("<INPUT TYPE=TEXT SIZE=5 NAME=SSETLEVEL"+(sSet.size()+1)+" VALUE=\"\">");
					str.append("</TD><TD WIDTH=50%>");
					str.append("<INPUT TYPE=TEXT SIZE=60 NAME=SSET"+(sSet.size()+1)+" VALUE=\"\">");
					str.append("</TD></TR>");
					str.append("</TABLE>");
					str.append(", ");
				}

				if(parms.containsKey("WEAPMATS"))
				{
					final String old=httpReq.getUrlParameter("WEAPMATS");
					List<String> set=null;
					if(old==null)
					{
						C=C.makeGenCharClass();
						final String matList=C.getStat("GETWMAT");
						set=CMParms.parseCommas(matList,true);
					}
					else
					{
						String id="";
						set=new Vector<String>();
						for(int i=0;httpReq.isUrlParameter("WEAPMATS"+id);id=""+(++i))
						{
							if(CMath.isInteger(httpReq.getUrlParameter("WEAPMATS"+id)))
								set.add(httpReq.getUrlParameter("WEAPMATS"+id));
						}
					}
					str.append("<OPTION VALUE=\"*\"");
					if(set.size()==0)
						str.append(" SELECTED");
					str.append(">ANY");
					for(final RawMaterial.Material m : RawMaterial.Material.values())
					{
						str.append("<OPTION VALUE="+m.mask());
						if(set.contains(""+m.mask()))
							str.append(" SELECTED");
						str.append(">"+m.noun());
					}
					str.append(", ");
				}
				if(parms.containsKey("ARMORMINOR"))
				{
					final String old=httpReq.getUrlParameter("ARMORMINOR");
					int armorMinor=-1;
					if(old==null)
					{
						C=C.makeGenCharClass();
						armorMinor=CMath.s_int(C.getStat("ARMORMINOR"));
					}
					else
						armorMinor=CMath.s_int(old);
					str.append("<OPTION VALUE=-1");
					if(armorMinor<0)
						str.append(" SELECTED");
					str.append(">N/A");
					for(int i=0;i<CMMsg.TYPE_DESCS.length;i++)
					{
						str.append("<OPTION VALUE="+i);
						if(i==armorMinor)
							str.append(" SELECTED");
						str.append(">"+CMMsg.TYPE_DESCS[i]);
					}
					str.append(", ");
				}
				if(parms.containsKey("STATCLASS"))
				{
					String old=httpReq.getUrlParameter("STATCLASS");
					if(old==null)
					{
						C=C.makeGenCharClass();
						old=""+C.getStat("STATCLASS");
					}
					str.append(classDropDown(old));
				}
				if(parms.containsKey("EVENTCLASS"))
				{
					String old=httpReq.getUrlParameter("EVENTCLASS");
					if(old==null)
					{
						C=C.makeGenCharClass();
						old=""+C.getStat("EVENTCLASS");
					}
					str.append(classDropDown(old));
				}
				if(parms.containsKey("CABILITIES"))
					str.append(cabilities(mob,C,httpReq,parms,1,"<FONT SIZE=-1 COLOR=WHITE>"));
				/******************************************************/
				// Here begins the displayable only fields.
				/******************************************************/
				if(parms.containsKey("HELP"))
				{
					StringBuilder s=CMLib.help().getHelpText(C.ID(),null,false,true);
					if(s==null)
						s=CMLib.help().getHelpText(C.name(),null,false,true);
					if(s!=null)
					{
						if(s.toString().startsWith("<CHARCLASS>"))
							s=new StringBuilder(s.toString().substring(11));
						int limit=78;
						if(parms.containsKey("LIMIT"))
							limit=CMath.s_int(parms.get("LIMIT"));
						str.append(helpHelp(s,limit));
					}
				}
				if(parms.containsKey("PLAYABLE"))
					str.append(Area.THEME_PHRASE_EXT[C.availabilityCode()]+", ");
				if(parms.containsKey("BASECLASS"))
					str.append(C.baseClass()+", ");
				if(parms.containsKey("MAXSTATS"))
					str.append(C.getMaxStatDesc()+", ");
				if(parms.containsKey("PRACS"))
					str.append(C.getPracticeDesc()+", ");
				if(parms.containsKey("TRAINS"))
					str.append(C.getTrainDesc()+", ");
				if(parms.containsKey("DAMAGE"))
					str.append(C.getDamageDesc()+", ");
				if(parms.containsKey("QUALDOMAINLIST"))
				{
					final Hashtable<String,Integer> domains=new Hashtable<String,Integer>();
					Ability A=null;
					String domain=null;
					for(final Enumeration<Ability> e=CMClass.abilities();e.hasMoreElements();)
					{
						A=e.nextElement();
						if(CMLib.ableMapper().getQualifyingLevel(C.ID(),true,A.ID())>0)
						{
							if((A.classificationCode()&Ability.ALL_DOMAINS)==0)
								domain=Ability.ACODE_DESCS[A.classificationCode()];
							else
								domain=Ability.DOMAIN_DESCS[(A.classificationCode()&Ability.ALL_DOMAINS)>>5];
							Integer I=domains.get(domain);
							if(I==null)
								I=Integer.valueOf(0);
							I=Integer.valueOf(I.intValue()+1);
							domains.remove(domain);
							domains.put(domain,I);
						}
					}
					Integer I=null;
					String winner=null;
					Integer winnerI=null;
					while(domains.size()>0)
					{
						winner=null;
						winnerI=null;
						for(final Enumeration<String> e=domains.keys();e.hasMoreElements();)
						{
							domain=e.nextElement();
							I=domains.get(domain);
							if((winnerI==null)||(I.intValue()>winnerI.intValue()))
							{
								winner=domain;
								winnerI=I;
							}
						}
						if(winnerI!=null)
							str.append(winner+"("+winnerI.intValue()+"), ");
						domains.remove(winner);
					}
				}

				if(parms.containsKey("HITPOINTS"))
					str.append(C.getHitPointDesc()+", ");
				if(parms.containsKey("MANA"))
					str.append(C.getManaDesc()+", ");
				if(parms.containsKey("MOVEMENT"))
					str.append(C.getMovementDesc()+", ");

				if(parms.containsKey("AVGHITPOINTS"))
				{
					final int sh=CMProps.getIntVar(CMProps.Int.STARTHP);
					if(parms.containsKey("AVGBASE"))
					{
						int num=0;
						final long[][] avgs=new long[3][3];
						for(final Enumeration<CharClass> c=CMClass.charClasses();c.hasMoreElements();)
						{
							final CharClass C1=c.nextElement();
							if(C1.baseClass().equals(C.baseClass()))
							{
								final int maxCon=18+C1.maxStatAdjustments()[CharStats.STAT_CONSTITUTION];
								final String f=C1.getHitPointsFormula();
								num++;
								avgs[0][0]+=avgMath(10,10,sh,f);
								avgs[0][1]+=avgMath(18,10,sh,f);
								avgs[0][2]+=avgMath(maxCon,10,sh,f);

								avgs[1][0]+=avgMath(10,50,sh,f);
								avgs[1][1]+=avgMath(18,50,sh,f);
								avgs[1][2]+=avgMath(maxCon,50,sh,f);

								avgs[2][0]+=avgMath(10,90,sh,f);
								avgs[2][1]+=avgMath(18,90,sh,f);
								avgs[2][2]+=avgMath(maxCon,90,sh,f);
							}
						}
						str.append("("+(avgs[0][0]/num)+"/"+(avgs[0][1]/num)+"/"+(avgs[0][2]/num)+") ");
						str.append("("+(avgs[1][0]/num)+"/"+(avgs[1][1]/num)+"/"+(avgs[1][2]/num)+") ");
						str.append("("+(avgs[2][0]/num)+"/"+(avgs[2][1]/num)+"/"+(avgs[2][2]/num)+") ");
					}
					else
					{
						final int maxCon=18+C.maxStatAdjustments()[CharStats.STAT_CONSTITUTION];
						final String f=C.getHitPointsFormula();
						str.append("("+avgMath(10,10,sh,f)+"/"+avgMath(18,10,sh,f)+"/"+avgMath(maxCon,10,sh,f)+") ");
						str.append("("+avgMath(10,50,sh,f)+"/"+avgMath(18,50,sh,f)+"/"+avgMath(maxCon,50,sh,f)+") ");
						str.append("("+avgMath(10,90,sh,f)+"/"+avgMath(18,90,sh,f)+"/"+avgMath(maxCon,90,sh,f)+") ");
					}
				}

				if(parms.containsKey("AVGMANA"))
				{
					final int sm=CMProps.getIntVar(CMProps.Int.STARTMANA);
					if(parms.containsKey("AVGBASE"))
					{
						int num=0;
						final long[][] avgs=new long[3][3];
						for(final Enumeration<CharClass> c=CMClass.charClasses();c.hasMoreElements();)
						{
							final CharClass C1=c.nextElement();
							if(C1.baseClass().equals(C.baseClass()))
							{
								final int maxInt=18+C1.maxStatAdjustments()[CharStats.STAT_INTELLIGENCE];
								final String f=C1.getManaFormula();
								num++;
								avgs[0][0]+=avgMath(10,10,sm,f);
								avgs[0][1]+=avgMath(18,10,sm,f);
								avgs[0][2]+=avgMath(maxInt,10,sm,f);

								avgs[1][0]+=avgMath(10,50,sm,f);
								avgs[1][1]+=avgMath(18,50,sm,f);
								avgs[1][2]+=avgMath(maxInt,50,sm,f);

								avgs[2][0]+=avgMath(10,90,sm,f);
								avgs[2][1]+=avgMath(18,90,sm,f);
								avgs[2][2]+=avgMath(maxInt,90,sm,f);
							}
						}
						str.append("("+(avgs[0][0]/num)+"/"+(avgs[0][1]/num)+"/"+(avgs[0][2]/num)+") ");
						str.append("("+(avgs[1][0]/num)+"/"+(avgs[1][1]/num)+"/"+(avgs[1][2]/num)+") ");
						str.append("("+(avgs[2][0]/num)+"/"+(avgs[2][1]/num)+"/"+(avgs[2][2]/num)+") ");
					}
					else
					{
						final int maxInt=18+C.maxStatAdjustments()[CharStats.STAT_INTELLIGENCE];
						final String f=C.getManaFormula();
						str.append("("+avgMath(10,10,sm,f)+"/"+avgMath(18,10,sm,f)+"/"+avgMath(maxInt,10,sm,f)+") ");
						str.append("("+avgMath(10,50,sm,f)+"/"+avgMath(18,50,sm,f)+"/"+avgMath(maxInt,50,sm,f)+") ");
						str.append("("+avgMath(10,90,sm,f)+"/"+avgMath(18,90,sm,f)+"/"+avgMath(maxInt,90,sm,f)+") ");
					}
				}
				if(parms.containsKey("AVGMOVEMENT"))
				{
					final int sm=CMProps.getIntVar(CMProps.Int.STARTMOVE);
					if(parms.containsKey("AVGBASE"))
					{
						int num=0;
						final long[][] avgs=new long[3][3];
						for(final Enumeration<CharClass> c=CMClass.charClasses();c.hasMoreElements();)
						{
							final CharClass C1=c.nextElement();
							if(C1.baseClass().equals(C.baseClass()))
							{
								final String f=C1.getMovementFormula();
								final int maxStrength=18+C1.maxStatAdjustments()[CharStats.STAT_STRENGTH];
								num++;
								avgs[0][0]+=avgMath(10,10,sm,f);
								avgs[0][1]+=avgMath(18,10,sm,f);
								avgs[0][2]+=avgMath(maxStrength,10,sm,f);

								avgs[1][0]+=avgMath(10,50,sm,f);
								avgs[1][1]+=avgMath(18,50,sm,f);
								avgs[1][2]+=avgMath(maxStrength,50,sm,f);

								avgs[2][0]+=avgMath(10,90,sm,f);
								avgs[2][1]+=avgMath(18,90,sm,f);
								avgs[2][2]+=avgMath(maxStrength,90,sm,f);
							}
						}
						str.append("("+(avgs[0][0]/num)+"/"+(avgs[0][1]/num)+"/"+(avgs[0][2]/num)+") ");
						str.append("("+(avgs[1][0]/num)+"/"+(avgs[1][1]/num)+"/"+(avgs[1][2]/num)+") ");
						str.append("("+(avgs[2][0]/num)+"/"+(avgs[2][1]/num)+"/"+(avgs[2][2]/num)+") ");
					}
					else
					{
						final String f=C.getMovementFormula();
						final int maxStrength=18+C.maxStatAdjustments()[CharStats.STAT_STRENGTH];
						str.append("("+avgMath(10,10,sm,f)+"/"+avgMath(18,10,sm,f)+"/"+avgMath(maxStrength,10,sm,f)+") ");
						str.append("("+avgMath(10,50,sm,f)+"/"+avgMath(18,50,sm,f)+"/"+avgMath(maxStrength,50,sm,f)+") ");
						str.append("("+avgMath(10,90,sm,f)+"/"+avgMath(18,90,sm,f)+"/"+avgMath(maxStrength,90,sm,f)+") ");
					}
				}

				if(parms.containsKey("PRIME"))
					str.append(C.getPrimeStatDesc()+", ");

				if(parms.containsKey("ATTACK"))
					str.append(C.getAttackDesc()+", ");

				if(parms.containsKey("WEAPONS"))
				{
					if(C.getWeaponLimitDesc().length()>0)
						str.append(C.getWeaponLimitDesc()+", ");
					else
						str.append("Any, ");
				}
				if(parms.containsKey("ARMORLIMITS"))
				{
					if(C.getArmorLimitDesc().length()>0)
						str.append(C.getArmorLimitDesc()+", ");
					else
						str.append("Any, ");
				}
				if(parms.containsKey("LIMITS"))
				{
					final StringBuffer limits = new StringBuffer("");
					if(C.getOtherLimitsDesc().length()>0)
						limits.append("  "+C.getOtherLimitsDesc());
					if(C.getLevelCap()>0)
						limits.append("  A player may not gain more than "+C.getLevelCap()+" levels in this class.");
					if(limits.length()==0)
						str.append("None, ");
					else
						str.append(limits.toString().trim()).append(", ");
				}
				if(parms.containsKey("BONUSES"))
				{
					if(C.getOtherBonusDesc().length()>0)
						str.append(C.getOtherBonusDesc()+", ");
					else
						str.append("None, ");
				}
				if(parms.containsKey("QUALS"))
				{
					if(C.getStatQualDesc().length()>0)
						str.append(C.getStatQualDesc()+", ");
				}
				if(parms.containsKey("STARTINGEQ"))
				{
					final List<Item> items=C.outfit(null);
					if(items !=null)
					{
						for(final Item I : items)
						{
							if(I!=null)
								str.append(I.name()+", ");
						}
					}
				}
				if(parms.containsKey("BALANCE"))
					str.append(balanceChart(C));
				String strstr=str.toString();
				if(strstr.endsWith(", "))
					strstr=strstr.substring(0,strstr.length()-2);
				if(C.isGeneric() && !origC.isGeneric())
					origC.initializeClass();
				return clearWebMacros(strstr);
			}
		}
		return "";
	}

	public String balanceChart(CharClass C)
	{
		final MOB M=CMClass.getFactoryMOB();
		M.basePhyStats().setLevel(1);
		M.baseCharStats().setCurrentClass(C);
		M.recoverCharStats();
		C.startCharacter(M,false,false);
		final HashSet<String> seenBefore=new HashSet<String>();
		int totalgained=0;
		int totalqualified=0;
		int uniqueClassSkills=0;
		final List<String> uniqueClassSkillsV=new LinkedList<String>();
		int uniqueClassSkillsGained=0;
		int uncommonClassSkills=0;
		final List<String> uncommonClassSkillsV=new LinkedList<String>();
		int uncommonClassSkillsGained=0;
		int totalCrossClassSkills=0;
		final List<String> totalCrossClassSkillsV=new LinkedList<String>();
		int totalCrossClassLevelDiffs=0;
		int maliciousSkills=0;
		int maliciousSkillsGained=0;
		int beneficialSkills=0;
		int beneficialSkillsGained=0;
		int levelCap=C.getLevelCap();
		if(levelCap < 0)
			levelCap=CMProps.getIntVar(CMProps.Int.LASTPLAYERLEVEL);
		for(int l=1;l<=levelCap;l++)
		{
			final List<String> set=CMLib.ableMapper().getLevelListings(C.ID(),true,l);
			for(int s=0;s<set.size();s++)
			{
				final String able=set.get(s);
				if(CMLib.ableMapper().getSecretSkill(C.ID(), true, able))
					continue;
				if(able.equalsIgnoreCase("Skill_Recall"))
					continue;
				if(able.equalsIgnoreCase("Skill_Write"))
					continue;
				if(able.equalsIgnoreCase("Skill_Swim"))
					continue;
				if(CMLib.ableMapper().getQualifyingLevel("All",true,able)==l)
					continue;
				if(seenBefore.contains(able))
					continue;
				final Ability A=CMClass.getAbility(able);
				seenBefore.add(able);
				int numOthers=0;
				int numOutsiders=0;
				int thisCrossClassLevelDiffs=0;
				int tlvl=0;
				for(final Enumeration<CharClass> c=CMClass.charClasses();c.hasMoreElements();)
				{
					final CharClass C2=c.nextElement();
					if(C2==C)
						continue;
					if(!CMProps.isTheme(C2.availabilityCode()))
						continue;
					if(C2.baseClass().equals(C.baseClass()))
					{
						tlvl=CMLib.ableMapper().getQualifyingLevel(C2.ID(),true,able);
						if((tlvl>0)&&(!CMLib.ableMapper().getSecretSkill(C2.ID(), true, able)))
						{
							if(tlvl>l)
								thisCrossClassLevelDiffs+=(tlvl-l);
							else
								thisCrossClassLevelDiffs+=(l-tlvl);
							numOthers++;
						}
					}
					else
					{
						tlvl=CMLib.ableMapper().getQualifyingLevel(C2.ID(),true,able);
						if((tlvl>0)&&(!CMLib.ableMapper().getSecretSkill(C2.ID(), true, able)))
						{
							if(tlvl>l)
								thisCrossClassLevelDiffs+=(tlvl-l);
							else
								thisCrossClassLevelDiffs+=(l-tlvl);
							numOutsiders++;
						}
					}
				}
				if((numOthers==0)&&(numOutsiders==0))
				{
					uniqueClassSkills++;
					if((A!=null)&&(A.flags()&(Ability.FLAG_HOLY+Ability.FLAG_UNHOLY))==(Ability.FLAG_HOLY+Ability.FLAG_UNHOLY))
						uniqueClassSkillsV.add(able+"("+l+")N");
					else
					if((A!=null)&&(A.flags()&(Ability.FLAG_HOLY+Ability.FLAG_UNHOLY))==(Ability.FLAG_HOLY))
						uniqueClassSkillsV.add(able+"("+l+")G");
					else
					if((A!=null)&&(A.flags()&(Ability.FLAG_HOLY+Ability.FLAG_UNHOLY))==(Ability.FLAG_UNHOLY))
						uniqueClassSkillsV.add(able+"("+l+")E");
					else
						uniqueClassSkillsV.add(able+"("+l+")");
				}
				else
				if(numOutsiders==0)
				{
					uncommonClassSkills++;
					if((A!=null)&&(A.flags()&(Ability.FLAG_HOLY+Ability.FLAG_UNHOLY))==(Ability.FLAG_HOLY+Ability.FLAG_UNHOLY))
						uncommonClassSkillsV.add(able+"("+l+")N");
					else
					if((A!=null)&&(A.flags()&(Ability.FLAG_HOLY+Ability.FLAG_UNHOLY))==(Ability.FLAG_HOLY))
						uncommonClassSkillsV.add(able+"("+l+")G");
					else
					if((A!=null)&&(A.flags()&(Ability.FLAG_HOLY+Ability.FLAG_UNHOLY))==(Ability.FLAG_UNHOLY))
						uncommonClassSkillsV.add(able+"("+l+")E");
					else
						uncommonClassSkillsV.add(able+"("+l+")");
				}
				else
				{
					totalCrossClassLevelDiffs+=(thisCrossClassLevelDiffs/numOutsiders);
					totalCrossClassSkills++;
					if((A!=null)&&(A.flags()&(Ability.FLAG_HOLY+Ability.FLAG_UNHOLY))==(Ability.FLAG_HOLY+Ability.FLAG_UNHOLY))
						totalCrossClassSkillsV.add(able+"("+l+")N");
					else
					if((A!=null)&&(A.flags()&(Ability.FLAG_HOLY+Ability.FLAG_UNHOLY))==(Ability.FLAG_HOLY))
						totalCrossClassSkillsV.add(able+"("+l+")G");
					else
					if((A!=null)&&(A.flags()&(Ability.FLAG_HOLY+Ability.FLAG_UNHOLY))==(Ability.FLAG_UNHOLY))
						totalCrossClassSkillsV.add(able+"("+l+")E");
					else
						totalCrossClassSkillsV.add(able+"("+l+")");
				}
				final boolean gained=(M.fetchAbility(able)!=null);
				if(gained)
				{
					totalgained++;
					if((numOthers==0)&&(numOutsiders==0))
					{
						uniqueClassSkillsGained++;
					}
					else
					if(numOutsiders==0)
					{
						uncommonClassSkillsGained++;
					}
				}
				else
					totalqualified++;
				if(A==null)
					continue;
				if((A.abstractQuality()==Ability.QUALITY_BENEFICIAL_OTHERS)
				   ||(A.abstractQuality()==Ability.QUALITY_BENEFICIAL_SELF))
				{
					beneficialSkills++;
					if(gained)
						beneficialSkillsGained++;
				}
				if(A.abstractQuality()==Ability.QUALITY_MALICIOUS)
				{
					maliciousSkills++;
					if(gained)
						maliciousSkillsGained++;
				}
			}
			CMLib.leveler().level(M);
		}
		final StringBuffer str=new StringBuffer("");
		str.append("<BR>Rule#1: Avg gained skill/level: "+CMath.div(Math.round(100.0*CMath.div(totalgained,30)),(long)100));
		str.append("<BR>Rule#2: Avg qualified skill/level: "+CMath.div(Math.round(100.0*CMath.div(totalqualified,30)),(long)100));
		str.append("<BR>Rule#3: Unique class skills gained: "+uniqueClassSkillsGained+"/"+uniqueClassSkills);
		str.append("<BR><FONT COLOR=WHITE>Rule#3</FONT>: Unique class skills: "+CMParms.toListString(uniqueClassSkillsV));
		str.append("<BR>Rule#4: Uncommon class skills gained: "+uncommonClassSkillsGained+"/"+uncommonClassSkills);
		str.append("<BR><FONT COLOR=WHITE>Rule#4</FONT>: Uncommon class skills: "+CMParms.toListString(uncommonClassSkillsV));
		str.append("<BR>Rule#5: Combat skills gained: "+(maliciousSkillsGained+beneficialSkillsGained)+"/"+(maliciousSkills+beneficialSkills));
		str.append("<BR>Rule#6: Avg Unique class skill/level: "+CMath.div(Math.round(100.0*CMath.div(uniqueClassSkills,30)),(long)100));
		str.append("<BR>Rule#7: CrossClass class skills diff: "+totalCrossClassLevelDiffs+"/"+totalCrossClassSkills);
		str.append("<BR><FONT COLOR=WHITE>Rule#7</FONT>: CrossClass class skills: "+CMParms.toListString(totalCrossClassSkillsV));
		str.append("<BR>Rule#8: Avg Cross class skill/level: "+CMath.div(Math.round(100.0*CMath.div(totalCrossClassSkills,30)),(long)100));
		M.destroy();
		return str.toString();
	}

	public int avgMath(int stat, int level, int add, String formula)
	{
		final double[] variables={
			level,
			stat,
			(double)stat+7,
			stat,
			(double)stat+7,
			stat,
			(double)stat+7,
			stat,
			stat
		};
		return add+(level*(int)Math.round(CMath.parseMathExpression(formula, variables)));
	}
}
