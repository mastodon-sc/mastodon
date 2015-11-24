package net.trackmate.revised.ui.selection;

/**
 * @author Jean-Yves Tinevez
 */
public interface NavigationListener
{
	/**
	 * Changes the view managed by the listener to display the vertex with the
	 * specified id.
	 *
	 * @param modelVertexId
	 *            the vertex model id.
	 */
	public void navigateToVertex( int modelVertexId );
}
