package org.mastodon.revised.mamut.feature;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.mastodon.graph.io.RawGraphIO.FileIdToGraphMap;
import org.mastodon.graph.io.RawGraphIO.GraphToFileIdMap;
import org.mastodon.properties.AbstractPropertyMap;
import org.mastodon.properties.PropertyMap;
import org.mastodon.revised.model.feature.Feature;
import org.mastodon.revised.model.feature.FeatureProjection;
import org.mastodon.revised.model.feature.FeatureSerializer;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.Spot;
import org.scijava.plugin.Plugin;

import net.imglib2.RealLocalizable;

@Plugin( type = SpotFeatureComputer.class, name = "Spot position" )
public class SpotPositionFeatureComputer implements SpotFeatureComputer< PropertyMap< Spot, RealLocalizable > >
{

	private static final String KEY = "Spot position";

	@Override
	public Set< String > getDependencies()
	{
		return Collections.emptySet();
	}

	@Override
	public String getKey()
	{
		return KEY;
	}

	@Override
	public Feature< Spot, PropertyMap< Spot, RealLocalizable > > compute( final Model model )
	{
		final LinkedHashMap< String, FeatureProjection< Spot > > map = new LinkedHashMap<>();
		for ( int d = 0; d < 3; d++ )
		{
			final String pname = "Spot " + ( char ) ( 'X' + d ) + " position";
			final SpotPositionProjection projection = new SpotPositionProjection( d );
			map.put( pname, projection );
		}
		final Map< String, FeatureProjection< Spot > > projections = Collections.unmodifiableMap( map );
		final RealLocalizablePropertyMap< Spot > rlpm = new RealLocalizablePropertyMap< >( model.getGraph().vertices().size() );
		final Feature< Spot, PropertyMap< Spot, RealLocalizable > > feature =
				new Feature< >(
						KEY, Spot.class,
						rlpm,
						projections );
		return feature;
	}

	private final static class SpotPositionProjection implements FeatureProjection< Spot >
	{

		private final int d;

		public SpotPositionProjection( final int d )
		{
			this.d = d;
		}

		@Override
		public boolean isSet( final Spot obj )
		{
			return true;
		}

		@Override
		public double value( final Spot obj )
		{
			return obj.getDoublePosition( d );
		}
	}

	/**
	 * Morphs a {@link RealLocalizable} position as a read-only PropertyMap.
	 *
	 * @author Jean-Yves Tinevez
	 *
	 */
	private static final class RealLocalizablePropertyMap< K extends RealLocalizable > extends AbstractPropertyMap< K, RealLocalizable >
	{

		private final int size;

		public RealLocalizablePropertyMap( final int size )
		{
			this.size = size;
		}

		@Override
		public RealLocalizable set( final K key, final RealLocalizable value )
		{
			throw new UnsupportedOperationException( "Cannot set a read-only property map." );
		}

		@Override
		public RealLocalizable remove( final K key )
		{
			throw new UnsupportedOperationException( "Cannot remove in a read-only property map." );
		}

		@Override
		public void beforeDeleteObject( final K key )
		{}

		@Override
		public void beforeClearPool()
		{}

		@Override
		public void clear()
		{
			throw new UnsupportedOperationException( "Cannot clear a read-only property map." );
		}

		@Override
		public RealLocalizable get( final K key )
		{
			return key;
		}

		@Override
		public boolean isSet( final K key )
		{
			return true;
		}

		@Override
		public int size()
		{
			return size;
		}

	}

	@Override
	public FeatureSerializer< Spot, PropertyMap< Spot, RealLocalizable >, Model > getSerializer()
	{
		return new FeatureSerializer< Spot, PropertyMap< Spot, RealLocalizable >, Model >()
		{

			@Override
			public void serialize( final Feature< Spot, PropertyMap< Spot, RealLocalizable > > feature, final File file, final Model support, final GraphToFileIdMap< ?, ? > idmap ) throws IOException
			{
				// Do nothing.
			}

			@Override
			public Feature< Spot, PropertyMap< Spot, RealLocalizable > > deserialize( final File file, final Model model, final FileIdToGraphMap< ?, ? > fileIdToGraphMap ) throws IOException
			{
				// Do nothing and return the feature as is.
				return compute( model );
			}
		};
	}
}
