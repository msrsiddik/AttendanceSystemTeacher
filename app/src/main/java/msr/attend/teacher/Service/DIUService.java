package msr.attend.teacher.Service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import msr.attend.teacher.FirebaseDatabaseHelper;
import msr.attend.teacher.Model.NoticeModel;
import msr.attend.teacher.Model.UserPref;

public class DIUService extends Service {
    private UserPref userPref;
    private FirebaseDatabaseHelper firebaseDatabaseHelper;
    private DateFormat dateFormat;

    public DIUService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        userPref = new UserPref(getApplicationContext());
        firebaseDatabaseHelper = new FirebaseDatabaseHelper();
        dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (!userPref.getTeacherId().equals("")) {
            firebaseDatabaseHelper.getNotice(userPref.getTeacherId(), noticeModels -> {
                for (NoticeModel notice : noticeModels){
                    try {
                        long validDate = dateFormat.parse(notice.getNoticeValidTime()).getTime();
                        if (validDate <= Calendar.getInstance().getTime().getTime()){
                            firebaseDatabaseHelper.removeNotice(notice.getTeacherId(),notice.getNoticeId());
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}