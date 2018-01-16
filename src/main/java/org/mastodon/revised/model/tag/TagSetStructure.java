package org.mastodon.revised.model.tag;

import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TagSetStructure
{
	private static final AtomicInteger tagSetIDgenerator = new AtomicInteger( 0 );

	private static final AtomicInteger tagIDgenerator = new AtomicInteger( 0 );

	private final List< TagSet > tagSets;

	public TagSetStructure()
	{
		this.tagSets = new ArrayList<>();
	}

	/**
	 * Replaces the content of this {@link TagSetStructure} by the one
	 * specified.
	 *
	 * @param other
	 *            the {@link TagSetStructure} to read from.
	 */
	public void set( final TagSetStructure other )
	{
		tagSets.clear();
		for ( final TagSet tagSet : other.tagSets )
			tagSets.add( tagSet.copy() );
	}

	public void saveRaw( final File file ) throws IOException
	{
		try (DataOutputStream dos = new DataOutputStream(
				new BufferedOutputStream(
						new FileOutputStream( file ) ) ))
		{
			// N tagsets.
			dos.writeInt( tagSets.size() );
			// Individual tagsets.
			for ( final TagSet tagSet : tagSets )
			{
				// TagSet id.
				dos.writeInt( tagSet.id );
				// TagSet name.
				dos.writeUTF( tagSet.name );
				// Tags.
				tagSet.write( dos );
			}
		}
	}

	public void loadRaw( final File file ) throws IOException
	{
		tagSets.clear();
		try (final DataInputStream dis = new DataInputStream(
				new BufferedInputStream(
						new FileInputStream( file ) ) ))
		{
			// N tagsets.
			final int nTagSets = dis.readInt();
			for ( int i = 0; i < nTagSets; i++ )
			{
				// TagSet id.
				final int tagSetId = dis.readInt();
				if (tagSetId >= tagSetIDgenerator.get())
					tagSetIDgenerator.set( tagSetId + 1 );
				// TagSet name.
				final String tagSetName = dis.readUTF();
				final TagSet tagSet = new TagSet( tagSetId, tagSetName );
				// Tags.
				tagSet.read( dis );
				tagSets.add( tagSet );
			}
		}
	}

	public TagSet createTagSet( final String name )
	{
		final int id = tagSetIDgenerator.getAndIncrement();
		final TagSet tagSet = new TagSet( id, name );
		tagSets.add( tagSet );
		return tagSet;
	}

	public List< TagSet > getTagSets()
	{
		return Collections.unmodifiableList( tagSets );
	}

	public void remove( final TagSet tagSet )
	{
		tagSets.remove( tagSet );
	}

	public static class TagSet
	{
		private final List< Tag > tags;

		private final int id;

		private String name;

		private TagSet( final int id, final String name )
		{
			this.id = id;
			this.name = name;
			this.tags = new ArrayList<>();
		}

		private void read( final DataInputStream dis ) throws IOException
		{
			tags.clear();
			// N tags.
			final int nTags = dis.readInt();
			// Tags.
			for ( int i = 0; i < nTags; i++ )
			{
				// Tag id.
				final int tagId = dis.readInt();
				if ( tagId >= tagIDgenerator.get() )
					tagIDgenerator.set( tagId + 1 );
				// Tag label.
				final String tagLabel = dis.readUTF();
				// Tag color as int.
				final Color tagColor = new Color( dis.readInt(), true );
				final Tag tag = new Tag( tagId, tagLabel, tagColor );
				tags.add( tag );
			}
		}

		private void write( final DataOutputStream dos ) throws IOException
		{
			// N tags.
			dos.writeInt( tags.size() );
			// Tags.
			for ( final Tag tag : tags )
			{
				// Tag id.
				dos.writeInt( tag.id );
				// Tag label.
				dos.writeUTF( tag.label );
				// Tag color as int.
				dos.writeInt( tag.color.getRGB() );
			}
		}

		/**
		 * Returns a copy of this TagSet, with copied ids and identical name.
		 *
		 * @return a new TagSet.
		 */
		private TagSet copy()
		{
			return copy( null );
		}

		/**
		 * Returns a copy of this TagSet, with the specified name and copied
		 * ids.
		 *
		 * @return a new TagSet.
		 */
		private TagSet copy( final String name )
		{
			final TagSet copy = new TagSet( this.id, name == null ? this.name : name );
			for ( final Tag tag : tags )
				copy.tags.add( tag.copy() );
			return copy;
		}

		public int id()
		{
			return id;
		}

		public void setName( final String name )
		{
			this.name = name;
		}

		public String getName()
		{
			return name;
		}

		public Tag createTag( final String name, final Color color )
		{
			final int id = tagIDgenerator.getAndIncrement();
			final Tag tag = new Tag( id, name, color );
			tags.add( tag );
			return tag;
		}

		public boolean removeTag( final Tag tag )
		{
			return tags.remove( tag );
		}

		public List< Tag > getTags()
		{
			return Collections.unmodifiableList( tags );
		}
	}

	public static class Tag
	{
		private final int id;

		private String label;

		private Color color;

		private Tag( final int id, final String label, final Color color )
		{
			this.id = id;
			this.label = label;
			this.color = color;
		}

		/**
		 * Returns a copy of this tag, with identical id and fields.
		 *
		 * @return a new Tag.
		 */
		private Tag copy()
		{
			return new Tag( id, label, color );
		}

		public int id()
		{
			return id;
		}

		public void setColor( final Color color )
		{
			this.color = color;
		}

		public void setLabel( final String label )
		{
			this.label = label;
		}

		public Color color()
		{
			return color;
		}

		public String label()
		{
			return label;
		}
	}

	@Override
	public String toString()
	{
		final StringBuilder str = new StringBuilder( super.toString() );
		for ( final TagSet tagSet : tagSets )
		{
			str.append( "\n  - " + tagSet.getName() + ", id = #" + tagSet.id + ":" );
			for ( final Tag tag : tagSet.getTags() )
				str.append( "\n      #" + tag.id() + ", " + tag.label() + ", color = " + tag.color() );
		}
		return str.toString();
	}
}
