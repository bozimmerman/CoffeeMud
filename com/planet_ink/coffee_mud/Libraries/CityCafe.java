package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMSecurity.DbgFlag;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.collections.MultiEnumeration.MultiEnumeratorBuilder;
import com.planet_ink.coffee_mud.core.interfaces.BoundedObject;
import com.planet_ink.coffee_mud.core.interfaces.BoundedObject.BoundedCube;
import com.planet_ink.coffee_mud.core.interfaces.LandTitle;
import com.planet_ink.coffee_mud.core.interfaces.MsgListener;
import com.planet_ink.coffee_mud.core.interfaces.PrivateProperty;
import com.planet_ink.coffee_mud.core.interfaces.SpaceObject;
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
   Copyright 2001-2022 Bo Zimmerman

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
public class CityCafe extends StdLibrary implements CityMap
{
	@Override
	public String ID()
	{
		return "CityCafe";
	}

	protected List<PostOffice>			postOfficeList			= new SVector<PostOffice>();
	protected List<Auctioneer>			auctionHouseList		= new SVector<Auctioneer>();
	protected List<Banker>				bankList				= new SVector<Banker>();
	protected List<Librarian>			libraryList				= new SVector<Librarian>();
	protected Map<String, Set<Places>>	holyPlaces				= new SHashtable<String, Set<Places>>();

	public int numPostOffices()
	{
		return postOfficeList.size();
	}

	@Override
	public void addPostOffice(final PostOffice newOne)
	{
		if(!postOfficeList.contains(newOne))
			postOfficeList.add(newOne);
	}

	@Override
	public void delPostOffice(final PostOffice oneToDel)
	{
		postOfficeList.remove(oneToDel);
	}

	@Override
	public PostOffice getPostOffice(final String chain, final String areaNameOrBranch)
	{
		final boolean anyArea = areaNameOrBranch.equalsIgnoreCase("*");
		for (final PostOffice P : postOfficeList)
		{
			if((P.postalChain().equalsIgnoreCase(chain))
			&&((anyArea)||(P.postalBranch().equalsIgnoreCase(areaNameOrBranch))))
				return P;
		}

		final Area A=CMLib.map().findArea(areaNameOrBranch);
		if(A==null)
			return null;

		for (final PostOffice P : postOfficeList)
		{
			if((P.postalChain().equalsIgnoreCase(chain))
			&&(CMLib.map().getStartArea(P)==A))
				return P;
		}
		return null;
	}

	@Override
	public Enumeration<PostOffice> postOffices()
	{
		return new IteratorEnumeration<PostOffice>(postOfficeList.iterator());
	}

	@Override
	public Enumeration<Auctioneer> auctionHouses()
	{
		return new IteratorEnumeration<Auctioneer>(auctionHouseList.iterator());
	}

	public int numAuctionHouses()
	{
		return auctionHouseList.size();
	}

	@Override
	public void addAuctionHouse(final Auctioneer newOne)
	{
		if (!auctionHouseList.contains(newOne))
		{
			auctionHouseList.add(newOne);
		}
	}

	@Override
	public void delAuctionHouse(final Auctioneer oneToDel)
	{
		auctionHouseList.remove(oneToDel);
	}

	@Override
	public Auctioneer getAuctionHouse(final String chain, final String areaNameOrBranch)
	{
		for (final Auctioneer C : auctionHouseList)
		{
			if((C.auctionHouse().equalsIgnoreCase(chain))
			&&(C.auctionHouse().equalsIgnoreCase(areaNameOrBranch)))
				return C;
		}

		final Area A=CMLib.map().findArea(areaNameOrBranch);
		if(A==null)
			return null;

		for (final Auctioneer C : auctionHouseList)
		{
			if((C.auctionHouse().equalsIgnoreCase(chain))
			&&(CMLib.map().getStartArea(C)==A))
				return C;
		}

		return null;
	}

	public int numBanks()
	{
		return bankList.size();
	}

	@Override
	public void addBank(final Banker newOne)
	{
		if (!bankList.contains(newOne))
			bankList.add(newOne);
	}

	@Override
	public void delBank(final Banker oneToDel)
	{
		bankList.remove(oneToDel);
	}

