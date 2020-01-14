package com.sevendark.ai.plugin.lib;

import org.intellij.lang.annotations.Language;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public interface Constant {

   String GROUP_NAME = "com.sevendark.ai.plugin.GroupedActions";

   @Language("RegExp")
   String VAR = ".*[a-z].*";
   @Language("RegExp")
   String STR = "^\"[^\"]+\"$";
   @Language("RegExp")
   String NUM = "\\d+";
   @Language("RegExp")
   String METHOD = "\\.?(([a-zA-Z0-9_]+)\\.)*[a-zA-Z_]+\\(?";
   @Language("RegExp")
   String JAVA_COMMENT = "//[\\S ]*";

   List<String> supportLanuage = new ArrayList<String>(){{
      add("java");
      add("scala");
      add("kotlin");
   }};

}
