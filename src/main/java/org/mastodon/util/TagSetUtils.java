package org.mastodon.util;

import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.tag.ObjTagMap;
import org.mastodon.model.tag.TagSetModel;
import org.mastodon.model.tag.TagSetStructure;

import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Collection of utilities related to manipulating {@link org.mastodon.model.tag.TagSetStructure.TagSet}s.
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
	 * Add a new tag set to the given model.
	 * @param model The model that will contain the new tag set.
	 * @param name The name of the new tag set.
	 * @param tagsAndColors The list of labels and colors for the new tags. This
	 *            could be a {@link Map#entrySet()} or a list of
	 *            {@link org.apache.commons.lang3.tuple.Pair}s.
	 * @return The new tag set.
	 */
	public static TagSetStructure.TagSet addNewTagSetToModel( Model model, String name,
			Collection< ? extends Map.Entry< String, Integer > > tagsAndColors )
	{
		TagSetModel< Spot, Link > tagSetModel = model.getTagSetModel();
		TagSetStructure original = tagSetModel.getTagSetStructure();
		TagSetStructure newTss = new TagSetStructure();
		newTss.set( original );
		TagSetStructure.TagSet newTagSet = newTss.createTagSet( name );
		for ( Map.Entry< String, Integer > tag : tagsAndColors )
			newTagSet.createTag( tag.getKey(), tag.getValue() );
		tagSetModel.setTagSetStructure( newTss );
		return newTagSet;
	}

	/**
	 * Assigns the specified {@code tag} to the given {@code spot} and all of its outgoing edges.
	 * @param model Model that contains the graph and the tag data structures.
	 * @param spot The spot, that specifies the branch to be tagged.
	 *             Doesn't need to be the branch start or branch end.
	 *             It can be any spot on the branch.   
	 * @param tagSet The {@link TagSetStructure.TagSet} the tag belongs to.
	 * @param tag The tag to assign to the branch.
	 */
	public static void tagSpotAndLinks( Model model, Spot spot, TagSetStructure.TagSet tagSet, TagSetStructure.Tag tag )
	{
		ObjTagMap< Spot, TagSetStructure.Tag > vertexTags = model.getTagSetModel().getVertexTags().tags( tagSet );
		ObjTagMap< Link, TagSetStructure.Tag > edgeTags = model.getTagSetModel().getEdgeTags().tags( tagSet );
		vertexTags.set( spot, tag );
		for ( Link link : spot.outgoingEdges() )
			edgeTags.set( link, tag );
	}

	/**
	 * Assigns the specified tag to all tags and spots that belong to the same branch as the given spot.
	 * @param model Model that contains the graph and the tag data structures.
	 * @param tagSet The {@link TagSetStructure.TagSet} the tag belongs to.
	 * @param tag The tag to assign to the branch.
	 * @param spot The spot, that specifies the branch to be tagged.
	 *             Doesn't need to be the branch start or branch end.
	 *             It can be any spot on the branch.
	 */
	public static void tagBranch( Model model, TagSetStructure.TagSet tagSet, TagSetStructure.Tag tag, Spot spot )
	{
		ModelGraph graphA = model.getGraph();
		Spot s = graphA.vertexRef();
		try
		{
			ObjTagMap< Spot, TagSetStructure.Tag > vertexTags = model.getTagSetModel().getVertexTags().tags( tagSet );
			ObjTagMap< Link, TagSetStructure.Tag > edgeTags = model.getTagSetModel().getEdgeTags().tags( tagSet );
			vertexTags.set( spot, tag );
			//forward
			s.refTo( spot );
			while ( s.outgoingEdges().size() == 1 )
			{
				Link link = s.outgoingEdges().get( 0 );
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
				Link link = s.incomingEdges().get( 0 );
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
	 * If all spots and links of the branch have the same tag, return that tag, return null otherwise.
	 * @param model The model that contains the graph and the tags.
	 * @param tagSet The tag set to consider.
	 * @param branchStart The spot at the start of the branch.
	 * @return the tag or null.
	 */
	public static TagSetStructure.Tag getBranchTag( Model model, TagSetStructure.TagSet tagSet, Spot branchStart )
	{
		ModelGraph graphA = model.getGraph();
		Spot s = graphA.vertexRef();
		try
		{
			ObjTagMap< Spot, TagSetStructure.Tag > vertexTags = model.getTagSetModel().getVertexTags().tags( tagSet );
			ObjTagMap< Link, TagSetStructure.Tag > edgeTags = model.getTagSetModel().getEdgeTags().tags( tagSet );
			TagSetStructure.Tag tag = vertexTags.get( branchStart );
			if ( tag == null )
				return null;
			//forward
			s.refTo( branchStart );
			while ( s.outgoingEdges().size() == 1 )
			{
				Link link = s.outgoingEdges().get( 0 );
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
	 * Returns a map of all tags in the given tag set, indexed by their label.
	 */
	public static Map< String, TagSetStructure.Tag > tagSetAsMap( TagSetStructure.TagSet tagSet )
	{
		return tagSet.getTags().stream()
				.collect( Collectors.toMap( TagSetStructure.Tag::label, tag -> tag ) );
	}

	/**
	 * @return the first tag set that matches the given name.
	 * @throws NoSuchElementException if the tagset was not found.
	 */
	public static TagSetStructure.TagSet findTagSet( TagSetModel< Spot, Link > tagsModel, String name )
	{
		for ( TagSetStructure.TagSet tagSet : tagsModel.getTagSetStructure().getTagSets() )
			if ( name.equals( tagSet.getName() ) )
				return tagSet;
		throw new NoSuchElementException( "Did not find a tag set with the given name: " + name );
	}
}