	@Override
	public Banker getBank(final String chain, final String areaNameOrBranch)
	{
		for (final Banker B : bankList)
		{
			if((B.bankChain().equalsIgnoreCase(chain))
			&&((areaNameOrBranch==null)||(B.bankChain().equalsIgnoreCase(areaNameOrBranch))))
				return B;
		}

		final Area A=CMLib.map().findArea(areaNameOrBranch);
		if(A==null)
			return null;

		for (final Banker B : bankList)
		{
			if((B.bankChain().equalsIgnoreCase(chain))
			&&(CMLib.map().getStartArea(B)==A))
				return B;
		}
		return null;
	}

	@Override
	public Enumeration<Banker> banks()
	{
		return new IteratorEnumeration<Banker>(bankList.iterator());
	}

	@Override
	public Enumeration<String> bankChains(final Area AreaOrNull)
	{
		final HashSet<String> H=new HashSet<String>();
		for (final Banker B : bankList)
		{
			if(!H.contains(B.bankChain()))
			{
				final Area sA=CMLib.map().getStartArea(B);
				if((AreaOrNull==null)
				||(sA==AreaOrNull)
				||(AreaOrNull.isChild(sA)))
					H.add(B.bankChain());
			}
		}
		return new IteratorEnumeration<String>(H.iterator());
	}

	protected Set<Places> getHolyPlaces(final String deityName)
	{
		if(deityName==null)
			return new TreeSet<Places>();
		final String udName=deityName.toUpperCase().trim();
		if(!holyPlaces.containsKey(udName))
			holyPlaces.put(udName, new TreeSet<Places>(Places.placeComparator));
		return holyPlaces.get(udName);
	}

	@Override
	public void registerHolyPlace(final String deityName, final Places newOne)
	{
		if(newOne != null)
		{
			final Set<Places> holyPlaces = getHolyPlaces(deityName);
			if (!holyPlaces.contains(newOne))
				holyPlaces.add(newOne);
		}
	}

	@Override
	public void deregisterHolyPlace(final String deityName, final Places newOne)
	{
		if(newOne != null)
		{
			final Set<Places> holyPlaces = getHolyPlaces(deityName);
			holyPlaces.remove(newOne);
		}
	}

	@Override
	public Enumeration<Places> holyPlaces(final String deityName)
	{
		final Set<Places> holyPlaces = getHolyPlaces(deityName);
		final ArrayList<Places> placesCopy=new ArrayList<Places>(holyPlaces.size());
		synchronized(holyPlaces)
		{
			for(final Iterator<Places> i=holyPlaces.iterator();i.hasNext();)
			{
				final Places place = i.next();
				if(place.amDestroyed())
					i.remove();
				else
					placesCopy.add(place);
			}
		}
		return new IteratorEnumeration<Places>(placesCopy.iterator());
	}

	@Override
	public int numLibraries()
	{
		return libraryList.size();
	}

	@Override
	public void addLibrary(final Librarian newOne)
	{
		if (!libraryList.contains(newOne))
			libraryList.add(newOne);
	}

	@Override
	public void delLibrary(final Librarian oneToDel)
	{
		libraryList.remove(oneToDel);
	}

	@Override
	public Librarian getLibrary(final String chain, final String areaNameOrBranch)
	{
		for (final Librarian B : libraryList)
		{
			if((B.libraryChain().equalsIgnoreCase(chain))
			&&(B.libraryChain().equalsIgnoreCase(areaNameOrBranch)))
				return B;
		}

		final Area A=CMLib.map().findArea(areaNameOrBranch);
		if(A==null)
			return null;

		for (final Librarian B : libraryList)
		{
			if((B.libraryChain().equalsIgnoreCase(chain))
			&&(CMLib.map().getStartArea(B)==A))
				return B;
		}
		return null;
	}

	@Override
	public Enumeration<Librarian> libraries()
	{
		return new IteratorEnumeration<Librarian>(libraryList.iterator());
	}

	@Override
	public Enumeration<String> libraryChains(final Area areaOrNull)
	{
		final HashSet<String> H=new HashSet<String>();
		for (final Librarian B : libraryList)
		{
			if(!H.contains(B.libraryChain()))
			{
				final Area sA=CMLib.map().getStartArea(B);
				if((areaOrNull==null)
				||(sA==areaOrNull)
				||(areaOrNull.isChild(sA)))
					H.add(B.libraryChain());
			}
		}
		return new IteratorEnumeration<String>(H.iterator());
	}

}
