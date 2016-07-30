package org.osgl.inject;

/**
 * A `GeniePlugin` can register it self to a genie instance
 */
public interface GeniePlugin {
    /**
     * Implementation shall register to a Genie instance
     * @param genie the Genie instance
     */
    void register(Genie genie);
}
