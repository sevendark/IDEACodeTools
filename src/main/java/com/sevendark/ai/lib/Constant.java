package com.sevendark.ai.lib;

import java.util.regex.Pattern;

public interface Constant {
   String GROUP_NAME = "com.sevendark.ai.plugin.GroupedActions";
   String AFTER_REG = "\\(";
   String PRE_REG = "([A-Z_]+\\.[A-Z_]+)+\\.";
   String METHOD = "(([A-Z_]+\\.[A-Z_]+)\\.)?[a-zA-Z_]+\\(";

   static void test(){
     Pattern.compile(METHOD);
   }
}
