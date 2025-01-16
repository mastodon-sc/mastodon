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
package org.mastodon.ui.util;

import java.awt.Point;
import java.util.List;

public class RamerDouglasPeucker
{

	public static void simplifyPath( final List< Point > points, final double epsilon )
	{
		if ( points == null || points.size() < 3 )
			return; // No need to simplify if there are fewer than 3 points

		simplifySection( points, 0, points.size() - 1, epsilon );
	}

	/**
	 * Determines if a point should be added to the specified polygon, based on
	 * the Ramer-Douglas-Peucker criterion (perpendicular distance from the last
	 * segment).
	 */
	public static boolean shouldAddPoint( final List< Point > points, final Point newPoint, final double epsilon )
	{
		if ( points.size() < 2 )
			return true;

		// Get the last two points
		final Point lastPoint = points.get( points.size() - 1 );
		final Point secondLastPoint = points.get( points.size() - 2 );

		final double distance = perpendicularDistance( newPoint, secondLastPoint, lastPoint );

		return distance > epsilon;
	}

	private static void simplifySection( final List< Point > points, final int start, final int end, final double epsilon )
	{
		if ( end <= start + 1 )
			return;

		double maxDistance = 0;
		int index = start;

		for ( int i = start + 1; i < end; i++ )
		{
			final double distance = perpendicularDistance( points.get( i ), points.get( start ), points.get( end ) );
			if ( distance > maxDistance )
			{
				index = i;
				maxDistance = distance;
			}
		}

		if ( maxDistance > epsilon )
		{
			// Simplify left half
			simplifySection( points, start, index, epsilon );
			// Simplify right half
			simplifySection( points, index, end, epsilon );
		}
		else
		{
			for ( int i = end - 1; i > start; i-- )
				points.remove( i );
		}
	}

	private static double perpendicularDistance( final Point point, final Point lineStart, final Point lineEnd )
	{
		final double dx = lineEnd.x - lineStart.x;
		final double dy = lineEnd.y - lineStart.y;

		if ( dx == 0 && dy == 0 )
			return point.distance( lineStart );

		// Calculate the perpendicular distance using the cross product method
		final double numerator = Math.abs( dy * point.x - dx * point.y + lineEnd.x * lineStart.y - lineEnd.y * lineStart.x );
		final double denominator = Math.sqrt( dx * dx + dy * dy );

		return numerator / denominator;
	}
}
