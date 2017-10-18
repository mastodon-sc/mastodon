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

import org.mastodon.RefPool;
import org.mastodon.collection.RefDoubleMap;
import org.mastodon.collection.RefIntMap;
import org.mastodon.properties.DoublePropertyMap;
import org.mastodon.properties.IntPropertyMap;
import org.mastodon.revised.model.feature.Feature;
import org.mastodon.revised.model.feature.FeatureProjection;
import org.mastodon.revised.model.feature.FeatureProjectors;
import org.mastodon.revised.model.feature.FeatureSerializer;
import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.Spot;

import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TIntIntHashMap;

/**
 * Utility {@link FeatureSerializer}s for MaMuT.
 *
 * @author Jean-Yves Tinevez
 */
public class MamutFeatureSerializers
{

	/**
	 * Returns a serializer able to de/serialize a <code>double</code> feature
	 * defined on {@link Link}.
	 *
	 * @param featureKey
	 *            the feature key.
	 * @return a new feature serializer.
	 */
	public static FeatureSerializer< Link, DoublePropertyMap< Link >, Model > doubleLinkSerializer( final String featureKey )
	{
		return new DoubleLinkFeatureSerializer( featureKey );
	}

	/**
	 * Returns a serializer able to de/serialize a <code>double</code> feature
	 * defined on {@link Spot}.
	 *
	 * @param featureKey
	 *            the feature key.
	 * @return a new feature serializer.
	 */
	public static FeatureSerializer< Spot, DoublePropertyMap< Spot >, Model > doubleSpotSerializer( final String featureKey )
	{
		return new DoubleSpotFeatureSerializer( featureKey );
	}

	/**
	 * Returns a serializer able to de/serialize a <code>in</code> feature
	 * defined on {@link Spot}.
	 *
	 * @param featureKey
	 *            the feature key.
	 * @return a new feature serializer.
	 */
	public static FeatureSerializer< Spot, IntPropertyMap< Spot >, Model > intSpotSerializer( final String featureKey )
	{
		return new IntSpotFeatureSerializer( featureKey );
	}

