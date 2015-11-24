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

	/**
	 * Returns <code>true</code> when this listener belongs to the specified
	 * group.
	 *
	 * @param group
	 *            the group.
	 * @return <code>true</code> if this listener belongs to the group.
	 */
	public boolean isInGroup( int group );
}
