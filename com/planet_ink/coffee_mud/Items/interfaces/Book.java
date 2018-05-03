package com.planet_ink.coffee_mud.Items.interfaces;
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
 * A book is a readable object that can usually be written to.
 * @author Bo Zimmerman
 */
public interface Book extends Item
{
	/**
	 * Returns the number of pages with writing on them.
	 * @see Book#getContent(int)
	 * @see Book#getRawContent(int)
	 * @see Book#getMaxPages()
	 * @return the number of pages with writing on them.
	 */
	public int getUsedPages();
	
	/**
	 * Returns the maximum number of pages that can be written on
	 * in this book.  0 means infinite.
	 * @see Book#setMaxPages(int)
	 * @see Book#getUsedPages()
	 * @return the maximum number of pages that can be written on
	 */
	public int getMaxPages();
	
	/**
	 * 
	 * Sets the maximum number of pages that can be written on
	 * in this book.  0 means infinite.
	 * @see Book#getMaxPages()
	 * @param max the maximum number of pages that can be written on
	 */
	public void setMaxPages(int max);
	
	/**
	 * Returns the maximum number of chars that can be written on
	 * each page in this book.  0 means infinite.
	 * @see Book#setMaxCharsPerPage(int)
	 * @return the maximum number of chars that can be written on
	 */
	public int getMaxCharsPerPage();
	
	/**
	 * Sets the maximum number of chars that can be written on
	 * each page in this book.  0 means infinite.
	 * @see Book#getMaxCharsPerPage()
	 * @param max the maximum number of pages that can be written on
	 */
	public void setMaxCharsPerPage(int max);
	
	/**
	 * Returns the raw content of a page
	 * @see Book#getContent(int)
	 * @see Book#getUsedPages()
	 * @param page the page number, 1-N
	 * @return the raw content of a page
	 */
	public String getRawContent(int page);

	/**
	 * Returns the readable content of a page
	 * @see Book#getRawContent(int)
	 * @see Book#getUsedPages()
	 * @param page the page number, 1-N
	 * @return the readable content of a page
	 */
	public String getContent(int page);
	
	/**
	 * Adds a page to this book, or appends content if just a piece of paper.
	 * @see Book#getRawContent(int)
	 * @see Book#getUsedPages()
	 * @param authorName the name of the author, which might not be used
	 * @param content the content, with subject surrounded by "::" as first chars, optional
	 */
	public void addRawContent(String authorName, String content);

	/**
	 * Returns whether the content is shared among all similar items, or
	 * is unique to this item or owner or a special key.
	 * @return true if it is shared, false otherwise
	 */
	public boolean isJournal();
}
