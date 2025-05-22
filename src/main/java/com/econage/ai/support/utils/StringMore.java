package com.econage.ai.support.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author hanpeng
 * @date 2025/2/21 17:21
 */
public class StringMore {
    public static final String SPACE = " ";

    public static String escapeBrackets(String input) {
        if(StringUtils.isEmpty(input)) {
            return input;
        }
        // 匹配单独的 `{`，但不匹配 `{{`
        Pattern leftBracePattern = Pattern.compile("(?<!\\{)\\{(?!\\{)");
        // 匹配单独的 `}`，但不匹配 `}}`
        Pattern rightBracePattern = Pattern.compile("(?<!\\})\\}(?!\\})");

        // 替换 `{` 为 `{{`
        Matcher leftMatcher = leftBracePattern.matcher(input);
        String result = leftMatcher.replaceAll("{{");

        // 替换 `}` 为 `}}`
        Matcher rightMatcher = rightBracePattern.matcher(result);
        result = rightMatcher.replaceAll("}}");

        return result;
    }
}
