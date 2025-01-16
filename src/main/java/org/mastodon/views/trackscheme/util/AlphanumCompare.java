/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2025 Tobias Pietzsch, Jean-Yves Tinevez
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.views.trackscheme.util;

/*
 * The Alphanum Algorithm is an improved sorting algorithm for strings
 * containing numbers.  Instead of sorting numbers in ASCII order like
 * a standard sort, this algorithm sorts numbers in numeric order.
 *
 * The Alphanum Algorithm is discussed at http://www.DaveKoelle.com
 *
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */
public class AlphanumCompare
{
	private AlphanumCompare()
	{}

	private static boolean isDigit( final char ch )
	{
		return ch >= 48 && ch <= 57;
	}

	/**
	 * Length of string is passed in for improved efficiency (only need to
	 * calculate it once)
	 **/
	private static String getChunk( final String s, final int slength, int marker )
	{
		final StringBuilder chunk = new StringBuilder();
		char c = s.charAt( marker );
		chunk.append( c );
		marker++;
		if ( isDigit( c ) )
		{
			while ( marker < slength )
			{
				c = s.charAt( marker );
				if ( !isDigit( c ) )
					break;
				chunk.append( c );
				marker++;
			}
		}
		else
		{
			while ( marker < slength )
			{
				c = s.charAt( marker );
				if ( isDigit( c ) )
					break;
				chunk.append( c );
				marker++;
			}
		}
		return chunk.toString();
	}

	public static int compare( final String s1, final String s2 )
	{

		int thisMarker = 0;
		int thatMarker = 0;
		final int s1Length = s1.length();
		final int s2Length = s2.length();

		while ( thisMarker < s1Length && thatMarker < s2Length )
		{
			final String thisChunk = getChunk( s1, s1Length, thisMarker );
			thisMarker += thisChunk.length();

			final String thatChunk = getChunk( s2, s2Length, thatMarker );
			thatMarker += thatChunk.length();

			// If both chunks contain numeric characters, sort them numerically
			int result = 0;
			if ( isDigit( thisChunk.charAt( 0 ) ) && isDigit( thatChunk.charAt( 0 ) ) )
			{
				// Simple chunk comparison by length.
				final int thisChunkLength = thisChunk.length();
				result = thisChunkLength - thatChunk.length();
				// If equal, the first different number counts
				if ( result == 0 )
				{
					for ( int i = 0; i < thisChunkLength; i++ )
					{
						result = thisChunk.charAt( i ) - thatChunk.charAt( i );
						if ( result != 0 )
						{ return result; }
					}
				}
			}
			else
			{
				result = thisChunk.compareTo( thatChunk );
			}

			if ( result != 0 )
				return result;
		}

		return s1Length - s2Length;
	}
}
