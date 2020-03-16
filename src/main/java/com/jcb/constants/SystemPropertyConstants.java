package com.jcb.constants;

public class SystemPropertyConstants {

    public static final String CASSANDRA_POINTS_PROPERTY = "com.jcb.cassandraPoints";

    public static final String REDIS_POINT_PROPERTY = "com.jcb.RedisPoint";

    public static final Boolean CREATE_KEYSPACE = (System.getProperty("com.jcb.CreateKeyspace") == null)
	    || Boolean.getBoolean("com.jcb.CreateKeyspace");

    public static final Boolean CREATE_TABLE = (System.getProperty("com.jcb.CreateTable") == null)
	    || Boolean.getBoolean("com.jcb.CreateTable");

    public static final Boolean CREATE_COLUMN = (System.getProperty("com.jcb.CreateColumn") == null)
	    || Boolean.getBoolean("com.jcb.CreateColumn");

    public static final Boolean ALTER_COLUMN = Boolean.getBoolean("com.jcb.AlterColumn");

    public static final Boolean DELETE_COLUMN = Boolean.getBoolean("com.jcb.DeleteColumn");

    public static final Boolean DELETE_TABLE = Boolean.getBoolean("com.jcb.DeleteTable");

    public static final Boolean DELETE_KEYSPACE = Boolean.getBoolean("com.jcb.DeleteKeyspace");

}
