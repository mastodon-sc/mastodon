/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2026 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.mamut.io.loader.adapter;

import static org.mastodon.mamut.io.loader.util.mobie.OmeZarrMultiscales.MULTI_SCALE_KEY;

import org.janelia.saalfeldlab.n5.zarr.v3.ZarrV3KeyValueReader;

/**
 * Adapter for OME-NGFF containers backed by Zarr v3, where the metadata is
 * nested under an {@code ome} key in the group attributes. This is the layout
 * introduced by OME-NGFF version 0.5.
 */
public class ZarrV3KeyValueReaderToViewerImgLoaderAdapter extends AbstractOmeZarrToViewerImgLoaderAdapter< ZarrV3KeyValueReader >
{

    /**
     * Key under which OME-NGFF 0.5 nests its metadata in the group attributes.
     */
    private static final String OME_KEY = "ome";

    public ZarrV3KeyValueReaderToViewerImgLoaderAdapter( final ZarrV3KeyValueReader n5, final String dataset )
    {
        super( n5, dataset, OME_KEY + "/" + MULTI_SCALE_KEY );
    }
}
