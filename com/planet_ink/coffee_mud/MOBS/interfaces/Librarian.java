package com.planet_ink.coffee_mud.MOBS.interfaces;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.MoneyLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine.PlayerData;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.List;
import java.util.Vector;

/*
   Copyright 2017-2018 Bo Zimmerman

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
/**
 * A Librarian is a kind of shopkeeper that belongs to a "chain" which
 * shares access to a common store of shop items.  Players can go to a 
 * librarian in a chain to borrow items available to the chain. 
 * Librarians respond to new commands such as DEPOSIT and WITHDRAW that 
 * normal shopkeepers do not.  Borrowed items not re-deposited within
 * the proper amount of time are auto-returned, and the borrower charged
 * a penalty.
 * 
 * Since they lend and retrieve any item in the shop inventory, they
 * do not have a shopkeeper type, but respect whatever type they are.
 * 
 * @author Bo Zimmerman
 */
public interface Librarian extends ShopKeeper
{
	/**
	 * The daily minimum base currency charge due the moment
	 * a withdrawn item becomes overdue.
	 */
	public final static double	DEFAULT_MIN_OVERDUE_CHARGE	= 1.0;

	/**
	 * The default percent from 0 to 1, of the value of a 
	 * withdrawn item, in base currency charge, due the moment
	 * a withdrawn item becomes overdue.
	 */
	public final static double	DEFAULT_PCT_OVERDUE_CHARGE	= 0.1;

	/**
	 * The default daily base currency charge due every day
	 * a withdrawn item remains overdue.
	 */
	public final static double	DEFAULT_MIN_OVERDUE_DAILY	= 1.0;

	/**
	 * The default percent from 0 to 1, of the value of a 
	 * withdrawn item, in base currency charge, due every
	 * day that a withdrawn item remains overdue.
	 */
	public final static double	DEFAULT_PCT_OVERDUE_DAILY	= 0.1;

	/**
	 * The default number of mud-days that an item can be checked
	 * out before being overdue.  After this number of days, the 
	 * item is considered overdue and charges begin accruing.
	 */
	public final static int		DEFAULT_MIN_OVERDUE_DAYS	= 3;

	/**
	 * The default maximum number of mud-days that an item can be
	 * checked out.  After this number of days, the item is
	 * automatically returned to the librarian, and the charges
	 * made.
	 */
	public final static int		DEFAULT_MAX_OVERDUE_DAYS	= 15;
	
	/**
	 * The default maximum number of items that one person can have
	 * checked out at any given time.
	 */
	public final static int		DEFAULT_MAX_BORROWED		= 5;


	/**
	 * Gets the minimum base currency charge due the moment
	 * a withdrawn item becomes overdue.
	 * 
	 * @see Librarian#setOverdueCharge(double)
	 * 
	 * @return the base charge
	 */
	public double getOverdueCharge();

	/**
	 * Sets the minimum base currency charge due the moment
	 * a withdrawn item becomes overdue.
	 * 
	 * @see Librarian#setOverdueCharge(double)
	 * 
	 * @param charge the base charge
	 */
	public void setOverdueCharge(double charge);

	/**
	 * Gets the daily base currency charge due every day
	 * a withdrawn item remains overdue.
	 * 
	 * @see Librarian#setDailyOverdueCharge(double)
	 * 
	 * @return the daily charge from 0-1
	 */
	public double getDailyOverdueCharge();

	/**
	 * Sets the daily base currency charge due every day
	 * a withdrawn item remains overdue. Value 0-1.
	 * 
	 * @see Librarian#setDailyOverdueCharge(double)
	 * 
	 * @param charge the daily charge from 0-1
	 */
	public void setDailyOverdueCharge(double charge);

	/**
	 * Gets the percent from 0 to 1, of the value of a 
	 * withdrawn item, in base currency charge, due the moment
	 * a withdrawn item becomes overdue.
	 * 
	 * @see Librarian#setOverdueChargePct(double)
	 * 
	 * @return the base charge as pct of item value from 0-1
	 */
	public double getOverdueChargePct();

