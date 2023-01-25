package org.mastodon.mamut.tags;

import java.io.IOException;
import java.util.Collection;

import org.mastodon.mamut.WindowManager;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.project.MamutProjectIO;
import org.mastodon.model.tag.TagSetModel;
import org.mastodon.model.tag.TagSetStructure;
import org.mastodon.model.tag.TagSetStructure.Tag;
import org.mastodon.model.tag.TagSetStructure.TagSet;
import org.scijava.Context;

import mpicbg.spim.data.SpimDataException;

public class PlayingWithTagsExample
{

	public static void main( final String[] args ) throws IOException, SpimDataException
	{

		final WindowManager wm = new WindowManager( new Context() );
		wm.getProjectManager().open( new MamutProjectIO().load( "/Users/tinevez/Development/MastodonWS/mastodon/samples/drosophila_crop.mastodon" ) );

		final Model model = wm.getAppModel().getModel();
		final TagSetModel< Spot, Link > tsm = model.getTagSetModel();

		/*
		 * Inspect the tag-set structure.
		 */

		final TagSetStructure tss = tsm.getTagSetStructure();
		System.out.println( "Tag-set strucutre: " + tss.toString() );

		/*
		 * Take one tag-set and one tag.
		 */

		final TagSet ts = tss.getTagSets().get( 0 );
		final Tag tag = ts.getTags().get( 0 );

		/*
		 * Tag some spots and links with this tag.
		 */

		System.out.println( "\nSetting tags" );
		int i = 0;
		final int N = 5;
		for ( final Spot spot : model.getGraph().vertices() )
		{
			System.out.println( "Tagging " + spot + " with " + tag );
			tsm.getVertexTags().set( spot, tag );
			if ( ++i > N )
				break;
		}
		i = 0;
		for ( final Link link : model.getGraph().edges() )
		{
			System.out.println( "Tagging " + link + " with " + tag );
			tsm.getEdgeTags().set( link, tag );
			if ( ++i > N )
				break;
		}

		/*
		 * Get the spots that are tagged with a certain tag.
		 */

		System.out.println( "\nSpots tagged with " + tag + ": " );
		final Collection< Spot > spotsTaggedWith = tsm.getVertexTags().getTaggedWith( tag );
		for ( final Spot spot : spotsTaggedWith )
			System.out.println( " - " + spot );

		/*
		 * Get the links that are tagged with a certain tag.
		 */

		System.out.println( "\nLinks tagged with " + tag + ": " );
		final Collection< Link > linksTaggedWith = tsm.getEdgeTags().getTaggedWith( tag );
		for ( final Link link : linksTaggedWith )
			System.out.println( " - " + link );


	}
}
