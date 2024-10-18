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
