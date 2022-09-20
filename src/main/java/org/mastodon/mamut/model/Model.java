/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2022 Tobias Pietzsch, Jean-Yves Tinevez
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.mamut.model;

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

import org.mastodon.feature.FeatureModel;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.io.RawGraphIO.FileIdToGraphMap;
import org.mastodon.graph.io.RawGraphIO.GraphToFileIdMap;
import org.mastodon.labels.LabelSets;
import org.mastodon.mamut.project.MamutProject;
import org.mastodon.model.AbstractModel;
import org.mastodon.model.tag.DefaultTagSetModel;
import org.mastodon.model.tag.RawTagSetModelIO;
import org.mastodon.model.tag.TagSetModel;
import org.mastodon.properties.Property;
import org.mastodon.spatial.SpatioTemporalIndex;
import org.mastodon.spatial.SpatioTemporalIndexImp;
import org.mastodon.spatial.SpatioTemporalIndexImpRebuilderThread;
import org.mastodon.undo.GraphUndoRecorder;
import org.mastodon.undo.Recorder;
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

	private final ReentrantReadWriteLock lock;

	private final GraphUndoRecorder< Spot, Link > undoRecorder;

	private final FeatureModel featureModel;

	private final DefaultTagSetModel< Spot, Link > tagSetModel;

	private final String spaceUnits;

	private final String timeUnits;

	public Model()
	{
		this( "pixel", "frame" );
	}

	public Model( final String spaceUnits, final String timeUnits )
	{
		super( new ModelGraph() );
		this.spaceUnits = spaceUnits;
		this.timeUnits = timeUnits;
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

		featureModel = new FeatureModel();
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
	 * @return the {@link FileIdToGraphMap} object generated by loading the
	 *         model graph.
	 * @throws IOException
	 *             if an I/O error occurs while reading the file.
	 */
	public FileIdToGraphMap< Spot, Link > loadRaw( final MamutProject.ProjectReader reader ) throws IOException
	{
		final FileIdToGraphMap< Spot, Link > idmap = modelGraph.loadRaw( reader.getRawModelInputStream(), ModelSerializer.getInstance() );

		tagSetModel.pauseListeners();
		tagSetModel.clear();
		try (
				final InputStream tis = reader.getRawTagsInputStream();
				final ObjectInputStream ois = new ObjectInputStream( new BufferedInputStream( tis, 1024 * 1024 ) ))
		{
			RawTagSetModelIO.read( tagSetModel, idmap, ois );
		}
		catch ( final FileNotFoundException e )
		{}
		tagSetModel.resumeListeners();

		return idmap;
	}

	/**
	 * Saves this model to the specified the specified project folder.
	 *
	 * @param writer
	 *            writer to save the raw project files.
	 * @return the {@link GraphToFileIdMap} object generated by saving the model
	 *         graph.
	 * @throws IOException
	 *             if an I/O error occurs while writing the file.
	 */
	public GraphToFileIdMap< Spot, Link > saveRaw( final MamutProject.ProjectWriter writer ) throws IOException
	{
		final GraphToFileIdMap< Spot, Link > idmap = modelGraph.saveRaw( writer.getRawModelOutputStream(), ModelSerializer.getInstance() );

		try (
				final OutputStream fos = writer.getRawTagsOutputStream();
				final ObjectOutputStream oos = new ObjectOutputStream( new BufferedOutputStream( fos, 1024 * 1024 ) ))
		{
			RawTagSetModelIO.write( tagSetModel, idmap, oos );
		}

		return idmap;
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

	public FeatureModel getFeatureModel()
	{
		return featureModel;
	}

	public TagSetModel< Spot, Link > getTagSetModel()
	{
		return tagSetModel;
	}

	public String getSpaceUnits()
	{
		return spaceUnits;
	}

	public String getTimeUnits()
	{
		return timeUnits;
	}
}
