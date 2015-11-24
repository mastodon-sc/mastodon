package net.trackmate.revised.ui.selection;

import net.trackmate.graph.Vertex;

/**
 * @author Jean-Yves Tinevez
 */
public interface NavigationListener< V extends Vertex< ? > >
{
	/**
	 * Changes the view managed by the listener to display the specified vertex.
	 *
	 * @param vertex
	 *            the vertex to navigate to.
	 */
	public void navigateToVertex( V vertex );
}
