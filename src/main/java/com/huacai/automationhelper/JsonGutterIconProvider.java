package com.huacai.automationhelper;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.json.psi.JsonFile;
import com.intellij.json.psi.JsonProperty;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class JsonGutterIconProvider implements LineMarkerProvider {

    String automationNameValue = null;

    @Nullable
    @Override
    public LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
        // 检查文件类型是否为 JSON
        if (element.getContainingFile() instanceof JsonFile) {
            // 检查 PsiElement 是否是 JSON 属性
            if (element instanceof JsonProperty) {
                JsonProperty property = (JsonProperty) element;
                String key = property.getName();
                if ("automationName".equals(key) || "caseName".equals(key)) {
                    if ("automationName".equals(key)) {
                        // 初始化的时候刷新这个值, 文件内容有变化时，更新这个值
                        automationNameValue = property.getValue().getText().replace("\"", "");
                    }
                    return new LineMarkerInfo<>(
                            element,
                            element.getTextRange(),
                            IconLoader.getIcon("/META-INF/pluginIcon.svg", getClass()), // 使用 IconLoader 加载图标
                            null,
                            (e, elt) -> {
                                try {
                                    // 获取键的值
                                    String value = property.getValue() != null ? property.getValue().getText().replace("\"", "") : "null";
                                    String urlString;
                                    if ("automationName".equals(key)) {
                                        urlString = "http://localhost:5001/run_proxy?automationName=" + URLEncoder.encode(automationNameValue, StandardCharsets.UTF_8);
                                    } else {
                                        if (automationNameValue != null) {
                                            urlString = "http://localhost:5001/run_proxy?automationName=" + URLEncoder.encode(automationNameValue, StandardCharsets.UTF_8) + "&caseName=" + URLEncoder.encode(value, StandardCharsets.UTF_8);
                                        } else {
                                            throw new Exception("automationName not found for caseName");
                                        }
                                    }
                                    String prettyJsonString = getString(urlString);
                                    // TODO 输出 prettyJsonString到控制台, 现在只有在调试的时候可以有，后面会改为类似http client的输出方式
                                    // 请求url
                                    LocalDateTime currentTime = LocalDateTime.now();
                                    System.out.printf("时间: %s 请求url: %s%n", currentTime.toString().substring(0, 19), urlString);
                                    System.out.println("响应：" + prettyJsonString + "\n");
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            },
                            GutterIconRenderer.Alignment.LEFT
                    );
                }
            }
        }
        return null;
    }

    private static String getString(String urlString) throws IOException {
        // 发送 GET 请求
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        conn.disconnect();
        // 格式化并输出 JSON 响应
        String jsonResponse = content.toString();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonElement jsonElement = JsonParser.parseString(jsonResponse);
        return gson.toJson(jsonElement);
    }

    @Nullable
    private JsonProperty findAutomationNameProperty(JsonProperty caseNameProperty) {
        PsiElement parent = caseNameProperty.getParent();
        while (parent != null) {
            if (parent instanceof JsonProperty) {
                JsonProperty parentProperty = (JsonProperty) parent;
                if ("automationName".equals(parentProperty.getName())) {
                    return parentProperty;
                }
            }
            parent = parent.getParent();
        }
        return null;
    }
}

