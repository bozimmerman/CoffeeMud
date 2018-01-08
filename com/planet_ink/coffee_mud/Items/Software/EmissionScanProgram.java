package com.planet_ink.coffee_mud.Items.Software;
import com.planet_ink.coffee_mud.Items.Basic.StdItem;
import com.planet_ink.coffee_mud.Items.BasicTech.GenElecItem;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.Technical.TechType;
import com.planet_ink.coffee_mud.Libraries.interfaces.GenericBuilder;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.*;

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
public class EmissionScanProgram extends GenSoftware
{
	@Override
	public String ID()
	{
		return "EmissionScanProgram";
	}

	protected final static short	AUTO_TICKDOWN		= 4;

	protected boolean				activated			= false;
	protected short					activatedTickdown	= AUTO_TICKDOWN;

	public EmissionScanProgram()
	{
		super();
		setName("a lifescan minidisk");
		setDisplayText("a minidisk sits here.");
		setDescription("Emissions software, for small computer/scanners, will locate electronic and wave emissions.");
		super.setCurrentScreenDisplay("EMISSIONSCAN: Activate for continual scanning, type for on-demand.\n\r");
		basePhyStats().setWeight(2); // the higher the weight, the wider the scan
		recoverPhyStats();
	}

	@Override
	public String getParentMenu()
	{
		return "";
	}

	@Override
	public String getInternalName()
	{
		return "";
	}

	public boolean isEmitting(Item I)
	{
		return ((I instanceof Electronics)&&(((Electronics)I).activated()));
	}

	public CMMsg getScanMsg(Room R)
	{
		return CMClass.getMsg(CMLib.map().getFactoryMOB(R), null, this, CMMsg.MASK_CNTRLMSG|CMMsg.MSG_SNIFF, null); // cntrlmsg is important
	}

	public void getDirDesc(String dirBuilder, StringBuilder str, boolean useShipDirs)
	{
		int numDone=0;
		int numTotal=0;
		for(int d=0;d<dirBuilder.length();d++)
			numTotal+=(Character.isLowerCase(dirBuilder.charAt(d))?1:0);
		if(dirBuilder.length()==0)
			str.append(" here");
		else
		for(int d=0;d<dirBuilder.length();d++)
		{
			if(dirBuilder.charAt(d)=='S')
			{
				if(numDone==0)
					str.append(L(" inside a ship"));
				else
				if(numDone<numTotal-1)
					str.append(L(", inside a ship"));
				else
					str.append(L(", and then inside a ship"));
				numDone++;
				continue;
			}
			String locDesc="";
			if(dirBuilder.charAt(d)=='D')
			{
				locDesc="behind a door ";
				d++;
			}
			if(dirBuilder.charAt(d)=='I')
			{
				locDesc="inside a room ";
				d++;
			}
			if(dirBuilder.charAt(d)=='o')
			{
				locDesc="outdoors ";
				d++;
			}
			final int dir=dirBuilder.charAt(d)-'a';
			if(numDone==0)
				str.append(" ").append(locDesc).append(useShipDirs?CMLib.directions().getShipDirectionName(dir):CMLib.directions().getDirectionName(dir));
			else
			if(numDone<numTotal-1)
				str.append(", ").append(locDesc).append(useShipDirs?CMLib.directions().getShipDirectionName(dir):CMLib.directions().getDirectionName(dir));
			else
				str.append(", and then ").append(locDesc).append(useShipDirs?CMLib.directions().getShipInDirectionName(dir):CMLib.directions().getInDirectionName(dir));
			numDone++;
		}
	}

