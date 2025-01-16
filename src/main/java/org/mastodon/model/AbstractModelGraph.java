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
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.mastodon.graph.GraphChangeNotifier;
import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.io.GraphSerializer;
import org.mastodon.graph.io.RawGraphIO;
import org.mastodon.graph.io.RawGraphIO.FileIdToGraphMap;
import org.mastodon.graph.io.RawGraphIO.GraphToFileIdMap;
import org.mastodon.graph.ref.AbstractListenableEdge;
import org.mastodon.graph.ref.AbstractListenableEdgePool;
import org.mastodon.graph.ref.ListenableGraphImp;
import org.mastodon.io.properties.PropertyMapSerializers;
import org.mastodon.io.properties.RawPropertyIO;
import org.mastodon.pool.MappedElement;
import org.mastodon.properties.PropertyChangeListener;
import org.mastodon.properties.PropertyMap;
import org.mastodon.spatial.VertexPositionChangeProvider;
import org.mastodon.spatial.VertexPositionListener;

public class AbstractModelGraph<
		G extends AbstractModelGraph< G, VP, EP, V, E, T >,
		VP extends AbstractSpotPool< V, E, T, G >,
		EP extends AbstractListenableEdgePool< E, V, T >,
		V extends AbstractSpot< V, E, VP, T, G >,
		E extends AbstractListenableEdge< E, V, EP, T >,
		T extends MappedElement >
		extends ListenableGraphImp< VP, EP, V, E, T >
		implements VertexPositionChangeProvider< V >, GraphChangeNotifier
{
	protected final GraphIdBimap< V, E > idmap;

	/**
	 * Subclasses need to add any vertex {@link PropertyMap}s that they want to
	 * be serialized with the graph.
	 */
	protected final PropertyMapSerializers< V > vertexPropertySerializers;

	protected final ReentrantReadWriteLock lock;

	public AbstractModelGraph( final EP edgePool )
	{
		super( edgePool );
		@SuppressWarnings( "unchecked" )
		final G g = ( G ) this;
		vertexPool.linkModelGraph( g );
		idmap = new GraphIdBimap<>( vertexPool, edgePool );
		vertexPropertySerializers = new PropertyMapSerializers<>();
		lock = new ReentrantReadWriteLock();
	}

	/**
	 * Exposes the bidirectional map between vertices and their id, and between
	 * edges and their id.
	 *
	 * @return the bidirectional id map.
	 */
	public GraphIdBimap< V, E > getGraphIdBimap()
	{
		return idmap;
	}

	/**
	 * Clears this model and loads the model from the specified raw file using
	 * the specified serializer.
	 *
	 * @param is
	 *            the raw data to load.
	 *            The stream will be closed when done!
	 * @param serializer
	 *            the serializer used for reading individual vertices.
	 * @return the map from  IDs used in the raw file to vertices/edges.
	 * @throws IOException
	 *             if an I/O error occurs while reading the file.
	 */
	public FileIdToGraphMap< V, E > loadRaw(
			final InputStream is,
			final GraphSerializer< V, E > serializer )
			throws IOException
	{
		final ObjectInputStream ois = new ObjectInputStream( new BufferedInputStream( is, 1024 * 1024 ) );
		pauseListeners();
		clear();
		final FileIdToGraphMap< V, E > fileIdMap = RawGraphIO.read( this, idmap, serializer, ois );
		RawPropertyIO.readPropertyMaps( fileIdMap.vertices(), vertexPropertySerializers, ois );
		// TODO: edge properties
		//		RawFeatureIO.readFeatureMaps( fileIdMap.vertices(), vertexFeatures, ois );
		//		RawFeatureIO.readFeatureMaps( fileIdMap.edges(), edgeFeatures, ois );
		ois.close();
		resumeListeners();

		return fileIdMap;
	}

	/**
	 * Saves this model to the specified raw file using the specified
	 * serializer.
	 *
	 * @param os
	 *            the stream to which raw data will be written.
	 *            The stream will be closed when done!
	 * @param serializer
	 *            the serializer used for writing individual vertices.
	//	 * @param vertexFeaturesToSerialize
	//	 *            the vertex features to serialize.
	//	 * @param edgeFeaturesToSerialize
	//	 *            the edge features to serialize.
	 * @return the map from vertices/edges to IDs used in the raw file.
	 * @throws IOException
	 *             if an I/O error occurs while writing the file.
	 */
	public GraphToFileIdMap< V, E > saveRaw(
			final OutputStream os,
			final GraphSerializer< V, E > serializer )
			throws IOException
	{
		final ObjectOutputStream oos = new ObjectOutputStream( new BufferedOutputStream( os, 1024 * 1024 ) );
		final GraphToFileIdMap< V, E > fileIdMap = RawGraphIO.write( this, idmap, serializer, oos );
		RawPropertyIO.writePropertyMaps( fileIdMap.vertices(), vertexPropertySerializers, oos );
		// TODO: edge properties
		//		RawFeatureIO.writeFeatureMaps( fileIdMap.vertices(), vertexFeatures, vertexFeaturesToSerialize, oos );
		//		RawFeatureIO.writeFeatureMaps( fileIdMap.edges(), edgeFeatures, edgeFeaturesToSerialize, oos );
		oos.close();

		return fileIdMap;
	}

	public ReentrantReadWriteLock getLock()
	{
		return lock;
	}

	@Override
	protected void clear()
	{
		super.clear();
	}

	@Override
	protected void pauseListeners()
	{
		super.pauseListeners();
		vertexPool.getPropertyMaps().pauseListeners();
		edgePool.getPropertyMaps().pauseListeners();
	}

	@Override
	protected void resumeListeners()
	{
		edgePool.getPropertyMaps().resumeListeners();
		vertexPool.getPropertyMaps().resumeListeners();
		super.resumeListeners();
	}

	@Override
	public void notifyGraphChanged()
	{
		super.notifyGraphChanged();
	}

	/**
	 * Register a {@link VertexPositionListener} that will be notified when
	 * feature values are changed.
	 *
	 * @param listener
	 *            the listener to register.
	 * @return {@code true} if the listener was successfully registered.
	 *         {@code false} if it was already registered.
	 */
	@Override
	public boolean addVertexPositionListener( final VertexPositionListener< V > listener )
	{
		return vertexPool.position.propertyChangeListeners().add( wrap( listener ) );
	}

	/**
	 * Removes the specified {@link VertexPositionListener} from the set of
	 * listeners.
	 *
	 * @param listener
	 *            the listener to remove.
	 * @return {@code true} if the listener was present in the listeners of this
	 *         model and was successfully removed.
	 */
	@Override
	public boolean removeVertexPositionListener( final VertexPositionListener< V > listener )
	{
		return vertexPool.position.propertyChangeListeners().remove( wrap( listener ) );
	}

	private VertexPositionListenerWrapper< V > wrap( final VertexPositionListener< V > l )
	{
		return new VertexPositionListenerWrapper<>( l );
	}

	private static class VertexPositionListenerWrapper< V > implements PropertyChangeListener< V >
	{
		private final VertexPositionListener< V > l;

		VertexPositionListenerWrapper( final VertexPositionListener< V > l )
		{
			this.l = l;
		}

		@Override
		public void propertyChanged( final V vertex )
		{
			l.vertexPositionChanged( vertex );
		}

		@Override
		public int hashCode()
		{
			return l.hashCode();
		}

		@SuppressWarnings( "unchecked" )
		@Override
		public boolean equals( final Object obj )
		{
			return ( obj instanceof VertexPositionListenerWrapper )
					&& l.equals( ( ( VertexPositionListenerWrapper< V > ) obj ).l );
		}
	}
}