	/**
	 * Sets the percent from 0 to 1, of the value of a 
	 * withdrawn item, in base currency charge, due the moment
	 * a withdrawn item becomes overdue.
	 * 
	 * @see Librarian#setOverdueChargePct(double)
	 * 
	 * @param pct the base charge as pct of item value from 0-1
	 */
	public void setOverdueChargePct(double pct);

	/**
	 * Gets the percent from 0 to 1, of the value of a 
	 * withdrawn item, in base currency charge, due every
	 * day that a withdrawn item remains overdue.
	 * 
	 * @see Librarian#setDailyOverdueChargePct(double)
	 * 
	 * @return the base charge as pct of item value from 0-1
	 */
	public double getDailyOverdueChargePct();

	/**
	 * Sets the percent from 0 to 1, of the value of a 
	 * withdrawn item, in base currency charge, due every
	 * day that a withdrawn item remains overdue.
	 * 
	 * @see Librarian#setDailyOverdueChargePct(double)
	 * 
	 * @param pct the base charge as pct of item value from 0-1
	 */
	public void setDailyOverdueChargePct(double pct);

	/**
	 * Gets the number of mud-days that an item can be checked
	 * out before being overdue.  After this number of days, the 
	 * item is considered overdue and charges begin accruing.
	 * 
	 * @see Librarian#setMinOverdueDays(int)
	 * @see Librarian#getMaxOverdueDays()
	 * @see Librarian#setMaxOverdueDays(int)
	 * 
	 * @return mud-days before its overdue
	 */
	public int getMinOverdueDays();
	
	/**
	 * Sets the number of mud-days that an item can be checked
	 * out before being overdue.  After this number of days, the 
	 * item is considered overdue and charges begin accruing.
	 * 
	 * @see Librarian#getMinOverdueDays()
	 * @see Librarian#getMaxOverdueDays()
	 * @see Librarian#setMaxOverdueDays(int)
	 * 
	 * @param days mud-days before its overdue
	 */
	public void setMinOverdueDays(int days);
	
	/**
	 * Gets the maximum number of mud-days that an item can be
	 * checked out.  After this number of days, the item is
	 * automatically returned to the librarian, and the charges
	 * made.
	 * 
	 * @see Librarian#setMaxOverdueDays(int)
	 * @see Librarian#getMinOverdueDays()
	 * @see Librarian#setMinOverdueDays(int)
	 * 
	 * @return mud-days to be overdue
	 */
	public int getMaxOverdueDays();
	
	/**
	 * Sets the maximum number of mud-days that an item can be
	 * checked out.  After this number of days, the item is
	 * automatically returned to the librarian, and the charges
	 * made.
	 * 
	 * @see Librarian#getMaxOverdueDays()
	 * @see Librarian#getMinOverdueDays()
	 * @see Librarian#setMinOverdueDays(int)
	 * 
	 * @param days mud-days to be overdue
	 */
	public void setMaxOverdueDays(int days);

	/**
	 * Gets the maximum number of items that one person can have
	 * checked out at any given time.
	 * 
	 * @see Librarian#setMaxBorrowed(int)
	 * 
	 * @return number of items
	 */
	public int getMaxBorrowed();

	/**
	 * Sets the maximum number of items that one person can have
	 * checked out at any given time.
	 * 
	 * @see Librarian#getMaxBorrowed()
	 * 
	 * @param items number of items
	 */
	public void setMaxBorrowed(int items);

	/**
	 * Gets the name of the library chain to which this librarian belongs.
	 * @see Librarian#setLibraryChain(String)
	 * @return the library chain name
	 */
	public String libraryChain();

	/**
	 * Sets the name of the library chain to which this librarian belongs.
	 * @see Librarian#libraryChain()
	 * @param name the library chain name
	 */
	public void setLibraryChain(String name);
	/**
	 * Returns the mask used to determine if a contributor is ignored by the Librarian
	 * for contribution.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary
	 * @see Librarian#finalContributorMask()
	 * @see Librarian#setContributorMask(String)
	 * @return the mask used
	 */
	public String contributorMask();
	/**
	 * Sets the mask used to determine if a contributor is ignored by the Librarian
	 * for contribution.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary
	 * @see Librarian#finalContributorMask()
	 * @see Librarian#contributorMask()
	 * @param mask the mask to use
	 */
	public void setContributorMask(String mask);
}
