package org.mastodon.revised.model.mamut;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import net.imglib2.RealLocalizable;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.io.RawGraphIO.FileIdToGraphMap;
import org.mastodon.graph.io.RawGraphIO.GraphToFileIdMap;
import org.mastodon.labels.LabelSets;
import org.mastodon.project.MamutProject;
import org.mastodon.properties.Property;
import org.mastodon.revised.model.AbstractModel;
import org.mastodon.revised.model.tag.DefaultTagSetModel;
import org.mastodon.revised.model.tag.RawTagSetModelIO;
import org.mastodon.revised.model.tag.TagSetModel;
import org.mastodon.spatial.SpatioTemporalIndex;
import org.mastodon.spatial.SpatioTemporalIndexImp;
import org.mastodon.spatial.SpatioTemporalIndexImpRebuilderThread;
import org.mastodon.undo.GraphUndoRecorder;
import org.mastodon.undo.Recorder;
import org.mastodon.undo.UndoPointMarker;

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

	private final ReentrantReadWriteLock lock;

	private final GraphUndoRecorder< Spot, Link > undoRecorder;

	private final DefaultTagSetModel< Spot, Link > tagSetModel;

	public Model()
	{
		super( new ModelGraph() );
		final SpatioTemporalIndexImp< Spot, Link > theIndex = new SpatioTemporalIndexImp<>( modelGraph, modelGraph.idmap().vertexIdBimap() );
		/*
		 * Every 1 second, rebuild spatial indices with more than 100
		 * modifications
		 */
		new SpatioTemporalIndexImpRebuilderThread( "Rebuild spatial indices", theIndex, 100, 1000, true ).start();
		index = theIndex;
		lock = modelGraph.getLock();


		final int initialCapacity = 1024;

		final List< Property< Spot > > vertexUndoableProperties = new ArrayList<>();
		vertexUndoableProperties.add( modelGraph.getVertexPool().positionProperty() );
		vertexUndoableProperties.add( modelGraph.getVertexPool().covarianceProperty() );
		vertexUndoableProperties.add( modelGraph.getVertexPool().boundingSphereRadiusSquProperty() );
		vertexUndoableProperties.add( modelGraph.getVertexPool().labelProperty() );

		final List< Property< Link > > edgeUndoableProperties = new ArrayList<>();

		tagSetModel = new DefaultTagSetModel<>( getGraph() );
		vertexUndoableProperties.add(
				new DefaultTagSetModel.SerialisationAccess< Spot, Link >( tagSetModel )
				{
					@Override
					protected LabelSets< Spot, Integer > getVertexIdLabelSets()
					{
						return super.getVertexIdLabelSets();
					}
				}.getVertexIdLabelSets() );
		edgeUndoableProperties.add(
				new DefaultTagSetModel.SerialisationAccess< Spot, Link >( tagSetModel )
				{
					@Override
					protected LabelSets< Link, Integer > getEdgeIdLabelSets()
					{
						return super.getEdgeIdLabelSets();
					}
				}.getEdgeIdLabelSets() );

		undoRecorder = new GraphUndoRecorder<>(
				initialCapacity,
				modelGraph,
				modelGraph.idmap(),
				ModelSerializer.getInstance().getVertexSerializer(),
				ModelSerializer.getInstance().getEdgeSerializer(),
				vertexUndoableProperties,
				edgeUndoableProperties );

		final Recorder< DefaultTagSetModel.SetTagSetStructureUndoableEdit > recorder = undoRecorder.createGenericUndoableEditRecorder();
		tagSetModel.setUndoRecorder( recorder );
	}

	/**
	 * Clears this model and loads the model from the specified project folder.
	 *
	 * @param reader
	 *            reader from which to load the raw project files.
	 * @throws IOException
	 *             if an I/O error occurs while reading the file.
	 */
	public void loadRaw( final MamutProject.ProjectReader reader ) throws IOException
	{
		final FileIdToGraphMap< Spot, Link > idmap = modelGraph.loadRaw( reader.getRawModelInputStream(), ModelSerializer.getInstance() );

		try
		{
			final InputStream tis = reader.getRawTagsInputStream();
			final ObjectInputStream ois = new ObjectInputStream( new BufferedInputStream( tis, 1024 * 1024 ) );
//			tagSetModel.pauseListeners(); // TODO
			RawTagSetModelIO.read( tagSetModel, idmap, ois );
			ois.close();
//			tagSetModel.resumeListeners(); // TODO
		}
		catch ( FileNotFoundException e )
		{
//			tagSetModel.clear(); // TODO
		}
	}

	/**
	 * Saves this model to the specified the specified project folder.
	 *
	 * @param writer
	 *            	writer to save the raw project files.
	 * @throws IOException
	 *             if an I/O error occurs while writing the file.
	 */
	public void saveRaw( final MamutProject.ProjectWriter writer ) throws IOException
	{
		final GraphToFileIdMap< Spot, Link > idmap = modelGraph.saveRaw( writer.getRawModelOutputStream(), ModelSerializer.getInstance() );

		final OutputStream fos = writer.getRawTagsOutputStream();
		final ObjectOutputStream oos = new ObjectOutputStream( new BufferedOutputStream( fos, 1024 * 1024 ) );
		RawTagSetModelIO.write( tagSetModel, idmap, oos );
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
		lock.writeLock().lock();
		try
		{
			undoRecorder.undo();
			modelGraph.notifyGraphChanged();
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}

	public void redo()
	{
		lock.writeLock().lock();
		try
		{
			undoRecorder.redo();
			modelGraph.notifyGraphChanged();
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}

	@Override
	public void setUndoPoint()
	{
		undoRecorder.setUndoPoint();
	}

	public TagSetModel< Spot, Link > getTagSetModel()
	{
		return tagSetModel;
	}
}
