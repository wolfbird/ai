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

package com.econage.ai.base.vectorstore.filter.converter;

import com.econage.ai.base.vectorstore.filter.Filter.Expression;
import com.econage.ai.base.vectorstore.filter.Filter.Group;
import com.econage.ai.base.vectorstore.filter.Filter.Key;
import com.econage.ai.base.vectorstore.filter.converter.AbstractFilterExpressionConverter;

/**
 * Converts {@link Expression} into test string format.
 *
 * @author Christian Tzolov
 */
public class PrintFilterExpressionConverter extends AbstractFilterExpressionConverter {

	public void doExpression(Expression expression, StringBuilder context) {
		this.convertOperand(expression.left(), context);
		context.append(" " + expression.type() + " ");
		this.convertOperand(expression.right(), context);

	}

	public void doKey(Key key, StringBuilder context) {
		context.append(key.key());
	}

	@Override
	public void doStartGroup(Group group, StringBuilder context) {
		context.append("(");
	}

	@Override
	public void doEndGroup(Group group, StringBuilder context) {
		context.append(")");
	}

}
