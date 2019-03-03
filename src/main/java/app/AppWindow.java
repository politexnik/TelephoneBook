package app;

import app.Database.DBConnect;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

class AppWindow extends JFrame {

    //размеры окон и начальные координаты
    private static final int WIN_HEIGHT = 555; // высота окна
    private static final int WIN_WIDTH = 507; // ширина окна
    private static final int WIN_POS_X = 800; // начальная координата
    private static final int WIN_POS_Y = 300; // начальная координата

    private static final AtomicInteger idDeletedRow = new AtomicInteger();  //для запоминания удаленного ID

    //кнопки на форме
    private JButton findUserButton = new JButton("Найти...");
    private JButton deleteUserButton = new JButton("Удалить");
    private JButton addUserButton = new JButton("Добавить");
    private JTable viewTable = new JTable();
    private DefaultTableModel defaultTableModel;

    private JPanel bottomPanel;

    private FindWindow findWindow = new FindWindow();

    AppWindow() {
        setBounds(WIN_POS_X, WIN_POS_Y, WIN_WIDTH, WIN_HEIGHT);        //размеры
        setTitle("Telephone Book");
        setResizable(false);
        addComponentsToPane(getContentPane());  //добавляем компоненты
        addListeners(getContentPane()); //добавляем слушателей
        setVisible(true);
    }

    //добавление компонентов на форму
    private void addComponentsToPane(Container pane) {
        //нижняя панель
        bottomPanel = new JPanel(new GridLayout(1, 3));  //нижняя панель
        bottomPanel.add(findUserButton);
        bottomPanel.add(deleteUserButton);
        bottomPanel.add(addUserButton);

        // добавление панели к окну
        add(bottomPanel, BorderLayout.PAGE_END);

        //список телефонов
        getPhoneList();
        JScrollPane jScrollPaneTable = new JScrollPane(viewTable);
        add(viewTable.getTableHeader(), BorderLayout.PAGE_START);
        add(jScrollPaneTable);
    }

    //добавление слушателей
    private void addListeners(Container pane) {
        findUserButton.addMouseListener(new MouseAdapter() {
                                            @Override
                                            public void mouseClicked(MouseEvent e) {
                                                findWindow.setVisible(true);
                                            }
                                        }
        );
        //отслеживание изменений в таблице
        defaultTableModel.addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent e) {
                switch (e.getType()) {
                    case TableModelEvent.INSERT: {    //INSERT
                        //добавляем пустую строку в базу
                        int firstRowNumber = e.getFirstRow();
                        int id = (Integer) defaultTableModel.getValueAt(firstRowNumber, 0);
                        DBConnect.executeUpdateQuery(String.format("INSERT INTO Users (UserID) VALUES (%d);", id));
                        break;
                    }
                    case TableModelEvent.DELETE: {    //DELETE
                        //удаляем убранную в таблице строку из базы
                        int firstRowNumber = e.getFirstRow();
                        DBConnect.executeUpdateQuery(String.format("DELETE FROM Users WHERE UserID = %d;", idDeletedRow.get()));
                        break;
                    }
                    case TableModelEvent.UPDATE: {    //UPDATE
                        //вытаскиваем новое значение и пуляем его в базу
                        int columnNumber = e.getColumn();
                        int firstRowNumber = e.getFirstRow();
                        String newValue = (String) defaultTableModel.getValueAt(firstRowNumber, columnNumber);
                        int id = (Integer) defaultTableModel.getValueAt(firstRowNumber, 0);
                        String columnName = defaultTableModel.getColumnName(columnNumber);
                        DBConnect.executeUpdateQuery(String.format("UPDATE Users SET %s = '%s' WHERE UserID = %s;", columnName, newValue, id));
                        break;
                    }
                }
            }
        });

        //добавление записей
        addUserButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                //по клику добавляем новую строку в таблицу с ID на единицу больше последнего
                int lastID = (Integer) defaultTableModel.getValueAt(defaultTableModel.getRowCount() - 1, 0);
                defaultTableModel.addRow(new Object[]{lastID + 1});
            }
        });

        //удаление записей
        deleteUserButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                //устанавливаем в память удаленный ID, удаляем строку
                idDeletedRow.set((Integer)defaultTableModel.getValueAt(viewTable.getSelectedRow(), 0));
                defaultTableModel.removeRow(viewTable.getSelectedRow());
            }
        });

        //закрытие коннекта с БД при выходе из приложения
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                DBConnect.closeDB();
                super.windowClosed(e);
                System.exit(0);
            }
        });
    }

    //создание и заполнение JTable
    private void getPhoneList() {
        ResultSet rs = DBConnect.executeSelectQuery("SELECT * FROM Users");
        viewTable = new JTable(buildTableModel(rs));
    }

    //экспорт из ResultSet в TableModel
    private DefaultTableModel buildTableModel(ResultSet rs) {
        ResultSetMetaData metaData;

        Vector<String> columnNames = null;
        Vector<Vector<Object>> data = null;
        try {
            metaData = rs.getMetaData();

            // Имена столбцов
            columnNames = new Vector<String>();
            int columnCount = metaData.getColumnCount();
            columnNames = getVectorColumnNames(rs);

            // Данные таблицы
            data = getVectorData(rs, columnCount);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        defaultTableModel = new PhoneTable(data, columnNames);
        return defaultTableModel;
    }

    // Вытаскивает данные таблицы из resultSet
    static Vector<Vector<Object>> getVectorData(ResultSet rs, int columnCount) throws SQLException {
        Vector<Vector<Object>> data = new Vector<Vector<Object>>();
        while (rs.next()) {
            Vector<Object> vector = new Vector<Object>();
            for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                vector.add(rs.getObject(columnIndex));
            }
            data.add(vector);
        }
        return data;
    }

    // Вытаскивает имена столбцов из resultSet
    static Vector<String> getVectorColumnNames(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        Vector<String> columnNames = new Vector<String>();
        int columnCount = metaData.getColumnCount();
        for (int column = 1; column <= columnCount; column++) {
            columnNames.add(metaData.getColumnName(column));
        }
        return columnNames;
    }
}
