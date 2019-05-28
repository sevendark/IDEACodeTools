package com.sevendark.ai.plugin.lib.sql.parser;

import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.*;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.comment.Comment;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.view.AlterView;
import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.execute.Execute;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.merge.Merge;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.upsert.Upsert;
import net.sf.jsqlparser.statement.values.ValuesStatement;

import java.util.HashMap;
import java.util.Map;

public class SqlParserVisitor implements StatementVisitor, ExpressionVisitor, SelectVisitor, SelectItemVisitor,
        FromItemVisitor, GroupByVisitor, IntoTableVisitor, ItemsListVisitor, OrderByVisitor, PivotVisitor {

    private StringBuilder sb;
    private Map<String, String> aliasTableMap;

    private SqlParserVisitor() {
    }

    private SqlParserVisitor(StringBuilder sb) {
        this.sb = sb;
        this.aliasTableMap = new HashMap<>();
    }

    public static SqlParserVisitor of() {
        return new SqlParserVisitor(new StringBuilder("dslContext"));
    }

    public static SqlParserVisitor of(String dslName) {
        return new SqlParserVisitor(new StringBuilder(dslName));
    }

    public String parse(String sql) {
        try {
            Statement statement = CCJSqlParserUtil.parse(filterBacktick(sql));
            SqlParserVisitor visitor = SqlParserVisitor.of();
            statement.accept(visitor);
            return visitor.toString();
        } catch (Exception e) {
            return null;
        }
    }

    private static String filterBacktick(String sql) {
        return sql.replace("`", "");
    }

    @Override
    public String toString() {
        StringBuilder ss = new StringBuilder();
        if (!aliasTableMap.isEmpty()) {
            for (Map.Entry<String, String> entry : aliasTableMap.entrySet()) {
                String tableName = entry.getValue();
                String alias = entry.getKey();
                ss.append(tableName.toUpperCase().charAt(0))
                        .append(tableName.toLowerCase().substring(1))
                        .append(" ").append(alias).append(" = ")
                        .append(tableName.toUpperCase())
                        .append(".as(\"").append(alias).append("\");\n");
            }
        }
        ss.append(sb.toString().replace("(,", "("));
        ss.append(";");
        return ss.toString();
    }

    @Override
    public void visit(BitwiseRightShift aThis) {
        System.out.println(aThis.getClass().getName() + "    " + aThis.toString());
    }

    @Override
    public void visit(BitwiseLeftShift aThis) {
        System.out.println(aThis.getClass().getName() + "    " + aThis.toString());
    }

    @Override
    public void visit(NullValue nullValue) {
        sb.append("null");
    }

    @Override
    public void visit(Function function) {
        sb.append("DSL.").append(function.getName().toLowerCase()).append("(");
        function.getParameters().accept(this);
        sb.append(")");
    }

    @Override
    public void visit(SignedExpression signedExpression) {
        System.out.println(signedExpression.getClass().getName() + "    " + signedExpression.toString());
    }

    @Override
    public void visit(JdbcParameter jdbcParameter) {
        System.out.println(jdbcParameter.getClass().getName() + "    " + jdbcParameter.toString());
    }

    @Override
    public void visit(JdbcNamedParameter jdbcNamedParameter) {
        System.out.println(jdbcNamedParameter.getClass().getName() + "    " + jdbcNamedParameter.toString());
    }

    @Override
    public void visit(DoubleValue doubleValue) {
        sb.append(doubleValue.getValue());
    }

    @Override
    public void visit(LongValue longValue) {
        sb.append(longValue.getValue()).append("L");
    }

    @Override
    public void visit(HexValue hexValue) {
        System.out.println(hexValue.getClass().getName() + "    " + hexValue.toString());
    }

    @Override
    public void visit(DateValue dateValue) {
        System.out.println(dateValue.getClass().getName() + "    " + dateValue.toString());
    }

    @Override
    public void visit(TimeValue timeValue) {
        System.out.println(timeValue.getClass().getName() + "    " + timeValue.toString());
    }

    @Override
    public void visit(TimestampValue timestampValue) {
        System.out.println(timestampValue.getClass().getName() + "    " + timestampValue.toString());
    }

    @Override
    public void visit(Parenthesis parenthesis) {
        parenthesis.getExpression().accept(this);
    }

    @Override
    public void visit(StringValue stringValue) {
        sb.append("\"").append(stringValue.getValue()).append("\"");
    }

    @Override
    public void visit(Addition addition) {
        System.out.println(addition.getClass().getName() + "    " + addition.toString());
    }

    @Override
    public void visit(Division division) {
        System.out.println(division.getClass().getName() + "    " + division.toString());
    }

    @Override
    public void visit(Multiplication multiplication) {
        System.out.println(multiplication.getClass().getName() + "    " + multiplication.toString());
    }

    @Override
    public void visit(Subtraction subtraction) {
        System.out.println(subtraction.getClass().getName() + "    " + subtraction.toString());
    }

    @Override
    public void visit(AndExpression andExpression) {
        andExpression.getLeftExpression().accept(this);
        sb.append("\n.and(");
        andExpression.getRightExpression().accept(this);
        sb.append(")");
    }

    @Override
    public void visit(OrExpression orExpression) {
        orExpression.getLeftExpression().accept(this);
        sb.append("\n.or(");
        orExpression.getRightExpression().accept(this);
        sb.append(")");
    }

    @Override
    public void visit(Between between) {
        System.out.println(between.getClass().getName() + "    " + between.toString());
    }

    @Override
    public void visit(EqualsTo equalsTo) {
        equalsTo.getLeftExpression().accept(this);
        sb.append(".eq(");
        equalsTo.getRightExpression().accept(this);
        sb.append(")");
    }

    @Override
    public void visit(GreaterThan greaterThan) {
        greaterThan.getLeftExpression().accept(this);
        sb.append(".gt(");
        greaterThan.getRightExpression().accept(this);
        sb.append(")");
    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {
        greaterThanEquals.getLeftExpression().accept(this);
        sb.append(".ge(");
        greaterThanEquals.getRightExpression().accept(this);
        sb.append(")");
    }

    @Override
    public void visit(InExpression inExpression) {
        inExpression.getLeftExpression().accept(this);
        sb.append(".in(");
        inExpression.getRightItemsList().accept(this);
        sb.append(")");
    }

    @Override
    public void visit(IsNullExpression isNullExpression) {
        isNullExpression.getLeftExpression().accept(this);
        if (isNullExpression.isNot()) {
            sb.append(".isNotNull()");
        } else {
            sb.append(".isNull()");
        }
    }

    @Override
    public void visit(LikeExpression likeExpression) {
        System.out.println(likeExpression.getClass().getName() + "    " + likeExpression.toString());
    }

    @Override
    public void visit(MinorThan minorThan) {
        minorThan.getLeftExpression().accept(this);
        sb.append(".lt(");
        minorThan.getRightExpression().accept(this);
        sb.append(")");
    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        minorThanEquals.getLeftExpression().accept(this);
        sb.append(".le(");
        minorThanEquals.getRightExpression().accept(this);
        sb.append(")");
    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        notEqualsTo.getLeftExpression().accept(this);
        sb.append(".ne(");
        notEqualsTo.getRightExpression().accept(this);
        sb.append(")");
    }

    @Override
    public void visit(Column tableColumn) {
        tableColumn.getTable().accept((FromItemVisitor) this);
        sb.append(".").append(tableColumn.getColumnName().toUpperCase());
    }

    @Override
    public void visit(Table tableName) {
        String name = tableName.getName();
        if (aliasTableMap.containsKey(name)) {
            sb.append(name);
        } else if (tableName.getAlias() != null) {
            if (aliasTableMap.containsKey(tableName.getAlias().getName())) {
                sb.append(tableName.getAlias().getName());
            } else {
                sb.append(name.toUpperCase()).append(".as(\"").append(tableName.getAlias().getName()).append("\")");
            }
        } else {
            sb.append(name.toUpperCase());
        }
    }

    @Override
    public void visit(SubSelect subSelect) {
        System.out.println(subSelect.getClass().getName() + "    " + subSelect.toString());
    }

    @Override
    public void visit(SubJoin subjoin) {
        subjoin.getLeft().accept(this);
        if (subjoin.getJoinList() != null) {
            for (Join join : subjoin.getJoinList()) {
                sb.append("\n.");
                if (join.isInner()) {
                    sb.append("innerJoin(");
                } else if (join.isLeft()) {
                    sb.append("leftOuterJoin(");
                } else {
                    sb.append("join(");
                }
                join.getRightItem().accept(this);
                sb.append(")\n.on(");
                join.getOnExpression().accept(this);
                sb.append(")");
            }
        }
    }

    @Override
    public void visit(LateralSubSelect lateralSubSelect) {
        System.out.println(lateralSubSelect.getClass().getName() + "    " + lateralSubSelect.toString());
    }

    @Override
    public void visit(ValuesList valuesList) {
        System.out.println(valuesList.getClass().getName() + "    " + valuesList.toString());
    }

    @Override
    public void visit(TableFunction tableFunction) {
        System.out.println(tableFunction.getClass().getName() + "    " + tableFunction.toString());
    }

    @Override
    public void visit(ParenthesisFromItem aThis) {
        System.out.println(aThis.getClass().getName() + "    " + aThis.toString());
    }

    @Override
    public void visit(ExpressionList expressionList) {
        for (Expression expression : expressionList.getExpressions()) {
            sb.append(",");
            expression.accept(this);
        }
    }

    @Override
    public void visit(NamedExpressionList namedExpressionList) {
        System.out.println(namedExpressionList.getClass().getName() + "    " + namedExpressionList.toString());
    }

    @Override
    public void visit(MultiExpressionList multiExprList) {
        System.out.println(multiExprList.getClass().getName() + "    " + multiExprList.toString());
    }

    @Override
    public void visit(CaseExpression caseExpression) {
        sb.append("DSL.decode()");
        if (caseExpression.getSwitchExpression() != null) {
            caseExpression.getSwitchExpression().accept(this);
        }
        if (caseExpression.getWhenClauses() != null) {
            for (WhenClause whenClause : caseExpression.getWhenClauses()) {
                sb.append(".when(");
                whenClause.getWhenExpression().accept(this);
                sb.append(", ");
                whenClause.getThenExpression().accept(this);
                sb.append(")");
            }
        }
        if (caseExpression.getElseExpression() != null) {
            sb.append(".otherwise(");
            caseExpression.getElseExpression().accept(this);
            sb.append(")");
        }
    }

    @Override
    public void visit(WhenClause whenClause) {
        System.out.println(whenClause.getClass().getName() + "    " + whenClause.toString());
    }

    @Override
    public void visit(ExistsExpression existsExpression) {
        System.out.println(existsExpression.getClass().getName() + "    " + existsExpression.toString());
    }

    @Override
    public void visit(AllComparisonExpression allComparisonExpression) {
        System.out.println(allComparisonExpression.getClass().getName() + "    " + allComparisonExpression.toString());
    }

    @Override
    public void visit(AnyComparisonExpression anyComparisonExpression) {
        System.out.println(anyComparisonExpression.getClass().getName() + "    " + anyComparisonExpression.toString());
    }

    @Override
    public void visit(Concat concat) {
        System.out.println(concat.getClass().getName() + "    " + concat.toString());
    }

    @Override
    public void visit(Matches matches) {
        System.out.println(matches.getClass().getName() + "    " + matches.toString());
    }

    @Override
    public void visit(BitwiseAnd bitwiseAnd) {
        System.out.println(bitwiseAnd.getClass().getName() + "    " + bitwiseAnd.toString());
    }

    @Override
    public void visit(BitwiseOr bitwiseOr) {
        System.out.println(bitwiseOr.getClass().getName() + "    " + bitwiseOr.toString());
    }

    @Override
    public void visit(BitwiseXor bitwiseXor) {
        System.out.println(bitwiseXor.getClass().getName() + "    " + bitwiseXor.toString());
    }

    @Override
    public void visit(CastExpression cast) {
        System.out.println(cast.getClass().getName() + "    " + cast.toString());
    }

    @Override
    public void visit(Modulo modulo) {
        System.out.println(modulo.getClass().getName() + "    " + modulo.toString());
    }

    @Override
    public void visit(AnalyticExpression aexpr) {
        System.out.println(aexpr.getClass().getName() + "    " + aexpr.toString());
    }

    @Override
    public void visit(ExtractExpression eexpr) {
        System.out.println(eexpr.getClass().getName() + "    " + eexpr.toString());
    }

    @Override
    public void visit(IntervalExpression iexpr) {
        System.out.println(iexpr.getClass().getName() + "    " + iexpr.toString());
    }

    @Override
    public void visit(OracleHierarchicalExpression oexpr) {
        System.out.println(oexpr.getClass().getName() + "    " + oexpr.toString());
    }

    @Override
    public void visit(RegExpMatchOperator rexpr) {
        System.out.println(rexpr.getClass().getName() + "    " + rexpr.toString());
    }

    @Override
    public void visit(JsonExpression jsonExpr) {
        System.out.println(jsonExpr.getClass().getName() + "    " + jsonExpr.toString());
    }

    @Override
    public void visit(JsonOperator jsonExpr) {
        System.out.println(jsonExpr.getClass().getName() + "    " + jsonExpr.toString());
    }

    @Override
    public void visit(RegExpMySQLOperator regExpMySQLOperator) {
        System.out.println(regExpMySQLOperator.getClass().getName() + "    " + regExpMySQLOperator.toString());
    }

    @Override
    public void visit(UserVariable var) {
        System.out.println(var.getClass().getName() + "    " + var.toString());
    }

    @Override
    public void visit(NumericBind bind) {
        System.out.println(bind.getClass().getName() + "    " + bind.toString());
    }

    @Override
    public void visit(KeepExpression aexpr) {
        System.out.println(aexpr.getClass().getName() + "    " + aexpr.toString());
    }

    @Override
    public void visit(MySQLGroupConcat groupConcat) {
        System.out.println(groupConcat.getClass().getName() + "    " + groupConcat.toString());
    }

    @Override
    public void visit(ValueListExpression valueList) {
        System.out.println(valueList.getClass().getName() + "    " + valueList.toString());
    }

    @Override
    public void visit(RowConstructor rowConstructor) {
        System.out.println(rowConstructor.getClass().getName() + "    " + rowConstructor.toString());
    }

    @Override
    public void visit(OracleHint hint) {
        System.out.println(hint.getClass().getName() + "    " + hint.toString());
    }

    @Override
    public void visit(TimeKeyExpression timeKeyExpression) {
        System.out.println(timeKeyExpression.getClass().getName() + "    " + timeKeyExpression.toString());
    }

    @Override
    public void visit(DateTimeLiteralExpression literal) {
        System.out.println(literal.getClass().getName() + "    " + literal.toString());
    }

    @Override
    public void visit(NotExpression aThis) {
        System.out.println(aThis.getClass().getName() + "    " + aThis.toString());
    }

    @Override
    public void visit(NextValExpression aThis) {
        System.out.println(aThis.getClass().getName() + "    " + aThis.toString());
    }

    @Override
    public void visit(CollateExpression aThis) {
        System.out.println(aThis.getClass().getName() + "    " + aThis.toString());
    }

    @Override
    public void visit(Comment comment) {
        System.out.println(comment.getClass().getName() + "    " + comment.toString());
    }

    @Override
    public void visit(Commit commit) {
        System.out.println(commit.getClass().getName() + "    " + commit.toString());
    }

    @Override
    public void visit(Delete delete) {
        System.out.println(delete.getClass().getName() + "    " + delete.toString());
    }

    @Override
    public void visit(Update update) {
        System.out.println(update.getClass().getName() + "    " + update.toString());
    }

    @Override
    public void visit(Insert insert) {
        System.out.println(insert.getClass().getName() + "    " + insert.toString());
    }

    @Override
    public void visit(Replace replace) {
        System.out.println(replace.getClass().getName() + "    " + replace.toString());
    }

    @Override
    public void visit(Drop drop) {
        System.out.println(drop.getClass().getName() + "    " + drop.toString());
    }

    @Override
    public void visit(Truncate truncate) {
        System.out.println(truncate.getClass().getName() + "    " + truncate.toString());
    }

    @Override
    public void visit(CreateIndex createIndex) {
        System.out.println(createIndex.getClass().getName() + "    " + createIndex.toString());
    }

    @Override
    public void visit(CreateTable createTable) {
        System.out.println(createTable.getClass().getName() + "    " + createTable.toString());
    }

    @Override
    public void visit(CreateView createView) {
        System.out.println(createView.getClass().getName() + "    " + createView.toString());
    }

    @Override
    public void visit(AlterView alterView) {
        System.out.println(alterView.getClass().getName() + "    " + alterView.toString());
    }

    @Override
    public void visit(Alter alter) {
        System.out.println(alter.getClass().getName() + "    " + alter.toString());
    }

    @Override
    public void visit(Statements stmts) {
        System.out.println(stmts.getClass().getName() + "    " + stmts.toString());
    }

    @Override
    public void visit(Execute execute) {
        System.out.println(execute.getClass().getName() + "    " + execute.toString());
    }

    @Override
    public void visit(SetStatement set) {
        System.out.println(set.getClass().getName() + "    " + set.toString());
    }

    @Override
    public void visit(ShowStatement set) {
        System.out.println(set.getClass().getName() + "    " + set.toString());
    }

    @Override
    public void visit(Merge merge) {
        System.out.println(merge.getClass().getName() + "    " + merge.toString());
    }

    @Override
    public void visit(Select select) {
        if (select.getSelectBody() != null) {
            select.getSelectBody().accept(this);
        }
    }

    @Override
    public void visit(Upsert upsert) {
        System.out.println(upsert.getClass().getName() + "    " + upsert.toString());
    }

    @Override
    public void visit(UseStatement use) {
        System.out.println(use.getClass().getName() + "    " + use.toString());
    }

    @Override
    public void visit(Block block) {
        System.out.println(block.getClass().getName() + "    " + block.toString());
    }

    @Override
    public void visit(PlainSelect plainSelect) {
        if (plainSelect.getFromItem() != null) {
            AliasTableNameFinder aliasTableNameFinder = new AliasTableNameFinder();
            aliasTableMap.putAll(aliasTableNameFinder.getTableMap(plainSelect.getFromItem()));
        }
        sb.append("\n.select(");
        if (plainSelect.getSelectItems() != null) {
            for (SelectItem selectItem : plainSelect.getSelectItems()) {
                selectItem.accept(this);
            }
        }
        sb.append(")");
        if (plainSelect.getFromItem() != null) {
            sb.append("\n.from(");
            plainSelect.getFromItem().accept(this);
            sb.append(")");
        }
        if (plainSelect.getWhere() != null) {
            sb.append("\n.where(");
            plainSelect.getWhere().accept(this);
            sb.append(")");
        }
        if (plainSelect.getGroupBy() != null) {
            plainSelect.getGroupBy().accept(this);
        }
        if (plainSelect.getHaving() != null) {
            plainSelect.getHaving().accept(this);
        }
        if (plainSelect.getOrderByElements() != null) {
            for (OrderByElement orderByElement : plainSelect.getOrderByElements()) {
                orderByElement.accept(this);
            }
        }
        if (plainSelect.getOffset() != null) {
            sb.append("\n.offset(").append(plainSelect.getOffset().getOffset()).append("L)");
        }
        if (plainSelect.getLimit() != null) {
            // TODO how to?
        }
    }

    @Override
    public void visit(SetOperationList setOpList) {
        System.out.println(setOpList.getClass().getName() + "    " + setOpList.toString());
    }

    @Override
    public void visit(WithItem withItem) {
        System.out.println(withItem.getClass().getName() + "    " + withItem.toString());
    }

    @Override
    public void visit(ValuesStatement values) {
        System.out.println(values.getClass().getName() + "    " + values.toString());
    }

    @Override
    public void visit(DescribeStatement describe) {
        System.out.println(describe.getClass().getName() + "    " + describe.toString());
    }

    @Override
    public void visit(ExplainStatement aThis) {
        System.out.println(aThis.getClass().getName() + "    " + aThis.toString());
    }

    @Override
    public void visit(GroupByElement groupBy) {
        System.out.println(groupBy.getClass().getName() + "    " + groupBy.toString());
    }

    @Override
    public void visit(OrderByElement orderBy) {
        System.out.println(orderBy.getClass().getName() + "    " + orderBy.toString());
    }

    @Override
    public void visit(Pivot pivot) {
        System.out.println(pivot.getClass().getName() + "    " + pivot.toString());
    }

    @Override
    public void visit(PivotXml pivot) {
        System.out.println(pivot.getClass().getName() + "    " + pivot.toString());
    }

    @Override
    public void visit(AllColumns allColumns) {
        // do nothing
    }

    @Override
    public void visit(AllTableColumns allTableColumns) {
        sb.append(",");
        allTableColumns.getTable().accept((FromItemVisitor) this);
    }

    @Override
    public void visit(SelectExpressionItem selectExpressionItem) {
        sb.append(",");
        selectExpressionItem.getExpression().accept(this);
        if (selectExpressionItem.getAlias() != null) {
            sb.append(".as(\"").append(selectExpressionItem.getAlias().getName()).append("\")");
        }
    }
}
