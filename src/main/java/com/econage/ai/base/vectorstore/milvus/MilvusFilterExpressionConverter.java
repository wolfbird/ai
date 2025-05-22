package com.econage.ai.base.vectorstore.milvus;

import com.econage.ai.base.vectorstore.filter.Filter;
import com.econage.ai.base.vectorstore.filter.converter.AbstractFilterExpressionConverter;

/**
 * @author hanpeng
 * @date 2025/2/18 11:01
 */
public class MilvusFilterExpressionConverter extends AbstractFilterExpressionConverter {

    @Override
    protected void doExpression(Filter.Expression exp, StringBuilder context) {
        if(exp.type() == Filter.ExpressionType.JSON_CONTAINS_ANY ) {
            context.append("JSON_CONTAINS_ANY(");
            this.convertOperand(exp.left(), context);
            context.append(", ");
            this.convertOperand(exp.right(), context);
            context.append(")");
            return;
        }

        this.convertOperand(exp.left(), context);
        context.append(getOperationSymbol(exp));
        this.convertOperand(exp.right(), context);
    }

    private String getOperationSymbol(Filter.Expression exp) {
        switch (exp.type()) {
            case AND:
                return " && ";
            case OR:
                return " || ";
            case EQ:
                return " == ";
            case NE:
                return " != ";
            case LT:
                return " < ";
            case LTE:
                return " <= ";
            case GT:
                return " > ";
            case GTE:
                return " >= ";
            case IN:
                return " in ";
            case NIN:
                return " nin ";
            default:
                throw new RuntimeException("Not supported expression type:" + exp.type());
        }
    }

    @Override
    protected void doGroup(Filter.Group group, StringBuilder context) {
        this.convertOperand(new Filter.Expression(Filter.ExpressionType.AND, group.content(), group.content()), context); // trick
    }

    @Override
    protected void doKey(Filter.Key key, StringBuilder context) {
        var identifier = (hasOuterQuotes(key.key())) ? removeOuterQuotes(key.key()) : key.key();
        context.append("metadata[\"" + identifier + "\"]");
    }

}
