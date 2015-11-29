package net.trackmate.revised.trackscheme;


/**
 * For a specific model attached to the TrackScheme, the
 * {@link ModelNavigationProperties} interface is implemented to forward
 * navigation events from the TrackScheme to some navigation facility of the
 * model.
 * <p>
 * It is required by {@link TrackSchemeNavigation}
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public interface ModelNavigationProperties
{
	public void notifyNavigateToVertex( final int modelVertexId );

	public void forwardNavigationEventsTo( final ModelNavigationListener listener );
}
