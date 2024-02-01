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
package org.mastodon.mamut;

import java.lang.ref.WeakReference;

import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.util.GarbageCollectionUtils;
import org.scijava.Context;

/**
 * This program is particularly useful to debug a memory leak in Mastodon.
 * It opens and closes Mastodon. And afterwards waits for the ModelGraph
 * to be garbage collected.
 * <p>
 * The program will continue indefinitely if there is a memory leak that
 * prevents the ModelGraph from being garbage collected.
 * The Eclipse Memory Analyzer could be used to debug the memory leak.
 * Simply take a heap dump of this program and open it with the Eclipse
 * Memory Analyzer to see what is preventing the ModelGraph from being
 * garbage collected.
 */
public class GarbageCollectionDemo
{

	public static void main(String... args) throws InterruptedException
	{
		try ( Context context = new Context() ) {
			WeakReference< ModelGraph > modelGraph = GarbageCollectionTest.openAndCloseMastodon( context );
			while( modelGraph.get() != null )
			{
				System.out.println( "Waiting for garbage collection to clean up Mastodon. ModelGraph is still in memory." );
				GarbageCollectionUtils.triggerGarbageCollection();
				Thread.sleep( 1000 );
			}
			System.out.println( "ModelGraph was garbage collected." );
		}
	}
}
