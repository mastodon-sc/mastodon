package org.mastodon.util;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import org.apache.commons.lang3.tuple.Pair;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph1;
import org.mastodon.model.tag.TagSetStructure;

/**
 * This class is a demo to show that the maximum number of tags that a Mastodon Model can have is 31,619.
 * Try to increase the number of tags to 31,620 and the program will create a memory related error.
 */
public class MaxTagSetsDemo
{
	private static final Random random = new Random( 42 );

	private static final int MAX_TAG_SETS = 31_619;

	public static void main( String[] args )
	{
		ExampleGraph1 exampleGraph1 = new ExampleGraph1();

		Collection< Pair< String, Integer > > labelColorPairs = new ArrayList<>();
		for ( int i = 0; i < MAX_TAG_SETS; i++ )
			labelColorPairs.add( Pair.of( "tag" + i, getRandomColor().getRGB() ) );

		TagSetUtils.addNewTagSetToModel( exampleGraph1.getModel(), "testTagsSet0", labelColorPairs );
		TagSetStructure.TagSet tagSet = exampleGraph1.getModel().getTagSetModel().getTagSetStructure().getTagSets().get( 0 );

		System.out.println( "Checking tag set..." );
		System.out.println( "Tag set name: " + tagSet.getName() );
		System.out.println( "Number of tags: " + tagSet.getTags().size() );
		System.out.println( "Done." );
	}

	private static Color getRandomColor()
	{
		// Generate random RGB values
		int red = random.nextInt( 256 );
		int green = random.nextInt( 256 );
		int blue = random.nextInt( 256 );

		// Create the color using the RGB values
		return new Color( red, green, blue );
	}
}
