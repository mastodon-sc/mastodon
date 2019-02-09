package org.mastodon.tomancak;

import java.util.HashMap;
import java.util.Map;
import org.mastodon.revised.model.tag.TagSetStructure;
import org.mastodon.revised.model.tag.TagSetStructure.Tag;
import org.mastodon.revised.model.tag.TagSetStructure.TagSet;

public class MergeTags
{
	static class TagId
	{
		final String tagSetName;

		final String tagName;

		public TagId( final String tagSetName, final String tagName )
		{
			this.tagSetName = tagSetName;
			this.tagName = tagName;
		}

		@Override
		public boolean equals( final Object o )
		{
			if ( this == o )
				return true;
			if ( !( o instanceof TagId ) )
				return false;
			final TagId tagId = ( TagId ) o;
			return tagSetName.equals( tagId.tagSetName ) && tagName.equals( tagId.tagName );
		}

		@Override
		public int hashCode()
		{
			int result = tagSetName.hashCode();
			result = 31 * result + tagName.hashCode();
			return result;
		}
	}

	static void mergeTagSetStructures( TagSetStructure tssA, TagSetStructure tssB )
	{
		final TagSetStructure mergedTagSetStructure = new TagSetStructure();
		final Map< Tag, TagSet > mergedTagToMergedTagSet = new HashMap<>();
		final Map< String, TagSet > nameToMergedTagSet = new HashMap<>();
		final Map< TagId, Tag > tagIdToMergedTag = new HashMap<>();

		// copy tssA to mergedTagSetStructure
		final Map< Tag, Tag > aToMerged = new HashMap<>();
		for ( TagSet tagSet : tssA.getTagSets() )
		{
			final String tsn = tagSet.getName();
			final TagSet mergedTagSet = nameToMergedTagSet.computeIfAbsent( tsn, mergedTagSetStructure::createTagSet );

			for ( Tag tag : tagSet.getTags() )
			{
				final String tn = tag.label();
				final int tc = tag.color();

				Tag mergedTag = mergedTagSet.createTag( tn,  tc );
				mergedTagToMergedTagSet.put( mergedTag, mergedTagSet );
				tagIdToMergedTag.put( new TagId( tsn, tn ), mergedTag );
				aToMerged.put( tag, mergedTag );
			}
		}

		// merge tssB into mergedTagSetStructure (add tags that are not present)
		final Map< Tag, Tag > bToMerged = new HashMap<>();
		for ( TagSet tagSet : tssB.getTagSets() )
		{
			final String tsn = tagSet.getName();
			final TagSet mergedTagSet = nameToMergedTagSet.computeIfAbsent( tsn, mergedTagSetStructure::createTagSet );

			for ( Tag tag : tagSet.getTags() )
			{
				final String tn = tag.label();
				final int tc = tag.color();

				Tag mergedTag = getTag( mergedTagSet, tn );
				if ( mergedTag == null )
				{
					mergedTag = mergedTagSet.createTag( tn, tc );
					mergedTagToMergedTagSet.put( mergedTag, mergedTagSet );
					tagIdToMergedTag.put( new TagId( tsn, tn ), mergedTag );
				}
				bToMerged.put( tag, mergedTag );
			}
		}

		System.out.println( "tssA = " + tssA );
		System.out.println( "=======================================\n\n" );
		System.out.println( "tssB = " + tssB );
		System.out.println( "=======================================\n\n" );
		System.out.println( "mergedTagSetStructure = " + mergedTagSetStructure );
	}

	private static Tag getTag( final TagSet tagSet, final String tagName )
	{
		return tagSet.getTags().stream()
				.filter( tag -> tag.label().equals( tagName ) )
				.findFirst()
				.orElse( null );
	}
}
