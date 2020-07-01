package org.mastodon.ui.coloring;

/**
 * Interface that can associate colors to objects.
 *
 * @param <T>
 *            the type of objects to color.
 *
 * @author Jean-Yves Tinevez.
 * @author Tobias Pietzsch
 */
public interface ColorGenerator< T >
{
	/**
	 * Gets the color for the specified object (ARGB bytes packed into
	 * {@code int}).
	 * <p>
	 * The special value {@code 0x00000000} is used to denote that no color is
	 * assigned to the object (which should be drawn in default color then).
	 *
	 * @param object
	 *            the object.
	 * @return a color (as ARGB bytes packed into {@code int}).
	 */
	public int color( T object );
}
