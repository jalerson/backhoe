package br.ufrn.ppgsc.backhoe.vo.wrapper;

import br.ufrn.ppgsc.backhoe.vo.AbstractVO;

public abstract class AbstractWrapper<T> extends AbstractVO {
	protected T wrapped;
	
	public T getWrapped() {
		return wrapped;
	}
	
	public void setWrapped(T wrapped) {
		this.wrapped = wrapped;
	}
}
