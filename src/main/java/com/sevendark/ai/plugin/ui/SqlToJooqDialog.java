package com.sevendark.ai.plugin.ui;

import com.intellij.openapi.ide.CopyPasteManager;
import com.sevendark.ai.plugin.lib.sql.JooqToSqlConverter;
import com.sevendark.ai.plugin.lib.sql.parser.SqlParserVisitor;

import javax.swing.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;

public class SqlToJooqDialog extends JDialog {
    private JPanel contentPane;
    private JTextArea sqlArea;
    private JTextArea jooqArea;
    private JButton copySqlBtn;
    private JButton copyJooqBtn;
    private JButton sqlToJooqBtn;
    private JButton jooqToSqlBtn;
    private JLabel introLabel;

    private final CopyPasteManager copyPasteManager = CopyPasteManager.getInstance();

    public SqlToJooqDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(sqlToJooqBtn);

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        sqlToJooqBtn.addActionListener(e -> {
            String sql = sqlArea.getText();
            String code = SqlParserVisitor.parse(sql);
            jooqArea.setText(code);
        });

        jooqToSqlBtn.addActionListener(e -> {
            String code = jooqArea.getText();
            String sql = JooqToSqlConverter.convert(code);
            if (sql != null) {
                sqlArea.setText(sql);
            } else {
                sqlArea.setText("Invalid jooq code");
            }
        });

        copySqlBtn.addActionListener(e -> {
            String str = sqlArea.getText();
            copyPasteManager.setContents(new StringSelection(str));
        });

        copyJooqBtn.addActionListener(e -> {
            String str = jooqArea.getText();
            copyPasteManager.setContents(new StringSelection(str));
        });
    }

    public JComponent getPreferredFocusedComponent() {
        return sqlArea;
    }

    public JComponent getCenterPanel() {
        return contentPane;
    }

    private void onCancel() {
        dispose();
    }

    public static void main(String[] args) {
        SqlToJooqDialog dialog = new SqlToJooqDialog();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
