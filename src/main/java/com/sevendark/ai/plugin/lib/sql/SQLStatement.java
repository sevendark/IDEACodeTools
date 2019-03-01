package com.sevendark.ai.plugin.lib.sql;

import java.util.ArrayList;
import java.util.List;



public class SQLStatement {
    public int i = 0;
    public StringBuilder refName = new StringBuilder();
    StringBuilder bodyStr = new StringBuilder();
    public StringBuilder qualifierSir = new StringBuilder();

    public List<SQLStatement> body = new ArrayList<>();
}
