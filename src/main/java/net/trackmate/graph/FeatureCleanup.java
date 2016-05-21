package net.trackmate.graph;

/**
 * When a vertex or edge is deleted it must be removed from all feature maps.
 * This can be done by {@link #delete(Object)}
 *
 * @param <K>
 *            object type (vertex or edge).
 */
public interface FeatureCleanup< K >
{
	public void delete( final K object );
}