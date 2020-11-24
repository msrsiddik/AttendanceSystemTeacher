package msr.attend.teacher;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import msr.attend.teacher.Model.StudentModel;
import msr.attend.teacher.Model.UserPref;

public class MyBatch extends Fragment {
    private UserPref userPref;
    private ListView studentListView;
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

        new FirebaseDatabaseHelper().getMyBatchStudent(userPref.getMyBatch(), list -> {
            studentListView.setAdapter(new MyStudentAdapter(getContext(), list));
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

            TextView name = view.findViewById(R.id.studentName);
            name.setText(list.get(position).getName());
            return view;
        }
    }
}