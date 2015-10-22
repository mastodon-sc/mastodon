package net.trackmate.revised.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import net.trackmate.graph.AbstractEdge;
import net.trackmate.graph.AbstractEdgePool;
import net.trackmate.graph.AbstractVertex;
import net.trackmate.graph.AbstractVertexPool;
import net.trackmate.graph.GraphIdBimap;
import net.trackmate.graph.ReadOnlyGraph;
import net.trackmate.graph.listenable.ListenableGraph;
import net.trackmate.graph.mempool.MappedElement;
import net.trackmate.io.RawGraphIO;


/**
 * Manages the model graph.
 * <p>
 * The model graph is only exposed as a {@link ReadOnlyGraph}.
 * All updates to the model graph are done through {@link AbstractModel}.
 * This includes vertex and edge attribute changes (although this currently cannot be enforced through {@link ReadOnlyGraph}).
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class AbstractModel<
		VP extends AbstractVertexPool< V, E, T >,
		EP extends AbstractEdgePool< E, V, T >,
		V extends AbstractVertex< V, E, T >,
		E extends AbstractEdge< E, V, T >,
		T extends MappedElement >
{
	public ListenableGraph< V, E > getGraph()
	{
		return modelGraph;
	}

	public GraphIdBimap< V, E > getGraphIdBimap()
	{
		return modelGraph.idmap;
	}

	protected final AbstractModelGraph< VP, EP, V, E, T > modelGraph;

	protected AbstractModel( final AbstractModelGraph< VP, EP, V, E, T > modelGraph )
	{
		this.modelGraph = modelGraph;
	}

	protected void loadRaw( final File file, final RawGraphIO.Serializer< V, E > serializer ) throws IOException
	{
		final FileInputStream fis = new FileInputStream( file );
		final ObjectInputStream ois = new ObjectInputStream( fis );
		modelGraph.pauseListeners();
		modelGraph.clear();
		RawGraphIO.read( modelGraph, modelGraph.idmap, serializer, ois );
		ois.close();
		modelGraph.resumeListeners();
	}

	protected void saveRaw( final File file, final RawGraphIO.Serializer< V, E > serializer ) throws IOException
	{
		final FileOutputStream fos = new FileOutputStream( file );
		final ObjectOutputStream oos = new ObjectOutputStream( fos );
		RawGraphIO.write( modelGraph, modelGraph.idmap, serializer, oos );
		oos.close();
	}
}
