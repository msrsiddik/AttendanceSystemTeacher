package msr.attend.teacher;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

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
    private int departPos;

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

        loadTeacherInfo();

        loadClassRoutine();

        setHasOptionsMenu(true);
    }

    private void loadClassRoutine() {
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
                userPref.clear();
                System.exit(0);
                return true;
            case R.id.addRoutine:
                firebaseDatabaseHelper.routineGetMode(mode -> {
                    if (!mode.isEmpty() && mode.equals("true")){
                        addRoutine(currentTeacher.getId());
                    } else {
                        Toast.makeText(getContext(), "Contact Admin", Toast.LENGTH_SHORT).show();
                    }
                });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void addRoutine(final String id) {
        Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.add_class);

        Spinner cDepart = dialog.findViewById(R.id.classDepart);
        Spinner cBatch = dialog.findViewById(R.id.classBatch);
        Spinner cSemester = dialog.findViewById(R.id.classSemester);
        Spinner cSubCode = dialog.findViewById(R.id.classSubjectCode);
        Spinner cDay = dialog.findViewById(R.id.classDay);
        EditText cTime = dialog.findViewById(R.id.classTime);
        Button cSubmitBtn = dialog.findViewById(R.id.classSubmitBtn);

        ArrayAdapter<CharSequence> departAdapter = ArrayAdapter.createFromResource(getContext(), R.array.department_name, android.R.layout.simple_spinner_item);
        departAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cDepart.setAdapter(departAdapter);

        cDepart.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                departPos = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        firebaseDatabaseHelper.getAllRunningBatch(batchs -> {
            List<String> allBatch = new ArrayList<>(batchs);
            allBatch.add(0,"Select Batch");
            ArrayAdapter<String> batchAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, allBatch);
            cBatch.setAdapter(batchAdapter);
        });

        ArrayAdapter<CharSequence> semesterAdapter = ArrayAdapter.createFromResource(getContext(), R.array.semester, android.R.layout.simple_spinner_item);
        semesterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cSemester.setAdapter(semesterAdapter);

        int[] subCodeBySemesterCSE = {R.array.subCodeFirstItem, R.array.first_bsc_cse, R.array.second_bsc_cse, R.array.third_bsc_cse, R.array.fourth_bsc_cse,
                R.array.fifth_bsc_cse, R.array.sixth_bsc_cse, R.array.seventh_bsc_cse, R.array.eighth_bsc_cse, R.array.ninth_bsc_cse,
                R.array.tenth_bsc_cse, R.array.eleventh_bsc_cse, R.array.twelfth_bsc_cse};

        int[] subCodeBySemesterEEE = {R.array.subCodeFirstItem, R.array.first_bsc_eee, R.array.second_bsc_eee, R.array.third_bsc_eee, R.array.fourth_bsc_eee,
                R.array.fifth_bsc_eee, R.array.sixth_bsc_eee, R.array.seventh_bsc_eee, R.array.eighth_bsc_eee, R.array.ninth_bsc_eee,
                R.array.tenth_bsc_eee, R.array.eleventh_bsc_eee, R.array.twelfth_bsc_eee};

        int[] subCodeBySemesterEETE = {R.array.subCodeFirstItem, R.array.first_bsc_eete, R.array.second_bsc_eete, R.array.third_bsc_eete, R.array.fourth_bsc_eete,
                R.array.fifth_bsc_eete, R.array.sixth_bsc_eete, R.array.seventh_bsc_eete, R.array.eighth_bsc_eete, R.array.ninth_bsc_eete,
                R.array.tenth_bsc_eete, R.array.eleventh_bsc_eete, R.array.twelfth_bsc_eete};

        int[] subCodeBySemesterEnglish = {R.array.subCodeFirstItem, R.array.first_hons_english, R.array.second_hons_english, R.array.third_hons_english, R.array.fourth_hons_english,
                R.array.fifth_hons_english, R.array.sixth_hons_english, R.array.seventh_hons_english, R.array.eighth_hons_english, R.array.ninth_hons_english,
                R.array.tenth_hons_english, R.array.eleventh_hons_english, R.array.twelfth_hons_english};

        int[] subCodeBySemesterLaw = {R.array.subCodeFirstItem, R.array.first_hons_llb, R.array.second_hons_llb, R.array.third_hons_llb, R.array.fourth_hons_llb,
                R.array.fifth_hons_llb, R.array.sixth_hons_llb, R.array.seventh_hons_llb, R.array.eighth_hons_llb, R.array.ninth_hons_llb,
                R.array.tenth_hons_llb, R.array.eleventh_hons_llb, R.array.twelfth_hons_llb};

        int[] subCodeBySemesterSociology = {R.array.subCodeFirstItem, R.array.first_bss_hons_sociology, R.array.second_bss_hons_sociology, R.array.third_bss_hons_sociology, R.array.fourth_bss_hons_sociology,
                R.array.fifth_bss_hons_sociology, R.array.sixth_bss_hons_sociology, R.array.seventh_bss_hons_sociology, R.array.eighth_bss_hons_sociology, R.array.ninth_bss_hons_sociology,
                R.array.tenth_bss_hons_sociology, R.array.eleventh_bss_hons_sociology, R.array.twelfth_bss_hons_sociology};


        cSemester.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ArrayAdapter<CharSequence> subCodeAdapterCSE = ArrayAdapter.createFromResource(getContext(), subCodeBySemesterCSE[position], android.R.layout.simple_spinner_item);
                ArrayAdapter<CharSequence> subCodeAdapterEEE = ArrayAdapter.createFromResource(getContext(), subCodeBySemesterEEE[position], android.R.layout.simple_spinner_item);
                ArrayAdapter<CharSequence> subCodeAdapterEETE = ArrayAdapter.createFromResource(getContext(), subCodeBySemesterEETE[position], android.R.layout.simple_spinner_item);
                ArrayAdapter<CharSequence> subCodeAdapterEnglish = ArrayAdapter.createFromResource(getContext(), subCodeBySemesterEnglish[position], android.R.layout.simple_spinner_item);
                ArrayAdapter<CharSequence> subCodeAdapterLaw = ArrayAdapter.createFromResource(getContext(), subCodeBySemesterLaw[position], android.R.layout.simple_spinner_item);
                ArrayAdapter<CharSequence> subCodeAdapterSociology = ArrayAdapter.createFromResource(getContext(), subCodeBySemesterSociology[position], android.R.layout.simple_spinner_item);

                subCodeAdapterCSE.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                subCodeAdapterEEE.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                subCodeAdapterEETE.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                subCodeAdapterEnglish.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                subCodeAdapterLaw.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                subCodeAdapterSociology.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                Log.e("position",position+"");

                switch (departPos) {
                    case 1:
                        cSubCode.setAdapter(subCodeAdapterCSE);
                        break;
                    case 2:
                        cSubCode.setAdapter(subCodeAdapterEEE);
                        break;
                    case 3:
                        cSubCode.setAdapter(subCodeAdapterEETE);
                        break;
                    case 4:
                        cSubCode.setAdapter(subCodeAdapterEnglish);
                        break;
                    case 5:
                        cSubCode.setAdapter(subCodeAdapterLaw);
                        break;
                    case 6:
                        cSubCode.setAdapter(subCodeAdapterSociology);
                        break;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        ArrayAdapter<CharSequence> dayAdapter = ArrayAdapter.createFromResource(getContext(), R.array.dayName, android.R.layout.simple_spinner_item);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cDay.setAdapter(dayAdapter);

        cTime.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                    (view, hourOfDay, minute) -> cTime.setText(hourOfDay + " : " + minute), 12, 00, true);
            timePickerDialog.setTitle("Select Time");
            timePickerDialog.show();
        });

        cSubmitBtn.setOnClickListener(v -> {
            if (cDepart.getSelectedItemPosition() != 0
                    && !cBatch.getSelectedItem().toString().equals("Select Batch")
//                    && cSubCode.getSelectedItemPosition() != 0
                    && cDay.getSelectedItemPosition() != 0) {
                String depart = cDepart.getSelectedItem().toString();
                String batch = cBatch.getSelectedItem().toString();
                String semester = cSemester.getSelectedItem().toString();
                String subCode = cSubCode.getSelectedItem().toString();
                String day = cDay.getSelectedItem().toString();
                String time = cTime.getText().toString();
                ClassModel classModel = new ClassModel(id, depart, batch, semester, subCode, day, time);
                new FirebaseDatabaseHelper().insertClassInfo(classModel, new FireMan.ClassInfoListener() {
                    @Override
                    public void classInfoIsInserted() {
                        Toast.makeText(getContext(), "Success", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        loadClassRoutine();
                    }

                    @Override
                    public void classInfoIsLoaded(List<ClassModel> classModelList) {

                    }
                });
            } else {
                Toast.makeText(getContext(), "Failed", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void editTeacher(final TeacherModel teacherModel){
        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.fragment_edit_teacher);
        TextInputEditText nameEdit = dialog.findViewById(R.id.teacherName);
        TextInputEditText phoneEdit = dialog.findViewById(R.id.teacherPhoneNum);
        TextInputEditText emailEdit = dialog.findViewById(R.id.teacherEmail);
        TextInputEditText passEdit = dialog.findViewById(R.id.teacherPassword);
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
            TextView txt2 = convertView.findViewById(R.id.classCountV);
            TextView txt = convertView.findViewById(R.id.header);
            txt.setTypeface(null, Typeface.BOLD);
            if (title.equals(new SimpleDateFormat("EEEE").format(Calendar.getInstance().getTime()))){
                txt.setBackgroundColor(Color.GREEN);
                txt2.setBackgroundColor(Color.GREEN);
                colorPos = 'c';
            } else {
                txt.setBackgroundColor(Color.WHITE);
                txt2.setBackgroundColor(Color.WHITE);
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