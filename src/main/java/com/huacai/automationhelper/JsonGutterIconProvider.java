package com.huacai.automationhelper;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import com.huacai.automationhelper.framework.dto.Result;
import com.huacai.automationhelper.framework.dto.RunAutomationReportDto;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.icons.AllIcons;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.json.psi.JsonFile;
import com.intellij.json.psi.JsonProperty;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.RegisterToolWindowTask;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiEditorUtil;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.project.Project;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Objects;


public class JsonGutterIconProvider implements LineMarkerProvider {
    String automationNameValue = null;
    ConsoleView consoleView;
    final static String separator = "-----------------------------------------------------------------------------------";

    @Nullable
    @Override
    public LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
        // 检查文件类型是否为 JSON
        if (element.getContainingFile() instanceof JsonFile) {
            // 解析json文件为java json对象
            // 检查文件类型是否为 JSON

            // 检查 PsiElement 是否是 JSON 属性
            if (element instanceof JsonProperty property) {
                String key = property.getName();
                if ("automationName".equals(key) || "caseName".equals(key)) {
                    return new LineMarkerInfo<>(element, element.getTextRange(), IconLoader.getIcon("/META-INF/pluginIcon.svg", getClass()), // 使用 IconLoader 加载图标
                            null, (e, elt) -> {
                        try {
                            if ("automationName".equals(key)) {
                                // 初始化的时候刷新这个值, 文件内容有变化时，更新这个值
                                automationNameValue = Objects.requireNonNull(property.getValue()).getText().replace("\"", "");
                            }
                            consoleView = getConsoleView(element);

                            // 获取键的值
                            String caseName = property.getValue() != null ? property.getValue().getText().replace("\"", "") : "null";
                            JsonElement extracted = file2JsonObject(element);
                            Editor editor = PsiEditorUtil.findEditor(element);

                            if ("caseName".equals(key)) {
                                assert extracted != null;
                                assert editor != null;
                                HintManager.getInstance().showInformationHint(editor, "只发送caseName: " + caseName);
                                extracted = filterJsonByCaseName(extracted, caseName);
                            } else {
                                assert editor != null;
                                HintManager.getInstance().showInformationHint(editor, "发送整个automationName: " + automationNameValue);
                            }
                            String url = PropertiesComponent.getInstance().getValue("plugin.api.url");
                            assert extracted != null;
                            JsonObject requestBody = extracted.getAsJsonObject();
                            consoleView.print(separator + "开始运行自动化测试" + separator + "\n", ConsoleViewContentType.NORMAL_OUTPUT);
                            // 检查 useInputData 是否存在并且是否为 true
                            if (!requestBody.has("useInputData") || !requestBody.get("useInputData").getAsBoolean()) {
                                // 如果不存在或者为 false，则设置为 true
                                consoleView.print("插件自动赋值请求体的useInputData字段为true" + "\n", ConsoleViewContentType.LOG_WARNING_OUTPUT);
                                requestBody.add("useInputData", new JsonPrimitive(true));
                            }
                            JsonObject respJson = sendPostRequest(url, requestBody);
                            String htmlFormattedJson = "响应body: \n" + getFormattedJson(respJson) + "\n";
                            consoleView.print(htmlFormattedJson, ConsoleViewContentType.NORMAL_OUTPUT);
                            consoleView.print(String.format("%s运行结果统计%s\n", separator, separator), ConsoleViewContentType.LOG_WARNING_OUTPUT);
                            summaryRespAndPrint2Console(respJson);


                        } catch (Exception ex) {
                            for (StackTraceElement stackTraceElement : ex.getStackTrace()) {
                                consoleView.print(stackTraceElement.toString() + "\n", ConsoleViewContentType.ERROR_OUTPUT);
                            }
                            consoleView.print(String.format("请求失败，请检查网络连接或URL是否正确, url: %s \n", PropertiesComponent.getInstance().getValue("plugin.api.url")), ConsoleViewContentType.ERROR_OUTPUT);


                        }
                    }, GutterIconRenderer.Alignment.LEFT);
                }
            }
        }
        return null;
    }

    private void summaryRespAndPrint2Console(JsonObject respJson) {
        // 反序列为Result<RunAutomationReportDto>
        Result<RunAutomationReportDto> result = new Gson().fromJson(respJson, new TypeToken<Result<RunAutomationReportDto>>() {
        }.getType());
        // 输出data最外层的allSuccess
        // 输出reports每个对象的allSuccess和caseName，以及对象下面每个steps的step和result
        if (result != null && result.getData() != null) {
            RunAutomationReportDto data = result.getData();
            consoleView.print(String.format("automationName: %s, allSuccess: %s\n", automationNameValue, resultFormat(data.getAllSuccess().toString())), data.getAllSuccess() ? ConsoleViewContentType.NORMAL_OUTPUT : ConsoleViewContentType.ERROR_OUTPUT);
            data.getReports().forEach(report -> {
                consoleView.print(String.format("%s caseName: %s, allSuccess: %s%s\n", separator, report.getCaseName(), resultFormat(report.getAllSuccess().toString()), separator), ConsoleViewContentType.NORMAL_OUTPUT);
                report.getSteps().forEach(step -> {
                    // 如果step.result不是"success"，就输出为ERROR
                    consoleView.print(String.format("step: %s, result: %s\n", step.getStep(), resultFormat(step.getResult())), step.getResult().equals("success") ? ConsoleViewContentType.NORMAL_OUTPUT : ConsoleViewContentType.ERROR_OUTPUT);
                });
            });
        }
    }

    private static String resultFormat(String s) {
        return switch (s) {
            case "success", "true" -> s + " ✅";
            case "fail", "false" -> s + " ❌";
            case "skip" -> s + " ⚠️";
            default -> s;
        };
    }


    private static String getFormattedJson(JsonObject jsonObject) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(jsonObject);
    }

    private void print2NormalConsole(@NotNull PsiElement element, String htmlFormattedJson) {
        ConsoleView consoleView = getConsoleView(element);
        consoleView.print(htmlFormattedJson + "\n", ConsoleViewContentType.NORMAL_OUTPUT);
    }

    private @NotNull ConsoleView getConsoleView(@NotNull PsiElement element) {
        Project project = element.getProject();
        return getOrCreateConsole(project, automationNameValue);
    }

    // 过滤方法，根据传入的 caseName 过滤 JSON
    public JsonElement filterJsonByCaseName(JsonElement originalJson, String caseName) {
        if (!originalJson.isJsonObject()) {
            throw new IllegalArgumentException("Input must be a JSON object");
        }

        JsonObject jsonObject = originalJson.getAsJsonObject();

        // 创建新的 JsonObject，保留所有元素
        JsonObject newJsonObject = new JsonObject();
        for (String key : jsonObject.keySet()) {
            if (!"cases".equals(key)) {
                newJsonObject.add(key, jsonObject.get(key));
            }
        }

        // 过滤 cases
        JsonArray cases = jsonObject.getAsJsonArray("cases");
        JsonArray filteredCases = new JsonArray();

        for (JsonElement caseElement : cases) {
            JsonObject caseObject = caseElement.getAsJsonObject();
            if (caseName.equals(caseObject.get("caseName").getAsString())) {
                filteredCases.add(caseObject);
            }
        }

        // 将过滤后的 cases 添加到新的 JSON 对象中
        newJsonObject.add("cases", filteredCases);

        return newJsonObject;
    }

    private JsonElement file2JsonObject(@NotNull PsiElement element) {
        PsiFile file = element.getContainingFile();
        if (file instanceof JsonFile) {
            // 读取文件内容
            String fileContent = file.getText();
            // 解析 JSON 文件内容为 JSON 对象
            return JsonParser.parseString(fileContent);
        }
        return null;
    }

    public JsonObject sendPostRequest(String urlString, JsonObject body) throws Exception {
        // 创建 URL 对象
        URL url = new URL(urlString);

        // 打开与该 URL 的连接
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // 设置请求方法为 POST
        connection.setRequestMethod("POST");

        // 设置请求头
        connection.setRequestProperty("Content-Type", "application/json; utf-8");
        connection.setRequestProperty("Accept", "application/json");

        // 允许写入输出流
        connection.setDoOutput(true);

        consoleView.print("\n", ConsoleViewContentType.NORMAL_OUTPUT);
        LocalDateTime currentTime = LocalDateTime.now();
        String requestInfo = String.format("时间: %s 请求url: %s%n", currentTime.toString().substring(0, 19), url);
        consoleView.print(requestInfo, ConsoleViewContentType.NORMAL_OUTPUT);
        consoleView.print("发送请求body: " + body.toString() + "\n", ConsoleViewContentType.NORMAL_OUTPUT);
        // 将 JsonObject 转换为字符串并写入输出流
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = body.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        } catch (IOException e) {
            consoleView.print(String.format("URL: %s Call API Error: %s %n", urlString, e.getMessage()), ConsoleViewContentType.ERROR_OUTPUT);
            // 往上抛出
            throw e;
        }

        // 读取响应码（触发实际的请求发送）
        int responseCode = connection.getResponseCode();

        // 如果请求成功，读取响应内容
        if (responseCode == HttpURLConnection.HTTP_OK) { // HTTP 200 OK
            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = in.readLine()) != null) {
                    response.append(line.trim());
                }

                // 将响应字符串解析为 JsonObject
                return JsonParser.parseString(response.toString()).getAsJsonObject();
            }
        } else {
            consoleView.print(String.format("URL: %s Call API Error: %s %n", urlString, responseCode), ConsoleViewContentType.ERROR_OUTPUT);
            return null;
        }
    }

    private ConsoleView getOrCreateConsole(Project project, String automationName) {
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        ToolWindow toolWindow = toolWindowManager.getToolWindow("Automation");

        // 如果 ToolWindow 不存在，则注册一个新的 ToolWindow
        if (toolWindow == null) {
            toolWindow = toolWindowManager.registerToolWindow(RegisterToolWindowTask.closable("Automation", AllIcons.Toolwindows.ToolWindowMessages, ToolWindowAnchor.BOTTOM));
        }

        // 确保 ToolWindow 是可见的
        if (!toolWindow.isVisible()) {
            toolWindow.show(null);
        }

        ContentManager contentManager = toolWindow.getContentManager();
        Content existingContent = null;

        // 检查是否已有相同名称的 Content
        for (Content content : contentManager.getContents()) {
            if (content.getDisplayName().equals(getConsoleName(automationName))) {
                existingContent = content;
                JComponent component = existingContent.getComponent();
                // 切换到这个Console
                contentManager.setSelectedContent(content);
                if (component instanceof ConsoleView) {
                    return (ConsoleView) component;
                }
            }
        }

        if (existingContent != null) {
            // 如果已存在相同名称的 Content，可以选择更新或移除旧的 Content
            contentManager.removeContent(existingContent, true);
        }

        // 创建新的 ConsoleView
        ConsoleView consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
        ContentFactory contentFactory = ContentFactory.getInstance();
        Content content = contentFactory.createContent(consoleView.getComponent(), getConsoleName(automationName), false);
        contentManager.addContent(content);
        contentManager.setSelectedContent(content);

        return consoleView;
    }

    private static @NotNull String getConsoleName(String automationName) {
        return "Console-" + automationName;
    }

    private void switchToConsole(Project project, String automationName) {
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        ToolWindow toolWindow = toolWindowManager.getToolWindow("Automation");

        if (toolWindow != null) {
            // 激活工具窗口
            toolWindow.activate(() -> {
                ContentManager contentManager = toolWindow.getContentManager();
                for (Content content : contentManager.getContents()) {
                    if (content.getDisplayName().equals(getConsoleName(automationName))) {
                        contentManager.setSelectedContent(content);
                        break;
                    }
                }
            });
        }
    }


}

