package net.trackmate.revised.model.mamut;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.imglib2.RealLocalizable;
import net.trackmate.graph.ReadOnlyGraph;
import net.trackmate.graph.features.unify.Feature;
import net.trackmate.revised.model.AbstractModel;
import net.trackmate.revised.model.ModelUndoRecorder;
import net.trackmate.spatial.SpatioTemporalIndex;
import net.trackmate.spatial.SpatioTemporalIndexImp;
import net.trackmate.undo.UndoPointMarker;

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
public class Model extends AbstractModel< ModelGraph, Spot, Link > implements UndoPointMarker
{
	/*
	 * SpatioTemporalIndex of model spots
	 */
	private final SpatioTemporalIndex< Spot > index;

	private final List< Feature< ?, Spot, ? > > featuresToSerialize;

	private final ModelUndoRecorder< Spot, Link > undoRecorder;

	public Model()
	{
		super( new ModelGraph() );
		index = new SpatioTemporalIndexImp<>( modelGraph, modelGraph.idmap().vertexIdBimap() );

		featuresToSerialize = new ArrayList<>();
		featuresToSerialize.add( Features.LABEL );

		undoRecorder = new ModelUndoRecorder<>( modelGraph, modelGraph.vertexFeatures(), modelGraph.edgeFeatures(), modelGraph.idmap(), ModelSerializer.getInstance() );
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
		modelGraph.loadRaw( file, ModelSerializer.getInstance() );
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
		modelGraph.saveRaw( file, ModelSerializer.getInstance(), featuresToSerialize );
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

	public void undo()
	{
		undoRecorder.undo();
		modelGraph.notifyGraphChanged();
	}

	public void redo()
	{
		undoRecorder.redo();
		modelGraph.notifyGraphChanged();
	}

	@Override
	public void setUndoPoint()
	{
		undoRecorder.setUndoPoint();
	}
}