	/**
	 * Returns a serializer able to de/serialize a <code>in</code> feature
	 * defined on {@link Link}.
	 *
	 * @param featureKey
	 *            the feature key.
	 * @return a new feature serializer.
	 */
	public static FeatureSerializer< Link, IntPropertyMap< Link >, Model > intLinkSerializer( final String featureKey )
	{
		return new IntLinkFeatureSerializer( featureKey );
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

	/*
	 * INNER CLASSES
	 */

	private static final class DoubleSpotFeatureSerializer implements FeatureSerializer< Spot, DoublePropertyMap< Spot >, Model >
	{

		private final String featureKey;

		public DoubleSpotFeatureSerializer( final String featureKey )
		{
			this.featureKey = featureKey;
		}

		@Override
		public void serialize( final Feature< Spot, DoublePropertyMap< Spot > > feature, final File file, final Model model ) throws IOException
		{
			serializeDoublePropertyMap( file, feature.getPropertyMap(), model.getGraph().vertices().getRefPool() );
		}

		@Override
		public Feature< Spot, DoublePropertyMap< Spot > > deserialize( final File file, final Model model ) throws IOException
		{
			final DoublePropertyMap< Spot > propertyMap = deserializeDoublePropertyMap( file, model.getGraph().vertices().getRefPool(), Double.NaN );
			return bundle( featureKey, propertyMap, Spot.class );
		}
	}

	private static final class DoubleLinkFeatureSerializer implements FeatureSerializer< Link, DoublePropertyMap< Link >, Model >
	{

		private final String featureKey;

		public DoubleLinkFeatureSerializer( final String featureKey )
		{
			this.featureKey = featureKey;
		}

		@Override
		public void serialize( final Feature< Link, DoublePropertyMap< Link > > feature, final File file, final Model model ) throws IOException
		{
			serializeDoublePropertyMap( file, feature.getPropertyMap(), model.getGraph().edges().getRefPool() );
		}

		@Override
		public Feature< Link, DoublePropertyMap< Link > > deserialize( final File file, final Model model ) throws IOException
		{
			final DoublePropertyMap< Link > propertyMap = deserializeDoublePropertyMap( file, model.getGraph().edges().getRefPool(), Double.NaN );
			return bundle( featureKey, propertyMap, Link.class );
		}

	}

	private static final class IntSpotFeatureSerializer implements FeatureSerializer< Spot, IntPropertyMap< Spot >, Model >
	{

		private final String featureKey;

		public IntSpotFeatureSerializer( final String featureKey )
		{
			this.featureKey = featureKey;
		}

		@Override
		public void serialize( final Feature< Spot, IntPropertyMap< Spot > > feature, final File file, final Model model ) throws IOException
		{
			serializeIntPropertyMap( file, feature.getPropertyMap(), model.getGraph().vertices().getRefPool() );
		}

		@Override
		public Feature< Spot, IntPropertyMap< Spot > > deserialize( final File file, final Model model ) throws IOException
		{
			final IntPropertyMap< Spot > pm = deserializeIntPropertyMap( file, model.getGraph().vertices().getRefPool(), Integer.MIN_VALUE );
			return bundle( featureKey, pm, Spot.class );
		}
	}

	private static final class IntLinkFeatureSerializer implements FeatureSerializer< Link, IntPropertyMap< Link >, Model >
	{

		private final String featureKey;

		public IntLinkFeatureSerializer( final String featureKey )
		{
			this.featureKey = featureKey;
		}

		@Override
		public void serialize( final Feature< Link, IntPropertyMap< Link > > feature, final File file, final Model model ) throws IOException
		{
			serializeIntPropertyMap( file, feature.getPropertyMap(), model.getGraph().edges().getRefPool() );
		}

		@Override
		public Feature< Link, IntPropertyMap< Link > > deserialize( final File file, final Model model ) throws IOException
		{
			final IntPropertyMap< Link > pm = deserializeIntPropertyMap( file, model.getGraph().edges().getRefPool(), Integer.MIN_VALUE );
			return bundle( featureKey, pm, Link.class );
		}
	}

	private static final < O > void serializeIntPropertyMap( final File file, final IntPropertyMap< O > propertyMap, final RefPool< O > pool ) throws IOException
	{
		try (final ObjectOutputStream oos =
				new ObjectOutputStream(
						new BufferedOutputStream(
								new FileOutputStream( file ), 1024 * 1024 ) ))
		{
			final TIntIntHashMap fmap = new TIntIntHashMap();
			final RefIntMap< O > pmap = propertyMap.getMap();
			pmap.forEachEntry( ( final O key, final int value ) -> {
				fmap.put( pool.getId( key ), value );
				return true;
			} );
			oos.writeObject( fmap );
		}
	}

	private static final < O > void serializeDoublePropertyMap( final File file, final DoublePropertyMap< O > propertyMap, final RefPool< O > pool ) throws IOException
	{
		try (final ObjectOutputStream oos =
				new ObjectOutputStream(
						new BufferedOutputStream(
								new FileOutputStream( file ), 1024 * 1024 ) ))
		{
			final TIntDoubleHashMap fmap = new TIntDoubleHashMap();
			final RefDoubleMap< O > pmap = propertyMap.getMap();
			pmap.forEachEntry( ( final O key, final double value ) -> {
				fmap.put( pool.getId( key ), value );
				return true;
			} );
			oos.writeObject( fmap );
		}
	}

	private static final < O > IntPropertyMap< O > deserializeIntPropertyMap( final File file, final RefPool< O > pool, final int noEntry ) throws IOException
	{
		try (final ObjectInputStream ois =
				new ObjectInputStream(
						new BufferedInputStream(
								new FileInputStream( file ), 1024 * 1024 ) ))
		{
			final TIntIntHashMap fmap = ( TIntIntHashMap ) ois.readObject();
			final IntPropertyMap< O > pm = new IntPropertyMap<>( pool, noEntry );
			final RefIntMap< O > pmap = pm.getMap();
			pmap.clear();
			final O ref = pool.createRef();
			fmap.forEachEntry( ( final int key, final int value ) -> {
				pmap.put( pool.getObject( key, ref ), value );
				return true;
			} );
			pool.releaseRef( ref );
			return pm;
		}
		catch ( final ClassNotFoundException e )
		{
			e.printStackTrace();
		}
		return null;
	}

	private static final < O > DoublePropertyMap< O > deserializeDoublePropertyMap( final File file, final RefPool< O > pool, final double noEntry ) throws IOException
	{
		try (final ObjectInputStream ois =
				new ObjectInputStream(
						new BufferedInputStream(
								new FileInputStream( file ), 1024 * 1024 ) ))
		{
			final TIntDoubleHashMap fmap = ( TIntDoubleHashMap ) ois.readObject();
			final DoublePropertyMap< O > pm = new DoublePropertyMap<>( pool, noEntry );
			final RefDoubleMap< O > pmap = pm.getMap();
			pmap.clear();
			final O ref = pool.createRef();
			fmap.forEachEntry( ( final int key, final double value ) -> {
				pmap.put( pool.getObject( key, ref ), value );
				return true;
			} );
			pool.releaseRef( ref );
			return pm;
		}
		catch ( final ClassNotFoundException e )
		{
			e.printStackTrace();
		}
		return null;
	}

	private MamutFeatureSerializers()
	{}

}
