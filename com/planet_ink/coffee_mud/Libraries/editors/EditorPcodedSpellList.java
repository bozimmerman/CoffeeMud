package com.planet_ink.coffee_mud.Libraries.editors;
import com.planet_ink.coffee_web.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityMapper.AbilityMapping;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityParameters.*;
import com.planet_ink.coffee_mud.core.exceptions.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.AbilityComponent.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.RawMaterial.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
   Copyright 2008-2025 Bo Zimmerman

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
public class EditorPcodedSpellList extends AbilityParmEditorImpl
{
	public EditorPcodedSpellList()
	{
		super("PCODED_SPELL_LIST",CMLib.lang().L("Spell Affects"),ParmType.SPECIAL);
	}

	@Override
	public void createChoices()
	{
	}

	@Override
	public int maxColWidth()
	{
		return 20;
	}

	@Override
	public int appliesToClass(final Object o)
	{
		if(o instanceof String)
		{
			final String chk=((String)o).toUpperCase();
			if(chk.equalsIgnoreCase("WALL")
			||chk.equalsIgnoreCase("DEMOLISH")
			||chk.equalsIgnoreCase("TITLE")
			||chk.equalsIgnoreCase("DESC"))
				return -1;
			final Pair<String[],String[]> codeFlags = getBuildingCodesNFlags();
			if(CMParms.contains(codeFlags.first, chk))
				return 1;
		}
		return -1;
	}

	@Override
	public boolean confirmValue(final String oldVal)
	{
		if(oldVal.trim().length()==0)
			return true;
		final String[] spells = CMParms.parseAny(oldVal.trim(), ')', true).toArray(new String[0]);
		for(String spell : spells)
		{
			final int x=spell.indexOf('(');
			if(x>0)
				spell=spell.substring(0,x);
			if(spell.trim().length()==0)
				continue;
			if((CMClass.getAbility(spell)==null)&&(CMClass.getBehavior(spell)==null))
				return false;
		}
		return true;
	}

	@Override
	public String convertFromItem(final ItemCraftor A, final Item I)
	{
		return "";
	}

	@Override
	public String defaultValue()
	{
		return "";
	}

	public String rebuild(final List<CMObject> spells) throws CMException
	{
		final StringBuffer newVal = new StringBuffer("");
		for(int s=0;s<spells.size();s++)
		{
			final String txt;
			if(spells.get(s) instanceof Ability)
				txt = ((Ability)spells.get(s)).text().trim();
			else
			if(spells.get(s) instanceof Behavior)
				txt = ((Behavior)spells.get(s)).getParms().trim();
			else
				continue;
			newVal.append(spells.get(s).ID()).append("(").append(txt).append(")");
		}
		return newVal.toString();
	}

	@Override
	public String[] fakeUserInput(final String oldVal)
	{
		final Vector<String> V = new Vector<String>();
		final String[] spells = CMParms.parseAny(oldVal.trim(), ')', true).toArray(new String[0]);
		for(String spell : spells)
		{
			final int x=spell.indexOf('(');
			String parms="";
			if(x>0)
			{
				parms=spell.substring(x+1).trim();
				spell=spell.substring(0,x);
			}
			if(spell.trim().length()==0)
				continue;
			if((CMClass.getAbility(spell)!=null)
			||(CMClass.getBehavior(spell)!=null))
			{
				V.add(spell);
				V.add(parms);
			}
		}
		return CMParms.toStringArray(V);
	}

	public List<CMObject> getCodedSpells(final String oldVal)
	{
		final String[] spellStrs = this.fakeUserInput(oldVal);
		final List<CMObject> spells=new ArrayList<CMObject>(spellStrs.length/2);
		for(int s=0;s<spellStrs.length;s+=2)
		{
			final Ability A=CMClass.getAbility(spellStrs[s]);
			if(A!=null)
			{
				if(spellStrs[s+1].length()>0)
					A.setMiscText(spellStrs[s+1]);
				spells.add(A);
			}
			else
			{
				final Behavior B=CMClass.getBehavior(spellStrs[s]);
				if(spellStrs[s+1].length()>0)
					B.setParms(spellStrs[s+1]);
				spells.add(B);
			}
		}
		return spells;
	}

	@Override
	public String webValue(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final String oldVal, final String fieldName)
	{
		List<CMObject> spells=null;
		if(httpReq.isUrlParameter(fieldName+"_AFFECT1"))
		{
			spells = new Vector<CMObject>();
			int num=1;
			String behav=httpReq.getUrlParameter(fieldName+"_AFFECT"+num);
			String theparm=httpReq.getUrlParameter(fieldName+"_ADATA"+num);
			while((behav!=null)&&(theparm!=null))
			{
				if(behav.length()>0)
				{
					final Ability A=CMClass.getAbility(behav);
					if(A!=null)
					{
						if(theparm.trim().length()>0)
							A.setMiscText(theparm);
						spells.add(A);
					}
					else
					{
						final Behavior B=CMClass.getBehavior(behav);
						if(theparm.trim().length()>0)
							B.setParms(theparm);
						spells.add(B);
					}
				}
				num++;
				behav=httpReq.getUrlParameter(fieldName+"_AFFECT"+num);
				theparm=httpReq.getUrlParameter(fieldName+"_ADATA"+num);
			}
		}
		else
		{
			spells = getCodedSpells(oldVal);
		}
		try
		{
			return rebuild(spells);
		}
		catch(final Exception e)
		{
			return oldVal;
		}
	}

