package com.jimmy.friday.agent.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.StringTokenizer;
import java.util.function.Consumer;

public final class StringUtil {
    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    public static boolean isBlank(String str) {
        return str == null || isEmpty(str.trim());
    }

    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    public static void setIfPresent(String value, Consumer<String> setter) {
        if (isNotEmpty(value)) {
            setter.accept(value);
        }
    }

    public static boolean contains(CharSequence str, CharSequence searchStr) {
        return null != str && null != searchStr && str.toString().contains(searchStr);
    }

    public static boolean isEmpty(CharSequence str) {
        return str == null || str.length() == 0;
    }

    public static String subAfter(CharSequence string, CharSequence separator, boolean isLastSeparator) {
        if (isEmpty(string)) {
            return null == string ? null : "";
        } else if (separator == null) {
            return "";
        } else {
            String str = string.toString();
            String sep = separator.toString();
            int pos = isLastSeparator ? str.lastIndexOf(sep) : str.indexOf(sep);
            return -1 != pos && string.length() - 1 != pos ? str.substring(pos + separator.length()) : "";
        }
    }

    public static String subBefore(CharSequence string, CharSequence separator, boolean isLastSeparator) {
        if (!isEmpty(string) && separator != null) {
            String str = string.toString();
            String sep = separator.toString();
            if (sep.isEmpty()) {
                return "";
            } else {
                int pos = isLastSeparator ? str.lastIndexOf(sep) : str.indexOf(sep);
                if (-1 == pos) {
                    return str;
                } else {
                    return 0 == pos ? "" : str.substring(0, pos);
                }
            }
        } else {
            return null == string ? null : string.toString();
        }
    }

    public static String[] tokenizeToStringArray(String str, String delimiters, boolean trimTokens, boolean ignoreEmptyTokens) {
        if (str == null) {
            return new String[0];
        } else {
            StringTokenizer st = new StringTokenizer(str, delimiters);
            ArrayList tokens = new ArrayList();

            while (true) {
                String token;
                do {
                    if (!st.hasMoreTokens()) {
                        return toStringArray(tokens);
                    }

                    token = st.nextToken();
                    if (trimTokens) {
                        token = token.trim();
                    }
                } while (ignoreEmptyTokens && token.length() <= 0);

                tokens.add(token);
            }
        }
    }

    public static String[] toStringArray(Collection<String> collection) {
        return collection.toArray(new String[collection.size()]);
    }

    public static String join(final char delimiter, final String... strings) {
        if (strings.length == 0) {
            return null;
        }
        if (strings.length == 1) {
            return strings[0];
        }
        int length = strings.length - 1;
        for (final String s : strings) {
            if (s == null) {
                continue;
            }
            length += s.length();
        }
        final StringBuilder sb = new StringBuilder(length);
        if (strings[0] != null) {
            sb.append(strings[0]);
        }
        for (int i = 1; i < strings.length; ++i) {
            if (!isEmpty(strings[i])) {
                sb.append(delimiter).append(strings[i]);
            } else {
                sb.append(delimiter);
            }
        }
        return sb.toString();
    }

    public static boolean substringMatch(CharSequence str, int index, CharSequence substring) {
        if (index + substring.length() > str.length()) {
            return false;
        }
        for (int i = 0; i < substring.length(); i++) {
            if (str.charAt(index + i) != substring.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    public static String cut(String str, int threshold) {
        if (isEmpty(str) || str.length() <= threshold) {
            return str;
        }
        return str.substring(0, threshold);
    }

    public static String trim(final String str, final char ch) {
        if (isEmpty(str)) {
            return null;
        }

        final char[] chars = str.toCharArray();

        int i = 0, j = chars.length - 1;
        // noinspection StatementWithEmptyBody
        for (; i < chars.length && chars[i] == ch; i++) {
        }
        // noinspection StatementWithEmptyBody
        for (; j > 0 && chars[j] == ch; j--) {
        }

        return new String(chars, i, j - i + 1);
    }
}
