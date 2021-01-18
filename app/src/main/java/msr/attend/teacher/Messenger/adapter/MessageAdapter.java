package msr.attend.teacher.Messenger.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import msr.attend.teacher.Messenger.model.Chat;
import msr.attend.teacher.Messenger.model.FireDatebase;
import msr.attend.teacher.Model.UserPref;
import msr.attend.teacher.R;


public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    public static  final int MSG_TYPE_LEFT = 0;
    public static  final int MSG_TYPE_RIGHT = 1;

    private Context mContext;
    private List<Chat> mChat;

    private DateFormat format = new SimpleDateFormat("dd/MM/yy h:mm a");

    public MessageAdapter(Context mContext, List<Chat> mChat){
        this.mChat = mChat;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right, parent, false);
            return new ViewHolder(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left, parent, false);
            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Chat chat = mChat.get(position);

        holder.show_message.setText(chat.getMessage());
        holder.time.setText(format.format(new Date(chat.getDateLong())));

        if (position == mChat.size()-1){
            if (chat.isIsseen()){
                holder.txt_seen.setText("Seen");
            } else {
                holder.txt_seen.setText("Delivered");
            }
        } else {
            holder.txt_seen.setVisibility(View.GONE);
        }

        holder.show_message.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setIcon(R.drawable.messenger);
                builder.setTitle(mChat.get(position).getMessage());
                builder.setMessage("Do you want to delete this message");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (mChat.get(position).getSender().equals(new UserPref(mContext).getTeacherId())) {
                            FireDatebase.getMessengerRef().child("Chats").child(mChat.get(position).getMsgId()).setValue(null);
                            Toast.makeText(mContext, "Deleted", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(mContext, "Delete only your MSG", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
                builder.show();

                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mChat.size();
    }

    public  class ViewHolder extends RecyclerView.ViewHolder{

        public TextView show_message;
        public TextView txt_seen;
        public TextView time;

        public ViewHolder(View itemView) {
            super(itemView);

            show_message = itemView.findViewById(R.id.show_message);
            txt_seen = itemView.findViewById(R.id.txt_seen);
            time = itemView.findViewById(R.id.time);
        }
    }

    @Override
    public int getItemViewType(int position) {
        UserPref userPref = new UserPref(mContext);
        if (mChat.get(position).getSender().equals(userPref.getTeacherId())){
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }
}
