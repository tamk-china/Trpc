package com.tamk.Trpc.protocol;

import java.io.Serializable;

/**
 * @author kuanqiang.tkq
 */
public class InvokeTO implements Serializable {
	private static final long serialVersionUID = 5247538516017488366L;

	private String interfaceName;
	private String functionName;
	private Class<?>[] paramClasses;
	private Object[] params;

	public String getFunctionName() {
		return functionName;
	}

	public void setFunctionName(String functionName) {
		this.functionName = functionName;
	}

	public Class<?>[] getParamClasses() {
		return paramClasses;
	}

	public void setParamClasses(Class<?>[] paramClasses) {
		this.paramClasses = paramClasses;
	}

	public String getInterfaceName() {
		return interfaceName;
	}

	public void setInterfaceName(String interfaceName) {
		this.interfaceName = interfaceName;
	}

	public Object[] getParams() {
		return params;
	}

	public void setParams(Object[] params) {
		this.params = params;
	}

}
