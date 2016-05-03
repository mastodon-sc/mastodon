package net.trackmate.graph.features;

import java.util.Map;

import net.trackmate.graph.FeatureRegistry.DuplicateKeyException;
import net.trackmate.graph.FeatureValue;
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
	protected FeatureValue< O > createFeatureValue( final Map< V, O > featureMap, final V vertex )
	{
		return new ObjFeatureValue<>( featureMap, vertex );
	}

	public static final class ObjFeatureValue< V, O > implements FeatureValue< O >
	{
		private final Map< V, O > featureMap;

		private final V vertex;

		protected ObjFeatureValue( final Map< V, O > featureMap, final V vertex )
		{
			this.featureMap = featureMap;
			this.vertex = vertex;
		}

		@Override
		public void set( final O value )
		{
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
}
