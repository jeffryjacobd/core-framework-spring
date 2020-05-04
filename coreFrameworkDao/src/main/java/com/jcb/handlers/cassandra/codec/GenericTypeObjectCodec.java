package com.jcb.handlers.cassandra.codec;

import com.datastax.oss.driver.api.core.ProtocolVersion;
import com.datastax.oss.driver.api.core.type.DataType;
import com.datastax.oss.driver.api.core.type.codec.TypeCodec;
import com.datastax.oss.driver.api.core.type.reflect.GenericType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.ByteBuffer;

import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "of")
public class GenericTypeObjectCodec<GenericObject> implements TypeCodec<GenericObject> {

    private Class<GenericObject> clazz;

    private DataType dataType;

    @Override
    public GenericType<GenericObject> getJavaType() {
	return GenericType.of(clazz);

    }

    @Override
    public DataType getCqlType() {
	return dataType;

    }

    @Override
    public ByteBuffer encode(GenericObject value, ProtocolVersion protocolVersion) {
	byte[] encodedBytes = null;
	try {
	    encodedBytes = new ObjectMapper().writeValueAsBytes(value);
	} catch (JsonProcessingException e) {
	    e.printStackTrace();
	    throw new RuntimeException(e);
	}
	return ByteBuffer.wrap(encodedBytes);
    }

    @Override
    public GenericObject decode(ByteBuffer bytes, ProtocolVersion protocolVersion) {
	GenericObject decodedObject = null;
	try {
	    decodedObject = new ObjectMapper().readValue(bytes.array(), clazz);
	} catch (IOException e) {
	    e.printStackTrace();
	    throw new RuntimeException(e);
	}
	return decodedObject;

    }

    @Override
    public String format(GenericObject value) {
	return null;
    }

    @Override
    public GenericObject parse(String value) {
	return null;
    }

}
