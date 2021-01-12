package msr.attend.teacher;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import msr.attend.teacher.Model.ClassAttendModel;
import msr.attend.teacher.Model.ClassModel;
import msr.attend.teacher.Model.StudentModel;
import msr.attend.teacher.Model.Utils;

public class MyBatchAttendaceDateByDate extends Fragment {
    private ExpandableListView expanListView;
    private String batch;
    private String subCode;
    private FirebaseDatabaseHelper firebaseDatabaseHelper;

    public MyBatchAttendaceDateByDate() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_batch_attendace_date_by_date, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        expanListView = view.findViewById(R.id.expanListView);

        Bundle bundle = getArguments();
        batch = bundle.getString("batch");
        subCode = bundle.getString("subCode");

        firebaseDatabaseHelper = new FirebaseDatabaseHelper();

        firebaseDatabaseHelper.getAllAttendanceInfoByBatchAndSubjectCode(batch,subCode,attendList -> {
            if (getActivity() != null){
                DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                List<String> date = new ArrayList<>();
                for (ClassAttendModel classAttend : attendList){
                    date.add(format.format(new Date(Long.parseLong(classAttend.getDate()))));
                }

                List<String> parent = new ArrayList<>(new HashSet<>(date));
                HashMap<String, List<String>> child = new HashMap<>();
                for (String p : parent){
                    List<String> list = new ArrayList<>();
                    for (ClassAttendModel attendModel : attendList){
                        if (format.format(new Date(Long.parseLong(attendModel.getDate()))).equals(p)){
                            list.add(attendModel.getStuId());
                        }
                    }
                    child.put(p,list);
                }

                firebaseDatabaseHelper.getMyBatchStudent(batch, list -> {
                    expanListView.setAdapter(new ExpanListAdapter(getContext(), child,parent,list));
                });

            }
        });

    }

    class ExpanListAdapter extends BaseExpandableListAdapter {
        Context context;
        HashMap<String, List<String>> child;
        List<String> parent;
        List<StudentModel> students;

        public ExpanListAdapter(Context context, HashMap<String, List<String>> child, List<String> parent, List<StudentModel> students) {
            this.context = context;
            this.child = child;
            this.parent = parent;
            this.students = students;
        }

        @Override
        public int getGroupCount() {
            return parent.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return child.get(parent.get(groupPosition)).size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return parent.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return child.get(parent.get(groupPosition)).get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return groupPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            String title = (String) getGroup(groupPosition);
            if (convertView == null){
                LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.ex_header_row,null);
            }
            TextView txt = convertView.findViewById(R.id.header);
            txt.setTypeface(null, Typeface.BOLD);
            txt.setText(title);
            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            String child = (String) getChild(groupPosition,childPosition);
            if (convertView == null){
                LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.ex_child_row,null);
            }
            TextView textView = convertView.findViewById(R.id.child);
            for (StudentModel student : students){
                if (student.getId().equals(child)) {
                    textView.setText(student.getName()+", "+student.getRoll());
                    textView.setOnClickListener(v -> {
                        Bundle bundle = new Bundle();
                        String s = Utils.getGsonParser().toJson(student);
                        bundle.putString("student", s);
                        bundle.putString("subCode", subCode);
                        bundle.putString("batch", batch);
                        bundle.putInt("totalClass",this.parent.size());
                        MyStudent myStudent = new MyStudent();
                        myStudent.setArguments(bundle);
                        getFragmentManager().beginTransaction().replace(R.id.fragContainer, myStudent).addToBackStack(null).commit();
                    });
                }
            }
            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }
}