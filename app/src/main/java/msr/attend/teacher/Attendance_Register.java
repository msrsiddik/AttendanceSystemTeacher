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
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

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
    private ToggleButton universityEntryMode;
    private ListView studentList;
    private ClassModel classModel;
    private long date = Calendar.getInstance().getTime().getTime();
    private DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

    private FirebaseDatabaseHelper firebaseDatabaseHelper;
    private List<StudentModel> studentModelList = new ArrayList<>();
    private List<ClassAttendModel> classAttendModels;

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
        universityEntryMode = view.findViewById(R.id.universityEntryMode);
        studentList = view.findViewById(R.id.studentList);
        getActivity().setTitle("Attendance Register");

        Bundle bundle = getArguments();
        classModel = Utils.getGsonParser().fromJson(bundle.getString("classModel"), ClassModel.class);

        firebaseDatabaseHelper = new FirebaseDatabaseHelper();

//        loadStudent();

        universityEntryMode.setChecked(true);
        universityEntryByLoadStudent();

        universityEntryMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                universityEntryByLoadStudent();
            } else {
                loadStudent();
            }
        });
    }

    private void universityEntryByLoadStudent() {
        firebaseDatabaseHelper.getUniEntryCurrentStatus(dateFormat.format(Calendar.getInstance().getTime()),
                students -> {
                    List<StudentModel> tempStudentModels = new ArrayList<>();
                    for (StudentModel student : studentModelList) {
                        for (String id : students) {
                            if (student.getId().equals(id)) {
                                tempStudentModels.add(student);
                            }
                        }
                    }

                    Log.e("get", "" + students);
                    Log.e("Register", "" + tempStudentModels);
                    if (getActivity() != null) {
                        studentList.setAdapter(new AttendRegisterAdapter(getContext(), tempStudentModels, classAttendModels));
                    }
                });
    }

    private void loadStudent() {
        firebaseDatabaseHelper.getAttendDataByDate(dateFormat.format(date), classModel.getSubCode(),
                classAttendModels -> new FirebaseDatabaseHelper().getMyBatchStudent(classModel.getBatch(),
                        list -> {
                            this.studentModelList = list;
                            this.classAttendModels = classAttendModels;
                            if (getActivity() != null) {
                                studentList.setAdapter(new AttendRegisterAdapter(getContext(), list, classAttendModels));
                            }
                        }));
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
            name.setText(student.getRoll()+". "+student.getName());

            CheckBox attendCheck = view.findViewById(R.id.attendCheck);

            boolean check = false;
            for (ClassAttendModel attendModel : classAttendModels) {
                if (attendModel.getStuId().equals(student.getId())) {
                    check = Boolean.parseBoolean(attendModel.getPresent());
                }
            }
            attendCheck.setChecked(check);

            attendCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    ClassAttendModel attendModel = new ClassAttendModel(student.getId(), student.getBatch(), classModel.getSubCode(), String.valueOf(date), "true", classModel.getTeacherId());
                    new FirebaseDatabaseHelper().insertClassAttend(attendModel, dateFormat.format(date), context);
                } else {
                    ClassAttendModel attendModel = new ClassAttendModel(student.getId(), student.getBatch(), classModel.getSubCode(), String.valueOf(date), "false", classModel.getTeacherId());
                    new FirebaseDatabaseHelper().insertClassAttend(attendModel, dateFormat.format(date), context);
                }
            });

            return view;
        }
    }
}