	public int getScanMsg(MOB viewerM, Room R, Set<Room> roomsDone, String dirBuilder, int depthLeft, CMMsg scanMsg, StringBuilder str)
	{
		if((R==null)||(roomsDone.contains(R)))
			return 0;
		roomsDone.add(R);
		int numFound=0;
		final boolean useShipDirs=(R instanceof BoardableShip)||(R.getArea() instanceof BoardableShip);
		for(int m=0;m<R.numInhabitants();m++)
		{
			final MOB M=R.fetchInhabitant(m);
			if(M!=null)
			for(int i=0;i<M.numItems();i++)
			{
				final Item I=M.getItem(i);
				if(isEmitting(I))
				{
					scanMsg.setTarget(I);
					if(R.okMessage(scanMsg.source(), scanMsg))
					{
						numFound++;
						if(roomsDone.size()==1)
						{
							if(CMLib.flags().canBeSeenBy(M, viewerM))
								str.append(L("Something on @x1",M.name(viewerM)));
							else
								str.append(L("Something on someone"));
						}
						else
							str.append("Something");
						getDirDesc(dirBuilder, str, useShipDirs);
						str.append(".\n\r");
						break;
					}
				}
			}
		}
		for(int i=0;i<R.numItems();i++)
		{
			final Item I=R.getItem(i);
			if(isEmitting(I))
			{
				scanMsg.setTarget(I);
				if(R.okMessage(scanMsg.source(), scanMsg))
				{
					numFound++;
					if(roomsDone.size()==1)
					{
						final Item C=I.ultimateContainer(null);
						if((C!=null)&&(C!=I))
						{
							if(CMLib.flags().canBeSeenBy(C, viewerM))
								str.append(L("Something in @x1",C.name(viewerM)));
							else
								str.append(L("Something inside something else"));
						}
						else
						if(CMLib.flags().canBeSeenBy(I, viewerM))
							str.append(I.name(viewerM));
						else
							str.append("Something");
					}
					else
						str.append("Something");
					getDirDesc(dirBuilder, str, useShipDirs);
					str.append(".\n\r");
				}
			}
			if((I instanceof SpaceShip)&&(depthLeft>0))
			{
				Room shipR=null;
				for(final Enumeration<Room> r=((SpaceShip)I).getShipArea().getProperMap(); r.hasMoreElements(); )
				{
					final Room R2=r.nextElement();
					for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
					{
						if(R2.getRoomInDir(d)==R)
						{
							final Exit E2=R2.getExitInDir(d);
							if(E2==null)
								continue;
							shipR=R2;
							break;
						}
					}
					if(shipR!=null)
						break;
				}
				if(shipR!=null)
					numFound+=getScanMsg(viewerM,shipR,roomsDone, dirBuilder+'S', depthLeft-1, scanMsg, str);
			}
		}
		if(depthLeft>0)
		{
			final boolean isIndoors=(R.domainType()&Room.INDOORS)==Room.INDOORS;
			for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
			{
				final Room R2=R.getRoomInDir(d);
				final Exit E2=R.getExitInDir(d);
				if((R2==null)||(E2==null))
					continue;
				final boolean willIndoors=(R.domainType()&Room.INDOORS)==Room.INDOORS;
				final boolean willADoor=E2.hasADoor() && !E2.isOpen();
				final String dirBCode=willADoor?"D":
								(isIndoors && (!willIndoors))?"O":
								(!isIndoors && (willIndoors))?"I":
								"";
				numFound+=getScanMsg(viewerM,R2, roomsDone, dirBuilder+dirBCode+((char)('a'+d)), depthLeft-1, scanMsg, str);
			}
		}
		return numFound;
	}

	public String getScanMsg(MOB viewerM)
	{
		final Room R=CMLib.map().roomLocation(this);
		if(R==null)
			return "";
		final StringBuilder str=new StringBuilder("");
		final int numFound=getScanMsg(viewerM, R,new HashSet<Room>(), "",phyStats().weight()+1,getScanMsg(R),str);
		if(activated)
			super.setCurrentScreenDisplay("EMISSIONSCAN: Activated: "+numFound+" found last scan.\n\r");
		else
			super.setCurrentScreenDisplay("EMISSIONSCAN: Activate for continual scanning, type for on-demand.\n\r");
		if(str.length()==0)
			return "No emissions detected.";
		return str.toString().toLowerCase();
	}

	@Override
	public boolean isActivationString(String word)
	{
		return "emissionscan".startsWith(CMLib.english().getFirstWord(word.toLowerCase()));
	}

	@Override
	public boolean isDeActivationString(String word)
	{
		return "emissionscan".startsWith(CMLib.english().getFirstWord(word.toLowerCase()));
	}

	@Override
	public boolean isCommandString(String word, boolean isActive)
	{
		return "emissionscan".startsWith(CMLib.english().getFirstWord(word.toLowerCase()));
	}

	@Override
	public String getActivationMenu()
	{
		return super.getActivationMenu();
	}

	@Override
	public boolean checkActivate(MOB mob, String message)
	{
		return super.checkActivate(mob, message);
	}

	@Override
	public boolean checkDeactivate(MOB mob, String message)
	{
		return super.checkDeactivate(mob, message);
	}

	@Override
	public boolean checkTyping(MOB mob, String message)
	{
		return super.checkTyping(mob, message);
	}

	@Override
	public boolean checkPowerCurrent(int value)
	{
		return super.checkPowerCurrent(value);
	}

	@Override
	public void onActivate(MOB mob, String message)
	{
		super.onActivate(mob, message);
		this.activated=true;
		activatedTickdown=AUTO_TICKDOWN;
		//TODO: emissionscan for particular items? Is that a special version of emissionscan?
		final String scan=getScanMsg(mob);
		if(scan.length()>0)
			super.addScreenMessage(scan);
	}

	@Override
	public void onDeactivate(MOB mob, String message)
	{
		super.onDeactivate(mob, message);
		if(activated)
			super.addScreenMessage("Emission scanning deactivated.");
		this.activated=false;
	}

	@Override
	public void onTyping(MOB mob, String message)
	{
		super.onTyping(mob, message);
		final String scan=getScanMsg(mob);
		if(scan.length()>0)
			super.addScreenMessage(scan);
	}

	@Override
	public void onPowerCurrent(int value)
	{
		super.onPowerCurrent(value);
		if((value != 0)&&(activated)&&(--activatedTickdown>=0)) // means there was power to give, 2 means is active menu, which doesn't apply
		{
			final MOB M=(owner() instanceof MOB)?((MOB)owner()):null;
			final String scan=getScanMsg(M);
			if(scan.length()>0)
				super.addScreenMessage(scan);
		}
	}
}
