package br.ufrn.ppgsc.backhoe.vo.wrapper;

public abstract class AbstractFileRevisionWrapper<T> extends AbstractWrapper<T> {
	
	public abstract String getPath();
	public abstract Long getRevision();
	
}
