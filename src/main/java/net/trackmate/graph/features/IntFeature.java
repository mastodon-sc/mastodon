package net.trackmate.graph.features;

import gnu.trove.map.TObjectIntMap;
import net.trackmate.collection.RefCollection;
import net.trackmate.collection.util.CollectionUtils;
import net.trackmate.graph.features.FeatureRegistry.DuplicateKeyException;

/**
 * A {@code int}-valued {@link Feature}.
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
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public final class IntFeature< O > extends Feature< TObjectIntMap< O >, O, IntFeatureValue< O > >
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
	 *             if a {@link Feature} with the same {@code name} already
	 *             exists.
	 */
	public IntFeature( final String name, final int noEntryValue ) throws DuplicateKeyException
	{
		super( name );
		this.noEntryValue = noEntryValue;
	}

	@Override
	protected TObjectIntMap< O > createFeatureMap( final RefCollection< O > pool )
	{
		return CollectionUtils.createRefIntMap( pool, noEntryValue, pool.size() );
	}

	@Override
	protected FeatureCleanup< O > createFeatureCleanup( final TObjectIntMap< O > featureMap )
	{
		return new FeatureCleanup< O >() {
			@Override
			public void delete( final O object )
			{
				featureMap.remove( object );
			}
		};
	}

	@Override
	public IntFeatureValue< O > createFeatureValue( final O object, final Features< O > features )
	{
		return new IntFeatureValue<>(
				features.getFeatureMap( this ),
				object,
				new NotifyValueChange<>( features, this, object ) );
	};

	@Override
	public IntUndoFeatureMap< O > createUndoFeatureMap( final TObjectIntMap< O > featureMap )
	{
		return new IntUndoFeatureMap<>( featureMap, noEntryValue );
	}
}
