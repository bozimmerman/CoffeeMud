package com.planet_ink.coffee_mud.core.interfaces;

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
/**
 * This interface is implemented by classes who can properly save
 * and restore a database ID, to uniquely identify the object at
 * a particular row in the database.
 * @author Bo Zimmerman
 *
 */
public interface DBIdentifiable {
	/**
	 * Retrieve the unique contextual database ID for this object.
	 * @see com.planet_ink.coffee_mud.core.interfaces.DBIdentifiable#canSaveDatabaseID()
	 * @see com.planet_ink.coffee_mud.core.interfaces.DBIdentifiable#setDatabaseID(String)
	 * @return the unique contextual database ID for this object.
	 */
	public String databaseID();
	/**
	 * Set the unique contextual database ID for this object.
	 * @param ID the unique contextual database ID for this object.
	 * @see com.planet_ink.coffee_mud.core.interfaces.DBIdentifiable#canSaveDatabaseID()
	 * @see com.planet_ink.coffee_mud.core.interfaces.DBIdentifiable#databaseID()
	 */
	public void setDatabaseID(String ID);

	/**
	 * Returns true if this object can safely store and retrieve
	 * a database ID, and false if the TYPE of object can, but
	 * this particular instance can not.
	 * @see com.planet_ink.coffee_mud.core.interfaces.DBIdentifiable#databaseID()
	 * @see com.planet_ink.coffee_mud.core.interfaces.DBIdentifiable#setDatabaseID(String)
	 * @return true if setDatabaseID can be called, false otherwise
	 */
	public boolean canSaveDatabaseID();
}
