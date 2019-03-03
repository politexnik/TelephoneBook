package app;

import app.Database.DBConnect;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Vector;

class FindWindow extends JFrame {

    //размеры окон и начальные координаты
    private static final int WIN_HEIGHT = 400; // высота окна
    private static final int WIN_WIDTH = 507; // ширина окна
    private static final int WIN_POS_X = 600; // начальная координата
    private static final int WIN_POS_Y = 200; // начальная координата

    //кнопки на форме
    private JButton findUserButton = new JButton("Найти");
    private JTextArea surnameArea = new JTextArea("Введите фамилию здесь...");
    private JTable viewTable = new JTable();
    private DefaultTableModel defaultTableModel;    //модель для таблицы
    private JScrollPane jScrollPaneTable;

    private JPanel bottomPanel;

    FindWindow() {
        setBounds(WIN_POS_X, WIN_POS_Y, WIN_WIDTH, WIN_HEIGHT);        //размеры
        setTitle("Find Users");
        setResizable(false);
        addComponentsToPane(getContentPane());  //добавляем компоненты
        addListeners(getContentPane()); //добавляем слушателей
    }

    //добавление компонентов на форму
    private void addComponentsToPane(Container pane) {

        bottomPanel = new JPanel(new GridLayout(1, 2));  //нижняя панель
        bottomPanel.add(findUserButton);
        bottomPanel.add(surnameArea);

        // добавление панели к окну
        add(bottomPanel, BorderLayout.PAGE_END);

        //список телефонов
        getPhoneList();
        jScrollPaneTable = new JScrollPane(viewTable);
        add(viewTable.getTableHeader(), BorderLayout.PAGE_START);
        add(jScrollPaneTable);
    }

    //добавление слушателей
    private void addListeners(Container pane) {
        findUserButton.addMouseListener(new MouseAdapter() {
                                            @Override
                                            public void mouseClicked(MouseEvent e) {
                                                getFindPhoneList();
                                            }
                                        }
        );
    }

    //заполнение JTable
    private void getPhoneList() {
        String query = String.format("SELECT * FROM Users WHERE Surname = '%s'", " ");
        ResultSet rs = DBConnect.executeSelectQuery(query);
        defaultTableModel = buildTableModel(rs);

        viewTable = new JTable(defaultTableModel);
    }

    private void getFindPhoneList() {
        String query = String.format("SELECT * FROM Users WHERE Surname = '%s'", surnameArea.getText());
        ResultSet rs = DBConnect.executeSelectQuery(query);
        //удаляем все строки, если есть
        while (defaultTableModel.getRowCount() > 0) {
            defaultTableModel.removeRow(0);
        }
        ArrayList<String> temp = new ArrayList<String>();
        try {
            while (rs.next()) {
                temp.add(rs.getString(1));   //получаем строки из БД, добавляем в коллекцию
                temp.add(rs.getString(2));
                temp.add(rs.getString(3));
                temp.add(rs.getString(4));
                temp.add(rs.getString(5));
                defaultTableModel.addRow(temp.toArray());
                temp.clear();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        viewTable.repaint();
    }

    //экспорт из ResultSet в TableModel
    private static DefaultTableModel buildTableModel(ResultSet rs) {
        ResultSetMetaData metaData;
        Vector<Vector<Object>> data = null;
        Vector<String> columnNames = null;
        try {
            metaData = rs.getMetaData();

            // Имена столбцов
            columnNames = new Vector<String>();
            int columnCount = metaData.getColumnCount();
            columnNames = AppWindow.getVectorColumnNames(rs);

            // Данные таблицы
            data = AppWindow.getVectorData(rs, columnCount);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new DefaultTableModel(data, columnNames);
    }
}
