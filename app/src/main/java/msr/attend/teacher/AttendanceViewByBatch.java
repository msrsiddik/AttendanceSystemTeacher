package msr.attend.teacher;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import msr.attend.teacher.Model.ClassAttendModel;
import msr.attend.teacher.Model.ClassPreferences;
import msr.attend.teacher.Model.StudentModel;
import msr.attend.teacher.Model.UserPref;
import msr.attend.teacher.Model.Utils;

public class AttendanceViewByBatch extends Fragment {
    private TextView totalClass;
    private EditText customAttendMark;
    private ListView studentList;
    private List<StudentModel> studentModelList;
    private FirebaseDatabaseHelper firebaseDatabaseHelper;
    private String subCode;
    private String batch;
    private String depart;
    private int highestClass;
    private ClassPreferences classPreferences;
    private int attendanceMark = 10;

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
        totalClass = view.findViewById(R.id.totalClass);
        customAttendMark = view.findViewById(R.id.customAttendMark);
        studentList = view.findViewById(R.id.studentList);

        firebaseDatabaseHelper = new FirebaseDatabaseHelper();

        classPreferences = new ClassPreferences(getContext());

        Bundle bundle = getArguments();
        this.depart = bundle.getString("depart");
        this.batch = bundle.getString("batch");
        loadStudentFromDb(depart, bundle.getString("batch"));
        this.subCode = bundle.getString("subCode");
        getActivity().setTitle("Batch : " + bundle.getString("batch") + " & Subject : " + subCode);

        customAttendMark.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() != 0 && !s.toString().contains(".")) {
                    attendanceMark = Integer.parseInt(s.toString());
                    loadStudentFromDb(depart,batch);
                } else {
                    attendanceMark = 10;
                    loadStudentFromDb(depart,batch);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        loadStudentFromDb(depart,batch);
    }

    private void loadStudentFromDb(String depart, String batch) {
        firebaseDatabaseHelper.getMyBatchStudent(depart, batch, list -> {
            if (getActivity() != null) {
                studentModelList = list;
                studentList.setAdapter(new MyStudentAdapter(getContext(), list));
                studentList.setOnItemClickListener((parent, view, position, id) -> {
                    Bundle bundle = new Bundle();
                    StudentModel model = list.get(position);
                    String s = Utils.getGsonParser().toJson(model);
                    bundle.putString("student", s);
                    bundle.putString("subCode", subCode);
                    bundle.putString("batch", batch);
                    bundle.putInt("totalClass", highestClass);
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
            View view = inflater.inflate(R.layout.my_batch_student_row, parent, false);

            StudentModel student = list.get(position);

            TextView name = view.findViewById(R.id.studentName);
            name.setText(student.getName());
            TextView roll = view.findViewById(R.id.studentRoll);
            roll.setText(student.getRoll());
            TextView attendCalc = view.findViewById(R.id.attendCalc);
            TextView attendPercent = view.findViewById(R.id.attendPercent);
            TextView attendMark = view.findViewById(R.id.attendMark);

            firebaseDatabaseHelper.getAllAttendanceInfoByStudentIdAndSubjectCode(student.getId(), subCode, attendList -> {
                if (highestClass < attendList.size()) {
                    highestClass = attendList.size();
                }
                totalClass.setText("Total Class : " + highestClass);
                classPreferences.setHighestClass(batch, highestClass);
                int presentAttendClass = attendList.size();
                attendCalc.setText(presentAttendClass + "");
                if (highestClass > 0) {
                    int percentAttendClass = (presentAttendClass * 100) / highestClass;
                    attendPercent.setText(percentAttendClass + "%");
                    attendMark.setText((percentAttendClass * attendanceMark) / 100 + "");

                    if (percentAttendClass >= 75) {
                        view.setBackgroundColor(Color.GREEN);
                    } else if (percentAttendClass >= 50 && percentAttendClass < 75) {
                        view.setBackgroundColor(Color.YELLOW);
                    } else {
                        view.setBackgroundColor(Color.RED);
                    }
                }
            });

            return view;
        }
    }
}