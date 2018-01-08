package com.planet_ink.coffee_web.interfaces;

import java.util.logging.Logger;

/*
   Copyright 2012-2018 Bo Zimmerman

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
 * This interface encapsulates the HTTP request portion 
 * of the SimpleServlet specification.
 */
public interface SimpleServletRequest extends HTTPRequest 
{
	/**
	 * Access the server who accepted and is managing this
	 * request.
	 * @return the server who accepted and is managing this request
	 */
	public SimpleServletManager getServletManager();
	
	/**
	 * Returns the session object associated with this servlet request
	 * @return the session object
	 */
	public SimpleServletSession getSession();
	
	/**
	 * Returns a java.util.Logger-compliant logger to write to
	 * @return a logger
	 */
	public Logger getLogger();
}
