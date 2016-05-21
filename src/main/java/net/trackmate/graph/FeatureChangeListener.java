package net.trackmate.graph;

/**
 * A listener that is notified when features ({@link VertexFeature}, {@code EdgeFeature}) of a graph change.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public interface FeatureChangeListener< V extends Vertex< E >, E extends Edge< V > >
{
	public void beforeFeatureChange( final VertexFeature< ?, V, ? > feature, V vertex );

	public void beforeFeatureChange( final EdgeFeature< ?, E, ? > feature, E edge );

}
