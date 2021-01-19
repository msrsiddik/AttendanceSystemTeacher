package msr.attend.teacher;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import msr.attend.teacher.Model.ClassModel;
import msr.attend.teacher.Model.UserPref;

public class MyClass extends Fragment {
    private UserPref userPref;
    private ListView myClassList;
    private FragmentInterface fragmentInterface;
    private List<ClassModel> classModels;

    public MyClass() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_class, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        myClassList = view.findViewById(R.id.myCLassList);
        userPref = new UserPref(getContext());

        fragmentInterface = (FragmentInterface) getActivity();
        getActivity().setTitle("My Class");

        todayClassList();

        myClassList.setOnItemClickListener((parent, view1, position, id) ->
                fragmentInterface.gotoAttendanceRegister(classModels.get(position)));


        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.register_option_menu,menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.allClass:
                allDayClassList();
                break;
            case R.id.todayClass:
                todayClassList();
                break;
        }
        return true;
    }

    private void allDayClassList(){
        new FirebaseDatabaseHelper().getClassInfo(userPref.getTeacherId(), new FireMan.ClassInfoListener() {
            @Override
            public void classInfoIsLoaded(List<ClassModel> list) {
                if (getActivity() != null) {
                    classModels = list;
                    MyClassAdapter adapter = new MyClassAdapter(getContext(), list);
                    myClassList.setAdapter(adapter);
                }
            }

            @Override
            public void classInfoIsInserted() {

            }
        });
    }

    private void todayClassList(){
        new FirebaseDatabaseHelper().getClassInfo(userPref.getTeacherId(), new FireMan.ClassInfoListener() {
            @Override
            public void classInfoIsLoaded(List<ClassModel> list) {
                if (getActivity() != null) {
                    List<ClassModel> classList = new ArrayList<>();
                    for (ClassModel model : list) {
                        if (model.getDay().equals(new SimpleDateFormat("EEEE").format(Calendar.getInstance().getTime()))){
                            classList.add(model);
                        }
                    }
                    classModels = classList;
                    MyClassAdapter adapter = new MyClassAdapter(getContext(), classList);
                    myClassList.setAdapter(adapter);
                }
            }

            @Override
            public void classInfoIsInserted() {

            }
        });
    }

    class MyClassAdapter extends ArrayAdapter<ClassModel> {
        Context context;
        List<ClassModel> list = null;
        public MyClassAdapter(@NonNull Context context, @NonNull List<ClassModel> objects) {
            super(context, R.layout.my_class_row, objects);
            this.context = context;
            this.list = objects;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            View view = inflater.inflate(R.layout.my_class_row, parent, false);

            TextView courseCode = view.findViewById(R.id.courseCode);
            courseCode.setText(list.get(position).getSubCode());

            TextView batch = view.findViewById(R.id.batch);
            batch.setText(list.get(position).getBatch());

            return view;
        }

    }
}