package org.mastodon.graph.features;

import java.util.Map;

import org.mastodon.collection.RefCollection;
import org.mastodon.collection.util.CollectionUtils;
import org.mastodon.graph.FeatureValue;
import org.mastodon.graph.features.FeatureRegistry.DuplicateKeyException;

/**
 * TODO revise javadoc
 *
 * A {@code Object}-valued {@link Feature}.
 * <p>
 * To use features, create exactly one {@link Feature} object for each feature
 * you want to use.
 *
 * <pre>
 * <code>
 *	public static final ObjFeature&lt;V,String&gt; LABEL = new ObjFeature&lt;&gt;("label");
 *	public static final IntFeature&lt;V&gt; ID = new IntFeature&lt;&gt;("id");
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
 * @param <O>
 *            type of object to which feature should be attached.
 * @param <T>
 *            the (feature) value type.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public final class ObjFeature< O, T > extends Feature< Map< O, T >, O, FeatureValue< T > >
{
	/**
	 * Create a new feature.
	 *
	 * @param name
	 *            the unique name of the feature.
	 * @throws DuplicateKeyException
	 *             if a {@link Feature} with the same {@code name} already
	 *             exists.
	 */
	public ObjFeature( final String name ) throws DuplicateKeyException
	{
		super( name );
	}

	@Override
	protected Map< O, T > createFeatureMap( final RefCollection< O > pool )
	{
		return CollectionUtils.createRefObjectMap( pool );
	}

	@Override
	protected FeatureCleanup< O > createFeatureCleanup( final Map< O, T > featureMap )
	{
		return new FeatureCleanup< O >(){
			@Override
			public void delete( final O object )
			{
				featureMap.remove( object );
			}
		};
	};

	@Override
	public FeatureValue< T > createFeatureValue( final O object, final Features< O > features )
	{
		return new ObjFeatureValue<>(
				features.getFeatureMap( this ),
				object,
				new NotifyValueChange<>( features, this, object ) );
	}

	@Override
	public ObjUndoFeatureMap< O, T > createUndoFeatureMap( final Map< O, T > featureMap )
	{
		return new ObjUndoFeatureMap<>( featureMap );
	}
}
