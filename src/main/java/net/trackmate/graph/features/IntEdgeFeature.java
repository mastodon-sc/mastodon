package net.trackmate.graph.features;

import gnu.trove.map.TObjectIntMap;
import net.trackmate.collection.util.CollectionUtils;
import net.trackmate.graph.Edge;
import net.trackmate.graph.EdgeFeature;
import net.trackmate.graph.FeatureRegistry.DuplicateKeyException;
import net.trackmate.graph.GraphFeatures;
import net.trackmate.graph.ReadOnlyGraph;

/**
 * A {@code int}-valued {@link EdgeFeature}.
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
 *
 * @author Jean-Yves Tinevez &lt;jeanyves.tinevez@gmail.com&gt;
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public final class IntEdgeFeature< E extends Edge< ? > > extends EdgeFeature< TObjectIntMap< E >, E, IntFeatureValue< E > >
{
	private final int noEntryValue;

	/**
	 * Create a new feature.
	 *
	 * @param name
	 *            the unique name of the feature.
	 * @param noEntryValue
	 *            a {@code int} value that represents null for the Value set.
	 * @throws DuplicateKeyException
	 *             if a {@link EdgeFeature} with the same {@code name} already
	 *             exists.
	 */
	public IntEdgeFeature( final String name, final int noEntryValue ) throws DuplicateKeyException
	{
		super( name );
		this.noEntryValue = noEntryValue;
	}

	@Override
	protected TObjectIntMap< E > createFeatureMap( final ReadOnlyGraph< ?, E > graph )
	{
		return CollectionUtils.createRefIntMap( graph.edges(), noEntryValue, graph.edges().size() );
	}

	@Override
	protected FeatureCleanup< E > createFeatureCleanup( final TObjectIntMap< E > featureMap )
	{
		return new FeatureCleanup< E >()
		{
			@Override
			public void delete( final E edge )
			{
				featureMap.remove( edge );
			}
		};
	}

	@Override
	public IntFeatureValue< E > createFeatureValue( final E edge, final GraphFeatures< ?, E > graphFeatures )
	{
		return new IntFeatureValue<>(
				graphFeatures.getEdgeFeature( this ),
				edge,
				new NotifyValueChange<>( graphFeatures, this, edge ) );
	};

	@Override
	public IntUndoFeatureMap< E > createUndoFeatureMap( final TObjectIntMap< E > featureMap )
	{
		return new IntUndoFeatureMap<>( featureMap, noEntryValue );
	}
}
