package org.mastodon.revised.mamut.feature;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.Map;

import org.mastodon.graph.io.RawGraphIO.FileIdToGraphMap;
import org.mastodon.graph.io.RawGraphIO.GraphToFileIdMap;
import org.mastodon.io.FileIdToObjectMap;
import org.mastodon.io.ObjectToFileIdMap;
import org.mastodon.io.properties.DoublePropertyMapSerializer;
import org.mastodon.io.properties.IntPropertyMapSerializer;
import org.mastodon.pool.PoolCollectionWrapper;
import org.mastodon.properties.DoublePropertyMap;
import org.mastodon.properties.IntPropertyMap;
import org.mastodon.revised.model.feature.Feature;
import org.mastodon.revised.model.feature.FeatureProjection;
import org.mastodon.revised.model.feature.FeatureProjectors;
import org.mastodon.revised.model.feature.FeatureSerializer;
import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.Spot;

/**
 * Utility {@link FeatureSerializer}s for MaMuT.
 *
 * @author Jean-Yves Tinevez
 */
public class MamutFeatureSerializers
{

	public static final FeatureSerializer< Link, DoublePropertyMap< Link >, Model > doubleLinkSerializer( final String key )
	{
		return new FeatureSerializer< Link, DoublePropertyMap< Link >, Model >()
		{

			@Override
			public void serialize( final Feature< Link, DoublePropertyMap< Link > > feature, final File file, final Model support, final GraphToFileIdMap< ?, ? > idmap ) throws IOException
			{
				try (final ObjectOutputStream oos = new ObjectOutputStream(
						new BufferedOutputStream(
								new FileOutputStream( file ), 1024 * 1024 ) ))
				{
					final DoublePropertyMap< Link > pm = feature.getPropertyMap();
					final DoublePropertyMapSerializer< Link > serializer = new DoublePropertyMapSerializer<>( pm );
					@SuppressWarnings( "unchecked" )
					final ObjectToFileIdMap< Link > linkToIdMap = ( ObjectToFileIdMap< Link > ) idmap.edges();
					serializer.writePropertyMap( linkToIdMap, oos );
				}
			}

			@Override
			public Feature< Link, DoublePropertyMap< Link > > deserialize( final File file, final Model model, final FileIdToGraphMap< ?, ? > fileIdToGraphMap ) throws IOException
			{
				try (final ObjectInputStream ois = new ObjectInputStream(
						new BufferedInputStream(
								new FileInputStream( file ), 1024 * 1024 ) ))
				{
					final PoolCollectionWrapper< Link > edges = model.getGraph().edges();
					final DoublePropertyMap< Link > pm = new DoublePropertyMap<>( edges, Double.NaN, edges.size() );
					final DoublePropertyMapSerializer< Link > serializer = new DoublePropertyMapSerializer<>( pm );
					@SuppressWarnings( "unchecked" )
					final FileIdToObjectMap< Link > idToLinkMap = ( FileIdToObjectMap< Link > ) fileIdToGraphMap.edges();
					serializer.readPropertyMap( idToLinkMap, ois );
					return MamutFeatureSerializers.bundle( key, pm, Link.class );
				}
				catch ( final ClassNotFoundException e )
				{
					e.printStackTrace();
				}
				return null;
			}
		};
	}

	public static final FeatureSerializer< Link, IntPropertyMap< Link >, Model > intLinkSerializer( final String key )
	{
		return new FeatureSerializer< Link, IntPropertyMap< Link >, Model >()
		{

			@Override
			public void serialize( final Feature< Link, IntPropertyMap< Link > > feature, final File file, final Model support, final GraphToFileIdMap< ?, ? > idmap ) throws IOException
			{
				try (final ObjectOutputStream oos = new ObjectOutputStream(
						new BufferedOutputStream(
								new FileOutputStream( file ), 1024 * 1024 ) ))
				{
					final IntPropertyMap< Link > pm = feature.getPropertyMap();
					final IntPropertyMapSerializer< Link > serializer = new IntPropertyMapSerializer<>( pm );
					@SuppressWarnings( "unchecked" )
					final ObjectToFileIdMap< Link > linkToIdMap = ( ObjectToFileIdMap< Link > ) idmap.edges();
					serializer.writePropertyMap( linkToIdMap, oos );
				}
			}

			@Override
			public Feature< Link, IntPropertyMap< Link > > deserialize( final File file, final Model model, final FileIdToGraphMap< ?, ? > fileIdToGraphMap ) throws IOException
			{
				try (final ObjectInputStream ois = new ObjectInputStream(
						new BufferedInputStream(
								new FileInputStream( file ), 1024 * 1024 ) ))
				{
					final PoolCollectionWrapper< Link > edges = model.getGraph().edges();
					final IntPropertyMap< Link > pm = new IntPropertyMap<>( edges, Integer.MIN_VALUE, edges.size() );
					final IntPropertyMapSerializer< Link > serializer = new IntPropertyMapSerializer<>( pm );
					@SuppressWarnings( "unchecked" )
					final FileIdToObjectMap< Link > idToLinkMap = ( FileIdToObjectMap< Link > ) fileIdToGraphMap.edges();
					serializer.readPropertyMap( idToLinkMap, ois );
					return MamutFeatureSerializers.bundle( key, pm, Link.class );
				}
				catch ( final ClassNotFoundException e )
				{
					e.printStackTrace();
				}
				return null;
			}
		};
	}

