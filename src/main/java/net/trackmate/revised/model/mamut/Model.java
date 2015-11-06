package net.trackmate.revised.model.mamut;

import java.io.File;
import java.io.IOException;

import net.trackmate.graph.ReadOnlyGraph;
import net.trackmate.revised.model.AbstractModel;
import net.trackmate.spatial.SpatioTemporalIndex;
import net.trackmate.spatial.SpatioTemporalIndexImp;

/**
 * Manages the model graph.
 * <p>
 * The model graph is only exposed as a {@link ReadOnlyGraph}.
 * All updates to the model graph are done through {@link Model}.
 * This includes vertex and edge attribute changes (although this currently cannot be enforced through {@link ReadOnlyGraph}).
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class Model extends AbstractModel< ModelGraph, Spot, Link >
{
	/*
	 * SpatioTemporalIndex of model spots
	 */
	private final SpatioTemporalIndex< Spot > index;

	public Model()
	{
		super( new ModelGraph() );
		index = new SpatioTemporalIndexImp<>( modelGraph, modelGraph.getVertexPool() );
	}

	public Spot addSpot( final int timepointId, final double[] pos, final double[][] cov, final Spot ref )
	{
		return modelGraph.emitVertexAdded( modelGraph.addVertex( ref ).init( timepointId, pos, cov ) );
	}

	// TODO: move to super class? (then rename addEdge)
	public Link addLink( final Spot source, final Spot target, final Link edge )
	{
		return modelGraph.addEdge( source, target, edge );
	}

	public void loadRaw( final File file ) throws IOException
	{
		super.loadRaw( file, ModelSerializer.getInstance() );
	}

	public void saveRaw( final File file ) throws IOException
	{
		super.saveRaw( file, ModelSerializer.getInstance() );
	}

	public SpatioTemporalIndex< Spot > getSpatioTemporalIndex()
	{
		return index;
	}
}
