package com.tamk.Trpc.test;

import com.baidu.bjf.remoting.protobuf.FieldType;
import com.baidu.bjf.remoting.protobuf.annotation.Protobuf;

public class SimpleTypeTest {

	@Protobuf(fieldType = FieldType.STRING, order = 1, required = true)
	private String name;

	@Protobuf(fieldType = FieldType.INT32, order = 2, required = false)
	private Integer value;

	public Integer getValue() {
		return value;
	}

	public void setValue(Integer value) {
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "SimpleTypeTest [name=" + name + ", value=" + value + "]";
	}

}