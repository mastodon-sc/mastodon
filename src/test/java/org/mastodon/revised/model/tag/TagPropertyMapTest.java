package org.mastodon.revised.model.tag;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefList;
import org.mastodon.graph.revised.CreateLargeModelExample;
import org.mastodon.pool.PoolCollectionWrapper;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.Spot;

public class TagPropertyMapTest
{

	private PoolCollectionWrapper< Spot > objects;

	private TagPropertyMap< Spot, String > tags;

	@Before
	public void setUp() throws Exception
	{
		final Model model = new CreateLargeModelExample().run( 2, 4, 4 );
		objects = model.getGraph().vertices();
		tags = new TagPropertyMap<>( objects );
	}

	@Test
	public void testSetOK()
	{
		// Test setting tag.
		final String t1 = "Tag1";
		final Spot o1 = objects.iterator().next();
		final String previous1 = tags.set( o1, t1 );
		assertNull( "Previous tag should be null.", previous1 );

		// Test known tags.
		final Set< String > tagSet = tags.getTags();
		assertEquals( "There should be 1 tag known.", 1, tagSet.size() );
		assertEquals( "Unexpected tag value.", t1, tagSet.iterator().next() );

		// Test collection for tag.
		final Collection< Spot > taggedWith1 = tags.getTaggedWith( t1 );
		assertNotNull( "Tagged collection should not be null.", taggedWith1 );
		assertFalse( "Tagged collection should not be empty.", taggedWith1.isEmpty() );
		assertEquals( "Tagged collection should contain exactly 1 element.", 1, taggedWith1.size() );
		assertEquals( "Tagged collection should contain expected element.", o1, taggedWith1.iterator().next() );

		// Test re-settting to a new tag.
		final String t2 = "Tag2";
		final String previous2 = tags.set( o1, t2 );
		assertEquals( "Previous tag is unexpected.", t1, previous2 );
		// Notice that we do not call the tagSet method again: tag set is in
		// sync, but unmodifiable.
		assertEquals( "There should be 1 tag known.", 1, tagSet.size() );
		assertEquals( "Unexpected tag value.", t2, tagSet.iterator().next() );

		// Test collection for tag.
		final Collection< Spot > taggedWith1bis = tags.getTaggedWith( t1 );
		assertNull( "Tagged collection with old tag should be null.", taggedWith1bis );

		final Collection< Spot > taggedWith2 = tags.getTaggedWith( t2 );
		assertNotNull( "Tagged collection should not be null.", taggedWith2 );
		assertFalse( "Tagged collection should not be empty.", taggedWith2.isEmpty() );
		assertEquals( "Tagged collection should contain exactly 1 element.", 1, taggedWith2.size() );
		assertEquals( "Tagged collection should contain expected element.", o1, taggedWith2.iterator().next() );
	}

	@Test
	public void testSetCollectionOfOK()
	{
		final String t1 = "Tag1";
		final Spot o1 = objects.iterator().next();
		tags.set( o1, t1 );

		final RefList< Spot > objs = RefCollections.createRefList( objects );
		final int nObjs = objects.size() / 10;
		int i = 0;
		final Iterator< Spot > it = objects.iterator();
		while ( i++ < nObjs )
			objs.add( it.next() );

		final String t2 = "Tag2";
		tags.set( objs, t2 );

		// Test collection for old tag.
		final Collection< Spot > taggedWith1bis = tags.getTaggedWith( t1 );
		assertNull( "Tagged collection with old tag should be null.", taggedWith1bis );

		// Test collection of new tag.
		final Collection< Spot > taggedWith2 = tags.getTaggedWith( t2 );
		assertNotNull( "Tagged collection should not be null.", taggedWith2 );
		assertFalse( "Tagged collection should not be empty.", taggedWith2.isEmpty() );
		assertEquals( "Tagged collection should contain exactly this number of element.", nObjs, taggedWith2.size() );
		assertTrue( "Tagged collection should contain expected elements.", taggedWith2.containsAll( objs ) );
		assertTrue( "All tagged element should be in the tagged collection.", objs.containsAll( taggedWith2 ) );
	}

	@Test
	public void testRemoveO()
	{
		final RefList< Spot > objs = RefCollections.createRefList( objects );
		final int nObjs = objects.size() / 10;
		int i = 0;
		final Iterator< Spot > it = objects.iterator();
		while ( i++ < nObjs )
			objs.add( it.next() );

		final String t2 = "Tag2";
		tags.set( objs, t2 );

		// Rmove tag of 1 object.
		final Spot o1 = objects.iterator().next();
		final String previous = tags.remove( o1 );
		assertEquals( "Previous tag is unexpected.", t2, previous );
		assertFalse( "Object should not be tagged.", tags.isSet( o1 ) );
		assertNull( "Current tag should by null.", tags.get( o1 ) );
		assertFalse( "Object with removed tag should not be in tag collection.", tags.getTaggedWith( t2 ).contains( o1 ) );
	}

