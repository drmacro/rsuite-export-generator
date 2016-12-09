package com.rsicms.exportgenerator.generation;

import com.rsicms.exportgenerator.api.MoType;

/**
 * Bean to represent managed objects
 */
public class ManagedObject {

    final int moid;
    final MoType moType;

    private final String displayName;

    public ManagedObject(int moid, MoType moType, String displayName) {
        this.moid = moid;
        this.moType = moType;
        this.displayName = displayName;
    }

    public int getID() {
        return this.moid;
    }

    public MoType getMoType() {
        return this.moType;
    }

    public String getDisplayName() {
        return displayName;
    }

}
