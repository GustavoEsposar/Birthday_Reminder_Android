package dev.gustavoesposar.reminder.utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class DateTextWatcher implements TextWatcher {
    private final EditText editText;
    private boolean isUpdating = false;
    private final String mask = "##/##/####";

    public DateTextWatcher(EditText editText) {
        this.editText = editText;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (isUpdating) return;

        String input = s.toString().replaceAll("[^\\d]", "");
        StringBuilder formatted = new StringBuilder();

        int index = 0;
        for (char m : mask.toCharArray()) {
            if (m == '#' && index < input.length()) {
                formatted.append(input.charAt(index++));
            } else if (m == '/' && index <= input.length()) {
                formatted.append('/');
            }
        }

        isUpdating = true;
        editText.setText(formatted.toString());
        editText.setSelection(formatted.length());  // Move o cursor para o final
        isUpdating = false;
    }

    @Override
    public void afterTextChanged(Editable s) {}
}
