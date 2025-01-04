package com.planet_ink.coffee_mud.Commands;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.*;

/*
   Copyright 2004-2025 Bo Zimmerman

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
public class Transfer extends At
{
	public Transfer()
	{
	}

	private final String[]	access	= I(new String[] { "TRANSFER" });

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	private String getComResponse(final PrintWriter writer, final BufferedReader reader) throws IOException
	{
		writer.flush();
		final long timeout=System.currentTimeMillis()+(30*1000);
		while((!reader.ready())&&(System.currentTimeMillis()<timeout))
			CMLib.s_sleep(1000);
		if(System.currentTimeMillis()>timeout)
			throw new IOException("Communication failure");
		String s="";
		while(reader.ready())
			s=reader.readLine();
		return s;
	}

	protected Coord3D fixSpaceCoords(final List<Physical> xferObjV,
									 final long distanceDm,
									 Coord3D targetSpace)
	{
		long baseDist = 1;
		for(final Environmental e : xferObjV)
		{
			if((e instanceof SpaceObject)
			&&(((SpaceObject)e).radius()>baseDist))
				baseDist=((SpaceObject)e).radius();
		}
		boolean somethingDone = true;
		final Random rand = CMLib.dice().getRandomizer();
		while(somethingDone)
		{
			somethingDone = false;
			for(final SpaceObject sO : CMLib.space().getSpaceObjectsByCenterpointWithin(targetSpace, 0, SpaceObject.Distance.AstroUnit.dm))
			{
				final long minDistance = baseDist+sO.radius()+distanceDm;
				while(CMLib.space().getDistanceFrom(sO.coordinates(), targetSpace) < minDistance)
				{
					final long distanceDiff = minDistance - CMLib.space().getDistanceFrom(sO.coordinates(), targetSpace);
					somethingDone=true;
					final Dir3D randomDir = new Dir3D(new double[] { rand.nextDouble() * Math.PI * 2.0, rand.nextDouble() * Math.PI });
					targetSpace = CMLib.space().moveSpaceObject(targetSpace, randomDir, distanceDiff);
				}
			}
		}
		return targetSpace;
	}

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		Room targetRoom=null;
		Coord3D targetSpace=null;
		long distanceDm = 1000;
		if(commands.size()<3)
		{
			mob.tell(L("Transfer whom where? Try all or a mob name or item followed by item name, followerd by a Room ID, target player name, inventory, area name, or room text!"));
			return false;
		}
		final List<String> origCommands = new XVector<String>(commands);
		commands.remove(0);
		String searchName=commands.get(0);
		final Room curRoom=mob.location();
		final Vector<Physical> xferObjV=new Vector<Physical>();
		StringBuffer cmd = new StringBuffer(CMParms.combine(commands,1));
		boolean allFlag=false;
		if(searchName.equalsIgnoreCase("ALL"))
		{
			allFlag=true;
			cmd = new StringBuffer(CMParms.combine(commands,1));
			if(commands.size()>2)
			{
				commands.remove(0);
				searchName=commands.get(0);
			}
			else
				searchName="";
		}
		boolean itemFlag=false;
		if((searchName.equalsIgnoreCase("item")||(searchName.equalsIgnoreCase("items"))))
		{
			itemFlag=true;
			cmd = new StringBuffer(CMParms.combine(commands,1));
			if(commands.size()>2)
			{
				commands.remove(0);
				searchName=commands.get(0);
			}
			else
				searchName="";
		}
		if((searchName.length()==0)&&(allFlag))
		{
			if(itemFlag)
			{
				for(int i=0;i<curRoom.numItems();i++)
					xferObjV.add(curRoom.getItem(i));
			}
			else
			for(int i=0;i<curRoom.numInhabitants();i++)
			{
				final MOB M=curRoom.fetchInhabitant(i);
				if(M!=null)
					xferObjV.add(M);
			}
		}
		else
		if(itemFlag)
		{
			if(!allFlag)
			{
				final Environmental E=curRoom.fetchFromMOBRoomFavorsItems(mob,null,searchName,Wearable.FILTER_UNWORNONLY);
				if(E instanceof Item)
					xferObjV.add((Item)E);
			}
			if((searchName.length()>0)
			&&(!cmd.toString().equals("here"))
			&&(!cmd.toString().equals(".")))
			{
				for(int i=0;i<curRoom.numItems();i++)
				{
					final Item I=curRoom.getItem(i);
					if((I!=null)&&(CMLib.english().containsString(I.name(),searchName)))
						xferObjV.add(I);
				}
			}
			if(xferObjV.size()==0)
			{
				for(final Enumeration<Room> r=curRoom.getArea().getProperMap();r.hasMoreElements();)
				{
					final Room R=r.nextElement();
					Item I=null;
					int num=1;
					while((num<=1)||(I!=null))
					{
						I=R.findItem(searchName+"."+num);
						if((I!=null)&&(!xferObjV.contains(I)))
							xferObjV.add(I);
						num++;
						if((!allFlag)&&(xferObjV.size()>0))
							break;
					}
					if((!allFlag)&&(xferObjV.size()>0))
						break;
				}
			}
			if(xferObjV.size()==0)
			{
				try
				{
					for(final Enumeration<Room> r=CMLib.map().rooms();r.hasMoreElements();)
					{
						final Room R=r.nextElement();
						Item I=null;
						int num=1;
						while((num<=1)||(I!=null))
						{
							I=R.findItem(searchName+"."+num);
							if((I!=null)&&(!xferObjV.contains(I)))
								xferObjV.add(I);
							num++;
							if((!allFlag)&&(xferObjV.size()>0))
								break;
						}
						if((!allFlag)&&(xferObjV.size()>0))
							break;
					}
				}
				catch (final NoSuchElementException nse)
				{
				}
			}
		}
		else
		{
			if(!allFlag)
			{
				final MOB M=CMLib.sessions().findCharacterOnline(searchName,true);
				if(M!=null)
					xferObjV.add(M);
			}
			if(xferObjV.size()==0)
			{
				final MOB M=curRoom.fetchInhabitant(searchName);
				if(M!=null)
					xferObjV.add(M);
			}
			if(xferObjV.size()==0)
			{
				for(final Enumeration<Room> r=curRoom.getArea().getProperMap();r.hasMoreElements();)
				{
					final Room R=r.nextElement();
					MOB M=null;
					int num=1;
					while((num<=1)||(M!=null))
					{
						M=R.fetchInhabitant(searchName+"."+num);
						if((M!=null)&&(!xferObjV.contains(M)))
							xferObjV.add(M);
						num++;
						if((!allFlag)&&(xferObjV.size()>0))
							break;
					}
					if((!allFlag)&&(xferObjV.size()>0))
						break;
				}
			}
			if(xferObjV.size()==0)
			{
				try
				{
					for(final Enumeration<Room> r=CMLib.map().rooms();r.hasMoreElements();)
					{
						final Room R=r.nextElement();
						MOB M=null;
						int num=1;
						while((num<=1)||(M!=null))
						{
							M=R.fetchInhabitant(searchName+"."+num);
							if((M!=null)&&(!xferObjV.contains(M)))
								xferObjV.add(M);
							num++;
							if((!allFlag)&&(xferObjV.size()>0))
								break;
						}
						if((!allFlag)&&(xferObjV.size()>0))
							break;
					}
				}
				catch (final NoSuchElementException nse)
				{
				}
			}
			if((!allFlag)&&(xferObjV.size()==0))
			{
				final MOB M=CMLib.players().getLoadPlayer(searchName);
				if(M!=null)
					xferObjV.add(M);
			}
		}

		if(xferObjV.size()==0)
		{
			try
			{
				final ConvertingEnumeration<SpaceObject,Item> spaceItems=new ConvertingEnumeration<SpaceObject,Item>(
					new FilteredEnumeration<SpaceObject>(CMLib.space().getSpaceObjects(),new Filterer<SpaceObject>()
					{
						@Override
						public boolean passesFilter(final SpaceObject obj)
						{
							return obj instanceof Item;
						}
					}),
					new Converter<SpaceObject,Item>()
					{
						@Override
						public Item convert(final SpaceObject obj)
						{
							return (Item)obj;
						}
					}
				);
				final Environmental E=CMLib.english().fetchEnvironmental(spaceItems, searchName, true);
				if(E instanceof Item)
					xferObjV.add((Item)E);
			}
			catch (final NoSuchElementException nse)
			{
			}
		}
		if(xferObjV.size()==0)
		{
			try
			{
				final ConvertingEnumeration<SpaceObject,Item> spaceItems=new ConvertingEnumeration<SpaceObject,Item>(
					new FilteredEnumeration<SpaceObject>(CMLib.space().getSpaceObjects(),new Filterer<SpaceObject>()
					{
						@Override
						public boolean passesFilter(final SpaceObject obj)
						{
							return obj instanceof Item;
						}
					}),
					new Converter<SpaceObject,Item>()
					{
						@Override
						public Item convert(final SpaceObject obj)
						{
							return (Item)obj;
						}
					}
				);
				final Environmental E=CMLib.english().fetchEnvironmental(spaceItems, searchName, false);
				if(E instanceof Item)
					xferObjV.add((Item)E);
			}
			catch (final NoSuchElementException nse)
			{
			}
		}
		if(xferObjV.size()==0)
		{
			try
			{
				final ConvertingEnumeration<Boardable,Item> shipItems=new ConvertingEnumeration<Boardable,Item>(
					new FilteredEnumeration<Boardable>(CMLib.map().ships(),new Filterer<Boardable>()
					{
						@Override
						public boolean passesFilter(final Boardable obj)
						{
							return obj instanceof Item;
						}
					}),
					new Converter<Boardable,Item>()
					{
						@Override
						public Item convert(final Boardable obj)
						{
							return (Item)obj;
						}
					}
				);
				final Environmental E=CMLib.english().fetchEnvironmental(shipItems, searchName, false);
				if(E instanceof Item)
					xferObjV.add((Item)E);
			}
			catch (final NoSuchElementException nse)
			{
			}
		}
		if(xferObjV.size()==0)
		{
			if((!itemFlag)&&(commands.size()>1))
			{
				origCommands.add(1, "ITEM");
				return this.execute(mob, origCommands, metaFlags);
			}
			mob.tell(L("Transfer what?  '@x1' is unknown to you.",searchName));
			return false;
		}

		boolean inventoryFlag=false;
		if(cmd.toString().equalsIgnoreCase("inventory"))
		{
			targetRoom=curRoom;
			inventoryFlag=true;
		}
		else
		if(cmd.toString().equalsIgnoreCase("here")||cmd.toString().equalsIgnoreCase("."))
			targetRoom=curRoom;
		else
		if(cmd.toString().indexOf('@')>0)
		{
			final String foreignThing=cmd.toString().substring(0,cmd.toString().lastIndexOf('@'));
			String server=cmd.toString().substring(cmd.toString().lastIndexOf('@')+1);
			int port;
			try
			{
				port = CMath.s_int(CMLib.host().executeCommand("GET CM1SERVER PORT"));
			}
			catch (final Exception e)
			{
				Log.errOut(e);
				return false;
			}
			final int ddex=server.indexOf('$');
			final int ddex2=(ddex<0)?-1:server.indexOf('$',ddex+1);
			if((ddex<0)||(ddex2<0))
			{
				mob.tell(L("Server format:  @user$pass$server:port"));
				return false;
			}
			final String user=server.substring(0,ddex);
			final String pass=server.substring(ddex+1,ddex2);
			server=server.substring(ddex2+1);
			if(port<=0)
				port=27733;
			final int pdex=server.lastIndexOf(':');
			if(pdex>0)
			{
				port=CMath.s_int(server.substring(pdex+1));
				server=server.substring(0,pdex);
			}
			final java.net.Socket sock=new java.net.Socket(server,port);
			try
			{
				final PrintWriter writer=new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
				final BufferedReader reader=new BufferedReader(new InputStreamReader(sock.getInputStream()));
				mob.tell(getComResponse(writer,reader));
				writer.write("LOGIN "+user+" "+pass+"\n\r");
				mob.tell(getComResponse(writer,reader));
				writer.write("TARGET "+foreignThing+"\n\r");
				mob.tell(getComResponse(writer,reader));
				for(int i=0;i<xferObjV.size();i++)
				{
					if(xferObjV.get(i) instanceof Item)
					{
						final Item I=(Item)xferObjV.get(i);
						final Room itemRoom=CMLib.map().roomLocation(I);
						if((CMSecurity.isAllowed(mob, itemRoom, CMSecurity.SecFlag.TRANSFER))
						&&(CMSecurity.isAllowed(mob, targetRoom, CMSecurity.SecFlag.TRANSFER)))
						{
							writer.write("BLOCK\n\r");
							final String s=getComResponse(writer,reader);
							mob.tell(s);
							String blockEnd;
							if(s.startsWith("[OK ")&&(s.endsWith("]")))
							{
								blockEnd=s.substring(4,s.length()-1);
								final String itemXML=CMLib.coffeeMaker().getItemXML(I);
								writer.write("IMPORT <ITEMS>"+itemXML.trim()+"</ITEMS>"+blockEnd+"\n");
								mob.tell(getComResponse(writer,reader));
							}
							else
								mob.tell(L("Communication failure."));
						}
					}
					else
					if(xferObjV.get(i) instanceof MOB)
					{
						final MOB M=(MOB)xferObjV.get(i);
						final Room mobRoom=CMLib.map().roomLocation(M);
						if((mobRoom!=null)
						&&(CMSecurity.isAllowed(mob, mobRoom, CMSecurity.SecFlag.TRANSFER))
						&&(CMSecurity.isAllowed(mob, targetRoom, CMSecurity.SecFlag.TRANSFER)))
						{
							writer.write("BLOCK\n\r");
							final String s=getComResponse(writer,reader);
							mob.tell(s);
							String blockEnd;
							if(s.startsWith("[OK ")&&(s.endsWith("]")))
							{
								blockEnd=s.substring(4,s.length()-1);
								final String mobXML=CMLib.coffeeMaker().getMobXML(M);
								writer.write("IMPORT <MOBS>"+mobXML.trim()+"</MOBS>"+blockEnd+"\n");
								mob.tell(getComResponse(writer,reader));
							}
							else
								mob.tell(L("Communication failure."));
						}
					}
				}
				if(mob.playerStats().getTranPoofOut().length()==0)
					mob.tell(L("Done."));
				writer.write("QUIT\n\r");
				CMLib.s_sleep(500);
				return true;
			}
			catch(final IOException e)
			{
				mob.tell(e.getMessage());
			}
			finally
			{
				sock.close();
			}
		}
		else
		if(cmd.toString().toLowerCase().startsWith("space:"))
		{
			String rest = cmd.toString().substring(6).trim();
			final List<String> rV=CMParms.parseSpaces(rest,true);
			if(rV.size()>1)
			{
				final String last=rV.remove(rV.size()-1);
				final BigDecimal distl = CMLib.english().parseSpaceDistance(last);
				if(distl != null)
				{
					distanceDm = distl.longValue();
					rest = CMParms.combine(rV);
				}
			}
			final int restx=rest.indexOf("->");
			if(rest.length()>0)
			{
				if(restx>0)
				{
					final String o1s = rest.substring(0,restx);
					final String o2s = rest.substring(restx+2);
					SpaceObject o1 = CMLib.space().findSpaceObject(o1s, true);
					if(o1 == null)
						o1 = CMLib.space().findSpaceObject(o1s, false);
					SpaceObject o2 = CMLib.space().findSpaceObject(o2s, true);
					if(o2 == null)
						o2 = CMLib.space().findSpaceObject(o2s, false);
					if((o1 != null)&&(o2 != null))
					{
						final Dir3D dir = CMLib.space().getDirection(o1.coordinates(), o2.coordinates());
						final Dir3D opDir = CMLib.space().getOppositeDir(dir);
						final Coord3D o1coords = o1.coordinates().copyOf();
						targetSpace = CMLib.space().moveSpaceObject(o1coords, opDir, o1.radius()+distanceDm).copyOf();
					}
				}
				else
				if(Character.isDigit(rest.charAt(0)))
				{
					final List<String> bits=CMParms.parseAny(rest,',',true);
					if(bits.size()==3)
						targetSpace=new Coord3D(new long[] { CMath.s_long(bits.get(0)), CMath.s_long(bits.get(1)), CMath.s_long(bits.get(2))});
				}
				else
				{
					SpaceObject o = CMLib.space().findSpaceObject(rest, true);
					if(o == null)
						o = CMLib.space().findSpaceObject(rest, false);
					if(o != null)
						targetSpace=o.coordinates().copyOf();
				}
			}
			if(targetSpace != null)
				targetSpace=fixSpaceCoords(xferObjV, distanceDm, targetSpace);
		}
		else
		if(CMLib.sessions().findCharacterOnline(cmd.toString(), true) != null)
			targetRoom=CMLib.sessions().findCharacterOnline(cmd.toString(), true).location();
		else
		if(CMLib.map().getRoom(cmd.toString())!=null)
			targetRoom=CMLib.map().getRoom(cmd.toString());
		else
		if(CMLib.directions().getDirectionCode(cmd.toString())>=0)
			targetRoom=curRoom.getRoomInDir(CMLib.directions().getDirectionCode(cmd.toString()));
		else
			targetRoom=CMLib.hunt().findWorldRoomLiberally(mob,cmd.toString(),"RIPME",100,120000);

		if((targetRoom==null)&&(targetSpace==null))
		{
			mob.tell(L("Transfer where? '@x1' is unknown.  Enter a Room ID, player name, area name, or room text!",cmd.toString()));
			return false;
		}
		while(xferObjV.size()>0)
		{
			final Environmental E=xferObjV.remove(0);
			if(E instanceof Item)
			{
				final Item I=(Item)E;
				final Room itemRoom=CMLib.map().roomLocation(I);
				if((targetRoom != null)
				&&((!targetRoom.isContent(I))||(inventoryFlag))
				&&(CMSecurity.isAllowed(mob, itemRoom, CMSecurity.SecFlag.TRANSFER))
				&&(CMSecurity.isAllowed(mob, targetRoom, CMSecurity.SecFlag.TRANSFER)))
				{
					if(inventoryFlag)
						mob.moveItemTo(I,ItemPossessor.Expire.Never,ItemPossessor.Move.Followers);
					else
					{
						targetRoom.moveItemTo(I,ItemPossessor.Expire.Never,ItemPossessor.Move.Followers);
						if(I instanceof SpaceObject)
							((SpaceObject)I).setSpeed(0);
						if(I instanceof SpaceShip)
							((SpaceShip)I).dockHere(targetRoom);
					}
				}
				else
				if(targetSpace != null)
				{
					if(!(I instanceof SpaceObject))
						mob.tell(L("@x1 can't be put into space, as its not a space object.",I.name(mob)));
					else
					{
						final List<Physical> temp=new XVector<Physical>(xferObjV);
						if(!temp.contains(I))
							temp.add(I);
						targetSpace=fixSpaceCoords(xferObjV, distanceDm, targetSpace);
						final SpaceObject o = (SpaceObject)I;
						if(!CMLib.space().isObjectInSpace(o))
							CMLib.space().addObjectToSpace(o, targetSpace);
						else
							o.setCoords(targetSpace);
						o.setSpeed(0.0); // bring to a stop
					}
				}
			}
			else
			if(E instanceof MOB)
			{
				final MOB M=(MOB)E;
				final Room mobRoom=CMLib.map().roomLocation(M);
				if((mobRoom!=null)
				&&(targetRoom != null)
				&&(!targetRoom.isInhabitant(M))
				&&(CMSecurity.isAllowed(mob, mobRoom, CMSecurity.SecFlag.TRANSFER))
				&&(CMSecurity.isAllowed(mob, targetRoom, CMSecurity.SecFlag.TRANSFER)))
				{
					if(M.isPlayer() && (!CMLib.flags().isInTheGame(M, true)))
						M.setLocation(targetRoom);
					else
					{
						if((mob.playerStats().getTranPoofOut().length()>0)&&(M.location()!=null))
							M.location().show(M,M.location(),CMMsg.MSG_LEAVE|CMMsg.MASK_ALWAYS,mob.playerStats().getTranPoofOut());
						targetRoom.bringMobHere(M,true);
					}
					if((mob.playerStats().getTranPoofIn().length()>0)&&(M.location()!=null))
						M.location().show(M,M.location(),CMMsg.MSG_ENTER|CMMsg.MASK_ALWAYS,mob.playerStats().getTranPoofIn());
					if(!M.isMonster() && (targetRoom.isInhabitant(M)))
						CMLib.commands().postLook(M,true);
				}
				else
				if(targetSpace != null)
				{
					mob.tell(L("@x1 can't be put into space, as its not a space object.",M.name(mob)));
				}
			}
		}
		if(mob.playerStats().getTranPoofOut().length()==0)
			mob.tell(L("Done."));
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

	@Override
	public boolean securityCheck(final MOB mob)
	{
		return CMSecurity.isAllowed(mob, mob.location(), CMSecurity.SecFlag.TRANSFER);
	}

}
