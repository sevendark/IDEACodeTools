package com.sevendark.ai.plugin.lib.sql;

import java.util.ArrayList;
import java.util.List;



public class SQLStatement {
    public int i = 0;
    public StringBuilder qualifierSir = new StringBuilder();
    public StringBuilder refName = new StringBuilder();
    public List<SQLStatement> body = new ArrayList<>();
    public boolean haveDott = false;

    StringBuilder bodyStr = new StringBuilder();
}
