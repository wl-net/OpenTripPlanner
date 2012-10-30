package org.opentripplanner.analyst.request;

import org.opentripplanner.analyst.core.Tile;
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
public class TileCache extends CacheLoader<Tile, Tile> 
    implements Weigher<Tile, Tile>, RemovalListener<Tile, Tile> { 
    
    private static final Logger LOG = LoggerFactory.getLogger(TileCache.class);

    @Autowired
    private SampleFactory sampleFactory;
    
    private final LoadingCache<Tile, Tile> tileCache = CacheBuilder
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
    public Tile load(Tile req) throws Exception {
        LOG.debug("tile cache miss; cache size is {}", this.tileCache.size());
        req.resampleDynamic(sampleFactory);
        req.materializeSamples();
        return req;
    }

    /** Delegate to the tile LoadingCache */
    public Tile get(Tile req) throws Exception {
        return tileCache.get(req);
    }
    
    /** Roughly estimate the size of a tile in kilobytes */
    @Override
    public int weigh(Tile req, Tile tile) {
        final int refSize = 6; // bytes
        final int sampleSize = 4 * 2 + refSize * 2;
        int nBytes = tile.totalSize() * sampleSize;
        int nkBytes = nBytes / 1000 + 1; 
        LOG.debug("weighed tile as {} kilobytes", nkBytes);
        return nkBytes;
    }

    @Override
    public void onRemoval(RemovalNotification<Tile, Tile> notification) {
        LOG.debug("removed tile: {}", notification);
    }
    
}
