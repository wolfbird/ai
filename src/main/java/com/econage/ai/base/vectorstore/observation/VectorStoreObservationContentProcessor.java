/*
 * Copyright 2023-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.econage.ai.base.vectorstore.observation;

import org.springframework.ai.document.Document;
import com.econage.ai.base.vectorstore.observation.VectorStoreObservationContext;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * Utilities to process the query content in observations for vector store operations.
 *
 * @author Thomas Vitale
 */
public final class VectorStoreObservationContentProcessor {

	private VectorStoreObservationContentProcessor() {
	}

	public static List<String> documents(VectorStoreObservationContext context) {
		if (CollectionUtils.isEmpty(context.getQueryResponse())) {
			return List.of();
		}

		return context.getQueryResponse().stream().map(Document::getText).toList();
	}

}
