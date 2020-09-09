package org.mastodon.feature.update;

import java.util.List;
import java.util.stream.Collectors;

import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureModel;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.properties.PropertyChangeListener;

/**
 * Listeners for incremental changes.
 */
public class GraphFeatureUpdateListeners
{

	/**
	 * Returns a new {@link PropertyChangeListener} that will remove vertices
	 * and their neighbor edges from a feature model if their property are
	 * modified.
	 *
	 * @return a new {@link PropertyChangeListener}.
	 */
	public static < V extends Vertex< E >, E extends Edge< V > > PropertyChangeListener< V > vertexPropertyListener( final FeatureModel featureModel, final Class< V > vertexClass, final Class< E > edgeClass )
	{
		return new MyVertexPropertyChangeListener<>( featureModel, vertexClass, edgeClass );
	}

	private static final class MyVertexPropertyChangeListener< V extends Vertex< E >, E extends Edge< V > > implements PropertyChangeListener< V >
	{

		private final FeatureModel featureModel;

		private final Class< V > vertexClass;

		private final Class< E > edgeClass;

		public MyVertexPropertyChangeListener( final FeatureModel featureModel, final Class< V > vertexClass, final Class< E > edgeClass )
		{
			this.featureModel = featureModel;
			this.vertexClass = vertexClass;
			this.edgeClass = edgeClass;
		}

		@Override
		public void propertyChanged( final V v )
		{
			featuresOfTarget( featureModel, vertexClass ).forEach( f -> f.remove( v ) );
			for ( final Feature< E > f : featuresOfTarget( featureModel, edgeClass ) )
				v.edges().forEach( e -> f.remove( e ) );
		}
	}

	private static < O > List< Feature< O > > featuresOfTarget( final FeatureModel featureModel, final Class< O > targetClass )
	{
		final List< ? > f = featureModel.getFeatureSpecs().stream()
				.filter( fs -> fs.getTargetClass().equals( targetClass ) )
				.map( fs -> featureModel.getFeature( fs ) ).collect( Collectors.toList() );
		@SuppressWarnings( "unchecked" )
		final List< Feature< O > > typed = ( List< Feature< O > > ) f;
		return typed;
	}
}
