package net.trackmate.io;

import gnu.trove.map.hash.TIntIntHashMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;

import net.trackmate.model.Link;
import net.trackmate.model.Model;
import net.trackmate.model.ModelGraph;
import net.trackmate.model.SpotCovariance;
import net.trackmate.model.SpotIO;

public class RawIO
{
	public static void write( final Model model, final File file ) throws IOException
	{
		final FileOutputStream fos = new FileOutputStream( file );
		final ObjectOutputStream oos = new ObjectOutputStream( fos );

		final ModelGraph< SpotCovariance > graph = model.getGraph();
		final int numSpots = graph.numSpots();
		oos.writeInt( numSpots );

		final Iterator< SpotCovariance > spotIterator = graph.vertexIterator();
		final byte[] bytes = new byte[ SpotIO.getSpotCovarianceNumBytes() ];
		final TIntIntHashMap internalIndexToFileIndex = new TIntIntHashMap( 2 * numSpots, 0.75f, -1, -1 );
		int i = 0;
		while( spotIterator.hasNext() )
		{
			final SpotCovariance spot = spotIterator.next();
			SpotIO.getBytes( spot, bytes );
			oos.write( bytes );

			final int internalPoolIndex = spot.getInternalPoolIndex();
			internalIndexToFileIndex.put( internalPoolIndex, i );
			++i;
		}

		final int numLinks = graph.numLinks();
		oos.writeInt( numLinks );

		final Iterator< Link< SpotCovariance > > edgeIterator = graph.edgeIterator();
		final SpotCovariance spot = graph.vertexRef();
		while( edgeIterator.hasNext() )
		{
			final Link< SpotCovariance > link = edgeIterator.next();
			final int from = internalIndexToFileIndex.get( link.getSource( spot ).getInternalPoolIndex() );
			final int to = internalIndexToFileIndex.get( link.getTarget( spot ).getInternalPoolIndex() );
			oos.writeInt( from );
			oos.writeInt( to );
		}

		oos.close();
	}

	public static Model read( final File file ) throws IOException
	{
		final FileInputStream fis = new FileInputStream( file );
		final ObjectInputStream ois = new ObjectInputStream( fis );

		final int numSpots = ois.readInt();
		final ModelGraph< SpotCovariance > graph = new ModelGraph< SpotCovariance >( new ModelGraph.SpotCovarianceFactory(), numSpots );
		final SpotCovariance spot1 = graph.vertexRef();
		final SpotCovariance spot2 = graph.vertexRef();
		final Link< SpotCovariance > link = graph.edgeRef();

		final byte[] bytes = new byte[ SpotIO.getSpotCovarianceNumBytes() ];
		final TIntIntHashMap fileIndexToInternalIndex = new TIntIntHashMap( 2 * numSpots, 0.75f, -1, -1 );
		for ( int i = 0; i < numSpots; ++i )
		{
			ois.readFully( bytes );
			graph.addVertex( spot1 );
			SpotIO.setBytes( spot1, bytes );

			final int internalPoolIndex = spot1.getInternalPoolIndex();
			fileIndexToInternalIndex.put( i, internalPoolIndex );
		}

		final int numEdges = ois.readInt();
		for ( int i = 0; i < numEdges; ++i )
		{
			final int from = fileIndexToInternalIndex.get( ois.readInt() );
			final int to = fileIndexToInternalIndex.get( ois.readInt() );
			SpotIO.getSpotByInternalID( graph, from, spot1 );
			SpotIO.getSpotByInternalID( graph, to, spot2 );
			graph.addEdge( spot1, spot2, link );
		}

		ois.close();

		final Model model = new Model( graph );
		final Iterator< SpotCovariance > spotIterator = graph.vertexIterator();
		while( spotIterator.hasNext() )
		{
			final SpotCovariance spot = spotIterator.next();
			final int t = spot.getTimePointId();
			model.getSpots( t ).add( spot );
		}

		return model;
	}
}
