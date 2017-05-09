package org.mastodon.revised.mamut.feature;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.mastodon.properties.AbstractPropertyMap;
import org.mastodon.properties.PropertyMap;
import org.mastodon.revised.model.feature.Feature;
import org.mastodon.revised.model.feature.FeatureProjection;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.Spot;
import org.scijava.plugin.Plugin;

import net.imglib2.RealLocalizable;

@Plugin( type = SpotFeatureComputer.class, name = "Spot position" )
public class SpotPositionFeatureComputer
		extends SpotFeatureComputer< Feature< Spot, RealLocalizable, PropertyMap< Spot, RealLocalizable > >, Model >
{

	private static final String KEY = "Spot position";

	private static final Map< String, FeatureProjection< Spot > > PROJECTIONS;
	static
	{
		final HashMap< String, FeatureProjection< Spot > > map = new HashMap<>();
		for ( int d = 0; d < 3; d++ )
		{
			final String pname = "Spot " + ( char ) ( 'X' + d ) + " position";
			final SpotPositionProjection projection = new SpotPositionProjection( d );
			map.put( pname, projection );
		}
		PROJECTIONS = Collections.unmodifiableMap( map );
	}

	private Feature< Spot, RealLocalizable, PropertyMap< Spot, RealLocalizable > > feature;

	@Override
	public Set< String > getDependencies()
	{
		return Collections.emptySet();
	}

	@Override
	public Map< String, FeatureProjection< Spot > > getProjections()
	{
		return PROJECTIONS;
	}


	@Override
	public void compute( final Model model )
	{
		this.feature = new IdentityFeature( KEY, model.getGraph().vertices().size() );
	}

	@Override
	public Feature< Spot, RealLocalizable, PropertyMap< Spot, RealLocalizable > > getFeature()
	{
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

	private final static class IdentityFeature extends Feature< Spot, RealLocalizable, PropertyMap< Spot, RealLocalizable > >
	{

		private final int size;

		public IdentityFeature( final String key, final int size )
		{
			super( key, null );
			this.size = size;
		}

		@Override
		public PropertyMap< Spot, RealLocalizable > getPropertyMap()
		{
			return new AbstractPropertyMap< Spot, RealLocalizable >()
			{

				@Override
				public RealLocalizable set( final Spot key, final RealLocalizable value )
				{
					// Ignored
					return null;
				}

				@Override
				public RealLocalizable remove( final Spot key )
				{
					// Ignored.
					return null;
				}

				@Override
				public void beforeDeleteObject( final Spot key )
				{}

				@Override
				public void beforeClearPool()
				{}

				@Override
				public void clear()
				{
					throw new UnsupportedOperationException();
				}

				@Override
				public RealLocalizable get( final Spot key )
				{
					return key;
				}

				@Override
				public boolean isSet( final Spot key )
				{
					return true;
				}

				@Override
				public int size()
				{
					return size;
				}
			};
		}

	}
}
