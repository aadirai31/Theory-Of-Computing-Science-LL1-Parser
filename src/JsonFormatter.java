import java.util.List;

public class JsonFormatter {

    public static String toJson(Object tree) {
        return toJson(tree, 0, false);
    }

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

    private static void appendIndent(StringBuilder sb, int indent) {
        for (int i = 0; i < indent * 2; i++) {
            sb.append(' ');
        }
    }

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
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }

        sb.append("\"");
        return sb.toString();
    }

    public static String toPrettyJson(Object tree) {
        return toJson(tree, 0, true);
    }
}
