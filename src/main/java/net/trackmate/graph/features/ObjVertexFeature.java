package net.trackmate.graph.features;

import java.util.Map;

import gnu.trove.impl.Constants;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.trackmate.graph.FeatureRegistry.DuplicateKeyException;
import net.trackmate.graph.FeatureValue;
import net.trackmate.graph.GraphFeatures;
import net.trackmate.graph.ReadOnlyGraph;
import net.trackmate.graph.Vertex;
import net.trackmate.graph.VertexFeature;
import net.trackmate.graph.collection.CollectionUtils;

public final class ObjVertexFeature< V extends Vertex< ? >, O > extends VertexFeature< Map< V, O >, V, FeatureValue< O > >
{
	public ObjVertexFeature( final String name ) throws DuplicateKeyException
	{
		super( name );
	}

	@Override
	protected Map< V, O > createFeatureMap( final ReadOnlyGraph< V, ? > graph )
	{
		return CollectionUtils.createVertexObjectMap( graph );
	}

	@Override
	protected FeatureCleanup< V > createFeatureCleanup( final Map< V, O > featureMap )
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
	protected FeatureValue< O > createFeatureValue( final V vertex, final GraphFeatures< V, ? > graphFeatures )
	{
		return new ObjFeatureValue<>(
				graphFeatures.getVertexFeature( this ),
				vertex,
				new NotifyValueChange<>( graphFeatures, this, vertex ) );
	}

	public static final class ObjFeatureValue< V extends Vertex< ? >, O > implements FeatureValue< O >
	{
		private final Map< V, O > featureMap;

		private final V vertex;

		private final NotifyValueChange< ? > notify;

		protected ObjFeatureValue( final Map< V, O > featureMap, final V vertex, final NotifyValueChange< ? > notify )
		{
			this.featureMap = featureMap;
			this.vertex = vertex;
			this.notify = notify;
		}

		@Override
		public void set( final O value )
		{
			notify.notifyBeforeFeatureChange();
			featureMap.put( vertex, value );
		}

		@Override
		public O get()
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
	public ObjUndoFeatureMap< V, O > createUndoFeatureMap( final Map< V, O > featureMap )
	{
		return new ObjUndoFeatureMap<>( featureMap );
	}

	public static final class ObjUndoFeatureMap< V, O > implements UndoFeatureMap< V >
	{
		private static final int NO_ENTRY_KEY = -1;

		private final Map< V, O > featureMap;

		private final TIntObjectMap< O > undoMap;

		protected ObjUndoFeatureMap( final Map< V, O > featureMap )
		{
			this.featureMap = featureMap;
			undoMap = new TIntObjectHashMap<>( Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, NO_ENTRY_KEY );
		}

		@Override
		public void store( final int undoId, final V vertex )
		{
			final O value = featureMap.get( vertex );
			if ( value != null )
				undoMap.put( undoId, value );
		}

		@Override
		public void retrieve( final int undoId, final V vertex )
		{
			final O value = undoMap.get( undoId );
			if ( value != null )
				featureMap.put( vertex, value );
			// TODO: CLEAR feature if not present in undoMap !?
			// TODO
			// TODO
			// TODO
			// TODO
			// TODO
			// TODO
			// TODO
			// TODO
			// TODO
			// TODO
		}

		@Override
		public void clear( final int undoId )
		{
			undoMap.remove( undoId );
		}
	}
}
