package org.mastodon.revised.model.mamut;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.io.RawGraphIO.GraphToFileIdMap;
import org.mastodon.io.ObjectToFileIdMap;
import org.mastodon.properties.Property;
import org.mastodon.revised.model.AbstractModel;
import org.mastodon.revised.model.feature.DefaultFeatureModel;
import org.mastodon.revised.model.feature.FeatureModel;
import org.mastodon.spatial.SpatioTemporalIndex;
import org.mastodon.spatial.SpatioTemporalIndexImp;
import org.mastodon.undo.GraphUndoRecorder;
import org.mastodon.undo.UndoPointMarker;

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

	private final FeatureModel featureModel;

	public Model()
	{
		super( new ModelGraph() );
		index = new SpatioTemporalIndexImp<>( modelGraph, modelGraph.idmap().vertexIdBimap() );

		final int initialCapacity = 1024;

		final List< Property< Spot > > vertexUndoableProperties = new ArrayList<>();
		vertexUndoableProperties.add( modelGraph.getVertexPool().positionProperty() );
		vertexUndoableProperties.add( modelGraph.getVertexPool().covarianceProperty() );
		vertexUndoableProperties.add( modelGraph.getVertexPool().boundingSphereRadiusSquProperty() );
		vertexUndoableProperties.add( modelGraph.getVertexPool().labelProperty() );

		final List< Property< Link > > edgeUndoableProperties = new ArrayList<>();

		featureModel = new DefaultFeatureModel();

		undoRecorder = new GraphUndoRecorder<>(
				initialCapacity,
				modelGraph,
				modelGraph.idmap(),
				ModelSerializer.getInstance().getVertexSerializer(),
				ModelSerializer.getInstance().getEdgeSerializer(),
				vertexUndoableProperties,
				edgeUndoableProperties );
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
		final FileOutputStream fos = new FileOutputStream( file );
		final ObjectOutputStream oos = new ObjectOutputStream( new BufferedOutputStream( fos, 1024 * 1024 ) );

		// Serialize model graph.
		final GraphToFileIdMap< Spot, Link > fileIdMap = modelGraph.writeRaw( oos, ModelSerializer.getInstance() );

		// Serialize feature model.
		final Map< Class< ? >, ObjectToFileIdMap< ? > > fileIdMaps = new HashMap<>();
		fileIdMaps.put( Spot.class, fileIdMap.vertices() );
		fileIdMaps.put( Link.class, fileIdMap.edges() );
		featureModel.writeRaw(oos, fileIdMaps);

		// Close.
		oos.close();
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

	public FeatureModel getFeatureModel()
	{
		return featureModel;
	}

}
