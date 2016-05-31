package net.trackmate.graph.features;

import java.util.Map;

import net.trackmate.collection.RefCollection;
import net.trackmate.collection.util.CollectionUtils;
import net.trackmate.graph.FeatureValue;
import net.trackmate.graph.features.unify.Feature;
import net.trackmate.graph.features.unify.FeatureCleanup;
import net.trackmate.graph.features.unify.FeatureRegistry.DuplicateKeyException;
import net.trackmate.graph.features.unify.Features;

/**
 * TODO revise javadoc
 *
 * A {@code Object}-valued {@link VertexFeature}.
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
 * @param <T>
 *            the (feature) value type.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public final class ObjFeature< V, T > extends Feature< Map< V, T >, V, FeatureValue< T > >
{
	/**
	 * Create a new feature.
	 *
	 * @param name
	 *            the unique name of the feature.
	 * @throws DuplicateKeyException
	 *             if a {@link VertexFeature} with the same {@code name} already
	 *             exists.
	 */
	public ObjFeature( final String name ) throws DuplicateKeyException
	{
		super( name );
	}

	@Override
	protected Map< V, T > createFeatureMap( final RefCollection< V > pool )
	{
		return CollectionUtils.createRefObjectMap( pool );
	}

	@Override
	protected FeatureCleanup< V > createFeatureCleanup( final Map< V, T > featureMap )
	{
		return new FeatureCleanup< V >(){
			@Override
			public void delete( final V vertex )
			{
				featureMap.remove( vertex );
			}
		};
	};

	@Override
	public FeatureValue< T > createFeatureValue( final V object, final Features< V > features )
	{
		return new ObjFeatureValue<>(
				features.getFeatureMap( this ),
				object,
				new NotifyValueChange<>( features, this, object ) );
	}

	@Override
	public ObjUndoFeatureMap< V, T > createUndoFeatureMap( final Map< V, T > featureMap )
	{
		return new ObjUndoFeatureMap<>( featureMap );
	}
}
