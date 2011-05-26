package com.project944.cov.layoutmanagers;

import java.io.IOException;
import java.util.List;

import com.project944.cov.CoverDetails;

public interface CoversLayoutManager {

	void layout(List<CoverDetails> covers);
	void save(List<CoverDetails> covers) throws IOException;
}
