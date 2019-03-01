package com.sevendark.sql.lib;

import java.util.ArrayList;
import java.util.List;

public class SQLStatement {
    public StringBuilder refName = new StringBuilder();
    public StringBuilder bodyStr = new StringBuilder();
    public StringBuilder qualifierSir = new StringBuilder();

    public List<SQLStatement> body = new ArrayList<>();
}
