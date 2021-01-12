package msr.attend.teacher.Messenger;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import msr.attend.teacher.Messenger.adapter.MessageAdapter;
import msr.attend.teacher.Messenger.model.Chat;
import msr.attend.teacher.Messenger.model.FireDatebase;
import msr.attend.teacher.Model.UserPref;
import msr.attend.teacher.R;


public class MessageActivity extends AppCompatActivity {
    private UserPref userPref;
    TextView username;
    ImageButton btn_send;
    EditText text_send;
    RecyclerView recyclerView;

    Intent intent;
    String userid;

    MessageAdapter messageAdapter;
    List<Chat> mchat;

    ValueEventListener seenListener;

    boolean notify = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        username = findViewById(R.id.username);
        btn_send = findViewById(R.id.btn_send);
        text_send = findViewById(R.id.text_send);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        intent = getIntent();
        getSupportActionBar().setTitle(intent.getStringExtra("username"));
        userid = intent.getStringExtra("userid");

        userPref = new UserPref(this);

        recyclerView.setAdapter(messageAdapter);

        btn_send.setOnClickListener(v -> {
            notify = true;
            String msg = text_send.getText().toString();
            if (!msg.equals("")){
                sendMessage(userPref.getTeacherId(), userid, msg);
            } else {
                Toast.makeText(MessageActivity.this, "You can't send empty message", Toast.LENGTH_SHORT).show();
            }
            text_send.setText("");
        });

        readMesagges(userPref.getTeacherId(), userid);

        seenMessage(userid);
    }

    private void seenMessage(final String userid){
        DatabaseReference reference = FireDatebase.getMessengerRef().child("Chats");
        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(userPref.getTeacherId()) && chat.getSender().equals(userid)){
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isseen", true);
                        snapshot.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage(String sender, final String receiver, String message){

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        hashMap.put("dateLong", Calendar.getInstance().getTime().getTime());
        hashMap.put("isseen", false);

        FireDatebase.getMessengerRef().child("Chats").push().setValue(hashMap);

        final DatabaseReference chatRef = FireDatebase.getMessengerRef().child("Chatlist")
                .child(userPref.getTeacherId())
                .child(userid);

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    chatRef.child("id").setValue(userid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final DatabaseReference chatRefReceiver = FireDatebase.getMessengerRef().child("Chatlist")
                .child(userid)
                .child(userPref.getTeacherId());
        chatRefReceiver.child("id").setValue(userPref.getTeacherId());

    }

    private void readMesagges(final String myid, final String userid){
        mchat = new ArrayList<>();

        FireDatebase.getMessengerRef().child("Chats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mchat.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(myid) && chat.getSender().equals(userid) ||
                            chat.getReceiver().equals(userid) && chat.getSender().equals(myid)){
                        mchat.add(chat);
                    }

                    messageAdapter = new MessageAdapter(MessageActivity.this, mchat);
                    recyclerView.setAdapter(messageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        FireDatebase.getMessengerRef().removeEventListener(seenListener);
    }
}