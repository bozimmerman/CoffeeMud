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
 * Manages FileCache objects
 * @author Bo Zimmerman
 *
 */
public interface FileCacheManager
{
	public static enum CompressionType {NONE, GZIP, DEFLATE}
	
	/**
	 * The publically accessible method for getting data from a file (or
	 * potentially from the cache.  You can also pass in a one dimensional
	 * eTay holder.  If the holder contains a valid tag that matches the
	 * file requested, a 304 not modified exception is thrown.  Otherwise,
	 * the holder is populated with the valid eTag when the byte[] buffer
	 * is returned.
	 * @param pageFile the local file to fetch
	 * @param eTag the r/w eTag holder, a one dimensional string array
	 * @return the byte[] buffer of the file to send to the client
	 * @throws HTTPException either 304 or 404
	 */
	public DataBuffers getFileData(File pageFile, final String[] eTag) throws HTTPException;
	
	/**
	 * The publically accessible method for either compressing file data, or
	 * potentially from the cache. 
	 * @param pageFile the file that is being compressed
	 * @param type the type of compression to look for
	 * @param uncompressedBytes the uncompressed file bytes
	 * @return the byte[] buffer of the file after compression
	 * @throws HTTPException either 304 or 404
	 */
	public DataBuffers compressFileData(final File pageFile, final CompressionType type, final DataBuffers uncompressedBytes) throws HTTPException;
}
