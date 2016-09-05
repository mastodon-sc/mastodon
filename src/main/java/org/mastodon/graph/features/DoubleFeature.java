package org.mastodon.graph.features;

import org.mastodon.collection.RefCollection;
import org.mastodon.collection.RefCollections;
import org.mastodon.graph.features.FeatureRegistry.DuplicateKeyException;

import gnu.trove.map.TObjectDoubleMap;

/**
 * A {@code double}-valued {@link Feature}.
 * <p>
 * To use features, create exactly one {@link Feature} object for each
 * feature you want to use.
 *
 * <pre>
 * <code>
 *	public static final DoubleFeature&lt;E&gt; DISP = new DoubleFeature&lt;&gt;("displacement");
 * </code>
 * </pre>
 *
 * Then use these objects as keys to access feature values. In the case of a
 * {@code double}-valued feature:
 *
 * <pre>
 * <code>
 *	edge.feature(DISP).set(3.56);
 * </code>
 * </pre>
 *
 * @param <O>
 *            type of object to which the feature should be attached.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 * @author Jean-Yves Tinevez &lt;jeanyves.tinevez@gmail.com&gt;
 */
public final class DoubleFeature< O > extends Feature< TObjectDoubleMap< O >, O, DoubleFeatureValue< O > >
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
	 *             if a {@link Feature} with the same {@code name} already
	 *             exists.
	 */
	public DoubleFeature( final String name, final double noEntryValue ) throws DuplicateKeyException
	{
		super( name );
		this.noEntryValue = noEntryValue;
	}

	@Override
	protected TObjectDoubleMap< O > createFeatureMap( final RefCollection< O > pool )
	{
		return RefCollections.createRefDoubleMap( pool, noEntryValue, pool.size() );
	}

	@Override
	protected FeatureCleanup< O > createFeatureCleanup( final TObjectDoubleMap< O > featureMap )
	{
		return new FeatureCleanup< O >()
		{
			@Override
			public void delete( final O object )
			{
				featureMap.remove( object );
			}
		};
	}

	@Override
	public DoubleFeatureValue< O > createFeatureValue( final O object, final Features< O > features )
	{
		return new DoubleFeatureValue<>(
				features.getFeatureMap( this ),
				object,
				new NotifyValueChange<>( features, this, object ) );
	};

	@Override
	public DoubleUndoFeatureMap< O > createUndoFeatureMap( final TObjectDoubleMap< O > featureMap )
	{
		return new DoubleUndoFeatureMap<>( featureMap, noEntryValue );
	}
}
