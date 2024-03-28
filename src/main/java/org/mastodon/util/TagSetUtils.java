/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2024 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.model.tag.ObjTagMap;
import org.mastodon.model.tag.TagSetModel;
import org.mastodon.model.tag.TagSetStructure;

/**
 * Collection of utilities related to manipulating
 * {@link org.mastodon.model.tag.TagSetStructure.TagSet}s.
 *
 * @author Matthias Arzt
 * @author Stefan Hahmann
 * @see TagSetStructure
 */
public class TagSetUtils
{
	private TagSetUtils()
	{
		// prevent instantiation of utility class
	}

	/**
	 * Help creating a visible color value by adding a (full opacity) alpha
	 * channel.
	 * 
	 * @param rgbAsBottom24bits
	 *            The encoded RGB triplet.
	 * @return The int value of the RGB color, ready to use with
	 *         {@link org.mastodon.model.tag.TagSetStructure.Tag}.
	 */
	public static int rgbToValidColor( final int rgbAsBottom24bits )
	{
		return 0xFF000000 | rgbAsBottom24bits;
	}

	/**
	 * Help creating a visible color value by adding a (full opacity) alpha
	 * channel.
	 * 
	 * @param r
	 *            0-255 valued red channel.
	 * @param g
	 *            0-255 valued green channel.
	 * @param b
	 *            0-255 valued blue channel.
	 * @return The int value of the RGB color, ready to use with
	 *         {@link org.mastodon.model.tag.TagSetStructure.Tag}.
	 */
	public static int rgbToValidColor( final int r, final int g, final int b )
	{
		return 0xFF000000 | ( ( r & 0xFF ) << 16 ) | ( ( g & 0xFF ) << 8 ) | ( b & 0xFF );
	}

	/**
	 * Help creating a fully-described color value.
	 * 
	 * @param r
	 *            0-255 valued red channel.
	 * @param g
	 *            0-255 valued green channel.
	 * @param b
	 *            0-255 valued blue channel.
	 * @param alpha
	 *            0-255 valued opacity (alpha) channel, 0 - fully transparent,
	 *            255 - fully opaque.
	 * @return The int value of the RGB color, ready to use with
	 *         {@link org.mastodon.model.tag.TagSetStructure.Tag}.
	 */
	public static int rgbaToValidColor( final int r, final int g, final int b, final int alpha )
	{
		return ( ( alpha & 0xFF ) << 24 ) | ( ( r & 0xFF ) << 16 ) | ( ( g & 0xFF ) << 8 ) | ( b & 0xFF );
	}

	/**
	 * Add a new tag set to the given model. The color values could be created,
	 * e.g., with the {@link TagSetUtils#rgbToValidColor(int, int, int)}.
	 * 
	 * @param model
	 *            The model that will contain the new tag set.
	 * @param name
	 *            The name of the new tag set.
	 * @param tagsAndColors
	 *            The list of labels and colors for the new tags. This could be
	 *            a {@link Map#entrySet()} or a list of
	 *            {@link org.apache.commons.lang3.tuple.Pair}s.
	 * @return The new tag set.
	 */
	public static TagSetStructure.TagSet addNewTagSetToModel( final Model model, final String name,
			final Collection< ? extends Map.Entry< String, Integer > > tagsAndColors )
	{
		final TagSetModel< Spot, Link > tagSetModel = model.getTagSetModel();
		final TagSetStructure original = tagSetModel.getTagSetStructure();
		final TagSetStructure replacement = new TagSetStructure();
		replacement.set( original );
		final TagSetStructure.TagSet newTagSet = replacement.createTagSet( name );
		for ( final Map.Entry< String, Integer > tag : tagsAndColors )
			newTagSet.createTag( tag.getKey(), tag.getValue() );
		tagSetModel.setTagSetStructure( replacement );
		return newTagSet;
	}

	/**
	 * Assigns the specified {@code tag} to the given {@code spot}.
	 * 
	 * @param model
	 *            the model to get the tag-set model from.
	 * @param tagSet
	 *            the tag-set.
	 * @param tag
	 *            the tag in the tag-set.
	 * @param spot
	 *            the spot.
	 */
	public static void tagSpot( final Model model, final TagSetStructure.TagSet tagSet, final TagSetStructure.Tag tag, final Spot spot )
	{
		model.getTagSetModel().getVertexTags().tags( tagSet ).set( spot, tag );
	}

