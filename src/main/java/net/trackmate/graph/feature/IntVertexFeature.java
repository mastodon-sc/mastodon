package net.trackmate.graph.feature;

import gnu.trove.map.TObjectIntMap;
import net.trackmate.graph.ReadOnlyGraph;
import net.trackmate.graph.Vertex;
import net.trackmate.graph.collection.CollectionUtils;
import net.trackmate.graph.feature.FeatureRegistry.DuplicateKeyException;
import net.trackmate.graph.feature.IntVertexFeature.IntFeatureValue;

public final class IntVertexFeature< V extends Vertex< ? > > extends VertexFeature< TObjectIntMap< V >, V, IntFeatureValue< V > >
{
	private final int noEntryValue;

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
	protected void deleteVertex( final V vertex, final TObjectIntMap< V > featureMap )
	{
		featureMap.remove( vertex );
	}

	@Override
	protected IntFeatureValue< V > createFeatureValue( final TObjectIntMap< V > featureMap, final V vertex )
	{
		return new IntFeatureValue<>( featureMap, vertex );
	};

	public static final class IntFeatureValue< V > implements FeatureValue< Integer >
	{
		private final TObjectIntMap< V > featureMap;

		private final V vertex;

		protected IntFeatureValue( final TObjectIntMap< V > featureMap, final V vertex )
		{
			this.featureMap = featureMap;
			this.vertex = vertex;
		}

		@Override
		public void set( final Integer value )
		{
			featureMap.put( vertex, value );
		}

		public void set( final int value )
		{
			featureMap.put( vertex, value );
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
}
