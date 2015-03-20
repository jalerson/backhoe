package br.ufrn.ppgsc.backhoe.vo.wrapper.svn;

import org.tmatesoft.svn.core.io.SVNFileRevision;

import br.ufrn.ppgsc.backhoe.vo.wrapper.AbstractFileRevisionWrapper;

public class SVNFileRevisionWrapper extends AbstractFileRevisionWrapper<SVNFileRevision> {

	@Override
	public String getPath() {
		return super.wrapped.getPath();
	}

	@Override
	public Long getRevision() {
		return super.wrapped.getRevision();
	}

}
