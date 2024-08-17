package com.huacai.automationhelper;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class PluginSettingsConfigurable implements Configurable {
    private JTextField urlField;
    private JPanel mainPanel;

    final static String defaultUrl = "http://localhost:8088/auto/run";

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Plugin Settings";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        urlField = new JTextField(25);  // Set width of the text field
        mainPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); // Set layout to left-align
        mainPanel.add(new JLabel("Run Automation API Endpoint:"));
        mainPanel.add(urlField);
        return mainPanel;
    }

    @Override
    public boolean isModified() {
        String storedUrl = PropertiesComponent.getInstance().getValue("plugin.api.url", defaultUrl);
        return !storedUrl.equals(urlField.getText());
    }

    @Override
    public void apply() throws ConfigurationException {
        String url = urlField.getText().trim();
        if (url.isEmpty()) {
            Messages.showErrorDialog("The API URL cannot be empty.", "Invalid URL");
            throw new ConfigurationException("URL cannot be empty.");
        }
        System.out.println("Saving URL: " + url);  // 调试输出
        PropertiesComponent.getInstance().setValue("plugin.api.url", url);
    }


    @Override
    public void reset() {
        String storedUrl = PropertiesComponent.getInstance().getValue("plugin.api.url", "http://localhost:8088/auto/run");
        if (storedUrl.isEmpty()) {
            storedUrl = defaultUrl;  // 确保使用默认值
        }
        urlField.setText(storedUrl);
    }


}