	/**
	 * Assigns the specified {@code tag} to the given {@code spot} and all of
	 * its outgoing edges.
	 * 
	 * @param model
	 *            the model to get the tag-set model from.
	 * @param tagSet
	 *            the tag-set.
	 * @param tag
	 *            the tag in the tag-set.
	 * @param spot
	 *            the spot.
	 */
	public static void tagSpotAndOutgoingEdges( final Model model, final TagSetStructure.TagSet tagSet, final TagSetStructure.Tag tag, final Spot spot )
	{
		tagSpot( model, tagSet, tag, spot );
		tagLinks( model, tagSet, tag, spot.outgoingEdges() );
	}

	/**
	 * Assigns the specified {@code tag} to the given {@code spot} and all of
	 * its incoming edges.
	 * 
	 * @param model
	 *            the model to get the tag-set model from.
	 * @param tagSet
	 *            the tag-set.
	 * @param tag
	 *            the tag in the tag-set.
	 * @param spot
	 *            the spot.
	 */
	public static void tagSpotAndIncomingEdges( final Model model, final Spot spot, final TagSetStructure.TagSet tagSet, final TagSetStructure.Tag tag )
	{
		tagSpot( model, tagSet, tag, spot );
		tagLinks( model, tagSet, tag, spot.incomingEdges() );
	}

	/**
	 * Assigns the specified {@code tag} to the given {@code spots}.
	 * 
	 * @param model
	 *            the model to get the tag-set model from.
	 * @param tagSet
	 *            the tag-set.
	 * @param tag
	 *            the tag in the tag-set.
	 * @param spots
	 *            the spots.
	 */
	public static void tagSpots( final Model model, final TagSetStructure.TagSet tagSet, final TagSetStructure.Tag tag, final Iterable< Spot > spots )
	{
		final ObjTagMap< Spot, TagSetStructure.Tag > vertexTags = model.getTagSetModel().getVertexTags().tags( tagSet );
		for ( final Spot spot : spots )
			vertexTags.set( spot, tag );
	}

	/**
	 * Assigns the specified {@code tag} to the given {@code links}.
	 * 
	 * @param model
	 *            the model to get the tag-set model from.
	 * @param tagSet
	 *            the tag-set.
	 * @param tag
	 *            the tag in the tag-set.
	 * @param links
	 *            the links.
	 */
	public static void tagLinks( final Model model, final TagSetStructure.TagSet tagSet, final TagSetStructure.Tag tag, final Iterable< Link > links )
	{
		final ObjTagMap< Link, TagSetStructure.Tag > edgeTags = model.getTagSetModel().getEdgeTags().tags( tagSet );
		for ( final Link link : links )
			edgeTags.set( link, tag );
	}

	/**
	 * Assigns the specified tag to all tags and spots that belong to the same
	 * branch as the given spot.
	 * 
	 * @param model
	 *            Model that contains the graph and the tag data structures.
	 * @param tagSet
	 *            The {@link TagSetStructure.TagSet} the tag belongs to.
	 * @param tag
	 *            The tag to assign to the branch.
	 * @param spot
	 *            The spot, that specifies the branch to be tagged. Doesn't need
	 *            to be the branch start or branch end. It can be any spot on
	 *            the branch.
	 */
	public static void tagBranch( final Model model, final TagSetStructure.TagSet tagSet, final TagSetStructure.Tag tag, final Spot spot )
	{
		final ModelGraph graphA = model.getGraph();
		Spot s = graphA.vertexRef();
		try
		{
			final ObjTagMap< Spot, TagSetStructure.Tag > vertexTags = model.getTagSetModel().getVertexTags().tags( tagSet );
			final ObjTagMap< Link, TagSetStructure.Tag > edgeTags = model.getTagSetModel().getEdgeTags().tags( tagSet );
			vertexTags.set( spot, tag );
			// forward
			s.refTo( spot );
			while ( s.outgoingEdges().size() == 1 )
			{
				final Link link = s.outgoingEdges().get( 0 );
				s = link.getTarget( s );
				if ( s.incomingEdges().size() != 1 )
					break;
				edgeTags.set( link, tag );
				vertexTags.set( s, tag );
			}
			// backward
			s.refTo( spot );
			while ( s.incomingEdges().size() == 1 )
			{
				final Link link = s.incomingEdges().get( 0 );
				s = link.getSource( s );
				if ( s.outgoingEdges().size() != 1 )
					break;
				edgeTags.set( link, tag );
				vertexTags.set( s, tag );
			}
		}
		finally
		{
			graphA.releaseRef( s );
		}
	}

