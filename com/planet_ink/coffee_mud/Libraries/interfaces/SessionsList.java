package com.planet_ink.coffee_mud.Libraries.interfaces;

import java.util.*;

import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
/*
   Copyright 2005-2022 Bo Zimmerman

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
 * Sessions are managed connections to a host telnet port
 * through which the players interact.  Each session is 
 * attached to a single player/character.  The sessions are
 * managed in this library.  
 * 
 * @author Bo Zimmerman
 *
 */
public interface SessionsList extends CMLibrary
{
	/**
	 * Given a session, this will attempt to stop
	 * its running thread in every way imaginable
	 * and remove the session from the manager.
	 *  
	 * @param S the session to stop and kill
	 */
	public void stopSessionAtAllCosts(Session S);
	
	/**
	 * Searches online characters whose sessions are managed
	 * by this library, and returns the session.
	 * 
	 * @param srchStr the whole or partial char name
	 * @param exactOnly true for whole, false for partial or whole
	 * @return null, or the character mob's session
	 */
	public Session findCharacterSessionOnline(String srchStr, boolean exactOnly);
	
	/**
	 * Searches online characters whose sessions are managed
	 * by this library, and returns the character mobs.
	 * 
	 * @param srchStr the whole or partial char name
	 * @param exactOnly true for whole, false for partial or whole
	 * @return null, or the character mob found
	 */
	public MOB findCharacterOnline(String srchStr, boolean exactOnly);
	
	/**
	 * Returns the sessions for all the the sessions
	 * currently managed, logging in or not, from all
	 * hosts running.
	 * 
	 * @see SessionsList#allIterable()
	 * @see SessionsList#allIterableAllHosts()
	 * @see SessionsList#localOnline()
	 * @see SessionsList#localOnlineIterable()
	 * 
	 * @return the sessions for all the sessions
	 */
	public Iterator<Session> sessions();
	
	/**
	 * Returns the sessions for all the the sessions
	 * currently managed, logging in or not, from all
	 * hosts running.
	 * 
	 * @see SessionsList#sessions()
	 * @see SessionsList#allIterableAllHosts()
	 * @see SessionsList#localOnline()
	 * @see SessionsList#localOnlineIterable()
	 * 
	 * @return the sessions for all the sessions
	 */
	public Iterable<Session> allIterable();
	
	/**
	 * Returns the sessions for all the the sessions
	 * currently managed, logging in or not, from all
	 * hosts running.
	 * 
	 * @see SessionsList#sessions()
	 * @see SessionsList#allIterable()
	 * @see SessionsList#localOnline()
	 * @see SessionsList#localOnlineIterable()
	 * 
	 * @return the sessions for all the sessions all hosts
	 */
	public Iterable<Session> allIterableAllHosts();
	
	/**
	 * Returns the sessions for all the online game
	 * characters that are on the map and playing.
	 * 
	 * @see SessionsList#sessions()
	 * @see SessionsList#allIterable()
	 * @see SessionsList#allIterableAllHosts()
	 * @see SessionsList#localOnlineIterable()
	 * 
	 * @return the sessions for all the online game chars
	 */
	public Iterator<Session> localOnline();
	
	/**
	 * Returns the sessions for all the online game
	 * characters that are on the map and playing.
	 * 
	 * @see SessionsList#sessions()
	 * @see SessionsList#allIterable()
	 * @see SessionsList#allIterableAllHosts()
	 * @see SessionsList#localOnline()
	 * 
	 * @return the sessions for all the online game chars
	 */
	public Iterable<Session> localOnlineIterable();
	
	/**
	 * The number of char sessions being managed here.
	 * This only includes actual player sessions for mobs
	 * in the game.
	 * 
	 * @see SessionsList#isSession(Session)
	 * @see SessionsList#add(Session)
	 * @see SessionsList#remove(Session)
	 * @see SessionsList#numSessions()
	 * 
	 * @return the number of sessions
	 */
	public int numLocalOnline();
	
	/**
	 * The number of sessions being managed here.
	 * This includes being logging on, logging off,
	 * and in all other states.
	 * 
	 * @see SessionsList#isSession(Session)
	 * @see SessionsList#add(Session)
	 * @see SessionsList#remove(Session)
	 * @see SessionsList#numLocalOnline()
	 * 
	 * @return the number of sessions
	 */
	public int numSessions();
	
	/**
	 * Adds the given session to this manager, to be managed
	 * 
	 * @see SessionsList#isSession(Session)
	 * @see SessionsList#add(Session)
	 * @see SessionsList#remove(Session)
	 * @see SessionsList#numSessions()
	 * @see SessionsList#numLocalOnline()
	 * 
	 * @param s the session to add
	 */
	public void add(Session s);
	
	/**
	 * Removes the given session from this manager.
	 * 
	 * @see SessionsList#isSession(Session)
	 * @see SessionsList#add(Session)
	 * @see SessionsList#remove(Session)
	 * @see SessionsList#numSessions()
	 * @see SessionsList#numLocalOnline()
	 * 
	 * @param s the session to remove
	 */
	public void remove(Session s);
	
	/**
	 * Returns whether the given session is still registered here.
	 * 
	 * @see SessionsList#add(Session)
	 * @see SessionsList#remove(Session)
	 * @see SessionsList#numSessions()
	 * @see SessionsList#numLocalOnline()
	 * 
	 * @param s the session to check for
	 * @return true if its still managed here, false otherwise
	 */
	public boolean isSession(Session s);

	/**
	 * Determines the correct thread group for the given theme, and marks the
	 * given session appropriately.
	 * 
	 * @param session the session to move
	 * @param theme the theme of the group to assign it to
	 */
	public void moveSessionToCorrectThreadGroup(final Session session, int theme);
}
