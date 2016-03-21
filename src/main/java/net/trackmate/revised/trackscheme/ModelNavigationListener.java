package net.trackmate.revised.trackscheme;

/**
 * A listener to navigation events. These are typically model navigation events
 * forwarded by a {@link ModelNavigationProperties} implementation
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public interface ModelNavigationListener
{
	public void navigateToVertex( int modelVertexId );

	public void navigateToEdge( int modelEdgeId );
}
