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
package org.mastodon.model.tag;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
		try (ObjectOutputStream oos = new ObjectOutputStream(
				new BufferedOutputStream(
						new FileOutputStream( file ),
						1024 * 1024 ) ))
		{
			saveRaw( oos );
		}
	}

	public void saveRaw( final ObjectOutputStream oos ) throws IOException
	{
		// N tagsets.
		oos.writeInt( tagSets.size() );
		// Individual tagsets.
		for ( final TagSet tagSet : tagSets )
		{
			// TagSet id.
			oos.writeInt( tagSet.id );
			// TagSet name.
			oos.writeUTF( tagSet.name );
			// Tags.
			tagSet.write( oos );
		}
	}

	public void loadRaw( final File file ) throws IOException
	{
		try (final ObjectInputStream ois = new ObjectInputStream(
				new BufferedInputStream(
						new FileInputStream( file ),
						1024 * 1024 ) ))
		{
			loadRaw( ois );
		}
	}

	public void loadRaw( final ObjectInputStream ois ) throws IOException
	{
		tagSets.clear();

		// N tagsets.
		final int nTagSets = ois.readInt();
		for ( int i = 0; i < nTagSets; i++ )
		{
			// TagSet id.
			final int tagSetId = ois.readInt();
			if ( tagSetId >= tagSetIDgenerator.get() )
				tagSetIDgenerator.set( tagSetId + 1 );
			// TagSet name.
			final String tagSetName = ois.readUTF();
			final TagSet tagSet = new TagSet( tagSetId, tagSetName );
			// Tags.
			tagSet.read( ois );
			tagSets.add( tagSet );
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

		private void read( final ObjectInputStream dis ) throws IOException
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
				final int tagColor = dis.readInt();
				final Tag tag = new Tag( tagId, tagLabel, tagColor );
				tags.add( tag );
			}
		}

		private void write( final ObjectOutputStream dos ) throws IOException
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
				dos.writeInt( tag.color );
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

		public Tag createTag( final String name, final int color )
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

		@Override
		public boolean equals( final Object o )
		{
			if ( this == o )
				return true;
			if ( !( o instanceof TagSet ) )
				return false;
			final TagSet tagSet = ( TagSet ) o;
			return id == tagSet.id;
		}

		@Override
		public int hashCode()
		{
			return id;
		}
	}

	public static class Tag
	{
		private final int id;

		private String label;

		private int color;

		private Tag( final int id, final String label, final int color )
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

		public void setColor( final int color )
		{
			this.color = color;
		}

		public void setLabel( final String label )
		{
			this.label = label;
		}

		public int color()
		{
			return color;
		}

		public String label()
		{
			return label;
		}

		@Override
		public boolean equals( final Object o )
		{
			if ( this == o )
				return true;
			if ( !( o instanceof Tag ) )
				return false;
			final Tag tag = ( Tag ) o;
			return id == tag.id;
		}

		@Override
		public int hashCode()
		{
			return id;
		}

		@Override
		public String toString()
		{
			final StringBuilder str = new StringBuilder();
			str.append( "Tag{id=" ).append( id() );
			str.append( ", label='" ).append( label() ).append( "'" );
			str.append( ", color=" ).append( String.format( "0x%08X", color() ) );
			str.append( "}" );
			return str.toString();
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
				str.append( "\n      #" + tag.id() + ", " + tag.label() + ", color = "
						+ String.format( "0x%08X", tag.color() ) );
		}
		return str.toString();
	}
}
