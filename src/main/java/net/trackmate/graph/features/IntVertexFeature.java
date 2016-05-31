package net.trackmate.graph.features;

import gnu.trove.map.TObjectIntMap;
import net.trackmate.collection.util.CollectionUtils;
import net.trackmate.graph.FeatureRegistry.DuplicateKeyException;
import net.trackmate.graph.features.unify.FeatureCleanup;
import net.trackmate.graph.GraphFeatures;
import net.trackmate.graph.ReadOnlyGraph;
import net.trackmate.graph.Vertex;
import net.trackmate.graph.VertexFeature;

/**
 * A {@code int}-valued {@link VertexFeature}.
 * <p>
 * To use features, create exactly one {@link VertexFeature} object for each
 * feature you want to use.
 *
 * <pre>
 * <code>
 *	public static final ObjVertexFeature&lt;V,String&gt; LABEL = new ObjVertexFeature&lt;&gt;("label");
 *	public static final IntVertexFeature&lt;V&gt; ID = new IntVertexFeature&lt;&gt;("id");
 * </code>
 * </pre>
 *
 * Then use these objects as keys to access feature values.
 *
 * <pre>
 * <code>
 *	String label = vertex.feature(LABEL).get();
 *	vertex.feature(ID).set(10);
 * </code>
 * </pre>
 *
 * @param <V>
 *            the vertex type
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public final class IntVertexFeature< V extends Vertex< ? > > extends VertexFeature< TObjectIntMap< V >, V, IntFeatureValue< V > >
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
	 *             if a {@link VertexFeature} with the same {@code name} already
	 *             exists.
	 */
	public IntVertexFeature( final String name, final int noEntryValue ) throws DuplicateKeyException
	{
		super( name );
		this.noEntryValue = noEntryValue;
	}

	@Override
	protected TObjectIntMap< V > createFeatureMap( final ReadOnlyGraph< V, ? > graph )
	{
		return CollectionUtils.createRefIntMap( graph.vertices(), noEntryValue, graph.vertices().size() );
	}

	@Override
	protected FeatureCleanup< V > createFeatureCleanup( final TObjectIntMap< V > featureMap )
	{
		return new FeatureCleanup< V >() {
			@Override
			public void delete( final V vertex )
			{
				featureMap.remove( vertex );
			}
		};
	}

	@Override
	public IntFeatureValue< V > createFeatureValue( final V vertex, final GraphFeatures< V, ? > graphFeatures )
	{
		return new IntFeatureValue<>(
				graphFeatures.getVertexFeature( this ),
				vertex,
				new NotifyValueChange<>( graphFeatures, this, vertex ) );
	};

	@Override
	public IntUndoFeatureMap< V > createUndoFeatureMap( final TObjectIntMap< V > featureMap )
	{
		return new IntUndoFeatureMap<>( featureMap, noEntryValue );
	}
}
