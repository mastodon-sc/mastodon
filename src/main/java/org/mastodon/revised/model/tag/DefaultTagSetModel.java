package org.mastodon.revised.model.tag;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.mastodon.graph.Edge;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.graph.io.RawGraphIO.FileIdToGraphMap;
import org.mastodon.graph.io.RawGraphIO.GraphToFileIdMap;
import org.mastodon.revised.model.tag.TagSetPanel.GraphTagPropertyMapFactory;

public class DefaultTagSetModel< V extends Vertex< E >, E extends Edge< V > > implements TagSetModel< V, E >
{

	private final GraphTagPropertyMapFactory factory;

	private final List< GraphTagPropertyMap< V, E > > tagSets;

	private final ReadOnlyGraph< V, E > graph;

	public DefaultTagSetModel( final ReadOnlyGraph< V, E > graph )
	{
		this.graph = graph;
		this.factory = ( name ) -> new GraphTagPropertyMap<>( name, graph );
		this.tagSets = new ArrayList<>();
	}

	@Override
	public List< GraphTagPropertyMap< V, E > > getTagSets()
	{
		return Collections.unmodifiableList( tagSets );
	}

	@Override
	public GraphTagPropertyMap< V, E > createTagSet( final String name )
	{
		@SuppressWarnings( "unchecked" )
		final GraphTagPropertyMap< V, E > tagSet = ( GraphTagPropertyMap< V, E > ) factory.create( name );
		tagSets.add( tagSet );
		return tagSet;
	}

	@Override
	public boolean removeTagSet( final GraphTagPropertyMap< V, E > tagSet )
	{
		return tagSets.remove( tagSet );
	}

	public void clear()
	{
		tagSets.clear();
	}

	@Override
	public String toString()
	{
		final StringBuilder str = new StringBuilder( super.toString() );
		for ( final GraphTagPropertyMap< V, E > tagSet : tagSets )
		{
			str.append( "\n" + tagSet.getName() + ":" );
			for ( final Tag tag : tagSet.getTags() )
			{
				str.append( "\n  - " + tag.label() + ":" );
				str.append( "\n    V: " );
				for ( final V v : tagSet.getTaggedVertices( tag ) )
					str.append( v + "," );
				str.append( "\n    E: " );
				for ( final E e : tagSet.getTaggedEdges( tag ) )
					str.append( e + "," );
			}
		}
		return str.toString();
	}

	@Override
	public void saveRaw( final File baseFolder, final GraphToFileIdMap< V, E > graphToFileIdMap )
	{
		final File tagFolder = new File( baseFolder, TAG_FOLDER );
		if ( !tagFolder.exists() )
		{
			final boolean created = tagFolder.mkdirs();
			if ( !created )
			{
				System.err.println( "Could not create folder to save tags in: " + tagFolder );
				return;
			}
		}
		if ( !tagFolder.isDirectory() )
		{
			System.err.println( "Target folder to save tags in is not a directory: " + tagFolder );
			return;
		}

		for ( final GraphTagPropertyMap< V, E > tagSet : tagSets )
		{
			final File file = makeTagFilePath( baseFolder, tagSet.getName() );
			try (final ObjectOutputStream oos = new ObjectOutputStream(
					new BufferedOutputStream(
							new FileOutputStream( file ), 1024 * 1024 ) ))
			{
				// Tags.
				final Collection< Tag > tags = tagSet.getTags();
				oos.writeInt( tags.size() );
				for ( final Tag tag : tags )
					oos.writeObject( tag );
				// Vertices.
				final TagPropertyMap< V, Tag > vpmap = tagSet.vertexMap();
				final TagPropertyMapSerializer< V, Tag > vSerializer = new TagPropertyMapSerializer<>( vpmap );
				vSerializer.writePropertyMap( graphToFileIdMap.vertices(), oos );
				// Edges.
				final TagPropertyMap< E, Tag > epmap = tagSet.edgeMap();
				final TagPropertyMapSerializer< E, Tag > eSerializer = new TagPropertyMapSerializer<>( epmap );
				eSerializer.writePropertyMap( graphToFileIdMap.edges(), oos );
			}
			catch ( final IOException e )
			{
				System.err.println( "Could not serialize tag " + tagSet.getName() + " to file " + file + ":\n" + e.getMessage() );
				e.printStackTrace();
			}
		}
	}

	@Override
	public void loadRaw( final File baseFolder, final FileIdToGraphMap< V, E > fileIdToGraphMap )
	{
		clear();
		final File tagFolder = new File( baseFolder, TAG_FOLDER );

		final File[] tagFiles = tagFolder.listFiles( ( pathname ) -> pathname.getName().endsWith( RAW_EXTENSION ) );
		if ( null == tagFiles )
			return;

		for ( final File file : tagFiles )
		{
			final String tagName = getTagNameFromFileName( file );
			if ( null == tagName )
			{
				System.err.println( "Cannot retrieve tag name from file name " + file + ". Skipped." );
				continue;
			}
			try (final ObjectInputStream ois = new ObjectInputStream(
					new BufferedInputStream(
							new FileInputStream( file ), 1024 * 1024 ) ))
			{
				// Tags
				final int nTags = ois.readInt();
				final List< Tag > tags = new ArrayList<>( nTags );
				for ( int i = 0; i < nTags; i++ )
				{
					final Tag tag = ( Tag ) ois.readObject();
					tags.add( tag );
				}
				// Vertices.
				final TagPropertyMap< V, Tag > vpmap = new TagPropertyMap<>( graph.vertices() );
				final TagPropertyMapSerializer< V, Tag > vSerializer = new TagPropertyMapSerializer<>( vpmap );
				vSerializer.readPropertyMap( fileIdToGraphMap.vertices(), ois );
				// Edges.
				final TagPropertyMap< E, Tag > epmap = new TagPropertyMap<>( graph.edges() );
				final TagPropertyMapSerializer< E, Tag > eSerializer = new TagPropertyMapSerializer<>( epmap );
				eSerializer.readPropertyMap( fileIdToGraphMap.edges(), ois );
				// Assemble.
				final GraphTagPropertyMap< V, E > tagSet = new GraphTagPropertyMap<>( tagName, graph, tags, vpmap, epmap );
				tagSets.add( tagSet );
			}
			catch ( final IOException | ClassNotFoundException e )
			{
				System.err.println( "Could not deserialize tag " + tagName + " from file " + file + ":\n" + e.getMessage() );
			}
		}
	}

	private static final String getTagNameFromFileName( final File tagFile )
	{
		final String name = tagFile.getName();
		final int dotIndex = name.lastIndexOf( '.' );
		return name.substring( 0, dotIndex );
	}

	private static final File makeTagFilePath( final File baseFolder, final String featureKey )
	{
		return new File( baseFolder, new File( TAG_FOLDER, featureKey + RAW_EXTENSION ).getPath() );
	}

	private static final String TAG_FOLDER = "tags";

	private static final String RAW_EXTENSION = ".raw";
}
