/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2023 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.ModelUtils;
import org.mastodon.mamut.model.Spot;

public class ModelUtilsTest
{
	@Test
	public void testDump()
	{
		Model model = new Model();
		ModelGraph graph = model.getGraph();
		Spot a = graph.addVertex().init( 0, new double[] { 1, 2, 3 }, 1 );
		Spot b = graph.addVertex().init( 1, new double[] { 1, 2, 3.2 }, 1 );
		a.setLabel( "A" );
		b.setLabel( "B" );
		graph.addEdge( a, b ).init();
		String actual = ModelUtils.dump( model );
		String expexted = "Model -\n"
				+ "Spots:\n"
				+ "       Id      Label   Frame          X          Y          Z    N incoming links    N outgoing links    Spot N links    Spot frame          X          Y          Z    Spot radius\n"
				+ "                                (pixel)    (pixel)    (pixel)                                                                          (pixel)    (pixel)    (pixel)        (pixel)\n"
				+ "-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------\n"
				+ "        0          A       0        1.0        2.0        3.0                   0                   1               1             0        1.0        2.0        3.0            1.0\n"
				+ "        1          B       1        1.0        2.0        3.2                   1                   0               1             1        1.0        2.0        3.2            1.0\n"
				+ "Links:\n"
				+ "       Id  Source Id  Target Id    Link delta T    Link displacement    Source spot id    Target spot id    Link velocity\n"
				+ "                                                             (pixel)                                        (pixel/frame)\n"
				+ "-------------------------------------------------------------------------------------------------------------------------\n"
				+ "        0          0          1             1.0                  0.2               0.0               1.0              0.2\n";
		assertEquals( expexted, actual.replaceFirst( "^Model.*", "Model -" ) );
	}
}
