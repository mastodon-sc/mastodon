package net.trackmate.revised.model;

import net.trackmate.graph.ReadOnlyGraph;
import net.trackmate.graph.Vertex;

// Features must have unique names!
public abstract class VertexFeature< M, V extends Vertex< ? >, F extends FeatureValue< ? > >
{
	private final String key;

	public VertexFeature( final String key )
	{
		this.key = key;
	}

	public String getKey()
	{
		return key;
	}

	/*
	 * Following part is for the graph to create feature maps, initialize
	 * features, serialize, etc...
	 */

	protected abstract M createFeatureMap( final ReadOnlyGraph< V, ? > graph );

	protected void addVertex( final V vertex, final M featureMap )
	{}

	protected abstract void deleteVertex( final V vertex, final M featureMap );

	protected abstract F createFeatureValue( M featureMap, V vertex );

	// TODO: for storing feature values for undo/redo
//	protected TIntObjectMap< F > createIdFeatureMap();


	@Override
	public int hashCode()
	{
		return key.hashCode();
	}

	@Override
	public boolean equals( final Object obj )
	{
		return obj instanceof VertexFeature
				&& ( ( VertexFeature< ?, ?, ? > ) obj ).key.equals( key );
	}
}
