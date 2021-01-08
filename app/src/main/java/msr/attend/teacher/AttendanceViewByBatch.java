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
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import msr.attend.teacher.Model.ClassAttendModel;
import msr.attend.teacher.Model.ClassPreferences;
import msr.attend.teacher.Model.StudentModel;
import msr.attend.teacher.Model.UserPref;
import msr.attend.teacher.Model.Utils;

public class AttendanceViewByBatch extends Fragment {
    private ListView studentList;
    private List<StudentModel> studentModelList;
    private FirebaseDatabaseHelper firebaseDatabaseHelper;
    private String subCode;
    private String batch;
    private int highestClass = 0;
    private ClassPreferences classPreferences;

    public AttendanceViewByBatch() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_attendance_view_by_batch, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        studentList = view.findViewById(R.id.studentList);

        firebaseDatabaseHelper = new FirebaseDatabaseHelper();

        classPreferences = new ClassPreferences(getContext());

        Bundle bundle = getArguments();
        this.batch = bundle.getString("batch");
        loadStudentFromDb(bundle.getString("batch"));
        this.subCode = bundle.getString("subCode");
        getActivity().setTitle("Batch : "+bundle.getString("batch")+" & Subject : "+subCode);
    }

    private void loadStudentFromDb(String batch) {
        firebaseDatabaseHelper.getMyBatchStudent(batch, list -> {
            if (getActivity()!=null) {
                studentModelList = list;
                studentList.setAdapter(new MyStudentAdapter(getContext(), list));
                studentList.setOnItemClickListener((parent, view, position, id) -> {
                    Bundle bundle = new Bundle();
                    StudentModel model = list.get(position);
                    String s = Utils.getGsonParser().toJson(model);
                    bundle.putString("student", s);
                    bundle.putString("subCode", subCode);
                    bundle.putString("batch", batch);
                    MyStudent myStudent = new MyStudent();
                    myStudent.setArguments(bundle);
                    getFragmentManager().beginTransaction().replace(R.id.fragContainer, myStudent).addToBackStack(null).commit();
                });
            }
        });
    }

    class MyStudentAdapter extends ArrayAdapter<StudentModel> {
        Context context;
        List<StudentModel> list = null;
        public MyStudentAdapter(@NonNull Context context, @NonNull List<StudentModel> objects) {
            super(context, R.layout.my_batch_student_row, objects);
            this.context = context;
            this.list = objects;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            View view = inflater.inflate(R.layout.my_batch_student_row, parent,false);

            StudentModel student = list.get(position);

            TextView name = view.findViewById(R.id.studentName);
            name.setText(student.getName());
            TextView roll = view.findViewById(R.id.studentRoll);
            roll.setText(student.getRoll());
            TextView attendCalc = view.findViewById(R.id.attendCalc);
            firebaseDatabaseHelper.getAllAttendanceInfoByStudentIdAndSubjectCode(student.getId(), subCode, attendList -> {
                if (highestClass < attendList.size()){
                    highestClass = attendList.size();
                }
                classPreferences.setHighestClass(batch,highestClass);
                attendCalc.setText("Total Attend Class : "+ attendList.size());
            });

            return view;
        }
    }
}