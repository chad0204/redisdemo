package com.pc.cacheloader.model;



public enum QueryEntity {
    USER(UserEntity.class);

    Class aClass;

    public Class getaClass() {
        return aClass;
    }

    public void setaClass(Class aClass) {
        this.aClass = aClass;
    }

    QueryEntity(Class aClass) {
        this.aClass = aClass;
    }
}
