package com.github.myzhan.plugin.action;

import com.github.myzhan.plugin.decompiler.FernflowerDecompiler;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.ui.DialogBuilder;

import org.apache.commons.lang3.StringUtils;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import org.python.core.imp;

import java.io.*;

public class ShowCompiledJavaCode extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent event) {
        Editor editor = event.getRequiredData(CommonDataKeys.EDITOR);
        SelectionModel selectionModel = editor.getSelectionModel();
        String selectedText = selectionModel.getSelectedText();
        if (StringUtils.isBlank(selectedText)) {
            // 没有选中文本，获取整个文件
            Document document = editor.getDocument();
            selectedText = document.getText();
        }
        try {
            byte[] bytecode = compileToBytecode(selectedText);
            String javaCode = decompileToJavacode(bytecode);
            showCode(event, javaCode);
        } catch (Exception ex) {
            showMessage(event,"编译失败: <br>"+ex.getMessage());
        }
    }

    private byte[] compileToBytecode(String pycode) {
        InputStream input = new ByteArrayInputStream(pycode.getBytes());
        byte[] bytes = imp.compileSource("tmp", input , null, imp.NO_MTIME);
        return bytes;
    }

    private String decompileToJavacode(byte[] bytecode) throws Exception {
        FernflowerDecompiler decompiler = new FernflowerDecompiler();
        return decompiler.decompile(bytecode);
    }

    private void showMessage(AnActionEvent event, String message) {
        DialogBuilder dialogBuilder = new DialogBuilder(event.getProject());
        dialogBuilder.setTitle("错误");
        dialogBuilder.setErrorText(message);
        dialogBuilder.show();
    }

    private void showCode(AnActionEvent event, String code) {
        RSyntaxTextArea richCodeTextArea = new RSyntaxTextArea();
        richCodeTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        richCodeTextArea.setCodeFoldingEnabled(true);
        richCodeTextArea.setTabSize(4);
        richCodeTextArea.setTabsEmulated(true);
        richCodeTextArea.setText(code);
        RTextScrollPane editorPane = new RTextScrollPane(richCodeTextArea);

        DialogBuilder dialogBuilder = new DialogBuilder(event.getProject());
        dialogBuilder.setCenterPanel(editorPane);
        dialogBuilder.setTitle("Java代码");
        dialogBuilder.show();
    }
}
