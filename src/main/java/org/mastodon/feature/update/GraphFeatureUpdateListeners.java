package org.mastodon.feature.update;

import java.util.ArrayList;
import java.util.List;

import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureModel.FeatureModelListener;
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

	private static final class MyVertexPropertyChangeListener< V extends Vertex< E >, E extends Edge< V > > implements PropertyChangeListener< V >, FeatureModelListener
	{

		private final FeatureModel featureModel;

		private final Class< V > vertexClass;

		private final Class< E > edgeClass;

		private final List< Feature< E > > edgeFeatures;

		private final List< Feature< V > > vertexFeatures;

		public MyVertexPropertyChangeListener( final FeatureModel featureModel, final Class< V > vertexClass, final Class< E > edgeClass )
		{
			this.featureModel = featureModel;
			this.vertexClass = vertexClass;
			this.edgeClass = edgeClass;
			this.edgeFeatures = new ArrayList<>();
			this.vertexFeatures = new ArrayList<>();
			featureModelChanged();
			featureModel.listeners().add( this );
		}

		@Override
		public void propertyChanged( final V v )
		{
			vertexFeatures.forEach( f -> f.invalidate( v ) );
			for ( final Feature< E > f : edgeFeatures )
				v.edges().forEach( e -> f.invalidate( e ) );
		}

		@Override
		public void featureModelChanged()
		{
			featuresOfTarget( featureModel, vertexClass, vertexFeatures );
			featuresOfTarget( featureModel, edgeClass, edgeFeatures );
		}

		@SuppressWarnings( "unchecked" )
		private static < O > void featuresOfTarget(
				final FeatureModel featureModel,
				final Class< O > targetClass,
				final List< Feature< O > > featureList )
		{
			featureList.clear();
			featureModel.getFeatureSpecs()
					.stream()
					.filter( fs -> fs.getTargetClass().equals( targetClass ) )
					.map( fs -> featureList.add( ( Feature< O > ) fs ) );
		}
	}
}