	@Override
	public String webField(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final String oldVal, final String fieldName)
	{
		final List<CMObject> spells=getCodedSpells(webValue(httpReq,parms,oldVal,fieldName));
		final StringBuffer str = new StringBuffer("");
		str.append("<TABLE WIDTH=100% BORDER=\"1\" CELLSPACING=0 CELLPADDING=0>");
		for(int i=0;i<spells.size();i++)
		{
			final CMObject A=spells.get(i);
			str.append("<TR><TD WIDTH=50%>");
			str.append("\n\r<SELECT ONCHANGE=\"EditAffect(this);\" NAME="+fieldName+"_AFFECT"+(i+1)+">");
			str.append("<OPTION VALUE=\"\">Delete!");
			str.append("<OPTION VALUE=\""+A.ID()+"\" SELECTED>"+A.ID());
			str.append("</SELECT>");
			str.append("</TD><TD WIDTH=50%>");
			final String theparm;
			if(A instanceof Ability)
				theparm=CMStrings.replaceAll(((Ability)A).text(),"\"","&quot;");
			else
			if(A instanceof Behavior)
				theparm=CMStrings.replaceAll(((Behavior)A).getParms(),"\"","&quot;");
			else
				continue;
			str.append("\n\r<INPUT TYPE=TEXT SIZE=30 NAME="+fieldName+"_ADATA"+(i+1)+" VALUE=\""+theparm+"\">");
			str.append("</TD></TR>");
		}
		str.append("<TR><TD WIDTH=50%>");
		str.append("\n\r<SELECT ONCHANGE=\"AddAffect(this);\" NAME="+fieldName+"_AFFECT"+(spells.size()+1)+">");
		str.append("<OPTION SELECTED VALUE=\"\">Select Effect/Behavior");
		for(final Enumeration<Ability> a=CMClass.abilities();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_ARCHON)
				continue;
			final String cnam=A.ID();
			str.append("<OPTION VALUE=\""+cnam+"\">"+cnam);
		}
		for(final Enumeration<Behavior> a=CMClass.behaviors();a.hasMoreElements();)
		{
			final Behavior A=a.nextElement();
			final String cnam=A.ID();
			str.append("<OPTION VALUE=\""+cnam+"\">"+cnam);
		}
		str.append("</SELECT>");
		str.append("</TD><TD WIDTH=50%>");
		str.append("\n\r<INPUT TYPE=TEXT SIZE=30 NAME="+fieldName+"_ADATA"+(spells.size()+1)+" VALUE=\"\">");
		str.append("</TD></TR>");
		str.append("</TABLE>");
		return str.toString();
	}

	@Override
	public String commandLinePrompt(final MOB mob, final String oldVal, final int[] showNumber, final int showFlag) throws java.io.IOException
	{
		final List<CMObject> spells=getCodedSpells(oldVal);
		final StringBuffer rawCheck = new StringBuffer("");
		for(int s=0;s<spells.size();s++)
		{
			rawCheck.append(spells.get(s).ID()).append(";");
			if(spells.get(s) instanceof Ability)
				rawCheck.append(((Ability)spells.get(s)).text()).append(";");
			else
			if(spells.get(s) instanceof Behavior)
				rawCheck.append(((Behavior)spells.get(s)).getParms()).append(";");
			else
				rawCheck.append(";");
		}
		boolean okToProceed = true;
		++showNumber[0];
		String newVal = null;
		while(okToProceed)
		{
			okToProceed = false;
			CMLib.genEd().spellsOrBehaviors(mob,spells,showNumber[0],showFlag,true);
			final StringBuffer sameCheck = new StringBuffer("");
			for(int s=0;s<spells.size();s++)
			{
				if(spells.get(s) instanceof Ability)
					rawCheck.append(((Ability)spells.get(s)).text()).append(";");
				else
				if(spells.get(s) instanceof Behavior)
					rawCheck.append(((Behavior)spells.get(s)).getParms()).append(";");
				else
					rawCheck.append(";");
			}
			if(sameCheck.toString().equals(rawCheck.toString()))
				return oldVal;
			try
			{
				newVal = rebuild(spells);
			}
			catch(final CMException e)
			{
				mob.tell(e.getMessage());
				okToProceed = true;
				break;
			}
		}
		return (newVal==null)?oldVal:newVal.toString();
	}
}
