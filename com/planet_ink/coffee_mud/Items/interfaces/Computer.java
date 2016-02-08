package com.planet_ink.coffee_mud.Items.interfaces;

import java.util.List;

import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;

/**
 * A computer is a particular type of electronics panel that holds
 * software, and has readers who monitor the messages this panel
 * generates, as well as use the Type command to enter data into
 * this panel's software.
 * @see Software
 * @see ElecPanel
 * @author Bo Zimmerman
 *
 */
public interface Computer extends ElecPanel
{
	/**
	 * Returns the list of Software objects installed in this computer.
	 * @see Software
	 * @return the list of Software objects installed in this computer
	 */
	public List<Software> getSoftware();
	
	/**
	 * Returns the list of mobs currently monitoring the output of this
	 * computers software.
	 * @return the list of mobs currently monitoring the output
	 */
	public List<MOB> getCurrentReaders();
	
	/**
	 * Forces all the current readers to "read" the computer, typically
	 * seeing the menu.
	 * @see Computer#getCurrentReaders()
	 * @see Computer#forceReadersSeeNew()
	 */
	public void forceReadersMenu();
	
	/**
	 * Forces all the current readers to see any new messages that 
	 * should be seen by anyone monitoring the computer.
	 * @see Computer#getCurrentReaders()
	 * @see Computer#forceReadersMenu()
	 */
	public void forceReadersSeeNew();
	
	/**
	 * Most software supports different levels of menu, and some software
	 * is even a sub-menu unto itself.  This method forces the system to
	 * recognize one of those menus as current.  The software takes it
	 * from there.
	 * @see Computer#getActiveMenu()
	 * @param internalName the menu to set as current and active
	 */
	public void setActiveMenu(String internalName);
	
	/**
	 * Most software supports different levels of menu, and some software
	 * is even a sub-menu unto itself.  This method returns the current
	 * active menu.
	 * @see Computer#setActiveMenu(String)
	 * @return internalName the menu to set as current and active
	 */
	public String getActiveMenu();
}