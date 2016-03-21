package net.trackmate.revised.ui.selection;

/**
 * TODO
 *
 * @param <V> vertex type.
 * @param <E> edge type.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 * @author Jean-Yves Tinevez
 */
public interface NavigationListener< V, E >
{
	/**
	 * Changes the view managed by the listener to display the specified vertex.
	 *
	 * @param vertex
	 *            the vertex to navigate to.
	 */
	public void navigateToVertex( V vertex );

	/**
	 * Changes the view managed by the listener to display the specified edge.
	 *
	 * @param edge
	 *            the edge to navigate to.
	 */
	public void navigateToEdge( E edge );
}
