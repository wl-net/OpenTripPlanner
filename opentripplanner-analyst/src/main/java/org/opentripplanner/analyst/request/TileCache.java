package org.opentripplanner.analyst.request;

import org.opentripplanner.analyst.core.RasterPopulation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.cache.Weigher;

@Component
public class TileCache extends CacheLoader<RasterPopulation, RasterPopulation> 
    implements Weigher<RasterPopulation, RasterPopulation>, RemovalListener<RasterPopulation, RasterPopulation> { 
    
    private static final Logger LOG = LoggerFactory.getLogger(TileCache.class);

    @Autowired
    private SampleFactory sampleFactory;
    
    private final LoadingCache<RasterPopulation, RasterPopulation> tileCache = CacheBuilder
            .newBuilder()
            .concurrencyLevel(4)
            .maximumWeight(200000) // weight as determined by weigher (1GB in kB)
            .weigher(this)
            //.maximumSize(200000) // size as in number of entries
            //.softValues()
            .removalListener(this)
            .build(this);

    /** 
     * Completes the abstract CacheLoader superclass by implementing cache miss behavior. 
     * We use tiles as their own keys, but add a sample generator and materialize the samples 
     * when there is a cache miss.
     */
    @Override
    public RasterPopulation load(RasterPopulation req) throws Exception {
        LOG.debug("tile cache miss; cache size is {}", this.tileCache.size());
        req.materializeSamples();
        return req;
    }

    /** Delegate to the tile LoadingCache */
    public RasterPopulation get(RasterPopulation req) throws Exception {
        return tileCache.get(req);
    }
    
    /* 
     * "Compressed oops represent managed pointers (in many but not all places in the JVM) as 
     * 32-bit values which must be scaled by a factor of 8 and added to a 64-bit base address 
     * to find the object they refer to."
     * https://wikis.oracle.com/display/HotSpotInternals/CompressedOops
     */
    static final int floatSize = 4, oopSize = 4; // bytes
    
    /** Roughly estimate the size of a tile in kilobytes */
    @Override
    public int weigh(RasterPopulation req, RasterPopulation tile) {
        final int sampleSize = floatSize * 2 + oopSize * 2;
        int nBytes = tile.totalSize() * sampleSize;
        int nkBytes = nBytes / 1000 + 1; 
        LOG.debug("weighed tile as {} kilobytes", nkBytes);
        return nkBytes;
    }

    @Override
    public void onRemoval(RemovalNotification<RasterPopulation, RasterPopulation> notification) {
        LOG.debug("removed tile: {}", notification);
    }
    
}
