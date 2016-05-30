package net.trackmate.graph.features;

import gnu.trove.map.TObjectDoubleMap;
import net.trackmate.collection.util.CollectionUtils;
import net.trackmate.graph.FeatureRegistry.DuplicateKeyException;
import net.trackmate.graph.GraphFeatures;
import net.trackmate.graph.ReadOnlyGraph;
import net.trackmate.graph.Vertex;
import net.trackmate.graph.VertexFeature;

/**
 * A {@code double}-valued {@link VertexFeature}.
 * <p>
 * To use features, create exactly one {@link VertexFeature} object for each
 * feature you want to use.
 *
 * <pre>
 * <code>
 *	public static final DoubleVertexFeature&lt;E&gt; DISP = new DoubleVertexFeature&lt;&gt;("displacement");
 * </code>
 * </pre>
 *
 * Then use these objects as keys to access feature values. In the case of a
 * {@code double}-valued feature:
 *
 * <pre>
 * <code>
 *	vertex.feature(DISP).set(3.56);
 * </code>
 * </pre>
 *
 * @param <V>
 *            the vertex type
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 * @author Jean-Yves Tinevez &lt;jeanyves.tinevez@gmail.com&gt;
 */
public final class DoubleVertexFeature< V extends Vertex< ? > > extends VertexFeature< TObjectDoubleMap< V >, V, DoubleFeatureValue< V > >
{
	private final double noEntryValue;

	/**
	 * Create a new feature.
	 *
	 * @param name
	 *            the unique name of the feature.
	 * @param noEntryValue
	 *            a {@code double} value that represents null for the Value set.
	 * @throws DuplicateKeyException
	 *             if a {@link VertexFeature} with the same {@code name} already
	 *             exists.
	 */
	public DoubleVertexFeature( final String name, final double noEntryValue ) throws DuplicateKeyException
	{
		super( name );
		this.noEntryValue = noEntryValue;
	}

	@Override
	protected TObjectDoubleMap< V > createFeatureMap( final ReadOnlyGraph< V, ? > graph )
	{
		return CollectionUtils.createRefDoubleMap( graph.vertices(), noEntryValue, graph.vertices().size() );
	}

	@Override
	protected FeatureCleanup< V > createFeatureCleanup( final TObjectDoubleMap< V > featureMap )
	{
		return new FeatureCleanup< V >()
		{
			@Override
			public void delete( final V edge )
			{
				featureMap.remove( edge );
			}
		};
	}

	@Override
	public DoubleFeatureValue< V > createFeatureValue( final V vertex, final GraphFeatures< V, ? > graphFeatures )
	{
		return new DoubleFeatureValue<>(
				graphFeatures.getVertexFeature( this ),
				vertex,
				new NotifyValueChange<>( graphFeatures, this, vertex ) );
	};

	@Override
	public DoubleUndoFeatureMap< V > createUndoFeatureMap( final TObjectDoubleMap< V > featureMap )
	{
		return new DoubleUndoFeatureMap<>( featureMap, noEntryValue );
	}
}
