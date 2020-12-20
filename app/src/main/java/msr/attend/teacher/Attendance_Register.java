package msr.attend.teacher;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import msr.attend.teacher.Model.ClassAttendModel;
import msr.attend.teacher.Model.ClassModel;
import msr.attend.teacher.Model.StudentModel;
import msr.attend.teacher.Model.Utils;

public class Attendance_Register extends Fragment {
    private ListView studentList;
    private ClassModel classModel;
    private long date = Calendar.getInstance().getTime().getTime();
    private DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

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
        classModel = Utils.getGsonParser().fromJson(bundle.getString("classModel"), ClassModel.class);

        new FirebaseDatabaseHelper().getAttendDataByDate(dateFormat.format(date), classModel.getSubCode(), new FireMan.ClassAttendListener() {
            @Override
            public void classIsLoaded(List<ClassAttendModel> classAttendModels) {
                new FirebaseDatabaseHelper().getMyBatchStudent(classModel.getBatch(),
                        list -> {
                    if (getActivity() != null) {
                        studentList.setAdapter(new AttendRegisterAdapter(getContext(), list, classAttendModels));
                    }
                });
            }
        });

//        new FirebaseDatabaseHelper().getMyBatchStudent(classModel.getBatch(),
//                list -> studentList.setAdapter(new AttendRegisterAdapter(getContext(), list, attendModels)));
    }

    class AttendRegisterAdapter extends ArrayAdapter<StudentModel> {
        Context context;
        List<StudentModel> list = null;
        List<ClassAttendModel> classAttendModels = null;

        public AttendRegisterAdapter(@NonNull Context context, @NonNull List<StudentModel> objects, List<ClassAttendModel> classAttendModels) {
            super(context, R.layout.attend_student_row, objects);
            this.context = context;
            this.list = objects;
            this.classAttendModels = classAttendModels;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.attend_student_row, parent, false);

            StudentModel student = list.get(position);

            TextView name = view.findViewById(R.id.studentName);
            name.setText(student.getName());

            CheckBox attendCheck = view.findViewById(R.id.attendCheck);

            boolean check = false;
            for (ClassAttendModel attendModel : classAttendModels){
                if (attendModel.getStuId().equals(student.getId())){
                    check = Boolean.parseBoolean(attendModel.getPresent());
                }
            }

            attendCheck.setChecked(check);

            attendCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    ClassAttendModel attendModel = new ClassAttendModel(student.getId(), classModel.getSubCode(), String.valueOf(date), "true", classModel.getTeacherId());
                    new FirebaseDatabaseHelper().insertClassAttend(attendModel, dateFormat.format(date), context);
                } else {
                    ClassAttendModel attendModel = new ClassAttendModel(student.getId(), classModel.getSubCode(), String.valueOf(date), "false", classModel.getTeacherId());
                    new FirebaseDatabaseHelper().insertClassAttend(attendModel, dateFormat.format(date), context);
                }
            });

            return view;
        }
    }
}