package msr.attend.teacher;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import msr.attend.teacher.Model.NoticeModel;
import msr.attend.teacher.Model.StudentModel;
import msr.attend.teacher.Model.UserPref;

public class NoticeBoard extends Fragment {
    private UserPref userPref;
    private ListView noticeList;
    private FloatingActionButton noticeSetBtn;
    private FragmentInterface fragmentInterface;
    private List<NoticeModel> noticeModelList;
    private FirebaseDatabaseHelper firebaseDatabaseHelper;

    public NoticeBoard() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notice_board, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        noticeList = view.findViewById(R.id.noticeList);
        noticeSetBtn = view.findViewById(R.id.noticeSetBtn);

        userPref = new UserPref(getContext());
        fragmentInterface = (FragmentInterface) getActivity();
        getActivity().setTitle("Notice Board");

        firebaseDatabaseHelper = new FirebaseDatabaseHelper();

        getFragmentManager().popBackStack("NoticeSet", FragmentManager.POP_BACK_STACK_INCLUSIVE);

        noticeSetBtn.setOnClickListener(v -> {
            fragmentInterface.gotoNoticeSet();
        });

        firebaseDatabaseHelper.getNotice(userPref.getTeacherId(), noticeModels -> {
            if (getActivity() != null){
                this.noticeModelList = noticeModels;
                noticeList.setAdapter(new NoticeAdapter(getContext(), noticeModels));
            }
        });

        noticeList.setOnItemClickListener((parent, view1, position, id) -> {
            Dialog dialog = new Dialog(getContext());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.notice_view);

            TextView title, body;
            title = dialog.findViewById(R.id.title);
            body = dialog.findViewById(R.id.body);

            NoticeModel model = noticeModelList.get(position);
            title.setText(model.getNoticeTitle());
            body.setText(model.getNoticeBody());

            dialog.show();
        });

        registerForContextMenu(noticeList);

    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId()==R.id.noticeList){
            menu.add(0,v.getId(),0,"Delete");
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        CharSequence title = item.getTitle();
        AdapterView.AdapterContextMenuInfo menuinfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        if ("Delete".equals(title)) {
            firebaseDatabaseHelper.removeNotice(userPref.getTeacherId(),noticeModelList.get(menuinfo.position).getNoticeId());
            Toast.makeText(getContext(), "Deleted", Toast.LENGTH_SHORT).show();
        } else {
            return false;
        }

        return true;
    }

    class NoticeAdapter extends ArrayAdapter<NoticeModel>{
        Context context = null;
        List<NoticeModel> list = null;

        public NoticeAdapter(@NonNull Context context, @NonNull List<NoticeModel> objects) {
            super(context, R.layout.notice_row, objects);
            this.context = context;
            this.list = objects;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.notice_row, parent, false);

            TextView validDate, batch, noticeTitle, noticeBody;
            validDate = view.findViewById(R.id.validDate);
            batch = view.findViewById(R.id.batch);
            noticeTitle = view.findViewById(R.id.noticeTitle);
            noticeBody = view.findViewById(R.id.noticeBody);

            NoticeModel notice = list.get(position);
            validDate.setText(notice.getNoticeValidTime());
            batch.setText(notice.getBatch());
            noticeTitle.setText(notice.getNoticeTitle());
            noticeBody.setText(notice.getNoticeBody());

            return view;
        }
    }
}