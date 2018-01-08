package com.planet_ink.coffee_web.interfaces;

import java.io.File;
import com.planet_ink.coffee_web.http.HTTPException;

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
 * Retrieves Web Server File Data
 * @author Bo Zimmerman
 *
 */
public interface HTTPFileGetter 
{
	/**
	 * Generates a bytebuffer representing the results of the request 
	 * contained herein.  HTTP errors can still be generated, however,
	 * so those are watched for.
	 * 
	 * Requests can trigger file reads, servlet calls and other ways
	 * of generating body and header data.
	 * 
	 * @param request the request to generate output for
	 * @throws HTTPException
	 * @return the entire full output for this request
	 */
	public DataBuffers generateOutput(HTTPRequest request) throws HTTPException;
	
	/**
	 * Retrieves a buffer set containing the possibly cached contents of the file. 
	 * This can trigger file reads, servlet calls and other ways
	 * of generating body data.
	 * 
	 * @param request the request to generate output for
	 * @throws HTTPException
	 * @return the entire full output for this request
	 */
	public DataBuffers getFileData(HTTPRequest request) throws HTTPException;
	
	/**
	 * After a uri has been broken apart and inspected, this method is called
	 * to reassemble it into a valid File path using local file separators.
	 * If you wish to add a special directory root for html docs, this would
	 * be the appropriate place to do it.
	 * @param request the request being processed
	 * @return the full assembled file path
	 */
	public String assembleFilePath(HTTPRequest request);
	
	/**
	 * After a final file path is assembled, this method
	 * returns a file object appropriate to accessing the
	 * given path.
	 * @param request the request being processed
	 * @param filePath the path being accessed
	 * @return the File object
	 */
	public File createFile(final HTTPRequest request, final String filePath);
}
