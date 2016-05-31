package net.trackmate.graph.features;

import java.util.Map;

import net.trackmate.collection.util.CollectionUtils;
import net.trackmate.graph.Edge;
import net.trackmate.graph.EdgeFeature;
import net.trackmate.graph.FeatureRegistry.DuplicateKeyException;
import net.trackmate.graph.features.unify.FeatureCleanup;
import net.trackmate.graph.FeatureValue;
import net.trackmate.graph.GraphFeatures;
import net.trackmate.graph.ReadOnlyGraph;

/**
 * A {@code Object}-valued {@link EdgeFeature}.
 * <p>
 * To use features, create exactly one {@link EdgeFeature} object for each
 * feature you want to use.
 *
 * <pre>
 * <code>
 *	public static final ObjEdgeFeature&lt;E,String&gt; LABEL = new ObjEdgeFeature&lt;&gt;("label");
 *	public static final IntEdgeFeature&lt;E&gt; ID = new IntEdgeFeature&lt;&gt;("id");
 * </code>
 * </pre>
 *
 * Then use these objects as keys to access feature values.
 *
 * <pre>
 * <code>
 *	String label = edge.feature(LABEL).get();
 *	edge.feature(ID).set(10);
 * </code>
 * </pre>
 *
 * @param <E>
 *            the edge type
 * @param <T>
 *            the (feature) value type.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public final class ObjEdgeFeature< E extends Edge< ? >, T > extends EdgeFeature< Map< E, T >, E, FeatureValue< T > >
{
	/**
	 * Create a new feature.
	 *
	 * @param name
	 *            the unique name of the feature.
	 * @throws DuplicateKeyException
	 *             if a {@link EdgeFeature} with the same {@code name} already
	 *             exists.
	 */
	public ObjEdgeFeature( final String name ) throws DuplicateKeyException
	{
		super( name );
	}

	@Override
	protected Map< E, T > createFeatureMap( final ReadOnlyGraph< ?, E > graph )
	{
		return CollectionUtils.createRefObjectMap( graph.edges() );
	}

	@Override
	protected FeatureCleanup< E > createFeatureCleanup( final Map< E, T > featureMap )
	{
		return new FeatureCleanup< E >(){
			@Override
			public void delete( final E vertex )
			{
				featureMap.remove( vertex );
			}
		};
	};

	@Override
	public FeatureValue< T > createFeatureValue( final E edge, final GraphFeatures< ?, E > graphFeatures )
	{
		return new ObjFeatureValue<>(
				graphFeatures.getEdgeFeature( this ),
				edge,
				new NotifyValueChange<>( graphFeatures, this, edge ) );
	}

	@Override
	public ObjUndoFeatureMap< E, T > createUndoFeatureMap( final Map< E, T > featureMap )
	{
		return new ObjUndoFeatureMap<>( featureMap );
	}
}
