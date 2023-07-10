package org.mastodon.util;

import java.util.Collection;
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
 */
public class TagHelper
{

	private final Model model;

	private final TagSetStructure.TagSet tagSet;

	private final TagSetStructure.Tag tag;

	private final ObjTagMap< Spot, TagSetStructure.Tag > vertexTags;

	private final ObjTagMap< Link, TagSetStructure.Tag > edgeTags;

	public TagHelper( Model model, TagSetStructure.TagSet tagSet, TagSetStructure.Tag tag )
	{
		this.model = model;
		this.tagSet = tagSet;
		this.tag = tag;
		this.vertexTags = model.getTagSetModel().getVertexTags().tags( tagSet );
		this.edgeTags = model.getTagSetModel().getEdgeTags().tags( tagSet );
	}

	public TagHelper( Model model, TagSetStructure.TagSet tagSet, String tagLabel )
	{
		this( model, tagSet, TagSetUtils.findTag( tagSet, tagLabel ) );
	}

	public TagHelper( Model model, String tagSetName, String tagLabel )
	{
		this( model, TagSetUtils.findTagSet( model, tagSetName ), tagLabel );
	}

	/**
	 * Returns the tag set this tag belongs to.
	 */
	public TagSetStructure.TagSet getTagSet()
	{
		return tagSet;
	}

	/**
	 * Returns the tag that is represented by this {@link TagHelper}.
	 */
	public TagSetStructure.Tag getTag()
	{
		return tag;
	}

	/**
	 * Assigns the tag to the specified spot.
	 */
	public void tagSpot( Spot spot )
	{
		vertexTags.set( spot, tag );
	}

	/**
	 * Assigns the tag to the specified link.
	 */
	public void tagLink( Link link )
	{
		edgeTags.set( link, tag );
	}

	/**
	 * Returns true is the specified spot is tagged with this tag.
	 */
	public boolean isTagged( Spot spot )
	{
		return vertexTags.get( spot ) == tag;
	}

	/**
	 * Returns true is the specified link is tagged with this tag.
	 */
	public boolean isTagged( Link link )
	{
		return edgeTags.get( link ) == tag;
	}

	/**
	 * Assigns the tag to the specified spot and all its incoming edges.
	 */
	public void tagSpotAndIncomingEdges( Spot spot )
	{
		tagSpot( spot );
		for ( Link link : spot.incomingEdges() )
			tagLink( link );
	}

	/**
	 * Assigns the tag to the specified spot and all its outgoing edges.
	 */
	public void tagSpotAndOutgoingEdges( Spot spot )
	{
		tagSpot( spot );
		for ( Link link : spot.outgoingEdges() )
			tagLink( link );
	}

	/**
	 * Removes the tag from the specified spot. This also removes any other
	 * tag in the same tag set from the spot.
	 */
	public void untagSpot( Spot spot )
	{
		vertexTags.remove( spot );
	}

	/**
	 * Removes the tag from the specified link. This also removes any other
	 * tag in the same tag set from the link.
	 */
	public void untagLink( Link link )
	{
		edgeTags.remove( link );
	}

	/**
	 * Assigns the tag to the branch that the specified spot belongs to.
	 */
	public void tagBranch( Spot spot )
	{
		forEachSpotAndLinkOfTheBranch( model.getGraph(), spot, this::tagSpot, this::tagLink );
	}

	/**
	 * Removes the tag from the branch that the specified spot belongs to.
	 * This also removes any other tag in the same tag set from the branch.
	 */
	public void untagBranch( Spot spot )
	{
		forEachSpotAndLinkOfTheBranch( model.getGraph(), spot, this::untagSpot, this::untagLink );
	}

	/**
	 * Returns all spots that are tagged with this tag.
	 */
	public Collection< Spot > getTaggedSpots()
	{
		return vertexTags.getTaggedWith( tag );
	}

	/**
	 * Returns all links that are tagged with this tag.
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
	private void forEachSpotAndLinkOfTheBranch( ModelGraph graph, Spot spot, Consumer< Spot > spotAction, Consumer< Link > linkAction )
	{
		Spot s = graph.vertexRef();
		try
		{
			spotAction.accept( spot );
			//forward
			s.refTo( spot );
			while ( s.outgoingEdges().size() == 1 )
			{
				Link link = s.outgoingEdges().get( 0 );
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
				Link link = s.incomingEdges().get( 0 );
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
