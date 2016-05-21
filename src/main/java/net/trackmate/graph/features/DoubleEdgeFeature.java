package net.trackmate.graph.features;

import gnu.trove.map.TObjectDoubleMap;
import net.trackmate.graph.CollectionUtils;
import net.trackmate.graph.Edge;
import net.trackmate.graph.EdgeFeature;
import net.trackmate.graph.FeatureCleanup;
import net.trackmate.graph.FeatureRegistry.DuplicateKeyException;
import net.trackmate.graph.FeatureValue;
import net.trackmate.graph.GraphFeatures;
import net.trackmate.graph.ReadOnlyGraph;
import net.trackmate.graph.features.DoubleEdgeFeature.DoubleFeatureValue;

/**
 * A {@code double}-valued {@link EdgeFeature}.
 * <p>
 * To use features, create exactly one {@link EdgeFeature} object for each
 * feature you want to use.
 *
 * <pre>
 * <code>
 *	public static final DoubleEdgeFeature&lt;E&gt; DISP = new DoubleEdgeFeature&lt;&gt;("displacement");
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
 * @param <E>
 *            the edge type
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 * @author Jean-Yves Tinevez &lt;jeanyves.tinevez@gmail.com&gt;
 */
public final class DoubleEdgeFeature< E extends Edge< ? > > extends EdgeFeature< TObjectDoubleMap< E >, E, DoubleFeatureValue< E > >
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
	 *             if a {@link EdgeFeature} with the same {@code name} already
	 *             exists.
	 */
	public DoubleEdgeFeature( final String name, final double noEntryValue ) throws DuplicateKeyException
	{
		super( name );
		this.noEntryValue = noEntryValue;
	}

	@Override
	protected TObjectDoubleMap< E > createFeatureMap( final ReadOnlyGraph< ?, E > graph )
	{
		return CollectionUtils.createEdgeDoubleMap( graph, noEntryValue, graph.vertices().size() );
	}

	@Override
	protected FeatureCleanup< E > createFeatureCleanup( final TObjectDoubleMap< E > featureMap )
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
	public DoubleFeatureValue< E > createFeatureValue( final E edge, final GraphFeatures< ?, E > graphFeatures )
	{
		return new DoubleFeatureValue< >(
				graphFeatures.getEdgeFeature( this ),
				edge,
				new NotifyValueChange< >( graphFeatures, this, edge ) );
	};

	public static final class DoubleFeatureValue< E extends Edge< ? > > implements FeatureValue< Double >
	{
		private final TObjectDoubleMap< E > featureMap;

		private final E edge;

		private final NotifyValueChange< ? > notify;

		protected DoubleFeatureValue( final TObjectDoubleMap< E > featureMap, final E edge, final NotifyValueChange< ? > notify )
		{
			this.featureMap = featureMap;
			this.edge = edge;
			this.notify = notify;
		}

		@Override
		public void set( final Double value )
		{
			notify.notifyBeforeFeatureChange();
			if ( value == null )
				featureMap.remove( edge );
			else
				featureMap.put( edge, value.intValue() );
		}

		public void set( final double value )
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
		public Double get()
		{
			final double d = getDouble();
			return ( d == featureMap.getNoEntryValue() ) ? null : d;
		}

		public double getDouble()
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
	public DoubleUndoFeatureMap< E > createUndoFeatureMap( final TObjectDoubleMap< E > featureMap )
	{
		return new DoubleUndoFeatureMap< >( featureMap, noEntryValue );
	}
}
