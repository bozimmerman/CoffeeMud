package com.planet_ink.coffee_mud.Items.Software;

import com.planet_ink.coffee_mud.Items.Basic.StdItem;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.BoundedObject.BoundedCube;
import com.planet_ink.coffee_mud.core.threads.TimeMs;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.MiniJSON.JSONObject;
import com.planet_ink.coffee_mud.core.MiniJSON.MJSONException;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.Software.SWServices;
import com.planet_ink.coffee_mud.Items.interfaces.Technical.TechCommand;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.net.*;
import java.io.*;
import java.util.*;

/*
 Copyright 2022-2022 Bo Zimmerman

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
public class DatabaseProgram extends GenShipProgram
{
	@Override
	public String ID()
	{
		return "DatabaseProgram";
	}

	protected volatile long				nextPowerCycleTmr	= System.currentTimeMillis() + (8 * 1000);
	protected final StringBuffer		scr					= new StringBuffer("");
	protected JSONObject				data				= new JSONObject();
	protected BoundedCube				spaceCube			= null;

	private static final String[] BASIC_FIELDS = {
		"NOTE", "NAME", "COORDS", "MASS", "RADIUS"
	};

	public DatabaseProgram()
	{
		super();
		setName("a database disk");
		setDisplayText("a small disk sits here.");
		setDescription("It appears to be a database program.");

		material = RawMaterial.RESOURCE_STEEL;
		baseGoldValue = 1000;
		basePhyStats().setWeight(100); // weight shall be how many entries can be held
		phyStats().setWeight(100);
		recoverPhyStats();
	}

	protected void decache()
	{
		scr.setLength(0);
	}

	@Override
	public String getSettings()
	{
		return data.toString();
	}

	@Override
	public void setSettings(final String var)
	{
		if(var.length()>0)
		{
			spaceCube=null;
			try
			{
				data=new MiniJSON().parseObject(var);
				if(data.containsKey("SPACECUBE"))
				{
					final JSONObject cubeData = data.getCheckedJSONObject("SPACECUBE");
					final String coordStr=cubeData.getCheckedString("COORDS");
					final String radiuStr=cubeData.getCheckedString("RADIUS");
					final long[] coords = convertStringToCoords(coordStr);
					final Long radiusL=CMLib.english().parseSpaceDistance(radiuStr);
					if((coords!=null)
					&&(radiusL!=null))
						spaceCube = new BoundedCube(coords,radiusL.longValue());
				}
			}
			catch (final MJSONException e)
			{
				Log.errOut(e);
			}
		}
		settings=var;
	}

	@Override
	protected SWServices[] getProvidedServices()
	{
		return new SWServices[] { Software.SWServices.IDENTIFICATION };
	}

	@Override
	protected SWServices[] getAppreciatedServices()
	{
		return new SWServices[] { Software.SWServices.TARGETING };
	}

	public final Filterer<SpaceObject> spaceFilter = new Filterer<SpaceObject>()
	{
		@Override
		public boolean passesFilter(final SpaceObject obj)
		{
			return isDBSpaceObject(obj);
		}
	};

	@Override
	public boolean isActivationString(String word)
	{
		word=word.toUpperCase().trim();
		return (word.equals("DATABASE") || word.equals("DB"));
	}

	@Override
	public boolean isDeActivationString(final String word)
	{
		return super.isDeActivationString(word);
	}

	@Override
	protected void onDeactivate(final MOB mob, final String message)
	{
		super.onDeactivate(mob, message);
		shutdown();
		super.addScreenMessage("Database browser closed.");
	}

	@Override
	public boolean isCommandString(final String word, final boolean isActive)
	{
		return isActive;
	}

	@Override
	public String getActivationMenu()
	{
		return "DATABASE    : Database Query Software";
	}

	protected void shutdown()
	{
		decache();
	}

	@Override
	protected boolean checkDeactivate(final MOB mob, final String message)
	{
		shutdown();
		return true;
	}

	@Override
	protected boolean checkTyping(final MOB mob, final String message)
	{
		return true;
	}

	@Override
	protected boolean checkPowerCurrent(final int value)
	{
		nextPowerCycleTmr = System.currentTimeMillis() + (8 * 1000);
		return true;
	}

	@Override
	public String getCurrentScreenDisplay()
	{
		return scr.toString();
	}

	@Override
	protected boolean checkActivate(final MOB mob, final String message)
	{
		if(!super.checkActivate(mob, message))
			return false;
		return true;
	}

	@Override
	protected void onActivate(final MOB mob, final String message)
	{
		super.onActivate(mob, message);
		if((message!=null)&&(message.length()>0))
			onTyping(mob, message);
	}

	protected void addLineToReadableScreen(final String s)
	{
		while(scr.length()>1024)
		{
			final int x=scr.indexOf("\n\r");
			if(x>0)
				scr.delete(0, x+2);
			else
				break;
		}
		scr.append(s).append("\n\r");
	}

	protected long[] convertStringToCoords(final String coordStr)
	{
		final List<String> coordCom = CMParms.parseCommas(coordStr,true);
		if(coordCom.size()==3)
		{
			final long[] coords=new long[3];
			for(int i=0;(i<coordCom.size()) && (i<3);i++)
			{
				final Long coord=CMLib.english().parseSpaceDistance(coordCom.get(i));
				if(coord != null)
					coords[i]=coord.longValue();
				else
					return null;
			}
			return coords;
		}
		return null;
	}

	protected long[] getCheckedCoords(final String key)
	{
		if((key==null)
		||(key.length()==0)
		||(!Character.isDigit(key.charAt(0)))
		||(key.indexOf(',')<0))
			return null;
		return convertStringToCoords(key);
	}

	protected long[] getDataCoords(final String key)
	{
		final long[] coords = getCheckedCoords(key);
		if(coords != null)
			return coords;
		if(!data.containsKey(key))
			return null;
		final Object o = data.get(key);
		if(o instanceof String) // it is KNOWN, queries using a name of some sort
			return getCheckedCoords((String)o);
		if(o instanceof JSONObject)
		{
			final JSONObject obj = (JSONObject)o;
			if(obj.containsKey("COORDS"))
				return getCheckedCoords(obj.get("COORDS").toString());
		}
		return null;
	}

	protected boolean isDBSpaceObject(final SpaceObject sO)
	{
		if(sO == null)
			return false;
		if(sO.speed()>0)
			return false;
		if(sO instanceof SpaceShip)
			return false;
		if((sO.radius()>0)&&(sO.radius() < SpaceObject.Distance.MoonRadius.dm/2))
			return false;
		if((sO.getMass()>0) && (sO.getMass() < SpaceObject.MOONLET_MASS/2))
			return false;
		return true;
	}

	protected String getDataName(String key)
	{
		key=key.toUpperCase().trim();
		final long[] coords = getCheckedCoords(key);
		if(coords==null)
		{
			if(!data.containsKey(key.toUpperCase().trim()))
				return "";
			final Object o = data.get(key);
			if((o instanceof String) // it must be an ID/NAME!
			&&(!((String)o).equalsIgnoreCase("SPACE")))
				return (String)o;
			if(o instanceof JSONObject)
			{
				final JSONObject obj = (JSONObject)o;
				if(obj.containsKey("NAME"))
					return obj.get("NAME").toString();
			}
			return key;
		}
		if(data.containsKey(key))
		{
			final Object o = data.get(key);
			if((o instanceof String) // it must be an ID/NAME!
			&&(!((String)o).equalsIgnoreCase("SPACE")))
				return (String)o;
			if(o instanceof JSONObject)
			{
				final JSONObject obj = (JSONObject)o;
				if(obj.containsKey("NAME"))
					return obj.get("NAME").toString();
			}
			// so, these coords ARE in the main database, so SOMETHING deserves to come back...
			final List<SpaceObject> objs = CMLib.space().getSpaceObjectsByCenterpointWithin(coords, 0, 10);
			for(final SpaceObject o1 : objs)
			{
				if((isDBSpaceObject(o1)||(o instanceof String))
				&& Arrays.equals(coords, o1.coordinates()))
					return o1.name();
			}
		}
		else
		{
			final BoundedCube bc = new BoundedCube(coords, 10);
			if((spaceCube!=null)
			&& (spaceCube.intersects(bc)))
			{
				final List<SpaceObject> objs = CMLib.space().getSpaceObjectsByCenterpointWithin(coords, 0, 10);
				for(final SpaceObject o : objs)
				{
					if(isDBSpaceObject(o)
					&& Arrays.equals(coords, o.coordinates()))
						return o.name();
				}
			}
		}
		return "";
	}

	protected List<SpaceObject> findDBSpaceObjects(final long[] coords, final long radius)
	{
		final BoundedCube cube = new BoundedCube(coords,radius);
		final List<SpaceObject> objs = CMLib.space().getSpaceObjectsInBound(cube);
		final List<SpaceObject> found=new LinkedList<SpaceObject>();
		if(objs.size()==0)
			return found;
		if((spaceCube != null)
		&&(spaceCube.intersects(cube)))
		{
			for(final SpaceObject o : objs)
			{
				if(isDBSpaceObject(o)
				&& (cube.contains(o.coordinates())))
					found.add(o);
			}
		}
		if((data.size()==0)
		||(data.containsKey("DISABLED")))
			return found;
		for(final String key : data.keySet())
		{
			final long[] dcoords = getDataCoords(key); // looks at keys, AND values
			if((dcoords != null)
			&&(cube.contains(dcoords)))
			{
				final Object rawo = data.get(key);
				if(((rawo instanceof String)
				&& (rawo.toString().equals("SPACE"))) // as it should be
				)
				{
					for(final SpaceObject so : objs)
					{
						if(Arrays.equals(so.coordinates(), dcoords)
						&&(!found.contains(so)))
							found.add(so);
					}
				}
				else
				if(rawo instanceof String)
				{
					final SpaceObject so=(SpaceObject)CMClass.getTech("StdSpaceBody");
					so.setRadius(0); // denotes that it's not real
					so.setCoords(dcoords);
					so.setName((String)rawo);
					found.add(so);
				}
				else
				if(rawo instanceof JSONObject)
				{
					final JSONObject jo = (JSONObject)rawo;
					if(((JSONObject) rawo).containsKey("SPACE"))
					{
						for(final SpaceObject so : objs)
						{
							if(Arrays.equals(so.coordinates(), dcoords)
							&&(!found.contains(so)))
								found.add(so);
						}
					}
					else
					{
						final SpaceObject so=(SpaceObject)CMClass.getTech("StdSpaceBody");
						so.setCoords(dcoords);
						if(jo.containsKey("RADIUS"))
							so.setRadius(CMath.s_long(jo.get("RADIUS").toString()));
						if(jo.containsKey("NAME"))
							so.setName(jo.get("NAME").toString());
						else
							so.setName("Unknown");
						found.add(so);
					}
				}
			}
			else
			{
				//what if its a name, pointed at the coords given?
				//answer -- that's also captured above
			}
		}
		return found;
	}

	protected String getCustomNotes(final long[] coords)
	{
		if(data.containsKey("DISABLED"))
			return "";
		for(final String key : data.keySet())
		{
			final long[] cord = getDataCoords(key);
			if((cord!=null)
			&&(Arrays.equals(cord, coords)))
			{
				final Object o = data.get(key);
				if(getCheckedCoords(key)!=null)
				{
					if(o instanceof String)
					{
						final String s=(String)o;
						if(s.equals("SPACE"))
							return "";
						return s;
					}
					else
					if(o instanceof JSONObject)
					{
						final JSONObject jo=(JSONObject)o;
						if(jo.containsKey("NOTE"))
							return jo.get("NOTE").toString();
					}
				}
				else
				if(getCheckedCoords(o.toString())!=null)
					return "";
			}
		}
		return "";
	}

	protected String getReportOnSpaceObject(final SpaceObject obj)
	{
		final StringBuilder str=new StringBuilder("");
		str.append(CMStrings.padRight("Coord: "+CMLib.english().coordDescShort(obj.coordinates()),40));
		str.append(CMStrings.padRight("Name : "+obj.name(),38));
		if(obj.getMass()>0)
			str.append("\n\r").append(CMStrings.padRight(" Mass: "+obj.getMass(),38));
		if(obj.radius()>0)
			str.append("\n\r").append(CMStrings.padRight(" Size: "+CMLib.english().distanceDescShort(obj.radius())+" radius",38));
		return str.toString();
	}

	protected SpaceObject spaceCubeMatch(final String query)
	{
		if(spaceCube==null)
			return null;
		final List<SpaceObject> objs = CMLib.space().getSpaceObjectsInBound(spaceCube);
		for(final SpaceObject o : objs)
		{
			if(isDBSpaceObject(o)
			&& o.Name().equalsIgnoreCase(query))
				return o;
		}
		return null;
	}

	protected List<SpaceObject> spaceCubeSearch(final String query)
	{
		final List<SpaceObject> cubeRes = new LinkedList<SpaceObject>();
		if(spaceCube==null)
			return cubeRes;
		final FilteredIterator<SpaceObject> fi = new FilteredIterator<SpaceObject>(
				CMLib.space().getSpaceObjectsInBound(spaceCube).iterator(), spaceFilter);
		final List<SpaceObject> objs = new LinkedList<SpaceObject>();
		for(;fi.hasNext();)
			objs.add(fi.next());
		if(objs.size()==0)
			return cubeRes;
		if(query.equals("*")||query.equals("**"))
			return objs;
		if(query.startsWith("*"))
		{
			if(query.endsWith("*"))
			{
				final String word=query.substring(1,query.length()-1).toUpperCase().trim();
				for(final SpaceObject o : objs)
				{
					if(o.Name().toUpperCase().indexOf(word)>=0)
						cubeRes.add(o);
				}
			}
			else
			{
				final String word=query.substring(1).toUpperCase().trim();
				for(final SpaceObject o : objs)
				{
					if(o.Name().toUpperCase().endsWith(word))
						cubeRes.add(o);
				}
			}
		}
		else
		if(query.endsWith("*"))
		{
			final String word=query.substring(0,query.length()-1).toUpperCase().trim();
			for(final SpaceObject o : objs)
			{
				if(o.Name().toUpperCase().startsWith(word))
					cubeRes.add(o);
			}
		}
		return cubeRes;
	}

	protected String getAllSpaceObjectDataResults(final long[] coords)
	{
		final List<SpaceObject> objs = CMLib.space().getSpaceObjectsByCenterpointWithin(coords, 0, 10);
		if(objs.size()==0)
			return ""; // nothing else to do!
		final SpaceObject obj = objs.iterator().next();
		final StringBuilder str=new StringBuilder("");
		str.append(this.getReportOnSpaceObject(obj));
		final String note=this.getCustomNotes(coords);
		if((note != null)&&(note.length()>0))
			str.append("\n\r").append(note);
		return str.toString();
	}

	protected String getSpaceObjectDataResults(final String key, final long[] ccoords, final JSONObject jo)
	{
		if(jo.containsKey("COORDS") && jo.containsKey("SPACE"))
			return getAllSpaceObjectDataResults(getCheckedCoords(jo.get("COORDS").toString()));
		int maxLen=6;
		for(final String k : jo.keySet())
		{
			if(k.length()>maxLen)
				maxLen=k.length();
		}
		String coords = "N/A";
		if(jo.containsKey("COORDS"))
			coords=CMLib.english().coordDescShort(getCheckedCoords(jo.get("COORDS").toString()));
		else
		if(ccoords != null)
			coords = CMLib.english().coordDescShort(ccoords);
		else
		{
			final long[] c = getCheckedCoords(key);
			if(c != null)
				coords = CMLib.english().coordDescShort(c);
		}
		String name=key;
		if(jo.containsKey("NAME"))
			name=jo.get("NAME").toString();
		final StringBuilder str=new StringBuilder("");
		str.append(CMStrings.padRight(CMStrings.padRight(L("Coord"),maxLen)+" : "+coords,40));
		str.append(CMStrings.padRight(CMStrings.padRight(L("Name"),maxLen)+" : "+name,38));
		if(jo.containsKey("MASS"))
			str.append("\n\r").append(CMStrings.padRight(L(CMStrings.padRight(L("Mass"),maxLen)+": @x1",jo.get("MASS").toString()),38));
		if(jo.containsKey("RADIUS"))
			str.append("\n\r").append(CMStrings.padRight(L(CMStrings.padRight(L("Size"),maxLen)+": @x1 radius",CMLib.english().distanceDescShort(CMath.s_long(jo.get("RADIUS").toString()))),38));
		if(jo.containsKey("NOTE"))
			str.append("\n\r").append(CMStrings.padRight(L("Note"),maxLen)+": ").append(jo.get("NOTE").toString());
		for(final String k : jo.keySet())
		{
			if(!CMParms.contains(DatabaseProgram.BASIC_FIELDS, k))
				str.append("\n\r").append(CMStrings.padRight(L(CMStrings.capitalizeAndLower(k)),maxLen)+": ").append(jo.get("NOTE").toString());
		}
		return str.toString();
	}

	protected String getDataResults(final String key)
	{
		if(data.containsKey("DISABLED"))
			return "";
		final Object o = data.get(key);
		if(o instanceof String)
		{
			final String s=(String)o;
			if(s.equalsIgnoreCase("SPACE"))
			{
				final long[] coords = this.getCheckedCoords(key);
				if(coords == null)
					return ""; // nothing else to do!
				return getAllSpaceObjectDataResults(coords);
			}
			final long[] coords = this.getCheckedCoords(s);
			if((coords != null)
			&&(data.containsKey(CMParms.toListString(coords))))
			{
				final Object oo = data.get(CMParms.toListString(coords));
				if(oo instanceof String)
				{
					final String ss = (String)oo;
					if(ss.equalsIgnoreCase("SPACE"))
						return getAllSpaceObjectDataResults(coords);
					// ss will never be coords, as its key was
					// key is not coords, value is s, not space, is coords, so key must be name
					final StringBuilder str=new StringBuilder("");
					str.append(CMStrings.padRight("Coord: "+CMLib.english().coordDescShort(coords),40)
						  +CMStrings.padRight("Entry : "+ss,38));
					//str.append("\n\r").append(" Name: ").append(key); // keys might be secret
					return str.toString();
				}
				else
				if(oo instanceof JSONObject)
				{
					final JSONObject joo=(JSONObject)oo;
					return this.getSpaceObjectDataResults(key, coords, joo);
				}
				else
				{
					final StringBuilder str=new StringBuilder("");
					str.append(CMStrings.padRight("Coord: "+CMLib.english().coordDescShort(coords),40)
						  +CMStrings.padRight("Entry : "+oo.toString(),38));
					//str.append("\n\r").append(" Name: ").append(key); // keys might be secret
					return str.toString();
				}
			}
			else
			if(coords != null)
			{
				// key is not coords, value is s, not space, is coords, so key must be name
				// HOWEVER, those same coords not cross referenced for a note, so nothing, really
				return CMStrings.padRight("Coord: "+CMLib.english().coordDescShort(coords),40)
					  +CMStrings.padRight("Name : Unknown",38); // key should be secret
			}
			else
			{
				// key is s, not space, not coords.  value is not coords, so wtf?!
				// keep key secret
				return CMStrings.padRight("Name : "+s,38);
			}
		}
		else
		if(o instanceof JSONObject)
		{
			// remember, key is not coords, so some name points to this
			final JSONObject jo = (JSONObject)o;
			return this.getSpaceObjectDataResults(key, null, jo);
		}
		else
		{
			return CMStrings.padRight("Name : "+o.toString(),38); // keep key secret
		}
	}

	protected String doSectorQuery(final String query)
	{
		final String q=query.toUpperCase().trim();
		for(final String secName : CMLib.space().getSectorMap().keySet())
		{
			boolean match=false;
			if(q.startsWith("*"))
			{
				if(q.endsWith("*")&&(q.length()>1))
					match = (secName.toUpperCase().indexOf(q.substring(1,q.length()-1))>=0);
				else
					match = (secName.toUpperCase().endsWith(q.substring(1)));
			}
			else
			if(q.endsWith("*"))
				match = (secName.toUpperCase().startsWith(q.substring(0,q.length()-1)));
			else
				match=secName.equalsIgnoreCase(q);
			if(match)
				return secName;
		}
		return null;
	}

	final Set<SpaceObject> getAllDataSpaceObjects()
	{
		final Set<SpaceObject> spaceObjs=new HashSet<SpaceObject>();
		if(data.containsKey("DISABLED"))
			return spaceObjs;
		for(final String key : data.keySet())
		{
			final long[] coord=this.getCheckedCoords(key);
			if((coord!=null)
			&&((data.get(key).toString().equals("SPACE"))
				||(data.containsKey(data.get(key)) && data.get(data.get(key)).toString().equals("SPACE"))))
			{
				final List<SpaceObject> os=CMLib.space().getSpaceObjectsByCenterpointWithin(coord, 0, 10);
				if(os.size()>0)
					spaceObjs.add(os.iterator().next());
			}
		}
		return spaceObjs;
	}

	protected List<SpaceObject> getAllBoundSpaceObjects(final BoundedCube bcube)
	{
		final List<SpaceObject> sos=CMLib.space().getSpaceObjectsInBound(bcube);
		final Set<SpaceObject> dataObjs = getAllDataSpaceObjects();
		for(final Iterator<SpaceObject> i=sos.iterator();i.hasNext();)
		{
			final SpaceObject O=i.next();
			if(((spaceCube==null)||(!isDBSpaceObject(O))||(!spaceCube.contains(O.coordinates())))
			&&(!dataObjs.contains(O)))
				i.remove();
		}
		return sos;
	}

	protected List<SpaceObject> dbQuerySpaceObjects(final String query, final boolean near)
	{
		long[] queryCoords = null;
		// was the query done by magic word?
		if(query.equalsIgnoreCase("here"))
		{
			final SpaceObject o = CMLib.space().getSpaceObject(this,true);
			if(o != null)
				queryCoords = o.coordinates();
		}
		// was the query directly for coordinates?
		if(queryCoords == null)
			queryCoords = getCheckedCoords(query);
		// was the query the exact name of a space object in the approved cube area?
		if(queryCoords == null)
		{
			final SpaceObject cubeExactMatch = this.spaceCubeMatch(query.toUpperCase().trim());
			if(cubeExactMatch != null)
				queryCoords = cubeExactMatch.coordinates();
		}
		final List<SpaceObject> objs;
		if(queryCoords != null)
		{
			final long radius = near?SpaceObject.Distance.AstroUnit.dm:10;
			// check only what I know, then cough it up
			objs = findDBSpaceObjects(queryCoords,radius);
		}
		else
		{
			// name search of the bounded cube systems, if all else failed
			final List<SpaceObject> chkObjs = this.spaceCubeSearch(query.toUpperCase().trim());
			if(chkObjs.size()>0)
				objs = chkObjs;
			else
			{
				final String sectorMapKey = doSectorQuery(query);
				if(sectorMapKey!=null)
				{
					final BoundedCube cube = CMLib.space().getSectorMap().get(sectorMapKey);
					objs = getAllBoundSpaceObjects(cube);
				}
				else
					objs = null;
			}
		}
		return objs;
	}

	protected List<String> getValues(final String key, final boolean noNotes)
	{
		final List<String> vals = new ArrayList<String>();
		if(data.get(key) instanceof String)
		{
			final String s=((String)data.get(key)).toUpperCase().trim();
			if(s.equals("SPACE"))
				return vals;
			if(this.getCheckedCoords(s) != null)
				vals.add(key.toUpperCase().trim());
			else
				vals.add(s);
		}
		else
		if(data.get(key) instanceof JSONObject)
		{
			final JSONObject job=(JSONObject)data.get(key);
			if(job.containsKey("NAME"))
				vals.add(((String)job.get("NAME")).toUpperCase().trim());
			if(job.containsKey("NOTE") && (!noNotes))
				vals.add(((String)job.get("NOTE")).toUpperCase().trim());
			for(final String jkey : job.keySet())
			{
				if(!CMParms.contains(DatabaseProgram.BASIC_FIELDS, jkey))
					vals.add(((String)job.get(jkey)).toUpperCase().trim());
			}
		}
		return vals;
	}

	protected Set<String> doKeyQuery(final String query, final boolean noNotes)
	{
		final Set<String> keys = new TreeSet<String>();
		if(data.containsKey("DISABLED"))
			return keys;

		// at this point, query is NOT coords, and is NOT in an embedded space box
		if(data.containsKey(query.toUpperCase().trim())) // a full name was entered
		{
			keys.add(query.toUpperCase().trim());
			return keys;
		}
		String q=query;
		int queryType = 0; // 0=whole word, 1=sw, 2=ew, 3=index
		if(query.startsWith("*"))
		{
			if(query.endsWith("*") && (query.length()>1))
			{
				queryType=3;
				q=query.substring(1,query.length()-1).toUpperCase().trim();
			}
			else
			{
				queryType=1;
				q=query.substring(1).toUpperCase().trim();
			}
		}
		else
		if(query.endsWith("*"))
		{
			queryType=2;
			q=query.substring(0,query.length()-1).toUpperCase().trim();
		}
		else
			q=query.toUpperCase().trim();
		for(final String key : data.keySet())
		{
			final List<String> vals = getValues(key,noNotes);
			if(vals.size()==0)
				continue;
			for(final String v : vals)
			{
				boolean match;
				switch(queryType) // 0=whole word, 1=sw, 2=ew, 3=index
				{
				case 0:
					match = v.equals(q);
					break;
				case 1:
					match = v.startsWith(q);
					break;
				case 2:
					match = v.endsWith(q);
					break;
				case 3:
					match = v.indexOf(q)>=0;
					break;
				default:
					match=false;
					break;
				}
				if(match)
				{
					keys.add(key);
					break;
				}
			}
		}
		return keys;
	}

	protected boolean doesNameExistInData(final String name, final String okKey)
	{
		final Set<String> keyMatches=this.doKeyQuery(name.toUpperCase().trim(),true);
		if((keyMatches.size()==1)&&(keyMatches.contains(okKey)))
			return false;
		for(final String k : keyMatches)
		{
			if(k.equals(okKey))
				continue;
			final List<String> vals = getValues(k,true);
			if(vals.contains(name.toUpperCase().trim()))
				return true;
		}
		return false;
	}

	protected String doDBQuery(final List<String> parsed, String query)
	{
		if(parsed.size()==0)
			return "";
		boolean near=false;
		if(parsed.get(0).equals("NEAR")
		||parsed.get(0).equals("AROUND"))
		{
			near=true;
			parsed.remove(0);
			if(parsed.size()==0)
				return "";
			query=CMParms.combine(parsed,0);
		}
		final List<SpaceObject> objs = dbQuerySpaceObjects(query, near);
		if(objs != null)
		{
			if(objs.size()==0)
				return "";
			final StringBuilder str=new StringBuilder("");
			for(final SpaceObject obj : objs)
			{
				if(str.length()>0)
					str.append("\n\r");
				str.append(getReportOnSpaceObject(obj));
				final String note=getCustomNotes(obj.coordinates());
				if((note != null)&&(note.length()>0))
					str.append("\n\r").append(note);
			}
			return str.toString(); // we are done!
		}

		if(data.containsKey("DISABLED"))
			return "";

		final Set<String> winKeys = this.doKeyQuery(query,false);
		if(winKeys.size()>0)
		{
			final StringBuilder str=new StringBuilder("");
			for(final String key : winKeys)
			{
				final String res = getDataResults(key);
				if(res.length()>0)
				{
					if(str.length()>0)
						str.append("\n\r");
					str.append(res);
				}
			}
			if(str.length()>0)
				return str.toString();
		}
		return "";
	}

	@Override
	protected void onTyping(final MOB mob, String message)
	{
		synchronized(this)
		{
			message = message.trim();
			final List<String> parsed=CMParms.parse(message);
			String uword=(parsed.size()>0)?parsed.get(0).toUpperCase():"";
			if(uword.equalsIgnoreCase("DATABASE")
			|| uword.equalsIgnoreCase("DB"))
			{
				parsed.remove(0);
				uword=(parsed.size()>0)?parsed.get(0).toUpperCase():"";
				message=CMParms.combine(parsed,0);
			}
			if(uword.equals("ADD")
			|| uword.equals("DEL")
			|| uword.startsWith("SET."))
			{
				if(data.containsKey("DISABLED"))
				{
					final String resp=L("Not supported by this software.");
					addLineToReadableScreen(resp);
					super.addScreenMessage(resp);
					return;
				}
				String dType="";
				if(uword.startsWith("SET."))
				{
					dType = uword.substring(4).toUpperCase().trim();
					if((dType.length()==0)||dType.equals("SPACE"))
					{
						final String resp=L("Illegal field name.");
						addLineToReadableScreen(resp);
						super.addScreenMessage(resp);
						return;
					}
					for(final String s : BASIC_FIELDS) // normalize
						if(s.startsWith(dType)||dType.startsWith(s))
							dType=s;
				}
				parsed.remove(0);
				message=CMParms.combine(parsed,1);
				if((parsed.size()<1)
				||(!uword.equals("DEL")&&(parsed.size()<2)))
				{
					final String queryStr = L("Note: @x1",CMParms.combine(parsed,0).toLowerCase());
					addLineToReadableScreen(queryStr);
					final String resp;
					if(uword.startsWith("SET."))
						resp =L("No target key and/or value given.");
					else
						resp =L("No target key given.");
					addLineToReadableScreen(resp);
					super.addScreenMessage(resp);
				}
				else
				{
					String key=uword.equals("DEL")?CMParms.combine(parsed,0):parsed.get(0);
					message=CMParms.combine(parsed,1);
					if(key.equalsIgnoreCase("TARGET")||(key.startsWith("TARGET."))||(key.endsWith(".TARGET")))
					{
						final Pair<String,String> tgtP = getTargetKeyName(CMLib.english().getContextDotNumber(key));
						if(tgtP != null)
							key=tgtP.first.toUpperCase().trim();
						else
							key="";
					}
					else
					{
						final List<SpaceObject> objs = dbQuerySpaceObjects(key, false);
						if((objs != null)
						&&(objs.size()>0))
						{
							if(objs.size()==1)
								key=CMParms.toListString(objs.iterator().next().coordinates());
							else
							if(objs.size()>1)
							{
								final String resp =L("Multiple targets selected.  Please narrow search.");
								addLineToReadableScreen(resp);
								super.addScreenMessage(resp);
							}
						}
						else
						if(!data.containsKey("DISABLED"))
						{
							final Set<String> winKeys = this.doKeyQuery(key,false);
							if(winKeys.size()==1)
								key=winKeys.iterator().next();
							else
							if(winKeys.size()>1)
							{
								final String resp =L("Multiple targets selected.  Please narrow search.");
								addLineToReadableScreen(resp);
								super.addScreenMessage(resp);
							}
						}
					}
					if(key.length()==0)
					{
						final String resp=L("No key found.");
						addLineToReadableScreen(resp);
						super.addScreenMessage(resp);
					}
					else
					if((message.length()>40)
					&&(dType.equals("NAME") || uword.equals("ADD")))
					{
						final String resp=L("Illegal name length.");
						addLineToReadableScreen(resp);
						super.addScreenMessage(resp);
					}
					else
					if(message.length()>120)
					{
						final String resp=L("Illegal message length.");
						addLineToReadableScreen(resp);
						super.addScreenMessage(resp);
					}
					else
					if((dType.equals("NAME") || uword.equals("ADD"))
					&&(doesNameExistInData(message, key)))
					{
						final String resp=L("Error: Duplicate name given.");
						addLineToReadableScreen(resp);
						super.addScreenMessage(resp);
					}
					else
					{
						final String queryStr = L("Note: @x1",message.toLowerCase());
						addLineToReadableScreen(queryStr);
						if(message.equalsIgnoreCase("space")||dType.equalsIgnoreCase("space"))
						{
							final String resp=L("Illegal field or message.");
							addLineToReadableScreen(resp);
							super.addScreenMessage(resp);
						}
						else
						if(uword.equals("ADD"))
						{
							if(data.containsKey(key))
							{
								final String resp=L("Entry already exists.");
								addLineToReadableScreen(resp);
								super.addScreenMessage(resp);
							}
							else
							{
								final JSONObject obj = new JSONObject();
								obj.put("NAME", message);
								data.put(key, obj);
								final String resp=L("Entry '@x1' added.",message);
								addLineToReadableScreen(resp);
								super.addScreenMessage(resp);
							}
						}
						else
						if((!data.containsKey(key))
						||(!(data.get(key) instanceof JSONObject)))
						{
							final String resp=L("Entry not found.");
							addLineToReadableScreen(resp);
							super.addScreenMessage(resp);
						}
						else
						if(uword.equals("DEL"))
						{
							final String name = this.getDataName(key);
							data.remove(key);
							final String resp=L("Entry @x1 deleted.",name);
							addLineToReadableScreen(resp);
							super.addScreenMessage(resp);
						}
						else
						{
							final String name = this.getDataName(key);
							final JSONObject jobj=(JSONObject)data.get(key);
							jobj.put(dType.toUpperCase().trim(), message);
							final String resp=L("Field @x1 in entry @x2 updated.",dType,name);
							addLineToReadableScreen(resp);
							super.addScreenMessage(resp);
						}
					}
				}
				return; // done, one way or another
			}
			if((parsed.size()==0)
			||(message.equalsIgnoreCase("HELP")))
			{
				final StringBuilder msg=new StringBuilder("");
				msg.append(L("-- @x1 instructions --\n\r",name()));
				msg.append(L("Search database:\n\r",name()));
				msg.append(L("  Enter search coordinates, e.g. -100gm,400dm,1000km\n\r"));
				msg.append(L("  Enter an object name, e.g. Vulcan\n\r"));
				msg.append(L("  Search for an object, e.g. Vulc*\n\r"));
				msg.append(L(" * Use special object names HERE, or a sector name.\n\r"));
				msg.append(L(" * Precede coordinates/name with the word NEAR\n\r"));
				if(!data.containsKey("DISABLED"))
				{
					msg.append(L("Setting names, notes, and fields:\n\r"));
					msg.append(L("  ADD \"[KEY]\" name\n\r"));
					msg.append(L("  DEL [KEY]\n\r"));
					msg.append(L("  SET.NOTE \"[KEY]\" comment\n\r"));
					if(svcs.containsKey(SWServices.TARGETING))
						msg.append(L(" * [KEY] can be TARGET, coordinates, search\n\r"));
					else
						msg.append(L(" * [KEY] can be coordinates or search\n\r"));
				}
				addLineToReadableScreen(msg.toString());
				super.addScreenMessage(msg.toString());
				return;
			}
			final String query = doDBQuery(parsed,message);
			final String queryStr = L("Query: @x1",message.toLowerCase());
			addLineToReadableScreen(queryStr);
			if((query==null)||(query.length()==0))
			{
				final String resp =L("No results.");
				addLineToReadableScreen(resp);
				super.addScreenMessage(resp);
			}
			else
			{
				addLineToReadableScreen(query);
				super.addScreenMessage(query);
			}
		}
	}

	@Override
	protected void onPowerCurrent(final int value)
	{
		super.onPowerCurrent(value);
		if (System.currentTimeMillis() > nextPowerCycleTmr)
		{
			this.shutdown();
		}
	}

	protected Pair<String,String> getTargetKeyName(final int context)
	{
		final List<String[]> names = super.doServiceTransaction(SWServices.TARGETING, new String[] {"PLEASE"});
		int ct = 0;
		for(final String[] res : names)
		{
			if(++ct<context)
				continue;
			final String NameStr=(res.length>0)?res[0]:"";
			final String nameStr=(res.length>1)?res[1]:"";
			final String coordStr=(res.length>2)?res[2]:"";
			final long[] coords = getCheckedCoords(coordStr);
			final List<SpaceObject> objs = CMLib.space().getSpaceObjectsByCenterpointWithin(coords, 0, 10);
			for(final SpaceObject o1 : objs)
			{
				if(isDBSpaceObject(o1) // separates planets from ships and random Things
				&& Arrays.equals(coords, o1.coordinates()))
				{
					if((this.spaceCube != null)
					&&(this.spaceCube.contains(coords)))
						return new Pair<String,String>(CMParms.toListString(coords), o1.name());
					else
						return new Pair<String,String>(CMParms.toListString(coords), L("Object@@x1",coordStr));
				}
			}
			if(NameStr.length()>0)
			{
				if(nameStr.length()>0)
					return new Pair<String,String>(NameStr,nameStr);
				if(coordStr.length()>0)
					return new Pair<String,String>(NameStr, L("Object@@x1",coordStr));
				else
					return new Pair<String,String>(NameStr, L("Unknown Obj"));
			}
		}
		return null;
	}

	@Override
	protected void provideService(final SWServices service, final Software S, final String[] parms, final CMMsg msg)
	{
		if((service == SWServices.IDENTIFICATION)
		&&(S!=null)
		&&(S!=this)
		&&(parms.length>0))
		{
			for(final String parm : parms)
			{
				String resp = getDataName(parm);
				final int x=resp.indexOf('\n');
				if(x>0)
					resp=resp.substring(0,x).trim();
				if(resp.length()>0)
				{
					final MOB factoryMOB = CMClass.getFactoryMOB(name(), 1, CMLib.map().roomLocation(this));
					try
					{
						final String code=TechCommand.SWSVCRES.makeCommand(service,new String[] { resp });
						final CMMsg msg2=CMClass.getMsg(factoryMOB, S, this,
								CMMsg.NO_EFFECT, null,
								CMMsg.MSG_ACTIVATE|CMMsg.MASK_ALWAYS|CMMsg.MASK_CNTRLMSG, code,
								CMMsg.NO_EFFECT, null);
						msg2.setTargetMessage(code);
						msg.addTrailerMsg(msg2);
					}
					finally
					{
						factoryMOB.destroy();
					}
				}
			}
		}
		else
		if((service == SWServices.COORDQUERY)
		&&(S!=null)
		&&(S!=this)
		&&(parms.length>0))
		{
			for(final String parm : parms)
			{
				final long[] coords = this.getDataCoords(parm);
				if(coords != null)
				{
					final MOB factoryMOB = CMClass.getFactoryMOB(name(), 1, CMLib.map().roomLocation(this));
					try
					{
						final String code=TechCommand.SWSVCRES.makeCommand(service,new String[] { CMParms.toListString(coords) });
						final CMMsg msg2=CMClass.getMsg(factoryMOB, S, this,
								CMMsg.NO_EFFECT, null,
								CMMsg.MSG_ACTIVATE|CMMsg.MASK_ALWAYS|CMMsg.MASK_CNTRLMSG, code,
								CMMsg.NO_EFFECT, null);
						msg2.setTargetMessage(code);
						msg.addTrailerMsg(msg2);
					}
					finally
					{
						factoryMOB.destroy();
					}
				}
			}
		}
	}
}
