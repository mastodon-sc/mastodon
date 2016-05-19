package net.trackmate.graph.features;

import gnu.trove.impl.Constants;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import net.trackmate.graph.CollectionUtils;
import net.trackmate.graph.FeatureValue;
import net.trackmate.graph.GraphFeatures;
import net.trackmate.graph.ReadOnlyGraph;
import net.trackmate.graph.Vertex;
import net.trackmate.graph.VertexFeature;
import net.trackmate.graph.FeatureRegistry.DuplicateKeyException;
import net.trackmate.graph.features.IntVertexFeature.IntFeatureValue;

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
		return CollectionUtils.createVertexIntMap( graph, noEntryValue, graph.vertices().size() );
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

	public static final class IntFeatureValue< V extends Vertex< ? > > implements FeatureValue< Integer >
	{
		private final TObjectIntMap< V > featureMap;

		private final V vertex;

		private final NotifyValueChange< ? > notify;

		protected IntFeatureValue( final TObjectIntMap< V > featureMap, final V vertex, final NotifyValueChange< ? > notify )
		{
			this.featureMap = featureMap;
			this.vertex = vertex;
			this.notify = notify;
		}

		@Override
		public void set( final Integer value )
		{
			notify.notifyBeforeFeatureChange();
			if ( value == null )
				featureMap.remove( vertex );
			else
				featureMap.put( vertex, value.intValue() );
		}

		public void set( final int value )
		{
			notify.notifyBeforeFeatureChange();
			featureMap.put( vertex, value );
		}

		@Override
		public void remove()
		{
			notify.notifyBeforeFeatureChange();
			featureMap.remove( vertex );
		}

		@Override
		public Integer get()
		{
			final int i = getInt();
			return ( i == featureMap.getNoEntryValue() ) ? null : i;
		}

		public int getInt()
		{
			return featureMap.get( vertex );
		}

		@Override
		public boolean isSet()
		{
			return featureMap.containsKey( vertex );
		}
	}

	@Override
	public IntUndoFeatureMap< V > createUndoFeatureMap( final TObjectIntMap< V > featureMap )
	{
		return new IntUndoFeatureMap<>( featureMap, noEntryValue );
	}

	public static final class IntUndoFeatureMap< V > implements UndoFeatureMap< V >
	{
		private static final int NO_ENTRY_KEY = -1;

		private final TObjectIntMap< V > featureMap;

		private final int noEntryValue;

		private final TIntIntMap undoMap;

		protected IntUndoFeatureMap( final TObjectIntMap< V > featureMap, final int noEntryValue )
		{
			this.featureMap = featureMap;
			this.noEntryValue = noEntryValue;
			undoMap = new TIntIntHashMap( Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, NO_ENTRY_KEY, noEntryValue );
		}

		@Override
		public void store( final int undoId, final V vertex )
		{
			final int value = featureMap.get( vertex );
			if ( value != noEntryValue )
				undoMap.put( undoId, value );
		}

		@Override
		public void retrieve( final int undoId, final V vertex )
		{
			final int value = undoMap.get( undoId );
			if ( value != noEntryValue )
				featureMap.put( vertex, value );
			else
				featureMap.remove( vertex );
		}

		@Override
		public void swap( final int undoId, final V vertex )
		{
			final int undoValue = undoMap.get( undoId );
			final int featureValue = featureMap.get( vertex );
			if ( featureValue != noEntryValue )
				undoMap.put( undoId, featureValue );
			else
				undoMap.remove( undoId );
			if ( undoValue != noEntryValue )
				featureMap.put( vertex, undoValue );
			else
				featureMap.remove( vertex );

		}

		@Override
		public void clear( final int undoId )
		{
			undoMap.remove( undoId );
		}
	}
}
