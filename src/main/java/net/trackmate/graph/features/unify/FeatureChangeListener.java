package net.trackmate.graph.features.unify;

/**
 * A listener that is notified when {@link Feature}s of a graph change.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public interface FeatureChangeListener< O >
{
	public void beforeFeatureChange( final Feature< ?, O, ? > feature, O object );
}
