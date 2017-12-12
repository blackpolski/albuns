package com.fernando.albuns.utils;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.EditText;

import java.text.DateFormat;
import java.util.Calendar;

/**
 * Created by 01796085090 on 24/11/2017.
 */

public class DateDialog extends DialogFragment implements DatePickerDialog.OnDateSetListener{
    private Calendar calendar;
    private EditText editText;
    private static DateFormat fmt = DateFormat.getDateInstance(DateFormat.LONG);

    public static DateDialog makeDialog(Calendar calendar, EditText editText) {
        DateDialog dialog = new DateDialog();
        dialog.calendar = calendar;
        dialog.editText = editText;
        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int dia = calendar.get(Calendar.DAY_OF_MONTH);
        int mes = calendar.get(Calendar.MONTH);
        int ano = calendar.get(Calendar.YEAR);

        return new DatePickerDialog(getActivity(), this, ano, mes, dia);
    }

    @Override
    public void onDateSet(DatePicker view, int ano, int mes, int dia) {
        calendar.set(ano, mes, dia);
        editText.setText(fmt.format(calendar.getTime()));
    }
}
