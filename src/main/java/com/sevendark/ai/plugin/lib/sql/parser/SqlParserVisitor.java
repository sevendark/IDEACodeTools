package com.sevendark.ai.plugin.lib.sql.parser;

import net.sf.jsqlparser.JSQLParserException;
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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SqlParserVisitor implements StatementVisitor, ExpressionVisitor, SelectVisitor, SelectItemVisitor,
        FromItemVisitor, GroupByVisitor, IntoTableVisitor, ItemsListVisitor, OrderByVisitor, PivotVisitor {

    private StringBuilder sb;
    private String onlyTable;
    private Map<String, String> aliasTableMap;
    private List<String> todoInfo;

    private SqlParserVisitor() {
    }

    private SqlParserVisitor(StringBuilder sb) {
        this.sb = sb;
        this.aliasTableMap = new HashMap<>();
        this.todoInfo = new ArrayList<>();
    }

    public static SqlParserVisitor instance() {
        return new SqlParserVisitor(new StringBuilder("dslContext"));
    }

    public static SqlParserVisitor of(String dslName) {
        return new SqlParserVisitor(new StringBuilder(dslName));
    }

    public static String parse(String sql) {
        try {
            Statement statement = CCJSqlParserUtil.parse(filterBacktick(sql));
            SqlParserVisitor visitor = SqlParserVisitor.instance();
            statement.accept(visitor);
            return visitor.toString();
        } catch (JSQLParserException e) {
            return "// Please Check your sql is correct.\n// tips: not support 'is true' at now";
        } catch (Exception e) {
            return "// Can not convert, cause:" + e.getMessage();
        }
    }

    private static String filterBacktick(String sql) {
        return sql.replace("`", "");
    }

    @Override
    public String toString() {
        StringBuilder ss = new StringBuilder();
        if (!todoInfo.isEmpty()) {
            for (String todoStr : todoInfo) {
                ss.append("// TODO ").append(todoStr).append(";\n");
            }
        }
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
            ss.append("\n");
        }
        ss.append(sb.toString().replaceAll(",\\s*\\)", ")"));
        ss.append(";\n// TODO may need to add fetch()/execute()/... at the end of the code,\n// and don't forget to format it;\n");
        return ss.toString();
    }

    @Override
    public void visit(BitwiseRightShift aThis) {
        todoInfo.add("Not implemented:    " + aThis.getClass().getName() + "    " + aThis.toString());
    }

    @Override
    public void visit(BitwiseLeftShift aThis) {
        todoInfo.add("Not implemented:    " + aThis.getClass().getName() + "    " + aThis.toString());
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
        todoInfo.add("Not implemented:    " + signedExpression.getClass().getName() + "    " + signedExpression.toString());
    }

    @Override
    public void visit(JdbcParameter jdbcParameter) {
        todoInfo.add("Not implemented:    " + jdbcParameter.getClass().getName() + "    " + jdbcParameter.toString());
    }

    @Override
    public void visit(JdbcNamedParameter jdbcNamedParameter) {
        todoInfo.add("Not implemented:    " + jdbcNamedParameter.getClass().getName() + "    " + jdbcNamedParameter.toString());
    }

    @Override
    public void visit(DoubleValue doubleValue) {
        sb.append(doubleValue.getValue());
    }

    @Override
    public void visit(LongValue longValue) {
        sb.append(longValue.getValue());
    }

    @Override
    public void visit(HexValue hexValue) {
        sb.append(hexValue.getValue());
    }

    @Override
    public void visit(DateValue dateValue) {
        todoInfo.add("Not implemented:    " + dateValue.getClass().getName() + "    " + dateValue.toString());
    }

    @Override
    public void visit(TimeValue timeValue) {
        todoInfo.add("Not implemented:    " + timeValue.getClass().getName() + "    " + timeValue.toString());
    }

    @Override
    public void visit(TimestampValue timestampValue) {
        todoInfo.add("Not implemented:    " + timestampValue.getClass().getName() + "    " + timestampValue.toString());
    }

    @Override
    public void visit(Parenthesis parenthesis) {
        Expression expression = parenthesis.getExpression();
        if (expression instanceof Parenthesis) {
            expression.accept(this);
        } else {
            sb.append("(");
            expression.accept(this);
            sb.append(")");
        }
    }

    @Override
    public void visit(StringValue stringValue) {
        sb.append("\"").append(stringValue.getValue()).append("\"");
    }

    @Override
    public void visit(Addition addition) {
        addition.getLeftExpression().accept(this);
        sb.append(".add(");
        addition.getRightExpression().accept(this);
        sb.append(")");
    }

    @Override
    public void visit(Division division) {
        division.getLeftExpression().accept(this);
        sb.append(".divide(");
        division.getRightExpression().accept(this);
        sb.append(")");
    }

    @Override
    public void visit(Multiplication multiplication) {
        multiplication.getLeftExpression().accept(this);
        sb.append(".multiply(");
        multiplication.getRightExpression().accept(this);
        sb.append(")");
    }

    @Override
    public void visit(Subtraction subtraction) {
        subtraction.getLeftExpression().accept(this);
        sb.append(".minus(");
        subtraction.getRightExpression().accept(this);
        sb.append(")");
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
        between.getLeftExpression().accept(this);
        sb.append(between.isNot() ? "\n.notBetween(" : "\n.between(");
        between.getBetweenExpressionStart().accept(this);
        between.getBetweenExpressionEnd().accept(this);
        sb.append(")");
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
        likeExpression.getLeftExpression().accept(this);
        sb.append(likeExpression.isCaseInsensitive() ? ".likeIgnoreCase(" : ".like(");
        likeExpression.getRightExpression().accept(this);
        sb.append(")");
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
        if (tableColumn.getTable() != null) {
            tableColumn.getTable().accept((FromItemVisitor) this);
        } else if (onlyTable != null) {
            sb.append(onlyTable);
        } else {
            sb.append(tableColumn.getColumnName().toUpperCase());
            return;
        }
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
        sb.append("DSL");
        subSelect.getSelectBody().accept(this);
        if (subSelect.getAlias() != null) {
            sb.append(".asTable(\"").append(subSelect.getAlias().getName()).append("\")");
        }
        todoInfo.add("sub-select alias not fully implemented.");
    }

    @Override
    public void visit(SubJoin subjoin) {
        subjoin.getLeft().accept(this);
        if (subjoin.getJoinList() != null) {
            for (Join join : subjoin.getJoinList()) {
                visit(join);
            }
        }
    }

    private void visit(Join join) {
        sb.append("\n.");
        if (join.isInner()) {
            sb.append("innerJoin(");
        } else if (join.isLeft()) {
            sb.append("leftOuterJoin(");
        } else if (join.isRight()) {
            sb.append("rightOuterJoin(");
        } else if (join.isCross()) {
            sb.append("crossJoin(");
        } else {
            sb.append("join(");
        }
        join.getRightItem().accept(this);
        sb.append(")\n.on(");
        join.getOnExpression().accept(this);
        sb.append(")");
    }

    @Override
    public void visit(LateralSubSelect lateralSubSelect) {
        todoInfo.add("Not implemented:    " + lateralSubSelect.getClass().getName() + "    " + lateralSubSelect.toString());
    }

    @Override
    public void visit(ValuesList valuesList) {
        todoInfo.add("Not implemented:    " + valuesList.getClass().getName() + "    " + valuesList.toString());
    }

    @Override
    public void visit(TableFunction tableFunction) {
        todoInfo.add("Not implemented:    " + tableFunction.getClass().getName() + "    " + tableFunction.toString());
    }

    @Override
    public void visit(ParenthesisFromItem aThis) {
        todoInfo.add("Not implemented:    " + aThis.getClass().getName() + "    " + aThis.toString());
    }

    @Override
    public void visit(ExpressionList expressionList) {
        for (Expression expression : expressionList.getExpressions()) {
            expression.accept(this);
            sb.append(", ");
        }
    }

    @Override
    public void visit(NamedExpressionList namedExpressionList) {
        todoInfo.add("Not implemented:    " + namedExpressionList.getClass().getName() + "    " + namedExpressionList.toString());
    }

    @Override
    public void visit(MultiExpressionList multiExprList) {
        if (multiExprList.getExprList() != null) {
            for (ExpressionList expressionList : multiExprList.getExprList()) {
                sb.append("\n.values(");
                expressionList.accept(this);
                sb.append(")");
            }
        }
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
        todoInfo.add("Not implemented:    " + whenClause.getClass().getName() + "    " + whenClause.toString());
    }

    @Override
    public void visit(ExistsExpression existsExpression) {
        todoInfo.add("Not implemented:    " + existsExpression.getClass().getName() + "    " + existsExpression.toString());
    }

    @Override
    public void visit(AllComparisonExpression allComparisonExpression) {
        todoInfo.add("Not implemented:    " + allComparisonExpression.getClass().getName() + "    " + allComparisonExpression.toString());
    }

    @Override
    public void visit(AnyComparisonExpression anyComparisonExpression) {
        todoInfo.add("Not implemented:    " + anyComparisonExpression.getClass().getName() + "    " + anyComparisonExpression.toString());
    }

    @Override
    public void visit(Concat concat) {
        todoInfo.add("Not implemented:    " + concat.getClass().getName() + "    " + concat.toString());
    }

    @Override
    public void visit(Matches matches) {
        todoInfo.add("Not implemented:    " + matches.getClass().getName() + "    " + matches.toString());
    }

    @Override
    public void visit(BitwiseAnd bitwiseAnd) {
        todoInfo.add("Not implemented:    " + bitwiseAnd.getClass().getName() + "    " + bitwiseAnd.toString());
    }

    @Override
    public void visit(BitwiseOr bitwiseOr) {
        todoInfo.add("Not implemented:    " + bitwiseOr.getClass().getName() + "    " + bitwiseOr.toString());
    }

    @Override
    public void visit(BitwiseXor bitwiseXor) {
        todoInfo.add("Not implemented:    " + bitwiseXor.getClass().getName() + "    " + bitwiseXor.toString());
    }

    @Override
    public void visit(CastExpression cast) {
        todoInfo.add("Not implemented:    " + cast.getClass().getName() + "    " + cast.toString());
    }

    @Override
    public void visit(Modulo modulo) {
        todoInfo.add("Not implemented:    " + modulo.getClass().getName() + "    " + modulo.toString());
    }

    @Override
    public void visit(AnalyticExpression aexpr) {
        todoInfo.add("Not implemented:    " + aexpr.getClass().getName() + "    " + aexpr.toString());
    }

    @Override
    public void visit(ExtractExpression eexpr) {
        todoInfo.add("Not implemented:    " + eexpr.getClass().getName() + "    " + eexpr.toString());
    }

    @Override
    public void visit(IntervalExpression iexpr) {
        todoInfo.add("Not implemented:    " + iexpr.getClass().getName() + "    " + iexpr.toString());
    }

    @Override
    public void visit(OracleHierarchicalExpression oexpr) {
        todoInfo.add("Not implemented:    " + oexpr.getClass().getName() + "    " + oexpr.toString());
    }

    @Override
    public void visit(RegExpMatchOperator rexpr) {
        todoInfo.add("Not implemented:    " + rexpr.getClass().getName() + "    " + rexpr.toString());
    }

    @Override
    public void visit(JsonExpression jsonExpr) {
        todoInfo.add("Not implemented:    " + jsonExpr.getClass().getName() + "    " + jsonExpr.toString());
    }

    @Override
    public void visit(JsonOperator jsonExpr) {
        todoInfo.add("Not implemented:    " + jsonExpr.getClass().getName() + "    " + jsonExpr.toString());
    }

    @Override
    public void visit(RegExpMySQLOperator regExpMySQLOperator) {
        todoInfo.add("Not implemented:    " + regExpMySQLOperator.getClass().getName() + "    " + regExpMySQLOperator.toString());
    }

    @Override
    public void visit(UserVariable var) {
        todoInfo.add("Not implemented:    " + var.getClass().getName() + "    " + var.toString());
    }

    @Override
    public void visit(NumericBind bind) {
        todoInfo.add("Not implemented:    " + bind.getClass().getName() + "    " + bind.toString());
    }

    @Override
    public void visit(KeepExpression aexpr) {
        todoInfo.add("Not implemented:    " + aexpr.getClass().getName() + "    " + aexpr.toString());
    }

    @Override
    public void visit(MySQLGroupConcat groupConcat) {
        todoInfo.add("Not implemented:    " + groupConcat.getClass().getName() + "    " + groupConcat.toString());
    }

    @Override
    public void visit(ValueListExpression valueList) {
        todoInfo.add("Not implemented:    " + valueList.getClass().getName() + "    " + valueList.toString());
    }

    @Override
    public void visit(RowConstructor rowConstructor) {
        todoInfo.add("Not implemented:    " + rowConstructor.getClass().getName() + "    " + rowConstructor.toString());
    }

    @Override
    public void visit(OracleHint hint) {
        todoInfo.add("Not implemented:    " + hint.getClass().getName() + "    " + hint.toString());
    }

    @Override
    public void visit(TimeKeyExpression timeKeyExpression) {
        todoInfo.add("Not implemented:    " + timeKeyExpression.getClass().getName() + "    " + timeKeyExpression.toString());
    }

    @Override
    public void visit(DateTimeLiteralExpression literal) {
        todoInfo.add("Not implemented:    " + literal.getClass().getName() + "    " + literal.toString());
    }

    @Override
    public void visit(NotExpression aThis) {
        todoInfo.add("Not implemented:    " + aThis.getClass().getName() + "    " + aThis.toString());
    }

    @Override
    public void visit(NextValExpression aThis) {
        todoInfo.add("Not implemented:    " + aThis.getClass().getName() + "    " + aThis.toString());
    }

    @Override
    public void visit(CollateExpression aThis) {
        todoInfo.add("Not implemented:    " + aThis.getClass().getName() + "    " + aThis.toString());
    }

    @Override
    public void visit(Comment comment) {
        sb.append("\n// ").append(comment.toString()).append("\n");
    }

    @Override
    public void visit(Commit commit) {
        todoInfo.add("Not implemented:    " + commit.getClass().getName() + "    " + commit.toString());
    }

    @Override
    public void visit(Delete delete) {
        sb.append("\n.deleteFrom(");
        if (delete.getTable() != null) {
            this.visit(delete.getTable());
        }
        sb.append(")");
        if (delete.getJoins() != null) {
            for (Join join : delete.getJoins()) {
                visit(join);
            }
        }
        if (delete.getWhere() != null) {
            sb.append("\n.where(");
            delete.getWhere().accept(this);
            sb.append(")");
        }
        if (delete.getOrderByElements() != null) {
            sb.append("\n.orderBy(");
            for (OrderByElement orderByElement : delete.getOrderByElements()) {
                orderByElement.accept(this);
            }
            sb.append(")");
        }
        if (delete.getLimit() != null) {
            Limit limit = delete.getLimit();
            if (limit.getRowCount() != null) {
                sb.append("\n.limit(");
                limit.getRowCount().accept(this);
                sb.append(")");
            }
        }
    }

    @Override
    public void visit(Update update) {
        if (update.getTables() != null) {
            for (Table table: update.getTables()) {
                AliasTableNameFinder aliasTableNameFinder = new AliasTableNameFinder();
                aliasTableMap.putAll(aliasTableNameFinder.getTableMap(table));
                onlyTable = aliasTableNameFinder.getOnlyTable();
            }
        }
        if (update.getFromItem() != null) {
            AliasTableNameFinder aliasTableNameFinder = new AliasTableNameFinder();
            aliasTableMap.putAll(aliasTableNameFinder.getTableMap(update.getFromItem()));
            onlyTable = aliasTableNameFinder.getOnlyTable();
        }
        if (update.getJoins() != null) {
            for (Join join : update.getJoins()) {
                AliasTableNameFinder aliasTableNameFinder = new AliasTableNameFinder();
                aliasTableMap.putAll(aliasTableNameFinder.getTableMap(join.getRightItem()));
            }
        }
        sb.append("\n.update(");
        if (update.getTables() != null) {
            for (Table table: update.getTables()) {
                table.accept((FromItemVisitor) this);
            }
        }
        sb.append(")");
        if (update.getColumns() != null && update.getExpressions() != null) {
            int l = Math.min(update.getColumns().size(), update.getExpressions().size());
            for (int i = 0; i < l; i++) {
                Column column = update.getColumns().get(i);
                Expression expression = update.getExpressions().get(i);
                sb.append("\n.set(");
                column.accept(this);
                sb.append(", ");
                expression.accept(this);
                sb.append(")");
            }
        }
        if (update.getFromItem() != null) {
            sb.append("\n.from(");
            update.getFromItem().accept(this);
            sb.append(")");
        }
        if (update.getJoins() != null) {
            for (Join join : update.getJoins()) {
                visit(join);
            }
        }
        if (update.getWhere() != null) {
            sb.append("\n.where(");
            update.getWhere().accept(this);
            sb.append(")");
        }
        if (update.getOrderByElements() != null) {
            sb.append("\n.orderBy(");
            for (OrderByElement orderByElement : update.getOrderByElements()) {
                orderByElement.accept(this);
            }
            sb.append(")");
        }
        if (update.getLimit() != null) {
            Limit limit = update.getLimit();
            if (limit.getRowCount() != null) {
                sb.append("\n.limit(");
                limit.getRowCount().accept(this);
                sb.append(")");
            }
        }
    }

    @Override
    public void visit(Insert insert) {
        if (insert.getTable() != null) {
            onlyTable = insert.getTable().getName().toUpperCase();
        }
        sb.append("\n.insertInto(");
        if (insert.getColumns() != null) {
            for (Column column : insert.getColumns()) {
                sb.append(",");
                column.accept(this);
            }
        } else if (insert.getTable() != null) {
            sb.append(insert.getTable().getName().toUpperCase());
        }
        sb.append(")");
        if (insert.getItemsList() != null) {
            ItemsList itemsList = insert.getItemsList();
            if (itemsList instanceof MultiExpressionList) {
                itemsList.accept(this);
            } else {
                sb.append("\n.values(");
                itemsList.accept(this);
                sb.append(")");
            }
        }
        if (insert.getSelect() != null) {
            insert.getSelect().accept(this);
        }
    }

    @Override
    public void visit(Replace replace) {
        todoInfo.add("Not implemented:    " + replace.getClass().getName() + "    " + replace.toString());
    }

    @Override
    public void visit(Drop drop) {
        todoInfo.add("Not implemented:    " + drop.getClass().getName() + "    " + drop.toString());
    }

    @Override
    public void visit(Truncate truncate) {
        todoInfo.add("Not implemented:    " + truncate.getClass().getName() + "    " + truncate.toString());
    }

    @Override
    public void visit(CreateIndex createIndex) {
        todoInfo.add("Not implemented:    " + createIndex.getClass().getName() + "    " + createIndex.toString());
    }

    @Override
    public void visit(CreateTable createTable) {
        todoInfo.add("Not implemented:    " + createTable.getClass().getName() + "    " + createTable.toString());
    }

    @Override
    public void visit(CreateView createView) {
        todoInfo.add("Not implemented:    " + createView.getClass().getName() + "    " + createView.toString());
    }

    @Override
    public void visit(AlterView alterView) {
        todoInfo.add("Not implemented:    " + alterView.getClass().getName() + "    " + alterView.toString());
    }

    @Override
    public void visit(Alter alter) {
        todoInfo.add("Not implemented:    " + alter.getClass().getName() + "    " + alter.toString());
    }

    @Override
    public void visit(Statements stmts) {
        todoInfo.add("Not implemented:    " + stmts.getClass().getName() + "    " + stmts.toString());
    }

    @Override
    public void visit(Execute execute) {
        todoInfo.add("Not implemented:    " + execute.getClass().getName() + "    " + execute.toString());
    }

    @Override
    public void visit(SetStatement set) {
        todoInfo.add("Not implemented:    " + set.getClass().getName() + "    " + set.toString());
    }

    @Override
    public void visit(ShowStatement set) {
        todoInfo.add("Not implemented:    " + set.getClass().getName() + "    " + set.toString());
    }

    @Override
    public void visit(Merge merge) {
        todoInfo.add("Not implemented:    " + merge.getClass().getName() + "    " + merge.toString());
    }

    @Override
    public void visit(Select select) {
        if (select.getSelectBody() != null) {
            select.getSelectBody().accept(this);
        }
    }

    @Override
    public void visit(Upsert upsert) {
        todoInfo.add("Not implemented:    " + upsert.getClass().getName() + "    " + upsert.toString());
    }

    @Override
    public void visit(UseStatement use) {
        todoInfo.add("Not implemented:    " + use.getClass().getName() + "    " + use.toString());
    }

    @Override
    public void visit(Block block) {
        todoInfo.add("Not implemented:    " + block.getClass().getName() + "    " + block.toString());
    }

    @Override
    public void visit(PlainSelect plainSelect) {
        if (plainSelect.getFromItem() != null) {
            AliasTableNameFinder aliasTableNameFinder = new AliasTableNameFinder();
            aliasTableMap.putAll(aliasTableNameFinder.getTableMap(plainSelect.getFromItem()));
            onlyTable = aliasTableNameFinder.getOnlyTable();
        }
        if (plainSelect.getJoins() != null) {
            for (Join join : plainSelect.getJoins()) {
                AliasTableNameFinder aliasTableNameFinder = new AliasTableNameFinder();
                aliasTableMap.putAll(aliasTableNameFinder.getTableMap(join.getRightItem()));
            }
        }
        sb.append("\n.select(");
        if (plainSelect.getSelectItems() != null) {
            int c = 1;
            for (SelectItem selectItem : plainSelect.getSelectItems()) {
                selectItem.accept(this);
                if (c % 3 == 0) {
                    sb.append("\n");
                }
                c++;
            }
        }
        sb.append(")");
        if (plainSelect.getFromItem() != null) {
            sb.append("\n.from(");
            plainSelect.getFromItem().accept(this);
            sb.append(")");
        }
        if (plainSelect.getJoins() != null) {
            for (Join join : plainSelect.getJoins()) {
                visit(join);
            }
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
            sb.append("\n.orderBy(");
            for (OrderByElement orderByElement : plainSelect.getOrderByElements()) {
                orderByElement.accept(this);
            }
            sb.append(")");
        }
        if (plainSelect.getOffset() != null) {
            sb.append("\n.offset(").append(plainSelect.getOffset().getOffset()).append(")");
        }
        if (plainSelect.getLimit() != null) {
            Limit limit = plainSelect.getLimit();
            if (limit.getOffset() != null) {
                sb.append("\n.offset(");
                limit.getOffset().accept(this);
                sb.append(")");
            }
            if (limit.getRowCount() != null) {
                sb.append("\n.limit(");
                limit.getRowCount().accept(this);
                sb.append(")");
            }
        }
    }

    @Override
    public void visit(SetOperationList setOpList) {
        todoInfo.add("Not implemented:    " + setOpList.getClass().getName() + "    " + setOpList.toString());
    }

    @Override
    public void visit(WithItem withItem) {
        todoInfo.add("Not implemented:    " + withItem.getClass().getName() + "    " + withItem.toString());
    }

    @Override
    public void visit(ValuesStatement values) {
        todoInfo.add("Not implemented:    " + values.getClass().getName() + "    " + values.toString());
    }

    @Override
    public void visit(DescribeStatement describe) {
        todoInfo.add("Not implemented:    " + describe.getClass().getName() + "    " + describe.toString());
    }

    @Override
    public void visit(ExplainStatement aThis) {
        todoInfo.add("Not implemented:    " + aThis.getClass().getName() + "    " + aThis.toString());
    }

    @Override
    public void visit(GroupByElement groupBy) {
        if (groupBy.getGroupByExpressions() != null) {
            sb.append("\n.groupBy(");
            for (Expression expression : groupBy.getGroupByExpressions()) {
                expression.accept(this);
                sb.append(", ");
            }
            sb.append(")");
        }
    }

    @Override
    public void visit(OrderByElement orderBy) {
        orderBy.getExpression().accept(this);
        sb.append(orderBy.isAsc() ? ".asc()" : ".desc()");
        sb.append(", ");
    }

    @Override
    public void visit(Pivot pivot) {
        todoInfo.add("Not implemented:    " + pivot.getClass().getName() + "    " + pivot.toString());
    }

    @Override
    public void visit(PivotXml pivot) {
        todoInfo.add("Not implemented:    " + pivot.getClass().getName() + "    " + pivot.toString());
    }

    @Override
    public void visit(AllColumns allColumns) {
        // do nothing
    }

    @Override
    public void visit(AllTableColumns allTableColumns) {
        allTableColumns.getTable().accept((FromItemVisitor) this);
        sb.append(", ");
    }

    @Override
    public void visit(SelectExpressionItem selectExpressionItem) {
        selectExpressionItem.getExpression().accept(this);
        if (selectExpressionItem.getAlias() != null) {
            sb.append(".as(\"").append(selectExpressionItem.getAlias().getName()).append("\")");
        }
        sb.append(", ");
    }
}
