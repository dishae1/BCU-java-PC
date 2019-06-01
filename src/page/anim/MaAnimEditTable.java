package page.anim;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import javax.swing.ListSelectionModel;
import javax.swing.text.JTextComponent;

import page.Page;
import page.support.AnimTable;
import page.support.AnimTableTH;
import util.anim.AnimC;
import util.anim.MaAnim;
import util.anim.Part;

public class MaAnimEditTable extends AnimTable<Part> {

	private static final long serialVersionUID = 1L;

	private static final String[] strs = new String[] { "part id", "modification", "loop", "name" };

	public AnimC anim;
	public MaAnim ma;
	private Page page;

	protected MaAnimEditTable(Page p) {
		page = p;
		selectionModel.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		setTransferHandler(new AnimTableTH<>(this, 2));
	}

	@Override
	public boolean editCellAt(int row, int column, EventObject e) {
		boolean result = super.editCellAt(row, column, e);
		final Component editor = getEditorComponent();
		if (editor == null || !(editor instanceof JTextComponent))
			return result;
		if (e instanceof KeyEvent)
			((JTextComponent) editor).selectAll();
		return result;
	}

	@Override
	public Class<?> getColumnClass(int c) {
		if (lnk[c] == 3)
			return String.class;
		return Integer.class;
	}

	@Override
	public int getColumnCount() {
		return strs.length;
	}

	@Override
	public String getColumnName(int c) {
		return strs[lnk[c]];
	}

	@Override
	public int getRowCount() {
		if (ma == null)
			return 0;
		return ma.n;
	}

	@Override
	public Part[] getSelected() {
		int[] rows = getSelectedRows();
		Part[] ps = new Part[rows.length];
		for (int i = 0; i < rows.length; i++)
			ps[i] = ma.parts[rows[i]];
		return ps;
	}

	@Override
	public Object getValueAt(int r, int c) {
		if (ma == null || r < 0 || c < 0 || r >= ma.n || c >= strs.length)
			return null;
		if (lnk[c] == 3)
			return ma.parts[r].name;
		return ma.parts[r].ints[lnk[c]];
	}

	@Override
	public boolean insert(int dst, Part[] data) {
		List<Part> l = new ArrayList<>();
		int ind = dst;
		for (Part p : ma.parts)
			if (p != null)
				l.add(p);
		for (int i = 0; i < data.length; i++) {
			l.add(i + ind, data[i] = data[i].clone());
			data[i].check(anim);
		}
		ma.parts = l.toArray(new Part[0]);
		ma.n = ma.parts.length;
		anim.unSave("maanim paste part");
		page.callBack(new int[] { 0, ind, ind + data.length - 1 });
		return true;
	}

	@Override
	public boolean isCellEditable(int r, int c) {
		return true;
	}

	@Override
	public boolean reorder(int dst, int[] ori) {
		List<Part> l = new ArrayList<>();
		List<Part> ab = new ArrayList<>();
		for (int row : ori) {
			ab.add(ma.parts[row]);
			ma.parts[row] = null;
		}
		for (int i = 0; i < dst; i++)
			if (ma.parts[i] != null)
				l.add(ma.parts[i]);
		int ind = l.size();
		l.addAll(ab);
		for (int i = dst; i < ma.n; i++)
			if (ma.parts[i] != null)
				l.add(ma.parts[i]);
		ma.parts = l.toArray(new Part[0]);
		anim.unSave("maanim reorder part");
		page.callBack(new int[] { 0, ind, ind + ori.length - 1 });
		return true;
	}

	@Override
	public synchronized void setValueAt(Object val, int r, int c) {
		if (ma == null)
			return;
		c = lnk[c];
		if (c == 3) {
			ma.parts[r].name = ((String) val).trim();
			anim.unSave("maanim edit name");
			return;
		}
		int v = (int) val;
		if (c == 0)
			if (v < 0)
				v = 0;
			else if (v >= anim.mamodel.n)
				v = anim.mamodel.n - 1;
		if (c == 1)
			if ((v < 0 || v > 14) && v != 50)
				v = 5;
		if (c == 2 && (v < -1 || v == 0))
			v = -1;
		ma.parts[r].ints[c] = v;
		ma.parts[r].validate();
		ma.validate();
		anim.unSave("maanim edit part");
		page.callBack(null);
	}

	protected void setAnim(AnimC au, MaAnim maa) {
		if (cellEditor != null)
			cellEditor.stopCellEditing();
		anim = au;
		ma = maa;
	}

}