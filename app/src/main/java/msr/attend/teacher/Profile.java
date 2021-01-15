package msr.attend.teacher;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import msr.attend.teacher.Model.ClassModel;
import msr.attend.teacher.Model.TeacherModel;
import msr.attend.teacher.Model.UserPref;

public class Profile extends Fragment {
    private TextView teacherName, teacherPhone, teacherEmail, teacherDepart, teacherGender;
    private ExpandableListView exListView;

    private UserPref userPref;
    private FirebaseDatabaseHelper firebaseDatabaseHelper;

    private TeacherModel currentTeacher;

    public Profile() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        teacherName = view.findViewById(R.id.teacherName);
        teacherPhone = view.findViewById(R.id.teacherPhone);
        teacherEmail = view.findViewById(R.id.teacherEmail);
        teacherDepart = view.findViewById(R.id.teacherDepart);
        teacherGender = view.findViewById(R.id.teacherGender);
        exListView = view.findViewById(R.id.exListView);

        firebaseDatabaseHelper = new FirebaseDatabaseHelper();
        userPref = new UserPref(getContext());

        getActivity().setTitle("My Profile");

        int back = getFragmentManager().getBackStackEntryCount();
        if (back > 0){
            for (int i = 0; i < back; i++) {
                getFragmentManager().popBackStack();
            }
        }

        loadTeacherInfo();

        firebaseDatabaseHelper.classModelByTeacherId(userPref.getTeacherId(), models -> {
            if (getActivity() != null){

                Comparator<String> dateComparator = (s1, s2) -> {
                    try{
                        SimpleDateFormat format = new SimpleDateFormat("EEE");
                        Date d1 = format.parse(s1);
                        Date d2 = format.parse(s2);
                        if(d1.equals(d2)){
                            return s1.compareTo(s2);
                        }else{
                            Calendar cal1 = Calendar.getInstance();
                            Calendar cal2 = Calendar.getInstance();
                            cal1.setTime(d1);
                            cal2.setTime(d2);
                            return cal1.get(Calendar.DAY_OF_WEEK) - cal2.get(Calendar.DAY_OF_WEEK);
                        }
                    }catch(ParseException pe){
                        throw new RuntimeException(pe);
                    }
                };

                Comparator<ClassModel> timeCompare = (s1, s2) -> {
                    try{
                        SimpleDateFormat format = new SimpleDateFormat("h");
                        Date d1 = format.parse(s1.getTime());
                        Date d2 = format.parse(s2.getTime());
                        if(d1.equals(d2)){
                            return s1.getTime().compareTo(s2.getTime());
                        }else{
                            Calendar cal1 = Calendar.getInstance();
                            Calendar cal2 = Calendar.getInstance();
                            cal1.setTime(d1);
                            cal2.setTime(d2);
                            return cal1.get(Calendar.HOUR_OF_DAY) - cal2.get(Calendar.HOUR_OF_DAY);
                        }
                    }catch(ParseException pe){
                        throw new RuntimeException(pe);
                    }
                };

                List<String> title = new ArrayList<>();
                HashMap<String, List<ClassModel>> map = new HashMap<>();
                for (ClassModel c : models){
                    if (!title.contains(c.getDay())){
                        title.add(c.getDay());
                    }
                }

                Collections.sort(title, dateComparator);

                for (String t : title){
                    List<ClassModel> classModels = new ArrayList<>();
                    for (ClassModel c : models){
                        if (c.getDay().equals(t)){
                            classModels.add(c);
                        }
                    }
                    Collections.sort(classModels, timeCompare);
                    map.put(t,classModels);
                }

                exListView.setAdapter(new ExpanListAdapter(getContext(),map,title));

            }
        });

