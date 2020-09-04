package com.company;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static Object parseJSONObject(String content) throws IOException {

        Map<String, Object> parsedObj = new HashMap<>();

        String s = content.substring(content.indexOf('{') + 1, content.lastIndexOf('}'));

        while (true) {

            //find first KV
            int kvBreak = s.indexOf(':');
            String key = getKey(s.substring(s.indexOf('"'), kvBreak));

            char valStartToken = s.substring(kvBreak + 1, kvBreak + 2).charAt(0);
            if (valStartToken == '"') {
                //value is string
                //get string value
                int valueEndIdx = s.indexOf('"', kvBreak + 2);

                String value = s.substring(kvBreak + 2, valueEndIdx);
                parsedObj.put(key, value);

                if (valueEndIdx + 1 == s.length()) {
                    //string parse over
                    break;
                }
                s = s.substring(s.indexOf(",", valueEndIdx) + 1);
            }
            else if (valStartToken == '{') {
                //value is json object
                //find the end of the json object
                Stack<Integer> brackets = new Stack<>();
                brackets.push(1);
                int i = kvBreak + 2;
                for (; i < s.length(); i++) {
                    if (s.charAt(i) == '{') {
                        brackets.push(1);
                    }
                    else if (s.charAt(i) == '}') {
                        brackets.pop();
                    }
                    if (brackets.empty()) {
                        Object value = parseJSONObject(s.substring(kvBreak + 1, i + 1));
                        parsedObj.put(key, value);
                        break;
                    }
                }

                if (i + 1 == s.length()) {
                    //string parse over
                    break;
                }

                s = s.substring(i + 2);
            }
            else if (valStartToken == '[') {
                //value is array
                //find the end of array
                Stack<Integer> brackets = new Stack<>();
                brackets.push(1);

                int i = kvBreak + 2;
                for (; i < s.length(); i++) {
                    if (s.charAt(i) == '[') {
                        brackets.push(1);
                    }
                    else if (s.charAt(i) == ']') {
                        brackets.pop();
                    }

                    if (brackets.empty()) {
                        List<Object> value = parseJSONArray(s.substring(kvBreak + 1, i + 1));
                        parsedObj.put(key, value);
                        break;
                    }
                }

                if (i + 1 == s.length()) {
                    //string parse over
                    break;
                }

                s = s.substring(i + 2);
            }
            else {
                //value is number
                int valueEndIdx = s.indexOf(',', kvBreak + 1);
                if (valueEndIdx == -1) {
                    String value = s.substring(kvBreak + 1);
                    parsedObj.put(key, Integer.parseInt(value));
                    break;
                }
                else {
                    String value = s.substring(kvBreak + 1, valueEndIdx);
                    parsedObj.put(key, Integer.parseInt(value));
                    s = s.substring(valueEndIdx + 1);
                }
            }
        }

        return parsedObj;
    }

    public static List<Object> parseJSONArray(String content) throws IOException {
        ArrayList<Object> arrayList = new ArrayList<>();
        String s = content.substring(content.indexOf('[') + 1, content.lastIndexOf(']'));

        Stack<Integer> stack = new Stack<>();
        int itemStartIdx = 0;
        int itemEndIdx = 0;
        for (int i = 0; i < s.length(); i++) {

            if (s.charAt(i) == '{') {
                if (stack.empty()) {
                    itemStartIdx = i;
                }
                stack.push(i);
            }
            else if (s.charAt(i) == '}') {
                stack.pop();
                if (stack.empty()) {
                    itemEndIdx = i;
                    arrayList.add(parseJSONObject(s.substring(itemStartIdx, itemEndIdx + 1)));
                }
            }
        }
        return arrayList;
    }


    public static String getKey(String ks) {
        return ks.substring(ks.indexOf('"') + 1, ks.lastIndexOf('"'));
    }

    public static String strip(String s) {
        String dest = "";
        Pattern p = Pattern.compile("\\s*|\t|\r|\n");
        Matcher m = p.matcher(s);
        dest = m.replaceAll("");
        return dest;
    }

    public static void main(String[] args) throws IOException {
        String content = " {\n" +
                "     \"ret\": 1,\n" +
                "     \"msg\": \"xxx\",\n" +
                "     \"data\": {\n" +
                "         \"red\": [{\"clr2\":2, \"clr\":3}],\n" +
                "         \"projectInfoById\": {\n" +
                "             \"name\": \"佳未开始\",\n" +
                "             \"merchantId\": 999,\n" +
                "             \"remark\": \"abc\"\n" +
                "         },\n" +
                "         \"awards\": [\n" +
                "             {\n" +
                "                 \"name\": \"award1\"\n" +
                "             },\n" +
                "             {\n" +
                "                 \"name\": \"award2\"\n" +
                "             }\n" +
                "         ]\n" +
                "     }\n" +
                " }";

        Object res = parseJSONObject(strip(content));
        System.out.println(res.toString());
    }
}
