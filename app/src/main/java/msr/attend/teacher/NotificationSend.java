package msr.attend.teacher;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import msr.attend.teacher.Model.ClassModel;
import msr.attend.teacher.Model.NoticeModel;
import msr.attend.teacher.Model.UserPref;

public class NotificationSend extends Fragment {
    private UserPref userPref;
    private Button datePicker, sendBtn;
    private Spinner batchSpinner;
    private TextView tNoticeTitle, tNoticeBody;
    private EditText eNoticeTitle, eNoticeBody;
    private final Calendar myCalendar = Calendar.getInstance();
    private FirebaseDatabaseHelper firebaseDatabaseHelper;
    private FragmentInterface fragmentInterface;


    public NotificationSend() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notification_sender, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        datePicker = view.findViewById(R.id.datePicker);
        batchSpinner = view.findViewById(R.id.batchSpinner);
        tNoticeTitle = view.findViewById(R.id.tNoticeTitle);
        tNoticeBody = view.findViewById(R.id.tNoticeBody);
        eNoticeTitle = view.findViewById(R.id.eNoticeTitle);
        eNoticeBody = view.findViewById(R.id.eNoticeBody);
        sendBtn = view.findViewById(R.id.sendBtn);

        userPref = new UserPref(getContext());
        firebaseDatabaseHelper = new FirebaseDatabaseHelper();
        getActivity().setTitle("Notice Sender");

        fragmentInterface = (FragmentInterface) getActivity();

        updateLabel(1);
        DatePickerDialog.OnDateSetListener date = (view1, year, monthOfYear, dayOfMonth) -> {
            // TODO Auto-generated method stub
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel(0);
        };
        datePicker.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), date, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            datePickerDialog.show();
        });

        firebaseDatabaseHelper.getClassInfo(userPref.getTeacherId(), new FireMan.ClassInfoListener() {
            @Override
            public void classInfoIsLoaded(List<ClassModel> list) {
                Set<String> batch = new HashSet<>();
                for (ClassModel model : list){
                    batch.add(model.getBatch());
                }
                ArrayAdapter adapter = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, batch.toArray());
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                batchSpinner.setAdapter(adapter);
            }

            @Override
            public void classInfoIsInserted() {

            }
        });

        sendBtn.setOnClickListener(v -> {
            String title = eNoticeTitle.getText().toString();
            String body = eNoticeBody.getText().toString();
            String batch = batchSpinner.getSelectedItem().toString();
            firebaseDatabaseHelper.setNotice(new NoticeModel(userPref.getTeacherId(), batch,
                    title,body,datePicker.getText().toString()));
            firebaseDatabaseHelper.sendNoticeByBatch(title,body,batch,getContext());
            fragmentInterface.gotoMyNotification();
        });
    }

    private void updateLabel(int d) {
        String myFormat = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        myCalendar.add(Calendar.DATE, d);
        datePicker.setText(sdf.format(myCalendar.getTime()));
    }

}