        setHasOptionsMenu(true);
    }

    private void loadTeacherInfo() {
        firebaseDatabaseHelper.getProfileInfo(userPref.getTeacherId(), new FirebaseDatabaseHelper.ProfileDataShot() {
            @Override
            public void profileInfoListener(TeacherModel teacherModel) {
                currentTeacher = teacherModel;
                teacherName.setText(teacherModel.getName());
                teacherPhone.setText(teacherModel.getPhone());
                teacherEmail.setText(teacherModel.getEmail());
                teacherDepart.setText(teacherModel.getDepartment());
                teacherGender.setText(teacherModel.getGender());
            }

            @Override
            public void profileEditListener() {

            }
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.option_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.editProfile:
                editTeacher(currentTeacher);
                return true;
            case R.id.logOut:
                userPref.setIsLogin(false);
                System.exit(0);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void editTeacher(final TeacherModel teacherModel){
        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.fragment_edit_teacher);
        EditText nameEdit = dialog.findViewById(R.id.teacherName);
        EditText phoneEdit = dialog.findViewById(R.id.teacherPhoneNum);
        EditText emailEdit = dialog.findViewById(R.id.teacherEmail);
        EditText passEdit = dialog.findViewById(R.id.teacherPassword);
        Spinner departmentSelect = dialog.findViewById(R.id.departmentSelect);
        RadioGroup radioGroup = dialog.findViewById(R.id.teacherGenderRdG);
        Button signUp = dialog.findViewById(R.id.teacherSubmit);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.department_name, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        departmentSelect.setAdapter(adapter);

        nameEdit.setText(teacherModel.getName());
        phoneEdit.setText(teacherModel.getPhone());
        emailEdit.setText(teacherModel.getEmail());
        passEdit.setText(teacherModel.getPassword());
        departmentSelect.setSelection(adapter.getPosition(teacherModel.getDepartment()));
        if (teacherModel.getGender().equals("Male")) {
            radioGroup.check(R.id.maleRd);
        } else if (teacherModel.getGender().equals("Female")) {
            radioGroup.check(R.id.femaleRd);
        }

        signUp.setOnClickListener(v -> {
            int selectGender = radioGroup.getCheckedRadioButtonId();
            RadioButton genderRdBtn = dialog.findViewById(selectGender);
            String departName = departmentSelect.getSelectedItem().toString();

            TeacherModel editableTeacher = new TeacherModel(teacherModel.getId(), nameEdit.getText().toString(),
                    phoneEdit.getText().toString(), emailEdit.getText().toString(), departName,
                    genderRdBtn.getText().toString(), passEdit.getText().toString());

            firebaseDatabaseHelper.editTeacher(editableTeacher, new FirebaseDatabaseHelper.ProfileDataShot() {
                @Override
                public void profileInfoListener(TeacherModel teacherModel) {

                }

                @Override
                public void profileEditListener() {
                    Toast.makeText(getContext(), "Edit Success", Toast.LENGTH_SHORT).show();
                    loadTeacherInfo();
                }
            });

            dialog.cancel();
        });

        dialog.show();
    }

    class ExpanListAdapter extends BaseExpandableListAdapter {
        Context context;
        HashMap<String, List<ClassModel>> child;
        List<String> parent;
        char colorPos = 'x';

        public ExpanListAdapter(Context context, HashMap<String, List<ClassModel>> child, List<String> parent) {
            this.context = context;
            this.child = child;
            this.parent = parent;
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
            if (title.equals(new SimpleDateFormat("EEEE").format(Calendar.getInstance().getTime()))){
                txt.setBackgroundColor(Color.GREEN);
                colorPos = 'c';
            } else {
                txt.setBackgroundColor(Color.WHITE);
                colorPos = 'x';
            }
            txt.setText(title);
            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            ClassModel child = (ClassModel) getChild(groupPosition,childPosition);
            if (convertView == null){
                LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.ex_child_row,null);
            }
            TextView textView = convertView.findViewById(R.id.child);
            textView.setText("   "+child.getSubCode()+" -> T: "+child.getTime() + " | B: "+child.getBatch());
            if (colorPos == 'c'){
                textView.setBackgroundColor(Color.LTGRAY);
            } else {
                textView.setBackgroundColor(Color.WHITE);
            }
            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }

}