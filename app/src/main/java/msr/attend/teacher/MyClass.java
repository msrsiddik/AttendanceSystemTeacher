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

        new FirebaseDatabaseHelper().getClassInfo(userPref.getTeacherId(), list -> {
            if (getActivity() != null) {
                classModels = list;
                MyClassAdapter adapter = new MyClassAdapter(getContext(), list);
                myClassList.setAdapter(adapter);
            }
        });

        myClassList.setOnItemClickListener((parent, view1, position, id) ->
                fragmentInterface.gotoAttendanceRegister(classModels.get(position)));
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