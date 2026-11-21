package fitmanage;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import java.awt.Frame;
import java.awt.event.KeyEvent;

/** Kotak pop-up (modal) yang membungkus sebuah FormPanel. Esc = tutup, Enter = tombol utama. */
final class FormDialog extends JDialog {

    static void tampilkan(Frame owner, FormPanel form) {
        new FormDialog(owner, form).setVisible(true);
    }

    private FormDialog(Frame owner, FormPanel form) {
        super(owner, form.getJudul(), true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        setContentPane(form);

        form.setPenutup(this::dispose);
        form.setPerbaruiUkuran(() -> {
            pack();
            setSize(Math.max(getWidth(), form.getLebarMin()), getHeight());
        });

        getRootPane().setDefaultButton(form.getTombolUtama());   // Enter = tombol utama
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        pack();
        setSize(Math.max(getWidth(), form.getLebarMin()), getHeight());
        setLocationRelativeTo(owner);
        SwingUtilities.invokeLater(form::fokusAwal);
    }
}