	public static FeatureSerializer< Spot, IntPropertyMap< Spot >, Model > intSpotSerializer( final String key )
	{
		return new FeatureSerializer< Spot, IntPropertyMap< Spot >, Model >()
		{

			@Override
			public void serialize( final Feature< Spot, IntPropertyMap< Spot > > feature, final File file, final Model support, final GraphToFileIdMap< ?, ? > idmap ) throws IOException
			{
				try (final ObjectOutputStream oos = new ObjectOutputStream(
						new BufferedOutputStream(
								new FileOutputStream( file ), 1024 * 1024 ) ))
				{
					final IntPropertyMap< Spot > pm = feature.getPropertyMap();
					final IntPropertyMapSerializer< Spot > serializer = new IntPropertyMapSerializer<>( pm );
					@SuppressWarnings( "unchecked" )
					final ObjectToFileIdMap< Spot > linkToIdMap = ( ObjectToFileIdMap< Spot > ) idmap.vertices();
					serializer.writePropertyMap( linkToIdMap, oos );
				}
			}

			@Override
			public Feature< Spot, IntPropertyMap< Spot > > deserialize( final File file, final Model model, final FileIdToGraphMap< ?, ? > fileIdToGraphMap ) throws IOException
			{
				try (final ObjectInputStream ois = new ObjectInputStream(
						new BufferedInputStream(
								new FileInputStream( file ), 1024 * 1024 ) ))
				{
					final PoolCollectionWrapper< Spot > spots = model.getGraph().vertices();
					final IntPropertyMap< Spot > pm = new IntPropertyMap<>( spots, Integer.MIN_VALUE, spots.size() );
					final IntPropertyMapSerializer< Spot > serializer = new IntPropertyMapSerializer<>( pm );
					@SuppressWarnings( "unchecked" )
					final FileIdToObjectMap< Spot > idToLinkMap = ( FileIdToObjectMap< Spot > ) fileIdToGraphMap.vertices();
					serializer.readPropertyMap( idToLinkMap, ois );
					return MamutFeatureSerializers.bundle( key, pm, Spot.class );
				}
				catch ( final ClassNotFoundException e )
				{
					e.printStackTrace();
				}
				return null;
			}
		};
	}

	public static FeatureSerializer< Spot, DoublePropertyMap< Spot >, Model > doubleSpotSerializer( final String key )
	{
		return new FeatureSerializer< Spot, DoublePropertyMap< Spot >, Model >()
		{

			@Override
			public void serialize( final Feature< Spot, DoublePropertyMap< Spot > > feature, final File file, final Model support, final GraphToFileIdMap< ?, ? > idmap ) throws IOException
			{
				try (final ObjectOutputStream oos = new ObjectOutputStream(
						new BufferedOutputStream(
								new FileOutputStream( file ), 1024 * 1024 ) ))
				{
					final DoublePropertyMap< Spot > pm = feature.getPropertyMap();
					final DoublePropertyMapSerializer< Spot > serializer = new DoublePropertyMapSerializer<>( pm );
					@SuppressWarnings( "unchecked" )
					final ObjectToFileIdMap< Spot > linkToIdMap = ( ObjectToFileIdMap< Spot > ) idmap.vertices();
					serializer.writePropertyMap( linkToIdMap, oos );
				}
			}

			@Override
			public Feature< Spot, DoublePropertyMap< Spot > > deserialize( final File file, final Model model, final FileIdToGraphMap< ?, ? > fileIdToGraphMap ) throws IOException
			{
				try (final ObjectInputStream ois = new ObjectInputStream(
						new BufferedInputStream(
								new FileInputStream( file ), 1024 * 1024 ) ))
				{
					final PoolCollectionWrapper< Spot > spots = model.getGraph().vertices();
					final DoublePropertyMap< Spot > pm = new DoublePropertyMap<>( spots, Double.NaN, spots.size() );
					final DoublePropertyMapSerializer< Spot > serializer = new DoublePropertyMapSerializer<>( pm );
					@SuppressWarnings( "unchecked" )
					final FileIdToObjectMap< Spot > idToLinkMap = ( FileIdToObjectMap< Spot > ) fileIdToGraphMap.vertices();
					serializer.readPropertyMap( idToLinkMap, ois );
					return MamutFeatureSerializers.bundle( key, pm, Spot.class );
				}
				catch ( final ClassNotFoundException e )
				{
					e.printStackTrace();
				}
				return null;
			}
		};
	}


	public static final < O > Feature< O, DoublePropertyMap< O > > bundle( final String featureKey, final DoublePropertyMap< O > propertyMap, final Class< O > clazz )
	{
		final Map< String, FeatureProjection< O > > projections = Collections.singletonMap( featureKey, FeatureProjectors.project( propertyMap ) );
		final Feature< O, DoublePropertyMap< O > > feature = new Feature<>(
				featureKey,
				clazz,
				propertyMap,
				projections );
		return feature;
	}

	public static final < O > Feature< O, IntPropertyMap< O > > bundle( final String featureKey, final IntPropertyMap< O > propertyMap, final Class< O > clazz )
	{
		final Map< String, FeatureProjection< O > > projections = Collections.singletonMap( featureKey, FeatureProjectors.project( propertyMap ) );
		final Feature< O, IntPropertyMap< O > > feature = new Feature<>(
				featureKey,
				clazz,
				propertyMap,
				projections );
		return feature;
	}

	private MamutFeatureSerializers()
	{}
}
