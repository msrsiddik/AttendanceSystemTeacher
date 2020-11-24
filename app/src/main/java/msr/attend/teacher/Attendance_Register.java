package msr.attend.teacher;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import msr.attend.teacher.Model.ClassModel;
import msr.attend.teacher.Model.StudentModel;
import msr.attend.teacher.Model.Utils;

public class Attendance_Register extends Fragment {
    private ListView studentList;

    public Attendance_Register() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_attendance__register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        studentList = view.findViewById(R.id.studentList);

        Bundle bundle = getArguments();
        ClassModel classModel = Utils.getGsonParser().fromJson(bundle.getString("classModel"), ClassModel.class);

        new FirebaseDatabaseHelper().getMyBatchStudent(classModel.getBatch(), new FireMan.MyBatchStudentLoad() {
            @Override
            public void studentIsLoaded(List<StudentModel> list) {
                studentList.setAdapter(new AttendRegisterAdapter(getContext(), list));
            }
        });
    }

    class AttendRegisterAdapter extends ArrayAdapter<StudentModel> {
        Context context;
        List<StudentModel> list = null;
        public AttendRegisterAdapter(@NonNull Context context, @NonNull List<StudentModel> objects) {
            super(context, R.layout.attend_student_row, objects);
            this.context = context;
            this.list = objects;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.attend_student_row, parent, false);

            TextView name = view.findViewById(R.id.studentName);
            name.setText(list.get(position).getName());

            CheckBox attendCheck = view.findViewById(R.id.attendCheck);

            return view;
        }
    }
}