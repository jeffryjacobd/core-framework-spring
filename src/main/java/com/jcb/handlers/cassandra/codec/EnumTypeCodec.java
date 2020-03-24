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
public class EnumTypeCodec<EnumClass> implements TypeCodec<EnumClass> {

    private Class<EnumClass> clazz;

    private DataType dataType;

    @Override
    public GenericType<EnumClass> getJavaType() {
	return GenericType.of(clazz);

    }

    @Override
    public DataType getCqlType() {
	return dataType;

    }

    @Override
    public ByteBuffer encode(EnumClass value, ProtocolVersion protocolVersion) {
	byte[] encodedBytes = null;
	try {
	    encodedBytes = new ObjectMapper().writeValueAsString(value).getBytes();
	} catch (JsonProcessingException e) {
	    e.printStackTrace();
	    throw new RuntimeException(e);
	}
	return ByteBuffer.wrap(encodedBytes);
    }

    @Override
    public EnumClass decode(ByteBuffer bytes, ProtocolVersion protocolVersion) {
	EnumClass decodedObject = null;
	try {
	    decodedObject = new ObjectMapper().readValue(bytes.array(), clazz);
	} catch (IOException e) {
	    e.printStackTrace();
	    throw new RuntimeException(e);
	}
	return decodedObject;
    }

    @Override
    public String format(EnumClass value) {
	// TODO Auto-generated method stub
	return null;

    }

    @Override
    public EnumClass parse(String value) {
	// TODO Auto-generated method stub
	return null;

    }

}
