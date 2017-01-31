package com.pass.prebug.hibernate.dao;

import com.pass.prebug.hibernate.database.MetaInfos;

public class MetaInfoDao extends GenericDaoJpa<MetaInfos>{
	
	public MetaInfos findLastMetaInfoEntry(String lastCommitDate) {
		return findLastEntry(MetaInfos.class,lastCommitDate);
	}

}
