package app;

import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import java.util.Vector;

public class PhoneTable extends DefaultTableModel {
    @Override
    public boolean isCellEditable(int row, int column) {
        //защищаем столбец с userID от изменений
        return column != 0;
    }

    public PhoneTable(Vector<? extends Vector> data, Vector<?> columnNames) {
        super(data, columnNames);
    }
}
