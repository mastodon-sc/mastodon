/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2025 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.model;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.mastodon.feature.FeatureModel;
import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.io.GraphSerializer;
import org.mastodon.graph.io.RawGraphIO.FileIdToGraphMap;
import org.mastodon.graph.io.RawGraphIO.GraphToFileIdMap;
import org.mastodon.graph.ref.AbstractListenableEdge;
import org.mastodon.mamut.io.project.MamutProject;
import org.mastodon.model.tag.DefaultTagSetModel;
import org.mastodon.model.tag.RawTagSetModelIO;
import org.mastodon.model.tag.TagSetModel;
import org.mastodon.spatial.SpatioTemporalIndex;
import org.mastodon.spatial.SpatioTemporalIndexImp;
import org.mastodon.spatial.SpatioTemporalIndexImpRebuilderThread;

/**
 * Base class for models that manage a graph of vertices and edges.
 * <p>
 * The vertices inherit from {@link AbstractSpot}, which provides spatial and
 * temporal location. So this class has methods related to the spatio-temporal
 *
 * @author Tobias Pietzsch
 * @param <MG>
 *            the model-graph used in this model.
 * @param <V>
 *            the type of vertices in the model-graph.
 * @param <E>
 *            the type of edges in the model-graph.
 */
public abstract class AbstractModel<
		MG extends AbstractModelGraph< MG, ?, ?, V, E, ? >,
		V extends AbstractSpot< V, E, ?, ?, MG >,
		E extends AbstractListenableEdge< E, V, ?, ? > >
{

	protected final FeatureModel featureModel;

	protected final DefaultTagSetModel< V, E > tagSetModel;

	protected final MG modelGraph;

	private final SpatioTemporalIndex< V > index;

	protected final ReentrantReadWriteLock lock;

	protected final String spaceUnits;

	protected final String timeUnits;

	protected AbstractModel( final MG modelGraph, final String spaceUnits, final String timeUnits )
	{
		this.modelGraph = modelGraph;
		this.spaceUnits = spaceUnits;
		this.timeUnits = timeUnits;
		this.featureModel = new FeatureModel();
		this.tagSetModel = new DefaultTagSetModel<>( modelGraph );

		final SpatioTemporalIndexImp< V, E > theIndex = new SpatioTemporalIndexImp<>( modelGraph, getGraphIdBimap().vertexIdBimap() );
		/*
		 * Every 1 second, rebuild spatial indices with more than 100
		 * modifications
		 */
		new SpatioTemporalIndexImpRebuilderThread( "Rebuild spatial indices", theIndex, 100, 1000, true ).start();
		index = theIndex;
		lock = modelGraph.getLock();
	}

	/**
	 * Exposes the graph managed by this model.
	 *
	 * @return the graph.
	 */
	public MG getGraph()
	{
		return modelGraph;
	}

	/**
	 * Exposes the bidirectional map between vertices and their id, and between
	 * edges and their id.
	 *
	 * @return the bidirectional id map.
	 */
	public GraphIdBimap< V, E > getGraphIdBimap()
	{
		return modelGraph.idmap;
	}


	public FeatureModel getFeatureModel()
	{
		return featureModel;
	}

	public TagSetModel< V, E > getTagSetModel()
	{
		return tagSetModel;
	}

	/**
	 * Exposes the spatio-temporal index of this model.
	 *
	 * @return the spatio-temporal index.
	 */
	public SpatioTemporalIndex< V > getSpatioTemporalIndex()
	{
		return index;
	}

	public abstract GraphSerializer< V, E > getGraphSerializer();

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
	public FileIdToGraphMap< V, E > loadRaw( final MamutProject.ProjectReader reader ) throws IOException
	{
		final FileIdToGraphMap< V, E > idmap = modelGraph.loadRaw( reader.getRawModelInputStream(), getGraphSerializer() );

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
	 * Saves this model to the specified project folder.
	 *
	 * @param writer
	 *            writer to save the raw project files.
	 * @return the {@link GraphToFileIdMap} object generated by saving the model
	 *         graph.
	 * @throws IOException
	 *             if an I/O error occurs while writing the file.
	 */
	public GraphToFileIdMap< V, E > saveRaw( final MamutProject.ProjectWriter writer ) throws IOException
	{
		final GraphToFileIdMap< V, E > idmap = modelGraph.saveRaw( writer.getRawModelOutputStream(), getGraphSerializer() );

		try (
				final OutputStream fos = writer.getRawTagsOutputStream();
				final ObjectOutputStream oos = new ObjectOutputStream( new BufferedOutputStream( fos, 1024 * 1024 ) ))
		{
			RawTagSetModelIO.write( tagSetModel, idmap, oos );
		}

		return idmap;
	}
}
