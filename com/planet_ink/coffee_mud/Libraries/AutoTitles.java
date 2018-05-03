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

import java.util.*;

/*
   Copyright 2008-2018 Bo Zimmerman

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
public class AutoTitles extends StdLibrary implements AutoTitlesLibrary
{
	@Override
	public String ID()
	{
		return "AutoTitles";
	}

	private TriadSVector<String,String,MaskingLibrary.CompiledZMask> autoTitles=null;

	private static final String titleFilename = "titles.ini";
	
	private String getTitleFilename()
	{
		CMFile F = new CMFile(Resources.makeFileResourceName(titleFilename),null);
		if(F.exists() && (F.canRead()))
			return titleFilename;
		final String oldFilename = titleFilename.substring(0,titleFilename.length()-4)+".txt"; 
		F = new CMFile(Resources.makeFileResourceName(oldFilename),null);
		if(F.exists() && (F.canRead()))
			return oldFilename;
		return titleFilename;
	}

	@Override
	public String evaluateAutoTitle(String row, boolean addIfPossible)
	{
		if(row.trim().startsWith("#")||row.trim().startsWith(";")||(row.trim().length()==0))
			return null;
		int x=row.indexOf('=');
		while((x>=1)&&(row.charAt(x-1)=='\\'))
			x=row.indexOf('=',x+1);
		if(x<0)
			return "Error: Invalid line! Not comment, whitespace, and does not contain an = sign!";
		final String title=row.substring(0,x).trim();
		final String mask=row.substring(x+1).trim();

		if(title.length()==0)
			return "Error: Blank title: "+title+"="+mask+"!";
		if(mask.length()==0)
			return "Error: Blank mask: "+title+"="+mask+"!";
		if(addIfPossible)
		{
			if(autoTitles==null)
				reloadAutoTitles();
			for(final Triad<String,String,MaskingLibrary.CompiledZMask> triad : autoTitles)
			{
				if(triad.first.equalsIgnoreCase(title))
					return "Error: Duplicate title: "+title+"="+mask+"!";
			}
			autoTitles.add(new Triad<String,String,MaskingLibrary.CompiledZMask>(title,mask,CMLib.masking().maskCompile(mask)));
		}
		return null;
	}

	@Override
	public boolean isExistingAutoTitle(String title)
	{
		if(autoTitles==null)
			reloadAutoTitles();
		title=title.trim();
		for(final Triad<String,String,MaskingLibrary.CompiledZMask> triad : autoTitles)
		{
			if(triad.first.equalsIgnoreCase(title))
				return true;
		}
		return false;
	}

	@Override
	public Enumeration<String> autoTitles()
	{
		if(autoTitles==null)
			reloadAutoTitles();
		return autoTitles.firstElements();
	}

	@Override
	public String getAutoTitleMask(String title)
	{
		if(autoTitles==null)
			reloadAutoTitles();
		for(final Triad<String,String,MaskingLibrary.CompiledZMask> triad : autoTitles)
		{
			if(triad.first.equalsIgnoreCase(title))
				return triad.second;
		}
		return "";
	}

	@Override
	public boolean evaluateAutoTitles(MOB mob)
	{
		if(mob==null)
			return false;
		final PlayerStats P=mob.playerStats();
		if(P==null)
			return false;
		if(autoTitles==null)
			reloadAutoTitles();
		String title=null;
		MaskingLibrary.CompiledZMask mask=null;
		int pdex=0;
		final List<String> ptV=P.getTitles();
		boolean somethingDone=false;
		synchronized(ptV)
		{
			for(final Triad<String,String,MaskingLibrary.CompiledZMask> triad : autoTitles)
			{
				mask=triad.third;
				title=triad.first;
				pdex=ptV.indexOf(title);
				if(pdex<0)
				{
					final String fixedTitle = CMStrings.removeColors(title).replace('\'', '`');
					for(int p=ptV.size()-1;p>=0;p--)
					{
						try
						{
							final String tit=CMStrings.removeColors(ptV.get(p)).replace('\'', '`');
							if(tit.equalsIgnoreCase(fixedTitle))
							{
								pdex=p;
								break;
							}
						}
						catch(final java.lang.IndexOutOfBoundsException ioe)
						{
						}
					}
				}

				if(CMLib.masking().maskCheck(mask,mob,true))
				{
					if(pdex<0)
					{
						if(ptV.size()>0)
							ptV.add(0,title);
						else
							ptV.add(title);
						somethingDone=true;
					}
				}
				else
				if(pdex>=0)
				{
					somethingDone=true;
					ptV.remove(pdex);
				}
			}
		}
		return somethingDone;
	}

	protected void dispossesTitle(String title)
	{
		final List<String> list=CMLib.database().getUserList();
		final String fixedTitle = CMStrings.removeColors(title).replace('\'', '`');
		for(final String playerName : list)
		{
			final MOB M=CMLib.players().getLoadPlayer(playerName);
			if(M.playerStats()!=null)
			{
				final List<String> ptV=M.playerStats().getTitles();
				synchronized(ptV)
				{
					int pdex=ptV.indexOf(title);
					if(pdex<0)
					{
						for(int p=ptV.size()-1;p>=0;p--)
						{
							try
							{
								final String tit=CMStrings.removeColors(ptV.get(p)).replace('\'', '`');
								if(tit.equalsIgnoreCase(fixedTitle))
								{
									pdex=p;
									break;
								}
							}
							catch(final java.lang.IndexOutOfBoundsException ioe)
							{
							}
						}
					}
					if(pdex>=0)
					{
						ptV.remove(pdex);
						if(!CMLib.flags().isInTheGame(M,true))
							CMLib.database().DBUpdatePlayerPlayerStats(M);
					}
				}
			}
		}
	}
	
	@Override
	public void appendAutoTitle(String text)
	{
		Resources.removeResource(this.getTitleFilename());
		final CMFile F=new CMFile(Resources.makeFileResourceName(titleFilename),null,CMFile.FLAG_LOGERRORS);
		F.saveText(text,true);
		reloadAutoTitles();
	}

	@Override
	public String deleteTitleAndResave(String title)
	{
		dispossesTitle(title);
		final CMFile F=new CMFile(Resources.makeFileResourceName(titleFilename),null,CMFile.FLAG_LOGERRORS);
		if(F.exists())
		{
			final boolean removed=Resources.findRemoveProperty(F, title);
			if(removed)
			{
				Resources.removeResource(titleFilename);
				reloadAutoTitles();
				return null;
			}
			return "Unable to delete title!";
		}
		return "Unable to open "+titleFilename+"!";
	}

	@Override
	public void reloadAutoTitles()
	{
		autoTitles=new TriadSVector<String,String,MaskingLibrary.CompiledZMask>();
		final List<String> V=Resources.getFileLineVector(Resources.getFileResource(this.getTitleFilename(),true));
		String WKID=null;
		for(int v=0;v<V.size();v++)
		{
			final String row=V.get(v);
			WKID=evaluateAutoTitle(row,true);
			if(WKID==null)
				continue;
			if(WKID.startsWith("Error: "))
				Log.errOut("CharCreation",WKID);
		}
		for(final Enumeration<MOB> e=CMLib.players().players();e.hasMoreElements();)
		{
			final MOB M=e.nextElement();
			if(M.playerStats()!=null)
			{
				if((evaluateAutoTitles(M))&&(!CMLib.flags().isInTheGame(M,true)))
					CMLib.database().DBUpdatePlayerPlayerStats(M);
			}
		}
	}
	
	@Override
	public String getAutoTitleInstructions()
	{
		final StringBuffer buf=new CMFile(Resources.makeFileResourceName(this.getTitleFilename()),null,CMFile.FLAG_LOGERRORS).text();
		final StringBuffer inst=new StringBuffer("");
		List<String> V=new Vector<String>();
		if(buf!=null)
			V=Resources.getFileLineVector(buf);
		for(int v=0;v<V.size();v++)
		{
			if(V.get(v).startsWith("#"))
				inst.append(V.get(v).substring(1)+"\n\r");
			else
			if(V.get(v).length()>0)
				break;
		}
		return inst.toString();
	}

}
