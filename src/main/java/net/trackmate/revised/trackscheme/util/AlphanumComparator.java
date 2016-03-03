package net.trackmate.revised.trackscheme.util;

import java.util.Comparator;

/**
 * The Alphanum Algorithm is an improved sorting algorithm for strings
 * containing numbers. Instead of sorting numbers in ASCII order like a standard
 * sort, this algorithm sorts numbers in numeric order.
 * <p>
 * The Alphanum Algorithm is discussed <a
 * href=http://www.DaveKoelle.com>here</a>
 * <p>
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or any later version.
 * <p>
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 * <p>
 * This is an updated version with enhancements made by Daniel Migowski, Andre
 * Bogus, and David Koelle
 */
public class AlphanumComparator implements Comparator< String >
{
	public static final AlphanumComparator instance = new AlphanumComparator();

	// Singleton
	private AlphanumComparator()
	{}

	@Override
	public int compare( final String s1, final String s2 )
	{
		return AlphanumCompare.compare( s1, s2 );
	}
}
