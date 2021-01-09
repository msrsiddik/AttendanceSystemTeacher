package msr.attend.teacher;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import msr.attend.teacher.Model.ClassAttendModel;
import msr.attend.teacher.Model.ClassPreferences;
import msr.attend.teacher.Model.StudentModel;
import msr.attend.teacher.Model.Utils;

public class MyStudent extends Fragment {
    private TextView studentName, studentBatch, studentDepart, studentNo, guardianNo, attendanceCalculateView;
    private ListView attendanceDate;
    private ClassPreferences classPreferences;

    public MyStudent() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_student, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        studentName = view.findViewById(R.id.studentName);
        studentBatch = view.findViewById(R.id.studentBatch);
        studentDepart = view.findViewById(R.id.studentDepart);
        studentNo = view.findViewById(R.id.studentNo);
        guardianNo = view.findViewById(R.id.guardianNo);
        attendanceDate = view.findViewById(R.id.attendanceDate);
        attendanceCalculateView = view.findViewById(R.id.attendanceCalculateView);

        classPreferences = new ClassPreferences(getContext());

        Bundle bundle = getArguments();
        StudentModel model = Utils.getGsonParser().fromJson(bundle.getString("student"), StudentModel.class);
        String subCode = bundle.getString("subCode");
        String batch = bundle.getString("batch");
        studentName.setText(model.getName());
        studentBatch.setText(model.getBatch());
        studentDepart.setText(model.getDepartment());
        studentNo.setText(model.getStudentPhone());
        guardianNo.setText(model.getGuardianPhone());

        new FirebaseDatabaseHelper().getAllAttendanceInfoByStudentIdAndSubjectCode(model.getId(), subCode, attendList -> {
            List<String> date = new ArrayList<>();
            for (ClassAttendModel c : attendList){
                date.add(new SimpleDateFormat("dd-MM-yyyy h:mm").format(new Date(Long.parseLong(c.getDate()))));
            }

            int totalClass = classPreferences.getHighestClass(batch);
            if (totalClass > 0) {
                attendanceCalculateView.setText("Present " + date.size() + " class of " + totalClass + " | " + (date.size() * 100) / totalClass + "% Attend");
            } else {
                attendanceCalculateView.setText("There have been no classes so far!");
            }

            ArrayAdapter adapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1,date.toArray());
            attendanceDate.setAdapter(adapter);
        });
    }
}