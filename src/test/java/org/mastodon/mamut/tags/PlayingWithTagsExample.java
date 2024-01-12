/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2023 Tobias Pietzsch, Jean-Yves Tinevez
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.mamut.tags;

import java.io.IOException;
import java.util.Collection;

import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.io.ProjectLoader;
import org.mastodon.mamut.io.project.MamutProjectIO;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
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

		final ProjectModel appModel = ProjectLoader.open( MamutProjectIO.load( "/Users/tinevez/Development/MastodonWS/mastodon/samples/drosophila_crop.mastodon" ), new Context() );
		final Model model = appModel.getModel();
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
