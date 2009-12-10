package com.planet_ink.coffee_mud.WebMacros.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;
import com.planet_ink.coffee_mud.core.exceptions.*;

/* 
   Copyright 2000-2010 Bo Zimmerman

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
 * Web Macros are special commands which can be inserted into coffeemud web
 * page (cmvp) files, and can have those command strings substituted with
 * calculated results.  They can include parameters, and can access the
 * other URL parameters.
 */
public interface WebMacro extends CMObject
{
	/** 
	 * The public name of this macro
	 * @return The public name of this macro
	 */
	public String name();
	/**
	 * Whether the runMacro or runBinaryMacro executor should be called.
	 * @see WebMacro#runBinaryMacro(ExternalHTTPRequests, String)
	 * @see WebMacro#runMacro(ExternalHTTPRequests, String)
	 * @return whether the runBinaryMacro executor should be called instead of runMacro
	 */
    public boolean preferBinary();
    /**
     * Whether this macro is restricted to the admin web server.
     * @return true if the macro is restricted to the admin web server
     */
	public boolean isAdminMacro();
	
	/**
	 * Whether this macro returns an attachment instead of something
	 * displayable.  If true, the content-disposition will reflect
	 * the filename parameter.
	 * @see WebMacro#getFilename(ExternalHTTPRequests, String)
	 * @param filename the filename from getFilename
	 * @return  this is an attachment macro, so send back header
	 */
    public String getSpecialContentHeader(String filename);
    
	/**
	 * Whether this macro substitutes as an aspect of the web path instead
	 * of a standard web macro.  If true is returned, URLs such as:
	 * http://mydomain.com/mymacroname?firstparm=value&secondparm=value
	 * might succeeed
	 * @see WebMacro#getFilename(ExternalHTTPRequests, String)
	 * @return whether this is a wierd URL macro
	 */
    public boolean isAWebPath();
    /**
     * If this macro returns true from isAWebPath(), this will be the substitute
     * filename to use as a page for returning to the caller.  It may simply
     * return what is given to it.
     * @see WebMacro#isAWebPath()
     * @see com.planet_ink.coffee_mud.Libraries.interfaces.ExternalHTTPRequests
     * @param httpReq the requests object
     * @param filename the default filename
     * @return usually the default filename again
     */
    public String getFilename(ExternalHTTPRequests httpReq, String filename);
    
    /**
     * This method is executed only if this macro returns true for preferBinary().
     * It will execute the macro and return its results as a binary byte array.
     * @see WebMacro#preferBinary()
     * @see com.planet_ink.coffee_mud.Libraries.interfaces.ExternalHTTPRequests
     * @param httpReq the external requests object
     * @param parm any parameter strigs given to the macro
     * @return the binary stream result of running this macro
     * @throws HTTPServerException
     */
    public byte[] runBinaryMacro(ExternalHTTPRequests httpReq, String parm) throws HTTPServerException;
    /**
     * This method is executed only if this macro returns false for preferBinary().
     * It will execute the macro and return its results as a string, which is then
     * substituted for the macro reference in the web page where the macro was found.
     * @see WebMacro#preferBinary()
     * @see com.planet_ink.coffee_mud.Libraries.interfaces.ExternalHTTPRequests
     * @param httpReq the external requests object
     * @param parm any parameter strigs given to the macro
     * @return the string result of running this macro
     * @throws HTTPServerException
     */
	public String runMacro(ExternalHTTPRequests httpReq, String parm) throws HTTPServerException;
}