package msr.attend.teacher;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import msr.attend.teacher.Model.StudentModel;
import msr.attend.teacher.Model.Utils;

public class StudentProfile extends Fragment {
    private TextView studentName, studentBatch, studentDepart, studentNo, guardianNo;

    public StudentProfile() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_student_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        studentName = view.findViewById(R.id.studentName);
        studentBatch = view.findViewById(R.id.studentBatch);
        studentDepart = view.findViewById(R.id.studentDepart);
        studentNo = view.findViewById(R.id.studentNo);
        guardianNo = view.findViewById(R.id.guardianNo);

        Bundle bundle = getArguments();
        StudentModel model = Utils.getGsonParser().fromJson(bundle.getString("student"),StudentModel.class);
        studentName.setText(model.getName());
        studentBatch.setText(model.getBatch());
        studentDepart.setText(model.getDepartment());
        studentNo.setText(model.getStudentPhone());
        guardianNo.setText(model.getGuardianPhone());
        Toast.makeText(getContext(), ""+model.getId(), Toast.LENGTH_SHORT).show();
    }
}