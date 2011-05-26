package com.project944.cov.sources;

import java.util.List;

import com.project944.cov.CoverDetails;
import com.project944.cov.utils.MyLogger;

public interface CoverSource {

	List<CoverDetails> getCovers(MyLogger logger);

    List<CoverDetails> refreshFromServer(List<CoverDetails> prevCovers, boolean forceRefreshAttempt, MyLogger logger);
}
