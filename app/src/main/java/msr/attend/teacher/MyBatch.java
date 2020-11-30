package msr.attend.teacher;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import msr.attend.teacher.Model.StudentModel;
import msr.attend.teacher.Model.UserPref;
import msr.attend.teacher.Model.Utils;

public class MyBatch extends Fragment {
    private UserPref userPref;
    private FloatingActionButton addStudentBtn;
    private FragmentInterface fragmentInterface;
    private ListView studentListView;
    private List<StudentModel> studentModelList = null;

    public MyBatch() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_batch, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        studentListView = view.findViewById(R.id.myBatchStudentList);
        userPref = new UserPref(getContext());
        addStudentBtn = view.findViewById(R.id.addStudentBtn);

        loadStudentFromDb();

        fragmentInterface = (FragmentInterface) getActivity();
        addStudentBtn.setOnClickListener(v -> fragmentInterface.addStudentForm());
        registerForContextMenu(studentListView);

    }

    @Override
    public void onStart() {
        super.onStart();
        loadStudentFromDb();
    }

    private void loadStudentFromDb() {
        new FirebaseDatabaseHelper().getMyBatchStudent(userPref.getMyBatch(), list -> {
            if (getActivity()!=null) {
                studentModelList = list;
                studentListView.setAdapter(new MyStudentAdapter(getContext(), list));
                studentListView.setOnItemClickListener((parent, view, position, id) -> {
                    Bundle bundle = new Bundle();
                    StudentModel model = list.get(position);
                    String s = Utils.getGsonParser().toJson(model);
                    bundle.putString("student", s);
                    StudentProfile profile = new StudentProfile();
                    profile.setArguments(bundle);
                    getFragmentManager().beginTransaction().replace(R.id.fragContainer, profile).addToBackStack(null).commit();
                });
            }
        });
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId()==R.id.myBatchStudentList){
            MenuInflater inflater = new MenuInflater(getContext());
            inflater.inflate(R.menu.menu_list, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        CharSequence title = item.getTitle();
        AdapterView.AdapterContextMenuInfo menuinfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        if ("Edit".equals(title)) {
            StudentModel studentModel = studentModelList.get(menuinfo.position);
            AddStudent editStudent = new AddStudent();
            Bundle bundle = new Bundle();
            bundle.putString("id", studentModel.getId());
            bundle.putString("name", studentModel.getName());
            bundle.putString("depart", studentModel.getDepartment());
            bundle.putString("studentRoll", studentModel.getRoll());
            bundle.putString("studentId", studentModel.getStudentId());
            bundle.putString("batch", studentModel.getBatch());
            bundle.putString("stPhone", studentModel.getStudentPhone());
            bundle.putString("grPhone", studentModel.getGuardianPhone());
            editStudent.setArguments(bundle);
            getFragmentManager().beginTransaction().replace(R.id.fragContainer, editStudent).addToBackStack(null).commit();

        } else if ("Delete".equals(title)) {
            new FirebaseDatabaseHelper().deleteStudent(studentModelList.get(menuinfo.position).getId(),
                    new FireMan.StudentDataShort() {
                        @Override
                        public void studentIsLoaded(List<StudentModel> students) {

                        }

                        @Override
                        public void studentIsInserted() {

                        }

                        @Override
                        public void studentIsDeleted() {
                            Toast.makeText(getContext(), "Deleted", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void studentIsEdited() {

                        }
                    });
        } else {
            return false;
        }

        return true;
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

            TextView name = view.findViewById(R.id.studentName);
            name.setText(list.get(position).getName());
            TextView roll = view.findViewById(R.id.studentRoll);
            roll.setText(list.get(position).getRoll());
            return view;
        }
    }
}