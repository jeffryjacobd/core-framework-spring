package com.jcb.entity;

import java.io.Serializable;

import lombok.Data;

@Data
public class SessionDataModel implements Serializable {

	private static final long serialVersionUID = -5042409338071112771L;

	private String key = "";

	private String route = "";

}
