package net.trackmate.graph.features.unify;

/**
 * When a vertex or edge is deleted it must be removed from all feature maps.
 * This can be done by {@link #delete(Object)}
 *
 * @param <O>
 *            object type (an object that has features, i.e., edge or vertex).
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public interface FeatureCleanup< O >
{
	public void delete( final O object );
}
