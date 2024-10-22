package dev.gustavoesposar.reminder;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Calendar;

public class NovoAniversarianteFragment extends Fragment {

    private EditText nameInput;
    private EditText birthdateInput;
    private Button submitButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_novoaniversario, container, false);

        initializeViews(view);
        setupBirthdateInput();
        setupSubmitButton();

        return view;
    }

    private void initializeViews(View view) {
        nameInput = view.findViewById(R.id.name);
        birthdateInput = view.findViewById(R.id.birthdate);
        submitButton = view.findViewById(R.id.submit_button);
    }

    private void setupBirthdateInput() {
        birthdateInput.setOnClickListener(v -> openDatePicker());
    }

    private void openDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getActivity(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    birthdateInput.setText(selectedDate);
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private void setupSubmitButton() {
        submitButton.setOnClickListener(v -> handleFormSubmission());
    }

    private void handleFormSubmission() {
        String name = nameInput.getText().toString();
        String birthdate = birthdateInput.getText().toString();

        // to do
    }
}
