package org.mastodon.revised.mamut.feature;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.mastodon.graph.io.RawGraphIO.FileIdToGraphMap;
import org.mastodon.io.ObjectToFileIdMap;
import org.mastodon.revised.model.feature.Feature;
import org.mastodon.revised.model.feature.FeatureProjection;
import org.mastodon.revised.model.feature.FeatureUtil;
import org.mastodon.revised.model.feature.FeatureUtil.Dimension;
import org.mastodon.revised.model.feature.IntFeatureProjection;
import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.ModelGraph;
import org.mastodon.revised.model.mamut.Spot;
import org.scijava.plugin.Plugin;

@Plugin( type = LinkFeatureComputer.class, name = "Link target IDs" )
public class LinkTargetIdComputer extends LinkFeatureComputer
{

	public static final String KEY = "Link target IDs";

	public static final String SOURCE_PROJECTION_KEY = "Source spot id";

	public static final String TARGET_PROJECTION_KEY = "Target spot id";

	private static final String HELP_STRING = "Exposes the link source and target spot ids.";


	public LinkTargetIdComputer()
	{
		super( KEY );
	}

	@Override
	public Set< String > getDependencies()
	{
		return Collections.emptySet();
	}

	@Override
	public Collection< String > getProjectionKeys()
	{
		return Arrays.asList( new String[] { SOURCE_PROJECTION_KEY, TARGET_PROJECTION_KEY }  );
	}


	@Override
	public Feature< Link, int[] > compute( final Model model )
	{
		return new LinkIdFeature( model.getGraph(), spaceUnits, timeUnits );
	}

	@Override
	public Feature< ?, ? > deserialize( final File file, final Model support, final FileIdToGraphMap< ?, ? > fileIdToGraphMap ) throws IOException
	{
		return new LinkIdFeature( support.getGraph(), spaceUnits, timeUnits );
	}

	@Override
	public String getHelpString()
	{
		return HELP_STRING;
	}

	private static class LinkIdFeature implements Feature< Link, int[] >
	{

		private final Spot ref;

		private Map< String, FeatureProjection< Link > > projections;

		private final String lSpaceUnits;

		private final String lTimeUnits;

		public LinkIdFeature( final ModelGraph graph, final String spaceUnits, final String timeUnits )
		{
			lSpaceUnits = spaceUnits;
			lTimeUnits = timeUnits;
			this.ref = graph.vertexRef();
			this.projections = new HashMap<>( 2 );
			final IntFeatureProjection< Link > sourceProjection = new IntFeatureProjection< Link >()
			{
				private final Spot ref = graph.vertexRef();

				@Override
				public double value( final Link obj )
				{
					return obj.getSource( ref ).getInternalPoolIndex();
				}

				@Override
				public boolean isSet( final Link obj )
				{
					return true;
				}

				@Override
				public String units()
				{
					return FeatureUtil.dimensionToUnits( Dimension.NONE, lSpaceUnits, lTimeUnits );
				}
			};
			projections.put( SOURCE_PROJECTION_KEY, sourceProjection );
			final IntFeatureProjection< Link > targetProjection = new IntFeatureProjection< Link >()
			{
				private final Spot ref = graph.vertexRef();

				@Override
				public double value( final Link obj )
				{
					return obj.getTarget( ref ).getInternalPoolIndex();
				}

				@Override
				public boolean isSet( final Link obj )
				{
					return true;
				}

				@Override
				public String units()
				{
					// TODO Auto-generated method stub
					return null;
				}
			};
			projections.put( TARGET_PROJECTION_KEY, targetProjection );
		}

		@Override
		public String getKey()
		{
			return KEY;
		}

		@Override
		public Map< String, FeatureProjection< Link > > getProjections()
		{
			return projections;
		}

		@Override
		public Class< Link > getTargetClass()
		{
			return Link.class;
		}

		@Override
		public int[] get( final Link o, final int[] array )
		{
			array[ 0 ] = o.getSource( ref ).getInternalPoolIndex();
			array[ 1 ] = o.getTarget( ref ).getInternalPoolIndex();
			return array;
		}

		@Override
		public int[] get( final Link o )
		{
			return get( o, new int[ 2 ] );
		}

		@Override
		public boolean isSet( final Link o )
		{
			return true;
		}

		@Override
		public void serialize( final File file, final ObjectToFileIdMap< Link > idmap ) throws IOException
		{
			// Do nothing.
		}
	}
}
