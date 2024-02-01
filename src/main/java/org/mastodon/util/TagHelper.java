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

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;

import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.tag.ObjTagMap;
import org.mastodon.model.tag.TagSetStructure;

/**
 * Class that makes tagging of spots and links easier.
 * An instance of this class represents a tag in a tag set.
 * @see TagSetStructure.TagSet
 * @see TagSetStructure.Tag
 */
public class TagHelper
{

	private final Model model;

	private final TagSetStructure.TagSet tagSet;

	private final TagSetStructure.Tag tag;

	private final ObjTagMap< Spot, TagSetStructure.Tag > vertexTags;

	private final ObjTagMap< Link, TagSetStructure.Tag > edgeTags;

	public TagHelper( final Model model, final TagSetStructure.TagSet tagSet, final TagSetStructure.Tag tag )
	{
		this.model = Objects.requireNonNull( model );
		this.tagSet = Objects.requireNonNull( tagSet );
		this.tag = Objects.requireNonNull( tag );
		if ( !tagSet.getTags().contains( tag ) )
			throw new NoSuchElementException( "Tag " + tag + " does not belong to tag set " + tagSet );
		this.vertexTags = model.getTagSetModel().getVertexTags().tags( tagSet );
		this.edgeTags = model.getTagSetModel().getEdgeTags().tags( tagSet );
		if ( vertexTags == null || edgeTags == null )
			throw new NoSuchElementException( "Tag set " + tagSet + " is not registered in the model." );
	}

	public TagHelper( final Model model, final TagSetStructure.TagSet tagSet, final String tagLabel )
	{
		this( model, tagSet, TagSetUtils.findTag( tagSet, tagLabel ) );
	}

	public TagHelper( final Model model, final String tagSetName, final String tagLabel )
	{
		this( model, TagSetUtils.findTagSet( model, tagSetName ), tagLabel );
	}

	/**
	 * Returns the tag set this tag belongs to.
	 * 
	 * @return the tag set this tag belongs to.
	 */
	public TagSetStructure.TagSet getTagSet()
	{
		return tagSet;
	}

	/**
	 * Returns the tag that is represented by this {@link TagHelper}.
	 * 
	 * @return the tag.
	 */
	public TagSetStructure.Tag getTag()
	{
		return tag;
	}

	/**
	 * Assigns the tag to the specified spot.
	 * 
	 * @param spot
	 *            the spot to associate to tag to.
	 */
	public void tagSpot( final Spot spot )
	{
		vertexTags.set( spot, tag );
	}

	/**
	 * Assigns the tag to the specified link.
	 * 
	 * @param link
	 *            the link to associate the tag to.
	 */
	public void tagLink( final Link link )
	{
		edgeTags.set( link, tag );
	}

	/**
	 * Returns <code>true</code> is the specified spot is tagged with this tag.
	 * 
	 * @param spot
	 *            the spot.
	 * @return <code>true</code> is the specified spot is tagged with this tag.
	 */
	public boolean isTagged( final Spot spot )
	{
		return vertexTags.get( spot ) == tag;
	}

	/**
	 * Returns <code>true</code> is the specified link is tagged with this tag.
	 * 
	 * @param link
	 *            the link.
	 * @return <code>true</code> is the specified link is tagged with this tag.
	 */
	public boolean isTagged( final Link link )
	{
		return edgeTags.get( link ) == tag;
	}

	/**
	 * Assigns the tag to the specified spot and all its incoming edges.
	 * 
	 * @param spot
	 *            the spot.
	 */
	public void tagSpotAndIncomingEdges( final Spot spot )
	{
		tagSpot( spot );
		for ( final Link link : spot.incomingEdges() )
			tagLink( link );
	}

	/**
	 * Assigns the tag to the specified spot and all its outgoing edges.
	 * 
	 * @param spot
	 *            the spot.
	 */
	public void tagSpotAndOutgoingEdges( final Spot spot )
	{
		tagSpot( spot );
		for ( final Link link : spot.outgoingEdges() )
			tagLink( link );
	}

	/**
	 * Removes the tag from the specified spot. This also removes any other tag
	 * in the same tag set from the spot.
	 * 
	 * @param spot
	 *            the spot.
	 */
	public void untagSpot( final Spot spot )
	{
		vertexTags.remove( spot );
	}

	/**
	 * Removes the tag from the specified link. This also removes any other tag
	 * in the same tag set from the link.
	 * 
	 * @param link
	 *            the link.
	 */
	public void untagLink( final Link link )
	{
		edgeTags.remove( link );
	}

	/**
	 * Assigns the tag to the branch that the specified spot belongs to.
	 * 
	 * @param spot
	 *            the spot to find the branch from.
	 */
	public void tagBranch( final Spot spot )
	{
		forEachSpotAndLinkOfTheBranch( model.getGraph(), spot, this::tagSpot, this::tagLink );
	}

	/**
	 * Removes the tag from the branch that the specified spot belongs to. This
	 * also removes any other tag in the same tag set from the branch.
	 * 
	 * @param spot
	 *            the spot to find the branch from.
	 */
	public void untagBranch( final Spot spot )
	{
		forEachSpotAndLinkOfTheBranch( model.getGraph(), spot, this::untagSpot, this::untagLink );
	}

	/**
	 * Returns all spots that are tagged with this tag.
	 * 
	 * @return the spots.
	 */
	public Collection< Spot > getTaggedSpots()
	{
		return vertexTags.getTaggedWith( tag );
	}

	/**
	 * Returns all links that are tagged with this tag.
	 * 
	 * @return the links.
	 */
	public Collection< Link > getTaggedLinks()
	{
		return edgeTags.getTaggedWith( tag );
	}

	// -- Helper methods --

	/**
	 * Applies the specified actions to all spots and links of the branch that
	 *
	 * @param graph The graph that contains the branch.
	 * @param spot The spot that specifies the branch. This does not need to be
	 *             the first spot of the branch. It can be any spot of the
	 *             branch.
	 * @param spotAction This action is performed on each spot of the branch.
	 * @param linkAction This action is performed on each link of the branch.
	 */
	private void forEachSpotAndLinkOfTheBranch( final ModelGraph graph, final Spot spot, final Consumer< Spot > spotAction, final Consumer< Link > linkAction )
	{
		Spot s = graph.vertexRef();
		try
		{
			spotAction.accept( spot );
			//forward
			s.refTo( spot );
			while ( s.outgoingEdges().size() == 1 )
			{
				final Link link = s.outgoingEdges().get( 0 );
				s = link.getTarget( s );
				if ( s.incomingEdges().size() != 1 )
					break;
				spotAction.accept( s );
				linkAction.accept( link );
			}
			// backward
			s.refTo( spot );
			while ( s.incomingEdges().size() == 1 )
			{
				final Link link = s.incomingEdges().get( 0 );
				s = link.getSource( s );
				if ( s.outgoingEdges().size() != 1 )
					break;
				spotAction.accept( s );
				linkAction.accept( link );
			}
		}
		finally
		{
			graph.releaseRef( s );
		}
	}
}
