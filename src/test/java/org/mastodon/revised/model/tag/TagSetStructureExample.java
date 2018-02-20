package org.mastodon.revised.model.tag;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.mastodon.revised.model.tag.TagSetStructure.TagSet;

public class TagSetStructureExample
{

	public static void main( final String[] args ) throws IOException
	{
		// A first tag set structure.
		final TagSetStructure other = new TagSetStructure();
		final TagSet preferredFruit = other.createTagSet( "Preferred fruit" );
		preferredFruit.createTag( "Banana", Color.YELLOW.getRGB() );
		preferredFruit.createTag( "Tomato", Color.RED.getRGB() );
		preferredFruit.createTag( "Mango", Color.ORANGE.getRGB() );
		System.out.println( "A first TagSetStructure:\n" + other );

		// Load a tag set structure. The id should not clash?
		final File file = new File( "samples/tagsetstructure.raw" );
		final TagSetStructure loadedtss = new TagSetStructure();
		loadedtss.loadRaw( file );
		System.out.println( "\nReloaded TagSetStructure:\n" + loadedtss );
	}

	public static void main3( final String[] args ) throws IOException
	{
		final File file = new File( "samples/tagsetstructure.raw" );
		final TagSetStructure loadedtss = new TagSetStructure();
		loadedtss.loadRaw( file );
		System.out.println( "\nReloaded TagSetStructure:\n" + loadedtss );

		// Another tag set structure. The ids should not clash.
		final TagSetStructure other = new TagSetStructure();
		final TagSet preferredFruit = other.createTagSet( "Preferred fruit" );
		preferredFruit.createTag( "Banana", Color.YELLOW.getRGB() );
		preferredFruit.createTag( "Tomato", Color.RED.getRGB() );
		preferredFruit.createTag( "Mango", Color.ORANGE.getRGB() );
		System.out.println( "\nAnother TagSetStructure:\n" + other );
	}

	public static void main2( final String[] args ) throws IOException
	{
		final Random ran = new Random( 0l );

		// First tag set structure.
		final TagSetStructure tss = new TagSetStructure();
		final TagSet reviewedByTag = tss.createTagSet( "Reviewed by" );
		reviewedByTag.createTag( "Pavel", ran.nextInt() | 0xFF000000 );
		reviewedByTag.createTag( "Mette", ran.nextInt() | 0xFF000000 );
		reviewedByTag.createTag( "Tobias", ran.nextInt() | 0xFF000000 );
		reviewedByTag.createTag( "JY", ran.nextInt() | 0xFF000000 );
		final TagSet locationTag = tss.createTagSet( "Location" );
		locationTag.createTag( "Anterior", ran.nextInt() | 0xFF000000 );
		locationTag.createTag( "Posterior", ran.nextInt() | 0xFF000000 );
		System.out.println( "Initial TagSetStructure:\n" + tss );

		// Save it.
		final File file = new File( "samples/tagsetstructure.raw" );
		tss.saveRaw( file );

		// Another tag set structure.
		final TagSetStructure other = new TagSetStructure();
		final TagSet preferredFruit = other.createTagSet( "Preferred fruit" );
		preferredFruit.createTag( "Banana", Color.YELLOW.getRGB() );
		preferredFruit.createTag( "Tomato", Color.RED.getRGB() );
		preferredFruit.createTag( "Mango", Color.ORANGE.getRGB() );
		System.out.println( "\nAnother TagSetStructure:\n" + other );

		// Copy tag set structure.
		final TagSetStructure copy = new TagSetStructure();
		copy.createTagSet( "This should disappear" ).createTag( "Did it?", Color.BLACK.getRGB() );
		copy.set( tss );
		System.out.println( "\nCopied TagSetStructure:\n" + copy );

		// Reload saved tag set structure.
		final TagSetStructure loadedtss = new TagSetStructure();
		loadedtss.createTagSet( "This should disappear too." ).createTag( "Right?", Color.WHITE.getRGB() );
		loadedtss.loadRaw( file );
		System.out.println( "\nReloaded TagSetStructure:\n" + loadedtss );
	}
}
