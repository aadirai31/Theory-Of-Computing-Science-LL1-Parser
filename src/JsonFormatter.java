import java.util.List;

/**
 * Utility class for converting parse trees to JSON format.
 * Handles nested lists, strings, and numbers according to JSON specification.
 */
public class JsonFormatter {

    /**
     * Converts a parse tree object to a formatted JSON string.
     *
     * @param tree The parse tree (Integer, String, or List<Object>)
     * @return JSON string representation
     */
    public static String toJson(Object tree) {
        return toJson(tree, 0, false);
    }

    /**
     * Converts a parse tree object to a formatted JSON string with indentation.
     *
     * @param tree The parse tree (Integer, String, or List<Object>)
     * @param indent Current indentation level
     * @param pretty Whether to use pretty printing with newlines and indentation
     * @return JSON string representation
     */
    private static String toJson(Object tree, int indent, boolean pretty) {
        if (tree == null) {
            return "null";
        } else if (tree instanceof Integer) {
            return tree.toString();
        } else if (tree instanceof String) {
            return escapeJsonString((String) tree);
        } else if (tree instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) tree;
            return listToJson(list, indent, pretty);
        } else {
            return "null";
        }
    }

    /**
     * Converts a list to JSON array format.
     */
    private static String listToJson(List<Object> list, int indent, boolean pretty) {
        if (list.isEmpty()) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("[");

        if (pretty) {
            sb.append("\n");
        }

        for (int i = 0; i < list.size(); i++) {
            if (pretty) {
                appendIndent(sb, indent + 1);
            }

            sb.append(toJson(list.get(i), indent + 1, pretty));

            if (i < list.size() - 1) {
                sb.append(",");
            }

            if (pretty) {
                sb.append("\n");
            }
        }

        if (pretty) {
            appendIndent(sb, indent);
        }

        sb.append("]");
        return sb.toString();
    }

    /**
     * Appends indentation spaces to a StringBuilder.
     */
    private static void appendIndent(StringBuilder sb, int indent) {
        for (int i = 0; i < indent * 2; i++) {
            sb.append(' ');
        }
    }

    /**
     * Escapes a string for JSON format.
     * Handles special characters: ", \, /, \b, \f, \n, \r, \t
     */
    private static String escapeJsonString(String str) {
        StringBuilder sb = new StringBuilder();
        sb.append("\"");

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '/':
                    sb.append("\\/");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (c < 0x20 || c > 0x7E) {
                        // Escape non-printable characters
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }

        sb.append("\"");
        return sb.toString();
    }

    /**
     * Converts a parse tree to pretty-printed JSON with indentation.
     */
    public static String toPrettyJson(Object tree) {
        return toJson(tree, 0, true);
    }
}