	/**
	 * If all spots and links of the branch have the same tag, return that tag,
	 * return null otherwise.
	 * 
	 * @param model
	 *            The model that contains the graph and the tags.
	 * @param tagSet
	 *            The tag set to consider.
	 * @param branchStart
	 *            The spot at the start of the branch.
	 * @return the tag or null.
	 */
	public static TagSetStructure.Tag getBranchTag( final Model model, final TagSetStructure.TagSet tagSet, final Spot branchStart )
	{
		final ModelGraph graphA = model.getGraph();
		Spot s = graphA.vertexRef();
		try
		{
			final ObjTagMap< Spot, TagSetStructure.Tag > vertexTags = model.getTagSetModel().getVertexTags().tags( tagSet );
			final ObjTagMap< Link, TagSetStructure.Tag > edgeTags = model.getTagSetModel().getEdgeTags().tags( tagSet );
			final TagSetStructure.Tag tag = vertexTags.get( branchStart );
			if ( tag == null )
				return null;
			// forward
			s.refTo( branchStart );
			while ( s.outgoingEdges().size() == 1 )
			{
				final Link link = s.outgoingEdges().get( 0 );
				s = link.getTarget( s );
				if ( s.incomingEdges().size() != 1 )
					break;
				if ( !tag.equals( edgeTags.get( link ) ) )
					return null;
				if ( !tag.equals( vertexTags.get( s ) ) )
					return null;
			}
			return tag;
		}
		finally
		{
			graphA.releaseRef( s );
		}
	}

	/**
	 * Returns the first tag-set that matches the given name.
	 * 
	 * @param model
	 *            the model to get the tag-set model from.
	 * @param name
	 *            the name to search for.
	 * @return the tag-set.
	 * @throws NoSuchElementException
	 *             if the tag-set was not found.
	 */
	public static TagSetStructure.TagSet findTagSet( final Model model, final String name )
	{
		for ( final TagSetStructure.TagSet tagSet : model.getTagSetModel().getTagSetStructure().getTagSets() )
			if ( name.equals( tagSet.getName() ) )
				return tagSet;
		throw new NoSuchElementException( "Did not find a tag set with the given name: " + name );
	}

	public static TagSetStructure.Tag findTag( final TagSetStructure.TagSet tagSet, final String tagLabel )
	{
		for ( final TagSetStructure.Tag tag : tagSet.getTags() )
			if ( tagLabel.equals( tag.label() ) )
				return tag;
		throw new NoSuchElementException( "Did not find a tag with the given label: " + tagLabel );
	}

	/**
	 * Returns the names of all tag sets in the model.
	 * @param model the model to get the tag-set model from.
	 * @return the names of all tag sets in the model.
	 */
	public static List< String > getTagSetNames( final Model model )
	{
		List< String > tagSetNames = new ArrayList<>();
		model.getTagSetModel().getTagSetStructure().getTagSets().forEach( tagSet -> tagSetNames.add( tagSet.getName() ) );
		return tagSetNames;
	}

	/**
	 * Gets the tag label of the first spot in the given branchSpot within the given tagSet.
	 * @param model the model to which the branch belongs
	 * @param branchSpot the branch spot
	 * @param tagSet the tag set
	 * @return the tag label
	 */
	public static String getTagLabel( final Model model, final BranchSpot branchSpot, final TagSetStructure.TagSet tagSet )
	{
		if ( model == null || branchSpot == null || tagSet == null )
			return null;
		Spot first = TreeUtils.getFirstSpot( model, branchSpot );
		TagSetStructure.Tag tag = TagSetUtils.getBranchTag( model, tagSet, first );
		return tag == null ? null : tag.label();
	}
}
