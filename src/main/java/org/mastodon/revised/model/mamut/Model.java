package org.mastodon.revised.model.mamut;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.properties.PropertyMap;
import org.mastodon.revised.model.AbstractModel;
import org.mastodon.revisedundo.GraphUndoRecorder;
import org.mastodon.revisedundo.UndoPointMarker;
import org.mastodon.revisedundo.attributes.AttributeSerializer;
import org.mastodon.spatial.SpatioTemporalIndex;
import org.mastodon.spatial.SpatioTemporalIndexImp;

import net.imglib2.RealLocalizable;

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

	private final GraphUndoRecorder< Spot, Link > undoRecorder;

	public Model()
	{
		super( new ModelGraph() );
		index = new SpatioTemporalIndexImp<>( modelGraph, modelGraph.idmap().vertexIdBimap() );

		final int initialCapacity = 1024;

		final AttributeSerializer< Spot > vertexSerializer = ModelSerializer.getInstance().getVertexSerializer();
		final AttributeSerializer< Link > edgeSerializer = ModelSerializer.getInstance().getEdgeSerializer();
//		final List< Attribute< Spot > > vertexAttributes = modelGraph.vertexAttributes();
//		final List< Attribute< Link > > edgeAttributes = modelGraph.edgeAttributes();
		final List< PropertyMap< Spot, ? > > vertexProperties = new ArrayList<>();
		vertexProperties.add( modelGraph.VERTEX_LABEL );
		final List< PropertyMap< Link, ? > > edgeProperties = new ArrayList<>();

		undoRecorder = new GraphUndoRecorder<>(
				initialCapacity,
				modelGraph,
				modelGraph.idmap(),
				vertexSerializer,
				edgeSerializer,
				modelGraph.vertexAttributes(),
				modelGraph.edgeAttributes(),
				vertexProperties,
				edgeProperties );
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
		modelGraph.saveRaw( file, ModelSerializer.getInstance() );
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
