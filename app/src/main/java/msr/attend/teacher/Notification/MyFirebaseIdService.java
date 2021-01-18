package msr.attend.teacher.Notification;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;

import msr.attend.teacher.FirebaseDatabaseHelper;
import msr.attend.teacher.Model.UserPref;

public class MyFirebaseIdService extends FirebaseMessagingService {
    private UserPref userPref;
    @Override
    public void onNewToken(String s)
    {
        super.onNewToken(s);

        String refreshToken = FirebaseInstanceId.getInstance().getToken();

        userPref = new UserPref(this);
        if (!userPref.getTeacherId().equals("")){
            new FirebaseDatabaseHelper().setNotificationToken(refreshToken,userPref.getTeacherId());
        }

    }
}
