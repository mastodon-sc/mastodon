package net.trackmate.graph.features;

import gnu.trove.map.TObjectIntMap;
import net.trackmate.graph.CollectionUtils;
import net.trackmate.graph.Edge;
import net.trackmate.graph.EdgeFeature;
import net.trackmate.graph.FeatureCleanup;
import net.trackmate.graph.FeatureRegistry.DuplicateKeyException;
import net.trackmate.graph.FeatureValue;
import net.trackmate.graph.GraphFeatures;
import net.trackmate.graph.ReadOnlyGraph;
import net.trackmate.graph.features.IntEdgeFeature.IntFeatureValue;

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
		return CollectionUtils.createEdgeIntMap( graph, noEntryValue, graph.vertices().size() );
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
				new NotifyValueChange< >( graphFeatures, this, edge ) );
	};

	public static final class IntFeatureValue< E extends Edge< ? > > implements FeatureValue< Integer >
	{
		private final TObjectIntMap< E > featureMap;

		private final E edge;

		private final NotifyValueChange< ? > notify;

		protected IntFeatureValue( final TObjectIntMap< E > featureMap, final E edge, final NotifyValueChange< ? > notify )
		{
			this.featureMap = featureMap;
			this.edge = edge;
			this.notify = notify;
		}

		@Override
		public void set( final Integer value )
		{
			notify.notifyBeforeFeatureChange();
			if ( value == null )
				featureMap.remove( edge );
			else
				featureMap.put( edge, value.intValue() );
		}

		public void set( final int value )
		{
			notify.notifyBeforeFeatureChange();
			featureMap.put( edge, value );
		}

		@Override
		public void remove()
		{
			notify.notifyBeforeFeatureChange();
			featureMap.remove( edge );
		}

		@Override
		public Integer get()
		{
			final int i = getInt();
			return ( i == featureMap.getNoEntryValue() ) ? null : i;
		}

		public int getInt()
		{
			return featureMap.get( edge );
		}

		@Override
		public boolean isSet()
		{
			return featureMap.containsKey( edge );
		}
	}

	@Override
	public IntUndoFeatureMap< E > createUndoFeatureMap( final TObjectIntMap< E > featureMap )
	{
		return new IntUndoFeatureMap<>( featureMap, noEntryValue );
	}

}
