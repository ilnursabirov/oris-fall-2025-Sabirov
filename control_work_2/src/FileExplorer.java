// сделать простой проводник файлов чтобы можно было смотреть папки файлы на java swing не использовать JFileSelector

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;


public class FileExplorer extends JFrame{

    private DefaultListModel<File> listModel = new DefaultListModel<>();
    private JList<File> fileList = new JList<>(listModel);
    private String currentPath = System.getProperty("user.home");

    public FileExplorer(){
        initUI();
        navigateTo(currentPath);
    }

    private void initUI() {
        setTitle("FileExplorer");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(720, 500);
        setLocationRelativeTo(null);

        fileList.setCellRenderer(new FileCellRenderer());
        fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        fileList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openSelected();
                }
            }
        });

        fileList.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "open");
        fileList.getActionMap().put("open", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                openSelected();
            }
        });

        add(new JScrollPane(fileList), BorderLayout.CENTER);

        JButton upButton = new JButton(getUpButtonText());
        upButton.addActionListener(e -> goUp());
        add(upButton, BorderLayout.NORTH);

        // Горячая клавиша: ⌘+↑ (macOS), Alt+↑ (Windows/Linux)
        int menuMask = getPlatformMenuMask();
        KeyStroke upShortcut = KeyStroke.getKeyStroke(KeyEvent.VK_UP, menuMask);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(upShortcut, "goUp");
        getRootPane().getActionMap().put("goUp", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                goUp();
            }
        });

    }

    private void openSelected() {
        File f = fileList.getSelectedValue();
        if (f == null) return;
        if (f.isDirectory()) {
            navigateTo(f.getAbsolutePath());
        } else {
            openFile(f);
        }
    }

    private void goUp() {
        File parent = new File(currentPath).getParentFile();
        if (parent != null) {
            navigateTo(parent.getAbsolutePath());
        }
    }

    private void navigateTo(String path) {
        currentPath = path;
        File dir = new File(currentPath);

        String title  = dir.getName();
        if(title.isEmpty()) {
            title = dir.getAbsolutePath();
        }
        setTitle(title);

        listModel.clear();
        File[] files = dir.listFiles();
        if (files == null) return;

        Arrays.sort(files, (a, b) -> {
            boolean aDir = a.isDirectory(), bDir = b.isDirectory();
            if (aDir && !bDir) return -1;
            if (!aDir && bDir) return 1;
            return a.getName().compareToIgnoreCase(b.getName());
        });

        for (File f : files) {
            // Пропускаем скрытые: .name на Unix/macOS и isHidden() везде
            if (f.isHidden() || isUnixHidden(f)) continue;
            listModel.addElement(f);
        }

    }

    private static boolean isUnixHidden(File f) {
        String name = f.getName();
        return name.startsWith(".") && !name.equals(".") && !name.equals("..");
    }

    private void openFile(File file) {
        if (!Desktop.isDesktopSupported()) {
            showError("Desktop API не поддерживается");
            return;
        }
        Desktop desktop = Desktop.getDesktop();
        if (!desktop.isSupported(Desktop.Action.OPEN)) {
            showError("Открытие файлов не поддерживается");
            return;
        }
        try {
            desktop.open(file);
        } catch (IOException | SecurityException e) {
            showError("Не удалось открыть файл:\n" + e.getMessage());
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }


    private static int getPlatformMenuMask() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("mac")) {
            return KeyEvent.META_DOWN_MASK; // ⌘
        } else {
            return KeyEvent.ALT_DOWN_MASK;  // Alt
        }
    }

    private static String getUpButtonText() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("mac") ? "↑" : "↑ Up";
    }

    private static class FileCellRenderer extends DefaultListCellRenderer{
        @Override
        public  Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            File file = (File) value;
            String text = file.getName() + (file.isDirectory() ? "/" : "");
            Icon icon = file.isDirectory()
                    ? UIManager.getIcon("FileView.directoryIcon")
                    : UIManager.getIcon("FileView.fileIcon");

            JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, text, index, isSelected, cellHasFocus);
            label.setIcon(icon);
            return label;
        }
    }

    static {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("mac")){
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "FileExplorer");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FileExplorer().setVisible(true));
    }

}

