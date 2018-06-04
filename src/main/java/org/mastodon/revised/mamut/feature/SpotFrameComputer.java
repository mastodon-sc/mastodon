package org.mastodon.revised.mamut.feature;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.mastodon.io.ObjectToFileIdMap;
import org.mastodon.revised.model.feature.Feature;
import org.mastodon.revised.model.feature.FeatureProjection;
import org.mastodon.revised.model.feature.IntFeatureProjection;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.Spot;
import org.scijava.plugin.Plugin;

@Plugin( type = SpotFeatureComputer.class, name = "Spot frame" )
public class SpotFrameComputer extends SpotIntScalarFeatureComputer
{

	private static final String KEY = "Spot frame";

	private static final String HELP_STRING = "Exposes the spot frame.";

	public SpotFrameComputer()
	{
		super( KEY );
	}

	@Override
	public Collection< String > getProjectionKeys()
	{
		return Collections.singleton( KEY );
	}

	@Override
	public Set< String > getDependencies()
	{
		return Collections.emptySet();
	}

	@Override
	public Feature< Spot, Integer > compute( final Model model )
	{
		return new MyFeature();
	}

	@Override
	public String getHelpString()
	{
		return HELP_STRING;
	}

	private static class MyFeature implements Feature< Spot, Integer >
	{

		private final Map< String, FeatureProjection< Spot > > projections;

		public MyFeature()
		{
			final MyProjection proj = new MyProjection();
			projections = Collections.singletonMap( KEY, proj );
		}

		@Override
		public String getKey()
		{
			return KEY;
		}

		@Override
		public Map< String, FeatureProjection< Spot > > getProjections()
		{
			return projections;
		}

		@Override
		public Class< Spot > getTargetClass()
		{
			return Spot.class;
		}

		@Override
		public Integer get( final Spot o, final Integer ref )
		{
			return Integer.valueOf( o.getTimepoint() );
		}

		@Override
		public Integer get( final Spot o )
		{
			return Integer.valueOf( o.getTimepoint() );
		}

		@Override
		public boolean isSet( final Spot o )
		{
			return true;
		}

		@Override
		public void serialize( final File file, final ObjectToFileIdMap< Spot > idmap ) throws IOException
		{
			// Do nothing.
		}
	}

	private static final class MyProjection implements IntFeatureProjection< Spot >
	{

		@Override
		public boolean isSet( final Spot obj )
		{
			return true;
		}

		@Override
		public double value( final Spot o )
		{
			return o.getTimepoint();
		}

		@Override
		public String units()
		{
			return ""; // No units, it's a frame 'position'.
		}
	}
}