	@Test
	public void testRemoveCollectionOfO()
	{
		final RefList< Spot > objs = RefCollections.createRefList( objects );
		final int nObjs = objects.size() / 10;
		int i = 0;
		final Iterator< Spot > it = objects.iterator();
		while ( i++ < nObjs )
			objs.add( it.next() );

		final String t2 = "Tag2";
		tags.set( objs, t2 );

		// Rmove tag of all object.
		tags.remove( objs );
		assertNull( "Tag collection should be null.", tags.getTaggedWith( t2 ) );
		for ( final Spot o : objs )
		{
			assertFalse( "Object should not be tagged.", tags.isSet( o ) );
			assertNull( "Current tag should by null.", tags.get( o ) );
		}
	}

	@Test
	public void testGetTaggedWith()
	{
		final String t1 = "Tag1";
		final RefList< Spot > objs = RefCollections.createRefList( objects );
		final int nObjs = objects.size() / 10;
		int i = 0;
		final Iterator< Spot > it = objects.iterator();
		while ( i++ < nObjs )
			objs.add( it.next() );

		for ( final Spot o : objs )
		{
			tags.set( o, t1 );
			assertTrue( "Tag set instance should contain newly tagged object.", tags.getTaggedWith( t1 ).contains( o ) );
		}
	}

	@Test
	public void testClearTag()
	{
		final String t1 = "Tag1";
		final RefList< Spot > objs = RefCollections.createRefList( objects );
		final int nObjs = objects.size() / 10;
		int i = 0;
		final Iterator< Spot > it = objects.iterator();
		while ( i++ < nObjs )
			objs.add( it.next() );

		// Tag a collection of objects.
		tags.set( objs, t1 );
		// Tag a single object separately.
		final String t2 = "Tag2";
		final Spot o1 = objects.iterator().next();
		tags.set( o1, t2 );
		// Clear first tag.
		tags.clearTag( t1 );

		assertNull( "Tagged collection with cleared tag should be null.", tags.getTaggedWith( t1 ) );
		assertEquals( "Unaffected object should have its tag.", t2, tags.get( o1 ) );
		for ( final Spot o : objs )
		{
			if ( o.equals( o1 ) )
				continue;
			assertFalse( "Objects with cleared tag should not be set.", tags.isSet( o ) );
			assertNull( "Objects with cleared tag should have a null tag.", tags.get( o ) );
		}
		assertFalse( "Cleared tag should not be part of the tag set anymore.", tags.getTags().contains( t1 ) );
	}

	@Test
	public void testSize()
	{
		final String t1 = "Tag1";
		final RefList< Spot > objs = RefCollections.createRefList( objects );
		final int nObjs = objects.size() / 10;
		int i = 0;
		final Iterator< Spot > it = objects.iterator();
		while ( i++ < nObjs )
			objs.add( it.next() );

		// Tag a collection of objects.
		tags.set( objs, t1 );
		assertEquals( "Unexpected size for tag map.", nObjs, tags.size() );

		// Tag a single object separately.
		final Spot o1 = objects.iterator().next();
		tags.remove( o1 );
		assertEquals( "Unexpected size for tag map.", nObjs - 1, tags.size() );

		final String t2 = "Tag2";
		tags.set( o1, t2 );
		assertEquals( "Unexpected size for tag map.", nObjs, tags.size() );
	}

	@Test
	public void testClear()
	{
		final String t1 = "Tag1";
		final RefList< Spot > objs = RefCollections.createRefList( objects );
		final int nObjs = objects.size() / 10;
		int i = 0;
		final Iterator< Spot > it = objects.iterator();
		while ( i++ < nObjs )
			objs.add( it.next() );

		// Tag a collection of objects.
		tags.set( objs, t1 );
		// Tag a single object separately.
		final String t2 = "Tag2";
		final Spot o1 = objects.iterator().next();
		tags.set( o1, t2 );

		// Clear all.
		tags.clear();
		assertTrue( "Tag map should be empty.", tags.size() == 0 );
		assertTrue( "Tag set should be empty.", tags.getTags().isEmpty() );
		for ( final Spot o : objects )
		{
			assertFalse( "No object should not set.", tags.isSet( o ) );
			assertNull( "The tag of all objects should be null.", tags.get( o ) );
		}
	}

}
