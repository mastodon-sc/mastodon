package net.trackmate.revised.model.mamut;

import java.io.File;
import java.io.IOException;

import net.imglib2.RealLocalizable;
import net.trackmate.graph.ReadOnlyGraph;
import net.trackmate.revised.model.AbstractModel;
import net.trackmate.spatial.SpatioTemporalIndex;
import net.trackmate.spatial.SpatioTemporalIndexImp;

/**
 * A model built to manage a graph of {@link Spot}s and {@link Link}s.
 * <p>
 * Spots are the graph vertices. They are {@link RealLocalizable} 3D ellipsoids,
 * whose volume is specified through a covariance matrix. Graph edges are plain
 * links between spots.
 * <p>
 * On top of a graph structure for the spots, this model manages a
 * {@link SpatioTemporalIndex}, that can slice the model at some given
 * time-points.
 * <p>
 * The model graph is only exposed as a {@link ReadOnlyGraph}. All updates to
 * the model graph are done through {@link Model}. This includes vertex and edge
 * attribute changes (although this currently cannot be enforced through
 * {@link ReadOnlyGraph}).
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class Model extends AbstractModel< ModelGraph, Spot, Link >
{
	/*
	 * SpatioTemporalIndex of model spots
	 */
	private final SpatioTemporalIndex< Spot > index;

	private final UndoRecorder undoRecorder;

	public Model()
	{
		super( new ModelGraph() );
		index = new SpatioTemporalIndexImp<>( modelGraph, modelGraph.getVertexPool() );
		undoRecorder = new UndoRecorder( modelGraph, getGraphIdBimap() );
	}

	/**
	 * Creates and adds a new spot to this model.
	 *
	 * @param timepointId
	 *            the time-point id to add the spot to in the spatio-temporal
	 *            index.
	 * @param pos
	 *            the position of the spot.
	 * @param cov
	 *            the covariance matrix that determines the shape of the
	 *            ellipsoid, as a {@code double[][]} (line, column). Since
	 *            the covariance matrix is symmetric, only the top-left of the
	 *            specified matrix is read.
	 * @param ref
	 *            a spot reference, used to create the spot.
	 * @return the spot created.
	 */
	public Spot addSpot( final int timepointId, final double[] pos, final double[][] cov, final Spot ref )
	{
		return modelGraph.notifyVertexAdded( modelGraph.addVertex( ref ).init( timepointId, pos, cov ) );
	}

	/**
	 * Creates and adds a new link between two spots to this model.
	 * <p>
	 * Links are <em>directed edges</em> in the graph, so the source spot and the
	 * target spot matters, depending on the convention for this model
	 * application.
	 *
	 * @param source
	 *            the source spot the link.
	 * @param target
	 *            the target spot the link.
	 * @param ref
	 *            a {@link Link} reference, used to create the link.
	 * @return the link created.
	 */
	public Link addLink( final Spot source, final Spot target, final Link ref )
	{
		return modelGraph.addEdge( source, target, ref );
	}

	/**
	 * Clears this model and loads the model from the specified raw file.
	 *
	 * @param file
	 *            the raw file to load.
	 * @throws IOException
	 *             if an I/O error occurs while reading the file.
	 */
	public void loadRaw( final File file ) throws IOException
	{
		super.loadRaw( file, ModelSerializer.getInstance() );
	}

	/**
	 * Saves this model to the specified raw file.
	 *
	 * @param file
	 *            the raw file to save.
	 * @throws IOException
	 *             if an I/O error occurs while writing the file.
	 */
	public void saveRaw( final File file ) throws IOException
	{
		super.saveRaw( file, ModelSerializer.getInstance() );
	}

	/**
	 * Exposes the spatio-temporal index of this model.
	 *
	 * @return the spatio-temporal index.
	 */
	public SpatioTemporalIndex< Spot > getSpatioTemporalIndex()
	{
		return index;
	}
}
