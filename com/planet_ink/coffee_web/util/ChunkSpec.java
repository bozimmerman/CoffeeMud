package com.planet_ink.coffee_web.util;

import java.util.HashSet;
import java.util.Set;

import com.planet_ink.coffee_web.http.MIMEType;

/*
   Copyright 2014-2018 Bo Zimmerman

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
 * A wrapper for chunked encoding specification
 * @author Bo Zimmerman
 *
 */
public class ChunkSpec
{
	private final	int				chunkSize;
	private final	Set<MIMEType>	mimeTypes;
	private final	long			minFileSize;

	/**
	 * Create a ChunkSpec object for a particular path/domain. 
	 * @param chunkSize the default size for each chunk, or smaller
	 * @param mimeTypes null for all mimetypes, or a list of allowed types
	 * @param minFileSize the minimum payload size to produce chunking, or 0 for all
	 */
	public ChunkSpec(int chunkSize, Set<MIMEType> mimeTypes, long minFileSize)
	{
		this.chunkSize = chunkSize;
		this.mimeTypes = new HashSet<MIMEType>();
		this.mimeTypes.addAll(mimeTypes);
		this.minFileSize = minFileSize;
	}

	public int getChunkSize() 
	{
		return chunkSize;
	}

	public Set<MIMEType> getMimeTypes() 
	{
		return mimeTypes;
	}

	public long getMinFileSize() 
	{
		return minFileSize;
	}
}
