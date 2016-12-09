package com.rsicms.exportgenerator.mogeneration;

import com.rsicms.exportgenerator.api.MoType;

/**
 * Bean to represent managed objects
 */
public class ManagedObject {

    final int moid;
    final MoType moType;

    public ManagedObject(int moid, MoType moType) {
        this.moid = moid;
        this.moType = moType;
    }

    public int getID() {
        return this.moid;
    }

    public MoType getMoType() {
        return this.moType;
    }

}
